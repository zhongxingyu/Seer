 /*
  * PreviewWidget.java
  *
  * Copyright (c) 2008-2009 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the program it is a part of, are made available
  * to you by its authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 package markerpen.ui;
 
 import org.freedesktop.cairo.Context;
 import org.freedesktop.cairo.FontOptions;
 import org.freedesktop.cairo.Matrix;
 import org.gnome.gdk.EventExpose;
 import org.gnome.gtk.Allocation;
 import org.gnome.gtk.DrawingArea;
 import org.gnome.gtk.PaperSize;
 import org.gnome.gtk.Unit;
 import org.gnome.gtk.Widget;
 import org.gnome.pango.FontDescription;
 import org.gnome.pango.Layout;
 import org.gnome.pango.LayoutLine;
 
 import static org.freedesktop.cairo.HintMetrics.OFF;
 
 /*
  * Work in "points", which makes sense since the target back end is PDF.
  */
 class PreviewWidget extends DrawingArea
 {
     private int pixelWidth;
 
     private int pixelHeight;
 
     private double pageWidth;
 
     private double pageHeight;
 
     private double topMargin;
 
     private double bottomMargin;
 
     private double leftMargin;
 
     private double rightMargin;
 
     private double scaleFactor;
 
     private double cursor;
 
     PreviewWidget() {
         super();
 
         this.connect(new Widget.ExposeEvent() {
             public boolean onExposeEvent(Widget source, EventExpose event) {
                 final Context cr;
 
                 cr = new Context(source.getWindow());
 
                 processSize(cr);
                 scaleOutput(cr);
                 drawPageOutline(cr);
                drawCrosshairs(cr);
                 processText(cr);
 
                 return true;
             }
         });
     }
 
     private void processSize(Context cr) {
         final Allocation rect;
         final PaperSize paper;
 
         rect = this.getAllocation();
 
         pixelWidth = rect.getWidth();
         pixelHeight = rect.getHeight();
 
         paper = PaperSize.A4;
         pageWidth = paper.getWidth(Unit.POINTS);
         pageHeight = paper.getHeight(Unit.POINTS);
 
         topMargin = 25;
         bottomMargin = 25;
         leftMargin = 45;
         rightMargin = 20;
     }
 
     private void drawPageOutline(Context cr) {
         final double shadow = 3.0;
 
         cr.rectangle(shadow, shadow, pageWidth, pageHeight);
         cr.setSource(0.1, 0.1, 0.1);
         cr.fill();
 
         cr.rectangle(0, 0, pageWidth, pageHeight);
         cr.setSource(1.0, 1.0, 1.0);
         cr.fillPreserve();
         cr.setSource(0.0, 0.0, 0.0);
         cr.setLineWidth(0.5);
         cr.stroke();
     }
 
    private void drawCrosshairs(Context cr) {
        drawCrosshairAt(cr, leftMargin, topMargin);
        drawCrosshairAt(cr, pageWidth - rightMargin, topMargin);
        drawCrosshairAt(cr, leftMargin, pageHeight - bottomMargin);
        drawCrosshairAt(cr, pageWidth - rightMargin, pageHeight - bottomMargin);
    }

    private void drawCrosshairAt(Context cr, final double x, final double y) {
        cr.moveTo(x, y - 10);
        cr.lineRelative(0, 20);
        cr.moveTo(x - 10, y);
        cr.lineRelative(20, 0);
        cr.stroke();
    }

     public void processText(Context cr) {
         cursor = topMargin;
 
         drawBlockText(cr, textview.LoremIpsum.text);
         drawBlockProgram(cr, "public class Hello {\n" + "    public static void main(String[] args) {\n"
                 + "        Gtk.init(args);\n" + "        Gtk.main();\n" + "    }\n" + "}");
         drawBlockText(cr, textview.LoremIpsum.text);
 
     }
 
     public void drawBlockText(Context cr, String text) {
         final Layout layout;
         final FontDescription desc;
         final FontOptions options;
         final String[] paras;
         double y, b, v = 0;
         boolean second;
 
         layout = new Layout(cr);
 
         options = new FontOptions();
         options.setHintMetrics(OFF);
         layout.getContext().setFontOptions(options);
 
         desc = new FontDescription("Liberation Serif");
         desc.setSize(8.0);
         layout.setFontDescription(desc);
 
         /*
          * This is fake. TODO pass in our TextStack object (which already
          * knows what the paragraphs are) and process from that.
          */
         paras = text.split("\n");
 
         layout.setWidth(pageWidth - (leftMargin + rightMargin));
         layout.setText("Workaround");
 
         cr.setSource(0.0, 0.0, 0.0);
 
         b = layout.getBaseline();
         y = cursor + b;
         second = false;
 
         for (String para : paras) {
 
             layout.setText(para);
 
             if (second) {
                 y += v; // blank line between paras
             }
 
             for (LayoutLine line : layout.getLinesReadonly()) {
                 v = line.getExtentsLogical().getHeight();
 
                 y += v;
 
                 if (y > (pageHeight - topMargin - bottomMargin)) {
                     return;
                 }
                 cr.moveTo(leftMargin, y);
                 cr.showLayout(line);
             }
 
             second = true;
         }
 
         cursor = y;
     }
 
     public void drawBlockProgram(Context cr, String prog) {
         final Layout layout;
         final FontDescription desc;
         final FontOptions options;
         final String[] paras;
         double y, b, v = 0;
 
         layout = new Layout(cr);
 
         options = new FontOptions();
         options.setHintMetrics(OFF);
         layout.getContext().setFontOptions(options);
 
         desc = new FontDescription("Liberation Mono");
         desc.setSize(8.0);
         layout.setFontDescription(desc);
 
         /*
          * This is fake. TODO pass in our TextStack object (which already
          * knows what the paragraphs are) and process from that.
          */
         paras = prog.split("\n");
 
         layout.setWidth(pageWidth - (leftMargin + rightMargin));
         layout.setText("Workaround");
 
         cr.setSource(0.0, 0.0, 0.0);
 
         b = layout.getBaseline();
         y = cursor + b;
         for (String para : paras) {
             layout.setText(para);
 
             for (LayoutLine line : layout.getLinesReadonly()) {
                 v = line.getExtentsLogical().getHeight();
 
                 y += v;
 
                 if (y > pageHeight) {
                     return;
                 }
                 cr.moveTo(leftMargin, y);
                 cr.showLayout(line);
             }
 
             // no blank line between paras
         }
 
         cursor = y;
     }
 
     private void scaleOutput(Context cr) {
         final Matrix matrix;
         final double scaleWidth, scaleHeight;
 
         scaleWidth = pixelWidth / (pageWidth + 10.0);
         scaleHeight = pixelHeight / (pageHeight + 10.0);
 
         if (scaleWidth > scaleHeight) {
             scaleFactor = scaleHeight;
         } else {
             scaleFactor = scaleWidth;
         }
 
         matrix = new Matrix();
         matrix.scale(scaleFactor, scaleFactor);
 
         if (scaleWidth > scaleHeight) {
             matrix.translate(((pixelWidth / scaleFactor) - pageWidth) / 2.0, 0.0);
         } else {
             matrix.translate(0.0, ((pixelHeight / scaleFactor) - pageHeight) / 2.0);
         }
 
         /*
          * Bump the image off of the top left corner.
          */
         matrix.translate(0.5, 1.5);
         cr.transform(matrix);
     }
 }
