 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2010 Operational Dynamics Consulting, Pty Ltd
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
 
 import org.freedesktop.cairo.Context;
 import org.freedesktop.cairo.Matrix;
 import org.gnome.gdk.EventExpose;
 import org.gnome.gtk.Alignment;
 import org.gnome.gtk.Allocation;
 import org.gnome.gtk.AttachOptions;
 import org.gnome.gtk.CellRendererPixbuf;
 import org.gnome.gtk.CellRendererText;
 import org.gnome.gtk.ComboBox;
 import org.gnome.gtk.DataColumn;
 import org.gnome.gtk.DataColumnPixbuf;
 import org.gnome.gtk.DataColumnString;
 import org.gnome.gtk.DrawingArea;
 import org.gnome.gtk.Entry;
 import org.gnome.gtk.HBox;
 import org.gnome.gtk.Label;
 import org.gnome.gtk.ListStore;
 import org.gnome.gtk.SizeGroup;
 import org.gnome.gtk.SizeGroupMode;
 import org.gnome.gtk.Table;
 import org.gnome.gtk.TextComboBox;
 import org.gnome.gtk.TreeIter;
 import org.gnome.gtk.VBox;
 import org.gnome.gtk.Widget;
 
 import parchment.format.Stylesheet;
 import parchment.render.RenderEngine;
 import quill.client.ApplicationException;
 import quill.textbase.Folio;
 
 import static org.freedesktop.bindings.Internationalization._;
 import static org.gnome.gtk.Alignment.CENTER;
 import static org.gnome.gtk.Alignment.LEFT;
 import static org.gnome.gtk.Alignment.RIGHT;
 import static org.gnome.gtk.Alignment.TOP;
 
 /**
  * UI for presenting and editing the active Stylesheet
  * 
  * @author Andrew Cowie
  */
 class StylesheetEditorWidget extends VBox
 {
     /**
      * Reference to self
      */
     private final VBox top;
 
     /**
      * The current Stylesheet
      */
     private Stylesheet style;
 
     /**
      * An instance of a renderer based on style.
      */
     private RenderEngine engine;
 
     private MilimetreEntry topMargin, leftMargin, rightMargin, bottomMargin;
 
     private Entry serifFont, sansFont, monoFont, headingFont;
 
     private MilimetreEntry serifSize, sansSize, monoSize, headingSize;
 
     private RendererPicker rendererList;
 
     private TextComboBox paperList;
 
     private Label paperWidth, paperHeight;
 
     private PageSizeDisplay page;
 
     private FontHeightDisplay preview;
 
     /**
      * SizeGroup to keep the subheading Labels aligned.
      */
     private final SizeGroup group;
 
     /**
      * Reference to the enclosing document Window.
      */
     private final PrimaryWindow primary;
 
     private Folio folio;
 
     /**
      * Are we in the midst of loading a new document? If so, ignore events.
      */
     private boolean loading;
 
     StylesheetEditorWidget(PrimaryWindow primary) {
         super(false, 0);
         top = this;
 
         this.primary = primary;
         this.group = new SizeGroup(SizeGroupMode.HORIZONTAL);
         this.loading = false;
 
         setupHeading();
         setupRenderSelector();
         setupPaperAndMargins();
         setupFontAndSizes();
         setupFontPreview();
     }
 
     private void setupHeading() {
         final Label heading;
 
         heading = new Label();
         heading.setUseMarkup(true);
         heading.setLabel("<span size='xx-large'>" + _("Stylesheet") + "</span>");
         heading.setAlignment(LEFT, TOP);
 
         top.packStart(heading, false, false, 6);
     }
 
     private void setupRenderSelector() {
         final Label heading;
 
         heading = new Label("<b>" + _("Render Engine") + "</b>");
         heading.setUseMarkup(true);
         heading.setAlignment(LEFT, CENTER);
         top.packStart(heading, false, false, 6);
 
         rendererList = new RendererPicker(group);
         top.packStart(rendererList, false, false, 0);
 
         rendererList.connect(new RendererPicker.Changed() {
             public void onChanged(String value) {
                 final Stylesheet replacement;
 
                 if (loading) {
                     return;
                 }
 
                 replacement = style.changeRendererClass(value);
                 propegateStylesheetChange(replacement);
             }
         });
     }
 
     private void setupPaperAndMargins() {
         final HBox sides;
         final VBox left;
         Label heading, label;
         final Table table;
         HBox box;
 
         sides = new HBox(false, 0);
         left = new VBox(false, 3);
 
         heading = new Label("<b>" + _("Paper") + "</b>");
         heading.setUseMarkup(true);
         heading.setAlignment(LEFT, CENTER);
         top.packStart(heading, false, false, 6);
 
         label = new Label(_("Size") + ":");
 
         /*
          * TODO, replace this with a better source of sizes? Remember that
          * we're deliberately not showing every paper size under the sun, just
          * a couple obvious ones. There needs to be a java-gnome PaperSize
          * constant for it...
          */
 
         paperList = new TextComboBox();
         paperList.appendText("A4");
         paperList.appendText("Letter");
         paperList.setActive(0);
 
         paperWidth = new Label("000.0 mm");
         paperHeight = new Label("000.0 mm");
 
         box = new KeyValueBox(group, label, paperList, false);
         left.packStart(box, false, false, 0);
         sides.packStart(left, false, false, 0);
 
         paperList.connect(new ComboBox.Changed() {
             public void onChanged(ComboBox source) {
                 final String str;
                 final Stylesheet replacement;
 
                 if (loading) {
                     return;
                 }
 
                 str = paperList.getActiveText();
                 replacement = style.changePaperSize(str);
                 propegateStylesheetChange(replacement);
             }
         });
 
         /*
          * On the right, include an illustration of the page size, showing
          * dimensions.
          */
 
         page = new PageSizeDisplay();
         page.setSizeRequest(130, 180);
 
         table = new Table(2, 2, false);
         table.attach(page, 0, 1, 0, 1, AttachOptions.SHRINK, AttachOptions.SHRINK, 0, 0);
 
         paperHeight.setAlignment(LEFT, 0.4f);
         table.attach(paperHeight, 1, 2, 0, 1);
 
         paperWidth.setAlignment(0.4f, TOP);
         table.attach(paperWidth, 0, 1, 1, 2);
 
         /*
          * Ensure the whole thing floats in the center of the pane
          */
 
         sides.packStart(table, true, false, 0);
 
         /*
          * Now, the margins
          */
 
         heading = new Label("<b>" + _("Margins") + "</b>");
         heading.setUseMarkup(true);
         heading.setAlignment(LEFT, CENTER);
         left.packStart(heading, false, false, 6);
 
         label = new Label(_("Top") + ":");
         topMargin = new MilimetreEntry();
         box = new KeyValueBox(group, label, topMargin, false);
         left.packStart(box, false, false, 0);
         topMargin.connect(new MilimetreEntry.Changed() {
             public void onChanged(String value) {
                 final Stylesheet replacement;
 
                 if (loading) {
                     return;
                 }
 
                 replacement = style.changeMarginTop(value);
                 propegateStylesheetChange(replacement);
             }
         });
 
         label = new Label(_("Left") + ":");
         leftMargin = new MilimetreEntry();
         box = new KeyValueBox(group, label, leftMargin, false);
         left.packStart(box, false, false, 0);
         leftMargin.connect(new MilimetreEntry.Changed() {
             public void onChanged(String value) {
                 final Stylesheet replacement;
 
                 if (loading) {
                     return;
                 }
 
                 replacement = style.changeMarginLeft(value);
                 propegateStylesheetChange(replacement);
             }
         });
 
         label = new Label(_("Right") + ":");
         rightMargin = new MilimetreEntry();
         box = new KeyValueBox(group, label, rightMargin, false);
         left.packStart(box, false, false, 0);
         rightMargin.connect(new MilimetreEntry.Changed() {
             public void onChanged(String value) {
                 final Stylesheet replacement;
 
                 if (loading) {
                     return;
                 }
 
                 replacement = style.changeMarginRight(value);
                 propegateStylesheetChange(replacement);
             }
         });
 
        label = new Label(_("Bottom:") + ":");
         bottomMargin = new MilimetreEntry();
         box = new KeyValueBox(group, label, bottomMargin, false);
         left.packStart(box, false, false, 0);
         bottomMargin.connect(new MilimetreEntry.Changed() {
             public void onChanged(String value) {
                 final Stylesheet replacement;
 
                 if (loading) {
                     return;
                 }
 
                 replacement = style.changeMarginBottom(value);
                 propegateStylesheetChange(replacement);
             }
         });
 
         top.packStart(sides, false, false, 6);
     }
 
     private void setupFontAndSizes() {
         final HBox sides;
         final VBox left;
         HBox box;
         final Label heading;
         Label label;
 
         heading = new Label("<b>" + _("Fonts") + "</b>");
         heading.setUseMarkup(true);
         heading.setAlignment(LEFT, CENTER);
         top.packStart(heading, false, false, 6);
 
         sides = new HBox(false, 0);
         left = new VBox(false, 3);
 
         label = new Label(_("Serif") + ":");
         serifFont = new Entry();
         serifSize = new MilimetreEntry();
         box = new KeyValueBox(group, label, serifFont, false);
         box.packStart(serifSize, false, false, 0);
         left.packStart(box, false, false, 0);
         serifFont.connect(new Entry.Activate() {
             public void onActivate(Entry source) {
                 final String value;
                 final Stylesheet replacement;
 
                 if (loading) {
                     return;
                 }
 
                 value = source.getText();
                 replacement = style.changeFontSerif(value);
                 propegateStylesheetChange(replacement);
             }
         });
         serifSize.connect(new MilimetreEntry.Changed() {
             public void onChanged(String value) {
                 final Stylesheet replacement;
 
                 if (loading) {
                     return;
                 }
 
                 replacement = style.changeSizeSerif(value);
                 propegateStylesheetChange(replacement);
             }
         });
 
         label = new Label(_("Sans") + ":");
         sansFont = new Entry();
         sansSize = new MilimetreEntry();
         box = new KeyValueBox(group, label, sansFont, false);
         box.packStart(sansSize, false, false, 0);
         left.packStart(box, false, false, 0);
         sansFont.connect(new Entry.Activate() {
             public void onActivate(Entry source) {
                 final String value;
                 final Stylesheet replacement;
 
                 if (loading) {
                     return;
                 }
 
                 value = source.getText();
                 replacement = style.changeFontSans(value);
                 propegateStylesheetChange(replacement);
             }
         });
         sansSize.connect(new MilimetreEntry.Changed() {
             public void onChanged(String value) {
                 final Stylesheet replacement;
 
                 if (loading) {
                     return;
                 }
 
                 replacement = style.changeSizeSans(value);
                 propegateStylesheetChange(replacement);
             }
         });
 
         label = new Label(_("Mono") + ":");
         monoFont = new Entry();
         monoSize = new MilimetreEntry();
         box = new KeyValueBox(group, label, monoFont, false);
         box.packStart(monoSize, false, false, 0);
         left.packStart(box, false, false, 0);
         monoFont.connect(new Entry.Activate() {
             public void onActivate(Entry source) {
                 final String value;
                 final Stylesheet replacement;
 
                 if (loading) {
                     return;
                 }
 
                 value = source.getText();
                 replacement = style.changeFontMono(value);
                 propegateStylesheetChange(replacement);
             }
         });
         monoSize.connect(new MilimetreEntry.Changed() {
             public void onChanged(String value) {
                 final Stylesheet replacement;
 
                 if (loading) {
                     return;
                 }
 
                 replacement = style.changeSizeMono(value);
                 propegateStylesheetChange(replacement);
             }
         });
 
         label = new Label(_("Heading") + ":");
         headingFont = new Entry();
         headingSize = new MilimetreEntry();
         box = new KeyValueBox(group, label, headingFont, false);
         box.packStart(headingSize, false, false, 0);
         left.packStart(box, false, false, 0);
         headingFont.connect(new Entry.Activate() {
             public void onActivate(Entry source) {
                 final String value;
                 final Stylesheet replacement;
 
                 if (loading) {
                     return;
                 }
 
                 value = source.getText();
                 replacement = style.changeFontHeading(value);
                 propegateStylesheetChange(replacement);
             }
         });
         headingSize.connect(new MilimetreEntry.Changed() {
             public void onChanged(String value) {
                 final Stylesheet replacement;
 
                 if (loading) {
                     return;
                 }
 
                 replacement = style.changeSizeHeading(value);
                 propegateStylesheetChange(replacement);
             }
         });
 
         sides.packStart(left, true, true, 0);
         top.packStart(sides, false, false, 6);
     }
 
     void initializeStylesheet(Folio folio) {
         loading = true;
         this.affect(folio);
         loading = false;
     }
 
     void affect(Folio folio) {
         final Stylesheet style;
         String str;
         final double width, height;
 
         this.folio = folio;
 
         style = folio.getStylesheet();
         if (style == this.style) {
             return;
         }
         this.style = style;
 
         try {
             engine = RenderEngine.createRenderer(style);
         } catch (ApplicationException ae) {
             throw new Error(ae);
         }
 
         str = engine.getClass().getName();
         rendererList.setActiveRenderer(str);
 
         str = style.getPaperSize();
 
         /*
          * FIXME This is horrid, and shouldn't be here. Worse it duplicates
          * code in RenderSettings, and there isn't the right place either.
          */
         if (str.equals("A4")) {
             paperList.setActive(0);
         } else if (str.equals("Letter")) {
             paperList.setActive(1);
         } else {
             throw new AssertionError("Unknown paper size");
         }
 
         str = style.getMarginTop();
         topMargin.setText(str);
 
         str = style.getMarginLeft();
         leftMargin.setText(str);
 
         str = style.getMarginRight();
         rightMargin.setText(str);
 
         str = style.getMarginBottom();
         bottomMargin.setText(str);
 
         str = style.getFontSerif();
         serifFont.setText(str);
         str = style.getSizeSerif();
         serifSize.setText(str);
 
         str = style.getFontSans();
         sansFont.setText(str);
         str = style.getSizeSans();
         sansSize.setText(str);
 
         str = style.getFontMono();
         monoFont.setText(str);
         str = style.getSizeMono();
         monoSize.setText(str);
 
         str = style.getFontHeading();
         headingFont.setText(str);
         str = style.getSizeHeading();
         headingSize.setText(str);
 
         page.setStyle(engine);
         page.queueDraw();
 
         width = engine.getPageWidth();
         height = engine.getPageHeight();
         paperWidth.setLabel(convertPageSize(width) + " mm");
         paperHeight.setLabel(convertPageSize(height) + " mm");
 
         preview.setRenderer(engine);
         preview.queueDraw();
     }
 
     /**
      * Create a new Folio, propegate it, and update the preview.
      */
     /*
      * Calling switchToPreview() is probably not ideal as a way to trigger an
      * update, as it really needs to be something asynchronous, but it works
      * and will do for now.
      */
     private void propegateStylesheetChange(Stylesheet style) {
         final Folio replacement;
 
         replacement = folio.update(style);
         primary.apply(replacement);
         this.affect(replacement);
         primary.forceRefresh();
     }
 
     private static String convertPageSize(double points) {
         final double mm;
         final String trim;
 
         mm = points / 72.0 * 25.4;
         trim = MilimetreEntry.constrainDecimal(mm);
         return trim;
     }
 
     /*
      * Make sure that something other than one of the entries has default. For
      * some reason calling grabDefault() on the Combo crashes?!?
      */
     public void grabDefault() {
         paperList.grabFocus();
     }
 
     private void setupFontPreview() {
         preview = new FontHeightDisplay();
         top.packStart(preview, true, true, 0);
     }
 }
 
 class KeyValueBox extends HBox
 {
     /**
      * @param expand
      *            Whether or not to give extra space to value Widget
      */
     KeyValueBox(SizeGroup size, Label label, Widget value, boolean expand) {
         super(false, 0);
 
         super.packStart(label, false, false, 3);
         label.setAlignment(RIGHT, CENTER);
         size.add(label);
 
         super.packStart(value, expand, expand, 3);
     }
 
     KeyValueBox(SizeGroup size, Label label, Widget value, Widget suffix) {
         this(size, label, value, false);
         super.packStart(suffix, false, false, 3);
     }
 }
 
 class RendererPicker extends VBox
 {
     private final VBox top;
 
     private final DataColumnString nameColumn;
 
     private final DataColumnPixbuf defaultColumn;
 
     private final DataColumnString classColumn;
 
     private final ListStore model;
 
     private final ComboBox combo;
 
     private final Label renderer;
 
     private RendererPicker.Changed handler;
 
     private String value;
 
     RendererPicker(final SizeGroup size) {
         super(false, 0);
         HBox box;
         Label label;
         CellRendererText text;
         CellRendererPixbuf image;
 
         top = this;
 
         label = new Label(_("Renderer") + ":");
 
         model = new ListStore(new DataColumn[] {
                 nameColumn = new DataColumnString(),
                 defaultColumn = new DataColumnPixbuf(),
                 classColumn = new DataColumnString()
         });
 
         combo = new ComboBox(model);
         text = new CellRendererText(combo);
         text.setMarkup(nameColumn);
 
         image = new CellRendererPixbuf(combo);
         image.setPixbuf(defaultColumn);
         image.setAlignment(Alignment.LEFT, Alignment.CENTER);
 
         combo.setSizeRequest(450, -1);
 
         /*
          * FIXME drive this based on some list of registered renderers!
          */
 
         populate(_("Manuscript"), _("Technical reports, conference papers, book manuscripts"), true,
                 "parchment.render.ReportRenderEngine");
         populate(_("Paperback Novel"), _("A printed novel, tradeback size"), false,
                 "parchment.render.NovelRenderEngine");
         populate(_("School paper"), _("University paper or School term report"), false,
                 "parchment.render.PaperRenderEngine");
 
         box = new KeyValueBox(size, label, combo, false);
         top.packStart(box, true, true, 0);
 
         combo.connect(new ComboBox.Changed() {
             public void onChanged(ComboBox source) {
                 final TreeIter row;
                 final String str;
 
                 row = source.getActiveIter();
 
                 str = model.getValue(row, classColumn);
                 renderer.setLabel("<tt>" + str + "</tt>");
 
                 if (handler == null) {
                     return;
                 }
                 if (str.equals(value)) {
                     return;
                 }
                 value = str;
 
                 handler.onChanged(str);
             }
         });
 
         /*
          * Now the display of the actual Java Class
          */
 
         label = new Label(_("Class") + ":");
 
         renderer = new Label("package.Class");
         renderer.setAlignment(LEFT, CENTER);
         renderer.setUseMarkup(true);
         renderer.setPadding(4, 0);
 
         box = new KeyValueBox(size, label, renderer, false);
         top.packStart(box, false, false, 0);
 
         combo.setActive(0);
     }
 
     /**
      * Access the Java class name that has been selected by this Widget.
      */
     String getSelectedRenderer() {
         final TreeIter row;
         final String str;
 
         row = combo.getActiveIter();
         str = model.getValue(row, classColumn);
 
         return str;
     }
 
     void setActiveRenderer(String renderer) {
         final TreeIter row;
         String str;
 
         row = model.getIterFirst();
         do {
             str = model.getValue(row, classColumn);
             if (str.equals(renderer)) {
                 combo.setActiveIter(row);
                 return;
             }
         } while (row.iterNext());
 
         throw new AssertionError("We haven't handled the case where you've loaded "
                 + "a renderer we don't already know about");
     }
 
     interface Changed
     {
         void onChanged(String value);
     }
 
     void connect(RendererPicker.Changed handler) {
         this.handler = handler;
     }
 
     private void populate(String rendererName, String rendererDescription, boolean isDefault,
             String typeName) {
         final TreeIter row;
 
         row = model.appendRow();
         model.setValue(row, nameColumn, "<b>" + rendererName + "</b>"
                 + (isDefault ? "  (" + _("default") + ")" : "") + "\n<span size='small'><i>"
                 + rendererDescription + "</i></span>");
 
         if (isDefault) {
             model.setValue(row, defaultColumn, null);
         } else {
             model.setValue(row, defaultColumn, null);
         }
 
         model.setValue(row, classColumn, typeName);
     }
 }
 
 /*
  * This was originally envisioned as something that would show margins, but it
  * turned out to just be an interesting way of illustrating the dimensions of
  * a page.
  */
 class PageSizeDisplay extends DrawingArea
 {
     private final DrawingArea drawing;
 
     private RenderEngine engine;
 
     PageSizeDisplay() {
         drawing = this;
 
         drawing.connect(new Widget.ExposeEvent() {
             public boolean onExposeEvent(Widget source, EventExpose event) {
                 final Context cr;
 
                 cr = new Context(event);
 
                 scaleOutput(cr, engine);
                 drawPageOutline(cr);
                 drawPageDimensions(cr);
 
                 return true;
             }
         });
     }
 
     void setStyle(RenderEngine engine) {
         this.engine = engine;
     }
 
     private static final int BUMP = 15;
 
     private static final int HEAD = 4;
 
     /**
      * Page width, in pixels! We round to integers so that we draw on pixels
      * and don't get anti-aliasing.
      */
     private int pageWidth, pageHeight;
 
     /**
      * Calculate a scaling factor and width parameters. Unlike PreviewWidget
      * we are not scaling the entire page. We're drawing in pixels.
      */
     private void scaleOutput(Context cr, final RenderEngine engine) {
         final Allocation rect;
         final Matrix matrix;
         final double engineWidth, engineHeight;
         final double pixelWidth;
         final double scaleFactor;
 
         rect = this.getAllocation();
 
         pixelWidth = rect.getWidth();
 
         engineWidth = engine.getPageWidth();
         engineHeight = engine.getPageHeight();
 
         /*
          * FUTURE This calculation is messy. We used to pick one of height or
          * width, except that meant that when you changed paper size one of
          * width or height stayed fixed, thereby not graphically representing
          * the different paper sizes. So we've somewhat hardcoded it.
          */
 
         scaleFactor = (pixelWidth - 2.0 * BUMP) / 600.0; // HARDCODE
 
         pageWidth = (int) (engineWidth * scaleFactor);
         pageHeight = (int) (engineHeight * scaleFactor);
 
         /*
          * Bump the image off of the top left corner. Adding (0.5, 0.5) means
          * we are drawing on pixels, rather than in the trough between them.
          */
 
         matrix = new Matrix();
         matrix.translate(0.5, 0.5);
         cr.transform(matrix);
     }
 
     /*
      * This code is almost identical to that in PreviewWidget
      */
     private void drawPageOutline(Context cr) {
         cr.rectangle(0.0, 0.0, pageWidth, pageHeight);
         cr.setSource(1.0, 1.0, 1.0);
         cr.fillPreserve();
         cr.setSource(0.0, 0.0, 0.0);
         cr.setLineWidth(1.0);
         cr.stroke();
     }
 
     /*
      * We're working in pixels, so fine, use ints, but the usual caveats about
      * not doing so if we're dividing. This is all cosmetic, so it doesn't
      * really matter. We work in proper doubles in the RenderEngines, of
      * course.
      */
     private void drawPageDimensions(Context cr) {
         cr.setLineWidth(1.0);
 
         /*
          * Horizontal
          */
 
         cr.moveTo(0, pageHeight + BUMP);
         cr.lineRelative(pageWidth, 0);
         cr.stroke();
 
         // left
         cr.moveTo(0, pageHeight + BUMP);
         cr.lineRelative(2 * HEAD, -HEAD);
         cr.lineRelative(0, HEAD * 2);
         cr.closePath();
         cr.fill();
 
         cr.moveTo(0, pageHeight + BUMP - HEAD);
         cr.lineRelative(0, 2 * HEAD);
         cr.stroke();
 
         // right
         cr.moveTo(pageWidth, pageHeight + BUMP);
         cr.lineRelative(-(2 * HEAD), -HEAD);
         cr.lineRelative(0, 2 * HEAD);
         cr.closePath();
         cr.fill();
 
         cr.moveTo(pageWidth, pageHeight + BUMP - HEAD);
         cr.lineRelative(0, 2 * HEAD);
         cr.stroke();
 
         /*
          * Vertical
          */
 
         cr.moveTo(pageWidth + BUMP, 0);
         cr.lineRelative(0, pageHeight);
         cr.stroke();
 
         // top
         cr.moveTo(pageWidth + BUMP, 0);
         cr.lineRelative(-HEAD, 2 * HEAD);
         cr.lineRelative(2 * HEAD, 0);
         cr.closePath();
         cr.fill();
 
         cr.moveTo(pageWidth + BUMP - HEAD, 0);
         cr.lineRelative(2 * HEAD, 0);
         cr.stroke();
 
         // bottom
         cr.moveTo(pageWidth + BUMP, pageHeight);
         cr.lineRelative(-HEAD, -(2 * HEAD));
         cr.lineRelative(2 * HEAD, 0);
         cr.closePath();
         cr.fill();
 
         cr.moveTo(pageWidth + BUMP - HEAD, pageHeight);
         cr.lineRelative(2 * HEAD, 0);
         cr.stroke();
     }
 }
