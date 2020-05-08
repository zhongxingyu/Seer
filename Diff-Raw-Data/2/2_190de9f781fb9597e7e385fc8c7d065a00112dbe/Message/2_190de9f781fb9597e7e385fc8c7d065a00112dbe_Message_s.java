 /**
  * This file is part of libRibbonData library (check README).
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
 
 package MessageClasses;
 
 /**
  * Message class.
  * Extends messageEntry class with additional field for message content.
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  * @see messageEntry
  */
 public class Message extends MessageClasses.MessageEntry {
     
     /**
      * Message's content
      */
     public String CONTENT;
     
     /**
      * Empty constructor.
      */
     public Message() {
         super();
     }
     
     /**
      * Default constructor from csv line.
      * @param givenCsv 
      */
     public Message(String givenCsv) {
         super(givenCsv);
     }
     
     /**
      * Constructor for internal post procedure;
      * @param givenHeader header of message;
      * @param givenAuthor author of message;
      * @param givenLang language of message;
      * @param givenDirs destination dirs for message;
      * @param givenTags tag marks for message;
      * @param givenContent content of the message;
      */
     public Message (String givenHeader, String givenAuthor, String givenLang, String[] givenDirs, String[] givenTags, String givenContent) {
         HEADER = givenHeader;
         AUTHOR = givenAuthor;
         ORIG_AUTHOR = "";
         LANG = givenLang;
         DIRS = givenDirs;
         TAGS = givenTags;
         CONTENT = givenContent;
         ORIG_INDEX = "-1";
     }
     
     /**
      * Construct Message from Messageentry;
      * @param givenEntry message entry;
      * @param givenContent content for message;
      */
     public Message (MessageEntry givenEntry, String givenContent) {
         INDEX = givenEntry.INDEX;
         ORIG_INDEX = givenEntry.ORIG_INDEX;
         HEADER = givenEntry.HEADER;
         AUTHOR = givenEntry.AUTHOR;
        ORIG_AUTHOR = givenEntry.AUTHOR;
         LANG = givenEntry.LANG;
         DIRS = givenEntry.DIRS;
         TAGS = givenEntry.TAGS;
         DATE = givenEntry.DATE;
         PROPERTIES = givenEntry.PROPERTIES;
         CONTENT = givenContent;
     }
     
     /**
      * Return message entry from Message
      * @return messageEntry object
      */
     public MessageClasses.MessageEntry returnEntry() {
         return (MessageClasses.MessageEntry) this;
     }
 }
