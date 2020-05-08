 package org.jboss.pressgang.ccms.restserver.webdav.topics;
 
 import net.java.dev.webdav.jaxrs.methods.PROPFIND;
 import net.java.dev.webdav.jaxrs.xml.elements.*;
 import org.jboss.pressgang.ccms.model.Topic;
 import org.jboss.pressgang.ccms.restserver.webdav.WebDavConstants;
 import org.jboss.pressgang.ccms.restserver.webdav.WebDavResource;
 import org.jboss.pressgang.ccms.restserver.webdav.WebDavUtils;
 
 import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
 import static net.java.dev.webdav.jaxrs.Headers.DEPTH;
 
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.*;
 
 import javax.persistence.EntityManager;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.ext.Providers;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
     The virtual folder that holds all the topics within a given id range.
 
     We do this because "ls -la" is a killer. It will enumerate the grandchildren
     of the current directory in order to provide file counts.
 
     This means that without at least two levels of indirection, an ls call
     could result in every topic being returned.
 
     With two levels of indirection we can reduce that down to a call that returns
     only 100 or so topics, which is manageable.
  */
 @Path("/TOPICS{var:.*}/{start:\\d+}-{end:\\d+}")
 public class TopicGroupedVirtualFolder extends WebDavResource {
 
     public static final String RESOURCE_NAME = "TOPICS";
     private static final Logger LOGGER = Logger.getLogger(TopicGroupedVirtualFolder.class.getName());
 
     @PathParam("start") int start;
     @PathParam("end") int end;
 
     @Override
     @Produces(MediaType.APPLICATION_XML)
     @PROPFIND
     public javax.ws.rs.core.Response propfind(@Context final UriInfo uriInfo, @HeaderParam(DEPTH) final int depth, final InputStream entityStream,
                                               @HeaderParam(CONTENT_LENGTH) final long contentLength, @Context final Providers providers,
                                               @Context final HttpHeaders httpHeaders) throws URISyntaxException, IOException {
         try {
             LOGGER.info("ENTER TopicGroupedVirtualFolder.propfind()");
 
             if (depth == 0) {
                 LOGGER.info("Depth == 0");
                 /* A depth of zero means we are returning information about this item only */
                return javax.ws.rs.core.Response.status(207).entity(new MultiStatus(getFolderProperties(uriInfo))).type(WebDavConstants.XML_MIME).build();
             } else {
 
                 final List<net.java.dev.webdav.jaxrs.xml.elements.Response> responses = new ArrayList<net.java.dev.webdav.jaxrs.xml.elements.Response>();
 
                 final int maxIdDigits = (end + "").length();
 
                 if (end - start + 1 > WebDavConstants.SECOND_LEVEL_GROUP_SIZE) {
                     LOGGER.info("Creating new level of grouping. This doesn't involve any query.");
 
                     for (int i = start; i < end; i += WebDavConstants.SECOND_LEVEL_GROUP_SIZE) {
                         final String start = String.format("%0" + maxIdDigits + "d", i);
                         final String end = String.format("%0" + maxIdDigits + "d", (i + WebDavConstants.SECOND_LEVEL_GROUP_SIZE - 1));
 
                         responses.add(getFolderProperties(uriInfo, start + "-" + end));
                     }
                 } else {
 
                     LOGGER.info("Getting child topics. This will return topics ids.");
                     /* Otherwise we are retuning info on the children in this collection */
                     final EntityManager entityManager = WebDavUtils.getEntityManager(false);
                     final List<Integer> topics = entityManager.createQuery("SELECT topic.topicId FROM Topic topic where topic.topicId >= " + start + " and topic.topicId <= " + end, Integer.class).getResultList();
 
                     for (final Integer topic : topics) {
                         final String topicId = String.format("%0" + maxIdDigits + "d", topic.toString());
                         responses.add(getFolderProperties(uriInfo, topicId));
                     }
                 }
 
                 final MultiStatus st = new MultiStatus(responses.toArray(new net.java.dev.webdav.jaxrs.xml.elements.Response[responses.size()]));
 
                return javax.ws.rs.core.Response.status(207).entity(st).type(WebDavConstants.XML_MIME).build();
             }
 
         } catch (final Exception ex) {
             LOGGER.severe(ex.toString());
             ex.printStackTrace();
             return javax.ws.rs.core.Response.status(500).build();
         }
     }
 
 }
