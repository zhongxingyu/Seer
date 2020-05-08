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
 package net.rptools.maptool.model;
 
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.geom.Area;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.maptool.client.AppPreferences;
 import net.rptools.maptool.model.drawing.DrawableColorPaint;
 import net.rptools.maptool.model.drawing.DrawablePaint;
 import net.rptools.maptool.model.drawing.DrawableTexturePaint;
 import net.rptools.maptool.model.drawing.DrawnElement;
 import net.rptools.maptool.util.StringUtil;
 
 /**
  * This object represents the maps that will appear for placement of {@link Token}s.  This
  * object extends Token because the background image is a scaled asset, which
  * is exactly the definition of a Token.
  */
 public class Zone extends BaseModel {
     
     public enum Event {
         TOKEN_ADDED,
         TOKEN_REMOVED,
         TOKEN_CHANGED,
         GRID_CHANGED,
         DRAWABLE_ADDED,
         DRAWABLE_REMOVED,
         FOG_CHANGED,
         LABEL_ADDED,
         LABEL_REMOVED,
         LABEL_CHANGED,
         TOPOLOGY_CHANGED
     }
     
 	public enum Layer {
 		TOKEN("Token"),
 		GM("GM"),
 		OBJECT("Object"),
 		BACKGROUND("Background");
 		
 		private String displayName;
 		
 		private Layer(String displayName) {
 			this.displayName = displayName;
 		}
 		
 		public String toString() {
 			return displayName;
 		}
 	}
     
 	public static final int DEFAULT_TOKEN_VISION_DISTANCE = 10000;
 	
     public static final int DEFAULT_FEET_PER_CELL = 5;
     
     public static final DrawablePaint DEFAULT_FOG = new DrawableColorPaint(Color.black);
     
     // The zones should be ordered.  We could have the server assign each zone
     // an incrementing number as new zones are created, but that would take a lot
     // more ellegance than we really need.  Instead, let's just keep track of the
     // time when it was created.  This should give us sufficient granularity, because
     // come on what's the likelihood of two GMs separately creating a new zone at exactly
     // the same millisecond since the epoc.
     private long creationTime = System.currentTimeMillis();
     
 	private GUID id = new GUID();
 
 	private Grid grid;
     private int gridColor = Color.black.getRGB();
     private float imageScaleX = 1;
     private float imageScaleY = 1;
     
     private int tokenVisionDistance = DEFAULT_TOKEN_VISION_DISTANCE;
     
     private int unitsPerCell = DEFAULT_FEET_PER_CELL;
     
     private List<DrawnElement> drawables = new LinkedList<DrawnElement>();
     private List<DrawnElement> gmDrawables = new LinkedList<DrawnElement>();
     private List<DrawnElement> objectDrawables = new LinkedList<DrawnElement>();
     private List<DrawnElement> backgroundDrawables = new LinkedList<DrawnElement>();
 
     private Map<GUID, Label> labels = new LinkedHashMap<GUID, Label>();
     private Map<GUID, Token> tokenMap = new HashMap<GUID, Token>();
     private List<Token> tokenOrderedList = new LinkedList<Token>();
 
     private Area exposedArea = new Area();
     private boolean hasFog;
 
     private Area topology = new Area();
 
     private DrawablePaint backgroundPaint;
     private MD5Key mapAsset;
     private DrawablePaint fogPaint;
     
     private String name;
     private boolean isVisible;
     
     // These are transitionary properties, very soon the width and height won't matter
     private int height;
     private int width;
     
     public static final Comparator<Token> TOKEN_Z_ORDER_COMPARATOR = new Comparator<Token>() {
     	public int compare(Token o1, Token o2) {
     		int lval = o1.getZOrder();
     		int rval = o2.getZOrder();
     		
     		return lval < rval ? -1 : lval == rval ? 0 : 1;
     	}
     };
     
     public Zone() {
         setGrid(new SquareGrid());
     }
 
     public void setBackgroundPaint(DrawablePaint paint) {
     	backgroundPaint = paint;
     }
 
     public void setMapAsset(MD5Key id) {
     	mapAsset = id;
     }
     
     public int getTokenVisionDistance() {
     	if (tokenVisionDistance == 0) {
     		// TODO: This is here to provide transition between pre 1.3b19 an 1.3b19.  Remove later
     		tokenVisionDistance = DEFAULT_TOKEN_VISION_DISTANCE;
     	}
     	return tokenVisionDistance;
     }
     
     public void setFogPaint(DrawablePaint paint) {
     	fogPaint = paint;
     }
 
     public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public MD5Key getMapAssetId() {
 		return mapAsset;
 	}
 	
 	public DrawablePaint getBackgroundPaint() {
     	return backgroundPaint;
     }
 	
 	public DrawablePaint getFogPaint() {
 		return fogPaint != null ? fogPaint : DEFAULT_FOG;
 	}
     
     public Zone(Zone zone) {
     	backgroundPaint = zone.backgroundPaint;
     	mapAsset = zone.mapAsset;
     	
     	setName(zone.getName());
 
     	try{
         	grid = (Grid)zone.grid.clone();
         	grid.setZone(this);
 		} catch (CloneNotSupportedException cnse) {
 			cnse.printStackTrace();
 		}
 
         unitsPerCell = zone.unitsPerCell;
         
         imageScaleX = zone.imageScaleX;
         imageScaleY = zone.imageScaleY;
         
 		if (zone.drawables != null) {
 			drawables = new LinkedList<DrawnElement>();
 			drawables.addAll(zone.drawables);
 		}
 		
 		if (zone.objectDrawables != null) {
 			objectDrawables = new LinkedList<DrawnElement>();
 			objectDrawables.addAll(zone.objectDrawables);
 		}
 
 		if (zone.backgroundDrawables != null) {
 			backgroundDrawables = new LinkedList<DrawnElement>();
 			backgroundDrawables.addAll(zone.backgroundDrawables);
 		}
 		
 		if (zone.gmDrawables != null) {
 			gmDrawables = new LinkedList<DrawnElement>();
 			gmDrawables.addAll(zone.gmDrawables);
 		}
 		
 		if (zone.labels != null) {
 			Iterator i = zone.labels.keySet().iterator();
 			while (i.hasNext()) {
 				this.putLabel( new Label( zone.labels.get(i.next()) ) );
 			}
 		}
 		
 		if (zone.tokenMap != null) {
 			Iterator i = zone.tokenMap.keySet().iterator();
 			while (i.hasNext()) {
 				this.putToken( new Token( zone.tokenMap.get(i.next()) ) );
 			}
 		}
 
         exposedArea = (Area)zone.exposedArea.clone();
         topology = (Area)zone.topology.clone();
         
         // in case the current map is visible but user wants the new map hidden or FOW
         hasFog = AppPreferences.getNewMapsHaveFOW();
         setVisible(AppPreferences.getNewMapsVisible());
         
             
     }
     
     public GUID getId() {
 		return id;
 	}
 
     
     
 	public int getHeight() {
 		return height;
 	}
 
 	public void setHeight(int height) {
 		this.height = height;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	public boolean isVisible() {
 		return isVisible;
 	}
 
 	public void setVisible(boolean isVisible) {
 		this.isVisible = isVisible;
 	}
 
 	public void setGrid(Grid grid) {
     	this.grid = grid;
     	grid.setZone(this);
         fireModelChangeEvent(new ModelChangeEvent(this, Event.GRID_CHANGED));
     }
 
     public Grid getGrid() {
     	return grid;
     }
     
     public int getGridColor() {
     	return gridColor;
     }
     
     public void setGridColor(int color) {
     	gridColor = color;
     }
     
     public boolean hasFog() {
     	return hasFog;
     }
     
     public float getImageScaleX() {
         return imageScaleX;
     }
 
     public void setImageScaleX(float imageScaleX) {
         this.imageScaleX = imageScaleX;
     }
 
     public float getImageScaleY() {
         return imageScaleY;
     }
 
     public void setImageScaleY(float imageScaleY) {
         this.imageScaleY = imageScaleY;
     }
 
     public void setHasFog(boolean flag) {
     	hasFog = flag;
         fireModelChangeEvent(new ModelChangeEvent(this, Event.FOG_CHANGED));
     }
 
     public boolean isPointVisible(ZonePoint point, Player.Role role) {
     	
     	if (!hasFog() || role == Player.Role.GM) {
     		return true;
     	}
     	
     	return exposedArea.contains(point.x, point.y);
     }
     
     public boolean isEmpty() {
     	return 
 			(drawables == null || drawables.size() == 0) && 
 			(gmDrawables == null || drawables.size() == 0) && 
 			(objectDrawables == null || drawables.size() == 0) && 
 			(backgroundDrawables == null || drawables.size() == 0) && 
     		(tokenOrderedList == null || tokenOrderedList.size() == 0) && 
     		(labels != null || labels.size() == 0);
     }
     
     public boolean isTokenVisible(Token token) {
 
         // Base case, nothing is visible
         if (!token.isVisible()) {
             return false;
         }
         
         // Base case, everything is visible
         if (!hasFog()) {
             return true;
         }
         
         // Token is visible, and there is fog
         int x = token.getX();
         int y = token.getY();
         Rectangle tokenSize = token.getBounds(this);
 
         return getExposedArea().intersects(x, y, tokenSize.width, tokenSize.height);
     }
     
     public void clearTopology() {
     	topology = new Area();
         fireModelChangeEvent(new ModelChangeEvent(this, Event.TOPOLOGY_CHANGED));
     }
     
     public void addTopology(Area area) {
     	topology.add(area);
         fireModelChangeEvent(new ModelChangeEvent(this, Event.TOPOLOGY_CHANGED));
     }
 
     public void removeTopology(Area area) {
     	topology.subtract(area);
         fireModelChangeEvent(new ModelChangeEvent(this, Event.TOPOLOGY_CHANGED));
     }
     
     public Area getTopology() {
     	return topology;
     }
     
     public void clearExposedArea() {
     	exposedArea = new Area();
         fireModelChangeEvent(new ModelChangeEvent(this, Event.FOG_CHANGED));
     }
     
     public void exposeArea(Area area) {
     	exposedArea.add(area);
         fireModelChangeEvent(new ModelChangeEvent(this, Event.FOG_CHANGED));
     }
     
     public void setFogArea(Area area) {
     	exposedArea = area;
         fireModelChangeEvent(new ModelChangeEvent(this, Event.FOG_CHANGED));
     }
     
     public void hideArea(Area area) {
     	exposedArea.subtract(area);
         fireModelChangeEvent(new ModelChangeEvent(this, Event.FOG_CHANGED));
     }
 
     public long getCreationTime() {
     	return creationTime;
     }
     
     public ZonePoint getNearestVertex(ZonePoint point) {
     	
     	int gridx = (int)Math.round((point.x - grid.getOffsetX()) / (double)grid.getCellWidth());
     	int gridy = (int)Math.round((point.y - grid.getOffsetY()) / (double)grid.getCellHeight());
     	
 //    	System.out.println("gx:" + gridx + " zx:" + (gridx * grid.getCellWidth() + grid.getOffsetX()));
     	return new ZonePoint((int)(gridx * grid.getCellWidth() + grid.getOffsetX()), (int)(gridy * grid.getCellHeight() + grid.getOffsetY()));
     }
     
     public Area getExposedArea() {
     	return exposedArea;
     }
     
     public int getUnitsPerCell() {
     	return unitsPerCell;
     }
     
     public void setUnitsPerCell(int unitsPerCell) {
     	this.unitsPerCell = unitsPerCell;
     }
     
     public int getLargestZOrder() {
         return tokenOrderedList.size() > 0 ? tokenOrderedList.get(tokenOrderedList.size()-1).getZOrder() : 0;
     }
     
     public int getSmallestZOrder() {
         return tokenOrderedList.size() > 0 ? tokenOrderedList.get(0).getZOrder() : 0;
     }
     
     ///////////////////////////////////////////////////////////////////////////
     // labels
     ///////////////////////////////////////////////////////////////////////////
     public void putLabel(Label label) {
         
         boolean newLabel = labels.containsKey(label.getId());
         labels.put(label.getId(), label);
         
         if (newLabel) {
             fireModelChangeEvent(new ModelChangeEvent(this, Event.LABEL_ADDED, label));
         } else {
             fireModelChangeEvent(new ModelChangeEvent(this, Event.LABEL_CHANGED, label));
         }
     }
     
     public List<Label> getLabels() {
         return new ArrayList<Label>(this.labels.values());
     }
     
     public void removeLabel(GUID labelId) {
         
         Label label = labels.remove(labelId);
         if (label != null) {
             fireModelChangeEvent(new ModelChangeEvent(this, Event.LABEL_REMOVED, label));
         }
       }
     
     
     ///////////////////////////////////////////////////////////////////////////
     // drawables
     ///////////////////////////////////////////////////////////////////////////
 
     public void addDrawable(DrawnElement drawnElement) {
     	switch(drawnElement.getDrawable().getLayer()){
     		case OBJECT: objectDrawables.add(drawnElement); break;
     		case BACKGROUND: backgroundDrawables.add(drawnElement); break;
     		case GM: gmDrawables.add(drawnElement); break;
     		default:
     			drawables.add(drawnElement);
     			
     	}
     	
         fireModelChangeEvent(new ModelChangeEvent(this, Event.DRAWABLE_ADDED, drawnElement));
     }
     
     public List<DrawnElement> getDrawnElements() {
     	return getDrawnElements(Zone.Layer.TOKEN);
     }
     
     public List<DrawnElement> getObjectDrawnElements() {
     	return getDrawnElements(Zone.Layer.OBJECT);
     }
     
     public List<DrawnElement> getGMDrawnElements() {
     	return getDrawnElements(Zone.Layer.GM);
     }
     
     public List<DrawnElement> getBackgroundDrawnElements() {
     	return getDrawnElements(Zone.Layer.BACKGROUND);
     }
 
     public List<DrawnElement> getDrawnElements(Zone.Layer layer) {
     	switch(layer) {
     	case OBJECT: return objectDrawables;
     	case GM: return gmDrawables;
     	case BACKGROUND: return backgroundDrawables;
     	default: return drawables;
     	}
     }
     
     public void removeDrawable(GUID drawableId) {
     	// Since we don't know anything about the drawable, look through all the layers
     	removeDrawable(drawables, drawableId);
     	removeDrawable(backgroundDrawables, drawableId);
     	removeDrawable(objectDrawables, drawableId);
     	removeDrawable(gmDrawables, drawableId);
     }
 
     private void removeDrawable(List<DrawnElement> drawableList, GUID drawableId) {
         ListIterator<DrawnElement> i = drawableList.listIterator();
         while (i.hasNext()) {
             DrawnElement drawable = i.next();
             if (drawable.getDrawable().getId().equals(drawableId)) {
               i.remove();
               
               fireModelChangeEvent(new ModelChangeEvent(this, Event.DRAWABLE_REMOVED, drawable));
               return;
             }
         }
     }
     
     ///////////////////////////////////////////////////////////////////////////
     // tokens
     ///////////////////////////////////////////////////////////////////////////
     public void putToken(Token token) {
         boolean newToken = !tokenMap.containsKey(token.getId());
 
         this.tokenMap.put(token.getId(), token);
         
         // LATER: optimize this
         tokenOrderedList.remove(token);
         tokenOrderedList.add(token);
 
         Collections.sort(tokenOrderedList, TOKEN_Z_ORDER_COMPARATOR);
 
         if (newToken) {
             
             fireModelChangeEvent(new ModelChangeEvent(this, Event.TOKEN_ADDED, token));
         } else {
             fireModelChangeEvent(new ModelChangeEvent(this, Event.TOKEN_CHANGED, token));
         }
     }
     
     public void removeToken(GUID id) {
         Token token = this.tokenMap.remove(id);
         if (token != null) {
         	tokenOrderedList.remove(token);
            fireModelChangeEvent(new ModelChangeEvent(this, Event.TOKEN_REMOVED, token));
         }
     }
 	
 	public Token getToken(GUID id) {
 		return tokenMap.get(id);
 	}
 	
 	/**
 	 * Returns the first token with a given name.  The name is matched case-insensitively.
 	 */
 	public Token getTokenByName(String name) {
 		for (Token token : getAllTokens()) {
 			if (StringUtil.isEmpty(token.getName())) {
 				continue;
 			}
 
 			if (token.getName().equalsIgnoreCase(name)) {
 				return token;
 			}
 		}
 		
 		return null;
 	}
 	
 	public Token resolveToken(String identifier) {
 		
 		Token token = getTokenByName(identifier);
 		if (token == null) {
 			token = getTokenByGMName(identifier);
 		}
 		if (token == null) {
 			try {
 				token = getToken(GUID.valueOf(identifier));
 			} catch (Exception e) {
 				// indication of not a GUID, OK to ignore
 			}
 		}
 		return token;
 	}
 	
 	/**
 	 * Returns the first token with a given GM name.  The name is matched case-insensitively.
 	 */
 	public Token getTokenByGMName(String name) {
 		for (Token token : getAllTokens()) {
 			if (StringUtil.isEmpty(token.getGMName())) {
 				continue;
 			}
 			
 			if (token.getGMName().equalsIgnoreCase(name)) {
 				return token;
 			}
 		}
 		
 		return null;
 	}
 
 	public List<DrawnElement> getAllDrawnElements() {
 		List<DrawnElement> list = new ArrayList<DrawnElement>();
 		
 		list.addAll(getDrawnElements());
 		list.addAll(getObjectDrawnElements());
 		list.addAll(getBackgroundDrawnElements());
 		list.addAll(getGMDrawnElements());
 		
 		return list;
 	}
 	
     public List<Token> getAllTokens() {
         return Collections.unmodifiableList(new ArrayList<Token>(tokenOrderedList));
     }
     
     public Set<MD5Key> getAllAssetIds() {
     	
     	Set<MD5Key> idSet = new HashSet<MD5Key>();
 
     	// Zone
     	if (getBackgroundPaint() instanceof DrawableTexturePaint) {
     		idSet.add(((DrawableTexturePaint)getBackgroundPaint()).getAssetId());
     	}
     	idSet.add(getMapAssetId());
     	if (getFogPaint() instanceof DrawableTexturePaint) {
     		idSet.add(((DrawableTexturePaint)getFogPaint()).getAssetId());
     	}
     	
     	// Tokens
     	for (Token token : getAllTokens()) {
     		idSet.addAll(token.getAllImageAssets());
     	}
     	
 		// Painted textures
 		for (DrawnElement drawn : getAllDrawnElements()) {
 			DrawablePaint paint = drawn.getPen().getPaint(); 
 			if (paint instanceof DrawableTexturePaint) {
 				idSet.add(((DrawableTexturePaint)paint).getAssetId());
 			}
 			
 			paint = drawn.getPen().getBackgroundPaint();
 			if (paint instanceof DrawableTexturePaint) {
 				idSet.add(((DrawableTexturePaint)paint).getAssetId());
 			}
 		}
 		
 		// It's easier to just remove null at the end than to do a is-null check on each asset
 		idSet.remove(null);
 		
 		return idSet;
     }
 
     /**
      * This is the list of non-stamp tokens, both pc and npc
      */
     public List<Token> getTokens() {
     	List<Token> copy = new LinkedList<Token>(tokenOrderedList);
     	for (ListIterator<Token> iter = copy.listIterator(); iter.hasNext();) {
     		Token token = iter.next();
     		if (token.isStamp()) {
     			iter.remove();
     		}
     	}
         return Collections.unmodifiableList(copy);
     }
     
     public List<Token> getStampTokens() {
     	List<Token> copy = new LinkedList<Token>(tokenOrderedList);
     	for (ListIterator<Token> iter = copy.listIterator(); iter.hasNext();) {
     		Token token = iter.next();
     		if (!token.isObjectStamp()) {
     			iter.remove();
     		}
     	}
         return Collections.unmodifiableList(copy);
     }
     public List<Token> getPlayerTokens() {
     	List<Token> copy = new LinkedList<Token>(tokenOrderedList);
     	for (ListIterator<Token> iter = copy.listIterator(); iter.hasNext();) {
     		Token token = iter.next();
     		if (token.getType() != Token.Type.PC) {
     			iter.remove();
     		}
     	}
         return Collections.unmodifiableList(copy);
     }
     public List<Token> getBackgroundStamps() {
     	List<Token> copy = new LinkedList<Token>(tokenOrderedList);
     	for (ListIterator<Token> iter = copy.listIterator(); iter.hasNext();) {
     		Token token = iter.next();
     		if (!token.isBackgroundStamp()) {
     			iter.remove();
     		}
     	}
         return Collections.unmodifiableList(copy);
     }
     public List<Token> getGMStamps() {
     	List<Token> copy = new LinkedList<Token>(tokenOrderedList);
     	for (ListIterator<Token> iter = copy.listIterator(); iter.hasNext();) {
     		Token token = iter.next();
     		if (!token.isGMStamp()) {
     			iter.remove();
     		}
     	}
         return Collections.unmodifiableList(copy);
     }
     
 }
