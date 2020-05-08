 package gui;
 
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.Queue;
 import java.util.Vector;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.JViewport;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.Timer;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 
 
 import core.abstraction.Controller;
 import core.im.IM;
 import core.whiteboard.WhiteboardPanel;
 
 public class MessageDialog implements ListDataListener{
 
 	String userName;
 	JDialog conversation;
 	JPanel messagePanel;
 	JTextArea inputArea;
 	Vector<Controller>listeners;
 	private JTextPane convoWindow;
 	private Dimension defaultSize = new Dimension(600,400);
 	
 	public MessageDialog(){
 		conversation = new JDialog(null, "Conversation", Dialog.ModalityType.MODELESS);
 		setupGUI();
 		conversation.pack();
 		conversation.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		conversation.setVisible(true);
 		listeners = new Vector<Controller>();
 		userName = "";
 	}
 	
 	public MessageDialog(String dialogName){
 		userName = dialogName;
 		conversation = new JDialog(null, dialogName, Dialog.ModalityType.MODELESS);
 		messagePanel = new JPanel();
 		setupGUI();
 		conversation.pack();
 		conversation.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		conversation.setVisible(true);
 		listeners = new Vector<Controller>();
 	}
 	
 	public void addController(Controller controller)
 	{
 		listeners.add(controller);
 	}
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		MessageDialog test = new MessageDialog();
 	}
 
 	private void setupGUI(){
 	//	BoxLayout bl = new BoxLayout(conversation.getContentPane(), BoxLayout.Y_AXIS);
 		BoxLayout bl2 = new BoxLayout(messagePanel, BoxLayout.Y_AXIS);
 	//	conversation.setLayout(bl); 
 		messagePanel.setLayout(bl2);
 		conversation.addWindowListener(new WindowListener() {
 			
 			@Override
 			public void windowOpened(WindowEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowIconified(WindowEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowDeiconified(WindowEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowDeactivated(WindowEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowClosing(WindowEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowClosed(WindowEvent e) {
 				// TODO Auto-generated method stub
 				for(int i = 0; i < listeners.size(); i++)
 				{
					
 					listeners.get(i).removeDialog(userName);
 				}
 				
 			}
 			
 			@Override
 			public void windowActivated(WindowEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		//TODO: for adding a menu later
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.add(new JMenu("One"));
 		menuBar.add(new JMenu("Two"));
 		menuBar.add(new JMenu("Three"));
 		conversation.setJMenuBar(menuBar);
 		
 		JPanel topPanel = new JPanel();
 		JPanel separatorPanel = new JPanel();
 		JPanel bottomPanel = new JPanel();
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 	//	convoPanel.setPreferredSize(new Dimension(300, 200));
 		topPanel.setLayout(new BorderLayout(0,5));
 		bottomPanel.setLayout(new BorderLayout(0,5));
 //		scrollWindow.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 //		scrollWindow.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		convoWindow = new JTextPane();
 		convoWindow.setEditable(false);
 		convoWindow.setPreferredSize(new Dimension(300, 200));
 		
 
 		JScrollPane scrollWindow = new JScrollPane(convoWindow);
 		//TODO: Will need a method that keeps a large string of the conversation and inserts usernames and shit
 		
 		scrollWindow.getViewport().add(convoWindow);
 		topPanel.add(scrollWindow);
 		//conversation.add(convoPanel);
 		inputArea = new JTextArea();
 		inputArea.setPreferredSize(new Dimension(300, 150));
 		inputArea.addKeyListener(new KeyListener(){
 		
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 				// TODO Auto-generated method stub
 			}
 		
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				if(arg0.getKeyCode() == KeyEvent.VK_ENTER){
 					if(!arg0.isShiftDown()){
 						sendMessage(inputArea);
 						arg0.consume();
 					}
 					else{
 						inputArea.append("\n");
 					}
 				}
 				
 			}
 		});
 		
 		bottomPanel.add(inputArea);
 		messagePanel.add(topPanel);
 		messagePanel.add(separatorPanel);
 		messagePanel.add(bottomPanel);
 		messagePanel.add(Box.createHorizontalStrut(3));
 		
 		JButton sendMsgButton = new JButton("Send Message");
 		sendMsgButton.addActionListener(new ActionListener(){
 		
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				sendMessage(inputArea);				
 			}
 		});
 	
 		JButton openWbButton = new JButton("Whiteboard >");
 		openWbButton.addActionListener(new ActionListener(){
 		
 			@Override
 			public void actionPerformed(ActionEvent e) {
 		            WhiteboardDialog wb = new WhiteboardDialog(userName, convoWindow.getText(), messagePanel, inputArea, convoWindow, listeners);
 		            //will remove the dialog
 		            for(int i = 0; i < listeners.size(); i++)
 					{
 						listeners.get(i).removeDialog(userName);
 						//listeners.get(i).addDialog(wb, userName);
 					}
 		            conversation.dispose();
 			}
 		});
 		
 		buttonPanel.add(sendMsgButton);
 		buttonPanel.add(openWbButton);
 		//conversation.add(Box.createVerticalGlue());
 		
 		conversation.add(messagePanel, BorderLayout.EAST);
 		conversation.add(buttonPanel, BorderLayout.SOUTH);
 	}
 
 	@Override
 	public void contentsChanged(ListDataEvent e) {
 		
 	}
 
 	@Override
 	public void intervalAdded(ListDataEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void intervalRemoved(ListDataEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	//TODO: currently just clears the box... eventually make it do other stuff
 	private void sendMessage(JTextArea inputField){
 		String message = inputField.getText();
 		for(int i = 0; i < listeners.size(); i++)
 		{
 			boolean b = listeners.get(i).sendMessage(userName, message);
 			if(b == false)
 				System.out.println("Error, could not send");
 			else
 				System.out.println(message);
 		}
 		
 		inputField.setText("");
 		//TODO:fix the user's username
 		convoWindow.setText(convoWindow.getText() + "\n" + "me!" + ":  " + message);
 	}
 	
 	public void receiveMessage(IM im)
 	{
 		//if needed we can map im.from to client aliases
 		String newSentence = im.from + ":  "+ im.message;
 		convoWindow.setText(convoWindow.getText() + "\n" + newSentence);
 		System.out.println(im.message);
 	}
 	
 	
 }
