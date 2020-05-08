 /*
  *  FatFind
  *  A Diamond application for adipocyte image exploration
  *  Version 1
  *
  *  Copyright (c) 2006, 2010 Carnegie Mellon University
  *  All Rights Reserved.
  *
  *  This software is distributed under the terms of the Eclipse Public
  *  License, Version 1.0 which can be found in the file named LICENSE.
  *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
  *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
  */
 
 package edu.cmu.cs.diamond.fatfind;
 
 import java.awt.BasicStroke;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.geom.Line2D;
 import java.awt.geom.Path2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.*;
 
 import javax.imageio.ImageIO;
 
 import org.freedesktop.cairo.Context;
 import org.gnome.gdk.*;
 import org.gnome.glade.Glade;
 import org.gnome.glade.XML;
 import org.gnome.gtk.*;
 import org.gnome.gtk.Window;
 import org.gnome.gtk.Button.Clicked;
 import org.gnome.gtk.Entry.Changed;
 import org.gnome.gtk.IconView.SelectionChanged;
 import org.gnome.gtk.Range.ValueChanged;
 import org.gnome.gtk.Widget.*;
 import org.gnome.pango.FontDescription;
 
 import edu.cmu.cs.diamond.opendiamond.*;
 
 public class Main {
     final private Window fatfind;
 
     final private DrawingArea selectedImage;
 
     final private DrawingArea calibrateRefImage;
 
     final private Label calibrateRefInfo;
 
     final private DrawingArea defineRefImage;
 
     final private Label defineRefInfo;
 
     final private DrawingArea simulatedSearch;
 
     final private AboutDialog aboutdialog1;
 
     final private Button saveSearchButton;
 
     final private Button startSearch;
 
     final private Button stopSearch;
 
     final private Button defineScope;
 
     final private IconView searchResults;
 
     final private Button clearSearch;
 
     final private Button generateHistogram;
 
     final private DrawingArea selectedResult;
 
     final private Window histogramWindow;
 
     final private TextView histogram;
 
     final private IconView calibrationImages;
 
     final private TreeView definedSearches;
 
     final private Range minSharpness;
 
     final private Button recomputePreview;
 
     final private Entry searchName;
 
     final private Label statsLabel;
 
     final private DataColumnString savedSearchName = new DataColumnString();
 
     final private DataColumnReference savedSearchObject = new DataColumnReference();
 
     final private ListStore savedSearchStore = new ListStore(new DataColumn[] {
             savedSearchName, savedSearchObject });
 
     final private DataColumnPixbuf foundItemThumbnail = new DataColumnPixbuf();
 
     final private DataColumnString foundItemTitle = new DataColumnString();
 
     final private DataColumnReference foundItemResult = new DataColumnReference();
 
     final private ListStore foundItems = new ListStore(new DataColumn[] {
             foundItemThumbnail, foundItemTitle, foundItemResult });
 
     final private DataColumnPixbuf calibrationImagesThumbnail = new DataColumnPixbuf();
 
     final private DataColumnString calibrationImagesFilename = new DataColumnString();
 
     final private ListStore calibrationImagesModel = new ListStore(
             new DataColumn[] { calibrationImagesThumbnail,
                     calibrationImagesFilename });
 
     private ProcessedImage calibrationImage;
 
     private ProcessedImage simulatedSearchImage;
 
     private ProcessedImage selectedResultImage;
 
     private Circle referenceCircle;
 
     double currentSharpness = 1.0;
 
     private final File dir;
 
     private final HScale radiusLower;
 
     private final HScale radiusUpper;
 
     private final HScale maxEccentricity;
 
     private CookieMap scope = CookieMap.emptyCookieMap();
 
     private final ExecutorService executor = Executors.newCachedThreadPool();
 
     private volatile ScheduledExecutorService statsExecutor;
 
     private volatile ScheduledFuture<?> statsFuture;
 
     private volatile int displayedObjects;
 
     private volatile Search search;
 
     private SearchFactory factory;
 
     private AddingCircle addingCircle;
 
     private Main(XML glade, File index) throws IOException {
         fatfind = (Window) getWidget(glade, "fatfind");
         selectedImage = (DrawingArea) getWidget(glade, "selectedImage");
         calibrateRefImage = (DrawingArea) getWidget(glade, "calibrateRefImage");
         calibrateRefInfo = (Label) getWidget(glade, "calibrateRefInfo");
         defineRefImage = (DrawingArea) getWidget(glade, "defineRefImage");
         defineRefInfo = (Label) getWidget(glade, "defineRefInfo");
         simulatedSearch = (DrawingArea) getWidget(glade, "simulatedSearch");
         aboutdialog1 = (AboutDialog) getWidget(glade, "aboutdialog1");
         saveSearchButton = (Button) getWidget(glade, "saveSearchButton");
         startSearch = (Button) getWidget(glade, "startSearch");
         stopSearch = (Button) getWidget(glade, "stopSearch");
         defineScope = (Button) getWidget(glade, "defineScope");
         searchResults = (IconView) getWidget(glade, "searchResults");
         clearSearch = (Button) getWidget(glade, "clearSearch");
         generateHistogram = (Button) getWidget(glade, "generateHistogram");
         selectedResult = (DrawingArea) getWidget(glade, "selectedResult");
         histogramWindow = (Window) getWidget(glade, "histogramWindow");
         histogram = (TextView) getWidget(glade, "histogram");
         calibrationImages = (IconView) getWidget(glade, "calibrationImages");
         definedSearches = (TreeView) getWidget(glade, "definedSearches");
         minSharpness = (Range) getWidget(glade, "minSharpness");
         recomputePreview = (Button) getWidget(glade, "recomputePreview");
         radiusLower = (HScale) getWidget(glade, "radiusLower");
         radiusUpper = (HScale) getWidget(glade, "radiusUpper");
         maxEccentricity = (HScale) getWidget(glade, "maxEccentricity");
         searchName = (Entry) getWidget(glade, "searchName");
         statsLabel = (Label) getWidget(glade, "statsLabel");
 
         dir = index.getParentFile();
 
         connectSignals(glade);
 
         setupThumbnails(index);
 
         setupSavedSearchStore();
 
         setupResultsStore();
     }
 
     private static Widget getWidget(XML glade, String name) {
         Widget w = glade.getWidget(name);
         if (w == null) {
             throw new NullPointerException("widget " + name + " not found");
         }
 
         return w;
     }
 
     private void setupResultsStore() {
         searchResults.setModel(foundItems);
 
         searchResults.setPixbufColumn(foundItemThumbnail);
         searchResults.setTextColumn(foundItemTitle);
     }
 
     private void setupSavedSearchStore() {
         definedSearches.setModel(savedSearchStore);
 
         TreeViewColumn column = definedSearches.appendColumn();
         column.setTitle("Name");
 
         CellRendererText renderer = new CellRendererText(column);
         renderer.setText(savedSearchName);
     }
 
     private void setupThumbnails(File file) throws IOException {
         // get index
         BufferedReader f = new BufferedReader(new FileReader(file));
 
         try {
             // create the model
 
             // get all the thumbnails
             String line;
             while ((line = f.readLine()) != null) {
                 String filename = line.trim();
                 Pixbuf pix = new Pixbuf(new File(dir, filename).getPath(), 150,
                         -1, true);
                 TreeIter row = calibrationImagesModel.appendRow();
                 calibrationImagesModel.setValue(row,
                         calibrationImagesThumbnail, pix);
                 calibrationImagesModel.setValue(row, calibrationImagesFilename,
                         filename);
             }
 
             // set it up
             calibrationImages.setModel(calibrationImagesModel);
             calibrationImages.setPixbufColumn(calibrationImagesThumbnail);
             // calibrationImages.setTextColumn(filenameColumn);
         } finally {
             try {
                 f.close();
             } catch (IOException ignore) {
             }
         }
     }
 
     public static void main(String[] args) throws IOException {
         if (args.length != 1) {
             System.out.println("No image index given on command line");
             System.exit(1);
         }
 
         Gtk.init(args);
         Gtk.setProgramName("FatFind");
 
         XML glade = Glade.parse("fatfind.glade", null);
 
         Main main = new Main(glade, new File(args[0]));
 
         main.showFatFind();
 
         Gtk.main();
     }
 
     private void showFatFind() {
         fatfind.showAll();
     }
 
     private void connectSignals(XML glade) {
         // fatfind
         fatfind.connect(new Window.DeleteEvent() {
             @Override
             public boolean onDeleteEvent(Widget source, Event event) {
                 System.exit(0);
                 return false;
             }
         });
 
         // quit1
         ImageMenuItem quit1 = (ImageMenuItem) getWidget(glade, "quit1");
         quit1.connect(new MenuItem.Activate() {
             @Override
             public void onActivate(MenuItem source) {
                 System.exit(0);
             }
         });
 
         // about1
         ImageMenuItem about1 = (ImageMenuItem) getWidget(glade, "about1");
         about1.connect(new MenuItem.Activate() {
             @Override
             public void onActivate(MenuItem source) {
                 aboutdialog1.show();
             }
         });
 
         // calibrationImages
         calibrationImages.connect(new IconView.SelectionChanged() {
             @Override
             public void onSelectionChanged(IconView source) {
                 // reset reference image
                 setReferenceCircle(null);
 
                 // load the image, find circles, draw offscreen items
                 for (TreePath path : source.getSelectedItems()) {
                     TreeIter item = calibrationImagesModel.getIter(path);
                     String filename = calibrationImagesModel.getValue(item,
                             calibrationImagesFilename);
 
                     try {
                         Pixbuf calibrationPix = new Pixbuf(new File(dir,
                                 filename).getPath());
 
                         // busy cursor
                         // XXX
                         fatfind.getWindow().setCursor(Cursor.BUSY);
 
                         // compute the circles
                         List<Circle> circles = Circle.createFromPixbuf(
                                 calibrationPix, 1.0);
                         calibrationImage = new ProcessedImage(selectedImage,
                                 calibrationPix, circles);
 
                         // reset simulated search sharpness
                         resetSharpness();
 
                         // draw simulated search
                         simulatedSearchImage = new ProcessedImage(
                                 simulatedSearch, calibrationPix, circles);
                         simulatedSearchImage.setShowCircles(true);
                     } catch (IOException e) {
                         e.printStackTrace();
                     } finally {
                         fatfind.getWindow().setCursor(Cursor.NORMAL);
                     }
                 }
             }
         });
 
         // aboutdialog1
         final AboutDialog aboutdialog1 = (AboutDialog) glade
                 .getWidget("aboutdialog1");
         aboutdialog1.connect(new Dialog.Response() {
             @Override
             public void onResponse(Dialog source, ResponseType response) {
                 aboutdialog1.hide();
             }
         });
 
         // selectedImage
         selectedImage.connect(new ExposeEvent() {
             @Override
             public boolean onExposeEvent(Widget source, EventExpose event) {
                 if (calibrationImage != null) {
                     calibrationImage.drawToWidget(Circle.FILTER_BY_IN_RESULT);
                 }
 
                 return true;
             }
         });
 
         selectedImage.connect(new ButtonPressEvent() {
             @Override
             public boolean onButtonPressEvent(Widget source, EventButton event) {
                 if (calibrationImage == null) {
                     return false;
                 }
 
                 if (event.getType() == EventType.BUTTON_PRESS
                         && event.getButton() == MouseButton.LEFT) {
                     Circle c = calibrationImage.getCircleAtPoint((int) event
                             .getX(), (int) event.getY());
                     if (c != null) {
                         setReferenceCircle(c);
                     }
                     return true;
                 }
                 return false;
             }
         });
 
         selectedImage.connect(new MotionNotifyEvent() {
             @Override
             public boolean onMotionNotifyEvent(Widget source, EventMotion event) {
                 if (calibrationImage != null) {
                     calibrationImage.setShowCircles(true);
                 }
                 return false;
             }
         });
 
         selectedImage.connect(new EnterNotifyEvent() {
 
             @Override
             public boolean onEnterNotifyEvent(Widget source, EventCrossing event) {
                 if (calibrationImage != null) {
                     calibrationImage.setShowCircles(true);
                 }
                 return false;
             }
         });
 
         selectedImage.connect(new LeaveNotifyEvent() {
             @Override
             public boolean onLeaveNotifyEvent(Widget source, EventCrossing event) {
                 if (calibrationImage != null) {
                     calibrationImage.setShowCircles(false);
                 }
                 return false;
             }
         });
 
         ValueChanged searchValueChanged = new ValueChanged() {
             @Override
             public void onValueChanged(Range source) {
                 simulatedSearch.queueDraw();
             }
         };
 
         // radiusLower
         radiusLower.connect(searchValueChanged);
 
         // radiusUpper
         radiusUpper.connect(searchValueChanged);
 
         // maxEccentricity
         maxEccentricity.connect(searchValueChanged);
 
         // minSharpness
         minSharpness.connect(new ValueChanged() {
             @Override
             public void onValueChanged(Range source) {
                 double newSharpness = minSharpness.getValue();
                 recomputePreview
                         .setSensitive((currentSharpness != newSharpness)
                                 && (calibrationImage != null));
             }
         });
 
         // recomputePreview
         recomputePreview.connect(new Clicked() {
             @Override
             public void onClicked(Button source) {
                 currentSharpness = minSharpness.getValue();
                 try {
                     List<Circle> circles = Circle.createFromPixbuf(
                             calibrationImage.getOriginal(), currentSharpness);
                     calibrationImage.setCircles(circles);
                     simulatedSearchImage.setCircles(circles);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         });
 
         // simulatedSearch
         simulatedSearch.connect(new ExposeEvent() {
             @Override
             public boolean onExposeEvent(Widget source, EventExpose event) {
                 if (simulatedSearchImage != null) {
                     simulatedSearchImage.drawToWidget(new Circle.Filter() {
                         @Override
                         public boolean filter(Circle c) {
                             if (referenceCircle == null) {
                                 return false;
                             }
 
                             double rMin = radiusLower.getValue();
                             double rMax = radiusUpper.getValue();
                             double maxEccentricity = Main.this.maxEccentricity
                                     .getValue();
 
                             double r = c.getQuadraticMeanRadius();
                             double refR = referenceCircle
                                     .getQuadraticMeanRadius();
 
                             // scale by reference
                             rMin *= refR;
                             rMax *= refR;
 
                             double e = c.getEccentricity();
 
                             return (r >= rMin) && (r <= rMax)
                                     && (e <= maxEccentricity);
                         }
                     });
                 }
 
                 return true;
             }
         });
 
         // searchName
         searchName.connect(new Changed() {
             @Override
             public void onChanged(Editable source) {
                 // change the sensitivity of the save as button, to allow
                 // clicking only when the user has changed to non-empty string
                 String text = searchName.getText();
                 saveSearchButton.setSensitive(!text.isEmpty());
             }
         });
 
         // saveSearchButton
         saveSearchButton.connect(new Clicked() {
             @Override
             public void onClicked(Button source) {
                 if (referenceCircle == null) {
                     return;
                 }
 
                 // scale by reference
                 double a = referenceCircle.getA();
                 double b = referenceCircle.getB();
                 double rMin = radiusLower.getValue() * Math.min(a, b);
                 double rMax = radiusUpper.getValue() * Math.max(a, b);
 
                 String name = searchName.getText();
                 SavedSearch ss = new SavedSearch(name, rMin, rMax,
                         maxEccentricity.getValue(), minSharpness.getValue());
 
                 TreeIter iter = savedSearchStore.appendRow();
                 savedSearchStore.setValue(iter, savedSearchName, name);
                 savedSearchStore.setValue(iter, savedSearchObject, ss);
 
                 saveSearchButton.setSensitive(false);
             }
         });
 
         // startSearch
         startSearch.connect(new Clicked() {
             @Override
             public void onClicked(Button source) {
                 TreeSelection selection = definedSearches.getSelection();
                 TreeIter iter = selection.getSelected();
                 if (iter == null) {
                     return;
                 }
 
                 SavedSearch ss = (SavedSearch) savedSearchStore.getValue(iter,
                         savedSearchObject);
 
                 try {
                     displayedObjects = 0;
                     search = startSearch(ss);
                     runBackgroundSearch(search);
 
                     stopSearch.setSensitive(true);
                     startSearch.setSensitive(false);
                     clearSearch.setSensitive(false);
                 } catch (IOException e) {
                     e.printStackTrace();
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                     Thread.currentThread().interrupt();
                 }
             }
         });
 
         // defineScope
         defineScope.connect(new Clicked() {
             @Override
             public void onClicked(Button source) {
                 try {
                     scope = CookieMap.createDefaultCookieMap();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         });
 
         // searchResults
         searchResults.connect(new SelectionChanged() {
             @Override
             public void onSelectionChanged(IconView source) {
                 // TODO
                 try {
                     for (TreePath path : source.getSelectedItems()) {
                         TreeIter item = foundItems.getIter(path);
                         FatFindResult r = (FatFindResult) foundItems.getValue(
                                 item, foundItemResult);
 
                         // busy cursor
                         // XXX
                         // fatfind.getWindow().setCursor(Cursor.BUSY);
 
                         // get the item
                         Set<String> atts = new HashSet<String>();
                         atts.add("");
                         Result newR = factory.generateResult(r.getId(), atts);
 
                         selectedResultImage = new ProcessedImage(
                                 selectedResult, new Pixbuf(newR.getData()), r
                                         .getCircles());
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         });
 
         // stopSearch
         stopSearch.connect(new Clicked() {
             @Override
             public void onClicked(Button source) {
                 try {
                     search.close();
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                     Thread.currentThread().interrupt();
                 }
             }
         });
 
         // clearSearch
         clearSearch.connect(new Clicked() {
             @Override
             public void onClicked(Button source) {
                 foundItems.clear();
             }
         });
 
         // generateHistogram
         generateHistogram.connect(new Clicked() {
             @Override
             public void onClicked(Button source) {
                 TextBuffer text = histogram.getBuffer();
 
                 histogramWindow.showAll();
 
                 // clear text
                 text.setText("");
 
                 // set font
                 FontDescription fontDesc = new FontDescription("Monospace");
                 histogram.modifyFont(fontDesc);
 
                 // for selected?
                 if (selectedResultImage != null) {
                     addHistogram(text, "selected image", selectedResultImage
                             .getCircles());
                 }
 
                 // for rest
                 List<Circle> allCircles = new ArrayList<Circle>();
 
                 // Walk through the list, reading each row
                 TreeIter iter = foundItems.getIterFirst();
                 if (iter != null) {
                     do {
                         FatFindResult r = (FatFindResult) foundItems.getValue(
                                 iter, foundItemResult);
                         allCircles.addAll(r.getCircles());
                     } while (iter.iterNext());
                 }
 
                 addHistogram(text, "all results", allCircles);
             }
         });
 
         // selectedResult
         selectedResult.connect(new ExposeEvent() {
             @Override
             public boolean onExposeEvent(Widget source, EventExpose event) {
                 if (selectedResultImage == null) {
                     return false;
                 }
 
                 selectedResultImage.drawToWidget(Circle.FILTER_BY_IN_RESULT);
 
                 // drawing circle?
                 if (addingCircle != null) {
                     Context cr = new Context(source.getWindow());
                     addingCircle.draw(cr);
                 }
 
                 return false;
             }
         });
 
         selectedResult.connect(new ButtonPressEvent() {
             @Override
             public boolean onButtonPressEvent(Widget source, EventButton event) {
                 if (selectedResultImage == null) {
                     return false;
                 }
 
                 selectedResultImage.setShowCircles(true);
 
                 if ((event.getType() == EventType.BUTTON_PRESS)
                         && event.getButton() == MouseButton.LEFT) {
                     if (event.getState().contains(ModifierType.SHIFT_MASK)) {
                         // delete (also from circles reference in the result in
                         // the model)
                         boolean deleted = selectedResultImage
                                 .deleteCircleAtPoint((int) event.getX(),
                                         (int) event.getY());
                         if (deleted) {
                             updateFoundItems();
                         }
                     } else {
                         // start add
                         addingCircle = new AddingCircle((int) event.getX(),
                                 (int) event.getY());
                     }
                 }
                 return false;
             }
         });
 
         selectedResult.connect(new ButtonReleaseEvent() {
             @Override
             public boolean onButtonReleaseEvent(Widget source, EventButton event) {
                 if (addingCircle != null) {
                     Circle newCircle = addingCircle
                             .createCircle(selectedResultImage.getScale());
                     double r = addingCircle.getR();
                     addingCircle = null;
 
                     if (r < 1.0) {
                         // too small, this toggles the exclusion filter
                         selectedResultImage.toggleCircleAtPoint((int) event
                                 .getX(), (int) event.getY());
                     } else {
                         // add new circle
                         selectedResultImage.addCircle(newCircle);
                     }
 
                     updateFoundItems();
                 }
 
                 return false;
             }
         });
 
         selectedResult.connect(new MotionNotifyEvent() {
             @Override
             public boolean onMotionNotifyEvent(Widget source, EventMotion event) {
                 if (selectedResultImage != null) {
                     selectedResultImage.setShowCircles(true);
 
                     if (addingCircle != null) {
                         addingCircle.setCurrent((int) event.getX(), (int) event
                                 .getY());
                     }
                     selectedResult.queueDraw();
                 }
                 return false;
             }
         });
 
         selectedResult.connect(new EnterNotifyEvent() {
             @Override
             public boolean onEnterNotifyEvent(Widget source, EventCrossing event) {
                 if (selectedResultImage != null) {
                     selectedResultImage.setShowCircles(true);
                 }
                 return false;
             }
         });
 
         selectedResult.connect(new LeaveNotifyEvent() {
             @Override
             public boolean onLeaveNotifyEvent(Widget source, EventCrossing event) {
                 if (selectedResultImage != null) {
                     selectedResultImage.setShowCircles(false);
                 }
                 return false;
             }
         });
 
         ExposeEvent drawReferenceCircle = new ExposeEvent() {
             @Override
             public boolean onExposeEvent(Widget source, EventExpose event) {
                 if (referenceCircle == null) {
                     return true;
                 }
 
                 Context cr = new Context(source.getWindow());
 
                 Circle circ = referenceCircle;
 
                 double extra = 1.2;
 
                 double r = circ.getQuadraticMeanRadius();
                 double x = circ.getX() - extra * r;
                 double y = circ.getY() - extra * r;
 
                 Pixbuf pix = calibrationImage.getOriginal();
                 int width = pix.getWidth();
                 int height = pix.getHeight();
 
                 int xsize = (int) (2 * extra * r);
                 int ysize = (int) (2 * extra * r);
 
                 int scaledX = 150;
                 int scaledY = 150;
 
                 // draw
                 if (x < 0) {
                     x = 0;
                 }
                 if (y < 0) {
                     y = 0;
                 }
                 if (x + xsize > width) {
                     xsize = (int) (width - x);
                 }
                 if (y + ysize > height) {
                     ysize = (int) (height - y);
                 }
 
                 if (xsize > ysize) {
                     scaledY *= ((double) ysize / (double) xsize);
                 } else if (ysize > xsize) {
                     scaledX *= ((double) xsize / (double) ysize);
                 }
 
                 double thisScale = (double) scaledX / (double) xsize;
 
                 // draw img
                 cr.rectangle(0, 0, scaledX, scaledY);
                 cr.clip();
 
                 cr.save();
                 cr.scale(thisScale, thisScale);
                 cr.setSource(pix, -x, -y);
                 cr.paint();
                 cr.restore();
 
                 cr.translate(-x * thisScale, -y * thisScale);
                 referenceCircle.draw(cr, thisScale, Circle.Fill.HAIRLINE);
 
                 return true;
             }
         };
 
         calibrateRefImage.connect(drawReferenceCircle);
         defineRefImage.connect(drawReferenceCircle);
     }
 
     private static void addHistogram(TextBuffer text, String title,
             List<Circle> circles) {
         List<Double> radii = new ArrayList<Double>();
 
         double minR = Double.MAX_VALUE;
         double maxR = 0.0;
 
         for (Circle c : circles) {
             if (c.isInResult()) {
                 double r = c.getQuadraticMeanRadius();
                 radii.add(r);
                 minR = Math.min(r, minR);
                 maxR = Math.max(r, maxR);
             }
         }
 
         if (maxR == minR) {
             maxR += 10.0;
         }
 
         // print histogram info
         int hist[] = new int[30];
         int total = 0;
         for (int i = 0; i < radii.size(); i++) {
             int cell = (int) ((radii.get(i) - minR) / (maxR - minR) * (hist.length - 1));
             total++;
             hist[cell]++;
         }
 
         TextIter textIter = text.getIter(-1);
         text.insert(textIter, "Histogram for " + title + "\n\n");
 
         double width = (maxR - minR) / hist.length;
         for (int i = 0; i < hist.length; i++) {
             double low = minR + (width * i);
             double high = minR + (width * (i + 1));
 
            text.insert(textIter, String.format(" (%#f -- %#f): %5d\n", low,
                     high, hist[i]));
         }
 
         text.insert(textIter, String.format("\n Total circles: %d\n\n", total));
 
         // insert graphic
         text.insert(textIter, drawHistogram(hist, minR, maxR));
 
         text.insert(textIter, "\n\n\n");
     }
 
     // this is a crappy histogram
     private static Pixbuf drawHistogram(int[] hist, double minR, double maxR) {
         int w = 320;
         int h = 240;
 
         int margin = 5;
 
         // initialize
         BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
         Graphics2D g = img.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
 
         g.setColor(java.awt.Color.WHITE);
         g.fillRect(0, 0, w, h);
 
         int maxCount = -1;
         for (int val : hist) {
             maxCount = Math.max(val, maxCount);
         }
 
         // draw frame
         g.setColor(java.awt.Color.BLACK);
         g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                 BasicStroke.JOIN_MITER));
         Path2D frame = new Path2D.Double();
         frame.moveTo(margin, margin);
         frame.lineTo(margin, h - margin);
         frame.lineTo(w - margin, h - margin);
         g.draw(frame);
 
         // draw bars
         double lineWidth = (w - ((4 + hist.length) * margin)) / hist.length;
         double xAdvance = lineWidth + margin;
         double x = xAdvance / 2.0;
 
         g.setColor(java.awt.Color.RED);
         g.setStroke(new BasicStroke((float) lineWidth, BasicStroke.CAP_BUTT,
                 BasicStroke.JOIN_MITER));
 
         for (int val : hist) {
             double barHeight = ((double) val / (double) maxCount)
                     * (h - (4 * margin));
             x += xAdvance;
             g.draw(new Line2D.Double(x, h - margin, x, h - margin - barHeight));
         }
 
         // done
         g.dispose();
         return createPixbuf(img);
     }
 
     private void runBackgroundSearch(final Search search) {
         statsExecutor = Executors.newSingleThreadScheduledExecutor();
         statsFuture = statsExecutor.scheduleWithFixedDelay(new Runnable() {
             @Override
             public void run() {
                 try {
                     int total = 0;
                     int processed = 0;
                     int dropped = 0;
 
                     Map<String, ServerStatistics> s = search.getStatistics();
                     for (Map.Entry<String, ServerStatistics> e : s.entrySet()) {
                         ServerStatistics v = e.getValue();
                         total += v.getTotalObjects();
                         processed += v.getProcessedObjects();
                         dropped += v.getDroppedObjects();
                     }
 
                     statsLabel
                             .setLabel(String
                                     .format(
                                             "Total objects: %d, Processed objects: %d, Dropped objects: %d, Displayed objects: %d",
                                             total, processed, dropped,
                                             displayedObjects));
                 } catch (SearchClosedException ignore) {
                 } catch (IOException e) {
                     e.printStackTrace();
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                     Thread.currentThread().interrupt();
                 }
             }
         }, 0, 500, TimeUnit.MILLISECONDS);
 
         executor.submit(new Callable<java.lang.Object>() {
             @Override
             public java.lang.Object call() throws Exception {
                 Result r;
                 while (((r = search.getNextResult()) != null)
                         && (displayedObjects <= 100)) {
                     // generate internal result object
                     List<Circle> circles = Circle.createFromDiamondResult(r
                             .getValue("circle-data"));
 
                     ObjectIdentifier id = r.getObjectIdentifier();
 
                     BufferedImage originalThumb = ImageIO
                             .read(new ByteArrayInputStream(r
                                     .getValue("thumbnail.jpeg")));
 
                     int w = Util.extractInt(r.getValue("_cols.int"));
 
                     FatFindResult ffr = new FatFindResult(circles, id,
                             originalThumb, w);
 
                     // add to Gtk
                     TreeIter iter = foundItems.appendRow();
 
                     foundItems.setValue(iter, foundItemThumbnail, ffr
                             .createThumbnail());
                     foundItems.setValue(iter, foundItemTitle, ffr
                             .createThumbnailTitle());
                     foundItems.setValue(iter, foundItemResult, ffr);
 
                     displayedObjects++;
                 }
 
                 if (statsExecutor != null) {
                     statsExecutor.shutdownNow();
                 }
                 if (statsFuture != null) {
                     statsFuture.cancel(true);
                 }
 
                 search.close();
 
                 stopSearch.setSensitive(false);
                 startSearch.setSensitive(true);
                 clearSearch.setSensitive(true);
 
                 return null;
             }
         });
     }
 
     private Search startSearch(SavedSearch ss) throws IOException,
             InterruptedException {
         List<String> emptyList = Collections.emptyList();
 
         List<String> dependencies = new ArrayList<String>();
         dependencies.add("RGB");
 
         List<String> thumbArgs = new ArrayList<String>();
         thumbArgs.add("150");
         thumbArgs.add("150");
 
         List<String> circleArgs = new ArrayList<String>();
         circleArgs.add(Double.toString(ss.getrMin()));
         circleArgs.add(Double.toString(ss.getrMax()));
         circleArgs.add(Double.toString(ss.getMaxEccentricity()));
         circleArgs.add(Double.toString(ss.getMinSharpness()));
 
         List<Filter> filters = new ArrayList<Filter>();
         filters.add(new Filter("RGB",
                 createFilterCode("/opt/snapfind/lib/fil_rgb.so"),
                 "f_eval_img2rgb", "f_init_img2rgb", "f_fini_img2rgb", 1,
                 emptyList, emptyList));
         filters.add(new Filter("thumbnailer",
                 createFilterCode("/opt/snapfind/lib/fil_thumb.so"),
                 "f_eval_thumbnailer", "f_init_thumbnailer",
                 "f_fini_thumbnailer", 1, dependencies, thumbArgs));
         filters.add(new Filter("circles",
                 createFilterCode("/usr/share/fatfind/filter/fil_circle.so"),
                 "f_eval_circles", "f_init_circles", "f_fini_circles", 1,
                 dependencies, circleArgs));
 
         List<String> applicationDependencies = new ArrayList<String>();
         applicationDependencies.add("RGB");
 
         factory = new SearchFactory(filters, applicationDependencies, scope);
 
         Set<String> pushAttributes = new HashSet<String>();
         pushAttributes.add("thumbnail.jpeg");
         pushAttributes.add("circle-data");
         pushAttributes.add("_rows.int");
         pushAttributes.add("_cols.int");
 
         return factory.createSearch(pushAttributes);
     }
 
     private static FilterCode createFilterCode(String path) throws IOException {
         InputStream in = null;
         try {
             in = new BufferedInputStream(new FileInputStream(path));
             return new FilterCode(in);
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException ignore) {
                 }
             }
         }
     }
 
     private void resetSharpness() {
         currentSharpness = 1;
         minSharpness.setValue(currentSharpness);
         recomputePreview.setSensitive(false);
     }
 
     private void setReferenceCircle(Circle c) {
         if (referenceCircle == c) {
             return;
         }
 
         Label text = calibrateRefInfo;
         Label text2 = defineRefInfo;
 
         referenceCircle = c;
 
         if (c == null) {
             // clear the text
             text.setLabel("");
             text2.setLabel("");
 
             saveSearchButton.setSensitive(false);
         } else {
             // set text
             double r = c.getQuadraticMeanRadius();
             double e = c.getEccentricity();
 
             String newText = String.format(
                     "Quadratic mean radius: %g\nEccentricity: %.2g%%\n", r,
                     e * 100.0);
             text.setLabel(newText);
             text2.setLabel(newText);
         }
 
         calibrateRefImage.queueDraw();
         defineRefImage.queueDraw();
     }
 
     private void updateFoundItems() {
         TreePath path = searchResults.getSelectedItems()[0];
         TreeIter iter = foundItems.getIter(path);
 
         FatFindResult r = (FatFindResult) foundItems.getValue(iter,
                 foundItemResult);
         foundItems.setValue(iter, foundItemThumbnail, r.createThumbnail());
         foundItems.setValue(iter, foundItemTitle, r.createThumbnailTitle());
     }
 
     static Pixbuf createPixbuf(BufferedImage img) {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         try {
             // convert to PPM
             String ppmHeader = "P6\n" + img.getWidth() + " " + img.getHeight()
                     + "\n255\n";
 
             out.write(ppmHeader.getBytes("UTF-8"));
 
             int data[] = ((DataBufferInt) img.getRaster().getDataBuffer())
                     .getData();
             for (int d : data) {
                 int r = (d >> 16) & 0xFF;
                 int g = (d >> 8) & 0xFF;
                 int b = d & 0xFF;
                 out.write(r);
                 out.write(g);
                 out.write(b);
             }
 
             return new Pixbuf(out.toByteArray());
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         throw new IllegalStateException("Should never happen");
     }
 }
