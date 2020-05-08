 /**
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, Board of Trustees-University of Illinois.
  * All rights reserved.
  *
  * Developed by:
  *
  * Automated Learning Group
  * National Center for Supercomputing Applications
  * http://www.seasr.org
  *
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal with the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimers.
  *
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimers in the
  *    documentation and/or other materials provided with the distribution.
  *
  *  * Neither the names of Automated Learning Group, The National Center for
  *    Supercomputing Applications, or University of Illinois, nor the names of
  *    its contributors may be used to endorse or promote products derived from
  *    this Software without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * WITH THE SOFTWARE.
  */
 
 package org.seasr.meandre.components.tools.webservice;
 
 import java.io.InputStreamReader;
 import java.util.logging.Level;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.generic.io.IOUtils;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         creator = "Boris Capitanu",
         description = "Extracts the payload (body/content) of a request.",
         name = "Extract Request Content",
         tags = "webservice, request, content, extract",
         rights = Licenses.UofINCSA,
         baseURL = "meandre://seasr.org/components/foundry/",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class ExtractRequestContent extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = "request_handler",
     		description = "The request object." +
     		    "<br>TYPE: javax.servlet.http.HttpServletRequest"
     )
     protected static final String IN_REQUEST = "request_handler";
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_TEXT,
             description = "The header value" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_TEXT = Names.PORT_TEXT;
 
     @ComponentOutput(
             name = "request_handler",
     		description = "Same as input."
     )
     protected static final String OUT_REQUEST = "request_handler";
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
     	HttpServletRequest request = (HttpServletRequest) cc.getDataComponentFromInput(IN_REQUEST);
 
    	if (console.isLoggable(Level.FINE)) {
         	console.fine("Content-Type: " + request.getContentType());
         	console.fine("Content-Length: " + request.getContentLength());
        	console.fine("Content-Encoding: " + request.getCharacterEncoding());
     	}
 
    	String content = IOUtils.getTextFromReader(new InputStreamReader(request.getInputStream(), request.getCharacterEncoding()));
 
     	cc.pushDataComponentToOutput(OUT_REQUEST, request);
     	cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(content));
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 }
