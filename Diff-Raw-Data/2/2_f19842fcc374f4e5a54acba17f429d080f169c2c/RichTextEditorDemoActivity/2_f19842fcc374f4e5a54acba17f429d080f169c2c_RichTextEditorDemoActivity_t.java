 /***
  Copyright (c) 2012 CommonsWare, LLC
   
   Licensed under the Apache License, Version 2.0 (the "License"); you may
   not use this file except in compliance with the License. You may obtain
   a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */    
 
 package com.commonsware.cwac.richedit.demo;
 
 import android.os.Bundle;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.commonsware.cwac.richedit.RichEditText;
 
 public class RichTextEditorDemoActivity extends SherlockActivity {
   RichEditText editor=null;
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     
     setContentView(R.layout.main);
     
     editor=(RichEditText)findViewById(R.id.editor);
     editor.enableActionModes(true);
   }
 }
