 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.template.parser;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.session.SessionManager;
 import org.wings.template.LabelTagHandler;
 import org.wings.template.TemplateSource;
 
 /**
  * <CODE>PageParser</CODE>
  * parses SGML markup'd pages and executes
  * <em>active</em> Tag. Returns its output
  * through a HttpServletResponse (given in a ParseContext).
  * Active Tags are handled with SpecialTagHandlers which
  * can be registered for a specific Tag.
  * <p/>
  * <p><h4>Error handling:</h4>
  * To simplify error detection and correction,
  * exceptions thrown by the <CODE>executeTag()</CODE>-methods of the
  * pluggable handlers (e.g. called servlets) are printed,
  * enclosed in comments ("&lt;!-- ... --&gt;"), in the HTML output.
  *
  * @author <A href="mailto:zeller@think.de">Henner Zeller</A>
  * @see javax.servlet.http.HttpServlet
  */
 
 public class PageParser {
     private final static Log log = LogFactory.getLog(PageParser.class);
 
     private static PageParser sharedInstance = null;
 
     /**
      * returns a singleton instance of this PageParser.
      * You usually want to use the singleton instance to make
      * use of a central cache.
      */
     public static PageParser getInstance() {
         if (sharedInstance == null) {
             synchronized (PageParser.class) {
                 if (sharedInstance == null)
                     sharedInstance = new PageParser();
             }
         }
 
         return sharedInstance;
     }
 
     /**
      * This Map contains the cached parsed
      * pages, saved in TemplateSourceInfo-Objects.
      * The key is the canonical name of the Data
      * Source.
      */
     private final Map/*<String, TemplateSourceInfo>*/ pages = new HashMap();
 
     /**
      * a Hashtable with key/value=tagname/handlerClass
      */
     private final Map/*<String,Class>*/ handlerClasses = new HashMap();
 
     /**
      * Constructs a new PageParser.
      */
     public PageParser() {
     }
 
     /**
      * Process a general DataStore representing a Template
      *
      * @param source  The template TemplateSource
      * @param context The context used while parsing; contains
      *                at least the HttpServletRequest and
      *                HttpServletResponse.
      * @see ParseContext
      * @see TemplateSource
      */
     public void process(TemplateSource source,
                         ParseContext context)
             throws IOException {
         interpretPage(source, getPageParts(source, context), context);
     }
 
     public Set<String> getContainedComponents(TemplateSource source, ParseContext context) throws IOException {
         getPageParts(source, context);
         String cName = source.getCanonicalName();
         TemplateSourceInfo sourceInfo = (TemplateSourceInfo)pages.get(cName);
         return sourceInfo.containedComponents;
     }
 
     public Map<String, Map<String, String>> getComponentProperties(TemplateSource source, ParseContext context) throws IOException {
         getPageParts(source, context);
         String cName = source.getCanonicalName();
         TemplateSourceInfo sourceInfo = (TemplateSourceInfo)pages.get(cName);
         return sourceInfo.componentProperties;
     }
 
     public Map getLabels(TemplateSource source) {
         String cName = source.getCanonicalName();
         if (cName == null)
             return null;
 
         TemplateSourceInfo sourceInfo = (TemplateSourceInfo) pages.get(cName);
         if (sourceInfo == null)
             return null;
 
         return sourceInfo.labels;
     }
 
     /**
      * register a handler for a specific tag (Class name).
      * Tags are case-insensitive.
      *
      * @param tagname          the name of the tag like 'MYSPECIALTAG' or 'SERVLET'
      * @param handlerClassName the <em>name of class</em> implementing the
      *                         action for this tag. This class must
      *                         implement the SpecialTagHandler
      *                         interface.
      * @throws ClassNotFoundException if the class with the specified
      *                                name is not found.
      */
     public void addTagHandler(String tagname, String handlerClassName)
             throws ClassNotFoundException {
         handlerClasses.put(tagname.toUpperCase(), Class.forName(handlerClassName));
     }
 
     /**
      * register a handler for a specific tag (Class).
      * Tags are case-insensitive.
      *
      * @param tagname      the name of the tag like 'MYSPECIALTAG' or 'SERVLET'
      * @param handlerClass the <em>class</em> implementing the
      *                     action for this tag. This class must
      *                     implement the SpecialTagHandler
      *                     interface.
      */
     public void addTagHandler(String tagname, Class handlerClass) {
         handlerClasses.put(tagname.toUpperCase(), handlerClass);
     }
 
     /**
      * @return Itearator of all Tags which are special to
      *         this PageParser
      */
     public Iterator getRegisteredTags() {
         return handlerClasses.keySet().iterator();
     }
 
     /**
      * If TemplateSource has changed or has not yet been loaded, load
      * it and chop into sections, storing result for future use.
      * Otherwise, return stored preprocessed page.
      *
      * @param source TemplateSource for which we want page section list
      * @return list of page sections, as described in parsePage().
      * @see #parsePage
      */
     private List getPageParts(TemplateSource source, ParseContext context)
             throws IOException {
         // first, check to see if we have cached version
         String cName = source.getCanonicalName();
         TemplateSourceInfo sourceInfo = null;
         if (cName != null)
             sourceInfo = (TemplateSourceInfo) pages.get(cName);
 
         /*
          * parse the page if it has changed or no cached
          * information is available.
          */
         if (sourceInfo == null ||
                 sourceInfo.lastModified != source.lastModified()) {
             // if no cached version, or modified, load
             sourceInfo = parsePage(source, context);
             if (cName != null)
                 pages.put(cName, sourceInfo);
         }
         return sourceInfo.parts;
     }
 
     /**
      * Scan through vector of page sections and build
      * output.
      * Read the static areas of the TemplateSource and copy them to the
      * output until the beginning of the next special tag. Invokes
      * the <CODE>executeTag()</CODE> Method for the tagand goes on with copying.
      * or invoking the servlets to which they refer.
      *
      * @param parts page sections, as provide by parsePage()
      * @see #parsePage
      */
     private void interpretPage(TemplateSource source, List parts, ParseContext context) throws IOException {
 
     	Writer out = new OutputStreamWriter(context.getOutputStream(), getStreamEncoding());
         Reader in = null;
         char[] buf = null;
 
         try {
             // input
             in = new InputStreamReader(source.getInputStream(), getStreamEncoding());
             long inPos = 0;
 
             /*
              * Get Copy Buffer.
              * If we allocate it here once and pass it to the
              * copy()-function we don't have to create and garbage collect
              * a buffer each time we call copy().
              *
              * REVISE: this should use a buffer Manager:
              * a queue which stores buffers. This
              * way the JVM doesn't have to garbage collect the buffers
              * created here, so we may use larger Buffers
              * here.
              */
             buf = new char[4096]; // Get buffer from Buffer Manager
 
             for (int i = 0; i < parts.size(); i++) {
                 /** <critical-path> **/
                 SpecialTagHandler part = (SpecialTagHandler) parts.get(i);
                 // copy TemplateSource content till the beginning of the Tag:
                 copy(in, out, part.getTagStart() - inPos, buf);
 
                 context.startTag(i);
                 try {
                     part.executeTag(context, in);
                 }
                         /*
                          * Display any Exceptions or Errors as
                          * comment in the page
                          */ catch (Throwable e) {
                     out.flush();
                     PrintWriter pout = new PrintWriter(out);
                     pout.println("<!-- ERROR: ------------");
                     e.printStackTrace(pout);
                     pout.println("-->");
                     pout.flush();
                 }
                 context.doneTag(i);
 
                 inPos = part.getTagStart() + part.getTagLength();
                 /** </critical-path> **/
             }
             // copy rest until end of TemplateSource
             copy(in, out, -1, buf);
         } finally {
             // clean up resouce: opened input stream
             if (in != null)
                 in.close();
             buf = null; // return buffer to Buffer Manager
         }
         out.flush();
     }
 
     /**
      * copies an InputStream to an OutputStream. copies max. length
      * bytes.
      *
      * @param in     The source reader
      * @param out    The destination writer
      * @param length number of bytes to copy; -1 for unlimited
      * @param buf    Buffer used as temporary space to copy
      *               block-wise.
      */
     private static void copy(Reader in, Writer out, long length, char[] buf) throws IOException {
         int len;
         boolean limited = (length >= 0);
         int rest = limited ? (int) length : buf.length;
         while (rest > 0 && (len = in.read(buf, 0, (rest > buf.length) ? buf.length : rest)) > 0) {
             out.write(buf, 0, len);
             if (limited) rest -= len;
         }
         out.flush();
     }
 
     /**
      * Open and read source, returning list of contents.
      * The returned vector will contain a list of
      * <CODE>SpecialTagHandler</CODE>s, containing the
      * position/length within the input source they are
      * responsible for.
      * This Vector is used within <CODE>interpretPage()</CODE>
      * to create the output.
      *
      * @param source source to open and process
      * @param context The context used while parsing; contains
      *                at least the HttpServletRequest and HttpServletResponse
      * @return TemplateSourceInfo containing page elements.
      *         <!-- see private <a href="#interpretPage">interpretPage()</a> -->
      */
     private TemplateSourceInfo parsePage(TemplateSource source, ParseContext context)
             throws IOException {
     	
         /*
          * read source contents. The SGMLTag requires
          * to read from a Reader which supports the
          * mark() operation so we need a BufferedReader
          * here.
          *
          * The PositionReader may be asked at which Position
          * it currently is (much like the java.io.LineNumberReader); this
          * is used to determine the exact position of the Tags in the
          * page to be able to loop through the fast copy/execute/copy
          * sequence in interpretPage().
          *
          * Since interpreting is operating on an InputStream which
          * copies and skip()s bytes, any source position count done here
          * assumes that sizeof(char) == sizeof(byte).
          * So we force the InputStreamReader to interpret the Stream's content
          * as ISO8859_1, because the localized default behaviour may
          * differ (e.g. UTF8 for which sizeof(char) != sizeof (byte)
          */
         PositionReader fin = null;
         // from JDK 1.1.6, the name of the encoding is ISO8859_1, but the old
         // value is still accepted.
         fin = new PositionReader(new BufferedReader(new InputStreamReader(source.getInputStream(), getStreamEncoding())));
         TemplateSourceInfo sourceInfo = new TemplateSourceInfo();
 
         try {
             // scan through page parsing SpecialTag statements
             sourceInfo.lastModified = source.lastModified();
             sourceInfo.parts = new ArrayList();
             sourceInfo.labels = new HashMap();
             long startPos;
             SGMLTag tag, endTag;
             long startTime = System.currentTimeMillis();
             do {
                 endTag = null;
                 startPos = fin.getPosition();
                 tag = new SGMLTag(fin, false);
                 if (tag.getName() != null) {
                     String upName = tag.getName().toUpperCase();
                     if (handlerClasses.containsKey(upName)) {
                         SpecialTagHandler handler = null;
                         try {
                             Class handlerClass = (Class) handlerClasses.get(upName);
                             handler = (SpecialTagHandler) handlerClass.newInstance();
 
                             endTag = handler.parseTag(context, fin, startPos, tag);
                         } catch (Exception e) {
                             log.warn("Exception",e);
                         }
                         if (endTag != null) {
                             if ("LABEL".equals(upName)) {
                                 LabelTagHandler labelHandler = (LabelTagHandler) handler;
                                 sourceInfo.labels.put(labelHandler.getFor(), labelHandler.getContent());
                             }
                             sourceInfo.parts.add(handler);
                         }
                     }
                 }
             } while (!tag.finished());
             sourceInfo.containedComponents = context.getContainedComponents();
             sourceInfo.componentProperties = context.getComponentProperties();
             /***
              sourceInfo.parseTime = System.currentTimeMillis() - startTime;
              System.err.println ("PageParser: parsing '" +
              source.getCanonicalName() + "' took " +
              sourceInfo.parseTime + "ms for " +
              sourceInfo.parts.size() + " handlers");
              ***/
         } finally {
             if (fin != null) fin.close();
         }
         return sourceInfo;
     }
 
     /**
      * Source info holds the parse information for
      * a TemplateSource .. and some statistical stuff which
      * may be interesting for administrative
      * frontends
      */
     private static final class TemplateSourceInfo {
         ArrayList parts;
         Map labels;
         long lastModified;
         Set<String> containedComponents;
         Map<String, Map<String, String>> componentProperties;
 
 
         public TemplateSourceInfo() {}
     }
     
     /**
      * Returns the encoding of the streams.
      * 
      * @return The encoding of the streams.
      */
    private final String getStreamEncoding() {

     	String encoding = (String) SessionManager.getSession().getProperty("wings.template.layout.encoding");
     	if (encoding == null || "".equals(encoding)) {
     		encoding = "UTF-8";
     	}
     	return encoding;
     }
 }
