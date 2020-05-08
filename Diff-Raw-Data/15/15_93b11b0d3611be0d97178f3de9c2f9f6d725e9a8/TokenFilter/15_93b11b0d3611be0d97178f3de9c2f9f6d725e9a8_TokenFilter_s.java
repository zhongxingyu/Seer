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
 
 package org.seasr.meandre.components.analytics.text.filters;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.seasr.datatypes.BasicDataTypes;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.datatypes.BasicDataTypes.Strings;
 import org.seasr.datatypes.BasicDataTypes.StringsMap;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.exceptions.UnsupportedDataTypeException;
 import org.seasr.meandre.support.parsers.DataTypeParser;
 
 /** This component tokenizes the text contained in the input model using OpenNLP.
  *
  * @author Xavier Llor&agrave;
  * @author Boris Capitanu
  *
  */
 
 @Component(
 		name = "Token Filter",
 		creator = "Xavier Llora",
 		baseURL = "meandre://seasr.org/components/tools/",
 		firingPolicy = FiringPolicy.any,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "semantic, tools, text, filter, tokenizer",
 		description = "This component filters the tokens on the inputs based " +
 				      "based on the list of tokens provided. If new tokens to " +
 				      "filter are provide they either replace the current ones " +
 				      "or add them to the black list. The component outputs the " +
 				      "filtered tokens.",
 		dependency = {"protobuf-java-2.0.3.jar"}
 )
 @SuppressWarnings("unchecked")
 public class TokenFilter extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_TOKEN_BLACKLIST,
 			description = "The list of tokens defining the blacklist."
 	)
 	protected static final String IN_TOKEN_BLACKLIST = Names.PORT_TOKEN_BLACKLIST;
 
 	@ComponentInput(
 			name = Names.PORT_TOKENS,
 			description = "The sequence of tokens to filter"
 	)
 	protected static final String IN_TOKENS = Names.PORT_TOKENS;
 
 	@ComponentInput(
 			name = Names.PORT_TOKEN_COUNTS,
 			description = "The token counts to filter"
 	)
 	protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;
 
 	@ComponentInput(
 			name = Names.PORT_TOKENIZED_SENTENCES,
 			description = "The tokenized sentences to filter"
 	)
 	protected static final String IN_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;
 
 	//------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			name = Names.PORT_TOKENS,
 			description = "The filtered of tokens"
 	)
 	protected static final String OUT_TOKENS = Names.PORT_TOKENS;
 
 	@ComponentOutput(
 			name = Names.PORT_TOKEN_COUNTS,
 			description = "The filtered token counts"
 	)
 	protected static final String OUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;
 
 	@ComponentOutput(
 			name = Names.PORT_TOKENIZED_SENTENCES,
 			description = "The filtered tokenized sentences"
 	)
 	protected static final String OUT_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
 	// Inherited ignoreErrors (PROP_IGNORE_ERRORS) from AbstractExecutableComponent
 
     @ComponentProperty(
             name = Names.PROP_REPLACE,
             description = "If set to true errors blacklisted tokens get replaced when a new set is provided. " +
                           "When set to false, tokens keep being appended to the blacklist. ",
             defaultValue = "true"
     )
     protected static final String PROP_REPLACE = Names.PROP_REPLACE;
 
 	//--------------------------------------------------------------------------------------------
 
 
 	/** Should the token black list be replaced */
 	protected boolean bReplace;
 
 	/** The temporary initial queue */
 	protected Queue<Object>[] queues  = new Queue[3];
 	protected final int PORT_TOKENS = 0;
 	protected final int PORT_TOKEN_COUNTS = 1;
 	protected final int PORT_TOKENIZED_SENTENCES = 2;
 
 	/** The list of available inputs */
 	protected String [] saInputName = null;
 
 	/** The set of blacklisted tokens */
 	protected Set<String> setBlacklist = null;
 
 
 	//--------------------------------------------------------------------------------------------
 
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 		this.bReplace = Boolean.parseBoolean(ccp.getProperty(PROP_REPLACE));
 		this.queues[PORT_TOKENS] = new LinkedList<Object>();
 		this.queues[PORT_TOKEN_COUNTS] = new LinkedList<Object>();
 		this.queues[PORT_TOKENIZED_SENTENCES] = new LinkedList<Object>();
 		this.saInputName = ccp.getInputNames();
 		this.setBlacklist = null;
 	}
 
	public void executeCallBack(ComponentContext cc) throws Exception {
 		if ( this.setBlacklist == null && !cc.isInputAvailable(IN_TOKEN_BLACKLIST) ) {
 			// No blacklist received yet, so queue the objects
 			queueObjects();
 		}
 		else if ( this.setBlacklist == null && cc.isInputAvailable(IN_TOKEN_BLACKLIST) ) {
 			// Process blacklist and pending
 			processBlacklistAndQueued();
 		}
 		else {
 			// Process normally with the incoming information
 			processNormally();
 		}
 	}
 
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         this.bReplace = false;
         this.queues[PORT_TOKENS] = this.queues[PORT_TOKEN_COUNTS] = this.queues[PORT_TOKENIZED_SENTENCES] = null;
         this.queues = null;
         this.saInputName = null;
         this.setBlacklist = null;
     }
 
 	//--------------------------------------------------------------------------------------------
 
     @Override
     protected void handleStreamInitiators() throws Exception {
         if (inputPortsWithInitiators.contains(IN_TOKENIZED_SENTENCES))
            componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS,
                     componentContext.getDataComponentFromInput(IN_TOKENIZED_SENTENCES));
 
         if (inputPortsWithInitiators.contains(IN_TOKEN_COUNTS))
             componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS,
                     componentContext.getDataComponentFromInput(IN_TOKEN_COUNTS));
 
         if (inputPortsWithInitiators.contains(IN_TOKENS))
             componentContext.pushDataComponentToOutput(OUT_TOKENS,
                     componentContext.getDataComponentFromInput(IN_TOKENS));
     }
 
     @Override
     protected void handleStreamTerminators() throws Exception {
         if (inputPortsWithTerminators.contains(IN_TOKENIZED_SENTENCES))
            componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS,
                     componentContext.getDataComponentFromInput(IN_TOKENIZED_SENTENCES));
 
         if (inputPortsWithTerminators.contains(IN_TOKEN_COUNTS))
             componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS,
                     componentContext.getDataComponentFromInput(IN_TOKEN_COUNTS));
 
         if (inputPortsWithTerminators.contains(IN_TOKENS))
             componentContext.pushDataComponentToOutput(OUT_TOKENS,
                     componentContext.getDataComponentFromInput(IN_TOKENS));
     }
 
     //--------------------------------------------------------------------------------------------
 
 	/**
 	 * No blacklist currently available, just queue the objects in the inputs.
 	 *
 	 * @throws ComponentContextException Invalid access to the component context
 	 */
 	private void queueObjects() throws ComponentContextException {
 		if ( componentContext.isInputAvailable(IN_TOKENS) )
 			this.queues[PORT_TOKENS].offer(componentContext.getDataComponentFromInput(IN_TOKENS));
 		if ( componentContext.isInputAvailable(IN_TOKEN_COUNTS) )
 			this.queues[PORT_TOKEN_COUNTS].offer(componentContext.getDataComponentFromInput(IN_TOKEN_COUNTS));
 		if ( componentContext.isInputAvailable(IN_TOKENIZED_SENTENCES) )
 			this.queues[PORT_TOKENIZED_SENTENCES].offer(componentContext.getDataComponentFromInput(IN_TOKENIZED_SENTENCES));
 	}
 
 	/**
 	 * Process the black list and catches up with the pending objects.
 	 *
 	 * @throws Exception Thrown if something goes wrong
 	 */
 	protected void processBlacklistAndQueued() throws Exception {
 		processBlacklist(componentContext.getDataComponentFromInput(IN_TOKEN_BLACKLIST));
 		processQueuedObjects();
 	}
 
 	/**
 	 * Process the blacklist.
 	 *
 	 * @param objBlackList The black list to process
 	 * @throws Exception Thrown if something goes wrong
 	 */
 	protected void processBlacklist(Object objBlackList) throws Exception {
 	    String[] words = DataTypeParser.parseAsString(objBlackList);
 		if ( this.setBlacklist==null )
 			this.setBlacklist = new HashSet<String>(100);
 		if ( this.bReplace )
 			this.setBlacklist.clear();
 		for ( String s : words )
 			this.setBlacklist.add(s);
 	}
 
 	/**
 	 * Process the queued object.
 	 *
 	 * @throws Exception Something went wrong while executing
 	 */
 	protected void processQueuedObjects() throws Exception {
 		Iterator<Object> iterTok = this.queues[PORT_TOKENS].iterator();
 		while ( iterTok.hasNext() ) processTokens(iterTok.next());
 
 		Iterator<Object> iterTokCnts = this.queues[PORT_TOKEN_COUNTS].iterator();
 		while ( iterTokCnts.hasNext() ) processTokenCounts(iterTokCnts.next());
 
 		Iterator<Object> iterTS = this.queues[PORT_TOKENIZED_SENTENCES].iterator();
 		while ( iterTS.hasNext() ) processTokenizedSentences(iterTS.next());
 	}
 
 
 	/**
 	 * Process the inputs normally.
 	 *
 	 * @throws Exception Problem while executing
 	 *
 	 */
 	protected void processNormally() throws Exception {
 		if ( componentContext.isInputAvailable(IN_TOKEN_BLACKLIST)) {
 			processBlacklist(componentContext.getDataComponentFromInput(IN_TOKEN_BLACKLIST));
 		}
 		if ( componentContext.isInputAvailable(IN_TOKENS) ) {
 			processTokens(componentContext.getDataComponentFromInput(IN_TOKENS));
 		}
 
 		if ( componentContext.isInputAvailable(IN_TOKEN_COUNTS) ) {
 			processTokenCounts(componentContext.getDataComponentFromInput(IN_TOKEN_COUNTS));
 		}
 
 		if ( componentContext.isInputAvailable(IN_TOKENIZED_SENTENCES) ) {
 			processTokenizedSentences(componentContext.getDataComponentFromInput(IN_TOKENIZED_SENTENCES));
 		}
 	}
 
 	/**
 	 * Process tokenized sentences.
 	 *
 	 * @param next The object to process
 	 * @throws Exception
 	 */
 	protected void processTokenizedSentences(Object next) throws Exception {
 		StringsMap im = safeStringsMapCast(next);
 		org.seasr.datatypes.BasicDataTypes.StringsMap.Builder res = BasicDataTypes.StringsMap.newBuilder();
 		for ( int i=0, iMax=im.getKeyCount() ; i<iMax ; i++ ) {
 			String sKey = im.getKey(i);
 			Strings sVals = im.getValue(i);
 			org.seasr.datatypes.BasicDataTypes.Strings.Builder resFiltered = BasicDataTypes.Strings.newBuilder();
 			for ( String s:sVals.getValueList())
 				if ( !this.setBlacklist.contains(s) )
 					resFiltered.addValue(s);
 			res.addKey(sKey);
 			res.addValue(resFiltered.build());
 		}
 		componentContext.pushDataComponentToOutput(OUT_TOKENIZED_SENTENCES, res.build());
 	}
 
 	/**
 	 * Process token counts sentences.
 	 *
 	 * @param next The object to process
 	 * @throws Exception
 	 */
 	protected void processTokenCounts(Object next) throws Exception {
 		Map<String, Integer> im;
 		try {
 		    im = DataTypeParser.parseAsStringIntegerMap(next);
 		}
 		catch (UnsupportedDataTypeException e) {
 		    if (ignoreErrors) {
 		        console.warning("processTokenCounts: UnsupportedDataTypeException ignored - input data was not in the correct format");
 		        im = new HashMap<String, Integer>();
 		    }
 		    else
 		        throw e;
 		}
 
 		Map<String, Integer> res = new HashMap<String, Integer>();
 		for ( Entry<String, Integer> entry : im.entrySet()) {
 			String sKey = entry.getKey();
 			Integer iVal = entry.getValue();
 			if ( !this.setBlacklist.contains(sKey) )
 				res.put(sKey, iVal);
 		}
 
 		componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS,
 		        BasicDataTypesTools.mapToIntegerMap(res, false));
 	}
 
 	/**
 	 * Process tokens sentences.
 	 *
 	 * @param next The object to process
 	 * @throws Exception
 	 */
 	private void processTokens(Object next) throws Exception {
 		String[] tokens;
 		try {
             tokens = DataTypeParser.parseAsString(next);
         }
         catch (UnsupportedDataTypeException e) {
             if (ignoreErrors) {
                 console.warning("processTokens: UnsupportedDataTypeException ignored - input data was not in the correct format");
                 tokens = new String[] {};
             }
             else
                 throw e;
         }
 
 		org.seasr.datatypes.BasicDataTypes.Strings.Builder res = BasicDataTypes.Strings.newBuilder();
 		for ( String sTok : tokens )
 			if ( !this.setBlacklist.contains(sTok) )
 				res.addValue(sTok);
 
 		componentContext.pushDataComponentToOutput(OUT_TOKENS, res.build());
 	}
 
 	/**
 	 * Safe cast of strings map.
 	 *
 	 * @param next The object to cast into strings
 	 * @return The strings
 	 * @throws Exception
 	 */
 	private StringsMap safeStringsMapCast(Object next) throws Exception {
 		try {
 			return (StringsMap)next;
 		}
 		catch ( ClassCastException e ) {
 			console.warning("Input data is not from the basic type Strings required for blacklists");
 			if ( !ignoreErrors )
 				throw new ComponentExecutionException(e);
 			return BasicDataTypesTools.buildEmptyStringsMap();
 		}
 	}
 }
