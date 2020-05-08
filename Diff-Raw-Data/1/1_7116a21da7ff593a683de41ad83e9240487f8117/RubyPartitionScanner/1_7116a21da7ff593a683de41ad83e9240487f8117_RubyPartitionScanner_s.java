 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui.text;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.dltk.ruby.core.text.RubyContext;
 import org.eclipse.dltk.ruby.internal.ui.text.syntax.RubyContextUtils;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.rules.EndOfLineRule;
 import org.eclipse.jface.text.rules.IPredicateRule;
 import org.eclipse.jface.text.rules.MultiLineRule;
 import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
 import org.eclipse.jface.text.rules.Token;
 
 public class RubyPartitionScanner extends RuleBasedPartitionScanner {
 
 	private Token string;
 	private Token comment;
 	private Token rubyDoc;
 	private Token defaultToken;
 	
 	/**
 	 * Creates the partitioner and sets up the appropriate rules.
 	 */
 	public RubyPartitionScanner() {
 		super();
 		
 		defaultToken = new Token (IDocument.DEFAULT_CONTENT_TYPE);
 
 		string = new Token(RubyPartitions.RUBY_STRING);
 
 		comment = new Token(RubyPartitions.RUBY_COMMENT);
 
 		rubyDoc = new Token(RubyPartitions.RUBY_DOC);
 
 		List/* < IPredicateRule > */rules = new ArrayList/* <IPredicateRule> */();
 
 		rules.add(new MultiLineRule("=begin", "=end", rubyDoc));
 
 		rules.add(new EndOfLineRule("#", comment));
 
 		rules.add(new MultiLineRule("\'", "\'", string, '\\', true));
 
 		rules.add(new MultiLineRule("\"", "\"", string, '\\', true));
 
 		rules.add(new RubyPercentStringRule(string, false));
 
 		rules.add(new RubySlashRegexpRule(string));
 		
 		rules.add(new RubyGlobalVarRule(defaultToken));
 
 		IPredicateRule[] result = new IPredicateRule[rules.size()];
 		rules.toArray(result);
 		setPredicateRules(result);
 	}
 
 	public int getOffsetForLaterContextLookup() {
 		return fOffset;		
 	}
 
 	public RubyContext getCurrentContext() {
 		return RubyContextUtils.determineContext(fDocument, fOffset, RubyContext.MODE_FULL);
 	}
 
 	public RubyContext getContext(int offset) {
 		return RubyContextUtils.determineContext(fDocument, offset, RubyContext.MODE_FULL);
 	}
 
 	protected Token getToken(String key) {
 		if (RubyPartitions.RUBY_STRING.equals(key))
 			return string;
 		if (RubyPartitions.RUBY_COMMENT.equals(key))
 			return comment;
 		if (RubyPartitions.RUBY_DOC.equals(key))
 			return rubyDoc;
 		return null;
 	}
 }
