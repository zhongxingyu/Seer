 /* 
  * Leks13
  * GPL v3
  */
 package ru.leks13.jabbertimer;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.StringTokenizer;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Presence;
 
 public class UserCommand {
 
     public static String muc;
 
     public static Boolean doUserCommand(String command, String jid, String admin) throws XMPPException, IOException, NumberFormatException, ClassNotFoundException, SQLException, ParseException {
         Boolean ans = false;
         String msg = null;
 
         if (command.startsWith("!report") && !ans) {
             command = new StringBuffer(command).delete(0, 7).toString();
             msg = command + " - " + jid;
             XmppNet.sendMessage(admin, msg);
             ans = true;
         }
 
         if (command.startsWith("!list") && !ans) {
             java.util.Date today = new java.util.Date();
             long time = (System.currentTimeMillis());
             msg = Sql.listOfTimer(jid);
             XmppNet.sendMessage(jid, msg);
             ans = true;
         }
 
         if (command.startsWith("!remind") && !ans) {
             command = command.replaceAll("!remind ", "");
             java.util.Date today = new java.util.Date();
             long time = (System.currentTimeMillis());
             StringTokenizer st = new StringTokenizer(command, "@");
             String noteU = "";
 
             while (st.hasMoreTokens()) {
                 command = st.nextToken();
                 if (!st.hasMoreElements()) {
                     NullNoteEx(jid);
                 }
                 noteU = st.nextToken();
             }
             SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
             Date dt = null;
             try {
                 dt = sdf.parse(command);
             } catch (ParseException e) {
                 msg = "Wrong date!";
                 XmppNet.sendMessage(jid, msg);
                 ans = true;
             }
 
             long dt1 = dt.getTime() / 1000;
             if (!ans) {
                 if (dt1 > (time / 1000)) {
                     Sql.add(dt1, jid, Main.id, noteU);
                     msg = "Timer is set!";
                 } else {
                     msg = "Wrong date";
                 }
             }
             XmppNet.sendMessage(jid, msg);
             ans = true;
             Main.id++;
         }
 
         if (command.startsWith("!note") && !ans) {
             command = command.replaceAll("!note ", "");
             Long time = 0L;
             if (!ans) {
                 Sql.add(time, jid, Main.id, command);
             }
             ans = true;
             msg = "Writed!";
             XmppNet.sendMessage(jid, msg);
             Main.id++;
 
         }
 
         if (command.startsWith("!my") && !ans) {
             msg = Sql.listOfNote(jid);
             ans = true;
             XmppNet.sendMessage(jid, msg);
             Main.id++;
         }
 
         if (command.startsWith("!del") && !ans) {
             command = command.replaceAll("!del #", "");
             if (!ans) {
                 Sql.deleteNote(jid, command);
             }
             ans = true;
             msg = "Command complete";
             XmppNet.sendMessage(jid, msg);
             Main.id++;
         }
 
         try {
             if (command.startsWith("!timer") && !ans) {
                 command = command.replaceAll("!timer ", "");
                 java.util.Date today = new java.util.Date();
                 long time = (System.currentTimeMillis());
                 StringTokenizer st = new StringTokenizer(command, "@");
                 String noteU = "";
                 while (st.hasMoreTokens()) {
                     command = st.nextToken();
                     if (!st.hasMoreElements()) {
                         NullNoteEx(jid);
                     }
                     noteU = st.nextToken();
                 }
                 if (Long.parseLong(command) < 1 || Long.parseLong(command) > 120) {
                     throw new NumberFormatException();
                 }
                 long timeDo = ((time + Long.parseLong(command) * 1000 * 60) / 1000L);
                 if (!ans) {
                     Sql.add(timeDo, jid, Main.id, noteU);
                 }
                 ans = true;
                 msg = "Timer is set!";
                 XmppNet.sendMessage(jid, msg);
                 Main.id++;
 
             }
         } catch (NumberFormatException ex1) {
             ans = true;
             XmppNet.sendMessage(jid, "Wrong timer interval \n"
                     + "The permissible range of 1 to 120 minutes.");
         }
 
 
         if (command.startsWith("!off") && !ans && jid.contains(admin)) {
             XmppNet.disconnect();
             ans = true;
         }
 
 
         if (command.startsWith("!roster") && !ans && jid.contains(admin)) {
             msg = XmppNet.getXmppRoster();
             XmppNet.sendMessage(jid, msg);
             ans = true;
 
         }
 
         if (command.startsWith("!status") && !ans && jid.contains(admin)) {
             command = new StringBuffer(command).delete(0, 8).toString();
             String status = command;
             Presence presence = new Presence(Presence.Type.available);
             presence.setStatus(status);
             XmppNet.connection.sendPacket(presence);
             ans = true;
         }
 
         if (command.equals("!help")) {
             msg = "Commands: \n"
                     + "!report <message> - send <message> to admin \n \n"
                    + "!remind <dd.mm.yyy HH:mm>@<remind> - set a reminder on this date \n"
                     + " For example  !remind 03.10.2012 18:51@Hello \n \n"
                     + "!timer <minutes>@<remind> - set timer. \n"
                     + "  For example '!timer 2@Hello' send after 2 minutes 'Hello' \n \n"
                     + "!list - list of installed timers \n \n"
                     + "Notes: \n"
                     + "!my - list of notes \n"
                     + "!note 'text' - write note \n"
                     + "!del #1234567890 - delete note with number #1234567890 \n";
             XmppNet.sendMessage(jid, msg);
             ans = true;
         }
 
         return ans;
     }
 
     private static void NullNoteEx(String jid) throws XMPPException {
         XmppNet.sendMessage(jid, "Blank or invalid string reminder!");
     }
 }
