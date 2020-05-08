 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.web.view;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.servicemix.jbi.util.FileUtil;
 import org.springframework.web.servlet.view.AbstractView;
 
 public class DotView extends AbstractView {
 
     public static final String DEFAULT_DOT_FORMAT = "svg";
     public static final String MODEL_SOURCE = "dotSource";
     public static final String MODEL_FORMAT = "dotFormat";
     
     private String dotModelSource = MODEL_SOURCE;
     private String dotFormat = DEFAULT_DOT_FORMAT;
     
     private static final Map<String, String> FORMATS;
     
     static {
         FORMATS = new HashMap<String, String>();
         FORMATS.put("svg", "image/svg+xml");
         FORMATS.put("gif", "image/gif");
         FORMATS.put("jpg", "image/jpeg");
     }
     
     @Override
     protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
         Object o = model.get(getDotModelSource());
         if (o == null) {
             throw new IllegalStateException("Dot model source not found in '" + getDotModelSource() + "'");
         }
         if (FORMATS.get(getDotFormat()) == null) {
             throw new IllegalStateException("Unknown format: '" + getDotFormat() + "'");
         }
         File dotSrc = null;
         File dotImg = null;
         try {
             dotSrc = File.createTempFile("smx_", ".dot");
             FileWriter w = new FileWriter(dotSrc);
             w.write(o.toString());
             w.close();
            dotImg = File.createTempFile("smx_", ".dot." + getDotFormat());
            
            String cmd = "-T" + getDotFormat() + " \"" + dotSrc.getCanonicalPath() + "\" -o\"" + dotImg.getAbsolutePath() + "\"";
             Dot.run(cmd);
             
             InputStream is = new FileInputStream(dotImg);
             if (is.available() == 0) {
                 throw new Exception("Error while rendering dot file");
             }
             response.setContentType(getContentType());
             response.setContentLength(is.available());
             FileUtil.copyInputStream(is, response.getOutputStream());
         } finally {
             if (dotSrc != null) {
                //dotSrc.delete();
             }
             if (dotImg != null) {
                 dotImg.delete();
             }
         }
     }
     
     public String getContentType() {
         return FORMATS.get(getDotFormat());
     }
 
     public String getDotFormat() {
         return dotFormat;
     }
 
     public void setDotFormat(String dotFormat) {
         this.dotFormat = dotFormat;
     }
 
     public String getDotModelSource() {
         return dotModelSource;
     }
 
     public void setDotModelSource(String dotModelSource) {
         this.dotModelSource = dotModelSource;
     }
 
 }
