 /*
  * Copyright 2008 Google Inc.
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
 package com.google.jstestdriver;
 
 /**
  * Contains a serialized command to be sent to the browser.
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class Command {
 
   private final String command;
 
   public Command(String command) {
     this.command = command;
   }
 
   public String getCommand() {
     return command;
   }
 
   @Override
   public String toString() {
     return String.format("command= %s", command);
   }

  @Override
  public int hashCode() {
    return 31 + ((command == null) ? 0 : command.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Command)) { return false;}
    Command other = (Command) obj;
    return command == null ? other.command == null : command.equals(other.command);
  }
  
 }
