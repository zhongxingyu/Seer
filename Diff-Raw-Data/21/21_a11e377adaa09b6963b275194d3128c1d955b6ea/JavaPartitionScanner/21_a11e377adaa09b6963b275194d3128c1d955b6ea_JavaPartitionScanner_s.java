 /*******************************************************************************
  * Copyright (c) 2000, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package descent.internal.ui.text;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.rules.EndOfLineRule;
 import org.eclipse.jface.text.rules.ICharacterScanner;
 import org.eclipse.jface.text.rules.IPredicateRule;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.IWordDetector;
 import org.eclipse.jface.text.rules.MultiLineRule;
 import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
 import org.eclipse.jface.text.rules.SingleLineRule;
 import org.eclipse.jface.text.rules.Token;
 import org.eclipse.jface.text.rules.WordRule;
 
 import descent.core.JavaCore;
 import descent.ui.text.IJavaPartitions;
 import descent.ui.text.rules.DEscapeRule;
 import descent.ui.text.rules.DStringRule;
 import descent.ui.text.rules.NestedCommentRule;
 
 
 /**
  * This scanner recognizes the JavaDoc comments and Java multi line comments.
  */
 public class JavaPartitionScanner extends RuleBasedPartitionScanner implements IJavaPartitions {
 	
 	private static final String SOURCE_VERSION= JavaCore.COMPILER_SOURCE;
 
 	/**
 	 * Detector for empty comments.
 	 */
 	static class EmptyCommentDetector implements IWordDetector {
 		
 		private final char relevantChar;
 
 		public EmptyCommentDetector(char relevantChar) {
 			this.relevantChar = relevantChar;
 		}
 
 		/*
 		 * @see IWordDetector#isWordStart
 		 */
 		public boolean isWordStart(char c) {
 			return (c == '/');
 		}
 
 		/*
 		 * @see IWordDetector#isWordPart
 		 */
 		public boolean isWordPart(char c) {
 			return (c == relevantChar || c == '/');
 		}
 	}
 
 
 	/**
 	 * Word rule for empty comments.
 	 */
 	static class EmptyCommentRule extends WordRule implements IPredicateRule {
 
 		private IToken fSuccessToken;
 		/**
 		 * Constructor for EmptyCommentRule.
 		 * @param successToken
 		 */
 		public EmptyCommentRule(char relevantChar, IToken successToken) {
 			super(new EmptyCommentDetector(relevantChar));
 			fSuccessToken= successToken;
 			
 			StringBuilder sb = new StringBuilder();
 			sb.append('/');
 			sb.append(relevantChar);
 			sb.append(relevantChar);
 			sb.append('/');
 			
 			addWord(sb.toString(), fSuccessToken); //$NON-NLS-1$
 		}
 
 		/*
 		 * @see IPredicateRule#evaluate(ICharacterScanner, boolean)
 		 */
 		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
 			return evaluate(scanner);
 		}
 
 		/*
 		 * @see IPredicateRule#getSuccessToken()
 		 */
 		public IToken getSuccessToken() {
 			return fSuccessToken;
 		}
 	}	
 
 	/**
 	 * Creates the partitioner and sets up the appropriate rules.
 	 */
 	public JavaPartitionScanner(IPreferenceStore store) {
 		super();
 		
 //		String version= store.getString(SOURCE_VERSION);
 //		boolean isD2 = JavaCore.VERSION_2_x.equals(version);
 
 		// TODO JDT UI update according to D partitions
 		IToken pragma= new Token(JAVA_PRAGMA);
 		IToken string= new Token(JAVA_STRING);
 		IToken character= new Token(JAVA_CHARACTER);
 		IToken javaDoc= new Token(JAVA_DOC);
 		IToken multiLineComment= new Token(JAVA_MULTI_LINE_COMMENT);
 		IToken singleLineComment= new Token(JAVA_SINGLE_LINE_COMMENT);
 		IToken singleLineDocComment= new Token(JAVA_SINGLE_LINE_DOC_COMMENT);
 		IToken multiLinePlusComment= new Token(JAVA_MULTI_LINE_PLUS_COMMENT);
 		IToken multiLinePlusDocComment= new Token(JAVA_MULTI_LINE_PLUS_DOC_COMMENT);
 		
 		List<IPredicateRule> rules = new ArrayList<IPredicateRule>();
 		
 		rules.add(new EndOfLineRule("#", pragma)); //$NON-NLS-1$
 		
 		rules.add(new EmptyCommentRule('+', multiLinePlusComment));
 		rules.add(new NestedCommentRule("/++", "/+", "+/", multiLinePlusDocComment, (char) 0, true)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		rules.add(new NestedCommentRule("/+", "/+", "+/", multiLinePlusComment, (char) 0, true)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		
 		rules.add(new EmptyCommentRule('*', javaDoc));
 		rules.add(new MultiLineRule("/**", "*/", javaDoc, (char) 0, true)); //$NON-NLS-1$ //$NON-NLS-2$
 		rules.add(new MultiLineRule("/*", "*/", multiLineComment, (char) 0, true)); //$NON-NLS-1$ //$NON-NLS-2$
 
 		rules.add(new EndOfLineRule("///", singleLineDocComment)); //$NON-NLS-1$ 
 		rules.add(new EndOfLineRule("//", singleLineComment)); //$NON-NLS-1$
 		
		rules.add(new DStringRule("x\"", "\"", string, (char) 0)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new DStringRule("r\"", "\"", string, (char) 0)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new DStringRule("`", "`", string, (char) 0)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new DStringRule("\"", "\"", string, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new DStringRule("q\"", "\"", string, (char) 0)); //$NON-NLS-1$ //$NON-NLS-2$
 		
 		rules.add(new DEscapeRule(string));
 		
 		rules.add(new SingleLineRule("'", "'", character, '\\', true)); //$NON-NLS-1$ //$NON-NLS-2$
 		
 		setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
 	}
 }
