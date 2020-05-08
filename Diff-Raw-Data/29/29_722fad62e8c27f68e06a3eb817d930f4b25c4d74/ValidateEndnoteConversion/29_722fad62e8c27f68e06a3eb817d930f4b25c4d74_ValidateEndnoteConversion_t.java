 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2009-2010 Operational Dynamics Consulting, Pty Ltd
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
 package parchment.quack;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import nu.xom.ParsingException;
 import nu.xom.ValidityException;
 import parchment.manuscript.Chapter;
 import parchment.manuscript.Manuscript;
 import quill.client.ImproperFilenameException;
 import quill.textbase.ChapterSegment;
import quill.textbase.CharacterSpan;
 import quill.textbase.EndnoteSegment;
 import quill.textbase.Extract;
 import quill.textbase.MarkerSpan;
 import quill.textbase.Markup;
 import quill.textbase.NormalSegment;
 import quill.textbase.QuoteSegment;
 import quill.textbase.Segment;
 import quill.textbase.Series;
 import quill.textbase.Span;
 import quill.textbase.SpanVisitor;
 import quill.textbase.Special;
 import quill.textbase.SpecialSegment;
 import quill.textbase.StringSpan;
 import quill.textbase.TextChain;
 
 public class ValidateEndnoteConversion extends QuackTestCase
 {
     public final void testMarkerSpan() {
         Span span;
 
         span = Span.createMarker("1", Special.NOTE);
         assertTrue(span instanceof MarkerSpan);
         assertEquals("1", span.getText());
         assertEquals(Special.NOTE, span.getMarkup());
     }
 
     public final void testInlineNote() throws IOException, ValidityException, ParsingException,
             ImproperFilenameException {
         final Series series;
         Segment segment;
         final Extract entire;
 
         series = loadDocument("tests/parchment/quack/Endnote.xml");
 
         assertEquals(2, series.size());
 
         segment = series.getSegment(1);
         assertTrue(segment instanceof QuoteSegment);
 
         entire = segment.getEntire();
         entire.visit(new SpanVisitor() {
             private int i = 0;
 
             public boolean visit(Span span) {
                 switch (i) {
                 case 0:
                     assertTrue(span instanceof StringSpan);
                     break;
                 case 1:
                     assertTrue(span instanceof MarkerSpan);
                     assertEquals(Special.NOTE, span.getMarkup());
                     break;
                 default:
                     fail();
                 }
                 i++;
                 return false;
             }
         });
 
         compareDocument(series);
     }
 
     public final void testManyNotes() throws IOException, ValidityException, ParsingException,
             ImproperFilenameException {
         final Series series;
         Segment segment;
 
         series = loadDocument("tests/parchment/quack/Manynotes.xml");
 
         /*
          * Check the state is what we think it is
          */
 
         assertEquals(4, series.size());
 
         segment = series.getSegment(0);
         assertTrue(segment instanceof ChapterSegment);
         segment = series.getSegment(1);
         assertTrue(segment instanceof NormalSegment);
         segment = series.getSegment(2);
         assertTrue(segment instanceof QuoteSegment);
         segment = series.getSegment(3);
         assertTrue(segment instanceof NormalSegment);
 
         /*
          * Now, write out, and test.
          */
 
         compareDocument(series);
     }
 
     /*
      * Bug: for some reason, markup at the beginning of a following block is
      * being lost. This test demonstrates the problem.
      */
     public final void testTwoBlockWithNote() throws IOException, ValidityException, ParsingException,
             ImproperFilenameException {
         final Manuscript manuscript;
         final Chapter chapter;
         Series series;
         Segment segment;
         final TextChain chain;
         Span span;
         final int offset;
         final Markup markup;
         final Extract extract;
         Extract entire;
         final QuackConverter converter;
         final ByteArrayOutputStream out;
         final String expected, result;
         int i;
 
         expected = loadFileIntoString("tests/parchment/quack/Manynotes2.xml");
 
         manuscript = new Manuscript();
         manuscript.setFilename("tests/parchment/quack/ValidateEndnoteConversion.parchment");
         chapter = new Chapter(manuscript);
         chapter.setFilename("Manynotes.xml");
 
         /*
          * Check the state is what we think it is
          */
 
         series = chapter.loadDocument();
         assertEquals(4, series.size());
 
         segment = series.getSegment(0);
         assertTrue(segment instanceof ChapterSegment);
         segment = series.getSegment(1);
         assertTrue(segment instanceof NormalSegment);
         segment = series.getSegment(2);
         assertTrue(segment instanceof QuoteSegment);
         segment = series.getSegment(3);
         assertTrue(segment instanceof NormalSegment);
 
         /*
          * Append a <note> to the end of the first NormalSegment
          */
 
         segment = series.getSegment(1);
         entire = segment.getEntire();
 
         span = Span.createMarker("[Einstein, 1905]", Special.NOTE);
         chain = new TextChain(entire);
         chain.append(span);
 
         /*
          * Make sure it got added
          */
 
         offset = chain.length();
         markup = chain.getMarkupAt(offset - 1);
         assertEquals(Special.NOTE, markup);
 
         extract = chain.extractRange(offset - 1, 1);
         extract.visit(new SpanVisitor() {
             public boolean visit(Span span) {
                 assertTrue(span instanceof MarkerSpan);
                 return true;
             }
         });
 
         /*
          * Now, write out, and test. We shouldn't have lost any markup.
          */
 
         entire = chain.extractAll();
         segment = segment.createSimilar(entire, 0, 0, entire.getWidth());
         series = series.update(1, segment);
 
         converter = new QuackConverter();
 
         for (i = 0; i < series.size(); i++) {
             converter.append(series.getSegment(i));
         }
 
         out = new ByteArrayOutputStream();
         converter.writeChapter(out);
 
         result = out.toString();
         assertEquals(expected, result);
     }
 
     public final void testSpecials() throws IOException, ValidityException, ParsingException,
             ImproperFilenameException {
         Series series;
         Segment segment;
 
         series = loadDocument("tests/parchment/quack/Specials.xml");
 
         /*
          * Check the state is what we think it is
          */
 
         assertEquals(2, series.size());
 
         segment = series.getSegment(0);
         assertTrue(segment instanceof ChapterSegment);
         segment = series.getSegment(1);
         assertTrue(segment instanceof SpecialSegment);
 
         /*
          * Now, write out, and test. We shouldn't lose anything
          */
 
         compareDocument(series);
     }
 
     /*
     * Bug encountered due to misplacement of captured newlines when wrapping
     * text followed by a <cite> element.
      */
     public final void testCitationInEndnoteWrapping() throws IOException, ValidityException,
             ParsingException, ImproperFilenameException {
         Series series;
         Segment segment;
         Extract entire;
         Span span;
 
         series = loadDocument("tests/parchment/quack/EndnoteWrappingBug.xml");
 
         assertEquals(2, series.size());
 
         segment = series.getSegment(0);
         assertTrue(segment instanceof ChapterSegment);
         segment = series.getSegment(1);
         assertTrue(segment instanceof EndnoteSegment);
 
         /*
          * This is pretty specific, but was debugging whether the problem was
          * in the Loader or...
          */
 
         entire = segment.getEntire();
 
         entire.visit(new SpanVisitor() {
             private int i;
 
             public boolean visit(Span span) {
                 String str;
                 Markup markup;
 
                 str = span.getText();
                 markup = span.getMarkup();
 
                 switch (i) {
                 case 0:
                     assertEquals("Enriched personal lives. See colophon to ", str);
                     break;
                 case 1:
                     assertTrue(span instanceof MarkerSpan);
                     assertTrue(markup == Special.CITE);
                     assertEquals("[12]", span.getText());
                     break;
                 case 2:
                    assertEquals(" and", str);
                     break;
                 case 3:
                    assertTrue(span instanceof CharacterSpan);
                    assertEquals(" ", str);
                    break;
                case 4:
                     assertTrue(span instanceof MarkerSpan);
                     assertTrue(markup == Special.CITE);
                     assertEquals("[18]", span.getText());
                     break;
                case 5:
                     assertEquals(".", str);
                     break;
                 default:
                     fail();
                 }
 
                 i++;
                 return false;
             }
         });
 
         /*
          * Now, write out, and test. We shouldn't change anything, but we were
          * getting spaces moved around.
          */
 
         compareDocument(series);
     }
 }
