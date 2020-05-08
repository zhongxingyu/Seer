 /*
  * Copyright (C) 2011  Francois Forster
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.tcpdelay;
 
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Class that proxies TCP requests. Can be used to test dependencies failures
  * and connection delays without having to touch the dependencies. Simply point
  * the service to be tested to the proxy and point the proxy to the dependency.
  */
 public abstract class Launcher {
     private static final String HELP_ARG = "-help";
     private static final String LOG_LEVEL_ARG = "-logLevel";
     private static final String DELAY_LEVEL_ARG = "-delayMs";
     private static final String REMOTE_PORT_ARG = "-remotePort";
     private static final String REMOTE_HOST_ARG = "-remoteHost";
     private static final String LOCAL_PORT_ARG = "-localPort";
 
     public static void main(String[] args)
             throws Exception {
         if (hasArg(args, HELP_ARG) ||
                 !hasArg(args, LOCAL_PORT_ARG) ||
                 !hasArg(args, REMOTE_HOST_ARG) ||
                 !hasArg(args, REMOTE_PORT_ARG)) {
             displayUsageAndExit();
         }
         Level logLevel = Level.INFO;
         if (hasArg(args, LOG_LEVEL_ARG)) {
             String logArg = getArgData(args, LOG_LEVEL_ARG);
             if ("DEBUG".equalsIgnoreCase(logArg)) {
                 logLevel = Level.FINE;
             } else if ("INFO".equalsIgnoreCase(logArg)) {
                 logLevel = Level.INFO;
             } else if ("ERROR".equalsIgnoreCase(logArg)) {
                 logLevel = Level.SEVERE;
             } else {
                 displayErrorMessageExit("Invalid log level: " + getArgData(args, LOG_LEVEL_ARG));
             }
         }
        Logger.getLogger(Launcher.class.getName()).setLevel(logLevel);
        getConsoleHandler().setLevel(logLevel);
 
         int localPort = 0;
         try {
             localPort = Integer.parseInt(getArgData(args, LOCAL_PORT_ARG));
         } catch (NumberFormatException e) {
             displayErrorMessageExit("Invalid local port: " + getArgData(args, LOCAL_PORT_ARG));
         }
         String remoteHost = getArgData(args, REMOTE_HOST_ARG);
         int remotePort = 0;
         try {
             remotePort = Integer.parseInt(getArgData(args, REMOTE_PORT_ARG));
         } catch (NumberFormatException e) {
             displayErrorMessageExit("Invalid remote port: " + getArgData(args, REMOTE_PORT_ARG));
         }
 
         int delayMs = 0;
         if (hasArg(args, DELAY_LEVEL_ARG)) {
             try {
                 delayMs = Integer.parseInt(getArgData(args, DELAY_LEVEL_ARG));
             } catch (NumberFormatException e) {
                 displayErrorMessageExit("Invalid delay: " + getArgData(args, DELAY_LEVEL_ARG));
             }
 
         }
 
         TCPDataReader reader = new TCPDataReader(localPort, remoteHost, remotePort, delayMs);
         reader.run();
     }
 
     private static void displayUsageAndExit() {
         System.err.println("Usage: java Launcher [" + HELP_ARG + "] [" + LOG_LEVEL_ARG
                 + " {DEBUG|INFO|ERROR}] [" + DELAY_LEVEL_ARG + " <ms delay>] " + LOCAL_PORT_ARG
                 + " <port> " + REMOTE_HOST_ARG + " <hostname> " + REMOTE_PORT_ARG + " <port>");
         System.exit(1);
     }
 
     private static void displayErrorMessageExit(String message) {
         System.err.println(message);
         System.exit(1);
     }
 
     private static boolean hasArg(String[] args, String arg) {
         for (String s : args) {
             if (arg.equalsIgnoreCase(s)) {
                 return true;
             }
         }
         return false;
     }
 
     private static String getArgData(String[] args, String arg) {
         for (int i = 0; i < args.length; i++) {
             if (arg.equalsIgnoreCase(args[i]) && args.length > i + 1) {
                 return args[i + 1];
             }
         }
         return null;
     }
 
    private static Handler getConsoleHandler() {
        Logger topLogger = java.util.logging.Logger.getLogger("");
         Handler consoleHandler = null;
         for (Handler handler : topLogger.getHandlers()) {
             if (handler instanceof ConsoleHandler) {
                 consoleHandler = handler;
                 break;
             }
         }
         if (consoleHandler == null) {
             consoleHandler = new ConsoleHandler();
             topLogger.addHandler(consoleHandler);
         }
        return consoleHandler;
     }
 
 }
