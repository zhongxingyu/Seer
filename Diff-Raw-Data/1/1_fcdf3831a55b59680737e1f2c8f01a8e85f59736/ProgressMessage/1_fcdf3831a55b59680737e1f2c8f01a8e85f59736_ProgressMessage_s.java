 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.bcaching.progress;
 
 import android.app.ProgressDialog;
 
 public enum ProgressMessage {
     SET_FILE {
         @Override
         void act(ProgressDialog progressDialog, int arg1, Object object) {
             progressDialog.incrementProgressBy(1);
         }
     },
 
     SET_MAX {
         @Override
         void act(ProgressDialog progressDialog, int arg1, Object object) {
             progressDialog.setMax(arg1);
         }
     },
     SET_PROGRESS {
         @Override
         void act(ProgressDialog progressDialog, int arg1, Object object) {
             progressDialog.setProgress(arg1);
         }
     },
     DONE {
         @Override
         void act(ProgressDialog progressDialog, int arg1, Object object) {
             progressDialog.dismiss();
         }
     },
     START {
         @Override
         void act(ProgressDialog progressDialog, int arg1, Object object) {
             progressDialog.show();
         }
     };
 
     abstract void act(ProgressDialog progressDialog, int arg1, Object object);
 
     static ProgressMessage fromInt(Integer i) {
         return ProgressMessage.class.getEnumConstants()[i];
     }
 }
