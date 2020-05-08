 package cz.cokrtvac.webgephi.rest;
 
 import cz.cokrtvac.webgephi.ejb.GraphService;
 import cz.cokrtvac.webgephi.gephi.ImageExporter;
 import cz.cokrtvac.webgephi.gephi.LayoutsExecutor;
 import cz.cokrtvac.webgephi.gephi.LayoutsPool;
 import cz.cokrtvac.webgephi.model.entity.GraphEntity;
 import cz.cokrtvac.webgephi.model.xml.graph.GraphDetailXml;
 import cz.cokrtvac.webgephi.model.xml.graph.GraphsXml;
 import cz.cokrtvac.webgephi.model.xml.layout.LayoutXml;
 import cz.cokrtvac.webgephi.util.StringUtil;
 import cz.cokrtvac.webgephi.util.XmlUtil;
 import org.slf4j.Logger;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 
 /**
  * JAX-RS Example
  * <p/>
  * This class produces a RESTful service to read the contents of the members table.
  */
 @Path("/graphs")
 @Stateless
 public class GraphResourceRESTService {
     @Inject
     private Logger log;
 
     @Inject
     private LayoutsPool configurationSingleton;
 
     @Inject
     private GraphService graphService;
 
     @Inject
     private LayoutsExecutor executor;
 
     @Inject
     private ImageExporter exporter;
 
     /**
      * Pure gexf format of saved graph
      *
      * @param id
      * @return
      * @throws ParserConfigurationException
      * @throws SAXException
      * @throws IOException
      */
     @GET
     @Path("/{id}/gexf")
     @Produces("text/xml")
     public String getGraphAsGexf(@Context HttpServletRequest req, @PathParam("id") Long id) throws ParserConfigurationException, SAXException, IOException {
         GraphEntity e = graphService.get(id);
         return e.getXml();
 
     }
 
     /**
      * Pure csv format of saved graph
      *
      * @param id
      * @return
      * @throws ParserConfigurationException
      * @throws SAXException
      * @throws IOException
      */
     @GET
     @Path("/{id}/svg")
     @Produces("image/svg+xml")
     public String getGraphAsSvg(@Context HttpServletRequest req, @PathParam("id") Long id) throws ParserConfigurationException, SAXException, IOException {
         GraphEntity e = graphService.get(id);
         String xml = e.getXml();
         return exporter.toSvg(xml);
     }
 
 
     /**
      * Graph saved in db
      * With link to GEXF data
      *
      * @param id
      * @return
      * @throws ParserConfigurationException
      * @throws SAXException
      * @throws IOException
      */
     @GET
     @Path("/{id}")
     @Produces("text/xml")
     public GraphDetailXml getGraphDetail(@Context HttpServletRequest req, @PathParam("id") Long id) throws ParserConfigurationException, SAXException, IOException {
         GraphEntity e = graphService.get(id);
         GraphDetailXml xml = GraphDetailXml.create(e);
         xml.setLink(req);
         return xml;
     }
 
     /**
      * List of all graphs in db
      * TODO - paging
      *
      * @return
      */
     @GET
     @Produces("text/xml")
     public GraphsXml getAllGraphs(@Context HttpServletRequest req) {
         GraphsXml graphs = new GraphsXml();
         graphs.setLink(req);
         for (GraphEntity e : graphService.getAll()) {
             GraphDetailXml xml = GraphDetailXml.create(e);
             xml.setLink(req);
            if(xml.getParent() != null){
                xml.getParent().setLink(req);
            }
             graphs.getGraphs().add(xml);
         }
         return graphs;
     }
 
     /**
      * Creates a new graph
      *
      * @param document
      * @param name
      * @return
      */
     @POST
     @Produces("text/xml")
     @Consumes("text/xml")
     public GraphDetailXml addGraph(@Context HttpServletRequest req, Document document, @QueryParam("name") String name) {
         log.info("doc: " + XmlUtil.toString(document) + "\nname=" + name);
         // TODO validate
 
         GraphEntity graph = new GraphEntity();
         graph.setName(name);
         graph.setXml(XmlUtil.toString(document));
         graphService.persist(graph);
 
         GraphDetailXml resXml = GraphDetailXml.create(graph);
         resXml.setLink(req);
 
         if(resXml.getParent() != null){
             resXml.getParent().setLink(req);
         }
         return resXml;
     }
 
     /**
      * Applies layout function defined in xml content and
      * saves result to DB
      *
      * @param id
      * @return Result graph - with link to GEXF data
      * @throws ParserConfigurationException
      * @throws SAXException
      * @throws IOException
      */
     @PUT
     @Path("/{id}")
     @Consumes("text/xml")
     @Produces("text/xml")
     public GraphDetailXml applyLayout(@Context HttpServletRequest req, LayoutXml layout, @PathParam("id") Long id, @QueryParam("repeat") Integer repeat) throws ParserConfigurationException, SAXException, IOException {
         GraphEntity e = graphService.get(id);
         log.debug("Layout: " + layout.toString());
         log.debug("Repeat: " + repeat);
         if(repeat == null){
             repeat = 1;
         }
 
         String resXml = executor.execute(e.getXml(), layout, repeat);
 
         GraphEntity result = new GraphEntity();
         result.setName(e.getName() + "_" + StringUtil.uriSafe(layout.getName()));
         result.setParent(e);
         result.setXml(resXml);
         graphService.persist(result);
 
         return GraphDetailXml.create(result);
     }
 
 
 }
