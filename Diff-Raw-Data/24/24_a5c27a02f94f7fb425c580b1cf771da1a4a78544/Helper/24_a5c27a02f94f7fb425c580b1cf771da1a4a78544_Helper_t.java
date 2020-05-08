 /*
 Copyright (c) 2012, Zubair Khan (governer@gmail.com) 
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 
     * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
     *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
     *
     *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package com.ds.itimer;
 
 
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.view.WindowManager;
 
 /**
  * 
  * @author zkhan
  *
  */
 public class Helper {
     
     public static final int DEGREES_PER_SEC = 3;
     public static final int SEC = 1;
     
     /**
      * 
      * @param input
      * @return
      */
     public static int fourToTime(String input) {
         int min = Integer.parseInt(input.substring(0, 2));
         int sec = Integer.parseInt(input.substring(2, 4));
         return min * 60 + sec;        
     }
 
     /**
      * 
      * @param input
      * @param newDigit
      * @return
      */
     public static String formatInput(String input, int newDigit) {
         String output = input + Integer.toString(newDigit);
         int start = output.length() - 4;
         if(start < 0) {
             start = 0;
         }
         String format = output.substring(start, output.length());
         return format;
     }
 
     /**
      * 
      * @param rate
      * @return
      */
     public static boolean isStandardRate(int rate) {
         if(rate == Helper.DEGREES_PER_SEC) {
             return true;
         }
         return false;
     }
     
     /**
      * 
      * @param input
      * @return
      */
     public static int fourToDegrees(String input) {
         int deg = Integer.parseInt(input);
         float rnd = Math.round((float)deg / (float)DEGREES_PER_SEC) * (float)DEGREES_PER_SEC;
         deg = (int)rnd;
         return deg;    
     }
 
     /**
      * 
      * @param input
      * @return
      */
     public static String degreesToFour(int input) {
         String four = String.format("%04d", input);
         return four;
     }
 
     /**
      * 
      * @param input
      * @return
      */
     public static String timeToFour(int input) {
         int min = input / 60;
         int sec = input % 60;
         String four;
         if(min > 99) {
             /*
              * No support > 2 digits
              */
            four = "0C" + String.format("%02d", sec);            
         }
         else {
             four = String.format("%02d", min) + String.format("%02d", sec);
         }
         return four;
     }
 
     /**
      * Set common features of all activities in the framework
      * @param act
      */
     public static void setOrientationAndOn(Activity act) {
         
         Preferences pref = new Preferences(act.getApplicationContext());
         if(pref.shouldScreenStayOn()) {
             act.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);            
         }
 
         if(pref.isPortrait()) {
             act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);            
         }
         else {
             act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         }
 
     }    
 }
