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
  /*
  * Created on Mar 16, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package net.sf.appia.test.broadcast;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.util.Date;
 
 import net.sf.appia.core.AppiaEventException;
 import net.sf.appia.core.Channel;
 import net.sf.appia.core.Direction;
 import net.sf.appia.core.Event;
 import net.sf.appia.core.Session;
 import net.sf.appia.core.TimeProvider;
 import net.sf.appia.core.events.channel.ChannelClose;
 import net.sf.appia.core.events.channel.ChannelInit;
 import net.sf.appia.core.message.Message;
 import net.sf.appia.protocols.common.RegisterSocketEvent;
 import net.sf.appia.xml.interfaces.InitializableSession;
 import net.sf.appia.xml.utils.SessionProperties;
 
 
 /**
  * This class defines a EccoSession.
  * 
  * @author Jose Mocito
  * @version 1.0
  */
 public class ApplicationSession extends Session implements InitializableSession {
 	
 	private Channel channel;
     private TimeProvider time;
     
 	private InetSocketAddress local;
 	private InetSocketAddress remote;
 	private int localPort = -1;
     
 	private AppShell shell;
 	
     /**
      * Creates a new EccoSession.
      * @param l
      */
 	public ApplicationSession(ApplicationLayer l) {
 		super(l);
 	}
 	
       /**
        * Initializes the session using the parameters given in the XML configuration.
        * Possible parameters:
        * <ul>
        * <li><b>localport</b> the local port to bind.
        * <li><b>remotehost</b> the remote host (IP address).
        * <li><b>remoteport</b> the remote port.
        * </ul>
        * 
        * @param params The parameters given in the XML configuration.
        */
 	public void init(SessionProperties params) {
 		
 	    this.localPort = Integer.parseInt(params.getProperty("localport"));
 		
 		/*
 		final String remoteHost = params.getProperty("remotehost");
 		final int remotePort = Integer.parseInt(params.getProperty("remoteport"));
 		try {
 			this.remote = 
 				new InetSocketAddress(InetAddress.getByName(remoteHost),remotePort);
 			
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}*/
 	}
 	
 	/**
      * Initialization method to be used directly by a static 
      * initialization process
      * @param localPort the local por
      * @param remote the remote address
 	 */
 	public void init(int localPort, InetSocketAddress remote){
         //this.localPort = localPort;
 		//this.remote = remote;
 	}
 
     /**
      * Main event handler.
      * @param ev the event to handle.
      * 
      * @see net.sf.appia.core.Session#handle(net.sf.appia.core.Event)
      */
 	public void handle(Event ev) {
 		if (ev instanceof ChannelInit)
 			handleChannelInit((ChannelInit) ev);
         else if (ev instanceof ChannelClose)
             handleChannelClose((ChannelClose) ev);
 		else if (ev instanceof BroadcastEvent)
 			handleBroadcastEvent((BroadcastEvent) ev);
         else if (ev instanceof RegisterSocketEvent)
             handleRSE((RegisterSocketEvent) ev);
 		else
 			try {
 				ev.go();
 			} catch (AppiaEventException e) {
 				e.printStackTrace();
 			}
 	}
 
     private void handleBroadcastEvent(BroadcastEvent ev) {
         
         final Message message = ev.getMessage();
         String bcastMessage = ev.getBroadcastMessage();
         
         
         if (ev.getDir() == Direction.DOWN) {
             // Event is going DOWN
             message.pushString(bcastMessage);
             
             //echo.source = local;
             //echo.dest = remote;
             
             try {
                 ev.setSourceSession(this);
                 ev.init();
                 ev.go();
             } catch (AppiaEventException e) {
                 e.printStackTrace();
             }
         }
         else {
             // Event is going UP
             System.out.println("WEIRD. DEBUG");
         }
 
     }
 
     /*
      * ChannelInit
      */
     private void handleChannelInit(ChannelInit init) {
         System.out.println("Channel init called");
         channel = init.getChannel();
         time = channel.getTimeProvider();
         try {
             init.go();
         } catch (AppiaEventException e) {
             e.printStackTrace();
         }
         
         /*
          * This event is used to register a socket on the layer that is used 
          * to interface Appia with sockets.
          */
         try {
             new RegisterSocketEvent(channel,Direction.DOWN,this,localPort).go();
         } catch (AppiaEventException e1) {
             e1.printStackTrace();
         }
     }
 
     /*
      * RegisterSocketEvent
      */
 	private void handleRSE(RegisterSocketEvent event) {
         if(event.error){
             System.err.println("Error on the RegisterSocketEvent!!!");
             System.exit(-1);
         }
 
         System.out.println("Reminder: event.localHost not set by us.");
         local = new InetSocketAddress(event.localHost,event.port);
         
         shell = new AppShell(channel);
         final Thread t = event.getChannel().getThreadFactory().newThread(shell);
         t.setName("App shell");
         t.start();
     }
 
  
     /*
      * ChannelClose
      */
     private void handleChannelClose(ChannelClose close) {
         try {
             System.out.println("Channel close called");
             close.go();
         } catch (AppiaEventException e) {
             e.printStackTrace();
         }
     }
     
 }
