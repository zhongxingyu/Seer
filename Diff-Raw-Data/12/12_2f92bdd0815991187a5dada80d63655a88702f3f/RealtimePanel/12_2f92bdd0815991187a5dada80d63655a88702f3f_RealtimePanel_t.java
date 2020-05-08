 package main.demo.gui;
 
 import gx.realtime.CollaborativeMap;
 import gx.realtime.Document;
 import gx.realtime.EventType;
 import gx.realtime.ValueChangedEvent;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 /**
  * Swing panel that handles the display and interaction with the Realtime library
  */
 public class RealtimePanel extends JPanel
 {
     private RealtimeTableModel model;
 
     public RealtimePanel(CollaborativeMap collabMap)
     {
         model = new RealtimeTableModel(collabMap);
 
         // Listen for ValueChangedEvents to update the UI
         collabMap.addEventListener(EventType.VALUE_CHANGED, (ValueChangedEvent event) -> {
             if (!event.isLocal()) {
                 if (event.getNewValue() != null) {
                     model.updateValue(event.getProperty(), (String) event.getNewValue(), event.isLocal());
                 } else {
                     model.removeValue(event.getProperty());
                 }
             }
 
             System.out.println("Received event for key " + event.getProperty());
             eventLogArea.append(event.toString() + "\n");
             eventLogArea.setCaretPosition(eventLogArea.getDocument().getLength());
         });
 
         // Init the components
         initComponents();
 
         // Put a selection listener on the table to prefill the key/value fields
         table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
             int row = table.getSelectedRow();
             if (row == -1)
                 return;
 
             keyField.setText((String) model.getValueAt(row, 0));
             valueField.setText((String) model.getValueAt(row, 1));
         });
     }
 
     /**
      * Method that creates the frame and handles some of the final setup actions.
      *
      * @param document
      * @param collabMap
      */
     public static void createUI(Document document, CollaborativeMap collabMap)
     {
         JFrame frame = new JFrame("Demo Realtime Gx Application");
 
         RealtimePanel newContentPane = new RealtimePanel(collabMap);
         newContentPane.setOpaque(true);
         frame.setContentPane(newContentPane);
 
         frame.pack();
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.addWindowListener(new WindowAdapter()
         {
             public void windowClosing(WindowEvent e)
             {
                 System.out.println("Politely closing API link...");
                 document.close();
             }
         });
         frame.setVisible(true);
     }
 
     private void clearButtonActionPerformed(ActionEvent e)
     {
         model.removeAll();
     }
 
     private void removeButtonActionPerformed(ActionEvent e)
     {
        // Save keys first to prevent changing indexes while in the process of deleting rows
        int[] rows = table.getSelectedRows();
        String[] keys = new String[rows.length];
        for (int i = 0; i < rows.length; i++) {
            keys[i] = (String) model.getValueAt(rows[i], 0);
        }

        // Remove values
        for (String key : keys) {
            model.removeValue(key);
         }
     }
 
     private void putButtonActionPerformed(ActionEvent e)
     {
         model.updateValue(keyField.getText(), valueField.getText(), true);
     }
 
     private void initComponents()
     {
         // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
         tableScrollPane = new JScrollPane();
         table = new JTable(model);
         clearButton = new JButton();
         removeButton = new JButton();
         putButton = new JButton();
         keyField = new JTextField();
         valueField = new JTextField();
         keyLabel = new JLabel();
         valueLabel = new JLabel();
         eventLogScrollPane = new JScrollPane();
         eventLogArea = new JTextArea();
         eventLogLabel = new JLabel();
 
         //======== this ========
 
         //======== tableScrollPane ========
         {
             tableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 
             //---- table ----
             table.setFillsViewportHeight(true);
             table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
             tableScrollPane.setViewportView(table);
         }
 
         //---- clearButton ----
         clearButton.setText("Clear map");
         clearButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 clearButtonActionPerformed(e);
             }
         });
 
         //---- removeButton ----
         removeButton.setText("Remove selection");
         removeButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 removeButtonActionPerformed(e);
             }
         });
 
         //---- putButton ----
         putButton.setText("Put key-value pair");
         putButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 putButtonActionPerformed(e);
             }
         });
 
         //---- keyField ----
         keyField.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 putButtonActionPerformed(e);
             }
         });
 
         //---- valueField ----
         valueField.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 putButtonActionPerformed(e);
             }
         });
 
         //---- keyLabel ----
         keyLabel.setText("Key:");
 
         //---- valueLabel ----
         valueLabel.setText("Value:");
 
         //======== eventLogScrollPane ========
         {
             eventLogScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
             eventLogScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
             //---- eventLogArea ----
             eventLogArea.setEditable(false);
             eventLogScrollPane.setViewportView(eventLogArea);
         }
 
         //---- eventLogLabel ----
         eventLogLabel.setText("Event log:");
 
         GroupLayout layout = new GroupLayout(this);
         setLayout(layout);
         layout.setHorizontalGroup(
                 layout.createParallelGroup()
                         .addGroup(layout.createSequentialGroup()
                                 .addContainerGap()
                                 .addGroup(layout.createParallelGroup()
                                         .addGroup(layout.createSequentialGroup()
                                                 .addGroup(layout.createParallelGroup()
                                                         .addGroup(layout.createSequentialGroup()
                                                                 .addComponent(tableScrollPane, GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                                         .addComponent(removeButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                         .addComponent(clearButton, GroupLayout.Alignment.TRAILING)
                                                                         .addComponent(putButton, GroupLayout.Alignment.TRAILING)
                                                                         .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                                                 .addComponent(keyLabel)
                                                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                 .addComponent(keyField, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE))
                                                                         .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                                                 .addComponent(valueLabel)
                                                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                 .addComponent(valueField, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE))))
                                                         .addComponent(eventLogScrollPane, GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
                                                 .addGap(6, 6, 6))
                                         .addGroup(layout.createSequentialGroup()
                                                 .addComponent(eventLogLabel)
                                                 .addContainerGap(408, Short.MAX_VALUE))))
         );
         layout.setVerticalGroup(
                 layout.createParallelGroup()
                         .addGroup(layout.createSequentialGroup()
                                 .addContainerGap()
                                 .addGroup(layout.createParallelGroup()
                                         .addGroup(layout.createSequentialGroup()
                                                 .addComponent(clearButton)
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                 .addComponent(removeButton)
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                         .addComponent(keyField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                         .addComponent(keyLabel))
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                         .addComponent(valueField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                         .addComponent(valueLabel))
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                 .addComponent(putButton)
                                                 .addGap(0, 21, Short.MAX_VALUE))
                                         .addComponent(tableScrollPane, GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE))
                                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(eventLogLabel)
                                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(eventLogScrollPane, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
                                 .addContainerGap())
         );
         // JFormDesigner - End of component initialization  //GEN-END:initComponents
     }
 
     // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
     private JScrollPane tableScrollPane;
     private JTable table;
     private JButton clearButton;
     private JButton removeButton;
     private JButton putButton;
     private JTextField keyField;
     private JTextField valueField;
     private JLabel keyLabel;
     private JLabel valueLabel;
     private JScrollPane eventLogScrollPane;
     private JTextArea eventLogArea;
     private JLabel eventLogLabel;
     // JFormDesigner - End of variables declaration  //GEN-END:variables
 }
