 package edu.cs319.dataobjects.impl;
 
 import edu.cs319.dataobjects.DocumentInfo;
 import edu.cs319.server.IServer;
 
 /**
  * Information needed to send requests to the Server
  **/
 public class DocumentInfoImpl implements DocumentInfo {
 	
 	private String user;
 	private String room;
 	private String doc;
 	
 	private IServer server;
 	
 	public DocumentInfoImpl(IServer server, String room, String doc, String user) {
 		this.user = user;
 		this.doc = doc;
 		this.room = room;
 		this.server = server;
 	}
 	
 	/**
 	 * The name of the room this document is part of
 	 * 
 	 * @return The name of the room this document is part of
 	 **/
 	public String getRoomName() {
 		return room;
 	}
 	
 	/**
 	 * The username of this client
 	 * 
 	 * @return The username of this client
 	 **/
 	public String getUserName() {
 		return user;
 	}
 	
 	/**
 	 * The name of this document
 	 * 
 	 * @return The name of this document
 	 **/
 	public String getDocumentName() {
 		return doc;
 	}
 	
 	
 	/**
 	 * The Server we're using
 	 * 
 	 * @return The Server we're using
 	 **/
 	public IServer getServer() {
 		return server;
 	}
 } 
