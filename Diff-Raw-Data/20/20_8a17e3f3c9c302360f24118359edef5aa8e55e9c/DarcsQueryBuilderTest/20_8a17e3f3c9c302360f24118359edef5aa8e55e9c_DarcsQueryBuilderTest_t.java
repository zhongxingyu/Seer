 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  */
 package org.jenkinsci.plugins.darcs.browsers;
 
 import junit.framework.TestCase;
 
 /**
  *
  * @author Sven Strittmatter <ich@weltraumschaf.de>
  */
 public class DarcsQueryBuilderTest extends TestCase {
 
     public DarcsQueryBuilderTest(String testName) {
         super(testName);
     }
 
     public void testEmptyQuery() {
         DarcsQueryBuilder sut = new DarcsQueryBuilder(DarcsQueryBuilder.SeparatorType.SLASHES);
         assertEquals("Test the seperator type", DarcsQueryBuilder.SeparatorType.SLASHES, sut.getType());
         assertEquals("Test empty query string.", "", sut.toString());
     }
 
     public void testAddToSlashedQuery() {
         DarcsQueryBuilder sut = new DarcsQueryBuilder(DarcsQueryBuilder.SeparatorType.SLASHES);
         assertEquals("Test the seperator type", DarcsQueryBuilder.SeparatorType.SLASHES, sut.getType());
         sut.add("foo").add("bar").add("baz");
        assertEquals("Test a slashed query.", "/foo/bar/baz", sut.toString());
     }
 
     public void testAddToSemicolonedQuery() {
         DarcsQueryBuilder sut;
 
         sut = new DarcsQueryBuilder(DarcsQueryBuilder.SeparatorType.SEMICOLONS);
         assertEquals("Test the seperator type", DarcsQueryBuilder.SeparatorType.SEMICOLONS, sut.getType());
         sut.add("foo").add("bar").add("baz");
         assertEquals("Test a semicoloned query.", "?foo;bar;baz", sut.toString());
 
         sut = new DarcsQueryBuilder(DarcsQueryBuilder.SeparatorType.SEMICOLONS);
         assertEquals("Test the seperator type", DarcsQueryBuilder.SeparatorType.SEMICOLONS, sut.getType());
         sut.add("foo=1").add("bar=2").add("baz=3");
         assertEquals("Test a semicoloned query.", "?foo=1;bar=2;baz=3", sut.toString());
     }
 
     public void testAddToAmpersandedQuery() {
         DarcsQueryBuilder sut;
 
         sut = new DarcsQueryBuilder(DarcsQueryBuilder.SeparatorType.AMPERSANDS);
         assertEquals("Test the seperator type", DarcsQueryBuilder.SeparatorType.AMPERSANDS, sut.getType());
         sut.add("foo").add("bar").add("baz");
         assertEquals("Test a ampersanded query.", "?foo&bar&baz", sut.toString());
 
         sut = new DarcsQueryBuilder(DarcsQueryBuilder.SeparatorType.AMPERSANDS);
         assertEquals("Test the seperator type", DarcsQueryBuilder.SeparatorType.AMPERSANDS, sut.getType());
         sut.add("foo=1").add("bar=2").add("baz=3");
         assertEquals("Test a ampersanded query.", "?foo=1&bar=2&baz=3", sut.toString());
     }
 }
