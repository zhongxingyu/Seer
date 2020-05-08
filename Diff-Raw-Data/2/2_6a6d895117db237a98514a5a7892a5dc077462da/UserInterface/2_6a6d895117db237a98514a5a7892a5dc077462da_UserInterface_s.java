 package com.github.pageallocation.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.table.DefaultTableModel;
 
 import com.github.pageallocation.algorithms.FIFOPageReplacement;
 import com.github.pageallocation.algorithms.LRUPageReplacement;
 import com.github.pageallocation.algorithms.OPTPageReplacement;
 import com.github.pageallocation.algorithms.PageReplacementStrategy;
 import com.github.pageallocation.gui.table.MyDefaultTableModel;
 import com.github.pageallocation.resources.Resources;
 import com.github.pageallocation.simulation.CompositeSimulation;
 import com.github.pageallocation.simulation.PageAllocationSimulation;
 import com.github.pageallocation.simulation.Simulation;
 import com.github.pageallocation.simulation.SimulationRunnerManager;
 import com.github.pageallocation.simulation.event.SimulationStateEvent;
 import com.github.pageallocation.simulation.event.SimulationStateListener;
 import com.github.pageallocation.util.Util;
 
 /*
  * This class holds the foundation of the program's Graphical
  * User Interface. The GUI is set up and displayed from this class.
  * This class also calls other classes and their methods in order to
  * display our data (such as our page allocations).
  */
 public class UserInterface extends JFrame implements ActionListener {
 	private static final int MINIMUM_REFERENCE_LENGTH = 7;
 	private static final long serialVersionUID = 1L;
 	private JFrame f;
 	private Container contentPane;
 	private JTextArea randStrArea;
 	private SpinnerNumberModel strLengthModel, frameSpinnerModel,
 			rangeSpinnerModel;
 	private PropertiesWindow propWin = new PropertiesWindow();
 	private List<SimulationPanel> simulationPanels = new ArrayList<>(3);
 	private SimulationRunnerManager simManager;
 	private JButton play, pause, step;
 	private final StateManager state;
 	private final Pattern REFERENCES_PATTERN = Pattern
			.compile("(\\d+){1}(,\\s+\\d*)*");
 
 	// Program Variables
 	private String version = "2.00"; // v1.00 (release date)
 
 	public UserInterface() {
 		createSimulationPanels();
 		f = new JFrame();
 		f.setTitle("Page Allocation Simulator"); // Set the title of the JFrame
 		contentPane = f.getContentPane();
 
 		// Add the components to the JFrame
 		f.setJMenuBar(menuBar()); // Add the JMenuBar to the JFrame
 		contentPane.add(northPanel(), BorderLayout.NORTH);
 		contentPane.add(centerPanel(), BorderLayout.CENTER);
 
 		if (getOS().contains("win"))
 			f.setSize(780, 560); // Set the size of the JFrame (width, height)
 		else
 			f.setSize(800, 560); // Set the size of the JFrame (width, height)
 		f.setIconImage(Resources.SMALL.getIcon().getImage());
 		f.setLocation(setFrameCentered()); // Set the location of the JFrame on
 											// the screen
 		f.setResizable(true); // Frame cannot be resized
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // End all java
 															// processes when
 															// the program is
 															// closed
 
 		state = new StateManager();
 		f.setVisible(true); // JFrame must be visible to see it
 
 	}
 
 	private void createSimulationPanels() {
 		simulationPanels.add(new SimulationPanel("FIFO", "First in First Out",
 				new FIFOPageReplacement()));
 		 simulationPanels.add(new SimulationPanel("OPT", "Optimal Algorithm",
 		 new OPTPageReplacement()));
 		 simulationPanels.add(new SimulationPanel("LRU",
 		 "Least Recently Used",
 		 new LRUPageReplacement()));
 	}
 
 	private JPanel northPanel() {
 		JPanel p = new JPanel(new BorderLayout());
 		p.add(northWestPanel(), BorderLayout.WEST);
 		p.add(northEastPanel(), BorderLayout.EAST);
 		return p;
 	}
 
 	private JPanel northEastPanel() {
 		JPanel topLayer = new JPanel(new BorderLayout());
 		topLayer.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
 		JPanel north = new JPanel();
 		JPanel south = new JPanel();
 		JPanel west = new JPanel();
 		JLabel label;
 		JSpinner spinner;
 
 		randStrArea = new JTextArea();
 		randStrArea.setLineWrap(true);
 		JScrollPane scrollPane = new JScrollPane(randStrArea);
 		scrollPane
 				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		scrollPane.setPreferredSize(new Dimension(470, 80)); // Width, Height
 		north.add(scrollPane);
 
 		JButton button;
 
 		button = new JButton();
 		button.setPreferredSize(new Dimension(85, 25));
 		button.setIcon(Resources.RESET.getIcon());
 		button.setToolTipText("Reset Simulation");
 		button.setFocusPainted(false);
 		button.setActionCommand("reset");
 		button.addActionListener(this);
 		south.add(button);
 
 		play = new JButton();
 		play.setPreferredSize(new Dimension(85, 25));
 		play.setIcon(Resources.PLAY.getIcon());
 		play.setToolTipText("Run Simulation");
 		play.setFocusPainted(false);
 		play.setActionCommand("run");
 		play.addActionListener(this);
 		south.add(play);
 
 		pause = new JButton();
 		pause.setPreferredSize(new Dimension(85, 25));
 		pause.setIcon(Resources.PAUSE.getIcon());
 		pause.setToolTipText("Pause Simulation");
 		pause.setFocusPainted(false);
 		pause.setActionCommand("pause");
 		pause.addActionListener(this);
 		pause.setEnabled(false);
 		south.add(pause);
 
 		step = new JButton();
 		step.setPreferredSize(new Dimension(85, 25));
 		step.setIcon(Resources.STEP.getIcon());
 		step.setToolTipText("Step Through");
 		step.setFocusPainted(false);
 		step.setActionCommand("step");
 		step.addActionListener(this);
 		step.setEnabled(true);
 		south.add(step);
 
 		button = new JButton("Generate");
 		button.setPreferredSize(new Dimension(110, 25));
 		button.setToolTipText("Generate Random String of Numbers");
 		button.setFocusPainted(false);
 		button.setActionCommand("generate");
 		button.addActionListener(this);
 		south.add(button);
 
 		label = new JLabel("# of Frames");
 		west.add(label);
 
 		frameSpinnerModel = new SpinnerNumberModel(3, 3, 100, 1); // Initial,
 																	// Min, Max,
 																	// Increment
 		spinner = new JSpinner(frameSpinnerModel);
 		spinner.setPreferredSize(new Dimension(55, 25));
 		west.add(spinner);
 
 		if (getOS().contains("mac"))
 			west.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 0));
 		else if (getOS().contains("win"))
 			west.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 0));
 		else
 			west.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));
 
 		label = new JLabel("Range of Pages");
 		label.setToolTipText("Range of Generated Numbers\n");
 		west.add(label);
 
 		rangeSpinnerModel = new SpinnerNumberModel(0, 0, 3000, 1); // Initial,
 																	// Min, Max,
 																	// Increment
 		spinner = new JSpinner(rangeSpinnerModel);
 		if (getOS().contains("mac"))
 			spinner.setPreferredSize(new Dimension(75, 25));
 		else
 			spinner.setPreferredSize(new Dimension(55, 25));
 		west.add(spinner);
 
 		label = new JLabel("String Length");
 		label.setToolTipText("Numbers to Generate");
 		west.add(label);
 
 		strLengthModel = new SpinnerNumberModel(MINIMUM_REFERENCE_LENGTH,
 				MINIMUM_REFERENCE_LENGTH, 99, 1); // Initial, Min,
 		// Max,
 		// Increment
 		spinner = new JSpinner(strLengthModel);
 		if (getOS().contains("mac"))
 			spinner.setPreferredSize(new Dimension(45, 25)); // width, height
 		else
 			spinner.setPreferredSize(new Dimension(35, 25)); // width, height
 		west.add(spinner);
 
 		topLayer.add(north, BorderLayout.NORTH);
 		topLayer.add(west, BorderLayout.WEST);
 		topLayer.add(south, BorderLayout.SOUTH);
 
 		return topLayer;
 	}
 
 	private JPanel northWestPanel() {
 		JPanel p = new JPanel();
 		ImageIcon i = Resources.SIMULATOR.getIcon();
 		JLabel label = new JLabel(i);
 
 		JLabel buffer = new JLabel();
 		buffer.setPreferredSize(new Dimension(18, 0));
 
 		p.add(buffer);
 		p.add(label);
 
 		return p;
 	}
 
 	private JPanel centerPanel() {
 		JPanel topLayer = new JPanel(new FlowLayout());
 		for (SimulationPanel alg : simulationPanels) {
 			topLayer.add(alg);
 		}
 
 		return topLayer;
 	}
 
 	/*
 	 * JMenuBar holding all menu options
 	 */
 	private JMenuBar menuBar() {
 		JMenuBar mb = new JMenuBar();
 		JMenu fileMenu, helpMenu;
 		JMenuItem run, step, reset, properties, exit, help, about;
 
 		// JMenu's
 		fileMenu = new JMenu("File");
 		helpMenu = new JMenu("Help");
 
 		fileMenu.setRolloverEnabled(true);
 		helpMenu.setRolloverEnabled(true);
 
 		// JMenuItem's
 		run = new JMenuItem("Run");
 		run.setIcon(Resources.PLAY.getIcon());
 		step = new JMenuItem("Step");
 		step.setEnabled(true);
 		step.setIcon(Resources.STEP.getIcon());
 		reset = new JMenuItem("Reset");
 		reset.setIcon(Resources.RESET.getIcon());
 		properties = new JMenuItem("Properties");
 		properties.setIcon(Resources.PROPERTIES.getIcon());
 		exit = new JMenuItem("Exit");
 		help = new JMenuItem("Help");
 		help.setIcon(Resources.HELP.getIcon());
 		about = new JMenuItem("About");
 		about.setIcon(Resources.EXCLAMATION.getIcon());
 
 		// Add JMenuItem's to the JMenu's
 		fileMenu.add(run);
 		fileMenu.add(step);
 		fileMenu.add(reset);
 		fileMenu.addSeparator();
 		fileMenu.add(properties);
 		fileMenu.addSeparator();
 		fileMenu.add(exit);
 		helpMenu.add(help);
 		helpMenu.add(about);
 
 		// Accelerator
 		run.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
 				ActionEvent.CTRL_MASK));
 		step.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
 				ActionEvent.CTRL_MASK));
 		reset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
 				ActionEvent.CTRL_MASK));
 
 		// Action Listening
 		run.setActionCommand("run");
 		run.addActionListener(this);
 		step.setActionCommand("step");
 		step.addActionListener(this);
 		reset.setActionCommand("reset");
 		reset.addActionListener(this);
 		properties.setActionCommand("properties");
 		properties.addActionListener(this);
 		exit.setActionCommand("exit");
 		exit.addActionListener(this);
 		help.setActionCommand("help");
 		help.addActionListener(this);
 		about.setActionCommand("about");
 		about.addActionListener(this);
 
 		// Add JMenu's to the JMenuBar
 		mb.add(fileMenu);
 		mb.add(helpMenu);
 
 		return mb;
 	}
 
 	/**
 	 * Centers the JFrame on the users screen.
 	 * 
 	 * @return returns a Point(x, y) where the JFrame will be centered on the
 	 *         users screen.
 	 */
 	private Point setFrameCentered() {
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		int w = f.getSize().width;
 		int h = f.getSize().height;
 		int x = (d.width - w) / 2;
 		int y = (d.height - h) / 2;
 
 		Point p = new Point(x, y - 20);
 		return p;
 	}
 
 	/**
 	 * Finds the name of the Operating System that the user is currently using.
 	 * Helpful function for writing platform specific code.
 	 * 
 	 * @return name of the operating system that the user is currently using
 	 */
 	private String getOS() {
 		String s = System.getProperty("os.name").toLowerCase();
 		return s;
 	}
 
 	/**
 	 * Add text from a specified file to the specified JTextArea
 	 * 
 	 * @param t
 	 *            JTextArea to add the content to
 	 * @param f
 	 *            Text file name
 	 */
 	private void addContent(JTextArea t, String f) {
 		String line;
 		try {
 
 			InputStream iStream = getClass().getClassLoader()
 					.getResourceAsStream(f);
 			InputStreamReader isr = new InputStreamReader(iStream);
 			BufferedReader reader = new BufferedReader(isr);
 
 			while ((line = reader.readLine()) != null) {
 				t.append(line);
 				t.append("\n");
 			}
 			iStream.close();
 			isr.close();
 			reader.close();
 		} catch (Exception e) {
 			System.out.println("Error: " + e.getMessage());
 		}
 	}
 
 	private void populateSimulationTable(int[] s) {
 		if (s == null)
 			return;
 
 		if (simManager != null) {
 			simManager.stopSimulation();
 		}
 		List<Simulation> simulations = new ArrayList<>(
 				this.simulationPanels.size());
 		PageReplacementStrategy strategy = null;
 		DecimalFormat fmt = new DecimalFormat("###.##");
 		int frames = frameSpinnerModel.getNumber().intValue();
 
 		for (SimulationPanel simulationPanel : simulationPanels) {
 
 			int columns = simulationPanel.getTable().getColumnCount();
 			strategy = simulationPanel.getStrategy();
 			strategy.setParams(s, frames);
 
 			PageAllocationSimulation sim = simulationPanel.getSimulation();
 			sim.setParams(strategy.allocateReferences(), frames, columns);
 			simulations.add(sim);
 			
 			simulationPanel.getFaults().setText(Integer.toString(strategy.faults()));
 			simulationPanel.getFaultRate().setText(
 					fmt.format(strategy.faultRate()) + "%");
 		}
 
 		simManager = new SimulationRunnerManager(new CompositeSimulation(
 				simulations), propWin);
 		simManager.addListener(state);
 
 	}
 
 	/*
 	 * This method takes the randomly generated string and adds each number to a
 	 * the column header.
 	 */
 	private void updateSimulationTablesColumns(int[] s) {
 		Object[] header = Util.makeHeaderArray(s);
 		for (SimulationPanel sims : simulationPanels) {
 			updateSimulationColumns(s, sims, header);
 
 		}
 	}
 
 	private void updateSimulationColumns(int[] s, SimulationPanel simulationPanel,
 			Object[] headers) {
 		MyDefaultTableModel model = simulationPanel.getModel();
 		model.setRowCount(s.length + 1);
 		model.setColumnIdentifiers(headers);
 		model.fireTableStructureChanged();
 	}
 
 	/*
 	 * This method either adds rows or deletes rows depending on the frame
 	 * number that the user has set.
 	 */
 	private void updateSimulationTablesRows() {
 		int frames = frameSpinnerModel.getNumber().intValue();
 		for (SimulationPanel sims : simulationPanels) {
 			DefaultTableModel model = sims.getModel();
 			model.setRowCount(0);
 
 			for (int i = 0; i < frames; i++) {
 				model.addRow(new Object[] { i });
 			}
 		}
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String actionCommand = e.getActionCommand();
 		// Menubar
 		if (actionCommand.equals("exit")) {
 			System.exit(0);
 		} else if (actionCommand.equals("help")) {
 			JTextArea textArea = new JTextArea();
 			addContent(textArea, "README.txt");
 			textArea.setEditable(false);
 			textArea.setCaretPosition(NORMAL);
 			JScrollPane sc = new JScrollPane(textArea);
 			sc.setPreferredSize(new Dimension(634, 250)); // Width, Height
 			sc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 			JOptionPane.showMessageDialog(f, sc, "Help",
 					JOptionPane.QUESTION_MESSAGE);
 		} else if (actionCommand.equals("about")) {
 			JOptionPane.showMessageDialog(f, "Version: " + version + "\n"
 					+ "Author: Victor J. Reventos" + "\n"
 					+ "GUI Based on:    \n" + "    Adam Childs\n"
 					+ "    Shawn Craine\n" + "    Dylan Meyer\n", "About",
 					JOptionPane.INFORMATION_MESSAGE);
 		}
 		// Buttons
 		else if (actionCommand.equals("reset")) {
 			resetGui();
 		} else if (actionCommand.equals("properties")) {
 			propWin.setVisible(true);
 		} else if (actionCommand.equals("run")) {
 			runOrStepSimulation(false);
 		} else if (actionCommand.equals("stop")) {
 			stopSimulation();
 		} else if (actionCommand.equals("step")) {
 			runOrStepSimulation(true);
 		}else if (actionCommand.equals("pause")) {
 			pauseSimulation();
 		} else if (actionCommand.equals("generate")) {
 			/*
 			 * Here we grab the value in the JSpinner which signifies the amount
 			 * of numbers that the individual wants for the string. Then we
 			 * generate that amount of random numbers with the
 			 * generateRandomNumbers() function and then place that string into
 			 * the JTextArea.
 			 */
 			int i = strLengthModel.getNumber().intValue();
 			randStrArea.setText(Util.generateRandomPageReference(i,
 					rangeSpinnerModel.getNumber().intValue()));
 		}
 	}
 
 	private void pauseSimulation() {
 		System.out.println("UserInterface.pauseSimulation()");
 		simManager.pause();
 
 	}
 
 	private void stopSimulation() {
 		System.out.println("UserInterface.stopSimulation() "
 				+ state.simManagerRunning);
 		simManager.stopSimulation();
 	}
 
 	private void runOrStepSimulation(boolean step) {
 		String text = randStrArea.getText();
 		if (text.equals("") || !isValidInputReferencesFormat(text)) {
 			JOptionPane.showMessageDialog(f, "Error! Invalid format. Please\n"
 					+ "generate or supply a string of numbers\n"
 					+ "with a comma and space between each\n" + "number.",
 					"String Error", JOptionPane.ERROR_MESSAGE);
 		} else {
 			if (state.simManagerRunning) {
 				if (step) {
 					simManager.step();
 				} else {
 					simManager.play();
 				}
 				return;
 			}
 			int[] s = Util.refStringToArray(text);
 			if (!(s.length < MINIMUM_REFERENCE_LENGTH)) {
 				updateSimulationTables(s);
 				populateSimulationTable(s);
 				if (step) {
 					simManager.step();
 				} else {
 					simManager.play();
 				}
 
 			} else {
 				JOptionPane.showMessageDialog(f,
 						"You must supply a string of\n"
 								+ "at least 7 numbers. Remember\n"
 								+ "to separate each number by a\n"
 								+ "comma and a space.", "String Error",
 						JOptionPane.ERROR_MESSAGE);
 			}
 			s = null;
 		}
 	}
 
 	private boolean isValidInputReferencesFormat(String text) {
 		return REFERENCES_PATTERN.matcher(text).matches();
 	}
 
 	private void updateSimulationTables(int[] s) {
 		updateSimulationTablesColumns(s);
 		updateSimulationTablesRows();
 	}
 
 	/**
 	 * If there is a simulation stop it. Clear all the simulation panels and
 	 * make the initial state.
 	 */
 	private void resetGui() {
 		if (simManager != null) {
 			simManager.stopSimulation();
 		}
 		for (SimulationPanel sim : simulationPanels) {
 			sim.clear();
 		}
 		state.initState();
 
 	}
 
 
 	class StateManager implements SimulationStateListener {
 
 		private boolean simManagerRunning;
 
 		public StateManager() {
 			initState();
 		}
 
 		void initState() {
 			play.setEnabled(true);
 			pause.setEnabled(false);
 			step.setEnabled(true);
 			simManagerRunning = false;
 
 		}
 
 		@Override
 		public void stepEvent(SimulationStateEvent e) {
 			System.out.println("UserInterface.StateManager.stepEvent()");
 			play.setEnabled(true);
 			pause.setEnabled(false);
 			simManagerRunning = true;
 
 		}
 
 		@Override
 		public void playEvent(SimulationStateEvent e) {
 			System.out.println("UserInterface.StateManager.playEvent()");
 			play.setEnabled(false);
 			pause.setEnabled(true);
 			simManagerRunning = true;
 
 		}
 
 		@Override
 		public void pauseEvent(SimulationStateEvent e) {
 			System.out.println("UserInterface.StateManager.pauseEvent()");
 			play.setEnabled(true);
 			pause.setEnabled(false);
 		}
 
 		@Override
 		public void stopEvent(SimulationStateEvent e) {
 			System.out.println("UserInterface.StateManager.stopEvent()");
 
 			play.setEnabled(false);
 			pause.setEnabled(false);
 			step.setEnabled(false);
 
 			simManagerRunning = false;
 			simManager.stopSimulation();
 			JOptionPane.showMessageDialog(f, "Simulation Finished",
 					"Simulation", JOptionPane.INFORMATION_MESSAGE);
 
 		}
 	}
 }
