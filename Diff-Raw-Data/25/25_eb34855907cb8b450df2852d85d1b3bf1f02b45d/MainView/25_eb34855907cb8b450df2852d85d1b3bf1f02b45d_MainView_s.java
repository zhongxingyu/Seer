 package com.md.dm.infovis.view;
 
 import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
 import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;
 
 import java.awt.BorderLayout;
 import java.awt.Checkbox;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JSplitPane;
 import javax.swing.JToggleButton;
 import javax.swing.border.TitledBorder;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 
 import com.md.dm.infovis.vast.controller.DataController;
 import com.md.dm.infovis.vast.controller.MapKitController;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.QueryBuilder;
 
 public class MainView extends JPanel {
 
 	private MapKitController mapKitController;
 	private CheckboxTree checkboxTree;
 	private DataController dataController;
 	private Checkbox healthy;
 	private Checkbox moderate;
 	private Checkbox nonNormal;
 	private Checkbox critical;
 	private Checkbox infected;
 	private HashMap<Checkbox, Integer> policyCheckboxMap;
 	private HashMap<Checkbox, Integer> activityCheckboxMap;
 	private Checkbox normal;
 	private Checkbox goingDown;
 	private Checkbox invalidLogin;
 	private Checkbox fullyConsumed;
 	private Checkbox deviceAdded;
 	private ArrayList<Checkbox> machineFunctionList;
 	private ArrayList<Checkbox> machineClassList;
 	private DataController machineDataController;
 	private JComboBox chartComboBox;
 
 	public MainView() {
 		initComponents();
 		afertInitComponents();
 	}
 
 	private void afertInitComponents() {
 		try {
 			dataController = new DataController();
 			machineDataController = new DataController(new Mongo("localhost:27022"), "vast", "region");
 			policyCheckboxMap = new HashMap<Checkbox, Integer>();
 			policyCheckboxMap.put(healthy, 1);
 			policyCheckboxMap.put(moderate, 2);
 			policyCheckboxMap.put(nonNormal, 3);
 			policyCheckboxMap.put(critical, 4);
 			policyCheckboxMap.put(infected, 5);
 			activityCheckboxMap = new HashMap<Checkbox, Integer>();
 			activityCheckboxMap.put(normal, 1);
 			activityCheckboxMap.put(goingDown, 2);
 			activityCheckboxMap.put(invalidLogin, 3);
 			activityCheckboxMap.put(fullyConsumed, 4);
 			activityCheckboxMap.put(deviceAdded, 5);			
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void initComponents() {
 		setLayout(new BorderLayout());
 
 		JPanel westPanel = new JPanel(new BorderLayout());
 		JPanel eastPanel = new JPanel(new BorderLayout());
 		JPanel northPanel = new JPanel(new BorderLayout());
 		JPanel southPanel = new JPanel(new BorderLayout());
 		JPanel centerPanel = new JPanel(new BorderLayout());
 
 		add(westPanel, BorderLayout.WEST);
 		add(eastPanel, BorderLayout.EAST);
 		add(northPanel, BorderLayout.NORTH);
 		add(southPanel, BorderLayout.SOUTH);
 		add(centerPanel, BorderLayout.CENTER);
 
 		// centerPanel.add(initMap(), BorderLayout.CENTER);
 		westPanel.add(initFilterPanel(), BorderLayout.CENTER);
 		// eastPanel.add(initRegionPanel(), BorderLayout.CENTER);
 		southPanel.add(initNavigationPanel(), BorderLayout.CENTER);
 
 		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				initMap(), initRegionPanel());
 		splitPane.setOneTouchExpandable(true);
 		splitPane.setResizeWeight(1.0);
 		centerPanel.add(splitPane, BorderLayout.CENTER);
 	}
 
 	private JPanel initMap() {
 
 		try {
 			mapKitController = new MapKitController();
 			return mapKitController.getMapKit();
 
 		} catch (Exception e) {
 			// TODO: informar
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private JPanel initFilterPanel() {
 
 		JPanel filterPanel = new JPanel();
 		BoxLayout boxLayout = new BoxLayout(filterPanel, WIDTH);
 		filterPanel.setLayout(boxLayout);
 
 		JPanel policyStatusPanel = new JPanel();
 		policyStatusPanel.setBorder(new TitledBorder("Policy Status"));
 		policyStatusPanel.setLayout(new BorderLayout());
 		JPanel centerPanel = new JPanel(new GridLayout(5, 0));
 		healthy = new Checkbox("Healthy");
 		moderate = new Checkbox("Moderate");
 		nonNormal = new Checkbox("Non normal");
 		critical = new Checkbox("Critical");
 		infected = new Checkbox("Infected");
 
 		centerPanel.add(healthy);
 		centerPanel.add(moderate);
 		centerPanel.add(nonNormal);
 		centerPanel.add(critical);
 		centerPanel.add(infected);
 		
 		JPanel eastPanel = new JPanel(new GridLayout(5, 0));
 		JLabel label = new JLabel("  ");
 		label.setOpaque(true);
 		label.setBackground(new Color(26, 150, 65, 100));
 		eastPanel.add(label);
		label = new JLabel("");
 		label.setOpaque(true);
 		label.setBackground(new Color(166, 217, 106, 100));
 		eastPanel.add(label);
		label = new JLabel("");
 		label.setOpaque(true);
 		label.setBackground(new Color(255, 255, 191, 100));
 		eastPanel.add(label);
		label = new JLabel("");
 		label.setOpaque(true);
 		label.setBackground(new Color(253, 174, 97, 100));
 		eastPanel.add(label);
		label = new JLabel("");
 		label.setOpaque(true);
 		label.setBackground(new Color(215, 25, 28, 100));
 		eastPanel.add(label);
 
 		policyStatusPanel.add(eastPanel, BorderLayout.EAST);
 		policyStatusPanel.add(centerPanel, BorderLayout.CENTER);
 		filterPanel.add(policyStatusPanel);
 
 		JPanel activityFlagPanel = new JPanel();
 		activityFlagPanel.setBorder(new TitledBorder("Activity Flag"));
 		activityFlagPanel.setLayout(new BorderLayout());
 		centerPanel = new JPanel(new GridLayout(5, 0));
 		normal = new Checkbox("Normal");
 		goingDown = new Checkbox("Going down");
 		invalidLogin = new Checkbox("5 invalid login");
 		fullyConsumed = new Checkbox("CPU fully consumed");
 		deviceAdded = new Checkbox("Device added");
 
 		centerPanel.add(normal);
 		centerPanel.add(goingDown);
 		centerPanel.add(invalidLogin);
 		centerPanel.add(fullyConsumed);
 		centerPanel.add(deviceAdded);
 
 		eastPanel = new JPanel(new GridLayout(5, 0));
 		label = new JLabel("  ");
 		label.setOpaque(true);
 		label.setBackground(new Color(27, 158, 119, 100));
 		eastPanel.add(label);
		label = new JLabel("");
 		label.setOpaque(true);
 		label.setBackground(new Color(217, 95, 2, 100));
 		eastPanel.add(label);
		label = new JLabel("");
 		label.setOpaque(true);
 		label.setBackground(new Color(117, 112, 179, 100));
 		eastPanel.add(label);
		label = new JLabel("");
 		label.setOpaque(true);
 		label.setBackground(new Color(231, 41, 138, 100));
 		eastPanel.add(label);
		label = new JLabel("");
 		label.setOpaque(true);
 		label.setBackground(new Color(102, 166, 30, 100));
 		eastPanel.add(label);
 
 		activityFlagPanel.add(eastPanel, BorderLayout.EAST);
 		activityFlagPanel.add(centerPanel, BorderLayout.CENTER);
 		filterPanel.add(activityFlagPanel);
 
 		JPanel machineClassPanel = new JPanel();
 		machineClassPanel.setBorder(new TitledBorder("Machine Class"));
 		machineClassPanel.setLayout(new GridLayout(3, 0));
 		machineClassList = new ArrayList<Checkbox>();
 		machineClassList.add(new Checkbox("workstation"));
 		machineClassList.add(new Checkbox("server"));
 		machineClassList.add(new Checkbox("atm"));
 
 		for (Checkbox checkbox : machineClassList) {
 			machineClassPanel.add(checkbox);
 		}
 
 		filterPanel.add(machineClassPanel);
 
 		JPanel machineFunctionPanel = new JPanel();
 		machineFunctionPanel.setBorder(new TitledBorder("Machine Class"));
 		machineFunctionPanel.setLayout(new GridLayout(9, 0));
 		machineFunctionList = new ArrayList<Checkbox>();
 		machineFunctionList.add(new Checkbox(""));
 		machineFunctionList.add(new Checkbox("web"));
 		machineFunctionList.add(new Checkbox("multiple"));
 		machineFunctionList.add(new Checkbox("office"));
 		machineFunctionList.add(new Checkbox("email"));
 		machineFunctionList.add(new Checkbox("teller"));
 		machineFunctionList.add(new Checkbox("compute"));
 		machineFunctionList.add(new Checkbox("loan"));
 		machineFunctionList.add(new Checkbox("file server"));
 
 		for (Checkbox checkbox : machineFunctionList) {
 			machineFunctionPanel.add(checkbox);
 		}
 
 		filterPanel.add(machineFunctionPanel);
 
 		
 		JPanel numConnectionsPanel = new JPanel();
 		numConnectionsPanel.setBorder(new TitledBorder("# Connections"));
 		numConnectionsPanel.setLayout(new GridLayout(1, 1));
 		String[] options = { "0-20", "20-50", "more than 50" };
 		JComboBox numConnectionsComboBox = new JComboBox(options);
 		numConnectionsPanel.add(numConnectionsComboBox);
 
 		filterPanel.add(numConnectionsPanel);
 
 		JPanel chartPanel = new JPanel();
 		chartPanel.setBorder(new TitledBorder("Chart"));
 		chartPanel.setLayout(new GridLayout(1, 1));
 		String[] types = { "PolicyStatus-Pie", "ActivityFlag-Pie" };
 		chartComboBox = new JComboBox(types);
 		chartPanel.add(chartComboBox);
 
 		filterPanel.add(chartPanel);
 
 		return filterPanel;
 	}
 
 	private JPanel initRegionPanel() {
 
 		JPanel regionPanel = new JPanel(new BorderLayout());
 
 		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Bank");
 		DefaultMutableTreeNode headQuartersNode = new DefaultMutableTreeNode(
 				"headquarters");
 		rootNode.add(headQuartersNode);
 
 		for (int i = 1; i <= 5; i++) {
 			headQuartersNode.add(new DefaultMutableTreeNode("datacenter-" + i));
 		}
 		headQuartersNode.add(new DefaultMutableTreeNode("headquarters"));
 
 		for (int i = 1; i <= 50; i++) {
 			DefaultMutableTreeNode regionNode = new DefaultMutableTreeNode(
 					"region-" + i);
 			rootNode.add(regionNode);
 			int branches = 50;
 			if (i >= 1 && i <= 10) {
 				branches = 200;
 			}
 			for (int j = 1; j <= branches; j++) {
 				regionNode.add(new DefaultMutableTreeNode("branch" + j));
 			}
 			regionNode.add(new DefaultMutableTreeNode("headquarters"));
 		}
 		TreeModel treeModel = new DefaultTreeModel(rootNode);
 		checkboxTree = new CheckboxTree(treeModel);
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setViewportView(checkboxTree);
 
 		regionPanel.add(scrollPane, BorderLayout.CENTER);
 
 		return regionPanel;
 	}
 
 	private JPanel initNavigationPanel() {
 		JPanel navigationPanel = new JPanel(new BorderLayout());
 
 		JProgressBar progressBar = new JProgressBar();
 		JSlider slider = new JSlider();
 		JButton applyButton = new JButton("Apply");
 		applyButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				showData();
 			}
 		});
 		JToggleButton toggleButton = new JToggleButton("Play");
 
 		JPanel controlPanel = new JPanel(new BorderLayout());
 		JPanel buttonPanel = new JPanel(new BorderLayout());
 		buttonPanel.add(applyButton, BorderLayout.WEST);
 		buttonPanel.add(toggleButton, BorderLayout.EAST);
 
 		controlPanel.add(buttonPanel, BorderLayout.WEST);
 		controlPanel.add(slider, BorderLayout.CENTER);
 
 		navigationPanel.add(progressBar, BorderLayout.WEST);
 		navigationPanel.add(controlPanel, BorderLayout.CENTER);
 
 		return navigationPanel;
 	}
 
 	private void showData() {
 
 		Set<String> businessUnits = new HashSet<String>();
 		Set<String> facilities = new HashSet<String>();
 		BasicDBObject key = new BasicDBObject();
 		key.append("bussinesUnit", true);
 		key.append("facility", true);
 		key.append("location", true);
 
 		QueryBuilder qb = new QueryBuilder();
 		qb.put("statusList").notEquals(new ArrayList());
 
 		TreeCheckingModel checkingModel = ((CheckboxTree) checkboxTree)
 				.getCheckingModel();
 		TreePath[] checkingPaths = checkingModel.getCheckingPaths();
 		for (int i = 0; i < checkingPaths.length; i++) {
 			boolean checked = checkingModel.isPathChecked(checkingPaths[i]);
 			if (checked) {
 				System.out.println(checkingPaths[i] + " " + checked);

 				if (checkingPaths[i].getPathCount() > 1) {
 
 					DefaultMutableTreeNode node = (DefaultMutableTreeNode) checkingPaths[i]
 							.getPathComponent(1);
 					String businessUnit = (String) node.getUserObject();
 					businessUnits.add(businessUnit);
 
 					if (checkingPaths[i].getPathCount() > 2) {
 						DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) checkingPaths[i]
 								.getPathComponent(2);
 						String facility = (String) childNode.getUserObject();
 						facilities.add(facility);
 						qb.or(new BasicDBObject("bussinesUnit", businessUnit)
 								.append("facility", facility));
 					}
 				}
 			}
 		}
 
 		System.out.println(businessUnits);
 		System.out.println(facilities);
 		
 		
 		//Filter policystatus
 		Set<Checkbox> keySet = policyCheckboxMap.keySet();
 		List<Integer> statuses = new ArrayList<Integer>();
 		for (Checkbox checkbox : keySet) {
 			if(checkbox.getState() == true){
 				statuses.add(policyCheckboxMap.get(checkbox));
 			}
 		}
 		
 		if(!statuses.isEmpty()){
 			qb.put("statusList.policyStatus").in(statuses);
 		}
 
 		//Filter activityflag
 		keySet = activityCheckboxMap.keySet();
 		statuses = new ArrayList<Integer>();
 		for (Checkbox checkbox : keySet) {
 			if(checkbox.getState() == true){
 				statuses.add(activityCheckboxMap.get(checkbox));
 			}
 		}
 		
 		if(!statuses.isEmpty()){
 			qb.put("statusList.activityFlag").in(statuses);
 		}
 		
 		//Filter machine class
 		List<String> filters = new ArrayList<String>();
 		for (Checkbox checkbox : machineClassList) {
 			if(checkbox.getState() == true){
 				filters.add(checkbox.getLabel());
 			}			
 		}
 		if(!filters.isEmpty()){
 			qb.put("machineClass").in(filters);
 		}
 		//Filter machine function
 		filters = new ArrayList<String>();
 		for (Checkbox checkbox : machineFunctionList) {
 			if(checkbox.getState() == true){
 				filters.add(checkbox.getLabel());
 			}			
 		}
 		if(!filters.isEmpty()){
 			qb.put("machineFunction").in(filters);
 		}
 		
 		System.out.println(qb.get());
 		
 		DBObject group = this.dataController.group(key, qb.get());
 		//DBCursor filter = machineDataController.filter(qb.get());
 		mapKitController.showData(group, (String)chartComboBox.getSelectedItem());
 	}
 
 }
