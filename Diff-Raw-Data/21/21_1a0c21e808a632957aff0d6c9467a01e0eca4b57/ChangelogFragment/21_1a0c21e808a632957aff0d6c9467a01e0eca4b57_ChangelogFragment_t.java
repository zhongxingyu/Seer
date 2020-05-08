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

 public class ChangelogFragment extends Fragment {
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         View changelogView = inflater.inflate(R.layout.changelog_fragment, container, false);
 
         AboutActivity aboutActivity = new AboutActivity();
 
         TextView changelogVersion = (TextView) changelogView.findViewById(R.id.changelog_version);
         String version =  aboutActivity.getRomVersion();
         changelogVersion.append(" " + version);
 
         TextView changelogText = (TextView) changelogView.findViewById(R.id.changelog);
         changelogText.setText(readChangelog());
 
         // Inflate the layout for this fragment
         return changelogView;
     }
 
     private String readChangelog() {
        InputStream inputStream = getResources().openRawResource(R.raw.changelog);
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
