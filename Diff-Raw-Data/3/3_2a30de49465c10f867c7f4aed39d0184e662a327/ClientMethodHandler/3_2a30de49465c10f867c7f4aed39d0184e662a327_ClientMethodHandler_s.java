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
 import java.util.Set;
 
 import net.rptools.clientserver.hessian.AbstractMethodHandler;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.client.ui.zone.ZoneRendererFactory;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Campaign;
 import net.rptools.maptool.model.CellPoint;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Label;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.model.Pointer;
 import net.rptools.maptool.model.TextMessage;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.model.drawing.Drawable;
 import net.rptools.maptool.model.drawing.DrawnElement;
 import net.rptools.maptool.model.drawing.Pen;
 import net.rptools.maptool.server.ServerPolicy;
 
 
 /**
  * @author drice
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class ClientMethodHandler extends AbstractMethodHandler {
     
     public ClientMethodHandler() {
     }
 
     public void handleMethod(String id, String method, Object[] parameters) {
         ClientCommand.COMMAND cmd = Enum.valueOf(ClientCommand.COMMAND.class, method);
         //System.out.println("ClientMethodHandler#handleMethod: " + cmd.name());
         
         GUID zoneGUID;
         Zone zone;
         
         switch (cmd) {
         case enforceZone:
         	
         	zoneGUID = (GUID) parameters[0];
         	ZoneRenderer renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
         	
         	if (renderer != null && renderer != MapTool.getFrame().getCurrentZoneRenderer() && (renderer.getZone().isVisible() || MapTool.getPlayer().isGM())) {
             	MapTool.getFrame().setCurrentZoneRenderer(renderer);
         	}
         	
         	break;
         case clearAllDrawings:
         	
         	zoneGUID = (GUID) parameters[0];
             zone = MapTool.getCampaign().getZone(zoneGUID);
             zone.getDrawnElements().clear();
             
             MapTool.getFrame().refresh();
         	break;
         case setZoneHasFoW:
         	
         	zoneGUID = (GUID) parameters[0];
         	boolean hasFog = (Boolean) parameters[1];
         	
             zone = MapTool.getCampaign().getZone(zoneGUID);
             zone.setHasFog(hasFog);
             
             // In case we're looking at the zone
             MapTool.getFrame().refresh();
         	break;
         	
         case exposeFoW:
         	
         	zoneGUID = (GUID) parameters[0];
             Area area = (Area) parameters[1];
 
             zone = MapTool.getCampaign().getZone(zoneGUID);
             zone.exposeArea(area);
 
         	MapTool.getFrame().getZoneRenderer(zoneGUID).updateFog();
         	break;
         	
         case hideFoW:
         	
         	zoneGUID = (GUID) parameters[0];
             area = (Area) parameters[1];
 
             zone = MapTool.getCampaign().getZone(zoneGUID);
             zone.hideArea(area);
 
             MapTool.getFrame().getZoneRenderer(zoneGUID).updateFog();
         	break;
         
         case setCampaign:
         	Campaign campaign = (Campaign) parameters[0];
         	MapTool.setCampaign(campaign);
             break;
             
         case putZone:
         	zone = (Zone) parameters[0];
         	MapTool.getCampaign().putZone(zone);
         	
         	// TODO: combine this with MapTool.addZone()
         	renderer = ZoneRendererFactory.newRenderer(zone);
         	MapTool.getFrame().addZoneRenderer(renderer);
         	if (MapTool.getFrame().getCurrentZoneRenderer() == null && zone.isVisible()) {
         		MapTool.getFrame().setCurrentZoneRenderer(renderer);
         	}
         	
         	AppListeners.fireZoneAdded(zone);
             break;
         case removeZone:
         	zoneGUID = (GUID)parameters[0];
         	MapTool.getCampaign().removeZone(zoneGUID);
         	MapTool.getFrame().removeZoneRenderer(MapTool.getFrame().getZoneRenderer(zoneGUID));
             break;
         case putAsset:
             AssetManager.putAsset((Asset) parameters[0]);
             MapTool.getFrame().refresh();
             break;
         case removeAsset:
             break;
         case putToken:
         	zoneGUID = (GUID) parameters[0];
         	zone = MapTool.getCampaign().getZone(zoneGUID);
         	Token token = (Token) parameters[1];
         	
         	zone.putToken(token);
         	
         	MapTool.getFrame().refresh();
             break;
             
         case putLabel:
             zoneGUID = (GUID) parameters[0];
             zone = MapTool.getCampaign().getZone(zoneGUID);
             Label label = (Label) parameters[1];
             
             zone.putLabel(label);
             
             MapTool.getFrame().refresh();
             break;
         case removeToken:
             zoneGUID = (GUID) parameters[0];
             zone = MapTool.getCampaign().getZone(zoneGUID);
             GUID tokenGUID = (GUID) parameters[1];
 
             zone.removeToken(tokenGUID);
             
             MapTool.getFrame().refresh();
             break;
         case removeLabel:
             zoneGUID = (GUID) parameters[0];
             zone = MapTool.getCampaign().getZone(zoneGUID);
             GUID labelGUID = (GUID) parameters[1];
 
             zone.removeLabel(labelGUID);
             
             MapTool.getFrame().refresh();
             break;
         case enforceZoneView: 
 
             zoneGUID = (GUID) parameters[0];
             int x = (Integer)parameters[1];
             int y = (Integer)parameters[2];
             int zoomIndex = (Integer)parameters[3];
             
             renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
             if (renderer == null) {
                 return;
             }
             
            renderer.centerOn(new ZonePoint(x, y));
             renderer.setScaleIndex(zoomIndex);
 
             break;
         case draw:
         	
         	zoneGUID = (GUID) parameters[0];
             Pen pen = (Pen) parameters[1];
         	Drawable drawable = (Drawable) parameters[2];
 
         	zone = MapTool.getCampaign().getZone(zoneGUID);
         	
         	zone.addDrawable(new DrawnElement(drawable, pen));
         	
         	MapTool.getFrame().refresh();
             break;
         
         case undoDraw:
           zoneGUID = (GUID) parameters[0];
           GUID drawableId = (GUID)parameters[1];
           zone = MapTool.getCampaign().getZone(zoneGUID);
           zone.removeDrawable(drawableId);
 
 		  if (MapTool.getFrame().getCurrentZoneRenderer().getZone().getId().equals(zoneGUID) && zoneGUID != null) {
 			  MapTool.getFrame().refresh();
 		  }
 
 		  break;
           
         case setZoneVisibility: 
 
         	zoneGUID = (GUID) parameters[0];
         	boolean visible = (Boolean) parameters[1];
         	
         	zone = MapTool.getCampaign().getZone(zoneGUID);
         	zone.setVisible(visible);
         	
         	ZoneRenderer currentRenderer = MapTool.getFrame().getCurrentZoneRenderer();
         	if (!visible && !MapTool.getPlayer().isGM() && currentRenderer != null && currentRenderer.getZone().getId().equals(zoneGUID)) {
         		MapTool.getFrame().setCurrentZoneRenderer(null);
         	}
         	if (visible && currentRenderer == null) {
         		currentRenderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
         		MapTool.getFrame().setCurrentZoneRenderer(currentRenderer);
         	}
         	
         	MapTool.getFrame().getZoneSelectionPanel().flush();
         	MapTool.getFrame().refresh();
         	break;
 		  
         case setZoneGridSize:
         	
         	zoneGUID = (GUID) parameters[0];
         	int xOffset = ((Integer) parameters[1]).intValue();
         	int yOffset = ((Integer) parameters[2]).intValue();
         	int size = ((Integer) parameters[3]).intValue();
         	int color = ((Integer) parameters[4]).intValue();
         	
         	zone = MapTool.getCampaign().getZone(zoneGUID);
         	zone.getGrid().setSize(size);
         	zone.getGrid().setOffset(xOffset, yOffset);
         	zone.setGridColor(color);
         	
         	MapTool.getFrame().refresh();
         	break;
 
         case playerConnected:
         	
         	MapTool.addPlayer((Player) parameters[0]);
         	MapTool.getFrame().refresh();
         	break;
 
         case playerDisconnected:
         	
         	MapTool.removePlayer((Player) parameters[0]);
         	MapTool.getFrame().refresh();
         	break;
             
         case message:
             TextMessage message = (TextMessage) parameters[0];
             MapTool.addServerMessage(message);
         	break;
             
         case showPointer:
         	MapTool.getFrame().getPointerOverlay().addPointer((String) parameters[0], (Pointer) parameters[1]);
         	MapTool.getFrame().refresh();
         	break;
         	
         case hidePointer:
         	MapTool.getFrame().getPointerOverlay().removePointer((String) parameters[0]);
         	MapTool.getFrame().refresh();
         	break;
         	
         case startTokenMove:
 			
 			String playerId = (String) parameters[0];
 			zoneGUID = (GUID) parameters[1];
 			GUID keyToken = (GUID) parameters[2];
 			Set<GUID> selectedSet = (Set<GUID>) parameters[3];
 			
 			renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
 			renderer.addMoveSelectionSet(playerId, keyToken, selectedSet, true);
 			
         	break;
         case stopTokenMove:
 
 			zoneGUID = (GUID) parameters[0];
 			keyToken = (GUID) parameters[1];
 			
 			renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
 			renderer.removeMoveSelectionSet(keyToken);
 
 			break;
         case updateTokenMove:
 
 			zoneGUID = (GUID) parameters[0];
 			keyToken = (GUID) parameters[1];
 			
 			x = ((Integer) parameters[2]).intValue();
 			y = ((Integer) parameters[3]).intValue();
 			
 			renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
 			renderer.updateMoveSelectionSet(keyToken, new ZonePoint(x, y));
 
 			break;
 			
         case toggleTokenMoveWaypoint:
 
 			zoneGUID = (GUID) parameters[0];
 			keyToken = (GUID) parameters[1];
 			CellPoint cp = (CellPoint) parameters[2];
 			
 			renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
 			renderer.toggleMoveSelectionSetWaypoint(keyToken, cp);
 
 			break;
 
         case setServerPolicy:
         	
         	ServerPolicy policy = (ServerPolicy) parameters[0];
         	MapTool.setServerPolicy(policy);
         	break;
         }
         
         	
     }
 
 }
