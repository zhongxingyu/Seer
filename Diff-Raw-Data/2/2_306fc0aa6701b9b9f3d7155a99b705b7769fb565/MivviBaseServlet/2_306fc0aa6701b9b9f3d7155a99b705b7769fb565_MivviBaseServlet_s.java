 /*
  * Mivvi - Metadata, organisation and identification for television programs
  * Copyright (C) 2004, 2005, 2006, 2010  Joseph Walton
  *
  * This library is free software: you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.kafsemo.mivvi.rest;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletResponse;
 
 import org.kafsemo.mivvi.app.SeriesData;
 import org.kafsemo.mivvi.appengine.AEBlobstorePopulator;
 import org.kafsemo.mivvi.appengine.AEDatastorePopulator;
 import org.kafsemo.mivvi.appengine.EmbeddedMivviDataPopulator;
 import org.kafsemo.mivvi.rdf.Mivvi;
 import org.kafsemo.mivvi.rdf.Presentation;
 import org.kafsemo.mivvi.rdf.RdfUtil;
 import org.openrdf.model.Graph;
 import org.openrdf.model.Statement;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.rdfxml.RDFXMLWriter;
 import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
 import org.openrdf.sail.memory.MemoryStore;
 
 /**
  * Base class for servlets that need data available.
  * 
  * @author joe
  */
 public class MivviBaseServlet extends HttpServlet
 {
     private MivviDataPopulator populator;
 //    SailRepository rep;
     SeriesData sd;
     Presentation pres;
 
     @Override
     public void init(ServletConfig config) throws ServletException
     {
         super.init(config);
         populator = new AEDatastorePopulator(config.getServletContext());
         // Billable AppSpot
 //        populator = new AEMivviDataPopulator();
 
       // Embedded resource
//        populator = new EmbeddedMivviDataPopulator();
     }
     
     synchronized void populateData() throws ServletException
     {
         if (sd != null && pres != null) {
             /* Data already loaded */
             return;
         }
             
         try {
             MemoryStore ms = new MemoryStore();
             Repository rep = new SailRepository(ms);
             rep.initialize();
             
             populator.populate(rep);
             
             this.sd = new SeriesData();
             sd.initMviRepository(rep);
             this.pres = new Presentation(rep.getConnection());
         } catch (RepositoryException e) {
             throw new ServletException(e);
         } catch (IOException e) {
             throw new ServletException(e);
         }
     }
     
     void writeGraphAsRdfXml(Graph g, HttpServletResponse resp)
         throws IOException, RDFHandlerException
     {
         resp.setContentType("application/rdf+xml");
 
         RDFXMLWriter rxw = new RDFXMLPrettyWriter(resp.getOutputStream());
         rxw.handleNamespace("mvi", Mivvi.URI);
         rxw.handleNamespace("dc", RdfUtil.DC_URI);
 
         rxw.startRDF();
 
         for (Statement s : g) {
             rxw.handleStatement(s);
         }
 
         rxw.endRDF();
     }
     
     void sendError(HttpServletResponse resp, int status, String message)
         throws IOException
     {
         resp.setStatus(status);
         
         resp.setContentType("text/plain");
         
         PrintWriter pw = resp.getWriter();
         pw.print(message);
         pw.close();
     }
 }
