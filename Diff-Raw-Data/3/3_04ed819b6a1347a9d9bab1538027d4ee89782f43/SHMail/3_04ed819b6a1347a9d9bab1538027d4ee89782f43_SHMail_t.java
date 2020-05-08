 /*  SHMail.java - sends and gets mail
  *  Copyright (C) 1999 Fredrik Ehnbom
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package org.gjt.fredde.yamm;
 
 import java.io.*;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import org.gjt.fredde.util.net.*;
 import org.gjt.fredde.util.gui.MsgDialog;
 import org.gjt.fredde.yamm.mail.*;
 import org.gjt.fredde.yamm.YAMM;
 
 /**
  * Sends and gets mail
  */
 public class SHMail extends Thread {
 
    /** Properties for smtpserver etc */
   static protected Properties props = new Properties();
 
   static protected boolean sent = false;
 
   JButton knappen;
   YAMM frame;
 
   /**
    * Disables the send/get-button and inits some stuff
    * @param frame1 the frame that will be used for error-msg etc.
    * @param name the name of this thread.
    * @param knapp the button to disable.
    */
   public SHMail(YAMM frame1, String name, JButton knapp) {
     super(name);
     knappen = knapp;
     knappen.setEnabled(false);
     frame = frame1;
   }
 
   /**
    * Creates a new progress-dialog, checks for server configuration-files and
    * connects to the servers with a config-file.
    */
   public void run() {
     frame.status.progress(0);
     frame.status.setStatus("");
 
     File files[] = new File(YAMM.home + "/servers/").listFiles();
 
     for(int i=0;i<files.length;i++) {
       /* load the config */
 
       try {
         InputStream in = new FileInputStream(files[i]);
         props.load(in);
         in.close();
       } catch (IOException propsioe) { System.err.println(propsioe); }
 
 
       String type = props.getProperty("type");
       String server = props.getProperty("server");
       String username = props.getProperty("username");
       String password = YAMM.decrypt(props.getProperty("password"));
       boolean del = false;
       if(YAMM.getProperty("delete", "false").equals("true")) del = true;
       if(YAMM.getProperty("sentbox", "false").equals("true")) sent = true;
 
       if(type != null && server != null && username != null && password != null) {
         if(type.equals("pop3")) {
           Object[] argstmp = {server};
           frame.status.setStatus(YAMM.getString("server.contact", argstmp));
 
           try { 
             Pop3 pop = new Pop3(username, password, server, YAMM.home + "/boxes/.filter");
             int messages = pop.getMessageCount();
 
             for(int j = 1; j<=messages;j++) {
               Object[] args = {"" + j, "" + messages};
               frame.status.setStatus(YAMM.getString("server.get", args));
 
               frame.status.progress(100-((100*messages-100*(j-1))/messages));
               pop.getMessage(j);
               if(del) pop.deleteMessage(j);
             }
             frame.status.setStatus(YAMM.getString("msg.done"));
             frame.status.progress(100);
             pop.closeConnection();
           }
           catch (IOException ioe) {
             Object[] args = {ioe.toString()}; 
             new MsgDialog(frame, YAMM.getString("msg.error"),
                                  YAMM.getString("msg.exception", args)); 
           }
         }
         else new MsgDialog(frame, YAMM.getString("msg.error"), 
                                   YAMM.getString("server.bad"));
       }
     }
 
    /* load the config */
 /*
    try {
      InputStream in = new FileInputStream(YAMM.home + "/.config");
      props = new Properties();
      props.load(in);
      in.close();
     } catch (IOException propsioe) { System.err.println(propsioe); }
 */
 
     if(YAMM.getProperty("smtpserver") != null && Mailbox.hasMail(YAMM.home + "/boxes/" + YAMM.getString("box.outbox"))) {
       Object[] argstmp = {""};
       frame.status.setStatus(YAMM.getString("server.send", argstmp));
       frame.status.progress(0);
       try { 
         Smtp smtp = new Smtp(YAMM.getProperty("smtpserver"));
         BufferedReader in = new BufferedReader(
                             new InputStreamReader(
                             new FileInputStream(YAMM.home + "/boxes/" +
                                                 YAMM.getString("box.outbox"))));
         PrintWriter out = null;
 
         if(sent) { 
           out = new PrintWriter(
                 new BufferedOutputStream(
                new FileOutputStream(YAMM.home + YAMM.sep + "boxes" + YAMM.sep +
                                     YAMM.getString("box.sent"), true)));
         }
 
         String temp = null, from2 = null, to2 = null;
         int i = 1;
 
         for(;;) {
           temp = in.readLine();
 
 
           if(temp == null) break;
           if(sent) out.println(temp);
 
           if(temp.startsWith("From:")) {
             smtp.from(temp.substring(temp.indexOf("<") + 1, temp.indexOf(">")));
             from2 = temp;
           }
 
           else if(temp.startsWith("To:")) {
             to2 = temp;
             if(temp.indexOf(",") == -1) smtp.to(temp.substring(4, temp.length()));
 
             else {
               temp.trim();
               temp = temp.substring(4, temp.length());
               while(temp.endsWith(",")) {
                 smtp.to(temp.substring(0, temp.length()-1));
                 temp = in.readLine().trim();
                 to2 += "\n      " + temp;
               }
               smtp.to(temp.substring(0, temp.length()-1));
               to2 += "\n      " + temp;
               temp = in.readLine();
             }
           }
 
           else if(temp.startsWith("Subject:")) {
             PrintWriter mail = smtp.getOutputStream();
             mail.println(from2 + "\n" + to2 + "\n" + temp);
 
             for(;;) {
               temp = in.readLine();
 
               if(temp == null) break;
 
               if(sent) out.println(temp);
 
               if(temp.equals(".")) {
                 smtp.sendMessage();
                 Object[] args = {"" + i};
                 frame.status.setStatus(YAMM.getString("server.send", args));
                 
                 i++;
                 break;
               }
               mail.println(temp);
             }
           }
         }
         in.close();
         if(sent) out.close();
         File file = new File(YAMM.home + "/boxes/" + YAMM.getString("box.outbox"));
         file.delete();
         file.createNewFile();
         smtp.closeConnection();
 
       }
       catch(IOException ioe) { 
         Object[] args = {ioe.toString()};
         new MsgDialog(frame, YAMM.getString("msg.error"),
                              YAMM.getString("msg.exception", args)); 
       }
     }
     frame.status.setStatus(YAMM.getString("msg.done"));
     frame.status.progress(100);
 
     if(Mailbox.hasMail(YAMM.home + "/boxes/.filter")) {
       frame.status.setStatus(YAMM.getString("server.filter"));
       frame.status.progress(0);
 
       try { new Filter(); }
       catch (IOException ioe) { 
         Object[] args = {ioe.toString()};
         new MsgDialog(frame, YAMM.getString("msg.error"),
                              YAMM.getString("msg.exception", args)); 
       }
 
       frame.status.setStatus(YAMM.getString("msg.done"));
       frame.status.progress(100);
     }
     frame.status.setStatus("");
     frame.status.progress(0); 
     knappen.setEnabled(true);
   }
 }
