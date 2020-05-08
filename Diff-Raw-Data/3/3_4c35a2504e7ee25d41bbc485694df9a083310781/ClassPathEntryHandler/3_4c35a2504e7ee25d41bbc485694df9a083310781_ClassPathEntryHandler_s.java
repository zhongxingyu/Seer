 /*
  *    Copyright 2010 The Meiyo Team
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package org.nnsoft.commons.meiyo.classpath;
 
 /**
  * The handler which receives ClassPath entry notifications.
  */
 public interface ClassPathEntryHandler {
 
     /**
      * Receive a ClassPath entry notification event.
      *
     * @param classPathEntry the notified {@link ClassPathEntry}.
      */
     void doHandle(String path, Class<?> classPathEntry);
 
 }
