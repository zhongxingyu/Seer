 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package client;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Iterator;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 
 /**
  *
  * @author radim
  */
 public class MainFrame extends JFrame {
 
     JMenuBar menubar;
     JPanel panel;
    int userId;
 
    public MainFrame(Role r, int id) {
        userId = id;
         // UI design&layout
         setTitle("Systém SPRÁVCE");
 
         menubar = new JMenuBar();
         menubar = new JMenuBar();
         setJMenuBar(menubar);
         JMenu session = new JMenu("Session");
         menubar.add(session);
         JMenuItem changePass = new JMenuItem("Change password");
         session.add(changePass);
         JMenuItem logout = new JMenuItem("Logout");
         session.add(logout);
         JMenuItem exit = new JMenuItem("EXIT");
         session.add(exit);
 
         JMenu functions = new JMenu("Functions");
         menubar.add(functions);
         Iterator<JMenuItem> items = r.getMenuItems().iterator();
         while (items.hasNext()) {
             functions.add(items.next());
         }
 
         JMenu help = new JMenu("Help");
         menubar.add(help);
 
         help.add(Main.getAboutMenuItem());
 
         // TODO get user information from server
         // TODO David - Add employee profile panel
         panel = new JPanel();
         getContentPane().add(panel);
         //CommonFrame info = new CommonFrame(idUser);
         //panel.add(info);
         pack();
         // UI event handling
         setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         addWindowListener(new WindowAdapter() {
 
             @Override
             public void windowClosing(WindowEvent e) {
                 logoutAndExit();
             }
         });
         exit.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 logoutAndExit();
             }
         });
         logout.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 logout();
                 setVisible(false);
                 dispose();
                 new LoginFrame().setVisible(true);
             }
         });
         changePass.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                new ChangePassDialog(userId).setVisible(true);
             }
         });
     }
 
     private void logoutAndExit() {
         try { // since ServerConnection.sendMsg crashes in demo runs
             logout();
         } finally {
             System.exit(0);
         }
     }
 
     private void logout() {
         ServerConnection.getInstance().close();
     }
 }
