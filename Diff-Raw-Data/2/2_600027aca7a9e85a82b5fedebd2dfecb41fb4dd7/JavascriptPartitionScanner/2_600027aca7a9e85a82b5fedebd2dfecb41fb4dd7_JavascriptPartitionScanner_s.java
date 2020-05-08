 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.ui.text;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.dltk.javascript.ui.text.IJavaScriptPartitions;
 import org.eclipse.jface.text.rules.EndOfLineRule;
 import org.eclipse.jface.text.rules.IPredicateRule;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.MultiLineRule;
 import org.eclipse.jface.text.rules.PatternRule;
 import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
 import org.eclipse.jface.text.rules.SingleLineRule;
 import org.eclipse.jface.text.rules.Token;
 
 public class JavascriptPartitionScanner extends RuleBasedPartitionScanner {
 
 	/**
 	 * Creates the partitioner and sets up the appropriate rules.
 	 */
 	public JavascriptPartitionScanner() {
 		super();
 
 		IToken string = new Token(IJavaScriptPartitions.JS_STRING);
 		IToken comment = new Token(IJavaScriptPartitions.JS_COMMENT);
 		IToken doc = new Token(IJavaScriptPartitions.JS_DOC);
 		IToken regexp = new Token(IJavaScriptPartitions.JS_REGEXP);
 
 		List/* < IPredicateRule > */rules = new ArrayList/* <IPredicateRule> */();
 		rules.add(new MultiLineRule("/**", "*/", doc)); //$NON-NLS-1$ //$NON-NLS-2$
 		rules.add(new MultiLineRule("/*", "*/", comment)); //$NON-NLS-1$ //$NON-NLS-2$
 		rules.add(new EndOfLineRule("//", comment)); //$NON-NLS-1$		
 
 		// simple regexp tester. Coloring is still default so it doesn't matter
 		// for now to much that var x = 10 / 5 / 10 is also seen as regexp
		rules.add(new PatternRule("/", "/", regexp, '\\', false, false));
 		// Add rule for character constants.
 		rules.add(new SingleLineRule("'", "'", string, '\\'));
 		rules.add(new MultiLineRule("\"", "\"", string, '\\'));
 
 		IPredicateRule[] result = new IPredicateRule[rules.size()];
 		rules.toArray(result);
 		setPredicateRules(result);
 	}
 }
