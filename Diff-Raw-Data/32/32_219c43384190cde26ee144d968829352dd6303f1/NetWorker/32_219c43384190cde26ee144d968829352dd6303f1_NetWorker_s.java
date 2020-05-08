 /**
  * This file is part of libRibbonApp library (check README).
  * Copyright (C) 2013 Stanislav Nepochatov
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
 
 package AppComponents;
 
 /**
  * Networking and protocol listeners container class;
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public class NetWorker extends Thread{
     
     /**
      * Client socket object.
      */
     public java.net.Socket clientSocket;
     
     /**
      * Input stream (from server).
      */
     public java.io.BufferedReader inStream;
     
     /**
      * Output stream (to server).
      */
     public java.io.PrintWriter outStream;
     
     /**
      * Display is NetWorker connection is alive.
      */
     public Boolean isAlive = false;
     
     /**
      * Array of protocol listeners.
      */
     public Listener[] NetListeners;
     
     /**
      * Link to current application object.
      */
     protected AppComponents.RibbonApplication currentApplication;
     
     /**
      * Define NetWorker collect/execute behavior.
      * <p>Modes:
      * <ul>
      * <li>0 - execute commands;</li>
      * <li>1 - collect single input line</li>
      * <li>2 - collect all line before END: command emmited;</li>
      * </ul>
      * </p>
      */
     protected Integer collectState = 0;
     
     /**
      * String buffer for collected data.
      */
     protected StringBuffer collectBuf = new StringBuffer();
     
     /**
      * Personal collect lock object.
      */
     private final Object collectLock = new Object();
     
     /**
      * Default constructor
      * @param givenApp link application to worker;
      */
     public NetWorker(AppComponents.RibbonApplication givenApp) {
         this.NetListeners = getProtocol();
         currentApplication = givenApp;
         try {
             clientSocket = new java.net.Socket(currentApplication.SERVER_IP, currentApplication.SERVER_PORT);
             inStream = new java.io.BufferedReader(new java.io.InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
             outStream = new java.io.PrintWriter(clientSocket.getOutputStream(), true);
             currentApplication.log(3, "мережевий доступ встановлено (" + currentApplication.SERVER_IP + ":" + currentApplication.SERVER_PORT.toString() + ")");
             isAlive = true;
         } catch (java.io.IOException ex) {
             currentApplication.log(1, "Неможливо з'єднатися з сервером!");
         }
     }
     
     /**
      * Get protocol listeners (should be overriden).
      * @return array of listeners;
      */
     public Listener[] getProtocol() {
         return null;
     }
     
     @Override
     public void run() {
          while (isAlive) {
             try {
                 String inputLine = inStream.readLine();
                 switch (collectState) {
                     case 0:
                         this.exec(inputLine);
                         break;
                     case 1:
                         synchronized (collectLock) {
                             collectBuf.append(inputLine);
                             collectState = 0;
                             collectLock.notify();
                         }
                         break;
                     case 2:
                         synchronized (collectLock) {
                             if (!inputLine.startsWith("END:")) {
                                 collectBuf.append(inputLine + "\n");
                                 if (inputLine.startsWith("RIBBON_ERROR:")) {
                                     collectState = 0;
                                     collectLock.notify();
                                 }
                             } else {
                                 collectState = 0;
                                 collectLock.notify();
                             }
                         }
                 }
             } catch (java.io.IOException ex) {
                 currentApplication.log(1, "Неможливо зчитати дані з сокету!");
                 isAlive = false;
             }
         }
     }
     
     /**
      * Execute command with listeners array.
      * @param inputCommand command to execute;
      */
     private void exec(String inputCommand) {
        String[] parsedCommandStruct = Generic.CsvFormat.parseDoubleStruct(inputCommand);
        Boolean executed = false;
        for (Listener currListener : NetListeners) {
            if (parsedCommandStruct[0].equals(currListener.COMM_NAME)) {
                try {
                    currListener.exec(parsedCommandStruct[1]);
                } catch (Exception ex) {
                    this.currentApplication.log(1, "ошибка при выполнении команды: " + inputCommand);
                    ex.printStackTrace();
                 }
                executed = true;
             }
        }
        if (!executed) {
            currentApplication.log(2, "невідома команда: " + inputCommand);
         }
     }
     
     /**
      * Send command to the server.
      * @param givenCommand command to send;
      */
     public void sendCommand(String givenCommand) {
         outStream.println(givenCommand);
     }
     
     /**
      * Send command and return server status.
      * @param givenCommand command to send;
      * @return return status line from server;
      */
     public String sendCommandWithReturn(String givenCommand) {
         String respond;
         this.collectState = 1;
         outStream.println(givenCommand);
         synchronized (collectLock) {
             while (collectState == 1) {
                 try {
                     collectLock.wait();
                 } catch (InterruptedException ex) {
                     
                 }
             }
             respond = collectBuf.toString();
             collectBuf = new StringBuffer();
         }
         return respond;
     }
     
     /**
      * Send command and get input stream to END: command break.
      * @param givenCommand command to send;
      * @return all lines to END:;
      */
     public String sendCommandWithCollect(String givenCommand) {
         String respond;
         this.collectState = 2;
         outStream.println(givenCommand);
         synchronized (collectLock) {
             while (collectState == 2) {
                 try {
                     collectLock.wait();
                 } catch (InterruptedException ex) {
                     
                 }
             }
             respond = collectBuf.toString();
             collectBuf = new StringBuffer();
         }
         return respond;
     }
     
     /**
      * Collect stream to END: command break.<br>
      * <p><b>WARNING! This method don't use synchronization with <code>socketLock</code>!</b></p>
      * @return all lines to END:;
      */
     public String collectToEnd() {
         StringBuffer buf = new StringBuffer();
         Boolean keepCollect = true;
         while (keepCollect) {
             try {
                 String inputLine = inStream.readLine();
                 if (!inputLine.equals("END:")) {
                     buf.append(inputLine);
                     buf.append("\n");
                 } else {
                     keepCollect = false;
                 }
             } catch (java.io.IOException ex) {
                 currentApplication.log(1, "Неможливо зчитати дані з сокету!");
                 isAlive = false;
             }
         }
         return buf.toString();
     }
 }
