 /*
  * Copyright (c) 1997-1999 The Java Apache Project.  All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. All advertising materials mentioning features or use of this
  *    software must display the following acknowledgment:
  *    "This product includes software developed by the Java Apache 
  *    Project for use in the Apache JServ servlet engine project
  *    (http://java.apache.org/)."
  *
  * 4. The names "Apache JServ", "Apache JServ Servlet Engine" and 
  *    "Java Apache Project" must not be used to endorse or promote products 
  *    derived from this software without prior written permission.
  *
  * 5. Products derived from this software may not be called "Apache JServ"
  *    nor may "Apache" nor "Apache JServ" appear in their names without 
  *    prior written permission of the Java Apache Project.
  *
  * 6. Redistributions of any form whatsoever must retain the following
  *    acknowledgment:
  *    "This product includes software developed by the Java Apache 
  *    Project for use in the Apache JServ servlet engine project
  *    (http://java.apache.org/)."
  *    
  * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
  * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Java Apache Group. For more information
  * on the Java Apache Project and the Apache JServ Servlet Engine project,
  * please see <http://java.apache.org/>.
  *
  */
 
 package org.wings.template.parser;
 
 import java.io.*;
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 /**
  * <CODE>PageParser</CODE> 
  * parses SGML markup'd pages and executes
  * <em>active</em> Tag. Returns its output
  * through a HttpServletResponse (given in a ParseContext).
  * Active Tags are handled with SpecialTagHandlers which
  * can be registered for a specific Tag.
  *
  * <p><h4>Error handling:</h4>
  * To simplify error detection and correction,
  * exceptions thrown by the <CODE>executeTag()</CODE>-methods of the
  * pluggable handlers (e.g. called servlets) are printed,
  * enclosed in comments ("&lt;!-- ... --&gt;"), in the HTML output.
  *
  * @author Roger Zeng
  * @author Tim Williams
  * @author current maintainer <A href="mailto:zeller@think.de">Henner Zeller</A>
  * @version $Revision$ $Date$
  * @see javax.servlet.http.HttpServlet
  */
 
 public class PageParser {
 
     /**
      * Source info holds the parse information for
      * a DataSource .. and some statistical stuff which 
      * may be interesting for administrative
      * frontends
      */
     private final class DataSourceInfo {
 	Vector parts;
 	long   lastModified;
 	long   parseTime;
	public DataSourceInfo () {}
     }
 
     /**
      * This hashtable contains the cached parsed
      * pages, saved in DataSourceInfo-Objects.
      * The key is the canonical name of the Data
      * Source.
      * @see DataSource
      * This may be considered to be replaced by 
      * a WeakHashMap when JDK1.2 is available on 
      * all Platforms
      */
     private Hashtable pages = null;
 
     /**
      * a Hashtable with key/value=tagname/handlerClass
      */
     private Hashtable handlerClasses = null;
 
     /** 
      * Constructs a new PageParser.
      */
     public PageParser () {
 	pages = new Hashtable();
 	handlerClasses = new Hashtable();
     }
 
     /**
      * Process a general DataStore representing a Template
      * @param source        The template DataSource
      * @param ParseContext  The context used while parsing; contains
      *                      at least the HttpServletRequest and
      *                      HttpServletResponse.
      * @see ParseContext
      * @see DataSource
      */
     public void process (DataSource source,
 			 ParseContext context)
 	throws IOException {
 	interpretPage(source, getPageParts(source, context), context);
     }
 
     /**
      * Processes a file.
      * @param file          The file containing SGML markup
      * @param ParseContext  The context used while parsing; contains
      *                      at least the HttpServletRequest and
      *                      HttpServletResponse.
      * @see ParseContext
      */
     public void process (File file,
 			 ParseContext context) 
 	throws IOException {
 	process (new FileDataSource(file), context);
     }
 
     /**
      * register a handler for a specific tag (Class name).
      * Tags are case-insensitive.
      * @param tagname the name of the tag like 'MYSPECIALTAG' or 'SERVLET'
      * @param handlerClassName the <em>name of class</em> implementing the
      *                         action for this tag. This class must
      *                         implement the SpecialTagHandler
      *                         interface.
      * @exception ClassNotFoundException if the class with the specified
      *                                   name is not found.
      */
     public void addTagHandler (String tagname, String handlerClassName) 
 	throws ClassNotFoundException {
 	handlerClasses.put (tagname.toUpperCase(),
 			    Class.forName (handlerClassName));
     }
 
     /**
      * register a handler for a specific tag (Class).
      * Tags are case-insensitive.
      * @param tagname the name of the tag like 'MYSPECIALTAG' or 'SERVLET'
      * @param handlerClass the <em>class</em> implementing the
      *                     action for this tag. This class must
      *                     implement the SpecialTagHandler
      *                     interface.
      */
     public void addTagHandler (String tagname, Class handlerClass) {
 	handlerClasses.put (tagname.toUpperCase(), handlerClass);
     }
 
     /**
      * @return Enumeration of all Tags which are special to
      *         this PageParser
      */
     public Enumeration getRegisteredTags () {
 	return handlerClasses.keys();
     }
 
     /**
      * If DataSource has changed or has not yet been loaded, load
      * it and chop into sections, storing result for future use.
      * Otherwise, return stored preprocessed page.
      * @param source DataSource for which we want page section list
      * @return list of page sections, as described in parsePage().
      * @see #parsePage
      */
     private Vector getPageParts (DataSource source, ParseContext context)
 	throws IOException {
 	// first, check to see if we have cached version
 	String cName = source.getCanonicalName();
 	DataSourceInfo sourceInfo = null;
 	if (cName != null) 
 	    sourceInfo = (DataSourceInfo) pages.get(cName);
       
 	/*
 	 * parse the page if it has changed or no cached 
 	 * information is available.
 	 */
 	if (sourceInfo == null   ||
 	    sourceInfo.lastModified != source.lastModified()) {
 	    // if no cached version, or modified, load
 	    sourceInfo = parsePage (source, context);
 	    if (cName != null)
 		pages.put(cName, sourceInfo);
 	}
 	return sourceInfo.parts;
     }
 
     /**
      * Scan through vector of page sections and build
      * output. 
      * Read the static areas of the DataSource and copy them to the 
      * output until the beginning of the next special tag. Invokes
      * the <CODE>executeTag()</CODE> Method for the tag
      * and goes on with copying.
      * or invoking the servlets to which they refer.
      * @param parts page sections, as provide by parsePage()
      * @see #parsePage
      */
     private void interpretPage(DataSource source, 
 			       Vector parts, ParseContext context)
 	throws IOException {
       
 	OutputStream out = context.getOutputStream();
 	InputStream inStream = null;
 	byte buf[] = null;
       
 	try {
 	    // input
 	    inStream = source.getInputStream();
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
 	    buf = new byte[4096]; // Get buffer from Buffer Manager
 	  
 	    for (int i = 0; i < parts.size(); i++) {
 		/** <critical-path> **/
 		SpecialTagHandler part = (SpecialTagHandler) parts.elementAt(i);
 		// copy DataSource content till the beginning of the Tag:
 		copy (inStream, out, part.getTagStart()-inPos, buf);
 	      
 		context.startTag (i);
 		try{
 		    part.executeTag (context);
 		}
 		/*
 		 * Display any Exceptions or Errors as
 		 * comment in the page
 		 */
 		catch (Throwable e) { 
 		    out.flush();
 		    PrintWriter pout = new PrintWriter(out);
 		    pout.println("<!-- ERROR: ------------");
 		    e.printStackTrace(pout);
 		    pout.println("-->");
 		    pout.flush();
 		}
 		context.doneTag (i);
 	      
 		// skip the <SPECIAL_TAG> ... </SPECIAL_TAG> area:
 		inStream.skip(part.getTagLength());
 		inPos = part.getTagStart()+part.getTagLength();
 		/** </critical-path> **/
 	    }
 	    // copy rest until end of DataSource
 	    copy (inStream, out, -1, buf);
 	}
 	finally {
 	    // clean up resouce: opened input stream
 	    if (inStream != null)
 		inStream.close();
 	    buf = null; // return buffer to Buffer Manager
 	}
 	out.flush();
     }
 
 
     /**
      * copies an InputStream to an OutputStream. copies max. length
      * bytes.
      * @param in     The source stream
      * @param out    The destination stream
      * @param length number of bytes to copy; -1 for unlimited
      * @param buf    Buffer used as temporary space to copy
      *               block-wise.
      */
     private static void copy(InputStream in, OutputStream out, long length,
 			     byte buf[]) 
 	throws IOException {
 	int len;
 	boolean limited = (length >= 0);
 	int rest = limited ? (int) length : buf.length;
 	while( rest > 0 &&
 	       (len = in.read(buf, 0, 
 			      (rest > buf.length) ? buf.length : rest)) > 0) {
 	    out.write(buf, 0, len);
 	    if (limited) rest -= len;
 	}
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
      * @param souce source to open and process
      * @return DataSourceInfo containing page elements.
      * <!-- see private <a href="#interpretPage">interpretPage()</a> -->
      */
     private DataSourceInfo parsePage (DataSource source, ParseContext context)
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
 	fin = new PositionReader (new BufferedReader (new InputStreamReader (source.getInputStream(),"8859_1")));
 	DataSourceInfo sourceInfo = new DataSourceInfo();
        
 	try {
 	    // scan through page parsing SpecialTag statements
 	    sourceInfo.lastModified = source.lastModified();
 	    sourceInfo.parts = new Vector();
 	    long startPos;
 	    SGMLTag tag, endTag;
 	    long StartTime = System.currentTimeMillis();
 	    do {
 		endTag = null;
 		startPos = fin.getPosition();
 		tag = new SGMLTag(fin, false);
 		if (tag.getName() != null) {
 		    String upName = tag.getName().toUpperCase();
 		    if (handlerClasses.containsKey(upName)) {
 			SpecialTagHandler handler = null;
 			try {
 			    Class handlerClass;
 			    
 			    handlerClass = (Class)handlerClasses.get(upName);
 			    handler= (SpecialTagHandler) handlerClass
 				.newInstance();
 			    endTag = handler.readTag(context,fin,startPos,tag);
 			}
 			catch (Exception e) {
 			    System.err.println (e.getMessage());
 			}
 			if (endTag != null) {
 			    sourceInfo.parts.addElement (handler);
 			}
 		    }
 		}
 	    } 
 	    while (!tag.finished());
 	    sourceInfo.parseTime = System.currentTimeMillis() - StartTime;
 	    /***
 		System.err.println ("PageParser: parsing '" + 
 		source.getCanonicalName() + "' took " + 
 		sourceInfo.parseTime + "ms for " +
 		sourceInfo.parts.size() + " handlers");
 	    ***/
 	}
 	finally {
 	    if (fin != null) fin.close();
 	}
 	return sourceInfo;
     }
 }
 
 /* 
  * Local variables:
  * c-basic-offset: 4
  * End:
  */
 
