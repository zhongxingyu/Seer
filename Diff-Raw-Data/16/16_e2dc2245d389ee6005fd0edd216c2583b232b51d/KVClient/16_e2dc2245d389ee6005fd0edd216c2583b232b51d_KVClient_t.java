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
 package edu.berkeley.cs162;
 
 import java.net.Socket;
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 
 /**
  * This class is used to communicate with (appropriately marshalling and unmarshalling) 
  * objects implementing the KeyValueInterface.
  *
  * @param <K> Java Generic type for the Key
  * @param <V> Java Generic type for the Value
  */
 public class KVClient implements KeyValueInterface {
 
 	private String server = null;
 	private int port = 0;
	private Socket connection;
 	
 	/**
 	 * @param server is the DNS reference to the Key-Value server
 	 * @param port is the port on which the Key-Value server is listening
 	 */
 	public KVClient(String server, int port) {
 		this.server = server;
 		this.port = port;
 	}
 	
 	private Socket connectHost() throws KVException {
 		try {
			connection = new Socket(server, port);
			return connection;
 		} catch (UnknownHostException e) {
 			KVMessage kmsg = new KVMessage("Network Error: Could not connect");
 			throw new KVException(kmsg);
 		} catch (IOException e) {
 			KVMessage kmsg = new KVMessage("Network Error: Could not create socket");
 			throw new KVException(kmsg);
 		}
 	}
 	
 	private void closeHost(Socket sock) throws KVException {
 	    try {
 	    	sock.close();
 	    } catch (IOException e) {
 	    	KVMessage kmsg = new KVMessage("Unknown Error: Could not closeHost");
 			throw new KVException(kmsg);
 	    }
 	}
 	
 	public boolean put(String key, String value) throws KVException {
 	    Socket sock = connectHost();
 	    KVMessage msg = new KVMessage("putreq");
 	    msg.setKey(key);
 	    msg.setValue(value);
 	    msg.sendMessage(sock);
 	    try {
 	    	KVMessage msgReturned = new KVMessage(sock.getInputStream());
 	    	if (msgReturned.getMessage() != "Error Message") {
 	    		if (msgReturned.getStatus() == "true") {
 	    			closeHost(sock);
 	    			return true;
 	    		}
 	    	}
 	    } catch (IOException e) {
	    	//TODO: not sure what to do here
 	    }
 	    closeHost(sock);
 	    return false;
 	}
 
 	public String get(String key) throws KVException {
 	    Socket sock = connectHost();
 	    KVMessage msg = new KVMessage("getreq");
 	    msg.setKey(key);
 	    msg.sendMessage(sock);
 	    try {
 	    	KVMessage msgReturned = new KVMessage(sock.getInputStream());
 	    	if (msgReturned.getMessage() != "Error Message") {
 	    		closeHost(sock);
 	    		return msgReturned.getValue();	    		
 	    	}
 	    } catch (IOException e) {
 	    	
 	    }
 	    closeHost(sock);
 	    return null;
 	}
 	
 	public void del(String key) throws KVException {
 		Socket sock = connectHost();
 		KVMessage msg = new KVMessage("delreq");
 		msg.setKey(key);
 		msg.sendMessage(sock);
 	    try {
 	    	KVMessage msgReturned = new KVMessage(sock.getInputStream());
 	    	if (msgReturned.getMessage() != "Error Message")
 	    		return;
 	    } catch (IOException e) {
 	    	
 	    }
 	    closeHost(sock);
 	}	
 }
