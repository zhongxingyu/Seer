 package gui;
 
 import java.awt.*;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.List;
 import javax.swing.*;
 
 import ctrl.GUIController;
 
 import gui.custom.RoundPanel;
 
 /************************************************************************************
  * Name: PreferredInfo.java
  * 
  * Description: This panel is responsible for displaying questions relating to
  * the preferences of the student.
  * 
  ************************************************************************************/
 
 public class PreferredInfo extends RoundPanel implements GUI {
 
 	public PreferredInfo() {
 
 		/*********************************
 		 * Declare / Instantiate Variables
 		 *********************************/
 		/** Containers **/
 		Container container = new Container();
 		Container marriedContainer = new Container();
 		Container childrenContainer = new Container();
 		Container childrenCountContainer = new Container();
 		Container ccardsContainer = new Container();
 
 		/** Layouts **/
		BoxLayout lytContainer = new BoxLayout(container, 1);
 		FlowLayout lytMarriedContainer = new FlowLayout();
 		FlowLayout lytChildrenContainer = new FlowLayout();
 		FlowLayout lytChildrenCountContainer = new FlowLayout();
 		FlowLayout lytCCardsContainer = new FlowLayout();
 
 		/** Labels **/
 		JLabel lblJob = new JLabel("Future Occupation? ");
 		JLabel lblMarried = new JLabel("Will you be married? ");
 		JLabel lblChildren = new JLabel("Will you have children?");
 		JLabel lblChildrenCount = new JLabel("How many? ");
 		JLabel lblCCards = new JLabel("Will you have credit?");
 		JLabel lblCCardUse = new JLabel("What will you use them for? ");
 
 		/** Combo Boxes **/
 		// Get categories list from GUIController
 		List<String> lstCategories = GUIController.getInstance()
 				.getCategories();
 		// Create a new model using the list and set the combo box to that model
 		DefaultComboBoxModel modCategories = new DefaultComboBoxModel(
 				lstCategories.toArray());
 		final JComboBox cboJobCategory = new JComboBox(modCategories);
 
 		// Get jobs list from GUIController using current category
 		List<String> lstJobs = GUIController.getInstance().getJobsForCategory(
 				lstCategories.get(0));
 		// Create a new model using the list and set the combo box to that model
 		DefaultComboBoxModel modJobs = new DefaultComboBoxModel(
 				lstJobs.toArray());
 		final JComboBox cboJob = new JComboBox(modJobs);
 
 		JComboBox cboChildrenCount = new JComboBox(new String[] { "1", "2",
 				"3", "4", "5" });
 		JComboBox cboCCardUse = new JComboBox(new String[] { "Emergencies",
 				"Non-Emergency", "---", "---", "Other" });
 
 		/** Radio Buttons and Groups **/
 		ButtonGroup bgMarried = new ButtonGroup();
 		JRadioButton btnMarriedYes = new JRadioButton("Yes");
 		JRadioButton btnMarriedNo = new JRadioButton("No");
 
 		ButtonGroup bgChildren = new ButtonGroup();
 		JRadioButton btnChildrenYes = new JRadioButton("Yes");
 		JRadioButton btnChildrenNo = new JRadioButton("No");
 
 		ButtonGroup bgCCards = new ButtonGroup();
 		JRadioButton btnCCardsYes = new JRadioButton("Yes");
 		JRadioButton btnCCardsNo = new JRadioButton("No");
 
 		/*********************************
 		 * Configure Panel Properties
 		 *********************************/
 		this.setBackground(PANEL_BACKGROUND);
 
 		/*********************************
 		 * Configure Components
 		 *********************************/
 		/** Containers **/
 		container.setLayout(lytContainer);
 
 		marriedContainer.setLayout(lytMarriedContainer);
 		childrenContainer.setLayout(lytChildrenContainer);
 		childrenCountContainer.setLayout(lytChildrenCountContainer);
 		ccardsContainer.setLayout(lytCCardsContainer);
 
 		/** Combo Boxes **/
 		cboJobCategory.setSize(150, 20);
 		cboJob.setSize(150, 20);
 
 		/** Set Initial Indexes **/
 		cboJobCategory.setSelectedIndex(0);
 		cboJob.setSelectedIndex(0);
 
 		/** Radio Buttons **/
 		bgMarried.add(btnMarriedYes);
 		bgMarried.add(btnMarriedNo);
 		bgChildren.add(btnChildrenYes);
 		bgChildren.add(btnChildrenNo);
 		bgCCards.add(btnCCardsYes);
 		bgCCards.add(btnCCardsNo);
 
 		/*********************************
 		 * Assemble Containers and Panel
 		 *********************************/
 		/** Married Container **/
 		marriedContainer.add(lblMarried);
 		marriedContainer.add(btnMarriedYes);
 		marriedContainer.add(btnMarriedNo);
 
 		/** Children Container **/
 		childrenContainer.add(lblChildren);
 		childrenContainer.add(btnChildrenYes);
 		childrenContainer.add(btnChildrenNo);
 
 		/** ChildrenCount Container **/
 		childrenCountContainer.add(lblChildrenCount);
 		childrenCountContainer.add(cboChildrenCount);
 
 		/** CreditCards Container **/
 		ccardsContainer.add(lblCCards);
 		ccardsContainer.add(btnCCardsYes);
 		ccardsContainer.add(btnCCardsNo);
 
 		/** Main Container **/
 
 		// First Row
 		container.add(lblJob);
 		container.add(cboJobCategory);
 		container.add(cboJob);
 
 		// Second Row
 		container.add(marriedContainer);
 		container.add(childrenContainer);
 		container.add(childrenCountContainer);
 
 		// Third Row
 		container.add(ccardsContainer);
 		container.add(lblCCardUse);
 		container.add(cboCCardUse);
 
 		/** Assemble Panel **/
 		this.add(container);
 
 		/*********************************
 		 * Listeners
 		 *********************************/
 
 		/** Combo Boxes **/
 		// Change Job Box when Category Box Changes
 		cboJobCategory.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent arg0) {
 				// Get Job Names from Controller
 				List<String> lstJobNames = GUIController.getInstance()
 						.getJobsForCategory(
 						// Cast Current Selected Category as String
 								(String) cboJobCategory.getSelectedItem());
 				// Create Model of Job Names
 				DefaultComboBoxModel model = new DefaultComboBoxModel(
 						lstJobNames.toArray());
 
 				// Populate Jobs ComboBox
 				cboJob.setModel(model);
 			}
 		});
 	}
 }
