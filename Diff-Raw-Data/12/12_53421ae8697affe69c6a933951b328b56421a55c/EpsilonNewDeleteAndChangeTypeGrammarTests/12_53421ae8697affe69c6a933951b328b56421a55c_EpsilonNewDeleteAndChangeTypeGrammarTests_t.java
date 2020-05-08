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
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 public class EpsilonNewDeleteAndChangeTypeGrammarTests {
 
 	@Test
 	public void newModelElement() {
 		assertEquals(1, countEpsilonStyleAssignmentsIn("a := new Foo!Bar;"));
 	}
 	
 	@Test
 	public void newModelElementWithoutAssignment() {
 		assertEquals(1, countEpsilonStyleAssignmentsIn("new Foo!Bar;"));
 	}
 	
 	@Test
	public void newInVariableName() {
		assertEquals(0, countEpsilonStyleAssignmentsIn("newsgroup := 'org.bar';"));
	}
	
	@Test
 	public void deleteModelElement() {
 		assertEquals(1, countEpsilonStyleAssignmentsIn("delete Element;"));
 	}
 	
 	@Test
 	public void flockToPart() {
 		assertEquals(1, countEpsilonStyleAssignmentsIn("migrate Foo to Bar"));
 	}
 	
 	@Test
 	public void assignment() {
 		assertEquals(0, countEpsilonStyleAssignmentsIn("a := b;"));
 	}
 	
 	@Test
 	public void combined() {
 		assertEquals(3, countEpsilonStyleAssignmentsIn("migrate Foo to Bar { a := new Baz; delete Bar.all.first; }"));
 	}
 	
 	private static int countEpsilonStyleAssignmentsIn(String text) {
 		return EpsilonGrammar.getInstance().countNewDeleteAndChangeTypeOperationsIn(text);
 	}
 }
