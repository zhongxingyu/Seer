 /*
  * Copyright (c) 2012 Dominik Obermaier.
  *
  *      Licensed under the Apache License, Version 2.0 (the "License");
  *      you may not use this file except in compliance with the License.
  *      You may obtain a copy of the License at
  *
  *          http://www.apache.org/licenses/LICENSE-2.0
  *
  *      Unless required by applicable law or agreed to in writing, software
  *      distributed under the License is distributed on an "AS IS" BASIS,
  *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *      See the License for the specific language governing permissions and
  *      limitations under the License.
  */
 
 package org.tomighty.plugin.statistics.core.config;
 
 import org.apache.commons.io.FileUtils;
 
 import javax.inject.Singleton;
 import java.io.File;
 
 @Singleton
 public class Directories {
 
     private static final String STATS_DIR_NAME = "stats";
    private static final String STATISTICS_PLUGIN_DIR_NAME = "statistics";
     private static final String PLUGIN_DIR_NAME = "plugins";
     private static final String TOMIGHTY_DIR_NAME = ".tomighty";
 
     private final File statsDirectory;
 
 
     public Directories() {
 
         final File tomightyDir = new File(FileUtils.getUserDirectory(), TOMIGHTY_DIR_NAME);
 
         final File pluginDir = new File(tomightyDir, PLUGIN_DIR_NAME);
 
         final File statisticsPluginDir = new File(pluginDir, STATISTICS_PLUGIN_DIR_NAME);
 
         statsDirectory = new File(statisticsPluginDir, STATS_DIR_NAME);
 
         statsDirectory.mkdirs();
     }
 
     public File getStatsDirectory() {
         return statsDirectory;
     }
 }
