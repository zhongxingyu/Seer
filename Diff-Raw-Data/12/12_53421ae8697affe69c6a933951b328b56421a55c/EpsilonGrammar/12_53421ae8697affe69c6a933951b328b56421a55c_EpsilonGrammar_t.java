 /*******************************************************************************
  * Copyright (c) 2009 The University of York.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Louis Rose - initial API and implementation
  ******************************************************************************
  *
  * $Id$
  */
 package grammar;
 
 
 
 public class EpsilonGrammar implements Grammar {
 
 	private static final EpsilonGrammar instance = new EpsilonGrammar();
 	
 	private EpsilonGrammar() {}
 	
 	public static EpsilonGrammar getInstance() {
 		return instance;
 	}
 	
 	
 	private final KeywordBasedMatcher grammar = new KeywordBasedMatcher(":=", "\\.add");
 
 	private int numberOfAssignments;
 	
 	public int countSimpleModelOperationsIn(String text) {
 		numberOfAssignments = grammar.countMatchesIn(text);
 		
 		uncountAssignmentsToNewInstances(text);
 		
 		return numberOfAssignments;
 	}
 	
 	private void uncountAssignmentsToNewInstances(String text) {
 		uncount(RegexUtil.getNumberOfMatchesIn(text, "= new "));
 	}
 	
 	private void uncount(int numberOfAssignmentsToBeUncounted) {
 		numberOfAssignments -= numberOfAssignmentsToBeUncounted;
 	}
 
 	public int countNewDeleteAndChangeTypeOperationsIn(String text) {
		return new KeywordBasedMatcher("new ", "delete", "migrate \\w* to \\w*").countMatchesIn(text);
 	}
 }
