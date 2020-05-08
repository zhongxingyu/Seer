 package final_project.view;
 
 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import java.io.*;
 
 import javax.swing.event.*;
 import javax.swing.table.*;
 
 import final_project.control.Constants;
 import final_project.control.TournamentController;
 
 import net.java.balloontip.BalloonTip;
 import net.java.balloontip.BalloonTip.*;
 
 
 public class CheckInPanel extends JPanel implements ActionListener, Constants {
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = 1L;
 	private JTable table;
 	private TournamentController tournament;
 	private SignInTableModel model;
 	private TableRowSorter<SignInTableModel> sorter;
 	private JScrollPane scrollPane;
 	private JSearchTextField searchField;
 	private JButton signInAll, unsignInAll, importXml, registerPersonButton, startPoolRound;
 	private Collection<BalloonTip> balloons;
 	private BalloonTip signInPlayerTip, registerNewPlayerTip, signInAllTip, unsignInAllTip, stripSetupTip, poolSizeTip;
 	private CheckInPlayerPanel signInPlayerPane;
 	private RegisterNewPlayerPanel registerNewPlayerPane;
 	private ConfirmationPanel signInAllPane, unsignInAllPane;
 	private StripSetupPanel stripSetupPane;
 	private PoolSizeInfoPanel poolSizeInfoPane;
 	private File xmlFile;
 	private JLabel fileLabel;
 
 	/**
 	 * Create the panel.
 	 */
 	public CheckInPanel(TournamentController t) {
 		super();
 		tournament = t;
 		initializeGridBagLayout();
 		initializeTable();
 		initializeComponents();
 		initializeSearch();
 		initializeBalloons();
 	}
 
 	private void initializeComponents() {
 		setOpaque(false);
 
 		signInAll = new JButton("Sign In All");
 		signInAll.addActionListener(this);
 		GridBagConstraints gbc_signInAll = new GridBagConstraints();
 		gbc_signInAll.anchor = GridBagConstraints.EAST;
 		gbc_signInAll.insets = new Insets(0, 0, 5, 5);
 		gbc_signInAll.gridx = 1;
 		gbc_signInAll.gridy = 1;
 		add(signInAll, gbc_signInAll);
 
 		unsignInAll = new JButton("Unsign In All");
 		unsignInAll.addActionListener(this);
 		GridBagConstraints gbc_unsignInAll = new GridBagConstraints();
 		gbc_unsignInAll.anchor = GridBagConstraints.WEST;
 		gbc_unsignInAll.insets = new Insets(0, 0, 5, 5);
 		gbc_unsignInAll.gridx = 2;
 		gbc_unsignInAll.gridy = 1;
 		add(unsignInAll, gbc_unsignInAll);
 
 		registerPersonButton = new JButton("Register Person");
 		GridBagConstraints gbc_registerPersonButton = new GridBagConstraints();
 		gbc_registerPersonButton.insets = new Insets(0, 0, 5, 5);
 		gbc_registerPersonButton.gridx = 5;
 		gbc_registerPersonButton.gridy = 1;
 		add(registerPersonButton, gbc_registerPersonButton);
 		registerPersonButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				registerNewPlayerPane.setNoResults(false);
 				registerNewPlayerPane.getNameTextField().requestFocusInWindow();
 				registerNewPlayerPane.getNameTextField().setText("");
 				registerNewPlayerPane.getPhoneNumberTextField().setText("");
 				tournament.getMainWindow().hideAllBalloons();
 				registerNewPlayerTip.setVisible(true);
 			}
 		});
 
 		scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.gridwidth = 5;
 		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 1;
 		gbc_scrollPane.gridy = 2;
 		add(scrollPane, gbc_scrollPane);
 
 		scrollPane.setViewportView(table);
 
 		importXml = new JButton("Import XML");
 		GridBagConstraints gbc_btnImportXml = new GridBagConstraints();
 		gbc_btnImportXml.insets = new Insets(0, 0, 5, 5);
 		gbc_btnImportXml.gridx = 1;
 		gbc_btnImportXml.gridy = 3;
 		add(importXml, gbc_btnImportXml);
 		importXml.addActionListener(this);
 		
 		fileLabel = new JLabel("");
 		GridBagConstraints gbc_label = new GridBagConstraints();
 		gbc_label.anchor = GridBagConstraints.WEST;
 		gbc_label.gridwidth = 3;
 		gbc_label.insets = new Insets(0, 0, 5, 5);
 		gbc_label.gridx = 2;
 		gbc_label.gridy = 3;
 		add(fileLabel, gbc_label);
 
 		startPoolRound = new JButton("Start Pool Round");
 		GridBagConstraints gbc_startPoolRound = new GridBagConstraints();
 		gbc_startPoolRound.insets = new Insets(0, 0, 5, 5);
 		gbc_startPoolRound.gridx = 5;
 		gbc_startPoolRound.gridy = 3;
 		add(startPoolRound, gbc_startPoolRound);
 		startPoolRound.addActionListener(this);
 	}
 
 	private void initializeSearch() {
 		searchField = new JSearchTextField();
 		searchField.getDocument().addDocumentListener(
 				new DocumentListener() {
 					public void changedUpdate(DocumentEvent e) {
 						filter();
 					}
 					public void insertUpdate(DocumentEvent e) {
 						filter();
 					}
 					public void removeUpdate(DocumentEvent e) {
 						filter();
 					}
 				});
 		GridBagConstraints gbc_txtSearch = new GridBagConstraints();
 		gbc_txtSearch.gridwidth = 2;
 		gbc_txtSearch.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtSearch.insets = new Insets(0, 0, 5, 5);
 		gbc_txtSearch.gridx = 3;
 		gbc_txtSearch.gridy = 1;
 		add(searchField, gbc_txtSearch);
 		searchField.setColumns(10);
 		
 		searchField.addKeyListener
 		(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 					signInSelectedPlayer(true);
 				}
 			}
 		});
 	}
 	
 	private void signInSelectedPlayer(boolean checkAs) {
 		tournament.getMainWindow().hideAllBalloons();
 
 		//changed to table.convertColumIndexToView to prevent bug where user rearranges column ordering
 		int id = (Integer)table.getValueAt(table.getSelectedRow(), table.convertColumnIndexToView(4)); //Getting the ID DOES THIS WORK??
 		//Checking in the fencer as the checkAs boolean
 		System.out.println("ID and boolean: " + id + " " + checkAs);
 		Object[][] newData = tournament.checkInFencer(id, checkAs);
 		model.setData(newData);
 		searchField.setText("");
 		
 		this.repaint();
 	}
 
 	private void initializeTable() {
 		//Set up table with custom sorter
 		model = new SignInTableModel();
 		model.addTableModelListener(model);
 		sorter = new TableRowSorter<SignInTableModel>(model);
 		table = new JTable(model);
 		table.setRowSorter(sorter);
 		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
 		table.setFillsViewportHeight(true);
 		table.grabFocus();
 		table.getRowSorter().toggleSortOrder(0);
 	}
 
 	private void initializeGridBagLayout() {
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 88, 102, 102, 102, 102, 0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 	}
 
 	private void initializeBalloons() {
 		balloons = new ArrayList<BalloonTip>();
 
 		//setup tooltips
 		signInPlayerPane = new CheckInPlayerPanel();
 		signInPlayerTip = new BalloonTip(registerPersonButton, signInPlayerPane, new DefaultBalloonStyle(), false);
 		signInPlayerTip.setOpacity(0.9f);
 		signInPlayerPane.getCancelButton().addActionListener(this);
 		signInPlayerPane.getSignInButton().addActionListener(this);
 
 		registerNewPlayerPane = new RegisterNewPlayerPanel();
 		registerNewPlayerTip = new BalloonTip(registerPersonButton, registerNewPlayerPane, new DefaultBalloonStyle(), false);
 		registerNewPlayerTip.setOpacity(0.9f);
 		registerNewPlayerPane.getCancelButton().addActionListener(this);
 		registerNewPlayerPane.getDoneButton().addActionListener(this);
 
 		signInAllPane = new ConfirmationPanel("sign in");
 		signInAllTip = new BalloonTip(signInAll, signInAllPane, new DefaultBalloonStyle(), false);
 		signInAllTip.setOpacity(0.9f);
 		signInAllPane.getCancelButton().addActionListener(this);
 		signInAllPane.getYesButton().addActionListener(this);
 
 		unsignInAllPane = new ConfirmationPanel("un-sign in");
 		unsignInAllTip = new BalloonTip(unsignInAll, unsignInAllPane, new DefaultBalloonStyle(), false);
 		unsignInAllTip.setOpacity(0.9f);
 		unsignInAllPane.getCancelButton().addActionListener(this);
 		unsignInAllPane.getYesButton().addActionListener(this);
 
 		stripSetupPane = new StripSetupPanel();
 		stripSetupTip = new BalloonTip(startPoolRound, stripSetupPane, new DefaultBalloonStyle(), Orientation.RIGHT_ABOVE, AttachLocation.ALIGNED, 10, 10, false);
 		stripSetupTip.setOpacity(0.9f);
 		stripSetupPane.getCancelButton().addActionListener(this);
 		stripSetupPane.getDoneButton().addActionListener(this);
 
 		poolSizeInfoPane = new PoolSizeInfoPanel(tournament);
 		poolSizeTip = new BalloonTip(startPoolRound, poolSizeInfoPane, new DefaultBalloonStyle(), Orientation.RIGHT_ABOVE, AttachLocation.ALIGNED, 10, 10, false);
 		poolSizeTip.setOpacity(0.9f);
 
 		balloons.add(signInPlayerTip);
 		balloons.add(registerNewPlayerTip);
 		balloons.add(signInAllTip);
 		balloons.add(unsignInAllTip);
 		balloons.add(stripSetupTip);
 		balloons.add(poolSizeTip);
		tournament.getMainWindow().hideAllBalloons();
 	}
 
 	private void filter() {
 		RowFilter<SignInTableModel, Object> rf = null;
 		//If current expression doesn't parse, don't update.
 		try {
 			rf = RowFilter.regexFilter("(?i)" + searchField.getText());
 		} catch (java.util.regex.PatternSyntaxException e) {
 			return;
 		}
 		sorter.setRowFilter(rf);
 		if (table.getRowCount() == 0) {
 			//Hide other tooltips
 			tournament.getMainWindow().hideAllBalloons();
 			//Make tooltip visible
 			registerNewPlayerTip.setVisible(true);
 			//Clear any old text
 			registerNewPlayerPane.setNoResults(true);
 			registerNewPlayerPane.getNameTextField().setText("");
 			registerNewPlayerPane.getPhoneNumberTextField().setText("");
 			//Decide whether entered text is a name or phone number
 			try {
 				Long.parseLong(searchField.getText());
 				registerNewPlayerPane.getPhoneNumberTextField().setText(searchField.getText());
 				searchField.setNextFocusableComponent(registerNewPlayerPane.getNameTextField());
 			} catch (NumberFormatException e) {
 				registerNewPlayerPane.getNameTextField().setText(searchField.getText());
 				searchField.setNextFocusableComponent(registerNewPlayerPane.getPhoneNumberTextField());
 			}
 		}
 		else if (table.getRowCount() == 1) {
 			//Hide other tooltip
 			registerNewPlayerTip.setVisible(false);
 			//select this row
 			ListSelectionModel selectionModel = table.getSelectionModel();
 			selectionModel.setSelectionInterval(0, 0);
 			//Make tooltip visible
 			signInPlayerPane.getResultLabel().setText("<html><i>Exact Match Found:</i> <b>" + table.getValueAt(0, 0) + "</b></html>");
 			searchField.setNextFocusableComponent(signInPlayerPane.getSignInButton());
 			tournament.getMainWindow().hideAllBalloons();
 			signInPlayerTip.setVisible(true);
 		}
 		else {
 			tournament.getMainWindow().hideAllBalloons();
 		}
 	}
 
 	class SignInTableModel extends AbstractTableModel implements TableModelListener {
 		private static final long serialVersionUID = 1L;
 
 		private  String[] columnNames = {"Name", "Team", "Group", "Signed In", "ID"};
 
 		private Object[][] data = tournament.giveSignInPanelInfo();
 
 		public void setData(Object[][] newData) {
 			data = newData;
 		}
 
 		@Override
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 		@Override
 		public int getRowCount() {
 			return data.length;
 		}
 		@Override
 		public String getColumnName(int col) {
 			return columnNames[col];
 		}
 		@Override
 		public Object getValueAt(int row, int col) {
 			return data[row][col];
 		}
 		@Override
 		public Class getColumnClass(int c) {
 			return getValueAt(0, c).getClass();
 		}
 		@Override
 		public boolean isCellEditable(int row, int col) {
 			if (col < 2) {
 				return false;
 			} else {
 				return true;
 			}
 		}
 		@Override
 		public void setValueAt(Object value, int row, int col) {
 			if(data.length == 0)
 				return;
 			data[row][col] = value;
 			fireTableCellUpdated(row, col);
 		}
 
 		@Override
 		public void tableChanged(TableModelEvent e) {
 	        int row = e.getFirstRow();
 			int column = e.getColumn();
 	        TableModel model = (TableModel)e.getSource();
 	        Object data = model.getValueAt(row, column); //TODO TOTALLY broken
 
 	        if(column == 3){ //If the column is the signedIn button col
 	        	System.out.println("Changed: " + data);
 	        	signInSelectedPlayer((Boolean)data);
 	        }
 	        //Fix for weird sorting
 	        sorter.sort();
 	        table.clearSelection();
 			
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == importXml) {
 			//Import xml file
 			JFileChooser fileChooser = new JFileChooser();
 			int returnValue = fileChooser.showOpenDialog(importXml);
 			
 			if (returnValue == JFileChooser.APPROVE_OPTION) {
 				xmlFile = fileChooser.getSelectedFile();
 				fileLabel.setText(xmlFile.getPath());
 			}
 		}
 		else if (e.getSource() == startPoolRound) {
 			tournament.getMainWindow().hideAllBalloons();
 			stripSetupTip.setVisible(true);
 		}
 		else if (e.getSource() == signInPlayerPane.getCancelButton()) {
 			tournament.getMainWindow().hideAllBalloons();
 		}
 		else if (e.getSource() == signInPlayerPane.getSignInButton()) {
 			signInSelectedPlayer(true);
 		}
 		else if (e.getSource() == registerNewPlayerPane.getCancelButton()) {
 			tournament.getMainWindow().hideAllBalloons();
 		}
 		else if (e.getSource() == registerNewPlayerPane.getDoneButton()) {
 			tournament.getMainWindow().hideAllBalloons();
 			//Getting the info out of the registerNewPlayerPane
 			String number = registerNewPlayerPane.getPhoneNumberTextField().getText();
 			String name = registerNewPlayerPane.getNameTextField().getText();
 			String firstName = "", lastName = "";
 			int nameSplit = name.lastIndexOf(' ');
 			if (nameSplit > 0) {
 				firstName = name.substring(0, nameSplit);
 				lastName = name.substring(nameSplit+1, name.length());
 			} else {
 				firstName = name;
 				lastName = "";
 			}
 			int rank = Integer.parseInt(registerNewPlayerPane.getRankField().getText());
 			//TODO: Need to make it impossible to click done unless all fields are filled out
 			
 			/* Registering player and resetting the data in the table */
 			Object[][] newData = tournament.registerAndCheckInFencer(number, firstName, lastName, rank);
 			model.setData(newData);
 			this.getSearchField().setText("");
 			//Making sure the table is updated nicely
 			sorter.modelStructureChanged();
 			sorter.sort();
 			table.clearSelection();
 			this.repaint();
 		}
 		else if (e.getSource() == signInAll) {
 			//Make new signInAllTooltip
 			tournament.getMainWindow().hideAllBalloons();
 			signInAllTip.setVisible(true);
 
 		}
 		else if (e.getSource() == unsignInAll) {
 			tournament.getMainWindow().hideAllBalloons();
 			unsignInAllTip.setVisible(true);
 
 		}
 		else if (e.getSource() == signInAllPane.getCancelButton() || e.getSource() == unsignInAllPane.getCancelButton()) {
 			tournament.getMainWindow().hideAllBalloons();
 		}
 		else if (e.getSource() == signInAllPane.getYesButton()) {
 			tournament.getMainWindow().hideAllBalloons();
 			//Checking in all as true!
 			Object[][] newData = tournament.checkInAll(true);
 			model.setData(newData);
 	        //Fix for weird sorting
 	        sorter.sort();
 	        table.clearSelection();
 			this.repaint();
 
 		}
 		else if (e.getSource() == unsignInAllPane.getYesButton()) {
 			tournament.getMainWindow().hideAllBalloons();
 			//Checking in all as false
 			Object[][] newData = tournament.checkInAll(false);
 			model.setData(newData);
 	        //Fix for weird sorting
 	        sorter.sort();
 	        table.clearSelection();
 			this.repaint();
 
 		}
 		else if (e.getSource() == stripSetupPane.getCancelButton()) {
 			tournament.getMainWindow().hideAllBalloons();
 		}
 		else if (e.getSource() == stripSetupPane.getDoneButton()) {
 			tournament.getMainWindow().hideAllBalloons();
 
 			//Getting the strip arrangement from the editor
 			int row = (Integer) stripSetupPane.getRowSpinner().getValue();
 			int col = (Integer) stripSetupPane.getColSpinner().getValue();
 			tournament.setStripSizes(EVENT_ID, row, col);
 
 			//Registering all of the current players into the event
 			tournament.addAllPlayersToEvent(EVENT_ID);
 			
 			poolSizeTip.setVisible(true);
 			Object[][] newData = tournament.getPoolSizeInfoTable();
 			poolSizeInfoPane.setData(newData);
 			this.repaint();
 		}
 	}
 
 	public JSearchTextField getSearchField() {
 		return searchField;
 	}
 
 	public JButton getAddFencerButton() {
 		return registerPersonButton;
 	}
 
 	public void hideAllBalloons() {
 		for (BalloonTip b : balloons)
 			b.setVisible(false);
 	}
 }
