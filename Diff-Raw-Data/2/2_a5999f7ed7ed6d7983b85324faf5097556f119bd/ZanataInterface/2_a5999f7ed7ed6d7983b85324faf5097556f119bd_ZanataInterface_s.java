 package org.jboss.pressgang.ccms.zanata;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.utils.common.VersionUtilities;
 import org.jboss.resteasy.client.ClientResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zanata.common.LocaleId;
 import org.zanata.rest.client.ISourceDocResource;
 import org.zanata.rest.client.ITranslatedDocResource;
 import org.zanata.rest.dto.VersionInfo;
 import org.zanata.rest.dto.resource.Resource;
 import org.zanata.rest.dto.resource.ResourceMeta;
 import org.zanata.rest.dto.resource.TranslationsResource;
 
 public class ZanataInterface {
     private static Logger log = LoggerFactory.getLogger(ZanataInterface.class);
     private final static ZanataDetails DEFAULT_DETAILS = new ZanataDetails();
 
     private final ZanataDetails details;
     private final ZanataProxyFactory proxyFactory;
     private final ZanataLocaleManager localeManager;
     private final Long minZanataRESTCallInterval;
     private long lastRESTCallTime = 0;
 
     /**
      * Constructs the interface.
      */
     public ZanataInterface() {
         this(0, DEFAULT_DETAILS.getProject());
     }
 
     /**
      * Constructs the interface.
      *
      * @param minZanataRESTCallInterval The minimum amount of time that should be waited in between calls to Zanata. This value
      *                                  is specified in seconds.
      */
     public ZanataInterface(final double minZanataRESTCallInterval) {
         this(minZanataRESTCallInterval, DEFAULT_DETAILS.getProject());
     }
 
     /**
      * Constructs the interface with a custom project
      *
      * @param projectOverride The name of the Zanata project to work with, which will override the default specified.
      */
     public ZanataInterface(final String projectOverride) {
         this(0, projectOverride);
     }
 
     /**
      * Constructs the interface with a custom project
      *
      * @param minZanataRESTCallInterval The minimum amount of time that should be waited in between calls to Zanata. This value
      *                                  is specified in seconds.
      * @param projectOverride           The name of the Zanata project to work with, which will override the default specified.
      */
     public ZanataInterface(final double minZanataRESTCallInterval, final String projectOverride) {
         details = new ZanataDetails(DEFAULT_DETAILS);
         details.setProject(projectOverride);
 
         this.minZanataRESTCallInterval = (long) (minZanataRESTCallInterval * 1000);
 
         URI URI = null;
         try {
             URI = new URI(details.getServer());
         } catch (URISyntaxException e) {
         }
 
         // Get the Version Details from the Zanata Common API library. 
         final VersionInfo versionInfo = new VersionInfo();
         versionInfo.setVersionNo(VersionUtilities.getAPIVersion(LocaleId.class));
         versionInfo.setBuildTimeStamp(VersionUtilities.getAPIBuildTimestamp(LocaleId.class));
 
         proxyFactory = new ZanataProxyFactory(URI, details.getUsername(), details.getToken(), versionInfo);
         localeManager = ZanataLocaleManager.getInstance(details.getProject());
     }
 
     /**
      * Get a specific Source Document from Zanata.
      *
      * @param id The ID of the Document in Zanata.
      * @return The Zanata Source Document that matches the id passed, or null if it doesn't exist.
      */
     public Resource getZanataResource(final String id) {
         ClientResponse<Resource> response = null;
         try {
             final ISourceDocResource client = proxyFactory.getSourceDocResource(details.getProject(), details.getVersion());
             response = client.getResource(id, null);
 
             final Status status = Response.Status.fromStatusCode(response.getStatus());
 
             if (status == Response.Status.OK) {
                 final Resource entity = response.getEntity();
                 return entity;
             }
         } catch (final Exception ex) {
             log.error("Failed to retrieve the Zanata Source Document", ex);
         } finally {
             /*
              * If you are using RESTEasy client framework, and returning a Response from your service method, you will
              * explicitly need to release the connection.
              */
             if (response != null) response.releaseConnection();
             
             /* Perform a small wait to ensure zanata isn't overloaded */
             performZanataRESTCallWaiting();
         }
 
         return null;
     }
 
     /**
      * Get all of the Document ID's available from Zanata for the configured project.
      *
      * @return A List of Resource Objects that contain information such as Document ID's.
      */
     public List<ResourceMeta> getZanataResources() {
         ClientResponse<List<ResourceMeta>> response = null;
         try {
             final ISourceDocResource client = proxyFactory.getSourceDocResource(details.getProject(), details.getVersion());
             response = client.get(null);
 
             final Status status = Response.Status.fromStatusCode(response.getStatus());
 
             if (status == Response.Status.OK) {
                 final List<ResourceMeta> entities = response.getEntity();
                 return entities;
             } else {
                 log.error(
                         "REST call to get() did not complete successfully. HTTP response code was " + status.getStatusCode() + ". Reason " +
                                 "was " + status.getReasonPhrase());
             }
         } catch (final Exception ex) {
             log.error("Failed to retrieve the list of Zanata Source Documents", ex);
         } finally {
             /*
              * If you are using RESTEasy client framework, and returning a Response from your service method, you will
              * explicitly need to release the connection.
              */
             if (response != null) response.releaseConnection();
             
             /* Perform a small wait to ensure zanata isn't overloaded */
             performZanataRESTCallWaiting();
         }
 
         return null;
     }
 
     /**
      * Create a Document in Zanata.
      *
      * @param resource The resource data to be used by Zanata to create the Document.
      * @return True if the document was successfully created, otherwise false.
      */
     public boolean createFile(final Resource resource) {
         ClientResponse<String> response = null;
         try {
             final IFixedSourceDocResource client = proxyFactory.getFixedSourceDocResources(details.getProject(), details.getVersion());
             response = client.post(details.getUsername(), details.getToken(), resource, null, true);
 
             final Status status = Response.Status.fromStatusCode(response.getStatus());
 
             if (status == Response.Status.CREATED) {
                 final String entity = response.getEntity();
                 if (entity.trim().length() != 0) log.info(entity);
 
                 return true;
             } else {
                 log.error("REST call to createResource() did not complete successfully. HTTP response code was " + status.getStatusCode() +
                         ". Reason was " + status.getReasonPhrase());
             }
 
         } catch (final Exception ex) {
             log.error("Failed to create the Zanata Document", ex);
         } finally {
             /*
              * If you are using RESTEasy client framework, and returning a Response from your service method, you will
              * explicitly need to release the connection.
              */
             if (response != null) response.releaseConnection();
             
             /* Perform a small wait to ensure zanata isn't overloaded */
             performZanataRESTCallWaiting();
         }
 
         return false;
     }
 
     /**
      * Get a Translation from Zanata using the Zanata Document ID and Locale.
      *
      * @param id     The ID of the document in Zanata.
      * @param locale The locale of the translation to find.
      * @return null if the translation doesn't exist or an error occurred, otherwise the TranslationResource containing the
      *         Translation Strings (TextFlowTargets).
      */
     public TranslationsResource getTranslations(final String id, final LocaleId locale) {
         ClientResponse<TranslationsResource> response = null;
         try {
             final ITranslatedDocResource client = proxyFactory.getTranslatedDocResource(details.getProject(), details.getVersion());
             response = client.getTranslations(id, locale, null);
 
             final Status status = Response.Status.fromStatusCode(response.getStatus());
 
             /* Remove the locale if it is forbidden */
             if (status == Response.Status.FORBIDDEN) {
                 localeManager.removeLocale(locale);
             } else if (status == Response.Status.OK) {
                 final TranslationsResource retValue = response.getEntity();
                 return retValue;
             }
         } catch (final Exception ex) {
             log.error("Failed to retrieve the Zanata Translated Document", ex);
         } finally {
             /*
              * If you are using RESTEasy client framework, and returning a Response from your service method, you will
              * explicitly need to release the connection.
              */
             if (response != null) response.releaseConnection();
             
             /* Perform a small wait to ensure zanata isn't overloaded */
             performZanataRESTCallWaiting();
         }
 
         return null;
     }
 
     /**
      * Delete a Document from Zanata.
      * <p/>
      * Note: This method should be used with extreme care.
      *
      * @param id The ID of the document in Zanata to be deleted.
      * @return True if the document was successfully deleted, otherwise false.
      */
     public boolean deleteResource(final String id) {
         performZanataRESTCallWaiting();
         ClientResponse<String> response = null;
         try {
             final IFixedSourceDocResource client = proxyFactory.getFixedSourceDocResources(details.getProject(), details.getVersion());
             response = client.deleteResource(details.getUsername(), details.getToken(), id);
 
             final Status status = Response.Status.fromStatusCode(response.getStatus());
 
             if (status == Response.Status.OK) {
                 final String entity = response.getEntity();
                if (entity.trim().length() != 0) System.out.println(entity);
                 return true;
             } else {
                 log.error("REST call to deleteResource() did not complete successfully. HTTP response code was " + status.getStatusCode() +
                         ". Reason was " + status.getReasonPhrase());
             }
         } catch (final Exception ex) {
             log.error("Failed to delete the Zanata Source Document", ex);
         } finally {
             /*
              * If you are using RESTEasy client framework, and returning a Response from your service method, you will
              * explicitly need to release the connection.
              */
             if (response != null) response.releaseConnection();
         }
 
         return false;
     }
 
     /**
      * Get a list of locales that can be synced to Zanata.
      *
      * @return A List of LocaleId objects that can be used to syn with Zanata.
      */
     public List<LocaleId> getZanataLocales() {
         return localeManager.getLocales();
     }
 
     /**
      * Get the Manager that handles what locales can be synced against in Zanata.
      *
      * @return The ZanataLocaleManager object that manages the available locales.
      */
     public ZanataLocaleManager getLocaleManager() {
         return localeManager;
     }
 
     /**
      * Sleep for a small amount of time to allow zanata to process other data between requests if the time between calls is less
      * than the wait interval specified.
      */
     private void performZanataRESTCallWaiting() {
         /* No need to wait when the call interval is nothing */
         if (minZanataRESTCallInterval <= 0) return;
 
         long currentTime = System.currentTimeMillis();
         /* Check if the current time is less than the last call plus the minimum wait time */
         if (currentTime < (lastRESTCallTime + minZanataRESTCallInterval)) {
             try {
                 Thread.sleep(minZanataRESTCallInterval);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
         
         /* Set the current time to the last call time. */
         lastRESTCallTime = System.currentTimeMillis();
     }
 }
