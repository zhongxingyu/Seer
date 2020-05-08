 /**
  * Appia: Group communication and protocol composition framework library
  * Copyright 2006 University of Lisbon
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  *
  * Initial developer(s): Alexandre Pinto and Hugo Miranda.
  * Contributor(s): See Appia web page for a list of contributors.
  */
  
 package org.continuent.appia.core.message;
 
 import java.io.PrintStream;
 import java.io.PrintWriter;
 
 /**
 * Exception raised by any of the methods of the Message.
  * <br>
  * To allow a transparent redefenition of the methods of Message, it extends
  * RuntimeException.
  * <br>
  * Only in very strange situations it will be raised.
  * <br>
  * It carries the original exception raised.
  *
  * @author Alexandre Pinto
  * @version 0.1
  * @see org.continuent.appia.core.message.Message
  * @see java.lang.RuntimeException
  */
 public class MessageException extends RuntimeException {
 
   private static final long serialVersionUID = 6445053185311031824L;
 
   /**
    * The original exception that caused this exception to be raised.
    */
   public Exception original;
 
   /**
    * Constructs a new exception.
    *
    * @param ex the original Exception raised.
    */
   public MessageException(Exception ex) {
     original=ex;
   }
 
   /**
    * Redefenition.
    * Also prints original exception.
    */
   public void printStackTrace() {
     original.printStackTrace();
     super.printStackTrace();
   }
 
   /**
    * Redefenition.
    * Also prints original exception.
    */
   public void printStackTrace(PrintStream s) {
     original.printStackTrace(s);
     super.printStackTrace(s);
   }
 
   /**
    * Redefenition.
    * Also prints original exception.
    */
   public void printStackTrace(PrintWriter s) {
     original.printStackTrace(s);
     super.printStackTrace(s);
   }
 }
