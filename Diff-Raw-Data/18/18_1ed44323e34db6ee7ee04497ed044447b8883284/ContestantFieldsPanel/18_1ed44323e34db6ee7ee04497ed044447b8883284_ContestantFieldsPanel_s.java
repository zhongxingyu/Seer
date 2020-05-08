 package admin.panel.person.contestant;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import admin.MainFrame;
 import admin.panel.person.PersonFields;
 import data.Contestant;
 import data.GameData;
 import data.InvalidFieldException;
 
 public class ContestantFieldsPanel extends JPanel implements PersonFields<Contestant> {
 
 	private static final long serialVersionUID = 1L;
 
 	// Store external references:
 	private JButton imgDisplay;
 	private String imgPath;
 	
 	private JLabel labelName;
 	// TODO: Refactor to something more obvious?
 	private JLabel labelCastOff;
 	private JComboBox<String> cbCastDate;
 	private JButton btnCastOff;
 	
 	private JLabel labelTribe;
 
 	private JLabel labelID;
 	private JTextField tfContID;
 
 	private JTextField tfFirstName;
 	private JTextField tfLastName;
 	private JComboBox<String> cbTribe;
 
 	// store internal
 	private GridBagLayout gbFields;
 	private GridBagConstraints gbFieldsConst;
 
 	
 	
 	// constants:
 	private static final String DEFAULT_PICTURE = "res/test/defaultpic.png";
 	private static final int IMAGE_MAX_DIM = 75;
 
 	public ContestantFieldsPanel(JButton _imgButton, JLabel _labelName, JTextField _tfFirstName,
 			JTextField _tfLastName, JLabel _labelID, JTextField _tfContID,
 			JLabel _labelCastOff, JComboBox<String> _cbCastDate, JButton _btnCastOff,
 			JLabel _labelTribe, JComboBox<String> _cbTribe) {
 		super();
 
 		setLayout(new BorderLayout(10, 10));
 		
 		imgDisplay = _imgButton;
 		
 		// passed in
 		labelName = _labelName;
 		labelCastOff = _labelCastOff;
 		cbCastDate = _cbCastDate;
 		btnCastOff = _btnCastOff;
 		
 		labelTribe = _labelTribe;
 
 		tfFirstName = _tfFirstName;
 		tfLastName = _tfLastName;
 		cbTribe = _cbTribe;
 
 		labelID = _labelID;
 		tfContID = _tfContID;
 		
 		gbFields = new GridBagLayout();
 		gbFieldsConst = new GridBagConstraints();
 
 		JPanel fieldsPane = new JPanel();
 		fieldsPane.setLayout(gbFields);
 		setupGridBag(fieldsPane, gbFields, gbFieldsConst);
 		
 		add(imgDisplay, BorderLayout.LINE_START);
 		add(fieldsPane, BorderLayout.CENTER);
 	}
 
 	private void setupGridBag(JPanel pane, GridBagLayout gbl, GridBagConstraints gbc) {
 		// gbc.insets = new Insets(5, 5, 5, 5);
 
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		gbc.weighty = 0.5;
 		gbc.weightx = 0.5;
 		gbc.gridy = 0;
 
 		// row: [Name Label] [First Name] [Last Name]
 		gbc.gridx = 0;
 		gbc.gridy++;
 		gbc.weightx = 0.25;
 		gbc.insets = new Insets(0, 0, 5, 10);
 		gbc.anchor = GridBagConstraints.LINE_START;
 		pane.add(labelName, gbc);
 
 		gbc.weightx = 0.5;
 		gbc.gridx = GridBagConstraints.RELATIVE;
 		gbc.anchor = GridBagConstraints.CENTER;
 		gbc.insets = new Insets(0, 0, 5, 5);
 		pane.add(tfFirstName, gbc);
 
 		gbc.anchor = GridBagConstraints.LINE_END;
 		gbc.insets = new Insets(0, 0, 5, 0);
 		pane.add(tfLastName, gbc);
 
 		// row: [ID Label] [ID TextEdit]
 		gbc.gridx = 0;
 		gbc.gridy++;
 		gbc.weightx = 0.25;
 		gbc.anchor = GridBagConstraints.LINE_START;
 		gbc.insets = new Insets(0, 0, 5, 10);
 		pane.add(labelID, gbc);
 
 		gbc.weightx = 0.5;
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		gbc.gridx = GridBagConstraints.RELATIVE;
 		gbc.insets = new Insets(0, 0, 5, 0);
 		pane.add(tfContID, gbc);
 
 		// row: [Date Label] [Date TF] [btn Cast off]
 		gbc.gridx = 0;
 		gbc.gridwidth = 1;
 		gbc.gridy++;
 		gbc.weightx = 0.25;
 		gbc.anchor = GridBagConstraints.LINE_START;
 		gbc.insets = new Insets(0, 0, 5, 10);
 		pane.add(labelCastOff, gbc);
 		
 		gbc.weightx = 0.25;
 		gbc.gridx = GridBagConstraints.RELATIVE;
 		gbc.anchor = GridBagConstraints.CENTER;
 		gbc.insets = new Insets(0, 0, 5, 5);
 		pane.add(cbCastDate, gbc);
 
 		//gbc.weightx = 0.25;
 		gbc.anchor = GridBagConstraints.LINE_END;
 		gbc.insets = new Insets(0, 0, 5, 0);
 		pane.add(btnCastOff, gbc);
 
 		// row: [Combo Label] [Combo Box]
 		gbc.gridx = 0;
 		gbc.gridy++;
 		gbc.gridwidth = 1;
 		gbc.weightx = 0.25;
 		gbc.anchor = GridBagConstraints.LINE_START;
 		gbc.insets = new Insets(0, 0, 5, 10);
 		pane.add(labelTribe, gbc);
 
 		gbc.weightx = 0.5;
 		gbc.gridx = GridBagConstraints.RELATIVE;
 		gbc.anchor = GridBagConstraints.LINE_END;
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		gbc.insets = new Insets(0, 0, 5, 0);
 		pane.add(cbTribe, gbc);
 	}
 	
 	/**
 	 * Updates the image displayed to have the path associated, helper method <br>
 	 * <b>Note:</b> Pictures must be PNG format.
 	 * 
 	 * @param path
 	 *            Path to new image.
 	 */
 	// apparently images have to be .png and alphanumeric
 	protected void updateContPicture(String path) {
 		// don't update if its already correct!
 		if (imgPath == path) {
 			return;
 		}
 
 		try {
 			Image img = ImageIO.read(new File(path));
 			if (img == null)
 				throw new IOException();
 
 			// TODO: Make this scale more approriately using Image's
 			// resolution/aspect ratio
 			// scale the image!
 			if (img.getWidth(null) > IMAGE_MAX_DIM
 					|| img.getHeight(null) > IMAGE_MAX_DIM) {
 				img = img.getScaledInstance(
 						Math.min(IMAGE_MAX_DIM, img.getWidth(null)),
 						Math.min(IMAGE_MAX_DIM, img.getHeight(null)),
 						Image.SCALE_SMOOTH);
 			}
 
 			// NO IO errors occured if getting here:
 			ImageIcon imgD = new ImageIcon(img);
 			imgDisplay.setIcon(imgD);
 			imgPath = path;
 		} catch (IOException e) {
 			System.out.println("Exception loading image for contestant "
 					+ "picture [" + path + "]");
 			imgDisplay.setIcon(null);
 			MainFrame.getRunningFrame().setStatusErrorMsg("Could not load: "+path,imgDisplay );
 		}
 
 	}
 
 	/**
 	 * Sets the editing information to the information stored in the contestant
 	 * 
 	 * @param c
 	 *            The contestant to edit
 	 */
 	@Override
 	public void setEditPane(Contestant c, boolean newContestant) {
 		
 		GameData g = GameData.getCurrentGame();
 		tfContID.setEnabled(!g.isSeasonStarted());
 
 		if (newContestant || c == null) {
 			// set default values
 			tfContID.setText("");
 			tfFirstName.setText("First Name");
 			tfLastName.setText("Last Name");
 
 			cbTribe.setSelectedIndex(0);
 			
 			updateContPicture(DEFAULT_PICTURE);
 			return;
 		}
 
 		tfFirstName.setText(c.getFirstName());
 		tfLastName.setText(c.getLastName());
 		
 		cbTribe.setSelectedItem(c.getTribe());
 
 		tfContID.setText(c.getID());
 		
 		if (c.isCastOff())
 			cbCastDate.setSelectedIndex(g.getCurrentWeek()-c.getCastDate()+1);
 		else
 			cbCastDate.setSelectedIndex(0);
 		
 		updateContPicture(c.getPicture());
 	}
 	
 	/**
 	 * gets the current information with the current contestant, will update
 	 * from the fields associated.
 	 * 
 	 * @return Current contestant loaded
 	 * @throws InvalidFieldException
 	 *             Thrown on any bad fields passed
 	 */
 	@Override
 	public void getFromPane(Contestant c) throws InvalidFieldException {
 		c.setID(tfContID.getText());
 		c.setFirstName(tfFirstName.getText().trim());
 		c.setLastName(tfLastName.getText().trim());
 		c.setTribe((String) cbTribe.getSelectedItem());
 		c.setPicture(imgPath);
 	}
 }
