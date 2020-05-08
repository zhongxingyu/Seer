 /*
  * ValidateWrapperExpansions.java
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
 
 public class ValidateStructuralChange extends TestCase
 {
     private Segment[] segments;
 
     public void setUp() {
         TextChain chain;
 
         segments = new Segment[] {
                 new ComponentSegment(), new HeadingSegment(), new ParagraphSegment(),
         };
 
         chain = new TextChain("Chapter 1");
         segments[0].setText(chain);
 
         chain = new TextChain("The beginning");
         segments[1].setText(chain);
 
         chain = new TextChain("Hello World");
         segments[2].setText(chain);
     }
 
     public final void testSetup() {
         final Series series;
 
         series = new Series(segments);
         assertEquals(3, series.size());
         assertEquals("Hello World", series.get(2).getText().toString());
     }
 
     /**
      * We don't really want this as a toString() on Series, so we just put it
      * here as a utility method.
      */
     private static String concatonate(Series series) {
         final StringBuilder str;
         Segment segment;
         int i;
         TextChain chain;
 
         str = new StringBuilder();
 
         for (i = 0; i < series.size(); i++) {
             segment = series.get(i);
             chain = segment.getText();
             str.append(chain.toString());
         }
 
         return str.toString();
     }
 
     public final void testSpliceSameSegmentMiddle() {
         final Series series;
         final Segment inserted;
         TextChain chain;
         final Change change;
 
         series = new Series(segments);
         assertEquals(3, series.size());
 
         inserted = new PreformatSegment();
         chain = new TextChain("cruel() ");
         inserted.setText(chain);
 
        change = new SplitStructuralChange(segments[2], 6, inserted);
         change.apply();
 
         assertEquals(5, series.size());
         assertSame(series.get(0), segments[0]);
         assertSame(series.get(1), segments[1]);
         assertSame(series.get(2), segments[2]);
         assertTrue(series.get(3) instanceof PreformatSegment);
         assertTrue(series.get(4) instanceof ParagraphSegment);
         assertEquals("Chapter 1The beginningHello cruel() World", concatonate(series));
     }
 
     public final void testRevertStructuralChange() {
         final Series series;
         final Segment inserted;
         TextChain chain;
         final Change change;
 
         series = new Series(segments);
         assertEquals(3, series.size());
 
         inserted = new PreformatSegment();
         chain = new TextChain("cruel() ");
         inserted.setText(chain);
 
        change = new SplitStructuralChange(segments[2], 6, inserted);
         change.apply();
 
         change.undo();
 
         assertEquals("Chapter 1The beginningHello World", concatonate(series));
     }
 
     public final void testSpliceSameSegmentEnd() {
         final Series series;
         final Segment inserted;
         TextChain chain;
         final Change change;
 
         series = new Series(segments);
         assertEquals(3, series.size());
 
         inserted = new PreformatSegment();
         chain = new TextChain("save()");
         inserted.setText(chain);
 
        change = new SplitStructuralChange(segments[2], 11, inserted);
         change.apply();
 
         assertEquals(4, series.size());
         assertSame(series.get(0), segments[0]);
         assertSame(series.get(1), segments[1]);
         assertSame(series.get(2), segments[2]);
         assertTrue(series.get(3) instanceof PreformatSegment);
         assertEquals("Chapter 1The beginningHello Worldsave()", concatonate(series));
 
         change.undo();
         assertEquals("Chapter 1The beginningHello World", concatonate(series));
     }
 
     public final void testSpliceSameSegmentBegin() {
         final Series series;
         final Segment inserted;
         final TextChain chain;
         final Change change;
 
         series = new Series(segments);
         assertEquals(3, series.size());
 
         inserted = new PreformatSegment();
         chain = new TextChain("init()");
         inserted.setText(chain);
 
        change = new SplitStructuralChange(segments[0], 0, inserted);
         change.apply();
 
         assertEquals(4, series.size());
         assertTrue(series.get(0) instanceof PreformatSegment);
         assertSame(series.get(1), segments[0]);
         assertSame(series.get(2), segments[1]);
         assertSame(series.get(3), segments[2]);
 
         assertEquals("init()Chapter 1The beginningHello World", concatonate(series));
 
         change.undo();
         assertEquals("Chapter 1The beginningHello World", concatonate(series));
     }
 }
