 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hadoop.hoya.tools;
 
 import groovy.transform.CompileStatic;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.fs.permission.FsAction;
 import org.apache.hadoop.fs.permission.FsPermission;
 import org.apache.hadoop.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.util.Map;
 import java.util.TreeSet;
 
 /**
  * Methods to aid in config, both in the Configuration class and
  * with other parts of setting up Hoya-initated processes
  */
 @CompileStatic
 public class ConfigHelper {
   private static final Logger log = LoggerFactory.getLogger(HoyaUtils.class);
 
   public static final FsPermission CONF_DIR_PERMISSION =
     new FsPermission(FsAction.ALL,
                      FsAction.READ_EXECUTE,
                      FsAction.NONE);
 
   /**
    * Dump the (sorted) configuration
    * @param conf config
    * @return the sorted keyset
    */
   public static TreeSet<String> dumpConf(Configuration conf) {
     TreeSet<String> keys = sortedConfigKeys(conf);
     for (String key : keys) {
      log.info("$key={}", conf.get(key));
     }
     return keys;
   }
 
   public static TreeSet<String> sortedConfigKeys(Configuration conf) {
     TreeSet<String> sorted = new TreeSet<String>();
     for (Map.Entry<String, String> entry : conf) {
       sorted.add(entry.getKey());
     }
     return sorted;
   }
 
 
   /**
    * Set an entire map full of values
    * @param map map
    * @return nothing
    */
   public static void addConfigMap(Configuration self, Map<String, String> map) {
     for (Map.Entry<String, String> mapEntry : map.entrySet()) {
       self.set(mapEntry.getKey(), mapEntry.getValue());
     }
   }
 
 
   /**
    * Generate a config file in a destination directory on a given filesystem
    * @param systemConf system conf used for creating filesystems
    * @param confdir the directory path where the file is to go
    * @param filename the filename
    * @return the destination path
    */
   public static Path generateConfig(Configuration systemConf,
                                     Configuration generatingConf,
                                     Path confdir,
                                     String filename) throws IOException {
     FileSystem fs = FileSystem.get(confdir.toUri(), systemConf);
     Path destPath = new Path(confdir, filename);
     FSDataOutputStream fos = fs.create(destPath);
     try {
       generatingConf.writeXml(fos);
     } finally {
       IOUtils.closeStream(fos);
     }
     return destPath;
   }
 
   /**
    * Generate a config file in a destination directory on the local filesystem
    * @param confdir the directory path where the file is to go
    * @param filename the filename
    * @return the destination path
    */
   public static File generateConfig(Configuration generatingConf,
                                     File confdir,
                                     String filename) throws IOException {
 
 
     File destPath = new File(confdir, filename);
     OutputStream fos = new FileOutputStream(destPath);
     try {
       generatingConf.writeXml(fos);
     } finally {
       IOUtils.closeStream(fos);
     }
     return destPath;
   }
 
   public static Configuration loadConfFromFile(File file) throws
                                                           MalformedURLException {
     Configuration conf = new Configuration(false);
     conf.addResource(file.toURI().toURL());
     return conf;
   }
 
   /**
    * looks for the config under confdir/templateFile; if not there
    * loads it from /conf/templateFile . 
    */
   public static Configuration loadTemplateConfiguration(Configuration systemConf,
                                                         Path confdir,
                                                         String templateFilename,
                                                         String resource) throws
                                                                          IOException {
     FileSystem fs = FileSystem.get(confdir.toUri(), systemConf);
 
     Path templatePath = new Path(confdir, templateFilename);
     Configuration conf = new Configuration(false);
     if (fs.exists(templatePath)) {
       log.debug("Loading template {}", templatePath);
       conf.addResource(templatePath);
     } else {
       log.debug("Template {} not found" +
                 " -reverting to classpath resource {}", templatePath, resource);
       conf.addResource(resource);
     }
     return conf;
   }
 
 
 }
