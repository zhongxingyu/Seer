 /*
 Copyright (c) 2003-2006, Lucas Holt
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:
 
   Redistributions of source code must retain the above copyright notice, this list of
   conditions and the following disclaimer.
 
   Redistributions in binary form must reproduce the above copyright notice, this
   list of conditions and the following disclaimer in the documentation and/or other
   materials provided with the distribution.
 
   Neither the name of the Just Journal nor the names of its contributors
   may be used to endorse or promote products derived from this software without
   specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package com.justjournal.rss;
 
 import com.justjournal.utility.Xml;
 import org.apache.log4j.Category;
 import org.w3c.dom.Document;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Retrieves a RSS document using HTTP, parses the document, and
  * converts it to HTML.
  *
  * @author Lucas Holt
 * @version $Id: HeadlineBean.java,v 1.5 2008/07/17 02:24:59 laffer1 Exp $
  * @since 1.0
  *        User: laffer1
  *        Date: Jul 22, 2003
  *        Time: 12:19:17
  *        <p/>
  *        Switch to cvs versioning.
  *        1.4 Fixed bugs when certain rss features are missing.
  *        1.3 now supports several RSS 2 features (non rdf format)
  *        1.2 added several properties to the output including
  *        the published date, and description.
  *        1.1 optimized code
  *        1.0 Initial release
  */
 
 public class HeadlineBean {
 
     private static Category log = Category.getInstance(HeadlineBean.class.getName());
 
     private static final char endl = '\n';
 
     protected URL u;
     protected InputStream inputXML;
     protected DocumentBuilder builder;
     protected Document document;
 
     final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 
     protected void getRssDocument(final String uri)
             throws Exception {
 
         if (log.isDebugEnabled())
             log.debug("Starting getRssDocument()");
 
         //Open the file for reading:
         u = new URL(uri);
         inputXML = u.openStream();
 
         //Build document:
         factory.setValidating(false);
        factory.setIgnoringComments(true);
        factory.setCoalescing(true);
         builder = factory.newDocumentBuilder();
         document = builder.parse(inputXML);
     }
 
     public String parse(final String url) {
         try {
             if (log.isDebugEnabled())
                 log.debug("Starting parse()");
 
             getRssDocument(url);
 
             // output variable
             final StringBuffer sb = new StringBuffer();
 
             String contentTitle = "";
             String contentLink = "";
             String contentDescription = "";
             String contentLastBuildDate = "";
             String contentGenerator = "";
 
             if (log.isDebugEnabled())
                 log.debug("Prepare xml nodelists");
 
             org.w3c.dom.NodeList channelList = document.getElementsByTagName("channel");
             org.w3c.dom.NodeList chnodes = channelList.item(0).getChildNodes();
 
             String imageUrl = null;
             String imageTitle = null;
             String imageLink = "";
             String imageWidth = null;
             String imageHeight = null;
 
             if (log.isDebugEnabled())
                 log.debug("Iterate through the nodelists");
 
             for (int k = 0; k < chnodes.getLength(); k++) {
                 org.w3c.dom.Node curNode = chnodes.item(k);
 
                 if (curNode.getNodeName().equals("title")) {
                     contentTitle = curNode.getChildNodes().item(0).getNodeValue();
                 } else if (curNode.getNodeName().equals("link")) {
                     contentLink = curNode.getChildNodes().item(0).getNodeValue();
                 } else if (curNode.getNodeName().equals("description")) {
                     contentDescription = curNode.getChildNodes().item(0).getNodeValue();
                 } else if (curNode.getNodeName().equals("lastBuildDate")) {
                     contentLastBuildDate = curNode.getChildNodes().item(0).getNodeValue();
                 } else if (curNode.getNodeName().equals("generator")) {
                     contentGenerator = curNode.getChildNodes().item(0).getNodeValue();
                 } else if (curNode.getNodeName().equals("image")) {
 
                     org.w3c.dom.NodeList imageNodes = curNode.getChildNodes();
 
                     for (int z = 0; z < imageNodes.getLength(); z++) {
                         if (imageNodes.item(z).getNodeName().equals("url")) {
                             imageUrl = imageNodes.item(z).getChildNodes().item(0).getNodeValue();
                         } else if (imageNodes.item(z).getNodeName().equals("height")) {
                             imageHeight = imageNodes.item(z).getChildNodes().item(0).getNodeValue();
                         } else if (imageNodes.item(z).getNodeName().equals("width")) {
                             imageWidth = imageNodes.item(z).getChildNodes().item(0).getNodeValue();
                         } else if (imageNodes.item(z).getNodeName().equals("title")) {
                             imageTitle = imageNodes.item(z).getChildNodes().item(0).getNodeValue();
                         } else if (imageNodes.item(z).getNodeName().equals("link")) {
                             imageLink = imageNodes.item(z).getChildNodes().item(0).getNodeValue();
                         }
                     }
                 }
             }
 
             if (log.isDebugEnabled())
                 log.debug("Prepare HTML output");
 
             // create header!
             sb.append("<div style=\"width: 100%; padding: .1in; background: #F2F2F2;\" class=\"ljfhead\">");
             sb.append(endl);
 
             sb.append("<!-- Generator: ");
             sb.append(contentGenerator);
             sb.append(" -->");
             sb.append(endl);
 
             if (imageUrl != null) {
                 sb.append("<span style=\"padding: 5px; float:left; ");
                 if (imageWidth != null && Integer.parseInt(imageWidth) > 0) {
                     sb.append("width:");
                     sb.append(imageWidth);
                     sb.append("px; ");
                 }
                 if (imageHeight != null && Integer.parseInt(imageHeight) > 0) {
                     sb.append("height:");
                     sb.append(imageHeight);
                     sb.append("px; ");
                 }
                 sb.append("position: relative;\">");
 
                 sb.append("<a href=\"");
                 sb.append(imageLink);
                 sb.append("\">");
 
                 sb.append("<img src=\"");
                 sb.append(imageUrl);
                 if (imageHeight != null && imageWidth != null
                         && Integer.parseInt(imageWidth) > 0 && Integer.parseInt(imageHeight) > 0) {
                     // Only add height and width attributes if they are defined in the feed.
                     sb.append("\" height=\"");
                     sb.append(imageHeight);
                     sb.append("\" width=\"");
                     sb.append(imageWidth);
                 }
                 sb.append("\" alt=\"");
                 sb.append(imageTitle);
                 sb.append("\" title=\"");
                 sb.append(imageTitle);
                 sb.append("\" /></a></span>");
                 sb.append(endl);
             }
 
             sb.append("<h3 ");
             if (contentDescription != null) {
                 sb.append("title=\"");
                 sb.append(contentDescription);
                 sb.append("\"");
             }
             sb.append(">");
             sb.append(contentTitle);
             sb.append("</h3>");
             sb.append(endl);
 
             // some rss feeds don't have a last build date
             if (contentLastBuildDate != null && contentLastBuildDate.length() > 0) {
                 sb.append("<p>Updated: ");
                 sb.append(contentLastBuildDate);
                 sb.append("<br />");
             } else {
                 sb.append("<p>");
             }
 
             /* if (contentDescription != null) {
                 sb.append(contentDescription);
                 sb.append("<br />");
             }*/
 
             if (contentLink != null) {
                sb.append("<a href=\"").append(contentLink).append("\">[").append(contentLink).append("]</a>");
             }
             sb.append("</p>");
             sb.append(endl);
 
 
             sb.append("<div style=\"clear: both;\">&nbsp;</div>");
             sb.append(endl);
 
             sb.append("</div>");
             sb.append(endl);
 
             //Generate the NodeList;
             org.w3c.dom.NodeList nodeList = document.getElementsByTagName("item");
 
             sb.append("<ol class=\"RssItems\">");
             sb.append(endl);
 
             for (int i = 0; i < nodeList.getLength() && i < 16; i++) {
                 org.w3c.dom.NodeList childList = nodeList.item(i).getChildNodes();
                 // some of the properties of <items>
                 String link = null;
                 String title = null;
                 String pubDate = null;
                 String description = null;
                 String guid = null;
 
                 for (int j = 0; j < childList.getLength(); j++) {
                     if (childList.item(j).getNodeName().equals("link")) {
                         link = childList.item(j).getChildNodes().item(0).getNodeValue();
                     } else if (childList.item(j).getNodeName().equals("title")) {
                         title = childList.item(j).getChildNodes().item(0).getNodeValue();
 
                     } else if (childList.item(j).getNodeName().equals("pubDate")) {
                         pubDate = childList.item(j).getChildNodes().item(0).getNodeValue();
 
                     } else if (childList.item(j).getNodeName().equals("description")) {
                         description = childList.item(j).getChildNodes().item(0).getNodeValue();
 
                     } else if (childList.item(j).getNodeName().equals("guid")) {
                         guid = childList.item(j).getChildNodes().item(0).getNodeValue();
                     }
                 }
 
                 // assert the basic properties are there.
                 if (link != null && title != null) {
                     sb.append("<li class=\"RssItem\">");
 
                     sb.append("<span onclick=\"expandcontent(this, 'r").append(url.hashCode()).append("_").append(i).append("')\" class=\"RssItemTitle\">");
                     sb.append("<span class=\"showstate\"></span> ");
                    sb.append("<!-- guid: ");
                    sb.append(guid);
                    sb.append(" -->");
                     sb.append("<a href=\"");
                     sb.append(link);
                     sb.append("\" title=\"");
                     sb.append(Xml.cleanString(title));
                     sb.append("\" >");
                     sb.append(Xml.cleanString(title));
                     sb.append("</a>");
                     sb.append("</span>");
                     sb.append(endl);
 
                     sb.append("<div id=\"r").append(url.hashCode()).append("_").append(i).append("\" class=\"switchcontent\">");
 
                     sb.append("<br />");
                     sb.append("<span class=\"RssItemDesc\">");
                     /*
                       /<(script|noscript|object|embed|style|frameset|frame|iframe)[>\s\S]*<\/\\1>/i /<\/?!?(param|link|meta|doctype|div|font)[^>]*>/i /(class|style|id)=\"[^\"]*\"/i
                     */
                     Pattern p = Pattern.compile("/<(script|noscript|object|embed|style|frameset|frame|iframe|link)[>\\s\\S]*<\\/\\1>/i");
                     Matcher m = p.matcher(description);
                     String result = m.replaceAll("");
 
                     sb.append(result);
                     sb.append("</span>");
 
                     sb.append(" <span class=\"RssReadMore\">");
                     sb.append("<a href=\"");
                     sb.append(link); // TODO: was guid ... why
                     sb.append("\">");
                     sb.append("Read More</a></span>");
                     sb.append(endl);
 
                     // some rss versions don't have a pub date per entry
                     if (pubDate != null) {
                         sb.append(" <br /><span class=\"RssItemPubDate\">");
                         sb.append(pubDate);
                         sb.append("</span>");
                         sb.append(endl);
                     }
 
                     sb.append("<div class=\"RssTechnoratiTrack\"><a title=\"Technorati Trackback\"");
                     sb.append(" href=\"http://www.technorati.com/cosmos/search.html?url=");
                     sb.append(link);
                     sb.append("\"><img src=\"/images/technorati.gif\" alt=\"Technorati Logo\" /></a>");
                     sb.append("</div>");
                     sb.append(endl);
 
                     sb.append("</div>");
                     sb.append(endl);
 
                     sb.append("</li>");
                     sb.append(endl);
                 }
             }
 
             sb.append("</ol>");
             sb.append(endl);
 
             return sb.toString();
        } catch (java.io.FileNotFoundException e404) {
            return "404 Not Found. The feed is no longer available. " + url + endl;
         } catch (Exception e) {
             return "Error, could not process request: " + e.toString() + endl;
         }
     }
 
 }
