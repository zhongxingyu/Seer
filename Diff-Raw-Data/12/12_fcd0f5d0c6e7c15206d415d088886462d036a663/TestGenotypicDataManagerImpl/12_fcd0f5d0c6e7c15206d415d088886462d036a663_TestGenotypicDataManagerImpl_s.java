 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 
 package org.generationcp.middleware.manager.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.generationcp.middleware.manager.Database;
 import org.generationcp.middleware.manager.DatabaseConnectionParameters;
 import org.generationcp.middleware.manager.ManagerFactory;
 import org.generationcp.middleware.manager.api.GenotypicDataManager;
 import org.generationcp.middleware.pojos.Name;
 import org.generationcp.middleware.pojos.gdms.AccMetadataSetPK;
 import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
 import org.generationcp.middleware.pojos.gdms.AllelicValueWithMarkerIdElement;
 import org.generationcp.middleware.pojos.gdms.DatasetElement;
 import org.generationcp.middleware.pojos.gdms.GermplasmMarkerElement;
 import org.generationcp.middleware.pojos.gdms.Map;
 import org.generationcp.middleware.pojos.gdms.MapInfo;
 import org.generationcp.middleware.pojos.gdms.MappingValueElement;
 import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
 import org.generationcp.middleware.pojos.gdms.MarkerInfo;
 import org.generationcp.middleware.pojos.gdms.MarkerNameElement;
 import org.generationcp.middleware.pojos.gdms.ParentElement;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TestGenotypicDataManagerImpl{
 
     private static ManagerFactory factory;
     private static GenotypicDataManager manager;
 
     @BeforeClass
     public static void setUp() throws Exception {
         DatabaseConnectionParameters local = new DatabaseConnectionParameters("testDatabaseConfig.properties", "local");
         DatabaseConnectionParameters central = new DatabaseConnectionParameters("testDatabaseConfig.properties", "central");
         factory = new ManagerFactory(local, central);
         manager = factory.getGenotypicDataManager();
     }
 
     @Test
     public void testGetNameIdsByGermplasmIds() throws Exception {
         List<Integer> germplasmIds = new ArrayList<Integer>();
         germplasmIds.add(Integer.valueOf(-3787));
         germplasmIds.add(Integer.valueOf(-6785));
         germplasmIds.add(Integer.valueOf(-4070));
         List<Integer> results = manager.getNameIdsByGermplasmIds(germplasmIds);
         System.out.println("testGetNameIdsByGermplasmIds(" + germplasmIds + ") RESULTS: " + results);
     }
 
     @Test
     public void testGetNamesByNameIds() throws Exception {
         List<Integer> nameIds = new ArrayList<Integer>();
         nameIds.add(Integer.valueOf(-1));
         nameIds.add(Integer.valueOf(-2));
         nameIds.add(Integer.valueOf(-3));
         List<Name> results = manager.getNamesByNameIds(nameIds);
         System.out.println("testGetNamesByNameIds(" + nameIds + ") RESULTS: " + results);
     }
 
     @Test
     public void testGetNameByNameId() throws Exception {
         Integer nameId = -1;
         Name name = manager.getNameByNameId(nameId);
         System.out.println("testGetNameByNameId(nameId=" + nameId + ") RESULTS: " + name);
     }
 
     @Test
     public void testGetFirstFiveMaps() throws Exception {
         List<Map> maps = manager.getAllMaps(0, 5, Database.LOCAL);
         System.out.println("RESULTS (testGetFirstFiveMaps): " + maps);
     }
 
     @Test
     public void testGetMapInfoByMapName() throws Exception {
         String mapName = ""; //TODO: test with a given map name
         List<MapInfo> results = manager.getMapInfoByMapName(mapName, Database.LOCAL);
         System.out.println("testGetMapInfoByMapName(mapName=" + mapName + ") RESULTS: " + results);
     }
 
     @Test
     public void testCountDatasetNames() throws Exception {
         long results = manager.countDatasetNames(Database.LOCAL);
         System.out.println("testCountDatasetNames(Database.LOCAL) RESULTS: " + results);
     }
 
     @Test
     public void testGetDatasetNames() throws Exception {
         List<String> results = manager.getDatasetNames(0, 5, Database.LOCAL);
         System.out.println("testGetDatasetNames(0,5,Database.Local) RESULTS: " + results);
     }
 
     @Test
     public void testGetDatasetDetailsByDatasetName() throws Exception {
         String datasetName = "MARS";
         List<DatasetElement> results = manager.getDatasetDetailsByDatasetName(datasetName, Database.LOCAL);
         System.out.println("testGetDatasetDetailsByDatasetName(" + datasetName + ",LOCAL) RESULTS: " + results);
     }
 
     @Test
     public void testGetMarkerIdsByMarkerNames() throws Exception {
         List<String> markerNames = new ArrayList<String>();
         markerNames.add("1_0085");
         markerNames.add("1_0319");
         markerNames.add("1_0312");
 
         /* Expected results are: [1, 2, 3, 174, 199, 201]
          * This is based on the sample input data templates uploaded to GDMS */
         List<Integer> markerIds = manager.getMarkerIdsByMarkerNames(markerNames, 0, 100, Database.LOCAL);
         System.out.println("getMarkerIdsByMarkerNames (" + markerNames + ") RESULTS: " + markerIds);
     }
 
     @Test
     public void testGetMarkerIdsByDatasetId() throws Exception {
         Integer datasetId = Integer.valueOf(2);
         List<Integer> results = manager.getMarkerIdsByDatasetId(datasetId);
         System.out.println("testGetMarkerIdByDatasetId(" + datasetId + ") RESULTS: " + results);
     }
 
     @Test
     public void testGetParentsByDatasetId() throws Exception {
         Integer datasetId = Integer.valueOf(2);
         List<ParentElement> results = manager.getParentsByDatasetId(datasetId);
         System.out.println("testGetParentsByDatasetId(" + datasetId + ") RESULTS: " + results);
     }
 
     @Test
     public void testGetMarkerTypesByMarkerIds() throws Exception {
         List<Integer> markerIds = new ArrayList<Integer>();
         markerIds.add(Integer.valueOf(1));
         markerIds.add(Integer.valueOf(2));
         markerIds.add(Integer.valueOf(3));
         markerIds.add(Integer.valueOf(4));
         markerIds.add(Integer.valueOf(5));
 
         List<String> results = manager.getMarkerTypesByMarkerIds(markerIds);
         System.out.println("testGetMarkerTypeByMarkerIds(" + markerIds + ") RESULTS: " + results);
     }
 
     @Test
     public void testGetMarkerNamesByGIds() throws Exception {
         List<Integer> gIds = new ArrayList<Integer>();
         gIds.add(Integer.valueOf(-4072));
         gIds.add(Integer.valueOf(-4070));
         gIds.add(Integer.valueOf(-4069));
 
         List<MarkerNameElement> results = manager.getMarkerNamesByGIds(gIds);
         System.out.println("testGetMarkerNamesByGIds(" + gIds + ") RESULTS: " + results);
     }
 
     @Test
     public void testGetGermplasmNamesByMarkerNames() throws Exception {
         List<String> markerNames = new ArrayList<String>();
         markerNames.add("1_0001");
         markerNames.add("1_0007");
         markerNames.add("1_0013");
 
         List<GermplasmMarkerElement> results = (List<GermplasmMarkerElement>) manager.getGermplasmNamesByMarkerNames(markerNames,
                 Database.LOCAL);
         System.out.println("testGetGermplasmNamesByMarkerNames(" + markerNames + ")  RESULTS: " + results);
     }
 
     @Test
     public void testGetMappingValuesByGidsAndMarkerNames() throws Exception {
         List<Integer> gids = new ArrayList<Integer>();
         gids.add(-3785);
         gids.add(-3786);
         gids.add(-3787);
         List<String> markerNames = new ArrayList<String>();
         markerNames.add("1_0085");
         markerNames.add("1_0319");
         markerNames.add("1_0312");
         /* Expected results are: [datasetId=2, mappingType=allelic, parentAGid=-6785, parentBGid=-6786, markerType=S]
          * This is based on the sample input data templates uploaded to GDMS */
         List<MappingValueElement> mappingValues = manager.getMappingValuesByGidsAndMarkerNames(gids, markerNames, 0, 100);
         System.out.println("testGetMappingValuesByGidsAndMarkerNames(" + gids + ", " + markerNames + ") RESULTS: " + mappingValues);
     }
 
     @Test
     public void testGetAllelicValuesByGidsAndMarkerNames() throws Exception {
         List<String> markerNames = new ArrayList<String>();
         markerNames.add("CaM0038");
         markerNames.add("CaM0463");
         markerNames.add("CaM0539");
         markerNames.add("CaM0639");
         markerNames.add("CaM0658");
         markerNames.add("1_0001");
         markerNames.add("1_0007");
         markerNames.add("1_0013");
         markerNames.add("1_0025");
         markerNames.add("1_0031");
         List<Integer> gids = new ArrayList<Integer>();
         gids.add(-5276);
         gids.add(-5287);
         gids.add(-5484);
         gids.add(-5485);
         gids.add(-6786);
         gids.add(-6785);
         gids.add(-3785);
         gids.add(-3786);
         /* Results will vary depending on the database connected to.
          * As of the moment, we have no data that contains test values in all 3 source tables */
         List<AllelicValueElement> allelicValues = manager.getAllelicValuesByGidsAndMarkerNames(gids, markerNames);
         System.out.println("testGetAllelicValuesByGidsAndMarkerNames(" + gids + ", " + markerNames + ") RESULTS: " + allelicValues);
     }
 
     @Test
     public void testGetAllelicValuesFromCharValuesByDatasetId() throws Exception {
         Integer datasetId = Integer.valueOf(2);
         long count = manager.countAllelicValuesFromCharValuesByDatasetId(datasetId);
         List<AllelicValueWithMarkerIdElement> allelicValues = manager.getAllelicValuesFromCharValuesByDatasetId(datasetId, 0, (int) count);
         System.out.println("testGetAllelicValuesFromCharValuesByDatasetId(" + datasetId + ") RESULTS: " + allelicValues);
     }
 
     @Test
     public void testGetAllelicValuesFromAlleleValuesByDatasetId() throws Exception {
         Integer datasetId = Integer.valueOf(2);
         long count = manager.countAllelicValuesFromCharValuesByDatasetId(datasetId);
         List<AllelicValueWithMarkerIdElement> allelicValues = manager
                 .getAllelicValuesFromAlleleValuesByDatasetId(datasetId, 0, (int) count);
         System.out.println("testGetAllelicValuesFromAlleleValuesByDatasetId(" + datasetId + ") RESULTS: " + allelicValues);
     }
 
     @Test
     public void testGetAllelicValuesFromMappingPopValuesByDatasetId() throws Exception {
         Integer datasetId = Integer.valueOf(2);
         long count = manager.countAllelicValuesFromCharValuesByDatasetId(datasetId);
         List<AllelicValueWithMarkerIdElement> allelicValues = manager.getAllelicValuesFromMappingPopValuesByDatasetId(datasetId, 0,
                 (int) count);
         System.out.println("testGetAllelicValuesFromMappingPopValuesByDatasetId(" + datasetId + ") RESULTS: " + allelicValues);
     }
 
     @Test
     public void testGetMarkerNamesByMarkerIds() throws Exception {
         List<Integer> markerIds = new ArrayList<Integer>();
         markerIds.add(-1);
         markerIds.add(-2);
         markerIds.add(-3);
         markerIds.add(-4);
         markerIds.add(-5);
 
         List<MarkerIdMarkerNameElement> markerNames = manager.getMarkerNamesByMarkerIds(markerIds);
         System.out.println("testGetMarkerNamesByMarkerIds(" + markerIds + ") RESULTS: ");
         for (MarkerIdMarkerNameElement e : markerNames) {
             System.out.println(e.getMarkerId() + " : " + e.getMarkerName());
         }
     }
 
     @Test
     public void testGetAllMarkerTypes() throws Exception {
         List<String> markerTypes = manager.getAllMarkerTypes(0, 10);
         System.out.println("testGetAllMarkerTypes(0,10) RESULTS: " + markerTypes);
     }
 
     @Test
     public void testCountAllMarkerTypesLocal() throws Exception {
         long result = manager.countAllMarkerTypes(Database.LOCAL);
         System.out.println("testCountAllMarkerTypes(Database.LOCAL) RESULTS: " + result);
     }
 
     @Test
     public void testCountAllMarkerTypesCentral() throws Exception {
         long result = manager.countAllMarkerTypes(Database.CENTRAL);
         System.out.println("testCountAllMarkerTypes(Database.CENTRAL) RESULTS: " + result);
     }
 
     @Test
     public void testGetMarkerNamesByMarkerType() throws Exception {
         String markerType = "asdf";
         List<String> markerNames = manager.getMarkerNamesByMarkerType(markerType, 1, 10);
         System.out.println("testGetMarkerNamesByMarkerType(" + markerType + ") RESULTS: " + markerNames);
     }
 
     @Test
     public void testCountMarkerNamesByMarkerType() throws Exception {
         String markerType = "asdf";
         long result = manager.countMarkerNamesByMarkerType(markerType);
         System.out.println("testCountMarkerNamesByMarkerType(" + markerType + ") RESULTS: " + result);
     }
 
     @Test
     public void testGetMarkerInfoByMarkerName() throws Exception {
         String markerName = "1_0437";
         long count = manager.countMarkerInfoByMarkerName(markerName);
         System.out.println("countMarkerInfoByMarkerName(" + markerName + ")  RESULTS: " + count);
         List<MarkerInfo> results = manager.getMarkerInfoByMarkerName(markerName, 0, (int) count);
         System.out.println("testGetMarkerInfoByMarkerName(" + markerName + ") RESULTS: " + results);
     }
 
     @Test
     public void testGetMarkerInfoByGenotype() throws Exception {
         String genotype = "";
         long count = manager.countMarkerInfoByGenotype(genotype);
         System.out.println("countMarkerInfoByGenotype(" + genotype + ") RESULTS: " + count);
         List<MarkerInfo> results = manager.getMarkerInfoByGenotype(genotype, 0, (int) count);
         System.out.println("testGetMarkerInfoByGenotype(" + genotype + ") RESULTS: " + results);
     }
 
     @Test
     public void testGetMarkerInfoByDbAccessionId() throws Exception {
         String dbAccessionId = "";
         long count = manager.countMarkerInfoByDbAccessionId(dbAccessionId);
         System.out.println("countMarkerInfoByDbAccessionId(" + dbAccessionId + ")  RESULTS: " + count);
         List<MarkerInfo> results = manager.getMarkerInfoByDbAccessionId(dbAccessionId, 0, (int) count);
         System.out.println("testGetMarkerInfoByDbAccessionId(" + dbAccessionId + ")  RESULTS: " + results);
     }
 
     @Test
     public void testGetGidsFromCharValuesByMarkerId() throws Exception {
         Integer markerId = 1;
         List<Integer> gids = manager.getGIDsFromCharValuesByMarkerId(markerId, 1, 10);
         System.out.println("testGetGidsFromCharValuesByMarkerId(" + markerId + ") RESULTS: " + gids);
     }
 
     @Test
     public void testCountGidsFromCharValuesByMarkerId() throws Exception {
         Integer markerId = 1;
         long result = manager.countGIDsFromCharValuesByMarkerId(markerId);
         System.out.println("testCountGidsFromCharValuesByMarkerId(" + markerId + ") RESULTS: " + result);
     }
 
     @Test
     public void testGetGidsFromAlleleValuesByMarkerId() throws Exception {
         Integer markerId = 1;
         List<Integer> gids = manager.getGIDsFromCharValuesByMarkerId(markerId, 1, 10);
         System.out.println("testGetGidsFromAlleleValuesByMarkerId(" + markerId + ") RESULTS: " + gids);
     }
 
     @Test
     public void testCountGidsFromAlleleValuesByMarkerId() throws Exception {
         Integer markerId = 1;
         long result = manager.countGIDsFromCharValuesByMarkerId(markerId);
         System.out.println("testCountGidsFromAlleleValuesByMarkerId(" + markerId + ") RESULTS: " + result);
     }
 
     @Test
     public void testGetGidsFromMappingPopValuesByMarkerId() throws Exception {
         Integer markerId = 1;
         List<Integer> gids = manager.getGIDsFromMappingPopValuesByMarkerId(markerId, 1, 10);
         System.out.println("testGetGidsFromMappingPopValuesByMarkerId(" + markerId + ") RESULTS: " + gids);
     }
 
     @Test
     public void testCountGidsFromMappingPopValuesByMarkerId() throws Exception {
         Integer markerId = 1;
         long result = manager.countGIDsFromMappingPopValuesByMarkerId(markerId);
         System.out.println("testCountGidsFromMappingPopValuesByMarkerId(" + markerId + ") RESULTS: " + result);
     }
 
     @Test
     public void testGetAllDbAccessionIdsFromMarker() throws Exception {
         List<String> dbAccessionIds = manager.getAllDbAccessionIdsFromMarker(1, 10);
         System.out.println("testGetAllDbAccessionIdsFromMarker(1,10) RESULTS: " + dbAccessionIds);
     }
 
     @Test
     public void testCountAllDbAccessionIdsFromMarker() throws Exception {
         long result = manager.countAllDbAccessionIdsFromMarker();
         System.out.println("testCountAllDbAccessionIdsFromMarker() RESULTS: " + result);
     }
 
     @Test
     public void testGetNidsFromAccMetadatasetByDatasetIds() throws Exception {
         List<Integer> datasetIds = new ArrayList<Integer>();
         datasetIds.add(-1);
         datasetIds.add(-2);
         datasetIds.add(-3);
 
         List<Integer> gids = new ArrayList<Integer>();
         gids.add(-2);
 
         List<Integer> nids = manager.getNidsFromAccMetadatasetByDatasetIds(datasetIds, 0, 10);
         List<Integer> nidsWithGidFilter = manager.getNidsFromAccMetadatasetByDatasetIds(datasetIds, gids, 0, 10);
 
         System.out.println("testGetNidsFromAccMetadatasetByDatasetIds RESULTS: " + nids);
         System.out.println("testGetNidsFromAccMetadatasetByDatasetIds with gid filter RESULTS: " + nidsWithGidFilter);
     }
 
     @Test
     public void testGetDatasetIdsForFingerPrinting() throws Exception {
         List<Integer> datasetIds = manager.getDatasetIdsForFingerPrinting(0, (int) manager.countDatasetIdsForFingerPrinting());
         System.out.println("testGetDatasetIdsForFingerPrinting() RESULTS: " + datasetIds);
     }
 
     @Test
     public void testCountDatasetIdsForFingerPrinting() throws Exception {
         long count = manager.countDatasetIdsForFingerPrinting();
         System.out.println("testCountDatasetIdsForFingerPrinting() RESULTS: " + count);
     }
 
     @Test
     public void testGetDatasetIdsForMapping() throws Exception {
         List<Integer> datasetIds = manager.getDatasetIdsForMapping(0, (int) manager.countDatasetIdsForMapping());
         System.out.println("testGetDatasetIdsForMapping() RESULTS: " + datasetIds);
     }
 
     @Test
     public void testCountDatasetIdsForMapping() throws Exception {
         long count = manager.countDatasetIdsForMapping();
         System.out.println("testCountDatasetIdsForMapping() RESULTS: " + count);
     }
 
     @Test
     public void testGetGdmsAccMetadatasetByGid() throws Exception {
         List<Integer> germplasmIds = new ArrayList<Integer>();
         germplasmIds.add(Integer.valueOf(956)); // Crop Tested: Groundnut
         germplasmIds.add(Integer.valueOf(1042));
         germplasmIds.add(Integer.valueOf(-2213));
         germplasmIds.add(Integer.valueOf(-2215));
         List<AccMetadataSetPK> accMetadataSets = manager.getGdmsAccMetadatasetByGid(germplasmIds, 0, 
                 (int) manager.countGdmsAccMetadatasetByGid(germplasmIds));
         System.out.println("testGetGdmsAccMetadatasetByGid() RESULTS: ");
         for (AccMetadataSetPK accMetadataSet : accMetadataSets) {
             System.out.println(accMetadataSet.toString());
         }
 
     }
 
     @Test
     public void testCountGdmsAccMetadatasetByGid() throws Exception { 
         List<Integer> germplasmIds = new ArrayList<Integer>(); 
         germplasmIds.add(Integer.valueOf(956)); // Crop Tested: Groundnut
         germplasmIds.add(Integer.valueOf(1042));
         germplasmIds.add(Integer.valueOf(-2213));
         germplasmIds.add(Integer.valueOf(-2215));
         long count = manager.countGdmsAccMetadatasetByGid(germplasmIds);
         System.out.println("testCountGdmsAccMetadatasetByGid() RESULTS: " + count);
     }
 
     @Test
     public void testGetMarkersByGidAndDatasetIds() throws Exception {
         Integer gid = Integer.valueOf(-2215);    // Crop Tested: Groundnut
 
         List<Integer> datasetIds = new ArrayList<Integer>();
         datasetIds.add(Integer.valueOf(1));
         datasetIds.add(Integer.valueOf(2));
 
         List<Integer> markerIds = manager.getMarkersByGidAndDatasetIds(gid, datasetIds, 0, 
                 (int) manager.countMarkersByGidAndDatasetIds(gid, datasetIds));
         System.out.println("testGetMarkersByGidAndDatasetIds() RESULTS: " + markerIds);
     }
 
     @Test
     public void testCountMarkersByGidAndDatasetIds() throws Exception { 
         Integer gid = Integer.valueOf(-2215);    // Crop Tested: Groundnut
 
         List<Integer> datasetIds = new ArrayList<Integer>();
         datasetIds.add(Integer.valueOf(1));
         datasetIds.add(Integer.valueOf(2));
 
         long count = manager.countMarkersByGidAndDatasetIds(gid, datasetIds);
         System.out.println("testCountGdmsAccMetadatasetByGid() RESULTS: " + count);
     }
     
     @Test
     public void testCountAlleleValuesByGids() throws Exception { 
         List<Integer> germplasmIds = new ArrayList<Integer>(); 
         germplasmIds.add(Integer.valueOf(956)); // Crop Tested: Groundnut
         germplasmIds.add(Integer.valueOf(1042));
         germplasmIds.add(Integer.valueOf(-2213));
         germplasmIds.add(Integer.valueOf(-2215));
         long count = manager.countAlleleValuesByGids(germplasmIds);
         System.out.println("testCountAlleleValuesByGids() RESULTS: " + count);
     }
     
 
     @Test
     public void testCountCharValuesByGids() throws Exception { 
         List<Integer> germplasmIds = new ArrayList<Integer>(); 
         germplasmIds.add(Integer.valueOf(956)); // Please replace the gids found in the target crop to be used in testing
         germplasmIds.add(Integer.valueOf(1042));
         germplasmIds.add(Integer.valueOf(-2213));
         germplasmIds.add(Integer.valueOf(-2215));
         long count = manager.countCharValuesByGids(germplasmIds);
         System.out.println("testCountCharValuesByGids() RESULTS: " + count);
     }
     
 
     @Test
     public void testGetIntAlleleValuesForPolymorphicMarkersRetrieval() throws Exception {
         List<Integer> germplasmIds = new ArrayList<Integer>(); 
         germplasmIds.add(Integer.valueOf(956)); // Crop Tested: Groundnut
         germplasmIds.add(Integer.valueOf(1042));
         germplasmIds.add(Integer.valueOf(-2213));
         germplasmIds.add(Integer.valueOf(-2215));
 
         List<AllelicValueElement> results = manager.getIntAlleleValuesForPolymorphicMarkersRetrieval(germplasmIds, 0, 
                 (int) manager.countIntAlleleValuesForPolymorphicMarkersRetrieval(germplasmIds));
         System.out.println("testGetIntAlleleValuesForPolymorphicMarkersRetrieval() RESULTS: " + results);
     }
 
     @Test
     public void testCountIntAlleleValuesForPolymorphicMarkersRetrieval() throws Exception { 
         List<Integer> germplasmIds = new ArrayList<Integer>(); 
         germplasmIds.add(Integer.valueOf(956)); // Crop Tested: Groundnut
         germplasmIds.add(Integer.valueOf(1042));
         germplasmIds.add(Integer.valueOf(-2213));
         germplasmIds.add(Integer.valueOf(-2215));
         long count = manager.countIntAlleleValuesForPolymorphicMarkersRetrieval(germplasmIds);
         System.out.println("testCountIntAlleleValuesForPolymorphicMarkersRetrieval() RESULTS: " + count);
     }    
     
     @Test
     public void testGetCharAlleleValuesForPolymorphicMarkersRetrieval() throws Exception {
         List<Integer> germplasmIds = new ArrayList<Integer>(); 
         germplasmIds.add(Integer.valueOf(956)); // Please replace the gids found in the target crop to be used in testing
         germplasmIds.add(Integer.valueOf(1042));
         germplasmIds.add(Integer.valueOf(-2213));
         germplasmIds.add(Integer.valueOf(-2215));
 
         List<AllelicValueElement> results = manager.getCharAlleleValuesForPolymorphicMarkersRetrieval(germplasmIds, 0, 
                 (int) manager.countCharAlleleValuesForPolymorphicMarkersRetrieval(germplasmIds));
         System.out.println("testGetCharAlleleValuesForPolymorphicMarkersRetrieval() RESULTS: " + results);
     }
 
 
     @Test
     public void testCountCharAlleleValuesForPolymorphicMarkersRetrieval() throws Exception { 
         List<Integer> germplasmIds = new ArrayList<Integer>(); 
         germplasmIds.add(Integer.valueOf(956)); // Please replace the gids found in the target crop to be used in testing
         germplasmIds.add(Integer.valueOf(1042));
         germplasmIds.add(Integer.valueOf(-2213));
         germplasmIds.add(Integer.valueOf(-2215));
         long count = manager.countCharAlleleValuesForPolymorphicMarkersRetrieval(germplasmIds);
         System.out.println("testCountCharAlleleValuesForPolymorphicMarkersRetrieval() RESULTS: " + count);
     }    
     
     @Test
     public void testGetNIdsByDatasetIdsAndMarkerIdsAndNotGIds() throws Exception { 
         List<Integer> gIds = new ArrayList<Integer>(); 
         gIds.add(Integer.valueOf(29)); // Please replace the germplasm ids found in the target crop to be used in testing
         gIds.add(Integer.valueOf(303));
         gIds.add(Integer.valueOf(4950));
         
         List<Integer> datasetIds = new ArrayList<Integer>(); 
         datasetIds.add(Integer.valueOf(2)); // Please replace the dataset ids found in the target crop to be used in testing
         
         List<Integer> markerIds = new ArrayList<Integer>(); 
         markerIds.add(Integer.valueOf(6803)); // Please replace the marker ids found in the target crop to be used in testing
 
         List<Integer> nIdList = manager.getNIdsByMarkerIdsAndDatasetIdsAndNotGIds(datasetIds, markerIds, gIds, 1, 5);
         System.out.println("testGetNIdsByDatasetIdsAndMarkerIdsAndNotGIds() RESULTS: " + nIdList);
     }  
     
     @Test
     public void testcCountNIdsByDatasetIdsAndMarkerIdsAndNotGIds() throws Exception { 
         List<Integer> gIds = new ArrayList<Integer>(); 
         gIds.add(Integer.valueOf(29)); // Please replace the germplasm ids found in the target crop to be used in testing
         gIds.add(Integer.valueOf(303));
         gIds.add(Integer.valueOf(4950));
         
         List<Integer> datasetIds = new ArrayList<Integer>(); 
         datasetIds.add(Integer.valueOf(2)); // Please replace the dataset ids found in the target crop to be used in testing
         
         List<Integer> markerIds = new ArrayList<Integer>(); 
         markerIds.add(Integer.valueOf(6803)); // Please replace the marker ids found in the target crop to be used in testing
 
         int count = manager.countNIdsByMarkerIdsAndDatasetIdsAndNotGIds(datasetIds, markerIds, gIds);
         System.out.println("testcCountNIdsByDatasetIdsAndMarkerIdsAndNotGIds() RESULTS: " + count);
     }  
     
     @Test
     public void testGetNIdsByDatasetIdsAndMarkerIds() throws Exception { 
         
         List<Integer> datasetIds = new ArrayList<Integer>(); 
         datasetIds.add(Integer.valueOf(2)); // Please replace the dataset ids found in the target crop to be used in testing
         
         List<Integer> markerIds = new ArrayList<Integer>(); 
         markerIds.add(Integer.valueOf(6803)); // Please replace the marker ids found in the target crop to be used in testing
 
        List<Integer> nIdList = manager.getNIdsByMarkerIdsAndDatasetIds(datasetIds, markerIds, 0, 5);
         System.out.println("testGetNIdsByDatasetIdsAndMarkerIds() RESULTS: " + nIdList);
     }  
     
     @Test
     public void testCountNIdsByDatasetIdsAndMarkerIds() throws Exception { 
         
         List<Integer> datasetIds = new ArrayList<Integer>(); 
         datasetIds.add(Integer.valueOf(2)); // Please replace the dataset ids found in the target crop to be used in testing
         
         List<Integer> markerIds = new ArrayList<Integer>(); 
         markerIds.add(Integer.valueOf(6803)); // Please replace the marker ids found in the target crop to be used in testing
 
         int count = manager.countNIdsByMarkerIdsAndDatasetIds(datasetIds, markerIds);
         System.out.println("testCountNIdsByDatasetIdsAndMarkerIds() RESULTS: " + count);
     }  
     
     @AfterClass
     public static void tearDown() throws Exception {
         factory.close();
     }
 
 }
