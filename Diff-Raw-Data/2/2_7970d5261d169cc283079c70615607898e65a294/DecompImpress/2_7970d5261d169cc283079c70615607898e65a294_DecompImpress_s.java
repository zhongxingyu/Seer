 /*
  * DecompImpress.java
  *
  * Created on 2010.01.12
  *
  */
 
 package edu.umich.med.umms;
 
 import com.sun.star.awt.Point;
 import com.sun.star.awt.Size;
 import com.sun.star.awt.XBitmap;
 
 import com.sun.star.beans.PropertyValue;
 import com.sun.star.beans.PropertyVetoException;
 import com.sun.star.beans.XPropertySet;
 import com.sun.star.beans.UnknownPropertyException;
 
 import com.sun.star.drawing.XDrawPagesSupplier;
 import com.sun.star.drawing.XDrawPages;
 import com.sun.star.drawing.XDrawPage;
 import com.sun.star.drawing.XDrawPageDuplicator;
 import com.sun.star.drawing.XShapes;
 import com.sun.star.drawing.XShape;
 import com.sun.star.frame.XController;
 import com.sun.star.frame.XDesktop;
 import com.sun.star.frame.XDispatchHelper;
 import com.sun.star.frame.XDispatchProvider;
 import com.sun.star.frame.XFrame;
 import com.sun.star.frame.XModel;
 import com.sun.star.lang.IllegalArgumentException;
 import com.sun.star.lang.XMultiComponentFactory;
 import com.sun.star.lang.XMultiServiceFactory;
 import com.sun.star.lang.XComponent;
 import com.sun.star.lang.IndexOutOfBoundsException;
 import com.sun.star.lang.WrappedTargetException;
 
 import com.sun.star.graphic.XGraphic;
 import com.sun.star.graphic.XGraphicProvider;
 
 import com.sun.star.lib.uno.adapter.ByteArrayToXInputStreamAdapter;
 import com.sun.star.style.ParagraphAdjust;
 import com.sun.star.text.ControlCharacter;
 import com.sun.star.text.XText;
 import com.sun.star.text.XTextCursor;
 import com.sun.star.text.XTextRange;
 
 import com.sun.star.uno.Exception;
 import com.sun.star.uno.UnoRuntime;
 import com.sun.star.uno.XComponentContext;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  *
  * @author kwc@umich.edu
  */
 public class DecompImpress {
 
     private com.spinn3r.log5j.Logger mylog = com.spinn3r.log5j.Logger.getLogger();
     private org.apache.log4j.Level myLogLevel = org.apache.log4j.Level.WARN;
 
     DecompImpress(org.apache.log4j.Level lvl)
     {
         mylog = com.spinn3r.log5j.Logger.getLogger();
         mylog.setLevel(lvl);
     }
     
     DecompImpress()
     {
         mylog = com.spinn3r.log5j.Logger.getLogger();
     }
 
     public void setLoggingLevel(org.apache.log4j.Level lvl)
     {
         myLogLevel = lvl;
         mylog.setLevel(myLogLevel);
     }
 
     public int extractImages(XComponentContext xContext,
                               XMultiComponentFactory xMCF,
                               XComponent xCompDoc,
                               String outputDir,
                               boolean includeCustomShapes)
     {
         // Query for the XDrawPagesSupplier interface
         XDrawPagesSupplier xDrawPagesSuppl =
                 (XDrawPagesSupplier) UnoRuntime.queryInterface(XDrawPagesSupplier.class, xCompDoc);
         if (xDrawPagesSuppl == null) {
             mylog.error("Cannot get XDrawPagesSupplier interface for Presentation Document???");
             return 8;
         }
         DecompUtil du = new DecompUtil();
         du.setLoggingLevel(myLogLevel);
 
         try {
             XDrawPages xDrawPages = xDrawPagesSuppl.getDrawPages();
             Object firstPage = xDrawPages.getByIndex(0);
             int pageCount = xDrawPages.getCount();
             mylog.debug("xDrawPages.getCount returned a value of '%d' pages", pageCount);
             XDrawPage currPage = null;
             Class pageClass = null;
             XPropertySet pageProps = null;
 
             // Loop through all the pages of the document
             for (int p = 0; p < pageCount; p++) {
                 currPage = getDrawPage(xDrawPages, p);
                 if (currPage == null) {
                     mylog.error("Failed to get currPage at page %d!", p+1);
                     xCompDoc.dispose();
                     return 22;
                 }
                 mylog.debug("=== Working with page %d ===", p+1);
                 du.exportContextImage(xContext, xMCF, currPage, outputDir, p+1);
 
                 int shapeCount = currPage.getCount();
                 mylog.debug("Page %d has %d shapes", p+1, shapeCount);
                 XShape currShape = null;
 
                 // Loop through all the shapes within the page
                 for (int s = 0; s < shapeCount; s++) {
 
                     String pictureURL = null;
 
                     currShape = getPageShape(currPage, s);
                     if (currShape == null) {
                         mylog.error("Failed to get currShape (%d) from page %d!", s, p+1);
                         xCompDoc.dispose();
                         return 33;
                     }
                     String currType = currShape.getShapeType();
                     com.sun.star.awt.Size shapeSize = currShape.getSize();
                     com.sun.star.awt.Point shapePoint = currShape.getPosition();
                     mylog.debug("--- Working with shape %d (At %d:%d, size %dx%d)\ttype: %s---", s, shapePoint.X, shapePoint.Y, shapeSize.Width, shapeSize.Height, currType);
 
                     try{
                         //du.printShapeProperties(origShape);
                     } catch (java.lang.Exception ex) {
                         // Never mind, don't print them!
                     }
 
                     /* Note that we specifically ignore TitleTextShape, OutlinerShape, and LineShape */
                     if (currType.equalsIgnoreCase("com.sun.star.drawing.GraphicObjectShape")) {
                         /*
                          * Note that GraphicObjectShape is handled differently so that we can keep
                          * the image in the original format.  du.exportImage saves everything as .png
                          *
                          * The try/catch below is used to catch cases where GraphicStreamURL is not set.
                          * In that case we fall back to GraphicURL.
                          */
                         mylog.debug("Handling GraphicObjectShape (%d) on page %d", s, p+1);
                         XPropertySet shapeProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, currShape);
                         try {
                             pictureURL = shapeProps.getPropertyValue("GraphicStreamURL").toString();
                             pictureURL = pictureURL.substring(30);  // Chop off the leading   "vnd.sun.star.Package:Pictures/"
                         } catch (java.lang.Exception ex) {
                             try {
                                 pictureURL = shapeProps.getPropertyValue("GraphicURL").toString();
                                 pictureURL = pictureURL.substring(27);  // Chop off the leading "vnd.sun.star.GraphicObject:"
                             } catch (java.lang.Exception e) {
                                 mylog.debug("extractImages: failed to get GraphicStreamURL or GraphicURL for GraphicObjectShape %d on page %d", s+1, p+1);
                             }
                         }
                         if (pictureURL == null) {
                             du.exportImage(xContext, xMCF, currShape, outputDir, p+1, s);
                         } else {
                             String outName = DecompUtil.constructBaseImageName(outputDir, p+1, s);
                             du.extractImageByURL(xContext, xMCF, xCompDoc, pictureURL, outName);
                         }
                     } else if (currType.equalsIgnoreCase("com.sun.star.drawing.OLE2Shape")) {
                         mylog.debug("Handling OLE2Shape (%d) on page %d", s, p+1);
                         du.exportImage(xContext, xMCF, currShape, outputDir, p+1, s);
                     } else if (currType.equalsIgnoreCase("com.sun.star.drawing.TableShape")) {
                         mylog.debug("Handling TableShape (%d) on page %d", s, p+1);
                         du.exportImage(xContext, xMCF, currShape, outputDir, p+1, s);
                     } else if (currType.equalsIgnoreCase("com.sun.star.drawing.GroupShape")) {
                         mylog.debug("Handling GroupShape (%d) on page %d", s, p+1);
                         du.exportImage(xContext, xMCF, currShape, outputDir, p + 1, s);
                     } else if (currType.equalsIgnoreCase("com.sun.star.drawing.CustomShape")) {
                         XPropertySet shapeProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, currShape);
                         String fillBmURL = shapeProps.getPropertyValue("FillBitmapURL").toString();
                         if (fillBmURL.contains("10000000000000200000002000309F1C") || fillBmURL.contains("00000000000000000000000000000000")) {
                             mylog.debug("SKIPPING boring image with fillBitmapURL '%s'", fillBmURL);
                         } else {
                             mylog.debug("Handling CustomShape (%d) on page %d, with fillBitmapURL of '%s'", s, p+1, fillBmURL);
                             du.exportImage(xContext, xMCF, currShape, outputDir, p+1, s);
                         }
                     } else {
                         mylog.debug("SKIPPING unhandled shape type '%s' (%d) on page %d", currType, s, p+1);
                     }
                 }
             }
         } catch (IndexOutOfBoundsException ex) {
             mylog.error("extractImages: Caught IndexOutOfBoundsException: %s", ex.getMessage());
             return 40;
             //Logger.getLogger(OpenOfficeUNODecomposition.class.getName()).log(Level.SEVERE, null, ex);
         } catch (WrappedTargetException ex) {
             mylog.error("extractImages: Caught WrappedTargetException: %s", ex.getMessage());
             return 41;
             //Logger.getLogger(OpenOfficeUNODecomposition.class.getName()).log(Level.SEVERE, null, ex);
         } catch (java.lang.Exception ex) {
             mylog.error("extractImages: Caught Exception: %s", ex.getMessage());
             return 42;
         }
         return 0;
     }
 
     private Size _calculateReplacementProportions(Size orig, Size repl)
     {
         Size newSize = new Size();
         float newHeight, newWidth;
 
         mylog.debug("The original size is " + orig.Width + " by " + orig.Height);
         mylog.debug("The replacement image size is " + repl.Width + " by " + repl.Height);
 
         float orig_prop = (float) orig.Width / (float) orig.Height;
         float repl_prop = (float) repl.Width / (float) repl.Height;
         mylog.debug("The proportion of the original image is: " + orig_prop);
         mylog.debug("The proportion of the replacement image is: " + repl_prop);
 
         if (orig_prop > repl_prop) {
             // Bound the replacement to the original's height
             newSize.Height = orig.Height;
             newWidth = (float) newSize.Height * repl_prop;
             newSize.Width = (int) newWidth;
         } else {
             // Bound the replacement to the original's width
             newSize.Width = orig.Width;
             newHeight = (float) newSize.Width / repl_prop;
             newSize.Height = (int) newHeight;
         }
 
         mylog.debug("The new proportional size is " + newSize.Width + " by " + newSize.Height);
         return newSize;
     }
 
     private void _printPageShapeInfo(String header, int p, XDrawPage page)
     {
         int shapeCount = page.getCount();
         int i;
         mylog.debug("*** " + header + " on page " + (p+1) + " with " + shapeCount + " shapes ***");
         for (i = 0; i < shapeCount; i++) {
             XShape shape = getPageShape(page, i);
             String type = shape.getShapeType();
             Point location = shape.getPosition();
             Size size = shape.getSize();
             mylog.debug("\t shape %02d at location %05d,%05d with size %05dx%05d has type %s",
                     i, location.X, location.Y, size.Width, size.Height, type);
         }
     }
 
 
     /*
      * Original is from http://www.oooforum.org/forum/viewtopic.phtml?t=81870
      * Original code was dealing with Text Documents.  This is for Drawing
      * Documents.  See:
      *   http://wiki.services.openoffice.org/wiki/Documentation/DevGuide/Drawings/Navigating
      * which says,
      *
      *    "Initially, shapes in a document can only be accessed by their index.
      *    The only method to get more information about a shape on the page is
      *    to test for the shape type, so it is impossible to identify a
      *    particular shape. However, after a shape is inserted, you can name
      *    it in the user interface or through the shape interface
      *    com.sun.star.container.XNamed, and identify the shape by its name
      *    after retrieving it by index. Shapes cannot be accessed by their names."
      *    Arrgghh!!
      *
      * XXX So for Drawing documents, we need to keep track of index values.  But
      * what if we insert a new image?  Doesn't that change the index values?
      * Need to understand the answer to that!
      *
      */
     public int replaceImage(XComponentContext xContext,
                             XMultiComponentFactory xMCF,
                             XComponent xCompDoc,
                             String originalImageName,
                             String replacementURL,
                             int p,
                             int s,
                             DecompDelayedRemovalCollection drc)
     {
 
         XDrawPage origPage = null;
         XShape origShape = null;
         XGraphicProvider xGraphicProvider = null;
         XGraphic xNewGraphic = null;
         XBitmap xBitmap = null;
 
         DecompUtil du = new DecompUtil();
         du.setLoggingLevel(myLogLevel);
 
         mylog.debug("=== Replacing image: page %d, shape %d ===", p+1, s);
 
         // Query for the XDrawPagesSupplier interface and get original page and shape
         XDrawPagesSupplier xDrawPagesSuppl =
                 (XDrawPagesSupplier) UnoRuntime.queryInterface(XDrawPagesSupplier.class, xCompDoc);
         if (xDrawPagesSuppl == null) {
             mylog.error("Cannot get XDrawPagesSupplier interface for Presentation Document???");
             return(2);
         }
         XDrawPages xDrawPages = xDrawPagesSuppl.getDrawPages();
 
         origPage = getDrawPage(xDrawPages, p);
         if (origPage == null) {
             mylog.error("Failed to get page %d, with index number %d!", p+1, p);
             return(3);
         }
         //_printPageShapeInfo("Before replacing shape", p, origPage);
 
         // int shapeCount = origPage.getCount();
         // mylog.debug("Page %d has %d shapes", p+1, shapeCount);
 
         origShape = getPageShape(origPage, s);
         if (origShape == null) {
             mylog.error("Failed to get shape %d, for page %d!", s, p+1);
             return(4);
         }
 
         try{
             //du.printShapeProperties(origShape);
         } catch (java.lang.Exception ex) {
             // Never mind, don't print them!
         }
 
         // Make sure we can handle the Shape to be replaced before continuing
         String origType = origShape.getShapeType();
         mylog.debug("Working with shape of type: '%s'", origType);
 
         if (!origType.equalsIgnoreCase("com.sun.star.drawing.GraphicObjectShape") &&
             !origType.equalsIgnoreCase("com.sun.star.drawing.CustomShape") &&
             !origType.equalsIgnoreCase("com.sun.star.drawing.GroupShape") &&
             !origType.equalsIgnoreCase("com.sun.star.drawing.TableShape") &&
             !origType.equalsIgnoreCase("com.sun.star.drawing.OLE2Shape")
            ) {
             mylog.error("No support to replace shape of type: " + origType);
             return(5);
         }
 
         // Read the replacement image as an XInputStream
         byte[] replacementByteArray = DecompUtil.getImageByteStream(replacementURL);
         if (replacementByteArray == null) {
             mylog.error("Unable to read data from replacement file: " + replacementURL);
             return(1);
         }
         ByteArrayToXInputStreamAdapter xReplStream = new ByteArrayToXInputStreamAdapter(replacementByteArray);
 
 
         try {
 
             xGraphicProvider = (XGraphicProvider) UnoRuntime.queryInterface(
                 XGraphicProvider.class,
                 xMCF.createInstanceWithContext(
                 "com.sun.star.graphic.GraphicProvider", xContext));
         } catch (Exception ex) {
             mylog.error("Could not get GraphicProvider interface!");
             return(6);
         }
 
         // Create new XGraphic image object
         try {
             PropertyValue[] sourceProps = new PropertyValue[1];
             sourceProps[0] = new PropertyValue();
             sourceProps[0].Name = "InputStream";
             sourceProps[0].Value = xReplStream;
 
             xNewGraphic = xGraphicProvider.queryGraphic(sourceProps);
         } catch (Exception ex) {
             mylog.error("Could not create XGraphic from replacement XInputStream!");
             return(7);
         }
 
         // Use the original size to bound the replacement size and keep the replacement's aspect ratio
         xBitmap = (XBitmap) UnoRuntime.queryInterface(XBitmap.class, xNewGraphic);
         Size replSize = xBitmap.getSize();
         Size origSize = origShape.getSize();
         Size newSize = _calculateReplacementProportions(origSize, replSize);
 
         // If replacing a GraphicObjectShape, just modify the original
         // Otherwise, add a new GraphicObjectShape and remove the original
         if (origType.equalsIgnoreCase("com.sun.star.drawing.GraphicObjectShape")) {
             try {
                 origShape.setSize(newSize);
             } catch (Exception ex) {
                 mylog.error("Shape rejected attempt to setSize");
                 return(8);
             }
 
             XPropertySet origProps = DecompUtil.getObjectPropertySet(origShape);
             try {
                 origProps.setPropertyValue("Graphic", xNewGraphic);
             } catch (Exception ex) {
                 mylog.error("Could not set Graphic property: " + ex.getMessage());
                 return(9);
             }
         } else {
             XShape replShape = null;
             try {
                 XMultiServiceFactory xDrawFactory = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, xCompDoc);
                 //                replShape = (XShape) UnoRuntime.queryInterface(XShape.class,
                 // replShape = (XShape) xMCF.createInstanceWithContext("com.sun.star.drawing.GraphicObjectShape", xContext);
                 Object oShape = xDrawFactory.createInstance("com.sun.star.drawing.GraphicObjectShape");
                 replShape = (XShape) UnoRuntime.queryInterface(XShape.class, oShape);
             } catch (Exception ex) {
                 mylog.error("Could not create new GraphicObjectShape");
                 return(10);
             }
             XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, origPage);
             xShapes.add(replShape);
 
             try {
                 replShape.setSize(newSize);
             } catch (Exception ex) {
                 mylog.error("New shape rejected attempt to setSize");
                 return(11);
             }
             replShape.setPosition(origShape.getPosition());
             XPropertySet replProps = DecompUtil.getObjectPropertySet(replShape);
             try {
                 replProps.setPropertyValue("Graphic", xNewGraphic);
             } catch (Exception ex) {
                 mylog.error("Count net set Graphic property: " + ex.getMessage());
                 return(12);
             }
 
             drc.addDelayedRemovalEntry(p, s);
         }
 
         //_printPageShapeInfo("After replacing shape", p, origPage);
         return 0;
     }
 
     public int removeImages(XComponentContext xContext,
                             XMultiComponentFactory xMCF,
                             XComponent xCompDoc,
                             DecompDelayedRemovalCollection drc)
     {
         XDrawPage page = null;
         XShape shape = null;
         int p, s, i;
         DecompDelayedRemovalCollection.DecompDelayedRemovalCollectionEntry[] drcArray;
 
         if (drc.numEntries() == 0)
             return 0;
 
         // Query for the XDrawPagesSupplier interface
         XDrawPagesSupplier xDrawPagesSuppl =
                 (XDrawPagesSupplier) UnoRuntime.queryInterface(XDrawPagesSupplier.class, xCompDoc);
         if (xDrawPagesSuppl == null) {
             mylog.error("Cannot get XDrawPagesSupplier interface for Presentation Document???");
             return(2);
         }
         XDrawPages xDrawPages = xDrawPagesSuppl.getDrawPages();
 
         drcArray = drc.getDecompDelayedRemovalCollectionEntryArray();
 
         for (i = 0; i < drcArray.length; i++) {
             p = drcArray[i].pageNum;
             s = drcArray[i].imageNum;
 
             page = getDrawPage(xDrawPages, p);
             if (page == null) {
                 mylog.error("Failed to get page %d, with index number %d!", p+1, p);
                 continue;
             }
 
             shape = getPageShape(page, s);
             if (shape == null) {
                 mylog.error("Failed to get shape %d, for page %d!", s, p+1);
                 continue;
             }
             page.remove(shape);
         }
         return 0;
     }
 
     /**
      * Utility function to add text to the full citation page(s)
      *
      * @param shape
      * @param text
      * @param fontname
      * @param fontsize
      * @param prependParagraph
      * @param centerParagraph
      * @param doubleSpaced
      * @return
      */
     private int insertCitationPageText(XShape shape, String text, String fontname, int fontsize,
             boolean prependParagraph, boolean centerParagraph, boolean doubleSpaced)
     {
         try {
             XText xText = (XText) UnoRuntime.queryInterface(XText.class, shape);
             XTextCursor xTextCursor = xText.createTextCursor();
             XTextRange cursorRange = (XTextRange) UnoRuntime.queryInterface(XTextRange.class, xTextCursor);
             XPropertySet xTxtProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xTextCursor);
 
             xTextCursor.gotoEnd(false);
             if (prependParagraph)
                 xText.insertControlCharacter(cursorRange, ControlCharacter.PARAGRAPH_BREAK, false);
             if (centerParagraph)
                 xTxtProps.setPropertyValue("ParaAdjust", ParagraphAdjust.CENTER);
             if (doubleSpaced) {
                 xTxtProps.setPropertyValue("ParaTopMargin", 200);
             }
             xTxtProps.setPropertyValue("CharFontName", fontname);
             xTxtProps.setPropertyValue("CharHeight", fontsize);
             xText.insertString(cursorRange, text, false);
         } catch (java.lang.Exception ex) {
             mylog.error("insertCitationPageText: Caught Exception: " + ex.getMessage());
             return 1;
         }
         return 0;
     }
 
     /**
      * Adds full citation text to the citation page
      *
      * @param xCompDoc
      * @param cShape
      * @param p
      * @param s
      * @param i
      * @param citationString
      * @param citationLicense
      * @param citationLicenseBadgeURL
      * @param newParagraph
      * @return
      */
     private int insertFullCitation(XComponent xCompDoc,
                                    XShape cShape,
                                    int p,     // Cited image page number
                                    int s,     // Cited image shape number
                                    int i,     // index of citation for this page
                                    String citationString,
                                    String citationLicense,
                                    String citationLicenseBadgeURL,
                                    boolean newParagraph)
     {
         try {
 
             // Note that the order of operations here is important!!
             StringBuffer fullCite = new StringBuffer();
 
             fullCite.append("Slide " + p + ", Image " + s + ": ");
             fullCite.append(citationString);
             if (citationLicense != null)
                 fullCite.append(", " + citationLicense);
             if (citationLicenseBadgeURL != null)
                 fullCite.append(", " + citationLicenseBadgeURL);
             insertCitationPageText(cShape, fullCite.toString(), "Arial", 10, newParagraph, false, true);
 
         } catch (java.lang.Exception ex) {
             mylog.error("insertFullCitation: Caught Exception!");
             //Logger.getLogger(DecompImpress.class.getName()).log(Level.SEVERE, null, ex);
             return 1;
         }
         return 0;
     }
 
 
     /**
      * Add a new page to the presentation to contain full citation information
      * 
      * @param xCompDoc
      * @return
      */
     private XShape addCitationPage(XComponent xCompDoc)
     {
 
         XDrawPage newPage = null;
         XShape citeShape = null;
         try {
             XDrawPagesSupplier xDrawPagesSuppl = (XDrawPagesSupplier) UnoRuntime.queryInterface(XDrawPagesSupplier.class, xCompDoc);
             if (xDrawPagesSuppl == null) {
                 //mylog.debug("Failed to get xDrawPagesSuppl from xComp");
                 return null;
             }
             XDrawPages xDrawPages = xDrawPagesSuppl.getDrawPages();
             int currentCount = xDrawPages.getCount();
             newPage = xDrawPages.insertNewByIndex(currentCount);
 
             XShape titleShape = DecompUtil.createShape(xCompDoc, new Point(1000, 1000), new Size(23000, 500), "com.sun.star.drawing.TextShape");
             newPage.add(titleShape);
             insertCitationPageText(titleShape, "Additional Source Information", "Arial", 24, false, true, false);
             insertCitationPageText(titleShape, "for more information see: http://open.umich.edu/wiki/CitationPolicy", "Arial", 12, true, true, false);
 
             citeShape = DecompUtil.createShape(xCompDoc, new Point(1000, 3000), new Size(23000, 14000), "com.sun.star.drawing.TextShape");
             newPage.add(citeShape);
         } finally {
             return citeShape;
         }
     }
 
     /**
      * Main routine to add one or more citation pages to the end of the presentation
      * 
      * @param xCompDoc
      * @param entries
      * @param pageOffset
      * @return
      */
     public int addCitationPages(XComponent xCompDoc, DecompCitationCollection.DecompCitationCollectionEntry[] entries, int pageOffset)
     {
         int i;
         int perPage = 15;
         XShape cShape = null;
         XDrawPage cPage = null;
         DecompCitationCollection.DecompCitationCollectionEntry cpe;
 
         for (i = 0; i < entries.length; i++)
         {
             if (i % perPage == 0)
                 cShape = addCitationPage(xCompDoc);
 
             cpe = entries[i];
             boolean newParagraph = (i % perPage != 0);
             insertFullCitation(xCompDoc, cShape, (cpe.pageNum + pageOffset + 1), cpe.imageNum, (i % perPage), cpe.fullCitation, null, null, newParagraph);
         }
         return 0;
     }
 
     /**
      * Adds citation information for an image to the slide.
      * Originally from http://www.oooforum.org/forum/viewtopic.phtml?t=45734
      *
      * @param xContext
      * @param xMCF
      * @param xCompDoc
      * @param citationText
      * @param citationURL
      * @param p
      * @param s
      * @return
      */
     public int insertImageCitation(XComponentContext xContext,
                                    XMultiComponentFactory xMCF,
                                    XComponent xCompDoc,
                                    String citationText,
                                    //String citationURL,
                                    int p,
                                    int s)
     {
         XDrawPage drawPage;
         try {
 
             String shortCitation = null, licenseURL = null, badgeURL = null;
             XShapes xShapes;
             XShape xCIShape = null, xOrigImage = null;
             CitationManipulate citemanip = new CitationManipulate(citationText);
 
             drawPage = getDrawPageByIndex(xCompDoc, p);
             if (drawPage == null)
                 return 1;
             //_printPageShapeInfo("Before adding citation", p, drawPage);
             //mylog.debug("drawPage.getCount says there are %d objects\n", drawPage.getCount());
             xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, drawPage);
             Object oOrigImage = xShapes.getByIndex(s);
             XPropertySet xOrigPropSet = (XPropertySet)
                     UnoRuntime.queryInterface(XPropertySet.class, oOrigImage);
             xOrigImage = (XShape) UnoRuntime.queryInterface(XShape.class, oOrigImage);
 
             shortCitation = citemanip.getShortCitation();
             badgeURL = citemanip.getBadgeURL();
 
             if (badgeURL != null) {
                 // Add citation image
                 String convertedURL = DecompUtil.getInternalURL(xCompDoc, badgeURL, badgeURL);
 
                 // Calculate citation image location using original image properties
                 Point citeImagePos = DecompUtil.calculateCitationImagePosition(xOrigImage);
                 Size citeImageSize = DecompUtil.calculateCitationImageSize(xOrigImage);
 
                 xCIShape = DecompUtil.createShape(xCompDoc, citeImagePos, citeImageSize,
                                         "com.sun.star.drawing.GraphicObjectShape");
                 XPropertySet xImageProps = (XPropertySet)
                         UnoRuntime.queryInterface(XPropertySet.class, xCIShape);
                 xImageProps.setPropertyValue("GraphicURL", convertedURL);
                 xShapes.add(xCIShape);
                 //mylog.debug("drawPage.getCount nows says there are %d objects\n", drawPage.getCount());
             }
  
             if (shortCitation != null) {
                 // Caclulate citation text location using citation image location
                 Point citeTextPos = DecompUtil.calculateCitationTextPosition(xOrigImage, xCIShape);
                 Size citeTextSize = DecompUtil.calculateCitationTextSize(xOrigImage, xCIShape);
 
                 // Add citation text
                 XShape xCTShape = DecompUtil.createShape(xCompDoc, citeTextPos, citeTextSize,
                                         "com.sun.star.drawing.TextShape");  // There is also a TextShape?
                 xShapes.add(xCTShape);
 
                 XText xText = (XText) UnoRuntime.queryInterface(XText.class, xCTShape);
                 XTextCursor xTextCursor = xText.createTextCursor();
                 XPropertySet xTxtProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xTextCursor);
 
                 xTxtProps.setPropertyValue("CharFontName", "Arial");
                 xTxtProps.setPropertyValue("CharHeight", 8);
                 xText.setString(shortCitation);
             }
 
         } catch (java.lang.Exception ex) {
             mylog.error("insertImageCitation: Caught exception: " + ex.getMessage());
             return 1;
         }
         //_printPageShapeInfo("After adding citation", p, drawPage);
         return 0;
     }
 
     /**
      * Copy the background properties from the srcPage to the destPage
      * If src has no specific background, then the destination background is unchanged
      *
      * @param destPage
      * @param srcPage
      */
     private void copyBackground(XDrawPage destPage, XDrawPage srcPage)
     {
         XPropertySet srcPageProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, srcPage);
         XPropertySet dstPageProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, destPage);
 
         try {
             Object srcBG = srcPageProps.getPropertyValue("Background");
             dstPageProps.setPropertyValue("Background", srcBG);
         } catch (UnknownPropertyException ex) {
             mylog.info("copyBackground: Source page has no Background property!\n");
         } catch (PropertyVetoException ex) {
             mylog.info("copyBackground: Destination page does not accept Background property!\n");
         } catch (Exception ex) {
             mylog.error("copyBackground: error while getting/setting Background property!\n");
         }
     }
 
 
     public int insertFrontBoilerplate(XComponentContext xContext,
                                       XDesktop xDesktop,
                                       XMultiComponentFactory xMCF,
                                       XComponent destDoc,
                                       String srcFileUrl)
     {
         XComponent srcDoc = null;
         XDrawPagesSupplier destPagesSuppl;
         XDrawPagesSupplier srcPagesSuppl;
         PropertyValue props[] = new PropertyValue[0];
 
         if (srcFileUrl == null)
             return 0;
 
         // Query for the XDrawPagesSupplier interfaces
         try {
             srcDoc = DecompUtil.openFileForProcessing(xDesktop, srcFileUrl);
         } catch (java.lang.Exception ex) {
             mylog.error("insertFrontBoilerplate: Exception (" + ex.getMessage() + ") while opening source file: " + srcFileUrl);
             return -1;
         }
 
         srcPagesSuppl =
                 (XDrawPagesSupplier) UnoRuntime.queryInterface(XDrawPagesSupplier.class, srcDoc);
         if (srcPagesSuppl == null) {
             mylog.error("Cannot get XDrawPagesSupplier interface for source Presentation Document???");
             srcDoc.dispose();
             return -1;
         }
 
         // Duplicate the original first page since we can't insert before it...
         XDrawPage destDP = getDrawPageByIndex(destDoc, 0);
        XDrawPageDuplicator xdup = UnoRuntime.queryInterface(XDrawPageDuplicator.class, destDoc);
         xdup.duplicate(destDP);
 
         XDrawPages srcDrawPages = srcPagesSuppl.getDrawPages();
         int srcCount = srcDrawPages.getCount();
 
         // First, get both documents into the correct Mode
         executeDispatch(xContext, xDesktop, xMCF, destDoc, ".uno:DrawingMode", props);
         executeDispatch(xContext, xDesktop, xMCF, srcDoc, ".uno:DrawingMode", props);
 
         //executeDispatch(xContext, xDesktop, xMCF, destDoc, ".uno:NormalMultiPaneGUI", props);
         //executeDispatch(xContext, xDesktop, xMCF, srcDoc, ".uno:NormalMultiPaneGUI", props);
         //executeDispatch(xContext, xDesktop, xMCF, destDoc, ".uno:PageMode", props);
         //executeDispatch(xContext, xDesktop, xMCF, srcDoc, ".uno:PageMode", props);
         //executeDispatch(xContext, xDesktop, xMCF, destDoc, ".uno:InsertMode", props);
         //executeDispatch(xContext, xDesktop, xMCF, srcDoc, ".uno:InsertMode", props);
         //executeDispatch(xContext, xDesktop, xMCF, destDoc, ".uno:AdvancedMode", props);
         //executeDispatch(xContext, xDesktop, xMCF, srcDoc, ".uno:AdvancedMode", props);
 
         for (int i = 0; i < srcCount; i++) {
             executeDispatch(xContext, xDesktop, xMCF, srcDoc, ".uno:SelectAll", props);
             executeDispatch(xContext, xDesktop, xMCF, srcDoc, ".uno:Copy", props);
 
             executeDispatch(xContext, xDesktop, xMCF, destDoc, ".uno:InsertPage", props);
             executeDispatch(xContext, xDesktop, xMCF, destDoc, ".uno:Paste", props);
             executeDispatch(xContext, xDesktop, xMCF, destDoc, ".uno:Paste", props);
 
             XDrawPage srcPage = getDrawPageByIndex(srcDoc, 0);  // Since we delete them as we copy, we always want the first page!
             XDrawPage destPage = getDrawPageByIndex(destDoc, i+1); // Because the new pages get inserted after the original first page
 
             copyBackground(destPage, srcPage);
             executeDispatch(xContext, xDesktop, xMCF, srcDoc, ".uno:DeletePage", props);
 
         }
         srcDoc.dispose();
 
         // Now remove the original first page
         destPagesSuppl =
                 (XDrawPagesSupplier) UnoRuntime.queryInterface(XDrawPagesSupplier.class, destDoc);
         XDrawPages destPages = destPagesSuppl.getDrawPages();
         destPages.remove(destDP);
 
         return srcCount;
     }
 
     // Based on http://www.oooforum.org/forum/viewtopic.phtml?t=48271
     private void executeDispatch(XComponentContext xContext,
                                  XDesktop xDesktop,
                                  XMultiComponentFactory xMCF,
                                  Object pobjDoc,
                                  String cmd,
                                  PropertyValue[] props)
     {
         try {
             XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, pobjDoc);
             XController xController = xModel.getCurrentController();
 
             XFrame xFrame = xController.getFrame();
             if (!xFrame.isActive())
                 xFrame.activate();
 
             XDispatchProvider impressDispatchProvider = (XDispatchProvider) UnoRuntime.queryInterface(XDispatchProvider.class, xFrame);
             Object oDispatchHelper = xMCF.createInstanceWithContext("com.sun.star.frame.DispatchHelper", xContext);
             XDispatchHelper dispatchHelper = (XDispatchHelper) UnoRuntime.queryInterface(XDispatchHelper.class, oDispatchHelper);
 
             printDispatchInfo(cmd, props);
             dispatchHelper.executeDispatch(impressDispatchProvider, cmd, "", 0, props);
         } catch (Exception e) {
             throw new RuntimeException(e);
         } catch (java.lang.Exception ex) {
             mylog.error("executeDispatch: Yikes!: " + ex.getMessage());
         }
     }
 
     private void printDispatchInfo(String cmd, PropertyValue[] props)
     {
         mylog.debug("Executing dispatch command '%s' with parameters:", cmd);
         if (props == null)
             return;
         for (int i = 0; i < props.length; i++) {
             if (props[i] != null)
                 mylog.error("   '%s': '%s'", props[i].Name, props[i].Value);
         }
     }
     
     private XDrawPage getDrawPageByIndex(XComponent xCompDoc, int nIndex)
     {
         XDrawPagesSupplier xDrawPagesSuppl =
                 (XDrawPagesSupplier) UnoRuntime.queryInterface(XDrawPagesSupplier.class, xCompDoc);
         if (xDrawPagesSuppl == null) {
             //mylog.debug("Failed to get xDrawPagesSuppl from xComp");
             return null;
         }
 
         XDrawPages xDrawPages = xDrawPagesSuppl.getDrawPages();
 
         return getDrawPage(xDrawPages, nIndex);
     }
 
     private XDrawPage getDrawPage(XDrawPages xDrawPages, int nIndex)
     {
         XDrawPage xDP = null;
         try {
             if ( nIndex < xDrawPages.getCount() )
                 xDP = (XDrawPage) UnoRuntime.queryInterface(
                         XDrawPage.class, xDrawPages.getByIndex(nIndex));
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             return xDP;
         }
     }
 
     private XShape getPageShape(XDrawPage xDrawPage, int nIndex)
     {
         XShape xShape = null;
         try {
             if ( nIndex < xDrawPage.getCount() )
                 xShape = (XShape) UnoRuntime.queryInterface(
                         XShape.class, xDrawPage.getByIndex(nIndex));
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             return xShape;
         }
     }
 
     // ASSumes there is a single group shape on the page,
     // therefore returns the first one found...
     private XShape getPageGroupShape(XDrawPage xDrawPage)
     {
         XShape xShape = null;
         int shapeCount = xDrawPage.getCount();
         try {
             for (int i = 0; i < shapeCount; i++) {
                 xShape = getPageShape(xDrawPage, i);
                 String currType = xShape.getShapeType();
                 if (currType.equalsIgnoreCase("com.sun.star.drawing.GroupShape")) {
                     return xShape;
                 }
             }
             return null;
         } catch (java.lang.Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 }
