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
 
 package org.seasr.meandre.components.tools.webservice;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContextProperties;
 import org.meandre.webui.ConfigurableWebUIFragmentCallback;
 import org.seasr.meandre.components.tools.Names;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         creator = "Boris Capitanu",
         description = "Configurable service head for a service that gets data via POSTs",
         name = "Configurable Service Head Post",
         tags = "WebUI, post, process request, configurable",
         rights = Licenses.UofINCSA,
         mode = Mode.webui,
         firingPolicy = FiringPolicy.all,
         baseURL = "meandre://seasr.org/components/foundry/"
 )
 public class ConfigurableServiceHeadPost extends ServiceHeadPost implements ConfigurableWebUIFragmentCallback {
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             description = "The URL context path that the component will respond to",
             name = Names.PROP_URL_CONTEXT_PATH,
             defaultValue = "/service/post"
     )
     public final static String PROP_URL_CONTEXT_PATH = Names.PROP_URL_CONTEXT_PATH;
 
     //--------------------------------------------------------------------------------------------
 
    @Override
    public String getContextPath(ComponentContextProperties ccp) {
        return ccp.getProperty(PROP_URL_CONTEXT_PATH);
     }
 
    public String getContextPath() {
        return getContextPath(componentContext);
    }
 }
