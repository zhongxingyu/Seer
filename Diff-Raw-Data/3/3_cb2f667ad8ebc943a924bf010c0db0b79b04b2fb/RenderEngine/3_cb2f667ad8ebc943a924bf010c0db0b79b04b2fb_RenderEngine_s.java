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
 import java.lang.reflect.Constructor;
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
 import parchment.format.RendererNotFoundException;
 import parchment.format.Stylesheet;
 import parchment.format.UnsupportedValueException;
 import quill.client.ApplicationException;
 import quill.textbase.AttributionSegment;
 import quill.textbase.ChapterSegment;
 import quill.textbase.Common;
 import quill.textbase.DivisionSegment;
 import quill.textbase.EndnoteSegment;
 import quill.textbase.Extract;
 import quill.textbase.Folio;
 import quill.textbase.HeadingSegment;
 import quill.textbase.ImageSegment;
 import quill.textbase.LeaderSegment;
 import quill.textbase.ListitemSegment;
 import quill.textbase.Markup;
 import quill.textbase.NormalSegment;
 import quill.textbase.Origin;
 import quill.textbase.PoeticSegment;
 import quill.textbase.Preformat;
 import quill.textbase.PreformatSegment;
 import quill.textbase.QuoteSegment;
 import quill.textbase.ReferenceSegment;
 import quill.textbase.Segment;
 import quill.textbase.Series;
 import quill.textbase.Span;
 import quill.textbase.SpanVisitor;
 import quill.textbase.Special;
 import quill.textbase.SpecialSegment;
 import quill.textbase.TextChain;
 
 import static org.freedesktop.cairo.HintMetrics.OFF;
 import static org.freedesktop.cairo.HintStyle.NONE;
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
 /*
  * There's a bit too much in this class. It now seems to be both the actual
  * layout engine, as well as the harness for driving a rendering run. Adding
  * caching to this makes this even worse.
  */
 public abstract class RenderEngine
 {
     private RenderSettings settings;
 
     private double pageWidth;
 
     private double pageHeight;
 
     private double topMargin;
 
     private double bottomMargin;
 
     private double leftMargin;
 
     private double rightMargin;
 
     private Folio folio;
 
     Typeface sansFace;
 
     Typeface serifFace;
 
     Typeface monoFace;
 
     Typeface headingFace;
 
     /**
      * Woarkaround the bug that Variant.SMALL_CAPS doesn't actually work; and
      * meanwhile, "Linux Libertine O" has small caps font in the Private Use
      * area. See {@link LibertineTypography#toSmallCase(int)}.
      */
     private Typeface smallFace;
 
     /**
      * This chapter's content, as prepared into Areas.
      */
     private ArrayList<Area> areas;
 
     /**
      * This chapter's content, as flowed into Pages.
      */
     private ArrayList<Page> pages;
 
     /**
      * After a render, what page are we on?
      */
     private int pageIndex;
 
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
      * Construct a new RenderEngine. Call {@link #configure(Stylesheet)
      * configure()} with a Stylesheet to setup, then {@link #render(Context)
      * render()} to actually draw. A RenderEngine is resuable so long as the
      * Stylesheet doesn't change.
      */
     protected RenderEngine() {}
 
     private void configure(Stylesheet style) throws UnsupportedValueException {
         final PaperSize paper;
 
         settings = new RenderSettings(style);
 
         paper = settings.getPaper();
         pageWidth = paper.getWidth(Unit.POINTS);
         pageHeight = paper.getHeight(Unit.POINTS);
 
         topMargin = settings.getMarginTop();
         bottomMargin = settings.getMarginBottom();
         leftMargin = settings.getMarginLeft();
         rightMargin = settings.getMarginRight();
     }
 
     public RenderSettings getRenderSettings() {
         return settings;
     }
 
     /**
      * Given a Context, have the rendering engine to draw to it. This assumes
      * that the target Surface either a) has the size as the PaperSize passed
      * to the constructor, or b) has been scaled to that size.
      */
     public void render(final Context cr, final Folio folio) {
         if (folio == null) {
             return;
         }
         this.folio = folio;
 
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
     public void render(final Context cr, final Folio folio, final int pageNum) {
         if (folio == null) {
             return;
         }
         this.folio = folio;
 
         synchronized (this) {
             specifyFonts(cr);
             processSegmentsIntoAreas(cr);
             flowAreasIntoPages(cr);
             renderSinglePage(cr, pageNum);
         }
     }
 
     public void render(Context cr, Folio folio, Origin cursor) {
         if (folio == null) {
             return;
         }
         this.folio = folio;
 
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
 
     private void renderSinglePage(final Context cr, final int pageNum) {
         final Surface surface;
         final Page page;
 
         surface = cr.getTarget();
 
         page = pages.get(pageNum - 1);
         page.render(cr);
 
         surface.finish();
 
         pageIndex = pageNum - 1;
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
 
         pageIndex = page.getPageNumber() - 1;
     }
 
     protected void specifyFonts(final Context cr) {
         FontDescription desc;
         final double size;
 
         desc = settings.getFontSerif();
         size = desc.getSize();
         serifFace = new Typeface(cr, desc, 0.2);
 
         desc = settings.getFontSans();
         sansFace = new Typeface(cr, desc, 0.0);
 
         desc = settings.getFontMono();
         monoFace = new Typeface(cr, desc, 0.0);
 
         desc = settings.getFontHeading();
         headingFace = new Typeface(cr, desc, 0.0);
 
         desc = new FontDescription("Linux Libertine O");
         desc.setSize(size);
         smallFace = new Typeface(cr, desc, 0.0);
 
         cr.setSource(0.0, 0.0, 0.0);
     }
 
     void processSegmentsIntoAreas(final Context cr) {
         int i, j, k;
         int I, J;
         Series series;
         Segment segment;
         Extract entire;
         TextChain chain;
         Extract[] paras;
         String filename;
         ArrayList<Segment>[] endnotes;
         final ArrayList<Segment> references;
         String label, type;
 
         I = folio.size();
         areas = new ArrayList<Area>(64);
 
         references = new ArrayList<Segment>(4);
 
         endnotes = new ArrayList[I];
 
         /*
          * Accumulate ReferenceSegments so they can later be output when a
          * SpecialSegment is encountered.
          */
 
         for (i = 0; i < I; i++) {
             series = folio.getSeries(i);
 
             J = series.size();
             for (j = 0; j < J; j++) {
                 segment = series.getSegment(j);
 
                 if (segment instanceof ReferenceSegment) {
                     references.add(segment);
                 }
             }
         }
 
         /*
          * Now process document
          */
 
         for (i = 0; i < I; i++) {
             series = folio.getSeries(i);
             folioIndex = i;
 
             if (i > 0) {
                 appendPageBreak(cr);
             }
 
             endnotes[i] = new ArrayList<Segment>(4);
 
             J = series.size();
             for (j = 0; j < J; j++) {
                 seriesIndex = j;
                 currentOffset = 0;
 
                 segment = series.getSegment(j);
                 entire = segment.getEntire();
 
                 if (segment instanceof ChapterSegment) {
                     label = segment.getImage();
                     appendTitle(cr, label, entire, 2.0, false);
                 } else if (segment instanceof DivisionSegment) {
                     appendWhitespace(cr, 100.0);
 
                     label = segment.getImage();
                     appendTitle(cr, label, entire, 3.0, true);
 
                     appendWhitespace(cr, 20.0);
                 } else if (segment instanceof HeadingSegment) {
                     appendSegmentBreak(cr);
 
                     label = segment.getImage();
                     appendHeading(cr, label, entire);
                 } else if (segment instanceof PreformatSegment) {
                     appendSegmentBreak(cr);
                     appendProgramCode(cr, entire);
                 } else if (segment instanceof QuoteSegment) {
                     chain = new TextChain(entire);
                     paras = chain.extractParagraphs();
                     for (k = 0; k < paras.length; k++) {
                         appendParagraphBreak(cr);
                         appendQuoteParagraph(cr, paras[k]);
                     }
                 } else if (segment instanceof NormalSegment) {
                     chain = new TextChain(entire);
                     paras = chain.extractParagraphs();
                     for (k = 0; k < paras.length; k++) {
                         appendParagraphBreak(cr);
                         appendNormalParagraph(cr, paras[k]);
                     }
                 } else if (segment instanceof ListitemSegment) {
                     label = segment.getImage();
 
                     chain = new TextChain(entire);
                     paras = chain.extractParagraphs();
                     for (k = 0; k < paras.length; k++) {
                         appendParagraphBreak(cr);
                         appendListParagraph(cr, label, paras[k]);
 
                         label = "";
                     }
                 } else if (segment instanceof PoeticSegment) {
                     appendSegmentBreak(cr);
                     appendNormalParagraph(cr, entire);
                 } else if (segment instanceof AttributionSegment) {
                     appendSegmentBreak(cr);
                     appendAttributionParagraph(cr, entire);
                 } else if (segment instanceof ImageSegment) {
                     filename = segment.getImage();
                     appendSegmentBreak(cr);
                     appendExternalGraphic(cr, filename);
                     if (entire == null) {
                         continue;
                     }
                     appendSegmentBreak(cr);
                     appendCaptionParagraph(cr, entire);
                 } else if (segment instanceof EndnoteSegment) {
                     endnotes[i].add(segment);
                 } else if (segment instanceof ReferenceSegment) {
                     // already accumulated
                 } else if (segment instanceof LeaderSegment) {
                     appendSegmentBreak(cr);
                     appendLeader(cr, entire);
                 } else if (segment instanceof SpecialSegment) {
                     type = segment.getImage();
 
                     if (type.equals("endnotes")) {
                         processSpecialEndnotes(cr, endnotes);
                     } else if (type.equals("references")) {
                         processSpecialReferences(cr, references);
                     }
                 }
             }
 
             appendSegmentBreak(cr);
         }
     }
 
     void processSpecialEndnotes(final Context cr, ArrayList<Segment>[] endnotes) {
         int i, j;
         int I, J;
         Series series;
         Extract entire;
         Segment segment;
         String which, label;
 
         I = endnotes.length;
 
         /*
          * Now build the notes and references. This is somewhat hardcoded, but
          * choose "intelligent defaults" as Robert Collins says.
          */
 
         for (i = 0; i < I; i++) {
             J = endnotes[i].size();
 
             if (J == 0) {
                 continue;
             }
 
             /*
              * If there's a chapter title, then put up a bit of bold text with
              * that title. If there isn't a title we keep on rendering notes;
              * the assumption would thus seem to be that they've done
              * continuous numbering (or, maybe, the RenderEngine is doing
              * notes at chapter end not document end). Only bother if there's
              * > 1 chapter, though.
              */
 
             series = folio.getSeries(i);
 
             if ((I > 1) && (series.size() > 0)) {
                 segment = series.getSegment(0);
                 if (segment instanceof ChapterSegment) {
                     entire = segment.getEntire();
                     which = entire.getText();
                     appendSegmentBreak(cr);
                     appendNormalParagraph(cr, which, Common.BOLD);
                 }
             }
 
             for (j = 0; j < J; j++) {
                 appendSegmentBreak(cr);
 
                 segment = endnotes[i].get(j);
                 label = segment.getImage();
                 entire = segment.getEntire();
                 appendReferenceParagraph(cr, label, entire);
             }
         }
     }
 
     void processSpecialReferences(final Context cr, final ArrayList<Segment> references) {
         int j;
         final int J;
         Segment segment;
         Extract entire;
         String label;
 
         J = references.size();
         if (J > 0) {
             for (j = 0; j < J; j++) {
                 appendSegmentBreak(cr);
 
                 segment = references.get(j);
                 label = segment.getImage();
                 entire = segment.getEntire();
                 appendReferenceParagraph(cr, label, entire);
             }
         }
     }
 
     /**
      * Append the blank line that comes between Segment blocks. Contrast this
      * to appendParaBreak, the blank line that comes between paras.
      * 
      * @param cr
      */
     protected void appendSegmentBreak(Context cr) {
         appendBlankLine();
     }
 
     /**
      * Append the blank line that comes between Segment blocks. Contrast this
      * to appendSegmentBreak, the blank line that comes between blocks of
      * various types.
      * 
      * @param cr
      */
     protected void appendParagraphBreak(Context cr) {
         appendBlankLine();
     }
 
     private void appendBlankLine() {
         final Origin origin;
         final Area area;
         final double request;
 
         request = serifFace.lineHeight * 0.7;
 
         origin = new Origin(folioIndex, seriesIndex, currentOffset++);
         area = new BlankArea(origin, request);
         accumulate(area);
     }
 
     /**
      * @param cr
      */
     protected void appendPageBreak(Context cr) {
         final Origin origin;
         final Area area;
 
         origin = new Origin(folioIndex, seriesIndex, currentOffset);
         area = new PageBreakArea(origin);
         accumulate(area);
     }
 
     /**
      * @param cr
      */
     /*
      * We use an ImageArea rather than a BlankArea so that it doesn't get
      * absorbed by the flow logic.
      */
     protected void appendWhitespace(final Context cr, final double request) {
         final Origin origin;
         final Area area;
 
         origin = new Origin(folioIndex, seriesIndex, currentOffset);
         area = new ImageArea(origin, 0.0, request, null, 1.0);
         accumulate(area);
     }
 
     protected void appendHeading(Context cr, String label, Extract entire) {
         final Area area;
         final Area[] list;
         double width;
 
         if ((label != null) && (label.length() > 0)) {
             area = layoutAreaLabel(cr, label, headingFace, false);
             accumulate(area);
 
             width = area.getWidth();
 
             if (width + 6.0 < 31.0) {
                 width = 31.0;
             } else {
                 width += 6.0;
             }
         } else {
             width = 0.0;
         }
 
         list = layoutAreaText(cr, entire, headingFace, false, false, width, 1, false);
         accumulate(list);
     }
 
     /**
      * Create an Area for the label attribute of a Chapter or Heading. Not to
      * be confused with the labels of bullet lists; see
      * {@link #layoutAreaBullet(Context, String, Typeface, double)}. This
      * exists largely so that the returned Area can be queried for its width.
      * 
      * @param take
      *            Use <code>true</code> to cause this label to take its
      *            height, <code>false</code> to have this label be transient
      *            (on the assumption that the Area that it belongs to will
      *            take the necessary height).
      */
     protected Area layoutAreaLabel(final Context cr, final String label, final Typeface face,
             boolean take) {
         final Layout layout;
         final LayoutLine line;
         final Origin origin;
         final Area result;
         final Rectangle rect;
         final double height;
 
         layout = new Layout(cr);
         layout.setFontDescription(face.desc);
         layout.setText(label);
 
         line = layout.getLineReadonly(0);
 
         if (take) {
             rect = line.getExtentsLogical();
             height = rect.getHeight();
         } else {
             height = 0.0;
         }
 
         origin = new Origin(folioIndex, seriesIndex, 0);
         result = new TextArea(origin, leftMargin, height, face.lineAscent, line, false);
 
         return result;
     }
 
     protected void appendTitle(final Context cr, final String label, final Extract entire,
             final double multiplier, final boolean centered) {
         final FontDescription desc;
         final double size;
         final Typeface face;
         final Span span;
         final Extract extract;
         final Area area;
         Area[] list;
         double width;
 
         desc = headingFace.desc.copy();
         size = desc.getSize();
         desc.setSize(size * multiplier);
         face = new Typeface(cr, desc, 0.0);
 
         if (centered) {
             if ((label != null) && (label.length() > 0)) {
                 span = Span.createSpan(label, null);
                 extract = Extract.create(span);
                 list = layoutAreaText(cr, extract, face, false, true, 0.0, 1, false);
                 accumulate(list);
             }
 
             width = 0.0;
         } else {
             if ((label != null) && (label.length() > 0)) {
                 area = layoutAreaLabel(cr, label, face, false);
                 accumulate(area);
 
                 width = area.getWidth();
 
                 /*
                  * TODO This 31 is the same as appendHeading() and
                  * appendListParagraph(). In fact, excepting the multiplier
                  * this is the same code as appendHeading(). Refactor.
                  */
 
                 if (width + 6.0 < 31.0) {
                     width = 31.0;
                 } else {
                     width += 6.0 * multiplier;
                 }
             } else {
                 width = 0.0;
             }
         }
 
         list = layoutAreaText(cr, entire, face, false, centered, width, 1, false);
         accumulate(list);
     }
 
     protected void appendLeader(Context cr, Extract entire) {
         final FontDescription desc;
         final double size;
         final Typeface face;
         final Area[] list;
 
         desc = serifFace.desc.copy();
         desc.setWeight(Weight.BOLD);
         size = desc.getSize();
         desc.setSize(size * 1.2);
         face = new Typeface(cr, desc, 0.0);
 
         list = layoutAreaText(cr, entire, face, false, true, 0.0, 1, false);
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
 
     protected void appendNormalParagraph(final Context cr, final String text, final Markup markup) {
         final Span span;
         final Extract extract;
 
         span = Span.createSpan(text, markup);
         extract = Extract.create(span);
 
         appendNormalParagraph(cr, extract);
     }
 
     protected void appendNormalParagraph(Context cr, Extract extract) {
         final Area[] list;
         final double indent;
         final int spacing;
 
         indent = getNormalIndent();
         spacing = getNormalSpacing();
 
         list = layoutAreaText(cr, extract, serifFace, false, false, indent, spacing, false);
         accumulate(list);
     }
 
     /**
      * Override this if you want to indent the paragraph...
      */
     protected double getNormalIndent() {
         return 0.0;
     }
 
     /**
      * Override this if you want to change the spacing between lines in a
      * paragraph... Acceptable values are {1,2}.
      */
     protected int getNormalSpacing() {
         return 1;
     }
 
     protected void appendQuoteParagraph(Context cr, Extract extract) {
         final double savedLeft, savedRight;
         final Area[] list;
         final double indent;
         final int spacing;
 
         savedLeft = leftMargin;
         savedRight = rightMargin;
 
         leftMargin += 45.0;
         rightMargin += 45.0;
 
         indent = getQuoteIndent();
         spacing = getQuoteSpacing();
 
         list = layoutAreaText(cr, extract, serifFace, false, false, indent, spacing, false);
         accumulate(list);
 
         leftMargin = savedLeft;
         rightMargin = savedRight;
     }
 
     protected double getQuoteIndent() {
         return 0.0;
     }
 
     protected int getQuoteSpacing() {
         return 1;
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
 
         list = layoutAreaText(cr, extract, face, false, false, 0.0, 1, false);
         accumulate(list);
 
         leftMargin = savedLeft;
         rightMargin = savedRight;
     }
 
     protected void appendProgramCode(Context cr, Extract entire) {
         final Area[] list;
 
         list = layoutAreaText(cr, entire, monoFace, true, false, 0.0, 1, false);
         accumulate(list);
     }
 
     protected void appendListParagraph(final Context cr, final String label, final Extract extract) {
         final Area area;
         final Area[] list;
         final double savedLeft;
 
         /*
          * Label
          */
 
         area = layoutAreaBullet(cr, label, serifFace, 9.0);
         accumulate(area);
 
         /*
          * Body. There's an interplay between bullet intent, body intent, and
          * still being a bit less than the block quote indentation. This
          * should probably be settable by subclasses.
          */
 
         savedLeft = leftMargin;
         leftMargin += 31.0;
 
         list = layoutAreaText(cr, extract, serifFace, false, false, 0.0, 1, false);
         accumulate(list);
 
         leftMargin = savedLeft;
     }
 
     protected void appendReferenceParagraph(final Context cr, final String label, final Extract extract) {
         final Area area;
         final Area[] list;
         final double savedLeft;
 
         /*
          * Label
          */
 
         area = layoutAreaBullet(cr, label, serifFace, 0.0);
         accumulate(area);
 
         /*
          * Body. 25 points is suitable for [99] and (barely) enough for [999].
          */
 
         savedLeft = leftMargin;
         leftMargin += 25.0;
 
         list = layoutAreaText(cr, extract, serifFace, false, false, 0.0, 1, false);
         accumulate(list);
 
         leftMargin = savedLeft;
     }
 
     private Area layoutAreaBullet(final Context cr, final String label, final Typeface face,
             final double position) {
         final Layout layout;
         final LayoutLine line;
         final Area area;
 
         layout = new Layout(cr);
         layout.setFontDescription(face.desc);
         layout.setText(label);
 
         line = layout.getLineReadonly(0);
 
         /*
          * Passing height 0 means that no vertical space will be consumed by
          * this Area; the ascent is stil needed to position the Cairo point
          * before drawing the LayoutLine.
          */
         area = new TextArea(null, leftMargin + position, 0.0, serifFace.lineAscent, line, false);
 
         return area;
     }
 
     // character
     private int previous;
 
     /**
      * Carry out smart typography replacements. Returns the number of
      * characters actually added, since some cases insert Unicode control
      * sequences.
      */
     private int translateAndAppend(final StringBuilder buf, final int ch, final Markup format,
             final boolean preformatted) {
         int num, i, tr;
         boolean code, small;
 
         code = false;
         small = false;
         num = 0;
 
         /*
          * If it's preformatted, then automatically assume code conditions.
          * Otherwise, setup switches based on Markup type.
          */
 
         if (preformatted) {
             code = true;
         } else if ((format == Common.LITERAL) || (format == Common.FILENAME)) {
             code = true;
         } else if (format == Common.ACRONYM) {
             small = true;
         }
 
         /*
          * Perform translations.
          */
 
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
         } else if (small) {
             tr = LibertineTypography.toSmallCase(ch);
 
             buf.appendCodePoint(tr);
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
             final boolean preformatted, final boolean centered, final double indent, final int spacing,
             boolean error) {
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
         options.setHintStyle(NONE);
         layout.getContext().setFontOptions(options);
 
         layout.setFontDescription(face.desc);
 
         layout.setWidth(pageWidth - (leftMargin + rightMargin));
         layout.setWrapMode(WrapMode.WORD_CHAR);
         layout.setIndent(indent); // see note below
 
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
                 final Markup format;
                 final int len;
                 int width, j;
                 String str;
 
                 format = span.getMarkup();
                 width = 0;
 
                 /*
                  * FIXME Use Span characters, not String!!!! Fixing this will
                  * involve refactoring MarkerSpan to store reference data in
                  * Markup instances, not Span text.
                  */
 
                 str = span.getText();
                 len = str.length();
 
                 for (j = 0; j < len; j++) {
                     width += translateAndAppend(buf, str.charAt(j), format, preformatted);
                 }
 
                 for (Attribute attr : attributesForMarkup(format)) {
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
 
         if (spacing == 1) {
             result = new Area[K];
         } else if (spacing == 2) {
             result = new Area[2 * K - 1];
         } else {
             throw new AssertionError();
         }
 
         for (k = 0; k < K; k++) {
             line = layout.getLineReadonly(k);
 
             /*
              * If you've told the Layout you're indenting, you still have to
              * manually offset the LayouLine's drawing point. This is weird,
              * but whatever.
              */
 
             if (!centered) {
                 x = leftMargin;
                 if ((k == 0) && (indent > 0.0)) {
                     x += indent;
                 }
             } else {
                 rect = line.getExtentsLogical();
                 x = pageWidth / 2 - rect.getWidth() / 2;
                 if ((k == 0) && (indent > 0.0)) {
                     x += indent;
                 }
             }
 
             origin = new Origin(folioIndex, seriesIndex, currentOffset);
             area = new TextArea(origin, x, face.lineHeight, face.lineAscent, line, error);
 
             /*
              * Handle double spacing, if that's specified.
              */
 
             if (spacing == 1) {
                 result[k] = area;
             } else if (spacing == 2) {
                 result[2 * k] = area;
                 if (k > 0) {
                     result[2 * k - 1] = new BlankArea(origin, face.lineHeight * 0.7);
                 }
             }
 
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
         int i, num, j;
         double headerHeight, footerHeight, available;
         double cursor, request;
         Page page;
         Area area;
         Area[] header, footer;
         Origin origin;
 
         pages = new ArrayList<Page>(8);
         lookup = new TreeMap<Origin, Page>();
 
         I = areas.size();
         i = 0;
         num = 1;
 
         while (i < I) {
             area = null; // hm
             page = new Page(num);
             headerHeight = 0;
             footerHeight = 0;
 
             /*
              * Create a header (if there is one), and add it to the Page.
              */
 
             header = layoutAreaHeader(cr, num);
 
             for (j = 0; j < header.length; j++) {
                 area = header[j];
 
                 if (area == null) {
                     continue;
                 }
                 headerHeight = serifFace.lineHeight + 5.0;
                 page.append(topMargin, area);
             }
 
             /*
              * Create a footer (if there is to be one).
              */
 
             footer = layoutAreaFooter(cr, num);
             for (j = 0; j < footer.length; j++) {
                 area = footer[j];
 
                 if (area == null) {
                     continue;
                 }
                 footerHeight = serifFace.lineHeight;
                 page.append(pageHeight - bottomMargin - footerHeight, area);
             }
 
             /*
              * available is the y position when we run out of space (ie not
              * scalar, which is why topMargin and headerHeight are not
              * included).
              */
 
             available = pageHeight - bottomMargin - footerHeight;
             cursor = topMargin + headerHeight;
 
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
                     new FontDescriptionAttribute(monoFace.desc),
                     new StyleAttribute(Style.ITALIC),
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
             } else if (m == Common.PROJECT) {
                 return new Attribute[] {
                     new FontDescriptionAttribute(sansFace.desc),
                     new WeightAttribute(Weight.BOLD),
                     new ForegroundColorAttribute(0.3, 0.3, 0.3),
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
             } else if (m == Common.TITLE) {
                 return new Attribute[] {
                     new StyleAttribute(Style.ITALIC),
                 };
             } else if (m == Common.KEYBOARD) {
                 return new Attribute[] {
                     new WeightAttribute(Weight.BOLD),
                 };
             } else if (m == Common.ACRONYM) {
                 /*
                  * Originally we called createSmallCaps() to map, but now we
                  * are mapping acronym upper case characters and numerals
                  * directly to the Linux Libertine font's Private Use area
                  * block. Absolutely need to make sure that we have that font
                  * at play.
                  */
                 return new Attribute[] {
                     new FontDescriptionAttribute(smallFace.desc),
                 };
             }
 
         } else if (m instanceof Preformat) {
             if (m == Preformat.USERINPUT) {
                 return empty;
             }
         } else if (m instanceof Special) {
             if (m == Special.NOTE) {
                 return new Attribute[] {
                     new SizeAttribute(4.5),
                     new RiseAttribute(4.5),
                 };
             } else if (m == Special.CITE) {
                 return empty;
             }
         }
 
         throw new IllegalArgumentException("\n" + "Translation of " + m + " not yet implemented");
     }
 
     /*
      * A series of getters for the calculated page dimension properties.
      * FUTURE should this become embedded in a wrapper object?
      */
     /**
      * The page width of the target area, in points.
      */
     public double getPageWidth() {
         return pageWidth;
     }
 
     /**
      * The page height of the target area, in points.
      */
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
 
     /**
      * The number of pages in this document, as rendered.
      */
     public int getPageCount() {
         return pages.size();
     }
 
     /**
      * The page number currently being shown (in the case of a single page
      * render). These are for internal use, and are 0 origin.
      */
     public int getPageIndex() {
         return pageIndex;
     }
 
     /**
      * Make the Folio currently being rendered available to subclasses.
      */
     protected Folio getFolio() {
         return folio;
     }
 
     /**
      * Return the set of Areas making up the footer. You CAN return null
      * elements.
      */
     protected Area[] layoutAreaFooter(final Context cr, final int pageNumber) {
         final Area right, center, left;
 
         right = layoutAreaFooterLeft(cr, pageNumber);
         center = layoutAreaFooterCenter(cr, pageNumber);
         left = layoutAreaFooterRight(cr, pageNumber);
 
         return new Area[] {
             right,
             center,
             left
         };
     }
 
     private Area layoutAreaFooterLeft(final Context cr, final int pageNumber) {
         final Layout layout;
         final LayoutLine line;
         final Area area;
 
         layout = getFooterLeft(cr, pageNumber);
         if (layout == null) {
             return null;
         }
 
         line = layout.getLineReadonly(0);
 
         area = new TextArea(null, leftMargin, serifFace.lineHeight, serifFace.lineAscent, line, false);
 
         return area;
     }
 
     private Area layoutAreaFooterCenter(final Context cr, final int pageNumber) {
         final Layout layout;
         final Rectangle ink;
         final LayoutLine line;
         final Area area;
 
         layout = getFooterCenter(cr, pageNumber);
         if (layout == null) {
             return null;
         }
 
         line = layout.getLineReadonly(0);
         ink = line.getExtentsInk();
 
         area = new TextArea(null, (pageWidth - ink.getWidth()) / 2.0, serifFace.lineHeight,
                 serifFace.lineAscent, line, false);
 
         return area;
     }
 
     private Area layoutAreaFooterRight(final Context cr, final int pageNumber) {
         final Layout layout;
         final LayoutLine line;
         final Rectangle ink;
         final Area area;
 
         layout = getFooterRight(cr, pageNumber);
         if (layout == null) {
             return null;
         }
 
         line = layout.getLineReadonly(0);
         ink = line.getExtentsInk();
 
         area = new TextArea(null, pageWidth - rightMargin - ink.getWidth(), serifFace.lineHeight,
                 serifFace.lineAscent, line, false);
 
         return area;
     }
 
     /**
      * The text on the left-hand of the footer. Only the first line of the
      * Layout will be used. Return <code>null</code> if you want to skip this
      * footer.
      * 
      * @param cr
      * @param pageNumber
      */
     protected Layout getFooterLeft(final Context cr, final int pageNumber) {
         return null;
     }
 
     /**
      * The text at the center of the footer. Only the first line of the Layout
      * will be used. Return <code>null</code> if you want to skip this footer.
      * 
      * @param cr
      * @param pageNumber
      */
     protected Layout getFooterCenter(final Context cr, final int pageNumber) {
         return null;
     }
 
     /**
      * The right-hand text for the footer. Only the first line of the Layout
      * will be used. Return <code>null</code> if you want to skip this footer.
      * 
      * @param cr
      * @param pageNumber
      */
     protected Layout getFooterRight(final Context cr, final int pageNumber) {
         return null;
     }
 
     /**
      * Return the set of Areas making up the header. You CAN return null
      * elements.
      */
     protected Area[] layoutAreaHeader(final Context cr, final int pageNumber) {
         final Area right, center, left;
 
         right = layoutAreaHeaderLeft(cr, pageNumber);
         center = layoutAreaHeaderCenter(cr, pageNumber);
         left = layoutAreaHeaderRight(cr, pageNumber);
 
         return new Area[] {
             right,
             center,
             left
         };
     }
 
     private Area layoutAreaHeaderLeft(final Context cr, final int pageNumber) {
         final Layout layout;
         final LayoutLine line;
         final Area area;
 
         layout = getHeaderLeft(cr, pageNumber);
         if (layout == null) {
             return null;
         }
 
         line = layout.getLineReadonly(0);
 
         area = new TextArea(null, leftMargin, serifFace.lineHeight, serifFace.lineAscent, line, false);
 
         return area;
     }
 
     private Area layoutAreaHeaderCenter(final Context cr, final int pageNumber) {
         final Layout layout;
         final Rectangle ink;
         final LayoutLine line;
         final Area area;
 
         layout = getHeaderCenter(cr, pageNumber);
         if (layout == null) {
             return null;
         }
 
         line = layout.getLineReadonly(0);
         ink = line.getExtentsInk();
 
         area = new TextArea(null, (pageWidth - ink.getWidth()) / 2.0, serifFace.lineHeight,
                 serifFace.lineAscent, line, false);
 
         return area;
     }
 
     private Area layoutAreaHeaderRight(final Context cr, final int pageNumber) {
         final Layout layout;
         final LayoutLine line;
         final Rectangle ink;
         final Area area;
 
         layout = getHeaderRight(cr, pageNumber);
         if (layout == null) {
             return null;
         }
 
         line = layout.getLineReadonly(0);
         ink = line.getExtentsInk();
 
         area = new TextArea(null, pageWidth - rightMargin - ink.getWidth(), serifFace.lineHeight,
                 serifFace.lineAscent, line, false);
 
         return area;
     }
 
     /**
      * The text on the left-hand of the header. Only the first line of the
      * Layout will be used. Return <code>null</code> if you want to skip this
      * one.
      * 
      * @param cr
      * @param pageNumber
      */
     protected Layout getHeaderLeft(final Context cr, final int pageNumber) {
         return null;
     }
 
     /**
      * The text at the center of the header. Only the first line of the Layout
      * will be used. Return <code>null</code> if you want to skip this one.
      * 
      * @param cr
      * @param pageNumber
      */
     protected Layout getHeaderCenter(final Context cr, final int pageNumber) {
         return null;
     }
 
     /**
      * The right-hand text for the header. Only the first line of the Layout
      * will be used. Return <code>null</code> if you want to skip this one.
      * 
      * @param cr
      * @param pageNumber
      */
     protected Layout getHeaderRight(final Context cr, final int pageNumber) {
         return null;
     }
 
     protected void appendExternalGraphic(final Context cr, final String source) {
         final Manuscript manuscript;
         final String parent, filename;
         final Pixbuf pixbuf;
         final TextChain chain;
         final Extract extract;
         final Area image;
 
         manuscript = folio.getManuscript();
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
 
         list = layoutAreaText(cr, extract, sansFace, false, true, 0.0, 1, true);
         accumulate(list);
     }
 
     /*
      * Indentation copied from drawQuoteParagraph(). And face setting copied
      * from drawHeading(). Both of these should probably be abstracted.
      */
     protected void appendCaptionParagraph(Context cr, Extract extract) {
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
 
         list = layoutAreaText(cr, extract, face, false, true, 0.0, 1, false);
         accumulate(list);
 
         leftMargin = savedLeft;
         rightMargin = savedRight;
     }
 
     /**
      * If the image is wider than the margins it will be scaled down.
      * 
      * @param cr
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
 
     /**
      * Create an instance of the RenderEngine specified by this presentation
      * element.
      */
     @SuppressWarnings("unchecked")
     public static RenderEngine createRenderer(final Stylesheet style) throws ApplicationException {
         final String renderer;
         final Class<? extends RenderEngine> type;
         final Constructor<RenderEngine> constructor;
         final RenderEngine result;
 
         renderer = style.getRendererClass();
         try {
             type = (Class<? extends RenderEngine>) Class.forName(renderer);
         } catch (ClassNotFoundException e) {
             throw new RendererNotFoundException(renderer);
         }
 
         try {
             constructor = (Constructor<RenderEngine>) type.getConstructor();
             result = constructor.newInstance();
         } catch (Exception e) {
             e.printStackTrace();
             throw new AssertionError();
         }
 
         result.configure(style);
 
         return result;
     }
 }
