 package com.md.dm.infovis.view;
 
 import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
 
 import java.awt.BorderLayout;
 import java.awt.Checkbox;
 import java.awt.GridLayout;
 
 import javax.swing.BoxLayout;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JSplitPane;
 import javax.swing.JToggleButton;
 import javax.swing.JTree;
 import javax.swing.border.TitledBorder;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeModel;
 
 import org.jdesktop.swingx.JXMapKit;
 import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
 import org.jdesktop.swingx.mapviewer.GeoPosition;
 
 public class MainView extends JPanel {
 	
 	
 	public MainView() {
 		initComponents();
 		afterInitComponents();
 	}
 
 	private void afterInitComponents() {
 		// TODO Auto-generated method stub
 		
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
 		
 		//centerPanel.add(initMap(), BorderLayout.CENTER);
 		westPanel.add(initFilterPanel(), BorderLayout.CENTER);
 		//eastPanel.add(initRegionPanel(), BorderLayout.CENTER);
 		southPanel.add(initNavigationPanel(), BorderLayout.CENTER);
 		
 		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, initMap(), initRegionPanel());
 		splitPane.setOneTouchExpandable(true);
 		splitPane.setResizeWeight(1.0);
 		centerPanel.add(splitPane, BorderLayout.CENTER);
 	}
 	
 	private JPanel initMap(){
 		
 		JXMapKit jXMapKit = new JXMapKit();
 		jXMapKit.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);
 		jXMapKit.setAddressLocationShown(false);
 		jXMapKit.setDataProviderCreditShown(false);
 		//jXMapKit.setCenterPosition(new GeoPosition(0, 0));
 		jXMapKit.setMiniMapVisible(true);
 		jXMapKit.setZoomSliderVisible(true);
 		jXMapKit.setZoomButtonsVisible(true);
 		jXMapKit.setAddressLocationShown(true);
 		jXMapKit.setCenterPosition(new GeoPosition( 45, -90));
 		//jXMapKit.setAddressLocation(new GeoPosition(1, 1));
 
 		jXMapKit.setZoom(8);
 		((DefaultTileFactory) jXMapKit.getMainMap().getTileFactory()).setThreadPoolSize(8);
 		return jXMapKit;
 	}
 	
 	private JPanel initFilterPanel(){
 		
 		JPanel filterPanel = new JPanel();
 		BoxLayout boxLayout = new BoxLayout(filterPanel, WIDTH);
 		filterPanel.setLayout(boxLayout);
 		
 		JPanel policyStatusPanel = new JPanel();
 		policyStatusPanel.setBorder(new TitledBorder("Policy Status"));
 		policyStatusPanel.setLayout(new GridLayout(5, 0));
 		Checkbox healthy = new Checkbox("Healthy");
 		Checkbox moderate = new Checkbox("Moderate");
 		Checkbox nonNormal = new Checkbox("Non normal");
 		Checkbox critical = new Checkbox("Critical");
 		Checkbox infected = new Checkbox("Infected");
 		
 		policyStatusPanel.add(healthy);		
 		policyStatusPanel.add(moderate);		
 		policyStatusPanel.add(nonNormal);		
 		policyStatusPanel.add(critical);		
 		policyStatusPanel.add(infected);
 		
 		filterPanel.add(policyStatusPanel);
 		
 		JPanel activityFlagPanel = new JPanel();
 		activityFlagPanel.setBorder(new TitledBorder("Activity Flag"));
 		activityFlagPanel.setLayout(new GridLayout(5, 0));
 		Checkbox normal = new Checkbox("Normal");
 		Checkbox goingDown = new Checkbox("Going down");
 		Checkbox invalidLogin = new Checkbox("5 invalid login");
 		Checkbox fullyConsumed = new Checkbox("CPU fully consumed");
 		Checkbox deviceAdded = new Checkbox("Device added");
 		
 		activityFlagPanel.add(normal);		
 		activityFlagPanel.add(goingDown);		
 		activityFlagPanel.add(invalidLogin);		
 		activityFlagPanel.add(fullyConsumed);		
 		activityFlagPanel.add(deviceAdded);
 		
 		filterPanel.add(activityFlagPanel);
 		
 		JPanel numConnectionsPanel = new JPanel();
 		numConnectionsPanel.setBorder(new TitledBorder("# Connections"));
 		numConnectionsPanel.setLayout(new GridLayout(1, 1));
 		String[] options = {"0-20", "20-50", "more than 50"};
 		JComboBox numConnectionsComboBox = new JComboBox(options);
 		numConnectionsPanel.add(numConnectionsComboBox);
 		
 		filterPanel.add(numConnectionsPanel);
 		
 		return filterPanel;
 	}
 
 	private JPanel initRegionPanel(){
 		
 		JPanel regionPanel = new JPanel(new BorderLayout());
 		
 		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Bank");
 		DefaultMutableTreeNode headQuartersNode = new DefaultMutableTreeNode("Headquarters");
 		rootNode.add(headQuartersNode);
 		for(int i = 1; i <= 5; i++){
 			headQuartersNode.add(new DefaultMutableTreeNode("DataCenter-" + i));
 		}
 		
 		for(int i = 1; i <= 50; i++){
			DefaultMutableTreeNode regionNode = new DefaultMutableTreeNode("Region-" + i);
 			rootNode.add(regionNode);
 			int branches = 50;
 			if(i>=1 && i <=10){
 				branches = 200;
 			}
 			for(int j=1; j <= branches; j++){
				regionNode.add(new DefaultMutableTreeNode("Branch-" + j));
 			}
 		}
		TreeModel yourTreeModel = new DefaultTreeModel(rootNode); 
		CheckboxTree checkboxTree = new CheckboxTree(yourTreeModel);

 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setViewportView(checkboxTree);
 		
 		regionPanel.add(scrollPane, BorderLayout.CENTER);
 		
 	
 		return regionPanel;
 	}
 	
 	private JPanel initNavigationPanel(){
 		JPanel navigationPanel = new JPanel(new BorderLayout());
 		
 		JProgressBar progressBar = new JProgressBar();
 		JSlider slider = new JSlider();
 		JToggleButton toggleButton = new JToggleButton("Play");
 
 		JPanel controlPanel = new JPanel(new BorderLayout());
 		controlPanel.add(toggleButton, BorderLayout.WEST);
 		controlPanel.add(slider, BorderLayout.CENTER);
 		
 		
 		navigationPanel.add(progressBar, BorderLayout.WEST);
 		navigationPanel.add(controlPanel, BorderLayout.CENTER);
 		
 		return navigationPanel;
 	}
 	
 }
