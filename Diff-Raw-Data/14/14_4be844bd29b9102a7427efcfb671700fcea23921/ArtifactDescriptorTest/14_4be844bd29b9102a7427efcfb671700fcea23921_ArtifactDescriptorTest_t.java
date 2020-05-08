 /*
  * (C) Copyright 2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Julien Carsique
  *
  */
 
 package org.nuxeo.build.maven;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 /**
  *
  */
 public class ArtifactDescriptorTest {
 
     /**
      * Test method for
      * {@link org.nuxeo.build.maven.ArtifactDescriptor#parse(java.lang.String)}.
      * Check read of artifact's coordinates given in one String (named key).
      * Key is of the form: groupId:artifactId:version:type:classifier:scope
      */
     @Test
     public void testParse() {
         String[] keys = new String[] {
                 "org.nuxeo:test:1.1:zip:classifier:provided",
                 "org.nuxeo:test:1.1:zip:classifier", "org.nuxeo:test:1.1:zip",
                 "org.nuxeo:test:1.1", "org.nuxeo:test::zip", "org.nuxeo:test",
                 "org.nuxeo:test::zip:classifier:provided",
                 "org.nuxeo:test::jar:classifier:provided",
                "org.nuxeo:test::jar:classifier", "org.nuxeo:test:::classifier" };
         String[] results = new String[] {
                 "org.nuxeo:test:1.1:zip:classifier:provided",
                 "org.nuxeo:test:1.1:zip:classifier:compile",
                 "org.nuxeo:test:1.1:zip:null:compile",
                 "org.nuxeo:test:1.1:jar:null:compile",
                 "org.nuxeo:test:null:zip:null:compile",
                 "org.nuxeo:test:null:jar:null:compile",
                 "org.nuxeo:test:null:zip:classifier:provided",
                 "org.nuxeo:test:null:jar:classifier:provided",
                "org.nuxeo:test:null:jar:classifier:compile",
                 "org.nuxeo:test:null:jar:classifier:compile" };
         for (int i = 0; i < keys.length; i++) {
             // System.out.println(keys[i] + " => "
             // + new ArtifactDescriptor(keys[i]).toString());
             System.out.println("Test: " + keys[i]);
             assertEquals(results[i], new ArtifactDescriptor(keys[i]).toString());
         }
     }
 
 }
