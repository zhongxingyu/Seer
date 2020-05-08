 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.steps.wordcount;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.steps.tokenization.Tokenizer;
 import net.sf.okapi.steps.tokenization.tokens.Tokens;
 import net.sf.okapi.steps.wordcount.common.BaseCounter;
 import net.sf.okapi.steps.wordcount.common.StructureParameters;
 
 /**
  * Word Count engine. Contains static methods to calculate number of words in a given text fragment. 
  * 
  * @version 0.1 07.07.2009
  */
 
 public class WordCounter extends BaseCounter {
 
 	private static StructureParameters params;
 	
 	protected static void loadParameters() {
 		
 		if (params != null) return; // Already loaded
 		
 		params = new StructureParameters();
 		if (params == null) return;
 		
		params.loadFromResource("word_counter.tprm");
 	}
 	
 	@Override
 	protected long doGetCount(String text, LocaleId language) {
 		
 		Tokens tokens = Tokenizer.tokenize(text, language, getTokenName());		
 		if (tokens == null) return 0;
 		
 		// DEBUG
 //		System.out.println(String.format("Tokens: %d (%s)", tokens.size(), text));
 //		System.out.println();
 //		System.out.println(tokens.toString());
 //		System.out.println();
 		
 		return tokens.size();
 	}
 	
 	public static long getCount(TextUnit textUnit, LocaleId language) {
 		return getCount(WordCounter.class, textUnit, language);		
 	}
 	
 	public static long getCount(TextContainer textContainer, LocaleId language) {
 		return getCount(WordCounter.class, textContainer, language);		
 	}
 
 	public static long getCount(TextFragment textFragment, LocaleId language) {
 		return getCount(WordCounter.class, textFragment, language);		
 	}
 	
 	public static long getCount(String string, LocaleId language) {
 		return getCount(WordCounter.class, string, language);		
 	}
 	
 	public static String getTokenName() {
 		
 		loadParameters();
 		
 		if (params == null) return "";
 		return params.getTokenName();
 	}
 
 }
