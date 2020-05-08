 /*
  * This file is part of VisualIllusionsMinecraftServerLauncher.
  *
  * Copyright © 2013-2014 Visual Illusions Entertainment
  *
  * VisualIllusionsMinecraftServerLauncher is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * VisualIllusionsMinecraftServerLauncher is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with VisualIllusionsMinecraftServerLauncher.
  * If not, see http://www.gnu.org/licenses/gpl.html.
  *
  * Minecraft is the property of Mojang AB/Notch Development AB
  * Copyright &copy; 2009-2014 Mojang AB
  * "Minecraft" is a trademark of Notch Development AB
  *
  * Visual Illusions Minecraft Server Launcher and Visual Illusions Entertainment are
  * NOT affilated with, endorsed by, or sponsored by Mojang AB or Notch Development AB.
  */
 package net.visualillusionsent.minecraft.server.launcher;
 
 import net.visualillusionsent.minecraft.server.launcher.controller.MinecraftOptions;
 import net.visualillusionsent.minecraft.server.launcher.controller.jvm.JVMOptions;
 import net.visualillusionsent.minecraft.server.launcher.display.LogPanelFormatter;
 import net.visualillusionsent.minecraft.server.launcher.display.OutputQueue;
 import net.visualillusionsent.minecraft.server.launcher.display.OutputThread;
 import net.visualillusionsent.minecraft.server.launcher.info.ServerInfo;
 import net.visualillusionsent.minecraft.server.launcher.info.ServerType;
 import net.visualillusionsent.minecraft.server.launcher.logging.LaunchLogger;
 import net.visualillusionsent.minecraft.server.launcher.menu.file.history.FileHistory;
 import net.visualillusionsent.minecraft.server.launcher.menu.file.history.ServerHistory;
 
 import java.awt.*;
 import java.io.File;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import java.util.logging.Level;
 import java.util.regex.Pattern;
 
 /** @author Jason (darkdiplomat) */
 public final class ControlRoom extends Thread {
    private static final Pattern modLoggerPattern = Pattern.compile("\\[\\d{2}:\\d{2}:\\d{2}(])?&nbsp;(\\[)?\\w+\\]:&nbsp;.+");
     private static final Pattern log4jPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}&nbsp;\\d{2}:\\d{2}:\\d{2},\\d{3}&nbsp;.+");
 
     private final String span = "<p style=\"color:%s\">%s</p>";
     private LaunchLogger logger = new LaunchLogger();
     private CentralPanel central;
     private LaunchPad pad;
     private Manifest mf;
     private static ControlRoom $;
     private static ServerInfo servInfo;
     private static JVMOptions jvmop;
     private static MinecraftOptions mc_op;
     private static FileHistory sfh;
     private LogPanelFormatter formatter;
     private volatile OutputQueue queue;
     private volatile boolean started;
 
     public static final void main(String[] args) {
         prepForLaunch();
     }
 
     public static final void stopServer() {
         $.pad.stopServer(false);
     }
 
     public static final void startServer() {
         $.pad.startServer();
     }
 
     public static final void restartServer() {
         $.pad.restartServer();
     }
 
     public static final void killServer() {
         $.pad.killServer(true);
     }
 
     public static final ServerInfo getServerInfo() {
         return servInfo;
     }
 
     public static final ServerType getServerType() {
         return servInfo.getServerType();
     }
 
     public static final void setServerInfo(ServerInfo serv_info) {
         servInfo = serv_info;
         $.central.repaintInfo();
         sfh.insertPathname(serv_info.getFullPath());
         button_menu_state();
     }
 
     public static final boolean isServerRunning() {
         return $.pad.serverRunning();
     }
 
     public static final void setJVMOptions(JVMOptions jvmopt) {
         if (jvmop == null) {
             jvmop = jvmopt;
         }
     }
 
     public static final void setMCOptions(MinecraftOptions mc_opt) {
         if (mc_op == null) {
             mc_op = mc_opt;
         }
     }
 
     public static final JVMOptions getJVMOptions() {
         return jvmop;
     }
 
     public final void exit() {
         log(Level.FINE, "Preparing to exit...", null);
         if (isServerRunning()) {
             log(Level.FINE, "Server Running. Stopping...", null);
             pad.stopServer(false);
         }
         log(Level.FINE, "Disposing of GUI...", null);
         central.setVisible(false);
         central.dispose();
         log(Level.FINE, "Saving settings...", null);
         LaunchSettings.$.saveProps();
         sfh.saveHistoryEntries();
         log(Level.FINE, "Exiting...", null);
         logger.close();
     }
 
     public static final void output(Level level, String log) {
         addLineToQueue(log, level);
         $.logger.log(level, log);
     }
 
     public static final void output(Level level, String log, Throwable thrown) {
         addLineToQueue(log, level);
         $.logger.log(level, log, thrown);
     }
 
     public static final void button_menu_state() {
         $.central.getControlPanel().getButtonsAndRam().getActionButtons().setState();
         $.central.getMainMenu().getServerMenu().setRunningState();
     }
 
     public static final void logFine(String msg) {
         $.logger.fine(msg);
     }
 
     public static final void log(Level level, String msg, Throwable thrown) {
         if (thrown != null) {
             $.logger.log(level, msg, thrown);
         }
         else {
             $.logger.log(level, msg);
         }
     }
 
     public static final int getRAM() {
         return $.central.getControlPanel().getButtonsAndRam().getRAMBox().getSelectedRAM();
     }
 
     public static final int getRAMIndex() {
         return $.central.getControlPanel().getButtonsAndRam().getRAMBox().getSelectedRAMIndex();
     }
 
     public static final String getMinecraftOptions() {
         return mc_op.getMinecraftArgs();
     }
 
     public static final void sendCommand(String action) {
         $.pad.sendCommand(action);
     }
 
     public static final void setWindowRelativeToCentral(Window window) {
         window.setLocationRelativeTo($.central);
     }
 
     public final String getVersion() {
         if (mf != null) {
             return mf.getMainAttributes().getValue("Specification-Version");
         }
         return "UNDEFINED";
     }
 
     public final String getStatus() {
         if (mf != null) {
             return mf.getMainAttributes().getValue("Build-Status");
         }
         return "UNDEFINED";
     }
 
     private final Manifest getManifest() {
         try {
             return new JarFile(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getManifest();
         }
         catch (Exception ex) {
             log(Level.SEVERE, "Unable to read Manifest...", ex);
         }
         return null;
     }
 
     public String getCleanStatus() {
         String status = getStatus();
         if (status.equals("STABLE")) {
             return "";
         }
         return status;
     }
 
    private static boolean stacking;
 
     public static final void addLineToQueue(String line, Level level) {
         String colorized = "#999999";
         String input;
 
         if (level == null) {
             boolean is4j = false;
             if (modLoggerPattern.matcher(line).matches() || (is4j = log4jPattern.matcher(line).matches())) {
                 String prefix = line.substring(0, !is4j ? line.indexOf(':', 8/* Adjust 8 characters to avoid Time*/) : 60);
                 if (prefix.contains("WARN"/*ING*/) || prefix.contains("DEBUG")) {
                     colorized = "#EB7400";
                    stacking = false;
                 }
                 else if (prefix.contains("SEVERE") || prefix.contains("ERROR") || prefix.contains("FATAL")) {
                     colorized = "red";
                    stacking = true;
                 }
                 else if (prefix.contains("INFO")) {
                     colorized = "white";
                    stacking = false;
                 }
             }
             else {
                if (stacking) {
                     colorized = "red";
                 }
             }
             input = $.formatter.convertToHTML(String.format($.span, colorized, line));
         }
         else {
             colorized = "green";
             if (level == Level.WARNING) {
                 colorized = "#EB7400";
             }
            else if (level == Level.SEVERE) {
                 colorized = "red";
             }
             input = String.format($.span, colorized, line);
         }
         $.queue.add(input);
     }
 
     private final static void prepForLaunch() {
         $ = new ControlRoom();
         $.start();
     }
 
     public final void run() {
         if (!started) {
             log(Level.FINE, "Starting Server Launcher...", null);
             started = true;
             log(Level.FINE, "Checking manifest...", null);
             this.mf = getManifest();
             log(Level.FINE, "Launching GUI...", null);
             central = new CentralPanel(this);
             pad = new LaunchPad(this);
             sfh = new FileHistory(new ServerHistory(central, central.getMainMenu()), getAppData("server_history.hst"));
             log(Level.FINE, "Initializing history...", null);
             sfh.initFileMenuHistory();
             queue = new OutputQueue();
             formatter = new LogPanelFormatter();
             new OutputThread(queue, $.central.getIOPanel().getLogPanel().getLogTextPanel()).start();
         }
     }
 
     public final static String getAppData(String file) {
         StringBuilder path = new StringBuilder(System.getProperty("user.home").toLowerCase());
         path.append(File.separator);
         if (path.toString().contains("win")) {
             path.append("AppData").append(File.separator).append("local").append(File.separator);
         }
         else if (path.toString().contains("mac")) {
             path.append("Library").append(File.separator).append("Application Support").append(File.separator);
         }
         else {
             path.append(".");
         }
         path.append("visualillusions").append(File.separator).append("MinecraftServerLauncher").append(File.separator);
         new File(path.toString()).mkdirs();
         path.append(file);
         return path.toString();
     }
 
     public final static ControlRoom instance() {
         return $;
     }
 }
