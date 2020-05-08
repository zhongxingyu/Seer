 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
  * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
  * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
  * conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package ac.hw.mytv;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ac.hw.mytv.MyTvClient.CommandHandler;
 
 public class SocketServer extends Thread{
 
 	private ServerSocket server;
 	private Socket client;
 	private PrintWriter out;
 	private BufferedReader in;
 	private int port;
 	private boolean listening = true;
 
 	private static final String GUI_STARTED = "GUI_STARTED";
 	private static final String GUI_STOPPED = "GUI_STOPPED";
 	private static final String USER_ACTION = "USER_ACTION";
 	private static final String CHANNEL_REQUEST = "CHANNEL_REQUEST";
 	private static final String MUTED_REQUEST = "MUTED_REQUEST";
 	private static final String RECEIVED = "RECEIVED";
 	private static final String FAILED = "FAILED";
 	private static final String START_MSG = "START_MSG";
 	private static final String END_MSG = "END_MSG";
 
 	private CommandHandler commandHandler;
 	private Logger LOG = LoggerFactory.getLogger(SocketServer.class);
 
 	public SocketServer(CommandHandler commandHandler){
 		this.commandHandler = commandHandler;
 	}
 	
 	public int setListenPort(){
 		try {
 			ServerSocket portLocator = new ServerSocket(0);
 			port = portLocator.getLocalPort();
 			portLocator.close();
 			LOG.debug("Found available port: "+port);
 			return port;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	@Override
 	public void run(){
 		while(listening){
 			listenSocket();
 		}
 	}
 
 	public void listenSocket(){
 		
 		try {
 			server = new ServerSocket(port);
 		} catch (IOException e) {
 			LOG.debug("ServerSocket creation failed: "+port);
 			LOG.debug(e.getStackTrace().toString());
 			return;
 		}
 		
 		try {
 			LOG.debug("Waiting for connection from GUI on port: "+port);
 			client = server.accept();
 		} catch (IOException e) {
 			LOG.debug("Accept failed: "+port);
 			LOG.debug(e.getStackTrace().toString());
 			return;
 		}
 
 		LOG.debug("Connection accepted from GUI!");
 
 		try {
 			out = new PrintWriter(client.getOutputStream(), true);
 			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
 		} catch (IOException e) {
 			LOG.debug("Accept failed: "+port);
 			LOG.debug(e.getStackTrace().toString());
 			return;
 		}
 
 		try{
 			String start = in.readLine();
 			LOG.debug("Got new input: "+start);
 			if(start.equalsIgnoreCase(START_MSG)){
 				LOG.debug("Processing new message...");
 
 				//loop to get rest of message
 				String message = "";
 				boolean reading = true;
 				while(reading){
 					LOG.debug("running through while again...");
 					String next = in.readLine();
 					LOG.debug("next = "+next);
 					if(!next.equalsIgnoreCase(END_MSG)){
 						LOG.debug("Inside if");
 						message = message+next+"\n";
 					}else{
 						LOG.debug("Inside else");
 						reading  = false;
 					}
 				}
 				LOG.debug("message = "+message);
 
 				//handle message
 				String[] splitData = message.split("\n");
 				LOG.debug("splitData length = "+splitData.length);
 				String command = splitData[0];
 				if (command.equalsIgnoreCase(GUI_STARTED)){
 					LOG.debug(GUI_STARTED+" message received");
 					String gui_ip = splitData[1];
 					commandHandler.connectToGUI(gui_ip);
					out.println(RECEIVED);
 					
 				}else if (command.equalsIgnoreCase(USER_ACTION)){
 					LOG.debug(USER_ACTION+" message received");
 					String parameterName = splitData[1];
 					String value = splitData[2];
 					commandHandler.processUserAction(parameterName, value);
					out.println(RECEIVED);
 					
 				}else if(command.equalsIgnoreCase(CHANNEL_REQUEST)){
 					LOG.debug(CHANNEL_REQUEST+" message received");
 					String response = commandHandler.getChannelPreference();
 					out.println(response);
 
 				}else if(command.equalsIgnoreCase(MUTED_REQUEST)){
 					LOG.debug(MUTED_REQUEST+" message received");
 					String response = commandHandler.getMutedPreference();
 					out.println(response);
 
 				}else if (command.equalsIgnoreCase(GUI_STOPPED)){
 					LOG.debug(GUI_STOPPED+" message received");
					commandHandler.disconnectFromGUI();
 					out.println(RECEIVED);
 				}
 				finalize();
 			}
 		} catch (IOException e) {
 			LOG.debug("Read failed");
 			out.println(FAILED);
 			finalize();
 		}
 	}
 
 
 	@Override
 	protected void finalize(){
 		//Clean up 
 		try{
 			in.close();
 			out.close();
 			server.close();
 		} catch (IOException e) {
 			LOG.debug("Could not close.");
 		}
 	}
 }
