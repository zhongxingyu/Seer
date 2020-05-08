 package de.hszg.atocc.core.util.test;
 
 import de.hszg.atocc.core.util.grammar.Grammar;
 
 import java.util.Arrays;
 import java.util.Collections;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public final class GrammarTests {
 
     private static final String A = "A";
     private static final String B = "B";
 
     private static final String TERMINAL_A = "a";
     private static final String TERMINAL_B = "b";
     private static final String TERMINAL_C = "c";
     
     private static final String TERMINAL_A1 = "a1";
     private static final String TERMINAL_A2 = "a2";
     
     @Test
     public void testAppendRule1() {
         final Grammar grammar = new Grammar();
         Assert.assertEquals(0, grammar.getRules().size());
 
         grammar.appendRule(A, TERMINAL_A);
         Assert.assertEquals(1, grammar.getRules().size());
 
         grammar.appendRule(A, TERMINAL_B);
         Assert.assertEquals(1, grammar.getRules().size());
 
         grammar.appendRule(B, TERMINAL_C);
         Assert.assertEquals(2, grammar.getRules().size());
     }
 
     @Test
     public void testAppendRule2() {
         final Grammar grammar = new Grammar();
         Assert.assertEquals(0, grammar.getRules().size());
 
         grammar.appendRule(A, Arrays.asList(TERMINAL_A, TERMINAL_B));
         Assert.assertEquals(1, grammar.getRules().size());
     }
 
     @Test
     public void testGetLeftHandSides() {
         final Grammar grammar = new Grammar();
         Assert.assertEquals(Collections.EMPTY_LIST, grammar.getLeftHandSides());
 
         grammar.appendRule(A, TERMINAL_A);
         Assert.assertEquals(Arrays.asList(A), grammar.getLeftHandSides());
 
         grammar.appendRule(A, TERMINAL_B);
         Assert.assertEquals(Arrays.asList(A), grammar.getLeftHandSides());
 
         grammar.appendRule(B, TERMINAL_C);
         Assert.assertEquals(Arrays.asList(A, B), grammar.getLeftHandSides());
     }
 
     @Test
     public void testContainsLeftHandSide() {
         final Grammar grammar = new Grammar();
         Assert.assertFalse(grammar.containsLeftHandSide(A));
         Assert.assertFalse(grammar.containsLeftHandSide(B));
 
         grammar.appendRule(A, TERMINAL_A);
         Assert.assertTrue(grammar.containsLeftHandSide(A));
         Assert.assertFalse(grammar.containsLeftHandSide(B));
 
         grammar.appendRule(B, TERMINAL_B);
         Assert.assertTrue(grammar.containsLeftHandSide(A));
         Assert.assertTrue(grammar.containsLeftHandSide(B));
     }
 
     @Test
     public void testRemoveRightHandSide1() {
         final Grammar grammar = new Grammar();
         grammar.appendRule(A, TERMINAL_A1);
         grammar.appendRule(A, TERMINAL_A2);
         grammar.appendRule(A, TERMINAL_B);
         grammar.appendRule(B, TERMINAL_B);
 
         Assert.assertEquals(Arrays.asList(TERMINAL_A1, TERMINAL_A2, TERMINAL_B),
                 grammar.getRightHandSidesFor(A));
         Assert.assertEquals(Arrays.asList(TERMINAL_B), grammar.getRightHandSidesFor(B));
 
         grammar.removeRightHandSide(TERMINAL_A2);
 
         Assert.assertEquals(Arrays.asList(TERMINAL_A1, TERMINAL_B),
                 grammar.getRightHandSidesFor(A));
         Assert.assertEquals(Arrays.asList(TERMINAL_B), grammar.getRightHandSidesFor(B));
 
         grammar.removeRightHandSide(TERMINAL_B);
         Assert.assertEquals(Arrays.asList(TERMINAL_A1), grammar.getRightHandSidesFor(A));
         Assert.assertFalse(grammar.containsLeftHandSide(B));
     }
     
     @Test
     public void testToString() {
         final Grammar grammar = new Grammar();
         Assert.assertEquals("", grammar.toString());
         
         grammar.appendRule(A, TERMINAL_A);
         Assert.assertEquals("A -> a", grammar.toString());
         
         grammar.appendRule(A, TERMINAL_B);
         Assert.assertEquals("A -> a | b", grammar.toString());
         
         grammar.appendRule(B, TERMINAL_B);
         Assert.assertEquals("A -> a | b\nB -> b", grammar.toString());
     }
     
     @Test
     public void testRemove() {
         final Grammar grammar = new Grammar();
         grammar.appendRule(A, TERMINAL_B);
         grammar.appendRule(B, TERMINAL_B);
         
         Assert.assertEquals(Arrays.asList(A, B), grammar.getLeftHandSides());
         Assert.assertEquals(Arrays.asList(TERMINAL_B), grammar.getRightHandSidesFor(A));
         Assert.assertEquals(Arrays.asList(TERMINAL_B), grammar.getRightHandSidesFor(B));
         
         grammar.remove(B, TERMINAL_B);
         
         Assert.assertEquals(Arrays.asList(TERMINAL_B), grammar.getRightHandSidesFor(A));
         Assert.assertFalse(grammar.containsLeftHandSide(B));
     }
     
 }
