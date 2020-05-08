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
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 
 /**
  *
  * @author Mike Haberman
  * @author Boris Capitanu
  *
  */
 
 @Component(
 		name = "CSV To Tuple",
 		creator = "Mike Haberman",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
		tags = "tools, text,",
		description = "This component converts a csv string into tuples.  Each line of the incoming text is a new tuple. It does not handle missing values" ,
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
 )
 public class CSVToTuple extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TEXT,
             description = "The the text to be parsed into tuples.  Each line is a new tuple." +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
     )
     protected static final String IN_TEXT = Names.PORT_TEXT;
 
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
 
 	//----------------------------- PROPERTIES ---------------------------------------------------
 
     @ComponentProperty(
             description = "The column names/labels to be used (comma separated). " +
             		"The values set here override the labels read from the data header. " +
             		"If this property is not set (empty) then the column names will be read from the data header (the first line of the data).",
             name = "labels",
             defaultValue = ""
     )
     protected static final String PROP_LABELS = "labels";
 
     @ComponentProperty(
             description = "The delimiter used to separate the data into columns",
             name = Names.PROP_SEPARATOR,
             defaultValue = ","
     )
     protected static final String PROP_DELIMITER = Names.PROP_SEPARATOR;
 
     @ComponentProperty(
             description = "Does the data contain data labels? This row of labels will be skipped if the value of the property 'labels' is not empty",
             name = Names.PROP_HEADER,
             defaultValue = "true"
     )
     protected static final String PROP_HEADER = Names.PROP_HEADER;
 
    	//--------------------------------------------------------------------------------------------
 
 
     protected String _separator;
     protected String[] _fieldNames = null;
     protected boolean _hasHeader = false;
 
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 	    _separator = getPropertyOrDieTrying(PROP_DELIMITER, false, true, ccp).replaceAll("\\\\t", "\t");
 	    _hasHeader = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_HEADER, ccp));
 
 	    String labels = getPropertyOrDieTrying(PROP_LABELS, true, false, ccp);
 	    if (labels.length() > 0)
 	        _fieldNames = labels.split(",");
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 	    String data = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))[0];
 	    BufferedReader reader = new BufferedReader(new StringReader(data));
 	    String line;
 
         SimpleTuplePeer outPeer = null;
         if (_fieldNames != null) {
             outPeer = new SimpleTuplePeer(_fieldNames);
             // skip the header if exists
             if (_hasHeader) readNextLineSkipComments(reader);
         } else {
             // Read the column names from the first line of the data
             line = readNextLineSkipComments(reader);
             String[] fieldNames = line.split(_separator);
             outPeer = new SimpleTuplePeer(fieldNames);
         }
 
 	    StringsArray.Builder tuplesBuilder = StringsArray.newBuilder();
 	    while ((line = reader.readLine()) != null) {
 	        // skip commented or empty lines
             if (line.startsWith("#") || line.trim().length() == 0)
                 continue;
 
 	        String[] fieldValues = line.split(_separator, outPeer.size());
 	        SimpleTuple tuple = outPeer.createTuple();
 	        tuple.setValues(fieldValues);
 	        tuplesBuilder.addValue(tuple.convert());
 	    }
 
 	    cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
 	    cc.pushDataComponentToOutput(OUT_TUPLES, tuplesBuilder.build());
 	}
 
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     //--------------------------------------------------------------------------------------------
 
     protected String readNextLineSkipComments(BufferedReader reader) throws IOException {
         String line;
         while ((line = reader.readLine()) != null) {
             // skip commented or empty lines
             if (line.startsWith("#") || line.trim().length() == 0)
                 continue;
 
             break;
         }
 
         return line;
     }
 }
