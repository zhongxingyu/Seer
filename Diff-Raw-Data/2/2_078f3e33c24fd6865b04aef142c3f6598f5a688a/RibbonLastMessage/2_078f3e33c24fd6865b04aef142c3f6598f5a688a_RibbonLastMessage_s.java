 /**
  * This file is part of RibbonLastMessage application (check README).
  * Copyright (C) 2012-2013 Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package ribbonlastmessage;
 
 /**
  * Main class
  * @author Stanislav Nepochatov
  */
 public class RibbonLastMessage {
     
     /**
      * Client socket object
      */
     public static java.net.Socket ClientSocket;
     
     /**
      * Stream of the answers from server
      */
     public static java.io.BufferedReader inStream;
     
     /**
      * PrintWriter to the server
      */
     public static java.io.PrintWriter outStream;
     
     /**
      * Massege window object
      */
     public static messageFrame messageWindow;
     
     /**
      * Networking thread
      */
     private static messegeWatherThread networking;
     
     /**
      * Network state flag
      */
     public static Boolean networkIsUp = false;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         messageWindow = new messageFrame();
         messageWindow.setVisible(true);
     }
     
     /**
      * Connect to server
      */
     public static void connect() {
         if (networkIsUp == true) {
             outStream.println("RIBBON_NCTL_CLOSE:");
         }
         networking = new messegeWatherThread();
         networking.start();
     }
     
     /**
      * Disconnect from server
      */
     public static void disconnect() {
         if (networkIsUp == true) {
             outStream.println("RIBBON_NCTL_CLOSE:");
             networkIsUp = false;
             messageWindow.setState();
         }
         networking = null;
     }
     
     /**
      * Network communication thread
      */
     private static class messegeWatherThread extends Thread {
         
         messegeWatherThread() {
             try {
                 String[] parsedArgs = RibbonLastMessage.messageWindow.getNetworkArgs();
                 RibbonLastMessage.ClientSocket = new java.net.Socket(java.net.InetAddress.getByName(parsedArgs[0]),
                     Integer.parseInt(parsedArgs[1]));
             } catch (java.io.IOException ex) {
                 RibbonLastMessage.errorMessage("Неможливо з'єднатися з сервером!");
             }
         }
         
         @Override
         public void run() {
             if (RibbonLastMessage.ClientSocket != null) {
                 try {
                     RibbonLastMessage.networkIsUp = false;
                     Boolean isAlive = true;
                     inStream = new java.io.BufferedReader(new java.io.InputStreamReader(ClientSocket.getInputStream(), "UTF-8"));
                     outStream = new java.io.PrintWriter(ClientSocket.getOutputStream(), true);
                     RibbonLastMessage.networkIsUp = true;
                     RibbonLastMessage.messageWindow.setState();
                     String inputLine = null;
                     outStream.println("RIBBON_NCTL_INIT:CLIENT,a2," + System.getProperty("file.encoding"));
                     waitForOK();
                    outStream.println("RIBBON_NCTL_LOGIN:{root},74cc1c60799e0a786ac7094b532f01b1");
                     RibbonLastMessage.loadLastMessage();
                     while (isAlive) {
                         inputLine = inStream.readLine();
                         System.out.println(inputLine);
                         String[] parsedCommand = RibbonLastMessage.parseCommand(inputLine);
                         if (parsedCommand[0].equals("COMMIT_END")) {
                             isAlive = false;
                         } else if (parsedCommand[0].equals("RIBBON_ERROR")) {
                             RibbonLastMessage.errorMessage(inputLine.split(":")[1]);
                         } else if (parsedCommand[0].equals("RIBBON_UCTL_LOAD_INDEX") || parsedCommand[0].equals("RIBBON_UCTL_UPDATE_INDEX")) {
                             RibbonLastMessage.loadNewMessage(parsedCommand[1]);
                         }
                     }
                 } catch (java.lang.NullPointerException ex ) {
                     RibbonLastMessage.errorMessage("З'єднання розірвано!");
                     RibbonLastMessage.networkIsUp = false;
                 } catch (java.io.IOException ex) {
                     RibbonLastMessage.errorMessage("Неможливо зчитати дані з сокету!");
                     RibbonLastMessage.networkIsUp = false;
                 }
             }
         }
         
         /**
          * Make current thread wait for status returning.
          */
         public void waitForOK() throws java.io.IOException {
             String returnedStatus;
             while (networkIsUp) {
                 if ((returnedStatus = inStream.readLine()).equals("OK:")) {
                     return;
                 }
             }
         }
         
     }
     
     /**
      * Show graphical warning message
      * @param Message text of warning message
      */
     public static void warningMessage (String Message) {
         final javax.swing.JPanel panel = new javax.swing.JPanel();
         javax.swing.JOptionPane.showMessageDialog(panel, Message, "Увага!", javax.swing.JOptionPane.WARNING_MESSAGE);
     }
     
     /**
      * Show graphical error message
      * @param Message text of warning message
      */
     public static void errorMessage (String Message) {
         final javax.swing.JPanel panel = new javax.swing.JPanel();
         javax.swing.JOptionPane.showMessageDialog(panel, Message, "Помилка!", javax.swing.JOptionPane.ERROR_MESSAGE);
     }
     
     /**
      * Parse command string
      * @param rawString string from socket
      * @return array with command and its arguments
      */
     public static String[] parseCommand (String rawString) {
         String[] returnedArray = new String[2];
         Integer splitIndex = -1;
         for (Integer cursorIndex = 0; cursorIndex < rawString.length(); cursorIndex++) {
             if (rawString.charAt(cursorIndex) == ':') {
                 splitIndex = cursorIndex;
                 break;
             }
         }
         if (splitIndex == -1) {
             return null;
         } else {
             returnedArray[0] = rawString.substring(0, splitIndex);
             returnedArray[1] = rawString.substring(splitIndex + 1);
             return returnedArray;
         }
     }
     
     /**
      * Get multiline command result<br>
      * <b>WARNING! This method temporary block input stream for reading!</b>
      * @param givenCommand comand to execute;
      * @return multiline string
      */
     public static String multiLineGet(String givenCommand) {
         Boolean keepGetting = true;
         String gottenString = "";
         StringBuffer gottenBuffer = new StringBuffer();
         RibbonLastMessage.outStream.println(givenCommand);
         while (keepGetting) {
             try {
                 String inputLine = RibbonLastMessage.inStream.readLine();
                 String[] parsedCommand = RibbonLastMessage.parseCommand(inputLine);
                 if (!inputLine.equals("END:")) {
                     gottenBuffer.append(inputLine);
                     gottenBuffer.append("\n");
                 } else if (parsedCommand[0].equals("RIBBON_ERROR:")) {
                     RibbonLastMessage.errorMessage(parsedCommand[1]);
                     return null;
                 } else {
                     keepGetting = false;
                 }
             } catch (java.io.IOException ex) {
                 RibbonLastMessage.errorMessage("Неможливо отримати дані з сокету!");
             }
         }
         return gottenBuffer.toString();
     }
     
     /**
      * Get last line of command result<br>
      * <b>WARNING! This method temporary block input stream for reading!</b>
      * @param givenCommand comand to execute;
      * @return last line of result
      */
     public static String multiLineGetLast(String givenCommand) {
         Boolean keepGetting = true;
         String gottenString = "";
         RibbonLastMessage.outStream.println(givenCommand);
         while (keepGetting) {
             try {
                 String inputLine = RibbonLastMessage.inStream.readLine();
                 String[] parsedCommand = RibbonLastMessage.parseCommand(inputLine);
                 if (!inputLine.equals("END:")) {
                     gottenString = inputLine;
                 } else if (parsedCommand[0].equals("RIBBON_ERROR:")) {
                     RibbonLastMessage.errorMessage(parsedCommand[1]);
                     return null;
                 } else {
                     keepGetting = false;
                 }
             } catch (java.io.IOException ex) {
                 RibbonLastMessage.errorMessage("Неможливо отримати дані з сокету!");
             }
         }
         return gottenString;
     }
     
     /**
      * Load last message from RobbonServer
      */
     public static void loadLastMessage() {
         String lastCsv = RibbonLastMessage.parseCommand(RibbonLastMessage.multiLineGetLast("RIBBON_LOAD_BASE_FROM_INDEX:0"))[1];
         MessageClasses.Message lastMessage = new MessageClasses.Message(lastCsv);
         lastMessage.CONTENT = RibbonLastMessage.multiLineGet("RIBBON_GET_MESSAGE:" + lastMessage.DIRS[0] + "," + lastMessage.INDEX);
         messageWindow.showMessage(lastMessage);
     }
     
     /**
      * Load new message from RibbonServer
      * @param csvLine csv part of server notification
      */
     public static void loadNewMessage(String csvLine) {
         MessageClasses.Message newMessage = new MessageClasses.Message(csvLine);
         newMessage.CONTENT = RibbonLastMessage.multiLineGet("RIBBON_GET_MESSAGE:" + newMessage.DIRS[0] + "," + newMessage.INDEX);
         messageWindow.showMessage(newMessage);
     }
 }
