 import java.awt.*;
 import java.awt.event.*;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import javax.swing.*;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 
 /** 
  * CS158B Project 3
  * 
  * Client side gui that will interact with the tcp server and network
  * elements
  * 
  * @author TeamCPD 
  *
  */
 public class ClientGui extends JPanel implements ActionListener{
 
 	private static Socket s;
 	private static ObjectOutputStream oos;
 	private ObjectInputStream ois;
 
 	protected static JTextField textFieldHost;
 	protected static JTextField textFieldPort;
 	protected static JTextField textFieldOID;
 	protected static JTextField textFieldCommStr;
     protected JTextArea textArea;
     protected JTextArea textAreaAlarm;
     protected JComboBox comboBoxMethods;
     protected static JTextField textFieldSet;
     private static SNMP snmp = new SNMP();//empty SNMP Object
     
     private static JTable table;
     private static AbstractTableModel abstractTableModel;
     private static Hashtable<String, String> hashAclTable;
     
     
     private static MyTableModel aclTable = new MyTableModel();
 
     
     private String snmpAgentStatus = "ON"; // For the Enable/Disable button snmpAgent disable and enable
     							 // Status: 0, snmpAgent Enabled
     							 // Status: 1, snmpAgent Disabled
     
     private final static String newline = "\n";
 
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) 
 	{
 		//Schedule a job for the event dispatch thread:
         //creating and showing this application's GUI.
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
             	createAndShowGUI();
             }
         });
 	}
 
 	/**
 	 * Create the application GUI.
 	 */
 	public ClientGui() {
 		super(new GridBagLayout());
 
 		//created new textfield
         textFieldHost = new JTextField(20);
         textFieldHost.setToolTipText("HostID goes here!");
 
         //created new textfield
         textFieldPort = new JTextField(20);
         textFieldPort.setToolTipText("Port goes here!");
 
 		//created new textfield
         textFieldOID = new JTextField(20);
         textFieldOID.setToolTipText("OID goes here!");
         //textField.addActionListener(this); //attach listener
  
         //created new textfield
         textFieldCommStr = new JTextField(20);
         textFieldCommStr.setToolTipText("Community String goes here!");
         
         //create new button
         JButton b = new JButton("Submit");
         b.setToolTipText("Click to submit");
         b.addActionListener(this); //add action listener to this button
         
         //create button alarm
         JButton getAlarmButton = new JButton("Get Alarms");
         getAlarmButton.setToolTipText("Press to get RMON Alarms"); //To be honest this should be setting the trap and then the alarms come in...
         getAlarmButton.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent evt)
             {
 				try {
 					// check HOST and PORT first
 					if (!(textFieldHost.getText().equals("")
 							|| textFieldPort.getText().equals("") || textFieldCommStr
 							.getText().equals(""))) {
 
 						Socket s = new Socket(textFieldHost.getText(), Integer
 								.parseInt(textFieldPort.getText()));// host,
 																	// port
 
 						Hashtable<String, String> ht = new Hashtable<String, String>();
 
 						ht.put(textFieldOID.getText(), textFieldOID.getText());
 
 						snmp = new SNMP("1", textFieldCommStr.getText(), "1",
 								"GET", ht);
 
 						// setting the flag to true to get events
 						snmp.setFlag();
 						snmp.setStatus(snmpAgentStatus);
 
 						// create the OutputStream to write
 						ObjectOutputStream oos = new ObjectOutputStream(s
 								.getOutputStream());
 						// write the SNMP object to server
 						oos.writeObject(snmp);
 
 						// Now Wait for response
 						ObjectInputStream ois = new ObjectInputStream(s
 								.getInputStream());
						Object obj = ois.readObject();
						if (obj instanceof String){
							//String response = (String) obj;
							textAreaAlarm.append((String)obj + newline);
							textAreaAlarm.setCaretPosition(textAreaAlarm.getDocument().getLength());
							return;
						}
						else{
 
 						// get the RMONevent response
						ArrayList<RMONEvent> response =(ArrayList<RMONEvent>) obj;
 
 						for (int i = 0; i < response.size(); i++) {
 							// System.out.println(response.vBinding.get(OID));
 							textAreaAlarm.append(response.get(i).toString()
 									+ newline);
 							textAreaAlarm.setCaretPosition(textAreaAlarm
 									.getDocument().getLength());
						}}
 
 					} else {
 						textArea.append("Host, Port, or Community not entered."
 								+ newline);
 						textArea.setCaretPosition(textArea.getDocument()
 								.getLength());
 					}
 
 
 				} catch (UnknownHostException e) {
 					// TODO Auto-generated catch block
 					// System.out.println("Unknown Host");
 					textAreaAlarm.append("Unknown Host" + newline);
 					textAreaAlarm.setCaretPosition(textAreaAlarm.getDocument()
 							.getLength());
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					// System.out.println("Error retreving data");
 					textAreaAlarm.append("Error retreving data" + newline);
 					textAreaAlarm.setCaretPosition(textAreaAlarm.getDocument()
 							.getLength());
 				} catch (ClassNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		});
 
         //create button Enable/Disable SNMP
         JButton snmpEnableDisableButton = new JButton("Enable/Disable SNMP Agent");
         snmpEnableDisableButton.setToolTipText("Press to enable/disable SNMP agent (Default: SNMP agent enabled)");        
         snmpEnableDisableButton.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent evt)
             {
 
 				try {
 					
 					//check HOST and PORT first
 					if (!(textFieldHost.getText().equals("") 
 							|| textFieldPort.getText().equals(""))) 
 					{
 
 						Socket s = new Socket(textFieldHost.getText(), Integer
 								.parseInt(textFieldPort.getText()));
 						
 						Hashtable<String, String> ht = new Hashtable<String, String>();
 
 						ht.put(textFieldOID.getText(), textFieldOID.getText());
 
 						snmp = new SNMP("1", textFieldCommStr.getText(), "1",
 								"SET", ht);
 
 						snmp.setFlag();
 						
 
 						// setting the SNMP Agent status, enable/disable
 						if (snmpAgentStatus.equalsIgnoreCase("ON")) {
 							// if on set off
 							snmp.setStatus("OFF");
 							snmpAgentStatus = "OFF";
 						} else {
 							// otherwise turn on
 							snmp.setStatus("ON");
 							snmpAgentStatus = "ON";
 						}
 
 						// create the OutputStream to write
 						ObjectOutputStream oos = new ObjectOutputStream(s
 								.getOutputStream());
 						// write the SNMP object to server
 						oos.writeObject(snmp);
 
 						// Now Wait for response
 						ObjectInputStream ois = new ObjectInputStream(s
 								.getInputStream());
 					
 						String response =(String) ois.readObject();
 						
 						
 						// System.out.println(response.vBinding.get(OID));
 						// textAreaAlarm.append(response.get(i).toString() +
 						// newline);
 						textArea.append(response
 								+ newline);
 						textArea.setCaretPosition(textArea.getDocument()
 								.getLength());
 					}
 					else //socket not connected
 					{
 						textArea.append("Host or Port not entered."
 								+ newline);
 						textArea.setCaretPosition(textArea.getDocument()
 								.getLength());
 					}
 					
 				} catch (UnknownHostException e) {
 					// TODO Auto-generated catch block
 					// System.out.println("Unknown Host");
 					textArea.append("Unknown Host" + newline);
 					textArea.setCaretPosition(textAreaAlarm.getDocument()
 							.getLength());
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					// System.out.println("Error retreving data");
 					textArea.append("Error retreving data" + newline);
 					textArea.setCaretPosition(textAreaAlarm.getDocument()
 							.getLength());
 				} catch (ClassNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		});
         
         //create button Enable/Disable SNMP
         JButton modifyACLButton = new JButton("Modify Access Control List");
         modifyACLButton.setToolTipText("Press to modify access control list for community");        
         modifyACLButton.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent evt)
             {
         		try {
 					
 					//check HOST and PORT first
 					if (!(textFieldHost.getText().equals("") 
 							|| textFieldPort.getText().equals(""))
 							|| textFieldCommStr.getText().equals("")) 
 					{
 						//request table data
 						Socket s = new Socket(textFieldHost.getText(), Integer
 								.parseInt(textFieldPort.getText()));
 						
 						Hashtable<String, String> ht = new Hashtable<String, String>();
 
 						ht.put(textFieldOID.getText(), textFieldOID.getText());
 
 						snmp = new SNMP("1", textFieldCommStr.getText(), "1",
 								"GET", ht);
 
 						//snmp.setFlag();
 						snmp.setACL();
 
 						/*// setting the SNMP Agent status, enable/disable
 						if (snmpAgentStatus.equalsIgnoreCase("ON")) {
 							// if on set off
 							snmp.setStatus("OFF");
 							snmpAgentStatus = "OFF";
 						} else {
 							// otherwise turn on
 							snmp.setStatus("ON");
 							snmpAgentStatus = "ON";
 						}*/
 
 						// create the OutputStream to write
 						ObjectOutputStream oos = new ObjectOutputStream(s
 								.getOutputStream());
 						// write the SNMP object to server
 						oos.writeObject(snmp);
 
 						
 						/* Once we get the data (HashTable) then we populate table*/
 						
 						// Now Wait for response
 						ObjectInputStream ois = new ObjectInputStream(s
 								.getInputStream());
 						Object obj = ois.readObject();
 						if(obj instanceof String)
 							System.out.println((String)obj);
 						else{
 						Hashtable<String, String> response =
 								(Hashtable<String, String>) obj;
 						
 						
 						//then we try to populate table
 						createTableGUI(response);}
 					}
 					else
 					{
 						textArea.append("Host, Port, Community not entered."
 								+ newline);
 						textArea.setCaretPosition(textArea.getDocument()
 								.getLength());
 					}
 					
 				} catch (UnknownHostException e) {
 					// TODO Auto-generated catch block
 					// System.out.println("Unknown Host");
 					textArea.append("Unknown Host" + newline);
 					textArea.setCaretPosition(textAreaAlarm.getDocument()
 							.getLength());
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					// System.out.println("Error retreving data");
 					textArea.append("Error retreving data" + newline);
 					textArea.setCaretPosition(textAreaAlarm.getDocument()
 							.getLength());
 				} catch (ClassNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 			}
 		});
         
         
         //create new text area
         textArea = new JTextArea(5, 20);
         textArea.setEditable(false);
         JScrollPane scrollPane = new JScrollPane(textArea);
  
         //create new text area for alarms
         textAreaAlarm = new JTextArea(5,20);
         textArea.setEditable(false);
         JScrollPane scrollPaneAlarm = new JScrollPane(textAreaAlarm);
         
         /*Tabbed Output Area*/
         JTabbedPane tabbedPane = new JTabbedPane();
          
         //JComponent panel1 = makeTextPanel("Output Console");
         tabbedPane.addTab("Output Console", null, scrollPane,
                 "Output Console");
         tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
          
         //JComponent panel2 = makeTextPanel("Alarms");
         tabbedPane.addTab("Alarms", null, scrollPaneAlarm,
                 "Output Alarm Console");
         tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
          
         //The following line enables to use scrolling tabs.
         tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
         
         
         //create combobox, for get and set
         String[] methodStr = { "GET", "SET" };
         //Create the combo box, select the item at index 4.
         //Indices start at 0, so 4 specifies the pig.
         comboBoxMethods = new JComboBox(methodStr);
         comboBoxMethods.setToolTipText("Choose GET or SET method");
         comboBoxMethods.setSelectedIndex(0);
         //add listener to combobox
         comboBoxMethods.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e)
             {
                 if ( ((String) comboBoxMethods.getSelectedItem()).equalsIgnoreCase("set") 
                 		&& (snmpAgentStatus.equalsIgnoreCase("ON")))
                 {
                 	createAndShowPopUp();
                 }
             }
         });
         
         
         //Add Components to this panel.
         GridBagConstraints c = new GridBagConstraints();
         c.gridwidth = GridBagConstraints.REMAINDER;
  
         //Host
         add(new JLabel("Host ")); //add label for OID
         c.fill = GridBagConstraints.HORIZONTAL;
         add(textFieldHost, c); //Host textfield
         
         //Port
         add(new JLabel("Port ")); //add label for OID
         c.fill = GridBagConstraints.HORIZONTAL;
         add(textFieldPort, c); //Port textfield
         
         //OID
         add(new JLabel("OID ")); //add label for OID
         c.fill = GridBagConstraints.HORIZONTAL;
         add(textFieldOID, c); //OID textfield
         
         //Community String
         add(new JLabel("Community String ")); //add label for community string
         c.fill = GridBagConstraints.HORIZONTAL;
         add(textFieldCommStr, c); //Community String community string
         
         //GET and SET method
         add(new JLabel("GET or SET ")); //add label for methods
         c.fill = GridBagConstraints.HORIZONTAL;
         add(comboBoxMethods, c); //combo box selection
                 
         //JPanel for buttons
         JPanel buttonPane = new JPanel();
         buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
         buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         buttonPane.add(Box.createHorizontalGlue());
         buttonPane.add(b);
         buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
         buttonPane.add(getAlarmButton);
         buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
         buttonPane.add(snmpEnableDisableButton);
         buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
         buttonPane.add(modifyACLButton);
         //add the button panel
         add(buttonPane, c);
         
         
         //add Output scrollPane
         c.fill = GridBagConstraints.BOTH;
         c.weightx = 1.0;
         c.weighty = 1.0;
         add(tabbedPane, c);
 	}
 
 	/**
      * Create the GUI and show it.  For thread safety,
      * this method should be invoked from the
      * event dispatch thread.
      */
     private static void createAndShowGUI() 
     {
         //Create and set up the window.
         JFrame frame = new JFrame("Client Gui");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         final MibBrowser mibBrowser = new MibBrowser();
         mibBrowser.getTree().addTreeSelectionListener(new TreeSelectionListener() {
 				public void valueChanged(TreeSelectionEvent e) 
 				{
 					String[] OID = mibBrowser.getTree().getLastSelectedPathComponent()
 							.toString().split("-");
 					
 					textFieldOID.setText( OID[1].trim() );
 				}
 		});
         
         JSplitPane clientguiPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
         		mibBrowser, new ClientGui());
         
         //frame.add(new MibBrowser(), BorderLayout.WEST);
         //Add contents to the window.
         //frame.add(new ClientGui(), BorderLayout.CENTER);
  
         frame.add(clientguiPane);
  
         //Display the window.
         //frame.pack(); // this packs all the components in the frame
         frame.setSize(920, 580);
         frame.setLocationRelativeTo(null); //this makes the window appear at the center
         frame.setVisible(true);
     }
     
     /**
      * Table for the Access Control List
      * @param response 
      */
     private static void createTableGUI(Hashtable<String, String> response)
     {
     	hashAclTable = response;
     	
     	//Create and set up the window.
         final JFrame frame = new JFrame("Access Control List");
         frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
         
         //got my jpanel
 		JPanel tablePane = new JPanel(new GridBagLayout());
 
 		//aclTable = new MyTableModel();
 		//aclTable.clearTable();
 		//Object[] newData = {"password", ""};
 		//Object[] newData1 = {"wordword", ""};
  		//aclTable.addData(newData);
 		//aclTable.addData(newData1);
 
 		//populate table with hashtable
 		aclTable.populateTable(hashAclTable);
 		
 		/*JTable*/ table = new JTable(aclTable);
 
 		//Table Column
 		TableColumn aclTypeColumn = table.getColumnModel().getColumn(1); 
 		
 		JComboBox<String> aclTypeComboBox = new JComboBox<String>();
 		aclTypeComboBox.addItem("RO");
 		aclTypeComboBox.addItem("RW");
 		aclTypeComboBox.setSelectedIndex(0); //the first option
 		aclTypeColumn.setCellEditor(new DefaultCellEditor(aclTypeComboBox));
 		
 		
 		//attach action listener for table
 		table.getModel().addTableModelListener(new TableModelListener(){
 
 			public void tableChanged(TableModelEvent evt) 
 			{
 		        //AbstractTableModel model = (AbstractTableModel)evt.getSource();
 		        abstractTableModel = (AbstractTableModel)evt.getSource();
 		        //model.fireTableRowsInserted(
 		        //		aclTable.getColumnCount()-1, aclTable.getColumnCount()-1);
 				
 				int row = evt.getFirstRow();
 		        int column = evt.getColumn();
 		        //String columnName = model.getColumnName(column);
 		        Object data = abstractTableModel.getValueAt(row, column);
 
 		        abstractTableModel.setValueAt(data, row, column);
 		        abstractTableModel.fireTableCellUpdated(row, column);
 			}
 		});
 
 
 
 		//aclTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
 		//aclTable.setFillsViewportHeight(true);
 
 		// Create the scroll pane and add the table to it.
 		JScrollPane scrollPane = new JScrollPane(table);
 
 		//create button new row button
         JButton newRowButton = new JButton("Add New Row");
         newRowButton.setToolTipText("Press to add a new empty row to the table");        
         newRowButton.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent evt)
             {
         		Object[] newData = {"", ""};//empty data
          		aclTable.addData(newData);
          		table.revalidate();
          		table.repaint();
          		aclTable.fireTableDataChanged();
             }
 		});
 		
         //create button submit data
         JButton submitButton = new JButton("Submit");
         submitButton.setToolTipText("Press to submit data");        
         submitButton.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent evt)
             {
         		//This is were we write back to the Server about
         		//The table change
         		
 				try {
 					/*
 					// create the OutputStream to write
 					ObjectOutputStream oos;
 					 oos = new ObjectOutputStream(s
 							.getOutputStream());
 					*/
 					
 					for (int i = 0; i < aclTable.getData().length; i++)
 	        		{
 	        			hashAclTable.put((String) aclTable.getData()[i][0], (String) aclTable.getData()[i][1]);
 	        		}
 	        		
 					//checking socket connection
 					Socket s = new Socket(textFieldHost.getText(), Integer
 							.parseInt(textFieldPort.getText()));
 					
 					Hashtable<String, String> ht = new Hashtable<String, String>();
 
 					ht.put(textFieldOID.getText(), textFieldOID.getText());
 
 					snmp = new SNMP("1", textFieldCommStr.getText(), "1",
 							"SET", ht);
 
 					//snmp.setFlag();
 					snmp.setACL();
 					
 					// create the OutputStream to write
 					ObjectOutputStream oos = new ObjectOutputStream(s
 							.getOutputStream());
 					// write the SNMP object to server
 					oos.writeObject(snmp);
 					oos.flush();
 					
 					// write the Hashtable object to server
 					oos.writeObject(hashAclTable);
 					oos.flush();
 					
 					frame.dispose();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}				
         		
 			}
 		});
         
         
         //JPanel for buttons
         JPanel buttonPane = new JPanel();
         buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
         buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         buttonPane.add(Box.createHorizontalGlue());
         buttonPane.add(newRowButton);
         buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
         buttonPane.add(submitButton);
         
 		//Add Components to this panel.
         GridBagConstraints c = new GridBagConstraints();
         c.gridwidth = GridBagConstraints.REMAINDER;
         c.fill = GridBagConstraints.BOTH;
         c.weightx = 1.0;
         c.weighty = 1.0;
 
 		// Add the scroll pane to this panel.
 		tablePane.add(scrollPane, c);
 		tablePane.add(buttonPane, c);
 		
 		// Add contents to the window.
 		frame.add(tablePane);
         
         //Display the window.
         frame.pack(); // this packs all the components in the frame
         //frame.setSize(400, 400);
         frame.setLocationRelativeTo(null); //this makes the window appear at the center
         frame.setVisible(true);
 
     }
     
     
     /**
      * Create the GUI for the Set method of the combo box
      */
     private static void createAndShowPopUp() 
     {
     	//Create and set up the window.
         final JFrame frame = new JFrame("SET Method");
         frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
         
         
         //got my jpanel
     	JPanel methodGUI = new JPanel(new GridBagLayout());
     	
     	//created new textfield
         textFieldSet = new JTextField(20);
         textFieldSet.setToolTipText("Please enter in a value!");
     	
         //create new button
         JButton b = new JButton("Submit");
         b.setToolTipText("Click to submit");
         b.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e)
             {
                 frame.dispose();
             }
         });      
         
         //Add Components to this panel.
         GridBagConstraints c = new GridBagConstraints();
         c.gridwidth = GridBagConstraints.REMAINDER;
  
         //SET
         methodGUI.add(new JLabel("SET value ")); //add label for OID
         c.fill = GridBagConstraints.HORIZONTAL;
         methodGUI.add(textFieldSet, c); //SET value
                 
         c.fill = GridBagConstraints.HORIZONTAL;
         methodGUI.add(b, c); //SET value
         
         //Add contents to the window.
         frame.add(methodGUI);
  
         
         //Display the window.
         frame.pack(); // this packs all the components in the frame
         //frame.setSize(400, 400);
         frame.setLocationRelativeTo(null); //this makes the window appear at the center
         frame.setVisible(true);
 
     }
     
     /**
      * ActionListener for this class, ClientGUI
      */
     public void actionPerformed(ActionEvent evt) {
 		String OID = textFieldOID.getText();
 		String CommStr = textFieldCommStr.getText();
 		String methodGetOrSet = (String) comboBoxMethods.getSelectedItem();
 
 		try {
 			
 
 			// check HOST and PORT first
 			if (!(textFieldHost.getText().equals("")
 					|| textFieldPort.getText().equals("") || textFieldCommStr
 					.getText().equals(""))) {
 				
 				if(snmpAgentStatus.equalsIgnoreCase("OFF"))// we should be able to assume that the agent is disabled
 				{
 					textArea.append("SNMP Agent is " + snmpAgentStatus
 							+ newline);
 
 					textArea.setCaretPosition(textArea.getDocument()
 							.getLength());
 					return;
 				}
 
 				System.out.println(textFieldHost.getText() + ":"
 						+ Integer.parseInt(textFieldPort.getText()));
 
 				Socket s = new Socket(textFieldHost.getText(),
 						Integer.parseInt(textFieldPort.getText()));// host, port
 
 				if (methodGetOrSet.equalsIgnoreCase("get")) {
 					Hashtable<String, String> ht = new Hashtable<String, String>();
 
 					ht.put(OID, OID);
 
 					snmp = new SNMP("1", CommStr, "1", "GET", ht);
 				} else {
 
 					Hashtable<String, String> ht = new Hashtable<String, String>();
 
 					ht.put(OID, textFieldSet.getText());
 
 					snmp = new SNMP("1", CommStr, "1", "SET", ht);
 
 				}
 
 				// gotta do another check for snmpStatusAgent
 
 				if (snmpAgentStatus.equals("ON")) {
 					ObjectOutputStream oos = new ObjectOutputStream(
 							s.getOutputStream());
 
 					// write the SNMP object to server
 					oos.writeObject(snmp);
 					oos.flush();
 
 					ObjectInputStream ois = new ObjectInputStream(
 							s.getInputStream());
 					
 					Object obj = ois.readObject(); 
 					if(obj instanceof String){
 						textArea.append((String)obj + newline);
 
 						textArea.setCaretPosition(textArea.getDocument()
 								.getLength());
 						return;
 					}
 
 					// get the SNMP response
 					SNMP response = (SNMP) obj;
 
 					// System.out.println(response.vBinding.get(OID));
 					textArea.append(response.vBinding.get(OID) + newline);
 
 					textArea.setCaretPosition(textArea.getDocument()
 							.getLength());
 				} 
 
 			} else {
 				textArea.append("Host, Port, Community not entered." + newline);
 				textArea.setCaretPosition(textArea.getDocument().getLength());
 			}
 			
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			//System.out.println("Unknown Host");
 			textArea.append("Unknown Host" + newline);
 	        textArea.setCaretPosition(textArea.getDocument().getLength());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			//System.out.println("Error retreving data");
 			textArea.append("Error retreving data" + newline);
 	        textArea.setCaretPosition(textArea.getDocument().getLength());
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}// end of action listener
     
 }// end of class
