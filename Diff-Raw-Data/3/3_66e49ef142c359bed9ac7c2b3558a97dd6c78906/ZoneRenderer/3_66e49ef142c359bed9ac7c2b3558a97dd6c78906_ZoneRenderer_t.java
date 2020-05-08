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
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JComponent;
 
 import net.rptools.clientserver.hessian.client.ClientConnection;
 import net.rptools.maptool.client.swing.SwingUtil;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.TokenSize;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.server.MapToolServer;
 import net.rptools.maptool.util.ImageManager;
 
 
 /**
  */
 public abstract class ZoneRenderer extends JComponent implements DropTargetListener {
     private static final long serialVersionUID = 3832897780066104884L;
 
     // TODO: Perhaps make this a user defined limit
     public static final int HOVER_SIZE_THRESHOLD = 40;
     public static final int EDGE_LIMIT = 25; // can't move board past this edge
 	
     public static final int MIN_GRID_SIZE = 10;
     
     protected Zone              zone;
 
     protected BufferedImage     backgroundImage;
 
     protected int               offsetX;
     protected int               offsetY;
 
     protected boolean           showGrid;
     protected Color             gridColor = new Color (150, 150, 150);
 
     protected int               scaleIndex;
     protected static float[]    scaleArray  = new float[] { .25F, .30F, .40F, .50F, .60F, .75F, 1F, 1.25F, 1.5F, 1.75F, 2F, 3F, 4F};
     protected static int SCALE_1TO1_INDEX; // Automatically scanned for
 
     private DrawableRenderer drawableRenderer = new DrawableRenderer();
     
     private Set<Token> zoomedTokenSet = new HashSet<Token>();
     private List<ZoneOverlay> overlayList = new ArrayList<ZoneOverlay>();
     private Map<Rectangle, Token> tokenBoundsMap = new HashMap<Rectangle, Token>();
     private Set<Token> selectedTokenSet = new HashSet<Token>();
 
     static {
     	for (int i = 0; i < scaleArray.length; i++) {
     		if (scaleArray[i] == 1) {
     			SCALE_1TO1_INDEX = i;
     			break;
     		}
     	}
     }
     
     public ZoneRenderer(Zone zone) {
         if (zone == null) { throw new IllegalArgumentException("Zone cannot be null"); }
 
         this.zone = zone;
         scaleIndex = SCALE_1TO1_INDEX;
         
         // DnD
         new DropTarget(this, this);
 
         // Get focus when clicked in
         addMouseListener(new MouseAdapter(){
 			public void mousePressed(MouseEvent e) {
 				requestFocus();
 			}
         });
     }
 
     /**
      * Clear internal caches and backbuffers
      */
     public void flush() {
     }
     
     public Zone getZone() {
     	return zone;
     }
     
     public void zoomToken(Token token) {
     	if (!zoomedTokenSet.contains(token)) {
     		zoomedTokenSet.add(token);
     		repaint();
     	}
     }
     
     public void unzoomToken(Token token) {
     	if (zoomedTokenSet.contains(token)) {
     		zoomedTokenSet.remove(token);
     		repaint();
     	}
     }
     
     public void addOverlay(ZoneOverlay overlay) {
         overlayList.add(overlay);
     }
 
     public void removeOverlay(ZoneOverlay overlay) {
         overlayList.remove(overlay);
     }
 
     public void toggleGrid() {
         showGrid = !showGrid;
         
         repaint();
     }
     
     public void setGridVisible(boolean visible) {
         showGrid = visible;
 
         repaint();
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
 
         offsetX += dx;
         offsetY += dy;
 
         repaint();
     }
 
     public void zoomReset() {
     	zoomTo(getSize().width/2, getSize().height/2, SCALE_1TO1_INDEX);
     }
 
     public void zoomIn(int x, int y) {
         zoomTo(x, y, scaleIndex + 1);
     }
 
     public void zoomOut(int x, int y) {
         zoomTo(x, y, scaleIndex - 1);
     }
 
     private void zoomTo(int x, int y, int index) {
 
         index = Math.max(index, 0);
         index = Math.min(index, scaleArray.length - 1);
 
         double oldScale = scaleArray[scaleIndex];
         double newScale = scaleArray[index];
 
         scaleIndex = index;
 
         // Keep the current pixel centered
         x -= offsetX;
         y -= offsetY;
 
         int newX = (int) ((x * newScale) / oldScale);
         int newY = (int) ((y * newScale) / oldScale);
 
         offsetX -= newX - x;
         offsetY -= newY - y;
 
         repaint();
     }
     
     public abstract BufferedImage getBackgroundImage();    
 
     public void paintComponent(Graphics g) {
 
         if (zone == null) { return; }
         int gridSize = (int) (zone.getGridSize() * getScale());
 
     	renderBoard(g);
         if (showGrid && gridSize >= MIN_GRID_SIZE) {renderGrid(g);}
         renderDrawableOverlay(g);
         renderTokens(g);
 		renderBorder(g);
 		
        for (int i = 0; i < overlayList.size(); i++) {
            ZoneOverlay overlay = overlayList.get(i);
             overlay.paintOverlay(this, (Graphics2D) g);
         }
     }
     
     protected void renderDrawableOverlay(Graphics g) {
         
     	drawableRenderer.renderDrawables(g, zone.getDrawnElements(), offsetX, offsetY, getScale());
     }
     
 	protected void renderBorder(Graphics g) { /* no op */ }
 	
     protected abstract void renderBoard(Graphics g);
     
     protected abstract void renderGrid(Graphics g);
     
     protected void renderTokens(Graphics g) {
 
         int gridSize = zone.getGridSize();
         int gridOffsetX = zone.getGridOffsetX();
         int gridOffsetY = zone.getGridOffsetY();
 
         tokenBoundsMap.clear();
         for (Token token : zone.getTokens()) {
 
             // OPTIMIZE:
             Asset asset = AssetManager.getAsset(token.getAssetID());
             if (asset == null) {
                 // TODO: this should be abstracted into the client better
                 if (MapToolClient.isConnected()) {
                     MapToolClient.getInstance().getConnection().callMethod(MapToolServer.COMMANDS.getAsset.name(), token.getAssetID());
                 }
                 continue;
             }
             
             Image image = ImageManager.getImage(asset);
             float scale = scaleArray[scaleIndex];
 
             int x = 0;
             int y = 0;
 
             int width = TokenSize.getWidth(token, gridSize);
             int height = TokenSize.getHeight(token, gridSize);
             
             // OPTIMIZE:
             x = (int)((token.getX() * gridSize) * scale + offsetX) + (int) (gridOffsetX * scaleArray[scaleIndex]) + 1;
             y = (int)((token.getY() * gridSize) * scale + offsetY) + (int) (gridOffsetY * scaleArray[scaleIndex]) + 1;
 
             if (scale >= 1.0 || !zoomedTokenSet.contains(token)) {
             	
             	width *= scale;
             	height *= scale;
             	
             } else {
 
             	Dimension dim = new Dimension(width, height);
             	SwingUtil.constrainTo(dim, HOVER_SIZE_THRESHOLD);
 
             	width = dim.width;
             	height = dim.height;
             	
             	x -= (width - (width*scale))/2;
             	y -= (height - (height*scale))/2;
             }
             
             g.drawImage(image, x, y, width, height, this);
 
             // OPTIMIZE: don't create new Rectangles each time
             Rectangle bounds = new Rectangle();
             bounds.setBounds(x, y, width, height);
             tokenBoundsMap.put(bounds, token);
 
             // Selected ?
             if (selectedTokenSet.contains(token)) {
             	ClientStyle.selectedBorder.paintAround((Graphics2D) g, x, y, width, height);
             }
         }
     }
 
     public Set<Token> getSelectedTokenSet() {
     	return selectedTokenSet;
     }
     
     public void selectToken(Token token) {
         if (token == null) {
             return;
         }
         
     	selectedTokenSet.add(token);
     	
     	repaint();
     }
     
     public void clearSelectedTokens() {
     	selectedTokenSet.clear();
     	
     	repaint();
     }
     
     public Rectangle getTokenBounds(Token token) {
     	
     	for (Rectangle rect : tokenBoundsMap.keySet()) {
     		if (tokenBoundsMap.get(rect) == token) {
     			return rect;
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
 		
 		for (Rectangle rect : tokenBoundsMap.keySet()) {
 			if (rect.contains(x, y)) {
 				return tokenBoundsMap.get(rect);
 			}
 		}
 		
 		return null;
 	}
 
 	public Point convertScreenToZone(int x, int y) {
 		
         double scale = scaleArray[scaleIndex];
         
         // Translate
         x -= offsetX;
         y -= offsetY;
         
         // Scale
         x = (int)(x / scale);
         y = (int)(y / scale);
         
         return new Point(x,y);
 	}
 	
     /**
      * Since the map can be scaled, this is a convenience method to find out
      * what cell is at this location
      * @param x
      * @param y
      * @return
      */
     public Point getCellAt (int x, int y) {
 
         double scale = scaleArray[scaleIndex];
         
         // Translate
         x -= offsetX + (int) (zone.getGridOffsetX() * scaleArray[scaleIndex]);
         y -= offsetY + (int) (zone.getGridOffsetY() * scaleArray[scaleIndex]);
         
 		// Scale
         x = (int)Math.floor(x / (zone.getGridSize() * scale));
         y = (int)Math.floor(y / (zone.getGridSize() * scale));
         
         return new Point(x, y);
     }
 
     public double getScale() {
     	return scaleArray[scaleIndex];
     }
     
     ////
     // DROP TARGET LISTENER
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
      */
     public void dragEnter(DropTargetDragEvent dtde) {
         // TODO Auto-generated method stub
 
     }
 
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
      */
     public void dragExit(DropTargetEvent dte) {
         // TODO Auto-generated method stub
 
     }
 
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
      */
     public void dragOver(DropTargetDragEvent dtde) {
         // TODO Auto-generated method stub
 
     }
 
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
      */
     public void drop(DropTargetDropEvent dtde) {
 
     	// TODO: This section needs to be consolidated with ZoneSelectionPanel.drop()
     	Asset asset = TransferableHelper.getAsset(dtde);
 
     	if (asset != null) {
 	        Point p = dtde.getLocation();
 	        p = getCellAt((int)p.getX(), (int)p.getY());
 	        int x = (int)p.getX();
 	        int y = (int)p.getY();
 	
 	        Token token = new Token(asset.getId());
 	        token.setX(x);
 	        token.setY(y);
 	
 	        zone.putToken(token);
 	
 	        // TODO: abstract this better
 	        if (MapToolClient.isConnected()) {
 	        	ClientConnection conn = MapToolClient.getInstance().getConnection();
 	        	
 	        	conn.callMethod(MapToolClient.COMMANDS.putToken.name(), zone.getId(), token);
 	        }
 	        
 	        repaint();
     	}
     }
 
     /* (non-Javadoc)
      * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
      */
     public void dropActionChanged(DropTargetDragEvent dtde) {
         // TODO Auto-generated method stub
 
     }
     
 	
 }
