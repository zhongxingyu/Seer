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
 
 import java.util.regex.Pattern;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
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
 		name = "Tuple Value Filter",
 		creator = "Mike Haberman",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "tuple, tools, text, filter",
 		description = "This component filters the incoming set of tuples based on a regular expression" ,
		dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class TupleValueFilter extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_TUPLES,
 			description = "The (set of) tuple(s)" +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
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
 			description = "The (set of) filtered tuple(s)" +
 			    "<br>TYPE: same as input"
 	)
 	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
 
 	@ComponentOutput(
 			name = Names.PORT_META_TUPLE,
 			description = "The meta data for the tuples (same as input)" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
 
 	//----------------------------- PROPERTIES ---------------------------------------------------
 
 	@ComponentProperty(
 			name = Names.PROP_FILTER_REGEX,
 			description = "The regular expression to apply to the tuples",
 		    defaultValue = ""
 	)
 	protected static final String PROP_FILTER_REGEX = Names.PROP_FILTER_REGEX;
 
     @ComponentProperty(
             name = "filter_out",
             description = "This setting controls how the regular expression is applied. " +
             		"When true, the regular expression is used for specifying tuples that " +
             		"should be discarded from the output. When false, the regular " +
             		"expression specifies the tuples that should be included in the output (everything else will be discarded)",
             defaultValue = "true"
     )
     protected static final String PROP_FILTER_OUT = "filter_out";
 
 	@ComponentProperty(
 			name = "filter_attribute",
 			description = "The attribute of the tuple to apply the filter to",
 		    defaultValue = ""
 	)
 	protected static final String PROP_ATTRIBUTE = "filter_attribute";
 
 	//--------------------------------------------------------------------------------------------
 
 
 	protected Pattern _regexp = null;
 	protected String _attribute;
 	protected boolean _filterOut = false;
 
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 		_attribute = getPropertyOrDieTrying(PROP_ATTRIBUTE, ccp);
 		_regexp = Pattern.compile(getPropertyOrDieTrying(PROP_FILTER_REGEX, false, false, ccp));
 		_filterOut = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_FILTER_OUT, ccp));
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 	    Strings inMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
 	    SimpleTuplePeer inPeer = new SimpleTuplePeer(inMeta);
 
 	    Object input = cc.getDataComponentFromInput(IN_TUPLES);
 	    Strings[] tuples;
 
 	    if (input instanceof StringsArray)
 	        tuples = BasicDataTypesTools.stringsArrayToJavaArray((StringsArray) input);
 
 	    else
 
         if (input instanceof Strings) {
             Strings inTuple = (Strings) input;
             tuples = new Strings[] { inTuple };
         }
 
         else
             throw new ComponentExecutionException("Don't know how to handle input of type: " + input.getClass().getName());
 
 		int FIELD_IDX = inPeer.getIndexForFieldName(_attribute);
 		if (FIELD_IDX == -1)
             throw new ComponentExecutionException(String.format("The tuple has no attribute named '%s'", _attribute));
 
 		StringsArray.Builder tuplesBuilder = StringsArray.newBuilder();
 
 		for (int i = 0, iMax = tuples.length; i < iMax; i++) {
 	        SimpleTuple tuple = inPeer.createTuple();
 			tuple.setValues(tuples[i]);
 
             String fieldValue = tuple.getValue(FIELD_IDX);
 
 			boolean match = _regexp.matcher(fieldValue).matches();
             if ((_filterOut && !match) || (!_filterOut && match))
 				tuplesBuilder.addValue(tuple.convert());
 		}
 
 		// Return if nothing to output
 		if (tuplesBuilder.getValueCount() == 0) {
 		    console.fine("Nothing to output - no tuples pass the filter rule");
 		    return;
 		}
 
 		if (input instanceof Strings)
 		    cc.pushDataComponentToOutput(OUT_TUPLES, tuplesBuilder.getValue(0));
 
 		if (input instanceof StringsArray)
 		    cc.pushDataComponentToOutput(OUT_TUPLES, tuplesBuilder.build());
 
 		cc.pushDataComponentToOutput(OUT_META_TUPLE, inMeta);
 	}
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 }
