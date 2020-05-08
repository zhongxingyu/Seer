 package cz.vutbr.fit.gja.gjaddr.gui;
 
 import cz.vutbr.fit.gja.gjaddr.gui.util.Validators;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.*;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.util.MessengersEnum;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.util.NameDays;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.util.TypesEnum;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import javax.swing.*;
 import org.jdesktop.swingx.JXDatePicker;
 import org.slf4j.LoggerFactory;
 
 /**
  * Contact editing window.
  *
  * @author Bc. Jan Kaláb <xkalab00@stud.fit,vutbr.cz>
  * @author Bc. Radek Gajdusek <xgajdu07@stud.fit,vutbr.cz>
  * @author Bc. Drahomira Herrmannova <xherrm01@stud.fit.vutbr.cz>
  */
 class ContactWindow extends JDialog {
 
     static final long serialVersionUID = 0;
     /**
      * Database instance.
      */
     private final Database db = Database.getInstance();
     /**
      * Currently edited contact.
      */
     private Contact contact;
     private final JButton button = new JButton();
     private final PhotoButton photo = new PhotoButton();
     private final JTextField nameField = new JTextField();
     private final JTextField surnameField = new JTextField();
     private final JTextField nicknameField = new JTextField();
     private final JTextField addressField = new JTextField();
     private final JTextField workEmailField = new JTextField();
     private final JTextField homeEmailField = new JTextField();
     private final JTextField otherEmailField = new JTextField();
     private final JTextField workUrlField = new JTextField();
     private final JTextField homeUrlField = new JTextField();
     private final JTextField otherUrlField = new JTextField();
     private final JTextField workPhoneField = new JTextField();
     private final JTextField homePhoneField = new JTextField();
     private final JTextField otherPhoneField = new JTextField();
     private final JTextField icqField = new JTextField();
     private final JTextField jabberField = new JTextField();
     private final JTextField skypeField = new JTextField();
     private final JTextArea noteField = new JTextArea();
     private final JXDatePicker birthdayPicker = new JXDatePicker();
     private final JXDatePicker namedayPicker = new JXDatePicker();
     private final JXDatePicker celebrationPicker = new JXDatePicker();
 
     /**
      * Constructor for adding new contact
      */
     public ContactWindow() {
         super();
         super.setTitle("Add contact");
         super.setPreferredSize(new Dimension(480, 480));
         super.setModal(true);
         setIconImage(new ImageIcon(getClass().getResource("/res/plus.png"), "+").getImage());
         button.setText("Add contact");
         button.addActionListener(new NewContactActionListener());
         contact = new Contact();
         prepare();
         log("Opening new contact window.");
     }
 
     /**
      * Constructor for editing contact
      */
     public ContactWindow(Contact contact) {
         super();
         super.setTitle("Edit contact");
         super.setPreferredSize(new Dimension(480, 480));
         super.setModal(true);
         this.contact = contact;
         setIconImage(new ImageIcon(getClass().getResource("/res/edit.png"), "Edit").getImage());
         photo.setContact(contact);
         button.setText("Edit contact");
         button.addActionListener(new EditContactActionListener());
         nameField.setText(contact.getFirstName());
         surnameField.setText(contact.getSurName());
         nicknameField.setText(contact.getNickName());
         addressField.setText(contact.getAllAddresses());
         birthdayPicker.setDate(contact.getBirthday() != null ? contact.getBirthday().getDate() : null);
         namedayPicker.setDate(contact.getNameDay() != null ? contact.getNameDay().getDate() : null);
         celebrationPicker.setDate(contact.getCelebration() != null ? contact.getCelebration().getDate() : null);
         noteField.setText(contact.getNote());
         for (Url u : contact.getUrls()) {
             if (u.getValue() != null) {
                 switch (u.getType()) {
                     case WORK:
                         workUrlField.setText(u.getValue().toString());
                         break;
                     case HOME:
                         homeUrlField.setText(u.getValue().toString());
                         break;
                     case OTHER:
                         otherUrlField.setText(u.getValue().toString());
                         break;
                 }
             }
         }
         for (Email e : contact.getEmails()) {
             switch (e.getType()) {
                 case WORK:
                     workEmailField.setText(e.getEmail());
                     break;
                 case HOME:
                     homeEmailField.setText(e.getEmail());
                     break;
                 case OTHER:
                     otherEmailField.setText(e.getEmail());
                     break;
             }
         }
         for (PhoneNumber p : contact.getPhoneNumbers()) {
             switch (p.getType()) {
                 case WORK:
                     workPhoneField.setText(p.getNumber());
                     break;
                 case HOME:
                     homePhoneField.setText(p.getNumber());
                     break;
                 case OTHER:
                     otherPhoneField.setText(p.getNumber());
                     break;
             }
         }
         for (Messenger m : contact.getMessenger()) {
             if (m.getValue() != null) {
                 switch (m.getType()) {
                     case ICQ:
                         icqField.setText(m.getValue());
                         break;
                     case JABBER:
                         jabberField.setText(m.getValue());
                         break;
                     case SKYPE:
                         skypeField.setText(m.getValue());
                         break;
                 }
             }
         }
 
         prepare();
         log("Opening edit contact window.");
     }
 
     /**
      * Make window escapable.
      *
      * @return
      */
     @Override
     protected JRootPane createRootPane() {
         JRootPane rp = new JRootPane();
         KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
         Action actionListener = new AbstractAction() {
             static final long serialVersionUID = 0;
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 dispose();
             }
         };
         InputMap inputMap = rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
         inputMap.put(stroke, "ESCAPE");
         rp.getActionMap().put("ESCAPE", actionListener);
         return rp;
     }
 
     /**
      * Method for creating window content layout.
      */
     private void prepare() {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
             System.err.println(e);
         }
 
         final JPanel form = new JPanel(new GridBagLayout());
         form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         final GridBagConstraints c = new GridBagConstraints();
 
         final JPanel formContact = new JPanel(new GridBagLayout());
         formContact.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         final GridBagConstraints cDetails = new GridBagConstraints();
 
         final JPanel formNote = new JPanel(new GridBagLayout());
         formNote.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         final GridBagConstraints cNote = new GridBagConstraints();
 
         final JPanel formPhoto = new JPanel(new GridBagLayout());
         formPhoto.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         final GridBagConstraints cPhoto = new GridBagConstraints();
 
         // create tabs
         JTabbedPane tabs = new JTabbedPane();
         tabs.addTab("Basic details", form);
         tabs.addTab("Contact details", formContact);
         tabs.addTab("Notes", formNote);
         tabs.addTab("Photo", formPhoto);
 
         // basic details
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(1, 1, 1, 1);
         c.weightx = 0;
         c.anchor = GridBagConstraints.NORTHWEST;
         c.gridx = 0;
         c.gridy = 0;
         form.add(new JLabel("Name"), c);
         c.gridx = 1;
         c.weightx = 1;
         form.add(nameField, c);
         c.gridy++;
         c.gridx = 0;
         c.weightx = 0;
         form.add(new JLabel("Surname"), c);
         c.gridx = 1;
         c.weightx = 1;
         form.add(surnameField, c);
         c.gridy++;
         c.gridx = 0;
         c.weightx = 0;
         form.add(new JLabel("Nickname"), c);
         c.gridx = 1;
         c.weightx = 1;
         form.add(nicknameField, c);
         c.gridy++;
         c.gridx = 0;
         c.weightx = 0;
         form.add(new JLabel("Address"), c);
         c.gridx = 1;
         c.weightx = 1;
         form.add(addressField, c);
         c.gridy++;
         c.gridx = 0;
         c.weightx = 0;
         form.add(new JLabel("Birthday"), c);
         c.gridx = 1;
         c.weightx = 1;
         birthdayPicker.setFormats(new String[]{"d. M. yyyy"});
         form.add(birthdayPicker, c);
         c.gridy++;
         c.gridx = 0;
         c.weightx = 0;
         form.add(new JLabel("Nameday"), c);
         c.gridx = 1;
         c.weightx = 1;
         namedayPicker.setFormats(new String[]{"d. M."});
         form.add(namedayPicker, c);
         c.gridy++;
         c.gridx = 0;
         c.weightx = 0;
         form.add(new JLabel("Anniversary"), c);
         c.gridx = 1;
         c.weightx = 1;
         celebrationPicker.setFormats(new String[]{"d. M. yyyy"});
         form.add(celebrationPicker, c);
         c.gridy++;
         c.weighty = 1.0;
         form.add(Box.createHorizontalGlue(), c);
 
         // contact details
         cDetails.fill = GridBagConstraints.HORIZONTAL;
         cDetails.insets = new Insets(1, 1, 1, 1);
         cDetails.weightx = 0;
         cDetails.anchor = GridBagConstraints.NORTHWEST;
         cDetails.gridy = 0;
         cDetails.gridx = 0;
         cDetails.weightx = 0;
         formContact.add(new JLabel("Work E-mail"), cDetails);
         cDetails.gridx = 1;
         cDetails.weightx = 1;
         formContact.add(workEmailField, cDetails);
         cDetails.gridy++;
         cDetails.gridx = 0;
         cDetails.weightx = 0;
         formContact.add(new JLabel("Home E-mail"), cDetails);
         cDetails.gridx = 1;
         cDetails.weightx = 1;
         formContact.add(homeEmailField, cDetails);
         cDetails.gridy++;
         cDetails.gridx = 0;
         cDetails.weightx = 0;
         formContact.add(new JLabel("Other E-mail"), cDetails);
         cDetails.gridx = 1;
         cDetails.weightx = 1;
         formContact.add(otherEmailField, cDetails);
         cDetails.gridy++;
         cDetails.gridx = 0;
         cDetails.weightx = 0;
         formContact.add(new JLabel("Work URL"), cDetails);
         cDetails.gridx = 1;
         cDetails.weightx = 1;
         formContact.add(workUrlField, cDetails);
         cDetails.gridy++;
         cDetails.gridx = 0;
         cDetails.weightx = 0;
         formContact.add(new JLabel("Home URL"), cDetails);
         cDetails.gridx = 1;
         cDetails.weightx = 1;
         formContact.add(homeUrlField, cDetails);
         cDetails.gridy++;
         cDetails.gridx = 0;
         cDetails.weightx = 0;
         formContact.add(new JLabel("Other URL"), cDetails);
         cDetails.gridx = 1;
         cDetails.weightx = 1;
         formContact.add(otherUrlField, cDetails);
         cDetails.gridy++;
         cDetails.gridx = 0;
         cDetails.weightx = 0;
         formContact.add(new JLabel("Work Phone"), cDetails);
         cDetails.gridx = 1;
         cDetails.weightx = 1;
         formContact.add(workPhoneField, cDetails);
         cDetails.gridy++;
         cDetails.gridx = 0;
         cDetails.weightx = 0;
         formContact.add(new JLabel("Home Phone"), cDetails);
         cDetails.gridx = 1;
         cDetails.weightx = 1;
         formContact.add(homePhoneField, cDetails);
         cDetails.gridy++;
         cDetails.gridx = 0;
         cDetails.weightx = 0;
         formContact.add(new JLabel("Other Phone"), cDetails);
         cDetails.gridx = 1;
         cDetails.weightx = 1;
         formContact.add(otherPhoneField, cDetails);
         cDetails.gridy++;
         cDetails.gridx = 0;
         cDetails.weightx = 0;
         formContact.add(new JLabel("ICQ"), cDetails);
         cDetails.gridx = 1;
         cDetails.weightx = 1;
         formContact.add(icqField, cDetails);
         cDetails.gridy++;
         cDetails.gridx = 0;
         cDetails.weightx = 0;
         formContact.add(new JLabel("Jabber"), cDetails);
         cDetails.gridx = 1;
         cDetails.weightx = 1;
         formContact.add(jabberField, cDetails);
         cDetails.gridy++;
         cDetails.gridx = 0;
         cDetails.weightx = 0;
         formContact.add(new JLabel("Skype"), cDetails);
         cDetails.gridx = 1;
         cDetails.weightx = 1;
         formContact.add(skypeField, cDetails);
        cDetails.gridy++;
        cDetails.weighty = 1.0;
        formContact.add(Box.createHorizontalGlue(), c);
 
         // note
         cNote.fill = GridBagConstraints.HORIZONTAL;
         cNote.insets = new Insets(1, 1, 1, 1);
         cNote.weightx = 0;
         cNote.anchor = GridBagConstraints.NORTHWEST;
         cNote.gridy = 0;
         cNote.gridx = 0;
         cNote.weightx = 0;
         JLabel nl = new JLabel("Note");
         nl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
         formNote.add(nl, cNote);
         cNote.gridx = 1;
         cNote.weightx = 1;
         noteField.setLineWrap(true);
         noteField.setWrapStyleWord(true);
         noteField.setRows(10);
         noteField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
         formNote.add(noteField, cNote);
         cNote.weighty = 1.0;
         cNote.gridy++;
         formNote.add(Box.createHorizontalGlue(), cNote);
 
         // photo
         cPhoto.insets = new Insets(1, 1, 1, 1);
         cPhoto.weightx = 0;
         cPhoto.anchor = GridBagConstraints.NORTHWEST;
         cPhoto.gridy = 0;
         cPhoto.gridx = 0;
         cPhoto.weightx = 0;
         JLabel pl = new JLabel("Photo");
         pl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
         formPhoto.add(pl, cPhoto);
         cPhoto.gridx = 1;
         cPhoto.weightx = 1;
         formPhoto.add(photo, cPhoto);
        cPhoto.gridy++;
        JButton photoButton = new JButton("Change Photo");
        // FUJ!
        ActionListener[] actionListeners = this.photo.getActionListeners();
        photoButton.addActionListener(actionListeners[0]);
        formPhoto.add(photoButton);
         cPhoto.weighty = 1.0;
         cPhoto.gridy++;
         formPhoto.add(Box.createHorizontalGlue(), cPhoto);
 
         // finish
         add(tabs, BorderLayout.CENTER);
         add(button, BorderLayout.PAGE_END);
         setLocationRelativeTo(null);
         pack();
         setVisible(true);
     }
 
     /**
      * Resolving contact data from user form input.
      *
      * @return true if is all correct, in error case returns false
      */
     private boolean resolvecontact() {
         boolean valid = this.validateData();
         if (valid) {
             contact.setFirstName(nameField.getText());
             contact.setSurName(surnameField.getText());
             contact.setNickName(nicknameField.getText());
             contact.setBirthday(birthdayPicker.getDate());
             if (namedayPicker.getDate() != null) {
                 contact.setNameDay(namedayPicker.getDate());
             } else {
                 if (!contact.getFirstName().isEmpty()) {
                     Calendar nameDay = NameDays.getInstance().getNameDay(contact.getFirstName());
                     if (nameDay != null) {
                         contact.setNameDay(nameDay.getTime());
                     }
                 }
             }
             contact.setCelebration(celebrationPicker.getDate());
             contact.setNote(noteField.getText());
             contact.setPhoto((ImageIcon) photo.getIcon());
 
             final ArrayList<Address> addresses = new ArrayList<Address>();
             addresses.add(new Address(TypesEnum.HOME, addressField.getText()));
             contact.setAdresses(addresses);
 
             final ArrayList<Url> urls = new ArrayList<Url>();
             urls.add(new Url(TypesEnum.WORK, workUrlField.getText()));
             urls.add(new Url(TypesEnum.HOME, homeUrlField.getText()));
             urls.add(new Url(TypesEnum.OTHER, otherUrlField.getText()));
             contact.setUrls(urls);
 
             final ArrayList<PhoneNumber> phones = new ArrayList<PhoneNumber>();
             phones.add(new PhoneNumber(TypesEnum.WORK, workPhoneField.getText()));
             phones.add(new PhoneNumber(TypesEnum.HOME, homePhoneField.getText()));
             phones.add(new PhoneNumber(TypesEnum.OTHER, otherPhoneField.getText()));
             contact.setPhoneNumbers(phones);
 
             final ArrayList<Email> emails = new ArrayList<Email>();
             emails.add(new Email(TypesEnum.WORK, workEmailField.getText()));
             emails.add(new Email(TypesEnum.HOME, homeEmailField.getText()));
             emails.add(new Email(TypesEnum.OTHER, otherEmailField.getText()));
             contact.setEmails(emails);
 
             final ArrayList<Messenger> messengers = new ArrayList<Messenger>();
             messengers.add(new Messenger(MessengersEnum.ICQ, icqField.getText()));
             messengers.add(new Messenger(MessengersEnum.JABBER, jabberField.getText()));
             messengers.add(new Messenger(MessengersEnum.SKYPE, skypeField.getText()));
             contact.setMessenger(messengers);
         }
         return valid;
     }
 
     /**
      * Check if is email and url valid, if is not valid, display a message.
      *
      * @return true if is validation successfull, otherwise false.
      */
     private boolean validateData() {
         StringBuilder message = new StringBuilder();
 
         // required entry is one of these - nick, name, surname
         if (nameField.getText().isEmpty()
                 && surnameField.getText().isEmpty()
                 && nicknameField.getText().isEmpty()) {
             message.append("Name, Surname or Nickname shouldn't be empty. Please fill at least one item.");
         }
 
         if (!Validators.isEmailValid(workEmailField.getText())) {
             message.append("Work email address is not valid\r\n");
         }
         if (!Validators.isEmailValid(homeEmailField.getText())) {
             message.append("Home email address is not valid\r\n");
         }
         if (!Validators.isEmailValid(otherEmailField.getText())) {
             message.append("Other email address is not valid\r\n");
         }
         if (!Validators.isUrlValid(workUrlField.getText())) {
             message.append("Work URL is not valid\r\n");
         }
         if (!Validators.isUrlValid(homeUrlField.getText())) {
             message.append("Home URL is not valid\r\n");
         }
         if (!Validators.isUrlValid(otherUrlField.getText())) {
             message.append("Other URL is not valid\r\n");
         }
         if (!Validators.isPhoneNumberValid(workPhoneField.getText())) {
             message.append("Work phone is not valid\r\n");
         }
         if (!Validators.isPhoneNumberValid(homePhoneField.getText())) {
             message.append("Home phone is not valid\r\n");
         }
         if (!Validators.isPhoneNumberValid(otherPhoneField.getText())) {
             message.append("Other phone is not valid\r\n");
         }
         if (!Validators.isPhoneNumberValid(otherPhoneField.getText())) {
             message.append("Other phone is not valid\r\n");
         }
         if (!Validators.isIcqValid(icqField.getText())) {
             message.append("Icq number is not valid\r\n");
         }
         if (!Validators.isJabberValid(jabberField.getText())) {
             message.append("Jabber acount is not valid\r\n");
         }
         if (!Validators.isSkypeValid(skypeField.getText())) {
             message.append("Skype acount is not valid\r\n");
         }
 
         // display message if is there same error
         if (message.length() > 0) {
             JOptionPane.showMessageDialog(this, message, "Validation failed!", JOptionPane.WARNING_MESSAGE);
         }
 
         return message.length() == 0;
     }
 
     /**
      * Submiting new contact action
      */
     private class NewContactActionListener implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             if (resolvecontact()) {
                 List<Contact> newContacts = new ArrayList<Contact>();
                 newContacts.add(contact);
                 db.addNewContacts(newContacts);
                 // update tables
                 ContactsPanel.fillTable(false, true);
                 GroupsPanel.fillList();
                 log("Closing new contact window.");
                 dispose();
             }
         }
     }
 
     /**
      * Confirming contact change action
      */
     private class EditContactActionListener implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             if (resolvecontact()) {
                 db.updateContact(contact);
                 ContactsPanel.fillTable(true, false);
                 log("Closing edit contact window.");
                 dispose();
             }
         }
     }
 
     /**
      * Method for messages logging.
      *
      * @param msg message to log
      */
     private void log(String msg) {
         LoggerFactory.getLogger(this.getClass()).info(msg);
     }
 }
