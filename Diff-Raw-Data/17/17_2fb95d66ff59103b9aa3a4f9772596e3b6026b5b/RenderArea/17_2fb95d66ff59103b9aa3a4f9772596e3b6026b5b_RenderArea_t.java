 package riskyspace.view.swingImpl;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.Timer;
 
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
 import riskyspace.services.EventHandler;
 import riskyspace.services.EventText;
 import riskyspace.view.Button;
 import riskyspace.view.Clickable;
 import riskyspace.view.SpriteMap;
 import riskyspace.view.ViewResources;
 import riskyspace.view.camera.Camera;
 import riskyspace.view.camera.CameraController;
 import riskyspace.view.menu.swingImpl.ColonyMenu;
 import riskyspace.view.menu.swingImpl.FleetMenu;
 import riskyspace.view.menu.swingImpl.PlanetMenu;
 import riskyspace.view.menu.swingImpl.TopMenu;
 
 public class RenderArea extends Canvas {
 
 	private static final long serialVersionUID = 8209691542499926289L;
 	
 	/**
 	 * Extra space at the Right and Left sides
 	 * of the screen.
 	 */
 	private static final int EXTRA_SPACE_HORIZONTAL = 3;
 	
 	/**
 	 * Extra space at the Top and Bottom sides
 	 * of the screen.
 	 */
 	private static final int EXTRA_SPACE_VERTICAL = 2;
 	
 	/*
 	 * Cameras
 	 */
 	private Camera currentCamera = null;
 	private Map<Player, Camera> cameras = null;
 	private CameraController cc = null;
 	
 	/*
 	 * Menu settings
 	 */
 	private ColonyMenu colonyMenu = null;
 	private FleetMenu fleetMenu = null;
 	private PlanetMenu planetMenu = null;
 	private TopMenu topMenu = null;
 	
 	/*
 	 * Screen measures
 	 */
 	private int width;
 	private int height;
 	private int squareSize;
 	
 	/*
 	 * BufferedImage background
 	 */
 	private BufferedImage background = null;
 	
 	/*
 	 * SpriteMap
 	 */
 	private SpriteMap spriteMap = null;
 	
 	private ClickHandler clickHandler = null;
 	private EventTextPrinter eventTextPrinter = null;
 	
 	private int rows, cols;
 	
 	/*
 	 * FPS printout
 	 */
 	private int times = 0;
 	private Timer fpsTimer = null;
 	private String fps = "";
 	private Font fpsFont = null;
 
 	/*
 	 * The player viewing this renderArea right now
 	 */
 	private Player viewer = null;
 
 	/*
 	 * The Player currently playing
 	 */
 	private Button otherPlayerActive = null;
 
 	public RenderArea(int rows, int cols) {
 		this.rows = rows;
 		this.cols = cols;
 		measureScreen();
 		createBackground();
 		initCameras();
 		createMenues();
 		fpsTimer = new Timer(500, new ActionListener() {
 			@Override public void actionPerformed(ActionEvent arg0) {
 				fps = "FPS: " + (2*times);
 				times = 0;
 			}		
 		});
 		eventTextPrinter = new EventTextPrinter();
 		clickHandler = new ClickHandler();
 		fpsFont = ViewResources.getFont().deriveFont(17f);
 		otherPlayerActive = new Button(width/2 - width/10, height/2 - height/10, width/5, height/10);
 		otherPlayerActive.setImage("res/menu/wide_button.png");
 		otherPlayerActive.setEnabled(false);
 		addMouseListener(clickHandler);
 	}
 
 	private void createMenues() {
 		int menuWidth = height / 3;
 		colonyMenu = new ColonyMenu(width - menuWidth, 80, menuWidth, height-80);
 		fleetMenu = new FleetMenu(width - menuWidth, 80, menuWidth, height-80);
 		planetMenu = new PlanetMenu(width - menuWidth, 80, menuWidth, height-80);
 		topMenu = new TopMenu(0, 0, width, 80);
 	}
 	
 	private void createBackground() {
 		int totalWidth = (cols + 2*EXTRA_SPACE_HORIZONTAL)*squareSize;
 		int totalHeight = (rows + 2*EXTRA_SPACE_VERTICAL)*squareSize;
 		background = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2D = background.createGraphics();
 		
 		g2D.setColor(Color.BLACK);
 		g2D.fillRect(0, 0, totalWidth, totalHeight);
 		
 		g2D.setColor(Color.LIGHT_GRAY);
 		/*
 		 * Draw Horizontal lines
 		 */
 		for (int row = 0; row <= rows; row++) {
 			int x1 = (EXTRA_SPACE_HORIZONTAL) * squareSize;
 			int x2 = (EXTRA_SPACE_HORIZONTAL + cols) * squareSize;
 			int y = (EXTRA_SPACE_VERTICAL + row) * squareSize;
 			g2D.drawLine(x1, y, x2, y);
 		}
 		/*
 		 * Draw Vertical lines
 		 */
 		for (int col = 0; col <= rows; col++) {
 			int x = (EXTRA_SPACE_HORIZONTAL + col) * squareSize;
 			int y1 = (EXTRA_SPACE_VERTICAL) * squareSize;
 			int y2 = (EXTRA_SPACE_VERTICAL + rows) * squareSize;
 			g2D.drawLine(x, y1, x, y2);
 		}
 		
 		/*
 		 * Draw stars
 		 */
 		g2D.setColor(Color.WHITE);
 		for (int i = 0; i < 5000; i++) {
 			int x = (int) (Math.random()*(cols+2*EXTRA_SPACE_HORIZONTAL)*squareSize);
 			int y = (int) (Math.random()*(rows+2*EXTRA_SPACE_VERTICAL)*squareSize);
 			g2D.fillRect(x, y, 1, 1);
 		}
 	}
 	
 	private void initCameras() {
 		cameras = new HashMap<Player, Camera>();
 		cameras.put(Player.BLUE, new Camera(0.93f,0.92f));
 		cameras.put(Player.RED, new Camera(0.07f,0.08f));
 		cameras.put(Player.GREEN, new Camera(0.07f,0.92f));
 		cameras.put(Player.PINK, new Camera(0.93f,0.08f));
 		cc = new CameraController();
 	}
 	
 	/*
 	 * Measure the screen and set the squareSize
 	 */
 	public void measureScreen() {
 		width = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
 		height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
 		squareSize = Math.min(width/6,height/6);
 	}
 	
 	public void setViewer(Player player) {
 		this.viewer = player;
 		currentCamera = cameras.get(player);
 		cc.setCamera(currentCamera);
 		if (!cc.isAlive()) {
 			cc.start();
 		}
 	}
 	
 	public void setActivePlayer(Player activePlayer) {
 		String player = activePlayer.toString().substring(0, 1)+ activePlayer.toString().substring(1, activePlayer.toString().length());
 		otherPlayerActive.setText(player + "'s turn");
 		otherPlayerActive.setTextColor(PlayerColors.getColor(activePlayer));
 		otherPlayerActive.setEnabled(viewer != activePlayer);
 	}
 	
 	public int translatePixelsX() {
 		return (int) (((cols+2*EXTRA_SPACE_HORIZONTAL)*squareSize - width)*currentCamera.getX());
 	}
 	
 	public int translatePixelsY() {
 		return (int) (((rows+2*EXTRA_SPACE_VERTICAL)*squareSize - height)*currentCamera.getY());
 	}
 
 
 	public void paint(Graphics g) {
 //		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 //		Graphics graphics = bi.createGraphics();
 		createBufferStrategy(2);
 		BufferStrategy bs = getBufferStrategy();
 		Graphics graphics = bs.getDrawGraphics();
 		
 		if (!fpsTimer.isRunning()) {
 			fpsTimer.start();
 		}
 		times++;
 		/*
 		 * Translate with cameras
 		 */
 		int xTrans = -translatePixelsX();
 		int yTrans = -translatePixelsY();
 		graphics.translate(xTrans, yTrans);
 		
 		// Draw background
 		graphics.drawImage(background, 0, 0, null);
 		
 		spriteMap.draw(graphics, squareSize, EXTRA_SPACE_HORIZONTAL, EXTRA_SPACE_VERTICAL);
 		
 		// Draw texts
 		eventTextPrinter.drawEventText(graphics);
 		
 		drawSelectionArea(graphics, xTrans, yTrans);
 		
 		// Draw menu
 		graphics.translate(-xTrans, -yTrans);
 		
 		colonyMenu.draw(graphics);
 		fleetMenu.draw(graphics);
 		planetMenu.draw(graphics);
 		topMenu.draw(graphics);
 		
 		otherPlayerActive.draw(graphics);
 		
 		graphics.setColor(ViewResources.WHITE);
 		graphics.setFont(fpsFont);
 		graphics.drawString(fps, 10, 110);
 		
 		graphics.dispose();
 		
 		bs.show();
 	}
 	
 	private void drawSelectionArea(Graphics g, int xTrans, int yTrans) {
 		g.setColor(Color.GREEN);
 		if (clickHandler.pressedPoint != null) {
 			Point p = MouseInfo.getPointerInfo().getLocation();
 			int x = clickHandler.pressedPoint.x;
 			int y = clickHandler.pressedPoint.y;
 			int otherX = p.x - xTrans;
 			int otherY = p.y - yTrans;
 			int width = Math.abs(x - otherX);
 			int height = Math.abs(y - otherY);
 			x = x > otherX ? otherX : x;
 			y = y > otherY ? otherY : y;
 			g.drawRect(x, y, width, height);
 			g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g.getColor().getBlue(), 0x3F));
 			g.fillRect(x + 1, y + 1, width, height);
 		}
 	}
 	 
 	public Position getPosition(Point point, boolean translated) {
 		int xTrans = translated ? 0 : translatePixelsX();
 		int yTrans = translated ? 0 : translatePixelsY();
 		int row = ((point.y + yTrans) / squareSize) + 1 - EXTRA_SPACE_VERTICAL;
 		int col = ((point.x  + xTrans) / squareSize) + 1 - EXTRA_SPACE_HORIZONTAL;
 		return new Position(row, col); 
 	}
 	
 	private boolean isLegalPos(Position pos) {
 		boolean rowLegal = pos.getRow() >= 1 && pos.getRow() <= rows;
 		boolean colLegal = pos.getCol() >= 1 && pos.getCol() <= cols;
 		return rowLegal && colLegal;
 	}
 	
 	public void updateData(SpriteMapData data) {
 		spriteMap = SpriteMap.getSprites(data, squareSize);
 	}
 
 	public void setStats(PlayerStats stats) {
 		topMenu.setStats(stats);
 		colonyMenu.setStats(stats);
 	}
 	
 	public void setQueue(Map<Colony, List<BuildAble>> colonyQueues) {
 		colonyMenu.setQueues(colonyQueues);
 		/*
 		 * Set for BuildQueueMenu
 		 */
 	}
 	
 	public void showTerritory(Territory selection) {
 		hideSideMenus();
 		planetMenu.setTerritory(selection);
 		planetMenu.setVisible(true);
 	}
 
 	public void showColony(Colony selection) {
 		if (selection.equals(colonyMenu.getColony()) && colonyMenu.isVisible()) {
 			// Update colony
 			colonyMenu.setColony(selection);
 		} else {
 			hideSideMenus();
 			colonyMenu.setColony(selection);
 			colonyMenu.setVisible(true);
 		}
 	}
 
 	public void showFleet(Fleet selection) {
 		hideSideMenus();
 		fleetMenu.setFleet(selection);
 		fleetMenu.setVisible(true);
 	}
 	
 	public void hideSideMenus() {
 		colonyMenu.setVisible(false);
 		fleetMenu.setVisible(false);
 		planetMenu.setVisible(false);
 	}
 	
 	private class ClickHandler implements MouseListener {
 
 		public Point pressedPoint;
 		/*
 		 * Click handling for different parts
 		 */
 		public boolean menuClick(Point point) {
 			boolean clicked = false;
 			if (topMenu instanceof Clickable) {
 				clicked = clicked || ((Clickable) topMenu).mousePressed(point);
 			}
 			if (colonyMenu instanceof Clickable) {
 				clicked = clicked || ((Clickable) colonyMenu).mousePressed(point);
 			}
 			if (fleetMenu instanceof Clickable) {
 				clicked = clicked || ((Clickable) fleetMenu).mousePressed(point);
 			}
 			if (planetMenu instanceof Clickable) {
 				clicked = clicked || ((Clickable) planetMenu).mousePressed(point);
 			}
 			return clicked;
 		}
 
 		public boolean colonizerClick(Point point) {
 			Position pos = getPosition(point, false);
 			if (isLegalPos(pos)) {
 				int dX = (point.x + translatePixelsX()) % squareSize;
 				int dY = (point.y + translatePixelsY()) % squareSize;
 				if (dX > squareSize/2 && dY > squareSize/2) {
 					Event evt = new Event(Event.EventTag.COLONIZER_SELECTED, pos);
 					EventBus.CLIENT.publish(evt);
 					return true;
 				}
 			}
 			return false;
 		}
 		
 		public boolean fleetClick(MouseEvent me) {
 			Point point = me.getPoint();
 			Position pos = getPosition(point, false);
 			if (isLegalPos(pos)) {
 				int dX = (point.x + translatePixelsX()) % squareSize;
 				int dY = (point.y + translatePixelsY()) % squareSize;
 				if (dX <= squareSize/2 && dY >= squareSize/2) {
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
 
 		public boolean planetClick(Point point) {
 			Position pos = getPosition(point, false);
 			if (isLegalPos(pos)) {
 				Event evt = new Event(Event.EventTag.PLANET_SELECTED, pos);
 				EventBus.CLIENT.publish(evt);
 				return true;
 			}
 			return false;
 		}
 		
 		public boolean pathClick(Point point) {
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
 				pressedPoint.y += translatePixelsY();
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
 				releasePoint.y += translatePixelsY();
 				if (Math.abs(releasePoint.x - pressedPoint.x) < squareSize/10 || Math.abs(releasePoint.y - pressedPoint.y) < squareSize/10) {
 					pressedPoint = null;
 					return;
 				}
 				
 				Point ltCorner = new Point(Math.min(releasePoint.x, pressedPoint.x), Math.min(releasePoint.y, pressedPoint.y));
 				Point rbCorner = new Point(Math.max(releasePoint.x, pressedPoint.x), Math.max(releasePoint.y, pressedPoint.y));
 				Position ltPos = getPosition(ltCorner, true);
 				Position rbPos = getPosition(rbCorner, true);
 				if (ltCorner.getX() % squareSize >= squareSize/2) {
 					ltPos = new Position(ltPos.getRow(), ltPos.getCol() + 1);
 				}
 				if (ltCorner.getY() % squareSize >= squareSize/2) {
 					ltPos = new Position(ltPos.getRow() + 1, ltPos.getCol());
 				}
 				if (rbCorner.getX() % squareSize < squareSize/2) {
 					rbPos = new Position(rbPos.getRow(), rbPos.getCol() - 1);
 				}
 				if (rbCorner.getY() % squareSize < squareSize/2) {
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
 	
 	private class EventTextPrinter implements EventHandler {
 		
 		final List<EventText> texts = new ArrayList<EventText>();
 		
 		public EventTextPrinter() {
 			/*
 			 * TODO:
 			 * What Bus should be used if any?
 			 */
 		}
 		
 		@Override
 		public void performEvent(Event evt) {
 			if (evt.getTag() == Event.EventTag.EVENT_TEXT) {
 				EventText et = (EventText) evt.getObjectValue();
 				texts.add(et);
 			}
 		}
 		
 		public void drawEventText(Graphics g) {
 			g.setColor(Color.GREEN);
 			g.setFont(new Font("Arial", Font.PLAIN, 12));
 			List<EventText> done = new ArrayList<EventText>();
 			for (EventText eventText : texts) {
 				Position pos = eventText.getPos();
 				int x = (int) ((pos.getCol()+EXTRA_SPACE_HORIZONTAL-0.5)*squareSize)-g.getFontMetrics().stringWidth(eventText.getText())/2;
 				int y = (int) ((pos.getRow()+EXTRA_SPACE_VERTICAL-0.5)*squareSize-eventText.getTimes()/12-g.getFontMetrics().getHeight()/2);
 				g.drawString(eventText.getText(), x, y);
 				eventText.incTimes();
 				if (eventText.getTimes() == 120) {
 					done.add(eventText);
 				}
 			}
 			texts.removeAll(done);
 		}
 	}
 }
