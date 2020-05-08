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
 
 package org.seasr.datatypes.tranformations.text;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.meandre.core.system.components.ext.StreamInitiator;
 import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.components.text.datatype.corpora.Document;
 import org.seasr.components.text.util.Factory;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.parsers.DataTypeParser;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         name = "Text To Document",
         description = "Creates a Document structure from the input text. " +
                       "This Document structure is used by other components for running various analyses.",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/tools/",
         firingPolicy = FiringPolicy.all,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         dependency = {"protobuf-java-2.0.3.jar"},
         tags = "text, document, transformation"
 )
 public class TextToDocument extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TEXT,
             description = "The text to be converted to a Document structure"
     )
     protected static final String IN_TEXT = Names.PORT_TEXT;
 
     @ComponentInput(
             name = Names.PORT_DOC_TITLE,
             description = "The title to associate with this Document"
     )
     protected static final String IN_DOC_TITLE = Names.PORT_DOC_TITLE;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_DOCUMENT,
             description = "The Document structure constructed from the input text"
     )
     protected static final String OUT_DOCUMENT = Names.PORT_DOCUMENT;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         String[] titles = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_DOC_TITLE));
         String[] texts = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT));
 
         if (titles.length != texts.length)
             throw new ComponentExecutionException("Cannot process inputs that are not aligned");
 
         for (int i = 0; i < titles.length; i++) {
             String title = titles[i];
             String text = texts[i];
 
             Document document = Factory.newDocument();
             document.setTitle(title);
             document.setDocID(java.util.UUID.randomUUID().toString());
             document.setContent(text);
 
             console.fine(String.format("Creating document: title='%s'  id='%s'",
                     document.getTitle(), document.getDocID()));
 
             cc.pushDataComponentToOutput(OUT_DOCUMENT, document);
         }
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     protected void handleStreamInitiators() throws Exception {
         if (inputPortsWithInitiators.contains(IN_TEXT) &&
             inputPortsWithInitiators.contains(IN_DOC_TITLE)) {
 
             StreamInitiator si = (StreamInitiator)componentContext.getDataComponentFromInput(IN_TEXT);
             componentContext.pushDataComponentToOutput(OUT_DOCUMENT, si);
        } else
            throw new Exception("Unbalanced or unexpected StreamInitiator received");
     }
 
     @Override
     protected void handleStreamTerminators() throws Exception {
         if (inputPortsWithTerminators.contains(IN_TEXT) &&
             inputPortsWithTerminators.contains(IN_DOC_TITLE)) {
 
             StreamTerminator st = (StreamTerminator)componentContext.getDataComponentFromInput(IN_TEXT);
             componentContext.pushDataComponentToOutput(OUT_DOCUMENT, st);
        } else
            throw new Exception("Unbalanced or unexpected StreamTerminator received");
     }
 }
