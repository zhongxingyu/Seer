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
 package net.rptools.maptool.client.tool;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Paint;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.Stroke;
 import java.awt.TexturePaint;
 import java.awt.event.ActionEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Area;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.AppActions;
 import net.rptools.maptool.client.AppPreferences;
 import net.rptools.maptool.client.AppStyle;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ScreenPoint;
 import net.rptools.maptool.client.ui.TokenLocation;
 import net.rptools.maptool.client.ui.TokenPopupMenu;
 import net.rptools.maptool.client.ui.Tool;
 import net.rptools.maptool.client.ui.token.TokenPropertiesDialog;
 import net.rptools.maptool.client.ui.zone.ZoneOverlay;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.CellPoint;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Pointer;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.TokenSize;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.util.ImageManager;
 
 /**
  */
 public class PointerTool extends DefaultTool implements ZoneOverlay {
 
 	private Paint nonAlphaSelectionPaint;
 	
 	private boolean isShowingTokenStackPopup;
     private boolean isShowingPointer; 
     private boolean isDraggingToken;
     private boolean isNewTokenSelected;
     private boolean isDrawingSelectionBox;
     private boolean isSpaceDown;
     private boolean isMovingWithKeys;
     private boolean isResizingToken;
     private boolean isRotatingToken;
     private Rectangle selectionBoundBox;
 
 	private Token tokenBeingDragged;
 	private Token tokenUnderMouse;
 	
 	private TokenStackPanel tokenStackPanel = new TokenStackPanel();
 	
 	private Map<Shape, Token> rotateBoundsMap = new HashMap<Shape, Token>();
 	private Map<Shape, Token> resizeBoundsMap = new HashMap<Shape, Token>();
 	
     // Offset from token's X,Y when dragging. Values are in cell coordinates.
     private int dragOffsetX;
     private int dragOffsetY;
 	private int dragStartX;
 	private int dragStartY;
 	
 	public PointerTool () {
         try {
             setIcon(new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/tool/PointerBlue16.png")));
 
             // Selection color using psuedo translucency
 			BufferedImage grid = SwingUtil.replaceColor(ImageUtil.getImage("net/rptools/maptool/client/image/grid.png"), 0x202020, 0x0000ff);
             nonAlphaSelectionPaint = new TexturePaint(grid, new Rectangle2D.Float(0, 0, grid.getWidth(), grid.getHeight()));
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
     }
     
 	@Override
 	public String getInstructions() {
 		return I18N.getText("tool.pointer.instructions");
 	}
 	
     @Override
     public String getTooltip() {
         return "Pointer tool";
     }
 
     public void startTokenDrag(Token keyToken) {
 		tokenBeingDragged = keyToken;
 		
 		if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().isMovementLocked()) {
 			// Not allowed
 			return;
 		}
 		
 		renderer.addMoveSelectionSet(MapTool.getPlayer().getName(), tokenBeingDragged.getId(), renderer.getSelectedTokenSet(), false);
 		MapTool.serverCommand().startTokenMove(MapTool.getPlayer().getName(), renderer.getZone().getId(), tokenBeingDragged.getId(), renderer.getSelectedTokenSet());
 		
 		isDraggingToken = true;
 		
     }
     
     public void stopTokenDrag() {
         renderer.commitMoveSelectionSet(tokenBeingDragged.getId()); // TODO: figure out a better way
         isDraggingToken = false;
         isMovingWithKeys = false;
     }
     
     private void showTokenStackPopup(List<Token> tokenList, int x, int y) {
 
 		tokenStackPanel.show(tokenList, x, y);
 		isShowingTokenStackPopup = true;
 		repaint();
     }
     
     private class TokenStackPanel {
 
     	private static final int PADDING = 4;
     	
     	private List<Token> tokenList;
     	private List<TokenLocation> tokenLocationList = new ArrayList<TokenLocation>();
 
     	private int x;
     	private int y;
     	
     	public void show(List<Token> tokenList, int x, int y) {
     		this.tokenList = tokenList;
     		this.x = x - TokenStackPanel.PADDING - getSize().width/2;
     		this.y = y - TokenStackPanel.PADDING - getSize().height/2;
     	}
     	
     	public Dimension getSize() {
     		int gridSize = (int)renderer.getScaledGridSize();
     		return new Dimension(tokenList.size()*(gridSize + PADDING) + PADDING, gridSize + PADDING*2);
     	}
     	
     	public void handleMouseEvent(MouseEvent event) {
     		// Nothing to do right now
     	}
     	
     	public void handleMouseMotionEvent(MouseEvent event) {
 
     		Point p = event.getPoint();
     		for (TokenLocation location : tokenLocationList) {
     			if (location.getBounds().contains(p.x, p.y)) {
 
     				if (!AppUtil.playerOwns(location.getToken())) {
     					return;
     				}
     				
     				renderer.clearSelectedTokens();
     				boolean selected = renderer.selectToken(location.getToken().getId());
 
     				if (selected) {
 	    				Tool tool = MapTool.getFrame().getToolbox().getSelectedTool();
 	    				if (!(tool instanceof PointerTool)) {
 	    					return;
 	    				}
 	    				
 	    				tokenUnderMouse = location.getToken();
 	    				((PointerTool) tool).startTokenDrag(location.getToken());
     				}
 
     				return;
     			}
     		}			
     	}
     	
     	public void paint(Graphics g) {
 
     		Dimension size = getSize();
     		int gridSize = (int)renderer.getScaledGridSize();
     		
     		// Background
     		g.setColor(getBackground());
     		g.fillRect(x, y, size.width, size.height);
     		
     		// Border
     		AppStyle.border.paintAround((Graphics2D) g, x, y, size.width-1, size.height-1);
     		
     		// Images
     		tokenLocationList.clear();
     		for (int i = 0; i < tokenList.size(); i++) {
     			
     			Token token = tokenList.get(i);
     			
     			BufferedImage image = ImageManager.getImage(AssetManager.getAsset(token.getAssetID()), renderer);
     			
     			Dimension imgSize = new Dimension(image.getWidth(), image.getHeight());
     			SwingUtil.constrainTo(imgSize, gridSize);
 
     			Rectangle bounds = new Rectangle(x + PADDING + i*(gridSize + PADDING), y + PADDING, imgSize.width, imgSize.height);
     			g.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, renderer);
     			
     			tokenLocationList.add(new TokenLocation(bounds, token));
     		}
     	}    	
     	
     	public boolean contains(int x, int y) {
     		return new Rectangle(this.x, this.y, getSize().width, getSize().height).contains(x, y);
     	}
     }
     
     ////
 	// Mouse
 	public void mousePressed(MouseEvent e) {
 		super.mousePressed(e);
 
 		if (isShowingTokenStackPopup) {
 			if (tokenStackPanel.contains(e.getX(), e.getY())) {
 				tokenStackPanel.handleMouseEvent(e);
 				return;
 			} else {
 				isShowingTokenStackPopup = false;
 				repaint();
 			}
 		}
 		
 		// So that keystrokes end up in the right place
 		renderer.requestFocusInWindow();
 		if (isDraggingMap()) {
 			return;
 		}
 		
 		if (isDraggingToken) {
 			return;
 		}
 		
 		dragStartX = e.getX();
 		dragStartY = e.getY();
 
 		// Check token resizing
 		for (Entry<Shape, Token> entry : resizeBoundsMap.entrySet()) {
 			Shape bounds = entry.getKey();
 			if (bounds.contains(dragStartX, dragStartY)) {
 				dragOffsetX = bounds.getBounds().width + bounds.getBounds().x - e.getX();
 				dragOffsetY = bounds.getBounds().height + bounds.getBounds().y - e.getY();
 
 				isResizingToken = true;
 				return;
 			}
 		}
 
 		// Check token rotation
 		for (Entry<Shape, Token> entry : rotateBoundsMap.entrySet()) {
 			if (entry.getKey().contains(dragStartX, dragStartY)) {
 				isRotatingToken = true;
 				return;
 			}
 		}
 		
 		// Properties
 		if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
 			List<Token> tokenList = renderer.getTokenStackAt(mouseX, mouseY);
 			if (tokenList != null) {
 				// Stack
 				renderer.clearSelectedTokens();
 				showTokenStackPopup(tokenList, e.getX(), e.getY());
 			} else {
 				// Single
 				Token token = renderer.getTokenAt(e.getX(), e.getY());
 				if (token != null) {
 
 					TokenPropertiesDialog dialog = new TokenPropertiesDialog(token);
 					Rectangle b = MapTool.getFrame().getBounds();
 					dialog.setLocation(b.x + (b.width - dialog.getWidth()) / 2, b.y + (b.height - dialog.getHeight()) / 2);
 					dialog.setVisible(true);
 					if (dialog.isTokenSaved()) {
 						renderer.repaint();
 						MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 					}
 				}
 			}
 			
 			return;
 		}
 		
 		// SELECTION
 		Token token = renderer.getTokenAt (e.getX(), e.getY());
 		if (token != null && !isDraggingToken && SwingUtilities.isLeftMouseButton(e)) {
 
 			// Permission
 			if (!AppUtil.playerOwns(token)) {
 				if (!SwingUtil.isShiftDown(e)) {
 					renderer.clearSelectedTokens();
 				}
 				return;
 			}
 			
 			// Don't select if it's already being moved by someone
 			isNewTokenSelected = false;
 			if (!renderer.isTokenMoving(token)) {
 				if (!renderer.getSelectedTokenSet().contains(token.getId()) && 
 						!SwingUtil.isShiftDown(e)) {
 					isNewTokenSelected = true;
                     renderer.clearSelectedTokens();
 				}
 				renderer.selectToken(token.getId());
         
 		        // Dragging offset for currently selected token
 		        ZonePoint pos = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
 		        dragOffsetX = pos.x - token.getX();
 		        dragOffsetY = pos.y - token.getY();
 			}
 		} else {
 
 			if (SwingUtilities.isLeftMouseButton(e)) {
 				// Starting a bound box selection
 				isDrawingSelectionBox = true;
 				selectionBoundBox = new Rectangle(e.getX(), e.getY(), 0, 0);
 			} else {
 				if (tokenUnderMouse != null) {
 					isNewTokenSelected = true;
 				}
 			}
 		}
 		
 	}
 	
 	public void mouseReleased(MouseEvent e) {
 
 		if (isShowingTokenStackPopup) {
 			if (tokenStackPanel.contains(e.getX(), e.getY())) {
 				tokenStackPanel.handleMouseEvent(e);
 				return;
 			} else {
 				isShowingTokenStackPopup = false;
 				repaint();
 			}
 		}
 		
 		if (isResizingToken) {
             MapTool.serverCommand().putToken(renderer.getZone().getId(), tokenUnderMouse);
 			isResizingToken = false;
 			return;
 		}
 		
 		if (SwingUtilities.isLeftMouseButton(e)) {
 	        try {
 	            SwingUtil.showPointer(renderer);
 	
 		        // SELECTION BOUND BOX
 		        if (isDrawingSelectionBox) {
 		        	isDrawingSelectionBox = false;
 		
 		        	if (!SwingUtil.isShiftDown(e)) {
 		        		renderer.clearSelectedTokens();
 		        	}
 		        	
 		        	renderer.selectTokens(selectionBoundBox);
 		        	
 		        	selectionBoundBox = null;
 		        	renderer.repaint();
 		        	return;
 		        }
 		
 				// DRAG TOKEN COMPLETE
 				if (isDraggingToken) {
 					stopTokenDrag();
 				}
 				
 		        // SELECT SINGLE TOKEN
 		        Token token = renderer.getTokenAt(e.getX(), e.getY());
 		        if (token != null && SwingUtilities.isLeftMouseButton(e) && !isDraggingToken && !SwingUtil.isShiftDown(e)) {
 		
 		    		// Only if it isn't already being moved
 					if (!renderer.isTokenMoving(token)) {
 			        	renderer.clearSelectedTokens();
 			        	renderer.selectToken(token.getId());
 					}
 		        }
 	        } finally {
 	        	isDraggingToken = false;
 	        	isDrawingSelectionBox = false;
 	        }
 	        
 	        return;
 		}
 		
 		// WAYPOINT
 		if (SwingUtilities.isMiddleMouseButton(e) && isDraggingToken) {
 			// Waypoint
             CellPoint cp = renderer.getZone().getGrid().convert(new ScreenPoint(mouseX, mouseY).convertToZone(renderer));
             
             renderer.toggleMoveSelectionSetWaypoint(tokenBeingDragged.getId(), cp);
             
             MapTool.serverCommand().toggleTokenMoveWaypoint(renderer.getZone().getId(), tokenBeingDragged.getId(), cp);
 			
 		}
         
 		// POPUP MENU
         if (SwingUtilities.isRightMouseButton(e) && !isDraggingToken && !isDraggingMap()) {
         	
        	if (tokenUnderMouse != null && isNewTokenSelected) {
         		if (!SwingUtil.isShiftDown(e)) {
         			renderer.clearSelectedTokens();
         		}
         		renderer.selectToken(tokenUnderMouse.getId());
                 isNewTokenSelected = false;
         	}
         	
         	if (tokenUnderMouse != null && renderer.getSelectedTokenSet().size() > 0) {
         		
         		new TokenPopupMenu(renderer.getSelectedTokenSet(), e.getX(), e.getY(), renderer, tokenUnderMouse).showPopup(renderer);
         		return;
         	}
         }
 
         super.mouseReleased(e);
 	}
     
 	////
 	// MouseMotion
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
 	 */
 	public void mouseMoved(MouseEvent e) {
 		if (renderer == null) {
 			return;
 		}
 		super.mouseMoved(e);
 		
 		if (isShowingTokenStackPopup) {
 			if (tokenStackPanel.contains(e.getX(), e.getY())) {
 				return;
 			} 
 
 			// Turn it off
 			isShowingTokenStackPopup = false;
 			repaint();
 			return;
 		}
 		
 		mouseX = e.getX();
 		mouseY = e.getY();
 		
 		if (isDraggingToken) {
 			if (isMovingWithKeys) {
 				return;
 			}
 			
 			ZonePoint zonePoint = new ScreenPoint(mouseX, mouseY).convertToZone(renderer);
 			handleDragToken(zonePoint);
 			return;
 		}
 		
 		tokenUnderMouse = renderer.getTokenAt(mouseX, mouseY);
 		renderer.setMouseOver(tokenUnderMouse);
 		
 	}
 	
 	private ScreenPoint getNearestVertex(ScreenPoint point) {
 		ZonePoint zp = point.convertToZone(renderer);
     	zp = renderer.getZone().getNearestVertex(zp);
     	return ScreenPoint.fromZonePoint(renderer, zp);
 	}
 	
 	public void mouseDragged(MouseEvent e) {
 
 		mouseX = e.getX();
 		mouseY = e.getY();
 		
 		if (isShowingTokenStackPopup) {
 			isShowingTokenStackPopup = false;
 			if (tokenStackPanel.contains(e.getX(), e.getY())) {
 				tokenStackPanel.handleMouseMotionEvent(e);
 				return;
 			} else {
 				renderer.repaint();
 			}
 		}
 
 		if (isResizingToken) {
 			
 			ScreenPoint sp = new ScreenPoint(mouseX + dragOffsetX, mouseY + dragOffsetY);
 			if (SwingUtil.isControlDown(e)) {
 				sp = getNearestVertex(sp);
 			}
 
 			ZonePoint zp = sp.convertToZone(renderer);
 			
 			int newWidth = Math.max(1, zp.x - tokenUnderMouse.getX());
 			int newHeight = Math.max(1, zp.y - tokenUnderMouse.getY());
 
 			tokenUnderMouse.setWidth(newWidth);
 			tokenUnderMouse.setHeight(newHeight);
 			
 			renderer.repaint();
 			return;
 		}
 		
 		CellPoint cellUnderMouse = renderer.getCellAt(new ScreenPoint(e.getX(), e.getY()));
 		if (cellUnderMouse != null) {
 			MapTool.getFrame().getCoordinateStatusBar().update(cellUnderMouse.x, cellUnderMouse.y);
 		}
 		
 		if (SwingUtilities.isLeftMouseButton(e) && !SwingUtilities.isRightMouseButton(e)) {
 			
 			if (isDrawingSelectionBox) {
 				
 				int x1 = dragStartX;
 				int y1 = dragStartY;
 				
 				int x2 = e.getX();
 				int y2 = e.getY();
 				
 				selectionBoundBox.x = Math.min(x1, x2);
 				selectionBoundBox.y = Math.min(y1, y2);
 				selectionBoundBox.width = Math.abs(x1 - x2);
 				selectionBoundBox.height = Math.abs(y1 - y2);
 				
 				renderer.repaint();
 				return;
 			}
 			
 			if (isDraggingToken) {
 				if (isMovingWithKeys) {
 					return;
 				}
 				ZonePoint zonePoint = new ScreenPoint(mouseX, mouseY).convertToZone(renderer);
 				handleDragToken(zonePoint);
 				return;
 			}
 			
 			if (tokenUnderMouse == null || !renderer.getSelectedTokenSet().contains(tokenUnderMouse.getId())) {
 				return;
 			}
 
 			if (!isDraggingToken && renderer.isTokenMoving(tokenUnderMouse)) {
                 return;
             }
 			
 			if (isNewTokenSelected) {
 				renderer.clearSelectedTokens();
 				renderer.selectToken(tokenUnderMouse.getId());
 			}
 			isNewTokenSelected = false;
 			
 			// Make user we're allowed
 			if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().isMovementLocked()) {
 				return;
 			}
 			
 			// Might be dragging a token
 			String playerId = MapTool.getPlayer().getName();
 			Set<GUID> selectedTokenSet = renderer.getSelectedTokenSet();
 			if (selectedTokenSet.size() > 0) {
 				
 				// Make sure we can do this
 				// LATER: This might be able to be removed since you can't 
 				// select an unowned token, check later
 				if (!MapTool.getPlayer().isGM() && MapTool.getServerPolicy().useStrictTokenManagement()) {
 					for (GUID tokenGUID : selectedTokenSet) {
 						Token token = renderer.getZone().getToken(tokenGUID);
 						if (!token.isOwner(playerId)) {
 							return;
 						}
 					}
 				}
 				
 				Point origin = new Point(tokenUnderMouse.getX(), tokenUnderMouse.getY());
 				
 				origin.translate(dragOffsetX, dragOffsetY);
         
 				startTokenDrag(tokenUnderMouse);
 				isDraggingToken = true;
                 SwingUtil.hidePointer(renderer);
 			}
 			
 			return;
 		}
 		
 		super.mouseDragged(e);
 	}	
 	
 	public boolean isDraggingToken() {
 		return isDraggingToken;
 	}
 	
 	/**
 	 * Move the keytoken being dragged to this zone point
 	 * @param zonePoint
 	 * @return true if the move was successful
 	 */
 	public boolean handleDragToken(ZonePoint zonePoint) {
 
 		// TODO: Optimize this (combine with calling code)
 		if (tokenBeingDragged.isSnapToGrid()) {
 
 			CellPoint cellUnderMouse = renderer.getZone().getGrid().convert(zonePoint);
 			zonePoint = renderer.getZone().getGrid().convert(cellUnderMouse);
 			MapTool.getFrame().getCoordinateStatusBar().update(cellUnderMouse.x, cellUnderMouse.y);
 		} else {
 		    zonePoint.translate(-dragOffsetX, -dragOffsetY);
 		}
 
 		// Don't bother if there isn't any movement
 		if (!renderer.hasMoveSelectionSetMoved(tokenBeingDragged.getId(), zonePoint)) {
 			return false;
 		}
 		
 		// Make sure it's a valid move
 		if (!validateMove(tokenBeingDragged, renderer.getSelectedTokenSet(), zonePoint)) {
 			return false;
 		}
 
 		dragStartX = zonePoint.x;
 		dragStartY = zonePoint.y;
 
 		renderer.updateMoveSelectionSet(tokenBeingDragged.getId(), zonePoint);
 		MapTool.serverCommand().updateTokenMove(renderer.getZone().getId(), tokenBeingDragged.getId(), zonePoint.x, zonePoint.y);
 		return true;
 	}
 
 	private boolean validateMove(Token leadToken, Set<GUID> tokenSet, ZonePoint point) {
 
 		Zone zone = renderer.getZone();
 		if (MapTool.getPlayer().isGM()) {
 			return true;
 		}
 		
 		if (zone.hasFog()) {
 			
 			// Check that the new position for each token is within the exposed area
 			Area fow = zone.getExposedArea();
 			if (fow == null) {
 				return true;
 			}
 
 			int deltaX = point.x - leadToken.getX();
 			int deltaY = point.y - leadToken.getY();
             boolean isVisible = false;
             Rectangle bounds = new Rectangle();
 			for (GUID tokenGUID : tokenSet) {
 				Token token = zone.getToken(tokenGUID);
 				if (token == null) {
 					continue;
 				}
 				
 				int x = token.getX() + deltaX;
 				int y = token.getY() + deltaY;
 	            int width = TokenSize.getWidth(token, zone.getGrid());
 	            int height = TokenSize.getHeight(token, zone.getGrid());
 	            
 	            int fudgeSize = 10;
 	            
 	            bounds.width = fudgeSize;
 	            bounds.height = fudgeSize;
 	            
 	            for (int by = y; by < y + height; by += fudgeSize) {
 	            	for (int bx = x; bx < x + width; bx += fudgeSize) {
 	            		bounds.x = bx;
 	            		bounds.y = by;
 	            		
 	    	            if (fow.contains(bounds)) {
 	    	            	isVisible = true;
 	    	            	break;
 	    	            }
 	            	}
 	            }
 			}
 			
 			if (!isVisible) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 
 	@Override
 	protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
 		super.installKeystrokes(actionMap);
 		
 		actionMap.put(KeyStroke.getKeyStroke("control C"), AppActions.COPY_TOKENS);
 		actionMap.put(KeyStroke.getKeyStroke("control V"), AppActions.PASTE_TOKENS);
 		
 		// TODO: Optimize this by making it non anonymous
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), new AbstractAction() {
 		
 			public void actionPerformed(java.awt.event.ActionEvent e) {
 				
 				ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 				
 				Set<GUID> selectedTokenSet = renderer.getSelectedTokenSet();
 				
 				for (GUID tokenGUID : selectedTokenSet) {
 					
 					Token token = renderer.getZone().getToken(tokenGUID);
 					
 					if (AppUtil.playerOwns(token)) {
                         renderer.getZone().removeToken(tokenGUID);
                         MapTool.serverCommand().removeToken(renderer.getZone().getId(), tokenGUID);
 					}
 				}
 				
 				renderer.clearSelectedTokens();
 			}
 		});
 		
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				if (isShowingPointer) {
 					isShowingPointer = false;
 					MapTool.serverCommand().hidePointer(MapTool.getPlayer().getName());
 				}
 				
 				isSpaceDown = false;
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				if (isSpaceDown) {
 					return;
 				}
 				
 				if (isDraggingToken) {
 					
 					// Waypoint
 		            CellPoint cp = renderer.getZone().getGrid().convert(new ScreenPoint(mouseX, mouseY).convertToZone(renderer));
 		            
 		            renderer.toggleMoveSelectionSetWaypoint(tokenBeingDragged.getId(), cp);
 		            
 		            MapTool.serverCommand().toggleTokenMoveWaypoint(renderer.getZone().getId(), tokenBeingDragged.getId(), cp);
 					
 				} else {
 					
 					// Pointer
 					isShowingPointer = true;
 					
 					ZonePoint zp = new ScreenPoint(mouseX, mouseY).convertToZone(renderer);
 					Pointer pointer = new Pointer(renderer.getZone(), zp.x, zp.y, 0);
 					
 					MapTool.serverCommand().showPointer(MapTool.getPlayer().getName(), pointer);
 				}
 				
 				isSpaceDown = true;
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				if (!isDraggingToken) {
 					return;
 				}
 					
 				// Stop
 				stopTokenDrag();
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				if (!isDraggingToken) {
 					return;
 				}
 					
 				// Stop
 				stopTokenDrag();
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				handleKeyMove(1, 0);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				handleKeyMove(-1, 0);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				handleKeyMove(0, -1);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				handleKeyMove(0, 1);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				handleKeyMove(-1, -1);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				handleKeyMove(1, -1);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				handleKeyMove(-1, 1);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				handleKeyMove(1, 1);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				handleKeyMove(1, 0);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				cycleSelectedToken(1);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.SHIFT_DOWN_MASK), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				
 				cycleSelectedToken(-1);
 			}
 		});
 	}
 
 	private void cycleSelectedToken(int direction) {
 		
 		List<Token> visibleTokens = renderer.getTokensOnScreen();
 		Set<GUID> selectedTokenSet = renderer.getSelectedTokenSet();
 		Integer newSelection = null;
 		
 		if (visibleTokens.size() == 0) {
 			return;
 		}
 		
 		if (selectedTokenSet.size() == 0) {
 			newSelection = 0;
 		} else {
 			
 			// Find the first selected token on the screen
 			for (int i = 0; i < visibleTokens.size(); i++) {
 				Token token = visibleTokens.get(i);
 				if (!renderer.isTokenSelectable(token.getId())) {
 					continue;
 				}
 				if (renderer.getSelectedTokenSet().contains(token.getId())) {
 					newSelection = i;
 					break;
 				}
 			}
 
 			// Pick the next
 			newSelection += direction;
 		}
 		
 		if (newSelection < 0) {
 			newSelection = visibleTokens.size()-1;
 		}
 		if (newSelection >= visibleTokens.size()) {
 			newSelection = 0;
 		}
 		
 		// Make the selection
 		renderer.clearSelectedTokens();
 		renderer.selectToken(visibleTokens.get(newSelection).getId());
 		
 	}
 	
 	private void handleKeyMove(int dx, int dy) {
 
 		if (!isDraggingToken) {
 			
 			// Start
 			Set<GUID> selectedTokenSet = renderer.getSelectedTokenSet();
 			if (selectedTokenSet.size() != 1) {
 				// only allow one at a time
 				return;
 			}
 			
 			Token token = renderer.getZone().getToken(selectedTokenSet.iterator().next());
 			if (token == null) {
 				return;
 			}
 
 			// Only one person at a time
 			if (renderer.isTokenMoving(token)) {
 				return;
 			}
 			
 			dragStartX = token.getX();
 			dragStartY = token.getY();
 			startTokenDrag(token);
 		}
 		
 		ZonePoint zp = null;
 		if (tokenBeingDragged.isSnapToGrid()) {
 			
 			CellPoint cp = renderer.getZone().getGrid().convert(new ZonePoint(dragStartX, dragStartY));
 
 			cp.x += dx;
 			cp.y += dy;
 			
 			zp = renderer.getZone().getGrid().convert(cp);
 		} else {
 			int size = TokenSize.getWidth(tokenBeingDragged, renderer.getZone().getGrid());
 			
 			int x = dragStartX + (size*dx);
 			int y = dragStartY + (size*dy);
 			
 			zp = new ZonePoint(x, y);
 		}
 
 		isMovingWithKeys = true;
 		handleDragToken(zp);
 	}
 
 	//// 
 	// ZoneOverlay
 	/* (non-Javadoc)
 	 * @see net.rptools.maptool.client.ZoneOverlay#paintOverlay(net.rptools.maptool.client.ZoneRenderer, java.awt.Graphics2D)
 	 */
 	public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
 		
 		if (selectionBoundBox != null) {
 			
 			Stroke stroke = g.getStroke();
 			g.setStroke(new BasicStroke(2));
 			
 			Composite composite = g.getComposite();
 			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, .25f));
 			// TODO: Use it's own preference, or genericize this one
 			g.setPaint(AppPreferences.getUseTranslucentFog() ? AppStyle.selectionBoxFill : nonAlphaSelectionPaint);
 			g.fillRoundRect(selectionBoundBox.x, selectionBoundBox.y, selectionBoundBox.width, selectionBoundBox.height,10, 10);
 			g.setComposite(composite);
 			
 			g.setColor(AppStyle.selectionBoxOutline);
 			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			g.drawRoundRect(selectionBoundBox.x, selectionBoundBox.y, selectionBoundBox.width, selectionBoundBox.height,10, 10);
 
 			g.setStroke(stroke);
 		}
 
 		if (isShowingTokenStackPopup) {
 			
 			tokenStackPanel.paint(g);
 		} else {
 			
 			resizeBoundsMap.clear();
 			rotateBoundsMap.clear();
 			for (GUID tokenGUID : renderer.getSelectedTokenSet()) {
 				Token token = renderer.getZone().getToken(tokenGUID);
 				if (token == null) {
 					continue;
 				}
 				
 				if (!token.isStamp() && !token.isBackground()) {
 					return;
 				}
 				
 				// Show sizing controls
 				Rectangle bounds = renderer.getTokenBounds(token);
 				if (bounds == null || renderer.isTokenMoving(token)) {
 					continue;
 				}
 				
 				// Resize
 				if (!token.isSnapToScale()) {
 					int x = bounds.x + bounds.width - 10;
 					int y = bounds.y + bounds.height - 10;
 					Rectangle resizeBounds = new Rectangle(x, y, 10, 10);
 
 					resizeBoundsMap.put(resizeBounds, token);
 
 					g.setColor(Color.black);
 					g.fill(resizeBounds);
 
 					g.setColor(Color.gray);
 					g.draw(resizeBounds);
 				}
 				
 				// Rotate
 //				int length = 35;
 //				int cx = bounds.x + bounds.width/2;
 //				int cy = bounds.y + bounds.height/2;
 //				int facing = token.getFacing() != null ? token.getFacing() : 0;
 //
 //				int x = (int)(cx + Math.cos(Math.toRadians(facing)) * length);
 //				int y = (int)(cy - Math.sin(Math.toRadians(facing)) * length);
 //				
 //				Ellipse2D rotateBounds = new Ellipse2D.Float(x-5, y-5, 10, 10);
 //				rotateBoundsMap.put(rotateBounds, token);
 //
 //				g.setColor(Color.black);
 //				g.drawLine(cx, cy, x, y);
 //				g.fill(rotateBounds);
 //				
 //				g.setColor(Color.gray);
 //				g.draw(rotateBounds);
 			}
 		}
 	}
 
 	
 }
