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
 
 import java.awt.geom.Area;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import net.rptools.clientserver.hessian.AbstractMethodHandler;
 import net.rptools.lib.MD5Key;
 import net.rptools.maptool.client.ClientCommand;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Campaign;
 import net.rptools.maptool.model.CellPoint;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Grid;
 import net.rptools.maptool.model.Label;
 import net.rptools.maptool.model.Pointer;
 import net.rptools.maptool.model.TextMessage;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.drawing.Drawable;
 import net.rptools.maptool.model.drawing.DrawnElement;
 import net.rptools.maptool.model.drawing.Pen;
 
 /**
  * @author drice
  */
 public class ServerMethodHandler extends AbstractMethodHandler implements ServerCommand {
     private final MapToolServer server;
     
     private final Object MUTEX = new Object();
     
     public ServerMethodHandler(MapToolServer server) {
         this.server = server;
     }
 
     public void handleMethod(String id, String method, Object[] parameters) {
         ServerCommand.COMMAND cmd = Enum.valueOf(ServerCommand.COMMAND.class, method);
         //System.out.println("ServerMethodHandler#handleMethod: " + id + " - " + cmd.name());
 
         try {
             RPCContext context = new RPCContext(id, method, parameters); 
             RPCContext.setCurrent(context);
             
         	switch (cmd) {
             
             case bringTokensToFront:      bringTokensToFront(context.getGUID(0), (Set<GUID>) context.get(1)); break;
             case draw:                    draw(context.getGUID(0), (Pen) context.get(1), (Drawable) context.get(2)); break;
             case enforceZoneView:         enforceZoneView(context.getGUID(0), context.getInt(1), context.getInt(2), context.getInt(3)); break;
             case exposeFoW:               exposeFoW(context.getGUID(0), (Area) context.get(1)); break;
             case getAsset:                getAsset((MD5Key) context.get(0)); break;
             case getZone:                 getZone(context.getGUID(0)); break;
             case hideFoW:                 hideFoW(context.getGUID(0), (Area) context.get(1)); break;
             case hidePointer:             hidePointer(context.getString(0)); break;
             case message:                 message((TextMessage)context.get(0)); break;
             case putAsset:                putAsset((Asset) context.get(0)); break;
             case putLabel:                putLabel(context.getGUID(0), (Label) context.get(1)); break;
             case putToken:                putToken(context.getGUID(0), (Token) context.get(1)); break;
             case putZone:                 putZone((Zone) context.get(0)); break;
             case removeZone:              removeZone(context.getGUID(0)); break;
             case removeAsset:             removeAsset((MD5Key) context.get(0)); break;
             case removeToken:             removeToken(context.getGUID(0), context.getGUID(1)); break;
             case removeLabel:             removeLabel(context.getGUID(0), context.getGUID(1)); break;
             case sendTokensToBack:        sendTokensToBack(context.getGUID(0), (Set<GUID>) context.get(1)); break;
             case setCampaign:             setCampaign((Campaign) context.get(0)); break;
             case setZoneGridSize:         setZoneGridSize(context.getGUID(0), context.getInt(1), context.getInt(2), context.getInt(3), context.getInt(4)); break;
             case setZoneVisibility:       setZoneVisibility(context.getGUID(0), (Boolean) context.get(1)); break;
             case setZoneHasFoW:           setZoneHasFoW(context.getGUID(0), context.getBool(1)); break;
             case showPointer:             showPointer(context.getString(0), (Pointer) context.get(1)); break;
             case startTokenMove:          startTokenMove(context.getString(0), context.getGUID(1), context.getGUID(2), (Set<GUID>) context.get(3)); break;
             case stopTokenMove:           stopTokenMove(context.getGUID(0), context.getGUID(1)); break;
             case toggleTokenMoveWaypoint: toggleTokenMoveWaypoint(context.getGUID(0), context.getGUID(1), (CellPoint)context.get(2)); break;
             case undoDraw:                undoDraw(context.getGUID(0), context.getGUID(1)); break;
             case updateTokenMove:         updateTokenMove(context.getGUID(0), context.getGUID(1), context.getInt(2), context.getInt(3)); break;
             case clearAllDrawings:        clearAllDrawings(context.getGUID(0)); break;
             case enforceZone:			  enforceZone(context.getGUID(0));break;
             case setServerPolicy:		  setServerPolicy((ServerPolicy) context.get(0));break;
             }
         } finally {
             RPCContext.setCurrent(null);
         }
     }
 
     /**
      * Send the current call to all other clients except for the sender
      */
     private void forwardToClients() {
         server.getConnection().broadcastCallMethod(new String[] { RPCContext.getCurrent().id }, RPCContext.getCurrent().method, RPCContext.getCurrent().parameters);
     }
 
     /**
      * Send the current call to all clients including the sender
      */
     private void forwardToAllClients() {
         server.getConnection().broadcastCallMethod(new String[] {}, RPCContext.getCurrent().method, RPCContext.getCurrent().parameters);
     }
 
     private void broadcastToClients(String exclude, String method, Object... parameters) {
         server.getConnection().broadcastCallMethod(new String[] { exclude }, method, parameters);
     }
     
     private void broadcastToAllClients(String method, Object... parameters) {
         server.getConnection().broadcastCallMethod(new String[] {}, method, parameters);
     }
     
     ////
     // SERVER COMMAND
     public void enforceZone(GUID zoneGUID) {
     	forwardToClients();
     }
     
     public void bringTokensToFront(GUID zoneGUID, Set<GUID> tokenSet) {
         synchronized (MUTEX) {
 
             Zone zone = server.getCampaign().getZone(zoneGUID);
 
             // Get the tokens to update
             List<Token> tokenList = new ArrayList<Token>();
             for (GUID tokenGUID : tokenSet) {
                 Token token = zone.getToken(tokenGUID);
                 
                 if (token != null) {
                     tokenList.add(token);
                 }
             }
             
             // Arrange
             Collections.sort(tokenList, Zone.TOKEN_Z_ORDER_COMPARATOR);
             
             // Update
             int z = zone.getLargestZOrder() + 1;
             for (Token token : tokenList) {
                 token.setZOrder(z ++);
             }
             
             // Broadcast
             for (Token token : tokenList) {
                 broadcastToAllClients(ClientCommand.COMMAND.putToken.name(), zoneGUID, token);
             }
         }
     }
     
     public void clearAllDrawings(GUID zoneGUID) {
     	
         Zone zone = server.getCampaign().getZone(zoneGUID);
         zone.getDrawnElements().clear();
 
     	forwardToAllClients();
     }
     
     public void draw(GUID zoneGUID, Pen pen, Drawable drawable) {
         
         server.getConnection().broadcastCallMethod(ClientCommand.COMMAND.draw.name(), RPCContext.getCurrent().parameters);
         
         Zone zone = server.getCampaign().getZone(zoneGUID);
         
         zone.addDrawable(new DrawnElement(drawable, pen));
     }
     
     public void enforceZoneView(GUID zoneGUID, int x, int y, int zoomIndex) {
        forwardToClients();
     }
     
     public void exposeFoW(GUID zoneGUID, Area area) {
 
         Zone zone = server.getCampaign().getZone(zoneGUID);
         zone.exposeArea(area);
 
         server.getConnection().broadcastCallMethod(ClientCommand.COMMAND.exposeFoW.name(), RPCContext.getCurrent().parameters);
     }
 
     public void getAsset(MD5Key assetID) {
         server.getConnection().callMethod(RPCContext.getCurrent().id, ClientCommand.COMMAND.putAsset.name(), AssetManager.getAsset(assetID));
     }
     
     public void getZone(GUID zoneGUID) {
         server.getConnection().callMethod(RPCContext.getCurrent().id, ClientCommand.COMMAND.putZone.name(), server.getCampaign().getZone(zoneGUID));
     }
     
     public void hideFoW(GUID zoneGUID, Area area) {
 
         Zone zone = server.getCampaign().getZone(zoneGUID);
         zone.hideArea(area);
 
         server.getConnection().broadcastCallMethod(ClientCommand.COMMAND.hideFoW.name(), RPCContext.getCurrent().parameters);
     }
 
     public void hidePointer(String player) {
         forwardToAllClients();
     }
     
     public void message(TextMessage message) {
         forwardToClients();
     }
     
     public void putAsset(Asset asset) {
         AssetManager.putAsset(asset);
     }
     
     public void putLabel(GUID zoneGUID, Label label) {
 
         Zone zone = server.getCampaign().getZone(zoneGUID);
         zone.putLabel(label);
         
         forwardToClients();
     }
 
     public void putToken(GUID zoneGUID, Token token) {
 
         Zone zone = server.getCampaign().getZone(zoneGUID);
 
         boolean newToken = zone.getToken(token.getId()) == null;
         synchronized (MUTEX) {
 	        // Set z-order for new tokens
 	        if (newToken) {
 	        	token.setZOrder(zone.getLargestZOrder() + 1);
 	        }
 	        
 	        zone.putToken(token);
         }
         
         if (newToken) {
         	forwardToAllClients();
         } else {
         	forwardToClients();
         }
     }
     
     public void putZone(Zone zone) {
 
         server.getCampaign().putZone(zone);
         forwardToClients();
     }
     
     public void removeAsset(MD5Key assetID) {
         AssetManager.removeAsset(assetID);
     }
     
     public void removeLabel(GUID zoneGUID, GUID labelGUID) {
 
         Zone zone = server.getCampaign().getZone(zoneGUID);
         zone.removeLabel(labelGUID);
         
         server.getConnection().broadcastCallMethod(ClientCommand.COMMAND.removeLabel.name(), RPCContext.getCurrent().parameters);
     }
     
     public void removeToken(GUID zoneGUID, GUID tokenGUID) {
 
         Zone zone = server.getCampaign().getZone(zoneGUID);
         zone.removeToken(tokenGUID);
         
         server.getConnection().broadcastCallMethod(ClientCommand.COMMAND.removeToken.name(), RPCContext.getCurrent().parameters);
     }
     
     public void removeZone(GUID zoneGUID) {
         server.getCampaign().removeZone(zoneGUID);
         forwardToClients();
     }
     
     public void sendTokensToBack(GUID zoneGUID, Set<GUID> tokenSet) {
         synchronized (MUTEX) {
             
             Zone zone = server.getCampaign().getZone(zoneGUID);
 
             // Get the tokens to update
             List<Token> tokenList = new ArrayList<Token>();
             for (GUID tokenGUID : tokenSet) {
                 Token token = zone.getToken(tokenGUID);
                 
                 if (token != null) {
                     tokenList.add(token);
                 }
             }
             
             // Arrange
             Collections.sort(tokenList, Zone.TOKEN_Z_ORDER_COMPARATOR);
             
             // Update
             int z = zone.getSmallestZOrder() - 1;
             for (Token token : tokenList) {
                 token.setZOrder(z --);
             }
             
             // Broadcast
             for (Token token : tokenList) {
                 broadcastToAllClients(ClientCommand.COMMAND.putToken.name(), zoneGUID, token);
             }
         }
     }
     
     public void setCampaign(Campaign campaign) {
 
         server.setCampaign(campaign);
         forwardToClients();
     }
     
     public void setZoneGridSize(GUID zoneGUID, int offsetX, int offsetY, int size, int color) {
 
         Zone zone = server.getCampaign().getZone(zoneGUID);
         Grid grid = zone.getGrid();
         grid.setSize(size);
         grid.setOffset(offsetX, offsetY);
         zone.setGridColor(color);
         
         server.getConnection().broadcastCallMethod(ClientCommand.COMMAND.setZoneGridSize.name(), RPCContext.getCurrent().parameters);
     }
 
     public void setZoneHasFoW(GUID zoneGUID, boolean hasFog) {
         
         Zone zone = server.getCampaign().getZone(zoneGUID);
         zone.setHasFog(hasFog);
 
         server.getConnection().broadcastCallMethod(ClientCommand.COMMAND.setZoneHasFoW.name(), RPCContext.getCurrent().parameters);
     }
 
     public void setZoneVisibility(GUID zoneGUID, boolean visible) {
 
         server.getCampaign().getZone(zoneGUID).setVisible(visible);
         
         server.getConnection().broadcastCallMethod(ClientCommand.COMMAND.setZoneVisibility.name(), RPCContext.getCurrent().parameters);
     }
 
     public void showPointer(String player, Pointer pointer) {
         server.getConnection().broadcastCallMethod(ClientCommand.COMMAND.showPointer.name(), RPCContext.getCurrent().parameters);
     }
 
     public void startTokenMove(String playerId, GUID zoneGUID, GUID tokenGUID, Set<GUID> tokenList) {
         forwardToClients();
     }
 
     public void stopTokenMove(GUID zoneGUID, GUID tokenGUID) {
         forwardToClients();
     }
 
     public void toggleTokenMoveWaypoint(GUID zoneGUID, GUID tokenGUID, CellPoint cp) {
         forwardToClients();
     }
 
     public void undoDraw(GUID zoneGUID, GUID drawableGUID) {
 
         server.getConnection().broadcastCallMethod(ClientCommand.COMMAND.undoDraw.name(), zoneGUID, drawableGUID);
         Zone zone = server.getCampaign().getZone(zoneGUID);
         zone.removeDrawable(drawableGUID);
     }
 
     public void updateTokenMove(GUID zoneGUID, GUID tokenGUID, int x, int y) {
         forwardToClients();
     }
     
     public void setServerPolicy(ServerPolicy policy) {
     	forwardToClients();
     }
     
     ////
     // CONTEXT
     private static class RPCContext {
         
         private static ThreadLocal<RPCContext> threadLocal = new ThreadLocal<RPCContext>();
         
         public String id;
         public String method;
         public Object[] parameters;
         
         public RPCContext (String id, String method, Object[] parameters) {
             this.id = id;
             this.method = method;
             this.parameters = parameters;
         }
         
         public static boolean hasCurrent() {
             return threadLocal.get() != null;
         }
         
         public static RPCContext getCurrent() {
             return threadLocal.get();
         }
         
         public static void setCurrent(RPCContext context) {
             threadLocal.set(context);
         }
         
         ////
         // Convenience methods
         public GUID getGUID(int index) {
             return (GUID) parameters[index];
         }
         
         public Integer getInt(int index) {
             return (Integer) parameters[index];
         }
         
         public Object get(int index) {
             return parameters[index];
         }
         
         public String getString(int index) {
             return (String) parameters[index];
         }
         
         public Boolean getBool(int index) {
             return (Boolean) parameters[index];
         }
     }
 }
