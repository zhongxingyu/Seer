 /*
  *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package org.apache.yoko.orb.OCI.IIOP;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.yoko.orb.OCI.IIOP.PLUGIN_ID;
 
 final class AccFactory_impl extends org.omg.CORBA.LocalObject implements
         org.apache.yoko.orb.OCI.AccFactory {
 
     // the real logger backing instance.  We use the interface class as the locator
     static final Logger logger = Logger.getLogger(org.apache.yoko.orb.OCI.AccFactory.class.getName());
     //
     // AccFactory information
     //
     private AccFactoryInfo_impl info_;
     
     private org.omg.CORBA.ORB orb_; // The ORB
 
     private ConnectionHelper connectionHelper_;   // client connection helper
 
     private ListenerMap listenMap_;
 
     // ------------------------------------------------------------------
     // Standard IDL to Java Mapping
     // ------------------------------------------------------------------
 
     public String id() {
         return PLUGIN_ID.value;
     }
 
     public int tag() {
         return org.omg.IOP.TAG_INTERNET_IOP.value;
     }
 
     public org.apache.yoko.orb.OCI.AccFactoryInfo get_info() {
         return info_;
     }
 
     public org.apache.yoko.orb.OCI.Acceptor create_acceptor(String[] params)
             throws org.apache.yoko.orb.OCI.InvalidParam {
         String bind = null;
         String[] hosts = null;
         boolean keepAlive = true;
         boolean multiProfile = false;
         int port = 0;
         int backlog = 0;
         boolean numeric = false;
        
         int i = 0;
         while (i < params.length) {
             if (params[i].equals("--backlog")) {
                 if (i + 1 >= params.length)
                     throw new org.apache.yoko.orb.OCI.InvalidParam(
                             "argument expected " + "for --backlog");
                 String arg = params[i + 1];
                 try {
                     backlog = Integer.valueOf(arg).intValue();
                 } catch (NumberFormatException ex) {
                     throw new org.apache.yoko.orb.OCI.InvalidParam(
                             "invalid argument " + "for backlog");
                 }
                 if (backlog < 1 || backlog > 65535)
                     throw new org.apache.yoko.orb.OCI.InvalidParam(
                             "invalid backlog");
                 i += 2;
             } else if (params[i].equals("--bind")) {
                 if (i + 1 >= params.length)
                     throw new org.apache.yoko.orb.OCI.InvalidParam(
                             "argument expected " + "for --bind");
                 bind = params[i + 1];
                 i += 2;
             } else if (params[i].equals("--host")) {
                 if (i + 1 >= params.length)
                     throw new org.apache.yoko.orb.OCI.InvalidParam(
                             "argument expected " + "for --host");
                 java.util.Vector vec = new java.util.Vector();
                 int start = 0;
                 String str = params[i + 1];
                 while (true) {
                     while (start < str.length() && str.charAt(start) == ' ')
                         start++;
                     if (start >= str.length())
                         break;
                     int comma = str.indexOf(',', start);
                     if (comma == start)
                         start++;
                     else {
                         if (comma == -1)
                             comma = str.length();
                         int end = comma - 1;
                         while (str.charAt(end) == ' ')
                             end--;
                         vec.addElement(str.substring(start, end + 1));
                         start = comma + 1;
                     }
                 }
                 if (vec.size() == 0)
                     throw new org.apache.yoko.orb.OCI.InvalidParam(
                             "invalid argument " + "for --host");
                 hosts = new String[vec.size()];
                 vec.copyInto(hosts);
                 i += 2;
             } else if (params[i].equals("--multi-profile")) {
                 multiProfile = true;
                 i++;
             } else if (params[i].equals("--no-keepalive")) {
                 keepAlive = false;
                 i++;
             } else if (params[i].equals("--numeric")) {
                 numeric = true;
                 i++;
             } else if (params[i].equals("--port")) {
                 if (i + 1 >= params.length)
                     throw new org.apache.yoko.orb.OCI.InvalidParam(
                             "argument expected " + "for --port");
                 String arg = params[i + 1];
                 try {
                     port = Integer.valueOf(arg).intValue();
                 } catch (NumberFormatException ex) {
                     throw new org.apache.yoko.orb.OCI.InvalidParam(
                             "invalid argument " + "for port");
                 }
                 if (port < 1 || port > 65535)
                     throw new org.apache.yoko.orb.OCI.InvalidParam(
                             "invalid port");
                 i += 2;
             } else {
                 throw new org.apache.yoko.orb.OCI.InvalidParam(
                         "unknown parameter: " + params[i]);
             }
         }
 
         if (hosts == null) {
             hosts = new String[1];
             hosts[0] = org.apache.yoko.orb.OB.Net.getCanonicalHostname(numeric);
         }
 
         logger.fine("Creating acceptor for port=" + port);
 
         if (bind == null) {
             return new Acceptor_impl(hosts, multiProfile, port, backlog,
                     keepAlive, connectionHelper_, listenMap_);
         }
         else {
             return new Acceptor_impl(bind, hosts, multiProfile, port, backlog,
                     keepAlive, connectionHelper_, listenMap_);
         }
     }
 
     public void change_key(org.omg.IOP.IORHolder ior, byte[] key) {
         //
         // Extract the IIOP profile information from the provided IOR
         //
         for (int profile = 0; profile < ior.value.profiles.length; profile++) {
             if (ior.value.profiles[profile].tag == org.omg.IOP.TAG_INTERNET_IOP.value) {
                 //
                 // Extract the 1_0 profile body
                 //
                 byte[] data = ior.value.profiles[profile].profile_data;
                 org.apache.yoko.orb.OCI.Buffer buf = new org.apache.yoko.orb.OCI.Buffer(
                         data, data.length);
                 org.apache.yoko.orb.CORBA.InputStream in = new org.apache.yoko.orb.CORBA.InputStream(
                         buf, 0, false, null, 0);
                 in._OB_readEndian();
                 org.omg.IIOP.ProfileBody_1_0 body = org.omg.IIOP.ProfileBody_1_0Helper
                         .read(in);
 
                 //
                 // Read components if the IIOP version is > 1.0
                 //
                 org.omg.IOP.TaggedComponent[] components;
                 if (body.iiop_version.major > 1 || body.iiop_version.minor > 0) {
                     int len = in.read_ulong();
                     components = new org.omg.IOP.TaggedComponent[len];
                     for (int j = 0; j < len; j++)
                         components[j] = org.omg.IOP.TaggedComponentHelper
                                 .read(in);
                 } else
                     components = new org.omg.IOP.TaggedComponent[0];
 
                 //
                 // Fill in the new object-key
                 //
                 body.object_key = key;
 
                 //
                 // Remarshal the new body
                 //
                 org.apache.yoko.orb.OCI.Buffer buf2 = new org.apache.yoko.orb.OCI.Buffer();
                 org.apache.yoko.orb.CORBA.OutputStream out = new org.apache.yoko.orb.CORBA.OutputStream(
                         buf2);
                 out._OB_writeEndian();
                 org.omg.IIOP.ProfileBody_1_0Helper.write(out, body);
 
                 //
                 // Remarshal the components if the IIOP version is > 1.0
                 //
                 if (body.iiop_version.major > 1 || body.iiop_version.minor > 0) {
                     out.write_ulong(components.length);
                     for (int i = 0; i < components.length; i++)
                         org.omg.IOP.TaggedComponentHelper.write(out,
                                 components[i]);
                 }
                 ior.value.profiles[profile].profile_data = new byte[buf2
                         .length()];
                 System.arraycopy(buf2.data(), 0,
                         ior.value.profiles[profile].profile_data, 0, buf2
                                 .length());
             }
         }
     }
 
     // ------------------------------------------------------------------
     // Yoko internal functions
     // Application programs must not use these functions directly
     // ------------------------------------------------------------------
 
     public AccFactory_impl(org.omg.CORBA.ORB orb, ListenerMap lm, ConnectionHelper helper) {
         orb_ = orb;
         info_ = new AccFactoryInfo_impl();
         listenMap_ = lm;
         connectionHelper_ = helper;
     }
 }
