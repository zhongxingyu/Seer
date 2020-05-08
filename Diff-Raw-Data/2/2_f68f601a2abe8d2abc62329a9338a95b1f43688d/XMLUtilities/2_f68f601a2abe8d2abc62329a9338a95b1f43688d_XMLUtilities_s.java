 package org.jboss.pressgang.ccms.utils.common;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.dom.DOMSource;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 
 import com.google.code.regexp.Matcher;
 import com.google.code.regexp.Pattern;
 import org.jboss.pressgang.ccms.utils.sort.EntitySubstitutionBoundaryDataBoundaryStartSort;
 import org.jboss.pressgang.ccms.utils.structures.EntitySubstitutionBoundaryData;
 import org.jboss.pressgang.ccms.utils.structures.Pair;
 import org.jboss.pressgang.ccms.utils.structures.StringToNodeCollection;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Entity;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.ls.LSSerializer;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 /**
  * A collection of XML related functions. Note to self: See http://www.gnu.org/s/
  * classpathx/jaxp/apidoc/gnu/xml/dom/ls/DomLSSerializer.html for LSSerializer options
  */
 public class XMLUtilities {
     private static final Logger LOG = LoggerFactory.getLogger(XMLUtilities.class);
     private static final String DOCTYPE_NAMED_GROUP = "Doctype";
     private static final Pattern DOCTYPE_PATTERN = Pattern.compile(
             "^(\\s*<\\?xml.*?\\?>)?\\s*(?<" + DOCTYPE_NAMED_GROUP + "><\\!DOCTYPE\\s+.*?(\\[.*\\]\\s*)?>)",
             java.util.regex.Pattern.MULTILINE | java.util.regex.Pattern.DOTALL);
     private static final String PREAMBLE_NAMED_GROUP = "Preamble";
     private static final Pattern PREAMBLE_PATTERN = Pattern.compile("^\\s*(?<" + PREAMBLE_NAMED_GROUP + "><\\?xml.*?\\?>)",
             java.util.regex.Pattern.MULTILINE | java.util.regex.Pattern.DOTALL);
     /**
      * The Docbook elements that contain translatable text
      */
     public static final ArrayList<String> TRANSLATABLE_ELEMENTS = CollectionUtilities.toArrayList(
             new String[]{"ackno", "bridgehead", "caption", "conftitle", "contrib", "entry", "firstname", "glossterm", "indexterm",
                     "jobtitle", "keyword", "label", "lastname", "lineannotation", "lotentry", "member", "orgdiv", "orgname", "othername",
                     "para", "phrase", "productname", "refclass", "refdescriptor", "refentrytitle", "refmiscinfo", "refname",
                     "refpurpose", "releaseinfo", "revremark", "screeninfo", "secondaryie", "seealsoie", "seeie", "seg", "segtitle",
                     "simpara", "subtitle", "surname", "term", "termdef", "tertiaryie", "title", "titleabbrev", "screen",
                     "programlisting", "literallayout"});
     /**
      * The Docbook elements that contain translatable text, and need to be kept inline
      */
     public static final ArrayList<String> INLINE_ELEMENTS = CollectionUtilities.toArrayList(
             new String[]{"footnote", "citerefentry", "indexterm", "productname", "phrase"});
     /**
      * The Docbook elements that should not have their text reformatted
      */
     public static final ArrayList<String> VERBATIM_ELEMENTS = CollectionUtilities.toArrayList(
             new String[]{"screen", "programlisting", "literallayout"});
     /**
      * The Docbook elements that should be translated only if their parent is not listed in TRANSLATABLE_ELEMENTS
      */
     public static final ArrayList<String> TRANSLATABLE_IF_STANDALONE_ELEMENTS = CollectionUtilities.toArrayList(
             new String[]{"indexterm", "productname", "phrase"});
     public static final String ENCODING_START = "encoding=\"";
     public static final String START_CDATA = "<![CDATA[";
     public static final String END_CDATA_RE = "\\]\\]>";
     public static final String END_CDATA_REPLACE = "]]&gt;";
     public static final String XML_ENTITY_NAMED_GROUP = "name";
     public static final String XML_ENTITY_RE = "\\&(?<" + XML_ENTITY_NAMED_GROUP + ">[#\\w\\d]*?);";
     public static final String DOCTYPE_START = "<!DOCTYPE";
     public static final String DOCTYPE_END = ">";
     public static final String ENTITY_START = "<!ENTITY";
     public static final String ENTITY_END = ">";
     public static final String PREAMBLE_START = "<?xml";
     public static final String PREAMBLE_END = ">";
     public static final String TRAILING_WHITESPACE_RE = "^(?<content>.*?)\\s+$";
     public static final String TRAILING_WHITESPACE_SIMPLE_RE = ".*?\\s+$";
     public static final String PRECEEDING_WHITESPACE_SIMPLE_RE = "^\\s+.*";
 
    public static final Pattern XML_ENTITY_PATTERN = Pattern.compile("\\&(?<" + XML_ENTITY_NAMED_GROUP + ">[#\\w\\d]*?);");
 
     public static final Pattern TRAILING_WHITESPACE_RE_PATTERN = Pattern.compile(TRAILING_WHITESPACE_RE,
             java.util.regex.Pattern.MULTILINE | java.util.regex.Pattern.DOTALL);
     public static final Pattern TRAILING_WHITESPACE_SIMPLE_RE_PATTERN = Pattern.compile(TRAILING_WHITESPACE_SIMPLE_RE,
             java.util.regex.Pattern.MULTILINE | java.util.regex.Pattern.DOTALL);
     public static final Pattern PRECEEDING_WHITESPACE_SIMPLE_RE_PATTERN = Pattern.compile(PRECEEDING_WHITESPACE_SIMPLE_RE,
             java.util.regex.Pattern.MULTILINE | java.util.regex.Pattern.DOTALL);
 
     public static String findEncoding(final String xml) {
         // Find the preamble first so we can dissect it to find the encoding.
         final String preamble = findPreamble(xml);
         if (preamble != null) {
             final int encodingIndexStart = preamble.indexOf(ENCODING_START);
             final int firstLineBreak = preamble.indexOf("\n");
 
             // make sure we found the encoding attribute
             if (encodingIndexStart != -1) {
                 final int encodingIndexEnd = preamble.indexOf("\"", encodingIndexStart + ENCODING_START.length());
 
                 // make sure the encoding attribute was found before the first
                 // line break
                 if (firstLineBreak == -1 || encodingIndexStart < firstLineBreak) {
                     // make sure we found the end of the attribute
                     if (encodingIndexEnd != -1) {
                         return preamble.substring(encodingIndexStart + ENCODING_START.length(), encodingIndexEnd);
                     }
                 }
             }
         }
 
         return null;
     }
 
     public static String findDocumentType(final String xml) {
         final Matcher matcher = DOCTYPE_PATTERN.matcher(xml);
         if (matcher.find()) {
             return matcher.group(DOCTYPE_NAMED_GROUP);
         } else {
             return null;
         }
     }
 
     public static String findPreamble(final String xml) {
         final Matcher matcher = PREAMBLE_PATTERN.matcher(xml);
         if (matcher.find()) {
             return matcher.group(PREAMBLE_NAMED_GROUP);
         } else {
             return null;
         }
     }
 
     /**
      * Removes all of the child nodes from a parent node.
      */
     public static void emptyNode(final Node parent) {
         final NodeList childNodes = parent.getChildNodes();
         for (int i = childNodes.getLength() - 1; i >= 0; i--) {
             final Node childNode = childNodes.item(i);
             childNode.getParentNode().removeChild(childNode);
         }
     }
 
     /**
      * Clones a document object.
      *
      * @param doc The document to be cloned.
      * @return The new document object that contains the same data as the original document.
      * @throws TransformerException Thrown if the document can't be
      */
     public static Document cloneDocument(final Document doc) throws TransformerException {
         final Node rootNode = doc.getDocumentElement();
 
         // Copy the doctype and xml version type data
         final TransformerFactory tfactory = TransformerFactory.newInstance();
         final Transformer tx = tfactory.newTransformer();
         final DOMSource source = new DOMSource(doc);
         final DOMResult result = new DOMResult();
         tx.transform(source, result);
 
         // Copy the actual content into the new document
         final Document copy = (Document) result.getNode();
         copy.removeChild(copy.getDocumentElement());
         final Node copyRootNode = copy.importNode(rootNode, true);
         copy.appendChild(copyRootNode);
 
         return copy;
     }
 
     /**
      * This function will return a map that contains entity names as keys, and random integer strings as values. The values are
      * guaranteed not to have appeared in the original xml.
      *
      * @param xml The xml to generate the replacements for
      * @return a map of entity names to unique random strings
      */
     private static Map<String, String> calculateEntityReplacements(final String xml) {
         final Map<String, String> retValue = new HashMap<String, String>();
 
         final Random randomGenerator = new Random();
 
         /* find any matches */
         final Matcher injectionSequencematcher = XML_ENTITY_PATTERN.matcher(xml);
 
         /* loop over the regular expression matches */
         while (injectionSequencematcher.find()) {
             final String entityName = injectionSequencematcher.group(XML_ENTITY_NAMED_GROUP);
 
             if (!retValue.containsKey(entityName)) {
                 String randomReplacement;
                 do {
                     randomReplacement = "[" + randomGenerator.nextInt() + "]";
                 } while (xml.indexOf(randomReplacement) != -1);
 
                 retValue.put(entityName, randomReplacement);
             }
         }
 
         return retValue;
     }
 
     /**
      * This function takes the Map generated by the calculateEntityReplacements function, and uses those values to replace any
      * entities in the XML string with their unique random integer replacements. The end results is an XML string that contains
      * no entities, but contains identifiable strings that can be used to replace those entities at a later point.
      *
      * @param replacements The Map generated by the calculateEntityReplacements function
      * @param xml          The XML string to modify
      * @return The modified XML
      */
     private static String replaceEntities(final Map<String, String> replacements, final String xml) {
         String retValue = xml;
         for (final Entry<String, String> entry : replacements.entrySet())
             retValue = retValue.replaceAll("\\&" + entry.getKey() + ";", entry.getValue());
         return retValue;
     }
 
     /**
      * This function takes a parsed Document, along with the Map generated by the calculateEntityReplacements function, and
      * restores all the entities.
      *
      * @param replacements The Map generated by the calculateEntityReplacements function
      * @param node         The node to modify
      */
     private static void restoreEntities(final Map<String, String> replacements, final Node node) {
         if (node == null || replacements == null || replacements.size() == 0) return;
 
         /* make the substitutions for all children nodes */
         final NodeList nodeList = node.getChildNodes();
         for (int i = 0; i < nodeList.getLength(); i++)
             restoreEntities(replacements, nodeList.item(i));
 
         /* make the substitutions for all attributes */
         final NamedNodeMap attrList = node.getAttributes();
         if (attrList != null) {
             for (int i = 0; i < attrList.getLength(); i++)
                 restoreEntities(replacements, attrList.item(i));
         }
 
         /* cdata sections just use a straight text replace */
         if (node.getNodeType() == Node.CDATA_SECTION_NODE || node.getNodeType() == Node.COMMENT_NODE) {
             for (final Entry<String, String> entityReplacement : replacements.entrySet()) {
                 final String entity = "&" + entityReplacement.getKey() + ";";
                 final String markerAsRE = entityReplacement.getValue().replace("[", "\\[").replace("]", "\\]");
                 final String textContent = node.getTextContent();
                 final String fixedTextContent = textContent.replaceAll(markerAsRE, entity);
                 node.setTextContent(fixedTextContent);
             }
         } else if (node.getNodeType() == Node.TEXT_NODE) {
             /* The list of substitution string boundaries */
             final List<EntitySubstitutionBoundaryData> boundaries = new ArrayList<EntitySubstitutionBoundaryData>();
 
             /*
              * find the start and end indexes of all the substitutions in this text node
              */
             for (final Entry<String, String> entityReplacement : replacements.entrySet()) {
                 final String entityName = entityReplacement.getKey();
                 final String entityPlaceholder = entityReplacement.getValue();
 
                 /* The length of the placeholder string */
                 final int entityPlaceholderLength = entityPlaceholder.length();
                 /* The text in this node, with the substitutions */
                 final String originalText = node.getTextContent();
 
                 int startIndex = 0;
                 while ((startIndex = originalText.indexOf(entityPlaceholder, startIndex)) != -1) {
                     boundaries.add(new EntitySubstitutionBoundaryData(entityName, entityPlaceholder,
                             new Pair<Integer, Integer>(startIndex, startIndex + entityPlaceholderLength - 1)));
                     startIndex += entityPlaceholderLength;
                 }
             }
 
             /*
              * if there are no boundaries, there is no need to do any substitutions
              */
             if (boundaries.size() != 0) {
                 /* Sort based on the start of the boundaries */
                 Collections.sort(boundaries, new EntitySubstitutionBoundaryDataBoundaryStartSort());
 
                 /* get the text content of the text node */
                 final String originalText = node.getTextContent();
 
                 /* the parent of this node holds only this text node. */
                 final Node parentNode = node.getParentNode();
 
                 /*
                  * loop through all the boundaries that define the position of the substitutions, and replace them with entity
                  * reference nodes.
                  * 
                  * this involves adding a new sequence of text and entity reference nodes before the existing text node, and
                  * then removing the existing text node.
                  */
                 for (int i = 0; i < boundaries.size(); ++i) {
                     final EntitySubstitutionBoundaryData boundary = boundaries.get(i);
                     final EntitySubstitutionBoundaryData lastBoundary = i != 0 ? boundaries.get(i - 1) : null;
 
                     /*
                      * The entity reference node.
                      * 
                      * Normal Elements can contain EntityReference nodes, however attributes appear to be unable to handle
                      * EntityReference nodes as children, so just convert the EntityReference to a normal text node in the
                      * Attribute.
                      */
                     final Node entityNode;
                     if (parentNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                         entityNode = parentNode.getOwnerDocument().createTextNode("&" + boundary.getEntityName() + ";");
                     } else {
                         entityNode = parentNode.getOwnerDocument().createEntityReference(boundary.getEntityName());
                     }
 
                     /* the first substitution where text proceeds it */
                     if (i == 0) {
                         if (boundary.getBoundary().getFirst() != 0) {
                             final Node textNode = parentNode.getOwnerDocument().createTextNode(
                                     originalText.substring(0, boundary.getBoundary().getFirst()));
                             parentNode.insertBefore(textNode, node);
                         }
 
                         /* append an entity node after the initial text node */
                         parentNode.insertBefore(entityNode, node);
                     } else {
                         /*
                          * there is a gap between the last boundary and this boundary
                          */
 
                         if (lastBoundary.getBoundary().getSecond() + 1 != boundary.getBoundary().getFirst()) {
                             final Node textNode = parentNode.getOwnerDocument().createTextNode(
                                     originalText.substring(lastBoundary.getBoundary().getSecond() + 1, boundary.getBoundary().getFirst()));
                             parentNode.insertBefore(textNode, node);
                         }
                     }
 
                     /*
                      * append an entity node after the text node following the last substitution
                      */
                     parentNode.insertBefore(entityNode, node);
 
                     /* the last substitution where text follows it */
                     if (i == boundaries.size() - 1) {
                         /* append an entity node before the last text node */
                         parentNode.insertBefore(entityNode, node);
 
                         if (boundary.getBoundary().getSecond() != originalText.length() - 1) {
                             final Node textNode = parentNode.getOwnerDocument().createTextNode(
                                     originalText.substring(boundary.getBoundary().getSecond() + 1));
                             parentNode.insertBefore(textNode, node);
                         }
                     }
                 }
 
                 /* finally, remove the existing text node */
                 parentNode.removeChild(node);
             }
         }
     }
 
     /**
      * @param xml The XML to be converted
      * @return A Document converted from the supplied XML, or null if the supplied XML was invalid
      * @throws SAXException
      */
     public static Document convertStringToDocument(final String xml) throws SAXException {
         return convertStringToDocument(xml, true);
     }
 
     /**
      * @param xml The XML to be converted
      * @param preserveEntities Whether or not entities should be preserved.
      * @return A Document converted from the supplied XML, or null if the supplied XML was invalid
      * @throws SAXException
      */
     public static Document convertStringToDocument(final String xml, final boolean preserveEntities) throws SAXException {
         if (xml == null) return null;
 
         try {
             // find the encoding, defaulting to UTF-8
             String encoding = findEncoding(xml);
             if (encoding == null) encoding = "UTF-8";
 
             /*
              * Xerces does not seem to have any way of simply importing entities "as is". It will try to expand them, which we
              * don't want. As a work around the calculateEntityReplacements() function will map entity names to random
              * substitution markers. These markers are parsed as plain text (they are in the format "[random_integer]"). The
              * replaceEntities() function will then replace the entity definitions in the source XML text with these
              * substitution markers.
              * 
              * At this point the XML has no entities, and so Xerces will parse the string without trying to expand the entities.
              * 
              * Once we have a Document object, we run the restoreEntities() function, which replaces the substitution markers
              * with entity reference nodes. Xerces does not try to expand entities when serializing a Document object to a
              * string, nor does it try to extend entity reference nodes when they are added. In this way we can parse any XML
              * and retain the entities without having to link to any DTDs or implement any EntityResolvers.
              */
             final Map<String, String> replacements = calculateEntityReplacements(xml);
             final String fixedXML = preserveEntities? replaceEntities(replacements, xml) : xml;
 
             final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
             // This was causing an exception... See below with the EntityResolver for an alternative.
             // builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
             // this is the default, but set it anyway
             // builderFactory.setValidating(false);
             builderFactory.setNamespaceAware(true);
 
             final DocumentBuilder builder = builderFactory.newDocumentBuilder();
 
             // disable the resolution of any entities. see http://stackoverflow.com/a/155330/157605
             builder.setEntityResolver(new EntityResolver() {
                 public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                     // Return an empty source so that File Not Found errors aren't generated.
                     return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
                 }
             });
 
             // Create an error handler that does nothing, so that the default handler (which only prints to stderr) isn't used.
             builder.setErrorHandler(new ErrorHandler() {
                 @Override
                 public void warning(SAXParseException e) throws SAXException {
                     // Do nothing
                 }
 
                 @Override
                 public void error(SAXParseException e) throws SAXException {
                     // Do nothing
                 }
 
                 @Override
                 public void fatalError(SAXParseException e) throws SAXException {
                     // Do nothing
                 }
             });
 
             final Document document = builder.parse(new org.xml.sax.InputSource(new ByteArrayInputStream(fixedXML.getBytes(encoding))));
 
             if (preserveEntities) {
                 restoreEntities(replacements, document.getDocumentElement());
             }
 
             return document;
         } catch (SAXException ex) {
             throw ex;
         } catch (ParserConfigurationException ex) {
             throw new RuntimeException(ex);
         } catch (UnsupportedEncodingException ex) {
             throw new RuntimeException(ex);
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     /**
      * Converts a Document to a String
      *
      * @param doc The Document to be converted
      * @return The String representation of the Document
      */
     public static String convertDocumentToString(final Document doc, final String encoding) {
         String retValue = convertDocumentToString(doc);
 
         /*
          * The encoding used is the encoding of the DOMString type, i.e. UTF-16
          * (http://www.w3.org/TR/DOM-Level-3-LS/load-save.html#LS-LSSerializer- writeToString). However, we need to use UTF-8
          * (https://bugzilla.redhat.com/show_bug.cgi?id=735904). So do a simple text replacement.
          */
 
         final String docEncoding = findEncoding(retValue);
         if (docEncoding != null) retValue = retValue.replace(docEncoding, encoding);
 
         return retValue;
     }
 
     /**
      * Convert an XML document to a string.
      *
      * @param doc       The Document to be converted
      * @param encoding  The encoding of the XML
      * @param entityDec Any additional XML entity declarations
      * @return The String representation of the XML Document
      */
     public static String convertDocumentToString(final Document doc, final String encoding, final String entityDec) {
         String retValue = convertDocumentToString(doc, encoding);
 
         final String docEncoding = findPreamble(retValue);
         if (docEncoding != null) retValue = retValue.replace(docEncoding, docEncoding + "\n" + entityDec);
 
         return retValue;
     }
 
     /**
      * Converts a Document to a String
      *
      * @param doc The Document to be converted
      * @return The String representation of the Document
      */
     public static String convertDocumentToString(final Document doc) {
         return convertDocumentToString(doc, false);
     }
 
     /**
      * Converts a Document to a String
      *
      * @param doc         The Document to be converted
      * @param prettyPrint If the xml should be formatted when being converted.
      * @return The String representation of the Document
      */
     public static String convertDocumentToString(final Document doc, final boolean prettyPrint) {
         final DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
         final LSSerializer lsSerializer = domImplementation.createLSSerializer();
         if (prettyPrint) {
             lsSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
         }
         return lsSerializer.writeToString(doc);
     }
 
     private static void appendIndent(final StringBuffer stringBuffer, final boolean tabIndent, final int indentLevel,
             final int indentCount) {
         final char indent = tabIndent ? '\t' : ' ';
 
         final int totalIndentCount = indentLevel * indentCount;
 
         stringBuffer.append("\n");
         for (int i = 0; i < totalIndentCount; ++i)
             stringBuffer.append(indent);
     }
 
     public static String convertNodeToString(final Node startNode, final boolean includeElementName) {
         return convertNodeToString(startNode, includeElementName, true, false, new ArrayList<String>(), new ArrayList<String>(),
                 new ArrayList<String>(), true, 0, 0);
     }
 
     public static String convertNodeToString(final Node startNode, final List<String> verbatimElements, final List<String> inlineElements,
             final List<String> contentsInlineElements, final boolean tabIndent) {
         return convertNodeToString(startNode, true, false, false, verbatimElements, inlineElements, contentsInlineElements, tabIndent, 1,
                 0);
     }
 
     /**
      * Converts a Node to a String.
      *
      * @param node               The Node to be converted
      * @param includeElementName true if the string should include the name of the node, or false if it is just to include the
      *                           contents of the node
      * @return The String representation of the Node
      */
     public static String convertNodeToString(final Node startNode, final boolean includeElementName, final boolean verbatim,
             final boolean inline, final List<String> verbatimElements, final List<String> inlineElements,
             final List<String> contentsInlineElements, final boolean tabIndent, final int indentCount, final int indentLevel) {
         /* Find out if this node is a document */
         final Node node = startNode instanceof Document ? ((Document) startNode).getDocumentElement() : startNode;
 
         final String nodeName = node.getNodeName();
         final short nodeType = node.getNodeType();
         final StringBuffer stringBuffer = new StringBuffer();
 
         /*
          * Find out if the previous node was a comment (excluding any empty text nodes). Also find out if this is the first node
          * in the parent.
          */
         boolean previousNodeWasComment = false;
         Node previousNode = startNode.getPreviousSibling();
         while (previousNode != null) {
             if ((previousNode.getNodeType() == Node.TEXT_NODE && previousNode.getNodeValue().trim().isEmpty())) {
                 previousNode = previousNode.getPreviousSibling();
                 continue;
             }
 
             if (previousNode.getNodeType() == Node.COMMENT_NODE) {
                 previousNodeWasComment = true;
                 break;
             }
 
             break;
         }
 
         /* Find out of this node is the document root node */
         final boolean documentRoot = node.getOwnerDocument().getDocumentElement() == node;
 
         final boolean firstNode = previousNode == null;
 
         if (Node.CDATA_SECTION_NODE == nodeType) {
             final StringBuffer retValue = new StringBuffer();
 
             if (!verbatim && !inline) appendIndent(retValue, tabIndent, indentLevel, indentCount);
 
             if (includeElementName) retValue.append("<![CDATA[");
             retValue.append(node.getNodeValue());
             if (includeElementName) retValue.append("]]>");
 
             return retValue.toString();
         }
 
         if (Node.COMMENT_NODE == nodeType) {
             final StringBuffer retValue = new StringBuffer();
 
             if (!verbatim && !inline) {
                 // If the previous node is a text node that isn't just whitespace then the comment must follow on, so don't add an indent
                 if (previousNode != null && previousNode instanceof Text) {
                     if (previousNode.getTextContent().trim().isEmpty()) {
                         appendIndent(retValue, tabIndent, indentLevel, indentCount);
                     }
                 } else {
                     appendIndent(retValue, tabIndent, indentLevel, indentCount);
                 }
             }
 
             if (includeElementName) retValue.append("<!--");
             retValue.append(node.getNodeValue());
             if (includeElementName) retValue.append("-->");
 
             return retValue.toString();
         }
 
         if (Node.TEXT_NODE == nodeType) {
             if (!verbatim) {
                 String trimmedNodeValue = cleanText(node.getNodeValue());
 
                 if (!trimmedNodeValue.trim().isEmpty()) {
                     final StringBuffer retValue = new StringBuffer();
 
                     /*
                      * if this is the first text node, remove all preceeding whitespace, and then add the indent
                      */
                     final boolean firstNotInlinedTextNode = !inline && firstNode;
                     if (firstNotInlinedTextNode) {
                         appendIndent(retValue, tabIndent, indentLevel, indentCount);
                     }
 
                     // Remove any white space at the beginning and end of the text, save for one space
                     final boolean startedWithWhiteSpace = StringUtilities.startsWithWhitespace(trimmedNodeValue);
                     final boolean endedWithWhitespace = StringUtilities.endsWithWhitespace(trimmedNodeValue);
 
                     while (StringUtilities.startsWithWhitespace(trimmedNodeValue)) {
                         trimmedNodeValue = trimmedNodeValue.substring(1);
                     }
 
                     while (StringUtilities.endsWithWhitespace(trimmedNodeValue)) {
                         trimmedNodeValue = trimmedNodeValue.substring(0, trimmedNodeValue.length() - 1);
                     }
 
                     // Only add whitespace if the node is in an inline element or isn't the first node
                     if (startedWithWhiteSpace && (inline || !firstNode)) trimmedNodeValue = " " + trimmedNodeValue;
 
                     // Only add whitespace if the node is in an inline element or isn't the last node
                     if (endedWithWhitespace && (node.getNextSibling() != null || inline)) trimmedNodeValue += " ";
 
                     retValue.append(trimmedNodeValue);
 
                     return retValue.toString();
                 }
                 /*
                  * Allow for spaces between nodes. i.e. <literal>Test</literal> <literal>Test2</literal>
                  */
                 else if (node.getNodeValue() != null && node.getNodeValue().matches("^[ ]+$") && node.getNextSibling() != null) {
                     return " ";
                 }
 
                 return "";
             } else {
                 return node.getNodeValue();
             }
         }
 
         if (Node.ENTITY_REFERENCE_NODE == nodeType) {
             final StringBuffer retValue = new StringBuffer();
 
             // if this is the first node, then add the indent
             if (!inline && !verbatim && firstNode) {
                 appendIndent(retValue, tabIndent, indentLevel, indentCount);
             }
 
             if (includeElementName) retValue.append("&");
             retValue.append(node.getNodeName());
             if (includeElementName) retValue.append(";");
 
             return retValue.toString();
         }
 
         /* open the tag */
         if (includeElementName) {
 
             if (!verbatim && !documentRoot && ((!inline && !inlineElements.contains(
                     nodeName)) || previousNodeWasComment || (firstNode && !inline)))
                 appendIndent(stringBuffer, tabIndent, indentLevel, indentCount);
 
             stringBuffer.append('<').append(nodeName);
 
             /* add attributes */
             final NamedNodeMap attrs = node.getAttributes();
             if (attrs != null) {
                 for (int i = 0; i < attrs.getLength(); i++) {
                     final Node attr = attrs.item(i);
                     stringBuffer.append(' ').append(attr.getNodeName()).append("=\"").append(attr.getNodeValue()).append("\"");
                 }
             }
         }
 
         /* deal with children */
         final NodeList children = node.getChildNodes();
         if (children.getLength() == 0) {
             final String nodeTextContent = node.getTextContent();
             if (nodeTextContent.length() == 0) {
                 if (includeElementName) stringBuffer.append("/>");
             } else {
                 stringBuffer.append(nodeTextContent);
 
                 /* indent */
                 if (!verbatim && !inline && !inlineElements.contains(nodeName))
                     appendIndent(stringBuffer, tabIndent, indentLevel, indentCount);
 
                 /* close that tag */
                 if (includeElementName) stringBuffer.append("</").append(nodeName).append('>');
             }
         } else {
             if (includeElementName) stringBuffer.append(">");
 
             final boolean inlineMyChildren = inline || inlineElements.contains(nodeName) || contentsInlineElements.contains(nodeName);
             final boolean verbatimMyChildren = verbatim || verbatimElements.contains(nodeName);
 
             for (int i = 0; i < children.getLength(); ++i) {
                 final String childToString = convertNodeToString(children.item(i), true, verbatimMyChildren, inlineMyChildren,
                         verbatimElements, inlineElements, contentsInlineElements, tabIndent, indentCount, indentLevel + 1);
                 if (childToString.length() != 0) stringBuffer.append(childToString);
             }
 
             /* close that tag */
             if (includeElementName) {
                 /* indent */
                 if (!verbatimMyChildren && !inlineMyChildren) appendIndent(stringBuffer, tabIndent, indentLevel, indentCount);
 
                 stringBuffer.append("</").append(nodeName).append('>');
             }
         }
 
         return stringBuffer.toString();
     }
 
     /**
      * Scans a node and all of its children for nodes of a particular type.
      *
      * @param parent    The parent node to search from.
      * @param nodeNames A single node name or list of node names to search for
      * @return A List of all the nodes found matching the nodeName(s) under the parent
      */
     public static List<Node> getChildNodes(final Node parent, final String... nodeNames) {
         return getChildNodes(parent, true, nodeNames);
     }
 
     public static List<Node> getComments(final Node parent) {
         return getChildNodes(parent, "#comment");
     }
 
     /**
      * Scans a node for directly related child nodes of a particular type. This method will not scan for nodes that aren't a child of the
      * parent node.
      *
      * @param parent    The parent node to search from.
      * @param nodeNames A single node name or list of node names to search for
      * @return A List of all the nodes found matching the nodeName(s) under the parent
      */
     public static List<Node> getDirectChildNodes(final Node parent, final String... nodeNames) {
         return getChildNodes(parent, false, nodeNames);
     }
 
     /**
      * Scans a node and all of its children for nodes of a particular type.
      *
      * @param parent          The parent node to search from.
      * @param recursiveSearch If the child nodes should be recursively searched.
      * @param nodeNames       A single node name or list of node names to search for
      * @return a List of all the nodes found matching the nodeName under the parent
      */
     protected static List<Node> getChildNodes(final Node parent, boolean recursiveSearch, final String... nodeNames) {
         final List<Node> nodes = new ArrayList<Node>();
         final NodeList children = parent.getChildNodes();
         for (int i = 0; i < children.getLength(); ++i) {
             final Node child = children.item(i);
 
             for (final String nodeName : nodeNames) {
                 if (child.getNodeName().equals(nodeName)) {
                     nodes.add(child);
                 }
                 if (recursiveSearch) {
                     nodes.addAll(getChildNodes(child, true, nodeName));
                 }
             }
         }
         return nodes;
     }
 
     /**
      * Add/Set the DOCTYPE for some XML content.
      *
      * @param xml             The XML to add or set the DOCTYPE for.
      * @param rootElementName The root Element Name for the DOCTYPE.
      * @return The XML with the DOCTYPE added.
      */
     public static String addDoctype(final String xml, final String rootElementName) {
         return addDoctype(xml, rootElementName, null);
     }
 
     /**
      * Add/Set the DOCTYPE for some XML content.
      *
      * @param xml             The XML to add or set the DOCTYPE for.
      * @param rootElementName The root Element Name for the DOCTYPE.
      * @param entityFileName  The file name for any external entities that should be included.
      * @return The XML with the DOCTYPE added.
      */
     public static String addDoctype(final String xml, final String rootElementName, final String entityFileName) {
         final String preamble = findPreamble(xml);
         final String docType = findDocumentType(xml);
         final String fixedPreamble = preamble == null ? "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" : preamble + "\n";
 
         // Remove any current doctype declarations
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
 
         // Add the local entity file
         if (entityFileName != null) {
             retValue.append(" [\n");
             retValue.append("<!ENTITY % BOOK_ENTITIES SYSTEM \"" + entityFileName + "\">\n");
             retValue.append("%BOOK_ENTITIES;\n");
             retValue.append("]");
         }
 
         retValue.append(">\n");
         retValue.append(fixedXML);
 
         return retValue.toString();
     }
 
     /**
      * Add/Set the PUBLIC DOCTYPE for some XML content.
      *
      * @param xml             The XML to add or set the DOCTYPE for.
      * @param publicName      The PUBLIC name for the DOCTYPE.
      * @param publicLocation  The PUBLIC location/url for the DOCTYPE.
      * @param rootElementName The root Element Name for the DOCTYPE.
      * @return The XML with the DOCTYPE added.
      */
     public static String addPublicDoctype(final String xml, final String publicName, final String publicLocation,
             final String rootElementName) {
         return addPublicDoctype(xml, publicName, publicLocation, null, rootElementName);
     }
 
     /**
      * Add/Set the PUBLIC DOCTYPE for some XML content.
      *
      * @param xml             The XML to add or set the DOCTYPE for.
      * @param publicName      The PUBLIC name for the DOCTYPE.
      * @param publicLocation  The PUBLIC location/url for the DOCTYPE.
      * @param entityFileName  The file name for any external entities that should be included.
      * @param rootElementName The root Element Name for the DOCTYPE.
      * @return The XML with the DOCTYPE added.
      */
     public static String addPublicDoctype(final String xml, final String publicName, final String publicLocation,
             final String entityFileName, final String rootElementName) {
         final String preamble = findPreamble(xml);
         final String docType = findDocumentType(xml);
         final String fixedPreamble = preamble == null ? "<?xml version='1.0' encoding='UTF-8' ?>\n" : preamble + "\n";
 
         // Remove any current doctype declarations
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
 
         retValue.append(">\n");
         retValue.append(fixedXML);
 
         return retValue.toString();
     }
 
     /**
      * Get the Translatable Strings from an XML Document. This method will return of Translation strings to XML DOM nodes within
      * the XML Document. <br />
      * <br />
      * Note: This function has a flaw when breaking up strings if the Child Nodes contain translatable elements.
      *
      * @param xml             The XML to get the translatable strings from.
      * @param allowDuplicates If duplicate translation strings should be created in the returned list.
      * @return A list of StringToNodeCollection objects containing the translation strings and nodes.
      */
     @Deprecated
     public static List<StringToNodeCollection> getTranslatableStringsV1(final Document xml, final boolean allowDuplicates) {
         if (xml == null) return null;
 
         final List<StringToNodeCollection> retValue = new ArrayList<StringToNodeCollection>();
 
         final NodeList nodes = xml.getDocumentElement().getChildNodes();
         for (int i = 0; i < nodes.getLength(); ++i) {
             final Node node = nodes.item(i);
             getTranslatableStringsFromNodeV1(node, retValue, allowDuplicates, new XMLProperties());
         }
 
         return retValue;
     }
 
     /**
      * Get the Translatable Strings from an XML Document. This method will return of Translation strings to XML DOM nodes within
      * the XML Document.
      *
      * @param xml             The XML to get the translatable strings from.
      * @param allowDuplicates If duplicate translation strings should be created in the returned list.
      * @return A list of StringToNodeCollection objects containing the translation strings and nodes.
      */
     public static List<StringToNodeCollection> getTranslatableStringsV2(final Document xml, final boolean allowDuplicates) {
         if (xml == null) return null;
 
         final List<StringToNodeCollection> retValue = new ArrayList<StringToNodeCollection>();
 
         final NodeList nodes = xml.getDocumentElement().getChildNodes();
         for (int i = 0; i < nodes.getLength(); ++i) {
             final Node node = nodes.item(i);
             getTranslatableStringsFromNodeV2(node, retValue, allowDuplicates, new XMLProperties());
         }
 
         return retValue;
     }
 
     /**
      * Check if a node has child translatable elements.
      *
      * @param node The node to check for child translatable elements.
      * @return True if the node has translatable child Elements.
      */
     @Deprecated
     private static boolean doesElementContainTranslatableContentV1(final Node node) {
         final NodeList children = node.getChildNodes();
         if (children != null) {
             /* check to see if any of the children are translatable nodes */
             for (int j = 0; j < children.getLength(); ++j) {
                 final Node child = children.item(j);
                 final String childName = child.getNodeName();
 
                 /* this child node is itself translatable, so return true */
                 if (TRANSLATABLE_ELEMENTS.contains(childName)) return true;
             }
 
             /*
              * now check to see if any of the child have children that are translatable
              */
             for (int j = 0; j < children.getLength(); ++j) {
                 final Node child = children.item(j);
                 final NodeList grandChildren = child.getChildNodes();
                 for (int k = 0; k < grandChildren.getLength(); ++k) {
                     final Node grandChild = grandChildren.item(k);
                     final boolean result = doesElementContainTranslatableContentV1(grandChild);
                     if (result) return true;
                 }
             }
         }
 
         return false;
     }
 
     /**
      * Check if a node has child translatable elements.
      *
      * @param node The node to check for child translatable elements.
      * @return True if the node has translatable child Elements.
      */
     private static boolean doesElementContainTranslatableContentV2(final Node node) {
         final NodeList children = node.getChildNodes();
         if (children != null) {
             // check to see if any of the children are translatable nodes
             for (int j = 0; j < children.getLength(); ++j) {
                 final Node child = children.item(j);
                 final String childName = child.getNodeName();
 
                 if (TRANSLATABLE_ELEMENTS.contains(childName)) {
                     // This child node is itself translatable, so return true
                     return true;
                 } else if (doesElementContainTranslatableContentV2(child)) {
                     // check if this child contains translatable nodes
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     /**
      * Get the Translatable String to Node collections from an XML DOM Node.
      *
      * @param node               The node to get the translatable elements from.
      * @param translationStrings The list of translation StringToNodeCollection objects to add to.
      * @param allowDuplicates    If duplicate translation strings should be created in the translationStrings list.
      * @param props              A set of XML Properties for the Node.
      */
     @Deprecated
     private static void getTranslatableStringsFromNodeV1(final Node node, final List<StringToNodeCollection> translationStrings,
             final boolean allowDuplicates, final XMLProperties props) {
         if (node == null || translationStrings == null) return;
 
         XMLProperties xmlProperties = new XMLProperties(props);
 
         final String nodeName = node.getNodeName();
         final String nodeParentName = node.getParentNode() != null ? node.getParentNode().getNodeName() : null;
 
         final boolean translatableElement = TRANSLATABLE_ELEMENTS.contains(nodeName);
         final boolean standaloneElement = TRANSLATABLE_IF_STANDALONE_ELEMENTS.contains(nodeName);
         final boolean translatableParentElement = TRANSLATABLE_ELEMENTS.contains(nodeParentName);
         if (!xmlProperties.isInline() && INLINE_ELEMENTS.contains(nodeName)) xmlProperties.setInline(true);
         if (!xmlProperties.isVerbatim() && VERBATIM_ELEMENTS.contains(nodeName)) xmlProperties.setVerbatim(true);
 
         /*
          * this element has translatable strings if:
          * 
          * 1. a translatableElement
          * 
          * OR
          * 
          * 2. a standaloneElement without a translatableParentElement
          * 
          * 3. not a standaloneElement and not an inlineElement
          */
 
         if ((translatableElement && ((standaloneElement && !translatableParentElement) || (!standaloneElement && !xmlProperties.isInline
                 ())))) {
             final NodeList children = node.getChildNodes();
             final boolean hasChildren = children == null || children.getLength() != 0;
 
             /* dump the node if it has no children */
             if (!hasChildren) {
                 final String nodeText = convertNodeToString(node, false);
                 final String cleanedNodeText = cleanTranslationText(nodeText, true, true);
 
                 if (xmlProperties.isVerbatim()) {
                     addTranslationToNodeDetailsToCollection(nodeText, node, allowDuplicates, translationStrings);
                 } else if (!cleanedNodeText.isEmpty()) {
                     addTranslationToNodeDetailsToCollection(cleanedNodeText, node, allowDuplicates, translationStrings);
                 }
 
             }
             /*
              * dump all child nodes until we hit one that itself contains a translatable element. in effect the translation
              * strings can contain up to one level of xml elements.
              */
             else {
                 ArrayList<Node> nodes = new ArrayList<Node>();
                 String translatableString = "";
 
                 final int childrenLength = children.getLength();
                 for (int i = 0; i < childrenLength; ++i) {
                     final Node child = children.item(i);
 
                     /*
                      * does this child have another level of translatable tags?
                      */
                     final boolean containsTranslatableTags = doesElementContainTranslatableContentV1(child);
 
                     /*
                      * if so, save the string we have been building up, process the child, and start building up a new string
                      */
                     if (containsTranslatableTags) {
                         if (nodes.size() != 0) {
                             /*
                              * We have found a child node that itself contains some translatable children. In this case we
                              * create a new translatable string. It is possible that the translatableString has some
                              * insignificant trailing whitespace, because the call to the cleanTranslationText function in the
                              * else statement below has assumed that the node being processed was not the last one in the
                              * translatable string, making the trailing whitespace important. So we clean up the trailing
                              * whitespace here.
                              */
 
                             final Matcher matcher = TRAILING_WHITESPACE_RE_PATTERN.matcher(translatableString);
                             if (matcher.matches()) translatableString = matcher.group("content");
 
                             addTranslationToNodeDetailsToCollection(translatableString, nodes, allowDuplicates, translationStrings);
 
                             translatableString = "";
                             nodes = new ArrayList<Node>();
                         }
 
                         getTranslatableStringsFromNodeV1(child, translationStrings, allowDuplicates, xmlProperties);
                     } else {
                         final String childName = child.getNodeName();
                         final String childText = convertNodeToString(child, true);
 
                         final String cleanedChildText = cleanTranslationText(childText, i == 0, i == childrenLength - 1);
                         final boolean isVerbatimNode = VERBATIM_ELEMENTS.contains(childName);
 
                         final String thisTranslatableString = isVerbatimNode || xmlProperties.isVerbatim() ? childText : cleanedChildText;
 
                         translatableString += thisTranslatableString;
                         nodes.add(child);
                     }
                 }
 
                 /* save the last translated string */
                 if (nodes.size() != 0) {
                     addTranslationToNodeDetailsToCollection(translatableString, nodes, allowDuplicates, translationStrings);
 
                     translatableString = "";
                 }
             }
         } else {
             /* if we hit a non-translatable element, process its children */
             final NodeList nodeList = node.getChildNodes();
             for (int i = 0; i < nodeList.getLength(); ++i) {
                 final Node child = nodeList.item(i);
                 getTranslatableStringsFromNodeV1(child, translationStrings, allowDuplicates, xmlProperties);
             }
         }
     }
 
     /**
      * Get the Translatable String to Node collections from an XML DOM Node.
      *
      * @param node               The node to get the translatable elements from.
      * @param translationStrings The list of translation StringToNodeCollection objects to add to.
      * @param allowDuplicates    If duplicate translation strings should be created in the translationStrings list.
      * @param props              A set of XML Properties for the Node.
      */
     private static void getTranslatableStringsFromNodeV2(final Node node, final List<StringToNodeCollection> translationStrings,
             final boolean allowDuplicates, final XMLProperties props) {
         if (node == null || translationStrings == null) return;
 
         XMLProperties xmlProperties = new XMLProperties(props);
 
         final String nodeName = node.getNodeName();
         final String nodeParentName = node.getParentNode() != null ? node.getParentNode().getNodeName() : null;
 
         final boolean translatableElement = TRANSLATABLE_ELEMENTS.contains(nodeName);
         final boolean standaloneElement = TRANSLATABLE_IF_STANDALONE_ELEMENTS.contains(nodeName);
         final boolean translatableParentElement = TRANSLATABLE_ELEMENTS.contains(nodeParentName);
         if (!xmlProperties.isInline() && INLINE_ELEMENTS.contains(nodeName)) xmlProperties.setInline(true);
         if (!xmlProperties.isVerbatim() && VERBATIM_ELEMENTS.contains(nodeName)) xmlProperties.setVerbatim(true);
 
         /*
          * this element has translatable strings if:
          * 
          * 1. a translatableElement
          * 
          * OR
          *
          * 2. a standaloneElement without a translatableParentElement
          * 
          * 3. not a standaloneElement and not an inlineElement
          */
 
         if ((translatableElement && ((standaloneElement && !translatableParentElement) || (!standaloneElement && !xmlProperties.isInline
                 ())))) {
             final NodeList children = node.getChildNodes();
             final boolean hasChildren = children == null || children.getLength() != 0;
 
             // dump the node if it has no children
             if (!hasChildren) {
                 final String nodeText = convertNodeToString(node, false);
                 final String cleanedNodeText = cleanTranslationText(nodeText, true, true);
 
                 if (xmlProperties.isVerbatim()) {
                     addTranslationToNodeDetailsToCollection(nodeText, node, allowDuplicates, translationStrings);
                 } else if (!cleanedNodeText.isEmpty() && !cleanedNodeText.matches("^\\s+$")) {
                     addTranslationToNodeDetailsToCollection(cleanedNodeText, node, allowDuplicates, translationStrings);
                 }
 
             }
             /*
              * dump all child nodes until we hit one that itself contains a translatable element. in effect the translation
              * strings can contain up to one level of xml elements.
              */
             else {
                 ArrayList<Node> nodes = new ArrayList<Node>();
                 String translatableString = "";
                 boolean removeWhitespaceFromStart = true;
 
                 final int childrenLength = children.getLength();
                 for (int i = 0; i < childrenLength; ++i) {
                     final Node child = children.item(i);
                     final String childNodeName = child.getNodeName();
 
                     // does this child have another level of translatable tags?
                     final boolean containsTranslatableTags = doesElementContainTranslatableContentV2(child);
                     final boolean childTranslatableElement = TRANSLATABLE_ELEMENTS.contains(childNodeName);
                     final boolean childInlineElement = INLINE_ELEMENTS.contains(childNodeName);
 
                     // if so, save the string we have been building up, process the child, and start building up a new string
                     if ((containsTranslatableTags || childTranslatableElement) && !childInlineElement) {
                         if (nodes.size() != 0) {
                             /*
                              * We have found a child node that itself contains some translatable children. In this case we
                              * create a new translatable string. It is possible that the translatableString has some
                              * insignificant trailing whitespace, because the call to the cleanTranslationText function in the
                              * else statement below has assumed that the node being processed was not the last one in the
                              * translatable string, making the trailing whitespace important. So we clean up the trailing
                              * whitespace here.
                              */
 
                             final Matcher matcher = TRAILING_WHITESPACE_RE_PATTERN.matcher(translatableString);
                             if (matcher.matches()) translatableString = matcher.group("content");
 
                             addTranslationToNodeDetailsToCollection(translatableString, nodes, allowDuplicates, translationStrings);
 
                             translatableString = "";
                             nodes = new ArrayList<Node>();
                             removeWhitespaceFromStart = true;
                         }
 
                         getTranslatableStringsFromNodeV2(child, translationStrings, allowDuplicates, xmlProperties);
                     } else {
                         final String childName = child.getNodeName();
                         final String childText = convertNodeToString(child, true);
 
                         final String cleanedChildText = cleanTranslationText(childText, removeWhitespaceFromStart, i == childrenLength - 1);
                         final boolean isVerbatimNode = VERBATIM_ELEMENTS.contains(childName);
 
                         final String thisTranslatableString = isVerbatimNode || xmlProperties.isVerbatim() ? childText : cleanedChildText;
 
                         if (!thisTranslatableString.isEmpty() && !thisTranslatableString.matches("^\\s+$")) {
                             translatableString += thisTranslatableString;
                             nodes.add(child);
 
                             /*
                              * We've processed the first element in the string so now we don't want to remove whitespace from
                              * the start of the String
                              */
                             removeWhitespaceFromStart = false;
                         }
                     }
                 }
 
                 // save the last translated string
                 if (nodes.size() != 0) {
                     addTranslationToNodeDetailsToCollection(translatableString, nodes, allowDuplicates, translationStrings);
 
                     translatableString = "";
                 }
             }
         } else {
             // if we hit a non-translatable element, process its children
             final NodeList nodeList = node.getChildNodes();
             for (int i = 0; i < nodeList.getLength(); ++i) {
                 final Node child = nodeList.item(i);
                 getTranslatableStringsFromNodeV2(child, translationStrings, allowDuplicates, xmlProperties);
             }
         }
     }
 
     public static void replaceTranslatedStrings(final Document xml, final Map<String, String> translations,
             final List<StringToNodeCollection> stringToNodeCollections) {
         if (xml == null || translations == null || translations.size() == 0 || stringToNodeCollections == null || stringToNodeCollections
                 .size() == 0)
             return;
 
         /*
          * We assume that the xml being provided here is either an exact match, or modified by Zanata in some predictable way
          * (i.e. some padding removed), as supplied to the getTranslatableStrings originally, which we then assume matches the
          * strings supplied as the keys in the translations parameter.
          */
 
         if (stringToNodeCollections == null || stringToNodeCollections.size() == 0) return;
 
         for (final StringToNodeCollection stringToNodeCollection : stringToNodeCollections) {
             final String originalString = stringToNodeCollection.getTranslationString();
             final ArrayList<ArrayList<Node>> nodeCollections = stringToNodeCollection.getNodeCollections();
 
             if (nodeCollections != null && nodeCollections.size() != 0) {
                 // Zanata will change the format of the strings that it returns. Here we account for any trimming that was done.
                 final TranslatedStringDetails fixedStringDetails = new TranslatedStringDetails(translations, originalString);
 
                 if (fixedStringDetails.getFixedString() != null) {
                     final String translation = translations.get(fixedStringDetails.getFixedString());
 
                     if (translation != null && !translation.isEmpty()) {
                         // Build up the padding that Zanata removed
                         final StringBuilder leftTrimPadding = new StringBuilder();
                         final StringBuilder rightTrimPadding = new StringBuilder();
 
                         for (int i = 0; i < fixedStringDetails.getLeftTrimCount(); ++i)
                             leftTrimPadding.append(" ");
 
                         for (int i = 0; i < fixedStringDetails.getRightTrimCount(); ++i)
                             rightTrimPadding.append(" ");
 
                         // wrap the returned translation in a root element
                         final String wrappedTranslation = "<tempRoot>" + leftTrimPadding + translation + rightTrimPadding + "</tempRoot>";
 
                         // convert the wrapped translation into an XML document
                         Document translationDocument = null;
                         try {
                             translationDocument = convertStringToDocument(wrappedTranslation);
                         } catch (Exception ex) {
                             LOG.error("Unable to convert Translated String to a DOM Document", ex);
                         }
 
                         // was the conversion successful
                         if (translationDocument != null) {
                             for (final ArrayList<Node> nodes : nodeCollections) {
                                 if (nodes != null && nodes.size() != 0) {
                                     // All nodes in a collection should share the same parent
                                     final Node parent = nodes.get(0).getParentNode();
 
                                     if (parent != null) {
                                         /*
                                          * Replace the old node with contents of the new node. To do this we need to iterate
                                          * over the children and place them from last to first after the node. This will ensure
                                          * the order of the nodes is kept. Also note that we can't just insert into the parent
                                          * at the start or end as there maybe more refined translations (ie an itemizedList) in
                                          * the middle of the content.
                                          */
                                         final Node importNode = xml.importNode(translationDocument.getDocumentElement(), true);
                                         final NodeList translatedChildren = importNode.getChildNodes();
                                         for (int i = translatedChildren.getLength() - 1; i >= 0; i--) {
                                             if (nodes.get(0).getNextSibling() == null) {
                                                 parent.appendChild(translatedChildren.item(i));
                                             } else {
                                                 parent.insertBefore(translatedChildren.item(i), nodes.get(0).getNextSibling());
                                             }
                                         }
 
                                         // remove the original node that the translated text came from
                                         for (final Node node : nodes) {
                                             if (parent == node.getParentNode()) parent.removeChild(node);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
 
     private static StringToNodeCollection findExistingText(final String text, final List<StringToNodeCollection> translationStrings) {
         for (final StringToNodeCollection stringToNodeCollection : translationStrings) {
             if (stringToNodeCollection.getTranslationString().equals(text)) return stringToNodeCollection;
         }
 
         return null;
     }
 
     private static void addTranslationToNodeDetailsToCollection(final String text, final Node node, final boolean allowDuplicates,
             final List<StringToNodeCollection> translationStrings) {
         final ArrayList<Node> nodes = new ArrayList<Node>();
         nodes.add(node);
         addTranslationToNodeDetailsToCollection(text, nodes, allowDuplicates, translationStrings);
     }
 
     private static void addTranslationToNodeDetailsToCollection(final String text, final ArrayList<Node> nodes,
             final boolean allowDuplicates, final List<StringToNodeCollection> translationStrings) {
 
         if (allowDuplicates) {
             translationStrings.add(new StringToNodeCollection(text).addNodeCollection(nodes));
         } else {
             final StringToNodeCollection stringToNodeCollection = findExistingText(text, translationStrings);
 
             if (stringToNodeCollection == null) translationStrings.add(new StringToNodeCollection(text).addNodeCollection(nodes));
             else stringToNodeCollection.addNodeCollection(nodes);
         }
     }
 
     /**
      * Cleans a string for presentation to a translator
      */
     private static String cleanTranslationText(final String input, final boolean removeWhitespaceFromStart,
             final boolean removeWhitespaceFromEnd) {
         String retValue = cleanText(input);
 
         final boolean hasStartWhiteSpace = PRECEEDING_WHITESPACE_SIMPLE_RE_PATTERN.matcher(input).matches();
         final boolean hasEndWhiteSpace = TRAILING_WHITESPACE_SIMPLE_RE_PATTERN.matcher(input).matches();
 
         retValue = retValue.trim();
 
         /*
          * When presenting the contents of a childless XML node to the translator, there is no need for white space padding.
          * When building up a translatable string from a succession of text nodes, whitespace becomes important.
          */
         if (!removeWhitespaceFromStart) {
             if (hasStartWhiteSpace) retValue = " " + retValue;
         }
 
         if (!removeWhitespaceFromEnd) {
             if (hasEndWhiteSpace) retValue += " ";
         }
 
         return retValue;
     }
 
     /**
      * Cleans a string for of insignificant whitespace
      */
     private static String cleanText(final String input) {
         if (input == null) return "";
         /* get rid of line breaks */
         String retValue = input.replaceAll("\\r\\n|\\r|\\n|\\t", " ");
         /* get rid of double spaces */
         while (retValue.indexOf("  ") != -1) retValue = retValue.replaceAll("  ", " ");
 
         return retValue;
     }
 
     /**
      * CDATA sections can not have a "]]>" in them. This method takes the input and wraps it up in one or more CDATA sections,
      * converting any "]]>" strings into "]]&gt;".
      */
     public static String wrapStringInCDATA(final String input) {
         final StringBuffer retValue = new StringBuffer("<![CDATA[");
         retValue.append(input.replaceAll(END_CDATA_RE, END_CDATA_RE + END_CDATA_REPLACE + START_CDATA));
         retValue.append("]]>");
         return retValue.toString();
     }
 
     /**
      * Creates an XIInclude element with a link to a file
      *
      * @param doc  The DOM Document to create the xi:include for.
      * @param file The file name/path to link to.
      * @return An xi:include element that can be used to include content from another file.
      */
     public static Element createXIInclude(final Document doc, final String file) {
         final Element xiInclude = doc.createElementNS("http://www.w3.org/2001/XInclude", "xi:include");
         xiInclude.setAttribute("href", file);
         xiInclude.setAttribute("xmlns:xi", "http://www.w3.org/2001/XInclude");
 
         return xiInclude;
     }
 
     /**
      * Parses a string that defines XML entities and returns a list of Entity objects. An example input string is:
      * <pre>
      *     &lt;!ENTITY ent "My Entity"&gt;
      *     &lt;!ENTITY cut "&lt;para&gt;&lt;/para&gt;"&gt;
      * </pre>
      *
      * @param entitiesString The content spec to get the entities from.
      * @return A list of Entity objects that were parsed from the string.
      */
     public static List<Entity> parseEntitiesFromString(final String entitiesString) {
         final List<Entity> retValue = new ArrayList<Entity>();
 
         // Check to make sure we have something to parse
         if (!isNullOrEmpty(entitiesString)) {
 
             /*
              * This has to be done in two steps because Xerces will not parse an entities value unless it is referenced in the XML
              * content. However since we don't know what entities we have, we need to do an initial parse to get the entity names. Afer
              * that we construct a new wrapper which references the entities and the convert it to a Document making sure that the entities
              * are expanded. Once this is done we can then look over the entities and construct the return value.
              */
             try {
                 // First Pass to find the entity names used
                 final String wrappedEntities = "<!DOCTYPE section [" + entitiesString + "]><section></section>";
                 final Document firstPassDoc = convertStringToDocument(wrappedEntities);
 
                 final List<String> entityNames = new ArrayList<String>();
                 final NamedNodeMap entities = firstPassDoc.getDoctype().getEntities();
                 for (int i = 0; i < entities.getLength(); i++) {
                     entityNames.add(entities.item(i).getNodeName());
                 }
 
                 // Build the second wrapper making sure to include all the custom entities so that they are parsed and we can get them later
                 final StringBuilder wrappedEntities2 = new StringBuilder("<!DOCTYPE section [");
                 wrappedEntities2.append(entitiesString);
                 wrappedEntities2.append("]><section>");
                 for (final String entityName : entityNames) {
                     wrappedEntities2.append("&").append(entityName).append(";");
                 }
                 wrappedEntities2.append("</section>");
 
                 // Do the second pass, as now that the entities are used the value will be parsed
                 final Document secondPassDoc = convertStringToDocument(wrappedEntities2.toString(), false);
                 final NamedNodeMap entities2 = secondPassDoc.getDoctype().getEntities();
                 for (int i = 0; i < entities2.getLength(); i++) {
                     final Entity entity = (Entity) entities2.item(i);
                     retValue.add(entity);
                 }
             } catch (Exception e) {
                 return retValue;
             }
         }
 
         return retValue;
     }
 }
 
 /**
  * Pushing to Zanata will modify strings sent to it for translation. This class contains the info necessary to take a string from Zanata
  * and match it to the source XML.
  */
 class TranslatedStringDetails {
     /**
      * The number of spaces that Zanata removed from the left
      */
     private final int leftTrimCount;
     /**
      * The number of spaces that Zanata removed from the right
      */
     private final int rightTrimCount;
     /**
      * The string that was matched to the one returned by Zanata. This will be null if there was no match.
      */
     private final String fixedString;
 
     TranslatedStringDetails(final Map<String, String> translations, final String originalString) {
         /*
          * Here we account for any trimming that is done by Zanata.
          */
         final String lTrimString = StringUtilities.ltrim(originalString);
         final String rTrimString = StringUtilities.rtrim(originalString);
         final String trimString = originalString.trim();
 
         final boolean containsExactMatch = translations.containsKey(originalString);
         final boolean lTrimMatch = translations.containsKey(lTrimString);
         final boolean rTrimMatch = translations.containsKey(rTrimString);
         final boolean trimMatch = translations.containsKey(trimString);
 
         /* remember the details of the trimming, so we can add the padding back */
         if (containsExactMatch) {
             this.leftTrimCount = 0;
             this.rightTrimCount = 0;
             this.fixedString = originalString;
         } else if (lTrimMatch) {
             this.leftTrimCount = originalString.length() - lTrimString.length();
             this.rightTrimCount = 0;
             this.fixedString = lTrimString;
         } else if (rTrimMatch) {
             this.leftTrimCount = 0;
             this.rightTrimCount = originalString.length() - rTrimString.length();
             this.fixedString = rTrimString;
         } else if (trimMatch) {
             this.leftTrimCount = StringUtilities.ltrimCount(originalString);
             this.rightTrimCount = StringUtilities.rtrimCount(originalString);
             this.fixedString = trimString;
         } else {
             this.leftTrimCount = 0;
             this.rightTrimCount = 0;
             this.fixedString = null;
         }
     }
 
     public int getLeftTrimCount() {
         return leftTrimCount;
     }
 
     public int getRightTrimCount() {
         return rightTrimCount;
     }
 
     public String getFixedString() {
         return fixedString;
     }
 }
 
 class XMLProperties {
     private boolean verbatim = false;
     private boolean inline = false;
 
     public XMLProperties() {
 
     }
 
     public XMLProperties(final XMLProperties props) {
         if (props != null) {
             this.inline = props.isInline();
             this.verbatim = props.isVerbatim();
         }
     }
 
     public boolean isVerbatim() {
         return verbatim;
     }
 
     public void setVerbatim(boolean verbatim) {
         this.verbatim = verbatim;
     }
 
     public boolean isInline() {
         return inline;
     }
 
     public void setInline(boolean inline) {
         this.inline = inline;
     }
 }
