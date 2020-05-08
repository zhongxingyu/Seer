 /*
    Copyright 2013 Zava (http://www.zavakid.com)
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package com.zavakid.mockingbird.common;
 
 /**
  * @author Zava 2013-1-28 下午9:55:25
  * @since 0.0.1
  */
 public class CharBuffer {
 
     private int    position;
     private int    length;
     private String s;
 
     public CharBuffer(String string){
         this.s = string;
         this.length = string.length();
     }
 
     public boolean remain() {
         return position < length;
     }
 
     public boolean remain(int i) {
         return (position + i) < length;
     }
 
     public Character next() {
         return s.charAt(position++);
     }
 
     public Character lookAhead(int i) {
        int p = position + i;
         if (p >= length) {
             return null;
         }
         return s.charAt(p);
     }
 
     public Character lookbefore(int i) {
         return s.charAt(position - i);
     }
 
 }
