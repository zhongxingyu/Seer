 /*
  * Copyright (c) Henrik Niehaus
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
 package lazybones.programmanager;
 
 import java.awt.event.MouseEvent;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.JPopupMenu;
 
 import lazybones.ChannelManager;
 import lazybones.ChannelManager.ChannelNotFoundException;
 import lazybones.LazyBones;
 import lazybones.LazyBonesTimer;
 import lazybones.TimerManager;
 import lazybones.VDRConnection;
 import lazybones.logging.LoggingConstants;
 import lazybones.logging.PopupHandler;
 import lazybones.programmanager.evaluation.DoppelpackDetector;
 import lazybones.programmanager.evaluation.Evaluator;
 import lazybones.programmanager.evaluation.Result;
 
 import org.hampelratte.svdrp.Response;
 import org.hampelratte.svdrp.commands.LSTE;
 import org.hampelratte.svdrp.parsers.EPGParser;
 import org.hampelratte.svdrp.responses.highlevel.Channel;
 import org.hampelratte.svdrp.responses.highlevel.EPGEntry;
 import org.hampelratte.svdrp.responses.highlevel.Timer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import util.programmouseevent.ProgramMouseEventHandler;
 import devplugin.Program;
 
 public class ProgramManager {
     private static transient Logger logger = LoggerFactory.getLogger(ProgramManager.class);
     private static transient Logger epgLog = LoggerFactory.getLogger(LoggingConstants.EPG_LOGGER);
     private static transient Logger popupLog = LoggerFactory.getLogger(PopupHandler.KEYWORD);
 
     private static ProgramManager instance;
 
     private final Evaluator evaluator = new Evaluator();
 
     private ProgramManager() {
     }
 
     public static ProgramManager getInstance() {
         if (instance == null) {
             instance = new ProgramManager();
         }
         return instance;
     }
 
     public LazyBonesTimer getTimerForTime(Calendar cal, devplugin.Channel chan) {
         long time_t = cal.getTimeInMillis() / 1000;
         Object o = ChannelManager.getChannelMapping().get(chan.getId());
         int channelNumber = ((Channel) o).getChannelNumber();
 
         LSTE cmd = new LSTE(channelNumber, time_t);
         Response res = VDRConnection.send(cmd);
         if (res != null && res.getCode() == 215) {
             List<EPGEntry> epg = new EPGParser().parse(res.getMessage());
             if (epg.size() > 0) {
                 EPGEntry entry = epg.get(0); // we can use the first element, because there will be only one item in the list
                 Timer timer = new Timer();
                 timer.setChannelNumber(channelNumber);
                 timer.setTitle(entry.getTitle());
                 timer.setStartTime(entry.getStartTime());
                 timer.setEndTime(entry.getEndTime());
                 timer.setDescription(entry.getDescription());
                 int prio = Integer.parseInt(LazyBones.getProperties().getProperty("timer.prio"));
                 int lifetime = Integer.parseInt(LazyBones.getProperties().getProperty("timer.lifetime"));
                 timer.setLifetime(lifetime);
                 timer.setPriority(prio);
                 return new LazyBonesTimer(timer);
             }
         }
         return null;
     }
 
     public JPopupMenu getContextMenuForTimer(LazyBonesTimer timer) {
         List<String> tvBrowserProgIds = timer.getTvBrowserProgIDs();
         JPopupMenu popup;
         if (tvBrowserProgIds.size() > 0) {
             Program prog = ProgramDatabase.getProgram(tvBrowserProgIds.get(0));
            popup = LazyBones.getPluginManager().createPluginContextMenu(prog, LazyBones.getInstance());
         } else {
             popup = LazyBones.getInstance().getSimpleContextMenu(timer);
         }
         return popup;
     }
 
     /**
      * Called to mark all Programs
      */
     public void markPrograms() {
         // for every timer
         try {
             for (LazyBonesTimer timer : TimerManager.getInstance().getTimers()) {
                 markSingularTimer(timer);
             }
         } catch (ConcurrentModificationException e) {
             // the timers list has changed while we were marking programs, let's start over
             logger.debug("Timers list has changed while marking programs. Beginning from scratch.");
 
             // first remove all previously set marks
             unmarkPrograms();
 
             // now mark all timers
             markPrograms();
         }
     }
 
     /**
      * Unmarks all programs, which are marked by LazyBones
      */
     public void unmarkPrograms() {
         Program[] markedPrograms = LazyBones.getPluginManager().getMarkedPrograms();
         for (Program marked : markedPrograms) {
             marked.unmark(LazyBones.getInstance());
         }
     }
 
     /**
      * Handles all timers, which couldn't be assigned automatically
      *
      */
     public void handleNotAssignedTimers() {
         if (Boolean.TRUE.toString().equals(LazyBones.getProperties().getProperty("supressMatchDialog"))) {
             return;
         }
         Iterator<LazyBonesTimer> iterator = TimerManager.getInstance().getNotAssignedTimers().iterator();
         logger.debug("Not assigned timers: {}", +TimerManager.getInstance().getNotAssignedTimers().size());
         while (iterator.hasNext()) {
             LazyBonesTimer timer = iterator.next();
             switch (timer.getReason()) {
             case LazyBonesTimer.NOT_FOUND:
                 // show message
                 java.util.Date date = new java.util.Date(timer.getStartTime().getTimeInMillis());
                 SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                 String dateString = sdf.format(date);
                 String title = timer.getPath() + timer.getTitle();
                 Channel chan = ChannelManager.getInstance().getChannelByNumber(timer.getChannelNumber());
                 String channelName = "unknown channel";
                 if (chan != null) {
                     channelName = chan.getName();
                 }
                 String msg = LazyBones.getTranslation("message_programselect",
                         "I couldn\'t find a program, which matches the vdr timer\n<b>{0}</b> at <b>{1}</b> on <b>{2}</b>.\n"
                                 + "You may assign this timer to a program in the context menu.", title, dateString, channelName);
                 popupLog.warn(msg);
                 break;
             case LazyBonesTimer.NO_EPG:
                 logger.warn("Couldn't assign timer: ", timer);
                 String mesg = LazyBones.getTranslation("noEPGdataTVB",
                         "<html>TV-Browser has no EPG-data the timer {0}.<br>Please update your EPG-data!</html>", timer.toString());
                 epgLog.error(mesg);
                 break;
             case LazyBonesTimer.NO_CHANNEL:
                 mesg = LazyBones.getTranslation("no_channel_defined", "No channel defined", timer.toString());
                 epgLog.error(mesg);
                 break;
             case LazyBonesTimer.NO_PROGRAM:
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
     private void markSingularTimer(LazyBonesTimer timer) {
         // get the day program of the day, the previous day and the next day
         List<Program> threeDayProgram;
         try {
             threeDayProgram = ProgramDatabase.getProgramAroundTimer(timer);
         } catch (ChannelNotFoundException e) {
             timer.setReason(LazyBonesTimer.NO_CHANNEL);
             return;
         }
 
         // Mark doppelpack timers. If we found a doppelpack, we can stop at this point
         boolean doppelpackFound = new DoppelpackDetector(threeDayProgram).markDoppelpackTimer(timer);
         if (doppelpackFound) {
             return;
         }
 
         // no doppelpacks, we can now evaluate the programs with
         // several criteria
         Result bestMatching = evaluator.evaluate(threeDayProgram, timer);
         if (bestMatching == null) {
             logger.warn("Couldn't assign timer: ", timer);
             timer.setReason(LazyBonesTimer.NOT_FOUND);
             return;
         }
 
         logger.debug("Best matching program for timer \"{}\" is \"{}\" with a percentage of {}", new Object[] { timer.getTitle(),
                 bestMatching.getProgram().getTitle(), bestMatching.getPercentage() });
         int threshold = Integer.parseInt(LazyBones.getProperties().getProperty("percentageThreshold"));
         // if the percentage of equality is higher than the config value
         // percentageThreshold, mark this program
         if (bestMatching.getPercentage() >= threshold) {
             assignTimerToProgram(bestMatching.getProgram(), timer);
         } else {
             // no candidate and no doppelpack found
             // look for the timer in stored timers
             boolean found = TimerManager.getInstance().lookUpTimer(timer, bestMatching.getProgram());
             if (!found) { // we have no mapping
                 logger.warn("Couldn't find a program with that title: ", timer.getTitle());
                 logger.warn("Couldn't assign timer: ", timer);
                 timer.setReason(LazyBonesTimer.NOT_FOUND);
             }
         }
     }
 
     public void assignTimerToProgram(Program prog, LazyBonesTimer timer) {
         timer.addTvBrowserProgID(prog.getUniqueID());
         prog.mark(LazyBones.getInstance());
         prog.validateMarking();
         // if (timer.isRepeating()) {
         // Date d = prog.getDate();
         // timer.getStartTime().set(Calendar.DAY_OF_MONTH, d.getDayOfMonth());
         // timer.getStartTime().set(Calendar.MONTH, d.getMonth()-1);
         // timer.getStartTime().set(Calendar.YEAR, d.getYear());
         // }
     }
 
     public void handleTimerDoubleClick(LazyBonesTimer timer, MouseEvent e) {
         List<String> progIDs = timer.getTvBrowserProgIDs();
         if (progIDs.size() > 0) {
             String firstProgID = progIDs.get(0);
             Program prog = ProgramDatabase.getProgram(firstProgID);
             if (prog != null) {
                 ProgramMouseEventHandler.handleProgramClick(prog, LazyBones.getInstance(), false, e);
             }
         }
     }
 }
