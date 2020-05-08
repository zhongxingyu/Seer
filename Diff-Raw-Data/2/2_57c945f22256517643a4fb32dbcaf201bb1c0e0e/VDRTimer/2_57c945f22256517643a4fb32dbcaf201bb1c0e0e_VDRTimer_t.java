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
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.hampelratte.svdrp.Connection;
 import org.hampelratte.svdrp.VDRVersion;
 
 
 /**
  * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net </a>
  * 
  * Represents a timer of the VDR software
  */
 public class VDRTimer implements Serializable, Comparable<VDRTimer>, Cloneable {
     
     private static final long serialVersionUID = 1L;
 
     public static final int INACTIVE = 0;
     public static final int ACTIVE = 1;
     public static final int INSTANT_TIMER = 2;
     public static final int VPS = 4;
     public static final int RECORDING = 8;
     
     private int state = ACTIVE;
     
     private Calendar startTime = GregorianCalendar.getInstance();
 
     private Calendar endTime = GregorianCalendar.getInstance();
 
     // start date for repeating timers
     private Calendar firstTime = GregorianCalendar.getInstance();
     
     private boolean hasFirstTime;
 
     private boolean[] repeatingDays = new boolean[7];
 
     private int channelNumber;
     
     private int ID;
 
     private int priority;
 
     private int lifetime;
 
     private String title = "";
 
     private String path = "";
 
     private String description = "";
     
     public VDRTimer() {
     }
 
     public boolean isActive() {
         return hasState(ACTIVE);
     }
     
     /**
      * Returns, if a timer has a specific state
      * @param STATE One of INACTIVE, ACTIVE, INSTANT_TIMER, VPS, RECORDING
      * @return true, if the timer has the state
      * @see VDRTimer#ACTIVE
      * @see VDRTimer#INACTIVE
      * @see VDRTimer#INSTANT_TIMER
      * @see VDRTimer#VPS
      * @see VDRTimer#RECORDING
      */
     public boolean hasState(int STATE) {
         return (state & STATE) == STATE;
     }
 
     public int getChannelNumber() {
         return channelNumber;
     }
 
     public void setChannelNumber(int channel) {
         this.channelNumber = channel;
     }
 
     public int getLifetime() {
         return lifetime;
     }
 
     public void setLifetime(int durability) {
         this.lifetime = durability;
     }
 
     public Calendar getEndTime() {
         return endTime;
     }
 
     public void setEndTime(Calendar endTime) {
         this.endTime = endTime;
         if (endTime.before(startTime)) {
             endTime.add(Calendar.DAY_OF_MONTH, 1);
         }
     }
 
     public int getPriority() {
         return priority;
     }
 
     public void setPriority(int priority) {
         this.priority = priority;
     }
 
     public Calendar getStartTime() {
         return startTime;
     }
 
     public void setStartTime(Calendar startTime) {
         this.startTime = startTime;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         title.replaceAll(":", "|");
         this.title = title;
     }
 
     /**
      * Returns a unique key, which consits of the channel, the day, the start time and the end time
      * @return a String which identifies this Timer
      */
     public String getUniqueKey() {
         SimpleDateFormat sdf;
         if(isRepeating()) {
             sdf = new SimpleDateFormat("HH:mm");
         } else {
             sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
         }
         
         String startTime = sdf.format(getStartTime().getTime());
         String endTime = sdf.format(getEndTime().getTime());
         StringBuffer sb = new StringBuffer();
         sb.append(getChannelNumber());
         sb.append(':');
         sb.append(getDayString());
         sb.append(':');
         sb.append(startTime);
         sb.append(':');
         sb.append(endTime);
         return sb.toString();
     }
     
     /**
      * Returns a settings string, which can be used to create or update timers
      * @return a settings string, which can be used to create or update timers
      */
     public String toNEWT() {
         String start = createTimeString(getStartTime());
         String end = createTimeString(getEndTime());
 
         StringBuffer sb = new StringBuffer();
         sb.append(getState());
         sb.append(':');
         sb.append(channelNumber);
         sb.append(':');
         sb.append(getDayString());
         sb.append(':');
         sb.append(start);
         sb.append(':');
         sb.append(end);
         sb.append(':');
         sb.append(priority);
         sb.append(':');
         sb.append(lifetime);
         sb.append(':');
         sb.append(getFile());
         sb.append(':');
         sb.append(description.replaceAll("\n", "\\|"));
 
         return sb.toString();
     }
 
     public String toString() {
             String start = createTimeString(getStartTime());
             String end = createTimeString(getEndTime());
             
             StringBuffer sb = new StringBuffer();
             sb.append(getState());
             sb.append(':');
             sb.append(channelNumber);
             sb.append(':');
             sb.append(getDayString());
             if(isRepeating()) {
                 sb.append(" [instance:"+createDateString(startTime, false)+"]");
             }
             sb.append(':');
             sb.append(start);
             sb.append(':');
             sb.append(end);
             sb.append(':');
             sb.append(priority);
             sb.append(':');
             sb.append(lifetime);
             sb.append(':');
             sb.append(getFile());
             sb.append(':');
             
             String desc = description.replaceAll("\n", "\\|");
             if(desc.length() > 15) {
                 desc = desc.substring(0,15) + "...";
             }
             sb.append(desc);
 
             return sb.toString();
         
     }
 
     public boolean equals(Object o) {
         if (o instanceof VDRTimer) {
             VDRTimer timer = (VDRTimer) o;
             return timer.toNEWT().equals(toNEWT());
         } else {
             return false;
         }
     }
 
     public int compareTo(VDRTimer that) {
         return that.toNEWT().compareTo(this.toNEWT());
     }
 
     public boolean[] getRepeatingDays() {
         return repeatingDays;
     }
 
     public void setRepeatingDays(boolean[] repeating_days) {
         this.repeatingDays = repeating_days;
     }
 
     public boolean isRepeating() {
         for (int i = 0; i < repeatingDays.length; i++) {
             if (repeatingDays[i])
                 return true;
         }
         return false;
     }
 
     public Calendar getFirstTime() {
         return firstTime;
     }
 
     public void setFirstTime(Calendar firstTime) {
         this.firstTime = firstTime;
     }
 
     public boolean hasFirstTime() {
         return hasFirstTime;
     }
 
     public void setHasFirstTime(boolean hasFirstTime) {
         this.hasFirstTime = hasFirstTime;
     }
 
     public String getDayString() {
         StringBuffer sb = new StringBuffer();
 
         if (isRepeating()) {
             sb.append(createRepeatingString());
             if (hasFirstTime()) {
                 sb.append('@');
                 sb.append(createDateString(firstTime, true));
             }
         } else {
             sb.append(createDateString(startTime, false));
         }
 
         return sb.toString();
     }
 
     private String createDateString(Calendar cal, boolean repeating) {
         // shall we use the new format?
         // if no connection is available, we have to use a dummy version
         VDRVersion v = Connection.getVersion();
         if (v == null) {
             v = new VDRVersion("1.0.0");
         }
 
         int major = v.getMajor();
         int minor = v.getMinor();
         int rev = v.getRevision();
 
         boolean newFormat = (major == 1 && minor >= 3 && rev >= 23) | isRepeating();
 
         String date = "";
         if (newFormat) {
             int day = cal.get(Calendar.DAY_OF_MONTH);
             String dayString = day < 10 ? ("0" + day) : Integer.toString(day);
             int month = cal.get(Calendar.MONTH) + 1;
             String monthString = month < 10 ? ("0" + month) : Integer
                     .toString(month);
             date = cal.get(Calendar.YEAR) + "-" + monthString + "-" + dayString;
         } else {
             date = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
         }
 
         return date;
     }
     
     private String createTimeString(Calendar time) {
         SimpleDateFormat df = new SimpleDateFormat("HHmm");
         Date date = new Date(time.getTimeInMillis());
         return df.format(date);
     }
 
     private String createRepeatingString() {
         StringBuffer day = new StringBuffer();
         char c = 'M';
 
         c = repeatingDays[0] ? 'M' : '-';
         day.append(c);
         c = repeatingDays[1] ? 'T' : '-';
         day.append(c);
         c = repeatingDays[2] ? 'W' : '-';
         day.append(c);
         c = repeatingDays[3] ? 'T' : '-';
         day.append(c);
         c = repeatingDays[4] ? 'F' : '-';
         day.append(c);
         c = repeatingDays[5] ? 'S' : '-';
         day.append(c);
         c = repeatingDays[6] ? 'S' : '-';
         day.append(c);
 
         return day.toString();
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getPath() {
         return path;
     }
 
     public void setPath(String path) {
         this.path = path;
         if (!this.path.endsWith("~") && !path.equals("")) {
             this.path += "~";
         }
     }
 
     public String getFile() {
         return (path + title).replaceAll(":", "|");
     }
 
     public void setFile(String file) {
         if (file.indexOf("~") >= 0) {
             int pos = file.lastIndexOf("~");
             setPath(file.substring(0, pos));
             setTitle(file.substring(pos + 1));
         } else {
             this.path = "";
             this.title = file;
         }
     }
 
     public boolean isDaySet(Calendar cal) {
         boolean[] days = getRepeatingDays();
         // days begins with 0 - Monday
         // Calendar begins with 1 - Sunday
         // so cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 matches the right day in
         // days
         return days[(cal.get(Calendar.DAY_OF_WEEK) + 5) % 7];
     }
 
     public Object clone() {
         VDRTimer timer = new VDRTimer();
         timer.setID(getID());
         timer.setState(getState());
         timer.setChannelNumber(getChannelNumber());
         timer.setDescription(getDescription());
         timer.setEndTime((Calendar)getEndTime().clone());
         timer.setFile(getFile());
         timer.setFirstTime((Calendar)getFirstTime().clone());
         timer.setHasFirstTime(hasFirstTime());
         timer.setChannelNumber(getChannelNumber());
         timer.setLifetime(getLifetime());
         timer.setPath(getPath());
         timer.setPriority(getPriority());
         timer.setRepeatingDays(getRepeatingDays().clone());
         timer.setStartTime((Calendar)getStartTime().clone());
         timer.setTitle(getTitle());
         return timer;
     }
 
     public int getID() {
         return ID;
     }
 
     public void setID(int id) {
         ID = id;
     }
 
     public int getState() {
         return state;
     }
     
     /**
      * Sets the state of a timer. To change a single part of the state, 
      * e.g. VPS or ACTIVE, please use {@link VDRTimer#changeStateTo(int, boolean)}
      * @param state The new state for the timer. 
      *          Bitwise OR of multiple states is possible. 
      *          E.g. setState(ACTIVE | VPS) sets the timer to ACTIVE and enables VPS
      */
     public void setState(int state) {
         this.state = state;
     }
     
     public void changeStateTo(int STATE, boolean enabled) {
         if(enabled && hasState(STATE) || !enabled && !hasState(STATE)) {
             // we don't have to change anything, because the timer already 
             // has the requested state 
             return;
         }
         
         int sign = enabled ? 1 : -1;
         state += sign * STATE;
     }
     
     public boolean isRecording() {
         if(hasState(ACTIVE) && hasState(RECORDING)) {
             return true;
         }
         
         Calendar now = Calendar.getInstance();
         if(now.after(getStartTime()) && now.before(getEndTime())) {
            return hasState(ACTIVE);
         }
         
         return false;
     }
 }
