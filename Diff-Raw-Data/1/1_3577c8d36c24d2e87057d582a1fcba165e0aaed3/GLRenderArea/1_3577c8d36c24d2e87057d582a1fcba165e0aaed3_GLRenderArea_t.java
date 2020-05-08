 package riskyspace.view.opengl.impl;
 
 import java.awt.Color;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 
 import com.jogamp.opengl.util.awt.TextRenderer;
 
 import riskyspace.PlayerColors;
 import riskyspace.logic.SpriteMapData;
 import riskyspace.model.BuildAble;
 import riskyspace.model.Colony;
 import riskyspace.model.Fleet;
 import riskyspace.model.Player;
 import riskyspace.model.PlayerStats;
 import riskyspace.model.Position;
 import riskyspace.model.Territory;
 import riskyspace.services.Event;
 import riskyspace.services.EventBus;
 import riskyspace.view.ViewResources;
 import riskyspace.view.camera.Camera;
 import riskyspace.view.camera.CameraController;
 import riskyspace.view.camera.GLCamera;
 import riskyspace.view.opengl.GLRenderAble;
 import riskyspace.view.opengl.Rectangle;
 import riskyspace.view.opengl.menu.GLColonyMenu;
 import riskyspace.view.opengl.menu.GLFleetMenu;
 import riskyspace.view.opengl.menu.GLPlanetMenu;
 import riskyspace.view.opengl.menu.GLTopMenu;
 
 public class GLRenderArea implements GLRenderAble {
 	
 	/**
 	 * Extra space at the Right and Left sides
 	 * of the screen.
 	 */
 	public static final int EXTRA_SPACE_HORIZONTAL = 3;
 	
 	/**
 	 * Extra space at the Top and Bottom sides
 	 * of the screen.
 	 */
 	public static final int EXTRA_SPACE_VERTICAL = 2;
 	
 	/**
 	 * The area of the screen
 	 */
 	private Rectangle screenArea = null;
 	
 	/**
 	 * The size of one game square
 	 */
 	private int squareSize;
 	
 	/**
 	 * Total size of the game
 	 */
 	private int totalWidth, totalHeight;
 	
 	/**
 	 * Array with coordinates used to draw all horizontal lines
 	 */
 	private int[][] hLines;
 	/**
 	 * Array with coordinates used to draw all vertical lines
 	 */
 	private int[][] vLines;
 	
 	/*
 	 * Star drawing variables
 	 */
 	private int starWidth;
 	private int starHeight;
 	private Point[] stars = new Point[800];
 	
 	/**
 	 * Game size
 	 */
 	private int rows, cols;
 	
 	/**
 	 * The current Camera used to move the View
 	 */
 	private Camera currentCamera = null;
 	
 	/**
 	 * Cameras for all Players
 	 */
 	private Map<Player, Camera> cameras = null;
 	
 	/**
 	 * Camera controller for the current Camera
 	 */
 	private CameraController cc = null;
 	
 	/**
 	 * Clickhandler to handle all clicks on the screen
 	 */
 	private ClickHandler clickHandler;
 	
 	/**
 	 * The player viewing this renderArea
 	 */
 	private Player viewer = null;
 	
 	private boolean gameOver = false;
 	private boolean winner = false;
 
 	/*
 	 * Status box, other player's turn
 	 */
 	private String statusString = "";
 	private Color statusStringColor;
 	private TextRenderer statusTextRenderer;
 	private GLSprite statusBackground;
 	
 	/**
 	 * All game sprites
 	 */
 	private GLSpriteMap sprites;
 	
 	/*
 	 * Menus
 	 */
 	private GLColonyMenu colonyMenu = null;
 	private GLPlanetMenu planetMenu = null;
 	private GLFleetMenu fleetMenu = null;
 	private GLTopMenu topMenu = null;
 
 	private String killedByString;
 
 	/**
 	 * Create a new RenderArea that will be the view of a player
 	 * @param width The width of the Game Screen
 	 * @param height The height of the Game Screen
 	 * @param rows The number of rows in the world to display
 	 * @param cols The number of columns in the world to display
 	 */
 	public GLRenderArea(int width, int height, int rows, int cols) {
 		screenArea = new Rectangle(0, 0, width, height);
 		squareSize = Math.min(width/6,height/6);
 		totalWidth = (cols + 2*EXTRA_SPACE_HORIZONTAL)*squareSize;
 		totalHeight = (rows + 2*EXTRA_SPACE_VERTICAL)*squareSize;
 		this.rows = rows;
 		this.cols = cols;
 		initCameras();
 		createGrid();
 		createStarMap();
 		createMenus();
 		createStatusBox();
 	}
 
 	/**
 	 * Initiates all variables used to draw the status box
 	 */
 	private void createStatusBox() {
 		statusTextRenderer = new TextRenderer(ViewResources.getFont().deriveFont(screenArea.getHeight()/30.0f));
 		statusBackground = new GLSprite("wide_button", 128, 32);
 		int width = screenArea.getWidth()/4;
 		int height = width/4;
 		int x = screenArea.getWidth() / 2 - width / 2;
 		int y = screenArea.getHeight() / 2 - height / 2;
 		statusBackground.setBounds(new Rectangle(x, y, width, height));
 	}
 	
 	/**
 	 * Creates all Menus at the correct locations in awt coordinate space to handle clicks 
 	 * more easily.
 	 */
 	private void createMenus() {
 		int menuWidth = screenArea.getHeight() / 3;
 		colonyMenu = new GLColonyMenu(screenArea.getWidth() - menuWidth, 80,
 				menuWidth, screenArea.getHeight()-80);
 		fleetMenu = new GLFleetMenu(screenArea.getWidth() - menuWidth, 80,
 				menuWidth, screenArea.getHeight()-80);
 		planetMenu = new GLPlanetMenu(screenArea.getWidth() - menuWidth, 80,
 				menuWidth, screenArea.getHeight()-80);
 		topMenu = new GLTopMenu(0, screenArea.getHeight(), screenArea.getWidth(), 80);
 	}
 
 	/**
 	 * Create a grid through x and y coordinates
 	 */
 	private void createGrid() {
 		hLines = new int[rows + 1][];
 		vLines = new int[cols + 1][];
 		
 		/*
 		 * Draw Horizontal lines
 		 */
 		for (int row = 0; row <= rows; row++) {
 			hLines[row] = new int[3];
 			hLines[row][0] = (EXTRA_SPACE_HORIZONTAL) * squareSize;
 			hLines[row][1] = (EXTRA_SPACE_HORIZONTAL + cols) * squareSize;
 			hLines[row][2] = (EXTRA_SPACE_VERTICAL + row) * squareSize;
 		}
 		/*
 		 * Draw Vertical lines
 		 */
 		for (int col = 0; col <= cols; col++) {
 			vLines[col] = new int[3];
 			vLines[col][0] = (EXTRA_SPACE_HORIZONTAL + col) * squareSize;
 			vLines[col][1] = (EXTRA_SPACE_VERTICAL) * squareSize;
 			vLines[col][2] = (EXTRA_SPACE_VERTICAL + rows) * squareSize;
 		}
 	}
 	
 	/**
 	 * Draw the game grid
 	 * @param drawable The drawable to draw to.
 	 */
 	private void drawGrid(GLAutoDrawable drawable) {
 		GL2 gl = drawable.getGL().getGL2();
 		gl.glDisable(GL2.GL_TEXTURE_2D);
 		gl.glColor4f(0.8f, 0.8f, 0.8f, 1);
 		gl.glBegin(GL2.GL_LINES);
 		for (int i = 0; i < hLines.length; i++) {
 			float x, x1, y;
 			x = 2f * (hLines[i][0] - (totalWidth - screenArea.getWidth()) * currentCamera.getX()) / screenArea.getWidth() -1f;
 			x1 = 2f * (hLines[i][1] - (totalWidth - screenArea.getWidth()) * currentCamera.getX()) / screenArea.getWidth() -1f;
 			y = 2f * (hLines[i][2] - (totalHeight - screenArea.getHeight()) * currentCamera.getY()) / screenArea.getHeight() -1f;
 			gl.glVertex3f(x, y, 0.98f);
 			gl.glVertex3f(x1, y, 0.98f);
 		}
 		for (int i = 0; i < hLines.length; i++) {
 			float x, y, y1;
 			x = 2f * (vLines[i][0] - (totalWidth - screenArea.getWidth()) * currentCamera.getX()) / screenArea.getWidth() -1f;
 			y = 2f * (vLines[i][1] - (totalHeight - screenArea.getHeight()) * currentCamera.getY()) / screenArea.getHeight() -1f;
 			y1 = 2f * (vLines[i][2] - (totalHeight - screenArea.getHeight()) * currentCamera.getY()) / screenArea.getHeight() -1f;
 			gl.glVertex3f(x, y, 0.98f);
 			gl.glVertex3f(x, y1, 0.98f);
 		}
 		gl.glEnd();
 		gl.glColor4f(1f, 1f, 1f, 1);
 		gl.glEnable(GL2.GL_TEXTURE_2D);
 	}
 	
 	/**
 	 * Creates random star locations on a background 50% wider and higher than the current screen
 	 */
 	private void createStarMap() {
 		starWidth = (int) (screenArea.getWidth() * 1.5);
 		starHeight = (int) (screenArea.getHeight() * 1.5);
 		for (int i = 0; i < stars.length; i++) {
 			stars[i] = new Point((int) (Math.random()*starWidth), (int) (Math.random()*starHeight));
 		}
 	}
 	
 	/**
 	 * Create cameras and starting locations for these for all players
 	 */
 	private void initCameras() {
 		cameras = new HashMap<Player, Camera>();
 		cameras.put(Player.BLUE, new GLCamera(0.93f,0.08f));
 		cameras.put(Player.RED, new GLCamera(0.07f,0.92f));
 		cameras.put(Player.GREEN, new GLCamera(0.07f,0.08f));
 		cameras.put(Player.YELLOW, new GLCamera(0.93f,0.92f));
 		cc = new CameraController();
 	}
 	
 	/**
 	 * Set this view to the perspective of a Player
 	 * @param player The Player whos perspective is to be used for this view
 	 */
 	public void setViewer(Player player) {
 		this.viewer = player;
 		currentCamera = cameras.get(player);
 		cc.setCamera(currentCamera);
 		if (!cc.isAlive()) {
 			cc.start();
 		}
 	}
 	
 	/**
 	 * Tell this view about the current player so that is may display useful information
 	 * @param player The current Player
 	 */
 	public void setActivePlayer(Player player) {
 		if (player == viewer) {
 			statusString = "";
 		} else if (!gameOver) {
 			statusString = player + "'S TURN";
 			statusStringColor = PlayerColors.getColor(player);
 		}
 	}
 	
 	public void showGameOver(Player killedBy) {
 		statusBackground = new GLSprite("square_button", 128, 80);
 		int width = screenArea.getWidth() / 2;
 		int height = screenArea.getHeight() / 2;
 		int x = screenArea.getWidth() / 2 - width / 2;
 		int y = screenArea.getHeight() / 2 - height / 2;
 		statusBackground.setBounds(new Rectangle(x, y, width, height));
 		statusString = "";
 		killedByString = killedBy.toString();
 		gameOver = true;
 	}
 	
 	public void showWinnerScreen() {
		gameOver = true;
 		winner = true;
 	}
 	
 	@Override
 	public Rectangle getBounds() {
 		return screenArea;
 	}
 
 	@Override
 	public void draw(GLAutoDrawable drawable, Rectangle objectRect, Rectangle targetArea, int zIndex) {
 		if(currentCamera != null){
 			drawStars(drawable);
 			drawGrid(drawable);
 			
 			if (sprites != null) {
 				sprites.draw(drawable, getCameraRect(), getCameraRect(), 2);
 			}
 			
 			drawSelectionBox(drawable, 10);
 			
 			/*
 			 * Draw menus
 			 */
 			topMenu.draw(drawable, topMenu.getBounds(), targetArea, 50);
 			colonyMenu.draw(drawable, colonyMenu.getBounds(), screenArea, 50);
 			fleetMenu.draw(drawable, fleetMenu.getBounds(), screenArea, 50);
 			planetMenu.draw(drawable, planetMenu.getBounds(), screenArea, 50);
 			
 			drawStatusBox(drawable, targetArea, 50);
 			drawGameOver(drawable, targetArea, 51);
 		}
 		//TODO: DRAW PICTURE
 	}
 
 	private void drawGameOver(GLAutoDrawable drawable, Rectangle targetArea, int zIndex) {
 		if (gameOver) {
 			statusBackground.draw(drawable, statusBackground.getBounds(), targetArea, zIndex);
 			
 			/* Center of screen position */
 			int cX = screenArea.getWidth() / 2;
 			int cY = screenArea.getHeight() / 2;
 			
 			String line1 = "";
 			String line2 = "";
 			String line3 = "";
 			String line4 = "";
 			
 			if (!winner) {
 			
 			line1 = "YOU HAVE BEEN DEFEATED!";
 			line2 = "ALL YOUR BASE";
 			line3 = "ARE BELONG TO";
 			line4 = killedByString;
 			
 			} else {
 				
 				line1 = "GREAT SUCCESS!";
 				line2 = "YOU KILLED ALL OF THE";
 				line3 = "NOOBS IN OUTER SPACE";
 				line4 = "";
 			}
 			/* Calculate lineHeight lH to use as Y offset */
 			int lH = (int) statusTextRenderer.getBounds("height").getHeight()+screenArea.getHeight() / 24;
 			
 			/* Calculate width offset for the intended string to print at row3 */
 			int wA = (int) statusTextRenderer.getBounds(line3).getWidth();
 			int wB = (int) statusTextRenderer.getBounds(line4).getWidth();
 			int cS = (int) statusTextRenderer.getCharWidth(' '); // Estimated width of one char
 			int wO = wA + wB;
 			
 			/* Calculate center of text Y */
 			int cTextY = cY + lH;
 			
 			Rectangle row1 = new Rectangle(cX - 1, cTextY + lH - 1, 2, 2);			
 			Rectangle row2 = new Rectangle(cX - 1, cTextY - 1, 2, 2);
 			Rectangle row3a = new Rectangle(cX - wO/2 + wA/2 - 1, cTextY - lH - 1, 2, 2);
 			Rectangle row3b = new Rectangle(cX + wO/2 - wB/2 + cS - 1, cTextY - lH - 1, 2, 2);
 			
 			/* Here we draw the game over message */
 
 			drawString(statusTextRenderer, row1, line1, ViewResources.WHITE);
 			drawString(statusTextRenderer, row2, line2, ViewResources.WHITE);
 			drawString(statusTextRenderer, row3a, line3, ViewResources.WHITE);
 			drawString(statusTextRenderer, row3b, line4, statusStringColor);
 			
 			statusTextRenderer.setColor(1,1,1,1);
 		}
 	}
 
 	/**
 	 * Draw a status box displaying the status text
 	 * @param drawable
 	 * @param targetArea
 	 * @param zIndex
 	 */
 	private void drawStatusBox(GLAutoDrawable drawable, Rectangle targetArea, int zIndex) {
 		if (statusString.length() > 0 && !gameOver) {
 			statusBackground.draw(drawable, statusBackground.getBounds(), targetArea, zIndex);
 			drawString(statusTextRenderer, statusBackground.getBounds(), statusString, statusStringColor);
 		}
 	}
 
 	/**
 	 * Draws a String centered at a Rectangle with the given TextRenderer
 	 * @param textRenderer The TextRenderer used for this font
 	 * @param rect The Rectangle this text should be centered at
 	 * @param s The String to draw
 	 * @param c The Color of the Text
 	 */
 	private void drawString(TextRenderer textRenderer, Rectangle rect, String s, Color c) {
 		textRenderer.beginRendering(screenArea.getWidth(), screenArea.getHeight());
 		textRenderer.setColor(c);
 		int textWidth = (int) textRenderer.getBounds(s).getWidth();
 		int textHeigth = (int) textRenderer.getBounds(s).getHeight();
 		int x = (rect.getX() + rect.getWidth() / 2) - textWidth / 2;
 		int y = (rect.getY() + rect.getHeight() / 2) - textHeigth / 2;
 		textRenderer.draw(s, x, y);
 		textRenderer.setColor(1,1,1,1);
 		textRenderer.endRendering();
 	}
 	
 	/**
 	 * Create a rectangle based on the current location of the camera
 	 * @return A Rectangle representing the current view of the camera.
 	 */
 	private Rectangle getCameraRect() {
 		int x = (int) ((totalWidth - screenArea.getWidth())*currentCamera.getX());
 		int y = (int) ((totalHeight - screenArea.getHeight())*currentCamera.getY());
 		return new Rectangle(x, y, screenArea.getWidth(), screenArea.getHeight());
 	}
 	
 	/**
 	 * Return the x coordinate of the camera in pixels
 	 * @return the current cameras x value
 	 */
 	public int translatePixelsX() {
 		return (int) (((cols+2*EXTRA_SPACE_HORIZONTAL)*squareSize - getBounds().getWidth())*currentCamera.getX());
 	}
 	
 	/**
 	 * Return the y coordinate of the camera in pixels 
 	 * @return the current cameras y value
 	 */
 	public int translatePixelsY() {
 		return (int) (((rows+2*EXTRA_SPACE_VERTICAL)*squareSize - getBounds().getHeight())*currentCamera.getY());
 	}
 	
 	/**
 	 * Draw a green box from the last pressed point to the current mouse selection if the 
 	 * mouse button has not yet been released.
 	 * @param drawable The drawable to draw to
 	 * @param zIndex The z Level of draw space to draw in
 	 */
 	private void drawSelectionBox(GLAutoDrawable drawable, int zIndex) {
 		/*
 		 * Draw a transparent box from pressed point
 		 * to current selection
 		 * 
 		 * If mouse has not yet been clicked return.
 		 */
 		if (clickHandler == null || clickHandler.pressedPoint == null) {
 			return;
 		}
 		
 		Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
 		
 		float x = 2*((float) (clickHandler.pressedPoint.x - translatePixelsX())/screenArea.getWidth()) -1f;
 		float y = 2*((float) (clickHandler.pressedPoint.y - translatePixelsY())/screenArea.getHeight()) -1f;
 		float x1 = 2*((float) mouseLoc.x)/screenArea.getWidth() - 1f;
 		float y1 = 2*(float) (screenArea.getHeight() - mouseLoc.y)/screenArea.getHeight() - 1f;
 		
 		GL2 gl = drawable.getGL().getGL2();
 		gl.glDisable(GL.GL_TEXTURE_2D);
 		
 		gl.glColor4f(0.1f, 1.0f, 0.2f, 1.0f);
 		gl.glBegin(GL2.GL_LINE_STRIP);
 		
 		gl.glVertex3f(x, y, 1f / zIndex);
 		gl.glVertex3f(x1, y, 1f / zIndex);
 		gl.glVertex3f(x1, y1, 1f / zIndex);
 		gl.glVertex3f(x, y1, 1f / zIndex);
 		gl.glVertex3f(x, y, 1f / zIndex);
 		
 		gl.glEnd();
 		
 		gl.glColor4f(0.1f, 1.0f, 0.2f, 0.3f);
 		gl.glBegin(GL2.GL_QUADS);
 		
 		gl.glVertex3f(x, y, 1f / zIndex);
 		gl.glVertex3f(x1, y, 1f / zIndex);
 		gl.glVertex3f(x1, y1, 1f / zIndex);
 		gl.glVertex3f(x, y1, 1f / zIndex);
 		
 		gl.glEnd();
 		
 		gl.glEnable(GL.GL_TEXTURE_2D);
 		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
 	}
 
 	/**
 	 * Draw white points representing stars at the locations
 	 * created by <code>createStars()</code>
 	 * @param drawable The drawable to draw to
 	 * @see createStars()
 	 */
 	private void drawStars(GLAutoDrawable drawable) {
 		GL2 gl = drawable.getGL().getGL2();
 		
 		gl.glDisable(GL2.GL_TEXTURE_2D);
 		gl.glColor4f(1, 1, 1, 1);
 		float x, y;
 		gl.glBegin(GL2.GL_POINTS);
 		for (int i = 0; i < stars.length; i++) {
 			x = 2*(stars[i].x - (starWidth - screenArea.getWidth())*currentCamera.getX())/screenArea.getWidth() - 1f;
 			y = 2*(stars[i].y - (starHeight - screenArea.getHeight())*currentCamera.getY())/screenArea.getHeight() - 1f;
 			gl.glVertex3f(x, y, 0.99f);
 		}
 		gl.glEnd();
 		gl.glEnable(GL2.GL_TEXTURE_2D);
 	}
 
 	/**
 	 * Update the size of the screen
 	 * @deprecated The game is now fullscreen exclusive
 	 * @param width The new width
 	 * @param height The new height
 	 */
 	public void updateSize(int width, int height) {
 		this.squareSize = Math.min(width/6,height/6);
 		screenArea.setHeight(height);
 		screenArea.setWidth(width);
 	}
 	
 	/**
 	 * Returns the handler for all clicks in this RenderArea
 	 * @return a ClickHandler that will listen to clicks and 
 	 * process them in this renderArea
  	 */
 	public MouseListener getClickHandler() {
 		if (clickHandler == null) {
 			clickHandler = new ClickHandler();
 		}
 		return clickHandler;
 	}
 	
 	/**
 	 * Add this Listener to the current frame if the Camera 
 	 * should be moveable with keyboard arrows
 	 * @return
 	 */
 	public KeyListener getCameraKeyListener() {
 		return cc;
 	}
 	
 	/* Data management Methods*/
 	public void updateData(SpriteMapData data) {
 		sprites = GLSpriteMap.getSprites(data, squareSize);
 	}
 	
 	/**
 	 * Set PlayerStats in this view to update shown information
 	 * @param stats PlayerStats to update the view with
 	 */
 	public void setStats(PlayerStats stats) {
 		topMenu.setStats(stats);
 		colonyMenu.setStats(stats);
 	}
 
 	/**
 	 * Set Queue Information in this view to update display
 	 * @param colonyQueues Queues to update the view with
 	 */
 	public void setQueue(Map<Colony, List<BuildAble>> colonyQueues) {
 		colonyMenu.setQueues(colonyQueues);
 		/*
 		 * Set for BuildQueueMenu
 		 */
 	}
 	
 	/**
 	 * Tell this RenderArea to display information about
 	 * a selected Fleet
 	 * @param selection The selected Fleet
 	 */
 	public void showFleet(Fleet selection) {
 		hideSideMenus();
 		fleetMenu.setFleet(selection);
 		fleetMenu.setVisible(true);
 	}
 	
 	/**
 	 * Tell this RenderArea to display information about
 	 * a selected Colony, if the colony is already shown 
 	 * update the data in the view
 	 * @param selection The selected Colony
 	 */
 	public void showColony(Colony selection) {
 		if (selection.equals(colonyMenu.getColony()) && colonyMenu.isVisible()) {
 			colonyMenu.setColony(selection);
 		} else {
 			hideSideMenus();
 			colonyMenu.setColony(selection);
 			colonyMenu.setVisible(true);
 		}
 	}
 	
 	/**
 	 * Tell this RenderArea to display information about
 	 * a selected Territory
 	 * @param selection The selected Territory
 	 */
 	public void showTerritory(Territory selection) {
 		hideSideMenus();
 		planetMenu.setTerritory(selection);
 		planetMenu.setVisible(true);
 	}
 	
 	/**
 	 * Hide all menus except for the top Menu
 	 */
 	public void hideSideMenus() {
 		colonyMenu.setVisible(false);
 		fleetMenu.setVisible(false);
 		planetMenu.setVisible(false);
 	}
 	
 	/**
 	 * Inner class that handles all mouse interaction with the renderArea
 	 * <p>
 	 * Translates necessary clicks from awt coordinates to openGL coordinates
 	 * @author Alexander Hederstaf
 	 */
 	private class ClickHandler implements MouseListener {
 
 		/**
 		 * The Point currently pressed, null in not currently pressed
 		 */
 		public Point pressedPoint;
 		/*
 		 * Click handling for different parts
 		 */
 		private boolean menuClick(Point point) {
 			boolean clicked = false;
 			clicked = clicked || topMenu.mousePressed(point);
 			clicked = clicked || colonyMenu.mousePressed(point);
 			clicked = clicked || fleetMenu.mousePressed(point);
 			clicked = clicked || planetMenu.mousePressed(point);
 			return clicked;
 		}
 
 		private boolean isLegalPos(Position pos) {
 			boolean rowLegal = pos.getRow() >= 1 && pos.getRow() <= rows;
 			boolean colLegal = pos.getCol() >= 1 && pos.getCol() <= cols;
 			return rowLegal && colLegal;
 		}
 		
 		private Position getPosition(Point point, boolean translated) {
 			int col = 0, row = 0;
 			if (translated) {
 				int rows1 = (rows + EXTRA_SPACE_VERTICAL + 1)*squareSize;
 				row = (rows1 - point.y) / squareSize;
 			} else {
 				int yTrans = translatePixelsY();
 				row = rows - (((screenArea.getHeight() - point.y) + yTrans) / squareSize - EXTRA_SPACE_VERTICAL);
 			}
 			int xTrans = translated ? 0 : translatePixelsX();
 			col = ((point.x  + xTrans) / squareSize) + 1 - EXTRA_SPACE_HORIZONTAL;
 			return new Position(row, col);
 		}
 		
 		private boolean colonizerClick(Point point) {
 			Position pos = getPosition(point, false);
 			if (isLegalPos(pos)) {
 				int dX = (point.x + translatePixelsX()) % squareSize;
 				int dY = ((screenArea.getHeight() - point.y) + translatePixelsY()) % squareSize;
 				if (dX > squareSize/2 && dY <= squareSize/2) {
 					Event evt = new Event(Event.EventTag.COLONIZER_SELECTED, pos);
 					EventBus.CLIENT.publish(evt);
 					return true;
 				}
 			}
 			return false;
 		}
 		
 		private boolean fleetClick(MouseEvent me) {
 			Point point = me.getPoint();
 			Position pos = getPosition(point, false);
 			if (isLegalPos(pos)) {
 				int dX = (point.x + translatePixelsX()) % squareSize;
 				int dY = ((screenArea.getHeight() - point.y) + translatePixelsY()) % squareSize;
 				if (dX <= squareSize/2 && dY <= squareSize/2) {
 					if (me.isShiftDown()) {
 						Event evt = new Event(Event.EventTag.ADD_FLEET_SELECTION, pos);
 						EventBus.CLIENT.publish(evt);
 					} else {
 						Event evt = new Event(Event.EventTag.NEW_FLEET_SELECTION, pos);
 						EventBus.CLIENT.publish(evt);
 					}
 					return true;
 				}
 			}
 			return false;
 		}
 
 		private boolean planetClick(Point point) {
 			Position pos = getPosition(point, false);
 			if (isLegalPos(pos)) {
 				Event evt = new Event(Event.EventTag.PLANET_SELECTED, pos);
 				EventBus.CLIENT.publish(evt);
 				return true;
 			}
 			return false;
 		}
 		
 		private boolean pathClick(Point point) {
 			Position pos = getPosition(point, false);
 			if (isLegalPos(pos)) {
 				Event evt = new Event(Event.EventTag.SET_PATH, pos);
 				EventBus.CLIENT.publish(evt);
 				return true;
 			}
 			return false;
 		}
 		
 		@Override public void mousePressed(MouseEvent me) {
 			if (me.getButton() == MouseEvent.BUTTON1) {
 				pressedPoint = me.getPoint();
 				pressedPoint.x += translatePixelsX();
 				pressedPoint.y = (screenArea.getHeight() - pressedPoint.y) + translatePixelsY();
 			} else if (me.getButton() == MouseEvent.BUTTON3) {
 				if (pathClick(me.getPoint())) {return;}
 			}
 		}
 		@Override public void mouseClicked(MouseEvent me) {
 			/*
 			 * Check each level of interaction in order.
 			 */
 			if (me.getButton() == MouseEvent.BUTTON1) {
 				if (menuClick(me.getPoint())) {return;}
 				if (fleetClick(me)) {return;}
 				if (colonizerClick(me.getPoint())) {return;}
 				if (planetClick(me.getPoint())) {return;}
 				else {
 					/*
 					 * Click was not in any trigger zone. Call deselect.
 					 */
 					EventBus.CLIENT.publish(new Event(Event.EventTag.DESELECT, null));
 					hideSideMenus();
 				}
 			}
 		}
 		@Override public void mouseEntered(MouseEvent me) {}
 		@Override public void mouseExited(MouseEvent me) {}
 		@Override public void mouseReleased(MouseEvent me) {
 			if (me.getButton() == MouseEvent.BUTTON1) {
 				Point releasePoint = me.getPoint();
 				releasePoint.x += translatePixelsX();
 				releasePoint.y = (screenArea.getHeight() - releasePoint.y) + translatePixelsY();
 				if (Math.abs(releasePoint.x - pressedPoint.x) < squareSize/4 || Math.abs(releasePoint.y - pressedPoint.y) < squareSize/4) {
 					pressedPoint = null;
 					return;
 				}
 				
 				Point ltCorner = new Point(Math.min(releasePoint.x, pressedPoint.x), Math.max(releasePoint.y, pressedPoint.y));
 				Point rbCorner = new Point(Math.max(releasePoint.x, pressedPoint.x), Math.min(releasePoint.y, pressedPoint.y));
 				Position ltPos = getPosition(ltCorner, true);
 				Position rbPos = getPosition(rbCorner, true);
 				
 				if (ltCorner.getX() % squareSize >= squareSize/2) {
 					ltPos = new Position(ltPos.getRow(), ltPos.getCol() + 1);
 				}
 				if (ltCorner.getY() % squareSize < squareSize/2) {
 					ltPos = new Position(ltPos.getRow() + 1, ltPos.getCol());
 				}
 				if (rbCorner.getX() % squareSize < squareSize/2) {
 					rbPos = new Position(rbPos.getRow(), rbPos.getCol() - 1);
 				}
 				if (rbCorner.getY() % squareSize >= squareSize/2) {
 					rbPos = new Position(rbPos.getRow() - 1, rbPos.getCol());
 				}
 				
 				List<Position> selectPos = new ArrayList<Position>();
 				for (int col = ltPos.getCol(); col <= rbPos.getCol(); col++) {
 					for (int row = ltPos.getRow(); row <= rbPos.getRow(); row++) {
 						Position pos = new Position(row, col);
 						if (isLegalPos(pos)) {
 							selectPos.add(pos);
 						}
 					}
 				}
 				if (!selectPos.isEmpty()) {
 					Event evt = new Event(Event.EventTag.NEW_FLEET_SELECTION, selectPos);
 					EventBus.CLIENT.publish(evt);
 				} else {
 					Event evt = new Event(Event.EventTag.DESELECT, null);
 					EventBus.CLIENT.publish(evt);
 					hideSideMenus();
 				}
 				pressedPoint = null;
 			}
 		}
 	}
 }
