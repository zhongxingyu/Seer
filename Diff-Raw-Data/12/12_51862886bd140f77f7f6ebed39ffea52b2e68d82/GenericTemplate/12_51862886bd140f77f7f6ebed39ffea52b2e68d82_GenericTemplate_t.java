 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.components.tools.text.io;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 import org.apache.velocity.VelocityContext;
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.webui.ConfigurableWebUIFragmentCallback;
 import org.meandre.webui.WebUIException;
 import org.meandre.webui.WebUIFragmentCallback;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.generic.html.VelocityTemplateService;
 
 /**
  * template loading notes:
  *
  *  templates are searched in
  *     1 local file system on the server: published_resources/templates (under the server install)
  *     2 local file system on the server: ./templates  where . is user.path
  *     3 on the classpath
  *     4 in any jars on the classpath
  *
  *     the default template is GUITemplate.vm
  *
  *  local files take precedence over class path files
  *
  *  ComponentContextProperties  are in the velocity context as ccp
  *  ComponentContext             is in the velocity context as cc
  *  HttpResponse                 is in the velocity context as response
  *  User supplied key=value pairs are in the velocity context as userMap
  *
  *  TODO: it would be nice to be able to put the HttpRequest object
  *  in the velocity context
  *
  *  @author Mike Haberman
  *  @author Boris Capitanu
  */
 
 
 
 @Component(
         creator = "Mike Haberman",
         description = "Generates and displays a webpage via a Velocity Template ",
         name = "Generic Template",
         tags = "string, visualization",
         rights = Licenses.UofINCSA,
         mode = Mode.webui,
         baseURL = "meandre://seasr.org/components/foundry/",
         resources = { "GenericTemplate.vm" },
         dependency = { "velocity-1.6.2-dep.jar" }
 )
 public class GenericTemplate extends AbstractExecutableComponent 
    implements ConfigurableWebUIFragmentCallback 
    {
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
 	@ComponentProperty(
 	        description = "The template name",
 	        name = Names.PROP_TEMPLATE,
 	        defaultValue = "org/seasr/meandre/components/tools/text/io/GenericTemplate.vm"
 	)
 	protected static final String PROP_TEMPLATE = Names.PROP_TEMPLATE;
 
 	@ComponentProperty(
 	        description = "User supplied property list (key=value comma-separated pairs)",
 	        name = Names.PROP_PROPERTIES,
 	        defaultValue = ""
 	)
 	protected static final String PROP_TEMPLATE_PROPERTIES = Names.PROP_PROPERTIES;
 
     //--------------------------------------------------------------------------------------------
 
 
     protected VelocityContext context;
     protected String templateName;
 
     // convenience properties to easily push additional properties
     // not needed, template can always do $ccp.getProperty("title")
     protected String[] templateVariables = {};
 
     protected boolean done;
 
 
     //--------------------------------------------------------------------------------------------
     public String getContextPath() {
     	return "/";
     }
     
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         templateName = ccp.getProperty(PROP_TEMPLATE);
 
         VelocityTemplateService velocity = VelocityTemplateService.getInstance();
         context = velocity.getNewContext();
 
         /*
          *  Make a context object and populate with the data.  This
          *  is where the Velocity engine gets the data to resolve the
          *  references (ex. $date) in the template
          */
 
         context.put("dir",  System.getProperty("user.dir"));
         context.put("date", new Date());
         context.put("ccp",  ccp);
 
         String toParse = ccp.getProperty(PROP_TEMPLATE_PROPERTIES);
         HashMap<String,String> map = new HashMap<String,String>();
         StringTokenizer tokens = new StringTokenizer(toParse, ",");
         while (tokens.hasMoreTokens()){
             String kv = tokens.nextToken();
             int idx = kv.indexOf('=');
             if (idx > 0) {
                 String key   = kv.substring(0,idx);
                 String value = kv.substring(idx+1);
                 map.put(key.trim(), value.trim());
             }
         }
         context.put("userMap", map);
 
         // push property values to the context
         for (String name: templateVariables) {
             String value = ccp.getProperty(name);
             context.put(name,value);
         }
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
     	
         String sInstanceId = cc.getExecutionInstanceID();
         context.put("sInstanceId", sInstanceId);
         context.put("cc", cc);
         context.put("gui", this);
         String webUIUrl = cc.getWebUIUrl(true).toString();
         if (webUIUrl.endsWith("/")) webUIUrl = webUIUrl.substring(0, webUIUrl.length()-1);
         context.put("webUIUrl", webUIUrl);
         
         console.info("webUIUrl " + webUIUrl);
 
         done = false;
 
         cc.startWebUIFragment(this);
 
         while (!cc.isFlowAborting() && !done)
             Thread.sleep(1000);
 
         if (cc.isFlowAborting())
             console.info("Flow abort requested - terminating component execution...");
 
         console.finest("Stopping WebUI fragment");
 
         cc.stopWebUIFragment(this);
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     //--------------------------------------------------------------------------------------------
 
     public void emptyRequest(HttpServletResponse response) throws WebUIException
     {
     	console.info("WARNING, emptyRequest() was called on a configuable web ui");
     	//
     	// since this is now a configurable web ui, this method
     	// will NOT be called
     	//
         console.entering(getClass().getName(), "emptyRequest", response);
 
     	// TODO: it would be nice to see the request object here
     	generateContent(null, response);
 
         console.exiting(getClass().getName(), "emptyRequest");
 
     }
 
     public void handle(HttpServletRequest request, HttpServletResponse response) throws WebUIException
     {
         console.entering(getClass().getName(), "handle", response);
         
        StringBuffer sb = request.getRequestURL();
        sb.append(" <Query Data> ").append(request.getQueryString());
        console.info("Request: " + sb.toString());
        
        
         Map pMap = request.getParameterMap();
         if (pMap.size() == 0) {
         	// there are no parameters, it's an empty request
         	generateContent(request, response);
         	console.exiting(getClass().getName(), "handle/generateContent, no data");
         	return;
         }
         
         
         //
         // this is the workhorse method
         // process the input, determine any errors,
         // set up any output that will be pushed
         //
         try {
            if (!processRequest(request)) {
         	   
         	   // regenerate the template
                generateContent(request, response);
                console.exiting(getClass().getName(), "handle/generateContent");
                return;
             }
         } catch(IOException ioe) {
             throw new WebUIException(ioe);
         }
 
         //
         // see if this component can handle multiple requests before
         // releasing the semaphore,
        //
         if (expectMoreRequests(request)) {
             console.finest("Expecting more requests");
             console.exiting(getClass().getName(), "handle/expecting more requests");
             return;
         }
 
         console.finest("done = true");
         done = true;
 
         // No Errors,
         // just push the browser to the "next" component
         try{
             generateMetaRefresh(response);
         } catch (IOException e) {
             throw new WebUIException("Unable to generate redirect response", e);
         }
 
         console.exiting(getClass().getName(), "handle");
     }
 
     //--------------------------------------------------------------------------------------------
 
     protected void generateContent(HttpServletRequest request, HttpServletResponse response) throws WebUIException
     {
         if (done) {
             try {
                 generateMetaRefresh(response);
             }
             catch (IOException e) {
                 throw new WebUIException(e);
             }
             return;
         }
 
         try {
         	
             // request could be null on the "first" request
         	// TODO: put the request parameters or a wrapper in the context
             
             context.put("request",  request);
             context.put("response", response);
 
             // render the template
             VelocityTemplateService velocity = VelocityTemplateService.getInstance();
             String html = velocity.generateOutput(context, templateName);
 
             // write the template to the response stream
             response.getWriter().println(html);
 
         } catch (Exception e) {
         	
         	try {
         		response.getWriter().println("An exception occurred, check the logs<br/>" + e.toString());
         	}
         	catch (Exception ee) {
         	}
         	
             throw new WebUIException(e);
         }
     }
 
     protected void generateMetaRefresh(HttpServletResponse response) throws IOException
     {
         console.finest("Sending refresh request");
         response.getWriter().println("<html><head><meta http-equiv='REFRESH' content='1;url=/'></head><body></body></html>");
     }
 
     protected boolean expectMoreRequests(HttpServletRequest request)
     {
     	// we are done, e.g. ?done=true
     	// if the request does NOT contain this parameter
     	// we will assume, we are expecting more input
     	return (request.getParameter("done") == null);
     }
     
     
     //
     // not only check errors, but process the input at this step
     // return false on errors, bad input, or you want to regenerate the template/html
     // return true if all processing is completed
     
     // NOTE: subclasses SHOULD override this method
     protected boolean processRequest(HttpServletRequest request) throws IOException
     {
     	console.finest("subclass did NOT override processRequest(), returning false");
     	
     	 // populate the velocity context with error objects
          // e.g. context.put("hasErrors", new Boolean(true));
        
        
     	return false;
     }
 }
