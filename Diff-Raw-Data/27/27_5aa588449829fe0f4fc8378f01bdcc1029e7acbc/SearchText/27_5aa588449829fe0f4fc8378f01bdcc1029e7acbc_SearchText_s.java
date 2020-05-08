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
 
 package org.seasr.meandre.components.tools.basic;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
 
 /**
  * Searches text using regular expressions
  *
  * @author Loretta Auvil
  *
  */
 
 @Component(
         name = "Search Text",
         creator = "Loretta Auvil",
 		description = "Searches the text input for the regular expression pattern. "+
		    "If the pattern is found then the matching text is pushed out on port Text_Found "+
		    "(multiple outputs are possible), otherwise, it outputs the original "+
		    "text on port Text.",
 		tags = "text, string, search",
 		rights = Licenses.UofINCSA,
 		baseURL = "meandre://seasr.org/components/foundry/",
 		dependency = { "protobuf-java-2.2.0.jar" }
 )
 public class SearchText extends AbstractExecutableComponent {
 
 	// ------------------------------ INPUTS ------------------------------------------------------
 
 	@ComponentInput(
 	        name = Names.PORT_TEXT,
 	        description = "Text to be searched." +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
 	)
 	protected static final String IN_TEXT = Names.PORT_TEXT;
 
 	// ------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 	        name = Names.PORT_TEXT,
 	        description = "The searched text." +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_TEXT = Names.PORT_TEXT;
 
 	@ComponentOutput(
 	        name = Names.PORT_TEXT_FOUND,
 	        description = "The matching text that satisfies the regular expression." +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
 	)
 	protected static final String OUT_MATCHED_TEXT = Names.PORT_TEXT_FOUND;
 
 	// ------------------------------ PROPERTIES --------------------------------------------------
 
 	@ComponentProperty(
 	        name = Names.PROP_EXPRESSION,
 	        description = "The regular expression to use as search criteria. ",
 	        defaultValue = ".+\\.pdf")
 	protected static final String PROP_EXPRESSION = Names.PROP_EXPRESSION;
 
 	// --------------------------------------------------------------------------------------------
 
 
 	/** The regular expression */
 	private String sExpression;
 
 
 	// --------------------------------------------------------------------------------------------
 
 	@Override
 	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 		sExpression = ccp.getProperty(PROP_EXPRESSION);
 	}
 
 	@Override
 	public void executeCallBack(ComponentContext cc) throws Exception {
 		String[] input = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT));
 
 		for (String text : input) {
     		console.finest(String.format("input text: %s", text));
     		console.finer(String.format("regular expression: %s", sExpression));
 
     		Pattern compiledRegex = Pattern.compile(sExpression);
     		Matcher regexMatcher = compiledRegex.matcher(text);
     		String matchingText;
     		boolean found = false;
 
     		while (regexMatcher.find()) {
     			console.finer("start = " + regexMatcher.start());
     			console.finer("end = " + regexMatcher.end());
 
     			matchingText = text.substring(regexMatcher.start(), regexMatcher.end());
 
     			console.fine(String.format("Match: %s", matchingText));
 
     			found = true;
 
     			componentContext.pushDataComponentToOutput(OUT_MATCHED_TEXT,
                         BasicDataTypesTools.stringToStrings(matchingText));
     		}
 
     		if (!found)
     		    componentContext.pushDataComponentToOutput(OUT_TEXT,
     		            BasicDataTypesTools.stringToStrings(text));
 		}
 	}
 
 	@Override
 	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
 		sExpression = null;
 	}
 
 	// --------------------------------------------------------------------------------------------
 
 	@Override
 	protected void handleStreamInitiators() throws Exception {
		if (inputPortsWithInitiators.contains(IN_TEXT))
			componentContext.pushDataComponentToOutput(OUT_TEXT, new StreamInitiator());
		else
			throw new Exception("Unbalanced or unexpected StreamInitiator received");
 	}
 
 	@Override
 	protected void handleStreamTerminators() throws Exception {
		if (inputPortsWithTerminators.contains(IN_TEXT))
			componentContext.pushDataComponentToOutput(OUT_TEXT, new StreamTerminator());
		else
			throw new Exception("Unbalanced or unexpected StreamTerminator received");
 	}
 }
