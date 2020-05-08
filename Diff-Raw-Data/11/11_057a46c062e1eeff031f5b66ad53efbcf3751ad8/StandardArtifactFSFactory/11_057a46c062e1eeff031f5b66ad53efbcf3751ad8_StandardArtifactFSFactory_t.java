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
 
 package org.eclipse.virgo.kernel.artifact.fs;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 
 import org.eclipse.virgo.kernel.artifact.fs.internal.DirectoryArtifactFS;
 import org.eclipse.virgo.kernel.artifact.fs.internal.FileArtifactFS;
import org.eclipse.virgo.kernel.artifact.fs.internal.JarFileArtifactFS;
 
 
 public final class StandardArtifactFSFactory implements ArtifactFSFactory {
     
     private static final List<String> JAR_EXTENSIONS = Arrays.asList("jar", "war");
 
     /**
      * {@inheritDoc}
      */
     public ArtifactFS create(File file) {
         if (file.isDirectory()) {
             return new DirectoryArtifactFS(file);
         }
         return looksLikeAJar(file.getName()) ? new JarFileArtifactFS(file) : new FileArtifactFS(file);
     }
     
     private boolean looksLikeAJar(String name) {
         String fileName = name.toLowerCase(Locale.ENGLISH);
 
         int dotLocation = fileName.lastIndexOf('.');
         if (dotLocation == -1) {
             return false;
         }
         return JAR_EXTENSIONS.contains(fileName.substring(dotLocation + 1));
     }
 
 
 }
