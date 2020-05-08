 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 
 package org.mule.module.facebook.automation.testcases;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.rules.Timeout;
 import org.mule.api.config.MuleProperties;
 import org.mule.api.store.ObjectStore;
 import org.mule.api.store.ObjectStoreException;
 import org.mule.module.facebook.oauth.FacebookConnectorOAuthState;
 import org.mule.module.facebook.types.Photo;
 import org.mule.modules.tests.ConnectorTestCase;
 
 import com.restfb.types.Album;
 import com.restfb.types.Comment;
 import com.restfb.types.Event;
 import com.restfb.types.Group;
 import com.restfb.types.Link;
 import com.restfb.types.Note;
 import com.restfb.types.StatusMessage;
 import com.restfb.types.User;
 
 public class FacebookTestParent extends ConnectorTestCase {
 
 	// Set global timeout of tests to 10minutes
 	@Rule
 	public Timeout globalTimeout = new Timeout(600000);
 
 	private Properties properties = new Properties();
 	
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	@Before
     public void init() throws ObjectStoreException, IOException {
     	ObjectStore objectStore = muleContext.getRegistry().lookupObject(MuleProperties.DEFAULT_USER_OBJECT_STORE_NAME);
     	objectStore.store("accessTokenId", (FacebookConnectorOAuthState) getBeanFromContext("connectorOAuthState"));
     	objectStore.store("accessTokenIdPage", (FacebookConnectorOAuthState) getBeanFromContext("connectorOAuthStatePage"));
     	
 		InputStream props = getClass().getClassLoader().getResourceAsStream("init-state.properties");
 		properties.load(props);
     }
 	
 	protected Album requestAlbum(String albumId) throws Exception {
 		upsertOnTestRunMessage("album", albumId);
 
 		return runFlowAndGetPayload("get-album");
     }
 
     protected String publishAlbum(String albumName, String msg, String profileId) throws Exception {
     	upsertOnTestRunMessage("albumName", albumName);
     	upsertOnTestRunMessage("msg", msg);
     	upsertOnTestRunMessage("profileId", profileId);
 
     	return runFlowAndGetPayload("publish-album");
     }
     
     protected String publishAlbumOnPage(String albumName, String msg, String pageId) throws Exception {
     	upsertOnTestRunMessage("albumName", albumName);
     	upsertOnTestRunMessage("msg", msg);
     	upsertOnTestRunMessage("profileId", pageId);
     	
     	return runFlowAndGetPayload("publish-album-on-page");
     }
 
 	protected String publishMessage(String profileId, String msg) throws Exception {
 		upsertOnTestRunMessage("profileId", profileId);
 		upsertOnTestRunMessage("msg", msg);
 
 		return runFlowAndGetPayload("publish-message");
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Collection<Album> requestUserAlbums(String user, String since, String until, String limit, String offset) throws Exception {
 		upsertOnTestRunMessage("user", user);
 		upsertOnTestRunMessage("since", since);
 		upsertOnTestRunMessage("until", until);
 		upsertOnTestRunMessage("limit", limit);
 		upsertOnTestRunMessage("offset", offset);
 
 		return runFlowAndGetPayload("get-user-albums");
     }
 
 	protected StatusMessage getStatus(String statusId) throws Exception {
 		upsertOnTestRunMessage("status", statusId);
 		
 		return runFlowAndGetPayload("get-status");
 	}
 
 	protected Album getAlbum(String albumId) throws Exception {
 		upsertOnTestRunMessage("album", albumId);
 
 		return runFlowAndGetPayload("get-album");
 	}
 
     protected User getLoggedUserDetails() throws Exception {
 		return runFlowAndGetPayload("logged-user-details");
     }
 
     protected String getProfileId() throws Exception {
     	return getLoggedUserDetails().getId();
     }
 
     protected Photo getPhoto(String photoId, String metadata) throws Exception {
     	upsertOnTestRunMessage("photo", photoId);
     	upsertOnTestRunMessage("metadata", metadata);
 
 		return runFlowAndGetPayload("get-photo");
     }
 
     protected String publishComment(String postId, String msg) throws Exception {
     	upsertOnTestRunMessage("postId", postId);
     	upsertOnTestRunMessage("msg", msg);
 
     	return runFlowAndGetPayload("publish-comment");
     }
 
     protected String publishLink(String profileId, String msg, String link) throws Exception {
     	upsertOnTestRunMessage("profileId", profileId);
     	upsertOnTestRunMessage("msg", msg);
     	upsertOnTestRunMessage("link", link);
 
 		return runFlowAndGetPayload("publish-link");
     }
 
     protected String publishPhoto(String albumId, String caption, File photo) throws Exception {
     	upsertOnTestRunMessage("albumId", albumId);
     	upsertOnTestRunMessage("caption", caption);
     	upsertOnTestRunMessage("photoRef", photo);
 
 		return runFlowAndGetPayload("publish-photo");
     }
 
     public boolean like(String postId) throws Exception {
     	upsertOnTestRunMessage("postId", postId);
 		return runFlowAndGetPayload("like");
     }
 
     public boolean dislike(String postId) throws Exception {
     	upsertOnTestRunMessage("postId", postId);
     	
 		return runFlowAndGetPayload("dislike");
     }
 
     public List<Comment> getStatusComments(String statusId) throws Exception {
     	return getStatusComments(statusId, "now", "yesterday", "100", "0");
     }
 
     @SuppressWarnings("unchecked")
 	public List<Comment> getStatusComments(String statusId, String until, String since, String limit, String offset) throws Exception {
     	upsertOnTestRunMessage("status", statusId);
     	upsertOnTestRunMessage("until", until);
     	upsertOnTestRunMessage("since", since);
     	upsertOnTestRunMessage("limit", limit);
     	upsertOnTestRunMessage("offset", offset);
 
 		return runFlowAndGetPayload("get-status-comments");
     }
 
     protected Boolean deleteObject(String objectId) throws Exception {
     	upsertOnTestRunMessage("objectId", objectId);
     	
 		return runFlowAndGetPayload("delete-object");
     }
     
     protected Boolean deletePageObject(String objectId) throws Exception {
     	upsertOnTestRunMessage("objectId", objectId);
 
     	return runFlowAndGetPayload("delete-object-from-page");
     }
 
     public Link getLink(String linkId) throws Exception{
     	upsertOnTestRunMessage("link", linkId);
 
     	return runFlowAndGetPayload("get-link");
 	}
 
     protected String publishEvent(String profileId, String eventName, String startTime) throws Exception {
     	upsertOnTestRunMessage("profileId", profileId);
     	upsertOnTestRunMessage("eventName", eventName);
     	upsertOnTestRunMessage("startTime", startTime);
 
 		return runFlowAndGetPayload("publish-event");
     }
 
     protected String publishEventPage(String pageId, String eventName, String startTime) throws Exception {
     	upsertOnTestRunMessage("profileId", pageId);
     	upsertOnTestRunMessage("eventName", eventName);
     	upsertOnTestRunMessage("startTime", startTime);
     	
 		return runFlowAndGetPayload("publish-event-on-page");
     }
 
     protected String publishMessage(String profileId, String msg, String link, String linkName, String description, String picture, String caption, String place) throws Exception {
     	upsertOnTestRunMessage("profileId", profileId);
     	upsertOnTestRunMessage("msg", msg);
     	upsertOnTestRunMessage("link", link);
     	upsertOnTestRunMessage("linkName", linkName);
     	upsertOnTestRunMessage("description", description);
     	upsertOnTestRunMessage("picture", picture);
     	upsertOnTestRunMessage("caption", caption);
     	upsertOnTestRunMessage("place", place);
 
 		return runFlowAndGetPayload("publish-message-all-attributes");
 	}
     
 	protected String publishNote(String profileId, String msg, String subject ) throws Exception{
 		upsertOnTestRunMessage("profileId", profileId);
 		upsertOnTestRunMessage("msg", msg);
 		upsertOnTestRunMessage("subject", subject);
 
 		return runFlowAndGetPayload("publish-note");
 	}
 
 	protected String publishNoteOnPage(String pageId, String msg, String subject ) throws Exception{
 		upsertOnTestRunMessage("profileId", pageId);
 		upsertOnTestRunMessage("msg", msg);
 		upsertOnTestRunMessage("subject", subject);
 
 		return runFlowAndGetPayload("publish-note-on-page");
 	}
 
 	protected Note getNote(String note) throws Exception {
 		upsertOnTestRunMessage("note", note);
 
 		return runFlowAndGetPayload("get-note");
 	}
 
 	protected Boolean attendEvent(String eventId) throws Exception {
 		upsertOnTestRunMessage("eventId", eventId);
 
 		return runFlowAndGetPayload("attend-event");
 	}
 
 	protected Boolean declineEvent(String eventId) throws Exception {
 		upsertOnTestRunMessage("eventId", eventId);
 
 		return runFlowAndGetPayload("decline-event");
 	}
 
 	protected Event getEvent(String eventId) throws Exception {
 		upsertOnTestRunMessage("eventId", eventId);
 
 		return runFlowAndGetPayload("get-event");
 	}
 
 
 	@SuppressWarnings("unchecked")
 	protected List<Group> searchGroups(String query) throws Exception {
 		upsertOnTestRunMessage("q", query);
 
 		return runFlowAndGetPayload("search-groups");
 
 	}
 
 	public static String today() {
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		Calendar calendar = GregorianCalendar.getInstance();
 		return format.format(calendar.getTime());
 	}
 
 	public static String tomorrow() {
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		Calendar calendar = GregorianCalendar.getInstance();
 		calendar.add(Calendar.DAY_OF_YEAR, 1);
 		return format.format(calendar.getTime());
 	}
 
 	public static String yesterday() {
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		Calendar calendar = GregorianCalendar.getInstance();
 		calendar.add(Calendar.DAY_OF_YEAR, -1);
 		return format.format(calendar.getTime());
 	}
 	
 	protected List<String> getExpectedMusic() throws IOException {
 		String musicLikes = properties.getProperty("facebook.init.music");
 		return Arrays.asList(musicLikes.split(","));
 	}
 	
 	protected List<String> getExpectedTelevision() throws IOException {
 		String televisionLikes = properties.getProperty("facebook.init.television");
 		return Arrays.asList(televisionLikes.split(","));
 	}
 	
 	protected List<String> getExpectedBooks() throws IOException {
 		String bookLikes = properties.getProperty("facebook.init.books");
 		return Arrays.asList(bookLikes.split(","));
 	}
 	
 	protected List<String> getExpectedMovies() throws IOException {
 		String movieLikes = properties.getProperty("facebook.init.movies");
 		return Arrays.asList(movieLikes.split(","));
 	}
 	
 	protected List<String> getExpectedLikes() throws IOException {
		List<String> music = getExpectedMovies();
 		List<String> television = getExpectedTelevision();
 		List<String> books = getExpectedBooks();
 		List<String> movies = getExpectedMovies();
 
 		List<String> finalList = new ArrayList<String>();
 		finalList.addAll(music);
 		finalList.addAll(television);
 		finalList.addAll(books);
 		finalList.addAll(movies);
 		return finalList;
 	}
 	
 }
