 /*
  *    Copyright 2012 Weald Technology Trading Limited
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package com.wealdtech;
 
 /**
  * A DataError covers all exceptions generated due to bad data. Such errors are
  * always correctable by altering the data presented the the throwing method
  * suitably.
  */
 public class DataError extends WealdError
 {
   private static final long serialVersionUID = -3487779032177158481L;
 
   /**
    * Generic data error
    */
   public DataError()
   {
    super(null, null, null, null);
   }
 
   /**
    * Data error with explanation
    * @param message the message
    */
   public DataError(final String message)
   {
     super(message, null, null, null);
   }
 
   /**
    * Data error with cause
    * @param t the cause
    */
   public DataError(final Throwable t)
   {
     super(null, null, null, t);
   }
 
   /**
    * Data error with explanation and cause
    * @param message the message
    * @param t the cause
    */
   public DataError(final String message,final Throwable t)
   {
     super(message, null, null, t);
   }
 
   /**
    * Generic data error.
    * <p>This should only be used where there no alternative available from a subclass of DataError.
    * @param message a message suitable to be displayed back to the developer
    * @param userMessage a message suitable to be displayed back to the end user
    * @param url a URL for more information about the problem
    * @param t the cause of the error, if any
    */
   public DataError(final String message, final String userMessage, final String url, final Throwable t)
   {
     super(message, userMessage, url, t);
   }
 
   /**
    * An error authenticating a request
    */
   public static class Authentication extends DataError
   {
     private static final long serialVersionUID = -6353201997641349475L;
     public static String URL = BASEURL + "authentication";
     public static String USERMESSAGE = "There was a problem authenticating your request";
     public Authentication(final String message)
     {
       super(message, USERMESSAGE, URL, null);
     }
     public Authentication(final String message, final Throwable t)
     {
       super(message, USERMESSAGE, URL, t);
     }
   }
 
   /**
    * An error due to missing data
    */
   public static class Missing extends DataError
   {
     private static final long serialVersionUID = -7181236040016670944L;
     public static String URL = BASEURL + "missingdata";
     public static String USERMESSAGE = "Some of the data required to complete your request is missing";
     public Missing(final String message)
     {
       super(message, USERMESSAGE, URL, null);
     }
 
     public Missing(final String message, final Throwable t)
     {
       super(message, USERMESSAGE, URL, t);
     }
   }
 
   /**
    * An error due to bad data
    */
   public static class Bad extends DataError
   {
     private static final long serialVersionUID = 8345370192499264874L;
     public static String URL = BASEURL + "baddata";
     public static String USERMESSAGE = "Some of the data required to complete your request is incorrect";
     public Bad(final String message)
     {
       super(message, USERMESSAGE, URL, null);
     }
     public Bad(final String message, final Throwable t)
     {
       super(message, USERMESSAGE, URL, t);
     }
   }
 }
