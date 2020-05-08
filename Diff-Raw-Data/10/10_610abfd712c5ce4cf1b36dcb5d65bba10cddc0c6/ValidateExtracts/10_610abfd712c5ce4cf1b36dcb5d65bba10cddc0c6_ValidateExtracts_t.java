 /*
  * ValidateExtracts.java
  *
  * Copyright (c) 2009 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the suite it is a part of, are made available
  * to you by the authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
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
         assertEquals(1, extract.range.length);
         assertEquals("ell", extract.range[0].getText());
 
         extract = text.extractRange(0, 11);
         assertEquals(3, extract.range.length);
         assertEquals("H", extract.range[0].getText());
         assertEquals("ell", extract.range[1].getText());
         assertEquals("o World", extract.range[2].getText());
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
         Span span;
 
         text = new TextChain();
         text.append(createSpan("Hello World", null));
         text.append(createSpan('\n', null));
         text.append(createSpan("Goodbye Eternity", null));
         assertEquals("Hello World\nGoodbye Eternity", text.toString());
 
         lines = text.extractParagraphs();
         assertEquals(2, lines.length);
 
         extract = lines[0];
         assertNotNull(extract);
         assertEquals(1, extract.size());
         span = extract.get(0);
         assertEquals("Hello World", span.getText());
 
         extract = lines[1];
         assertNotNull(extract);
         assertEquals(1, extract.size());
         span = extract.get(0);
         assertEquals("Goodbye Eternity", span.getText());
     }
 
     /*
      * Corner case: only a single span
      */
     public final void testExtractLinesSingle() {
         final TextChain text;
         Extract[] lines;
         Extract extract;
         Span span;
 
         text = new TextChain();
         text.append(createSpan("Hello World", null));
 
         lines = text.extractParagraphs();
         assertEquals(1, lines.length);
 
         extract = lines[0];
         assertNotNull(extract);
         assertEquals(1, extract.size());
         span = extract.get(0);
         assertEquals("Hello World", span.getText());
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
         Span span;
 
         text = new TextChain();
         text.append(createSpan('H', null));
 
         lines = text.extractParagraphs();
         assertEquals(1, lines.length);
 
         extract = lines[0];
         assertNotNull(extract);
         assertEquals(1, extract.size());
         span = extract.get(0);
         assertEquals("H", span.getText());
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
         assertNotNull(extract);
         assertEquals(0, extract.size());
         extract = lines[1];
         assertNotNull(extract);
         assertEquals(0, extract.size());
     }
 
     public final void testExtractLinesEmbeddedNewline() {
         final TextChain text;
         Extract[] lines;
         Extract extract;
         Span span;
 
         text = new TextChain();
         text.append(createSpan("Hello World\nGoodbye Eternity", null));
         assertEquals("Hello World\nGoodbye Eternity", text.toString());
 
         lines = text.extractParagraphs();
         assertEquals(2, lines.length);
 
         extract = lines[0];
         assertNotNull(extract);
         assertEquals(1, extract.size());
         span = extract.get(0);
         assertEquals("Hello World", span.getText());
 
         extract = lines[1];
         assertNotNull(extract);
         assertEquals(1, extract.size());
         span = extract.get(0);
         assertEquals("Goodbye Eternity", span.getText());
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
         assertEquals(1, extract.size());
         span = extract.get(0);
         assertEquals("H", span.getText());
 
         extract = lines[1];
         assertNotNull(extract);
         assertEquals(1, extract.size());
         span = extract.get(0);
         assertEquals("ello World", span.getText());
 
         text = new TextChain();
         text.append(createSpan("\nHello World", null));
         assertEquals("\nHello World", text.toString());
 
         lines = text.extractParagraphs();
         assertEquals(2, lines.length);
 
         extract = lines[0];
         assertNotNull(extract);
         assertEquals(0, extract.size());
 
         extract = lines[1];
         assertNotNull(extract);
         assertEquals(1, extract.size());
         span = extract.get(0);
         assertEquals("Hello World", span.getText());
 
         text = new TextChain();
        span = createSpan("Hello World\n", null);
        text.append(span);
         assertEquals("Hello World\n", text.toString());
 
         lines = text.extractParagraphs();
         assertEquals(2, lines.length);
 
         extract = lines[0];
         assertNotNull(extract);
         assertEquals(1, extract.size());
         span = extract.get(0);
         assertEquals("Hello World", span.getText());
 
         extract = lines[1];
         assertNotNull(extract);
         assertEquals(0, extract.size());
     }
 }
