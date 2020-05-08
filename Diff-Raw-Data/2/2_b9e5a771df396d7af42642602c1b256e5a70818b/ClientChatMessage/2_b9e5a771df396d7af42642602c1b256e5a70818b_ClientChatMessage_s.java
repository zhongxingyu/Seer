 package com.friendlyblob.mayhemandhell.server.network.packets.client;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.friendlyblob.mayhemandhell.server.model.World;
 import com.friendlyblob.mayhemandhell.server.model.actors.GameCharacter;
 import com.friendlyblob.mayhemandhell.server.model.actors.Player;
 import com.friendlyblob.mayhemandhell.server.network.packets.ClientPacket;
 import com.friendlyblob.mayhemandhell.server.network.packets.server.ChatMessageNotify;
 
 public class ClientChatMessage extends ClientPacket {
 
 	String msg;
 
 //	  	TALK(0),
 //	  	WHISPER(1),
 //	  	GUILD(2),
 //	  	PARTY(3),
 //	  	BROADCAST(4);
 	
 	@Override
 	protected boolean read() {
 		this.msg = readS().trim();
 	
 		return true;
 	}
 
 	@Override
 	public void run() {
 		// Parse message
 		if (msg.length() > 0) {
 			String playerName = getClient().getPlayer().getName();
     		
     		ChatMessageNotify packet;
     		String actualMessage;
 			try {
 				// check whether we should try to parse a command
 				if (msg.startsWith("/")) {
 					int firstSpace = msg.indexOf(" ");
 					String cmd = msg.substring(1, firstSpace);
 					String params = msg.substring(firstSpace + 1).trim();
 
 					switch (cmd) {
 	    	    		case "b":
 		    				if (params.length() > 0) {
 			    				packet = new ChatMessageNotify(playerName, params, 4);
 								
 								ConcurrentHashMap<Integer, GameCharacter> players = World.getInstance().getPlayers();
 								
 								for (GameCharacter character : players.values()) {
 								    character.sendPacket(packet);
 								}
 		    				}
 
 	    	    			break;
 	    	    		case "w":
 	    	    			String recipientPlayer = params.substring(0, params.indexOf(" "));
         					actualMessage = params.substring(recipientPlayer.length()+1);
 
 	    					if (recipientPlayer.trim().length() > 0 && actualMessage.trim().length() > 0) {
 	        					GameCharacter gc = World.getInstance().getPlayersByNames().get(recipientPlayer);
 	            				
 	        					if (gc != null) {
	            					packet = new ChatMessageNotify(gc.getName(), actualMessage, 1);
 	            					
 	            					// Send to recipient   						
 	        						((Player) gc).sendPacket(packet);
 	            					
 	            					// Send to itself
 	            					getClient().getPlayer().sendPacket(packet);
 	        					}
 	    					}
 
 	    	    			break;
 	    	    		case "g":
 	    	    			// TODO: implement when guilds added
 	    	    			break;
 	    	    		case "p":
 	    	    			// TODO implement when parties added
 	    	    			break;
 	    				default:
 	        		}
 				} else {
 					// TALK
 					packet = new ChatMessageNotify(playerName, msg, 0);
 					getClient().getPlayer().getRegion().broadcastToCloseRegions(packet);
 				}
 			} catch (StringIndexOutOfBoundsException e) {
 				System.out.println("String received: " + msg);
 			}
 		}
 	}
 }
