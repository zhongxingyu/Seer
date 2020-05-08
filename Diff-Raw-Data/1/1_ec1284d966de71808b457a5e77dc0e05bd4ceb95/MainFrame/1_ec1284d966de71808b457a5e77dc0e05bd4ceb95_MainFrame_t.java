 package gui;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import main.Engine;
 import main.Functions;
 import main.Message;
 
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.text.SimpleDateFormat;
 import java.util.LinkedList;
 
 public class MainFrame extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	private JPanel contentPane;
 	private JTextField inputField;
 	private Engine engine;
 	private JTextArea textArea;
 
 	private String chatroom;
 	// Name of the last sender
 	private String lastSender = "";
 
 	//The only message to be preserved when the view is refreshed;
 	private String entryMessage;
 	
 	public LinkedList<Message> messageList = new LinkedList<Message>();
 
 	public MainFrame(Engine e) {
 		engine = e;
 		chatroom = e.chatroom;
 
 		setTitle("CHATROOM : " + chatroom);
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 600, 450);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		textArea = new JTextArea();
 		textArea.setBounds(15, 15, 570, 355);
 		contentPane.add(textArea);
 		entryMessage = Functions.getTime(new SimpleDateFormat("(HH:mm:ss)"))
 				+ ": Welcome to chatroom \"" + chatroom + "\", " + engine.name;
 		textArea.append(entryMessage);
 		textArea.append("\n");
 
 		textArea.setEditable(false);
 
 		inputField = new JTextField();
 		inputField.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
 					send(inputField.getText());
 					inputField.setText("");
 				}
 			}
 		});
 		inputField.setBounds(15, 380, 570, 28);
 		contentPane.add(inputField);
 		inputField.setColumns(10);
 	}
 
 	private void send(String s) {
 		Message m = new Message(s, engine.name);
 		messageList.add(m);
 		engine.tempMessage = s;
 		engine.sendRequest(s.length());
		update();
 	}
 
 	//Refreshes the view
 	public void update() {
 		textArea.setText(entryMessage);
 		textArea.append("\n");
 		for (Message m : messageList) {
 			if (lastSender.equalsIgnoreCase(m.sender)) {
 				textArea.append("\n");
 				textArea.append(m.format());
 			} else {
 				textArea.append("\n");
 				textArea.append("\n");
 				textArea.append(m.sender);
 				textArea.append("\n");
 				textArea.append(m.format());
 				lastSender = m.sender;
 			}
 		}
 	}
 }
