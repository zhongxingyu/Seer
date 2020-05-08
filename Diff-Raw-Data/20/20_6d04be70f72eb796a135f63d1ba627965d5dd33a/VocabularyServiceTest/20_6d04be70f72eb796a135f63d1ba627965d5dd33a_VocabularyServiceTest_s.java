 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Content Registry 3
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev or Zero Technologies are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Juhan Voolaid
  */
 
 package eionet.meta.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.unitils.UnitilsJUnit4;
 import org.unitils.spring.annotation.SpringApplicationContext;
 import org.unitils.spring.annotation.SpringBeanByType;
 
 import eionet.meta.dao.domain.Folder;
 import eionet.meta.dao.domain.VocabularyConcept;
 import eionet.meta.dao.domain.VocabularyFolder;
 import eionet.meta.dao.domain.VocabularyType;
 import eionet.meta.service.data.VocabularyConceptFilter;
 import eionet.meta.service.data.VocabularyConceptResult;
 
 /**
  * JUnit integration test with Unitils for vocabulary service.
  *
  * @author Juhan Voolaid
  */
 @SpringApplicationContext("spring-context.xml")
 // @DataSet({"seed-vocabularies.xml"})
 public class VocabularyServiceTest extends UnitilsJUnit4 {
 
     /** Logger. */
     protected static final Logger LOGGER = Logger.getLogger(VocabularyServiceTest.class);
 
     @SpringBeanByType
     private IVocabularyService vocabularyService;
 
     @BeforeClass
     public static void loadData() throws Exception {
         DBUnitHelper.loadData("seed-vocabularies.xml");
     }
 
     @AfterClass
     public static void deleteData() throws Exception {
         DBUnitHelper.deleteData("seed-vocabularies.xml");
     }
 
     @Test
     public void testGetVocabularyFolder_byId() throws ServiceException {
         VocabularyFolder result = vocabularyService.getVocabularyFolder(1);
         assertNotNull("Expected vocabulary folder", result);
     }
 
     @Test
     public void testGetVocabularyFolder_byIdentifier() throws ServiceException {
         VocabularyFolder result = vocabularyService.getVocabularyFolder("common", "test_vocabulary2", true);
         assertEquals("Expected id", 3, result.getId());
     }
 
     @Test
     public void testGetVocabularyFolders_anonymous() throws ServiceException {
         List<VocabularyFolder> result = vocabularyService.getVocabularyFolders(null);
         assertEquals("Result size", 2, result.size());
     }
 
     @Test
     public void testGetVocabularyFolders_testUser() throws ServiceException {
         List<VocabularyFolder> result = vocabularyService.getVocabularyFolders("testUser");
         assertEquals("Result size", 3, result.size());
     }
 
     @Test
     public void testCreateVocabularyFolder() throws ServiceException {
         VocabularyFolder vocabularyFolder = new VocabularyFolder();
         vocabularyFolder.setFolderId(1);
         vocabularyFolder.setLabel("test");
         vocabularyFolder.setIdentifier("test");
         vocabularyFolder.setType(VocabularyType.COMMON);
 
         int id = vocabularyService.createVocabularyFolder(vocabularyFolder, null, "testUser");
         VocabularyFolder result = vocabularyService.getVocabularyFolder(id);
         assertNotNull("Expected vocabulary folder", result);
     }
 
     @Test
     public void testCreateVocabularyFolder_withNewFolder() throws ServiceException {
         VocabularyFolder vocabularyFolder = new VocabularyFolder();
         vocabularyFolder.setLabel("test");
         vocabularyFolder.setIdentifier("test");
         vocabularyFolder.setType(VocabularyType.COMMON);
 
         Folder newFolder = new Folder();
         newFolder.setIdentifier("new");
         newFolder.setLabel("new");
 
         int id = vocabularyService.createVocabularyFolder(vocabularyFolder, newFolder, "testUser");
         VocabularyFolder result = vocabularyService.getVocabularyFolder(id);
         assertNotNull("Expected vocabulary folder", result);
     }
 
     @Test
     public void testSearchVocabularyConcepts() throws ServiceException {
         VocabularyConceptFilter filter = new VocabularyConceptFilter();
         filter.setVocabularyFolderId(3);
 
         VocabularyConceptResult result = vocabularyService.searchVocabularyConcepts(filter);
         assertEquals("Result size", 2, result.getFullListSize());
     }
 
     @Test
     public void testCreateVocabularyConcept() throws ServiceException {
         VocabularyConcept concept = new VocabularyConcept();
         concept.setIdentifier("test3");
         concept.setLabel("test3");
 
         int id = vocabularyService.createVocabularyConcept(3, concept);
 
         VocabularyConcept result = vocabularyService.getVocabularyConcept(id, true);
         assertNotNull("Expected concept", result);
     }
 
     @Test
     public void testUpdateVocabularyConcept() throws ServiceException {
         VocabularyConcept result = vocabularyService.getVocabularyConcept(1, true);
         result.setLabel("modified");
         vocabularyService.updateVocabularyConcept(result);
         result = vocabularyService.getVocabularyConcept(1, true);
         assertEquals("Modified label", "modified", result.getLabel());
     }
 
     @Test
     public void testUpdateVocabularyFolder() throws ServiceException {
         VocabularyFolder result = vocabularyService.getVocabularyFolder(1);
         result.setLabel("modified");
         vocabularyService.updateVocabularyFolder(result, null);
         result = vocabularyService.getVocabularyFolder(1);
         assertEquals("Modified label", "modified", result.getLabel());
     }
 
     @Test
     public void testUpdateVocabularyFolder_withNewFolder() throws ServiceException {
         Folder newFolder = new Folder();
         newFolder.setIdentifier("new");
         newFolder.setLabel("new");
 
         VocabularyFolder result = vocabularyService.getVocabularyFolder(1);
         result.setLabel("modified");
         vocabularyService.updateVocabularyFolder(result, newFolder);
         result = vocabularyService.getVocabularyFolder(1);
         assertEquals("Modified label", "modified", result.getLabel());
     }
 
     @Test
     public void testDeleteVocabularyConcepts() throws ServiceException {
         vocabularyService.deleteVocabularyConcepts(Collections.singletonList(1));
 
         Exception exception = null;
         try {
             vocabularyService.getVocabularyConcept(1, true);
             fail("Expected concept not found exception");
         } catch (ServiceException e) {
             exception = e;
         }
         assertNotNull("Expected exception", exception);
     }
 
     @Test
     public void testMarkConceptsObsolete() throws ServiceException {
         vocabularyService.markConceptsObsolete(Collections.singletonList(1));
         VocabularyConcept concept = vocabularyService.getVocabularyConcept(1, true);
         assertNotNull("Obsolete date", concept.getObsolete());
     }
 
     @Test
     public void testUnMarkConceptsObsolete() throws ServiceException {
         vocabularyService.markConceptsObsolete(Collections.singletonList(1));
         vocabularyService.unMarkConceptsObsolete(Collections.singletonList(1));
         VocabularyConcept concept = vocabularyService.getVocabularyConcept(1, true);
         assertNull("Obsolete date", concept.getObsolete());
     }
 
     @Test
     public void testDeleteVocabularyFolders() throws ServiceException {
         vocabularyService.deleteVocabularyFolders(Collections.singletonList(1));
 
         Exception exception = null;
         try {
             vocabularyService.getVocabularyFolder(1);
             fail("Expected vocabulary not found exception");
         } catch (ServiceException e) {
             exception = e;
         }
         assertNotNull("Expected exception", exception);
     }
 
     @Test
     public void testCheckOutVocabularyFolder() throws ServiceException {
         vocabularyService.checkOutVocabularyFolder(1, "testUser");
         VocabularyFolder result = vocabularyService.getVocabularyFolder("common", "test_vocabulary1", true);
 
         assertNotNull("Working copy vocabulary", result);
         assertEquals("Working user", "testUser", result.getWorkingUser());
         assertEquals("Working copy", true, result.isWorkingCopy());
         assertEquals("Checked out copy id", 1, result.getCheckedOutCopyId());
     }
 
     @Test
     public void testCheckInVocabularyFolder() throws ServiceException {
         vocabularyService.checkInVocabularyFolder(3, "testUser");
 
         Exception exception = null;
         try {
             vocabularyService.getVocabularyFolder("common", "test_vocabulary2", true);
             fail("Expected vocabulary not found exception");
         } catch (ServiceException e) {
             exception = e;
         }
         assertNotNull("Expected exception", exception);
 
         VocabularyFolder result = vocabularyService.getVocabularyFolder("common", "test_vocabulary2", false);
 
         assertNotNull("Original vocabulary", result);
         assertNull("Working user", result.getWorkingUser());
         assertEquals("Working copy", false, result.isWorkingCopy());
         assertEquals("Checked out copy id", 0, result.getCheckedOutCopyId());
     }
 
     @Test
     public void testCreateVocabularyFolderCopy() throws ServiceException {
         VocabularyFolder vocabularyFolder = new VocabularyFolder();
         vocabularyFolder.setType(VocabularyType.COMMON);
         vocabularyFolder.setFolderId(1);
         vocabularyFolder.setLabel("copy");
         vocabularyFolder.setIdentifier("copy");
         int id = vocabularyService.createVocabularyFolderCopy(vocabularyFolder, 1, "testUser", null);
         VocabularyFolder result = vocabularyService.getVocabularyFolder(id);
         assertNotNull("Expected vocabulary folder", result);
     }
 
     @Test
     public void testCreateVocabularyFolderCopy_withNewFolder() throws ServiceException {
         Folder newFolder = new Folder();
         newFolder.setIdentifier("new");
         newFolder.setLabel("new");
 
         VocabularyFolder vocabularyFolder = new VocabularyFolder();
         vocabularyFolder.setType(VocabularyType.COMMON);
         vocabularyFolder.setLabel("copy");
         vocabularyFolder.setIdentifier("copy");
         int id = vocabularyService.createVocabularyFolderCopy(vocabularyFolder, 1, "testUser", newFolder);
         VocabularyFolder result = vocabularyService.getVocabularyFolder(id);
         assertNotNull("Expected vocabulary folder", result);
     }
 
     @Test
     public void testGetVocabularyFolderVersions() throws ServiceException {
         List<VocabularyFolder> result = vocabularyService.getVocabularyFolderVersions("123", 1, "testUser");
         assertEquals("Number of other versions", 2, result.size());
     }
 
     @Test
     public void testUndoCheckOut() throws ServiceException {
         vocabularyService.checkOutVocabularyFolder(1, "testUser");
         VocabularyFolder workingCopy = vocabularyService.getVocabularyFolder("common", "test_vocabulary1", true);
         vocabularyService.undoCheckOut(workingCopy.getId(), "testUser");
 
         Exception exception = null;
         try {
             vocabularyService.getVocabularyFolder("common", "test_vocabulary1", true);
             fail("Expected vocabulary not found exception");
         } catch (ServiceException e) {
             exception = e;
         }
         assertNotNull("Expected exception", exception);
     }
 
     @Test
     public void testGetVocabularyWorkingCopy() throws ServiceException {
         VocabularyFolder result = vocabularyService.getVocabularyWorkingCopy(2);
         assertNotNull("Expected vocabulary folder", result);
     }
 
     @Test
     public void testIsUniqueFolderIdentifier() throws ServiceException {
         boolean result = vocabularyService.isUniqueVocabularyFolderIdentifier(1, "test", null);
         assertEquals("Is unique", true, result);
     }
 
     @Test
     public void testiIUniqueConceptIdentifier() throws ServiceException {
         boolean result = vocabularyService.isUniqueConceptIdentifier("2", 3, 2);
         assertEquals("Is unique", true, result);
     }
 
     @Test
     public void testGetFolders() throws ServiceException {
         List<Folder> result = vocabularyService.getFolders("testUser", 1);
         assertEquals("Folders size", 4, result.size());
         Folder folderCommon = null;
         for (Folder folder : result) {
             if ("common".equals(folder.getIdentifier())) {
                 folderCommon = folder;
             }
         }
         assertEquals("Items size", 3, folderCommon.getItems().size());
     }
 
     @Test
     public void testGetFolder() throws ServiceException {
         Folder result = vocabularyService.getFolder(1);
         assertNotNull("Folder", result);
     }
 
     @Test
     public void testIsFolderEmpty() throws ServiceException {
         assertFalse("Folder empty", vocabularyService.isFolderEmpty(1));
     }
 
     @Test
     public void testDeleteFolder() throws ServiceException {
         vocabularyService.deleteFolder(2);
         Exception exception = null;
         try {
             vocabularyService.getFolder(2);
             fail("Expected vocabulary not found exception");
         } catch (ServiceException e) {
             exception = e;
         }
         assertNotNull("Expected exception", exception);
     }
 
     @Test
     public void testDeleteFolder_notEmpty() throws ServiceException {
         Exception exception = null;
         try {
             vocabularyService.deleteFolder(1);
             fail("Expected folder not empty exception");
         } catch (ServiceException e) {
             exception = e;
         }
         assertNotNull("Expected exception", exception);
     }
 
     @Test
     public void testUpdateFolder() throws ServiceException {
         Folder folder = vocabularyService.getFolder(2);
         folder.setIdentifier("new");
         folder.setLabel("new");
         vocabularyService.updateFolder(folder);
         folder = vocabularyService.getFolder(2);
 
         assertEquals("Modified identifier", "new", folder.getIdentifier());
         assertEquals("Modified label", "new", folder.getLabel());
     }
 
     @Test
     public void testGetFolderByIdentifier() throws ServiceException {
         Folder result = vocabularyService.getFolderByIdentifier("test1");
         assertNotNull("Folder", result);
     }
 
     @Test
     public void testGetFolders_sorting() throws ServiceException {
         List<Folder> result = vocabularyService.getFolders(null, null);
         assertEquals("The first folder", "xxx", result.get(0).getLabel());
     }
 
     /**
      * The purpose is to test the vocabularies' "enforce concept notation equals concept identifier" functionality.
      *
      * @throws ServiceException An error happens in the called services.
      */
     @Test
     public void testNotationEqualsIdentifier() throws ServiceException {
 
         String userName = "testUser";
 
         // First lets create a vocabulary with no particular setting on the enforce-notation-equals-identifier policy.
 
         VocabularyFolder vocabulary = new VocabularyFolder();
         vocabulary.setFolderId(1);
         vocabulary.setLabel("TestVoc1");
         vocabulary.setIdentifier("test_voc_1");
         vocabulary.setType(VocabularyType.COMMON);
         int vocId = vocabularyService.createVocabularyFolder(vocabulary, null, userName);
         vocabulary = vocabularyService.getVocabularyFolder(vocId);
         assertNotNull("Expected a vocabulary folder", vocabulary);
         assertFalse("Expected the enforcement flag to be down", vocabulary.isNotationsEqualIdentifiers());
 
         // Now lets check out the freshly created vocabulary, so that we can start adding concepts to it.
 
         vocId = vocabularyService.checkOutVocabularyFolder(vocId, userName);
         assertTrue("Expected working copy id to be greater than the original id", vocId > vocabulary.getId());
         vocabulary = vocabularyService.getVocabularyFolder(vocId);
         assertNotNull("Expected a vocbulary working copy", vocabulary);
         assertEquals("Expected a working user", userName, vocabulary.getWorkingUser());
         assertEquals("Expected working copy flag set", true, vocabulary.isWorkingCopy());
 
         // Now lets add concepts to the freshly created vocabulary working copy.
 
         VocabularyConcept concept1 = new VocabularyConcept();
         concept1.setIdentifier("conc1");
         concept1.setLabel("Concept 1");
         concept1.setNotation("Conc_1");
         int concId1 = vocabularyService.createVocabularyConcept(vocId, concept1);
         concept1 = vocabularyService.getVocabularyConcept(concId1, true);
         assertNotNull("Expected a concept", concept1);
         assertNotEquals("Expected unequal notation and identifier", concept1.getNotation(), concept1.getIdentifier());
 
         VocabularyConcept concept2 = new VocabularyConcept();
         concept2.setIdentifier("conc2");
         concept2.setLabel("Concept 2");
         concept2.setNotation("Conc_2");
         int concId2 = vocabularyService.createVocabularyConcept(vocId, concept2);
         concept2 = vocabularyService.getVocabularyConcept(concId2, true);
         assertNotNull("Expected a concept", concept2);
         assertNotEquals("Expected unequal notation and identifier", concept2.getNotation(), concept2.getIdentifier());
 
         // Now lets enforce the notation=identifier rule on the vocabulary working copy.
 
         vocabulary.setNotationsEqualIdentifiers(true);
         vocabularyService.updateVocabularyFolder(vocabulary, null);
         vocabulary = vocabularyService.getVocabularyFolder(vocabulary.getId());
         assertNotNull("Expected an updated vocbulary", vocabulary);
         assertTrue("Expected the enforcement flag to be up", vocabulary.isNotationsEqualIdentifiers());
 
         // Check that both concept notations have now been forcefully made equal to the identifiers.
 
         concept1 = vocabularyService.getVocabularyConcept(concId1, true);
         assertEquals("Expected equal notation and identifier", concept1.getNotation(), concept1.getIdentifier());
         concept2 = vocabularyService.getVocabularyConcept(concId2, true);
         assertEquals("Expected equal notation and identifier", concept2.getNotation(), concept2.getIdentifier());
 
         // Add one more concept, and check that its notation now gets forcefully overwritten with identifier.
 
         VocabularyConcept concept3 = new VocabularyConcept();
         concept3.setIdentifier("conc3");
         concept3.setLabel("Concept 3");
         concept3.setNotation("Conc_3");
         int concId3 = vocabularyService.createVocabularyConcept(vocId, concept3);
         concept3 = vocabularyService.getVocabularyConcept(concId3, true);
         assertNotNull("Expected a concept", concept3);
         assertEquals("Expected equal notation and identifier", concept3.getNotation(), concept3.getIdentifier());
     }
 }
