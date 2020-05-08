 package admin.panel.person.contestant;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Arrays;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 
 import admin.FileDrop;
 import admin.MainFrame;
 import admin.panel.person.PersonPanel;
 import data.Contestant;
 import data.GameData;
 import data.GameData.UpdateTag;
 import data.InvalidFieldException;
 import data.history.History;
 
 public class ContestantPanel extends PersonPanel<Contestant> implements MouseListener, Observer {
 
 	private static final long serialVersionUID = 1L;
 
 	// container for top stuff
 	private JButton btnSetStatus;
 	
 	private JButton imgDisplay;
 	
 	private JLabel labelName;
 	// TODO: Refactor to something more obvious?
 	private JLabel labelCastOff;
 	private JComboBox tfCastDate;
 	private JLabel labelTribe;
 
 	private JTextField tfFirstName;
 	private JTextField tfLastName;
 	private JComboBox<String> cbTribe;
 
 	private JTextField tfContID;
 	private JLabel labelID;
 	
 	private JButton btnPickWin;
 	
 	// static constants:
 	private static final String SET_STATUS_TEXT = "Set Status";
 	// tool tip texts:
 	protected static final String TOOL_NAME = "First and Last name must be alphabetic",
 			TOOL_ID = "ID must be two characters long and alpha-numeric",
 			TOOL_TRIBE = "Select a tribe",
 			TOOL_CASTOFF_BTN = "Click to cast off contestant",
 			TOOL_CASTOFF_FIELD = "Week player was cast off",
 			TOOL_SAVE = "Click to save contestant data",
 			TOOL_IMAGE = "Click to select image",
 			TOOL_ADDNEW = "Click to add new contestant",
 			TOOL_DELETE = "Click to remove currently selected Contestant",
 			TOOL_WINNER = "Click to choose winner";
 	
 	public ContestantPanel() {
 		super(new Contestant());
 
 		// ////////////////////////////
 		// Top Panel:
 		// ////////////////////////////
 		// TODO: Better Test picture
 		imgDisplay = new JButton();
 
 		// Edit fields:
 		labelName = new JLabel("Name:");
 		tfFirstName = new JTextField(20);
 		tfLastName = new JTextField(20);
 
 		labelCastOff = new JLabel("Cast off:");
 		tfCastDate = new JComboBox<String>();
 		btnSetStatus = new JButton("Set Status");
 
 		labelTribe = new JLabel("Tribe:");
 		cbTribe = new JComboBox<String>(GameData.getCurrentGame()
 				.getTribeNames());
 
 		labelID = new JLabel("ID:");
 		tfContID = new JTextField(2);
 
 		// holds all the fields
 		personFields = new ContestantFieldsPanel(imgDisplay, labelName, 
 				tfFirstName, tfLastName, labelID, tfContID, labelCastOff, 
 				tfCastDate, btnSetStatus, labelTribe, cbTribe);
 		// add the mouse listener to all components.
 		for (Component c : ((JPanel)personFields).getComponents()) {
 			if (c instanceof JPanel) {
 				for (Component d: ((JPanel)c).getComponents())
 					d.addMouseListener(this);
 			}
 			c.addMouseListener(this);
 		}
 		
 		btnPickWin = new JButton("<html><center>Select<br>Winner</center></html>");
 
 		// check to stop casting off before start
 		if (!GameData.getCurrentGame().isSeasonStarted()) {
 			btnSetStatus.setEnabled(false);
 		}
 		
 		tfCastDate.addItem("Active");
 		//////////////////////////////
 		// Mid (table!)
 		//////////////////////////////
 		// handled by super call
 
 		// ////////////////////////////
 		// Bottom
 		//////////////////////////////
 		
 		// build the two panes
 		// setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		
 		assembleAll();
 	}
 	
 	@Override
 	/**
 	 * Builds the top panel including all the editable information
 	 */
 	protected void buildTopPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout(10, 10));
 
 		// this does not need to be referenced else where, only for layout
 		JPanel rightPane = new JPanel();
 		BoxLayout b = new BoxLayout(rightPane, BoxLayout.Y_AXIS);
 
 		btnSave.setPreferredSize(btnPickWin.getPreferredSize());
 		
 		rightPane.setLayout(b);
 		rightPane.add(Box.createVerticalGlue());
 		rightPane.add(btnPickWin);
 		rightPane.add(Box.createVerticalStrut(10));
 		rightPane.add(btnSave);
 		rightPane.add(Box.createVerticalGlue());
 
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
 	
 	/**
 	 * Sets the tool tips for all the components.
 	 */
 	@Override
 	protected void setToolTips() {
 		imgDisplay.setToolTipText(TOOL_IMAGE);
 		
 		labelName.setToolTipText(ContestantPanel.TOOL_NAME);
 		tfFirstName.setToolTipText(ContestantPanel.TOOL_NAME);
 		tfLastName.setToolTipText(ContestantPanel.TOOL_NAME);
 		
 		labelID.setToolTipText(ContestantPanel.TOOL_ID);
 		tfContID.setToolTipText(ContestantPanel.TOOL_ID);
 		
 		labelTribe.setToolTipText(ContestantPanel.TOOL_TRIBE);
 		cbTribe.setToolTipText(ContestantPanel.TOOL_TRIBE);
 		
 		tfCastDate.setToolTipText(TOOL_CASTOFF_FIELD);
 		btnSetStatus.setToolTipText(TOOL_CASTOFF_BTN);
 		
 		btnPickWin.setToolTipText(TOOL_WINNER);
 		btnSave.setToolTipText(TOOL_SAVE);
 		
 		btnAddNew.setToolTipText(TOOL_ADDNEW);
 		btnDelete.setToolTipText(TOOL_DELETE);
 	}
 
 	/**
 	 * Wrapper that allows the super class to do most of the work. Small 
 	 * adjustments for contestants vs. players.
 	 * 
 	 * @param c
 	 * @param newContestant
 	 */
 	@Override
 	protected void setPanelPerson(Contestant c, boolean newContestant) {
 		super.setPanelPerson(c, newContestant);
 
 		btnSetStatus.setEnabled(GameData.getCurrentGame().isSeasonStarted());
 			
 		if (newContestant || c == null) {
 			// we don't want any rows selected
 			ListSelectionModel m = table.getSelectionModel();
 			m.clearSelection();
 
 			return;
 		}
 		
 		tableModel.setRowSelect(c);
 	}
 
 	/**
 	 * Sets the error infromation based on an exception!
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
 
 	/**
 	 * Used to store the listener event so we can remove it later.
 	 */
 	private ActionListener imgButtonListener = new ActionListener() {
 
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
 	 * 
 	 */
 	@Override
 	protected void buildActions() {
 		super.buildActions();
 		
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
 						savePerson();
 					} catch (InvalidFieldException ex) {
 						setExceptionError(ex);
 						return;
 					}
 				}
 
 				setPanelPerson(null, true);
 			}
 		});
 		
 		
 
 		btnSetStatus.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String s = (String)tfCastDate.getSelectedItem();
 	
 				Contestant c = null;
 				try {
 					c = getPerson();
 				} catch (InvalidFieldException ie) {
 					// FIXME: Intelligently respond on the exception.
 					// In theory, it shouldn't happen, but we shouldn't cast
 					// someone who isn't fully in the game.. :/
 					return;
 				}
 				
 				GameData g = GameData.getCurrentGame();
 				History h = g.getHistory();
 
 				if (!s.equals("Active")) {
 					int i = Integer.valueOf(s).intValue();
 					if(i == g.getCurrentWeek()){
 					// check if someone is already cast off
 					if (g.doesElimExist() == true) {
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
 					
 					if (g.isFinalWeek()) {
 						JOptionPane.showMessageDialog(null,
 								"In the final week nobody is cast off, " +
 								"instead you must select a winner.");
 						return;
 					}
 					
 					if (g.isSeasonEnded()) {
 						JOptionPane.showMessageDialog(null,
 								"The season has ended. Thank you for playing!");
 						return;
 					}
 					g.castOff(i,c);
 					}
 					
 				 else{
 					 if(g.getCastOff(i) != null){
 					 if(!h.canCastoff(i,c)){
 						int resp = JOptionPane.showConfirmDialog(null, "Doing this will invalidate your current point standings." +
 								                     " Proceed?", "Redoing cast off", JOptionPane.YES_NO_OPTION);
 					 if(resp == JOptionPane.YES_OPTION){	 
 					 g.castOff(i,c);
 					 g.undoCastOff(i,g.getCastOff(i));
 					 }
 					 else JOptionPane.showMessageDialog(null,"Contestant is ineligible for re-cast off.");
 					 }
 					 else g.castOff(i,c);
 					 }
 				}
 				}
 					else{
 						if(c.getCastDate() == g.getCurrentWeek()){
 				        g.undoCastOff(g.getCurrentWeek(),c);
 				}   else return;
 			}
 
 				update(GameData.getCurrentGame(), EnumSet.of(UpdateTag.CONTESTANT_CAST_OFF));
 			}
 		});
 		
 		btnPickWin.addActionListener( new ActionListener() {
 			
 			public void actionPerformed(ActionEvent ae) {
 				GameData g = GameData.getCurrentGame();
 				
 				if (g.isSeasonEnded()) {
 					JOptionPane.showMessageDialog(null,
 							"The season has ended.");
 					return;
 				}
 				
 				if (!g.isFinalWeek()){ // only applicable if the last week. 
 					return;
 				}
 				
 				Contestant win = null;
 				try {
 					win = getPerson();
 				} catch (InvalidFieldException e) { return; } // shouldn't happen
 				
 				if (win == null) return;
 				
 				// cast off the other two contestants (always three left)
 				for (Contestant c: g.getActiveContestants(true)) {
 					if (!c.equals(win))
 						g.castOff(g.getCurrentWeek(),c);
 				}
 				
 				// FIXME: What do here?
 			}
 		});
 		
 		imgDisplay.addActionListener(imgButtonListener);
 		
 		new FileDrop( this, new FileDrop.Listener(){   
 			public void filesDropped( java.io.File[] files ){   
 				updateContPicture(files[0].getAbsolutePath());
 			}
 		});
 
 		List<JTextField> tfArr = Arrays.asList(tfContID, tfFirstName,
 				tfLastName);
 		for (JTextField tf : tfArr) {
 			tf.addFocusListener(editAdapt);
 		}
 	}
 
 	/**
 	 * Convienience wrapper. 
 	 * @param absolutePath
 	 */
 	protected void updateContPicture(String absolutePath) {
 		((ContestantFieldsPanel)personFields).updateContPicture(absolutePath);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		Component c = e.getComponent();
 		
 		if (!c.isEnabled()) return;
 		
 		if (c == tfContID || c == tfFirstName || c == tfLastName
 				|| c == cbTribe || c == table || c == btnSetStatus) {
 			setFieldsChanged(true);
 		}
 	}
 
 	/**
 	 * Refreshes all values associated with GameData reference. <br>
 	 * Currently: - Tribe combobox - Table - Sets buttons enabled/disabled as
 	 * appropriate.
 	 * 
 	 * @see GameDataDependant.refreshGameFields
 	 */
 	// TODO: Make this use the EnumSet notion of what's passed in. 
 	@Override
 	public void update(Observable obj, Object arg) {
 		GameData g = (GameData) obj;
 
 		@SuppressWarnings("unchecked")
 		EnumSet<UpdateTag> update = (EnumSet<GameData.UpdateTag>)arg;
 		
 		if (update.size() == 0 || 
 				update.contains(UpdateTag.SET_TRIBE_NAMES) ||
 				update.contains(UpdateTag.CONTESTANT_CAST_OFF)) {
 			// tribe combobox
 			String[] newTribes = g.getTribeNames();
 			cbTribe.removeAllItems();
 			for (String s : newTribes) {
 				cbTribe.addItem(s);
 			}
 
 			// updates the data in the table
 			tableModel.fireTableDataChanged();
 		}
 		
 		/*
 		 * If the season has started, or the week has advanced
 		 */
 		if (update.size() == 0 ||
 				update.contains(UpdateTag.START_SEASON) ||  
 				update.contains(UpdateTag.ADVANCE_WEEK) || 
 				update.contains(UpdateTag.END_GAME)) {
 			// depends on season started:
 			boolean sStart = g.isSeasonStarted();
 			
 			// change text to "select winner" once its the final week
 			
 			btnAddNew.setEnabled(!sStart);
 			tfCastDate.setEnabled(sStart);
 			if (g.isSeasonEnded()) { // game end
 				btnSetStatus.setEnabled(false);
 				btnSave.setEnabled(false);
 			} else if (g.isFinalWeek()) { // final week
 				btnSetStatus.setEnabled(false);
 				btnPickWin.setEnabled(true);
 			} else {
 				btnSetStatus.setEnabled(sStart);
 				btnPickWin.setEnabled(false);
 			}
 			
 			btnDelete.setEnabled(!sStart);
 			tfLastName.setEnabled(!sStart);
 			tfFirstName.setEnabled(!sStart);
 			tfContID.setEnabled(!sStart);
 			
 			tfCastDate.removeAllItems();
 			tfCastDate.addItem("Active");
 			int i = 1;
 			while(i <= g.getCurrentWeek()){
 			tfCastDate.addItem("" + i);
 			i++;
 			}
 			
 			// defaults to the current date for easier standard "cast offs". 
 			tfCastDate.setSelectedIndex(tfCastDate.getItemCount() - 1);
 			
 			List<ActionListener> acts = Arrays.asList(imgDisplay
 					.getActionListeners());
 			boolean actPresent = acts.contains(imgButtonListener);
 			if (actPresent && sStart) {
 				imgDisplay.removeActionListener(imgButtonListener);
 			} else if (!actPresent && !sStart) {
 				imgDisplay.addActionListener(imgButtonListener);
 			}
 		}
 		
 		/*
 		 * Final week, so set the cast off disabled, and pick a winner
 		 */
 		if (update.contains(UpdateTag.FINAL_WEEK)) {
 			btnSetStatus.setEnabled(false);
 			btnPickWin.setEnabled(true);
 			tfCastDate.setEditable(false);
 		}
 	}
 }
