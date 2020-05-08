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
 package quill.ui;
 
 import java.util.ArrayList;
 
 import org.freedesktop.cairo.Antialias;
 import org.freedesktop.cairo.Context;
 import org.gnome.gdk.EventExpose;
import org.gnome.glib.Glib;
 import org.gnome.gtk.Alignment;
 import org.gnome.gtk.Button;
 import org.gnome.gtk.DrawingArea;
 import org.gnome.gtk.HBox;
 import org.gnome.gtk.Image;
 import org.gnome.gtk.Label;
 import org.gnome.gtk.PolicyType;
 import org.gnome.gtk.ReliefStyle;
 import org.gnome.gtk.ScrolledWindow;
 import org.gnome.gtk.SizeGroup;
 import org.gnome.gtk.VBox;
 import org.gnome.gtk.Widget;
 import org.gnome.pango.EllipsizeMode;
 
 import parchment.format.Chapter;
 import parchment.format.Manuscript;
 import parchment.format.Metadata;
 import quill.client.ApplicationException;
 import quill.textbase.CharacterVisitor;
 import quill.textbase.ComponentSegment;
 import quill.textbase.Extract;
 import quill.textbase.Folio;
 import quill.textbase.HeadingSegment;
 import quill.textbase.ImageSegment;
 import quill.textbase.Markup;
 import quill.textbase.PreformatSegment;
 import quill.textbase.Segment;
 import quill.textbase.Series;
 import quill.textbase.TextChain;
 
 import static org.gnome.glib.Glib.markupEscapeText;
 import static org.gnome.gtk.SizeGroupMode.HORIZONTAL;
 
 /**
  * A table of contents style interface to navigate the document. This could be
  * a lot fancier, but the tree of buttons conveys the idea.
  * 
  * @author Andrew Cowie
  */
 /*
  * TODO This is just a demonstration placeholder. It will need to be made live
  * and reactive to changes to the DataLayer. The code to generate the
  * "compressed lines" is horrendous.
  */
 class OutlineWidget extends ScrolledWindow
 {
     private final ScrolledWindow scroll;
 
     private VBox top;
 
     private Folio folio;
 
     private SizeGroup group;
 
     private Label wordsDocument;
 
     private Label[] wordsChapter;
 
     private int countDocument;
 
     private int[] countChapter;
 
     private final PrimaryWindow primary;
 
     public OutlineWidget(PrimaryWindow window) {
         super();
         scroll = this;
 
         top = new VBox(false, 0);
         scroll.addWithViewport(top);
         scroll.setPolicy(PolicyType.NEVER, PolicyType.ALWAYS);
 
         folio = null;
         primary = window;
     }
 
     private void buildOutline() {
         final Metadata meta;
         final Manuscript manuscript;
         final Alignment align;
         final int J;
         Chapter chapter;
         Series series;
         HBox box;
         Segment segment;
         int i, j;
         Extract entire;
         StringBuilder buf;
         PresentSegmentButton button;
         Label label;
         DrawingArea lines;
         Image image;
         String str;
 
         if (folio == null) {
             return;
         }
         meta = folio.getMetadata();
 
         group = new SizeGroup(HORIZONTAL);
         buttons = new ArrayList<PresentSegmentButton>();
 
         buf = new StringBuilder();
 
         buf.append("<span size='xx-large'>");
         buf.append("<span font='Liberation Serif 12'>");
         buf.append('“');
         buf.append("</span>");
         buf.append(meta.getDocumentTitle());
         buf.append("<span font='Liberation Serif 12'>");
         buf.append('”');
         buf.append("</span>");
         buf.append("</span>");
 
         label = new HeadingLabel();
         label.setLabel(buf.toString());
         align = new Alignment(Alignment.LEFT, Alignment.CENTER, 0.0f, 0.0f);
         align.add(label);
         align.setPadding(0, 0, 0, 2);
 
         box = new HBox(false, 0);
         box.packStart(align, false, false, 3);
 
         /*
          * Document filename
          */
 
         buf.setLength(0);
         manuscript = folio.getManuscript();
 
         label = createDocumentFilenameLabel(manuscript);
 
         box.packStart(label, true, true, 0);
 
         J = folio.size();
 
         countDocument = 0;
         countChapter = new int[J];
         for (j = 0; j < J; j++) {
             countChapter[j] = 0;
         }
 
         wordsChapter = new Label[J];
 
         wordsDocument = new Label();
         wordsDocument.setUseMarkup(true);
         wordsDocument.setAlignment(Alignment.LEFT, Alignment.BOTTOM);
         box.packEnd(wordsDocument, false, false, 0);
 
         top.packStart(box, false, false, 3);
 
         for (j = 0; j < J; j++) {
             series = folio.getSeries(j);
 
             for (i = 0; i < series.size(); i++) {
                 segment = series.getSegment(i);
                 entire = segment.getEntire();
 
                 buf.setLength(0);
                 box = new HBox(false, 0);
 
                 if (segment instanceof ComponentSegment) {
                     incrementWordCount(j, entire);
 
                     button = new PresentSegmentButton(primary, group);
                     box.packStart(button, false, false, 0);
 
                     button.setAddress(series, segment);
                     buttons.add(button);
 
                     chapter = folio.getChapter(j);
                     label = createChapterFilenameLabel(chapter);
                     label.setAlignment(Alignment.LEFT, Alignment.CENTER);
                     box.packStart(label, true, true, 0);
 
                     /*
                      * We need to both create a Label so it can be packed and
                      * store a reference to it so we can update it later.
                      */
 
                     label = new Label();
                     label.setUseMarkup(true);
                     wordsChapter[j] = label;
                     box.packEnd(label, false, false, 0);
 
                 } else if (segment instanceof HeadingSegment) {
                     incrementWordCount(j, entire);
 
                     button = new PresentSegmentButton(primary, group);
                     box.packStart(button, false, false, 0);
 
                     button.setAddress(series, segment);
                     buttons.add(button);
 
                 } else if (segment instanceof ImageSegment) {
                     incrementWordCount(j, entire);
 
                     image = new Image(images.graphic);
                     image.setAlignment(Alignment.LEFT, Alignment.TOP);
                     image.setPadding(40, 3);
 
                     box.packStart(image, false, false, 0);
                 } else {
 
                     if (segment instanceof PreformatSegment) {
                         lines = new CompressedLines(entire, true);
                     } else {
                         lines = new CompressedLines(entire, false);
                         incrementWordCount(j, entire);
                     }
                     box.packStart(lines, false, false, 0);
                 }
 
                 top.packStart(box, false, false, 0);
             }
         }
 
         /*
          * Now update the word count labels. We use constant width so that the
          * (fairly common) case of a single chapter showing its word count
          * next to the document word count looks good - otherwise they're
          * different widths, and it's a bit jarring visually.
          */
 
         str = formatWordCount(countDocument);
         wordsDocument.setLabel("<tt><b>" + str + "</b></tt>");
 
         for (j = 0; j < J; j++) {
             str = formatWordCount(countChapter[j]);
             wordsChapter[j].setLabel("<tt>" + str + "</tt>");
         }
 
         top.showAll();
     }
 
     private static Label createChapterFilenameLabel(final Chapter chapter) {
         final Label result;
         final String str;
 
         str = chapter.getRelative();
 
         /*
          * It would be better if this colour was taken from quill.ui.Format,
          * seeing as how we're using the same visual as used for <filename>
          * markup in EditorTextView.
          */
 
         result = new Label("<span color='darkgreen'> <tt>" + markupEscapeText(str) + "</tt></span>");
         result.setUseMarkup(true);
 
         result.setAlignment(Alignment.LEFT, Alignment.CENTER);
 
         result.setEllipsize(EllipsizeMode.START);
 
         return result;
     }
 
     private static Label createDocumentFilenameLabel(final Manuscript manuscript) {
         String str;
         final String dir, file;
         final Label result;
 
         str = manuscript.getDirectory();
         dir = str.replace(System.getenv("HOME"), "~");
 
         file = manuscript.getBasename() + ".parchment";
 
         result = new Label("<span color='darkgreen' size='x-small'><tt>" + markupEscapeText(dir)
                 + "</tt></span>" + "\n" + "<span color='darkgreen'> <tt><b>" + markupEscapeText(file)
                 + "</b></tt></span>");
         result.setUseMarkup(true);
         result.setAlignment(Alignment.LEFT, Alignment.BOTTOM);
 
         result.setEllipsize(EllipsizeMode.START);
 
         return result;
     }
 
     private ArrayList<PresentSegmentButton> buttons;
 
     /**
      * Given a Folio, display it.
      */
     void affect(Folio after) {
         final Folio before;
 
         if (this.folio == after) {
             return;
         }
         before = this.folio;
         this.folio = after;
 
         /*
          * This is a bit tricky. We can only update the Buttons' Segments if
          * there are the same number of Series and Segments therein. Otherwise
          * we have to do a full rebuild.
          */
 
         try {
             updateButtons(before);
         } catch (StructureChangedException sce) {
             rebuildOutline();
         }
     }
 
     private void updateButtons(Folio before) throws StructureChangedException {
         final int J;
         int i, j, I, k;
         final Folio after;
         Series previous, next;
         Segment segment;
         PresentSegmentButton button;
 
         after = this.folio;
 
         /*
          * First we run through everything to see if the structure has
          * changed. If it has, we have to rebuild everything.
          */
 
         if (before == null) {
             throw new StructureChangedException();
         }
         if (before.size() != after.size()) {
             throw new StructureChangedException();
         }
 
         J = after.size();
 
         for (j = 0; j < J; j++) {
             previous = before.getSeries(j);
             next = after.getSeries(j);
 
             if (previous.size() != next.size()) {
                 throw new StructureChangedException();
             }
         }
 
         /*
          * Ok, so we have the same number of Buttons. Good. We can just update
          * the references on them.
          */
 
         k = 0;
 
         for (j = 0; j < J; j++) {
             previous = before.getSeries(j);
             next = after.getSeries(j);
 
             segment = next.getSegment(0);
             button = buttons.get(k);
             button.setAddress(next, segment);
             k++;
 
             I = next.size();
 
             for (i = 1; i < I; i++) {
                 segment = next.getSegment(i);
 
                 if (segment instanceof HeadingSegment) {
                     button = buttons.get(k);
                     button.setAddress(next, segment);
                     k++;
                 }
             }
         }
     }
 
     /**
      * Request the Widget actually (re)build itself. This is only called to
      * make a state visible, not on every state update.
      */
     /*
      * Actually, if this whole thing was ExposeEvent driven, then we'd be
      * doing this there and just having people call queueDraw(). But as we
      * have strong Widgets in the composition of this display, we need to
      * reconstruct and repack.
      */
     void refreshDisplay() {
         rebuildOutline();
     }
 
     private void rebuildOutline() {
         for (Widget child : top.getChildren()) {
             top.remove(child);
         }
 
         buildOutline();
     }
 
     private void incrementWordCount(int index, Extract entire) {
         final WordCountingCharacterVisitor tourist;
         final int num;
 
         tourist = new WordCountingCharacterVisitor();
         entire.visit(tourist);
 
         num = tourist.getCount();
         countDocument += num;
         countChapter[index] += num;
     }
 
     private String formatWordCount(final int num) {
         final String str;
         final StringBuffer buf;
         int i;
 
         str = Integer.toString(num);
         buf = new StringBuffer(str);
 
         i = buf.length();
         i -= 3;
         while (i > 0) {
             buf.insert(i, ',');
             i -= 3;
         }
 
         return buf.toString();
     }
 
     private class WordCountingCharacterVisitor implements CharacterVisitor
     {
         private boolean word;
 
         private int count;
 
         private WordCountingCharacterVisitor() {
             count = 0;
             word = false;
         }
 
         public boolean visit(int character, Markup markup) {
             if (Character.isWhitespace(character)) {
                 if (word) {
                     word = false;
                 }
             } else {
                 if (!word) {
                     count++;
                     word = true;
                 }
             }
             return false;
         }
 
         private int getCount() {
             return count;
         }
     }
 
     @SuppressWarnings("serial")
     private class StructureChangedException extends ApplicationException
     {
     }
 }
 
 class PresentSegmentButton extends Button implements Button.Clicked
 {
     private Series series;
 
     private Segment segment;
 
     private final PrimaryWindow primary;
 
     private final Label label;
 
     PresentSegmentButton(PrimaryWindow primary, SizeGroup group) {
         super();
         this.primary = primary;
         super.setRelief(ReliefStyle.NONE);
         super.connect(this);
 
         label = new HeadingLabel();
         super.add(label);
         group.add(label);
     }
 
     public void onClicked(Button source) {
         primary.ensureVisible(series, segment);
     }
 
     void setAddress(final Series series, final Segment segment) {
         final StringBuilder buf;
         final Extract entire;
        final String str, escaped;
 
         /*
          * Update state if necessary
          */
 
         if (series == this.series) {
             return;
         }
         this.series = series;
         this.segment = segment;
 
         /*
          * Redo label.
          */
 
         entire = segment.getEntire();
         str = entire.getText();
        escaped = Glib.markupEscapeText(str);
 
         buf = new StringBuilder();
 
         if (segment instanceof HeadingSegment) {
             buf.append("      ");
            buf.append(escaped);
         } else if (segment instanceof ComponentSegment) {
             buf.append("<span size='x-large'>  ");
            buf.append(escaped);
             buf.append("</span>");
         } else {
             throw new AssertionError();
         }
 
         label.setLabel(buf.toString());
     }
 
 }
 
 class HeadingLabel extends Label
 {
     HeadingLabel() {
         super();
         super.setUseMarkup(true);
         super.setAlignment(Alignment.LEFT, Alignment.TOP);
         super.setSizeRequest(300, -1);
 
         /*
          * Ellipsize the text...
          */
 
         super.setEllipsize(EllipsizeMode.END);
 
         /*
          * Without this, the Label will ellipsize, but to the width being
          * driven by the CompressedLines. With this, the headings are slightly
          * wider than that width, and nicely overhand the lines on both sides.
          */
 
         super.setMaxWidthChars(32);
     }
 }
 
 class CompressedLines extends DrawingArea
 {
     private int[] dots;
 
     private int num;
 
     private boolean pre;
 
     private static final int WIDTH = 200;
 
     private static final int SPACING = 3;
 
     CompressedLines(Extract entire, boolean program) {
         super();
         final DrawingArea self;
         final TextChain chain;
         Extract[] paras;
         String line;
         int j, i, words, width;
 
         self = this;
 
         chain = new TextChain(entire);
         paras = chain.extractParagraphs();
         dots = new int[paras.length];
         j = 0;
 
         for (Extract extract : paras) {
             if (extract == null) {
                 continue;
             }
             line = extract.getText();
             i = -1;
             words = 0;
 
             do {
                 i++;
                 words++;
             } while ((i = line.indexOf(' ', i)) != -1);
 
             dots[j++] = words;
         }
 
         num = 1;
         width = 0;
         for (i = 0; i < dots.length; i++) {
             width += dots[i] + 2;
             if (width > WIDTH) {
                 num++;
                 width = 0;
             }
         }
 
         pre = program;
 
         self.setSizeRequest(40 + WIDTH, SPACING * num);
 
         this.connect(new Widget.ExposeEvent() {
             public boolean onExposeEvent(Widget source, EventExpose event) {
                 final Context cr;
                 int i, j, width, rem;
 
                 cr = new Context(event);
 
                 if (pre) {
                     cr.setSource(0.7, 0.7, 0.7);
                 } else {
                     cr.setSource(0.5, 0.5, 0.5);
                 }
 
                 cr.setLineWidth(1.0);
                 cr.setAntialias(Antialias.NONE);
                 cr.translate(0.5, 0.5);
 
                 cr.moveTo(40, 0);
                 width = 0;
                 j = 0;
 
                 for (i = 0; i < dots.length; i++) {
                     if (width + dots[i] > WIDTH) {
                         rem = WIDTH - width;
                         cr.lineRelative(rem, 0);
                         j++;
                         cr.moveTo(40, SPACING * j);
                         cr.lineRelative(dots[i] - rem, 0);
                         width = dots[i] - rem + 2;
                     } else {
                         cr.lineRelative(dots[i], 0);
                         width += dots[i] + 2;
                     }
                     cr.moveRelative(2, 0);
                 }
 
                 cr.stroke();
 
                 return true;
             }
         });
     }
 }
