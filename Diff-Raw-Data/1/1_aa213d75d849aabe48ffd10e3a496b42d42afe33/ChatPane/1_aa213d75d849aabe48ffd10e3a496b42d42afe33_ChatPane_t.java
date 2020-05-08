 /*
  * ChatPane.java
  *
  * Created on Apr 5, 2009, 2:13:23 PM
  *
  * This file is a part of Shoddy Battle.
  * Copyright (C) 2009  Catherine Fitzpatrick and Benjamin Gwin
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, visit the Free Software Foundation, Inc.
  * online at http://gnu.org.
  */
 
 package shoddybattleclient;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.JScrollPane;
 import shoddybattleclient.LobbyWindow.Channel;
 import shoddybattleclient.utils.CloseableTabbedPane.CloseableTab;
 import shoddybattleclient.utils.HTMLPane;
 import shoddybattleclient.utils.Text;
 
 /**
  *
  * @author ben
  */
 public class ChatPane extends javax.swing.JPanel implements CloseableTab {
 
     public static class CommandException extends Exception {
         public CommandException(String message) {
             super(message);
         }
     }
 
     private static boolean m_logging = Preference.getAutosaveChatLogs();
 
     private static final GregorianCalendar CALENDAR = new GregorianCalendar();
 
     private HTMLPane m_chatPane;
     private LobbyWindow m_lobby;
     private String m_name;
     private LobbyWindow.Channel m_channel;
     private String m_sub;
     private int m_tabCount = 0;
     //hour of the last message received
     private int m_lastTime = CALENDAR.get(Calendar.HOUR);
     private PrintWriter m_log;
 
     public LobbyWindow getLobby() {
         return m_lobby;
     }
 
     /** Creates new form ChatPane */
     public ChatPane(LobbyWindow.Channel c, LobbyWindow lobby, String name) {
         m_channel = c;
         m_lobby = lobby;
         m_name = name;
         initComponents();
         txtChat.setFocusTraversalKeysEnabled(false);
         m_chatPane = new HTMLPane();
         scrollChat.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         scrollChat.add(m_chatPane);
         scrollChat.setViewportView(m_chatPane);
 
         m_chatPane.addKeyListener(new KeyListener() {
            public void keyReleased(KeyEvent evt) {}
            public void keyPressed(KeyEvent evt) {}
            public void keyTyped(KeyEvent evt) {
                txtChat.requestFocusInWindow();
                txtChat.dispatchEvent(evt);
            }
         });
         initLogging();
     }
 
     private File getLogDir() {
         String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
         String dir = Preference.getLogDirectory() + "chat";
         return new File(dir, date);
     }
 
     private void initLogging() {
         if (m_channel.getType() == Channel.TYPE_BATTLE) return;
        if (!m_logging) return;
         File dir = getLogDir();
         dir.mkdirs();
         File f = new File(dir, m_channel.getName() + ".txt");
         FileWriter out = null;
         try {
             out = new FileWriter(f, true);
             m_log = new PrintWriter(out, true);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public LobbyWindow.Channel getChannel() {
         return m_channel;
     }
 
     private void parseCommand(String command, String args)
             throws CommandException {
         command = command.toLowerCase();
         if ("mode".equals(command)) {
             int idx = args.indexOf(' ');
             String action, cmd;
             if (idx == -1) {
                 action = args;
                 cmd = "";
             } else {
                 action = args.substring(0, idx);
                 cmd = args.substring(idx + 1);
             }
             parseMode(action.toLowerCase(), cmd);
         } else if ("ignore".equals(command)) {
             if ("".equals(args.trim())) throw new CommandException("Usage: /ignore name");
             boolean success = Preference.ignore(args);
             if (!success) {
                 addMessage(null, "You are already ignoring " + args);
             } else {
                 addMessage(null, "You are now ignoring " + args);
             }
         } else if ("unignore".equals(command)) {
             if ("".equals(args.trim())) throw new CommandException("Usage: /unignore name");
             boolean success = Preference.unignore(args);
             if (!success) {
                 addMessage(null, "You are not ignoring " + args);
             } else {
                 addMessage(null, "You are no longer ignoring " + args);
             }
         } else if ("ban".equals(command) ||
                 "gban".equals(command) ||
                 "ipban".equals(command)) {
             String[] parts = args.split(",", 2);
             int channel = "ban".equals(command) ? m_channel.getId() : -1;
             boolean ipBan = "ipban".equals(command);
             String user;
             long d;
             if (parts.length == 2) {
                 user = parts[0].trim();
                 String dateFormat = parts[1].trim();
                 d = parseDateFormat(dateFormat);
             } else if (parts.length == 1) {
                 user = parts[0].trim();
                 BanDialog bd = new BanDialog(user);
                 bd.setVisible(true);
                 d = bd.getBanLength();
             } else {
                 throw new CommandException(
                         "/[ban|gban|ipban] user, [[*y][*d][*h][*m]]");
             }
             if (d == 0) return;
             m_lobby.getLink().sendBanMessage(channel, user, d, ipBan);
         } else if ("gunban".equals(command)) {
             String user = args.trim();
             m_lobby.getLink().sendBanMessage(-1, user, -1, false);
         } else if ("unban".equals(command)) {
             String user = args.trim();
             m_lobby.getLink().sendBanMessage(m_channel.getId(), user, -1, false);
         } else if ("kick".equals(command)) {
             String user = args.trim();
             m_lobby.getLink().sendBanMessage(m_channel.getId(), user, 0, false);
         } else if ("kill".equals(command)) {
             String user = args.trim();
             m_lobby.getLink().sendBanMessage(-1, user, 0, false);
         } else if ("lookup".equals(command)) {
             String user = args.trim();
             m_lobby.getLink().requestUserLookup(user);
         } else if ("msg".equals(command)) {
             String user = args.trim();
             m_lobby.openPrivateMessage(user, true);
         }
     }
 
     private int getMode(char c) {
         switch (c) {
             case 'a':
                 return 0;
             case 'o':
                 return 1;
             case 'v':
                 return 2;
             case 'b':
                 return 3;
             case 'm':
                 return 4;
             case 'i':
                 return 5;
             // todo: idle?
         }
         throw new InternalError();
     }
 
     private void parseMode(String action, String users) throws CommandException {
         if ("".equals(action) || "help".equals(action)) {
             throw new CommandException(
                     "Usage: /mode +a/o/v/b/m/i [user1[,user2,...]]");
         }
         char char1 = action.charAt(0);
         if ((char1 != '+') && (char1 != '-')) {
             throw new CommandException("Try '/mode help' for usage");
         }
         boolean add = (char1 == '+');
         action = action.substring(1);
         if (action.length() == 1) {
             String user = users;
             char c = action.charAt(0);
             switch (c) {
                 case 'a':
                 case 'o':
                 case 'v':
                 case 'b':
                 case 'm':
                 case 'i':
                     int mode = getMode(c);
                     m_lobby.getLink().updateMode(m_channel.getId(),
                             user, mode, add);
                     break;
                 default:
                     throw new CommandException("Invalid command: " + action);
 
             }
         } else {
             String[] args = users.split(",");
             for (int i = 0; i < action.length(); i++) {
                 String user;
                 if (i >= args.length) {
                     user = "";
                 } else {
                     user = args[i];
                 }
                 String pm = add ? "+" : "-";
                 parseMode(pm + action.substring(i, i + 1), user.trim());
             }
         }
     }
 
     private static long parseDateFormat(String s) throws CommandException {
         Pattern p = Pattern.compile("((\\d+)y)?((\\d+)d)?((\\d+)h)?((\\d+)m?)?");
         Matcher matcher = p.matcher(s);
         long time = 0;
         if (matcher.matches()) {
             String y = matcher.group(2);
             String d = matcher.group(4);
             String h = matcher.group(6);
             String m = matcher.group(8);
             try {
                 if (y != null) time += Integer.parseInt(y) * 31536000;
                 if (d != null) time += Integer.parseInt(d) * 86400;
                 if (h != null) time += Integer.parseInt(h) * 3600;
                 if (m != null) time += Integer.parseInt(m) * 60;
             } catch (Exception e) {
                 throw new CommandException("Time format [_y][_d][_h][_m]");
             }
         }
         return (int)time;
     }
 
     public void addMessage(String user, String message) {
         addMessage(user, message, true);
     }
 
     public void addMessage(String user, String message, boolean encode) {
         m_chatPane.addMessage(user, message, encode);
 
         //open new log file if it is a new day
         int newTime = CALENDAR.get(Calendar.HOUR);
         if (newTime < m_lastTime) {
             initLogging();
         }
         m_lastTime = newTime;
         
         if (m_logging && (m_log != null)) {
             if (!encode) message = Text.stripTags(message);
             String out = message;
             if (user != null) {
                 user = Text.stripTags(user);
                 out = user + ": " + message;
             }
             String timestamp = new SimpleDateFormat("[hh:mm:ss] ").format(new Date());
             out = timestamp + out;
             m_log.println(out);
         }
     }
 
     public void sendMessage(String message) throws CommandException {
         message = message.trim();
         if (message.equals("")) {
             return;
         }
 
         if (message.indexOf('/') == 0) {
             int idx = message.indexOf(' ');
             String command, args;
             if (idx != -1) {
                 command = message.substring(1, idx);
                 args = message.substring(idx + 1);
             } else {
                 command = message.substring(1);
                 args = "";
             }
             parseCommand(command.toLowerCase(), args);
             return;
         }
 
         if (m_channel.getType() == Channel.TYPE_PRIVATE_MESSAGE) {
             m_lobby.getLink().sendPrivateMessage(m_channel.getName(), message);
         } else {
             m_lobby.getLink().sendChannelMessage(m_channel.getId(), message);
         }
     }
 
     public JScrollPane getPane() {
         return scrollChat;
     }
 
     public HTMLPane getChat() {
         return m_chatPane;
     }
 
     @Override
     public boolean informClosed() {
         if (m_channel.getType() == Channel.TYPE_PRIVATE_MESSAGE) {
             m_lobby.closePrivateMessage(m_channel.getName());
         } else {
             m_lobby.getLink().partChannel(m_channel.getId());
         }
         return true;
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         txtChat = new javax.swing.JTextField();
         scrollChat = new javax.swing.JScrollPane();
 
         setBackground(new java.awt.Color(244, 242, 242));
         setOpaque(false);
 
         txtChat.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 txtChatFocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 txtChatFocusLost(evt);
             }
         });
         txtChat.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtChatKeyReleased(evt);
             }
         });
 
         scrollChat.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, txtChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, scrollChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .add(scrollChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(txtChat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void txtChatFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtChatFocusGained
 
 }//GEN-LAST:event_txtChatFocusGained
 
     private void txtChatFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtChatFocusLost
 
     }//GEN-LAST:event_txtChatFocusLost
 
     private void txtChatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtChatKeyReleased
         String msg = txtChat.getText();
 
         if (evt.getKeyCode() == KeyEvent.VK_TAB) {
             if ("".equals(msg)) return;
             int idx = msg.lastIndexOf(" ");
             if (m_tabCount == 0) {
                 m_sub = msg.substring(idx + 1);
                 if ("".equals(m_sub)) return;
             }
             List<String> names = m_lobby.autocompleteUser(m_sub);
             Collections.sort(names, new Comparator<String>() {
                 public int compare(String o1, String o2) {
                     return o1.compareToIgnoreCase(o2);
                 }
             });
             int size = names.size();
             if (size == 0) return;
             int index = m_tabCount;
             while (index >= size) {
                 index -= size;
             }
             String newStr = names.get(index);
             if (newStr != null){
                 msg = msg.substring(0, idx + 1) + newStr;
                 txtChat.setText(msg);
             }
             m_tabCount++;
         } else {
             m_tabCount = 0;
             if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                 try {
                     sendMessage(msg);
                 } catch (CommandException e) {
                     String str = "<font class='help'>" + e.getMessage() + "</font>";
                     addMessage(null, str, false);
                 }
                 txtChat.setText(null);
             }
         }
     }//GEN-LAST:event_txtChatKeyReleased
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JScrollPane scrollChat;
     private javax.swing.JTextField txtChat;
     // End of variables declaration//GEN-END:variables
 
 }
