 /**
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		Jon Feauto,  Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ********************************************************************************/
 
 package org.nchelp.meteor.provider.access;
 
 import java.util.Iterator;
 import java.util.List;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.StringReader;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.provider.DataProvider;
 import org.nchelp.meteor.registry.Directory;
 import org.nchelp.meteor.registry.DirectoryFactory;
 import org.nchelp.meteor.util.exception.DirectoryException;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 
 /**
  * @author jonf
  *
  * This servlet queries the registry for a list of all current data
  * providers subscribed to the Meteor network.  The results are translated
  * into an xml document and applied to an xsl template in order to display
  * the results to the user.
  * 
  * @version   $Revision$ $Date$
  * @since     Meteor1.0
  */
 public class DataProviderQueryService extends HttpServlet {
 	
 	private final Logger log = Logger.create(this.getClass());
 
 	/**
 	 * This is the main workhorse of this object.  This is the 
 	 * method responsible for handling the HTTP request and
 	 * responding.
 	 * 
 	 * @param req HTTP request object
 	 * @param res HTTP response object
 	 */
 	public void doGet(HttpServletRequest req, HttpServletResponse res)
 		throws ServletException, IOException {
 
 		try {
			List aup = null;
 			String xmlReturn = "<DataProviders>";
 			String xslFile = "";
 			String level = req.getParameter("Level");
 
 			try {
 				Directory dir = DirectoryFactory.getInstance().getDirectory();
 				
				aup = dir.getDataProviders();
 			} catch (DirectoryException e) {
 				log.error("Error getting Data Providers", e);
 			}
			
			Iterator i = aup.iterator();
 			while(i.hasNext()){
 				DataProvider dp = (DataProvider)i.next();
 				if(dp.getName() == null){
 					dp.setName(dp.getURL().toString());
 				}
 				xmlReturn += "<DataProvider><Name>" + 
 				             dp.getName() + "</Name>" + 
 				             "<URL>" + dp.getURL().toString() + "</URL>" +
 				             "</DataProvider>";
 			}
 			xmlReturn += "</DataProviders>";
 			
 			log.debug("Data Providers: " + xmlReturn);
 		
 			Resource resource = ResourceFactory.createResource("accessprovider.properties");
 			xslFile = resource.getProperty("meteor.servlet.transaction." + level);
 			if (xslFile == null) {
 				xslFile = resource.getProperty("meteor.servlet.transaction.default");
 			}
 			//If it is still null, then call the handleError method
 			if (xslFile == null) {
 				criticalError(res, "Error reading XSL stylesheet", null);
 				return;
 			}
 	
 			//
 			// Convert the xml and send response to user
 			//
 			res.setContentType("text/html");
 			PrintWriter out = res.getWriter();
 	
 			try {
 				// Get the XML input document and the stylesheet, both in the servlet
 				// engine document directory.
 				Source xmlSource = new StreamSource(new StringReader(xmlReturn));
 	
 				InputStream is = getServletContext().getResourceAsStream(xslFile);
 	
 				StreamSource xslSource = new StreamSource(is);
 	
 				// Generate the transformer.
 				TransformerFactory tFactory = TransformerFactory.newInstance();
 				Transformer transformer = tFactory.newTransformer(xslSource);
 	
 				// Put the Servlet name in the xslt Parameters too
 				transformer.setParameter("SERVLET", req.getContextPath() + req.getServletPath());
 				
 				// Set Context Path
 				transformer.setParameter("CONTEXTPATH", req.getContextPath());
 	
 				// Perform the transformation, sending the output to the response.
 				transformer.transform(xmlSource, new StreamResult(out));
 			}
 			// If an Exception occurs, return the error to the client.
 			catch (Exception e) {
 				// Send it to the console too
 				log.error("Error Transforming XSL: " + xslFile, e);
 				out.write(e.getMessage());
 				e.printStackTrace(out);
 			}
 			out.close();
 
 		} catch (Exception e) {
 			criticalError(res, "Unspecified error occurred: " + e.getMessage(), e);
 		}
 	}
 	
 	/**
 	 * Handles any errors occurring during processing of the request
 	 * @param msg Error message
 	 */
 	private void criticalError(HttpServletResponse res, String msg, Exception e)
 		throws java.io.IOException {
 			
 		log.error("Critical Error occured in DataProviderQueryService: " + msg, e);	
 			
 		// Send the response
 		res.setContentType("text/html");
 		PrintWriter out = res.getWriter();
 
 		out.println("<html>");
 		out.println("<head><title>Error Page</title></head>");
 		out.println("<body>");
 		out.println(
 			"<h1>The follow error has occurred while processing "
 				+ "the Meteor request:  "
 				+ msg
 				+ "</h1>");
 		out.println("</body></html>");
 
 		if (e != null)
 			e.printStackTrace(out);
 
 		out.close();
 	}	
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
 	 */
 	protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
 		throws ServletException, IOException {
 		this.doGet(arg0, arg1);
 	}
 }
