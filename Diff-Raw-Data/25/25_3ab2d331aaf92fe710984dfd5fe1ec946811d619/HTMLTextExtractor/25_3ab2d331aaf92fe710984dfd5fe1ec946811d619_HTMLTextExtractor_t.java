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
 
 package org.seasr.meandre.components.tools.text.transform;
 
 import java.util.logging.Logger;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.parsers.DataTypeParser;
 import org.seasr.meandre.support.text.HTMLUtils;
 
 @Component(creator = "Lily Dong",
            description = "Converts an HTML doc to plain text. All nodes from " +
            		         "the dom tree that are plain text nodes are appended " +
            		         "together and returned as a string.",
            name = "HTML Text Extractor",
            tags = "html, text, converter",
            baseURL = "meandre://seasr.org/components/")
 
 public class HTMLTextExtractor extends AbstractExecutableComponent
 {
     @ComponentInput(description = "The HTML document." +
     		                      "<br>TYPE: String, Text, byte[]",
                     name = Names.PORT_HTML)
    protected final static String IN_HTML = Names.PORT_HTML;
 
     @ComponentOutput(description = "The text extracted from the HTML document."+
                                    "<br>TYPE: Text",
                      name = Names.PORT_TEXT)
    protected final static String OUT_TEXT = Names.PORT_TEXT;
 
 
     private Logger _console;
 
 
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         _console = getConsoleLogger();
     }
 
     public void executeCallBack(ComponentContext cc) throws Exception {
         Object data = cc.getDataComponentFromInput(IN_HTML);
         _console.fine("Got input of type: " + data.getClass().toString());
 
         String html = DataTypeParser.parseAsString(data);
         String text = HTMLUtils.extractText(html);
 
         cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(text));
     }
 
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 }
