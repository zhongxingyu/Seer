 /*  
  * BennoSearchResource.java  
  *   
  * Copyright (C) 2009 LWsystems GmbH & Co. KG  
  * This program is free software; you can redistribute it and/or 
  * modify it under the terms of the GNU General Public License as 
  * published by the Free Software Foundation; either version 2 of 
  * the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,  
  * but WITHOUT ANY WARRANTY; without even the implied warranty of  
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
  * GNU General Public License for more details.  
  *   
  * You should have received a copy of the GNU General Public License  
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */
 package de.lwsystems.mailarchive.web.service;
 
 import de.lwsystems.mailarchive.web.RestrictedQuery;
 import de.lwsystems.mailarchive.web.SearchResultModel;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletContext;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.Produces;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Request;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.search.Query;
 
 /**
  * REST Web Service
  *
  * @author rene
  */
 @Path("search/{limit}/{query}")
 public class BennoSearchResource {
 
     @Context
     private UriInfo context;
     @Context
     private Request request;
     @Context
     private ServletContext servletContext;
 
     /**
      * Retrieves representation of an instance of de.lwsystems.mailarchive.web.service.BennoSearchResource
      * @return an instance of java.lang.String
      */
     @GET
     @Produces("text/plain")
     public String getXml(@PathParam("limit") int maxdoc, @PathParam("query") String query) {
         SearchResultModel srm = null;
         String exString = "";
 
         if (servletContext == null) {
             return "No servlet context!";
         }
 	try {
             srm = SearchResultModel.getDefaultInstance(servletContext);
             exString = "Well done!";
             if (srm == null) {
                 return "ERROR No SearchResultModel created! Servlet Context Path: " + servletContext.getServletContextName() + "\nExceptions: " + exString;
             }
 
 	    try {
                 Query q = RestrictedQuery.getParsedQueryWithRestrictions(query);
                 if (q == null) {
                     return "ERROR No restricted q could be constructed from " + query;
                 }
 
                 srm.query(q);
                 long rows = Math.min(srm.getRowCount(), maxdoc);
                 StringBuilder result = new StringBuilder("OK " + rows + " " + srm.getRowCount() + "\n");
                 for (int i = 0; i < rows; i++) {
                     try {
                         result.append(srm.getDataBlock(i));
                     } catch (CorruptIndexException ex) {
                         Logger.getLogger(BennoSearchResource.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (IOException ex) {
                         Logger.getLogger(BennoSearchResource.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (java.text.ParseException ex) {
                         Logger.getLogger(BennoSearchResource.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
 
                 return result.toString();
             } catch (ParseException ex) {
                 Logger.getLogger(BennoSearchResource.class.getName()).log(Level.SEVERE, null, ex);
                 return "Parse Exception: " + ex;
             } catch (IOException ex) {
                 Logger.getLogger(BennoSearchResource.class.getName()).log(Level.SEVERE, null, ex);
                 return ("I/O Exception: " + ex);
             }
 
         } catch (CorruptIndexException ex) {
             Logger.getLogger(BennoSearchResource.class.getName()).log(Level.SEVERE, null, ex);
             exString = exString.concat(ex.toString());
         } catch (IOException ex) {
             Logger.getLogger(BennoSearchResource.class.getName()).log(Level.SEVERE, null, ex);
             exString = exString.concat(ex.toString());
         } finally {
 	}
 
         return "";
     }
 
     /**
     * PUT method for updating message or creating an instance of BennoSearchResource
      * @param content representation for the resource
      * @return an HTTP response with content of the updated or created resource.
      */
     @PUT
     @Consumes("application/xml")
     public void putXml(String content) {
     }
 }
