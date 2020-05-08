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
 import java.awt.Composite;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Area;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.AppActions;
 import net.rptools.maptool.client.AppState;
 import net.rptools.maptool.client.AppStyle;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.CellPoint;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ScreenPoint;
 import net.rptools.maptool.client.ZonePoint;
 import net.rptools.maptool.client.ui.StackSummaryPanel;
 import net.rptools.maptool.client.ui.TokenPopupMenu;
 import net.rptools.maptool.client.ui.zone.ZoneOverlay;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Pointer;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.TokenSize;
 import net.rptools.maptool.model.Zone;
 
 /**
  */
 public class PointerTool extends DefaultTool implements ZoneOverlay {
 
     private boolean isShowingPointer; 
     private boolean isDraggingToken;
     private boolean isNewTokenSelected;
     private boolean isDrawingSelectionBox;
     private boolean isSpaceDown;
     private Rectangle selectionBoundBox;
 
 	private Token tokenBeingDragged;
 	private Token tokenUnderMouse;
 	
     // Offset from token's X,Y when dragging. Values are in cell coordinates.
     private int dragOffsetX;
     private int dragOffsetY;
 	private int dragStartX;
 	private int dragStartY;
 
 	public PointerTool () {
         try {
             setIcon(new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/tool/PointerBlue16.png")));
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
 
     public void selectToken(Token token) {
     	
     }
     
     public void startTokenDrag(Token keyToken) {
     	
     }
     
     public void stopTokenDrag(Token keyToken) {
     	
     }
     
     public void moveToken(Token keyToken, ZonePoint location) {
     	
     }
 
     public void showPointer(ZonePoint location) {
     	
     }
     
     public void hidePointer() {
     	
     }
     
     public void showTokenMenu() {
     	
     }
     
     ////
 	// Mouse
 	public void mousePressed(MouseEvent e) {
 
 		if (isDraggingMap()) {
 			return;
 		}
 		
 		dragStartX = e.getX();
 		dragStartY = e.getY();
 		
 		// SELECTION
 		// Token
 		Token token = renderer.getTokenAt (e.getX(), e.getY());
 		if (token != null && !isDraggingToken) {
 
 			// Permission
 			if (!AppUtil.playerOwnsToken(token)) {
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
 		        ZonePoint pos = ZonePoint.fromScreenPoint(renderer, e.getX(), e.getY());
 		        dragOffsetX = pos.x - token.getX();
 		        dragOffsetY = pos.y - token.getY();
 			}
 		} else {
 
 			if (SwingUtilities.isLeftMouseButton(e)) {
 				// Starting a bound box selection
 				isDrawingSelectionBox = true;
 				selectionBoundBox = new Rectangle(e.getX(), e.getY(), 0, 0);
 			}
 			
 		}
 		
 		super.mousePressed(e);
 	}
 	
 	public void mouseReleased(MouseEvent e) {
 
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
 	
 			// POPUP MENU
 	        if (SwingUtilities.isRightMouseButton(e) && !isDraggingToken) {
 	        	
 	        	if (tokenUnderMouse != null && renderer.getSelectedTokenSet().size() > 0) {
 	        		
 	        		new TokenPopupMenu(renderer.getSelectedTokenSet(), e.getX(), e.getY(), renderer, tokenUnderMouse).showPopup(renderer);
 	        	}
 	        }
 	
 			// DRAG TOKEN COMPLETE
 			if (isDraggingToken) {
 	            renderer.commitMoveSelectionSet(tokenBeingDragged.getId()); // TODO: figure out a better way
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
         
 		super.mouseReleased(e);
 	}
     
 	////
 	// MouseMotion
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
 	 */
 	public void mouseMoved(MouseEvent e) {
 		
 		mouseX = e.getX();
 		mouseY = e.getY();
 		
 		CellPoint cp = renderer.getCellAt(new ScreenPoint(e.getX(), e.getY()));
 		if (cp != null) {	
 			MapTool.getFrame().setStatusMessage("Cell: " + cp.x + ", " + cp.y);
 		} else {
 		    MapTool.getFrame().setStatusMessage("");
         }
 
 		tokenUnderMouse = renderer.getTokenAt(mouseX, mouseY);
 		renderer.setMouseOver(tokenUnderMouse);
 		
 		// LATER: clean this up
 		List<Token> tokenList = renderer.getTokenStackAt(mouseX, mouseY);
 		if (tokenList != null) {
 			int gridSize = (int)renderer.getScaledGridSize();
 			StackSummaryPanel summaryPanel = new StackSummaryPanel (gridSize, tokenList);
 			
 			Integer x = null;
 			Integer y = null;
 			
 			// Calculate the top left corner of the stack
 			for (Token token : tokenList) {
 				if (x == null || token.getX() < x) {
 					x = token.getX();
 				}
 				if (y == null || token.getY() < y) {
 					y = token.getY();
 				}
 			}
 			
 			ScreenPoint sp = ScreenPoint.fromZonePoint(renderer, x, y);
 			
 			Point p = SwingUtilities.convertPoint(renderer, sp.x, sp.y, MapTool.getFrame().getGlassPane());
 			
 			x = p.x - StackSummaryPanel.PADDING - summaryPanel.getPreferredSize().width/2 + gridSize/2;
 			y = p.y - StackSummaryPanel.PADDING;
 			
 			//MapTool.getFrame().showNonModalGlassPane(summaryPanel, x, y);
 		}
 	}
 	
 	public void mouseDragged(MouseEvent e) {
 
		mouseX = e.getX();
		mouseY = e.getY();
		
 		CellPoint cellUnderMouse = renderer.getCellAt(new ScreenPoint(e.getX(), e.getY()));
 		if (cellUnderMouse != null) {
 			MapTool.getFrame().setStatusMessage("Cell: " + cellUnderMouse.x + ", " + cellUnderMouse.y);
 		}
 		
 		if (SwingUtilities.isLeftMouseButton(e)) {
 			
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
         
 				int x = e.getX();
 				int y = e.getY();
 				
 				ZonePoint zonePoint = ZonePoint.fromScreenPoint(renderer, x, y);
 				if (!isDraggingToken) {
 					tokenBeingDragged = tokenUnderMouse;
 					
 					renderer.addMoveSelectionSet(MapTool.getPlayer().getName(), tokenBeingDragged.getId(), selectedTokenSet, false);
 					MapTool.serverCommand().startTokenMove(MapTool.getPlayer().getName(), renderer.getZone().getId(), tokenBeingDragged.getId(), selectedTokenSet);
 				} else {
 					
 					if (tokenBeingDragged.isSnapToGrid()) {
 
 						zonePoint = cellUnderMouse.convertToZone(renderer);
 					} else {
 					    zonePoint.translate(-dragOffsetX, -dragOffsetY);
                     }
 					
 					// Make sure it's a valid move
 					if (!validateMove(tokenBeingDragged, renderer.getSelectedTokenSet(), zonePoint)) {
 						return;
 					}
 
 					renderer.updateMoveSelectionSet(tokenBeingDragged.getId(), zonePoint);
 					MapTool.serverCommand().updateTokenMove(renderer.getZone().getId(), tokenBeingDragged.getId(), zonePoint.x, zonePoint.y);
 				}
 				isDraggingToken = true;
                 SwingUtil.hidePointer(renderer);
 			}
 			
 			return;
 		}
 		
 		super.mouseDragged(e);
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
             Rectangle bounds = new Rectangle();
 			for (GUID tokenGUID : tokenSet) {
 				Token token = zone.getToken(tokenGUID);
 				if (token == null) {
 					continue;
 				}
 				
 				int x = token.getX() + deltaX;
 				int y = token.getY() + deltaY;
 	            int width = TokenSize.getWidth(token, zone.getGridSize());
 	            int height = TokenSize.getHeight(token, zone.getGridSize());
 
 	            int fudgeW = (int)(width*.25);
 	            int fudgeH = (int)(height*.25);
 	            bounds.setBounds(x+fudgeW, y+fudgeH, width-fudgeW*2, height-fudgeH*2);
 
 	            if (!fow.contains(bounds)) {
 	            	return false;
 	            }
 			}
 		}
 		
 		return true;
 	}
 	/* (non-Javadoc)
 	 * @see net.rptools.maptool.client.Tool#getKeyActionMap()
 	 */
 	protected Map<KeyStroke, Action> getKeyActionMap() {
 		return new HashMap<KeyStroke, Action>() {
 			{
 				put(KeyStroke.getKeyStroke("control C"), AppActions.COPY_TOKENS);
 				put(KeyStroke.getKeyStroke("control V"), AppActions.PASTE_TOKENS);
 				
 				// TODO: Optimize this by making it non anonymous
 				put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), new AbstractAction() {
 				
 					public void actionPerformed(java.awt.event.ActionEvent e) {
 						
 						ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 						
 						Set<GUID> selectedTokenSet = renderer.getSelectedTokenSet();
 						
 						for (GUID tokenGUID : selectedTokenSet) {
 							
 							Token token = renderer.getZone().getToken(tokenGUID);
 							
 							if (AppUtil.playerOwnsToken(token)) {
 	                            renderer.getZone().removeToken(tokenGUID);
 	                            MapTool.serverCommand().removeToken(renderer.getZone().getId(), tokenGUID);
 							}
 						}
 						
 						renderer.clearSelectedTokens();
 					}
 				});
 				
 				put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), new AbstractAction() {
 					public void actionPerformed(ActionEvent e) {
 						
 						if (isShowingPointer) {
 							isShowingPointer = false;
 							MapTool.serverCommand().hidePointer(MapTool.getPlayer().getName());
 						}
 						
 						isSpaceDown = false;
 					}
 				});
 				put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), new AbstractAction() {
 					public void actionPerformed(ActionEvent e) {
 						
 						if (isSpaceDown) {
 							return;
 						}
 						
 						if (isDraggingToken) {
 							
 							// Waypoint
 				            CellPoint cp = ZonePoint.fromScreenPoint(renderer, mouseX, mouseY).convertToCell(renderer);
 				            
 				            renderer.toggleMoveSelectionSetWaypoint(tokenBeingDragged.getId(), cp);
 				            
 				            MapTool.serverCommand().toggleTokenMoveWaypoint(renderer.getZone().getId(), tokenBeingDragged.getId(), cp);
 							
 						} else {
 							
 							// Pointer
 							isShowingPointer = true;
 							
 							ZonePoint p = ZonePoint.fromScreenPoint(renderer, mouseX, mouseY);
 							Pointer pointer = new Pointer(renderer.getZone(), p.x, p.y, 0);
 							
 							MapTool.serverCommand().showPointer(MapTool.getPlayer().getName(), pointer);
 						}
 						
 						isSpaceDown = true;
 					}
 				});
 			}
 		};
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
 			g.setColor(AppStyle.selectionBoxFill);
 			g.fillRoundRect(selectionBoundBox.x, selectionBoundBox.y, selectionBoundBox.width, selectionBoundBox.height,10, 10);
 			g.setComposite(composite);
 			
 			g.setColor(AppStyle.selectionBoxOutline);
 			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			g.drawRoundRect(selectionBoundBox.x, selectionBoundBox.y, selectionBoundBox.width, selectionBoundBox.height,10, 10);
 
 			g.setStroke(stroke);
 		}
 		
 	}
 	
 }
