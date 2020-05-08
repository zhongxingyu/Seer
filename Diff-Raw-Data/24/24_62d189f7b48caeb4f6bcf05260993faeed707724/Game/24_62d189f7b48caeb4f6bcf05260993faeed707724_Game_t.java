 import elements.Window;
 import values.Settings;
import values.BrickColor;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.swing.WindowConstants;
 import java.awt.Dimension;
 import java.awt.Toolkit;
import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 
import basic.Layout;

 /**
  * Class holds game window and implements game management actions.
  * 
  * @author X-Stranger
  */
 @SuppressWarnings("serial")
 public class Game extends JFrame {
     private Settings settings = new Settings();
     private Window window = new Window(settings);
     
     /**
      * Default constructor.
      */
     public Game() {
         super();
        this.printJvmDetails();
         this.setLAF();
         this.setMenu();
         this.start();        
     }
     
    private void printJvmDetails() {
        String ver = System.getProperty("java.version");
        String ven = System.getProperty("java.vendor");
        System.out.println("JVM v" + ver + " by " + ven);
    }

     /**
      * Methos configures game window and shows it at the screen.
      */
     private void start() {
         // activate savegame slots 
         window.activateSlots();
 
         // displaying window at the screen
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setTitle(settings.getString("TITLE"));
         setIconImage(new ImageIcon(ClassLoader.getSystemResource("images/icon.png")).getImage());
         
         setContentPane(window);
         
         addWindowListener(window);
 
         pack();
         setResizable(false);
         
        // fix game window size (workaround for OpenJDK)
        Insets insets = this.getInsets();
        int w = BrickColor.BLACK.getColor().getIconWidth() * (Layout.FIELD + 3 * 2);
        int h = BrickColor.BLACK.getColor().getIconHeight() * (Layout.FIELD + 3 * 2);
        this.setSize(w + insets.left + insets.right, 
                h + getJMenuBar().getHeight() + insets.top + insets.bottom);

        
         Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
         setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
 
         try {
             window.loadFromFile();
             window.updateTitle();
         } catch (Exception e) {
             System.out.println(settings.getString("ERROR_LOAD") + " (" + e.getMessage() + ")");
         }
 
         setVisible(true);
     }
     
     /**
      * Methos configures Look-And-Feel.
      */
     private void setLAF() {
         // configuring LAF
         try {
             String laf = settings.getLaF();
             UIManager.setLookAndFeel(laf);
             
         } catch (Exception e) {
             System.out.println(settings.getString("ERROR_LAF"));
         }
     }
     
     /**
      * Method creates game menu.
      */
     private void setMenu() {
         // configuring menu
         setJMenuBar(settings.getMenu());
         
         JMenu gameMenu = new JMenu(settings.getString("MENU_GAME"));
         settings.getMenu().add(gameMenu);
         
         JMenuItem menuItem = new JMenuItem(settings.getString("MENU_GAME_NEW"), KeyEvent.VK_N);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
         menuItem.addActionListener(window);
         gameMenu.add(menuItem);
 
         menuItem = new JMenuItem(settings.getString("MENU_GAME_UNDO"), KeyEvent.VK_Z);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
         menuItem.addActionListener(window);
         menuItem.setEnabled(false);
         gameMenu.add(menuItem);
         settings.setUndo(menuItem);
 
         gameMenu.addSeparator();
 
         menuItem = new JMenuItem(settings.getString("MENU_GAME_SAVE"), KeyEvent.VK_S);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
         menuItem.addActionListener(window);
         gameMenu.add(menuItem);
 
         menuItem = new JMenu(settings.getString("MENU_GAME_SAVE_SLOT"));
         menuItem.addActionListener(window);
         gameMenu.add(menuItem);
         for (Integer i = 1; i <= 5; i++) {
             JMenuItem slot = new JMenuItem(settings.getString("MENU_GAME_SLOT") + " " + i);
             slot.setName(settings.getString("MENU_GAME_SAVE_SLOT") + " " + i);
             slot.addActionListener(window);
             menuItem.add(slot);
             window.addSaveSlot(slot);
         }
 
         gameMenu.addSeparator();
 
         menuItem = new JMenuItem(settings.getString("MENU_GAME_LOAD"), KeyEvent.VK_L);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
         menuItem.addActionListener(window);
         gameMenu.add(menuItem);
 
         menuItem = new JMenu(settings.getString("MENU_GAME_LOAD_SLOT"));
         menuItem.addActionListener(window);
         gameMenu.add(menuItem);
         for (Integer i = 1; i <= 5; i++) {
             JMenuItem slot = new JMenuItem(settings.getString("MENU_GAME_SLOT") + " " + i);
             slot.setName(settings.getString("MENU_GAME_LOAD_SLOT") + " " + i);
             slot.addActionListener(window);
             menuItem.add(slot);
             window.addLoadSlot(slot);
         }
 
         gameMenu.addSeparator();
 
         menuItem = new JMenuItem(settings.getString("MENU_GAME_OPTIONS"), KeyEvent.VK_P);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
         menuItem.addActionListener(window);
         gameMenu.add(menuItem);
         
         gameMenu.addSeparator();
         
         menuItem = new JMenuItem(settings.getString("MENU_GAME_EXIT"), KeyEvent.VK_X);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
         menuItem.addActionListener(window);
         gameMenu.add(menuItem);
 
         JMenu aboutMenu = new JMenu(settings.getString("MENU_ABOUT"));
         settings.getMenu().add(aboutMenu);
 
         menuItem = new JMenuItem(settings.getString("MENU_ABOUT_HIGHSCORES"), KeyEvent.VK_H);
         menuItem.addActionListener(window);
         aboutMenu.add(menuItem);
 
         menuItem = new JMenuItem(settings.getString("MENU_ABOUT_GAME"), KeyEvent.VK_G);
         menuItem.addActionListener(window);
         aboutMenu.add(menuItem);
     }
 }
