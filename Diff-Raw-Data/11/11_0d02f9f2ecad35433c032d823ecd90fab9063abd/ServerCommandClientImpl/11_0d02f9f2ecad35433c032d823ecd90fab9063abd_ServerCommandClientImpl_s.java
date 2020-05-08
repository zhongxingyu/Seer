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
 package net.rptools.maptool.client;
 
 import java.awt.geom.Area;
import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetAvailableListener;
import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Campaign;
 import net.rptools.maptool.model.CellPoint;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Label;
 import net.rptools.maptool.model.Pointer;
 import net.rptools.maptool.model.TextMessage;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.TokenProperty;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.drawing.Drawable;
 import net.rptools.maptool.model.drawing.Pen;
 import net.rptools.maptool.server.ServerCommand;
 import net.rptools.maptool.server.ServerPolicy;
 
 public class ServerCommandClientImpl implements ServerCommand {
 
     private TimedEventQueue movementUpdateQueue = new TimedEventQueue(100);
     private LinkedBlockingQueue<MD5Key> assetRetrieveQueue = new LinkedBlockingQueue<MD5Key>();
 	
 	public ServerCommandClientImpl() {
 		movementUpdateQueue.start();
 //		new AssetRetrievalThread().start();
 	}
 	
 	public void heartbeat(String data) {
 		makeServerCall(COMMAND.heartbeat, data);
 	}
     
 	public void bootPlayer(String player) {
 		makeServerCall(COMMAND.bootPlayer, player);
 	}
     
     public void setCampaign(Campaign campaign) {
     	try {
 	    	campaign.setBeingSerialized(true);
 	        makeServerCall(COMMAND.setCampaign, campaign);
     	} finally {
 	        campaign.setBeingSerialized(false);
     	}
     }
 
     public void updateCampaign(String typeName, List<TokenProperty> propertyList) {
     	makeServerCall(COMMAND.updateCampaign, typeName, propertyList);
     }
     
     public void getZone(GUID zoneGUID) {
         makeServerCall(COMMAND.getZone, zoneGUID);
     }
 
     public void putZone(Zone zone) {
         makeServerCall(COMMAND.putZone, zone);
     }
 
     public void removeZone(GUID zoneGUID) {
         makeServerCall(COMMAND.removeZone, zoneGUID);
     }
 
     public void renameZone(GUID zoneGUID, String name) {
         makeServerCall(COMMAND.renameZone, zoneGUID, name);
     }
     
     public void putAsset(Asset asset) {
         makeServerCall(COMMAND.putAsset, asset);
     }
 
     public void getAsset(MD5Key assetID) {
 		makeServerCall(COMMAND.getAsset, assetID);
     }
 
     public void removeAsset(MD5Key assetID) {
         makeServerCall(COMMAND.removeAsset, assetID);
     }
     
     public void enforceZoneView(GUID zoneGUID, int x, int y, int zoomIndex) {
     	makeServerCall(COMMAND.enforceZoneView, zoneGUID, x, y, zoomIndex);
     }
 
     public void putToken(GUID zoneGUID, Token token) {
         makeServerCall(COMMAND.putToken, zoneGUID, token);
     }
 
     public void removeToken(GUID zoneGUID, GUID tokenGUID) {
         makeServerCall(COMMAND.removeToken, zoneGUID, tokenGUID);
     }
 
     public void putLabel(GUID zoneGUID, Label label) {
         makeServerCall(COMMAND.putLabel, zoneGUID, label);
     }
 
     public void removeLabel(GUID zoneGUID, GUID labelGUID) {
         makeServerCall(COMMAND.removeLabel, zoneGUID, labelGUID);
     }
 
     public void draw(GUID zoneGUID, Pen pen, Drawable drawable) {
         makeServerCall(COMMAND.draw, zoneGUID, pen, drawable);
     }
 
     public void clearAllDrawings(GUID zoneGUID) {
         makeServerCall(COMMAND.clearAllDrawings, zoneGUID);
     }
 
     public void undoDraw(GUID zoneGUID, GUID drawableGUID) {
         makeServerCall(COMMAND.undoDraw, zoneGUID, drawableGUID);
     }
 
     public void setZoneGridSize(GUID zoneGUID, int xOffset, int yOffset, int size, int color) {
         makeServerCall(COMMAND.setZoneGridSize, zoneGUID, xOffset, yOffset, size, color);
     }
     
     public void setZoneVisibility(GUID zoneGUID, boolean visible) {
     	makeServerCall(COMMAND.setZoneVisibility, zoneGUID, visible);
     }
 
     public void message(TextMessage message) {
         makeServerCall(COMMAND.message, message);
     }
 
 	public void showPointer(String player, Pointer pointer) {
 		makeServerCall(COMMAND.showPointer, player, pointer);
 	}
 	
 	public void hidePointer(String player) {
 		makeServerCall(COMMAND.hidePointer, player);
 	}
 
 	public void startTokenMove(String playerId, GUID zoneGUID, GUID tokenGUID, Set<GUID> tokenList) {
 		makeServerCall(COMMAND.startTokenMove, playerId, zoneGUID, tokenGUID, tokenList);
 	}
 
 	public void stopTokenMove(GUID zoneGUID, GUID tokenGUID) {
         movementUpdateQueue.flush();
 		makeServerCall(COMMAND.stopTokenMove, zoneGUID, tokenGUID);
 	}
 	
 	public void updateTokenMove(GUID zoneGUID, GUID tokenGUID, int x, int y) {
 		movementUpdateQueue.enqueue(COMMAND.updateTokenMove, zoneGUID, tokenGUID, x, y);
 	}
 
 	public void toggleTokenMoveWaypoint(GUID zoneGUID, GUID tokenGUID, CellPoint cp) {
     	movementUpdateQueue.flush();
     	makeServerCall(COMMAND.toggleTokenMoveWaypoint, zoneGUID, tokenGUID, cp);
     }
 
 	public void addTopology(GUID zoneGUID, Area area) {
     	makeServerCall(COMMAND.addTopology, zoneGUID, area);
 	}
 	
 	public void removeTopology(GUID zoneGUID, Area area) {
     	makeServerCall(COMMAND.removeTopology, zoneGUID, area);
 	}
 	
 	public void exposeFoW(GUID zoneGUID, Area area) {
     	makeServerCall(COMMAND.exposeFoW, zoneGUID, area);
 	}
 	
 	public void setFoW(GUID zoneGUID, Area area) {
     	makeServerCall(COMMAND.setFoW, zoneGUID, area);
 	}
 	
 	public void hideFoW(GUID zoneGUID, Area area) {
     	makeServerCall(COMMAND.hideFoW, zoneGUID, area);
 	}
 	
 	public void setZoneHasFoW(GUID zoneGUID, boolean hasFog) {
     	makeServerCall(COMMAND.setZoneHasFoW, zoneGUID, hasFog);
 	}
 	
     public void bringTokensToFront(GUID zoneGUID, Set<GUID> tokenList) {
         makeServerCall(COMMAND.bringTokensToFront, zoneGUID, tokenList);
     }
     
     public void sendTokensToBack(GUID zoneGUID, Set<GUID> tokenList) {
         makeServerCall(COMMAND.sendTokensToBack, zoneGUID, tokenList);
     }
     
     public void enforceZone(GUID zoneGUID) {
     	makeServerCall(COMMAND.enforceZone, zoneGUID);
     }
     
     public void setServerPolicy(ServerPolicy policy) {
     	makeServerCall(COMMAND.setServerPolicy, policy);
     }
     
 	private static void makeServerCall(ServerCommand.COMMAND command, Object... params) {
        
        MapTool.getConnection().callMethod(command.name(), params);
     }
     
     /**
      * Some events become obsolete very quickly, such as dragging a token
      * around.  This queue always has exactly one element, the more current
      * version of the event.  The event is then dispatched at some time interval.
      * If a new event arrives before the time interval elapses, it is replaced.
      * In this way, only the most current version of the event is released.
      */
     private static class TimedEventQueue extends Thread {
         
         ServerCommand.COMMAND command;
         Object[] params;
         
         long delay;
 
 		Object sleepSemaphore = new Object();
 		
         public TimedEventQueue(long millidelay) {
             delay = millidelay;
         }
         
         public synchronized void enqueue (ServerCommand.COMMAND command, Object... params) {
             
             this.command = command;
             this.params = params;
         }
 
         public synchronized void flush() {
             
             if (command != null) {
                 makeServerCall(command, params);
             }
             command = null;
             params = null;
         }
         
         public void run() {
             
             while(true) {
              
                 flush();
                 synchronized (sleepSemaphore) {
                     try {
                         Thread.sleep(delay);
                     } catch (InterruptedException ie) {
                         // nothing to do
                     }
                 }
             }
         }
     }
 }
