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
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.meandre.components.tools.Names;
 
 /**
  * @author Lily Dong
  * @author Mike Haberman
  * @author Boris Capitanu
  */
 
 @Component(
         creator = "Lily Dong",
         description = "Presents a simple text area for user to input string.",
         name = "Input Text",
         tags = "string, visualization",
         rights = Licenses.UofINCSA,
         mode = Mode.webui,
         baseURL = "meandre://seasr.org/components/",
         dependency = { "velocity-1.6.1-dep.jar" },
         resources = { "InputText.vm" }
 )
 public class InputText extends GenericTemplate {
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             description = "The text",
             name = Names.PORT_TEXT
     )
     protected static final String OUT_TEXT = Names.PORT_TEXT;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
 	//
 	// specific to this component
 	//
 	@ComponentProperty(
 	        description = "The title for the page",
 	        name = Names.PROP_TITLE,
 	        defaultValue = "Input a string"
 	)
 	protected static final String PROP_TITLE = Names.PROP_TITLE;
 
 	@ComponentProperty(
 	        description = "The message to present to the user",
 	        name = Names.PROP_MESSAGE,
 	        defaultValue = "Please input a string"
 	)
 	protected static final String PROP_MESSAGE = Names.PROP_MESSAGE;
 
 	@ComponentProperty(
 	        description = "Default value to use for the input",
 	        name = Names.PROP_DEFAULT,
 	        defaultValue = ""
 	)
 	protected static final String PROP_DEFAULT = Names.PROP_DEFAULT;
 
 	@ComponentProperty(
 	        description = "The template name",
 	        name = GenericTemplate.PROP_TEMPLATE,
 	        defaultValue = "org/seasr/meandre/components/tools/text/io/InputText.vm"
 	)
     protected static final String PROP_TEMPLATE = GenericTemplate.PROP_TEMPLATE;
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
 	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    templateVariables = new String[] { PROP_TITLE, PROP_MESSAGE, PROP_DEFAULT };
	    //
        // velocity could always access these via $ccp.getProperty("title")
        // but now they will be visible as $title, $message
        //

 	    super.initializeCallBack(ccp);
 	}
 
 	@Override
 	protected boolean processRequest(HttpServletRequest request) throws IOException {
 	    try {
             componentContext.pushDataComponentToOutput(OUT_TEXT, request.getParameter("context"));
         }
         catch (ComponentContextException e) {
             throw new IOException(e.toString());
         }
 
 	    return true;
 	}
 }
