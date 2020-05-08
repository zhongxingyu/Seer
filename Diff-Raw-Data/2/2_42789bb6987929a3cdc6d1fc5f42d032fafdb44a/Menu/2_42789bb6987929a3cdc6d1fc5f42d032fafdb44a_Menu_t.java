 package music.store;
 
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 @SuppressWarnings("serial")
 public class Menu extends JFrame {
     private final Action exitListener = new ExitListener();
     private final Action musicListener = new MusicAppListener();
     private final Action orderListener = new OrderAppListener();
     private final Action salesListener = new SalesAppListener();
     private final Action adminListener = new AdminAppListener();
 
     public void createMenu() {
         EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 try {
                     Menu frame = new Menu();
                     frame.setVisible(true);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         });
     }
 
     public Menu() {
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setBounds(200, 200, 900, 600);
 
         JMenuBar menuBar = new JMenuBar();
         setJMenuBar(menuBar);
 
         JMenu mnMainMenu = new JMenu("Main Menu");
         menuBar.add(mnMainMenu);
 
         JMenuItem mntmMusicApplications = new JMenuItem("Music Applications");
         mntmMusicApplications.setAction(musicListener);
         mnMainMenu.add(mntmMusicApplications);
 
         JMenuItem mntmOrderingApplications = new JMenuItem("Ordering Applications");
         mntmOrderingApplications.setAction(orderListener);
         mnMainMenu.add(mntmOrderingApplications);
 
         JMenuItem mntmSalesApplications = new JMenuItem("Sales Applications");
         mntmSalesApplications.setAction(salesListener);
         mnMainMenu.add(mntmSalesApplications);
 
         JMenuItem mntmAdministrativeApplications = new JMenuItem("Administrative Applications");
        mntmAdministrativeApplications.setAction(adminListener);
         mnMainMenu.add(mntmAdministrativeApplications);
 
         JMenuItem mntmExit = new JMenuItem("Exit");
         mntmExit.setAction(exitListener);
         mnMainMenu.add(mntmExit);
     }
 
     private class ExitListener extends AbstractAction {
         public ExitListener() {
             putValue(NAME, "Exit");
             putValue(SHORT_DESCRIPTION, "Some short description");
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             dispose();
         }
     }
 
     private class MusicAppListener extends AbstractAction {
         public MusicAppListener() {
             putValue(NAME, "Music Applications");
             putValue(SHORT_DESCRIPTION, "Some short description");
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
         }
     }
 
     private class OrderAppListener extends AbstractAction {
         public OrderAppListener() {
             putValue(NAME, "Order Applications");
             putValue(SHORT_DESCRIPTION, "Some short description");
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
         }
     }
 
     private class SalesAppListener extends AbstractAction {
         public SalesAppListener() {
             putValue(NAME, "Sales Applications");
             putValue(SHORT_DESCRIPTION, "Some short description");
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
         }
     }
 
     private class AdminAppListener extends AbstractAction {
         public AdminAppListener() {
             putValue(NAME, "Admin Applications");
             putValue(SHORT_DESCRIPTION, "Some short description");
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
         }
     }
 }
