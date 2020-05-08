 /*
  * Copyright (c) 2009 ioko365 Ltd
  *
  * This file is part of ioko tapestry-commons.
  *
  *     Foobar is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Foobar is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with ioko tapestry-commons.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.co.ioko.tapestry.caching.services;
 
 import org.apache.tapestry5.Asset;
 import org.apache.tapestry5.ComponentResources;
 import org.apache.tapestry5.FieldFocusPriority;
 import org.apache.tapestry5.RenderSupport;
 import org.apache.tapestry5.ioc.internal.util.Defense;
 import org.apache.tapestry5.json.JSONArray;
 import org.apache.tapestry5.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.co.ioko.tapestry.caching.services.support.MethodCall;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Proxy class for RenderSupport that records all methods called on it.  This is so we can 'playback'
  * method calls for cached components.
  *
  * @author seldred
  */
 public class RenderSupportRecorder implements RenderSupport {
 
 	private static final Logger logger = LoggerFactory.getLogger(RenderSupportRecorder.class);
 
 	private List<MethodCall> methodCalls;
 
 	private RenderSupport renderSupport;
 
 	public RenderSupportRecorder(RenderSupport renderSupport) {
 		this.renderSupport = renderSupport;
 	}
 
 	// == IMPLEMENT ALL METHODS IN RENDER SUPPORT ==
 
 	public void addClasspathScriptLink(String... classpaths) {
 		Method method = getMethod("addClasspathScriptLink", String[].class);
		recordMethodCall(method, new Object[] { classpaths});
 		renderSupport.addClasspathScriptLink(classpaths);
 	}
 
 	public void addInit(String functionName, JSONArray parameterList) {
 		Method method = getMethod("addInit", String.class, JSONArray.class);
 		recordMethodCall(method, functionName, parameterList);
 		renderSupport.addInit(functionName, parameterList);
 	}
 
 	public void addInit(String functionName, JSONObject parameter) {
 		Method method = getMethod("addInit", String.class, JSONObject.class);
 		recordMethodCall(method, functionName, parameter);
 		renderSupport.addInit(functionName, parameter);
 	}
 
 	public void addInit(String functionName, String... parameters) {
 		Method method = getMethod("addInit", String.class, String[].class);
 		recordMethodCall(method, functionName, parameters);
 		renderSupport.addInit(functionName, parameters);
 	}
 
 	public void addScript(String format, Object... arguments) {
 		Method method = getMethod("addScript", String.class, Object[].class);
 		recordMethodCall(method, format, arguments);
 		renderSupport.addScript(format, arguments);
 	}
 
 	public void addScript(String script) {
 		Method method = getMethod("addScript", String.class);
 		recordMethodCall(method, script);
 		renderSupport.addScript(script);
 	}
 
 	public void addScriptLink(Asset... scriptAssets) {
 		List<String> assets = new ArrayList<String>();
         for (Asset asset : scriptAssets) {
             Defense.notNull(asset, "scriptAsset");
             assets.add(asset.toClientURL());
         }
         addScriptLink(assets.toArray(new String[assets.size()]));
 	}
 
 	public void addScriptLink(String... scriptURLs) {
 		Method method = getMethod("addScriptLink", String[].class);
 		recordMethodCall(method, new Object[] {scriptURLs});
 		renderSupport.addScriptLink(scriptURLs);
 	}
 
 	public void addStylesheetLink(Asset stylesheet, String media) {
         Defense.notNull(stylesheet, "stylesheet");
         addStylesheetLink(stylesheet.toClientURL(), media);
 	}
 
 	public void addStylesheetLink(String stylesheetURL, String media) {
 		Method method = getMethod("addStylesheetLink", String.class, String.class);
 		recordMethodCall(method, stylesheetURL, media);
 		renderSupport.addStylesheetLink(stylesheetURL, media);
 	}
 
 	public String allocateClientId(ComponentResources resources) {
 		return allocateClientId(resources.getId());
 	}
 
 	public String allocateClientId(String id) {
 		Method method = getMethod("allocateClientId", String.class);
 		recordMethodCall(method, id);
 		return renderSupport.allocateClientId(id);
 	}
 
 	public void autofocus(FieldFocusPriority priority, String fieldId) {
 		Method method = getMethod("autofocus", FieldFocusPriority.class, String.class);
 		recordMethodCall(method, priority, fieldId);
 		renderSupport.autofocus(priority, fieldId);
 	}
 
 	// =============================================
 
 	public List<MethodCall> getMethodCalls() {
 		return methodCalls;
 	}
 
 	private void recordMethodCall(Method method, Object... params) {
 		MethodCall methodCall = new MethodCall(method.getName(), method.getParameterTypes(), params);
 		if (methodCalls == null) {
 			methodCalls = new ArrayList<MethodCall>();
 		}
 		methodCalls.add(methodCall);
 	}
 
 	private Method getMethod(String name, Class<?>... parameterTypes) {
 		try {
 			return RenderSupport.class.getMethod(name, parameterTypes);
 		}
 		catch (NoSuchMethodException e) {
 			logger.error("{}", e);
 			throw new RuntimeException(e);
 		}
 	}
 }
