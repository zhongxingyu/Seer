 /**
  * Client component for generating load for the KeyValue store. 
  * This is also used by the Master server to reach the slave nodes.
  * 
  * @author Mosharaf Chowdhury (http://www.mosharaf.com)
  * @author Prashanth Mohan (http://www.cs.berkeley.edu/~prmohan)
  * 
  * Copyright (c) 2012, University of California at Berkeley
  * All rights reserved.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *  * Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of University of California, Berkeley nor the
  *    names of its contributors may be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *    
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
  *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 //package edu.berkeley.cs162;
 package edu.berkeley.cs162;
 
 import java.net.Socket;
 
 /** Part II */
 import edu.berkeley.cs162.KVMessage;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import java.io.IOException;
 /** Part II END */
 
 
 /**
  * This class is used to communicate with (appropriately marshalling and unmarshalling) 
  * objects implementing the {@link KeyValueInterface}.
  *
  * @param <K> Java Generic type for the Key
  * @param <V> Java Generic type for the Value
  */
 public class KVClient implements KeyValueInterface {
 	//Fields
 	private String server = null;
 	private int port = 0;
 	
 	//Constructor
 	/**
 	 * @param server is the DNS reference to the Key-Value server
 	 * @param port is the port on which the Key-Value server is listening
 	 */
 	public KVClient(String server, int port) {
 		this.server = server;
 		this.port = port;
 	}
 	
 	//Helper Methods
 	public String getServer() { return this.server; }
 	public int getPort() { return this.port; }
 	
 	//Action Methods
 	/** Part II */
 	private Socket connectHost() throws KVException {
 		Socket socket;
 		try {
 			socket = new Socket(this.server, this.port);
 		}
 		catch (Exception e) {
 			throw new KVException(new KVMessage("resp", "Network Error: Could not create socket"));
 		}
 		return socket;
 	}
 	
 	private void closeHost(Socket sock) throws KVException {
 	    try {
 	    	sock.close();
 	    }
 	    catch (Exception e) {
 	    	throw new KVException(new KVMessage("resp", "Unknown Error: Could not close socket"));
 	    }
 	}
 	
 	public void put(String key, String value) throws KVException {
 	    if (key.length() > 256)
 	    	throw new KVException(new KVMessage("resp", "Oversized key"));
 	    if (value.length() > 256*1024)
 	    	throw new KVException(new KVMessage("resp", "Oversized value"));
 	    
 	    Socket socket = connectHost();
 	    
 	    KVMessage request = null;
 	    KVMessage response = null;
 	    InputStream is = null;
 	    
 	    request = new KVMessage("putreq");
 	    request.setKey(key);
 	    request.setValue(value);
 	    request.sendMessage(socket);
 	    
 	    try {
 	    	is = socket.getInputStream();
 	    }
 	    catch (IOException e){
 	    	throw new KVException(new KVMessage("resp", "Network Error: Could not receive data"));
 	    }
 	    
 	    response = new KVMessage(is);
 	    
 	    if (response.getMessage().equals("Success")) {
             closeHost(socket);
         }
	    throw new KVException(response);
 	}
 
 
 	public String get(String key) throws KVException {
 		if (key.length() > 256)
 	    	throw new KVException(new KVMessage("resp", "Oversized key"));
 		
 		Socket socket = connectHost();
 		
 	    String value = null;
 	    KVMessage request = null;
 	    KVMessage response = null;
 	    InputStream is = null;
 	    
 	    request = new KVMessage("getreq");
 	    request.setKey(key);
 	    request.sendMessage(socket);
 	    
 	    try {
 	    	is = socket.getInputStream();
 	    }
 	    catch (IOException e) {
 	    	throw new KVException(new KVMessage("resp", "Network Error: Could not receive data"));
 	    }
 	    
 	    response = new KVMessage(is);
 	    value = response.getValue();
 	    if (value != null) {
             closeHost(socket);
 	    	return value;
         }
 	    throw new KVException(response);
 	}
 	
 	public void del(String key) throws KVException {
 		if (key.length() > 256)
 	    	throw new KVException(new KVMessage("resp", "Oversized value"));
 		
 		Socket socket = connectHost();
 		
 		KVMessage request = null;
 		KVMessage response = null;
 		InputStream is = null;
 		
 		request = new KVMessage("delreq");
 		request.setKey(key);
 		request.sendMessage(socket);
 		
 		try {
 			is = socket.getInputStream();
 		}
 	    catch (IOException e) {
 	    	throw new KVException(new KVMessage("resp", "Network Error: Could not receive data"));
 	    }
 		
 		response = new KVMessage(is);
 		if (response.getMessage().equals("Success")) {
             closeHost(socket);
 			return;
         }
 		throw new KVException(response);
 	}
 	/** Part II END */
 }
