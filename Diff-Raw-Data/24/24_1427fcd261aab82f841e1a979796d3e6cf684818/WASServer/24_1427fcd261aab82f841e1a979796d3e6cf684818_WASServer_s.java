 /*
  * The MIT License
  *
 * Copyright (c) 2009-2010, Manufacture Française des Pneumatiques Michelin, Romain Seguy
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.michelin.cio.hudson.plugins.wasbuilder;
 
 import hudson.util.Secret;
 import java.util.Arrays;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 /**
  * Corresponds to WAS application server.
  *
  * <p>To use a {@link WASServer}, it's necessary to couple it to a {@link
  * WASInstallation} whom {@code wsadmin} executable is used.<p>
  * <p>Connection settings are set at the {@link WASServer} level, for example to
  * ensure the user can't directly access the password.</p>
  *
  * @author Romain Seguy (http://openromain.blogspot.com)
  */
 public class WASServer {
 
     // We don't allow the use of IPC for the moment
     //public final static String CONNTYPE_IPC = "IPC";
     public final static String CONNTYPE_JSR160RMI = "JSR160RMI";
     public final static String CONNTYPE_RMI = "RMI";
     public final static String CONNTYPE_SOAP = "SOAP";  // this is the preferred conntype
     public final static String[] CONNTYPES = { CONNTYPE_SOAP, CONNTYPE_RMI, CONNTYPE_JSR160RMI };
 
     private final String wasInstallationName;
     private final String name;
     private final String conntype;
     private final String host;
     private final int port;
     private final String user;
     private Secret password;    // this one can't be final, otherwise we get some NullPointerExceptions when using it
 
     @DataBoundConstructor
     public WASServer(String wasInstallationName, String name, String conntype, String host, int port, String user, String password) {
         this.wasInstallationName = wasInstallationName;
         this.name = name;
         if(conntype == null || !Arrays.asList(CONNTYPES).contains(conntype)) {
             // we may get here if the user has manually modified the config file
             // default assumed connection type: SOAP
             this.conntype = CONNTYPE_SOAP;
         }
         else {
             this.conntype = conntype;
         }
         this.host = host;
         this.port = port;
         this.user = user;
         this.password = Secret.fromString(password);
     }
 
     public String getConntype() {
         return conntype;
     }
 
     public String getHost() {
         return host;
     }
 
     public String getName() {
         return name;
     }
 
     public String getPassword() {
         return Secret.toString(password);
     }
 
     public int getPort() {
         return port;
     }
 
     public String getUser() {
         return user;
     }
 
     public WASInstallation getWasInstallation() {
         return WASInstallation.getWasInstallationByName(getWasInstallationName());
     }
 
     public String getWasInstallationName() {
         return wasInstallationName;
     }
 
 }
