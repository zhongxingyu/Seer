 package org.infuse.hexview;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
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
 import javax.swing.JTable;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.table.TableColumnModel;
 
 @SuppressWarnings("serial")
 public class MainFrame extends JFrame {
   
   private JTable _table;
   
   public MainFrame() throws IOException {
     initGUI();
   }
   
   private void initGUI() throws IOException {
     // Main window
     setTitle("hexview");
     setSize(568, 320);
     setDefaultCloseOperation(EXIT_ON_CLOSE);
     setLocationRelativeTo(null);
     setLayout(new BorderLayout());
     
     // Hex view
     _table = new JTable(new HexViewTableModel(null));
     _table.setFont(new Font("Courier New", _table.getFont().getStyle(), _table.getFont().getSize()));
     TableColumnModel cm = _table.getColumnModel();
     fixWidth(cm, 0, 80);
     for (int i = 1; i < 17; i++) { fixWidth(cm, i, 22); }
     cm.getColumn(17).setMinWidth(80);
     _table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
     _table.setFillsViewportHeight(true);
     JScrollPane sp = new JScrollPane(_table);
     sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
     add(sp, BorderLayout.CENTER);
     
     // Menu
     JMenuBar mb = new JMenuBar();
     JMenu m = new JMenu("File");
     m.setMnemonic(KeyEvent.VK_F);
     JMenuItem mi = new JMenuItem("Open...", KeyEvent.VK_O);
     mi.addActionListener(new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent e) {
         JFileChooser fc = new JFileChooser();
         if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(MainFrame.this)) {
           File sf = fc.getSelectedFile();
           if (sf.exists() && !sf.isDirectory()) {
             try {
               ((HexViewTableModel)MainFrame.this._table.getModel()).setFile(sf);
              MainFrame.this._table.revalidate();
             } catch (IOException ex) {
               // TODO: Show message
             }
           }
         }
       }
     });
     m.add(mi);
     mb.add(m);
     setJMenuBar(mb);
     
     // Finalize window
     setVisible(true);
   }
   
   private void fixWidth(TableColumnModel cm, int index, int width) {
     cm.getColumn(index).setMinWidth(width);
     cm.getColumn(index).setPreferredWidth(width);
     cm.getColumn(index).setMaxWidth(width);
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
