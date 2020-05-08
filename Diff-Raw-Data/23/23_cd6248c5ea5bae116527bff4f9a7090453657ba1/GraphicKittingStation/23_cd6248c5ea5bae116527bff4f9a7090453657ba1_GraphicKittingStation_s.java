 package factory.graphics;
 
 import java.awt.*;
 
 import javax.swing.ImageIcon;
 
 /**
  * @author Tobias Lee <p>
  * <b>{@code GraphicKittingStation.java}</b> (80x300)<br>
  * The graphical representation of a Kit Station.
  * Holds up to two Kits and one more for Inspection.
  */
 
 public class GraphicKittingStation {
 	
 	/**The x coordinate of the Kit Station*/
 	private int x;
 	/**The y coordinate of the Kit Station*/
 	private int y;
 	/**The Kits the Kit Station holds*/
 	private GraphicKit[] kits;
 	/**The Kit in the Inspection Station*/
 	private GraphicKit check;
 	/**Timer for animation of Inspection Camera*/
 	private int timer;
 	/**The GraphicPanel for intercomponent communication*/
 	GraphicPanel GP;
 	
 	/**The maximum number of kits a station can hold*/
 	public static final int MAX_KITS = 2;
 	public static final Image TRASH_CHUTE_IMAGE = new ImageIcon("Images/trashChute.png").getImage();
 	public static final Image KITTING_STATION_IMAGE = new ImageIcon("Images/kittingStation.png").getImage();
 	
 	/**
 	 * Creates a Kit Station at the given x and y coordinates
 	 * @param x The initial x coordinate of the Kit Station
 	 * @param y The initial y coordinate of the Kit Station
 	 * @param GP The GraphicPanel for intercomponent communication
 	 */
 	public GraphicKittingStation(int x, int y, GraphicPanel GP) {
 		this.x = x;
 		this.y = y;
 		this.GP = GP;
 		
 		timer = -1;
 		kits = new GraphicKit[MAX_KITS];
 		for (int i = 0; i < MAX_KITS; i++)
 			kits[i] = null;
 		check = null;
 	}
 	
 	/**
 	 * Paints the Kit Station
 	 * @param g The specified graphics window
 	 */
 	public void paint(Graphics g) {
 		// Draw kitting station
 		g.drawImage(KITTING_STATION_IMAGE, x, y, null);
 		
 		// Draw trash chute
 		g.drawImage(TRASH_CHUTE_IMAGE, x, y+313, null);
 
 		// Draw kits
 		drawKits(g);
 		
 		//Camera flash
 		if (timer != -1) {
 			timer++;
 			g.setColor(Color.white);
 			g.fillOval(x-5*timer+25, y+250-5*timer, 10*timer, 10*timer);
 			if (timer == 5) {
 				timer = -1;
 				GP.takePictureOfInspectionSlotDone();
 			}
 		}
 	}
 	
 	/**
 	 * Repositions the Kits in the Kit Station
 	 */
 	public void revalidateKits() {
 		for (int i = 0; i < MAX_KITS; i++)
 			if (kits[i] != null)
				kits[i].move(x+15, y+100*i+15);
 		if (hasCheck())
			check.move(x+15, y+215);
 	}
 	/**
 	 * Paints the Kits in the Kit Station
 	 * @param g The specified graphics window
 	 */
 	public void drawKits(Graphics g) {
 		revalidateKits();
 		
 		for (int i = 0; i < MAX_KITS; i++)
 			if (kits[i] != null)
 				kits[i].paint(g);
 		if (hasCheck())
 			check.paint(g);
 	}
 	
 	/**
 	 * Generically adds a Kit to whatever slot is available
 	 * @param kit The Kit being added
 	 * @return {@code true} if the Kit was added successfully; {@code false} otherwise
 	 * @deprecated Use addKit(GraphicKit kit, int index) instead
 	 */
 	public boolean addKit(GraphicKit kit) {
 		if (kits[0] == null)
 			kits[0] = kit;
 		else if (kits[1] == null)
 			kits[1] = kit;
 		else
 			return false;
 		return true;
 	}
 	
 	/**
 	 * Adds a Kit to the given slot
 	 * @param kit The Kit being added
 	 * @param index The slot to add the Kit to
 	 * @return {@code true} if the Kit was added successfully; {@code false} otherwise
 	 */
 	public boolean addKit(GraphicKit kit, int index) {
 		if (index != 0 && index != 1)
 			return addKit(kit);
 		if (kits[index] == null)
 			kits[index] = kit;
 		else
 			return false;
 		return true;
 	}
 	
 	/**
 	 * Adds a Kit to the Inspection Station
 	 * @param kit The Kit being added
 	 * @return {@code true} if the Kit was added successfully; {@code} false otherwise
 	 */
 	public boolean addCheck(GraphicKit kit) {
 		if (hasCheck())
 			return false;
 		check = kit;
 		return true;
 	}
 	
 	/**
 	 * Gets the Kit at the given index
 	 * @param index The slot to get the Kit from
 	 * @return The Kit at the given index
 	 */
 	public GraphicKit getKit(int index) {
 		return kits[index];
 	}
 	
 	/**
 	 * Gets the Kit in the Inspection Station
 	 * @return the Kit at the Inspection Station
 	 */
 	public GraphicKit getCheck() {
 		return check;
 	}
 	
 	/**
 	 * Gets and removes the Kit at the given index
 	 * @param index The slot to get the Kit from
 	 * @return The Kit at the given index
 	 */
 	public GraphicKit popKit(int index) {
 		GraphicKit temp = kits[index];
 		kits[index] = null;
 		return temp;
 	}
 	
 	/**
 	 * Gets and removes the Kit in the Inspection Station
 	 * @return The Kit in the Inspection Station
 	 */
 	public GraphicKit popCheck() {
 		GraphicKit temp = check;
 		check = null;
 		return temp;
 	}
 	
 	/**
 	 * Checks if the Kit Station has any Kits
 	 * @return {@code true} if the Kit Station has at least 1 Kit; {@code false} otherwise
 	 */
 	public boolean hasKits() {
 		return kits[0] != null || kits[1] != null;
 	}
 	
 	/**
 	 * Checks if the Kit Station is full
 	 * @return {@code true} if the Kit Station has the maximum number of Kits; {@code false} otherwise
 	 */
 	public boolean maxed() {
 		return kits[0] != null && kits[1] != null;
 	}
 	
 	/**
 	 * Checks if there is a Kit in the Inspection Station
 	 * @return {@code true} if there is a Kit in the Inspection Station; {@code false} otherwise
 	 */
 	public boolean hasCheck() {
 		return check != null;
 	}
 	
 	/**
 	 * Calls the Camera Flash
 	 */
 	public void checkKit() {
 		timer = 0;
 	}
 	
 	/**
 	 * Adds an Item to the Kit at the given slot
 	 * @param item The Item to add to the Kit
 	 * @param target The slot whose Kit should be added to
 	 */
 	public void addItem(GraphicItem item, int target) {
 		if (target != 0 && target != 1)
 			return;
 		if (kits[target] == null)
 			return;
 		kits[target].addItem(item);
 	}
 	
 	/**
 	 * Gets the x coordinate of the Kit Station
 	 * @return The x coordinate of the Kit Station
 	 */
 	public int getX() {
 		return x;
 	}
 	
 	/**
 	 * Gets the y coordinate of the Kit Station
 	 * @return The y coordinate of the Kit Station
 	 */
 	public int getY() {
 		return y;
 	}
 	
 }
