 package org.jbundle.base.screen.control.xslservlet;
 
 /**
  * @(#)DBServlet.java 0.00 12-Feb-97 Don Corley
  *
  * Copyright (c) 2009 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  */
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.jbundle.base.screen.control.servlet.BaseHttpTask.SERVLET_TYPE;
 import org.jbundle.base.screen.control.servlet.ServletTask;
 import org.jbundle.base.screen.control.servlet.xml.XMLServlet;
 import org.jbundle.base.screen.model.BaseScreen;
 import org.jbundle.base.util.DBParams;
 import org.jbundle.thin.base.db.Constants;
 import org.jbundle.thin.base.util.Application;
 import org.jbundle.thin.base.util.Util;
 
 
 /**
  * XSLServlet
  * 
  * This is the xsl servlet.
  */
 public class XSLServlet extends XMLServlet
 {
 	private static final long serialVersionUID = 1L;
 
 	public XSLServlet()
 	{
 		super();
 	}
 	/**
      * returns the servlet info
      */ 
     public String getServletInfo()
     {
         return "This the xsl servlet";
     }
     /**
      * init method.
      * @exception ServletException From inherited class.
      */
     @Override
     public void init(ServletConfig config) throws ServletException
     {
         super.init(config);
     }
     /**
      * Destroy this Servlet and any active applications.
      * This is only called when all users are done using this Servlet.
      */
     @Override
     public void destroy()
     {
         super.destroy();
     }
     /**
      *  process an HTML get or post.
      * @exception ServletException From inherited class.
      * @exception IOException From inherited class.
      */
     @Override
     public void service(HttpServletRequest req, HttpServletResponse res) 
         throws ServletException, IOException
     {
     	//String sourceFileName = req.getParameter("source");
 
     	String stylesheet = null;
     	ServletTask servletTask = null;
     	
         StringWriter stringWriter = new StringWriter();
         PrintWriter writer = new PrintWriter(stringWriter);
         try   {
             servletTask = new ServletTask(this, SERVLET_TYPE.COCOON);
 			BaseScreen screen = servletTask.doProcessInput(this, req, null);
 			
 			if (stylesheet == null)
 				stylesheet = req.getParameter(DBParams.TEMPLATE);
 			if (stylesheet == null)
 				if (screen != null)
 					if (screen.getScreenFieldView() != null)
 						stylesheet = screen.getScreenFieldView().getStylesheetPath();
 			if (stylesheet == null)
 				stylesheet = req.getParameter("stylesheet");
 			if (stylesheet == null)
 				stylesheet = "docs/styles/xsl/flat/base/menus-ajax.xsl";
 			StreamSource stylesheetSource = this.getStylesheetSource(servletTask, stylesheet);
 			
 	    	ServletOutputStream outStream = res.getOutputStream();
             Result result = new StreamResult(outStream);
 
             // TODO - Cache transformers
             TransformerFactory tFact = TransformerFactory.newInstance();
             URIResolver resolver = new MyURIResolver(servletTask, stylesheet);
             tFact.setURIResolver(resolver);
             Transformer transformer = tFact.newTransformer(stylesheetSource);
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             // TODO - Create a task to feed the writer to the (transformer) input stream.
 			servletTask.doProcessOutput(this, req, null, writer, screen);
 
 			writer.flush();
 	        String string = stringWriter.toString();
 	        StringReader sourceFileReader = new StringReader(string);
             StreamSource source = new StreamSource(sourceFileReader);
 
             transformer.transform(source, result);
         } catch (TransformerConfigurationException ex)    {
             ex.printStackTrace();
         } catch (TransformerException ex) {
             ex.printStackTrace();
             servletTask.free();
         } catch (ServletException ex) {
         } // Never
     	
    	//x Don't call super.service(req, res);
     }
 	public StreamSource getStylesheetSource(ServletTask servletTask, String stylesheet) throws MalformedURLException
 	{
 		stylesheet = Util.getFullFilename(stylesheet, null, Constants.DOC_LOCATION, true);
 		URL stylesheetURL = null;
 		Application app = servletTask.getApplication();
 		if (app != null)
 		    stylesheetURL = app.getResourceURL(stylesheet, null);
 		else
 		    stylesheetURL = this.getClass().getClassLoader().getResource(stylesheet);
 		if (stylesheetURL == null)
 		{
 			if (stylesheet.indexOf(':') == -1)
 				stylesheet = "file:" + stylesheet;
 			stylesheetURL = new URL(stylesheet);
 		}
 		
 		if (stylesheetURL != null)
 		{
 		    try {
 		        InputStream is = stylesheetURL.openStream();
 		        return new StreamSource(is);
 		    } catch (IOException ex)    {
 		    	// return null;
 		    }
 		}
 		return null;
 	}
     /**
      * Class to return base path of imports and includes.
      * @author don
      *
      */
     public class MyURIResolver
     	implements URIResolver
     {
     	String mainStylesheet;
     	ServletTask servletTask;
     	
         public MyURIResolver(ServletTask servletTask, String mainStylesheet)
         {
         	super();
         	this.mainStylesheet = mainStylesheet;
         	this.servletTask = servletTask;
         }
 
         /**
          * Using the main stylesheet, create a path to this stylesheet
          */
 		@Override
 		public Source resolve(String href, String base)
 				throws TransformerException {
 			Source source = null;
 			int lastSlash = mainStylesheet.lastIndexOf('/');
 			if (lastSlash == -1)
 				lastSlash = mainStylesheet.lastIndexOf(File.separatorChar);
 			if (lastSlash == -1)
 				return null;	// Never
 			if ((href.startsWith("/")) || (href.endsWith(File.separator)))
 				lastSlash--;
 			String sourceStylesheet = mainStylesheet.substring(0, lastSlash + 1) + href;
 			
 			try {
 				return getStylesheetSource(servletTask, sourceStylesheet);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
     	
     }
 }
