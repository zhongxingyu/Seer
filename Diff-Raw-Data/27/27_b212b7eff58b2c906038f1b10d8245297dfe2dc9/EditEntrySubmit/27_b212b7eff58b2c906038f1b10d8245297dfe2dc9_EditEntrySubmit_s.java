 /*
 Copyright (c) 2005, Lucas Holt
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:
 
   Redistributions of source code must retain the above copyright notice, this list of
   conditions and the following disclaimer.
 
   Redistributions in binary form must reproduce the above copyright notice, this
   list of conditions and the following disclaimer in the documentation and/or other
   materials provided with the distribution.
 
   Neither the name of the Just Journal nor the names of its contributors
   may be used to endorse or promote products derived from this software without
   specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package com.justjournal.ctl;
 
 import com.justjournal.db.DateTime;
 import com.justjournal.db.DateTimeBean;
 import com.justjournal.db.EntryDAO;
 import com.justjournal.db.EntryTo;
 import com.justjournal.utility.StringUtil;
 import org.apache.log4j.Category;
 
 /**
  * Created by IntelliJ IDEA.
  * User: laffer1
  * Date: Jan 1, 2004
  * Time: 9:08:33 PM
 * To change this template use Options | File Templates.
  */
 public class EditEntrySubmit extends Protected {
     private static Category log = Category.getInstance(EditEntrySubmit.class.getName());
 
     protected int entryId;
     protected String body;
     protected String subject;
     protected int userId;
     protected int mood;
     protected int location;
     protected int security;
     protected String music;
     protected int year;
     protected int month;
     protected int day;
     protected int hour;
     protected int minute;
 
     // optional features
     // these will contain checked if they are active.
     // most browsers don't pass check boxes from forms
     // unless the are checked in which case the name/value
     // pair is set.  I used "checked" in the html code.
     protected String discomments;  // disable comments
     protected String noemail;      // disable email when comments
     protected String aformat;      // auto format
 
     public int getEntryId() {
         return this.entryId;
     }
 
     public void setEntryId(int entryId) {
         this.entryId = entryId;
     }
 
     public void setSubject(String subject) {
         this.subject = subject;
     }
 
     public void setBody(String body) {
         this.body = body;
     }
 
     public void setYear(int year) {
         this.year = year;
     }
 
     public void setMonth(int month) {
         this.month = month;
     }
 
     public void setDay(int day) {
         this.day = day;
     }
 
     public void setHour(int hour) {
         this.hour = hour;
     }
 
     public void setMinute(int minute) {
         this.minute = minute;
     }
 
     public void setUserId(int userId) {
         this.userId = userId;
     }
 
     public void setLocation(int location) {
         this.location = location;
     }
 
     public void setMood(int mood) {
         this.mood = mood;
     }
 
     public void setSecurity(int security) {
         this.security = security;
     }
 
     public void setMusic(String music) {
         this.music = music;
     }
 
     public void setAformat(String aformat) {
         this.aformat = aformat;
     }
 
     public void setNoemail(String noemail) {
         this.noemail = noemail;
     }
 
     public void setDiscomments(String discomments) {
         this.discomments = discomments;
     }
 
     public String getMyLogin() {
         return this.currentLoginName();
     }
 
     protected String insidePerform() throws Exception {
 
         if (log.isDebugEnabled())
             log.debug("Loading DAO Objects  ");
 
         final EntryDAO edao = new EntryDAO();
         final EntryTo entry = new EntryTo();
         final DateTime myDate = new DateTimeBean();
 
         if (this.currentLoginId() < 1)
             addError("login", "The login timed out or is invalid.");
 
         try {
             myDate.setYear(year);
             myDate.setMonth(month);
             myDate.setDay(day);
             myDate.setHour(hour);
             myDate.setMinutes(minute);
 
             entry.setBody(StringUtil.replace(body, '\'', "\\\'"));
             entry.setDate(myDate);
             entry.setSubject(StringUtil.replace(subject, '\'', "\\\'"));
             entry.setUserId(userId);
             entry.setId(entryId);
             entry.setSecurityLevel(security);
             entry.setMoodId(mood);
             entry.setLocationId(location);
 
             // music can't be null!
             if (music == null)
                 music = "";
 
             entry.setMusic(StringUtil.replace(music, '\'', "\\\'"));
 
             // the check box says disable auto format
             if (this.aformat != null && this.aformat.equals("checked"))
                 entry.setAutoFormat(false);
             else
                 entry.setAutoFormat(true);
 
             // disable comments
             if (this.discomments != null && this.discomments.equals("checked"))
                 entry.setAllowComments(false);
             else
                 entry.setAllowComments(true);
 
             // disable email notifications
             if (this.noemail != null && this.noemail.equals("checked"))
                 entry.setEmailComments(false);
             else
                 entry.setEmailComments(true);
 
             if (log.isDebugEnabled())
                 log.debug("entry to add:\n" + entry.toString());
         } catch (IllegalArgumentException e) {
             addError("Input", e.getMessage());
 
             if (log.isDebugEnabled())
                 log.debug("Error loading user input: " + e.getMessage());
         }
 
        if (this.hasErrors() == false) {
             boolean result = edao.update(entry);
 
             if (log.isDebugEnabled())
                 log.debug("Was there an error with data tier?  " + !result);
 
            if (result == false)
                 addError("Unknown", "Could not update entry.");
         }
 
         if (this.hasErrors())
             return ERROR;
         else
             return SUCCESS;
     }
 
 
 }
