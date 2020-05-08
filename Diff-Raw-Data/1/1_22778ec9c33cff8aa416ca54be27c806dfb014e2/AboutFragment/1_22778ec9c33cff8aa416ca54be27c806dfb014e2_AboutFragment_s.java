 /*
  * Copyright (C) 2012 The Android Open Source Project
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
 
 package com.thinkingbridge.welcome;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 public class AboutFragment extends Fragment {
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         View aboutView = inflater.inflate(R.layout.about_fragment, container, false);
 
         AboutActivity aboutActivity = new AboutActivity();
 
         TextView aboutText = (TextView) aboutView.findViewById(R.id.about);
         aboutText.setText(readAbout());
 
         // Inflate the layout for this fragment
         return aboutView;
     }
 
 
     private String readAbout() {
 try {
 			inputStream = new BufferedReader(
 			    new InputStreamReader(getResources().openRawResource(R.raw.about_thinkingbridge),"UTF-8"),512);
 		} catch (UnsupportedEncodingException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (NotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 
         int i;
         try {
             i = inputStream.read();
         while (i != -1) {
         	
            byteArrayOutputStream.write(i);
            i = inputStream.read();
         }
             inputStream.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return byteArrayOutputStream.toString();
     }
 
 }
