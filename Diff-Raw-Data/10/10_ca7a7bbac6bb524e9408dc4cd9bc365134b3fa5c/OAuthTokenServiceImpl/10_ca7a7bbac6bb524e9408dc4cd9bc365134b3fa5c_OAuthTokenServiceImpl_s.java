 package com.github.nmorel.homework.api.services.impl;
 
 import java.io.InputStreamReader;
 import java.util.concurrent.TimeUnit;
 
 import javax.annotation.Nullable;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.nmorel.homework.api.config.Config;
 import com.github.nmorel.homework.api.model.AccessToken;
 import com.github.nmorel.homework.api.providers.UserIdProvider;
 import com.github.nmorel.homework.api.services.OAuthTokenService;
 import com.google.api.client.http.EmptyContent;
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpResponse;
 import com.google.api.client.http.HttpTransport;
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import com.google.gson.Gson;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 @Singleton
 public class OAuthTokenServiceImpl
     implements OAuthTokenService
 {
 
     private static final Logger logger = LoggerFactory.getLogger( OAuthTokenServiceImpl.class );
 
     // Saving tokens in memory. Not the best solution but the easiest one. For a more robust solution, look to add a
     // database.
     private Cache<String, String> tokenCache = CacheBuilder.newBuilder().expireAfterWrite( 1, TimeUnit.DAYS ).build();
 
     @Inject
     private UserIdProvider userIdProvider;
 
     @Inject
     private Config config;
 
     @Inject
     private HttpTransport httpTransport;
 
     @Override
     public void retrieveAndStoreToken( String code )
     {
         Preconditions.checkNotNull( code, "code can't be null" );
 
         Optional<String> userId = userIdProvider.get();
         if ( !userId.isPresent() )
         {
             logger.warn( "We asked to retrieve a token but the userId is null" );
             // we can't associate the token to a user, no need to pursue
             return;
         }
 
         logger.debug( "Retrieving token from github with code {} and user {}", code, userId.get() );
 
         GenericUrl url = new GenericUrl( config.getGithubTokenUrl() );
         url.set( "client_id", config.getGithubClientId() );
         url.set( "client_secret", config.getGithubClientSecret() );
         url.set( "code", code );
 
         String token;
         try
         {
             HttpRequest request = httpTransport.createRequestFactory().buildPostRequest( url, new EmptyContent() );
             request.getHeaders().setAccept( "application/json" );
             HttpResponse response = request.execute();
             AccessToken accessToken =
                 new Gson().fromJson( new InputStreamReader( response.getContent() ), AccessToken.class );
             token = accessToken.getAccessToken();
         }
         catch ( Exception e )
         {
             logger.error( "Error while retrieving the token from github", e );
             // TODO what to do if there is an exception ?
             return;
         }
 
         tokenCache.put( userId.get(), token );
 
         logger.debug( "Token associated to user {}", userId.get() );
     }
 
     @Override
     public Optional<String> getToken()
     {
         return getToken( userIdProvider.get().orNull() );
     }
 
     @Override
     public Optional<String> getToken( @Nullable String userId )
     {
         if ( null == userId )
         {
             return Optional.absent();
         }
        logger.debug( "Looking token for user {}", userId );
         String token = tokenCache.getIfPresent( userId );
         logger.trace( "Token : {}", token );
         return Optional.fromNullable( token );
     }
 
     @Override
     public void deleteToken( @Nullable String userId )
     {
         if ( null != userId )
         {
             logger.debug( "Deleting token if it exists for user {}", userId );
             tokenCache.invalidate( userId );
         }
     }
 }
