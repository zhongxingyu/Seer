 package com.psddev.dari.util;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 /**
  * {@link StorageItem} stored in
  * <a href="http://www.brightcove.com/">Brightcove</a>.
  *
  * <p>To use this class, it must be able to access the
  * <a href="http://hc.apache.org/httpcomponents-client-ga/httpmime/">HttpMime library</a>.
  * If you use Maven, you should add the following dependency:</p>
  *
  * <blockquote><pre><code data-type="xml">{@literal
  *<dependency>
  *    <groupId>org.apache.httpcomponents</groupId>
  *    <artifactId>httpmime</artifactId>
  *    <version>4.0.1</version>
  *</dependency>}</code></pre></blockquote>
  */
 public class BrightcoveStorageItem extends AbstractStorageItem implements StorageItem {
 
     /** Setting key for Brightcove read service url. */
     public static final String READ_SERVICE_URL_SETTING = "readServiceUrl";
 
     /** Setting key for Brightcove write service url. */
     public static final String WRITE_SERVICE_URL_SETTING = "writeServiceUrl";
 
     /** Setting key for Brightcove read token key. */
     public static final String READ_TOKEN_SETTING = "readToken";
 
     /** Setting key for Brightcove read (URL) token key. */
     public static final String READ_URL_TOKEN_SETTING = "readUrlToken";
 
     /** Setting key for Brightcove secret key. */
     public static final String WRITE_TOKEN_SETTING = "writeToken";
 
     /** Setting key for Brightcove encoding format. */
     public static final String DEFAULT_ENCODING_SETTING = "defaultEncoding";
 
     /** Setting key for Brightcove key for preview player */
     public static final String PREVIEW_PLAYER_KEY_SETTING = "previewPlayerKey";
 
     /** Setting key for Brightcove player ID for preview player */
     public static final String PREVIEW_PLAYER_ID_SETTING = "previewPlayerId";
 
     private transient String readServiceUrl;
     private transient String writeServiceUrl;
     private transient String readToken;
     private transient String readUrlToken;
     private transient String writeToken;
     private transient Encoding defaultEncoding;
     private transient String previewPlayerKey;
     private transient String previewPlayerId;
 
     /** Returns the Brigthcove service URL. */
     public String getReadServiceUrl() {
         return readServiceUrl;
     }
 
     /** Sets the Brightcove service URL */
     public void setReadServiceUrl(String readServiceUrl) {
         this.readServiceUrl = readServiceUrl;
     }
 
     /** Returns the Brigthcove service URL. */
     public String getWriteServiceUrl() {
         return writeServiceUrl;
     }
 
     /** Sets the Brightcove service URL */
     public void setWriteServiceUrl(String writeServiceUrl) {
         this.writeServiceUrl = writeServiceUrl;
     }
 
     /** Returns the Brightcove read token. */
     public String getReadToken() {
         return readToken;
     }
 
     /** Sets the Brightcove read token. */
     public void setReadToken(String readToken) {
         this.readToken = readToken;
     }
 
     /** Returns the Brightcove read (URL) token. */
     public String getReadUrlToken() {
         return readUrlToken;
     }
 
     /** Sets the Brightcove read (URL) token. */
     public void setReadUrlToken(String readUrlToken) {
         this.readUrlToken = readUrlToken;
     }
 
     /** Returns the Brightcove write token. */
     public String getWriteToken() {
         return writeToken;
     }
 
     /** Sets the Brightcove write token. */
     public void setWriteToken(String writeToken) {
         this.writeToken = writeToken;
     }
 
     /** Returns the Brightcove encoding format. */
     public Encoding getDefaultEncoding() {
         return defaultEncoding;
     }
 
     /** Sets the Brightcove encoding format. */
     public void setDefaultEncoding(Encoding encoding) {
         this.defaultEncoding = encoding;
     }
 
     /** Returns the Brightcove key for the preview player */
     public String getPreviewPlayerKey() {
         return previewPlayerKey;
     }
 
     /** Sets the Brightcove key for the preview player */
     public void setPreviewPlayerKey(String previewPlayerKey) {
         this.previewPlayerKey = previewPlayerKey;
     }
 
     /** Returns the Brightcove player ID for the preview player */
     public String getPreviewPlayerId() {
         return previewPlayerId;
     }
 
     /** Sets the Brightcove player ID for the preview player */
     public void setPreviewPlayerId(String previewPlayerId) {
         this.previewPlayerId = previewPlayerId;
     }
 
     /* Object fields mirror the Video type definition for the BrightcoveAPI on http://support.brightcove.com/en/docs/media-api-objects-reference#Video */
 
     /* The title of the Video, limited to 255 characters. The name is a required property when you create a video. */
     private String name;
 
     /* A number that uniquely identifies this Video, assigned by Video Cloud when the Video is created. */
     /* READ-ONLY */
     private String brightcoveId;
 
     /* A user-specified id that uniquely identifies the Video, limited to 150 characters. A referenceId can be used as a foreign-key to identify this video in another system. Note that that the find_videos_by_reference_ids method cannot handle a referenceId that contain commas, so you may want to avoid using commas in referenceId values. */
     private String referenceId;
 
     /* A number, assigned by Video Cloud, that uniquely identifies the account to which the Video belongs. */
     /* READ-ONLY */
     private String accountId;
 
     /* Encoding specification, used to override any defaultEncoding specified through DEFAULT_ENCODING_SETTING */
     private Encoding encoding;
 
     /* A short description describing the Video, limited to 250 characters. The shortDescription is a required property when you create a video. */
     private String shortDescription;
 
     /* A longer description of this Video, limited to 5000 characters. */
     private String longDescription;
 
     /* The URL of the video file for this Video. Note that this property can be accessed with the Media API only with a special read or write token. This property applies, no matter whether the video's encoding is FLV (VP6) or MP4 (H.264). See Accessing Video Content with the Media API. */
     /* READ-ONLY */
     private String flvUrl;
 
     /* The date this Video was created, represented as the number of milliseconds since the UNIX epoch. */
     /* READ-ONLY */
     private Date creationDate;
 
     /* The date this Video was last made active, represented as the number of milliseconds since the UNIX epoch. */
     /* READ-ONLY */
     private Date publishedDate;
 
     /* The date this Video was last modified, represented as the number of milliseconds since the UNIX epoch. */
     private Date lastModifiedDate;
 
     /* An ItemStateEnum. One of the properties: ACTIVE, INACTIVE, or DELETED. You can set this property only to ACTIVE or INACTIVE; you cannot delete a video by setting its itemState to DELETED. */
     private ItemState itemState;
 
     /* The first date this Video is available to be played, represented as the number of milliseconds since the UNIX epoch. */
     private Date startDate;
 
     /* The last date this Video is available to be played, represented as the number of milliseconds since the UNIX epoch. */
     private Date endDate;
 
     /* An optional URL to a related item, limited to 255 characters. */
     private String linkUrl;
 
     /* The text displayed for the linkURL, limited to 255 characters. */
     private String linkText;
 
     /* A list of Strings representing the tags assigned to this Video. Each tag can be not more than 128 characters, and a video can have no more than 1200 tags. */
     private List<String> tags;
 
     /* The URL to the video still image associated with this Video. Video stills are 480x360 pixels. */
     /* READ-ONLY */
     private String videoStillUrl;
 
     /* The URL to the thumbnail image associated with this Video. Thumbnails are 120x90 pixels. */
     /* READ-ONLY */
     private String thumbnailUrl;
 
     /* The length of this video in milliseconds. */
     /* READ-ONLY */
     private Long length;
 
     /* A map of names and values for custom fields set up for videos in your account. More information and examples. */
     private Map<String, String> customFields;
 
     /* An EconomicsEnum. Either FREE or AD_SUPPORTED. AD_SUPPORTED means that ad requests are enabled for the Video. */
     private Economics economics;
 
     /* A string representing the ad key/value pairs assigned to the video. Key/value pairs are formatted as key=value and are separated by ampersands (&). For example: "adKeys":"category=sports&live=true"  */
     private Map<String, String> adKeys;
 
     /* True indicates that the video is geo-restricted. */
     private Boolean geoRestricted;
 
     /* A list of the ISO-3166 two-letter codes of the countries to enforce geo-restriction rules on. Use lowercase for the country codes. */
     private List<String> geoFilteredCountries;
 
     /* If true, the video can be viewed in all countries except those listed in geoFilteredCountries; if false, the video can be viewed only in the countries listed in geoFilteredCountries. */
     private Boolean geoFilterExclude;
 
     /* How many times the Video has been played since its creation. */
     /* READ-ONLY */
     private Integer playsTotal;
 
     /* How many times the Video has been played within the past seven days, exclusive of today. */
     /* READ-ONLY */
     private Integer playsTrailingWeek;
 
     /** Returns the name field, used in Brightcove */
     public String getName() {
         return name;
     }
 
     /** Sets the name field, used in Brightcove */
     public void setName(String name) {
         this.name = name;
     }
 
     /** Returns the Brightcove video ID */
     public String getBrightcoveId() {
         return brightcoveId;
     }
 
     /** Sets the Brightcove ID. */
     /** Intentionally private.  Property is READ-ONLY in Brightcove */
     private void setBrightcoveId(String brightcoveId) {
         this.brightcoveId = brightcoveId;
     }
 
     /** Gets the Reference ID, used in Brightcove */
     public String getReferenceId() {
         return referenceId;
     }
 
     /** Sets the Reference ID, used in Brightcove */
     public void setReferenceId(String referenceId) {
         this.referenceId = referenceId;
     }
 
     /** Returns the Account ID, used in Brightcove */
     public String getAccountId() {
         return accountId;
     }
 
     /** Sets the Account ID field, used in Brightcove */
     /** Intentionally private.  Property is READ-ONLY in Brightcove */
     private void setAccountId(String accountId) {
         this.accountId = accountId;
     }
 
     /** Returns the encoding to be specified when creating a video in Brightcove */
     public Encoding getEncoding() {
         return encoding;
     }
 
     /** Sets encoding to be specified when creating a video in Brightcove */
     public void setEncoding(Encoding encoding) {
         this.encoding = encoding;
     }
 
     /** Returns the shortDescription field, used in Brightcove */
     public String getShortDescription() {
         return shortDescription;
     }
 
     /** Sets the shortDescription field, used in Brightcove */
     public void setShortDescription(String shortDescription) {
         this.shortDescription = shortDescription;
     }
 
     /** Returns the longDescription field, used in Brightcove */
     public String getLongDescription() {
         return longDescription;
     }
 
     /** sets the longDescription field, used in Brightcove */
     public void setLongDescription(String longDescription) {
         this.longDescription = longDescription;
     }
 
     /** Returns the FLV URL from Brightcove */
     public String getFlvUrl() {
         return flvUrl;
     }
 
     /** Sets the FLV URL property */
     /** Intentionally private.  Property is READ-ONLY in Brightcove */
     private void setFlvUrl(String flvUrl) {
         this.flvUrl = flvUrl;
     }
 
     public Date getCreationDate() {
         return creationDate;
     }
 
     /** Intentionally private.  Property is READ-ONLY in Brightcove */
     private void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
     }
 
     public Date getPublishedDate() {
         return publishedDate;
     }
 
     /** Intentionally private.  Property is READ-ONLY in Brightcove */
     private void setPublishedDate(Date publishedDate) {
         this.publishedDate = publishedDate;
     }
 
     public Date getLastModifiedDate() {
         return lastModifiedDate;
     }
 
     /** Intentionally private.  Property is READ-ONLY in Brightcove */
     private void setLastModifiedDate(Date lastModifiedDate) {
         this.lastModifiedDate = lastModifiedDate;
     }
 
     public ItemState getItemState() {
         return itemState;
     }
 
     public void setItemState(ItemState itemState) {
         this.itemState = itemState;
     }
 
     public Date getStartDate() {
         return startDate;
     }
 
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     public Date getEndDate() {
         return endDate;
     }
 
     public void setEndDate(Date endDate) {
         this.endDate = endDate;
     }
 
     public String getLinkUrl() {
         return linkUrl;
     }
 
     public void setLinkUrl(String linkUrl) {
         this.linkUrl = linkUrl;
     }
 
     public String getLinkText() {
         return linkText;
     }
 
     public void setLinkText(String linkText) {
         this.linkText = linkText;
     }
 
     public List<String> getTags() {
         return tags;
     }
 
     public void setTags(List<String> tags) {
         this.tags = tags;
     }
 
     public String getVideoStillUrl() {
         return videoStillUrl;
     }
 
     /** Intentionally private.  Property is READ-ONLY in Brightcove */
     private void setVideoStillUrl(String videoStillUrl) {
         this.videoStillUrl = videoStillUrl;
     }
 
     public String getThumbnailUrl() {
         return thumbnailUrl;
     }
 
     /** Intentionally private.  Property is READ-ONLY in Brightcove */
     private void setThumbnailUrl(String thumbnailUrl) {
         this.thumbnailUrl = thumbnailUrl;
     }
 
     public Long getLength() {
         return length;
     }
 
     /** Intentionally private.  Property is READ-ONLY in Brightcove */
     private void setLength(Long length) {
         this.length = length;
     }
 
     public Map<String, String> getCustomFields() {
         return customFields;
     }
 
     public void setCustomFields(Map<String, String> customFields) {
         this.customFields = customFields;
     }
 
     public Economics getEconomics() {
         return economics;
     }
 
     public void setEconomics(Economics economics) {
         this.economics = economics;
     }
 
     public Map<String, String> getAdKeys() {
         return adKeys;
     }
 
     public void setAdKeys(Map<String, String> adKeys) {
         this.adKeys = adKeys;
     }
 
     public Boolean getGeoRestricted() {
         return geoRestricted;
     }
 
     public void setGeoRestricted(Boolean geoRestricted) {
         this.geoRestricted = geoRestricted;
     }
 
     public List<String> getGeoFilteredCountries() {
         return geoFilteredCountries;
     }
 
     public void setGeoFilteredCountries(List<String> geoFilteredCountries) {
         this.geoFilteredCountries = geoFilteredCountries;
     }
 
     public Boolean getGeoFilterExclude() {
         return geoFilterExclude;
     }
 
     public void setGeoFilterExclude(Boolean geoFilterExclude) {
         this.geoFilterExclude = geoFilterExclude;
     }
 
     public Integer getPlaysTotal() {
         return playsTotal;
     }
 
     /** Intentionally private.  Property is READ-ONLY in Brightcove */
     private void setPlaysTotal(Integer playsTotal) {
         this.playsTotal = playsTotal;
     }
 
     public Integer getPlaysTrailingWeek() {
         return playsTrailingWeek;
     }
 
     /** Intentionally private.  Property is READ-ONLY in Brightcove */
     private void setPlaysTrailingWeek(Integer playsTrailingWeek) {
         this.playsTrailingWeek = playsTrailingWeek;
     }
 
     // --- AbstractStorageItem support ---
 
     @Override
     public void initialize(String settingsKey, Map<String, Object> settings) {
         super.initialize(settingsKey, settings);
 
         setReadServiceUrl(ObjectUtils.to(String.class, settings.get(READ_SERVICE_URL_SETTING)));
         if(ObjectUtils.isBlank(getReadServiceUrl())) {
             throw new SettingsException(settingsKey + "/" + READ_SERVICE_URL_SETTING, "No read service url!");
         }
 
         setWriteServiceUrl(ObjectUtils.to(String.class, settings.get(WRITE_SERVICE_URL_SETTING)));
         if(ObjectUtils.isBlank(getWriteServiceUrl())) {
             throw new SettingsException(settingsKey + "/" + WRITE_SERVICE_URL_SETTING, "No write service url!");
         }
 
         setReadToken(ObjectUtils.to(String.class, settings.get(READ_TOKEN_SETTING)));
         if (ObjectUtils.isBlank(getReadToken())) {
             throw new SettingsException(settingsKey + "/" + READ_TOKEN_SETTING, "No read token!");
         }
 
         setWriteToken(ObjectUtils.to(String.class, settings.get(WRITE_TOKEN_SETTING)));
         if (ObjectUtils.isBlank(getWriteToken())) {
             throw new SettingsException(settingsKey + "/" + WRITE_TOKEN_SETTING, "No write token!");
         }
 
         setReadUrlToken(ObjectUtils.to(String.class, settings.get(READ_URL_TOKEN_SETTING)));
         if (ObjectUtils.isBlank(getReadUrlToken())) {
             throw new SettingsException(settingsKey + "/" + READ_URL_TOKEN_SETTING, "No read url token!");
         }
 
         setDefaultEncoding(ObjectUtils.to(Encoding.class, settings.get(DEFAULT_ENCODING_SETTING)));
 
         setPreviewPlayerKey(ObjectUtils.to(String.class, settings.get(PREVIEW_PLAYER_KEY_SETTING)));
 
         setPreviewPlayerId(ObjectUtils.to(String.class, settings.get(PREVIEW_PLAYER_ID_SETTING)));
     }
 
     @Override
     public String getPublicUrl() {
 
         String url = null;
 
         StringBuilder requestUrl = new StringBuilder();
         HttpClient client = new DefaultHttpClient();
 
         try {
             requestUrl.append(getReadServiceUrl());
             requestUrl.append("?command=find_video_by_id");
            requestUrl.append("&token=").append(getReadUrlToken());
            requestUrl.append("&video_id=").append(getBrightcoveId());
             requestUrl.append("&video_fields=FLVURL");
 
             HttpGet httpGet = new HttpGet(requestUrl.toString());
 
             HttpResponse response = client.execute(httpGet);
 
             StringWriter writer = new StringWriter();
             IOUtils.copy(response.getEntity().getContent(), writer);
 
             Map<String, Object> videoJson = (Map<String, Object>) ObjectUtils.fromJson(writer.toString());
 
             if(!ObjectUtils.isBlank(videoJson)) {
                 url = videoJson.get("FLVURL").toString();
             }
         } catch(IOException ex) {
             throw new IllegalStateException("Could not retrieve FLV_URL from Brightcove!");
         } finally {
             try {
                 client.getConnectionManager().shutdown();
             } catch (Exception ignore) {
                 throw new IllegalStateException("Could not close connection to Brightcove API!");
             }
         }
 
         return url;
     }
 
     @Override
     protected InputStream createData() throws MalformedURLException, IOException {
 
         String publicUrl = getPublicUrl();
         if(!ObjectUtils.isBlank(publicUrl)) {
             URL url = new URL(publicUrl);
             return url.openStream();
         }
 
         return null;
     }
 
     @Override
     protected void saveData(InputStream data) throws IOException {
 
         Map<String, Object> requestJson = new HashMap<String, Object>();
         requestJson.put("method", "create_video");
 
         Map<String, Object> params = new HashMap<String, Object>();
 
         // set writeToken as the "token" parameter or throw an exception if it's.
         if(this.writeToken != null) {
             params.put("token", this.writeToken);
         } else {
             throw new IllegalStateException("writeToken is null.");
         }
 
         // encoding is required by Brightcove, so throw an error if it's not set.
         if(this.encoding != null) {
             params.put("encode_to", this.encoding.name());
         } else if(this.defaultEncoding != null) {
             params.put("encode_to", this.defaultEncoding.name());
         }
 
         // name is required by Brightcove, so throw an error if it's not set.
         if(this.name == null) {
             throw new IllegalStateException("name is null.");
         }
 
         // shortDescription is required by Brightcove, so throw an error if it's not set.
         if(this.shortDescription == null) {
             throw new IllegalStateException("shortDescription is null.");
         }
 
         params.put("video", buildVideoJson());
         params.put("filename", getPath());
 
         requestJson.put("params", params);
 
         HttpClient client = new DefaultHttpClient();
 
         try {
 
             HttpPost httpPost = new HttpPost(getWriteServiceUrl());
 
             MultipartEntity reqEntity = new MultipartEntity();
 
             reqEntity.addPart("JSON-RPC", new StringBody(ObjectUtils.toJson(requestJson)));
 
             // create a temporary File.  This is necessary, because the Content-Length attribute cannot
             // be known otherwise.  Using InputStreamBody instead of FileBody in the HttpPost request method
             // results in a cryptic "error 100 - UnknownServerError" response from BrightCove.
 
             File storageFile = File.createTempFile(getPath().replaceAll("/", ""), ".video");
             storageFile.deleteOnExit();
 
             try {
                 FileOutputStream foStream = new FileOutputStream(storageFile);
                 try {
                     IOUtils.copy(data, foStream);
                 } finally {
                     foStream.close();
                 }
 
                 FileBody fileBody = new FileBody(storageFile);
                 reqEntity.addPart(getPath(), fileBody);
                 httpPost.setEntity(reqEntity);
 
                 HttpResponse response = client.execute(httpPost);
 
                 StringWriter writer = new StringWriter();
                 IOUtils.copy(response.getEntity().getContent(), writer);
 
                 Map<String, Object> responseJson = (Map<String, Object>) ObjectUtils.fromJson(writer.toString());
 
                 if(!ObjectUtils.isBlank(responseJson)) {
                     String brightcoveId = ObjectUtils.to(String.class, responseJson.get("result"));
                     if(!ObjectUtils.isBlank(brightcoveId)) {
                         setBrightcoveId(brightcoveId);
                     }
                 }
 
             } finally {
                 storageFile.delete();
             }
         } finally {
             try { client.getConnectionManager().shutdown(); } catch (Exception ignore) { }
         }
     }
 
     /**
      * This method is explicitly without an InputStream parameter, because I am not implementing support for updating
      * the raw video data for a video in Brightcove.  If new video data is needed, a new BrightcoveStorageItem should
      * be created.  If the old video data needs to be removed, call .delete() on it before replacing it with a new
      * BrightcoveStorageItem.
      * @throws IOException
      */
     public Map<String, Object> pushToBrightcove() {
 
         try {
             Map<String, Object> requestJson = new HashMap<String, Object>();
             requestJson.put("method", "update_video");
 
             // set writeToken as the "token" parameter or throw an exception if it's.
             Map<String, Object> params = new HashMap<String, Object>();
             if(this.writeToken != null) {
                 params.put("token", this.writeToken);
             } else {
                 throw new IllegalStateException("writeToken is null.");
             }
 
             Map<String, Object> video = buildVideoJson();
 
             params.put("video", video);
             requestJson.put("params", params);
 
             HttpClient client = new DefaultHttpClient();
             try {
 
                 HttpPost httpPost = new HttpPost(getWriteServiceUrl());
 
                 MultipartEntity reqEntity = new MultipartEntity();
 
                 reqEntity.addPart("JSON-RPC", new StringBody(ObjectUtils.toJson(requestJson)));
 
                 httpPost.setEntity(reqEntity);
 
                 HttpResponse response = client.execute(httpPost);
 
                 StringWriter writer = new StringWriter();
                 IOUtils.copy(response.getEntity().getContent(), writer);
 
                 Map<String, Object> resultJson = (Map<String, Object>) ObjectUtils.fromJson(writer.toString());
 
                 if(!ObjectUtils.isBlank(resultJson.get("error"))) {
                     return null;
                 } else {
                     Map<String, Object> videoJson = (Map<String, Object>) resultJson.get("result");
                     update(videoJson);
                     return videoJson;
                 }
             } finally {
                 try { client.getConnectionManager().shutdown(); } catch (Exception ignore) { }
             }
         } catch(IOException e) {
             return null;
         }
     }
 
     public boolean pullFromBrightcove() {
 
         try {
             update(fetchVideoJsonFromBrightcove());
             return true;
         } catch(IOException e) {
             return false;
         }
     }
 
     private void update(Map<String, Object> videoJson) {
 
         if(!ObjectUtils.isBlank(videoJson.get("id"))) {
             this.brightcoveId = ObjectUtils.to(String.class, videoJson.get("id"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("name"))) {
             this.name = ObjectUtils.to(String.class, videoJson.get("name"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("shortDescription"))) {
             this.shortDescription = ObjectUtils.to(String.class, videoJson.get("shortDescription"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("longDescription"))) {
             this.longDescription = ObjectUtils.to(String.class, videoJson.get("longDescription"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("creationDate"))) {
             this.creationDate = ObjectUtils.to(Date.class, videoJson.get("creationDate"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("publishedDate"))) {
             this.publishedDate = ObjectUtils.to(Date.class, videoJson.get("publishedDate"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("lastModifiedDate"))) {
             this.lastModifiedDate = ObjectUtils.to(Date.class, videoJson.get("lastModifiedDate"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("linkURL"))) {
             this.linkUrl = ObjectUtils.to(String.class, videoJson.get("linkURL"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("linkText"))) {
             this.linkText = ObjectUtils.to(String.class, videoJson.get("linkText"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("tags"))) {
             this.tags = ObjectUtils.to(new TypeReference<List<String>>()  {}, videoJson.get("tags"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("videoStillURL"))) {
             this.videoStillUrl = ObjectUtils.to(String.class, videoJson.get("videoStillURL"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("thumbnailURL"))) {
             this.thumbnailUrl = ObjectUtils.to(String.class, videoJson.get("thumbnailURL"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("referenceId"))) {
             this.referenceId = ObjectUtils.to(String.class, videoJson.get("referenceId"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("length"))) {
             this.length = ObjectUtils.to(Long.class, videoJson.get("length"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("economics"))) {
             try {
                 this.economics = ObjectUtils.to(Economics.class, videoJson.get("economics"));
             } catch (Exception ignore) { }
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("playsTotal"))) {
             this.playsTotal = ObjectUtils.to(Integer.class, videoJson.get("playsTotal"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("playsTrailingWeek"))) {
             this.playsTrailingWeek = ObjectUtils.to(Integer.class, videoJson.get("playsTrailingWeek"));
         }
 
         if(!ObjectUtils.isBlank(videoJson.get("FLVURL"))) {
             this.flvUrl = ObjectUtils.to(String.class, videoJson.get("FLVURL"));
         }
     }
 
     public void delete() throws IOException {
 
         Map<String, Object> requestJson = new HashMap<String, Object>();
         requestJson.put("method", "delete_video");
 
         // set writeToken as the "token" parameter or throw an exception if it's.
         Map<String, Object> params = new HashMap<String, Object>();
         if(this.writeToken != null) {
             params.put("token", this.writeToken);
         } else {
             throw new IllegalStateException("writeToken is null.");
         }
     }
 
     private Map<String, Object> buildVideoJson() {
 
         Map<String, Object> videoJson = new HashMap<String, Object>();
         // set the brightcoveId as the "id" property of the "video" json property or throw an exception if it's null
         if(this.brightcoveId != null) {
             videoJson.put("id", this.brightcoveId);
         }
 
         if(this.name != null) {
             videoJson.put("name", this.name);
         }
 
         if(this.referenceId != null) {
             videoJson.put("referenceId", this.referenceId);
         }
 
         if(this.shortDescription != null) {
             videoJson.put("shortDescription", this.shortDescription);
         }
 
         if(this.longDescription != null) {
             videoJson.put("longDescription", this.longDescription);
         }
 
         if(this.itemState != null) {
             videoJson.put("itemState", this.itemState.name());
         }
 
         if(this.startDate != null) {
             videoJson.put("startDate", this.startDate);
         }
 
         if(this.endDate != null) {
             videoJson.put("endDate", this.endDate);
         }
 
         if(this.linkUrl != null) {
             videoJson.put("linkURL", this.linkUrl);
         }
 
         if(this.linkText != null) {
             videoJson.put("linkText", this.linkText);
         }
 
         if(this.tags != null && this.tags.size() > 0) {
             videoJson.put("tags", this.tags);
         }
 
         if(this.customFields != null) {
             videoJson.put("customFields", this.customFields);
         }
 
         if(this.economics != null) {
             videoJson.put("economics", this.economics.name());
         }
 
         if(this.adKeys != null) {
             List<String> keyValues = new ArrayList<String>();
             for(String key : this.adKeys.keySet()) {
                 keyValues.add(key + "=" + adKeys.get(key));
             }
             videoJson.put("adKeys", StringUtils.join(keyValues, "&"));
         }
 
         if(this.geoRestricted != null) {
             videoJson.put("geoRestricted", this.geoRestricted);
         }
 
         if(this.geoFilteredCountries != null) {
             videoJson.put("geoFilteredCountries", this.geoFilteredCountries);
         }
 
         if(this.geoFilterExclude != null) {
             videoJson.put("geoFilterExclude", this.geoFilterExclude);
         }
 
         return videoJson;
     }
 
     private Map<String, Object> fetchVideoJsonFromBrightcove() throws IOException {
 
         StringBuilder requestUrl = new StringBuilder();
 
         // start with the readServiceUrl or throw an exception if it's null
         if(this.readServiceUrl != null) {
             requestUrl.append(this.readServiceUrl);
         } else {
             throw new IllegalStateException("readServiceUrl is null.");
         }
 
         // set the brightcoveId as a "video_id" parameter or throw an exception if it's null
         if(getBrightcoveId() != null) {
             // append "find_video_by_id" as the "command" parameter (differs significantly from the POST methods)
             requestUrl.append("?command=find_video_by_id");
            requestUrl.append("&video_id=").append(getBrightcoveId());
         } else if(getReferenceId() != null) {
             // append "find_vide_by_reference_id" as the "command" parameter
             requestUrl.append("?command=find_video_by_reference_id");
            requestUrl.append("&reference_id=").append(getReferenceId());
         } else {
             throw new IllegalStateException("brightcoveId and referenceId are both null.");
         }
 
         // set readUrlToken or readToken as the "token" parameter or throw an exception if both are null.
         if(this.readUrlToken != null) {
            requestUrl.append("&token=").append(this.readUrlToken);
         } else if(this.readToken != null) {
            requestUrl.append("&token=").append(this.readToken);
         } else {
             throw new IllegalStateException("Both readToken and readUrlToken are null.");
         }
 
         HttpClient client = new DefaultHttpClient();
 
         try {
             HttpGet httpGet = new HttpGet(requestUrl.toString());
 
             HttpResponse response = client.execute(httpGet);
 
             StringWriter writer = new StringWriter();
             IOUtils.copy(response.getEntity().getContent(), writer);
 
             Map<String, Object> resultJson = (Map<String, Object>) ObjectUtils.fromJson(writer.toString());
 
             if(ObjectUtils.isBlank(resultJson)) {
                 if(getBrightcoveId() != null) {
                     throw new IllegalStateException("No video was found in Brightcove with brightcoveId = " + getBrightcoveId());
                 } else if(getReferenceId() != null) {
                     throw new IllegalStateException("No video was found in Brightcove with referenceId = " + getReferenceId());
                 } else {
                     throw new IllegalStateException("This point should never be reached; both brightcoveId and referenceId are null!");
                 }
 
             } else {
                 return resultJson;
             }
 
         } finally {
             try { client.getConnectionManager().shutdown(); } catch (Exception ignore) { }
         }
     }
 
     @Override
     public boolean isInStorage() {
 
         return false;
     }
 
     public static enum ItemState {
         ACTIVE,
         INACTIVE,
         DELETED
     }
 
     public static enum Economics {
         FREE,
         AD_SUPPORTED
     }
 
     public static enum Encoding {
         FLV,
         MP4
     }
 }
