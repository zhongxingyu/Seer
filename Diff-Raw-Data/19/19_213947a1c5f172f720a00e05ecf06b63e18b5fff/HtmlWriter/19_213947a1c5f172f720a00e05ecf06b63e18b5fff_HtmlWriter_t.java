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
 
 import com.google.inject.Inject;
 
 import java.io.IOException;
 
 public class HtmlWriter {
     private final StringWriterWrapper mWriter;
 
     static final String HEADER = "<html>\n<head>\n"
            + "<script type=\"text/javascript\" src=\"file:///android_asset/rot13.js\"></script>\n"
            + "<style type='text/css'> div.hint_loading { display: none } a.hint_loading { color: gray } "
            + "  a.hint_loading:after { content: 'ing hint, please wait...' }</style>\n"
            + "</head>\n<body onLoad=encryptAll()>";
 
     @Inject
     public HtmlWriter(StringWriterWrapper writerWrapper) {
         mWriter = writerWrapper;
     }
 
     public void close() throws IOException {
         mWriter.close();
     }
 
     public void open(String path) throws IOException {
         mWriter.open(path);
     }
 
     public void writeln(String text) throws IOException {
         mWriter.write(text + "<br/>\n");
     }
 
     public void write(String text) throws IOException {
         mWriter.write(text + "\n");
     }
 
     public void writeFooter() throws IOException {
         mWriter.write("  </body>\n");
         mWriter.write("</html>\n");
     }
 
     public void writeHeader() throws IOException {
         mWriter.write(HEADER);
     }
 
     public void writeSeparator() throws IOException {
         mWriter.write("<hr/>\n");
     }
 }
