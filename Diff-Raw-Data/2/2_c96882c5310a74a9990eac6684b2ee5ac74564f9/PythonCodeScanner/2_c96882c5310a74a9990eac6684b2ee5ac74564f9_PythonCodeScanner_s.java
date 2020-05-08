 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.python.internal.ui.text;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.dltk.python.internal.ui.rules.PythonFloatNumberRule;
 import org.eclipse.dltk.ui.text.AbstractScriptScanner;
 import org.eclipse.dltk.ui.text.IColorManager;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.rules.EndOfLineRule;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.WhitespaceRule;
 import org.eclipse.jface.text.rules.WordRule;
 
 public class PythonCodeScanner extends AbstractScriptScanner {
 	private static String[] fgKeywords = {
 			"and", "del", "for", "is", "raise", "assert", "elif", "from", "lambda", "break", "else", "global", "not", "try", "class",
			"except", "if", "or", "while", "continue", "exec", "import", "pass", "yield", "def", "finally", "in", "print", "self"
 	};
 	private static String fgReturnKeyword = "return";
 	private static String fgTokenProperties[] = new String[] {
 			PythonColorConstants.PYTHON_SINGLE_LINE_COMMENT, PythonColorConstants.PYTHON_DEFAULT, PythonColorConstants.PYTHON_KEYWORD,
 			PythonColorConstants.PYTHON_KEYWORD_RETURN, PythonColorConstants.PYTHON_NUMBER, PythonColorConstants.PYTHON_CLASS_DEFINITION,
 			PythonColorConstants.PYTHON_FUNCTION_DEFINITION, PythonColorConstants.PYTHON_DECORATOR
 	};
 
 	public PythonCodeScanner(IColorManager manager, IPreferenceStore store) {
 		super(manager, store);
 		initialize();
 	}
 
 	protected String[] getTokenProperties() {
 		return fgTokenProperties;
 	}
 
 	protected List createRules() {
 		List/* <IRule> */rules = new ArrayList/* <IRule> */();
 		IToken keyword = getToken(PythonColorConstants.PYTHON_KEYWORD);
 		IToken keywordReturn = getToken(PythonColorConstants.PYTHON_KEYWORD_RETURN);
 		IToken comment = getToken(PythonColorConstants.PYTHON_SINGLE_LINE_COMMENT);
 		IToken other = getToken(PythonColorConstants.PYTHON_DEFAULT);
 		IToken cls = getToken(PythonColorConstants.PYTHON_CLASS_DEFINITION);
 		IToken def = getToken(PythonColorConstants.PYTHON_FUNCTION_DEFINITION);
 		IToken number = getToken(PythonColorConstants.PYTHON_NUMBER);
 		IToken decorator = getToken(PythonColorConstants.PYTHON_DECORATOR);
 		// Add rule for single line comments.
 		rules.add(new EndOfLineRule("#", comment));
 		// Add generic whitespace rule.
 		rules.add(new WhitespaceRule(new PythonWhitespaceDetector()));
 		// Add word rule for keywords, types, and constants.
 		PythonWordRule wordRule = new PythonWordRule(new PythonWordDetector(), other, cls, def);
 		for (int i = 0; i < fgKeywords.length; i++) {
 			wordRule.addWord(fgKeywords[i], keyword);
 		}
 		wordRule.addWord(fgReturnKeyword, keywordReturn);
 		rules.add(wordRule);
 		rules.add(new WordRule(new PythonDecoratorDetector(), decorator));
 		rules.add(new PythonFloatNumberRule(number));
 		setDefaultReturnToken(other);
 		return rules;
 	}
 }
