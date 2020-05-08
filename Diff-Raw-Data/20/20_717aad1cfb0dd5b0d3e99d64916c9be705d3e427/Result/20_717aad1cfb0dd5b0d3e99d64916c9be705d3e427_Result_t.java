 /*
  * Copyright 2013 Harald Wellmann
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.ops4j.pax.carrot.step;
 
 import org.hamcrest.Matcher;
 
 /**
  * The result of an {@link Invocation} applied to a fixture.
  * 
  * @author Harald Wellmann
 * 
  */
 public class Result {
 
     public enum Status {
         RIGHT, WRONG, EXCEPTION, IGNORED
     }
 
     private Matcher<?> matcher;

     private Object actual;

     private Throwable exception;

     private Status status;
 
     public Result() {
     }
 
     public Result(Matcher<?> matcher) {
         this.matcher = matcher;
     }
 
     public void setActual(Object actual) {
         this.actual = actual;
     }
 
     public Matcher<?> getExpected() {
         return matcher;
     }
 
    /**
     * Returns the actual result or an empty string, if the result is null.
     * 
     * @return result or empty string
     */
     public Object getActual() {
        return actual == null ? "" : actual;
     }
 
     public void setException(Throwable exception) {
         this.exception = exception;
     }
 
     public Throwable getException() {
         return exception;
     }
 
     public Status getStatus() {
         if (status == null) {
             if (exception != null) {
                 status = Status.EXCEPTION;
             }
             else if (matcher == null) {
                 status = Status.IGNORED;
             }
            else if (matcher.matches(getActual().toString())) {
                 status = Status.RIGHT;
             }
             else {
                 status = Status.WRONG;
             }
         }
         return status;
     }
 }
