 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.Serializable;
 import java.util.Random;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  * Scene to display the planets of the universe. Also represents actual planet
  * objects in the universe.
  * 
  * @author TeamTroll
  * @version 1.0
  */
 public class Planet implements Scene, Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private GameScreen screen;
 	private String name, tech, /*res,*/ pol;
	private ImageIcon img;
 	private Point loc;
	private JLabel planetNameLabel, planetTechLabel, planetSpecialEventLabel,
 			planetPolLabel;
 	private JButton marketButton, universeButton;
 	private Rectangle planetRect;
 	
	private Random rand = new Random();
	private String specialEvent;
	private String []  allEvents = {  "DROUGHT" , "ALLGOOD" ,  "COLD" , "CROPFAIL", "WAR", "BOREDOM", "WAR","PLAGUE"
 			,"LACKOFWORKERS",  "BOREDOM", "LACKOFWORKERS"};
 
 	
 
 	/**
 	 * Creates the create player scene. GameScreen screen
 	 * 
 	 * @param GameScreen
 	 *            screen The screen of the game.
 	 * @param String
 	 *            name The name of the planet.
 	 * @param Point
 	 *            loc The location of the planet.
 	 * @param String
 	 *            tech The technology level of the planet.
 	 * @param String
 	 *            res The resource level of the planet.
 	 * @param String
 	 *            pol The political type of the planet.
 	 * @param String
 	 *            filename A string representation of the planet's image
 	 *            location.
 	 */
 	public Planet(final GameScreen screen, String name, Point loc, String tech,
 			String res, String pol, String filename) {
 		
 		specialEvent = allEvents[rand.nextInt(allEvents.length)];
 		
 		this.screen = screen;
 		this.name = name;
 		this.loc = loc;
 		this.tech = tech;
 //		this.res = res;
 		this.pol = pol;
 		img = new ImageIcon(filename);
 	
 		planetRect = new Rectangle(loc, new Dimension(img.getIconWidth(),
 				img.getIconHeight()));
 		planetNameLabel = new JLabel("Planet Name: " + name, JLabel.CENTER);
 		planetTechLabel = new JLabel("Planet Tech Level: " + tech,
 				JLabel.CENTER);
 		planetSpecialEventLabel = new JLabel("Planet Resourses: " + specialEvent, JLabel.CENTER);
 		planetPolLabel = new JLabel("Planet Politics: " + pol, JLabel.CENTER);
 
 		planetNameLabel.setPreferredSize(new Dimension(518, 50));
 		planetTechLabel.setPreferredSize(new Dimension(518, 50));
 		planetSpecialEventLabel.setPreferredSize(new Dimension(518, 50));
 		planetPolLabel.setPreferredSize(new Dimension(518, 50));
 
 		marketButton = new JButton("Market");
 		universeButton = new JButton("Universe");
 		
 		marketButton.addActionListener(new ActionListener() {
 			/**
 			 * Goes to the market.
 			 * 
 			 * @param ActionEvent
 			 *            e The created action event.
 			 */
 			public void actionPerformed(ActionEvent e) {
 				screen.setScene(new Market(screen));
 			}
 		});
 
 		universeButton.addActionListener(new ActionListener() {
 			/**
 			 * Goes to the universe.
 			 * 
 			 * @param ActionEvent
 			 *            e The created action event.
 			 */
 			public void actionPerformed(ActionEvent e) {
 				// Set to screen.game.getUniverse();
 				screen.setScene(screen.game.getUniverse());
 			}
 		});
 
 	}
 
 	/**
 	 * Planet draws itself to the screen.
 	 * 
 	 * @param JPanel
 	 *            p The current jpanel of the game.
 	 * @param Graphics
 	 *            g The current page of the game.
 	 */
 	public void draw(JPanel p, Graphics g) {
 		img.paintIcon(null, g, loc.x, loc.y);
 	}
 
 	/**
 	 * Paints the components onto the screen.
 	 * 
 	 * @param JPanel
 	 *            p The panel to contain the components.
 	 * @param Graphics
 	 *            g The graphics to the corresponding page.
 	 */
 	public void paint(JPanel p, Graphics g) {
 		p.removeAll();
 		p.add(planetNameLabel);
 		p.add(planetTechLabel);
 		p.add(planetSpecialEventLabel);
 		p.add(planetPolLabel);
 		p.add(marketButton);
 		p.add(universeButton);
 
 		p.revalidate();
 	}
 
 	/**
 	 * Getter for the planet's name.
 	 * 
 	 * @return String The planet's name.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Getter for the planet's location.
 	 * 
 	 * @return Point loc The planet's location.
 	 */
 	public Point getLoc() {
 		return loc;
 	}
 
 	/**
 	 * Getter for the planet's tech level.
 	 * 
 	 * @return String The planet's tech level.
 	 */
 	public String getTech() {
 		return tech;
 	}
 
 	/**
 	 * Getter for an integer version of the planet's tech level.
 	 * 
 	 * @return int The planet's tech level.
 	 */
 	public int getTechLevel() {
 		int level = 0;
 
 		if (tech.equals("Pre-Agriculture"))
 			level = 0;
 		else if (tech.equals("Medieval"))
 			level = 1;
 		else if (tech.equals("Renaissance"))
 			level = 2;
 		else if (tech.equals("Early Industrial"))
 			level = 3;
 		else if (tech.equals("Industrial"))
 			level = 4;
 		else if (tech.equals("Post-Industrial"))
 			level = 5;
 		else if (tech.equals("Hi-Tech"))
 			level = 6;
 
 		return level;
 	}
 
 	/**
 	 * Getter for the planet's resource type.
 	 * 
 	 * @return String The planet's resource type.
 	 */
 //	public String getRes() {
 //		return res;
 //	}
 
 	/**
 	 * Getter for the planet's political system type.
 	 * 
 	 * @return String The planet's political system type.
 	 */
 	public String getPol() {
 		return pol;
 	}
 
 	/**
 	 * Getter for the planet's image.
 	 * 
 	 * @return ImageIcon The planet's image.
 	 */
 	public ImageIcon getImg() {
 		return img;
 	}
 
 	/**
 	 * Getter for the planet's rectangle.
 	 * 
 	 * @return Rectangle The planet's rectangle.
 	 */
 	public Rectangle getRect() {
 		return planetRect;
 	}
 	
 	public String getSpecialEvent(){
 		return specialEvent;
 	}
 	
 	public String toString(){
 		return super.toString();
 	}
 }

