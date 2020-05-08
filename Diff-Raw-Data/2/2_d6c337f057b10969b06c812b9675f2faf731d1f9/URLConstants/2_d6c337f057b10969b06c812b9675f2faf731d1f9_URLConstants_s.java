 package ru.terra.spending.constants;
 
 public class URLConstants
 {
 	public class Pages
 	{
 		public static final String SPRING_LOGIN = "/spending/do.login";
 		public static final String LOGIN = "login";
 		public static final String TRANSACTIONS = "transactions";
 		public static final String HOME = "/";
		public static final String REGISTER = "register";
 	}
 
 	public class DoJson
 	{
 		public static final String LOGIN_DO_LOGIN_JSON = "/login/do.login.json";
 		public static final String LOGIN_DO_REGISTER_JSON = "/login/do.register.json";
 		public static final String LOGIN_DO_GET_MY_ID = "/login/do.getmyid.json";
 
 		public class MobileTransactions
 		{
 			public class MT_GET_TR
 			{
 				public static final String URL = "/mobiletransaction/get.transactions.json";
 				public static final String PARAM_USER = "user";
 			}
 
 			public class MT_REG_TR
 			{
 				public static final String URL = "/mobiletransaction/do.transaction.register.json";
 				public static final String PARAM_UID = "uid";
 				public static final String PARAM_TYPE = "type";
 				public static final String PARAM_MONEY = "money";
 				public static final String PARAM_DATE = "date";
 			}
 		}
 		
 		public static final String TYPES_GET_TYPES_JSON = "/types/get.types.json";
 	}
 
 	public class Views
 	{
 		public static final String LOGIN = "login";
 		public static final String TRANSACTIONS = "transactions";
 		public static final String HOME = "home";
 		public static final String REGISTER = "reg";
 	}
 }
