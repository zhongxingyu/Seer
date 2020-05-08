 /*
  * Copyright 2007 BoxySystems Inc. <siddique@boxysystems.com>
  *                  http://www.BoxySystems.com
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
 
 package com.boxysystems.libraryfinder.model;
 
 /**
  * Constants used by the library finder
  *
  * @author: Siddique Hameed
  * @since: Dec 7, 2005
  */
 
 public interface Constants {
   public static final String PLUGIN_ID = "Library Finder";
   public static final String CLASS_FILE_EXT = ".class";
   public static final String ZIP_FILE_EXT = ".zip";
   public static final String JAR_FILE_EXT = ".jar";
   public static final String PATH_NOT_FOUND_MESSAGE = "The specified path does not exist !!!";
   public static final String DATA_NOT_FOUND_MESSAGE = "No library found for the given search criteria !!!";
   public static final String PATH_NOT_VALID_MESSAGE = "The specified path is not a directory !!!";
   public static final String FINDING_LIBRARY_MESSAGE = "Finding Library...";
   public static final String ACTION_CANCELLED_MESSAGE = "Cancelled!";
   public static final String ACTION_COMPLETED_MESSAGE = "Completed!";
   public static final String REGEX_OR_SEPARATOR = "|";
   public static final String PLUGIN_HOME_PAGE = "http://plugins.intellij.net/plugin/?id=51";
   public static final String SEARCH_MSG_PREFIX = "Searching in ";
   public static final String ERR_MSG_PREFIX = "Error opening zip file ";
   public static final String INVALID_PATTERN_MSG_PREFIX = "Invalid pattern syntax, Error Message : ";
   public static final String NEW_LINE = System.getProperty("line.separator");
   public static final String FIND_LIBRARY_TITLE = "Find Library";
   public static final String CLASSPATH_SEPARATOR = System.getProperty("path.separator");
  public static final String DEFAULT_EXCLUDED_FOLDERS = "CVS;SCCS;RCS;rcs;.DS_Store;.svn;vssver.scc;vssver2.scc;.git;.idea";
   public static final String OS = System.getProperty("os.name");
 
   // Tracking actions
   public static final String PLUGIN_LOAD_ACTION = "PluginLoad";
   public static final String FIND_LIBRARY_ACTION = "FindLibraryDialog";
   public static final String FIND_LIBRARY_INTENTION_ACTION = "FindLibraryIntentionAction";
   public static final String FIND_LIBRARIES_INTENTION_ACTION = "FindLibrariesIntentionAction";
   public static final String CONFIG_FORM = "ConfigForm";
 }
