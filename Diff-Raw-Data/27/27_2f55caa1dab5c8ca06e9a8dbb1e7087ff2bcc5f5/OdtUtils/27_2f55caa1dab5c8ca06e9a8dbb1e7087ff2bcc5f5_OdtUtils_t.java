 /**
  *  odt2daisy - OpenDocument to DAISY XML/Audio
  *
  *  (c) Copyright 2008 - 2012 by Vincent Spiewak, All Rights Reserved.
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Lesser Public License as published by
  *  the Free Software Foundation; either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License along
  *  with this program; if not, write to the Free Software Foundation, Inc.,
  *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 package com.versusoft.packages.jodl;
 
 import com.sun.org.apache.xpath.internal.XPathAPI;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.LinkedHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import org.w3c.dom.Element;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * OdtUtils.java: convert zipped odt to flat odt xml. It provide also some nice fonctions
  *
  * @author Vincent Spiewak
  */
 public class OdtUtils {
 
     public static final String PICTURE_FOLDER = "Pictures/";
     private static final Logger logger = Logger.getLogger("com.versusoft.packages.jodl.odtutils");
     //
     private String odtFile;
     private DocumentBuilderFactory docFactory;
     private DocumentBuilder docBuilder;
     private Document doc;
     private Element root;
     private ZipFile zf;
 
     /**
      * Merges the content of meta.xml, styles.xml, content.xml and settings.xml
      * into a single XML file where the document element is called
      * "office:document" and adds namespace attributes to this new XML document.
      *
      * @param odtFile The ODF file.
      */
     public void open(String odtFile) {
         try {
 
             logger.fine("entering");
 
             zf = null;
             this.odtFile = odtFile;
 
             docFactory = DocumentBuilderFactory.newInstance();
             docFactory.setValidating(false);
             docBuilder = docFactory.newDocumentBuilder();
             docBuilder.setEntityResolver(new EntityResolver() {
 
             public InputSource resolveEntity(java.lang.String publicId, java.lang.String systemId) throws SAXException, java.io.IOException {
                     return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
                 }
             });         
             
             zf = new ZipFile(odtFile);
 
             ZipEntry metaEntry = zf.getEntry("meta.xml");
             ZipEntry stylesEntry = zf.getEntry("styles.xml");
             ZipEntry contentEntry = zf.getEntry("content.xml");
             ZipEntry settingsEntry = zf.getEntry("settings.xml");
 
             doc = docBuilder.newDocument();
 
             Element racine = doc.createElement("office:document");
 
             Document metaDoc = docBuilder.parse(zf.getInputStream(metaEntry));
             Document stylesDoc = docBuilder.parse(zf.getInputStream(stylesEntry));
             Document contentDoc = docBuilder.parse(zf.getInputStream(contentEntry));
             Document settingsDoc = docBuilder.parse(zf.getInputStream(settingsEntry));
 
             replaceObjectContent(docBuilder, contentDoc, zf);
 
             racine.setAttribute("xmlns:meta", "urn:oasis:names:tc:opendocument:xmlns:meta:1.0");
             racine.setAttribute("xmlns:xsl", "http://www.w3.org/1999/XSL/Transform");
             racine.setAttribute("xmlns:office", "urn:oasis:names:tc:opendocument:xmlns:office:1.0");
             racine.setAttribute("xmlns:style", "urn:oasis:names:tc:opendocument:xmlns:style:1.0");
             racine.setAttribute("xmlns:text", "urn:oasis:names:tc:opendocument:xmlns:text:1.0");
             racine.setAttribute("xmlns:table", "urn:oasis:names:tc:opendocument:xmlns:table:1.0");
             racine.setAttribute("xmlns:draw", "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0");
             racine.setAttribute("xmlns:fo", "urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0");
             racine.setAttribute("xmlns:meta", "urn:oasis:names:tc:opendocument:xmlns:meta:1.0");
             racine.setAttribute("xmlns:number", "urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0");
             racine.setAttribute("xmlns:svg", "urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0");
             racine.setAttribute("xmlns:chart", "urn:oasis:names:tc:opendocument:xmlns:chart:1.0");
             racine.setAttribute("xmlns:dr3d", "urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0");
             racine.setAttribute("xmlns:form", "urn:oasis:names:tc:opendocument:xmlns:form:1.0");
             racine.setAttribute("xmlns:script", "urn:oasis:names:tc:opendocument:xmlns:script:1.0");
             racine.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
             racine.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
             racine.setAttribute("xmlns:math", "http://www.w3.org/1998/Math/MathML");
             racine.setAttribute("xmlns:dom", "http://www.w3.org/2001/xml-events");
             racine.setAttribute("xmlns:xforms", "http://www.w3.org/2002/xforms");
             racine.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
             racine.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
             racine.setAttribute("xmlns:config", "urn:oasis:names:tc:opendocument:xmlns:config:1.0");
             racine.setAttribute("xmlns:ooo", "http://openoffice.org/2004/office");
             racine.setAttribute("xmlns:officeooo", "http://openoffice.org/2009/office");
             racine.setAttribute("xmlns:drawooo", "http://openoffice.org/2009/draw");
             racine.setAttribute("xmlns:manifest", "urn:oasis:names:tc:opendocument:xmlns:manifest:1.0");
 
             NodeList nodelist = metaDoc.getDocumentElement().getChildNodes();
             for (int i = 0; i < nodelist.getLength(); i++) {
                 racine.appendChild(doc.importNode(nodelist.item(i), true));
             }
 
             nodelist = settingsDoc.getDocumentElement().getChildNodes();
             for (int i = 0; i < nodelist.getLength(); i++) {
                 racine.appendChild(doc.importNode(nodelist.item(i), true));
             }
 
 
             nodelist = stylesDoc.getDocumentElement().getChildNodes();
             for (int i = 0; i < nodelist.getLength(); i++) {
                 racine.appendChild(doc.importNode(nodelist.item(i), true));
             }
 
             nodelist = contentDoc.getDocumentElement().getChildNodes();
             for (int i = 0; i < nodelist.getLength(); i++) {
                 racine.appendChild(doc.importNode(nodelist.item(i), true));
             }
 
 
             doc.appendChild(racine);
             root = doc.getDocumentElement();
 
         } catch (SAXException ex) {
             logger.log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             logger.log(Level.SEVERE, null, ex);
         } catch (ParserConfigurationException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
 
     }
 
     /**
      * Saves the merged XML content of the ODF file as a file
      * with the chosen file name.
      * 
      * @param fileOut The file to which the DOM should be saved. The String must conform to the URI syntax.
      * @return true if the file could be saved; false if the file could not be saved.
      */
     public boolean saveXML(String fileOut) {
 
         return saveDOM(doc, fileOut);
 
     }
 
     /**
      * Performs a few basic corrections on the XML content.
      * 
      * @see OdtUtils#removeEmptyHeadings(org.w3c.dom.Node)
      * @see OdtUtils#normalizeTextS(org.w3c.dom.Document, org.w3c.dom.Node)
      * @see OdtUtils#removeEmptyParagraphs(org.w3c.dom.Node)
      * @see OdtUtils#insertEmptyParaForHeadings(org.w3c.dom.Document, org.w3c.dom.Node) 
      *
      * @param xmlFile The path to the XML file.
      * @throws ParserConfigurationException If a DocumentBuilder cannot be created which satisfies the configuration requested.
      * @throws SAXException If an exceptional condition occurred while parsing the XML file.
      * @throws IOException If an exceptional condition occurred while parsing the XML file.
      * @throws TransformerConfigurationException If a serious transformer configuration error occurred.
      * @throws TransformerException If an exceptional condition occurred during an XPath query.
      */
     public static void correctionProcessing(String xmlFile)
             throws ParserConfigurationException, SAXException, 
             IOException, TransformerConfigurationException,
             TransformerException {
 
         DocumentBuilderFactory docFactory;
         DocumentBuilder docBuilder;
 
         Document contentDoc;
 
         // Node pointer
         Node currentNode = null;
 
         docFactory =
                 DocumentBuilderFactory.newInstance();
         docFactory.setValidating(false);
 
         docBuilder =
                 docFactory.newDocumentBuilder();
         docBuilder.setEntityResolver(new EntityResolver() {
 
             public InputSource resolveEntity(
                     java.lang.String publicId, java.lang.String systemId)
                     throws SAXException, java.io.IOException {
 
                 return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
 
             }
         });
 
         contentDoc = docBuilder.parse(xmlFile);
         Element root = contentDoc.getDocumentElement();
 
         // Select first element after text:sequence-decls
         currentNode =
                 XPathAPI.selectSingleNode(root, "/document/body/text/sequence-decls/following-sibling::*[1]");
 
         if (currentNode == null) {
             System.out.println("XPath select failed");
         }
 
         removeEmptyHeadings(root);
        normalizePictureIds(root);
         normalizeTextS(contentDoc,root);
         // @todo make removing empty paragraphs an option in odt2daisy dialog.
         removeEmptyParagraphs(root);
         insertEmptyParaForHeadings(contentDoc,root);
 
         saveDOM(contentDoc, xmlFile);
 
     }
 
     // Christophe's best guess for this method's JavaDoc:
     /**
      * Facilitate page numbering support.
      *
      * @param xmlFile The path to the XML file.
      * @throws ParserConfigurationException If a DocumentBuilder cannot be created which satisfies the configuration requested.
      * @throws SAXException If an exceptional condition occurred while parsing the XML file.
      * @throws IOException If an exceptional condition occurred while parsing the XML file.
      * @throws TransformerConfigurationException If a serious transformer configuration error occurred
      * @throws TransformerException If an exceptional condition occurred during 
      * an XPath query or while inserting pagination.
      */
     public static void paginationProcessing(String xmlFile)
             throws ParserConfigurationException, SAXException, 
             IOException, TransformerConfigurationException,
             TransformerException {
 
         DocumentBuilderFactory docFactory;
         DocumentBuilder docBuilder;
 
         Document contentDoc;
 
         // Node pointer
         Node currentNode = null;
 
         docFactory =
                 DocumentBuilderFactory.newInstance();
         docFactory.setValidating(false);
 
         docBuilder =
                 docFactory.newDocumentBuilder();
         docBuilder.setEntityResolver(new EntityResolver() {
 
             public InputSource resolveEntity(
                     java.lang.String publicId, java.lang.String systemId)
                     throws SAXException, java.io.IOException {
 
                 return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
 
             }
         });
 
         contentDoc = docBuilder.parse(xmlFile);
         Element root = contentDoc.getDocumentElement();
 
         // Select first element after text:sequence-decls
         currentNode =
                 XPathAPI.selectSingleNode(root, "/document/body/text/sequence-decls/following-sibling::*[1]");
 
         if (currentNode == null) {
             System.out.println("XPath select failed");
         }
 
         insertPagination(root, currentNode, 0, false, "", "Standard", true, true);
         //correctionProcessing(root);
         saveDOM(contentDoc, xmlFile);
 
     }
 
     /**
      * Return an ArrayList of image(s) path(s) included in the ODF file,
      * i.e. a ArrayList of Strings like 'Pictures/100000000000034300000273CAF76237.png'.
      *
      * @param odtFile The path to the ODF file.
      * @return ArrayList of image(s) path(s)
      * @throws java.io.IOException If an exceptional condition occurred while creating a ZipFile based on the path to the ODF file.
      */
     public static ArrayList<String> getPictures(String odtFile) throws IOException {
         ArrayList<String> ret = new ArrayList<String>();
         ZipFile zf = null;
         Enumeration<? extends ZipEntry> entries = null;
 
         zf =
                 new ZipFile(odtFile);
         entries =
                 zf.entries();
 
         while (entries.hasMoreElements()) {
 
             ZipEntry entry = (ZipEntry) entries.nextElement();
 
             if (entry.getName().startsWith(PICTURE_FOLDER)) {
                 if (!entry.isDirectory()) {
                     ret.add(entry.getName());
                 }
 
             }
 
         }
 
         return ret;
 
     }
 
     /**
      * Extract pictures included inside an ODT file<br />
      * outDir can be: images, images/, pics/, book/pics/, ...
      *
      * @param odtFile
      * @param outDir
      * @throws java.io.IOException
      */
     public static void extractPictures(String odtFile, String outDir) throws IOException {
 
         ZipFile zip = new ZipFile(odtFile);
         File dir = new File(outDir);
 
         ArrayList<String> pics = getPictures(odtFile);
 
         if (pics.size() < 1) {
             return;
         }
 
         if (dir.isFile()) {
             throw new IOException("Wrong argument: outDir is a file");
         }
 
         if (!dir.exists()) {
             dir.mkdirs();
         }
 
         for (int i = 0; i <
                 pics.size(); i++) {
             copyInputStream(
                     zip.getInputStream(zip.getEntry(pics.get(i))),
                     new FileOutputStream(outDir + pics.get(i).substring(PICTURE_FOLDER.length())));
 
         }
 
     }
 
     /**
      * Extract and normalize picture names, i.e.
      * converts <code>xlink:href</code> values like
      * 'Pictures/100000000000034300000273CAF76237.png'
      * into values like 'images/0.png'.
      *
      * @param xmlFile The path to the XML file (merged XML files inside the ODF file).
      * @param odtFile The path to the ODF file.
      * @param parentDir The parent directory (of the new directory for the images (??)).
      * @param imgBaseDir The new directory for the images.
      * @return LinkedHashMap where keys are old file names and values are new file names.
      * @throws org.xml.sax.SAXException If an input source for the XML content cannot be created.
      * @throws java.io.IOException If the String representing the image directory is actually a file instead of a directory, or if an input source for the XML content cannot be created.
      * @throws javax.xml.parsers.ParserConfigurationException If a DocumentBuilder cannot be created which satisfies the configuration requested, i.e. a validating parser cannot be created.
      * @throws javax.xml.transform.TransformerConfigurationException If a a serious configuration error occured.
      * @throws javax.xml.transform.TransformerException If an exceptional condition occured during the transformation process.
      */
     public static LinkedHashMap<String,String> extractAndNormalizeEmbedPictures(String xmlFile, String odtFile, String parentDir, String imgBaseDir) throws SAXException, IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
 
         logger.fine("entering");
 
         ZipFile zip;
         File imgDir;
         ArrayList<String> pics;
         DocumentBuilderFactory docFactory;
         DocumentBuilder docBuilder;
         Document contentDoc;
         LinkedHashMap<String,String> oldAndNewImgNames = new LinkedHashMap<String,String>();
 
         pics = getPictures(odtFile);
         zip = new ZipFile(odtFile);
 
         // @todo clean up / irrelevant after making method return oldAndNewImgNames:
         if (pics.size() < 1) {
             return oldAndNewImgNames;
         }
 
         imgDir = new File(parentDir + imgBaseDir);
 
         logger.fine("imgBaseDir: " + imgBaseDir + "\n");
         logger.fine("parentDir: " + parentDir + "\n");
 
         if (imgDir.isFile()) {
             throw new IOException("Wrong argument: parentDir is a file");
         }
 
         if (!imgDir.exists()) {
             imgDir.mkdirs();
         }
 
         docFactory = DocumentBuilderFactory.newInstance();
         docFactory.setValidating(false);
 
         docBuilder =
                 docFactory.newDocumentBuilder();
         docBuilder.setEntityResolver(new EntityResolver() {
 
             public InputSource resolveEntity(
                     java.lang.String publicId, java.lang.String systemId)
                     throws SAXException, java.io.IOException {
 
                 return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
 
             }
         });
 
 
         contentDoc = docBuilder.parse(xmlFile);
 
         Element root = contentDoc.getDocumentElement();
         NodeList nodelist = root.getElementsByTagName("draw:image");
 
         // for every draw:image element in the merged XML:
         for (int i = 0; i < nodelist.getLength(); i++) {
 
             Node objectNode = nodelist.item(i);
             Node hrefNode = objectNode.getAttributes().getNamedItem("xlink:href");
 
             String imagePath = hrefNode.getTextContent();
             logger.fine("Image path: " + imagePath);
 
             // if the xlink:href value can be found in the list of images from the ODF file:
             if (pics.contains(imagePath)) {
 
                 int id = pics.indexOf(imagePath);
                 // create file extension based on original extension turned to lower case:
                 String ext = imagePath.substring(imagePath.lastIndexOf(".")).toLowerCase();
 
                 //String newImageName = id + ext;
                 //String newImagePath = parentDir + imgBaseDir + newImageName;
 
                 //if (ext.endsWith("gif") || ext.endsWith("bmp") || ext.endsWith("wbmp")) {
                 //    hrefNode.setTextContent(imgBaseDir + id + ".png");
                 //    logger.fine("extract image\n");
                 //    copyInputStream(zip.getInputStream(zip.getEntry(imagePath)), new FileOutputStream(parentDir + imgBaseDir + id + ext));
                 //    logger.fine("convert to png\n");
                 //    toPNG(parentDir + imgBaseDir + id + ext, parentDir + imgBaseDir + id + ".png");
                 //    logger.fine("delete old image\n");
                 //    new File(parentDir + imgBaseDir + id + ext).delete();
                 //} else {
                 // Set xlink:href value to new image directory + image index + image extension (lower case):
                 String newXlinkHref = imgBaseDir + id + ext;
                 hrefNode.setTextContent(newXlinkHref);
                 logger.fine("extracted image: " + newXlinkHref + "\n");
                 // Store mapping between new and old image names:
                 oldAndNewImgNames.put(imagePath, newXlinkHref);
                 copyInputStream(zip.getInputStream(zip.getEntry(imagePath)), new FileOutputStream(parentDir + imgBaseDir + id + ext));
                 //}
 
                 //@todo Remove logger output after testing
                 logger.fine("Image mapping = " + oldAndNewImgNames.toString());
                 logger.fine("done\n");
             }
         }
 
         saveDOM(contentDoc, xmlFile);
         logger.fine("done");
         return oldAndNewImgNames;
     }
 
     // @todo remove unused method?
     /**
      * Replace embed pictures base dir.
      *
      * @param xmlFile 
      * @param imgBaseDir
      * @throws javax.xml.parsers.ParserConfigurationException If a DocumentBuilder for the XML file cannot be created which satisfies the configuration requested.
      * @throws org.xml.sax.SAXException If the <code>EntityResolver</code> to be used to resolve entities present in the XML document cannot be created, or if an error occurs while parsing the XML file.
      * @throws java.io.IOException If the <code>EntityResolver</code> to be used to resolve entities present in the XML document cannot be created, or if an error occurs while parsing the XML file.
      * @throws javax.xml.transform.TransformerConfigurationException
      * @throws javax.xml.transform.TransformerException
      */
     public static void replaceEmbedPicturesBaseDir(String xmlFile, String imgBaseDir) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException {
         DocumentBuilderFactory docFactory;
         DocumentBuilder docBuilder;
 
         Document contentDoc;
 
         docFactory =
                 DocumentBuilderFactory.newInstance();
         docFactory.setValidating(false);
 
         docBuilder =
                 docFactory.newDocumentBuilder();
         docBuilder.setEntityResolver(new EntityResolver() {
 
             public InputSource resolveEntity(
                     java.lang.String publicId, java.lang.String systemId)
                     throws SAXException, java.io.IOException {
 
                 return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
 
             }
         });
 
 
         contentDoc =
                 docBuilder.parse(xmlFile);
         replaceEmbedPicturesBaseDir(contentDoc, imgBaseDir);
         saveDOM(contentDoc, xmlFile);
     }
 
     /**
      * Replace embed pictures base dir.
      *
      * @param contentDoc A DOM Document object.
      * @param imgBaseDir
      * @throws IOException
      * @throws SAXException
      */
     private static void replaceEmbedPicturesBaseDir(Document contentDoc, String imgBaseDir) throws IOException, SAXException {
 
         Element root = contentDoc.getDocumentElement();
         NodeList nodelist = root.getElementsByTagName("draw:image");
 
         for (int i = 0; i <
                 nodelist.getLength(); i++) {
 
             Node objectNode = nodelist.item(i);
             Node hrefNode = objectNode.getAttributes().getNamedItem("xlink:href");
 
             String imagePath = hrefNode.getTextContent(); // throws org.w3c.dom.DOMException ?
             logger.fine("image path=" + imagePath);
 
             if (imagePath.startsWith(PICTURE_FOLDER)) {
 
                 String newImagePath = imgBaseDir + imagePath.substring(PICTURE_FOLDER.length());
                 hrefNode.setTextContent(newImagePath);
 
             }
 
         }
 
     }
    
    /**
     * Normalize Picture Ids by replacing spaces with underscores.
     * 
     * @param root The document element (or "root element").
     */
    private static void normalizePictureIds(Node root){
                // for each text:h
        // remove empty headings
        NodeList picNodes = ((Element) root).getElementsByTagName("draw:frame");
        for (int i = 0; i < picNodes.getLength(); i++) {
            Node node = picNodes.item(i);
            if(node.hasAttributes()) {
                Node picIdNode = node.getAttributes().getNamedItem("draw:name");
                if(picIdNode != null) {
                    String picId = picIdNode.getNodeValue();
                    picId = picId.trim().replace(" ", "_");
                    logger.info("Normalized picture id from '"+picIdNode.getNodeValue()+"' to '"+picId+"'");
                    picIdNode.setTextContent(picId);
                }
            }
        }
    }
    
     /**
      * Remove empty <code>text:h</code> elements.
      * 
      * @param root The document element (or "root element").
      */
     private static void removeEmptyHeadings(Node root){
 
         // for each text:h
         // remove empty headings
         NodeList hNodes = ((Element) root).getElementsByTagName("text:h");
         for (int i = 0; i < hNodes.getLength(); i++) {
 
             Node node = hNodes.item(i);
 
             if (node.getChildNodes().getLength() > 0) {
 
                 boolean empty = true;
 
                 for (int j = 0; j < node.getChildNodes().getLength(); j++) {
                     if (!node.getChildNodes().item(j).getTextContent().trim().equals("")) {
                         empty = false;
                     }
                 }
 
                 if (empty) {
                     node.getParentNode().removeChild(node);
                     i--;
                 }
 
             } else {
                 if (node.getTextContent().trim().equals("")) {
                     node.getParentNode().removeChild(node);
                     i--;
                 }
 
             }
         }
 
     }
 
     /**
      * Add empty paragraph to heading x - heading x
      *
      * @param doc A DOM Document object.
      * @param root The document element (or "root element").
      */
     private static void insertEmptyParaForHeadings(Document doc, Node root){
 
         NodeList hNodes = ((Element) root).getElementsByTagName("text:h");
         for (int i = 0; i < hNodes.getLength()-1; i++) {
 
             Element hElem = (Element) hNodes.item(i);
             Element nextElem;
             Node nextNode = hElem.getNextSibling();
 
             while (nextNode != null && nextNode.getNodeType() != Node.ELEMENT_NODE){
 
                 nextNode = nextNode.getNextSibling();
 
             }
 
             nextElem = (Element) nextNode;
 
             if(nextElem != null
                     && nextElem.getNodeName().equals("text:h")
                     && hElem.hasAttribute("text:outline-level")
                     && nextElem.hasAttribute("text:outline-level")
                     && hElem.getAttribute("text:outline-level").equals(
                     nextElem.getAttribute("text:outline-level"))
                     ){
                 Element para = doc.createElement("text:p");
                 hElem.getParentNode().insertBefore(para, nextNode);
             }
         }
     }
 
     /**
      * Normalize space characters.<br />
      * The <code>text:s</code> element is used to represent the Unicode
      * character " " (U+0020, SPACE).<br />
      * ODF 1.2 specification, section 6.1.3: <q>This element shall be used to
      * represent the second and all following " " (U+0020, SPACE) characters
      * in a sequence of " " (U+0020, SPACE) characters.</q><br />
      * ODF 1.2 specification, section 19.673: <q>The <code>text:c</code> attribute
      * specifies the number of " " (U+0020, SPACE) characters that a
      * <code>text:s</code> element represents. A missing <code>text:c</code>
      * attribute is interpreted as a single " " (U+0020, SPACE).</q>
      *
      * @param doc A DOM Document object.
      * @param root The document element (or "root element").
      */
     private static void normalizeTextS(Document doc, Node root){
 
         NodeList sNodes = ((Element) root).getElementsByTagName("text:s");
 
         for (int i = 0; i < sNodes.getLength(); i++) {
 
             Element elem = (Element) sNodes.item(i);
 
             int c = 1;
             String s = "";
 
             if(elem.hasAttribute("text:c")){
                 c = Integer.parseInt(elem.getAttribute("text:c"));
             }
 
             for(int j=0; j<c; j++){
                 s += " ";
             }
 
             Node textNode = doc.createTextNode(s);
             elem.getParentNode().replaceChild(textNode, elem);
             i--;
         }
     }
 
     /**
      * Remove empty <code>text:p</code> elements.
      * 
      * @param root The document element (or "root element").
      */
     private static void removeEmptyParagraphs(Node root){
 
         // for each text:p
         NodeList pNodes = ((Element) root).getElementsByTagName("text:p");
         for (int i = 0; i < pNodes.getLength(); i++) {
 
             Node node = pNodes.item(i);
 
             // if no text
             if (node.getTextContent().trim().equals("")){
 
                 // if no children
                 if(!node.hasChildNodes()){
 
                    // then remove
                    node.getParentNode().removeChild(node);
                    i--;
 
                 // if children
                 } else {
 
                     boolean empty = true;
 
                     // don't remove if an element is present (like image...)
                     for(int j=0; j<node.getChildNodes().getLength(); j++){
                        if(node.getChildNodes().item(j).getNodeType() == node.ELEMENT_NODE){
                             empty = false;
                        }
                     }
 
                     if(empty){
                         node.getParentNode().removeChild(node);
                         i--;
                     }
                 }
             }
 
         }
     }
 
     // Christophe's best guess for this method's JavaDoc:
     /**
      * Insert <code>pagenum</code> elements to facilitate page numbering support.
      * 
      * @param root The document element (or "root element") of the XML instance.
      * @param node The current node in the XML instance.
      * @param pagenum The current page number.
      * @param incPageNum true if the document contains <code>page-number</code> elements; false if otherwise (??).
      * @param enumType The page number format (<code>style:num-format</code>), which specifies a numbering sequence.
      * According to the ODF 1.2 specification, the defined values for the style:num-format attribute are:<ul>
      * <li><code>1</code>: Hindu-Arabic number sequence starts with 1.</li>
      * <li><code>a</code>: number sequence of lowercase Modern Latin basic alphabet characters starts with "a".</li>
      * <li><code>A</code>: number sequence of uppercase Modern Latin basic alphabet characters starts with "A".</li>
      * <li><code>i</code>: number sequence of lowercase Roman numerals starts with "i".</li>
      * <li><code>I</code>: number sequence of uppercase Roman numerals start with "I".</li>
      * <li>a value of type <code>string<code> (see chapter 18.2 of the ODF specification).</li>
      * <li>an empty string: no number sequence displayed.</li></ul>
      * If no value is given, no number sequence is displayed.
      * @param masterPageName The master page for a paragraph or table style (<code>style:master-page-name</code>), e.g. "Standard".
      * @param isFirst true if it is the first element in the document; false otherwise.
      * @param recCall true if the method should recursively call itself to process the next sibling; false otherwise.
      * @return The next page number.
      * @throws TransformerException
      */
     private static int insertPagination(Node root, Node node, int pagenum, boolean incPageNum, String enumType, String masterPageName, boolean isFirst, boolean recCall) throws TransformerException {
 
         Node next = null;
         boolean append = false;
         String xpath = null;
         String xpath1 = null;
         String xpath2 = null;
         String xpath3 = null;
 
         String styleName = null;
 
         // Select next sibling element
         next = node.getNextSibling();
         while (next != null && next.getNodeType() != Node.ELEMENT_NODE) {
             next = next.getNextSibling();
         }
 
         //if(next!=null)
         //logger.log(Level.SEVERE, "PageProcessing Next Sibling: "+next.getNodeName()+" "+next.getNodeValue());
 
         // if first element in doc
         if (isFirst) {
             append = true;
         }
 
         // text:p or text:h
         if (node.getNodeName().equals("text:p") || node.getNodeName().equals("text:h")) {
 
             styleName = node.getAttributes().getNamedItem("text:style-name").getNodeValue();
 
             // text:p break-before='page'
             xpath1 =
                     "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties[@break-before='page']";
             // text:p page-number='auto'
             xpath2 =
                     "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties[@page-number='auto']";
             // text:p page-number="value"
             xpath3 =
                     "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties[@page-number>0]";
 
             if (XPathAPI.eval(root, xpath3).bool()) {
 
                 xpath = "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties/@page-number";
                 pagenum = Integer.parseInt(XPathAPI.eval(root, xpath).str());
                 pagenum--;
 
                 append = true;
             } else if (XPathAPI.eval(root, xpath2).bool()) {
                 append = true;
             } else if (XPathAPI.eval(root, xpath1).bool()) {
                 append = true;
             }
 
             // update masterPageName
             if (append) {
 
                 xpath = "/document/automatic-styles/style[@name='" + styleName + "']/@master-page-name";
                 boolean hasMasterPage = XPathAPI.eval(root, xpath).bool();
                 if (hasMasterPage) {
                     xpath = "/document/automatic-styles/style[@name='" + styleName + "']/@master-page-name";
                     masterPageName =
                             XPathAPI.eval(root, xpath).str();
                 }
 
             }
 
 
         } // text:list
         else if (node.getNodeName().equals("text:list")) {
 
             styleName = node.getAttributes().getNamedItem("text:style-name").getNodeValue();
 
             // text:list break-before='page'
             xpath1 = "/document/automatic-styles/style[@list-style-name='" + styleName + "']/paragraph-properties[@break-before='page']";
             // text:list page-number='auto'
             xpath2 = "/document/automatic-styles/style[@list-style-name='" + styleName + "']/paragraph-properties[@page-number='auto']";
             // text:list page-number="value"
             xpath3 = "/document/automatic-styles/style[@list-style-name='" + styleName + "']/paragraph-properties[@page-number>0]";
 
             if (XPathAPI.eval(root, xpath3).bool()) {
                 xpath = "/document/automatic-styles/style[@list-style-name='" + styleName + "']/paragraph-properties/@page-number";
                 pagenum = Integer.parseInt(XPathAPI.eval(root, xpath).str());
                 pagenum--;
 
                 append = true;
             } else if (XPathAPI.eval(root, xpath2).bool()) {
                 append = true;
             } else if (XPathAPI.eval(root, xpath1).bool()) {
                 append = true;
             }
 
             // update masterPageName
             if (append) {
 
                 xpath = "/document/automatic-styles/style[@list-style-name='" + styleName + "']/@master-page-name";
                 boolean hasMasterPage = XPathAPI.eval(root, xpath).bool();
                 if (hasMasterPage) {
                     xpath = "/document/automatic-styles/style[@list-style-name='" + styleName + "']/@master-page-name";
                     masterPageName = XPathAPI.eval(root, xpath).str();
                 }
 
             }
 
         } // table:table
         else if (node.getNodeName().equals("table:table")) {
 
             styleName = node.getAttributes().getNamedItem("table:style-name").getNodeValue();
 
             // table:table break-before='page'
             xpath1 = "/document/automatic-styles/style[@name='" + styleName + "']/table-properties[@break-before='page']";
             // table:table page-number='auto'
             xpath2 = "/document/automatic-styles/style[@name='" + styleName + "']/table-properties[@page-number='0']";
             // table:table page-number="value"
             xpath3 = "/document/automatic-styles/style[@name='" + styleName + "']/table-properties[@page-number>0]";
 
             if (XPathAPI.eval(root, xpath3).bool()) {
 
                 xpath = "/document/automatic-styles/style[@name='" + styleName + "']/table-properties/@page-number";
                 pagenum = Integer.parseInt(XPathAPI.eval(root, xpath).str());
                 pagenum--;
 
                 append = true;
             } else if (XPathAPI.eval(root, xpath2).bool()) {
                 append = true;
             } else if (XPathAPI.eval(root, xpath1).bool()) {
                 append = true;
             }
 
             // update masterPageName
             if (append) {
 
                 xpath = "/document/automatic-styles/style[@name='" + styleName + "']/@master-page-name";
                 boolean hasMasterPage = XPathAPI.eval(root, xpath).bool();
                 if (hasMasterPage) {
                     xpath = "/document/automatic-styles/style[@name='" + styleName + "']/@master-page-name";
                     masterPageName =
                             XPathAPI.eval(root, xpath).str();
                 }
 
             }
 
         } // text:table-of-content
         else if (node.getNodeName().equals("text:table-of-content") ||
                 node.getNodeName().equals("text:alphabetical-index") ||
                 node.getNodeName().equals("text:illustration-index") ||
                 node.getNodeName().equals("text:table-index") ||
                 node.getNodeName().equals("text:user-index") ||
                 node.getNodeName().equals("text:object-index") ||
                 node.getNodeName().equals("text:bibliography")) {
 
             Node indexBodyNode = ((Element)node).getElementsByTagName("text:index-body").item(0);
             NodeList indexTitleNodes = ((Element)indexBodyNode).getElementsByTagName("text:index-title");
 
             if(indexTitleNodes.getLength() > 0)
                 styleName = ((Element) indexTitleNodes.item(0)).
                         getElementsByTagName("text:p").item(0).getAttributes().
                         getNamedItem("text:style-name").getNodeValue();
             else
                 styleName = ((Element) indexBodyNode).
                         getElementsByTagName("text:p").item(0).getAttributes().
                         getNamedItem("text:style-name").getNodeValue();
 
             // text:table-of-content break-before='page'
             xpath1 = "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties[@break-before='page']";
             // text:table-of-content page-number='auto'
             xpath2 = "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties[@page-number='auto']";
             // text:table-of-content page-number="value"
             xpath3 = "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties[@page-number>0]";
 
             if (XPathAPI.eval(root, xpath3).bool()) {
 
                 xpath = "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties/@page-number";
                 pagenum = Integer.parseInt(XPathAPI.eval(root, xpath).str());
                 pagenum--;
 
                 append = true;
             } else if (XPathAPI.eval(root, xpath2).bool()) {
                 append = true;
             } else if (XPathAPI.eval(root, xpath1).bool()) {
                 append = true;
             }
 
             // update masterPageName
             if (append) {
 
                 xpath = "/document/automatic-styles/style[@name='" + styleName + "']/@master-page-name";
                 boolean hasMasterPage = XPathAPI.eval(root, xpath).bool();
                 if (hasMasterPage) {
                     xpath = "/document/automatic-styles/style[@name='" + styleName + "']/@master-page-name";
                     masterPageName =
                             XPathAPI.eval(root, xpath).str();
                 }
 
             }
 
         } // text:section
         else if (node.getNodeName().equals("text:section")) {
 
             for (int i = 0; i <
                     node.getChildNodes().getLength(); i++) {
 
                 if (node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
 
                     //System.out.println("child: "+node.getChildNodes().item(i).getNodeName());
                     //System.out.println("child: "+node.getChildNodes().item(i).getNodeValue());
 
                     int oldlength = node.getChildNodes().getLength();
 
                     pagenum = insertPagination(root, node.getChildNodes().item(i), pagenum, incPageNum, enumType, masterPageName, false, false);
 
                     //adding offset for inserted childs
                     i += node.getChildNodes().getLength() - oldlength;
                 }
 
             //System.out.println("child(" + i + "): " + node.getChildNodes().item(i).getNodeName() + " Type: " + node.getChildNodes().item(i).getNodeType());
             }
         }
 
 
         // append pagenum node
         if (append) {
 
             pagenum++;
 
             // update incPageNum
             xpath = "(count(/document/master-styles/master-page[@name='" + masterPageName + "']/header/p/page-number)" +
                     "+" + "count(/document/master-styles/master-page[@name='" + masterPageName + "']/footer/p/page-number))>0";
             incPageNum = XPathAPI.eval(root, xpath).bool();
 
             // update enumType
             xpath =
                     "/document/automatic-styles/page-layout[@name=(/document/master-styles/master-page[@name='" + masterPageName + "']/@page-layout-name)]/page-layout-properties/@num-format";
             enumType =
                     XPathAPI.eval(root, xpath).str();
 
 
             /** Patch for Overwritten page num-format */
             xpath = "/document/master-styles/master-page[@name='" + masterPageName + "']//page-number/@num-format";
             String enumTypeOverwrite = XPathAPI.eval(root, xpath).str();
             if(enumTypeOverwrite.length()>0){
                 enumType = enumTypeOverwrite;
             }
 
             Element pageNode = root.getOwnerDocument().createElement("pagenum");
             pageNode.setAttribute("num", Integer.toString(pagenum));
             pageNode.setAttribute("enum", enumType);
             pageNode.setAttribute("render", Boolean.toString(incPageNum));
 
             if (enumType.equals("i")) {
                 pageNode.setAttribute("value", RomanNumbering.toRoman(pagenum));
             } else if (enumType.equals("I")) {
                 pageNode.setAttribute("value", RomanNumbering.toRoman(pagenum).toUpperCase());
             } else if (enumType.equals("a")) {
                 pageNode.setAttribute("value", LetterNumbering.toLetter(pagenum));
             } else if (enumType.equals("A")) {
                 pageNode.setAttribute("value", LetterNumbering.toLetter(pagenum).toUpperCase());
             } else {
                 pageNode.setAttribute("value", String.valueOf(pagenum));
             }
 
             node.getParentNode().insertBefore(pageNode, node);
 
         }
 
         String nName = node.getNodeName();
         NodeList pageBreaks = ((Element) node).getElementsByTagName("text:soft-page-break");
 
         if (pageBreaks.getLength() > 0) {
 
             // case: text:h with a soft-page-break inside
             if(nName.equals("text:h")){
 
                 pagenum++;
                 Element pageNode = root.getOwnerDocument().createElement("pagenum");
                 pageNode.setAttribute("num", Integer.toString(pagenum));
                 pageNode.setAttribute("enum", enumType);
                 pageNode.setAttribute("render", Boolean.toString(incPageNum));
                 pageNode.setAttribute("value", String.valueOf(pagenum));
 
                 pageBreaks.item(0).getParentNode().getParentNode().insertBefore(pageNode, pageBreaks.item(0).getParentNode());
 
             // case: toc & indexes with a soft-page-break inside
             } else if (nName.equals("text:table-of-content") ||
                     nName.equals("text:alphabetical-index") ||
                     nName.equals("text:illustration-index") ||
                     nName.equals("text:table-index") ||
                     nName.equals("text:user-index") ||
                     nName.equals("text:object-index") ||
                     nName.equals("text:bibliography")) {
 
                 for (int i = 0; i < pageBreaks.getLength(); i++) {
 
                     // adding tableNode and pageNode
                     pagenum++;
 
                     Element pageNode = root.getOwnerDocument().createElement("pagenum");
                     pageNode.setAttribute("num", Integer.toString(pagenum));
                     pageNode.setAttribute("enum", enumType);
                     pageNode.setAttribute("render", Boolean.toString(incPageNum));
                     pageNode.setAttribute("value", String.valueOf(pagenum));
 
                     pageBreaks.item(i).getParentNode().getParentNode().insertBefore(pageNode, pageBreaks.item(i).getParentNode());
                 }
 
             } else {
 
                 for(int i = 0; i < pageBreaks.getLength(); i++){
 
                     pagenum++;
 
                     Element pageNode = root.getOwnerDocument().createElement("pagenum");
                     pageNode.setAttribute("num", Integer.toString(pagenum));
                     pageNode.setAttribute("enum", enumType);
                     pageNode.setAttribute("render", Boolean.toString(incPageNum));
                     pageNode.setAttribute("value", String.valueOf(pagenum));
                     pageBreaks.item(i).getParentNode().replaceChild(pageNode, pageBreaks.item(i));
                 }
 
             }
 
         }
 
 
         // recursive call on next sibling
         if (next != null && recCall) {
             //System.out.println("next: " + next.getNodeName() + " value: " + next.getTextContent());
             pagenum = insertPagination(root, next, pagenum, incPageNum, enumType, masterPageName, false, true);
         }
 
         return pagenum;
     }
 
     /**
      * Insert MathML separated files into Flat ODT XML
      * @param docBuilder
      * @param contentDoc
      * @param zf
      * @param parentPath
      * @throws java.io.IOException
      * @throws org.xml.sax.SAXException
      */
     private static void replaceObjectContent(
             DocumentBuilder docBuilder, Document contentDoc, ZipFile zf) throws IOException, SAXException {
 
         logger.fine("entering");
 
         Element root = contentDoc.getDocumentElement();
         NodeList nodelist = root.getElementsByTagName("draw:object");
 
         for (int i = 0; i < nodelist.getLength(); i++) {
 
             Node objectNode = nodelist.item(i);
             Node hrefNode = objectNode.getAttributes().getNamedItem("xlink:href");
 
             String objectPath = hrefNode.getTextContent();
             logger.fine("object path=" + objectPath);
 
             Document objectDoc = docBuilder.parse(zf.getInputStream(zf.getEntry(objectPath.substring(2) + "/" + "content.xml")));
             Node objectContentNode = objectDoc.getDocumentElement();
 
             String tagName = objectContentNode.getNodeName();
             logger.fine("tagName=" + tagName);
 
             if (tagName.equals("math:math") || tagName.equals("math")) {
                 logger.fine("replacing math");
 
                 Node newObjectNode = contentDoc.createElement("draw:object");
                 newObjectNode.appendChild(contentDoc.importNode(objectContentNode, true));
                 objectNode.getParentNode().replaceChild(newObjectNode, objectNode);
             }
 
         }
 
         logger.fine("done");
     }
 
     private static final void copyInputStream(InputStream in, OutputStream out)
             throws IOException {
         byte[] buffer = new byte[1024];
         int len;
 
         while ((len = in.read(buffer)) >= 0) {
             out.write(buffer, 0, len);
         }
 
         in.close();
         out.close();
     }
 
     /**
      * Saves a document object model (DOM) as a file with the chosen file name.
      * 
      * @param doc DOM representation of XML instance.
      * @param filename The file to which the DOM should be saved. The String must conform to the URI syntax.
      * @return true if the file could be saved; false if the file could not be saved.
      */
     public static boolean saveDOM(Document doc, String filename) {
         boolean save = false;
         try {
 
             Transformer transformer = TransformerFactory.newInstance().newTransformer();
             transformer.setOutputProperty(OutputKeys.METHOD, "xml");
             transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
 
             StreamResult result = new StreamResult(filename);
             DOMSource source = new DOMSource(doc);
             transformer.transform(source, result);
 
             save = true;
         } catch (TransformerConfigurationException ex) {
             logger.log(Level.SEVERE, null, ex);
         } catch (TransformerException ex) {
             logger.log(Level.SEVERE, null, ex);
         } finally {
 
             return save;
 
         }
     }
 }
