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
 
 package com.dmdirc.addons.identd;
 
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.events.UserErrorEvent;
 import com.dmdirc.interfaces.Connection;
 import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.interfaces.User;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.config.ReadOnlyConfigProvider;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.util.io.StreamUtils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 /**
  * The IdentClient responds to an ident request.
  */
 public class IdentClient implements Runnable {
 
     /** The event bus to post errors on. */
     private final DMDircMBassador eventBus;
     /** The IdentdServer that owns this Client. */
     private final IdentdServer server;
     /** The Socket that we are in charge of. */
     private final Socket socket;
     /** The Thread in use for this client. */
     private volatile Thread thread;
     /** Server manager. */
     private final ConnectionManager connectionManager;
     /** Global configuration to read settings from. */
     private final AggregateConfigProvider config;
     /** This plugin's settings domain. */
     private final String domain;
 
     /**
      * Create the IdentClient.
      *
      * @param eventBus      The event bus to post errors on
      * @param server        The server that owns this
      * @param socket        The socket we are handing
      * @param connectionManager Server manager to retrieve servers from
      * @param config        Global config to read settings from
      * @param domain        This plugin's settings domain
      */
     public IdentClient(final DMDircMBassador eventBus, final IdentdServer server, final Socket socket,
             final ConnectionManager connectionManager, final AggregateConfigProvider config,
             final String domain) {
         this.eventBus = eventBus;
         this.server = server;
         this.socket = socket;
         this.connectionManager = connectionManager;
         this.config = config;
         this.domain = domain;
     }
 
     /**
      * Starts this ident client in a new thread.
      */
     public void start() {
         thread = new Thread(this);
         thread.start();
     }
 
     /**
      * Process this connection.
      */
     @Override
     public void run() {
         final Thread thisThread = Thread.currentThread();
         try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
             final String inputLine;
             if ((inputLine = in.readLine()) != null) {
                 out.println(getIdentResponse(inputLine, config));
             }
         } catch (IOException e) {
             if (thisThread == thread) {
                 eventBus.publishAsync(new UserErrorEvent(ErrorLevel.HIGH, e,
                         "ClientSocket Error: " + e.getMessage(), ""));
             }
         } finally {
             StreamUtils.close(socket);
             server.delClient(this);
         }
     }
 
     /**
      * Get the ident response for a given line. Complies with rfc1413
      * (http://www.faqs.org/rfcs/rfc1413.html)
      *
      * @param input  Line to generate response for
      * @param config The config manager to use for settings
      *
      * @return the ident response for the given line
      */
     protected String getIdentResponse(final String input, final ReadOnlyConfigProvider config) {
         final String unescapedInput = unescapeString(input);
         final String[] bits = unescapedInput.replaceAll("\\s+", "").split(",", 2);
         if (bits.length < 2) {
             return String.format("%s : ERROR : X-INVALID-INPUT", escapeString(unescapedInput));
         }
         final int myPort;
         final int theirPort;
         try {
             myPort = Integer.parseInt(bits[0].trim());
             theirPort = Integer.parseInt(bits[1].trim());
         } catch (NumberFormatException e) {
             return String.format("%s , %s : ERROR : X-INVALID-INPUT", escapeString(bits[0]),
                     escapeString(bits[1]));
         }
 
         if (myPort > 65535 || myPort < 1 || theirPort > 65535 || theirPort < 1) {
             return String.format("%d , %d : ERROR : INVALID-PORT", myPort, theirPort);
         }
 
         final Connection connection = getConnectionByPort(myPort);
         if (!config.getOptionBool(domain, "advanced.alwaysOn") && (connection == null
                 || config.getOptionBool(domain, "advanced.isNoUser"))) {
             return String.format("%d , %d : ERROR : NO-USER", myPort, theirPort);
         }
 
         if (config.getOptionBool(domain, "advanced.isHiddenUser")) {
             return String.format("%d , %d : ERROR : HIDDEN-USER", myPort, theirPort);
         }
 
         final String osName = System.getProperty("os.name").toLowerCase();
         final String os;
 
         final String customSystem = config.getOption(domain, "advanced.customSystem");
         if (config.getOptionBool(domain, "advanced.useCustomSystem") && customSystem
                 != null && !customSystem.isEmpty() && customSystem.length() < 513) {
             os = customSystem;
         } else {
             // Tad excessive maybe, but complete!
             // Based on: http://mindprod.com/jgloss/properties.html
             // and the SYSTEM NAMES section of rfc1340 (http://www.faqs.org/rfcs/rfc1340.html)
             if (osName.startsWith("windows")) {
                 os = "WIN32";
             } else if (osName.startsWith("mac")) {
                 os = "MACOS";
             } else if (osName.startsWith("linux")) {
                 os = "UNIX";
             } else if (osName.contains("bsd")) {
                 os = "UNIX-BSD";
             } else if ("os/2".equals(osName)) {
                 os = "OS/2";
             } else if (osName.contains("unix")) {
                 os = "UNIX";
             } else if ("irix".equals(osName)) {
                 os = "IRIX";
             } else {
                 os = "UNKNOWN";
             }
         }
 
         final String customName = config.getOption(domain, "general.customName");
         final String username;
         if (config.getOptionBool(domain, "general.useCustomName") && customName
                 != null && !customName.isEmpty() && customName.length() < 513) {
             username = customName;
         } else if (connection != null && config.getOptionBool(domain, "general.useNickname")) {
            username = connection.getLocalUser().map(User::getNickname).orElse("Unknown");
         } else if (connection != null && config.getOptionBool(domain, "general.useUsername")) {
            username = connection.getLocalUser().flatMap(User::getUsername).orElse("Unknown");
         } else {
             username = System.getProperty("user.name");
         }
 
         return String.format("%d , %d : USERID : %s : %s", myPort, theirPort, escapeString(os),
                 escapeString(username));
     }
 
     /**
      * Escape special chars.
      *
      * @param str String to escape
      *
      * @return Escaped string.
      */
     public static String escapeString(final String str) {
         return str.replace("\\", "\\\\").replace(":", "\\:").replace(",", "\\,").replace(" ", "\\ ");
     }
 
     /**
      * Unescape special chars.
      *
      * @param str String to escape
      *
      * @return Escaped string.
      */
     public static String unescapeString(final String str) {
         return str.replace("\\:", ":").replace("\\ ", " ").replace("\\,", ",").replace("\\\\", "\\");
     }
 
     /**
      * Close this IdentClient.
      */
     public void close() {
         if (thread != null) {
             final Thread tmpThread = thread;
             thread = null;
             if (tmpThread != null) {
                 tmpThread.interrupt();
             }
             StreamUtils.close(socket);
         }
     }
 
     /**
      * Retrieves the server that is bound to the specified local port.
      *
      * @param port Port to check for
      *
      * @return The server instance listening on the given port
      */
     protected Connection getConnectionByPort(final int port) {
         for (Connection connection : connectionManager.getConnections()) {
             if (connection.getParser().get().getLocalPort() == port) {
                 return connection;
             }
         }
         return null;
     }
 
 }
