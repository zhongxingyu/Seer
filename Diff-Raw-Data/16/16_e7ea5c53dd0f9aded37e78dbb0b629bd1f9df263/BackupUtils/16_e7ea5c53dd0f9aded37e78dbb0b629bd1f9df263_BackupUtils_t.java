 /*
  * Copyright (C) 2008 Esmertec AG.
  * Copyright (C) 2008 The Android Open Source Project
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
 package com.android.settings;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 public class BackupUtils {
 	
     
     public static boolean copyBackup(String fromFile, String toFile) throws IOException {
     
     	InputStream in = new FileInputStream(fromFile);
     	OutputStream out = new FileOutputStream(toFile);
     	try {
     	    byte[] buffer = new byte[1024];
     	    int read;
     	    while((read = in.read(buffer)) != -1){
     	      out.write(buffer, 0, read);
     	    }
     	} catch (IOException ex) {
     		return false;
     	} finally {
     		try {
     			if (in != null)
     				in.close();
     			if (out != null)
     				out.close();
 
     		} catch (IOException ex) {
         		return false;
         	}
     	}
 		return true;
   	}
     
     
     public static boolean backupExist(String File) throws IOException {
     	
     	try {        
     	InputStream in = new FileInputStream(File);
     	if (in != null) {
     		in.close();
     	}
     	} catch (IOException ex) {
     		return false;
     	}
 		return true;
   	}
 
     
     public static boolean settingsExist() throws IOException {
     	
     	try {        
      	InputStream in = new FileInputStream("data/data/com.android.settings/shared_prefs/Junk_Settings.xml");
     	if (in != null) {
     		in.close();
     	}
     	} catch (IOException ex) {
     		return false;
     	}
 		return true;
   	}    
 
     public static boolean themeOneExist() throws IOException {
     	
     	try {        
      	InputStream in = new FileInputStream("data/data/com.android.settings/shared_prefs/Junk_Theme_One.xml");
     	if (in != null) {
     		in.close();
     	}
     	} catch (IOException ex) {
     		return false;
     	}
 		return true;
   	}    
   
     public static boolean themeTwoExist() throws IOException {
     	
     	try {        
      	InputStream in = new FileInputStream("data/data/com.android.settings/shared_prefs/Junk_Theme_Two.xml");
     	if (in != null) {
     		in.close();
     	}
     	} catch (IOException ex) {
     		return false;
     	}
 		return true;
   	}
     
     public static boolean themeThreeExist() throws IOException {
     	
     	try {        
      	InputStream in = new FileInputStream("data/data/com.android.settings/shared_prefs/Junk_Theme_Three.xml");
     	if (in != null) {
     		in.close();
     	}
     	} catch (IOException ex) {
     		return false;
     	}
 		return true;
   	}
     
     

     
     
 }
