 package gui2;
 
 import static gui2.Style.FILE_PANE_ICON_BUTTON_SIZE;
 import static gui2.Style.LEFT_PANEL_DIMENSION;
 import static gui2.Style.LOAD_BUTTON_X;
 import static gui2.Style.LOAD_BUTTON_Y;
 import static gui2.Style.LOAD_PETRINET_DISABLED_ICON;
 import static gui2.Style.LOAD_PETRINET_ICON;
 import static gui2.Style.LOAD_PETRINET_PRESSED_ICON;
 import static gui2.Style.NEW_BUTTON_X;
 import static gui2.Style.NEW_BUTTON_Y;
 import static gui2.Style.NEW_PETRINET_DISABLED_ICON;
 import static gui2.Style.NEW_PETRINET_ICON;
 import static gui2.Style.NEW_PETRINET_PRESSED_ICON;
 import static gui2.Style.PETRINET_FILE_PANE_BORDER;
 import static gui2.Style.SAVE_AS_BUTTON_X;
 import static gui2.Style.SAVE_AS_BUTTON_Y;
 import static gui2.Style.SAVE_AS_PETRINET_DISABLED_ICON;
 import static gui2.Style.SAVE_AS_PETRINET_ICON;
 import static gui2.Style.SAVE_AS_PETRINET_PRESSED_ICON;
 import static gui2.Style.SAVE_BUTTON_X;
 import static gui2.Style.SAVE_BUTTON_Y;
 import static gui2.Style.SAVE_PETRINET_DISABLED_ICON;
 import static gui2.Style.SAVE_PETRINET_ICON;
 import static gui2.Style.SAVE_PETRINET_PRESSED_ICON;
 import static gui2.Style.SOUTH_PANEL_HEIHT;
 import static gui2.Style.DELETE_BUTTON_X;
 import static gui2.Style.DELETE_BUTTON_Y;
 import static gui2.Style.DELETE_PETRINET_DISABLED_ICON;
 import static gui2.Style.DELETE_PETRINET_PRESSED_ICON;
 import static gui2.Style.DELETE_PETRINET;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import exceptions.EngineException;
 
 /**
  * Class for FilePanes (saving and creating petrinets and rules)<br/>
  * They are in one class as they are almost the same<br/>
  * There are two instances of filePane: petrinet file pane and rule file pane
  * */
 class FilePane {
 
 	private static final String FILE_EXTENSION = ".PNML";
 	private static final String FILE_EXTENSION_WITHOUT_DOT = "PNML";
 
 	/** Listener for button new petri net */
 	private static class NewPetrinetListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			FilePane.getPetrinetFilePane().insertData("Petrinetz");
 		}
 	}
 
 	private static class PetrinetListSelectionListener implements
 			ListSelectionListener {
 		@Override
 		public void valueChanged(ListSelectionEvent e) {
 			if (!e.getValueIsAdjusting()) {
 				Object selectedValue = ((JList) e.getSource())
 						.getSelectedValue();
 				if (selectedValue != null) {
 					int pId = FilePane.getPetrinetFilePane().getIdFrom(
 							selectedValue);
 					PetrinetPane.getInstance().displayPetrinet(pId,
 							"Petrinetz: " + selectedValue);
 				} else {
 					PetrinetPane.getInstance().displayEmpty();
 				}
 			}
 		}
 	}
 
 	private static class RuleListSelectionListener implements
 			ListSelectionListener {
 		@Override
 		public void valueChanged(ListSelectionEvent e) {
 			if (!e.getValueIsAdjusting()) {
 				Object selectedValue = ((JList) e.getSource())
 						.getSelectedValue();
 				if (selectedValue != null) {
 					int ruleId = FilePane.getRuleFilePane().getIdFrom(
 							selectedValue);
 					RulePane.getInstance().displayRule(ruleId);
 				} else {
 					RulePane.getInstance().displayEmpty();
 				}
 			}
 		}
 	}
 
 	/** Listener for button load petri net */
 	private static class LoadPetrinetListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			File file = FilePane.getPetrinetFilePane().choseFileForLoad(
 					"Petrinetz");
 			if (file != null) {
 				FilePane.getPetrinetFilePane().loadPetrinet(file);
 			}
 		}
 	}
 
 	/** Listener for button save petri net */
 	private static class SavePetrinetListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			File fileToSafe = FilePane.getPetrinetFilePane()
 					.existsFileForSelectedItem();
 			if (fileToSafe != null) {
 				FilePane.getPetrinetFilePane().savePetrinetOnFilesystem(
 						fileToSafe);
 			} else {
 				(new SaveAsPetrinetListener()).actionPerformed(e);
 			}
 		}
 	}
 
 	/** Listener for button save as petri net */
 	private static class SaveAsPetrinetListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			File filepath = FilePane.getPetrinetFilePane()
 					.choseFileForSaveAsAndRememberpath("Petrinetz");
 			if (filepath != null) {
 				FilePane.getPetrinetFilePane().savePetrinetOnFilesystem(
 						filepath);
 			}
 		}
 	}
 
 	/** Listener for button new rule */
 	private static class NewRuleListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			FilePane.getRuleFilePane().insertData("Regel");
 		}
 	}
 
 	/** Listener for button load rule */
 	private static class LoadRuleListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			File file = FilePane.getRuleFilePane().choseFileForLoad("Regel");
 			if (file != null) {
 				FilePane.getRuleFilePane().loadRule(file);
 			}
 		}
 	}
 
 	/** Listener for button save rule */
 	private static class SaveRuleListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			File fileToSafe = FilePane.getRuleFilePane()
 					.existsFileForSelectedItem();
 			if (fileToSafe != null) {
 				FilePane.getRuleFilePane().saveRuleOnFilesystem(fileToSafe);
 			} else {
 				(new SaveAsRuleListener()).actionPerformed(e);
 			}
 		}
 	}
 
 	/** Listener for button save rule as */
 	private static class SaveAsRuleListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			File filepath = FilePane.getRuleFilePane()
 					.choseFileForSaveAsAndRememberpath("Regel");
 			if (filepath != null) {
 				FilePane.getRuleFilePane().saveRuleOnFilesystem(filepath);
 			}
 		}
 	}
 
 	/** Listener for button delete rule */
 	private static class DeleteRuleListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			int id = FilePane.getRuleFilePane().deleteSelectedItem();
 			if (id != -1) {
 				RulePane.getInstance().displayEmpty();
 				try {
 					EngineAdapter.getRuleManipulation().closeRule(id);
 				} catch (EngineException e1) {
 					PopUp.popError(e1);
 					e1.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/** Listener for button delete petrinet */
 	private static class DeletePetrinetListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			int id = FilePane.getPetrinetFilePane().deleteSelectedItem();
 			if (id != -1) {
 				PetrinetPane.getInstance().displayEmpty();
 				try {
 					EngineAdapter.getPetrinetManipulation().closePetrinet(id);
 				} catch (EngineException e1) {
 					PopUp.popError(e1);
 					e1.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/** One of the instances. This instance is the file pane for petrinets */
 	private static FilePane petrinetFilePane;
 
 	/** One of the instances. This instance is the file pane for rules */
 	private static FilePane ruleFilePane;
 
 	/*
 	 * Initiating the two "singleton" instances (singleton because there can
 	 * only be one petrinet file pane and only one rule file pane) The only
 	 * differences are the descriptions and the listeners (look at constructors)
 	 */
 	static {
 		petrinetFilePane = new FilePane("Petrinetz", "Petrinetze",
 				new NewPetrinetListener(), new LoadPetrinetListener(),
 				new SavePetrinetListener(), new SaveAsPetrinetListener(),
 				new DeletePetrinetListener(),
 				new PetrinetListSelectionListener());
 
 		ruleFilePane = new FilePane("Regel", "Regeln", new NewRuleListener(),
 				new LoadRuleListener(), new SaveRuleListener(),
 				new SaveAsRuleListener(), new DeleteRuleListener(),
 				new RuleListSelectionListener());
 	}
 
 	/** Retruns the only instance of a petrinet file panel */
 	public static FilePane getPetrinetFilePane() {
 		return petrinetFilePane;
 	}
 
 	/** Returns the only instance of a rule file panel */
 	public static FilePane getRuleFilePane() {
 		return ruleFilePane;
 	}
 
 	/** Top level JPanel for layouting. This is added into the main frame */
 	private JPanel treeAndButtonContainerWithBorder;
 
 	/**
 	 * Sub level JPanel for layouting buttons. This is added into top level
 	 * JPanel
 	 */
 	private JPanel buttonContainer;
 
 	/** The List containing all loaded files */
 	private JList list;
 
 	/** The button for creating a new petrinet/rule */
 	private JButton newButton;
 
 	/** The button for saving a petrinet/rule */
 	private JButton saveButton;
 
 	/** The button for loading a petrinet/rule */
 	private JButton loadButton;
 
 	/** The Button for deleting a petrinet/rule */
 	private JButton deleteButton;
 
 	/** The button for saving a petrinet/rule in a certain file */
 	private JButton saveAsButton;
 
 	/** Model for list */
 	private DefaultListModel listModel;
 
 	/** To remember what list entry refers to wich petrinet */
 	private Map<Object, Integer> listItemToPId;
 
 	/** To remember waht list entry refers to which filepath */
 	private Map<Integer, File> listIdToFilepath;
 
 	// private DefaultMutableTreeNode root;
 
 	/** No default instances */
 	private FilePane() {
 	};
 
 	/**
 	 * Constructor that sets all the instance variables
 	 * 
 	 * @param type
 	 *            "Petrinetz" / "Regel"
 	 * @param typePlural
 	 *            "Petrinetze" / "Regeln"
 	 * @param newListener
 	 *            Listener for new button
 	 * @param loadListener
 	 *            Listener for load button
 	 * @param saveListener
 	 *            Listener for save button
 	 * @param saveAsListener
 	 *            Listener for save as button
 	 * @param deleteListener
 	 *            Listener for delete button
 	 */
 	private FilePane(String type, String typePlural,
 			ActionListener newListener, ActionListener loadListener,
 			ActionListener saveListener, ActionListener saveAsListener,
 			ActionListener deleteListener,
 			ListSelectionListener selectionListener) {
 		listItemToPId = initiateListItemToPid();
 		listIdToFilepath = iniateListItemToFilepath();
 
 		newButton = initiateNewButton(type, newListener);
 		saveButton = initiateSaveButton(type, saveListener);
 		loadButton = initiateLoadButton(type, loadListener);
 		saveAsButton = initiateSaveAsButton(type, saveAsListener);
 		deleteButton = initiateDeleteButton(type, deleteListener);
 		buttonContainer = initiateButtonContainer(newButton, saveButton,
 				loadButton, saveAsButton, deleteButton);
 		list = initiateList(selectionListener);
 		treeAndButtonContainerWithBorder = initiateTreeAndButtonContainerWithBorder(
 				list, buttonContainer, typePlural);
 	}
 
 	private Map<Object, Integer> initiateListItemToPid() {
 		return new HashMap<Object, Integer>();
 	}
 
 	private Map<Integer, File> iniateListItemToFilepath() {
 		return new HashMap<Integer, File>();
 	}
 
 	private JList initiateList(ListSelectionListener listener) {
 		listModel = new DefaultListModel();
 		JList list = new JList(listModel);
 		list.addListSelectionListener(listener);
 		return list;
 	}
 
 	/**
 	 * Initiates the top level JPanel for layouting
 	 * 
 	 * @param tree
 	 * @param buttonContainer
 	 * @param typePlural
 	 *            Descriptor: "Petrinetze" / "Regeln"
 	 * @return
 	 */
 	private JPanel initiateTreeAndButtonContainerWithBorder(JList list,
 			JPanel buttonContainer, String typePlural) {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout());
 		panel.setBorder(BorderFactory.createTitledBorder(
 				BorderFactory.createEtchedBorder(), "Geladene " + typePlural));
 
 		panel.add(new JScrollPane(list), BorderLayout.CENTER);
 		panel.add(buttonContainer, BorderLayout.SOUTH);
 
 		return panel;
 	}
 
 	/**
 	 * Initiates an internal JPanel thats used for layouting the buttons. Also
 	 * adds the buttons to the JPanel
 	 * 
 	 * @param newButton
 	 * @param saveButton
 	 * @param loadButton
 	 * @param saveAsButton
 	 * @return
 	 */
 	private JPanel initiateButtonContainer(JButton newButton,
 			JButton saveButton, JButton loadButton, JButton saveAsButton,
 			JButton deleteButton) {
 		JPanel panel = new JPanel();
 		panel.setLayout(null); // layouting with setbounds(...)
 		panel.setPreferredSize(new Dimension(LEFT_PANEL_DIMENSION.width,
 				SOUTH_PANEL_HEIHT));
 		panel.add(newButton);
 		panel.add(saveButton);
 		panel.add(loadButton);
 		panel.add(saveAsButton);
 		panel.add(deleteButton);
 
 		return panel;
 	}
 
 	/**
 	 * Initiates the save as button with size, icon, tooltip etc
 	 * 
 	 * @param type
 	 *            Descriptor: "Petrinetz" / "Regel"
 	 * @param saveAsListener
 	 *            Listener for button
 	 * @return
 	 */
 	private JButton initiateSaveAsButton(String type,
 			ActionListener saveAsListener) {
 		JButton button = new JButton(SAVE_AS_PETRINET_ICON);
 		button.setBounds(SAVE_AS_BUTTON_X, SAVE_AS_BUTTON_Y,
 				FILE_PANE_ICON_BUTTON_SIZE, FILE_PANE_ICON_BUTTON_SIZE);
 		button.setPressedIcon(SAVE_AS_PETRINET_PRESSED_ICON);
 		button.setDisabledIcon(SAVE_AS_PETRINET_DISABLED_ICON);
 
 		button.setToolTipText(type + " speichern unter...");
 		button.setRolloverEnabled(true);
 
 		button.addActionListener(saveAsListener);
 
 		return button;
 	}
 
 	/**
 	 * Initiates the delete button with size, icon, tooltip etc
 	 * 
 	 * @param type
 	 *            Descriptor: "Petrinetz" / "Regel"
 	 * @param deleteListener
 	 *            Listener for button
 	 * @return
 	 */
 	private JButton initiateDeleteButton(String type,
 			ActionListener deleteListener) {
 		JButton button = new JButton(DELETE_PETRINET);
 		button.setBounds(DELETE_BUTTON_X, DELETE_BUTTON_Y,
 				FILE_PANE_ICON_BUTTON_SIZE, FILE_PANE_ICON_BUTTON_SIZE);
 		button.setPressedIcon(DELETE_PETRINET_PRESSED_ICON);
 		button.setDisabledIcon(DELETE_PETRINET_DISABLED_ICON);
 
 		button.setToolTipText(type
 				+ " aus der Liste entfernen. Wird nicht aus Filesystem gelöscht.");
 		button.setRolloverEnabled(true);
 
 		button.addActionListener(deleteListener);
 
 		return button;
 	}
 
 	/**
 	 * Initiates the load button with size, icon, tooltip etc
 	 * 
 	 * @param type
 	 *            Descriptor: "Petrinetz" / "Regel"
 	 * @param loadListener
 	 *            Listener for button
 	 * @return
 	 */
 	private JButton initiateLoadButton(String type, ActionListener loadListener) {
 		JButton button = new JButton(LOAD_PETRINET_ICON);
 		button.setBounds(LOAD_BUTTON_X, LOAD_BUTTON_Y,
 				FILE_PANE_ICON_BUTTON_SIZE, FILE_PANE_ICON_BUTTON_SIZE);
 		button.setPressedIcon(LOAD_PETRINET_PRESSED_ICON);
 		button.setDisabledIcon(LOAD_PETRINET_DISABLED_ICON);
 
 		button.setToolTipText(type + " aus Datei laden");
 		button.setRolloverEnabled(true);
 
 		button.addActionListener(loadListener);
 
 		return button;
 	}
 
 	/**
 	 * Initiates the save button with size, icon, tooltip etc
 	 * 
 	 * @param type
 	 *            Descriptor: "Petrinetz" / "Regel"
 	 * @param saveListener
 	 *            Listener for button
 	 * @return
 	 */
 	private JButton initiateSaveButton(String type, ActionListener saveListener) {
 		JButton button = new JButton(SAVE_PETRINET_ICON);
 		button.setBounds(SAVE_BUTTON_X, SAVE_BUTTON_Y,
 				FILE_PANE_ICON_BUTTON_SIZE, FILE_PANE_ICON_BUTTON_SIZE);
 
 		button.setPressedIcon(SAVE_PETRINET_PRESSED_ICON);
 		button.setDisabledIcon(SAVE_PETRINET_DISABLED_ICON);
 		button.setToolTipText(type + " speichern");
 
 		button.setRolloverEnabled(true);
 
 		if (type == "Petrinetz") {
 			button.setMnemonic(KeyEvent.VK_S);
 		}
 		button.addActionListener(saveListener);
 
 		return button;
 	}
 
 	/**
 	 * Initiates the new button with size, icon, tooltip etc
 	 * 
 	 * @param type
 	 *            Descriptor: "Petrinetz" / "Regel"
 	 * @param newListener
 	 *            Listener for button
 	 * @return
 	 */
 	private JButton initiateNewButton(String type, ActionListener newListener) {
 		JButton button = new JButton(NEW_PETRINET_ICON);
 		button.setBounds(NEW_BUTTON_X, NEW_BUTTON_Y,
 				FILE_PANE_ICON_BUTTON_SIZE, FILE_PANE_ICON_BUTTON_SIZE);
 		button.setPressedIcon(NEW_PETRINET_PRESSED_ICON);
 		button.setDisabledIcon(NEW_PETRINET_DISABLED_ICON);
 
 		button.setToolTipText(type + " erstellen");
 		button.setRolloverEnabled(true);
 
 		button.addActionListener(newListener);
 
 		return button;
 	}
 
 	/** Adds the internal JPanel into the given JPanel "frame" */
 	public void addTo(JPanel frame) {
 		frame.add(treeAndButtonContainerWithBorder);
 	}
 
 	int getIdFrom(Object listItem) {
 		Integer integer = listItemToPId.get(listItem);
 		if (integer != null) {
 			return integer;
 		} else {
 			return -1;
 		}
 	}
 
 	/** disable hole buttons and tree */
 	void setHoleButtonsDisable() {
 		loadButton.setEnabled(false);
 		newButton.setEnabled(false);
 		saveAsButton.setEnabled(false);
 		saveButton.setEnabled(false);
 		deleteButton.setEnabled(false);
 		list.setEnabled(false);
 	}
 
 	/** enable hole buttons and tree */
 	void setHoleButtonsEnable() {
 		loadButton.setEnabled(true);
 		newButton.setEnabled(true);
 		saveAsButton.setEnabled(true);
 		saveButton.setEnabled(true);
 		deleteButton.setEnabled(true);
 		list.setEnabled(true);
 	}
 
 	/**
 	 * write user data into list
 	 * 
 	 * @param type
 	 *            of panel
 	 * @return name of the petrinet, if no data entered return <tt>null</tt>
 	 */
 	String insertData(String type) {
 		String input = JOptionPane.showInputDialog("Bitte Name für " + type
 				+ " eingeben.", "neue(s) " + type);
		while(listItemToPId.keySet().contains(input)){
			input = JOptionPane.showInputDialog("Name für " + type
					+ " bereits vergeben. Bitte geben sie einen anderen ein.", "neue(s) " + type);
		}
 		if (input != null) {
 			// Add item to list without listeners! they acticate on
 			// inserting thus leading to inconsistent states: nullpointers
 			ListSelectionListener petrinetListSelectionListener = list
 					.getListSelectionListeners()[0];
 			list.removeListSelectionListener(petrinetListSelectionListener);
 
 			listModel.add(0, input);
 
 			// After inserting add the listener again
 			list.addListSelectionListener(petrinetListSelectionListener);
 
 			int id;
 			if (this == FilePane.getPetrinetFilePane()) {
 				id = EngineAdapter.getPetrinetManipulation().createPetrinet();
 			} else {
 				id = EngineAdapter.getRuleManipulation().createRule();
 			}
 			listItemToPId.put(input, id);
 			list.setSelectedIndex(0);
 			return input;
 		}
 		return null;
 	}
 
 	/**
 	 * Returns true if display should stay same, false if display shall be empty
 	 */
 	int deleteSelectedItem() {
 		Object selectedValue = list.getSelectedValue();
 		int id = getIdFromSelectedItem();
 		if (selectedValue != null) {
 			int confirmDialog = JOptionPane.showConfirmDialog(
 					treeAndButtonContainerWithBorder, "Wollen sie "
 							+ selectedValue + " wirklich löschen?",
 					"Petrinetz löschen?", JOptionPane.YES_NO_OPTION);
 			if (confirmDialog == 0) {
 				// Remove item from list without listeners! they acticate on
 				// deleting thus leading to inconsistent states: nullpointers
 				ListSelectionListener petrinetListSelectionListener = list
 						.getListSelectionListeners()[0];
 				list.removeListSelectionListener(petrinetListSelectionListener);
 
 				boolean success = listModel.removeElement(selectedValue)
 						&& listItemToPId.remove(selectedValue) != null;
 
 				// After removing add the listener again
 				list.addListSelectionListener(petrinetListSelectionListener);
 				if (success) {
 					return id;
 				} else {
 					return -1;
 				}
 			} else {
 				return -1;
 			}
 		} else {
 			return -1;
 		}
 	}
 
 	/**
 	 * Open filechooser and do all the save as logic
 	 * 
 	 * @param type
 	 *            type of the Saveing Object. (Petrinet or Rule)
 	 * @return File with the Filepath
 	 */
 	public File choseFileForSaveAsAndRememberpath(String type) {
 		if (!isItemSelected()) {
 			JOptionPane
 					.showMessageDialog(
 							null,
 							"Keine Auswahl in der Liste getroffen. \n Bitte eine Auswahl in der Liste vornehmen.",
 							"keine Auswahl", JOptionPane.INFORMATION_MESSAGE);
 		} else {
 			final JFileChooser fileChooser = new JFileChooser();
 			fileChooser.setDialogTitle(type + " speichern unter...");
 			int returnVal = fileChooser.showSaveDialog(fileChooser);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 
 				File file = null;
 				String path = fileChooser.getSelectedFile().getPath();
 
 				if (!path.toLowerCase().endsWith(FILE_EXTENSION)) {
 					path = path + FILE_EXTENSION;
 				}
 				file = new File(path);
 				if (file.exists()) {
 					int confirmDialog = JOptionPane.showConfirmDialog(null,
 							"Wollen sie die Datei  \"" + path
 									+ "\" wirklich überspeichern?", type
 									+ " speichern", JOptionPane.YES_NO_OPTION,
 							JOptionPane.QUESTION_MESSAGE);
 					if (confirmDialog == 0) {
 						rememberFilePath(file);
 						return file;
 					} else {
 						return null;
 					}
 				} else {
 					rememberFilePath(file);
 					return file;
 				}
 			}
 		}
 		return null;
 	}
 
 	public File choseFileForLoad(String type) {
 		final JFileChooser fileChooser = new JFileChooser();
 		fileChooser.setDialogTitle(type + " laden");
 		int returnVal = fileChooser.showOpenDialog(fileChooser);
 
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 
 			File file = null;
 			String path = fileChooser.getSelectedFile().getPath();
 
 			if (!path.endsWith(FILE_EXTENSION)) {
 				JOptionPane.showMessageDialog(null, "Datei muss mit "
 						+ FILE_EXTENSION + " enden.",
 						"Warnung: Falsche Endung", JOptionPane.WARNING_MESSAGE);
 				return null;
 			}
 			file = new File(path);
 			if (file.exists()) {
 				return file;
 			} else {
 				JOptionPane.showMessageDialog(null, "Datei " + path
 						+ " existiert nicht.", "Warnung: Falscher Dateipfad",
 						JOptionPane.WARNING_MESSAGE);
 				return null;
 			}
 		}
 		return null;
 	}
 
 	public void savePetrinetOnFilesystem(File path) {
 		int id = getIdFromSelectedItem();
 		try {
 			EngineAdapter.getPetrinetManipulation().save(id, path.getParent(),
 					extractListEntryNameFromFilePath(path),
 					FILE_EXTENSION_WITHOUT_DOT);
 		} catch (EngineException e) {
 			PopUp.popError(e);
 			e.printStackTrace();
 		}
 	}
 
 	public void saveRuleOnFilesystem(File path) {
 		int id = getIdFromSelectedItem();
 		try {
 			EngineAdapter.getRuleManipulation().save(id, path.getParent(),
 					extractListEntryNameFromFilePath(path),
 					FILE_EXTENSION_WITHOUT_DOT);
 		} catch (EngineException e) {
 			PopUp.popError(e);
 			e.printStackTrace();
 		}
 		// PopUp.popUnderConstruction("SaveRuleOnFilesystem in Filepane");
 	}
 
 	public void loadPetrinet(File path) {
 		// EngineAdapter.getPetrinetManipulation().createPetrinet();
 		int petrinetId = EngineAdapter.getPetrinetManipulation().load(
 				path.getParent(), path.getName());
 
 		// Add item to list without listeners! they acticate on
 		// inserting thus leading to inconsistent states: nullpointers
 		ListSelectionListener petrinetListSelectionListener = list
 				.getListSelectionListeners()[0];
 		list.removeListSelectionListener(petrinetListSelectionListener);
 
 		String listItem = extractListEntryNameFromFilePath(path);
 		listModel.add(0, listItem);
 
 		// After inserting add the listener again
 		list.addListSelectionListener(petrinetListSelectionListener);
 
 		listItemToPId.put(listItem, petrinetId);
 		listIdToFilepath.put(petrinetId, path);
 	}
 
 	String extractListEntryNameFromFilePath(File path) {
		String initial = path.getName().split(FILE_EXTENSION)[0];
		String number = "";
		while(listItemToPId.keySet().contains(initial+number)){
			if (number.equals("")) {
				number = "(1)";
			} else {
				number = "("+(Integer.parseInt(String.valueOf(number.charAt(1)))+1)+")";
			}
		}
		return initial+number;
 	}
 
 	public void loadRule(File path) {
 		PopUp.popUnderConstruction("LoadRule in Filepane");
 		// TODO: Remember Id and Filepath in Map
 	}
 
 	public File existsFileForSelectedItem() {
 		int id = getIdFromSelectedItem();
 		File file = listIdToFilepath.get(id);
 		if (file != null) {
 			if (file.exists()) {
 				return file; // file is on file system. can be safed
 			} else {
 				return null; // is mapped but file does not exist
 			}
 		} else {
 			return null; // not mapped / no file chosen by user
 		}
 	}
 
 	private boolean isItemSelected() {
 		return list.getSelectedValue() != null;
 	}
 
 	private int getIdFromSelectedItem() {
 		Object selectedValue = list.getSelectedValue();
 		if (selectedValue != null) {
 			return getIdFrom(selectedValue);
 		} else {
 			return -1;
 		}
 	}
 
 	private void rememberFilePath(File path) {
 		int id = getIdFromSelectedItem();
 		listIdToFilepath.put(id, path);
 	}
 
 	public Collection<Integer> getIdsFromSelectedListItems() {
 		Collection<Integer> result = new HashSet<Integer>();
 		for (Object value : list.getSelectedValues()) {
 			result.add(listItemToPId.get(value));
 		}
 		return result;
 	}
 
 }
