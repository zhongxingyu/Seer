 /*
  * Copyright 2010 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.util;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import com.google.jstestdriver.model.HandlerPathPrefix;
 
 /**
  * A simple method to easy writing html. Replace with a proper templating system.
  * 
  * @author corbinrsmith@gmail.com (Cory Smith)
  */
 public class HtmlWriter {
   public static String QUIRKS =
     "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">";
   public static String STRICT =
     "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/DTD/strict.dtd\">";
   private final Writer writer;
   private final HandlerPathPrefix prefix;
 
   public HtmlWriter(Writer writer, HandlerPathPrefix prefix) {
     this.writer = writer;
     this.prefix = prefix;
   }
 
   public HtmlWriter writeStrictDtd() throws IOException {
     writer.append(STRICT)
       .append("<html>");
     return this;
   }
 
   public HtmlWriter writeQuirksDtd() throws IOException {
     writer.append(QUIRKS)
       .append("<html>");
     return this;
   }
 
   public HtmlWriter startHead() throws IOException {
     writer.append("<head>");
     return this;
   }
 
   public HtmlWriter writeTitle(String title) throws IOException {
     writer.append("<title>")
       .append(title)
       .append("</title>");
     return this;
   }
 
   public HtmlWriter writeStyleSheet(String path) throws IOException {
     writer.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"")
       .append(prefix.prefixPath(path))
       .append("\"/>");
     return this;
   }
 
   public HtmlWriter finishHead() throws IOException {
     writer.append("</head>");
     return this;
   }
 
   public HtmlWriter startBody() throws IOException {
     writer.append("<body>");
     return this;
   }
 
   public HtmlWriter writeIframe(String id, String src) throws IOException {
     writer.append("<iframe id=\"")
       .append(id)
       .append("\" src=\"")
       .append(prefix.prefixPath(src))
       .append("\" frameborder=\"0\"></iframe>");
     return this;
   }
 
   public HtmlWriter finishBody() throws IOException {
     writer.append("</body>");
     return this;
   }
   
   public HtmlWriter writeExternalScript(String path) throws IOException {
     writer.append("<script src=\"")
       .append(prefix.prefixPath(path))
       .append("\" type=\"text/javascript\"></script>");
     return this;
   }
 
   public void flush() throws IOException {
     writer.append("</html>");
     writer.flush();
   }
 
   public HtmlWriter writeScript(String script) throws IOException {
     writer.append("<script type=\"text/javascript\">")
      .append(prefix.prefixPath(script))
       .append("</script>");
     return this;
   }
 }
