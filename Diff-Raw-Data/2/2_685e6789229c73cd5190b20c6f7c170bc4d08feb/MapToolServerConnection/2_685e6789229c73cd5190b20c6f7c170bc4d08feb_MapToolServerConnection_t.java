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
 
 import net.rptools.clientserver.hessian.server.ServerConnection;
 import net.rptools.clientserver.simple.server.ServerObserver;
 import net.rptools.maptool.client.MapToolClient;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.server.MapToolServer.COMMANDS;
 
 /**
  * @author trevor
  */
 public class MapToolServerConnection extends ServerConnection  implements ServerObserver {
 
 	private Map<String, Player> playerMap = new HashMap<String, Player>();
 	
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
 			Player player = Handshake.receiveHandshake(socket);
 		
 			playerMap.put(id, player);
 			
 			return true;
 		} catch (IOException ioe) {
 			return false;
 		}
 	}
 
 	
 	
 	public Player getPlayer(String id) {
 
 		return playerMap.get(id);
 	}
 	
     ////
     // SERVER OBSERVER
     
     /**
      * Handle late connections
      */
     public void connectionAdded(net.rptools.clientserver.simple.client.ClientConnection conn) {
     	
     	// Since the server is the first connection observer, this should be called
     	// before any other events can be sent to the client, so it should be inherantly
     	// synchronized for handshaking.  
     	// TODO: Determine if this needs the be synchronized with the actual zone update
     	// events
         server.getConnection().callMethod(conn.getId(), MapToolClient.COMMANDS.setCampaign.name(), server.getCampaign());
 
         for (String id : playerMap.keySet()) {
         	
             server.getConnection().callMethod(conn.getId(), MapToolClient.COMMANDS.playerConnected.name(), playerMap.get(id));
         }
         
         server.getConnection().broadcastCallMethod(MapToolClient.COMMANDS.playerConnected.name(), playerMap.get(conn.getId()));
        server.getConnection().callMethod(conn.getId(), MapToolClient.COMMANDS.setCampaign.name(), server.getCampaign());
     }
     
     public void connectionRemoved(net.rptools.clientserver.simple.client.ClientConnection conn) {
 
         server.getConnection().broadcastCallMethod(MapToolClient.COMMANDS.playerDisconnected.name(), playerMap.get(conn.getId()));
     }
     
 }
