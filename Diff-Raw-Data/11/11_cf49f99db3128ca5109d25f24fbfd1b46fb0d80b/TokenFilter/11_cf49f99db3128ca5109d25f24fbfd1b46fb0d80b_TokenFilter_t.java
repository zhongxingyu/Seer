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
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Set;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.meandre.core.ExecutableComponent;
 import org.meandre.core.system.components.ext.StreamDelimiter;
 import org.seasr.datatypes.BasicDataTypes;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.datatypes.BasicDataTypes.Integers;
 import org.seasr.datatypes.BasicDataTypes.IntegersMap;
 import org.seasr.datatypes.BasicDataTypes.Strings;
 import org.seasr.datatypes.BasicDataTypes.StringsMap;
 import org.seasr.meandre.components.tools.Names;
 
 /** This component tokenizes the text contained in the input model using OpenNLP.
  *
 * @author Xavier Llor&agrave;
  *
  */
 @SuppressWarnings("unchecked")
 @Component(
 		name = "Token filter",
 		creator = "Xavier Llora",
 		baseURL = "meandre://seasr.org/components/tools/",
 		firingPolicy = FiringPolicy.any,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		dependency = {"protobuf-java-2.0.3.jar"},
 		tags = "semantic, tools, text, filter, tokenizer",
 		description = "This component filters the tokens on the inputs based " +
 				      "based on the list of tokens provided. If new tokens to " +
 				      "filter are provide they either replace the current ones " +
 				      "or add them to the black list. The component outputs the " +
 				      "filtered tokens."
 )
 public class TokenFilter
 implements ExecutableComponent {
 
 	//--------------------------------------------------------------------------------------------
 
 	@ComponentProperty(
 			name = Names.PROP_ERROR_HANDLING,
 			description = "If set to true errors will be handled and they will be reported to the screen ." +
 					      "Otherwise, the component will throw an exception an force the flow to abort. ",
 		    defaultValue = "true"
 		)
 	protected final static String PROP_ERROR_HANDLING = Names.PROP_ERROR_HANDLING;
 
 	@ComponentProperty(
 			name = Names.PROP_REPLACE,
 			description = "If set to true errors blacklisted tokens get replaced when a new set is provided. " +
 					      "When set to false, tokens keep being appended to the blacklist. ",
 		    defaultValue = "true"
 		)
 	protected final static String PROP_REPLACE = Names.PROP_REPLACE;
 
 
 	//--------------------------------------------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_TOKEN_BLACKLIST,
 			description = "The list of tokens defining the blacklist."
 		)
 	private final static String INPUT_TOKEN_BLACKLIST = Names.PORT_TOKEN_BLACKLIST;
 
 	@ComponentInput(
 			name = Names.PORT_TOKENS,
 			description = "The sequence of tokens to filter"
 		)
 	private final static String INPUT_TOKENS = Names.PORT_TOKENS;
 
 	@ComponentInput(
 			name = Names.PORT_TOKEN_COUNTS,
 			description = "The token counts to filter"
 		)
 	private final static String INPUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;
 
 	@ComponentInput(
 			name = Names.PORT_TOKENIZED_SENTENCES,
 			description = "The tokenized sentences to filter"
 		)
 	private final static String INPUT_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;
 
 	@ComponentOutput(
 			name = Names.PORT_TOKENS,
 			description = "The filtered of tokens"
 		)
 	private final static String OUTPUT_TOKENS = Names.PORT_TOKENS;
 
 	@ComponentOutput(
 			name = Names.PORT_TOKEN_COUNTS,
 			description = "The filtered token counts"
 		)
 	private final static String OUTPUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;
 
 	@ComponentOutput(
 			name = Names.PORT_TOKENIZED_SENTENCES,
 			description = "The filtered tokenized sentences"
 		)
 	private final static String OUTPUT_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;
 
 
 	//--------------------------------------------------------------------------------------------
 
 	/** The error handling flag */
 	protected boolean bErrorHandling;
 
 	/** Should the token black list be replaced */
 	protected boolean bReplace;
 
 	/** The temporary initial queue */
 	protected Queue<Object>[] queues  = new Queue[3];
 	protected final int PORT_TOKENS = 0;
 	protected final int PORT_TOKEN_COUNTS = 1;
 	protected final int PORT_TOKENIZE_SENTENCES = 2;
 
 	/** The list of available inputs */
 	protected String [] saInputName = null;
 
 	/** The set of blacklisted tokens */
 	protected Set<String> setBlacklist = null;
 
 	//--------------------------------------------------------------------------------------------
 
 	/**
 	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
 	 */
 	public void initialize(ComponentContextProperties ccp)
 			throws ComponentExecutionException, ComponentContextException {
 		this.bErrorHandling = Boolean.parseBoolean(ccp.getProperty(PROP_ERROR_HANDLING));
 		this.bReplace = Boolean.parseBoolean(ccp.getProperty(PROP_REPLACE));
 		this.queues[PORT_TOKENS] = new LinkedList<Object>();
 		this.queues[PORT_TOKEN_COUNTS] = new LinkedList<Object>();
 		this.queues[PORT_TOKENIZE_SENTENCES] = new LinkedList<Object>();
 		this.saInputName = ccp.getInputNames();
 		this.setBlacklist = null;
 	}
 
 	/**
 	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
 	 */
 	public void dispose(ComponentContextProperties ccp)
 			throws ComponentExecutionException, ComponentContextException {
 		this.bReplace = false;
 		this.queues[PORT_TOKENS] = this.queues[PORT_TOKEN_COUNTS] = this.queues[PORT_TOKENIZE_SENTENCES] = null;
 		this.queues = null;
 		this.saInputName = null;
 		this.setBlacklist = null;
 		this.bErrorHandling = false;
 	}
 
 	/**
 	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
 	 */
 	public void execute(ComponentContext cc)
 			throws ComponentExecutionException, ComponentContextException {
 
 		if ( this.setBlacklist==null && !cc.isInputAvailable(INPUT_TOKEN_BLACKLIST) ) {
 			// No blacklist received yet, so queue the objects
 			queueObjects(cc);
 		}
 		else if ( this.setBlacklist==null && cc.isInputAvailable(INPUT_TOKEN_BLACKLIST) ) {
 			// Process blacklist and pending
 			processBlacklistAndQueued(cc);
 		}
 		else {
 			// Process normally with the incoming information
 			processNormally(cc);
 		}
 	}
 
 	//--------------------------------------------------------------------------------------------
 
 	/** No blacklist currently available, just queue the objects in the inputs.
 	 *
 	 * @param cc The component context object
 	 * @throws ComponentContextException Invalid access to the component context
 	 */
 	private void queueObjects(ComponentContext cc) throws ComponentContextException {
 		if ( cc.isInputAvailable(INPUT_TOKENS) )
 			this.queues[PORT_TOKENS].offer(cc.getDataComponentFromInput(INPUT_TOKENS));
 		if ( cc.isInputAvailable(INPUT_TOKEN_COUNTS) )
 			this.queues[PORT_TOKEN_COUNTS].offer(cc.getDataComponentFromInput(INPUT_TOKEN_COUNTS));
 		if ( cc.isInputAvailable(INPUT_TOKENIZED_SENTENCES) )
 			this.queues[PORT_TOKENIZE_SENTENCES].offer(cc.getDataComponentFromInput(INPUT_TOKENIZED_SENTENCES));
 	}
 
 
 	/** Process the black list and catches up with the pending objects.
 	 *
 	 * @param cc The component context object
 	 * @throws ComponentContextException Invalid access to the component context
 	 * @throws ComponentExecutionException Black list cast exception
 	 */
 	private void processBlacklistAndQueued(ComponentContext cc) throws ComponentContextException, ComponentExecutionException {
 		processBlacklist(cc.getDataComponentFromInput(INPUT_TOKEN_BLACKLIST),cc);
 		processQueuedObjects(cc);
 	}
 
 	/** Process the blacklist.
 	 *
 	 * @param objBlackList The black list to process
 	 * @param cc The component context
 	 * @throws ComponentExecutionException The blacklist was not of type Strings
 	 */
 	private void processBlacklist(Object objBlackList, ComponentContext cc ) throws ComponentExecutionException {
 		try {
 			if ( objBlackList instanceof StreamDelimiter )
 				return;
 			else {
 				Strings strBlacklist = (Strings)objBlackList;
 				if ( this.setBlacklist==null )
 					this.setBlacklist = new HashSet<String>(100);
 				if ( this.bReplace )
 					this.setBlacklist.clear();
 				for ( String s:strBlacklist.getValueList() )
 					this.setBlacklist.add(s);
 			}
 		}
 		catch ( ClassCastException e ) {
 			String sMessage = "Input data is not from the basic type Strings required for blacklists";
 			cc.getLogger().warning(sMessage);
 			cc.getOutputConsole().println("WARNING: "+sMessage);
 			if ( !bErrorHandling )
 				throw new ComponentExecutionException(e);
 		}
 	}
 
 	/** Process the queued object.
 	 *
 	 * @param cc The component context
 	 * @throws ComponentContextException Something went wrong while executing
 	 * @throws ComponentExecutionException Invalid casts
 	 */
 	private void processQueuedObjects(ComponentContext cc) throws ComponentContextException, ComponentExecutionException {
 		Iterator<Object> iterTok = this.queues[PORT_TOKENS].iterator();
 		while ( iterTok.hasNext() ) processTokens(iterTok.next(),cc);
 
 		Iterator<Object> iterTokCnts = this.queues[PORT_TOKEN_COUNTS].iterator();
 		while ( iterTokCnts.hasNext() ) processTokenCounts(iterTokCnts.next(),cc);
 
 		Iterator<Object> iterTS = this.queues[PORT_TOKENIZE_SENTENCES].iterator();
 		while ( iterTS.hasNext() ) processTokenizedSentences(iterTS.next(),cc);
 	}
 
 
 	/** Process the inputs normally.
 	 *
 	 * @param cc The component context
 	 * @throws ComponentExecutionException Problem while executing
 	 * @throws ComponentContextException  The component context was improperly exception
 	 *
 	 */
 	private void processNormally(ComponentContext cc) throws ComponentContextException, ComponentExecutionException {
 		if ( cc.isInputAvailable(INPUT_TOKEN_BLACKLIST)) {
 			processBlacklist(cc.getDataComponentFromInput(INPUT_TOKEN_BLACKLIST),cc);
 		}
 		if ( cc.isInputAvailable(INPUT_TOKENS) ) {
 			processTokens(cc.getDataComponentFromInput(INPUT_TOKENS),cc);
 		}
 
 		if ( cc.isInputAvailable(INPUT_TOKEN_COUNTS) ) {
 			processTokenCounts(cc.getDataComponentFromInput(INPUT_TOKEN_COUNTS),cc);
 		}
 
 		if ( cc.isInputAvailable(INPUT_TOKENIZED_SENTENCES) ) {
 			processTokenizedSentences(cc.getDataComponentFromInput(INPUT_TOKENIZED_SENTENCES),cc);
 		}
 	}
 
 	/** Process tokenized sentences.
 	 *
 	 * @param next The object to process
 	 * @param cc The component context
 	 * @throws ComponentContextException Invalid access to the component context
 	 * @throws ComponentExecutionException Invalid cast
 	 */
 	private void processTokenizedSentences(Object next, ComponentContext cc) throws ComponentContextException, ComponentExecutionException {
 		if ( next instanceof StreamDelimiter )
 			cc.pushDataComponentToOutput(OUTPUT_TOKEN_COUNTS, next);
 		else {
 			StringsMap im = safeStringsMapCast(next, cc);
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
 			cc.pushDataComponentToOutput(OUTPUT_TOKENIZED_SENTENCES, res.build());
 		}
 	}
 
 	/** Process token counts sentences.
 	 *
 	 * @param next The object to process
 	 * @param cc The component context
 	 * @throws ComponentContextException Invalid access to the component context
 	 * @throws ComponentExecutionException Class cast exception
 	 */
 	private void processTokenCounts(Object next, ComponentContext cc) throws ComponentContextException, ComponentExecutionException {
 		if ( next instanceof StreamDelimiter )
 			cc.pushDataComponentToOutput(OUTPUT_TOKEN_COUNTS, next);
 		else {
 			IntegersMap im = safeIntegerMapCast(next, cc);
 			org.seasr.datatypes.BasicDataTypes.IntegersMap.Builder res = BasicDataTypes.IntegersMap.newBuilder();
 			for ( int i=0, iMax=im.getKeyCount() ; i<iMax ; i++ ) {
 				String sKey = im.getKey(i);
 				Integers iVals = im.getValue(i);
 				if ( !this.setBlacklist.contains(sKey) ) {
 					res.addKey(sKey);
 					res.addValue(iVals);
 				}
 			}
 			cc.pushDataComponentToOutput(OUTPUT_TOKEN_COUNTS, res.build());
 		}
 	}
 
 	/** Process tokens sentences.
 	 *
 	 * @param next The object to process
 	 * @param cc The component context
 	 * @throws ComponentContextException Invalid access to the component context
 	 * @throws ComponentExecutionException Invalid cast
 	 */
 	private void processTokens(Object next, ComponentContext cc) throws ComponentContextException, ComponentExecutionException {
 		if ( next instanceof StreamDelimiter )
 			cc.pushDataComponentToOutput(OUTPUT_TOKENS, next);
 		else {
 			Strings sToks = safeStringsCast(next,cc);
 			org.seasr.datatypes.BasicDataTypes.Strings.Builder res = BasicDataTypes.Strings.newBuilder();
 			for ( String sTok:sToks.getValueList() )
 				if ( !this.setBlacklist.contains(sTok) )
 					res.addValue(sTok);
 			cc.pushDataComponentToOutput(OUTPUT_TOKENS, res.build());
 		}
 	}
 
 	/** Safe cast of strings.
 	 *
 	 * @param next The object to cast into strings
 	 * @param cc The component context
 	 * @return The strings
 	 * @throws ComponentExecutionException
 	 */
 	private Strings safeStringsCast(Object next, ComponentContext cc)
 	throws ComponentExecutionException {
 		try {
 			return (Strings)next;
 		}
 		catch ( ClassCastException e ) {
 			String sMessage = "Input data is not from the basic type Strings required for blacklists";
 			cc.getLogger().warning(sMessage);
 			cc.getOutputConsole().println("WARNING: "+sMessage);
 			if ( !bErrorHandling )
 				throw new ComponentExecutionException(e);
 			return BasicDataTypesTools.buildEmptyStrings();
 		}
 	}
 
 	/** Safe cast of integers map.
 	 *
 	 * @param next The object to cast into strings
 	 * @param cc The component context
 	 * @return The strings
 	 * @throws ComponentExecutionException
 	 */
 	private IntegersMap safeIntegerMapCast(Object next, ComponentContext cc)
 	throws ComponentExecutionException {
 		try {
 			return (IntegersMap)next;
 		}
 		catch ( ClassCastException e ) {
 			String sMessage = "Input data is not from the basic type Strings required for blacklists";
 			cc.getLogger().warning(sMessage);
 			cc.getOutputConsole().println("WARNING: "+sMessage);
 			if ( !bErrorHandling )
 				throw new ComponentExecutionException(e);
 			return BasicDataTypesTools.buildEmptyIntegersMap();
 		}
 	}
 
 	/** Safe cast of strings map.
 	 *
 	 * @param next The object to cast into strings
 	 * @param cc The component context
 	 * @return The strings
 	 * @throws ComponentExecutionException
 	 */
 	private StringsMap safeStringsMapCast(Object next, ComponentContext cc)
 	throws ComponentExecutionException {
 		try {
 			return (StringsMap)next;
 		}
 		catch ( ClassCastException e ) {
 			String sMessage = "Input data is not from the basic type Strings required for blacklists";
 			cc.getLogger().warning(sMessage);
 			cc.getOutputConsole().println("WARNING: "+sMessage);
 			if ( !bErrorHandling )
 				throw new ComponentExecutionException(e);
 			return BasicDataTypesTools.buildEmptyStringsMap();
 		}
 	}
 
 
 }
