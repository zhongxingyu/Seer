 /* HTMLPane.java
  *
  * Created April 7, 2009
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
 
 package shoddybattleclient.utils;
 import java.awt.Color;
 import javax.swing.*;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.html.HTML;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLEditorKit;
 import javax.swing.text.html.StyleSheet;
 import shoddybattleclient.LobbyWindow;
 
 /**
  *
  * @author ben
  */
 public class HTMLPane extends JTextPane {
 
     private static final int MAXIMUM_LINES = 150;
 
     private String m_user;
     private int m_lines = 0;
     private int m_actualLines = 0;
 
     public HTMLPane(String name) {
         super();
         m_user = name;
         setContentType("text/html");
         setEditable(false);
         setBackground(Color.WHITE);
         setOpaque(true);
         HTMLEditorKit kit = new HTMLEditorKit();
         StyleSheet css = new StyleSheet();
         css.importStyleSheet(HTMLPane.class.getResource("../resources/main.css"));
         kit.setStyleSheet(css);
         setEditorKit(kit);
 
         this.addHyperlinkListener(new HyperlinkListener() {
             public void hyperlinkUpdate(HyperlinkEvent e) {
                 if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                     LobbyWindow.viewWebPage(e.getURL());
                 }
             }
         });
     }
 
     /**
      * Encode HTML entities.
      * Copied from www.owasp.org.
      */
     public static String htmlEntityEncode(String s) {
         StringBuffer buf = new StringBuffer();
         for (int i = 0; i < s.length(); i++) {
             char c = s.charAt(i);
             if ((c >= 'a' && c <= 'z') ||
                     (c >= 'A' && c <= 'Z') ||
                     (c >= '0' && c <= '9')) {
                 buf.append(c);
             } else {
                 buf.append("&#" + (int)c + ";");
             }
         }
         return buf.toString();
     }
 
     public void addMessage(String user, String message) {
         if (message == null) return;
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
         message = htmlEntityEncode(message);
         StringBuffer buffer = new StringBuffer();
         //Date d = new Date();
         //SimpleDateFormat f = new SimpleDateFormat("H:mm:ss");
         //buffer.append("[");
         //buffer.append(f.format(d));
         //buffer.append("] ");
         if (user != null) {
             String style = user.equals(m_user) ? "self" : "others";
             buffer.append("<font class=\"");
             buffer.append(style);
             buffer.append("\">");
             buffer.append(htmlEntityEncode(user));
             buffer.append("</font>");
             buffer.append(": ");
         }
         buffer.append(message);
         String msg = new String(buffer);
         msg = msg.replaceAll("&#32;", " ")
                         .replaceAll("\\b([^ ]*&#58;&#47;&#47;[^ ]+)",
                             "<a href=\"$1\">$1</a>");
         msg = "<div>" + msg + "</div>";
         HTMLDocument doc = (HTMLDocument)getDocument();
         HTMLEditorKit kit = (HTMLEditorKit)getEditorKit();
         try {
             kit.insertHTML(doc, doc.getLength(), msg, 1, 0, HTML.Tag.DIV);
             if (++m_lines > MAXIMUM_LINES) {
                 int position = 0;
                 int index;
                 while (true) {
                     index = doc.getText(position, doc.getLength()).indexOf("\n");
                     if (index != 0) {
                         position += index;
                         break;
                     } else {
                         position += 1;
                     }
                 }
                 doc.remove(0, position);
             }
         } catch (Exception e) {
             
         }
         
         //scroll only if we are already at the bottom
         JScrollBar vbar = ((JScrollPane)this.getParent().getParent()).getVerticalScrollBar();
         if ((vbar.getValue() + vbar.getVisibleAmount()) == vbar.getMaximum()) {
             this.setCaretPosition(doc.getLength());
         }
     }
 
     private void clear() {
         setText("");
         m_lines = 0;
     }
 
     private void parseCommand(String command, String args) {
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
         }
     }
 
     private void parseMode(String action, String users) {
        if ("".equals(action) || "help".equals(action)) {
             addMessage(null, "Usage: /mode +q/a/o/h/v/b/m/i user1[,user2,...]");
             return;
         }
         char char1 = action.charAt(0);
         if ((char1 != '+') && (char1 != '-')) {
             addMessage(null, "Try '/mode help' for usage");
             return;
         }
         boolean add = (char1 == '+');
         action = action.substring(1);
         if (action.length() == 1) {
             String user = users;
             String verb = add ? "Adding" : "Removing";
             System.out.println(verb + " " + action + " to " + user);
             switch (action.charAt(0)) {
                 case 'q':
                     break;
                 case 'a':
                     break;
                 case 'o':
                     break;
                 case 'h':
                     break;
                 case 'v':
                     break;
                 case 'b':
                     break;
                 case 'm':
                     break;
                 case 'i':
                     break;
                 default:
                     addMessage(null, "Invalid command: " + action);
 
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
 
     public static void main(String[] args) {
         JFrame frame = new JFrame("test htmlpane");
         frame.setSize(500, 300);
         frame.setLayout(new java.awt.GridLayout(1, 1));
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         final HTMLPane pane = new HTMLPane("Ben");
         JScrollPane scrollPane = new JScrollPane();
         scrollPane.setViewportView(pane);
         frame.add(scrollPane);
         pane.addMessage("Ben", "I am a polymath");
         pane.addMessage("Catherine", "I agree wholeheartedly");
         pane.addMessage("Catherine", "check out http://cathyisnotapolymath.com");
         frame.setVisible(true);
         /*new Thread(new Runnable() {
             public void run() {
                 for (int i = 0; ; i++) {
                     pane.addMessage("spammer", String.valueOf(i));
                     synchronized (this) {
                         try {
                             wait(100);
                         } catch (InterruptedException ex) {
 
                         }
                     }
                 }
             }
         }).run();*/
         
     }
 }
