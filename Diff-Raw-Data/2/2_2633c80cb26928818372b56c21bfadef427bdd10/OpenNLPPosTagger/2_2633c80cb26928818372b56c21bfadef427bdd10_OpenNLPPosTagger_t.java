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
 
 package org.seasr.meandre.components.nlp.opennlp;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import opennlp.tools.lang.english.PosTagger;
 import opennlp.tools.postag.POSDictionary;
 import opennlp.tools.postag.POSTaggerME;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.datatypes.BasicDataTypes.Strings;
 import org.seasr.datatypes.BasicDataTypes.StringsArray;
 import org.seasr.datatypes.BasicDataTypes.StringsMap;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 
 
 /**
  * This component perform POS tagging on the text passed using OpenNLP.
  *
  * @author Mike Haberman;
  *
  */
 
 //
 // General Path:  Text -> SentenceDetector -> SentenceTokenizer -> PosTagger
 //
 
 @Component(
		name = "OpenNLP POS Tagger",
 		creator = "Mike Haberman",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "semantic, tools, text, opennlp, tokenizer, sentences, pos, tagging",
 		description = "This component tags the incoming set of tokenized sentences " +
 				      "unsing OpenNLP pos facilities.",
 		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar", "maxent-models.jar", "seasr-commons.jar"}
 )
 public class OpenNLPPosTagger extends OpenNLPBaseUtilities {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_TOKENIZED_SENTENCES,
 			description = "The sequence of tokenized sentences" +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
 	)
 	protected static final String IN_TOKENS = Names.PORT_TOKENIZED_SENTENCES;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			name = Names.PORT_TUPLES,
 			description = "set of tuples: (pos,sentenceId,offset,token)" +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
 	)
 	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
 
 	@ComponentOutput(
 			name = Names.PORT_META_TUPLE,
 			description = "meta data for tuples: (pos,sentenceId,offset,token)" +
 			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
 
 	//----------------------------- PROPERTIES ---------------------------------------------------
 
 	@ComponentProperty(
 			name = Names.PROP_FILTER_REGEX,
 			description = "optional regular expression to inline filter POS (e.g. JJ|RB)",
 		    defaultValue = ""
 		)
 	protected static final String PROP_POS_FILTER_REGEX = Names.PROP_FILTER_REGEX;
 
 	//--------------------------------------------------------------------------------------------
 
 
 	/** The OpenNLP tokenizer to use */
 	private POSTaggerME tagger = null;
 	Pattern pattern = null;
 
 	SimpleTuplePeer tuplePeer;
 
 	public static final String POS_FIELD         = "pos";
 	public static final String SENTENCE_ID_FIELD = "sentenceId";
 	public static final String TOKEN_START_FIELD = "tokenStart";
 	public static final String TOKEN_FIELD       = "token";
 
 
 	//--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 
     	super.initializeCallBack(ccp);
 
     	String regex = ccp.getProperty(PROP_POS_FILTER_REGEX).trim();
     	if (regex.length() > 0) {
     		pattern = Pattern.compile(regex);
     	}
 
 
     	try {
     		tagger = build(sOpenNLPDir, sLanguage);
     	}
     	catch ( Throwable t ) {
     		console.severe("Failed to open tokenizer model for " + sLanguage);
     		throw new ComponentExecutionException(t);
     	}
 
 
     	String[] fields =
     		new String[] {POS_FIELD, SENTENCE_ID_FIELD, TOKEN_START_FIELD, TOKEN_FIELD};
 
     	this.tuplePeer = new SimpleTuplePeer(fields);
 
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception
 	{
 
 		List<Strings> output = new ArrayList<Strings>();
 
 
 		// input was encoded via :
 		// cc.pushDataComponentToOutput(OUT_TOKENS, BasicDataTypesTools.stringToStrings(ta));
 		//
 
 		//
 		// NEED a parser here ? DataTypeParser.parseAsMap ???
 		//
 		StringsMap input = (StringsMap) cc.getDataComponentFromInput(IN_TOKENS);
 
         int globalOffset = 0;
 		int count = input.getKeyCount();
 		console.fine("processing " + count);
 
 		int POS_IDX         = tuplePeer.getIndexForFieldName(POS_FIELD);
 		int SENTENCE_ID_IDX = tuplePeer.getIndexForFieldName(SENTENCE_ID_FIELD);
 		int TOKEN_START_IDX = tuplePeer.getIndexForFieldName(TOKEN_START_FIELD);
 		int TOKEN_IDX       = tuplePeer.getIndexForFieldName(TOKEN_FIELD);
 
 		SimpleTuple tuple = tuplePeer.createTuple();
 
 		for (int i = 0; i < count; i++) {
 			String key    = input.getKey(i);    // this is the entire sentence
 			Strings value = input.getValue(i);  // this is the set of tokens for that sentence
 
 			String[] tokens = DataTypeParser.parseAsString(value);
 			String[] tags   = tagger.tag(tokens);
 
 			int withinSentenceOffset = 0;
 			for (int j = 0; j < tags.length; j++) {
 
 				if ( pattern == null || pattern.matcher(tags[j]).matches())
 				{
 				   // find where the token is in the sentence
 				   int tokenStart = key.indexOf(tokens[j], withinSentenceOffset);
 				   // add in the global offset
 				   tokenStart += globalOffset;
 
 
 				   tuple.setValue(POS_IDX,         tags[j]);
 				   tuple.setValue(SENTENCE_ID_IDX, i);  // keep this zero based
 				   tuple.setValue(TOKEN_START_IDX, tokenStart);
 				   tuple.setValue(TOKEN_IDX,       tokens[j]);
 
 				   //
 				   // we have a choice at this point:
 				   // we can push out each result
 				   // or we can collect all the results
 				   // and push out an array of results
 				   //
 				   // console.fine("pos pushing tuple " + tuple.toString());
 
 				   output.add(tuple.convert());
 
 
 				   /*
 				    * cc.pushDataComponentToOutput(OUT_POS_TUPLE,tuple.convert());
 				    *
 				    * ALSO push the meta data here as well
 				    *
 				    */
 
 
 				}
 
 				withinSentenceOffset += tokens[j].length();
 
 			}
 			// add the key's length, not the offset
 			// since the key will contain white space
 			// we need a true index
 			globalOffset += key.length();
 		}
 
 
 		// push the whole collection, protocol safe
 	    Strings[] results = new Strings[output.size()];
 	    output.toArray(results);
 
 	    StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
 	    cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
 
 	    //
 		// metaData for this tuple producer
 		//
 	    Strings metaData = tuplePeer.convert();
 	    cc.pushDataComponentToOutput(OUT_META_TUPLE, metaData);
 	}
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         super.disposeCallBack(ccp);
         this.tagger = null;
     }
 
     //--------------------------------------------------------------------------------------------
 
     public static POSTaggerME build(String sOpenNLPDir, String sLanguage) throws IOException
     {
 
         // from maxent-models.jar
         String tagPath          = // e.g.  /opennlp/models/English/parser/tag.bin.gz
             sOpenNLPDir + "parser" + File.separator+"tag.bin.gz";
 
         //		String dictionaryPath    = // e.g. /opennlp/models/English/parser/dict.bin.gz
         //			sOpenNLPDir + "parser"+ File.separator+"dict.bin.gz";
 
         String tagDictionaryPath = // e.g. /opennlp/models/English/parser/tagdict
             sOpenNLPDir + "parser"+ File.separator+"tagdict";
 
         File tagFile     = new File(tagPath);
         //		File dictFile    = new File(dictionaryPath);
         File tagDictFile = new File(tagDictionaryPath);
 
         if (! tagFile.canRead()) {
             throw new IOException("Failed to open tag file for " + tagPath);
         }
 
         if (! tagDictFile.canRead()) {
             throw new IOException("Failed to open tag dictionary model for " + tagDictionaryPath);
         }
 
         /*
     	if (! dictFile.canRead()) {
     		console.severe("Failed to open dictionary model for " + dictionaryPath);
     		throw new ComponentExecutionException();
     	}
     	InputStream dIs  = new FileInputStream(dictFile);
          */
 
 
         /*  NEW WAY  */
 
         POSTaggerME tagger = new PosTagger(tagPath,
                 // new Dictionary(dIs),
                 new POSDictionary(tagDictionaryPath, true));
 
 
         return tagger;
 
 
 
         /* OLD WAY  using the ngram Dictionary (which no longer exists)
     	tagger = new PosTagger(tagPath,
     			               new Dictionary(dictionaryPath),
     			               new POSDictionary(tagDictionaryPath, true));
          */
 
     }
 }
