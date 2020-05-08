 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 
 package org.mule.module.facebook;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.mule.api.annotations.Configurable;
 import org.mule.api.annotations.Connector;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.oauth.OAuth2;
 import org.mule.api.annotations.oauth.OAuthAccessToken;
 import org.mule.api.annotations.oauth.OAuthAccessTokenIdentifier;
 import org.mule.api.annotations.oauth.OAuthConsumerKey;
 import org.mule.api.annotations.oauth.OAuthConsumerSecret;
 import org.mule.api.annotations.oauth.OAuthProtected;
 import org.mule.api.annotations.oauth.OAuthScope;
 import org.mule.api.annotations.param.Default;
 import org.mule.api.annotations.param.Optional;
 import org.mule.module.facebook.types.GetApplicationTaggedResponseType;
 import org.mule.module.facebook.types.GetUserAccountResponseType;
 import org.mule.module.facebook.types.Member;
 import org.mule.module.facebook.types.OutboxThread;
 import org.mule.module.facebook.types.Thread;
 import org.mule.modules.utils.MuleSoftException;
 
 import com.restfb.DefaultJsonMapper;
 import com.restfb.JsonMapper;
 import com.restfb.json.JsonObject;
 import com.restfb.types.Album;
 import com.restfb.types.Application;
 import com.restfb.types.Checkin;
 import com.restfb.types.Comment;
 import com.restfb.types.Event;
 import com.restfb.types.Group;
 import com.restfb.types.Insight;
 import com.restfb.types.Link;
 import com.restfb.types.NamedFacebookType;
 import com.restfb.types.Note;
 import com.restfb.types.Page;
 import com.restfb.types.PageConnection;
 import com.restfb.types.Photo;
 import com.restfb.types.Post;
 import com.restfb.types.Post.Likes;
 import com.restfb.types.StatusMessage;
 import com.restfb.types.User;
 import com.restfb.types.Video;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.representation.Form;
 import com.sun.jersey.multipart.FormDataMultiPart;
 import com.sun.jersey.multipart.file.FileDataBodyPart;
 import com.sun.jersey.multipart.impl.MultiPartWriter;
 
 /**
  * Facebook is a social networking service and website launched in February 2004.
  * 
  * @author MuleSoft, inc.
  */
 @Connector(name = "facebook", schemaVersion = "2.0", friendlyName="Facebook", minMuleVersion="3.5", configElementName="config-with-oauth")
 @OAuth2(accessTokenUrl = "https://graph.facebook.com/oauth/access_token", authorizationUrl = "https://graph.facebook.com/oauth/authorize",
         accessTokenRegex = "access_token=([^&]+?)&", expirationRegex = "expires=([^&]+?)$")
 public class FacebookConnector {
 
     private static final Logger logger = Logger.getLogger(FacebookConnector.class);
 	private static String FACEBOOK_URI = "https://graph.facebook.com";
     private static String ACCESS_TOKEN_QUERY_PARAM_NAME = "access_token";
     private static JsonMapper mapper = new DefaultJsonMapper();
     
     /**
      * The application identifier as registered with Facebook
      */
     @Configurable
     @OAuthConsumerKey
     private String consumerKey;
 
     /**
      * The application secret
      */
     @Configurable
     @OAuthConsumerSecret
     private String consumerSecret;
 
     /**
      * Facebook permissions
      */
     @Configurable
     @Optional
     @Default(value = "email,read_stream,publish_stream")
     @OAuthScope
     private String scope;
 
     /**
      * Jersey client
      */
     private Client client;
 
     /**
      * Constructor
      */
     public FacebookConnector()
     {
     	ClientConfig config = new DefaultClientConfig();
     	config.getClasses().add(MultiPartWriter.class);
         client = Client.create(config);
     }
     
     @OAuthAccessToken
     private String accessToken;
     
     private String userId;
     
     @OAuthAccessTokenIdentifier
     public String getUserId() {
     	if (this.userId == null) {
     		
     		if (StringUtils.isEmpty(this.accessToken)) {
     			if (logger.isDebugEnabled()) {
     				logger.debug("No access token yet available. Returning null as user id");
     			}
     			return null;
     		}
     		
     		User user = this.loggedUserDetails();
     		this.userId = user.getUsername();
     	}
     	
     	return this.userId;
     }
     
     /**
      * Gets the user logged details.
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:logged-user-details}
      * 
      * @return response from Facebook the actual user.
      */
     @Processor
 	@OAuthProtected
     public User loggedUserDetails()
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("me").build();
         WebResource resource = this.newWebResource(uri, accessToken);
         String json = resource.queryParam("access_token", accessToken).type(MediaType.APPLICATION_FORM_URLENCODED).get(String.class);
         return mapper.toJavaObject(json, User.class);
     }
     
     /**
      * Search over all public posts in the social graph
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:search-posts}
      * 
      * @param q The search string
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of posts
      */
     @Processor
 	@OAuthProtected
     public List<Post> searchPosts(String q,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("100") String limit,
                                   @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("search").build();
         WebResource resource = this.newWebResource(uri, accessToken);
         
         final String jsonResponse = resource.queryParam("q", q)
                                             .queryParam("since", since)
                                             .queryParam("until", until)
                                             .queryParam("limit", limit)
                                             .queryParam("offset", offset)
                                             .queryParam("type", "post")
                                             .get(String.class);
         
         return mapper.toJavaList(jsonResponse, Post.class);
     }
     
     /**
      * Search over all users in the social graph
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:search-users}
      * 
      * @param q The search string
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of users
      */
     @Processor
 	@OAuthProtected
     public List<User> searchUsers(String q,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("100") String limit,
                                   @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("search").build();
         WebResource resource = this.newWebResource(uri, accessToken);
         final String jsonResponse = resource.queryParam("q", q)
                                             .queryParam("since", since)
                                             .queryParam("until", until)
                                             .queryParam("limit", limit)
                                             .queryParam("offset", offset)
                                             .queryParam("type", "user")
                                             .get(String.class);
         
         return mapper.toJavaList(jsonResponse, User.class);
     }
     
     /**
      * Search over all pages in the social graph
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:search-pages}
      * 
      * @param q The search string
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of pages
      */
     @Processor
 	@OAuthProtected
     public List<Page> searchPages(String q,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("100") String limit,
                                   @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("search").build();
         WebResource resource = this.newWebResource(uri, accessToken);
         final String jsonResponse = resource.queryParam("q", q)
                                             .queryParam("type", "page")
                                             .queryParam("since", since)
                                             .queryParam("until", until)
                                             .queryParam("limit", limit)
                                             .queryParam("offset", offset)
                                             .get(String.class);
         return mapper.toJavaList(jsonResponse, Page.class);
     }
     
     /**
      * Search over all events in the social graph
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:search-events}
      * 
      * @param q The search string
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of events
      */
     @Processor
 	@OAuthProtected
     public List<Event> searchEvents(String q,
                                     @Optional @Default("last week") String since,
                                     @Optional @Default("yesterday") String until,
                                     @Optional @Default("100") String limit,
                                     @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("search").build();
         WebResource resource = this.newWebResource(uri, accessToken);
         final String jsonResponse = resource.queryParam("q", q)
                                             .queryParam("type", "event")
                                             .queryParam("since", since)
                                             .queryParam("until", until)
                                             .queryParam("limit", limit)
                                             .queryParam("offset", offset)
                                             .get(String.class);
         return mapper.toJavaList(jsonResponse, Event.class);
     }
     
     /**
      * Search over all groups in the social graph
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:search-groups}
      * 
      * @param q The search string
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of groups
      */
     @Processor
 	@OAuthProtected
     public List<Group> searchGroups(String q,
                                     @Optional @Default("last week") String since,
                                     @Optional @Default("yesterday") String until,
                                     @Optional @Default("100") String limit,
                                     @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("search").build();
         WebResource resource = this.newWebResource(uri, accessToken);
         final String jsonResponse = resource.queryParam("q", q)
                                             .queryParam("type", "group")
                                             .queryParam("since", since)
                                             .queryParam("until", until)
                                             .queryParam("limit", limit)
                                             .queryParam("offset", offset)
                                             .get(String.class);
         return mapper.toJavaList(jsonResponse, Group.class);
     }
     
     /**
      * This request returns you or your friend's latest checkins,
      * or checkins where you or your friends have been tagged; currently,
      * it does not accept a query parameter.
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:search-checkins}
      * 
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of checkins
      */
     @Processor
 	@OAuthProtected
     public List<Checkin> searchCheckins(@Optional @Default("last week") String since,
                                         @Optional @Default("yesterday") String until,
                                         @Optional @Default("100") String limit,
                                         @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("search").build();
         WebResource resource = this.newWebResource(uri, accessToken);
         final String jsonResponse = resource.queryParam("type", "checkin")
                                             .queryParam("since", since)
                                             .queryParam("until", until)
                                             .queryParam("limit", limit)
                                             .queryParam("offset", offset)
                                             .get(String.class);
         return mapper.toJavaList(jsonResponse, Checkin.class);
     }
 
     /**
      * A photo album
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getAlbum}
      * 
      * @param album Represents the ID of the album object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return The album
      */
     @Processor
 	@OAuthProtected
     public Album getAlbum(String album, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{album}").build(album);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaObject( resource.queryParam("metadata", metadata).get(String.class), Album.class);
     }
 
     /**
      * The photos contained in this album
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getAlbumPhotos}
      * 
      * @param album Represents the ID of the album object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
 	@OAuthProtected
     public List<Photo> getAlbumPhotos(String album,
                                  @Optional @Default("last week") String since,
                                  @Optional @Default("yesterday") String until,
                                  @Optional @Default("100") String limit,
                                  @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{album}/photos").build(album);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Photo.class);
     }
 
     /**
      * The comments made on this album
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getAlbumComments}
      * 
      * @param album Represents the ID of the album object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
 	@OAuthProtected
     public List<Comment> getAlbumComments(String album,
                                    @Optional @Default("last week") String since,
                                    @Optional @Default("yesterday") String until,
                                    @Optional @Default("100") String limit,
                                    @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{album}/comments").build(album);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Comment.class);
     }
 
     /**
      * Specifies information about an event, including the location, event name, and
      * which invitees plan to attend.
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getEvent}
      * 
      * @param eventId Represents the ID of the event object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return response from Facebook
      */
     @Processor
 	@OAuthProtected
     public Event getEvent(String eventId, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}").build(eventId);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaObject( resource.queryParam("metadata", metadata).
 
         get(String.class), Event.class);
     }
 
     /**
      * This event's wall
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getEventWall}
      * 
      * @param eventId Represents the ID of the event object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
 	@OAuthProtected
     public List<Post> getEventWall(String eventId,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("100") String limit,
                                @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}/feed").build(eventId);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList( resource.queryParam("since", since)
                                           .queryParam("until", until)
                                           .queryParam("limit", limit)
                                           .queryParam("offset", offset)
                                           .get(String.class), Post.class);
     }
 
     /**
      * All of the users who have been not yet responded to their invitation to this
      * event
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getEventNoReply}
      * 
      * @param eventId Represents the ID of the event object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of users
      */
     @Processor
 	@OAuthProtected
     public List<User> getEventNoReply(String eventId,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("100") String limit,
                                   @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}/noreply").build(eventId);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), User.class);
     }
 
     /**
      * All of the users who have been responded "Maybe" to their invitation to this
      * event
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getEventMaybe}
      * 
      * @param eventId Represents the ID of the event object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of users
      */
     @Processor
 	@OAuthProtected
     public List<User> getEventMaybe(String eventId,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}/maybe").build(eventId);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
 						            .queryParam("since", since)
 						            .queryParam("until", until)
 						            .queryParam("limit", limit)
 						            .queryParam("offset", offset)
 						            .get(String.class), User.class);
     }
 
     /**
      * All of the users who have been invited to this event
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getEventInvited}
      * 
      * @param eventId Represents the ID of the event object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of users
      */
     @Processor
 	@OAuthProtected
     public List<User> getEventInvited(String eventId,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("100") String limit,
                                   @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}/invited").build(eventId);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList(resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), User.class);
     }
 
     /**
      * All of the users who are attending this event
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getEventAttending}
      * 
      * 
      * @param eventId Represents the ID of the event object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of users
      */
     @Processor
 	@OAuthProtected
     public List<User> getEventAttending(String eventId,
                                     @Optional @Default("last week") String since,
                                     @Optional @Default("yesterday") String until,
                                     @Optional @Default("100") String limit,
                                     @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}/attending").build(eventId);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList(resource
 						            .queryParam("since", since)
 						            .queryParam("until", until)
 						            .queryParam("limit", limit)
 						            .queryParam("offset", offset)
 						            .get(String.class), User.class);
     }
 
     /**
      * All of the users who declined their invitation to this event
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getEventDeclined}
      * 
      * @param eventId Represents the ID of the event object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of events
      */
     @Processor
 	@OAuthProtected
     public List<User> getEventDeclined(String eventId,
                                    @Optional @Default("last week") String since,
                                    @Optional @Default("yesterday") String until,
                                    @Optional @Default("100") String limit,
                                    @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}/declined").build(eventId);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList(resource
 						            .queryParam("since", since)
 						            .queryParam("until", until)
 						            .queryParam("limit", limit)
 						            .queryParam("offset", offset)
 						            .get(String.class), User.class);
     }
 
     /**
      * The event's profile picture
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getEventPicture}
      * 
      * 
      * @param eventId Represents the ID of the event object.
      * @param type One of square (50x50), small (50 pixels wide, variable height),
      *            and large (about 200 pixels wide, variable height)
      * @return The image as a Byte array
      */
     @Processor
 	@OAuthProtected
     public byte[] getEventPicture(String eventId, @Optional @Default("small") String type)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{event}/picture").build(eventId);
         WebResource resource = this.newWebResource(uri, accessToken);
         BufferedImage image = resource.queryParam("type", type).get(BufferedImage.class);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         try
         {
             ImageIO.write(image, "jpg", baos);
         }
         catch (Exception e)
         {
             MuleSoftException.soften(e);
         }
         return baos.toByteArray();
     }
 
     /**
      * A Facebook group
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getGroup}
      * 
      * 
      * @param group Represents the ID of the group object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return The group represented by the given id
      */
     @Processor
 	@OAuthProtected
     public Group getGroup(String group, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{group}").build(group);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaObject( resource.queryParam("metadata", metadata).get(String.class), Group.class);
     }
 
     /**
      * This group's wall
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getGroupWall}
      * 
      * 
      * @param group Represents the ID of the group object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of posts
      */
     @Processor
 	@OAuthProtected
     public List<Post> getGroupWall(String group,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("100") String limit,
                                @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{group}/feed").build(group);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList(resource
 						            .queryParam("since", since)
 						            .queryParam("until", until)
 						            .queryParam("limit", limit)
 						            .queryParam("offset", offset)
 						            .get(String.class), Post.class);
     }
 
     /**
      * All of the users who are members of this group
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getGroupMembers}
      * 
      * 
      * @param group Represents the ID of the group object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
 	@OAuthProtected
     public List<Member> getGroupMembers(String group,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("100") String limit,
                                   @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{group}/members").build(group);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
 							            .queryParam("since", since)
 							            .queryParam("until", until)
 							            .queryParam("limit", limit)
 							            .queryParam("offset", offset)
 							            .get(String.class), Member.class);
     }
 
     /**
      * The profile picture of this group
      * <p/>
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getGroupPicture}
      * 
      * 
      * @param group Represents the ID of the group object.
      * @param type One of square (50x50), small (50 pixels wide, variable height),
      *            and large (about 200 pixels wide, variable height)
      * @return response from Facebook
      */
     @Processor
 	@OAuthProtected
     public byte[] getGroupPicture(String group, @Optional @Default("small") String type)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{group}/picture").build(group);
         WebResource resource = this.newWebResource(uri, accessToken);
         return bufferedImageToByteArray(resource.queryParam("type", type).get(BufferedImage.class));
     }
 
     /**
      * A link shared on a user's wall 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getLink}
      * 
      * 
      * @param link Represents the ID of the link object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return The link from facebook
      */
     @Processor
 	@OAuthProtected
     public Link getLink(String link,
                         @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{link}").build(link);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaObject(resource.queryParam("metadata", metadata).get(String.class), Link.class);
     }
 
     /**
      * All of the comments on this link 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getLinkComments}
      * 
      * 
      * @param link Represents the ID of the link object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of comments
      */
     @Processor
 	@OAuthProtected
     public List<Comment> getLinkComments(String link,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("100") String limit,
                                   @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{link}/comments").build(link);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Comment.class);
     }
 
     /**
      * A Facebook note
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getNote}
      * 
      * 
      * @param note Represents the ID of the note object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return The note represented by the given id
      */
     @Processor
 	@OAuthProtected
     public Note getNote(String note, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{note}").build(note);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaObject( resource.queryParam("metadata", metadata).get(String.class), Note.class);
     }
 
     /**
      * All of the comments on this note 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getNoteComments}
      * 
      * 
      * @param note Represents the ID of the note object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of comments from the given note
      */
     @Processor
 	@OAuthProtected
     public List<Comment> getNoteComments(String note,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("100") String limit,
                                   @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{note}/comments").build(note);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList(resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Comment.class);
     }
 
     /**
      * People who like the note 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getNoteLikes}
      * 
      * 
      * @param note Represents the ID of the note object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return The links from the given note
      */
     @Processor
 	@OAuthProtected
     public Likes getNoteLikes(String note,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("100") String limit,
                                @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{note}/likes").build(note);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaObject( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Likes.class);
     }
 
     /**
      * Retrieves the page with the given ID
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPage}
      * 
      * 
      * @param page Represents the ID of the page object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return The page represented by the given id
      */
     @Processor
 	@OAuthProtected
     public Page getPage(String page, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}").build(page);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaObject( resource.queryParam("metadata", metadata).get(String.class), Page.class);
     }
 
     /**
      * The page's wall 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageWall}
      * 
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of posts from the given page wall
      */
     @Processor
 	@OAuthProtected
     public List<Post> getPageWall(String page,
                               @Optional @Default("last week") String since,
                               @Optional @Default("yesterday") String until,
                               @Optional @Default("100") String limit,
                               @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/feed").build(page);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Post.class);
     }
 
     /**
      * The page's profile picture 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPagePicture}
      * 
      * 
      * @param page Represents the ID of the page object.
      * @param type One of square (50x50), small (50 pixels wide, variable height),
      *            and large (about 200 pixels wide, variable height)
      * @return A byte array with the page picture
      */
     @Processor
 	@OAuthProtected
     public byte[] getPagePicture(String page, @Optional @Default("small") String type)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/picture").build(page);
         WebResource resource = this.newWebResource(uri, accessToken);
         return bufferedImageToByteArray( resource.queryParam("type", type).get(BufferedImage.class));
     }
 
     /**
      * The photos, videos, and posts in which this page has been tagged 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageTagged}
      * 
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of posts
      */
     @Processor
 	@OAuthProtected
     public List<Post> getPageTagged(String page,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/tagged").build(page);
         return mapper.toJavaList(this.newWebResource(uri, accessToken) 
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Post.class);
     }
 
     /**
      * The page's posted links 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageLinks}
      * 
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of this page's links
      */
     @Processor
 	@OAuthProtected
     public List<Link> getPageLinks(String page,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("100") String limit,
                                @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/links").build(page);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Link.class);
     }
 
     /**
      * The photos this page has uploaded 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPagePhotos}
      * 
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of photos from this page
      */
     @Processor
 	@OAuthProtected
     public List<Photo> getPagePhotos(
     							String page,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/photos").build(page);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList(resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Photo.class);
     }
 
     /**
      * The groups this page is a member of 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageGroups}
      * 
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return The list of groups
      */
     @Processor
 	@OAuthProtected
     public List<Group> getPageGroups(String page,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/groups").build(page);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Group.class);
     }
 
     /**
      * The photo albums this page has created 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageAlbums}
      * 
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return The list of albums
      */
     @Processor
 	@OAuthProtected
     public List<Album> getPageAlbums(
     							String page,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/albums").build(page);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList(resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Album.class);
     }
 
     /**
      * The page's status updates 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageStatuses}
      * 
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return The list of status messages
      */
     @Processor
 	@OAuthProtected
     public List<StatusMessage> getPageStatuses(String page,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("100") String limit,
                                   @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/statuses").build(page);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), StatusMessage.class);
     }
 
     /**
      * The videos this page has created 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageVideos}
      * 
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return The list of videos
      */
     @Processor
 	@OAuthProtected
     public List<Video> getPageVideos(String page,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/videos").build(page);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Video.class);
     }
 
     /**
      * The page's notes 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageNotes}
      * 
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
 	@OAuthProtected
     public List<Note> getPageNotes(String page,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("100") String limit,
                                @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/notes").build(page);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Note.class);
     }
 
     /**
      * The page's own posts 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPagePosts}
      * 
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of posts
      */
     @Processor
 	@OAuthProtected
     public List<Post> getPagePosts(String page,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("100") String limit,
                                @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/posts").build(page);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Post.class);
     }
 
     /**
      * The events this page is attending 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageEvents}
      * 
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return The list of events
      */
     @Processor
 	@OAuthProtected
     public List<Event> getPageEvents(String page,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/events").build(page);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Event.class);
     }
 
     /**
      * Checkins made by the friends of the current session user 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPageCheckins}
      * 
      * 
      * @param page Represents the ID of the page object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
 	@OAuthProtected
     public List<Checkin> getPageCheckins(String page,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("100") String limit,
                                   @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{page}/checkins").build(page);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Checkin.class);
     }
 
     /**
      * An individual photo 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPhoto}
      * 
      * 
      * @param photo Represents the ID of the photo object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return The photo represented by the given id
      */
     @Processor
 	@OAuthProtected
     public org.mule.module.facebook.types.Photo getPhoto(String photo, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{photo}").build(photo);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaObject(resource.queryParam("metadata", metadata).get(String.class), org.mule.module.facebook.types.Photo.class);
     }
 
     /**
      * All of the comments on this photo 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPhotoComments}
      * 
      * 
      * @param photo Represents the ID of the photo object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return The list of comments of the given photo
      */
     @Processor
 	@OAuthProtected
     public List<Comment> getPhotoComments(
     							   String photo,
                                    @Optional @Default("last week") String since,
                                    @Optional @Default("yesterday") String until,
                                    @Optional @Default("100") String limit,
                                    @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{photo}/comments").build(photo);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList(resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Comment.class);
     }
 
     /**
      * People who like the photo 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPhotoLikes}
      * 
      * 
      * @param photo Represents the ID of the photo object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return The likes from the given photo
      */
     @Processor
 	@OAuthProtected
     public Likes getPhotoLikes(String photo,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{photo}/likes").build(photo);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaObject( resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Likes.class);
     }
 
     /**
      * An individual entry in a profile's feed 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPost}
      * 
      * 
      * @param post Represents the ID of the post object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return The post represented by the given id
      */
     @Processor
 	@OAuthProtected
     public Post getPost(String post, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{post}").build(post);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaObject(resource.queryParam("metadata", metadata).get(String.class), Post.class);
     }
 
     /**
      * All of the comments on this post 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getPostComments}
      * 
      * 
      * @param post Represents the ID of the post object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of comments from this post
      */
     @Processor
 	@OAuthProtected
     public List<Comment> getPostComments(String post,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("100") String limit,
                                   @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{post}/comments").build(post);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList(resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Comment.class);
     }
 
     /**
      * A status message on a user's wall 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getStatus}
      * 
      * 
      * @param status Represents the ID of the status object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return The status represented by the given id
      */
     @Processor
 	@OAuthProtected
     public StatusMessage getStatus(String status, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{status}").build(status);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaObject( resource.queryParam("metadata", metadata).get(String.class), StatusMessage.class);
     }
 
     /**
      * All of the comments on this message 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getStatusComments}
      * 
      * 
      * @param status Represents the ID of the status object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return The list of comments
      */
     @Processor
 	@OAuthProtected
     public List<Comment> getStatusComments(String status,
                                     @Optional @Default("last week") String since,
                                     @Optional @Default("yesterday") String until,
                                     @Optional @Default("100") String limit,
                                     @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{status}/comments").build(status);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Comment.class);
     }
 
     /**
      * A user profile. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUser}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return The user represented by the given id
      */
     @Processor
 	@OAuthProtected
     public User getUser(String user, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}").build(user);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaObject( resource.queryParam("metadata", metadata).get(String.class), User.class);
     }
 
     /**
      * Search an individual user's News Feed, restricted to that user's friends
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserSearch}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param q The text for which to search.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of posts
      */
     @Processor
 	@OAuthProtected
     public List<Post> getUserSearch(String user,
                                     @Optional @Default("facebook") String q,
                                     @Optional @Default("0") String metadata,
                                     @Optional @Default("last week") String since,
                                     @Optional @Default("yesterday") String until,
                                     @Optional @Default("100") String limit,
                                     @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/home").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("q", q)
             .queryParam("metadata", metadata)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Post.class);
     }
 
     /**
      * The user's News Feed. Requires the read_stream permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserHome}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of posts
      */
     @Processor
 	@OAuthProtected
     public List<Post> getUserHome(String user,
                               @Optional @Default("last week") String since,
                               @Optional @Default("yesterday") String until,
                               @Optional @Default("100") String limit,
                               @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/home").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Post.class);
     }
 
     /**
      * The user's wall. Requires the read_stream permission to see non-public posts.
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserWall}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of posts
      */
     @Processor
 	@OAuthProtected
     public List<Post> getUserWall(String user,
                               @Optional @Default("last week") String since,
                               @Optional @Default("yesterday") String until,
                               @Optional @Default("100") String limit,
                               @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/feed").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Post.class);
     }
 
     /**
      * The photos, videos, and posts in which this user has been tagged. Requires the
      * user_photos, user_video_tags, friends_photos, or friend_video_tags
      * permissions 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserTagged}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return response from Facebook
      */
     @Processor
 	@OAuthProtected
     public List<Post> getUserTagged(String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/tagged").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Post.class);
     }
 
     /**
      * The user's own posts. Requires the read_stream permission to see non-public
      * posts. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserPosts}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of posts
      */
     @Processor
 	@OAuthProtected
     public List<Post> getUserPosts(String user,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("100") String limit,
                                @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/posts").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Post.class);
     }
 
     /**
      * The user's profile picture 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserPicture}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param type One of square (50x50), small (50 pixels wide, variable height),
      *            and large (about 200 pixels wide, variable height)
      * @return byte[] with the jpg image
      */
     @Processor
 	@OAuthProtected
     public byte[] getUserPicture(String user, @Optional @Default("small") String type)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/picture").build(user);
         WebResource resource = this.newWebResource(uri, accessToken);
         BufferedImage image = resource.queryParam("type", type).get(BufferedImage.class);
         return bufferedImageToByteArray(image);
     }
 
     /**
      * The user's friends 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserFriends}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of objects with the name and id of the given user's friends
      */
     @Processor
 	@OAuthProtected
     public List<NamedFacebookType> getUserFriends(String user,
                                  @Optional @Default("last week") String since,
                                  @Optional @Default("yesterday") String until,
                                  @Optional @Default("100") String limit,
                                  @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/friends").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), NamedFacebookType.class);
     }
 
     /**
      * The activities listed on the user's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserActivities}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of objects containing activity id, name, category and create_time fields. 
      */
     @Processor
 	@OAuthProtected
     public List<PageConnection> getUserActivities(String user,
                                     @Optional @Default("last week") String since,
                                     @Optional @Default("yesterday") String until,
                                     @Optional @Default("100") String limit,
                                     @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/activities").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), PageConnection.class);
     }
 
     /**
      * The music listed on the user's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserCheckins}
      *
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list with the user checkins
      */
     @Processor
 	@OAuthProtected
     public List<Checkin> getUserCheckins(String user,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("100") String limit,
                                   @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/checkins").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Checkin.class);
     }
 
     /**
      * The interests listed on the user's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserInterests}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list with the user interests
      */
     @Processor
 	@OAuthProtected
     public List<PageConnection> getUserInterests(String user,
                                    @Optional @Default("last week") String since,
                                    @Optional @Default("yesterday") String until,
                                    @Optional @Default("100") String limit,
                                    @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/interests").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), PageConnection.class);
     }
 
     /**
      * The music listed on the user's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserMusic}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list with the given user's music
      */
     @Processor
 	@OAuthProtected
     public List<PageConnection> getUserMusic(String user,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("100") String limit,
                                @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/music").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), PageConnection.class);
     }
 
     /**
      * The books listed on the user's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserBooks}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the given user's books
      */
     @Processor
 	@OAuthProtected
     public List<PageConnection> getUserBooks(String user,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("100") String limit,
                                @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/books").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), PageConnection.class);
     }
 
     /**
      * The movies listed on the user's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserMovies}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the given user's movies
      */
     @Processor
 	@OAuthProtected
     public List<PageConnection> getUserMovies(String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/movies").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), PageConnection.class);
     }
 
     /**
      * The television listed on the user's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserTelevision}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the television listed on the given user's profile
      */
     @Processor
 	@OAuthProtected
     public List<PageConnection> getUserTelevision(String user,
                                     @Optional @Default("last week") String since,
                                     @Optional @Default("yesterday") String until,
                                     @Optional @Default("100") String limit,
                                     @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/television").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), PageConnection.class);
     }
 
     /**
      * All the pages this user has liked. Requires the user_likes or friend_likes
      * permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserLikes}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing all the pages this user has liked
      */
     @Processor
 	@OAuthProtected
     public List<PageConnection> getUserLikes(String user,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("100") String limit,
                                @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/likes").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), PageConnection.class);
     }
 
     /**
      * The photos this user is tagged in. Requires the user_photos or friend_photos
      * permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserPhotos}
      * 
      *  
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of photos the given user is tagged in
      */
     @Processor
 	@OAuthProtected
     public List<Photo> getUserPhotos(String user,
     							@Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/photos").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
         	.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Photo.class);
     }
 
     /**
      * The photo albums this user has created. Requires the user_photos or
      * friend_photos permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserAlbums}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the photo albums the given user has created
      */
     @Processor
 	@OAuthProtected
     public List<Album> getUserAlbums(
     							String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/albums").build(user);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList(resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Album.class);
     }
 
     /**
      * The videos this user has been tagged in. Requires the user_videos or
      * friend_videos permission. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserVideos}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the videos the given user has been tagged in
      */
     @Processor
 	@OAuthProtected
     public List<Video> getUserVideos(String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/videos").build(user);
         
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Video.class);
     }
 
     /**
      * The groups this user is a member of. Requires the user_groups or friend_groups
      * permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserGroups}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the Groups that the given user belongs to
      */
     @Processor
 	@OAuthProtected
     public List<Group> getUserGroups(String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/groups").build(user);
 
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Group.class);
     }
 
     /**
      * The user's status updates. Requires the read_stream permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserStatuses}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list contining the user's status updates
      */
     @Processor
 	@OAuthProtected
     public List<StatusMessage> getUserStatuses(String user,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("100") String limit,
                                   @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/statuses").build(user);
 
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), StatusMessage.class);
     }
 
     /**
      * The user's posted links. Requires the read_stream permission 
      * {@sample.xml../../../doc/mule-module-facebook.xml.sample facebook:getUserLinks}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the given user's posted links 
      */
     @Processor
 	@OAuthProtected
     public List<Link> getUserLinks(String user,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("100") String limit,
                                @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/links").build(user);
 
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Link.class);
     }
 
     /**
      * The user's notes. Requires the read_stream permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserNotes}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the given user's notes
      */
     @Processor
 	@OAuthProtected
     public List<Note> getUserNotes(String user,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("100") String limit,
                                @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/notes").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Note.class);
     }
 
     /**
      * The events this user is attending. Requires the user_events or friend_events
      * permission 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserEvents}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the events the given user is attending
      */
     @Processor
 	@OAuthProtected
     public List<Event> getUserEvents(String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/events").build(user);
 
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Event.class);
     }
 
     /**
      * The threads in this user's inbox. Requires the read_mailbox permission
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserInbox}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the threads in the given user's inbox
      */
     @Processor
 	@OAuthProtected
     public List<org.mule.module.facebook.types.Thread> getUserInbox(
                                String user,
                                @Optional @Default("last week") String since,
                                @Optional @Default("yesterday") String until,
                                @Optional @Default("100") String limit,
                                @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/inbox").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Thread.class);
     }
 
     /**
      * The messages in this user's outbox. Requires the read_mailbox permission
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserOutbox}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of threads
      */
     @Processor
 	@OAuthProtected
     public List<OutboxThread> getUserOutbox(
                                 String user,
                                 @Optional @Default("last week") String since,
                                 @Optional @Default("yesterday") String until,
                                 @Optional @Default("100") String limit,
                                 @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/outbox").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), OutboxThread.class);
     }
 
     /**
      * The updates in this user's inbox. Requires the read_mailbox permission
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getUserUpdates}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the given user updates
      */
     @Processor
 	@OAuthProtected
     public List<OutboxThread> getUserUpdates(String user,
                                  @Optional @Default("last week") String since,
                                  @Optional @Default("yesterday") String until,
                                  @Optional @Default("100") String limit,
                                  @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/updates").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), OutboxThread.class);
     }
 
     /**
      * The Facebook pages owned by the current user 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getUserAccounts}
      * 
      * 
      * @param user Represents the ID of the user object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of objects containing account name, access_token, category, id
      */
     @Processor
 	@OAuthProtected
     public List<GetUserAccountResponseType> getUserAccounts(String user,
                                   @Optional @Default("last week") String since,
                                   @Optional @Default("yesterday") String until,
                                   @Optional @Default("100") String limit,
                                   @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{user}/accounts").build(user);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), GetUserAccountResponseType.class);
     }
 
     /**
      * An individual video 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getVideo}
      * 
      * 
      * @param video Represents the ID of the video object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return response from Facebook
      */
     @Processor
 	@OAuthProtected
     public Video getVideo(String video, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{video}").build(video);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaObject(resource
             .queryParam("metadata", metadata)
             .get(String.class), Video.class);
     }
 
     /**
      * All of the comments on this video 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getVideoComments}
      * 
      * 
      * @param video Represents the ID of the video object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the given video's comments
      */
     @Processor
 	@OAuthProtected
     public List<Comment> getVideoComments(
     							   String video,
                                    @Optional @Default("last week") String since,
                                    @Optional @Default("yesterday") String until,
                                    @Optional @Default("100") String limit,
                                    @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{video}/comments").build(video);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList(resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Comment.class);
     }
 
     /**
      * Write to the given profile's feed/wall. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:publishMessage}
      * 
      * 
      * @param profile_id the profile where to publish the message
      * @param msg The message
      * @param picture If available, a link to the picture included with this post
      * @param link The link attached to this post
      * @param caption The caption of the link (appears beneath the link name)
      * @param linkName The name of the link
      * @param description A description of the link (appears beneath the link
      *            caption)
      * @param place The page ID of the place that this message is associated with
      * @return The id of the published object
      */
     @Processor
 	@OAuthProtected
     public String publishMessage(
                                  String profile_id,
                                  String msg,
                                  @Optional String picture,
                                  @Optional String link,
                                  @Optional String caption,
                                  @Optional String linkName,
                                  @Optional String description,
                                  @Optional String place)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{profile_id}/feed").build(profile_id);
         WebResource resource = this.newWebResource(uri, accessToken);
         Form form = new Form();
         form.add("access_token", accessToken);
         form.add("message", msg);
 
         if (picture != null) form.add("picture", picture);
         if (link != null) form.add("link", link);
         if (caption != null) form.add("caption", caption);
         if (linkName != null) form.add("name", linkName);
         if (description != null) form.add("description", description);
         if (place != null) form.add("place", place);
 
         String json = resource.type(MediaType.APPLICATION_FORM_URLENCODED).post(String.class, form);
 		JsonObject obj = mapper.toJavaObject(json, JsonObject.class);
 		return obj.getString("id");
     }
 
     /**
      * Comment on the given post 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:publishComment}
      * 
      * 
      * @param postId Represents the ID of the post object.
      * @param msg comment on the given post
      * @return The id of the published comment
      */
     @Processor
 	@OAuthProtected
     public String publishComment(String postId, String msg)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{postId}/comments").build(postId);
         WebResource resource = this.newWebResource(uri, accessToken);
         Form form = new Form();
         form.add("access_token", accessToken);
         form.add("message", msg);
 
         WebResource.Builder type = resource.type(MediaType.APPLICATION_FORM_URLENCODED);
         String json = type.accept(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE)
                         .post(String.class, form);
 		JsonObject obj = mapper.toJavaObject(json, JsonObject.class);
 		return obj.getString("id");
     }
 
     /**
      * Write to the given profile's feed/wall. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:like}
      * 
      * 
      * @param postId Represents the ID of the post object.
      * @return Returns true if successfully liked
      */
     @Processor
 	@OAuthProtected
     public Boolean like(String postId)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{postId}/likes").build(postId);
         WebResource resource = this.newWebResource(uri, accessToken);
         String response = resource.type(MediaType.APPLICATION_FORM_URLENCODED).post(String.class);
         
         return Boolean.valueOf(response);
     }
 
     /**
      * Write a note on the given profile. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:publishNote}
      * 
      * 
      * @param profile_id the profile where to publish the note
      * @param msg The message
      * @param subject the subject of the note
      * @return note id
      */
     @Processor
 	@OAuthProtected
     public String publishNote(String profile_id, String msg,
                             String subject)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{profile_id}/notes").build(profile_id);
         WebResource resource = this.newWebResource(uri, accessToken);
         Form form = new Form();
         form.add("message", msg);
         form.add("subject", subject);
         WebResource.Builder type = resource
 				.type(MediaType.APPLICATION_FORM_URLENCODED);
         String json = type.accept(MediaType.APPLICATION_JSON_TYPE,
 				MediaType.APPLICATION_XML_TYPE).post(String.class, form);
         
 		JsonObject obj = mapper.toJavaObject(json, JsonObject.class);
         
 		return obj.getString("id");
     }
 
     /**
      * Write a note on the given profile. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:publishLink}
      * 
      * 
      * @param profile_id the profile where to publish the link
      * @param msg The message
      * @param link the link
      * @return link id
      */
     @Processor
 	@OAuthProtected
     public String publishLink(String profile_id, String msg, String link)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{profile_id}/links").build(profile_id);
         WebResource resource = this.newWebResource(uri, accessToken);
         Form form = new Form();
         form.add("message", msg);
         form.add("link", link);
 
         WebResource.Builder type = resource
 				.type(MediaType.APPLICATION_FORM_URLENCODED);
         String json = type.accept(MediaType.APPLICATION_JSON_TYPE,
 				MediaType.APPLICATION_XML_TYPE).post(String.class, form);
         
 		JsonObject obj = mapper.toJavaObject(json, JsonObject.class);
 		return obj.getString("id");
     }
 
     /**
      * Post an event in the given profile. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:publishEvent}
      * 
      * 
      * @param profile_id the profile where to publish the event
      * @param event_name the name of the event
      * @param start_time the event start time, in ISO-8601 
      * @param end_time the event end time, in ISO-8601
      * @param description the event description
      * @param location the event location
      * @param location_id Facebook Place ID of the place the Event is taking place 
      * @param privacy_type string containing 'OPEN' (default), 'SECRET', or 'FRIENDS' 
      * @return The id of the published event
      */
     @Processor
 	@OAuthProtected
     public String publishEvent(String profile_id, String event_name, String start_time, @Optional String end_time, @Optional String description, @Optional String location,
     		@Optional String location_id, @Optional String privacy_type)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{profile_id}/events").build(profile_id);
         WebResource resource = this.newWebResource(uri, accessToken);
         
                
         resource = resource.queryParam("access_token", accessToken)
         		.queryParam("name", event_name)
         		.queryParam("start_time", start_time);
         
         if(end_time != null) {
         	resource = resource.queryParam("end_time", end_time);
         }
         if(description != null) {
         	resource = resource.queryParam("description", description);
         }
         if(location != null) {
         	resource = resource.queryParam("location", location);
         }
         if(location_id != null) {
         	resource = resource.queryParam("location_id", location_id);
         }
         if(privacy_type != null) {
         	resource = resource.queryParam("privacy_type", privacy_type);
         }
         
         String json = resource.post(String.class);
 		JsonObject obj = mapper.toJavaObject(json, JsonObject.class);
 		return obj.getString("id");
     }
 
     /**
      * Attend the given event. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:attendEvent}
      * 
      * 
      * @param eventId the id of the event to attend
      * @return Boolean result indicating success or failure of operation
      */
     @Processor
 	@OAuthProtected
     public Boolean attendEvent(String eventId)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{eventId}/attending").build(eventId);
         WebResource resource = this.newWebResource(uri, accessToken);
         String res = resource.type(MediaType.APPLICATION_FORM_URLENCODED).post(String.class);
         
         return Boolean.parseBoolean(res);
     }
 
     /**
      * Maybe attend the given event. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:tentativeEvent}
      * 
      * 
      * @param eventId Represents the id of the event object
      * @return The result of the API request
      */
     @Processor
 	@OAuthProtected
     public boolean tentativeEvent(String eventId)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{eventId}/maybe").build(eventId);
         WebResource resource = this.newWebResource(uri, accessToken);
         String res = resource.type(MediaType.APPLICATION_FORM_URLENCODED).post(String.class);
         
         return Boolean.parseBoolean(res);
     }
 
     /**
      * Decline the given event. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:declineEvent}
      * 
      * 
      * @param eventId Represents the id of the event object
      * @return Boolean result indicating success or failure of operation
      */
     @Processor
 	@OAuthProtected
     public Boolean declineEvent(String eventId)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{eventId}/declined").build(eventId);
         WebResource resource = this.newWebResource(uri, accessToken);
         Form form = new Form();
         form.add("eventId", eventId);
         String res = resource.type(MediaType.APPLICATION_FORM_URLENCODED).post(String.class, form);
         
         return Boolean.parseBoolean(res);
     }
     
     /**
      * Invites a user to a given event.
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:inviteUser}
      * 
      * @param eventId The ID of the event.
      * @param userId The ID of the user to invite.
      * @return Boolean result indicating success or failure of operation
      */
     public Boolean inviteUser(String eventId, String userId)
     {
     	URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{eventId}/invited/{userId}").build(eventId, userId);
         WebResource resource = this.newWebResource(uri, accessToken);
         Form form = new Form();
         form.add("eventId", eventId);
         form.add("userId", userId);
         String res = resource.type(MediaType.APPLICATION_FORM_URLENCODED).post(String.class, form);
         
         return Boolean.parseBoolean(res);
     }
 
     /**
      * Create an album. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:publishAlbum}
      * 
      * 
      * @param profile_id the id of the profile object
      * @param msg The message
      * @param albumName the name of the album
      * @return The ID of the album that was just created
      */
     @Processor
 	@OAuthProtected
     public String publishAlbum(String profile_id, String msg, String albumName)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{profile_id}/albums").build(profile_id);
         WebResource resource = this.newWebResource(uri, accessToken);
         Form form = new Form();
         form.add("message", msg);
         form.add("name", albumName);
 
         String json = resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE).post(String.class, form);
         
         JsonObject jsonObject = mapper.toJavaObject(json, JsonObject.class);
         String albumId = (String) jsonObject.get("id");
         
         return albumId;
     }
 
     /**
      * Upload a photo to an album. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:publishPhoto}
      * 
      * 
      * @param albumId the id of the album object
      * @param caption Caption of the photo
      * @param photo File containing the photo
      * @return The ID of the photo that was just published
      */
     @Processor
 	@OAuthProtected
     public String publishPhoto(String albumId, String caption, File photo)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{albumId}/photos").build(albumId);
         WebResource resource = this.newWebResource(uri, accessToken);
         FormDataMultiPart multiPart = new FormDataMultiPart();
         multiPart.bodyPart(new FileDataBodyPart("source", photo, MediaType.APPLICATION_OCTET_STREAM_TYPE));
         multiPart.field("message", caption);
         
         String jsonId = resource.type(MediaType.MULTIPART_FORM_DATA).post(String.class, multiPart);
         
         JsonObject obj = mapper.toJavaObject(jsonId, JsonObject.class);
         String photoId = (String) obj.get("id");
         return photoId;
     }
 
     /**
      * Delete an object in the graph. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:deleteObject}
      * 
      * 
      * @param objectId The ID of the object to be deleted
      * @return The result of the deletion
      */
     @Processor
 	@OAuthProtected
     public Boolean deleteObject(String objectId)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{object_id}").build(objectId);
         WebResource resource = this.newWebResource(uri, accessToken);
         String result = resource.type(MediaType.APPLICATION_FORM_URLENCODED).delete(String.class);
         return Boolean.valueOf(result);
     }
 
     /**
      * Remove a 'like' from a post. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:dislike}
      * 
      * 
      * @param postId The ID of the post to be disliked
      * @return Returns true if API call was successfull
      */
     @Processor
 	@OAuthProtected
     public Boolean dislike(String postId)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{postId}/likes").build(postId);
         WebResource resource = client.resource(uri).queryParam(ACCESS_TOKEN_QUERY_PARAM_NAME, accessToken);
         String response = resource.type(MediaType.APPLICATION_FORM_URLENCODED).delete(String.class);
         return Boolean.valueOf(response);
     }
 
     /**
      * A check-in that was made through Facebook Places. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getCheckin}
      * 
      * 
      * @param checkin Represents the ID of the checkin object.
      * @param metadata The Graph API supports introspection of objects, which enables
      *            you to see all of the connections an object has without knowing its
      *            type ahead of time.
      * @return The checkin represented by the given id
      */
     @Processor
 	@OAuthProtected
     public Checkin getCheckin(String checkin, @Optional @Default("0") String metadata)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{checkin}").build(checkin);
         WebResource resource = client.resource(uri).queryParam(ACCESS_TOKEN_QUERY_PARAM_NAME, accessToken);
         return mapper.toJavaObject(resource.queryParam("metadata", metadata).get(String.class), Checkin.class);
     }
 
     /**
      * An application's profile 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplication}
      * 
      * 
      * @param application Represents the ID of the application object.
      * @return The application represented by the given id
      */
     @Processor
 	@OAuthProtected
     public Application getApplication(String application)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}").build(application);
         WebResource resource = client.resource(uri).queryParam(ACCESS_TOKEN_QUERY_PARAM_NAME, accessToken);
         return mapper.toJavaObject( resource.get(String.class), Application.class);
     }
 
     /**
      * The application's wall. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationWall}
      * 
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the given application posts
      */
     @Processor
 	@OAuthProtected
     public List<Post> getApplicationWall(String application,
                                      @Optional @Default("last week") String since,
                                      @Optional @Default("yesterday") String until,
                                      @Optional @Default("100") String limit,
                                      @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/feed").build(application);
         WebResource resource = client.resource(uri).queryParam(ACCESS_TOKEN_QUERY_PARAM_NAME, accessToken);
         return mapper.toJavaList(resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Post.class);
     }
 
     /**
      * The application's logo 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationPicture}
      * 
      * 
      * @param application Represents the ID of the application object.
      * @param type One of square (50x50), small (50 pixels wide, variable height),
      *            and large (about 200 pixels wide, variable height)
      * @return The given application picture
      */
     @Processor
 	@OAuthProtected
     public byte[] getApplicationPicture(String application, @Optional @Default("small") String type)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/picture").build(application);
         WebResource resource = client.resource(uri).queryParam(ACCESS_TOKEN_QUERY_PARAM_NAME, accessToken);
         BufferedImage image = resource.queryParam("type", type).get(BufferedImage.class);
         return bufferedImageToByteArray(image);
     }
 
     /**
      * The photos, videos, and posts in which this application has been tagged.
      * 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample
      * facebook:getApplicationTagged}
      * 
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return The posts where this application has been tagged
      */
     @Processor
 	@OAuthProtected
     public List<GetApplicationTaggedResponseType> getApplicationTagged(String application,
                                        @Optional @Default("last week") String since,
                                        @Optional @Default("yesterday") String until,
                                        @Optional @Default("100") String limit,
                                        @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/tagged").build(application);
         WebResource resource = client.resource(uri).queryParam(ACCESS_TOKEN_QUERY_PARAM_NAME, accessToken);
         return mapper.toJavaList(resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), GetApplicationTaggedResponseType.class);
     }
 
     /**
      * The application's posted links. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationLinks}
      * 
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containig the links of the given application
      */
     @Processor
 	@OAuthProtected
     public List<Post> getApplicationLinks(String application,
                                       @Optional @Default("last week") String since,
                                       @Optional @Default("yesterday") String until,
                                       @Optional @Default("100") String limit,
                                       @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/links").build(application);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList(resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Post.class);
     }
 
     /**
      * The photos this application is tagged in. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationPhotos}
      *
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by shorttime
      * @param until A unix timestamp or any date accepted by shorttime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list with photos
      */
     @Processor
 	@OAuthProtected
     public List<Photo> getApplicationPhotos(String application,
                                        @Optional @Default("last week") String since,
                                        @Optional @Default("yesterday") String until,
                                        @Optional @Default("100") String limit,
                                        @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/photos").build(application);
         WebResource resource = client.resource(uri).queryParam(ACCESS_TOKEN_QUERY_PARAM_NAME, accessToken);
         return mapper.toJavaList(resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Photo.class);
     }
 
     /**
      * The photo albums this application has created. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationAlbums}
      * 
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the given application's albums
      */
     @Processor
 	@OAuthProtected
     public List<Album> getApplicationAlbums(String application,
                                        @Optional @Default("last week") String since,
                                        @Optional @Default("yesterday") String until,
                                        @Optional @Default("100") String limit,
                                        @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/albums").build(application);
         WebResource resource = client.resource(uri).queryParam(ACCESS_TOKEN_QUERY_PARAM_NAME, accessToken);
         return mapper.toJavaList(resource.queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Album.class);
     }
 
     /**
      * The application's status updates. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationStatuses}
      * 
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the status messages for the given application
      */
     @Processor
 	@OAuthProtected
     public List<StatusMessage> getApplicationStatuses(String application,
                                          @Optional @Default("last week") String since,
                                          @Optional @Default("yesterday") String until,
                                          @Optional @Default("100") String limit,
                                          @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/statuses").build(application);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), StatusMessage.class);
     }
 
     /**
      * The videos this application has created 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationVideos}
      *
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list of videos for the given application
      */
     @Processor
 	@OAuthProtected
     public List<Video> getApplicationVideos(String application,
                                        @Optional @Default("last week") String since,
                                        @Optional @Default("yesterday") String until,
                                        @Optional @Default("100") String limit,
                                        @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/videos").build(application);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Video.class);
     }
 
     /**
      * The application's notes. 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationNotes}
      *
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the notes for the given application
      */
     @Processor
 	@OAuthProtected
     public List<Note> getApplicationNotes(String application,
                                       @Optional @Default("last week") String since,
                                       @Optional @Default("yesterday") String until,
                                       @Optional @Default("100") String limit,
                                       @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/notes").build(application);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Note.class);
     }
 
     /**
      * The events this page is managing 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationEvents}
      *
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the events for the given application
      */
     @Processor
 	@OAuthProtected
     public List<Event> getApplicationEvents(String application,
                                        @Optional @Default("last week") String since,
                                        @Optional @Default("yesterday") String until,
                                        @Optional @Default("100") String limit,
                                        @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/events").build(application);
         return mapper.toJavaList(this.newWebResource(uri, accessToken)
             .queryParam("since", since)
             .queryParam("until", until)
             .queryParam("limit", limit)
             .queryParam("offset", offset)
             .get(String.class), Event.class);
     }
 
     /**
      * Usage metrics for this application 
      * 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:getApplicationInsights}
      *
      * 
      * @param application Represents the ID of the application object.
      * @param since A unix timestamp or any date accepted by strtotime
      * @param until A unix timestamp or any date accepted by strtotime
      * @param limit Limit the number of items returned.
      * @param offset An offset to the response. Useful for paging.
      * @return A list containing the insights for the given application
      */
     @Processor
 	@OAuthProtected
     public List<Insight> getApplicationInsights(String application,
                                          @Optional @Default("last week") String since,
                                          @Optional @Default("yesterday") String until,
                                          @Optional @Default("100") String limit,
                                          @Optional @Default("0") String offset)
     {
         URI uri = UriBuilder.fromPath(FACEBOOK_URI).path("{application}/insights").build(application);
         WebResource resource = this.newWebResource(uri, accessToken);
         return mapper.toJavaList(resource.queryParam("since", since)
 							            .queryParam("until", until)
 							            .queryParam("limit", limit)
 							            .queryParam("offset", offset)
 							            .get(String.class), Insight.class);
     }
     
     /**
      * This is a convinience processor that simply fetchs an uri which is expected to return an image
      * and returns it as a Byte array. Notice that I'm using the word image instead of photo or picture which are
      * words with a particular meaning in facebook. By image, I refer to a generic bitmap.
      * 
      * {@sample.xml ../../../doc/mule-module-facebook.xml.sample facebook:download-image}
      * 
      * 
      * @param imageUri the uri of an image resource
      * @return a byte array with the image
      */
     @Processor
 	@OAuthProtected
     public byte[] downloadImage(String imageUri) {
     	URI uri = URI.create(imageUri);
     	return this.bufferedImageToByteArray(this.newWebResource(uri, accessToken).get(BufferedImage.class));
     }
     
     private byte[] bufferedImageToByteArray(BufferedImage image)
     {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         try
         {
             ImageIO.write(image, "jpg", baos);
             baos.close();
         }
         catch (IOException e)
         {
             throw MuleSoftException.soften(e);
         }
         catch (IllegalArgumentException iae)
         {
             throw MuleSoftException.soften(iae);
         }
         return baos.toByteArray();
     }
     
     private WebResource newWebResource(URI uri, String accessToken) {
     	return this.client.resource(uri).queryParam(ACCESS_TOKEN_QUERY_PARAM_NAME, accessToken);
     }
 
     public String getConsumerKey()
     {
         return consumerKey;
     }
 
     public void setConsumerKey(String consumerKey)
     {
         this.consumerKey = consumerKey;
     }
 
     public String getConsumerSecret()
     {
         return consumerSecret;
     }
 
     public void setConsumerSecret(String consumerSecret)
     {
         this.consumerSecret = consumerSecret;
     }
 
     public String getScope()
     {
         return scope;
     }
 
     public void setScope(String scope)
     {
         this.scope = scope;
     }
     
     public Client getClient()
     {
         return client;
     }
     
     public void setClient(Client client)
     {
         this.client = client;
     }
 
 	public String getAccessToken() {
 		return accessToken;
 	}
 
 	public void setAccessToken(String accessToken) {
 		this.accessToken = accessToken;
 	}
     
 }
