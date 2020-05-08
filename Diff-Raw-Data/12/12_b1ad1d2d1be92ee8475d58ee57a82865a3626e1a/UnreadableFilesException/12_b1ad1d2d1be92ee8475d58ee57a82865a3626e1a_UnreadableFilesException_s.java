 /*
  * Copyright 2011 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.config;
 
 import java.util.List;
 
 import com.google.common.base.Joiner;
 
 /**
  * Thrown when files are are unredable.
  *
  * @author Cory Smith (corbinrsmith@gmail.com) 
  */
 public class UnreadableFilesException extends RuntimeException {
 
   private static final long serialVersionUID = 9101262574286912459L;
 
   private final List<UnreadableFile> unreadables;
 
   /**
    * @param unreadable
    */
   public UnreadableFilesException(List<UnreadableFile> unreadables) {
     this.unreadables = unreadables;
   }
 
   @Override
   public String toString() {
     return Joiner.on("\n").join(unreadables);
   }
 }
