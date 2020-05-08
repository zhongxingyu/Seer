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
 
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
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
 import javax.swing.JPopupMenu;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 import net.rptools.common.swing.SwingUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ui.Tool;
 import net.rptools.maptool.client.ui.ZoneOverlay;
 import net.rptools.maptool.client.ui.ZoneRenderer;
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
 	private int dragStartX;
 	private int dragStartY;
 	
 	private int mouseX;
 	private int mouseY;
 	
 	private Token tokenBeingDragged;
 	private Token tokenUnderMouse;
 	
     // Offset from token's X,Y when dragging. Values are in cell coordinates.
     private int dragOffsetX;
     private int dragOffsetY;
 
     ////
 	// Mouse
 	
 	public void mousePressed(MouseEvent e) {
 
         isDraggingMap = false;
         isDraggingToken = false;
 		ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 
 		// SELECTION
 		if (SwingUtilities.isLeftMouseButton(e)) {
 			
 			// Pointer
 			if (SwingUtil.isControlDown(e)) {
 				
 				isShowingPointer = true;
 				
 				Point p = renderer.convertScreenToZone(e.getX(), e.getY());
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
			        Point cell = renderer.getCellAt(e.getX(), e.getY());
			        dragOffsetX = cell.x - token.getX();
			        dragOffsetY = cell.y - token.getY();
 				}
 			} else {
 				renderer.clearSelectedTokens();
 			}
 			
 		}
 		
         // DRAG PREPARATION
         if (SwingUtilities.isRightMouseButton(e)) {
 
 			// Perhaps a map drag
 			dragStartX = e.getX();
 			dragStartY = e.getY();
 		}
 	}
 	
 	public void mouseReleased(MouseEvent e) {
 		
 		// POINTER
 		if (isShowingPointer) {
 			isShowingPointer = false;
 			MapTool.serverCommand().hidePointer(MapTool.getPlayer().getName());
 			return;
 		}
 		
 		// POPUP MENU
 		ZoneRenderer renderer = (ZoneRenderer) e.getSource();
         if (SwingUtilities.isRightMouseButton(e) && !isDraggingMap) {
         	
         	if (renderer.getSelectedTokenSet().size() > 0) {
         		showTokenContextMenu(e);
         	}
         	return;
         }
 
 		// DRAG TOKEN COMPLETE
 		if (isDraggingToken) {
 			renderer.removeMoveSelectionSet(tokenBeingDragged.getId());
 			MapTool.serverCommand().stopTokenMove(renderer.getZone().getId(), tokenBeingDragged.getId());
 		}
 		
         // SELECT SINGLE TOKEN
         Token token = renderer.getTokenAt(e.getX(), e.getY());
         if (token != null && SwingUtilities.isLeftMouseButton(e) && !isDraggingToken && !SwingUtil.isShiftDown(e)) {
 			
 			if (!renderer.isTokenMoving(token)) {
 	        	renderer.clearSelectedTokens();
 	        	renderer.selectToken(token.getId());
 			}
         }
         	
         
 		isDraggingMap = false;
 		isDraggingToken = false;
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
 		
 		Point p = renderer.getCellAt(e.getX(), e.getY());
 		if (p != null) {	
 			MapTool.getFrame().setStatusMessage("Cell: " + p.x + ", " + p.y);
 		}
 
 		tokenUnderMouse = renderer.getTokenAt(mouseX, mouseY);
 //			
 //		Token token = renderer.getTokenAt(mouseX, mouseY);
 //		if (token == null) {
 //			if (tokenUnderMouse != null) {
 //
 //				renderer.unzoomToken(tokenUnderMouse);
 //
 //				tokenUnderMouse = null;
 //				renderer.repaint();
 //			}
 //		} else {
 //			if (token != tokenUnderMouse) {
 //
 //				// TODO: PLEEEEASE, for all that it HOLY ..... CLEAN UP THIS CODE !
 //				int tokenWidth = (int)(TokenSize.getWidth(token, renderer.getZone().getGridSize()) * renderer.getScale());
 //				int tokenHeight = (int)(TokenSize.getHeight(token, renderer.getZone().getGridSize()) * renderer.getScale());
 //
 //				boolean tokenTooSmall = tokenWidth < ZoneRenderer.HOVER_SIZE_THRESHOLD && tokenHeight < ZoneRenderer.HOVER_SIZE_THRESHOLD;
 //				
 //				renderer.unzoomToken(tokenUnderMouse);
 //
 //				tokenUnderMouse = token;
 //				renderer.repaint();
 //				if (tokenTooSmall) {
 //					renderer.zoomToken(token);
 //				}
 //			}
 //		}
 	}
 	
 	public void mouseDragged(MouseEvent e) {
 
 		ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 
 		Point cellUnderMouse = renderer.getCellAt(e.getX(), e.getY());
 		if (cellUnderMouse != null) {
 			MapTool.getFrame().setStatusMessage("Cell: " + cellUnderMouse.x + ", " + cellUnderMouse.y);
 		}
 		
 		if (SwingUtilities.isLeftMouseButton(e)) {
 			
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
 				
 				Point p = renderer.convertScreenToZone(x, y);
 				if (!isDraggingToken) {
 					tokenBeingDragged = tokenUnderMouse;
 					
 					renderer.addMoveSelectionSet(MapTool.getPlayer().getName(), tokenBeingDragged.getId(), selectedTokenSet, false);
 					MapTool.serverCommand().startTokenMove(MapTool.getPlayer().getName(), renderer.getZone().getId(), tokenBeingDragged.getId(), selectedTokenSet);
 				} else {
 					
 					if (tokenBeingDragged.isSnapToGrid()) {
 						// OPTIMIZE:
 						int gridSize = renderer.getZone().getGridSize();
 	
 						int scalex = (p.x / gridSize);
 						int scaley = (p.y / gridSize);
 						
 						p = new Point(scalex * gridSize, scaley * gridSize);
 						
					}
 
 					renderer.updateMoveSelectionSet(tokenBeingDragged.getId(), p.x, p.y);
 					MapTool.serverCommand().updateTokenMove(renderer.getZone().getId(), tokenBeingDragged.getId(), p.x, p.y);
 				}
 				isDraggingToken = true;
 			}
 			
 			return;
 		}
 		
 		// MAP MOVEMENT
 		if (SwingUtilities.isRightMouseButton(e)) {
 
 			isDraggingMap = true;
 			int dx = e.getX() - dragStartX;
 			int dy = e.getY() - dragStartY;
 	
 			dragStartX = e.getX();
 			dragStartY = e.getY();
 			
 			renderer.moveViewBy(dx, dy);
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
 		
     	JPopupMenu popup = new JPopupMenu();
     	ZoneRenderer renderer = (ZoneRenderer)e.getSource();
     	
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
         JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("placeholder", !snapToGrid); 
         menuItem.setAction(new SnapToGridAction(snapToGrid, renderer));
         popup.add(menuItem);
     	
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
