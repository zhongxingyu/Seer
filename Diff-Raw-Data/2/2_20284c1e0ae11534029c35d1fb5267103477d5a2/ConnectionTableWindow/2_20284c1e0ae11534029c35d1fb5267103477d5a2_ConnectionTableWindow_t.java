 /**
  * ConnectionTableWindow
  * <p>
  * This widget shows a table for the connection between two nodes.
  * 
  * @author Marius Spix
  */
 package de.bwv_aachen.dijkstra.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.util.Arrays;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.WindowConstants;
 
 public class ConnectionTableWindow extends JFrame {
 
     private static final long      serialVersionUID = 4956218067122590646L;
     private Vector<Vector<Object>> rowData;
 
     /**
      * ConnectionTableWindow
      * 
      * @param rowData
      *            Vector of the rows which should be showed
      *            <p>
      *            Reads a file for the data model factory
      */
     public ConnectionTableWindow(Vector<Vector<Object>> rowData) {
         this.rowData = rowData;
     }
 
     /***
      * draw
      * <p>
      * Draws the window.
      */
     public void draw() {
         ((JComponent) getContentPane()).setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.LIGHT_GRAY));
 
         Vector<String> columnNames = new Vector<String>(Arrays.asList(
                 "Abflughafen", "Zielflughafen", "Flugzeit"));
 
         super.setTitle("Ihre Verbindungen");
         super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
         JTable connections = new JTable(rowData, columnNames);
         Container cp = super.getContentPane();
         cp.setLayout(new BorderLayout());
         JScrollPane scroll = new JScrollPane(connections);
 
        cp.add(BorderLayout.NORTH, new JLabel("Verbindungsübersicht"));
         cp.add(BorderLayout.SOUTH, scroll);
 
         super.pack();
         super.setLocationRelativeTo(null);
 
         super.setVisible(true);
     }
 }
