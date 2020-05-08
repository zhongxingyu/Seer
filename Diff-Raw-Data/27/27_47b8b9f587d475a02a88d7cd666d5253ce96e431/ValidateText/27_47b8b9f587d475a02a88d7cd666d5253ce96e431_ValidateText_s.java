 /*
  * ValidateChunks.java
  *
  * Copyright (c) 2008 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the suite it is a part of, are made available
  * to you by the authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 
 package com.operationaldynamics.textbase;
 
 import junit.framework.TestCase;
 
 public class ValidateText extends TestCase
 {
     public final void testInitialText() {
         final Text start;
 
         start = new Text("Hello world");
 
         assertEquals("Hello world", start.toString());
     }
 
     public final void testTwoSequentialChunks() {
         final Text text;
         final Chunk second;
 
         text = new Text("Hello world");
 
         second = new Chunk(" it is a sunny day");
         text.append(second);
 
         assertEquals("Hello world it is a sunny day", text.toString());
     }
 
     public final void testExtractedChunks() {
         final Text text;
         final Chunk initial, one, two, three, space;
 
         initial = new Chunk("Emergency broadcast system");
 
         one = new Chunk(initial, 0, 9);
         two = new Chunk(initial, 10, 9);
         three = new Chunk(initial, 20, 6);
         space = new Chunk(initial, 9, 1);
 
         text = new Text("");
         text.append(three);
         text.append(space);
         text.append(two);
         text.append(space);
         text.append(one);
 
         assertEquals("system broadcast Emergency", text.toString());
     }
 
     public final void testSplittingAtPoint() {
         final Text text;
         final Chunk initial;
         final Piece one, two;
 
         initial = new Chunk("Concave");
         text = new Text(initial);
         assertEquals("Concave", text.toString());
         assertNull(text.first.next);
         assertNull(text.first.prev);
 
         one = text.splitAt(text.first, 3);
         assertEquals("Concave", text.toString());
 
         assertNotNull(one.next);
         assertEquals("Con", one.chunk.toString());
 
         two = one.next;
         assertEquals("cave", two.chunk.toString());
         assertNull(two.next);
         assertEquals(one, two.prev);
     }
 
     public final void testSplittingAtPieceBoundary() {
         final Text text;
         final Chunk a, b;
         final Piece first, one, two;
 
         a = new Chunk("Alpha");
         b = new Chunk("Bravo");
 
         text = new Text(a);
         text.append(b);
         assertEquals("AlphaBravo", text.toString());
 
         assertNull(text.first.prev);
         assertNull(text.first.next.next);
 
         first = text.first;
         one = text.splitAt(5);
         assertEquals("AlphaBravo", text.toString());
 
         assertSame(first, one);
         assertNotNull(one.next);
         assertNotSame(one, one.next);
         assertEquals("Alpha", one.chunk.toString());
 
         two = one.next;
         assertEquals("Bravo", two.chunk.toString());
         assertNull(two.next);
         assertEquals(one, two.prev);
     }
 
     public final void testSingleSplice() {
         final Text text;
 
         text = new Text("This Emergency Broadcast System");
 
         text.insert(5, "is a test of the ");
         assertEquals("This is a test of the Emergency Broadcast System", text.toString());
     }
 
     public final void testMultipleSplice() {
         final Text text;
         final Chunk one, two, three, four, space, addition;
         one = new Chunk("One");
         space = new Chunk(" ");
         two = new Chunk("Two");
         three = new Chunk("Three");
         four = new Chunk("Four");
 
         text = new Text(one);
         text.append(space);
         text.append(two);
         text.append(space);
         text.append(three);
         text.append(space);
         text.append(four);
 
         assertEquals("One Two Three Four", text.toString());
 
         assertEquals(7, calculateNumberPieces(text));
 
         /*
          * Now, try splicing something in
          */
 
         addition = new Chunk("wentyT");
         text.insert(5, addition);
         assertEquals("One TwentyTwo Three Four", text.toString());
 
         assertEquals(9, calculateNumberPieces(text));
     }
 
     private static int calculateNumberPieces(Text text) {
         Piece p;
         int i;
 
         i = 0;
         p = text.first;
         while (p != null) {
             p = p.next;
             i++;
         }
         return i;
     }
 
     public final void testTextLength() {
         final Text text;
         final Chunk zero, one, two;
 
         zero = new Chunk("Hello");
         assertEquals(5, zero.width);
         assertEquals(5, zero.text.length);
         text = new Text(zero);
         assertEquals(5, text.length());
 
         one = new Chunk("Happy");
         two = new Chunk("Days");
 
         text.append(one);
         text.append(two);
         assertEquals(14, text.length());
     }
 
     public final void testInsertBetweenExistingChunks() {
         final Text text;
         final Chunk zero, one, two, three;
 
         zero = new Chunk("Zero");
         one = new Chunk("One");
         two = new Chunk("Two");
         three = new Chunk("Three");
 
         text = new Text(one);
         text.append(two);
         text.append(three);
 
         assertEquals("OneTwoThree", text.toString());
 
         text.insert(0, zero);
         assertEquals("ZeroOneTwoThree", text.toString());
 
         text.insert(15, zero);
         assertEquals("ZeroOneTwoThreeZero", text.toString());
 
         text.insert(7, zero);
         assertEquals("ZeroOneZeroTwoThreeZero", text.toString());
     }
 
     /*
      * Ideally you'd just call append() here, but perhaps you don't know
      * you're at the end. So long as you specify an offset equalling the
      * character length of the Text (ie, not greater), it (should) still still
      * work, which this tests.
      */
     public final void testInsertIntoEnd() {
         final Text text;
         final String str;
 
         str = "All this has happened before";
         text = new Text(str);
 
         text.insert(str.length(), ", all this will happen again.");
         assertEquals("All this has happened before, all this will happen again.", text.toString());
     }
 
     public final void testConcatonatingChunks() {
         final Text text;
         final Chunk zero, one, two, three, result;
         final Piece splice;
 
         zero = new Chunk("Zero");
         one = new Chunk("One");
         two = new Chunk("Two");
         three = new Chunk("Three");
 
         text = new Text(zero);
         text.append(one);
         text.append(two);
         text.append(three);
 
         assertEquals("ZeroOneTwoThree", text.toString());
 
         splice = text.concatonateFrom(2, 11);
 
         assertEquals("ZeroOneTwoThree", text.toString());
         result = splice.chunk;
         assertEquals(0, result.start);
         assertEquals(11, result.width);
         assertEquals("roOneTwoThr", result.toString());
     }
 
     public final void testDeleteRange() {
         final Text text;
         final Chunk zero, one, two, three;
 
         zero = new Chunk("Zero");
         one = new Chunk("One");
         two = new Chunk("Two");
         three = new Chunk("Three");
 
         text = new Text(zero);
         text.append(one);
         text.append(two);
         text.append(three);
         assertEquals("ZeroOneTwoThree", text.toString());
 
         text.delete(2, 11);
         assertEquals("Zeee", text.toString());
         assertEquals(2, calculateNumberPieces(text));
         assertEquals("Ze", text.first.chunk.toString());
         assertEquals("ee", text.first.next.chunk.toString());
 
         text.delete(1, 2);
         assertEquals("Ze", text.toString());
         assertEquals(2, calculateNumberPieces(text));
     }
 
     public final void testDeleteBoundaries() {
         final Text text;
 
         text = new Text("Hello World");
 
         text.delete(0, 6);
         assertEquals("World", text.toString());
         assertEquals(1, calculateNumberPieces(text));
     }
 
    public final void testDeleteAll() {}
 }
