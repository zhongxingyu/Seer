 /*
  * Copyright (c) 2006-2014 DMDirc Developers
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
 
 package com.dmdirc.addons.dcc;
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.FrameContainer;
 import com.dmdirc.addons.dcc.events.DccChatRequestEvent;
 import com.dmdirc.addons.dcc.events.DccSendRequestEvent;
 import com.dmdirc.addons.dcc.io.DCC;
 import com.dmdirc.addons.dcc.io.DCCChat;
 import com.dmdirc.addons.dcc.io.DCCTransfer;
 import com.dmdirc.addons.dcc.kde.KFileChooser;
 import com.dmdirc.addons.ui_swing.SwingWindowFactory;
 import com.dmdirc.addons.ui_swing.components.frames.ComponentFrameFactory;
 import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
 import com.dmdirc.addons.ui_swing.injection.MainWindow;
 import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
 import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
 import com.dmdirc.commandparser.parsers.GlobalCommandParser;
 import com.dmdirc.events.ServerCtcpEvent;
 import com.dmdirc.interfaces.CommandController;
 import com.dmdirc.interfaces.Connection;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.config.ConfigProvider;
 import com.dmdirc.interfaces.config.IdentityController;
 import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
 import com.dmdirc.messages.MessageSinkManager;
 import com.dmdirc.parser.interfaces.ClientInfo;
 import com.dmdirc.parser.interfaces.Parser;
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.ui.WindowManager;
 import com.dmdirc.ui.input.TabCompleterFactory;
 import com.dmdirc.util.URLBuilder;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 
 import java.awt.Window;
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Arrays;
 import java.util.HashSet;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 /**
  * This plugin adds DCC to DMDirc.
  */
 @Singleton
 public class DCCManager {
 
     /** Our DCC Container window. */
     private PlaceholderContainer container;
     /** Config manager to read settings from. */
     private final AggregateConfigProvider config;
     /** The sink manager to use to despatch messages. */
     private final MessageSinkManager messageSinkManager;
     /** Window Management. */
     private final WindowManager windowManager;
     /** The command controller to use. */
     private final CommandController commandController;
     /** The factory to use for tab completers. */
     private final TabCompleterFactory tabCompleterFactory;
     /** The client's main window that will parent any new windows. */
     private final Window mainWindow;
     /** The configuration domain to use. */
     private final String domain;
     /** The URL builder to use when finding icons. */
     private final URLBuilder urlBuilder;
     /** The bus to despatch events on. */
     private final EventBus eventBus;
 
     /**
      * Creates a new instance of this plugin.
      *
      * @param mainWindow            The window that will parent any new dialogs.
      * @param pluginInfo            This plugin's plugin info
      * @param identityController    The Identity controller that provides the current config
      * @param globalConfig          The configuration to read settings from.
      * @param commandController     Command controller to register commands
      * @param messageSinkManager    The sink manager to use to despatch messages.
      * @param windowManager         Window Management
      * @param tabCompleterFactory   The factory to use for tab completers.
      * @param windowFactory         The window factory to register the DCC implementations with.
      * @param componentFrameFactory Factory to use to create new component frames for DCC windows.
      * @param urlBuilder            The URL builder to use when finding icons.
      * @param eventBus              The bus to despatch events on.
      * @param commandParser         The command parser to use for DCC windows.
      * @param baseDirectory         The directory to create a downloads directory within.
      */
     @Inject
     public DCCManager(
             @MainWindow final Window mainWindow,
             final PluginInfo pluginInfo,
             final IdentityController identityController,
             @GlobalConfig final AggregateConfigProvider globalConfig,
             final CommandController commandController,
             final MessageSinkManager messageSinkManager,
             final WindowManager windowManager,
             final TabCompleterFactory tabCompleterFactory,
             final SwingWindowFactory windowFactory,
             final ComponentFrameFactory componentFrameFactory,
             final URLBuilder urlBuilder,
             final EventBus eventBus,
             final GlobalCommandParser commandParser,
             @Directory(DirectoryType.BASE) final String baseDirectory) {
         this.mainWindow = mainWindow;
         this.messageSinkManager = messageSinkManager;
         this.windowManager = windowManager;
         this.commandController = commandController;
         this.tabCompleterFactory = tabCompleterFactory;
         this.domain = pluginInfo.getDomain();
         this.config = globalConfig;
         this.urlBuilder = urlBuilder;
         this.eventBus = eventBus;
 
         windowFactory.registerImplementation(
                 new HashSet<>(Arrays.asList("com.dmdirc.addons.dcc.ui.PlaceholderPanel")),
                 new SwingWindowFactory.WindowProvider() {
                     @Override
                     public TextFrame getWindow(final FrameContainer container) {
                         return componentFrameFactory.getComponentFrame(container, commandParser);
                     }
                 });
         windowFactory.registerImplementation(
                 new HashSet<>(Arrays.asList("com.dmdirc.addons.dcc.ui.TransferPanel")),
                 new SwingWindowFactory.WindowProvider() {
                     @Override
                     public TextFrame getWindow(final FrameContainer container) {
                         return componentFrameFactory.getComponentFrame(container, commandParser);
                     }
                 });
 
         final ConfigProvider defaults = identityController.getAddonSettings();
         defaults.setOption(domain, "receive.savelocation",
                 baseDirectory + "downloads" + File.separator);
     }
 
     public String getDomain() {
         return domain;
     }
 
     /**
      * Ask the location to save a file, then start the download.
      *
      * @param nickname Person this dcc is from.
      * @param send     The DCCSend to save for.
      * @param parser   The parser this send was received on
      * @param reverse  Is this a reverse dcc?
      * @param token    Token used in reverse dcc.
      */
     public void saveFile(final String nickname, final DCCTransfer send,
             final Parser parser, final boolean reverse, final String token) {
         // New thread to ask the user where to save in to stop us locking the UI
         new Thread(new Runnable() {
 
             @Override
             public void run() {
                 final JFileChooser jc = KFileChooser.getFileChooser(config,
                         DCCManager.this,
                         config.getOption(getDomain(), "receive.savelocation"));
                 int result;
                 if (config.getOptionBool(getDomain(), "receive.autoaccept")) {
                     result = JFileChooser.APPROVE_OPTION;
                 } else {
                     result = showFileChooser(send, jc);
                 }
                 if (result != JFileChooser.APPROVE_OPTION) {
                     return;
                 }
                 send.setFileName(jc.getSelectedFile().getPath());
                 if (!handleExists(send, jc, nickname, parser, reverse, token)) {
                     return;
                 }
                 final boolean resume = handleResume(jc);
                 if (reverse && !token.isEmpty()) {
                     TransferContainer container = new TransferContainer(DCCManager.this, send,
                             config, "*Receive: " + nickname, nickname, null, urlBuilder, eventBus);
                     windowManager.addWindow(getContainer(), container);
                     send.setToken(token);
                     if (resume) {
                         if (config.getOptionBool(getDomain(),
                                 "receive.reverse.sendtoken")) {
                             parser.sendCTCP(nickname, "DCC", "RESUME "
                                     + send.getShortFileName() + " 0 "
                                     + jc.getSelectedFile().length() + " "
                                     + token);
                         } else {
                             parser.sendCTCP(nickname, "DCC", "RESUME "
                                     + send.getShortFileName() + " 0 "
                                     + jc.getSelectedFile().length());
                         }
                     } else {
                         if (listen(send)) {
                             parser.sendCTCP(nickname, "DCC", "SEND "
                                     + send.getShortFileName() + " "
                                     + DCC.ipToLong(getListenIP(parser))
                                     + " " + send.getPort() + " "
                                     + send.getFileSize() + " " + token);
                         }
                     }
                 } else {
                     TransferContainer container = new TransferContainer(DCCManager.this, send,
                             config, "Receive: " + nickname, nickname, null, urlBuilder, eventBus);
                     windowManager.addWindow(getContainer(), container);
                     if (resume) {
                         parser.sendCTCP(nickname, "DCC", "RESUME "
                                 + send.getShortFileName() + " "
                                 + send.getPort() + " "
                                 + jc.getSelectedFile().length());
                     } else {
                         send.connect();
                     }
                 }
             }
         }, "saveFileThread: " + send.getShortFileName()).start();
     }
 
     /**
      * Checks if the selected file exists and prompts the user as required.
      *
      * @param send     DCC Transfer
      * @param jc       File chooser
      * @param nickname Remote nickname
      * @param parser   Parser
      * @param reverse  Reverse DCC?
      * @param token    DCC token
      *
      * @return true if the user wants to continue, false if they wish to abort
      */
     private boolean handleExists(final DCCTransfer send, final JFileChooser jc,
             final String nickname, final Parser parser, final boolean reverse,
             final String token) {
         if (jc.getSelectedFile().exists() && send.getFileSize() > -1
                 && send.getFileSize() <= jc.getSelectedFile().length()) {
             if (config.getOptionBool(getDomain(), "receive.autoaccept")) {
                 return false;
             } else {
                 JOptionPane.showMessageDialog(
                         mainWindow,
                         "This file has already "
                         + "been completed, or is longer than the file you are "
                         + "receiving.\nPlease choose a different file.",
                         "Problem with selected file",
                         JOptionPane.ERROR_MESSAGE);
                 saveFile(nickname, send, parser, reverse, token);
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Prompts the user to resume a transfer if required.
      *
      * @param jc File chooser
      *
      * @return true if the user wants to continue the transfer false otherwise
      */
     private boolean handleResume(final JFileChooser jc) {
         if (jc.getSelectedFile().exists()) {
             if (config.getOptionBool(getDomain(), "receive.autoaccept")) {
                 return true;
             } else {
                 final int result = JOptionPane.showConfirmDialog(
                         mainWindow, "This file exists already"
                         + ", do you want to resume an exisiting download?",
                         "Resume Download?", JOptionPane.YES_NO_OPTION);
                 return (result == JOptionPane.YES_OPTION);
             }
         }
         return false;
     }
 
     /**
      * Sets up and display a file chooser.
      *
      * @param send DCCTransfer object sending the file
      * @param jc   File chooser
      *
      * @return the return state of the file chooser on popdown:
      * <ul>
      * <li>JFileChooser.CANCEL_OPTION
      * <li>JFileChooser.APPROVE_OPTION
      * <li>JFileChooser.ERROR_OPTION if an error occurs or the dialog is dismissed
      * </ul>
      */
     private int showFileChooser(final DCCTransfer send, final JFileChooser jc) {
         jc.setDialogTitle("Save " + send.getShortFileName() + " As - DMDirc");
         jc.setFileSelectionMode(JFileChooser.FILES_ONLY);
         jc.setMultiSelectionEnabled(false);
         jc.setSelectedFile(new File(send.getFileName()));
         return jc.showSaveDialog(mainWindow);
     }
 
     /**
      * Make the given DCC start listening. This will either call dcc.listen() or
      * dcc.listen(startPort, endPort) depending on config.
      *
      * @param dcc DCC to start listening.
      *
      * @return True if Socket was opened.
      */
     protected boolean listen(final DCC dcc) {
         final boolean usePortRange = config.
                 getOptionBool(getDomain(), "firewall.ports.usePortRange");
         try {
             if (usePortRange) {
                 final int startPort = config.getOptionInt(getDomain(), "firewall.ports.startPort");
                 final int endPort = config.getOptionInt(getDomain(), "firewall.ports.endPort");
                 dcc.listen(startPort, endPort);
             } else {
                 dcc.listen();
             }
             return true;
         } catch (IOException ioe) {
             return false;
         }
     }
 
     @Subscribe
     public void handleServerCtctpEvent(final ServerCtcpEvent event) {
         final boolean autoAccept = config.getOptionBool(getDomain(), "receive.autoaccept");
         final String[] ctcpData = event.getContent().split(" ");
         if (!"DCC".equalsIgnoreCase(event.getType())) {
             return;
         }
         switch (event.getType().toLowerCase()) {
             case "chat":
                 if (ctcpData.length > 3) {
                     handleChat(autoAccept, ctcpData, event.getClient(), event.getConnection());
                 }
                 break;
             case "send":
                 if (ctcpData.length > 3) {
                     handleSend(autoAccept, ctcpData, event.getClient(), event.getConnection());
                 }
                 break;
             case "resume":
             //Fallthrough
             case "accept":
                 if (ctcpData.length > 2) {
                     handleReceive(ctcpData, event.getClient(), event.getConnection());
                 }
                 break;
             default:
                 break;
         }
     }
 
     /**
      * Handles a DCC chat request.
      *
      * @param dontAsk    Don't ask any questions, assume yes.
      * @param ctcpData   CTCP data bits
      * @param client     Client receiving DCC
      * @param connection Connection DCC received on
      */
     private void handleChat(final boolean dontAsk, final String[] ctcpData,
             final ClientInfo client, final Connection connection) {
         final String nickname = client.getNickname();
         if (dontAsk) {
             handleDCCChat(connection.getParser(), nickname, ctcpData);
         } else {
             eventBus.post(new DccChatRequestEvent(connection, nickname));
             new ChatRequestDialog(mainWindow, this, connection, nickname, ctcpData).display();
         }
     }
 
     void handleDCCChat(final Parser parser, final String nickname, final String[] ctcpData) {
         long ipAddress;
         int port;
         try {
             ipAddress = Long.parseLong(ctcpData[2]);
             port = Integer.parseInt(ctcpData[3]);
         } catch (NumberFormatException nfe) {
             return;
         }
         final DCCChat chat = new DCCChat();
         chat.setAddress(ipAddress, port);
         final String myNickname = parser.getLocalClient().getNickname();
         final DCCFrameContainer f = new ChatContainer(
                 getContainer(),
                 chat,
                 config,
                 commandController,
                 "Chat: " + nickname,
                 myNickname,
                 nickname,
                 tabCompleterFactory,
                 messageSinkManager,
                 urlBuilder,
                 eventBus);
         windowManager.addWindow(getContainer(), f);
         f.addLine("DCCChatStarting", nickname, chat.getHost(), chat.getPort());
         chat.connect();
     }
 
     /**
      * Handles a DCC send request.
      *
      * @param dontAsk    Don't ask any questions, assume yes.
      * @param ctcpData   CTCP data bits
      * @param client     Client that received the DCC
      * @param connection Connection the DCC was received on
      */
     private void handleSend(final boolean dontAsk, final String[] ctcpData, final ClientInfo client,
             final Connection connection) {
         final String nickname = client.getNickname();
         final String filename;
         String tmpFilename;
         // Clients tend to put files with spaces in the name in ""
         final StringBuilder filenameBits = new StringBuilder();
         int i;
         final boolean quoted = ctcpData[1].startsWith("\"");
         if (quoted) {
             for (i = 1; i < ctcpData.length; i++) {
                 String bit = ctcpData[i];
                 if (i == 1) {
                     bit = bit.substring(1);
                 }
                 if (bit.endsWith("\"")) {
                     filenameBits.append(" ").append(bit.substring(0, bit.length() - 1));
                     break;
                 } else {
                     filenameBits.append(" ").append(bit);
                 }
             }
             tmpFilename = filenameBits.toString().trim();
         } else {
             tmpFilename = ctcpData[1];
             i = 1;
         }
 
         // Try to remove path names if sent.
         // Change file separatorChar from other OSs first
         if (File.separatorChar == '/') {
             tmpFilename = tmpFilename.replace('\\', File.separatorChar);
         } else {
             tmpFilename = tmpFilename.replace('/', File.separatorChar);
         }
         // Then get just the name of the file.
         filename = new File(tmpFilename).getName();
 
         final String ip = ctcpData[++i];
         final String port = ctcpData[++i];
         long size;
         if (ctcpData.length + 1 > i) {
             try {
                 size = Integer.parseInt(ctcpData[++i]);
             } catch (NumberFormatException nfe) {
                 size = -1;
             }
         } else {
             size = -1;
         }
         final String token = (ctcpData.length - 1 > i
                 && !ctcpData[i + 1].equals("T")) ? ctcpData[++i] : "";
 
         // Ignore incorrect ports, or non-numeric IP/Port
         long ipLong;
         int portInt;
         try {
             portInt = Integer.parseInt(port);
             if (portInt > 65535 || portInt < 0) {
                 return;
             }
             ipLong = Long.parseLong(ip);
         } catch (NumberFormatException nfe) {
             return;
         }
 
         if (DCCTransfer.findByToken(token) == null && !dontAsk) {
             if (!token.isEmpty() && !port.equals("0")) {
                 // This is a reverse DCC Send that we no longer care about.
             } else {
                 eventBus.post(new DccSendRequestEvent(connection, nickname, filename));
                 new SendRequestDialog(mainWindow, this, token, ipLong, portInt, filename, size,
                         nickname, connection).display();
             }
         }
     }
 
     void handleDCCSend(final String token, final long ip, final int port, final String filename,
             final long size, final String nickname, final Parser parser) {
         DCCTransfer send = DCCTransfer.findByToken(token);
         final boolean newSend = send == null;
         if (newSend) {
             send = new DCCTransfer(config.getOptionInt(getDomain(), "send.blocksize"));
             send.setTurbo(config.getOptionBool(getDomain(), "send.forceturbo"));
         } else {
             return;
         }
         send.setAddress(ip, port);
         if (newSend) {
             send.setFileName(filename);
             send.setFileSize(size);
             saveFile(nickname, send, parser, port == 0, token);
         } else {
             send.connect();
         }
     }
 
     /**
      * Handles a DCC chat request.
      *
      * @param ctcpData   CTCP data bits
      * @param client     Client receiving the DCC
      * @param connection Connection the DCC was received on
      */
     private void handleReceive(final String[] ctcpData, final ClientInfo client,
             final Connection connection) {
         final String filename;
         // Clients tend to put files with spaces in the name in ""
         final StringBuilder filenameBits = new StringBuilder();
         int i;
         final boolean quoted = ctcpData[1].startsWith("\"");
         if (quoted) {
             for (i = 1; i < ctcpData.length; i++) {
                 String bit = ctcpData[i];
                 if (i == 1) {
                     bit = bit.substring(1);
                 }
                 if (bit.endsWith("\"")) {
                     filenameBits.append(" ")
                             .append(bit.substring(0, bit.length() - 1));
                     break;
                 } else {
                     filenameBits.append(" ").append(bit);
                 }
             }
             filename = filenameBits.toString().trim();
         } else {
             filename = ctcpData[1];
             i = 1;
         }
 
         final int port;
         final int position;
         try {
             port = Integer.parseInt(ctcpData[++i]);
             position = Integer.parseInt(ctcpData[++i]);
         } catch (NumberFormatException nfe) {
             return;
         }
         final String token = (ctcpData.length - 1 > i) ? " "
                 + ctcpData[++i] : "";
 
         // Now look for a dcc that matches.
         for (DCCTransfer send : DCCTransfer.getTransfers()) {
             if (send.getPort() == port && (new File(send.getFileName()))
                     .getName().equalsIgnoreCase(filename)) {
                 if ((!token.isEmpty() && !send.getToken().isEmpty())
                         && (!token.equals(send.getToken()))) {
                     continue;
                 }
                 final Parser parser = connection.getParser();
                 final String nick = client.getNickname();
                 if (ctcpData[0].equalsIgnoreCase("resume")) {
                     parser.sendCTCP(nick, "DCC", "ACCEPT " + ((quoted) ? "\""
                             + filename + "\"" : filename) + " " + port + " "
                             + send.setFileStart(position) + token);
                 } else {
                     send.setFileStart(position);
                     if (port == 0) {
                         // Reverse dcc
                         if (listen(send)) {
                             if (send.getToken().isEmpty()) {
                                 parser.sendCTCP(nick, "DCC", "SEND "
                                         + ((quoted) ? "\"" + filename
                                         + "\"" : filename) + " "
                                         + DCC.ipToLong(send.getHost())
                                         + " " + send.getPort()
                                         + " " + send.getFileSize());
                             } else {
                                 parser.sendCTCP(nick, "DCC", "SEND "
                                         + ((quoted) ? "\"" + filename
                                         + "\"" : filename)
                                         + " " + DCC.ipToLong(send.getHost())
                                         + " " + send.getPort()
                                         + " " + send.getFileSize() + " "
                                         + send.getToken());
                             }
                         }
                     } else {
                         send.connect();
                     }
                 }
             }
         }
     }
 
     /**
      * Retrieves the container for the placeholder.
      *
      * @since 0.6.4
      * @return This plugin's placeholder container
      */
     public synchronized PlaceholderContainer getContainer() {
         if (container == null) {
             createContainer();
         }
 
         return container;
     }
 
     /**
      * Removes the cached container.
      *
      * @since 0.6.4
      */
     public synchronized void removeContainer() {
         container = null;
     }
 
     /**
      * Create the container window.
      */
     protected void createContainer() {
         container = new PlaceholderContainer(this, config, mainWindow, urlBuilder, eventBus);
         windowManager.addWindow(container);
     }
 
     /**
      * Called when the plugin is loaded.
      */
     public void onLoad() {
         final File dir = new File(config.getOption(getDomain(),
                 "receive.savelocation"));
         if (dir.exists()) {
             if (!dir.isDirectory()) {
                Logger.userError(ErrorLevel.LOW,
                        "Unable to create download dir (file exists instead)");
             }
         } else {
             try {
                 dir.mkdirs();
                 dir.createNewFile();
             } catch (IOException ex) {
                Logger.userError(ErrorLevel.LOW,
                        "Unable to create download dir");
             }
         }
 
         eventBus.register(this);
     }
 
     /**
      * Called when this plugin is Unloaded.
      */
     public synchronized void onUnload() {
         eventBus.unregister(this);
         if (container != null) {
             container.close();
         }
     }
 
     /**
      * Get the IP Address we should send as our listening IP.
      *
      * @param parser Parser the IRC Parser where this dcc is initiated
      *
      * @return The IP Address we should send as our listening IP.
      */
     public String getListenIP(final Parser parser) {
         final String configIP = config.getOption(getDomain(), "firewall.ip");
         if (!configIP.isEmpty()) {
             try {
                 return InetAddress.getByName(configIP).getHostAddress();
             } catch (UnknownHostException ex) { //NOPMD - handled below
                 //Continue below
             }
         }
         if (parser != null) {
             final String myHost = parser.getLocalClient().getHostname();
             if (!myHost.isEmpty()) {
                 try {
                     return InetAddress.getByName(myHost).getHostAddress();
                 } catch (UnknownHostException e) { //NOPMD - handled below
                     //Continue below
                 }
             }
         }
         try {
             return InetAddress.getLocalHost().getHostAddress();
         } catch (UnknownHostException e) {
             // This is almost certainly not what we want, but we can't work out
             // the right one.
             return "127.0.0.1"; //NOPMD
         }
     }
 
 }
