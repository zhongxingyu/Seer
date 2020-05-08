 package cz.vutbr.fit.gja.gjaddr.gui;
 
 import cz.vutbr.fit.gja.gjaddr.gui.util.Validators;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.*;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.util.TypesEnum;
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.*;
 
 /**
  * Contact editing window.
  *
  * @author Bc. Jan Kaláb <xkalab00@stud.fit,vutbr.cz>
  */
 class ContactWindow extends JFrame {
 	static final long serialVersionUID = 0;
 
 	private final Database db = Database.getInstance();
 
 	private Contact contact;
 	private final JButton button = new JButton();
 	private final PhotoButton photo = new PhotoButton();
 	private final JTextField nameField = new JTextField();
 	private final JTextField surnameField = new JTextField();
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
 
 	/**
 	 * Constructor for adding new contact
 	 */
 	public ContactWindow() {
 		super("Add contact");
 		setIconImage(new ImageIcon(getClass().getResource("/res/plus.png"), "+").getImage());
 		photo.setIcon(new ImageIcon(getClass().getResource("/res/photo.png"), ":)"));
 		button.setText("Add contact");
 		button.addActionListener(new NewContactActionListener());
 		contact = new Contact();
 		prepare();
 	}
 
 	/**
 	 * Constructor for editing contact
 	 */
 	public ContactWindow(Contact contact) {
 		super("Edit contact");
 		this.contact = contact;
 		setIconImage(new ImageIcon(getClass().getResource("/res/edit.png"), "Edit").getImage());
 		photo.setContact(contact);
 		button.setText("Edit contact");
 		button.addActionListener(new EditContactActionListener());
 		nameField.setText(contact.getFirstName());
 		surnameField.setText(contact.getSurName());
 		addressField.setText(contact.getAllAddresses());
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
 		prepare();
 	}
 
 	/**
 	 * Method for creating window content layout.
 	 */
 	private void prepare() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (Exception e) {
 			System.err.println(e);
 		}
 		final JPanel form = new JPanel(new GridBagLayout());
 		form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		final GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(1, 1, 1, 1);
 		c.weightx = 0;
 		c.anchor = GridBagConstraints.NORTHWEST;
 		c.gridx = 0;
 		c.gridy = 0;
 		form.add(new JLabel("Photo"), c);
 		c.gridx = 1;
 		c.weightx = 1;
 		form.add(photo, c);
 		c.gridy++;
 		c.gridx = 0;
 		c.weightx = 0;
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
 		form.add(new JLabel("Address"), c);
 		c.gridx = 1;
 		c.weightx = 1;
 		form.add(addressField, c);
 		c.gridy++;
 		c.gridx = 0;
 		c.weightx = 0;
 		form.add(new JLabel("Work E-mail"), c);
 		c.gridx = 1;
 		c.weightx = 1;
 		form.add(workEmailField, c);
 		c.gridy++;
 		c.gridx = 0;
 		c.weightx = 0;
 		form.add(new JLabel("Home E-mail"), c);
 		c.gridx = 1;
 		c.weightx = 1;
 		form.add(homeEmailField, c);
 		c.gridy++;
 		c.gridx = 0;
 		c.weightx = 0;
 		form.add(new JLabel("Other E-mail"), c);
 		c.gridx = 1;
 		c.weightx = 1;
 		form.add(otherEmailField, c);
 		c.gridy++;
 		c.gridx = 0;
 		c.weightx = 0;
 		form.add(new JLabel("Work URL"), c);
 		c.gridx = 1;
 		c.weightx = 1;
 		form.add(workUrlField, c);
 		c.gridy++;
 		c.gridx = 0;
 		c.weightx = 0;
 		form.add(new JLabel("Home URL"), c);
 		c.gridx = 1;
 		c.weightx = 1;
 		form.add(homeUrlField, c);
 		c.gridy++;
 		c.gridx = 0;
 		c.weightx = 0;
 		form.add(new JLabel("Other URL"), c);
 		c.gridx = 1;
 		c.weightx = 1;
 		form.add(otherUrlField, c);
 		c.gridy++;
 		c.gridx = 0;
 		c.weightx = 0;
 		form.add(new JLabel("Work Phone"), c);
 		c.gridx = 1;
 		c.weightx = 1;
 		form.add(workPhoneField, c);
 		c.gridy++;
 		c.gridx = 0;
 		c.weightx = 0;
 		form.add(new JLabel("Home Phone"), c);
 		c.gridx = 1;
 		c.weightx = 1;
 		form.add(homePhoneField, c);
 		c.gridy++;
 		c.gridx = 0;
 		c.weightx = 0;
 		form.add(new JLabel("Other Phone"), c);
 		c.gridx = 1;
 		c.weightx = 1;
 		form.add(otherPhoneField, c);
 		add(form, BorderLayout.CENTER);
 		add(button, BorderLayout.PAGE_END);
 		setLocationRelativeTo(null);
 		pack();
 		setVisible(true);
 	}
 
 	private boolean resolvecontact() {
 
 		boolean valid = this.validateData();
 		if (valid) {
 			contact.setPhoto((ImageIcon) photo.getIcon());
 			contact.setFirstName(nameField.getText());
 			contact.setSurName(surnameField.getText());
 
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
 		}
 		return valid;
 	}
 
 	/**
 	 * Check if is email and url valid, if is not valid, display a message.
 	 * @return true if is validation successfull, otherwise false.
 	 */
 	private boolean validateData() {
 		StringBuilder message = new StringBuilder();
 
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
 			List<Contact> newContacts = new ArrayList<Contact>();
 
 			boolean result = resolvecontact();
 			if (!result) {
 				return;
 			}
 
 			newContacts.add(contact);
 			db.addNewContacts(newContacts);
 
 			// update tables
 			ContactsPanel.fillTable(false);
 			GroupsPanel.fillList();
 
 			dispose();
 		}
 	}
 
 	/**
 	 * Confirming contact change action
 	 */
 	private class EditContactActionListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {

			boolean result = resolvecontact();
			if (!result) {
				return;
 			}

			db.updateContact(contact);
			ContactsPanel.fillTable(false);
			dispose();
 		}
 	}
 }
