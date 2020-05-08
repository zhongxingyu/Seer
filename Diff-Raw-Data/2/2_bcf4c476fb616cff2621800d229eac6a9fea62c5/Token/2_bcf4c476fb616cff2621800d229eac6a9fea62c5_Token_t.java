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
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import net.rptools.lib.MD5Key;
 
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
 		SQUARE
 	}
 	
 	private MD5Key assetID;
 
 	private int x;
 	private int y;
 	private int z;
 
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
 
 		if (token.ownerList != null) {
 			ownerList = new HashSet<String>();
 			ownerList.addAll(token.ownerList);
 		}
 
 		if (token.state != null) {
 			state = new HashMap<String, Object>();
 			for (Map.Entry<String, Object> entry : token.state.entrySet()) {
 				state.put(entry.getKey(), entry.getValue());
 			}
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
 		this.x = x;
 	}
 
 	public int getY() {
 		return y;
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
 }
