 import java.awt.*;
 import java.awt.event.*;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.rmi.Naming;
 import java.rmi.server.UnicastRemoteObject;
 import javax.swing.*;
 import distributed.hash.table.*;
 
 public class DHTInteractiveClient extends JFrame{
 	private static int MaxSize = 250000;
     private int mServerCount;
     public int[] mPortMap;
     private IDistributedHashTable[] mDhtServerArray = null;
     private int mRequestId = 1;
 
 	private static final long serialVersionUID = 1L;
 	private JTextField keyBox;
 	private JTextField valueBox;
 	private JTextField serverBox;
 	private JLabel keyLabel;
 	private JLabel valueLabel;
 	private JLabel serverLabel;
 	private JLabel serverOutput;
 	private JButton execButton;
 	private JButton purgeButton;
 	private Panel resultPanel;
 	private JTextArea resultArea;
 	private JScrollPane scrollingArea;
 	private Panel upperPanel;
 	private JRadioButton deleteRadionButton;
 	private JRadioButton insertRadionButton;
 	private JRadioButton lookupRadionButton;
 	private JRadioButton countRadionButton;
     private ButtonGroup actionGroup;
 
 	private void initComponents(){
 		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setTitle("Distributed Hash Table Client");
 		keyBox = new JTextField(7);
 		valueBox = new JTextField(15);
 		serverBox = new JTextField(2);
 		keyLabel = new JLabel("Key: ");
 		keyLabel.setLabelFor(keyBox);
 		valueLabel = new JLabel("Value: ");
 		valueLabel.setLabelFor(valueBox);
 		serverLabel = new JLabel("Server Id: ");
 		serverLabel.setLabelFor(serverBox);
 		serverOutput = new JLabel("Server Output:"); 
 
 		JPanel pane = new JPanel(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		pane.add(keyLabel, c);		
 		c.gridx = 1;
 		pane.add(keyBox, c);
 		
 		c.gridx = 0;
 		c.gridy = 1;
 		pane.add(valueLabel, c);		
 		c.gridx = 1;
 		pane.add(valueBox, c);
 		
 		c.gridx = 0;
 		c.gridy = 2;
 		pane.add(serverLabel, c);		
 		c.gridx = 1;
 		pane.add(serverBox, c);
 		
 		RadioListener radioListener = new RadioListener();
 		actionGroup = new ButtonGroup();
 		insertRadionButton = new JRadioButton("Insert");
 		insertRadionButton.setSelected(true);
 		insertRadionButton.setActionCommand("insert");
 		insertRadionButton.addActionListener(radioListener);
 		lookupRadionButton = new JRadioButton("Lookup");
 		lookupRadionButton.setActionCommand("lookup");
 		lookupRadionButton.addActionListener(radioListener);
 		deleteRadionButton = new JRadioButton("Delete");
 		deleteRadionButton.setActionCommand("delete");
 		deleteRadionButton.addActionListener(radioListener);
 		countRadionButton = new JRadioButton("Count");
 		countRadionButton.setActionCommand("count");
 		countRadionButton.addActionListener(radioListener);
 		actionGroup.add(insertRadionButton);
 		actionGroup.add(lookupRadionButton);
 		actionGroup.add(deleteRadionButton);
 		actionGroup.add(countRadionButton);
 		
 		c.gridx = 2;
 		c.gridy = 0;
 		pane.add(insertRadionButton, c);		
 		c.gridy = 1;
 		pane.add(lookupRadionButton, c);
 		c.gridy = 2;
 		pane.add(deleteRadionButton, c);
 		c.gridy = 3;
 		pane.add(countRadionButton, c);
 
 
 		execButton= new JButton("Execute");
 		execButton.addActionListener(new ExecButtonListener());
 		c.gridx = 3;
 		c.gridy = 0;
 		pane.add(execButton, c);
 		
 		purgeButton	= new JButton("Purge");
 		purgeButton.addActionListener(new PurgeButtonListener());
 		c.gridx = 3;
 		c.gridy = 2;
 		pane.add(purgeButton, c);
 
 		
 		resultArea = new JTextArea(28, 70);
 		resultArea.setEditable(false);
 		scrollingArea = new JScrollPane(resultArea);
 		
 		upperPanel = new Panel(new BorderLayout());
 		upperPanel.add(pane, BorderLayout.CENTER);
 		
 		resultPanel = new Panel();
 		resultPanel.setLayout (new FlowLayout(FlowLayout.CENTER));
 		resultPanel.add(serverOutput);
 		resultPanel.add(scrollingArea);
 		
 		add(upperPanel, BorderLayout.NORTH);
 	    add(resultPanel, BorderLayout.CENTER);
 		setSize(800, 600);
         this.setVisible(true);
 	}
 	
 	private void initServers(){
 		
 		try{
 			java.net.URL path = ClassLoader.getSystemResource("serverSetting.txt");	
 			FileReader fr = new FileReader (path.getFile());
 	        BufferedReader br = new BufferedReader (fr);
 	        try {
 				String[] portMap = br.readLine().split(",");
 				mServerCount = portMap.length;
 				mPortMap = new int[mServerCount];
 				for(int i = 0; i < mServerCount; i++){
 					mPortMap[i] = Integer.parseInt(portMap[i]);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 				System.exit(-1);
 			}
 		} catch (FileNotFoundException e2) {
 			e2.printStackTrace();
 			System.exit(-1);
 		}
 		
 		mDhtServerArray = new IDistributedHashTable[mServerCount];
         for (int i = 0; i < mServerCount; i++) {
         	try{
             mDhtServerArray[i] = (IDistributedHashTable) 
             Naming.lookup("rmi://localhost:" + mPortMap[i] + "/DistributedHashTable");
         	}catch(Exception e) {
         		appendOutput("initServers: " + (i+1) + " " +  e.getMessage());
         	}
         }
 	}
 	
 	private void sendInsertRequest(int key, Object value,  int server)
 	{
 		IInsertRequest insReq = new InsertRequest(mRequestId++, server, key, value);
 		try {
 			if(mDhtServerArray[server-1] != null){
 				UnicastRemoteObject.exportObject(insReq);
 				mDhtServerArray[server-1].insert(insReq);
 				appendOutput("DHT Server:\n" + insReq.getMessage());
 			}
 			else
 				appendOutput("sendInsertRequest: server " + server + " is not initialized");
 		} catch (Exception e) {
 			appendOutput("sendInsertRequest: " + server +  e.getMessage());
 		}
 	}
 	
 	private void sendDeleteRequest(int key, int server)
 	{
 		IQueryRequest queryReq = new QueryRequest(mRequestId++, server, key);
 		try {
 			if(mDhtServerArray[server-1] != null){
 				UnicastRemoteObject.exportObject(queryReq);
 				mDhtServerArray[server-1].delete(queryReq);
 				appendOutput("DHT Server:\n" + queryReq.getMessage());
 			}
 			else
 				appendOutput("sendDeleteRequest: server " + server + " is not initialized");
 		} catch (Exception e) {
 			appendOutput("sendDeleteRequest: " + server +  e.getMessage());
 		}
 	}
 	
 	private void sendLookupRequest(int key, int server)
 	{
 		IQueryRequest queryReq = new QueryRequest(mRequestId++, server, key);
 		try {
 			if(mDhtServerArray[server-1] != null){
 				UnicastRemoteObject.exportObject(queryReq);
 				String value = (String)mDhtServerArray[server-1].lookup(queryReq);
 				appendOutput("DHT Server:\n" + queryReq.getMessage() + "\nDHT Client:\nlookup value is " + value);
 			}
 			else
 				appendOutput("sendLookupRequest: server " + server + " is not initialized");			
 		} catch (Exception e) {
 			appendOutput("sendLookupRequest: " + server +  e.getMessage());
 		}
 	}
 	
 	private void sendCountRequest(int server)
 	{
 		try {
 			if(mDhtServerArray[server-1] != null){
 				int n = mDhtServerArray[server-1].count();
 				appendOutput("Count machine " + server + " is " + n);
 			}
 			else
 				appendOutput("sendCountRequest: server " + server + " is not initialized");			
 		} catch (Exception e) {
 			appendOutput("sendCountRequest: " + server +  e.getMessage());
 		}
 	}
 
 	private void sendPurgeRequest()
 	{
 		for(int i = 0; i < mServerCount; i ++){
 			if(mDhtServerArray[i] != null){
 				try {
 					mDhtServerArray[i].purge();
 					appendOutput("purge: machine " + (i+1));
 				} catch (Exception e1) {
 					appendOutput("purge server " + (i+1) + " " + e1.getMessage());
 				}
 			}
 			else
 				appendOutput("purge server " + (i+1) + " is not initialized");	
 		}
 	}
 	
 	private void appendOutput(String msg){
 		resultArea.append(msg + "\n");
 		resultArea.append("             ******************************\n");
 	}
 	
 	class RadioListener implements ActionListener { 
         public void actionPerformed(ActionEvent e) {
         	String action = e.getActionCommand();
         	if(action == "insert"){
        		valueBox.enable(true);
        		keyBox.enable(true);
         	}
         	else if(action == "lookup"){
         		valueBox.setText("");
        		valueBox.enable(false);
        		keyBox.enable(true);
         	}
         	else if(action == "delete"){
         		valueBox.setText("");
        		valueBox.enable(false);
        		keyBox.enable(true);
         	}
         	else if(action == "count"){
         		valueBox.setText("");
         		keyBox.setText("");
        		valueBox.enable(false);
        		keyBox.enable(false);
         	}
         }
 	}
 	
 	class ExecButtonListener implements ActionListener { 
         public void actionPerformed(ActionEvent e) {
         	String action = actionGroup.getSelection().getActionCommand();
         	String key = keyBox.getText();
         	String value = valueBox.getText();
         	String server = serverBox.getText();
         	if(action == "insert"){
         		if(!validateKey()){
         			return;
         		}
         		else if(value.isEmpty()){
         			JOptionPane.showMessageDialog(null, "Please insert value");
         			return;
         		}
         		if(!validateServer()){
         			return;
         		}
         		sendInsertRequest(Integer.parseInt(key), value, Integer.parseInt(server));
         	}
         	else if(action == "lookup"){
         		if(!validateKey()){
         			return;
         		}
         		if(!validateServer()){
         			return;
         		}
         		sendLookupRequest(Integer.parseInt(key), Integer.parseInt(server));
         	}
         	else if(action == "delete"){
         		if(!validateKey()){
         			return;
         		}
         		if(!validateServer()){
         			return;
         		}
         		sendDeleteRequest(Integer.parseInt(key), Integer.parseInt(server));
         	}
         	else if(action == "count"){
         		if(!validateServer()){
         			return;
         		}
         		sendCountRequest(Integer.parseInt(server));
         	}
         }
         
         private boolean validateServer(){
         	String server = serverBox.getText();
         	if(server.isEmpty()){
     			JOptionPane.showMessageDialog(null, "Please insert server id");
     			return false;
     		}
         	if(Integer.parseInt(server) < 1 || Integer.parseInt(server) > mServerCount){
         		JOptionPane.showMessageDialog(null, "Server is not in range");
         		return false;
         	}
         	return true;
         }
         
         private boolean validateKey(){
         	String key = keyBox.getText();
         	if(key.isEmpty()){
     			JOptionPane.showMessageDialog(null, "Please insert key");
     			return false;
     		}
         	if(Integer.parseInt(key) < 1 || Integer.parseInt(key) > mServerCount * MaxSize){
         		JOptionPane.showMessageDialog(null, "Key is not in range");
         		return false;
         	}
         	return true;
         }
 	}
 	
 	class PurgeButtonListener implements ActionListener { 
         public void actionPerformed(ActionEvent e) {
         	if(JOptionPane.showConfirmDialog(null, "Are you sure to purge distributed hash table?", "Purge",
         			JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
         		sendPurgeRequest();
         	}
         }
 	}
 	
 	private void createAndShowGUI() {
 		initComponents();
 		initServers();
     }
 
 	public static void main(String[] args) {
 		final DHTInteractiveClient dhtClient = new DHTInteractiveClient();
 				
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
             	dhtClient.createAndShowGUI();
             }
         });
 
 	}
 }
