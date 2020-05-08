 /*
  * Copyright (c) 2005 Aetrion LLC.
  */
 package com.aetrion.flickr.people;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.xml.sax.SAXException;
 
 import com.aetrion.flickr.FlickrException;
 import com.aetrion.flickr.Parameter;
 import com.aetrion.flickr.Response;
 import com.aetrion.flickr.Transport;
 import com.aetrion.flickr.contacts.OnlineStatus;
 import com.aetrion.flickr.groups.Group;
 import com.aetrion.flickr.photos.Photo;
 import com.aetrion.flickr.util.XMLUtilities;
 import java.net.URLEncoder;
 /**
  * Interface for finding Flickr users.
  *
  * @author Anthony Eden
  */
 public class PeopleInterface {
 
     public static final String METHOD_FIND_BY_EMAIL = "flickr.people.findByEmail";
     public static final String METHOD_FIND_BY_USERNAME = "flickr.people.findByUsername";
     public static final String METHOD_GET_INFO = "flickr.people.getInfo";
     public static final String METHOD_GET_ONLINE_LIST = "flickr.people.getOnlineList";
     public static final String METHOD_GET_PUBLIC_GROUPS = "flickr.people.getPublicGroups";
     public static final String METHOD_GET_PUBLIC_PHOTOS = "flickr.people.getPublicPhotos";
     public static final String METHOD_GET_UPLOAD_STATUS = "flickr.people.getUploadStatus";
 
     private String apiKey;
     private Transport transportAPI;
 
     public PeopleInterface(String apiKey, Transport transportAPI) {
         this.apiKey = apiKey;
         this.transportAPI = transportAPI;
     }
 
     /**
      * Find the user by their email address.
      *
      * @param email The email address
      * @return The User
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public User findByEmail(String email) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_FIND_BY_EMAIL));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("find_email", email));
 
         Response response = transportAPI.get(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element userElement = response.getPayload();
         User user = new User();
         user.setId(userElement.getAttribute("nsid"));
         user.setUsername(XMLUtilities.getChildValue(userElement, "username"));
         return user;
     }
 
     /**
      * Find a User by the username.
      *
      * @param username The username
      * @return The User object
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public User findByUsername(String username) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_FIND_BY_USERNAME));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("username", URLEncoder.encode(username, "UTF-8")));
 
         Response response = transportAPI.get(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element userElement = response.getPayload();
         User user = new User();
         user.setId(userElement.getAttribute("nsid"));
         user.setUsername(XMLUtilities.getChildValue(userElement, "username"));
         return user;
     }
 
     /**
      * Get info about the specified user.
      *
      * @param userId The user ID
      * @return The User object
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public User getInfo(String userId) throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_INFO));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("user_id", userId));
 
         Response response = transportAPI.get(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element userElement = response.getPayload();
         User user = new User();
         user.setId(userElement.getAttribute("nsid"));
         user.setAdmin("1".equals(userElement.getAttribute("isadmin")));
         user.setPro("1".equals(userElement.getAttribute("ispro")));
         user.setIconServer(userElement.getAttribute("iconserver"));
         user.setUsername(XMLUtilities.getChildValue(userElement, "username"));
         user.setRealName(XMLUtilities.getChildValue(userElement, "realname"));
         user.setLocation(XMLUtilities.getChildValue(userElement, "location"));
         user.setMbox_sha1sum(XMLUtilities.getChildValue(userElement, "mbox_sha1sum"));
         
         Element photosElement = XMLUtilities.getChild(userElement, "photos");
         user.setPhotosFirstDate(XMLUtilities.getChildValue(photosElement, "firstdate"));
         user.setPhotosFirstDateTaken(XMLUtilities.getChildValue(photosElement, "firstdatetaken"));
         user.setPhotosCount(XMLUtilities.getChildValue(photosElement, "count"));
 
         return user;
     }
 
     /**
      * Get the list of current online users.
      *
      * @return The list of online users
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      * @deprecated To be removed from the Flickr API
      */
     public Collection getOnlineList() throws IOException, SAXException, FlickrException {
         List online = new ArrayList();
 
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_ONLINE_LIST));
         parameters.add(new Parameter("api_key", apiKey));
 
         Response response = transportAPI.get(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element onlineElement = response.getPayload();
         NodeList userNodes = onlineElement.getElementsByTagName("user");
         for (int i = 0; i < userNodes.getLength(); i++) {
             Element userElement = (Element) userNodes.item(i);
             User user = new User();
             user.setId(userElement.getAttribute("nsid"));
             user.setUsername(userElement.getAttribute("username"));
             user.setOnline(OnlineStatus.fromType(userElement.getAttribute("online")));
             if (user.getOnline() == OnlineStatus.AWAY) {
                 Text awayMessageElement = (Text) userElement.getFirstChild();
                 if (awayMessageElement != null)
                     user.setAwayMessage(awayMessageElement.getData());
             }
             online.add(user);
         }
         return online;
     }
 
     /**
      * Get a collection of public groups for the user.
      *
      * @param userId The user ID
      * @return The public groups
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public Collection getPublicGroups(String userId) throws IOException, SAXException, FlickrException {
         List groups = new ArrayList();
 
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_PUBLIC_GROUPS));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("user_id", userId));
 
         Response response = transportAPI.get(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element groupsElement = response.getPayload();
         NodeList groupNodes = groupsElement.getElementsByTagName("photo");
         for (int i = 0; i < groupNodes.getLength(); i++) {
             Element groupElement = (Element) groupNodes.item(i);
             Group group = new Group();
             group.setId(groupElement.getAttribute("nsid"));
             group.setName(groupElement.getAttribute("name"));
             group.setAdmin("1".equals(groupElement.getAttribute("admin")));
             group.setEighteenPlus("1".equals(groupElement.getAttribute("eighteenplus")));
             groups.add(group);
         }
 
         return groups;
     }
 
     /**
      * Get a collection of public photos for the specified user ID.
      *
      * @param userId The User ID
      * @param perPage The number of photos per page
      * @param page The page offset
      * @return The collection of Photo objects
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public Collection getPublicPhotos(String userId, int perPage, int page) throws IOException, SAXException,
             FlickrException {
         List photos = new ArrayList();
 
         List parameters = new ArrayList();
         parameters.add(new Parameter("method", METHOD_GET_PUBLIC_PHOTOS));
         parameters.add(new Parameter("api_key", apiKey));
 
         parameters.add(new Parameter("user_id", userId));
 
         if (perPage > 0) {
             parameters.add(new Parameter("per_page", new Integer(perPage)));
         }
         if (page > 0) {
             parameters.add(new Parameter("page", new Integer(page)));
         }
 
         Response response = transportAPI.get(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element photosElement = response.getPayload();
         NodeList photoNodes = photosElement.getElementsByTagName("photo");
         for (int i = 0; i < photoNodes.getLength(); i++) {
             Element photoElement = (Element) photoNodes.item(i);
             Photo photo = new Photo();
             photo.setId(photoElement.getAttribute("id"));
 
             User owner = new User();
             owner.setId(photoElement.getAttribute("owner"));
             photo.setOwner(owner);
 
             photo.setSecret(photoElement.getAttribute("secret"));
             photo.setServer(photoElement.getAttribute("server"));
             photo.setTitle(photoElement.getAttribute("title"));
             photo.setPublicFlag("1".equals(photoElement.getAttribute("ispublic")));
             photo.setFriendFlag("1".equals(photoElement.getAttribute("isfriend")));
             photo.setFamilyFlag("1".equals(photoElement.getAttribute("isfamily")));
 
             photos.add(photo);
         }
         return photos;
     }
 
     /**
      * Get upload status for the currently authenticated user.
      * <p/>
      * Note: Requires authentication with 'read' permission using the new authentication API.
      *
      * @return A User object with upload status data fields filled
      * @throws IOException
      * @throws SAXException
      * @throws FlickrException
      */
     public User getUploadStatus() throws IOException, SAXException, FlickrException {
         List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_UPLOAD_STATUS));
         parameters.add(new Parameter("api_key", apiKey));
 
         Response response = transportAPI.get(transportAPI.getPath(), parameters);
         if (response.isError()) {
             throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
         }
         Element userElement = response.getPayload();
         User user = new User();
         user.setId(userElement.getAttribute("id"));
         user.setPro("1".equals(userElement.getAttribute("ispro")));
         user.setUsername(XMLUtilities.getChildValue(userElement, "username"));
 
         Element bandwidthElement = XMLUtilities.getChild(userElement, "bandwidth");
         user.setBandwidthMax(bandwidthElement.getAttribute("max"));
         user.setBandwidthUsed(bandwidthElement.getAttribute("used"));
 
         Element filesizeElement = XMLUtilities.getChild(userElement, "filesize");
         user.setFilesizeMax(filesizeElement.getAttribute("max"));
 
         return user;
     }
 }
