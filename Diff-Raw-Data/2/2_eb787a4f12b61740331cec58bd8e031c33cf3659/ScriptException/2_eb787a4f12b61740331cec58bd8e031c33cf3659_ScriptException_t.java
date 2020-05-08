 /**
  *
  * Copyright 2005 (C) The original author or authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package javax.script;
 
 /**
  * @version $Revision: $ $Date: $
  */
 public class ScriptException extends Exception
 {
     private final String fileName;
     private final int lineNumber;
     private final int columnNumber;
 
     public ScriptException(String message)
     {
         this(message, null, -1, -1);
     }
 
     public ScriptException(Exception e)
     {
        super(e.getMessage(), e);
 
         this.fileName = null;
         this.lineNumber = -1;
         this.columnNumber = -1;
     }
 
     public ScriptException(String message, String fileName, int lineNumber)
     {
         this(message, fileName, lineNumber, -1);
     }
 
     public ScriptException(String message, String fileName, int lineNumber, int columnNumber)
     {
         super(message);
 
         this.fileName = fileName;
         this.lineNumber = lineNumber;
         this.columnNumber = columnNumber;
     }
 
     public String getMessage()
     {
         StringBuffer message = new StringBuffer(super.getMessage());
 
         if (fileName != null)
         {
             if (lineNumber != -1)
             {
                 message.append(" at [").append(lineNumber);
                 if (columnNumber != -1) message.append(", ").append(columnNumber);
                 message.append("]");
             }
             message.append(" in file ").append(fileName);
         }
 
         return message.toString();
     }
 
     public String getFileName()
     {
         return fileName;
     }
 
     public int getLineNumber()
     {
         return lineNumber;
     }
 
     public int getColumnNumber()
     {
         return columnNumber;
     }
 }
