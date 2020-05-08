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
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.BasicDataTypes;
 import org.seasr.datatypes.core.BasicDataTypes.Strings;
 import org.seasr.datatypes.core.BasicDataTypes.StringsMap;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.datatypes.core.exceptions.UnsupportedDataTypeException;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.components.text.normalize.porter.PorterStemmer;
 
 
 /**
  * @author D. Searsmith
  * @author Lily Dong
  *
  * TODO: Testing, Unit Tests
  *
  */
 
 @Component(
         creator = "Lily Dong",
         description = "<p>Overview: <br>"
     		+ "This component transforms terms into their word stems. In this way, "
     		+ "different forms of the same word (plurals etc...) will be recognized as the same term."
     		+ "The algorithm used is the Porter stemming method."
     		+ "</p>"
     		+ "<p>References: <br>"
     		+ "See: http://www.tartarus.org/~martin/PorterStemmer/"
     		+ "</p>"
     		+ "<p>Data Type Restrictions: <br>"
     		+ "The input document must have been tokenized."
     		+ "</p>"
     		+ "<p>Data Handling: <br>"
     		+ "This component will modify (as described above) the document object that is input."
     		+ "</p>"
     		+ "<p>Scalability: <br>"
     		+ "This compnent makes one pass over the token list resulting in linear time complexity "
     		+ "per the number of tokens. Memory usage is proportional to the number tokens."
     		+ "</p>"
     		+ "<p>Trigger Criteria: <br>" + "All." + "</p>",
         name = "Stem",
         tags = "nlp text document normalize stem",
         rights = Licenses.UofINCSA,
         firingPolicy = FiringPolicy.all,
         baseURL = "meandre://seasr.org/components/foundry/"
 )
 public class Stem extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_OBJECT,
 			description = "The tokens or tokenized_sentences to be stemmed" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
 	)
 	protected static final String IN_OBJECT = Names.PORT_OBJECT;
 
 	//------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			name = Names.PORT_TOKENS,
 			description = "The stemmed tokens" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_TOKENS = Names.PORT_TOKENS;
 
 	@ComponentOutput(
 			name = Names.PORT_TOKENIZED_SENTENCES,
 			description = "The stemmed tokenized sentences" +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
 	)
 	protected static final String OUT_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;
 
     //--------------------------------------------------------------------------------------------
 
 
 	private PorterStemmer stemmer = null;
 
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 		stemmer = null;
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 		if (stemmer == null)
 			stemmer = new PorterStemmer();
 
 		Object object = cc.getDataComponentFromInput(IN_OBJECT);
 
 		if (object instanceof Strings)
 			processTokens(object);
 
 		else
 
 		if(object instanceof StringsMap)
 			processSentences(object);
 
 		else
 			throw new Exception("Unsupported data type: " + object.getClass().toString());
 	}
 
 	@Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
 	    stemmer = null;
 	}
 
 	//--------------------------------------------------------------------------------------------
 
 	/**
 	 *
 	 * @param object input tokens
 	 * @throws Exception
 	 */
 	private void processTokens(Object object) throws Exception {
 		String[] tokens = null;
 
 		try {
             tokens = DataTypeParser.parseAsString(object);
         }
         catch (UnsupportedDataTypeException e) {
             if (ignoreErrors)
                 console.warning("processTokens: UnsupportedDataTypeException ignored - input data was not in the correct format");
             else
                 throw e;
         }
 
         org.seasr.datatypes.core.BasicDataTypes.Strings.Builder res = BasicDataTypes.Strings.newBuilder();
 
		for (String sToken : tokens ) {
			String stem = stemmer.normalizeTerm(sToken);
			res.addValue(stem);
		}
 
 		componentContext.pushDataComponentToOutput(OUT_TOKENS, res.build());
 	}
 
 	/**
 	 *
 	 * @param object input sentences
 	 * @throws Exception
 	 */
 	private void processSentences (Object object) throws Exception {
 		StringsMap im = (StringsMap)object;
 
 		org.seasr.datatypes.core.BasicDataTypes.StringsMap.Builder res = BasicDataTypes.StringsMap.newBuilder();
 
 		for ( int i=0, iMax=im.getKeyCount() ; i<iMax ; i++ ) {
 			String sKey = im.getKey(i);
 			Strings sVals = im.getValue(i);
 			org.seasr.datatypes.core.BasicDataTypes.Strings.Builder resStemmed = BasicDataTypes.Strings.newBuilder();
 			for ( String s:sVals.getValueList()) {
 				String stem = stemmer.normalizeTerm(s);
 				resStemmed.addValue(stem);
 			}
 			res.addKey(sKey);
 			res.addValue(resStemmed.build());
 		}
 
 		componentContext.pushDataComponentToOutput(OUT_TOKENIZED_SENTENCES, res.build());
 	}
 }
