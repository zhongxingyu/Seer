 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.client.tool;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.TexturePaint;
 import java.awt.event.ActionEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Area;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.AppActions;
 import net.rptools.maptool.client.AppConstants;
 import net.rptools.maptool.client.AppPreferences;
 import net.rptools.maptool.client.AppStyle;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ScreenPoint;
 import net.rptools.maptool.client.swing.HTMLPanelRenderer;
 import net.rptools.maptool.client.tool.LayerSelectionDialog.LayerSelectionListener;
 import net.rptools.maptool.client.ui.StampPopupMenu;
 import net.rptools.maptool.client.ui.TokenLocation;
 import net.rptools.maptool.client.ui.TokenPopupMenu;
 import net.rptools.maptool.client.ui.Tool;
 import net.rptools.maptool.client.ui.Toolbox;
 import net.rptools.maptool.client.ui.token.EditTokenDialog;
 import net.rptools.maptool.client.ui.zone.FogUtil;
 import net.rptools.maptool.client.ui.zone.PlayerView;
 import net.rptools.maptool.client.ui.zone.ZoneOverlay;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.CellPoint;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Pointer;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.TokenProperty;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.model.Zone.Layer;
 import net.rptools.maptool.util.GraphicsUtil;
 import net.rptools.maptool.util.ImageManager;
 import net.rptools.maptool.util.StringUtil;
 import net.rptools.maptool.util.TokenUtil;
 
 /**
  */
 public class PointerTool extends DefaultTool implements ZoneOverlay {
 
 	private boolean isShowingTokenStackPopup;
 	private boolean isShowingPointer;
 	private boolean isDraggingToken;
 	private boolean isNewTokenSelected;
 	private boolean isDrawingSelectionBox;
 	private boolean isSpaceDown;
 	private boolean isMovingWithKeys;
 	private Rectangle selectionBoundBox;
 
 	// Hovers
 	private boolean isShowingHover;
 	private Area hoverTokenBounds;
 	private String hoverTokenNotes;
 
 	private Token tokenBeingDragged;
 	private Token tokenUnderMouse;
 	private Token markerUnderMouse;
 
 	private TokenStackPanel tokenStackPanel = new TokenStackPanel();
 
 	private HTMLPanelRenderer htmlRenderer = new HTMLPanelRenderer();
 
 	private BufferedImage statSheet;
 	private Token tokenOnStatSheet;
 
 	private static int PADDING = 7;
 
 	private Font boldFont = AppStyle.labelFont.deriveFont(Font.BOLD);
 
 	// Offset from token's X,Y when dragging. Values are in cell coordinates.
 	private int dragOffsetX;
 	private int dragOffsetY;
 	private int dragStartX;
 	private int dragStartY;
 
 	private LayerSelectionDialog layerSelectionDialog;
 
 	public PointerTool() {
 		try {
 			setIcon(new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/tool/pointer-blue.png")));
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 
 		htmlRenderer.setBackground(new Color(0, 0, 0, 200));
 		htmlRenderer.setForeground(Color.black);
 		htmlRenderer.setOpaque(false);
 		htmlRenderer.addStyleSheetRule("body{color:black}");
 		htmlRenderer.addStyleSheetRule(".title{font-size: 14pt}");
 
 		layerSelectionDialog = new LayerSelectionDialog(new Zone.Layer[] { Zone.Layer.TOKEN, Zone.Layer.GM, Zone.Layer.OBJECT, Zone.Layer.BACKGROUND }, new LayerSelectionListener() {
 			public void layerSelected(Layer layer) {
 				if (renderer != null) {
 					renderer.setActiveLayer(layer);
 					if (layer != Zone.Layer.TOKEN) {
 						MapTool.getFrame().getToolbox().setSelectedTool(StampTool.class);
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	protected void attachTo(ZoneRenderer renderer) {
 		super.attachTo(renderer);
 
 		renderer.setActiveLayer(Zone.Layer.TOKEN);
 
 		if (MapTool.getPlayer().isGM()) {
 			MapTool.getFrame().showControlPanel(layerSelectionDialog);
 		}
 
 		htmlRenderer.attach(renderer);
 
 		layerSelectionDialog.updateViewList();
 	}
 
 	@Override
 	protected void detachFrom(ZoneRenderer renderer) {
 		super.detachFrom(renderer);
 		MapTool.getFrame().hideControlPanel();
 		htmlRenderer.detach(renderer);
 	}
 
 	@Override
 	public String getInstructions() {
 		return "tool.pointer.instructions";
 	}
 
 	@Override
 	public String getTooltip() {
 		return "tool.pointer.tooltip";
 	}
 
 	public void startTokenDrag(Token keyToken) {
 		tokenBeingDragged = keyToken;
 
 		if (!MapTool.getPlayer().isGM() && (MapTool.getServerPolicy().isMovementLocked()
 		        || MapTool.getFrame().getInitiativePanel().isMovementLocked(keyToken))) {
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
 
 		dragOffsetX = 0;
 		dragOffsetY = 0;
 
		if (renderer.getZone().hasFog() && AppPreferences.getAutoRevealVisionOnGMMovement() && (MapTool.getPlayer().isGM() || MapTool.getServerPolicy().getPlayersCanRevealVision())) {
 			Set<GUID> exposeSet = new HashSet<GUID>();
 			for (GUID tokenGUID : renderer.getSelectedTokenSet()) {
 				Token token = renderer.getZone().getToken(tokenGUID);
 				if (token == null) {
 					continue;
 				}
 				if (token.getType() == Token.Type.PC) {
 					exposeSet.add(tokenGUID);
 				}
 			}
 			FogUtil.exposeLastPath(renderer, exposeSet);
 		}
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
 			this.x = x - TokenStackPanel.PADDING - getSize().width / 2;
 			this.y = y - TokenStackPanel.PADDING - getSize().height / 2;
 		}
 
 		public Dimension getSize() {
 			int gridSize = (int) renderer.getScaledGridSize();
 			FontMetrics fm = getFontMetrics(getFont());
 			return new Dimension(tokenList.size() * (gridSize + PADDING) + PADDING, gridSize + PADDING * 2 + fm.getHeight() + 10);
 		}
 
 		public void handleMouseReleased(MouseEvent event) {
 
 		}
 
 		public void handleMousePressed(MouseEvent event) {
 			if (event.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(event)) {
 				Token token = getTokenAt(event.getX(), event.getY());
 				if (token == null || !AppUtil.playerOwns(token)) {
 					return;
 				}
 
 				// TODO: Combine this with the code just like it below
 				EditTokenDialog tokenPropertiesDialog = MapTool.getFrame().getTokenPropertiesDialog();
 				tokenPropertiesDialog.showDialog(tokenUnderMouse);
 
 				if (tokenPropertiesDialog.isTokenSaved()) {
 					renderer.repaint();
 					renderer.flush(token);
 					MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 					renderer.getZone().putToken(token);
 					MapTool.getFrame().resetTokenPanels();
 				}
 			}
 			if (SwingUtilities.isRightMouseButton(event)) {
 				Token token = getTokenAt(event.getX(), event.getY());
 				if (token == null || !AppUtil.playerOwns(token)) {
 					return;
 				}
 				tokenUnderMouse = token;
 				Set<GUID> selectedSet = new HashSet<GUID>();
 				selectedSet.add(token.getId());
 				new TokenPopupMenu(selectedSet, event.getX(), event.getY(), renderer, token).showPopup(renderer);
 			}
 		}
 
 		public void handleMouseMotionEvent(MouseEvent event) {
 
 			Token token = getTokenAt(event.getX(), event.getY());
 			if (token == null) {
 				return;
 			}
 
 			if (!AppUtil.playerOwns(token)) {
 				return;
 			}
 
 			renderer.clearSelectedTokens();
 			boolean selected = renderer.selectToken(token.getId());
 
 			if (selected) {
 				Tool tool = MapTool.getFrame().getToolbox().getSelectedTool();
 				if (!(tool instanceof PointerTool)) {
 					return;
 				}
 
 				tokenUnderMouse = token;
 				((PointerTool) tool).startTokenDrag(token);
 			}
 		}
 
 		public void paint(Graphics g) {
 
 			Dimension size = getSize();
 			int gridSize = (int) renderer.getScaledGridSize();
 
 			FontMetrics fm = g.getFontMetrics();
 
 			// Background
 			((Graphics2D) g).setPaint(new GradientPaint(x, y, Color.white, x + size.width, y + size.height, Color.gray));
 			g.fillRect(x, y, size.width, size.height);
 
 			// Border
 			AppStyle.border.paintAround((Graphics2D) g, x, y, size.width - 1, size.height - 1);
 
 			// Images
 			tokenLocationList.clear();
 			for (int i = 0; i < tokenList.size(); i++) {
 
 				Token token = tokenList.get(i);
 
 				BufferedImage image = ImageManager.getImage(AssetManager.getAsset(token.getImageAssetId()), renderer);
 
 				Dimension imgSize = new Dimension(image.getWidth(), image.getHeight());
 				SwingUtil.constrainTo(imgSize, gridSize);
 
 				Rectangle bounds = new Rectangle(x + PADDING + i * (gridSize + PADDING), y + PADDING, imgSize.width, imgSize.height);
 				g.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, renderer);
 
 				GraphicsUtil.drawBoxedString((Graphics2D) g, token.getName(), bounds.x + bounds.width / 2, bounds.y + bounds.height + fm.getAscent());
 
 				tokenLocationList.add(new TokenLocation(bounds, token));
 			}
 		}
 
 		public Token getTokenAt(int x, int y) {
 			for (TokenLocation location : tokenLocationList) {
 				if (location.getBounds().contains(x, y)) {
 					return location.getToken();
 				}
 			}
 			return null;
 		}
 
 		public boolean contains(int x, int y) {
 			return new Rectangle(this.x, this.y, getSize().width, getSize().height).contains(x, y);
 		}
 	}
 
 	// //
 	// Mouse
 	public void mousePressed(MouseEvent e) {
 		super.mousePressed(e);
 
 		if (isShowingHover) {
 			isShowingHover = false;
 			hoverTokenBounds = null;
 			hoverTokenNotes = null;
 			markerUnderMouse = renderer.getMarkerAt(e.getX(), e.getY());
 			repaint();
 		}
 
 		if (isShowingTokenStackPopup) {
 			if (tokenStackPanel.contains(e.getX(), e.getY())) {
 				tokenStackPanel.handleMousePressed(e);
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
 
 					if (!AppUtil.playerOwns(token)) {
 						return;
 					}
 
 					EditTokenDialog tokenPropertiesDialog = MapTool.getFrame().getTokenPropertiesDialog();
 					tokenPropertiesDialog.showDialog(token);
 
 					if (tokenPropertiesDialog.isTokenSaved()) {
 						renderer.repaint();
 						renderer.flush(token);
 						MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 						renderer.getZone().putToken(token);
 						MapTool.getFrame().resetTokenPanels();
 					}
 				}
 			}
 
 			return;
 		}
 
 		// SELECTION
 		Token token = renderer.getTokenAt(e.getX(), e.getY());
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
 				if (!renderer.getSelectedTokenSet().contains(token.getId()) && !SwingUtil.isShiftDown(e)) {
 					isNewTokenSelected = true;
 					renderer.clearSelectedTokens();
 				}
 
 				if (SwingUtil.isShiftDown(e) && renderer.getSelectedTokenSet().contains(token.getId())) {
 					renderer.deselectToken(token.getId());
 				} else {
 					renderer.selectToken(token.getId());
 				}
 
 				// Dragging offset for currently selected token
 				ZonePoint pos = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
 				Rectangle tokenBounds = token.getBounds(renderer.getZone());
 				
 				if (token.isSnapToGrid()) {
 					dragOffsetX = (pos.x - tokenBounds.x) - (tokenBounds.width/2);
 					dragOffsetY = (pos.y - tokenBounds.y) - (tokenBounds.height/2);
 				} else {
 					dragOffsetX = pos.x - tokenBounds.x;
 					dragOffsetY = pos.y - tokenBounds.y;
 				}
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
 				tokenStackPanel.handleMouseReleased(e);
 				return;
 			} else {
 				isShowingTokenStackPopup = false;
 				repaint();
 			}
 		}
 
 		if (SwingUtilities.isLeftMouseButton(e)) {
 			try {
 
 				// MARKER
 				renderer.setCursor(Cursor.getPredefinedCursor(markerUnderMouse != null ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
 				if (tokenUnderMouse == null && markerUnderMouse != null && !isShowingHover && !isDraggingToken) {
 					isShowingHover = true;
 					hoverTokenBounds = renderer.getMarkerBounds(markerUnderMouse);
 					hoverTokenNotes = createHoverNote(markerUnderMouse);
 					if (hoverTokenBounds == null) {
 						// Uhhhh, where's the token ?
 						isShowingHover = false;
 					}
 					repaint();
 				}
 
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
 					SwingUtil.showPointer(renderer);
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
 			setWaypoint();
 		}
 
 		// POPUP MENU
 		if (SwingUtilities.isRightMouseButton(e) && !isDraggingToken && !isDraggingMap()) {
 
 			if (tokenUnderMouse != null && !renderer.getSelectedTokenSet().contains(tokenUnderMouse.getId())) {
 				if (!SwingUtil.isShiftDown(e)) {
 					renderer.clearSelectedTokens();
 				}
 				renderer.selectToken(tokenUnderMouse.getId());
 				isNewTokenSelected = false;
 			}
 
 			if (tokenUnderMouse != null && renderer.getSelectedTokenSet().size() > 0) {
 
 				if (tokenUnderMouse.isStamp()) {
 					new StampPopupMenu(renderer.getSelectedTokenSet(), e.getX(), e.getY(), renderer, tokenUnderMouse).showPopup(renderer);
 				} else {
 					new TokenPopupMenu(renderer.getSelectedTokenSet(), e.getX(), e.getY(), renderer, tokenUnderMouse).showPopup(renderer);
 				}
 
 				return;
 			}
 		}
 
 		super.mouseReleased(e);
 	}
 
 	// //
 	// MouseMotion
 	public void mouseMoved(MouseEvent e) {
 		if (renderer == null) {
 			return;
 		}
 		super.mouseMoved(e);
 
 		if (isShowingPointer) {
 			ZonePoint zp = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
 
 			Pointer pointer = MapTool.getFrame().getPointerOverlay().getPointer(MapTool.getPlayer().getName());
 			if (pointer != null) {
 				pointer.setX(zp.x);
 				pointer.setY(zp.y);
 
 				renderer.repaint();
 
 				MapTool.serverCommand().movePointer(MapTool.getPlayer().getName(), zp.x, zp.y);
 			}
 			return;
 		}
 
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
 
 		if (tokenUnderMouse == null) {
 			statSheet = null;
 		}
 
 		Token marker = renderer.getMarkerAt(mouseX, mouseY);
 		if (!AppUtil.tokenIsVisible(renderer.getZone(), marker, renderer.getPlayerView())) {
 			marker = null;
 		}
 		if (marker != markerUnderMouse && marker != null) {
 			markerUnderMouse = marker;
 			renderer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 			MapTool.getFrame().setStatusMessage(markerUnderMouse.getName());
 		} else if (marker == null && markerUnderMouse != null) {
 			markerUnderMouse = null;
 			renderer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			MapTool.getFrame().setStatusMessage("");
 		}
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
 
 				// NOTE: This is a weird one that has to do with the order of
 				// the mouseReleased event. if the selection
 				// box started the drag while hovering over a marker, we need to
 				// tell it to not show the marker after the
 				// drag is complete
 				markerUnderMouse = null;
 
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
 	 * 
 	 * @param zonePoint
 	 * @return true if the move was successful
 	 */
 	public boolean handleDragToken(ZonePoint zonePoint) {
 
 		// TODO: Optimize this (combine with calling code)
 		zonePoint.translate(-dragOffsetX, -dragOffsetY);
 		if (tokenBeingDragged.isSnapToGrid()) {
 
 			// cellUnderMouse actually token position if token being dragged
 			// with keys.
 			CellPoint cellUnderMouse = renderer.getZone().getGrid().convert(zonePoint);
 			zonePoint = renderer.getZone().getGrid().convert(cellUnderMouse);
 
 			MapTool.getFrame().getCoordinateStatusBar().update(cellUnderMouse.x, cellUnderMouse.y);
 		} else {
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
 
 			// Check that the new position for each token is within the exposed
 			// area
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
 				Rectangle tokenSize = token.getBounds(zone);
 
 				int fudgeSize = 10;
 
 				bounds.width = fudgeSize;
 				bounds.height = fudgeSize;
 
 				for (int by = y; by < y + tokenSize.height; by += fudgeSize) {
 					for (int bx = x; bx < x + tokenSize.width; bx += fudgeSize) {
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
 
 		actionMap.put(AppActions.CUT_TOKENS.getKeyStroke(), AppActions.CUT_TOKENS);
 		actionMap.put(AppActions.COPY_TOKENS.getKeyStroke(), AppActions.COPY_TOKENS);
 		actionMap.put(AppActions.PASTE_TOKENS.getKeyStroke(), AppActions.PASTE_TOKENS);
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, AppActions.menuShortcut), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				// TODO: Combine all this crap with the Stamp tool
 				if (renderer.getSelectedTokenSet().size() == 0) {
 					return;
 				}
 
 				Toolbox toolbox = MapTool.getFrame().getToolbox();
 
 				FacingTool tool = (FacingTool) toolbox.getTool(FacingTool.class);
 				tool.init(renderer.getZone().getToken(renderer.getSelectedTokenSet().iterator().next()), renderer.getSelectedTokenSet());
 
 				toolbox.setSelectedTool(FacingTool.class);
 			}
 		});
 
 		// TODO: Optimize this by making it non anonymous
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), new AbstractAction() {
 
 			public void actionPerformed(java.awt.event.ActionEvent e) {
 
 				ZoneRenderer renderer = (ZoneRenderer) e.getSource();
 
 				// Check to see if this is the required action
 				if (!MapTool
 						.confirmTokenDelete()) {
 					return;
 				}				
 				
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
 
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), new StopPointerActionListener());
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.CTRL_MASK, true), new StopPointerActionListener());
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.SHIFT_MASK, true), new StopPointerActionListener());
 
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), new PointerActionListener(Pointer.Type.ARROW));
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.CTRL_MASK, false), new PointerActionListener(Pointer.Type.SPEECH_BUBBLE));
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.SHIFT_MASK, false), new PointerActionListener(Pointer.Type.THOUGHT_BUBBLE));
 
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
 
 				handleKeyMove(0, 1);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 
 				handleKeyMove(1, 0);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 
 				handleKeyRotate(-1, false); // clockwise
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 
 				handleKeyRotate(-1, true); // clockwise
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 
 				handleKeyMove(-1, 0);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 
 				handleKeyRotate(1, false); // counter-clockwise
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 
 				handleKeyRotate(1, true); // counter-clockwise
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 
 				handleKeyMove(0, -1);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 
 				renderer.cycleSelectedToken(1);
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.SHIFT_DOWN_MASK), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 
 				renderer.cycleSelectedToken(-1);
 			}
 		});
 
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				if (MapTool.getPlayer().isGM() || MapTool.getServerPolicy().getPlayersCanRevealVision()) {
 					FogUtil.exposeVisibleArea(renderer, renderer.getSelectedTokenSet());
 				}
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				if (MapTool.getPlayer().isGM() || MapTool.getServerPolicy().getPlayersCanRevealVision()) {
 					FogUtil.exposePCArea(renderer);
 				}
 			}
 		});
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				if (MapTool.getPlayer().isGM() || MapTool.getServerPolicy().getPlayersCanRevealVision()) {
 					FogUtil.exposeLastPath(renderer, renderer.getSelectedTokenSet());
 				}
 			}
 		});
 	}
 
 	/**
 	 * Handle token rotations when using the arrow keys.
 	 * 
 	 * @param direction
 	 *            -1 is cw & 1 is ccw
 	 */
 	private void handleKeyRotate(int direction, boolean freeRotate) {
 
 		Set<GUID> tokenGUIDSet = renderer.getSelectedTokenSet();
 		if (tokenGUIDSet.size() == 0) {
 			return;
 		}
 
 		for (GUID tokenGUID : tokenGUIDSet) {
 			Token token = renderer.getZone().getToken(tokenGUID);
 			if (token == null) {
 				continue;
 			}
 
 			if (!AppUtil.playerOwns(token)) {
 				continue;
 			}
 
 			Integer facing = token.getFacing();
 			// TODO: this should really be a per grid setting
 			if (facing == null) {
 				facing = -90; // natural alignment
 			}
 
 			if (freeRotate) {
 				facing += direction * 5;
 			} else {
 				int[] facingArray = renderer.getZone().getGrid().getFacingAngles();
 				int facingIndex = TokenUtil.getIndexNearestTo(facingArray, facing);
 
 				facingIndex += direction;
 
 				if (facingIndex < 0) {
 					facingIndex = facingArray.length - 1;
 				}
 				if (facingIndex == facingArray.length) {
 					facingIndex = 0;
 				}
 
 				facing = facingArray[facingIndex];
 			}
 
 			token.setFacing(facing);
 
 			renderer.flush(token);
 			MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
 		}
 
 		renderer.repaint();
 
 	}
 
 	private void handleKeyMove(int dx, int dy) {
 
 		Token keyToken = null;
 		if (!isDraggingToken) {
 
 			// Start
 			Set<GUID> selectedTokenSet = renderer.getSelectedTokenSet();
 
 			for (GUID tokenId : selectedTokenSet) {
 
 				Token token = renderer.getZone().getToken(tokenId);
 				if (token == null) {
 					return;
 				}
 
 				// Need a key token to orient the move from, just arbitraily
 				// pick the first one
 				if (keyToken == null) {
 					keyToken = token;
 				}
 
 				// Only one person at a time
 				if (renderer.isTokenMoving(token)) {
 					return;
 				}
 			}
 			if (keyToken == null) {
 				return;
 			}
 			dragStartX = keyToken.getX();
 			dragStartY = keyToken.getY();
 			startTokenDrag(keyToken);
 		}
 		if (!isMovingWithKeys) {
 			dragOffsetX = 0;
 			dragOffsetY = 0;
 		}
 
 		ZonePoint zp = null;
 		if (tokenBeingDragged.isSnapToGrid()) {
 
 			CellPoint cp = renderer.getZone().getGrid().convert(new ZonePoint(dragStartX, dragStartY));
 
 			cp.x += dx;
 			cp.y += dy;
 
 			zp = renderer.getZone().getGrid().convert(cp);
 		} else {
 			Rectangle tokenSize = tokenBeingDragged.getBounds(renderer.getZone());
 
 			int x = dragStartX + (tokenSize.width * dx);
 			int y = dragStartY + (tokenSize.height * dy);
 
 			zp = new ZonePoint(x, y);
 		}
 
 		isMovingWithKeys = true;
 		handleDragToken(zp);
 	}
 
 	private void setWaypoint() {
 
 		ZonePoint p = new ZonePoint(dragStartX, dragStartY);
 
 		if (!tokenBeingDragged.isSnapToGrid()) {
 			// Center on the token
 			Rectangle footprintBounds = tokenBeingDragged.getBounds(renderer.getZone());
 
 			p.translate(footprintBounds.width / 2, footprintBounds.height / 2);
 		}
 
 		renderer.toggleMoveSelectionSetWaypoint(tokenBeingDragged.getId(), p);
 
 		MapTool.serverCommand().toggleTokenMoveWaypoint(renderer.getZone().getId(), tokenBeingDragged.getId(), p);
 	}
 
 	// //
 	// POINTER KEY ACTION
 	private class PointerActionListener extends AbstractAction {
 
 		Pointer.Type type;
 
 		public PointerActionListener(Pointer.Type type) {
 			this.type = type;
 		}
 
 		public void actionPerformed(ActionEvent e) {
 
 			if (isSpaceDown) {
 				return;
 			}
 
 			if (isDraggingToken) {
 				setWaypoint();
 			} else {
 
 				// Pointer
 				isShowingPointer = true;
 
 				ZonePoint zp = new ScreenPoint(mouseX, mouseY).convertToZone(renderer);
 				Pointer pointer = new Pointer(renderer.getZone(), zp.x, zp.y, 0, type);
 
 				MapTool.serverCommand().showPointer(MapTool.getPlayer().getName(), pointer);
 			}
 
 			isSpaceDown = true;
 		}
 
 	}
 
 	// //
 	// STOP POINTER ACTION
 	private class StopPointerActionListener extends AbstractAction {
 
 		public void actionPerformed(ActionEvent e) {
 
 			if (isShowingPointer) {
 				isShowingPointer = false;
 				MapTool.serverCommand().hidePointer(MapTool.getPlayer().getName());
 			}
 
 			isSpaceDown = false;
 		}
 	}
 
 	// //
 	// ZoneOverlay
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.rptools.maptool.client.ZoneOverlay#paintOverlay(net.rptools.maptool
 	 * .client.ZoneRenderer, java.awt.Graphics2D)
 	 */
 	public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
 
 		Dimension viewSize = renderer.getSize();
 
 		Composite composite = g.getComposite();
 		if (selectionBoundBox != null) {
 
 			Stroke stroke = g.getStroke();
 			g.setStroke(new BasicStroke(2));
 
 			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, .25f));
 			g.setPaint(AppStyle.selectionBoxFill);
 			g.fillRoundRect(selectionBoundBox.x, selectionBoundBox.y, selectionBoundBox.width, selectionBoundBox.height, 10, 10);
 			g.setComposite(composite);
 
 			g.setColor(AppStyle.selectionBoxOutline);
 			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			g.drawRoundRect(selectionBoundBox.x, selectionBoundBox.y, selectionBoundBox.width, selectionBoundBox.height, 10, 10);
 
 			g.setStroke(stroke);
 		}
 
 		if (isShowingTokenStackPopup) {
 
 			tokenStackPanel.paint(g);
 		}
 
 		// Statsheet
 		if (tokenUnderMouse != null && !isDraggingToken && AppUtil.tokenIsVisible(renderer.getZone(), tokenUnderMouse, new PlayerView(MapTool.getPlayer().getRole()))) {
 
 			if (AppPreferences.getPortraitSize() > 0 && (tokenOnStatSheet == null || !tokenOnStatSheet.equals(tokenUnderMouse) || statSheet == null)) {
 
 				tokenOnStatSheet = tokenUnderMouse;
 
 				// Portrait
 				MD5Key portraitId = tokenUnderMouse.getPortraitImage() != null ? tokenUnderMouse.getPortraitImage() : tokenUnderMouse.getImageAssetId();
 				BufferedImage image = ImageManager.getImage(AssetManager.getAsset(portraitId));
 
 				Dimension imgSize = new Dimension(image.getWidth(), image.getHeight());
 
 				// Size
 				SwingUtil.constrainTo(imgSize, AppPreferences.getPortraitSize());
 
 				// Stats
 				Dimension statSize = null;
 				Map<String, String> propertyMap = new LinkedHashMap<String, String>();
 				if (AppPreferences.getShowStatSheet()) {
 					for (TokenProperty property : MapTool.getCampaign().getTokenPropertyList(tokenUnderMouse.getPropertyType())) {
 	
 						if (property.isShowOnStateSheet()) {
 	
 							if (property.isGMOnly() && !MapTool.getPlayer().isGM()) {
 								continue;
 							}
 	
 							if (property.isOwnerOnly() && !AppUtil.playerOwns(tokenUnderMouse)) {
 								continue;
 							}
 	
 							Object propertyValue = tokenUnderMouse.getEvaluatedProperty(property.getName());
 							if (propertyValue != null) {
 								if (propertyValue.toString().length() > 0) {
 									String propName = property.getName();
 									if (property.getShortName() != null) {
 										propName = property.getShortName();
 									}
 	
 									Object value = tokenUnderMouse.getEvaluatedProperty(property.getName());
 	
 									propertyMap.put(propName, value != null ? value.toString() : "");
 								}
 							}
 						}
 					}
 				}
 				
 				if (tokenUnderMouse.getPortraitImage() != null || propertyMap.size() > 0) {
 					Font font = AppStyle.labelFont;
 					FontMetrics valueFM = g.getFontMetrics(font);
 					FontMetrics keyFM = g.getFontMetrics(boldFont);
 					int rowHeight = Math.max(valueFM.getHeight(), keyFM.getHeight());
 					if (propertyMap.size() > 0) {
 
 						// Figure out size requirements
 						int height = propertyMap.size() * (rowHeight + PADDING);
 						int width = -1;
 						for (Entry<String, String> entry : propertyMap.entrySet()) {
 
 							int lineWidth = SwingUtilities.computeStringWidth(keyFM, entry.getKey()) + SwingUtilities.computeStringWidth(valueFM, "  " + entry.getValue());
 							if (width < 0 || lineWidth > width) {
 								width = lineWidth;
 							}
 						}
 
 						statSize = new Dimension(width + PADDING * 3, height);
 					}
 
 					// Create the space for the image
 					int width = imgSize.width + (statSize != null ? statSize.width + AppStyle.miniMapBorder.getRightMargin() : 0) + AppStyle.miniMapBorder.getLeftMargin()
 							+ AppStyle.miniMapBorder.getRightMargin();
 					int height = Math.max(imgSize.height, (statSize != null ? statSize.height + AppStyle.miniMapBorder.getRightMargin() : 0)) + AppStyle.miniMapBorder.getTopMargin()
 							+ AppStyle.miniMapBorder.getBottomMargin();
 					statSheet = new BufferedImage(width, height, BufferedImage.BITMASK);
 					Graphics2D statsG = statSheet.createGraphics();
 					statsG.setClip(new Rectangle(0, 0, width, height));
 					statsG.setFont(font);
 					SwingUtil.useAntiAliasing(statsG);
 
 					// Draw the stats first, right aligned
 					if (statSize != null) {
 						Rectangle bounds = new Rectangle(width - statSize.width - AppStyle.miniMapBorder.getRightMargin(), statSize.height == height ? 0 : height - statSize.height
 								- AppStyle.miniMapBorder.getBottomMargin(), statSize.width, statSize.height);
 
 						statsG.setPaint(new TexturePaint(AppStyle.panelTexture, new Rectangle(0, 0, AppStyle.panelTexture.getWidth(), AppStyle.panelTexture.getHeight())));
 						statsG.fill(bounds);
 						AppStyle.miniMapBorder.paintAround(statsG, bounds);
 						AppStyle.shadowBorder.paintWithin(statsG, bounds);
 
 						// Stats
 						int y = bounds.y + rowHeight;
 						for (Entry<String, String> entry : propertyMap.entrySet()) {
 
 							// Box
 							statsG.setColor(new Color(249, 241, 230, 140));
 							statsG.fillRect(bounds.x, y - keyFM.getAscent(), bounds.width - PADDING / 2, rowHeight);
 							statsG.setColor(new Color(175, 163, 149));
 							statsG.drawRect(bounds.x, y - keyFM.getAscent(), bounds.width - PADDING / 2, rowHeight);
 
 							// Values
 							statsG.setColor(Color.black);
 							statsG.setFont(boldFont);
 							statsG.drawString(entry.getKey(), bounds.x + PADDING * 2, y);
 							statsG.setFont(font);
 							int strw = SwingUtilities.computeStringWidth(valueFM, entry.getValue());
 							statsG.drawString(entry.getValue(), bounds.x + bounds.width - strw - PADDING, y);
 
 							y += PADDING + rowHeight;
 						}
 					}
 
 					// Draw the portrait
 					Rectangle bounds = new Rectangle(AppStyle.miniMapBorder.getLeftMargin(), height - imgSize.height - AppStyle.miniMapBorder.getBottomMargin(), imgSize.width, imgSize.height);
 
 					statsG.setPaint(new TexturePaint(AppStyle.panelTexture, new Rectangle(0, 0, AppStyle.panelTexture.getWidth(), AppStyle.panelTexture.getHeight())));
 					statsG.fill(bounds);
 					statsG.drawImage(image, bounds.x, bounds.y, imgSize.width, imgSize.height, this);
 					AppStyle.miniMapBorder.paintAround(statsG, bounds);
 					AppStyle.shadowBorder.paintWithin(statsG, bounds);
 
 					// Label
 					GraphicsUtil.drawBoxedString(statsG, tokenUnderMouse.getName(), bounds.width / 2 + AppStyle.miniMapBorder.getLeftMargin(), height - 15);
 
 					statsG.dispose();
 				}
 
 			}
 
 			if (statSheet != null) {
 				g.drawImage(statSheet, 5, viewSize.height - statSheet.getHeight() - 5, this);
 			}
 		}
 
 		// Hovers
 		if (isShowingHover) {
 
 			// Anchor next to the token
 			Dimension size = htmlRenderer.setText(hoverTokenNotes, (int) (renderer.getWidth() * .75), (int) (renderer.getHeight() * .75));
 			Point location = new Point(hoverTokenBounds.getBounds().x + hoverTokenBounds.getBounds().width / 2 - size.width / 2, hoverTokenBounds.getBounds().y);
 
 			// Anchor in the bottom left corner
 			location.x = 4 + PADDING;
 			location.y = viewSize.height - size.height - 4 - PADDING;
 
 			// Keep it on screen
 			if (location.x + size.width > viewSize.width) {
 				location.x = viewSize.width - size.width;
 			}
 			if (location.x < 4) {
 				location.x = 4;
 			}
 			if (location.y + size.height > viewSize.height - 4) {
 				location.y = viewSize.height - size.height - 4;
 			}
 			if (location.y < 4) {
 				location.y = 4;
 			}
 
 			// Background
 			// g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
 			// .5f));
 			// g.setColor(Color.black);
 			// g.fillRect(location.x, location.y, size.width, size.height);
 			// g.setComposite(composite);
 			g.setPaint(new TexturePaint(AppStyle.panelTexture, new Rectangle(0, 0, AppStyle.panelTexture.getWidth(), AppStyle.panelTexture.getHeight())));
 			g.fillRect(location.x, location.y, size.width, size.height);
 
 			// Content
 			htmlRenderer.render(g, location.x, location.y);
 
 			// Border
 			AppStyle.miniMapBorder.paintAround(g, location.x, location.y, size.width, size.height);
 			AppStyle.shadowBorder.paintWithin(g, location.x, location.y, size.width, size.height);
 			// AppStyle.border.paintAround(g, location.x, location.y,
 			// size.width, size.height);
 		}
 	}
 
 	private String createHoverNote(Token marker) {
 		boolean showGMNotes = MapTool.getPlayer().isGM() && !StringUtil.isEmpty(marker.getGMNotes());
 
 		StringBuilder builder = new StringBuilder();
 
 		if (marker.getPortraitImage() != null) {
 			builder.append("<table><tr><td valign=top>");
 		}
 
 		if (!StringUtil.isEmpty(marker.getNotes())) {
 			builder.append("<b><span class='title'>").append(marker.getName()).append("</span></b><br>");
 			builder.append(markerUnderMouse.getNotes());
 			// add a gap between player and gmNotes
 			if (showGMNotes) {
 				builder.append("\n\n");
 			}
 		}
 
 		if (showGMNotes) {
 			builder.append("<b><span class='title'>GM Notes");
 			if (!StringUtil.isEmpty(marker.getGMName())) {
 				builder.append(" - ").append(marker.getGMName());
 			}
 			builder.append(":</span></b><br>");
 			builder.append(marker.getGMNotes());
 		}
 
 		if (marker.getPortraitImage() != null) {
 
 			BufferedImage image = ImageManager.getImageAndWait(AssetManager.getAsset(marker.getPortraitImage()));
 			Dimension imgSize = new Dimension(image.getWidth(), image.getHeight());
 			if (imgSize.width > AppConstants.NOTE_PORTRAIT_SIZE || imgSize.height > AppConstants.NOTE_PORTRAIT_SIZE) {
 				SwingUtil.constrainTo(imgSize, AppConstants.NOTE_PORTRAIT_SIZE);
 			}
 			builder.append("</td><td valign=top>");
 			builder.append("<img src=asset://").append(marker.getPortraitImage()).append(" width=").append(imgSize.width).append(" height=").append(imgSize.height).append("></tr></table>");
 		}
 
 		String notes = builder.toString();
 		notes = notes.replaceAll("\n", "<br>");
 
 		return notes;
 	}
 
 }
