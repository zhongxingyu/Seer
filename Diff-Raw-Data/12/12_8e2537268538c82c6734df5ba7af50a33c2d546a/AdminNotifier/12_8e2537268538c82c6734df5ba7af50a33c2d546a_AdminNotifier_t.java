 /*
  *  The MIT License
  *
  *  Copyright 2011 Sony Ericsson Mobile Communications. All rights reserved.
 *  Copyright 2012 Sony Mobile Communications AB. All rights reserved.
  *
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  *  in the Software without restriction, including without limitation the rights
  *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *  copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  *
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *  THE SOFTWARE.
  */
 package com.sonyericsson.jenkins.plugins.externalresource.dispatcher.utils;
 
 import com.sonyericsson.jenkins.plugins.externalresource.dispatcher.Constants;
 import com.sonyericsson.jenkins.plugins.externalresource.dispatcher.PluginImpl;
 import com.sonyericsson.jenkins.plugins.externalresource.dispatcher.data.ExternalResource;
 import hudson.model.Hudson;
 import hudson.model.Node;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.text.MessageFormat;
 import java.util.Calendar;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Utility for sending notification to administrators when request failed.
  * Record the failed info into an admin file.
  * @author Hu, Jack &lt;jack.hu@sonyericsson.com&gt;
  */
 public final class AdminNotifier {
 
      /**
       * the logger.
       */
     private static final Logger logger = Logger.getLogger(AdminNotifier.class.getName());
 
     private static AdminNotifier instance = new AdminNotifier();
 
     /**
      * This singleton instance.
      *
      * @return the instance.
      */
     public static AdminNotifier getInstance() {
         return instance;
     }
 
     /**
       * the administrator file for record the notification..
       */
     private String adminFile = "";
 
     /**
       * the Random Access File..
       */
     private RandomAccessFile raf = null;
     /**
       * the constructor.
       */
     private AdminNotifier() {
         this.adminFile = PluginImpl.getInstance().getAdminNotifierFile();
 
         if (null == adminFile || adminFile.equals("")) {
             logger.log(Level.WARNING, "Admin Notifier File is not set!");
             String rootDir = Hudson.getInstance().getRootDir().getAbsolutePath();
             String defaultDir = rootDir + "/" + Constants.DEFAULT_ADMIN_NOTIFIER_FILE;
             setAdminFile(defaultDir);
         }
         try {
             File file = new File(adminFile);
             if (!(file.exists())) {
                 String adminFilePath = file.getParent();
                 if (null != adminFilePath && !(adminFilePath.equals(""))) {
                     File p = new File(adminFilePath);
                     if (!(p.exists())) {
                         boolean result = p.mkdirs();
                     }
                 }
             }
             raf = new RandomAccessFile(adminFile, "rw");
         } catch (IOException e) {
             logger.log(Level.WARNING, "Failed to open the Admin Notifier File.", e);
         }
     }
 
     /**
      * Notify the failed message.
      *
      * @param msgType        the message type.
      * @param opType        the operation type.
      * @param node        the external resource in which node.
      * @param er        the external resource which failed.
      * @param msg       the failed info.
      */
 
     public void notify(MessageType msgType, OperationType opType, Node node, ExternalResource er, String msg) {
         String deviceId = "";
         if (null != er) {
             deviceId = er.getId();
         }
         String nodeName = node.getDisplayName();
         String message = MessageFormat.format("{0}, {1}, {2}, {3}, {4}, {5}\n", Calendar.getInstance().getTime(),
                     msgType.toString(), deviceId, opType.toString(), nodeName, msg);
         recordFile(message);
     }
 
     /**
      * Record the failed info into an admin file.
      *
      * @param msg       the failed info.
      */
     private synchronized void recordFile(String msg) {
         try {
            if (raf != null) {
                raf.seek(raf.length());
                raf.writeBytes(msg);
            } else {
                logger.log(Level.WARNING,
                        "Failed to record the following message into admin notifier file: {0} since the file "
                            + "couldn't be opened", msg);
            }
         } catch (IOException e) {
             logger.log(Level.WARNING, MessageFormat.format(
                     "Failed to record the following message into admin notifier file: {0}",
                     msg), e);
         }
     }
 
     /**
       * the setter method.
      *
      * @param adminFile        admin notifier file.        .
       */
     public void setAdminFile(String adminFile) {
         this.adminFile = adminFile;
     }
 
     /**
      * The operation type outgoing.
      */
     public static enum OperationType {
       /**
          * The operation to reserve an external resource.
          */
         RESERVE,
 
       /**
          * The operation to lock an external resource.
          */
         LOCK,
 
       /**
          * The operation to release an external resource.
          */
         RELEASE,
 
       /**
          * The operation to release all the external resources.
          */
         RELEASEALL
     }
 
     /**
      * The message type recording.
      */
     public static enum MessageType {
       /**
          * The Debug type.
          */
         DEBUG,
 
       /**
          * The Info type.
          */
         INFO,
 
       /**
          * The Warring type.
          */
         WARNING,
 
       /**
          * The Error type.
          */
         ERROR
     }
 }
