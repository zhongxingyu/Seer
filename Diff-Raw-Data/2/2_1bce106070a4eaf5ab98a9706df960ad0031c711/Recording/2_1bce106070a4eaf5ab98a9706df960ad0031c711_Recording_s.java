 /* $Id$
  * 
  * Copyright (c) 2005, Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice, 
  *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project (Lazy Bones) nor the names of its 
  *    contributors may be used to endorse or promote products derived from this 
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package org.hampelratte.svdrp.responses.highlevel;
 
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * 
  * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net</a>
  *
  * Represents a recording of VDR
  */
 @SuppressWarnings("unchecked")
 public class Recording implements Comparable {
     private int number;
 
     private Calendar startTime;
     
     private boolean isNew = false;
 
     private String title;
 
     private EPGEntry epgInfo;
 
     public EPGEntry getEpgInfo() {
         return epgInfo;
     }
 
     public void setEpgInfo(EPGEntry epgInfo) {
         this.epgInfo = epgInfo;
     }
 
     public int getNumber() {
         return number;
     }
 
     public void setNumber(int number) {
         this.number = number;
     }
 
     public Calendar getStartTime() {
         return startTime;
     }
 
     public void setStartTime(Calendar startTime) {
         this.startTime = startTime;
     }
     
     public void setStartTime(Date date) {
         Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(date.getTime());
         setStartTime(cal);
     }
 
     public String getTitle() {
         return title;
     }
     
     public String getDisplayTitle() {
         String display = getTitle();
         while(display.startsWith("%")) {
             display = display.substring(1);
         }
         return display;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public boolean isNew() {
         return isNew;
     }
 
     public void setNew(boolean isNew) {
         this.isNew = isNew;
     }
     
     public boolean isCut() {
        return getTitle().startsWith("%");
     }
     
     public String toString() {
         return getNumber() + " "+ 
                 DateFormat.getDateTimeInstance().format(getStartTime().getTime()) + 
                 (isNew() ? "*" : "") + " " + 
                 getTitle() + 
                 (getEpgInfo() != null ? " - " + getEpgInfo() : "");
     }
 
     public int compareTo(Object o) {
         if(o instanceof Recording) {
             Recording that = (Recording) o;
             if(this.getEpgInfo() != null && that.getEpgInfo() != null) {
                 return this.getEpgInfo().getTitle().compareTo(that.getEpgInfo().getTitle());
             } else {
                 return this.getTitle().compareTo(that.getTitle());
             }
         }
         return -1;
     }
 }
