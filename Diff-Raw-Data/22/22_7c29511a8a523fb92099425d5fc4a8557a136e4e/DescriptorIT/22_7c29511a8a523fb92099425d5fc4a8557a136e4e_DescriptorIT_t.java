 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.maven.plugins.prerelease.core;
 
 import net.oneandone.maven.plugins.prerelease.maven.Maven;
 import net.oneandone.maven.plugins.prerelease.util.IntegrationBase;
 import net.oneandone.sushi.fs.file.FileNode;
 import org.apache.maven.project.MavenProject;
 import org.junit.Test;
 
 import java.net.URI;
 
 import static org.junit.Assert.assertEquals;
 
 public class DescriptorIT extends IntegrationBase {
     @Test
     public void normal() throws Exception {
         FileNode dir;
         Maven maven;
         MavenProject project;
         long revision;
         Descriptor descriptor;
 
         dir = checkoutProject("minimal");
         maven = Maven.withDefaults(WORLD);
         project = maven.loadPom(dir.join("pom.xml"));
         revision = WorkingCopy.load(dir).revision(); // TODO: expensive
         descriptor = Descriptor.checkedCreate(WORLD, project, revision, new Schedule());
         assertEquals(revision, descriptor.revision);
         assertEquals("1.0.0-SNAPSHOT", descriptor.previous);
         assertEquals("minimal", descriptor.project.artifactId);
        assertEquals("net.oneandone.maven.plugins.prerelease", descriptor.project.groupId);
         assertEquals("1.0.0", descriptor.project.version);
         assertEquals("1.0.1-SNAPSHOT", descriptor.next);
         assertEquals(REPOSITORY_URL + "/minimal/trunk", descriptor.svnOrig);
         assertEquals(REPOSITORY_URL + "/minimal/tags/minimal-1.0.0", descriptor.svnTag);
     }
 
     @Test(expected = TagAlreadyExists.class)
     public void tagAlreadyException() throws Exception {
         FileNode dir;
         Maven maven;
         MavenProject project;
         long revision;
         URI tag;
 
         dir = checkoutProject("minimal");
         maven = Maven.withDefaults(WORLD);
         project = maven.loadPom(dir.join("pom.xml"));
         revision = WorkingCopy.load(dir).revision(); // TODO: expensive
         Descriptor.checkedCreate(WORLD, project, revision, new Schedule());
         tag = new URI(REPOSITORY_URL + "/minimal/tags/minimal-1.0.0");
         svnMkdir(tag);
         try {
             Descriptor.checkedCreate(WORLD, project, revision, new Schedule());
         } finally {
             svnRemove(tag);
         }
     }
 
     @Test(expected = VersioningProblem.class)
     public void parentSnapshot() throws Exception {
         FileNode dir;
         Maven maven;
         MavenProject project;
         long revision;
 
         dir = checkoutProject("parentSnapshot");
         maven = Maven.withDefaults(WORLD);
         project = maven.loadPom(dir.join("pom.xml"));
         revision = WorkingCopy.load(dir).revision(); // TODO: expensive
         Descriptor.checkedCreate(WORLD, project, revision, new Schedule());
     }
 
     @Test(expected = VersioningProblem.class)
     public void dependencySnapshot() throws Exception {
         FileNode dir;
         Maven maven;
         MavenProject project;
         long revision;
 
         dir = checkoutProject("dependencySnapshot");
         maven = Maven.withDefaults(WORLD);
         project = maven.loadPom(dir.join("pom.xml"));
         revision = WorkingCopy.load(dir).revision(); // TODO: expensive
         Descriptor.checkedCreate(WORLD, project, revision, new Schedule());
     }
 
     @Test(expected = VersioningProblem.class)
     public void pluginSnapshot() throws Exception {
         FileNode dir;
         Maven maven;
         MavenProject project;
         long revision;
 
         dir = checkoutProject("pluginSnapshot");
         maven = Maven.withDefaults(WORLD);
         project = maven.loadPom(dir.join("pom.xml"));
         revision = WorkingCopy.load(dir).revision(); // TODO: expensive
         Descriptor.checkedCreate(WORLD, project, revision, new Schedule());
     }
 }
