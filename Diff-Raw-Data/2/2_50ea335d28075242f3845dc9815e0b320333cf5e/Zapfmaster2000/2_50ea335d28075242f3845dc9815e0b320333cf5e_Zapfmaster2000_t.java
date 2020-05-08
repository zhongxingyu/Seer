 /*
        Licensed to the Apache Software Foundation (ASF) under one
        or more contributor license agreements.  See the NOTICE file
        distributed with this work for additional information
        regarding copyright ownership.  The ASF licenses this file
        to you under the Apache License, Version 2.0 (the
        "License"); you may not use this file except in compliance
        with the License.  You may obtain a copy of the License at
 
          http://www.apache.org/licenses/LICENSE-2.0
 
        Unless required by applicable law or agreed to in writing,
        software distributed under the License is distributed on an
        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
        KIND, either express or implied.  See the License for the
        specific language governing permissions and limitations
        under the License.
  */
 
 package de.kile.zapfmaster2000.app;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import android.os.Bundle;
 import org.apache.cordova.*;
 
 public class Zapfmaster2000 extends DroidGap
 {
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         super.setIntegerProperty("splashscreen", R.drawable.zapfmaster2000);
         try {
 			if(Arrays.asList(getResources().getAssets().list("")).contains("app.html")) {
 	        	super.loadUrl("file:///android_asset/app.html",5000);
 	        }else{
 	        	super.loadUrl("file:///android_asset/webapp/app.html",5000);
 	        }
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
//        super.loadUrl("file:///android_asset/tmp/index.html",1000);
        
         
        // this.appView.getSettings().setJavaScriptEnabled(true);
        // this.appView.addJavascriptInterface(new MyPhoneGap(), "MyPhoneGap");
           
     }
 }
 
