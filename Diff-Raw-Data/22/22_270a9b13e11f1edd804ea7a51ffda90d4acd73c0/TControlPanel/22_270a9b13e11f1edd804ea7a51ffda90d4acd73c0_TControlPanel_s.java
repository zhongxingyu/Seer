 package com.bluebarracudas.app;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 
 import com.bluebarracudas.model.TFactory;
 import com.bluebarracudas.model.TStation;
 
 public class TControlPanel extends JPanel implements ActionListener {
 
 	public static final int PREF_WIDTH = 300;
	public static final int PREF_HEIGHT = 600;
 
 	private JButton m_pAddStationButton;
 	private JButton m_pClearRouteButton;
 	private JButton m_pFindRouteButton;
 	private JComboBox stationCombo;
 	private DefaultListModel selectedStations;
 	private JList selectedStationsContainer;
 
 	private final Dimension m_pPrefButtonDimension = new Dimension(40, 40);
 
 	/** default constructor */
 	public TControlPanel() {
 
 		// Set our preferred size
 		setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));
 
 		// Set up our buttons
 		m_pAddStationButton = new JButton();
 		m_pAddStationButton.setVisible(true);
 		m_pAddStationButton.setPreferredSize(m_pPrefButtonDimension);
 		m_pAddStationButton.setText("+");
 		m_pAddStationButton.addActionListener(this);
 		add(m_pAddStationButton);
 
 		m_pClearRouteButton = new JButton();
 		m_pClearRouteButton.setVisible(true);
 		m_pClearRouteButton.setPreferredSize(m_pPrefButtonDimension);
 		m_pClearRouteButton.setText("-");
 		m_pClearRouteButton.addActionListener(this);
 		add(m_pClearRouteButton);
 
 		m_pFindRouteButton = new JButton();
 		m_pFindRouteButton.setVisible(true);
 		m_pFindRouteButton.setPreferredSize(m_pPrefButtonDimension);
 		m_pFindRouteButton.setText("ok");
 		m_pFindRouteButton.addActionListener(this);
 		add(m_pFindRouteButton);
 
 		initializeComboBox();
 
 		selectedStations = new DefaultListModel();
 
 		selectedStationsContainer = new JList(selectedStations);
 		selectedStationsContainer
 				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		selectedStationsContainer.setLayoutOrientation(JList.VERTICAL);
 
 		JScrollPane listScroller = new JScrollPane(selectedStationsContainer);
 		listScroller.setPreferredSize(new Dimension(150, 200));
 		add(listScroller);
 
 		setFocusable(true);
 		setVisible(true);
 	}
 
 	/** initializes the station combo box */
 	private void initializeComboBox() {
 		ArrayList<String> stationNames = new ArrayList<String>();
 		for (TStation pStation : TFactory.getAllStationsByName()) {
 			stationNames.add(pStation.getName());
 		}
 
 		stationCombo = new JComboBox(stationNames.toArray());
 		stationCombo.setSelectedIndex(0);
 		stationCombo.addActionListener(this);
 		add(stationCombo);
 	}
 
 	/**
 	 * Adds the currently selected station from combo box to the selected
 	 * station list
 	 */
 	private void addSelectedStation() {
 		selectedStations.addElement(stationCombo.getSelectedItem());
 	}
 
 	/**
 	 * Removes the currently selected station in the select station list from
 	 * the selected station list
 	 */
 	private void removeSelectedStation() {
 		int selectedIndex = selectedStationsContainer.getSelectedIndex();
 		if (selectedIndex >= 0) {
 			selectedStations.removeElementAt(selectedIndex);
 		}
 	}
 
 	/** Gets called when the ok button is clicked. */
 	private void okButtonHandler() {
 		// Skeleton
 	}
 
 	/** Listens for and handles action events appropriately */
 	public void actionPerformed(ActionEvent actionEvent) {
 		String actionCommand = actionEvent.getActionCommand();
 		if (actionCommand == "+") {
 			addSelectedStation();
 		} else if (actionCommand == "-") {
 			removeSelectedStation();
 		} else if (actionCommand == "ok") {
 			okButtonHandler();
 		}
 	}
 }
