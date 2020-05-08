 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.table.DefaultTableModel;
 
 public class LobbyWindow {
 
 	public JFrame frmLobby;
 	private JTable gameList;
 	private JTextField textField;
 	private JTextArea chatWindow;
 	private JTextField tableNameField;
 	private JSpinner spinner;
 	
 	public LobbyWindow() {
 		initialize();
 	}
 	
 	@SuppressWarnings("serial")
 	private void initialize() {
 
 		frmLobby = new JFrame();
 		frmLobby.setResizable(false);
 		frmLobby.setTitle("Game Lobby");
 		frmLobby.setBounds(100, 100, 800, 500);
 		frmLobby.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmLobby.getContentPane().setLayout(null);
 		
 		JScrollPane scrollPane_1 = new JScrollPane();
 		scrollPane_1.setBounds(50, 50, 500, 325);
 		frmLobby.getContentPane().add(scrollPane_1);
 		
 		gameList = new JTable();
 		gameList.setAutoCreateRowSorter(true);
 		gameList.setModel(new DefaultTableModel(
 			new Object[][] {},
 			new String[] {
 				"Table Number", "Table Name", "Players", "Status"
 			}
 		) {
 			boolean[] columnEditables = new boolean[] {
 				false, false, false, false
 			};
 			public boolean isCellEditable(int row, int column) {
 				return columnEditables[column];
 			}
 		});
 		gameList.getColumnModel().getColumn(0).setResizable(false);
 		gameList.getColumnModel().getColumn(0).setPreferredWidth(75);
 		gameList.getColumnModel().getColumn(1).setResizable(false);
 		gameList.getColumnModel().getColumn(1).setPreferredWidth(150);
 		gameList.getColumnModel().getColumn(2).setResizable(false);
 		gameList.getColumnModel().getColumn(2).setPreferredWidth(75);
 		gameList.getColumnModel().getColumn(3).setResizable(false);
 		gameList.getColumnModel().getColumn(3).setPreferredWidth(100);
 		gameList.setFillsViewportHeight(true);
 		scrollPane_1.setViewportView(gameList);
 		
 		JButton btnNewButton = new JButton("Create Game");
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				createGame();
 			}
 		});
 		btnNewButton.setBounds(340, 386, 100, 44);
 		frmLobby.getContentPane().add(btnNewButton);
 		
 		JButton btnNewButton_1 = new JButton("Join Table");
 		btnNewButton_1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				joinTable();
 			}
 		});
 		btnNewButton_1.setBounds(450, 386, 100, 44);
 		frmLobby.getContentPane().add(btnNewButton_1);
 		
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setBounds(575, 50, 200, 350);
 		frmLobby.getContentPane().add(scrollPane);
 		
 		chatWindow = new JTextArea();
 		chatWindow.setEditable(false);
 		chatWindow.setEnabled(false);
 		scrollPane.setViewportView(chatWindow);
 		
 		textField = new JTextField();
 		textField.setBounds(575, 400, 200, 30);
 		textField.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent evt) {
 				// TODO Auto-generated method stub
 				if(evt.getKeyCode() == KeyEvent.VK_ENTER){
                    sendChat();
                 }
 			}
 			@Override
 			public void keyReleased(KeyEvent e) {}
 			@Override
 			public void keyTyped(KeyEvent e) {}
 		});
 		
 		frmLobby.getContentPane().add(textField);
 		textField.setColumns(10);
 		
 		
 		tableNameField = new JTextField();
 		tableNameField.setBounds(50, 405, 175, 20);
 		frmLobby.getContentPane().add(tableNameField);
 		tableNameField.setColumns(10);
 		
 		SpinnerModel sm = new SpinnerNumberModel(2, 2, 5, 1); // Inital, Min, Max, Step
 		spinner = new JSpinner(sm);
 		spinner.setBounds(250, 405, 70, 20);
 		frmLobby.getContentPane().add(spinner);
 		
 		JLabel lblTableName = new JLabel("Table Name");
 		lblTableName.setBounds(91, 387, 100, 14);
 		frmLobby.getContentPane().add(lblTableName);
 		
 		JLabel lblTablePlayers = new JLabel("Table Players");
 		lblTablePlayers.setBounds(232, 387, 100, 14);
 		frmLobby.getContentPane().add(lblTablePlayers);
 		
 	}
 	
 	private void createGame() {
 		String tableName = tableNameField.getText();
 		if(tableName.contains(";")){
 			invalidChar();
 		} else{
 			int maxPlayers = (Integer) spinner.getValue();
 			MainClient.sendMessage("T;" + tableName + ";" + maxPlayers);
 		}
 	}
 	
 	private void joinTable() {
 		if(gameList.getSelectedRow() != -1) {
 			int tableNum = Integer.parseInt( (String) gameList.getValueAt(gameList.getSelectedRow(), 0) );
 			MainClient.sendMessage("J;" + tableNum);
 		} else {
 			selectATable();			
 		}
 	}
 	
 	public void addTable(String tableNum, String tableName, String numPlayers, String maxPlayers) {
 		String status;
 		if(Integer.parseInt(numPlayers) < Integer.parseInt(maxPlayers)) {
 			status = "Open";
 		} else {
 			status = "Full";
 		}
 		( (DefaultTableModel) gameList.getModel() ).addRow(new Object[]{tableNum, tableName, numPlayers + "/" + maxPlayers, status});
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void updateTable(String tableNum, String tableName, String numPlayers, String maxPlayers) {
 		int i = 0;
 		DefaultTableModel TM = ( (DefaultTableModel) gameList.getModel() );
 		Vector<Vector<String> > tableVector = TM.getDataVector();
 		Iterator<Vector<String> > it = tableVector.iterator();
 	    while(it.hasNext()) {
 	      Vector<String> v = it.next();
 	      if(Integer.parseInt(tableNum) == Integer.parseInt(v.elementAt(0))) {
 	    	  TM.removeRow(i);
 	    	  if(Integer.parseInt(numPlayers) > 0) { // If the table still exists
 		    	  String status;
 			  		if(Integer.parseInt(numPlayers) < Integer.parseInt(maxPlayers)) {
 			  			status = "Open";
 			  		} else {
 			  			status = "Full";
 			  		}
 		    	  TM.insertRow(i, new Object[]{tableNum, tableName, numPlayers + "/" + maxPlayers, status});
 	    	  }
 	    	  return;
 	      }
 	      i++;
 	    }
 	    // If the function has not returned, then the table is new
 	    this.addTable(tableNum, tableName, numPlayers, maxPlayers);
 	}
 	
 	public void newChat(String user, String chat) {
 		chatWindow.append(user + ": " + chat + "\n");
 	}
 	
 	public void sendChat() {
 		String txt = textField.getText();
 		if(txt.contains(";")) {
 			JOptionPane.showMessageDialog(frmLobby, "Cannot send message with \";\" in it.");
		} else if(!txt.isEmpty()) {
 			MainClient.sendMessage("C;" + txt);
 			textField.setText(null);
 		}
 	}
 	
 	public void selectATable() {
 		JOptionPane.showMessageDialog(frmLobby, "Please select a table to join!");
 	}
 	
 	public void tableIsFull() {
 		JOptionPane.showMessageDialog(frmLobby, "Table is full!");
 	}
 	
 	public void alreadyAtTable() {
 		JOptionPane.showMessageDialog(frmLobby, "You are already at a table!");
 	}
 	public void invalidChar() {
 		JOptionPane.showMessageDialog(frmLobby, "\";\" is not a valid character!");
 	}
 }
