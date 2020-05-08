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
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Paint;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.Stroke;
 import java.awt.TexturePaint;
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
 import java.awt.geom.GeneralPath;
 import java.awt.geom.QuadCurve2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.swing.ImageBorder;
 import net.rptools.maptool.client.AppPreferences;
 import net.rptools.maptool.client.AppState;
 import net.rptools.maptool.client.AppStyle;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.MapToolUtil;
 import net.rptools.maptool.client.ScreenPoint;
 import net.rptools.maptool.client.TransferableHelper;
 import net.rptools.maptool.client.ui.Scale;
 import net.rptools.maptool.client.ui.token.TokenOverlay;
 import net.rptools.maptool.client.ui.token.TokenStates;
 import net.rptools.maptool.client.ui.token.TokenTemplate;
 import net.rptools.maptool.client.walker.ZoneWalker;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.CellPoint;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Grid;
 import net.rptools.maptool.model.GridCapabilities;
 import net.rptools.maptool.model.HexGrid;
 import net.rptools.maptool.model.Label;
 import net.rptools.maptool.model.ModelChangeEvent;
 import net.rptools.maptool.model.ModelChangeListener;
 import net.rptools.maptool.model.Path;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.TokenSize;
 import net.rptools.maptool.model.Vision;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.model.drawing.DrawnElement;
 import net.rptools.maptool.model.vision.FacingConicVision;
 import net.rptools.maptool.model.vision.RoundVision;
 import net.rptools.maptool.util.GraphicsUtil;
 import net.rptools.maptool.util.ImageManager;
 import net.rptools.maptool.util.TokenUtil;
 
 
 /**
  */
 public abstract class ZoneRenderer extends JComponent implements DropTargetListener, Comparable {
     private static final long serialVersionUID = 3832897780066104884L;
 
     // TODO: Perhaps make this a user defined limit
     public static final int HOVER_SIZE_THRESHOLD = 40;
     public static final int EDGE_LIMIT = 25; // can't move board past this edge
 
     private static final Color CELL_HIGHLIGHT_COLOR = new Color(0xff, 0xff, 0xff, 0x80);
     
     private static BufferedImage GRID_IMAGE;
     
     public static final int MIN_GRID_SIZE = 5;
     private static final Stroke HALO_STROKE = new BasicStroke(2);
     
     protected Zone zone;
 
     private Scale zoneScale;
     
     private DrawableRenderer drawableRenderer = new DrawableRenderer();
     
     private List<ZoneOverlay> overlayList = new ArrayList<ZoneOverlay>();
     private Map<Zone.Layer, List<TokenLocation>> tokenLocationMap = new HashMap<Zone.Layer, List<TokenLocation>>();
     private Set<GUID> selectedTokenSet = new HashSet<GUID>();
     private List<LabelLocation> labelLocationList = new LinkedList<LabelLocation>();
     private Set<Rectangle> coveredTokenSet = new HashSet<Rectangle>();
 
 	private Map<GUID, SelectionSet> selectionSetMap = new HashMap<GUID, SelectionSet>();
 
 	private BufferedImage fog;
 	private boolean updateFog;
 	
 	private GeneralPath facingArrow;
 	
 	private List<Token> showPathList = new ArrayList<Token>();
 	
     // Optimizations
     private Map<Token, BufferedImage> replacementImageMap = new HashMap<Token, BufferedImage>();
 
 	private Token tokenUnderMouse;
 
 	private ScreenPoint pointUnderMouse;
 	
 	private Zone.Layer activeLayer;
 	
 	private Timer repaintTimer;
 
 	private boolean isLoaded;
 	
 //    private FramesPerSecond fps = new FramesPerSecond();
 
 	static {
 		try {
 			GRID_IMAGE = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/grid.png");
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 	
     public ZoneRenderer(Zone zone) {
         if (zone == null) { throw new IllegalArgumentException("Zone cannot be null"); }
 
         this.zone = zone;
         zone.addModelChangeListener(new ZoneModelChangeListener());
         
         setFocusable(true);
         setZoneScale(new Scale());
         
         // DnD
         new DropTarget(this, this);
 
         // Focus
         addMouseListener(new MouseAdapter(){
 			public void mousePressed(MouseEvent e) {
 				requestFocusInWindow();
 			}
 			@Override
 			public void mouseExited(MouseEvent e) {
 				pointUnderMouse = null;
 			}
 			@Override
 			public void mouseEntered(MouseEvent e) {
 			}
         });
         addMouseMotionListener(new MouseMotionAdapter() {
         	@Override
         	public void mouseMoved(MouseEvent e) {
         		pointUnderMouse = new ScreenPoint(e.getX(), e.getY());
         	}
         });
         
 //        fps.start();
     }
     
     public void setRepaintTimer(Timer timer) {
     	repaintTimer = timer;
     }
 
     public void showPath(Token token) {
     	showPathList.add(token);
     }
     
     public void clearShowPaths() {
     	showPathList.clear();
     	repaint();
     }
     
     public Scale getZoneScale() {
     	return zoneScale;
     }
     
     protected void setZoneScale(Scale scale) {
     	zoneScale = scale;
     	
     	scale.addPropertyChangeListener(new PropertyChangeListener() {
     		public void propertyChange(java.beans.PropertyChangeEvent evt) {
     			updateFog();
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
 
 	public boolean hasMoveSelectionSetMoved(GUID keyToken, ZonePoint point) {
 		
 		SelectionSet set = selectionSetMap.get(keyToken);
 		if (set == null) {
 			return false;
 		}
 		
 		Token token = zone.getToken(keyToken);
 		int x = point.x - token.getX();
 		int y = point.y - token.getY();
 
 		return set.offsetX != x || set.offsetY != y;
 	}
 	
 	public void updateMoveSelectionSet (GUID keyToken, ZonePoint offset) {
 		
 		SelectionSet set = selectionSetMap.get(keyToken);
 		if (set == null) {
 			return;
 		}
 		
 		Token token = zone.getToken(keyToken);
 		
 //		int tokenWidth = (int)(TokenSize.getWidth(token, zone.getGrid().getSize()) * getScale());
 //		int tokenHeight = (int)(TokenSize.getHeight(token, zone.getGrid().getSize()) * getScale());
 //		
 //		// figure out screen bounds
 //		ScreenPoint tsp = ScreenPoint.fromZonePoint(this, token.getX(), token.getY());
 //		ScreenPoint dsp = ScreenPoint.fromZonePoint(this, offset.x, offset.y);
 //		ScreenPoint osp = ScreenPoint.fromZonePoint(this, token.getX() + set.offsetX, token.getY() + set.offsetY);
 //
 //		int strWidth = SwingUtilities.computeStringWidth(fontMetrics, set.getPlayerId());
 //		
 //		int x = Math.min(tsp.x, dsp.x) - strWidth/2-4/*playername*/;
 //		int y = Math.min(tsp.y, dsp.y);
 //		int width = Math.abs(tsp.x - dsp.x)+ tokenWidth + strWidth+8/*playername*/;
 //		int height = Math.abs(tsp.y - dsp.y)+ tokenHeight + 45/*labels*/;
 //		Rectangle newBounds = new Rectangle(x, y, width, height);
 //		
 //		x = Math.min(tsp.x, osp.x) - strWidth/2-4/*playername*/;
 //		y = Math.min(tsp.y, osp.y);
 //		width = Math.abs(tsp.x - osp.x)+ tokenWidth + strWidth+8/*playername*/;
 //		height = Math.abs(tsp.y - osp.y)+ tokenHeight + 45/*labels*/;
 //		Rectangle oldBounds = new Rectangle(x, y, width, height);
 //
 //		newBounds = newBounds.union(oldBounds);
 //		
 		set.setOffset(offset.x - token.getX(), offset.y - token.getY());
 
 		//repaint(newBounds.x, newBounds.y, newBounds.width, newBounds.height);
 		repaint();
 	}
 
 	public void toggleMoveSelectionSetWaypoint(GUID keyToken, CellPoint location) {
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
     
     public void commitMoveSelectionSet (GUID keyTokenId) {
 
         // TODO: Quick hack to handle updating server state
         SelectionSet set = selectionSetMap.get(keyTokenId);
 
         removeMoveSelectionSet(keyTokenId);
         MapTool.serverCommand().stopTokenMove(getZone().getId(), keyTokenId);
 
         Token keyToken = zone.getToken(keyTokenId);
         CellPoint originPoint = zone.getGrid().convert(new ZonePoint(keyToken.getX(), keyToken.getY()));
         Path path = set.getWalker() != null ? set.getWalker().getPath() : null;
         
         for (GUID tokenGUID : set.getTokens()) {
             
             Token token = zone.getToken(tokenGUID);
             
             CellPoint tokenCell = zone.getGrid().convert(new ZonePoint(token.getX(), token.getY()));
             
             int cellOffX = originPoint.x - tokenCell.x;
             int cellOffY = originPoint.y - tokenCell.y;
             
             token.applyMove(set.getOffsetX(), set.getOffsetY(), path != null ? path.derive(cellOffX, cellOffY) : null);
 
             // No longer need this version
             replacementImageMap.remove(token);
             
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
         
         x = getSize().width/2 - (int)(x*getScale())-1;
         y = getSize().height/2 - (int)(y*getScale())-1;
 
         setViewOffset(x, y);
 
         repaint();
     }
     
     public void centerOn(CellPoint point) {
         centerOn(zone.getGrid().convert(point));
     }
     
     /**
      * Clear internal caches and backbuffers
      */
     public void flush() {
     	ImageManager.flushImage(zone.getAssetID());
     	drawableRenderer.flush();
     	
         replacementImageMap.clear();
         
         isLoaded = false;
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
 
     public void moveViewBy(int dx, int dy) {
 
     	setViewOffset(getViewOffsetX() + dx, getViewOffsetY() + dy);
     }
 
     public void zoomReset() {
     	zoneScale.reset();
     }
 
     public void zoomIn(int x, int y) {
         zoneScale.zoomIn(x, y);
     }
 
     public void zoomOut(int x, int y) {
         zoneScale.zoomOut(x, y);
     }
 
     public void setView(int x, int y, int zoomIndex) {
     	
     	setViewOffset(x, y);
 
     	zoneScale.setIndex(zoomIndex);
     }
     
     public abstract BufferedImage getMiniImage(int size);
     
     public void paintComponent(Graphics g) {
 
     	if (repaintTimer != null) {
     		repaintTimer.restart();
     	}
     	
         Graphics2D g2d = (Graphics2D) g;
 		
         // Are we still waiting to show the zone ?
         if (isLoading()) {
         	Dimension size = getSize();
         	g2d.setColor(Color.black);
         	g2d.fillRect(0, 0, size.width, size.height);
         	
         	GraphicsUtil.drawBoxedString(g2d, "    Loading    ", size.width/2, size.height/2);
         	
         	return;
         }
 
         if (zone == null) { return; }
 
         // Clear internal state
         tokenLocationMap.clear();
         coveredTokenSet.clear();
 
         // Rendering pipeline
     	renderBoard(g2d);
         renderTokens(g2d, zone.getBackgroundTokens());
         renderDrawableOverlay(g2d);
         renderTokenTemplates(g2d);
         renderGrid(g2d);
         renderVision(g2d);
         renderTokens(g2d, zone.getNonBackgroundTokens());
 		renderMoveSelectionSets(g2d);
         renderLabels(g2d);
 		
         renderFog(g2d);
         
         for (int i = 0; i < overlayList.size(); i++) {
             ZoneOverlay overlay = overlayList.get(i);
             overlay.paintOverlay(this, (Graphics2D) g);
         }
         
         if (!zone.isVisible()) {
         	GraphicsUtil.drawBoxedString(g2d, "Zone not visible to players", getSize().width/2, 20);
         }
         
 //        fps.bump();
 //        g.setColor(Color.white);
 //        g.drawString(fps.getFramesPerSecond() + "", 10, 10);
 
 //        g2d.setColor(Color.red);
 //        g2d.drawRect(g.getClipBounds().x, g.getClipBounds().y, g.getClipBounds().width-1, g.getClipBounds().height-1);
     }
     
     private void renderVision(Graphics2D g) {
 
     	Object oldAntiAlias = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
     	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
     	
     	Area visibleArea = null;
     	for (Token token : zone.getAllTokens()) {
 
     		if (token.hasVision()) {
     			
             	// Don't bother if it's not a player token
            	if (!token.isVisible() && !MapTool.getPlayer().isGM()) {
             		continue;
             	}
 
             	int width = TokenSize.getWidth(token, zone.getGrid());
     			int height = TokenSize.getHeight(token, zone.getGrid());
     			
     			for (Vision vision : token.getVisionList()) {
     				
     				if (!vision.isEnabled()) {
     					continue;
     				}
     				
     				Area visionArea = vision.getArea(getZone());
     				if (visionArea == null) {
     					continue;
     				}
     				
     				int x = token.getX();
     				int y = token.getY();
     				switch(vision.getAnchor()) {
     				case CENTER:
     					x += width/2;
     					y += height/2;
     				}
     				
     				visionArea = FogUtil.calculateVisibility(x, y, visionArea, zone.getTopology());
     				if (visionArea == null) {
     					continue;
     				}
     				
     				visionArea.transform(AffineTransform.getScaleInstance(getScale(), getScale()));
     				visionArea.transform(AffineTransform.getTranslateInstance(getViewOffsetX(), getViewOffsetY()));
 
     				if (visibleArea == null) {
     					visibleArea = new Area();
     				}
     				visibleArea.add(visionArea);
     				
     				if (token == tokenUnderMouse) {
     					Stroke oldStroke = g.getStroke();
     			    	g.setColor(new Color(200, 200, 200));
     					g.draw(visionArea);
     					g.setStroke(oldStroke);
     				}
     			}
     		}
     	}
     	if (visibleArea != null) {
         	g.setColor(new Color(255, 255, 255, 100));
     		g.fill(visibleArea);
     	}
     	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntiAlias);
     }
     
     /**
      * Paint all of the token templates for selected tokens. 
      * 
      * @param g Paint on this graphic object.
      */
     private void renderTokenTemplates(Graphics2D g) {
 		float scale = zoneScale.getScale();
 		int scaledGridSize = (int) getScaledGridSize();
 
 		// Find tokens with template state
 		// TODO: I really don't like this, it should be optimized
 		for (Token token : zone.getAllTokens()) {
 			for (String state : token.getStatePropertyNames()) {
 				Object value = token.getState(state);
 				if (value instanceof TokenTemplate) {
 
 					// Only show if selected
 					if (!AppState.isShowLightRadius()) {
 						continue;
 					}
 
 					// Calculate the token bounds
 					int width = (int) (TokenSize.getWidth(token, zone.getGrid()) * scale) - 1;
 					int height = (int) (TokenSize.getHeight(token, zone.getGrid()) * scale) - 1;
 					ScreenPoint tokenScreenLocation = ScreenPoint.fromZonePoint(this, token.getX(), token.getY());
 					int x = tokenScreenLocation.x + 1;
 					int y = tokenScreenLocation.y + 1;
 					if (width < scaledGridSize) {
 						x += (scaledGridSize - width) / 2;
 					}
 					if (height < scaledGridSize) {
 						y += (scaledGridSize - height) / 2;
 					}
 					Rectangle bounds = new Rectangle(x, y, width, height);
 
 					// Set up the graphics, paint the template, restore the
 					// graphics
 					Shape clip = g.getClip();
 					g.translate(bounds.x, bounds.y);
 					((TokenTemplate) value).paintTemplate(g, token, bounds,this);
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
     	boolean useAlphaFog = AppPreferences.getUseTranslucentFog();
     	Dimension size = getSize();
     	if (fog == null || fog.getWidth() != size.width || fog.getHeight() != size.height) {
             
             int type = MapTool.getPlayer().isGM() && useAlphaFog ? Transparency.TRANSLUCENT : Transparency.BITMASK; 
     		fog = ImageUtil.createCompatibleImage (size.width, size.height, type);
 
     		updateFog = true;
     	}
     	
     	// Render back buffer
     	if (updateFog) {
     		Graphics2D fogG = fog.createGraphics();
         	if (MapTool.getPlayer().isGM() && !useAlphaFog) {
         		Paint paint = new TexturePaint(GRID_IMAGE, new Rectangle2D.Float(0, 0, GRID_IMAGE.getWidth(), GRID_IMAGE.getHeight()));
         		fogG.setPaint(paint);
         	} else {
         		fogG.setColor(Color.black);
         	}
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
     	if (MapTool.getPlayer().isGM() && useAlphaFog) {
     		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .40f));
     	}
     	g.drawImage(fog, 0, 0, this);
     	g.setComposite(oldComposite);
     }
 
     public void updateFog() {
     	updateFog = true;
     	repaint();
     }
     
     public void flushFog() {
     	fog = null;
     	repaint();
     }
     
     public boolean isLoading() {
 
     	if (isLoaded) {
     		// We're done, until the cache is cleared
     		return false;
     	}
     	
     	// Get a list of all the assets in the zone
     	Set<MD5Key> assetSet = new HashSet<MD5Key>();
     	assetSet.add(zone.getAssetID());
     	for (Token token : zone.getAllTokens()) {
     		assetSet.add(token.getAssetID());
     	}
     	
     	// Make sure they are loaded
     	boolean loaded = true;
     	for (MD5Key id : assetSet) {
     		
     		// Have we gotten the actual data yet ?
     		Asset asset = AssetManager.getAsset(id);
     		if (asset == null) {
     			loaded = false;
     			break;
     		}
     		
     		// Have we loaded the image into memory yet ?
     		Image image  = ImageManager.getImage(asset, new ImageObserver[]{}); 
     		if (image == null || image == ImageManager.UNKNOWN_IMAGE ) {
     			loaded = false;
     			break;
     		}
     	}
 
     	isLoaded = loaded;
     	return !isLoaded;
     }
     
     protected void renderDrawableOverlay(Graphics g) {
         
     	Rectangle viewport = new Rectangle(zoneScale.getOffsetX(), zoneScale.getOffsetY(), getSize().width, getSize().height);
     	List<DrawnElement> drawableList = new ArrayList<DrawnElement>();
     	drawableList.addAll(zone.getDrawnElements());
     	drawableRenderer.renderDrawables(g, drawableList, viewport, getScale());
     }
     
     protected abstract void renderBoard(Graphics2D g);
     
     protected void renderGrid(Graphics2D g) {
         int gridSize = (int) (zone.getGrid().getSize() * getScale());
     	if (!AppState.isShowGrid() || gridSize < MIN_GRID_SIZE) {
     		return;
     	}
     	
 		zone.getGrid().draw(this, g, g.getClipBounds());
     }
     
 	protected void renderMoveSelectionSets(Graphics2D g) {
 	
 		Grid grid = zone.getGrid();
         int gridSize = grid.getSize();
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
 	            
                 ScreenPoint newScreenPoint = ScreenPoint.fromZonePoint(this, token.getX() + setOffsetX, token.getY() + setOffsetY);
 				
 				// OPTIMIZE: combine this with the code in renderTokens()
 	            int width = TokenSize.getWidth(token, zone.getGrid());
 	            int height = TokenSize.getHeight(token, zone.getGrid());
 				
             	int scaledWidth = (int)(width * scale);
             	int scaledHeight = (int)(height * scale);
                 int scaledGridWidth = (int)(grid.getCellWidth()*getScale());
                 int scaledGridHeight = (int)(grid.getCellHeight()*getScale());
             	
                 int x = newScreenPoint.x + 1 + (int)(grid.getCellOffset().width*scale);
                 int y = newScreenPoint.y + 1 + (int)(grid.getCellOffset().height*scale);
                 
                 if (width < scaledGridWidth && token.isSnapToGrid()) {
                     x += (scaledGridWidth - scaledWidth)/2;
                 }
                 if (height < scaledGridHeight && token.isSnapToGrid()) {
                     y += (scaledGridHeight - scaledHeight)/2;
                 }
 
                 // Show distance only on the key token
 				if (token == keyToken) {
 
 					if (!token.isBackground()) {
 						if (!token.isStamp() && zone.getGrid().getCapabilities().isPathingSupported() && token.isSnapToGrid()) {
 							renderPath(g, walker.getPath(), width/gridSize, height/gridSize);
 						} else {
 							g.setColor(Color.black);
 							ScreenPoint originPoint = ScreenPoint.fromZonePoint(this, token.getX()+width/2+(int)(grid.getCellOffset().width*scale), token.getY()+height/2+(int)(grid.getCellOffset().height*scale));
 							g.drawLine(originPoint.x, originPoint.y, x + scaledWidth/2, y + scaledHeight/2);
 						}
 					}
 				}
 
 				// Center token in cell if it is smaller than a single cell
                 if (scaledWidth < scaledGridSize) {
                 	newScreenPoint.x += (scaledGridSize - scaledWidth)/2;
                 }
                 if (scaledHeight < scaledGridSize) {
                     newScreenPoint.y += (scaledGridSize - scaledHeight)/2;
                 }
                 
                 BufferedImage image = ImageManager.getImage(AssetManager.getAsset(token.getAssetID())); 
     			if (token.hasFacing() && (token.getTokenType() == Token.Type.TOP_DOWN || token.isStamp() || token.isBackground())) {
 
     				// Rotated
     				AffineTransform at = new AffineTransform();
     				at.translate(x, y);
     				at.rotate(Math.toRadians(-token.getFacing() - 90), scaledWidth/2, scaledHeight/2); // facing defaults to down, or -90 degrees
     				if (token.isSnapToScale()) {
     					at.scale((double)TokenSize.getWidth(token, zone.getGrid()) / token.getWidth(), (double)TokenSize.getHeight(token, zone.getGrid()) / token.getHeight());
     				} else {
     					at.scale((double) token.getWidth() / image.getWidth(), (double) token.getHeight() / image.getHeight());
     				}
     				at.scale(getScale(), getScale());
     	            g.drawImage(image, at, this);
     			
     			} else {
     				// Normal
     				g.drawImage(image, x, y, scaledWidth-1, scaledHeight-1, this);
     			}
                 
 
 				// Other details
 				if (token == keyToken) {
 
 					y +=  10 + scaledHeight;
 					x += scaledWidth/2;
                     
 					if (!token.isBackground()) {
 						if (AppState.getShowMovementMeasurements() && zone.getGrid().getCapabilities().isPathingSupported() && walker.getDistance() >= 1) {
 							GraphicsUtil.drawBoxedString(g, Integer.toString(walker.getDistance()), x, y);
 							y += 20;
 						}
 					}
 					if (set.getPlayerId() != null && set.getPlayerId().length() >= 1) {
 						GraphicsUtil.drawBoxedString(g, set.getPlayerId(), x, y);
 					}
 				}
 				
 			}
 
 		}
 	}
 	
 	public void renderPath(Graphics2D g, Path path, int width, int height) {
 		Object oldRendering = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		CellPoint previousPoint = null;
 		Point previousHalfPoint = null;
 		
 		Grid grid = zone.getGrid();
 		
 		Dimension cellOffset = grid.getCellOffset();
 
 		width = Math.max (width, 1);
 		height = Math.max(height, 1);
 		
 		int xOffset = (int)((width-1)*getScaledGridSize()/2);
 		int yOffset = (int)((height-1)*getScaledGridSize()/2);
 
 		List<CellPoint> cellPath = path.getCellPath();
 
 		Set<CellPoint> pathSet = new HashSet<CellPoint>();
 		Set<CellPoint> waypointSet = new HashSet<CellPoint>();
 		for (CellPoint p : cellPath) {
 
 			for (int x = 0; x < width; x++) {
 				for (int y = 0; y < height; y++) {
 					
 					pathSet.add(new CellPoint(p.x+x, p.y+y));
 				}
 			}
 			if (path.isWaypoint(p) && previousPoint != null) {
 				waypointSet.add(new CellPoint(p.x + width/2, p.y+height/2));
 			}
 			previousPoint = p;
 		}
 		for (CellPoint p : pathSet) {
 			highlightCell(g, p, grid.getCellHighlight(), 1.0f);
 //			highlightCell(g, p);
 		}
 		for (CellPoint p : waypointSet) {
 			highlightCell(g, p, AppStyle.cellWaypointImage, .333f);
 		}
 
 		// Line path
 		// TODO: Make this work with hexes
 		if (!(grid instanceof HexGrid)) {
 			previousPoint = null;
 			for (CellPoint p : cellPath) {
 	
 				if (previousPoint != null) {
 	
 					int ox = (int)(previousPoint.x*grid.getCellWidth()+grid.getOffsetX()+(grid.getCellWidth()/2) + cellOffset.width);
 					int oy = (int)(previousPoint.y*grid.getCellHeight() + grid.getOffsetY()+(grid.getCellHeight()/2) + cellOffset.height);
 					
 					int dx = (int)(p.x*grid.getCellWidth()+grid.getOffsetX()+(grid.getCellHeight()/2) + cellOffset.width);
 					int dy = (int)(p.y*grid.getCellHeight() + grid.getOffsetY()+(grid.getCellHeight()/2) + cellOffset.height);  
 						
 					ScreenPoint origin = ScreenPoint.fromZonePoint(this, ox, oy);
 					ScreenPoint destination = ScreenPoint.fromZonePoint(this, dx, dy);
 	
 					int halfx = (int)((origin.x + destination.x)/2);
 					int halfy = (int)((origin.y + destination.y)/2);
 					Point halfPoint = new Point(halfx, halfy);
 	
 					if (previousHalfPoint != null) {
 						g.setColor(Color.blue);
 						
 						int x1 = previousHalfPoint.x+xOffset+cellOffset.width;
 						int y1 = previousHalfPoint.y+yOffset+cellOffset.height;
 						
 						int x2 = origin.x+xOffset+cellOffset.width;
 						int y2 = origin.y+yOffset+cellOffset.height;
 						
 						int xh = halfPoint.x+xOffset+cellOffset.width;
 						int yh = halfPoint.y+yOffset+cellOffset.height;
 						
 						QuadCurve2D curve = new QuadCurve2D.Float(x1, y1, x2, y2, xh, yh);
 						g.draw(curve);
 					}
 	
 					previousHalfPoint = halfPoint;
 				}
 				previousPoint = p;
 			}
 		}
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldRendering);		
 	}
 	
 	public void highlightCell(Graphics2D g, CellPoint point, BufferedImage image, float size) {
 		
 		Grid grid = zone.getGrid();
 		double cwidth = grid.getCellWidth() * getScale();
 		double cheight = grid.getCellHeight() * getScale();
 
 		double iwidth = cwidth * size;
 		double iheight = cheight * size;
 		
 		// Top left of cell
 		ScreenPoint sp = point.convertToScreen(this);
 		
 		sp.x += zone.getGrid().getCellOffset().width * getScale() + 1;
 		sp.y += zone.getGrid().getCellOffset().height * getScale() + 1;
 		
 		g.drawImage(image, sp.x + (int)((cwidth - iwidth)/2), sp.y + (int)((cheight-iheight)/2), (int)iwidth, (int)iheight, this);
 	}
 
 	private BufferedImage cellShape;
 	private int lastScale;
 	public void highlightCell(Graphics2D g, CellPoint point) {
 
 		if (cellShape == null || lastScale != getZoneScale().getIndex()) {
 			
 			Grid grid = zone.getGrid();
 			Area shape = grid.getCellShape().createTransformedArea(AffineTransform.getScaleInstance(getScale(), getScale()));
 
 			// Top left of cell
 			ScreenPoint sp = point.convertToScreen(this);
 
 			Rectangle rect = shape.getBounds();
 			cellShape = new BufferedImage(rect.width, rect.height, Transparency.TRANSLUCENT);
 			Graphics2D g2d = cellShape.createGraphics();
 			g2d.setColor(CELL_HIGHLIGHT_COLOR);
 			g2d.fill(shape);
 			g2d.dispose();
 			
 			lastScale = getZoneScale().getIndex();
 		}
 		
 		highlightCell(g, point, cellShape, 1.0f);
 	}
 
 	/**
 	 * Get a list of tokens currently visible on the screen.  The list is ordered by location starting
 	 * in the top left and going to the bottom right
 	 * @return
 	 */
 	public List<Token> getTokensOnScreen() {
 		List<Token> list = new ArrayList<Token>();
 
 		// Always assume tokens, for now
 		List<TokenLocation> tokenLocationListCopy = new ArrayList<TokenLocation>();
 		tokenLocationListCopy.addAll(getTokenLocations(Zone.Layer.TOKEN));
 		for (TokenLocation location : tokenLocationListCopy) {
 			list.add(location.token);
 		}
 		
 		// Sort by location on screen, top left to bottom right
 		Collections.sort(list, new Comparator<Token>(){
 			public int compare(Token o1, Token o2) {
 				
 				if (o1.getY() < o2.getY()) {
 					return -1;
 				}
 				if (o1.getY() > o2.getY()) {
 					return 1;
 				}
 				if (o1.getX() < o2.getX()) {
 					return -1;
 				}
 				if (o1.getX() > o2.getX()) {
 					return 1;
 				}
 				
 				return 0;
 			}
 		});
 		
 		return list;
 	}
 	
 	public Zone.Layer getActiveLayer() {
 		return activeLayer != null ? activeLayer : Zone.Layer.TOKEN;
 	}
 	public void setActiveLayer(Zone.Layer layer) {
 		activeLayer = layer;
 		selectedTokenSet.clear();
 		repaint();
 	}
 	
 	/**
 	 * Get the token locations for the given layer, creates an empty list
 	 * if there are not locations for the given layer
 	 */
 	private List<TokenLocation> getTokenLocations(Zone.Layer layer) {
 		List<TokenLocation> list = tokenLocationMap.get(layer);
 		if (list != null) {
 			return list;
 		}
 		
 		list = new LinkedList<TokenLocation>();
 		tokenLocationMap.put(layer, list);
 	
 		return list;
 	}
 	
 	// TODO: I don't like this hardwiring
 	protected Shape getCircleFacingArrow(int angle, int size) {
 
 		int base = (int)(size * .75);
 		int width = (int)(size * .35);
 		
 		facingArrow = new GeneralPath();
 		facingArrow.moveTo(base, -width);
 		facingArrow.lineTo(size, 0);
 		facingArrow.lineTo(base, width);
 		facingArrow.lineTo(base, -width);
 		
 		return ((GeneralPath)facingArrow.createTransformedShape(AffineTransform.getRotateInstance(-Math.toRadians(angle)))).createTransformedShape(AffineTransform.getScaleInstance(getScale(), getScale()));
 	}
 	// TODO: I don't like this hardwiring
 	protected Shape getSquareFacingArrow(int angle, int size) {
 
 		int base = (int)(size * .75);
 		int width = (int)(size * .35);
 		
 		facingArrow = new GeneralPath();
 		facingArrow.moveTo(0, 0);
 		facingArrow.lineTo(-(size - base), -width);
 		facingArrow.lineTo(-(size - base), width);
 		facingArrow.lineTo(0, 0);
 		
 		return ((GeneralPath)facingArrow.createTransformedShape(AffineTransform.getRotateInstance(-Math.toRadians(angle)))).createTransformedShape(AffineTransform.getScaleInstance(getScale(), getScale()));
 	}
 	
     protected void renderTokens(Graphics2D g, List<Token> tokenList) {
 
     	Grid grid = zone.getGrid();
     	Dimension screenSize = getSize();
         int scaledGridWidth = (int)(grid.getCellWidth()*getScale());
         int scaledGridHeight = (int)(grid.getCellHeight()*getScale());
         
         Rectangle clipBounds = g.getClipBounds();
         float scale = zoneScale.getScale();
         double gridSize = getScaledGridSize();
         for (Token token : tokenList) {
 
         	// Don't bother if it's not visible
         	if (!zone.isTokenVisible(token) && !MapTool.getPlayer().isGM()) {
         		continue;
         	}
         	
         	if (token.isBackground() && isTokenMoving(token)) {
         		continue;
         	}
         	
             int width = (int)Math.ceil(TokenSize.getWidth(token, zone.getGrid()) * scale);
             int height = (int)Math.ceil(TokenSize.getHeight(token, zone.getGrid()) * scale);
             
             if (!token.isStamp() && !token.isBackground()) {
             	// Fit inside the grid
             	width --;
             	height --;
             }
             
             ScreenPoint tokenScreenLocation = ScreenPoint.fromZonePoint(this, token.getX(), token.getY());
             int x = tokenScreenLocation.x + 1 + (int)(grid.getCellOffset().width*scale);
             int y = tokenScreenLocation.y + 1 + (int)(grid.getCellOffset().height*scale);
 
             // Center the token
             if (width < scaledGridWidth && token.isSnapToGrid()) {
                 x += (scaledGridWidth - width)/2;
             }
             if (height < scaledGridHeight && token.isSnapToGrid()) {
                 y += (scaledGridHeight - height)/2;
             }
             
             Rectangle tokenBounds = new Rectangle(x, y, width, height);
             
             // Adjust based on facing
             
             
             if (x+width < 0 || x > screenSize.width || y+height < 0 || y > screenSize.height) {
             	// Not on the screen, don't have to worry about it
             	continue;
             }
 
             // Stacking check
             if (!token.isStamp() && !token.isBackground()) {
 	            for (TokenLocation location : getTokenLocations(Zone.Layer.TOKEN)) {
 	
 	            	Rectangle r1 = location.bounds;
 	            	
 	            	// Are we covering anyone ?
 	            	if (tokenBounds.intersects(r1)) {
 	
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
             }
             
             // Keep track of the location on the screen
             // Note the order where the top most token is at the end of the list
             List<TokenLocation> locationList = null;
             if (!token.isStamp() && !token.isBackground()) {
             	locationList = getTokenLocations(Zone.Layer.TOKEN);
             } else {
             	if (token.isStamp()) {
             		locationList = getTokenLocations(Zone.Layer.OBJECT);
             	} 
             	if (token.isBackground()) {
             		locationList = getTokenLocations(Zone.Layer.BACKGROUND);
             	}
             }
             if (locationList != null) {
             	locationList.add(new TokenLocation(tokenBounds, token));
             }
 
             // OPTIMIZE:
 			BufferedImage image = null;
             Asset asset = AssetManager.getAsset(token.getAssetID());
             if (asset == null) {
 
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
 				if (replacementImage == null) {
 
 					replacementImage = ImageUtil.rgbToGrayscale(image);
 					
 					replacementImageMap.put(token, replacementImage);
 				}
 				
 				image = replacementImage;
 			}
 
 			// Previous path
 			if (showPathList.contains(token) && token.getLastPath() != null) {
 				renderPath(g, token.getLastPath(), (int)(width/gridSize), (int)(height/gridSize));
 			}
 			
 			// Halo (TOPDOWN, CIRCLE)
 			if (token.hasHalo() && (token.getTokenType() == Token.Type.TOP_DOWN || token.getTokenType() == Token.Type.CIRCLE)) {
 				Stroke oldStroke = g.getStroke();
 				g.setStroke(HALO_STROKE);
 				g.setColor(token.getHaloColor());
 				g.drawRect(x, y, width, height);
 				g.setStroke(oldStroke);
 			}
 			
             // Draw image
 			if (token.hasFacing() && (token.getTokenType() == Token.Type.TOP_DOWN || token.isStamp() || token.isBackground())) {
 				// Rotated
 				AffineTransform at = new AffineTransform();
 				at.translate(x, y);
 				at.rotate(Math.toRadians(-token.getFacing() - 90), width/2, height/2); // facing defaults to down, or -90 degrees
 
 				if (token.isSnapToScale()) {
 					at.scale((double)TokenSize.getWidth(token, zone.getGrid()) / token.getWidth(), (double)TokenSize.getHeight(token, zone.getGrid()) / token.getHeight());
 				} else {
 					at.scale((double) token.getWidth() / image.getWidth(), (double) token.getHeight() / image.getHeight());
 				}
 				at.scale(getScale(), getScale());
 	            g.drawImage(image, at, this);
 			} else {
 				// Normal
 	            g.drawImage(image, x, y, width, height, this);
 			}
 			
 			// Halo (SQUARE)
 			if (token.hasHalo() && token.getTokenType() == Token.Type.SQUARE) {
 				Stroke oldStroke = g.getStroke();
 				g.setStroke(HALO_STROKE);
 				g.setColor(token.getHaloColor());
 				g.drawRect(x, y, width, height);
 				g.setStroke(oldStroke);
 			}
 			
 			// Facing ?
             // TODO: Optimize this by doing it once per token per facing
 			if (token.hasFacing()) {
 
 				Token.Type tokenType = token.getTokenType();
 				switch(tokenType) {
 				case CIRCLE:
 					int size = TokenSize.getWidth(token, zone.getGrid())/2;
 					
 					Shape arrow = getCircleFacingArrow(token.getFacing(), size);
 
 					int cx = x + width/2;
 					int cy = y + height/2;
 					
 					g.translate(cx, cy);
 					g.setColor(Color.yellow);
 					g.fill(arrow);
 					g.setColor(Color.darkGray);
 					g.draw(arrow);
 					g.translate(-cx, -cy);
 					break;
 				case SQUARE:
 					size = TokenSize.getWidth(token, zone.getGrid())/2;
 					
 					int facing = token.getFacing();
 					arrow = getSquareFacingArrow(facing, size);
 
 					cx = x + width/2;
 					cy = y + height/2;
 
 					// Find the edge of the image
 					int xp = width/2;
 					int yp = height/2;
 					
 					if (facing >= 45 && facing <= 135 || facing <= -45 && facing >= -135) {
 						xp = (int)(yp / Math.tan(Math.toRadians(facing)));
 						if (facing < 0 ) {
 							xp = -xp;
 							yp = -yp;
 						}
 					} else {
 						yp = (int)(xp * Math.tan(Math.toRadians(facing)));
 						if (facing > 135 || facing < -135) {
 							xp = -xp;
 							yp = -yp;
 						}
 					}
 
 					cx += xp;
 					cy -= yp;
 					
 					g.translate(cx, cy);
 					g.setColor(Color.yellow);
 					g.fill(arrow);
 					g.setColor(Color.darkGray);
 					g.draw(arrow);
 					g.translate(-cx, -cy);
 					break;
 				}
 			}
 			
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
                 }
               }
               
               // Restore the graphics context
               g.translate(-x, -y);
               g.setClip(clip);
             } 
         }
         
         // Selection and labels
         for (TokenLocation location : getTokenLocations(getActiveLayer())) {
         	
         	Rectangle bounds = location.bounds;
         	
         	// TODO: This isn't entirely accurate as it doesn't account for the actual text
         	// to be in the clipping bounds, but I'll fix that later
             if (!bounds.intersects(clipBounds)) {
                 continue;
             }
 
         	Token token = location.token;
 
         	boolean isSelected = selectedTokenSet.contains(token.getId());
         	if (isSelected) {
         		ImageBorder selectedBorder = token.isStamp() || token.isBackground() ? AppStyle.selectedStampBorder : AppStyle.selectedBorder;
                 // Border
     			if (token.hasFacing() && (token.getTokenType() == Token.Type.TOP_DOWN || token.isStamp() || token.isBackground())) {
     				AffineTransform oldTransform = g.getTransform();
 
     				// Rotated
     				g.translate(bounds.x, bounds.y);
     				g.rotate(Math.toRadians(-token.getFacing() - 90), bounds.width/2, bounds.height/2); // facing defaults to down, or -90 degrees
     				selectedBorder.paintAround(g, 0, 0, bounds.width, bounds.height);
 
     				g.setTransform(oldTransform);
     			} else {
     				selectedBorder.paintAround(g, bounds);
     			}
         	}
 
         	// Name
         	if (AppState.isShowTokenNames() || isSelected || token == tokenUnderMouse) {
 
         		String name = token.getName();
         		if (MapTool.getPlayer().isGM() && token.getGMName() != null && token.getGMName().length() > 0) {
         			name += " (" + token.getGMName() + ")";
         		}
         		
         		Color background = token.isVisible() ? Color.white : Color.gray;
         		Color foreground = token.isVisible() ? Color.black : Color.lightGray;
                 GraphicsUtil.drawBoxedString(g, name, bounds.x + bounds.width/2, bounds.y + bounds.height + 10, SwingUtilities.CENTER, background, foreground);
             }
         }
         
         // Stacks
         for (Rectangle rect : coveredTokenSet) {
         	
         	BufferedImage stackImage = AppStyle.stackImage;
         	g.drawImage(stackImage, rect.x + rect.width - stackImage.getWidth() + 2, rect.y - 2, null);
         }
     }
 
     public Set<GUID> getSelectedTokenSet() {
     	return selectedTokenSet;
     }
     
     public boolean isTokenSelectable(GUID tokenGUID) {
     	
         if (tokenGUID == null) {
             return false;
         }
  
         Token token = zone.getToken(tokenGUID);
         if (token == null) {
         	return false;
         }
         
         if (!AppUtil.playerOwns(token)) {
         	return false;
         }
 
         // FOR NOW: if you own the token, you can select it
 //        if (!AppUtil.playerOwns(token) && !zone.isTokenVisible(token)) {
 //        	return false;
 //        }
 //        
         return true;
     }
     
     public boolean selectToken(GUID tokenGUID) {
     	
     	if (!isTokenSelectable(tokenGUID)) {
     		return false;
     	}
         
     	selectedTokenSet.add(tokenGUID);
     	
     	repaint();
     	return true;
     }
     
     /**
      * Screen space rectangle
      */
     public void selectTokens(Rectangle rect) {
 
     	for (TokenLocation location : getTokenLocations(getActiveLayer())) {
     		if (rect.intersects(location.bounds)) {
     			selectToken(location.token.getId());
     		}
     	}
     }
     
     public void clearSelectedTokens() {
 		clearShowPaths();
     	selectedTokenSet.clear();
     	repaint();
     }
     
     public Rectangle getTokenBounds(Token token) {
     	
     	for (TokenLocation location : getTokenLocations(getActiveLayer())) {
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
 		locationList.addAll(getTokenLocations(getActiveLayer()));
 		Collections.reverse(locationList);
 		for (TokenLocation location : locationList) {
 			if (location.bounds.contains(x, y)) {
 				return location.token;
 			}
 		}
 		
 		return null;
 	}
 
 	public List<Token> getTokenStackAt (int x, int y) {
 		
 		List<Rectangle> stackList = new ArrayList<Rectangle>();
 		stackList.addAll(coveredTokenSet);
 		for (Rectangle bounds : stackList) {
 			if (bounds.contains(x, y)) {
 
 				List<Token> tokenList = new ArrayList<Token>();
 				for (TokenLocation location : getTokenLocations(getActiveLayer())) {
 					if (location.bounds.intersects(bounds)) {
 						tokenList.add(location.token);
 					}
 				}
 				
 				return tokenList;
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
 
     public int getViewOffsetX() {
         return zoneScale.getOffsetX();
     }
     
     public int getViewOffsetY() {
         return zoneScale.getOffsetY();
     }
 
     public void adjustGridSize(int delta) {
         zone.getGrid().setSize(Math.max(0, zone.getGrid().getSize() + delta));
 
         repaint();
     }
 
     public void moveGridBy(int dx, int dy) {
 
     	int gridOffsetX = zone.getGrid().getOffsetX();
     	int gridOffsetY = zone.getGrid().getOffsetY();
     	
         gridOffsetX += dx;
         gridOffsetY += dy;
 
         if (gridOffsetY > 0) {
             gridOffsetY = gridOffsetY - (int)zone.getGrid().getCellHeight();
         }
         
         if (gridOffsetX > 0) {
             gridOffsetX = gridOffsetX - (int)zone.getGrid().getCellWidth();
         }
 
         zone.getGrid().setOffset(gridOffsetX, gridOffsetY);
         
         repaint();
     }
 
   /**
    * Since the map can be scaled, this is a convenience method to find out
    * what cell is at this location. 
    * 
    * @param screenPoint Find the cell for this point.
    * @return The cell coordinates of the passed screen point.
    */
     public CellPoint getCellAt(ScreenPoint screenPoint) {
     
 		ZonePoint zp = screenPoint.convertToZone(this);
 		
 		return zone.getGrid().convert(zp);
     }
   
     public float getScale() {
     	return zoneScale.getScale();
     }
 
     public int getScaleIndex() {
     	// Used when enforcing view
     	return zoneScale.getIndex();
     }
     
     public void setScaleIndex(int index) {
     	zoneScale.setIndex(index);
     }
     
     public double getScaledGridSize() {
     	// Optimize: only need to calc this when grid size or scale changes
     	return getScale() * zone.getGrid().getSize();
     }
 
     /**
      * This makes sure that any image updates get refreshed.  This could be a little smarter.
      */
     @Override
     public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
     	repaint();
     	return super.imageUpdate(img, infoflags, x, y, w, h);
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
 
 			if (ZoneRenderer.this.zone.getGrid().getCapabilities().isPathingSupported()) {
 				
 				CellPoint tokenPoint = zone.getGrid().convert(new ZonePoint(token.getX(), token.getY()));
 
 				walker = ZoneRenderer.this.zone.getGrid().createZoneWalker();
 				walker.setWaypoints(tokenPoint, tokenPoint);
 			}
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
             
 			if (ZoneRenderer.this.zone.getGrid().getCapabilities().isPathingSupported()) {
 				CellPoint point = zone.getGrid().convert(new ZonePoint(token.getX()+x, token.getY()+y));
 	
 				walker.replaceLastWaypoint(point);
 			}            
 		}
 
     /**
      * Add the waypoint if it is a new waypoint. If it is
      * an old waypoint remove it.
      * 
      * @param location The point where the waypoint is toggled.
      */
 		public void toggleWaypoint(CellPoint location) {
 		  
 			walker.toggleWaypoint(location);
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
     public void dragOver(DropTargetDragEvent dtde) {
 
     }
 
     private void addTokens(List<Token> tokens, ZonePoint zp) {
         GridCapabilities gridCaps = zone.getGrid().getCapabilities();
         for (Token token : tokens) {
             
             // Check the name
             token.setName(MapToolUtil.nextTokenId(zone, token.getName()));
             
             // Get the snap to grid value for the current prefs and abilities
             token.setSnapToGrid(gridCaps.isSnapToGridSupported() && AppPreferences.getTokensStartSnapToGrid());
             if (gridCaps.isSnapToGridSupported() && token.isSnapToGrid()) {
                 zp = zone.getGrid().convert(zone.getGrid().convert(zp));
             }
             token.setX(zp.x);
             token.setY(zp.y);
             token.setVisible(!MapTool.getPlayer().isGM() || AppPreferences.getNewTokensVisible());
             
             // Set the image properties
             BufferedImage image = ImageManager.getImageAndWait(AssetManager.getAsset(token.getAssetID()));
             token.setTokenType(TokenUtil.guessTokenType((BufferedImage)image));
             token.setWidth(image.getWidth(null));
             token.setHeight(image.getHeight(null));
             
             // He who drops, owns, if there are not players already set
             if (!token.hasOwners() && !MapTool.getPlayer().isGM())
                 token.addOwner(MapTool.getPlayer().getName());
 
             // Token type
             if (getActiveLayer() == Zone.Layer.BACKGROUND) {
             	token.setLayer(Zone.Layer.BACKGROUND);
             	token.setSnapToScale(!AppPreferences.getBackgroundsStartFreesize());
             	token.setSnapToGrid(AppPreferences.getBackgroundsStartSnapToGrid());
             	token.setTokenType(Token.Type.TOP_DOWN);
             }
             if (getActiveLayer() == Zone.Layer.OBJECT) {
             	token.setLayer(Zone.Layer.OBJECT);
             	token.setSnapToScale(!AppPreferences.getStampsStartFreesize());
             	token.setSnapToGrid(AppPreferences.getStampsStartSnapToGrid());
             	token.setTokenType(Token.Type.TOP_DOWN);
             }
 
             // Save the token and tell everybody about it
             zone.putToken(token);
             MapTool.serverCommand().putToken(zone.getId(), token);
         } // endfor
         
         // For convenience, select them
         clearSelectedTokens();
         for (Token token : tokens)
             selectToken(token.getId());
         requestFocusInWindow();
         repaint();
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
      */
     public void drop(DropTargetDropEvent dtde) {
         final ZonePoint zp = new ScreenPoint((int) dtde.getLocation().getX(),
                 (int) dtde.getLocation().getY()).convertToZone(this);
         GridCapabilities gridCaps = zone.getGrid().getCapabilities();
         List<Token> tokens = null;
         List<Asset> assets = TransferableHelper.getAsset(dtde);
         if (assets != null) {
             tokens = new ArrayList<Token>(assets.size());
             for (Asset asset : assets) {
                 tokens.add(new Token(asset.getName(), asset.getId()));
             }
             addTokens(tokens, zp);
         } else {
             tokens = TransferableHelper.getTokens(dtde.getTransferable());
             if (tokens != null) {
                 addTokens(tokens, zp);
             }
         }
         dtde.dropComplete(tokens != null);
     }
 
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
      */
     public void dropActionChanged(DropTargetDragEvent dtde) {
         // TODO Auto-generated method stub
 
     }
 
     ////
     // ZONE MODEL CHANGE LISTENER
     private class ZoneModelChangeListener implements ModelChangeListener {
     	public void modelChanged(ModelChangeEvent event) {
     		if (event.getEvent() == Zone.Event.FOG_CHANGED) {
     			flushFog();
     		}
     		
     	}
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
