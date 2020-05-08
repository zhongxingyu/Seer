 package com.nigorojr.typebest;
 
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.text.DateFormat;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneLayout;
 
 @SuppressWarnings("serial")
 public class RecordsWindow extends JFrame {
     private JPanel recordsPanel = new JPanel();
     public static final Font font = new Font("Arial", Font.PLAIN, 20);
 
     public RecordsWindow(Records records) {
         super("Records");
 
         setSize(800, 500);
         setPreferredSize(getSize());
 
         recordsPanel.setLayout(new BoxLayout(recordsPanel, BoxLayout.PAGE_AXIS));
         addRecords(records);
 
         JScrollPane jsp = new JScrollPane(recordsPanel);
         jsp.setLayout(new ScrollPaneLayout());
         add(jsp);
 
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setLocationRelativeTo(null);
     }
 
     private void addRecords(Records records) {
         Record[] recordArray = records.getAllRecords();
 
         for (Record record : recordArray) {
             JPanel oneRecord = new JPanel();
             oneRecord.setLayout(new FlowLayout(FlowLayout.LEADING));
 
             String[] recordString = {
                     String.format("%.3f", record.time / 1000000000.0),
                     Integer.toString(record.miss),
                     record.username,
                     record.keyboardLayout,
                     DateFormat.getInstance().format(record.date),
             };
             for (String str : recordString) {
                 JLabel label = new JLabel(str);
                 label.setFont(font);
                 oneRecord.add(label);
             }
 
             recordsPanel.add(oneRecord);
         }
     }
 }
