 package com.episkipoe.common.rooms;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.episkipoe.common.Door;
 import com.episkipoe.common.Game;
 import com.episkipoe.common.Point;
 import com.episkipoe.common.dialog.Dialog;
 import com.episkipoe.common.dialog.DialogElement;
 import com.episkipoe.common.draw.Drawable;
 import com.episkipoe.common.draw.ImageDrawable;
 import com.episkipoe.common.draw.ImageUtils;
 import com.episkipoe.common.interact.Clickable;
 import com.google.gwt.canvas.dom.client.Context2d;
 
 public abstract class Room {
 	public Room() { }
 
 	/**
 	 * 
 	 * @return the list of image file names that should be found in the {@link ImageLibrary}
 	 * 	this list may, but does not need to, include the background image 
 	 *  or images associated with {@link Drawable}s loaded in the constructor
 	 */
 	public Collection<String> getRequiredImages() { return new ArrayList<String>(); }
 	
 	/**
 	 * 
 	 *  the image in the main {@link ImageLibrary} to draw full-screen behind everything
 	 */
 	private String background;
 	public final void setBackground(String background) { this.background = background; }
 	protected final String getBackground() { return background; }
 
 	/**
 	 * called (only once) after the last required image has been loaded
 	 */
 	protected void onLoad() {}
 	/**
 	 * called every time the user clicks to enter the room
 	 */
 	public void onEnter() { }
 	/**
 	 * called when the player leaves the room
 	 */
 	public void onExit() { }
 	
 	/**
 	 * Called at the beginning of the Room's draw method (but after the background)
 	 * @param context
 	 */
 	protected void preDraw(Context2d context) {}
 	/**
 	 * Called at the end of the Room's draw method
 	 * @param context
 	 */
 	protected void postDraw(Context2d context) {}
 
 	public boolean showHud() { return true; }
 	
 	/*
 	 * Final methods
 	 */
 	
 	//don't manipulate drawables directly, concurrent modification error will occur
 	private List<Drawable> drawables; 
 	protected final List<Drawable> getDrawables() {
 		if(drawables==null) drawables = new ArrayList<Drawable> ();
 		return drawables;
 	}
 	List<Drawable> itemsToAdd;
 	private final List<Drawable> getItemsToAdd() { 
 		if(itemsToAdd==null) itemsToAdd = new ArrayList<Drawable>();
 		return itemsToAdd;
 	}	
 	public final void addDrawable(Drawable d) {
 		getItemsToAdd().add(d);
 	}
 
 	List<Drawable> itemsToPrune;
 	private final List<Drawable> getItemsToPrune() { 
 		if(itemsToPrune==null) itemsToPrune = new ArrayList<Drawable>();
 		return itemsToPrune;
 	}
 	public final void removeDrawable(Drawable d) {
 		getItemsToPrune().add(d);
 	}
 	public final void clearDrawables() { 
 		for(Drawable d: getDrawables()) { removeDrawable(d); }
 	}
 	
 	
 	/**
 	 * exits are separate from drawables so that drawables can be cleared without nuking exits
 	 */
 	private List<Drawable> exits;
 	private final List<Drawable> getExits() {
 		if(exits == null) exits = new ArrayList<Drawable>();
 		return exits;
 	}
 	public final void addExit(Drawable e) {
 		getExits().add(e);
 	}	
 	
 	/**
 	 * Convenience method: adds a exit {@link Door} in the default location and style
 	 * @param exit the room to exit to
 	 */
 	protected final void addExit(Class<? extends Room> exit) {
 		addExit(new Door(new Point(560, 500), exit));
 	}
 	/**
 	 * Convenience method:  adds a {@link Door} to the next room in the default location and style
 	 * @param room
 	 */
 	protected final void addRightDoor(Class<? extends Room> room) {
 		addExit(new Door(new Point(700, 500), room, "RightArrow.png"));
 	}
 	/**
 	 * Convenience method:  adds a {@link Door} to the next room in the default location and style
 	 * @param room
 	 */
 	protected final void addLeftDoor(Class<? extends Room> room) {
 		addExit(new Door(new Point(100, 500), room, "LeftArrow.png"));
 	}
 	
 	
 	private List<Clickable> clickables; 
 	private final List<Clickable> getClickables() {
 		if(clickables==null) clickables= new ArrayList<Clickable> ();
 		return clickables;
 	}
 	public final void addClickable(Clickable c) {
 		getClickables().add(c);
 	}	
 	
 	private Dialog dialog;
 	public final Dialog getDialog() { 
 		if(dialog == null) dialog = new Dialog();
 		return dialog;
 	}
 	
 	private final boolean itemExpired(Drawable d) {
 		if(d instanceof DialogElement) {
 			DialogElement dialog = (DialogElement) d;
 			return dialog.expired();
 		}
 		if(d instanceof Dialog) {
 			Dialog dialog = (Dialog) d;
 			return dialog.isEmpty();
 		}		
 		return false;
 	}
 	public final void draw(Context2d context) {
 		if(getBackground() != null) ImageUtils.draw(context, getBackground());
 		if(!getItemsToAdd().isEmpty()) {
 			getDrawables().addAll(getItemsToAdd());
 		}
 		itemsToAdd.clear();
 		preDraw(context);
 		synchronized(drawables) {
 			for(Drawable d: getDrawables()) {
 				d.draw(context);
 				if(itemExpired(d)) {
 					getItemsToPrune().add(d);
 				}
 			}
 		}
 		getDrawables().removeAll(getItemsToPrune());
 		getItemsToPrune().clear();
 		for(Drawable e: getExits()) {
 			e.draw(context);
 		}
 		postDraw(context);
 		getDialog().draw(context);
 	}
 	
 	private List<Clickable> getAllClickables() {
 		List<Clickable> clickList = new ArrayList<Clickable>();
 		for(Drawable d: getExits()) {
 			clickList.add((Clickable)d);
 		}
 		for(Drawable d: getDrawables()) {
 			if(d instanceof Clickable) {
 				clickList.add((Clickable)d);
 			}
 		}
 		clickList.addAll(getClickables());
 		return clickList;
 	}
 
 	/**
 	 * 
 	 * @param point
 	 * @throws Exception 
 	 */
 	public final void click(Point point) throws Exception {
 		for(Drawable d: getExits()) {
 			Clickable target = (Clickable)d;
 			if(target.intersectsWith(point)) {
 				target.click();
 				return;
 			}
 		}
 		synchronized(drawables) {
 			for(Drawable d: getDrawables()) {
 				if(d instanceof Clickable) {
 					Clickable target = (Clickable)d;
 					if(target.intersectsWith(point)) {
 						target.click();
 						if(!target.continueProcessing()) {
 							return;
 						}
 					}
 				}
 			}
 		}
		synchronized(clickables) {
			for(Clickable c: getClickables()) {
				if(c.intersectsWith(point)) c.click();
 			}
 		}
 	}
 	
 	public final boolean pointIsClickable(Point point) {
 		for(Clickable c: getAllClickables()) {
 			if(c.intersectsWith(point)) return true;
 		}
 		return false;
 	}
 	
 	public final boolean pointHasDrawable(Point point) {
 		List<Drawable> allDrawables = new ArrayList<Drawable>();
 		allDrawables.addAll(getDrawables());
 		allDrawables.addAll(getItemsToAdd());
 		for(Drawable d: allDrawables) {
 			if(d instanceof Clickable) {
 				if(((Clickable) d).intersectsWith(point)) return true;
 			}
 		}
 		return false;
 	}
 	
 	public final Collection<String> getAllImages() {
 		Set<String> images = new HashSet<String>();
 		if(getBackground() != null) images.add(getBackground());
 		
 		for(Drawable d: getDrawables()) {
 			if(d instanceof ImageDrawable) {
 				ImageDrawable img = (ImageDrawable)d;	
 				images.add(img.getFilename());
 			}
 		}
 		
 		Collection<String> required = getRequiredImages();
 		if(required != null) images.addAll(required);
 		images.addAll(Game.getCommonImages());
 		
 		return images;
 	}
 	public final boolean allImagesAreLoaded() {
 		for(String img : getAllImages()) {
 			if(!Game.images.contains(img)) {
 				return false;
 			} 
 		}		
 		return true;
 	}
 	public final void imageLoaded(String fileName) {
 		if(!getAllImages().contains(fileName)) return;
 		if(allImagesAreLoaded()) {
 			onLoad();
 			onEnter();
 		}
 	}
 	public final void enter() {
 		if(allImagesAreLoaded()) {
 			onEnter();
 		} 
 	}
 	
 }
