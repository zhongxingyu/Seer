 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2008-2010 Operational Dynamics Consulting, Pty Ltd
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
 package parchment.render;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 import org.freedesktop.cairo.Context;
 import org.freedesktop.cairo.FontOptions;
 import org.freedesktop.cairo.Surface;
 import org.gnome.gdk.Pixbuf;
 import org.gnome.gtk.PaperSize;
 import org.gnome.gtk.Unit;
 import org.gnome.pango.Attribute;
 import org.gnome.pango.AttributeList;
 import org.gnome.pango.BackgroundColorAttribute;
 import org.gnome.pango.FontDescription;
 import org.gnome.pango.FontDescriptionAttribute;
 import org.gnome.pango.ForegroundColorAttribute;
 import org.gnome.pango.Layout;
 import org.gnome.pango.LayoutLine;
 import org.gnome.pango.Rectangle;
 import org.gnome.pango.RiseAttribute;
 import org.gnome.pango.SizeAttribute;
 import org.gnome.pango.Style;
 import org.gnome.pango.StyleAttribute;
 import org.gnome.pango.Weight;
 import org.gnome.pango.WeightAttribute;
 import org.gnome.pango.WrapMode;
 
 import parchment.format.Manuscript;
 import quill.textbase.AttributionSegment;
 import quill.textbase.Common;
 import quill.textbase.ComponentSegment;
 import quill.textbase.Extract;
 import quill.textbase.Folio;
 import quill.textbase.HeadingSegment;
 import quill.textbase.ImageSegment;
 import quill.textbase.Markup;
 import quill.textbase.NormalSegment;
 import quill.textbase.Origin;
 import quill.textbase.PoeticSegment;
 import quill.textbase.Preformat;
 import quill.textbase.PreformatSegment;
 import quill.textbase.QuoteSegment;
 import quill.textbase.Segment;
 import quill.textbase.Series;
 import quill.textbase.Span;
 import quill.textbase.SpanVisitor;
 import quill.textbase.Special;
 import quill.textbase.TextChain;
 
 import static org.freedesktop.cairo.HintMetrics.OFF;
 import static quill.textbase.Span.createSpan;
 
 /**
  * Render a Series.
  * 
  * <p>
  * This class and its concrete descendants work in in "points", which makes
  * sense since the ultimate target back end is PDF. When used to generate a
  * preview for screen, it's up to the rendering Widget to scale the Context to
  * the allocated size.
  * 
  * @author Andrew Cowie
  */
 public abstract class RenderEngine
 {
     private double pageWidth;
 
     private double pageHeight;
 
     private double topMargin;
 
     private double bottomMargin;
 
     private double leftMargin;
 
     private double rightMargin;
 
     private double footerHeight;
 
     private Folio folio;
 
     private Manuscript manuscript;
 
     Typeface sansFace;
 
     Typeface serifFace;
 
     Typeface monoFace;
 
     Typeface headingFace;
 
     /**
      * This chapter's content, as prepared into Areas.
      */
     private ArrayList<Area> areas;
 
     /**
      * This chapter's content, as flowed into Pages.
      */
     private ArrayList<Page> pages;
 
     /**
      * Where is a given (Segment, offset) pair?
      */
     private TreeMap<Origin, Page> lookup;
 
     /**
      * The index that the present Series is into the Folio (for composing
      * Origins).
      */
     private int folioIndex;
 
     /**
      * The current Segment's index into the Series (for composing Origins).
      */
     private int seriesIndex;
 
     /**
      * The current offset into the current Segment (for composing Origins).
      */
     private int currentOffset;
 
     /**
      * Construct a new RenderEngine. Call {@link #render(Context) render()} to
      * actually draw. Pass in the document being rendered (the target file
      * will be derived from this) and the current document state root.
      */
     protected RenderEngine(final PaperSize paper, final Manuscript manuscript, final Folio folio) {
         specifySize(paper);
         this.manuscript = manuscript;
         this.folio = folio;
     }
 
     /**
      * Given a Context, have the rendering engine to draw to it. This assumes
      * that the target Surface either a) has the size as the PaperSize passed
      * to the constructor, or b) has been scaled to that size.
      */
     public void render(final Context cr) {
         if (folio == null) {
             return;
         }
 
         synchronized (this) {
             specifyFonts(cr);
             processSegmentsIntoAreas(cr);
             flowAreasIntoPages(cr);
             renderAllPages(cr);
         }
     }
 
     /*
      * TODO needs to act to prepare and flow, caching the result so that
      * subsequent calls here don't re-do everything.
      */
     public void render(Context cr, int pageNum) {
         if (folio == null) {
             return;
         }
 
         synchronized (this) {
             specifyFonts(cr);
             processSegmentsIntoAreas(cr);
             flowAreasIntoPages(cr);
             renderSinglePage(cr, pageNum);
         }
     }
 
     public void render(Context cr, Origin cursor) {
         if (folio == null) {
             return;
         }
         if (cursor == null) {
             return;
         }
 
         synchronized (this) {
             specifyFonts(cr);
             processSegmentsIntoAreas(cr);
             flowAreasIntoPages(cr);
             renderSinglePage(cr, cursor);
         }
     }
 
     private void renderAllPages(Context cr) {
         final Surface surface;
         final int I;
         int i;
         Page page;
 
         I = pages.size();
         surface = cr.getTarget();
 
         for (i = 0; i < I; i++) {
             page = pages.get(i);
 
             /*
              * Draw the page.
              */
 
             page.render(cr);
 
             /*
              * Flush the page out, and begin a new one.
              */
 
             if (i < I - 1) {
                 surface.showPage();
             }
         }
 
         surface.finish();
     }
 
     private void renderSinglePage(Context cr, int pageNum) {
         final Surface surface;
         final Page page;
 
         surface = cr.getTarget();
 
         page = pages.get(pageNum - 1);
         page.render(cr);
 
         surface.finish();
     }
 
     private void renderSinglePage(final Context cr, final Origin target) {
         final Surface surface;
         final Origin key;
         final Page page;
 
         surface = cr.getTarget();
 
         key = lookup.floorKey(target);
         if (key != null) {
             page = lookup.get(key);
         } else {
             /*
              * Assuming there's a (0,0) Origin for the first page, we
              * shouldn't ever get here. But guard against it as the Area ->
              * Origin:Page logic is still a little raw.
              */
             page = pages.get(0);
         }
 
         page.render(cr);
 
         surface.finish();
     }
 
     /*
      * This will move to the actual RenderEngine subclass, I expect.
      */
     protected void specifyFonts(final Context cr) {
         serifFace = new Typeface(cr, new FontDescription("Linux Libertine O, 9.0"), 0.2);
 
         monoFace = new Typeface(cr, new FontDescription("Inconsolata, 8.3"), 0.0);
 
         sansFace = new Typeface(cr, new FontDescription("Liberation Sans, 7.3"), 0.0);
 
         headingFace = new Typeface(cr, new FontDescription("Linux Libertine O C"), 0.0);
 
         cr.setSource(0.0, 0.0, 0.0);
     }
 
     /*
      * 2 cm = 56.67 pt
      */
     private void specifySize(final PaperSize paper) {
         pageWidth = paper.getWidth(Unit.POINTS);
         pageHeight = paper.getHeight(Unit.POINTS);
 
         topMargin = 40.0;
         bottomMargin = 30.0;
         leftMargin = 56.67;
         rightMargin = 45.0;
     }
 
     public void processSegmentsIntoAreas(final Context cr) {
         int i, j, k;
         Series series;
         Segment segment;
         Extract entire;
         TextChain chain;
         Extract[] paras;
         String filename;
 
         areas = new ArrayList<Area>(64);
         footerHeight = serifFace.lineHeight;
 
         for (i = 0; i < folio.size(); i++) {
             series = folio.getSeries(i);
             folioIndex = i;
 
             for (j = 0; j < series.size(); j++) {
                 seriesIndex = j;
                 currentOffset = 0;
 
                 segment = series.getSegment(j);
                 entire = segment.getEntire();
 
                 if (segment instanceof ComponentSegment) {
                     appendHeading(cr, entire, 32.0);
                     appendBlankLine(cr);
                 } else if (segment instanceof HeadingSegment) {
                     appendHeading(cr, entire, 16.0);
                     appendBlankLine(cr);
                 } else if (segment instanceof PreformatSegment) {
                     appendProgramCode(cr, entire);
                     appendBlankLine(cr);
                 } else if (segment instanceof QuoteSegment) {
                     chain = new TextChain(entire);
                     paras = chain.extractParagraphs();
                     for (k = 0; k < paras.length; k++) {
                         appendQuoteParagraph(cr, paras[k]);
                         appendBlankLine(cr);
                     }
                 } else if (segment instanceof NormalSegment) {
                     chain = new TextChain(entire);
                     paras = chain.extractParagraphs();
                     for (k = 0; k < paras.length; k++) {
                         appendNormalParagraph(cr, paras[k]);
                         appendBlankLine(cr);
                     }
                 } else if (segment instanceof PoeticSegment) {
                    appendNormalParagraph(cr, entire);
                     appendBlankLine(cr);
                 } else if (segment instanceof AttributionSegment) {
                    appendAttributionParagraph(cr, entire);
                     appendBlankLine(cr);
                 } else if (segment instanceof ImageSegment) {
                     filename = segment.getImage();
                     appendExternalGraphic(cr, filename);
                     appendBlankLine(cr);
                     if (entire == null) {
                         continue;
                     }
                     appendCitationParagraph(cr, entire);
                     appendBlankLine(cr);
                 }
             }
 
             appendPageBreak(cr);
         }
     }
 
     protected void appendBlankLine(Context cr) {
         final Origin origin;
         final Area area;
         final double request;
 
         request = serifFace.lineHeight * 0.7;
 
         origin = new Origin(folioIndex, seriesIndex, currentOffset++);
         area = new BlankArea(origin, request);
         accumulate(area);
     }
 
     protected void appendPageBreak(Context cr) {
         final Origin origin;
         final Area area;
 
         origin = new Origin(folioIndex, seriesIndex, currentOffset);
         area = new PageBreakArea(origin);
         accumulate(area);
     }
 
     protected void appendHeading(Context cr, Extract entire, double size) {
         final FontDescription desc;
         final Typeface face;
         final Area[] list;
 
         desc = headingFace.desc.copy();
         desc.setSize(size);
         face = new Typeface(cr, desc, 0.0);
 
         list = layoutAreaText(cr, entire, face, false, false, false);
         accumulate(list);
     }
 
     private void accumulate(Area[] list) {
         int i;
 
         for (i = 0; i < list.length; i++) {
             areas.add(list[i]);
         }
     }
 
     private void accumulate(Area area) {
         areas.add(area);
     }
 
     protected void appendNormalParagraph(Context cr, Extract extract) {
         final Area[] list;
 
         list = layoutAreaText(cr, extract, serifFace, false, false, false);
         accumulate(list);
     }
 
     protected void appendQuoteParagraph(Context cr, Extract extract) {
         final double savedLeft, savedRight;
         final Area[] list;
 
         savedLeft = leftMargin;
         savedRight = rightMargin;
 
         leftMargin += 45.0;
         rightMargin += 45.0;
 
         list = layoutAreaText(cr, extract, serifFace, false, false, false);
         accumulate(list);
 
         leftMargin = savedLeft;
         rightMargin = savedRight;
     }
 
    protected void appendAttributionParagraph(Context cr, Extract extract) {
         final FontDescription desc;
         final Typeface face;
         final double savedLeft, savedRight;
         final Area[] list;
 
         desc = serifFace.desc.copy();
         desc.setSize(7.0);
         face = new Typeface(cr, desc, 0.0);
 
         savedLeft = leftMargin;
         savedRight = rightMargin;
 
         leftMargin = pageWidth / 2 + 50.0;
         rightMargin += 10.0;
 
         list = layoutAreaText(cr, extract, face, false, false, false);
         accumulate(list);
 
         leftMargin = savedLeft;
         rightMargin = savedRight;
     }
 
     protected void appendProgramCode(Context cr, Extract entire) {
         final Area[] list;
 
         list = layoutAreaText(cr, entire, monoFace, true, false, false);
         accumulate(list);
     }
 
     // character
     private int previous;
 
     /**
      * Carry out smart typography replacements. Returns the number of
      * characters actually added, since some cases insert Unicode control
      * sequences.
      */
     private int translateAndAppend(final StringBuilder buf, final int ch, final boolean code) {
         int num, i;
 
         num = 0;
 
         if (code) {
             /*
              * Prevent Pango from doing line breaks on opening brackets.
              * U+2060 is the WORD JOINER character, similar to a zero width
              * space but with a more precise semantic.
              */
 
             if ((ch == '(') || (ch == '{') || (ch == '[')) {
                 buf.append('\u2060');
                 num++;
             }
 
             /*
              * Should we choose to replace spaces with non-breaking spaces in
              * code blocks, it's U+00A0. Anyway, now add the character.
              */
 
             buf.appendCodePoint(ch);
             num++;
         } else if (ch == '"') {
             /*
              * Replace normal quotes. When there's a space (or paragraph
              * start) preceeding the character we're considering, replace with
              * U+201C aka the LEFT DOUBLE QUOTATION MARK. Otherwise, close the
              * quotation with U+201D aka the RIGHT DOUBLE QUOTATION MARK.
              * Inspired by Smarty, of Markdown fame. Note that we don't do
              * this in preformatted code blocks
              */
 
             if (previous == '\0') {
                 buf.append('“');
                 num++;
             } else if (!Character.isWhitespace(previous)) {
                 buf.append('”');
                 num++;
             } else {
                 buf.append('“');
                 num++;
             }
         } else if (ch == '\'') {
             /*
              * Replace apostrophies. Unlike the double quote case above, we do
              * not replace matched pairs since we are NOT using single quotes
              * for quoted speech, and because there is no way to differentiate
              * "I heard him say 'wow' out loud" and the aspirated contraction
              * "There any 'round here?" We take the second case as more
              * important to get right, which requires a close quote only. The
              * relevant characters are U+2018 aka the LEFT SINGLE QUOTATION
              * MARK and U+2019 aka the RIGHT SINGLE QUOTATION MARK. We only
              * use the latter for typsetting contractions.
              */
             buf.append('’');
             num++;
         } else if (ch == ' ') {
             /*
              * If the preceeding sequence is " - " then replace the hyphen
              * with U+2014 EM DASH.
              */
             if (previous == '-') {
                 i = buf.length();
                 if ((i > 1) && (buf.charAt(i - 2) == ' ')) {
                     buf.setCharAt(i - 1, '\u2014');
                 }
             }
             buf.append(' ');
             num++;
         } else {
             /*
              * Normal character. Just add it.
              */
 
             buf.appendCodePoint(ch);
             num++;
         }
 
         previous = ch;
         return num;
     }
 
     /**
      * Render an Extract of text in the given Typeface into a TextArea object.
      * 
      * Fancy typesetting character substitutions (smary quotes, etc) will
      * occur if not preformatted text.
      */
     protected final Area[] layoutAreaText(final Context cr, final Extract extract, final Typeface face,
             final boolean preformatted, final boolean centered, boolean error) {
         final Layout layout;
         final FontOptions options;
         final StringBuilder buf;
         final AttributeList list;
         int k;
         final int K;
         String str;
         LayoutLine line;
         final Area[] result;
         Rectangle rect;
         double x;
         Origin origin;
         Area area;
 
         if (extract == null) {
             return new Area[] {};
         }
 
         layout = new Layout(cr);
 
         options = new FontOptions();
         options.setHintMetrics(OFF);
         layout.getContext().setFontOptions(options);
 
         layout.setFontDescription(face.desc);
 
         layout.setWidth(pageWidth - (leftMargin + rightMargin));
         layout.setWrapMode(WrapMode.WORD_CHAR);
 
         buf = new StringBuilder();
 
         list = new AttributeList();
 
         previous = '\0';
 
         /*
          * Now iterate over the Spans, accumulating their characters, and
          * creating Attributes along the way.
          */
 
         // TODO replace with CharacterVisitor?
         extract.visit(new SpanVisitor() {
             private int offset = 0;
 
             public boolean visit(Span span) {
                 Markup format;
                 final int len;
                 int width, j;
                 boolean code;
                 String str;
 
                 format = span.getMarkup();
                 width = 0;
 
                 if (preformatted) {
                     code = true;
                 } else if ((format == Common.LITERAL) || (format == Common.FILENAME)) {
                     code = true;
                 } else {
                     code = false;
                 }
 
                 /*
                  * FIXME Use Span characters, not String!!!! Fixing this will
                  * involve refactoring MarkerSpan to store reference data in
                  * Markup instances, not Span text.
                  */
 
                 str = span.getText();
                 len = str.length();
 
                 for (j = 0; j < len; j++) {
                     width += translateAndAppend(buf, str.charAt(j), code);
                 }
 
                 for (Attribute attr : attributesForMarkup(span.getMarkup())) {
                     attr.setIndices(offset, width);
                     list.insert(attr);
                 }
 
                 offset += width;
 
                 return false;
             }
         });
 
         str = buf.toString();
         layout.setText(str);
         layout.setAttributes(list);
 
         /*
          * Finally, we can render the individual lines of the paragraph. We do
          * NOT use each line's logical extents! We keep the line spacing
          * consistent; it's up to the RenderEngine [subclass] and font choices
          * therein to ensure that the various markup being drawn stays between
          * the lines.
          */
 
         K = layout.getLineCount();
         result = new Area[K];
 
         for (k = 0; k < K; k++) {
             line = layout.getLineReadonly(k);
 
             if (!centered) {
                 x = leftMargin;
             } else {
                 rect = line.getExtentsLogical();
                 x = pageWidth / 2 - rect.getWidth() / 2;
             }
 
             origin = new Origin(folioIndex, seriesIndex, currentOffset);
             area = new TextArea(origin, x, face.lineHeight, face.lineAscent, line, error);
             result[k] = area;
 
             /*
              * Query the layoutline for it's width, thereby finding out where
              * the next Origin should be marked. This isn't 100% correct,
              * because the number of characters laid out is NOT the same as
              * the number of characters in the editor [due to our typography
              * changes]. But it's usually the same, and I'm not sure how we
              * can go from LayoutLine (start, width) pairs to actual (Segment,
              * offset) unless we track the mapping between Segments'
              * TextChains and the typography() result.
              */
 
             currentOffset += line.getLength();
         }
 
         return result;
     }
 
     /**
      * Take the Area[] and pour them into a Page[].
      */
     private void flowAreasIntoPages(Context cr) {
         final int I;
         int i, num;
         final double available;
         double cursor, request;
         Page page;
         Area area, footer;
         Origin origin;
 
         pages = new ArrayList<Page>(8);
         lookup = new TreeMap<Origin, Page>();
 
         I = areas.size();
         i = 0;
         num = 1;
 
         available = pageHeight - bottomMargin - footerHeight;
 
         while (i < I) {
             area = null; // hm
             page = new Page(num);
             cursor = topMargin;
 
             /*
              * Absorb whitespace if it turns up at the top of a new Page
              */
 
             while (i < I) {
                 area = areas.get(i);
                 if (area instanceof BlankArea) {
                     i++;
                     continue;
                 }
                 break;
             }
 
             origin = area.getOrigin();
             lookup.put(origin, page);
 
             /*
              * Flow Areas onto the Page until we run out of room.
              */
 
             while (i < I) {
                 area = areas.get(i);
 
                 if (area instanceof PageBreakArea) {
                     i++;
                     break;
                 }
 
                 request = area.getHeight();
 
                 if (cursor + request > available) {
                     break;
                 }
 
                 page.append(cursor, area);
 
                 cursor += request;
                 i++;
             }
 
             /*
              * Finally, create a footer and add it to the end of the Page.
              */
 
             footer = layoutAreaFooter(cr, num);
             page.append(available, footer);
 
             /*
              * Accumulate the Page, then end the loop.
              */
 
             pages.add(page);
 
             num++;
         }
     }
 
     private static final Attribute[] empty = new Attribute[] {};
 
     /*
      * This is just a placeholder... move to rendering engine once we have
      * such things
      */
     private Attribute[] attributesForMarkup(Markup m) {
         if (m == null) {
             return empty;
         }
         if (m instanceof Common) {
             if (m == Common.ITALICS) {
                 return new Attribute[] {
                     new StyleAttribute(Style.ITALIC),
                 };
             } else if (m == Common.BOLD) {
                 return new Attribute[] {
                     new WeightAttribute(Weight.BOLD),
                 };
             } else if (m == Common.FILENAME) {
                 return new Attribute[] {
                         new FontDescriptionAttribute(monoFace.desc), new StyleAttribute(Style.ITALIC),
                 };
             } else if (m == Common.TYPE) {
                 return new Attribute[] {
                     new FontDescriptionAttribute(sansFace.desc),
                 };
             } else if (m == Common.FUNCTION) {
                 return new Attribute[] {
                     new FontDescriptionAttribute(monoFace.desc),
                 };
             } else if (m == Common.LITERAL) {
                 return new Attribute[] {
                     new FontDescriptionAttribute(monoFace.desc),
                 };
             } else if (m == Common.APPLICATION) {
                 return new Attribute[] {
                     new WeightAttribute(Weight.BOLD),
                 };
             } else if (m == Common.COMMAND) {
                 return new Attribute[] {
                         new FontDescriptionAttribute(monoFace.desc),
                         new WeightAttribute(Weight.SEMIBOLD),
                         new ForegroundColorAttribute(0.1, 0.1, 0.1),
                 };
             } else if (m == Common.HIGHLIGHT) {
                 return new Attribute[] {
                     new BackgroundColorAttribute(1.0, 1.0, 0.0),
                 };
             }
 
         } else if (m instanceof Preformat) {
             if (m == Preformat.USERINPUT) {
                 return empty;
             }
         } else if (m instanceof Special) {
             if (m == Special.NOTE) {
                 return new Attribute[] {
                         new SizeAttribute(4.0), new RiseAttribute(4.5),
                 };
             } else if (m == Special.CITE) {
                 return empty;
             }
         }
 
         throw new IllegalArgumentException("\n" + "Translation of " + m + " not yet implemented");
     }
 
     /**
      * Given a Series representing the Segments in a chapter or article,
      * instruct this Widget to render a preview of them.
      */
     /*
      * This will need refinement, obviously, once we start having live preview
      * and start dealing with multiple pages.
      */
     void renderSeries(Series series) {
 
     }
 
     /*
      * A series of getters for the calculated page dimension properties.
      * FUTURE should this become embedded in a wrapper object?
      */
     public double getPageWidth() {
         return pageWidth;
     }
 
     public double getPageHeight() {
         return pageHeight;
     }
 
     public double getMarginTop() {
         return topMargin;
     }
 
     public double getMarginLeft() {
         return leftMargin;
     }
 
     public double getMarginRight() {
         return rightMargin;
     }
 
     public double getMarginBottom() {
         return bottomMargin;
     }
 
     protected Area layoutAreaFooter(Context cr, int pageNumber) {
         final Layout layout;
         final Rectangle ink;
         final LayoutLine line;
 
         layout = new Layout(cr);
         layout.setFontDescription(serifFace.desc);
         layout.setText(Integer.toString(pageNumber));
         ink = layout.getExtentsInk();
 
         // switch to a layout, not just a line?
         line = layout.getLineReadonly(0);
         return new TextArea(null, pageWidth - rightMargin - ink.getWidth(), footerHeight,
                 serifFace.lineAscent, line, false);
 
     }
 
     protected void appendExternalGraphic(final Context cr, final String source) {
         final String parent, filename;
         final Pixbuf pixbuf;
         final TextChain chain;
         final Extract extract;
         final Area image;
 
         parent = manuscript.getDirectory();
         filename = parent + "/" + source;
 
         try {
             pixbuf = new Pixbuf(filename);
         } catch (FileNotFoundException e) {
             chain = new TextChain();
             chain.append(createSpan("image" + "\n", null));
             chain.append(createSpan(filename, Common.FILENAME));
             chain.append(createSpan("\n" + "not found", null));
             extract = chain.extractAll();
             appendErrorParagraph(cr, extract);
             return;
         }
 
         image = layoutAreaImage(cr, pixbuf);
         accumulate(image);
     }
 
     /**
      * Show a message (in red) indicating a processing problem.
      */
     protected void appendErrorParagraph(Context cr, Extract extract) {
         final Area[] list;
 
         list = layoutAreaText(cr, extract, sansFace, false, true, true);
         accumulate(list);
     }
 
     /*
      * Indentation copied from drawQuoteParagraph(). And face setting copied
      * from drawHeading(). Both of these should probably be abstracted.
      */
     protected void appendCitationParagraph(Context cr, Extract extract) {
         final double savedLeft, savedRight;
         final FontDescription desc;
         final Typeface face;
         final Area[] list;
 
         savedLeft = leftMargin;
         savedRight = rightMargin;
 
         leftMargin += 45.0;
         rightMargin += 45.0;
 
         desc = serifFace.desc.copy();
         desc.setStyle(Style.ITALIC);
         face = new Typeface(cr, desc, 0.0);
 
         list = layoutAreaText(cr, extract, face, false, true, false);
         accumulate(list);
 
         leftMargin = savedLeft;
         rightMargin = savedRight;
     }
 
     /**
      * If the image is wider than the margins it will be scaled down.
      */
     protected final Area layoutAreaImage(final Context cr, final Pixbuf pixbuf) {
         final double width, height;
         final double available, scaleFactor, request;
         final double leftCorner;
         final Origin origin;
         final Area area;
 
         width = pixbuf.getWidth();
         height = pixbuf.getHeight();
 
         available = pageWidth - rightMargin - leftMargin;
 
         if (width > available) {
             scaleFactor = available / width;
             leftCorner = leftMargin;
         } else {
             scaleFactor = 1.0;
             leftCorner = pageWidth / 2 - width / 2;
         }
         request = height * scaleFactor;
 
         origin = new Origin(folioIndex, seriesIndex, 0);
         area = new ImageArea(origin, leftCorner, request, pixbuf, scaleFactor);
         return area;
     }
 }
