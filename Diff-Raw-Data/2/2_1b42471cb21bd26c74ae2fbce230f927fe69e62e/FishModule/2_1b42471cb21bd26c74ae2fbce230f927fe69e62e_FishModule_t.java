 /*
  * Copyright 2013 http://code.google.com/p/littleware
  * 
  * The contents of this file are available subject to the terms of the
  * Lesser GNU General Public License (LGPL) Version 2.1.
  * http://www.gnu.org/licenses/lgpl-2.1.html.
  */
 package littleware.apps.fishRunner;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.google.inject.Binder;
 import com.google.inject.Module;
 import com.google.inject.Scopes;
 import com.google.inject.name.Names;
 import org.glassfish.embeddable.GlassFish;
 import org.glassfish.embeddable.GlassFishProperties;
 
 /**
  * Guice module for fishRunner package.
  */
 public class FishModule implements Module {
 
     private final String s3Key;
     private final String s3Secret;
     private final java.net.URI dbURI;
     private final int glassfishPort;
 
     /**
      * Constructor injects S3 and database credentials
      *
      * @param s3Key
      * @param s3Secret
      * @param glassfishPort for http listener
      */
     public FishModule(String s3Key, String s3Secret, java.net.URI dbURI, int glassfishPort) {
         this.s3Key = s3Key;
         this.s3Secret = s3Secret;
         this.dbURI = dbURI;
         this.glassfishPort = glassfishPort;
     }
 
     @Override
     public void configure(Binder binder) {
         // setup AWS ...
         final AWSCredentials creds = new BasicAWSCredentials(s3Key, s3Secret);
         binder.bind(AWSCredentials.class).toInstance(creds);
         binder.bind(AmazonS3.class).to(AmazonS3Client.class);
         binder.bind(AmazonS3Client.class).toInstance(new AmazonS3Client(creds));
 
         binder.bind(java.net.URI.class).annotatedWith(Names.named("DATABASE_URL")).toInstance(dbURI);
         binder.bind(GlassFish.class).toProvider(FishFactory.class).in(Scopes.SINGLETON);
         binder.bind(FishFactory.class).in(Scopes.SINGLETON);
 
         final GlassFishProperties glassfishProperties = new GlassFishProperties();
        glassfishProperties.setPort("http-listener", glassfishPort );
         // glassfishProperties.setPort("https-listener", 8181);
 
         binder.bind( GlassFishProperties.class ).toInstance( glassfishProperties );
     }
 }
