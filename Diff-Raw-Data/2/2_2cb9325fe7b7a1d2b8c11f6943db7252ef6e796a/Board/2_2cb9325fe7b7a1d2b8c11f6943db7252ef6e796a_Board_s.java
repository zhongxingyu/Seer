 package ch.zhaw.mcag.view;
 
 import ch.zhaw.mcag.inputAdapter.*;
 import ch.zhaw.mcag.*;
 import ch.zhaw.mcag.Config;
 import ch.zhaw.mcag.level.Level;
 import ch.zhaw.mcag.model.*;
 import ch.zhaw.mcag.sensor.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import com.leapmotion.leap.*;
 
 /**
  * The front end
  */
 public class Board extends JPanel implements ActionListener {
 
 	private static final long serialVersionUID = 6466804428038769553L;
 	private final Game c;
 	private final Controller leapController;
 	private final Listener sensorListener;
 	private final Listener shootListener;
 	private final IControlable leapAdapter;
 	private final Menu menu;
 	private boolean showMenu = true;
 
 	/**
 	 * Create a new board
 	 *
 	 * @param c
 	 */
 	public Board(Game c) {
 		// add leap motion controller
 		leapAdapter = new SensorAdapter(c);
 		sensorListener = new SensorListener(leapAdapter);
 		shootListener = new ShootListener(leapAdapter);
 		leapController = new Controller();
 		leapController.addListener(sensorListener);
 		leapController.addListener(shootListener);
 
 		addKeyListener(new KeyboardAdapter(c, this));
 
 		this.menu = new Menu(this, c);
 		setFocusable(true);
 		setDoubleBuffered(true);
 		this.c = c;
 		c.setPlayer(ItemFactory.createPlayer());
 		c.setBackground(ItemFactory.createBackground());
 
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		super.paint(g);
 
 		//menu on game over
 		if (c.getLifes() < 1) {
 			showMenu = true;
 		}
 
 		this.paintBackground((Graphics2D) g);
 		this.paintItems((Graphics2D) g);
 		this.paintLifes((Graphics2D) g);
 		this.paintScore((Graphics2D) g);
 		this.paintMenu((Graphics2D) g);
 
 		Toolkit.getDefaultToolkit().sync();
 		g.dispose();
 	}
 
 	private void paintMenu(Graphics2D g2d) {
 		if (showMenu == true) {
			if (c.getPoints() > c.getHighscore().getLowestPointsInTable()) {
 				menu.setState(4);
 			}
 			menu.draw(g2d);
 		}
 	}
 
 	private void paintBackground(Graphics2D g2d) {
 		int x = c.getBackground().getPosition().getX();
 		int y = c.getBackground().getPosition().getY();
 		g2d.drawImage(c.getBackground().getImage(), x - c.getBackground().getDimension().getLength(), y, Config.getBoardDimension().getLength(), Config.getBoardDimension().getHeight(), this);
 		g2d.drawImage(c.getBackground().getImage(), x, y, Config.getBoardDimension().getLength(), Config.getBoardDimension().getHeight(), this);
 		g2d.drawImage(c.getBackground().getImage(), x + c.getBackground().getDimension().getLength(), y, Config.getBoardDimension().getLength(), Config.getBoardDimension().getHeight(), this);
 		if (Config.getForeground().compareTo("") == 0) {
 			ImageIcon foreground = new ImageIcon(this.getClass().getResource(Config.getImagePath() + Config.getForeground()));
 			g2d.drawImage(foreground.getImage(), 0, Config.getBoardDimension().getHeight() - foreground.getIconHeight(), Config.getBoardDimension().getLength(), foreground.getIconHeight(), this);
 		}
 	}
 
 	private void paintItems(Graphics2D g2d) {
 		for (Item item : c.getAllStuff()) {
 			if (item != null) {
 				if (item.flicker()) {
 					g2d.drawImage(item.getImage(), item.getPosition().getX(), item.getPosition().getY(), this);
 				}
 			}
 		}
 	}
 
 	private void paintScore(Graphics2D g2d) {
 		Font font = new Font("sans", Font.PLAIN, 36);
 		g2d.setColor(Color.green);
 		g2d.setFont(font);
 		g2d.drawString((int) c.getPoints() + "", 10, 50);
 	}
 
 	private void paintLifes(Graphics2D g2d) {
 		int lifes = c.getLifes();
 		for (int i = 0; i < lifes; i++) {
 			ImageIcon imageIcon = new ImageIcon(ItemFactory.class.getResource(Config.getImagePath() + Level.getLevel().getLife()));
 			Life life = ItemFactory.createLife(Config.getBoardDimension().getLength() - (2 + i) * imageIcon.getIconWidth(), 0, imageIcon);
 			g2d.drawImage(life.getImage(), life.getPosition().getX(), life.getPosition().getY(), this);
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 	}
 
 	/**
 	 * Toggle the menu
 	 */
 	public void toggleMenu() {
 		if (c.getLifes() > 0) {
 			c.setPause(!c.isPaused());
 			showMenu = !showMenu;
 		}
 	}
 
 	/**
 	 * Get the menu
 	 *
 	 * @return menu
 	 */
 	public Menu getMenu() {
 		return menu;
 	}
 
 	/**
 	 * Return true if the menu should be shown
 	 *
 	 * @return showMenu
 	 */
 	public boolean showMenu() {
 		return showMenu;
 	}
 }
