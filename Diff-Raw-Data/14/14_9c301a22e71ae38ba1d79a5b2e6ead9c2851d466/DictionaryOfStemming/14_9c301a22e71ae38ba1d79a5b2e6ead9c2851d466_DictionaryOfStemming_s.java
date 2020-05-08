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
 
 package org.seasr.meandre.components.tools.text.normalize.porter;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.system.components.ext.StreamInitiator;
 import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.datatypes.BasicDataTypes;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
 import org.seasr.meandre.support.components.exceptions.UnsupportedDataTypeException;
 
 @Component(creator = "Lily Dong",
 		description = "Maps the stemmed token back to " +
 		"the actual word in the original document.",
 		firingPolicy = FiringPolicy.all,
		name = "dictionary of stemming",
 		tags = "stem dictionary",
 		baseURL="meandre://seasr.org/components/tools/")
 
 public class DictionaryOfStemming extends AbstractExecutableComponent {
 	// IO
 
 	@ComponentInput(
 			name = Names.PORT_TOKENS,
 			description = "The stemmed tokens"
 	)
 	protected static final String IN_TOKENS = Names.PORT_TOKENS;
 
 	@ComponentInput(
 			name = Names.PORT_WORDS,
 			description = "The original words"
 	)
 	protected static final String IN_WORDS= Names.PORT_WORDS;
 
 	@ComponentOutput(
 			name = Names.PORT_WORDS,
 			description = "The mapped words"
 	)
 	protected static final String OUT_WORDS = Names.PORT_WORDS;
 
 	/**
 	 * Store mapping between token and word
 	 */
 	Map<String, String> map;
 
 	private boolean _gotInitiator;
 
 	// ================
 	// Public Methods
 	// ================
 
	public void initializeCallBack(ComponentContextProperties ccp)
     throws Exception {
 		map = new HashMap<String, String>();
 		_gotInitiator = false;
 	}
 
	public void disposeCallBack(ComponentContextProperties ccp)
     throws Exception {
 	}
 
	public void executeCallBack(ComponentContext cc)
 	throws Exception {
 		String[] words = null;
 		String[] tokens = null;
 
 		try {
 			words = DataTypeParser.parseAsString(
             		cc.getDataComponentFromInput(IN_WORDS));
             tokens = DataTypeParser.parseAsString(
             		cc.getDataComponentFromInput(IN_TOKENS));
         }
         catch (UnsupportedDataTypeException e) {
             if (ignoreErrors)
                 console.warning("processTokens: UnsupportedDataTypeException ignored - input data was not in the correct format");
             else
                 throw e;
         }
 
         for (int i=0; i<words.length; i++ ) {
         	String key = tokens[i];
         	String theValue = words[i];
         	if(map.containsKey(key)) { //take the shorter one
         		String value = map.get(key);
         		value = (value.length()>theValue.length())?
         				theValue: value;
         		map.put(key, value);
         	} else
         		map.put(key, theValue);
         }
 
         if (!_gotInitiator) {
         	org.seasr.datatypes.BasicDataTypes.StringsMap.Builder mres = BasicDataTypes.StringsMap.newBuilder();
             Set<String> set = map.keySet();
             for (String s : set ) {
             	org.seasr.datatypes.BasicDataTypes.Strings.Builder sres = BasicDataTypes.Strings.newBuilder();
             	sres.addValue(map.get(s));
             	mres.addKey(s);
     			mres.addValue(sres.build());
             }
 
             componentContext.pushDataComponentToOutput(OUT_WORDS, mres.build());
 
             map.clear();
         }
 	}
 
 	@Override
 	protected void handleStreamInitiators() throws Exception {
 		if (_gotInitiator)
 	            throw new UnsupportedOperationException("Cannot process multiple streams at the same time!");
 
 		_gotInitiator = true;
 	}
 
 	@Override
 	protected void handleStreamTerminators() throws Exception {
 		if (!_gotInitiator)
 	            throw new Exception("Received StreamTerminator without receiving StreamInitiator");
 
 		org.seasr.datatypes.BasicDataTypes.StringsMap.Builder mres = BasicDataTypes.StringsMap.newBuilder();
         Set<String> set = map.keySet();
         for (String s : set ) {
         	org.seasr.datatypes.BasicDataTypes.Strings.Builder sres = BasicDataTypes.Strings.newBuilder();
         	sres.addValue(map.get(s));
         	mres.addKey(s);
 			mres.addValue(sres.build());
         }
 
         componentContext.pushDataComponentToOutput(OUT_WORDS, new StreamInitiator());
         componentContext.pushDataComponentToOutput(OUT_WORDS, mres.build());
         componentContext.pushDataComponentToOutput(OUT_WORDS, new StreamTerminator());
 
         map.clear();
         _gotInitiator = false;
 	}
 }
