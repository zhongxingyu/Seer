 /*-
  * Copyright (c) 2010, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail.util;
 
 import java.io.IOException;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.StreamConnection;
 
 import net.rim.device.api.servicebook.ServiceBook;
 import net.rim.device.api.servicebook.ServiceRecord;
 import net.rim.device.api.system.CoverageInfo;
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.system.RadioInfo;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.conf.ConnectionConfig;
 
 public class ConnectionBB42 extends Connection {
 
     /** Stores transport ServiceBooks if found. Otherwise, null */
     private ServiceRecord srMDS, srWAP2, srWiFi;
     /** Flags indicating the coverage status of each transport */
     protected boolean coverageTCP=false, coverageMDS=false, coverageWAP2=false, coverageWiFi=false;
 
     private int transportSelection = TRANSPORT_AUTO;
     
     /**
      * Initializes a new connection object.
      * 
      * @param connectionConfig Configuration data for the connection
      */
     ConnectionBB42(ConnectionConfig connectionConfig) {
         super(connectionConfig);
     }
 
     protected StreamConnection openStreamConnection() throws IOException {
         initializeTransportAvailability();
         
         StringBuffer buf = new StringBuffer();
         buf.append(useSSL ? "ssl" : "socket");
         buf.append("://");
         buf.append(serverName);
         buf.append(':');
         buf.append(serverPort);
         String urlBase = buf.toString();
         
         StreamConnection connection = null;
         if((transportSelection & TRANSPORT_WIFI) != 0 && srWiFi != null && coverageWiFi) {
             connection = attemptWiFi(urlBase);
         }
         if(connection == null && (transportSelection & TRANSPORT_DIRECT_TCP) != 0 && coverageTCP) {
             connection = attemptDirectTCP(urlBase);
         }
         if(connection == null && (transportSelection & TRANSPORT_MDS) != 0 && srMDS != null && coverageMDS) {
             connection = attemptMDS(urlBase);
         }
         if(connection == null && (transportSelection & TRANSPORT_WAP2) != 0 && srWAP2 != null && coverageWAP2) {
             connection = attemptWAP2(urlBase);
         }
         
         return connection;
     }
 
     /**
      * Initializes the ServiceRecord instances for each transport (if available). Otherwise leaves it null.
      * Also determines if sufficient coverage is available for each transport and sets coverage* flags.
      */
     private void initializeTransportAvailability() {
         ServiceBook sb = ServiceBook.getSB();
         ServiceRecord[] records = sb.getRecords();
 
         for (int i = 0; i < records.length; i++) {
             ServiceRecord myRecord = records[i];
             String cid, uid;
 
             if (myRecord.isValid() && !myRecord.isDisabled()) {
                 cid = myRecord.getCid().toLowerCase();
                 uid = myRecord.getUid().toLowerCase();
                 // BES
                 if (cid.indexOf("ippp") != -1 && uid.indexOf("gpmds") == -1) {
                     srMDS = myRecord;
                 }
                 // WiFi
                 if (cid.indexOf("wptcp") != -1 && uid.indexOf("wifi") != -1) {
                     srWiFi = myRecord;
                 }               
                 // Wap2
                 if (cid.indexOf("wptcp") != -1 && uid.indexOf("wap2") != -1) {
                     srWAP2 = myRecord;
                 }
             }   
         }
         
         initializeCoverage();
     }
 
     /**
      * Coverage check APIs change on different OS versions, so this method
      * exists to allow subclasses to override as necessary.
      */
     protected void initializeCoverage() {
        // CoverageInfo.COVERAGE_CARRIER does not exist on newer API versions,
        // which would have prevented this method from compiling.  However,
        // since its value and the newer constant are both 1, we will just
        // use the value directly.
        int COVERAGE_CARRIER = 1;
        if(CoverageInfo.isCoverageSufficient(COVERAGE_CARRIER)){
             coverageTCP=true;
             coverageWAP2=true;
         }
         if(CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_MDS)){           
             coverageMDS=true;
         }   
        if(CoverageInfo.isCoverageSufficient(COVERAGE_CARRIER, RadioInfo.WAF_WLAN, false)) {
             coverageWiFi = true;
         }
     }
     
     private StreamConnection attemptWiFi(String urlBase) {
         String connectStr = urlBase + ";interface=wifi";
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID, "Attempting WiFi".getBytes(),
                     EventLogger.DEBUG_INFO);
         }
         StreamConnection socket = openSocket(connectStr);
         return socket;
     }
 
     private StreamConnection attemptDirectTCP(String urlBase) {
         String connectStr = urlBase + ";deviceside=true";
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID, "Attempting Direct TCP".getBytes(),
                     EventLogger.DEBUG_INFO);
         }
         StreamConnection socket = openSocket(connectStr);
         return socket;
     }
 
     private StreamConnection attemptMDS(String urlBase) {
         String connectStr = urlBase + ";deviceside=false";
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID, "Attempting MDS".getBytes(),
                     EventLogger.DEBUG_INFO);
         }
         StreamConnection socket = openSocket(connectStr);
         return socket;
     }
 
     private StreamConnection attemptWAP2(String urlBase) {
         String connectStr = urlBase + ";deviceside=true;ConnectionUID=" + srWAP2.getUid();
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID, "Attempting WAP2".getBytes(),
                     EventLogger.DEBUG_INFO);
         }
         StreamConnection socket = openSocket(connectStr);
         return socket;
     }
 
     private StreamConnection openSocket(String connectStr) {
         StreamConnection socket;
         try {
             socket = (StreamConnection) Connector.open(
                     connectStr,
                     Connector.READ_WRITE, true);
             if (EventLogger.getMinimumLevel() >= EventLogger.INFORMATION) {
                 String msg = "Opened connection:\r\n" + connectStr + "\r\n";
                 EventLogger.logEvent(AppInfo.GUID, msg.getBytes(),
                         EventLogger.INFORMATION);
             }
         } catch (IOException e) {
             EventLogger.logEvent(AppInfo.GUID, e.getMessage().getBytes(), EventLogger.ERROR);
             socket = null;
         }
         return socket;
     }
 }
