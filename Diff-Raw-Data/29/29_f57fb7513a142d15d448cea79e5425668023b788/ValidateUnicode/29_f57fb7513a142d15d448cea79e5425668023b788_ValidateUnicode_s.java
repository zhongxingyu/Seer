 /*
  * ValidateUnicode.java
  *
  * Copyright (c) 2009 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the suite it is a part of, are made available
  * to you by the authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 
 package quill.textbase;
 
 import java.lang.reflect.Field;
 
 import org.gnome.gtk.TextBuffer;
 import org.gnome.gtk.TextIter;
 
 import quill.ui.GraphicalTestCase;
 
 import static quill.textbase.Span.createSpan;
 
 /**
  * Make sure textbase properly handles 2- and 3-byte characters.
  * 
  * @author Andrew Cowie
  */
 /*
  * DANGER: Eclipse seems to be buggy at editing lines with >2 byte characters
  * in them. This file is UTF-8, so the content is correct. And the display in
  * Eclipse is correct. But if you try editing a line with the 3-byte n
  * character, Bad Things™ happen. Presumably Eclipse's editor is making the
  * assumption that String.length() == number of displayed characters.
  */
 public class ValidateUnicode extends GraphicalTestCase
 {
     public final void testSurrogageIdentification() {
         assertTrue(Character.MIN_HIGH_SURROGATE == Character.MIN_SURROGATE);
         assertTrue(Character.MAX_HIGH_SURROGATE > Character.MIN_SURROGATE);
         assertTrue(Character.MAX_HIGH_SURROGATE < Character.MAX_SURROGATE);
 
         assertTrue(Character.MIN_LOW_SURROGATE > Character.MIN_SURROGATE);
         assertTrue(Character.MIN_LOW_SURROGATE < Character.MAX_SURROGATE);
         assertTrue(Character.MAX_LOW_SURROGATE == Character.MAX_SURROGATE);
 
         assertTrue(isSurrogate(Character.MAX_SURROGATE));
 
         // but
         assertTrue(Character.isSupplementaryCodePoint(0x1d45b));
     }
 
     /*
      * It turned out we didn't need this after all. So the code is moved here
      * from UnicodeSpan for safe keeping.
      */
     private static final boolean isSurrogate(int point) {
         if ((point >= Character.MIN_SURROGATE) && (point <= Character.MAX_SURROGATE)) {
             return true;
         } else {
             return false;
         }
     }
 
     public final void testLatin1Supplement() {
         final TextChain chain;
 
         chain = new TextChain("This character 'µ' is a micro symbol");
         assertEquals("This character '\u00b5' is a micro symbol", chain.toString());
     }
 
     public final void testMathematicalAlphanumericSymbols() {
         final TextChain chain1, chain2, chain3, chain4;
 
         chain1 = new TextChain("This character '𝑛' is an italic lower-case N");
         assertEquals("This character '\ud835\udc5b' is an italic lower-case N", chain1.toString());
 
         chain2 = new TextChain("This character '\ud835\udc5b' is an italic lower-case N");
         assertEquals("This character '𝑛' is an italic lower-case N", chain2.toString());
 
         chain3 = new TextChain("This character '𝑛' is an italic lower-case N");
         assertEquals("This character '𝑛' is an italic lower-case N", chain3.toString());
 
         chain4 = new TextChain("This character '\ud835\udc5b' is an italic lower-case N");
         assertEquals("This character '\ud835\udc5b' is an italic lower-case N", chain4.toString());
     }
 
     /*
      * Observe interations betwen UTF-8 .java sources and the Java Language
      * Specification, and use of multi-char UTF-16.
      */
     public final void testUnicdeSurrogates() {
         final String str;
 
         str = "1𝑛3";
         assertEquals(4, str.length()); // !?!
 
         assertEquals('1', str.charAt(0));
         assertEquals('1', str.codePointAt(0));
 
         assertEquals('\ud835', str.charAt(1));
 
         assertEquals(0x1d45b, str.codePointAt(1));
         assertTrue(Character.isLetter(0x1d45b));
         assertEquals(Character.LOWERCASE_LETTER, Character.getType(0x1d45b));
         assertTrue(Character.isHighSurrogate('\ud835'));
 
         /*
          * Now the freaky stuff
          */
         assertEquals('\udc5b', str.charAt(2));
         assertTrue(!Character.isHighSurrogate('\udc5b'));
         assertTrue(Character.isLowSurrogate('\udc5b'));
         assertEquals(0xdc5b, str.codePointAt(2));
 
         assertEquals('3', str.charAt(3));
         assertEquals('3', str.codePointAt(3));
     }
 
     private int length;
 
     /*
      * Test that we're actually getting a UTF-16 encoded character like we
      * thing we are.
      */
     public final void testBufferCharacters() {
         final TextBuffer buffer;
 
         buffer = new TextBuffer();
         length = 0;
 
         buffer.connect(new TextBuffer.InsertText() {
             public void onInsertText(TextBuffer source, TextIter pointer, String text) {
                 length = text.length();
             }
         });
 
         buffer.insertAtCursor("𝑛");
         assertEquals(2, length);
     }
 
     public final void testUnicodeSpan() {
         final String str;
         final Span span;
 
         str = "Cru𝑛ch";
 
         span = createSpan(str, null);
 
         assertEquals(6, span.getWidth());
         assertEquals('C', span.getChar(0));
         assertEquals('r', span.getChar(1));
         assertEquals('u', span.getChar(2));
         assertEquals(0x1d45b, span.getChar(3));
         assertEquals('c', span.getChar(4));
         assertEquals('h', span.getChar(5));
     }
 
     public final void testOffsetCorrosion() {
         final TextBuffer buffer;
         final TextChain chain;
         final String str;
 
         str = "Cru𝑛ch";
         assertEquals(7, str.length());
 
         buffer = new TextBuffer();
         buffer.setText(str);
         assertEquals(6, buffer.getCharCount());
 
         chain = new TextChain(str);
         assertNotSame(str, chain.toString());
         assertEquals(str, chain.toString());
         assertEquals(6, chain.length());
     }
 
     public final void testUnicodeSpanArrayCaching() {
         final UnicodeSpan t1, t2, w1, w2;
 
         /*
          * Both int[] and String will be cached.
          */
         t1 = (UnicodeSpan) createSpan("𝌐", null);
         t2 = (UnicodeSpan) createSpan("𝌐", null);
 
         assertSame(introspectPointsArray(t1), introspectPointsArray(t2));
         assertSame(t1.getText(), t2.getText());
 
         /*
          * Won't be cached, and nor will the String data [here, force separate
          * String objects.
          */
         w1 = (UnicodeSpan) createSpan(new String("The 𝌐 symbol is divergence"), null);
         w2 = (UnicodeSpan) createSpan(new String("The 𝌐 symbol is divergence"), null);
 
         assertNotSame(introspectPointsArray(w1), introspectPointsArray(w2));
         assertNotSame(w1.getText(), w2.getText());
     }
 
     public final void testUnicodeSplitting() {
         final UnicodeSpan u1, u2;
        final Span c1, s1, u3, u4, u5;
 
         u1 = (UnicodeSpan) createSpan(new String("The 𝌤 symbol is packing"), null);
         u2 = (UnicodeSpan) u1.split(4, 5);
         assertSame(introspectPointsArray(u1), introspectPointsArray(u2));
 
         c1 = u1.split(6, 7);
         assertTrue(c1 instanceof CharacterSpan);
         assertEquals('s', c1.getChar(0));
 
         s1 = u1.split(16);
         assertTrue(s1 instanceof StringSpan);
         assertEquals("packing", s1.getText());
 
         u3 = u1.split(0, 12);
         assertTrue(u3 instanceof UnicodeSpan);
         assertEquals("The 𝌤 symbol", u3.getText());
 
         u4 = u1.split(1, 9);
         assertEquals("he 𝌤 sym", u4.getText());
 
         u5 = u4.split(1, 6);
         assertEquals("e 𝌤 s", u5.getText());
         assertSame(introspectPointsArray(u5), introspectPointsArray(u4));
     }
 
     private static int[] introspectPointsArray(Span span) {
         final Field field;
         Object points;
         final int[] result;
 
         assertTrue(span instanceof UnicodeSpan);
 
         try {
             field = UnicodeSpan.class.getDeclaredField("points");
             field.setAccessible(true);
             points = field.get(span);
             result = (int[]) points;
 
             assertNotNull(result);
 
             return result;
         } catch (Exception e) {
             fail(e.toString());
             return null;
         }
     }
 }
