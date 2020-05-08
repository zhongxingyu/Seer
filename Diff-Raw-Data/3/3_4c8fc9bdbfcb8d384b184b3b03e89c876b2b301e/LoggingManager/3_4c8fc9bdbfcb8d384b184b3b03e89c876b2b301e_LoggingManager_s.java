 /*
  * Copyright (c) 2006-2015 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.logging;
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.FrameContainer;
 import com.dmdirc.Query;
 import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
 import com.dmdirc.config.prefs.PluginPreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesDialogModel;
 import com.dmdirc.config.prefs.PreferencesSetting;
 import com.dmdirc.config.prefs.PreferencesType;
 import com.dmdirc.events.BaseChannelActionEvent;
 import com.dmdirc.events.BaseChannelMessageEvent;
 import com.dmdirc.events.BaseQueryActionEvent;
 import com.dmdirc.events.BaseQueryMessageEvent;
 import com.dmdirc.events.ChannelClosedEvent;
 import com.dmdirc.events.ChannelGotTopicEvent;
 import com.dmdirc.events.ChannelJoinEvent;
 import com.dmdirc.events.ChannelKickEvent;
 import com.dmdirc.events.ChannelModeChangeEvent;
 import com.dmdirc.events.ChannelNickChangeEvent;
 import com.dmdirc.events.ChannelOpenedEvent;
 import com.dmdirc.events.ChannelPartEvent;
 import com.dmdirc.events.ChannelQuitEvent;
 import com.dmdirc.events.ChannelTopicChangeEvent;
 import com.dmdirc.events.ClientPrefsOpenedEvent;
 import com.dmdirc.events.QueryClosedEvent;
 import com.dmdirc.events.QueryOpenedEvent;
 import com.dmdirc.events.UserErrorEvent;
 import com.dmdirc.interfaces.GroupChat;
 import com.dmdirc.interfaces.GroupChatUser;
 import com.dmdirc.interfaces.PrivateChat;
 import com.dmdirc.interfaces.User;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.config.ConfigChangeListener;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.plugins.PluginDomain;
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.ui.WindowManager;
 import com.dmdirc.ui.messages.BackBufferFactory;
 import com.dmdirc.ui.messages.Styliser;
 import com.dmdirc.util.io.ReverseFileReader;
 import com.dmdirc.util.io.StreamUtils;
 
 import java.awt.Color;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 import javax.inject.Singleton;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Manages logging activities.
  */
 @Singleton
 public class LoggingManager implements ConfigChangeListener {
 
     /** Date format used for "File Opened At" log. */
     private static final DateFormat OPENED_AT_FORMAT = new SimpleDateFormat(
             "EEEE MMMM dd, yyyy - HH:mm:ss");
     /** Object for synchronising access to the date forma.t */
     private static final Object FORMAT_LOCK = new Object();
     /** This plugin's plugin info. */
     private final String domain;
     private final PluginInfo pluginInfo;
     /** Global config. */
     private final AggregateConfigProvider config;
     /** The manager to add history windows to. */
     private final WindowManager windowManager;
     /** Map of open files. */
     private final Map<String, OpenFile> openFiles = Collections.synchronizedMap(
             new HashMap<>());
     private final DMDircMBassador eventBus;
     private final Provider<String> directoryProvider;
     private final BackBufferFactory backBufferFactory;
     private final LogFileLocator locator;
     /** Timer used to close idle files. */
     private Timer idleFileTimer;
     /** Cached boolean settings. */
     private boolean addtime;
     private boolean stripcodes;
     private boolean channelmodeprefix;
     private boolean autobackbuffer;
     private boolean backbufferTimestamp;
     /** Cached string settings. */
     private String timestamp;
     private String colour;
     /** Cached int settings. */
     private int historyLines;
     private int backbufferLines;
 
     @Inject
     public LoggingManager(@PluginDomain(LoggingPlugin.class) final String domain,
             @PluginDomain(LoggingPlugin.class) final PluginInfo pluginInfo,
             @GlobalConfig final AggregateConfigProvider globalConfig,
             final WindowManager windowManager, final DMDircMBassador eventBus,
             @Directory(LoggingModule.LOGS_DIRECTORY) final Provider<String> directoryProvider,
             final BackBufferFactory backBufferFactory,
             final LogFileLocator locator) {
         this.domain = domain;
         this.pluginInfo = pluginInfo;
         this.config = globalConfig;
         this.windowManager = windowManager;
         this.eventBus = eventBus;
         this.directoryProvider = directoryProvider;
         this.backBufferFactory = backBufferFactory;
         this.locator = locator;
     }
 
     public void load() {
         setCachedSettings();
 
         final File dir = new File(directoryProvider.get());
         if (dir.exists()) {
             if (!dir.isDirectory()) {
                 eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null,
                         "Unable to create logging dir (file exists instead)", ""));
             }
         } else {
             if (!dir.mkdirs()) {
                 eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null,
                         "Unable to create logging dir", ""));
             }
         }
 
         config.addChangeListener(domain, this);
 
         // Close idle files every hour.
         idleFileTimer = new Timer("LoggingPlugin Timer");
         idleFileTimer.schedule(new TimerTask() {
 
             @Override
             public void run() {
                 timerTask();
             }
         }, 3600000);
 
         eventBus.subscribe(this);
     }
 
     public void unload() {
         if (idleFileTimer != null) {
             idleFileTimer.cancel();
             idleFileTimer.purge();
         }
 
         synchronized (openFiles) {
             for (OpenFile file : openFiles.values()) {
                 StreamUtils.close(file.writer);
             }
             openFiles.clear();
         }
 
         eventBus.unsubscribe(this);
     }
 
     /**
      * What to do every hour when the timer fires.
      */
     protected void timerTask() {
         // Oldest time to allow
         final long oldestTime = System.currentTimeMillis() - 3480000;
 
         synchronized (openFiles) {
             final Collection<String> old = new ArrayList<>(openFiles.size());
             for (Map.Entry<String, OpenFile> entry : openFiles.entrySet()) {
                 if (entry.getValue().lastUsedTime < oldestTime) {
                     StreamUtils.close(entry.getValue().writer);
                     old.add(entry.getKey());
                 }
             }
 
             openFiles.keySet().removeAll(old);
         }
     }
 
     @Handler
     public void handleQueryOpened(final QueryOpenedEvent event) {
         final String filename = locator.getLogFile(event.getQuery().getUser());
         if (autobackbuffer) {
             showBackBuffer(event.getQuery(), filename);
         }
 
         synchronized (FORMAT_LOCK) {
             appendLine(filename, "*** Query opened at: %s", OPENED_AT_FORMAT.format(new Date()));
             appendLine(filename, "*** Query with User: %s", event.getQuery().getHost());
             appendLine(filename, "");
         }
     }
 
     @Handler
     public void handleQueryClosed(final QueryClosedEvent event) {
         final String filename = locator.getLogFile(event.getQuery().getUser());
 
         synchronized (FORMAT_LOCK) {
             appendLine(filename, "*** Query closed at: %s", OPENED_AT_FORMAT.format(new Date()));
         }
 
         if (openFiles.containsKey(filename)) {
             StreamUtils.close(openFiles.get(filename).writer);
             openFiles.remove(filename);
         }
     }
 
     @Handler
     public void handleQueryActions(final BaseQueryActionEvent event) {
         final User user = event.getUser();
         final String filename = locator.getLogFile(user);
         appendLine(filename, "* %s %s", user.getNickname(), event.getMessage());
     }
 
     @Handler
     public void handleQueryMessages(final BaseQueryMessageEvent event) {
         final User user = event.getUser();
         final String filename = locator.getLogFile(user);
         appendLine(filename, "<%s> %s", user.getNickname(), event.getMessage());
     }
 
     @Handler
     public void handleChannelMessage(final BaseChannelMessageEvent event) {
         final String filename = locator.getLogFile(event.getChannel().getChannelInfo());
         appendLine(filename, "<%s> %s", getDisplayName(event.getClient()), event.getMessage());
     }
 
     @Handler
     public void handleChannelAction(final BaseChannelActionEvent event) {
         final String filename = locator.getLogFile(event.getChannel().getChannelInfo());
         appendLine(filename, "* %s %s", getDisplayName(event.getClient()), event.getMessage());
     }
 
     @Handler
     public void handleChannelGotTopic(final ChannelGotTopicEvent event) {
         final String filename = locator.getLogFile(event.getChannel().getChannelInfo());
         final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
         final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
 
         appendLine(filename, "*** Topic is: %s", event.getTopic().getTopic());
         appendLine(filename, "*** Set at: %s on %s by %s",
                 timeFormat.format(1000 * event.getTopic().getTime()),
                 dateFormat.format(1000 * event.getTopic().getTime()),
                         event.getTopic().getClient()
                                 .map(GroupChatUser::getNickname).orElse("Unknown"));
     }
 
     @Handler
     public void handleChannelTopicChange(final ChannelTopicChangeEvent event) {
         final String filename = locator.getLogFile(event.getChannel().getChannelInfo());
         appendLine(filename, "*** %s Changed the topic to: %s",
                 event.getTopic().getClient().map(this::getDisplayName).orElse(""), event.getTopic());
     }
 
     @Handler
     public void handleChannelJoin(final ChannelJoinEvent event) {
         final String filename = locator.getLogFile(event.getChannel().getChannelInfo());
         final GroupChatUser channelClient = event.getClient();
         appendLine(filename, "*** %s (%s) joined the channel", getDisplayName(channelClient),
                 channelClient.getNickname());
     }
 
     @Handler
     public void handleChannelPart(final ChannelPartEvent event) {
         final String filename = locator.getLogFile(event.getChannel().getChannelInfo());
         final String message = event.getMessage();
         final GroupChatUser channelClient = event.getClient();
         if (message.isEmpty()) {
             appendLine(filename, "*** %s (%s) left the channel", getDisplayName(channelClient),
                     channelClient.getNickname());
         } else {
             appendLine(filename, "*** %s (%s) left the channel (%s)",
                     getDisplayName(channelClient), channelClient.getNickname(), message);
         }
     }
 
     @Handler
     public void handleChannelQuit(final ChannelQuitEvent event) {
         final String filename = locator.getLogFile(event.getChannel().getChannelInfo());
         final String reason = event.getMessage();
         final GroupChatUser channelClient = event.getClient();
         if (reason.isEmpty()) {
             appendLine(filename, "*** %s (%s) Quit IRC",
                     getDisplayName(channelClient), channelClient.getNickname());
         } else {
             appendLine(filename, "*** %s (%s) Quit IRC (%s)",
                     getDisplayName(channelClient), channelClient.getNickname(), reason);
         }
     }
 
     @Handler
     public void handleChannelKick(final ChannelKickEvent event) {
         final GroupChatUser victim = event.getVictim();
         final GroupChatUser perpetrator = event.getClient();
         final String reason = event.getReason();
         final String filename = locator.getLogFile(event.getChannel().getChannelInfo());
 
         if (reason.isEmpty()) {
             appendLine(filename, "*** %s was kicked by %s",
                     getDisplayName(victim), getDisplayName(perpetrator));
         } else {
             appendLine(filename, "*** %s was kicked by %s (%s)",
                     getDisplayName(victim), getDisplayName(perpetrator), reason);
         }
     }
 
     @Handler
     public void handleNickChange(final ChannelNickChangeEvent event) {
         final String filename = locator.getLogFile(event.getChannel().getChannelInfo());
         appendLine(filename, "*** %s is now %s", getDisplayName(event.getClient(),
                 event.getOldNick()), getDisplayName(event.getClient()));
     }
 
     @Handler
     public void handleModeChange(final ChannelModeChangeEvent event) {
         final String filename = locator.getLogFile(event.getChannel().getChannelInfo());
         if (event.getClient().getNickname().isEmpty()) {
             appendLine(filename, "*** Channel modes are: %s", event.getModes());
         } else {
             appendLine(filename, "*** %s set modes: %s",
                     getDisplayName(event.getClient()), event.getModes());
         }
     }
 
     @Override
     public void configChanged(final String domain, final String key) {
         setCachedSettings();
     }
 
     @Handler
     public void handleChannelOpened(final ChannelOpenedEvent event) {
         final String filename = locator.getLogFile(event.getChannel().getName());
 
         if (autobackbuffer) {
             showBackBuffer(event.getChannel(), filename);
         }
 
         synchronized (FORMAT_LOCK) {
             appendLine(filename, "*** Channel opened at: %s", OPENED_AT_FORMAT.format(new Date()));
             appendLine(filename, "");
         }
     }
 
     @Handler
     public void handleChannelClosed(final ChannelClosedEvent event) {
         final String filename = locator.getLogFile(event.getChannel().getName());
 
         synchronized (FORMAT_LOCK) {
             appendLine(filename, "*** Channel closed at: %s", OPENED_AT_FORMAT.format(new Date()));
         }
 
         if (openFiles.containsKey(filename)) {
             StreamUtils.close(openFiles.get(filename).writer);
             openFiles.remove(filename);
         }
     }
 
     /**
      * Add a backbuffer to a frame.
      *
      * @param frame    The frame to add the backbuffer lines to
      * @param filename File to get backbuffer from
      */
     protected void showBackBuffer(final FrameContainer frame, final String filename) {
         if (frame == null) {
             eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null, "Given a null frame", ""));
             return;
         }
 
         final Path testFile = Paths.get(filename);
         if (Files.exists(testFile)) {
             try {
                 final ReverseFileReader file = new ReverseFileReader(testFile);
                 // Because the file includes a newline char at the end, an empty line
                 // is returned by getLines. To counter this, we call getLines(1) and do
                 // nothing with the output.
                 file.getLines(1);
                 final Stack<String> lines = file.getLines(backbufferLines);
                 while (!lines.empty()) {
                     frame.addLine(getColouredString(colour, lines.pop()), backbufferTimestamp);
                 }
                 file.close();
                 frame.addLine(getColouredString(colour, "--- End of backbuffer\n"),
                         backbufferTimestamp);
             } catch (IOException | SecurityException e) {
                 eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, e,
                         "Unable to show backbuffer (Filename: " + filename + "): " + e.getMessage(),
                         ""));
             }
         }
     }
 
     /**
      * Get a coloured String. If colour is invalid, IRC Colour 14 will be used.
      *
      * @param colour The colour the string should be (IRC Colour or 6-digit hex colour)
      * @param line   the line to colour
      *
      * @return The given line with the appropriate irc codes appended/prepended to colour it.
      */
     protected static String getColouredString(final String colour, final String line) {
         String res = null;
         if (colour.length() < 3) {
             int num;
 
             try {
                 num = Integer.parseInt(colour);
             } catch (NumberFormatException ex) {
                 num = -1;
             }
 
             if (num >= 0 && num <= 15) {
                 res = String.format("%c%02d%s%1$c", Styliser.CODE_COLOUR, num, line);
             }
         } else if (colour.length() == 6) {
             try {
                 Color.decode('#' + colour);
                 res = String.format("%c%s%s%1$c", Styliser.CODE_HEXCOLOUR, colour, line);
             } catch (NumberFormatException ex) { /* Do Nothing */ }
         }
 
         if (res == null) {
             res = String.format("%c%02d%s%1$c", Styliser.CODE_COLOUR, 14, line);
         }
         return res;
     }
 
     /**
      * Add a line to a file.
      *
      * @param filename Name of file to write to
      * @param format   Format of line to add. (NewLine will be added Automatically)
      * @param args     Arguments for format
      *
      * @return true on success, else false.
      */
     protected boolean appendLine(final String filename, final String format, final Object... args) {
         return appendLine(filename, String.format(format, args));
     }
 
     /**
      * Add a line to a file.
      *
      * @param filename Name of file to write to
      * @param line     Line to add. (NewLine will be added Automatically)
      *
      * @return true on success, else false.
      */
     protected boolean appendLine(final String filename, final String line) {
         final StringBuilder finalLine = new StringBuilder();
 
         if (addtime) {
             String dateString;
             try {
                 final DateFormat dateFormat = new SimpleDateFormat(timestamp);
                 dateString = dateFormat.format(new Date()).trim();
             } catch (IllegalArgumentException iae) {
                 // Default to known good format
                 final DateFormat dateFormat = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]");
                 dateString = dateFormat.format(new Date()).trim();
 
                 eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, iae,
                         "Dateformat String '" + timestamp + "' is invalid. For more information: "
                         + "http://java.sun.com/javase/6/docs/api/java/text/SimpleDateFormat.html",
                         ""));
             }
             finalLine.append(dateString);
             finalLine.append(' ');
         }
 
         if (stripcodes) {
             finalLine.append(Styliser.stipControlCodes(line));
         } else {
             finalLine.append(line);
         }
 
         try {
             final BufferedWriter out;
             if (openFiles.containsKey(filename)) {
                 final OpenFile of = openFiles.get(filename);
                 of.lastUsedTime = System.currentTimeMillis();
                 out = of.writer;
             } else {
                 out = new BufferedWriter(new FileWriter(filename, true));
                 openFiles.put(filename, new OpenFile(out));
             }
             out.write(finalLine.toString());
             out.newLine();
             out.flush();
             return true;
         } catch (IOException e) {
             /*
              * Do Nothing
              *
              * Makes no sense to keep adding errors to the logger when we can't write to the file,
              * as chances are it will happen on every incomming line.
              */
         }
         return false;
     }
 
     /**
      * Get name to display for channelClient (Taking into account the channelmodeprefix setting).
      *
      * @param channelClient The client to get the display name for
      *
      * @return name to display
      */
     protected String getDisplayName(final GroupChatUser channelClient) {
         return getDisplayName(channelClient, "");
     }
 
     /**
      * Get name to display for channelClient (Taking into account the channelmodeprefix setting).
      *
      * @param channelClient The client to get the display name for
      * @param overrideNick  Nickname to display instead of real nickname
      *
      * @return name to display
      */
     protected String getDisplayName(final GroupChatUser channelClient, final String overrideNick) {
         if (channelClient == null) {
             return overrideNick.isEmpty() ? "Unknown Client" : overrideNick;
         } else if (overrideNick.isEmpty()) {
            return channelmodeprefix ? channelClient.toString() : channelClient.getNickname();
         } else {
             return channelmodeprefix ? channelClient.getImportantMode() + overrideNick :
                     overrideNick;
         }
     }
 
     /**
      * Shows the history window for the specified target, if available.
      *
      * @param target The window whose history we're trying to open
      *
      * @return True if the history is available, false otherwise
      */
     protected boolean showHistory(final FrameContainer target) {
         final String descriptor;
 
         if (target instanceof GroupChat) {
             descriptor = target.getName();
         } else if (target instanceof Query) {
             descriptor = ((PrivateChat) target).getNickname();
         } else {
             // Unknown component
             return false;
         }
 
         final Path log = Paths.get(locator.getLogFile(descriptor));
 
         if (!Files.exists(log)) {
             // File doesn't exist
             return false;
         }
 
         windowManager.addWindow(target, new HistoryWindow("History", log, target,
                 eventBus, backBufferFactory, historyLines));
 
         return true;
     }
 
     /** Updates cached settings. */
     public void setCachedSettings() {
         addtime = config.getOptionBool(domain, "general.addtime");
         stripcodes = config.getOptionBool(domain, "general.stripcodes");
         channelmodeprefix = config.getOptionBool(domain, "general.channelmodeprefix");
         autobackbuffer = config.getOptionBool(domain, "backbuffer.autobackbuffer");
         backbufferTimestamp = config.getOptionBool(domain, "backbuffer.timestamp");
         timestamp = config.getOption(domain, "general.timestamp");
         historyLines = config.getOptionInt(domain, "history.lines");
         colour = config.getOption(domain, "backbuffer.colour");
         backbufferLines = config.getOptionInt(domain, "backbuffer.lines");
     }
 
     @Handler
     public void showConfig(final ClientPrefsOpenedEvent event) {
         final PreferencesDialogModel manager = event.getModel();
         final PreferencesCategory general = new PluginPreferencesCategory(
                 pluginInfo, "Logging", "General configuration for Logging plugin.");
         final PreferencesCategory backbuffer = new PluginPreferencesCategory(
                 pluginInfo, "Back Buffer", "Options related to the automatic backbuffer");
         final PreferencesCategory advanced = new PluginPreferencesCategory(
                 pluginInfo, "Advanced",
                 "Advanced configuration for Logging plugin. You shouldn't need to edit this unless you know what you are doing.");
 
         general.addSetting(new PreferencesSetting(PreferencesType.DIRECTORY,
                 pluginInfo.getDomain(), "general.directory", "Directory",
                 "Directory for log files", manager.getConfigManager(),
                 manager.getIdentity()));
         general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 pluginInfo.getDomain(), "general.networkfolders",
                 "Separate logs by network",
                 "Should the files be stored in a sub-dir with the networks name?",
                 manager.getConfigManager(), manager.getIdentity()));
         general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 pluginInfo.getDomain(), "general.addtime", "Timestamp logs",
                 "Should a timestamp be added to the log files?",
                 manager.getConfigManager(), manager.getIdentity()));
         general.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                 pluginInfo.getDomain(), "general.timestamp", "Timestamp format",
                 "The String to pass to 'SimpleDateFormat' to format the timestamp",
                 manager.getConfigManager(), manager.getIdentity()));
         general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 pluginInfo.getDomain(), "general.stripcodes", "Strip Control Codes",
                 "Remove known irc control codes from lines before saving?",
                 manager.getConfigManager(), manager.getIdentity()));
         general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 pluginInfo.getDomain(), "general.channelmodeprefix",
                 "Show channel mode prefix", "Show the @,+ etc next to nicknames",
                 manager.getConfigManager(), manager.getIdentity()));
 
         backbuffer.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 pluginInfo.getDomain(), "backbuffer.autobackbuffer", "Automatically display",
                 "Automatically display the backbuffer when a channel is joined",
                 manager.getConfigManager(), manager.getIdentity()));
         backbuffer.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                 pluginInfo.getDomain(), "backbuffer.colour", "Colour to use for display",
                 "Colour used when displaying the backbuffer",
                 manager.getConfigManager(), manager.getIdentity()));
         backbuffer.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                 pluginInfo.getDomain(), "backbuffer.lines", "Number of lines to show",
                 "Number of lines used when displaying backbuffer",
                 manager.getConfigManager(), manager.getIdentity()));
         backbuffer.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 pluginInfo.getDomain(), "backbuffer.timestamp", "Show Formatter-Timestamp",
                 "Should the line be added to the frame with the timestamp from "
                         + "the formatter aswell as the file contents",
                 manager.getConfigManager(), manager.getIdentity()));
 
         advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 pluginInfo.getDomain(), "advanced.filenamehash", "Add Filename hash",
                 "Add the MD5 hash of the channel/client name to the filename. "
                         + "(This is used to allow channels with similar names "
                         + "(ie a _ not a  -) to be logged separately)",
                 manager.getConfigManager(), manager.getIdentity()));
 
         advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 pluginInfo.getDomain(), "advanced.usedate", "Use Date directories",
                 "Should the log files be in separate directories based on the date?",
                 manager.getConfigManager(), manager.getIdentity()));
         advanced.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                 pluginInfo.getDomain(), "advanced.usedateformat", "Archive format",
                 "The String to pass to 'SimpleDateFormat' to format the "
                         + "directory name(s) for archiving",
                 manager.getConfigManager(), manager.getIdentity()));
 
         general.addSubCategory(backbuffer.setInline());
         general.addSubCategory(advanced.setInline());
         manager.getCategory("Plugins").addSubCategory(general.setInlineAfter());
     }
 
     /** Open File. */
     private static class OpenFile {
 
         /** Last used time. */
         public long lastUsedTime = System.currentTimeMillis();
         /** Open file's writer. */
         public final BufferedWriter writer;
 
         /**
          * Creates a new open file.
          *
          * @param writer Writer that has file open
          */
         protected OpenFile(final BufferedWriter writer) {
             this.writer = writer;
         }
 
     }
 
 }
