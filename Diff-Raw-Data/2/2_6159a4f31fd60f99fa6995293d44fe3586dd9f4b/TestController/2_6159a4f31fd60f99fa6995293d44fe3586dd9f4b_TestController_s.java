 /**************************************************************************************
  * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
  * http://fusesource.com                                                              *
  * ---------------------------------------------------------------------------------- *
  * The software in this package is published under the terms of the AGPL license      *
  * a copy of which has been included with this distribution in the license.txt file.  *
  **************************************************************************************/
 package org.fusesource.cloudmix.testing;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fusesource.cloudmix.agent.RestGridClient;
 import org.fusesource.cloudmix.common.GridClient;
 import org.fusesource.cloudmix.common.dto.Dependency;
 import org.fusesource.cloudmix.common.dto.DependencyStatus;
 import org.fusesource.cloudmix.common.dto.FeatureDetails;
 import org.fusesource.cloudmix.common.dto.ProfileDetails;
 import org.fusesource.cloudmix.common.dto.ProfileStatus;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.UUID;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 
 /**
  * Creates a new temporary environment for a distributed test,
  * initialises the system, then runs the test and kills the enviroment
  *
  * @version $Revision: 1.1 $
  */
 public abstract class TestController {
     private static final transient Log LOG = LogFactory.getLog(TestController.class);
 
     /**
      * The name of the file which all the newly created profile IDs are written on each test run.
      * You can then clean up your test cloud by deleting all of the profiles in this file
      */
     public static final String PROFILE_ID_FILENAME = ".cloudmix.profiles";
 
     /**
      * The system property of the URL of the controller
      */
     public static final String CLOUDMIX_URL_PROPERTY = "cloudmix.url";
 
     /**
      * The default value of the {@link #CLOUDMIX_URL_PROPERTY} system property
      */
    public static final String DEFAULT_CONTROLLER_URL = "http://localhost:8080/";
 
     protected long startupTimeout = 60 * 1000;
     protected String controllerUrl = DEFAULT_CONTROLLER_URL;
 
     protected List<FeatureDetails> features = new CopyOnWriteArrayList<FeatureDetails>();
     protected GridClient gridClient;
     protected ProfileDetails profile;
     protected String profileId;
     protected boolean provisioned;
     protected boolean destroyProfileAfter = false;
 
     /**
      * Registers any features which are required for this system test
      */
     protected abstract void installFeatures();
 
     @Before
     public void checkProvisioned() throws URISyntaxException, IOException {
         if (provisioned) {
             return;
         }
 
         // lets get the default URL for cloudmix
         String controllerUrl = System.getProperty(CLOUDMIX_URL_PROPERTY, DEFAULT_CONTROLLER_URL);
         System.out.println("Using controller URL: " + controllerUrl);
 
         // lets register the features
         GridClient controller = getGridClient();
 
         if (profileId == null) {
             profileId = UUID.randomUUID().toString();
         }
 
         // lets append the profileId to the file!
         onProfileIdCreated(profileId);
         profile = new ProfileDetails(profileId);
 
         installFeatures();
 
         for (FeatureDetails feature : features) {
             // associate the feature with the profile, so that when the profile is deleted, so is the feature
             feature.setOwnedByProfileId(profileId);
 
             // lets ensure the feature ID is unique (though the code could be smart enough to deduce it!)
             String featureId = feature.getId();
             if (!featureId.startsWith(profileId)) {
                 featureId = profileId + ":" + featureId;
                 feature.setId(featureId);
             }
             profile.getFeatures().add(new Dependency(featureId));
 
             System.out.println("Adding feature: " + feature.getId());
             controller.addFeature(feature);
         }
 
         controller.addProfile(profile);
 
 
         // now lets start the remote grid
         assertProvisioned();
         provisioned = true;
 
         System.out.println("All features provisioned!!");
     }
 
     protected void onProfileIdCreated(String profileId) throws IOException {
         String fileName = PROFILE_ID_FILENAME;
         try {
             FileWriter writer = new FileWriter(fileName, true);
             writer.append(profileId);
             writer.append("\n");
             writer.close();
         } catch (IOException e) {
             LOG.error("Failed to write profileId to file: " + fileName);
         }
     }
 
     @After
     public void tearDown() throws Exception {
         if (destroyProfileAfter) {
             if (gridClient != null) {
                 if (profile != null) {
                     gridClient.removeProfile(profile);
                 }
             }
             provisioned = false;
         }
     }
 
     public GridClient getGridClient() throws URISyntaxException {
         if (gridClient == null) {
             gridClient = createGridController();
         }
         return gridClient;
     }
 
     public void setGridClient(GridClient gridClient) {
         this.gridClient = gridClient;
     }
 
     /**
      * Returns a newly created client. Factory method
      */
     protected GridClient createGridController() throws URISyntaxException {
         return new RestGridClient(controllerUrl);
     }
 
 
     /**
      * Allow a feature to be registered prior to starting the profile
      */
     protected void addFeature(FeatureDetails featureDetails) {
         features.add(featureDetails);
     }
 
 
     /**
      * Allows feature to be registered prior to starting the profile
      */
     protected void addFeatures(FeatureDetails... featureDetails) {
         for (FeatureDetails featureDetail : featureDetails) {
             addFeature(featureDetail);
         }
     }
 
 
     /**
      * Allows feature to be registered prior to starting the profile
      */
     protected void addFeatures(Iterable<FeatureDetails> featureDetails) {
         for (FeatureDetails featureDetail : featureDetails) {
             addFeature(featureDetail);
         }
     }
 
 
     /**
      * Asserts that all the requested features have been provisioned properly
      */
     protected void assertProvisioned() {
         long start = System.currentTimeMillis();
 
         Set<String> provisionedFeatures = new TreeSet<String>();
         Set<String> failedFeatures = null;
         while (true) {
             failedFeatures = new TreeSet<String>();
             long now = System.currentTimeMillis();
 
             try {
                 ProfileStatus profileStatus = getGridClient().getProfileStatus(profileId);
                 if (profileStatus != null) {
                     List<DependencyStatus> dependencyStatus = profileStatus.getFeatures();
                     for (DependencyStatus status : dependencyStatus) {
                         String featureId = status.getFeatureId();
                         if (status.isProvisioned()) {
                             if (provisionedFeatures.add(featureId)) {
                                 LOG.info("Provisioned feature: " + featureId);
                             }
                         }
                         else {
                             failedFeatures.add(featureId);
                         }
                     }
                 }
                 if (failedFeatures.isEmpty()) {
                     return;
                 }
             } catch (URISyntaxException e) {
                 LOG.warn("Failed to poll profile status: " + e, e);
             }
 
             long delta = now - start;
             if (delta > startupTimeout) {
                 Assert.fail("Provision failure. Not enough instances of features: " + failedFeatures + " after waiting " + (startupTimeout / 1000) + " seconds");
             } else {
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e) {
                     // ignore
                 }
             }
         }
     }
 
 
 }
