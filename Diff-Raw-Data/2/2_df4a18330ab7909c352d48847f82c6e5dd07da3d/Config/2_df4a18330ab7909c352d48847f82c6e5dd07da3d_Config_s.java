 /*
  * Copyright 2012 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.android.apps.iosched;
 
 public class Config {
     // OAuth 2.0 related config
 	/*
 	 * Google+ configuration
 	 */
 //    public static final String APP_NAME = "Google-IO-Android-App";
 //    public static final String API_KEY = "AIzaSyCXjH2cfTuClN0OTbJpF1XnHbhdPQ6wg4Y"; // from the APIs console
 //    public static final String CLIENT_ID = "237695054204.apps.googleusercontent.com"; // from the APIs console
 
     
     public static final String APP_NAME = "GDGDevFest-Android-App";
     public static final String API_KEY = "AIzaSyCsAij0bSMlGHdta3snhfxD4rAOw9WeSDg"; // from the APIs console
     public static final String CLIENT_ID = "903246180582.apps.googleusercontent.com"; // from the APIs console
 
     // Conference API-specific config
     // NOTE: the backend used for the Google I/O 2012 Android app is not currently open source, so
     // you should modify these fields to reflect your own backend.
     private static final String CONFERENCE_API_KEY = "AIzaSyA2MhtOhocnrkFvc_uyavMbrLj_Qi36Vak";
 //    private static final String ROOT_EVENT_ID = "googleio2012";
     private static final String ROOT_EVENT_ID = "gdgdevfest2012";
     
 //    private static final String BASE_URL = "https://google-developers.appspot.com/_ah/api/resources/v0.1";
     //private static final String BASE_URL = "https://kupriyanov7/_ah/api/resources/v0.1";
 //    private static final String BASE_URL = "http://192.168.178.34:8082/api";
 //    private static final String BASE_URL = "http://192.168.178.42:8082";
 //    private static final String BASE_URL = "http://www.devfest.info/json";
     private static final String BASE_URL = "http://www.devfest.at/json";
      
     public static final String GET_ALL_SESSIONS_URL      = BASE_URL + "/sessions?parent_event=" + ROOT_EVENT_ID + "&api_key=" + CONFERENCE_API_KEY;
     public static final String GET_ALL_SPEAKERS_URL      = BASE_URL + "/speakers?event_id=" + ROOT_EVENT_ID + "&api_key=" + CONFERENCE_API_KEY;
     public static final String GET_ALL_ANNOUNCEMENTS_URL = BASE_URL + "/announcements?parent_event=" + ROOT_EVENT_ID + "&api_key=" + CONFERENCE_API_KEY;
     public static final String EDIT_MY_SCHEDULE_URL      = BASE_URL + "/editmyschedule/o/";
 
     
     //public static final String BASE_SESSION_URL      =  "https://developers.google.com/events/io/sessions/";
     public static final String BASE_SESSION_URL      =  BASE_URL + "/events/io/sessions/";
     
     // Static file host for the sandbox data
     //public static final String GET_SANDBOX_URL = "https://developers.google.com/events/io/sandbox-data";
     public static final String GET_SANDBOX_URL = BASE_URL + "/events/io/sandbox-data?parent_event=" + ROOT_EVENT_ID + "&api_key=" + CONFERENCE_API_KEY;
     
 
     // YouTube API config
     public static final String YOUTUBE_API_KEY = "API_KEY";
     // YouTube share URL
     public static final String YOUTUBE_SHARE_URL_PREFIX = "http://youtu.be/";
 
     // Livestream captions config
     public static final String PRIMARY_LIVESTREAM_CAPTIONS_URL = "TODO";
     public static final String SECONDARY_LIVESTREAM_CAPTIONS_URL = "TODO";
     public static final String PRIMARY_LIVESTREAM_TRACK = "android";
     public static final String SECONDARY_LIVESTREAM_TRACK = "chrome";
 
     // GCM config
 //    public static final String GCM_SERVER_URL = "https://iosched-gcm.appspot.com";
 //    public static final String GCM_SENDER_ID = "1009579170083"; // project ID from the APIs console
     
     public static final String GCM_SERVER_URL = "http://2012.mkupriyanov.appspot.com";
     public static final String GCM_SENDER_ID = "903246180582"; // project ID from the APIs console
 
 
 //	public static final String AUTH_TOKEN_SCOPE = "https://www.googleapis.com/auth/developerssite";
 //	public static final String AUTH_TOKEN_SCOPE_READONLY = "https://www.googleapis.com/auth/developerssite.readonly";
 
 	public static final String AUTH_TOKEN_SCOPE = "https://192.168.178.42/auth/gdgdevfest";
 	public static final String AUTH_TOKEN_SCOPE_READONLY = "https://192.168.178.42/auth/gdgdevfest.readonly";
 	
 	public static final boolean FEATURE_MAP_ENABLED = false;
 	public static final boolean FEATURE_SESSION_URL_ENABLED = false;
 	public static final boolean FEATURE_PLUSONE_ENABLED = false;
 	
	public static final String GOOGLE_ANALYTICS = "UA-35397535-1";
 	
 	
 	
 
 
 }
