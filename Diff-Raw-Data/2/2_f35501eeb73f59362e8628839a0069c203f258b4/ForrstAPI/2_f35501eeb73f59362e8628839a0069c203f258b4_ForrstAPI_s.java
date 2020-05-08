 package com.forrst.api;
 
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONObject;
 
 import com.forrst.api.model.Auth;
 import com.forrst.api.model.Comment;
 import com.forrst.api.model.Post;
 import com.forrst.api.model.Stat;
 import com.forrst.api.model.User;
 import com.forrst.api.util.ForrstAuthenticationException;
 
 public interface ForrstAPI {
 
     /**
      * All API calls (with the exception of stats) are rated
      * limited at 150 calls per hour. In the future, we will
      * offer whitelisting by request. When making unauthenticated
      * calls, each request counts against the total made by
      * the calling IP address; when authenticating, calls
      * count against the authenticated user's limit. You may
      * request rate limiting stats at api/v2/stats (it won't
      * count against your total). Your limit resets each hour.
      */
     public int RATE_LIMIT = 150;
 
     /**
      * Returns stats about your API usage. Note: does
      * not count against your rate limit.
      *
      * @return Stat object containing current rate limit & calls made
      */
     public Stat stats();
     
     /**
      * Return notification items for the authenticating user.
      * Most items have an associated redirect URL that will
      * take the user to the appropriate post/comment/etc. and
      * clear the associated notification. Construct the URLs
      * using the view_url_format format (provided in the
      * response), replacing ID with the ID of the desired
      * notification. Also note that not every type of
      * notification (currently for likes, comments [replies,
      * subscription-based, on your post], mentions, jobs, and
      * follows) will have the same fields present for data.
      * 
      * @param accessToken Token obtained when the user is authenticated
      * @param options is a map & can contain
      *        grouped: [optional] Boolean indicating whether
      *                 to return items logically grouped by
      *                 type 
      * @return JSON response containing:
      *         items {
      *             like {
      *                 ...
      *             }
      *         },
      *         view_url_format
      */
     public JSONObject notifications(String accessToken, Map<String,String> options);
 
     /**
      * User authentication. Provide an email/username
      * and password and get the access token back
      *
      * @param emailOrUsername Email/Username
      * @param password Password
      * @return Auth object containing access token and user id
      * @throws ForrstAuthenticationException when authentication fails
      */
     public Auth usersAuth(String emailOrUsername, String password) throws ForrstAuthenticationException;
 
     /**
      * Given a property identifying a user return a user
      * 
      * @param userInfo Map containing user id or username
      * @return JSON response containing:
      *         id,
      *         username,
      *         name,
      *         url,
      *         posts,
      *         comments,
      *         likes,
      *         followers,
      *         following,
      *         photos {
      *             xl_url,
      *             large_url,
      *             medium_url,
      *             small_url,
      *             thumb_url
      *         },
      *         bio,
      *         is_a,
      *         homepage_url,
      *         twitter,
      *         in_directory,
      *         tag_string
      */
    public User usersInfo(Map<String,String> userInfo);    
 
     /**
      * Returns a user's posts
      * 
      * @param id User identifier - id or username
      * @param options is a map & can contain:
      *        type [optional] Post type (code, snap, link, question)
      *        limit [optional, default = 10, max = 25] number of posts to return per page
      *        after [optional] if given, return posts with an ID lower than after
      * @return list of posts
      */
     public List<Post> userPosts(Map<String,String> userInfo, Map<String,String> options);
 
     /**
      * Return data about a single post. Note: For questions,
      * content is the question. For code, content contains
      * the code snippet. For code, snaps, and links, description
      * is the post description; it is not used for questions.
      * 
      * @param id Post ID
      * @return post object containing user (+ users photos) and
      *          post snaps if available.
      */
     public Post postsShow(int id);
 
     /**
      * Returns a list of all posts in reverse-chron order
      * 
      * @return JSON response containing:
      *         posts [{
      *             id,
      *             tiny_id,
      *             post_type,
      *             post_url,
      *             created_at,
      *             updated_at,
      *             user: {
      *                 ...
      *             },
      *             published,
      *             public,
      *             title,
      *             url,
      *             content,
      *             description,
      *             formatted_content,
      *             formatted_description,
      *             like_count,
      *             comment_count,
      *             snaps {
      *                 mega_url,
      *                 keith_url,
      *                 large_url,
      *                 medium_url,
      *                 small_url,
      *                 thumb_url,
      *                 original_url
      *             }
      *         }],
      *         page
      */
     public JSONObject postsAll(Map<String,String> options);
 
     /**
      * Returns a list of posts of a given type
      * 
      * @param postType Post type (code, snap, link, question)
      * @param options Map containing
      *        sort [optional, default = recent] Sort by recent, popular, best (staff picks)
      *        page [optional, default = 1] Page of results to return
      * @return JSON response containing:
      *         posts [{
      *             id,
      *             tiny_id,
      *             post_type,
      *             post_url,
      *             created_at,
      *             updated_at,
      *             user: {
      *                 ...
      *             },
      *             published,
      *             public,
      *             title,
      *             url,
      *             content,
      *             description,
      *             formatted_content,
      *             formatted_description,
      *             like_count,
      *             comment_count,
      *             snaps {
      *                 mega_url,
      *                 keith_url,
      *                 large_url,
      *                 medium_url,
      *                 small_url,
      *                 thumb_url,
      *                 original_url
      *             }
      *         }],
      *         page
      */
     public JSONObject postsList(String postType, Map<String,String> options);
 
     /**
      * Returns a post's comments
      * 
      * @param accessToken Token obtained when the user is authenticated
      * @param id Post ID
      * @return JSON response containing:
      *         comments [{
      *             id,
      *             user {
      *                 ...
      *             },
      *             body,
      *             created_at,
      *             updated_at
      *         }],
      *         count
      */
     public List<Comment> postComments(String accessToken, int id);
 
     /**
      * Returns a map containing the name of the endpoint and its URI
      * @return A map of endpoint name and uri
      */
     public Map<String,String> getEndpointsURIs();
 }
