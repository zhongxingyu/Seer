 /*
  * Copyright 2010 Armin Čoralić
  * 
  * 	http://blog.coralic.nl
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /**
  * 
  */
 package nl.coralic.beta.sms.utils.objects;
 
 /**
  * @author "Armin Čoralić"
  */
 public class Const
 {
     public static final String TAG_MAIN = "BETA_SMS";
     public static final String TAG_PHH = "PHONESHANDLER";
     public static final String TAG_CPLA = "CPLISTADAPTER";
    public static final String TAG_WZD = "WIZARD";
 
     public static final int PICK_CONTACT = 1;
 
     //these are used for different things, don't change [KEY_USERNAME,KEY_PASSWORD,KEY_TO,KEY_TEXT,KEY_FROM]
     //because they are also used for http post argument names when sending sms to betamax
     public static final String KEY_USERNAME = "username";
     public static final String KEY_PASSWORD = "password";
     public static final String KEY_TO = "to";
     public static final String KEY_TEXT = "text";
     public static final String KEY_FROM = "from";
     public static final String KEY_VERIFIED = "VERIFIED";
     public static final String KEY_PROVIDERID = "PROVIDERID";
     public static final String KEY_PROVIDER = "PROVIDER";
 
     public static final int BETAMAX_FIXED_ERROR_CODE = 9999;
     public static final String BALANCE_UNKNOWN = "*";
     
     public static final String ACTION_RESP = "nl.coralic.beta.sms.REFRESH_SALDO";
 }
