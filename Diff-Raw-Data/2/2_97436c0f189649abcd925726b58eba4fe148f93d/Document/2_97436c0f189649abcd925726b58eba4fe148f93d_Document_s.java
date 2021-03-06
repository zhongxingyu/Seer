 /*
  * Scriptographer
  *
  * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
  *
  * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
  * All rights reserved.
  *
  * Please visit http://scriptographer.com/ for updates and contact.
  *
  * -- GPL LICENSE NOTICE --
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  * -- GPL LICENSE NOTICE --
  *
  * File created on 23.01.2005.
  *
  * $Id$
  */
 
 package com.scriptographer.ai;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.scratchdisk.util.ConversionUtils;
 import com.scratchdisk.util.SoftIntMap;
 import com.scriptographer.CommitManager;
 import com.scriptographer.ScriptographerException;
 import com.scriptographer.script.EnumUtils;
 
 /**
  * @author lehni
  */
 public class Document extends NativeObject {
 
 	private LayerList layers = null;
 	private DocumentViewList views = null;
 	private SymbolList symbols = null;
 	private SwatchList swatches = null;
 	private Dictionary data = null;
 
 	/**
 	 * Opens an existing document.
 	 * 
 	 * Sample code:
 	 * <code>
 	 * var file = new File('/path/to/poster.ai');
 	 * var poster = new Document(file);
 	 * </code>
 	 * 
 	 * @param file the file to read from
 	 * @param colorModel the document's desired color model {@default 'cmyk'}
 	 * @param dialogStatus how dialogs should be handled {@default 'none'}
 	 */
 	public Document(File file, ColorModel colorModel, DialogStatus dialogStatus) {
 		super(nativeCreate(file,
 				(colorModel != null ? colorModel : ColorModel.CMYK).value,
 				(dialogStatus != null ? dialogStatus : DialogStatus.NONE).value));
 	}
 
 	public Document(File file, ColorModel colorModel) {
 		this(file, colorModel, null);
 	}
 
 	public Document(File file) {
 		this(file, null, null);
 	}
 
 	/**
 	 * Creates a new document.
 	 * 
 	 * Sample code:
 	 * <code>
 	 * // Create a new document named 'poster'
 	 * // with a width of 100pt and a height of 200pt:
 	 * var doc = new Document('poster', 100, 200);;
 	 * 
 	 * // Create a document with a CMYK color mode
 	 * // and show Illustrator's 'New Document' dialog:
 	 * var doc = new Document('poster', 100, 200, 'cmyk', 'on');
 	 * </code>
 	 * 
 	 * @param title the title of the document
 	 * @param width the width of the document
 	 * @param height the height of the document
 	 * @param colorModel the document's desired color model {@default 'cmyk'}
 	 * @param dialogStatus how dialogs should be handled {@default 'none'}
 	 */
 	public Document(String title, float width, float height, ColorModel colorModel,
 			DialogStatus dialogStatus) {
 		super(nativeCreate(title, width, height, 
 				(colorModel != null ? colorModel : ColorModel.CMYK).value,
 				(dialogStatus != null ? dialogStatus : DialogStatus.NONE).value));
 	}
 
 	public Document(String title, float width, float height, ColorModel colorModel) {
 		this(title, width, height, colorModel, null);
 	}
 
 	public Document(String title, float width, float height) {
 		this(title, width, height, null, null);
 	}
 
 	protected Document(int handle) {
 		super(handle);
 	}
 
 	private static native int nativeCreate(File file, int colorModel,
 			int dialogStatus);
 
 	private static native int nativeCreate(String title, float width,
 			float height, int colorModel, int dialogStatus);
 	
 	// use a SoftIntMap to keep track of already wrapped documents:
 	private static SoftIntMap<Document> documents = new SoftIntMap<Document>();
 	
 	protected static Document wrapHandle(int handle) {
 		if (handle == 0)
 			return null;
 		Document doc = (Document) documents.get(handle);
 		if (doc == null) {
 			doc = new Document(handle);
 			documents.put(handle, doc);
 		}
 		return doc;
 	}
 	
 	private static native int nativeGetActiveDocumentHandle();
 	
 	private static native int nativeGetWorkingDocumentHandle();
 
 	/**
 	 * @jshide
 	 */
 	public static Document getActiveDocument() {
 		return Document.wrapHandle(nativeGetActiveDocumentHandle());
 	}
 
 	/**
 	 * @jshide
 	 */
 	public static Document getWorkingDocument() {
 		return Document.wrapHandle(nativeGetWorkingDocumentHandle());
 	}
 
 	/**
 	 * Called before ai functions are executed
 	 * 
 	 * @jshide
 	 */
 	public static native void beginExecution();
 	
 	/**
 	 * Called after ai functions are executed
 	 * 
 	 * @jshide
 	 */
 	public static native void endExecution();
 
 	/**
 	 * Activates this document, so all newly created items will be placed
 	 * in it.
 	 * 
 	 * @param focus When set to true, the document window is brought to the
 	 *        front, otherwise the window sequence remains the same.
 	 * @param forCreation if set to true, the internal pointer gActiveDoc will
 	 *        not be modified, but gCreationDoc will be set, which then is only
 	 *        used once in the next call to Document_activate() (native stuff).
 	 */
 	private native void activate(boolean focus, boolean forCreation);
 
 	/**
 	 * Activates this document, so all newly created items will be placed
 	 * in it.
 	 * 
 	 * @param focus When set to {@code true}, the document window is
 	 *        brought to the front, otherwise the window sequence remains the
 	 *        same. Default is {@code true}.
 	 */
 	public void activate(boolean focus) {
 		activate(focus, false);
 	}
 	
 	/**
 	 * Activates this document and brings its window to the front
 	 */
 	public void activate() {
 		activate(true, false);
 	}
 	
 	/**
 	 * The layers contained within the document.
 	 * 
 	 * Sample code:
 	 * <code>
 	 *  // When you create a new Document it always contains
 	 *  // a layer called 'Layer 1'
 	 *  print(document.layers); // Layer (Layer 1)
 	 *
 	 *  // Create a new layer called 'test' in the document
 	 *  var newLayer = new Layer();
 	 *  newLayer.name = 'test';
 	 *
 	 *  print(document.layers); // Layer (test), Layer (Layer 1)
 	 *  print(document.layers[0]); // Layer (test)
 	 *  print(document.layers.test); // Layer (test)
 	 *  print(document.layers['Layer 1']); // Layer (Layer 1)
 	 * </code>
 	 */
 	public LayerList getLayers() {
 		if (layers == null)
 			layers = new LayerList(this);
 		return layers;
 	}
 
 	/**
 	 * The layer which is currently active. The active layer is indicated in the
 	 * Layers palette by a black triangle. New items will be created on this
 	 * layer by default.
 	 * @return The layer which is currently active
 	 */
 	public native Layer getActiveLayer();
 	
 	/**
 	 * The views contained within the document.
 	 */
 	public DocumentViewList getViews() {
 		if (views == null)
 			views = new DocumentViewList(this);
 		return views;
 	}
 	
 	// getActiveView can not be native as there is no wrapViewHandle defined
 	// nativeGetActiveView returns the handle, that still needs to be wrapped
 	// here. as this is only used once, that's the prefered way (just like
 	// DocumentList.getActiveDocument
 	
 	private native int getActiveViewHandle(); 
 
 	/**
 	 * The document view which is currently active.
 	 */
 	public DocumentView getActiveView() {
 		return DocumentView.wrapHandle(getActiveViewHandle(), this);
 	}
 	
 	/**
 	 * The symbols contained within the document.
 	 */
 	public SymbolList getSymbols() {
 		if (symbols == null)
 			symbols = new SymbolList(this);
 		return symbols;
 	}
 	
 	private native int getActiveSymbolHandle(); 
 
 	/**
 	 * The symbol which is selected in the Symbols menu.
 	 */
 	public Symbol getActiveSymbol() {
 		return (Symbol) Symbol.wrapHandle(getActiveSymbolHandle(), this);
 	}
 
 	/**
 	 * The swatches contained within the document.
 	 */
 	public SwatchList getSwatches() {
 		if (swatches == null)
 			swatches = new SwatchList(this);
 		return swatches;
 	}
 
 	// TODO: getActiveSwatch, getActiveGradient
 	
 	/**
 	 * The point of the lower left corner of the imageable page, relative to the
 	 * ruler origin.
 	 */
 	public native Point getPageOrigin();
 	
 	public native void setPageOrigin(Point pt);
 
 	/**
 	 * The point of the ruler origin of the document, relative to the bottom
 	 * left of the artboard.
 	 */
 	public native Point getRulerOrigin();
 	
 	public native void setRulerOrigin(Point pt);
 
 	/**
 	 * The size of the document.
 	 * Setting size only works while reading a document!
 	 */
 	public native Size getSize();
 
 	/**
 	 * @jshide
 	 */
 	public native void setSize(double width, double height);
 	
 	public void setSize(Size size) {
 		setSize(size.width, size.height);
 	}
 
 	/**
 	 * The document's cropbox.
 	 */
 	public native Rectangle getCropBox();
 	
 	public native void setCropBox(Rectangle cropBox);
 
 	/**
 	 * Specifies if the document has been edited since it was last saved. When
 	 * set to {@code true}, closing the document will present the user
 	 * with a dialog box asking to save the file.
 	 */
 	public native boolean isModified();
 	
 	public native void setModified(boolean modified);
 
 	/**
 	 * The document's file.
 	 */
 	public native File getFile();
 
 	private native int nativeGetFileFormat();
 
 	private native void nativeSetFileFormat(int handle);
 	
 	public FileFormat getFileFormat() {
 		return FileFormat.getFormat(nativeGetFileFormat());
 	}
 
 	public void setFileFormat(FileFormat format) {
 		nativeSetFileFormat(format != null ? format.handle : 0);
 	}
 
 	private native void nativePrint(int status);
 
 	/**
 	 * Prints the document.
 	 * 
 	 * @param status
 	 */
 	public void print(DialogStatus status) {
 		nativePrint(status.value);
 	}
 
 	public void print() {
 		print(DialogStatus.OFF);
 	}
 
 	/**
 	 * Saves the document.
 	 */
 	public native void save();
 	
 	/**
 	 * Closes the document.
 	 */
 	public native void close();
 	
 	/**
 	 * Forces the document to be redrawn.
 	 */
 	public native void redraw();
 	
 	/**
 	 * Copies the selected items to the clipboard.
 	 */
 	public native void copy();
 	
 	/**
 	 * Cuts the selected items to the clipboard.
 	 */
 	public native void cut();
 	
 	/**
 	 * Pastes the contents of the clipboard into the active layer of the
 	 * document.
 	 */
 	public native void paste();
 
 	/**
 	 * Places a file in the document.
 	 * 
 	 * Sample code:
 	 * <code>
 	 * var file = new File('/path/to/image.jpg');
 	 * var item = document.place(file);
 	 * </code>
 	 * 
 	 * @param file the file to place
 	 * @param linked when set to {@code true}, the placed object is a
 	 *        link to the file, otherwise it is embedded within the document
 	 */
 	public native Item place(File file, boolean linked);
 	
 	public Item place(File file) {
 		return place(file, true);
 	}
 
 	/**
 	 * @jshide
 	 */
 	public native void invalidate(float x, float y, float width, float height);
 	
 	/**
 	 * Invalidates the rectangle in artwork coordinates. This will cause all
 	 * views of the document that contain the given rectangle to update at the
 	 * next opportunity.
 	 */
 	public void invalidate(Rectangle rect) {
 		invalidate((float) rect.x, (float) rect.y, (float) rect.width, (float) rect.height);
 	}
 
 	private native boolean nativeWrite(File file, int formatHandle, boolean ask);
 	
 	public boolean write(File file, FileFormat format, boolean ask) {
 		if (format == null) {
 			// Try to get format by extension
 			String name = file.getName();
 			int pos = name.lastIndexOf('.');
 			format = FileFormatList.getInstance().get(name.substring(pos + 1));
 			if (format == null)
 				format = this.getFileFormat();
 		}
 		return nativeWrite(file, format != null ? format.handle : 0, ask);
 	}
 
 	public boolean write(File file, FileFormat format) {
 		return write(file, format, false);
 	}
 
 	public boolean write(File file) {
 		return write(file, null, false);
 	}
 
 	/**
 	 * Checks whether the document contains any selected items.
 	 * 
 	 * @return {@code true} if the document contains selected items,
 	 *         false otherwise.
 	 * 
 	 * @jshide
 	 */
 	public native boolean hasSelectedItems();
 
 	/**
 	 * The selected items contained within the document.
 	 */
 	public native ItemList getSelectedItems();
 
 	/**
 	 * Returns the selected items that are instances of one of the passed classes.
 	 * 
 	 * Sample code:
 	 * <code>
 	 * // Get all selected groups and paths:
 	 * var items = document.getSelectedItems([Group, Path]);
 	 * </code>
 	 * @param types
 	 */
 	public ItemList getSelectedItems(Class[] types) {
 		if (types == null) {
 			return getSelectedItems();
 		} else {
 			HashMap<Object, Object> map = new HashMap<Object, Object>();
 			map.put(ItemAttribute.SELECTED, true);
 			return getItems(types, map);
 		}
 	}
 	
 	/**
 	 * Returns the selected items that are an instance of the passed class.
 	 * 
 	 * Sample code:
 	 * <code>
 	 * // Get all selected rasters:
 	 * var items = document.getSelectedItems(Raster);
 	 * </code>
 	 * @param types
 	 */
 	public ItemList getSelectedItems(Class type) {
 		return getSelectedItems(new Class[] { type });
 	}
 
 	/**
 	 * Deselects all the selected items in the document.
 	 */
 	public native void deselectAll();
 	
 	private native ItemList nativeGetMatchingItems(Class type, HashMap<Integer, Boolean> attributes);
 
 	/**
 	 * @jshide
 	 */
 	public ItemList getItems(Class[] types, Map<Object, Object> attributes) {
 		// Convert the attributes list to a new HashMap containing only
 		// integer -> boolean pairs.
 		HashMap<Integer, Boolean> converted = new HashMap<Integer, Boolean>();
 		if (attributes != null) {
 			for (Map.Entry entry : attributes.entrySet()) {
 				Object key = entry.getKey();
 				if (!(key instanceof ItemAttribute)) {
 					key = EnumUtils.get(ItemAttribute.class, key.toString());
 					if (key == null)
 						throw new ScriptographerException("Undefined attribute: " + entry.getKey());
 				}
 				converted.put(((ItemAttribute) key).value,
 						ConversionUtils.toBoolean(entry.getValue()));
 			}
 		}
 		ItemList set = null;
 		for (int i = 0; i < types.length; i++) {
 			Class type = types[i];
 			ItemList subSet = nativeGetMatchingItems(type, converted);
 			// Filter out TextItems that do not match the given type.
 			// This is needed since nativeGetMatchingItems returns all TextItems...
 			// TODO: Move this to the native side maybe?
 			if (TextItem.class.isAssignableFrom(type))
 				for (Item item : subSet)
 					if (!type.isInstance(item))
 						subSet.remove(item);
 			if (set == null) {
 				set = subSet;
 			} else {
 				set.addAll(subSet);
 			}
 		}
 		// Filter out matched children when the parent matches too
 		for (int i = set.size() - 1; i >= 0; i--) {
 			Item item = set.get(i);
 			if (set.contains(item.getParent()))
 				set.remove(i);
 		}
 		// TODO: Expand PathItem -> Path / CompoundPath
 		return set;
 	}
 
 	/**
 	 * @jshide
 	 */
 	public ItemList getItems(Class[] types) {
 		return getItems(types, (Map<Object, Object>) null);
 	}
 
 	/**
 	 * @jshide
 	 */
 	public ItemList getItems(Class type, Map<Object, Object> attributes) {
 		return getItems(new Class[] { type }, attributes);
 	}
 	
 	/**
 	 * @jshide
 	 */
 	public ItemList getItems(Class type) {
 		return getItems(new Class[] { type });
 	}
 
 	/**
 	 * Returns all items that match a set of attributes, as specified by the
 	 * passed map. For each of the keys in the map, the demanded value can
 	 * either be true or false.
 	 * 
 	 * Sample code: <code>
 	 * // All hidden paths and rasters contained in the document.
 	 * var hiddenItems = document.getItems({ 
 	 *     type: [Path, Raster], 
 	 *     hidden: true
 	 * });
 	 * 
 	 * // All locked Paths contained in the document.
 	 * var lockedItems = document.getItems({
 	 *     type: Path,
 	 *     locked: true
 	 * });
 	 * </code>
 	 * 
 	 * @param attributes an object containing the various attributes to check
 	 *        for. The key {@code type} defines a single prototype or an array of prototypes to check for.
 	 *        The following keys have {@code Boolean} values to check for the state of the matching items: {@enum ItemAttribute}.
 	 */
 	public ItemList getItems(Map<Object, Object> attributes) {
 		ArrayList<Class> classes = new ArrayList<Class>();
 		// Convert types to class array:
 		Object types = attributes.get("type");
 		if (types != null) {
 			if (types instanceof String)
 				types = ((String) types).split(",");
 			if (types instanceof Object[]) {
 				for (Object type : (Object[]) types) {
 					if (type instanceof Class) {
 						classes.add((Class) type);
 					} else if (type instanceof String) {
 						// Try loading class from String name.
 						try {
							classes.add(Class.forName(Item.class.getPackage() + "." + type));
 						} catch (ClassNotFoundException e) {
 						}
 					}
 				}
 			} else if (types instanceof Class)
 				classes.add((Class) types);
 		}
 		// Filter out classes again that are not inheriting Item.
 		for (int i = classes.size() - 1; i >= 0; i--)
 			if (!Item.class.isAssignableFrom((classes.get(i))))
 				classes.remove(i);
 		// If no class was specified, match them all through Item.
 		if (classes.isEmpty())
 			classes.add(Item.class);
 		// Remove types from map that's passed to getItems.
 		HashMap<Object, Object> clone = new HashMap<Object, Object>(attributes);
 		clone.remove("type");
 		return getItems(classes.toArray(new Class[classes.size()]), clone);
 	}
 
 	/* TODO: make these
 	public Item getInsertionItem();
 	public int getInsertionOrder();
 	public boolean isInsertionEditable();
 	*/
 
 	private Path createPath() {
 		activate(false, true);
 		return new Path();
 	}
 
 	/**
 	 * @jshide
 	 */
 	public Path createLine(Point pt1, Point pt2) {
 		Path path = this.createPath();
 		path.moveTo(pt1);
 		path.lineTo(pt2);
 		return path;
 	}
 
 	/**
 	 * @jshide
 	 */
 	public Path createLine(double x1, double y1, double x2, double y2) {
 		return createLine(new Point(x1, y1), new Point(x2, y2));
 	}
 
 	/**
 	 * Creates a rectangular path
 	 * 
 	 * @param rect
 	 * @return the newly created path
 	 * 
 	 * @jshide
 	 */
 	public native Path createRectangle(Rectangle rect);
 
 	/**
 	 * @jshide
 	 */
 	public Path createRectangle(double x, double y, double width, double height) {
 		return createRectangle(new Rectangle(x, y, width, height));
 	}
 		
 	/**
 	 * Creates a rectangular path with rounded corners
 	 * 
 	 * @param rect
 	 * @param size the size of the rounded corners
 	 * @return the newly created path
 	 * 
 	 * @jshide
 	 */
 	public native Path createRoundRectangle(Rectangle rect, Size size);
 
 	/**
 	 * @jshide
 	 */
 	public Path createRoundRectangle(double x, double y, double width, double height, float hor, float ver) {
 		return createRoundRectangle(new Rectangle(x, y, width, height), new Size(hor, ver));
 	}
 
 	/**
 	 * Creates an oval shaped path
 	 * 
 	 * @param rect
 	 * @param circumscribed if this is set to true the oval shaped path will be
 	 *        created so the rectangle fits into it. If it's set to false the
 	 *        oval path will fit within the rectangle.
 	 * @return the newly created path
 	 * 
 	 * @jshide
 	 */
 	public native Path createOval(Rectangle rect, boolean circumscribed);
 
 	/**
 	 * Creates an oval shaped path
 	 * @param rect
 	 * @return the newly created path
 	 * 
 	 * @jshide
 	 */
 	public Path createOval(Rectangle rect) {
 		return createOval(rect, false);
 	}
 
 	/**
 	 * @jshide
 	 */
 	public Path createOval(double x, double y, double width, double height, boolean circumscribed) {
 		return createOval(new Rectangle(x, y, width, height), circumscribed);
 	}
 	
 	/**
 	 * @jshide
 	 */
 	public Path createOval(double x, double y, double width, double height) {
 		return createOval(x, y, width, height);
 	}
 
 	/**
 	 * Creates a regular polygon shaped path
 	 * 
 	 * @param numSides the number of sides of the polygon
 	 * @param center
 	 * @param radius
 	 * @return the newly created path
 	 * 
 	 * @jshide
 	 */
 	public native Path createRegularPolygon(Point center, int numSides,
 			float radius);
 
 	/**
 	 * Created a star shaped path
 	 * 
 	 * The largest of {@code radius1} and {@code radius2} will be
 	 * the outer radius of the star. The smallest of radius1 and radius2 will be
 	 * the inner radius.
 	 * 
 	 * @param numPoints the number of points of the star
 	 * @param center
 	 * @param radius1
 	 * @param radius2
 	 * @return the newly created path
 	 * 
 	 * @jshide
 	 */
 	public native Path createStar(Point center, int numPoints, float radius1,
 			float radius2);
 
 	/**
 	 * Creates a spiral shaped path
 	 * 
 	 * @param firstArcCenter the center point of the first arc
 	 * @param start the starting point of the spiral
 	 * @param decayPercent the percentage by which each succeeding arc will be
 	 *        scaled
 	 * @param numQuarterTurns the number of quarter turns (arcs)
 	 * @param clockwiseFromOutside if this is set to {@code true} the
 	 *        spiral will spiral in a clockwise direction from the first point.
 	 *        If it's set to {@code false} it will spiral in a counter
 	 *        clockwise direction
 	 * @return the newly created path
 	 * 
 	 * @jshide
 	 */
 	public native Path createSpiral(Point firstArcCenter, Point start,
 			float decayPercent, int numQuarterTurns,
 			boolean clockwiseFromOutside);
 
 	/**
 	 * @jshide
 	 */
 	public Path createCircle(Point center, float radius) {
 		return createOval(new Rectangle(
 				center.subtract(radius, radius),
 				center.add(radius, radius)));
 	}
 
 	/**
 	 * @jshide
 	 */
 	public Path createCircle(float x, float y, float radius) {
 		return createCircle(new Point(x, y), radius);
 	}
 
 	
 	protected native HitResult nativeHitTest(Point point, int request,
 			float tolerance, Item item);
 
 	/**
 	 * @param point
 	 * @param request
 	 * @param tolerance the hit-test tolerance in view coordinates (pixels at
 	 *        the current zoom factor). correct results for large values are not
 	 *        guaranteed {@default 2}
 	 */
 	public HitResult hitTest(Point point, HitRequest request, float tolerance) {
 		return this.nativeHitTest(point, (request != null ? request : HitRequest.ALL).value, tolerance, null);
 	}
 
 	public HitResult hitTest(Point point, HitRequest request) {
 		return this.hitTest(point, request, HitResult.DEFAULT_TOLERANCE);
 	}
 
 	public HitResult hitTest(Point point) {
 		return this.hitTest(point, HitRequest.ALL, HitResult.DEFAULT_TOLERANCE);
 	}
 	
 	private native int nativeGetStories();
 	
 	/**
 	 * Text reflow is suspended during script execution. when reflowText() is
 	 * called, the reflow of text is forced.
 	 */
 	public native void reflowText();
 
 	private TextStoryList stories = null;
 	
 	/**
 	 * The stories contained within the document.
 	 */
 	public TextStoryList getStories() {
 		// We need to version TextStoryLists, since document handles seem to not be unique:
 		// When there is only one document, closing it and opening a new one results in the
 		// same document handle. Versioning seems the only way to keep story lists updated.
 		if (stories == null) {
 			int handle = nativeGetStories();
 			if (handle != 0)
 				stories = new TextStoryList(handle, this);
 		} else if (stories.version != CommitManager.version) {
 			int handle = nativeGetStories();
 			if (handle != 0)
 				stories.changeHandle(handle);
 			else
 				stories = null;
 		}
 		return stories;
 	}
 
 	/**
 	 * Checks whether the document is valid, i.e. it hasn't been closed.
 	 * 
 	 * Sample code:
 	 * <code>
 	 * print(document.isValid()); // true
 	 * document.close();
 	 * print(document.isValid()); // false
 	 * </code>
 	 * 
 	 * @return {@true if the document is valid}
 	 */
 	public native boolean isValid();
 
 	private native int nativeGetData();
 
 	/**
 	 * An object contained within the document which can be used to store data.
 	 * The values in this object can be accessed even after the file has been closed and opened again.
 	 * 
 	 * Sample code:
 	 * <code>
 	 * document.data['shoppingList'] = ['oranges', 'apples'];
 	 * print(document.data['shoppingList']); // 'oranges', 'apples'
 	 * </code>
 	 * 
 	 */
 	public Dictionary getData() {
 		if (data == null)
 			data = Dictionary.wrapHandle(nativeGetData(), this);
 		return data;	
 	}
 
 	public void setData(Map<String, Object> map) {
 		Dictionary data = getData();
 		if (map != data) {
 			data.clear();
 			data.putAll(map);
 		}
 	}
 }
