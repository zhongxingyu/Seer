 package cz.vutbr.fit.gja.gjaddr.gui;
 
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import javax.swing.*;
 
 /**
  * Panel with contact detail
  *
  * @author Bc. Jan Kaláb <xkalab00@stud.fit,vutbr.cz>
  */
 class DetailPanel extends JPanel {
 	static final long serialVersionUID = 0;
 	private final Database db = Database.getInstance();
 	private final JLabel name = new JLabel();
 	private final JLabel nickname = new JLabel();
 	private final JPanel address = new JPanel();
 	private final JPanel emails = new JPanel();
 	private final JLabel phones = new JLabel();
 	private final JPanel webs = new JPanel();
 	private final JLabel birthday = new JLabel();
 	private final JLabel nameday = new JLabel();
 	private final JLabel celebration = new JLabel();
 	private final JLabel note = new JLabel();
 	private final JLabel bdayIcon = new JLabel();
 	private final JLabel namedayIcon = new JLabel();
 	private final JLabel celebrationIcon = new JLabel();
 	private final PhotoButton photo = new PhotoButton();
 	private final JLabel groups = new JLabel();
 	private final JLabel nicknameLabel = new JLabel("<html><b>Nickname: </b></html>");
 	private final JLabel addressLabel = new JLabel("<html><b>Address: </b></html>");
 	private final JLabel emailLabel = new JLabel("<html><b>Email: </b></html>");
 	private final JLabel phoneLabel = new JLabel("<html><b>Phone: </b></html>");
 	private final JLabel websLabel = new JLabel("<html><b>Webs: </b></html>");
 	private final JLabel birthdayLabel = new JLabel("<html><b>Birthday: </b></html>");
 	private final JLabel namedayLabel = new JLabel("<html><b>Nameday: </b></html>");
 	private final JLabel celebrationLabel = new JLabel("<html><b>Celebration: </b></html>");
 	private final JLabel noteLabel = new JLabel("<html><b>Note: </b></html>");
 	private final JLabel groupsLabel = new JLabel("<html><b>Groups: </b></html>");
 	private JScrollPane detailScrollPane;
 
 	/**
 	 * Constructor
 	 */
 	public DetailPanel() {
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
 		final JLabel label = new JLabel("Detail");
 		label.setAlignmentX(CENTER_ALIGNMENT);
 		add(label);
 
 		// create panel with name and with birthday icon
 		final JPanel namePanel = new JPanel();
 		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.LINE_AXIS));
 		bdayIcon.setIcon(new ImageIcon(getClass().getResource("/res/present.png")));
 		bdayIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
 		bdayIcon.setVisible(false);
 		namedayIcon.setIcon(new ImageIcon(getClass().getResource("/res/nameday.png")));
 		namedayIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
 		namedayIcon.setVisible(false);
 		celebrationIcon.setIcon(new ImageIcon(getClass().getResource("/res/celebration.png")));
 		celebrationIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
 		celebrationIcon.setVisible(false);
 		namePanel.add(bdayIcon);
 		namePanel.add(namedayIcon);
 		namePanel.add(celebrationIcon);
 		namePanel.add(name);
 		photo.setVisible(false);
 		namePanel.add(photo);
 		/*
 		namePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
 		namePanel.setAlignmentY(Component.TOP_ALIGNMENT);
 		*/
 		add(namePanel);
 
 		final JPanel detailPanel = new JPanel(new GridBagLayout());
 		final GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 1;
 		c.anchor = GridBagConstraints.PAGE_START;
 		c.gridx = 0;
 		c.gridy = 0;
 		detailPanel.add(nicknameLabel, c);
 		c.gridx = 1;
 		nickname.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(nickname, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(addressLabel, c);
 		c.gridx = 1;
 		address.setLayout(new BoxLayout(address, BoxLayout.PAGE_AXIS));
 		address.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(address, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(emailLabel, c);
 		c.gridx = 1;
 		emails.setLayout(new BoxLayout(emails, BoxLayout.PAGE_AXIS));
 		emails.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(emails, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(phoneLabel, c);
 		c.gridx = 1;
 		phones.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(phones, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(websLabel, c);
 		c.gridx = 1;
 		webs.setLayout(new BoxLayout(webs, BoxLayout.PAGE_AXIS));
 		webs.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(webs, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(birthdayLabel, c);
 		c.gridx = 1;
 		birthday.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(birthday, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(namedayLabel, c);
 		c.gridx = 1;
 		birthday.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(nameday, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(celebrationLabel, c);
 		c.gridx = 1;
 		birthday.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(celebration, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(noteLabel, c);
 		c.gridx = 1;
 		note.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(note, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(groupsLabel, c);
 		c.gridx = 1;
 		//groups.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));	//Last without margin
 		detailPanel.add(groups, c);
 		c.gridy++;
 		c.weighty = 1;
 		detailPanel.add(Box.createVerticalGlue(), c);
 		detailScrollPane = new JScrollPane(detailPanel);
 		detailScrollPane.setBorder(BorderFactory.createEmptyBorder());
 		detailScrollPane.setVisible(false);
 		add(detailScrollPane);
 	}
 
 	void show(Contact contact) {
 		if (contact != null) {
 			if (contact.hasBirthday()) {
 				bdayIcon.setVisible(true);
 			} else {
 				bdayIcon.setVisible(false);
 			}
 			if (contact.hasNameDay()) {
 				namedayIcon.setVisible(true);
 			} else {
 				namedayIcon.setVisible(false);
 			}
 			if (contact.hasCelebration()) {
 				celebrationIcon.setVisible(true);
 			} else {
 				celebrationIcon.setVisible(false);
 			}
 			photo.setContact(contact);
 			photo.setVisible(true);
 			name.setText(String.format("<html><h1>" + contact.getFullName() + "</h1></html>"));
 			if (contact.getNickName() != null && !contact.getNickName().isEmpty()) {
 				nicknameLabel.setVisible(true);
 				nickname.setVisible(true);
 				nickname.setText(contact.getNickName());
 			} else {
 				nicknameLabel.setVisible(false);
 				nickname.setVisible(false);
 			}
 
 			address.removeAll();
 			addressLabel.setVisible(false);
 			for (Address a : contact.getAdresses()) {
 				if (!a.getAddress().isEmpty()) {
 					addressLabel.setVisible(true);
 					JLabelButton l = new JLabelButton();
 					l.setVerticalTextPosition(JLabel.TOP);
 					l.setHorizontalTextPosition(JLabel.CENTER);
 					l.setAlignmentX(Component.LEFT_ALIGNMENT);
 					l.setCursor(new Cursor(Cursor.HAND_CURSOR));
 					l.setText(a.getAddress());
 					try {
 						l.setIcon(new ImageIcon(new URL("http://maps.google.com/maps/api/staticmap?size=128x128&sensor=false&markers=" + URLEncoder.encode(l.getText(), "utf8"))));
 					} catch (IOException e) {
 						System.err.println(e);
 					}
 					l.addActionListener(new MapListener());
 					address.add(l);
 				}
 			}
 
 			emails.removeAll();
 			emailLabel.setVisible(false);
 			for (Email e : contact.getEmails()) {
 				if (!e.getEmail().isEmpty()) {
 					emailLabel.setVisible(true);
 					JLabelButton lb = new JLabelButton(e.getEmail());
 					lb.setCursor(new Cursor(Cursor.HAND_CURSOR));
 					lb.addActionListener(new EmailListener());
 					emails.add(lb);
 				}
 			}
 
 			webs.removeAll();
 			websLabel.setVisible(false);
 			for (Url u : contact.getUrls()) {
 				if (u.getValue() != null) {
 					websLabel.setVisible(true);
 					JLabelButton lb = new JLabelButton(u.getValue().toString());
 					lb.setCursor(new Cursor(Cursor.HAND_CURSOR));
 					lb.addActionListener(new WebListener());
					emails.add(lb);
 				}
 			}
 
 			if (!contact.getPhoneNumbers().isEmpty()) {
 				phoneLabel.setVisible(true);
 				phones.setVisible(true);
 				phones.setText("<html>" + contact.getAllPhones().replaceAll(", ", "<br>") + "</html>");
 			} else {
 				phoneLabel.setVisible(false);
 				phones.setVisible(false);
 			}
 
 			if (contact.getBirthday() != null && contact.getBirthday().getDate() != null) {
 				birthday.setVisible(true);
 				birthdayLabel.setVisible(true);
 				birthday.setText(DateFormat.getDateInstance().format(contact.getBirthday().getDate()));
 			} else {
 				birthday.setVisible(false);
 				birthdayLabel.setVisible(false);
 			}
 
 			if (contact.getNameDay() != null && contact.getNameDay().getDate() != null) {
 				nameday.setVisible(true);
 				namedayLabel.setVisible(true);
 				nameday.setText(DateFormat.getDateInstance().format(contact.getNameDay().getDate()));
 			} else {
 				nameday.setVisible(false);
 				namedayLabel.setVisible(false);
 			}
 
 			if (contact.getCelebration() != null && contact.getCelebration().getDate() != null) {
 				celebration.setVisible(true);
 				celebrationLabel.setVisible(true);
 				celebration.setText(DateFormat.getDateInstance().format(contact.getCelebration().getDate()));
 			} else {
 				celebration.setVisible(false);
 				celebrationLabel.setVisible(false);
 			}
 
 			if (contact.getNote() != null && !contact.getNote().isEmpty()) {
 				note.setVisible(true);
 				noteLabel.setVisible(true);
 				note.setText("<html>" + contact.getNote().replaceAll("\n", "<br>") + "</html>");
 			} else {
 				note.setVisible(false);
 				noteLabel.setVisible(false);
 			}
 
 			String separator = "";
 			final StringBuilder groupstring = new StringBuilder();
 			groups.setVisible(false);
 			groupsLabel.setVisible(false);
 			for (Group g : db.getAllGroupsForContact(contact)) {
 				groups.setVisible(true);
 				groupsLabel.setVisible(true);
 				groupstring.append(separator);
 				groupstring.append(g.getName());
 				separator = ", ";
 			}
 			groups.setText(groupstring.toString());
 
 			detailScrollPane.setVisible(true);
 		}
 	}
 
 	/**
 	 * Action for opening mail client
 	 */
 	private class EmailListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent ev) {
 			final JButton b = (JButton) ev.getSource();
 			try {
 				Desktop.getDesktop().mail(new URI("mailto", b.getText(), null));
 			} catch (URISyntaxException ex) {
 				System.err.println(ex);
 			} catch (IOException ex) {
 				System.err.println(ex);
 			}
 		}
 	}
 
 	/**
 	 * Action for opening browser with maps
 	 */
 	private class MapListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent ev) {
 			final JButton b = (JButton) ev.getSource();
 			try {
 				Desktop.getDesktop().browse(new URI("http://maps.google.com/maps?q=" + URLEncoder.encode(b.getText(), "utf8")));
 			} catch (URISyntaxException ex) {
 				System.err.println(ex);
 			} catch (IOException ex) {
 				System.err.println(ex);
 			}
 		}
 	}
 
 	/**
 	 * Action for opening browser with web
 	 */
 	private class WebListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent ev) {
 			final JButton b = (JButton) ev.getSource();
 			try {
 				Desktop.getDesktop().browse(new URI(b.getText()));
 			} catch (URISyntaxException ex) {
 				System.err.println(ex);
 			} catch (IOException ex) {
 				System.err.println(ex);
 			}
 		}
 	}
 }
