/* $Id: LazyBones.java,v 1.117 2010-08-29 14:03:46 hampelratte Exp $
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
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Properties;
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.Icon;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import lazybones.captureplugin.driver.LazyBonesDevice;
 import lazybones.gui.MainDialog;
 import lazybones.gui.settings.VDRSettingsPanel;
 import lazybones.logging.DebugConsoleHandler;
 import lazybones.logging.PopupHandler;
 import lazybones.logging.SimpleFormatter;
 import lazybones.programmanager.ProgramManager;
 
 import org.hampelratte.svdrp.Response;
 import org.hampelratte.svdrp.commands.NEWT;
 import org.hampelratte.svdrp.responses.highlevel.Channel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.thoughtworks.xstream.XStream;
 
 import devplugin.ActionMenu;
 import devplugin.ButtonAction;
 import devplugin.Marker;
 import devplugin.Plugin;
 import devplugin.PluginInfo;
 import devplugin.PluginTreeNode;
 import devplugin.Program;
 import devplugin.ProgramReceiveTarget;
 import devplugin.Version;
 
 /**
  * A remote control plugin for VDR
  * 
  * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net </a>
  * 
  */
 public class LazyBones extends Plugin implements Observer {
 
     private static transient Logger logger = LoggerFactory.getLogger(LazyBones.class);
     
     /** Translator */
     private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(LazyBones.class);
 
     private MainDialog mainDialog;
 
     private static Properties props;
     
     private ContextMenuFactory cmf = new ContextMenuFactory();
     
     private static LazyBones instance;
     
     public static final String TIMER_MENU_KEY = "TIMER_MENU_KEY";
     
     public static LazyBones getInstance() {
         return instance;
     }
     
     public ActionMenu getContextMenuActions(final Program program) {
         return cmf.createActionMenu(program);
     }
 
     private ButtonAction buttonAction;
     public ActionMenu getButtonAction() {
         buttonAction = new ButtonAction();
         buttonAction.setActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 getMainDialog().setVisible(true);
             }
         });
 
         buttonAction.setBigIcon(createImageIcon("lazybones/vdr24.png"));
         buttonAction.setSmallIcon(createImageIcon("lazybones/vdr16.png"));
         buttonAction.setShortDescription(LazyBones.getTranslation("lazybones", "Lazy Bones"));
         buttonAction.setText(LazyBones.getTranslation("lazybones", "Lazy Bones"));
 
         return new ActionMenu(buttonAction);
     }
 
     /**
      * Called by TimerSelectionDialog, if a VDR-Program has been selected
      * @param selectedProgram
      */
     public void timerSelectionCallBack(Program selectedProgram, Program originalProgram) {
         int buffer_before = Integer.parseInt(props.getProperty("timer.before"));
         int buffer_after = Integer.parseInt(props.getProperty("timer.after"));
         
         Timer t = ((TimerProgram) selectedProgram).getTimer();
         // start the recording x min before the beggining of the program
         t.getStartTime().add(Calendar.MINUTE, -buffer_before);
         // stop the recording x min after the end of the program
         t.getEndTime().add(Calendar.MINUTE, buffer_after);
         Response response = VDRConnection.send(new NEWT(t));
         if (response.getCode() == 250) {
             TimerManager.getInstance().assignProgramToTimer(selectedProgram, t);
         } else {
             logger.error(LazyBones.getTranslation("couldnt_create", "Couldn\'t create timer:")
                     + " " + response.getMessage());
         }
         
         logger.debug("Storing {}-{} in tvb2vdr", originalProgram.getTitle(), t.getTitle());
         TimerManager.getInstance().getTitleMapping().put(originalProgram.getTitle(), t.getTitle());
     }
 
     public String getMarkIconName() {
         return "lazybones/vdr16.png";
     }
 
     public PluginInfo getInfo() {
         String name = LazyBones.getTranslation("lazybones", "Lazy Bones");
         String description = LazyBones.getTranslation("desc",
                         "This plugin is a remote control for a VDR (by Klaus Schmidinger).");
         String author = "Henrik Niehaus, henrik.niehaus@gmx.de";
         return new PluginInfo(getClass(), name, description, author, "BSD", "http://hampelratte.org/blog/?page_id=6");
     }
     
     public static Version getVersion () {
         //return new Version(0,0,false,"cvs-2010-02-05");
         return new Version(0, 57, 0, true);
     }
 
     public MainDialog getMainDialog() {
         if(mainDialog == null) {
             mainDialog = new MainDialog(getParent(), LazyBones.getTranslation(
                 "lazybones", "Lazy Bones"), false);
         }
         return mainDialog;
     }
 
     
     public devplugin.SettingsTab getSettingsTab() {
         return new VDRSettingsPanel();
     }
     
     public void onDeactivation() {
         try {
             Player.stop();
         } catch (Exception e) {
         }
     }
 
     public void loadSettings(Properties props) {
         LazyBones.props = props;
         
         // load data
         loadData();
         
         String host = props.getProperty("host");
         host = host == null ? "localhost" : host;
         props.setProperty("host", host);
         String charset = props.getProperty("charset");
         charset = charset == null ? "ISO-8859-1" : charset;
         props.setProperty("charset", charset);
         String streamurl = props.getProperty("streamurl");
         streamurl = streamurl == null ? "http://<host>:3000/<streamtype>/<channel>" : streamurl;
         props.setProperty("streamurl", streamurl);
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
         String timer_prio = props.getProperty("timer.prio");
         timer_prio = timer_prio == null ? "50" : timer_prio;
         String timer_lifetime = props.getProperty("timer.lifetime");
         timer_lifetime = timer_lifetime == null ? "50" : timer_lifetime;
         props.setProperty("timer.before", timer_before);
         props.setProperty("timer.after", timer_after);
         props.setProperty("timer.prio", timer_prio);
         props.setProperty("timer.lifetime", timer_lifetime);
         
         String vpsDefault = props.getProperty("vps.default");
         vpsDefault = vpsDefault == null ? "false" : vpsDefault;
         props.setProperty("vps.default", vpsDefault);
         
         String numberOfCards = props.getProperty("numberOfCards");
         numberOfCards = numberOfCards == null ? "1" : numberOfCards;
         props.setProperty("numberOfCards", numberOfCards);
 
         String preview_url = props.getProperty("preview.url");
         preview_url = preview_url == null ? "http://localhost:8000/preview.jpg" : preview_url;
         String preview_path = props.getProperty("preview.path");
         preview_path = preview_path == null ? "/pub/web/preview.jpg" : preview_path;
         String preview_method = props.getProperty("preview.method");
         preview_method = preview_method == null ? "HTTP" : preview_method;
         props.setProperty("preview.url", preview_url);
         props.setProperty("preview.path", preview_path);
         props.setProperty("preview.method", preview_method);
 
         String switchBefore = props.getProperty("switchBefore");
         switchBefore = switchBefore == null ? "false" : switchBefore;
         props.setProperty("switchBefore", switchBefore);
         String recordingURL = props.getProperty("recording.url");
         recordingURL = recordingURL == null ? "http://<host>:3001/rec/<recording_number>" : recordingURL;
         props.setProperty("recording.url", recordingURL);
         
         String logConnectionErrors = props.getProperty("logConnectionErrors");
         logConnectionErrors = logConnectionErrors == null ? "true" : logConnectionErrors;
         props.setProperty("logConnectionErrors", logConnectionErrors);
         
         String logEPGErrors = props.getProperty("logEPGErrors");
         logEPGErrors = logEPGErrors == null ? "true" : logEPGErrors;
         props.setProperty("logEPGErrors", logEPGErrors);
         
         String showTimerOptionsDialog = props.getProperty("showTimerOptionsDialog");
         showTimerOptionsDialog = showTimerOptionsDialog == null ? "true" : showTimerOptionsDialog;
         props.setProperty("showTimerOptionsDialog", showTimerOptionsDialog);
         
         String descSourceTvb = props.getProperty("descSourceTvb");
         descSourceTvb = descSourceTvb == null ? "0" : descSourceTvb;
         props.setProperty("descSourceTvb", descSourceTvb);
         
         String minChannelNumber = props.getProperty("minChannelNumber");
         minChannelNumber = minChannelNumber == null ? "0" : minChannelNumber;
         props.setProperty("minChannelNumber", minChannelNumber);
         String maxChannelNumber = props.getProperty("maxChannelNumber");
         maxChannelNumber = maxChannelNumber == null ? "0" : maxChannelNumber;
         props.setProperty("maxChannelNumber", maxChannelNumber);
         
         VDRConnection.host = host;
         VDRConnection.port = Integer.parseInt(port);
         VDRConnection.timeout = Integer.parseInt(timeout);
         VDRConnection.charset = charset;
         VDRConnection.persistentConnection = false;
         
         init();
     }
     
     @SuppressWarnings("unchecked")
     private void loadData() {
         XStream xstream = new XStream();
         
         // load title mapping
         try {
             Map<String, String> titleMapping = (HashMap<String, String>) xstream.fromXML(props.getProperty("titleMapping"));
             TimerManager.getInstance().setTitleMappingValues(titleMapping);
         } catch (Exception e) {
             logger.warn("Couldn't load title mapping", e);
         }
         
         // load channel mapping
         try {
             Map<String, Channel> channelMapping = (Hashtable<String, Channel>) xstream.fromXML(props.getProperty("channelMapping"));
             ChannelManager.setChannelMapping(channelMapping);
         } catch (Exception e) {
             logger.warn("Couldn't load channel mapping", e);
         }
         
         // load timers
         try {
             List<Timer> timers = (ArrayList<Timer>) xstream.fromXML(props.getProperty("timers"));
             TimerManager.getInstance().setStoredTimers(timers);
         } catch (Exception e) {
             logger.warn("Couldn't load timers", e);
         }
         
         // load channel list
         try {
             List<Channel> channelList = (List<Channel>) xstream.fromXML(props.getProperty("channelList")); 
             ChannelManager.getInstance().setChannels(channelList);
         } catch (Exception e) {
             logger.warn("Couldn't load channel list", e);
         }
         
         // remove outdated timers
         Calendar today = GregorianCalendar.getInstance();
         for (Iterator<Timer> iter = TimerManager.getInstance().getStoredTimers().iterator(); iter.hasNext();) {
             Timer timer = iter.next();
             if (timer.getEndTime().before(today) & !timer.isRepeating()) {
                 iter.remove();
             }
         }
     }
 
     public void handleTvBrowserStartFinished() {
         // upload channel list from vdr
         logger.debug("Updating channel list");
         ChannelManager.getInstance().update();
         
         // synchronize timers and recordings
         synchronize();
     }
     
     private void init() {
         instance = this;
         
         // observe the timer list
         TimerManager.getInstance().addObserver(this);
         
         // initialize logging
         initLogging();
     }
     
     private void initLogging() {
         java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
         rootLogger.setLevel(Level.ALL);
 //        Handler[] handlers = logger.getHandlers();
 //        for (int i = 0; i < handlers.length; i++) {
 //            handlers[i].setLevel(Level.ALL);
 //        }
 
         // create our special logging components
         SimpleFormatter formatter = new SimpleFormatter();
         Handler eph = new PopupHandler();
         eph.setLevel(Level.FINEST);
         Handler dch = new DebugConsoleHandler();
         dch.setLevel(Level.FINE);
         
         // add our special handlers to all lazybones.* messages
         java.util.logging.Logger lazyLogger = java.util.logging.Logger.getLogger("lazybones");
         lazyLogger.addHandler(eph);
         lazyLogger.addHandler(dch);
         eph.setFormatter(formatter);
         
         // add our special handlers to all svdrp messages
         java.util.logging.Logger svdrpLogger = java.util.logging.Logger.getLogger("org.hampelratte.svdrp");
         svdrpLogger.addHandler(dch);
         
         // add our special handlers to the popuplogger
         java.util.logging.Logger popupLogger = java.util.logging.Logger.getLogger(PopupHandler.KEYWORD);
         popupLogger.setLevel(Level.ALL);
         popupLogger.addHandler(eph);
         popupLogger.addHandler(dch);
 
         // create a custom file handler only for lazy bones
         Handler fh = null;
         String settingsDir = getPluginManager().getTvBrowserSettings().getTvBrowserUserHome();
         try {
              fh = new FileHandler(settingsDir + File.separator + "lazybones.log");
              fh.setFormatter(formatter);
         } catch (Exception e) {
             logger.warn("Couldn't add file handler for Lazy Bones", e);
         } 
         
         if(fh != null) {
             lazyLogger.addHandler(fh);
             popupLogger.addHandler(fh);
         }
     }
 
     public Properties storeSettings() {
         storeData();        
         return props;
     }
     
     private void storeData() {
         XStream xstream = new XStream();
         props.setProperty( "channelMapping", xstream.toXML(ChannelManager.getChannelMapping()) );
         props.setProperty( "timers", xstream.toXML(TimerManager.getInstance().getTimers()) );
         props.setProperty( "titleMapping", xstream.toXML(TimerManager.getInstance().getTitleMappingValues()) );
         props.setProperty( "channelList", xstream.toXML(ChannelManager.getInstance().getChannels()) );
     }
 
     public static Properties getProperties() {
         return props;
     }
 
     public Icon getIcon(String path) {
         return createImageIcon(path);
     }
 
     public void handleTvDataUpdateFinished() {
         ProgramManager.getInstance().markPrograms();
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
         
         for(Timer timer : TimerManager.getInstance().getTimers()) {
             if(timer.isAssigned()) {
                 for (String progID : timer.getTvBrowserProgIDs()) {
                     Program prog = ProgramManager.getInstance().getProgram(timer.getStartTime(), progID);
                     if(prog != null) {
                         node.addProgram(prog);
                     } else { // can be null, if program time is near 00:00, because then
                              // the wrong day is taken to ask tvb for the programm
                         prog = ProgramManager.getInstance().getProgram(timer.getEndTime(), progID);
                         if(prog != null) {
                             node.addProgram(prog);
                         }
                     }
                 }
             }
         }
 
         node.update();
     }
 
     public boolean canUseProgramTree() {
         return true;
     }
 
     public static String getTranslation(String key, String altText) {
         return mLocalizer.msg(key,altText);
     }
     
     public static String getTranslation(String key, String altText, String arg1) {
         return mLocalizer.msg(key,altText, arg1);
     }
     
     public static String getTranslation(String key, String altText, String arg1, String arg2) {
         return mLocalizer.msg(key,altText, arg1, arg2);
     }
     
     public static String getTranslation(String key, String altText, String arg1, String arg2, String arg3) {
         return mLocalizer.msg(key,altText, arg1, arg2, arg3);
     }
     
     public JPopupMenu getSimpleContextMenu(Timer timer) {
         return cmf.createSimpleActionMenu(timer);
     }
     
     public void update(Observable arg0, Object arg1) {
         // mark all "timed" programs 
         ProgramManager.getInstance().markPrograms();
 
         List<Timer> notAssigned = TimerManager.getInstance().getNotAssignedTimers();
         if(notAssigned.size() > 0) {
             ProgramManager.getInstance().handleNotAssignedTimers();
         }
         
         // update the plugin tree
         updateTree();
     }
     
     public void synchronize() {
         TimerManager.getInstance().synchronize();
         RecordingManager.getInstance().synchronize();
     }
     
     private class ContextMenuFactory {
         public ActionMenu createActionMenu(final Program program) {
            AbstractAction action = new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {

                }
            };
            action.putValue(Action.NAME, LazyBones.getTranslation("lazybones", "Lazy Bones"));
            action.putValue(Action.SMALL_ICON, createImageIcon("lazybones/vdr16.png"));

             Marker[] markers = program.getMarkerArr();
             boolean marked = false;
             for (int i = 0; i < markers.length; i++) {
                 if (markers[i].getId().equals(getId())) {
                     marked = true;
                     break;
                 }
             }
 
             boolean notAssignedTimersExist = TimerManager.getInstance().getNotAssignedTimers().size() > 0;
             
             int size = 3;
             
             if(marked || notAssignedTimersExist) {
                 size++;
             }
             
             ActionMenu[] actions = null;
             if (marked) {
                 actions = new ActionMenu[size];
 
                 AbstractAction action1 = new AbstractAction() {
                     public void actionPerformed(ActionEvent evt) {
                         TimerManager.getInstance().deleteTimer(program);
                     }
                 };
                 action1.putValue(Action.NAME, LazyBones.getTranslation("dont_capture", "Delete timer"));
                 action1.putValue(Action.SMALL_ICON, createImageIcon("actions", "edit-delete", 16));
                 actions[1] = new ActionMenu(action1);
 
                 AbstractAction action2 = new AbstractAction() {
                     public void actionPerformed(ActionEvent evt) {
                         Timer timer = (Timer) TimerManager.getInstance().getTimer(program);
                         TimerManager.getInstance().editTimer(timer);
                     }
                 };
                 action2.putValue(Action.NAME, LazyBones.getTranslation("edit", "Edit Timer"));
                 action2.putValue(Action.SMALL_ICON, createImageIcon("actions", "document-edit", 16));
                 actions[2] = new ActionMenu(action2);
 
                 AbstractAction action3 = new AbstractAction() {
                     public void actionPerformed(ActionEvent evt) {
                         LazyBones.getInstance().synchronize();
                     }
                 };
                 action3.putValue(Action.NAME, LazyBones.getTranslation("resync", "Synchronize with VDR"));
                 action3.putValue(Action.SMALL_ICON, createImageIcon("actions", "view-refresh", 16));
                 actions[3] = new ActionMenu(action3);
             } else {
                 actions = new ActionMenu[size];
 
                 AbstractAction action1 = new AbstractAction() {
                     public void actionPerformed(ActionEvent evt) {
                         TimerManager.getInstance().createTimer(program, false);
                     }
                 };
                 action1.putValue(Action.NAME, LazyBones.getTranslation("capture", "Capture with VDR"));
                 action1.putValue(Action.SMALL_ICON, createImageIcon("lazybones/capture.png"));
                 actions[1] = new ActionMenu(action1);
 
                 AbstractAction action2 = new AbstractAction() {
                     public void actionPerformed(ActionEvent evt) {
                         LazyBones.getInstance().synchronize();
                     }
                 };
                 action2.putValue(Action.NAME, LazyBones.getTranslation("resync", "Synchronize with VDR"));
                 action2.putValue(Action.SMALL_ICON, createImageIcon("actions", "view-refresh", 16));
                 actions[2] = new ActionMenu(action2);
                 
                 if(notAssignedTimersExist) {
                    AbstractAction action3 = new AbstractAction() {public void actionPerformed(ActionEvent evt) {}};
                    action3.putValue(Action.NAME, LazyBones.getTranslation("assign", "Assign"));
                    action3.putValue(Action.SMALL_ICON, createImageIcon("lazybones/appointment-new.png"));
                    action3.putValue(Plugin.DISABLED_ON_TASK_MENU, true);
    
                     Action[] timers = new Action[TimerManager.getInstance().getNotAssignedTimers().size()];
                     List<Timer> timerList = TimerManager.getInstance().getNotAssignedTimers();
                     for (int i = 0; i < timers.length; i++) {
                         final Timer timer = timerList.get(i); 
                         timers[i] = new AbstractAction() {
                             public void actionPerformed(ActionEvent e) {
                                 ProgramManager.getInstance().assignTimerToProgram(program, timer);
                                 TimerManager.getInstance().assignProgramToTimer(program, timer);
                                 updateTree();
                             }
                         };
                         timers[i].putValue(Action.NAME, timerList.get(i).getDisplayTitle());
                         timers[i].putValue(LazyBones.TIMER_MENU_KEY, timerList.get(i));
                         timers[i].putValue(Action.SMALL_ICON, createImageIcon("lazybones/appointment-new.png"));
                     }
                    actions[3] = new ActionMenu(action3, timers);
                 }
             }
 
             AbstractAction action0 = new AbstractAction() {
                 public void actionPerformed(ActionEvent evt) {
                     Player.play(program);
                 }
             };
             action0.putValue(Action.NAME, LazyBones.getTranslation("watch", "Watch this channel"));
             action0.putValue(Action.SMALL_ICON, createImageIcon("actions", "media-playback-start", 16));
             actions[0] = new ActionMenu(action0);
 
            return new ActionMenu(action, actions);
         }
         
         JPopupMenu simpleMenu;
         public JPopupMenu createSimpleActionMenu(final Timer timer) {
             if(simpleMenu == null) {
                 simpleMenu = new JPopupMenu();
                 JMenuItem delItem = new JMenuItem(LazyBones.getTranslation("dont_capture", "Delete timer"), createImageIcon("actions", "edit-delete", 16));
                 delItem.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         TimerManager.getInstance().deleteTimer(timer);
                     }
                     
                 });
                 
                 JMenuItem editItem = new JMenuItem(LazyBones.getTranslation("edit", "Edit Timer"), createImageIcon("actions", "document-edit", 16));
                 editItem.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         TimerManager.getInstance().editTimer(timer);
                     }
                     
                 });
                 
                 simpleMenu.add(editItem);
                 simpleMenu.add(delItem);
             }
             
             return simpleMenu;
         }
         
     }
 
 //################ to receive Programs from other plugins ######################
     private final String TARGET_WATCH = "watch";
     private final String TARGET_CAPTURE = "capture";
     
     @Override
     public boolean canReceiveProgramsWithTarget() {
         return true;
     }
 
     @Override
     public ProgramReceiveTarget[] getProgramReceiveTargets() {
         return new ProgramReceiveTarget[] {
                  new ProgramReceiveTarget(this, LazyBones.getTranslation("capture", "Capture with VDR"), TARGET_CAPTURE),
                  new ProgramReceiveTarget(this, LazyBones.getTranslation("watch", "Watch this channel"), TARGET_WATCH)
         };
     }
 
     @Override
     public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
         logger.debug("Program received for target [{}]", receiveTarget.getTargetId());
         if(TARGET_CAPTURE.equals(receiveTarget.getTargetId())) {
             // store current property values
             String threshold = props.getProperty("percentageThreshold");
             String showTimerOptionsDialog = props.getProperty("showTimerOptionsDialog");
             
             String logEpgErrors = props.getProperty("logEPGErrors");
             String logConnectionErrors = props.getProperty("logConnectionErrors");
             
             // set properties to "automatic mode"
             props.setProperty("percentageThreshold", "0");
             props.setProperty("showTimerOptionsDialog", "false");
             props.setProperty("logEPGErrors", "false");
             props.setProperty("logConnectionErrors", "false");
             
             // create timers for all programs
             for (int i = 0; i < programArr.length; i++) {
                 Program prog = programArr[i];
                 TimerManager.getInstance().createTimer(prog, true);
             }
             
             // restore old properties
             props.setProperty("percentageThreshold", threshold);
             props.setProperty("showTimerOptionsDialog", showTimerOptionsDialog);
             props.setProperty("logEPGErrors", logEpgErrors);
             props.setProperty("logConnectionErrors", logConnectionErrors);
         } else if(TARGET_WATCH.equals(receiveTarget.getTargetId())) {
             Player.play(programArr[0]);
         } else if (LazyBonesDevice.TARGET_CAPTURE_PLUGIN_ADD.equals(receiveTarget.getTargetId())) {
             TimerManager.getInstance().createTimer(programArr[0], false);
         } else if (LazyBonesDevice.TARGET_CAPTURE_PLUGIN_REMOVE.equals(receiveTarget.getTargetId())) {
             TimerManager.getInstance().deleteTimer(programArr[0]);
         }
         return true;
     }
 }
