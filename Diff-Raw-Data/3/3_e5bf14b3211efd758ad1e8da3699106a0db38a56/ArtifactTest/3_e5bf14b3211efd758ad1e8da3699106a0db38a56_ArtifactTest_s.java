 package org.mule.galaxy.impl;
 
 
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Set;
 
 import org.mule.galaxy.Item;
 import org.mule.galaxy.PropertyInfo;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.artifact.Artifact;
 import org.mule.galaxy.lifecycle.Phase;
 import org.mule.galaxy.query.Query;
 import org.mule.galaxy.test.AbstractGalaxyTest;
 import org.mule.galaxy.type.TypeManager;
 import org.w3c.dom.Document;
 
 public class ArtifactTest extends AbstractGalaxyTest {
     public void testMove() throws Exception {
         Item a = importHelloWsdl();
         
         Item w = registry.newItem("test", typeManager.getTypeByName(TypeManager.WORKSPACE)).getItem();
         
         registry.move(a, w.getPath(), a.getName());
         
         assertEquals(w.getId(), a.getParent().getId());
         
         Set<Item> results = registry.search(new Query().fromId(w.getId())).getResults();
         
         assertEquals(1, results.size());
         
         // test moving it into the workspace its already in.
         registry.move(a, w.getPath(), a.getName());
     }
     
     public void testRename() throws Exception {
         Item a = importHelloWsdl();
         a.setName("2.0");
         
         List<Item> artifacts = a.getParent().getItems();
         assertEquals(1, artifacts.size());
         
         Item a2 = (Item) artifacts.iterator().next();
         assertEquals("2.0", a2.getName());
     }
 //
 //    public void testWorkspaces() throws Exception {
 //        Collection<Item> workspaces = registry.getItems();
 //        assertEquals(1, workspaces.size());
 //        
 //        Item newWork = registry.newItem("New Workspace", type);
 //        assertEquals("New Workspace", newWork.getName());
 //        assertNotNull(newWork.getId());
 //        
 //        try {
 //            registry.newItem("New Workspace", type);
 //            fail("Two workspaces with the same name");
 //        } catch (DuplicateItemException e) {
 //        }
 //        
 //        Item child = newWork.newItem("Child", type);
 //        assertEquals("Child", child.getName());
 //        assertNotNull(child.getId());
 //        assertNotNull(child.getUpdated());
 //        
 //        assertEquals(1, newWork.getChildren().size());
 //        
 //        newWork.delete();
 //        
 //        assertEquals(1, registry.getItems().size());
 //        
 //        Item root = workspaces.iterator().next();
 //        child = root.newItem("child", type);
 //        
 //        Item newRoot = registry.newItem("newroot", type);
 //        registry.save(child, newRoot.getId());
 //        
 //        Collection<Item> children = newRoot.getChildren();
 //        assertEquals(1, children.size());
 //        
 //        child = children.iterator().next();
 //        assertNotNull(child.getParent());
 //        
 //        registry.save(newRoot, "root");
 //        
 //        Item newWorkspace = newRoot.newItem("child2", type);
 //        
 //        registry.save(newWorkspace, "root");
 //        
 //        assertNull(newWorkspace.getParent());
 //    }
 //    
 //    public void testAddDuplicate() throws Exception {
 //        InputStream helloWsdl = getResourceAsStream("/wsdl/hello.wsdl");
 //        
 //        Collection<Item> workspaces = registry.getItems();
 //        assertEquals(1, workspaces.size());
 //        Item workspace = workspaces.iterator().next();
 //        
 //        workspace.createArtifact("application/wsdl+xml", "hello_world.wsdl", "0.1", helloWsdl);
 //        
 //        helloWsdl = getResourceAsStream("/wsdl/hello.wsdl");
 //        try {
 //            workspace.createArtifact("application/wsdl+xml", "hello_world.wsdl", "0.1", helloWsdl);
 //            fail("Expected a duplicate item exception");
 //        } catch (DuplicateItemException e) {
 //            // great! expected
 //        }
 //        
 //        Collection<Item> artifacts = workspace.getItems();
 //        assertEquals(1, artifacts.size());
 //    }
 //    
 //    public void testAddWsdlWithApplicationOctetStream() throws Exception {
 //        InputStream helloWsdl = getResourceAsStream("/wsdl/hello.wsdl");
 //        
 //        Collection<Item> workspaces = registry.getItems();
 //        assertEquals(1, workspaces.size());
 //        Item workspace = workspaces.iterator().next();
 //        
 //        NewItemResult ar = workspace.createArtifact("application/octet-stream", 
 //                                                    "hello_world.wsdl", "0.1", helloWsdl);
 //        
 //        ArtifactImpl artifact = (ArtifactImpl) ar.getItem();
 //        
 //        assertEquals("application/wsdl+xml", artifact.getContentType().toString());
 //    }   
 //    
 //    public void testAddMuleConfig() throws Exception {
 //        InputStream helloMule = getResourceAsStream("/mule/hello-config.xml");
 //        
 //        Collection<Item> workspaces = registry.getItems();
 //        assertEquals(1, workspaces.size());
 //        Item workspace = workspaces.iterator().next();
 //        
 //        // Try application/xml
 //        NewItemResult ar = workspace.createArtifact("application/xml", 
 //                                                    "hello_world.xml", "0.1", helloMule);
 //        
 //        ArtifactImpl artifact = (ArtifactImpl) ar.getItem();
 //        
 //        assertEquals("application/xml", artifact.getContentType().toString());
 //        assertEquals("mule-configuration", artifact.getDocumentType().getLocalPart());
 //        
 //
 //        // Try application/octent-stream
 //        helloMule = getResourceAsStream("/mule/hello-config.xml");
 //        ar = workspace.createArtifact("application/octet-stream", "hello_world2.xml", "0.1",
 //                                     helloMule);
 //        
 //        artifact = (ArtifactImpl) ar.getItem();
 //        
 //        assertEquals("application/xml", artifact.getContentType().toString());
 //        assertEquals("mule-configuration", artifact.getDocumentType().getLocalPart());
 //    }
     
     public void testAddWsdl() throws Exception {
         Item av = importHelloWsdl();
         assertNotNull(av.getId());
 
         Phase p = av.getProperty(Registry.PRIMARY_LIFECYCLE);
         assertNotNull(p);
         
         p = av.getParent().getProperty(Registry.PRIMARY_LIFECYCLE);
         assertNull(p);
         
         Artifact artifact = av.getProperty("artifact");
         assertNotNull(artifact);
         assertEquals("application/xml", artifact.getContentType().toString());
         assertNotNull(artifact.getDocumentType());
         assertEquals("definitions", artifact.getDocumentType().getLocalPart());
         
         // test properties
         boolean testedTNS = false;
         for (PropertyInfo next : av.getProperties()) {
             if (next.getName().equals("wsdl.targetNamespace")) {
                 assertEquals("wsdl.targetNamespace", next.getName());
                 assertNotNull(next.getValue());
                 assertTrue(next.isLocked());
                 assertTrue(next.isVisible());
                 
                 assertEquals("WSDL Target Namespace", next.getDescription());
                 testedTNS = true;
             }
         }
         
         Calendar origUpdated = av.getUpdated();
         assertNotNull(origUpdated);
         
         assertTrue(testedTNS);
 
         // This is odd, but otherwise the updates happen too fast, and the lastUpdated tstamp isn't changed
         Thread.sleep(500);
 
         av.setLocked("wsdl.targetNamespace", true);
         av.setVisible("wsdl.targetNamespace", false);
         PropertyInfo pi = av.getPropertyInfo("wsdl.targetNamespace");
         assertTrue(pi.isLocked());
         assertFalse(pi.isVisible());
         
         Calendar update = av.getUpdated();
         assertTrue(update.after(origUpdated));
         
         av.setProperty("foo", "bar");
         assertEquals("bar", av.getProperty("foo"));
         
         // Test the version history
        assertTrue(artifact.getData() instanceof Document);
         assertNotNull(av.getAuthor());
         assertEquals("Created", getPhase(av).getName());
         
         Calendar created = av.getCreated();
         assertTrue(created.getTime().getTime() > 0);
         
         av.setProperty("foo", "bar");
         assertEquals("bar", av.getProperty("foo"));
          
         // Create another version
         InputStream stream = artifact.getInputStream();
         assertNotNull(stream);
         stream.close();
         
 //        InputStream helloWsdl2 = getResourceAsStream("/wsdl/hello.wsdl");
 //        
 //        ar = artifact.newVersion(helloWsdl2, "0.2");
 //        assertTrue(waitForIndexing((ArtifactVersion)ar.getEntryVersion()));
 //        JcrVersion newVersion = (JcrVersion) ar.getEntryVersion();
 //        assertTrue(newVersion.isLatest());
 //        assertFalse(version.isLatest());
 //        
 //        assertSame(newVersion, ar.getItem().getDefaultOrLastVersion());
 //        
 //        versions = artifact.getVersions();
 //        assertEquals(2, versions.size());
 //        
 //        assertEquals("0.2", newVersion.getVersionLabel());
 //        assertEquals("Created", getPhase(newVersion).getName());
 //        
 //        stream = newVersion.getStream();
 //        assertNotNull(stream);
 //        assertTrue(stream.available() > 0);
 //        stream.close();
 //        
 //        assertNotNull(newVersion.getAuthor());
 //        
 //        newVersion.setProperty("foo2", "bar");
 //        assertEquals("bar", newVersion.getProperty("foo2"));
 //        assertNull(version.getProperty("foo2"));
 //        
 //        ArtifactImpl a2 = (ArtifactImpl) registry.resolve(workspace, artifact.getName());
 //        assertNotNull(a2);
 //        
 //        version.setAsDefaultVersion();
 //        
 //        assertEquals(2, a2.getVersions().size());
 //        EntryVersion activeVersion = a2.getDefaultOrLastVersion();
 //        assertEquals("0.1", activeVersion.getVersionLabel());
 //        
 //        activeVersion.delete();
 //        
 //        assertEquals(1, a2.getVersions().size());
 //        
 //        activeVersion = a2.getDefaultOrLastVersion();
 //        assertNotNull(activeVersion);
 //        
 //        assertTrue(((JcrVersion)activeVersion).isLatest());
 //        
 //        Collection<Item> artifacts = a2.getParent().getItems();
 //        boolean found = false;
 //        for (Item a : artifacts) {
 //            if (a.getId().equals(a2.getId())) {
 //                found = true;
 //                break;
 //            }
 //        }
 //        assertTrue(found);
 //        a2.delete();
     }
 //
 //    /**
 //     * Test for http://mule.mulesource.org/jira/browse/GALAXY-54 .
 //     */
 //    public void testActiveVersion() throws Exception
 //    {
 //        Collection<Item> workspaces = registry.getItems();
 //        assertEquals(1, workspaces.size());
 //
 //        Item workspace = workspaces.iterator().next();
 //
 //        String version1 = "This is version 1";
 //        String version2 = "This is version 2";
 //
 //        ByteArrayInputStream bais = new ByteArrayInputStream(version1.getBytes("UTF-8"));
 //
 //        NewItemResult ar = workspace.createArtifact("text/plain",
 //                                                     "test.txt",
 //                                                     "1",
 //                                                     bais);
 //        assertNotNull(ar);
 //        assertTrue(ar.isApproved());
 //
 //        bais = new ByteArrayInputStream(version2.getBytes());
 //
 //        final ArtifactImpl artifact = (ArtifactImpl) ar.getItem();
 //
 //        ar = artifact.newVersion(bais, "2");
 //        assertNotNull(ar);
 //        assertTrue(ar.isApproved());
 //
 //        assertNotNull(ar.getEntryVersion().getPrevious());
 //        
 //        artifact.getVersion("1").setAsDefaultVersion();
 //
 //        ArtifactImpl a = (ArtifactImpl) registry.resolve(workspace, "test.txt");
 //        assertNotNull(a);
 //        assertEquals("1", a.getDefaultOrLastVersion().getVersionLabel());
 //        assertEquals(version1, IOUtils.readStringFromStream(((ArtifactVersion)a.getDefaultOrLastVersion()).getStream()));
 //
 //        ArtifactVersion artifactVersion = (ArtifactVersion) a.getVersion("2");
 //        assertEquals(version2, IOUtils.readStringFromStream(artifactVersion.getStream()));
 //    }
 //
 //
 //    public void testAddNonUnderstood() throws Exception {
 //        InputStream logProps = getResourceAsStream("/log4j.properties");
 //        
 //        Collection<Item> workspaces = registry.getItems();
 //        assertEquals(1, workspaces.size());
 //        Item workspace = workspaces.iterator().next();
 //        
 //        NewItemResult ar = workspace.createArtifact("text/plain", 
 //                                                     "log4j.properties", 
 //                                                     "0.1", 
 //                                                     logProps);
 //        
 //        assertNotNull(ar);
 //    }
 
 }
