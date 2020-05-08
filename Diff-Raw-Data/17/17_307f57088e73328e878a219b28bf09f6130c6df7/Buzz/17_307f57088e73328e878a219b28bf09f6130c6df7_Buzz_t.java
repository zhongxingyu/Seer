 package com.google.buzz;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.net.ssl.HttpsURLConnection;
 
 import oauth.signpost.OAuth;
 // Made signpost 1.2.1.1 compliant
 //import oauth.signpost.signature.SignatureMethod;
 
 import com.google.buzz.exception.BuzzAuthenticationException;
 import com.google.buzz.exception.BuzzIOException;
 import com.google.buzz.exception.BuzzParsingException;
 import com.google.buzz.exception.BuzzValidationException;
 import com.google.buzz.io.BuzzIO;
 import com.google.buzz.model.BuzzComment;
 import com.google.buzz.model.BuzzCommentsFeed;
 import com.google.buzz.model.BuzzContent;
 import com.google.buzz.model.BuzzFeed;
 import com.google.buzz.model.BuzzFeedEntry;
 import com.google.buzz.model.BuzzLink;
 import com.google.buzz.model.BuzzUserProfile;
 import com.google.buzz.oauth.BuzzOAuth;
 import com.google.buzz.oauth.BuzzOAuth1;
 import com.google.buzz.oauth.BuzzOAuth2;
 import com.google.buzz.parser.BuzzCommentParser;
 import com.google.buzz.parser.BuzzCommentsParser;
 import com.google.buzz.parser.BuzzFeedEntryParser;
 import com.google.buzz.parser.BuzzFeedParser;
 import com.google.buzz.parser.BuzzUserProfileParser;
 import com.google.buzz.parser.BuzzUsersProfilesParser;
 import com.google.buzz.xml.XMLGenerator;
 
 /**
  * Main class for the Buzz Client API.
  * 
  * @author roberto.estivill
  */
 public class Buzz
 {
     /**
      * Constant for buzz read only oauth scope
      */
     public static final String BUZZ_SCOPE_READONLY = "https://www.googleapis.com/auth/buzz.readonly";
 
     /**
      * Constant for buzz full access ( write ) oauth scope
      */
     public static final String BUZZ_SCOPE_WRITE = "https://www.googleapis.com/auth/buzz";
 
     /**
      * Constant for buzz activities url
      */
     public static final String BUZZ_URL_ACTIVITIES = "https://www.googleapis.com/buzz/v1/activities/";
 
     /**
      * Constant for buzz people url
      */
     public static final String BUZZ_URL_PEOPLE = "https://www.googleapis.com/buzz/v1/people/";
 
     /**
      * The OAuth wrapper class to be used.
      */
     private BuzzOAuth buzzOAuth;
 
     /**
      * Default Constructor method.
      */
     public Buzz()
     {
         buzzOAuth = null;
     }
 
     public void setOAuthVersion(int version)
     {
      switch(version) {
        case 1: buzzOAuth = new BuzzOAuth1();
                break;
        case 2: buzzOAuth = new BuzzOAuth2();
                break;
        default:
                throw new IllegalArgumentException("oauth version must be 1 or 2");
      }
     }
 
    public BuzzOAuth getBuzzOAuth()
     {
      if (buzzOAuth==null) {
          buzzOAuth=new BuzzOAuth2();
      }
      return buzzOAuth;
     }
 
     /**
      * Method to obtain the Google user authentication web page. <br/>
      * This page is going to be used by the user to allow third parties applications access his/her
      * Google Buzz information and activities.
      * 
      * @param scope either BUZZ_SCOPE_READONLY or BUZZ_SCOPE_WRITE
      * @param consumerKey to retrieve the request token
      * @param consumerSecret to retrieve the request token
      * @param callbackUrl the url google should redirect the user after a successful login
      * @return the authentication url for the user to log in
      * @throws BuzzAuthenticationException if an OAuth problem occurs
      */
     public String getAuthenticationUrl( String scope, String consumerKey, String consumerSecret, String callbackUrl )
         throws BuzzAuthenticationException
     {
 // Made signpost 1.2.1.1 compliant
 //        return buzzOAuth.getAuthenticationUrl( SignatureMethod.HMAC_SHA1, scope, consumerKey, consumerSecret,
 //                                               callbackUrl );
     
         return getBuzzOAuth().getAuthenticationUrl(scope, consumerKey, 
                                               consumerSecret,
                                               callbackUrl );
     }
 

     /**
      * Convinient method overloading for non-web applications.<br/>
      * Sets callbackUrl to 'oob', OAuth default callback value for non-web applications.
      * 
      * @param scope either BUZZ_SCOPE_READONLY or BUZZ_SCOPE_WRITE
      * @param consumerKey to retrieve the request token
      * @param consumerSecret to retrieve the request token
      * @return the authentication url for the user to log in
      * @throws BuzzAuthenticationException if an OAuth problem occurs
      */
     public String getAuthenticationUrl( String scope, String consumerKey, String consumerSecret )
         throws BuzzAuthenticationException
     {
         return getAuthenticationUrl( scope, consumerKey, consumerSecret, OAuth.OUT_OF_BAND );
     }
 
     /**
      * retrieve the access token to be used in the request signature.<br/>
      * 
      * @param accessToken to be used in the request signing process.
      * @throws BuzzAuthenticationException if an OAuth problem occurs
      */
     public void retrieveAccessToken( String oauthVerifier, String callback )
         throws BuzzAuthenticationException
     {
         getBuzzOAuth().retrieveAccessToken( oauthVerifier, callback );
     }
     
 
     /**
      * Set the token / secret to be used for authentication.<br/>
      * 
      * @param accessToken to be used for authentication.
      * @param tokenSecret to be used for authentication.
      * @throws BuzzAuthenticationException if an OAuth problem occurs
      */
     public void setTokenWithSecret( String accessToken, String tokenSecret )
    		throws BuzzAuthenticationException
 	{
 	    getBuzzOAuth().setTokenWithSecret( accessToken, tokenSecret );
 	}
 
 
     /**
      * set consumer with known key and secret for given scope.
      *@param consumerKey  consumerKey to be used for authentication.
      *@param consumerToken  consumerToken to be used for authentication.
      *@param scope - scope, for which we authenticate.
      **/
     public void setConsumerForScope(String consumerKey, String consumerSecret,
                                     String scope)
    		throws BuzzAuthenticationException
      {
          getBuzzOAuth().setConsumerForScope(consumerKey, consumerSecret, scope);
      }
 
     /**
      * Wrapper method for getting feeds. <br/>
      * Depending on the feed type, the request might need to be signed or not.
      * 
      * @param userId for the feed.
      * @param feedType the type of the feed to be retrieved.
      * @return the correspondent BuzzFeed
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      * @throws BuzzParsingException if a parsing error occurs
      */
     public BuzzFeed getPosts( String userId, BuzzFeed.Type feedType )
         throws BuzzIOException, BuzzAuthenticationException, BuzzParsingException
     {
         if ( BuzzFeed.Type.PUBLIC.equals( feedType ) )
         {
             return getPostsWithoutAuthentication( userId, feedType );
         }
         return getPostsWithAuthentication( userId, feedType );
     }
 
     public BuzzFeed search(String query)
         throws BuzzIOException, BuzzAuthenticationException, BuzzParsingException
     {
      try {
       String escapedQuery = new URI(null,null,null,query,null).toASCIIString().substring(1);
       HttpsURLConnection request = BuzzIO.createRequest(
                             BUZZ_URL_ACTIVITIES+"search?q="+escapedQuery);
       buzzOAuth.signRequest( request );
       String xmlResponse = BuzzIO.send( request );
       return BuzzFeedParser.parseFeed( xmlResponse );
      }catch(URISyntaxException ex){
         throw new BuzzIOException(ex);
      }
     }
 
     /**
      * Retrieve the feeds that requires authentication, @consumption and @self. <br/>
      * Parses the response into a model object
      * 
      * @param userId for the feed.
      * @param feedType the type of the feed to be retrieved.
      * @return the correspondent BuzzFeed
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      * @throws BuzzParsingException if a parsing error occurs
      */
     private BuzzFeed getPostsWithAuthentication( String userId, BuzzFeed.Type feedType )
         throws BuzzIOException, BuzzAuthenticationException, BuzzParsingException
     {
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_ACTIVITIES + userId + "/" + feedType.getName() );
         buzzOAuth.signRequest( request );
         String xmlResponse = BuzzIO.send( request );
         return BuzzFeedParser.parseFeed( xmlResponse );
     }
 
     /**
      * Retrieve the feeds that doesn't require authentication, @public. <br/>
      * Parses the response into a model object
      * 
      * @param userId for the feed.
      * @param feedType the type of the feed to be retrieved.
      * @return the correspondent BuzzFeed
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      * @throws BuzzParsingException if a parsing error occurs
      */
     private BuzzFeed getPostsWithoutAuthentication( String userId, BuzzFeed.Type feedType )
         throws BuzzIOException, BuzzAuthenticationException, BuzzParsingException
     {
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_ACTIVITIES + userId + "/" + feedType.getName() );
         String xmlResponse = BuzzIO.send( request );
         return BuzzFeedParser.parseFeed( xmlResponse );
     }
 
     /**
      * Retrieves the google profile for a particular user.
      * 
      * @return the BuzzUserProfile for the given user
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      * @throws BuzzParsingException if a parsing error occurs
      */
     public BuzzUserProfile getUserProfile( String userId )
         throws BuzzIOException, BuzzAuthenticationException, BuzzParsingException
     {
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_PEOPLE + userId + "/"
             + BuzzFeed.Type.PRIVATE.getName() );
         buzzOAuth.signRequest( request );
         String xmlResponse = BuzzIO.send( request );
         return BuzzUserProfileParser.parseProfile( xmlResponse );
     }
 
     /**
      * Retrieves the full list of people that are following a user.
      * 
      * @param userId of a person who wanna check who is following him/her
      * @return the list of BuzzUserProfile's of all the followers
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      * @throws BuzzParsingException if a parsing error occurs
      */
     public List<BuzzUserProfile> followers( String userId )
         throws BuzzIOException, BuzzAuthenticationException, BuzzParsingException
     {
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_PEOPLE + userId + "/@groups/"
             + BuzzFeed.Type.FOLLOWERS.getName() );
         buzzOAuth.signRequest( request );
         String xmlResponse = BuzzIO.send( request );
         return BuzzUsersProfilesParser.parseUsersProfiles( xmlResponse );
     }
 
     /**
      * Retrieves the full list of people that a user is following.
      * 
      * @param userId of the person who is following the list
      * @return the list of BuzzUserProfile's of all the people being followed by the user
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      * @throws BuzzParsingException if a parsing error occurs
      */
     public List<BuzzUserProfile> following( String userId )
         throws BuzzIOException, BuzzAuthenticationException, BuzzParsingException
     {
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_PEOPLE + userId + "/@groups/"
             + BuzzFeed.Type.FOLLOWING.getName() );
         buzzOAuth.signRequest( request );
         String xmlResponse = BuzzIO.send( request );
         return BuzzUsersProfilesParser.parseUsersProfiles( xmlResponse );
     }
 
     /**
      * Start following another person.
      * 
      * @param userId of the person who wants to follow somebody else
      * @param userIdToUnfollow id of the person to follow
      * @return the string response
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      */
     public void follow( String userId, String userIdToFollow )
         throws BuzzIOException, BuzzAuthenticationException
     {
         Map<String, String> headers = new HashMap<String, String>();
         headers.put( "Content-Length", "0" );
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_PEOPLE + userId + "/@groups/"
             + BuzzFeed.Type.FOLLOWING.getName() + "/" + userIdToFollow, BuzzIO.HTTP_METHOD_PUT, headers );
         buzzOAuth.signRequest( request );
         BuzzIO.send( request );
     }
 
     /**
      * Unfollows a person from Google Buzz
      * 
      * @param userId of the person who is following another person
      * @param userIdToUnfollow id of the person being followed
      * @return the string response
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      */
     public void unfollow( String userId, String userIdToUnfollow )
         throws BuzzIOException, BuzzAuthenticationException
     {
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_PEOPLE + userId + "/@groups/"
             + BuzzFeed.Type.FOLLOWING.getName() + "/" + userIdToUnfollow, BuzzIO.HTTP_METHOD_DELETE, null );
         buzzOAuth.signRequest( request );
         BuzzIO.send( request );
     }
 
     /**
      * Creates a new post in Google Buzz
      * 
      * @param userId of the person who creates the post
      * @param content of the new post
      * @param link to a different resource
      * @return the created post
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      * @throws BuzzValidationException if any required element of the new post is missing
      * @throws BuzzParsingException if a parsing error occurs
      */
     public BuzzFeedEntry createPost( String userId, BuzzContent content, BuzzLink link )
         throws BuzzIOException, BuzzAuthenticationException, BuzzValidationException, BuzzParsingException
     {
         String payload = XMLGenerator.constructPayload( content, link );
 
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_ACTIVITIES + userId + "/"
             + BuzzFeed.Type.PRIVATE.getName(), BuzzIO.HTTP_METHOD_POST );
 
         buzzOAuth.signRequest( BuzzIO.addBody( request, payload ) );
 
         String xmlResponse = BuzzIO.send( request );
         return BuzzFeedEntryParser.parseFeedEntry( xmlResponse );
 
     }
 
     /**
      * Overloaded method to create posts without a link object. <br/>
      * End up calling <b>Buzz.createPost( String userId, BuzzContent content, BuzzLink link )</b>
      * with a link == null
      * 
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      * @throws BuzzValidationException if any required element of the new post is missing
      * @throws BuzzParsingException
      */
     public BuzzFeedEntry createPost( String userId, BuzzContent content )
         throws BuzzIOException, BuzzAuthenticationException, BuzzValidationException, BuzzParsingException
     {
         return createPost( userId, content, null );
     }
 
     /**
      * Read a post from Google Buzz
      * 
      * @param userId of the owner of the post
      * @param activityId to be read
      * @return the retrieved post
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      * @throws BuzzParsingException if a parsing error occurs
      */
     public BuzzFeedEntry getPost( String userId, String activityId )
         throws BuzzIOException, BuzzAuthenticationException, BuzzParsingException
     {
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_ACTIVITIES + userId + "/"
             + BuzzFeed.Type.PRIVATE.getName() + "/" + activityId );
 
         buzzOAuth.signRequest( request );
 
         String xmlResponse = BuzzIO.send( request );
 
         return BuzzFeedEntryParser.parseFeedEntry( xmlResponse );
     }
 
     /**
      * Delete a post in Google Buzz
      * 
      * @param userId of the owner of the post
      * @param activityId to be delete
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      */
     public void deletePost( String userId, String activityId )
         throws BuzzIOException, BuzzAuthenticationException
     {
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_ACTIVITIES + userId + "/"
             + BuzzFeed.Type.PRIVATE.getName() + "/" + activityId, BuzzIO.HTTP_METHOD_DELETE );
 
         buzzOAuth.signRequest( request );
 
         BuzzIO.send( request );
     }
 
     /**
      * Update a google buzz post.
      * 
      * @param userId of the person who created the post
      * @param content of the new post
      * @param activityId of the activity to be updated
      * @return the created post
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      * @throws BuzzValidationException if any required element of the new post is missing
      * @throws BuzzParsingException if a parsing error occurs
      */
     public BuzzFeedEntry updatePost( String userId, String activityId, BuzzContent content )
         throws BuzzValidationException, BuzzIOException, BuzzAuthenticationException, BuzzParsingException
     {
         String payload = XMLGenerator.constructPayload( content, null );
 
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_ACTIVITIES + userId + "/"
             + BuzzFeed.Type.PRIVATE.getName() + "/" + activityId, BuzzIO.HTTP_METHOD_PUT );
 
         buzzOAuth.signRequest( BuzzIO.addBody( request, payload ) );
 
         String xmlResponse = BuzzIO.send( request );
         return BuzzFeedEntryParser.parseFeedEntry( xmlResponse );
     }
 
     /**
      * Creates a new comment in a Google Buzz activity
      * 
      * @param userId of the person who creates the post
      * @param content of the new comment
      * @return the created post
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      * @throws BuzzValidationException if any required element of the new post is missing
      * @throws BuzzParsingException if a parsing error occurs
      */
     public BuzzComment createComment( String userId, String activityId, BuzzContent content )
         throws BuzzIOException, BuzzAuthenticationException, BuzzValidationException, BuzzParsingException
     {
         String payload = XMLGenerator.constructPayload( content, null );
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_ACTIVITIES + userId + "/"
             + BuzzFeed.Type.PRIVATE.getName() + "/" + activityId + "/" + BuzzFeed.Type.COMMENTS.getName(),
                                                            BuzzIO.HTTP_METHOD_POST );
         buzzOAuth.signRequest( BuzzIO.addBody( request, payload ) );
 
         String xmlResponse = BuzzIO.send( request );
 
         return BuzzCommentParser.parseComment( xmlResponse );
     }
 
     /**
      * Delete a comment in a Google Buzz post
      * 
      * @param userId of the owner of the post
      * @param activityId where the comment is
      * @param commentId to be deleted
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      */
     public void deleteComment( String userId, String activityId, String commentId )
         throws BuzzIOException, BuzzAuthenticationException
     {
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_ACTIVITIES + userId + "/"
             + BuzzFeed.Type.PRIVATE.getName() + "/" + activityId + "/" + BuzzFeed.Type.COMMENTS.getName() + "/"
             + commentId, BuzzIO.HTTP_METHOD_DELETE );
 
         buzzOAuth.signRequest( request );
 
         BuzzIO.send( request );
     }
 
     /**
      * Read a post comment from Google Buzz
      * 
      * @param userId of the owner of the post
      * @param activityId where the comment is posted
      * @param commentId to be read
      * @return the buzz comment object
      * @return the retrieved post comment
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      * @throws BuzzParsingException if a parsing error occurs
      */
     public BuzzComment getComment( String userId, String activityId, String commentId )
         throws BuzzIOException, BuzzAuthenticationException, BuzzParsingException
     {
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_ACTIVITIES + userId + "/"
             + BuzzFeed.Type.PRIVATE.getName() + "/" + activityId + "/" + BuzzFeed.Type.COMMENTS.getName() + "/"
             + commentId );
 
         buzzOAuth.signRequest( request );
 
         String xmlResponse = BuzzIO.send( request );
 
         return BuzzCommentParser.parseComment( xmlResponse );
     }
 
     /**
      * Get all the comments from a specific post.
      * 
      * @param userId logged in
      * @param activityId of the comments to be retrieved
      * @return the comments feed object
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      * @throws BuzzParsingException if a parsing error occurs
      */
     public BuzzCommentsFeed getComments( String userId, String activityId )
         throws BuzzIOException, BuzzAuthenticationException, BuzzParsingException
     {
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_ACTIVITIES + userId + "/"
             + BuzzFeed.Type.PRIVATE.getName() + "/" + activityId + "/" + BuzzFeed.Type.COMMENTS.getName() );
 
         buzzOAuth.signRequest( request );
 
         String xmlResponse = BuzzIO.send( request );
 
         return BuzzCommentsParser.parseComments( xmlResponse );
     }
 
     /**
      * Updates a comment in a Google Buzz activity
      * 
      * @param userId of the person who updates the post
      * @param activityId where the comment was done
      * @param content to be updated
      * @return the updated comment
      * @throws BuzzIOException if any IO error occurs ( networking ).
      * @throws BuzzAuthenticationException if any OAuth error occurs
      * @throws BuzzValidationException if any required element of the new post is missing
      * @throws BuzzParsingException if a parsing error occurs
      */
     public BuzzComment updateComment( String userId, String activityId, String commentId, BuzzContent content )
         throws BuzzValidationException, BuzzIOException, BuzzAuthenticationException, BuzzParsingException
     {
         String payload = XMLGenerator.constructPayload( content, null );
 
         HttpsURLConnection request = BuzzIO.createRequest( BUZZ_URL_ACTIVITIES + userId + "/"
             + BuzzFeed.Type.PRIVATE.getName() + "/" + activityId + "/" + BuzzFeed.Type.COMMENTS.getName() + "/"
             + commentId, BuzzIO.HTTP_METHOD_PUT );
 
         buzzOAuth.signRequest( BuzzIO.addBody( request, payload ) );
 
         String xmlResponse = BuzzIO.send( request );
 
         return BuzzCommentParser.parseComment( xmlResponse );
     }
 
     /**
      * Returns the people who liked the post.
      *@param userId -- author of post
      *@param postId -- id of post.
      */
     public List<BuzzUserProfile> getLikes(String userId, String postId)
         throws BuzzValidationException, BuzzIOException, BuzzAuthenticationException, BuzzParsingException
     {
         HttpsURLConnection request = BuzzIO.createRequest( 
                BUZZ_URL_ACTIVITIES + userId + "/@self/"+postId+"/@liked");
         buzzOAuth.signRequest( request );
         String xmlResponse = BuzzIO.send( request );
         return BuzzUsersProfilesParser.parseUsersProfiles( xmlResponse );
     }
 
     public BuzzFeed likedPosts()
         throws BuzzIOException, BuzzAuthenticationException, 
                BuzzParsingException
     {
       return getPosts("@me",BuzzFeed.Type.LIKED);
     }
 
     public void likePost(String userId, String postId)
         throws BuzzIOException, BuzzAuthenticationException,
                BuzzValidationException
     {
       HttpsURLConnection request = BuzzIO.createRequest( 
                BUZZ_URL_ACTIVITIES + userId  + "/@liked/"+postId,
                BuzzIO.HTTP_METHOD_PUT);
       String payload = " "; // not-empty, becouse Content-Length required. 
       request = BuzzIO.addBody(request, payload ); 
       buzzOAuth.signRequest( request );
       String xmlResponse = BuzzIO.send( request );
     }
 
     public void likePost(String postId)
         throws BuzzIOException, BuzzAuthenticationException, 
               BuzzValidationException
       { likePost("@me",postId); }
 
     public void unlikePost(String userId, String postId)
         throws BuzzIOException, BuzzAuthenticationException
     {
       HttpsURLConnection request = BuzzIO.createRequest( 
                BUZZ_URL_ACTIVITIES + userId  + "/@liked/"+postId,
                BuzzIO.HTTP_METHOD_DELETE);
       buzzOAuth.signRequest( request );
       String xmlResponse = BuzzIO.send( request );
     }
 
     public void unlikePost(String postId)
         throws BuzzIOException, BuzzAuthenticationException
     { unlikePost("@me",postId); }
 
 
     // yet not work: see 
     //     http://code.google.com/p/google-buzz-api/issues/detail?id=134
     public BuzzFeedEntry resharePost(String userId,
                                      String postId, String annotation)
         throws BuzzIOException, BuzzAuthenticationException,
               BuzzValidationException, BuzzParsingException
     {
       HttpsURLConnection request = BuzzIO.createRequest( 
                BUZZ_URL_ACTIVITIES +  userId+"/@self",
                BuzzIO.HTTP_METHOD_POST);
       String payload = XMLGenerator.constructActivityIdPayload(postId, 
                                                                annotation);
       //System.err.println("payload="+payload);
       request = BuzzIO.addBody(request, payload ); 
       buzzOAuth.signRequest( request );
       
       String xmlResponse = BuzzIO.send( request );
       return BuzzFeedEntryParser.parseFeedEntry(xmlResponse); 
     }
 
     public void mutedPosts()
     {
     }
 
     public void mutePost()
     {
     }
 
     public void unmutePost()
     {
     }
 
     public List<BuzzUserProfile> searchPeople(String query)
         throws BuzzIOException, BuzzAuthenticationException, BuzzParsingException
     {
      try {
       String escapedQuery = new URI(null,null,null,query,null).toASCIIString().substring(1);
       HttpsURLConnection request = BuzzIO.createRequest(
                             BUZZ_URL_PEOPLE+"search?q="+escapedQuery);
       buzzOAuth.signRequest( request );
       String xmlResponse = BuzzIO.send( request );
       return BuzzUsersProfilesParser.parseUsersProfiles( xmlResponse );
      }catch(URISyntaxException ex){
         throw new BuzzIOException(ex);
      }
     }
 
 
 
     public void suggestedUsers()
     {
     }
 
     public void getGroups()
     {
     }
 
     public void createGroup()
     {
     }
 
     public void updateGroup()
     {
     }
 
     public void deleteGroup()
     {
     }
 
     public void getGroupMembers()
     {
     }
 
     public void createGroupMember()
     {
     }
 
     public void deleteGroupMember()
     {
     }
 
     public void blocked()
     {
     }
 
     public void block()
     {
     }
 
     public void unblock()
     {
     }
 
     public void isBlocked()
     {
     }
 
     public void reportActivity()
     {
     }
 
     public void reportUser()
     {
     }
 }
