 /*
  * DecompUtil.java
  *
  * Created on 2010.01.12
  *
  */
 
 package edu.umich.med.umms;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.sun.star.awt.Point;
 import com.sun.star.awt.Size;
 
 import com.sun.star.beans.Property;
 import com.sun.star.beans.PropertyValue;
 import com.sun.star.beans.XPropertySet;
 import com.sun.star.beans.UnknownPropertyException;
 
 import com.sun.star.container.XNameAccess;
 import com.sun.star.container.XNameContainer;
 
 import com.sun.star.document.XExporter;
 import com.sun.star.document.XFilter;
 import com.sun.star.document.XTypeDetection;
 import com.sun.star.document.XStorageBasedDocument;
 
 import com.sun.star.drawing.XDrawPage;
 import com.sun.star.drawing.XShape;
 //import com.sun.star.drawing.XTextShape; // Doesn't exist?
 //import com.sun.star.drawing.GraphicObjectShape;
 
 import com.sun.star.embed.XStorage;
 
 import com.sun.star.frame.XComponentLoader;
 import com.sun.star.frame.XDesktop;
 import com.sun.star.frame.XStorable;
 //import com.sun.star.gallery.GalleryItemType;
 //import com.sun.star.gallery.XGalleryThemeProvider;
 //import com.sun.star.gallery.XGalleryTheme;
 //import com.sun.star.gallery.XGalleryItem;
 
 import com.sun.star.lang.XMultiComponentFactory;
 import com.sun.star.lang.XMultiServiceFactory;
 import com.sun.star.lang.XComponent;
 import com.sun.star.lang.WrappedTargetException;
 
 import com.sun.star.io.XInputStream;
 import com.sun.star.io.XStream;
 
 import com.sun.star.lang.XServiceInfo;
 
 import com.sun.star.lib.uno.adapter.XOutputStreamToByteArrayAdapter;
 //import com.sun.star.table.BorderLine;
 
 import com.sun.star.ucb.XSimpleFileAccess2;
 
 import com.sun.star.uno.Exception;
 import com.sun.star.uno.UnoRuntime;
 import com.sun.star.uno.XComponentContext;
 import java.io.File;
 import java.util.UUID;
 
 
 /**
  *
  * @author kwc@umich.edu
  */
 public class DecompUtil {
 
     private com.spinn3r.log5j.Logger mylog = com.spinn3r.log5j.Logger.getLogger();
     private org.apache.log4j.Level myLogLevel = org.apache.log4j.Level.WARN;
 
     public DecompUtil()
     {
         mylog = com.spinn3r.log5j.Logger.getLogger();
     }
 
     public DecompUtil(DecompParameters dp)
     {
     }
 
     public void setLoggingLevel(org.apache.log4j.Level lvl)
     {
         myLogLevel = lvl;
         mylog.setLevel(myLogLevel);
     }
 
     public static XComponent openFileForProcessing(XDesktop xDesktop, String inputFileUrl) throws java.lang.Exception
     {
         com.spinn3r.log5j.Logger locallog = com.spinn3r.log5j.Logger.getLogger();
         locallog.debug("Opening file '%s'\n", inputFileUrl);
         // Set up to load the document
         PropertyValue propertyValues[] = new PropertyValue[3];
         propertyValues[0] = new PropertyValue();
         propertyValues[0].Name = "Hidden";
         propertyValues[0].Value = new Boolean(true);
 
 // XXX Restore to READ-ONLY?  Or add an option to the function?
 //        propertyValues[1] = new PropertyValue();
 //        propertyValues[1].Name = "ReadOnly";
 //        propertyValues[1].Value = new Boolean(true);
 
 //        propertyValues[2] = new PropertyValue();
 //        propertyValues[2].Name = "FilterName";
 //        propertyValues[2].Value = new String("pdf_Portable_Document_Format");
 
 
         XComponentLoader xCompLoader = (XComponentLoader)
                 UnoRuntime.queryInterface(XComponentLoader.class, xDesktop);
 
 
         return xCompLoader.loadComponentFromURL(inputFileUrl, "_blank", 0, propertyValues);
     }
 
     public static XPropertySet getObjectPropertySet(Object origObj)
     {
         XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, origObj);
         return xPropSet;
     }
 
     /* http://www.oooforum.org/forum/viewtopic.phtml?t=45734 */
     public static String getInternalURL(XComponent xDrawDoc,
                                         String srcUrl,
                                         String imgName) throws Exception
     {
         XMultiServiceFactory xFactory = (XMultiServiceFactory)
                 UnoRuntime.queryInterface(XMultiServiceFactory.class, xDrawDoc);
         Object xBitObj = xFactory.createInstance("com.sun.star.drawing.BitmapTable");
         XNameContainer bitMaps = (XNameContainer)
                 UnoRuntime.queryInterface(XNameContainer.class, xBitObj);
 
         try {
             bitMaps.insertByName(imgName, srcUrl);
         } catch (com.sun.star.container.ElementExistException e) {
             // Cool, it is already there!
         }
         return (String) bitMaps.getByName(imgName);
     }
 
     public static XShape createShape(XComponent xDrawDoc,
                                      com.sun.star.awt.Point aPos,
                                      com.sun.star.awt.Size aSize,
                                      String sShapeType ) throws java.lang.Exception
     {
         XShape xShape = null;
         XMultiServiceFactory xFactory = (XMultiServiceFactory)
                 UnoRuntime.queryInterface(XMultiServiceFactory.class, xDrawDoc);
         Object xObj = xFactory.createInstance(sShapeType);
         xShape = (XShape) UnoRuntime.queryInterface(XShape.class, xObj);
         xShape.setPosition(aPos);
         xShape.setSize(aSize);
         return xShape;
     }
 
     public /*static*/ void printShapeProperties(XShape shape) throws WrappedTargetException
     {
 
         // Get and print all the shape's properties
         XPropertySet xShapeProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, shape);
         Property[] props = xShapeProperties.getPropertySetInfo().getProperties();
         mylog.debug("----- Printing Shape Properties -----");
         for (int x = 0; x < props.length; x++) {
             try {
                 mylog.debug("    Property " + props[x].Name + " = " + xShapeProperties.getPropertyValue(props[x].Name));
             } catch (UnknownPropertyException ex) {
                 Logger.getLogger(OpenOfficeUNODecomposition.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         mylog.debug("");
 
     }
 
     public /*static*/ void printObjectProperties(Object obj) throws WrappedTargetException
     {
 
         // Get and print all the shape's properties
         XPropertySet xShapeProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, obj);
         Property[] props = xShapeProperties.getPropertySetInfo().getProperties();
         mylog.debug("----- Printing Object Properties -----");
         for (int x = 0; x < props.length; x++) {
             try {
                 mylog.debug("    Property " + props[x].Name + " = " + xShapeProperties.getPropertyValue(props[x].Name));
             } catch (UnknownPropertyException ex) {
                 Logger.getLogger(OpenOfficeUNODecomposition.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         mylog.debug("");
 
     }
 
     // Assumes that the original image shape is supplied
     public static Point calculateCitationImagePosition(XShape xOrigImage)
     {
         Point aPos = xOrigImage.getPosition();
         Size aSize = xOrigImage.getSize();
         Point citationPos = new Point();
 
         citationPos.X = aPos.X;
         citationPos.Y = aPos.Y + aSize.Height + 200;
         return citationPos;
     }
 
     // Assumes that the original image shape is supplied, and perhaps the citation image shape
     public static Point calculateCitationTextPosition(XShape xOrigImage, XShape xCiteImage)
     {
         Point citeTextPos = new Point();
 
         if (xCiteImage != null) {
             Point aPos = xCiteImage.getPosition();
             Size aSize = xCiteImage.getSize();
             citeTextPos.X = aPos.X + aSize.Width + 200;
             citeTextPos.Y = aPos.Y;
         } else {
             Point aPos = xOrigImage.getPosition();
             Size aSize = xOrigImage.getSize();
             citeTextPos.X = aPos.X;
             citeTextPos.Y = aPos.Y + aSize.Height + 200;
         }
         return citeTextPos;
     }
 
     // Assumes that the original image shape is supplied
     public static Size calculateCitationImageSize(XShape xOrigImage)
     {
         Point aPos = xOrigImage.getPosition();
         Size aSize = xOrigImage.getSize();
         Size citationSize = new Size();
 
         citationSize.Width = 88 * 20; // Image is 88x31 pixels -- show it 20 times that size
         citationSize.Height = 31 * 20;
         return citationSize;
     }
 
     // Assumes that the original image shape is supplied, and perhaps the citation image shape
     public static Size calculateCitationTextSize(XShape xOrigImage, XShape xCiteImage)
     {
         Size imageSize = xOrigImage.getSize();
         Size citeTextSize = new Size();
 
         if (xCiteImage != null) {
             Size citeSize = xCiteImage.getSize();
             citeTextSize.Width = imageSize.Width - citeSize.Width - 200;
             citeTextSize.Height = citeSize.Height;
         } else {
             citeTextSize.Width = imageSize.Width;
             citeTextSize.Height = 31 * 20;
         }
         return citeTextSize;
     }
 
     public /*static*/ void printSupportedServices(XComponent xCompDoc)
     {
                 XServiceInfo xServiceInfo = (XServiceInfo) UnoRuntime.queryInterface(
                     XServiceInfo.class, xCompDoc);
             if (xServiceInfo != null) {
                 String[] svcNames = xServiceInfo.getSupportedServiceNames();
                 if (svcNames.length > 0)
                     mylog.debug("This document supports the following services:");
                 for (int i = 0; i < svcNames.length; i++) {
                     mylog.debug("\t%s", svcNames[i]);
                 }
             }
     }
 
     public /*static*/ void exportContextImage(XComponentContext xContext,
                                            XMultiComponentFactory xMCF,
                                            XDrawPage page,
                                            String outputDir,
                                            int p)
     {
 
         XPropertySet shapeProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, page);
         String fname = String.format("%s/%s%03d.%s", outputDir, "Slide", p, "png");
 
         PropertyValue outProps[] = new PropertyValue[2];
         outProps[0] = new PropertyValue();
         outProps[0].Name = "MediaType";
         outProps[0].Value = "image/png";
 
         outProps[1] = new PropertyValue();
         outProps[1].Name = "URL";
         outProps[1].Value = "file://" + fname;
 
         mylog.debug("Exporting page %d to file '%s'", p, fname);
 
         try {
 
             Object GraphicExportFilter = xMCF.createInstanceWithContext("com.sun.star.drawing.GraphicExportFilter", xContext);
             XExporter xExporter = (XExporter)UnoRuntime.queryInterface(XExporter.class, GraphicExportFilter);
 
             XComponent xCompPage = (XComponent)UnoRuntime.queryInterface(XComponent.class, page);
             xExporter.setSourceDocument(xCompPage);
 
             XFilter xFilter = (XFilter) UnoRuntime.queryInterface(XFilter.class, GraphicExportFilter);
             xFilter.filter(outProps);
 
         } catch (Exception e) {
             mylog.error("Caught Exception exporting image:" + e.getMessage());
         }
     }
 
 
     public /*static*/ void exportImage(XComponentContext xContext,
                                     XMultiComponentFactory xMCF,
                                     XShape shape,
                                     String outputDir,
                                     int p,
                                     int s)
     {
 
         XPropertySet shapeProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, shape);
         String fname = String.format("%s/%s-%05d-%03d.%s", outputDir, "image", p, s, "png");
 
         PropertyValue outProps[] = new PropertyValue[2];
         outProps[0] = new PropertyValue();
 
         outProps[0].Name = "MediaType";
         outProps[0].Value = "image/png";
 //        outProps[0].Name = "FilterName";
 //        outProps[0].Value = "WMF - MS Windows Metafile";
 
         outProps[1] = new PropertyValue();
         outProps[1].Name = "URL";
         outProps[1].Value = fileNameToOOoURL(fname);
 
         mylog.debug("exportImage: Exporting shape %d from page %d to file '%s'", s, p, fname);
 
         try {
 
             Object GraphicExportFilter = xMCF.createInstanceWithContext("com.sun.star.drawing.GraphicExportFilter", xContext);
             XExporter xExporter = (XExporter)UnoRuntime.queryInterface(XExporter.class, GraphicExportFilter);
 
             XComponent xCompShape = (XComponent)UnoRuntime.queryInterface(XComponent.class, shape);
             xExporter.setSourceDocument(xCompShape);
 
             XFilter xFilter = (XFilter) UnoRuntime.queryInterface(XFilter.class, GraphicExportFilter);
             xFilter.filter(outProps);
 
         } catch (Exception e) {
             mylog.error("Caught Exception exporting image: " + e.getMessage());
             e.printStackTrace();
         }
     }
 
     public /*static*/ void extractImageByURL(XComponentContext xContext,
                                          XMultiComponentFactory xMCF,
                                          XComponent xCompDoc,
                                          String pictureURL,
                                          String outName)
     {
         try {
             Object oSFAcc = xMCF.createInstanceWithContext(
                     "com.sun.star.ucb.SimpleFileAccess", xContext);
             XSimpleFileAccess2 xFileWriter = (XSimpleFileAccess2)
                     UnoRuntime.queryInterface(XSimpleFileAccess2.class, oSFAcc);
             XStorageBasedDocument xStorageBasedDocument = (XStorageBasedDocument)
                     UnoRuntime.queryInterface(XStorageBasedDocument.class, xCompDoc);
 
             XStorage xDocStorage = xStorageBasedDocument.getDocumentStorage();
             XStorage xDocPictures = (XStorage) UnoRuntime.queryInterface(
                     XStorage.class, xDocStorage.getByName("Pictures"));
 
             XNameAccess xDocStorageNameAccess = (XNameAccess)
                     UnoRuntime.queryInterface(XNameAccess.class, xDocStorage);
 
             if (!xDocStorageNameAccess.hasByName("Pictures")) {
                 mylog.warn("Found no \"Pictures\" in the document!!!");
                 return;
             }
 
             Object oPicturesStorage = xDocStorageNameAccess.getByName("Pictures");
             XNameAccess xPicturesNameAccess = (XNameAccess)
                     UnoRuntime.queryInterface(XNameAccess.class, oPicturesStorage);
 
             String[] aNames = xPicturesNameAccess.getElementNames();
             mylog.debug("There were a total of %d pictures found via DocStorageAccess", aNames.length);
             for (int i = 0; i < aNames.length; i++) {
                 //mylog.debug("Picture %d has name '%s'", i+1, aNames[i]);
                 if (aNames[i].contains(pictureURL)) {
                     Object oElement = xPicturesNameAccess.getByName(aNames[i]);
                     XStream xStream = (XStream) UnoRuntime.queryInterface(XStream.class, oElement);
                     Object oInput = xStream.getInputStream();
                     XInputStream xInputStream = (XInputStream)
                             UnoRuntime.queryInterface(XInputStream.class, oInput);
 
                     mylog.debug("exportImageByURL: Exporting image '%s' to '%s.%s'", pictureURL, outName, getExtension(aNames[i]));
                     xFileWriter.writeFile(outName + "." + getExtension(aNames[i]), xInputStream);
                     break;
                 }
 
             }
         } catch (Exception ex) {
             Logger.getLogger(OpenOfficeUNODecomposition.class.getName()).log(Level.SEVERE, null, ex);
             return;
         }
 
     }
 
     public /*static*/ void printDocumentType(XComponentContext xContext,
                                           XMultiComponentFactory xMCF,
                                           String sURL)
     {
         Object oInterface = null;
 
         try {
             oInterface = xMCF.createInstanceWithContext(
                     "com.sun.star.document.TypeDetection", xContext);
         } catch (java.lang.Exception ex) {
             ex.printStackTrace();
         }
 
         if(oInterface == null) {
               mylog.error("__FUNCTION__: unable to create TypeDetection service");
         }
 
         XTypeDetection m_xDetection = (XTypeDetection)
                 UnoRuntime.queryInterface(XTypeDetection.class, oInterface);
 
         // queryTypeByURL does a "flat" detection of filetype (looking at the suffix???)
         mylog.debug("queryTypeByURL says '"
                 + sURL + "' is of type: '" +
                 m_xDetection.queryTypeByURL(sURL) +"'");
 
         // queryTypeByDescriptor does an optional "deep" detection of filetype
         PropertyValue testProps[][] = new PropertyValue[1][1];
         testProps[0][0] = new PropertyValue();
         testProps[0][0].Name = "URL";
         testProps[0][0].Value = sURL;
 
         mylog.debug("queryTypeByDescriptor says '" +
                 sURL + "' is of type: '" +
                 m_xDetection.queryTypeByDescriptor(testProps, false) +"'");
 
     }
 
     public /*static*/ String getDocumentType(XComponentContext xContext,
                                           XMultiComponentFactory xMCF,
                                           String sURL)
     {
         Object oInterface = null;
 
         try {
             oInterface = xMCF.createInstanceWithContext(
                     "com.sun.star.document.TypeDetection", xContext);
         } catch (java.lang.Exception ex) {
             ex.printStackTrace();
         }
 
         if(oInterface == null) {
               mylog.error("__FUNCTION__: unable to create TypeDetection service");
               return null;
         }
 
         XTypeDetection m_xDetection = (XTypeDetection)
                 UnoRuntime.queryInterface(XTypeDetection.class, oInterface);
 
         // do the optional "deep" detection of filetype
         PropertyValue testProps[][] = new PropertyValue[1][1];
         testProps[0][0] = new PropertyValue();
         testProps[0][0].Name = "URL";
         testProps[0][0].Value = sURL;
 
         return m_xDetection.queryTypeByDescriptor(testProps, true);
     }
 
     public static String fileNameToOOoURL(final String fName) {
         StringBuilder sLoadUrl;
 
         // XXX Consider the case where we actually get a URL in
         if (fName.contains("://"))
             sLoadUrl = new StringBuilder();
         else
             sLoadUrl = new StringBuilder("file://");
         sLoadUrl.append(fName.replace('\\', '/'));
         return sLoadUrl.toString();
     }
 
     public /*static*/ void storeDocument(XComponentContext xContext,
                                       XMultiComponentFactory xMCF,
                                       XComponent xCompDoc,
                                       String newFNameUrl,
                                       String filterName)
     {
         mylog.debug("Storing file using URL '%s'", newFNameUrl);
 
         XStorable xStorable = (XStorable) UnoRuntime.queryInterface(XStorable.class, xCompDoc);
         PropertyValue[] propertyValue = new PropertyValue[3];
         propertyValue[2] = new com.sun.star.beans.PropertyValue();
         propertyValue[2].Name = "Overwrite";
         propertyValue[2].Value = Boolean.valueOf(true);
         propertyValue[0] = new com.sun.star.beans.PropertyValue();
         propertyValue[0].Name = "FilterName";
         propertyValue[0].Value = filterName;
 
         XOutputStreamToByteArrayAdapter outputStream = new XOutputStreamToByteArrayAdapter();
         propertyValue[1] = new com.sun.star.beans.PropertyValue();
         propertyValue[1].Name = "OutputStream";
         propertyValue[1].Value = outputStream;
         try {
             xStorable.storeToURL(newFNameUrl, propertyValue);
         } catch (com.sun.star.io.IOException ex) {
             mylog.error("Storing document: " + ex.getMessage());
             ex.printStackTrace();
         }
 
     }
 
     public static boolean isaSupportedFormat(OOoFormat format)
     {
         return (java.util.Arrays.asList(OpenOfficeUNODecomposition.SUPPORTED_FORMATS).contains(format));
     }
 
     public static OOoFormat getNativeFormat(OOoFormat origFormat)
     {
         OOoFormat nativeFormat = origFormat;
 
         if (origFormat.getHandlerType() == 0) {
             nativeFormat = OOoFormat.OpenDocument_Text;
         } else if (origFormat.getHandlerType() == 2) {
             nativeFormat = OOoFormat.OpenDocument_Presentation;
         }
         return nativeFormat;
     }
 
     public /*static*/ String possiblyUseTemporaryDocument(XComponentContext xContext,
                                                XMultiComponentFactory xMCF,
                                                XComponent xCompOrigDoc,
                                                String origName,
                                                OOoFormat origFormat)
     {
         String newName;
         OOoFormat nativeFormat;
 
         // Get current file type and determine if we need to save it in OO format
         // If already in Native OO format, no need to continue
         if ((nativeFormat = getNativeFormat(origFormat)) == null) {
             return null;
         }
         if (nativeFormat == origFormat) {
             return null;
         }
 
         newName = new String("/tmp/" + UUID.randomUUID().toString() + "." + nativeFormat.getFileExtension());
 
         // Save in OO format and return the name of the temporary file
         storeDocument(xContext, xMCF, xCompOrigDoc, fileNameToOOoURL(newName), nativeFormat.getFilterName());
 
         return newName;
     }
 
     public static boolean removeTemporaryDocument(String name)
     {
         File f = new File(name);
         if (!f.exists())
             return false;
 
         return f.delete();
     }
 
     public static String getExtension(String fullName)
     {
         int dotPos = fullName.lastIndexOf(".");
         String strExtension = fullName.substring(dotPos + 1);
         return strExtension;
         //String strFilename = fullName.substring(0, dotPos);
     }
 
     public static String constructBaseImageName(String dir, int page, int num)
     {
         String outName = new String();
 
         outName = String.format("%s/image-%05d-%03d", dir, page, num);
         return outName;
     }
 
         private static byte[] readStream(InputStream inStream) throws IOException
     {
         byte[] bytes = new byte[1024];
         int numRead;
         ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);
 
         while ((numRead = inStream.read(bytes)) > 0) {
             outStream.write(bytes, 0, numRead);
         }
 
         byte[] outBytes = outStream.toByteArray();
 
         outStream.close();
 
         return outBytes;
     }
 
     public static byte[] getImageByteStream(String imageURL)
     {
         byte[] returnBytes = null;
         InputStream inStream = null;
         URL theURL = null;
 
         try {
             theURL = new URL(imageURL);
             inStream = theURL.openStream();
             returnBytes = readStream(inStream);
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 if (inStream != null) {
                     inStream.close();
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return returnBytes;
     }
 
     public static void sleepFor(int seconds)
     {
         long sleeptime = seconds * 1000;
         try {
             System.err.printf("Sleeping for %d seconds... ", seconds);
             Thread.sleep(sleeptime);
             System.err.printf("awake\n");
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 }
