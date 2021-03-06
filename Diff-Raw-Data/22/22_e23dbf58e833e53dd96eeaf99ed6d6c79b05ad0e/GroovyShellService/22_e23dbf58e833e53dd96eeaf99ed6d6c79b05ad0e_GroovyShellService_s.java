 /*
  * Copyright 2007 Bruce Fancher
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.iterative.groovy.service;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.*;
 
 /**
  * @author Bruce Fancher
  */
 public class GroovyShellService extends GroovyService {
 
 	private ServerSocket serverSocket;
 	private int port;
 	private List<GroovyShellThread> threads = new ArrayList<GroovyShellThread>();
 
 	public GroovyShellService() {
 		super();
 	}
 
 	public GroovyShellService(int port) {
 		super();
 		this.port = port;
 	}
 
 	public GroovyShellService(Map bindings, int port) {
 		super(bindings);
 		this.port = port;
 	}
 
 	public void launch() {
		logger.info("GroovyShellService launch()");

 		try {
 			serverSocket = new ServerSocket(port);
 			logger.info("GroovyShellService launch() serverSocket: " + serverSocket);
 
 			while ( true ) {
				Socket clientSocket = null;
 				try {
 					clientSocket = serverSocket.accept();
 					logger.info("GroovyShellService launch() clientSocket: " + clientSocket);
 				} catch ( IOException e ) {
 					logger.debug("e: " + e);
 					return;
 				}
 
 				GroovyShellThread clientThread = new GroovyShellThread(clientSocket, createBinding());
 				threads.add(clientThread);
 				clientThread.start();
 			}
 		} catch ( IOException e ) {
			logger.debug("e: " + e);
 		} finally {
			try {
				serverSocket.close();
			} catch ( IOException e ) {
				logger.warn("e: " + e);
 			}
			logger.info("GroovyShellService launch() closed connection");
 		}
 	}
 
 	@Override
 	public void destroy() {
 		logger.info("closing serverSocket: " + serverSocket);
 		try {
 			serverSocket.close();
 			for ( GroovyShellThread nextThread : threads ) {
 				logger.info("closing nextThread: " + nextThread);
 				nextThread.getSocket().close();
 			}
 		} catch ( IOException e ) {
 			logger.warn("e: " + e);
 		}
 	}
 
 	public void setPort(final int port) {
 		this.port = port;
 	}
 }
