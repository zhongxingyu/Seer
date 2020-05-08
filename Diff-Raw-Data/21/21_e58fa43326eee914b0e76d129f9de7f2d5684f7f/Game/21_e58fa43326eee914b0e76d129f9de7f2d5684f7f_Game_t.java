 package com.episkipoe.common;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.episkipoe.common.draw.ImageLibrary;
 import com.episkipoe.common.draw.TextUtils;
 import com.episkipoe.common.interact.MouseMode;
 import com.episkipoe.common.inventory.Inventory;
 import com.episkipoe.common.player.Player;
 import com.episkipoe.common.rooms.Room;
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.canvas.dom.client.CssColor;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 
/**
 *   The main class, contains the {@link Canvas}, the {@link Player}, etc
 */
 public class Game {
 	static private Canvas canvas;
 	static public Context2d context;
 	static private final CssColor redrawColor = CssColor.make("rgba(255,255,255,0.6)");
 	static public final int canvasWidth = 800;
 	static public final int canvasHeight = 600;
 	static private final int refreshRate = 45;
 	static public final int secondsToFrames(int seconds) {
 		return 1000/refreshRate*seconds;
 	}
 	
 	static private GameStorage localStorage;
 	static public ImageLibrary images;
 	static public Player player;
 	
 	
 	static private Inventory inventory;
 	static public Inventory getInventory() {
 		if(inventory==null) inventory = new Inventory();
 		return inventory;
 	}
 	static public void newInventory() {
 		inventory = new Inventory();
 	}
 	
 	/**
 	 * This should be called from the app's entry point
 	 */
 	static public void begin(GameStorage storage) {
 		localStorage = storage;
 		canvas = Canvas.createIfSupported();
         if (canvas == null) {
               RootPanel.get().add(new Label("Sorry, your browser doesn't support the HTML5 Canvas element"));
               return;
         }
 		context = canvas.getContext2d();
         canvas.setStyleName("mainCanvas");
         canvas.setWidth(canvasWidth + "px");
         canvas.setCoordinateSpaceWidth(canvasWidth);
  
         canvas.setHeight(canvasHeight + "px");
         canvas.setCoordinateSpaceHeight(canvasHeight);
         
         canvas.addKeyPressHandler(keyPressHandler());
         
 		currentMousePos = new Point();
 		player = new Player();
 		loadImages();
         RootPanel.get().add(canvas);
         try {
         	localStorage.startup();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
         
         // setup timer
         final Timer timer = new Timer() {
           @Override
           public void run() {
         	  draw();
           }
         };
         timer.scheduleRepeating(refreshRate);
 	}
 
 	/*
 	 * Setup
 	 */
 	static private void loadImages() {
 		images = new ImageLibrary();
 		if(localStorage.getCommonImages() != null)
 			images.loadImages(Arrays.asList(localStorage.getCommonImages()));	
 	}
 
 	/*
 	 * Events
 	 */
 	static public void setMouseMode(MouseMode newMode) {
 		if(newMode.mouseMoveHandler() != null) canvas.addMouseMoveHandler(newMode.mouseMoveHandler());
 		if(newMode.mouseUpHandler() != null) canvas.addMouseUpHandler(newMode.mouseUpHandler());
 		if(newMode.mouseDownHandler() != null) canvas.addMouseDownHandler(newMode.mouseDownHandler());
 	}
 	
 	static public Point getPointFromEvent(MouseMoveEvent event) {
 		return new Point(event.getRelativeX(canvas.getElement()), event.getRelativeY(canvas.getElement()));
 	}
 	static public Point getPointFromEvent(MouseDownEvent event) {
 		return new Point(event.getRelativeX(canvas.getElement()), event.getRelativeY(canvas.getElement()));
 	}
 	
 	static private Point currentMousePos;
 	static public Point getCurrentPos() { return currentMousePos; }
 
 	static public Point getCenterPoint() {
 		return new Point(canvasWidth*0.5, canvasHeight*0.5);
 	}
 
 	public static void click(Point point) throws Exception {
 		//The Inventory button is drawn at the top-left of the screen (except in the InventoryRoom)
 		if (room.showHud() && getInventory().intersectsWith(point)) {
 			getInventory().click();
 			return;
 		}
 		room.click(point);
 	}
 
 	/**
 	 * 
 	 * @param point
 	 * @return true if clicking at this point will cause something to happen
 	 */
 	static public boolean pointIsClickable(Point point) {
 		if(getInventory().intersectsWith(point) && room.showHud()) {
 			return true;
 		}
 		return room.pointIsClickable(point);
 	}
 
 
 	/*
 	 * Room management
 	 */
 	/**
 	 * The {@link Room} that the Player currently is in
 	 */
 	static public Room room;
 	/**
 	 * The room that the player was in before {@link #room}
 	 */
 	static public Room previousRoom;
 	/**
 	 * 
 	 * @param newRoom  becomes the new {@link #room}
 	 * @throws Exception
 	 */
 	static public void switchRoom(Class <? extends Room> newRoom) {
 		previousRoom = room;
 		if(previousRoom!=null) {
 			previousRoom.getDialog().clear();
 			previousRoom.onExit();
 		}
 		try {
 			room = getRoom(newRoom);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if(room==null) {
 			System.out.println("WARNING:  Room " + newRoom + " could not be loaded\n");
 		}
 		room.enter();
 		images.loadImages(room.getAllImages());
 	}
 	
 	/**
 	 * switch to the {@link #previousRoom}
 	 * @throws Exception
 	 */
 	static public void goBack() {
 		switchRoom(previousRoom.getClass());
 	}
 	
 	static private Map <Class<? extends Room>, Room> roomSet;
 	static public Map <Class<? extends Room>, Room> getRoomSet() {
 		if(roomSet==null) {
 			roomSet = new HashMap<Class<? extends Room>, Room> ();
 		}		
 		return roomSet;
 	}
 	static public Room getRoom(Class<? extends Room> room) throws Exception {
 		if(!getRoomSet().containsKey(room)) {
 			//Reflection does not work on server, i.e.:  getRoomSet().put(room, (Room) GWT.create(room));
 			//rooms must be registered in advance
 			return null;
 		}
 		return getRoomSet().get(room);
 	}
 	
 	static public void registerRoom(Class<? extends Room> roomClass, Room room) {
 		getRoomSet().put(roomClass, room);
 	}
 	
 	/*
 	 * Draw
 	 */
 	static public void draw() {
 		if(player == null || room == null) return; 
 		
 		context.setFillStyle(redrawColor);
 	    context.fillRect(0, 0, canvasWidth, canvasHeight);
 	    room.draw(context);
 		if (room.showHud()) {
 			getInventory().draw(context);
 			String money = "$"+player.getMoney();
 			TextUtils.drawWhiteText(context, Arrays.asList(money), new Point(0,70));
 		}
 		player.draw(context);
 	}
 	
 	static private KeyPressHandler keyPressHandler() {
 		return new KeyPressHandler() {
 			@Override
 			public void onKeyPress(KeyPressEvent event) {
 				if(event.getCharCode() == 'b') { 
 					try {
 						goBack();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}				
 				} else if(event.getCharCode() == 'n') { 
 					try {
 						localStorage.newGame();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				} 
 			}
 		};
 	}
 
 	public static void save() {
 		localStorage.saveGame();
 	}
 
 }
