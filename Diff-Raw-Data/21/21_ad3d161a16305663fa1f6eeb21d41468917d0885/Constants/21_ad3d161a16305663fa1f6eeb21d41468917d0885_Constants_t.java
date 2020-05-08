 package com.hyperactivity.android_app;
 
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lucas
  * Date: 3/25/13
  * Time: 2:17 PM
  */
 public class Constants {
     public class Log {
         public static final String TAG = "android-app"; //To be used as tag with all android log calls.
     }
 
     public class Server {
        public static final String IP = "130.229.182.25";
         public static final String PORT = "12345";
         public static final int TIMEOUT = 10000;
         public static final boolean USE_LOCAL_DEVELOPMENT_SERVER = true;
     }
 
     public class Methods {
         public static final String LOGIN = "login";
         public static final String REGISTER = "register";
         public static final String GET_PROFILE = "get_profile";
         public static final String UPDATE_PROFILE = "update_profile";
         public static final String GET_CATEGORY_CONTENT = "get_category_content";
         public static final String GET_THREAD = "get_thread";
         public static final String CREATE_THREAD = "create_thread";
         public static final String CREATE_REPLY = "create_reply";
         public static final String GET_FORUM_CONTENT = "get_forum_content";
         public static final String GET_LATEST_THREADS = "get_latest_threads";
         public static final String GET_ACCOUNT = "get_account";
         public static final String CREATE_CATEGORY = "create_category";
         public static final String MODIFY_CATEGORY = "modify_category";
         public static final String DELETE_CATEGORY = "delete_category";
         public static final String GET_THREAD_CONTENT = "get_thread_content";
         public static final String MODIFY_THREAD = "modify_thread";
         public static final String DELETE_THREAD = "delete_thread";
         public static final String MODIFY_REPLY = "modify_reply";
         public static final String DELETE_REPLY = "delete_reply";
     }
 
     public class Transfer {
         public static final String TOKEN = "token";
         public static final String FAILED = "failed";
         public static final String ACCOUNT = "account";
         public static final String ACCOUNT_ID = "account_id";
         public static final String PROFILE = "profile";
         public static final String ID = "id";
         public static final String RESULT = "result";
         public static final String EMAIL = "email";
         public static final String USERNAME = "username";
         public static final String FACEBOOK_ID = "facebookID";
         public static final String USER_ID = "userID";
         public static final String SUCCESS = "success";
         public static final String DESCRIPTION = "description";
         public static final String SHOW_BIRTHDATE = "show_birthdate";
         public static final String AVATAR = "avatar";
         public static final String TYPE = "type";
         public static final String PRIVATE = "private";
         public static final String PUBLIC = "public";
         public static final String CATEGORIES = "categories";
         public static final String CATEGORY_ID = "category_id";
         public static final String THREADS = "threads";
         public static final String THREAD_ID = "thread_id";
         public static final String THREAD = "thread";
         public static final String HEADLINE = "headline";
         public static final String TEXT = "text";
         public static final String PARENT_CATEGORY = "parent_category";
         public static final String PARENT_THREAD = "parent_thread";
         public static final String STATUS = "status";
         public static final String LIMIT = "limit";
         public static final String COLOR_CODE = "color_code";
         public static final String SORT_TYPE = "sort_type";
         public static final String REPLY_ID = "reply_id";
         public static final String THUMB_UP = "thumb_up";
         public static final String REPLIES = "replies";
         public static final String REPLY = "reply";
     }
 }
