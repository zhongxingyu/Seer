 package org.infuse.hexview;
 
 import java.awt.BorderLayout;
import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JScrollPane;
import javax.swing.JTextArea;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingUtilities;
 
 @SuppressWarnings("serial")
 public class MainFrame extends JFrame {
   
   private HexViewTable _table;
   
   public MainFrame() throws IOException {
     initGUI();
   }
   
   private void initGUI() throws IOException {
     // Main window
     setTitle("hexview");
    setSize(1280, 320);
     setDefaultCloseOperation(EXIT_ON_CLOSE);
     setLocationRelativeTo(null);
     setLayout(new BorderLayout());
     
     // Hex view
     _table = new HexViewTable();
     JScrollPane sp = new JScrollPane(_table);
     sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    sp.setPreferredSize(new Dimension(_table.getPreferredSize().width+24, _table.getPreferredSize().height));
    add(sp, BorderLayout.WEST);
     
     // Menu
     JMenuBar mb = new JMenuBar();
     JMenu m = new JMenu("File");
     m.setMnemonic(KeyEvent.VK_F);
     JMenuItem mi = new JMenuItem("Open...", KeyEvent.VK_O);
     mi.addActionListener(new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent e) {
         chooseFile();
       }
     });
     m.add(mi);
     mb.add(m);
     setJMenuBar(mb);
     
     // Finalize window
     setVisible(true);
   }
   
   private void chooseFile() {
     JFileChooser fc = new JFileChooser();
     if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(MainFrame.this)) {
       File sf = fc.getSelectedFile();
       if (sf.exists() && !sf.isDirectory()) {
         try {
           ((HexViewTableModel)_table.getModel()).setFile(sf);
           _table.revalidate();
         } catch (IOException ex) {
           // TODO: Show message
         }
       }
     }
   }
   
   public static void main(String[] args) {
     SwingUtilities.invokeLater(new Runnable() {
       @Override
       public void run() {
         try {
           new MainFrame();
         } catch(IOException ex) {
           // TODO: Show message
         }
       }
     });
   }
   
 }
