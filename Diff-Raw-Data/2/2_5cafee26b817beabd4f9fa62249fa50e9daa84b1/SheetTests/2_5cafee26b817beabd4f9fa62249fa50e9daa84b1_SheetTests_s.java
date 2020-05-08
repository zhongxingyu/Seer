 package com.sutemi;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
public class PartOneSheetTest {
 
 	@Test
 	public void testThatCellsAreEmptyByDefault() {
 		Sheet sheet = new Sheet();
 		assertEquals("",sheet.get("A1"));
 		assertEquals("",sheet.get("ZX347"));
 	}
 
 	@Test
 	public void testThatTextCellsAreStored() {
 		Sheet sheet = new Sheet();
 		String theCell = "A21";
 		
 		sheet.put(theCell, "A String");
 		assertEquals("A String", sheet.get(theCell));
 	}
 	
 	@Test
 	public void testThatManyCellsExist() {
 		Sheet sheet = new Sheet();
 		sheet.put("A1", "First");
 		sheet.put("X27", "Second");
 		sheet.put("ZX901", "Third");
 		
 		assertEquals("A1", "First", sheet.get("A1"));
 		assertEquals("X27", "Second", sheet.get("X27"));
 		assertEquals("ZX901", "Third", sheet.get("ZX901"));
 		
 		sheet.put("A1", "Fourth");
 		assertEquals("A1 after", "Fourth", sheet.get("A1"));
 		assertEquals("X27", "Second", sheet.get("X27"));
 		assertEquals("ZX901", "Third", sheet.get("ZX901"));
 	}
 	
 	@Test
 	public void testThatNumericCellsAreIdentifiedAndStored() {
 		Sheet sheet = new Sheet();
 		String theCell = "A21";
 		
 		sheet.put(theCell, "X99"); // obvious string
 		assertEquals("X99", sheet.get(theCell));
 		
 		sheet.put(theCell, "14"); // obvious number
 		assertEquals("14", sheet.get(theCell));
 		
 		sheet.put(theCell, " 99 X"); // whole string must be numeric
 		assertEquals(" 99 X", sheet.get(theCell));
 		
 		sheet.put(theCell, " 1234 "); // blanks ignored
 		assertEquals("1234", sheet.get(theCell));
 		
 		sheet.put(theCell, " "); // just a blank
 		assertEquals(" ", sheet.get(theCell));
 	}
 	
 	@Test
 	public void testThatWeHaveAccessToCellLiteralValuesForEditing() {
 		Sheet sheet = new Sheet();
 		String theCell = "A21";
 		
 		sheet.put(theCell, "Some string");
 		assertEquals("Some string", sheet.getLiteral(theCell));
 		
 		sheet.put(theCell, " 1234 ");
 		assertEquals(" 1234 ", sheet.getLiteral(theCell));
 		
 		sheet.put(theCell, "=7"); // foreshadowing formulas
 		assertEquals("=7", sheet.getLiteral(theCell));
 	}
 }
