 /*
  * Copyright (c) 2003, Mikael Stldal
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * 3. Neither the name of the author nor the names of its contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *
  * Note: This is known as "the modified BSD license". It's an approved
  * Open Source and Free Software license, see
  * http://www.opensource.org/licenses/
  * and
  * http://www.gnu.org/philosophy/license-list.html
  */
 
 package nu.staldal.lsp.servlet;
 
 import java.io.*;
 import java.util.*;
 
 import org.xml.sax.*;
 import javax.xml.parsers.*;
 import javax.xml.transform.*;
 import javax.xml.transform.sax.*;
 import javax.xml.transform.stream.StreamResult;
 
 import javax.servlet.*;
 
 import nu.staldal.util.Utils;
 import nu.staldal.xmlutil.ContentHandlerFixer;
 import nu.staldal.lsp.*;
 
 
 /**
  * Handle execution of LSP pages in a Servlet environment.
  * This class is thread-safe.
  *
  * There is one instance of LSPManager per {@link javax.servlet.ServletContext},
  * use the {@link #getInstance} method to obtain it.
  */
 public class LSPManager
 {
 	private final ServletContext context;
 	private final ClassLoader servletClassLoader;
 	
 	private final Map lspPages;
 	
 	private final SAXParserFactory spf;
 	private final SAXTransformerFactory tfactory;
 	
 
 	/**
 	 * Output type XML.
 	 */
 	public static final String XML = "xml";
 		
 	/**
 	 * Output type HTML.
 	 */
 	public static final String HTML = "html";
 		
 	/**
 	 * Output type XHTML.
 	 */
 	public static final String XHTML = "xhtml";
 		
 	/**
 	 * Output type TEXT.
 	 */
 	public static final String TEXT = "text";		
 
 	
 	/**
 	 * Obtain the the LSPManager instance for the given 
 	 * {@link javax.servlet.ServletContext}. Creates a new instance 
 	 * if nessecary.
 	 *
 	 * @param  context  the {@link javax.servlet.ServletContext}
 	 * @param  servletClassLoader  the {@link java.lang.ClassLoader} 
 	 *                             used to load LSPPages, use
 	 *                             <code>getClass().getClassLoader()</code> 
 	 *                             on the Servlet
 	 *
 	 * @return  the LSPManager instance for the given {@link javax.servlet.ServletContext} 
 	 */
 	public static LSPManager getInstance(ServletContext context, 
 										 ClassLoader servletClassLoader)
 	{
 		LSPManager manager = 
 			(LSPManager)context.getAttribute(LSPManager.class.getName());
 		
 		if (manager == null)
 		{
 			manager = new LSPManager(context, servletClassLoader);
 			context.setAttribute(LSPManager.class.getName(), manager);
 		}
 		
 		return manager;
 	}
 	
 	
 	private LSPManager(ServletContext context, ClassLoader servletClassLoader)
 	{
 		this.context = context;
 		this.servletClassLoader = servletClassLoader;
 		
 		lspPages = Collections.synchronizedMap(new HashMap());
 		
 		spf = SAXParserFactory.newInstance();
 		spf.setNamespaceAware(true);
 		spf.setValidating(false);
 
 		TransformerFactory tf = TransformerFactory.newInstance();
         if (!(tf.getFeature(SAXTransformerFactory.FEATURE)
               	&& tf.getFeature(StreamResult.FEATURE)))
         {
             throw new Error("The transformer factory "
                 + tf.getClass().getName() + " doesn't support SAX");
         }            
 		tfactory = (SAXTransformerFactory)tf;
 	}
 
 
 	/**
 	 * Get the default Content-Type for a givent output type.
 	 *
 	 * @param outputType  how to serialize the page; XML, HTML, XHTML or TEXT
 	 */	
 	public String defaultContentType(String outputType)
 	{
 		String contentType;
 		
 		if (outputType.equals(HTML))
 			contentType = "text/html; charset=ISO-8859-1";
 		else if (outputType.equals(XML))
 			contentType = "text/xml; charset=UTF-8";
 		else if (outputType.equals(XHTML))
 			contentType = "application/xhtml+xml; charset=ISO-8859-1";
 		else if (outputType.equals(TEXT))
 			contentType = "text/plain; charset=ISO-8859-1";
 		else
 			contentType = null;
 		
 		return contentType;
 	}
 	
 	
 	/**
 	 * The method executes an LSP page and write the result to a 
 	 * {@link javax.servlet.ServletResponse}. 
 	 * You should set Content-Type on the response before using this method.
 	 *
 	 * @see #defaultContentType
 	 *
  	 * @param thePage     the LSP page
 	 * @param lspParams   parameters to the LSP page
 	 * @param response    the {@link javax.servlet.ServletResponse}
 	 * @param outputType  how to serialize the page; XML, HTML, XHTML or TEXT
 	 * @param doctypePublic the XML DOCTYPE PUBLIC (<code>null</code> for default)
 	 * @param doctypeSystem the XML DOCTYPE SYSTEM (<code>null</code> for default)
 	 */	
 	public void executePage(LSPPage thePage, Map lspParams, 
 							ServletResponse response, String outputType,
 							String doctypePublic, String doctypeSystem)
 		throws SAXException, IOException
 	{
 		OutputStream out = response.getOutputStream();
 		
 		ContentHandler sax;						
 		try {
 			TransformerHandler th = tfactory.newTransformerHandler();
 			th.setResult(new StreamResult(out));
 		
 			Transformer trans = th.getTransformer();
 			
 			Properties outputProperties = new Properties();
 			outputProperties.setProperty(OutputKeys.METHOD, outputType);				
 			outputProperties.setProperty(OutputKeys.ENCODING, 
 				response.getCharacterEncoding());
 			if (outputType.equals(HTML))
 			{
 				outputProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC,
 					"-//W3C//DTD HTML 4.01 Transitional//EN");
 				outputProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM,
 						"http://www.w3.org/TR/html4/loose.dtd");				
 			}
 			else if (outputType.equals(XHTML))
 			{
 				outputProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC,
 					"-//W3C//DTD XHTML 1.0 Transitional//EN");
 				outputProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM,
 					"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
 			}
 			if (doctypePublic != null)
 				outputProperties.setProperty(OutputKeys.DOCTYPE_PUBLIC,
 					doctypePublic);
 			if (doctypeSystem != null)
 				outputProperties.setProperty(OutputKeys.DOCTYPE_SYSTEM,
 					doctypeSystem);
 			trans.setOutputProperties(outputProperties);
 			
 			boolean isHtml = outputType.equals(HTML);
 				
 			sax = new ContentHandlerFixer(th, !isHtml, isHtml);
 		}
 		catch (TransformerConfigurationException e)
 		{
 			throw new SAXException(e.getMessage());
 		}
 					
 		sax.startDocument();
 		thePage.execute(sax,
 					new URLResolver() {
 						public void resolve(String url, ContentHandler ch) 
 							throws IOException, SAXException
 						{
 							getFileAsSAX(url, ch);	
 						}
 					},
 					lspParams, context);
 		sax.endDocument();
     }
 	
 
 	/**
 	 * Get the {@link nu.staldal.lsp.LSPPage} instance for a given page name.
 	 *
 	 * @param pageName  the name of the LSP page
 	 *
 	 * @return <code>null</code> if the given page name is not found
 	 */
 	public LSPPage getPage(String pageName)
 	{
 		LSPPage page = (LSPPage)lspPages.get(pageName);
 		
 		if (page == null)
 		{
 			page = loadPage(pageName);
 		}
 		
 		return page;
 	}
 	
 	
 	/**
 	 * Get a {@link javax.servlet.RequestDispatcher} for a given page name.
 	 * Using the default Content-Type and DOCTYPE for the given output type.
      *
 	 * The attributes in the {@link javax.servlet.ServletRequest} object
 	 * will be used as parameters to the LSP page.
 	 *
  	 * @param pageName    the name of the LSP page
 	 * @param outputType  how to serialize the page; XML, HTML, XHTML or TEXT
 	 *
 	 * @return <code>null</code> if the LSP page cannot be found
 	 */
 	public RequestDispatcher getRequestDispatcher(String pageName, 
 												  String outputType)
 	{
 		return getRequestDispatcher(pageName, outputType, null, null, null);
 	}
 
 	
 	/**
 	 * Get a {@link javax.servlet.RequestDispatcher} for a given page name.
 	 *
 	 * The attributes in the {@link javax.servlet.ServletRequest} object
 	 * will be used as parameters to the LSP page.
      *
  	 * @param pageName  the name of the LSP page
 	 * @param outputType  how to serialize the page; XML, HTML, XHTML or TEXT
 	 * @param contentType the MIME Content-Type (<code>null</code> for default)
 	 * @param doctypePublic the XML DOCTYPE PUBLIC (<code>null</code> for default)
 	 * @param doctypeSystem the XML DOCTYPE SYSTEM (<code>null</code> for default)
 	 *
 	 * @return <code>null</code> if the LSP page cannot be found
 	 */
 	public RequestDispatcher getRequestDispatcher(String pageName, 
 												  String outputType,
 												  String contentType,
 												  String doctypePublic,
 												  String doctypeSystem)
 	{
 		LSPPage page = getPage(pageName);
 			
 		if (page == null)
 		{
 			context.log("Unable to find LSP page: " + pageName);
 			return null;
 		}
 		
 		return new LSPRequestDispatcher(this, page, outputType, contentType,
 			doctypePublic, doctypeSystem);
 	}
 
 
 	/**
 	 * @return <code>null</code> if not found.
 	 */
 	private LSPPage loadPage(String pageName)
 	{
 		try {
 			Class pageClass = Class.forName("_LSP_"+pageName, true, 
 				servletClassLoader);
 
 			LSPPage page = (LSPPage)pageClass.newInstance();
 			
 			lspPages.put(pageName, page);			
 			
 		  	return page;
 		}
 		catch (ClassNotFoundException e)
 		{
 			return null;
 		}				
 		catch (InstantiationException e)
 		{
 			context.log("Invalid LSP page: " + pageName, e);
 			return null;
 		}				
 		catch (IllegalAccessException e)
 		{
 			context.log("Invalid LSP page: " + pageName, e);
 			return null;
 		}				
 		catch (VerifyError e)
 		{
 			context.log("Invalid LSP page: " + pageName, e);
 			return null;
 		}				
 	}
 
 
 	private void getFileAsSAX(String url, ContentHandler ch)
 		throws SAXException, IOException
 	{
 		InputSource is;
 		
 		if (Utils.absoluteURL(url))
 		{
 			is = new InputSource(url);
 		}
 		else if (Utils.pseudoAbsoluteURL(url))
 		{
 			InputStream istream = context.getResourceAsStream(url);
 			if (istream == null) throw new FileNotFoundException(url);
 			is = new InputSource(istream);
 		}
 		else // relative URL 	
 		{
 			InputStream istream = context.getResourceAsStream(url); 
 			if (istream == null) throw new FileNotFoundException(url);
 			is = new InputSource(istream);
 		}
 
 		try {
 			XMLReader parser = spf.newSAXParser().getXMLReader(); 
 
 			parser.setContentHandler(ch);
 
 			parser.parse(is);
 		}
 		catch (ParserConfigurationException e)
 		{
 			throw new SAXException(e);
 		}		
 	}
 
 }
 
