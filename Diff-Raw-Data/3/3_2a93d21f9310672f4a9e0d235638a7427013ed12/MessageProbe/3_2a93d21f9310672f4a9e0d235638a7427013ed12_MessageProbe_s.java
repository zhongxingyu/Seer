 /**
  * This file is part of RibbonWeb application (check README).
  * Copyright (C) 2012-2013 Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package models;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 import play.db.ebean.*;
 import com.avaje.ebean.validation.*;
 import play.data.format.*;
 
 /**
  * Message post probe model;
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 @Entity
 public class MessageProbe extends Model {
     
     @Id
     public String id;
     
     /**
      * Header of message (max = 250 chars).
      */
     @NotNull
     @Length(max=500)
     public String header;
     
     /**
      * Date of message creating.
      */
     @NotNull
     @Formats.DateTime(pattern="HH:mm:ss dd.MM.yyyy")
     @Past
     public java.util.Date date = new java.util.Date();
     
     /**
      * Pseudo directory to post (max = 100 chars).
      */
     @NotNull
     @Length(max=100)
     public String psdir;
     
     /**
      * Tags of message (max = 100 chars).
      */
     @Length(max=200)
     public String tags;
     
     /**
      * Urgent flag of this message.
      */
    public Boolean urgent;
     
     /**
      * Content of message (max = 2M chars).
      */
     @NotNull
     @Length(max=4000000)
     public String content;
     
     /**
      * Author of this message (max = 100 chars).
      */
     @NotNull
     @Length(max=200)
     public String author;
     
     /**
      * Index of this message in the Ribbon System (optional).
      */
     @Length(max=10)
     public String ribbon_index;
     
     /**
      * Status of message processing stage.
      */
     public enum STATUS {
         
         /**
          * Message is new in the system: post required.
          */
         NEW,
         
         /**
          * Message has been posted to the system: do nothing.
          */
         POSTED,
         
         /**
          * Not posted, get error: notify user.
          */
         WITH_ERROR,
         
         /**
          * Edited by user (require processing).
          */
         EDITED,
         
         /**
          * Edition processed and wait for confirm.
          */
         WAIT_CONFIRM,
         
         /**
          * Edited by other user in the system, author cann't edit message with this status.
          */
         ACCEPTED,
         
         /**
          * Marked to delete from database: will delete soon.
          */
         DELETED
     }
     
     /**
      * Current status of this probe.
      */
     @NotNull
     public STATUS curr_status = STATUS.NEW;
     
     /**
      * Posting error.
      */
     public String curr_error = null;
     
     /**
      * Get formatted date as string.
      * @param format date specific format;
      * @return formatted string;
      */
     public String getDateWithFormat(String format) {
         java.text.DateFormat dateFormat = new java.text.SimpleDateFormat(format);
         String strDate = dateFormat.format(date);
         return strDate;
     }
     
     /**
      * Get message probe CSV representation for post to the system.
      * @return formatted to CSV command;
      */
     public String getCsvToPost() {
         String compiledDate = this.getDateWithFormat("HH:mm:ss dd.MM.yyyy");
         return "RIBBON_POST_MESSAGE_BY_PSEUDO:{" + this.psdir + "},UKN,{" + this.header + "},[" + tags.replaceAll(" ", "") + "],{"
                 + "COPYRIGHT,{" + this.author + "},{" + this.author + "}," + compiledDate + "$"
                 + "PSEUDO_DIR,{" + this.author + "},{" + this.psdir + "}," + compiledDate + "$"
                 + "REMOTE_ID,{" + this.author + "},{" + this.id + "}," + compiledDate +
                 (this.urgent ? "$URGENT,{},{" + this.id + "}," + compiledDate : "")
                 + "}\n" + content + "\nEND:";
     }
     
     /**
      * Get message probe CSV representation for modify existing message.
      * @return formatted string;
      */
     public String getCsvToModify() {
         String compiledDate = this.getDateWithFormat("HH:mm:ss dd.MM.yyyy");
         return "RIBBON_MODIFY_MESSAGE_BY_PSEUDO:" + this.ribbon_index + ",{" + this.psdir + "},UKN,{" + this.header + "},[" + tags.replaceAll(" ", "") + "],{"
                 + "COPYRIGHT,{" + this.author + "},{" + this.author + "}," + compiledDate + "$"
                 + "PSEUDO_DIR,{" + this.author + "},{" + this.psdir + "}," + compiledDate + "$"
                 + "REMOTE_ID,{" + this.author + "},{" + this.id + "}," + compiledDate + 
                 (this.urgent ? "$URGENT,{},{" + this.id + "}," + compiledDate : "")
                 + "}\n" + content + "\nEND:";
     }
 }
