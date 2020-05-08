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
 	private static final String CHANNEL_PREFERENCE_REQUEST = "CHANNEL_PREFERENCE_REQUEST";
 	private static final String MUTED_PREFERENCE_REQUEST = "MUTED_PREFERENCE_REQUEST";
 	private static final String CHANNEL_INTENT_REQUEST = "CHANNEL_INTENT_REQUEST";
 	private static final String MUTED_INTENT_REQUEST = "MUTED_INTENT_REQUEST";
 	private static final String RECEIVED = "RECEIVED";
 	private static final String FAILED = "FAILED";
 	private static final String START_MSG = "START_MSG";
 	private static final String END_MSG = "END_MSG";
 

 	private Logger LOG = LoggerFactory.getLogger(SocketServer.class);
 	private MyTvClient myTVClient;
 
 	public SocketServer(MyTvClient myTVClient){
 		this.myTVClient = myTVClient;

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
	//	while(listening){
 			listenSocket();
		//}
 	}
 
 	public void listenSocket(){

 		try {
 			server = new ServerSocket(port);
 		} catch (IOException e) {
 			if(LOG.isDebugEnabled()) LOG.debug("ServerSocket creation failed: "+port);
 			e.printStackTrace();
 			return;
 		}

 		try {
			while(listening)
			{
				if(LOG.isDebugEnabled()) LOG.debug("Waiting for connection from GUI on port: "+port);
				client = server.accept();
				new Thread(new CommsServerAction(client, myTVClient, port));
			}
 		} catch (IOException e) {
 			if(LOG.isDebugEnabled()) LOG.debug("Accept failed: "+port);
 			e.printStackTrace();
 			return;
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
 			if(LOG.isDebugEnabled()) LOG.debug("Could not close.");
 		}
 	}
 }
