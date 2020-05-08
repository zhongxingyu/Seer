 /**
  * Copyright 2011 Unicon (R) Licensed under the
  * Educational Community License, Version 2.0 (the "License"); you may
  * not use this file except in compliance with the License. You may
  * obtain a copy of the License at
  *
  * http://www.osedu.org/licenses/ECL-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS"
  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing
  * permissions and limitations under the License.
  */
 package net.unicon.kaltura.service;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.unicon.kaltura.MediaItem;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Modified;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.sling.commons.osgi.OsgiUtil;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventHandler;
 import org.sakaiproject.nakamura.api.connections.ConnectionManager;
 import org.sakaiproject.nakamura.api.doc.ServiceDocumentation;
 import org.sakaiproject.nakamura.api.files.FileUploadHandler;
 import org.sakaiproject.nakamura.api.files.FilesConstants;
 import org.sakaiproject.nakamura.api.lite.ClientPoolException;
 import org.sakaiproject.nakamura.api.lite.Repository;
 import org.sakaiproject.nakamura.api.lite.Session;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
 import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
 import org.sakaiproject.nakamura.api.lite.authorizable.User;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.api.lite.content.ContentManager;
 import org.sakaiproject.nakamura.api.messagebucket.MessageBucketService;
 import org.sakaiproject.nakamura.api.profile.ProfileService;
 import org.sakaiproject.nakamura.api.search.solr.SolrSearchServiceFactory;
 import org.sakaiproject.nakamura.api.user.BasicUserInfoService;
 import org.sakaiproject.nakamura.api.user.UserConstants;
 import org.sakaiproject.nakamura.lite.content.InternalContent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.kaltura.client.KalturaApiException;
 import com.kaltura.client.KalturaClient;
 import com.kaltura.client.KalturaConfiguration;
 import com.kaltura.client.enums.KalturaEditorType;
 import com.kaltura.client.enums.KalturaMediaType;
 import com.kaltura.client.enums.KalturaSessionType;
 import com.kaltura.client.services.KalturaBaseEntryService;
 import com.kaltura.client.services.KalturaSessionService;
 import com.kaltura.client.types.KalturaBaseEntry;
 import com.kaltura.client.types.KalturaBaseEntryFilter;
 import com.kaltura.client.types.KalturaBaseEntryListResponse;
 import com.kaltura.client.types.KalturaFilterPager;
 import com.kaltura.client.types.KalturaMediaEntry;
 import com.kaltura.client.types.KalturaMixEntry;
 
 
 /**
  * The Kaltura service which handles all the actual processing related to Kaltura
  * 
  * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
  */
 @ServiceDocumentation(
     name = "Kaltura Service",
     description = "Handles all the processing related to the kaltura media integration"
 )
 @Component(immediate = true, metatype=true)
 @Service({KalturaService.class, FileUploadHandler.class, EventHandler.class})
 public class KalturaService implements FileUploadHandler, EventHandler {
 
     private static final Logger LOG = LoggerFactory.getLogger(KalturaService.class);
 
     private static final int MAX_ITEMS_FROM_KALTURA = 1000;
     private static final int defaultWidgetWidth = 480;
     private static final int defaultWidgetHeight = 360;
 
     public static final String KALTURA_MIMETYPE_VIDEO = "kaltura/video";
     public static final String KALTURA_MIMETYPE_AUDIO = "kaltura/audio";
     public static final String KALTURA_MIMETYPE_IMAGE = "kaltura/image";
 
     @Property(value="Unicon, Inc.")
     static final String SERVICE_VENDOR = "service.vendor";
     @Property(value="Handles all the processing related to the kaltura media integration")
     static final String SERVICE_DESCRIPTION = "service.description";
     @Property(value={ KalturaService.TOPIC_CONTENT_UPDATED })
     static final String SERVICE_EVENT_TOPICS = "event.topics";
 
     @Property(intValue=111, label="Partner Id")
     private static final String KALTURA_PARTNER_ID = "kaltura.partnerid";
     @Property(value="setThisToYourKalturaSecret", label="Secret")
     private static final String KALTURA_SECRET = "kaltura.secret";
     @Property(value="setThisToYourKalturaAdminSecret", label="Admin Secret")
     private static final String KALTURA_ADMIN_SECRET = "kaltura.adminsecret";
     @Property(value="http://www.kaltura.com", label="Endpoint")
     private static final String KALTURA_ENDPOINT = "kaltura.endpoint";
     @Property(value="http://cdn.kaltura.com", label="CDN")
     private static final String KALTURA_CDN = "kaltura.cdn";
 
     /* DEFAULT set as confirmed by Kaltura (Nir) on 21 Sept 2010 @ 2300
     kaltura.player.image  - 2162571
     kaltura.player.audio  - 2158531
     kaltura.player.view  - 1522202
     kaltura.player.edit  - 1522362
     kaltura.uploader - 2011401
     kaltura.editor - 2733871
      */
     //@Property(value="2162571", label="Player - Image")
     //private static final String KALTURA_PLAYER_IMAGE = "kaltura.player.image";
     @Property(value="2158531", label="Player - Audio")
     private static final String KALTURA_PLAYER_AUDIO = "kaltura.player.audio";
     @Property(value="1522202", label="Player - Video View")
     private static final String KALTURA_PLAYER_VIEW = "kaltura.player.view";
     //@Property(value="1522362", label="Player - Video Edit")
     //private static final String KALTURA_PLAYER_EDIT = "kaltura.player.edit";
     //@Property(value="2733871", label="Player - Editor")
     //private static final String KALTURA_PLAYER_EDITOR = "kaltura.player.editor";
 
     //@Property(intValue=KalturaService.defaultWidgetWidth, label="Player - Image - Width")
     //private static final String KALTURA_PLAYER_IMAGE_WIDTH = "kaltura.player.image.width";
     //@Property(intValue=KalturaService.defaultWidgetHeight, label="Player - Image - Height")
     //private static final String KALTURA_PLAYER_IMAGE_HEIGHT = "kaltura.player.image.height";
     @Property(intValue=KalturaService.defaultWidgetWidth, label="Player - Audio - Width")
     private static final String KALTURA_PLAYER_AUDIO_WIDTH = "kaltura.player.audio.width";
     @Property(intValue=30, label="Player - Audio - Height")
     private static final String KALTURA_PLAYER_AUDIO_HEIGHT = "kaltura.player.audio.height";
     @Property(intValue=KalturaService.defaultWidgetWidth, label="Player - Video - Width")
     private static final String KALTURA_PLAYER_VIDEO_WIDTH = "kaltura.player.video.width";
     @Property(intValue=KalturaService.defaultWidgetHeight, label="Player - Video - Height")
     private static final String KALTURA_PLAYER_VIDEO_HEIGHT = "kaltura.player.video.height";
 
     KalturaConfiguration kalturaConfig;
     String kalturaCDN = null;
     /*
      * The kaltura widget ids from config
      */
     String kalturaPlayerIdImage = null;
     String kalturaPlayerIdAudio = null;
     String kalturaPlayerIdView = null;
     String kalturaPlayerIdEdit = null;
     String kalturaEditorId = null;
     /*
      * widgets sizes from config
      */
     int kalturaPlayerImageWidth = KalturaService.defaultWidgetWidth;
     int kalturaPlayerImageHeight = KalturaService.defaultWidgetHeight;
     int kalturaPlayerAudioWidth = KalturaService.defaultWidgetWidth;
     int kalturaPlayerAudioHeight = 30;
     int kalturaPlayerVideoWidth = KalturaService.defaultWidgetWidth;
     int kalturaPlayerVideoHeight = KalturaService.defaultWidgetHeight;
 
     // SERVICES
 
     @Reference
     Repository repository;
 
     @Reference
     ConnectionManager connectionManager;
 
     @Reference
     ProfileService profileService;
 
     @Reference
     MessageBucketService messageBucketService;
 
     @Reference
     SolrSearchServiceFactory searchServiceFactory;
 
     @Reference
     BasicUserInfoService basicUserInfoService;
 
     // OSGI INIT CODE
 
     @Activate
     protected void activate(Map<?, ?> properties) {
         LOG.info("Kaltura: start");
         init(properties);
     }
 
     @Deactivate
     protected void deactivate(Map<?, ?> properties) {
         LOG.info("Kaltura: stop");
     }
 
     @Modified
     protected void modified(Map<?, ?> properties) {
         LOG.info("Kaltura: modified config");
         init(properties);
     }
 
     /**
      * Initialize the configuration based on the OSGi config properties for this service
      * @param properties map of config settings
      */
     protected void init(Map<?, ?> properties) {
         // load up the config
         int kalturaPartnerId = getConfigurationSetting(KALTURA_PARTNER_ID, -1, properties);
         String kalturaSecret = getConfigurationSetting(KALTURA_SECRET, null, properties);
         String kalturaAdminSecret = getConfigurationSetting(KALTURA_ADMIN_SECRET, null, properties);
         String kalturaEndpoint = getConfigurationSetting(KALTURA_ENDPOINT, null, properties);
         this.kalturaCDN = getConfigurationSetting(KALTURA_CDN, null, properties);
 
         // supports customizing the look and feel AND functionality of the kaltura widgets
         //this.kalturaPlayerIdImage = getConfigurationSetting(KALTURA_PLAYER_IMAGE, "2162571", properties);
         this.kalturaPlayerIdAudio = getConfigurationSetting(KALTURA_PLAYER_AUDIO, "2158531", properties);
         this.kalturaPlayerIdView = getConfigurationSetting(KALTURA_PLAYER_VIEW, "1522202", properties);
         //this.kalturaPlayerIdEdit = getConfigurationSetting(KALTURA_PLAYER_EDIT, "1522362", properties);
         //this.kalturaEditorId = getConfigurationSetting(KALTURA_PLAYER_EDITOR, "2733871", properties);
 
         // allows for config of the sizes of the players
         //this.kalturaPlayerImageWidth = getConfigurationSetting(KALTURA_PLAYER_IMAGE_WIDTH, this.kalturaPlayerImageWidth, properties);
         //this.kalturaPlayerImageHeight = getConfigurationSetting(KALTURA_PLAYER_IMAGE_HEIGHT, this.kalturaPlayerImageHeight, properties);
         this.kalturaPlayerAudioWidth = getConfigurationSetting(KALTURA_PLAYER_AUDIO_WIDTH, this.kalturaPlayerAudioWidth, properties);
         this.kalturaPlayerAudioHeight = getConfigurationSetting(KALTURA_PLAYER_AUDIO_HEIGHT, this.kalturaPlayerAudioHeight, properties);
         this.kalturaPlayerVideoWidth = getConfigurationSetting(KALTURA_PLAYER_VIDEO_WIDTH, this.kalturaPlayerVideoWidth, properties);
         this.kalturaPlayerVideoHeight = getConfigurationSetting(KALTURA_PLAYER_VIDEO_HEIGHT, this.kalturaPlayerVideoHeight, properties);
 
         MediaItem.setDefaultSizes(
                 this.kalturaPlayerImageWidth, this.kalturaPlayerImageHeight,
                 this.kalturaPlayerAudioWidth, this.kalturaPlayerAudioHeight,
                 this.kalturaPlayerVideoWidth, this.kalturaPlayerVideoHeight
         );
 
         // create the shared kaltura config
         KalturaConfiguration kc = new KalturaConfiguration();
         kc.setPartnerId(kalturaPartnerId);
         kc.setSecret(kalturaSecret);
         kc.setAdminSecret(kalturaAdminSecret);
         kc.setEndpoint(kalturaEndpoint);
         this.kalturaConfig = kc;
         // dump the config
         dumpServiceConfigToLog(properties);
 
         // test out that the kc can initialize a session
         KalturaClient kalturaClient = makeKalturaClient("admin", KalturaSessionType.ADMIN, 10);
         if (kalturaClient == null || kalturaClient.getSessionId() == null) {
             throw new RuntimeException("Failed to connect to kaltura server endpoint ("+kc.getEndpoint()+") as admin");
         }
         kalturaClient = makeKalturaClient("admin", KalturaSessionType.USER, 10);
         if (kalturaClient == null || kalturaClient.getSessionId() == null) {
             throw new RuntimeException("Failed to connect to kaltura server endpoint ("+kc.getEndpoint()+") as user");
         }
         LOG.info("Kaltura: Init complete: API version: "+kalturaClient.getApiVersion()+", Connected to endpoint: "+kc.getEndpoint());
     }
 
     /**
      * Special logging method
      * @param properties
      */
     private void dumpServiceConfigToLog(Map<?, ?> properties) {
         String propsDump="";
         if (properties != null && LOG.isDebugEnabled()) {
             StringBuilder sb = new StringBuilder();
             sb.append("\n Properties:\n");
             for (Map.Entry<?, ?> entry : properties.entrySet()) {
                 sb.append("  * ");
                 sb.append(entry.getKey());
                 sb.append(" -> ");
                 sb.append(entry.getValue());
                 sb.append("\n");
             }
             propsDump = sb.toString();
         }
         LOG.info("\nKalturaService Configuration: START ---------\n"
                 +" partnerId="+this.kalturaConfig.getPartnerId()+"\n"
                 +" endPoint="+this.kalturaConfig.getEndpoint()+"\n"
                 +" timeout="+this.kalturaConfig.getTimeout()+"\n"
                 +" kalturaCDN="+this.kalturaCDN+"\n"
                 +" kalturaEditorId="+this.kalturaEditorId+"\n"
                 +" kalturaPlayerIdView="+this.kalturaPlayerIdView+"\n"
                 +" kalturaPlayerIdEdit="+this.kalturaPlayerIdEdit+"\n"
                 +" kalturaPlayerIdAudio="+this.kalturaPlayerIdAudio+"\n"
                 +" kalturaPlayerIdImage="+this.kalturaPlayerIdImage+"\n"
                 +propsDump
                 +"KalturaService Configuration: END ---------\n");
     }
 
     /**
      * Special logging method
      * @param properties
      * @param name
      */
     protected void dumpMapToLog(Map<?, ?> properties, String name) {
         String propsDump="";
         if (properties != null) {
             StringBuilder sb = new StringBuilder();
             for (Map.Entry<?, ?> entry : properties.entrySet()) {
                 sb.append("  * ");
                 sb.append(entry.getKey());
                 sb.append(" -> ");
                 sb.append(entry.getValue());
                 sb.append("\n");
             }
             propsDump = sb.toString();
         }
         LOG.info("\nMap ("+name+"): START ---------\n"
                 +propsDump
                 +"Map ("+name+"): END ---------\n");
     }
 
     /**
      * Special method for handling retrieval of OAE config settings in a typesafe way
      * @param <T>
      * @param settingName the key for the setting
      * @param defaultValue the default value if unset
      * @param properties the set of properties to search
      * @return the value of the setting (if set) or default value if not
      */
     @SuppressWarnings("unchecked")
     private <T> T getConfigurationSetting(String settingName, T defaultValue, Map<?,?> properties) {
         T returnValue = defaultValue;
         Object propValue = properties.get(settingName);
         if (defaultValue == null) {
             returnValue = (T) OsgiUtil.toString(propValue, null);
             if ("".equals(returnValue)) {
                 returnValue = null;
             }
         } else {
             if (defaultValue instanceof Number) {
                 int num = ((Number) defaultValue).intValue();
                 int value = OsgiUtil.toInteger(propValue, num);
                 returnValue = (T) Integer.valueOf(value);
             } else if (defaultValue instanceof Boolean) {
                 boolean bool = ((Boolean) defaultValue).booleanValue();
                 boolean value = OsgiUtil.toBoolean(propValue, bool);
                 returnValue = (T) Boolean.valueOf(value);
             } else if (defaultValue instanceof String) {
                 returnValue = (T) OsgiUtil.toString(propValue, (String) defaultValue);
             }
         }
         return returnValue;
     }
 
     protected static final String TOPIC_CONTENT_UPDATED = "org/sakaiproject/nakamura/lite/content/UPDATED";
     private static final String TOPIC_PROPERTY_POOLID = "path";
     /* (non-Javadoc)
      * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
      */
     public void handleEvent(Event event) {
         /* NOTES: Q&A from AZ to Carl and Mark
         > 1) What's the constant for
         > "org/sakaiproject/nakamura/lite/content/UPDATED"? grep did not find it
         > so I am guessing it is probably constructed.
 
         Yep, other spots in the code seem to just hard-code the strings.  I'd
         normally look in org.sakaiproject.nakamura.api.lite.StoreListener for
         such a constant, but I already did and there isn't one :)
 
 
         > 2) Is that going to be the "topic" for the event? If so, where do I
         > get the poolId/path for the content item once I have the event? If
         > not, what is that matching?
 
         Yep, org/sakaiproject/nakamura/lite/content/UPDATED is the topic.  You
         can get the pathId with:
 
          event.getProperty("path")
 
         (again, most places in the code just seem to use "path" instead of a
         constant...).  You can look at the events getting fired by hitting:
 
          http://localhost:8080/system/console/events
 
         and the ones you're interested in will just have an unadorned path like
         "h1o6Hi3ie".  In mine I see other events with the same topic but paths
         like "/activity/content/h1o6Hi3ie" too, but those aren't relevant of
         interest here and can be ignored.
 
         So yeah, it's all a bit hairy :)  If that all makes sense I'll try to
         amend the doco for FileUploadHandler to emphasise that getting notified
         about uploaded files is only half the story.
 
         NOTES:
         - this is called 16 times on a new item upload and about 12 times for content updates and 6 times for update version file uploads,
           as a result we cannot just update kaltura each time this is called or it will crush the kaltura servers
 
         - I attempted to compare the original and current properties to see if I could identify when they changed but this did not work
           as the results in the logs show: 
             ... realUpdate=false, (az-test.mov)=(az-test.mov),(null)=(null)
             ... realUpdate=false, (aaaaaaaaaa)=(aaaaaaaaaa),(bbbbbb)=(bbbbbb)
         
         - Attempting to use the 'update' property to filter down the number of events - "update".equals(event.getProperty("op"),
           this only gets it down to 3 events so still too many to be reasonable
 
        - added in a filter to check if the _versionHistoryId is present, this seems to finally get it down to only 1 update
          */
         boolean updateEvent = "update".equals(event.getProperty("op"));
         String poolId = (String) event.getProperty(TOPIC_PROPERTY_POOLID);
         if (poolId != null && updateEvent) {
             Content content = getContent(poolId);
             if (content != null) {
                 // check for the key
                 String kalturaEntryId = (String) content.getProperties().get(OAE_CONTENT_NEW_FLAG);
                 String versionHistoryId = (String) content.getProperties().get(InternalContent.VERSION_HISTORY_ID_FIELD);
                 boolean realUpdate = versionHistoryId != null && content.getOriginalProperties().containsKey(OAE_CONTENT_NEW_FLAG);
                 if (kalturaEntryId != null && realUpdate) {
                     /*
                      * If it gets to this point it means 3 things are true:
                      *  (1) This is a kaltura content item which has not be updated to kaltura server yet
                      *  (2) This event type operation is an update
                      *  (3) The content item has the version history id set
                      */
                     //dumpMapToLog(content.getProperties(), "contentProperties - "+kalturaEntryId);
                     LOG.info("Found kaltura content item ("+poolId+") to update during OAE content update with keid ("+kalturaEntryId+")...");
                     // make the kaltura entry to update it
                     KalturaBaseEntry kbe = new KalturaBaseEntry();
                     kbe.id = kalturaEntryId;
                     int version = getContentVersion(poolId);
                     kbe.name = makeKalturaTitle(content.getProperties(), version);
                     kbe.description = (String)content.getProperties().get(FilesConstants.SAKAI_DESCRIPTION); // may be blank
                     kbe.tags = makeKalturaTags(content.getProperties());
                     updateKalturaItem(null, kbe);
 
                     // remove the flag and update the kaltura updated timestamp
                     Map<String, Object> props = new HashMap<String, Object>(2);
                     props.put(OAE_CONTENT_NEW_FLAG, null);
                     props.put("kaltura-updated", new Date().getTime());
                     updateContent(poolId, props); // exception if update fails
                     //dumpMapToLog(newProps, "updatedContentProperties");
                     LOG.info("Updated OAE content item ("+poolId+") and synced Kaltura item ("+kalturaEntryId+") data");
                 }
             }
         }
     }
 
     // OAE FILE UPLOAD HANDLER
 
     private static final String OAE_CONTENT_NEW_FLAG = "kaltura-content-new";
     private static final String OAE_CONTENT_EXTENSION = "sakai:fileextension";
 
     /*
      * NOTE: requires https://github.com/marktriggs/nakamura/tree/fileuploadhandlers for now
      * 
      * Handling requires some SPECIAL work here because of weaknesses in OAE:
      * 1) When user uploads a new file, we process the call to handle method and
      * put in fake meta-info and then put a marker in the content properties
      * 2) When next post comes in a few ms later, the event processor method is called 
      * which will tell us that the content has been updated, we
      * check for the marker and if it is there then we have to do a second call
      * over to kaltura to update the 3 values for title, desc, and tags, then
      * we clear the marker
      * 3) When the user later on updates the content, we have to ignore those
      * updates which would trigger calls to that interface as long as the
      * marker is not present
      * 4) When the user uploads a new version, we have to process that upload via
      * the handle method and also update the metadata in one operation
      * because in that case the metadata is correct
      * 
      * (non-Javadoc)
      * @see org.sakaiproject.nakamura.api.files.FileUploadHandler#handleFile(java.lang.String, java.io.InputStream, java.lang.String, boolean)
      */
     public void handleFile(String poolId, InputStream inputStream, String userId, boolean isNew) throws IOException {
         Map<String, Object> contentProperties = getContentProperties(poolId);
         //dumpMapToLog(contentProperties, "contentProperties");
         // check if this is a video file and do nothing if it is not
         String mimeType = (String)contentProperties.get(InternalContent.MIMETYPE_FIELD);
         String fileExtension = (String)contentProperties.get(OAE_CONTENT_EXTENSION);
         String path = (String)contentProperties.get(InternalContent.PATH_FIELD);
         String fileName = path+fileExtension;
 
         // NOTE: no handling for images yet
         KalturaMediaType mediaType = KalturaMediaType.VIDEO;
         boolean isVideo = isFileVideo(fileExtension, mimeType);
         boolean isAudio = false;
         if (!isVideo) {
             isAudio = isFileAudio(fileExtension, mimeType);
             if (isAudio) {
                 mediaType = KalturaMediaType.AUDIO;
             }
         }
 
         if ( userId != null && UserConstants.ANON_USERID.equals(userId)) {
             // only include real users, no anonymous ones
             LOG.warn("Anonymous user uploaded a file - it is not being processed into Kaltura: "+fileName);
         } else if (!isVideo && !isAudio) {
             if (!isAudio) {
                 LOG.debug("Uploaded file is not an audio file, no processing for Kaltura: "+fileName);
             } else {
                 LOG.debug("Uploaded file is not a video, no processing for Kaltura: "+fileName);
             }
         } else {
             //String fileId = (String)contentProperties.get(InternalContent.UUID_FIELD);
             int version = 1;
             if (isNew) {
                 // do something different when this is new
 
             } else {
                 // do things when this is an update to an existing content item
                 version = getContentVersion(poolId); // exception if lookup fails
             }
             String title = makeKalturaTitle(contentProperties, version);
             String desc = (String)contentProperties.get(FilesConstants.SAKAI_DESCRIPTION); // may be blank
             String tags = makeKalturaTags(contentProperties);
             // do processing of the video file
             long fileSize = (Long) contentProperties.get(InternalContent.LENGTH_FIELD);
             KalturaBaseEntry kbe = uploadItem(userId, fileName, fileSize, inputStream, mediaType, title, desc, tags); // exception if upload fails
             if (kbe != null) {
                 // item upload successful
                 MediaItem mediaItem = new MediaItem(kbe, userId);
 
                 Map<String, Object> props = new HashMap<String, Object>(10);
                 if (isNew) {
                     // if this is newly uploaded content then we have to do special handling, store the kaltura entry ID here
                     props.put(OAE_CONTENT_NEW_FLAG, mediaItem.getKalturaId());
                 }
                 props.put("kaltura-updated", new Date().getTime());
                 props.put("kaltura-id", mediaItem.getKalturaId());
                 props.put("kaltura-thumbnail", mediaItem.getThumbnail());
                 props.put("kaltura-download", mediaItem.getDownloadURL());
                 props.put("kaltura-duration", mediaItem.getDuration()); // probably will be 0
                 props.put("kaltura-height", mediaItem.getHeight());
                 props.put("kaltura-width", mediaItem.getWidth());
                 props.put("kaltura-type", mediaItem.getType());
                 String kalturaMimeType = KALTURA_MIMETYPE_VIDEO;
                 if (MediaItem.TYPE_AUDIO.equals(mediaItem.getMediaType())) {
                     kalturaMimeType = KALTURA_MIMETYPE_AUDIO;
                 } else if (MediaItem.TYPE_IMAGE.equals(mediaItem.getMediaType())) {
                     kalturaMimeType = KALTURA_MIMETYPE_IMAGE;
                 }
                 props.put(InternalContent.MIMETYPE_FIELD, kalturaMimeType);
 
                 LOG.info("Completed upload ("+title+") to Kaltura of file ("+fileName+") of type ("+kalturaMimeType+") and created kalturaEntry ("+mediaItem.getKalturaId()+")");
 
                 updateContent(poolId, props); // exception if update fails
                 // Map<String, Object> newProps = ...
                 //dumpMapToLog(newProps, "newContentProperties");
             } else {
                 // should we fail here if kaltura does not return a valid KBE? -AZ
             }
             LOG.info("Kaltura file upload handler complete: "+fileName);
         }
     }
 
     /**
      * Make a title to be sent to Kaltura
      * @param contentProperties OAE content properties
      * @param version the content version (greater than or equal to 1)
      * @return the title to send to kaltura
      */
     private String makeKalturaTitle(Map<String, Object> contentProperties, int version) {
         String title = "title";
         if (contentProperties.get(FilesConstants.POOLED_CONTENT_FILENAME) != null) {
             title = (String) contentProperties.get(FilesConstants.POOLED_CONTENT_FILENAME);
         }
         if (version < 1) {
             version = 1;
         }
         title += " - "+version;
         return title;
     }
 
     /**
      * Make the tags to be send to kaltura based on OAE content
      * @param contentProperties OAE content properties
      * @return the tags comma separated string (empty string if there are none)
      */
     private String makeKalturaTags(Map<String, Object> contentProperties) {
         String tags = "";
         if (contentProperties.get(FilesConstants.SAKAI_TAGS) != null) {
             // convert tags array into CSV string
             String[] fileTags = (String[]) contentProperties.get(FilesConstants.SAKAI_TAGS);
             if (fileTags.length > 0) {
                 StringBuilder sb = new StringBuilder();
                 for (int i = 0; i < fileTags.length; i++) {
                     String tag = fileTags[i];
                     if (i > 0) {
                         sb.append(",");
                     }
                     sb.append(tag);
                 }
                 tags = sb.toString();
             }
         }
         return tags;
     }
 
 
     // OAE processing methods
 
     /**
      * Find the current version number (same as the number of versions) for this content item
      * @param poolId the content pool id
      * @return the current version number (defaults to 1)
      */
     private int getContentVersion(String poolId) {
         // NOTE: InternalContent.VERSION_NUMBER_FIELD is not useful
         int version = 1;
         try {
             Session adminSession = repository.loginAdministrative();
             ContentManager cm = adminSession.getContentManager();
             // Content content = cm.getVersion(poolId, fileId);
             List<String> versions = cm.getVersionHistory(poolId);
             version = versions.size();
             adminSession.logout();
         } catch (Exception e) {
             LOG.error("Unable to get versions for pool="+poolId+", defaulting to "+version+": "+e, e);
         }
         return version;
     }
 
     /**
      * Retrieve an OAE content item
      * @param poolId the unique path/poolId of a content object
      * @return the Content object
      * @throws RuntimeException if the content object cannot be retrieved
      */
     private Content getContent(String poolId) {
         Content content = null;
         try {
             Session adminSession = repository.loginAdministrative();
             ContentManager cm = adminSession.getContentManager();
             content = cm.get(poolId);
             adminSession.logout();
         } catch (Exception e) {
             LOG.error("Unable to get content by path="+poolId+": "+e, e);
             throw new RuntimeException("Unable to get content by path="+poolId+": "+e, e);
         }
         if (content == null) {
             throw new RuntimeException("Unable to get content by path="+poolId+": item not found");
         }
         return content;
     }
 
     /**
      * Retrieve the properties for some OAE content
      * @param poolId the unique path/poolId of a content object
      * @return the Map of content properties
      * @throws RuntimeException if the content object cannot be retrieved
      */
     private Map<String, Object> getContentProperties(String poolId) {
         Content content = getContent(poolId);
         return content.getProperties();
     }
 
     /**
      * Update an OAE content item
      * @param poolId the unique path/poolId of a content object
      * @param properties the properties to update or delete on this object (props with a NULL value will be removed, all others will be replaced or added)
      * @return the complete set of new properties for the content
      * @throws RuntimeException if the content object cannot be updated
      */
     private Map<String, Object> updateContent(String poolId, Map<?, ?> properties) {
         Map<String, Object> props = null;
         Content contentItem = getContent(poolId);
         //dumpMapToLog(properties, "NEW-properties");
         for (Entry<?, ?> entry : properties.entrySet()) {
             String key = (String) entry.getKey();
             Object val = entry.getValue();
             if (val != null) {
                 contentItem.setProperty(key, val);
             } else {
                 contentItem.removeProperty(key);
             }
         }
         try {
             Session adminSession = repository.loginAdministrative();
             ContentManager contentManager = adminSession.getContentManager();
             contentManager.update(contentItem);
             Content content = contentManager.get(poolId);
             props = content.getProperties();
             adminSession.logout();
             LOG.debug("Completed update of content item props ("+poolId+") for Kaltura upload");
         } catch (Exception e) {
             LOG.error("Unable to update content at path="+poolId+": "+e, e);
             throw new RuntimeException("Unable to update content at path="+poolId+": "+e, e);
         }
         return props;
     }
 
     /**
      * Determine if a file has video content
      * @param fileExtension the file extension (includes the ., e.g. .mov)
      * @param mimeType the mimetype from the UI
      * @return true if video, false otherwise
      */
     protected boolean isFileVideo(String fileExtension, String mimeType) {
         boolean video = false;
         if (KALTURA_MIMETYPE_VIDEO.equals(mimeType) || mimeType.startsWith("video/")) {
             video = true;
         } else {
             if (fileExtension.equals(".avi")  // avi
                     || fileExtension.equals(".mpg") // mpeg 2
                     || fileExtension.equals(".mpe") // mpeg 2
                     || fileExtension.equals(".mpeg") // mpeg 2
                     || fileExtension.equals(".mp4") // mpeg 4
                     || fileExtension.equals(".m4v") // mpeg 4
                     || fileExtension.equals(".mov") // quicktime
                     || fileExtension.equals(".qt") // quicktime
                     || fileExtension.equals(".asf") // windows media
                     || fileExtension.equals(".asx") // windows media
                     || fileExtension.equals(".wmv") // windows media
                     || fileExtension.equals(".rm") // real video
                     || fileExtension.equals(".ogm") // OG media
                     || fileExtension.equals(".3gp") // 3gpp
                     || fileExtension.equals(".mkv") // matroska
             ) {
                 video = true;
             }
         }
         return video;
     }
 
     /**
      * Determine if a file has audio content
      * @param fileExtension the file extension (includes the ., e.g. .mov)
      * @param mimeType the mimetype from the UI
      * @return true if audio, false otherwise
      */
     protected boolean isFileAudio(String fileExtension, String mimeType) {
         boolean audio = false;
         if (KALTURA_MIMETYPE_AUDIO.equals(mimeType) || mimeType.startsWith("audio/")) {
             audio = true;
         } else {
             if (fileExtension.equals(".wav")  // wave audio
                     || fileExtension.equals(".aif") // aiff
                     || fileExtension.equals(".mp3") // mpeg 3
                     || fileExtension.equals(".aac") // aac
                     || fileExtension.equals(".mid") // midi
                     || fileExtension.equals(".mpa") // mpeg 2 audio
                     || fileExtension.equals(".wma") // windows media audio
                     || fileExtension.equals(".ra") // realaudio
             ) {
                 audio = true;
             }
         }
         return audio;
     }
 
     /**
      * Get a user and their data based on the user identifier
      * @param userId user id (username)
      * @return the User object OR null if not found
      */
     protected User getUser(String userId) {
         User u = null;
         Session adminSession = null;
         try {
             adminSession = repository.loginAdministrative();
             AuthorizableManager authorizableManager = adminSession.getAuthorizableManager();
             Authorizable authorizable = authorizableManager.findAuthorizable(userId);
             u = (User) authorizable;
             adminSession.logout();
         } catch (StorageClientException e) {
             // nothing to do here
         } catch (AccessDeniedException e) {
             // nothing to do here
         } finally {
             if ( adminSession != null ) {
                 try {
                     adminSession.logout();
                 } catch (ClientPoolException e) {
                     LOG.warn(e.getMessage(), e);
                 }
             }
         }
         return u;
     }
 
 
     // KALTURA CLIENT
 
     /*
      * NOTE: the KalturaClient is not even close to being threadsafe -AZ
      */
     ThreadLocal<KalturaClient> kctl = new ThreadLocal<KalturaClient>() {
         @Override
         protected KalturaClient initialValue() {
             return makeKalturaClient();
         };
     };
     /**
      * threadsafe method to get a kaltura client
      * @return the current kaltura client for this thread
      */
     public KalturaClient getKalturaClient() {
         return kctl.get();
     }
     /**
      * threadsafe method to get a kaltura client
      * @param userKey the user key (normally should be the username)
      * @return the current kaltura client for this thread
      */
     public KalturaClient getKalturaClient(String userKey) {
         if (userKey != null && !"".equals(userKey)) {
             KalturaClient kc = makeKalturaClient(userKey, KalturaSessionType.ADMIN, 0);
             kctl.set(kc);
         }
         return kctl.get();
     }
     /**
      * destroys the current kaltura client
      */
     public void clearKalturaClient() {
         kctl.remove();
     }
 
     /**
      * NOTE: this method will generate a new kaltura client using all defaults and sakai user, 
      * make sure you store this into the {@link #kctl} threadlocal if you are generating it using this method
      */
     private KalturaClient makeKalturaClient() {
         // defaults
         String userKey = "anonymous";
         KalturaSessionType sessionType = KalturaSessionType.USER;
         // NOTE: there is no way to get the user outside of a request in OAE
         KalturaClient kc = makeKalturaClient(userKey, sessionType, 0);
         return kc;
     }
 
     /**
      * NOTE: this method will generate a new kaltura client, 
      * make sure you store this into the {@link #kctl} threadlocal if you are generating it using this method
      */
     private KalturaClient makeKalturaClient(String userKey, KalturaSessionType sessionType, int timeoutSecs) {
         // client is not threadsafe
         if (timeoutSecs <= 0) {
             timeoutSecs = 86400; // NOTE set to 24 hours by request of kaltura   60; // default to 60 seconds
         }
         KalturaClient kalturaClient = new KalturaClient(this.kalturaConfig);
         String secret = this.kalturaConfig.getSecret();
         if (KalturaSessionType.ADMIN.equals(sessionType)) {
             secret = this.kalturaConfig.getAdminSecret();
         }
         KalturaSessionService sessionService = kalturaClient.getSessionService();
         try {
             String sessionId = sessionService.start(secret, userKey, sessionType, 
                     this.kalturaConfig.getPartnerId(), timeoutSecs, "edit:*"); // the edit is needed to fix an issue with kaltura servers
             kalturaClient.setSessionId(sessionId);
             LOG.debug("Created new kaltura client (oid="+kalturaClient.toString()+", tid="+Thread.currentThread().getId()+", ks="+kalturaClient.getSessionId()+")");
         } catch (KalturaApiException e) {
             //kalturaClient.setSessionId(null); // should we clear this?
             LOG.error("Unable to establish a kaltura session ("+kalturaClient.toString()+", "+kalturaClient.getSessionId()+"):: " + e, e);
         }
         return kalturaClient;
     }
 
 
     // KALTURA METHODS
 
     public KalturaBaseEntry uploadItem(String userId, String fileName, long fileSize, InputStream inputStream, 
             KalturaMediaType mediaType, String title, String description, String tags) {
         if (title == null || "".equals(title)) {
             title = fileName;
         }
         if (mediaType == null) {
             mediaType = KalturaMediaType.VIDEO;
         }
         KalturaMediaEntry kme = null;
         KalturaClient kc = getKalturaClient(userId); // force this to be an admin key
         if (kc != null) {
             try {
                 String uploadTokenId = kc.getMediaService().upload(inputStream, fileName, fileSize);
                 //LOG.info("upload token result: "+uploadTokenId);
                 KalturaMediaEntry mediaEntry = new KalturaMediaEntry();
                 mediaEntry.mediaType = KalturaMediaType.VIDEO;
                 mediaEntry.userId = userId;
                 mediaEntry.name = title;
                 if (description != null) {
                     mediaEntry.description = description;
                 }
                 if (tags != null) {
                     mediaEntry.tags = tags;
                 }
                 mediaEntry.adminTags = "OAE"; // Should we handle with custom meta fields instead (for 9 July 2011, we will not)?
                 kme = kc.getMediaService().addFromUploadedFile(mediaEntry, uploadTokenId);
                 //kme = kc.getBaseEntryService().update(entryId, mediaEntry); // NOTE: updateKalturaItem()
             } catch (Exception e) {
                 LOG.error("Failure uploading item ("+fileName+"): "+e, e);
                 throw new RuntimeException(e);
             }
         }
         return kme;
     }
 
     /**
      * @param textFilter a search filter string, null or "" includes all
      * @param keids [OPTIONAL] listing of keids to limit the results to
      * @param start 0 for all, or >0 start with that item
      * @param max 0 for all, or >0 to only return that many
      * @return the List of kaltura entries
      */
     public List<KalturaBaseEntry> getKalturaItems(String userKey, String textFilter, String[] keids, int start, int max) {
         if (start < 0) {
             start = 0;
         }
         if (max <= 0) {
             max = MAX_ITEMS_FROM_KALTURA;
         }
         List<KalturaBaseEntry> items = new ArrayList<KalturaBaseEntry>();
         if (textFilter == null) {
             textFilter = "";
         }
         KalturaClient kc = getKalturaClient();
         if (kc != null) {
             try {
                 // use base entry service instead to get all -AZ
                 //KalturaBaseEntry kbe = entryService.get("qqqq");
                 KalturaBaseEntryService entryService = kc.getBaseEntryService();
                 KalturaBaseEntryFilter filter = new KalturaBaseEntryFilter();
                 filter.partnerIdEqual = this.kalturaConfig.getPartnerId();
                 filter.userIdEqual = userKey;
                 if (StringUtils.isNotBlank(textFilter)) {
                     filter.searchTextMatchOr = textFilter; // I think this is what I need but it does not seem to prioritize results?
                     //filter.nameLike = textFilter;
                 }
                 filter.statusIn = "0,1,2"; // KalturaEntryStatus.IMPORT+","+KalturaEntryStatus.PRECONVERT+","+KalturaEntryStatus.READY;
                 // limit to a set of items as needed
                 if (keids != null) {
                     filter.idIn = StringUtils.join(keids, ',');
                 }
                 //kmef.orderBy = "title";
                 KalturaFilterPager pager = new KalturaFilterPager();
                 pager.pageSize = max;
                 pager.pageIndex = 0; // NOTE - kaltura does not support a start item in the paging API, only a start page
                 KalturaBaseEntryListResponse listResponse = entryService.list(filter, pager);
                 for (KalturaBaseEntry entry : listResponse.objects) {
                     items.add(entry); // KalturaMediaEntry KalturaMixEntry
                 }
             } catch (KalturaApiException e) {
                 LOG.error("Unable to get kaltura media items listing using session (oid="+kc.toString()+", tid="+Thread.currentThread().getId()+", ks="+kc.getSessionId()+"):: " + e, e);
             }
         }
         return items;
     }
 
     /**
      * Retrieve a single KME by the kaltura id
      * @param keid the kaltura entry id
      * @return the entry OR null if none found
      */
     public KalturaBaseEntry getKalturaItem(String userKey, String keid) {
         if (keid == null) {
             throw new IllegalArgumentException("keid must not be null");
         }
         KalturaBaseEntry kme = null;
         KalturaClient kc = getKalturaClient();
         if (kc != null) {
             try {
                 //KalturaMediaService mediaService = kc.getMediaService();
                 KalturaBaseEntryService entryService = kc.getBaseEntryService();
                 kme = getKalturaEntry(userKey, keid, entryService);
             } catch (KalturaApiException e) {
                 LOG.error("Unable to get kaltura media item ("+keid+") using session (oid="+kc.toString()+", tid="+Thread.currentThread().getId()+", ks="+kc.getSessionId()+"):: " + e, e);
             }
         }
         return kme;
     }
 
     public boolean removeKalturaItem(String userKey, String keid) {
         if (keid == null) {
             throw new IllegalArgumentException("keid must not be null");
         }
         boolean removed = false;
         KalturaClient kc = getKalturaClient();
         if (kc != null) {
             try {
                 KalturaBaseEntryService entryService = kc.getBaseEntryService();
                 KalturaBaseEntry entry = getKalturaEntry(userKey, keid, entryService);
                 entryService.delete(entry.id);
                 removed = true;
             } catch (KalturaApiException e) {
                 LOG.error("Unable to remove kaltura item ("+keid+") using session (oid="+kc.toString()+", tid="+Thread.currentThread().getId()+", ks="+kc.getSessionId()+"):: " + e, e);
                 removed = false;
             }
         }
         return removed;
     }
 
     /**
      * Creates a new kaltura mix for the current user/kaltura session from an existing kaltura entry
      * @param keid the id of the entry to create this mix from
      * @param name OPTIONAL the name for this new mix, null to use the entry name
      * @return the new mix item
      * @throws IllegalStateException if the mix cannot be created
      */
     public KalturaMixEntry createMix(String userKey, String keid, String name) {
         if (keid == null) {
             throw new IllegalArgumentException("keid must not be null");
         }
         KalturaMixEntry kmix = null;
         KalturaClient kc = getKalturaClient();
         if (kc != null) {
             try {
                 KalturaBaseEntry kme = getKalturaItem(userKey, keid);
                 if (kme == null) {
                     throw new IllegalArgumentException("Invalid keid ("+keid+"), cannot find entry");
                 }
                 KalturaMixEntry mix = new KalturaMixEntry();
                 mix.name = name != null ? name : kme.name;
                 mix.editorType = KalturaEditorType.ADVANCED;
                 kmix = kc.getMixingService().add(mix);
                 // append existing entry to this mix
                 kc.getMixingService().appendMediaEntry(kmix.id, kme.id);
                 // flattening is async, no way to tell if a mix has been flattened?
                 //kc.getMixingService().requestFlattening(entryId, fileFormat);s
             } catch (KalturaApiException e) {
                 throw new IllegalStateException("Unable to create new mix ("+name+") using session (oid="+kc.toString()+", tid="+Thread.currentThread().getId()+", ks="+kc.getSessionId()+"):: " + e, e);
             }
         }
         return kmix;
     }
 
     public KalturaBaseEntry updateKalturaItem(String userKey, KalturaBaseEntry kalturaEntry) {
         if (kalturaEntry == null) {
             throw new IllegalArgumentException("entry must not be null");
         }
         String keid = kalturaEntry.id;
         if (keid == null) {
             throw new IllegalArgumentException("entry keid must not be null");
         }
         KalturaBaseEntry kbe = null;
         KalturaClient kc = getKalturaClient();
         if (kc != null) {
             try {
                 KalturaBaseEntryService entryService = kc.getBaseEntryService();
                 kbe = getKalturaEntry(userKey, keid, entryService);
                 if (kbe == null) {
                     throw new IllegalArgumentException("Cannot find KME to update using id ("+keid+")");
                 }
                 // integrate the fields we allow to be changed
                 KalturaBaseEntry fields = new KalturaBaseEntry();
                 //fields.creditUrl = entry.creditUrl;
                 //fields.creditUserName = entry.creditUserName;
                 fields.description = kalturaEntry.description;
                 fields.name = kalturaEntry.name;
                 fields.tags = kalturaEntry.tags;
                 // now update the KME
                 kbe = entryService.update(keid, fields);
             } catch (KalturaApiException e) {
                 String msg = "Unable to update kaltura media item ("+keid+") using session (oid="+kc.toString()+", tid="+Thread.currentThread().getId()+", ks="+kc.getSessionId()+"):: " + e;
                 LOG.error(msg, e);
                 throw new RuntimeException(msg, e);
             }
         }
         return kbe;
     }
 
 
     /**
      * Get the KME with a permissions check to make sure the user key matches
      * @param keid the kaltura entry id
      * @param entryService the katura entry service
      * @return the entry
      * @throws KalturaApiException if kaltura cannot be accessed
      * @throws IllegalArgumentException if the keid cannot be found for this user
      */
     private KalturaBaseEntry getKalturaEntry(String userKey, String keid, KalturaBaseEntryService entryService) throws KalturaApiException {
         // DO NOT CACHE THIS ONE
         KalturaBaseEntry entry = null;
         // Cannot use the KMEF because it cannot filter by id correctly -AZ
         /*
         KalturaBaseEntryFilter kmef = new KalturaBaseEntryFilter();
         kmef.partnerIdEqual = this.kalturaConfig.getPartnerId();
         kmef.userIdEqual = currentUserName;
         kmef.idEqual = keid;
         //kmef.orderBy = "title";
         KalturaMediaListResponse listResponse = mediaService.list(kmef);
         if (listResponse != null && ! listResponse.objects.isEmpty()) {
             kme = listResponse.objects.get(0); // just get the first one
         }
          */
         // have to use - mediaService.get(keid); despite it not even checking if we have access to this - AZ
         entry = entryService.get(keid);
         if (entry == null) {
             // did not find the item by keid so we die
             throw new IllegalArgumentException("Cannot find kaltura item ("+keid+") with for user ("+userKey+")");
         }
         // also do a manual check for security, not so sure about this check though -AZ
         if (entry.partnerId != this.kalturaConfig.getPartnerId()) {
             throw new SecurityException("KME partnerId ("+entry.partnerId+") does not match current one ("+this.kalturaConfig.getPartnerId()+"), cannot access this KME ("+keid+")");
         }
         return entry;
     }
 
 }
