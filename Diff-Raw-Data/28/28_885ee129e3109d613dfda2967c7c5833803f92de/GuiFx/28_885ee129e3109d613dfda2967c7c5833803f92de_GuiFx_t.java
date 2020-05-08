 package guiFX;
 
 /**
 
  * Copyright (c) 2008, 2012 Oracle and/or its affiliates.
 
  * All rights reserved. Use is subject to license terms.
 
  */
 
 import javafx.application.Application;
 import javafx.application.Platform;
 import javafx.collections.FXCollections;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.scene.Group;
 import javafx.scene.Node;
 import javafx.scene.Scene;
 import javafx.stage.PopupWindow;
 import javafx.stage.Stage;
 import javafx.stage.WindowEvent;
 import javafx.scene.control.Button;
 import javafx.scene.control.ChoiceBox;
 import javafx.scene.control.Label;
 import javafx.scene.control.TextArea;
 import javafx.scene.control.TextField;
 import javafx.scene.image.ImageView;
 import javafx.scene.layout.BorderPane;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.VBox;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.image.Image;
 import javafx.scene.input.KeyCode;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.Rectangle;
 import javafx.scene.text.Font;
 import javafx.scene.text.Text;
 import javafx.scene.web.PopupFeatures;
 import javafx.scene.web.WebEngine;
 import javafx.scene.web.WebView;
 import javafx.util.Callback;
 
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import datapath.NodePuppet;
 import datapath.TrafficWatch;
 import discovery.NetworkDiscovery;
 import discovery.PathDiscovery;
 import tests.pktLoss;
 import topo.ConnectionList;
 import topo.GuiConnection;
 import topo.NodeList;
 import vty.VTYSession;
 
 public class GuiFx extends Application {
 
 	private Iterator<Node> i;
 	
 	private String srcIP;
 	private String dstIP;
 	private String targetIP;
 	private int srcPort;
 	private int dstPort;
 	private int window;
 	private String protocol;
 	private String username;
 	private String password;
 	private ArrayList<Thread>threads = new ArrayList<Thread>();
 	private static ArrayList<String>discoveredIPs = new ArrayList<String>();
 	private static ArrayList<String>nodeIPs = new ArrayList<String>();
 	private static ArrayList<GuiConnection> connections = new ArrayList<GuiConnection>();
 	private static NodeList guiNodes = new NodeList();
 	private static ConnectionList guiConnections = new ConnectionList();
 	
 	private TextField srcIPField = new TextField("10.192.10.110");
 	private TextField dstIPField = new TextField("10.192.40.140");
 	private TextField srcPortField = new TextField("0");
 	private TextField dstPortField = new TextField("80");
 	private TextField windowField = new TextField("100");
 	private ChoiceBox<String> protocolField = new ChoiceBox<String>(FXCollections.observableArrayList("TCP", "UDP", "ICMP", "EGP", "RSVP", "IGRP", "GRE", "ESP", "AH", "ALL"));
 	private TextField usernameField = new TextField("cisco");
 	private TextField passwordField = new TextField("cisco");
 	private TextField targetIPField = new TextField("1.1.1.1");
 	private NetworkDiscovery network;
 	private TrafficWatch traffic;
 	private Browser browser;
 	private pktLoss tester;
 
 	private ArrayList<NodePuppet> puppetList = new ArrayList<NodePuppet>();
 	//public static Map<Integer, List<String>> synchMap = Collections.synchronizedMap(new HashMap<Integer, List<String>>());
 	//FlowBuffer buffer = new FlowBuffer();
 	//private FlowBuffer map = new FlowBuffer();
 
 	private Image loaderImage = new Image("img/loader.gif", true);
 	
 	private final Object lock = new Object();
 	
 	@Override public void start(Stage stage) throws Exception {
 		Group root = new Group();
 		Scene scene = new Scene(root, 1000, 800);
 		HBox body = new HBox();
 		root.getChildren().add(body);
 		HBox controlPane = new HBox();
 		controlPane.setStyle("-fx-background-image: url('img/left_banner.png');");
 		controlPane.setPrefSize(260, 800);
 		controlPane.setPadding(new Insets(10));
 		VBox viewPane = new VBox();
 		viewPane.setPrefSize(740,800);
 		body.getChildren().addAll(controlPane, viewPane);
 		
 		// control pane
 		final VBox labels = new VBox(31);
 		final VBox fields = new VBox(26);
 		labels.setPadding(new Insets(188, 8, 8, 8));
 		fields.setPadding(new Insets(186, 6, 6, 6));
 		controlPane.getChildren().addAll(labels,fields);
 		
 		Label srcIPLabel = new Label("Source IP:");
 		Label dstIPLabel = new Label("Destination IP:");
 		Label srcPortLabel = new Label("Source Port:");
 		Label dstPortLabel = new Label("Destination Port:");
 		Label windowLabel = new Label("Window Size:");
 		Label protocolLabel = new Label("Protocol:");
 		Label usernameLabel = new Label("Username:");
 		Label passwordLabel = new Label("Password:");
 		Label targetIPLabel = new Label("Target IP:");
 		Button discoverButton = new Button("Discover");
 		Button connectButton = new Button("Connect");
 		
 		final Button traceButton = new Button("Trace");
 		final Button stopButton = new Button("Stop");
 		protocolField.getSelectionModel().selectFirst();
 		
 		labels.getChildren().addAll(srcIPLabel, dstIPLabel,srcPortLabel, dstPortLabel, 
 				windowLabel, protocolLabel, usernameLabel, passwordLabel, targetIPLabel);
 		fields.getChildren().addAll(this.srcIPField, this.dstIPField, this.srcPortField, this.dstPortField, 
 				this.windowField, this.protocolField, this.usernameField, this.passwordField, targetIPField, discoverButton, connectButton, traceButton);
 		
 		// topology pane
 		browser = new Browser("src/web/topSlice.html", "src/web/botSlice.html", loaderImage); 
 		viewPane.getChildren().add(browser);
 		
 		// format labels
 		i = labels.getChildren().iterator();
 		while(i.hasNext()) {
 			Node node = i.next();
 			if (node instanceof Label) {
 				Label label = (Label) node;
 				label.setFont(Font.font("Cambria", 14));
 			}
 		}
 		
 		// format fields
 		i = fields.getChildren().iterator();
 		while(i.hasNext()) {
 			Node node = i.next();
 			if (node instanceof TextField) {
 				TextField field = (TextField) node;
 				field.setMaxWidth(100);
 			}
 		}
 		
 
 		// Stop trace Listener
 		stopButton.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent e) {
 
 				fields.getChildren().remove(stopButton);
 				fields.getChildren().add(traceButton);
 				traceButton.requestFocus();
 				// call stop trace object (inputs)
 				traffic.run = false;
 			}
 		});
 		
 		// Discovery route listener
 		discoverButton.setOnAction(new EventHandler<ActionEvent>() {
 			public void handle(ActionEvent e) {
 				if (Validator.validateIP(srcIPField))
 					srcIP = srcIPField.getText().trim();
 				else 
 					return;
 				if (Validator.validateUsername(usernameField))
 					username = usernameField.getText().trim();
 				else 
 					return;
 				if (Validator.validatePassword(passwordField))
 					password = passwordField.getText().trim();
 				else 
 					return;
 				
 				try {
 					network = new NetworkDiscovery(browser, discoveredIPs, nodeIPs, guiNodes, guiConnections, srcIP, dstIP, username, password);
 					Thread thread = new Thread(network);
 					threads.add(thread);
 					thread.start();
 					browser.loadLoader();
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 
 		});
 		
 		// console into a box
 		connectButton.setOnAction(new EventHandler<ActionEvent>() {
 			public void handle(ActionEvent e) {
 				if (Validator.validateIP(targetIPField))
 					targetIP = targetIPField.getText().trim();
 				else 
 					return;
 				if (Validator.validateUsername(usernameField))
 					username = usernameField.getText().trim();
 				else 
 					return;
 				if (Validator.validatePassword(passwordField))
 					password = passwordField.getText().trim();
 				else 
 					return;
 				
 				/*for (String ip : discoveredIPs) // checks to see if node exist in disicoverey list
 					System.out.println(ip);*/
 				if (!discoveredIPs.contains(targetIP)) {
 					Validator.setFalse(targetIPField);
 					return;
 				}
 					
 				Console console = new Console(targetIP, username, password);
 				Thread thread = new Thread(console);
 				threads.add(thread);
 				thread.start();
 			}
 		});
 		
 		// Trace Listener
 		traceButton.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent e) {
 				if (Validator.validateIP(srcIPField))
 					srcIP = srcIPField.getText().trim();
 				else
 					return;
 				
 				if (Validator.validateIP(dstIPField))
 					dstIP = dstIPField.getText().trim();
 				else 
 					return;
 				
 				if (Validator.validatePort(dstPortField))
 					dstPort = Integer.parseInt(dstPortField.getText().trim());
 				else 
 					return;
 				
 				if (Validator.validatePort(srcPortField))
 					srcPort = Integer.parseInt(srcPortField.getText().trim());
 				else 
 					return;
 				
				/*if (!discoveredIPs.contains(srcIP)) {
 					Validator.setFalse(srcIPField);
 					return;
 				}
 				
 				if (!discoveredIPs.contains(srcIP)) {
 					Validator.setFalse(srcIPField);
 					return;
				}*/
 				
 				fields.getChildren().remove(traceButton);
 				fields.getChildren().add(stopButton);
 				stopButton.requestFocus();
 				
				/*if (!discoveredIPs.contains(srcIP)) {
 					Validator.setFalse(srcIPField);
 					return;
				}*/
 				
 				if (!discoveredIPs.contains(dstIP)) {
 					Validator.setFalse(dstIPField);
 					return;
 				}
 				
 				// call  trace object (inputs)
 				try {
 					//network.findPaths(srcIP, dstIP);
 					
 					traffic = new TrafficWatch(guiNodes, guiConnections, browser, nodeIPs, protocolField.getSelectionModel().getSelectedItem(), srcIP, srcPort, dstIP, dstPort);
 					Thread trafficThread = new Thread(traffic);
 					threads.add(trafficThread);
 					trafficThread.start();
 					//for(String node : nodeIPs){
 					//	puppetList.add(new NodePuppet(node, "cisco", "cisco", 6, "192.168.56.1", 0, "10.192.40.140", 80, buffer));
 					//}
 					//puppetList.add(new NodePuppet("10.192.10.120", "cisco", "cisco", 6, "192.168.56.1", 0, "10.192.40.140", 80, buffer));
 					//puppetList.add(new NodePuppet("10.192.10.110", "cisco", "cisco", 6, "192.168.56.1", 0, "10.192.40.140", 80, buffer));
 					//puppetList.add(new NodePuppet("10.192.40.140", "cisco", "cisco", 6, "192.168.56.1", 0, "10.192.40.140", 80, buffer));
 					//for(NodePuppet puppet: puppetList){
 					//	new Thread(puppet).start();
 					//	Thread.sleep(2000);
 					//}
 					//Thread.sleep(10000);
 					//buffer.printBuffer();
 					
 //					tester = new pktLoss(connections, browser, network);
 //					Thread testThread = new Thread(tester);
 //					testThread.start();
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					System.out.println("WE FAILED!!! find paths");
 					e1.printStackTrace();
 				}
 			}
 		});
 		
 		// close operation
 		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
 		    @SuppressWarnings("deprecation")
 			@Override public void handle(WindowEvent t) {
 		    	Platform.exit();
 		    	for(Thread thread: threads)
 		    		thread.interrupt();
 		    }
 		});
 		
 		// core stage
 		stage.setTitle("PathDrop - Kickass Network Visualizer & Packet Tracer");
 		stage.setScene(scene);
 		stage.getIcons().add(new Image("img/cisco_blue.png"));
 		stage.show();
 		stage.setResizable(false);
 	}
 	
 	public static void main(String[] args) { 
 		launch(args); 
 	}
 }
