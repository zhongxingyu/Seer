 package org.jboss.pressgang.ccms.restserver.webdav.resources;
 
 import net.java.dev.webdav.jaxrs.xml.elements.*;
 import org.apache.commons.io.IOUtils;
 import org.jboss.pressgang.ccms.restserver.webdav.managers.DeleteManager;
 import org.jboss.pressgang.ccms.restserver.webdav.resources.hierarchy.InternalResourceRoot;
 import org.jboss.pressgang.ccms.restserver.webdav.resources.hierarchy.topics.InternalResourceTopicVirtualFolder;
 import org.jboss.pressgang.ccms.restserver.webdav.resources.hierarchy.topics.topic.InternalResourceTopic;
 import org.jboss.pressgang.ccms.restserver.webdav.resources.hierarchy.topics.topic.fields.InternalResourceTempTopicFile;
 import org.jboss.pressgang.ccms.restserver.webdav.resources.hierarchy.topics.topic.fields.InternalResourceTopicContent;
 
 import javax.annotation.Nullable;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.constraints.NotNull;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static javax.ws.rs.core.Response.Status.OK;
 import static net.java.dev.webdav.jaxrs.xml.properties.ResourceType.COLLECTION;
 
 /**
  * The WebDAV server exposes resources from multiple locations. Some resources are found in a database, and some
  * are saved as files.
  * <p/>
  * All resources can potentially be written to, read and deleted. Copying and moving are just combinations of this
  * basic functionality.
  * <p/>
  * Instances of the InternalResource class wrap the functionality needed to read, write and delete.
  * <p/>
  * The InternalResource class is also a factory, matching url paths to the InternalResource instances that manage
  * them. This provides a simple way for the JAX-RS interface to pass off the actual implementation of these underlying
  * methods.
  * <p/>
  * This means that the WebDavResource class can defer functionality to InternalResource.
  */
 public abstract class InternalResource {
     private static final Logger LOGGER = Logger.getLogger(InternalResource.class.getName());
 
    /** Matches something like /webdav/TOPICS/3/4/5/6/TOPIC3456/ */
     public static final Pattern TOPIC_RE = Pattern.compile("/webdav/TOPICS(?<var>(/\\d)*)/TOPIC(?<TopicID>\\d*)/?");
    /** Matches the root directory /webdav */
    public static final Pattern ROOT_FOLDER_RE = Pattern.compile("/webdav/?");
    /** Matches something like /webdav/TOPICS/3/4/5/6/ or /TOPICS */
     public static final Pattern TOPIC_FOLDER_RE = Pattern.compile("/webdav/TOPICS(?<var>(/\\d)*)/?");
    /** Matches something like /webdav/TOPICS/3/4/5/6/TOPIC3456/3456.xml */
     public static final Pattern TOPIC_CONTENTS_RE = Pattern.compile("/webdav/TOPICS(/\\d)*/TOPIC(?<TopicID>\\d+)/\\k<TopicID>.xml");
    /** Matches something like /webdav/TOPICS/3/4/5/6/TOPIC3456/3456.xml~ */
     public static final Pattern TOPIC_TEMP_FILE_RE = Pattern.compile("/webdav/TOPICS(/\\d)*/TOPIC\\d+/[^/]+");
 
     @Nullable
     private final Integer intId;
     @Nullable
     private final String stringId;
     @Nullable
     private final UriInfo uriInfo;
     @Nullable
     private final String remoteAddress;
     @NotNull private final DeleteManager deleteManager;
 
     protected InternalResource(@Nullable final UriInfo uriInfo, final DeleteManager deleteManager, @Nullable final String remoteAddress, @NotNull final Integer intId) {
         this.intId = intId;
         this.stringId = null;
         this.uriInfo = uriInfo;
         this.remoteAddress = remoteAddress;
         this.deleteManager = deleteManager;
     }
 
     protected InternalResource(@Nullable final UriInfo uriInfo, final DeleteManager deleteManager, @Nullable final String remoteAddress, @NotNull final String stringId) {
         this.intId = null;
         this.stringId = stringId;
         this.uriInfo = uriInfo;
         this.remoteAddress = remoteAddress;
         this.deleteManager = deleteManager;
     }
 
     public int write(@NotNull final byte[] contents) {
         throw new UnsupportedOperationException();
     }
 
     public int delete() {
         throw new UnsupportedOperationException();
     }
 
     public ByteArrayReturnValue get() {
         throw new UnsupportedOperationException();
     }
 
     public MultiStatusReturnValue propfind(final int depth) {
         throw new UnsupportedOperationException();
     }
 
     public static javax.ws.rs.core.Response propfind(@NotNull final DeleteManager deleteManager, @NotNull final String remoteAddress, @NotNull final UriInfo uriInfo, final int depth) {
         LOGGER.info("ENTER InternalResource.propfind() " + uriInfo.getPath() + " " + depth + " " + remoteAddress);
 
         final InternalResource sourceResource = InternalResource.getInternalResource(uriInfo, deleteManager, remoteAddress, uriInfo.getPath());
 
         if (sourceResource != null) {
             final MultiStatusReturnValue multiStatusReturnValue = sourceResource.propfind(depth);
 
             if (multiStatusReturnValue.getStatusCode() != 207) {
                 return javax.ws.rs.core.Response.status(multiStatusReturnValue.getStatusCode()).build();
             }
 
             return javax.ws.rs.core.Response.status(207).entity(multiStatusReturnValue.getValue()).type(MediaType.TEXT_XML).build();
         }
 
         return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
     }
 
     public static javax.ws.rs.core.Response copy(@NotNull final DeleteManager deleteManager, @NotNull final String remoteAddress, @NotNull final UriInfo uriInfo, @NotNull final String overwriteStr, @NotNull final String destination) {
         LOGGER.info("ENTER InternalResource.copy() " + uriInfo.getPath() + " " + destination + " " + remoteAddress);
 
         try {
             final HRef destHRef = new HRef(destination);
             final URI destUriInfo = destHRef.getURI();
 
             final InternalResource destinationResource = InternalResource.getInternalResource(null, deleteManager, remoteAddress, destUriInfo.getPath());
             final InternalResource sourceResource = InternalResource.getInternalResource(uriInfo, deleteManager, remoteAddress, uriInfo.getPath());
 
             if (destinationResource != null && sourceResource != null) {
                 final ByteArrayReturnValue byteArrayReturnValue = sourceResource.get();
 
                 if (byteArrayReturnValue.getStatusCode() != javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
                     return javax.ws.rs.core.Response.status(byteArrayReturnValue.getStatusCode()).build();
                 }
 
                 int statusCode;
                 if ((statusCode = destinationResource.write(byteArrayReturnValue.getValue())) != javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()) {
                     return javax.ws.rs.core.Response.status(statusCode).build();
                 }
 
                 return javax.ws.rs.core.Response.ok().build();
 
             }
         } catch (final URISyntaxException e) {
 
         }
 
         return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
     }
 
     public static javax.ws.rs.core.Response move(@NotNull final DeleteManager deleteManager, @NotNull final String remoteAddress, @NotNull final UriInfo uriInfo, @NotNull final String overwriteStr, @NotNull final String destination) {
 
         LOGGER.info("ENTER InternalResource.move() " + uriInfo.getPath() + " " + destination + " " + remoteAddress);
 
         /*
             We can't move outside of the filesystem
          */
         if (!destination.startsWith(uriInfo.getBaseUri().toString())) {
             return javax.ws.rs.core.Response.status(Response.Status.NOT_FOUND).build();
         }
 
         final InternalResource destinationResource = InternalResource.getInternalResource(null, deleteManager, remoteAddress, "/" + destination.replaceFirst(uriInfo.getBaseUri().toString(), ""));
         final InternalResource sourceResource = InternalResource.getInternalResource(uriInfo, deleteManager, remoteAddress, uriInfo.getPath());
 
         if (destinationResource != null && sourceResource != null) {
             final ByteArrayReturnValue byteArrayReturnValue = sourceResource.get();
 
             if (byteArrayReturnValue.getStatusCode() != javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
                 return javax.ws.rs.core.Response.status(byteArrayReturnValue.getStatusCode()).build();
             }
 
             int statusCode;
             if ((statusCode = destinationResource.write(byteArrayReturnValue.getValue())) != javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()) {
                 return javax.ws.rs.core.Response.status(statusCode).build();
             }
 
             if ((statusCode = sourceResource.delete()) != javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()) {
                 return javax.ws.rs.core.Response.status(statusCode).build();
             }
 
             return javax.ws.rs.core.Response.ok().build();
 
         }
 
         return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
     }
 
     public static javax.ws.rs.core.Response delete(@NotNull final DeleteManager deleteManager, @NotNull final String remoteAddress, @NotNull final UriInfo uriInfo) {
         LOGGER.info("ENTER InternalResource.delete() " + uriInfo.getPath() + " " + remoteAddress);
 
         final InternalResource sourceResource = InternalResource.getInternalResource(uriInfo, deleteManager, remoteAddress, uriInfo.getPath());
 
         if (sourceResource != null) {
             return javax.ws.rs.core.Response.status(sourceResource.delete()).build();
         }
 
         return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
     }
 
     public static ByteArrayReturnValue get(@NotNull final DeleteManager deleteManager, @NotNull final String remoteAddress, @NotNull final UriInfo uriInfo) {
         LOGGER.info("ENTER InternalResource.get() " + uriInfo.getPath() + " " + remoteAddress);
 
         final InternalResource sourceResource = InternalResource.getInternalResource(uriInfo, deleteManager, remoteAddress, uriInfo.getPath());
 
         if (sourceResource != null) {
             ByteArrayReturnValue statusCode;
             if ((statusCode = sourceResource.get()).getStatusCode() != javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
                 return statusCode;
             }
 
             return statusCode;
         }
 
         return new ByteArrayReturnValue(javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode(), null);
     }
 
     public static javax.ws.rs.core.Response put(@NotNull final DeleteManager deleteManager, @NotNull final String remoteAddress, @NotNull final UriInfo uriInfo, @NotNull final InputStream entityStream) {
         LOGGER.info("ENTER InternalResource.put() " + uriInfo.getPath() + " " + remoteAddress);
 
         try {
             final InternalResource sourceResource = InternalResource.getInternalResource(uriInfo, deleteManager, remoteAddress, uriInfo.getPath());
 
             if (sourceResource != null) {
                 final byte[] data = IOUtils.toByteArray(entityStream);
 
                 int statusCode = sourceResource.write(data);
                 return javax.ws.rs.core.Response.status(statusCode).build();
             }
 
             return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
         } catch (final IOException e) {
 
         }
 
         return javax.ws.rs.core.Response.serverError().build();
     }
 
     /**
      * The factory method that returns the object to handle a URL request.
      * @param uri The request URI
      * @return  The object to handle the response, or null if the URL is invalid.
      */
     public static InternalResource getInternalResource(@Nullable final UriInfo uri, @NotNull final DeleteManager deleteManager, @NotNull final String remoteAddress, @NotNull final String requestPath) {
 
         LOGGER.info("ENTER InternalResource.getInternalResource() " + requestPath);
 
         /*
             Order is important here, as the TOPIC_TEMP_FILE_RE will match everything the
             TOPIC_CONTENTS_RE will. So we check TOPIC_TEMP_FILE_RE after TOPIC_CONTENTS_RE.
 
             Other regexes are specific enough not to match each other.
          */
 
         final Matcher topicContents = TOPIC_CONTENTS_RE.matcher(requestPath);
         if (topicContents.matches()) {
             LOGGER.info("Matched InternalResourceTopicContent");
             return new InternalResourceTopicContent(uri, deleteManager, remoteAddress, Integer.parseInt(topicContents.group("TopicID")));
         }
 
         final Matcher topicFolder = TOPIC_FOLDER_RE.matcher(requestPath);
         if (topicFolder.matches()) {
             LOGGER.info("Matched InternalResourceTopicVirtualFolder");
             return new InternalResourceTopicVirtualFolder(uri, deleteManager, remoteAddress, requestPath);
         }
 
         final Matcher rootFolder = ROOT_FOLDER_RE.matcher(requestPath);
         if (rootFolder.matches()) {
             LOGGER.info("Matched InternalResourceRoot");
             return new InternalResourceRoot(uri, deleteManager, remoteAddress, requestPath);
         }
 
         final Matcher topic = TOPIC_RE.matcher(requestPath);
         if (topic.matches()) {
             LOGGER.info("Matched InternalResourceTopic");
             return new InternalResourceTopic(uri, deleteManager, remoteAddress, Integer.parseInt(topic.group("TopicID")));
         }
 
         final Matcher topicTemp = TOPIC_TEMP_FILE_RE.matcher(requestPath);
         if (topicTemp.matches()) {
             LOGGER.info("Matched InternalResourceTempTopicFile");
             return new InternalResourceTempTopicFile(uri, deleteManager, remoteAddress, requestPath);
         }
 
 
 
         LOGGER.info("None matched");
         return null;
     }
 
     /**
      * Returning a child folder means returning a Respose that identifies a WebDAV collection.
      * This method populates the returned request with the information required to identify
      * a child folder.
      *
      * @param uriInfo      The URI of the current request
      * @param resourceName The name of the child folder
      * @return The properties for a child folder
      */
     public static net.java.dev.webdav.jaxrs.xml.elements.Response getFolderProperties(@NotNull final UriInfo uriInfo, @NotNull final String resourceName) {
         /*final Date lastModified = new Date(0);
         final CreationDate creationDate = new CreationDate(lastModified);
         final GetLastModified getLastModified = new GetLastModified(lastModified);
         final Prop prop = new Prop(creationDate, getLastModified, COLLECTION);*/
 
         final Prop prop = new Prop(COLLECTION);
 
         final Status status = new Status((javax.ws.rs.core.Response.StatusType) OK);
         final PropStat propStat = new PropStat(prop, status);
 
         final URI uri = uriInfo.getRequestUriBuilder().path(resourceName).build();
         final HRef hRef = new HRef(uri);
         final net.java.dev.webdav.jaxrs.xml.elements.Response folder = new net.java.dev.webdav.jaxrs.xml.elements.Response(hRef, null, null, null, propStat);
 
         return folder;
     }
 
     /**
      * @param uriInfo The URI of the current request
      * @return The properties for the current folder
      */
     public static net.java.dev.webdav.jaxrs.xml.elements.Response getFolderProperties(@NotNull final UriInfo uriInfo) {
         /*final Date lastModified = new Date(0);
         final CreationDate creationDate = new CreationDate(lastModified);
         final GetLastModified getLastModified = new GetLastModified(lastModified);
         final Prop prop = new Prop(creationDate, getLastModified, COLLECTION);*/
 
         final Prop prop = new Prop(COLLECTION);
 
         final Status status = new Status((javax.ws.rs.core.Response.StatusType) OK);
         final PropStat propStat = new PropStat(prop, status);
 
         final URI uri = uriInfo.getRequestUri();
         final HRef hRef = new HRef(uri);
         final net.java.dev.webdav.jaxrs.xml.elements.Response folder = new net.java.dev.webdav.jaxrs.xml.elements.Response(hRef, null, null, null, propStat);
 
         return folder;
     }
 
     /**
      * The info about the request that was used to retrieve this object. This can be null
      * when the initial request results in a second resource object being looked up (copy and move).
      *
      * All resource objects should check to make sure this is not null when doing a propfind (which is
      * where the uriInfo is actually used). However, there should never be a case where a secondary
      * resource object has its propfind method called.
      */
     @Nullable
     public UriInfo getUriInfo() {
         return uriInfo;
     }
 
     /**
      * The id of the entity that this resource represents. Usually a database primary key,
      * but the context of the ID is up to the resource class.
      */
     @Nullable
     public Integer getIntId() {
         return intId;
     }
 
     /**
      * The id of the entity that this resource represents. Usually a file name,
      * but the context of the ID is up to the resource class.
      */
     @Nullable
     public String getStringId() {
         return stringId;
     }
 
     @Nullable
     public String getRemoteAddress() {
         return remoteAddress;
     }
 
     public DeleteManager getDeleteManager() {
         return deleteManager;
     }
 }
