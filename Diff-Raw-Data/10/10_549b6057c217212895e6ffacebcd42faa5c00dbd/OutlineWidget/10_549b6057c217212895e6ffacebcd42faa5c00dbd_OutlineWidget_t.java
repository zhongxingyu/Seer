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
 
 import java.io.FileNotFoundException;
 
 import org.freedesktop.cairo.Antialias;
 import org.freedesktop.cairo.Context;
 import org.gnome.gdk.EventExpose;
 import org.gnome.gdk.Pixbuf;
 import org.gnome.gtk.Alignment;
 import org.gnome.gtk.Button;
 import org.gnome.gtk.DrawingArea;
 import org.gnome.gtk.HBox;
 import org.gnome.gtk.Image;
 import org.gnome.gtk.Label;
 import org.gnome.gtk.PolicyType;
 import org.gnome.gtk.ReliefStyle;
 import org.gnome.gtk.ScrolledWindow;
 import org.gnome.gtk.VBox;
 import org.gnome.gtk.Widget;
 
 import quill.textbase.ComponentSegment;
 import quill.textbase.Extract;
 import quill.textbase.Folio;
 import quill.textbase.HeadingSegment;
 import quill.textbase.ImageSegment;
 import quill.textbase.PreformatSegment;
 import quill.textbase.Segment;
 import quill.textbase.Series;
 import quill.textbase.TextChain;
 
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
 
     public OutlineWidget() {
         super();
         scroll = this;

         top = new VBox(false, 0);
        scroll.addWithViewport(top);
        scroll.setPolicy(PolicyType.NEVER, PolicyType.ALWAYS);

         folio = null;
     }
 
     private void buildOutline() {
         Series series;
         Segment segment;
         int i, j;
         Extract entire;
         StringBuilder str;
         Button button;
         Label label;
         DrawingArea lines;
 
         if (folio == null) {
             return;
         }
 
         for (j = 0; j < folio.size(); j++) {
             series = folio.getSeries(j);
 
             for (i = 0; i < series.size(); i++) {
                 segment = series.getSegment(i);
 
                 if (segment instanceof ComponentSegment) {
                     entire = segment.getEntire();
 
                     str = new StringBuilder();
                     str.append("<span size=\"xx-large\"> ");
                     str.append(entire.getText());
                     str.append("</span>");
                 } else if (segment instanceof HeadingSegment) {
                     entire = segment.getEntire();
 
                     str = new StringBuilder();
                     str.append("      ");
                     str.append(entire.getText());
                 } else if (segment instanceof ImageSegment) {
                     Image image;
                     Pixbuf pixbuf;
                     HBox left;
 
                     try {
                         pixbuf = new Pixbuf("share/pixmaps/graphic-16x16.png", -1, 10, true);
                     } catch (FileNotFoundException e) {
                         throw new Error(e);
                     }
                     image = new Image(pixbuf);
                     image.setAlignment(Alignment.LEFT, Alignment.TOP);
                     image.setPadding(40, 3);
                     left = new HBox(false, 0);
                     left.packStart(image, false, false, 0);
                     top.packStart(left, false, false, 0);
                     continue;
                 } else {
                     entire = segment.getEntire();
 
                     if (segment instanceof PreformatSegment) {
                         lines = new CompressedLines(entire, true);
                     } else {
                         lines = new CompressedLines(entire, false);
                     }
                     top.packStart(lines, false, false, 0);
                     continue;
                 }
 
                 button = new Button(str.toString());
                 button.setRelief(ReliefStyle.NONE);
                 label = (Label) button.getChild();
                 label.setUseMarkup(true);
                 label.setAlignment(Alignment.LEFT, Alignment.TOP);
 
                 top.packStart(button, false, false, 0);
             }
         }
 
        top.showAll();
     }
 
     /**
      * Given a Folio, display it.
      */
     /*
      * FIXME Mockup! We actually need to evaluate the Series(s) against the
      * ones being displayed, and rebuild if/as necessary, and presumably if
      * we're actually showing.
      */
     /*
      * At the moment this is horribly inefficient; we should have something
      * more dynamic that merely updates the Labels rather than wholesale
      * recreates everything.
      */
     void affect(Folio folio) {
         if (this.folio == folio) {
             return;
         }
 
         this.folio = folio;
     }
 
     /**
      * Request the Widget actually (re)build itself. This is only called when
      * to make a state visible, not on every state update.
      */
     /*
      * Actually, if this whole thing was ExposeEvent driven, then we'd be
      * doing this there and just having people call queueDraw(). But as we
      * have strong Widgets in the composition of this display, we need to
      * reconstruct and repack.
      */
     public void queueDraw() {
         for (Widget child : top.getChildren()) {
             top.remove(child);
         }
 
         buildOutline();
 
         super.queueDraw();
     }
 }
 
 class CompressedLines extends DrawingArea
 {
     private int[] dots;
 
     private int num;
 
     private boolean pre;
 
     private static final int WIDTH = 200;
 
     private static final int SPACING = 4;
 
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
