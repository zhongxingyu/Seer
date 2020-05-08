 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package javathing;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import javathing.settings.Settings;
 import javathing.level.LevelManager;
 import javathing.load.LevelLoader;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javathing.input.MenuMouseListener;
 import javathing.menu.ButtonEvent;
 import javathing.menu.MenuButton;
 import javathing.menu.MenuManager;
 import javathing.menu.RectangleMenuButton;
 import javathing.render.Paintable;
 import javathing.sprite.Player;
 import javax.swing.JFrame;
 
 /**
  *
  * @author lausd_user
  */
 public class MainClass {
 
     /**
      * @param args the command line arguments
      */
     private static LevelManager levelManager;
     private static MenuManager menuManager;
     private static GameContainer container;
     private static Screen screen;
     private static JFrame frame;
     private static MainApplet mainApplet;
 
     public static void main(String[] args) {
 	/*init order:
 	 * frame
 	 * menu
 	 * app
 	 * event listeners
 	 * start thread
 	 * ...
 	 * player
 	 * level
 	 * screen
 	 * 
 	 */
 	frame = new JFrame();
 	setSettings(frame);
 
 	//initLevel();
 	//screen = new Screen(levelManager.getStartingPosition().x - 20, levelManager.getStartingPosition().y - 20);
 	initMenu();
 	mainApplet = new MainApplet();
 	menuManager.activateListeners();
 	
 	frame.add(mainApplet);
 	mainApplet.setVisible(true);
 	mainApplet.setSize(Settings.SCREEN_WIDTH, Settings.SCREEN_HTIGHT);
 
 
 
 
 	Thread updateThread = new Thread(new Runnable() {
 	    @Override
 	    public void run() {
 		while (true) {
 		    mainApplet.repaint();
 		    try {
 			Thread.sleep(Settings.SLEEPTIME);
 		    } catch (InterruptedException ex) {
 			Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
 		    }
 		}
 
 	    }
 	});
 	updateThread.start();
     }
 
     private static void setSettings(JFrame frame) {
 	frame.setResizable(false);
 	frame.setName("Window");
 	frame.setTitle("Window");
 	frame.setSize(Settings.SCREEN_WIDTH, Settings.SCREEN_HTIGHT);
 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	frame.setVisible(true);
 
     }
     
     private static void initMenu() {
 	List<MenuButton> buttons = new ArrayList<MenuButton>();
 	buttons.add(new RectangleMenuButton(20, 20, 100, 50, Color.red, "Start Game", new ButtonEvent() {
 
 	    @Override
 	    public void pressed() {
 		
 		initLevel();
 		menuManager = null;
 	    }
 	    
 	}));
 	menuManager = new MenuManager(buttons, Color.BLACK);
 	menuManager.addMouseListener(new MenuMouseListener());
 	setContainer(new GameContainer() {
 	    @Override
 	    public void paint(Graphics g) {
 		BufferedImage buf = new BufferedImage(Settings.SCREEN_WIDTH, Settings.SCREEN_HTIGHT, BufferedImage.TYPE_INT_BGR);
 		Graphics gr = buf.createGraphics();
 
 		menuManager.paint(gr);
 
 		g.drawImage(buf, 0, 0, null);
 	    }
 
 	    @Override
 	    public void update() {
 		
 	    }
 
 	    @Override
 	    public String getName() {
 		return "Game:Menu";
 	    }
 
 	    @Override
 	    public void dispose() {
 		menuManager.deactivateListeners();
 	    }
 	});
        
 	
     }
     
     private static Player player;
 
     private static void initLevel() {
 
 	try {
 	    LevelLoader loader = new LevelLoader("C:\\users\\henry\\desktop\\level.txt");
 	    levelManager = loader.getLevelManager();
 	} catch (FileNotFoundException ex) {
 	    ex.printStackTrace();
 	}
 
 	//levelManager.activateListeners();
 	setContainer(new GameContainer() {
 	    @Override
 	    public void paint(Graphics g) {
 		BufferedImage buf = new BufferedImage(Settings.SCREEN_WIDTH, Settings.SCREEN_HTIGHT, BufferedImage.TYPE_INT_BGR);
 		Graphics gr = buf.createGraphics();
 
 		for (Paintable paintable : levelManager.getPaintables()) {
 		    paintable.paint(gr);
 		}
 
 		g.drawImage(buf, 0, 0, null); //To change body of generated methods, choose Tools | Templates.
 	    }
 
 	    @Override
 	    public void update() {
 		for (Updateable updateable : levelManager.getUpdateables()) {
 		    updateable.update();
 		}
 	    }
 
 	    @Override
 	    public String getName() {
 		return "Game:Level";
 	    }
 
 	    @Override
 	    public void dispose() {
 		levelManager.deactivateListeners();
 	    }
 	});
         levelManager.activateListeners();
 	screen = new Screen(levelManager.getStartingPosition().x - 20, levelManager.getStartingPosition().y - 20);
 	
         player = Player.initNewPlayer();
         //levelManager.activateListeners();
     }
 
     /**
      * @return the levelManager
      */
     public static LevelManager getLevelManager() {
 	return levelManager;
     }
     
     /**
      * @return the menuManager
      */
     public static MenuManager getMenuManager() {
 	return menuManager;
     }
 
     /**
      * @return the screen
      */
     public static Screen getScreen() {
 	return screen;
     }
 
     public static void addKeyListener(KeyListener keyListener) {
         mainApplet.addKeyListener(keyListener);
	//frame.addKeyListener(keyListener);
        System.err.println("fdsaaffdas");
     }
 
     public static void removeKeyListener(KeyListener keyListener) {
         mainApplet.removeKeyListener(keyListener);
	//frame.removeKeyListener(keyListener);
     }
     
     public static void addMouseListener(MouseListener mouseListener) {
 	mainApplet.addMouseListener(mouseListener);
     }
 
     public static void removeMouseListener(MouseListener mouseListener) {
 	mainApplet.removeMouseListener(mouseListener);
     }
 
     /**
      * @return the player
      */
     public static Player getPlayer() {
 	return player;
     }
 
     /**
      * @return the container
      */
     public static GameContainer getContainer() {
 	return container;
     }
 
     /**
      * @param aContainer the container to set
      */
     public static void setContainer(GameContainer aContainer) {
 	if (container != null) {
 	    container.dispose();
 	}
 	container = aContainer;
     }
 }
