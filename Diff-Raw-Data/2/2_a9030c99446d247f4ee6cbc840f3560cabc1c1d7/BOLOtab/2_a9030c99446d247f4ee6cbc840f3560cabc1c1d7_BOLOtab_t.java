 package boloTab;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Date;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import net.miginfocom.swing.MigLayout;
 import utilities.DatabaseHelper;
 import utilities.SwingHelper;
 
 public class BOLOtab extends JPanel  implements ActionListener {
 	
//TODO Java Docs
 	
 private static final long serialVersionUID = 1L;
 	ArrayList<Bolo> boloList;
 	JFrame parent;
 //-----------------------------------------------------------------------------
 	public BOLOtab(final JFrame parent){
 		this.setLayout(new BorderLayout());
 				
 		//ArrayList<Bolo> boloList = null;
 		this.parent = parent;
 		
 		//Create BOLOs tabbed display area
 		JTabbedPane tabbedPane = new JTabbedPane();
 		//Add recent BOLOs tab
 		JPanel recentBolosTab = createRecentBOLOsTab();
 		tabbedPane.addTab("Recent BOLOs", recentBolosTab);
 		tabbedPane.setMnemonicAt(0, KeyEvent.VK_2);
 	    //Add archived BOLOs tab 
 		JPanel archievedBolosTab = new JPanel();
 		tabbedPane.addTab("Archieved", archievedBolosTab);
 		tabbedPane.setMnemonicAt(1, KeyEvent.VK_3);
         
 		//Create a button to create a new BOLO 
 		JButton newBOLOButton = SwingHelper.createImageButton("Create BOLO", 
 				"icons/plusSign_48.png");
 		newBOLOButton.addActionListener(new ActionListener() {
 			//BOLO form dialog
 			BOLOform formDialog = new BOLOform(parent);
 			public void actionPerformed(ActionEvent e){
 				formDialog.setVisible(true);
 			}
 		});
 
 		//Create a button to import an existing BOLO
 		JButton importBOLOButton = SwingHelper.createImageButton("Import Existing BOLO", 
 				"icons/Import.png");
 		importBOLOButton.addActionListener(new ActionListener() {
 			//file chooser dialog
 			public void actionPerformed(ActionEvent e){
 				//file chooser dialog .setVisable(true);
 				//Create a file chooser
 				final JFileChooser fc = new JFileChooser();
 
 				//In response to a button click:
 			//	int returnVal = 
 						fc.showOpenDialog(parent);
 			}
 		});
 
 		//Create search button
 		JButton searchButton = SwingHelper.createImageButton("Search Records", 
 				"icons/search.png");
 		searchButton.addActionListener(new ActionListener() {
 			//Search dialog
 			JDialog searchDialog = createSearchDialog(parent);
 			public void actionPerformed(ActionEvent e){
 				searchDialog.setVisible(true);
 			}
 		});
 
         this.add(tabbedPane, BorderLayout.CENTER);
         JPanel buttonsPanel = new JPanel();
         buttonsPanel.add(newBOLOButton);
         buttonsPanel.add(importBOLOButton);
         buttonsPanel.add(searchButton);
         this.add(buttonsPanel, BorderLayout.PAGE_END);
 		
 	}
 //-----------------------------------------------------------------------------
 	JDialog createSearchDialog(JFrame parent){
 		//Create the dialog and set the size
 		JDialog searchDialog = new JDialog(parent, "Search BOLO Database", true);
 		searchDialog.setPreferredSize(SwingHelper.SEARCH_DIALOG_DIMENSION);
 		searchDialog.setSize(SwingHelper.SEARCH_DIALOG_DIMENSION);
 		
 		//Put the dialog in the middle of the screen
 		searchDialog.setLocationRelativeTo(null);
 	
 		//Create the various search fields and add them to the dialog
 		JPanel searchPanel = new JPanel();
 		searchPanel.setLayout(new MigLayout("align left"));
 		SwingHelper.addLineBorder(searchPanel);
 
 		JLabel caseNumLabel = new JLabel("Case #: ");
 		JLabel locationLabel = new JLabel("Location: ");
 		JLabel statusLabel = new JLabel("Status: ");
 		
 		JTextField caseNumField = new JTextField(SwingHelper.DEFAULT_TEXT_FIELD_LENGTH);
 		JTextField locationField = new JTextField(SwingHelper.DEFAULT_TEXT_FIELD_LENGTH);
 		
 		String[] statusStrings = { "Need to Identify", "Identified", "Arrested" };
 		JComboBox<String> statusList = new JComboBox<String>(statusStrings);
 		statusList.setSelectedIndex(0);
 
 		searchPanel.add(caseNumLabel, "alignx left");
 		searchPanel.add(caseNumField, "alignx left, wrap");
 		searchPanel.add(locationLabel,"alignx left");
 		searchPanel.add(locationField, "alignx left, wrap");
 
 		SwingHelper.addDateRangePanel(searchPanel);
 
 		searchPanel.add(statusLabel, "alignx left");
 		searchPanel.add(statusList, "alignx left, wrap");
 
 		
 		Container contentPane = searchDialog.getContentPane();
 		contentPane.add(searchPanel);
 		return searchDialog;
 	}
 //-----------------------------------------------------------------------------
 	public JPanel createRecentBOLOsTab(){
 		JPanel recentBOLOsPanel = new JPanel(new MigLayout("gapx 30"));
 		JButton boloPanel;
 		Date prepDate;
 		
 		try {
 			boloList = DatabaseHelper.getBOLOsFromDB();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		for(Bolo bolo: boloList){
 			String listId = "" + boloList.indexOf(bolo);
 			System.out.printf("listId = %s\n", listId);
 			prepDate = DatabaseHelper.convertEpochToDate(bolo.getprepDate());
 			ImageIcon photo = bolo.getPhoto();
 			String txt = prepDate.toString();
 			boloPanel = new JButton(txt, photo);
 			boloPanel.setActionCommand(listId);
 			boloPanel.addActionListener(this);
 			
 			//TODO: add other stuff
 			
 			recentBOLOsPanel.add(boloPanel, "width 10%, height 40%");
 		}
 		
 		return recentBOLOsPanel;
 	}
 //-----------------------------------------------------------------------------		
 	/* (non-Javadoc)
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed(ActionEvent ev) {
 		String listId = ev.getActionCommand();
 		int id = Integer.valueOf(listId);
 		
 		Bolo selectedBOLO = boloList.get(id);
 		BOLOpreview preview = new BOLOpreview(parent, selectedBOLO);
 		preview.setVisible(true);
 		
 	}
 //-----------------------------------------------------------------------------	
 }
