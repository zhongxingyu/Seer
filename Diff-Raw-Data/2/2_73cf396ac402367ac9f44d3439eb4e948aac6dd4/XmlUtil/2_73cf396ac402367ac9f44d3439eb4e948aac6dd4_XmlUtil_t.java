 /* **********************************************************************
     Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 
 package edu.rpi.sss.util.xml;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.StringWriter;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import org.w3c.tidy.Configuration;
 import org.w3c.tidy.Tidy;
 
 import org.xml.sax.SAXException;
 
 /**
  * Utility routines associated with handling xml
  *
  * @author Mike Douglass   douglm@rpi.edu
  */
 public final class XmlUtil implements Serializable {
   /** These values define the state of xml  after tidying
    */
 
   /** Field is tidied
    */
   public static final int xmlOk       = 0;
 
   /** Field has no value
    */
   public static final int xmlMissing  = 1;
 
   /** Xml is (probably) valid but not tidied
    */
   public static final int xmlUntidy   = 2;
 
   /** Errors occurred during tidy
    */
   public static final int xmlError    = 3;
 
   /** Xml became null when tidied
    */
   public static final int xmlTidyNull = 4;
 
   /** The parameter was untagged text
    */
   public static final int xmlNotXml = 5;
 
   /** Exception when tidied
    */
   public static final int xmlTidyException = 5;
 
   /** Result from checking string
    */
   public static class CheckStringResult {
     /** */
     public String errStr;
     /** */
     public int tidyState;
     /** */
     public String newContent;
   }
 
   /** Given a string return a tidied form of the string. Throws an exception
    *  if the xml tidy method does so.
    *
    *  <p>Take care, certain forms of input result in no output. Note that
    * we watch specifically for a line consisting of a single new line
    * character.
    *
    * <p>We also check for a string that is just text with no markup.
    * This check is rather simple.
    *
    * <p>If the first non-whitespace character is not &gt; we assume simple
    * text input and optionally attempt to tag it.
    *
    * <p>This check is suppressed if tagUntagged = false.
    *
    * @param  id           String to identify input
    * @param  val          String to tidy
    * @param  crs          Object to hold results
    * @param debug
    */
   public static void tidyString(String id,
                                 String val,
                                 CheckStringResult crs,
                                 boolean debug) {
     crs.errStr = null;
     crs.tidyState = xmlOk;
     crs.newContent = null;
 
     if ((val == null) || (val.length() == 0)) {
       return;
     }
 
     StringWriter errStrw = new StringWriter();
     PrintWriter err = new PrintWriter(errStrw);
 
     try {
       crs.newContent = tidyXML(id, val, err);
     } catch (Exception e) {
       crs.tidyState = xmlTidyException;
       e.printStackTrace(err);
       if (debug) {
         System.out.println("************* Exception tidying html/xml for " + id + ":");
         System.out.println(e.getMessage());
         e.printStackTrace();
       }
 
       return;
     }
 
     crs.errStr = errStrw.getBuffer().toString();
 
     if ((crs.errStr != null) && (crs.errStr.length() != 0)) {
       // Presume an error
       crs.tidyState = xmlError;
       if (debug) {
         System.out.println("************* Errors tidying html/xml for " + id + ":");
         System.out.println(crs.errStr);
       }
 
       return;
     }
 
     if ((crs.newContent == null) || (crs.newContent.length() == 0)) {
       crs.newContent = null;
       return;
     }
 
     /* * Watch for invalid characters - the tidy process doesn't seem to
      *  catch them.
      * /
     byte[] bytes = crs.newContent.getBytes();
 
     for (int i = 0; i < bytes.length; i++) {
       if ((bytes[i] & 0x0ff) > 127) {
         int end = Math.min(bytes.length, i + 20);
         errStrw.write("Found byte > 128 at " + i +
                       " bytes = " + (bytes[i] & 0x0ff) +
                       " " +
                       crs.newContent.substring(i, end) +
                       "\n");
         crs.tidyState = xmlError;
       }
     }
 
     /* * Watch for characters over 127
      * /
     StringReader sr = new StringReader(crs.newContent);
     int ct = 0;
     int len = crs.newContent.length();
 
     try {
       while (sr.ready()) {
         int ch = sr.read();
 
         if (ch < 0) {
           break;
         }
 
         if (ch > 127) {
           int end = Math.min(len, ct + 20);
           errStrw.write("Found char > 127 at " + ct +
                         " char = " + ch +
                         " " +
                         crs.newContent.substring(ct, end) +
                         "\n");
           crs.tidyState = xmlError;
         }
 
         ct++;
       }
     } catch (Throwable t) {
       errStrw.write("Unexpected exception " + t.getMessage());
       crs.tidyState = xmlError;
     }
     */
 
     /** For the moment just make all entities numeric
      */
     try {
       crs.newContent = EntityMap.makeNumeric(crs.newContent, true);
     } catch (Throwable t) {
       errStrw.write("Tidy exception " + t.getMessage());
       crs.tidyState = xmlError;
     }
 
     if (crs.tidyState == xmlError) {
       // Found some bad characters
       crs.errStr = errStrw.getBuffer().toString();
     }
   }
 
   /** This does everything tidyString does with the addition that it adjusts
    * the result to indicate something about the original string.
    *
    * <p>if emptyOk is false and the result is null the status is xmlNull
    * otherwise a null value is considered OK.
    *
    * <p>We also check for the value changing. A change indicates some
    * tidying took place so we return xmlUntidy.
    *
    * @param  id           String to identify input
    * @param  val          String to tidy
    * @param  crs          Object to hold results
    * @param  emptyOk      True if empty text is not special
    * @param debug
    */
   public static void checkString(String id,
                                  String val,
                                  CheckStringResult crs,
                                  boolean emptyOk,
                                  boolean debug) {
     tidyString(id, val, crs, /*tagUntagged,*/ debug);
 
     if ((crs.tidyState == xmlOk) &&
         (crs.newContent == null)) {
       if (!emptyOk) {
         crs.tidyState = xmlTidyNull;
       }
       return;
     }
 
     if (crs.tidyState == xmlError) {
       return;
     }
 
     /** See if the content changed - presumably if it did it's tidier now.
      *  We can't do a straight equality check - the newText version is
      * missing char val 13 (cr?)
      */
 
     if (!strip(val).equals(strip(crs.newContent))) {
       crs.tidyState = xmlUntidy;
       return;
     }
 
     // All ok
     return;
   }
 
   /* Strip out the characters causing comparison problems.
    *
    * @param strVal
    * @return
    */
   private static String strip(String strVal) {
     StringBuffer val = new StringBuffer(strVal);
 
     int i = 0;
     while (i < val.length()) {
       if ((int)val.charAt(i) == 13) {
         val.deleteCharAt(i);
       } else {
         i++;
       }
     }
 
     return val.toString();
   }
 
   /* The config fields from Tidy
 
      boolean BreakBeforeBR = false;    * o/p newline before <br> or not? *
      boolean BurstSlides = false;      * create slides on each h2 element *
      boolean DropEmptyParas = true;    * discard empty p elements *
      boolean DropFontTags = false;     * discard presentation tags *
      boolean Emacs = false;            * if true format error output for GNU
                                          Emacs *
      boolean EncloseBlockText = false; * if yes text in blocks is wrapped in
                                          <p>'s *
      boolean EncloseBodyText = false;  * if yes text at body is wrapped in
                                          <p>'s *
      boolean FixBackslash = true;      * fix URLs by replacing \ with / *
      boolean FixComments = true;       * fix comments with adjacent hyphens *
      boolean HideEndTags = false;      * suppress optional end tags *
      boolean IndentAttributes = false; * newline+indent before each attribute *
      boolean IndentContent = false;    * indent content of appropriate tags *
      boolean KeepFileTimes = true;     * if yes last modied time is preserved *
      boolean LiteralAttribs = false;   * if true attributes may use newlines *
      boolean LogicalEmphasis = false;  * replace i by em and b by strong *
      boolean MakeClean = false;        * remove presentational clutter *
      boolean NumEntities = false;      * use numeric entities *
      boolean OnlyErrors = false;       * if true normal output is suppressed *
      boolean QuoteAmpersand = true;    * output naked ampersand as &amp; *
      boolean QuoteMarks = false;       * output " marks as &quot; *
      boolean QuoteNbsp = true;         * output non-breaking space as entity *
      boolean Quiet = false;            * no 'Parsing X', guessed DTD or summary
      boolean RawOut = false;           * avoid mapping values > 127 to entities
      boolean ShowWarnings = true;      * however errors are always shown *
      boolean SmartIndent = false;      * does text/block level content effect
                                          indentation *
      boolean TidyMark = true;          * add meta element indicating tidied doc
      boolean UpperCaseAttrs = false;   * output attributes in upper not lower
                                          case *
      boolean UpperCaseTags = false;    * output tags in upper not lower case *
      boolean WrapAsp = true;           * wrap within ASP pseudo elements *
      boolean WrapAttVals = false;      * wrap within attribute values *
      boolean WrapJste = true;          * wrap within JSTE pseudo elements *
      boolean WrapPhp = true;           * wrap within PHP pseudo elements *
      boolean WrapScriptlets = false;   * wrap within JavaScript string literals
      boolean WrapSection = true;       * wrap within <![ ... ]> section tags *
      boolean Word2000 = false;         * draconian cleaning for Word2000 *
      boolean xHTML = false;            * output extensible HTML *
      boolean XmlOut = false;           * create output as XML *
      boolean XmlPi = false;            * add <?xml?> for XML docs *
      boolean XmlPIs = false;           * if set to yes PIs must end with ?> *
      boolean XmlSpace = false;         * if set to yes adds xml:space attr as
                                          needed *
      boolean XmlTags = false;          * treat input as XML *
   */
 
 
   /** Given a string return a tidied form of the string. Throws an exception if
    *  the xml tidy method does so.
    *
    *  <p>Take care, certain forms of input result in no output. Note that
    * we watch specifically for a line cosisting of a single new line
    * character.
    *
    * <p>We also check for a string that is just text with no markup.
    * This check is rather simple.
    *
    * <p>If the first non-whitespace character is not &gt; we assume simple
    * text input and optionally attempt to tag it.
    *
    * @param  id           String to identify input
    * @param  val          String to tidy
    * @param  err          An error output stream
    * @return String    tidied result.
    * @throws Exception When tidying fails.
    */
   public static String tidyXML(String id,
                                String val,
                                PrintWriter err) throws Exception {
     if (val == null) {
       return null;
     }
 //    if (checkSimpleText(val)) {
 //      val = tagSimpleText(val);
 //    }
 
     ByteArrayInputStream in = new ByteArrayInputStream(val.getBytes());
     ByteArrayOutputStream out = new ByteArrayOutputStream();
 
     try {
 /* ORIGINAL CONFIG FOLLOWS: (3/24/2004)
       Tidy tidy = new Tidy();
       tidy.setXmlOut(true);
 //      tidy.setXmlTags(true);
       tidy.setEncloseBlockText(true);
       tidy.setEncloseText(true);
       tidy.setSmartIndent(true);
 //      tidy.setQuiet(false);
       tidy.setQuiet(true);
       tidy.setNumEntities(true);
       tidy.setShowWarnings(false);
       tidy.setInputStreamName(id);
 */
 
 // ARLEN'S CONFIG FOLLOWS: (2004/04/05)
       Tidy tidy = new Tidy();
       tidy.setAltText("please supply an alt tag");
       tidy.setDropEmptyParas(true);
       tidy.setDropFontTags(true);
       tidy.setEncloseBlockText(false);
       tidy.setEncloseText(true);
       tidy.setFixBackslash(true);
       tidy.setFixComments(true);
       tidy.setIndentContent(true);
       tidy.setIndentAttributes(true);
       tidy.setInputStreamName(id);
       tidy.setLogicalEmphasis(true);
       tidy.setNumEntities(false);
       tidy.setQuiet(true);
       tidy.setQuoteAmpersand(true);
       tidy.setShowWarnings(false);
       tidy.setSmartIndent(true);
       tidy.setSpaces(2);
       tidy.setTabsize(2);
       //tidy.setWord2000(true);
       tidy.setWord2000(false);
       tidy.setXHTML(true);
       //tidy.setMakeClean(true);
       tidy.setMakeClean(false);
 
       tidy.setCharEncoding(Configuration.UTF8);
 // END ARLEN'S CONFIG
 
       tidy.setErrout(err);
       tidy.parse(in, out);
 
       String res = out.toString();
 
       if ((res.length() == 1) && ((int)res.charAt(0) == 10)) {
         err.println("Tidy for " + id + " results in empty string");
         res = null;
       }
       return res;
     } catch (Exception e) {
       throw new Exception("Unable to tidy document");
     }
   }
 
   /** See if we have simple untagged text. At the moment this is a very
    *  simple check, if the first character after white-space is a less-than
    *  we assume tagged text.
    *
    * @param  val      The text to check
    * @return boolean  true if the text is apparently untagged
    * /
   private static boolean checkSimpleText(String val) {
     int i = skipWhite(val, 0);
 
     if (val.charAt(i) == '< ') {
       return false;
     }
 
     return true;
   }
 
   /** Convert simple untagged text to tagged text.
    * <p>We should check for special characters if this is untagged and
    * replace with the appropriate entities
    *
    * @param val      The text to check
    * @return String  modified text.
    * /
   private static String tagSimpleText(String val) {
     int i = skipWhite(val, 0);
 
     if (i == val.length()) {
       return "";
     }
 
     /** Assume we have non-html/xml. We should attempt to add paragraph tags
      * /
 
     StringReader sr = new StringReader(val);
     LineNumberReader lr = new LineNumberReader(sr);
     StringBuffer sb = new StringBuffer();
 
     sb.append("<p>\n");
     try {
       while (lr.ready()) {
         String l = lr.readLine();
 
         if (l == null) {
           break;
         }
 
         if (skipWhite(l, 0) == l.length()) {
           sb.append("</p>\n");
           sb.append("<p>\n");
         } else {
           sb.append(l);
           sb.append('\n');
         }
       }
       sb.append("</p>\n");
 
       return sb.toString();
     } catch (Exception e) {
       System.out.println("Exception in checkSimpleText(): " +
                          e.getMessage());
       e.printStackTrace();
       return val;
     }
   }
 
   private static int skipWhite(String val, int i) {
     while ((i < val.length()) && (Character.isWhitespace(val.charAt(i)))) {
       i++;
     }
 
     return i;
   }*/
 
   /** Get the value of an element. We expect 0 or 1 child nodes.
    * For no child node we return null, for more than one we raise an
    * exception.
    *
    * @param el          Node whose value we want
    * @param name        String name to make exception messages more readable
    * @return String     node value or null
    * @throws SAXException
    */
   public static String getOneNodeVal(Node el, String name)
       throws SAXException {
     /* We expect one child of type text */
 
     if (!el.hasChildNodes()) {
       return null;
     }
 
     NodeList children = el.getChildNodes();
     if (children.getLength() > 1){
       throw new SAXException("Multiple property values: " + name);
     }
 
     Node child = children.item(0);
     return child.getNodeValue();
   }
 
   /** Get the value of an element. We expect 0 or 1 child nodes.
    * For no child node we return null, for more than one we raise an
    * exception.
    *
    * @param el          Node whose value we want
    * @return String     node value or null
    * @throws SAXException
    */
   public static String getOneNodeVal(Node el) throws SAXException {
     return getOneNodeVal(el, el.getNodeName());
   }
 
   /** Get the value of an element. We expect 1 child node otherwise we raise an
    * exception.
    *
    * @param el          Node whose value we want
    * @param name        String name to make exception messages more readable
    * @return String     node value
    * @throws SAXException
    */
   public static String getReqOneNodeVal(Node el, String name)
       throws SAXException {
     String str = getOneNodeVal(el, name);
 
     if ((str == null) || (str.length() == 0)) {
       throw new SAXException("Missing property value: " + name);
     }
 
     return str;
   }
 
   /** Get the value of an element. We expect 1 child node otherwise we raise an
    * exception.
    *
    * @param el          Node whose value we want
    * @return String     node value
    * @throws SAXException
    */
   public static String getReqOneNodeVal(Node el) throws SAXException {
     return getReqOneNodeVal(el, el.getNodeName());
   }
 
   /** Return the value of the named attribute of the given element.
    *
    * @param el          Element
    * @param name        String name of desired attribute
    * @return String     attribute value or null
    * @throws SAXException
    */
   public static String getAttrVal(Element el, String name)
       throws SAXException {
     Attr at = el.getAttributeNode(name);
     if (at == null) {
       return null;
     }
 
     return at.getValue();
   }
 
   /** Return the required value of the named attribute of the given element.
    *
    * @param el          Element
    * @param name        String name of desired attribute
    * @return String     attribute value
    * @throws SAXException
    */
   public static String getReqAttrVal(Element el, String name)
       throws SAXException {
     String str = getAttrVal(el, name);
 
     if ((str == null) || (str.length() == 0)) {
       throw new SAXException("Missing attribute value: " + name);
     }
 
     return str;
   }
 
   /** Return the attribute value of the named attribute from the given map.
    *
    * @param nnm         NamedNodeMap
    * @param name        String name of desired attribute
    * @return String     attribute value or null
    */
   public static String getAttrVal(NamedNodeMap nnm, String name) {
     Node nmAttr = nnm.getNamedItem(name);
 
     if ((nmAttr == null) || (absent(nmAttr.getNodeValue()))) {
       return null;
     }
 
     return nmAttr.getNodeValue();
   }
 
   /** The attribute value of the named attribute in the given map must be
    * absent or "yes" or "no".
    *
    * @param nnm         NamedNodeMap
    * @param name        String name of desired attribute
    * @return Boolean    attribute value or null
    * @throws SAXException
    */
   public static Boolean getYesNoAttrVal(NamedNodeMap nnm, String name)
       throws SAXException {
     String val = getAttrVal(nnm, name);
 
     if (val == null) {
       return null;
     }
 
     if ((!"yes".equals(val)) && (!"no".equals(val))) {
       throw new SAXException("Invalid attribute value: " + val);
     }
 
     return new Boolean("yes".equals(val));
   }
 
   /**
    * @param nd
    * @return int number of attributes
    */
   public static int numAttrs(Node nd) {
     NamedNodeMap nnm = nd.getAttributes();
 
     if (nnm == null) {
       return 0;
     }
 
     return nnm.getLength();
   }
 
   /** All the children must be elements or white space text nodes.
    *
    * @param nd
    * @return ArrayList   element nodes. Always non-null
    * @throws SAXException
    */
   public static ArrayList<Element> getElements(Node nd) throws SAXException {
     ArrayList<Element> al = new ArrayList<Element>();
 
     NodeList children = nd.getChildNodes();
 
     for (int i = 0; i < children.getLength(); i++) {
       Node curnode = children.item(i);
 
       if (curnode.getNodeType() == Node.TEXT_NODE) {
         String val = curnode.getNodeValue();
 
         if (val != null) {
           for (int vi= 0; vi < val.length(); vi++) {
             if (!Character.isWhitespace(val.charAt(vi))) {
               throw new SAXException("Non-whitespace text in element body for " +
                                      nd.getLocalName());
             }
           }
         }
       } else if (curnode.getNodeType() == Node.COMMENT_NODE) {
         // Ignore
       } else if (curnode.getNodeType() == Node.ELEMENT_NODE) {
         al.add((Element)curnode);
       } else {
         throw new SAXException("Unexpected child node " + curnode.getLocalName() +
                                " for " + nd.getLocalName());
       }
     }
 
     return al;
   }
 
   /** Return the content for the current element. All leading and trailing
    * whitespace and embedded comments will be removed.
    *
    * <p>This is only intended for an element with no child elements.
    *
    * @param el
    * @return element content
    * @throws SAXException
    */
   public static String getElementContent(Element el) throws SAXException {
     StringBuffer sb = new StringBuffer();
 
     NodeList children = el.getChildNodes();
 
     for (int i = 0; i < children.getLength(); i++) {
       Node curnode = children.item(i);
 
       if (curnode.getNodeType() == Node.TEXT_NODE) {
         sb.append(curnode.getNodeValue());
       } else if (curnode.getNodeType() == Node.CDATA_SECTION_NODE) {
         sb.append(curnode.getNodeValue());
       } else if (curnode.getNodeType() == Node.COMMENT_NODE) {
         // Ignore
       } else {
         throw new SAXException("Unexpected child node " + curnode.getLocalName() +
                                " for " + el.getLocalName());
       }
     }
 
     return sb.toString().trim();
   }
 
   /** Return true if the current element has non zero length content.
    *
    * @param el
    * @return boolean
    * @throws SAXException
    */
   public static boolean hasContent(Element el) throws SAXException {
     String s = getElementContent(el);
 
    return (s != null) && (s.length() > 0);
   }
 
   /** See if this node has any children
    *
    * @param el
    * @return boolean   true for any child elements
    * @throws SAXException
    */
   public static boolean hasChildren(Element el) throws SAXException {
     NodeList children = el.getChildNodes();
 
     for (int i = 0; i < children.getLength(); i++) {
       Node curnode = children.item(i);
 
       short ntype =  curnode.getNodeType();
       if ((ntype != Node.TEXT_NODE) &&
           (ntype != Node.CDATA_SECTION_NODE) &&
           (ntype != Node.COMMENT_NODE)) {
         return true;
       }
     }
 
     return false;
   }
 
   /** See if this node is empty
    *
    * @param el
    * @return boolean   true for empty
    * @throws SAXException
    */
   public static boolean isEmpty(Element el) throws SAXException {
     return !hasChildren(el) && !hasContent(el);
   }
 
 
   /**
    * @param nd
    * @return element array from node
    * @throws SAXException
    */
   public static Element[] getElementsArray(Node nd) throws SAXException {
     ArrayList<Element> al = getElements(nd);
 
     return (Element[])al.toArray(new Element[al.size()]);
   }
 
   /**
    * @param nd
    * @return only child node
    * @throws SAXException  if not exactly one child elemnt
    */
   public static Element getOnlyElement(Node nd) throws SAXException {
     Element[] els = getElementsArray(nd);
 
     if (els.length != 1) {
       throw new SAXException("Expected exactly one child node for " +
                               nd.getLocalName());
     }
 
     return els[0];
   }
 
   private static boolean absent(String val) {
     return (val == null) || (val.length() == 0);
   }
 }
 
