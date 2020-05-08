 /**
  * Copyright (c) 2012, Nick Harvey
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution.
  * - Neither the name of the <ORGANIZATION> nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without specific prior written permission.
  *   
  *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  *   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  *   ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  *   LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  *   CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  *   SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  *   INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  *   CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  *   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *   POSSIBILITY OF SUCH DAMAGE.
  */
 package com.bluemini.websockets.server;
 
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import com.bluemini.websockets.handlers.SWSSHandler;
 
 public class Server implements Runnable {
 	
 	private boolean keepServing = true;
 	
 	private static int defaultPort = 88;
 	private int listenPort;
 	public SWSSHandler handler;
 	private ArrayList<SocketAddress> connections = new ArrayList<SocketAddress>();
 	private HashSet<String> Hosts = new HashSet<String>();
 	
 	public Server(SWSSHandler handler)
 	{
 		this(defaultPort, handler);
 	}
 	
 	public Server(int port, SWSSHandler handler)
 	{
 		listenPort = port;
 		this.handler = handler;
 	}
 	
 	public void setHost(String host)
 	{
 		Hosts.add(host);
 	}
 	
 	@Override
 	public void run() {
 		ServerSocket server;
 		Socket socket = null;
 		
 		// set up the HTTP server to listen for connections..
 		try
 		{
 			server = new ServerSocket(listenPort);
 		}
 		catch (Exception e)
 		{
 			System.out.println(e.getMessage());
 			return;
 		}
 		
 		System.out.println("listening for connections on port "+listenPort);
 		
 		/* wait for incoming connection. When received, we process it to
 		 * see if we have a matching url to service. 
 		 */
 		while (keepServing) {
 			try
 			{
 				socket = server.accept();
				new WSRequest(this, socket).run();
 			}
 			catch (Exception e)
 			{
 				System.out.println("Server Error: "+e.getMessage());
 				return;
 			}
			finally
 			{
				if (socket != null)
				{
					try
					{
						socket.close();
					}
					catch (Exception e)
					{}
				}
 			}
 		}
 	}
 	
 	synchronized public boolean hasHost(String host)
 	{
 		if (Hosts.contains("*") || Hosts.contains(host))
 		{
 			return true;
 		}
 		return false;
 	}
 	
 }
