 package com.prcse.utils;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Observable;
 import java.util.Observer;
 
 import com.prcse.datamodel.Artist;
 import com.prcse.protocol.CustomerInfo;
 import com.prcse.protocol.FrontPage;
 
 public class PrcseConnection extends Observable implements Connectable, PrcseSource {
 	
     private Socket socket = null;
     private ObjectOutputStream out = null;
     private ObjectInputStream in = null;
     private int clientId = 0;
     private String host;
     private int port;
     private String error;
 	
 	public PrcseConnection(String host, int port) {
 		super();
 		this.host = host;
 		this.port = port;
 	}
 
 	@Override
 	public void connect() throws Exception {
         socket = new Socket(host, port);
         out = new ObjectOutputStream(socket.getOutputStream());
         in = new ObjectInputStream(socket.getInputStream());
         clientId = ((Integer)in.readObject()).intValue();
 	}
 
 	@Override
 	public void disconnect() throws Exception {
 		out.close();
 		in.close();
 		socket.close();
 		socket = null;
 	}
 
 	@Override
 	public boolean isConnected() {
 		return socket != null;
 	}
 	
 	@Override
	public ArrayList<Object> getFrontPage() throws Exception {
 		out.writeObject(new FrontPage());
 		try {
 			FrontPage response = (FrontPage)in.readObject();
 			if(response.getError() != null) {
 				error = response.getError();
 			}
 			else {
 				return response.getArtists();
 			}
 		}
 		catch (ClassNotFoundException e) {
 			error = e.getMessage();
 		}
 		return null;
 	}
 
 	@Override
 	public CustomerInfo login(CustomerInfo request) throws Exception {
 		out.writeObject(request);
 		try {
 			CustomerInfo response = (CustomerInfo)in.readObject();
 			if(response.getError() != null) {
 				error = response.getError();
 			}
 			else {
 				return response;
 			}
 		}
 		catch (ClassNotFoundException e) {
 			error = e.getMessage();
 		}
 		return null;
 	}
 
 	@Override
 	public CustomerInfo syncCustomer(CustomerInfo request) throws Exception {
 		out.writeObject(request);
 		try {
 			CustomerInfo response = (CustomerInfo)in.readObject();
 			if(response.getError() != null) {
 				error = response.getError();
 			}
 			else {
 				return response;
 			}
 		}
 		catch (ClassNotFoundException e) {
 			error = e.getMessage();
 		}
 		return null;
 	}
 }
