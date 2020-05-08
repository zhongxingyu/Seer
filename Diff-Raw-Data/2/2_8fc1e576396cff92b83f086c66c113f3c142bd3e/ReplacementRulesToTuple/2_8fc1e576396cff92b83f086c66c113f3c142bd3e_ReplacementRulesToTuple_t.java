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
 
 package org.seasr.meandre.components.transform.text;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
 import org.seasr.datatypes.core.BasicDataTypes.StringsMap;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         creator = "Boris Capitanu",
         description = "This component transforms a set of replacement rules into tuples " +
         		"where the first column is the correct word and the second column is the misspelled word. " +
         		"If multiple misspellings exist for the same correct word, multiple tuple rows are created.",
         name = "Replacement Rules To Tuple",
         tags = "replacement rules, tuple, transform",
         firingPolicy = FiringPolicy.all,
         rights = Licenses.UofINCSA,
         baseURL = "meandre://seasr.org/components/foundry/",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class ReplacementRulesToTuple extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
            description = "Replacement rules format: newText = {old1, old2, old3}; newText2 = {old4,old5}; newText3=old6;" +
                 " If you need to use an equals sign, use := to separate values (e.g.  newtext=blah := {old1=A,old2} )" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object",
             name = "replacement_rules"
     )
     protected static final String IN_REPLACEMENT_RULES = "replacement_rules";
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_TUPLES,
             description = "The set of tuples" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
     )
     protected static final String OUT_TUPLES = Names.PORT_TUPLES;
 
     @ComponentOutput(
             name = Names.PORT_META_TUPLE,
             description = "The meta data for tuples" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
 
     //--------------------------------------------------------------------------------------------
 
 
     private static final String WORD_LABEL = "word";
     private static final String MISSPELLING_LABEL = "misspelling";
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         Object input = cc.getDataComponentFromInput(IN_REPLACEMENT_RULES);
         SimpleTuplePeer outPeer = new SimpleTuplePeer(new String[] { WORD_LABEL, MISSPELLING_LABEL });
 
         StringsArray.Builder tuplesBuilder = StringsArray.newBuilder();
         if (input instanceof StringsMap) {
             // Processing input as StringsMap
             Map<String, String[]> rulesMap = BasicDataTypesTools.StringMapToMap((StringsMap) input);
 
             for (Entry<String, String[]> entry : rulesMap.entrySet())
                 for (String misspelling : entry.getValue())
                     tuplesBuilder.addValue(getTuple(outPeer, entry.getKey(), misspelling).convert());
         } else {
             // Processing input as text
             String rules = DataTypeParser.parseAsString(input)[0];
             Map<String, List<String>> rulesMap = SpellCheckWithCounts.buildTransformDictionary(rules);
 
             for (Entry<String, List<String>> entry : rulesMap.entrySet())
                 for (String misspelling : entry.getValue())
                     tuplesBuilder.addValue(getTuple(outPeer, entry.getKey(), misspelling).convert());
         }
 
         cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
         cc.pushDataComponentToOutput(OUT_TUPLES, tuplesBuilder.build());
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     //--------------------------------------------------------------------------------------------
 
     private SimpleTuple getTuple(SimpleTuplePeer outPeer, String word, String misspelling) {
         SimpleTuple tuple = outPeer.createTuple();
         tuple.setValue(WORD_LABEL, word);
         tuple.setValue(MISSPELLING_LABEL, misspelling);
 
         return tuple;
     }
 
 }
