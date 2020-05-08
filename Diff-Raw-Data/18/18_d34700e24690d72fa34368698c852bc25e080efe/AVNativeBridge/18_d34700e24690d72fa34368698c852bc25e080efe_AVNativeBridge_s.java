 /*
  * Copyright 2012 Alexander Shabanov - http://alexshabanov.com.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.alexshabanov.mm4j.av;
 
 /**
  * libavcodec/libavformat bridge.
  * TODO: make package-local
  */
 public final class AVNativeBridge {
     private AVNativeBridge() {}
 
     public static native byte[] initInputFileContext(String name);
 
     public static native void disposeFileContext(byte[] arr);
 
    static {
         try {
            System.loadLibrary("libmm4j-avbridge.so");
         } catch (Throwable e) {
             e.printStackTrace();
             System.exit(1);
         }
     }
 }
