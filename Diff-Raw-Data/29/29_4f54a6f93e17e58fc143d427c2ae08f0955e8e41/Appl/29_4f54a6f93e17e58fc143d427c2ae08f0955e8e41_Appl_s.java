 /**
  * Appia: Group communication and protocol composition framework library
  * Copyright 2006 University of Lisbon
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  *
  * Initial developer(s): Alexandre Pinto and Hugo Miranda.
  * Contributor(s): See Appia web page for a list of contributors.
  */
  
 package org.continuent.appia.demo;
 
 
 
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.text.ParseException;
 import java.io.*;
 
 import org.continuent.appia.core.*;
 import org.continuent.appia.protocols.common.*;
 import org.continuent.appia.protocols.group.*;
 import org.continuent.appia.protocols.utils.ParseUtils;
 import org.continuent.appia.test.appl.*;
 
 /**
  * Test application for the group communication protocols.
  * @see org.continuent.appia.protocols.group
  * @author Hugo Miranda and Alexandre Pinto
  */
 public class Appl {
   
   public static final int DEFAULT_GOSSIP_PORT=10000;
   public static final int DEFAULT_MULTICAST_PORT=7000;
   
   private static Layer[] qos={
     new org.continuent.appia.protocols.udpsimple.UdpSimpleLayer(),
     //new appia.protocols.fifo.fifomulticast.FifoMulticastLayer(),
     //new appia.protocols.fifo.fifodual.FifoDualLayer(),
     new org.continuent.appia.protocols.fifo.FifoLayer(),
     //new appia.protocols.tcpcomplete.TcpCompleteLayer(),
     //new appia.protocols.sslcomplete.SslCompleteLayer(),
     new org.continuent.appia.protocols.group.bottom.GroupBottomLayer(),
     new org.continuent.appia.protocols.group.heal.GossipOutLayer(),
     new org.continuent.appia.protocols.group.suspect.SuspectLayer(),
     new org.continuent.appia.protocols.group.intra.IntraLayer(),
     new org.continuent.appia.protocols.group.inter.InterLayer(),
     new org.continuent.appia.protocols.group.heal.HealLayer(),
     new org.continuent.appia.protocols.group.stable.StableLayer(),
     new org.continuent.appia.protocols.group.leave.LeaveLayer(),
     new org.continuent.appia.protocols.group.sync.VSyncLayer(),
     new ApplLayer(),
   };
   
   public static void main(String args[]) {
     int i;
     int port=-1;
     InetSocketAddress multicast=null;
     InetSocketAddress[] gossips=null;
     InetSocketAddress[] viewAddrs=null;
     Group group=null;
     LineNumberReader file=null;
     
     boolean ssl=false;
     String keystoreFile=null;
     String keystorePass=null;
     
     for (i=0 ; i < args.length ; i++) {
       
       // PORT
       if(args[i].equals("-port")) {
         if (++i >= args.length)
           argInvalid("missing port number");
         try {
           port=Integer.parseInt(args[i]);
         } catch(NumberFormatException ex) {
           argInvalid("invalid port: "+args[i]);
         }
         
         // GOSSIP
       } else if (args[i].equals("-gossip")) {
         if (++i >= args.length)
           argInvalid("missing gossip server list");
         try {
          gossips=ParseUtils.parseInetWithPortArray(args[i],InetAddress.getLocalHost(),DEFAULT_GOSSIP_PORT);
         } catch (UnknownHostException e) {
           System.err.println("Host unknown: "+e.getMessage());
           System.exit(1);
         } catch (ParseException e) {
           System.err.println("Incorrect gossip server list format: "+e.getMessage());
           System.exit(1);
         } catch (NumberFormatException e) {
           System.err.println("Incorrect port: "+e.getMessage());
           System.exit(1);
         }
         
         // MULTICAST
       } else if (args[i].equals("-multicast")) {
         if (++i >= args.length)
           argInvalid("missing multicast address");
         try {
          multicast=ParseUtils.parseInetWithPort(args[i],null,DEFAULT_MULTICAST_PORT);
         } catch (NumberFormatException ex) {
           argInvalid("invalid port in multicast: \""+args[i]+"\"");
         } catch (java.net.UnknownHostException ex) {
           argInvalid("invalid address in multicast: \""+args[i]+"\"");
         } catch (ParseException e) {
           argInvalid("invalid format in multicast: \""+args[i]+"\"");
         }
         if (!multicast.getAddress().isMulticastAddress())
           argInvalid("not multicast address: \""+args[i]+"\"");
         
         // VIEW
       } else if (args[i].equals("-view")) {
         if (++i >= args.length)
           argInvalid("missing view addresses list");
         try {
           viewAddrs=ParseUtils.parseInetWithPortArray(args[i],null,-1);
         } catch (UnknownHostException e) {
           System.err.println("Host unknown: "+e.getMessage());
           System.exit(1);
         } catch (ParseException e) {
           System.err.println("Incorrect view format: "+e.getMessage());
           System.exit(1);
         } catch (NumberFormatException e) {
           System.err.println("Incorrect port: "+e.getMessage());
           System.exit(1);
         }
         
         // QOS
       } else if (args[i].equals("-qos")) {
         if (++i >= args.length)
           argInvalid("missing qos file name");
         try {
           file=new LineNumberReader(new FileReader(args[i]));
           String s=null;
           int j=0;
           while ((s=file.readLine()) != null) {
             s=s.trim();
             if (s.length() > 0) {
               if (j == qos.length-1) {
                 Layer[] aux=new Layer[qos.length*2];
                 System.arraycopy(qos,0,aux,0,j);
                 qos=aux;
               }
               qos[j++]=(Layer)Class.forName(s).newInstance();
             }
           }
           if (j < qos.length-1) {
             Layer[] aux=new Layer[j+1];
             System.arraycopy(qos,0,aux,0,j);
             qos=aux;
           }
           qos[j]=new ApplLayer();
         } catch (FileNotFoundException ex) {
           argInvalid("qos file not found: \""+args[i]+"\"");
         } catch (IOException ex) {
           ex.printStackTrace();
           argInvalid("impossible to read qos file: \""+args[i]+"\"");
         } catch (ClassNotFoundException ex) {
           argInvalid("class not found: "+ex.getMessage());
         } catch (InstantiationException ex) {
           argInvalid("impossible to create layer: "+ex.getMessage());
         } catch (IllegalAccessException ex) {
           argInvalid("impossible to create layer: "+ex.getMessage());
         }
         
         // SSL
       } else if (args[i].equals("-ssl")) {
         ssl=true;
         
         // SSLKEYS
       } else if (args[i].equals("-sslkeys")) {
         ssl=true;
         if (++i >= args.length)
           argInvalid("missing key store file");
         keystoreFile=args[i];
         if (++i >= args.length)
           argInvalid("missing key store password");
         keystorePass=args[i];
         
         // HELP
       } else if (args[i].equals("-help")) {
         printUsage();
         System.exit(0);
         
         // DEFAULT
       } else {
         argInvalid("Invalid parameters: "+args[i]);
       }
     }
     
 //    for (int k=0 ; k < qos.length ; k++) {
 //      System.out.println(" "+k+": "+qos[k]);
 //    }
     
     /* Create a QoS */
     QoS myQoS=null;
     try {
       myQoS=new QoS("Appl QoS",qos);
     } catch(AppiaInvalidQoSException ex) {
       System.err.println("Invalid QoS");
       System.err.println(ex.getMessage());
       System.exit(1);
     }
     
     /* Create a channel. Uses default event scheduler. */
     Channel myChannel=myQoS.createUnboundChannel("Appl Channel");
     
     /* Application Session requires special arguments: qos and port.
        A session is created and binded to the stack. Remaining ones
        are created by default
      */
     
     ApplSession as=(ApplSession)qos[qos.length-1].createSession();
     if (ssl)
       as.initWithSSL(port, multicast,gossips,group, viewAddrs, keystoreFile, keystorePass);
     else
       as.init(port,multicast,gossips,group,viewAddrs);
     
     ChannelCursor cc=myChannel.getCursor();
     /* Application is the last session of the array. Positioning
        in it is simple */
     try {
       cc.top();
       cc.setSession(as);
     } catch(AppiaCursorException ex) {
       System.err.println("Unexpected exception in main. Type code:"+
       ex.type);
       System.exit(1);
     }
     
     /* Remaining ones are created by default. Just tell the channel to start */
     try {
       myChannel.start();
     } catch(AppiaDuplicatedSessionsException ex) {
       System.err.println("Sessions binding strangely resulted in "+
       "one single sessions occurring more than "+
       "once in a channel");
       System.exit(1);
     }
     
     /* All set. Appia main class will handle the rest */
     Appia.run();
   }
   
  
   private static void argInvalid(String s) {
     System.err.println("Appl: "+s);
     printUsage();
     System.exit(1);
   }
 
   private static void printUsage() {
     System.err.println("Usage: \n\t java Appl [options]");
     System.err.println("Options:"+
     "\n    -port <port>\t\t\tUDP/TCP port"+
     "\n    -gossip <IP:Port>,<IP:Port>,...\tGossip Servers addresses"+
     "\n    -multicast <Multicast_IP:Port>\tMulticast Address used for communication"+
     "\n    -view <IP:Port>,<IP:Port>,...\tInitial members addresses"+
     "\n    -qos <file>\t\t\t\tFile with QoS to use"+
     "\n    -ssl \t\t\t\tIf available uses SSL"+
     "\n    -sslkeys <keystore_file> <keystore_passwd\tIf available uses SSL with the given key store"
     );
   }
 }
