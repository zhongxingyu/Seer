 /* ValaCodeScanner.java
  *
  * Copyright (C) 2007  Johann Prieur <johann.prieur@gmail.com>
  * Copyright (C) 2011  Marco Trevisan (Treviño) <mail@3v1n0.net>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  */
 package valable.editors.vala;
 
 import java.util.ArrayList;
 
 import org.eclipse.jface.text.TextAttribute;
 import org.eclipse.jface.text.rules.EndOfLineRule;
 import org.eclipse.jface.text.rules.IRule;
 import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
 import org.eclipse.jface.text.rules.NumberRule;
 import org.eclipse.jface.text.rules.RuleBasedScanner;
 import org.eclipse.jface.text.rules.SingleLineRule;
 import org.eclipse.jface.text.rules.Token;
 import org.eclipse.jface.text.rules.WhitespaceRule;
 import org.eclipse.jface.text.rules.WordRule;
 import org.eclipse.swt.SWT;
 
 import valable.editors.util.ColorManager;
 import valable.editors.util.WhitespaceDetector;
 
 public class ValaCodeScanner extends RuleBasedScanner 
 	implements IValaLanguageWords {
 
 	public ValaCodeScanner(ColorManager manager) {
 		IToken keyword = new Token(new TextAttribute(manager.getColor(ColorManager.KEYWORD), 
 													 null, SWT.BOLD));
 		IToken type = new Token(new TextAttribute(manager.getColor(ColorManager.TYPE)));
 		IToken number = new Token(new TextAttribute(manager.getColor(ColorManager.NUMBER)));
 		IToken character = new Token(new TextAttribute(manager.getColor(ColorManager.NUMBER)));
 		IToken comment = new Token(new TextAttribute(manager.getColor(ColorManager.COMMENT)));
 		IToken other = new Token(new TextAttribute(manager.getColor(ColorManager.DEFAULT)));
 		
 		setDefaultReturnToken(other);
 		
 		ArrayList<IRule> rules = new ArrayList<IRule>();
 		
 		// Rule for single line comment
 		rules.add(new EndOfLineRule("//", comment));
 
 		// Rule for char
 		rules.add(new SingleLineRule("'", "'", character, '\\'));
 
 		// Rule for numbers
 		rules.add(new NumberRule(number));
 		
 		// Rule for whitespaces
 		rules.add(new WhitespaceRule(new WhitespaceDetector()));
 		
 		// Rule for keywords, types and constants
 		WordRule wordRule = new ValaWordRule(new ValaWordDetector(), other);
 		for(String kw : keywords)
 			wordRule.addWord(kw, keyword);
 		for(String t : types)
 			wordRule.addWord(t, type);
 		for(String c : constants)
 			wordRule.addWord(c, type);
 		rules.add(wordRule);
 
 		IRule[] r = new IRule[rules.size()];
 		rules.toArray(r);
 		setRules(r);
 	}
 }
