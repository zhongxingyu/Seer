 package net.calzoneman.TileLand;
 
 import java.awt.Choice;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.TrayIcon.MessageType;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class StartupGUI extends JFrame implements ActionListener, ItemListener, FocusListener {
 	
 	public String selectedMapName;
 	public boolean makeNewLevel;
 	public Dimension newMapSize;
 	public boolean ready = false;
 	public String playerName;
 	
 	private JPanel contentPanel;
 	private JComboBox levelList;
 	private JButton loadLevelBtn;
 	private JButton createLevelBtn;
 	private JCheckBox checkMakeNewLevel;
 	private JTextField newLevelName;
 	private JTextField newLevelWidth;
 	private JTextField newLevelHeight;
 	private JTextField playerNameField;
 	
 	public StartupGUI() {
 		setTitle("TileLand");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		contentPanel = (JPanel)getContentPane();
 		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
 		setSize(200, 200);
 		this.setLocationByPlatform(true);
 	
 		levelList = new JComboBox();
 		levelList.setSize(100, 20);
 		try {
 			File folder = new File("saves");
 			for(File f : folder.listFiles()) {
 				if(f.getName().endsWith(".tl")) {
 					levelList.addItem(f.getName());
 				}
 			}
 		}
 		catch(Exception ex) {
 			
 		}
 		//levelList.setAlignmentX(LEFT_ALIGNMENT);
 		levelList.addItemListener(this);
 		contentPanel.add(levelList);
 		
 		playerNameField = new JTextField("Player Name");
 		playerNameField.addFocusListener(this);
 		contentPanel.add(playerNameField);
 		
 		
 		loadLevelBtn = new JButton("Load Level");
 		loadLevelBtn.setAlignmentX(CENTER_ALIGNMENT);
 		loadLevelBtn.addActionListener(this);
 		contentPanel.add(loadLevelBtn);
 		
 		newLevelWidth = new JTextField("New Level Width");
 		newLevelWidth.addFocusListener(this);
 		contentPanel.add(newLevelWidth);
 		
 		newLevelHeight = new JTextField("New Level Height");
 		newLevelHeight.addFocusListener(this);
 		contentPanel.add(newLevelHeight);
 		
 		newLevelName = new JTextField("New Level Name");
 		newLevelName.addActionListener(this);
 		newLevelName.addFocusListener(this);
 		contentPanel.add(newLevelName);
 		
 		createLevelBtn = new JButton("Create Level");
 		createLevelBtn.setAlignmentX(CENTER_ALIGNMENT);
 		createLevelBtn.addActionListener(this);
 		contentPanel.add(createLevelBtn);
 		
 		setVisible(true);
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == loadLevelBtn) {
 			makeNewLevel = false;
 			selectedMapName = (String)levelList.getSelectedItem();
 			playerName = playerNameField.getText();
 			setVisible(false);
 			ready = true;
 		}
 		else if(e.getSource() == createLevelBtn || e.getSource() == newLevelName) {
 			try {
 				newMapSize = new Dimension(Integer.parseInt(newLevelWidth.getText()), Integer.parseInt(newLevelWidth.getText()));
 				makeNewLevel = true;
 				selectedMapName = newLevelName.getText();
 				playerName = playerNameField.getText();
 				ready = true;
				System.out.println(ready);
 				setVisible(false);
 			}
 			catch(Exception ex) {
 				JOptionPane.showMessageDialog(this, "Error parsing input values.  Please check that you have entered everything correctly");
 			}
 		}
 	}
 	
 	public boolean isReady() {
 		return ready;
 	}
 
 	@Override
 	public void focusGained(FocusEvent e) {
 		if(e.getSource() == newLevelWidth) {
 			if(newLevelWidth.getText().equals("New Level Width")) {
 				newLevelWidth.setText("");
 			}
 		}
 		else if(e.getSource() == newLevelHeight) {
 			if(newLevelHeight.getText().equals("New Level Height")) {
 				newLevelHeight.setText("");
 			}
 		}
 		else if(e.getSource() == newLevelName) {
 			if(newLevelName.getText().equals("New Level Name")) {
 				newLevelName.setText("");
 			}
 		}
 		else if(e.getSource() == playerNameField) {
 			if(playerNameField.getText().equals("Player Name")) {
 				playerNameField.setText("");
 			}
 		}
 	}
 
 	@Override
 	public void focusLost(FocusEvent e) {
 		if(e.getSource() == newLevelWidth) {
 			if(newLevelWidth.getText().equals("")) {
 				newLevelWidth.setText("New Level Width");
 			}
 		}
 		else if(e.getSource() == newLevelHeight) {
 			if(newLevelHeight.getText().equals("")) {
 				newLevelHeight.setText("New Level Height");
 			}
 		}
 		else if(e.getSource() == newLevelName) {
 			if(newLevelName.getText().equals("")) {
 				newLevelName.setText("New Level Name");
 			}
 		}
 		else if(e.getSource() == playerNameField) {
 			if(playerNameField.getText().equals("")) {
 				playerNameField.setText("Player Name");
 			}
 		}
 		
 	}
 
 }
