 /*  mainMenu.java - mainMenu class
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
 
 package org.gjt.fredde.yamm.gui.main;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 import java.awt.Rectangle;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import org.gjt.fredde.util.gui.MsgDialog;
 import org.gjt.fredde.yamm.gui.sourceViewer;
 import org.gjt.fredde.yamm.mail.Mailbox;
 import org.gjt.fredde.yamm.YAMMWrite;
 import org.gjt.fredde.yamm.Options;
 import org.gjt.fredde.yamm.YAMM;
 
 /**
  * The mainMenu class.
  * This is the menu that the mainwindow uses.
  */
 public class mainMenu extends JMenuBar {
 
   /**
    * This frame is used to do som framestuff
    */
   static YAMM frame = null;
 
   /**
    * The Properties that loads and saves information.
    */
   static protected Properties     props = new Properties();
 
   /**
    * Makes the menu, adds a menulistener etc...
    * @param frame2 The JFrame that will be used when displaying error messages etc
    */
   public mainMenu(YAMM frame2) {
     frame = frame2;
 
     try {
       InputStream in = new FileInputStream(System.getProperty("user.home") + "/.yamm/.config");
       props.load(in);
       in.close();
     } catch (IOException propsioe) { System.err.println(propsioe); }
 
     JMenu file = new JMenu(YAMM.getString("file"));
     JMenuItem rad;
     add(file);
 
 
     // the file menu
     rad = new JMenuItem(YAMM.getString("file.new"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/new_mail.gif"));
     rad.addActionListener(MListener);
     file.add(rad);
 
     rad = new JMenuItem(YAMM.getString("file.save_as"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/save_as.gif"));
     rad.addActionListener(MListener);
     file.add(rad);
 
     file.addSeparator();
 
     rad = new JMenuItem(YAMM.getString("file.exit"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/exit.gif"));
     rad.addActionListener(MListener);
     file.add(rad);
 
     // the edit menu
     JMenu edit = new JMenu(YAMM.getString("edit"));
     add(edit);
 
     rad = new JMenuItem(YAMM.getString("edit.settings"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/prefs.gif"));
     rad.addActionListener(MListener);
     edit.add(rad);
 
     rad = new JMenuItem(YAMM.getString("edit.view_source"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/search.gif"));
     rad.addActionListener(MListener);
     edit.add(rad);
 
     // the help menu
     JMenu help = new JMenu(YAMM.getString("help"));
     add(help);
 
     rad = new JMenuItem(YAMM.getString("help.about_you"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/help.gif"));
     rad.addActionListener(MListener);
     help.add(rad);
 
     rad = new JMenuItem(YAMM.getString("help.about"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/help.gif"));
     rad.addActionListener(MListener);
     help.add(rad);
 
     rad = new JMenuItem(YAMM.getString("help.license"), new ImageIcon("org/gjt/fredde/yamm/images/types/text.gif"));
     rad.addActionListener(MListener);
     help.add(rad);
 
     rad = new JMenuItem(YAMM.getString("help.bug_report"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/bug.gif"));
     rad.addActionListener(MListener);
     help.add(rad);
   }
 
   ActionListener MListener = new ActionListener() {
     public void actionPerformed(ActionEvent ae) {
       String kommando = ((JMenuItem)ae.getSource()).getText();
 
       if(kommando.equals(YAMM.getString("file.exit"))) {
         frame.Exit();
       }
       else if(kommando.equals(YAMM.getString("help.about_you"))) {
         String host = null, ipaddress = null;
 
         try {
           InetAddress myInetaddr = InetAddress.getLocalHost();
 
           ipaddress = myInetaddr.getHostAddress();
           host = myInetaddr.getHostName();
         }
         catch (UnknownHostException uhe) {};
         if (ipaddress == null) ipaddress = "unknown";
         if (host == null) host = "unknown";
         Object[] args = {System.getProperty("os.name"),
                          System.getProperty("os.version"),
                          System.getProperty("os.arch"),
                          ipaddress,
                          host,
                          System.getProperty("java.version"),
                          System.getProperty("java.vendor"),
                          System.getProperty("java.vendor.url"),
                          System.getProperty("user.name"),
                          System.getProperty("user.home")};
 
         new MsgDialog(null, YAMM.getString("help.about_you"),
                       YAMM.getString("info.about.you", args));
       }
 
       else if(kommando.equals(YAMM.getString("help.about"))) {
         new MsgDialog(null, YAMM.getString("help.about"),
                       "Copyright (C) 1999 Fredrik Ehnbom\n"
                     + "YAMM-version: " + YAMM.yammVersion + "\n"
                     + "Compiledate: " + YAMM.compDate + "\n"
                     + "Homepage: http://www.gjt.org/~fredde/yamm.html\n"
                     + "E-mail: <fredde@gjt.org>\n"
                     + "\n"
                     + "This program is part of the Giant Java Tree.\n"
                     + "For more info please visit http://www.gjt.org\n"
                     + "\n"
                     + "Most icons are made or based on icons made\n"
                     + "by Tuomas Kuosmanen <tigert@gimp.org>");
                     
       }
       else if(kommando.equals(YAMM.getString("help.bug_report"))) {
         YAMMWrite yw = new YAMMWrite("fredde@gjt.org", "Bug report");
          JTextArea jt = yw.myTextArea;
          jt.append("What is the problem?\n\n");
          jt.append("How did you make it happen?\n\n");
          jt.append("Can you make it happen again?\n\n");
          jt.append("\nAnd now some info about your system:\n");
          p("java.version", jt);
          p("java.vendor", jt);
          p("java.vendor.url", jt);
          p("java.home", jt);
          p("java.vm.specification.version", jt);
          p("java.vm.specification.vendor", jt); 
          p("java.vm.specification.name", jt);
          p("java.vm.version", jt);
          p("java.vm.vendor", jt);
          p("java.vm.name", jt);
          p("java.specification.version", jt);
          p("java.specification.vendor", jt);
          p("java.specification.name", jt);
          p("java.class.version", jt);
          p("java.class.path", jt);
          p("os.name", jt);
          p("os.arch", jt);
          p("os.version", jt);
       }
       else if(kommando.equals(YAMM.getString("help.license"))) {
         new MsgDialog(null, YAMM.getString("help.license"), 
                       "Yet Another Mail Manager " + YAMM.yammVersion + " E-Mail Client\n" +
                       "Copyright (C) 1999 Fredrik Ehnbom\n" +
                       YAMM.getString("license"),
                       MsgDialog.OK, JLabel.LEFT);
       }  
       else if(kommando.equals(YAMM.getString("edit.settings"))) {
         new Options(frame);
       }
       else if(kommando.equals(YAMM.getString("file.new"))) {
         new YAMMWrite();
       }
       else if(kommando.equals(YAMM.getString("edit.view_source"))) {
         int test = ((JTable)frame.mailList).getSelectedRow();
         if(test >= 0 && test < frame.listOfMails.size()) {
           int i = 0;
 
           while(i<4) {
             if(((JTable)frame.mailList).getColumnName(i).equals("#")) { break; }
             i++;
           }
 
           int msg = Integer.parseInt(((JTable)frame.mailList).getValueAt(((JTable)frame.mailList).getSelectedRow(), i).toString());
           if(msg != -1) Mailbox.viewSource(frame.selectedbox, msg, new sourceViewer().jtarea);
         }
       }
       else if(kommando.equals(YAMM.getString("file.save_as"))) {
         int test = ((JTable)frame.mailList).getSelectedRow();
         if(test != -1 && test <= frame.listOfMails.size()) {
           JFileChooser jfs = new JFileChooser();
           jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
           jfs.setMultiSelectionEnabled(false);
           jfs.setFileFilter(new filter());
           jfs.setSelectedFile(new File("mail.html"));
           int ret = jfs.showSaveDialog(frame);
 
           if(ret == JFileChooser.APPROVE_OPTION) {
             String tmp = frame.selectedbox;
             String boxName = tmp.substring(tmp.indexOf("boxes")+6,tmp.length());
             tmp = frame.mailPageString;
             boxName = tmp.substring(8, tmp.length()) + boxName + YAMM.sep + 
                       frame.mailList.getSelectedRow() + ".html";
 
             new File(boxName).renameTo(jfs.getSelectedFile());
           }
         }
       }
     }
     void p(String prop, JTextArea ta) {
       ta.append(prop + " : " + System.getProperty(prop) + "\n");
     }
 
   };
 
   protected class filter extends FileFilter {
     public boolean accept(File f) {
       if(f.isDirectory()) {
          return true;
       }
 
       String s = f.getName();
       int i = s.lastIndexOf('.');
       if(i > 0 &&  i < s.length() - 1) {
         String extension = s.substring(i+1).toLowerCase();
         if ("htm".equals(extension) || "html".equals(extension)) return true;
         else return false;
       }
       return false;
     }
        
     // The description of this filter
     public String getDescription() {
       return "HTML files";
     }
   }
 }
    
