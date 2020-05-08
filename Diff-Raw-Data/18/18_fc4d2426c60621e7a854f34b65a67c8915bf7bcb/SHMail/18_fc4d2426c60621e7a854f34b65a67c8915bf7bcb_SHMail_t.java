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
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import org.gjt.fredde.util.net.*;
 import org.gjt.fredde.util.gui.MsgDialog;
 import org.gjt.fredde.yamm.gui.pDialog;
 import org.gjt.fredde.yamm.mail.*;
 
 /**
  * Sends and gets mail
  */
 public class SHMail extends Thread {
 
    /** Properties for smtpserver etc */
   static protected Properties props = new Properties();
 
   JButton knappen;
   JFrame frame;
 
   /**
    * Disables the send/get-button and inits some stuff
    * @param frame1 the frame that will be used for error-msg etc.
    * @param name the name of this thread.
    * @param knapp the button to disable.
    */
   public SHMail(JFrame frame1, String name, JButton knapp) {
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
     pDialog pd = new pDialog("Status");
 
     File files[] = new File(System.getProperty("user.home") + "/.yamm/servers/").listFiles();
 
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
       boolean del;
       if(props.getProperty("delete", "false").equals("true")) del = true;
       else del = false;
 
       if(type != null && server != null && username != null && password != null) {
         if(type.equals("pop3")) {
           pd.setString("contacting server: " + server);
 
           try { 
             Pop3 pop = new Pop3(username, password, server, System.getProperty("user.home") + "/.yamm/boxes/.filter");
             int messages = pop.getMessageCount();
 
             for(int j = 1; j<=messages;j++) {
               pd.setString("Getting mail " + j + " of " + messages);
 
               pd.progress(100 - ( 100 * ( ( messages - i ) / messages ) ) );
               pop.getMessage(j);
               if(del) pop.deleteMessage(j);
             }
             pop.closeConnection();
           }
           catch (IOException ioe) { new MsgDialog(frame, "Error!", ioe.toString()); }
         }
         else new MsgDialog(frame, "Error!", "Server isn't configurated correctly!");
       }
     }
 
    /* load the config */
    try {
      InputStream in = new FileInputStream(System.getProperty("user.home") + "/.yamm/.config");
      props = new Properties();
      props.load(in);
      in.close();
     } catch (IOException propsioe) { System.err.println(propsioe); }
 
     if(props.getProperty("smtpserver") != null && Mailbox.hasMail(System.getProperty("user.home") + "/.yamm/boxes/outbox")) {
       pd.setString("Sending mails...");
       pd.progress(0);
       try { 
         Smtp smtp = new Smtp(props.getProperty("smtpserver"));
         BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(System.getProperty("user.home") + "/.yamm/boxes/outbox")));
         String temp = null, from2 = null, to2 = null;
         int i = 1;
 
         for(;;) {
           temp = in.readLine();
 
           if(temp == null) break;
 
           else if(temp.startsWith("From:")) {
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
             PrintWriter out = smtp.getOutputStream();
             out.println(from2 + "\n" + to2 + "\n" + temp);
 
             for(;;) {
               temp = in.readLine();
 
               if(temp == null) break;
 
               else if(temp.equals(".")) {
                 smtp.sendMessage();
                 pd.setString("Sending mail " + i);
                 i++;
                 break;
               }
               out.println(temp);
             }
           }
         }
         in.close();
         File file = new File(System.getProperty("user.home") + "/.yamm/boxes/outbox");
         file.delete();
         file.createNewFile();
         smtp.closeConnection();
 
       }
       catch(IOException ioe) { new MsgDialog(frame, "Error!", ioe.toString()); }
     }
     pd.setString("done!");
     pd.progress(100);
 
    if(Mailbox.hasMail(System.getProperty("user.home") + "/.yamm/boxes/.filter")) {
      pd.setString("Applying filters...");
      pd.progress(0);
 
      try { new Filter(); }
      catch (IOException ioe) { new MsgDialog(frame, "Error!", ioe.toString()); }
 
      pd.setString("done!");
      pd.progress(100);
    }
    
     knappen.setEnabled(true);
     pd.dispose();
   }
 }
   
