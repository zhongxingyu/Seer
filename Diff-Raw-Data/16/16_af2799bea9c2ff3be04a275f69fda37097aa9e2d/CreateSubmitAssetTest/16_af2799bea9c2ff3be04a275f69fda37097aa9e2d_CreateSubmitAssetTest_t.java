 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 
 package org.ebayopensource.turmeric.repository.wso2;
 
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assume.assumeTrue;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.List;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.wso2.carbon.registry.app.RemoteRegistry;
 import org.wso2.carbon.registry.core.Resource;
 import org.wso2.carbon.registry.core.exceptions.RegistryException;
 
 import org.ebayopensource.turmeric.common.v1.types.AckValue;
 import org.ebayopensource.turmeric.repository.v2.services.Artifact;
 import org.ebayopensource.turmeric.repository.v2.services.ArtifactInfo;
 import org.ebayopensource.turmeric.repository.v2.services.ArtifactValueType;
 import org.ebayopensource.turmeric.repository.v2.services.AssetInfo;
 import org.ebayopensource.turmeric.repository.v2.services.AssetKey;
 import org.ebayopensource.turmeric.repository.v2.services.AssetLifeCycleInfo;
 import org.ebayopensource.turmeric.repository.v2.services.AttributeNameValue;
 import org.ebayopensource.turmeric.repository.v2.services.BasicAssetInfo;
 import org.ebayopensource.turmeric.repository.v2.services.CreateAndSubmitAssetRequest;
 import org.ebayopensource.turmeric.repository.v2.services.CreateAndSubmitAssetResponse;
 import org.ebayopensource.turmeric.repository.v2.services.ExtendedAssetInfo;
 import org.ebayopensource.turmeric.repository.v2.services.FlattenedRelationship;
 import org.ebayopensource.turmeric.repository.v2.services.Library;
 import org.ebayopensource.turmeric.repository.v2.services.Relation;
 
 /**
  * @author mgorovoy
  * 
  */
 public class CreateSubmitAssetTest extends Wso2Base {
     // First resource path must be the primary resource created by the test
     // in order for the assumption checks to work correctly.
     private static final String[] resources = {
             "/_system/governance/services/http/www/domain/com/assets/CreateSubmitAssetTest",
             "/_system/governance/endpoints/http/www/domain/com/ep-CreateSubmitAssetTest", };
 
     private static final String dstAsset = "/_system/governance/services/http/www/domain/com/marketplace/services/RepositoryMetadataService";
    
     private static final String assetName = "CreateSubmitAssetTest";
     private static final String assetDesc = "CreateSubmitAssetTest description";
     private static final String libraryName = "http://www.domain.com/assets";
     private static final String baseUrl = "http://www.domain.com/assets/";
     private static final String stringProperty = "a test value";
     private static final Long longProperty = new Long(1234567l);
     private static final Boolean booleanProperty = Boolean.FALSE;
 
     @BeforeClass
     public static void checkRepository() {
         boolean exists = false;
         try {
             RemoteRegistry wso2 = RSProviderUtil.getRegistry();
             exists = wso2.resourceExists("/");
 
             for (String resource : resources) {
                 if (wso2.resourceExists(resource)) {
                     wso2.delete(resource);
                 }
             }
         }
         catch (Exception ex) {
         }
 
         assumeTrue(exists);
     }
 
     private CreateAndSubmitAssetResponse createAndSubmitAsset() throws Exception {
         AssetKey key = new AssetKey();
         Library lib = new Library();
         lib.setLibraryName(libraryName);
         key.setLibrary(lib);
         key.setAssetName(assetName);
 
         BasicAssetInfo basicInfo = new BasicAssetInfo();
         basicInfo.setAssetKey(key);
         basicInfo.setAssetName(assetName);
         basicInfo.setAssetDescription(assetDesc);
         basicInfo.setAssetType("Service");
 
         ExtendedAssetInfo extendedInfo = new ExtendedAssetInfo();
         List<AttributeNameValue> attrs = extendedInfo.getAttribute();
         attrs.add(RSProviderUtil.newAttribute("namespace", libraryName));
         attrs.add(RSProviderUtil.newAttribute("stringProperty", stringProperty));
         attrs.add(RSProviderUtil.newAttribute("longProperty", longProperty.longValue()));
         attrs.add(RSProviderUtil.newAttribute("booleanProperty", booleanProperty.booleanValue()));
 
         AssetLifeCycleInfo lifeCycleInfo = new AssetLifeCycleInfo();
         lifeCycleInfo.setDomainOwner("John Doe");
         lifeCycleInfo.setDomainType("Technical Owner");
 
         AssetInfo assetInfo = new AssetInfo();
         assetInfo.setBasicAssetInfo(basicInfo);
         assetInfo.setExtendedAssetInfo(extendedInfo);
         assetInfo.setAssetLifeCycleInfo(lifeCycleInfo);
 
         Artifact endpoint = new Artifact();
         endpoint.setArtifactName("ep-" + assetName);
         endpoint.setArtifactCategory("endpoint");
         endpoint.setArtifactValueType(ArtifactValueType.URL);
 
         String endpointUrl = baseUrl + assetName;
         ArtifactInfo endpointInfo = new ArtifactInfo();
         endpointInfo.setArtifact(endpoint);
         endpointInfo.setArtifactDetail(endpointUrl.getBytes("UTF-8"));
         endpointInfo.setContentType("application/vnd.wso2.endpoint");
 
         Artifact wsdl = new Artifact();
         wsdl.setArtifactName(assetName + ".wsdl");
         wsdl.setArtifactCategory("wsdl");
         wsdl.setArtifactValueType(ArtifactValueType.FILE);
 
         ArtifactInfo wsdlInfo = new ArtifactInfo();
         wsdlInfo.setArtifact(wsdl);
        wsdlInfo.setArtifactDetail(loadFile("src/main/resources/META-INF/soa/services/wsdl/CreateServiceTest/CreateServiceTest.wsdl"));
         wsdlInfo.setContentType("application/wsdl+xml");
 
         List<ArtifactInfo> artifactList = assetInfo.getArtifactInfo();
         artifactList.add(endpointInfo);
         artifactList.add(wsdlInfo);
         
         FlattenedRelationship rel = new FlattenedRelationship();
         List<Relation> relationList = rel.getRelatedAsset();
         relationList.add(
                         new Relation() {
                             {
                                 this.setSourceAsset(RSProviderUtil.completeAssetKey(
                                                 new AssetKey() {
                                                     {
                                                         this.setAssetId(resources[0]);
                                                     }
                                                 }, null, null));
                                 this.setTargetAsset(RSProviderUtil.completeAssetKey(
                                                 new AssetKey() {
                                                     {
                                                         this.setAssetId(dstAsset);
                                                     }
                                                 }, null, null));
                                 this.setAssetRelationship("DependsOn");
                             }
                         });
         assetInfo.setFlattenedRelationship(rel);
         
         CreateAndSubmitAssetRequest request = new CreateAndSubmitAssetRequest();
         request.setAssetInfo(assetInfo);
 
         RepositoryServiceProviderImpl provider = new RepositoryServiceProviderImpl();
         return provider.createAndSubmitAsset(request);
     }
 
     @Test
     public void createTest() throws Exception {
         boolean clean = false;
         try {
             RemoteRegistry wso2 = RSProviderUtil.getRegistry();
             clean = !wso2.resourceExists(resources[0]);
         }
         catch (RegistryException e) {
         }
         assumeTrue(clean);
 
         CreateAndSubmitAssetResponse response = createAndSubmitAsset();
 
         assertEquals(AckValue.SUCCESS, response.getAck());
         assertEquals(null, response.getErrorMessage());
 
         RemoteRegistry wso2 = RSProviderUtil.getRegistry();
         Resource asset = wso2.get(resources[0]);
         
         assertEquals(stringProperty, asset.getProperty("stringProperty"));
         assertEquals(longProperty.toString(), asset.getProperty("longProperty"));
         assertEquals(booleanProperty.toString(), asset.getProperty("booleanProperty"));
         
         RSLifeCycle lc = RSLifeCycle.get(asset);
         assertEquals("New", lc.getState());
         assertEquals(true, lc.getItem(0).isValue());
     }
 
     private byte[] loadFile(String pathname) throws Exception {
         File file = new File(pathname);
         int size = (int) file.length();
         byte[] content = new byte[size];
 
         FileInputStream fin = new FileInputStream(file);
         fin.read(content, 0, size);
         return content;
     }
 }
