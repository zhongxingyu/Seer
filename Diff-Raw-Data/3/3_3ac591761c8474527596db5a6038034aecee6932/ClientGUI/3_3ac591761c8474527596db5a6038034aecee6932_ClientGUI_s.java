 package client.gui;
 
 import java.io.*;
 import java.util.*;
 import java.util.List;
 import java.awt.*;
 import java.awt.event.*;
 import java.text.*;
 
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.event.*;
 
 import util.xml.*;
 import util.xml.message.*;
 import util.xml.message.Message;
 
 import org.apache.log4j.*;
 import org.apache.log4j.xml.*;
 
 import client.thread.*;
 
 public class ClientGUI extends JFrame implements Observer, Runnable {
 	
 	private static final Logger logger = Logger.getLogger("im.client");
 	private static final long serialVersionUID = 4229L;
 	private static final String SIMPLE = "SimpleMessage";
 	private static final String AUTH = "AuthorizeMessage";
 	private static final String USERS = "UserListMessage";
 	private static final String ANSWER = "AnswerMessage";
 	private static final String HISTORY = "HistoryMessage";
 	private static final String FULL_HISTORY = "FullHistoryMessage";
 	private static final String CONNECT = "ConnectUserMessage";
 	
 	private JTextField input;
 	private JTextArea messages;
 	private JScrollPane scroll;
 	private JButton sendButton;
 	private JList<String> users;
 	private DefaultListModel<String> usersModel = new DefaultListModel<String>();
 	
 	private PrintWriter pipedOut;
 	private DataOutputStream socketOut; 
 	
 	private String userName;
 	private String selectedName;
 	private Map<String, List<String>> history;
 	private List<String> nonReadedUsers;
 	
 	public ClientGUI(PrintWriter output, String name, DataOutputStream out) {
 		super("Client - " + name);
 		
 		pipedOut = output;
 		socketOut = out;
 		userName = name;
 		history = new Hashtable<String, List<String>>();
 		nonReadedUsers = new ArrayList<>();
 	}
 	
 	@SuppressWarnings( "unchecked" )
 	public void update(Observable source, Object object) {
 		Message message = (Message) object;
 		switch (message.getType()) {
 			case SIMPLE:
 				MessageType receivedMessage = (MessageType) message.getValue();
 				
 				Date time = receivedMessage.getTime();
 				String from = checkName(receivedMessage.getFromUser());
 				String to = receivedMessage.getToUser();
 				String text = receivedMessage.getMessage();
 				
 				//if(!from.equals(selectedName)) {
 				//	if(!from.equals(userName)) {
 				//		Object[] options = { "Yes", "No" };
 				//		int n = JOptionPane.showOptionDialog(this, "New message from " + from, "New message", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
 				//		
 				//		users.setSelectedValue(from, true);
 				//	}
 				//}
 				
 				if (!text.isEmpty()) {
 					logger.info("Receiving message from server...");
 					if (selectedName != null && (from.equals(selectedName) || from.equals(userName))) {
 						messages.append(DateFormat.getDateInstance().format(time) + " (" + from + ") : " + text + "\n");
 					} else {
                         if (!nonReadedUsers.contains(from)) {
                         	usersModel.removeElement(from);
                             usersModel.addElement(from + "*");
                             nonReadedUsers.add(from);
                         }
 					}
 					
 					List<String> list = null;
 					if (history.containsKey(from)) {
 						list = history.get(from);
 					} else {
 						list = new ArrayList<String>();
 						history.put(from, list);
 					}
 					list.add(text);
 					
 				}
 				break;
 			case USERS:
 				logger.info("Receiving users list from server...");
 				
 				ArrayList<String> users = (ArrayList<String>) message.getValue();
 				
 				usersModel.clear();				
 					
 				for (int i = 0; i < users.size(); i++) {
 					usersModel.addElement(users.get(i));
 				}
 				
 				usersModel.removeElement(userName);
 				break;
 			case HISTORY:
 				logger.info("Receiving history from server...");
 				ArrayList<String> list = (ArrayList<String>) message.getValue();
 				history.put(selectedName, list); 
 				
 				messages.setText("");
 				for (String str : list) {
 					messages.append(str + "\n");
 				}
 				break;	
 			case FULL_HISTORY:
 				logger.info("Receiving full history from server...");
 				history = (Map<String, List<String>>) message.getValue();
 				break;
 			case ANSWER:
 				logger.info("Receiving answer from server...");
 				String answer = (String) message.getValue();
 				if (answer.equals("AUTH_FAIL")) {
 					
 				}
 				break;
 		}
 	}
 	
 	@Override
 	public void run() {
 		logger.info("Creating frame...");
 		createFrame();
 	}
 	
 	@Override
 	public Dimension getPreferredSize() {
 		return new Dimension(640, 480);
 	}
 	
 	@Override
 	public Dimension getMinimumSize() {
 		return getPreferredSize();
 	}
 	
 	private void createFrame() {
 		input = new JTextField();
 		input.setEnabled(false);
 		
 		messages = new JTextArea();
 		messages.setEditable(false);
 		
 		scroll = new JScrollPane(messages);
 		
 		sendButton = new JButton("Send");
 		sendButton.setEnabled(false);
 		sendButton.addActionListener(new SendButtonListener(input, pipedOut));
 		
 		input.addActionListener(new SendButtonListener(input, pipedOut));
 		
 		users = new JList<String>(usersModel);
 		
 		ListSelectionListener listSelection = new ListSelectionListener() {
 		
 			@Override
 			public void valueChanged(ListSelectionEvent event) {
 				selectedName = checkName(users.getSelectedValue());
 				
 				input.setEnabled(true);
 				sendButton.setEnabled(true);
 				messages.setText("");
 				logger.info("Selected user - " + selectedName);
 				
 				if (nonReadedUsers.contains(selectedName)) {
 					nonReadedUsers.remove(selectedName);
 				}
 				if(selectedName != null) {
 					logger.info("Loading history...");
 					if (history.containsKey(selectedName)) {
 						logger.info("Loading local history...");
 						List<String> list = history.get(selectedName);
 						for (String str : list) {
 							messages.append(str + "\n");
 						}
 					} else {
 						try {
 							logger.info("Sending request to server to load history...");
 							Operations.sendConnectUser(selectedName, socketOut);
 						} catch (Exception e) {
 							logger.warn("Failed to load history");
 						}
 					}
 				}
 			}
 		};
 		
 		users.addListSelectionListener(listSelection);
 		
 		JPanel panel = new JPanel(new BorderLayout());
 		panel.add(input, BorderLayout.CENTER);
 		panel.add(sendButton, BorderLayout.EAST);
 		
 		JPanel leftSplitPanel = new JPanel(new BorderLayout());
 		leftSplitPanel.add(scroll, BorderLayout.CENTER);
 		leftSplitPanel.add(panel, BorderLayout.SOUTH);
 		
 		JPanel rightSplitPanel = new JPanel(new BorderLayout());
 		rightSplitPanel.setMinimumSize(new Dimension(150, 400));
 		rightSplitPanel.add(users, BorderLayout.CENTER);
 		
 		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftSplitPanel, rightSplitPanel);
 		splitPane.setDividerLocation(450);
 		
 		getContentPane().add(splitPane, BorderLayout.CENTER);
 		
 		addWindowListener(new WindowAdapter() {
 			
 			@Override
 			public void windowActivated(WindowEvent e) {
 				logger.info("Activating window...");
 				loadHistory();
 			}
 			
 			@Override
 			public void windowClosing(WindowEvent e) {
 				logger.info("Closing window...");
 				saveHistory();
 				setVisible(false);
 				System.exit(0);
 			}
 		});
 		
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		pack();
 		setVisible(true);
 	}	
 	
 	private class SendButtonListener implements ActionListener {
 		
 		private PrintWriter output;
 		private JTextField input;
 		
 		public SendButtonListener(JTextField input, PrintWriter output) {
 			this.output = output;
 			this.input = input;
 		}
 		
 		public void actionPerformed(ActionEvent event) {
 			logger.info("Sending message...");
 			output.println(userName);
 			output.println(selectedName);
 			output.println(input.getText());
 			input.setText("");
 		}
 	}
 	
 	private void saveHistory() {
 		String fileName = "client_history/" + userName + ".hst";
 		try {
 			logger.info("Trying to save history at file : " + fileName);
 			File file = new File(fileName);
 			FileOutputStream out = new FileOutputStream(file);
 			DataOutputStream dataOut = new DataOutputStream(out);
 		
 			Operations.sendFullHistory(history, dataOut);
 		} catch (Exception e) {
 			logger.warn("Failed to save history at file : " + fileName);
 		}
 	}
 	
 	private void loadHistory() {
 		String fileName = "client_history/" + userName + ".hst";
 		try {
 			logger.info("Trying to load history from file : " + fileName);
 			File file = new File(fileName);
 			FileInputStream in = new FileInputStream(file);
 			DataInputStream dataIn = new DataInputStream(in);
 			
 			Message message = Operations.receive(dataIn);
 			update(null, message);
 		} catch (Exception e) {
 			logger.warn("Failed to load history from file : " + fileName);
 		}
 	}
 	
 	private String checkName(String name) {
 		if (name.endsWith("*")) {
 			return name.substring(0, name.length() - 2);
 		}
         return name;
 	}
 
 }
