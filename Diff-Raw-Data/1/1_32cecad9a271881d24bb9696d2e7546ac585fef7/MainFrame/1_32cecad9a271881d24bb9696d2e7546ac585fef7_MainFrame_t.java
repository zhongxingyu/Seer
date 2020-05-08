 package window;
 
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.Box;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 
 import solver.Data;
 import solver.Solver;
 import start.Const;
 import window.panels.SettingsPanel;
 import window.panels.SolutionPanel;
 import window.panels.TablesPanel;
 
 import java.awt.Toolkit;
 
 /**
  *   .    SettingsPanel. 
  *    (  )   
  * TablesPanel    .     SolutionPanel
  *   .
  * 
  *         
  * (     )
  */
 
 public class MainFrame extends JFrame implements ActionListener, WindowListener {
 	private static final long serialVersionUID = 1L;
 
 	public MainFrame() {
 		Image frameIcon = Toolkit.getDefaultToolkit().getImage("frameIcon.png");
 		setIconImage(frameIcon);
 		setTitle(" ");
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		this.addWindowListener(this);
 		this.saved = true;
 		this.setLocation(150, 150);
 
 		rootPanel = Box.createVerticalBox();
 
 		createSettingsPanel();
 
 		this.add(rootPanel);
 		this.setVisible(true);
 
 		this.log.addItem(GregorianCalendar.getInstance().getTimeInMillis(),
 				"");
 	}
 
 	public void actionPerformed(ActionEvent ev) {
 		if (((JButton) ev.getSource()).getText().equals("Ok")) {
 			// SettingsPanel -> okButton -> Click
 			//
 			//      ,   TablesPanel
 			createTablesPanel();
 		} else if (((JButton) ev.getSource()).getText().equals("")) {
 			new HelpFrame();
 		} else if (((JButton) ev.getSource()).getText().equals("")) {
 			// TablesPanel -> okButton -> Click
 			//
 			//      ,   SolutionPanel
 			createSolutionPanel();
 		} else if (((JButton) ev.getSource()).getText().equals(" ")) {
 			// TablesPanel -> backButton -> Click
 			//
 			//      
 			rebuildSettingsPanel();
 		} else if (((JButton) ev.getSource()).getText().equals("")) {
 			// SolutionPanel -> saveButton -> Click
 			saveLog();
 		} else if (((JButton) ev.getSource()).getText().equals("")) {
 			// SolutionPanel -> backButton -> Click
 			rebuildTablesPanel();
 		} else if (((JButton) ev.getSource()).getText().equals("")) {
 			// SolutionPanel -> exitButton -> Click
 			exit();
 		}
 	}
 
 	/**
 	 *        SettingsPanel (
 	 *  TablesPanel -> backButton -> click)
 	 */
 	private void rebuildSettingsPanel() {
 		rootPanel.removeAll();
 		rootPanel.add(settingsPanel);
 
 		this.setMinimumSize(Const.START_FRAME_SIZE);
 		this.setMaximumSize(Const.DEFAULT_FRAME_SIZE);
 		this.setSize(Const.START_FRAME_SIZE);
 	}
 
 	/**
 	 *     SettingsPanel
 	 */
 	private void createSettingsPanel() {
 		rootPanel.removeAll();
 
 		settingsPanel = new SettingsPanel();
 		settingsPanel.getOkButton().addActionListener(this);
 		settingsPanel.getHelpButton().addActionListener(this);
 		rootPanel.add(settingsPanel);
 
 		this.setMinimumSize(Const.START_FRAME_SIZE);
 		this.setMaximumSize(Const.DEFAULT_FRAME_SIZE);
 		this.setSize(Const.START_FRAME_SIZE);
 	}
 
 	/**
 	 *        TablesPanel (
 	 *  SolutionPanel -> backButton -> click)
 	 */
 	private void rebuildTablesPanel() {
 		rootPanel.removeAll();
 		rootPanel.add(tablesPanel);
 
 		this.setMinimumSize(Const.MIN_FRAME_SIZE);
 		this.setMaximumSize(Const.MAX_FRAME_SIZE);
 		this.setSize(Const.DEFAULT_FRAME_SIZE);
 	}
 
 	/**
 	 *     TablesPanel
 	 */
 	private void createTablesPanel() {
 		int error = settingsPanel.checkData();
 		if (error != Const.NO_ERRORS) {
 			JOptionPane.showMessageDialog(this, errorMessages[error], "",
 					JOptionPane.WARNING_MESSAGE);
 			return;
 		}
 
 		rootPanel.removeAll();
 
 		tablesPanel = new TablesPanel(settingsPanel.getMineNumber(),
 				settingsPanel.getFactoriesNumber());
 		tablesPanel.getOkButton().addActionListener(this);
 		tablesPanel.getBackButton().addActionListener(this);
 		rootPanel.add(tablesPanel);
 
 		this.setMinimumSize(Const.MIN_FRAME_SIZE);
 		this.setMaximumSize(Const.MAX_FRAME_SIZE);
 		this.setSize(Const.DEFAULT_FRAME_SIZE);
 
 		this.log.addItem(GregorianCalendar.getInstance().getTimeInMillis(),
 				"  : " + settingsPanel.getMineNumber()
 						+ "   " + settingsPanel.getFactoriesNumber()
 						+ " .");
 	}
 
 	/**
 	 *     SolutionPanel
 	 */
 	private void createSolutionPanel() {
 		int error = tablesPanel.checkData();
 		if (error != Const.NO_ERRORS) {
 			JOptionPane.showMessageDialog(this, errorMessages[error], "",
 					JOptionPane.WARNING_MESSAGE);
 			return;
 		}
 		this.saved = false;
 		
 		rootPanel.removeAll();
 
 		this.log.addItem(GregorianCalendar.getInstance().getTimeInMillis(),
 				"   : ");
 		int[][] mineArray = { tablesPanel.getMineArray() };
 		int[][] factoryArray = { tablesPanel.getFactoryArray() };
 		this.log.addTable(mineArray, GregorianCalendar.getInstance()
 				.getTimeInMillis(), "    :");
 		this.log.addTable(factoryArray, GregorianCalendar.getInstance()
 				.getTimeInMillis(), " ,   :");
 		this.log.addTable(tablesPanel.getCostArray(), GregorianCalendar
 				.getInstance().getTimeInMillis(), " ");
 
 		Data data = new Data(tablesPanel.getMineArray(),
 				tablesPanel.getFactoryArray(), tablesPanel.getCostArray());
 		Solver solver = new Solver(data);
 
 		this.log.addItem(GregorianCalendar.getInstance().getTimeInMillis(),
 				"   ...");
 		Integer[][] solution = solver.solve();
 		this.log.addTable(solution, GregorianCalendar.getInstance()
 				.getTimeInMillis(),
 				" : ");
 
 		solutionPanel = new SolutionPanel(settingsPanel.getMineNumber(),
 				settingsPanel.getFactoriesNumber());
 		solutionPanel.setTableData(solution);
 		solutionPanel.getSaveButton().addActionListener(this);
 		solutionPanel.getBackButton().addActionListener(this);
 		solutionPanel.getExitButton().addActionListener(this);
 		rootPanel.add(solutionPanel);
 
 		this.setMinimumSize(new Dimension(Const.MIN_FRAME_SIZE.width,
 				Const.MIN_FRAME_SIZE.height - 100));
 		this.setSize(Const.DEFAULT_FRAME_SIZE.width - 5,
 				Const.DEFAULT_FRAME_SIZE.height - 5);
 		this.repaint();
 	}
 
 	/**
 	 *            
 	 * .
 	 */
 	private void saveLog() {
 		ExtensionFileFilter filter = new ExtensionFileFilter();
 		filter.addExtension(".txt");
 		filter.setDescription("Text files");
 
 		JFileChooser chooser = new JFileChooser();
 		chooser.setFileFilter(filter);
 		chooser.setCurrentDirectory(new File("C:/"));
 
 		switch (chooser.showSaveDialog(this)) {
 		case JFileChooser.APPROVE_OPTION: {
 			File file = chooser.getSelectedFile();
 			try {
 				FileWriter out = new FileWriter(file);
 				String line;
 				while ((line = this.log.nextLine()) != null) {
 					out.write(line);			
 				}
 
 				this.saved = true;
 				out.close();
 			} catch (IOException e) {
 				JOptionPane
 						.showMessageDialog(
 								this,
 								"   .    ",
 								"", JOptionPane.WARNING_MESSAGE);
 				return;
 			}
 
 			break;
 		}
 		case JFileChooser.CANCEL_OPTION: {
 			return;
 		}
 		case JFileChooser.ERROR_OPTION: {
 			JOptionPane.showMessageDialog(this,
 					"   .    ",
 					"", JOptionPane.WARNING_MESSAGE);
 			return;
 		}
 		}
 	}
 
 	/**
 	 *     ,    .
 	 */
 	private void exit() {
 		if (this.saved)
 			System.exit(0);
 		else {
 			int answer = JOptionPane.showConfirmDialog(this,
 					"    . ?",
 					"?", JOptionPane.YES_NO_OPTION);
 			if (answer == JOptionPane.YES_OPTION)
 				saveLog();
 			else if (answer == JOptionPane.NO_OPTION)
 				System.exit(0);
 		}
 	}
 
 	public void windowClosing(WindowEvent arg0) {
 		exit();
 	}
 
 	/**
 	 *  ,      .
 	 */
 	private class Log {
 		public Log() {
 			this.log = new ArrayList<String>();
 		}
 
 		public void addItem(long time, String event) {
 			this.log.add("[" + (TimeUnit.MILLISECONDS.toHours(time) % 24 + 3)
 					+ ":" + TimeUnit.MILLISECONDS.toMinutes(time) % 60 + ":"
 					+ TimeUnit.MILLISECONDS.toSeconds(time) % 60 + "] - "
 					+ event + "\n");
 		}
 
 		public void addTable(int[][] data, long time, String message) {
 			this.log.add(message + "\n");
 
 			String curLine = new String();
 
 			for (int i = 0; i < data.length; ++i) {
 				curLine = new String("|");
 				for (int j = 0; j < data[i].length; ++j) {
 					curLine += fillString(" ", 10 - String.valueOf(data[i][j])
 							.length())
 							+ data[i][j] + "|";
 				}
 				this.log.add(fillString("-", curLine.length()) + "\n");
 				this.log.add(curLine + "\n");
 			}
 
 			this.log.add(fillString("-", curLine.length()) + "\n");
 		}
 
 		public void addTable(Integer[][] data, long time, String message) {
 			int[][] newData = new int[data.length][data[0].length];
 
 			for (int i = 0; i < data.length; ++i) {
 				for (int j = 0; j < data[0].length; ++j) {
 					if (data[i][j] == null)
 						newData[i][j] = 0;
 					else
 						newData[i][j] = data[i][j];
 				}
 			}
 
 			addTable(newData, time, message);
 		}
 
 		public String nextLine() {
 			if (returnedLine == log.size()) {
 				returnedLine = 0;
 				return null;
 			} else
 				return log.get(returnedLine++);
 		}
 
 		private ArrayList<String> log;
 		private int returnedLine;
 	}
 
 	private String fillString(String subStr, int times) {
 		String result = new String();
 		for (int i = 0; i < times; ++i)
 			result += subStr;
 
 		return result;
 	}
 
 	private Box rootPanel;
 	private SettingsPanel settingsPanel;
 	private TablesPanel tablesPanel;
 	private SolutionPanel solutionPanel;
 	private Log log = new Log();
 	private boolean saved;
 	private String[] errorMessages = {
 			"Ok",
 			"   .    ",
 			" .    ",
 			"      ,     ",
 			"        " };
 	
 /* 
  *  .    .
  */
 	public void windowClosed(WindowEvent arg0) {
 	}
 
 	public void windowActivated(WindowEvent arg0) {
 	}
 
 	public void windowDeactivated(WindowEvent arg0) {
 	}
 
 	public void windowDeiconified(WindowEvent arg0) {
 	}
 
 	public void windowIconified(WindowEvent arg0) {
 	}
 
 	public void windowOpened(WindowEvent arg0) {
 	}
 }
