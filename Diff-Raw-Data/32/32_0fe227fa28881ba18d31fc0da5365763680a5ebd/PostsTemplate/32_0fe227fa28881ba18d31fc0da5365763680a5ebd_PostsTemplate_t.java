 package org.springframework.social.appdotnet.api.impl;
 
 import org.springframework.social.appdotnet.api.PostsOperations;
 import org.springframework.social.appdotnet.api.data.ADNResponse;
 import org.springframework.social.appdotnet.api.data.post.ADNPost;
 import org.springframework.social.appdotnet.api.data.post.ADNPosts;
 import org.springframework.util.LinkedMultiValueMap;
 import org.springframework.util.MultiValueMap;
 import org.springframework.web.client.RestTemplate;
 
import java.net.URI;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Implementation of {@link PostsOperations}, accessing App.net posts resources
  *
  * @author Arik Galansky
  */
 class PostsTemplate extends AbstractAppdotnetOperations implements PostsOperations {
 
     public PostsTemplate(String accessToken, RestTemplate restTemplate) {
         super(accessToken, restTemplate, "posts", VERSION_0);
     }
 
     @Override
     public ADNPost get(String id) {
         return restTemplate.getForObject(buildUri(id), PostResponse.class).getData();
     }
 
     @Override
     public ADNPost create(String text) {
         return create(text, null);
     }
 
     @Override
     public ADNPost create(String text, String replyTo) {
         MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
         map.add("text", text);
         if (replyTo != null) {
             map.add("reply_to", replyTo);
         }
         return restTemplate.postForObject(buildUri(), map, PostResponse.class).getData();
     }
 
     @Override
     public void delete(String id) {
         restTemplate.delete(buildUri(id));
     }
 
     @Override
     public ADNPost repost(String id) {
         return restTemplate.postForObject(buildUri(id + "/repost"), null, PostResponse.class).getData();
     }
 
     @Override
     public void unrepost(String id) {
         restTemplate.delete(buildUri(id + "/repost"));
     }
 
     @Override
     public ADNPost star(String id) {
         return restTemplate.postForObject(buildUri(id + "/star"), null, PostResponse.class).getData();
     }
 
     @Override
     public void unstar(String id) {
         restTemplate.delete(buildUri(id + "/star"));
     }
 
     @Override
     public ADNPosts getPersonalStream(Map<String, String> extraParams) {
         return getPosts(extraParams, "stream", null);
     }
 
     @Override
     public ADNPosts getPersonalStream() {
         return getPersonalStream(null);
     }
 
     @Override
     public ADNPosts getGlobalStream(Map<String, String> extraParams) {
         return getPosts(extraParams, "stream/global", null);
     }
 
     @Override
     public ADNPosts getGlobalStream() {
         return getGlobalStream(null);
     }
 
     @Override
     public ADNPosts getHashtagStream(String hashtag, Map<String, String> extraParams) {
         return getPosts(extraParams, "tag/" + hashtag, null);
     }
 
     @Override
     public ADNPosts getHashtagStream(String hashtag) {
         return getHashtagStream(hashtag, null);
     }
 
     @Override
     public ADNPosts getPostReplies(String id, Map<String, String> extraParams) {
         return getPosts(extraParams, id + "/replies", null);
     }
 
     @Override
     public ADNPosts getPostReplies(String id) {
         return getPostReplies(id, null);
     }
 
     @Override
     public ADNPosts getUserPosts(String userId, Map<String, String> extraParams) {
         return getPosts(extraParams, userId + "/posts", "users");
     }
 
     @Override
     public ADNPosts getUserPosts(String userId) {
         return getUserPosts(userId, null);
     }
 
     @Override
     public ADNPosts getUserMentions(String userId, Map<String, String> extraParams) {
         return getPosts(extraParams, userId + "/mentions", "users");
     }
 
     @Override
     public ADNPosts getUserMentions(String userId) {
         return getUserMentions(userId, null);
     }
 
     // private
 
     private ADNPosts getPosts(Map<String, String> extraParams, String action, String resource) {
        URI url;
         if (resource == null) {
             url = buildUri(action, extraParams);
         } else {
             url = buildUri(resource, action, extraParams);
         }
         ADNResponse<List<ADNPost>> response = restTemplate.getForObject(url, PostsResponse.class);
         return new ADNPosts(response.getData(), createPaging(response.getMeta()));
     }
 }
