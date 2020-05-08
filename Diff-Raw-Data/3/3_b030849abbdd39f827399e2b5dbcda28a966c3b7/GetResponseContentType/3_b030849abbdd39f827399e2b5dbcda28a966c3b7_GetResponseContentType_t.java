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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 
 import com.google.gdata.util.ContentType;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         creator = "Boris Capitanu",
         description = "Returns the best content type, as specified in the 'Accept' header, based on the specified supported content types. " +
         		"If the request does not supply an Accept header value, then the first content type specified in the 'supported_types' " +
         		"property will be returned.",
         name = "Get Response Content Type",
         tags = "webservice, header, content type, accept",
         rights = Licenses.UofINCSA,
         baseURL = "meandre://seasr.org/components/foundry/",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class GetResponseContentType extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = "request_handler",
     		description = "The request object." +
     		    "<br>TYPE: javax.servlet.http.HttpServletRequest"
     )
     protected static final String IN_REQUEST = "request_handler";
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = "content_type",
             description = "The best content type" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_CONTENT_TYPE = "content_type";
 
     @ComponentOutput(
             name = "request_handler",
     		description = "Same as input."
     )
     protected static final String OUT_REQUEST = "request_handler";
 
     //------------------------------ PROPERTIES ---------------------------------------------------
 
     @ComponentProperty (
             description = "The comma-separated list of supported content types in descending order of preference " +
             		"(non-empty, and each entry is of the form type/subtype without the wildcard char '*')",
             name = "supported_types",
             defaultValue = ""
     )
     protected static final String PROP_SUPPORTED_TYPES = "supported_types";
 
     @ComponentProperty (
             description = "Should the charset from request be included?",
             name = "include_request_charset",
             defaultValue = "true"
     )
     protected static final String PROP_INCLUDE_REQUEST_CHARSET = "include_request_charset";
 
     //--------------------------------------------------------------------------------------------
 
 
     protected List<ContentType> _supportedTypes;
     protected boolean _appendCharset;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         String supportedTypes[] = getPropertyOrDieTrying(PROP_SUPPORTED_TYPES, ccp).split(",");
         _supportedTypes = new ArrayList<ContentType>(supportedTypes.length);
         for (String type : supportedTypes)
             _supportedTypes.add(new ContentType(type.trim()));
 
         _appendCharset = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_INCLUDE_REQUEST_CHARSET, ccp));
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
     	HttpServletRequest request = (HttpServletRequest) cc.getDataComponentFromInput(IN_REQUEST);
 
     	String accept = request.getHeader("Accept");
     	ContentType bestType = accept.length() > 0 ? ContentType.getBestContentType(accept, _supportedTypes) : _supportedTypes.get(0);
     	String encoding = request.getCharacterEncoding();
    	String contentType = (_appendCharset && encoding != null) ?
    	        String.format("%s; charset=%s", bestType.toString(), encoding) : bestType.toString();
     	console.fine("Best content type: " + contentType);
 
     	cc.pushDataComponentToOutput(OUT_CONTENT_TYPE, BasicDataTypesTools.stringToStrings(contentType));
     	cc.pushDataComponentToOutput(OUT_REQUEST, request);
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         _supportedTypes.clear();
         _supportedTypes = null;
     }
 }
