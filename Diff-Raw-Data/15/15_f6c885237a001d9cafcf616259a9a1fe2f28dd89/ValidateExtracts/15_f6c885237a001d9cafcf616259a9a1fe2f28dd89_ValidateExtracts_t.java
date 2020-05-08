 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2009 Operational Dynamics Consulting, Pty Ltd
  *
  * The code in this file, and the program it is a part of, is made available
  * to you by its authors as open source software: you can redistribute it
  * and/or modify it under the terms of the GNU General Public License version
  * 2 ("GPL") as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
  *
  * You should have received a copy of the GPL along with this program. If not,
  * see http://www.gnu.org/licenses/. The authors of this program may be
  * contacted through http://research.operationaldynamics.com/projects/quill/.
  */
 package quill.textbase;
 
 import junit.framework.TestCase;
 
 import static quill.textbase.Span.createSpan;
 
 /**
  * Make sure the extraction and reversing properties of Extract and the
  * extractRange() which creates them hold.
  * 
  * @author Andrew Cowie
  */
 public class ValidateExtracts extends TestCase
 {
     public final void testExtractRange() {
         final TextChain text;
         Extract extract;
 
         text = new TextChain();
         text.append(createSpan("Hello World", null));
 
         extract = text.extractRange(1, 3);
         assertEquals("Hello World", text.toString());
         assertEquals("ell", extract.getText());
 
         extract = text.extractRange(0, 11);
         assertEquals("Hello World", extract.getText());
     }
 
     /*
      * This used to check that extractRange() would deal with inverted
      * parameters, but we later ran into a problem whereby we need the
      * absolute offset to construct the Change objects, and had no [good] way
      * to get the correct offset value back.
      */
     public final void testWidthNegative() {
         final TextChain text;
         Extract extract;
 
         text = new TextChain();
         text.append(createSpan("Hello World", null));
 
         try {
             extract = text.extractRange(9, -2);
             fail();
             extract.getWidth();
         } catch (IllegalArgumentException iae) {
             // good
         }
     }
 
     public final void testExtractLines() {
         final TextChain text;
         Extract[] lines;
         Extract extract;
 
         text = new TextChain();
         text.append(createSpan("Hello World", null));
         text.append(createSpan('\n', null));
         text.append(createSpan("Goodbye Eternity", null));
         assertEquals("Hello World\nGoodbye Eternity", text.toString());
 
         lines = text.extractParagraphs();
         assertEquals(2, lines.length);
 
         extract = lines[0];
         assertNotNull(extract);
         assertEquals("Hello World", extract.getText());
 
         extract = lines[1];
         assertNotNull(extract);
         assertEquals("Goodbye Eternity", extract.getText());
     }
 
     /*
      * Corner case: only a single span
      */
     public final void testExtractLinesSingle() {
         final TextChain text;
         Extract[] lines;
         Extract extract;
 
         text = new TextChain();
         text.append(createSpan("Hello World", null));
 
         lines = text.extractParagraphs();
         assertEquals(1, lines.length);
 
         extract = lines[0];
         assertNotNull(extract);
         assertEquals("Hello World", extract.getText());
     }
 
     /*
      * Corner case: no content
      */
     public final void testExtractLinesNone() {
         final TextChain text;
         Extract[] lines;
 
         text = new TextChain();
 
         lines = text.extractParagraphs();
         assertEquals(0, lines.length);
     }
 
     /*
      * Corner case: only a single Span
      */
     public final void testExtractLinesChar() {
         final TextChain text;
         Extract[] lines;
         Extract extract;
 
         text = new TextChain();
         text.append(createSpan('H', null));
 
         lines = text.extractParagraphs();
         assertEquals(1, lines.length);
 
         extract = lines[0];
         assertNotNull(extract);
         assertEquals("H", extract.getText());
     }
 
     /*
      * Corner case: only a single span
      */
     public final void testExtractLinesTwoEmpty() {
         final TextChain text;
         Extract[] lines;
         Extract extract;
 
         text = new TextChain();
         text.append(createSpan('\n', null));
 
         lines = text.extractParagraphs();
         assertEquals(2, lines.length);
 
         extract = lines[0];
        assertNull(extract);
         extract = lines[1];
        assertNull(extract);
     }
 
     public final void testExtractLinesEmbeddedNewline() {
         final TextChain text;
         Extract[] lines;
         Extract extract;
 
         text = new TextChain();
         text.append(createSpan("Hello World\nGoodbye Eternity", null));
         assertEquals("Hello World\nGoodbye Eternity", text.toString());
 
         lines = text.extractParagraphs();
         assertEquals(2, lines.length);
 
         extract = lines[0];
         assertNotNull(extract);
         assertEquals("Hello World", extract.getText());
 
         extract = lines[1];
         assertNotNull(extract);
         assertEquals("Goodbye Eternity", extract.getText());
     }
 
     /*
      * Same test again, but with leading and trailing newlines.
      */
     public final void testExtractLinesBoundaryConditions() {
         TextChain text;
         Extract[] lines;
         Extract extract;
         Span span;
 
         text = new TextChain();
         text.append(createSpan("H\nello World", null));
         assertEquals("H\nello World", text.toString());
 
         lines = text.extractParagraphs();
         assertEquals(2, lines.length);
 
         extract = lines[0];
         assertNotNull(extract);
         assertEquals("H", extract.getText());
 
         extract = lines[1];
         assertNotNull(extract);
         assertEquals("ello World", extract.getText());
 
         text = new TextChain();
         text.append(createSpan("\nHello World", null));
         assertEquals("\nHello World", text.toString());
 
         lines = text.extractParagraphs();
         assertEquals(2, lines.length);
 
         extract = lines[0];
        assertNull(extract);
 
         extract = lines[1];
         assertNotNull(extract);
         assertEquals("Hello World", extract.getText());
 
         text = new TextChain();
         span = createSpan("Hello World\n", null);
         text.append(span);
         assertEquals("Hello World\n", text.toString());
 
         lines = text.extractParagraphs();
         assertEquals(2, lines.length);
 
         extract = lines[0];
         assertNotNull(extract);
         assertEquals("Hello World", extract.getText());
 
         extract = lines[1];
        assertNull(extract);
     }
 
     public final void testExtractParagraphBoundaryAfterUnicode() {
         TextChain text;
         Extract[] lines;
         Extract extract;
         Span span;
 
         text = new TextChain();
         text.append(createSpan("Pro𝑛to\nSurprise", null));
         assertEquals("Pro𝑛to\nSurprise", text.toString());
         extract = text.extractAll();
         assertNotNull(extract);
         assertEquals(15, extract.getWidth());
 
         lines = text.extractParagraphs();
 
         extract = text.extractAll();
         assertEquals("Pro𝑛to\nSurprise", extract.getText());
 
         assertEquals(2, lines.length);
 
         extract = lines[0];
         assertNotNull(extract);
         assertEquals("Pro𝑛to", extract.getText());
 
         extract = lines[1];
         assertNotNull(extract);
         assertEquals("Surprise", extract.getText());
     }
 }
