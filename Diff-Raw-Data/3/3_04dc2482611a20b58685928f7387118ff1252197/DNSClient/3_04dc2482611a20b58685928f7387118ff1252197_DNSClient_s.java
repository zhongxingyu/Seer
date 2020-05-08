 /* **************************************************************************
  *                                                                          *
  *  Copyright (C)  2011  Nils Foken, Andr Kielich,                        *
  *                       Peter Kossek, Hans Laser                           *
  *                                                                          *
  *  Nils Foken       <nils.foken@it2009.ba-leipzig.de>                      *
  *  Andr Kielich   <andre.kiesslich@it2009.ba-leipzig.de>                 *
  *  Peter Kossek     <peter.kossek@it2009.ba-leipzig.de>                    *
  *  Hans Laser       <hans.laser@it2009.ba-leipzig.de>                      *
  *                                                                          *
  ****************************************************************************
  *                                                                          *
  *  This file is part of 'javadns'.                                         *
  *                                                                          *
  *  This project is free software: you can redistribute it and/or modify    *
  *  it under the terms of the GNU General Public License as published by    *
  *  the Free Software Foundation, either version 3 of the License, or       *
  *  any later version.                                                      *
  *                                                                          *
  *  This project is distributed in the hope that it will be useful,         *
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of          *
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           *
  *  GNU General Public License for more details.                            *
  *                                                                          *
  *  You should have received a copy of the GNU General Public License       *
  *  along with this project. If not, see <http://www.gnu.org/licenses/>.    *
  *                                                                          *
  ****************************************************************************/
 
 package de.baleipzig.javadns;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 import java.util.HashMap;
 
 import javax.naming.directory.Attribute;
 import javax.swing.AbstractButton;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.EmptyBorder;
 
 /**
  * This is the DNSClient that can communicate with the DNSServer.
  * It is a JFrame.
  */
 @SuppressWarnings("serial")
 public class DNSClient extends JFrame implements ActionListener {
 	/** Action command to reset the server. */
 	private static final String RESET = "RESET";
 	/** Action command to look up a host's attribute */
 	private static final String LOOKUP = "LOOKUP";
 	/** Action command to register the client */
 	private static final String REGISTER = "REGISTER";
 	/** System's line separator */
 	private static final String LINE_SEPARATOR = System
 			.getProperty("line.separator");
 	private JTextField txfName;
 	private JTextArea textArea;
 	private JTextField txfDnsIP;
 	private JComboBox cmbxOTHER;
 	private final ButtonGroup btngrpRecordType = new ButtonGroup();
 	private JTextField txfDnsPort;
 	private JRadioButton rdbtnOTHER;
 
 	public DNSClient() {
 		/* 
 		 * setze die minimale Gre des Fensters auf 600x500
 		 * vergebe Fenstertitel und LookAndFeel
 		 */
 		setMinimumSize(new Dimension(600, 500));
 		setTitle("DNS Client");
 		try {
 			// try to set system's look & feel
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			// simply ignore if it doesn't work
 		}
 		
 		// Beende das Programm beim schlieen durch [x]
 		// exit application on closing event
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		getContentPane().setLayout(new BorderLayout(0, 0));
 		
 		// create layout and contents
 
 		/*
 		 * das MainPanel erhlt Rnder und ein GridBagLayout
 		 * Positionen werden ber die Constraints angegeben
 		 * Bentigte Felder werden ff erstellt
 		 */
 		JPanel pnlMainPanel = new JPanel();
 		pnlMainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
 		getContentPane().add(pnlMainPanel, BorderLayout.CENTER);
 		GridBagLayout gbl_pnlMainPanel = new GridBagLayout();
 		gbl_pnlMainPanel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
 		gbl_pnlMainPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
 		gbl_pnlMainPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0,
 				Double.MIN_VALUE };
 		gbl_pnlMainPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,
 				0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
 		pnlMainPanel.setLayout(gbl_pnlMainPanel);
 
 		JLabel lblDns = new JLabel("DNS IP:");
 		GridBagConstraints gbc_lblDns = new GridBagConstraints();
 		gbc_lblDns.anchor = GridBagConstraints.EAST;
 		gbc_lblDns.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDns.gridx = 0;
 		gbc_lblDns.gridy = 0;
 		pnlMainPanel.add(lblDns, gbc_lblDns);
 
 		txfDnsIP = new JTextField();
 		txfDnsIP.setText("127.0.0.1");
 		GridBagConstraints gbc_txfDnsIP = new GridBagConstraints();
 		gbc_txfDnsIP.gridwidth = 3;
 		gbc_txfDnsIP.insets = new Insets(0, 0, 5, 0);
 		gbc_txfDnsIP.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txfDnsIP.gridx = 1;
 		gbc_txfDnsIP.gridy = 0;
 		pnlMainPanel.add(txfDnsIP, gbc_txfDnsIP);
 		txfDnsIP.setColumns(10);
 
 		JLabel lblNewLabel = new JLabel("DNS Port:");
 		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
 		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
 		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel.gridx = 0;
 		gbc_lblNewLabel.gridy = 1;
 		pnlMainPanel.add(lblNewLabel, gbc_lblNewLabel);
 
 		txfDnsPort = new JTextField();
 		txfDnsPort.setText("53");
 		GridBagConstraints gbc_txfDnsPort = new GridBagConstraints();
 		gbc_txfDnsPort.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txfDnsPort.gridwidth = 3;
 		gbc_txfDnsPort.insets = new Insets(0, 0, 5, 0);
 		gbc_txfDnsPort.gridx = 1;
 		gbc_txfDnsPort.gridy = 1;
 		pnlMainPanel.add(txfDnsPort, gbc_txfDnsPort);
 		txfDnsPort.setColumns(10);
 
 		JLabel lblName = new JLabel("Name:");
 		GridBagConstraints gbc_lblName = new GridBagConstraints();
 		gbc_lblName.insets = new Insets(0, 0, 5, 5);
 		gbc_lblName.anchor = GridBagConstraints.EAST;
 		gbc_lblName.gridx = 0;
 		gbc_lblName.gridy = 2;
 		pnlMainPanel.add(lblName, gbc_lblName);
 
 		txfName = new JTextField();
 		txfName.setText("google.de");
 		GridBagConstraints gbc_txfName = new GridBagConstraints();
 		gbc_txfName.gridwidth = 3;
 		gbc_txfName.insets = new Insets(0, 0, 5, 0);
 		gbc_txfName.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txfName.gridx = 1;
 		gbc_txfName.gridy = 2;
 		pnlMainPanel.add(txfName, gbc_txfName);
 		txfName.setColumns(10);
 		
 		// radio buttons for common record types
 
 		JLabel lblRequestType = new JLabel("Record Type:");
 		GridBagConstraints gbc_lblRequestType = new GridBagConstraints();
 		gbc_lblRequestType.anchor = GridBagConstraints.EAST;
 		gbc_lblRequestType.insets = new Insets(0, 0, 5, 5);
 		gbc_lblRequestType.gridx = 0;
 		gbc_lblRequestType.gridy = 3;
 		pnlMainPanel.add(lblRequestType, gbc_lblRequestType);
 
 		JRadioButton rdbtnA = new JRadioButton("A (IPv4)");
 		rdbtnA.setActionCommand("A");
 
 		rdbtnA.setSelected(true);
 		btngrpRecordType.add(rdbtnA);
 		GridBagConstraints gbc_rdbtnA = new GridBagConstraints();
 		gbc_rdbtnA.anchor = GridBagConstraints.WEST;
 		gbc_rdbtnA.insets = new Insets(0, 0, 5, 5);
 		gbc_rdbtnA.gridx = 1;
 		gbc_rdbtnA.gridy = 3;
 		pnlMainPanel.add(rdbtnA, gbc_rdbtnA);
 
 		JRadioButton rdbtnNS = new JRadioButton("NS (name server)");
 		rdbtnNS.setActionCommand("NS");
 		btngrpRecordType.add(rdbtnNS);
 		GridBagConstraints gbc_rdbtnNS = new GridBagConstraints();
 		gbc_rdbtnNS.gridwidth = 2;
 		gbc_rdbtnNS.anchor = GridBagConstraints.WEST;
 		gbc_rdbtnNS.insets = new Insets(0, 0, 5, 0);
 		gbc_rdbtnNS.gridx = 2;
 		gbc_rdbtnNS.gridy = 3;
 		pnlMainPanel.add(rdbtnNS, gbc_rdbtnNS);
 
 		JRadioButton rdbtnAAAA = new JRadioButton("AAAA (IPv6)");
 		rdbtnAAAA.setActionCommand("AAAA");
 		btngrpRecordType.add(rdbtnAAAA);
 		GridBagConstraints gbc_rdbtnAAAA = new GridBagConstraints();
 		gbc_rdbtnAAAA.anchor = GridBagConstraints.WEST;
 		gbc_rdbtnAAAA.insets = new Insets(0, 0, 5, 5);
 		gbc_rdbtnAAAA.gridx = 1;
 		gbc_rdbtnAAAA.gridy = 4;
 		pnlMainPanel.add(rdbtnAAAA, gbc_rdbtnAAAA);
 
 		JRadioButton rdbtnRP = new JRadioButton("RP (responsible person)");
 		rdbtnRP.setActionCommand("RP");
 		btngrpRecordType.add(rdbtnRP);
 		GridBagConstraints gbc_rdbtnRP = new GridBagConstraints();
 		gbc_rdbtnRP.gridwidth = 2;
 		gbc_rdbtnRP.anchor = GridBagConstraints.WEST;
 		gbc_rdbtnRP.insets = new Insets(0, 0, 5, 0);
 		gbc_rdbtnRP.gridx = 2;
 		gbc_rdbtnRP.gridy = 4;
 		pnlMainPanel.add(rdbtnRP, gbc_rdbtnRP);
 
 		JRadioButton rdbtnLOC = new JRadioButton("LOC (geographical location)");
 		rdbtnLOC.setActionCommand("LOC");
 		btngrpRecordType.add(rdbtnLOC);
 		GridBagConstraints gbc_rdbtnLOC = new GridBagConstraints();
 		gbc_rdbtnLOC.anchor = GridBagConstraints.WEST;
 		gbc_rdbtnLOC.insets = new Insets(0, 0, 5, 5);
 		gbc_rdbtnLOC.gridx = 1;
 		gbc_rdbtnLOC.gridy = 5;
 		pnlMainPanel.add(rdbtnLOC, gbc_rdbtnLOC);
 
 		JRadioButton rdbtnTXT = new JRadioButton("TXT (text)");
 		rdbtnTXT.setActionCommand("TXT");
 		btngrpRecordType.add(rdbtnTXT);
 		GridBagConstraints gbc_rdbtnTXT = new GridBagConstraints();
 		gbc_rdbtnTXT.gridwidth = 2;
 		gbc_rdbtnTXT.anchor = GridBagConstraints.WEST;
 		gbc_rdbtnTXT.insets = new Insets(0, 0, 5, 0);
 		gbc_rdbtnTXT.gridx = 2;
 		gbc_rdbtnTXT.gridy = 5;
 		pnlMainPanel.add(rdbtnTXT, gbc_rdbtnTXT);
 
 		JRadioButton rdbtnMX = new JRadioButton("MX (mail exchange)");
 		rdbtnMX.setActionCommand("MX");
 		btngrpRecordType.add(rdbtnMX);
 		GridBagConstraints gbc_rdbtnMX = new GridBagConstraints();
 		gbc_rdbtnMX.anchor = GridBagConstraints.WEST;
 		gbc_rdbtnMX.insets = new Insets(0, 0, 5, 5);
 		gbc_rdbtnMX.gridx = 1;
 		gbc_rdbtnMX.gridy = 6;
 		pnlMainPanel.add(rdbtnMX, gbc_rdbtnMX);
 
 		rdbtnOTHER = new JRadioButton("other:");
 		rdbtnOTHER.setActionCommand("OTHER");
 
 		btngrpRecordType.add(rdbtnOTHER);
 		GridBagConstraints gbc_rdbtnOTHER = new GridBagConstraints();
 		gbc_rdbtnOTHER.anchor = GridBagConstraints.WEST;
 		gbc_rdbtnOTHER.insets = new Insets(0, 0, 5, 5);
 		gbc_rdbtnOTHER.gridx = 2;
 		gbc_rdbtnOTHER.gridy = 6;
 		pnlMainPanel.add(rdbtnOTHER, gbc_rdbtnOTHER);
 
 		cmbxOTHER = new JComboBox();
 		cmbxOTHER.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				// automatically select radio button
 				// that belongs to combo box
 				rdbtnOTHER.setSelected(true);
 			}
 		});
 		// fill combo box with values
 		cmbxOTHER.setModel(new DefaultComboBoxModel(new String[] { "AFSDB",
 				"APL", "CERT", "CNAME", "DHCID", "DLV", "DNAME", "DNSKEY",
 				"DS", "HIP", "IPSECKEY", "KEY", "KX", "NAPTR", "NSEC", "NSEC3",
 				"NSEC3PARAM", "PTR", "RRSIG", "SIG", "SOA", "SPF", "SRV",
 				"SSHFP", "TA", "TKEY", "TSIG" }));
 		GridBagConstraints gbc_txfOTHER = new GridBagConstraints();
 		gbc_txfOTHER.insets = new Insets(0, 0, 5, 0);
 		gbc_txfOTHER.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txfOTHER.gridx = 3;
 		gbc_txfOTHER.gridy = 6;
 		pnlMainPanel.add(cmbxOTHER, gbc_txfOTHER);
 
 		JPanel pnlButtonPanel = new JPanel();
 		GridBagConstraints gbc_pnlButtonPanel = new GridBagConstraints();
 		gbc_pnlButtonPanel.anchor = GridBagConstraints.WEST;
 		gbc_pnlButtonPanel.gridwidth = 3;
 		gbc_pnlButtonPanel.fill = GridBagConstraints.VERTICAL;
 		gbc_pnlButtonPanel.insets = new Insets(0, 0, 5, 5);
 		gbc_pnlButtonPanel.gridx = 1;
 		gbc_pnlButtonPanel.gridy = 7;
 		pnlMainPanel.add(pnlButtonPanel, gbc_pnlButtonPanel);
 		pnlButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 
 		// buttons
 		
 		JButton btnStartLookup = new JButton("Start Lookup");
 		pnlButtonPanel.add(btnStartLookup);
 		btnStartLookup.setActionCommand(LOOKUP);
 
 		JButton btnResetServer = new JButton("Reset Server");
 		pnlButtonPanel.add(btnResetServer);
 		btnResetServer.setActionCommand(RESET);
 
 		JButton btnRegister = new JButton("Register");
 		btnRegister.setActionCommand(REGISTER);
 		btnRegister.addActionListener(this);
 		
 		pnlButtonPanel.add(btnRegister);
 		btnResetServer.addActionListener(this);
 		btnStartLookup.addActionListener(this);
 		
 		// log area
 
 		JLabel lblResponse = new JLabel("Log:");
 		GridBagConstraints gbc_lblResponse = new GridBagConstraints();
 		gbc_lblResponse.anchor = GridBagConstraints.NORTHEAST;
 		gbc_lblResponse.insets = new Insets(0, 0, 0, 5);
 		gbc_lblResponse.gridx = 0;
 		gbc_lblResponse.gridy = 8;
 		pnlMainPanel.add(lblResponse, gbc_lblResponse);
 
 		JScrollPane scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridwidth = 3;
 		gbc_scrollPane.gridx = 1;
 		gbc_scrollPane.gridy = 8;
 		pnlMainPanel.add(scrollPane, gbc_scrollPane);
 
 		textArea = new JTextArea();
 		textArea.setEditable(false);
 		textArea.setLineWrap(true);
 		textArea.setWrapStyleWord(true);
 		scrollPane.setViewportView(textArea);
 		textArea.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,
 				null, null));
 
 		JLabel lblStatus = new JLabel("Some status label");
 		getContentPane().add(lblStatus, BorderLayout.SOUTH);
 
 		// center on screen
 		setLocationRelativeTo(null);
 		// show frame
 		setVisible(true);
 	}
 
 	/**
 	 * Append some text to the log area followed by a line separator.
 	 * @param text The text to add.
 	 */
 	private void appendText(String text) {
 		textArea.append(text + LINE_SEPARATOR);
 		textArea.setCaretPosition(textArea.getText().length());
 	}
 
 	public static void main(String[] args) {
 		/*
 		 * erzeuge ein Instanz von DNSClient
 		 */
 		new DNSClient();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent evt) {
 		String dnsAddress = txfDnsIP.getText();
 		int dnsPort;
 		try {
 			dnsPort = Integer.parseInt(txfDnsPort.getText());
 		} catch (NumberFormatException e) {
 			JOptionPane.showMessageDialog(null, "The port must be a number!",
 					"Input error", JOptionPane.ERROR_MESSAGE);
 			txfDnsPort.requestFocus();
 			txfDnsPort.selectAll();
 			return;
 		}
 
 		String recordType = "";
 		String lookupName = txfName.getText();
 
 		// create request
 		Request request = null;
 		if (evt.getActionCommand().equals(LOOKUP)) {
 			// find selected button and get record type
 			JRadioButton selectedButton = null;
 			Enumeration<AbstractButton> elements = btngrpRecordType
 					.getElements();
 			while (elements.hasMoreElements()) {
 				AbstractButton nextElement = elements.nextElement();
 				if (nextElement instanceof JRadioButton
 						&& nextElement.isSelected()) {
 					selectedButton = (JRadioButton) nextElement;
 				}
 			}
 			if (selectedButton != null) {
 				recordType = selectedButton.getActionCommand();
 				if (recordType.equals(rdbtnOTHER.getActionCommand())) {
 					recordType = cmbxOTHER.getSelectedItem().toString();
 				}
 			}
 			request = new Request(lookupName, recordType);
 		} else if (evt.getActionCommand().equals(RESET)) {
 			// create reset request
 			request = new Request();
 		} else if (evt.getActionCommand().equals(REGISTER)) {
 			// open dialog to specify record entries
 			RegisterDialog dialog = new RegisterDialog();
 			if (dialog.getButtonClicked() == RegisterDialog.OK) {
 				String hostName = dialog.getHostName();
 				HashMap<String,Attribute> map = dialog.getAttributes();
 				request = new Request(hostName, map);
 			}
 		}
 
 		try {
 			appendText(">> Sending request: "+request.toString());
 			// send request and receive response
 			String response = sendRequest(request, dnsAddress, dnsPort);
 			if (response.isEmpty())
 				appendText("<< The result was empty or there was no result at all."
 						+ LINE_SEPARATOR);
 			else
 				appendText("<< "+response + LINE_SEPARATOR);
 		} catch (ClassNotFoundException e) {
 			JOptionPane.showMessageDialog(null, e.getMessage(),
 					"The server sent rubbish", JOptionPane.ERROR_MESSAGE);
 		} catch (UnknownHostException e) {
 			JOptionPane.showMessageDialog(null, e.getMessage(), "Host unknown",
 					JOptionPane.ERROR_MESSAGE);
 			return;
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(null, e.getMessage(), "IO Error",
 					JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 	}
 
 	/**
 	 * Sends a Request
 	 * 
 	 * @param request The request to send.
 	 * @param targetAddress Address to send the request to.
 	 * @param targetPort Port to send the request to.
 	 * @return A message from the server.
 	 * @throws UnknownHostException
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	private String sendRequest(Request request, String targetAddress,
 			int targetPort) throws UnknownHostException, IOException,
 			ClassNotFoundException {
 		Socket socket = null;
 		// create socket to target
 		socket = new Socket(targetAddress, targetPort);
 		// get outputstream to write to
 		ObjectOutputStream oos = new ObjectOutputStream(
 				socket.getOutputStream());
 		// send request object
 		oos.writeObject(request);
 		// flush the stream
 		oos.flush();
 
 		// open inputstream for reading
 		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
 		String response;
 		// read response string
 		response = (String) ois.readObject();
 		// close the socket
 		socket.close();
 		return response;
 	}
 
 }
