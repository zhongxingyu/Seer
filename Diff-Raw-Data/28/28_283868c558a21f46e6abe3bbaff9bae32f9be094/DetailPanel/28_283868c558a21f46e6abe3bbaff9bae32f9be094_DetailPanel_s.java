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
 	private final JPanel address = new JPanel();
 	private final JPanel emails = new JPanel();
 	private final JLabel phones = new JLabel();
 	private final JLabel webs = new JLabel();
 	private final JLabel birthday = new JLabel();
 	private final JLabel note = new JLabel();
 	private final JLabel nameIcon = new JLabel();
 	private final PhotoButton photo = new PhotoButton();
 	private final JLabel groups = new JLabel();
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
 		nameIcon.setIcon(new ImageIcon(getClass().getResource("/res/present.png")));
 		nameIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
 		nameIcon.setVisible(false);
 		namePanel.add(nameIcon);
 		namePanel.add(name);
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
 		detailPanel.add(new JLabel("<html><b>Address:</b></html>"), c);
 		c.gridx = 1;
 		address.setLayout(new BoxLayout(address, BoxLayout.PAGE_AXIS));
 		address.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(address, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(new JLabel("<html><b>Email:</b></html>"), c);
 		c.gridx = 1;
 		emails.setLayout(new BoxLayout(emails, BoxLayout.PAGE_AXIS));
 		emails.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(emails, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(new JLabel("<html><b>Phone:</b></html>"), c);
 		c.gridx = 1;
 		phones.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(phones, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(new JLabel("<html><b>Webs:</b></html>"), c);
 		c.gridx = 1;
 		webs.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(webs, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(new JLabel("<html><b>Birthday:</b></html>"), c);
 		c.gridx = 1;
 		birthday.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(birthday, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(new JLabel("<html><b>Note:</b></html>"), c);
 		c.gridx = 1;
 		note.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
 		detailPanel.add(note, c);
 		c.gridx = 0;
 		c.gridy++;
 		detailPanel.add(new JLabel("<html><b>Groups:</b></html>"), c);
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
 				nameIcon.setVisible(true);
 			} else {
 				nameIcon.setVisible(false);
 			}
 			photo.setContact(contact);
 			name.setText(String.format("<html><h1>" + contact.getFullName() + "</h1></html>"));
 			address.removeAll();
 			for (Address a : contact.getAdresses()) {
 				if (!a.getAddress().isEmpty()){
 					//System.out.println(a);
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
 			for (Email e : contact.getEmails()) {
 				if (!e.getEmail().isEmpty()) {
 					JLabelButton lb = new JLabelButton(e.getEmail());
 					lb.setCursor(new Cursor(Cursor.HAND_CURSOR));
 					lb.addActionListener(new EmailListener());
 					emails.add(lb);
 				}
 			}
 			phones.setText("<html>" + contact.getAllPhones().replaceAll(", ", "<br>") + "</html>");
 			if (contact.getDateOfBirth() != null) {
 				birthday.setText(DateFormat.getDateInstance().format(contact.getDateOfBirth()));
 			} else {
 				birthday.setText("");
 			}
 			note.setText(contact.getNote());
 
 			String separator = "";
 			final StringBuilder groupstring = new StringBuilder();
 			for (Group g : db.getAllGroupsForContact(contact)) {
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
 }
