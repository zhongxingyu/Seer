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
 
 import android.os.Message;
 
 public enum ProgressMessage {
     SET_FILE {
         @Override
         void act(ProgressHandler progressHandler, Message msg) {
             progressHandler.setFile((String)msg.obj);
         }
     },
 
     SET_MAX {
         @Override
         void act(ProgressHandler progressHandler, Message msg) {
             progressHandler.setMax(msg.arg1);
         }
     },
     SET_PROGRESS {
         @Override
         void act(ProgressHandler progressHandler, Message msg) {
             progressHandler.setProgress(msg.arg1);
         }
     },
     DONE {
         @Override
         void act(ProgressHandler progressHandler, Message msg) {
             progressHandler.done();
         }
     },
     START {
         @Override
         void act(ProgressHandler progressHandler, Message msg) {
             progressHandler.setProgress(0);
            progressHandler.setMax(100);
             progressHandler.show();
         }
     },
     REFRESH {
         @Override
         void act(ProgressHandler progressHandler, Message msg) {
             progressHandler.refresh();
         }
     };
 
     abstract void act(ProgressHandler progressHandler, Message msg);
 
     static ProgressMessage fromInt(Integer i) {
         return ProgressMessage.class.getEnumConstants()[i];
     }
 }
