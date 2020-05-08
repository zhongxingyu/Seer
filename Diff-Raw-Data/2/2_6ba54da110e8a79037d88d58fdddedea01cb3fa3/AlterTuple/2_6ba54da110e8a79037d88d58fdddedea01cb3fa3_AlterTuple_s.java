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
 
 package org.seasr.meandre.components.tools.tuples;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
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
 import org.seasr.datatypes.core.BasicDataTypes.Strings;
 import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         name = "Alter Tuple",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.all,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "tuple",
         description = "This component provides the ability to rename and/or remove fields from tuples. " +
         		"The original input tuples are not altered. The output is an altered copy of the original tuples." ,
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class AlterTuple extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TUPLES,
             description = "The tuples" +
                     "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
     )
     protected static final String IN_TUPLES = Names.PORT_TUPLES;
 
     @ComponentInput(
             name = Names.PORT_META_TUPLE,
             description = "The meta data for the tuples" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_TUPLES,
             description = "The altered tuples" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
     )
     protected static final String OUT_TUPLES = Names.PORT_TUPLES;
 
     @ComponentOutput(
             name = Names.PORT_META_TUPLE,
             description = "The meta data for the altered tuples" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
 
     //----------------------------- PROPERTIES ---------------------------------------------------
 
     @ComponentProperty(
             name = "rename",
             description = "The comma-separated list of renaming rules for field names in the format original_field_name = new_field_name. " +
             		"For example: text=location  - this rule specifies that the original field " +
             		"name 'text' will be replaced by 'location'.",
             defaultValue = ""
     )
     protected static final String PROP_RENAME = "rename";
 
     @ComponentProperty(
             name = "remove",
             description = "The comma-separated list of field names to be removed.",
             defaultValue = ""
     )
     protected static final String PROP_REMOVE = "remove";
 
     @ComponentProperty(
             name = "add",
             description = "The comma-separated list of rules for field names to add in the format field_name = value. " +
             		"For example: type=location  - this rule adds a new field 'type' with value 'location' to all the tuples. " +
             		"Note: whitespaces are trimmed from both field names and values; " +
             		"also, equals sign '=' cannot be used in either the field name nor the value",
             defaultValue = ""
     )
     protected static final String PROP_ADD = "add";
 
     //--------------------------------------------------------------------------------------------
 
 
     protected Map<String, String> _renameRules = new HashMap<String, String>();
     protected Set<String> _fieldsToRemove = new HashSet<String>();
     protected Map<String, String> _fieldsToAdd = new HashMap<String, String>();
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         String rename = getPropertyOrDieTrying(PROP_RENAME, true, false, ccp);
         if (rename.length() > 0)
             for (String rule : rename.split(",")) {
                 String[] kv = rule.split("=");
                 if (kv.length != 2) throw new ComponentContextException("Invalid rename rule format: " + rule);
                 _renameRules.put(kv[0].trim(), kv[1].trim());
             }
 
         String remove = getPropertyOrDieTrying(PROP_REMOVE, true, false, ccp);
         if (remove.length() > 0)
             for (String fieldName : remove.split(","))
                 _fieldsToRemove.add(fieldName.trim());
 
         String add = getPropertyOrDieTrying(PROP_ADD, true, false, ccp);
         if (add.length() > 0)
             for (String rule : add.split(",")) {
                 String[] kv = rule.split("=");
                 if (kv.length != 2) throw new ComponentContextException("Invalid add rule format: " + rule);
                 _fieldsToAdd.put(kv[0].trim(), kv[1].trim());
             }
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         Strings inMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
         StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
 
         SimpleTuplePeer inPeer = new SimpleTuplePeer(inMeta);
         Strings[] inTuples = BasicDataTypesTools.stringsArrayToJavaArray(input);
 
        List<String> inFieldNames = Arrays.asList(inPeer.getFieldNames());
         List<String> outFieldNames = new ArrayList<String>(inFieldNames.size());
         for (int i = 0, iMax = inFieldNames.size(); i < iMax; i++) {
             String fieldName = inFieldNames.get(i);
             if (_renameRules.containsKey(fieldName))
                 fieldName = _renameRules.get(fieldName);
             outFieldNames.add(fieldName);
         }
 
         inFieldNames.removeAll(_fieldsToRemove);
         outFieldNames.removeAll(_fieldsToRemove);
 
         outFieldNames.addAll(_fieldsToAdd.keySet());
 
         String[] fieldNames = new String[outFieldNames.size()];
         SimpleTuplePeer outPeer = new SimpleTuplePeer(outFieldNames.toArray(fieldNames));
         StringsArray.Builder outTuples = StringsArray.newBuilder();
 
         for (Strings t : inTuples) {
             SimpleTuple inTuple = inPeer.createTuple();
             inTuple.setValues(t);
 
             SimpleTuple outTuple = outPeer.createTuple();
             for (String fieldName : inFieldNames) {
                 String outFieldName = fieldName;
                 if (_renameRules.containsKey(fieldName))
                     outFieldName = _renameRules.get(fieldName);
                 outTuple.setValue(outFieldName, inTuple.getValue(fieldName));
             }
 
             for (Entry<String, String> entry : _fieldsToAdd.entrySet())
                 outTuple.setValue(entry.getKey(), entry.getValue());
 
             outTuples.addValue(outTuple.convert());
         }
 
         cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
         cc.pushDataComponentToOutput(OUT_TUPLES, outTuples.build());
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         _renameRules.clear();
         _renameRules = null;
         _fieldsToRemove.clear();
         _fieldsToRemove = null;
     }
 }
