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
 package net.rptools.maptool.model;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Transparency;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.transferable.TokenTransferData;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.functions.JSONMacroFunctions;
 import net.rptools.maptool.util.ImageManager;
 import net.rptools.maptool.util.StringUtil;
 import net.rptools.parser.ParserException;
 
 /**
  * This object represents the placeable objects on a map. For example an icon
  * that represents a character would exist as an {@link Asset} (the image
  * itself) and a location and scale.
  */
 public class Token extends BaseModel {
 	private GUID id = new GUID();
 	
 	public static final String FILE_EXTENSION = "rptok";
 	public static final String FILE_THUMBNAIL = "thumbnail";
 	
 	public static final String NAME_USE_FILENAME = "Use Filename";
 	public static final String NAME_USE_CREATURE = "Use \"Creature\"";
 	
 	public static final String NUM_INCREMENT = "Increment";
 	public static final String NUM_RANDOM = "Random 2-digit";
 	
 	public static final String NUM_ON_NAME = "Name";
 	public static final String NUM_ON_GM = "GM Name";
 	public static final String NUM_ON_BOTH = "Both";
 	
 	private boolean beingImpersonated = false;
 
 	public enum TokenShape {
 		TOP_DOWN("Top down"),
 		CIRCLE("Circle"),
 		SQUARE("Square");
 		
 		private String displayName;
 		
 		private TokenShape(String displayName) {
 			this.displayName = displayName;
 		}
 		
 		public String toString() {
 			return displayName;
 		}
 	}
 	
 	public enum Type {
 		PC,
 		NPC
 	}
 
 	public static final Comparator<Token> NAME_COMPARATOR = new Comparator<Token>() {
 		public int compare(Token o1, Token o2) {
 			return o1.getName().compareToIgnoreCase(o2.getName());
 		}
 	};
 
 	private Map<String, MD5Key> imageAssetMap;
 	private String currentImageAsset;
 
 	private int x;
 	private int y;
 	private int z;
 	
 	private int anchorX;
 	private int anchorY;
 	
 	private double sizeScale = 1;
 
 	private int lastX;
 	private int lastY;
 	private Path<? extends AbstractPoint> lastPath;
 	
 	private boolean snapToScale = true; // Whether the scaleX and scaleY
 										// represent snap-to-grid measurements
 
 	// These are the original image width and height
 	private int width;
 	private int height;
 	
 	private double scaleX = 1;
 	private double scaleY = 1;
 
 	private Map<Class<? extends Grid>, GUID> sizeMap;
 
 	private boolean snapToGrid = true; // Whether the token snaps to the
 										// current grid or is free floating
 
 	private boolean isVisible = true;
 
 	private String name;
 	private Set<String> ownerList;
 
 	private int ownerType;
 
 	private static final int OWNER_TYPE_ALL = 1;
 	private static final int OWNER_TYPE_LIST = 0;
 	
 	private String tokenShape;
 	private String tokenType;
 	private String layer;
 	private transient Zone.Layer actualLayer;
 	
 	private String propertyType = Campaign.DEFAULT_TOKEN_PROPERTY_TYPE;
 
 	private Integer facing = null;
 	
 	private Integer haloColorValue;
 	private transient Color haloColor;
     
     private Integer visionOverlayColorValue;
     private transient Color visionOverlayColor;
 	
 	private boolean isFlippedX;
 	private boolean isFlippedY;
 	
 	private MD5Key charsheetImage;
 	private MD5Key portraitImage;
 	
 	private List<AttachedLightSource> lightSourceList;
 	private String sightType;
 	private boolean hasSight;
 	
 	private String label;
 
   /**
    * The notes that are displayed for this token.
    */
 	private String notes;
   
 	private String gmNotes;
 	
 	private String gmName;
 	
 	/**
 	 * A state properties for this token. This allows state to be added that can
 	 * change appearance of the token.
 	 */
 	private Map<String, Object> state;
 	
 	/**
 	 * Properties
 	 */
 	private Map<String, Object> propertyMap;
 
 	private Map<String, String> macroMap;
 	private Map<Integer, Object> macroPropertiesMap;
 
 	private Map<String, String> speechMap;
 
 	// Deprecated, here to allow deserialization
 	private transient int size; // 1.3b16
 	private transient List<Vision> visionList; // 1.3b18
 	
 	public enum ChangeEvent {
 		name
 	}
 
 	public Token(Token token) {
 		this(token.name, token.getImageAssetId());
 		currentImageAsset = token.currentImageAsset;
 		
 		x = token.x;
 		y = token.y;
		z = token.z;
 
 		// These properties shouldn't be transferred, they are more transient and relate to token history, not to new tokens
 		//		lastX = token.lastX;
 		//		lastY = token.lastY;
 		//		lastPath = token.lastPath;
 		
 
 		snapToScale = token.snapToScale;
 		width = token.width;
 		height = token.height;
 		scaleX = token.scaleX;
 		scaleY = token.scaleY;
 		facing = token.facing;
 		tokenShape = token.tokenShape;
 		tokenType = token.tokenType;
 		haloColorValue = token.haloColorValue;
 
 		snapToGrid = token.snapToGrid;
 		isVisible = token.isVisible;
 		name = token.name;
 	    notes = token.notes;
 	    gmName = token.gmName;
 	    gmNotes = token.gmNotes;
 	    label = token.label;
 
 	    isFlippedX = token.isFlippedX;
 	    isFlippedY = token.isFlippedY;
 	    
 	    layer = token.layer;
         
         visionOverlayColor = token.visionOverlayColor;
 	    
         charsheetImage = token.charsheetImage;
         portraitImage = token.portraitImage;
         anchorX = token.anchorX;
         anchorY = token.anchorY;
         sizeScale = token.sizeScale;
         sightType = token.sightType;
         hasSight = token.hasSight;
         propertyType = token.propertyType;
         
 	    ownerType = token.ownerType;
 		if (token.ownerList != null) {
 			ownerList = new HashSet<String>();
 			ownerList.addAll(token.ownerList);
 		}
 
 		if (token.lightSourceList != null) {
 			lightSourceList = new ArrayList<AttachedLightSource>(token.lightSourceList);
 		}
 		
 		if (token.state != null) {
 			state.putAll(token.state);
 		}
     
 		if (token.propertyMap != null) {
 			propertyMap = new HashMap<String, Object>(token.propertyMap);
 		}
 
 		if (token.macroPropertiesMap != null) {
 			macroPropertiesMap = new HashMap<Integer, Object>(token.macroPropertiesMap);
 		}
 
 		// convert old-style macros
 		if (token.macroMap != null) {
 			macroMap = new HashMap<String, String>(token.macroMap);
 			loadOldMacros();
 		}
 
 		if (token.speechMap != null) {
 			speechMap = new HashMap<String, String>(token.speechMap);
 		}
 		
 		if (token.imageAssetMap != null) {
 			imageAssetMap.putAll(token.imageAssetMap);
 		}
 		
 		if (token.sizeMap != null) {
 			sizeMap = new HashMap<Class<? extends Grid>, GUID>(token.sizeMap);
 		}
 	}
 
 	public Token() {
 		imageAssetMap = new HashMap<String, MD5Key>();
 
 	}
 
 	public Token(MD5Key assetID) {
 		this("", assetID);
 	}
 
 	public Token(String name, MD5Key assetId) {
 		this.name = name;
 
 		state = new HashMap<String, Object>();
 		imageAssetMap = new HashMap<String, MD5Key>();
 
 		// NULL key is the default
 		imageAssetMap.put(null, assetId);
 		
 		// convert old-style macros
 		if (macroMap != null) {
 			loadOldMacros();
 		}
 	}
 
 	public void setHasSight(boolean hasSight) {
 		this.hasSight = hasSight;
 	}
 
 	public void setWidth(int width) {
 		this.width = width;
 	}
 	
 	public void setHeight(int height) {
 		this.height = height;
 	}
 	
 	public boolean isMarker() {
 		return isStamp() && (!StringUtil.isEmpty(notes) || !StringUtil.isEmpty(gmNotes) || portraitImage != null);
 	}
 	
 	public String getPropertyType() {
 		return propertyType;
 	}
 
 	public void setPropertyType(String propertyType) {
 		this.propertyType = propertyType;
 	}
 
 	public String getGMNotes() {
 		return gmNotes;
 	}
 	
 	public void setGMNotes(String notes) {
 		gmNotes = notes;
 	}
 	
 	public String getGMName() {
 		return gmName;
 	}
 	
 	public void setGMName(String name) {
 		gmName = name;
 	}
 	
 	public boolean hasHalo() {
 		return haloColorValue != null;
 	}
 	
 	public String getLabel() {
 		return label;
 	}
 
 	public void setLabel(String label) {
 		this.label = label;
 	}
 
 	public void setHaloColor(Color color) {
 		if (color != null) {
 			haloColorValue = color.getRGB();
 		} else {
 			haloColorValue = null;
 		}
 		haloColor = color;
 	}
 	
 	public Color getHaloColor() {
 		if (haloColor == null && haloColorValue != null) {
 			haloColor = new Color(haloColorValue);
 		}
 		
 		return haloColor;
 	}
 	
 	public boolean isObjectStamp() {
 		return getLayer() == Zone.Layer.OBJECT;
 	}
 	
 	public boolean isGMStamp() {
 		return getLayer() == Zone.Layer.GM;
 	}
 	
 	public boolean isBackgroundStamp() {
 		return getLayer() == Zone.Layer.BACKGROUND;
 	}
 	
 	public boolean isStamp() {
 		switch(getLayer()) {
 		case BACKGROUND:
 		case OBJECT:
 		case GM:
 			return true;
 		}
 		return false;	}
 	
 	public boolean isToken() {
 		return getLayer() == Zone.Layer.TOKEN;
 	}
 	
 	public TokenShape getShape() {
 		try {
 			return tokenShape != null ? TokenShape.valueOf(tokenShape) : TokenShape.SQUARE;  // TODO: make this a psf
 		} catch (IllegalArgumentException iae) {
 			return TokenShape.SQUARE;
 		}
 	}
 	
 	public void setShape(TokenShape type) {
 		this.tokenShape = type.name();
 	}
 	
 	public Type getType() {
 		try {
 			return tokenType != null ? Type.valueOf(tokenType) : Type.NPC;  // TODO: make this a psf
 		} catch (IllegalArgumentException iae) {
 			return Type.NPC;
 		}
 	}
 	
 	public void setType(Type type) {
 		this.tokenType = type.name();
 		
 		if (type == Type.PC) {
 			hasSight = true;
 		}
 	}
 	
 	public Zone.Layer getLayer() {
 		try {
 			if (actualLayer == null) {
 				actualLayer = layer != null ? Zone.Layer.valueOf(layer) : Zone.Layer.TOKEN;
 			}
 			return actualLayer;
 		} catch (IllegalArgumentException iae) {
 			return Zone.Layer.TOKEN;
 		}
 	}
 	
 	public void setLayer(Zone.Layer layer) {
 		this.layer = layer.name();
 		actualLayer = layer;
 	}
 	
 	public boolean hasFacing() {
 		return facing != null;
 	}
 	
 	public void setFacing(Integer facing) {
 		this.facing = facing;
 	}
 	
 	public Integer getFacing() {
 		return facing;
 	}
 
 	public boolean getHasSight() {
 		return hasSight;
 	}
 
 	public void addLightSource(LightSource source, Direction direction) {
 		if (lightSourceList == null) {
 			lightSourceList = new ArrayList<AttachedLightSource>();
 		}
 		lightSourceList.add(new AttachedLightSource(source, direction));
 	}
 	
 	public void removeLightSource(LightSource source) {
 		if (lightSourceList == null) {
 			return;
 		}
 		
 		for (ListIterator<AttachedLightSource> i = lightSourceList.listIterator(); i.hasNext();) {
 			AttachedLightSource als = i.next();
 			if (als.getLightSourceId().equals(source.getId())) {
 				i.remove();
 				break;
 			}
 		}
 	}
 	
 	//My Addition
 	public void clearLightSources() {
 		if (lightSourceList == null) {
 			return;
 		}
 		
 		lightSourceList = null;
 	}
 	//End My Addtion
 	
 	public boolean hasLightSource(LightSource source) {
 		if (lightSourceList == null) {
 			return false;
 		}
 		
 		for (ListIterator<AttachedLightSource> i = lightSourceList.listIterator(); i.hasNext();) {
 			AttachedLightSource als = i.next();
 			if (als.getLightSourceId().equals(source.getId())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean hasLightSources() {
 		return lightSourceList != null && lightSourceList.size() > 0;
 	}
 	
 	public List<AttachedLightSource> getLightSources() {
 		return lightSourceList != null ? Collections.unmodifiableList(lightSourceList) : new LinkedList<AttachedLightSource>();
 	}
 	
 	public synchronized void addOwner(String playerId) {
 		ownerType = OWNER_TYPE_LIST;
 		if (ownerList == null) {
 			ownerList = new HashSet<String>();
 		}
 
 		ownerList.add(playerId);
 	}
 
   public synchronized boolean hasOwners() {
     return ownerType == OWNER_TYPE_ALL || (ownerList != null && !ownerList.isEmpty());
   }
   
 	public synchronized void removeOwner(String playerId) {
 		ownerType = OWNER_TYPE_LIST;
 		if (ownerList == null) {
 			return;
 		}
 
 		ownerList.remove(playerId);
 
 		if (ownerList.size() == 0) {
 			ownerList = null;
 		}
 	}
 
 	public synchronized void setOwnedByAll(boolean ownedByAll) {
 		if (ownedByAll) {
 			ownerType = OWNER_TYPE_ALL;
 			ownerList = null;
 		} else {
 			ownerType = OWNER_TYPE_LIST;
 		}
 	}
 
 	public Set<String> getOwners() {
 		
 		return ownerList != null ? Collections.unmodifiableSet(ownerList) : new HashSet<String>();
 	}
 	
 	public boolean isOwnedByAll() {
 		return ownerType == OWNER_TYPE_ALL;
 	}
 
 	public synchronized void clearAllOwners() {
 		ownerList = null;
 	}
 
 	public synchronized boolean isOwner(String playerId) {
 		return /*getType() == Type.PC && */(ownerType == OWNER_TYPE_ALL
 				|| (ownerList != null && ownerList.contains(playerId)));
 	}
 
 	@Override
 	public int hashCode() {
 		return id.hashCode();
 	}
 	
 	public boolean equals(Object o) {
 		if (!(o instanceof Token)) {
 			return false;
 		}
 
 		return id.equals(((Token) o).id);
 	}
 
 	public void setZOrder(int z) {
 		this.z = z;
 	}
 
 	public int getZOrder() {
 		return z;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 		fireModelChangeEvent(new ModelChangeEvent(this, ChangeEvent.name, name));
 	}
 
 	public MD5Key getImageAssetId() {
 		MD5Key assetId = imageAssetMap.get(currentImageAsset);
 		if (assetId == null) {
 			assetId = imageAssetMap.get(null); // default image
 		}
 		return assetId;
 	}
 
 	public void setImageAsset(String name, MD5Key assetId) {
 		imageAssetMap.put(name, assetId);
 	}
 	
 	public void setImageAsset(String name) {
 		currentImageAsset = name;
 	}
 
 	public Set<MD5Key> getAllImageAssets() {
 		Set<MD5Key> assetSet = new HashSet<MD5Key>(imageAssetMap.values()); 
 		assetSet.add(charsheetImage);
 		assetSet.add(portraitImage);
 		return assetSet;
 	}
 	
 	public MD5Key getPortraitImage() {
 		return portraitImage;
 	}
 	
 	public void setPortraitImage(MD5Key image) {
 		portraitImage = image;
 	}
 	
 	public MD5Key getCharsheetImage() {
 		return charsheetImage;
 	}
 	
 	public void setCharsheetImage(MD5Key charsheetImage) {
 		this.charsheetImage = charsheetImage;
 	}
 	
 	public GUID getId() {
 		return id;
 	}
 
 	public void setId(GUID id) {
 		this.id = id;
 	}
 
 	public int getX() {
 		return x;
 	}
 	
 	public int getY() {
 		return y;
 	}
 
 	public void setX(int x) {
 		lastX = this.x;
 		this.x = x;
 	}
 	
 	public void setY(int y) {
 		lastY = this.y;
 		this.y = y;
 	}
 
 	public void applyMove(int xOffset, int yOffset, Path<AbstractPoint> path) {
 		setX(x + xOffset);
 		setY(y + yOffset);
 		lastPath = path;
 	}
 
 	public void setLastPath(Path<? extends AbstractPoint> path) {
 		lastPath = path;
 	}
 	
 	public int getLastY() {
 		return lastY;
 	}
 	
 	public int getLastX() {
 		return lastX;
 	}
 	
 	public Path<? extends AbstractPoint> getLastPath() {
 		return lastPath;
 	}
 
 	public double getScaleX() {
 		return scaleX;
 	}
 	
 	public double getScaleY() {
 		return scaleY;
 	}
 	
 	public void setScaleX(double scaleX) {
 		this.scaleX = scaleX;
 	}
 
 	public void setScaleY(double scaleY) {
 		this.scaleY = scaleY;
 	}
 
 	/**
 	 * @return Returns the snapScale.
 	 */
 	public boolean isSnapToScale() {
 		return snapToScale;
 	}
 
 	/**
 	 * @param snapScale
 	 *            The snapScale to set.
 	 */
 	public void setSnapToScale(boolean snapScale) {
 		this.snapToScale = snapScale;
 	}
 
 	public void setVisible(boolean visible) {
 		this.isVisible = visible;
 	}
 
 	public boolean isVisible() {
 		return isVisible;
 	}
 
 	public String getName() {
 		return name != null ? name : "";
 	}
 
 	public Rectangle getBounds(Zone zone) {
 		TokenFootprint footprint = getFootprint(zone.getGrid());
         Rectangle footprintBounds = footprint.getBounds(zone.getGrid(), zone.getGrid().convert(new ZonePoint(getX(), getY())));
 
         double width = footprintBounds.width;
         double height = footprintBounds.height;
         
         // Sizing
         if (!isSnapToScale()) {
         	width = this.width * getScaleX();
         	height = this.height * getScaleY();
         } else {
         	width = footprintBounds.width * footprint.getScale() * sizeScale;
         	height = footprintBounds.height * footprint.getScale() * sizeScale;
         }
 
         // Positioning
         if (!isSnapToGrid()) {
             footprintBounds.x = getX();
             footprintBounds.y = getY();
         } else {
         	if (!isBackgroundStamp()) {
 	        	// Center it on the footprint
 	        	footprintBounds.x -= (width - footprintBounds.width)/2;
 	        	footprintBounds.y -= (height - footprintBounds.height)/2;
         	} else {
 //	        	footprintBounds.x -= zone.getGrid().getSize()/2;
 //	        	footprintBounds.y -= zone.getGrid().getSize()/2;
         	}
         }
         
         footprintBounds.width = (int)width; // perhaps make this a double
         footprintBounds.height = (int)height;
         
         // Offset
         footprintBounds.x += anchorX;
         footprintBounds.y += anchorY;
 
 		return footprintBounds;
 	}
 	
 	public String getSightType() {
 		return sightType;
 	}
 
 	public void setSightType(String sightType) {
 		this.sightType = sightType;
 	}
 
 	/**
 	 * @return Returns the size.
 	 */
 	public TokenFootprint getFootprint(Grid grid) {
 		return grid.getFootprint(getSizeMap().get(grid.getClass()));
 	}
 	
 	public TokenFootprint setFootprint(Grid grid, TokenFootprint footprint) {
 		return grid.getFootprint(getSizeMap().put(grid.getClass(), footprint.getId()));
 	}
 
 	private Map<Class<? extends Grid>, GUID> getSizeMap() {
 		if (sizeMap == null) {
 			sizeMap = new HashMap<Class<? extends Grid>, GUID>();
 		}
 		return sizeMap;
 	}
 	
 	public boolean isSnapToGrid() {
 		return snapToGrid;
 	}
 
 	public void setSnapToGrid(boolean snapToGrid) {
 		this.snapToGrid = snapToGrid;
 	}
 
 	/**
 	 * Get a particular state property for this Token.
 	 * 
 	 * @param property
 	 *            The name of the property being read.
 	 * @return Returns the current value of property.
 	 */
 	public Object getState(String property) {
 		return state.get(property);
 	}
 
 	/**
 	 * Set the value of state for this Token.
 	 * 
 	 * @param aState
 	 *            The property to set.
 	 * @param aValue
 	 *            The new value for the property.
 	 * @return The original vaoue of the property, if any.
 	 */
 	public Object setState(String aState, Object aValue) {
 		if (aValue == null)
 			return state.remove(aState);
 		return state.put(aState, aValue);
 	}
 	
 	public void setProperty(String key, Object value) {
 //		if (key != null) {
 //			key = key.toUpperCase();
 //		}
 		getPropertyMap().put(key, value);
 	}
 	
 	public Object getProperty(String key) {
 //		if (key != null) {
 //			key = key.toUpperCase();
 //		}
 		Object value = getPropertyMap().get(key);
 		
 //		// Short name ?
 //		if (value == null) {
 //			for (TokenProperty property : MapTool.getCampaign().getCampaignProperties().getTokenPropertyList(getPropertyType())) {
 //				if (property.getShortName().equals(key)) {
 //					value = getPropertyMap().get(property.getShortName().toUpperCase());
 //				}
 //			}
 //		}
 		return value;
 	}
 
 	public Object getEvaluatedProperty(String key) {
 		Object val = getProperty(key);
 		if (val == null) {
 			// Global default ?
 			List<TokenProperty> propertyList = MapTool.getCampaign().getCampaignProperties().getTokenPropertyList(propertyType); 
 			if (propertyList != null) {
 				for (TokenProperty property : propertyList) {
 					if (key.equalsIgnoreCase(property.getName()) || key.equalsIgnoreCase(property.getShortName())) {
 						val = property.getDefaultValue();
 						break;
 					}
 				}
 			}
 		}
 		if (val == null) {
 			return "";
 		}
 		
 		// First we try convert it to a JSON object.
 		if (val.toString().trim().startsWith("[") || val.toString().trim().startsWith("{")) {
 			Object obj  = JSONMacroFunctions.convertToJSON(val.toString());
 			if (obj != null) {
 				return obj;
 			}
 		}
 		
 		try {
 			val = MapTool.getParser().parseLine(this, val.toString());
 		} catch (ParserException pe) {
 			//pe.printStackTrace();
 			val = val.toString();
 		}
 		if (val == null) {
 			val = "";
 		}
 		return val;
 	}
 	
 	public Set<String> getPropertyNames() {
 		return getPropertyMap().keySet();
 	}
 	
 	private Map<String, Object> getPropertyMap() {
 		if (propertyMap == null) {
 			propertyMap = new HashMap<String, Object>();
 		}
 		return propertyMap;
 	}
 
 	private void loadOldMacros() {
 		if (macroMap == null) {
 			return;
 		}
 		MacroButtonProperties prop;
 		Set<String> oldMacros = macroMap.keySet();
 		for (String macro : oldMacros) {
 			prop=new MacroButtonProperties(getMacroNextIndex());
 			prop.setLabel(macro);
 			prop.setCommand(macroMap.get(macro));
 			prop.setApplyToTokens(true);
 			macroPropertiesMap.put(prop.getIndex(),prop);
 		}
 		macroMap = null;
 //		System.out.println("Token.loadOldMacros() set up "+macroPropertiesMap.size()+ " new macros.");
 	}
 	
 	public int getMacroNextIndex(){
 		if (macroPropertiesMap == null){
 			macroPropertiesMap = new HashMap<Integer, Object>();
 		}
 		Set<Integer> indexSet = macroPropertiesMap.keySet();
 		int maxIndex = 0;
 		for (int index : indexSet){
 			if (index>maxIndex)
 				maxIndex = index;
 		}
 		return maxIndex+1;
 	}
 	
 	public Map<Integer,Object> getMacroPropertiesMap(boolean secure){
 		if (macroPropertiesMap == null){
 			macroPropertiesMap = new HashMap<Integer, Object>();
 		}
 		if (macroMap != null) {
 			loadOldMacros();
 		}
 		if (secure && !AppUtil.playerOwns(this)){
 			return new HashMap<Integer, Object>();
 		} else {
 			return macroPropertiesMap;
 		}
 	}
 
 	public MacroButtonProperties getMacro(int index, boolean secure){
 		return (MacroButtonProperties)getMacroPropertiesMap(secure).get(index);
 	}
 	
 	// avoid this; it loads the first macro with this label, but there could be more than one macro with that label
 	public MacroButtonProperties getMacro(String label, boolean secure) {
 		Set<Integer> keys = getMacroPropertiesMap(secure).keySet();
 		for (int key : keys) {
 			MacroButtonProperties prop = (MacroButtonProperties)macroPropertiesMap.get(key);
 			if (prop.getLabel().equals(label)){
 				return prop;
 			}
 		}
 		return null;
 	}
 	
 	public List<MacroButtonProperties> getMacroList(boolean secure){
 		Set<Integer> keys = getMacroPropertiesMap(secure).keySet();
 		List<MacroButtonProperties> list = new ArrayList<MacroButtonProperties>();
 		for (int key : keys) {
 			list.add((MacroButtonProperties)macroPropertiesMap.get(key));
 		}
 		return list;
 	}
 	
 	public void replaceMacroList(List<MacroButtonProperties> newMacroList){
 		// used by the token edit dialog, which will handle resetting panels and putting token to zone
 		macroPropertiesMap.clear();
 		for (MacroButtonProperties macro : newMacroList){
 			if (macro.getLabel() == null || macro.getLabel().trim().length() == 0 || macro.getCommand().trim().length() == 0) {
 				continue;
 			}
 			macroPropertiesMap.put(macro.getIndex(), macro);
 		}
 	}
 	
 	public List<String> getMacroNames(boolean secure) {
 		Set<Integer> keys = getMacroPropertiesMap(secure).keySet();
 		List<String> list = new ArrayList<String>();
 		for (int key : keys) {
 			MacroButtonProperties prop = (MacroButtonProperties)macroPropertiesMap.get(key);
 			list.add(prop.getLabel());
 		}
 		return list;
 	}
 
 	public boolean hasMacros(boolean secure) {
 		if ( getMacroPropertiesMap(secure).size() > 0){
 			return true;
 		}
 		return false;
 	}
 	
 	public void saveMacroButtonProperty(MacroButtonProperties prop){
 		getMacroPropertiesMap(false).put(prop.getIndex(),prop);
 		MapTool.getFrame().resetTokenPanels();
 		MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), this);
 	}
 
 	public void deleteMacroButtonProperty(MacroButtonProperties prop){
 		getMacroPropertiesMap(false).remove(prop.getIndex());
 		MapTool.getFrame().resetTokenPanels();
 		MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), this);
 	}
 
 	public void setSpeechMap(Map<String, String> map) {
 		getSpeechMap().clear();
 		getSpeechMap().putAll(map);
 	}
 	
 	public Set<String> getSpeechNames() {
 		return getSpeechMap().keySet();
 	}
 	
 	public String getSpeech(String key) {
 		return getSpeechMap().get(key);
 	}
 	
 	public void setSpeech(String key, String value) {
 		getSpeechMap().put(key, value);
 	}
 	
 	private Map<String, String> getSpeechMap() {
 		if (speechMap == null) {
 			speechMap = new HashMap<String, String>();
 		}
 		return speechMap;
 	}
 	
 	/**
 	 * Get a set containing the names of all set properties on this token.
 	 * 
 	 * @return The set of state property names that have a value associated with
 	 *         them.
 	 */
 	public Set<String> getStatePropertyNames() {
 		return state.keySet();
 	}
 
   /** @return Getter for notes */
     public String getNotes() {
         return notes;
     }
 
     /**
      * @param aNotes
      *            Setter for notes
      */
     public void setNotes(String aNotes) {
         notes = aNotes;
     }
     
     public boolean isFlippedY() {
 		return isFlippedY;
 	}
 
 	public void setFlippedY(boolean isFlippedY) {
 		this.isFlippedY = isFlippedY;
 	}
 
 	public boolean isFlippedX() {
 		return isFlippedX;
 	}
 
 	public void setFlippedX(boolean isFlippedX) {
 		this.isFlippedX = isFlippedX;
 	}
     
     public Color getVisionOverlayColor() {
 		if (visionOverlayColor == null && visionOverlayColorValue != null) {
 			visionOverlayColor = new Color(visionOverlayColorValue);
 		}
 		
         return visionOverlayColor;
     }
 
     public void setVisionOverlayColor(Color color) {
 		if (color != null) {
 			visionOverlayColorValue = color.getRGB();
 		} else {
 			visionOverlayColorValue = null;
 		}
 		visionOverlayColor = color;
     }    
     
     @Override
     public String toString() {
     	return "Token: " + id;
     }
     
     public void setAnchor(int x, int y) {
     	anchorX = x;
     	anchorY = y;
     }
 
     public Point getAnchor() {
     	return new Point(anchorX, anchorY);
     }
     
     public double getSizeScale() {
     	return sizeScale;
     }
     
     public void setSizeScale(double scale) {
     	sizeScale = scale;
     }
     
 	/**
      * Convert the token into a hash map. This is used to ship all of the
      * properties for the token to other apps that do need access to the
      * <code>Token</code> class.
      * 
      * @return A map containing the properties of the token.
      */
     public TokenTransferData toTransferData() {
         TokenTransferData td = new TokenTransferData();
         td.setName(name);
         td.setPlayers(ownerList);
         td.setVisible(isVisible);
         td.setLocation(new Point(x, y));
         td.setFacing(facing);
 
         // Set the properties
         td.put(TokenTransferData.ID, id.toString());
         td.put(TokenTransferData.ASSET_ID, imageAssetMap.get(null));
         td.put(TokenTransferData.Z, z);
         td.put(TokenTransferData.SNAP_TO_SCALE, snapToScale);
         td.put(TokenTransferData.WIDTH, scaleX);
         td.put(TokenTransferData.HEIGHT, scaleY);
 //        td.put(TokenTransferData.SIZE, size);
         td.put(TokenTransferData.SNAP_TO_GRID, snapToGrid);
         td.put(TokenTransferData.OWNER_TYPE, ownerType);
         td.put(TokenTransferData.TOKEN_TYPE, tokenShape);
         td.put(TokenTransferData.NOTES, notes);
         td.put(TokenTransferData.GM_NOTES, gmNotes);
         td.put(TokenTransferData.GM_NAME, gmName);
 
         // Put all of the serializable state into the map
         for (String key : getStatePropertyNames()) {
             Object value = getState(key);
             if (value instanceof Serializable)
                 td.put(key, value);
         } 
         td.putAll(state);
 
         // Create the image from the asset and add it to the map
         Asset asset = AssetManager.getAsset(imageAssetMap.get(null));
         Image image = ImageManager.getImageAndWait(asset);
         if (image != null)
             td.setToken(new ImageIcon(image)); // Image icon makes it serializable.
         return td;
     }
 
     /**
      * Constructor to create a new token from a transfer object containing its property
      * values. This is used to read in a new token from other apps that don't
      * have access to the <code>Token</code> class.
      * 
      * @param td
      *            Read the values from this transfer object.
      */
     public Token(TokenTransferData td) {
         if (td.getLocation() != null) {
             x = td.getLocation().x;
             y = td.getLocation().y;
         } 
         snapToScale = getBoolean(td, TokenTransferData.SNAP_TO_SCALE, true);
         scaleX = getInt(td, TokenTransferData.WIDTH, 1);
         scaleY = getInt(td, TokenTransferData.HEIGHT, 1);
 //        size = getInt(td, TokenTransferData.SIZE, TokenSize.Size.Medium.value());
         snapToGrid = getBoolean(td, TokenTransferData.SNAP_TO_GRID, true);
         isVisible = td.isVisible();
         name = td.getName();
         ownerList = td.getPlayers();
         ownerType = getInt(td, TokenTransferData.OWNER_TYPE,
                 ownerList == null ? OWNER_TYPE_ALL : OWNER_TYPE_LIST);
         tokenShape = (String) td.get(TokenTransferData.TOKEN_TYPE);
         facing = td.getFacing();
         notes = (String) td.get(TokenTransferData.NOTES);
         gmNotes = (String) td.get(TokenTransferData.GM_NOTES);
         gmName = (String) td.get(TokenTransferData.GM_NAME);
 
         // Get the image for the token
         ImageIcon icon = td.getToken();
         if (icon != null) {
 
             // Make sure there is a buffered image for it
             Image image = icon.getImage();
             if (!(image instanceof BufferedImage)) {
                 image = new BufferedImage(icon.getIconWidth(), icon
                         .getIconHeight(), Transparency.TRANSLUCENT);
                 Graphics2D g = ((BufferedImage) image).createGraphics();
                 icon.paintIcon(null, g, 0, 0);
             } 
 
             // Create the asset
             try {
                 Asset asset = new Asset(name, ImageUtil
                         .imageToBytes((BufferedImage) image));
                 if (!AssetManager.hasAsset(asset))
                     AssetManager.putAsset(asset);
                 imageAssetMap.put(null, asset.getId());
             } catch (IOException e) {
                 e.printStackTrace();
             } 
         } 
 
         // Get all of the non maptool state
         state = new HashMap<String, Object>();
         for (String key : td.keySet()) {
             if (key.startsWith(TokenTransferData.MAPTOOL))
                 continue;
             setState(key, td.get(key));
         } // endfor
     }
 
     /**
      * Get an integer value from the map or return the default value
      * 
      * @param map
      *            Get the value from this map
      * @param propName
      *            The name of the property being read.
      * @param defaultValue
      *            The value for the property if it is not set in the map.
      * @return The value for the passed property
      */
     private static int getInt(Map<String, Object> map, String propName,
             int defaultValue) {
         Integer integer = (Integer) map.get(propName);
         if (integer == null)
             return defaultValue;
         return integer.intValue();
     }
 
     /**
      * Get a boolean value from the map or return the default value
      * 
      * @param map
      *            Get the value from this map
      * @param propName
      *            The name of the property being read.
      * @param defaultValue
      *            The value for the property if it is not set in the map.
      * @return The value for the passed property
      */
     private static boolean getBoolean(Map<String, Object> map, String propName,
             boolean defaultValue) {
         Boolean bool = (Boolean) map.get(propName);
         if (bool == null)
             return defaultValue;
         return bool.booleanValue();
     }
     
     public static boolean isTokenFile(String filename) {
     	return filename != null && filename.toLowerCase().endsWith(FILE_EXTENSION);
     }
 	
 	public Icon getIcon(int width, int height) {
 		ImageIcon icon = new ImageIcon(ImageManager.getImageAndWait(AssetManager.getAsset(getImageAssetId())));
 		Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT);
 		return new ImageIcon(image);
 	}
 	
 	public boolean isBeingImpersonated() {
 		return beingImpersonated;
 	}
 	
 	public void setBeingImpersonated(boolean bool) {
 		beingImpersonated = bool;
 	}
 	
 	public void deleteMacroGroup(String macroGroup, Boolean secure) {
 		List<MacroButtonProperties> tempMacros = new ArrayList<MacroButtonProperties>(getMacroList(true));
 		for(MacroButtonProperties nextProp : tempMacros) {
 			if(macroGroup.equals(nextProp.getGroup())) {
 				getMacroPropertiesMap(secure).remove(nextProp.getIndex());
 			}
 		}
 		MapTool.getFrame().resetTokenPanels();
 		MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), this);
 	}
 	
 	public void deleteAllMacros(Boolean secure) {
 		List<MacroButtonProperties> tempMacros = new ArrayList<MacroButtonProperties>(getMacroList(true));
 		for(MacroButtonProperties nextProp : tempMacros) {
 			getMacroPropertiesMap(secure).remove(nextProp.getIndex());
 		}
 		MapTool.getFrame().resetTokenPanels();
 		MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), this);
 	}
 	
 	public static final Comparator<Token> COMPARE_BY_NAME = new Comparator<Token>() {
 		public int compare(Token o1, Token o2) {
 			if (o1 == null || o2 == null) {
 				return 0;
 			}
 			
 			return o1.getName().compareTo(o2.getName());
 		}
 	};
 	public static final Comparator<Token> COMPARE_BY_ZORDER = new Comparator<Token>() {
 		public int compare(Token o1, Token o2) {
 			if (o1 == null || o2 == null) {
 				return 0;
 			}
 			
 			return o1.z < o2.z ? -1 : o1.z == o2.z ? 0 : 1;
 		}
 	};
 }
