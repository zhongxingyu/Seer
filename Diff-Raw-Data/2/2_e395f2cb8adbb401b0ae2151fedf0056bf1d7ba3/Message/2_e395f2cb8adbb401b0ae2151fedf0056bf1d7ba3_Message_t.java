 package com.twitter.tokyo.kucho.daemon;
 
 public class Message {
     static String[] messages = {
             "がんばってピ",
             "あきらめないで！",
             "ファイト一発",
             "疲れたらお砂場で遊んでぴよ",
             "じゃあ巣作りにもどるぴよ",
            "はばないすでーぴよ",
            "かもめはかもめ",
             "今日も仕事がんばるピヨ",
             "ほほほのほーい",
             "これで快適ピヨ",
     };
 
     public static String getMessage() {
         return messages[(int) (System.currentTimeMillis() % messages.length)];
     }
 }
