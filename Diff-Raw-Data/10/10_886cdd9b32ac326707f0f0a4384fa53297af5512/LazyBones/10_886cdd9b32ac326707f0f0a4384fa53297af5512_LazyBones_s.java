/* $Id: LazyBones.java,v 1.32 2005-12-16 20:16:34 hampelratte Exp $
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
 
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.*;
 
 import javax.swing.*;
 
 import lazybones.gui.TimerList;
 import de.hampelratte.svdrp.Connection;
 import de.hampelratte.svdrp.Response;
 import de.hampelratte.svdrp.VDRVersion;
 import de.hampelratte.svdrp.commands.*;
 import de.hampelratte.svdrp.responses.highlevel.EPGEntry;
 import de.hampelratte.svdrp.responses.highlevel.VDRTimer;
 import de.hampelratte.svdrp.util.EPGParser;
 import de.hampelratte.svdrp.util.TimerParser;
 import devplugin.*;
 import devplugin.Date;
 
 /**
  * A VDRRemoteControl Plugin
  * 
  * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net </a>
  * 
  */
 public class LazyBones extends Plugin {
 
     public static final Logger LOG = Logger.getLogger();
 
     /** Translator */
     private static final util.ui.Localizer mLocalizer = util.ui.Localizer
             .getLocalizerFor(LazyBones.class);
 
     private JDialog remoteControl;
 
     private PreviewPanel pp;
 
     private Properties props;
     
     private TimerList timerList;
 
     public ActionMenu getContextMenuActions(final Program program) {
         AbstractAction action = new AbstractAction() {
             public void actionPerformed(ActionEvent evt) {
 
             }
         };
         action.putValue(Action.NAME, mLocalizer.msg("vdr",
                 "Video Disk Recorder"));
         action.putValue(Action.SMALL_ICON,
                 createImageIcon("lazybones/vdr16.png"));
 
         PluginAccess[] plugins = program.getMarkedByPlugins();
         boolean marked = false;
         for (int i = 0; i < plugins.length; i++) {
             if (plugins[i].getId().equals(this.getId())) {
                 marked = true;
                 break;
             }
         }
 
         Action[] actions = null;
 
         if (marked) {
             actions = new Action[5];
 
             actions[1] = new AbstractAction() {
                 public void actionPerformed(ActionEvent evt) {
                     deleteTimer(program);
                 }
             };
             actions[1].putValue(Action.NAME, mLocalizer.msg("dont_capture",
                     "Delete timer"));
             actions[1].putValue(Action.SMALL_ICON,
                     createImageIcon("lazybones/cancel.png"));
 
             actions[2] = new AbstractAction() {
                 public void actionPerformed(ActionEvent evt) {
                     Timer timer = (Timer) TimerManager.getInstance().getTimer(
                             program.getID());
                     editTimer(timer);
                 }
             };
             actions[2].putValue(Action.NAME, mLocalizer.msg("edit",
                     "Edit Timer"));
             actions[2].putValue(Action.SMALL_ICON,
                     createImageIcon("lazybones/Edit16.png"));
 
             actions[3] = new AbstractAction() {
                 public void actionPerformed(ActionEvent evt) {
                     getTimersFromVDR();
                 }
             };
             actions[3].putValue(Action.NAME, mLocalizer.msg("resync",
                     "Synchronize with VDR"));
             actions[3].putValue(Action.SMALL_ICON,
                     createImageIcon("lazybones/Refresh16.gif"));
             
             actions[4] = new AbstractAction() {
                 public void actionPerformed(ActionEvent evt) {
                     getTimerList().setVisible(true);
                 }
             };
             actions[4].putValue(Action.NAME, mLocalizer.msg("showAllTimers",
                     "Show all timers"));
             actions[4].putValue(Action.SMALL_ICON,
                     createImageIcon("lazybones/view_detailed.png"));
 
         } else {
             actions = new Action[4];
 
             actions[1] = new AbstractAction() {
                 public void actionPerformed(ActionEvent evt) {
                     createTimer(program);
                 }
             };
             actions[1].putValue(Action.NAME, mLocalizer.msg("capture",
                     "Capture with VDR"));
             actions[1].putValue(Action.SMALL_ICON,
                     createImageIcon("lazybones/capture.png"));
 
             actions[2] = new AbstractAction() {
                 public void actionPerformed(ActionEvent evt) {
                     getTimersFromVDR();
                 }
             };
             actions[2].putValue(Action.NAME, mLocalizer.msg("resync",
                     "Synchronize with VDR"));
             actions[2].putValue(Action.SMALL_ICON,
                     createImageIcon("lazybones/Refresh16.gif"));
             
             actions[3] = new AbstractAction() {
                 public void actionPerformed(ActionEvent evt) {
                     getTimerList().setVisible(true);
                 }
             };
             actions[3].putValue(Action.NAME, mLocalizer.msg("showAllTimers",
                     "Show all timers"));
             actions[3].putValue(Action.SMALL_ICON,
                     createImageIcon("lazybones/view_detailed.png"));
         }
 
         actions[0] = new AbstractAction() {
             public void actionPerformed(ActionEvent evt) {
                 watch(program);
             }
         };
         actions[0].putValue(Action.NAME, mLocalizer.msg("watch",
                 "Watch this channel"));
         actions[0].putValue(Action.SMALL_ICON,
                 createImageIcon("lazybones/vdr16.png"));
 
         return new ActionMenu(action, actions);
     }
 
     public ActionMenu getButtonAction() {
         ButtonAction action = new ButtonAction();
         action.setActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (remoteControl == null) {
                     initRemoteControl();
                 }
                 pp.startGrabbing();
                 remoteControl.setVisible(true);
             }
         });
 
         action.setBigIcon(createImageIcon("lazybones/vdr24.png"));
         action.setSmallIcon(createImageIcon("lazybones/vdr16.png"));
         action.setShortDescription(mLocalizer.msg("vdr", "Video Disk Recorder"));
         action.setText(mLocalizer.msg("vdr", "Video Disk Recorder"));
 
         return new ActionMenu(action);
     }
 
     private void watch(Program program) {
         Player.play(program, this);
     }
 
     
     public void deleteTimer(Timer timer) {
         Response res = VDRConnection.send(new DELT(Integer.toString(timer.getID())));
         if (res == null) {
             return;
         } else if (res.getCode() == 250) {
             String progID = timer.getTvBrowserProgID();
             if(progID != null) {
                 Program prog = ProgramManager.getInstance().getProgram(timer);
                 prog.unmark(this);
             }
             getTimersFromVDR();
             updateTree();
         } else {
             LOG.log(mLocalizer.msg(
                     "couldnt_delete", "Couldn\'t delete timer:")
                     + " " + res.getMessage(), Logger.OTHER, Logger.ERROR);
         }
     }
     
     public void deleteTimer(Program prog) {
         Timer timer = TimerManager.getInstance().getTimer(prog.getID());
         Response res = VDRConnection.send(new DELT(Integer.toString(timer.getID())));
         if (res == null) {
             return;
         } else if (res.getCode() == 250) {
             prog.unmark(this);
             getTimersFromVDR();
             updateTree();
         } else {
             LOG.log(mLocalizer.msg(
                     "couldnt_delete", "Couldn\'t delete timer:")
                     + " " + res.getMessage(), Logger.OTHER, Logger.ERROR);
         }
     }
     
     public void createTimer() {
         Timer timer = new Timer();
         timer.setChannel(1);
         Program prog = ProgramManager.getInstance().getProgram(timer);
         
         boolean showTimerOptions = Boolean.TRUE.toString().equals(props.getProperty("showTimerOptionsDialog"));
         props.setProperty("showTimerOptionsDialog",Boolean.TRUE.toString());
         createTimer(prog);
         props.setProperty("showTimerOptionsDialog",Boolean.toString(showTimerOptions));
     }
     
     private void createTimer(Program prog) {
         if (prog.isExpired()) {
             LOG.log(mLocalizer.msg(
                     "expired", "This program has expired"), Logger.OTHER, Logger.ERROR);
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
 
         Object o = ProgramManager.getChannelMapping().get(prog.getChannel().getId());
         if (o == null) {
             LOG.log(mLocalizer.msg("no_channel_defined",
                     "No channel defined", prog.toString()), Logger.OTHER, Logger.ERROR);
             return;
         }
         int id = ((VDRChannel) o).getId();
         Response res = VDRConnection.send(new LSTE(Integer.toString(id), "at "
                 + Long.toString(millis / 1000)));
 
         if (res != null && res.getCode() == 215) {
             List epgList = EPGParser.parse(res.getMessage());
 
             if (epgList.size() <= 0) {
                 noEPGAvailable(prog, id);
                 return;
             }
 
             /*
              * VDR 1.3 already returns the matching entry, for 1.2 we need to
              * search for a match
              */
             VDRVersion version = Connection.getVersion();
             boolean isOlderThan1_3 = version.getMajor() < 1
                     || (version.getMajor() == 1 && version.getMinor() < 3);
             EPGEntry vdrEPG = isOlderThan1_3 ? filterEPGDate(epgList,
                     ((VDRChannel) o).getName(), millis) : (EPGEntry) epgList
                     .get(0);
 
             Timer timer = new Timer();
             timer.setChannel(id);
             timer.setTvBrowserProgID(prog.getID());
             timer.setPriority(50);
             timer.setLifetime(50);
 
             int buffer_before = Integer.parseInt(props
                     .getProperty("timer.before"));
             int buffer_after = Integer.parseInt(props
                     .getProperty("timer.after"));
 
             if (vdrEPG != null) {
                 Calendar calStart = vdrEPG.getStartTime();
                 // start the recording x min before the beggining of the program
                 calStart.add(Calendar.MINUTE, -buffer_before);
                 timer.setStartTime(calStart);
 
                 Calendar calEnd = vdrEPG.getEndTime();
                 // stop the recording x min after the end of the program
                 calEnd.add(Calendar.MINUTE, buffer_after);
                 timer.setEndTime(calEnd);
 
                 timer.setFile(vdrEPG.getTitle());
                 timer.setDescription(vdrEPG.getDescription());
             } else { // VDR has no EPG data
                 noEPGAvailable(prog, id);
                 return;
             }
 
             boolean showOptionsDialog = Boolean.TRUE.toString().equals(props.getProperty("showTimerOptionsDialog"));
             boolean create = true;
             if(showOptionsDialog) {
                 create = showTimerOptionsDialog(timer, false);
             }
             
             if (create) {
                 if (timer.getTitle() != null) {
                     int percentage = Utilities.percentageOfEquality(prog
                             .getTitle(), timer.getTitle());
                     if (timer.getFile().indexOf("EPISODE") >= 0
                             || timer.getFile().indexOf("TITLE") >= 0
                             || timer.isRepeating()) {
                         percentage = 100;
                     }
                     int threshold = Integer.parseInt(props
                             .getProperty("percentageThreshold"));
                     if (percentage > threshold) {
                         Response response = VDRConnection.send(new NEWT(timer
                                 .toNEWT()));
                         if (response.getCode() == 250) {
                             // since we dont have the ID of the new timer, we
                             // have to
                             // get the whole timer list again :-(
                             getTimersFromVDR();
                         } else {
                             LOG.log(
                                 mLocalizer.msg("couldnt_create",
                                    "Couldn\'t create timer:")
                                     + " " + response.getMessage(),Logger.OTHER, Logger.ERROR);
                         }
                     } else {
                         Program selectedProgram = showTimerConfirmDialog(timer,
                                 prog);
                         if (selectedProgram != null) {
                             Timer t = ((TimerProgram) selectedProgram)
                                     .getTimer();
                             System.out.println(t);
                             // start the recording x min before the beggining of
                             // the program
                             t.getStartTime().add(Calendar.MINUTE,
                                     -buffer_before);
                             // stop the recording x min after the end of the
                             // program
                             t.getEndTime().add(Calendar.MINUTE, buffer_after);
                             Response response = VDRConnection.send(new NEWT(t
                                     .toNEWT()));
                             if (response.getCode() == 250) {
                                 timerCreatedOK(prog, t);
                             } else {
                                 LOG.log(
                                         mLocalizer.msg("couldnt_create",
                                            "Couldn\'t create timer:")
                                             + " " + response.getMessage(),Logger.OTHER, Logger.ERROR);
                             }
                         }
                     }
                 } else { // VDR has no EPG data
                     noEPGAvailable(prog, id);
                 }
             }
         } else if(res != null && res.getCode() == 550 & "No schedule found\n".equals(res.getMessage())) {
             noEPGAvailable(prog, id);
         } else {
             String msg = res != null ? res.getMessage() : "Reason unknown";
             LOG.log(mLocalizer.msg("couldnt_create",
                     "Couldn\'t create timer\n: ") + " " + msg,Logger.OTHER, Logger.ERROR);
         }
     }
     
     private boolean showTimerOptionsDialog(Timer timer, boolean updateDialog) {
         TimerOptionsDialog tod = new TimerOptionsDialog(this, timer,
                 updateDialog);
         tod.start();
         try {
             tod.join();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         return tod.getConfirmation();
     }
 
     private void noEPGAvailable(Program prog, int channelID) {
         int buffer_before = Integer.parseInt(props.getProperty("timer.before"));
         int buffer_after = Integer.parseInt(props.getProperty("timer.after"));
 
         boolean dontCare = Boolean.FALSE.toString().equals(props.getProperty("logEPGErrors"));
         int result = JOptionPane.NO_OPTION;
         if(!dontCare) {
             result = JOptionPane.showConfirmDialog(null, mLocalizer.msg(
                 "noEPGdata", ""), "", JOptionPane.YES_NO_OPTION);
         }
         if (dontCare || result == JOptionPane.OK_OPTION) {
             Timer newTimer = new Timer();
             newTimer.setActive(true);
             newTimer.setChannel(channelID);
             newTimer.setLifetime(50);
             newTimer.setPriority(50);
             newTimer.setTitle(prog.getTitle());
             newTimer.setTvBrowserProgID(prog.getID());
 
             Date d = prog.getDate();
             Calendar startTime = d.getCalendar();
 
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
 
             boolean create = showTimerOptionsDialog(newTimer, false);
             if (create) {
                 Response res = VDRConnection.send(new NEWT(newTimer.toNEWT()));
                 if (res.getCode() == 250) {
                     timerCreatedOK(prog, newTimer);
                 }
             }
         }
     }
 
     protected void timerCreatedOK(Program prog, Timer timer) {
         timer.setTvBrowserProgID(prog.getID());
         TimerManager.getInstance().replaceStoredTimer(timer);
         
         // since we dont have the ID of the new timer, we have
         // to get the whole timer list again :-(
         getTimersFromVDR();
     }
 
     /**
      * If a Program can't be assigned to a VDR-Program, this method shows a
      * dialog to select the right VDR-Program
      * 
      * @param prog
      *            the Program selected in TV-Browser
      * @param timerOptions
      *            the timer from TimerOptionsDialog
      * @return the selected VDR-Program
      */
     private Program showTimerConfirmDialog(Timer timerOptions, Program prog) {
         // get all programs 2 hours before and after the given program
         Calendar cal = GregorianCalendar.getInstance();
         Date date = prog.getDate();
         cal.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
         cal.set(Calendar.MONTH, date.getMonth() - 1);
         cal.set(Calendar.YEAR, date.getYear());
         cal.set(Calendar.HOUR_OF_DAY, prog.getHours());
         cal.set(Calendar.MINUTE, prog.getMinutes());
         cal.add(Calendar.MINUTE, prog.getLength() / 2);
 
         Channel chan = prog.getChannel();
 
         TreeSet programSet = new TreeSet();
         for (int i = 10; i <= 120; i += 10) {
             // get the program before the given one
             Calendar c = GregorianCalendar.getInstance();
             c.setTimeInMillis(cal.getTimeInMillis());
             c.add(Calendar.MINUTE, i * -1);
             Timer t1 = getVDRProgramAt(c, chan);
             if (t1 != null) {
                 programSet.add(t1);
             }
 
             // get the program after the given one
             c = GregorianCalendar.getInstance();
             c.setTimeInMillis(cal.getTimeInMillis());
             c.add(Calendar.MINUTE, i);
             Timer t2 = getVDRProgramAt(c, chan);
             if (t2 != null) {
                 programSet.add(t2);
             }
         }
 
         Program[] programs = new Program[programSet.size()];
         int i = 0;
         for (Iterator iter = programSet.iterator(); iter.hasNext();) {
             Timer timer = (Timer) iter.next();
             Calendar time = timer.getStartTime();
             TimerProgram p = new TimerProgram(chan, new Date(time), time
                     .get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));
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
         TimerSelectionDialog dialog = new TimerSelectionDialog(this);
         dialog.showSelectionDialog(programs);
         Program tmp = dialog.getProgram();
         if (tmp != null) {
             TimerProgram program = (TimerProgram) tmp;
             VDRTimer t = program.getTimer();
             t.setTitle(timerOptions.getTitle());
             t.setDescription(timerOptions.getDescription());
             t.setLifetime(timerOptions.getLifetime());
             t.setPriority(timerOptions.getPriority());
             t.setStartTime(timerOptions.getStartTime());
             t.setEndTime(timerOptions.getEndTime());
             t.setHasFirstTime(timerOptions.hasFirstTime());
             t.setFirstTime(timerOptions.getFirstTime());
             t.setRepeatingDays(timerOptions.getRepeatingDays());
             return program;
         }
         return null;
     }
 
     /**
      * If a timer can't be assigned to a Program, this method shows a dialog to
      * select the right Program
      * 
      * @param timer
      *            the timer received from the VDR
      */
     private void showProgramConfirmDialog(Timer timer) {
         Calendar cal = timer.getStartTime();
 
         Calendar start = GregorianCalendar.getInstance();
         start.setTimeInMillis(cal.getTimeInMillis());
 
         Enumeration en = ProgramManager.getChannelMapping().keys();
         Channel chan = null;
         while (en.hasMoreElements()) {
             String channelID = (String) en.nextElement();
             VDRChannel channel = (VDRChannel) ProgramManager.getChannelMapping().get(channelID);
             if (channel.getId() == timer.getChannel()) {
                 chan = ProgramManager.getInstance().getChannelById(channelID);
             }
         }
 
         // if we cant find the channel, stop
         if (chan == null)
             return;
 
         // get all programs 3 hours before and after the given program
         HashSet programSet = new HashSet();
         for (int i = 0; i <= 180; i++) { 
             // get the program before the given one
             Calendar c = GregorianCalendar.getInstance();
             c.setTimeInMillis(start.getTimeInMillis());
             c.add(Calendar.MINUTE, i * -1);
             Program p1 = ProgramManager.getInstance().getProgramAt(c, c, chan);
             if (p1 != null) {
                 programSet.add(p1);
             }
 
             // get the program after the given one
             c = GregorianCalendar.getInstance();
             c.setTimeInMillis(start.getTimeInMillis());
             c.add(Calendar.MINUTE, i);
             Program p2 = ProgramManager.getInstance().getProgramAt(c, c, chan);
             if (p2 != null) {
                 programSet.add(p2);
             }
         }
 
         Program[] programs = new Program[programSet.size()];
         int i = 0;
         for (Iterator iter = programSet.iterator(); iter.hasNext();) {
             Program p = (Program) iter.next();
             programs[i++] = p;
         }
         Arrays.sort(programs, new ProgramComparator());
 
         // show dialog
         ProgramSelectionDialog dialog = new ProgramSelectionDialog(this);
         dialog.showSelectionDialog(programs, timer);
     }
 
     protected Timer getVDRProgramAt(Calendar cal, Channel chan) {
         long millis = cal.getTimeInMillis() / 1000;
         Object o = ProgramManager.getChannelMapping().get(chan.getId());
         int id = ((VDRChannel) o).getId();
 
         LSTE cmd = new LSTE(Integer.toString(id), "at " + Long.toString(millis));
         Response res = VDRConnection.send(cmd);
         if (res != null && res.getCode() == 215) {
             List epg = EPGParser.parse(res.getMessage());
             if (epg.size() > 0) {
                 EPGEntry entry = (EPGEntry) epg.get(0);
                 VDRTimer timer = new VDRTimer();
                 timer.setChannel(id);
                 timer.setTitle(entry.getTitle());
                 timer.setStartTime(entry.getStartTime());
                 timer.setEndTime(entry.getEndTime());
                 timer.setDescription(entry.getDescription());
                 timer.setLifetime(50);
                 timer.setPriority(50);
                 return new Timer(timer);
             }
         }
         return null;
     }
 
     public String getMarkIconName() {
         return "lazybones/vdr16.png";
     }
 
     public PluginInfo getInfo() {
         String name = mLocalizer.msg("vdr", "Video Disk Recorder");
         String description = mLocalizer
                 .msg("desc",
                         "This plugin is a remote control for a VDR (by Klaus Schmidinger).");
         String author = "Henrik Niehaus, hampelratte@users.sf.net";
         return new PluginInfo(name, description, author, new Version(0, 1));
     }
 
     /**
      * Called by loadSettings to initialize the GUI and other things
      */
     private void initRemoteControl() {
         remoteControl = new JDialog(getParent(), mLocalizer.msg(
                 "remoteControl", "VDR RemoteControl"), true);
         remoteControl.setSize(800, 450);
         remoteControl.getContentPane().setLayout(new GridBagLayout());
         remoteControl.getContentPane().add(
                 new RemoteControl(this),
                 new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                         GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                         new Insets(5, 5, 5, 5), 0, 0));
         pp = new PreviewPanel(this);
         remoteControl.getContentPane().add(
                 pp,
                 new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
                         GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                         new Insets(5, 5, 5, 5), 0, 0));
         remoteControl.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
         remoteControl.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent event) {
                 pp.stopGrabbing();
                 remoteControl.setVisible(false);
             }
         });
     }
 
     public devplugin.SettingsTab getSettingsTab() {
         return new VDRSettingsPanel(this);
     }
 
     public void readData(ObjectInputStream in) {
         try {
             // load channel mapping
             ProgramManager.setChannelMapping((Hashtable) in.readObject());
 
             // load stored timers. 
             // if no connection is available, we use these ones
             TimerManager.getInstance().setStoredTimers((ArrayList) in.readObject());
         } catch (Exception e) {
             LOG.log("Couldn't read data", Logger.OTHER, Logger.ERROR);
             e.printStackTrace(System.out);
         }
 
         Calendar today = GregorianCalendar.getInstance();
 
         for (ListIterator iter = TimerManager.getInstance().getStoredTimers()
                 .listIterator(); iter.hasNext();) {
             Timer timer = (Timer) iter.next();
             if (timer.getEndTime().before(today) & !timer.isRepeating()) {
                 iter.remove();
             }
         }
     }
 
     protected void getTimersFromVDR() {
         TimerManager tm = TimerManager.getInstance();
         ArrayList timers = tm.getTimers();
         for (Iterator it = timers.iterator(); it.hasNext();) {
             Timer timer = (Timer) it.next();
             String progID = timer.getTvBrowserProgID();
             if(progID != null) { // timer could be assigned
                 Date date = new Date(timer.getStartTime());
                 Program prog = LazyBones.getPluginManager()
                     .getProgram(date, progID);
                 if(prog != null) {
                     prog.unmark(this);
                 }
             }
         }
 
         tm.removeAll();
         Response res = VDRConnection.send(new LSTT());
         if (res != null && res.getCode() == 250) {
             LOG.log("Timers retrieved from VDR",Logger.OTHER, Logger.INFO);
             String timersString = res.getMessage();
             ArrayList vdrtimers = TimerParser.parse(timersString);
             tm.setTimers(vdrtimers, true);
         } else if (res != null && res.getCode() == 550) {
             // no timers are defined, do nothing
             LOG.log("No timer defined on VDR",Logger.OTHER, Logger.INFO);
         } else { /* something went wrong, we have no timers -> 
                   * load the stored ones */
             LOG.log(mLocalizer.msg("using_stored_timers",
                 "Couldn't retrieve timers from VDR, using stored ones."), 
                 Logger.CONNECTION, Logger.ERROR);
             
             ArrayList vdrtimers = tm.getStoredTimers();
             tm.setTimers(vdrtimers, false);
         }
 
         // mark all "timed" programs (haltOnNoChannel = false, because we can
         // have multiple channels)
         markPrograms(TimerManager.getInstance().getTimers(), false);
 
         // update the plugin tree
         updateTree();
     }
     
     private TimerList getTimerList() {
         if(timerList == null) {
             timerList = new TimerList(this);
         }
         return timerList;
     }
 
     /**
      * Called to mark all Programs
      * 
      * @param affectedTimers
      *            An ArrayList with all timers
      * @param haltOnNoChannel
      *            Determines, if the method should terminate, if the channel
      *            couldn't be found for a timer. This is useful, if all timers
      *            have the same channel (see markPrograms(ChannelDayProgram
      *            prog))
      * @see LazyBones#markPrograms(ChannelDayProgram)
      */
     private void markPrograms(ArrayList affectedTimers, boolean haltOnNoChannel) {
         Iterator iter = affectedTimers.iterator();
 
         // for every timer
         while (iter.hasNext()) {
             Timer timer = (Timer) iter.next();
             Channel chan = ProgramManager.getInstance().getChannel(timer);
             if (chan == null) {
                 timer.setReason(Timer.NO_CHANNEL);
 
                 // leave method only, if the haltOnNoChannel is true
                 // this is the case, if the call comes from markPrograms(ChannelDayProgram)
                 if (haltOnNoChannel) {
                     return;
                 }
 
                 // we couldn't find a channel for this timer, continue with the
                 // next timer
                 continue;
             }
 
             markSingularTimer(timer, chan);
         }
         handleNotAssignedTimers();
     }
     
     /**
      * Handles all timers, which couldn't be assigned automatically
      *
      */
     private void handleNotAssignedTimers() {
         if (Boolean.TRUE.toString().equals(
                 props.getProperty("supressMatchDialog"))) {
             return;
         }
         Iterator iterator = TimerManager.getInstance().getNotAssignedTimers()
                 .iterator();
         LOG.log("Not assigned timers: "
                 + TimerManager.getInstance().getNotAssignedTimers().size(),
                 Logger.OTHER, Logger.DEBUG);
         while (iterator.hasNext()) {
             Timer timer = (Timer) iterator.next();
             switch(timer.getReason()) {
             case Timer.NOT_FOUND:
                 showProgramConfirmDialog(timer);
                 break;
             case Timer.NO_EPG:
                 LOG.log("Couldn't assign timer: " + timer, Logger.EPG, Logger.WARN);
                 String mesg = mLocalizer.msg("noEPGdataTVB","<html>TV-Browser has no EPG-data the timer {0}.<br>Please update your EPG-data!</html>",timer.toString());
                 LOG.log(mesg, Logger.EPG, Logger.ERROR);
                 break;
             case Timer.NO_CHANNEL:
                 LOG.log(mLocalizer.msg("no_channel_defined",
                         "No channel defined", timer.toString()), Logger.EPG, Logger.ERROR);
                 break;
             case Timer.NO_PROGRAM:
                 // do nothing
                 break;
             default:
                 LOG.log("Not assigned timer: " + timer.toString(), Logger.OTHER,
                         Logger.DEBUG);
             }
         }
     }
 
     /**
      * 
      * @param timer
      * @param chan
      */
     private void markSingularTimer(Timer timer, Channel chan) {
         // TODO eventuell erkennen: ein timer kann auch mehrere sendungen
         // aufnehmen (doppelpacks z.b.)
 
         // create a clone of the timer and subtract the recording buffers
         Timer bufferLessTimer = (Timer) timer.clone();
         removeTimerBuffers(bufferLessTimer);
 
         int day = bufferLessTimer.getStartTime().get(Calendar.DAY_OF_MONTH);
         int month = bufferLessTimer.getStartTime().get(Calendar.MONTH) + 1;
         int year = bufferLessTimer.getStartTime().get(Calendar.YEAR);
 
         Date date = new Date(year, month, day);
 
         Iterator it = getPluginManager().getChannelDayProgram(date, chan);
         if (it != null) {
             TreeMap candidates = new TreeMap();
             while (it.hasNext()) { // iterate over all programs of one day and
                                     // compare start and end time
                 Program prog = (Program) it.next();
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
                 // System.out.println(timer.getTitle() + " " + deltaStart + ":"
                 // + deltaEnd);
 
                 // collect candidates
                 if (Math.abs(deltaStart) <= 45 && Math.abs(deltaEnd) <= 45) {
                     candidates.put(new Integer(Math.abs(deltaStart)), prog);
                 }
             }
 
             if (candidates.size() == 0) {
                 boolean found = lookUpTimer(timer);
                 if (found) {
                     return;
                 } else {
                     timer.setReason(Timer.NOT_FOUND);
                     return;
                 }
             }
 
             // get the best fitting candidate. this is the first key, because
             // TreeMap is sorted
             Program progMin = (Program) candidates.get(candidates.firstKey());
 
             // calculate the precentage of common words
             int percentage = 0;
             if(!timer.getPath().equals("")) {
                 int percentagePath = Utilities.percentageOfEquality(timer.getPath(), progMin
                         .getTitle());
                 int percentageTitle = Utilities.percentageOfEquality(timer.getTitle(), progMin
                         .getTitle());
                 percentage = Math.max(percentagePath, percentageTitle);
             } else {
                 percentage = Utilities.percentageOfEquality(timer.getTitle(), progMin
                     .getTitle());
             }
 
             // override the percentage
             if (timer.getFile().indexOf("EPISODE") >= 0
                     || timer.getFile().indexOf("TITLE") >= 0
                     || timer.isRepeating()) {
                 percentage = 100;
             }
             
             LOG.log("Percentage:"+percentage + " " + timer.toString(), Logger.OTHER, Logger.DEBUG);
 
             int threshold = Integer.parseInt(props
                     .getProperty("percentageThreshold"));
             // if the percentage of common words is
             // higher than the config value percentageThreshold, mark this
             // program
             if (percentage >= threshold) {
                 progMin.mark(this);
                 timer.setTvBrowserProgID(progMin.getID());
                 if (timer.isRepeating()) {
                     Date d = progMin.getDate();
                     timer.getStartTime().set(Calendar.DAY_OF_MONTH, d.getDayOfMonth());
                     timer.getStartTime().set(Calendar.MONTH, d.getMonth()-1);
                     timer.getStartTime().set(Calendar.YEAR, d.getYear());
                 }
             } else {
                 boolean found = lookUpTimer(timer);
                 if (!found) { // we have no mapping
                     LOG.log("Couldn't find a program with that title: "
                             + timer.getTitle(), Logger.OTHER, Logger.WARN);
                     LOG.log("Couldn't assign timer: " + timer, Logger.OTHER, Logger.WARN);
                     timer.setReason(Timer.NOT_FOUND);
                 }
             }
         } else { // no channeldayprogram was found
             if(!timer.isRepeating()) {
                 timer.setReason(Timer.NO_EPG);
             }
         }
     }
 
     protected void removeTimerBuffers(Timer timer) {
         int buffer_before = Integer.parseInt(props.getProperty("timer.before"));
         timer.getStartTime().add(Calendar.MINUTE, buffer_before);
         int buffer_after = Integer.parseInt(props.getProperty("timer.after"));
         timer.getEndTime().add(Calendar.MINUTE, -buffer_after);
     }
 
 
     public void writeData(ObjectOutputStream out) {
         try {
             out.writeObject(ProgramManager.getChannelMapping());
             out.writeObject(TimerManager.getInstance().getTimers());
         } catch (IOException e) {
             e.printStackTrace(System.out);
         }
     }
 
     public void onDeactivation() {
         try {
             Player.stop();
         } catch (Exception e) {
         }
     }
 
     public void loadSettings(Properties props) {
         this.props = props;
 
         String host = props.getProperty("host");
         host = host == null ? "localhost" : host;
         props.setProperty("host", host);
         String streamtype = props.getProperty("streamtype");
         streamtype = streamtype == null ? "TS" : streamtype;
         props.setProperty("streamtype", streamtype);
         String port = props.getProperty("port");
         port = port == null ? "2001" : port;
         props.setProperty("port", port);
         String timeout = props.getProperty("timeout");
         timeout = timeout == null ? "500" : timeout;
         props.setProperty("timeout", timeout);
         String threshold = props.getProperty("percentageThreshold");
         threshold = threshold == null ? "45" : threshold;
         props.setProperty("percentageThreshold", threshold);
 
         String timer_before = props.getProperty("timer.before");
         timer_before = timer_before == null ? "5" : timer_before;
         String timer_after = props.getProperty("timer.after");
         timer_after = timer_after == null ? "10" : timer_after;
         props.setProperty("timer.before", timer_before);
         props.setProperty("timer.after", timer_after);
 
         String preview_url = props.getProperty("preview.url");
         preview_url = preview_url == null ? "http://htpc:8000/preview.jpg"
                 : preview_url;
         String preview_path = props.getProperty("preview.path");
         preview_path = preview_path == null ? "/pub/web/preview.jpg"
                 : preview_path;
         props.setProperty("preview.url", preview_url);
         props.setProperty("preview.path", preview_path);
 
         String switchBefore = props.getProperty("switchBefore");
         switchBefore = switchBefore == null ? "false" : switchBefore;
         props.setProperty("switchBefore", switchBefore);
         
         String logConnectionErrors = props.getProperty("logConnectionErrors");
         logConnectionErrors = logConnectionErrors == null ? "true" : logConnectionErrors;
         props.setProperty("logConnectionErrors", logConnectionErrors);
         Logger.logConnectionErrors = new Boolean(logConnectionErrors).booleanValue();
         
         String logEPGErrors = props.getProperty("logEPGErrors");
         logEPGErrors = logEPGErrors == null ? "true" : logEPGErrors;
         props.setProperty("logEPGErrors", logEPGErrors);
         Logger.logEPGErrors = new Boolean(logEPGErrors).booleanValue();
         
         String showTimerOptionsDialog = props.getProperty("showTimerOptionsDialog");
         showTimerOptionsDialog = showTimerOptionsDialog == null ? "true" : showTimerOptionsDialog;
         props.setProperty("showTimerOptionsDialog", showTimerOptionsDialog);
 
         VDRConnection.host = host;
         VDRConnection.port = Integer.parseInt(port);
         VDRConnection.timeout = Integer.parseInt(timeout);
 
         getTimersFromVDR();
     }
 
     public Properties storeSettings() {
         return props;
     }
 
     public Properties getProperties() {
         return props;
     }
 
     public Icon getIcon(String path) {
         return createImageIcon(path);
     }
 
     public void handleTvDataAdded(ChannelDayProgram newProg) {
         markPrograms(newProg);
     }
 
     /**
      * Called if handleTVData* has been called.
      * In this case only one channel has to be marked again
      * @param prog the ChannelDayProgram, which has to be marked
      */
     private void markPrograms(ChannelDayProgram prog) {
         ArrayList affectedTimers = new ArrayList();
         Iterator iterator = TimerManager.getInstance().getTimers().iterator();
         while (iterator.hasNext()) {
             Timer timer = (Timer) iterator.next();
             Channel timerChannel = ProgramManager.getInstance().getChannel(timer);
             Channel progChannel = prog.getChannel();
             
             // timer channel couldn't be found
             // no channel available for this timer
             if(timerChannel == null) {
                 return;
             } 
             
             // timer channel and prog channel are equal
             // add this timer to affected timers
             if (timerChannel.equals(progChannel)) {
                 affectedTimers.add(timer);
             }
         }
 
         // markPrograms only for one channel -> haltOnNoChannel = true
         LOG.log("Affected timers " + affectedTimers.size(), Logger.OTHER, Logger.DEBUG);
         markPrograms(affectedTimers, true);
     }
 
     public void handleTvDataDeleted(ChannelDayProgram oldProg) {
         markPrograms(oldProg);
     }
 
     public Frame getParent() {
         return getParentFrame();
     }
 
     /**
      * Updates the pluginview of TV-Browser
      */
     public void updateTree() {
         PluginTreeNode node = getRootNode();
         node.removeAllActions();
         node.removeAllChildren();
         
         Iterator it = TimerManager.getInstance().getTimers().iterator();
         while(it.hasNext()) {
             Timer timer = (Timer)it.next();
             if(timer.isAssigned()) {
                 Timer bufferLess = (Timer)timer.clone();
                 removeTimerBuffers(bufferLess);
                 Program prog = ProgramManager.getInstance().getProgram(bufferLess);
                 if(prog != null) {
                     node.addProgram(prog);
                 }
             }
         }
 
         node.update();
     }
 
     public boolean canUseProgramTree() {
         return true;
     }
 
     public void editTimer(Timer timer) {
         boolean change = showTimerOptionsDialog(timer, true);
 
         if (change) {
             Response response = VDRConnection.send(new UPDT(timer.toNEWT()));
 
             if (response == null) {
                 String mesg = mLocalizer.msg(
                         "couldnt_change", "Couldn\'t change timer:")
                         + "\n"
                         + mLocalizer.msg("couldnt_connect",
                                 "Couldn\'t connect to VDR");
                 LOG.log(mesg, Logger.CONNECTION, Logger.ERROR);                
                 return;
             }
 
             if (response.getCode() == 250) {
                 // since we dont have the ID of the new timer, we have to
                 // get the whole timer list again :-(
                 getTimersFromVDR();
             } else {
                 String mesg =  mLocalizer.msg(
                         "couldnt_change", "Couldn\'t change timer:")
                         + " " + response.getMessage();
                 LOG.log(mesg, Logger.OTHER, Logger.ERROR);
             }
         }
     }
 
     /*
      * public boolean canReceivePrograms() { return true; }
      * 
      * public void receivePrograms(Program[] progs) {
      * if(VDRConnection.isAvailable()) { for (int i = 0; i < progs.length; i++) {
      * createTimer(progs[i]); } } else { JOptionPane.showMessageDialog(null,
      * "Couldn't connect to VDR"); } }
      */
 
     /**
      * Filters a list of EPGEntries by a provided VDR channel name and a time
      * which has to be in between a EPGEntries start and end time.
      * 
      * @param epgList
      *            list of EPGEntries retrieved from VDR
      * @param vdrChannelName
      *            VDR channel name which has to match a EPGEntry channel name
      * @param middleTime
      *            time of a program which has to be between start and end time
      *            of a EPGEntry
      * @return EPGEntry from the list which matches channel and time
      */
     private static EPGEntry filterEPGDate(List epgList, String vdrChannelName,
             long middleTime) {
         for (Iterator iter = epgList.iterator(); iter.hasNext();) {
             EPGEntry element = (EPGEntry) iter.next();
             if (element.getStartTime().getTimeInMillis() <= middleTime
                     && middleTime <= element.getEndTime().getTimeInMillis()
                     && vdrChannelName.equals(element.getChannelName())) {
                 return element;
             }
         }
         return null;
     }
     
     private boolean lookUpTimer(Timer timer) {
         LOG.log("Looking in storedTimers for: " + timer.toString(),Logger.OTHER, Logger.DEBUG);
         String progID = TimerManager.getInstance().hasBeenMappedBefore(timer);
         if (progID != null) { // we have a mapping of this timer to a program
             if(progID.equals("NO_PROGRAM")) {
                 LOG.log("Timer " + timer.toString()+" should never be assigned",Logger.OTHER, Logger.DEBUG);
                 timer.setReason(Timer.NO_PROGRAM);
                 return true;
             } else {
                 Channel c = ProgramManager.getInstance().getChannel(timer);
                 if (c != null) {
                     Date date = new Date(timer.getStartTime());
                     Iterator iterator = getPluginManager()
                             .getChannelDayProgram(date, c);
                     while (iterator.hasNext()) {
                         Program p = (Program) iterator.next();
                         if (p.getID().equals(progID)
                                 && p.getDate().equals(date)) {
                             p.mark(this);
                             timer.setTvBrowserProgID(p.getID());
                             LOG.log("Mapping found for: " + timer.toString(),
                                     Logger.OTHER, Logger.DEBUG);
                             return true;
                         }
                     }
                 }
             }
         } else {
             LOG.log("No mapping found for: " + timer.toString(),Logger.OTHER, Logger.DEBUG);
         }
 
         return false;
     }
 }
