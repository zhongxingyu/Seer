 package br.edu.axelrod.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import br.edu.axelrod.AxelrodCanvas;
 import br.edu.axelrod.AxelrodNetwork;
 import br.edu.axelrod.AxelrodSimulation;
 import br.edu.axelrod.AxelrodSimulation.SimulationObserver;
 
 public class MainApplicationFrame extends JFrame {
 
 	/** */
 	private static final long serialVersionUID = -2360890643572400835L;
 
 	private static final int FRAME_WIDTH = 1000;
 	private static final int FRAME_HEIGHT = 800;
 	private static final int CANVAS_WIDTH = 800;
 	private static final boolean BORDERS = false;
 
 	private static final String APP_TITLE = "Axelrod Simulation";
 	
 	final JFileChooser fc = new JFileChooser();
 	
 	JMenuBar menuBar = new JMenuBar();
 	JMenu fileMenu = new JMenu("File");
 	
 	JPanel controls = new JPanel();
 
 	JPanel line0 = new JPanel();
 	JLabel lLbl = new JLabel("Size:");
 	JTextField lTxtIn = new JTextField("100");
 
 	JPanel line1 = new JPanel();
 	JLabel fLbl = new JLabel("F:");
 	JTextField fTxtIn = new JTextField("2");
 
 	JPanel line2 = new JPanel();
 	JLabel qLbl = new JLabel("q:");
 	JTextField qTxtIn = new JTextField("2");
 
 	JPanel line3 = new JPanel();
 	JLabel paintLbl = new JLabel("Paint with state: ");
 	JTextField paintTxtIn = new JTextField();
 	
 	JPanel line4 = new JPanel();
 	JButton resetBtn = new JButton("Reset");
 	JButton toggleSimBtn = new JButton("Start");
 	JButton stepSimBtn = new JButton("Step");
 
 	JTextArea out = new JTextArea(5, 30);
 
 	// JLabel tLbl = new JLabel("Time: ");
 	JLabel iterationsLbl = new JLabel("Iterations: ");
 	JLabel iterationsLblOut = new JLabel("0");
 	JLabel interactionsLbl = new JLabel("Interactions: ");
 	JLabel interactionsLblOut = new JLabel("0");
 	JLabel epochsLbl = new JLabel("Epochs: ");
 	JLabel epochsLblOut = new JLabel("0");
 
 	AxelrodCanvas canvas;
 	AxelrodSimulation sim;
 	Thread simThr = new Thread(sim);
 
 	public MainApplicationFrame(){
 		
 //		setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
 		setTitle(APP_TITLE);
 		
 		resetBtn.addActionListener(resetSim);
 		toggleSimBtn.addActionListener(toggleSim);
 		stepSimBtn.addActionListener(stepSim);
 		resetSim.actionPerformed(null);
 		
 		fileMenu.add(loadFromFile);
 		fileMenu.add(saveToFile);
 		fileMenu.add(quit);
 		
 		menuBar.add(fileMenu);
 		
 		controls.setLayout(new BoxLayout(controls, BoxLayout.PAGE_AXIS));
 		controls.setPreferredSize(new Dimension(120, 120));
 		
 		lTxtIn.setPreferredSize(new Dimension(36, 18));
 		line0.add(lLbl);
 		line0.add(lTxtIn);
 		
 		fTxtIn.setPreferredSize(new Dimension(36, 18));
 		line1.add(fLbl);
 		line1.add(fTxtIn);
 		
 		qTxtIn.setPreferredSize(new Dimension(36, 18));
 		line2.add(qLbl);
 		line2.add(qTxtIn);
 		
 		paintTxtIn.setPreferredSize(new Dimension(100, 18));
 		paintTxtIn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int[] state = parseState(paintTxtIn.getText());
 				if(state != null){
 					canvas.stateStroke = state;
 				}
 			}
 		});
 		
 		line3.add(paintLbl);
 		line3.add(paintTxtIn);
 		
 		line4.add(resetBtn);
 		line4.add(toggleSimBtn);
 		line4.add(stepSimBtn);
 		
 		controls.add(line0);
 		controls.add(line1);
 		controls.add(line2);
 		controls.add(line3);
 		controls.add(line4);
 		
 		Container pane = this.getContentPane();
 		pane.add(menuBar, BorderLayout.NORTH);
 		pane.add(controls, BorderLayout.EAST);
 		
 		out.setEditable(false);
 		JScrollPane scrollOut = new JScrollPane(out);
 		System.setOut(new PrintStream(textAreaOutputStream(out)));
 		pane.add(scrollOut, BorderLayout.SOUTH);
 	}
 	
 	private void resetGui() {
 		Container pane = this.getContentPane(); 
 		if (canvas != null) {
 			pane.remove(canvas);
 		}
 		canvas = new AxelrodCanvas(CANVAS_WIDTH, sim.nw, BORDERS);
 		pane.add(canvas, BorderLayout.CENTER);
 		this.pack();
 		this.repaint();
 		SimulationObserver obs = new SimulationObserver() {
 			public void simulationStep(AxelrodNetwork nw) {}
 			public void nodeInteraction(int i, int j, int[] newState) {
 				canvas.repaint();
 			}
 		};
 		sim.setObserver(obs);
 		simThr = new Thread(sim);
 		toggleSimBtn.setText("Start");
 		stepSimBtn.setEnabled(true);
 		lTxtIn.setText(String.valueOf(sim.nw.size));
 		fTxtIn.setText(String.valueOf(sim.nw.features));
 		qTxtIn.setText(String.valueOf(sim.nw.traits));
 	}
 
 	private ActionListener resetSim = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			System.out.println("resetting simulation thread...");
 			if (sim != null) sim.quit();
 			try {
 				sim = new AxelrodSimulation(Integer.parseInt(lTxtIn.getText()), Integer.parseInt(fTxtIn.getText()), Integer.parseInt(qTxtIn.getText()));
 			} catch (NumberFormatException ex) {
 				JOptionPane.showMessageDialog(MainApplicationFrame.this, "Numbers only please!", "Error", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			resetGui();
 		}
 
 	};
 
 	private ActionListener toggleSim = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			if (!simThr.isAlive()) {
 				simThr.start();
 			}
 			if (sim.state == 1){
 				sim.stop();
 				toggleSimBtn.setText("Start");
 				stepSimBtn.setEnabled(true);
 			} else {
 				System.out.println("starting simulation thread...");
 				sim.start();
 				toggleSimBtn.setText("Stop");
 				stepSimBtn.setEnabled(false);
 			}
 		}
 	};
 	
 	private ActionListener stepSim = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			sim.sim_step();
 		}
 	};
 
 	private OutputStream textAreaOutputStream(final JTextArea t) {
 		return new OutputStream() {
 			JTextArea ta = t;
 			public void write(int b) {
 				byte[] bs = new byte[1];
 				bs[0] = (byte) b;
 				ta.append(new String(bs));
 				ta.setCaretPosition(ta.getDocument().getLength());
 			}
 		};
 	}
 	
 	private int[] parseState(String stateString){
 		String[] features = stateString.split(":");
 		if (features.length == sim.nw.features){
 			int[] state = new int[sim.nw.features];
 			for (int i = 0; i < features.length; i++) {
 				int f = -1;
 				try {
 					f = Integer.parseInt(features[i]);
 				} catch (Exception e) {
 					return null;
 				}
 				if (f < 0 || f > sim.nw.traits){
 					return null;
 				}
 				state[i] = f;
 			}
 			return state;
 		}
 		return null;
 	}
 	
 	Action saveToFile = new AbstractAction("Save...") {
 		
 		private static final long serialVersionUID = -4675654942388512094L;
 
 		public void actionPerformed(ActionEvent e) {
 			int select = fc.showSaveDialog(MainApplicationFrame.this);
 			if (select == JFileChooser.APPROVE_OPTION){
 				File f = fc.getSelectedFile();
 				try {
 					sim.nw.save_to_file(f);
 				} catch (IOException e1) {
 					JOptionPane.showMessageDialog(MainApplicationFrame.this, "Error saving to file: "+e1.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		}
 	};
 	
 	Action loadFromFile = new AbstractAction("Load...") {
 		private static final long serialVersionUID = -4675654942388512094L;
 		
 		public void actionPerformed(ActionEvent e) {
 			int select = fc.showOpenDialog(MainApplicationFrame.this);
 			if (select == JFileChooser.APPROVE_OPTION){
 				File f = fc.getSelectedFile();
 				try {
 					AxelrodNetwork nw = new AxelrodNetwork(f);
 					sim = new AxelrodSimulation(nw);
 					resetGui();
 				} catch (IOException e1) {
 					JOptionPane.showMessageDialog(MainApplicationFrame.this, "Error saving to file: "+e1.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		}
 	};
 	
 	Action quit = new AbstractAction("Exit") {
 		private static final long serialVersionUID = -4675654942388512094L;
 		
 		public void actionPerformed(ActionEvent e) {
 			System.exit(0);
 		}
 	};
 
 	public static void main(String[] args) {
 		MainApplicationFrame axelrod = new MainApplicationFrame();
 		axelrod.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		axelrod.pack();
 		axelrod.setVisible(true);
 	}
 }
