 /**
  *
  */
 
 package eu.scape_project.resource;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.StreamingOutput;
 import javax.xml.bind.JAXBException;
 
 import org.fcrepo.session.InjectedSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import eu.scape_project.service.ConnectorService;
 import eu.scapeproject.model.File;
 import eu.scapeproject.model.IntellectualEntity;
 import eu.scapeproject.model.Representation;
 import eu.scapeproject.util.ScapeMarshaller;
 
 @Component
 @Scope("prototype")
 @Path("/scape/sru")
 public class SRUSearch {
 
     @InjectedSession
     private Session session;
 
     @Autowired
     private ConnectorService connectorService;
 
     private final ScapeMarshaller marshaller;
 
     public SRUSearch()
             throws JAXBException {
         this.marshaller = ScapeMarshaller.newInstance();
     }
 
     @GET
     @Path("/entities")
     public Response searchIntellectualEntities(@QueryParam("operation")
     final String operation, @QueryParam("query")
     final String query, @QueryParam("version")
     final String version, @QueryParam("startRecord")
    @DefaultValue("0")
     final int offset, @QueryParam("maximumRecords")
     @DefaultValue("25")
     final int limit) throws RepositoryException {
 
         final List<String> uris =
                 this.connectorService.searchEntities(this.session, query,
                         offset, limit);
         return Response.ok(new StreamingOutput() {
 
             @Override
             public void write(OutputStream output) throws IOException,
                     WebApplicationException {
                 writeSRUHeader(output, uris.size());
                 for (String uri : uris) {
                     try {
                         final IntellectualEntity ie =
                                 SRUSearch.this.connectorService.fetchEntity(
                                         session, uri.substring(uri
                                                 .lastIndexOf('/') + 1));
                         writeSRURecord(ie, output);
                     } catch (RepositoryException e) {
                         throw new IOException(e);
                     }
                 }
                 writeSRUFooter(output);
             }
         }).build();
     }
 
     @GET
     @Path("/representations")
     public Response searchRepresentations(@QueryParam("operation")
     final String operation, @QueryParam("query")
     final String query, @QueryParam("version")
     final String version, @QueryParam("startRecord")
     final int offset, @QueryParam("maximumRecords")
     @DefaultValue("25")
     final int limit) throws RepositoryException {
 
         final List<String> uris =
                 this.connectorService.searchRepresentations(this.session,
                         query, offset, limit);
         return Response.ok(new StreamingOutput() {
 
             @Override
             public void write(OutputStream output) throws IOException,
                     WebApplicationException {
                 writeSRUHeader(output, uris.size());
                 for (String uri : uris) {
                     try {
                         final Representation rep =
                                 SRUSearch.this.connectorService
                                         .fetchRepresentation(
                                                 session,
                                                 uri.substring(uri
                                                         .indexOf("/" +
                                                                 ConnectorService.ENTITY_FOLDER)));
                         writeSRURecord(rep, output);
                     } catch (RepositoryException e) {
                         throw new IOException(e);
                     }
                 }
                 writeSRUFooter(output);
             }
         }).build();
     }
 
     @GET
     @Path("/files")
     public Response searchFiles(@QueryParam("operation")
     final String operation, @QueryParam("query")
     final String query, @QueryParam("version")
     final String version, @QueryParam("startRecord")
     final int offset, @QueryParam("maximumRecords")
     @DefaultValue("25")
     final int limit) throws RepositoryException {
 
         final List<String> uris =
                 this.connectorService.searchFiles(this.session, query, offset,
                         limit);
         return Response.ok(new StreamingOutput() {
 
             @Override
             public void write(OutputStream output) throws IOException,
                     WebApplicationException {
                 writeSRUHeader(output, uris.size());
                 for (String uri : uris) {
                     try {
                         final File f =
                                 SRUSearch.this.connectorService
                                         .fetchFile(
                                                 session,
                                                 uri.substring(uri
                                                         .indexOf("/" +
                                                                 ConnectorService.ENTITY_FOLDER)));
                         writeSRURecord(f, output);
                     } catch (RepositoryException e) {
                         throw new IOException(e);
                     }
                 }
                 writeSRUFooter(output);
             }
         }).build();
     }
 
     private void writeSRURecord(Object o, OutputStream output)
             throws IOException {
         final StringBuilder sru = new StringBuilder();
         sru.append("<srw:record>");
         sru.append("<srw:recordSchema>http://scapeproject.eu/schema/plato</srw:recordSchema>");
         sru.append("<srw:recordData>");
         output.write(sru.toString().getBytes());
         try {
             this.marshaller.serialize(o, output);
         } catch (JAXBException e) {
             throw new IOException(e);
         }
         sru.setLength(0);
         sru.append("</srw:recordData>");
         sru.append("</srw:record>");
         output.write(sru.toString().getBytes());
     }
 
     private void writeSRUFooter(OutputStream output) throws IOException {
         final StringBuilder sru = new StringBuilder();
         sru.append("</srw:records>");
         sru.append("</srw:searchRetrieveResponse>");
         output.write(sru.toString().getBytes());
     }
 
     private void writeSRUHeader(OutputStream output, int size)
             throws IOException {
         final StringBuilder sru = new StringBuilder();
         sru.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
         sru.append("<srw:searchRetrieveResponse xmlns:srw=\"http://scapeproject.eu/srw/\">");
         sru.append("<srw:numberOfRecords>" + size + "</srw:numberOfRecords>");
         sru.append("<srw:records>");
         output.write(sru.toString().getBytes("UTF-8"));
     }
 
 }
