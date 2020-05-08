 package org.jboss.pressgang.ccms.utils.common;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 /**
  * A collection of static variables and functions that can be used when working
  * with DocBook
  */
 public class DocBookUtilities {
     /**
      * The name of the section tag
      */
     public static final String TOPIC_ROOT_NODE_NAME = "section";
     /**
      * The name of the id attribute
      */
     public static final String TOPIC_ROOT_ID_ATTRIBUTE = "id";
     /**
      * The name of the title tag
      */
     public static final String TOPIC_ROOT_TITLE_NODE_NAME = "title";
     /**
      * The name of the sectioninfo tag
      */
     public static final String TOPIC_ROOT_SECTIONINFO_NODE_NAME = "sectioninfo";
 
     /**
      * Finds the first title element in a DocBook XML file.
      *
      * @param xml The docbook xml file to find the title from.
      * @return The first title found in the xml.
      */
     public static String findTitle(final String xml) {
         // Convert the string to a document to make it easier to get the proper title
         Document doc = null;
         try {
             doc = XMLUtilities.convertStringToDocument(xml);
         } catch (Exception ex) {
             ExceptionUtilities.handleException(ex);
         }
 
         return findTitle(doc);
     }
 
     /**
      * Finds the first title element in a DocBook XML file.
      *
      * @param doc The docbook xml transformed into a DOM Document to find the title from.
      * @return The first title found in the xml.
      */
     public static String findTitle(final Document doc) {
         if (doc == null) return null;
 
         // loop through the child nodes until the title element is found
         final NodeList childNodes = doc.getDocumentElement().getChildNodes();
         for (int i = 0; i < childNodes.getLength(); i++) {
             Node node = childNodes.item(i);
 
             // check if the node is the title and if its parent is the document root element
             if (node.getNodeName().equals(TOPIC_ROOT_TITLE_NODE_NAME) && node.getParentNode().equals(doc.getDocumentElement())) {
                 return ((Element) node).getTextContent();
             }
         }
 
         return null;
     }
 
     /**
      * Escapes a title so that it is alphanumeric or has a fullstop, underscore or hyphen only.
      * It also removes anything from the front of the title that isn't alphanumeric.
      *
      * @param title The title to be escaped
      * @return The escaped title string.
      */
     public static String escapeTitle(final String title) {
         return title.replaceAll(" ", "_").replaceAll("^[^A-Za-z0-9]*", "").replaceAll("[^A-Za-z0-9_.-]", "");
     }
 
     public static void setSectionTitle(final String titleValue, final Document doc) {
         assert doc != null : "The doc parameter can not be null";
 
         final Element newTitle = doc.createElement(DocBookUtilities.TOPIC_ROOT_TITLE_NODE_NAME);
 
         /*
          * Attempt to parse the title as XML. If this fails
          * then just set the title as plain text.
          */
         try {
             final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             final DocumentBuilder docBuilder = factory.newDocumentBuilder();
             final Document tempDoc = docBuilder.parse(new ByteArrayInputStream(("<title>" + titleValue + "</title>").getBytes("UTF-8")));
             final Node titleEle = doc.importNode(tempDoc.getDocumentElement(), true);
 
             // Add the child elements to the ulink node
             final NodeList nodes = titleEle.getChildNodes();
             while (nodes.getLength() > 0) {
                 newTitle.appendChild(nodes.item(0));
             }
         } catch (Exception e) {
             newTitle.appendChild(doc.createTextNode(titleValue));
         }
 
         final Element docElement = doc.getDocumentElement();
         if (docElement != null && docElement.getNodeName().equals(DocBookUtilities.TOPIC_ROOT_NODE_NAME)) {
             // remove any id attributes from the section
             docElement.removeAttribute("id");
 
             final NodeList titleNodes = docElement.getElementsByTagName(DocBookUtilities.TOPIC_ROOT_TITLE_NODE_NAME);
             // see if we have a title node whose parent is the section
             if (titleNodes.getLength() != 0 && titleNodes.item(0).getParentNode().equals(docElement)) {
                 final Node title = titleNodes.item(0);
                 title.getParentNode().replaceChild(newTitle, title);
             } else {
                 // Find the first node that isn't text or a comment
                 Node firstNode = docElement.getFirstChild();
                 while (firstNode != null && firstNode.getNodeType() != Node.ELEMENT_NODE) {
                     firstNode = firstNode.getNextSibling();
                 }
 
                 // Set the section title based on if the first node is a "sectioninfo" node.
                 if (firstNode != null && firstNode.getNodeName().equals(DocBookUtilities.TOPIC_ROOT_SECTIONINFO_NODE_NAME)) {
                     final Node nextNode = firstNode.getNextSibling();
                     if (nextNode != null) {
                         docElement.insertBefore(newTitle, nextNode);
                     } else {
                         docElement.appendChild(newTitle);
                     }
                 } else if (firstNode != null) {
                     docElement.insertBefore(newTitle, firstNode);
                 } else {
                     docElement.appendChild(newTitle);
                 }
             }
         }
     }
 
     public static void setRootElementTitle(final String titleValue, final Document doc) {
         assert doc != null : "The doc parameter can not be null";
 
         final Element newTitle = doc.createElement(DocBookUtilities.TOPIC_ROOT_TITLE_NODE_NAME);
         
         /* 
          * Attempt to parse the title as XML. If this fails
          * then just set the title as plain text.
          */
         try {
             final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             final DocumentBuilder docBuilder = factory.newDocumentBuilder();
             final Document tempDoc = docBuilder.parse(new ByteArrayInputStream(("<title>" + titleValue + "</title>").getBytes("UTF-8")));
             final Node titleEle = doc.importNode(tempDoc.getDocumentElement(), true);
             
             /* Add the child elements to the ulink node */
             final NodeList nodes = titleEle.getChildNodes();
             while (nodes.getLength() > 0) {
                 newTitle.appendChild(nodes.item(0));
             }
         } catch (Exception e) {
             newTitle.appendChild(doc.createTextNode(titleValue));
         }
 
         final Element docElement = doc.getDocumentElement();
         if (docElement != null) {
             final NodeList titleNodes = docElement.getElementsByTagName(DocBookUtilities.TOPIC_ROOT_TITLE_NODE_NAME);
             /* see if we have a title node whose parent is the section */
             if (titleNodes.getLength() != 0 && titleNodes.item(0).getParentNode().equals(docElement)) {
                 final Node title = titleNodes.item(0);
                 title.getParentNode().replaceChild(newTitle, title);
             } else {
                 docElement.appendChild(newTitle);
             }
         }
     }
 
     public static void setSectionInfo(final Element sectionInfo, final Document doc) {
         assert doc != null : "The doc parameter can not be null";
         assert sectionInfo != null : "The sectionInfo parameter can not be null";
 
         final Element docElement = doc.getDocumentElement();
         if (docElement != null && docElement.getNodeName().equals(DocBookUtilities.TOPIC_ROOT_NODE_NAME)) {
             final NodeList sectionInfoNodes = docElement.getElementsByTagName(DocBookUtilities.TOPIC_ROOT_SECTIONINFO_NODE_NAME);
             /* see if we have a sectioninfo node whose parent is the section */
             if (sectionInfoNodes.getLength() != 0 && sectionInfoNodes.item(0).getParentNode().equals(docElement)) {
                 final Node sectionInfoNode = sectionInfoNodes.item(0);
                 sectionInfoNode.getParentNode().replaceChild(sectionInfo, sectionInfoNode);
             } else {
                 final Node firstNode = docElement.getFirstChild();
                 if (firstNode != null) docElement.insertBefore(sectionInfo, firstNode);
                 else docElement.appendChild(sectionInfo);
             }
         }
     }
 
     public static String buildChapter(final String contents, final String title) {
         return buildChapter(contents, title, null);
     }
 
     public static String buildChapter(final String contents, final String title, final String id) {
         final String titleContents = title == null || title.length() == 0 ? "" : title;
         final String chapterContents = contents == null || contents.length() == 0 ? "" : contents;
         final String idAttribute = id == null || id.length() == 0 ? "" : " id=\"" + id + "\"";
         return "<chapter" + idAttribute + "><title>" + titleContents + "</title>" + chapterContents + "</chapter>";
     }
 
     public static String buildAppendix(final String contents, final String title) {
         return buildAppendix(contents, title, null);
     }
 
     public static String buildAppendix(final String contents, final String title, final String id) {
         final String titleContents = title == null || title.length() == 0 ? "" : title;
         final String chapterContents = contents == null || contents.length() == 0 ? "" : contents;
         final String idAttribute = id == null || id.length() == 0 ? "" : " id=\"" + id + "\"";
         return "<appendix" + idAttribute + "><title>" + titleContents + "</title>" + chapterContents + "</appendix>";
     }
 
     public static String buildCleanSection(final String contents, final String title) {
         return buildCleanSection(contents, title, null);
     }
 
     public static String buildCleanSection(final String contents, final String title, final String id) {
         final String titleContents = title == null || title.length() == 0 ? "" : title;
         final String chapterContents = contents == null || contents.length() == 0 ? "" : contents;
         final String idAttribute = id == null || id.length() == 0 ? "" : " id=\"" + id + "\"";
         return "<section" + idAttribute + "><title>" + titleContents + "</title>" + chapterContents + "</section>";
     }
 
     /**
      * Add/Set the PUBLIC DOCTYPE for some XML content.
      *
      * @param xml             The XML to add or set the DOCTYPE for.
      * @param publicName      The PUBLIC name for the DOCTTYPE.
      * @param publicLocation  The PUBLIC location/url for the DOCTYPE.
      * @param rootElementName The root Element Name for the DOCTYPE.
      * @return The XML with the DOCTYPE added.
      */
     public static String addXMLPublicDoctype(final String xml, final String publicName, final String publicLocation,
             final String rootElementName) {
         return addXMLPublicDoctype(xml, publicName, publicLocation, null, rootElementName);
     }
 
     /**
      * Add/Set the PUBLIC DOCTYPE for some XML content.
      *
      * @param xml             The XML to add or set the DOCTYPE for.
      * @param publicName      The PUBLIC name for the DOCTTYPE.
      * @param publicLocation  The PUBLIC location/url for the DOCTYPE.
      * @param entityFileName  A name for a local entity to set in the doctype.
      * @param rootElementName The root Element Name for the DOCTYPE.
      * @return The XML with the DOCTYPE added.
      */
     public static String addXMLPublicDoctype(final String xml, final String publicName, final String publicLocation,
             final String entityFileName, final String rootElementName) {
         final String preamble = XMLUtilities.findPreamble(xml);
         final String docType = XMLUtilities.findDocumentType(xml);
         final String fixedPreamble = preamble == null ? "<?xml version='1.0' encoding='UTF-8' ?>\n" : preamble + "\n";
 
         final String fixedXML;
         if (docType != null) {
             final String tempFixedXML = preamble == null ? xml : xml.replace(preamble, "");
             fixedXML = tempFixedXML.replace(docType, "");
         } else {
             fixedXML = preamble == null ? xml : xml.replace(preamble, "");
         }
 
         final StringBuilder retValue = new StringBuilder(fixedPreamble);
         retValue.append("<!DOCTYPE ");
         if (rootElementName == null) {
             retValue.append("chapter");
         } else {
             retValue.append(rootElementName);
         }
         retValue.append(" PUBLIC \"" + publicName + "\" \"" + publicLocation + "\" ");
 
         // Add the local entity file
         if (entityFileName != null) {
             retValue.append("[\n");
             retValue.append("<!ENTITY % BOOK_ENTITIES SYSTEM \"" + entityFileName + "\">\n");
             retValue.append("%BOOK_ENTITIES;\n");
             retValue.append("]");
         }
 
         retValue.append(">\n\n");
         retValue.append(fixedXML);
 
         return retValue.toString();
     }
 
     public static String addDocbook45XMLDoctype(final String xml) {
         return addDocbook45XMLDoctype(xml, null, "chapter");
     }
 
     public static String addDocbook45XMLDoctype(final String xml, final String entityFileName, final String rootElementName) {
         return addXMLPublicDoctype(xml, "-//OASIS//DTD DocBook XML V4.5//EN", "http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd",
                 entityFileName, rootElementName);
     }
 
     public static String addDocbook50XMLDoctype(final String xml) {
         return addDocbook50XMLDoctype(xml, null, "chapter");
     }
 
     public static String addDocbook50XMLDoctype(final String xml, final String entityFileName, final String rootElementName) {
         return addXMLPublicDoctype(xml, "-//OASIS//DTD DocBook XML V5.0//EN", "http://www.oasis-open.org/docbook/xml/5.0/docbookx.dtd",
                 entityFileName, rootElementName);
     }
 
     public static String buildXRefListItem(final String xref, final String role) {
         final String roleAttribute = role == null || role.length() == 0 ? "" : " role=\"" + role + "\"";
         return "<listitem><para><xref" + roleAttribute + " linkend=\"" + xref + "\"/></para></listitem>";
     }
 
     public static List<Element> buildXRef(final Document xmlDoc, final String xref) {
         return buildXRef(xmlDoc, xref, null);
     }
 
     public static List<Element> buildXRef(final Document xmlDoc, final String xref, final String xrefStyle) {
         final List<Element> retValue = new ArrayList<Element>();
 
         final Element xrefItem = xmlDoc.createElement("xref");
         xrefItem.setAttribute("linkend", xref);
 
         if (xrefStyle != null && !xrefStyle.isEmpty()) {
             xrefItem.setAttribute("xrefstyle", xrefStyle);
         }
 
         retValue.add(xrefItem);
 
         return retValue;
     }
 
     public static String buildXRef(final String xref) {
         return "<xref linkend=\"" + xref + "\" />";
     }
 
     public static String buildXRef(final String xref, final String xrefStyle) {
         return "<xref linkend=\"" + xref + "\" xrefstyle=\"" + xrefStyle + "\" />";
     }
 
     public static List<Element> buildULink(final Document xmlDoc, final String url, final String label) {
         final List<Element> retValue = new ArrayList<Element>();
 
         final Element ulinkItem = xmlDoc.createElement("ulink");
         ulinkItem.setAttribute("url", url);
 
         final Text labelElement = xmlDoc.createTextNode(label);
         ulinkItem.appendChild(labelElement);
 
         retValue.add(ulinkItem);
 
         return retValue;
     }
 
     public static String buildULink(final String url, final String label) {
         return "<ulink url=\"" + url + "\">" + label + "</ulink>";
     }
 
     public static String buildULinkListItem(final String url, final String label) {
         return "<listitem><para><ulink url=\"" + url + "\">" + label + "</ulink></para></listitem>";
     }
 
     public static List<Element> buildEmphasisPrefixedXRef(final Document xmlDoc, final String prefix, final String xref) {
         final List<Element> retValue = new ArrayList<Element>();
 
         final Element emphasis = xmlDoc.createElement("emphasis");
         emphasis.setTextContent(prefix);
         retValue.add(emphasis);
 
         final Element xrefItem = xmlDoc.createElement("xref");
         xrefItem.setAttribute("linkend", xref);
         retValue.add(xrefItem);
 
         return retValue;
     }
 
     public static List<Element> buildEmphasisPrefixedULink(final Document xmlDoc, final String prefix, final String url,
             final String label) {
         final List<Element> retValue = new ArrayList<Element>();
 
         final Element emphasis = xmlDoc.createElement("emphasis");
         emphasis.setTextContent(prefix);
         retValue.add(emphasis);
 
         final Element xrefItem = xmlDoc.createElement("ulink");
         xrefItem.setAttribute("url", url);
         retValue.add(xrefItem);
 
         final Text labelElement = xmlDoc.createTextNode(label);
         xrefItem.appendChild(labelElement);
 
         return retValue;
     }
 
     public static Node buildDOMXRefLinkListItem(final String xref, final String title, final Document xmlDoc) {
         final Element listItem = xmlDoc.createElement("listitem");
 
         final Element paraItem = xmlDoc.createElement("para");
         listItem.appendChild(paraItem);
 
         final Element linkItem = xmlDoc.createElement("link");
         linkItem.setAttribute("linkend", xref);
         linkItem.setTextContent(title);
         paraItem.appendChild(linkItem);
 
         return listItem;
     }
 
     public static Node buildDOMLinkListItem(final List<Node> children, final Document xmlDoc) {
         final Element listItem = xmlDoc.createElement("listitem");
 
         final Element paraItem = xmlDoc.createElement("para");
         listItem.appendChild(paraItem);
 
         for (final Node node : children)
             paraItem.appendChild(node);
 
         return listItem;
     }
 
     public static Node buildDOMXRef(final String xref, final String title, final Document xmlDoc) {
         final Element linkItem = xmlDoc.createElement("link");
         linkItem.setAttribute("linkend", xref);
         linkItem.setTextContent(title);
         return linkItem;
     }
 
     public static Node buildDOMText(final String title, final Document xmlDoc) {
         final Node textNode = xmlDoc.createTextNode(title);
         return textNode;
     }
 
     public static String buildListItem(final String text) {
         return "<listitem><para>" + text + "</para></listitem>\n";
     }
 
     public static Element buildDOMListItem(final Document doc, final String text) {
         final Element listItem = doc.createElement("listitem");
         final Element para = doc.createElement("para");
         para.setTextContent(text);
         listItem.appendChild(para);
         return listItem;
     }
 
     public static String buildSection(final String contents, final String title) {
         return buildSection(contents, title, null, null, null);
     }
 
     public static String buildSection(final String contents, final String title, final String id) {
         return buildSection(contents, title, id, null, null);
     }
 
     public static String buildSection(final String contents, final String title, final String id, final String titleRole) {
         return buildSection(contents, title, id, titleRole, null);
     }
 
     public static String buildSection(final String contents, final String title, final String id, final String titleRole,
             final String xreflabel) {
         final String idAttribute = id == null || id.length() == 0 ? "" : " id=\"" + id + "\"";
         final String xreflabelAttribute = xreflabel == null || xreflabel.length() == 0 ? "" : " xreflabel=\"" + xreflabel + "\"";
         final String titleRoleAttribute = titleRole == null || titleRole.length() == 0 ? "" : " role=\"" + titleRole + "\"";
 
         return "<section" + idAttribute + xreflabelAttribute + ">\n" +
                 "<title" + titleRoleAttribute + ">" + title + "</title>\n" +
                 contents + "\n" +
                 "</section>\n";
     }
 
     public static String wrapInListItem(final String content) {
         return "<listitem>" + content + "</listitem>";
     }
 
     public static String wrapListItems(final List<String> listItems) {
         return wrapListItems(listItems, null, null);
     }
 
     public static String wrapListItems(final List<String> listItems, final String title) {
         return wrapListItems(listItems, title, null);
     }
 
     public static String wrapListItems(final List<String> listItems, final String title, final String id) {
         final String idAttribute = id != null && id.length() != 0 ? " id=\"" + id + "\" " : "";
         final String titleElement = title == null || title.length() == 0 ? "" : "<title>" + title + "</title>";
 
         final StringBuilder retValue = new StringBuilder("<itemizedlist" + idAttribute + ">" + titleElement);
         for (final String listItem : listItems)
             retValue.append(listItem);
         retValue.append("</itemizedlist>");
 
         return retValue.toString();
     }
 
     public static String wrapListItemsInPara(final String listItems) {
         if (listItems.length() != 0) {
             return "<para>" +
                     "<itemizedlist>\n" + listItems + "</itemizedlist>" +
                     "</para>";
         }
 
         return "";
     }
 
     public static String wrapInPara(final String contents) {
         return wrapInPara(contents, null, null);
     }
 
     public static String wrapInPara(final String contents, final String role) {
         return wrapInPara(contents, role, null);
     }
 
     public static String wrapInPara(final String contents, final String role, final String id) {
         final String idAttribute = id == null || id.length() == 0 ? "" : " id=\"" + id + "\"";
         final String roleAttribute = role == null || role.length() == 0 ? "" : " role=\"" + role + "\"";
         return "<para" + idAttribute + roleAttribute + ">" +
                 contents +
                 "</para>";
     }
 
     public static List<Element> wrapItemizedListItemsInPara(final Document xmlDoc, final List<List<Element>> items) {
         final List<Element> retValue = new ArrayList<Element>();
 
         final Element para = xmlDoc.createElement("para");
 
         final Element itemizedlist = xmlDoc.createElement("itemizedlist");
         para.appendChild(itemizedlist);
 
         for (final List<Element> itemSequence : items) {
             final Element listitem = xmlDoc.createElement("listitem");
             itemizedlist.appendChild(listitem);
 
             final Element listItemPara = xmlDoc.createElement("para");
             listitem.appendChild(listItemPara);
 
             for (final Element item : itemSequence) {
                 listItemPara.appendChild(item);
             }
         }
 
         retValue.add(para);
 
         return retValue;
     }
 
     public static List<Element> wrapOrderedListItemsInPara(final Document xmlDoc, final List<List<Element>> items) {
         final List<Element> retValue = new ArrayList<Element>();
 
         final Element para = xmlDoc.createElement("para");
 
         final Element orderedlist = xmlDoc.createElement("orderedlist");
         para.appendChild(orderedlist);
 
         for (final List<Element> itemSequence : items) {
             final Element listitem = xmlDoc.createElement("listitem");
             orderedlist.appendChild(listitem);
 
             final Element listItemPara = xmlDoc.createElement("para");
             listitem.appendChild(listItemPara);
 
             for (final Element item : itemSequence) {
                 listItemPara.appendChild(item);
             }
         }
 
         retValue.add(para);
 
         return retValue;
     }
 
     public static List<Element> wrapItemsInListItems(final Document xmlDoc, final List<List<Element>> items) {
         final List<Element> retValue = new ArrayList<Element>();
 
         for (final List<Element> itemSequence : items) {
             final Element listitem = xmlDoc.createElement("listitem");
             final Element listItemPara = xmlDoc.createElement("para");
             listitem.appendChild(listItemPara);
 
             for (final Element item : itemSequence) {
                 listItemPara.appendChild(item);
             }
 
             retValue.add(listitem);
         }
 
         return retValue;
     }
 
     public static String wrapInSimpleSect(final String contents) {
         return wrapInSimpleSect(contents, null, null);
     }
 
     public static String wrapInSimpleSect(final String contents, final String role) {
         return wrapInSimpleSect(contents, null, null);
     }
 
     public static String wrapInSimpleSect(final String contents, final String role, final String id) {
         final String roleAttribute = role == null || role.length() == 0 ? "" : " role=\"" + role + "\"";
         final String idAttribute = id == null || id.length() == 0 ? "" : " id=\"" + id + "\"";
 
         return "<simplesect" + idAttribute + roleAttribute + ">\n" +
                 "\t<title></title>\n" +
                 contents + "\n" +
                 "</simplesect>";
     }
 
     public static Element wrapListItems(final Document xmlDoc, final List<Node> listItems) {
         return wrapListItems(xmlDoc, listItems, null);
     }
 
     public static Element wrapListItems(final Document xmlDoc, final List<Node> listItems, final String title) {
         final Element paraElement = xmlDoc.createElement("para");
 
         final Element itemizedlistElement = xmlDoc.createElement("itemizedlist");
         paraElement.appendChild(itemizedlistElement);
 
         if (title != null) {
             final Element titleElement = xmlDoc.createElement("title");
             itemizedlistElement.appendChild(titleElement);
             titleElement.setTextContent(title);
         }
 
         for (final Node listItem : listItems)
             itemizedlistElement.appendChild(listItem);
 
         return paraElement;
     }
 
     public static void insertNodeAfter(final Node reference, final Node insert) {
         final Node parent = reference.getParentNode();
         final Node nextSibling = reference.getNextSibling();
 
         if (parent == null) return;
 
         if (nextSibling != null) parent.insertBefore(insert, nextSibling);
         else parent.appendChild(insert);
     }
 
     public static Node createRelatedTopicXRef(final Document xmlDoc, final String xref, final Node parent) {
         final Element listItem = xmlDoc.createElement("listitem");
         if (parent != null) parent.appendChild(listItem);
 
         final Element paraItem = xmlDoc.createElement("para");
         listItem.appendChild(paraItem);
 
         final Element xrefItem = xmlDoc.createElement("xref");
         xrefItem.setAttribute("linkend", xref);
         paraItem.appendChild(xrefItem);
 
         return listItem;
     }
 
     public static Node createRelatedTopicXRef(final Document xmlDoc, final String xref) {
         return createRelatedTopicXRef(xmlDoc, xref, null);
     }
 
     public static Node createRelatedTopicULink(final Document xmlDoc, final String url, final String title, final Node parent) {
         final Element listItem = xmlDoc.createElement("listitem");
         if (parent != null) parent.appendChild(listItem);
 
         final Element paraItem = xmlDoc.createElement("para");
         listItem.appendChild(paraItem);
 
         final Element xrefItem = xmlDoc.createElement("ulink");
         xrefItem.setAttribute("url", url);
         paraItem.appendChild(xrefItem);
         
         /* 
          * Attempt to parse the title as XML. If this fails
          * then just set the title as plain text.
          */
         try {
             final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             final DocumentBuilder docBuilder = factory.newDocumentBuilder();
             final Document doc = docBuilder.parse(new ByteArrayInputStream(("<title>" + title + "</title>").getBytes("UTF-8")));
             final Node titleEle = xmlDoc.importNode(doc.getDocumentElement(), true);
 
 	        /* Add the child elements to the ulink node */
             final NodeList nodes = titleEle.getChildNodes();
             while (nodes.getLength() > 0) {
                 xrefItem.appendChild(nodes.item(0));
             }
         } catch (Exception e) {
             final Text labelElement = xmlDoc.createTextNode(title);
             xrefItem.appendChild(labelElement);
         }
 
         return listItem;
     }
 
     public static Node createRelatedTopicULink(final Document xmlDoc, final String url, final String title) {
         return createRelatedTopicULink(xmlDoc, url, title, null);
     }
 
     public static Node createRelatedTopicItemizedList(final Document xmlDoc, final String title) {
         final Node itemizedlist = xmlDoc.createElement("itemizedlist");
         final Node itemizedlistTitle = xmlDoc.createElement("title");
         itemizedlistTitle.setTextContent(title);
         itemizedlist.appendChild(itemizedlistTitle);
 
         return itemizedlist;
     }
 
     public static Document wrapDocumentInSection(final Document doc) {
         if (!doc.getDocumentElement().getNodeName().equals("section")) {
             final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
             Document newDoc = null;
             try {
                 final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                 newDoc = dBuilder.newDocument();
             } catch (ParserConfigurationException ex) {
                 ExceptionUtilities.handleException(ex);
                 return null;
             }
             final Element section = newDoc.createElement("section");
             section.appendChild(newDoc.importNode(doc.getDocumentElement(), true));
             newDoc.appendChild(section);
             return newDoc;
         } else {
             return doc;
         }
     }
 
     /**
      * Wrap a list of Strings in a {@code<row>} element. Each string
      * is also wrapped in a {@code<entry>} element.
      *
      * @param items The list of items to be set in the table row.
      * @return The strings wrapped in row and entry elements.
      */
     public static String wrapInTableRow(final List<String> items) {
         final StringBuilder output = new StringBuilder("<row>");
         for (final String entry : items) {
             output.append("<entry>" + entry + "</entry>");
         }
 
         output.append("</row>");
         return output.toString();
     }
 
     public static String wrapInTable(final String title, final List<List<String>> rows) {
         return wrapInTable(title, null, null, rows);
     }
 
     public static String wrapInTable(final String title, final List<String> headers, final List<List<String>> rows) {
         return wrapInTable(title, headers, null, rows);
     }
 
     public static String wrapInTable(final String title, final List<String> headers, final List<String> footers,
             final List<List<String>> rows) {
         if (rows == null) throw new IllegalArgumentException("rows cannot be null");
 
         final StringBuilder output = new StringBuilder("<table>\n");
         output.append("\t<title>" + title + "</title>\n");
 
         final int numColumns = headers == null ? (rows == null || rows.size() == 0 ? 0 : rows.get(0).size()) : Math.max(headers.size(),
                 (rows == null || rows.size() == 0 ? 0 : rows.get(0).size()));
         output.append("\t<tgroup cols=\"" + numColumns + "\">\n");
         // Add the headers
         if (headers != null && !headers.isEmpty()) {
             output.append("\t\t<thead>\n");
             output.append("\t\t\t" + wrapInTableRow(headers));
             output.append("\t\t</thead>\n");
         }
 
         // Add the footer
         if (footers != null && !footers.isEmpty()) {
             output.append("\t\t<tfoot>\n");
             output.append("\t\t\t" + wrapInTableRow(footers));
             output.append("\t\t</tfoot>\n");
         }
 
         // Create the table body
         output.append("\t\t<tbody>\n");
         for (final List<String> row : rows) {
             output.append("\t\t\t" + wrapInTableRow(row));
         }
         output.append("\t\t</tbody>\n");
         output.append("\t</tgroup>\n");
         output.append("</table>\n");
         return output.toString();
     }
 
     public static String wrapInGlossTerm(final String glossTerm) {
         return "<glossterm>" + glossTerm + "</glossterm>";
     }
 
     /**
      * Creates a Glossary Definition element that contains an itemized list.
      * Each item specified in the items list is wrapped in a {@code<para>} and
      * {@code<listitem>} element and then added to the itemizedlist.
      *
      * @param title The title for the itemized list.
      * @param items The list of items that should be created in the list.
      * @return The {@code<glossdef>} wrapped list of items.
      */
     public static String wrapInItemizedGlossDef(final String title, final List<String> items) {
         final List<String> itemizedList = new ArrayList<String>();
         for (final String listItem : items) {
             itemizedList.add(wrapInListItem(wrapInPara(listItem)));
         }
         return "<glossdef>" + DocBookUtilities.wrapListItems(itemizedList) + "</glossdef>";
     }
 
     public static String wrapInGlossEntry(final String glossTerm, final String glossDef) {
         return "<glossentry>" + glossTerm + glossDef + "</glossentry>";
     }
 
     /**
      * Check to ensure that a table isn't missing any entries in its rows.
      *
      * @param table The DOM table node to be checked.
      * @return True if the table has the required number of entries, otherwise false.
      */
     public static boolean validateTableRows(final Element table) {
         assert table != null;
         assert table.getNodeName().equals("table") || table.getNodeName().equals("informaltable");
 
         final NodeList tgroups = table.getElementsByTagName("tgroup");
         for (int i = 0; i < tgroups.getLength(); i++) {
             final Element tgroup = (Element) tgroups.item(i);
             if (!validateTableGroup(tgroup)) return false;
         }
 
         return true;
     }
 
     /**
      * Check to ensure that a Docbook tgroup isn't missing an row entries, using number of cols defined for the tgroup.
      *
      * @param entryTbl The DOM entrytbl element to be checked.
      * @return True if the tgroup has the required number of entries, otherwise false.
      */
     public static boolean validateTableGroup(final Element tgroup) {
         assert tgroup != null;
         assert tgroup.getNodeName().equals("tgroup");
 
         final Integer numColumns = Integer.parseInt(tgroup.getAttribute("cols"));
 
         // Check that all the thead, tbody and tfoot elements have the correct number of entries.
         final List<Node> nodes = XMLUtilities.getDirectChildNodes(tgroup, "thead", "tbody", "tfoot");
         for (final Node ele : nodes) {
             // Find all child nodes that are a row
             final NodeList children = ele.getChildNodes();
             for (int i = 0; i < children.getLength(); i++) {
                 final Node node = children.item(i);
                 if (node.getNodeName().equals("row") || node.getNodeName().equals("tr")) {
                     if (!validateTableRow(node, numColumns)) return false;
                 }
             }
         }
 
         return true;
     }
 
     /**
      * Check to ensure that a docbook row has the required number of columns for a table.
      *
      * @param row        The DOM row element to be checked.
      * @param numColumns The number of entry elements that should exist in the row.
      * @return True if the row has the required number of entries, otherwise false.
      */
     public static boolean validateTableRow(final Node row, final int numColumns) {
         assert row != null;
         assert row.getNodeName().equals("row") || row.getNodeName().equals("tr");
 
         if (row.getNodeName().equals("row")) {
             final List<Node> entries = XMLUtilities.getDirectChildNodes(row, "entry");
             final List<Node> entryTbls = XMLUtilities.getDirectChildNodes(row, "entrytbl");
 
             if ((entries.size() + entryTbls.size()) <= numColumns) {
                 for (final Node entryTbl : entryTbls) {
                     if (!validateEntryTbl((Element) entryTbl)) return false;
                 }
                 return true;
             } else {
                 return false;
             }
         } else {
             final List<Node> nodes = XMLUtilities.getDirectChildNodes(row, "td", "th");
 
             return nodes.size() <= numColumns;
         }
     }
 
     /**
      * Check to ensure that a Docbook entrytbl isn't missing an row entries, using number of cols defined for the entrytbl.
      *
      * @param entryTbl The DOM entrytbl element to be checked.
      * @return True if the entryTbl has the required number of entries, otherwise false.
      */
     public static boolean validateEntryTbl(final Element entryTbl) {
         assert entryTbl != null;
         assert entryTbl.getNodeName().equals("entrytbl");
 
         final Integer numColumns = Integer.parseInt(entryTbl.getAttribute("cols"));
 
         // Check that all the thead and tbody elements have the correct number of entries.
         final List<Node> nodes = XMLUtilities.getDirectChildNodes(entryTbl, "thead", "tbody");
         for (final Node ele : nodes) {
             // Find all child nodes that are a row
             final NodeList children = ele.getChildNodes();
             for (int i = 0; i < children.getLength(); i++) {
                 final Node node = children.item(i);
                 if (node.getNodeName().equals("row") || node.getNodeName().equals("tr")) {
                     if (!validateTableRow(node, numColumns)) return false;
                 }
             }
         }
 
         return true;
     }
 
     /**
      * Check the XML Document and it's children for condition
      * statements. If any are found then check if the condition
      * matches the passed condition string. If they don't match
      * then remove the nodes.
      *
      * @param condition The condition regex to be tested against.
      * @param doc       The Document to check for conditional statements.
      */
     public static void processConditions(final String condition, final Document doc) {
         processConditions(condition, doc, "default");
     }
 
     /**
      * Check the XML Document and it's children for condition
      * statements. If any are found then check if the condition
      * matches the passed condition string. If they don't match
      * then remove the nodes.
      *
      * @param condition        The condition regex to be tested against.
      * @param doc              The Document to check for conditional statements.
      * @param defaultCondition The default condition to allow a default block when processing conditions.
      */
     public static void processConditions(final String condition, final Document doc, final String defaultCondition) {
         final Map<Node, List<String>> conditionalNodes = getConditionNodes(doc.getDocumentElement());
 
         // Loop through each condition found and see if it matches
         for (final Map.Entry<Node, List<String>> entry : conditionalNodes.entrySet()) {
             final Node node = entry.getKey();
             final List<String> nodeConditions = entry.getValue();
             boolean matched = false;
 
             // Check to see if the condition matches
             for (final String nodeCondition : nodeConditions) {
                 if (condition != null && nodeCondition.matches(condition)) {
                     matched = true;
                 } else if (condition == null && defaultCondition!= null && nodeCondition.matches(defaultCondition)) {
                     matched = true;
                 }
             }
 
             // If there was no match then remove the node
             if (!matched) {
                 final Node parentNode = node.getParentNode();
                 if (parentNode != null) {
                     parentNode.removeChild(node);
                 }
            } else {
                // Remove the condition attribute so that it can't get processed by something else downstream
                ((Element) node).removeAttribute("condition");
             }
         }
     }
 
     /**
      * Collects any nodes that have the "condition" attribute in the
      * passed node or any of it's children nodes.
      *
      * @param node The node to collect condition elements from.
      * @return A mapping of nodes to their conditions.
      */
     public static Map<Node, List<String>> getConditionNodes(final Node node) {
         final Map<Node, List<String>> conditionalNodes = new HashMap<Node, List<String>>();
         getConditionNodes(node, conditionalNodes);
         return conditionalNodes;
     }
 
     /**
      * Collects any nodes that have the "condition" attribute in the
      * passed node or any of it's children nodes.
      *
      * @param node             The node to collect condition elements from.
      * @param conditionalNodes A mapping of nodes to their conditions
      */
     private static void getConditionNodes(final Node node, final Map<Node, List<String>> conditionalNodes) {
         final NamedNodeMap attributes = node.getAttributes();
         if (attributes != null) {
             final Node attr = attributes.getNamedItem("condition");
 
             if (attr != null) {
                 final String conditionStatement = attr.getNodeValue();
 
                 final String[] conditions = conditionStatement.split("\\s*;\\s*");
 
                 conditionalNodes.put(node, Arrays.asList(conditions));
             }
         }
 
         // Check the child nodes for condition attributes
         final NodeList elements = node.getChildNodes();
         for (int i = 0; i < elements.getLength(); ++i) {
             getConditionNodes(elements.item(i), conditionalNodes);
         }
     }
 }
