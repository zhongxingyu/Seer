 package org.apache.maven.plugin.cmake.ng;
 
 /*
  * Copyright 2012 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Goal which removes existing build products.
  *
  * @goal clean
  * @phase clean
  */
 public class CleanMojo extends AbstractMojo {
   /**
    * Location of the build products.
    *
    * @parameter expression="${output}"
    * @required
    */
   private File output;
 
   void recursiveDelete(File f) throws IOException {
     if (f.isDirectory()) {
       for (File c : f.listFiles()) {
         recursiveDelete(c);
       }
     }
     if (!f.delete()) {
       throw new IOException("Failed to delete file: " + f);
     }
   }
 
   public void execute() throws MojoExecutionException {
     try {
       recursiveDelete(output);
     } catch (IOException e) {
       throw new MojoExecutionException("Error removing output directory '" +
           output + "'", e);
     }
   }
 }
