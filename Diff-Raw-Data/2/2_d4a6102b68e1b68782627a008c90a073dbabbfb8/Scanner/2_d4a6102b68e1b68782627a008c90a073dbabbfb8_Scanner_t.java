 // Copyright (c) 2011, David J. Pearce (djp@ecs.vuw.ac.nz)
 // Copyright (c) 2011, Lee Trezise(Lee.Trezise@gmail.com)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //    * Redistributions of source code must retain the above copyright
 //      notice, this list of conditions and the following disclaimer.
 //    * Redistributions in binary form must reproduce the above copyright
 //      notice, this list of conditions and the following disclaimer in the
 //      documentation and/or other materials provided with the distribution.
 //    * Neither the name of the <organization> nor the
 //      names of its contributors may be used to endorse or promote products
 //      derived from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL DAVID J. PEARCE BE LIABLE FOR ANY
 // DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package wyclipse.ui.editor;
 
 import org.eclipse.jface.text.TextAttribute;
 import org.eclipse.jface.text.rules.EndOfLineRule;
 import org.eclipse.jface.text.rules.IRule;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.IWordDetector;
 import org.eclipse.jface.text.rules.MultiLineRule;
 import org.eclipse.jface.text.rules.RuleBasedScanner;
 import org.eclipse.jface.text.rules.SingleLineRule;
 import org.eclipse.jface.text.rules.Token;
 import org.eclipse.jface.text.rules.WhitespaceRule;
 import org.eclipse.jface.text.rules.WordRule;
 import org.eclipse.swt.SWT;
 
 import wyc.io.WhileyFileLexer;
 
 public class Scanner extends RuleBasedScanner {
 
 	private String[] KEYWORDS = {
 			"all",
 			"any",
 			"assert",
 			"assume",
 			"bool",
 			"break",
 			"byte",
 			"catch",
 			"case",
 			"catch",
 			"char",
 			"continue",
 			"constant",
 			"debug",
 			"default",
 			"do",
 			"else",
 			"ensures",
 			"export",
 			"false",
 			"finite",
 			"for",
 			"function",
 			"from",
 			"if",
 			"import",
 			"in",
 			"is",
 			"method",
 			"native",
 			"new",
 			"no",
 			"null",
 			"package",
 			"private",
 			"protected",
 			"public",
 			"real",	
 			"requires",
 			"return",
 			"skip",
 			"some",
 			"string",
 			"switch",
 			"total",
 			"throw",
 			"throws",
 			"true",
 			"try",
 			"type",
 			"void",
 			"where",
 			"while"	
 	};
 	
 	public Scanner() {
 		IToken keyword = new Token(new TextAttribute(
 				ColorManager.KEYWORD_COLOR_C, null, SWT.BOLD));
 		IToken specKeyword = new Token(new TextAttribute(
 				ColorManager.SPEC_KEYWORD_COLOR_C, null, SWT.BOLD));
 		IToken comment = new Token(new TextAttribute(
 				ColorManager.COMMENT_COLOR_C));
 		IToken string = new Token(
 				new TextAttribute(ColorManager.STRING_COLOR_C));
 		
 		WordRule keywordRule = new WordRule(new KeywordDetector());
 		WordRule specKeywordRule = new WordRule(new KeywordDetector());
 				
		for (String s : KEYWORDS) {
 			if(s.equals("requires") || s.equals("ensures") || s.equals("where")) {
 				specKeywordRule.addWord(s, specKeyword);
 			} else {
 				keywordRule.addWord(s, keyword);
 			}
 		}
 		keywordRule.addWord("from", keyword); // HACK. TODO FIX
 		
 		IRule[] rules = new IRule[7];
 		rules[0] = new SingleLineRule("\"", "\"", string, '\\');
 		// Add a rule for single quotes
 		rules[1] = new SingleLineRule("'", "'", string, '\\');
 		// Add generic whitespace rule.
 		rules[2] = new WhitespaceRule(new WhitespaceDetector());
 
 		rules[3] = specKeywordRule;
 		rules[4] = keywordRule;
 		rules[5] = new EndOfLineRule("//", comment);
 		rules[6] = new MultiLineRule("/*", "", comment, (char) 0, true);
 		setRules(rules);
 	}
 
 	private static final class KeywordDetector implements IWordDetector {
 		@Override
 		public boolean isWordStart(char c) {
 			return Character.isJavaIdentifierStart(c);
 		}
 
 		@Override
 		public boolean isWordPart(char c) {
 			return Character.isJavaIdentifierPart(c);
 		}	
 	}
 }
