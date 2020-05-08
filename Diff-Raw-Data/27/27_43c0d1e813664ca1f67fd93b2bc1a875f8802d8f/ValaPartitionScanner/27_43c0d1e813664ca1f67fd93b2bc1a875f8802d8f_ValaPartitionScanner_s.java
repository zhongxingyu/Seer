 /* ValaPartitionScanner.java
  *
  * Copyright (C) 2007  Johann Prieur <johann.prieur@gmail.com>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  */
 package valable.editors.vala;
 
 import java.util.ArrayList;
 
 import org.eclipse.jface.text.rules.IPredicateRule;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.MultiLineRule;
 import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
 import org.eclipse.jface.text.rules.Token;
 
 public class ValaPartitionScanner extends RuleBasedPartitionScanner {
 	
 	public final static String VALA_MULTILINE_COMMENT = "__vala_multiline_comment";
 	public final static String GTKDOC_COMMENT = "__gtkdoc_comment";
 	public final static String VALA_MULTILINE_STRING = "__vala_multiline_string";
 	public final static String VALA_VERBATIM_STRING = "__vala_verbatim_string";
 
 	public ValaPartitionScanner() {
 		IToken valaMultilineComment = new Token(VALA_MULTILINE_COMMENT);
 		IToken gtkdocComment = new Token(GTKDOC_COMMENT);
 		IToken valaMultilineString = new Token(VALA_MULTILINE_STRING);
 		IToken valaVerbatimString = new Token(VALA_VERBATIM_STRING);
 		
 		ArrayList<IPredicateRule> rules = new ArrayList<IPredicateRule>();
 		
 		// Rule for gtk-doc comments
 		rules.add(new MultiLineRule("/**", "*/", gtkdocComment));
 		
 		// Rule for multi line comments
 		rules.add(new MultiLineRule("/*", "*/", valaMultilineComment));
 
 		// Rule for verbatim strings
		rules.add(new MultiLineRule("\"\"\"", "\"\"\"", valaVerbatimString));
 
 		// Rule for multi line strings
		rules.add(new MultiLineRule("\"", "\"", valaMultilineString, '\\'));
 		rules.add(new MultiLineRule("@\"", "\"", valaMultilineString, '\\'));
 		
 		IPredicateRule[] r = new IPredicateRule[rules.size()];
 		rules.toArray(r);
 		setPredicateRules(r);
 	}
 }
