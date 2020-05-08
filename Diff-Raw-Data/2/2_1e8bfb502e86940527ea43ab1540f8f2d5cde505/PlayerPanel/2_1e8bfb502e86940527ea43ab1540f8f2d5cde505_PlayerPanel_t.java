 package admin.panel.person.player;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.UIManager;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.table.TableCellRenderer;
 
 import admin.MainFrame;
 import admin.Utils;
 import admin.panel.person.PersonPanel;
 import data.Contestant;
 import data.GameData;
 import data.InvalidFieldException;
 import data.Person;
 import data.User;
 
 /**
  * TODO: Doc
  * 
  * @author kevin
  * 
  */
 public class PlayerPanel extends PersonPanel<User> implements ChangeListener,
 		MouseListener, Observer {
 
 	private static final long serialVersionUID = 1L;
 
 	// input fields:
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
 
 	// etc
 	/*
 	 * FIXME: Break into two labels one with "Points:" other with actual value
 	 * of pts
 	 */
 	private JLabel labelPts;
 	
 	// Constants:
 	protected static final String TOOL_NAME = "First and Last name must be alphabetic";
 	protected static final String TOOL_IDTXT = "ID must be 2-7 chars long and may end with numbers";
 	protected static final String TOOL_IDBTN = "Click to auto-generate ID from first and last name";
 	protected static final String TOOL_WEEKLY = "Select Weekly pick";
 	protected static final String TOOL_ULT = "Select Ultimate Winner";
 	protected static final String TOOL_SAVE = "";
 	protected static final String TOOL_DELETE = "Remove selected User from system";
 	protected static final String TOOL_NEW = "Add a new User to system";
 	protected static final String TOOL_TABLE = "Click Heading to sort by column";
 	
 	/**
 	 * THIS VARIABLE IS A REFERENCE MAINTAINED INTERNALLY. DO NOT ADJUST UNLESS
 	 * YOU KNOW WHAT YOU ARE DOING.
 	 */
 	private Person loadedPerson;
 
 	public PlayerPanel() {
 		super(new User());
 
 		setLayout(new BorderLayout(5, 5));
 
 		// ////////////////////////////
 		// Top Panel:
 		// ////////////////////////////
 		labelName = new JLabel("Name:");
 		tfFirstName = new JTextField();
 		tfLastName = new JTextField();
 
 		labelID = new JLabel("User ID:");
 		tfID = new JTextField();
 		btnGenID = new JButton("Generate ID");
 
 		labelWeekly = new JLabel("Weekly Pick:");
 		cbWeeklyPick = new JComboBox<Contestant>();
 
 		labelUltimate = new JLabel("Ultimate Pick:");
 		cbUltPick = new JComboBox<Contestant>();
 
 		personFields = new PlayerFieldsPanel(labelName, tfFirstName,
 				tfLastName, labelID, tfID, btnGenID, labelWeekly, cbWeeklyPick,
 				labelUltimate, cbUltPick);
 		// add the mouse listener to all components.
 		for (Component c : ((JPanel)personFields).getComponents()) {
 			c.addMouseListener(this);
 		}
 
 		// right side!
 		labelPts = new JLabel("Points: 0");
 
 		// ////////////////////////////
 		// Mid
 		// ////////////////////////////
 		// handled in super
 
 		// ////////////////////////////
 		// Bottom
 		// ////////////////////////////
 	
 
 		assembleAll();
 	}
 
 	/**
 	 * Builds the top panel including all the editable information
 	 */
 	@Override
 	protected void buildTopPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout(10, 10));
 
 		// this does not need to be referenced else where, only for layout
 		JPanel rightPane = new JPanel();
 		BoxLayout b = new BoxLayout(rightPane, BoxLayout.Y_AXIS);
 		rightPane.setLayout(b);
 		rightPane.add(Box.createVerticalStrut(32));
 		rightPane.add(labelPts);
 		rightPane.add(Box.createVerticalGlue());
 		rightPane.add(btnSave);
 		rightPane.add(Box.createVerticalStrut(32));
 
 		// add all components on top:
 		panel.add((JPanel)personFields, BorderLayout.CENTER);
 		panel.add(rightPane, BorderLayout.LINE_END);
 
 		add(panel, BorderLayout.PAGE_START);
 
 		// add the mouse listener to all components.
 		for (Component c : panel.getComponents()) {
 			c.addMouseListener(this);
 		}
 
 		for (Component c : rightPane.getComponents())
 			c.addMouseListener(this);
 	}
 
 	@Override
 	protected void buildBottomPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		
 		panel.add(btnAddNew);
 		panel.add(btnDelete);
 
 		add(panel, BorderLayout.PAGE_END);
 		// add the mouse listener to all components.
 		for (Component c : panel.getComponents()) {
 			c.addMouseListener(this);
 		}
 	}
 
 	/**
 	 * Currently used to check if a tab is changed, and if its changed to the
 	 * PlayerPanel, it will modify ComboBoxes.
 	 */
 	@Override
 	public void stateChanged(ChangeEvent e) {
 
 		Object obj = e.getSource();
 		if (obj instanceof JTabbedPane) // tab clicked
 		{
 
 			JTabbedPane tab = (JTabbedPane) obj;
 
 			if (tab.getSelectedIndex() != 2)
 				return;
 
 			refreshContestantCBs();
 			return;
 		}
 	}
 
 	/**
 	 * Sets the user on the screen to the specified container. If newUser is
 	 * true, it will specify that when save is hit, then the GUI should add it
 	 * to the table rather than modify a pre-existing data.
 	 * 
 	 * @param u
 	 * @param newUser
 	 */
 	@Override
 	protected void setPanelPerson(User u, boolean newUser) {
 		super.setPanelPerson(u, newUser);
 		
 		btnSave.setEnabled(false);
 
 		if (newUser || u == null) {
 			// we don't want any rows selected
 			ListSelectionModel m = table.getSelectionModel();
 			m.clearSelection();
 			
 			return;
 		}
 		
 		tableModel.setRowSelect(u);
 	}
 
 	/**
	 * Sets the error information based on an exception!
 	 * 
 	 * @param e
 	 *            Exception with the information necessary
 	 */
 	@Override
 	protected void setExceptionError(InvalidFieldException e) {
 		if (e.isHandled())
 			return;
 
 		MainFrame mf = MainFrame.getRunningFrame();
 
 		switch (e.getField()) {
 		case USER_ID:
 			mf.setStatusErrorMsg("Invalid ID (must be between 2 and 7 chars"
 					+ " long, followed by numbers)", tfID);
 			break;
 		case USER_ID_DUP:
 			mf.setStatusErrorMsg("Invalid ID (in use)", tfID);
 			break;
 		case USER_FIRST:
 			mf.setStatusErrorMsg("Invalid First Name (must be alphabetic"
 					+ ", 1-20 characters)", tfFirstName);
 			break;
 		case USER_LAST:
 			mf.setStatusErrorMsg("Invalid Last Name (must be alphabetic"
 					+ ", 1-20 characters)", tfLastName);
 			break;
 		case USER_ULT_PICK:
 			mf.setStatusErrorMsg("Invalid Ultimate Pick", cbUltPick);
 			break;
 		case USER_WEEKLY_PICK:
 			mf.setStatusErrorMsg("Invalid Weekly Pick", cbWeeklyPick);
 			break;
 		default:
 			mf.setStatusErrorMsg("Unknown problem with fields");
 		}
 
 		e.handle();
 	}
 
 	@Override
 	protected void buildActions() {
 		super.buildActions();
 		
 		btnAddNew.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (fieldsChanged) {
 					try {
 						savePerson();
 					} catch (InvalidFieldException ex) {
 						// can't add new.. :/
 						setExceptionError(ex);
 						return;
 					}
 				}
 
 				setPanelPerson(null, true);
 			}
 
 		});
 
 		/* TODO: Is this necessary?
 		btnSave.addActionListener(new ActionListener() {
 			// FIXME: global setting? Its reset every time the GUI is loaded
 			// right now
 			boolean dontShowConfirm = false;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				// make sure they want to save initially.
 				if (isNewUser && !dontShowConfirm) {
 					JCheckBox checkbox = new JCheckBox("Don't show again?");
 					String msg = "Would you like to save a new selected "
 							+ "user? You can not change ID after first save.";
 					Object[] objs = { msg, checkbox };
 
 					int response = JOptionPane.showConfirmDialog(null, objs,
 							"Save User?", JOptionPane.YES_NO_OPTION);
 
 					dontShowConfirm = checkbox.isSelected();
 					if (response == JOptionPane.NO_OPTION) {
 						return;
 					}
 				}
 
 				if (fieldsChanged) {
 					try {
 						saveUser();
 					} catch (InvalidFieldException ex) {
 					}
 				}
 			}
 		}); */
 
 		btnGenID.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				User u = new User();
 				try {
 					u.setFirstName(tfFirstName.getText().trim());
 					u.setLastName(tfLastName.getText().trim());
 				} catch (InvalidFieldException ex) {
 					setExceptionError(ex);
 					return;
 				}
 				GameData g = GameData.getCurrentGame();
 				List<Person> userList = Utils.castListElem(g.getAllUsers(),
 						(Person) (new User())); // lol so ugly.
 
 				String id = Utils.generateID(u, userList);
 
 				tfID.setText(id);
 			}
 		});
 
 		List<JTextField> tfArr = Arrays.asList(tfID, tfFirstName, tfLastName);
 		for (JTextField tf : tfArr) {
 			tf.addFocusListener(editAdapt);
 		}
 	}
 
 	/**
 	 * Loads the contestant data in the ComboBoxes from the GameData.
 	 */
 	private void refreshContestantCBs() {
 		GameData g = (GameData) GameData.getCurrentGame();
 
 		if (g == null) {
 			return;
 		}
 
 		List<Contestant> cons = g.getActiveContestants();
 
 		cbWeeklyPick.removeAllItems();
 		cbUltPick.removeAllItems();
 
 		boolean seasonStarted = g.isSeasonStarted();
 		cbUltPick.setEnabled(seasonStarted);
 		cbWeeklyPick.setEnabled(seasonStarted);
 
 		Contestant nullC = new Contestant();
 		nullC.setNull();
 
 		cbWeeklyPick.addItem(nullC);
 		cbUltPick.addItem(nullC);
 
 		if (seasonStarted) {
 			for (Contestant c : cons) {
 				cbWeeklyPick.addItem(c);
 				cbUltPick.addItem(c);
 			}
 		}
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent me) {
 		Component c = me.getComponent();
 		if (c == tfFirstName || c == tfLastName || c == tfID || c == btnGenID || 
 				c == cbUltPick || c == cbWeeklyPick) {
 			fieldsChanged = true;
 			btnSave.setEnabled(c.isEnabled());
 		}
 	}
 
 	/**
 	 * Changes all fields that have data changed. <br>
 	 * Currently calls: - Table update - Updates ComboBoxes
 	 */
 	@Override
 	public void update(Observable o, Object arg) {
 		GameData g = (GameData)o;
 		btnAddNew.setEnabled(!g.isSeasonStarted());
 		btnDelete.setEnabled(!g.isSeasonStarted());
 		
 		btnGenID.setEnabled(!g.isSeasonStarted());
 		tfID.setEnabled(!g.isSeasonStarted());
 		
 		refreshContestantCBs();
 				
 		tableModel.fireTableDataChanged();
 	}
 
 	@Override
 	protected void setToolTips() {
 		
 		labelName.setToolTipText(TOOL_NAME);
 		tfFirstName.setToolTipText(TOOL_NAME);
 		tfLastName.setToolTipText(TOOL_NAME);
 		
 		labelID.setToolTipText(PlayerPanel.TOOL_IDTXT);
 		tfID.setToolTipText(PlayerPanel.TOOL_IDTXT);
 		btnGenID.setToolTipText(PlayerPanel.TOOL_IDBTN);
 
 		labelWeekly.setToolTipText(PlayerPanel.TOOL_WEEKLY);
 		cbWeeklyPick.setToolTipText(PlayerPanel.TOOL_WEEKLY);
 	
 		labelUltimate.setToolTipText(PlayerPanel.TOOL_ULT);
 		cbUltPick.setToolTipText(PlayerPanel.TOOL_ULT);
 		
 		btnSave.setToolTipText(TOOL_SAVE);
 		
 		btnAddNew.setToolTipText(TOOL_NEW);
 		btnDelete.setToolTipText(TOOL_DELETE);
 		
 	}
 }
