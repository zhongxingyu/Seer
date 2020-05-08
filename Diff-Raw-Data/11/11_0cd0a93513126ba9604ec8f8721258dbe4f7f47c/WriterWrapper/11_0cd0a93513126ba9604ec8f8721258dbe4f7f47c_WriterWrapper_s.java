 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.cachedetails;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 public class WriterWrapper {
     private Writer mWriter;
 
     public void close() throws IOException {
         mWriter.close();
     }
 
     public void open(String path) throws IOException {
         mWriter = new BufferedWriter(new FileWriter(path), 4000);
     }
 
     public void write(String str) throws IOException {
         try {
             mWriter.write(str);
         } catch (IOException e) {
             throw new IOException("Error writing line '" + str + "'");
         }
     }
}
