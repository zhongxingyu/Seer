 package admin.playertab;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 
 import admin.GameDataDependant;
 import admin.MainFrame;
 import admin.Utils;
 import data.Contestant;
 import data.GameData;
 import data.InvalidFieldException;
 import data.Person;
 import data.User;
 import data.InvalidFieldException.Field;
 
 
 /**
  * TODO: Doc
  * @author kevin
  *
  */
 public class PlayerPanel extends JPanel implements ChangeListener,
 		MouseListener, GameDataDependant {
 
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
 	/* FIXME: Break into two labels one with "Points:" other with actual value 
 	 * of pts
 	 */
 	private JLabel labelPts;
 	private JButton btnSave;
 
 	// / Table fields
 	private PlayerTableModel tableModel;
 	private JTable table;
 	private JTableHeader header;
 
 	// Bottom buttons
 	private JButton btnAddNew;
 	private JButton btnDelete;
 	private PlayerFieldsPanel playerFields;
 	
 	// Vars
 	private boolean isNewUser;
 	
 	// has the PlayerFields changed?
 	private boolean fieldsChanged;
 
 	public PlayerPanel() {
 		super();
 
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
 
 		playerFields = new PlayerFieldsPanel(labelName, tfFirstName,
 				tfLastName, labelID, tfID, btnGenID, labelWeekly, cbWeeklyPick,
 				labelUltimate, cbUltPick);
 		// add the mouse listener to all components.
 		for (Component c : playerFields.getComponents()) {
 			c.addMouseListener(this);
 		}
 
 		// right side!
 		labelPts = new JLabel("Points: 0");
 		btnSave = new JButton("Save");
 
 		// ////////////////////////////
 		// Mid
 		// ////////////////////////////
 		List<User> users = GameData.getCurrentGame().getAllUsers();
 		tableModel = new PlayerTableModel(users);
 		table = new JTable(tableModel);
 		header = table.getTableHeader();
 
 		// ////////////////////////////
 		// Bottom
 		// ////////////////////////////
 		btnAddNew = new JButton("Add New");
 		btnDelete = new JButton("Delete");
 
 		// build the two panes
 		// setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		setLayout(new BorderLayout(5, 5));
 		buildTopPanel();
 		buildTablePanel();
 		buildBottomPanel();
 		
 		buildActions();
 		
 		// init the GUI components
 		
 		refreshGameFields();
 		
 		if (users.size() > 0) {
 			setPanelUser(users.get(0), false);
 			table.setRowSelectionInterval(0, 0);
 		} else {
 			setPanelUser(null, true);
 		}
 	}
 
 	/**
 	 * Builds the top panel including all the editable information
 	 */
 	private void buildTopPanel() {
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
 		panel.add(playerFields, BorderLayout.CENTER);
 		panel.add(rightPane, BorderLayout.LINE_END);
 
 		add(panel, BorderLayout.PAGE_START);
 
 		// add the mouse listener to all components.
 		for (Component c : panel.getComponents()) {
 			c.addMouseListener(this);
 		}
 
 		for (Component c : rightPane.getComponents())
 			c.addMouseListener(this);
 	}
 
 	/**
 	 * Builds the panel containing the JTable
 	 */
 	private void buildTablePanel() {
 		JPanel panel = new JPanel();
 
 		// settings:
 		header.setReorderingAllowed(false); // no moving.
 		table.setColumnSelectionAllowed(true);
 		table.setRowSelectionAllowed(true);
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		header.addMouseListener(tableModel.new SortColumnAdapter());
 
 		TableCellRenderer renderer = new TableCellRenderer() {
 
 			JLabel label = new JLabel();
 
 			@Override
 			public JComponent getTableCellRendererComponent(JTable table,
 					Object value, boolean isSelected, boolean hasFocus,
 					int row, int column) {
 
 				if (table.isRowSelected(row)) {
 					label.setBackground(Utils.getThemeTableHighlight());
 					label.setForeground(Utils.getThemeBG());
 				} else {
 					label.setBackground(UIManager.getColor("Table.background"));
 					label.setForeground(UIManager.getColor("Table.foreground"));
 				}
 
 				label.setOpaque(true);
 				label.setText("" + value);
 
 				return label;
 			}
 
 		};
 		table.setDefaultRenderer(Object.class, renderer);
 
 		JScrollPane scroll = new JScrollPane(table);
 
 		panel.setLayout(new BorderLayout(5, 5));
 		panel.add(scroll, BorderLayout.CENTER);
 
 		add(panel, BorderLayout.CENTER);
 
 		// add the mouse listener to all components.
 		for (Component c : scroll.getComponents()) {
 			c.addMouseListener(this);
 		}
 	}
 
 	private void buildBottomPanel() {
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
 
 	public void seasonStarted(){
 		tfID.setEnabled(false);
 		tfFirstName.setEnabled(false);
 		tfLastName.setEnabled(false);
 		btnGenID.setEnabled(false);
 		btnAddNew.setEnabled(false);
 		btnDelete.setEnabled(false);
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
 	 * Returns a new user represented by the data in the User fields.
 	 * @return New User
 	 * @throws InvalidFieldException thrown if any field is invalid.
 	 */
 	private User getUser() throws InvalidFieldException {
 		
 		User u = new User();
 		String uID = tfID.getText();
 		
 		u.setID(uID);
 		
 		u.setFirstName(tfFirstName.getText().trim());
 		u.setLastName(tfLastName.getText().trim());
 		
 		int item = cbUltPick.getSelectedIndex();
 		Contestant c = cbUltPick.getItemAt(item);
 		u.setUltimatePick(c);
 		
 		
 		item = cbWeeklyPick.getSelectedIndex();
 		c = cbWeeklyPick.getItemAt(item);
 		u.setWeeklyPick(c);
 		
 		return u;
 	}
 	
 	/**
 	 * Sets the user on the screen to the specified container. If newUser is
 	 * true, it will specify that when save is hit, then the GUI should add it
 	 * to the table rather than modify a pre-existing data.
 	 * @param u
 	 * @param newUser
 	 */
 	private void setPanelUser(User u, boolean newUser) {
 		isNewUser = newUser;
 		
 		if (fieldsChanged) {
 			System.out.println("Player panel changing, fields modified.");
 			saveUser();
 			fieldsChanged = false;
 		}
 		
 		tfID.setEnabled(newUser);
 		btnGenID.setEnabled(newUser);
 		btnSave.setEnabled(false);
 		
 		if (newUser || u == null) {
 			// set default values
 			tfID.setText("");
 			tfFirstName.setText("First Name");
 			tfLastName.setText("Last Name");
 			
 			cbUltPick.setSelectedIndex(0);
 			cbWeeklyPick.setSelectedIndex(0);
 			
 			labelPts.setText(Integer.toString(0));
 			
 			//we don't want any rows selected
 			ListSelectionModel m = table.getSelectionModel();
 			int row = table.getSelectedRow();
 			if (row >= 0) {
 				m.removeIndexInterval(row, row);
 			}
 			
 			return;
 		}
 		
 		tfID.setText(u.getID());
 		
 		tfFirstName.setText(u.getFirstName());
 		tfLastName.setText(u.getLastName());
 		
 		labelPts.setText(Integer.toString(u.getPoints()));
 		
 		// iterate through combo boxes setting indexes as necessary
 		boolean ultSet = false, weekSet = false;
 		for (int i = 0; i < cbUltPick.getItemCount(); i++) {
 			
 			// get the contestant to compare with, both store same values
 			Contestant c = u.getUltimatePick();
 			
 			Contestant cbCon = cbUltPick.getItemAt(i);
 			if (!ultSet && c.getID().equals(cbCon.getID())) {
 				cbUltPick.setSelectedIndex(i);
 				ultSet = true;
 			}
 			
 			c = u.getWeeklyPick();
 			if (!weekSet && c.getID().equals(cbCon.getID())) {
 				cbWeeklyPick.setSelectedIndex(i);
 				weekSet = true;
 			}
 			
 			if (ultSet && weekSet) 
 				break; // break if both are set
 		}
 	}
 	
 	/**
 	 * Sets the error infromation based on an exception!
 	 * @param e Exception with the information necessary
 	 */
 	private void setExceptionError(InvalidFieldException e) {
 		MainFrame mf = MainFrame.getRunningFrame();
 		
 		switch (e.getField()) {
 		case USER_ID:
 			mf.setStatusErrorMsg("Invalid ID (must be between 2 and 7 chars" +
 					" long, followed by numbers)", tfID);
 			break;	
 		case USER_ID_DUP:
 			mf.setStatusErrorMsg("Invalid ID (in use)", tfID);
 			break;
 		case USER_FIRST:
 			mf.setStatusErrorMsg("Invalid First Name (must be alphabetic" +
 					", 1-20 characters)", tfFirstName);
 			break;
 		case USER_LAST:
 			mf.setStatusErrorMsg("Invalid Last Name (must be alphabetic" +
 					", 1-20 characters)", tfLastName);
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
 	}
 	
 	private void saveUser() {
 		
 		User user = null;
 		try {
 			user = getUser();
 			
 			GameData g = GameData.getCurrentGame();
 			if (isNewUser && g.isUserIDInUse(user.getID())) {
 				throw new InvalidFieldException(Field.USER_ID_DUP, 
 						"Invalid ID (in use)");
 			}
 			
 			tableModel.updateUser(user);
 		} catch (InvalidFieldException e) {
 			setExceptionError(e);
 			return;
 		} // end catch block
 		
 		isNewUser = false;
 		fieldsChanged = false;
 		
 		int row = tableModel.getRowByUser(user);
 		if (row >= 0 && table.getSelectedRow() != row) // select a row
 			table.setRowSelectionInterval(row, row);
 	}
 	
 	private void buildActions() {
 		btnAddNew.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (fieldsChanged) {
 					saveUser();
 				}
 				
 				setPanelUser(null, true);
 			}
 			
 		});
 		
 		btnSave.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				
 				// make sure they want to save initially.
 				if (isNewUser) {
 					int response = JOptionPane.showConfirmDialog(null,
 						"Would you like to save a new selected user? You can " +
 						"not change ID after first save.",
 						"Delete User?",
 						JOptionPane.YES_NO_OPTION);
 					if(response == JOptionPane.NO_OPTION){
 						return;
 					}
 				}
 				
 				if (fieldsChanged) {
 					saveUser();
 				}
 			}
 		});
 		
 		btnDelete.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int response = JOptionPane.showConfirmDialog(null,
 						"Would you like to delete currently selected user?","Delete User",
 								JOptionPane.YES_NO_OPTION);
 				if(response == JOptionPane.YES_OPTION){
 					User u = null;
 					try {
 						u = getUser();
 					} catch (InvalidFieldException ex) {
 						if (ex.getField() == InvalidFieldException.Field.USER_ID) {
 							MainFrame.getRunningFrame().setStatusErrorMsg("Can not delete User (invalid ID)", tfID);
 							return;
 						}
 					}
 
 					GameData g = GameData.getCurrentGame();
 					User t = g.getUser(u.getID());
 					int row = tableModel.getRowByUser(t);
 					boolean selRow = (table.getRowCount() > 1);
 					tableModel.removeUser(t);
 					if (selRow) {
 						row %= table.getRowCount();
 						table.setRowSelectionInterval(row, row);
 					} else {
 						btnAddNew.doClick();
 					}
 				}
 			}
 		});
 		
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
 						(Person)(new User())); // lol so ugly.
 				
 				String id = Utils.generateID(u, userList);
 				
 				tfID.setText(id);
 			}
 		});
 		
 		table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
 
 			int oldRow = -1; // breaks an infinite loop since setPanelUser fires this event
 			
 			public void valueChanged(ListSelectionEvent le) {
 				 int row = table.getSelectedRow();
 				 
 				 if (row < 0 || oldRow == row) return;
 				 
 				 oldRow = row;
 				 
 				 User u = tableModel.getByRow(row);
 			     
 				 if (u != null){
 					 if (fieldsChanged) {
 						 saveUser();
 					 }
 					 
 					 setPanelUser(u, false); 
 				 }
 				 
 				 
 			}
 		});
 		
 		FocusAdapter fa = new FocusAdapter() {
 			JTextField src;
 			
 			public void focusGained(FocusEvent evt) {
 				src = (JTextField)evt.getComponent();
 				
 				SwingUtilities.invokeLater( new Runnable() {
     				@Override
     				public void run() {
     					src.selectAll();		
     				}
     			});
     	    }
 		};
 		
 		List<JTextField> tfArr = Arrays.asList(tfID, tfFirstName, 
 				tfLastName);
 		for (JTextField tf: tfArr) {
 			tf.addFocusListener(fa);
 		}
 	}
 	
 	/**
 	 * Loads the contestant data in the ComboBoxes 
 	 * from the GameData.
 	 */
 	private void refreshContestantCBs() {
 		GameData g = (GameData) GameData.getCurrentGame();
 		
 		if (g == null) {
 			return;
 		}
 		
 		List<Contestant> cons = g.getActiveContestants();
 
 		cbWeeklyPick.removeAllItems();
 		cbUltPick.removeAllItems();
 		
 		Contestant nullC = new Contestant();
 		nullC.setNull();
 		
 		cbWeeklyPick.addItem(nullC);
 		cbUltPick.addItem(nullC);
 
 		for (Contestant c : cons) {
 			cbWeeklyPick.addItem(c);
 			cbUltPick.addItem(c);
 		}
 	}
 	
 	/**
 	 * Changes all fields that have data changed. <br>
 	 * Currently calls:
 	 * - Table update
 	 */
 	@Override
 	public void refreshGameFields() {
 		refreshContestantCBs();
 		tableModel.fireTableDataChanged();
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		Component c = e.getComponent();
 		MainFrame mf = MainFrame.getRunningFrame();
 		if (c == labelName || c == tfFirstName || c == tfLastName) {
 			mf.setStatusMsg("First and Last name must be alphabetic");
 		} else if (c == labelID || c == tfID) {
 			mf.setStatusMsg("ID must be 2-7 chars long and may end with numbers");
 		} else if (c == btnGenID) {
 			mf.setStatusMsg("Click to auto-generate ID from first and last name");
 		} else if (c == labelWeekly || c == cbWeeklyPick) {
 			mf.setStatusMsg("Select Weekly pick");
 		} else if (c == labelUltimate || c == cbUltPick) {
 			mf.setStatusMsg("Select Ultimate Winner");
 		} else if (c == btnAddNew) {
 			mf.setStatusMsg("Add a new User to system");
 		} else if (c == btnDelete) {
 			mf.setStatusMsg("Remove selected User from system");
 		} else if (c == table || c == header) {
 			mf.setStatusMsg("Click Heading to sort by column");
 		}
 		// System.out.println("MouseEntered: " + c.toString());
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// Currently unused stubs.
 		return;
 	}
 
 	@Override
 	public void mousePressed(MouseEvent me) {
 		Component c = me.getComponent();
 		if (c == tfFirstName || c == tfLastName || c == tfID || 
 				c == cbUltPick || c == cbWeeklyPick) {
 			fieldsChanged = true;
 			btnSave.setEnabled(true);
 		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// Currently unused stubs.
 		return;
 	}
 }
