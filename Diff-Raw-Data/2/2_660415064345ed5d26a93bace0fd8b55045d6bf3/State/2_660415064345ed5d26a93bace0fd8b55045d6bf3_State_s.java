 /* 
  * Eddie RSS and Atom feed parser
  * Copyright (C) 2006  David Pashley
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  * 
  * Linking this library statically or dynamically with other modules is making a
  * combined work based on this library. Thus, the terms and conditions of the GNU
  * General Public License cover the whole combination.
  * 
  * As a special exception, the copyright holders of this library give you
  * permission to link this library with independent modules to produce an
  * executable, regardless of the license terms of these independent modules, and
  * to copy and distribute the resulting executable under a liense certified by the
  * Open Source Initative (http://www.opensource.org), provided that you also meet,
  * for each linked independent module, the terms and conditions of the license of
  * that module. An independent module is a module which is not derived from or
  * based on this library. If you modify this library, you may extend this
  * exception to your version of the library, but you are not obligated to do so.
  * If you do not wish to do so, delete this exception statement from your version.
  */
 package uk.org.catnip.eddie.parser;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.helpers.AttributesImpl;
 import java.lang.StringBuilder;
 import java.util.Map;
 import java.util.Hashtable;
 import org.apache.log4j.Logger;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 public class State {
     private static Map<String,String> element_aliases = createElementAliases();
 
     static Logger log = Logger.getLogger(State.class);
 
     private static Map<String,String> namespace_aliases = createNamespaceAliases();
 
     private static Map<String,String> createElementAliases() {
         Map<String,String> aliases = new Hashtable<String,String>();
         aliases.put("abstract", "description");
         aliases.put("body", "content");
         aliases.put("content:encoded", "content_encoded");
         aliases.put("dcterms:created", "created");
         aliases.put("dc:author", "author");
         aliases.put("dc:creator", "author");
         aliases.put("dc:contributor", "contributor");
         aliases.put("dc:date", "modified");
         aliases.put("dc:language", "language");
         aliases.put("dc:publisher", "publisher");
         aliases.put("dc:rights", "copyright");
         aliases.put("dc:subject", "category");
         aliases.put("dc:title", "title");
         aliases.put("dcterms:modified", "modified");
         aliases.put("item", "entry");
         aliases.put("itunes:author", "author");
         aliases.put("itunes:block", "itunes_block");
         aliases.put("itunes:category", "itunes_category");
         aliases.put("itunes:duration", "itunes_duration");
         aliases.put("itunes:email", "email");
         aliases.put("itunes:explicit", "itunes_explicit");
         aliases.put("itunes:image", "image");
         aliases.put("itunes:keywords", "itunes_keywords");
         aliases.put("itunes:link", "link");
         aliases.put("itunes:owner", "publisher");
         aliases.put("itunes:name", "name");
         aliases.put("itunes:subtitle", "subtitle");
         aliases.put("itunes:summary", "description");
         aliases.put("feedinfo", "channel");
         aliases.put("fullitem", "content_encoded");
         aliases.put("homepage", "url");
         aliases.put("keywords", "category");
         aliases.put("dcterms:issued", "issued");
         aliases.put("managingeditor", "author");
         aliases.put("product", "item");
         aliases.put("producturl", "link");
         aliases.put("pubdate", "modified");
         aliases.put("published", "dcterms_created");
         aliases.put("rights", "copyright");
         aliases.put("tagline", "subtitle");
         aliases.put("uri", "url");
         aliases.put("webmaster", "publisher");
         aliases.put("wfw:comment", "wfw_comment");
         aliases.put("wfw:commentrss", "wfw_commentrss");
         aliases.put("xhtml_body", "body");
         aliases.put("updated", "modified");
         return aliases;
     }
 
     private static Map<String,String> createNamespaceAliases() {
         Map<String,String> aliases = new Hashtable<String,String>();
         // aliases.put("http://backend.userland.com/rss", "");
         // aliases.put("http://blogs.law.harvard.edu/tech/rss", "");
         // aliases.put("http://purl.org/rss/1.0/", "");
         // aliases.put("http://my.netscape.com/rdf/simple/0.9/", "");
         // aliases.put("http://example.com/newformat#", "");
         // aliases.put("http://example.com/necho", "");
         // aliases.put("http://purl.org/echo/", "");
         // aliases.put("uri/of/echo/namespace#", "");
         // aliases.put("http://purl.org/pie/", "");
         // aliases.put("http://purl.org/atom/ns#", "");
         // aliases.put("http://purl.org/rss/1.0/modules/rss091#", "");
 
         aliases.put("http://webns.net/mvcb/", "admin");
         aliases.put("http://purl.org/rss/1.0/modules/aggregation/", "ag");
         aliases.put("http://purl.org/rss/1.0/modules/annotate/", "annotate");
         aliases.put("http://media.tangent.org/rss/1.0/", "audio");
         aliases.put("http://backend.userland.com/blogChannelModule",
                 "blogChannel");
         aliases.put("http://web.resource.org/cc/", "cc");
         aliases.put("http://backend.userland.com/creativeCommonsRssModule",
                 "creativeCommons");
         aliases.put("http://purl.org/rss/1.0/modules/company", "co");
         aliases.put("http://purl.org/rss/1.0/modules/content/", "content");
         aliases.put("http://my.theinfo.org/changed/1.0/rss/", "cp");
         aliases.put("http://purl.org/dc/elements/1.1/", "dc");
         aliases.put("http://purl.org/dc/terms/", "dcterms");
         aliases.put("http://purl.org/rss/1.0/modules/email/", "email");
         aliases.put("http://purl.org/rss/1.0/modules/event/", "ev");
         aliases.put("http://postneo.com/icbm/", "icbm");
         aliases.put("http://purl.org/rss/1.0/modules/image/", "image");
         aliases.put("http://xmlns.com/foaf/0.1/", "foaf");
         aliases.put("http://freshmeat.net/rss/fm/", "fm");
         aliases.put("http://www.itunes.com/dtds/podcast-1.0.dtd", "itunes");
         aliases.put("http://example.com/dtds/podcast-1.0.dtd", "itunes");
         aliases.put("http://purl.org/rss/1.0/modules/link/", "l");
         aliases.put("http://madskills.com/public/xml/rss/module/pingback/",
                 "pingback");
         aliases.put("http://prismstandard.org/namespaces/1.2/basic/", "prism");
         aliases.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
         aliases.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
         aliases.put("http://purl.org/rss/1.0/modules/reference/", "ref");
         aliases.put("http://purl.org/rss/1.0/modules/richequiv/", "reqv");
         aliases.put("http://purl.org/rss/1.0/modules/search/", "search");
         aliases.put("http://purl.org/rss/1.0/modules/slash/", "slash");
         aliases.put("http://purl.org/rss/1.0/modules/servicestatus/", "ss");
         aliases.put("http://hacks.benhammersley.com/rss/streaming/", "str");
         aliases.put("http://purl.org/rss/1.0/modules/subscription/", "sub");
         aliases.put("http://purl.org/rss/1.0/modules/syndication/", "sy");
         aliases.put("http://purl.org/rss/1.0/modules/taxonomy/", "taxo");
         aliases.put("http://purl.org/rss/1.0/modules/threading/", "thr");
         aliases.put("http://purl.org/rss/1.0/modules/textinput/", "ti");
         aliases.put("http://madskills.com/public/xml/rss/module/trackback/",
                 "trackback");
        aliases.put("http://wellformedweb.org/CommentAPI/", "wfw");
         aliases.put("http://purl.org/rss/1.0/modules/wiki/", "wiki");
         aliases.put("http://schemas.xmlsoap.org/soap/envelope/", "soap");
         aliases.put("http://www.w3.org/1999/xhtml", "xhtml");
         aliases.put("http://www.w3.org/XML/1998/namespace", "xml");
         return aliases;
     }
 
     private Attributes atts = new AttributesImpl();
 
     private URI base;
 
     public boolean content = false;
 
     private String element;
 
     private boolean expectingText = false;
 
     private String language;
 
     private String localName;
 
     private String mode;
 
     private String namespace;
 
     private String qName;
 
     private StringBuilder text = new StringBuilder();
 
     private String type;
 
     private String uri;
 
     public State() {
     }
 
     public State(String uri, String localName, String qName) {
         this.uri = uri;
         this.localName = localName.toLowerCase();
         if (namespace_aliases.containsKey(this.uri.toLowerCase())) {
             this.namespace = (String) namespace_aliases.get(this.uri.toLowerCase());
         }
         this.element = aliasElement(this.namespace, this.localName);
         this.qName = qName;
     }
 
     public State(String uri, String localName, String qName, Attributes atts,
             State prev) {
         this.uri = uri;
         this.localName = localName.toLowerCase();
         this.qName = qName;
         this.atts = atts;
         if (namespace_aliases.containsKey(this.uri.toLowerCase())) {
             this.namespace = (String) namespace_aliases.get(this.uri.toLowerCase());
         }
 
         this.element = aliasElement(this.namespace, this.localName);
 
         this.type = this.getAttr("type", prev.type);
         this.mode = this.getAttr("mode", prev.mode);
         if (this.type == null || this.type.equals("")) {
             this.type = "text/plain";
         }
         this.language = this.getAttr("xml:lang", prev.getLanguage());
         this.setBase(this.getAttr("xml:base", prev.getBase()));
         if (this.isBaseRelative()) {
             this.resolveBaseWith(prev.getBase());
         }
         // log.debug(this);
 
     }
 
     public void addText(String str) {
         text.append(str);
     }
 
     private String aliasElement(String namespace, String element) {
         if (namespace != null && !namespace.equals("xhtml")) {
             element = namespace + ":" + element;
         }
         if (element_aliases.containsKey(element)) {
             return (String) element_aliases.get(element);
         }
         return element;
     }
 
     public String getAttr(String key) {
         return this.getAttr(key, null);
     }
 
     public String getAttr(String key, String default_value) {
         // TODO: remove this hack
         if (key.equals("type") && namespace != null
                 && namespace.equals("xhtml")) {
             return "application/xhtml+xml";
         }
         String ret = atts.getValue(key);
         if (ret == null) {
             ret = default_value;
         }
         log.trace("getAttr: " + key + " = '" + ret + "'");
         return ret;
     }
 
     public String getBase() {
         if (base != null) {
             return base.toString();
         } else {
             return null;
         }
 
     }
 
     public String getElement() {
         return element;
     }
 
     public String getLanguage() {
         return language;
     }
 
     public String getText() {
         return text.toString();
     }
 
     
     public String getType() {
         return type;
     }
 
     public String getUri() {
         return uri;
     }
 
     public String resolveUri(String uri) {
         if (base == null) {
             return uri;
         }
         if (uri == null) {
             return uri;
         }
 
         return base.resolve(uri).toString();
     }
     public boolean isBaseRelative() {
         if (this.base == null) { return false; }
         return !this.base.isAbsolute();
     }
     
     public void resolveBaseWith(String uri) {
         try {
             if (this.base != null && uri != null) {
                 URI real_base = new URI(uri);
                 this.base = real_base.resolve(this.base);
             }
         } catch (Exception e){
             log.warn("exception", e);
         }
     }
     
     public void setBase(String base) {
         if (base != null) {
             base = base.replaceAll("^([A-Za-z][A-Za-z0-9+-.]*://)(/*)(.*?)", "$1$3");
             try {
                 if (this.base != null) {
                     this.base = this.base.resolve(base);
                 } else {
                     
                     this.base = new URI(base);
                 }
             } catch (URISyntaxException e) {
                 log.warn(e);
                 try {
                     this.base = new URI("");
                 } catch (URISyntaxException ex) {
                     log.warn(ex);
                 }
             }
         }
     }
 
     public void setElement(String element) {
         this.element = element;
     }
 
     public void setLanguage(String language) {
         this.language = language;
     }
 
     public void setType(String type) {
         if (type.equals("text")) {
             this.type = "text/plain";
         } else if (type.equals("html")) {
             this.type = "text/html";
         } else if (type.equals("xhtml")) {
             this.type = "application/xhtml+xml";
         } else {
             this.type = type;
         }
     }
 
     public void setUri(String uri) {
         this.uri = uri;
     }
 
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("{");
         sb.append("element = '" + element + "', ");
         sb.append("mode = '" + mode + "', ");
         sb.append("type = '" + type + "', ");
         sb.append("base = '" + base + "', ");
         sb.append("language = '" + language + "', ");
         sb.append("namespace = '" + namespace + "', ");
         sb.append("uri = '" + uri + "', ");
         sb.append("qname = '" + qName + "', ");
         sb.append("localname = '" + localName + "', ");
         sb.append("text = '" + text + "'");
         sb.append("}");
         return sb.toString();
     }
 
     public boolean isExpectingText() {
         return expectingText;
     }
 
     public void setExpectingText(boolean expectingText) {
         this.expectingText = expectingText;
     }
 
     public Attributes getAttributes() {
         return atts;
     }
 
     public String getMode() {
         return mode;
     }
 
     public void setMode(String mode) {
         this.mode = mode;
     }
 }
