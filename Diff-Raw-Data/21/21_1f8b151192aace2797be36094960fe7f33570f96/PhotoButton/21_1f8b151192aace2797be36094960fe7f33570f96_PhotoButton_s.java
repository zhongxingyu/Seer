 package cz.vutbr.fit.gja.gjaddr.gui;
 
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Contact;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Database;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileFilter;
 
 /**
  * Button for changin contact photo
  *
  * @author Bc. Jan Kaláb <xkalab00@stud.fit,vutbr.cz>
  */
 class PhotoButton extends JButton {
 	static final long serialVersionUID = 0;
 	private Contact contact;
 	private final JFileChooser photochooser = new JFileChooser();
 
 	/**
 	 * Constructor
 	 * Creates button with plaeholder image
 	 */
 	PhotoButton() {
 		super();
         this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
         this.setFocusPainted(false);
         this.setFocusable(false);
 		setIcon(new ImageIcon(getClass().getResource("/res/photo.png")));
 		init();
 	}
 
 	/**
 	 * Constructor with contact
 	 * Assigns contact to button and shows it's photo
 	 */
 	PhotoButton(Contact contact) {
 		super();
         this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
         this.setFocusPainted(false);
         this.setFocusable(false);
 		setContact(contact);
 		init();
 	}
 
 	/**
 	 * Set contact for button, also changes photo icon
 	 */
 	public void setContact(Contact contact) {
 		this.contact = contact;
 		if (contact.getPhoto() != null) {
 			setIcon(contact.getPhoto());
 		} else {
 			setIcon(new ImageIcon(getClass().getResource("/res/photo.png"), ":)"));
 		}
 	}
 
 	/**
 	 * Initialization
 	 * Sets image file filter
 	 */
 	private void init() {
 		photochooser.setFileFilter(new ImageFileFilter());
 		addActionListener(new PhotoActionListener());
 	}
 
 	/**
 	 * Filter for image files
 	 */
 	private class ImageFileFilter extends FileFilter {
 		@Override
 		public boolean accept(File f) {
 			if (f.isDirectory()) {
 				return true;
 				}
 				String e = "";
 				String s = f.getName();
 				int i = s.lastIndexOf('.');
 				if (i > 0 && i < s.length() - 1) {
 					e = s.substring(i + 1).toLowerCase();
 				}
 				if (e.equals("jpg") || e.equals("jpeg") || e.equals("gif") || e.equals("png")) {
 					return true;
 				}
 				return false;
 		}
 
 		@Override
 		public String getDescription() {
 			return "Images";
 		}
 	}
 
 	/**
 	 * Action for loading photo
 	 */
 	private class PhotoActionListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (photochooser.showOpenDialog(PhotoButton.this) == JFileChooser.APPROVE_OPTION) {
 				File f = photochooser.getSelectedFile();
 				try {
 					final BufferedImage image = ImageIO.read(f);
 					final ImageIcon photo = new ImageIcon(image.getScaledInstance(50, 50, BufferedImage.SCALE_DEFAULT));
 					setIcon(photo);
					contact.setPhoto(photo);
					final Database db = Database.getInstance();
					db.updateContact(contact);
 				} catch (IOException ex) {
 					System.err.println(ex);
 				}
 			}
 		}
 	}
 }
