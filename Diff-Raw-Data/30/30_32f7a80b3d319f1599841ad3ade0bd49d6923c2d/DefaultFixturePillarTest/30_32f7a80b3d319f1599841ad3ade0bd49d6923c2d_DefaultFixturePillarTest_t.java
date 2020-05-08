 /*
  * #%L
  * Bitrepository Protocol
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.pillar;
 
 import org.bitrepository.clienttest.MessageReceiver;
 import org.bitrepository.common.settings.SettingsProvider;
 import org.bitrepository.common.settings.XMLFileSettingsLoader;
 import org.bitrepository.protocol.IntegrationTest;
 import org.bitrepository.protocol.TestMessageFactory;
 import org.testng.annotations.AfterClass;
 
 /**
  * Contains the generic parts for pillar tests integrating to the message bus. 
  * Mostly copied from DefaultFixtureClientTest...
  */
 public abstract class DefaultFixturePillarTest extends IntegrationTest {
     protected static final String DEFAULT_FILE_ID = TestMessageFactory.FILE_ID_DEFAULT;
 
     protected static String pillarDestinationId;
     protected MessageReceiver pillarTopic;
     
     protected static String clientDestinationId;
     protected MessageReceiver clientTopic;
 
     /** Indicated whether reference pillars should be started should be started and used. Note that mockup pillars 
      * should be used in this case, e.g. the useMockupPillar() call should return false. */ 
     public boolean useEmbeddedReferencePillars() {
         return System.getProperty("useEmbeddedReferencePillars", "false").equals("true");
     }
 
     /**
      * Removes references to the message bus, such as listeners.
      * @throws Exception 
      */
     @AfterClass (alwaysRun = true)
     public void teardownTest() throws Exception {
         teardownMessageBus(); 
         teardownHttpServer();
     }
 
     @Override
     protected void setupMessageBus() throws Exception {
         super.setupMessageBus();
         pillarTopic = new MessageReceiver("pillar topic receiver", testEventManager);
         clientTopic = new MessageReceiver("client topic receiver", testEventManager);
     }
 
     @Override
     protected void defineDestinations() {
         super.defineDestinations();
         pillarDestinationId = "pillar_topic" + getTopicPostfix();
         clientDestinationId = "client" + getTopicPostfix();
         messageBus.addListener(pillarDestinationId, pillarTopic.getMessageListener());    
         messageBus.addListener(bitRepositoryCollectionDestinationID, bitRepositoryCollectionDestination.getMessageListener());
         messageBus.addListener(clientDestinationId, clientTopic.getMessageListener());    
     }
 
     @Override
     protected void setupSettings() {
         super.setupSettings();
     }
 
     @Override
     protected void teardownMessageBus() {
         messageBus.removeListener(pillarDestinationId, pillarTopic.getMessageListener());
 
         super.teardownMessageBus();
     }
     
     @Override
     public void initCollectionSettings() {
         if(settings == null) {
             try {
                 SettingsProvider settingsLoader = new SettingsProvider(
                        new XMLFileSettingsLoader("settings/xml"));
                 settings = settingsLoader.getSettings("bitrepository-devel");
             } catch(Exception e) {
                 throw new RuntimeException("Could not load settings.", e);
             }
         }
     }
 }
