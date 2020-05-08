 /**
  * Mule LinkedIn Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.linkedin;
 
 import com.google.code.linkedinapi.client.LinkedInApiClient;
 import com.google.code.linkedinapi.client.Parameter;
 import com.google.code.linkedinapi.client.enumeration.ConnectionModificationType;
 import com.google.code.linkedinapi.client.enumeration.FacetField;
 import com.google.code.linkedinapi.client.enumeration.NetworkUpdateType;
 import com.google.code.linkedinapi.client.enumeration.ProfileField;
 import com.google.code.linkedinapi.client.enumeration.ProfileType;
 import com.google.code.linkedinapi.client.enumeration.SearchParameter;
 import com.google.code.linkedinapi.client.enumeration.SearchSortOrder;
 import com.google.code.linkedinapi.schema.Connections;
 import com.google.code.linkedinapi.schema.FacetType;
 import com.google.code.linkedinapi.schema.Likes;
 import com.google.code.linkedinapi.schema.Network;
 import com.google.code.linkedinapi.schema.People;
 import com.google.code.linkedinapi.schema.PeopleSearch;
 import com.google.code.linkedinapi.schema.Person;
 import com.google.code.linkedinapi.schema.UpdateComments;
 import com.google.code.linkedinapi.schema.VisibilityType;
 import org.mule.api.annotations.Configurable;
 import org.mule.api.annotations.Module;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.oauth.OAuth;
 import org.mule.api.annotations.oauth.OAuthAccessToken;
 import org.mule.api.annotations.oauth.OAuthAccessTokenSecret;
 import org.mule.api.annotations.oauth.OAuthConsumerKey;
 import org.mule.api.annotations.oauth.OAuthConsumerSecret;
 import org.mule.api.annotations.param.Default;
 import org.mule.api.annotations.param.Optional;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * LinkedIn is a business-related social networking site. Founded in December 2002 and launched in May 2003, it is
  * mainly used for professional networking. This connector allows you to interact with LinkedIn API.
  *
  * @author MuleSoft, Inc.
  */
 @Module(name = "linkedin")
 @OAuth(requestTokenUrl = "https://api.linkedin.com/uas/oauth/requestToken",
         accessTokenUrl = "https://api.linkedin.com/uas/oauth/accessToken",
         authorizationUrl = "https://api.linkedin.com/uas/oauth/authorize")
 public class LinkedInConnector {
 
     /**
      * API Key
      */
     @Configurable
     @OAuthConsumerKey
     private String apiKey;
 
     /**
      * API Secret
      */
     @Configurable
     @OAuthConsumerSecret
     private String apiSecret;
 
     private LinkedInApiClient client;
 
     /**
      * Gets the profile for current user.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1002">http://developer.linkedin.com/docs/DOC-1002</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:getProfileForCurrentUser}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:getProfileForCurrentUser}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param profileFields     the profile fields to retrieve
      * @return the profile for current user
      */
     @Processor
     public Person getProfileForCurrentUser(@OAuthAccessToken String accessToken,
                                            @OAuthAccessTokenSecret String accessTokenSecret,
                                            @Optional List<ProfileField> profileFields) {
         if (profileFields == null) {
             return getClient(accessToken, accessTokenSecret).getProfileForCurrentUser();
         } else {
             return getClient(accessToken, accessTokenSecret).getProfileForCurrentUser(createSet(profileFields));
         }
     }
 
     /**
      * Gets the profile by id.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1002">http://developer.linkedin.com/docs/DOC-1002</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:getProfileById}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:getProfileById}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param id                the id to search
      * @param profileFields     the profile fields to retrieve
      * @return the profile by id
      */
     @Processor
     public Person getProfileById(@OAuthAccessToken String accessToken,
                                  @OAuthAccessTokenSecret String accessTokenSecret,
                                  String id,
                                  @Optional List<ProfileField> profileFields) {
         if (profileFields == null) {
             return getClient(accessToken, accessTokenSecret).getProfileById(id);
         } else {
             return getClient(accessToken, accessTokenSecret).getProfileById(id, createSet(profileFields));
         }
     }
 
     /**
      * Gets the profile by url.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1002">http://developer.linkedin.com/docs/DOC-1002</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:getProfileByUrl}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:getProfileByUrl}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param url               the url to search
      * @param profileType       the profile type to search
      * @param profileFields     the profile fields to retrieve
      * @return the profile by url
      */
     @Processor
     public Person getProfileByUrl(@OAuthAccessToken String accessToken,
                                   @OAuthAccessTokenSecret String accessTokenSecret,
                                   String url,
                                   ProfileType profileType,
                                   @Optional List<ProfileField> profileFields) {
         if (profileFields == null) {
             return getClient(accessToken, accessTokenSecret).getProfileByUrl(url, profileType);
         } else {
             return getClient(accessToken, accessTokenSecret).getProfileByUrl(url, profileType, createSet(profileFields));
         }
     }
 
     /**
      * Gets the network updates.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1006">http://developer.linkedin.com/docs/DOC-1006</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:getNetworkUpdates}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:getNetworkUpdates}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param updateTypes       the update types to retrieve
      * @param start             the start, if set count needs to be specified
      * @param count             the count, if set start needs to be specified
      * @param startDate         the start date, if set end date needs to be specified
      * @param endDate           the end date, if set start date needs to be specified
      * @param showHiddenMembers whether to show hidden memberts
      * @return the network updates
      */
     @Processor
     public Network getNetworkUpdates(@OAuthAccessToken String accessToken,
                                      @OAuthAccessTokenSecret String accessTokenSecret,
                                      @Optional List<NetworkUpdateType> updateTypes,
                                      @Optional Integer start,
                                      @Optional Integer count,
                                      @Optional Date startDate,
                                      @Optional Date endDate,
                                      @Optional Boolean showHiddenMembers) {
         if (updateTypes != null && start != null && count != null && startDate != null && endDate != null && showHiddenMembers != null) {
             return getClient(accessToken, accessTokenSecret).getNetworkUpdates(createSet(updateTypes), start, count, startDate, endDate, showHiddenMembers);
         }
         if (updateTypes != null && start != null && count != null && startDate != null && endDate != null) {
             getClient(accessToken, accessTokenSecret).getNetworkUpdates(createSet(updateTypes), start, count, startDate, endDate);
         }
         if (updateTypes != null && start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).getNetworkUpdates(createSet(updateTypes), start, count);
         }
         if (updateTypes != null && startDate != null && endDate != null) {
             return getClient(accessToken, accessTokenSecret).getNetworkUpdates(createSet(updateTypes), startDate, endDate);
         }
         if (start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).getNetworkUpdates(start, count);
         }
         if (startDate != null && endDate != null) {
             return getClient(accessToken, accessTokenSecret).getNetworkUpdates(startDate, endDate);
         }
         if (updateTypes != null) {
             return getClient(accessToken, accessTokenSecret).getNetworkUpdates(createSet(updateTypes));
         }
         return getClient(accessToken, accessTokenSecret).getNetworkUpdates();
     }
 
     /**
      * Gets the network updates.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1006">http://developer.linkedin.com/docs/DOC-1006</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:getUserUpdates}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:getUserUpdates}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param updateTypes       the update types to retrieve
      * @param start             the start, if set count needs to be specified
      * @param count             the count, if set start needs to be specified
      * @param startDate         the start date, if set end date needs to be specified
      * @param endDate           the end date, if set start date needs to be specified
      * @return the network updates
      */
     @Processor
     public Network getUserUpdates(@OAuthAccessToken String accessToken,
                                   @OAuthAccessTokenSecret String accessTokenSecret,
                                   @Optional List<NetworkUpdateType> updateTypes,
                                   @Optional Integer start,
                                   @Optional Integer count,
                                   @Optional Date startDate,
                                   @Optional Date endDate) {
         if (updateTypes != null && start != null && count != null && startDate != null && endDate != null) {
             return getClient(accessToken, accessTokenSecret).getUserUpdates(createSet(updateTypes), start, count, startDate, endDate);
         }
         if (updateTypes != null && start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).getUserUpdates(createSet(updateTypes), start, count);
         }
         if (updateTypes != null && startDate != null && endDate != null) {
             return getClient(accessToken, accessTokenSecret).getUserUpdates(createSet(updateTypes), startDate, endDate);
         }
         if (startDate != null && endDate != null) {
             return getClient(accessToken, accessTokenSecret).getUserUpdates(startDate, endDate);
         }
         if (start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).getUserUpdates(start, count);
         }
         if (updateTypes != null) {
             return getClient(accessToken, accessTokenSecret).getUserUpdates(createSet(updateTypes));
         }
         return getClient(accessToken, accessTokenSecret).getUserUpdates();
     }
 
     /**
      * Gets the network updates.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1006">http://developer.linkedin.com/docs/DOC-1006</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:getUserUpdatesById}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:getUserUpdatesById}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param id                the id to search
      * @param updateTypes       the update types to retrieve
      * @param start             the start, if set count needs to be specified
      * @param count             the count, if set start needs to be specified
      * @param startDate         the start date, if set end date needs to be specified
      * @param endDate           the end date, if set end date needs to be specified
      * @return the network updates
      */
     @Processor
     public Network getUserUpdatesById(@OAuthAccessToken String accessToken,
                                       @OAuthAccessTokenSecret String accessTokenSecret,
                                       String id,
                                       @Optional List<NetworkUpdateType> updateTypes,
                                       @Optional Integer start,
                                       @Optional Integer count,
                                       @Optional Date startDate,
                                       @Optional Date endDate) {
         if (updateTypes != null && start != null && count != null && startDate != null && endDate != null) {
             return getClient(accessToken, accessTokenSecret).getUserUpdates(id, createSet(updateTypes), start, count, startDate, endDate);
         }
         if (updateTypes != null && start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).getUserUpdates(id, createSet(updateTypes), start, count);
         }
         if (updateTypes != null && startDate != null && endDate != null) {
             return getClient(accessToken, accessTokenSecret).getUserUpdates(id, createSet(updateTypes), startDate, endDate);
         }
         if (start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).getUserUpdates(id, start, count);
         }
         if (startDate != null && endDate != null) {
             return getClient(accessToken, accessTokenSecret).getUserUpdates(id, startDate, endDate);
         }
         if (updateTypes != null) {
             return getClient(accessToken, accessTokenSecret).getUserUpdates(id, createSet(updateTypes));
         }
         return getClient(accessToken, accessTokenSecret).getUserUpdates(id);
     }
 
     /**
      * Gets the network update comments.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1043">http://developer.linkedin.com/docs/DOC-1043</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:getNetworkUpdateComments}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:getNetworkUpdateComments}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param networkUpdateId   the network update id to search
      * @return the network update comments
      */
     @Processor
     public UpdateComments getNetworkUpdateComments(@OAuthAccessToken String accessToken,
                                                    @OAuthAccessTokenSecret String accessTokenSecret,
                                                    String networkUpdateId) {
         return getClient(accessToken, accessTokenSecret).getNetworkUpdateComments(networkUpdateId);
     }
 
     /**
      * Gets the network update likes.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1043">http://developer.linkedin.com/docs/DOC-1043</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:getNetworkUpdateLikes}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:getNetworkUpdateLikes}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param networkUpdateId   the network update id to search
      * @return the network update likes
      */
     @Processor
     public Likes getNetworkUpdateLikes(@OAuthAccessToken String accessToken,
                                        @OAuthAccessTokenSecret String accessTokenSecret,
                                        String networkUpdateId) {
         return getClient(accessToken, accessTokenSecret).getNetworkUpdateLikes(networkUpdateId);
     }
 
     /**
      * Gets the connections for current user.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1004">http://developer.linkedin.com/docs/DOC-1004</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:getConnectionsForCurrentUser}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:getConnectionsForCurrentUser}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param profileFields     the profile fields to retrieve
      * @param start             the start, if set count needs to be specified
      * @param count             the count, if set start needs to be specified
      * @param modificationDate  the modification date, if set modification type needs to be specified
      * @param modificationType  the modification type, if set modification date needs to be specified
      * @return the connections for current user
      */
     @Processor
     public Connections getConnectionsForCurrentUser(@OAuthAccessToken String accessToken,
                                                     @OAuthAccessTokenSecret String accessTokenSecret,
                                                     @Optional List<ProfileField> profileFields,
                                                     @Optional Integer start,
                                                     @Optional Integer count,
                                                     @Optional Date modificationDate,
                                                     @Optional ConnectionModificationType modificationType) {
         if (profileFields != null && start != null && count != null && modificationDate != null && modificationType != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsForCurrentUser(createSet(profileFields), start, count, modificationDate, modificationType);
         }
         if (profileFields != null && modificationDate != null && modificationType != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsForCurrentUser(createSet(profileFields), modificationDate, modificationType);
         }
         if (profileFields != null && start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsForCurrentUser(createSet(profileFields), start, count);
         }
         if (start != null && count != null && modificationDate != null && modificationType != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsForCurrentUser(start, count, modificationDate, modificationType);
         }
         if (start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsForCurrentUser(start, count);
         }
         if (modificationDate != null && modificationType != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsForCurrentUser(modificationDate, modificationType);
         }
         if (profileFields != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsForCurrentUser(createSet(profileFields));
         }
         return getClient(accessToken, accessTokenSecret).getConnectionsForCurrentUser();
     }
 
     /**
      * Gets the connections by id.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1004">http://developer.linkedin.com/docs/DOC-1004</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:getConnectionsById}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:getConnectionsById}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param id                the id to search
      * @param profileFields     the profile fields to retrieve
      * @param start             the start, if set count needs to be specified
      * @param count             the count, if set start needs to be specified
      * @param modificationDate  the modification date, if set modification type needs to be specified
      * @param modificationType  the modification type, if set modification date needs to be specified
      * @return the connections by id
      */
     @Processor
     public Connections getConnectionsById(@OAuthAccessToken String accessToken,
                                           @OAuthAccessTokenSecret String accessTokenSecret,
                                           String id,
                                           @Optional List<ProfileField> profileFields,
                                           @Optional Integer start,
                                           @Optional Integer count,
                                           @Optional Date modificationDate,
                                           @Optional ConnectionModificationType modificationType) {
         if (profileFields != null && start != null && count != null && modificationDate != null && modificationType != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsById(id, createSet(profileFields), start, count, modificationDate, modificationType);
         }
         if (profileFields != null && modificationDate != null && modificationType != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsById(id, createSet(profileFields), modificationDate, modificationType);
         }
         if (profileFields != null && start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsById(id, createSet(profileFields), start, count);
         }
         if (start != null && count != null && modificationDate != null && modificationType != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsById(id, start, count, modificationDate, modificationType);
         }
         if (modificationDate != null && modificationType != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsById(id, modificationDate, modificationType);
         }
         if (start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsById(id, start, count);
         }
         if (profileFields != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsById(id, createSet(profileFields));
         }
         return getClient(accessToken, accessTokenSecret).getConnectionsById(id);
     }
 
     /**
      * Gets the connections by url.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1004">http://developer.linkedin.com/docs/DOC-1004</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:getConnectionsByUrl}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:getConnectionsByUrl}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param url               the url to search
      * @param profileFields     the profile fields to retrieve
      * @param start             the start, if set count needs to be specified
      * @param count             the count, if set start needs to be specified
      * @param modificationDate  the modification date, if set modification type needs to be specified
      * @param modificationType  the modification type, if set modification date needs to be specified
      * @return the connections by url
      */
     @Processor
     public Connections getConnectionsByUrl(@OAuthAccessToken String accessToken,
                                            @OAuthAccessTokenSecret String accessTokenSecret,
                                            String url,
                                            @Optional List<ProfileField> profileFields,
                                            @Optional Integer start,
                                            @Optional Integer count,
                                            @Optional Date modificationDate,
                                            @Optional ConnectionModificationType modificationType) {
         if (profileFields != null && start != null && count != null && modificationDate != null && modificationType != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsByUrl(url, createSet(profileFields), start, count, modificationDate, modificationType);
         }
         if (profileFields != null && modificationDate != null && modificationType != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsByUrl(url, createSet(profileFields), modificationDate, modificationType);
         }
         if (profileFields != null && start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsByUrl(url, createSet(profileFields), start, count);
         }
         if (start != null && count != null && modificationDate != null && modificationType != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsByUrl(url, start, count, modificationDate, modificationType);
         }
         if (modificationDate != null && modificationType != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsByUrl(url, modificationDate, modificationType);
         }
         if (start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsByUrl(url, start, count);
         }
         if (profileFields != null) {
             return getClient(accessToken, accessTokenSecret).getConnectionsByUrl(url, createSet(profileFields));
         }
         return getClient(accessToken, accessTokenSecret).getConnectionsByUrl(url);
     }
 
     /**
      * Search people.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1005">http://developer.linkedin.com/docs/DOC-1005</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:searchPeople}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:searchPeople}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param searchParameters  the search parameters to use
      * @param profileFields     the profile fields to retriee
      * @param start             the start, if set count needs to be specified
      * @param count             the count, if set start needs to be specified
      * @param sortOrder         the sort order to use, defaults to RELEVANCE
      * @return the people
      */
     @Processor
     public People searchPeople(@OAuthAccessToken String accessToken,
                                @OAuthAccessTokenSecret String accessTokenSecret,
                               @Optional Map<SearchParameter, String> searchParameters,
                               @Optional List<ProfileField> profileFields,
                                @Optional Integer start,
                                @Optional Integer count,
                                @Optional @Default(value = "RELEVANCE") SearchSortOrder sortOrder) {
         if (searchParameters != null && profileFields != null && start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).searchPeople(searchParameters, createSet(profileFields), start, count, sortOrder);
         }
         if (searchParameters != null && profileFields != null) {
             return getClient(accessToken, accessTokenSecret).searchPeople(searchParameters, createSet(profileFields), sortOrder);
         }
         if (searchParameters != null && start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).searchPeople(searchParameters, start, count, sortOrder);
         }
         if (searchParameters != null) {
             return getClient(accessToken, accessTokenSecret).searchPeople(searchParameters, sortOrder);
         }
         return getClient(accessToken, accessTokenSecret).searchPeople();
     }
 
     /**
      * Search people.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1005">http://developer.linkedin.com/docs/DOC-1005</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:searchPeopleWithFacets}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:searchPeopleWithFacets}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param searchParameters  the search parameters
      * @param profileFields     the profile fields to retrieve
      * @param start             the start, if set count needs to be specified
      * @param count             the count, if set start needs to be specified
      * @param sortOrder         the sort order to use, defaults to RELEVANCE
      * @param facets            the facet type and a comma separated string with all the values
      * @return the people
      */
 
     @Processor
     public People searchPeopleWithFacets(@OAuthAccessToken String accessToken,
                                          @OAuthAccessTokenSecret String accessTokenSecret,
                                         Map<SearchParameter, String> searchParameters,
                                         @Optional List<ProfileField> profileFields,
                                          @Optional Integer start,
                                          @Optional Integer count,
                                          @Optional @Default(value = "RELEVANCE") SearchSortOrder sortOrder,
                                          Map<FacetType, String> facets) {
         if (profileFields != null && start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).searchPeople(searchParameters, createSet(profileFields), start, count, sortOrder, adapt(facets));
         }
         if (profileFields != null) {
             return getClient(accessToken, accessTokenSecret).searchPeople(searchParameters, createSet(profileFields), sortOrder, adapt(facets));
         }
         if (start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).searchPeople(searchParameters, start, count, sortOrder, adapt(facets));
         }
         return getClient(accessToken, accessTokenSecret).searchPeople(searchParameters, sortOrder, adapt(facets));
     }
 
     /**
      * Search people.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1005">http://developer.linkedin.com/docs/DOC-1005</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:searchPeopleWithFacetFields}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:searchPeopleWithFacetFields}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param searchParameters  the search parameters
      * @param profileFields     the profile fields to retrieve
      * @param facetFields       the facet fields to use
      * @param start             the start, if set count needs to be specified
      * @param count             the count, if set start needs to be specified
      * @param sortOrder         the sort order, defaults to RELEVANCE
      * @param facets            the facets to use
      * @return the people
      */
 
     @Processor
     public PeopleSearch searchPeopleWithFacetFields(@OAuthAccessToken String accessToken,
                                                     @OAuthAccessTokenSecret String accessTokenSecret,
                                                    Map<SearchParameter, String> searchParameters,
                                                    List<ProfileField> profileFields,
                                                     List<FacetField> facetFields,
                                                     @Optional Integer start,
                                                     @Optional Integer count,
                                                     @Optional @Default(value = "RELEVANCE") SearchSortOrder sortOrder,
                                                     @Optional Map<FacetType, String> facets) {
         if (start != null && count != null && facets != null) {
             return getClient(accessToken, accessTokenSecret).searchPeople(searchParameters, createSet(profileFields), createSet(facetFields), start, count, sortOrder, adapt(facets));
         }
         if (start != null && count != null) {
             return getClient(accessToken, accessTokenSecret).searchPeople(searchParameters, createSet(profileFields), createSet(facetFields), start, count, sortOrder);
         }
         if (facets != null) {
             return getClient(accessToken, accessTokenSecret).searchPeople(searchParameters, createSet(profileFields), createSet(facetFields), sortOrder, adapt(facets));
         }
         return getClient(accessToken, accessTokenSecret).searchPeople(searchParameters, createSet(profileFields), createSet(facetFields), sortOrder);
     }
 
     /**
      * Post network update.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1009">http://developer.linkedin.com/docs/DOC-1009</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:postNetworkUpdate}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:postNetworkUpdate}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param updateText        the update text
      */
     @Processor
     public void postNetworkUpdate(@OAuthAccessToken String accessToken,
                                   @OAuthAccessTokenSecret String accessTokenSecret,
                                   String updateText) {
         getClient(accessToken, accessTokenSecret).postNetworkUpdate(updateText);
     }
 
     /**
      * Post comment.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1043">http://developer.linkedin.com/docs/DOC-1043</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:postComment}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:postComment}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param networkUpdateId   the network update id
      * @param commentText       the comment text
      */
     @Processor
     public void postComment(@OAuthAccessToken String accessToken,
                             @OAuthAccessTokenSecret String accessTokenSecret,
                             String networkUpdateId,
                             String commentText) {
         getClient(accessToken, accessTokenSecret).postComment(networkUpdateId, commentText);
     }
 
     /**
      * Like post.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1043">http://developer.linkedin.com/docs/DOC-1043</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:likePost}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:likePost}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param networkUpdateId   the network update id
      */
     @Processor
     public void likePost(@OAuthAccessToken String accessToken,
                          @OAuthAccessTokenSecret String accessTokenSecret,
                          String networkUpdateId) {
         getClient(accessToken, accessTokenSecret).likePost(networkUpdateId);
     }
 
     /**
      * Unlike post.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1043">http://developer.linkedin.com/docs/DOC-1043</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:unlikePost}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:unlikePost}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param networkUpdateId   the network update id
      */
     @Processor
     public void unlikePost(@OAuthAccessToken String accessToken,
                            @OAuthAccessTokenSecret String accessTokenSecret,
                            String networkUpdateId) {
         getClient(accessToken, accessTokenSecret).unlikePost(networkUpdateId);
     }
 
     /**
      * Update current status.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1007">http://developer.linkedin.com/docs/DOC-1007</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:updateCurrentStatus}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:updateCurrentStatus}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param status            the status
      * @param postToTwitter     whether to post the update to Twitter
      */
     @Processor
     public void updateCurrentStatus(@OAuthAccessToken String accessToken,
                                     @OAuthAccessTokenSecret String accessTokenSecret,
                                     String status,
                                     @Optional @Default("false") Boolean postToTwitter) {
         getClient(accessToken, accessTokenSecret).updateCurrentStatus(status, postToTwitter);
     }
 
     /**
      * Delete current status.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1007">http://developer.linkedin.com/docs/DOC-1007</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:deleteCurrentStatus}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:deleteCurrentStatus}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      */
     @Processor
     public void deleteCurrentStatus(@OAuthAccessToken String accessToken,
                                     @OAuthAccessTokenSecret String accessTokenSecret) {
         getClient(accessToken, accessTokenSecret).deleteCurrentStatus();
     }
 
     /**
      * Send message.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1044">http://developer.linkedin.com/docs/DOC-1044</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:sendMessage}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:sendMessage}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param recepientIds      the recepient ids
      * @param subject           the subject
      * @param message           the message
      */
     @Processor
     public void sendMessage(@OAuthAccessToken String accessToken,
                             @OAuthAccessTokenSecret String accessTokenSecret,
                             List<String> recepientIds,
                             String subject,
                             String message) {
         getClient(accessToken, accessTokenSecret).sendMessage(recepientIds, subject, message);
     }
 
     /**
      * Send invite.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1012">http://developer.linkedin.com/docs/DOC-1012</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:sendInviteByEmail}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:sendInviteByEmail}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param email             the email
      * @param firstName         the first name
      * @param lastName          the last name
      * @param subject           the subject
      * @param message           the message
      */
     @Processor
     public void sendInviteByEmail(@OAuthAccessToken String accessToken,
                                   @OAuthAccessTokenSecret String accessTokenSecret,
                                   String email,
                                   String firstName,
                                   String lastName,
                                   String subject,
                                   String message) {
         getClient(accessToken, accessTokenSecret).sendInviteByEmail(email, firstName, lastName, subject, message);
     }
 
     /**
      * Post share.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1212">http://developer.linkedin.com/docs/DOC-1212</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:postShare}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:postShare}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param commentText       the comment text
      * @param title             the title
      * @param url               the url
      * @param imageUrl          the image url
      * @param visibility        the visibility
      * @param postToTwitter     whether to post to twitter
      */
     @Processor
     public void postShare(@OAuthAccessToken String accessToken,
                           @OAuthAccessTokenSecret String accessTokenSecret,
                           String commentText,
                           String title,
                           String url,
                           String imageUrl,
                           VisibilityType visibility,
                           @Optional @Default("false") Boolean postToTwitter) {
         getClient(accessToken, accessTokenSecret).postShare(commentText, title, url, imageUrl, visibility, postToTwitter);
     }
 
     /**
      * Re-share.
      * For details see <a href="http://developer.linkedin.com/docs/DOC-1212">http://developer.linkedin.com/docs/DOC-1212</a>
      * <p/>
      * {@sample.xml ../../../doc/linkedin-connector.xml.sample linkedin:reShare}
      * {@sample.java ../../../doc/linkedin-connector.java.sample linkedin:reShare}
      *
      * @param accessToken       the access token to used as described by the OAuth 1.0a specification.
      * @param accessTokenSecret the access token secret to used as described by the OAuth 1.0a specification.
      * @param shareId           the share id
      * @param commentText       the comment text
      * @param visibility        the visibility
      */
     @Processor
 
     public void reShare(@OAuthAccessToken String accessToken,
                         @OAuthAccessTokenSecret String accessTokenSecret,
                         String shareId,
                         String commentText,
                         VisibilityType visibility) {
         getClient(accessToken, accessTokenSecret).reShare(shareId, commentText, visibility);
     }
 
     public void setApiKey(String apiKey) {
         this.apiKey = apiKey;
     }
 
     public void setApiSecret(String apiSecret) {
         this.apiSecret = apiSecret;
     }
 
     private <T> Set<T> createSet(List<T> list) {
         Set<T> set = new HashSet<T>(list.size());
         set.addAll(list);
         return set;
     }
 
     private List<Parameter<FacetType, String>> adapt(Map<FacetType, String> facets) {
         List<Parameter<FacetType, String>> facetList = new ArrayList<Parameter<FacetType, String>>();
         for (Map.Entry<FacetType, String> entry : facets.entrySet()) {
             String facetValuesCommaSeparated = entry.getValue();
             for (String facetValue : facetValuesCommaSeparated.split(",")) {
                 facetList.add(new Parameter<FacetType, String>(entry.getKey(), facetValue));
             }
         }
         return facetList;
     }
 
     private synchronized LinkedInApiClient getClient(String accesToken, String accessTokenSecret) {
         if (client == null) {
             client = LinkedInClientFactory.getClient(apiKey, apiSecret, accesToken, accessTokenSecret);
         }
         return client;
     }
 
     public String getApiKey() {
         return apiKey;
     }
 
     public String getApiSecret() {
         return apiSecret;
     }
 }
