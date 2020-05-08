 /**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/portal/presence/src/java/org/sakaiproject/tool/portal/PresenceTool.java $
 * $Id: PresenceTool.java 632 2005-07-14 21:22:50Z janderse@umich.edu $
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
 package org.sakaiproject.tool.OSIDRepository;
 
import java.io.IOException;
import java.io.PrintWriter;

import org.sakaiproject.tool.search.MatchItem;
import org.sakaiproject.tool.search.QueryBase;
import org.sakaiproject.tool.search.ResultPageBase;
import org.sakaiproject.tool.search.SearchResultBase;
import org.sakaiproject.tool.util.DomException;
import org.sakaiproject.tool.util.DomUtils;
import org.sakaiproject.tool.util.HttpTransactionUtils;
import org.sakaiproject.tool.util.SearchException;
import org.sakaiproject.tool.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLDocument;
 
 /*
  * public class Search extends ComponentServlet {
  */
 public class OSIDRepositoryTool extends HttpServlet {
   /**
    * Servlet context
    */
   protected ServletContext    _context;
 
 	/**
    * Cleanup
    */
   public void destroy() {
     super.destroy();
   }
 
   /**
    * Initialize
    */
   public void init(ServletConfig servletConfig) throws ServletException {
 		String resource;
 
     super.init(servletConfig);
     _context = servletConfig.getServletContext();
 
 		try {
 	    /*
 	     * Get our search source configuration (mandatory)
 	     */
 	    //resource = "WEB-INF/" + getInitParameter("source-configuration-file");
 	    //SearchSource.populate(_context.getResourceAsStream(resource));
 	    SearchSource.populate();
 	    /*
 	     * Configure [optional] security support
 	     */
 	    resource = "WEB-INF/" + getInitParameter("security-configuration-file");
 	    SecuritySetup.initialize(_context.getResourceAsStream(resource));
 
 	  } catch (Exception exception) {
 	  	throw new ServletException(exception.toString());
 	  }
   }
 
   /**
    * Service a GET request
    * @param request Standard HTTP request object
    * @param response Standard HTTP response
    */
   public void doGet(HttpServletRequest request, HttpServletResponse response)
                                        throws ServletException, IOException {
     doPost(request, response);
   }
 
   /**
    * Service a POST request
    * @param request Standard HTTP request object
    * @param response Standard HTTP response
    */
   public void doPost(HttpServletRequest request, HttpServletResponse response)
                                         throws ServletException, IOException 
   {
 
     String				cssFile			= request.getParameter("cssFile");
 	String				db				= request.getParameter("database");
     String				initialQuery	= request.getParameter("initialQuery");
     HTMLDocument		document		= null;
     QueryBase			query			= null;
     SearchResultBase	searchResult	= null;
 
 	SessionContext		sessionContext;
     PrintWriter			writer;
 
 	try 
 	{
 		if (cssFile == null) 
 		{
 			cssFile = ResultPageBase.COMMONCSS;
 //			throw new IllegalArgumentException("Missing styesheet");
 		}
 			
 		if (initialQuery != null) 
 		{
 			/*
 			* Initial request - render the "blank" search page (search form only)
 			*/
 			document = formatInitialQuery(db, cssFile);
 		}
 		else
 		{
 			String	database[];
 			int numDatabases = 0;
 			
 			try 
 			{
 					database = request.getParameterValues("database");
 					numDatabases = database.length;
 			} 
 			catch (Throwable t) 
 			{
 				throw new IllegalArgumentException("No database selected");
 			}
 
 			if (request.getParameter("searchString").trim().equals("")) 
 			{
 				throw new IllegalArgumentException("Nothing to find");
 			}
 
 			if (numDatabases > 0)
 			{
 				// There is more than one selection so we are going to accumulate results					
 				query = null;
 				
 				java.util.Vector itemsVector = new java.util.Vector();
 				
 				for (int i=0; i < numDatabases; i++)
 				{
 					sessionContext 	= SessionContext.getInstance(getHttpSession(request));
 					System.out.println("Searching " + database[i]);
 					query = doQuery(request, sessionContext, database[i]);
 					searchResult = parseResponse(query,database[i]);
 					
 					// capture what is in this searches set of results
 					MatchItem items[] = searchResult.toArray();
 					
 					// add all that came before
 					for (int j=0, size = itemsVector.size(); j < size; j++)
 					{
 						searchResult.addItem( (MatchItem)itemsVector.elementAt(j) );
 					}
 					
 					// keep running total, unless we are on the last search
 					if (i < (numDatabases-1))
 					{
 						for (int j=0; j < items.length; j++)
 						{
 							itemsVector.addElement(items[j]);
 						}
 					}
 				}				
 				document = formatResultDocument(query, searchResult);
 			}
 		}
     } 
 	catch (Exception exception) 
 	{
     	document = formatErrorMessage(exception.getMessage(), cssFile, db);
     }
 		/*
 		 * In every case, render the result document back to the browser
 		 */
     response.setContentType("text/html; charset=" + DomUtils.ENCODING);
     writer = response.getWriter();
 
     try {
       DomUtils.serializeHtml(document, writer);
 
     } catch (DomException exception) {
       throw new ServletException(exception.toString());
 
     } finally {
       try { writer.flush(); } catch (Exception ignore) { }
       try { writer.close(); } catch (Exception ignore) { }
     }
   }
 
 	/**
 	 * Get our session context object
 	 * @return HttpSession object
 	 */
   private HttpSession getHttpSession(HttpServletRequest request) {
   	HttpSession session;
 
 		session = request.getSession(true);
 		System.out.println("HTTP SESSION = " + session);
 		return session;
 	}
 
   /**
    * Execute the query for a "standard" search operation
    * @param request The servlet request block for this transaction
    * @param sessionContext Session context for this user
    * @param database Database name
    */
   private QueryBase doQuery(HttpServletRequest 	request,
   													SessionContext 			sessionContext,
   													String 							database) throws ServletException {
   	QueryBase	query;
 
 		query = selectQueryHandler(sessionContext, database);
 		query.parseRequest(HttpTransactionUtils.getAttributesAsMap(request));
 		
 		String host = "http://" + request.getServerName() + ":" + request.getServerPort();
 		String path = request.getPathInfo();
 		String uri = request.getRequestURI();
 		String middle = uri.substring(0,uri.indexOf(path));
 		String base = host + middle + "/OSIDRepository";
 		
 		query.doQuery(base);
 
     return query;
   }
 
   /**
    * Parse the search engine response
    * @param query Query handler
    */
   private SearchResultBase parseResponse(QueryBase query, String database)
                                          throws ServletException {
 
     SearchResultBase searchResult;
 
     searchResult = selectSearchResultHandler(database);
 
     searchResult.initialize(query);
     searchResult.doParse(database);
 
     return searchResult;
   }
 
   /**
    * Generate the result HTML page(s)
    * @param query Query handler
    * @param searchResult Encapsulated search results
    */
   private HTMLDocument formatResultDocument(QueryBase query, SearchResultBase searchResult)
                                             throws ServletException {
 
     ResultPageBase result;
 
     result = selectResultPageHandler(query.getRequestParameter("database"));
 
     result.initialize(query, searchResult);
     result.doResultPageHeader();
     result.doResultPageBody();
     result.doResultPageFooter();
 
     return result.getRenderedResponseDocument();
   }
 
   /**
    * Generate the initial search form.  This is the standard, simple
    * form with no ancillary result data displayed.
    * @param database Page type
    * @param cssFile The stylesheet to use for this page
    */
   private HTMLDocument formatInitialQuery(String database, String cssFile)
   																			  throws ServletException {
     String					db;
     ResultPageBase  result;
     Element					body;
 
 	db = database;
 	if (StringUtils.isNull(database)) {
     	db	= SearchSource.getDefaultSourceName();
     }
 
     result = selectResultPageHandler(db);
 
 	try{
 		result.initialize(db, cssFile);
 		result.doResultPageHeader();
 		body = result.doInitialQueryBody();
 		result.doSearchForm(body);
 		result.doResultPageFooter();
 	} catch (Throwable t) {
 		t.printStackTrace();
 	}
 
     return result.getRenderedResponseDocument();
   }
 
   /**
    * Generate an error page
    * @param text The error text
    * @param cssFile The stylesheet to use for this page
    * @param database Page type
    */
   private HTMLDocument formatErrorMessage(String text,
                                    				String cssFile,
                                    				String database) throws ServletException {
 
     ResultPageBase  result = new ResultPageBase();
 
 	  result.initialize(database, cssFile);
     result.doResultPageHeader();
     result.doResultPageErrorBody(text);
     result.doResultPageFooter();
 
     return result.getRenderedResponseDocument();
   }
 
   /**
    * Select a query handler for the requested database
    * @param sessionContext Session context
    * @param name Database name (eg ERIC)
    * @return Query handler
    */
   private QueryBase selectQueryHandler(SessionContext sessionContext,
   																		 String 				name) {
     SearchSource	source 	= SearchSource.getSourceByName(name);
 		QueryBase			handler;
 
 		try {
 			handler = source.getQueryHandler();
 		} catch (Exception exception) {
 			throw new SearchException(exception.toString());
 		}
     /*
      * Initialize and return the query handler
      */
     handler.initialize(sessionContext);
     return handler;
   }
 
   /**
    * Select a search result handler for this source
    * @param name Source name/form type
    * @return Search result handler
    */
   private SearchResultBase selectSearchResultHandler(String name) {
     SearchSource			source = SearchSource.getSourceByName(name);
 		SearchResultBase	handler;
 
 		try {
 			handler = source.getSearchResultHandler();
 		} catch (Exception exception) {
 			throw new SearchException(exception.toString());
 		}
 		return handler;
   }
 
 	/**
    * Select a result page generator for the requested search engine
    * @param name Database name (eg ERIC)
    * @return Result handler
    */
   private ResultPageBase selectResultPageHandler(String name) {
     SearchSource		source = SearchSource.getSourceByName(name);
 		ResultPageBase	handler;
 
 		try {
 			handler = source.getResultPageHandler();
 		} catch (Exception exception) {
 			throw new SearchException(exception.toString());
 		}
 		return handler;
   }
 }
