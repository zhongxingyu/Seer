 /*
  * #%L
  * Bitrepository Integrity Client
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
 package org.bitrepository.integrityclient;
 
 import java.util.Date;
 
 import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumsDataGroupedByChecksumSpec;
 import org.bitrepository.bitrepositoryelements.FileIDs;
 import org.bitrepository.bitrepositoryelements.FileIDsData;
 import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
 import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.settings.TestSettingsProvider;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.integrityclient.cache.CachedIntegrityInformationStorage;
 import org.bitrepository.integrityclient.cache.MemoryBasedIntegrityCache;
 import org.bitrepository.integrityclient.checking.IntegrityChecker;
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 public class IntegrityCheckingTest extends ExtendedTestCase {
     /** The settings for the tests. Should be instantiated in the setup.*/
     Settings settings;
     
     public static final String TEST_PILLAR_1 = "test-pillar-1";
     public static final String TEST_PILLAR_2 = "test-pillar-2";
     
     @BeforeClass (alwaysRun = true)
     public void setup() {
         settings = TestSettingsProvider.reloadSettings();
     }
     
     @Test(groups = {"regressiontest"})
     public void testFileidsValid() {
         addDescription("Tests the file ids validation is able to give good result, when two pillars give the same "
                 + "fileids results.");
         addStep("Setup the environment for this test.", "Should define the pillars and fileids, and the clear the cache.");
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_1);
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_2);
         
         CachedIntegrityInformationStorage cache = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage();
         ((MemoryBasedIntegrityCache) cache).clearCache();
         String[] fileids = new String[]{"test-file-1", "test-file-2"};
         
         addStep("Initialise the file ids data.", "Should be created and put into the cache.");
         FileIDsData fileidsData1 = new FileIDsData();
         FileIDsDataItems items1 = new FileIDsDataItems();
 
         for(String fileid : fileids) {
             FileIDsDataItem item = new FileIDsDataItem();
             item.setFileID(fileid);
             item.setCreationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
             items1.getFileIDsDataItem().add(item);
         }
         fileidsData1.setFileIDsDataItems(items1);
         
         // add the files for two pillars.
         cache.addFileIDs(fileidsData1, TEST_PILLAR_1);
         cache.addFileIDs(fileidsData1, TEST_PILLAR_2);
         
         addStep("Instantiate the IntegrityChecker and the file ids to validate", "Should validate all the files.");
        IntegrityChecker checker = IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settings);
         
         FileIDs fileidsToCheck = new FileIDs();
         fileidsToCheck.setAllFileIDs("true");
         
         addStep("Check whether all pillars have all the file ids", "They should contain the same files.");
         Assert.assertTrue(checker.checkFileIDs(fileidsToCheck), "The file ids should be validated");
     }
     
     @Test(groups = {"regressiontest"})
     public void testFileidsMissingAtOnePillar() {
         addDescription("Tests the file ids validation is able to give bad result, when only one pillars has delivered "
                 + "fileids results.");
         addStep("Setup the environment for this test.", "Should define the pillars and fileids, and the clear the cache.");
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_1);
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_2);
         
         CachedIntegrityInformationStorage cache = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage();
         ((MemoryBasedIntegrityCache) cache).clearCache();
         String[] fileids = new String[]{"test-file-1", "test-file-2"};
         
         addStep("Initialise the file ids data.", "Should be created and put into the cache.");
         FileIDsData fileidsData1 = new FileIDsData();
         FileIDsDataItems items1 = new FileIDsDataItems();
 
         for(String fileid : fileids) {
             FileIDsDataItem item = new FileIDsDataItem();
             item.setFileID(fileid);
             item.setCreationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
             items1.getFileIDsDataItem().add(item);
         }
         fileidsData1.setFileIDsDataItems(items1);
         
         // add the files for two pillars.
         cache.addFileIDs(fileidsData1, TEST_PILLAR_1);
         
         addStep("Instantiate the IntegrityChecker and the file ids to validate", "Should validate all the files.");
        IntegrityChecker checker = IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settings);
         
         FileIDs fileidsToCheck = new FileIDs();
         fileidsToCheck.setAllFileIDs("true");
         
         addStep("Check whether all pillars have all the file ids", "Only one should contain the fileids, so it should return false");
         Assert.assertFalse(checker.checkFileIDs(fileidsToCheck), "The file ids should be validated");
     }
 
     @Test(groups = {"regressiontest"})
     public void testChecksumValid() {
         addDescription("Tests that the checksum validation is able to give good result, when two pillars give the same "
                 + "checksum results.");
         addStep("Setup the environment for this test.", "Should define the pillars and fileids, and the clear the cache.");
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_1);
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_2);
         
         CachedIntegrityInformationStorage cache = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage();
         ((MemoryBasedIntegrityCache) cache).clearCache();
         String[] fileids = new String[]{"test-file-1", "test-file-2"};
        
         addStep("Initialise the checksum results data.", "Should be created and put into the cache.");
         ChecksumsDataGroupedByChecksumSpec checksumdata = new ChecksumsDataGroupedByChecksumSpec();
         ChecksumSpecTYPE checksumtype = new ChecksumSpecTYPE();
         checksumtype.setChecksumSalt("");
         checksumtype.setChecksumType("MD5");
         checksumdata.setChecksumSpec(checksumtype);
         
         for(String fileid : fileids) {
             ChecksumDataForChecksumSpecTYPE checksumCalculation = new ChecksumDataForChecksumSpecTYPE();
             checksumCalculation.setFileID(fileid);
             checksumCalculation.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
             checksumCalculation.setChecksumValue("123" + fileid + "123");
             checksumdata.getChecksumDataForChecksumSpec().add(checksumCalculation);
         }
         
         // add the checksums for two pillars.
         cache.addChecksums(checksumdata, TEST_PILLAR_1);
         cache.addChecksums(checksumdata, TEST_PILLAR_2);
         
         addStep("Instantiate the IntegrityChecker and the file ids to validate", "Should validate all the files.");
        IntegrityChecker checker = IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settings);
         
         FileIDs fileidsToCheck = new FileIDs();
         fileidsToCheck.setAllFileIDs("true");
         
         addStep("Check whether all pillars have all the file ids", "They should contain the same files.");
        Assert.assertTrue(checker.checkFileIDs(fileidsToCheck), "The file ids should be validated");
         
         addStep("Check that the checksum for these file ids are the same", "They should be.");
         Assert.assertTrue(checker.checkChecksum(fileidsToCheck), "The checksums should be validated");
     }
     
     @Test(groups = {"regressiontest"})
     public void testChecksumsMissingFromOnePillar() {
         addDescription("Tests that the checksum validation is able to give good result, even though not all pillars have the requested files.");
         addStep("Setup the environment for this test.", "Should define the pillars and fileids, and the clear the cache.");
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_1);
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_2);
         
         CachedIntegrityInformationStorage cache = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage();
         ((MemoryBasedIntegrityCache) cache).clearCache();
         String[] fileids = new String[]{"test-file-1", "test-file-2"};
        
         addStep("Initialise the checksum results data.", "Should be created and put into the cache.");
         ChecksumsDataGroupedByChecksumSpec checksumdata = new ChecksumsDataGroupedByChecksumSpec();
         ChecksumSpecTYPE checksumtype = new ChecksumSpecTYPE();
         checksumtype.setChecksumSalt("");
         checksumtype.setChecksumType("MD5");
         checksumdata.setChecksumSpec(checksumtype);
         
         for(String fileid : fileids) {
             ChecksumDataForChecksumSpecTYPE checksumCalculation = new ChecksumDataForChecksumSpecTYPE();
             checksumCalculation.setFileID(fileid);
             checksumCalculation.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
             checksumCalculation.setChecksumValue("123" + fileid + "123");
             checksumdata.getChecksumDataForChecksumSpec().add(checksumCalculation);
         }
         
         // add the checksums for two pillars.
         cache.addChecksums(checksumdata, TEST_PILLAR_1);
         
         addStep("Instantiate the IntegrityChecker and the file ids to validate", "Should validate all the files.");
        IntegrityChecker checker = IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settings);
         
         FileIDs fileidsToCheck = new FileIDs();
         fileidsToCheck.setAllFileIDs("true");
         
         addStep("Check whether all pillars have all the file ids", "Only one pillar should contain the file ids.");
         Assert.assertFalse(checker.checkFileIDs(fileidsToCheck), "The file ids should be validated");
         
         addStep("Check that the checksum for these file ids are valid", "They should be, since only pillar has the checksums.");
         Assert.assertTrue(checker.checkChecksum(fileidsToCheck), "The checksums should be validated");
     }
     
     @Test(groups = {"regressiontest"})
     public void testChecksumsDifferForOneFile() {
         addDescription("Tests that the checksum validation is able to give a negative result, when two pillars give "
                 + "different checksum results.");
         addStep("Setup the environment for this test.", "Should define the pillars and fileids, and the clear the cache.");
         settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_1);
         settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_2);
         
         CachedIntegrityInformationStorage cache = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage();
         ((MemoryBasedIntegrityCache) cache).clearCache();
         String[] fileids = new String[]{"test-file-1", "test-file-2"};
        
         addStep("Initialise the two different checksum results data.", "Should be created and put into the cache for each pillar.");
         ChecksumSpecTYPE checksumtype = new ChecksumSpecTYPE();
         checksumtype.setChecksumSalt("");
         checksumtype.setChecksumType("MD5");
         ChecksumsDataGroupedByChecksumSpec checksumdata1 = new ChecksumsDataGroupedByChecksumSpec();
         checksumdata1.setChecksumSpec(checksumtype);
         for(String fileid : fileids) {
             ChecksumDataForChecksumSpecTYPE checksumCalculation = new ChecksumDataForChecksumSpecTYPE();
             checksumCalculation.setFileID(fileid);
             checksumCalculation.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
             checksumCalculation.setChecksumValue("123" + fileid + "123");
             checksumdata1.getChecksumDataForChecksumSpec().add(checksumCalculation);
         }
         
         ChecksumsDataGroupedByChecksumSpec checksumdata2 = new ChecksumsDataGroupedByChecksumSpec();
         checksumdata2.setChecksumSpec(checksumtype);
         for(String fileid : fileids) {
             ChecksumDataForChecksumSpecTYPE checksumCalculation = new ChecksumDataForChecksumSpecTYPE();
             checksumCalculation.setFileID(fileid);
             checksumCalculation.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
             checksumCalculation.setChecksumValue("abc" + fileid + "abc");
             checksumdata2.getChecksumDataForChecksumSpec().add(checksumCalculation);
         }
         
         // add the checksums for two pillars.
         cache.addChecksums(checksumdata1, TEST_PILLAR_1);
         cache.addChecksums(checksumdata2, TEST_PILLAR_2);
         
         addStep("Instantiate the IntegrityChecker and the file ids to validate", "Should validate all the files.");
        IntegrityChecker checker = IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settings);
         
         FileIDs fileidsToCheck = new FileIDs();
         fileidsToCheck.setAllFileIDs("true");
         
         addStep("Check whether all pillars have all the file ids", "They should contain the same files.");
         Assert.assertTrue(checker.checkFileIDs(fileidsToCheck), "The file ids should be validated");
         
         addStep("Check whether the checksum for these file ids are the same", "They should NOT be.");
         Assert.assertFalse(checker.checkChecksum(fileidsToCheck), "The checksums should not be validated");
     }
 }
