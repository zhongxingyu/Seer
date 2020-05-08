 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.server;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.rptools.clientserver.hessian.server.ServerConnection;
 import net.rptools.clientserver.simple.server.ServerObserver;
 import net.rptools.maptool.client.ClientCommand;
 import net.rptools.maptool.model.Player;
 
 /**
  * @author trevor
  */
 public class MapToolServerConnection extends ServerConnection  implements ServerObserver {
 
 	private Map<String, Player> playerMap = new ConcurrentHashMap<String, Player>();
 	
 	private MapToolServer server;
 	
 	public MapToolServerConnection(MapToolServer server, int port) throws IOException {
 		super(port);
 		
 		this.server = server;
 		
 		addObserver(this);
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.rptools.clientserver.simple.server.ServerConnection#handleConnectionHandshake(java.net.Socket)
 	 */
 	public boolean handleConnectionHandshake(String id, Socket socket) {
 		
 		try {
 			Player player = Handshake.receiveHandshake(server, socket);
 		
 			if (player != null) {
 				playerMap.put(id.toUpperCase(), player);
 				return true;
 			}
 		} catch (IOException ioe) {
 			// LATER: perhaps log this, or at least keep track for accounting purposes
 		}
 		return false;
 	}
 
 	
 	
 	public Player getPlayer(String id) {
 
 		for (Player player : playerMap.values()) {
 			if (player.getName().equalsIgnoreCase(id)) {
 				return player;
 			}
 		}
 		return null;
 	}
 	
     ////
     // SERVER OBSERVER
     
     /**
      * Handle late connections
      */
     public void connectionAdded(net.rptools.clientserver.simple.client.ClientConnection conn) {
 
     	server.configureConnection(conn.getId());
     	
         for (String id : playerMap.keySet()) {
         	
             server.getConnection().callMethod(conn.getId(), ClientCommand.COMMAND.playerConnected.name(), playerMap.get(id));
         }
         
         server.getConnection().broadcastCallMethod(ClientCommand.COMMAND.playerConnected.name(), playerMap.get(conn.getId().toUpperCase()));
         server.getConnection().callMethod(conn.getId(), ClientCommand.COMMAND.setCampaign.name(), server.getCampaign());
     }
     
     public void connectionRemoved(net.rptools.clientserver.simple.client.ClientConnection conn) {
 
     	server.releaseConnection(conn.getId());
     	
        server.getConnection().broadcastCallMethod(new String[]{conn.getId()}, ClientCommand.COMMAND.playerDisconnected.name(), playerMap.get(conn.getId().toUpperCase()));
         playerMap.remove(conn.getId().toUpperCase());
     }
     
 }
