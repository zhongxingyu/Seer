 package gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 public class CollaborateDialog extends JDialog {
     
     private GridBagConstraints _gridBagConstraints;
     private JFrame _frame;
     private int _mode; //Type of Collaborative Session to Open
     
     public static final int SHARE_NEW = 1;
     public static final int SHARE_EXISTING = 2;
     
     /*
      * Helper method to add the Component to the main Frame with the specified
      * constraints.
      *  @Requires
      *      component != null
      *      x >= 0
      *      y >= 0
      *      gridWidth >= 0
      *      fillMethod is valid gridWidth fillMethod
      *  @Ensures
      *      frame contains the component
      */
     private void addComponent ( Component component, int x, int y, int gridWidth, int fillMethod ) {
         _gridBagConstraints.gridx = x;
         _gridBagConstraints.gridy = y;
         _gridBagConstraints.gridwidth = gridWidth;
         _gridBagConstraints.fill = fillMethod;
         _gridBagConstraints.anchor = GridBagConstraints.EAST;
         _frame.add(component, _gridBagConstraints);
     }
 
 
     public CollaborateDialog ( Frame parent ) {
         _mode = SHARE_NEW;
         _frame = new JFrame("Collaborate");
         _gridBagConstraints = new GridBagConstraints();
         _gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
         _frame.setLayout(new GridBagLayout());
         _frame.setPreferredSize(new java.awt.Dimension(400, 250));
 
         JLabel modeLabel = new JLabel("Collaborate By");
         addComponent(modeLabel, 0, 0, 1, GridBagConstraints.NONE);
 
         final JComboBox modeComboBox = new JComboBox();
         String[] options = new String[]{"Share Current Document", "Connect to Shared Document"};
         modeComboBox.setModel(new javax.swing.DefaultComboBoxModel(options));
         addComponent(modeComboBox, 1, 0, 1, GridBagConstraints.NONE);
 
         JLabel serverURLLabel = new JLabel("Server URL");
         addComponent(serverURLLabel, 0, 1, 1, GridBagConstraints.NONE);
 
         JTextField serverURLField = new JTextField();
         addComponent(serverURLField, 1, 1, 7, GridBagConstraints.HORIZONTAL);
  
         JLabel serverPortLabel = new JLabel("Server Port");
         addComponent(serverPortLabel, 0, 2, 1, GridBagConstraints.NONE);
 
         JTextField serverPortField = new JTextField();
         addComponent(serverPortField, 1, 2, 7, GridBagConstraints.HORIZONTAL);
         
         JLabel documentNameLabel = new JLabel("Document Name");
         addComponent(documentNameLabel, 0, 3, 1, GridBagConstraints.NONE);
 
         JTextField documentNameField = new JTextField();
         addComponent(documentNameField, 1, 3, 7, GridBagConstraints.HORIZONTAL);
 
         JLabel userIDLabel = new JLabel("User ID");
         addComponent(userIDLabel, 0, 4, 1, GridBagConstraints.NONE);
 
         JTextField userIDField = new JTextField();
         addComponent(userIDField, 1, 4, 5, GridBagConstraints.HORIZONTAL);
 
         JLabel documentPasswordLabel = new JLabel("Document Password");
         addComponent(documentPasswordLabel, 0, 5, 1, GridBagConstraints.NONE);
 
         JPasswordField docPasswordField = new JPasswordField();
         addComponent(docPasswordField, 1, 5, 5, GridBagConstraints.HORIZONTAL);
 
         final JLabel confirmDocumentPasswordLabel = new JLabel("Confirm Password");
         addComponent(confirmDocumentPasswordLabel, 0, 6, 1, GridBagConstraints.NONE);
 
         final JPasswordField confirmDocumentPasswordField = new JPasswordField();
         addComponent(confirmDocumentPasswordField, 1, 6, 5, GridBagConstraints.HORIZONTAL);
 
         JButton okButton = new JButton("OK");
         addComponent(okButton, 4, 7, 1, GridBagConstraints.NONE);
 
         JButton cancelButton = new JButton("Cancel");
         addComponent(cancelButton, 5, 7, 1, GridBagConstraints.NONE);
 
        _frame.setSize(500, 270);
         _frame.setVisible(true);
 
         modeComboBox.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 String optionName = modeComboBox.getSelectedItem().toString();
                 if (optionName == "Share Current Document") {
                     //Enable the confirm password label and text field
                     confirmDocumentPasswordLabel.setEnabled(true);
                     confirmDocumentPasswordField.setEnabled(true);
                     _mode = SHARE_NEW;
                 } else if (optionName == "Connect to Shared Document") {
                     //Disable the confirm password label and text field
                     confirmDocumentPasswordLabel.setEnabled(false);
                     confirmDocumentPasswordField.setEnabled(false);
                     //Clear the field when disabled
                     confirmDocumentPasswordField.setText("");
                     _mode = SHARE_EXISTING;
                 }
             }
         });
 
     }
 
 }
