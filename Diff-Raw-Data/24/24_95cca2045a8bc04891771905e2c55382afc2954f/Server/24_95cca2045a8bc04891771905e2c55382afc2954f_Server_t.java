 /**
  * 
  */
 package com.github.ribesg.javachat.server;
 
 /**
  * @author Tom
  * 
  */
 public class Server extends Thread {
 
 	private boolean terminated = false;
 
 	private ConnectedClient[] clients;
 
 	public enum AddResult {
 		OK, FULL, BAD_PASSWORD, ALREADY_CO
 	};
 
 	public Server() {
 		clients = new ConnectedClient[2];
 	}
 
 	public ConnectedClient findClient(long sessionId) {
 		ConnectedClient res = null;
 		if((clients[0] != null && clients[0].getSessionID() == sessionId)) {
 			res = clients[0];
 		} else if ((clients[1] != null && clients[1].getSessionID() == sessionId)) {
 			res = clients[1];
 		}
 		return res;
 	}
 
 	public AddResult addClient(String username, String password, long sessionId) throws Exception {
 		AddResult result;
 		if (findClient(sessionId) == null) {
 			if (match(username, password)) {
 				if (clients[0] == null) {
 					clients[0] = new ConnectedClient(username, sessionId);
 					result = AddResult.OK;
 				} else if (clients[1] == null) {
 					clients[1] = new ConnectedClient(username, sessionId);
 					result = AddResult.OK;
 				} else {
 					result = AddResult.FULL;
 				}
 			} else {
 				result = AddResult.BAD_PASSWORD;
 			}
 		} else {
 			result = AddResult.ALREADY_CO;
 		}
 		return result;
 	}
 
 	private boolean match(String username, String password) {
 		// TODO compare user/pass with values stored in file/DB ?
 		return true;
 	}
 
 
 	
 	
 	
 	
 	@Override
 	public void run() {
 		while (!terminated) {
 			//we check the timeout on active clients
 			if(clients[0] != null && clients[0].timeout())
 				clients[0] = null;
 			if(clients[1] != null && clients[1].timeout())
 				clients[1] = null;
 			
 			
 			
 			
 			
 			try { //TODO : change this to schedule machintruc
 				sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public boolean ping(long sessionId) {
 		boolean res = false;
 		ConnectedClient client;
 		client = findClient(sessionId);
 		if(client  != null) {
 			client.resetTimeout();
 			res = true;
 		}
 		return res;
 	}
 
 }
