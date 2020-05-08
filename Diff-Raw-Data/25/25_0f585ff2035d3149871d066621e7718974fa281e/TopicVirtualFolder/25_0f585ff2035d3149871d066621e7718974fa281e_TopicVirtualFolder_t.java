 package org.jboss.pressgang.ccms.restserver.webdav.topics;
 
 import net.java.dev.webdav.jaxrs.methods.PROPFIND;
 import net.java.dev.webdav.jaxrs.xml.elements.MultiStatus;
 import org.jboss.pressgang.ccms.model.Topic;
 import org.jboss.pressgang.ccms.restserver.webdav.WebDavConstants;
 import org.jboss.pressgang.ccms.restserver.webdav.WebDavResource;
 import org.jboss.pressgang.ccms.restserver.webdav.WebDavUtils;
 
 import javax.persistence.EntityManager;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.ext.Providers;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
 import static net.java.dev.webdav.jaxrs.Headers.DEPTH;
 
 /**
     The virtual folder that holds groups of topic ids. Grouping in this way prevents
     the server from having to process too many topics in any part of the tree.
  */
 @Path("/TOPICS")
 public class TopicVirtualFolder extends WebDavResource {
 
     public static final String RESOURCE_NAME = "TOPICS";
     private static final Logger LOGGER = Logger.getLogger(TopicVirtualFolder.class.getName());
 
 
     @Override
     @Produces(MediaType.APPLICATION_XML)
     @PROPFIND
     public javax.ws.rs.core.Response propfind(@Context final UriInfo uriInfo, @HeaderParam(DEPTH) final int depth, final InputStream entityStream,
                                               @HeaderParam(CONTENT_LENGTH) final long contentLength, @Context final Providers providers,
                                               @Context final HttpHeaders httpHeaders) throws URISyntaxException, IOException {
         try {
             LOGGER.info("ENTER TopicVirtualFolder.propfind()");
 
             if (depth == 0) {
                 LOGGER.info("Depth == 0");
                 /* A depth of zero means we are returning information about this item only */
                return javax.ws.rs.core.Response.status(207).entity(new MultiStatus(getFolderProperties(uriInfo))).type(MediaType.TEXT_XML).build();
             } else {
                 LOGGER.info("Getting children of the TOPICS virtual folder. This is a min/max query.");
                 /* Otherwise we are retuning info on the children in this collection */
                 final EntityManager entityManager = WebDavUtils.getEntityManager(false);
 
                 final Integer minId = entityManager.createQuery("SELECT MIN(topic.topicId) FROM Topic topic", Integer.class).getSingleResult();
                 final Integer maxId = entityManager.createQuery("SELECT MAX(topic.topicId) FROM Topic topic", Integer.class).getSingleResult();
                 final int maxIdDigits = maxId.toString().length();
 
                 LOGGER.info("Minimum topic id is " + minId + " and the maximum id is " + maxId);
 
                 final List<net.java.dev.webdav.jaxrs.xml.elements.Response> responses = new ArrayList<net.java.dev.webdav.jaxrs.xml.elements.Response>();
 
                 for (int i = minId; i < maxId; i += WebDavConstants.GROUP_SIZE) {
                     final String start = String.format("%0" + maxIdDigits + "d", i);
                     final String end = String.format("%0" + maxIdDigits + "d", (i + WebDavConstants.GROUP_SIZE - 1));
 
                     responses.add(getFolderProperties(uriInfo, start + "-" + end));
                 }
 
                 final MultiStatus st = new MultiStatus(responses.toArray(new net.java.dev.webdav.jaxrs.xml.elements.Response[responses.size()]));
 
                return javax.ws.rs.core.Response.status(207).entity(st).type(MediaType.TEXT_XML).build();
             }
 
         } catch (final Exception ex) {
             LOGGER.severe(ex.toString());
             ex.printStackTrace();
             return javax.ws.rs.core.Response.status(500).build();
         }
     }
 
 }
