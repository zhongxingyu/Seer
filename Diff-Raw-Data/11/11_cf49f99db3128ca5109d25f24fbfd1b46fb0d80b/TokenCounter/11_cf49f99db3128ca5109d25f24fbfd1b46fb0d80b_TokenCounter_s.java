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
 
 package org.seasr.meandre.components.analytics.text.statistics;
 
 import java.util.Hashtable;
 
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
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.datatypes.BasicDataTypes.Strings;
 import org.seasr.meandre.components.tools.Names;
 
 
 /** This component tokenizes the text contained in the input model using OpenNLP.
  *
 * @author Xavier Llor�
  *
  */
 @Component(
 		name = "Token counter",
 		creator = "Xavier Llora",
 		baseURL = "meandre://seasr.org/components/tools/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		dependency = {"protobuf-java-2.0.3.jar"},
 		tags = "semantic, tools, text, tokenizer, counting",
 		description = "Given a document containing collections of tokens, " +
 				      "this component counts all the different occurences of the " +
 				      "tokens. If the document contains multiple token sequences, the " +
 				      "component aggregate all the sequences providing a cummulative count."
 )
 public class TokenCounter
 implements ExecutableComponent {
 
 	//--------------------------------------------------------------------------------------------
 
 	@ComponentProperty(
 			name = Names.PROP_ERROR_HANDLING,
 			description = "If set to true errors will be handled and they will be reported to the screen ." +
 					      "Otherwise, the component will throw an exception an force the flow to abort. ",
 		    defaultValue = "true"
 		)
 	private final static String PROP_ERROR_HANDLING = Names.PROP_ERROR_HANDLING;
 
 	@ComponentProperty(
 			name = Names.PROP_ORDERED,
 			description = "Should the token counts be ordered?",
 			defaultValue = "true"
 		)
 	private final static String PROP_ORDERED = Names.PROP_ORDERED;
 
 	//--------------------------------------------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_TOKENS,
 			description = "The tokens to be counted"
 		)
 	private final static String INPUT_TOKENS = Names.PORT_TOKENS;
 
 	@ComponentOutput(
 			name = Names.PORT_TOKEN_COUNTS,
 			description = "The token counts"
 		)
 	private final static String OUTPUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;
 
 	//--------------------------------------------------------------------------------------------
 
 	/** The error handling flag */
 	private boolean bErrorHandling;
 
 	/** Should the tokens be ordered */
 	private boolean bOrdered;
 
 	//--------------------------------------------------------------------------------------------
 
 	/**
 	 * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
 	 */
 	public void initialize(ComponentContextProperties ccp)
 			throws ComponentExecutionException, ComponentContextException {
 		this.bErrorHandling = Boolean.parseBoolean(ccp.getProperty(PROP_ERROR_HANDLING));
 		this.bOrdered = Boolean.parseBoolean(ccp.getProperty(PROP_ORDERED));
 	}
 
 	/**
 	 * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
 	 */
 	public void dispose(ComponentContextProperties ccp)
 			throws ComponentExecutionException, ComponentContextException {
 	}
 
 	/**
 	 * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
 	 */
 	public void execute(ComponentContext cc)
 			throws ComponentExecutionException, ComponentContextException {
 		Object obj = cc.getDataComponentFromInput(INPUT_TOKENS);
 		if ( obj instanceof StreamDelimiter )
 			cc.pushDataComponentToOutput(OUTPUT_TOKEN_COUNTS, obj);
 		else {
 			Hashtable<String,Integer> htCounts = new Hashtable<String,Integer>(1000);
 			try {
 				Strings strTokens = (Strings)obj;
 				// Retrieve the tokens and count them
 				for ( String sToken:strTokens.getValueList() ) {
 					if ( htCounts.containsKey(sToken) )
 						htCounts.put(sToken, htCounts.get(sToken)+1);
 					else
 						htCounts.put(sToken, 1);
 				}
 
 			} catch (ClassCastException e ) {
 				String sMessage = "Input data is not a semantic model";
 				cc.getLogger().warning(sMessage);
 				cc.getOutputConsole().println("WARNING: "+sMessage);
 				if ( !bErrorHandling )
 					throw new ComponentExecutionException(e);
 			}
 
 			cc.pushDataComponentToOutput(OUTPUT_TOKEN_COUNTS, BasicDataTypesTools.mapToIntegerMap(htCounts,bOrdered));
 		}
 	}
 
 	//--------------------------------------------------------------------------------------------
 
 
 
 }
