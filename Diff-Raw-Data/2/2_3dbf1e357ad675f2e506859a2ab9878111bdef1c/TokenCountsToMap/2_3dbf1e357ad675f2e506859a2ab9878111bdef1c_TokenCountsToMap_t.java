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
 
 package org.seasr.datatypes.tranformations;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.datatypes.BasicDataTypes.IntegersMap;
 import org.seasr.meandre.components.tools.Names;
 
 
 /**
  * This component transforms a token count into a map.
  *
  * @author Xavier Llor&agrave;
  * @author Boris Capitanu
  *
  */
 @Component(
		name = "Token Counts To Map",
 		creator = "Xavier Llora",
 		baseURL = "meandre://seasr.org/components/tools/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "tools, tokenizer, counting, transformations",
 		description = "Given a collection of token counts, this component converts them " +
 				      "to a Java map.",
 		dependency = {"protobuf-java-2.0.3.jar"}
 )
 public class TokenCountsToMap extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 			name = Names.PORT_TOKEN_COUNTS,
 			description = "The token counts to convert to text"
 	)
 	protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			name = Names.PORT_TOKEN_MAP,
 			description = "The converted token map"
 		)
 	private final static String OUT_TOKEN_MAP = Names.PORT_TOKEN_MAP;
 
 
 	//--------------------------------------------------------------------------------------------
 
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
 	public void executeCallBack(ComponentContext cc) throws Exception {
 		cc.pushDataComponentToOutput(OUT_TOKEN_MAP,
 		        BasicDataTypesTools.IntegerMapToMap(
 		                (IntegersMap)cc.getDataComponentFromInput(IN_TOKEN_COUNTS)));
 	}
 
 	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
 	}
 }
