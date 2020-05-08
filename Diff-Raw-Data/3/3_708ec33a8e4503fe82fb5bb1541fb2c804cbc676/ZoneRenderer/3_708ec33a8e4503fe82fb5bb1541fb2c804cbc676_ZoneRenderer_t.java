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
 package net.rptools.maptool.client.ui.zone;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.Transparency;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 import java.awt.geom.QuadCurve2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JComponent;
 
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.maptool.client.AppState;
 import net.rptools.maptool.client.AppStyle;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.CellPoint;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.MapToolUtil;
 import net.rptools.maptool.client.ScreenPoint;
 import net.rptools.maptool.client.TransferableHelper;
 import net.rptools.maptool.client.ZonePoint;
 import net.rptools.maptool.client.ui.Scale;
 import net.rptools.maptool.client.ui.token.TokenOverlay;
 import net.rptools.maptool.client.ui.token.TokenStates;
 import net.rptools.maptool.client.ui.token.TokenTemplate;
 import net.rptools.maptool.client.walker.ZoneWalker;
 import net.rptools.maptool.client.walker.astar.AStarEuclideanWalker;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Label;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.TokenSize;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.util.GraphicsUtil;
 import net.rptools.maptool.util.ImageManager;
 
 
 /**
  */
 public abstract class ZoneRenderer extends JComponent implements DropTargetListener, Comparable {
     private static final long serialVersionUID = 3832897780066104884L;
 
     // TODO: Perhaps make this a user defined limit
     public static final int HOVER_SIZE_THRESHOLD = 40;
     public static final int EDGE_LIMIT = 25; // can't move board past this edge
 	
     public static final int MIN_GRID_SIZE = 5;
     
     protected Zone              zone;
 
     protected Scale zoneScale = new Scale();
     
     private DrawableRenderer drawableRenderer = new DrawableRenderer();
     
     private List<ZoneOverlay> overlayList = new ArrayList<ZoneOverlay>();
     private List<TokenLocation> tokenLocationList = new LinkedList<TokenLocation>();
     private Set<GUID> selectedTokenSet = new HashSet<GUID>();
     private List<LabelLocation> labelLocationList = new LinkedList<LabelLocation>();
     
 
 	private Map<GUID, SelectionSet> selectionSetMap = new HashMap<GUID, SelectionSet>();
 
 	private BufferedImage fog;
 	private boolean updateFog;
 	
     // Optimizations
     private Map<Token, BufferedImage> replacementImageMap = new HashMap<Token, BufferedImage>();
 
 	private Token tokenUnderMouse;
 
 	private ScreenPoint pointUnderMouse;
 	
     public ZoneRenderer(Zone zone) {
         if (zone == null) { throw new IllegalArgumentException("Zone cannot be null"); }
 
         this.zone = zone;
         
         // DnD
         new DropTarget(this, this);
 
         addMouseListener(new MouseAdapter(){
 			public void mousePressed(MouseEvent e) {
 				requestFocus();
 			}
 			@Override
 			public void mouseExited(MouseEvent e) {
 				pointUnderMouse = null;
 			}
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				requestFocus();
 			}
         });
         addMouseMotionListener(new MouseMotionAdapter() {
         	@Override
         	public void mouseMoved(MouseEvent e) {
         		pointUnderMouse = new ScreenPoint(e.getX(), e.getY());
         	}
         });
      
     }
 
     public ScreenPoint getPointUnderMouse() {
     	return pointUnderMouse;
     }
     
     public void setMouseOver(Token token) {
     	if (tokenUnderMouse == token) {
     		return;
     	}
     	
     	tokenUnderMouse = token;
     	repaint();
     }
     
     @Override
     public boolean isOpaque() {
     	return false;
     }
     
 	public void addMoveSelectionSet (String playerId, GUID keyToken, Set<GUID> tokenList, boolean clearLocalSelected) {
 		
 		// I'm not supposed to be moving a token when someone else is already moving it
 		if (clearLocalSelected) {
 			for (GUID guid : tokenList) {
 				
 				selectedTokenSet.remove (guid);
 			}
 		}
 		
 		selectionSetMap.put(keyToken, new SelectionSet(playerId, keyToken, tokenList));
 		repaint();
 	}
 
 	public void updateMoveSelectionSet (GUID keyToken, ZonePoint offset) {
 		
 		SelectionSet set = selectionSetMap.get(keyToken);
 		if (set == null) {
 			return;
 		}
 		
 		Token token = zone.getToken(keyToken);
 		set.setOffset(offset.x - token.getX(), offset.y - token.getY());
 		repaint();
 	}
 
 	public void toggleMoveSelectionSetWaypoint(GUID keyToken, ZonePoint location) {
 		SelectionSet set = selectionSetMap.get(keyToken);
 		if (set == null) {
 			return;
 		}
 		
 		set.toggleWaypoint(location);
 		repaint();
 	}
 	
 	public void removeMoveSelectionSet (GUID keyToken) {
 		
 		SelectionSet set = selectionSetMap.remove(keyToken);
 		if (set == null) {
 			return;
 		}
 		
 		repaint();
 	}
     
     public void commitMoveSelectionSet (GUID keyToken) {
 
         // TODO: Quick hack to handle updating server state
         SelectionSet set = selectionSetMap.get(keyToken);
 
         removeMoveSelectionSet(keyToken);
         MapTool.serverCommand().stopTokenMove(getZone().getId(), keyToken);
 
         for (GUID tokenGUID : set.getTokens()) {
             
             Token token = zone.getToken(tokenGUID);
             token.setX(set.getOffsetX() + token.getX());
             token.setY(set.getOffsetY() + token.getY());
             
             MapTool.serverCommand().putToken(zone.getId(), token);
         }
         
     }
 
 	public boolean isTokenMoving(Token token) {
 		
 		for (SelectionSet set : selectionSetMap.values()) {
 			
 			if (set.contains(token)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	protected void setViewOffset(int x, int y) {
 
 		zoneScale.setOffset(x, y);
 	}
 	
     public void centerOn(ZonePoint point) {
         
         int x = point.x;
         int y = point.y;
         
         x = getSize().width/2 - (int)(x*getScale());
         y = getSize().height/2 - (int)(y*getScale());
 
         setViewOffset(x, y);
         updateFog = true;
 
         repaint();
     }
     
     public void centerOn(CellPoint point) {
         centerOn(point.convertToZone(this));
     }
     
     /**
      * Clear internal caches and backbuffers
      */
     public void flush() {
         replacementImageMap.clear();
     }
     
     public Zone getZone() {
     	return zone;
     }
     
     public void addOverlay(ZoneOverlay overlay) {
         overlayList.add(overlay);
     }
 
     public void removeOverlay(ZoneOverlay overlay) {
         overlayList.remove(overlay);
     }
 
     /* (non-Javadoc)
 	 * @see javax.swing.JComponent#isRequestFocusEnabled()
 	 */
 	public boolean isRequestFocusEnabled() {
 		return true;
 	}
     
     public void moveGridBy(int dx, int dy) {
 
     	int gridOffsetX = zone.getGridOffsetX();
     	int gridOffsetY = zone.getGridOffsetY();
     	int gridSize = zone.getGridSize();
     	
         gridOffsetX += dx;
         gridOffsetY += dy;
 
         gridOffsetX %= gridSize;
         gridOffsetY %= gridSize;
 
         if (gridOffsetY > 0) {
             gridOffsetY = gridOffsetY - gridSize;
         }
         
         if (gridOffsetX > 0) {
             gridOffsetX = gridOffsetX - gridSize;
         }
 
         zone.setGridOffsetX(gridOffsetX);
         zone.setGridOffsetY(gridOffsetY);
         
         repaint();
     }
 
     public void adjustGridSize(int delta) {
         zone.setGridSize(Math.max(0, zone.getGridSize() + delta));
 
         repaint();
     }
 
     public void moveViewBy(int dx, int dy) {
 
     	setViewOffset(getViewOffsetX() + dx, getViewOffsetY() + dy);
         updateFog = true;
 
         repaint();
     }
 
     public void zoomReset() {
     	zoneScale.reset();
         repaint();
     }
 
     public void zoomIn(int x, int y) {
         zoneScale.zoomIn(x, y);
        updateFog = true;
         repaint();
     }
 
     public void zoomOut(int x, int y) {
         zoneScale.zoomOut(x, y);
        updateFog = true;
         repaint();
     }
 
     public void setView(int x, int y, int zoomIndex) {
     	
     	setViewOffset(x, y);
 
     	zoneScale.setIndex(zoomIndex);
         updateFog = true;
     	
     	repaint();
     }
     
     public abstract BufferedImage getMiniImage(int size);
     
     public void paintComponent(Graphics g) {
 
         Graphics2D g2d = (Graphics2D) g;
 		
         if (zone == null) { return; }
         int gridSize = (int) (zone.getGridSize() * getScale());
         
     	renderBoard(g2d);
         renderDrawableOverlay(g2d);
         renderTokenTemplates(g2d);
         if (AppState.isShowGrid() && gridSize >= MIN_GRID_SIZE) {renderGrid(g2d);}
         renderTokens(g2d);
 		renderMoveSelectionSets(g2d);
         renderLabels(g2d);
 		
         for (int i = 0; i < overlayList.size(); i++) {
             ZoneOverlay overlay = overlayList.get(i);
             overlay.paintOverlay(this, (Graphics2D) g);
         }
         
         if (!zone.isVisible()) {
         	GraphicsUtil.drawBoxedString(g2d, "Zone not visible to players", getSize().width/2, 20);
         }
         
         renderFog(g2d);
     }
 
     /**
      * Paint all of the token templates. 
      * 
      * @param g Paint on this graphic object.
      */
     private void renderTokenTemplates(Graphics2D g) {
       int gridSize = zone.getGridSize();
       int gridOffsetX = zone.getGridOffsetX();
       int gridOffsetY = zone.getGridOffsetY();
       float scale = zoneScale.getScale();
       int scaledGridSize = (int)getScaledGridSize();
       
       // Find tokens with template state
       // TODO: I really don't like this, it should be optimized
       for (Token token : zone.getTokens()) {
         for (String state : token.getStatePropertyNames()) {
           Object value = token.getState(state);
           if (value instanceof TokenTemplate) {
 
         	  // Only show if selected
         	  if (!selectedTokenSet.contains(token.getId())) {
         		  continue;
         	  }
         	  
             // Calculate the token bounds
             Rectangle bounds = new Rectangle();
             bounds.x = (int)(token.getX() * scale + zoneScale.getOffsetX()) + (int) (gridOffsetX * scale) + 1;
             bounds.y = (int)(token.getY() * scale + zoneScale.getOffsetY()) + (int) (gridOffsetY * scale) + 1;
             bounds.width = (int)(TokenSize.getWidth(token, gridSize) * scale)-1;
             bounds.height = (int)(TokenSize.getHeight(token, gridSize) * scale)-1;
             if (bounds.width < scaledGridSize) bounds.x += (scaledGridSize - bounds.width)/2;
             if (bounds.height < scaledGridSize) bounds.y += (scaledGridSize - bounds.height)/2;
             
             // Set up the graphics, paint the template, restore the graphics
             Shape clip = g.getClip();
             g.translate(bounds.x, bounds.y);
             ((TokenTemplate)value).paintTemplate(g, token, bounds, this);
             g.translate(-bounds.x, -bounds.y);
             g.setClip(clip);
           } 
         } 
       } 
     }
     private void renderLabels(Graphics2D g) {
         
     	labelLocationList.clear();
         for (Label label : zone.getLabels()) {
 
         	ZonePoint zp = new ZonePoint(label.getX(), label.getY());
         	if (!zone.isPointVisible(zp)) {
         		continue;
         	}
         	
             ScreenPoint sp = ScreenPoint.fromZonePoint(this, zp.x, zp.y);
             
             Rectangle bounds = GraphicsUtil.drawBoxedString(g, label.getLabel(), sp.x, sp.y);
             
             labelLocationList.add(new LabelLocation(bounds, label));
         }
     }
     
     private void renderFog(Graphics2D g) {
 
     	if (!zone.hasFog()) {
     		return;
     	}
     	
     	// Update back buffer overlay size
     	Dimension size = getSize();
     	if (fog == null || fog.getWidth() != size.width || fog.getHeight() != size.height) {
             
             int type = MapTool.getPlayer().isGM() ? Transparency.TRANSLUCENT : Transparency.BITMASK; 
     		fog = ImageUtil.createCompatibleImage (size.width, size.height, type);
 
     		updateFog = true;
     	}
     	
     	// Render back buffer
     	if (updateFog) {
     		Graphics2D fogG = fog.createGraphics();
     		fogG.setColor(Color.black);
     		fogG.fillRect(0, 0, fog.getWidth(), fog.getHeight());
     		
     		fogG.setComposite(AlphaComposite.Src);
     		fogG.setColor(new Color(0, 0, 0, 0));
 
     		Area area = zone.getExposedArea().createTransformedArea(AffineTransform.getScaleInstance(getScale(), getScale()));
     		area = area.createTransformedArea(AffineTransform.getTranslateInstance(zoneScale.getOffsetX(), zoneScale.getOffsetY()));
     		fogG.fill(area);
     		
     		fogG.dispose();
     		
     		updateFog = false;
     	}
     	
     	// Render fog
     	Composite oldComposite = g.getComposite();
     	if (MapTool.getPlayer().isGM()) {
     		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .40f));
     	}
     	g.drawImage(fog, 0, 0, this);
     	g.setComposite(oldComposite);
     }
 
     public void updateFog() {
     	updateFog = true;
     	repaint();
     }
     
     protected void renderDrawableOverlay(Graphics g) {
         
     	drawableRenderer.renderDrawables(g, zone.getDrawnElements(), zoneScale.getOffsetX(), zoneScale.getOffsetY(), getScale());
     }
     
     protected abstract void renderBoard(Graphics2D g);
     
     protected abstract void renderGrid(Graphics2D g);
     
 	protected void renderMoveSelectionSets(Graphics2D g) {
 	
         int gridSize = zone.getGridSize();
         float scale = zoneScale.getScale();
         int scaledGridSize = (int) getScaledGridSize();
 
 		for (SelectionSet set : selectionSetMap.values()) {
 			
 			Token keyToken = zone.getToken(set.getKeyToken());
 			ZoneWalker walker = set.getWalker();
 
 			int setOffsetX = set.getOffsetX();
 			int setOffsetY = set.getOffsetY();
 			
 			for (GUID tokenGUID : set.getTokens()) {
 				
 				Token token = zone.getToken(tokenGUID);
                 
                 // Perhaps deleted ?
                 if (token == null) {
                     continue;
                 }
 				
             	// Don't bother if it's not visible
             	if (!token.isVisible() && !MapTool.getPlayer().isGM()) {
             		continue;
             	}
 
             	Asset asset = AssetManager.getAsset(token.getAssetID());
 	            if (asset == null) {
 	                continue;
 	            }
 	            
                 ScreenPoint newScreenPoint = ScreenPoint.fromZonePoint(this, token.getX() + setOffsetX + zone.getGridOffsetX(), token.getY() + setOffsetY + zone.getGridOffsetY());
 				
 				// OPTIMIZE: combine this with the code in renderTokens()
 	            int width = TokenSize.getWidth(token, gridSize);
 	            int height = TokenSize.getHeight(token, gridSize);
 				
             	int scaledWidth = (int)(width * scale);
             	int scaledHeight = (int)(height * scale);
             	
 				// Show distance only on the key token
 				if (token == keyToken) {
 
 					renderPath(g, walker);
 				}
 
 				// Center token in cell if it is smaller than a single cell
                 if (scaledWidth < scaledGridSize) {
                 	newScreenPoint.x += (scaledGridSize - scaledWidth)/2;
                 }
                 if (scaledHeight < scaledGridSize) {
                     newScreenPoint.y += (scaledGridSize - scaledHeight)/2;
                 }
 				g.drawImage(ImageManager.getImage(AssetManager.getAsset(token.getAssetID()), this), newScreenPoint.x+1, newScreenPoint.y+1, scaledWidth, scaledHeight, this);
 
 				// Other details
 				if (token == keyToken) {
 
 					int y = newScreenPoint.y + scaledHeight + 10;
 					int x = newScreenPoint.x + scaledWidth/2;
                     
 					if (walker.getDistance() >= 1) {
 						GraphicsUtil.drawBoxedString(g, Integer.toString(walker.getDistance()), x, y);
 					}
 					if (set.getPlayerId() != null && set.getPlayerId().length() >= 1) {
 						GraphicsUtil.drawBoxedString(g, set.getPlayerId(), x, y + 20);
 					}
 				}
 				
 			}
 
 		}
 	}
 	
 	public void renderPath(Graphics2D g, ZoneWalker walker) {
 		Object oldRendering = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		CellPoint previousPoint = null;
 		Point previousHalfPoint = null;
 		// JOINTS
 		List<CellPoint> path = walker.getPath();
 		for (CellPoint p : path) {
 			
 			highlightCell(g, p, AppStyle.cellPathImage, 1.0f);
 			if (walker.isWaypoint(p) && previousPoint != null) {
 				highlightCell(g, p, AppStyle.cellWaypointImage, .333f);
 			}
 			previousPoint = p;
 		}
 
 		previousPoint = null;
 		for (CellPoint p : path) {
 
 			if (previousPoint != null) {
 				// LATER: Optimize this
 				ScreenPoint origin = ScreenPoint.fromZonePoint(this, previousPoint.x*zone.getGridSize()+zone.getGridOffsetX()+(zone.getGridSize()/2), previousPoint.y*zone.getGridSize() + zone.getGridOffsetY()+(zone.getGridSize()/2));
 				ScreenPoint destination = ScreenPoint.fromZonePoint(this, p.x*zone.getGridSize()+zone.getGridOffsetX()+(zone.getGridSize()/2), p.y*zone.getGridSize() + zone.getGridOffsetY()+(zone.getGridSize()/2));
 
 				int halfx = (int)((origin.x + destination.x)/2);
 				int halfy = (int)((origin.y + destination.y)/2);
 				Point halfPoint = new Point(halfx, halfy);
 
 				if (previousHalfPoint != null) {
 					g.setColor(Color.blue);
 					QuadCurve2D curve = new QuadCurve2D.Float(previousHalfPoint.x, previousHalfPoint.y, origin.x, origin.y, halfPoint.x, halfPoint.y);
 					g.draw(curve);
 				}
 
 				previousHalfPoint = halfPoint;
 			}
 			previousPoint = p;
 		}
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldRendering);		
 	}
 	
 	public void highlightCell(Graphics2D g, CellPoint point, BufferedImage image, float size) {
 		
 		int gridSize = (int) getScaledGridSize();
 		
 		// Top left of cell
 		int imgSize = (int)(gridSize * size);
 		ScreenPoint p = ScreenPoint.fromZonePoint(this, point.x*zone.getGridSize()+zone.getGridOffsetX(), point.y*zone.getGridSize() + zone.getGridOffsetY());
 
 		//g.drawImage(image, p.x+imgSize/2, p.y+imgSize/2, imgSize, imgSize, this);
 		g.drawImage(image, p.x + (int)((gridSize - imgSize)/2), p.y + (int)((gridSize-imgSize)/2), imgSize, imgSize, this);
 	}
 	
     protected void renderTokens(Graphics2D g) {
 
         int gridSize = zone.getGridSize();
         int gridOffsetX = zone.getGridOffsetX();
         int gridOffsetY = zone.getGridOffsetY();
         int scaledGridSize = (int)getScaledGridSize();
         
         Rectangle clipBounds = g.getClipBounds();
         float scale = zoneScale.getScale();
         Set<Rectangle> coveredTokenSet = new HashSet<Rectangle>();
         tokenLocationList.clear();
         for (Token token : zone.getTokens()) {
 
         	// Don't bother if it's not visible
         	if (!zone.isTokenVisible(token) && !MapTool.getPlayer().isGM()) {
         		continue;
         	}
         	
             int width = (int)(TokenSize.getWidth(token, gridSize) * scale)-1;
             int height = (int)(TokenSize.getHeight(token, gridSize) * scale)-1;
             
             // OPTIMIZE:
             int x = (int)(token.getX() * scale + zoneScale.getOffsetX()) + (int) (gridOffsetX * scale) + 1;
             int y = (int)(token.getY() * scale + zoneScale.getOffsetY()) + (int) (gridOffsetY * scale) + 1;
 
             if (width < scaledGridSize) {
                 x += (scaledGridSize - width)/2;
             }
             if (height < scaledGridSize) {
                 y += (scaledGridSize - height)/2;
             }
             
             
             Rectangle tokenBounds = new Rectangle(x, y, width, height);
             for (TokenLocation location : tokenLocationList) {
 
             	Rectangle r1 = location.bounds;
             	
             	// Are we covering anyone ?
             	if (tokenBounds.contains(r1)) {
 
             		// Are we covering someone that is covering someone ?
             		Rectangle oldRect = null;
             		for (Rectangle r2 : coveredTokenSet) {
             			
             			if (tokenBounds.contains(r2)) {
             				oldRect = r2;
             				break;
             			}
             		}
             		if (oldRect != null) {
             			coveredTokenSet.remove(oldRect);
             		}
             		coveredTokenSet.add(tokenBounds);
             	}
             }
             // Note the order where the top most token is at the end of the list
             tokenLocationList.add(new TokenLocation(tokenBounds, token));
 
             // OPTIMIZE:
 			BufferedImage image = null;
             Asset asset = AssetManager.getAsset(token.getAssetID());
             if (asset == null) {
                 MapTool.serverCommand().getAsset(token.getAssetID());
 
                 // In the mean time, show a placeholder
                 image = ImageManager.UNKNOWN_IMAGE;
             } else {
             
 				image = ImageManager.getImage(AssetManager.getAsset(token.getAssetID()), this);
             }
 
             // Only draw if we're visible
             // NOTE: this takes place AFTER resizing the image, that's so that the user
             // sufferes a pause only once while scaling, and not as new tokens are
             // scrolled onto the screen
             if (!tokenBounds.intersects(clipBounds)) {
                 continue;
             }
 
 			// Moving ?
 			if (isTokenMoving(token)) {
 				BufferedImage replacementImage = replacementImageMap.get(token);
 				if (replacementImage == null || replacementImage.getWidth() != width || replacementImage.getHeight() != height) {
 					replacementImage = ImageUtil.rgbToGrayscale(image);
 					
 					// TODO: fix this memory leak -> when to clean up the image (when selection set is removed)
 					replacementImageMap.put(token, replacementImage);
 				}
 				
 				image = replacementImage;
 			}
 
             // Draw image
             g.drawImage(image, x, y, width, height, this);
             
             // Check for state
             if (!token.getStatePropertyNames().isEmpty()) {
               
               // Set up the graphics so that the overlay can just be painted.
               Shape clip = g.getClip();
               g.translate(x, y);
               Rectangle bounds = new Rectangle(0, 0, width, height);
               Rectangle overlayClip = g.getClipBounds().intersection(bounds);
               g.setClip(overlayClip);
               
               // Check each of the set values
               for (String state : token.getStatePropertyNames()) {
                 Object stateValue = token.getState(state);
                 
                 // Check for the on/off states & paint them
                 if (stateValue instanceof Boolean && ((Boolean)stateValue).booleanValue()) {
                   TokenOverlay overlay =  TokenStates.getOverlay(state);
                   if (overlay != null) overlay.paintOverlay(g, token, bounds);
                 
                 // Check for an overlay state value and paint that
                 } else if (stateValue instanceof TokenOverlay) {
                   ((TokenOverlay)stateValue).paintOverlay(g, token, bounds);
                 } // endif
               } // endfor
               
               // Restore the graphics context
               g.translate(-x, -y);
               g.setClip(clip);
             } // endif
         }
         
         // Selection and labels
         for (TokenLocation location : tokenLocationList) {
         	
         	Rectangle bounds = location.bounds;
         	
         	// TODO: This isn't entirely accurate as it doesn't account for the actual text
         	// to be in the clipping bounds, but I'll fix that later
             if (!bounds.intersects(clipBounds)) {
                 continue;
             }
 
         	Token token = location.token;
 
         	boolean isSelected = selectedTokenSet.contains(token.getId());
         	if (isSelected) {
                 // Border
             	AppStyle.selectedBorder.paintAround(g, bounds);
         	}
 
         	if (AppState.isShowTokenNames() || isSelected || token == tokenUnderMouse) {
 
         		// Name
                 GraphicsUtil.drawBoxedString(g, token.getName(), bounds.x + bounds.width/2, bounds.y + bounds.height + 10);
             }
         }
         
         // Stacks
         for (Rectangle rect : coveredTokenSet) {
         	
         	BufferedImage stackImage = AppStyle.stackImage;
         	g.drawImage(stackImage, rect.x + rect.width - stackImage.getWidth() + 2, rect.y - 2, null);
         }
     }
 
     // LATER: I don't like this mechanism, it's too ugly, and exposes too much
     // of the internal workings.  Fix it later
     public void flush(Token token) {
     }
     
     public Set<GUID> getSelectedTokenSet() {
     	return selectedTokenSet;
     }
     
     public void selectToken(GUID tokenGUID) {
         if (tokenGUID == null) {
             return;
         }
  
         Token token = zone.getToken(tokenGUID);
         if (token == null) {
         	return;
         }
         
         if (!AppUtil.playerOwnsToken(token)) {
         	return;
         }
 
         if (!MapTool.getPlayer().isGM() && !zone.isTokenVisible(token)) {
         	return;
         }
         
     	selectedTokenSet.add(tokenGUID);
     	
     	repaint();
     }
     
     /**
      * Screen space rectangle
      * @param rect
      */
     public void selectTokens(Rectangle rect) {
     	
     	for (TokenLocation location : tokenLocationList) {
     		if (rect.intersects(location.bounds)) {
     			selectToken(location.token.getId());
     		}
     	}
     }
     
     public void clearSelectedTokens() {
     	selectedTokenSet.clear();
     	repaint();
     }
     
     public Rectangle getTokenBounds(Token token) {
     	
     	for (TokenLocation location : tokenLocationList) {
     		if (location.token == token) {
     			return location.bounds;
     		}
     	}
     	
     	return null;
     }
     
     public Rectangle getLabelBounds(Label label) {
     	
     	for (LabelLocation location : labelLocationList) {
     		if (location.label == label) {
     			return location.bounds;
     		}
     	}
     	
     	return null;
     }
     
 	/**
 	 * Returns the token at screen location x, y (not cell location). To get
 	 * the token at a cell location, use getGameMap() and use that.
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public Token getTokenAt (int x, int y) {
 		
 		List<TokenLocation> locationList = new ArrayList<TokenLocation>();
 		locationList.addAll(tokenLocationList);
 		Collections.reverse(locationList);
 		for (TokenLocation location : locationList) {
 			if (location.bounds.contains(x, y)) {
 				return location.token;
 			}
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Returns the label at screen location x, y (not cell location). To get
 	 * the token at a cell location, use getGameMap() and use that.
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public Label getLabelAt (int x, int y) {
 		
 		List<LabelLocation> labelList = new ArrayList<LabelLocation>();
 		labelList.addAll(labelLocationList);
 		Collections.reverse(labelList);
 		for (LabelLocation location : labelList) {
 			if (location.bounds.contains(x, y)) {
 				return location.label;
 			}
 		}
 		
 		return null;
 	}
 
     /**
      * Translate the zone coordinates in p and change them to cell coordinates.
      * Note that the result is not the cell x,y, but rather the zone x,y of the
      * cell the point is contained by
      * @param p
      * @return original point
      */
     public ZonePoint constrainToCell(ZonePoint p) {
         
         int gridSize = zone.getGridSize();
         
         int scalex = (p.x / gridSize);
         int scaley = (p.y / gridSize);
         
         // Handle +/- transition
         if (p.x < 0) {scalex --;}
         if (p.y < 0) {scaley --;}
         
         p.x = scalex * gridSize;
         p.y = scaley * gridSize;
 
         return p;
     }
     
     public int getViewOffsetX() {
         return zoneScale.getOffsetX();
     }
     
     public int getViewOffsetY() {
         return zoneScale.getOffsetY();
     }
     
   /**
    * Since the map can be scaled, this is a convenience method to find out
    * what cell is at this location. 
    * 
    * @param screenPoint Find the cell for this point.
    * @return The cell coordinates of the passed screen point.
    */
   public CellPoint getCellAt(ScreenPoint screenPoint) {
     
     float scale = zoneScale.getScale();
     
     int x = screenPoint.x;
     int y = screenPoint.y;
     
     // Translate
     x -= zoneScale.getOffsetX() + (int) (zone.getGridOffsetX() * scale);
     y -= zoneScale.getOffsetY() + (int) (zone.getGridOffsetY() * scale);
     
     // Scale
     x = (int)Math.floor(x / (zone.getGridSize() * scale));
     y = (int)Math.floor(y / (zone.getGridSize() * scale));
     
     return new CellPoint(x, y);
   }
   
     public float getScale() {
     	return zoneScale.getScale();
     }
 
     public int getScaleIndex() {
     	// Used when enforcing view
     	return zoneScale.getIndex();
     }
     
     public double getScaledGridSize() {
     	// Optimize: only need to calc this when grid size or scale changes
     	return getScale() * zone.getGridSize();
     }
 	
 	/**
 	 * Represents a movement set
 	 */
 	private class SelectionSet {
 		
 		private HashSet<GUID> selectionSet = new HashSet<GUID>();
 		private GUID keyToken;
 		private String playerId;
 		private ZoneWalker walker;
 		private Token token;
 		
 		// Pixel distance from keyToken's origin
         private int offsetX;
         private int offsetY;
 		
 		public SelectionSet(String playerId, GUID tokenGUID, Set<GUID> selectionList) {
 
 			selectionSet.addAll(selectionList);
 			keyToken = tokenGUID;
 			this.playerId = playerId;
 			
 			token = zone.getToken(tokenGUID);
 			CellPoint tokenPoint = new CellPoint(token.getX()/zone.getGridSize(), token.getY()/zone.getGridSize());
 			walker = new AStarEuclideanWalker(zone);
 			walker.setWaypoints(tokenPoint, tokenPoint);
 		}
 		
 		public ZoneWalker getWalker() {
 			return walker;
 		}
 		
 		public GUID getKeyToken() {
 			return keyToken;
 		}
 
 		public Set<GUID> getTokens() {
 			return selectionSet;
 		}
 		
 		public boolean contains(Token token) {
 			return selectionSet.contains(token.getId());
 		}
 		
 		public void setOffset(int x, int y) {
 
             offsetX = x;
             offsetY = y;
             
             // TODO: abstract this calculation
             int cellX = (token.getX()+offsetX)/zone.getGridSize();
             int cellY = (token.getY()+offsetY)/zone.getGridSize();
 			CellPoint point = new CellPoint(cellX, cellY);
 			walker.replaceLastWaypoint(point);
             
 		}
 
     /**
      * Add the waypoint if it is a new waypoint. If it is
      * an old waypoint remove it.
      * 
      * @param location The point where the waypoint is toggled.
      */
 		public void toggleWaypoint(ZonePoint location) {
 		  
 		  int cellX = (location.x)/zone.getGridSize();
 		  int cellY = (location.y)/zone.getGridSize();
 		  CellPoint point = new CellPoint(cellX, cellY);
       walker.toggleWaypoint(point);
 		}
 		
 		public int getOffsetX() {
 			return offsetX;
 		}
 		
 		public int getOffsetY() {
 			return offsetY;
 		}
 		
 		public String getPlayerId() {
 			return playerId;
 		}
 	}
 
 	private static class TokenLocation {
 		public Rectangle bounds;
 		public Token token;
 		
 		public TokenLocation(Rectangle bounds, Token token) {
 			this.bounds = bounds;
 			this.token = token;
 		}
 	}
 	
 	private static class LabelLocation {
 		public Rectangle bounds;
 		public Label label;
 		
 		public LabelLocation(Rectangle bounds, Label label) {
 			this.bounds = bounds;
 			this.label = label;
 		}
 	}
 	
 	////
     // DROP TARGET LISTENER
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
      */
     public void dragEnter(DropTargetDragEvent dtde) {}
 
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
      */
     public void dragExit(DropTargetEvent dte) {}
 
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
      */
     public void dragOver(DropTargetDragEvent dtde) {}
 
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
      */
     public void drop(DropTargetDropEvent dtde) {
 
     	// TODO: This section needs to be consolidated with ZoneSelectionPanel.drop()
     	Asset asset = TransferableHelper.getAsset(dtde);
 
     	if (asset != null) {
 	
 	        BufferedImage image = ImageManager.getImage(asset, this);
 	        Token token = new Token(MapToolUtil.nextTokenId(zone, asset.getName()), asset.getId(), image.getWidth(), image.getHeight());
 	        token.setSnapToGrid(AppState.isTokensStartSnapToGrid());
 	        
     		ZonePoint zp = ZonePoint.fromScreenPoint(this, (int)dtde.getLocation().getX(), (int)dtde.getLocation().getY());
 
 	        if (token.isSnapToGrid()) {
 	    		int hwidth = TokenSize.getWidth(token, zone.getGridSize())/2 * (zp.x > 0 ? 1 : -1);
 		        int hheight = TokenSize.getHeight(token, zone.getGridSize())/2 * (zp.y > 0 ? 1 : -1);
 		        
 		        zp.translate(hwidth, hheight);
 
 	        	CellPoint cp = zp.convertToCell(this);
 		        token.setX(cp.x * zone.getGridSize());
 		        token.setY(cp.y * zone.getGridSize());
 	        } else {
 	        	token.setX(zp.x);
 	        	token.setY(zp.y);
 	        }
 
 	        if (AppState.isDropTokenAsInvisible()) {
 	        	token.setVisible(false);
 	        }
 
 	        // He who drops, owns
 	        if (MapTool.getServerPolicy().useStrictTokenManagement() && !MapTool.getPlayer().isGM()) {
 	        	token.addOwner(MapTool.getPlayer().getName());
 	        }
 	        
 	        zone.putToken(token);
 
             MapTool.serverCommand().putToken(zone.getId(), token);
 
             // For convenience, select it
             clearSelectedTokens();
             selectToken(token.getId());
             
             dtde.dropComplete(true);
 	        repaint();
 	        return;
     	}
     	
     	dtde.dropComplete(false);
     }
 
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
      */
     public void dropActionChanged(DropTargetDragEvent dtde) {
         // TODO Auto-generated method stub
 
     }
 
     ////
     // COMPARABLE
     public int compareTo(Object o) {
     	if (!(o instanceof ZoneRenderer)) {
     		return 0;
     	}
     	
     	return zone.getCreationTime() < ((ZoneRenderer)o).zone.getCreationTime() ? -1 : 1;
     }
 }
