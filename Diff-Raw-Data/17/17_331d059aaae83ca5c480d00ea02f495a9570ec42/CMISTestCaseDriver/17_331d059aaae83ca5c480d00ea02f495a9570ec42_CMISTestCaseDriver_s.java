 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 
 package org.mule.module.cmis;
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.util.List;
 
 import org.apache.chemistry.opencmis.client.api.*;
 import org.apache.chemistry.opencmis.commons.data.Acl;
 import org.apache.chemistry.opencmis.commons.data.ContentStream;
 import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.mule.api.ConnectionException;
 import org.mule.api.lifecycle.InitialisationException;
 
 public class CMISTestCaseDriver {
 
     private static final CMISCloudConnector cmis = new CMISCloudConnector();
     private static ObjectId folder;
 
     @BeforeClass
     public static void setUpTests() throws ConnectionException {
         cmis.connect("user", "bitnami", "http://192.168.56.101/alfresco/cmis/", "d88d6ab2-1d08-4e49-954d-1a221532ff2f",
                 "SOAP", null, "false", "org.apache.chemistry.opencmis.client.bindings.spi.webservices.CXFPortProvider");
        
 //        cmis.deleteTree(null, getObjectId("/tmp-mule-tests"), true, UnfileObject.UNFILE, true);
 //        folder = cmis.createFolder("tmp-mule-tests", getObjectId("/"));
 //        cmis.createFolder("test", folder.getId());
 //        cmis.createDocumentByPath("/tmp-mule-tests/test", "SomeFile.txt", "Hello!", "text/plain",
 //                VersioningState.NONE, "cmis:document", null, true);
 //        cmis.createFolder("move-this", folder.getId());
     }
 
 //    @AfterClass
     public static void removeFolder() {
         cmis.deleteTree(null, folder.getId(), true, UnfileObject.UNFILE, true);
     }
     
     @Test(expected = IllegalArgumentException.class)
     public void failWrongId() {
         CmisObject obj = cmis.getObjectByPath("tmp-mule-tests/test");
         String wrongId = "1";
         cmis.getContentStream(obj, wrongId);
     }
 
     @Test
     @Ignore
     public void changeLog() throws InitialisationException {
         ChangeEvents events = cmis.changelog(cmis.repositoryInfo().getLatestChangeLogToken(), true);
         assertFalse(events.getHasMoreItems());
         assertTrue(events.getTotalNumItems() > 0);
     }
 
     @Test
     public void repositories() {
         List<Repository> repositories = cmis.repositories();
 
         for(Repository repository : repositories) {
             System.out.println("Repository: " + repository.getId());
         }
 
         assertNotNull(repositories);
     }
 
     @Test
     public void folderParent() {
         String subFolderId = getObjectId("/tmp-mule-tests/test");
         Folder parent = (Folder) cmis.folder(null, subFolderId, NavigationOptions.PARENT, null, null, null);
 
         assertEquals("/tmp-mule-tests", parent.getPath());
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void folderTree() {
         String folderId = getObjectId("/");
         List<Tree<FileableCmisObject>> tree = (List<Tree<FileableCmisObject>>) cmis.folder(null,
                 folderId, NavigationOptions.TREE, 1, null, null);
         assertNotNull(tree);
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void folderDescendants() {
         String folderId = getObjectId("/tmp-mule-tests");
         List<Tree<FileableCmisObject>> tree = (List<Tree<FileableCmisObject>>) cmis.folder(null,
                 folderId, NavigationOptions.DESCENDANTS, 1, null, null);
         assertNotNull(tree);
     }
 
     @Test
     public void objectParents() {
         String parent = "/tmp-mule-tests";
         CmisObject obj = cmis.getObjectByPath(parent + "/test");
         List<Folder> parents = cmis.getParentFolders(obj, null);
 
         boolean present = false;
 
         for (Folder folder : parents) {
             if (folder.getPath().equals(parent)) {
                 present = true;
             }
         }
 
         assertTrue(present);
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void folderContent() {
         String folderId = getObjectId("/");
         ItemIterable<CmisObject> it = (ItemIterable<CmisObject>) cmis.folder(null, folderId,
                 NavigationOptions.CHILDREN, null, null, null);
         Assert.assertNotNull(it);
     }
 
     @Test
     public void checkedOutDocs() {
         ItemIterable<Document> docs = cmis.getCheckoutDocs(null, null);
         assertNotNull(docs);
     }
 
     @Test
     @Ignore
     public void checkOut() {
         ObjectId id = cmis.checkOut(null, getObjectId("/tmp-mule-tests/test"));
         cmis.cancelCheckOut(null, id.getId());
     }
 
     @Test
     public void query() {
         ItemIterable<QueryResult> results = cmis.query("SELECT * from cmis:folder ", false, null, null);
         assertNotNull(results);
     }
 
     @Test
     public void getContentStream() {
         CmisObject object = cmis.getObjectByPath("/tmp-mule-tests/test/SomeFile.txt");
         ContentStream contentStream = cmis.getContentStream(object, null);
         assertEquals("text/plain;charset=UTF-8", contentStream.getMimeType());
     }
 
     @Test
     public void moveObject() {
         String f1 = getObjectId("/tmp-mule-tests");
         String f2 = getObjectId("/tmp-mule-tests/test");
         FileableCmisObject obj = (FileableCmisObject) cmis.getObjectByPath("/tmp-mule-tests/move-this");
         cmis.moveObject(obj, null, f1, f2);
         assertNotNull(cmis.getObjectByPath("/tmp-mule-tests/test/move-this"));
         cmis.moveObject(obj, null, f2, f1);
     }
 
     @Test
     public void getAllVersions() {
         String id = getObjectId("/tmp-mule-tests/test/SomeFile.txt");
         assertNotNull(cmis.getAllVersions(null, id, null, null));
     }
 
     @Test
     @Ignore
     public void checkout() {
         String id = getObjectId("/tmp-mule-tests/test");
         ObjectId pwc = cmis.checkOut(null, id);
         cmis.cancelCheckOut(null, pwc.getId());
     }
 
     @Test
     @Ignore
     public void checkIn() {
         String id = getObjectId("/tmp-mule-tests/test");
         cmis.checkIn(null, id, "modified content", "test", "application/octet-stream;charset=UTF-8", true,
                 "modified test file", null);
     }
 
     @Test
     public void relationShips() {
         String id = getObjectId("/tmp-mule-tests/test");
         assertNotNull(cmis.getObjectRelationships(null, id));
     }
 
     @Test
     public void getAcl() {
         Acl acl = cmis.getAcl(null, getObjectId("/tmp-mule-tests/test"));
         assertNotNull(acl);
     }
 
     @Test
     public void getAppliedPolicies() {
         cmis.getAppliedPolicies(null, getObjectId("/tmp-mule-tests/test"));
     }
 
     @Test
     public void createAndDeleteFolder() {
         String parentId = getObjectId("/");
         ObjectId toDelete = cmis.createFolder("delete me", parentId);
         cmis.delete(null, toDelete.getId(), true);
     }
 
     @Test
     public void createAndDeleteTree() {
         String parentId = getObjectId("/");
         ObjectId parentToDelete = cmis.createFolder("parent to delete", parentId);
         cmis.createFolder("delete me", parentToDelete.getId());
         cmis.deleteTree(null, parentToDelete.getId(), true, UnfileObject.UNFILE, true);
     }
 
     @Test
     public void createAndDeleteDocument() {
         ObjectId id = cmis.createDocumentByPath("/", "foo.txt", "txttxttxt",
                 "text/plain", VersioningState.NONE, "cmis:document", null, false);
         cmis.delete(null, id.getId(), true);
     }
 
     private static String getObjectId(String path) {
         return cmis.getObjectByPath(path).getId();
     }
 
     /**
      * Tests that a document can be created under an existent path
      */
     @Test
     public void createDocumentExistentPath() throws Exception {
         assertCanCreateInFolder("/tmp-mule-tests/");
     }
 
     @Test
     public void createDocumentFromStream() throws Exception {
         cmis.createDocumentByPathFromContent("/", "newFile.txt", new ByteArrayInputStream(new byte[0]),
                 "text/plain", VersioningState.NONE, "cmis:document", null, false);
     }
 
     /**
      * Tests that a document can be created under an inexistent path
      * that contains existent folders
      */
     @Test
     public void createDocumentPartialInexistentPath() throws Exception {
         try {
             assertCanCreateInFolder("/tmp-mule-tests/bar/baz/");
         } finally {
             deleteTree("/tmp-mule-tests/bar/");
         }
     }
 
     /**
      * Tests that a document can be created under an inexistent path
      */
     @Test
     public void createDocumentInexistentPath() throws Exception {
         try {
             assertCanCreateInFolder("/foobar/bar/baz/");
         } finally {
             deleteTree("/foobar/");
         }
 
     }
 
     private void deleteTree(String folder) {
         CmisObject objectByPath = cmis.getObjectByPath(folder);
         if (objectByPath != null) {
             cmis.deleteTree(null, objectByPath.getId(), true, null, false);
         }
     }
 
     private void assertCanCreateInFolder(String folderPath) {
         ObjectId document = null;
         try {
             document = cmis.createDocumentByPath(folderPath, "File.txt", "Hello!", "text/plain",
                     VersioningState.NONE, "cmis:document", null, true);
             assertNotNull(document);
         } finally {
             if (document != null) {
                 cmis.delete(null, document.getId(), true);
             }
         }
     }
 
     public static ObjectId getFolder() {
         return folder;
     }
 
     public static void setFolder(ObjectId folder) {
         CMISTestCaseDriver.folder = folder;
     }
 }
