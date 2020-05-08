 /*
 Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 
 This program is licensed to you under the Apache License Version 2.0, 
 and you may not use this file except in compliance with the Apache License Version 2.0. 
 You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 
 Unless required by applicable law or agreed to in writing, 
 software distributed under the Apache License Version 2.0 is distributed on an 
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
 
 package org.sonatype.plexus.build.incremental;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 
 import org.codehaus.plexus.util.DirectoryScanner;
 import org.codehaus.plexus.util.Scanner;
 
 /**
  * Filesystem based non-incremental build context implementation which behaves as if all files
  * were just created. More specifically, 
  * 
  * hasDelta returns <code>true</code> for all paths
  * newScanner returns Scanner that scans all files under provided basedir
  * newDeletedScanner always returns empty scanner.
  * isIncremental returns <code>false</code<
  * getValue always returns <code>null</code>
  * 
  * @plexus.component role="org.sonatype.plexus.build.incremental.BuildContext"
  *                   role-hint="default"
  */
public class DefaultBuildContext implements BuildContext {
 
   public boolean hasDelta(String relpath) {
     return true;
   }
 
   public boolean hasDelta(File file) {
     return true;
   }
 
   public boolean hasDelta(List relpaths) {
     return true;
   }
 
   public OutputStream newFileOutputStream(File file) throws IOException {
     return new FileOutputStream(file);
   }
 
   public Scanner newScanner(File basedir) {
     DirectoryScanner ds = new DirectoryScanner();
     ds.setBasedir(basedir);
     return ds;
   }
 
   public void refresh(File file) {
     // do nothing
   }
 
   public Scanner newDeleteScanner(File basedir) {
     return new EmptyScanner(basedir);
   }
 
   public Scanner newScanner(File basedir, boolean ignoreDelta) {
     return newScanner(basedir);
   }
 
   public boolean isIncremental() {
     return false;
   }
 
   public Object getValue(String key) {
     return null;
   }
 
   public void setValue(String key, Object value) {
   }
 
   public void addWarning(File file, int line, int column, String message, Throwable cause) {
   }
 
   public void addError(File file, int line, int column, String message, Throwable cause) {
   }
 
   public boolean isUptodate(File target, File source) {
     return target != null && target.exists() && source != null && source.exists()
         && target.lastModified() > source.lastModified();
   }
 }
