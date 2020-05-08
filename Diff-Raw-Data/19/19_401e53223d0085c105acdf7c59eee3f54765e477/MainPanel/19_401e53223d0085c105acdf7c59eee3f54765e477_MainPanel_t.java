 /*
  * Copyright (c) Novedia Group 2012.
  *
  *     This file is part of Hubiquitus.
  *
  *     Hubiquitus is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Hubiquitus is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with Hubiquitus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package main;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import org.hubiquitus.hapi.client.HClient;
 import org.hubiquitus.hapi.hStructures.HCommand;
 import org.hubiquitus.hapi.hStructures.HOptions;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * 
  * @author speed
  * @version 0.3
  * The panel for this example
  */
 
 @SuppressWarnings("serial")
 public class MainPanel extends JPanel {
 	private HClient client;
 	
 	private HOptions option = new HOptions();
 	private CallbackExample callback = new CallbackExample(this);
 
 	private JTextField usernameField = new JTextField("");
 	private JTextField passwordField = new JTextField("");
 	private JTextField endPointField = new JTextField("");
 	private JTextField serverPortField = new JTextField("");
 	private JTextField serverHostField = new JTextField("");
 	private JTextField hechoField = new JTextField("");
 	
 	private JButton connectButton = new JButton("Connection");
 	private JButton disconnectButton = new JButton("Disconnection");
 	private JButton cleanButton = new JButton("Clean");
 	private JButton hcommandButton = new JButton("hEcho");
 	
 	private JRadioButton xmppRadioButton = new JRadioButton("XMPP");
 	private JRadioButton socketRadioButton = new JRadioButton("Socket IO");
 	private ButtonGroup buttonGroup = new ButtonGroup();
 	
 	private JTextArea logArea = new JTextArea(10,70);
 	private JTextArea statusArea = new JTextArea(1,70);
 
 	public MainPanel() {
 		super();
 		initComponents();
 		initListeners();
 	}
 
 	/**
 	 * Initialization of all the component
 	 */
 	public void initComponents() {
 		BorderLayout layout = new BorderLayout();
 		this.setLayout(layout);
 		
 		
 		buttonGroup.add(xmppRadioButton);
 		buttonGroup.add(socketRadioButton);
 		xmppRadioButton.setSelected(true);
 
 		//Initialization of Labels,TextFields and RadioButtons
 		JPanel paramsPanel = new JPanel();
 		GridLayout paramsLayout = new GridLayout(8, 2);
 		paramsPanel.setLayout(paramsLayout);
 		paramsPanel.add(new JLabel("username"));
 		paramsPanel.add(usernameField);
 		paramsPanel.add(new JLabel("password"));
 		paramsPanel.add(passwordField);
 		paramsPanel.add(new JLabel("endPoint"));
 		paramsPanel.add(endPointField);
 		paramsPanel.add(new JLabel("serverHost"));
 		paramsPanel.add(serverHostField);
 		paramsPanel.add(new JLabel("serverPort"));
 		paramsPanel.add(serverPortField);
 		paramsPanel.add(new JLabel("hecho Text"));
 		paramsPanel.add(hechoField);
 		paramsPanel.add(xmppRadioButton);
 		paramsPanel.add(socketRadioButton);
 		
 		statusArea.setEditable(false);
 		paramsPanel.add(statusArea);
 		
 		//Initialization of Buttons
 		JPanel controlsPanel = new JPanel();
 		GridLayout controlsLayout = new GridLayout(1, 4);
 		controlsPanel.setLayout(controlsLayout);
 		controlsPanel.add(connectButton);
 		controlsPanel.add(disconnectButton);
 		controlsPanel.add(hcommandButton);
 		controlsPanel.add(cleanButton);
 		
 		
 		//Initialization of the TextArea
 		JPanel consolePanel = new JPanel();
 		logArea.setEditable(false);
 		consolePanel.add(logArea);
 		JScrollPane txtScrol=new JScrollPane(logArea);
 		txtScrol.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		txtScrol.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		consolePanel.add(txtScrol);
 
 		//Add all in the layout
 		this.add(paramsPanel, BorderLayout.NORTH);
 		this.add(controlsPanel, BorderLayout.CENTER);
 		this.add(consolePanel, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * Initialization of the listeners 
 	 */
 	public void initListeners() {
 		connectButton.addMouseListener(new ConnectionButtonListener());
 		disconnectButton.addMouseListener(new DisconnectionButtonListener());
 		hcommandButton.addMouseListener(new HCommandButtonListener());
 		cleanButton.addMouseListener(new CleanButtonListener());
 	}
 	
 	/**
 	 * Add a text to the TextArea
 	 * @param text
 	 */
 	public void addTextArea(String text) {
 		String tempTxt = this.logArea.getText() + "\n" + text;
 		this.logArea.setText(tempTxt);
 	}
 	
 	/**
 	 * Clean the TextArea
 	 */
 	public void cleanTextArea() {
 		this.logArea.setText("clean");
 	}
 	
 	/**
 	 * Change the status 
 	 * @param text
 	 */
 	public void setStatusArea(String text) {
 		this.statusArea.setText(text);
 	}
 	
 	// Listener of button connection
 	class ConnectionButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			
 			String endpoint = endPointField.getText();
 			option.getEndpoints().clear();
 			if (endpoint == null || endpoint.equalsIgnoreCase("")) {
 				option.setEndpoints(null);
 			} else {
 				ArrayList<String> endpoints = new ArrayList<String>();
 				endpoints.add(endpoint);
 				option.setEndpoints(endpoints);
 			}
 			String serverHost = serverHostField.getText();
 			if(serverHost != null) {
 				option.setServerHost(serverHost);
 			}
 			String serverPort = serverPortField.getText();
 			if(serverPort != null) {
 				option.setServerPort(serverPort);
 			}
 			
 			if(socketRadioButton.isSelected())
 				option.setTransport("socketio");
 			else
 				option.setTransport("xmpp");
 			client.connect(usernameField.getText(), passwordField.getText(), callback, option);
 		}
 	}
 
 	// Listener of button disconnection
 	class DisconnectionButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			client.disconnect();
 		}
 	}
 
 	// Listener of button hcommand
 	class HCommandButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			JSONObject jsonObj = new JSONObject();
 			try {
 				jsonObj.put("text",  hechoField.getText());
				HCommand cmd = new HCommand("hnode.hub.novediagroup.com","hecho",jsonObj);
 				client.command(cmd);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 	}
 	// Listener of button clean
 	class CleanButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			cleanTextArea();
 		}
 	}
 	
 	/* Getters & Setters */
 	public HClient getClient() {
 		return client;
 	}
 
 	public void setClient(HClient client) {
 		this.client = client;
 	}
 	
 	public void setTextArea(String text){
 		this.logArea.setText(text);
 	}
 
 	
 	
 	
 }
