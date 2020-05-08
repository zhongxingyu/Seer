 /*******************************************************************************
  * (C) Copyright 2013 Open Wide (http://www.openwide.fr/) and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl-2.1.html
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  ******************************************************************************/
 package fr.openwide.nuxeo.propertysync;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.runtime.test.runner.Deploy;
 import org.nuxeo.runtime.test.runner.LocalDeploy;
 
 import com.google.inject.Inject;
 
 import fr.openwide.nuxeo.DefaultHierarchy;
 import fr.openwide.nuxeo.test.AbstractNuxeoTest;
 import fr.openwide.nuxeo.types.TypeFile;
 import fr.openwide.nuxeo.types.TypeFolder;
 import fr.openwide.nuxeo.types.TypeNote;
 
 /**
  * 
  * @author mkalam-alami
  * 
  */
 @Deploy({
    "org.nuxeo.ecm.platform.types.api",
    "org.nuxeo.ecm.platform.types.core", // Contains the TypeManager service
     "fr.openwide.nuxeo.commons.constants",
     "fr.openwide.nuxeo.commons.propertysync"
 })
 @LocalDeploy("fr.openwide.nuxeo.commons.propertysync:OSGI-INF/propertysync-test.xml")
 public class PropertySyncTest extends AbstractNuxeoTest {
 
     private static final String MATCHING_FACET = "Sync";
 
     private static final String FOLDER_1_TITLE = "f1";
     
     private static final String FOLDER_2_TITLE = "f2";
 
     private static DocumentModel folder1;
 
     private static DocumentModel note1;
 
     private static DocumentModel file1;
 
     @Inject
     private CoreSession documentManager;
 
     @Test
     public void testUpdateOnChildAction() throws Exception {
         String rootPath = DefaultHierarchy.WORKSPACES_PATH_AS_STRING;
         
         folder1 = documentManager.createDocumentModel(rootPath, "f1", TypeFolder.TYPE);
         folder1.setPropertyValue(TypeFolder.XPATH_TITLE, FOLDER_1_TITLE);
         folder1 = documentManager.createDocument(folder1);
 
         DocumentModel folder2 = documentManager.createDocumentModel(rootPath, "f2", TypeFolder.TYPE);
         folder2.setPropertyValue(TypeFolder.XPATH_TITLE, FOLDER_2_TITLE);
         folder2 = documentManager.createDocument(folder2);
         
         note1 = documentManager.createDocumentModel(folder1.getPathAsString(), "n1", TypeNote.TYPE);
         note1 = documentManager.createDocument(note1);
         Assert.assertEquals(FOLDER_1_TITLE, note1.getPropertyValue(TypeNote.XPATH_DESCRIPTION));
         Assert.assertEquals(FOLDER_1_TITLE, note1.getPropertyValue(TypeNote.XPATH_TITLE));
         
         // Copy
         DocumentModel note2 = documentManager.copy(note1.getRef(), folder2.getRef(), "n2");
         Assert.assertEquals(FOLDER_2_TITLE, note2.getPropertyValue(TypeNote.XPATH_DESCRIPTION));
         
         // Move
         note2 = documentManager.move(note1.getRef(), folder1.getRef(), "n2");
         Assert.assertEquals(FOLDER_1_TITLE, note2.getPropertyValue(TypeNote.XPATH_DESCRIPTION));
 
         file1 = documentManager.createDocumentModel(folder1.getPathAsString(), "file1", TypeFile.TYPE);
         file1.addFacet(MATCHING_FACET);
         file1 = documentManager.createDocument(file1);
         Assert.assertEquals(FOLDER_1_TITLE, file1.getPropertyValue(TypeNote.XPATH_DESCRIPTION));
         
         // Creation : Ignore not matching
         DocumentModel file2 = documentManager.createDocumentModel(folder1.getPathAsString(), "file2", TypeFile.TYPE);
         file2 = documentManager.createDocument(file2);
         Assert.assertNull(file2.getPropertyValue(TypeFile.XPATH_DESCRIPTION));
         
         // Creation : Ignore if not null
         DocumentModel note3 = documentManager.createDocumentModel(folder1.getPathAsString(), "n3", TypeNote.TYPE);
         note3.setPropertyValue(TypeNote.XPATH_TITLE, "n3");
         note3 = documentManager.createDocument(note3);
         Assert.assertEquals("n3", note3.getPropertyValue(TypeNote.XPATH_TITLE));
     }
     
     @Test
     public void testUpdateOnAncestorAction() throws Exception {
         DocumentModel folder3 = documentManager.createDocumentModel(folder1.getPathAsString(), "f3", TypeFolder.TYPE);
         folder3 = documentManager.createDocument(folder3);
         
         // Update parent
         folder1.setPropertyValue(TypeFolder.XPATH_TITLE, FOLDER_2_TITLE);
         documentManager.saveDocument(folder1);
         
         // Check on doctype
         note1 = documentManager.getDocument(note1.getRef());
         Assert.assertEquals(FOLDER_2_TITLE, note1.getPropertyValue(TypeNote.XPATH_DESCRIPTION));
         
         // Check on facet
         file1 = documentManager.getDocument(file1.getRef());
         Assert.assertEquals(FOLDER_2_TITLE, file1.getPropertyValue(TypeNote.XPATH_DESCRIPTION));
         
         // Check no mass update
         folder3 = documentManager.getDocument(folder3.getRef());
         Assert.assertEquals(FOLDER_1_TITLE, folder3.getPropertyValue(TypeFolder.XPATH_DESCRIPTION));
     }
 
 }
