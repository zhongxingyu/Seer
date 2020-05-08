 package edu.ucsb.cs56.S13.lab04.shanencross;
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertFalse;
 
 /** test class for PersonInLine
 
 @author Shanen Cross
 @version CS56, S13, lab04
 @see PersonInLine
 
 */
 
 
 public class PersonInLineTest {
     
 
     @Test public void testNoArgConstructor() {
         PersonInLine linePerson = new PersonInLine();
         assertEquals("", linePerson.getName());
         assertEquals(1, linePerson.getNumberInLine());
     }
     
     @Test public void testStringAndIntConstructor() {
         PersonInLine linePerson = new PersonInLine("Bob Miller", 8);
         assertEquals("Bob Miller", linePerson.getName());
        assertEquals("8", linePerson.getNumberInLine());
     }
 
     @Test public void testGetName() {
 	PersonInLine linePerson = new PersonInLine("Bob Miller", 8);
 	assertEquals("Bob Miller", linePerson.getName());
     }  
 
     @Test public void testGetNumberInLine() {
 	PersonInLine linePerson = new PersonInLine("Bob Miller", 8);
 	assertEquals(8, linePerson.getNumberInLine());
     }
 
     @Test public void testSetName() {
  	PersonInLine linePerson = new PersonInLine();
 	linePerson.setName("Bob Miller");
 	assertEquals("Bob Miller", linePerson.getName());
     }  
 
     @Test public void testSetNumberInLine() {
 	PersonInLine linePerson = new PersonInLine();
 	linePerson.setNumberInLine(8);
 	assertEquals(8, linePerson.getNumberInLine());
     }  
 
     @Test public void testEquals1() {
 	PersonInLine linePerson = new PersonInLine("Bob Miller", 8);
 	PersonInLine otherLinePerson = new PersonInLine("Bob Miller", 8);
 	assertTrue(linePerson.equals(otherLinePerson));
     }  
 
     @Test public void testEquals2() {
 	PersonInLine linePerson = new PersonInLine("Bob Miller", 8);
 	PersonInLine otherLinePerson = new PersonInLine("Frank Miller", 8);
 	assertFalse(linePerson.equals(otherLinePerson));
     }  
 
     @Test public void testEquals3() {
 	PersonInLine linePerson = new PersonInLine("Bob Miller", 8);
 	PersonInLine otherLinePerson = new PersonInLine("Bob Miller", 3);
 	assertFalse(linePerson.equals(otherLinePerson));
     }  
 
     @Test public void testEquals4() {
 	PersonInLine linePerson = new PersonInLine("Bob Miller", 8);
 	PersonInLine otherLinePerson = new PersonInLine("Frank Miller", 3);
 	assertFalse(linePerson.equals(otherLinePerson));
     }  
 
     @Test public void testEquals5() {
 	PersonInLine linePerson = new PersonInLine("Bob Miller", 8);
 	String notPerson = "Hello World";
 	assertFalse(linePerson.equals(notPerson));
     }  
 
     @Test public void testToString() {
 	PersonInLine linePerson = new PersonInLine("Bob Miller", 8);
 	String expected = "Bob Miller, 8th in line";
 	assertEquals(expected, linePerson.toString());
     }
 
 }
