 //TODO: Warn user when they are about to delete a serial or paper
 //TODO: Don't warn user if their isn't anything to warn them about. (ie overwrited nonexistant data)

 
 
 package Jeff;
 
 import Daniel.*;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowEvent;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 /**
  * Project #3
  * CS 2334, Section 011
  * 10/9/2013
  * This class allows for user interaction between a selection view, a display view, and a scholarship model
  * @version 1.0
  */
 public class ScholarPubController{
 	/** The model */
 	private ScholarshipModel model;
 	/** The selection view */
 	private SelectionView selectionView;
 	/** The display view */
 	private DisplayView displayView;
 	/** The information view */
 	private InformationView informationView;
 	
 	/** The default constructor
 	 * 
 	 * @param model		The model to be used
 	 */
 	public ScholarPubController(ScholarshipModel model){
 		this.model = model;
 	}
 
 	/**
 	 * Sets the selection view to the given view
 	 * @param view		The new selection view
 	 */
 	public void setSelectionView(SelectionView view){
 		selectionView = view;
 		ListClickListener listClickListener = new ListClickListener();
 		selectionView.getScholarList().addMouseListener(listClickListener);
 		selectionView.getSerialList().addMouseListener(listClickListener);
 		selectionView.getPaperList().addMouseListener(listClickListener);
 		selectionView.getAddScholarsButton().addActionListener(new AddScholarsListener());
 		selectionView.getAddSerialsButton().addActionListener(new AddSerialsListener());
 		selectionView.getAddPapersButton().addActionListener(new AddPapersListener());
 		selectionView.getRemoveScholarsButton().addActionListener(new RemoveScholarsListener());
 		selectionView.getRemoveSerialsButton().addActionListener(new RemoveSerialsListener());
 		selectionView.getRemovePapersButton().addActionListener(new RemovePapersListener());
 		selectionView.getRemoveAllScholarsButton().addActionListener(new RemoveAllScholarsListener());
 		selectionView.getRemoveAllSerialsButton().addActionListener(new RemoveAllSerialsListener());
 		selectionView.getRemoveAllPapersButton().addActionListener(new RemoveAllPapersListener());
 
 		selectionView.getSaveMenuItem().addActionListener(new SaveListener());
 		selectionView.getLoadMenuItem().addActionListener(new LoadListener());
 		
 		DisplayViewListener odvl = new DisplayViewListener();
 		selectionView.getTypeMenuItem().addActionListener(odvl);
 		selectionView.getPubsPerYearMenuItem().addActionListener(odvl);
 		selectionView.getConfPapsPerYearMenuItem().addActionListener(odvl);
 		selectionView.getJourArtsPerYearMenuItem().addActionListener(odvl);
 		selectionView.getNumOfCoauthsMenuItem().addActionListener(odvl);
 		
 		selectionView.getInformationItem().addActionListener(new InformationListener());
 	}
 
 	/**
 	 * Sets the display view to the given view
 	 * @param view		The new display view
 	 */
 	public void setDisplayView(DisplayView view){
 		displayView = view;
 	}
 	
 	/**
 	 * Sets the information view to the given view
 	 * @param view		The new information view
 	 */
 	public void setInformationView(InformationView view){
 		informationView = view;
 	}
 
 	/**
 	 * Opens a dialog that prompts the user for issue information
 	 * @return	A new Issue created using the user input
 	 */
 	private Issue openCreateIssueDialog(){
 		final JComboBox<String> monthSelector = new JComboBox<String>(new String[]{"January","February","March","April","May","June","July","August","September","October","November","December"});
 		String[] years = new String[150];
 		for(int i = 149; i >= 0; i--){
 			years[i] = ""+ (Calendar.getInstance().get(Calendar.YEAR) - i);
 		}
 		JLabel dateLabel = new JLabel("Date");
 		final JComboBox<String> yearSelector = new JComboBox<String>(years);
 		JLabel editorLabel = new JLabel("Editors");
		final JList<Scholar> editorList = new JList<Scholar>((Scholar[]) model.getScholarMap().values().toArray());
 		JLabel reviewerLabel = new JLabel("Reviewers");
		final JList<Scholar> reviewerList = new JList<Scholar>((Scholar[]) model.getScholarMap().values().toArray());
 		JLabel paperLabel = new JLabel("Papers");
		final JList<Paper> paperList = new JList<Paper>((Paper[]) model.getPaperMap().values().toArray());
 		JButton saveIssueButton = new JButton("Create Issue");
 
 		JPanel datePanel = new JPanel();
 		JPanel editorPanel = new JPanel();
 		JPanel reviewerPanel = new JPanel();
 		JPanel paperPanel = new JPanel();
 		JPanel savePanel = new JPanel();
 
 		datePanel.add(dateLabel);
 		datePanel.add(monthSelector);
 		datePanel.add(yearSelector);
 		editorPanel.add(editorLabel);
 		editorPanel.add(editorList);
 		reviewerPanel.add(reviewerLabel);
 		reviewerPanel.add(reviewerList);
 		paperPanel.add(paperLabel);
 		paperPanel.add(paperList);
 		savePanel.add(saveIssueButton);
 
 		Box addIssueBox = Box.createVerticalBox();
 		addIssueBox.add(Box.createVerticalGlue());
 		addIssueBox.add(datePanel);
 		addIssueBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addIssueBox.add(editorPanel);
 		addIssueBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addIssueBox.add(reviewerPanel);
 		addIssueBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		if(model.getPaperMap().size() > 0){
 			addIssueBox.add(paperPanel);
 			addIssueBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		}
 		addIssueBox.add(savePanel);
 		addIssueBox.add(Box.createVerticalGlue());
 
 		final JDialog addIssueDialog = new JDialog();
 		
 		final Issue theNewIssue = new Issue();
 		saveIssueButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				//Add date
 				theNewIssue.setMonth(monthSelector.getSelectedItem().toString());
 				theNewIssue.setYear(yearSelector.getSelectedItem().toString());
 				//Add editors
 				ScholarMap editors = new ScholarMap();
 				Object[] editorArr = editorList.getSelectedValuesList().toArray();
 				for(int i = 0; i < editorArr.length; i++){
 					editors.put(((Scholar)editorArr[i]).getName(), (Scholar)editorArr[i]);
 				}
 				theNewIssue.setEditors(editors);
 				//Add reviewers
 				ScholarMap reviewers = new ScholarMap();
 				Object[] reviewerArr = reviewerList.getSelectedValuesList().toArray();
 				for(int i = 0; i < reviewerArr.length; i++){
 					reviewers.put(((Scholar)reviewerArr[i]).getName(), (Scholar)reviewerArr[i]);
 				}
 				theNewIssue.setReviewers(reviewers);
 				//Add papers
 				PaperMap papers = new PaperMap();
 				Object[] paperArr = paperList.getSelectedValuesList().toArray();
 				for(int i = 0; i < paperArr.length; i++){
 					papers.put(((Paper)paperArr[i]).getTitle(), (Paper)paperArr[i]);
 				}
 				theNewIssue.setPapers(papers);
 				
 				addIssueDialog.dispatchEvent(new WindowEvent(addIssueDialog, WindowEvent.WINDOW_CLOSING));
 			}
 		});
 		
 		addIssueDialog.setTitle("Add Issue");
 		addIssueDialog.add(addIssueBox);
 		addIssueDialog.setSize(selectionView.getWidth() - 40, selectionView.getHeight() - 100);
 		addIssueDialog.setLocationRelativeTo(selectionView);
 		addIssueDialog.setModal(true);
 		addIssueDialog.setVisible(true);
 		
 		if(theNewIssue.getMonth() == null){
 			return null;
 		}
 		return theNewIssue;
 	}
 
 	/**
 	 * Opens a dialog that prompts the user for meeting information
 	 * @return	A new Meeting created using the user input
 	 */
 	private Meeting openCreateMeetingDialog(){
 		final JComboBox<String> monthSelector = new JComboBox<String>(new String[]{"January","February","March","April","May","June","July","August","September","October","November","December"});
 		String[] years = new String[150];
 		for(int i = 149; i >= 0; i--){
 			years[i] = ""+ (Calendar.getInstance().get(Calendar.YEAR) - i);
 		}
 		JLabel dateLabel = new JLabel("Date");
 		final JComboBox<String> yearSelector = new JComboBox<String>(years);
 		JLabel cityLabel = new JLabel("City");
 		final JTextField cityField = new JTextField();
 		JLabel stateLabel = new JLabel("State/Province");
 		final JTextField stateField = new JTextField();
 		JLabel countryLabel = new JLabel("Country");
 		final JTextField countryField = new JTextField();
 		JLabel chairLabel = new JLabel("Chair");
		final JList<Scholar> chairList = new JList<Scholar>((Scholar[]) model.getScholarMap().values().toArray());
 		chairList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		JLabel committeeLabel = new JLabel("Committee Members");
		final JList<Scholar> committeeList = new JList<Scholar>((Scholar[]) model.getScholarMap().values().toArray());
 		JLabel paperLabel = new JLabel("Papers");
		final JList<Paper> paperList = new JList<Paper>((Paper[]) model.getPaperMap().values().toArray());
 		JButton saveMeetingButton = new JButton("Create Meeting");
 		
 		final Dimension textFieldDimension = new Dimension(160, 30);
 		cityField.setPreferredSize(textFieldDimension);
 		stateField.setPreferredSize(textFieldDimension);
 		countryField.setPreferredSize(textFieldDimension);
 
 		JPanel datePanel = new JPanel();
 		JPanel cityPanel = new JPanel();
 		JPanel statePanel = new JPanel();
 		JPanel countryPanel = new JPanel();
 		JPanel chairPanel = new JPanel();
 		JPanel committeePanel = new JPanel();
 		JPanel paperPanel = new JPanel();
 		JPanel savePanel = new JPanel();
 
 		datePanel.add(dateLabel);
 		datePanel.add(monthSelector);
 		datePanel.add(yearSelector);
 		cityPanel.add(cityLabel);
 		cityPanel.add(cityField);
 		statePanel.add(stateLabel);
 		statePanel.add(stateField);
 		countryPanel.add(countryLabel);
 		countryPanel.add(countryField);
 		chairPanel.add(chairLabel);
 		chairPanel.add(chairList);
 		committeePanel.add(committeeLabel);
 		committeePanel.add(committeeList);
 		paperPanel.add(paperLabel);
 		paperPanel.add(paperList);
 		savePanel.add(saveMeetingButton);
 
 		Box addMeetingBox = Box.createVerticalBox();
 		addMeetingBox.add(Box.createVerticalGlue());
 		addMeetingBox.add(datePanel);
 		addMeetingBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addMeetingBox.add(cityPanel);
 		addMeetingBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addMeetingBox.add(statePanel);
 		addMeetingBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addMeetingBox.add(countryPanel);
 		addMeetingBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addMeetingBox.add(chairPanel);
 		addMeetingBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addMeetingBox.add(committeePanel);
 		addMeetingBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		if(model.getPaperMap().size() > 0){
 			addMeetingBox.add(paperPanel);
 			addMeetingBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		}
 		addMeetingBox.add(savePanel);
 		addMeetingBox.add(Box.createVerticalGlue());
 
 		final JDialog addMeetingDialog = new JDialog();
 		
 		final Meeting theNewMeeting = new Meeting();
 		saveMeetingButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				//Add date
 				theNewMeeting.setMonth(monthSelector.getSelectedItem().toString());
 				theNewMeeting.setYear(yearSelector.getSelectedItem().toString());
 				//Add location
 				if(cityField.getText().length() == 0){
 					JOptionPane.showMessageDialog(addMeetingDialog, "Please enter a city", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				if(countryField.getText().length() == 0){
 					JOptionPane.showMessageDialog(addMeetingDialog, "Please enter a country", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				if(stateField.getText().length() != 0){
 					theNewMeeting.setLocation(new Location(cityField.getText(), stateField.getText(), countryField.getText()));
 				}
 				else{
 					theNewMeeting.setLocation(new Location(cityField.getText(), countryField.getText()));
 				}
 				//Add chairs
 				ScholarMap chairs = new ScholarMap();
 				Object[] chairArr = chairList.getSelectedValuesList().toArray();
 				for(int i = 0; i < chairArr.length; i++){
 					chairs.put(((Scholar)chairArr[i]).getName(), (Scholar)chairArr[i]);
 				}
 				theNewMeeting.setChairs(chairs);
 				//Add committee members
 				ScholarMap members = new ScholarMap();
 				Object[] memberArr = committeeList.getSelectedValuesList().toArray();
 				for(int i = 0; i < memberArr.length; i++){
 					members.put(((Scholar)memberArr[i]).getName(), (Scholar)memberArr[i]);
 				}
 				theNewMeeting.setCommitteeMembers(members);
 				//Add papers
 				PaperMap papers = new PaperMap();
 				Object[] paperArr = paperList.getSelectedValuesList().toArray();
 				for(int i = 0; i < paperArr.length; i++){
 					papers.put(((Paper)paperArr[i]).getTitle(), (Paper)paperArr[i]);
 				}
 				theNewMeeting.setPapers(papers);
 
 				addMeetingDialog.dispatchEvent(new WindowEvent(addMeetingDialog, WindowEvent.WINDOW_CLOSING));
 			}
 		});
 		
 		addMeetingDialog.setTitle("Add Meeting");
 		addMeetingDialog.add(addMeetingBox);
 		addMeetingDialog.setSize(selectionView.getWidth() - 40, selectionView.getHeight() - 100);
 		addMeetingDialog.setLocationRelativeTo(selectionView);
 		addMeetingDialog.setModal(true);
 		addMeetingDialog.setVisible(true);
 
 		if(theNewMeeting.getLocation() == null){
 			return null;
 		}
 		return theNewMeeting;
 	}
 
 	/**
 	 * Opens a dialog that prompts the user for volume information
 	 * @return	A new Volume created using the user input
 	 */
 	private Volume openCreateVolumeDialog(){
 		JButton addIssueButton = new JButton("Add Issue");
 		JButton saveVolumeButton = new JButton("Create Volume");
 
 		final JPanel issuesPanel = new JPanel();
 		JPanel savePanel = new JPanel();
 
 		issuesPanel.add(addIssueButton);
 		savePanel.add(saveVolumeButton);
 
 		Box addVolumeBox = Box.createVerticalBox();
 		addVolumeBox.add(Box.createVerticalGlue());
 		addVolumeBox.add(issuesPanel);
 		addVolumeBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addVolumeBox.add(savePanel);
 		addVolumeBox.add(Box.createVerticalGlue());
 
 		final JDialog addVolumeDialog = new JDialog();
 		final Volume theNewVolume = new Volume();
 		
 		addIssueButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				Issue newIssue = openCreateIssueDialog();
 				if(newIssue != null){
 					theNewVolume.add(newIssue);
 					issuesPanel.add(new JLabel(newIssue.toString()), issuesPanel.getComponentCount() - 1);
 					addVolumeDialog.validate();
 				}
 			}
 		});
 		
 		saveVolumeButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				if(theNewVolume.size() == 0){
 					JOptionPane.showMessageDialog(addVolumeDialog, "Please add an issue", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				addVolumeDialog.dispatchEvent(new WindowEvent(addVolumeDialog, WindowEvent.WINDOW_CLOSING));
 			}
 		});
 		
 		addVolumeDialog.setTitle("Add Volume");
 		addVolumeDialog.add(addVolumeBox);
 		addVolumeDialog.setSize(selectionView.getWidth() - 40, selectionView.getHeight() - 100);
 		addVolumeDialog.setLocationRelativeTo(selectionView);
 		addVolumeDialog.setModal(true);
 		addVolumeDialog.setVisible(true);
 
 		if(theNewVolume.size() == 0){
 			return null;
 		}
 		return theNewVolume;
 	}
 
 	/**
 	 * Opens a dialog that prompts the user for scholar information
 	 * @return	A new Scholar created using the user input
 	 */
 	private Scholar openCreateScholarDialog(){
 		JLabel nameLabel = new JLabel("Name");
 		final JTextField nameField = new JTextField();
 		JLabel institutionLabel = new JLabel("Institution");
 		final ArrayList<JTextField> institutionFields = new ArrayList<JTextField>();
 		institutionFields.add(new JTextField());
 		JButton addInstitutionButton = new JButton("Add Institution");
 		JLabel researchAreaLabel = new JLabel("Research Area");
 		final ArrayList<JTextField> researchAreaFields = new ArrayList<JTextField>();
 		researchAreaFields.add(new JTextField());
 		JButton addResearchAreaButton = new JButton("Add Research Area");
 		JButton saveScholarButton = new JButton("Create Scholar");
 		
 		final Dimension textFieldDimension = new Dimension(160, 30);
 		nameField.setPreferredSize(textFieldDimension);
 		institutionFields.get(0).setPreferredSize(textFieldDimension);
 		researchAreaFields.get(0).setPreferredSize(textFieldDimension);
 		
 		JPanel namePanel = new JPanel();
 		final JPanel institutionPanel = new JPanel();
 		final JPanel researchAreaPanel = new JPanel();
 		JPanel savePanel = new JPanel();
 		
 		namePanel.add(nameLabel);
 		namePanel.add(nameField);
 		institutionPanel.add(institutionLabel);
 		institutionPanel.add(institutionFields.get(0));
 		institutionPanel.add(addInstitutionButton);
 		researchAreaPanel.add(researchAreaLabel);
 		researchAreaPanel.add(researchAreaFields.get(0));
 		researchAreaPanel.add(addResearchAreaButton);
 		savePanel.add(saveScholarButton);
 
 		Box addScholarBox = Box.createVerticalBox();
 		addScholarBox.add(Box.createVerticalGlue());
 		addScholarBox.add(namePanel);
 		addScholarBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addScholarBox.add(institutionPanel);
 		addScholarBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addScholarBox.add(researchAreaPanel);
 		addScholarBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addScholarBox.add(savePanel);
 		addScholarBox.add(Box.createVerticalGlue());
 
 		final JDialog addScholarDialog = new JDialog();
 		final Scholar theNewScholar = new Scholar();
 
 		addInstitutionButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				institutionFields.add(new JTextField());
 				institutionFields.get(institutionFields.size() - 1).setPreferredSize(textFieldDimension);;
 				institutionPanel.add(institutionFields.get(institutionFields.size() - 1), institutionPanel.getComponentCount() - 1);
 				addScholarDialog.validate();
 			}
 		});
 
 		addResearchAreaButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				researchAreaFields.add(new JTextField());
 				researchAreaFields.get(researchAreaFields.size() - 1).setPreferredSize(textFieldDimension);;
 				researchAreaPanel.add(researchAreaFields.get(researchAreaFields.size() - 1), researchAreaPanel.getComponentCount() - 1);
 				addScholarDialog.validate();
 			}
 		});
 
 		saveScholarButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				//Add name
 				if(nameField.getText().length() == 0){
 					JOptionPane.showMessageDialog(addScholarDialog, "Please enter a name", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				else{
 					theNewScholar.setName(nameField.getText());
 				}
 				
 				//Add institution(s)
 				if(institutionFields.get(0).getText().length() == 0){
 					JOptionPane.showMessageDialog(addScholarDialog, "Please enter an institution", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				else{
 					for(int i = 0; i < institutionFields.size(); i++){
 						if(institutionFields.get(i).getText().length() != 0){
 							theNewScholar.addInstitutionalAffiliations(institutionFields.get(i).getText());
 						}
 					}
 				}
 				
 				//Add research area(s)
 				if(researchAreaFields.get(0).getText().length() == 0){
 					JOptionPane.showMessageDialog(addScholarDialog, "Please enter a research area", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				else{
 					for(int i = 0; i < researchAreaFields.size(); i++){
 						if(researchAreaFields.get(i).getText().length() != 0){
 							theNewScholar.addResearchArea(researchAreaFields.get(i).getText());
 						}
 					}
 				}
 				
 				addScholarDialog.dispatchEvent(new WindowEvent(addScholarDialog, WindowEvent.WINDOW_CLOSING));
 			}
 		});
 		
 		addScholarDialog.setTitle("Add Scholar");
 		addScholarDialog.add(addScholarBox);
 		addScholarDialog.setSize(selectionView.getWidth() - 40, selectionView.getHeight() - 100);
 		addScholarDialog.setLocationRelativeTo(selectionView);
 		addScholarDialog.setModal(true);
 		addScholarDialog.setVisible(true);
 
 		if(theNewScholar.getName() == null){
 			return null;
 		}
 		return theNewScholar;
 	}
 
 	/**
 	 * Opens a dialog that prompts the user for conference information
 	 * @return	A new Conference created using the user input
 	 */
 	private Conference openCreateConferenceDialog(){
 		JLabel organizationLabel = new JLabel("Organization");
 		final JTextField organizationField = new JTextField();
 		JButton addMeetingButton = new JButton("Add Meeting");
 		JButton saveConferenceButton = new JButton("Create Conference");
 		
 		final Dimension textFieldDimension = new Dimension(160, 30);
 		organizationField.setPreferredSize(textFieldDimension);
 		
 		JPanel organizationPanel = new JPanel();
 		final JPanel addMeetingPanel = new JPanel();
 		JPanel savePanel = new JPanel();
 		
 		organizationPanel.add(organizationLabel);
 		organizationPanel.add(organizationField);
 		addMeetingPanel.add(addMeetingButton);
 		savePanel.add(saveConferenceButton);
 
 		Box addConferenceBox = Box.createVerticalBox();
 		addConferenceBox.add(Box.createVerticalGlue());
 		addConferenceBox.add(organizationPanel);
 		addConferenceBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addConferenceBox.add(addMeetingPanel);
 		addConferenceBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addConferenceBox.add(savePanel);
 		addConferenceBox.add(Box.createVerticalGlue());
 
 		final JDialog addConferenceDialog = new JDialog();
 		
 		final ArrayList<Meeting> meetings = new ArrayList<Meeting>();
 		addMeetingButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				Meeting newMeeting = openCreateMeetingDialog();
 				if(newMeeting != null){
 					meetings.add(newMeeting);
 					addMeetingPanel.add(new JLabel(meetings.get(meetings.size() - 1).getLocation().toString()), addMeetingPanel.getComponentCount() - 1);
 					addConferenceDialog.validate();
 				}
 			}
 		});
 
 		final Conference theNewConference = new Conference();
 		saveConferenceButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				//Add name
 				if(organizationField.getText().length() == 0){
 					JOptionPane.showMessageDialog(addConferenceDialog, "Please enter an organization", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				else{
 					theNewConference.setOrganizationName(organizationField.getText());
 				}
 				
 				//Add meetings
 				if(meetings.size() == 0){
 					JOptionPane.showMessageDialog(addConferenceDialog, "Please enter a meeting", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				theNewConference.setMeetings(meetings);
 
 				addConferenceDialog.dispatchEvent(new WindowEvent(addConferenceDialog, WindowEvent.WINDOW_CLOSING));
 			}
 		});
 		
 		addConferenceDialog.setTitle("Add Conference");
 		addConferenceDialog.add(addConferenceBox);
 		addConferenceDialog.setSize(selectionView.getWidth() - 40, selectionView.getHeight() - 100);
 		addConferenceDialog.setLocationRelativeTo(selectionView);
 		addConferenceDialog.setModal(true);
 		addConferenceDialog.setVisible(true);
 
 		if(theNewConference.getOrganizationName() == null){
 			return null;
 		}
 		return theNewConference;
 	}
 
 	/**
 	 * Opens a dialog that prompts the user for journal information
 	 * @return	A new Journal created using the user input
 	 */
 	private Journal openCreateJournalDialog(){
 		JLabel organizationLabel = new JLabel("Organization");
 		final JTextField organizationField = new JTextField();
 		JLabel cityLabel = new JLabel("City");
 		final JTextField cityField = new JTextField();
 		JLabel stateLabel = new JLabel("State/Province");
 		final JTextField stateField = new JTextField();
 		JLabel countryLabel = new JLabel("Country");
 		final JTextField countryField = new JTextField();
 		JButton addVolumeButton = new JButton("Add Volume");
 		JButton saveJournalButton = new JButton("Create Journal");
 		
 		final Dimension textFieldDimension = new Dimension(160, 30);
 		organizationField.setPreferredSize(textFieldDimension);
 		cityField.setPreferredSize(textFieldDimension);
 		stateField.setPreferredSize(textFieldDimension);
 		countryField.setPreferredSize(textFieldDimension);
 
 		JPanel organizationPanel = new JPanel();
 		JPanel cityPanel = new JPanel();
 		JPanel statePanel = new JPanel();
 		JPanel countryPanel = new JPanel();
 		final JPanel addVolumePanel = new JPanel();
 		JPanel savePanel = new JPanel();
 
 		organizationPanel.add(organizationLabel);
 		organizationPanel.add(organizationField);
 		cityPanel.add(cityLabel);
 		cityPanel.add(cityField);
 		statePanel.add(stateLabel);
 		statePanel.add(stateField);
 		countryPanel.add(countryLabel);
 		countryPanel.add(countryField);
 		addVolumePanel.add(addVolumeButton);
 		savePanel.add(saveJournalButton);
 
 		Box addJournalBox = Box.createVerticalBox();
 		addJournalBox.add(Box.createVerticalGlue());
 		addJournalBox.add(organizationPanel);
 		addJournalBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addJournalBox.add(cityPanel);
 		addJournalBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addJournalBox.add(statePanel);
 		addJournalBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addJournalBox.add(countryPanel);
 		addJournalBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addJournalBox.add(addVolumePanel);
 		addJournalBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addJournalBox.add(savePanel);
 		addJournalBox.add(Box.createVerticalGlue());
 
 		final JDialog addJournalDialog = new JDialog();
 		
 		final ArrayList<Volume> volumes = new ArrayList<Volume>();
 		addVolumeButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				Volume newVolume = openCreateVolumeDialog();
 				if(newVolume != null){
 					volumes.add(newVolume);
 					addVolumePanel.add(new JLabel(volumes.get(volumes.size() - 1).toString()), addVolumePanel.getComponentCount() - 1);
 					addJournalDialog.validate();
 				}
 			}
 		});
 
 		final Journal theNewJournal = new Journal();
 		saveJournalButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				//Add name
 				if(organizationField.getText().length() == 0){
 					JOptionPane.showMessageDialog(addJournalDialog, "Please enter an organization", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				else{
 					theNewJournal.setOrganizationName(organizationField.getText());
 				}
 				//Add location
 				if(cityField.getText().length() == 0){
 					JOptionPane.showMessageDialog(addJournalDialog, "Please enter a city", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				if(countryField.getText().length() == 0){
 					JOptionPane.showMessageDialog(addJournalDialog, "Please enter a country", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				if(stateField.getText().length() != 0){
 					theNewJournal.setLocation(new Location(cityField.getText(), stateField.getText(), countryField.getText()));
 				}
 				else{
 					theNewJournal.setLocation(new Location(cityField.getText(), countryField.getText()));
 				}
 				//Add volumes
 				if(volumes.size() == 0){
 					JOptionPane.showMessageDialog(addJournalDialog, "Please add a volume", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				theNewJournal.setVolumes(volumes);
 
 				addJournalDialog.dispatchEvent(new WindowEvent(addJournalDialog, WindowEvent.WINDOW_CLOSING));
 			}
 		});
 		
 		addJournalDialog.setTitle("Add Journal");
 		addJournalDialog.add(addJournalBox);
 		addJournalDialog.setSize(selectionView.getWidth() - 40, selectionView.getHeight() - 100);
 		addJournalDialog.setLocationRelativeTo(selectionView);
 		addJournalDialog.setModal(true);
 		addJournalDialog.setVisible(true);
 
 		if(theNewJournal.getOrganizationName() == null){
 			return null;
 		}
 		return theNewJournal;
 	}
 
 	/**
 	 * Opens a dialog that prompts the user for conference paper information
 	 * @return	A new ConferencePaper created using the user input
 	 */
 	private ConferencePaper openCreateConferencePaperDialog(){
 		JLabel titleLabel = new JLabel("Title");
 		final JTextField titleField = new JTextField();
 		JLabel pagesLabel = new JLabel("Pages");
 		final JTextField startPageField = new JTextField();
 		JLabel pagesHyphen = new JLabel("-");
 		final JTextField endPageField = new JTextField();
 		JLabel confLabel = new JLabel("Conference");
 		OutletList outletList = model.getOutletList();
 		ArrayList<Conference> conferences = new ArrayList<Conference>();
 		for(int i = 0; i < outletList.size(); i++){
 			if(outletList.get(i) instanceof Conference){
 				conferences.add((Conference)outletList.get(i));
 			}
 		}
 		if(conferences.size() == 0){
 			JOptionPane.showMessageDialog(null, "Must Add Conferences First!", "", JOptionPane.WARNING_MESSAGE);
 			return null;
 		}
		final JList<Conference> confList = new JList<Conference>((Conference[]) conferences.toArray());
 		confList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		JLabel scholarLabel = new JLabel("Authors");
		final JList<Scholar> scholarList = new JList<Scholar>((Scholar[]) model.getScholarMap().values().toArray());
 		JLabel diLabel = new JLabel("Digital Identifier (optional)");
 		final JTextField diField = new JTextField();
 		JButton saveConferencePaperButton = new JButton("Create Conference Paper");
 		
 		final Dimension textFieldDimension = new Dimension(160, 30);
 		titleField.setPreferredSize(textFieldDimension);
 		startPageField.setPreferredSize(textFieldDimension);
 		endPageField.setPreferredSize(textFieldDimension);
 		diField.setPreferredSize(textFieldDimension);
 
 		JPanel titlePanel = new JPanel();
 		JPanel pagesPanel = new JPanel();
 		JPanel confPanel = new JPanel();
 		JPanel scholarPanel = new JPanel();
 		JPanel diPanel = new JPanel();
 		JPanel savePanel = new JPanel();
 
 		titlePanel.add(titleLabel);
 		titlePanel.add(titleField);
 		pagesPanel.add(pagesLabel);
 		pagesPanel.add(startPageField);
 		pagesPanel.add(pagesHyphen);
 		pagesPanel.add(endPageField);
 		confPanel.add(confLabel);
 		confPanel.add(confList);
 		scholarPanel.add(scholarLabel);
 		scholarPanel.add(scholarList);
 		diPanel.add(diLabel);
 		diPanel.add(diField);
 		savePanel.add(saveConferencePaperButton);
 
 		Box addConferencePaperBox = Box.createVerticalBox();
 		addConferencePaperBox.add(Box.createVerticalGlue());
 		addConferencePaperBox.add(titlePanel);
 		addConferencePaperBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addConferencePaperBox.add(pagesPanel);
 		addConferencePaperBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addConferencePaperBox.add(confPanel);
 		addConferencePaperBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addConferencePaperBox.add(scholarPanel);
 		addConferencePaperBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addConferencePaperBox.add(diPanel);
 		addConferencePaperBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addConferencePaperBox.add(savePanel);
 		addConferencePaperBox.add(Box.createVerticalGlue());
 
 		final JDialog addConferencePaperDialog = new JDialog();
 		
 		final ConferencePaper theNewConferencePaper = new ConferencePaper();
 		saveConferencePaperButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				//Add title
 				if(titleField.getText().length() == 0){
 					JOptionPane.showMessageDialog(addConferencePaperDialog, "Please enter a title", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				else{
 					theNewConferencePaper.setTitle(titleField.getText());
 				}
 				//Add pages
 				if(startPageField.getText().length() == 0 || endPageField.getText().length() == 0){
 					JOptionPane.showMessageDialog(addConferencePaperDialog, "Please enter pages", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				else{
 					try{
 						theNewConferencePaper.setPageNumbers(new int[]{Integer.parseInt(startPageField.getText()), Integer.parseInt(endPageField.getText())});
 					}
 					catch(NumberFormatException ex){
 						JOptionPane.showMessageDialog(addConferencePaperDialog, "Invalid page numbers", "", JOptionPane.WARNING_MESSAGE);
 						return;
 					}
 				}
 				//Add conference
 				if(confList.getSelectedValue() == null){
 					JOptionPane.showMessageDialog(addConferencePaperDialog, "Please select a conference", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				theNewConferencePaper.setConference((Conference)confList.getSelectedValue());
 				//Add scholars
 				ScholarMap scholars = new ScholarMap();
 				Object[] scholarArr = scholarList.getSelectedValuesList().toArray();
 				for(int i = 0; i < scholarArr.length; i++){
 					scholars.put(((Scholar)scholarArr[i]).getName(), (Scholar)scholarArr[i]);
 				}
 				if(scholars.isEmpty()){
 					JOptionPane.showMessageDialog(addConferencePaperDialog, "Please add authors", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				theNewConferencePaper.setAuthors(scholars);
 				//Add digital object identifier
 				if(diField.getText().length() != 0){
 					theNewConferencePaper.setDigitalIdentifier(diField.getText());
 				}
 
 				addConferencePaperDialog.dispatchEvent(new WindowEvent(addConferencePaperDialog, WindowEvent.WINDOW_CLOSING));
 			}
 		});
 		
 		addConferencePaperDialog.setTitle("Add Conference Paper");
 		addConferencePaperDialog.add(addConferencePaperBox);
 		addConferencePaperDialog.setSize(selectionView.getWidth() - 40, selectionView.getHeight() - 100);
 		addConferencePaperDialog.setLocationRelativeTo(selectionView);
 		addConferencePaperDialog.setModal(true);
 		addConferencePaperDialog.setVisible(true);
 
 		if(theNewConferencePaper.getTitle() == null){
 			return null;
 		}
 		return theNewConferencePaper;
 	}
 
 	/**
 	 * Opens a dialog that prompts the user for journal article information
 	 * @return	A new JournalArticle created using the user input
 	 */
 	private JournalArticle openCreateJournalArticleDialog(){
 		JLabel titleLabel = new JLabel("Title");
 		final JTextField titleField = new JTextField();
 		JLabel pagesLabel = new JLabel("Pages");
 		final JTextField startPageField = new JTextField();
 		JLabel pagesHyphen = new JLabel("-");
 		final JTextField endPageField = new JTextField();
 		JLabel issueLabel = new JLabel("Issue");
 		OutletList outletList = model.getOutletList();
 		ArrayList<Journal> journals = new ArrayList<Journal>();
 		for(int i = 0; i < outletList.size(); i++){
 			if(outletList.get(i) instanceof Journal){
 				journals.add((Journal)outletList.get(i));
 			}
 		}
 		ArrayList<Issue> issues = new ArrayList<Issue>();
 		for(int i = 0; i < journals.size(); i++){
 			for(int j = 0; j < journals.get(i).getVolumes().size(); j++){
 				issues.addAll(journals.get(i).getVolumes().get(j));
 			}
 		}
 		if(issues.size() == 0){
 			JOptionPane.showMessageDialog(null, "Must Add Journal Issues First!", "", JOptionPane.WARNING_MESSAGE);
 			return null;
 		}
		final JList<Issue> issueList = new JList<Issue>((Issue[]) issues.toArray());
 		issueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		JLabel scholarLabel = new JLabel("Authors");
		final JList<Scholar> scholarList = new JList<Scholar>((Scholar[]) model.getScholarMap().values().toArray());
 		JLabel diLabel = new JLabel("Digital Identifier (optional)");
 		final JTextField diField = new JTextField();
 		JButton saveJournalArticleButton = new JButton("Create Journal Article");
 		
 		final Dimension textFieldDimension = new Dimension(160, 30);
 		titleField.setPreferredSize(textFieldDimension);
 		startPageField.setPreferredSize(textFieldDimension);
 		endPageField.setPreferredSize(textFieldDimension);
 		diField.setPreferredSize(textFieldDimension);
 
 		JPanel titlePanel = new JPanel();
 		JPanel pagesPanel = new JPanel();
 		JPanel issuePanel = new JPanel();
 		JPanel scholarPanel = new JPanel();
 		JPanel diPanel = new JPanel();
 		JPanel savePanel = new JPanel();
 
 		titlePanel.add(titleLabel);
 		titlePanel.add(titleField);
 		pagesPanel.add(pagesLabel);
 		pagesPanel.add(startPageField);
 		pagesPanel.add(pagesHyphen);
 		pagesPanel.add(endPageField);
 		issuePanel.add(issueLabel);
 		issuePanel.add(issueList);
 		scholarPanel.add(scholarLabel);
 		scholarPanel.add(scholarList);
 		diPanel.add(diLabel);
 		diPanel.add(diField);
 		savePanel.add(saveJournalArticleButton);
 
 		Box addJournalArticleBox = Box.createVerticalBox();
 		addJournalArticleBox.add(Box.createVerticalGlue());
 		addJournalArticleBox.add(titlePanel);
 		addJournalArticleBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addJournalArticleBox.add(pagesPanel);
 		addJournalArticleBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addJournalArticleBox.add(issuePanel);
 		addJournalArticleBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addJournalArticleBox.add(scholarPanel);
 		addJournalArticleBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addJournalArticleBox.add(diPanel);
 		addJournalArticleBox.add(Box.createVerticalStrut(dialogVerticalMargin));
 		addJournalArticleBox.add(savePanel);
 		addJournalArticleBox.add(Box.createVerticalGlue());
 
 		final JDialog addJournalArticleDialog = new JDialog();
 		
 		final JournalArticle theNewJournalArticle = new JournalArticle();
 		saveJournalArticleButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				//Add title
 				if(titleField.getText().length() == 0){
 					JOptionPane.showMessageDialog(addJournalArticleDialog, "Please enter a title", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				else{
 					theNewJournalArticle.setTitle(titleField.getText());
 				}
 				//Add pages
 				if(startPageField.getText().length() == 0 || endPageField.getText().length() == 0){
 					JOptionPane.showMessageDialog(addJournalArticleDialog, "Please enter pages", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				else{
 					try{
 						theNewJournalArticle.setPageNumbers(new int[]{Integer.parseInt(startPageField.getText()), Integer.parseInt(endPageField.getText())});
 					}
 					catch(NumberFormatException ex){
 						JOptionPane.showMessageDialog(addJournalArticleDialog, "Invalid page numbers", "", JOptionPane.WARNING_MESSAGE);
 						return;
 					}
 				}
 				//Add issue
 				if(issueList.getSelectedValue() == null){
 					JOptionPane.showMessageDialog(addJournalArticleDialog, "Please select an issue", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				theNewJournalArticle.setIssue((Issue)issueList.getSelectedValue());
 				//Add scholars
 				ScholarMap scholars = new ScholarMap();
 				Object[] scholarArr = scholarList.getSelectedValuesList().toArray();
 				for(int i = 0; i < scholarArr.length; i++){
 					scholars.put(((Scholar)scholarArr[i]).getName(), (Scholar)scholarArr[i]);
 				}
 				if(scholars.isEmpty()){
 					JOptionPane.showMessageDialog(addJournalArticleDialog, "Please add authors", "", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				theNewJournalArticle.setAuthors(scholars);
 				//Add digital object identifier
 				if(diField.getText().length() != 0){
 					theNewJournalArticle.setDigitalIdentifier(diField.getText());
 				}
 
 				addJournalArticleDialog.dispatchEvent(new WindowEvent(addJournalArticleDialog, WindowEvent.WINDOW_CLOSING));
 			}
 		});
 		
 		addJournalArticleDialog.setTitle("Add Journal Article");
 		addJournalArticleDialog.add(addJournalArticleBox);
 		addJournalArticleDialog.setSize(selectionView.getWidth() - 40, selectionView.getHeight() - 100);
 		addJournalArticleDialog.setLocationRelativeTo(selectionView);
 		addJournalArticleDialog.setModal(true);
 		addJournalArticleDialog.setVisible(true);
 
 		if(theNewJournalArticle.getTitle() == null){
 			return null;
 		}
 		return theNewJournalArticle;
 	}
 
 	/** The height of the vertical margins between panels in the information dialogs */
 	private final int dialogVerticalMargin = 5;
 	
 	private class AddScholarsListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			Scholar newScholar = openCreateScholarDialog();
 			if(newScholar != null){
 				model.addScholar(newScholar);
 			}
 		}
 	}
 	private class AddSerialsListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			//TODO: track openCreateDialiogs to make sure they can actually do stuff
 			if(model.getScholarMap().isEmpty()){
 				return;
 			}
 			int choice = JOptionPane.showOptionDialog(selectionView, "What type of serial would you like to create?", "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Journal","Conference"}, null);
 			if(choice == 0){
 				AcademicOutlet newOutlet = openCreateJournalDialog();
 				if(newOutlet != null){
 					model.addAcademicOutlet(newOutlet);
 				}
 			}
 			else if(choice == 1){
 				AcademicOutlet newOutlet = openCreateConferenceDialog();
 				if(newOutlet != null){
 					model.addAcademicOutlet(newOutlet);
 				}
 			}
 		}
 	}
 	private class AddPapersListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			if(model.getScholarMap().isEmpty() || model.getOutletList().isEmpty()){
 				return;
 			}
 			int choice = JOptionPane.showOptionDialog(selectionView, "What type of paper would you like to create?", "", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Journal Article","Conference Paper"}, null);
 			if(choice == 0){
 				boolean hasJournal=false;
 				for(AcademicOutlet a: model.getOutletList())
 				{
 					if(a instanceof Journal)
 						hasJournal=true;
 				}
 				if(hasJournal)
 				{
 					Paper newPaper = openCreateJournalArticleDialog();
 					if(newPaper != null){
 						model.addPaper(newPaper);
 					}
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(null, "You have no Journal to publish a paper. Please create a new Serial of type Journal.");
 				}
 			}
 			else if(choice == 1){
 				boolean hasJournal=false;
 				for(AcademicOutlet a: model.getOutletList())
 				{
 					if(a instanceof Conference)
 						hasJournal=true;
 				}
 				if(hasJournal)
 				{
 					Paper newPaper = openCreateConferencePaperDialog();
 					if(newPaper != null){
 						model.addPaper(newPaper);
 					}
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(null, "You have no Conference to publish a paper. Please create a new Serial of type Conference.");
 				}
 			}
 		}
 	}
 	private class RemoveScholarsListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			Object[] selectedObjects = selectionView.getScholarList().getSelectedValuesList().toArray();
 			Scholar[] scholarList = new Scholar[selectedObjects.length];
 			for(int i = 0; i < selectedObjects.length; i++){
 				scholarList[i] = (Scholar)selectedObjects[i];
 			}
 			model.removeScholars(scholarList);
 		}
 	}
 	private class RemoveSerialsListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			Object[] selectedObjects = selectionView.getSerialList().getSelectedValuesList().toArray();
 			AcademicOutlet[] outletList = new AcademicOutlet[selectedObjects.length];
 			for(int i = 0; i < selectedObjects.length; i++){
 				outletList[i] = (AcademicOutlet)selectedObjects[i];
 			}
 			model.removeAcademicOutlets(outletList);
 		}
 	}
 	private class RemovePapersListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			Object[] selectedObjects = selectionView.getPaperList().getSelectedValuesList().toArray();
 			Paper[] paperList = new Paper[selectedObjects.length];
 			for(int i = 0; i < selectedObjects.length; i++){
 				paperList[i] = (Paper)selectedObjects[i];
 			}
 			model.removePapers(paperList);
 		}
 	}
 	private class RemoveAllScholarsListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			model.removeAllScholars();
 		}
 	}
 	private class RemoveAllSerialsListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			model.removeAllAcademicOutlets();
 		}
 	}
 	private class RemoveAllPapersListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			model.removeAllPapers();
 		}
 	}
 
 	private class SaveListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			JFileChooser chooser = new JFileChooser();
 			FileNameExtensionFilter filter = new FileNameExtensionFilter("Scholarly System Files", "schsys");
 			chooser.setAcceptAllFileFilterUsed(false);
 			chooser.setFileFilter(filter);
 			if(chooser.showSaveDialog(selectionView) == JFileChooser.APPROVE_OPTION){
 				File file = chooser.getSelectedFile();
 				if(file.getName().lastIndexOf('.') == -1){
 					file = new File(file.toString() + ".schsys");
 				}
 				try{
 					ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
 					writer.writeObject(model.getSystem());
 					writer.close();
 				}
 				catch(IOException ex){
 					JOptionPane.showMessageDialog(selectionView, "Error", "Error", JOptionPane.ERROR_MESSAGE);
 					ex.printStackTrace();
 				}
 			}
 		}
 	}
 	private class LoadListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			if(!model.getScholarMap().isEmpty())
 			{
 				int choice = JOptionPane.showConfirmDialog(null, "WARNING! You may have unsaved data you are about to overwrite. Would you like to save it before loading the new data?");
 				if(choice==JOptionPane.YES_OPTION)
 					selectionView.getSaveMenuItem().doClick();
 				else if(choice==JOptionPane.CANCEL_OPTION)
 					return;
 				else if(choice==JOptionPane.CLOSED_OPTION)
 					return;
 			}
 			JFileChooser chooser = new JFileChooser();
 			FileNameExtensionFilter filter = new FileNameExtensionFilter("Scholarly System Files", "schsys");
 			chooser.setFileFilter(filter);
 			if(chooser.showOpenDialog(selectionView) == JFileChooser.APPROVE_OPTION){
 				File file = chooser.getSelectedFile();
 				try{
 					ObjectInputStream reader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
 					ScholarlySystem system = (ScholarlySystem)reader.readObject();
 					model.removeAllScholars();
 					ArrayList<Paper> papers = new ArrayList<Paper>(system.getPaperMap().values());
 					for(int i = 0; i < papers.size(); i++){
 						model.addPaper(papers.get(i));
 					}
 					ArrayList<Scholar> scholars = new ArrayList<Scholar>(system.getScholarMap().values());
 					for(int i = 0; i < scholars.size(); i++){
 						model.addScholar(scholars.get(i));
 					}
 					for(int i = 0; i < system.getOutletList().size(); i++){
 						model.addAcademicOutlet(system.getOutletList().get(i));
 					}
 					reader.close();
 				}
 				catch(IOException ex){
 					if(ex instanceof FileNotFoundException){
 						JOptionPane.showMessageDialog(selectionView, "File not found", "", JOptionPane.WARNING_MESSAGE);
 						actionPerformed(e);
 					}
 				}
 				catch(ClassNotFoundException ex){
 					JOptionPane.showMessageDialog(selectionView, "Internal Error", "", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		}
 	}
 	
 
 	private class InformationListener implements ActionListener {
 		public void actionPerformed(ActionEvent e)
 		{
 			JDialog displayDialog = new JDialog();
 			displayDialog.add(informationView, BorderLayout.CENTER);
 			displayDialog.setSize(selectionView.getWidth(), selectionView.getHeight());
 			displayDialog.setLocationRelativeTo(selectionView);
 			displayDialog.setVisible(true);
 		}
 	}
 	
 	/**
 	 * Listens for right clicks on the list
 	 *
 	 */
 	private class ListClickListener extends MouseAdapter{
 		public void mousePressed(final MouseEvent e){
 			if(e.isPopupTrigger()){
 				final int verticalMargin = 10;
 				JPopupMenu infoMenu = new JPopupMenu();
 				JMenuItem infoItem = new JMenuItem("Get Info");
 				JMenuItem deleteSelected = new JMenuItem("Delete");
 				infoMenu.add(infoItem);
 				infoMenu.add(deleteSelected);
 				if(e.getSource() == selectionView.getScholarList()){
 					selectionView.getScholarList().setSelectedIndex(selectionView.getScholarList().locationToIndex(e.getPoint()));
 					infoMenu.show(selectionView.getScholarList(), e.getX(), e.getY());
 					infoItem.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent event){
 							Scholar scholar = (Scholar)selectionView.getScholarList().getModel().getElementAt(selectionView.getScholarList().locationToIndex(e.getPoint()));
 							String nameText = "<html><div style='text-align:center;'><h3>Name</h3>" + scholar.getName() + "</div></html>";
 							String institutionText = "<html><div style='text-align:center;'><h3>Institutional Affiliations</h3>";
 							String researchAreaText = "<html><div style='text-align:center;'><h3>Research Areas</h3>";
 							String conferencePaperText = "<html><div style='text-align:center;'><h3>Conference Papers</h3>";
 							String journalArticleText = "<html><div style='text-align:center;'><h3>Journal Articles</h3>";
 							String chairText = "<html><div style='text-align:center;'><h3>Committees Chaired</h3>";
 							String committeeText = "<html><div style='text-align:center;'><h3>Committees</h3>";
 							String editorText = "<html><div style='text-align:center;'><h3>Editor Positions</h3>";
 							String reviewerText = "<html><div style='text-align:center;'><h3>Reviewer Positions</h3>";
 
 							for(int i = 0; i < scholar.getInstitutionalAffiliations().size(); i++){
 								institutionText += scholar.getInstitutionalAffiliations().get(i) + "<br />";
 							}
 							institutionText = institutionText.substring(0, institutionText.length() - 6) + "</div></html>";
 							for(int i = 0; i < scholar.getResearchAreas().size(); i++){
 								researchAreaText += scholar.getResearchAreas().get(i) + "<br />";
 							}
 							researchAreaText = researchAreaText.substring(0, researchAreaText.length() - 6) + "</div></html>";
 							for(int i = 0; i < scholar.getConferencePapers().size(); i++){
 								conferencePaperText += scholar.getConferencePapers().get(i) + "<br />";
 							}
 							conferencePaperText = conferencePaperText.substring(0, conferencePaperText.length() - 6) + "</div></html>";
 							for(int i = 0; i < scholar.getJournalArticles().size(); i++){
 								journalArticleText += scholar.getJournalArticles().get(i) + "<br />";
 							}
 							journalArticleText = journalArticleText.substring(0, journalArticleText.length() - 6) + "</div></html>";
 							for(int i = 0; i < scholar.getChairs().size(); i++){
 								chairText += scholar.getChairs().get(i) + "<br />";
 							}
 							chairText = chairText.substring(0, chairText.length() - 6) + "</div></html>";
 							for(int i = 0; i < scholar.getCommittees().size(); i++){
 								committeeText += scholar.getCommittees().get(i) + "<br />";
 							}
 							committeeText = committeeText.substring(0, committeeText.length() - 6) + "</div></html>";
 							for(int i = 0; i < scholar.getEditingPositions().size(); i++){
 								editorText += scholar.getEditingPositions().get(i) + "<br />";
 							}
 							editorText = editorText.substring(0, editorText.length() - 6) + "</div></html>";
 							for(int i = 0; i < scholar.getReviewingPositions().size(); i++){
 								reviewerText += scholar.getReviewingPositions().get(i) + "<br />";
 							}
 							reviewerText = reviewerText.substring(0, reviewerText.length() - 6) + "</div></html>";
 							
 							JLabel nameLabel = new JLabel(nameText);
 							JLabel institutionLabel = new JLabel(institutionText);
 							JLabel researchAreaLabel = new JLabel(researchAreaText);
 							JLabel conferencePaperLabel = new JLabel(conferencePaperText);
 							JLabel journalArticleLabel = new JLabel(journalArticleText);
 							JLabel chairLabel = new JLabel(chairText);
 							JLabel committeeLabel = new JLabel(committeeText);
 							JLabel editorLabel = new JLabel(editorText);
 							JLabel reviewerLabel = new JLabel(reviewerText);
 							nameLabel.setAlignmentX(JLabel.CENTER);
 							institutionLabel.setAlignmentX(JLabel.CENTER);
 							researchAreaLabel.setAlignmentX(JLabel.CENTER);
 							conferencePaperLabel.setAlignmentX(JLabel.CENTER);
 							journalArticleLabel.setAlignmentX(JLabel.CENTER);
 							chairLabel.setAlignmentX(JLabel.CENTER);
 							committeeLabel.setAlignmentX(JLabel.CENTER);
 							editorLabel.setAlignmentX(JLabel.CENTER);
 							reviewerLabel.setAlignmentX(JLabel.CENTER);
 
 							JPanel namePanel = new JPanel();
 							JPanel institutionPanel = new JPanel();
 							JPanel researchAreaPanel = new JPanel();
 							JPanel conferencePaperPanel = new JPanel();
 							JPanel journalArticlePanel = new JPanel();
 							JPanel chairPanel = new JPanel();
 							JPanel committeePanel = new JPanel();
 							JPanel editorPanel = new JPanel();
 							JPanel reviewerPanel = new JPanel();
 							
 							namePanel.add(nameLabel);
 							institutionPanel.add(institutionLabel);
 							researchAreaPanel.add(researchAreaLabel);
 							conferencePaperPanel.add(conferencePaperLabel);
 							journalArticlePanel.add(journalArticleLabel);
 							chairPanel.add(chairLabel);
 							committeePanel.add(committeeLabel);
 							editorPanel.add(editorLabel);
 							reviewerPanel.add(reviewerLabel);
 
 							Box infoBox = Box.createVerticalBox();
 							infoBox.add(Box.createVerticalGlue());
 							infoBox.add(namePanel);
 							if(scholar.getInstitutionalAffiliations().size() > 0){
 								infoBox.add(Box.createVerticalStrut(verticalMargin));
 								infoBox.add(institutionPanel);
 							}
 							if(scholar.getResearchAreas().size() > 0){
 								infoBox.add(Box.createVerticalStrut(verticalMargin));
 								infoBox.add(researchAreaPanel);
 							}
 							if(scholar.getConferencePapers().size() > 0){
 								infoBox.add(Box.createVerticalStrut(verticalMargin));
 								infoBox.add(conferencePaperPanel);
 							}
 							if(scholar.getJournalArticles().size() > 0){
 								infoBox.add(Box.createVerticalStrut(verticalMargin));
 								infoBox.add(journalArticlePanel);
 							}
 							if(scholar.getChairs().size() > 0){
 								infoBox.add(Box.createVerticalStrut(verticalMargin));
 								infoBox.add(chairPanel);
 							}
 							if(scholar.getCommittees().size() > 0){
 								infoBox.add(Box.createVerticalStrut(verticalMargin));
 								infoBox.add(committeePanel);
 							}
 							if(scholar.getEditingPositions().size() > 0){
 								infoBox.add(Box.createVerticalStrut(verticalMargin));
 								infoBox.add(editorPanel);
 							}
 							if(scholar.getReviewingPositions().size() > 0){
 								infoBox.add(Box.createVerticalStrut(verticalMargin));
 								infoBox.add(reviewerPanel);
 							}
 							infoBox.add(Box.createVerticalGlue());
 							infoBox.setPreferredSize(new Dimension(selectionView.getWidth() - 80, selectionView.getHeight() - 200));
 							JScrollPane scrollPane = new JScrollPane(infoBox);
 							scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 							JDialog infoDialog = new JDialog();
 							infoDialog.setSize(new Dimension(selectionView.getWidth() - 80, selectionView.getHeight() - 200));
 							infoDialog.add(scrollPane, BorderLayout.CENTER);
 							infoDialog.setLocationRelativeTo(selectionView);
 							infoDialog.setModal(true);
 							infoDialog.setVisible(true);
 						}
 					});
 					deleteSelected.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent e) {
 							selectionView.getRemoveScholarsButton().doClick();
 						}	
 					});
 				}
 				else if(e.getSource() == selectionView.getSerialList()){
 					selectionView.getSerialList().setSelectedIndex(selectionView.getSerialList().locationToIndex(e.getPoint()));
 					infoMenu.show(selectionView.getSerialList(), e.getX(), e.getY());
 					infoItem.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent event){
 							AcademicOutlet serial = (AcademicOutlet)selectionView.getSerialList().getModel().getElementAt(selectionView.getSerialList().locationToIndex(e.getPoint()));
 							Box infoBox = Box.createVerticalBox();
 							if(serial instanceof Journal){
 								Journal journal = (Journal)serial;
 								String organizationText = "<html><div style='text-align:center;'><h3>Organization</h3>" + journal.getOrganizationName() + "</div></html>";
 								String locationText = "<html><div style='text-align:center;'><h3>Location</h3>" + journal.getLocation() + "</div></html>";
 								String issuesText = "<html><div style='text-align:center;'><h3>Issues</h3>";
 
 								for(int i = 0; i < journal.getIssues().size(); i++){
 									issuesText += journal.getIssues().get(i) + "<br />";
 								}
 								issuesText = issuesText.substring(0, issuesText.length() - 6) + "</div></html>";
 								
 								JLabel organizationLabel = new JLabel(organizationText);
 								JLabel locationLabel = new JLabel(locationText);
 								JLabel issuesLabel = new JLabel(issuesText);
 								organizationLabel.setAlignmentX(JLabel.CENTER);
 								locationLabel.setAlignmentX(JLabel.CENTER);
 								issuesLabel.setAlignmentX(JLabel.CENTER);
 
 								JPanel organizationPanel = new JPanel();
 								JPanel locationPanel = new JPanel();
 								JPanel issuesPanel = new JPanel();
 								
 								organizationPanel.add(organizationLabel);
 								locationPanel.add(locationLabel);
 								issuesPanel.add(issuesLabel);
 
 								infoBox.add(Box.createVerticalGlue());
 								infoBox.add(organizationPanel);
 								infoBox.add(Box.createVerticalStrut(verticalMargin));
 								infoBox.add(locationPanel);
 								if(journal.getIssues().size() > 0){
 									infoBox.add(Box.createVerticalStrut(verticalMargin));
 									infoBox.add(issuesPanel);
 								}
 								infoBox.add(Box.createVerticalGlue());
 							}
 							if(serial instanceof Conference){
 								Conference conference = (Conference)serial;
 								String organizationText = "<html><div style='text-align:center;'><h3>Organization</h3>" + conference.getOrganizationName() + "</div></html>";
 								String meetingsText = "<html><div style='text-align:center;'><h3>Meetings</h3>";
 
 								for(int i = 0; i < conference.getMeetings().size(); i++){
 									meetingsText += conference.getMeetings().get(i) + "<br />";
 								}
 								meetingsText = meetingsText.substring(0, meetingsText.length() - 6) + "</div></html>";
 								
 								JLabel organizationLabel = new JLabel(organizationText);
 								JLabel meetingsLabel = new JLabel(meetingsText);
 								organizationLabel.setAlignmentX(JLabel.CENTER);
 								meetingsLabel.setAlignmentX(JLabel.CENTER);
 
 								JPanel organizationPanel = new JPanel();
 								JPanel meetingsPanel = new JPanel();
 								
 								organizationPanel.add(organizationLabel);
 								meetingsPanel.add(meetingsLabel);
 
 								infoBox.add(Box.createVerticalGlue());
 								infoBox.add(organizationPanel);
 								if(conference.getMeetings().size() > 0){
 									infoBox.add(Box.createVerticalStrut(verticalMargin));
 									infoBox.add(meetingsPanel);
 								}
 								infoBox.add(Box.createVerticalGlue());
 							}
 							
 							infoBox.setPreferredSize(new Dimension(selectionView.getWidth() - 80, selectionView.getHeight() - 200));
 							JScrollPane scrollPane = new JScrollPane(infoBox);
 							scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 							JDialog infoDialog = new JDialog();
 							infoDialog.setSize(new Dimension(selectionView.getWidth() - 80, selectionView.getHeight() - 200));
 							infoDialog.add(scrollPane, BorderLayout.CENTER);
 							infoDialog.setLocationRelativeTo(selectionView);
 							infoDialog.setModal(true);
 							infoDialog.setVisible(true);
 						}
 					});
 					deleteSelected.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent e) {
 							selectionView.getRemoveSerialsButton().doClick();
 						}	
 					});
 				}
 				else if(e.getSource() == selectionView.getPaperList()){
 					selectionView.getPaperList().setSelectedIndex(selectionView.getPaperList().locationToIndex(e.getPoint()));
 					infoMenu.show(selectionView.getPaperList(), e.getX(), e.getY());
 					infoItem.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent event){
 							Paper paper = (Paper)selectionView.getPaperList().getModel().getElementAt(selectionView.getPaperList().locationToIndex(e.getPoint()));
 							Box infoBox = Box.createVerticalBox();
 							if(paper instanceof JournalArticle){
 								JournalArticle journalArticle = (JournalArticle)paper;
 								String titleText = "<html><div style='text-align:center;'><h3>Title</h3>" + journalArticle.getTitle() + "</div></html>";
 								String pagesText = "no pages found";
 								if(journalArticle.getPageNumbers().length >= 2){
 									pagesText = "<html><div style='text-align:center;'><h3>Pages</h3>" + journalArticle.getPageNumbers()[0] + " - " + journalArticle.getPageNumbers()[1] + "</div></html>";
 								}
 								String authorsText = "<html><div style='text-align:center;'><h3>Authors</h3>";
 								String issueText = "<html><div style='text-align:center;'><h3>Journal Issue</h3>" + journalArticle.getIssue() + "</div></html>";
 								String diText = "<html><div style='text-align:center;'><h3>Digital Identifier</h3>" + journalArticle.getDigitalIdentifier() + "</div></html>";
 								
 								ArrayList<Scholar> authorList = new ArrayList<Scholar>(journalArticle.getAuthors().values());
 								for(int i = 0; i < authorList.size(); i++){
 									authorsText += authorList.get(i) + "<br />";
 								}
 								authorsText = authorsText.substring(0, authorsText.length() - 6) + "</div></html>";
 
 								JLabel titleLabel = new JLabel(titleText);
 								JLabel pagesLabel = new JLabel(pagesText);
 								JLabel authorsLabel = new JLabel(authorsText);
 								JLabel issueLabel = new JLabel(issueText);
 								JLabel diLabel = new JLabel(diText);
 								titleLabel.setAlignmentX(JLabel.CENTER);
 								pagesLabel.setAlignmentX(JLabel.CENTER);
 								authorsLabel.setAlignmentX(JLabel.CENTER);
 								issueLabel.setAlignmentX(JLabel.CENTER);
 								diLabel.setAlignmentX(JLabel.CENTER);
 
 								JPanel titlePanel = new JPanel();
 								JPanel pagesPanel = new JPanel();
 								JPanel authorsPanel = new JPanel();
 								JPanel issuePanel = new JPanel();
 								JPanel diPanel = new JPanel();
 
 								titlePanel.add(titleLabel);
 								pagesPanel.add(pagesLabel);
 								authorsPanel.add(authorsLabel);
 								issuePanel.add(issueLabel);
 								diPanel.add(diLabel);
 
 								infoBox.add(Box.createVerticalGlue());
 								infoBox.add(titlePanel);
 								infoBox.add(Box.createVerticalStrut(verticalMargin));
 								infoBox.add(pagesPanel);
 								if(authorList.size() > 0){
 									infoBox.add(Box.createVerticalStrut(verticalMargin));
 									infoBox.add(authorsPanel);
 								}
 								infoBox.add(Box.createVerticalStrut(verticalMargin));
 								infoBox.add(issuePanel);
 								if(!journalArticle.getDigitalIdentifier().equals("")){
 									infoBox.add(Box.createVerticalStrut(verticalMargin));
 									infoBox.add(diPanel);
 								}
 								infoBox.add(Box.createVerticalGlue());
 							}
 							if(paper instanceof ConferencePaper){
 								ConferencePaper conferencePaper = (ConferencePaper)paper;
 								String titleText = "<html><div style='text-align:center;'><h3>Title</h3>" + conferencePaper.getTitle() + "</div></html>";
 								String pagesText = "no pages found";
 								if(conferencePaper.getPageNumbers().length >= 2){
 									pagesText = "<html><div style='text-align:center;'><h3>Pages</h3>" + conferencePaper.getPageNumbers()[0] + " - " + conferencePaper.getPageNumbers()[1] + "</div></html>";
 								}
 								String authorsText = "<html><div style='text-align:center;'><h3>Authors</h3>";
 								String conferenceText = "<html><div style='text-align:center;'><h3>Conference</h3>" + conferencePaper.getConference() + "</div></html>";
 								String diText = "<html><div style='text-align:center;'><h3>Digital Identifier</h3>" + conferencePaper.getDigitalIdentifier() + "</div></html>";
 								
 								ArrayList<Scholar> authorList = new ArrayList<Scholar>(conferencePaper.getAuthors().values());
 								for(int i = 0; i < authorList.size(); i++){
 									authorsText += authorList.get(i) + "<br />";
 								}
 								authorsText = authorsText.substring(0, authorsText.length() - 6) + "</div></html>";
 
 								JLabel titleLabel = new JLabel(titleText);
 								JLabel pagesLabel = new JLabel(pagesText);
 								JLabel authorsLabel = new JLabel(authorsText);
 								JLabel conferenceLabel = new JLabel(conferenceText);
 								JLabel diLabel = new JLabel(diText);
 								titleLabel.setAlignmentX(JLabel.CENTER);
 								pagesLabel.setAlignmentX(JLabel.CENTER);
 								authorsLabel.setAlignmentX(JLabel.CENTER);
 								conferenceLabel.setAlignmentX(JLabel.CENTER);
 								diLabel.setAlignmentX(JLabel.CENTER);
 
 								JPanel titlePanel = new JPanel();
 								JPanel pagesPanel = new JPanel();
 								JPanel authorsPanel = new JPanel();
 								JPanel conferencePanel = new JPanel();
 								JPanel diPanel = new JPanel();
 
 								titlePanel.add(titleLabel);
 								pagesPanel.add(pagesLabel);
 								authorsPanel.add(authorsLabel);
 								conferencePanel.add(conferenceLabel);
 								diPanel.add(diLabel);
 
 								infoBox.add(Box.createVerticalGlue());
 								infoBox.add(titlePanel);
 								infoBox.add(Box.createVerticalStrut(verticalMargin));
 								infoBox.add(pagesPanel);
 								if(authorList.size() > 0){
 									infoBox.add(Box.createVerticalStrut(verticalMargin));
 									infoBox.add(authorsPanel);
 								}
 								infoBox.add(Box.createVerticalStrut(verticalMargin));
 								infoBox.add(conferencePanel);
 								if(!conferencePaper.getDigitalIdentifier().equals("")){
 									infoBox.add(Box.createVerticalStrut(verticalMargin));
 									infoBox.add(diPanel);
 								}
 								infoBox.add(Box.createVerticalGlue());
 							}
 							
 							infoBox.setPreferredSize(new Dimension(selectionView.getWidth() - 80, selectionView.getHeight() - 200));
 							JScrollPane scrollPane = new JScrollPane(infoBox);
 							scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 							JDialog infoDialog = new JDialog();
 							infoDialog.setSize(new Dimension(selectionView.getWidth() - 80, selectionView.getHeight() - 200));
 							infoDialog.add(scrollPane, BorderLayout.CENTER);
 							infoDialog.setLocationRelativeTo(selectionView);
 							infoDialog.setModal(true);
 							infoDialog.setVisible(true);
 						}
 					});
 					deleteSelected.addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent e) {
 							selectionView.getRemovePapersButton().doClick();
 						}	
 					});
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Listens for clicks to open up the display view
 	 */
 	private class DisplayViewListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			if(displayView == null || model.getScholarMap().isEmpty()){
 				return;
 			}
 			if(e.getSource() == selectionView.getTypeMenuItem()){
 				displayView.setGraphType(DisplayView.PUBLICATION_TYPE);
 			}
 			else if(e.getSource() == selectionView.getPubsPerYearMenuItem()){
 				displayView.setGraphType(DisplayView.PUBLICATIONS_PER_YEAR);
 			}
 			else if(e.getSource() == selectionView.getConfPapsPerYearMenuItem()){
 				displayView.setGraphType(DisplayView.CONFERENCE_PAPERS_PER_YEAR);
 			}
 			else if(e.getSource() == selectionView.getJourArtsPerYearMenuItem()){
 				displayView.setGraphType(DisplayView.JOURNAL_ARTICLES_PER_YEAR);
 			}
 			else if(e.getSource() == selectionView.getNumOfCoauthsMenuItem()){
 				displayView.setGraphType(DisplayView.NUMBER_OF_COAUTHORS);
 			}
 			JDialog displayDialog = new JDialog();
 			displayDialog.add(displayView, BorderLayout.CENTER);
 			displayDialog.setSize(selectionView.getWidth(), selectionView.getHeight());
 			displayDialog.setLocationRelativeTo(selectionView);
 			displayDialog.setModal(true);
 			displayDialog.setVisible(true);
 		}
 	}
 }
