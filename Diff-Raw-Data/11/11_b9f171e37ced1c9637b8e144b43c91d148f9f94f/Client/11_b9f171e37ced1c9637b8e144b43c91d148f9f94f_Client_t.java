 package pudgewars.network;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.net.Socket;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.text.DefaultCaret;
 
 import pudgewars.Window;
 
 public class Client {
 	static JTextArea chatWindow, onlineClients1, onlineClients2;
 	static JTextField message, name;
 	static JLabel welcome;
 	static JButton readyButton, cancelButton, changeTeamButton;
 	private static MyConnection conn;
 
 	public JPanel createContentPane() {
 		JPanel pane = new JPanel();
 		pane.setLayout(null);
 
 		welcome = new JLabel("");
 		welcome.setLocation(10, 10);
 		welcome.setSize(300, 30);
 		pane.add(welcome);
 
 		JLabel label1 = new JLabel("Chat Window");
 		label1.setLocation(10, 50);
 		label1.setSize(100, 30);
 		pane.add(label1);
 
 		JLabel label2 = new JLabel("Online Players");
 		label2.setLocation(380, 50);
 		label2.setSize(100, 30);
 		pane.add(label2);
 
 		JLabel label3 = new JLabel("Team 1");
 		label3.setLocation(380, 70);
 		label3.setSize(50, 30);
 		pane.add(label3);
 
 		JLabel label4 = new JLabel("Team 2");
 		label4.setLocation(380, 190);
 		label4.setSize(50, 30);
 		pane.add(label4);
 
 		chatWindow = new JTextArea();
 		chatWindow.setLineWrap(true);
 		chatWindow.setWrapStyleWord(true);
 		chatWindow.setEditable(false);
 
 		JScrollPane chatWindowScroll = new JScrollPane(chatWindow);
 		chatWindowScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		chatWindowScroll.setBounds(10, 80, 350, 230);
 		pane.add(chatWindowScroll);
 
 		DefaultCaret caret = (DefaultCaret) chatWindow.getCaret();
 		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
 
 		onlineClients1 = new JTextArea();
 		onlineClients1.setLineWrap(true);
 		onlineClients1.setWrapStyleWord(true);
 		onlineClients1.setEditable(false);
 
 		onlineClients2 = new JTextArea();
 		onlineClients2.setLineWrap(true);
 		onlineClients2.setWrapStyleWord(true);
 		onlineClients2.setEditable(false);
 
 		JScrollPane onlineClientsScroll1 = new JScrollPane(onlineClients1);
 		onlineClientsScroll1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		onlineClientsScroll1.setBounds(380, 95, 150, 100);
 		pane.add(onlineClientsScroll1);
 
 		JScrollPane onlineClientsScroll2 = new JScrollPane(onlineClients2);
 		onlineClientsScroll2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		onlineClientsScroll2.setBounds(380, 215, 150, 100);
 		pane.add(onlineClientsScroll2);
 
 		message = new JTextField();
 		message.addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 					if (!message.getText().trim().equals("")) {
 						conn.sendMessage(message.getText().trim() + "\nEOM");
 						if (message.getText().trim().equals("/q")) {
 							System.exit(0);
 						}
 						message.setText("");
 					}
 					message.requestFocusInWindow();
 				}
 			}
 		});
 
 		message.setBounds(10, 320, 350, 35);
 		pane.add(message);
 
 		changeTeamButton = new JButton("Change Team");
 		changeTeamButton.setBounds(380, 10, 150, 35);
 		changeTeamButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				conn.sendMessage("/ct\nEOM");
 			}
 		});
 		pane.add(changeTeamButton);
 
 		readyButton = new JButton("Ready");
 		readyButton.setBounds(380, 320, 150, 35);
 
 		cancelButton = new JButton("Cancel");
 		cancelButton.setBounds(380, 320, 150, 35);
 		cancelButton.setVisible(false);
 		cancelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				cancelButton.setVisible(false);
 				readyButton.setVisible(true);
 				changeTeamButton.setEnabled(true);
 				conn.sendMessage("/cs \nEOM");
 			}
 		});
 		pane.add(cancelButton);
 
 		readyButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				readyButton.setVisible(false);
 				cancelButton.setVisible(true);
 				changeTeamButton.setEnabled(false);
 				conn.sendMessage("/cs Ready\nEOM");
 			}
 		});
 		pane.add(readyButton);
 
 		pane.setOpaque(true);
 		return pane;
 	}
 
 	private static void createAndShowGUI() {
 		JFrame frame = new JFrame("Pudge Wars Client");
 
 		Client chat = new Client();
 		frame.setContentPane(chat.createContentPane());
 
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setBounds(400, 100, 550, 390);
 		frame.setResizable(false);
 		frame.setVisible(true);
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				conn.sendMessage("/q\nEOM");
 			}
 		});
 
 		message.requestFocusInWindow();
 	}
 
 	private static class getMessage implements Runnable {
 		public void run() {
 			try {
 				while (true) {
 					String msg = conn.getMessage();
 					if (msg.equals("ClientsInfo1")) {
 						onlineClients1.setText("");
 						msg = conn.getMessage();
 						while (!msg.equals("ClientsInfo2")) {
 							onlineClients1.setText(onlineClients1.getText() + msg + "\n");
 							msg = conn.getMessage();
 						}
 						onlineClients2.setText("");
 						msg = conn.getMessage();
 						while (!msg.equals("EOM")) {
 							onlineClients2.setText(onlineClients2.getText() + msg + "\n");
 							msg = conn.getMessage();
 						}
 					} else if (msg.equals("NAME")) {
 						msg = conn.getMessage();
 						welcome.setText("Welcome to Pudge Wars, " + msg + "!");
 						conn.getMessage();
 					} else if (msg.equals("SERVERERROR")) {
 						msg = conn.getMessage();
 						JOptionPane.showMessageDialog(null, "The server is full or there is an ongoing game. Try again later.");
 						System.exit(1);
 					} else if (msg.equals("FULLERROR")) {
 						msg = conn.getMessage();
 						JOptionPane.showMessageDialog(null, "The other team is already full.");
 					} else if (msg.equals("TEAMERROR")) {
 						msg = conn.getMessage();
 						JOptionPane.showMessageDialog(null, "There should be at least one person in both teams to start a game.");
 						cancelButton.setVisible(false);
 						readyButton.setVisible(true);
 						changeTeamButton.setEnabled(true);
 					} else if (msg.equals("START")) {
 						conn.getMessage();
 						conn.sendMessage("STOP");
 						Window w = new Window(conn);
 						w.startClientGame();
 					} else {
 						do {
 							chatWindow.setText(chatWindow.getText() + msg + "\n");
 							msg = conn.getMessage();
 						} while (!msg.equals("EOM"));
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static void main(String args[]) {
		String host = JOptionPane.showInputDialog(null, "Enter host:", "127.0.0.1");
		// String host = "127.0.0.1";
 		createAndShowGUI();
 		chatWindow.setText("Connecting to host: " + host + "...\n");
 		try {
 			Socket socket = new Socket(host, 8888);
 			conn = new MyConnection(socket);
 
 			Thread t = new Thread(new getMessage());
 			t.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(null, "Unable to connect to host: " + host + ". Server not initialized.");
 			System.exit(1);
 		}
 	}
 }
