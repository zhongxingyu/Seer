 /**
  * 
  * Copyright 2002 - 2007 NCHELP
  * 
  * Author:	Tim Bornholtz - The Bornholtz Group 
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
 
 package org.nchelp.meteor.provider.status;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.xpath.XPathAPI;
 import org.nchelp.meteor.provider.DataProvider;
 import org.nchelp.meteor.registry.Directory;
 import org.nchelp.meteor.registry.DirectoryFactory;
 import org.nchelp.meteor.util.DateDelta;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.XMLParser;
 import org.nchelp.meteor.util.exception.DirectoryException;
 import org.nchelp.meteor.util.exception.ParameterException;
 import org.nchelp.meteor.util.exception.ParsingException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 
 /**
  * This servlet queries the registry for a list of all current data providers
  * subscribed to the Meteor network. The results are translated into an xml
  * document and applied to an xsl template in order to display the results to
  * the user.
  * 
  * @since Meteor1.0
  */
 public class StatusServlet extends HttpServlet
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -971376452505018384L;
 	private final Log log = LogFactory.getLog(this.getClass());
 
 	/**
 	 * This is the main workhorse of this object. This is the method responsible
 	 * for handling the HTTP request and responding.
 	 * 
 	 * @param req
 	 *          HTTP request object
 	 * @param res
 	 *          HTTP response object
 	 */
 	public void doGet (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
 	{
 		try
 		{
 			String xslFile = "";
 
 			String[] providers = req.getParameterValues("dataprovider");
 			
 			String xmlReturn;
 			
 			Resource resource = ResourceFactory.createResource("statusprovider.properties");
 
 			if(providers == null || providers.length == 0){
 				xmlReturn = this.getDataProviderList(req);
 				xslFile = resource.getProperty("statusprovider.dataproviderlist");
 			} else {
 				try{
 					xmlReturn = this.queryDataProviders(providers);
 				} catch(ParameterException e){
					xmlReturn = "<DataProviders><Errors>Error retrieving data responses" + e.getLocalizedMessage() + "</Errors></DataProviders>";
 				}
 				xslFile = resource.getProperty("statusprovider.dataproviderresults");
 			}
 
 			if (xslFile == null)
 			{
 				criticalError(res, "Error reading XSL stylesheet", null);
 				return;
 			}
 
 			//
 			// Convert the xml and send response to user
 			//
 			res.setContentType("text/html");
 			PrintWriter out = res.getWriter();
 
 			try
 			{
 				// Get the XML input document and the stylesheet, both in the servlet
 				// engine document directory.
 				Source xmlSource = new StreamSource(new StringReader(xmlReturn));
 
 				InputStream is = getServletContext().getResourceAsStream(xslFile);
 
 				StreamSource xslSource = new StreamSource(is);
 				URL xslURL = getServletContext().getResource(xslFile);
 				xslSource.setSystemId(xslURL.toExternalForm());
 
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
 			catch (Exception e)
 			{
 				// Send it to the console too
 				log.error("Error Transforming XSL: " + xslFile, e);
 				out.write(e.getMessage());
 				e.printStackTrace(out);
 			}
 			out.close();
 
 		}
 		catch (Exception e)
 		{
 			criticalError(res, "Unspecified error occurred: " + e.getMessage(), e);
 		}
 
 	}
 
 	/**
 	 * Handles any errors occurring during processing of the request
 	 * 
 	 * @param msg
 	 *          Error message
 	 */
 	private void criticalError (HttpServletResponse res, String msg, Exception e) throws java.io.IOException
 	{
 		log.error("Critical Error occured in StatusService: " + msg, e);
 
 		// Send the response
 		res.setContentType("text/html");
 		PrintWriter out = res.getWriter();
 
 		out.println("<html>");
 		out.println("<head><title>Error Page</title></head>");
 		out.println("<body>");
 		out.println("<h1>The follow error has occurred while processing " + "the Meteor request:  " + msg + "</h1>");
 		out.println("</body></html>");
 
 		if (e != null)
 			e.printStackTrace(out);
 
 		out.close();
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
 	 */
 	protected void doPost (HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException
 	{
 		this.doGet(arg0, arg1);
 	}
 
 	private String getDataProviderList(HttpServletRequest req) throws DirectoryException{
 
 		List dplist = null;
 		String xmlReturn = "<DataProviders>";
 
 		Directory dir = null;
 
 		dir = DirectoryFactory.getInstance().getDirectory();
 		dplist = dir.getDataProviders();
 
 		if (dplist == null)
 			dplist = new Vector();
 
 		Iterator i = dplist.iterator();
 		while (i.hasNext())
 		{
 			DataProvider dp = (DataProvider)i.next();
 			String id = dp.getId();
 			if (dp.getName() == null)
 			{
 				dp.setName(dir.getInstitutionName(id));
 			}
 
 			xmlReturn += "<DataProvider><Identifier>" + id + "</Identifier><Name>" + dp.getName() + "</Name>" + "</DataProvider>";
 		}
 		xmlReturn += "</DataProviders>";
 
 		log.debug("Data Providers: " + xmlReturn);
 		
 		return xmlReturn;
 	}
 
 	private String queryDataProviders(String[] providers) throws ParameterException, DirectoryException {
 		StatusService service = new StatusService();
 		
 		Directory dir = DirectoryFactory.getInstance().getDirectory();
 
 		List results;
 
 		results = service.callProviders(providers);
 		
 		StringBuffer retStr = new StringBuffer("<DataProviders>");
 		
 		Iterator iter = results.iterator();
 		while(iter.hasNext()){
 			Provider p = (Provider)iter.next();
 			DataProvider dp = p.getProviderStatus().getDataProvider();
 			if (dp.getName() == null)
 			{
 				dp.setName(dir.getInstitutionName(dp.getId()));
 			}
 
 			
 			retStr.append("<DataProvider>");
 			retStr.append("<Identifier>");
 			retStr.append(dp.getId());
 			retStr.append("</Identifier>");
 			retStr.append("<Name>");
 			retStr.append(dp.getName());
 			retStr.append("</Name>");
 			retStr.append("<StartTime>");
 			retStr.append(dp.getStartTime());
 			retStr.append("</StartTime>");
 			retStr.append("<EndTime>");
 			retStr.append(dp.getEndTime());
 			retStr.append("</EndTime>");
 			
 			retStr.append("<ElapsedTime>");
 			retStr.append(DateDelta.getSecondDelta(dp.getStartTime(), dp.getEndTime()));
 			retStr.append("</ElapsedTime>");
 
 			retStr.append("<Status>");
 			retStr.append(dp.getStatus());
 			retStr.append("</Status>");
 
 			String response = dp.getResponse();
 
 			retStr.append("<Messages>");
 			retStr.append(this.getMessages(response));
 			retStr.append("</Messages>");
 
 
 			retStr.append("<Result><![CDATA[");
 			retStr.append(response);
 			retStr.append("]]></Result>");
 
 			retStr.append("</DataProvider>");
 		}
 		retStr.append("</DataProviders>");
 		
 		if(log.isDebugEnabled()){
 			log.debug("Final XML: " + retStr.toString()); 
 		}
 		return retStr.toString();
 	}
 
 	private String getMessages(String response){
 		try {
 			Document doc = XMLParser.parseXML(response);
 			
 			Node n = XPathAPI.selectSingleNode(doc, "//MeteorDataProviderMsg");
 			if(n == null){
 				return "";
 			}
 			return XMLParser.xmlToString(XMLParser.createDocument(n));
 		} catch (ParsingException e) {
 			return "Unable to parse response";
 		} catch (TransformerException e) {
 			return "Unable to retrieve messages from response";
 		}
 	}
 	
 }
 
