 package org.jboss.pressgang.ccms.restserver.webdav.resources.hierarchy.topics.topic.fields;
 
 import net.java.dev.webdav.jaxrs.xml.elements.*;
 import net.java.dev.webdav.jaxrs.xml.properties.*;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.jboss.pressgang.ccms.restserver.webdav.constants.WebDavConstants;
 import org.jboss.pressgang.ccms.restserver.webdav.resources.InternalResource;
 import org.jboss.pressgang.ccms.restserver.webdav.resources.MultiStatusReturnValue;
 import org.jboss.pressgang.ccms.restserver.webdav.resources.ByteArrayReturnValue;
 import org.jboss.pressgang.ccms.restserver.webdav.managers.DeleteManager;
 
 import javax.annotation.Nullable;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.constraints.NotNull;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import static javax.ws.rs.core.Response.Status.OK;
 
 /**
  * Handles access to temporary files.
  */
 public class InternalResourceTempTopicFile extends InternalResource {
 
     private static final Logger LOGGER = Logger.getLogger(InternalResourceTempTopicFile.class.getName());
 
     public InternalResourceTempTopicFile(@NotNull final UriInfo uriInfo, @NotNull final DeleteManager deleteManager, @Nullable final String remoteAddress, @NotNull final String path) {
         super(uriInfo, deleteManager, remoteAddress, path);
     }
 
     @Override
     public int write(@NotNull final byte[] contents) {
         LOGGER.info("ENTER InternalResourceTempTopicFile.write() " + getStringId());
 
         try {
             final File directory = new java.io.File(WebDavConstants.TEMP_LOCATION);
             final String fileLocation = buildTempFileName(getStringId());
 
             if (!directory.exists()) {
                 directory.mkdirs();
             } else if (!directory.isDirectory()) {
                 directory.delete();
                 directory.mkdirs();
             }
 
             final File file = new File(fileLocation);
 
             if (!file.exists()) {
                 file.createNewFile();
             }
 
             FileUtils.writeByteArrayToFile(file, contents);
 
             return Response.Status.NO_CONTENT.getStatusCode();
         } catch (final IOException e) {
 
         }
 
         return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
     }
 
     @Override
     public ByteArrayReturnValue get() {
 
         LOGGER.info("ENTER InternalResourceTempTopicFile.get() " + getStringId());
 
         final String fileLocation = buildTempFileName(getStringId());
 
         try {
 
             if (!exists(fileLocation)) {
                 return new ByteArrayReturnValue(Response.Status.NOT_FOUND.getStatusCode(), null);
             }
 
             final FileInputStream inputStream = new FileInputStream(fileLocation);
             try {
                 final byte[] fileContents = IOUtils.toByteArray(inputStream);
                 return new ByteArrayReturnValue(Response.Status.OK.getStatusCode(), fileContents);
 
             } catch (final Exception ex) {
 
             } finally {
                 try {
                     inputStream.close();
                 } catch (final Exception ex) {
 
                 }
             }
 
         } catch (final FileNotFoundException e) {
             return new ByteArrayReturnValue(Response.Status.NOT_FOUND.getStatusCode(), null);
         }
 
         return new ByteArrayReturnValue(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), null);
     }
 
     /**
         Temp files only live for a short period of time. This method determines if the
         temp file should be visible.
     */
     public static boolean exists(@NotNull final String fileLocation) {
         return exists(new File(fileLocation));
     }
 
     /**
      Temp files only live for a short period of time. This method determines if the
      temp file should be visible.
      */
     public static boolean exists(@NotNull final File file) {
         if (file.exists()) {
 
             final Calendar window = Calendar.getInstance();
             window.add(Calendar.SECOND, -WebDavConstants.TEMP_WINDOW);
 
             final Date lastModified = new Date(file.lastModified());
 
            if (lastModified.before(window.getTime())) {
                 file.delete();
             }
             else {
                 return true;
             }
         }
 
         return false;
     }
 
     @Override
     public int delete() {
         LOGGER.info("ENTER InternalResourceTempTopicFile.delete() " + getStringId());
 
         final String fileLocation = buildTempFileName(getStringId());
 
         final File file = new File(fileLocation);
         if (file.exists()) {
             file.delete();
             return Response.Status.OK.getStatusCode();
         }
 
         return Response.Status.NOT_FOUND.getStatusCode();
     }
 
     @Override
     public MultiStatusReturnValue propfind(final int depth) {
 
         if (getUriInfo() == null) {
             throw new IllegalStateException("Can not perform propfind without uriInfo");
         }
 
         try {
             final String fileLocation = InternalResourceTempTopicFile.buildTempFileName(getUriInfo().getPath());
 
             final File file = new File(fileLocation);
 
             if (file.exists()) {
                 final net.java.dev.webdav.jaxrs.xml.elements.Response response = getProperties(getUriInfo(), file, true);
                 final MultiStatus st = new MultiStatus(response);
                 return new MultiStatusReturnValue(207, st);
             }
 
         } catch (final NumberFormatException ex) {
             return new MultiStatusReturnValue(404);
         }
 
         return new MultiStatusReturnValue(404);
     }
 
     public static String buildTempFileName(final String path) {
         return WebDavConstants.TEMP_LOCATION + "/" + path.replace("/", "_");
     }
 
     public static String buildWebDavFileName(final String path, final File file) {
         return file.getName().replaceFirst(path.replace("/", "_"), "");
     }
 
     /**
      * @param uriInfo The uri that was used to access this resource
      * @param file    The file that this content represents
      * @param local   true if we are building the properties for the resource at the given uri, and false if we are building
      *                properties for a child resource.
      * @return
      */
     public static net.java.dev.webdav.jaxrs.xml.elements.Response getProperties(final UriInfo uriInfo, final File file, final boolean local) {
         final HRef hRef = local ? new HRef(uriInfo.getRequestUri()) : new HRef(uriInfo.getRequestUriBuilder().path(InternalResourceTempTopicFile.buildWebDavFileName(uriInfo.getPath(), file)).build());
         final GetLastModified getLastModified = new GetLastModified(new Date(file.lastModified()));
         final GetContentType getContentType = new GetContentType(MediaType.APPLICATION_OCTET_STREAM);
         final GetContentLength getContentLength = new GetContentLength(file.length());
         final DisplayName displayName = new DisplayName(file.getName());
         final SupportedLock supportedLock = new SupportedLock();
         final LockDiscovery lockDiscovery = new LockDiscovery();
         final Prop prop = new Prop(getLastModified, getContentType, getContentLength, displayName, supportedLock, lockDiscovery);
         final Status status = new Status((javax.ws.rs.core.Response.StatusType) OK);
         final PropStat propStat = new PropStat(prop, status);
 
         final net.java.dev.webdav.jaxrs.xml.elements.Response davFile = new net.java.dev.webdav.jaxrs.xml.elements.Response(hRef, null, null, null, propStat);
 
         return davFile;
     }
 }
