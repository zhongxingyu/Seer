 /**
  * This file is part of RibbonWeb application (check README).
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
 
 package controllers;
 
 /**
  * Basic Ribbon gate prototype implementation.
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public final class MiniGate {
     
     /**
      * Gate status flag.
      */
     public static Boolean isGateReady = false;
     
     /**
      * Gate initiation error string.
      */
     public static String gateErrorStr = "Немає з'язку";
     
     /**
      * Gate connection to the Ribbon server.
      */
     public static GateWorker gate = new GateWorker();
     
     /**
      * Sender for messages.
      */
     public static SenderThread sender;
     
     /**
      * Initialization thread run flag.
      */
     private static Boolean initRun = false;
     
     /**
      * Static initiation at server start.
      */
     static {
         init();
     }
 
     /**
      * Init connection to the server.
      */
     public static void init() {
         //Exit if gate initiated or init thread is running
         if (isGateReady || initRun) {
             return;
         }
         new Thread() {
             @Override
             public void run() {
                 initRun = true;
                 while (true) {
                     //Try to set up sockets.
                     gate.tryConnect();
                     //If sockets initiation failed -> sleep 60 sec and return to begin of cycle
                     if (!gate.isAlive) {
                         try {
                             Thread.sleep(60 * 1000);
                         } catch (InterruptedException ex) {}
                     } else {
                         //Gate starting.
                         gate.start();
                         
                         //Handling connection initiation error
                         //When occurred: if you use server with different version of protocol.
                         String initErr = gate.sendCommandWithCheck("RIBBON_NCTL_INIT:CLIENT,a2,UTF-8");
                         if (initErr != null) {
                             setError(initErr);
                             continue;
                         }
                         
                         //Handling login error
                         //Gate connection must be logined in order to use remote mode of connection
                         //When occurred: when you try to login invalid user or login with invalid password
                         String loginErr = gate.sendCommandWithCheck("RIBBON_NCTL_LOGIN:{root},63a9f0ea7bb98050796b649e85481845");
                         if (loginErr != null) {
                             setError(loginErr);
                             continue;
                         }
                         
                         //Handling remote mode error
                         //When occurred: when user which logined by gate doesn't have rights to enable remote mode
                         String remoteErr = gate.sendCommandWithCheck("RIBBON_NCTL_SET_REMOTE_MODE:1");
                         if (remoteErr != null) {
                             setError(remoteErr);
                             continue;
                         }
                         
                         //Completing initialization
                         isGateReady = true;
                         initRun = false;
                         sender = new SenderThread();
                         sender.start();
                         break;
                     }
                 }
             }
             
             /**
              * Set error string in gate and other stuff.
              * @param error string representation of error
              */
             private void setError(String error) {
                 gate.closeGate();
                 gate = new GateWorker();
                 gateErrorStr = error;
                 initRun = false;
                isGateReady = false;
                 try {
                     Thread.sleep(60 * 1000);
                 } catch (InterruptedException ex) {}
             }
             
         }.start();
     }
     
     /**
      * Get MD5 hash of string.
      * @param givenStr string to hash
      * @return string representation of hash
      */
     public static String getHash(String givenStr) {
         StringBuffer hexString = new StringBuffer();
         try {
             java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
             md.update(givenStr.getBytes());
             byte[] hash = md.digest();
             for (int i = 0; i < hash.length; i++) {
                 if ((0xff & hash[i]) < 0x10) {
                     hexString.append("0"
                             + Integer.toHexString((0xFF & hash[i])));
                 } else {
                     hexString.append(Integer.toHexString(0xFF & hash[i]));
                 }
             }
         } catch (Exception ex) {
         }
         return hexString.toString();
     }
 	
 }
