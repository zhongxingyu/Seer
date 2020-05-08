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
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
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
         DBUnitHelper.loadData("");
     }
 
     @AfterClass
     public static void deleteData() throws Exception {
         DBUnitHelper.deleteData("seed-vocabularies.xml");
     }
 
     @Test
     public void testGetVodabularyFolder_byId() throws ServiceException {
         VocabularyFolder result = vocabularyService.getVocabularyFolder(1);
         assertNotNull("Expected vocabulary folder", result);
     }
 
     @Test
     public void testGetVodabularyFolder_byIdentifier() throws ServiceException {
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
         assertEquals("Folders size", 2, result.size());
         assertEquals("Items size", 3, result.get(0).getItems().size());
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
 }
