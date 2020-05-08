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
 
 package org.apache.hcatalog.hcatmix;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.hcatalog.hcatmix.conf.HiveTableSchema;
 
 import java.io.File;
 
 public class HCatMixUtils {
     /**
      * The returned location would be a directory in case of map reduce mode, otherwise a file in case of
      * local mode
      * @param outputDir
      * @param tableName
      * @return
      */
     public static String getDataLocation(final String outputDir, final String tableName) {
         return outputDir + tableName;
     }
 
     /**
      * Get the script file name where PigLoader and HCatStorer is used
      * @param pigScriptDir
      * @param tableName
      * @return
      */
     public static String getHCatStoreScriptName(final String pigScriptDir, final String tableName) {
         return appendSlashIfRequired(pigScriptDir) + tableName + ".pigLoadHcatStore.pig";
     }
 
     /**
      * Get the script file name where HCatStorer and PigLoader is used
      * @param pigScriptDir
      * @param tableName
      * @return
      */
     public static String getHCatLoadScriptName(final String pigScriptDir, final String tableName) {
         return appendSlashIfRequired(pigScriptDir) + tableName + ".hcatLoadPigStore.pig";
     }
 
     /**
      * Get the script file name where the default pig PigLoader() and PigStorer() is used
      * @param pigScriptDir
      * @param tableName
      * @return
      */
     public static String getPigLoadStoreScriptName(final String pigScriptDir, final String tableName) {
         return appendSlashIfRequired(pigScriptDir) + tableName + ".pigLoadPigStore.pig";
     }
 
     /**
      * Get the script file name where HCatLoader() and HCatStorer() is used
      * @param pigScriptDir
      * @param tableName
      * @return
      */
     public static String getHCatLoadStoreScriptName(final String pigScriptDir, final String tableName) {
         return appendSlashIfRequired(pigScriptDir) + tableName + ".hcatLoadHCatStore.pig";
     }
 
     /**
      * Check that the directory name is valid and append a slash to it if required
      * @param outputDir
      * @return
      */
     public static String appendSlashIfRequired(String outputDir) {
         if(StringUtils.isEmpty(outputDir)) {
             throw new IllegalArgumentException("The directory name cannot be null/empty");
         }
         if (!outputDir.endsWith("/")) {
             outputDir += "/";
         }
         return outputDir;
     }
 
     public static String getCopyTableName(String tableName) {
         return tableName + "_copy";
     }
 
    public static String getPigOutputLocation(final String pigOutputRoot, final String tableName) {
        return HCatMixUtils.appendSlashIfRequired(pigOutputRoot) + tableName + "/";
     }
 
     public static void assertDirExists(String dirName) {
         File directory = new File(dirName);
         if(!directory.exists()) {
             throw new IllegalStateException("The directory name: " + dirName + " does not exist");
         }
 
         if(!directory.isDirectory()) {
             throw new IllegalStateException("The directory name: " + dirName + " is not a directory");
         }
     }
 }
