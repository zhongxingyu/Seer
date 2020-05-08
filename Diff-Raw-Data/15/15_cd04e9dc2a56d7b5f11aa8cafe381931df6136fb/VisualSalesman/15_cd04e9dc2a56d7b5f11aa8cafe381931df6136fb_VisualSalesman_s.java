 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Vector;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 
 
 public class VisualSalesman extends JFrame{
 	private static final long serialVersionUID = 1L;
 	public static final String THUMB_DIR = "images"+ System.getProperty ("file.separator");
 	public static final String DEFAULT_THUMB = THUMB_DIR+"defaultThumb.bmp";
 
 	JComboBox algoChooser;
 	JList graphList;
 	Canvas canvas;
 	Graphics g;
 	JLabel distanceLabel;
 	JLabel nameLabel;
 	JLabel oldDistanceLabel;
 	JLabel oldNameLabel;
 	JCheckBox isClumpedCheckbox;
 	Vector<GraphFile> graphs;
 	JComboBox optimizerChooser;
 
 	World w;
 	GraphFile gf;
 	int[] answer;
 	
 	String currentlyDrawnMap = "";
 	int[][] drawnPositions;
 
 	public static void main(String[] args){
 		for (LookAndFeelInfo lnf : UIManager.getInstalledLookAndFeels ()){
 			if (lnf.getName ().contains ("Nimbus"))
 				try {
 					UIManager.setLookAndFeel (lnf.getClassName ());
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				} catch (InstantiationException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				} catch (UnsupportedLookAndFeelException e) {
 					e.printStackTrace();
 				}
 		}
 		new VisualSalesman ();
 	}
 
 	public VisualSalesman (){
 		try {
 			this.setDefaultCloseOperation (DISPOSE_ON_CLOSE);
 			File thumbDir = new File (THUMB_DIR);
 			if (!thumbDir.exists ())
 				thumbDir.mkdirs ();
 			graphs = TestCaseFactory.getSavedTestCases ();
 			buildGui (graphs, TSP.algorithms, TSP.optimizations);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		pack ();
 		this.setVisible (true);
 		g = canvas.getGraphics ();
 
 	}
 
 	private void buildGui (Vector<GraphFile> graphs, Algorithm[] algorithms, Optimization opts[]){
 		this.setLayout (new BorderLayout ());
 		JPanel canvasPanel = new JPanel ();
 		canvas = new Canvas ();
 		canvas.setSize (800, 600);
 		canvas.setBackground (Color.WHITE);
 		canvasPanel.add (canvas);
 		this.add (canvasPanel, BorderLayout.WEST);
 
 		JPanel algoPanel = new JPanel ();
 		algoChooser = new JComboBox (algorithms);
 		JButton runSolverButton = new JButton ("Solve");
 		runSolverButton.addActionListener (new ActionListener (){
 
 			@Override
 			public void actionPerformed (ActionEvent e) {
 				Algorithm chosenAlgo = (Algorithm) algoChooser.getItemAt (algoChooser.getSelectedIndex ());
 				gf = (GraphFile) graphList.getSelectedValue ();
 				runSolution (chosenAlgo, gf);
 
 			}
 		});
 		algoPanel.add (algoChooser);
 		algoPanel.add (runSolverButton);
 		
 		JPanel optPanel = new JPanel ();
 		optimizerChooser = new JComboBox (opts);
 		JButton runOptimizerButton = new JButton ("Optimize once");
 		
 		runOptimizerButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Optimization o = (Optimization) optimizerChooser.getItemAt(optimizerChooser.getSelectedIndex());
 				runOptimization(o);
 			}
 		});
 		
 		
 		optPanel.add(optimizerChooser);
 		optPanel.add(runOptimizerButton);
 
 		JPanel eastPanel = new JPanel ();
 		eastPanel.setLayout (new BoxLayout (eastPanel, BoxLayout.Y_AXIS));
 		JScrollPane listPane = new JScrollPane ();
 		graphList = new JList (graphs);
 		graphList.getSelectionModel ().addListSelectionListener (new GraphListListener ());
 		listPane.setViewportView (graphList);
 
 		JButton removeGraphButton = new JButton ("Remove");
 		JButton generateGraphButton = new JButton ("Generate");
 		isClumpedCheckbox = new JCheckBox ("Clump");
 		generateGraphButton.addActionListener (new GenerateButtonListener ());
 		removeGraphButton.addActionListener (new RemoveButtonListener ());
 		JPanel graphButtons = new JPanel ();
 		graphButtons.setLayout (new BoxLayout (graphButtons, BoxLayout.X_AXIS));
 		graphButtons.add (generateGraphButton);
 		graphButtons.add (isClumpedCheckbox);
 		graphButtons.add (removeGraphButton);
 		
 
 		JPanel resultsPanel = getResultTextPane ();
 
 		eastPanel.add (listPane);
 		eastPanel.add (graphButtons);
 		eastPanel.add (algoPanel);
 		eastPanel.add (optPanel);
 		eastPanel.add (resultsPanel);
 
 
 		JPanel eastContainer = new JPanel ();
 		eastContainer.setLayout (new BorderLayout ());
 		eastContainer.add (eastPanel);
 		eastContainer.add (resultsPanel, BorderLayout.SOUTH);
 		this.add (eastContainer, BorderLayout.EAST);
 
 	}
 
 	private JPanel getResultTextPane (){
 
 		JLabel oldTitle = new JLabel ();
 		oldTitle.setText ("Latest solution");
 
 		JLabel newTitle = new JLabel ();
 		newTitle.setText ("solution");
 
 		distanceLabel = new JLabel ();
 		distanceLabel.setText ("    ");
 
 		nameLabel = new JLabel ();
 		nameLabel.setText ("   ");
 
 		oldDistanceLabel = new JLabel ();
 		oldDistanceLabel.setText ("    ");
 
 		oldNameLabel = new JLabel ();
 		oldNameLabel.setText ("    ");
 
 		JPanel resultsPanel = new JPanel ();
 		resultsPanel.setLayout (new BoxLayout (resultsPanel, BoxLayout.Y_AXIS));
 
 		resultsPanel.add (newTitle);
 		resultsPanel.add (distanceLabel);
 		resultsPanel.add (nameLabel);
 
 		resultsPanel.add (oldTitle);
 		resultsPanel.add (oldDistanceLabel);
 		resultsPanel.add (oldNameLabel);
 
 		return resultsPanel;
 
 	}
 
 	private void runSolution (Algorithm chosenAlgo, GraphFile gf) {
 		
 		try {
 			w = TSP.makeWorld (new BufferedReader(new FileReader(gf.file)));
 			answer = TSP.solveForWorld (chosenAlgo, w);
 			drawAnswer (answer, w, gf.name);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 	
 	private void runOptimization (Optimization opt) {
 		try {
			TraversalGraph g = new TraversalGraph(answer, w);
			g = TSP.optimizeResult(opt, g, w);
			
			answer = g.toIntArray();
 			drawAnswer (answer, w, gf.name);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private void drawAnswer (int[] answer, World w, String name) {
 		clearCanvas ();
 		drawMap (w, name, true);
 		Color lineColor = Color.RED;
 		for (int i=0;i<answer.length-1;i++){
 			int from = answer[i];
 			int to = answer[i+1];
 			drawLineBetween (from, to, lineColor);
 		}
 
 		drawLineBetween (answer[answer.length-1], answer[0], lineColor);
 		if (Utils.answerHasDuplicates (answer)){
 			Color frgnd = distanceLabel.getForeground ();
 			distanceLabel.setForeground (Color.RED);
 			distanceLabel.setText ("ANSWER HAS DUPLICATE");
 			distanceLabel.setForeground (frgnd);
 		}else{
 			Utils.printAnswer (answer);
 			updateResultTexts (Utils.getAnswerDistance (answer, w));
 		}
 	}
 
 	private void updateResultTexts (double answerDistance) {
 		String distString = "Total distance "+answerDistance;
 		distString = distString.substring (0, distString.indexOf ('.')+2);
 		Algorithm algo = (Algorithm) algoChooser.getSelectedItem ();
 		String algoName = algo.toString ();
 		oldDistanceLabel.setText (distanceLabel.getText ());
 		oldNameLabel.setText (nameLabel.getText ());
 
 		distanceLabel.setText (distString);
 		nameLabel.setText (algoName+" | "+currentlyDrawnMap);
 
 	}
 
 	private void drawLineBetween (int from, int to, Color paintColor){
 		Color tmp = g.getColor ();
 		g.setColor (paintColor);
 		int fromX = drawnPositions[from][0];
 		int fromY = drawnPositions[from][1];
 		int toX = drawnPositions[to][0];
 		int toY = drawnPositions[to][1];
 
 		g.drawLine (fromX, fromY, toX, toY);
 		g.setColor (tmp);
 	}
 
 	private void drawMap (World w, String name, boolean forceRedraw){
 		if (!forceRedraw && name.equals (currentlyDrawnMap))
 			return;
 		clearCanvas ();
 		
 		double widthFactor = getScaleFactor (w.getWidth (), canvas.getWidth ()-20);
 		double heightFactor = getScaleFactor (w.getHeight (), canvas.getHeight ()-20);
 		
 		drawnPositions = calculatePositions (w, heightFactor, widthFactor);
 		for (int i=0;i<w.getSize ();i++){
 			int x = drawnPositions[i][0]; int y = drawnPositions[i][1];
 			drawCityAt (x,y, widthFactor, heightFactor);
 
 		}
 		currentlyDrawnMap = name;
 
 	}
 	
 	private double getScaleFactor (double graph, int canvas){
 		return canvas / graph;
 	}
 
 	private void drawCityAt (int x, int y, double xF, double yF) {
 		g.drawOval (x, y, 3, 3);
 
 	}
 
 	/*
 	 * Recalculate positions in graph to pixel positions in canvas
 	 */
 	private int[][] calculatePositions (World w, double heightFactor, double widthFactor) {
 		
 
 		double[][] positions = w.getPositions ();
 		int[][] scaledPositions = new int[positions.length][positions[0].length];
 		for (int i=0;i<positions.length;i++){
 			double graphX = positions[i][0];
 			double graphY = positions[i][1];
 
 			int scaledX = (int) (widthFactor * graphX)+10;
 			int scaledY = (int) (heightFactor * graphY)+10;
 
 			scaledPositions[i][0] = scaledX;
 			scaledPositions[i][1] = scaledY;
 		}
 
 		return scaledPositions;
 	}
 
 	private void clearCanvas (){
 		Color tmp = g.getColor ();
 		g.setColor (canvas.getBackground ());
 		g.clearRect (0, 0, canvas.getWidth (), canvas.getHeight ());
 		g.setColor (tmp);
 
 	}
 
 	private class GenerateButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed (ActionEvent e) {
 			try {
 				GraphFile gf = TestCaseFactory.askAndCreateGraphFile ();
 				if (gf == null){
 					JOptionPane.showMessageDialog (((Component) e.getSource ()).getParent (), "Invalid format");
 					return;
 				}
 				gf.setIsClumped (isClumpedCheckbox.isSelected ());
 				TestCaseFactory.writeGraph (gf);
 				TestCaseFactory.addFileToFileList (gf);
 				graphs.add(gf);
 				graphList.updateUI ();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		}
 	}
 
 	private class RemoveButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed (ActionEvent e) {
 			GraphFile gf = (GraphFile) graphList.getSelectedValue ();
 			removeFile (gf.file);
 			updateGraphDb (gf);
 			graphs.remove (gf);
 			graphList.updateUI ();
 		}
 
 		private void updateGraphDb (GraphFile gf) {
 			TestCaseFactory.removeGraphFromDb (gf);
 		}
 
 		private void removeFile (File f){
 			if (f.exists ())
 				f.delete ();
 
 		}
 	}
 
 	private class GraphListListener implements ListSelectionListener {
 
 		@Override
 		public void valueChanged (ListSelectionEvent e) {
 
 			try {
 				if (e.getValueIsAdjusting ()){
 					return;
 				}
 				GraphFile gf = (GraphFile) graphList.getSelectedValue ();
 				if (gf.name.equals (currentlyDrawnMap))
 					return;
 				World w = TSP.makeWorld (new BufferedReader (new FileReader (gf.file)));
 				drawMap (w, gf.name, false);
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 
 		}
 
 	}
 
 }
