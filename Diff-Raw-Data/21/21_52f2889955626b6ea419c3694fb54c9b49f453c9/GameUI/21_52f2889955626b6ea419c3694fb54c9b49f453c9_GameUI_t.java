 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 public class GameUI {
 	
 	private static final Color COLOR_DEAD = new Color(255, 255, 255);
 	private static final Color COLOR_ALIVE = new Color(0,0,0);
 	
 	private JFrame gameFrame = new JFrame("Conway's Game of life");
 	private JPanel gridPanel;
 	private JButton [][] gridButtons;
 	private JButton btnStart;
 	private JButton btnClear;
 	private JButton btnStep;
 	private JButton btnChangeGridSize;
 	private JTextField rows = new JTextField(2);
 	private JTextField columns = new JTextField(2);
 	private JMenuItem menuItemStore;
 	private JMenuItem menuItemLoad;
 		
 	public GameUI(LifeSimulation simulation) {
 		JMenuBar menuBar = new JMenuBar();
 		JMenu menuFile = new JMenu("File");
 		menuItemStore = new JMenuItem("Store");
 		menuItemLoad = new JMenuItem("Load");
 		
 		menuBar.add(menuFile);
 		menuFile.add(menuItemLoad);
 		menuFile.add(menuItemStore);
 		
 		btnStart = new JButton("Start");
 		btnClear = new JButton("Clear/Stop");
 		btnStep = new JButton("Step by Step");
 		btnChangeGridSize = new JButton("Change Size");
 		btnChangeGridSize.setToolTipText("Enter: rows and columns to text fields then press this button to change!");
 		gridButtons = new JButton[simulation.getRow()][simulation.getColumn()];
 		
 		gridPanel = new JPanel();
 		gridPanel.setSize(600, 600);
 		initializeGrid(simulation);
 		
 		rows.setText(Integer.toString(simulation.getRow()));
 		columns.setText(Integer.toString(simulation.getColumn()));
 		rows.setDisabledTextColor(Color.RED);
 		columns.setDisabledTextColor(Color.RED);
 		
 		JPanel panelButtons = new JPanel();
 		panelButtons.add(btnStart);
 		panelButtons.add(btnStep);
 		panelButtons.add(btnClear);
 		panelButtons.add(rows);
 		panelButtons.add(columns);
 		panelButtons.add(btnChangeGridSize);
 		panelButtons.setSize(600, 100);
 		
 		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		gameFrame.setResizable(false);
 		gameFrame.setSize(600,  600);
 		gameFrame.setJMenuBar(menuBar);
 		gameFrame.add(gridPanel);
 		gameFrame.add(BorderLayout.SOUTH, panelButtons);
 		gameFrame.setVisible(true);
 		gameFrame.setAlwaysOnTop(true);
 	}
 	
 	//Methods to interact with grid
 	public void initializeGrid(LifeSimulation simulation) {
 		gridPanel.removeAll();
 		gridPanel.setLayout(new GridLayout(simulation.getRow(), simulation.getColumn()));
 		gridButtons = new JButton[simulation.getRow()][simulation.getColumn()];
 		
 		for (int row=0; row < simulation.getRow(); row++) {
 			for (int col=0; col < simulation.getColumn(); col++) {
 				gridButtons[row][col] = new JButton("");
 				gridButtons[row][col].setBackground(simulation.getLife(row, col) ? COLOR_ALIVE : COLOR_DEAD);
 				gridButtons[row][col].setName(row+","+col);
 				gridPanel.add(gridButtons[row][col]);
 			}
 		}
 		
 		rows.setText(Integer.toString(simulation.getRow()));
 		columns.setText(Integer.toString(simulation.getColumn()));
 
		//gameFrame.validate();
		gameFrame.pack();
 		gameFrame.setSize(600, 600);
 	}
 	
 	public void clearGrid(LifeSimulation gameLogic) {
 		for (int row=0; row<gameLogic.getRow(); row++) {
 			for (int col=0; col<gameLogic.getColumn(); col++) {
 				gameLogic.setLife(row, col, false);
 				gridButtons[row][col].setBackground(COLOR_DEAD);
 			}
 		}
 	}
 	
 	public void setLifeInGrid(LifeSimulation gameLogic) {
 		if (gameLogic != null) {
 			for (int row=0; row < gameLogic.getRow(); row++) {
 				for (int col=0; col < gameLogic.getColumn(); col++) {
 					if (gameLogic.getLife(row, col)) {
 						gridButtons[row][col].setBackground(COLOR_ALIVE);
 					}
 					else {
 						gridButtons[row][col].setBackground(COLOR_DEAD);
 					}
 				}
 			}
 		}
 	}
 	
 	public void setLifeFromGrid(JButton cell, LifeSimulation gameSimulation) {
 		if (cell.getBackground() == COLOR_ALIVE) {
 			cell.setBackground(COLOR_DEAD);
 			gameSimulation.setLifeByString(cell.getName(), false);
 		}
 		else {
 			cell.setBackground(COLOR_ALIVE);
 			gameSimulation.setLifeByString(cell.getName(), true);
 		}
 	}
 	
 	private boolean validateGridSize(int row, int col) {
 		return (row != 0 && col !=0 && row < 100 && col < 100);
 	}
 	
 	public LifeSimulation changeGridSize() {
 		LifeSimulation simulation = null;
 		
 		try {
 			String rowCount = rows.getText();
 			String colCount = columns.getText();
 			
 			int row = Math.abs(Integer.parseInt(rowCount));
 			int col = Math.abs(Integer.parseInt(colCount));
 			
 			if (validateGridSize(row, col)) {
 				simulation = new LifeSimulation(row, col);
 			}
 			else {
 				simulation = illegalSize();
 			}
 			
 		} catch (NumberFormatException ex) {
 			simulation = illegalSize();
 		}
 		
 		initializeGrid(simulation);
 		return simulation;
 	}
 	
 	private LifeSimulation illegalSize() {
 		JOptionPane.showMessageDialog(gameFrame, "Enter numbers between 1..99 !");
 		return new LifeSimulation(50, 50);
 	}
 	
 	//Methods to set action listener for each button..
 	public void addBtnStartActionListener(ActionListener startClicked) {
 		btnStart.addActionListener(startClicked);
 	}
 	
 	public void addBtnClearActionListener(ActionListener clearClicked) {
 		btnClear.addActionListener(clearClicked);
 	}
 	
 	public void addBtnStepActionListener(ActionListener stepClicked) {
 		btnStep.addActionListener(stepClicked);
 	}
 	
 	public void addGridButtonListener(ActionListener btnClicked) {
 		for (int row=0; row < gridButtons.length; row++) {
 			for (int col=0; col < gridButtons[0].length; col++) {
 				gridButtons[row][col].addActionListener(btnClicked);
 			}
 		}
 	}
 	
 	public void addChangeGridSizeButtonListener(ActionListener changeGridSizeClicked) {
 		btnChangeGridSize.addActionListener(changeGridSizeClicked);
 	}
 	
 	public void addMenuItemLoadListener(ActionListener itemLoadClicked) {
 		menuItemLoad.addActionListener(itemLoadClicked);
 	}
 	
 	public void addMenuItemStoreListener(ActionListener itemStoreClicked) {
 		menuItemStore.addActionListener(itemStoreClicked);
 	}
 	
 	public void enableOrDisableTextFields(boolean isEditable) {
 		rows.setEditable(isEditable);
 		columns.setEditable(isEditable);
 	}
 	
 	//Methods for input/output..
 	public String getFileName() {
 		String inputFileName = JOptionPane.showInputDialog(gameFrame, "Enter file name without extension: ",
 				"Save", JOptionPane.PLAIN_MESSAGE);
 		return inputFileName;
 	}
 	
 	public File fileBrowser() {
 		JFileChooser fileChooser = new JFileChooser("path.dat");
 		FileNameExtensionFilter filter = new FileNameExtensionFilter("Conway's game files", "dat", "xml", "cfg");
 		File file;
 		
 		fileChooser.setFileFilter(filter);
 		int result = fileChooser.showOpenDialog(gameFrame);
 		
 		if (result == JFileChooser.APPROVE_OPTION) {
 			file = fileChooser.getSelectedFile();
 			return file;
 		}
 		return null;
 	}
 	
 	public void warningMessage() {
 		JOptionPane.showMessageDialog(gameFrame, "Can't do this operation while game is Running!");
 	}
 }
