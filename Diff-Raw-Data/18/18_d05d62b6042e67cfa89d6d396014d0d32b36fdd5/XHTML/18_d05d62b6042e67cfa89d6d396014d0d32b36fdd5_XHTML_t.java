 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package util.xml;
 
 import java.io.*;
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.w3c.dom.*;
 
 /** Class creates XHTML format data from DOM nodes. */
 public class XHTML
 {
 	// List of elements that must be minimized (no close tags) and may contain
 	// no content
 	private static Collection<String> sMinimize=new HashSet<String>(Arrays.asList(
 			new String[] {"br","img","hr","meta","link","input"}));
 
 	private static String USER_AGENT = "User-Agent";
 
 	private static String MSIE_9 = "MSIE 9.";
 
 	/**
 	 * Saves an XHTML element to a Writer.
 	 * <p>
 	 * Includes the junk that goes at the start of the document, and replaces
 	 * the parent element with an &lt;html&gt; tag that contains the appropriate
 	 * namespace information.
 	 * @param d Document (The root element will be discarded and replaced with
 	 *   an &lt;html&gt; element, but attributes on it will be retained)
 	 * @param w Writer to receive data
 	 * @param bIncludeXMLDeclaration If true, includes... you know the drill
 	 * @param sLang Default language of page
 	 * @throws IOException If the Writer reports any errors.
 	 */
 	public static void saveFullDocument(Document d,Writer w,
 		boolean bIncludeXMLDeclaration,String sLang) throws IOException
 	{
 		saveFullDocument(d.getDocumentElement(),w,bIncludeXMLDeclaration,sLang);
 
 	}
 
 	/**
 	 * Saves an XHTML element to a Writer.
 	 * <p>
 	 * Includes the junk that goes at the start of the document, and replaces
 	 * the parent element with an &lt;html&gt; tag that contains the appropriate
 	 * namespace information.
 	 * @param e Parent element (any attributes etc. on this will be retained,
 	 *   but it will be replaced with an &lt;html&gt; element)
 	 * @param w Writer to receive data
 	 * @param bIncludeXMLDeclaration If true, includes... you know the drill
 	 * @param sLang Default language of page
 	 * @throws IOException If the Writer reports any errors.
 	 */
 	public static void saveFullDocument(Element e,Writer w,
 	  boolean bIncludeXMLDeclaration,String sLang) throws IOException
 	{
 		// Write initial junk
 		writePrologue(w, bIncludeXMLDeclaration, sLang,e);
 
 		// Write contents
 		for(Node nChild=e.getFirstChild();nChild!=null;nChild=nChild.getNextSibling())
 		{
 			save(nChild,w);
 		}
 
 		// Close tag
 		w.write("</html>");
 	}
 
 	/** Write the prologue of an XML file, up to the opening html tag (you must
 	 * write the closing /html tag).
 	 * @param w Writer
 	 * @param bIncludeXMLDeclaration True to include the XML declaration
 	 * @param sLang Language
 	 * @param eElement Element for copying attributes from (may be null)
 	 * @throws IOException Any error
 	 */
 	public static void writePrologue(
 		Writer w,
 		boolean bIncludeXMLDeclaration,
 		String sLang,
 		Element eElement)
 		throws IOException
 	{
 		if(bIncludeXMLDeclaration)
 		  w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
 		w.write("<!DOCTYPE html\n"+
 			"	PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"+
 			"	\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"+
 			"<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='"+sLang+"' lang='"+sLang+"'");
 
 		// Copy attributes
 		if(eElement!=null)
 		{
 			NamedNodeMap nnm=eElement.getAttributes();
 			for(int i=0;i<nnm.getLength();i++)
 			{
 				Attr a=(Attr)nnm.item(i);
 				w.write(
 					" "+a.getName()+"='"+escape(a.getValue(),ESCAPE_ATTRSQ)+"'");
 			}
 		}
 
 		w.write(">\n");
 	}
 
 
 	/**
 	 * Saves an XHTML node to a Writer. Does not include the junk that goes at
 	 * the start of a document, e.g. doctype etc.
 	 * @param n Node to save
 	 * @param w Writer to which node should be saved
 	 * @throws IOException If there is an I/O error (no, really?)
 	 */
 	public static void save(Node n,Writer w) throws IOException
 	{
 		// We had some NPEs reported; this code should allow us to narrow down
 		// exactly what is null (and how!)
 		if(n==null) throw new NullPointerException();
 		if(w==null) throw new NullPointerException();
 
 		switch(n.getNodeType())
 		{
 			case Node.ELEMENT_NODE:
 			{
 				// Write start of element
 				Element e=(Element)n;
 				w.write("<"+e.getTagName());
 
 				// Write attributes if any
 				NamedNodeMap nnm=e.getAttributes();
 				for(int iAttribute=0;iAttribute<nnm.getLength();iAttribute++)
 				{
 					Attr a=(Attr)nnm.item(iAttribute);
 					String sValue=a.getValue();
 
 					// Try to avoid escaping things in the attribute if possible
 					if(sValue.indexOf('\"')!=-1)
 						w.write(" "+a.getName()+"=\'"+escape(sValue,ESCAPE_ATTRSQ)+"\'");
 					else
 						w.write(" "+a.getName()+"=\""+escape(sValue,ESCAPE_ATTRDQ)+"\"");
 				}
 
 				// Check for minimizing
 				if(sMinimize.contains(e.getTagName()))
 				{
 					// Do not write contents of minimized elements
 					w.write(" />");
 				}
 				else
 				{
 					w.write(">");
 
 					// Write contents
 					for(Node nChild=e.getFirstChild();nChild!=null;nChild=nChild.getNextSibling())
 					{
 						save(nChild,w);
 					}
 
 					// Write close tag
 					w.write("</"+e.getTagName()+">");
 				}
 			} break;
 			case Node.TEXT_NODE:
 			case Node.CDATA_SECTION_NODE:
 			{
 				w.write(escape(n.getNodeValue(),ESCAPE_TEXT));
 			} break;
 			case Node.COMMENT_NODE:
 			{
 				w.write("<!-- "+n.getNodeValue()+" -->");
 			} break;
 			case Node.DOCUMENT_NODE:
 			{
 				save(((Document)n).getDocumentElement(),w);
 			} break;
 			default:
 			{
 				w.write("<!-- [UNEXPECTED XML] "+n.getNodeType()+": "+n.getNodeValue()+" -->");
 			} break;
 		}
 	}
 
 	/**
 	 * Saves an XHTML node to a string. Does not include the junk that goes at
 	 * the start of a document, e.g. doctype etc.
 	 * @param n Node to save
 	 * @return The string representation of the XML with root n.
 	 * @throws IOException If there is an I/O error (no, really?)
 	 */
 	public static String saveString(Node n) throws IOException
 	{
 		StringWriter sw=new StringWriter();
 		save(n,sw);
 		return sw.toString();
 	}
 
 	/** Escape control: text content */
 	public final static int ESCAPE_TEXT=0;
 	/** Escape control: within a single-quoted attribute */
 	public final static int ESCAPE_ATTRSQ=1;
 	/** Escape control: within a double-quoted attribute */
 	public final static int ESCAPE_ATTRDQ=2;
 
 	/**
 	 * Escapes a string using HTML entities where necessary (use this when writing
 	 * HTML 'by hand' rather than using save methods).
 	 * @param sSource String that may contain triangle brackets and quotes
 	 * @param iEscapeType Set to appropriate ESCAPE_xxx constant
 	 * @return String that will contain HTML entities instead, and has illegal
 	 *   control characters stripped
 	 */
 	public static String escape(String sSource,int iEscapeType)
 	{
 		// Optimisation: don't bother escaping strings that contain no dangerous
 		// characters. This improves performance significantly.
 		int iSkipCheck;
 		for(iSkipCheck=0;iSkipCheck<sSource.length();iSkipCheck++)
 		{
 			char c=sSource.charAt(iSkipCheck);
 			// This if (which is sort of approximate) is faster than a humungous switch
 			if( (c<=0x27 && c!=0x20) || c==0x3c) break;
 		}
 		if(iSkipCheck==sSource.length()) return sSource;
 
 		StringBuffer sbOutput=new StringBuffer();
 		for(int i=0;i<sSource.length();i++)
 		{
 			char c=sSource.charAt(i);
 
 			switch(c)
 			{
 				case '&' : sbOutput.append("&amp;"); break;
 				case '<' : sbOutput.append("&lt;"); break;
 				case '\"' :
 				{
 					if(iEscapeType==ESCAPE_ATTRDQ)
 						sbOutput.append("&quot;");
 					else
 						sbOutput.append(c);
 				} break;
 				case '\'' :
 				{
 					if(iEscapeType==ESCAPE_ATTRSQ)
 						sbOutput.append("&#39;");
 					else
 						sbOutput.append(c);
 				} break;
 				case 9:
 				case 10:
 				case 13:
 				{
 					sbOutput.append(c);
 				} break;
 				default:
 				{
 					// Skip altogether control characters that are forbidden by XML
 					if(c>=32) sbOutput.append(c);
 				} break;
 			}
 		}
 
 		return sbOutput.toString();
 	}
 
 	/**
 	 * Sets the response MIME type based on browser capabilities.
 	 * @param request Request (used to get Accept header)
 	 * @param response Response (MIME type will be set)
 	 * @return True if really using XHTML, false if HTML
 	 */
 	public static boolean setContentType(HttpServletRequest request,
 		HttpServletResponse response) {
 		// This is not the correct way to parse the Accept header
 		boolean bXHTML;
 		String sAccept=request.getHeader("Accept");
 		bXHTML=sAccept!=null && (sAccept.indexOf("application/xhtml+xml")!=-1);
 
 		if(bXHTML) {
 			if (!isIE9(request)) {
 				response.setContentType("application/xhtml+xml");
			} else {
				response.setContentType("text/html");
 			}
 		} else {
 			response.setContentType("text/html");
 		}
 		response.setCharacterEncoding("UTF-8");
 		return bXHTML;
 	}
 
 	/**
 	 * Simply checks to identify if the request is from an IE9* browser.
 	 * @param request
 	 * @param response
 	 * @author Trevor Hinson
 	 */
 	public static boolean isIE9(HttpServletRequest request) {
 		boolean isIE9 = false;
 		if (null != request) {
 			String userAgent = request.getHeader(USER_AGENT);
 			if (StringUtils.isNotEmpty(userAgent)
 				? userAgent.contains(MSIE_9) : false) {
 				isIE9 = true;
 			}
 		}
 		return isIE9;
 	}
 
 	/**
 	 * Outputs an XHTML document in response to a servlet request.
 	 * @param d Document to output
 	 * @param request Servlet request
 	 * @param response Servlet response
 	 * @param sLang Language code e.g. "en"
 	 * @throws IOException In event of I/O errors
 	 */
 	public static void output(Document d,HttpServletRequest request,
 		HttpServletResponse response, String sLang) throws IOException {
 		if (isIE9(request)) {
 			response.addHeader("X-UA-Compatible", "IE=8");
 		}
 		XHTML.setContentType(request,response);
 		PrintWriter pw=response.getWriter();
 		XHTML.saveFullDocument(d,pw,false,sLang);
 		pw.close();
 	}
 }
