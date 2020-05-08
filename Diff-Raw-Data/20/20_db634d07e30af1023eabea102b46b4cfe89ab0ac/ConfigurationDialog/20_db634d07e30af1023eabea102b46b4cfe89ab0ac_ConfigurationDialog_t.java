 package suncertify.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Dialog;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import suncertify.util.ApplicationMode;
 import suncertify.util.PropertyManager;
 
 /**
  *
  * @author William Brosnan
  */
 public class ConfigurationDialog extends JDialog implements ActionListener {
     
     /**
      * Constant to represent property key in properties file
      */
     public static final String DB_PATH = "dbPath";
     /**
      * Constant to represent property key in properties file
      */
     public static final String RMI_HOST = "rmiHost";
     /**
      * Constant to represent property key in properties file
      */
     public static final String RMI_PORT = "rmiPort";
     /**
      * Constant to represent confirmation in the dialog
      */
     private static final String CONFIRM = "Confirm";
     /**
      * Constant to exit out of the entire application
      */
     private static final String KILL = "Kill";
     /**
      * Constant to choose file with <code>JFileChooser</code>
      */
     private static final String BROWSE = "Browse";
     /**
      * JPanel to hold the database selection components
      */
     private JPanel dbPanel;
     /**
      * JPanel to hold the RMI configuration components
      */
     private JPanel rmiPanel;
     /**
      * JPanel to hold confirmation buttons
      */
     private JPanel confirmationPanel;
     /**
      * JLabel for database location
      */
     private JLabel dbLabel;
     /**
      * JTextField for database location
      */
     private JTextField dbField;
     /**
      * JButton for database location, action listener on button
      * will call a JFileChooser making it easier for user to choose file
      * can be used independently of dbField
      */
     private JButton dbButton;
     /**
      * JLabel for port number for RMI server
      */
     private JLabel portLabel;
     /**
      * JTextField to enter RMI port number
      */
     private JTextField portField;
     /**
      * JLabel for host name for RMI
      */
     private JLabel hostLabel;
     /**
      * JTextField for host name location
      */
     private JTextField hostField;
     /**
      * The mode that the application is running in (standalone, server or
      * network client)
      */
     private ApplicationMode appMode;
     /**
      * JButton for confirmation
      */
     private JButton okButton;
     /**
      * JButton to exit the dialog
      */
     private JButton cancelButton;
     /**
      * flag to see if dbPath is valid
      */
     private boolean dbFlag = false;
     /**
      * flag to see if rmiPort is valid
      */
     private boolean portFlag = false;
     /**
      * flag to see if RMI host name is valid
      */
     private boolean hostFlag = false;
     /**
      * Properties instance to get/set properties from properties file
      */
     private PropertyManager properties = PropertyManager.getInstance();
     /**
      * String to hold database location
      */
     private String dbPath = null;
     /**
      * String to hold port number
      */
     private String rmiPort = null;
     /**
      * String to hold dbHost
      */
     private String rmiHost = null;
     
     /**
      * Constructor for class, set parameters for the JFrame on startup
      */
     public ConfigurationDialog(ApplicationMode applicationMode) {
         appMode = applicationMode;
         setTitle("Configure Options");
         //Manually setting size
         setSize(600,200);
         setResizable(false);
         //centers the GUI in the middle of the screen
         setLocationRelativeTo(null);
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);        
         add(loadDBPanel(), BorderLayout.NORTH);
         add(loadRMIPanel(), BorderLayout.CENTER);
         add(loadConfirmationPanel(), BorderLayout.SOUTH);
         initComponents();
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent we) {
                 getEvent(KILL);
             }
         });
     }
     
     /**
      * Load a JPanel with the necessary swing components for the GUI
      * @return JPanel which is added into main JFrame
      */
     private JPanel loadDBPanel() {
         dbPanel = new JPanel();
         dbPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
         dbLabel = new JLabel("Enter database location");
         dbPanel.add(dbLabel);
         //Manually set size of JTextField
         dbField = new JTextField(30);
         dbPanel.add(dbField);
         dbButton = new JButton("Choose File");
         dbButton.setActionCommand(BROWSE);
         dbButton.addActionListener(this);
         dbPanel.add(dbButton);
         portLabel = new JLabel("Enter an RMI port");
         dbPanel.add(portLabel);
         //Manually set size of JTextField
         portField = new JTextField(10);
         dbPanel.add(portField);
         hostLabel = new JLabel("Enter an RMI hostname");
         dbPanel.add(hostLabel);
         //Manually set size of JTextField
         hostField = new JTextField(20);
         dbPanel.add(hostField);
         //returns the JPanel to the class constructor
         return dbPanel;
     }
     
     private JPanel loadRMIPanel() {
         rmiPanel = new JPanel();
         portLabel = new JLabel("Enter an RMI port");
         rmiPanel.add(portLabel);
         //Manually set size of JTextField
         portField = new JTextField("5005", 10);
         rmiPanel.add(portField);
         hostLabel = new JLabel("Enter an RMI hostname");
         rmiPanel.add(hostLabel);
         //Manually set size of JTextField
         hostField = new JTextField(20);
         rmiPanel.add(hostField);
         //returns the JPanel to the class constructor
         return rmiPanel;
     }
     
     private JPanel loadConfirmationPanel() {
         confirmationPanel = new JPanel();
         okButton = new JButton("OK");
         okButton.setActionCommand(CONFIRM);
         okButton.addActionListener(this);
         confirmationPanel.add(okButton);
         cancelButton = new JButton("Cancel");
         cancelButton.setActionCommand(KILL);
         cancelButton.addActionListener(this);
         confirmationPanel.add(cancelButton);
         //returns the JPanel to the class constructor
         return confirmationPanel;
     }
     
     /**
      * Used to populate/disable fields based on application mode
      */
     private void initComponents() {
         dbFlag = false;
         portFlag = false;
         hostFlag = false;
         System.out.println(properties.getProperty("dbPath"));
         dbPath = properties.getProperty("dbPath");
         rmiPort = properties.getProperty("rmiPort");
         rmiHost = properties.getProperty("rmiHost");
         switch (appMode) {
             case ALONE :
                 dbField.setText(dbPath);
                 portField.setEnabled(false);
                 hostField.setEnabled(false);
                 break;
             case SERVER :
                 dbField.setText(dbPath);
                 portField.setText(rmiPort);
                 hostField.setEnabled(false);
                 break;
             case NETWORK :
                 dbField.setEnabled(false);
                 dbButton.setEnabled(false);
                 portField.setText(rmiPort);
                 hostField.setText(rmiHost);
                 break;
             default :
                 throw new UnsupportedOperationException
                         ("Invalid application startup mode");                
         }
     }
     
     public void actionPerformed(ActionEvent ae) {
         getEvent(ae.getActionCommand());
     }
     
     private void getEvent(String event) {
         if (CONFIRM.equals(event)) {
             confirmation();
         }
         else if (BROWSE.equals(event)) {
             chooseFile();            
         }
         else {
             
         }
     }
     
     /**
      * Private class used to listen when the Search button is fired
      * Search will use the entries added in hotel name and location fields
      * as search terms
      */
     
         
     private void chooseFile() {
         JFileChooser fileChooser = new JFileChooser();
         FileNameExtensionFilter filter = new FileNameExtensionFilter(
                 "Database file", "db");
         fileChooser.setFileFilter(filter);
         int returnVal = fileChooser.showOpenDialog(null);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             dbField.setText(fileChooser.getSelectedFile().toString());
         }
     }
 
     private void confirmation() {
         switch (appMode) {
             case ALONE:
                 portFlag = true;
                 hostFlag = true;
                 if (!dbField.getText().equals("")) {
                     File file = new File(dbPath);
                     if (file.exists() && file.isFile()) {
                         dbFlag = true;
                         dbPath = dbField.getText();
                         properties.setProperty("dbPath", dbPath);
                        System.out.println("DBPath: " + dbPath);
                         this.setVisible(false);
                     } else {
                         JOptionPane.showMessageDialog(confirmationPanel,
                                 "Path entered is invalid");
                     }
                 } else {
                     JOptionPane.showMessageDialog(confirmationPanel,
                             "Please enter a path to the local database");
                 }
                 break;
             case SERVER:
 
             case NETWORK:
 
         }
     }
     
     public String getDatabaseLocation() {
         return dbPath;
     }
     
     public String getRMIPort() {
         return rmiPort;
     }
     
     public String getRMIHost() {
         return rmiHost;
     }
 }
 
