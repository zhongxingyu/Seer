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
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Transparency;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.swing.ImageIcon;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.transferable.TokenTransferData;
 import net.rptools.maptool.util.ImageManager;
 
 /**
  * This object represents the placeable objects on a map. For example an icon
  * that represents a character would exist as an {@link Asset} (the image
  * itself) and a location and scale.
  */
 public class Token {
 	private GUID id = new GUID();
 
 	public enum Type {
 		TOP_DOWN,
 		CIRCLE,
 		SQUARE,
 		STAMP,
 		BACKGROUND
 	}
 	
 	public static final Comparator<Token> NAME_COMPARATOR = new Comparator<Token>() {
 		public int compare(Token o1, Token o2) {
 			return o1.getName().compareToIgnoreCase(o2.getName());
 		}
 	};
   
 	private MD5Key assetID;
 
 	private int x;
 	private int y;
 	private int z;
 
 	private int lastX;
 	private int lastY;
 	private Path lastPath;
 	
 	private boolean snapToScale = true; // Whether the scaleX and scaleY
 										// represent snap-to-grid measurements
 
 	private int width = 1; // Default to using exactly 1x1 grid cell
 	private int height = 1;
 	private int size = TokenSize.Size.Medium.value(); // Abstract size
 
 	private boolean snapToGrid = true; // Whether the token snaps to the
 										// current grid or is free floating
 
 	private boolean isVisible = true;
 
 	private String name;
 	private Set<String> ownerList;
 
 	private int ownerType;
 
 	private static final int OWNER_TYPE_ALL = 1;
 	private static final int OWNER_TYPE_LIST = 0;
 	
 	private String tokenType; // TODO: Make tokens understand enums for hessian
 
 	private Integer facing = null;
 	
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
 
 	// Transient so that it isn't transfered over the wire
 	private transient List<ModelChangeListener> listenerList = new CopyOnWriteArrayList<ModelChangeListener>();
 
 	public enum ChangeEvent {
 		name
 	}
 
 	public Token(Token token) {
 		id = new GUID();
 		assetID = token.assetID;
 		x = token.x;
 		y = token.y;
 
 		snapToScale = token.snapToScale;
 		width = token.width;
 		height = token.height;
 		size = token.size;
 		facing = token.facing;
 		tokenType = token.tokenType;
 
 		snapToGrid = token.snapToGrid;
 		isVisible = token.isVisible;
 		name = token.name;
 	    notes = token.notes;
 	    gmName = token.gmName;
 	    gmNotes = token.gmNotes;
 
 		if (token.ownerList != null) {
 			ownerList = new HashSet<String>();
 			ownerList.addAll(token.ownerList);
 		}
 
 		if (token.state != null) {
 			state = new HashMap<String, Object>(token.state);
 		}
     
 	}
 
 	public Token() {
 
 	}
 
 	public Token(MD5Key assetID) {
 		this("", assetID);
 	}
 
 	public Token(String name, MD5Key assetID) {
 		this.name = name;
 		this.assetID = assetID;
 		state = new HashMap<String, Object>();
 	}
 	
 	public String getGMNotes() {
 		return gmNotes;
 	}
 	
 	public void setGMNote(String notes) {
 		gmNotes = notes;
 	}
 	
 	public String getGMName() {
 		return gmName;
 	}
 	
 	public void setGMName(String name) {
 		gmName = name;
 	}
 	
 	public boolean isStamp() {
 		return getTokenType() == Type.STAMP;
 	}
 	
 	public boolean isBackground() {
 		return getTokenType() == Type.BACKGROUND;
 	}
 	
 	public Type getTokenType() {
 		return tokenType != null ? Type.valueOf(tokenType) : Token.Type.SQUARE;  // TODO: make this a psf
 	}
 	
 	public void setTokenType(Type type) {
 		this.tokenType = type.name();
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
 
 	public synchronized void setAllOwners() {
 		ownerType = OWNER_TYPE_ALL;
 		ownerList = null;
 	}
 
 	public boolean isOwnedByAll() {
 		return ownerType == OWNER_TYPE_ALL;
 	}
 
 	public synchronized void clearAllOwners() {
 		ownerList = null;
 		ownerType = OWNER_TYPE_LIST;
 	}
 
 	public synchronized boolean isOwner(String playerId) {
 		return ownerType == OWNER_TYPE_ALL
 				|| (ownerList != null && ownerList.contains(playerId));
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
 	}
 
 	public MD5Key getAssetID() {
 		return assetID;
 	}
 
 	public void setAsset(MD5Key assetID) {
 		this.assetID = assetID;
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
 
 	public void setX(int x) {
 		lastX = this.x;
 		this.x = x;
 	}
 
 	public int getY() {
 		lastY = this.y;
 		return y;
 	}
 
 	public void applyMove(int xOffset, int yOffset, Path path) {
 		setX(x + xOffset);
 		setY(y + yOffset);
 		lastPath = path;
 	}
 
 	public void setLastPath(Path path) {
 		lastPath = path;
 	}
 	
 	public int getLastY() {
 		return lastY;
 	}
 	
 	public int getLastX() {
 		return lastX;
 	}
 	
 	public Path getLastPath() {
 		return lastPath;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	/**
 	 * @return Returns the scaleX.
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * @param scaleX
 	 *            The scaleX to set.
 	 */
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	/**
 	 * @return Returns the sizeY.
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 	/**
 	 * @param height
 	 *            The sizeY to set.
 	 */
 	public void setHeight(int height) {
 		this.height = height;
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
 
 	/**
 	 * @return Returns the size.
 	 */
 	public int getSize() {
 		return size;
 	}
 
 	/**
 	 * @param size
 	 *            The size to set.
 	 */
 	public void setSize(int size) {
 		this.size = size;
 	}
 
 	public boolean isSnapToGrid() {
 		return snapToGrid;
 	}
 
 	public void setSnapToGrid(boolean snapToGrid) {
 		this.snapToGrid = snapToGrid;
 	}
 
 	public void addModelChangeListener(ModelChangeListener listener) {
 		listenerList.add(listener);
 	}
 
 	public void removeModelChangeListener(ModelChangeListener listener) {
 		listenerList.remove(listener);
 	}
 
 	protected void fireModelChangeEvent(ModelChangeEvent event) {
 
 		for (ModelChangeListener listener : listenerList) {
 			listener.modelChanged(event);
 		}
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
         td.put(TokenTransferData.ASSET_ID, assetID);
         td.put(TokenTransferData.Z, z);
         td.put(TokenTransferData.SNAP_TO_SCALE, snapToScale);
         td.put(TokenTransferData.WIDTH, width);
         td.put(TokenTransferData.HEIGHT, height);
         td.put(TokenTransferData.SIZE, size);
         td.put(TokenTransferData.SNAP_TO_GRID, snapToGrid);
         td.put(TokenTransferData.OWNER_TYPE, ownerType);
         td.put(TokenTransferData.TOKEN_TYPE, tokenType);
         td.put(TokenTransferData.NOTES, notes);
         td.put(TokenTransferData.GM_NOTES, gmNotes);
         td.put(TokenTransferData.GM_NAME, gmName);
 
         // Put all of the serializable state into the map
         for (String key : getStatePropertyNames()) {
             Object value = getState(key);
             if (value instanceof Serializable)
                 td.put(key, value);
         } // endfor
         td.putAll(state);
 
         // Create the image from the asset and add it to the map
         Asset asset = AssetManager.getAsset(assetID);
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
         } // endif
         snapToScale = getBoolean(td, TokenTransferData.SNAP_TO_SCALE, true);
         width = getInt(td, TokenTransferData.WIDTH, 1);
         height = getInt(td, TokenTransferData.HEIGHT, 1);
         size = getInt(td, TokenTransferData.SIZE, TokenSize.Size.Medium.value());
         snapToGrid = getBoolean(td, TokenTransferData.SNAP_TO_GRID, true);
         isVisible = td.isVisible();
         name = td.getName();
         ownerList = td.getPlayers();
         ownerType = getInt(td, TokenTransferData.OWNER_TYPE,
                 ownerList == null ? OWNER_TYPE_ALL : OWNER_TYPE_LIST);
         tokenType = (String) td.get(TokenTransferData.TOKEN_TYPE);
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
             } // endif
 
             // Create the asset
             try {
                 Asset asset = new Asset(name, ImageUtil
                         .imageToBytes((BufferedImage) image));
                 if (!AssetManager.hasAsset(asset))
                     AssetManager.putAsset(asset);
                 assetID = asset.getId();
             } catch (IOException e) {
                 e.printStackTrace();
             } // endtry
         } // endtry
 
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
 }
