 package edu.tufts.cs.gv;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JSplitPane;
 import javax.swing.Timer;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import edu.tufts.cs.gv.controller.VizState;
 import edu.tufts.cs.gv.model.Dataset;
 import edu.tufts.cs.gv.view.GraphView;
 import edu.tufts.cs.gv.view.ResultsView;
 import edu.tufts.cs.gv.view.StudentView;
 import edu.tufts.cs.gv.view.VizView;
 
 public class Visualization extends JFrame {
 	//
 	// Global
 	//
 	
 	private static final long serialVersionUID = 1L;
 	
 	static final String STARTSCREEN = "Start Screen";
 	static final String VIZSCREEN = "Visualization Screen";
 	
 	private Timer renderTimer;
 	
 	private JFileChooser datasetChooser;
 	
 	private JPanel pnlRoot;
 	private CardLayout crdRoot;
 	
 	//
 	// Start Screen
 	//
 	
 	private JPanel pnlStart;
 	private JButton btnChoose;
 	
 	//
 	// Visualization Screen
 	//
 	
 	private JSplitPane studentSplit, testSplit;
 	
 	// Student view & chooser button
 	private JButton btnDiff, btnHelp;
 	private JPanel pnlDiff, pnlStudents;
 	private StudentView studentView;
 	
 	// Results view
 	private VizView resultsView;
 	private JScrollPane resultsScrollView;
 	
 	// Graph view
 	private GraphView graphView;
 	private JLabel lblSpring, lblSpringLen, lblRepel, lblGrav, lblEnergy;
 	private JSlider sldSpring, sldSpringLen, sldRepel, sldGrav, sldEnergy;
 	private JPanel pnlAdvanced;
 	private JPanel pnlGraphView;
 	
 	private JPanel pnlVisualization;
 	
 	public Visualization(int fps) {
 		//
 		// Start Screen
 		//
 	
 		btnChoose = new JButton("Open a dataset...");
 		btnChoose.setFont(btnChoose.getFont().deriveFont(60.0f));
 		btnChoose.setAlignmentX(CENTER_ALIGNMENT);
 		btnChoose.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) { onOpenDataset(); }
 		});
 		
 		pnlStart = new JPanel();
 		pnlStart.setLayout(new BoxLayout(pnlStart, BoxLayout.PAGE_AXIS));
 		pnlStart.add(Box.createVerticalGlue());
 		pnlStart.add(btnChoose);
 		pnlStart.add(Box.createVerticalGlue());
 		
 		//
 		// Visualization Screen
 		//
 		
 		// Student view & button
 		studentView = new StudentView();
 		btnDiff = new JButton("Change the dataset...");
 		btnDiff.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) { onOpenDataset(); }
 		});
 		btnHelp = new JButton("Help");
 		btnHelp.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				VizState.getState().setShowingHelp(!VizState.getState().isShowingHelp());
 			}
 		});
 		
 		pnlDiff = new JPanel(new BorderLayout());
 		pnlDiff.add(btnDiff, BorderLayout.WEST);
 		pnlDiff.add(btnHelp, BorderLayout.EAST);
 		
 		pnlStudents = new JPanel(new BorderLayout());
 		pnlStudents.add(studentView, BorderLayout.CENTER);
 		pnlStudents.add(pnlDiff, BorderLayout.NORTH);
 		
 		// Results view
 		resultsView = new ResultsView();
 		resultsScrollView = new JScrollPane(resultsView);
 		resultsScrollView.setBorder(BorderFactory.createEmptyBorder());
 		resultsScrollView.addComponentListener(((ResultsView)resultsView).getListener());
 		
 		// Graph view
 		graphView = new GraphView();
 		
 		lblSpring = new JLabel("Spring", JLabel.CENTER);
 		lblSpringLen = new JLabel("Spring Length", JLabel.CENTER);
 		lblRepel = new JLabel("Repulsion", JLabel.CENTER);
 		lblGrav = new JLabel("Gravity", JLabel.CENTER);
 		lblEnergy = new JLabel("Energy", JLabel.CENTER);
 		sldSpring = new JSlider(JSlider.HORIZONTAL, 0, 1000, 500);
 		sldSpring.addChangeListener(graphView.getSpringListener());
 		sldSpringLen = new JSlider(JSlider.HORIZONTAL, 0, 1000, 750);
 		sldSpringLen.addChangeListener(graphView.getSpringLengthListener());
 		sldRepel = new JSlider(JSlider.HORIZONTAL, 0, 1000, 500);
 		sldRepel.addChangeListener(graphView.getRepulstionListener());
 		sldGrav = new JSlider(JSlider.HORIZONTAL, 0, 1000, 1000);
 		sldGrav.addChangeListener(graphView.getGravityListener());
 		sldEnergy = new JSlider(JSlider.HORIZONTAL, 0, 1000, 50);
 		sldEnergy.addChangeListener(graphView.getEnergyListener());
 		pnlAdvanced = new JPanel();
 		GroupLayout layout = new GroupLayout(pnlAdvanced);
 		pnlAdvanced.setLayout(layout);
 		layout.setHorizontalGroup(layout.createParallelGroup()
 				.addGroup(layout.createSequentialGroup()
 						.addComponent(lblSpring, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(lblSpringLen, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(lblRepel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(lblGrav, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(lblEnergy, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				.addGroup(layout.createSequentialGroup()
 						.addComponent(sldSpring, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(sldSpringLen, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(sldRepel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(sldGrav, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(sldEnergy, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
 		layout.setVerticalGroup(layout.createSequentialGroup()
 				.addGroup(layout.createParallelGroup()
 						.addComponent(lblSpring)
 						.addComponent(lblSpringLen)
 						.addComponent(lblRepel)
 						.addComponent(lblGrav)
 						.addComponent(lblEnergy))
 				.addGroup(layout.createParallelGroup()
 						.addComponent(sldSpring)
 						.addComponent(sldSpringLen)
 						.addComponent(sldRepel)
 						.addComponent(sldGrav)
 						.addComponent(sldEnergy)));
 		
 		pnlGraphView = new JPanel();
 		pnlGraphView.setLayout(new BorderLayout());
 		pnlGraphView.add(pnlAdvanced, BorderLayout.NORTH);
 		pnlGraphView.add(graphView, BorderLayout.CENTER);
 		pnlGraphView.setBorder(null);
 		
 		// Layout setup
 		testSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, pnlGraphView, resultsScrollView);
 		testSplit.setResizeWeight(.7);
 		testSplit.setDividerLocation(.7);
 		testSplit.setOneTouchExpandable(true);
 		testSplit.setBorder(BorderFactory.createEmptyBorder());
 		
 		studentSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, pnlStudents, testSplit);
 		studentSplit.setResizeWeight(.3);
 		studentSplit.setDividerLocation(.3);
 		studentSplit.setOneTouchExpandable(true);
 		studentSplit.setBorder(BorderFactory.createEmptyBorder());
 		
 		pnlVisualization = new JPanel(new BorderLayout());
 		pnlVisualization.add(studentSplit, BorderLayout.CENTER);
 		
 		//
 		// Global
 		//
 		
 		crdRoot = new CardLayout();
 		
 		pnlRoot = new JPanel(crdRoot);
 		pnlRoot.add(pnlStart, STARTSCREEN);
 		pnlRoot.add(pnlVisualization, VIZSCREEN);
 		
 		datasetChooser = new JFileChooser("..");
 		
 		this.setSize(600, 600);
 		this.add(pnlRoot);
 		this.setVisible(true);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		renderTimer = new Timer((int)(1000.0 / fps), new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				graphView.update();
 				resultsView.update();
 				repaint();
 			}
 		});
 		renderTimer.start();
 	}
 	
 	private void onOpenDataset() {
 		int result = datasetChooser.showOpenDialog(Visualization.this);
 		if (result == JFileChooser.APPROVE_OPTION) {
 			VizState.getState().setDataset(Dataset.parseFromFile(datasetChooser.getSelectedFile().getAbsolutePath()));
 			crdRoot.show(pnlRoot, VIZSCREEN);
 		}
 	}
 	
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException | InstantiationException
 				| IllegalAccessException | UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 		new Visualization(30); //TODO: This defines the fps. It should not be 1!
 	}
 }
