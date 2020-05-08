 /*
  * Copyright 2013 The Last Crusade ContactLastCrusade@gmail.com
  * 
  * This file is part of SoundStream.
  * 
  * SoundStream is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SoundStream is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SoundStream.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.thelastcrusade.soundstream.components;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 
 import com.thelastcrusade.soundstream.R;
 
 /**
  * @author Elizabeth
  *
  */
 public class InstructionsDialog {
     private Activity activity;
     private static final int[] MESSAGEIDS = {R.string.welcome, R.string.welcome_connection, 
             R.string.welcome_music, R.string.welcome_network };
    private static final int WELCOME_INDEX = 0;
     
     public InstructionsDialog(Activity activity){
         this.activity = activity;
     }
     public void show(){
        showInstructions(WELCOME_INDEX);
     }
     
     private void showInstructions(final int messageIndex){
         String positiveString = messageIndex < MESSAGEIDS.length-1?  "Next" : "Done";
        String negativeString = messageIndex > WELCOME_INDEX ?  "Back" : "Skip";
         
         new AlertDialog.Builder(activity)
             .setMessage(MESSAGEIDS[messageIndex])
             .setPositiveButton(positiveString,
             new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog,int which) {
                    //as long as we haven't hit the last message, we should move to the next one
                    if(messageIndex < MESSAGEIDS.length-1){ 
                         showInstructions(messageIndex+1);    
                     }
                 }
              })
         .setNegativeButton(negativeString, new DialogInterface.OnClickListener() {
             
             @Override
             public void onClick(DialogInterface dialog, int which) {
                if(messageIndex > WELCOME_INDEX ){
                     showInstructions(messageIndex-1);    
                 }  
             }
         }).show();
     }
 
 }
