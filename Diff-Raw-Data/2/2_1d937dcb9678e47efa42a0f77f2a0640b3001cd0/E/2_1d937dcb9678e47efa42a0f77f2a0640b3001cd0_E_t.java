 /*
  *   Copyright 2012 Hai Bison
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package group.pals.android.lib.ui.filechooser.utils;
 
 import android.app.AlertDialog;
 import android.content.Context;
 
 /**
  * Something funny :-)
  * 
  * @author Hai Bison
  * 
  */
 public class E {
 
     /**
      * Shows it!
      * 
      * @param context
      *            {@link Context}
      */
     public static void show(Context context) {
         String msg = null;
         try {
             msg = String.format("Hi pal, you've found Easter egg  :-)\n\n"
                     + "%s v%s\n" + "...by hai bison\n\n"
                     + "http://sites.google.com/site/haitimeid/\n\n"
                     + "Hope you enjoy this library!", "android-filechooser",
                    "3.3");
         } catch (Exception e) {
             msg = "Oops... You've found a broken Easter egg, try again later  :-(";
         }
 
         new AlertDialog.Builder(context).setTitle("...").setMessage(msg).show();
     }
 }
