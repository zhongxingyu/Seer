 /* Copyright 2010-2014 Norconex Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.norconex.jef4.job;
 
 import com.norconex.jef4.JEFException;
 
 /**
  * Represents a job-related exception.  Implementors are invited to
  * wrap exceptions they want explicitly handled by the framework in instances
  * of <code>JobException</code>.
  * @author Pascal Essiembre
  */
 public class JobException extends JEFException {
 
     /** For serialisation. */
     private static final long serialVersionUID = 5236102272021889018L;
 
     public JobException(final String message) {
         super(message);
     }
     public JobException(final Throwable exception) {
         super(exception);
     }
     public JobException(final String message, final Throwable exception) {
         super(message, exception);
     }
 }
