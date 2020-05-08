 package org.seasr.meandre.components.vis.flash;
 
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
 
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.webui.ConfigurableWebUIFragmentCallback;
 import org.meandre.webui.WebUIException;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.tools.Names;
 
 
 
 import org.seasr.meandre.components.tools.text.io.GenericTemplate;
 
 
 /**
  * @author Mike Haberman
  * 
  * If a flow is used to serve data that will be fetched via a Flash client, then that client
  * will request a crossdomain.xml file.  This component can be used to register the correct 
  * context path and deliver that file.  Note this is ONLY needed when the data and the client (Flash) 
  * are served via different domains.
  * 
  */
 
 @Component(
         creator = "Mike Haberman",
         description = "flash xml cross domain policy requests",
         name = "Cross Domain Policy Server",
         tags = "flash",
         rights = Licenses.UofINCSA,
         mode = Mode.webui,
         baseURL = "meandre://seasr.org/components/",
        dependency = { "velocity-1.6.2-dep.jar" },
         resources = {"CrossDomainPolicyServer.vm"}
 )
 
 public class CrossDomainPolicyServer extends GenericTemplate 
      implements ConfigurableWebUIFragmentCallback  
      {
 	
     //------------------------------ PROPERTIES --------------------------------------------------
     @ComponentProperty(
 	        description = "The template name",
 	        name = GenericTemplate.PROP_TEMPLATE,
 	        defaultValue = "org/seasr/meandre/components/vis/flash/CrossDomainPolicyServer.vm"
 	)
     protected static final String PROP_TEMPLATE = GenericTemplate.PROP_TEMPLATE;
 
     //--------------------------------------------------------------------------------------------
     public String getContextPath() {
     	return "/crossdomain.xml";
     }
 
    @Override
 	public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
 	{
 	    super.initializeCallBack(ccp);
 	    // console.info("Service location: " + ccp.getWebUIUrl(true) + ccp.getExecutionInstanceID());
 	}
    
     public void handle(HttpServletRequest request, HttpServletResponse response) throws WebUIException
     {
        console.info("request made for crossdomain file");
 	   
 	   super.emptyRequest(response);
 	   
 	   response.setContentType("text/xml");
    }
 
 	@Override
 	protected boolean processRequest(HttpServletRequest request) throws IOException 
 	{   
 	    return true;
 	}
 	
 }
