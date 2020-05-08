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
 
 package org.seasr.meandre.components.tools.tuples;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.BasicDataTypes.Strings;
 import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
 import org.seasr.datatypes.core.BasicDataTypesTools;
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
 		name = "Tuple To CSV",
 		creator = "Mike Haberman",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "tuple, tools, text, filter",
 		description = "This component writes the incoming set of tuples to CSV String" ,
		dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class TupleToCSV extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_TUPLES,
 			description = "The set of tuples" +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
 	)
 	protected static final String IN_TUPLES = Names.PORT_TUPLES;
 
 	@ComponentInput(
 			name = Names.PORT_META_TUPLE,
 			description = "The meta data for tuples" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			name = Names.PORT_TUPLES,
 			description = "The set of tuples (same as input)" +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
 	)
 	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
 
 	@ComponentOutput(
 			name = Names.PORT_META_TUPLE,
 			description = "The meta data for the tuples (same as input)" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
 
 	@ComponentOutput(
 			name = Names.PORT_TEXT,
 			description = "The CSV string" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_TEXT = Names.PORT_TEXT;
 
 	//----------------------------- PROPERTIES ---------------------------------------------------
 
 	@ComponentProperty(
 			name = Names.PROP_SEPARATOR,
 			description = "The delimiter to use to separate the data columns",
 		    defaultValue = ","
 	)
 	protected static final String PROP_SEPARATOR = Names.PROP_SEPARATOR;
 
 	@ComponentProperty(
 			name = Names.PROP_HEADER,
 			description = "Should the header be added? ",
 		    defaultValue = "true"
 	)
 	protected static final String PROP_HEADER = Names.PROP_HEADER;
 
     //--------------------------------------------------------------------------------------------
 
 
 	protected String _separator;
 	protected boolean _addHeader;
 
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 	    _separator = getPropertyOrDieTrying(PROP_SEPARATOR, false, true, ccp).replaceAll("\\\\t", "\t");
 	    _addHeader = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_HEADER, ccp));
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
 		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
 		SimpleTuple tuple = tuplePeer.createTuple();
 
 		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
 		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);
 
 		StringBuilder sb = new StringBuilder();
 
 		int size = tuplePeer.size();;
 
 		//
 		// write out the field names as the first row
 		//
 		if ( _addHeader ) {
 			for (int i = 0; i < size; i++) {
 				sb.append(tuplePeer.getFieldNameForIndex(i));
 				if (i + 1 < size) {
 					sb.append(_separator);
 				}
 			}
 			sb.append("\n");
 		}
 
 		for (int i = 0; i < in.length; i++) {
 
 			tuple.setValues(in[i]);
 
 			for (int j = 0; j < size; j++) {
 				String value = tuple.getValue(j);
 
 				// make sure the value doesn't contain the separator or else we're in trouble
 				while (value.contains(_separator))
 				    value = value.replace(_separator, "");
 
 				//TODO what to do if value="" at this point?
 
                 sb.append(value);
 				if (j + 1 < size) {
 					sb.append(_separator);
 				}
 			}
 			sb.append("\n");
 		}
 
 		Strings safe = BasicDataTypesTools.stringToStrings(sb.toString());
 
 		cc.pushDataComponentToOutput(OUT_TEXT,   safe);
 		cc.pushDataComponentToOutput(OUT_TUPLES,     input);
 		cc.pushDataComponentToOutput(OUT_META_TUPLE, inputMeta);
 	}
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 }
 
