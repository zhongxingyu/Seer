 /**
  * Copyright (C) 2012 Future Invent Informationsmanagement GmbH. All rights
  * reserved. <http://www.fuin.org/>
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option) any
  * later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.fuin.srcmixins4j.core;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.emftext.language.java.JavaClasspath;
 import org.emftext.language.java.resource.JaMoPPUtil;
 import org.junit.BeforeClass;
 
 /**
  * Base class for mixin tests.
  */
 public abstract class AbstractSrcMixins4JTest {
    
     @BeforeClass
     public static void beforeClass() throws IOException {
         JaMoPPUtil.initialize();
     }
     
     /**
      * Returns a resource set for the given directory.
      * 
      * @param dir Root source directory.
      * 
      * @return Resource set.
      * 
      * @throws IOException Error creating a canonical root path.
      */
     protected static ResourceSet createResourceSet(final File dir)
             throws IOException {
         final ResourceSet set = new ResourceSetImpl();
 
         // Add the resource folder with test classes
         JavaClasspath.get(set).registerSourceOrClassFileFolder(
                 URI.createFileURI(dir.getCanonicalPath()));
 
         // Add annotations source folder
         final File parentDir = new File(new File("..").getCanonicalPath());
         final File annotationsDir = new File(parentDir,
                 "srcmixins4j-annotations");
         final File annotationsJavaDir = new File(annotationsDir,
                 "src/main/java");
         JavaClasspath.get(set).registerSourceOrClassFileFolder(
                 URI.createFileURI(annotationsJavaDir.getCanonicalPath()));
 
         return set;
     }
 
 }
