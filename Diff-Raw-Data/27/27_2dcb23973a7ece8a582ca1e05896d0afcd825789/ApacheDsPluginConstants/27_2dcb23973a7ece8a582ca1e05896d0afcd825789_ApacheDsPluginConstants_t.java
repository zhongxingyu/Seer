 /*
  *  Licensed to the Apache Software Foundation (ASF) under one
  *  or more contributor license agreements.  See the NOTICE file
  *  distributed with this work for additional information
  *  regarding copyright ownership.  The ASF licenses this file
  *  to you under the Apache License, Version 2.0 (the
  *  "License"); you may not use this file except in compliance
  *  with the License.  You may obtain a copy of the License at
  *  
  *    http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing,
  *  software distributed under the License is distributed on an
  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *  KIND, either express or implied.  See the License for the
  *  specific language governing permissions and limitations
  *  under the License. 
  *  
  */
 package org.apache.directory.studio.apacheds;
 
 

 /**
  * This interface stores all the constants used in the plugin.
  *
  * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
  * @version $Rev$, $Date$
  */
 public interface ApacheDsPluginConstants
 {
    /** The plug-in ID */
    public static final String PLUGIN_ID = ApacheDsPlugin.getDefault().getPluginProperties().getString( "Plugin_id" );
    
     // ------
     // IMAGES
     // ------
     public static final String IMG_SERVER_NEW = "resources/icons/server_new.gif";
     public static final String IMG_SERVER_NEW_WIZARD = "resources/icons/server_new_wizard.png";
     public static final String IMG_SERVER = "resources/icons/server.gif";
     public static final String IMG_SERVER_STARTED = "resources/icons/server_started.gif";
     public static final String IMG_SERVER_STARTING1 = "resources/icons/server_starting1.gif";
     public static final String IMG_SERVER_STARTING2 = "resources/icons/server_starting2.gif";
     public static final String IMG_SERVER_STARTING3 = "resources/icons/server_starting3.gif";
     public static final String IMG_SERVER_STOPPED = "resources/icons/server_stopped.gif";
     public static final String IMG_SERVER_STOPPING1 = "resources/icons/server_stopping1.gif";
     public static final String IMG_SERVER_STOPPING2 = "resources/icons/server_stopping2.gif";
     public static final String IMG_SERVER_STOPPING3 = "resources/icons/server_stopping3.gif";
     public static final String IMG_RUN = "resources/icons/run.gif";
     public static final String IMG_STOP = "resources/icons/stop.gif";
     public static final String IMG_CREATE_CONNECTION = "resources/icons/connection_new.gif";
 
     // --------
     // COMMANDS
     // --------
     public static final String CMD_NEW_SERVER = ApacheDsPlugin.getDefault().getPluginProperties().getString(
         "Cmd_NewServer_id" );
     public static final String CMD_RUN = ApacheDsPlugin.getDefault().getPluginProperties().getString( "Cmd_Run_id" );
     public static final String CMD_STOP = ApacheDsPlugin.getDefault().getPluginProperties().getString( "Cmd_Stop_id" );
     public static final String CMD_PROPERTIES = ApacheDsPlugin.getDefault().getPluginProperties().getString(
         "Cmd_Properties_id" );
     public static final String CMD_OPEN_CONFIGURATION = ApacheDsPlugin.getDefault().getPluginProperties().getString(
         "Cmd_OpenConfiguration_id" );
     public static final String CMD_DELETE = ApacheDsPlugin.getDefault().getPluginProperties().getString(
         "Cmd_Delete_id" );;
     public static final String CMD_RENAME = ApacheDsPlugin.getDefault().getPluginProperties().getString(
         "Cmd_Rename_id" );;
     public static final String CMD_CREATE_CONNECTION = ApacheDsPlugin.getDefault().getPluginProperties().getString(
         "Cmd_CreateConnection_id" );
 
     // --------------
     // PROPERTY PAGES
     // --------------
     public static final String PROP_SERVER_PROPERTY_PAGE = ApacheDsPlugin.getDefault().getPluginProperties().getString(
         "Prop_ServerPropertyPage_id" );
 
     // -----
     // VIEWS
     // -----
     public static final String VIEW_SERVERS_VIEW = ApacheDsPlugin.getDefault().getPluginProperties().getString(
         "View_ServersView_id" );
 
     // --------
     // CONTEXTS
     // --------
     public static final String CONTEXTS_SERVERS_VIEW = ApacheDsPlugin.getDefault().getPluginProperties().getString(
         "Ctx_ServersView_id" );
 
     // -----------
     // PREFERENCES
     // -----------
     /** The Preference ID for the Colors and Font Debug Font setting */
     public static final String PREFS_COLORS_AND_FONTS_DEBUG_FONT = "org.apache.directory.studio.apacheds.prefs.colorAndFonts.debugFont";
     /** The Preference ID for the Colors and Font Debug Color setting */
     public static final String PREFS_COLORS_AND_FONTS_DEBUG_COLOR = "org.apache.directory.studio.apacheds.prefs.colorAndFonts.debugColor";
 
     /** The Preference ID for the Colors and Font Info Font setting */
     public static final String PREFS_COLORS_AND_FONTS_INFO_FONT = "org.apache.directory.studio.apacheds.prefs.colorAndFonts.infoFont";
     /** The Preference ID for the Colors and Font Info Color setting */
     public static final String PREFS_COLORS_AND_FONTS_INFO_COLOR = "org.apache.directory.studio.apacheds.prefs.colorAndFonts.infoColor";
 
     /** The Preference ID for the Colors and Font Warn Font setting */
     public static final String PREFS_COLORS_AND_FONTS_WARN_FONT = "org.apache.directory.studio.apacheds.prefs.colorAndFonts.warnFont";
     /** The Preference ID for the Colors and Font Warn Color setting */
     public static final String PREFS_COLORS_AND_FONTS_WARN_COLOR = "org.apache.directory.studio.apacheds.prefs.colorAndFonts.warnColor";
 
     /** The Preference ID for the Colors and Font Error Font settings */
     public static final String PREFS_COLORS_AND_FONTS_ERROR_FONT = "org.apache.directory.studio.apacheds.prefs.colorAndFonts.errorFont";
     /** The Preference ID for the Colors and Font Error Color setting */
     public static final String PREFS_COLORS_AND_FONTS_ERROR_COLOR = "org.apache.directory.studio.apacheds.prefs.colorAndFonts.errorColor";
 
     /** The Preference ID for the Colors and Font Fatal Font setting */
     public static final String PREFS_COLORS_AND_FONTS_FATAL_FONT = "org.apache.directory.studio.apacheds.prefs.colorAndFonts.fatalFont";
     /** The Preference ID for the Colors and Font Fatal Color setting */
     public static final String PREFS_COLORS_AND_FONTS_FATAL_COLOR = "org.apache.directory.studio.apacheds.prefs.colorAndFonts.fatalColor";
 
     /** The Preference ID for the Servers Logs Level setting */
     public static final String PREFS_SERVER_LOGS_LEVEL = "org.apache.directory.studio.apacheds.prefs.serverLogs.level";
     /** The Preference ID for the Servers Logs Level Debug value */
     public static final String PREFS_SERVER_LOGS_LEVEL_DEBUG = "debug";
     /** The Preference ID for the Servers Logs Level Info value */
     public static final String PREFS_SERVER_LOGS_LEVEL_INFO = "info";
     /** The Preference ID for the Servers Logs Level Warn value */
     public static final String PREFS_SERVER_LOGS_LEVEL_WARN = "warning";
     /** The Preference ID for the Servers Logs Level Error value */
     public static final String PREFS_SERVER_LOGS_LEVEL_ERROR = "error";
     /** The Preference ID for the Servers Logs Level Fatal value */
     public static final String PREFS_SERVER_LOGS_LEVEL_FATAL = "fatal";
 
     /** The Preference ID for the Servers Logs Pattern setting */
     public static final String PREFS_SERVER_LOGS_PATTERN = "org.apache.directory.studio.apacheds.prefs.serverLogs.pattern";
 }
