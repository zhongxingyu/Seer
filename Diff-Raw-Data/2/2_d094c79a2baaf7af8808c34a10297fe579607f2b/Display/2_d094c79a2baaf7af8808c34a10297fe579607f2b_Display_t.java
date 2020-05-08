 package UI;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Stroke;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 import sound.AudioPlayer;
 import state.Dude;
 
 import state.StructureType;
 
 import state.Resource;
 import state.Structure;
 
 import state.Tile;
 import state.World;
 import util.UIImageStorage;
 
 public class Display extends JPanel {
 
 	// FIELDS
 	private final Dimension DIMENSION = new Dimension(1920, 1080);
 	private final int VIEW_WIDTH = 70, VIEW_HEIGHT = 70; // Camera = 60x60
 
 	private final int SCREEN_Y_DISPLACEMENT = 490; // Arbitrary y axis
 													// displacement
 	private final int SCREEN_BUFFER_ZONE = 20; // Arbitrary screen edge buffer
 
 	private World world;
 
 	private boolean tileHighLighted = false;
 
 	private boolean trippy = false;
 
 	// <UI
 	int miniMapWidth = 280;
 	int miniMapHeight = 280;
 	int padding = 10;
 
 	int toggleSize = 74;
 	int toggleImageSize = 64;
 	int tpad = (75 - 64) / 2;
 
 	private boolean buildStruct = false;
 
 	public void toggleStruct() { buildStruct = !buildStruct; }
 
 	Map<String, Rectangle> toggleButtons = null;
 	Map<String, MouseListener> toggleButtonsListener = null;
 	Map<String, String> toggleButtonsImages = null;
 	HashSet<Rectangle> UISpace = null;
 	Map<String, Rectangle> resourceSelect = null;
 	Map<String, Rectangle> structureSelect = null;
 
 
 	Rectangle resourceSelectRect = null;
 	// UI/>
 
 	// pixel size of each tile
 	private final int TILE_WIDTH = 64, TILE_HEIGHT = 32;
 
 	private int rotation = 0;
 	private Coord camera = new Coord(0, 0); // ARBITRARY START POINT
 
 	// Camera stores coord of topmost tile
 
 	// CONSTRUCTOR
 	public Display(World world) {
 		super();
 		setPreferredSize(DIMENSION); // Necessary?
 		this.world = world;
 	}
 
 	public Set<java.awt.Rectangle> getUISpace() {
 		UISpace = new HashSet<Rectangle>();
 		UISpace.add(new Rectangle(this.getWidth() - padding - miniMapWidth
 				- toggleSize, padding, miniMapWidth + toggleSize, miniMapHeight));
 		UISpace.add(new Rectangle(padding, padding, 150, 64 * 3 + 5 * 3 + 10
 				+ 10));
 		if (resourceSelectRect != null) {
 			UISpace.add(resourceSelectRect);
 		}
 		return UISpace;
 	}
 
 	public Map<String, Rectangle> getToggleMap() {
 		Rectangle toggleHealth = new Rectangle(this.getWidth() - miniMapWidth
 				- toggleSize - padding + tpad, padding + tpad, toggleSize,
 				toggleSize);
 		Rectangle newDudeToggle = new Rectangle(toggleHealth.x, toggleHealth.y
 				+ toggleSize - tpad, toggleSize, toggleSize);
 		Rectangle slugBalancingToggle = new Rectangle(newDudeToggle.x,
 				newDudeToggle.y + toggleSize - tpad, toggleSize, toggleSize);
 		Rectangle tripToggle = new Rectangle(slugBalancingToggle.x,
 				slugBalancingToggle.y + toggleSize - tpad, toggleSize,
 				toggleSize);
 
 		if (toggleButtons == null) {
 			toggleButtons = new HashMap<String, Rectangle>();
 			toggleButtonsListener = new HashMap<String, MouseListener>();
 			toggleButtonsImages = new HashMap<String, String>();
 
 			MouseListener listener = new MouseListener() {
 				@Override
 				public void mouseReleased(MouseEvent e) {
 				}
 
 				@Override
 				public void mousePressed(MouseEvent e) {
 				}
 
 				@Override
 				public void mouseExited(MouseEvent e) {
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent e) {
 				}
 
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					world.toggleShowHealth();
 					if (world.showHealth())
 						toggleButtonsImages.put("ButtonHealth",
 								"ButtonHealthOn");
 					else
 						toggleButtonsImages.put("ButtonHealth",
 								"ButtonHealthOff");
 				}
 			};
 
 			toggleButtonsListener.put("ButtonHealth", listener);
 			toggleButtonsImages.put("ButtonHealth", "ButtonHealthOn");
 
 			listener = new MouseListener() {
 				@Override
 				public void mouseReleased(MouseEvent e) {
 				}
 
 				@Override
 				public void mousePressed(MouseEvent e) {
 				}
 
 				@Override
 				public void mouseExited(MouseEvent e) {
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent e) {
 				}
 
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					world.toggleDudeSpawning();
 					if (world.isDudeSpawningEnabled())
 						toggleButtonsImages.put("ButtonAddDude",
 								"ButtonAddDudeHover");
 					else
 						toggleButtonsImages.put("ButtonAddDude",
 								"ButtonAddDude");
 				}
 			};
 
 			toggleButtonsListener.put("ButtonAddDude", listener);
 			toggleButtonsImages.put("ButtonAddDude", "ButtonAddDudeHover");
 
 			listener = new MouseListener() {
 
 				@Override
 				public void mouseReleased(MouseEvent e) {
 				}
 
 				@Override
 				public void mousePressed(MouseEvent e) {
 				}
 
 				@Override
 				public void mouseExited(MouseEvent e) {
 
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent e) {
 				}
 
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					if (toggleButtonsImages.get("ButtonMute").equals(
 							"ButtonMuteOff")) {
 						// Mute here
 						if (world.getAudioPlayer() != null) {
 						//	world.getAudioPlayer().toggleMute();
 							world.getAudioPlayer().toggleMusic();
 						}
 						toggleButtonsImages.put("ButtonMute", "ButtonMuteOn");
 					} else {
 						// Unmute here
 
 						if (world.getAudioPlayer() != null) {
 						//	world.getAudioPlayer().toggleMute();
 						  world.getAudioPlayer().toggleMusic();
 						}
 						toggleButtonsImages.put("ButtonMute", "ButtonMuteOff");
 					}
 				}
 			};
 
 			toggleButtonsListener.put("ButtonMute", listener);
 			toggleButtonsImages.put("ButtonMute", "ButtonMuteOff");
 
 			listener = new MouseListener() {
 
 				@Override
 				public void mouseReleased(MouseEvent e) {
 				}
 
 				@Override
 				public void mousePressed(MouseEvent e) {
 				}
 
 				@Override
 				public void mouseExited(MouseEvent e) {
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent e) {
 				}
 
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					if (toggleButtonsImages.get("ButtonBG").equals(
 							"ButtonBGOff")) {
 						trippy = true;
 						toggleButtonsImages.put("ButtonBG", "ButtonBGOn");
 					} else {
 						trippy = false;
 						toggleButtonsImages.put("ButtonBG", "ButtonBGOff");
 					}
 				}
 			};
 
 			toggleButtonsListener.put("ButtonBG", listener);
 			toggleButtonsImages.put("ButtonBG", "ButtonBGOff");
 
 			listener = new MouseListener() {
 
 				@Override
 
 				public void mouseReleased(MouseEvent e) {}
 
 				@Override
 				public void mousePressed(MouseEvent e) {}
 
 				@Override
 				public void mouseExited(MouseEvent e) {}
 
 				@Override
 				public void mouseEntered(MouseEvent e) {}
 
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					Point p = e.getPoint();
 					if (buildStruct) {
 
 					} else {
 						if (resourceSelect != null) {
 							for (String key : resourceSelect.keySet()) {
 								if (resourceSelect.get(key).contains(p)) {
 									world.setCurrentBuild(key);
 								}
 							}
 						}
 					}
 				}
 			};
 			toggleButtonsListener.put("selectTile", listener);
 		}
 		toggleButtons.put("ButtonAddDude", newDudeToggle);
 		toggleButtons.put("ButtonMute", slugBalancingToggle);
 		toggleButtons.put("ButtonHealth", toggleHealth);
 		toggleButtons.put("ButtonBG", tripToggle);
 
 		toggleButtons.put("selectTile", resourceSelectRect);
 
 		return toggleButtons;
 	}
 
 	public MouseListener buttonClicked(String key) {
 		return toggleButtonsListener.get(key);
 	}
 
 	private static final long serialVersionUID = 8274011568777903027L;
 
 	// WHAT DOES THIS EVEN DO??
 
 	public int[] getCameraCoordinates() {
 		return new int[] { camera.x, camera.y };
 	}
 
 	// public void setCameraCoordinates(int[] coord){
 	// camera = new Coord(coord[0],coord[1]);
 	// }
 
 	public World getWorld() {
 		return world;
 	}
 
 	public void panLeft(int idx) {
 		camera = new Coord(camera.x - idx, camera.y + idx);
 	}
 
 	public void panRight(int idx) {
 		camera = new Coord(camera.x + idx, camera.y - idx);
 	}
 
 	public void panDown(int idy) {
 		camera = new Coord(camera.x + idy, camera.y + idy);
 	}
 
 	public void panUp(int idy) {
 		camera = new Coord(camera.x - idy, camera.y - idy);
 	}
 
 	// RENDERING
 	public void paintComponent(Graphics g) {
 		g.setColor(Color.PINK);
 		g.fillRect(0, 0, this.getWidth(), this.getHeight());
 		paintMap(g);
 		drawHUD(g);
 	}
 
 	private Tile getCameraRelativeTile(int x, int y) {
 		int temp;
 		switch (rotation) {
 		case 0:
 			break;
 		case 1:
 			temp = VIEW_WIDTH - x;
 			x = y;
 			y = temp;
 			break;
 		case 2:
 			x = VIEW_WIDTH - x;
 			y = VIEW_HEIGHT - y;
 			break;
 		case 3:
 			temp = VIEW_HEIGHT - y;
 			y = x;
 			x = temp;
 			break;
 		}
 		return world.getTile(x + camera.x, y + camera.y);
 	}
 
 	public Point tileToDisplayCoordinates(double x, double y) {
 		x -= camera.x;
 		y -= camera.y;
 
 		double temp;
 		switch (rotation) {
 		case 0:
 			break;
 		case 3:
 			temp = VIEW_WIDTH - x;
 			x = y;
 			y = temp;
 			break;
 		case 2:
 			x = VIEW_WIDTH - x;
 			y = VIEW_HEIGHT - y;
 			break;
 		case 1:
 			temp = VIEW_HEIGHT - y;
 			y = x;
 			x = temp;
 			break;
 		}
 
 		return new Point(getPixelX(x, y), getPixelY(x, y));
 	}
 
 	public Point displayToTileCoordinates(int x, int y) {
 		/*
 		 * x -= camera.x; y -= camera.y;
 		 *
 		 *
 		 *
 		 * return new Point(getPixelX(x, y), getPixelY(x, y));
 		 */
 
 		double xMinusY = (x - getWidth() / 2) / (TILE_WIDTH / 2.0); // ( x click
 																	// - half
 																	// width of
 																	// screen )
 																	// / half
 																	// the width
 																	// of a tile
 		double xPlusY = ((y + SCREEN_Y_DISPLACEMENT) / (TILE_HEIGHT / 2.0)); // (
 																				// y
 																				// click
 																				// /
 																				// half
 																				// height
 																				// of
 																				// tile
 																				// )
 
 		// finds integer value of square in array position and adjusts for
 		// where the camera is looking
 		int tileX = (int) ((xPlusY + xMinusY) / 2);
 		int tileY = (int) ((xPlusY - xMinusY) / 2);
 
 		int temp;
 		switch (rotation) {
 		case 0:
 			break;
 		case 1:
 			temp = VIEW_WIDTH - tileX;
 			tileX = tileY;
 			tileY = temp;
 			break;
 		case 2:
 			tileX = VIEW_WIDTH - tileX;
 			tileY = VIEW_HEIGHT - tileY;
 			break;
 		case 3:
 			temp = VIEW_HEIGHT - tileY;
 			tileY = tileX;
 			tileX = temp;
 			break;
 		}
 
 		tileX += camera.x;
 		tileY += camera.y;
 
 		return new Point(tileX, tileY);
 	}
 
 	private int getPixelX(double x, double y) {
 		return (int) ((this.getWidth() / 2) + (x - y) * (TILE_WIDTH / 2));
 	}
 
 	private int getPixelY(double x, double y) {
 		return (int) ((x + y) * (TILE_HEIGHT / 2) - SCREEN_Y_DISPLACEMENT + TILE_HEIGHT);
 	}
 
 	/**
 	 * Paints the "view" on-screen at any one time. The algorithm goes through,
 	 * drawing the tiles from the top down, and draws them on the graphics pane.
 	 */
 	private void paintMap(Graphics g) {
 
 		if (trippy) {
 			Color trippingColor = new Color((int) (Math.random() * Math.pow(2,
 					24)));
 			g.setColor(trippingColor);
 			g.fillRect(0, 0, this.getWidth(), this.getHeight());
 		}
 
 		for (int x = 0; x < VIEW_WIDTH; x++) {
 			for (int y = 0; y < VIEW_HEIGHT; y++) {
 				Tile t = getCameraRelativeTile(x, y);
 				if (t != null) {
 					// System.out.println("CAMERA: " + camera.x + " " + camera.y
 					// +".");
 
 					// minimum depth to render to
 					int minDepth;
 					Tile t1 = getCameraRelativeTile(x + 1, y);
 					Tile t2 = getCameraRelativeTile(x, y + 1);
 					int t1Depth = (t1 == null ? -10 : t1.getHeight());
 					int t2Depth = (t2 == null ? -10 : t2.getHeight());
 					minDepth = Math.min(t1Depth, t2Depth);
 
 					if (minDepth < t.getHeight())
 						minDepth++;
 
 					for (int σ = minDepth; σ <= t.getHeight(); σ++) {
 						// Translated tile coordinates to account for raised
 						// elevations (i,j)
 						int i = x - σ;
 						int j = y - σ;
 						// displays each tile
 						g.drawImage(t.getImage(), getPixelX(i, j)
 								- (TILE_WIDTH / 2), getPixelY(i, j)
 								- TILE_HEIGHT, TILE_WIDTH, t.getImage()
 								.getHeight(null), null);
 						if (tileHighLighted && t.getX() == hoverX
 								&& t.getY() == hoverY)
 							g.drawImage(hoverImage, getPixelX(i, j)
 									- (TILE_WIDTH / 2), getPixelY(i, j)
 									- TILE_HEIGHT, TILE_WIDTH, 32, null);
 					}
 
 					int bottomPixelX = getPixelX(x - t.getHeight(),
 							y - t.getHeight());
 					int bottomPixelY = getPixelY(x - t.getHeight(),
 							y - t.getHeight());
 
 					if (t.getStructure() != null) { // If there is a structure
 													// in the tile --> DRAW
 													// HE/SHE/IT!
 
 						t.getStructure().draw(g, this, bottomPixelX,
 								bottomPixelY);
 
 					}
 
 					Dude dude = t.getDude();
 					if (dude != null) { // If there is a dude in the tile
 						dude.draw(g, this, bottomPixelX, bottomPixelY,
 								world.showHealth());
 
 					}
 				}
 			}
 		}
 		if (trippy) {
 			Color trippingColor = new Color((int) (Math.random() * Math.pow(2,
 					32)), true);
 			g.setColor(trippingColor);
 			g.fillRect(0, 0, this.getWidth(), this.getHeight());
 		}
 	}
 
 	/**
 	 * Displays the HUD on the main game window
 	 *
 	 * @param g
 	 *            Display graphics object
 	 */
 	private void drawHUD(Graphics g) {
 		// draw the Minimap
 
 		Graphics2D g2d = (Graphics2D) g;
 		g2d.setColor(Color.WHITE);
 		g2d.fillRect(this.getWidth() - miniMapWidth - padding, padding,
 				miniMapWidth, miniMapHeight);
 
 		BufferedImage miniMap = new BufferedImage(miniMapWidth, miniMapHeight,
 				BufferedImage.TYPE_INT_RGB);
 
 		for (int x = 0; x < miniMapWidth; x++) {
 			for (int y = 0; y < miniMapHeight; y++) {
 				Tile t = world.getTile(x + camera.x, y + camera.y);
 				if (t != null) {
 					miniMap.setRGB(x, y, t.getColor().getRGB());
 
 					Structure struc = t.getStructure();
 					if (struc != null) {
 						miniMap.setRGB(x, y, (struc instanceof Resource ? Color.blue : Color.cyan).getRGB());
 					}
 
 					Dude dude = t.getDude();
 					if (dude != null) {
 						miniMap.setRGB(x, y, Color.yellow.getRGB());
 					}
 				}
 			}
 		}
 		g2d.drawImage(miniMap, this.getWidth() - miniMapWidth - padding,
 				padding, null);
 
 		// draw the button panel
 		g2d.setColor(Color.black);
 		g2d.fillRect(this.getWidth() - miniMapWidth - toggleSize - padding,
 				padding, toggleSize, miniMapHeight);
 
 		/*
 		 * int buttonx = this.getWidth() - 235; g2d.setColor(Color.red);
 		 * g2d.fillRect(buttonx, 5, 55, 55);
 		 */// TODO upto here
 			// draw the object selecter
 
 		// border minimap and buttons
 		g2d.setColor(new Color(212, 175, 55));
 		Stroke orig = g2d.getStroke();
 		g2d.setStroke(new BasicStroke(3));
 		int r = 5;
 		g2d.drawRoundRect(
 				this.getWidth() - miniMapWidth - toggleSize - padding, padding,
 				miniMapWidth + toggleSize, miniMapHeight, r, r);
 		g2d.drawLine(this.getWidth() - miniMapWidth - padding, padding,
 				this.getWidth() - miniMapWidth - padding, miniMapHeight
 						+ padding);
 		g2d.setStroke(orig);
 
 		getToggleMap();
 		for (String key : toggleButtons.keySet()) {
 			if (key.equals("selectTile"))
 				continue;
 			g2d.drawImage(UIImageStorage.get(toggleButtonsImages.get(key)),
 					toggleButtons.get(key).x, toggleButtons.get(key).y, null);
 		}
 
 		g2d.setColor(Color.black);
 		g2d.setStroke(new BasicStroke(3));
 		g2d.fillRect(padding, padding, TILE_WIDTH * 2 + tpad * 3, 64 * 3 + 5
 				* 3 + 10 + 10);
 		g2d.setColor(new Color(212, 175, 55));
 		g2d.drawRoundRect(padding, padding, TILE_WIDTH * 2 + tpad * 3, 64 * 3
 				+ 5 * 3 + 10 + 10, r, r);
 
 		for (int i = 0; i < 3; i++) {
 
 			g2d.drawImage(UIImageStorage.get("IconCrystal"), padding + 10,
 					padding + 10, null);
 			g2d.drawImage(UIImageStorage.get("IconPlants"), padding + 10,
 					padding + 10 + 64 + 5, null);
 			g2d.drawImage(UIImageStorage.get("IconWood"), padding + 10, padding
 					+ 10 + 64 + 64 + 5 + 5, null);
 
 			g2d.drawString("" + world.getCrystalResource(),
 					padding + 64 + 10 + 5, padding + 20);
 			g2d.drawString("" + world.getPlantResource(),
 					padding + 64 + 10 + 5, padding + 20 + 64 + 5);
 			g2d.drawString("" + world.getWoodResource(), padding + 64 + 10 + 5,
 					padding + 20 + 64 + 5 + 64 + 5);
 
 		}
 
 		int x = 0;
 		int y = 0;
 
 		int start = 64 * 3 + 5 * 3 + 10 + 10 + padding;
 
 
 
 		if (buildStruct) {
 			Map<String, StructureType> structureMap = StructureType.getTypes();
 
 			int selectHeight = (structureMap.size() + 1) / 2;
 			resourceSelectRect = new Rectangle(padding, padding + start, 2
 					* (tpad + TILE_WIDTH) + tpad, selectHeight
 					* (tpad + TILE_HEIGHT * 2) + tpad);
 
 			resourceSelect = new HashMap<String, Rectangle>();
 
 			g2d.setColor(Color.gray);
 			g2d.fill(resourceSelectRect);
 
 			for (String key : structureMap.keySet()) {
 				Image image = structureMap.get(key).getImage();
 
 				Rectangle rect = new Rectangle(tpad + resourceSelectRect.x + x
 						* (TILE_WIDTH + tpad), tpad + resourceSelectRect.y + y
 						* (TILE_HEIGHT * 2 + tpad), TILE_WIDTH,
						TILE_HEIGHT * 2);
 				g2d.drawImage(image, rect.x, rect.y, rect.width, rect.height, null);
 
 				resourceSelect.put(key, rect);
 
 				x++;
 				y += x / 2;
 				x %= 2;
 			}
 		} else {
 			Map<String, BufferedImage> tileMap = Tile.getImagesCache().getMap();
 
 			int selectHeight = (tileMap.size() + 1) / 2;
 			resourceSelectRect = new Rectangle(padding, padding + start, 2
 					* (tpad + TILE_WIDTH) + tpad, selectHeight
 					* (tpad + TILE_HEIGHT * 2) + tpad);
 
 			g2d.setColor(Color.gray);
 			g2d.fill(resourceSelectRect);
 
 			resourceSelect = new HashMap<String, Rectangle>();
 
 			for (String key : tileMap.keySet()) {
 				BufferedImage image = tileMap.get(key);
 				Rectangle rect = new Rectangle(tpad + resourceSelectRect.x + x
 						* (TILE_WIDTH + tpad), tpad + resourceSelectRect.y + y
 						* (TILE_HEIGHT * 2 + tpad), image.getWidth(),
 						image.getHeight());
 
 				g2d.drawImage(image, rect.x, rect.y, null);
 
 				resourceSelect.put(key, rect);
 
 				x++;
 				y += x / 2;
 				x %= 2;
 			}
 		}
 		g2d.setColor(new Color(212, 175, 55));
 		g2d.setStroke(new BasicStroke(3));
 		g2d.drawRoundRect(resourceSelectRect.x, resourceSelectRect.y,
 				resourceSelectRect.width, resourceSelectRect.height, r, r);
 	}
 
 	public void rotate() {
 		rotation = (rotation + 1) % 4;
 	}
 
 	/**
 	 * Returns the number of steps clockwise the display is rotated, from 0 to
 	 * 3.
 	 */
 	public int getRotation() {
 		return rotation;
 	}
 
 	private int hoverX, hoverY;
 	private Image hoverImage = new ImageIcon(
 			"Assets/Templates/TileTemplate.png").getImage();
 
 	public void setHighlightedTile(int x, int y) {
 		hoverX = x;
 		hoverY = y;
 		tileHighLighted = true;
 	}
 
 	public void unHighlightTile() {
 		tileHighLighted = false;
 	}
 }
