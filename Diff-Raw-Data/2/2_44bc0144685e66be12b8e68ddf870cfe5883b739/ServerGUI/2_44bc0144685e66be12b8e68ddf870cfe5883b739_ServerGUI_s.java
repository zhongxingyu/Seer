 /*
  * Copyright (C) 2010 Peter Martischka This program is free software; you can
  * redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation; either version 3 of the
  * License, or (at your option) any later version. This program is distributed
  * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details. You should have received
  * a copy of the GNU General Public License along with this program; if not, see
  * <http://www.gnu.org/licenses/ >.
  */
 
 package de.pitapoison.chat.server;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 public class ServerGUI
 {
     private static JTextArea logArea;
     private static JFrame frame;
     
     //Singleton
     private ServerGUI(){}
     
     public static void showLog()
     {
         if(frame != null)
             return;
         
         frame=new JFrame("Chatserver");
         frame.setLayout(new BorderLayout());
         
         //Erzeuge die LogArea
         logArea=new JTextArea();
         logArea.setEditable(false);
         frame.add(new JScrollPane(logArea));
         
         //Fenster anzeigen
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(400,300);
         frame.setVisible(true);
     }
     
     public static void message(String message)
     {
         System.out.println(message);
         
         //Gebe es auf der grafischen Oberfläche aus, falls vorhanden
         if(logArea!=null)
             logArea.setText(logArea.getText() + message + "\n");
     }
     
     public static void main(String[] args)
     {
         //Wenn es nicht im Headless Modus, also ohne GUI gestartet wurde
        if(args.length==0 || args[1].equals("headless"))
             ServerGUI.showLog();
         
         new Server();
     }
 }
