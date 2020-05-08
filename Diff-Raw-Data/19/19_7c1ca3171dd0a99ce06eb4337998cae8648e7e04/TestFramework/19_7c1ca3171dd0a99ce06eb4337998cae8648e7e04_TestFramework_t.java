 package test;
 
 import generator.AdaptiveAnnealingEngine;
 import generator.MoleculeAnnealerAdapter;
 import generator.MoleculeState;
 import generator.State;
 import generator.StateListener;
 import generator.Step;
 import generator.TemperatureListener;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.openscience.cdk.DefaultChemObjectBuilder;
 import org.openscience.cdk.exception.InvalidSmilesException;
 import org.openscience.cdk.interfaces.IMolecule;
 import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
 import org.openscience.cdk.smiles.SmilesGenerator;
 import org.openscience.cdk.smiles.SmilesParser;
 import org.openscience.cdk.structgen.SingleStructureRandomGenerator;
 
 import display.MoleculePanel;
 import display.SpectrumPanel;
 import display.GraphPanel;
 
 import spectrum.JudgeListener;
 import spectrum.NMRJudge;
 import spectrum.PredictionEvent;
 import spectrum.PredictionTool;
 import spectrum.Spectrum;
 
 @SuppressWarnings("serial")
 public class TestFramework extends JFrame
 	implements ActionListener, JudgeListener, StateListener,
 				TemperatureListener, ListSelectionListener {
 
 	private GraphPanel temperatureGraphPanel;
 	private GraphPanel fullScoreGraphPanel;
 	private GraphPanel acceptedScoreGraphPanel;
 	private PredictionTool predictor;
 
 	private SmilesGenerator smilesGen;
 
 	private IMolecule startingMolecule;
 	private IMolecule targetMolecule;
 
 	private MoleculePanel startingMoleculePanel;
 	private MoleculePanel targetMoleculePanel;
 	private MoleculePanel finalMoleculePanel;
 	private SpectrumPanel spectrumPanel;
 
 	private JList stepList;
 	private DefaultListModel stepListModel;
 
 	private JTextField stepField;
 	private int evalSMax = 300; // TMP an iteration guard
 	private double maxT = 0.5;
 
 //	private ArrayList<Step> steps;
 
 //	private String startingSmiles = "C(N)(O)COC(O)(N)";  				// random thing C3
 //	private String startingSmiles = "C(N)(O)CCOC(O)(N)"; 				// random thing C4
 	private String startingSmiles = "CC1CCC2CC1C2(C)C";	 				// pinane       C8
 //	private String startingSmiles = "CC1CCCC(CCC(CCC(CCC1)C)C(C)C)C";	// cembrane     C10
 //	private String startingSmiles
 //		= "CC(CCC=C(C)C)C1CCC2(C1(CCC3=C2CCC4C3(CCC(C4(C)C))C)C)C";		// lanosterol   C30!
 
 	// TMP
 	private IMolecule currentMol;
 	private Spectrum currentSpectrum;
 	private double currentScore;
 	private double currentAcceptedScore;
 	private MoleculeState.Acceptance currentAcceptance = MoleculeState.Acceptance.ACCEPT;
 
 	public TestFramework() {
 //		this.steps = new ArrayList<Step>();
 		smilesGen = new SmilesGenerator();
 
 		this.setLayout(new BorderLayout());
 
 		JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
 
 		JButton runButton = new JButton("Run");
 		runButton.setActionCommand("Run");
 		runButton.addActionListener(this);
 		buttonPanel.add(runButton);
 
 		stepField = new JTextField(String.valueOf(this.evalSMax));
 		stepField.setActionCommand("Step");
 		stepField.addActionListener(this);
 		buttonPanel.add(stepField);
 
 		// a control panel
 		JPanel controlPanel = new JPanel(new BorderLayout());
 		controlPanel.add(buttonPanel, BorderLayout.WEST);
 
 		// the graph panel
 		int graphPanelW = 800;
 		int graphPanelH = 100;
 		JPanel graphPanel = new JPanel();
 		graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.Y_AXIS));
 		this.temperatureGraphPanel =
 			new GraphPanel("temp", Color.RED,
 					maxT, evalSMax, graphPanelW, graphPanelH);
 		this.fullScoreGraphPanel =
 			new GraphPanel("full score", Color.BLACK,
 					110, evalSMax, graphPanelW, graphPanelH);
 		this.acceptedScoreGraphPanel =
 			new GraphPanel("accepted score", Color.BLUE,
 					110, evalSMax, graphPanelW, graphPanelH);
 		graphPanel.add(this.fullScoreGraphPanel);
 		graphPanel.add(this.acceptedScoreGraphPanel);
 		graphPanel.add(this.temperatureGraphPanel);
 		controlPanel.add(graphPanel, BorderLayout.CENTER);
 		this.add(controlPanel, BorderLayout.NORTH);
 
 		// molecule list
 		this.stepListModel = new DefaultListModel();
 		this.stepList = new JList(stepListModel);
 		this.stepList.addListSelectionListener(this);
 		this.add(new JScrollPane(stepList), BorderLayout.CENTER);
 
 		// display panel
 		JPanel displayPanel = new JPanel(new GridLayout(0, 1));
 
 		// spectrum sub panel
 		spectrumPanel = new SpectrumPanel();
 
 		// molecule sub panel
 		startingMoleculePanel = new MoleculePanel("Start");
 		targetMoleculePanel = new MoleculePanel("Target");
 		finalMoleculePanel = new MoleculePanel("Final");
 
 		displayPanel.add(startingMoleculePanel);
 		displayPanel.add(targetMoleculePanel);
 		displayPanel.add(finalMoleculePanel);
 		displayPanel.add(spectrumPanel);
 		this.add(displayPanel, BorderLayout.EAST);
 
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.setPreferredSize(new Dimension(900, 800));
 		this.pack();
 		this.setVisible(true);
 	}
 
 	private void setup() {
 		SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
 
 		try {
 			targetMolecule = parser.parseSmiles(startingSmiles);
 		} catch (InvalidSmilesException i) {
 
 		}
 
 		try {
 			SingleStructureRandomGenerator ssrg
 				= new SingleStructureRandomGenerator(System.currentTimeMillis());
 			ssrg.setAtomContainer((IMolecule)targetMolecule.clone());
 			boolean same = false;
 			do {
 				System.err.println("generating");
 				startingMolecule = ssrg.generate();
 				if (UniversalIsomorphismTester.isIsomorph(targetMolecule, startingMolecule)) {
 					same = true;
 				}
 			} while (same);
 		} catch (Exception e) {
 			System.err.println("problem making target"  + e);
 		}
 
 		try {
 			this.startingMoleculePanel.setMolecule(this.startingMolecule);
 			this.targetMoleculePanel.setMolecule(this.targetMolecule);
 			repaint();
 		} catch (Exception e) {
 			System.out.println("Problem in setting molecule display");
 		}
 		this.finalMoleculePanel.clear();
 		this.acceptedScoreGraphPanel.clear();
 		this.fullScoreGraphPanel.clear();
 		this.temperatureGraphPanel.clear();
 		this.stepListModel.clear();
 	}
 
 	public void changeStep() {
 
 		try {
 			int tmp = Integer.parseInt(this.stepField.getText());
 			this.evalSMax = tmp;
 			this.fullScoreGraphPanel.setStepRange(evalSMax);
 			this.temperatureGraphPanel.setStepRange(evalSMax);
 			this.acceptedScoreGraphPanel.setStepRange(evalSMax);
 		} catch (NumberFormatException nfe) {
 			nfe.printStackTrace();
 		}
 
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		String command = e.getActionCommand();
 		if (command.equals("Run")) {
 			run();
 		} else if (command.equals("Step")) {
 			System.err.println("changing step");
 			changeStep();
 		}
 	}
 
 	public int counter;	//TMP
 	public void run() {
 		setup();
 		counter = 1;	// TMP
 		if (predictor == null) {
 			try {
 				predictor = new PredictionTool("nmrshiftdb.csv");
 			} catch (IOException ioe) {
 				System.out.println("problem with prediction tool " + ioe);
 				return;
 			}
 		}
 
 		NMRJudge judge = new NMRJudge(predictor, targetMolecule);
 //		Judge judge = new SimpleHOSECodeJudge(targetMolecule);
 		judge.addJudgeListener(this);
 
		final MoleculeAnnealerAdapter adapter
 			= new MoleculeAnnealerAdapter(startingMolecule, judge);
 		adapter.addStateListener(this);
 
 		final AdaptiveAnnealingEngine annealingEngine
 			= new AdaptiveAnnealingEngine(adapter, evalSMax);
 		annealingEngine.addTemperatureListener(this);
 
 		this.stepListModel.addElement(new Step(
 						0, this.targetMolecule, startingSmiles,
 						judge.getTargetSpectrum(), 100, MoleculeState.Acceptance.ACCEPT));
 
 
 		Thread runThread = new Thread() {
 			public void run() {
 //				MoleculeAnnealerAdapter result = (MoleculeAnnealerAdapter) annealingEngine.run();
 				annealingEngine.run();
				finishUp(adapter);
 			}
 		};
 		runThread.start();

	}

	private void finishUp(MoleculeAnnealerAdapter adapter) {
 		IMolecule outcome = adapter.getBest();
 		storeStep();
 		int firstHighest = this.findFirstHighest();
 		if (firstHighest != -1) {
 			System.err.println("selecting index " + firstHighest);
 			this.stepList.setSelectedIndex(firstHighest);

			Rectangle cell
				= this.stepList.getCellBounds(firstHighest, firstHighest);
			this.stepList.scrollRectToVisible(cell);
 			this.fullScoreGraphPanel.setSelectedStep(firstHighest);
 			this.temperatureGraphPanel.setSelectedStep(firstHighest);
		} else {
			System.err.println("no selected index");
 		}
 		try {
 			this.finalMoleculePanel.setMolecule(outcome);
 		} catch (Exception e) {
 
 		}
 		this.repaint();
 	}
 
 	private int findFirstHighest() {
 		// start from one to avoid the target step, which is the first!
 		double highestScoreSeen = 0.0;
 		int highestIndexSeen = -1;
 		for (int i = 1; i < this.stepListModel.getSize(); i++) {
 			double score = ((Step)this.stepListModel.get(i)).score;
 			if (score == 100.0) return i;
 			if (score > highestScoreSeen) {
 				highestScoreSeen = score;
 				highestIndexSeen = i;
 			}
 		}
 		return highestIndexSeen;
 	}
 
 	public static void main(String[] args) {
 		new TestFramework();
 	}
 
 	public void storeStep() {
 		if (currentAcceptance == MoleculeState.Acceptance.ACCEPT) {
 			this.currentAcceptedScore = this.currentScore;
 		}
 		this.acceptedScoreGraphPanel.addValue(this.currentAcceptedScore);
 
 		String smiles = this.smilesGen.createSMILES(this.currentMol);
 		this.stepListModel.addElement(new Step(
 				counter, this.currentMol, smiles,
 				this.currentSpectrum, this.currentScore, this.currentAcceptance));
 
 	}
 
 	public void temperatureChange(double temp) {
 		this.temperatureGraphPanel.addValue(temp);
 		System.out.println("i =" + counter + " temp now = " + temp);
 
 		// this counter mirrors the internal counter...
 		storeStep();
 		counter++;
 		repaint();
 	}
 
 	public void stateChanged(State state) {
 		try {
 			MoleculeState molState = ((MoleculeState)state);
 			IMolecule molecule = molState.molecule;
 			System.out.println("new molecule");
 			this.currentMol = molecule;
 			this.currentAcceptance = molState.acceptance;
 		} catch (Exception e) {
 			System.err.println("exception in molecule state change");
 		}
 	}
 
 	public void predictionChanged(PredictionEvent p) {
 		this.spectrumPanel.setSpectrum(p.spectrum);
 		this.fullScoreGraphPanel.addValue(p.score);
 		this.currentScore = p.score;
 		this.currentSpectrum = p.spectrum;
 	}
 
 	public void valueChanged(ListSelectionEvent e) {
 		JList source = (JList)e.getSource();
 		int first = e.getFirstIndex();
 
 		if (source == this.stepList && this.stepListModel.size() > 0) {
 			Step step = (Step) this.stepListModel.get(first);
 			try {
 				this.finalMoleculePanel.setMolecule(step.smiles);
 				this.spectrumPanel.setSpectrum(step.spectrum);
 				this.fullScoreGraphPanel.setSelectedStep(step.index);
 				this.temperatureGraphPanel.setSelectedStep(step.index);
 				repaint();
 			} catch (Exception ex) {
 
 			}
 		}
 	}
 
 }
