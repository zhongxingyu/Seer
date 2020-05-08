 package de.fhkoeln.gm.wba2.phase2.xmpp_client;
 
 
 import javax.swing.BoundedRangeModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JTextField;
 import javax.swing.JList;
 import javax.swing.JTextArea;
 import javax.swing.border.LineBorder;
 import java.awt.Color;
 import javax.swing.JRadioButton;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.util.List;
 
 import javax.swing.JLabel;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.JTextPane;
 import javax.swing.ListModel;
 import javax.swing.JScrollBar;
 
 public class ClientFrame extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	
 	private JPanel contentPane;
 	private ConnectionHandler ch;
 	private JTextField textNodeName;
 	private JRadioButton rdbtnPublish;
 	private JRadioButton rdbtnSubscribe;
 	private JRadioButton rdbtnUnsubscribe;
 	private JRadioButton rdbtnDelete;
 	private JTextArea textPayload;
 	private JTextPane textMessages;
 	private JTextArea textNodeInformation;
 	private JList listSubs;
 	private JList listNodes;
 	private final ButtonGroup actionType = new ButtonGroup();
 	
 	private DefaultListModel<String> listmodel;
 	private DefaultListModel<String> listmodelsubs;
 	private JLabel lblMessages;
 	private JButton btnNewButton_2;
 
 	public ClientFrame() {
 		setResizable(false);
 		setVisible(true);
 		setEnabled(false);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 800, 600);
 		setLocationRelativeTo(null);
 		
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		listmodel = new DefaultListModel<>();
 		
 		contentPane.setLayout(null);
 
 		listNodes = new JList(listmodel);
 		listNodes.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				JList list = (JList)e.getSource();
 				int index = list.getSelectedIndex();
 				
 				if(index >= 0) {
 					listSubs.clearSelection();
 					textNodeName.setText(listmodel.get(index));
 				}
 			}
 		});
 		listNodes.setBorder(new LineBorder(new Color(0, 0, 0)));
 		listNodes.setBounds(636, 122, 104, 219);
 		contentPane.add(listNodes);
 		
 		textPayload = new JTextArea();
 		textPayload.setLineWrap(true);
 		textPayload.setBounds(57, 190, 427, 151);
 		contentPane.add(textPayload);
 		
 		textNodeName = new JTextField();
 		textNodeName.setBounds(57, 85, 104, 25);
 		contentPane.add(textNodeName);
 		textNodeName.setColumns(10);
 		
 		rdbtnPublish = new JRadioButton("Publish");
 		actionType.add(rdbtnPublish);
 		rdbtnPublish.setBounds(295, 86, 77, 23);
 		contentPane.add(rdbtnPublish);
 		
 		rdbtnSubscribe = new JRadioButton("Subscribe");
 		rdbtnSubscribe.setSelected(true);
 		actionType.add(rdbtnSubscribe);
 		rdbtnSubscribe.setBounds(170, 86, 104, 23);
 		contentPane.add(rdbtnSubscribe);
 		
 		rdbtnUnsubscribe = new JRadioButton("Unsubscribe");
 		actionType.add(rdbtnUnsubscribe);
 		rdbtnUnsubscribe.setBounds(170, 122, 120, 23);
 		contentPane.add(rdbtnUnsubscribe);
 		
 		rdbtnDelete = new JRadioButton("Delete");
 		actionType.add(rdbtnDelete);
 		rdbtnDelete.setBounds(295, 120, 77, 23);
 		contentPane.add(rdbtnDelete);
 
 		
 		JButton btnNewButton = new JButton("Senden");
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent action) {
 			
 				if(! (textNodeName.getText().length() > 0))
 					return;
 				
 				if(rdbtnPublish.isSelected()) {
 					if(ch.publishWithPayload(textNodeName.getText(), textPayload.getText())) {
						if(!listmodel.contains(textNodeName.getText()))
							listmodel.addElement(textNodeName.getText());
						
 						System.out.println("Publishing succeded!");
 					}
 					else {
 						System.err.println("Publishing failed!");
 					}
 				}
 				else if (rdbtnSubscribe.isSelected()) {
 					if(ch.subscribeToNode(textNodeName.getText())) {
 						System.out.println("Subscribing succeded!");
 						refreshLists();
 					}
 					else {
 						System.err.println("Subscribing failed!");
 					}
 				}
 				else if (rdbtnUnsubscribe.isSelected()) {
 					if(ch.unsubscribeToNode(textNodeName.getText())) {
 						System.out.println("Unsubscribing succeded!");
 						refreshLists();
 					}
 					else {
 						System.err.println("Unsubscribing failed!");
 					}
 				}
 				else if (rdbtnDelete.isSelected()) {
 					if(ch.deleteNode(textNodeName.getText())) {
 						System.out.println("Deleting succeded!");
 						refreshLists();
 					}
 					else {
 						System.err.println("Deleting failed!");
 					}
 				}
 				
 			}
 		});
 		btnNewButton.setBounds(380, 83, 104, 25);
 		contentPane.add(btnNewButton);
 		
 		JLabel lblPayload = new JLabel("Payload");
 		lblPayload.setBounds(56, 163, 70, 15);
 		contentPane.add(lblPayload);
 		
 		lblMessages = new JLabel("Empfangene Nachrichten");
 		lblMessages.setBounds(57, 370, 200, 15);
 		contentPane.add(lblMessages);
 		
 		JLabel lblAlle = new JLabel("Alle");
 		lblAlle.setBounds(636, 95, 91, 15);
 		contentPane.add(lblAlle);
 		
 		textMessages = new JTextPane();
 		textMessages.setEditable(false);
 		textMessages.setBounds(57, 397, 427, 99);
 		contentPane.add(textMessages);
 		
 		listmodelsubs = new DefaultListModel<>();
 		
 		listSubs = new JList(listmodelsubs);
 		listSubs.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				JList list = (JList)e.getSource();
 				int index = list.getSelectedIndex();
 				
 				if(index >= 0) {
 					listNodes.clearSelection();
 					textNodeName.setText(listmodelsubs.get(index));
 					textNodeInformation.setText(ch.getNodeInformation(listmodelsubs.get(index)));
 				}
 			}
 		});
 		listSubs.setBorder(new LineBorder(new Color(0, 0, 0)));
 		listSubs.setBounds(520, 123, 104, 218);
 		contentPane.add(listSubs);
 		
 		JLabel lblAbboniert = new JLabel("Abonniert");
 		lblAbboniert.setBounds(523, 95, 91, 15);
 		contentPane.add(lblAbboniert);
 		
 		JLabel lblNodeId = new JLabel("Node ID");
 		lblNodeId.setBounds(57, 58, 70, 15);
 		contentPane.add(lblNodeId);
 		
 		JLabel lblNodeInformationen = new JLabel("Node Informationen");
 		lblNodeInformationen.setBounds(523, 370, 200, 15);
 		contentPane.add(lblNodeInformationen);
 		
 		textNodeInformation = new JTextArea();
 		textNodeInformation.setBounds(520, 397, 220, 99);
 		contentPane.add(textNodeInformation);
 		
 		JScrollPane scrollpane = new JScrollPane(textNodeInformation);
 		scrollpane.setBounds(520,  397,  220,  99);
 		contentPane.add(scrollpane);
 		
 		btnNewButton_2 = new JButton("Aktu.");
 		btnNewButton_2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent a) {
 				refreshLists();
 			}
 		});
 		btnNewButton_2.setBounds(380, 119, 104, 25);
 		contentPane.add(btnNewButton_2);
 	}
 
 	
 	private void refreshLists() {
 		/* Fill listmodel with all existing nodes */
 		List<String> entries = ch.getAllNodes();
 		listmodel.clear();
 		
 		for(String curr: entries) {
 			listmodel.addElement(curr);
 		}
 		
 		/* Fill listmodelsubs with all subscribed nodes */
 		List<String> subs_entries = ch.getSubscribedNodes();
 		listmodelsubs.clear();
 		
 		for(String curr: subs_entries) {
 			listmodelsubs.addElement(curr);
 		}
 	}
 	
 	
 	public void setConnectionHandler(ConnectionHandler ch) {
 		this.ch = ch;
 
 		this.ch.addItemListener(new ItemLoggingListener(textMessages));
 		
 		refreshLists();
 	}
 }
