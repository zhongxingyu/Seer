 
 package swirc;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import javax.swing.*;
 
 /**
  * Class for showing dialog that lets user connect to a server.
  * @author Janne Kallunki, Ville Hämäläinen, Jaakko Ritvanen
  */
 public class ConnectDialog extends JDialog {
     private SwircModel model;
     private ConnectDialogController controller;
     
     private JComboBox hostName;
     private JSpinner portNumber;
     private JPasswordField serverPsw;
     private JCheckBox showPsw;
     private JTextField nick;
     private boolean confirmed;
     
     /**
      * Constructor for ConnectDialog.
      * Parent needed for relative positioning.
      * @param parent Parent for dialog
      * @param model Model object of Swircs MVC-model
      */
     public ConnectDialog(JFrame parent, SwircModel model) {
         this.model = model;
         controller = new ConnectDialogController(model, this);
         this.confirmed = false;
         Container cp = getContentPane();
         cp.setLayout(new BorderLayout());
         
         JPanel formPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
         
         JPanel inputPane = new JPanel(new GridLayout(9, 1, 5, 5));
         
         inputPane.add(new JLabel("Server address"));
         String[] servers = this.model.getUsedServers();
         if(servers == null) {
             hostName = new JComboBox();
         }
         else {
             hostName = new JComboBox(this.model.getUsedServers()); //TODO check this
         }
         hostName.setEditable(true);
         inputPane.add(hostName);
         
         SpinnerNumberModel numberSpinner = new SpinnerNumberModel(6667, 0, 9999, 1);
         inputPane.add(new JLabel("Port"));
         portNumber = new JSpinner(numberSpinner);
         inputPane.add(portNumber);
         
         inputPane.add(new JLabel("Password"));
         serverPsw = new JPasswordField(20);
         inputPane.add(serverPsw);
         showPsw = new  JCheckBox("Show password");
         inputPane.add(showPsw);
             
         //TODO remember to remove nick from this dialog
         inputPane.add(new JLabel("Nick"));
         nick = new JTextField(20);
         nick.setText(this.model.getUserData("nick"));
         inputPane.add(nick);
         
         formPane.add(inputPane);
         
         
         cp.add(formPane, BorderLayout.CENTER);
         
         JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
         JButton cancel = new JButton("Cancel");
         cancel.setActionCommand("cancel");
         cancel.addActionListener(controller);
         buttonPane.add(cancel);
         
         JButton connect = new JButton("Connect");
         connect.setActionCommand("connect");
         connect.addActionListener(controller);
         buttonPane.add(connect);
         cp.add(buttonPane, BorderLayout.SOUTH);
         
         setModalityType(ModalityType.APPLICATION_MODAL);
 
         setTitle("Connect a server");
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         this.pack();
         setLocationRelativeTo(parent);
     }
     
     /**
      * Method gets servers address.
      * @return Servers address
      */
     public String getServerAddress() {
         String address = hostName.getSelectedItem().toString();
         return (address.length() > 0) ? address : null;
     }
     
     /**
      * Method gets users nickname.
      * @return Users nickname
      */
     public String getNick() {
         return nick.getText();
     }
     
     public String getPassword() {
         return serverPsw.getText();
     }
     
     public String getPort() {
        return (String) portNumber.getValue();
     }
     
     /**
      * 
      * @param c
      */
     public void setConfirmed(boolean c) {
         this.confirmed = c;
     }
     
     /**
      * Method returns true if connecting was confirmed.
      * @return True if proceeding connection
      */
     public boolean isConfirmed() {
         return this.confirmed;
     }
 }
