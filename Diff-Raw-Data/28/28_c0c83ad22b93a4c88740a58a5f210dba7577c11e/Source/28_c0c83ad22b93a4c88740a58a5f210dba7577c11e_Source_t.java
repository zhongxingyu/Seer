 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.jasmin.model;
 
 import net.oneandone.sushi.fs.Node;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.Properties;
 
 /** Per-classpath item info. */
 public class Source {
     public static Source load(Node properties, Node base) throws IOException {
         Properties p;
         String groupId;
         String artifactId;
         String version;
         String scm;
 
         properties.checkExists();
         try (Reader src = properties.createReader()) {
             p = new Properties();
             p.load(src);
         }
         groupId = get(p, "groupId");
         artifactId = get(p, "artifactId");
         version = get(p, "version");
         scm = get(p, "scmConnection");
         return new Source(base, groupId, artifactId, version, scm);
     }
 
     private static String get(Properties p, String key) {
         String value;
 
         value = p.getProperty(key);
         if (value == null) {
             throw new IllegalArgumentException("unknown key: " + key);
         }
         return value;
     }
 
     /** root node searched for files. */
     public final Node classpathBase;
 
     public final String groupId;
     public final String artifactId;
     public final String version;
     public final String scm;
 
     public Source(Node classpathBase, String groupId, String artifactId, String version, String scm) {
         this.classpathBase = classpathBase;
         this.groupId = groupId;
         this.artifactId = artifactId;
         this.version = version;
         this.scm = scm;
     }
 }
