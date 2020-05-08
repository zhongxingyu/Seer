 /*
  * Copyright (c) Henrik Niehaus & Lazy Bones development team
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
 
 import java.awt.Cursor;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.TreeSet;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.swing.JOptionPane;
 
 import lazybones.actions.CreateTimerAction;
 import lazybones.actions.DeleteTimerAction;
 import lazybones.actions.ModifyTimerAction;
 import lazybones.actions.VDRAction;
 import lazybones.actions.responses.ConnectionProblem;
 import lazybones.gui.components.timeroptions.TimerOptionsDialog;
 import lazybones.gui.components.timeroptions.TimerOptionsPanel;
 import lazybones.gui.timers.TimerSelectionDialog;
 import lazybones.logging.LoggingConstants;
 import lazybones.programmanager.ProgramManager;
 import lazybones.utils.Utilities;
 
 import org.hampelratte.svdrp.Connection;
 import org.hampelratte.svdrp.Response;
 import org.hampelratte.svdrp.Version;
 import org.hampelratte.svdrp.commands.LSTE;
 import org.hampelratte.svdrp.commands.LSTT;
 import org.hampelratte.svdrp.parsers.EPGParser;
 import org.hampelratte.svdrp.parsers.TimerParser;
 import org.hampelratte.svdrp.responses.highlevel.Channel;
 import org.hampelratte.svdrp.responses.highlevel.EPGEntry;
 import org.hampelratte.svdrp.responses.highlevel.Timer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import devplugin.Date;
 import devplugin.Program;
 
 /**
  * Class to manage all timers.
  * 
  * @author <a href="hampelratte@users.sf.net">hampelratte@users.sf.net</a>
  */
 public class TimerManager extends Observable {
 
     private static transient Logger logger = LoggerFactory.getLogger(TimerManager.class);
     private static transient Logger conLog = LoggerFactory.getLogger(LoggingConstants.CONNECTION_LOGGER);
 
     private static TimerManager instance;
 
     /**
      * Stores all timers as Timer objects
      */
     private final List<LazyBonesTimer> timers;
 
     /**
      * The VDR timers from the last session, which have been stored to disk
      */
     private List<LazyBonesTimer> storedTimers = new ArrayList<LazyBonesTimer>();
 
     /**
      * Stores mappings the user has made for later use. The user has to map one Program only once. Later the mapping will be looked up here
      */
     private TitleMapping titleMapping = new TitleMapping();
 
     private final Cursor WAITING_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
     private final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
 
     private TimerManager() {
         timers = new ArrayList<LazyBonesTimer>();
     }
 
     public synchronized static TimerManager getInstance() {
         if (instance == null) {
             instance = new TimerManager();
         }
         return instance;
     }
 
     private void addTimer(LazyBonesTimer timer, boolean calculateRepeatingTimers, boolean notifyObservers) {
         if (!timer.isRepeating() || !calculateRepeatingTimers) {
             timerListLock.lock();
             timers.add(timer);
             timerListLock.unlock();
         } else {
             Calendar startTime = timer.getStartTime();
             Calendar endTime = timer.getEndTime();
             long duration = endTime.getTimeInMillis() - startTime.getTimeInMillis();
 
             if (timer.hasFirstTime()) {
                 Calendar tmp = timer.getFirstTime();
                 startTime.set(Calendar.DAY_OF_MONTH, tmp.get(Calendar.DAY_OF_MONTH));
                 startTime.set(Calendar.MONTH, tmp.get(Calendar.MONTH));
                 startTime.set(Calendar.YEAR, tmp.get(Calendar.YEAR));
             }
 
             if (calculateRepeatingTimers) {
                 for (int j = 0; j < 21; j++) { // next 3 weeks, more data is never available in TVB
                     Calendar tmpStart = (Calendar) startTime.clone();
                     tmpStart.add(Calendar.DAY_OF_MONTH, j);
                     if (timer.isDaySet(tmpStart)) {
                         LazyBonesTimer oneDayTimer = (LazyBonesTimer) timer.clone();
                         oneDayTimer.setStartTime(tmpStart);
                         long start = tmpStart.getTimeInMillis();
                         oneDayTimer.getEndTime().setTimeInMillis(start + duration);
                         timerListLock.lock();
                         timers.add(oneDayTimer);
                         timerListLock.unlock();
                     }
                 }
             }
         }
 
         if (notifyObservers) {
             setChanged();
             notifyObservers(new TimersChangedEvent(TimersChangedEvent.TIMER_ADDED, timer));
         }
     }
 
     /**
      * Removes this timer from the internal list. This will result in a disappearence of this timer in any GUI element.
      * 
      * <string>Note!</strong>This method will not delete the timer on the VDR. To achieve that, use {@link #deleteTimer(Timer)}
      * 
      * @param timer
      */
     public void removeTimer(LazyBonesTimer timer) {
         timerListLock.lock();
         timers.remove(timer);
         timerListLock.unlock();
         setChanged();
         notifyObservers(new TimersChangedEvent(TimersChangedEvent.TIMER_REMOVED, timer));
     }
 
     /**
      * @return a List of Timer objects
      */
     public List<LazyBonesTimer> getTimers() {
         return timers;
     }
 
     /**
      * @param vdrTimers
      *            an List of Timer objects
      */
     public void setTimers(List<LazyBonesTimer> vdrTimers, boolean calculateRepeatingTimers) {
         for (LazyBonesTimer timer : vdrTimers) {
             addTimer(new LazyBonesTimer(timer), calculateRepeatingTimers, false);
         }
 
         // try to mark programs
         ProgramManager.getInstance().markPrograms();
         List<LazyBonesTimer> notAssigned = TimerManager.getInstance().getNotAssignedTimers();
         if (notAssigned.size() > 0) {
             ProgramManager.getInstance().handleNotAssignedTimers();
         }
 
         // notify observers, that the timers have changed
         setChanged();
         notifyObservers(new TimersChangedEvent(TimersChangedEvent.ALL, getTimers()));
     }
 
     /**
      * 
      * @param prog
      *            a devplugin.Program object
      * @return the timer for this program or null
      * @see Program
      */
     private final Lock timerListLock = new ReentrantLock();
 
     public LazyBonesTimer getTimer(Program prog) {
         String progID = prog.getUniqueID();
         if (progID == null) {
             logger.warn("Unique program ID is null. Unable to find a timer for this program");
             return null;
         }
 
         Calendar cal = prog.getDate().getCalendar();
         timerListLock.lock();
         try {
             for (LazyBonesTimer timer : timers) {
                 List<String> tvBrowserProdIDs = timer.getTvBrowserProgIDs();
                 for (String curProgID : tvBrowserProdIDs) {
                     if (progID.equals(curProgID)) {
                         LazyBonesTimer bufferless = timer.getTimerWithoutBuffers();
                         if (tvBrowserProdIDs.size() == 1 || Utilities.sameDay(cal, bufferless.getStartTime())) {
                             return timer;
                         }
                     }
                 }
             }
         } finally {
             timerListLock.unlock();
         }
         return null;
     }
 
     /**
      * 
      * @param timerNumber
      *            The number of the timer
      * @return The timer with the specified number
      */
     public LazyBonesTimer getTimer(int timerNumber) {
         timerListLock.lock();
         try {
             for (LazyBonesTimer timer : timers) {
                 if (timer.getID() == timerNumber) {
                     return timer;
                 }
             }
         } finally {
             timerListLock.unlock();
         }
         return null;
     }
 
     /**
      * Returns all timers, which couldn't be assigned to a program
      * 
      * @return an ArrayList with Timer objects
      */
     public List<LazyBonesTimer> getNotAssignedTimers() {
         ArrayList<LazyBonesTimer> list = new ArrayList<LazyBonesTimer>();
         timerListLock.lock();
         try {
             for (LazyBonesTimer timer : timers) {
                 if (!timer.isAssigned()) {
                     list.add(timer);
                 }
             }
         } finally {
             timerListLock.unlock();
         }
         return list;
     }
 
     /**
      * For DEBUG only - print all timers to System.out
      * 
      */
     public void printTimers() {
         System.out.println("########## Listing timers #################");
         for (LazyBonesTimer timer : timers) {
             System.out.println(timer);
         }
         System.out.println("################ End ######################");
     }
 
     public List<LazyBonesTimer> getStoredTimers() {
         return storedTimers;
     }
 
     public void setStoredTimers(List<LazyBonesTimer> storedTimers) {
         this.storedTimers = storedTimers;
     }
 
     /**
      * Checks storedTimers, if this timer has been mapped to Program before
      * 
      * @param timer
      * @return the ProgramID or null
      */
     public List<String> hasBeenMappedBefore(LazyBonesTimer timer) {
         for (LazyBonesTimer storedTimer : storedTimers) {
             if (timer.getUniqueKey().equals(storedTimer.getUniqueKey())) {
                 if (storedTimer.getReason() == LazyBonesTimer.NO_PROGRAM) {
                     List<String> timers = new ArrayList<String>();
                     timers.add("NO_PROGRAM");
                     return timers;
                 } else {
                     return storedTimer.getTvBrowserProgIDs();
                 }
             }
         }
         return null;
     }
 
     public void replaceStoredTimer(LazyBonesTimer timer) {
         for (LazyBonesTimer storedTimer : storedTimers) {
             if (timer.getUniqueKey().equals(storedTimer.getUniqueKey())) {
                 storedTimers.remove(storedTimer);
                 storedTimers.add(timer);
                 return;
             }
         }
 
         // timer couldn't be found -> this is a new timer
         storedTimers.add(timer);
     }
 
     /**
      * 
      * @see TimerManager#titleMapping
      */
     public Map<String, String> getTitleMappingValues() {
         return titleMapping.getAsMap();
     }
 
     /**
      * 
      * @see TimerManager#titleMapping
      */
     public void setTitleMappingValues(Map<String, String> titleMapping) {
         this.titleMapping.setMappingFromMap(titleMapping);
     }
 
     /**
      * Returns the next day, on which a timer events starts or stops, after the given calendar
      * 
      * @param currentDay
      * @return the next day, on which a timer events starts or stops, after the given calendar
      */
     public Calendar getNextDayWithEvent(Calendar currentDay) {
         List<LazyBonesTimer> timers = TimerManager.getInstance().getTimers();
         TreeSet<Calendar> events = new TreeSet<Calendar>();
         for (Iterator<LazyBonesTimer> iter = timers.iterator(); iter.hasNext();) {
             LazyBonesTimer timer = iter.next();
             events.add(timer.getStartTime());
             events.add(timer.getEndTime());
         }
 
         for (Iterator<Calendar> iter = events.iterator(); iter.hasNext();) {
             Calendar event = iter.next();
             if (!event.before(currentDay) & !Utilities.sameDay(event, currentDay)) {
                 return event;
             }
         }
 
         return null;
     }
 
     /**
      * @see #getNextDayWithEvent(Calendar)
      * @param currentDay
      * @return
      */
     public Calendar getPreviousDayWithEvent(Calendar currentDay) {
         List<LazyBonesTimer> timers = TimerManager.getInstance().getTimers();
         TreeSet<Calendar> events = new TreeSet<Calendar>();
         for (Iterator<LazyBonesTimer> iter = timers.iterator(); iter.hasNext();) {
             LazyBonesTimer timer = iter.next();
             events.add(timer.getStartTime());
             events.add(timer.getEndTime());
         }
 
         ArrayList<Calendar> eventList = new ArrayList<Calendar>(events);
         Collections.reverse(eventList);
         for (Iterator<Calendar> iter = eventList.iterator(); iter.hasNext();) {
             Calendar event = iter.next();
             if (!event.after(currentDay) & !Utilities.sameDay(event, currentDay)) {
                 return event;
             }
         }
 
         return null;
     }
 
     /**
      * @see #getNextDayWithEvent(Calendar)
      * @param currentDay
      * @return
      */
     public boolean hasNextDayWithEvent(Calendar currentDay) {
         return getNextDayWithEvent(currentDay) != null;
     }
 
     /**
      * @see #getPreviousDayWithEvent(Calendar)
      * @param currentDay
      * @return
      */
     public boolean hasPreviousDayWithEvent(Calendar currentDay) {
         return getPreviousDayWithEvent(currentDay) != null;
     }
 
     public TitleMapping getTitleMapping() {
         return this.titleMapping;
     }
 
     public void setTitleMapping(TitleMapping mapping) {
         this.titleMapping = mapping;
     }
 
     /**
      * Fetches the timer list from vdr
      */
     public synchronized void synchronize() {
         LazyBones.getInstance().getParent().setCursor(WAITING_CURSOR);
         LazyBones.getInstance().getMainDialog().setCursor(WAITING_CURSOR);
 
         // unmark all tvbrowser programs
         unmarkPrograms();
 
         // clear timer list
         timerListLock.lock();
         this.timers.clear();
         timerListLock.unlock();
 
         // fetch current timer list from vdr
         Response res = VDRConnection.send(new LSTT());
         if (res != null && res.getCode() == 250) {
             logger.info("Timers retrieved from VDR");
             String timersString = res.getMessage();
             List<Timer> vdrtimers = TimerParser.parse(timersString);
             List<LazyBonesTimer> timers = new ArrayList<LazyBonesTimer>();
             for (Timer vdrtimer : vdrtimers) {
                 timers.add(new LazyBonesTimer(vdrtimer));
             }
             setTimers(timers, true);
 
             // update recording list if necessary
             boolean updateRecordings = false;
             for (Timer timer : vdrtimers) {
                 if (timer.isRecording()) {
                     updateRecordings = true;
                     break;
                 }
             }
             if (updateRecordings) {
                 RecordingManager.getInstance().synchronize();
             }
         } else if (res != null && res.getCode() == 550) {
             // no timers are defined, do nothing
             logger.info("No timer defined on VDR");
             setChanged();
             notifyObservers(new TimersChangedEvent(TimersChangedEvent.ALL, getTimers()));
         } else { /*
          * something went wrong, we have no timers -> load the stored ones
          */
             conLog.error(LazyBones.getTranslation("using_stored_timers", "Couldn't retrieve timers from VDR, using stored ones."));
 
             List<LazyBonesTimer> vdrtimers = getStoredTimers();
             setTimers(vdrtimers, false);
         }
 
         // handle conflicts, if some have been detected
         ConflictFinder.getInstance().handleConflicts();
 
         LazyBones.getInstance().getParent().setCursor(DEFAULT_CURSOR);
         LazyBones.getInstance().getMainDialog().setCursor(DEFAULT_CURSOR);
     }
 
     /**
      * Unmarks all programs, which are marked by LazyBones
      */
     private void unmarkPrograms() {
         Program[] markedPrograms = LazyBones.getPluginManager().getMarkedPrograms();
         for (Program marked : markedPrograms) {
             marked.unmark(LazyBones.getInstance());
         }
     }
 
     /**
      * Deletes a timer on the VDR
      * 
      * @param timer
      *            timer to delete
      */
     public void deleteTimer(final LazyBonesTimer timer) {
         deleteTimer(timer, null);
     }
 
     /**
      * Deletes a timer on the VDR
      * 
      * @param timer
      *            timer to delete
      * @param callback
      *            a Runnable object, which is run after the delete process is finished
      */
     public void deleteTimer(final LazyBonesTimer timer, final Runnable callback) {
         VDRCallback _callback = new VDRCallback() {
             @Override
             public void receiveResponse(VDRAction cmd, Response response) {
                 if (!cmd.isSuccess()) {
                     logger.error(LazyBones.getTranslation("couldnt_delete", "Couldn't delete timer:") + " " + cmd.getResponse().getMessage());
                 } else {
                     synchronize();
                 }
 
                 if (callback != null) {
                     callback.run();
                 }
             }
         };
         DeleteTimerAction dta = new DeleteTimerAction(timer, _callback);
         dta.enqueue();
     }
 
     public void createTimer() {
         LazyBonesTimer timer = new LazyBonesTimer();
         timer.setChannelNumber(1);
         Program prog = ProgramManager.getInstance().getProgram(timer);
 
         // in this situation it makes sense to show the timer options
         // so we override the user setting (hide options dialog)
         boolean showTimerOptions = Boolean.TRUE.toString().equals(LazyBones.getProperties().getProperty("showTimerOptionsDialog"));
         LazyBones.getProperties().setProperty("showTimerOptionsDialog", Boolean.TRUE.toString());
         createTimer(prog, false);
         LazyBones.getProperties().setProperty("showTimerOptionsDialog", Boolean.toString(showTimerOptions));
     }
 
     /**
      * Creates a new timer on the VDR
      * 
      * @param prog
      *            the Program to create a timer for
      * @param automatic
      *            supresses all user interaction
      */
     public void createTimer(Program prog, boolean automatic) {
         if (prog.isExpired()) {
             if (!automatic) {
                 logger.error(LazyBones.getTranslation("expired", "This program has expired"));
             }
             return;
         }
 
         Calendar cal = GregorianCalendar.getInstance();
         Date date = prog.getDate();
         cal.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
         cal.set(Calendar.MONTH, date.getMonth() - 1);
         cal.set(Calendar.YEAR, date.getYear());
         cal.set(Calendar.HOUR_OF_DAY, prog.getHours());
         cal.set(Calendar.MINUTE, prog.getMinutes());
         cal.add(Calendar.MINUTE, prog.getLength() / 2);
         long millis = cal.getTimeInMillis();
 
         Object o = ChannelManager.getChannelMapping().get(prog.getChannel().getId());
         if (o == null) {
             logger.error(LazyBones.getTranslation("no_channel_defined", "No channel defined", prog.toString()));
             return;
         }
         int id = ((Channel) o).getChannelNumber();
         Response res = VDRConnection.send(new LSTE(Integer.toString(id), "at " + Long.toString(millis / 1000)));
 
         if (res != null && res.getCode() == 215) {
             List<EPGEntry> epgList = new EPGParser().parse(res.getMessage());
 
             if (epgList.size() <= 0) {
                 noEPGAvailable(prog, id, automatic);
                 return;
             }
 
             /*
              * VDR 1.3 already returns the matching entry, for 1.2 we need to search for a match
              */
             Version version = Connection.getVersion();
             boolean isOlderThan1_3 = version.getMajor() < 1 || (version.getMajor() == 1 && version.getMinor() < 3);
             EPGEntry vdrEPG = isOlderThan1_3 ? Utilities.filterEPGDate(epgList, ((Channel) o).getName(), millis) : (EPGEntry) epgList.get(0);
 
             LazyBonesTimer timer = new LazyBonesTimer();
             timer.setChannelNumber(id);
             timer.addTvBrowserProgID(prog.getUniqueID());
             int prio = Integer.parseInt(LazyBones.getProperties().getProperty("timer.prio"));
             timer.setPriority(prio);
             int lifetime = Integer.parseInt(LazyBones.getProperties().getProperty("timer.lifetime"));
             timer.setLifetime(lifetime);
 
             int buffer_before = Integer.parseInt(LazyBones.getProperties().getProperty("timer.before"));
             int buffer_after = Integer.parseInt(LazyBones.getProperties().getProperty("timer.after"));
 
             if (vdrEPG != null) {
                 boolean vpsDefault = Boolean.parseBoolean(LazyBones.getProperties().getProperty("vps.default"));
 
                 // set start and end time
                 Calendar calStart = vdrEPG.getStartTime();
                 timer.setStartTime(calStart);
                 Calendar calEnd = vdrEPG.getEndTime();
                 timer.setEndTime(calEnd);
 
                 // if we have a vps timer, set the status to vps, otherwise
                 // add the buffers
                 if (vpsDefault) {
                     timer.changeStateTo(Timer.VPS, true);
                 } else {
                     // start the recording x min before the beginning of the program
                     calStart.add(Calendar.MINUTE, -buffer_before);
 
                     // stop the recording x min after the end of the program
                     calEnd.add(Calendar.MINUTE, buffer_after);
                 }
 
                 timer.setFile(vdrEPG.getTitle());
 
                 // set the default directory, if it is configured in the settings
                 String defaultDirectory = LazyBones.getProperties().getProperty("default.directory");
                 if (defaultDirectory != null && !defaultDirectory.isEmpty()) {
                     timer.setPath(defaultDirectory);
                 }
 
                 // set the description
                 String descVdr = timer.getDescription() == null ? "" : timer.getDescription();
                 String descTvb = prog != null ? prog.getDescription() != null ? prog.getDescription() : "" : "";
                 int useTvbDescription = Integer.parseInt(LazyBones.getProperties().getProperty("descSourceTvb"));
                 switch (useTvbDescription) {
                 case TimerOptionsPanel.DESC_VDR:
                     timer.setDescription(descVdr);
                     break;
                 case TimerOptionsPanel.DESC_TVB:
                     timer.setDescription(descTvb);
                     break;
                 case TimerOptionsPanel.DESC_LONGEST:
                     if (descVdr.length() < descTvb.length()) {
                         timer.setDescription(descTvb);
                     } else {
                         timer.setDescription(descVdr);
                     }
                     break;
                 default:
                     timer.setDescription(descVdr);
                     break;
                 }
             } else { // VDR has no EPG data
                 noEPGAvailable(prog, id, automatic);
                 return;
             }
 
             boolean showOptionsDialog = !automatic && Boolean.TRUE.toString().equals(LazyBones.getProperties().getProperty("showTimerOptionsDialog"));
 
             if (showOptionsDialog) {
                 TimerOptionsDialog tod = new TimerOptionsDialog(timer, prog, TimerOptionsDialog.Mode.NEW);
                 if (tod.isAccepted()) {
                     commitTimer(tod.getTimer(), tod.getOldTimer(), tod.getProgram(), false, false);
                 }
             } else {
                 commitTimer(timer, null, prog, false, automatic);
             }
 
         } else if (res != null && res.getCode() == 550 & "No schedule found\n".equals(res.getMessage())) {
             noEPGAvailable(prog, id, automatic);
         } else {
             if (res instanceof ConnectionProblem) {
                 conLog.error(LazyBones.getTranslation("couldnt_create", "Couldn\'t create timer\n: ") + " " + res.getMessage());
             } else {
                 String msg = res != null ? res.getMessage() : "Reason unknown";
                 logger.error(LazyBones.getTranslation("couldnt_create", "Couldn\'t create timer\n: ") + " " + msg);
             }
         }
     }
 
     /**
      * 
      * @param prog
      *            Program to create a timer for
      * @param channelNumber
      *            the corresponding VDR channel
      * @param automatic
      *            supresses all user interaction
      */
     private void noEPGAvailable(Program prog, int channelNumber, boolean automatic) {
         int buffer_before = Integer.parseInt(LazyBones.getProperties().getProperty("timer.before"));
         int buffer_after = Integer.parseInt(LazyBones.getProperties().getProperty("timer.after"));
 
         boolean dontCare = automatic || Boolean.FALSE.toString().equals(LazyBones.getProperties().getProperty("logEPGErrors"));
         int result = JOptionPane.NO_OPTION;
         if (!dontCare) {
             result = JOptionPane.showConfirmDialog(null, LazyBones.getTranslation("noEPGdata", ""), "", JOptionPane.YES_NO_OPTION);
         }
         if (dontCare || result == JOptionPane.OK_OPTION) {
             LazyBonesTimer newTimer = new LazyBonesTimer();
             newTimer.setState(Timer.ACTIVE);
             newTimer.setChannelNumber(channelNumber);
             int prio = Integer.parseInt(LazyBones.getProperties().getProperty("timer.prio"));
             int lifetime = Integer.parseInt(LazyBones.getProperties().getProperty("timer.lifetime"));
             newTimer.setLifetime(lifetime);
             newTimer.setPriority(prio);
             newTimer.setTitle(prog.getTitle());
             newTimer.addTvBrowserProgID(prog.getUniqueID());
 
             Calendar startTime = prog.getDate().getCalendar();
             int start = prog.getStartTime();
             int hour = start / 60;
             int minute = start % 60;
             startTime.set(Calendar.HOUR_OF_DAY, hour);
             startTime.set(Calendar.MINUTE, minute);
 
             Calendar endTime = (Calendar) startTime.clone();
             endTime.add(Calendar.MINUTE, prog.getLength());
 
             // add buffers
             startTime.add(Calendar.MINUTE, -buffer_before);
             newTimer.setStartTime(startTime);
             endTime.add(Calendar.MINUTE, buffer_after);
             newTimer.setEndTime(endTime);
 
             if (automatic) {
                 commitTimer(newTimer, null, prog, false, true);
             } else {
                 TimerOptionsDialog tod = new TimerOptionsDialog(newTimer, prog, TimerOptionsDialog.Mode.NEW);
                 if (tod.isAccepted()) {
                     commitTimer(tod.getTimer(), tod.getOldTimer(), tod.getProgram(), false, false);
                 }
             }
         }
     }
 
     /**
      * Commits a new or changed timer to VDR
      * 
      * @param timer
      *            The new created / updated Timer
      * @param oldTimer
      *            A clone of the timer with the old settings. Can be null for new timers.
      * @param prog
      *            The according Program
      * @param update
      *            If the Timer is a new one or if the timer has been edited
      * @param automatic
      *            Supresses all user interaction
      */
     private void commitTimer(final LazyBonesTimer timer, LazyBonesTimer oldTimer, final Program prog, boolean update, boolean automatic) {
         int id = -1;
         if (prog != null) {
             Channel chan = ChannelManager.getChannelMapping().get(prog.getChannel().getId());
             if (chan == null) {
                 logger.error(LazyBones.getTranslation("no_channel_defined", "No channel defined", prog.toString()));
                 return;
             }
             id = chan.getChannelNumber();
         }
 
         if (update) {
             VDRCallback callback = new VDRCallback() {
                 @Override
                 public void receiveResponse(VDRAction cmd, Response response) {
                     if (cmd.isSuccess()) {
                         TimerManager.getInstance().synchronize();
                     } else {
                         String mesg = LazyBones.getTranslation("couldnt_change", "Couldn\'t change timer:") + " " + cmd.getResponse().getMessage();
                         logger.error(mesg);
                     }
                 }
             };
             ModifyTimerAction mta = new ModifyTimerAction(timer, oldTimer);
             mta.setCallback(callback);
             mta.enqueue();
         } else {
             if (timer.getTitle() != null) {
                 int percentage;
                 if (timer.getPath() != null && timer.getPath() != "") {
                     percentage = Utilities.percentageOfEquality(prog.getTitle(), timer.getPath() + timer.getTitle());
                 } else {
                     percentage = Utilities.percentageOfEquality(prog.getTitle(), timer.getTitle());
                 }
                 if (timer.getFile().indexOf("EPISODE") >= 0 || timer.getFile().indexOf("TITLE") >= 0 || timer.isRepeating() || automatic) {
                     percentage = 100;
                 }
                 int threshold = Integer.parseInt(LazyBones.getProperties().getProperty("percentageThreshold"));
                 if (percentage > threshold) {
                     CreateTimerAction cta = new CreateTimerAction(timer);
                     cta.enqueue();
                 } else {
                     logger.debug("Looking in title mapping for timer {}", timer);
                     // lookup in mapping history
                     TimerManager tm = TimerManager.getInstance();
                     String timerTitle = tm.getTitleMapping().getVdrTitle(prog.getTitle());
                     if (timer.getTitle().equals(timerTitle)) {
                         VDRCallback callback = new VDRCallback() {
                             @Override
                             public void receiveResponse(VDRAction cmd, Response response) {
                                 if (cmd.isSuccess()) {
                                     timer.addTvBrowserProgID(prog.getUniqueID());
                                     replaceStoredTimer(timer);
                                 }
                             }
                         };
                         CreateTimerAction cta = new CreateTimerAction(timer);
                         cta.setCallback(callback);
                         cta.enqueue();
                     } else { // no mapping found -> ask the user
                         showTimerConfirmDialog(timer, prog);
                     }
                 }
             } else { // VDR has no EPG data
                 noEPGAvailable(prog, id, automatic);
             }
         }
     }
 
     public void assignProgramToTimer(Program prog, LazyBonesTimer timer) {
         timer.addTvBrowserProgID(prog.getUniqueID());
         replaceStoredTimer(timer);
         getTitleMapping().put(prog.getTitle(), timer.getTitle());
     }
 
     /**
      * If a Program can't be assigned to a VDR-Program, this method shows a dialog to select the right VDR-Program
      * 
      * @param prog
      *            the Program selected in TV-Browser
      * @param timerOptions
      *            the timer from TimerOptionsDialog
      */
     private void showTimerConfirmDialog(LazyBonesTimer timerOptions, Program prog) {
         // get all programs 2 hours before and after the given program
         Calendar cal = GregorianCalendar.getInstance();
         Date date = prog.getDate();
         cal.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
         cal.set(Calendar.MONTH, date.getMonth() - 1);
         cal.set(Calendar.YEAR, date.getYear());
         cal.set(Calendar.HOUR_OF_DAY, prog.getHours());
         cal.set(Calendar.MINUTE, prog.getMinutes());
         cal.add(Calendar.MINUTE, prog.getLength() / 2);
 
         devplugin.Channel chan = prog.getChannel();
         TreeSet<LazyBonesTimer> programSet = new TreeSet<LazyBonesTimer>();
 
         // get the program for the timer's time
         Calendar c = GregorianCalendar.getInstance();
         c.setTimeInMillis(cal.getTimeInMillis());
         LazyBonesTimer t = ProgramManager.getInstance().getTimerForTime(c, chan);
         if (t != null) {
             programSet.add(t);
         }
 
         for (int i = 10; i <= 120; i += 10) {
             // get the program before the given one
             c = GregorianCalendar.getInstance();
             c.setTimeInMillis(cal.getTimeInMillis());
             c.add(Calendar.MINUTE, i * -1);
             LazyBonesTimer t1 = ProgramManager.getInstance().getTimerForTime(c, chan);
             if (t1 != null) {
                 programSet.add(t1);
             }
 
             // get the program after the given one
             c = GregorianCalendar.getInstance();
             c.setTimeInMillis(cal.getTimeInMillis());
             c.add(Calendar.MINUTE, i);
             LazyBonesTimer t2 = ProgramManager.getInstance().getTimerForTime(c, chan);
             if (t2 != null) {
                 programSet.add(t2);
             }
         }
 
         Program[] programs = new Program[programSet.size()];
         int i = 0;
         for (LazyBonesTimer timer : programSet) {
             Calendar time = timer.getStartTime();
             TimerProgram p = new TimerProgram(chan, new Date(time), time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));
             p.setTitle(timer.getTitle());
             p.setDescription("");
             p.setTimer(timer);
             programs[i++] = p;
         }
 
         // reverse the order of the programs
         Program[] temp = new Program[programs.length];
         for (int j = 0; j < programs.length; j++) {
             temp[j] = programs[programs.length - 1 - j];
         }
         programs = temp;
 
         // show dialog
         new TimerSelectionDialog(programs, timerOptions, prog);
 
         LazyBones.getInstance().synchronize();
     }
 
     public void deleteTimer(final Program prog) {
         LazyBonesTimer timer = TimerManager.getInstance().getTimer(prog);
         logger.debug("Deleting timer {}", timer);
         VDRCallback callback = new VDRCallback() {
             @Override
             public void receiveResponse(VDRAction cmd, Response response) {
                 if (cmd instanceof DeleteTimerAction) {
                     if (!cmd.isSuccess()) {
                         logger.error(LazyBones.getTranslation("couldnt_delete", "Couldn\'t delete timer:") + " " + cmd.getResponse().getMessage());
                         return;
                     }
 
                     prog.unmark(LazyBones.getInstance());
                     TimerManager.getInstance().synchronize();
                 }
             }
         };
         DeleteTimerAction dta = new DeleteTimerAction(timer, callback);
         dta.enqueue();
     }
 
     public void editTimer(LazyBonesTimer timer) {
         Program prog = null;
         if (timer.getTvBrowserProgIDs().size() > 0) {
             prog = ProgramManager.getInstance().getProgram(timer.getTvBrowserProgIDs().get(0));
         } else {
             logger.warn("Timer has no program IDs assigned.");
         }
         TimerOptionsDialog tod = new TimerOptionsDialog(timer, prog, TimerOptionsDialog.Mode.UPDATE);
         if (tod.isAccepted()) {
             commitTimer(tod.getTimer(), tod.getOldTimer(), tod.getProgram(), true, false);
         }
     }
 
     public boolean lookUpTimer(LazyBonesTimer timer, Program candidate) {
         logger.debug("Looking in storedTimers for: {}", timer.toString());
         List<String> progIDs = TimerManager.getInstance().hasBeenMappedBefore(timer);
         if (progIDs != null) { // we have a mapping of this timer to a program
             for (String progID : progIDs) {
                 if (progID.equals("NO_PROGRAM")) {
                     logger.debug("Timer {} should never be assigned", timer.toString());
                     timer.setReason(LazyBonesTimer.NO_PROGRAM);
                     return true;
                 } else {
                     devplugin.Channel c = ChannelManager.getInstance().getChannel(timer);
                     if (c != null) {
                         Date date = new Date(timer.getStartTime());
                         Iterator<Program> iterator = LazyBones.getPluginManager().getChannelDayProgram(date, c);
                         while (iterator != null && iterator.hasNext()) {
                             Program p = iterator.next();
                             if (p.getUniqueID().equals(progID) && p.getDate().equals(date)) {
                                 p.mark(LazyBones.getInstance());
                                 timer.setTvBrowserProgIDs(progIDs);
                                 logger.debug("Mapping found for: {}", timer.toString());
                                 return true;
                             }
                         }
                     }
                 }
             }
         } else {
             logger.debug("No mapping found for: {}", timer.toString());
             if (candidate != null) {
                 logger.debug("Looking up old mappings");
                 TimerManager tm = TimerManager.getInstance();
                 String progTitle = tm.getTitleMapping().getTvbTitle(timer.getTitle());
                 if (candidate.getTitle().equals(progTitle)) {
                     candidate.mark(LazyBones.getInstance()); // wieso mark hier drin? lookup hört sich nicht danach an
                     timer.addTvBrowserProgID(candidate.getUniqueID());
                     logger.debug("Old mapping found for: {}", timer.toString());
                     return true;
                 }
             }
         }
 
         return false;
     }
 }
