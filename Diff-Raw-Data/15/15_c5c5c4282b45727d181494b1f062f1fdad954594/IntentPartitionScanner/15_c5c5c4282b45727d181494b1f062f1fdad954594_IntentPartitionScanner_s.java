 /*******************************************************************************
  * Copyright (c) 2008, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.editor.scanner;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.rules.IPredicateRule;
 import org.eclipse.jface.text.rules.IRule;
 import org.eclipse.jface.text.rules.MultiLineRule;
 import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
 import org.eclipse.jface.text.rules.Token;
 import org.eclipse.mylyn.docs.intent.parser.IntentKeyWords;
 
 /**
  * Global partition scanner that exclusively uses predicate rules.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class IntentPartitionScanner extends RuleBasedPartitionScanner {
 
 	/**
 	 * Represents an Intent structural content (like section or chapter).
 	 */
 	public static final String INTENT_STRUCTURAL_CONTENT = "__Intent__structuralcontent";
 
 	/**
 	 * Represents an Intent Modeling Unit (from '@M' to 'M@').
 	 */
 	public static final String INTENT_MODELINGUNIT = "__Intent__modelingunit";
 
 	/**
 	 * Represents an Intent Description Unit.
 	 */
 	public static final String INTENT_DESCRIPTIONUNIT = IDocument.DEFAULT_CONTENT_TYPE;
 
 	/**
 	 * Represents all the content types handled by this partitioner.
 	 */
 	public static final String[] LEGAL_CONTENT_TYPES = new String[] {INTENT_STRUCTURAL_CONTENT,
 			INTENT_MODELINGUNIT, INTENT_DESCRIPTIONUNIT,
 	};
 
 	/**
 	 * Constructor.
 	 */
 	public IntentPartitionScanner() {
 		List<IRule> rules = new ArrayList<IRule>();
 
 		computeModelingUnitRules(rules);
 		computeStructuralContentRules(rules);
 
 		setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
 	}
 
 	private void computeModelingUnitRules(List<IRule> rules) {
 		rules.add(new MultiLineRule(IntentKeyWords.MODELING_UNIT_BEGIN, IntentKeyWords.MODELING_UNIT_END,
 				new Token(INTENT_MODELINGUNIT)));
 	}
 
 	private void computeStructuralContentRules(List<IRule> rules) {
		// TODO redefine pattern rule to disable recognition of pattern start only
		rules.add(new SingleLineRule(IntentKeyWords.INTENT_KEYWORD_DOCUMENT
				+ IntentKeyWords.INTENT_WHITESPACE, IntentKeyWords.INTENT_KEYWORD_OPEN, new Token(
				INTENT_STRUCTURAL_CONTENT)));
		rules.add(new SingleLineRule(
				IntentKeyWords.INTENT_KEYWORD_CHAPTER + IntentKeyWords.INTENT_WHITESPACE,
 				IntentKeyWords.INTENT_KEYWORD_OPEN, new Token(INTENT_STRUCTURAL_CONTENT)));
		rules.add(new SingleLineRule(
				IntentKeyWords.INTENT_KEYWORD_SECTION + IntentKeyWords.INTENT_WHITESPACE,
 				IntentKeyWords.INTENT_KEYWORD_OPEN, new Token(INTENT_STRUCTURAL_CONTENT)));
 	}
 }
