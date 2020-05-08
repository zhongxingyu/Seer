 package admin.panel.person.player;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import admin.panel.person.PersonFields;
 import data.Contestant;
 import data.InvalidFieldException;
 import data.User;
 
 public class PlayerFieldsPanel extends JPanel implements PersonFields<User> {
 
 	private static final long serialVersionUID = 1L;
 
 	// Store external references:
 	private JLabel labelName;
 	private JTextField tfFirstName;
 	private JTextField tfLastName;
 
 	private JLabel labelID;
 	private JTextField tfID;
 	private JButton btnGenID;
 
 	private JLabel labelWeekly;
 	private JComboBox<Contestant> cbWeeklyPick;
 
 	private JLabel labelUltimate;
 	private JComboBox<Contestant> cbUltPick;
 	
 	private JLabel labelPts;
 	private JLabel labelPtsValue;
 
 	// store internal
 	private GridBagLayout gbFields;
 	private GridBagConstraints gbFieldsConst;
 
 	public PlayerFieldsPanel(JLabel _labelName, JTextField _tfFirstName,
 			JTextField _tfLastName, JLabel _labelID, JTextField _tfID,
 			JButton _btnGenID, JLabel _labelWeekly,
 			JComboBox<Contestant> _cbWeeklyPick, JLabel _labelUltimate,
 			JComboBox<Contestant> _cbUltPick, JLabel ptsLabel, JLabel ptsValue) {
 		super();
 		
 		/// name
 		labelName = _labelName;
 		tfFirstName = _tfFirstName;
 		tfLastName = _tfLastName;
 		/// end name
 		
 		/// ID
 		labelID = _labelID;
 		tfID = _tfID;
 		btnGenID = _btnGenID;
 		/// end ID
 		
 		/// contestant picks:
 		labelWeekly = _labelWeekly;
 		cbWeeklyPick = _cbWeeklyPick;
 		
 		labelUltimate = _labelUltimate;
 		cbUltPick = _cbUltPick;
 		/// end contestant picks
 		
 		labelPts = ptsLabel;
 		labelPtsValue = ptsValue;
 		
 		gbFields = new GridBagLayout();
 		gbFieldsConst = new GridBagConstraints();
 
 		setLayout(gbFields);
 		setupGridBag(gbFields, gbFieldsConst);
 	}
 
 	private void setupGridBag(GridBagLayout gbl, GridBagConstraints gbc) {
 		// gbc.insets = new Insets(5, 5, 5, 5);
 
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		gbc.weighty = 0.5;
 		gbc.weightx = 0.5;
 		gbc.gridy = 0;
 		
 		JPanel ptsPanel = new JPanel();
 		ptsPanel.setLayout(new BoxLayout(ptsPanel, BoxLayout.PAGE_AXIS));
 		ptsPanel.add(labelPts);
 		ptsPanel.add(labelPtsValue);
 
 		// row: [Name Label] [First Name] [Last Name]
 		gbc.gridx = 0;
 		gbc.gridy++;
 		gbc.weightx = 0.25;
 		gbc.insets = new Insets(0, 0, 5, 10);
 		gbc.anchor = GridBagConstraints.LINE_START;
 		add(labelName, gbc);
 
 		gbc.weightx = 0.5;
 		gbc.gridx = GridBagConstraints.RELATIVE;
 		gbc.anchor = GridBagConstraints.CENTER;
 		gbc.insets = new Insets(0, 0, 5, 5);
 		add(tfFirstName, gbc);
 
 		gbc.anchor = GridBagConstraints.LINE_END;
 		gbc.insets = new Insets(0, 0, 5, 0);
 		add(tfLastName, gbc);
 
 		// row: [ID Label] [ID TextEdit] [Gen Button]
 		gbc.gridx = 0;
 		gbc.gridy++;
 		gbc.weightx = 0.25;
 		gbc.anchor = GridBagConstraints.LINE_START;
 		gbc.insets = new Insets(0, 0, 5, 10);
 		add(labelID, gbc);
 
 		gbc.weightx = 0.5;
 		gbc.gridx = GridBagConstraints.RELATIVE;
 		gbc.insets = new Insets(0, 0, 5, 5);
 		add(tfID, gbc);
 
 		gbc.weightx = 0.25;
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		gbc.insets = new Insets(0, 0, 5, 0);
 		add(btnGenID, gbc);
 
 		// row: [Combo Label] [Combo Box] [ pts label ]
 		gbc.gridx = 0;
 		gbc.gridy++;
 		gbc.gridwidth = 1;
 		gbc.anchor = GridBagConstraints.LINE_START;
 		gbc.insets = new Insets(0, 0, 5, 10);
 		add(labelWeekly, gbc);
 
 		gbc.gridx = GridBagConstraints.RELATIVE;
 		gbc.anchor = GridBagConstraints.LINE_END;
 		gbc.gridwidth = GridBagConstraints.RELATIVE;
 		gbc.insets = new Insets(0, 0, 5, 5);
 		add(cbWeeklyPick, gbc);
 		
 		gbc.weightx = 0.5;
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		gbc.gridheight = 2;
 		gbc.anchor = GridBagConstraints.LINE_END;
 		gbc.insets = new Insets(0, 0, 5, 0);
 		add(ptsPanel, gbc);
 
 		// row: [Combo Label] [Combo Box] [pts label cont]
 		gbc.gridx = 0;
 		gbc.gridy++;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		gbc.anchor = GridBagConstraints.LINE_START;
 		gbc.insets = new Insets(0, 0, 5, 10);
 		add(labelUltimate, gbc);
 
 		gbc.gridx = GridBagConstraints.RELATIVE;
 		//gbc.anchor = GridBagConstraints.LINE_START;
 		gbc.gridwidth = GridBagConstraints.RELATIVE;
 		gbc.insets = new Insets(0, 0, 5, 5);
 		add(cbUltPick, gbc);
 	}
 
 	@Override
 	public void setEditPane(User u, boolean newUser) {
 		if (newUser || u == null) {
 			// set default values
 			tfID.setText("");
 			tfFirstName.setText("First Name");
 			tfLastName.setText("Last Name");
 
 			cbUltPick.setSelectedIndex(0);
 			cbWeeklyPick.setSelectedIndex(0);
 
 			labelPtsValue.setText("" + 0);
 			
 			return;
 		}
 
 		tfID.setText(u.getID());
 
 		tfFirstName.setText(u.getFirstName());
 		tfLastName.setText(u.getLastName());
 
 		labelPtsValue.setText(Integer.toString(u.getPoints()));
 
 		// iterate through combo boxes setting indexes as necessary
 		Contestant ultPick = u.getUltimatePick();
 		if (ultPick == null) {
 			ultPick = new Contestant();
 			ultPick.setNull();
 		}
 		Contestant weekPick = u.getWeeklyPick();
 		if (weekPick == null) {
 			weekPick = new Contestant();
 			weekPick.setNull();
 		}
 
 		boolean ultSet = false, weekSet = false;
 
 		for (int i = 0; i < cbUltPick.getItemCount(); i++) {
 
 			// get the contestant to compare with, both store same values
 			Contestant cbCon = cbUltPick.getItemAt(i);
 			if (!ultSet && ultPick.getID().equals(cbCon.getID())) {
 				cbUltPick.setSelectedIndex(i);
 				ultSet = true;
 			}
 
 			if (!weekSet && weekPick.getID().equals(cbCon.getID())) {
 				cbWeeklyPick.setSelectedIndex(i);
 				weekSet = true;
 			}
 
 			if (ultSet && weekSet)
 				break; // break if both are set
 		}
 	}
 
 	@Override
 	public void getFromPane(User u) throws InvalidFieldException {
 		u.setID(tfID.getText());
 
 		u.setFirstName(tfFirstName.getText().trim());
 		u.setLastName(tfLastName.getText().trim());
 		
 		// not necessary since we cant change the pts anyway
 		//u.setPoints(Integer.parseInt(labelPtsValue.getText()));
 
 		int item = cbUltPick.getSelectedIndex();
 		Contestant c = cbUltPick.getItemAt(item);
 		u.setUltimatePick(c);
 
 		item = cbWeeklyPick.getSelectedIndex();
 		c = cbWeeklyPick.getItemAt(item);
 		u.setWeeklyPick(c);
 	}
 }
