 package ru.terra.jbrss.constants;
 
 public class URLConstants {
	public static final String SERVER_URL = "http://terranout.ath.cx/jbrss/";
 
 	public class Pages {
 		public static final String SPRING_LOGIN = "/jbrss/do.login";
 		public static final String HOME = "/";
 		public static final String ABOUT = "about";
 		public static final String CATEGORY = "category";
 		public static final String ERROR404 = "error404";
 		public static final String LOGIN = "login";
 		public static final String JBRSS_HOME = "jbrss";
 	}
 
 	public class DoJson {
 		public class Login {
 			public static final String LOGIN_DO_LOGIN_JSON = "/login/do.login.json";
 			public static final String LOGIN_DO_REGISTER_JSON = "/login/do.register.json";
 			public static final String LOGIN_DO_GET_MY_ID = "/login/do.getmyid.json";
 
 			public static final String LOGIN_PARAM_USER = "user";
 			public static final String LOGIN_PARAM_PASS = "pass";
 			public static final String LOGIN_PARAM_CAPTCHA = "captcha";			
 			public static final String LOGIN_PARAM_CAPVAL = "capval";
 		}
 
 		public class Rss {
 			public static final String RSS_DO_ADD = "/rss/do.add.json";
 			public static final String RSS_DO_DEL = "/rss/do.del.json";
 			public static final String RSS_DO_UPDATE = "/rss/do.update.json";
 			public static final String RSS_DO_MARK_READ = "/rss/do.markread.json";
 			public static final String RSS_DO_LIST = "/rss/do.list.json";
 			public static final String RSS_DO_GET_UNREAD = "/rss/do.get.unread.json";
 			
 			public static final String RSS_PARAM_TIMESTAMP = "timestamp";
 		}
 
 		public class Captcha {
 			public static final String CAP_GET = "/captcha/do.get.json";
 			public static final String CAP_CHECK = "/captcha/do.check.json";
 			public static final String CAP_PARAM_ID = "id";
 			public static final String CAP_PARAM_VAL = "val";
 		}
 	}
 
 	public class Views {
 		public static final String ABOUT = "about";
 		public static final String CATEGORY = "category";
 		public static final String ERROR404 = "error404";
 		public static final String LOGIN = "login";
 		public static final String JBRSS_HOME = "jbrss";
 	}
 }
