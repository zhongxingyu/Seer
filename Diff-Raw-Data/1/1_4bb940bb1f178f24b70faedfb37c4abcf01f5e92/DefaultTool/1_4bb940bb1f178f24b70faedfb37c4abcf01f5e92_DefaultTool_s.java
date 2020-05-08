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
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.CellPoint;
 import net.rptools.maptool.client.ClientStyle;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ScreenPoint;
 import net.rptools.maptool.client.ZonePoint;
 import net.rptools.maptool.client.ui.Tool;
 import net.rptools.maptool.client.ui.zone.ZoneOverlay;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Pointer;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.TokenSize;
 
 /**
  */
 public abstract class DefaultTool extends Tool implements MouseListener, MouseMotionListener, ZoneOverlay, MouseWheelListener {
     private static final long serialVersionUID = 3258411729238372921L;
 
     private boolean isShowingPointer; 
     private boolean isDraggingMap;
     private boolean isDraggingToken;
     private boolean isNewTokenSelected;
     private boolean isDrawingSelectionBox;
     private Rectangle selectionBoundBox;
 	private int dragStartX;
 	private int dragStartY;
 	
 	private int mouseX;
 	private int mouseY;
 	
 	private Token tokenBeingDragged;
 	private Token tokenUnderMouse;
 	
     // Offset from token's X,Y when dragging. Values are in cell coordinates.
     private int dragOffsetX;
     private int dragOffsetY;
 
     // This is to manage overflowing of map move events (keep things snappy)
     private long lastMoveRedraw;
     private int mapDX, mapDY;
     private static final int REDRAW_DELAY = 25; // millis
     
     ////
 	// Mouse
 	
 	public void mousePressed(MouseEvent e) {
 
 		ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 
 		// SELECTION
 		if (SwingUtilities.isLeftMouseButton(e)) {
 			
 			// Pointer
 			if (SwingUtil.isControlDown(e)) {
 				
 				isShowingPointer = true;
 				
 				ZonePoint p = ZonePoint.fromScreenPoint(renderer, e.getX(), e.getY());
 				Pointer pointer = new Pointer(renderer.getZone(), p.x, p.y, 0);
 				
 				MapTool.serverCommand().showPointer(MapTool.getPlayer().getName(), pointer);
 				return;
 			}			
 
 			// Token
 			Token token = renderer.getTokenAt (e.getX(), e.getY());
 			if (token != null) {
 				
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
 
 				if (!SwingUtil.isShiftDown(e)) {
 					renderer.clearSelectedTokens();
 				}
 				
 				// Starting a bound box selection
 				isDrawingSelectionBox = true;
 				selectionBoundBox = new Rectangle(e.getX(), e.getY(), 0, 0);
 				
 				dragStartX = e.getX();
 				dragStartY = e.getY();
 			}
 			return;
 		}
 		
         // Waypoints
         if (SwingUtilities.isRightMouseButton(e) && isDraggingToken) {
             
             ZonePoint zp = ZonePoint.fromScreenPoint(renderer, e.getX(), e.getY());
             
             renderer.toggleMoveSelectionSetWaypoint(tokenBeingDragged.getId(), zp);
             
             MapTool.serverCommand().toggleTokenMoveWaypoint(renderer.getZone().getId(), tokenBeingDragged.getId(), zp.x, zp.y);
             return;
         }
         
         // DRAG PREPARATION
         if (SwingUtilities.isRightMouseButton(e)) {
 
 			// Perhaps a map drag
 			dragStartX = e.getX();
 			dragStartY = e.getY();
 		}
 	}
 	
 	public void mouseReleased(MouseEvent e) {
 		
         ZoneRenderer renderer = (ZoneRenderer) e.getSource();
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
 
         // POINTER
 		if (isShowingPointer) {
 			isShowingPointer = false;
 			MapTool.serverCommand().hidePointer(MapTool.getPlayer().getName());
 			return;
 		}
 		
 		// POPUP MENU
         if (SwingUtilities.isRightMouseButton(e)) {
         	
         	if (!isDraggingMap && !isDraggingToken && renderer.getSelectedTokenSet().size() > 0) {
         		showTokenContextMenu(e);
         	}
             
             if (isDraggingMap) {
                 isDraggingMap = false;
             }
         	return;
         }
 
 		// DRAG TOKEN COMPLETE
 		if (isDraggingToken) {
             renderer.commitMoveSelectionSet(tokenBeingDragged.getId()); // TODO: figure out a better way
 
             isDraggingToken = false;
 		}
 		
         // SELECT SINGLE TOKEN
         Token token = renderer.getTokenAt(e.getX(), e.getY());
         if (token != null && SwingUtilities.isLeftMouseButton(e) && !isDraggingToken && !SwingUtil.isShiftDown(e)) {
 			
 			if (!renderer.isTokenMoving(token)) {
 	        	renderer.clearSelectedTokens();
 	        	renderer.selectToken(token.getId());
 			}
         }
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
 	 */
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 	
 	}	
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
 	 */
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
 	 */
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	////
 	// MouseMotion
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
 	 */
 	public void mouseMoved(MouseEvent e) {
 		
 		mouseX = e.getX();
 		mouseY = e.getY();
 		
 		ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 		
 		CellPoint p = renderer.getCellAt(new ScreenPoint(e.getX(), e.getY()));
 		if (p != null) {	
 			MapTool.getFrame().setStatusMessage("Cell: " + p.x + ", " + p.y);
 		} else {
 		    MapTool.getFrame().setStatusMessage("");
         }
 
 		tokenUnderMouse = renderer.getTokenAt(mouseX, mouseY);
 	}
 	
 	public void mouseDragged(MouseEvent e) {
 
 		ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 
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
 			
 			if (tokenUnderMouse == null) {
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
 			Set<GUID> selectedTokenSet = renderer.getSelectedTokenSet();
 			if (selectedTokenSet.size() > 0) {
 				
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
 
                         renderer.constrainToCell(zonePoint);
 					} else {
 					    zonePoint.translate(-dragOffsetX, -dragOffsetY);
                     }
 
 					renderer.updateMoveSelectionSet(tokenBeingDragged.getId(), zonePoint);
 					MapTool.serverCommand().updateTokenMove(renderer.getZone().getId(), tokenBeingDragged.getId(), zonePoint.x, zonePoint.y);
 				}
 				isDraggingToken = true;
                 SwingUtil.hidePointer(renderer);
 			}
 			
 			return;
 		}
 		
 		// MAP MOVEMENT
 		if (SwingUtilities.isRightMouseButton(e)) {
 
 			isDraggingMap = true;
 			mapDX += e.getX() - dragStartX;
 			mapDY += e.getY() - dragStartY;
 	
 			dragStartX = e.getX();
 			dragStartY = e.getY();
 			
             long now = System.currentTimeMillis();
             if (now - lastMoveRedraw > REDRAW_DELAY) {
                 // TODO: does it matter to capture the last map move in the series ?
                 // TODO: This should probably be genericized an put into ZoneRenderer
                 // to prevent over zealous repainting
                 renderer.moveViewBy(mapDX, mapDY);
                 mapDX = 0;
                 mapDY = 0;
                 lastMoveRedraw = now;
             }
 		}
 	}	
 	
 	/* (non-Javadoc)
 	 * @see net.rptools.maptool.client.Tool#getKeyActionMap()
 	 */
 	protected Map<KeyStroke, Action> getKeyActionMap() {
 		return new HashMap<KeyStroke, Action>() {
 			{
 				// TODO: Optimize this by making it non anonymous
 				put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), new AbstractAction() {
 				
 					public void actionPerformed(java.awt.event.ActionEvent e) {
 						
 						ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 						
 						Set<GUID> selectedTokenSet = renderer.getSelectedTokenSet();
 						
 						for (GUID token : selectedTokenSet) {
 							
                             MapTool.serverCommand().removeToken(renderer.getZone().getId(), token);
 						}
 						
 						renderer.clearSelectedTokens();
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
 			g.setColor(ClientStyle.selectionBoxFill);
 			g.fillRoundRect(selectionBoundBox.x, selectionBoundBox.y, selectionBoundBox.width, selectionBoundBox.height,10, 10);
 			g.setComposite(composite);
 			
 			g.setColor(ClientStyle.selectionBoxOutline);
 			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			g.drawRoundRect(selectionBoundBox.x, selectionBoundBox.y, selectionBoundBox.width, selectionBoundBox.height,10, 10);
 
 			g.setStroke(stroke);
 		}
 
 //		if (tokenUnderMouse != null && renderer.getSelectedTokenSet().contains(tokenUnderMouse)) {
 //
 //			Rectangle rect = renderer.getTokenBounds(tokenUnderMouse);
 //			
 //			g.setColor(Color.black);
 //			g.fillRect(rect.x + rect.width - 10, rect.y + rect.height - 10, 10, 10);
 //
 //			g.setColor(Color.white);
 //			g.fillRect(rect.x + rect.width - 8, rect.y + rect.height - 8, 8, 8);
 //		}
 	}
 	
 	////
 	// Mouse Wheel
 	public void mouseWheelMoved(MouseWheelEvent e) {
 
 		ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 		if (e.getWheelRotation() > 0) {
 			
 			renderer.zoomOut(e.getX(), e.getY());
 		} else {
 			
 			renderer.zoomIn(e.getX(), e.getY());
 		}
 		
 	}	
 
 	////
 	// INTERNAL
 	
 	private void showTokenContextMenu(MouseEvent e) {
 
 		if (tokenUnderMouse == null) {
 			return;
 		}
 		
     	JPopupMenu popup = new JPopupMenu();
     	final ZoneRenderer renderer = (ZoneRenderer)e.getSource();
     	
     	// SIZE
     	// TODO: Genericize the heck out of this.
     	JMenu sizeMenu = new JMenu("Size");
     	JMenuItem freeSize = new JMenuItem("Free Size");
     	freeSize.setEnabled(false);
         sizeMenu.add(freeSize);
         sizeMenu.addSeparator();
         
         for (TokenSize.Size size : TokenSize.Size.values()) {
             JMenuItem menuItem = new JMenuItem(new ChangeSizeAction(size.name(), size));
             sizeMenu.add(menuItem);
         }
     	popup.add(sizeMenu);
         
         // Grid
         boolean snapToGrid = !tokenUnderMouse.isSnapToGrid();
         JCheckBoxMenuItem checkboxMenuItem = new JCheckBoxMenuItem("placeholder", !snapToGrid); 
         checkboxMenuItem.setAction(new SnapToGridAction(snapToGrid, renderer));
         popup.add(checkboxMenuItem);
 
         // Visibility
         checkboxMenuItem = new JCheckBoxMenuItem("Visible", tokenUnderMouse.isVisible());
         // TODO: Make this an action, not aic
         checkboxMenuItem.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
 
         		for (GUID guid : renderer.getSelectedTokenSet()) {
         			
         			Token token = renderer.getZone().getToken(guid);
         			if (token == null) {
         				continue;
         			}
         			
         			token.setVisible(((JCheckBoxMenuItem )e.getSource()).isSelected());
             		renderer.flush(token);
 
             		MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
         		}
 
         		renderer.repaint();
         	}
         });
         popup.add(checkboxMenuItem);
         
         // Rename
         // TODO: Make this an action, not aic
         if (renderer.getSelectedTokenSet().size() == 1) {
         	JMenuItem menuItem = new JMenuItem("Rename");
 
             menuItem.addActionListener(new ActionListener() {
             	public void actionPerformed(ActionEvent e) {
             		
                 	Token token = renderer.getZone().getToken(renderer.getSelectedTokenSet().iterator().next());
                 	
                 	String newName = (String)JOptionPane.showInputDialog(renderer, "Pick a new name for this token", "Rename Token", JOptionPane.QUESTION_MESSAGE, null, null, token.getName());
                 	if (newName == null || newName.length() == 0) {
                 		return;
                 	}
                 	
                 	token.setName(newName);
             		MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
             		MapTool.getFrame().repaint();
             	}
             });
             popup.add(menuItem);
         }
         
         
         // 
     	popup.show(renderer, e.getX(), e.getY());
 	}
 
 	private class SnapToGridAction extends AbstractAction {
 		
 		private boolean snapToGrid;
 		private ZoneRenderer renderer;
 		
 		public SnapToGridAction(boolean snapToGrid, ZoneRenderer renderer) {
 			super("Snap to grid");
 			this.snapToGrid = snapToGrid;
 			this.renderer = renderer;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			
 			for (GUID guid : renderer.getSelectedTokenSet()) {
 				
 				Token token = renderer.getZone().getToken(guid);
 				if (token == null) {
 					continue;
 				}
 				
 				token.setSnapToGrid(snapToGrid);
                 MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 			}
 		}
 	}
 	
 	private class ChangeSizeAction extends AbstractAction {
 		
 		private TokenSize.Size size;
 		
 		public ChangeSizeAction(String label, TokenSize.Size size) {
 			super(label);
 			this.size = size;
 		}
 		
 		/* (non-Javadoc)
 		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		 */
 		public void actionPerformed(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			for (GUID tokenGUID : renderer.getSelectedTokenSet()) {
 				
 				Token token = renderer.getZone().getToken(tokenGUID);
 				token.setSize(size.value());
                 MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 			}
 			
 			renderer.repaint();
 		}
 		
 	}
 
   @Override
   protected void resetTool() {
     // Do nothing here for now
   }
   
 }
