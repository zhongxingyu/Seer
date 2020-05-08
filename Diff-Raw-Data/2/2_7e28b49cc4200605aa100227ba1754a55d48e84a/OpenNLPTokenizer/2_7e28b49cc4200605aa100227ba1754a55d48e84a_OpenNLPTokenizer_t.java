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
 
 package org.seasr.meandre.components.nlp.opennlp;
 
 import opennlp.tools.tokenize.SimpleTokenizer;
 import opennlp.tools.tokenize.WhitespaceTokenizer;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         name = "OpenNLP Tokenizer",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.all,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "nlp, text, opennlp, tokenizer",
         description = "This component performs tokenization on text, and outputs the tokens." ,
         dependency = {
                 "protobuf-java-2.2.0.jar",
                 "opennlp-tools-1.5.2-incubating.jar",
                 "opennlp-maxent-3.0.2-incubating.jar"
         }
 )
 public class OpenNLPTokenizer extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TEXT,
             description = "The text to be tokenized" +
                  "<br>TYPE: java.lang.String" +
                  "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                  "<br>TYPE: byte[]" +
                  "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                  "<br>TYPE: java.lang.Object"
     )
     protected static final String IN_TEXT = Names.PORT_TEXT;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_TOKENS,
            description = "The tokens" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_TOKENS = Names.PORT_TOKENS;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             name = "tokenizer_type",
             description = "The tokenizer type. One of 'whitespace' or 'simple' (no quotes). " +
             		"The whitespace tokenizer splits text into tokens based on whitespaces. " +
             		"The simple tokenizer splits text based on character classes.",
             defaultValue = "simple"
     )
     protected static final String PROP_TOKENIZER_TYPE = "tokenizer_type";
 
     //--------------------------------------------------------------------------------------------
 
 
     protected String _tokenizerType;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         _tokenizerType = getPropertyOrDieTrying(PROP_TOKENIZER_TYPE, ccp);
         if (!_tokenizerType.equalsIgnoreCase("simple") && !_tokenizerType.equalsIgnoreCase("whitespace"))
             throw new ComponentContextException("Invalid tokenizer type specified!");
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         String text = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))[0];
         String[] tokens;
 
         if (_tokenizerType.equalsIgnoreCase("simple"))
             tokens = SimpleTokenizer.INSTANCE.tokenize(text);
 
         else
 
         if (_tokenizerType.equalsIgnoreCase("whitespace"))
             tokens = WhitespaceTokenizer.INSTANCE.tokenize(text);
 
         else
             throw new ComponentExecutionException("Invalid tokenizer");
 
         cc.pushDataComponentToOutput(OUT_TOKENS, BasicDataTypesTools.stringToStrings(tokens));
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 }
