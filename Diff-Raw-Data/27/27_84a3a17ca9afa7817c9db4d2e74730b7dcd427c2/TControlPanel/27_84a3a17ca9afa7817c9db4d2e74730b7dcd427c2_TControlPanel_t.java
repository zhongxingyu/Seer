 package com.bluebarracudas.app;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Time;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.ScrollPaneConstants;
 
 import com.bluebarracudas.model.TFactory;
 import com.bluebarracudas.model.TListCellRenderer;
 import com.bluebarracudas.model.TPrediction;
 import com.bluebarracudas.model.TRoute;
 import com.bluebarracudas.model.TStation;
 import com.bluebarracudas.model.TStop;
 
 public class TControlPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 2520383704533363877L;
 
 	public static final int PREF_WIDTH = 300;
 	public static final int PREF_HEIGHT = 500;
 
 	private JButton m_pAddStationButton;
 	private JButton m_pClearRouteButton;
 	private JButton m_pFindRouteButton;
 	private JComboBox stationCombo;
 	private DefaultListModel selectedStations;
 	private JList selectedStationsContainer;
 	private JComboBox m_pDepartureCombo;
 	private JComboBox m_pDepartureTimeCombo;
 	private JList m_pRoute;
 	private DefaultListModel routeStops;
 	private JComboBox m_pRouteOptionsCombo;
 	
 	private final Dimension m_pPrefButtonDimension = new Dimension(50, 50);
 
 	private enum ROUTE_TIME_OPTION {
 		Departat, Arriveby;
 	}
 
	/** Default constructor */
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
 		m_pFindRouteButton.setEnabled(false);
 		add(m_pFindRouteButton);
 
 		initializeComboBox();
 
 		selectedStations = new DefaultListModel();
 		
 		selectedStationsContainer = new JList(selectedStations);
 		selectedStationsContainer
 				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		selectedStationsContainer.setLayoutOrientation(JList.VERTICAL);
 		
 		
 
 		JScrollPane listScroller = new JScrollPane(selectedStationsContainer);
 		listScroller.setPreferredSize(new Dimension(200, 100));
 		add(listScroller);
 
 		initializeDepartureCombos();
 		
 		initializeRouteOptionsCombo();
 		
 		JLabel routeLabel = new JLabel("Suggested Route:");
 		add(routeLabel);
 		routeStops = new DefaultListModel();
 		m_pRoute = new JList(routeStops);
 		JScrollPane listScroller2 = new JScrollPane(m_pRoute);
 		listScroller2.setPreferredSize(new Dimension(200, 200));
 		listScroller2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		add(listScroller2);
 	}
 
 	/**
 	 * Initializes the Route options: Earliest Departure, Earliest Arrival,
 	 * Fastest Route, Fewest Transfers
 	 */
 	private void initializeRouteOptionsCombo() {
 		List<String> routeOptions = new ArrayList<String>();
 		routeOptions.add("Earliest Departure");
 		routeOptions.add("Earliest Arrival");
 		routeOptions.add("Fastest Route");
 		routeOptions.add("Fewest Transfers");
 		m_pRouteOptionsCombo = new JComboBox(routeOptions.toArray());
 		m_pRouteOptionsCombo.setSelectedIndex(0);
 		m_pRouteOptionsCombo.addActionListener(this);
 		add(m_pRouteOptionsCombo);	
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
 
 	/** Initializes the departure options combo boxes */
 	private void initializeDepartureCombos() {
 		List<String> departureOptions = new ArrayList<String>();
 		departureOptions.add("Depart at");
 		departureOptions.add("Arrive by");
 		m_pDepartureCombo = new JComboBox(departureOptions.toArray());
 		m_pDepartureCombo.setSelectedIndex(0);
 		m_pDepartureCombo.addActionListener(this);
 		add(m_pDepartureCombo);	
 		
 		List<String> times = new ArrayList<String>();
 		times.add("now");
 		
 		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
 		
 		Date today = new Date();
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(today);
 		calendar.set(Calendar.SECOND, 0);
 		calendar.set(Calendar.MILLISECOND, 0);
 		int unroundedMinutes = calendar.get(Calendar.MINUTE);
 		int mod = unroundedMinutes % 15;
 		calendar.add(Calendar.MINUTE, 15 - mod);
 		long now = calendar.getTime().getTime();
 		times.add(dateFormat.format(new Date(now)));
 		
 		int fifteen = 15 * 60 * 1000;
 		for (int t = 1; t <= 20; t++) {
 			Date nextDate = (new Date(now + fifteen*t));
 			times.add(dateFormat.format(nextDate));
 		}
 
 		m_pDepartureTimeCombo = new JComboBox(times.toArray());
 		m_pDepartureTimeCombo.setSelectedIndex(0);
 		m_pDepartureTimeCombo.addActionListener(this);
 		add(m_pDepartureTimeCombo);
 	}
 	
 	/** Displays a route on the map */
 	private void displayRoute(TRoute route) {
 		displayRoute(route, -1);
 	}
 	
 	/** Displays a route on the map, with an arrive by time in seconds */
 	private void displayRoute(TRoute route, int arriveBy) {
 		m_pRoute.setCellRenderer(new TListCellRenderer());
 		m_pRoute.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		m_pRoute.setLayoutOrientation(JList.VERTICAL);
 		
 		setFocusable(true);
 		setVisible(true);
 		
 		List<TPrediction> preds = route.getPredictions();
 		List<String> displayVals = new ArrayList<String>();
 		int size = route.getStops().size();
 		for (int i = 0; i < size; i++) {
 			TStop lastStop = route.getStops().get(size - 1);
 			lastStop.getNextPred(0);
 			TStop stop = route.getStops().get(i);
 			String display = stop.getID() + "";
 			if (preds.get(i) == null)
 				display += " (time unknown)";
 			else if (arriveBy >= 0){
 				if(preds.size() >= size){
 					TPrediction pred = preds.get(size - 1);
 					
 					if(pred != null && arriveBy >= pred.getSecToArrival()){
 						display += " in " + preds.get(i).getSecToArrival() / 60 + " mins";
 					}else{
 						display += " (time unknown)";	
 					}
 				}else{
 					display += " (time unknown)";	
 				}
 			}else{
 				TPrediction pred = preds.get(i);
 				display += " in " + pred.getSecToArrival() / 60 + " mins";
 			}
 			
 			routeStops.addElement(display);
 			displayVals.add(display);
 		}
 		m_pRoute.setListData(displayVals.toArray());
 	}
 	
 	/**
 	 * Adds the currently selected station from combo box to the selected
 	 * station list
 	 */
 	private void addSelectedStation() {
 		selectedStations.addElement(stationCombo.getSelectedItem());
 		enableOkButton();
 	}
 
 	/**
 	 * Removes the currently selected station in the select station list from
 	 * the selected station list
 	 */
 	private void removeSelectedStation() {
 		int selectedIndex = selectedStationsContainer.getSelectedIndex();
 		if (selectedIndex >= 0) {
 			selectedStations.removeElementAt(selectedIndex);
 			enableOkButton();
 		}
 	}
 
 	/** Enables or disables the ok button */
 	private void enableOkButton() {
 		if (selectedStations.size() >= 2) {
 			m_pFindRouteButton.setEnabled(true);
 		} else {
 			m_pFindRouteButton.setEnabled(false);
 		}
 	}
 
 	/**
 	 * Gets called when the ok button is clicked. Supposed to intiate the
 	 * calcuation of a TPrediction
 	 */
 	private void okButtonHandler() {
 		Date today = new Date();
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(today);
 		long nowMillis = calendar.getTime().getTime();
 		
 		List<TStation> stations = new ArrayList<TStation>();
 		for (Object sName : selectedStations.toArray()) {
 			stations.add(TFactory.getStation((String)sName));
 		}
 
 		System.out.println(stations);
 
 		int selectedTimeSeconds;
 		if (m_pDepartureTimeCombo.getSelectedItem().toString() != "now"){
 			int epochTimeNowMillis = (int)(nowMillis % (1000 * 60 * 60 * 24)); 
 			selectedTimeSeconds = (int) (Time.valueOf(m_pDepartureTimeCombo.getSelectedItem().toString()).getTime() - epochTimeNowMillis)/1000;
 		}else{
 			selectedTimeSeconds = 0;
 		}
 
 		ROUTE_TIME_OPTION routeTimeOption = ROUTE_TIME_OPTION.valueOf(m_pDepartureCombo.getSelectedItem().toString().replace(" ", ""));
 		if(routeTimeOption == ROUTE_TIME_OPTION.Arriveby){
 			displayRoute(TFactory.findShortestPath(stations, 0), selectedTimeSeconds);
 		} else {
 			displayRoute(TFactory.findShortestPath(stations, selectedTimeSeconds));
 		}
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
