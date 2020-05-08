 package dk.frv.eavdam.menus;
 
 import dk.frv.eavdam.data.Address;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.data.EAVDAMUser;
 import dk.frv.eavdam.data.Person;
 import dk.frv.eavdam.io.XMLExporter;
 import dk.frv.eavdam.io.XMLImporter;
 import dk.frv.eavdam.utils.DBHandler;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
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
 import java.net.MalformedURLException;
 import java.net.URL;
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
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.xml.bind.JAXBException;
 
 /**
  * This class represents a menu item that opens a frame where the user can edit
  * user information.
  */
 public class UserInformationMenuItem extends JMenuItem {
 
     public static final long serialVersionUID = 3L;
 
     public UserInformationMenuItem(EavdamMenu eavdamMenu) {        
         super("Edit User Information");                
         addActionListener(new UserInformationActionListener(eavdamMenu));
     }
 
 }
  
 class UserInformationActionListener implements ActionListener, DocumentListener {
 
     private EavdamMenu eavdamMenu;
                                                          
     private JDialog dialog;
     
     private JTextField organizationNameTextField;
     private JTextField postalAddressTextField;
     private JTextField zipCodeTextField;
     private JTextField cityTextField;
     private JComboBox countryComboBox;
     private JTextField telephoneNumberTextField;
     private JTextField faxNumberTextField;
     private JTextField websiteTextField;                    
 
     private JTextField pointOfContactNameTextField; 
     private JTextField pointOfContactPhoneTextField;
     private JTextField pointOfContactEmailTextField;           
 
     private JTextField pointOfTechnicalContactNameTextField; 
     private JTextField pointOfTechnicalContactPhoneTextField;
     private JTextField pointOfTechnicalContactEmailTextField;    
     
     private JTextArea additionalInformationJTextArea;
     
     private JButton saveButton;
     private JButton cancelButton;
     
     private EAVDAMData data;                
               
     public UserInformationActionListener(EavdamMenu eavdamMenu) {
         super();
         this.eavdamMenu = eavdamMenu;
     }
                    
     public void actionPerformed(ActionEvent e) {
         
         if (e.getSource() instanceof UserInformationMenuItem) {
 
             dialog = new JDialog(eavdamMenu.getOpenMapFrame(), "User Information", true);
         
             organizationNameTextField = new JTextField(16);
             organizationNameTextField.getDocument().addDocumentListener(this);
             postalAddressTextField = new JTextField(16);             
             postalAddressTextField.getDocument().addDocumentListener(this);
             zipCodeTextField = new JTextField(16);             
             zipCodeTextField.getDocument().addDocumentListener(this);
             cityTextField = new JTextField(16);
             cityTextField.getDocument().addDocumentListener(this);
             countryComboBox = new JComboBox
                 (new String[] {"Denmark", "Finland", "Norway" , "Sweden"});
             countryComboBox.addActionListener(this);
             telephoneNumberTextField = new JTextField(16);
             telephoneNumberTextField.getDocument().addDocumentListener(this);
             faxNumberTextField = new JTextField(16);
             faxNumberTextField.getDocument().addDocumentListener(this);
             websiteTextField = new JTextField(16);  
             websiteTextField.getDocument().addDocumentListener(this);
             pointOfContactNameTextField = new JTextField(16);  
             pointOfContactNameTextField.getDocument().addDocumentListener(this);
             pointOfContactPhoneTextField = new JTextField(16);                                                                                                
             pointOfContactPhoneTextField.getDocument().addDocumentListener(this);
             pointOfContactEmailTextField = new JTextField(16);  
             pointOfContactEmailTextField.getDocument().addDocumentListener(this);
             pointOfTechnicalContactNameTextField = new JTextField(16);  
             pointOfTechnicalContactNameTextField.getDocument().addDocumentListener(this);
             pointOfTechnicalContactPhoneTextField = new JTextField(16);                                          
             pointOfTechnicalContactPhoneTextField.getDocument().addDocumentListener(this);
             pointOfTechnicalContactEmailTextField = new JTextField(16); 
             pointOfTechnicalContactEmailTextField.getDocument().addDocumentListener(this);
             additionalInformationJTextArea = new JTextArea("");                
             additionalInformationJTextArea.getDocument().addDocumentListener(this);
 
             data = DBHandler.getData();            
             EAVDAMUser user = null;                    
             if (data != null) {
                 user = data.getUser();
             }                                   
          
             if (user != null) {
                 if (user.getOrganizationName() != null) {
                     organizationNameTextField.setText(user.getOrganizationName());
                 }
                 if (user.getCountryID() != null) {
                     if (user.getCountryID().equals("DK")) {
                         countryComboBox.setSelectedItem("Denmark");
                     } else if (user.getCountryID().equals("FI")) {
                         countryComboBox.setSelectedItem("Finland");
                     } else if (user.getCountryID().equals("NO")) {
                         countryComboBox.setSelectedItem("Norway");
                     } else if (user.getCountryID().equals("SV")) {
                         countryComboBox.setSelectedItem("Sweden");
                     }
                 }
                 if (user.getPostalAddress() != null) {
                     Address address = user.getPostalAddress();
                     if (address.getAddressline1() != null) {
                         postalAddressTextField.setText(address.getAddressline1());
                     }                  
                     if (address.getZip() != null) {
                         zipCodeTextField.setText(address.getZip());
                     }                               
                     if (address.getCity() != null) {
                         cityTextField.setText(address.getCity());
                     }    
                 }
                 if (user.getPhone() != null) {
                     telephoneNumberTextField.setText(user.getPhone());
                 }
                 if (user.getFax() != null) {
                     faxNumberTextField.setText(user.getFax());
                 }
                 if (user.getWww() != null) {
                     websiteTextField.setText(user.getWww().toString());
                 }
                 if (user.getContact() != null) {
                     Person contact = user.getContact();
                     if (contact.getName() != null) {
                         pointOfContactNameTextField.setText(contact.getName());
                     }
                 }
                 if (user.getContact() != null) {
                     Person contact = user.getContact();
                     if (contact.getPhone() != null) {
                         pointOfContactPhoneTextField.setText(contact.getPhone());
                     }
                 }
                 if (user.getContact() != null) {
                     Person contact = user.getContact();
                     if (contact.getEmail() != null) {
                         pointOfContactEmailTextField.setText(contact.getEmail());
                     }
                 }                                                                                                                                                                      
                 if (user.getTechnicalContact() != null) {
                     Person technicalContact = user.getTechnicalContact();
                     if (technicalContact.getName() != null) {
                         pointOfTechnicalContactNameTextField.setText(technicalContact.getName());
                     }
                 }
                 if (user.getTechnicalContact() != null) {
                     Person technicalContact = user.getTechnicalContact();
                     if (technicalContact.getPhone() != null) {
                         pointOfTechnicalContactPhoneTextField.setText(technicalContact.getPhone());
                     }
                 }
                 if (user.getTechnicalContact() != null) {
                     Person technicalContact = user.getTechnicalContact();
                     if (technicalContact.getEmail() != null) {
                         pointOfTechnicalContactEmailTextField.setText(technicalContact.getEmail());
                     }
                 }                      
                 if (user.getDescription() != null) {
                     additionalInformationJTextArea.setText(user.getDescription());
                 }
             }
 
             JPanel panel = new JPanel();
             panel.setLayout(new GridBagLayout());                  
             
             JPanel p1 = new JPanel(new GridBagLayout());
             p1.setBorder(BorderFactory.createTitledBorder("General information"));
             GridBagConstraints c = new GridBagConstraints();
             c.gridx = 0;
             c.gridy = 0;                   
             c.anchor = GridBagConstraints.LINE_START;
             c.insets = new Insets(5,5,5,5);
             p1.add(new JLabel("Name of organisation:"), c);
             c.gridx = 1;                
             p1.add(organizationNameTextField, c);
             c.gridx = 0;
             c.gridy = 1;                  
             p1.add(new JLabel("Postal address:"), c);
             c.gridx = 1;                    
             p1.add(postalAddressTextField, c);                    
             c.gridx = 0;
             c.gridy = 2;                  
             p1.add(new JLabel("Zip code:"), c);
             c.gridx = 1;                    
             p1.add(zipCodeTextField, c);
             c.gridx = 0;
             c.gridy = 3;                  
             p1.add(new JLabel("City:"), c);
             c.gridx = 1;                    
             p1.add(cityTextField, c);                    
             c.gridx = 0;
             c.gridy = 4;                  
             p1.add(new JLabel("Country:"), c);
             c.gridx = 1;                    
             p1.add(countryComboBox, c);
             c.gridx = 0;
             c.gridy = 5;                  
             p1.add(new JLabel("Telephone number:"), c);
             c.gridx = 1;                    
             p1.add(telephoneNumberTextField, c);  
             c.gridx = 0;
             c.gridy = 6;                  
             p1.add(new JLabel("Fax number:"), c);
             c.gridx = 1;                    
             p1.add(faxNumberTextField, c);  
             c.gridx = 0;
             c.gridy = 7;                  
             p1.add(new JLabel("Website:"), c);
             c.gridx = 1;                    
             p1.add(websiteTextField, c);  
             
             c.gridx = 0;
             c.gridy = 0;
             c.gridheight = 2;
             c.anchor = GridBagConstraints.FIRST_LINE_START;
             panel.add(p1, c);                    
   
             JPanel p2 = new JPanel(new GridBagLayout());
             p2.setBorder(BorderFactory.createTitledBorder("Point of contact"));
             c.gridheight = 1;
             c.gridx = 0;
             c.gridy = 0;                   
             c.anchor = GridBagConstraints.LINE_START;
             p2.add(new JLabel("Name:"), c);
             c.gridx = 1;                    
             p2.add(pointOfContactNameTextField, c);
             c.gridx = 0;
             c.gridy = 1;                  
             p2.add(new JLabel("Phone:"), c);
             c.gridx = 1;                    
             p2.add(pointOfContactPhoneTextField, c);                    
             c.gridx = 0;
             c.gridy = 2;                  
             p2.add(new JLabel("E-mail:"), c);
             c.gridx = 1;                    
             p2.add(pointOfContactEmailTextField, c);
   
             c.gridx = 1;
             c.gridy = 0;
             c.anchor = GridBagConstraints.FIRST_LINE_START;
             panel.add(p2, c);                    
                                           
             JPanel p3 = new JPanel(new GridBagLayout());
             p3.setBorder(BorderFactory.createTitledBorder("Point of technical contact"));
             c.gridx = 0;
             c.gridy = 0;                  
             c.anchor = GridBagConstraints.LINE_START;                    
             p3.add(new JLabel("Name:"), c);
             c.gridx = 1;                    
             p3.add(pointOfTechnicalContactNameTextField, c);
             c.gridx = 0;
             c.gridy = 1;                  
             p3.add(new JLabel("Phone:"), c);
             c.gridx = 1;                    
             p3.add(pointOfTechnicalContactPhoneTextField, c);                    
             c.gridx = 0;
             c.gridy = 2;                  
             p3.add(new JLabel("E-mail:"), c);
             c.gridx = 1;                    
             p3.add(pointOfTechnicalContactEmailTextField, c);                                        
             
             c.gridx = 1;
             c.gridy = 1;
             c.anchor = GridBagConstraints.FIRST_LINE_START;
             panel.add(p3, c);
             
             additionalInformationJTextArea.setLineWrap(true);
             additionalInformationJTextArea.setWrapStyleWord(true);                    
             JScrollPane p4 = new JScrollPane(additionalInformationJTextArea);
             p4.setBorder(BorderFactory.createTitledBorder("Additional information"));
             p4.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
             p4.setPreferredSize(new Dimension(580, 90));
             p4.setMaximumSize(new Dimension(580, 90));
             
             c.gridx = 0;
             c.gridy = 2;
             c.gridwidth = 2;
             c.anchor = GridBagConstraints.FIRST_LINE_START;
             panel.add(p4, c);                   
             
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
 
             JPanel buttonPanel = new JPanel();
             buttonPanel.add(saveButton);
             saveButton.setEnabled(false);                                        
 			if (data != null && user != null) {
 				buttonPanel.add(cancelButton); 
 			}
                               
             c.gridx = 0;
             c.gridy = 3;
             c.gridwidth = 2;
             c.anchor = GridBagConstraints.CENTER;                    
             panel.add(buttonPanel, c);		
 
             dialog.getContentPane().add(panel);
                                                                          
             int frameWidth = 640;
             int frameHeight = 480;
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             dialog.setBounds((int) screenSize.getWidth()/2 - frameWidth/2,
                 (int) screenSize.getHeight()/2 - frameHeight/2, frameWidth, frameHeight);
             dialog.setVisible(true);
         
         } else if (saveButton != null && e.getSource() == saveButton) {
             
             boolean success = saveUser();
             
             if (success) {
                 saveButton.setEnabled(false);
                 dialog.dispose();
             }
 
         } else if (cancelButton != null && e.getSource() == cancelButton) {
             if (saveButton.isEnabled()) {
                 int response = JOptionPane.showConfirmDialog(dialog, "Do you want to save the changes before exiting?", "Confirm action", JOptionPane.YES_NO_OPTION);
                 if (response == JOptionPane.YES_OPTION) {
                     boolean success = saveUser();
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
 
     public void changedUpdate(DocumentEvent e) {
         if (saveButton != null) {
             if (isChanged()) {
                 saveButton.setEnabled(true);
             } else {
                 saveButton.setEnabled(false);
             }
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
     } 
     
     public void removeUpdate(DocumentEvent e) {
         if (saveButton != null) {
             if (isChanged()) {
                 saveButton.setEnabled(true);
             } else {
                 saveButton.setEnabled(false);
             }
         }
     }
 
     /** 
      * Saves a user to XML.
      *
      * @return True if the save was succesful, false otherwise
      */        
     private boolean saveUser() {
 
		boolean addingNew = false;
	
         if (data == null) {
             data = new EAVDAMData();
         }
 
         EAVDAMUser user = data.getUser();
         if (user == null) {
			addingNew = true;
             user = new EAVDAMUser();
         }
         
         try {
             String oldOrganizationName = user.getOrganizationName();            
             user.setOrganizationName(organizationNameTextField.getText().trim());
             if (((String) countryComboBox.getSelectedItem()).equals("Denmark")) {
                 user.setCountryID("DK");
             } else if (((String) countryComboBox.getSelectedItem()).equals("Finland")) {
                 user.setCountryID("FI");
             } else if (((String) countryComboBox.getSelectedItem()).equals("Norway")) {
                 user.setCountryID("NO");
             } else if (((String) countryComboBox.getSelectedItem()).equals("Sweden")) {
                 user.setCountryID("SV");
             }
             Address address = user.getPostalAddress();
             if (address == null) {
                 address = new Address();
             }
             if ((!postalAddressTextField.getText().trim().isEmpty() ||
                     !cityTextField.getText().trim().isEmpty() ||
                     !zipCodeTextField.getText().trim().isEmpty()) &&
                     (postalAddressTextField.getText().trim().isEmpty() ||
                     cityTextField.getText().trim().isEmpty() ||
                     zipCodeTextField.getText().trim().isEmpty())) {
                 JOptionPane.showMessageDialog(dialog, "Postal address, city and zip code must be all given.");                  
                 return false;
             }
             if (!postalAddressTextField.getText().trim().isEmpty()) {               
                 address.setAddressline1(postalAddressTextField.getText().trim());
                 address.setCity(cityTextField.getText().trim());
                 address.setZip(zipCodeTextField.getText().trim());
                 address.setCountry((String) countryComboBox.getSelectedItem());
                 user.setPostalAddress(address);
             }
             if (telephoneNumberTextField.getText().trim().isEmpty()) {
                 user.setPhone(null);
             } else {
                 user.setPhone(telephoneNumberTextField.getText().trim());
             }
             if (faxNumberTextField.getText().trim().isEmpty()) {
                 user.setFax(null);
             } else {
                 user.setFax(faxNumberTextField.getText().trim());
             }
             if (websiteTextField.getText().trim().isEmpty()) {
                 user.setWww(null);
             } else {
                 try {
                     user.setWww(new URL(websiteTextField.getText().trim()));
                 } catch (MalformedURLException ex) {                       
                     JOptionPane.showMessageDialog(dialog, "Website address is not a valid URL.");
                     return false;
                 }
             }
             if (pointOfContactNameTextField.getText().trim().isEmpty() &&
                     (!pointOfContactPhoneTextField.getText().trim().isEmpty() ||
                     !pointOfContactEmailTextField.getText().trim().isEmpty())) {
                 JOptionPane.showMessageDialog(dialog, "Point of contact's name must ge given.");                  
                 return false;
             }         
             if (pointOfContactNameTextField.getText().trim().isEmpty()) {
                 user.setContact(null);
             } else {
                 Person contact = new Person();
                 contact.setName(pointOfContactNameTextField.getText().trim());
                 if (pointOfContactPhoneTextField.getText().trim().isEmpty()) {
                     contact.setPhone(null);
                 } else {
                     contact.setPhone(pointOfContactPhoneTextField.getText().trim());
                 }
                 if (pointOfContactEmailTextField.getText().trim().isEmpty()) {
                     contact.setEmail(null);
                 } else {
                     contact.setEmail(pointOfContactEmailTextField.getText().trim());
                 }
                 user.setContact(contact);              
             }
             if (pointOfTechnicalContactNameTextField.getText().trim().isEmpty() &&
                     (!pointOfTechnicalContactPhoneTextField.getText().trim().isEmpty() ||
                     !pointOfTechnicalContactEmailTextField.getText().trim().isEmpty())) {
                 JOptionPane.showMessageDialog(dialog, "Point of technical contact's name must ge given.");                  
                 return false;
             } 
             if (pointOfTechnicalContactNameTextField.getText().trim().isEmpty()) {
                 user.setTechnicalContact(null);
             } else {
                 Person technicalContact = new Person();                            
                 technicalContact.setName(pointOfTechnicalContactNameTextField.getText().trim());                    
                 if (pointOfTechnicalContactPhoneTextField.getText().trim().isEmpty()) {
                     technicalContact.setPhone(null);
                 } else {
                     technicalContact.setPhone(pointOfTechnicalContactPhoneTextField.getText().trim());
                 }               
                 if (pointOfTechnicalContactEmailTextField.getText().trim().isEmpty()) {
                     technicalContact.setEmail(null);
                 } else {
                     technicalContact.setEmail(pointOfTechnicalContactEmailTextField.getText().trim());
                 }                   
                 user.setTechnicalContact(technicalContact);                        
             }                          
             if (additionalInformationJTextArea.getText().trim().isEmpty()) {
                 user.setDescription(null);
             } else {
                 user.setDescription(additionalInformationJTextArea.getText().trim());
             }
         } catch (IllegalArgumentException e) {
             JOptionPane.showMessageDialog(dialog, e.getMessage());
             return false;
         }
         
         data.setUser(user);                                
		
		
         
        DBHandler.saveUserData(user, addingNew);              
         
         return true;
     
     }
 
     /** 
      * Checks whether the form fields have changed.
      *
      * @return  True if the fields have changed, false if not
      */    
     private boolean isChanged() {
         
         if (data == null) {
             return true;
         }
         
         EAVDAMUser user = data.getUser();
         if (user == null) {
             user = new EAVDAMUser();
         }
         
         if (user.getOrganizationName() == null && !organizationNameTextField.getText().isEmpty()) {
             return true;
         }
         if (user.getOrganizationName() != null && !user.getOrganizationName().equals(organizationNameTextField.getText())) {
             return true;
         }        
         
         Address address = user.getPostalAddress();
         if (address == null) {
             address = new Address();
         }
         if (address.getAddressline1() == null && !postalAddressTextField.getText().isEmpty()) {
             return true;
         }
         if (address.getAddressline1() != null && !address.getAddressline1().equals(postalAddressTextField.getText())) {
             return true;
         }
         if (address.getCity() == null && !cityTextField.getText().isEmpty()) {
             return true;
         }
         if (address.getCity() != null && !address.getCity().equals(cityTextField.getText())) {
             return true;
         }
         if (address.getZip() == null && !zipCodeTextField.getText().isEmpty()) {
             return true;
         }
         if (address.getZip() != null && !address.getZip().equals(zipCodeTextField.getText())) {
             return true;
         }
         if (address.getCountry() != null && !address.getCountry().equals((String) countryComboBox.getSelectedItem())) {
             return true;
         }
         if (user.getPhone() == null && !telephoneNumberTextField.getText().isEmpty()) {
             return true;
         }
         if (user.getPhone() != null && !user.getPhone().equals(telephoneNumberTextField.getText())) {
             return true;
         }
         if (user.getFax() == null && !faxNumberTextField.getText().isEmpty()) {
             return true;
         }
         if (user.getFax() != null && !user.getFax().equals(faxNumberTextField.getText())) {
             return true;
         }
         if (user.getWww() == null && !websiteTextField.getText().isEmpty()) {
             return true;
         }
         if (user.getWww() != null && !user.getWww().toString().equals(websiteTextField.getText())) {
             return true;
         }
         Person contact = user.getContact();
         if (contact == null) {
             contact = new Person();
         }
         if (contact.getName() == null && !pointOfContactNameTextField.getText().isEmpty()) {
             return true;
         }
         if (contact.getName() != null && !contact.getName().equals(pointOfContactNameTextField.getText())) {
             return true;
         }
         if (contact.getPhone() == null && !pointOfContactPhoneTextField.getText().isEmpty()) {
             return true;
         }
         if (contact.getPhone() != null && !contact.getPhone().equals(pointOfContactPhoneTextField.getText())) {
             return true;
         }
         if (contact.getEmail() == null && !pointOfContactEmailTextField.getText().isEmpty()) {
             return true;
         }
         if (contact.getEmail() != null && !contact.getEmail().equals(pointOfContactEmailTextField.getText())) {
             return true;
         }        
         Person technicalContact = user.getTechnicalContact();
         if (technicalContact == null) {
             technicalContact = new Person();
         }
         if (technicalContact.getName() == null && !pointOfTechnicalContactNameTextField.getText().isEmpty()) {
             return true;
         }
         if (technicalContact.getName() != null && !technicalContact.getName().equals(pointOfTechnicalContactNameTextField.getText())) {
             return true;
         }
         if (technicalContact.getPhone() == null && !pointOfTechnicalContactPhoneTextField.getText().isEmpty()) {
             return true;
         }
         if (technicalContact.getPhone() != null && !technicalContact.getPhone().equals(pointOfTechnicalContactPhoneTextField.getText())) {
             return true;
         }
         if (technicalContact.getEmail() == null && !pointOfTechnicalContactEmailTextField.getText().isEmpty()) {
             return true;
         }
         if (technicalContact.getEmail() != null && !technicalContact.getEmail().equals(pointOfTechnicalContactEmailTextField.getText())) {
             return true;
         }       
         if (user.getDescription() == null && !additionalInformationJTextArea.getText().isEmpty()) {
             return true;
         }                 
         if (user.getDescription() != null && !user.getDescription().equals(additionalInformationJTextArea.getText())) {
             return true;
         }
         return false;        
     }
 }
 
