 /**
  * Mule CMIS Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.cmis;
 
 import java.util.List;
 
 import org.apache.chemistry.opencmis.client.api.ChangeEvents;
 import org.apache.chemistry.opencmis.client.api.CmisObject;
 import org.apache.chemistry.opencmis.client.api.Document;
 import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
 import org.apache.chemistry.opencmis.client.api.Folder;
 import org.apache.chemistry.opencmis.client.api.ItemIterable;
 import org.apache.chemistry.opencmis.client.api.ObjectId;
 import org.apache.chemistry.opencmis.client.api.QueryResult;
 import org.apache.chemistry.opencmis.client.api.Tree;
 import org.apache.chemistry.opencmis.commons.data.Acl;
 import org.apache.chemistry.opencmis.commons.data.ContentStream;
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.mule.api.lifecycle.InitialisationException;
 
 public class CMISTestCaseDriver
 {
     private final CMISCloudConnector cmis = new CMISCloudConnector();
     {
         cmis.setUsername("admin");
         cmis.setPassword("admin");
         cmis.setRepositoryId("371554cd-ac06-40ba-98b8-e6b60275cca7");
         cmis.setBaseUrl("http://cmis.alfresco.com/service/cmis");
         cmis.setEndpoint("atompub");
         try
         {
             cmis.initialise();
         }
         catch (InitialisationException e)
         {
             e.printStackTrace();
         }
         
         
     }
    
     @Test(expected = IllegalArgumentException.class)
     @Ignore
     public void failWrongId()
     {
         final CmisObject obj = cmis.getObjectByPath("mule-cloud-connector/test");
         final String wrongId = "1";
         cmis.getContentStream(obj, wrongId);
     }
     
     @Test
     @Ignore    
     public void changeLog() throws InitialisationException
     {
         final ChangeEvents events =  cmis.changelog("42215", false);
         Assert.assertFalse(events.getHasMoreItems());
         Assert.assertTrue(events.getTotalNumItems() > 0);
     }
     
     @Test
     @Ignore
     public void repositories()
     {
         Assert.assertNotNull(cmis.repositories());
     }
     
     @Test
     @Ignore
     public void folderParent()
     {
         final String subFolderId = getObjectId("/mule-cloud-connector/test-folder");
         Folder parent = (Folder) cmis.folder(null, subFolderId, NavigationOptions.PARENT, 
             null, null, null);
         
         Assert.assertEquals("/mule-cloud-connector", parent.getPath());
     }
     
     @SuppressWarnings("unchecked")
     @Test
     @Ignore
     public void folderTree()
     {
         final String folderId = getObjectId("/mule-cloud-connector");
         final List<Tree<FileableCmisObject>> tree = (List<Tree<FileableCmisObject>>) cmis.folder(
                                  null, folderId, NavigationOptions.TREE, 1, null, null);
         Assert.assertNotNull(tree);
     }
     
     @SuppressWarnings("unchecked")
     @Test
     @Ignore
     public void folderDescendants()
     {
         final String folderId = getObjectId("/mule-cloud-connector");
         final List<Tree<FileableCmisObject>> tree = (List<Tree<FileableCmisObject>>) cmis.folder(
             null, folderId, NavigationOptions.DESCENDANTS, 1, null, null);
         Assert.assertNotNull(tree);
     }
     
     @Test
     @Ignore
     public void objectParents()
     {
         final String parent = "/mule-cloud-connector";
         final CmisObject obj = cmis.getObjectByPath(parent + "/test");
         final List<Folder> parents = cmis.getParentFolders(obj, null);
         
         boolean present = false;
         
         for (Folder folder : parents)
         {
             if (folder.getPath().equals(parent))
             {
                 present = true;
             }
         }
         
         Assert.assertTrue(present);
     }
 
     @Test
     @Ignore
     @SuppressWarnings("unchecked")
     public void folderContent()
     {
         final String folderId = getObjectId("/mule-cloud-connector");
         ItemIterable<CmisObject> it = (ItemIterable<CmisObject>) cmis.folder(
             null, folderId, NavigationOptions.CHILDREN, 
             null, null, null);
         Assert.assertNotNull(it);
     }
     
     @Test
     @Ignore
     public void checkedOutDocs()
     {
         final ItemIterable<Document> docs = cmis.getCheckoutDocs(null, null);
         Assert.assertNotNull(docs);
     }
     
     @Test
     @Ignore
     public void checkOut()
     {
         final ObjectId id = cmis.checkOut(null, getObjectId("/mule-cloud-connector/test"));
         cmis.cancelCheckOut(null, id.getId());
     }
     
     @Test
     @Ignore
     public void query()
     {
         ItemIterable<QueryResult> results = cmis.query("SELECT * from cmis:folder ",
                                                        false, null, null);
         Assert.assertNotNull(results);
     }
     
     @Test
     @Ignore
     public void getContentStream()
     {
         final CmisObject object = cmis.getObjectByPath("/mule-cloud-connector/test");
         final ContentStream contentStream = cmis.getContentStream(object, null);
         Assert.assertEquals("application/octet-stream;charset=UTF-8", contentStream.getMimeType());
     }
 
     @Test
     @Ignore
     public void moveObject()
     {
         final String f1 = getObjectId("/mule-cloud-connector");
         final String f2 = getObjectId("/mule-cloud-connector/test-folder");
         final FileableCmisObject obj = (FileableCmisObject) cmis.getObjectByPath("/mule-cloud-connector/move-this");
         cmis.moveObject(obj, null, f1, f2);
         Assert.assertNotNull(cmis.getObjectByPath("/mule-cloud-connector/test-folder/move-this"));
         cmis.moveObject(obj, null, f2, f1);
     }
 
     @Test
     @Ignore
     public void getAllVersions()
     {
         final String id = getObjectId("/mule-cloud-connector/test");
         Assert.assertNotNull(cmis.getAllVersions(null, id, null, null));
     }
     
     @Test
     @Ignore
     public void checkout()
     {
         final String id = getObjectId("/mule-cloud-connector/test");
         final ObjectId pwc = cmis.checkOut(null, id);
         cmis.cancelCheckOut(null, pwc.getId());
     }
     
     @Test
     @Ignore
     public void checkIn()
     {
         final String id = getObjectId("/mule-cloud-connector/test");
         cmis.checkIn(null, id, "modified content", "test", 
             "application/octet-stream;charset=UTF-8", true, "modified test file");
     }
     
     @Test
     @Ignore
     public void relationShips()
     {
         final String id = getObjectId("/mule-cloud-connector/test");
         Assert.assertNotNull(cmis.getObjectRelationships(null, id));
     }
     
     @Test
     @Ignore
     public void getAcl()
     {
         Acl acl = cmis.getAcl(null, getObjectId("/mule-cloud-connector/test"));
         Assert.assertNotNull(acl);
     }
     
     @Test
     @Ignore
     public void getAppliedPolicies()
     {
        Assert.assertNotNull(cmis.getAppliedPolicies(null, getObjectId("/mule-cloud-connector/test")));
     }
     
     @Test
     @Ignore
     public void createAndDeleteFolder()
     {
         final String parentId = getObjectId("/mule-cloud-connector");
         final ObjectId toDelete = cmis.createFolder("delete me", parentId);
         cmis.delete(null, toDelete.getId(), true);
     }
 
     @Test
     @Ignore
     public void createAndDeleteTree()
     {
         final String parentId = getObjectId("/mule-cloud-connector");
         final ObjectId toDelete = cmis.createFolder("delete me", parentId);
         cmis.delete(null, toDelete.getId(), true);
     }
     
     @Test
     @Ignore
     public void createAndDeleteDocument()
     {
         final ObjectId id = cmis.createDocumentByPath("/mule-cloud-connector", 
                 "foo.txt", "txttxttxt", "text/plain", VersioningState.NONE, "cmis:document");
         cmis.delete(null, id.getId(), true);
     }
 
     private String getObjectId(final String path)
     {
         return cmis.getObjectByPath(path).getId();
     }
     
 }
