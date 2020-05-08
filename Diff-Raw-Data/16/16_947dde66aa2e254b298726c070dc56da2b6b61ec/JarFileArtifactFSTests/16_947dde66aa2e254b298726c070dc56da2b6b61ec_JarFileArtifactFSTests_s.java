 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.artifact.fs.internal;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Scanner;
 
 import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
 import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;
 import org.junit.Test;
 
 public class JarFileArtifactFSTests {
 
     private final FileArtifactFS artifactFS = new JarFileArtifactFS(new File("src/test/resources/artifacts/simple.jar"));
 
     @Test(expected = IllegalArgumentException.class)
     public void constructorDirectory() {
         new JarFileArtifactFS(new File("target"));
     }
 
     /**
      * This usage is not expected, but is tested to ensure predictable behaviour.
      */
     @Test
     public void constructorNonJarFile() {
         ArtifactFS fileArtifactFS = new JarFileArtifactFS(new File("src/test/resources/properties/foo.properties"));
         File file = fileArtifactFS.getFile();
         assertEquals("foo.properties", file.getName());
         ArtifactFSEntry entry = fileArtifactFS.getEntry("");
         assertTrue(entry instanceof FileArtifactFSEntry);
         assertFalse(entry instanceof JarFileArtifactFSEntry);
         ArtifactFSEntry badEntry = fileArtifactFS.getEntry("blah");
         assertFalse(badEntry.exists());
     }
 
     /**
      * Just in case manifests are somehow special cased in the implementation.
      */
     @Test
     public void getManifestEntry() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("META-INF/MANIFEST.MF");
         InputStream inputStream = entry.getInputStream();
         String manifest = new Scanner(inputStream).useDelimiter("\\A").next();
         assertEquals(
             "Manifest-Version: 1.0\nCreated-By: 1.6.0_07 (Apple Inc.)\nBundle-Name: test\nBundle-SymbolicName: test\nBundle-Version: 0.0.0\n\n",
             manifest);
         assertEquals("MANIFEST.MF", entry.getName());
         assertEquals("META-INF/MANIFEST.MF", entry.getPath());
     }
 
     @Test
     public void getNormalEntry() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("test/rawfile");
         InputStream inputStream = entry.getInputStream();
         String rawfile = new Scanner(inputStream).useDelimiter("\\A").next();
         assertEquals("rawfile", rawfile);
         assertFalse(entry.isDirectory());
         assertEquals("rawfile", entry.getName());
         assertEquals("test/rawfile", entry.getPath());
         assertTrue(entry.exists());
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void getOutputStream() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("test/rawfile");
         entry.getOutputStream();
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void delete() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("test/rawfile");
         entry.delete();
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void getEntryArtifactFS() {
         this.artifactFS.getFile();
         ArtifactFSEntry entry = this.artifactFS.getEntry("test/rawfile");
         entry.getArtifactFS();
     }
 
     @Test
     public void getDirectoryEntry() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("test/");
         assertTrue(entry.isDirectory());
         assertEquals("test", entry.getName());
         assertEquals("test/", entry.getPath());
         assertTrue(entry.exists());
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void getDirectoryInputStream() throws IOException {
         ArtifactFSEntry entry = this.artifactFS.getEntry("test/");
         entry.getInputStream();
     }
 
     @Test
     public void getDirectoryChildren() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("test/");
         ArtifactFSEntry[] children = entry.getChildren();
         assertEquals(1, children.length);
         assertEquals("rawfile", children[0].getName());
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void getFileChildren() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("test/rawfile");
         ArtifactFSEntry[] children = entry.getChildren();
         assertEquals(0, children.length);
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void getChildrenOfNonExistentFile() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch");
         ArtifactFSEntry[] children = entry.getChildren();
         assertEquals(0, children.length);
     }
 
     @Test
     public void getPathOfNonExistentFile() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch");
         assertEquals("x/nosuch", entry.getPath());
     }
 
     @Test
     public void getNameOfNonExistentFile() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch");
         assertEquals("nosuch", entry.getName());
     }
 
     @Test
     public void isDirectoryOfNonExistentFile() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch");
         assertFalse(entry.isDirectory());
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void getInputStreamOfNonExistentFile() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch");
         entry.getInputStream();
     }
 
     @Test
     public void getExistenceOfNonExistentFile() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch");
         assertFalse(entry.exists());
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void getChildrenOfNonExistentDirectory() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch/");
         ArtifactFSEntry[] children = entry.getChildren();
         assertEquals(0, children.length);
     }
 
     @Test
     public void getPathOfNonExistentDirectory() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch/");
         assertEquals("x/nosuch/", entry.getPath());
     }
 
     @Test
     public void getNameOfNonExistentDirectory() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch/");
         assertEquals("nosuch", entry.getName());
     }
 
     @Test
     public void isDirectoryOfNonExistentDirectory() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch/");
         assertFalse(entry.isDirectory());
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void getInputStreamOfNonExistentDirectory() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch/");
         entry.getInputStream();
     }
 
     @Test
     public void getExistenceOfNonExistentDirectory() {
         ArtifactFSEntry entry = this.artifactFS.getEntry("x/nosuch/");
         assertFalse(entry.exists());
     }
 
     @Test
     public void getEntryNull() {
         assertTrue(this.artifactFS.getEntry(null) instanceof FileArtifactFSEntry);
     }
 
     @Test
     public void getFile() {
         File file = this.artifactFS.getFile();
         assertEquals("simple.jar", file.getName());
     }
 }
