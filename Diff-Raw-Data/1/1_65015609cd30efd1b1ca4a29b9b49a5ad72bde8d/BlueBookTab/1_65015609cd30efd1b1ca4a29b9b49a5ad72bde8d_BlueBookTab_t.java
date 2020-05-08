 package blueBookTab;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import net.miginfocom.swing.MigLayout;
 import utilities.DatabaseHelper;
 import utilities.SearchHelper;
 import utilities.ui.DisplayPanel;
 import utilities.ui.ImageHandler;
 import utilities.ui.SwingHelper;
 
 //-----------------------------------------------------------------------------	
 /**
  * The <code>BlueBookTab</code> class creates a tab on the UMPD Management
  * System to hold information of <code>BlueBookEntry</code>s. 
  */
 public class BlueBookTab extends JPanel implements ActionListener {
 	private static final long serialVersionUID = 1L;
 	ArrayList<BlueBookEntry> bluebook;
 	JFrame parent;
 	JTextField caseNumField;
 	JTextField locationField;
 	JTextField nameField;
 	// -----------------------------------------------------------------------------
 	/**
 	 * Creates and sets the <code>BlueBookTab</code> to view all the
 	 * <code>BlueBookEntry</code>s, create <code>BlueBookEntry</code>s and
 	 * search the database for <code>BlueBookEntry</code>s
 	 * 
 	 * @param parent
 	 */
 	public BlueBookTab(final JFrame parent) {
 		this.setLayout(new BorderLayout());
 		this.parent = parent;
 
 		JPanel entriesPanel = createEntriesPanel();
 		// Create a button to create a new Blue Book entry
 		JButton newEntryButton = SwingHelper.createImageButton("Create Entry",
 				"icons/plusSign_48.png");
 		newEntryButton.addActionListener(new ActionListener() {
 			// Create new Blue Book entry form dialog
 			BlueBookForm formDialog = new BlueBookForm(parent);
 
 			public void actionPerformed(ActionEvent e) {
 				formDialog.setVisible(true);
 			}
 		});
 
 		// Create search button
 		JButton searchButton = SwingHelper.createImageButton(
 				"Search Blue Book", "icons/search.png");
 		searchButton.addActionListener(new ActionListener() {
 			// Search dialog
 			JDialog searchDialog = createSearchDialog(parent);
 
 			public void actionPerformed(ActionEvent e) {
 				searchDialog.setVisible(true);
 			}
 		});
 
 		// add the components to this panel
 
 		// this.add(tabbedPane, BorderLayout.CENTER);
 		this.add(entriesPanel, BorderLayout.CENTER);
 		JPanel buttonsPanel = new JPanel();
 		buttonsPanel.add(newEntryButton);
 		buttonsPanel.add(searchButton);
 		this.add(buttonsPanel, BorderLayout.PAGE_END);
 	}
 
 	// -----------------------------------------------------------------------------
 	/**
 	 * Creates a search dialog for the <code>BlueBookTab</code> when the
 	 * <code>searchButton</code> is clicked
 	 * 
 	 * @param parent
 	 * @return
 	 */
 	public JDialog createSearchDialog(JFrame parent) {
 		// Create the dialog and set the size
 		final JDialog searchDialog = new JDialog(parent, "Search Blue Book Database",
 				true);
 		searchDialog.setPreferredSize(SwingHelper.SEARCH_DIALOG_DIMENSION);
 		searchDialog.setSize(SwingHelper.SEARCH_DIALOG_DIMENSION);
 
 		// Put the dialog in the middle of the screen
 		searchDialog.setLocationRelativeTo(null);
 
 		// Create the various search fields and add them to the dialog
 		JPanel searchPanel = new JPanel();
 		searchPanel.setLayout(new MigLayout("align left"));
 		SwingHelper.addLineBorder(searchPanel);
 
 		JLabel caseNumLabel = new JLabel("Case #: ");
 		JLabel locationLabel = new JLabel("Location: ");
 		JLabel nameLabel = new JLabel("Name: ");
 
 		caseNumField = new JTextField(SwingHelper.DEFAULT_TEXT_FIELD_LENGTH);
 		locationField = new JTextField(SwingHelper.DEFAULT_TEXT_FIELD_LENGTH);
 		nameField = new JTextField(SwingHelper.DEFAULT_TEXT_FIELD_LENGTH);
 
 		JButton searchButton = SwingHelper.createImageButton("Search",
 				"icons/search.png");
 		searchButton.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent arg0) {
 				search();
 				searchDialog.dispose();
 			}
 			
 		});
 
 		String[] statusStrings = { "Need to Identify", "Identified", "Arrested" };
 		JComboBox<String> statusList = new JComboBox<String>(statusStrings);
 		statusList.setSelectedIndex(0);
 
 		searchPanel.add(caseNumLabel, "alignx left");
 		searchPanel.add(caseNumField, "alignx left, wrap");
 		searchPanel.add(nameLabel, "alignx left");
 		searchPanel.add(nameField, "alignx left, wrap");
 		searchPanel.add(locationLabel, "alignx left");
 		searchPanel.add(locationField, "alignx left, wrap");
 		SwingHelper.addDateRangePanel(searchPanel);
 		searchPanel.add(searchButton, "alignx center, wrap");
 
 		Container contentPane = searchDialog.getContentPane();
 		contentPane.add(searchPanel);
 		return searchDialog;
 	}
 
 	// -----------------------------------------------------------------------------
 
 	@SuppressWarnings("unchecked")
 	public void search() {
 		ArrayList<BlueBookEntry> searchResults = new ArrayList<BlueBookEntry>();
 		ArrayList<String >fields = new ArrayList<String>();
 		ArrayList<String >parameters = new ArrayList<String>();
 	    //fill search terms		
 		if (!caseNumField.getText().equals("")) {
 		    fields.add("caseNum");
 	        parameters.add(caseNumField.getText());
 		}
 		if (!locationField.getText().equals("")){
 	        fields.add("location");
 	        parameters.add(locationField.getText());
 		} if (!nameField.getText().equals("")) {
 	        fields.add("name");
 	        parameters.add(nameField.getText());
 		}			    			    		
 		try {
 			searchResults = (ArrayList<BlueBookEntry>) SearchHelper.search("bluebook", fields, parameters);
 			//DEBUG
 //			for (BlueBookEntry entry : searchResults) {
 //					System.out.println("case number :" + entry.getCaseNum());	
 //			}
 		} catch (Exception e) {
 			System.out.println("Couldn't run search in bluebook"); 
 			e.printStackTrace();
 		}
 		JDialog searchDialog = new JDialog();
 		JPanel searchEntriesPanel = createSearchEntriesPanel();
 		searchDialog.add(searchEntriesPanel, BorderLayout.CENTER);
 		searchDialog.setLocationRelativeTo(null);
 		searchDialog.setSize(500,500); //set dynamically TODO
 		searchDialog.setVisible(true);//BUG disappears
 	}
 
 	// -----------------------------------------------------------------------------
 	/**
 	 * Create the <code>entriesPanel</code> and populate it with data from the
 	 * database
 	 * 
 	 * @return entriesPanel
 	 */
 	public JPanel createEntriesPanel() {
 		JPanel entriesPanel = new JPanel(new MigLayout("gapx 30, wrap 4"));
 		JPanel entryPanel;
 		// Date prepDate;
 
 		// TODO: make scrollable!
 
 		try {
 			bluebook = DatabaseHelper.getBluebookFromDB();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		int listSize = bluebook.size();
 		JPanel[] items = new JPanel[listSize];
 		Format formatter = new SimpleDateFormat("E, MMM dd, yyyy");
 
 		int i = 0;
 		for (BlueBookEntry entry : bluebook) {
 			entryPanel = new JPanel(
 					new MigLayout("flowy", "[][]", "[][center]"));
 			String listId = "" + bluebook.indexOf(entry);
 			if (entry.getPhotoFilePath() != null) {
 				JLabel photoLabel = new JLabel(ImageHandler.getScaledImageIcon(
 						entry.getPhoto(), 100, 100));
 				entryPanel.add(photoLabel);
 			}
 			String caseNum = "";
 			if (entry.getCaseNum() != null) {
 				caseNum = entry.getCaseNum();
 			}
 			String status = "";
 			if (entry.getStatus() != null) {
 				status = entry.getStatus();
 			}
 			String armedText = "";
 			if (entry.getWeapon() != null) {
 				armedText = ("<html><center><font color=#FF0000>ARMED</font></center></html>");
 			}
 
 			entryPanel.add(new JLabel(armedText, SwingConstants.CENTER),
 					"alignx center,wrap");
 
 			entryPanel.add(new JLabel(" "), "split 3, aligny top");
 			entryPanel.add(new JLabel("Case#: " + caseNum));
 			entryPanel.add(new JLabel(status));
 			entryPanel.setSize(new Dimension(130, 150));
 			entryPanel.setPreferredSize(new Dimension(130, 150));
 
 			entryPanel.setName(listId);
 			items[i] = entryPanel;
 			i++;
 		}
 
 		DisplayPanel itemsPanel = new DisplayPanel(items, this);
 
 		entriesPanel.add(itemsPanel);
 
 		return entriesPanel;
 	}
 // -----------------------------------------------------------------------------
 	/**
 	 * Create the <code>entriesPanel</code> and populate it with data from the
 	 * database
 	 * 
 	 * @return entriesPanel
 	 */
 	public JPanel createSearchEntriesPanel() {
 		JPanel entriesPanel = new JPanel(new MigLayout("gapx 30, wrap 4"));
 		JPanel entryPanel;
 		// Date prepDate;
 
 		// TODO: make scrollable!
 
 		try {
 			bluebook = DatabaseHelper.getBluebookFromDB();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		int listSize = bluebook.size();
 		JPanel[] items = new JPanel[listSize];
 		Format formatter = new SimpleDateFormat("E, MMM dd, yyyy");
 
 		int i = 0;
 		for (BlueBookEntry entry : bluebook) {
 			entryPanel = new JPanel(
 					new MigLayout("flowy", "[][]", "[][center]"));
 			String listId = "" + bluebook.indexOf(entry);
 			if (entry.getPhotoFilePath() != null) {
 				JLabel photoLabel = new JLabel(ImageHandler.getScaledImageIcon(
 						entry.getPhoto(), 100, 100));
 				entryPanel.add(photoLabel);
 			}
 			String caseNum = "";
 			if (entry.getCaseNum() != null) {
 				caseNum = entry.getCaseNum();
 			}
 			String status = "";
 			if (entry.getStatus() != null) {
 				status = entry.getStatus();
 			}
 			String armedText = "";
 			if (entry.getWeapon() != null) {
 				armedText = ("<html><center><font color=#FF0000>ARMED</font></center></html>");
 			}
 
 			entryPanel.add(new JLabel(armedText, SwingConstants.CENTER),
 					"alignx center,wrap");
 
 			entryPanel.add(new JLabel(" "), "split 3, aligny top");
 			entryPanel.add(new JLabel("Case#: " + caseNum));
 			entryPanel.add(new JLabel(status));
 			entryPanel.setSize(new Dimension(130, 150));
 			entryPanel.setPreferredSize(new Dimension(130, 150));
 
 			entryPanel.setName(listId);
 			items[i] = entryPanel;
 			i++;
 		}
 
 		DisplayPanel itemsPanel = new DisplayPanel(items, this);
 
 		entriesPanel.add(itemsPanel);
 
 		return entriesPanel;
 	}
 // -----------------------------------------------------------------------------
 	/**
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 
 	public void actionPerformed(ActionEvent ev) {
 
 		String listId = ev.getActionCommand();
 		int id = Integer.valueOf(listId);
 		System.out.println("BlueBookTab: actionPerformed: id = " + id);
 		BlueBookEntry selectedEntry = bluebook.get(id);
 		BlueBookForm form = new BlueBookForm(parent, selectedEntry);
 		form.setVisible(true);
 	}
 	// -----------------------------------------------------------------------------
 }
