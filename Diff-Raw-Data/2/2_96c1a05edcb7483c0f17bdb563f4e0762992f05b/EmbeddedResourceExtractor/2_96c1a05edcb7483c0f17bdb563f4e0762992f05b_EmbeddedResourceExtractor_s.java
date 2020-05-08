 /*
  * Copyright 2000-2012 Octopus Deploy Pty. Ltd.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package octopus.teamcity.agent;
 
 import java.io.*;
 
 public class EmbeddedResourceExtractor {
     public void extractTo(String destinationPath) throws IOException {
        extractFile("/resources/Octo.exe", destinationPath + "\\Octo.exe");
         extractFile("/resources/Octo.exe.config", destinationPath + "\\Octo.exe.config");
     }
 
     private void extractFile(String resourceName, String destinationName) throws IOException {
         File file = new File(destinationName);
         if (file.exists())
             return;
 
         InputStream is = getClass().getResourceAsStream(resourceName);
         OutputStream os = new FileOutputStream(destinationName, false);
 
         byte[] buffer = new byte[4096];
         int length;
         while ((length = is.read(buffer)) > 0) {
             os.write(buffer, 0, length);
         }
 
         os.close();
         is.close();
     }
 }
