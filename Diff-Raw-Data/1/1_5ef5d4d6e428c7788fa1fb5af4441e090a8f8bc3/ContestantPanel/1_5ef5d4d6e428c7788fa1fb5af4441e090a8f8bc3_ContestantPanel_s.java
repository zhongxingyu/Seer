 package admin.panel.person.contestant;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableRowSorter;
 
 import admin.FileDrop;
 import admin.MainFrame;
 import admin.Utils;
 import admin.panel.person.PersonTableModel;
 import data.Contestant;
 import data.GameData;
 import data.InvalidFieldException;
 
 public class ContestantPanel extends JPanel implements MouseListener, Observer {
 
 	private static final long serialVersionUID = 1L;
 	private JButton imgDisplay;
 	private String imgPath;
 
 	private ContestantFieldsPanel paneEditFields;
 	// container for top stuff
 	private JButton btnCastOff;
 	private JButton btnSaveCon;
 	
 	private JLabel labelName;
 	// TODO: Refactor to something more obvious?
 	private JLabel labelCastOff;
 	private JLabel labelCastStatus;
 	private JLabel labelTribe;
 
 	private JTextField tfFirstName;
 	private JTextField tfLastName;
 	private JComboBox<String> cbTribe;
 
 	private JTextField tfContID;
 	private JLabel labelID;
 
 	private JTable table;
 	private ContestantTableModel tableModel;
 	private JTableHeader header;
 	
 	private JButton btnAddNew;
 	private JButton btnDelCon;
 	
 	// vars:
 	private boolean isNewContestant = false;
 	private boolean fieldsChanged = false;
 	
 	// static constants:
 	private static final String CAST_OFF_TEXT = "Cast Off";
 	private static final String UNDO_CAST_TEXT = "Undo Cast Off";
 	
 	// tool tip texts:
 	protected static final String TOOL_NAME = "First and Last name must be alphabetic";
 	protected static final String TOOL_ID = "ID must be two characters long and " +
 			"alpha-numeric";
 	protected static final String TOOL_TRIBE = "Select a tribe";
 	protected static final String TOOL_CASTOFF = "Click to cast off contestant.";
 	protected static final String TOOL_SAVE = "Click to save contestant data";
 	protected static final String TOOL_IMAGE = "Click to select image";
 	protected static final String TOOL_ADDNEW = "Click to add new contestant";
 	protected static final String TOOL_DELETE = "Click to remove currently selected " +
 			"Contestant";
 	
 	
 	private static final String DEFAULT_PICTURE = "res/test/defaultpic.png";
 	private static final int IMAGE_MAX_DIM = 75;
 	
 	/**
 	 * THIS VARIABLE IS A REFERENCE MAINTAINED INTERNALLY. DO NOT ADJUST UNLESS
 	 * YOU KNOW WHAT YOU ARE DOING.
 	 */
 	private Contestant loadedContestant;
 
 	public ContestantPanel() {
 		super();
 
 		// ////////////////////////////
 		// Top Panel:
 		// ////////////////////////////
 		// TODO: Better Test picture
 		imgDisplay = new JButton();
 		updateContPicture(DEFAULT_PICTURE); // apparently images have to be .png
 											// and alphanumeric
 
 		// Edit fields:
 		labelName = new JLabel("Name:");
 		tfFirstName = new JTextField(20);
 		tfLastName = new JTextField(20);
 
 		labelCastOff = new JLabel("Cast off:");
 		// TODO: FIx the init of this.. :>
 		labelCastStatus = new JLabel("-");
 
 		labelTribe = new JLabel("Tribe:");
 		cbTribe = new JComboBox<String>(GameData.getCurrentGame()
 				.getTribeNames());
 
 		labelID = new JLabel("ID:");
 		tfContID = new JTextField(2);
 
 		// holds all the fields
 		paneEditFields = new ContestantFieldsPanel(labelName, tfFirstName,
 				tfLastName, labelID, tfContID, labelCastOff, labelCastStatus,
 				labelTribe, cbTribe);
 		// add the mouse listener to all components.
 		for (Component c : paneEditFields.getComponents()) {
 			c.addMouseListener(this);
 		}
 
 		// buttons:
 		btnCastOff = new JButton("Cast Off");
 		/* check to stop casting off before start */
 		if (!GameData.getCurrentGame().isSeasonStarted()) {
 			btnCastOff.setEnabled(false);
 		}
 		btnSaveCon = new JButton("Save");
 		
 		//////////////////////////////
 		// Mid (table!)
 		//////////////////////////////
 		List<Contestant> cons = GameData.getCurrentGame().getAllContestants();
 		
 		table = new JTable();
 		tableModel = new ContestantTableModel(table, cons);
 		TableRowSorter<PersonTableModel<Contestant>> sort = 
 				new TableRowSorter<PersonTableModel<Contestant>>(tableModel);
 		tableModel.setComparators(sort);
 		
 		table.setModel(tableModel);
 		table.setRowSorter(sort);
 		sort.toggleSortOrder(ContestantTableModel.INDEX_ID);
 		
 		header = table.getTableHeader();
 
 		// ////////////////////////////
 		// Bottom
 		//////////////////////////////
 		btnAddNew = new JButton("New");
 		btnDelCon = new JButton("Delete");
 		
 		// build the two panes
 		// setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		setLayout(new BorderLayout(5, 5));
 		buildTopPanel();
 		buildTablePanel();
 		buildBottomPanel();
 
 		buildActions();
 
 		update(GameData.getCurrentGame(), null);
 
 		if (cons.size() > 0) {
 			tableModel.setRowSelect(0, false);
 		} else {
 			setPanelContestant(null, true);
 		}
 		setFieldsChanged(false);
 
 		GameData.getCurrentGame().addObserver(this);
 	}
 
 	/**
 	 * The action listener used by the Image Button.
 	 */
 	private ActionListener imgActionListener = new ActionListener() {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			JFileChooser fc = new JFileChooser();
 			int ret = fc.showOpenDialog(null);
 			if (ret == JFileChooser.APPROVE_OPTION) {
 				// File f = fc.getSelectedFile();
 				updateContPicture(fc.getSelectedFile().getAbsolutePath());
 			}
 		}
 
 	};
 
 	/**
 	 * Builds the top panel including all the editable information
 	 */
 	private void buildTopPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout(10, 10));
 
 		// this does not need to be referenced else where, only for layout
 		JPanel paneButtons = new JPanel();
 		GridLayout bl = new GridLayout(2, 1);
 		paneButtons.setLayout(bl);
 
 		paneButtons.add(btnCastOff);
 		paneButtons.add(btnSaveCon);
 		
 		btnCastOff.setToolTipText(TOOL_CASTOFF);
 		btnSaveCon.setToolTipText(TOOL_SAVE);
 		imgDisplay.setToolTipText(TOOL_IMAGE);
 		
 		// add all components on top:
 		panel.add(imgDisplay, BorderLayout.LINE_START);
 		panel.add(paneEditFields, BorderLayout.CENTER);
 		panel.add(paneButtons, BorderLayout.LINE_END);
 
 		add(panel, BorderLayout.PAGE_START);
 
 		// add the mouse listener to all components.
 		for (Component c : panel.getComponents()) {
 			c.addMouseListener(this);
 		}
 
 		for (Component c : paneButtons.getComponents())
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
 
 		//header.addMouseListener(tableModel.new SortColumnAdapter());
 
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
 	
 	/**
 	 * Helper method to build the bottom panel of the container
 	 */
 	private void buildBottomPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		
 		panel.add(btnAddNew);
 		panel.add(btnDelCon);
 		
 		btnAddNew.setToolTipText(TOOL_ADDNEW);
 		btnDelCon.setToolTipText(TOOL_DELETE);
 		
 		add(panel, BorderLayout.PAGE_END);
 		// add the mouse listener to all components.
 		for (Component c : panel.getComponents()) {
 			c.addMouseListener(this);
 		}
 	}
 
 	/**
 	 * Updates the image displayed to have the path associated, helper method <br>
 	 * <b>Note:</b> Pictures must be PNG format.
 	 * 
 	 * @param path
 	 *            Path to new image.
 	 */
 	// apparently images have to be .png and alphanumeric
 	private void updateContPicture(String path) {
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
 	 * gets the current information with the current contestant, will update
 	 * from the fields associated.
 	 * 
 	 * @return Current contestant loaded
 	 * @throws InvalidFieldException
 	 *             Thrown on any bad fields passed
 	 */
 	private Contestant getContestant() throws InvalidFieldException {
 		Contestant c = loadedContestant;
 
 		c.setID(tfContID.getText());
 		c.setFirstName(tfFirstName.getText().trim());
 		c.setLastName(tfLastName.getText().trim());
 		c.setTribe((String) cbTribe.getSelectedItem());
 		c.setPicture(imgPath);
 
 		return c;
 	}
 
 	private void setPanelIsActive(boolean castOff, int week) {
 		if (!castOff) {
 			labelCastStatus.setText("Active");
 			btnCastOff.setText(CAST_OFF_TEXT);
 		} else {
 			labelCastStatus.setText("Week " + week);
 			btnCastOff.setText(UNDO_CAST_TEXT);
 		}
 		btnCastOff.setEnabled(GameData.getCurrentGame().isSeasonStarted());
 	}
 
 	/**
 	 * Sets the panel to the passed Contestant value. If newContestant is true,
 	 * then it loads a NEW contestant object, otherwise it uses the reference
 	 * passed in.
 	 * 
 	 * @param c
 	 * @param newContestant
 	 */
 	private void setPanelContestant(Contestant c, boolean newContestant) {
 		if (getFieldsChanged()) {
 			System.out.println("Player panel changing, fields modified.");
 			try {
 				saveContestant();
 			} catch (InvalidFieldException e) {
 				setExceptionError(e);
 				return;
 			}
 		}
 
 		isNewContestant = newContestant;
 
 		if (isNewContestant) {
 			loadedContestant = new Contestant();
 			btnSaveCon.setText("Add");
 		} else {
 			if (loadedContestant == c) {
 				return; // don't need to set it then..
 			}
 			
 			loadedContestant = c;
 			btnSaveCon.setText("Save");
 		}
 
 		GameData g = GameData.getCurrentGame();
 		tfContID.setEnabled(!g.isSeasonStarted());
 
 		btnCastOff.setEnabled(!isNewContestant);
 		
 		// delete button activation
 		btnDelCon.setEnabled(table.getRowCount() > 0);
 			
 		
 		if (newContestant || c == null) {
 			// set default values
 			tfContID.setText("");
 			tfFirstName.setText("First Name");
 			tfLastName.setText("Last Name");
 
 			cbTribe.setSelectedIndex(0);
 
 			setPanelIsActive(false, -1);
 
 			updateContPicture(DEFAULT_PICTURE);
 
 			// we don't want any rows selected
 			ListSelectionModel m = table.getSelectionModel();
 			m.clearSelection();
 
 			return;
 		}
 
 		tfFirstName.setText(c.getFirstName());
 		tfLastName.setText(c.getLastName());
 
 		setPanelIsActive(c.isCastOff(), c.getCastDate());
 
 		cbTribe.setSelectedItem(c.getTribe());
 
 		tfContID.setText(c.getID());
 
 		updateContPicture(c.getPicture());
 		
 		tableModel.setRowSelect(c);
 	}
 
 	private void saveContestant() throws InvalidFieldException {
 		Contestant con = null;
 		
 		// when a contestant is added, delete becomes active
 		btnDelCon.setEnabled(true);
 		try {
 			con = getContestant();
 
 			tableModel.updatePerson(con);
 		} catch (InvalidFieldException e) {
 			setExceptionError(e);
 			throw e;
 		} // end catch block
 
 		// set that its now NOT a new contestant, and no fields have changed.
 		isNewContestant = false;
 		setFieldsChanged(false);
 	}
 
 	/**
 	 * Should ALWAYS used when modifying fieldsChanged.
 	 * 
 	 * @param value
 	 *            new value for fieldsChanged field.
 	 */
 	private void setFieldsChanged(boolean value) {
 		fieldsChanged = value;
 		btnSaveCon.setEnabled(value);
 	}
 
 	/**
 	 * Returns whether fields have changed or not.
 	 * 
 	 * @return True if changed, false otherwise.
 	 */
 	private boolean getFieldsChanged() {
 		return fieldsChanged;
 	}
 
 	/**
 	 * Sets the error infromation based on an exception!
 	 * 
 	 * @param e
 	 *            Exception with the information necessary
 	 */
 	private void setExceptionError(InvalidFieldException e) {
 		if (e.isHandled())
 			return;
 
 		MainFrame mf = MainFrame.getRunningFrame();
 
 		switch (e.getField()) {
 		case CONT_ID:
 			mf.setStatusErrorMsg("Invalid ID (must be 2 alphanumeric "
 					+ "characters long)", tfContID);
 			break;
 		case CONT_ID_DUP:
 			mf.setStatusErrorMsg("Invalid ID (in use)", tfContID);
 			break;
 		case CONT_FIRST:
 			mf.setStatusErrorMsg("Invalid First Name (must be alphabetic"
 					+ ", 1-20 characters)", tfFirstName);
 			break;
 		case CONT_LAST:
 			mf.setStatusErrorMsg("Invalid Last Name (must be alphabetic"
 					+ ", 1-20 characters)", tfLastName);
 			break;
 		case CONT_TRIBE: // how you did this is beyond me..
 			mf.setStatusErrorMsg("Invalid Tribe selection", cbTribe);
 			break;
 		default:
 			mf.setStatusErrorMsg("Unknown problem with fields");
 		}
 
 		e.handle();
 	}
 
 	private void buildActions() {
 		btnAddNew.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				GameData g = GameData.getCurrentGame();
 				
 				// check if too many contestants
 				if (g.getAllContestants().size() == g.getInitialContestants()) {
 					JOptionPane.showMessageDialog(
 									null,
 									"There are already the maximum "
 											+ "number of contestants in the " +
 											"game.  To add another you must " +
 											"delete an existing contestant.");
 					return;
 				}
 				
 				if (getFieldsChanged()) {
 					try {
 						saveContestant();
 					} catch (InvalidFieldException ex) {
 						setExceptionError(ex);
 						return;
 					}
 				}
 
 				isNewContestant = true;
 				setPanelContestant(null, true);
 			}
 		});
 		
 		btnSaveCon.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {		
 				if (!getFieldsChanged()) 
 					return;
 				
 				try { 
 					saveContestant(); 
 					
 					Contestant c = getContestant(); // this wont cause exception
 					
 					tableModel.setRowSelect(c);
 				} catch (InvalidFieldException ex) {
 					setExceptionError(ex);
 					return;
 				}				
 			}
 
 		});
 
 		btnCastOff.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String s = ((JButton) e.getSource()).getText();
 
 				Contestant c = null;
 				try {
 					c = getContestant();
 				} catch (InvalidFieldException ie) {
 					// FIXME: Intelligently respond on the exception.
 					// In theory, it shouldn't happen, but we shouldn't cast
 					// someone who isn't fully in the game.. :/
 					return;
 				}
 
 				if (s.equals("Cast Off")) {
 					// check if someone is already cast off
 					if (GameData.getCurrentGame().doesElimExist() == true) {
 						JOptionPane.showMessageDialog(
 										null,
 										"You can't cast off more than one " +
 										"person per week. If you accidently" +
 										" casted the wrong person, you can " +
 										"undo the incorrect cast first, and " +
 										"then cast off the correct one.");
 						return;
 					}
 					
 					// can't cast off someone already off.
 					if (c.isCastOff()) {
 						JOptionPane.showMessageDialog(null,
 								"This person is already out of the game.");
 						return;
 					}
 
 					c.toCastOff();
 					labelCastStatus.setText("Week " + c.getCastDate());
 				} else {
 					c.undoCast();
 					labelCastStatus.setText("Active");
 					btnCastOff.setText("Cast Off");
 				}
 
 				update(GameData.getCurrentGame(), null);
 			}
 		});
 		
 		btnDelCon.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// ask the admin for input on whether to delete or not
 				int response = JOptionPane.showConfirmDialog(null,
 						"Would you like to delete currently selected "
 								+ "Contestant?", "Delete Contestant?",
 						JOptionPane.YES_NO_OPTION);
 
 				if (response == JOptionPane.YES_OPTION) {
 					// user said they want to delete contestant
 					
 					Contestant c = null;
 					try {
 						c = getContestant();
 					} catch (InvalidFieldException ex) {
 						if (ex.getField() == InvalidFieldException.Field.CONT_ID) {
 							MainFrame.getRunningFrame().setStatusErrorMsg(
 									"Can not delete Contestant"
 											+ " (invalid ID)", tfContID);
 							return;
 						}
 						System.out.println("Delete contestant, exception");
 					}
 					
 					if (c == null) {
 						System.out.println("We goofed really badly.");
 						throw new NullPointerException("Could not get " +
 								"contestant from game data.");
 					}
 					
 					int row = tableModel.getRowByPerson(c);
 					boolean selRow = (table.getRowCount() > 1);
 
 					// remove the contestant from the game
 					tableModel.removePerson(c);
 					
 					if (selRow && (c != null)) {
 						row %= table.getRowCount();
 						tableModel.setRowSelect(row, false);
 					} else {
 						btnAddNew.doClick();
 					}
 				}
 			}
 		});
 		
 		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			
 			public void valueChanged(ListSelectionEvent le) {
 				 int row = table.getSelectedRow();
 				 if (row < 0) return;
 				// oldRow = row;
 				 
 				 row = table.getRowSorter().convertRowIndexToModel(row);
 				 Contestant c = tableModel.getByRow(row);
 			     
 				 if (c != null){
 					 if (getFieldsChanged())
 						 btnSaveCon.doClick();
 					 
 					 setPanelContestant(c, false); 
 				 }
				 table.setRowSelectionInterval(row, row);
 			}
 		});
 		
 		new FileDrop( this, new FileDrop.Listener(){   
 			public void filesDropped( java.io.File[] files ){   
 				updateContPicture(files[0].getAbsolutePath());
 			}
 		});
 
 		FocusAdapter fa = new FocusAdapter() {
 			JTextField src;
 
 			public void focusGained(FocusEvent evt) {
 				src = (JTextField) evt.getComponent();
 
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						src.selectAll();
 					}
 				});
 			}
 		};
 
 		List<JTextField> tfArr = Arrays.asList(tfContID, tfFirstName,
 				tfLastName);
 		for (JTextField tf : tfArr) {
 			tf.addFocusListener(fa);
 		}
 	}
 
 	/**
 	 * Helper method that will get the selected row, call the runnable method
 	 * then reset the table to where it was by the contestant.
 	 * 
 	 * @param run
 	 *            Method to run
 	 */
 	private void callResetSelectedRow(Runnable run) {
 		int row = table.getSelectedRow();
 		Contestant c = null;
 		
 		if (row > -1) {
 			row = table.getRowSorter().convertRowIndexToModel(row);
 			c = tableModel.getByRow(row);
 		}
 
 		run.run();
 
 		if (c != null)
 			tableModel.setRowSelect(c);	
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		return;
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		JComponent c = (JComponent)e.getComponent();
 		MainFrame mf = MainFrame.getRunningFrame();
 		
 		String txt = c.getToolTipText();
 		if (txt != null)
 			mf.setStatusMsg(txt);
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		mouseEntered(e);
 	}
 
 	// unused
 	@Override
 	public void mousePressed(MouseEvent e) {
 		Component c = e.getComponent();
 		if (c == tfContID || c == tfFirstName || c == tfLastName
 				|| c == cbTribe || c == table || c == btnCastOff) {
 			setFieldsChanged(true);
 		}
 	}
 
 	// unused
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		return;
 	}
 
 	/**
 	 * Refreshes all values associated with GameData reference. <br>
 	 * Currently: - Tribe combobox - Table - Sets buttons enabled/disabled as
 	 * appropriate.
 	 * 
 	 * @see GameDataDependant.refreshGameFields
 	 */
 	@Override
 	public void update(Observable obj, Object arg) {
 		GameData g = (GameData) obj;
 
 		// tribe combobox
 		String[] newTribes = g.getTribeNames();
 		cbTribe.removeAllItems();
 		for (String s : newTribes) {
 			cbTribe.addItem(s);
 		}
 
 		// updates the data in the table
 		tableModel.fireTableDataChanged();
 
 		// depends on season started:
 		boolean sStart = g.isSeasonStarted();
 		
 		btnAddNew.setEnabled(!sStart);
 		btnCastOff.setEnabled(sStart);
 		btnDelCon.setEnabled(!sStart);
 		tfLastName.setEnabled(!sStart);
 		tfFirstName.setEnabled(!sStart);
 		tfContID.setEnabled(!sStart);
 
 		List<ActionListener> acts = Arrays.asList(imgDisplay
 				.getActionListeners());
 		boolean actPresent = acts.contains(imgActionListener);
 		if (actPresent && sStart) {
 			imgDisplay.removeActionListener(imgActionListener);
 		} else if (!actPresent && !sStart) {
 			imgDisplay.addActionListener(imgActionListener);
 		}
 	}
 
 }
