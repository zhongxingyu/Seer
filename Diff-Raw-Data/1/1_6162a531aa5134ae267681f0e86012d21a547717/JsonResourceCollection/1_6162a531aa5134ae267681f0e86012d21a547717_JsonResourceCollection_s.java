 package fnug.servlet;
 
 import java.util.LinkedList;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.codehaus.jackson.annotate.JsonPropertyOrder;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
 
 import com.googlecode.jslint4java.JSLintResult;
 
 import fnug.resource.HasJSLintResult;
 import fnug.resource.Resource;
 import fnug.resource.ResourceCollection;
 
 /*
  Copyright 2010 Martin Algesten
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 
 @JsonPropertyOrder({ "name", "compJs", "compCss", "files" })
 @JsonSerialize(include = Inclusion.NON_NULL)
 public class JsonResourceCollection {
 
     @JsonProperty
     String name;
     @JsonProperty
     String compJs;
     @JsonProperty
     String compCss;
     @JsonProperty
     LinkedList<JsonResourceCollectionFile> files = new LinkedList<JsonResourceCollectionFile>();
 
     public JsonResourceCollection(ResourceCollection c) {
 
         name = c.getBundle().getName();
         if (c.getCompressedJs().getLastModified() > 0) {
             compJs = c.getCompressedJs().getFullPath();
         }
         if (c.getCompressedCss().getLastModified() > 0) {
             compCss = c.getCompressedCss().getFullPath();
         }
 
         for (Resource r : c.getAggregates()) {
             files.add(new JsonResourceCollectionFile(r));
         }
         if (files.isEmpty()) {
             files = null;
         }
     }
 }
 
 @JsonSerialize(include = Inclusion.NON_NULL)
@SuppressWarnings("unused")
 class JsonResourceCollectionFile {
     @JsonProperty
     private String path;
     @JsonProperty
     private String lint;
 
     public JsonResourceCollectionFile(Resource r) {
         path = r.getPath();
         if (r instanceof HasJSLintResult) {
             JSLintResult partResult = ((HasJSLintResult) r).getJSLintResult();
             if (partResult != null && !partResult.getReport().isEmpty()) {
                 lint = filter(partResult.getReport());
             }
         }
     }
 
     private String filter(String html) {
         if (html == null) {
             return null;
         }
         html = html.replace("<br>", "");
 
         // functions are warnings, not errors, we can't afford to keep that in
         // the bootstrap json
         int functions = html.indexOf("<div id=functions>");
         if (functions >= 0) {
             html = html.substring(0, functions);
         }
 
         html = html.trim();
         if (html.isEmpty()) {
             return null;
         } else {
             return html;
         }
     }
 
 }
