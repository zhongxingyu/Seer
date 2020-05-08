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
 
 package org.seasr.meandre.components.transform.text;
 
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
 
 @Component(
         creator = "Lily Dong",
         description = "Removes characters or replaces characters with replacement.",
         name = "Text Cleaner",
         tags = "text, remove, replace",
         rights = Licenses.UofINCSA,
         baseURL = "meandre://seasr.org/components/foundry/",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class TextCleaner extends AbstractExecutableComponent{
 
 	//------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             description = "The text to clean or replace" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object",
             name = Names.PORT_TEXT
     )
     protected static final String IN_TEXT = Names.PORT_TEXT;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             description = "The cleaned or replaced text" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings",
             name = Names.PORT_TEXT
     )
     protected static final String OUT_TEXT = Names.PORT_TEXT;
 
     //------------------------------ PROPERTIES --------------------------------------------------
     @ComponentProperty(
 	        description = "The regular expression to clean out the matched substring. " +
 	        "For example, if specifying the regular expression as [~|||^]+, " +
 	        "~, |, ^ are removed.",
             name = Names.PROP_REMOVE,
             defaultValue = ""
 	)
 	protected static final String PROP_REMOVE = Names.PROP_REMOVE;
 
 	@ComponentProperty(
 	        description = "The regular expression to find the matched substring. " +
 	        "For example, if specifying the regular expression as push and " +
 	        "the replacement as pushing, push is substituted with pushing",
             name = Names.PROP_FIND,
             defaultValue = ""
 	)
 	protected static final String PROP_FIND = Names.PROP_FIND;
 
 	@ComponentProperty(
 	        description = "The replacement to substitute the matched substring found by find.",
             name = Names.PROP_REPLACE,
             defaultValue = ""
 	)
 	protected static final String PROP_REPLACE = Names.PROP_REPLACE;
 
 	@ComponentProperty(
 	        description = "The regular expression to find the matched substring.",
             name = Names.PROP_FIND_2,
             defaultValue = ""
 	)
 	protected static final String PROP_FIND_2 = Names.PROP_FIND_2;
 
 	@ComponentProperty(
 	        description = "The replacement to substitute the matched substring found by find2.",
             name = Names.PROP_REPLACE_2,
             defaultValue = ""
 	)
 	protected static final String PROP_REPLACE_2 = Names.PROP_REPLACE_2;
 
 	@ComponentProperty(
 	        description = "The regular expression to find the matched substring.",
             name = Names.PROP_FIND_3,
             defaultValue = ""
 	)
 	protected static final String PROP_FIND_3 = Names.PROP_FIND_3;
 
 	@ComponentProperty(
 	        description = "The replacement to substitute the matched substring found by find3.",
             name = Names.PROP_REPLACE_3,
             defaultValue = ""
 	)
 	protected static final String PROP_REPLACE_3 = Names.PROP_REPLACE_3;
 
 	@ComponentProperty(
 	        description = "The regular expression to find the matched substring.",
             name = Names.PROP_FIND_4,
             defaultValue = ""
 	)
 	protected static final String PROP_FIND_4 = Names.PROP_FIND_4;
 
 	@ComponentProperty(
 	        description = "The replacement to substitute the matched substring found by find4.",
             name = Names.PROP_REPLACE_4,
             defaultValue = ""
 	)
 	protected static final String PROP_REPLACE_4 = Names.PROP_REPLACE_4;
 
 	//--------------------------------------------------------------------------------------------
 
 	private static final int NUM = 4; //the number of find-replace pairs
 	private static final String FIND = "find";
 	private static final String REPLACE = "replace";
 
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 		String text = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))[0];
 
 		Pattern pattern;
 		Matcher matcher;
 
 		String regex = cc.getProperty(PROP_REMOVE).trim();
 		if(regex!=null && regex.trim().length()!=0) {
 			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
 			matcher = pattern.matcher(text);
 			text = matcher.replaceAll("");
 		}
 
 		for(int i=1; i<=NUM; i++) {
			String propertyName = FIND;
			propertyName = (i==1)? propertyName: propertyName+"_"+Integer.toString(i);
 			regex = cc.getProperty(propertyName);
 
			if(regex!=null && regex.trim().length()!=0) {
				propertyName = REPLACE;
				propertyName = (i==1)? propertyName: propertyName+"_"+Integer.toString(i);

 				String replacement = cc.getProperty(propertyName);

				if(replacement!=null && replacement.trim().length()!=0) {
					pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
					matcher = pattern.matcher(text);
 					text = matcher.replaceAll(replacement);
				}
 			}
 		}
 
 		text = text.replaceAll("[\\s]+", " ");
 
 		cc.pushDataComponentToOutput(
 				OUT_TEXT, BasicDataTypesTools.stringToStrings(text));
 	}
 
 	@Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
 	}
 }
