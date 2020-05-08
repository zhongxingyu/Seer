 /*
  * Copyright 2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.evinceframework.web.dojo.mvc.view;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.servlet.View;
 import org.springframework.web.servlet.support.RequestContextUtils;
 
 import com.evinceframework.web.dojo.json.JsonStoreEngine;
 import com.evinceframework.web.dojo.mvc.view.config.DojoConfiguration;
 import com.evinceframework.web.dojo.mvc.view.config.DojoConfigurationResolver;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 /**
  * Renders an HTML page that has the following structure where the content described between
  * pipes (|) is provided by the {@link DojoLayout} or {@link DojoConfiguration}. 
  * 
  * <code>
  * 	|DOCTYPE|
  * 	<html>
  * 		<head>
  * 		|HEAD CONTENT|
  *  	</head>
  *  	<body class="|BODY CSS|">
  *  		|BODY CONTENT|
  *  		<script src="dojo.js"></script>
  *  		
  *  		|DOJO INITIALIZATION CODE|
  *  	</body>
  * 	</html> 
  * </code>
  * 
  * @author Craig Swing
  */
 public class DojoView implements View {
 	
 	private static final String DEFAULT_CONTENT_TYPE = "text/html";
 	
 	private ObjectMapper mapper = new ObjectMapper();
 	
 	private JsonStoreEngine jsonEngine;
 	
 	private String[] storeNames;
 	
 	private DojoConfigurationResolver configurationResolver;
 	
 	private DojoLayout layout;
 	
 	private AuthenticationDetailsProvider<?> authenticationDetailsProvider;
 	
 	@Override
 	public String getContentType() {
 		return DEFAULT_CONTENT_TYPE;
 	}
 
 	public DojoConfigurationResolver getConfigurationResolver() {
 		return configurationResolver;
 	}
 
 	public DojoView(JsonStoreEngine jsonEngine, String[] storeNames, DojoConfigurationResolver configurationResolver, 
 			DojoLayout layout, AuthenticationDetailsProvider<?> authProvider) {
 		this.jsonEngine = jsonEngine;
 		this.storeNames = storeNames;
 		this.configurationResolver = configurationResolver;
 		this.layout = layout;
 		this.authenticationDetailsProvider = authProvider;
 	}
 
 	@Override
 	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
 				
 		DojoViewRenderingContext ctx = new DojoViewRenderingContext(
 				getConfigurationResolver().resolve(model, request, response), model, request, response);
 		DojoConfiguration cfg = ctx.getConfiguration();
 		
 		PrintWriter writer = response.getWriter();
 
 		writer.write(layout.getDocType(ctx));
 		writer.write("\n<html>\n<head>\n");
 		
 		layout.renderHeadContent(writer, ctx);
 		
 		writer.write("\n\n</head>\n");
 		writer.write(String.format("<body class=\"%s\">\n\n", cfg.getBodyCss()));
 	
 		layout.renderBodyContent(writer, ctx);
 		
 		// Render the main script with the configuration
 		writeDojoConfiguration(writer, cfg, ctx);
 		writer.write("\n\n<script type=\"text/javascript\" ");
 		writer.write(String.format(" src=\"%s\"", 
 				PathUtils.buildScriptPath(ctx, cfg.getCoreDojoPath())));
 		writer.write(">\n</script>\n");
 		
 		for(String path : cfg.getAuxiliaryScriptPaths()) {			
 			writer.write(String.format(
 					"<script type=\"text/javascript\" src=\"%s\"></script>\n", 
 					PathUtils.buildScriptPath(ctx, path)));
 		}
 		
 		renderInitializationCode(writer, ctx, cfg);
 		
 		writer.write("</script>\n");
 		writer.write("\n</body>\n</html>");
 	}
 	
 	protected void writeDojoConfiguration(PrintWriter writer, DojoConfiguration cfg, DojoViewRenderingContext ctx) throws IOException {
 		
 		Map<String, Object> params = new HashMap<String, Object>(cfg.getConfigParameters()); 
 
 		if(!params.containsKey("parseOnLoad")) {
 			params.put("parseOnLoad", true);
 		}
 		if(!params.containsKey("async")) {
 			params.put("async", true);
 		}
 		if(!params.containsKey("isDebug")) {
 			//params.put("isDebug", cfg.isD);
 		}
 		params.put("contextPath", ctx.getRequest().getContextPath());
 		
 		Locale locale = RequestContextUtils.getLocale(ctx.getRequest());
		params.put("locale", locale.toString());
 		
 		params.put("paths", cfg.getSourcePaths());
 		params.put("user", authenticationDetailsProvider.getUserDetails());
 		params.put("roles", authenticationDetailsProvider.getSecurityRoles()); 
 				
 		writer.write("\n\n<script type=\"text/javascript\">");
 		writer.write("var dojoConfig =");
 		writer.write(mapper.writeValueAsString(params));
 		writer.write(";</script>");
 	}
 	
 	protected void renderInitializationCode(PrintWriter writer, DojoViewRenderingContext ctx, DojoConfiguration cfg) {
 		
 		writer.write("<script type=\"text/javascript\">");
 		
 		writer.write("\nrequire(['dojo/_base/lang', 'dojo/io-query', 'dojo/ready', 'dojo/store/Observable', ");
 		writer.write("'evf/store/ViewModel', 'evf/_lang', 'dojo/domReady!'],");
 		writer.write("\nfunction(lang, ioQuery, ready, Observable, ViewModel) {");
 		
 		writer.write("\nready(20, function(){");
 		
 		writer.write("\n\tvar uri = window.location.href;");
 		writer.write("\n\tvar urlParams = ioQuery.queryToObject(uri.substring(uri.indexOf('?') + 1, uri.length));");
 		writer.write("\n\tlang.setObject('urlParams', urlParams);\n");
 		
 		for (String storeName : storeNames) {
 			Object obj = ctx.getModel().get(storeName);
 			writer.write(String.format("\n\tlang.setObject('%s', new Observable(new ViewModel({data: %s})));", 
 					storeName, jsonEngine.serialize(obj)));
 		}
 		writer.write("\n});\n});\n\n");
 		
 		cfg.renderJavascript(writer, ctx);
 		layout.renderJavascript(writer, ctx);
 	}
 	
 }
