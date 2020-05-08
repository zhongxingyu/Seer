 /*******************************************************************************
 * Copyright (c) 2011 Nokia Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Comarch team - initial API and implementation
 *******************************************************************************/
 package org.ned.client;
 
 import com.sun.lwuit.Font;
 import com.sun.lwuit.util.Resources;
 
 public class NedResources {
 
     static private NedResources mInstance;
 
     private Resources res;
 
     public static NedResources getInstance() {
         if( mInstance == null ) {
             mInstance = new NedResources();
         }
         return mInstance;
     }
 
     public void setRes(Resources resources) {
         res = resources;
     }
 
     public static Resources getRes() {
         if( mInstance == null ) {
             mInstance = new NedResources();
         }
         return mInstance.res;
     }
 
     public static Font getFont(String id) {
         if( mInstance == null ) {
             mInstance = new NedResources();
         }
         return mInstance.res.getFont(id);
     }
 
     public static final String TITLE_FONT = "titleFont";
     public static final String LIST_FONT = "New Font";
     public static final String BUTTON_SELECTED_FONT = "buttonSelFont";
     public static final String BUTTON_FONT = "buttonFont";
 
 
     public static final String GLOBAL_CONNECTING = Localization.getMessage("GLOBAL_CONNECTING");
     public static final String GLOBAL_UPDATING = Localization.getMessage("GLOBAL_UPDATING");
 
     public static final String MID_TITLE = Localization.getMessage("MID_TITLE");
     public static final String MID_DEFAULTMOTD = Localization.getMessage("MID_DEFAULTMOTD");
 
     public static final String MID_EXIT_COMMAND = Localization.getMessage("MID_EXIT_COMMAND");
     public static final String MID_BACK_COMMAND = Localization.getMessage("MID_BACK_COMMAND");
     public static final String MID_OK_COMMAND = Localization.getMessage("MID_OK_COMMAND");
     public static final String MID_OPTIONS_COMMAND = Localization.getMessage("MID_OPTIONS_COMMAND");
     public static final String MID_REMOVE_LIB_COMMAND = Localization.getMessage("MID_REMOVE_LIB_COMMAND");
     public static final String MID_REMOVE_COMMAND = Localization.getMessage("MID_REMOVE_COMMAND");
     public static final String MID_SEARCH_COMMAND = Localization.getMessage("MID_SEARCH_COMMAND");
     public static final String MID_OK_SEARCH_COMMAND = Localization.getMessage("MID_OK_SEARCH_COMMAND");
     public static final String MID_CANCEL_SEARCH_COMMAND = Localization.getMessage("MID_CANCEL_SEARCH_COMMAND");
     public static final String MID_STATISTICS_COMMAND = Localization.getMessage("MID_STATISTICS_COMMAND");
 
     public static final String MID_ANSWER_YES = Localization.getMessage("MID_ANSWER_YES");
     public static final String MID_ANSWER_NO = Localization.getMessage("MID_ANSWER_NO");
 
     public static final String MID_EMPTY_CATALOGUE = Localization.getMessage("MID_EMPTY_CATALOGUE");
     public static final String MID_EMPTY_CATEGORY = Localization.getMessage("MID_EMPTY_CATEGORY");
 
     public static final String MID_VIDEO_DOWNLOAD = Localization.getMessage("MID_VIDEO_DOWNLOAD");
     public static final String MID_VIDEO_PLAY = Localization.getMessage("MID_VIDEO_PLAY");
 
     public static final String MID_AUTOMATIC = Localization.getMessage("MID_AUTOMATIC");
     public static final String MID_MANUAL = Localization.getMessage("MID_MANUAL");
 
     public static final String MID_ADDED_DOWNLOAD_MESSAGE = "MID_ADDED_DOWNLOAD_MESSAGE";
 
     public static final String MID_REMOVE_ENTRY_QUESTION = Localization.getMessage("MID_REMOVE_ENTRY_QUESTION");
     public static final String MID_REMOVING_ENTRY_FAILED = Localization.getMessage("MID_REMOVING_ENTRY_FAILED");
 
     public static final String MID_SETTINGS_TITLE = Localization.getMessage("MID_SETTINGS_TITLE");
     public static final String MID_DOWNLOAD_STATE_SETTINGS = Localization.getMessage("MID_DOWNLOAD_STATE_SETTINGS");
     public static final String MID_USERNAME_SETTINGS = Localization.getMessage("MID_USERNAME_SETTINGS");
     public static final String MID_PASSWORD_SETTINGS = Localization.getMessage("MID_PASSWORD_SETTINGS");
     public static final String MID_SERVER_URL_SETTINGS = Localization.getMessage("MID_SERVER_URL_SETTINGS");
     public static final String MID_AUTOMATIC_SETTINGS = Localization.getMessage("MID_AUTOMATIC_SETTINGS");
     public static final String MID_MANUAL_SETTINGS = Localization.getMessage("MID_MANUAL_SETTINGS");
     public static final String MID_DOWNLOAD_TIME_SETTINGS = Localization.getMessage("MID_DOWNLOAD_TIME_SETTINGS");
     public static final String MID_SCHEDULER_SETTINGS = Localization.getMessage("MID_SCHEDULER_SETTINGS");
     public static final String MID_ON_SETTINGS = Localization.getMessage("MID_ON_SETTINGS");
     public static final String MID_OFF_SETTINGS = Localization.getMessage("MID_OFF_SETTINGS");
     public static final String MID_QUEUE_MESSAGE_SETTINGS = Localization.getMessage("MID_QUEUE_MESSAGE_SETTINGS");
     public static final String MID_SERVER_MESSAGE_SETTINGS = Localization.getMessage("MID_SERVER_MESSAGE_SETTINGS");
 
     public static final String MID_DOWNLOADS = Localization.getMessage("MID_DOWNLOADS");
 
     public static final String MID_EXIT_QUEUE_MESSAGE = Localization.getMessage("MID_EXIT_QUEUE_MESSAGE");
     public static final String MID_EMPTY_QUEUE_MESSAGE = Localization.getMessage("MID_EMPTY_QUEUE_MESSAGE");
 
     public static final String MID_SEARCH_TITLE = Localization.getMessage("MID_SEARCH_TITLE");
     public static final String MID_SEARCH_TEXT = Localization.getMessage("MID_SEARCH_TEXT");
     public static final String MID_SEARCH_RESULT_MESSAGE = "MID_SEARCH_RESULT_MESSAGE";
 
     public static final String MID_UPDATE_DIALOG_TITLE = Localization.getMessage("MID_UPDATE_DIALOG_TITLE");
     public static final String MID_UPDATE_DIALOG_TEXT = Localization.getMessage("MID_UPDATE_DIALOG_TEXT");
 
     public static final String STAT_TITLE = Localization.getMessage("STAT_TITLE");
     public static final String STAT_NUMBER_OF_FILES = Localization.getMessage("STAT_NUMBER_OF_FILES");
 
     public static final String VC_BACK = Localization.getMessage("VC_BACK");
     public static final String VC_PAUSE = Localization.getMessage("VC_PAUSE");
     public static final String AC_BACK = Localization.getMessage("AC_BACK");
     public static final String AC_PAUSE = Localization.getMessage("AC_PAUSE");
 
     public static final String TRA_START_DOWNLOAD = Localization.getMessage("TRA_START_DOWNLOAD");
     public static final String TRA_REMOVE_DOWNLOAD = Localization.getMessage("TRA_REMOVE_DOWNLOAD");
     public static final String TRA_TITLE = Localization.getMessage("TRA_TITLE");
     public static final String TRA_TOTAL_BYTES = Localization.getMessage("TRA_TOTAL_BYTES");
     public static final String TRA_BYTES_DOWNLOADED = Localization.getMessage("TRA_BYTES_DOWNLOADED");
     public static final String TRA_PERCENT_DOWNLOADED = Localization.getMessage("TRA_PERCENT_DOWNLOADED");
     public static final String TRA_STATUS = Localization.getMessage("TRA_STATUS");
     public static final String TRA_WAITING_STATUS = Localization.getMessage("TRA_WAITING_STATUS");
     public static final String TRA_CHECKING_STATUS = Localization.getMessage("TRA_CHECKING_STATUS");
     public static final String TRA_ERROR_STATUS = Localization.getMessage("TRA_ERROR_STATUS");
     public static final String TRA_CONNECTING_STATUS = Localization.getMessage("TRA_CONNECTING_STATUS");
     public static final String TRA_CONNECTED_STATUS = Localization.getMessage("TRA_CONNECTED_STATUS");
     public static final String TRA_CANCELLING_STATUS = Localization.getMessage("TRA_CANCELLING_STATUS");
     public static final String TRA_COMPLETED_STATUS = Localization.getMessage("TRA_COMPLETED_STATUS");
     public static final String TRA_PAUSE_DOWNLOAD = Localization.getMessage("TRA_PAUSE_DOWNLOAD");
     public static final String TRA_REMOVE_DOWNLOAD_DIALOG = Localization.getMessage("TRA_REMOVE_DOWNLOAD_DIALOG");
     public static final String TRA_DOWNLOAD_LIMIT_DIALOG = Localization.getMessage("TRA_DOWNLOAD_LIMIT_DIALOG");
 
     public static final String SCH_MESSAGE = Localization.getMessage("SCH_MESSAGE" );
 
     public static final String DLM_CONNECTION_FAILED = Localization.getMessage("DLM_CONNECTION_FAILED");
     public static final String DLM_UPDATE_COMPLETED = Localization.getMessage("DLM_UPDATE_COMPLITED");
     public static final String DLM_UPDATE_FAILED = Localization.getMessage("DLM_UPDATE_FAILED");
 
     public static final String SUCCESFULUPLOAD = Localization.getMessage("DLM_SUCCESFULUPLOAD");
     public static final String UPLOADFAILED = Localization.getMessage("DLM_UPLOADFAILED");
     public static final String STATNOTMODIFIED = Localization.getMessage("DLM_STATNOTMODIFIED");
     public static final String DLM_STATSUPDATED = Localization.getMessage("DLM_STATSUPDATED");
     public static final String DLM_NEWSTATS = Localization.getMessage("DLM_NEWSTATS");
     public static final String DLM_MISSINGIMEI = Localization.getMessage("DLM_MISSINGIMEI");
     public static final String DLM_SERVERINTERNALERROR = Localization.getMessage(" DLM_SERVERINTERNALERROR");
     public static final String DLM_UNKNOWN = Localization.getMessage("DLM_UNKNOWN");
     public static final String MID_UPLOAD_COMMAND = Localization.getMessage("MID_UPLOAD_COMMAND");
 
     public static final String BAD_LOGIN = Localization.getMessage("BAD_LOGIN");
     public static final String LOGIN = Localization.getMessage("LOGIN");
     public static final String CATALOGS_NO_UNKNOWN = Localization.getMessage("CATALOGS_NO_UNKNOWN");
     public static final String CATALOGS = Localization.getMessage("CATALOGS");
     public static final String MEDIA_ITEMS = Localization.getMessage("MEDIA_ITEMS");
     public static final String CATEGORIES = Localization.getMessage("CATEGORIES");
     public static final String ADD_LIBRARY = Localization.getMessage("ADD_LIBRARY");
     public static final String SHOW_LIBRARY = Localization.getMessage("SHOW_LIBRARY");
     public static final String LIBRARY_ID_= Localization.getMessage("LIBRARY_ID_");
     public static final String LIBRARY_MANAGER = Localization.getMessage("LIBRARY_MANAGER");
     public static final String CONNECTING = Localization.getMessage("CONNECTING");
     public static final String LIBRARY_NOT_EXISTS = Localization.getMessage("LIBRARY_NOT_EXISTS");
     public static final String USER_NAME = Localization.getMessage("USER_NAME");
     public static final String PASSWORD = Localization.getMessage("PASSWORD");
     public static final String USER_AUTHENTICATION = Localization.getMessage("USER_AUTHENTICATION");
     public static final String INFO = Localization.getMessage("INFO");
     public static final String WARNING = Localization.getMessage("WARNING");
     public static final String ERROR = Localization.getMessage("ERROR");
     public static final String QUESTION = Localization.getMessage("QUESTION");
     public static final String ALL_FILES_DOWNLOADED = Localization.getMessage("ALL_FILES_DOWNLOADED");
     public static final String DOWNLOAD_ALL = Localization.getMessage("DOWNLOAD_ALL");
     public static final String DOC_NOT_SUPPORTED = Localization.getMessage("DOC_NOT_SUPPORTED");
     public static final String DEFAULT_MOTD = Localization.getMessage("DEFAULT_MOTD");
     public static final String NO_MEMORY_CARD = Localization.getMessage("NO_MEMORY_CARD");
     public static final String SHOW_DETAILS = Localization.getMessage("SHOW_DETAILS");
     public static final String SHOW_LINKS = Localization.getMessage("SHOW_LINKS");
     public static final String QUESTION_REMOVE_CONTENT = Localization.getMessage("QUESTION_REMOVE_CONTENT");
     public static final String START = Localization.getMessage("START");
     public static final String PAUSE = Localization.getMessage("PAUSE");
     public static final String REMOVE = Localization.getMessage("REMOVE");
     public static final String DELETE = Localization.getMessage("DELETE");
     public static final String HISTORY = Localization.getMessage("HISTORY");
     public static final String DOWNLOAD_OPTIONS = Localization.getMessage("DOWNLOAD_OPTIONS");
     public static final String DOWNLOADS = Localization.getMessage("DOWNLOADS");
     public static final String SWITCH_USER = Localization.getMessage("SWITCH_USER");
     public static final String QUESTION_LOGOUT_USER = Localization.getMessage("QUESTION_LOGOUT_USER");
     public static final String QUESTION_REMOVE_USER = Localization.getMessage("QUESTION_REMOVE_USER");
     public static final String CHECK_SERVER = Localization.getMessage("CHECK_SERVER");
     public static final String CHECK_FOR_UPDATE = Localization.getMessage("CHECK_FOR_UPDATE");
     public static final String NO_LIBRARIES = Localization.getMessage("NO_LIBRARIES");
     public static final String LIBRARIES = Localization.getMessage("LIBRARIES");
     public static final String NO_DETAILS = Localization.getMessage("NO_DETAILS");
     public static final String ENLARGE = Localization.getMessage("ENLARGE");
     public static final String FIT_TO_SCREEN = Localization.getMessage("FIT_TO_SCREEN");
     public static final String SERVER_WIZARD = Localization.getMessage("SERVER_WIZARD");
     public static final String ENTER_SERVER_ADDRESS = Localization.getMessage("ENTER_SERVER_ADDRESS");
     public static final String AC_PLAY = Localization.getMessage("AC_PLAY");
     public static final String OPEN = Localization.getMessage("OPEN");
     public static final String CANCEL = Localization.getMessage("CANCEL");
     public static final String SELECT = Localization.getMessage("SELECT");
     public static final String LIBRARYMANAGER = Localization.getMessage("LIBRARYMANAGER");
     public static final String USER_DELETED = Localization.getMessage("USER_DELETED");
     public static final String OPEN_LINK = Localization.getMessage("OPEN_LINK");
     public static final String DOWNLOAD_NEW_LIBRARY = Localization.getMessage("DOWNLOAD_NEW_LIBRARY");
     public static final String MOVING_FILES = Localization.getMessage("MOVING_FILES");
     public static final String LIBRARY_UPTODATE = Localization.getMessage("LIBRARY_UPTODATE");
     public static final String ADDTOQUEUE = Localization.getMessage("ADDTOQUEUE");
     public static final String BROWSE = Localization.getMessage("BROWSE");
     public static final String DOWNLOAD_NOW = Localization.getMessage("DOWNLOAD_NOW");
     public static final String UNSUPPORTED_MEDIA_FORMAT = Localization.getMessage("UNSUPPORTED_MEDIA_FORMAT");
     public static final String NO_DOWNLOADS = Localization.getMessage("NO_DOWNLOADS");
     public static final String NO_HISTORY = Localization.getMessage("NO_HISTORY");
     public static final String QUESTION_REMOVE_LIBRARY = Localization.getMessage("QUESTION_REMOVE_LIBRARY");
     public static final String REMOVE_USER = Localization.getMessage("REMOVE_USER");
     public static final String FACTORY_SETTINGS = Localization.getMessage("FACTORY_SETTINGS");
     public static final String NO_LINKS = Localization.getMessage("NO_LINKS");
     public static final String MEMORY_OUT = Localization.getMessage("MEMORY_OUT");
     public static final String ACCESS_DENIED = Localization.getMessage("ACCESS_DENIED");
     public static final String NO_ITEMS_FOUND = Localization.getMessage("NO_ITEMS_FOUND");
     public static final String STATISTICS_SENDING_MODE = Localization.getMessage("STATISTICS_SENDING_MODE");
     public static final String STATISTICS_OPTIONS = Localization.getMessage("STATISTICS_OPTIONS");
     public static final String SEARCH_FOR = Localization.getMessage("SEARCH_FOR");
     public static final String ITEM_ADDED_TO_QUEUE = "ITEM_ADDED_TO_QUEUE";
     public static final String LOCAL = Localization.getMessage("LOCAL");
     public static final String REMOTE = Localization.getMessage("REMOTE");
     public static final String GO_TO_START = Localization.getMessage("GO_TO_START");
     public static final String QUESTION_FACTORY = Localization.getMessage("QUESTION_FACTORY");
     public static final String QUESTION_FACTORY2 = Localization.getMessage("QUESTION_FACTORY2");
     public static final String HELP = Localization.getMessage("HELP");
     public static final String LOGIN_AGAIN = Localization.getMessage("LOGIN_AGAIN");
     public static final String LOGIN_ONLINE = Localization.getMessage("LOGIN_ONLINE");
     public static final String EXIT_CONFIRM_MESSAGE = Localization.getMessage("EXIT_CONFIRM_MESSAGE");
     public static final String TOO_MANY_DOWNLOADS = Localization.getMessage("TOO_MANY_DOWNLOADS");
     public static final String LIB_NOT_EXIST_ANY_MORE = Localization.getMessage("LIB_NOT_EXIST_ANY_MORE");
     public static final String TRA_FILESIZE = Localization.getMessage("TRA_FILESIZE");
     public static final String TRA_UNKNOWN_SIZE = Localization.getMessage("TRA_UNKNOWN_SIZE");
     public static final String ABOUT = Localization.getMessage("ABOUT");
     public static final String VERSION = Localization.getMessage("VERSION");
     public static final String REMEMBERME = Localization.getMessage("REMEMBERME");
     public static final String REMOVEALL = Localization.getMessage("REMOVEALL");
     public static final String REMOVEALL_DOWNLOAD_DIALOG = Localization.getMessage("TRA_REMOVEALL_DOWNLOAD_DIALOG");
 
     public static final String CONNECTIONERROR = Localization.getMessage("CONNECTIONERROR");
     public static final String LOCALSECURITY = Localization.getMessage("LOCALSECURITY");
     public static final String ABORTED = Localization.getMessage("ABORTED");
     public static final String OTHERCONNECTIONPROBLEM = Localization.getMessage("OTHERCONNECTIONPROBLEM");
     public static final String UNKNOWN = Localization.getMessage("UNKNOWN");
 
     public NedResources() {
     }
 }
