 package com.yrek.rideapp.config;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.ec2.AmazonEC2;
 import com.amazonaws.services.ec2.AmazonEC2Client;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.google.inject.servlet.ServletModule;
 
 import com.sun.jersey.api.container.filter.LoggingFilter;
 import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
 
 import org.codehaus.jackson.map.DeserializationConfig;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 
 import com.yrek.rideapp.facebook.FacebookClient;
 import com.yrek.rideapp.facebook.FacebookOAuth2Client;
 import com.yrek.rideapp.facebook.FacebookOAuth2Session;
 import com.yrek.rideapp.oauth2.OAuth2Client;
 import com.yrek.rideapp.oauth2.OAuth2Session;
 import com.yrek.rideapp.oauth2.OAuth2.OAuth2Filter;
 import com.yrek.rideapp.oauth2.OAuth2.OAuth2Servlet;
 import com.yrek.rideapp.rest.RESTAuthFilter;
 import com.yrek.rideapp.rest.RESTAPI;
 import com.yrek.rideapp.servlet.PingServlet;
 import com.yrek.rideapp.servlet.SetAttributesFilter;
 import com.yrek.rideapp.servlet.UploadServlet;
 import com.yrek.rideapp.servlet.UserServlet;
 import com.yrek.rideapp.storage.EC2MemcachedStorage;
 import com.yrek.rideapp.storage.FileStorage;
 import com.yrek.rideapp.storage.MemcachedStorage;
 import com.yrek.rideapp.storage.Storage;
 import com.yrek.rideapp.storage.S3Storage;
 
 abstract class BaseModule extends ServletModule {
     private static final Logger LOG = Logger.getLogger(BaseModule.class.getName());
 
     private ArrayList<Closeable> closeables = new ArrayList<Closeable>();
     private Properties properties;
 
     BaseModule(Properties properties) {
         this.properties = properties;
     }
 
     protected abstract void defineBindings();
 
     public void close() {
         for (Closeable closeable : closeables)
             try {
                 closeable.close();
             } catch (Exception e) {
                 LOG.log(Level.SEVERE,"",e);
             }
     }
 
     @Override
     protected void configureServlets() {
         defineBindings();
         bind(RESTAPI.class);
 
         serve("/oauth2").with(OAuth2Servlet.class);
         // Stupid Guice bugs 455/522
         // serve("/user/*").with(UserServlet.class); 
         filter("/index.jsp","/refreshSession.jsp").through(OAuth2Filter.class);
         filter("/index.jsp","/refreshSession.jsp").through(SetAttributesFilter.class);
         filter("/rest/*").through(RESTAuthFilter.class);
         serve("/ping").with(PingServlet.class);
         serve("/rest/upload").with(UploadServlet.class);
         serve("/rest/*").with(GuiceContainer.class, jerseyParams());
     }
 
     protected HashMap<String,String> jerseyParams() {
         HashMap<String,String> properties = new HashMap<String,String>();
         properties.put("com.sun.jersey.spi.container.ContainerRequestFilters", LoggingFilter.class.getName());
         properties.put("com.sun.jersey.spi.container.ContainerResponseFilters", LoggingFilter.class.getName());
         return properties;
     }
 
     private <T extends Closeable> T provideCloseable(T closeable) {
         closeables.add(0, closeable);
         return closeable;
     }
 
     @Provides
     FacebookOAuth2Client provideFacebookOAuth2Client() {
         return provideCloseable(new FacebookOAuth2Client(properties.getProperty("facebook.clientID"), properties.getProperty("facebook.clientSecret"), properties.getProperty("facebook.canvasURL")));
     }
 
     @Provides
     FacebookClient provideFacebookClient(OAuth2Session oAuth2Session) {
         return provideCloseable(new FacebookClient(oAuth2Session));
     }
 
     @Provides @Singleton
     SetAttributesFilter provideSetAttributesFilter(FacebookClient facebookClient) {
         return new SetAttributesFilter(facebookClient, properties.getProperty("garmin.garminUnlock"), properties.getProperty("facebook.clientID"));
     }
 
     @Provides @Singleton
     AWSCredentials provideAWSCredentials() {
         return new BasicAWSCredentials(properties.getProperty("aws.accessKey"), properties.getProperty("aws.secretKey"));
     }
 
     @Provides @Singleton
     AmazonS3 provideAmazonS3(AWSCredentials credentials) {
         final AmazonS3Client amazonS3 = new AmazonS3Client(credentials);
         provideCloseable(new Closeable() {
             @Override
             public void close() {
                 amazonS3.shutdown();
             }
         });
         return amazonS3;
     }
 
     @Provides @Singleton
     AmazonEC2 provideAmazonEC2(AWSCredentials credentials) {
         final AmazonEC2Client amazonEC2 = new AmazonEC2Client(credentials);
         provideCloseable(new Closeable() {
             @Override
             public void close() {
                 amazonEC2.shutdown();
             }
         });
         return amazonEC2;
     }
 
     @Provides @Singleton
     S3Storage provideS3Storage(AmazonS3 amazonS3) {
         return new S3Storage(amazonS3, properties.getProperty("aws.s3.bucketName"), properties.getProperty("aws.s3.prefix"));
     }
 
     @Provides @Singleton
     MemcachedStorage provideMemcachedStorage(S3Storage s3Storage) throws IOException {
         ArrayList<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();
         for (String host : properties.getProperty("memcached.pool").split(","))
             addresses.add(new InetSocketAddress(host, Integer.parseInt(properties.getProperty("memcached.port"))));
         return provideCloseable(new MemcachedStorage(s3Storage, "~", 864000, addresses));
     }
 
     @Provides @Singleton
     EC2MemcachedStorage provideEC2MemcachedStorage(S3Storage s3Storage, AmazonEC2 amazonEC2) throws IOException {
        return provideCloseable(new EC2MemcachedStorage(s3Storage, "~", 864000, amazonEC2, properties.getProperty("memcached.ec2-pool.securityGroup"), Integer.parseInt(properties.getProperty("memcached.ec2-pool.port")), Integer.parseInt(properties.getProperty("memcached.ec2-pool.pollIntervalMinutes"))));
     }
 
     @Provides @Singleton
     FileStorage provideFileStorage() {
         return new FileStorage(new File(properties.getProperty("storage.dir")));
     }
 
     @Provides @Singleton
     ObjectMapper provideObjectMapper() {
         ObjectMapper objectMapper = new ObjectMapper();
         objectMapper.getDeserializationConfig().disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
         objectMapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
         return objectMapper;
     }
 }
