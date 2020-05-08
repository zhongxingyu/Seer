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
 package org.continuent.appia.test.xml.ecco;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.util.Date;
 
 import org.continuent.appia.core.AppiaEventException;
 import org.continuent.appia.core.Channel;
 import org.continuent.appia.core.Direction;
 import org.continuent.appia.core.Event;
 import org.continuent.appia.core.Session;
 import org.continuent.appia.core.TimeProvider;
 import org.continuent.appia.core.events.channel.ChannelClose;
 import org.continuent.appia.core.events.channel.ChannelInit;
 import org.continuent.appia.core.message.Message;
 import org.continuent.appia.protocols.common.AppiaThreadFactory;
 import org.continuent.appia.protocols.common.RegisterSocketEvent;
 import org.continuent.appia.xml.interfaces.InitializableSession;
 import org.continuent.appia.xml.utils.SessionProperties;
 
 /**
  * This class defines a EccoSession.
  * 
  * @author Jose Mocito
  * @version 1.0
  */
 public class EccoSession extends Session implements InitializableSession {
 	
 	private Channel channel;
     private TimeProvider time;
     
 	private InetSocketAddress local;
 	private InetSocketAddress remote;
 	private int localPort = -1;
     
 	private MyShell shell;
 	
     /**
      * Creates a new EccoSession.
      * @param l
      */
 	public EccoSession(EccoLayer l) {
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
 		String remoteHost = params.getProperty("remotehost");
 		int remotePort = Integer.parseInt(params.getProperty("remoteport"));
 		try {
 			this.remote = 
 				new InetSocketAddress(InetAddress.getByName(remoteHost),remotePort);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
      * Initialization method to be used directly by a static 
      * initialization process
      * @param localPort the local por
      * @param remote the remote address
 	 */
 	public void init(int localPort, InetSocketAddress remote){
         this.localPort = localPort;
 		this.remote = remote;
 	}
 
     /**
      * Main event handler.
     * @param ev the event to handle.
      * 
      * @see org.continuent.appia.core.Session#handle(org.continuent.appia.core.Event)
      */
 	public void handle(Event ev) {
 		if (ev instanceof ChannelInit)
 			handleChannelInit((ChannelInit) ev);
         else if (ev instanceof ChannelClose)
             handleChannelClose((ChannelClose) ev);
 		else if (ev instanceof MyEccoEvent)
 			handleMyEchoEvent((MyEccoEvent) ev);
         else if (ev instanceof RegisterSocketEvent)
             handleRSE((RegisterSocketEvent) ev);
 		else
 			try {
 				ev.go();
 			} catch (AppiaEventException e) {
 				e.printStackTrace();
 			}
 	}
 
     /*
      * ChannelInit
      */
     private void handleChannelInit(ChannelInit init) {
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
             RegisterSocketEvent rse = new RegisterSocketEvent(channel,Direction.DOWN,this,localPort);
             rse.go();
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
         
         local = new InetSocketAddress(event.localHost,event.port);
         
         shell = new MyShell(channel);
         AppiaThreadFactory.getThreadFactory().newThread(shell,"Ecco shell").start();
 
     }
 
     /*
      * EchoEvent
      */
 	private void handleMyEchoEvent(MyEccoEvent echo) {
 		Message message = echo.getMessage();
         
 		if (echo.getDir() == Direction.DOWN) {
             // Event is going DOWN
 			String text = echo.getText();
 			message.pushString(text);
 			
 			echo.source = local;
 			echo.dest = remote;
 			try {
 				echo.setSource(this);
 				echo.init();
 				echo.go();
 			} catch (AppiaEventException e) {
 				e.printStackTrace();
 			}
 		}
 		else {
             // Event is going UP
 			String text = message.popString();
 			echo.setText(text);
             final long now = time.currentTimeMillis();
 			System.out.print("\nOn ["+new Date(now)+"] : "+echo.getText()+"\n> ");
 		}
 	}
 
     /*
      * ChannelClose
      */
     private void handleChannelClose(ChannelClose close) {
         try {
             close.go();
         } catch (AppiaEventException e) {
             e.printStackTrace();
         }
     }
     
 }
