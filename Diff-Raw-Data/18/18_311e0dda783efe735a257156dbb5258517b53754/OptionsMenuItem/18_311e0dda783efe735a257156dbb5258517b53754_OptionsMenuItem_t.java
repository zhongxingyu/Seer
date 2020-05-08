 package dk.frv.eavdam.menus;
 
 import dk.frv.eavdam.data.FTP;
 import dk.frv.eavdam.data.Options;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.xml.bind.JAXBException;
 
 /**
  * This class represents a menu item that opens a frame where the user can edit
  * application's options.
  */
 public class OptionsMenuItem extends JMenuItem {
 
     public static final long serialVersionUID = 3L;
 
     public OptionsMenuItem(EavdamMenu eavdamMenu) {        
         super("Settings");                
         addActionListener(new OptionsActionListener(this, eavdamMenu));
     }
     
     /**
      * Loads options
      *
      * @return  Options
      */
     public static Options loadOptions() {
 
         Options op = new Options();
 
         StringBuilder contents = new StringBuilder();
     
         try {
             BufferedReader input =  new BufferedReader(new FileReader("data/settings.txt"));
             try {
                 String line = null;
                 List<FTP> ftps = new ArrayList<FTP>();                
                 while ((line = input.readLine()) != null) {
                     if (line.startsWith("ftp:")) {
                         String temp = line.substring(4).trim();
                         String[] arr = temp.split(";");
                         if (arr.length == 4) {
                             FTP ftp = new FTP(arr[0], arr[1], arr[2], arr[3]);
                             ftps.add(ftp);
                         }                        
                     } else if (line.startsWith("email-to:")) {
                         op.setEmailTo(line.substring(9).trim());                        
                     } else if (line.startsWith("email-from:")) {
                         op.setEmailFrom(line.substring(11).trim());                            
                     } else if (line.startsWith("email-subject:")) {
                         op.setEmailSubject(line.substring(14).trim());                            
                     } else if (line.startsWith("email-host:")) {                                                
                         op.setEmailHost(line.substring(11).trim());                            
                     } else if (line.startsWith("email-auth:")) {
                         String temp = line.substring(11).trim();
                         if (temp.equals("true")) {
                             op.setEmailAuth(true);
                         } else if (temp.equals("false")) {
                             op.setEmailAuth(false);
                         }
                     } else if (line.startsWith("email-username:")) {
                         op.setEmailUsername(line.substring(15).trim());    
                     } else if (line.startsWith("email-password:")) {
                         op.setEmailPassword(line.substring(15).trim());                                                    
                     } else if (line.startsWith("icons:")) {
                         String temp = line.substring(7).trim();
                         if (temp.equals("large")) {
                             op.setIconsSize(Options.LARGE_ICONS);
                         } else if (temp.equals("small")) {
                             op.setIconsSize(Options.SMALL_ICONS);
                         }
                     }
                 }
                 op.setFTPs(ftps);
             } finally {
                 input.close();
             }    
         } catch (IOException ex){
             System.out.println("Settings file could not be loaded");
         }
     
         return op;
     }    
 
 }
  
 class OptionsActionListener implements ActionListener, ChangeListener, DocumentListener {
 
     private EavdamMenu eavdamMenu;
                                                          
     private JDialog dialog;
 
     private JTabbedPane tabbedPane;
 
     private JPanel emailPane;
     private JPanel ftpPane;
     private JPanel visualPane;
 
     private JComboBox ftpServersComboBox;
     private int currentlySelectedFTPServerIndex = -1;
     private JButton addFTPServerButton;
     private JButton deleteFTPServerButton;
     private JLabel ftpServerLabel;
     private JTextField ftpServerTextField;
     private JLabel ftpDirectoryLabel;
     private JTextField ftpDirectoryTextField;
     private JLabel ftpUsernameLabel;
     private JTextField ftpUsernameTextField;
     private JLabel ftpPasswordLabel;
     private JPasswordField ftpPasswordTextField;
 
     private JDialog ftpDialog;
     private JTextField newFTPServerTextField;
     private JTextField newFTPDirectoryTextField;
     private JTextField newFTPUsernameTextField;
     private JPasswordField newFTPPasswordTextField;            
     private JButton addNewFTPButton;
     private JButton cancelNewFTPButton;
 
     private JTextArea emailToTextArea;
     private JTextField emailFromTextField;
     private JTextField emailSubjectTextField;
     private JTextField emailHostTextField;
     private JComboBox emailAuthComboBox;
     private JLabel emailUsernameLabel;    
     private JTextField emailUsernameTextField;
     private JLabel emailPasswordLabel;
     private JPasswordField emailPasswordTextField;
     
     private JComboBox iconsSizeComboBox;
 
     private JButton saveButton;
     private JButton cancelButton;
     
     private Options options;                   
     
     private List<FTP> ftps;
     
     private OptionsMenuItem optionsMenuItem;
               
     public OptionsActionListener(OptionsMenuItem optionsMenuItem, EavdamMenu eavdamMenu) {
         super();
         this.optionsMenuItem = optionsMenuItem;
         this.eavdamMenu = eavdamMenu;
     }
                    
     public void actionPerformed(ActionEvent e) {
         
         if (e.getSource() instanceof OptionsMenuItem) {
 
             dialog = new JDialog(eavdamMenu.getOpenMapFrame(), "Settings", true);
         
             options = optionsMenuItem.loadOptions();  
            if (options == null || options.getFTPs() == null) {
                ftps = new ArrayList<FTP>();
            } else {
                ftps = new ArrayList<FTP>(options.getFTPs());
            }
 
             ftpServersComboBox = null;
             ftpServerTextField = null;
             ftpDirectoryTextField = null;
             ftpUsernameTextField = null;
             ftpPasswordTextField = null;
             emailToTextArea = null;
             emailFromTextField = null;
             emailSubjectTextField = null;
             emailHostTextField = null;
             emailAuthComboBox = null;
             emailUsernameTextField = null;
             emailPasswordTextField = null;
             iconsSizeComboBox = null;
 
             createButtons();             
             emailPane = getEmailPane();
             ftpPane = getFTPPane(0);
             visualPane = getVisualPane();            
             
             JPanel contentPane = new JPanel();
 
             tabbedPane = new JTabbedPane();
             tabbedPane.addTab("E-mail", null, new JPanel(), "E-mail");
             tabbedPane.addTab("FTP", null, new JPanel(), "FTP");            
             tabbedPane.addTab("Visual", null, new JPanel(), "GUI");
             tabbedPane.setSelectedIndex(1);
             tabbedPane.addChangeListener(this);
             tabbedPane.setSelectedIndex(0);
             
             contentPane.add(tabbedPane, BorderLayout.NORTH);
 
             dialog.getContentPane().add(contentPane);
                                                                          
             int frameWidth = 420;
             int frameHeight = 380;
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             dialog.setBounds((int) screenSize.getWidth()/2 - frameWidth/2,
                 (int) screenSize.getHeight()/2 - frameHeight/2, frameWidth, frameHeight);
             dialog.setVisible(true);
         
         } else if (e.getSource() == emailAuthComboBox) {        
             if (((String) emailAuthComboBox.getSelectedItem()).equals("Yes")) {
                 emailUsernameLabel.setVisible(true);
                 emailUsernameTextField.setVisible(true);
                 emailPasswordLabel.setVisible(true);
                 emailPasswordTextField.setVisible(true);                
             } else {
                 emailUsernameLabel.setVisible(false);
                 emailUsernameTextField.setText("");
                 emailUsernameTextField.setVisible(false);
                 emailPasswordLabel.setVisible(false);
                 emailPasswordTextField.setText("");
                 emailPasswordTextField.setVisible(false);                
             }
             
         } else if (e.getSource() == addFTPServerButton) {
         
             ftpDialog = new JDialog(dialog, "Add new FTP site", true);        
         
             JPanel panel = new JPanel();
             panel.setLayout(new GridBagLayout());   
 
             GridBagConstraints c = new GridBagConstraints();    
             c.gridx = 0;
             c.gridy = 0;
             c.anchor = GridBagConstraints.FIRST_LINE_START;
             c.insets = new Insets(5,5,5,5);
             panel.add(new JLabel("Server name: "), c);
             newFTPServerTextField = new JTextField(16);            
             c.gridx = 1;
             panel.add(newFTPServerTextField, c);  
     
             c.gridx = 0;
             c.gridy = 1;
             panel.add(new JLabel("Directory: "), c);
             newFTPDirectoryTextField = new JTextField(16);            
             c.gridx = 1;
             panel.add(newFTPDirectoryTextField, c);     
     
             c.gridx = 0;
             c.gridy = 2;
             panel.add(new JLabel("Username: "), c);
             newFTPUsernameTextField = new JTextField(16);            
             c.gridx = 1;
             panel.add(newFTPUsernameTextField, c);  
             
             c.gridx = 0;
             c.gridy = 3;
             panel.add(new JLabel("Password: "), c);
             newFTPPasswordTextField = new JPasswordField(16);            
             c.gridx = 1;
             panel.add(newFTPPasswordTextField, c);             
                     
             JPanel buttonPanel = new JPanel();
             buttonPanel.setLayout(new GridBagLayout());
             c.gridx = 0;
             c.gridy = 0;
             buttonPanel.add(addNewFTPButton, c);
             c.gridx = 1;
             buttonPanel.add(cancelNewFTPButton, c);                    
             c.gridx = 0;
             c.gridy = 4;
             c.gridwidth = 2;
             c.anchor = GridBagConstraints.CENTER;                    
             panel.add(buttonPanel, c);  
             
             ftpDialog.getContentPane().add(panel);
                                                                          
             int frameWidth = 300;
             int frameHeight = 300;
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             ftpDialog.setBounds((int) screenSize.getWidth()/2 - frameWidth/2,
                 (int) screenSize.getHeight()/2 - frameHeight/2, frameWidth, frameHeight);
             ftpDialog.setVisible(true);
             
         } else if (e.getSource() == addNewFTPButton) {
             
             if (newFTPServerTextField.getText().isEmpty() || newFTPDirectoryTextField.getText().isEmpty() ||
                     (new String(newFTPPasswordTextField.getPassword())).isEmpty()) {
                 JOptionPane.showMessageDialog(dialog, "All fields except directory are mandatory!");
             } else {
                 FTP ftp = new FTP(newFTPServerTextField.getText(), newFTPDirectoryTextField.getText(),
                     newFTPUsernameTextField.getText(), new String(newFTPPasswordTextField.getPassword()));
                 ftps.add(ftp);
                 ftpServersComboBox = null;
                 ftpServerTextField = null;
                 ftpDirectoryTextField = null;
                 ftpUsernameTextField = null;
                 ftpPasswordTextField = null;
                 deleteFTPServerButton.setVisible(true);
                 ftpPane = getFTPPane(ftps.size()-1);
                 tabbedPane.setComponentAt(1, ftpPane); 
                 ftpDialog.dispose();
             }
         
         } else if (e.getSource() == ftpServersComboBox) {
                     
             ftpServerTextField.getDocument().removeDocumentListener(this);
             ftpDirectoryTextField.getDocument().removeDocumentListener(this);
             ftpUsernameTextField.getDocument().removeDocumentListener(this);
             ftpPasswordTextField.getDocument().removeDocumentListener(this);                        
             FTP ftp = new FTP(ftpServerTextField.getText(), ftpDirectoryTextField.getText(),
                     ftpUsernameTextField.getText(), new String(ftpPasswordTextField.getPassword()));
             ftps.set(currentlySelectedFTPServerIndex, ftp);
             ftpServerTextField.setText(ftps.get(ftpServersComboBox.getSelectedIndex()).getServer());
             ftpDirectoryTextField.setText(ftps.get(ftpServersComboBox.getSelectedIndex()).getDirectory());
             ftpUsernameTextField.setText(ftps.get(ftpServersComboBox.getSelectedIndex()).getUsername());
             ftpPasswordTextField.setText(ftps.get(ftpServersComboBox.getSelectedIndex()).getPassword());     
             currentlySelectedFTPServerIndex = ftpServersComboBox.getSelectedIndex();
             ftpServerTextField.getDocument().addDocumentListener(this);
             ftpDirectoryTextField.getDocument().addDocumentListener(this);
             ftpUsernameTextField.getDocument().addDocumentListener(this);
             ftpPasswordTextField.getDocument().addDocumentListener(this);   
             
         } else if (e.getSource() == cancelNewFTPButton) {
             
             if (!newFTPServerTextField.getText().isEmpty() || !newFTPDirectoryTextField.getText().isEmpty() ||
                     !newFTPDirectoryTextField.getText().isEmpty() || !(new String(newFTPPasswordTextField.getPassword())).isEmpty()) {
                 int response = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to cancel adding a new FTP site?", "Confirm action", JOptionPane.YES_NO_OPTION);
                 if (response == JOptionPane.YES_OPTION) {
                     ftpDialog.dispose();
                 } else {
                     // do nothing
                 }
             } else {
                 ftpDialog.dispose();
             }
             
         } else if (e.getSource() == deleteFTPServerButton) {
             
                 int response = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to delete the currently open FTP site?", "Confirm action", JOptionPane.YES_NO_OPTION);
                 if (response == JOptionPane.YES_OPTION) {
                     ftps.remove(ftpServersComboBox.getSelectedIndex());
                     ftpServersComboBox = null;
                     ftpServerTextField = null;
                     ftpDirectoryTextField = null;
                     ftpUsernameTextField = null;
                     ftpPasswordTextField = null;
                     ftpPane = getFTPPane(0);
                     tabbedPane.setComponentAt(1, ftpPane);                                
                 } else {
                     // do nothing
                 }
             
         } else if (saveButton != null && e.getSource() == saveButton) {
             
             if (emailToTextArea.getText().isEmpty() || emailFromTextField.getText().isEmpty() ||
                     emailSubjectTextField.getText().isEmpty() || emailHostTextField.getText().isEmpty() ||
                     (((String) emailAuthComboBox.getSelectedItem()).equals("Yes") &&
                     (emailUsernameTextField.getText().isEmpty() || new String(emailPasswordTextField.getPassword()).isEmpty()))) {
                 JOptionPane.showMessageDialog(dialog, "All e-mail fields are mandatory!");
                 return;                        
             }
             for (FTP ftp : ftps) {       
                 if (ftp.getServer().isEmpty() || ftp.getUsername().isEmpty() || ftp.getPassword().isEmpty()) {
                     JOptionPane.showMessageDialog(dialog, "All FTP fields except directory are mandatory!");
                     return;
                 }
             }
             
             boolean success = saveOptions();
             
             if (success) {
                 saveButton.setEnabled(false);
                 eavdamMenu.getStationLayer().updateStations();
                 dialog.dispose();
             }
 
         } else if (cancelButton != null && e.getSource() == cancelButton) {
             if (saveButton.isEnabled()) {
                 int response = JOptionPane.showConfirmDialog(dialog, "Do you want to save the changes before exiting?", "Confirm action", JOptionPane.YES_NO_OPTION);
                 if (response == JOptionPane.YES_OPTION) {
                     boolean success = saveOptions();
                     if (success) {
                         dialog.dispose();
                     }
                 } else if (response == JOptionPane.NO_OPTION) {                    
                     dialog.dispose();
                 }
             } else {
                 dialog.dispose();                  
             }
         
         } else if (saveButton != null) {
             if (isChanged()) {
                 saveButton.setEnabled(true);
             } else {
                 saveButton.setEnabled(false);
             }
         }
     }
     
     private void createButtons() {
     
         saveButton = new JButton("Save and exit", null);        
         saveButton.setVerticalTextPosition(AbstractButton.BOTTOM);
         saveButton.setHorizontalTextPosition(AbstractButton.CENTER);
         saveButton.setPreferredSize(new Dimension(130, 20));
         saveButton.setMaximumSize(new Dimension(130, 20));
         saveButton.addActionListener(this);
             
         cancelButton = new JButton("Cancel", null);        
         cancelButton.setVerticalTextPosition(AbstractButton.BOTTOM);
         cancelButton.setHorizontalTextPosition(AbstractButton.CENTER);
         cancelButton.setPreferredSize(new Dimension(100, 20));
         cancelButton.setMaximumSize(new Dimension(100, 20));                              
         cancelButton.addActionListener(this);
 
         addFTPServerButton = new JButton("Add new", null);
         addFTPServerButton.setVerticalTextPosition(AbstractButton.BOTTOM);
         addFTPServerButton.setHorizontalTextPosition(AbstractButton.CENTER);
         addFTPServerButton.setPreferredSize(new Dimension(100, 20));
         addFTPServerButton.setMaximumSize(new Dimension(100, 20));
         addFTPServerButton.addActionListener(this);    
 
         deleteFTPServerButton = new JButton("Delete", null);
         deleteFTPServerButton.setVerticalTextPosition(AbstractButton.BOTTOM);
         deleteFTPServerButton.setHorizontalTextPosition(AbstractButton.CENTER);
         deleteFTPServerButton.setPreferredSize(new Dimension(100, 20));
         deleteFTPServerButton.setMaximumSize(new Dimension(100, 20));
         deleteFTPServerButton.addActionListener(this);     
         
         addNewFTPButton = new JButton("Add", null);
         addNewFTPButton.setVerticalTextPosition(AbstractButton.BOTTOM);
         addNewFTPButton.setHorizontalTextPosition(AbstractButton.CENTER);
         addNewFTPButton.setPreferredSize(new Dimension(60, 20));
         addNewFTPButton.setMaximumSize(new Dimension(60, 20));
         addNewFTPButton.addActionListener(this);   
         
         cancelNewFTPButton = new JButton("Cancel", null);
         cancelNewFTPButton.setVerticalTextPosition(AbstractButton.BOTTOM);
         cancelNewFTPButton.setHorizontalTextPosition(AbstractButton.CENTER);
         cancelNewFTPButton.setPreferredSize(new Dimension(100, 20));
         cancelNewFTPButton.setMaximumSize(new Dimension(100, 20));
         cancelNewFTPButton.addActionListener(this);                      
     }    
     
     private JPanel getEmailPane() {
         
         JPanel panel = new JPanel();
         panel.setLayout(new GridBagLayout());   
 
         GridBagConstraints c = new GridBagConstraints();
         c.gridx = 0;
         c.gridy = 0;                   
         c.anchor = GridBagConstraints.FIRST_LINE_START;
         c.insets = new Insets(5,5,0,5);
         
         panel.add(new JLabel("To: "), c);
         if (emailToTextArea == null) {
             emailToTextArea = new JTextArea(options.getEmailTo());
             emailToTextArea.setLineWrap(true);
             emailToTextArea.setWrapStyleWord(true);
             emailToTextArea.getDocument().addDocumentListener(this);
         }
         JScrollPane scrollPane = new JScrollPane(emailToTextArea);
         scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         scrollPane.setPreferredSize(new Dimension(180, 40));
         scrollPane.setMaximumSize(new Dimension(180, 40));        
         c.anchor = GridBagConstraints.LINE_START;
         c.gridx = 1;
         panel.add(scrollPane, c);       
 
         c.gridy = 1;
         c.insets = new Insets(0,5,5,0);        
         JLabel advice = new JLabel("Use comma (,) to separate multiple addresses");
         advice.setFont(new Font("Serif", Font.PLAIN, 10));
         panel.add(advice, c);       
 
         c.gridx = 0;        
         c.gridy = 2;
         c.insets = new Insets(5,5,5,5);        
         panel.add(new JLabel("From: "), c);
         if (emailFromTextField == null) {
             emailFromTextField = new JTextField(16);
             emailFromTextField.setText(options.getEmailFrom());
             emailFromTextField.getDocument().addDocumentListener(this);
         }
         c.gridx = 1;
         panel.add(emailFromTextField, c);
 
         c.gridx = 0;        
         c.gridy = 3;
         panel.add(new JLabel("Subject: "), c);
         if (emailSubjectTextField == null) {
             emailSubjectTextField = new JTextField(16);
             emailSubjectTextField.setText(options.getEmailSubject());
             emailSubjectTextField.getDocument().addDocumentListener(this);
         }
         c.gridx = 1;
         panel.add(emailSubjectTextField, c);
         
         c.gridx = 0;        
         c.gridy = 4;
         panel.add(new JLabel("SMTP server address: "), c);
         if (emailHostTextField == null) {
             emailHostTextField = new JTextField(16);
             emailHostTextField.setText(options.getEmailHost());
             emailHostTextField.getDocument().addDocumentListener(this);
         }
         c.gridx = 1;
         panel.add(emailHostTextField, c);
         
         c.gridx = 0;        
         c.gridy = 5;
         panel.add(new JLabel("Requires authentication: "), c);        
         if (emailAuthComboBox == null) {
             emailAuthComboBox = new JComboBox(new String[] {"No", "Yes"});
             if (!options.isEmailAuth()) {
                 emailAuthComboBox.setSelectedIndex(0);
             } else {
                 emailAuthComboBox.setSelectedIndex(1);
             }        
             emailAuthComboBox.addActionListener(this);
         }
         c.gridx = 1;
         panel.add(emailAuthComboBox, c);
         
         c.gridx = 0;        
         c.gridy = 6;
         emailUsernameLabel = new JLabel("Username: ");
         panel.add(emailUsernameLabel, c);
         if (emailUsernameTextField == null) {
             emailUsernameTextField = new JTextField(16);
             emailUsernameTextField.setText(options.getEmailUsername());
             emailUsernameTextField.getDocument().addDocumentListener(this);
         }
         c.gridx = 1;
         panel.add(emailUsernameTextField, c);    
         if (!options.isEmailAuth()) {
             emailUsernameLabel.setVisible(false);
             emailUsernameTextField.setVisible(false);
         }
         
         c.gridx = 0;        
         c.gridy = 7;
         emailPasswordLabel = new JLabel("Password: ");
         panel.add(emailPasswordLabel, c);
         if (emailPasswordTextField == null) {
             emailPasswordTextField = new JPasswordField(16);
             emailPasswordTextField.setText(options.getEmailPassword());
             emailPasswordTextField.getDocument().addDocumentListener(this);
         }
         c.gridx = 1;
         panel.add(emailPasswordTextField, c);    
         if (!options.isEmailAuth()) {
             emailPasswordLabel.setVisible(false);
             emailPasswordTextField.setVisible(false);
         }        
 
         JPanel buttonPanel = new JPanel();
         buttonPanel.setLayout(new GridBagLayout());
         c.gridx = 0;
         c.gridy = 1;
         buttonPanel.add(saveButton, c);
         if (!isChanged()) {
             saveButton.setEnabled(false);
         } else {
             saveButton.setEnabled(true);
         }
         c.gridx = 1;
         buttonPanel.add(cancelButton, c);                    
         c.gridx = 0;
         c.gridy = 8;
         c.gridwidth = 2;
         c.anchor = GridBagConstraints.CENTER;                    
         panel.add(buttonPanel, c);  
         
         return panel;        
     }
 
     private JPanel getFTPPane(int selectedIndex) {
         
         JPanel panel = new JPanel();
         panel.setLayout(new GridBagLayout());   
 
         GridBagConstraints c = new GridBagConstraints();
         c.gridx = 0;
         c.gridy = 0;                   
         c.anchor = GridBagConstraints.FIRST_LINE_START;
         c.insets = new Insets(5,5,5,5);
 
         if (ftpServersComboBox == null) {
             ftpServersComboBox = new JComboBox();            
             for (FTP ftp : ftps) {
                 ftpServersComboBox.addItem(ftp.getServer());
             }
         }
         ftpServersComboBox.removeActionListener(this);
         if (selectedIndex < ftps.size()) {
             ftpServersComboBox.setSelectedIndex(selectedIndex);
             currentlySelectedFTPServerIndex = selectedIndex;
         }
         ftpServersComboBox.addActionListener(this);
         c.anchor = GridBagConstraints.LINE_START;
         panel.add(ftpServersComboBox, c);
         
         JPanel ftpButtonPanel = new JPanel();
         ftpButtonPanel.setLayout(new GridBagLayout());
         ftpButtonPanel.add(addFTPServerButton, c);
         c.gridx = 1;
         ftpButtonPanel.add(deleteFTPServerButton, c);
         panel.add(ftpButtonPanel, c);
 
         c.gridx = 0;
         c.gridy = 1;
         ftpServerLabel = new JLabel("Server name: ");
         panel.add(ftpServerLabel, c);
         if (ftpServerTextField == null) {
             ftpServerTextField = new JTextField(16);
             if (selectedIndex < ftps.size()) {
                 ftpServerTextField.setText(ftps.get(selectedIndex).getServer());
             }            
             ftpServerTextField.getDocument().addDocumentListener(this);
         }
         c.gridx = 1;
         panel.add(ftpServerTextField, c);  
 
         c.gridx = 0;        
         c.gridy = 2;
         ftpDirectoryLabel = new JLabel("Directory: ");
         panel.add(ftpDirectoryLabel, c);
         if (ftpDirectoryTextField == null) {
             ftpDirectoryTextField = new JTextField(16);
             if (selectedIndex < ftps.size()) {
                 ftpDirectoryTextField.setText(ftps.get(selectedIndex).getDirectory());
             }    
             ftpDirectoryTextField.getDocument().addDocumentListener(this);
         }
         c.gridx = 1;
         panel.add(ftpDirectoryTextField, c);  
         
         c.gridx = 0;        
         c.gridy = 3;
         ftpUsernameLabel = new JLabel("Username: ");
         panel.add(ftpUsernameLabel, c);
         if (ftpUsernameTextField == null) {
             ftpUsernameTextField = new JTextField(16);
             if (selectedIndex < ftps.size()) {
                 ftpUsernameTextField.setText(ftps.get(selectedIndex).getUsername());
             }
             ftpUsernameTextField.getDocument().addDocumentListener(this);
         }
         c.gridx = 1;
         panel.add(ftpUsernameTextField, c);  
         
         c.gridx = 0;        
         c.gridy = 4;
         ftpPasswordLabel = new JLabel("Password: ");
         panel.add(ftpPasswordLabel, c);
         if (ftpPasswordTextField == null) {
             ftpPasswordTextField = new JPasswordField(16);
             if (selectedIndex < ftps.size()) {
                 ftpPasswordTextField.setText(ftps.get(selectedIndex).getPassword());
             }
             ftpPasswordTextField.getDocument().addDocumentListener(this);
         }
         c.gridx = 1;
         panel.add(ftpPasswordTextField, c);                  
 
         if (ftps.isEmpty()) {
             currentlySelectedFTPServerIndex = -1;
             ftpServersComboBox.setVisible(false);
             deleteFTPServerButton.setVisible(false);
             ftpServerLabel.setVisible(false);
             ftpServerTextField.setVisible(false);
             ftpDirectoryLabel.setVisible(false);
             ftpDirectoryTextField.setVisible(false);
             ftpUsernameLabel.setVisible(false);
             ftpUsernameTextField.setVisible(false);
             ftpPasswordLabel.setVisible(false);
             ftpPasswordTextField.setVisible(false);                                                                                                
         }
 
         JPanel buttonPanel = new JPanel();
         buttonPanel.setLayout(new GridBagLayout());
         c.gridx = 0;
         c.gridy = 0;
         buttonPanel.add(saveButton, c);
         if (!isChanged()) {
             saveButton.setEnabled(false);
         } else {
             saveButton.setEnabled(true);
         }
         c.gridx = 1;
         buttonPanel.add(cancelButton, c);                    
         c.gridx = 0;
         c.gridy = 5;
         c.gridwidth = 2;
         c.anchor = GridBagConstraints.CENTER;                    
         panel.add(buttonPanel, c);  
         
         return panel; 
     }        
 
     private JPanel getVisualPane() {
     
         if (iconsSizeComboBox == null) {
             iconsSizeComboBox = new JComboBox(new String[] {"Large", "Small"});                     
             if (options.getIconsSize() == Options.LARGE_ICONS) {
                 iconsSizeComboBox.setSelectedIndex(0);
             } else if (options.getIconsSize() == Options.SMALL_ICONS) {
                 iconsSizeComboBox.setSelectedIndex(1);
             }            
             iconsSizeComboBox.addActionListener(this);
         }
         
         JPanel panel = new JPanel();
         panel.setLayout(new GridBagLayout());                  
         
         JPanel p1 = new JPanel(new GridBagLayout());
         p1.setBorder(BorderFactory.createTitledBorder("Icons"));
         GridBagConstraints c = new GridBagConstraints();
         c.gridx = 0;
         c.gridy = 0;                   
         c.anchor = GridBagConstraints.LINE_START;
         c.insets = new Insets(5,5,5,5);
         p1.add(new JLabel("Icons size:"), c);
         c.gridx = 1;                
         p1.add(iconsSizeComboBox, c);           
         
         c.gridx = 0;
         c.gridy = 0;
         c.anchor = GridBagConstraints.FIRST_LINE_START;
         panel.add(p1, c);
 
         JPanel buttonPanel = new JPanel();
         buttonPanel.add(saveButton);
         if (!isChanged()) {
             saveButton.setEnabled(false);
         } else {
             saveButton.setEnabled(true);
         }
         buttonPanel.add(cancelButton);                    
         c.gridx = 0;
         c.gridy = 1;
         c.gridwidth = 2;
         c.anchor = GridBagConstraints.CENTER;                    
         panel.add(buttonPanel, c);  
         
         JPanel contentPane = new JPanel();
         contentPane.add(panel, BorderLayout.NORTH);
         
         return contentPane;
     }
 
     public void stateChanged(ChangeEvent evt) {
         int sel = tabbedPane.getSelectedIndex();    
         if (sel == 0) {  // e-mail            
             emailPane = getEmailPane();
             tabbedPane.setComponentAt(0, emailPane);        
         } else if (sel == 1) {  // ftp
             ftpPane = getFTPPane(0);
             tabbedPane.setComponentAt(1, ftpPane); 
         } else if (sel == 2) {  // gui
             visualPane = getVisualPane();
             tabbedPane.setComponentAt(2, visualPane);  
         } 
     }
 
     public void changedUpdate(DocumentEvent e) {
         if (saveButton != null) {
             if (isChanged()) {
                 saveButton.setEnabled(true);
             } else {
                 saveButton.setEnabled(false);
             }            
         }
         if (e.getDocument() == ftpServerTextField.getDocument() || e.getDocument() == ftpDirectoryTextField.getDocument() ||
                 e.getDocument() == ftpUsernameTextField.getDocument() || e.getDocument() == ftpPasswordTextField.getDocument()) {
             updateCurrentFTP();       
         }
     }  
     
     public void insertUpdate(DocumentEvent e) {
         if (saveButton != null) {
             if (isChanged()) {
                 saveButton.setEnabled(true);
             } else {
                 saveButton.setEnabled(false);
             }
         }
         if (e.getDocument() == ftpServerTextField.getDocument() || e.getDocument() == ftpDirectoryTextField.getDocument() ||
                 e.getDocument() == ftpUsernameTextField.getDocument() || e.getDocument() == ftpPasswordTextField.getDocument()) {
             updateCurrentFTP();
         }
     } 
     
     public void removeUpdate(DocumentEvent e) {
         if (saveButton != null) {
             if (isChanged()) {
                 saveButton.setEnabled(true);
             } else {
                 saveButton.setEnabled(false);
             }
         }
         if (e.getDocument() == ftpServerTextField.getDocument() || e.getDocument() == ftpDirectoryTextField.getDocument() ||
                 e.getDocument() == ftpUsernameTextField.getDocument() || e.getDocument() == ftpPasswordTextField.getDocument()) {
             updateCurrentFTP();       
         }
     }
     
     private void updateCurrentFTP() {
         FTP ftp = new FTP(ftpServerTextField.getText(), ftpDirectoryTextField.getText(),
             ftpUsernameTextField.getText(), new String(ftpPasswordTextField.getPassword()));
         ftps.set(currentlySelectedFTPServerIndex, ftp);
         if (!ftpServerTextField.getText().equals((String) ftpServersComboBox.getSelectedItem())) {
             ftpServersComboBox.removeActionListener(this);
             ftpServersComboBox.removeItemAt(currentlySelectedFTPServerIndex);
             if (ftpServersComboBox.getItemCount() > 0) {
                 ftpServersComboBox.insertItemAt(ftpServerTextField.getText(), currentlySelectedFTPServerIndex);
             } else {
                 ftpServersComboBox.addItem(ftpServerTextField.getText());                
             }
             ftpServersComboBox.addActionListener(this);
         }
     }
 
     /** 
      * Saves options.
      *
      * @return  True if the save was succesful, false otherwise
      */        
     private boolean saveOptions() {
 
         try {
             File file = new File("data");
             if (!file.exists()) {
                 file.mkdir();
             }
             String data = "";
             for (FTP ftp : ftps) {       
                 if (!ftp.getServer().isEmpty() && !ftp.getUsername().isEmpty() && !ftp.getPassword().isEmpty()) {
                     data += "ftp:" + ftp.getServer() + ";" + ftp.getDirectory() + ";" + ftp.getUsername() + ";" + ftp.getPassword() + "\n";
                 }
             }
             data += "email-to: " + emailToTextArea.getText() + "\n" +
                 "email-from: " + emailFromTextField.getText() + "\n" +
                 "email-subject: " + emailSubjectTextField.getText() + "\n" +
                 "email-host: " + emailHostTextField.getText() + "\n" +
                 "email-auth: ";
             if (((String) emailAuthComboBox.getSelectedItem()).equals("Yes")) {
                 data += "true\n";
             } else if (((String) emailAuthComboBox.getSelectedItem()).equals("No")) {
                 data += "false\n";
             }
             data +=
                 "email-username: " + emailUsernameTextField.getText() + "\n" +
                 "email-password: " + new String(emailPasswordTextField.getPassword()) + "\n" +
                 "icons: ";
             if (iconsSizeComboBox.getSelectedIndex() == 0) {
                 data += "large\n";
             } else if (iconsSizeComboBox.getSelectedIndex() == 1) {
                 data += "small\n";
             }
             File optionsFile = new File("data/settings.txt");
             FileWriter fw = new FileWriter(optionsFile);
             fw.write(data);
             fw.close();
         } catch (FileNotFoundException ex) {
             System.out.println(ex.getMessage());
         } catch (IOException ex) {
             System.out.println(ex.getMessage());
         }
 
         return true;
     }
 
     /** 
      * Checks whether the form fields have changed.
      *
      * @return  True if the fields have changed, false if not
      */    
     private boolean isChanged() {
         
         List<FTP> savedFTPs = options.getFTPs();
         
         if (savedFTPs != null && ftps != null && !savedFTPs.equals(ftps)) {
             return true;
         }                
         
         if (options == null && (!emailToTextArea.getText().isEmpty() ||
                 !emailFromTextField.getText().isEmpty() ||
                 !emailSubjectTextField.getText().isEmpty() ||
                 !emailHostTextField.getText().isEmpty() ||
                 !((String) emailAuthComboBox.getSelectedItem()).equals("No") ||
                 !emailUsernameTextField.getText().isEmpty() ||
                 !new String(emailPasswordTextField.getPassword()).isEmpty())) {
             return true;
         }
               
         if ((options.getEmailTo() == null && !emailToTextArea.getText().isEmpty()) ||
                 (options.getEmailTo() != null && !options.getEmailTo().equals(emailToTextArea.getText()))) {
             return true;
         }
         
         if ((options.getEmailFrom() == null && !emailFromTextField.getText().isEmpty()) ||
                 (options.getEmailFrom() != null && !options.getEmailFrom().equals(emailFromTextField.getText()))) {
             return true;
         }
         
         if ((options.getEmailSubject() == null && !emailSubjectTextField.getText().isEmpty()) ||
                 (options.getEmailSubject() != null && !options.getEmailSubject().equals(emailSubjectTextField.getText()))) {
             return true;
         }
         
         if ((options.getEmailHost() == null && !emailHostTextField.getText().isEmpty()) ||
                 (options.getEmailHost() != null && !options.getEmailHost().equals(emailHostTextField.getText()))) {
             return true;
         }                                        
 
         if ((options.isEmailAuth() == false && !((String) emailAuthComboBox.getSelectedItem()).equals("No")) ||
                 (options.isEmailAuth() == true && !((String) emailAuthComboBox.getSelectedItem()).equals("Yes"))) {
             return true;
         }   
         
         if ((options.getEmailUsername() == null && !emailUsernameTextField.getText().isEmpty()) ||
                 (options.getEmailUsername() != null && !options.getEmailUsername().equals(emailUsernameTextField.getText()))) {
             return true;
         }   
         
         if ((options.getEmailPassword() == null && !new String(emailPasswordTextField.getPassword()).isEmpty()) ||
                 (options.getEmailPassword() != null && !options.getEmailPassword().equals(new String(emailPasswordTextField.getPassword())))) {
             return true;
         }
       
         if (options.getIconsSize() == Options.LARGE_ICONS && iconsSizeComboBox != null && iconsSizeComboBox.getSelectedIndex() != 0) {
             return true;
         } else if (options.getIconsSize() == Options.SMALL_ICONS && iconsSizeComboBox != null && iconsSizeComboBox.getSelectedIndex() != 1) {
             return true;
         }        
        
         return false;        
     }
 }
 
