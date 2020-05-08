 package com.force.sample.service;
 
 import java.io.*;
 import java.net.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.force.sample.dao.ChatterPostDao;
 import com.force.sample.model.ChatterPost;
 import com.force.sdk.oauth.context.ForceSecurityContextHolder;
 import com.force.sdk.oauth.context.SecurityContext;
 
 /**
  * 
  * This service uses the Chatter REST API and the ChatterPostDao to retrieve and manipulate 
  * Chatter To Do items for the logged in user. 
  *
  * @author John Simone
  */
 @Service
 public class ChatterService {
     
     public static final String API_VERSION = "v22.0";
     
     private static final Logger logger = LoggerFactory.getLogger(ChatterService.class);
     
     //The dao will be created and injected in by Spring
     @Autowired
     private ChatterPostDao chatterPostDao;
     
     /**
      * Builds the connection to the specified API destination based on data from the logged 
      * in user. Optionally the userid can be added to the end of the URL.
      */
     private URLConnection buildConn(String destination, boolean includeUserId) throws IOException {
         SecurityContext sc = ForceSecurityContextHolder.get(false);
         
         String endpoint = getEndpoint();
         String url = endpoint + destination + (includeUserId ? sc.getUserId() : "");
         
         URL restUrl = new URL(url);
         URLConnection urlConn = restUrl.openConnection();
         urlConn.addRequestProperty("Authorization", "OAuth " + sc.getSessionId());
         
         return urlConn;
     }
 
     /**
      * Get the user id of the logged in user.
      */
     private String getUserId() {
         SecurityContext sc = ForceSecurityContextHolder.get(false);
         return sc.getUserId();
     }
     
     /**
      * Get the API endpoint for the logged in user.
      */
     private String getEndpoint() {
         SecurityContext sc = ForceSecurityContextHolder.get(false);
         return sc.getEndPoint().substring(0, sc.getEndPoint().indexOf("/services"));
     }
 
     /**
      * Read the JSON result from the connection to the API
      * @throws IOException 
      */
     private String readResult(URLConnection urlConn) throws IOException {
         BufferedReader in = null;
         StringBuilder jsonReturn = new StringBuilder();
         
         try {
             in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
             
             String inputLine;
             
             while((inputLine = in.readLine()) != null) {
                 jsonReturn.append(inputLine);
             }            
        } catch(IOException e) {
            BufferedReader errorIn = null;
            try {                
                errorIn = new BufferedReader(new InputStreamReader(((HttpURLConnection)urlConn).getErrorStream()));
                String apiErrorMessage = errorIn.readLine();
                logger.error("Error while connecting to the REST API: " + apiErrorMessage);
                throw new IOException(apiErrorMessage, e);
            } finally {
                if(errorIn != null) {
                    try {
                        errorIn.close();
                    } catch (IOException e1) {
                        logger.error("Error closing error input stream from chatter api call.");
                    }
                }                
            }
        }
        finally {
             if(in != null) {
                 try {
                     in.close();
                 } catch (IOException e1) {
                     logger.error("Error closing input stream from chatter api call.");
                 }
             }
         }
         
        return jsonReturn.toString();
     }
     
     /**
      * Return all of the posts in the current user's to do list. Posts will be pulled from the database first
      * and then new posts retrieved from the API will be added to the list.
      * 
      * @return A list of ChatterPosts
      * @throws IOException
      */
     public List<ChatterPost> getToDoFeed() throws IOException {
         List<ChatterPost> posts = getStoredPostsForUser();
         getLikes(posts);
         getMentions(posts);
         Collections.sort(posts);
         return posts;
     }
     
     /**
      * Add posts where the logged in user is mentioned to the list of posts if they are not already present.
      * 
      * @param posts - The existing posts
      * @return The updated list of posts
      * @throws IOException if there is an error connecting to the API
      */
     public List<ChatterPost> getMentions(List<ChatterPost> posts) throws IOException {
         
         URLConnection urlConn = buildConn("/services/data/" + API_VERSION + "/chatter/feeds/to/", true);
         String json = readResult(urlConn);
         
         ObjectMapper mapper = new ObjectMapper();
         JsonNode rootNode = mapper.readValue(json.getBytes(), JsonNode.class);
         
         JsonNode itemsNode = rootNode.path("feedItems").path("items");
         Iterator<JsonNode> itemsIter = itemsNode.getElements();
         while(itemsIter.hasNext()) {
             JsonNode itemNode = itemsIter.next();
             ChatterPost post = buildChatterPost(itemNode, ChatterPost.TO_DO_REASON.MENTION);
             if(!posts.contains(post)) {                
                 posts.add(post);
                 chatterPostDao.savePost(post);
             }
         }
         
         return posts;
     }
 
     /**
      * Add posts that the logged in user likes to the list of posts if they are not already present.
      * 
      * @param posts - The existing posts
      * @return The updated list of posts
      * @throws IOException if there is an error connecting to the API
      */
     public List<ChatterPost> getLikes(List<ChatterPost> posts) throws IOException {
 
         URLConnection urlConn = buildConn("/services/data/" + API_VERSION + "/chatter/feeds/news/me ", false);
         
         String json = readResult(urlConn);
         
         ObjectMapper mapper = new ObjectMapper();
         JsonNode rootNode = mapper.readValue(json.getBytes(), JsonNode.class);
 
         JsonNode itemsNode = rootNode.path("feedItems").path("items");
         Iterator<JsonNode> itemsIter = itemsNode.getElements();
         String userId = getUserId();
         while(itemsIter.hasNext()) {
             JsonNode itemNode = itemsIter.next();
             if(didILike(itemNode.get("likes"), userId)) {
                 ChatterPost post = buildChatterPost(itemNode, ChatterPost.TO_DO_REASON.LIKE);
                 if(!posts.contains(post)) {
                     posts.add(post);
                     chatterPostDao.savePost(post);
                 }
             }
         }
         
         return posts;
     }
     
     /**
      * Set the post's done field to true and save it to the database.
      * The post will also be checked to see that it belongs to the current user.
      * 
      * @param postId
      */
     @Transactional
     public void setPostToDone(Integer postId) {
         ChatterPost post = chatterPostDao.getPost(postId, getUserId());
         post.setDone(true);
         chatterPostDao.savePost(post);
     }
 
     /**
      * Set the post's done field to false and save it to the database.
      * The post will also be checked to see that it belongs to the current user.
      * 
      * @param postId
      */    
     @Transactional
     public void setPostToNotDone(Integer postId) {
         ChatterPost post = chatterPostDao.getPost(postId, getUserId());
         post.setDone(false);
         chatterPostDao.savePost(post);
     }
     
     /**
      * Construct a ChatterPost object from a JsonNode of a chatter feed item.
      */
     private ChatterPost buildChatterPost(JsonNode item, ChatterPost.TO_DO_REASON toDoReason) {
         ChatterPost post = new ChatterPost();
         
         post.setId(item.path("id").getTextValue());
         post.setTitle(item.path("title").getTextValue());
         post.setAuthorName(item.path("user").path("name").getTextValue());
         post.setAuthorId(item.path("user").path("id").getTextValue());
         post.setFeedOwnerUserId(getUserId());
         post.setBody(item.path("body").path("text").getTextValue());
         post.setReason(toDoReason);
         
         String createDate = item.path("createdDate").getTextValue();
         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
         try {
             post.setPostDate(df.parse(createDate));
         } catch (ParseException e1) {
             post.setPostDate(null);
         }
         
         try {
             post.setAuthorLink(new URL(buildLink(item.path("user").path("id").getTextValue())));
             post.setPostLink(new URL(buildLink(item.path("user").path("id").getTextValue(), post.getId())));
         } catch (MalformedURLException e) {
             post.setAuthorLink(null);
             post.setPostLink(null);
         }
 
         return post;
     }
     
     /**
      * Inspect the JsonNode representing the list of likes for a post and decide if the
      * current user liked this post.
      */
     private boolean didILike(JsonNode likesNode, String userId) {
         int total = likesNode.path("total").getIntValue();
         
         if(total < 1) {
             return false;
         }
         
         Iterator<JsonNode> likesIter = likesNode.path("likes").getElements();
         
         while(likesIter.hasNext()) {
             JsonNode likeNode = likesIter.next();
             String likeUserId = likeNode.path("user").path("id").getTextValue();
             if(userId.equals(likeUserId)) {
                 return true;
             }
         }
         
         return false;
         
     }
     
     /**
      * Build the link to the Chatter post specified by this user id and post id.
      */
     private String buildLink(String userId, String postId) {
         return buildLink(userId) + "&ChatterFeedItemId=" + postId;
     }
     
     /**
      * Build the link to the user's profile.
      */
     private String buildLink(String userId) {
         return getEndpoint() + "/_ui/core/userprofile/UserProfilePage?u=" + userId;
     }
     
     /**
      * Retrieves the posts that are stored in the DB for this user.
      */
     private List<ChatterPost> getStoredPostsForUser() {
         return chatterPostDao.getPostsForUser(getUserId());
     }
 }
