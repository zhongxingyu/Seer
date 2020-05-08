 /**
  * Copyright 2012 Alex Jones
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  *
  * @author unclealex72
  *
  */
 
 package uk.co.unclealex.music.command.inject;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 
 import uk.co.unclealex.music.Validator;
 import uk.co.unclealex.music.action.ActionExecutor;
 import uk.co.unclealex.music.action.ActionExecutorImpl;
 import uk.co.unclealex.music.command.checkin.covers.AmazonArtworkSearchingService;
 import uk.co.unclealex.music.command.checkin.covers.ArtworkSearchingService;
 import uk.co.unclealex.music.command.checkin.covers.SignedRequestsService;
 import uk.co.unclealex.music.command.checkin.covers.SignedRequestsServiceImpl;
 import uk.co.unclealex.music.command.checkin.process.MappingService;
 import uk.co.unclealex.music.command.checkin.process.MappingServiceImpl;
 import uk.co.unclealex.music.command.checkout.EncodingService;
 import uk.co.unclealex.music.command.checkout.LameEncodingService;
 import uk.co.unclealex.music.configuration.Configuration;
 import uk.co.unclealex.music.configuration.json.JsonConfigurationFactory;
 import uk.co.unclealex.music.files.DirectoryService;
 import uk.co.unclealex.music.files.DirectoryServiceImpl;
 import uk.co.unclealex.music.musicbrainz.MusicBrainzBaseResource;
 import uk.co.unclealex.music.musicbrainz.MusicBrainzClient;
 import uk.co.unclealex.music.musicbrainz.MusicBrainzClientImpl;
 import uk.co.unclealex.music.musicbrainz.MusicBrainzRetryFilter;
 import uk.co.unclealex.music.musicbrainz.MusicBrainzThrottleDelay;
 import uk.co.unclealex.music.musicbrainz.MusicBrainzThrottleRetries;
 import uk.co.unclealex.music.musicbrainz.MusicBrainzWebResourceFactory;
 import uk.co.unclealex.music.musicbrainz.MusicBrainzWebResourceFactoryImpl;
 import uk.co.unclealex.music.musicbrainz.OwnerService;
 import uk.co.unclealex.music.musicbrainz.OwnerServiceImpl;
 
 import com.google.inject.AbstractModule;
 
 /**
  * A module that contains all bindings that rely on either external
  * configuration or non-java code.
  * 
  * @author alex
  * 
  */
 public class ExternalModule extends AbstractModule {
 
   /**
    * {@inheritDoc}
    */
   @Override
   protected void configure() {
     // Configuration
     bindConfiguration();
     // LAME Encoding.
     bind(EncodingService.class).to(LameEncodingService.class);
     // Amazon artwork
     bind(SignedRequestsService.class).to(SignedRequestsServiceImpl.class);
     bind(ArtworkSearchingService.class).to(AmazonArtworkSearchingService.class);
     // MusicBrainz ownership
     bind(MusicBrainzWebResourceFactory.class).to(MusicBrainzWebResourceFactoryImpl.class);
     bind(MusicBrainzRetryFilter.class);
     bind(MusicBrainzClient.class).to(MusicBrainzClientImpl.class);
     bind(OwnerService.class).to(OwnerServiceImpl.class);
     bindConstant().annotatedWith(MusicBrainzThrottleDelay.class).to(1000l);
     bindConstant().annotatedWith(MusicBrainzThrottleRetries.class).to(5);
     bindConstant().annotatedWith(MusicBrainzBaseResource.class).to("http://musicbrainz.org/ws/2/");
     // The action executor
     bind(ActionExecutor.class).to(ActionExecutorImpl.class);
     // Directories and files.
     bind(DirectoryService.class).to(DirectoryServiceImpl.class);
     bind(MappingService.class).to(MappingServiceImpl.class);
   }
 
   /**
    * The provider for binding the configuration.
    * 
    * @author alex
    * 
    */
   static class ConfigurationProvider implements Provider<Configuration> {
 
     /**
      * The {@link Validator} used to validate the JSON configuration.
      */
     private final Validator validator;
 
     /**
      * Instantiates a new configuration provider.
      * 
      * @param validator
      *          the validator
      */
     @Inject
     public ConfigurationProvider(Validator validator) {
       super();
       this.validator = validator;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Configuration get() {
       Path configurationPath = Paths.get(System.getProperty("user.home"), ".flacman.json");
       try (InputStream in = Files.newInputStream(configurationPath)) {
         Configuration configuration = new JsonConfigurationFactory().load(in);
         return getValidator().validate(configuration, "The configuration is invalid");
       }
       catch (IOException e) {
         throw new IllegalStateException("Could not read configuration file ", e);
       }
     }
 
     /**
      * Gets the {@link Validator} used to validate the JSON configuration.
      * 
      * @return the {@link Validator} used to validate the JSON configuration
      */
     public Validator getValidator() {
       return validator;
     }
   }
 
   /**
    * Create a {@link Configuration} to be used to a validated configuration
    * found in <code>$HOME/.flacman.json</code>. This method should be overriden
    * for testing.
    */
   protected void bindConfiguration() {
     bind(Configuration.class).toProvider(ConfigurationProvider.class);
   }
 
 }
