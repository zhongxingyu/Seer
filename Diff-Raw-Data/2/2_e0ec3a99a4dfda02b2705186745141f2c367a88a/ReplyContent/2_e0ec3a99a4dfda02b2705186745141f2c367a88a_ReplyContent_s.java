 /*
  *  Copyright (C) 2000 - 2011 Silverpeas
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  * 
  *  As a special exception to the terms and conditions of version 3.0 of
  *  the GPL, you may redistribute this Program in connection with Free/Libre
  *  Open Source Software ("FLOSS") applications as described in Silverpeas's
  *  FLOSS exception.  You should have recieved a copy of the text describing
  *  the FLOSS exception, and it is also available here:
  *  "http://www.silverpeas.com/legal/licensing"
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  * 
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package com.silverpeas.migration.questionreply;
 
 /**
  *
  * @author ehugonnet
  */
 public class ReplyContent {
 
   private String content;
   private String instanceId;
   private int id;
 
   public ReplyContent(int id, String instanceId, String content) {
     if (content != null) {
      this.content = content;
     } else {
       this.content = "";
     }
     this.instanceId = instanceId;
     this.id = id;
   }
 
   /**
    * Get the value of id
    *
    * @return the value of id
    */
   public int getId() {
     return id;
   }
 
   /**
    * Get the value of instanceId
    *
    * @return the value of instanceId
    */
   public String getInstanceId() {
     return instanceId;
   }
 
   /**
    * Get the value of content
    *
    * @return the value of content
    */
   public String getContent() {
     return content;
   }
 }
