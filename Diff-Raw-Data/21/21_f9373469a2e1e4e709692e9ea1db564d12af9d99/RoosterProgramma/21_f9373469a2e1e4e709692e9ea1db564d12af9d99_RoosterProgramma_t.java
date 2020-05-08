 package roosterprogramma;
 
 import connectivity.Dbmanager;
 import connectivity.QueryManager;
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Arrays;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import view.Login;
 
 /**
  *
  * @author Dark
  */
 public class RoosterProgramma {
 
     private static RoosterProgramma instance = new RoosterProgramma();
     private static JFrame mainWindow;
     public static final String NAME = "Rooster Programma";      // ToDo : Setting ervan maken?
     public static final Font FONT_10_PLAIN = new Font("Verdana", Font.PLAIN, 10);
     public static final Font FONT_10_BOLD = new Font("Verdana", Font.BOLD, 10);
     public static final Font FONT_12_BOLD = new Font("Verdana", Font.BOLD, 12);
     public static final Font FONT_16_BOLD = new Font("Verdana", Font.BOLD, 16);
     public static final Font FONT_25_BOLD = new Font("Verdana", Font.BOLD, 25);
     private Dbmanager dbManager;
     private QueryManager queryManager;
 
     public int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
     public int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
 
     public static RoosterProgramma getInstance() {
         return instance;
     }
 
     public static QueryManager getQueryManager() {
         return getInstance().queryManager;
     }
 
     public static void main(String args[]) {
         final RoosterProgramma applicatie = RoosterProgramma.getInstance();
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 try {
                     applicatie.initialize();
                     applicatie.startup();
                 } catch (Exception e) {
                     System.out.println("Application" + applicatie.getClass().getName() + "failed to launch");
                 }
             }
         });
     }
 
     public void showPanel(JPanel panel) {
         mainWindow.getContentPane().removeAll();
         mainWindow.getContentPane().add(panel, BorderLayout.CENTER);
         mainWindow.getContentPane().validate();
         mainWindow.getContentPane().repaint();
     }
 
     public void initialize() {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
             System.err.println("Error setting LookAndFeelClassName: " + e);
         }
         dbManager = new Dbmanager();
         dbManager.openConnection();
         queryManager = new QueryManager(dbManager);
     }
 
     public void startup() {
         mainWindow = new JFrame(NAME);
         mainWindow.setSize(screenWidth, screenHeight);
         mainWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
         mainWindow.addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 shutdown();
             }
         });
 
         InputKeyListener keylisten = new InputKeyListener(mainWindow);
         keylisten.start();
 
         mainWindow.getContentPane().setLayout(new BorderLayout());
 
         //UpdateChecker check = new UpdateChecker();
         //showPanel(new Login(check.checkForUpdate()));
         showPanel(new Login(4));
 
         mainWindow.setVisible(true);
     }
 
     public void shutdown() {
         mainWindow.setVisible(false);
         mainWindow.dispose();
         dbManager.closeConnection();
         System.exit(0);
     }
 
     public String decodePassword(char[] input) {
         String typedPassword = "";
         for (char output : input)
         {
             typedPassword += output;
         }
         Arrays.fill(input, '0');
         return typedPassword;
     }
 
     public boolean promptWarning(String question) {
         int choice = JOptionPane.showConfirmDialog(
             null,
             question,
             "Waarschuwing!",
             JOptionPane.YES_NO_OPTION,
             JOptionPane.WARNING_MESSAGE
         );
 
         if (choice == JOptionPane.YES_OPTION)
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     public void showError(String error) {
         JOptionPane.showMessageDialog(
             null,
             error,
             "Fout!",
             JOptionPane.ERROR_MESSAGE
         );
     }
 
     public boolean isEmpty (String strToTest) {
        boolean empty = false;
         try {
             strToTest.length();
            
         } catch (NullPointerException npe) {
            empty = true;
         }
         return empty;
     }
 
     public boolean isNumeric (String strToTest) {
         boolean numeric = true;
         try {
             Double.parseDouble(strToTest);
         } catch (NumberFormatException nfe) {
             numeric = false;
         }
         return numeric;
     }
 }
