 package com.yanchuanli.games.pokr.util;
 
 /**
  * Author: Yanchuan Li
  * Date: 5/18/12
  * Email: mail@yanchuanli.com
  */
 public class Config {
     public static int port = 9999;
     public static String serverAddress = "localhost";
     public static final byte START = '$' & 0xFF;
     public static final byte SPLIT = '|' & 0xFF;
    public static boolean offlineDebug = false;
     public static final int TYPE_LOGIN_INGAME = 0;
     public static final int TYPE_REGIST_INGAME = 1;
     public static final int TYPE_USER_INGAME = 4;
     public static final int TYPE_ACTION_INGAME = 5;
     public static final int TYPE_CARD_INGAME = 6;
 }
