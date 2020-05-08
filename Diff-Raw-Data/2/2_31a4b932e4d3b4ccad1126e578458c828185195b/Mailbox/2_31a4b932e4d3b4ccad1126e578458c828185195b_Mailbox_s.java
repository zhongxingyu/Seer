 /*  Mailbox.java - box handling system
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
 
 package org.gjt.fredde.yamm.mail;
 
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 import java.awt.Graphics;
 import java.io.*;
 import java.awt.print.Book;
 import java.util.*;
 import javax.swing.JTextArea;
 import org.gjt.fredde.util.gui.MsgDialog;
 import org.gjt.fredde.yamm.YAMM;
 
 /**
  * A class that handels messages and information about messages
  */
 public class Mailbox {
 
   /**
    * puts the source code in a JTextArea.
    * @param whichBox Which box the message to view is in
    * @param whichmail Which mail to view
    * @param jtarea Which JTextArea to append the source to
    */
   public static void viewSource(String whichBox, int whichmail, JTextArea jtarea) {
     try {
       BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichBox)));
       int i = 0;
       String temp;
 
       for(;;) {
         temp = in.readLine();
       
         if(temp == null) break;
 
         if(whichmail != i && temp.equals(".")) i++;
 	else if(whichmail == i) {
           if(!temp.equals(".")) jtarea.append(temp + "\n");
 	  else  break;
         }
       }
       in.close();
     }
     catch (IOException ioe) { System.err.println(ioe); }
   }
 
   /**
    * Creates a list with the mails in this box.
    * @param whichBox Which box to get messages from.
    * @param mailList The vector that should contain the information
    */
   public static void createList(String whichBox, Vector mailList) {
     String subject = null, from = null, date = null, status = null;
 
     String temp = null;
     mailList.clear(); // = new Vector();
     Vector vec1 = new Vector();
     int makeItem = 0;
 
     int i = 0;
 
     try {
       BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichBox)));
 
       for(;;) {
         temp = in.readLine();
       
         if(temp == null)
           break;
 
         else if(temp.startsWith("From:") && from == null) {
           from = temp.substring(6, temp.length());
           makeItem++;
         }
 
         else if(temp.startsWith("Subject:") && subject == null) {
           subject = temp.substring(8, temp.length());
           makeItem++;
         }
         else if(temp.startsWith("YAMM-Status:") && status == null) {
           status = temp.substring(13, temp.length());
           makeItem++;
         }
         else if(temp.startsWith("Date: ") && date == null) {
           date = temp.substring(6, temp.lastIndexOf(":") + 3).trim();
           SimpleDateFormat dateFormat2 = null;
 
           if (date.charAt(2) == ' ' || date.charAt(1) == ' ') {
 		dateFormat2 = new SimpleDateFormat("dd MMM yyyy HH:mm:ss",
 						 Locale.US);
 		
           } else {
 		dateFormat2 = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss",
 						 Locale.US);
           }
           SimpleDateFormat dateFormat3 = new SimpleDateFormat(YAMM.getString("shortdate"));
           try {
             Date nisse = dateFormat2.parse(date);
 
             date = dateFormat3.format(nisse);
           } catch (ParseException pe) { System.err.println(pe); date = " ";}
           makeItem++;
         }
 
         else if(temp.equals(".") && makeItem > 0) {
           vec1.insertElementAt(Integer.toString(i), 0);
           vec1.insertElementAt(subject, 1);
           vec1.insertElementAt(from, 2);
           vec1.insertElementAt(date, 3);
           vec1.insertElementAt(status, 4);
 
           mailList.insertElementAt(vec1, i);
           vec1 = new Vector();
           subject = null;
           from = null;
           date = null;
           status = null;
           makeItem = 0;
           i++;
         }
       }
       in.close();
     }
     catch(IOException ioe) {
       System.out.println("Error: " + ioe);
     }
   }
 
   /**
    * Creates a list with the mails in the filter box.
    * @param mailList The vector that should contain the information
    */
   public static void createFilterList(Vector list) {
     String subject = null, from = null, to = null, reply = null;
 
     String temp = null;
     list.clear();
     Vector vec1 = new Vector();
     boolean makeItem = false;
 
     int i = 0;
 
     try {
       BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(System.getProperty("user.home") + "/.yamm/boxes/.filter")));
 
       for(;;) {
         temp = in.readLine();
       
         if(temp == null)
           break;
 
         else if(temp.startsWith("From:")) {
           if(from == null) from = temp.substring(6, temp.length());
         }
 
         else if(temp.startsWith("Subject:")) {
           if(subject == null) subject = temp.substring(9, temp.length());
         }
         else if(temp.startsWith("To:")) {
           if(to == null) to = temp.substring(4, temp.length());
         }
         else if(temp.startsWith("Reply-To:")) {
           if(reply == null) reply = temp.substring(10, temp.length());
         }
         else if(temp.equals(".")) { makeItem = true; }
 
         if(subject != null && from != null && to != null && makeItem) {
           vec1.insertElementAt(from, 0);
           vec1.insertElementAt(to, 1);
           vec1.insertElementAt(subject, 2);
           if(reply != null) vec1.insertElementAt(reply, 3);
           else vec1.insertElementAt("",3);
 
           list.insertElementAt(vec1, i);
           vec1 = new Vector();
           subject = null;
           from = null;
           to = null;
           reply = null;
           makeItem = false;
           i++;
         }
       }
       in.close();
     }
     catch(IOException ioe) {
       System.out.println("Error: " + ioe);
     }
   }
 
   /**
    * This function sets the status of the mail.
    * @param whichBox The box the message is in.
    * @param whichmail The mail to set the status for.
    * @param status The status string.
    */
   public static void setStatus(String whichBox, int whichmail, String status) {
     File source = new File(whichBox),target = new File(whichBox + ".tmp");
     String temp = null;
     try {
       BufferedReader in = new BufferedReader(
                           new InputStreamReader(new FileInputStream(source)));
       PrintWriter out   = new PrintWriter(new BufferedOutputStream(
                           new FileOutputStream(target)));
       int i = 0;
 
       for(;;) {
         temp = in.readLine();
    
         if(temp == null)
           break;
 
         else if(temp.equals(".")) i++;
 
         if(i == whichmail) {
           out.println(temp);
           boolean header = false;
           for(;;) {
             temp = in.readLine();
 
             if(temp == null) break;
 
 
             else if(temp.equals(".")) { i++; out.println(temp); break; }
 
             if(temp.startsWith("YAMM-Status:")) {
               i++;
               temp = "YAMM-Status: " + status;
               break;
             }
             else if(temp.equals("") && header) {
               i++;
               temp = "YAMM-Status: " + status + "\n";
               break;
             }
 
             if(!temp.equals("")) header = true; 
 
             out.println(temp);
           }
         }
 
         out.println(temp);
       }
       in.close();
       out.close();
 
       source.delete();
       target.renameTo(source);
     } catch (IOException ioe) { System.err.println("test:" + ioe); }
   }
  
   /**
    * Prints the mail to ~home/.yamm/tmp/cache/<whichBox>/<whichmail>.html
    * @param whichBox Which box the message is in
    * @param whichmail Whichmail to export
    * @param attach Which vector to add attachments to
    */
   public static void getMail(String whichBox, int whichmail) {
 
     String  boundary = null;
     String  temp = null;
     boolean wait = true;
     boolean html = false;
     int attaches = 0;
 
     temp = System.getProperty("user.home") + "/.yamm/";
     String tempdir = temp + "tmp/";
     String boxpath = whichBox.substring(whichBox.indexOf("boxes") + 6, whichBox.length());
     File   cache = new File(tempdir + "cache/" + boxpath + "/");
 
 
     if(!new File(cache, whichmail + ".html").exists()) {
       if(!cache.exists()) {
         if(!cache.mkdirs()) {
           System.err.println("Couldn't create dir: " + cache.toString());
         }
       }
 
       try {
         BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichBox)));
         PrintWriter outFile = new PrintWriter(new BufferedOutputStream(new FileOutputStream(new File(cache, whichmail + ".html"))));
         int i = 0;
         outFile.println("<html>\n<body>\n<table>");
 
         for(;;) {
           temp = in.readLine();
       
           if(temp == null)
             break;
 
           else if(temp.startsWith("Date: ")) {
             if(i == whichmail) {
               String date = temp.substring(6, temp.lastIndexOf(":") + 3).trim();
               SimpleDateFormat dateFormat2 = null;
 
               if (date.charAt(2) == ' ' || date.charAt(1) == ' ') {
                     dateFormat2 = new SimpleDateFormat("dd MMM yyyy HH:mm:ss",
                                                      Locale.US);     
                 
               } else {
                     dateFormat2 = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss",
                                                      Locale.US);     
               }
 
               SimpleDateFormat dateFormat3 = new SimpleDateFormat(YAMM.getString("longdate"));
               try {
                 Date nisse = dateFormat2.parse(date);
 
                 date = dateFormat3.format(nisse);
               } catch (ParseException pe) { System.err.println(pe); date = " ";}
 	      outFile.println("<tr><td>" + YAMM.getString("mail.date") + 
                               "</td><td> " + date + "</td></tr>");
             }
           }
 
           else if(temp.equals(".")) i++;
 
           else if(temp.startsWith("From:")) {
 
             if (i == whichmail) {
               String from = "", name = "";
               if(temp.indexOf("<") != -1 && temp.indexOf(">") != -1) {
                 from = temp.substring(temp.lastIndexOf("<") + 1, 
                        temp.lastIndexOf(">"));
                 name = temp.substring(6, temp.indexOf("<")).trim();
 
                 outFile.println("<tr><td>" + YAMM.getString("mail.from") + 
                                 "</td><td>" + name + "&lt;" +
                                 "<a href=\"mailto:" + from + "\">" +
                                 from + "</a>&gt;</td></tr>");
               }
               else {
                 from = temp.substring(6, temp.length());
                 outFile.println("<tr><td>" + YAMM.getString("mail.from") + 
                                 "</td><td> <a href=\"mailto:" + from + "\">" + 
                                 temp.substring(6, temp.length()) + 
                                 "</a></td></tr>");
               }
               wait = false;
             }
           }
 
           else if(temp.startsWith("To:")) {
             if(i == whichmail) {
             
               if(temp.indexOf("<") != -1) temp = temp.substring(temp.indexOf("<")  +1, temp.lastIndexOf(">"));
               else temp = temp.substring(3, temp.length());
 
               while(temp.indexOf(",", temp.length() - 5) != -1) {
                 String temp2 = in.readLine();
 
                 if(temp2.indexOf("<") != -1) temp2 = temp2.substring(temp2.indexOf("<")  +1, temp2.lastIndexOf(">"));
 
                 temp += temp2;
               }
 
               outFile.println("<tr><td>" + YAMM.getString("mail.to") + 
                               "</td><td> " + temp + "</td></tr>");
             }
           }
 
           else if(temp.startsWith("Reply-To:") && i == whichmail) {
             String reply = "", name = "";
             if(temp.indexOf("<") != -1 && temp.indexOf(">") != -1) {
               reply = temp.substring(temp.lastIndexOf("<") + 1, 
                                      temp.lastIndexOf(">"));
               name = temp.substring(10, temp.indexOf("<")).trim();
 
               outFile.println("<tr><td>" + YAMM.getString("mail.reply_to") + 
                               "</td><td>" + name +
                               " &lt;<a href=\"mailto:" + reply + "\">" + 
                               reply + "</a>&gt;</td></tr>");
             }
             else {
               reply = temp.substring(10, temp.length());
               outFile.println("<tr><td>" + YAMM.getString("mail.reply_to") + 
                               "</td><td> <a href=\"mailto:" + reply + "\">" + 
                               temp.substring(10, temp.length()) + 
                               "</a></td></tr>");
             }
           }
 
           else if(temp.indexOf("boundary=\"") != -1) {
             if(i == whichmail) {
               boundary = temp.substring(temp.indexOf("boundary=\"") + 10, temp.indexOf("\"", temp.indexOf("boundary=\"") + 11));
             }
           }
 
 
           else if(temp.startsWith("Subject:")) {
             if(i == whichmail) {
               outFile.println("<tr><td>" + YAMM.getString("mail.subject") + 
                               "</td><td> " + temp.substring(8, temp.length()) +
                               "</td></tr>");
             }
           }
 
           else if(temp.equals("")) {
             if(i == whichmail && wait == false) {
 
 	      outFile.println("</table>\n<pre>");
               for(;;) {
                 outFile.println(temp);
                 outFile.flush();
 
                 temp = in.readLine();
 
                 if(temp == null) break;
 
 
                 else if(temp.startsWith("<!doctype")) temp = in.readLine();
                 else if(temp.toLowerCase().indexOf("</html>") != -1) { html = false; temp = in.readLine(); }
 
                 else if(temp.toLowerCase().indexOf("<html>") != -1) { html = true; temp = in.readLine(); }
               
                 else if(temp.indexOf("MIME") != -1 || temp.indexOf("mime") != -1) {
                   temp = in.readLine();
                   temp = in.readLine();
 
                   if(temp.startsWith("--" + boundary)) {
                     for(;;) {
                       temp = in.readLine();
                       if(temp == null) break;
 
                       else if(temp.equals("")) break;
                     }
                   }
                 }
 
                 if(!html && (temp.indexOf("<") != -1 || temp.indexOf(">") != -1 || temp.indexOf("=") != -1)) temp = removeTags(temp);
 
                 if(!html && temp.indexOf("://") != -1 && temp.indexOf("href=") == -1 && temp.indexOf("HREF=") == -1) {
                   int    protBegin = temp.indexOf("://");
                   int    space     = temp.indexOf(" ", protBegin + 3);
                   String prot      = temp.substring( (((protBegin - 4) != -1) ? protBegin - 4 : 0), protBegin + 3).trim();
                   String link      = temp.substring(protBegin + 3, ((space == -1) ? temp.length() : space));
                   if(link.endsWith(".")) link = link.substring(0, link.length() -1);
                   int    dot       = temp.indexOf(".", protBegin + 3 + link.length());
                   int    tag       = temp.indexOf("&gt;", protBegin +3);
 
                   if(tag != -1) link = temp.substring(protBegin +3, tag);
                   String begin = temp.substring(0, temp.indexOf(prot));
                   String end   = "";
   
                   if(space != -1) {
                     if(dot < space && dot != -1) end = temp.substring(dot, temp.length());
                     else end = temp.substring(space, temp.length());
                   }
                   if(link.endsWith(">")) {
                     link = link.substring(0, link.length()-1);
                   }
                   else if(dot != -1) end = temp.substring(dot, temp.length());
                   else if(tag != -1) end = temp.substring(tag, temp.length());
                 
 
                   temp = begin + "<a href=\"" + prot + link + "\">" + prot + link + "</a>" + end;
                 }
 
                 else if(!html && temp.indexOf("@") != -1 && temp.indexOf("href=") == -1 && temp.indexOf("HREF=") == -1) {
                   String temp2 = null;
  
                   StringTokenizer tok = new StringTokenizer(temp);
                   for(;tok.hasMoreTokens();) {
                     temp2 = tok.nextToken();
                     if(temp2.indexOf("@") != -1) break;
                   }
                 
                   String begin = temp.substring(0, temp.indexOf(temp2));
                   String end = temp.substring(temp.indexOf(temp2) + temp2.length(), temp.length());
                   if(temp2.endsWith("&gt;")) {
                     end = temp2.substring(temp2.length()-4, temp2.length()) + end;
                     temp2 = temp2.substring(0, temp2.length()-4);
                   }
                   if(temp2.startsWith("&lt;")) {
                     begin += temp2.substring(0, 4);
                     temp2 = temp2.substring(4, temp2.length());
                   }
 
                   temp = begin + "<a href=\"mailto:" + temp2 + "\">" + temp2 + "</a>" + end;
                 }
 
                 else if(temp.startsWith("--" + boundary) && temp.endsWith("--")) break;
                 else if(temp.startsWith("--" + boundary) && !temp.endsWith("--")) {
                   attaches++;
                   boolean Break = false;
                   File f = new File(cache, whichmail + ".attach." + attaches);
 
                   try {
                     PrintWriter out = new PrintWriter(
                                       new BufferedOutputStream(
                                       new FileOutputStream(f)));
 //                    for(int four = 0; four < 4; four++) {
                     for(;;) {
                       temp = in.readLine();
                       if(temp == null) break;
                       if(temp.equals("")) { out.println(temp); break; }
                       if(temp.startsWith("Content-Transfer-Encoding: quoted-printable")) { Break = true; }
 
                       out.println(temp);
                     }
 
                     if(!Break) {
                       for(;;) {
                         temp =  in.readLine();
 
                         if(temp == null) break;
                         if(temp.equals(".")) return;
                         if(temp.equals("")) break;
 
                         out.println(temp);
                       }
                     }
                       
                     out.close();
                     if(Break) {
                       attaches--;
                       f.delete();
                     }
                   }
                   catch(IOException ioe) { 
                     System.out.println("Error!: " + ioe); 
                   }
                 }
 /* 
                 else if(temp.startsWith("--" + boundary)) {
                   String encode = null, filename = null;
                   temp = in.readLine();
  
                   if(!temp.equals("") && temp.indexOf("message") == -1) {
                     for(;;) {
                       if(temp == null) break;
 
                       if(temp.equals(".")) break;
 
                       if (temp.startsWith("Content-Transfer-Encoding: ")) {
                         encode = temp.substring(27, temp.length());
                       }
 
                       if(temp.indexOf("name=") != -1) {
                         filename = temp.substring(temp.indexOf("=\"") + 2, temp.indexOf("\"", temp.indexOf("=")+ 2));
                       }
                       if(encode != null && filename != null) {
                         Vector vect1 = new Vector();
                         vect1.add(encode);
                         vect1.add(filename);
 
                         attach.add(vect1);
                         encode = null;
                         filename = null;
                       }
                       temp = in.readLine();
                     }
                     wait = true;
                     break;
                   }
                   for(;;) {
                     temp = in.readLine();
  
                     if(temp.equals("")) break;
                     else if(temp.equals(".")) break;
                     else if(temp == null) break;
                   }
                 }
 */
                 else if(temp.equals(".")) {
                   wait = true;
                   break;
                 }
               }
               break;
             }
           }
         }
         outFile.println("</pre></body></html>");
         in.close();
         outFile.close();
       }
       catch(IOException ioe) {
         System.err.println("Error: " + ioe);
       }
     }
   }
 
   protected static String removeTags(String html) {
     int index = 0;
 
     for(;html.indexOf("<", index) != -1;) {
       index = html.indexOf("<", index) + 1;
 
       String begin = html.substring(0, index -1);
       String end = html.substring(index, html.length());
       html = begin + "&lt;" + end;
     }
     index = 0;
     for(;html.indexOf(">", index) != -1;) {
       index = html.indexOf(">", index) + 1;
 
       String begin = html.substring(0, index -1);
       String end = html.substring(index, html.length());
       html = begin + "&gt;" + end;
     }
     if(html.indexOf("=") != -1) {
       index = 0;
       char[] check = "0123456789ABCDEF".toCharArray();
       for(;html.indexOf("=", index) != -1 && html.indexOf("=", index) + 2 < html.length();) {
         index = html.indexOf("=", index) + 1;
         char[] hex = html.substring(index, index+2).toCharArray();
         boolean char1 = false, char2 = false;
 
         for(int i = 0; i < 16;i++) {
           if(hex[0] == check[i]) { char1 = true; break; }
         }
         for(int i = 0; i < 16;i++) {
           if(hex[1] == check[i]) { char2 = true; break; }
         }
 
         if(char1 && char2) {
           int htmlchar = Integer.parseInt(new String(hex), 16);
           String begin = html.substring(0, index -1);
           String end = html.substring(index + 2, html.length());
           html = begin + "&#" + htmlchar + ";" + end;
         }
       }
     }
 
     return html;
   }
 
   /**
    * Gets the from and subject field from specified message
    * @param whichBox Which box the message is in
    * @param whichmail Which mail to get the headers from
    */
   public static String[] getMailForReplyHeaders(String whichBox, int whichmail) {
 
     String  temp = null, from = null, subject = null;
 
     try {
       BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichBox)));
       int i = 0;
 
       for(;;) {
         temp = in.readLine();
       
         if(temp == null)
           break;
 
         else if(temp.startsWith("From:")) {
           if (i == whichmail) {
             from = "";
 
             if(temp.indexOf("<") != -1 && temp.indexOf(">") != -1)
               from = temp.substring(temp.lastIndexOf("<") + 1, temp.lastIndexOf(">"));
             else  from = temp.substring(6, temp.length());
           }
         }
         else if(temp.equals(".")) i++;
 
         else if(temp.startsWith("Subject:")) {
           if(i == whichmail) {
             subject = temp.substring(9, temp.length());
           }
         }
 
         else if(temp.equals("") && from != null && subject != null) {
 	  break;
         }
       }
       in.close();
     }
     catch(IOException ioe) {
       System.out.println("Error: " + ioe);
     }
     String[] ret = {from,subject};
     return ret;
   }
 
   /**
    * Gets the content of this mail and adds to the specified JTextArea
    * @param whichBox Which box the message is in
    * @param whichmail Which mail to export
    * @param jtarea The JTextArea to append the message to
    */
   public static void getMailForReply(String whichBox, int whichmail, JTextArea jtarea) {
 
     String  temp = null, boundary = null;
     boolean wait = true;
 
     try {
       BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichBox)));
       int i = 0;
 
       for(;;) {
         temp = in.readLine();
       
         if(temp == null)
           break;
         else if(temp.equals(".")) i++;
         else if(temp.startsWith("From: ") && i == whichmail) wait = false;
         else if(temp.equals("") && i == whichmail && !wait) {
           for(;;) {
             jtarea.append(">" + temp + "\n");
 
             temp = in.readLine();
 
             if(temp.indexOf("=") != -1) {
               int index = 0;
               char[] check = "0123456789ABCDEF".toCharArray();
               for(;temp.indexOf("=", index) != -1 && temp.indexOf("=", index) + 2 < temp.length();) {
                 index = temp.indexOf("=", index) + 1;
                 char[] hex = temp.substring(index, index+2).toCharArray();
                 boolean char1 = false, char2 = false;
 
                 for(int j = 0; j < 16;j++) {
                   if(hex[0] == check[j]) { char1 = true; break; }
                 }
                 for(int j = 0; j < 16;j++) {
                   if(hex[1] == check[j]) { char2 = true; break; }
                 }
 
                 if(char1 && char2) {
                   int htmlchar = Integer.parseInt(new String(hex), 16);
                   String begin = temp.substring(0, index -1);
                   String end = temp.substring(index + 2, temp.length());
                  temp = begin + (int)htmlchar + end;
                 }
               }
             }
             if(temp == null) break;
             else if(temp.indexOf("MIME") != -1 || temp.indexOf("mime") != -1) {
               temp = in.readLine();
               temp = in.readLine();
 
               if(temp.startsWith("--" + boundary)) {
                 for(;;) {
                   temp = in.readLine();
                   if(temp == null) break;
                   else if(temp.equals("")) break;
                 }
               }
             }
 
             else if(temp.startsWith("--" + boundary) && !temp.substring(14, 15).equals("-") && !temp.substring(14,15).equals(" ")) break;
             else if(temp.equals(".")) {
               break;
             }
           }
           break;
         }
       }
       in.close();
     } catch(IOException ioe) { System.out.println("Error: " + ioe); }
   }
 
 
   /**
    * Deletes the specified mail. Returns false if it fails.
    * @param whichBox The box the message is in
    * @param whichmail The mail to delete
    */ 
   public static boolean deleteMail(String whichBox, int[] whichmail) {
     String temp = null;
     boolean firstmail = true;
     String home = System.getProperty("user.home");
     String sep = System.getProperty("file.separator");
     int next = 0;
 
     if(!whichBox.equals(home + sep + ".yamm" + sep + "boxes" + sep + YAMM.getString("box.trash"))) {
       try {
 	File inputFile = new File(whichBox);
 	File outputFile = new File(whichBox + ".tmp");
         PrintWriter outFile = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFile)));
         PrintWriter outFile2 = new PrintWriter(new BufferedOutputStream(new FileOutputStream(System.getProperty("user.home") + "/.yamm/boxes/" + YAMM.getString("box.trash"), true)));
         BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
 
         int i = 0;
 
         for(;;) {
           temp = in.readLine();
 
           if(temp == null) break;
 
           else if(firstmail) {
             if(i != whichmail[next]) {
               for(;;) {
                 outFile.println(temp);
                 temp = in.readLine();
           
                 if(temp == null) break;
                 else if(temp.equals(".")) {
                   outFile.println(temp);
                   break;
                 }
               } 
             }
             else if(i == whichmail[next]) {
               for(;;) {
                 outFile2.println(temp);
                 temp = in.readLine();
 
                 if(temp == null) break;
                 else if(temp.equals(".")) {
                   outFile2.println(temp);
                   break;
                 }
               }
               if(!(next++ < whichmail.length-1)) break;
             }
             firstmail = false;
           }
           else if(!firstmail) {
             i++;
        
             if(i != whichmail[next]) {
               for(;;) {
                 outFile.println(temp);
                 temp = in.readLine();
 
                 if(temp == null) break;
                 else if(temp.equals(".")) {
                   outFile.println(temp);
                   break;
                 }
               }
             }
             else if(i == whichmail[next]) {
               for(;;) {
                 outFile2.println(temp);
                 temp = in.readLine();
 
                 if(temp == null) break;
                 else if(temp.equals(".")) {
                   outFile2.println(temp);
                   break;
                 }
               }
               if(!(next++ < whichmail.length-1)) break;
             }
           }
         }
         for(;;) {
           temp = in.readLine();
           if(temp == null) break;
           else outFile.println(temp);
         }
         in.close();
         outFile.close();
         outFile2.close();
 
         inputFile.delete();
         if(!outputFile.renameTo(inputFile)) {
           System.err.println("ERROR: Couldn't rename " + whichBox + ".tmp to " + whichBox);
         }
       }
       catch(IOException ioe) {
         System.out.println("Error: " + ioe);
         return false;
       }
     }
     else if(whichBox.equals(home + sep + ".yamm" + sep + "boxes" + sep + YAMM.getString("box.trash"))) {
       try {
         File inputFile = new File(home + "/.yamm/boxes/" +
                                   YAMM.getString("box.trash"));
 	File outputFile = new File(home + "/.yamm/boxes/" +
                                    YAMM.getString("box.trash") + ".tmp");
         BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
         PrintWriter outFile = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFile)));
 
         int i = 0;
 
         for(;;) {
           temp = in.readLine();
 
           if(temp == null) break;
           else if(firstmail) {
             if(i != whichmail[next]) {
               for(;;) {
                 outFile.println(temp);
                 temp = in.readLine();
           
                 if(temp == null) break;
                 else if(temp.equals(".")) {
                   outFile.println(temp);
                   break;
                 }
               } 
             }
             else if(i == whichmail[next]) {
 
               for(;;) {
                 temp = in.readLine();
 
                 if(temp == null) break;
                 else if(temp.equals(".")) break;
               }
               if(!(next++ < whichmail.length-1)) break;
             }
             firstmail = false;
           }
           else if(!firstmail) {
             i++;
        
             if(i != whichmail[next]) {
               for(;;) {
                 outFile.println(temp);
                 temp = in.readLine();
 
                 if(temp == null) break;
                 else if(temp.equals(".")) {
                   outFile.println(temp);
                   break;
                 }
               }
             }
             else if(i == whichmail[next]) {
               for(;;) {
                 temp = in.readLine();
 
                 if(temp == null) break;
                 else if(temp.equals(".")) break;
               }
               if(!(next++ < whichmail.length-1)) break;
             }
           }
         }
         for(;;) {
           temp = in.readLine();
           if(temp == null) break;
           else outFile.println(temp);
         }
 
         in.close();
         outFile.close();
 
         inputFile.delete();
         if(!outputFile.renameTo(inputFile)) {
           System.err.println("ERROR: Couldn't rename " + whichBox + ".tmp to " + whichBox);
         }
       }
       catch(IOException ioe) {
         System.out.println("Error: " + ioe);
         return false;
       }
     }
     return true;
   }
 
   /**
    * This function tries to copy a mail from a box to another.
    * @param fromBox From which box
    * @param toBox To which box
    * @param whichmail The mail to copy from fromBox to toBox
    */
   public static boolean copyMail(String fromBox, String toBox, int[] whichmail) {
 
     String  temp = null;
     boolean firstmail = true;
     int next = 0;
 
     try {
       BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fromBox)));
       PrintWriter outFile2 = new PrintWriter(new BufferedOutputStream(new FileOutputStream(toBox, true)));
 
       int i = 0;
 
       for(;;) {
         temp = in.readLine();
         
 
         if(temp == null) break;
 
         else if(firstmail) {
           if(i != whichmail[next]) {
             for(;;) {
               temp = in.readLine();
         
               if(temp == null) break;
               else if(temp.equals(".")) break;
             } 
           }
 
           else if(i == whichmail[next]) {
             for(;;) {
               outFile2.println(temp);
               temp = in.readLine();
 
               if(temp == null) break;
               else if(temp.equals(".")) {
                 outFile2.println(temp);
                 break;
               }
             }
             if(!(next++ < whichmail.length-1)) break;
           }
           firstmail = false;
         }
 
         else if(!firstmail) {
           i++;
        
           if(i != whichmail[next]) {
             for(;;) {
               temp = in.readLine();
 
               if(temp == null) break;
               else if(temp.equals(".")) break;
             }
           }
 
           else if(i == whichmail[next]) {
             for(;;) {
               outFile2.println(temp);
               temp = in.readLine();
 
               if(temp == null) break;
               else if(temp.equals(".")) {
                 outFile2.println(temp);
                 break;
               }
             }
             if(!(next++ < whichmail.length-1)) break;
           }
         }
       }
       in.close();
       outFile2.close();
     }
     catch(IOException ioe) {
       System.out.println("Error: " + ioe);
       return false;
     }
     return true;
   }
 
   /**
    * Moves a mail from one box to another.
    * @param fromBox The box to move the mail from
    * @param toBox The box to move the mail to
    * @param whichmail The mail to move
    */
   public static boolean moveMail(String fromBox, String toBox, int[] whichmail) {
     String temp = null;
     boolean firstmail = true;
     int next = 0;
 
 
     if(fromBox.equals(toBox)) return false;
 
     try {
       File inputFile = new File(fromBox);
       BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
       File outputFile = new File(fromBox + ".tmp");
       PrintWriter outFile = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFile)));
       PrintWriter outFile2 = new PrintWriter(new BufferedOutputStream(new FileOutputStream(toBox, true)));
 
       int i = 0;
 
       for(;;) {
         temp = in.readLine();
         
 
         if(temp == null) break;
 
         else if(firstmail) {
           if(i != whichmail[next]) {
             for(;;) {
               outFile.println(temp);
               temp = in.readLine();
         
               if(temp == null) break;
               else if(temp.equals(".")) {
                 outFile.println(temp);
                 break;
               }
             } 
           }
 
           else if(i == whichmail[next]) {
             for(;;) {
               outFile2.println(temp);
               temp = in.readLine();
 
               if(temp == null) break;
               else if(temp.equals(".")) {
                 outFile2.println(temp);
                 break;
               }
             }
             if(!(next++ < whichmail.length-1)) break;
           }
           firstmail = false;
         }
 
         else if(!firstmail) {
           i++;
        
           if(i != whichmail[next]) {
             for(;;) {
               outFile.println(temp);
               temp = in.readLine();
 
               if(temp == null) break;
               else if(temp.equals(".")) {
                 outFile.println(temp);
                 break;
               }
             }
           }
 
           else if(i == whichmail[next]) {
             for(;;) {
               outFile2.println(temp);
               temp = in.readLine();
 
               if(temp == null) break;
               else if(temp.equals(".")) {
                 outFile2.println(temp);
                 break;
               }
             }
             if(!(next++ < whichmail.length-1)) break;
           }
         }
       }
       for(;;) {
         temp = in.readLine();
         if(temp == null) break;
         else outFile.println(temp);
       }
 
       in.close();
       outFile.close();
       outFile2.close();
 
       inputFile.delete();
       if(!outputFile.renameTo(inputFile)) {
         System.err.println("ERROR: Couldn't rename " + fromBox + ".tmp to " + fromBox);
       }
     }
     catch(IOException ioe) {
       System.out.println("Error: " + ioe);
       return false;
     }
     return true;
   }
 
   /**
    * Counts how many mails the box have.
    * @param whichbox The box to count the mails in
    */
   public static int mailsInBox(String whichbox) {
     String temp = null;
 
     int i = 0;
 
     try {
       BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichbox)));
 
       for(;;) {
         temp = in.readLine();
 
         if(temp == null) break;
 
         if(temp.equals(".")) i++;
       }
     }
     catch(IOException ioe) {
       System.out.println("Error: " + ioe);
     }
     return i;
   }
 
   /**
    * Checks if the box has mail in it.
    * @param whichBox The box to check for mail
    */
   public static boolean hasMail(String whichBox) {
     String temp = null;
     boolean mail = false;
 
     try {
       BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(whichBox)));
 
       for(;;) {
         temp = in.readLine();
         
         if(temp == null) break;
         else if(temp.startsWith("Date:")) { mail = true; break; }
       }
       in.close();
     }
     catch(IOException ioe) {
       System.out.println("Error: " + ioe);
       return false;
     }
     if(mail) return true;
     else return false;
   }
 }
