 package blueBookTab;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Toolkit;
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
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import net.miginfocom.swing.MigLayout;
 import program.ResourceManager;
 import userinterface.MainInterfaceWindow;
 import utilities.DatabaseHelper;
 import utilities.SearchHelper;
 import utilities.ui.DisplayPanel;
 import utilities.ui.ImageHandler;
 import utilities.ui.SwingHelper;
 
 /*
  * BUG When you try to upload multiple blue book entries back to back, opens the preview for the last created bbentry 
  * instead of a new form 
  *
  * BUG doesn't refresh after creating a bolo, have to reopen project
  */
 //-----------------------------------------------------------------------------	
 /**
  * The <code>BlueBookTab</code> class creates a tab on the UMPD Management
  * System to hold information of <code>BlueBookEntry</code>s. 
  */
 public class BlueBookTab extends JPanel implements ActionListener {
 	private static final long serialVersionUID = 1L;
 	//Search Fields
 	JTextField caseNumField;
 	JTextField locationField;
 	JTextField nameField;
 	MainInterfaceWindow mainInterface;
 	ResourceManager rm;
 	BlueBookForm newFormDialog;
 	ArrayList<BlueBookEntry> bluebook;
 	DisplayPanel entriesScroller;
 //-----------------------------------------------------------------------------
 	/**
 	 * Creates and sets the <code>BlueBookTab</code> to view all the
 	 * <code>BlueBookEntry</code>s, create <code>BlueBookEntry</code>s and
 	 * search the database for <code>BlueBookEntry</code>s
 	 * 
 	 * @param parent
 	 */
 	public BlueBookTab(final ResourceManager rm, MainInterfaceWindow mainInterface) {
 		this.setLayout(new BorderLayout());
 		this.mainInterface=mainInterface;
 		this.rm=rm;
 		
 		//Create entries display area
		DisplayPanel entriesScroller = createEntriesPanel();
 		
 		//Create New Entry button
 		JButton newEntryButton = SwingHelper.createImageButton("Create Entry",
 				"icons/plusSign_48.png");
 		newEntryButton.addActionListener(new ActionListener() {
 			// Create new Blue Book entry form dialog
 			public void actionPerformed(ActionEvent e) {
 				//Display the new BlueBookEntry form dialog
 				newFormDialog.setVisible(true);
 				//wait for the dialog to be dismissed before continuing
 				newFormDialog.setModal(true);
 				//refresh to display any changes
 				
 				refreshBBtab();
 
 				//refresh to display any changes
 //				entriesPanel.removeAll();
 //				entriesPanel.add(createEntriesPanel());
 //				panel.revalidate();
 			}
 		});
 
 		//Search button
 		JButton searchButton = SwingHelper.createImageButton(
 				"Search Blue Book", "icons/search.png");
 		searchButton.addActionListener(new ActionListener() {
 			// Search dialog
 			JDialog searchDialog = createSearchDialog(rm.getGuiParent());
 
 			public void actionPerformed(ActionEvent e) {
 				searchDialog.setVisible(true);
 			}
 		});
 
 		
 		// add the components to this tab
 
 		//this.add(entriesPanel, BorderLayout.CENTER);
 		this.add(entriesScroller, BorderLayout.CENTER);
 		JPanel buttonsPanel = new JPanel();
 		buttonsPanel.add(newEntryButton);
 		buttonsPanel.add(searchButton);
 		this.add(buttonsPanel, BorderLayout.PAGE_END);
 		
 		//TODO: Change below to be happening on bg thread so usr doesn't have to wait
 		newFormDialog = new BlueBookForm(rm.getGuiParent(), this);
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * Creates a search dialog for the <code>BlueBookTab</code> when the
 	 * <code>searchButton</code> is clicked.
 	 * 
 	 * @param parent - Parent JFrame
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
 //-----------------------------------------------------------------------------
 	@SuppressWarnings("unchecked")
 	public void search() {
 		ArrayList<BlueBookEntry> searchResults = new ArrayList<BlueBookEntry>();
 		ArrayList<String >fields = new ArrayList<String>();
 		ArrayList<String >parameters = new ArrayList<String>();
 		
 		//TODO deal with all fields null case (probably pop up another dialog saying such)
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
 		JDialog searchDialog = new JDialog(rm.getGuiParent(), "Search Results", true);
 		JPanel searchEntriesPanel = createSearchEntriesPanel(searchResults);
 		searchDialog.add(searchEntriesPanel, BorderLayout.CENTER);
 		searchDialog.setLocationByPlatform(true);
 		Toolkit toolkit =  Toolkit.getDefaultToolkit ();
 		Dimension dialogDim = new Dimension(toolkit.getScreenSize().width/2, toolkit.getScreenSize().height/2);
 		searchDialog.setSize(dialogDim); 
 		searchDialog.setVisible(true);
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * Create the <code>DisplayPanel</code> and populate it with data from the
 	 * database.
 	 * 
 	 * @return   ... JDOC
 	 */
 	public DisplayPanel createEntriesPanel() {
 		JPanel entryPanel;
 		// Date prepDate;
 
 		try {
 			bluebook = DatabaseHelper.getBluebookFromDB();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		int listSize = bluebook.size();
 //DEBUG
 System.out.println("bluebook size = " + bluebook.size());
 		
 		JPanel[] items = new JPanel[listSize];
 		Format formatter = new SimpleDateFormat("E, MMM dd, yyyy");
 
 		int i = 0;
 		for (BlueBookEntry entry : bluebook) {
 			String listId = "" + bluebook.indexOf(entry);
 			
 			entryPanel = new JPanel(new MigLayout("flowy", "[][]", "[][center]"));
 			
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
 
		DisplayPanel itemsPanel = new DisplayPanel(items, this, 4);
 		
		return itemsPanel;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * 
 	 */
 	public JPanel[] createItemsPanels() {
 		JPanel entryPanel;
 		// Date prepDate;
 
 		try {
 			bluebook = DatabaseHelper.getBluebookFromDB();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		int listSize = bluebook.size();
 //DEBUG
 System.out.println("bluebook size = " + bluebook.size());
 		
 		JPanel[] items = new JPanel[listSize];
 		Format formatter = new SimpleDateFormat("E, MMM dd, yyyy");
 
 		int i = 0;
 		for (BlueBookEntry entry : bluebook) {
 			String listId = "" + bluebook.indexOf(entry);
 			
 			entryPanel = new JPanel(new MigLayout("flowy", "[][]", "[][center]"));
 			
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
 		return items;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * Create the <code>entriesPanel</code> and populate it with data from the
 	 * database.
 	 * @return entriesPanel
 	 */
 	public JPanel createSearchEntriesPanel(ArrayList<BlueBookEntry> bluebook) {
 		JPanel entriesPanel = new JPanel(new MigLayout("gapx 30, wrap 4"));
 		JPanel entryPanel;
 		// Date prepDate;
 
 		// TODO: make scrollable!
 
 		//DEBUG
 		if (bluebook == null) {
 			System.out.println("error with search results in create search entries"); 
 		} //end DEBUG
 
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
 
 		DisplayPanel itemsPanel = new DisplayPanel(items, this, 4);
 
 		entriesPanel.add(itemsPanel, BorderLayout.CENTER);
 
 		return entriesPanel;
 	}
 //-----------------------------------------------------------------------------		
 	/**
 	 * JDOC 
 	 */
 	public void refreshBBtab(){
 		JPanel[] newItems = createItemsPanels();
 		entriesScroller.refreshContents(newItems);
 		entriesScroller.revalidate();
 		//tabbedPane.revalidate();
 
 	}
 //-----------------------------------------------------------------------------
 	public void actionPerformed(ActionEvent ev) {
 		//get which entry was click
 		String listId = ev.getActionCommand();
 		int id = Integer.valueOf(listId);
 //DEBUG
 		System.out.println("BlueBookTab: actionPerformed: id = " + id);
 		
 		BlueBookEntry selectedEntry = bluebook.get(id);
 		BlueBookPreview bbPreview = new BlueBookPreview(rm.getGuiParent(), this, selectedEntry);
 		bbPreview.setVisible(true);
 		//BlueBookForm form = new BlueBookForm(parent, this, selectedEntry);
 		//form.setVisible(true);
 	}
 //-----------------------------------------------------------------------------
 }
