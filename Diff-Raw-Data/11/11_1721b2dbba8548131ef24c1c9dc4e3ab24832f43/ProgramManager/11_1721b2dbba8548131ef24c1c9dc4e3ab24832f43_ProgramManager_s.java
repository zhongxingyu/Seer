/* $Id: ProgramManager.java,v 1.20 2008-05-19 20:17:09 hampelratte Exp $
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
 package lazybones;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeMap;
 
 import javax.swing.JPopupMenu;
 
 import lazybones.logging.LoggingConstants;
 import lazybones.logging.PopupHandler;
 import lazybones.utils.Utilities;
 
 import org.hampelratte.svdrp.Response;
 import org.hampelratte.svdrp.commands.LSTE;
 import org.hampelratte.svdrp.responses.highlevel.Channel;
 import org.hampelratte.svdrp.responses.highlevel.EPGEntry;
 import org.hampelratte.svdrp.responses.highlevel.VDRTimer;
 import org.hampelratte.svdrp.util.EPGParser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import devplugin.Date;
 import devplugin.Program;
 
 public class ProgramManager {
     private static transient Logger logger = LoggerFactory.getLogger(ProgramManager.class);
     private static transient Logger epgLog = LoggerFactory.getLogger(LoggingConstants.EPG_LOGGER);
     private static transient Logger popupLog = LoggerFactory.getLogger(PopupHandler.KEYWORD);
     
     private static ProgramManager instance;
     
     private ProgramManager() {
     }
 
     public static ProgramManager getInstance() {
         if (instance == null) {
             instance = new ProgramManager();
         }
         return instance;
     }
     
     /**
      * @param startTime the startTime of the Program
      * @param middleTime the middleTime of the Program
      * @param chan the channel of the Program
      * @return the Program or null
      * 
      * startTime ist notwendig, weil getChannelDayProgram benutzt wird.
      * Bsp.: start 23:30 ende 01:00 middleTime würde dann schon am nächsten tag
      * liegen (00:15), so dass man nicht mehr das richtige channelDayProgram bekommt
      * und das Program nicht findet 
      */
     public Program getProgramAt(Calendar startTime, Calendar middleTime, devplugin.Channel chan) {
         Iterator<Program> dayProgram = LazyBones.getPluginManager().getChannelDayProgram(
                 new Date(startTime), chan);
         while (dayProgram != null && dayProgram.hasNext()) {
             Program prog = dayProgram.next();
             
             Calendar progStart = GregorianCalendar.getInstance();
             progStart.set(Calendar.YEAR, prog.getDate().getYear());
             progStart.set(Calendar.MONTH, prog.getDate().getMonth()-1);
             progStart.set(Calendar.DAY_OF_MONTH, prog.getDate().getDayOfMonth());
             progStart.set(Calendar.HOUR_OF_DAY, prog.getHours());
             progStart.set(Calendar.MINUTE, prog.getMinutes());
 
             Calendar progEnd = GregorianCalendar.getInstance();
             progEnd.setTimeInMillis(progStart.getTimeInMillis());
             progEnd.add(Calendar.MINUTE, prog.getLength());
             
             if (middleTime.after(progStart) && middleTime.before(progEnd)) {
                 return prog;
             }
         }
         return null;
     }
     
     // TODO schlechter name, weil Timer zurückgeliefert wird
     public Timer getVDRProgramAt(Calendar cal, devplugin.Channel chan) {
         long time_t = cal.getTimeInMillis() / 1000;
         Object o = ChannelManager.getChannelMapping().get(chan.getId());
         int channelNumber = ((Channel) o).getChannelNumber();
 
         LSTE cmd = new LSTE(channelNumber, time_t);
         Response res = VDRConnection.send(cmd);
         if (res != null && res.getCode() == 215) {
             List<EPGEntry> epg = EPGParser.parse(res.getMessage());
             if (epg.size() > 0) {
                 EPGEntry entry = epg.get(0); // we can use the first element, because there will be only one item in the list
                 VDRTimer timer = new VDRTimer();
                 timer.setChannelNumber(channelNumber);
                 timer.setTitle(entry.getTitle());
                 timer.setStartTime(entry.getStartTime());
                 timer.setEndTime(entry.getEndTime());
                 timer.setDescription(entry.getDescription());
                 int prio = Integer.parseInt(LazyBones.getProperties().getProperty("timer.prio"));
                 int lifetime = Integer.parseInt(LazyBones.getProperties().getProperty("timer.lifetime"));
                 timer.setLifetime(lifetime);
                 timer.setPriority(prio);
                 return new Timer(timer);
             }
         }
         return null;
     }
     
     public Program getProgram(Timer timer) {
         // determine channel
         devplugin.Channel chan = ChannelManager.getInstance().getChannel(timer);
         
         if(chan == null) 
             return null;
             
         // determine middle of the program
         long startTime = timer.getStartTime().getTimeInMillis();
         long endTime = timer.getEndTime().getTimeInMillis();
         long duration = endTime - startTime;
         Calendar time = GregorianCalendar.getInstance();
         long middleTime = startTime + duration/2;
         time.setTimeInMillis(middleTime);
         
         return getProgramAt(timer.getStartTime(), time, chan);
     }
     
     /**
      * 
      * @param time A Calendar object representing the day, the program is running at
      * @param progID the progID of the program
      * @return {@link devplugin.PluginManager#getProgram(Date, String)}
      */
     public Program getProgram(Calendar time, String progID) {
         return LazyBones.getPluginManager().getProgram(new devplugin.Date(time), progID);
     }
     
     public JPopupMenu getContextMenuForTimer(Timer timer) {
         List<String> tvBrowserProgIds = timer.getTvBrowserProgIDs();
         JPopupMenu popup;
         if(tvBrowserProgIds.size() > 0) {
             Program prog = ProgramManager.getInstance().getProgram(timer.getStartTime(), tvBrowserProgIds.get(0));
             popup = LazyBones.getPluginManager().createPluginContextMenu(prog, null);
         } else {
             popup = LazyBones.getInstance().getSimpleContextMenu(timer);
         }
         return popup;
     }
     
     /**
      * Called to mark all Programs
      */
     public void  markPrograms() {
         // for every timer
         for(Timer timer : TimerManager.getInstance().getTimers()) {
             devplugin.Channel chan = ChannelManager.getInstance().getChannel(timer);
             if (chan == null) {
                 timer.setReason(Timer.NO_CHANNEL);
 
                 // we couldn't find a channel for this timer, continue with the
                 // next timer
                 continue;
             }
 
             markSingularTimer(timer, chan);
         }
     }
     
     /**
      * Handles all timers, which couldn't be assigned automatically
      *
      */
     public void handleNotAssignedTimers() {
         if (Boolean.TRUE.toString().equals(
                 LazyBones.getProperties().getProperty("supressMatchDialog"))) {
             return;
         }
         Iterator<Timer> iterator = TimerManager.getInstance().getNotAssignedTimers().iterator();
         logger.debug("Not assigned timers: {}",
                 + TimerManager.getInstance().getNotAssignedTimers().size());
         while (iterator.hasNext()) {
             Timer timer = iterator.next();
             switch(timer.getReason()) {
             case Timer.NOT_FOUND:
                 //  show message
                 java.util.Date date = new java.util.Date(timer.getStartTime().getTimeInMillis());
                 SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                 String dateString = sdf.format(date);
                 String title = timer.getPath() + timer.getTitle();
                 Channel chan = ChannelManager.getInstance().getChannelByNumber(timer.getChannelNumber());
                 String msg = LazyBones.getTranslation("message_programselect", "I couldn\'t find a program, which matches the vdr timer\n<b>{0}</b> at <b>{1}</b> on <b>{2}</b>.\n" + "You may assign this timer to a program in the context menu.", title, dateString, chan.getName());
                 popupLog.warn(msg);
                 break;
             case Timer.NO_EPG:
                 logger.warn("Couldn't assign timer: ", timer);
                 String mesg = LazyBones.getTranslation("noEPGdataTVB","<html>TV-Browser has no EPG-data the timer {0}.<br>Please update your EPG-data!</html>",timer.toString());
                 epgLog.error(mesg);
                 break;
             case Timer.NO_CHANNEL:
                 mesg = LazyBones.getTranslation("no_channel_defined", "No channel defined", timer.toString());
                 epgLog.error(mesg);
                 break;
             case Timer.NO_PROGRAM:
                 // do nothing
                 break;
             default:
                 logger.debug("Not assigned timer: {}", timer);
             }
         }
     }
     
     /**
      * 
      * @param timer
      * @param chan
      */
     private void markSingularTimer(Timer timer, devplugin.Channel chan) {
         // create a clone of the timer and subtract the recording buffers
         Timer bufferLessTimer = timer.getTimerWithoutBuffers();
         int day = bufferLessTimer.getStartTime().get(Calendar.DAY_OF_MONTH);
         int month = bufferLessTimer.getStartTime().get(Calendar.MONTH) + 1;
         int year = bufferLessTimer.getStartTime().get(Calendar.YEAR);
 
         Date date = new Date(year, month, day);
 
         Iterator<Program> it = LazyBones.getPluginManager().getChannelDayProgram(date, chan);
         if (it != null) {
             // contains programs, which could be the right program for the timer
             TreeMap<Integer, Program> candidates = new TreeMap<Integer, Program>();
             
             // contains programs, which start and stop between the start and the stop time
             // of the timer and could be part of a Doppelpack
             ArrayList<Program> doppelPack = new ArrayList<Program>();
             
             while (it.hasNext()) { // iterate over all programs of one day and
                                     // compare start and end time
                 Program prog = it.next();
                 int startTime = prog.getStartTime();
                 int endTime = startTime + prog.getLength();
 
                 Calendar timerStartCal = bufferLessTimer.getStartTime();
                 int hour = timerStartCal.get(Calendar.HOUR_OF_DAY);
                 int minute = timerStartCal.get(Calendar.MINUTE);
                 int timerstart = hour * 60 + minute;
 
                 Calendar timerEndCal = bufferLessTimer.getEndTime();
                 hour = timerEndCal.get(Calendar.HOUR_OF_DAY);
                 minute = timerEndCal.get(Calendar.MINUTE);
                 int timerend = hour * 60 + minute;
                 timerend = timerend < timerstart ? timerend + 1440 : timerend;
 
                 int deltaStart = startTime - timerstart;
                 int deltaEnd = endTime - timerend;
 
                 // MAYBE zeittoleranz als option anbieten
                // collect candidates
                if (Math.abs(deltaStart) <= 20 && Math.abs(deltaEnd) <= 20) {
                     candidates.put(new Integer(Math.abs(deltaStart)), prog);
                 }
                 
                 // collect doppelpack candidates
                 // use timer with buffers
                 timerStartCal = timer.getStartTime();
                 hour = timerStartCal.get(Calendar.HOUR_OF_DAY);
                 minute = timerStartCal.get(Calendar.MINUTE);
                 timerstart = hour * 60 + minute;
                 timerEndCal = timer.getEndTime();
                 hour = timerEndCal.get(Calendar.HOUR_OF_DAY);
                 minute = timerEndCal.get(Calendar.MINUTE);
                 timerend = hour * 60 + minute;
                 timerend = timerend < timerstart ? timerend + 1440 : timerend;
                 if(startTime >= timerstart && endTime <= timerend) {
                     doppelPack.add(prog);
                 }
             }
 
             if (candidates.size() == 0) {
                 if (doppelPack.size() > 1) {
                     timer.setReason(Timer.NOT_FOUND); // if no doppelpack is found, this timer
                     // is a timer with weird start and end times
                     // then we have to show a ProgramConfirmDialog, so we set
                     // the reason to not found and if a doppelpack is detected we
                     // set the reason to no_reason
                     ArrayList<String> list = new ArrayList<String>();
                     String doppelpackTitle = null;
                     for (Program prog : doppelPack) {
                         String title = prog.getTitle();
                         if(list.contains(title)) {
                             logger.debug("Doppelpack found: {}", title);
                             timer.setReason(Timer.NO_REASON);
                             doppelpackTitle = title;
                         } else {
                             list.add(title);
                         }
                     }
                     
                     // mark all doppelpack programs
                     if(doppelpackTitle != null) {
                         for (Program prog : doppelPack) {
                             if(prog.getTitle().equals(doppelpackTitle)) {
                                 prog.mark(LazyBones.getInstance());
                                 timer.addTvBrowserProgID(prog.getID());
                             }
                         }
 
                     }
                     
                     // this timer is no doppelpack, but the times are weird
                     // we now look if the user has made a mapping before
                     if(timer.getReason() == Timer.NOT_FOUND) {
                         // lookup old mappings
                         boolean found = TimerManager.getInstance().lookUpTimer(timer, null);
                         if (!found) { // we have no mapping
                             logger.warn("Couldn't find a program with that title: ", timer.getTitle());
                             logger.warn("Couldn't assign timer: ", timer);
                             timer.setReason(Timer.NOT_FOUND);
                         } else {
                             timer.setReason(Timer.NO_REASON);
                         }
                     }
                 } else {
                     boolean found = TimerManager.getInstance().lookUpTimer(timer, null);
                     if (!found) {
                         timer.setReason(Timer.NOT_FOUND);
                     }
                 }
                 return;
             }
             
             // get the best fitting candidate. this is the first key, because
             // TreeMap is sorted
             Program progMin = (Program) candidates.get(candidates.firstKey());
 
             // calculate the precentage of common words
             int percentage = 0;
             int percentagePath = Utilities.percentageOfEquality(timer.getPath(), progMin.getTitle());
             int percentageTitle = Utilities.percentageOfEquality(timer.getTitle(), progMin.getTitle());
             int percentageBoth = Utilities.percentageOfEquality(timer.getPath() + timer.getTitle(), progMin.getTitle());
             percentage = Math.max(percentagePath, percentageTitle);
             percentage = Math.max(percentage, percentageBoth);
 
 
             // override the percentage
             if (timer.getFile().indexOf("EPISODE") >= 0
                     || timer.getFile().indexOf("TITLE") >= 0
                     || timer.isRepeating()) {
                 percentage = 100;
             }
             
             logger.debug("Percentage: {} {}", percentage, timer.toString());
 
             int threshold = Integer.parseInt(LazyBones.getProperties().getProperty("percentageThreshold"));
             // if the percentage of common words is
             // higher than the config value percentageThreshold, mark this
             // program
             if (percentage >= threshold) {
                 assignTimerToProgram(progMin, timer);
             } else {
                 boolean found = TimerManager.getInstance().lookUpTimer(timer, progMin);
                 if (!found) { // we have no mapping
                     logger.warn("Couldn't find a program with that title: ", timer.getTitle());
                     logger.warn("Couldn't assign timer: ", timer);
                     timer.setReason(Timer.NOT_FOUND);
                 }
             }
         } else { // no channeldayprogram was found
             if(!timer.isRepeating()) {
                 timer.setReason(Timer.NO_EPG);
             }
         }
     }
     
     public void assignTimerToProgram(Program prog, Timer timer) {
         prog.mark(LazyBones.getInstance());
         timer.addTvBrowserProgID(prog.getID());
         if (timer.isRepeating()) {
             Date d = prog.getDate();
             timer.getStartTime().set(Calendar.DAY_OF_MONTH, d.getDayOfMonth());
             timer.getStartTime().set(Calendar.MONTH, d.getMonth()-1);
             timer.getStartTime().set(Calendar.YEAR, d.getYear());
         }
     }
     
     public void handleTimerDoubleClick(Timer timer) {
         List<String> progIDs = timer.getTvBrowserProgIDs();
         if(progIDs.size() > 0) {
             String firstProgID = progIDs.get(0);
             Program prog = ProgramManager.getInstance().getProgram(timer.getStartTime(), firstProgID);
             if(prog != null) {
                 LazyBones.getPluginManager().handleProgramDoubleClick(prog);
             }
         }
     }
 }
