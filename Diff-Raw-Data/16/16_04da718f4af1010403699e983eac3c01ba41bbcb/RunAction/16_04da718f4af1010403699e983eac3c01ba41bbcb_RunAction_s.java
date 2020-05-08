 package inat.cytoscape;
 
 import giny.model.Edge;
 import giny.model.Node;
 import inat.analyser.LevelResult;
 import inat.analyser.SMCResult;
 import inat.analyser.uppaal.ResultAverager;
 import inat.analyser.uppaal.UppaalModelAnalyserFasterConcrete;
 import inat.analyser.uppaal.VariablesModel;
 import inat.exceptions.InatException;
 import inat.model.Model;
 import inat.model.Reactant;
 import inat.model.Reaction;
 import inat.model.Scenario;
 import inat.model.ScenarioMono;
 import inat.network.UPPAALClient;
 import inat.util.Table;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFormattedTextField;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.task.Task;
 import cytoscape.task.TaskMonitor;
 import cytoscape.task.ui.JTaskConfig;
 import cytoscape.task.util.TaskManager;
 import cytoscape.util.CytoscapeAction;
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.cytopanels.CytoPanel;
 import cytoscape.view.cytopanels.CytoPanelImp;
 import cytoscape.view.cytopanels.CytoPanelState;
 
 /**
  * The run action runs the network through the INAT analyser.
  * 
  * @author Brend Wanders
  * 
  */
 public class RunAction extends CytoscapeAction {
 	private static final long serialVersionUID = -5018057013811632477L;
 	private static final String NUMBER_OF_LEVELS = Model.Properties.NUMBER_OF_LEVELS, //The total number of levels for a node (=reactant), or for the whole network (the name of the property is the same)
 								SECONDS_PER_POINT = Model.Properties.SECONDS_PER_POINT, //The number of real-life seconds represented by a single UPPAAL time unit
 								SECS_POINT_SCALE_FACTOR = Model.Properties.SECS_POINT_SCALE_FACTOR, //The scale factor for the UPPAAL time settings, allowing to keep the same scenario parameters, while varying the "density" of simulation sample points
 								INCREMENT = Model.Properties.INCREMENT, //The increment in activity caused by a reaction on its downstream reactant
 								BI_REACTION = Model.Properties.BI_REACTION, //Identifies a reaction having two reatants
 								MONO_REACTION = Model.Properties.MONO_REACTION, //Identifies a reaction having only one reactant
 								REACTANT = Model.Properties.REACTANT, //The name of the reactant taking part to the reaction
 								CATALYST = Model.Properties.CATALYST, //The name of the catalyst enabling the reaction
 								SCENARIO = Model.Properties.SCENARIO, //The id of the scenario used to set the parameters for an edge (=reaction)
 								CYTOSCAPE_ID = Model.Properties.CYTOSCAPE_ID, //The id assigned to the node/edge by Cytoscape
 								CANONICAL_NAME = Model.Properties.CANONICAL_NAME, //The name of a reactant displayed to the user
 								INITIAL_LEVEL = Model.Properties.INITIAL_LEVEL, //The starting activity level of a reactant
 								UNCERTAINTY = Model.Properties.UNCERTAINTY, //The uncertainty about the parameters setting for an edge(=reaction)
 								ENABLED = Model.Properties.ENABLED, //Whether the node/edge is enabled. Influences the display of that node/edge thanks to the discrete Visual Mapping defined by AugmentAction
 								GROUP = Model.Properties.GROUP; //Could possibly be never used. All nodes(=reactants) belonging to the same group represent alternative (in the sense of exclusive or) phosphorylation sites of the same protein.
 	private static final int VERY_LARGE_TIME_VALUE = 1073741822;
 	private int timeTo = 1200; //The default number of UPPAAL time units until which a simulation will run
 	private double scale = 0.2; //The time scale representing the number of real-life minutes represented by a single UPPAAL time unit
 	private JRadioButton remoteUppaal, smcUppaal; //The RadioButtons telling us whether we use a local or a remote engine, and whether we use the Statistical Model Checking or the "normal" engine
 	private JCheckBox computeStdDev; //Whether to compute the standard deviation when computing the average of a series of runs (if average of N runs is requested)
 	private JFormattedTextField timeToFormula, nSimulationRuns; //Up to which point in time (real-life minutes) the simulation(s) will run, and the number of simulations (if average of N runs is requested)
 	private JTextField serverName, serverPort, smcFormula; //The name of the server, and the corresponding port, in the case we use a remote engine. The text inserted by the user for the SMC formula. Notice that this formula will need to be changed so that it will be compliant with the UPPAAL time scale, and reactant names
 	private boolean needToStop; //Whether the user has pressed the Cancel button on the TaskMonitor while we were running an analysis process
 	private RunAction meStesso; //Myself
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param plugin the plugin we should use
 	 */
 	public RunAction(InatPlugin plugin, JRadioButton remoteUppaal, JTextField serverName, JTextField serverPort, JRadioButton smcUppaal, JFormattedTextField timeToFormula, JFormattedTextField nSimulationRuns, JCheckBox computeStdDev, JTextField smcFormula) {
 		super("Analyse network");
 		this.remoteUppaal = remoteUppaal;
 		this.serverName = serverName;
 		this.serverPort = serverPort;
 		this.smcUppaal = smcUppaal;
 		this.timeToFormula = timeToFormula;
 		this.nSimulationRuns = nSimulationRuns;
 		this.computeStdDev = computeStdDev;
 		this.smcFormula = smcFormula;
 		this.meStesso = this;
 	}
 	
 	public static String timeDifferenceFormat(long startTime, long endTime) {
 		long diffInSeconds = (endTime - startTime) / 1000;
 	    long diff[] = new long[] { 0, 0, 0, 0 };
 	    /* sec */diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
 	    /* min */diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
 	    /* hours */diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
 	    /* days */diff[0] = (diffInSeconds = (diffInSeconds / 24));
 	    
 	    return String.format(
 		        "%d day%s, %d hour%s, %d minute%s, %d second%s",
 		        diff[0],
 		        diff[0] != 1 ? "s" : "",
 		        diff[1],
 		        diff[1] != 1 ? "s" : "",
 		        diff[2],
 		        diff[2] != 1 ? "s" : "",
 		        diff[3],
 		        diff[3] != 1 ? "s" : "");
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		RunTask task = new RunTask();
 
 		// Configure JTask Dialog Pop-Up Box
 		JTaskConfig jTaskConfig = new JTaskConfig();
 		jTaskConfig.setOwner(Cytoscape.getDesktop());
 		// jTaskConfig.displayCloseButton(true);
 		// jTaskConfig.displayCancelButton(true);
 
 		jTaskConfig.displayStatus(true);
 		jTaskConfig.setAutoDispose(true);
 		jTaskConfig.displayCancelButton(true);
 		jTaskConfig.displayTimeElapsed(true);
 		
 		long startTime = System.currentTimeMillis();
 		Date now = new Date(startTime);
 		File logFile = null;
 		PrintStream logStream = null;
 		PrintStream oldErr = System.err;
 		try {
 			logFile = File.createTempFile("Cytoscape run " + now.toString(), ".log");
 			logFile.deleteOnExit();
 			logStream = new PrintStream(new FileOutputStream(logFile));
 			System.setErr(logStream);
 		} catch (Exception ex) {
 			//We have no log file, bad luck: we will have to use System.err.
 		}
 		
 		// Execute Task in New Thread; pops open JTask Dialog Box.
 		TaskManager.executeTask(task, jTaskConfig);
 		
 		long endTime = System.currentTimeMillis();
 		
 		try {
 			System.err.println("Time taken: " + timeDifferenceFormat(startTime, endTime));
 			System.err.flush();
 			System.setErr(oldErr);
 			if (logStream != null) {
 				logStream.close();
 			}
 		} catch (Exception ex) {
 			
 		}
 	}
 	
 	public boolean needToStop() {
 		return this.needToStop;
 	}
 
 	private class RunTask implements Task {
 
 		private static final String TIMES_U = Model.Properties.TIMES_UPPER;
 		private static final String TIMES_L = Model.Properties.TIMES_LOWER;
 		private static final String REACTION_TYPE = Model.Properties.REACTION_TYPE;
 		private static final String REACTANT_NAME = Model.Properties.REACTANT_NAME;
 		private static final String REACTANT_ALIAS = Model.Properties.ALIAS;
 		private TaskMonitor monitor;
 
 		@Override
 		public String getTitle() {
 			return "INAT analysis";
 		}
 
 		@Override
 		public void halt() {
 			needToStop = true;
 		}
 
 		@Override
 		public void run() {
 			try {
 				needToStop = false;
 				
 				this.monitor.setStatus("Creating model representation");
 				this.monitor.setPercentCompleted(0);
 				
 				final Model model = this.getInatModel();
 				
 				if (smcUppaal.isSelected()) {
 					performSMCAnalysis(model);
 				} else {
 					performNormalAnalysis(model);
 				}
 				
 			} catch (InterruptedException e) {
 				this.monitor.setException(e, "Analysis cancelled by the user.");
 			} catch (Exception e) {
 				this.monitor.setException(e, "An error occurred while analysing the network.");
 			}
 		}
 		
 		/**
 		 * Translate the SMC formula into UPPAAL time units, reactant names
 		 * and give it to the analyser. Show the result in a message window.
 		 * @param model
 		 * @throws Exception
 		 */
 		private void performSMCAnalysis(final Model model) throws Exception {
 			//TODO: "understand" the formula and correctly change time values and reagent names
 			String probabilisticFormula = smcFormula.getText();
 			for (Reactant r : model.getReactants()) {
 				String name = r.get(REACTANT_ALIAS).as(String.class);
 				if (probabilisticFormula.contains(name)) {
 					probabilisticFormula = probabilisticFormula.replace(name, r.getId());
 				}
 			}
 			if (probabilisticFormula.contains("Pr[<")) {
 	            String[] parts = probabilisticFormula.split("Pr\\[<");
 	            StringBuilder sb = new StringBuilder();
 	            for (String p : parts) {
 	               if (p.length() < 1) continue;
 	               String timeS;
 	               if (p.startsWith("=")) {
 	                  timeS = p.substring(1, p.indexOf("]"));
 	               } else {
 	                  timeS = p.substring(0, p.indexOf("]"));
 	               }
 	               int time;
 	               try {
 	                  time = Integer.parseInt(timeS);
 	               } catch (Exception ex) {
 	                  throw new Exception("Problems with the identification of time string \"" + timeS + "\"");
 	               }
 	               time = (int)(time * 60.0 / model.getProperties().get(SECONDS_PER_POINT).as(Double.class));
 	               sb.append("Pr[<");
 	               if (p.startsWith("=")) {
 	                  sb.append("=");
 	               }
 	               sb.append(time);
 	               sb.append(p.substring(p.indexOf("]")));
 	            }
 	            probabilisticFormula = sb.toString();
 	         }
 
 			
 			this.monitor.setStatus("Analysing model with UPPAAL");
 			this.monitor.setPercentCompleted(-1);
 
 			// analyse model
 			final SMCResult result;
 			
 			if (remoteUppaal.isSelected()) {
 				UPPAALClient client = new UPPAALClient(serverName.getText(), Integer.parseInt(serverPort.getText()));
 				result = client.analyzeSMC(model, probabilisticFormula);
 			} else {
 				result = new UppaalModelAnalyserFasterConcrete(monitor, meStesso).analyzeSMC(model, probabilisticFormula);
 			}
 			
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					JOptionPane.showMessageDialog(Cytoscape.getDesktop(), result.toString(), "Result", JOptionPane.INFORMATION_MESSAGE);
 				}
 			});
 			
 		}
 		
 		/**
 		 * Perform a simulation analysis. Translate the user-set number of real-life minutes
 		 * for the length of the simulation, and obtain all input data for the model engine,
 		 * based on the control the user has set (average, N simulation, StdDev, etc).
 		 * When the analysis is done, display the obtained SimpleLevelResult on a ResultPanel
 		 * @param model
 		 * @throws Exception
 		 */
 		private void performNormalAnalysis(final Model model) throws Exception {
 
 			int nMinutesToSimulate = 0;
 			try {
 				nMinutesToSimulate = Integer.parseInt(timeToFormula.getValue().toString());
 			} catch (Exception ex) {
 				throw new Exception("Unable to understand the number of minutes requested for the simulation.");
 			}
 				/*(int)(timeTo * model.getProperties().get(SECONDS_PER_POINT).as(Double.class) / 60);
 			String inputTime = JOptionPane.showInputDialog(Cytoscape.getDesktop(), "Up to which time (in real-life MINUTES)?", nMinutesToSimulate);
 			if (inputTime != null) {
 				try {
 					nMinutesToSimulate = Integer.parseInt(inputTime);
 				} catch (Exception ex) {
 					//the default value is still there, so nothing to change
 				}
 			} else {
 				return;
 			}*/
 			
 			timeTo = (int)(nMinutesToSimulate * 60.0 / model.getProperties().get(SECONDS_PER_POINT).as(Double.class));
 			scale = (double)nMinutesToSimulate / timeTo;
 			
 			//this.monitor.setStatus("Analysing model with UPPAAL");
 			this.monitor.setPercentCompleted(-1);
 
 			// composite the analyser (this should be done from
 			// configuration)
 			//ModelAnalyser<LevelResult> analyzer = new UppaalModelAnalyser(new VariablesInterpreter(), new VariablesModel());
 
 			// analyse model
 			final LevelResult result;
 			
 			if (remoteUppaal.isSelected()) {
 				UPPAALClient client = new UPPAALClient(serverName.getText(), Integer.parseInt(serverPort.getText()));
 				int nSims = 1;
 				if (nSimulationRuns.isEnabled()) {
 					try {
 						nSims = Integer.parseInt(nSimulationRuns.getText());
 					} catch (Exception e) {
 						throw new Exception("Unable to understand the number of requested simulations.");
 					}
 				} else {
 					nSims = 1;
 				}
 				result = client.analyze(model, timeTo, nSims, computeStdDev.isSelected());
 			} else {
 				//ModelAnalyser<LevelResult> analyzer = new UppaalModelAnalyser(new VariablesInterpreter(), new VariablesModel());
 				//result = analyzer.analyze(model, timeTo);
 				if (nSimulationRuns.isEnabled()) {
 					int nSims = 0;
 					try {
 						nSims = Integer.parseInt(nSimulationRuns.getText());
 					} catch (Exception e) {
 						throw new Exception("Unable to understand the number of requested simulations.");
 					}
 					result = new ResultAverager(monitor, meStesso).analyzeAverage(model, timeTo, nSims, computeStdDev.isSelected());
 				} else {
 					result = new UppaalModelAnalyserFasterConcrete(monitor, meStesso).analyze(model, timeTo);
 				}
 			}
 
 			/*CsvWriter csvWriter = new CsvWriter();
 			csvWriter.writeCsv("/tmp/test.csv", model, result);*/
 
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					final CytoPanel p = Cytoscape.getDesktop().getCytoPanel(SwingConstants.EAST);
 
 					// JFrame frame = new JFrame("Inat result viewer");
 					// frame.setLayout(new BorderLayout());
 					InatResultPanel resultViewer = new InatResultPanel(model, result, scale);
 					// frame.add(resultViewer, BorderLayout.CENTER);
 					// frame.setLocationRelativeTo(Cytoscape.getDesktop());
 					// frame.pack();
 					// frame.setSize(new Dimension(800, 600));
 					// frame.setVisible(true);
 
 					final JPanel container = new JPanel(new BorderLayout(2, 2));
 					container.add(resultViewer, BorderLayout.CENTER);
 					JPanel buttons = new JPanel(new GridLayout(1, 4, 2, 2));
 
 					JButton close = new JButton(new AbstractAction("Close") {
 						private static final long serialVersionUID = 4327349309742276633L;
 
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							p.remove(container);
 						}
 					});
 
 					buttons.add(close);
 					container.add(buttons, BorderLayout.NORTH);
 
 					p.add("INAT Results", container);
 
 					if (p.getState().equals(CytoPanelState.HIDE)) {
 						CytoPanelImp p1 = (CytoPanelImp)Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST);
 						CyNetworkView p2 = Cytoscape.getCurrentNetworkView();
 						CytoPanelImp p3 = (CytoPanelImp)Cytoscape.getDesktop().getCytoPanel(SwingConstants.SOUTH);
 						Dimension d = Cytoscape.getDesktop().getSize();
 						if (!p1.getState().equals(CytoPanelState.HIDE)) {
 							d.width -= p1.getWidth();
 						}
 						if (p2 != null) {
 							d.width -= Cytoscape.getDesktop().getNetworkViewManager().getInternalFrame(p2).getWidth();
 						}
 						if (!p3.getState().equals(CytoPanelState.HIDE)) {
 							d.height -= p3.getHeight();
 						}
 						((CytoPanelImp)p).setPreferredSize(d);
 						((CytoPanelImp)p).setMaximumSize(d);
 						((CytoPanelImp)p).setSize(d);
 						p.setState(CytoPanelState.DOCK);
 					}
 				}
 			});
 		}
 
 		@Override
 		public void setTaskMonitor(TaskMonitor monitor) throws IllegalThreadStateException {
 			this.monitor = monitor;
 		}
 
 		/**
 		 * Translate the Cytoscape network in the internal INAT model representation.
 		 * This intermediate model will then translated as needed into the proper UPPAAL
 		 * model by the analysers. All properties needed from the Cytoscape network are
 		 * copied in the resulting model, checking that all are set ok.
 		 * @return The intermediate INAT model
 		 * @throws InatException
 		 */
 		@SuppressWarnings("unchecked")
 		private Model getInatModel() throws InatException {
 			checkParameters();
 			
 			Map<String, String> nodeNameToId = new HashMap<String, String>();
 			Map<String, String> edgeNameToId = new HashMap<String, String>();
 			
 			Model model = new Model();
 			
 			CyNetwork network = Cytoscape.getCurrentNetwork();
 			
 			final int totalWork = network.getNodeCount() + network.getEdgeCount();
 			int doneWork = 0;
 			
 			CyAttributes networkAttributes = Cytoscape.getNetworkAttributes();
 			
 			model.getProperties().let(NUMBER_OF_LEVELS).be(networkAttributes.getAttribute(network.getIdentifier(), NUMBER_OF_LEVELS));
 			model.getProperties().let(SECONDS_PER_POINT).be(networkAttributes.getAttribute(network.getIdentifier(), SECONDS_PER_POINT));
 			double secStepFactor = networkAttributes.getDoubleAttribute(network.getIdentifier(), SECS_POINT_SCALE_FACTOR);
 			model.getProperties().let(SECS_POINT_SCALE_FACTOR).be(secStepFactor);
 			
 			final Integer MaxNLevels = networkAttributes.getIntegerAttribute(network.getIdentifier(), NUMBER_OF_LEVELS);
 			final Double nSecondsPerPoint = networkAttributes.getDoubleAttribute(network.getIdentifier(), SECONDS_PER_POINT);
 			
 			model.getProperties().let(NUMBER_OF_LEVELS).be(MaxNLevels);
 			model.getProperties().let(SECONDS_PER_POINT).be(nSecondsPerPoint);
 			
 			// do nodes first
 			CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
 			final Iterator<Node> nodes = (Iterator<Node>) network.nodesIterator();
 			for (int i = 0; nodes.hasNext(); i++) {
 				this.monitor.setPercentCompleted((100 * doneWork++) / totalWork);
 				Node node = nodes.next();
 				
 				final String reactantId = "reactant" + i;
 				Reactant r = new Reactant(reactantId);
 				nodeNameToId.put(node.getIdentifier(), reactantId);
 				
 				r.let(CYTOSCAPE_ID).be(node.getIdentifier());
 				r.let(REACTANT_NAME).be(node.getIdentifier());
 				r.let(REACTANT_ALIAS).be(nodeAttributes.getAttribute(node.getIdentifier(), CANONICAL_NAME));
 				r.let(NUMBER_OF_LEVELS).be(nodeAttributes.getAttribute(node.getIdentifier(), NUMBER_OF_LEVELS));
 				r.let(GROUP).be(nodeAttributes.getAttribute(node.getIdentifier(), GROUP));
 				r.let(ENABLED).be(nodeAttributes.getAttribute(node.getIdentifier(), ENABLED));
 				r.let(INITIAL_LEVEL).be(nodeAttributes.getIntegerAttribute(node.getIdentifier(), INITIAL_LEVEL));
 				
 				model.add(r);
 			}
 			
 			
 			// do edges next
 			CyAttributes edgeAttributes = Cytoscape.getEdgeAttributes();
 			final Iterator<Edge> edges = (Iterator<Edge>) network.edgesIterator();
 			for (int i = 0; edges.hasNext(); i++) {
 				this.monitor.setPercentCompleted((100 * doneWork++) / totalWork);
 				Edge edge = edges.next();
 				
 				String reactionId = "reaction" + i;
 				Reaction r = new Reaction(reactionId);
 				edgeNameToId.put(edge.getIdentifier(), reactionId);
 				
 				r.let(ENABLED).be(edgeAttributes.getAttribute(edge.getIdentifier(), ENABLED));
 				r.let(INCREMENT).be(edgeAttributes.getAttribute(edge.getIdentifier(), INCREMENT));
 
 				if (edge.getSource() == edge.getTarget()) {
 					r.let(REACTION_TYPE).be(MONO_REACTION);
 
 					final String reactant = nodeNameToId.get(edge.getTarget().getIdentifier());
 					r.let(REACTANT).be(reactant);
 					
 					int nLevels;
 					
 					if (!model.getReactant(reactant).get(NUMBER_OF_LEVELS).isNull()) {
 						nLevels = model.getReactant(reactant).get(NUMBER_OF_LEVELS).as(Integer.class);
 					} else {
 						nLevels = model.getProperties().get(NUMBER_OF_LEVELS).as(Integer.class);
 					}
 					
 					ScenarioMono scenario = new ScenarioMono();
 					
 					String[] parameters = scenario.listVariableParameters();
 					for (int j = 0;j < parameters.length;j++) {
 						Double parVal = edgeAttributes.getDoubleAttribute(edge.getIdentifier(), parameters[j]);
 						if (parVal != null) {
 							scenario.setParameter(parameters[j], parVal);
 						} else {
 							//this should never happen, because the parameter should at least have its default value (see checkParameters)
 						}
 					}
 					
 					int uncertainty = edgeAttributes.getIntegerAttribute(edge.getIdentifier(), UNCERTAINTY);
 					
 					List<Double> times = scenario.generateTimes(1 + nLevels);
 					Table timesLTable = new Table(nLevels + 1, 1);
 					Table timesUTable = new Table(nLevels + 1, 1);
 					
 					for (int j = 0; j < nLevels + 1; j++) {
 						Double t = times.get(j);
 						if (Double.isInfinite(t)) {
 							timesLTable.set(j, 0, VariablesModel.INFINITE_TIME);
 							timesUTable.set(j, 0, VariablesModel.INFINITE_TIME);
 						} else if (uncertainty == 0) {
 							timesLTable.set(j, 0, (int)Math.round(secStepFactor * t));
 							timesUTable.set(j, 0, (int)Math.round(secStepFactor * t));
 						} else {
 							timesLTable.set(j, 0, Math.max(1, (int)Math.round(secStepFactor * t * (100.0 - uncertainty) / 100.0))); //we use Math.max because we do not want to put 0 as a time
 							timesUTable.set(j, 0, Math.max(1, (int)Math.round(secStepFactor * t * (100.0 + uncertainty) / 100.0)));
 						}
 					}
 					r.let(TIMES_L).be(timesLTable);
 					r.let(TIMES_U).be(timesUTable);
 
 				} else {
 					r.let(REACTION_TYPE).be(BI_REACTION);
 
 					final String reactant = nodeNameToId.get(edge.getTarget().getIdentifier());
 					r.let(REACTANT).be(reactant);
 
 					final String catalyst = nodeNameToId.get(edge.getSource().getIdentifier());
 					r.let(CATALYST).be(catalyst);
 					
 					int nLevelsR1,
 						nLevelsR2;
 					
 					if (!model.getReactant(catalyst).get(NUMBER_OF_LEVELS).isNull()) {
 						nLevelsR1 = model.getReactant(catalyst).get(NUMBER_OF_LEVELS).as(Integer.class);
 					} else {
 						nLevelsR1 = model.getProperties().get(NUMBER_OF_LEVELS).as(Integer.class);
 					}
 					if (!model.getReactant(reactant).get(NUMBER_OF_LEVELS).isNull()) {
 						nLevelsR2 = model.getReactant(reactant).get(NUMBER_OF_LEVELS).as(Integer.class);
 					} else {
 						nLevelsR2 = model.getProperties().get(NUMBER_OF_LEVELS).as(Integer.class);
 					}
 
 					Scenario[] scenarios = Scenario.sixScenarios;
 					Integer scenarioIdx;
 					/*if (edgeAttributes.hasAttribute(edge.getIdentifier(), SCENARIO)) {
 						scenarioIdx = edgeAttributes.getIntegerAttribute(edge.getIdentifier(), SCENARIO);
 					} else {
 						//we do this thing in checkParameters
 						scenarioIdx = 0;
 					}*/
 					scenarioIdx = edgeAttributes.getIntegerAttribute(edge.getIdentifier(), SCENARIO);
 					Scenario scenario = scenarios[scenarioIdx];
 					
 					String[] parameters = scenario.listVariableParameters();
 					for (int j = 0;j < parameters.length;j++) {
 						Double parVal = edgeAttributes.getDoubleAttribute(edge.getIdentifier(), parameters[j]);
 						if (parVal != null) {
 							scenario.setParameter(parameters[j], parVal);
 						} else {
 							//checkParameters should make sure that each parameter is present, at least with its default value
 						}
 					}
 					
 					int uncertainty = edgeAttributes.getIntegerAttribute(edge.getIdentifier(), UNCERTAINTY);
 					
 					boolean activatingReaction = true;
 					if (edgeAttributes.getIntegerAttribute(edge.getIdentifier(), INCREMENT) > 0) {
 						activatingReaction = true;
 					} else {
 						activatingReaction = false;
 					}
 					List<Double> times = scenario.generateTimes(1 + nLevelsR1, 1 + nLevelsR2, activatingReaction);
 					Table timesLTable = new Table(nLevelsR2 + 1, nLevelsR1 + 1);
 					Table timesUTable = new Table(nLevelsR2 + 1, nLevelsR1 + 1);
 					
 					for (int j = 0; j < nLevelsR2 + 1; j++) {
 						for (int k = 0; k < nLevelsR1 + 1; k++) {
 							Double t = times.get(j * (nLevelsR1 + 1) + k);
 							if (Double.isInfinite(t)) {
 								timesLTable.set(j, k, VariablesModel.INFINITE_TIME);
 								timesUTable.set(j, k, VariablesModel.INFINITE_TIME);
 							} else if (uncertainty == 0) {
 								timesLTable.set(j, k, (int)Math.round(secStepFactor * t));
 								timesUTable.set(j, k, (int)Math.round(secStepFactor * t));
 							} else {
 								timesLTable.set(j, k, Math.max(1, (int)Math.round(secStepFactor * t * (100.0 - uncertainty) / 100.0)));
 								timesUTable.set(j, k, Math.max(1, (int)Math.round(secStepFactor * t * (100.0 + uncertainty) / 100.0)));
 							}
 						}
 					}
 					r.let(TIMES_L).be(timesLTable);
 					r.let(TIMES_U).be(timesUTable);
 				}
 
 				model.add(r);
 			}
 			
 			/*This should not be necessary any more, as we do that in checkParameters()
 			//check that the number of levels is present in each reactant
 			Integer defNumberOfLevels = model.getProperties().get(NUMBER_OF_LEVELS).as(Integer.class);
 			for (Reactant r : model.getReactants()) {
 				Integer nLvl = r.get(NUMBER_OF_LEVELS).as(Integer.class);
 				if (nLvl == null) {
 					Property nameO = r.get(REACTANT_ALIAS);
 					String name;
 					if (nameO == null) {
 						name = r.getId();
 					} else {
 						name = nameO.as(String.class);
 					}
 					String inputLevels = JOptionPane.showInputDialog("Missing number of levels for reactant \"" + name + "\" (" + r.getId() + ").\nPlease insert the max number of levels for \"" + name + "\"", defNumberOfLevels);
 					if (inputLevels != null) {
 						try {
 							nLvl = new Integer(inputLevels);
 						} catch (Exception ex) {
 							nLvl = defNumberOfLevels;
 						}
 					} else {
 						nLvl = defNumberOfLevels;
 					}
 					r.let(NUMBER_OF_LEVELS).be(nLvl);
 					//System.err.println("Numbero di livelli di " + r.get("cytoscape id").as(String.class) + " = " + nLvl);
 					nodeAttributes.setAttribute(r.get(CYTOSCAPE_ID).as(String.class), NUMBER_OF_LEVELS, nLvl);
 				}
 			}*/
 			
 			
 			return model;
 		}
 
 		
 		/**
 		 * Check that all parameters are ok. If possible, ask the user to
 		 * input parameters on the fly. If this is not possible, throw an
 		 * exception specifying what parameters are missing.
 		 */
 		@SuppressWarnings("unchecked")
 		private void checkParameters() throws InatException {
 			
 			CyNetwork network = Cytoscape.getCurrentNetwork();
 			CyAttributes networkAttributes = Cytoscape.getNetworkAttributes();
 			CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
 			CyAttributes edgeAttributes = Cytoscape.getEdgeAttributes();
 			
 			
 			//============================== FIRST PART: CHECK THAT ALL PROPERTIES ARE SET =====================================
 			//TODO: we could collect the list of all things that were set automatically and show them before continuing with the
 			//generation of the model. Alternatively, we could throw exceptions like bullets for any slight misbehavior =)
 			//Another alternative is to collect the list of what we want to change, and actually make the changes only after the
 			//user has approved them. Otherwise, interrupt the analysis by throwing exception.
 			
 			if (!networkAttributes.hasAttribute(network.getIdentifier(), NUMBER_OF_LEVELS)) {
 				//throw new InatException("Network attribute '" + NUMBER_OF_LEVELS + "' is missing.");
 				int defaultNLevels = 15;
				String inputLevels = JOptionPane.showInputDialog(Cytoscape.getDesktop(), "Missing number of levels for the network. Please insert the max number of levels", defaultNLevels);
 				Integer nLvl;
 				if (inputLevels != null) {
 					try {
 						nLvl = new Integer(inputLevels);
 					} catch (Exception ex) {
 						nLvl = defaultNLevels;
 					}
 				} else {
 					nLvl = defaultNLevels;
 				}
 				networkAttributes.setAttribute(network.getIdentifier(), NUMBER_OF_LEVELS, nLvl);
 			}
 			
 			if (!networkAttributes.hasAttribute(network.getIdentifier(), SECONDS_PER_POINT)) {
 				//throw new InatException("Network attribute '" + SECONDS_PER_POINT + "' is missing.");
 				double defaultSecondsPerPoint = 12;
				String inputSecs = JOptionPane.showInputDialog(Cytoscape.getDesktop(), "Missing number of seconds per point for the network.\nPlease insert the number of real-life seconds a simulation point will represent", defaultSecondsPerPoint);
 				Double nSecPerPoint;
 				if (inputSecs != null) {
 					try {
 						nSecPerPoint = new Double(inputSecs);
 					} catch (Exception ex) {
 						nSecPerPoint = defaultSecondsPerPoint;
 					}
 				} else {
 					nSecPerPoint = defaultSecondsPerPoint;
 				}
 				networkAttributes.setAttribute(network.getIdentifier(), SECONDS_PER_POINT, nSecPerPoint);
 			}
 			
 			double secStepFactor;
 			if (networkAttributes.hasAttribute(network.getIdentifier(), SECS_POINT_SCALE_FACTOR)) {
 				secStepFactor = networkAttributes.getDoubleAttribute(network.getIdentifier(), SECS_POINT_SCALE_FACTOR);
 			} else {
 				secStepFactor = 1.0;
 				networkAttributes.setAttribute(network.getIdentifier(), SECS_POINT_SCALE_FACTOR, secStepFactor);
 			}
 			
 			Iterator<Node> nodes = (Iterator<Node>) network.nodesIterator();
 			for (int i = 0; nodes.hasNext(); i++) {
 				Node node = nodes.next();
 				if (!nodeAttributes.hasAttribute(node.getIdentifier(), ENABLED)) {
 					nodeAttributes.setAttribute(node.getIdentifier(), ENABLED, true);
 				}
 				
 				if (!nodeAttributes.hasAttribute(node.getIdentifier(), NUMBER_OF_LEVELS)) {
 					nodeAttributes.setAttribute(node.getIdentifier(), NUMBER_OF_LEVELS, networkAttributes.getIntegerAttribute(network.getIdentifier(), NUMBER_OF_LEVELS));
 				}
 				
 				if (!nodeAttributes.hasAttribute(node.getIdentifier(), INITIAL_LEVEL)) {
 					//throw new InatException("Node attribute 'initialConcentration' is missing on '" + node.getIdentifier() + "'");
 					nodeAttributes.setAttribute(node.getIdentifier(), INITIAL_LEVEL, 0);
 				}
 			}
 			
 			Iterator<Edge> edges = (Iterator<Edge>) network.edgesIterator();
 			for (int i = 0; edges.hasNext(); i++) {
 				Edge edge = edges.next();
 				if (!edgeAttributes.hasAttribute(edge.getIdentifier(), ENABLED)) {
 					edgeAttributes.setAttribute(edge.getIdentifier(), ENABLED, true);
 				}
 				
 				//Check that the edge has a selected scenario
 				if (!edgeAttributes.hasAttribute(edge.getIdentifier(), SCENARIO)) {
 					edgeAttributes.setAttribute(edge.getIdentifier(), SCENARIO, 0);
 				}
 				//Check that the edge has the definition of all parameters requested by the selected scenario
 				//otherwise set the parameters to their default values
 				Scenario scenario;
 				if (edge.getSource().equals(edge.getTarget())) {
 					scenario = new ScenarioMono();
 				} else {
 					scenario = Scenario.sixScenarios[edgeAttributes.getIntegerAttribute(edge.getIdentifier(), SCENARIO)];
 				}
 				String[] paramNames = scenario.listVariableParameters();
 				for (String param : paramNames) {
 					if (!edgeAttributes.hasAttribute(edge.getIdentifier(), param)) {
 						edgeAttributes.setAttribute(edge.getIdentifier(), param, scenario.getDefaultParameterValue(param));
 					}
 				}
 				
 				if (!edgeAttributes.hasAttribute(edge.getIdentifier(), UNCERTAINTY)) {
 					edgeAttributes.setAttribute(edge.getIdentifier(), UNCERTAINTY, 0);
 				}
 				
 				if (!edgeAttributes.hasAttribute(edge.getIdentifier(), INCREMENT)) {
 					edgeAttributes.setAttribute(edge.getIdentifier(), INCREMENT, 1);
 				}
 			}
 			
 			
 			//============ SECOND PART: MAKE SURE THAT REACTION PARAMETERS IN COMBINATION WITH TIME POINTS DENSITY (SECONDS/POINT) DON'T GENERATE BAD PARAMETERS FOR UPPAAL =============
 			
 			double minSecStep = Double.NEGATIVE_INFINITY, maxSecStep = Double.POSITIVE_INFINITY, //The lower bound of the "valid" interval for secs/step (minSecStep) is the maximum of the lower bounds we find for it, while the upper bound (maxSecStep) is the minimum of all upper bounds. This is why we compute them in this apparently strange way
 				   secPerStep = networkAttributes.getDoubleAttribute(network.getIdentifier(), SECONDS_PER_POINT);
 			
 			
 			edges = (Iterator<Edge>) network.edgesIterator();
 			for (int i = 0; edges.hasNext(); i++) {
 				Edge edge = edges.next();
 				if (edge.getSource() == edge.getTarget()) {
 					String rId = edge.getSource().getIdentifier();
 					
 					int nLevels;
 					if (nodeAttributes.hasAttribute(rId, NUMBER_OF_LEVELS)) {
 						nLevels = nodeAttributes.getIntegerAttribute(rId, NUMBER_OF_LEVELS);
 					} else {
 						nLevels = networkAttributes.getIntegerAttribute(network.getIdentifier(), NUMBER_OF_LEVELS);
 					}
 					
 					ScenarioMono scenario = new ScenarioMono();
 					String[] parameters = scenario.listVariableParameters();
 					for (int j = 0;j < parameters.length;j++) {
 						Double parVal = edgeAttributes.getDoubleAttribute(edge.getIdentifier(), parameters[j]);
 						if (parVal != null) {
 							scenario.setParameter(parameters[j], parVal);
 						} else {
 							//TODO: show the editing window
 						}
 					}
 					
 					int uncertainty;
 					if (edgeAttributes.hasAttribute(edge.getIdentifier(), UNCERTAINTY)) {
 						uncertainty = edgeAttributes.getIntegerAttribute(edge.getIdentifier(), UNCERTAINTY);
 					} else {
 						uncertainty = 0;
 					}
 					
 					Double massimo = scenario.computeFormula(1),
 						minimo = scenario.computeFormula(nLevels);
 					int massimoUB,
 						minimoLB;
 					if (!Double.isInfinite(massimo)) {
 						massimoUB = Math.max(1, (int)Math.round(secStepFactor * massimo * (100.0 + uncertainty) / 100.0));
 					} else {
 						massimoUB = VariablesModel.INFINITE_TIME;
 					}
 					if (!Double.isInfinite(minimo)) {
 						minimoLB = Math.max(1, (int)Math.round(secStepFactor * minimo * (100.0 - uncertainty) / 100.0));
 					} else {
 						minimoLB = VariablesModel.INFINITE_TIME;
 					}
 					if (massimoUB > VERY_LARGE_TIME_VALUE) {
 						//System.err.println("La reazione " + nodeAttributes.getAttribute(rId, Model.Properties.CANONICAL_NAME) + " --| " + nodeAttributes.getAttribute(rId, Model.Properties.CANONICAL_NAME) + " ha un numero troppo alto in angolo alto-sx!! (1)");
 						double rate = scenario.computeRate(1);
 						double proposedSecStep = secPerStep / (VERY_LARGE_TIME_VALUE * rate / (secStepFactor * (100.0 + uncertainty) / 100.0)); //Math.ceil(secPerStep / (VERY_LARGE_TIME_VALUE * rate / ((100.0 + uncertainty) / 100.0)));
 						if (proposedSecStep > minSecStep) {
 							minSecStep = proposedSecStep;
 						}
 					}
 					if (minimoLB == 1) {
 						//System.err.println("La reazione " + nodeAttributes.getAttribute(rId, Model.Properties.CANONICAL_NAME) + " --| " + nodeAttributes.getAttribute(rId, Model.Properties.CANONICAL_NAME) + " ha un uno in angolo basso-dx!! (" + nLevels + ")");
 						double rate = scenario.computeRate(nLevels);
 						double proposedSecStep = secPerStep / (1.5 * rate / (secStepFactor * (100.0 - uncertainty) / 100.0)); //Math.floor(secPerStep / (1.5 * rate / ((100.0 - uncertainty) / 100.0)));
 						if (proposedSecStep < maxSecStep) {
 							maxSecStep = proposedSecStep;
 						}
 					}
 				} else {
 					String r1Id = edge.getSource().getIdentifier(),
 						   r2Id = edge.getTarget().getIdentifier();
 				
 					int nLevelsR1, nLevelsR2;
 					if (nodeAttributes.hasAttribute(r1Id, NUMBER_OF_LEVELS)) {
 						nLevelsR1 = nodeAttributes.getIntegerAttribute(r1Id, NUMBER_OF_LEVELS);
 					} else {
 						//TODO: il controllo per la presenza dei livelli non l'ho ancora fatto a questo punto!!
 						//suggerisco di fare una funzione apposta per fare tutta la serie di controlli che facciamo all'inizio di getModel
 						//a cui aggiungiamo in coda questo controllo sugli uni (e numeri troppo grandi)!
 						nLevelsR1 = networkAttributes.getIntegerAttribute(network.getIdentifier(), NUMBER_OF_LEVELS);
 					}
 					if (nodeAttributes.hasAttribute(r2Id, NUMBER_OF_LEVELS)) {
 						nLevelsR2 = nodeAttributes.getIntegerAttribute(r2Id, NUMBER_OF_LEVELS);
 					} else {
 						nLevelsR2 = networkAttributes.getIntegerAttribute(network.getIdentifier(), NUMBER_OF_LEVELS);
 					}
 					
 					Scenario[] scenarios = Scenario.sixScenarios;
 					Integer scenarioIdx = edgeAttributes.getIntegerAttribute(edge.getIdentifier(), SCENARIO);
 					if (scenarioIdx == null) {
 						//TODO: show the editing window
 						scenarioIdx = 0;
 					}
 					Scenario scenario = scenarios[scenarioIdx];
 					
 					String[] parameters = scenario.listVariableParameters();
 					for (int j = 0;j < parameters.length;j++) {
 						Double parVal = edgeAttributes.getDoubleAttribute(edge.getIdentifier(), parameters[j]);
 						if (parVal != null) {
 							scenario.setParameter(parameters[j], parVal);
 						} else {
 							//TODO: show the editing window
 						}
 					}
 					
 					int uncertainty;
 					if (edgeAttributes.hasAttribute(edge.getIdentifier(), UNCERTAINTY)) {
 						uncertainty = edgeAttributes.getIntegerAttribute(edge.getIdentifier(), UNCERTAINTY);
 					} else {
 						uncertainty = 0;
 					}
 					
 					boolean activatingReaction = true;
 					if (edgeAttributes.hasAttribute(edge.getIdentifier(), INCREMENT) && edgeAttributes.getIntegerAttribute(edge.getIdentifier(), INCREMENT) > 0) {
 						activatingReaction = true;
 					} else {
 						activatingReaction = false;
 					}
 					
 					if (activatingReaction) {
 						//System.err.println("Controllo la reazione " + nodeAttributes.getAttribute(r1Id, Model.Properties.CANONICAL_NAME) + " --> " + nodeAttributes.getAttribute(r2Id, Model.Properties.CANONICAL_NAME) + "..."); 
 						Double angoloAltoDx = scenario.computeFormula(nLevelsR1, 0, activatingReaction),
 							angoloBassoSx = scenario.computeFormula(1, nLevelsR2 - 1, activatingReaction);
 						int angoloAltoDxLB,
 							angoloBassoSxUB;
 						if (!Double.isInfinite(angoloAltoDx)) {
 							angoloAltoDxLB = Math.max(1, (int)Math.round(secStepFactor * angoloAltoDx * (100.0 - uncertainty) / 100.0));
 						} else {
 							angoloAltoDxLB = VariablesModel.INFINITE_TIME;
 						}
 						if (!Double.isInfinite(angoloBassoSx)) {
 							angoloBassoSxUB = Math.max(1, (int)Math.round(secStepFactor * angoloBassoSx * (100.0 + uncertainty) / 100.0));
 						} else {
 							angoloBassoSxUB = VariablesModel.INFINITE_TIME;
 						}
 						if (angoloAltoDxLB == 1) {
 							//System.err.println("La reazione " + nodeAttributes.getAttribute(r1Id, Model.Properties.CANONICAL_NAME) + " --> " + nodeAttributes.getAttribute(r2Id, Model.Properties.CANONICAL_NAME) + " ha un uno in angolo alto-dx!! (" + (nLevelsR1) + ", " + 0 + ")");
 							double rate = scenario.computeRate(nLevelsR1, 0, activatingReaction);
 							/*if (rate > 1) {
 								System.err.println("\tIl rate (" + rate + ")  infatti > 1");
 							} else {
 								System.err.println("\tIl rate (" + rate + ") per NON  > 1! Il reciproco viene " + (int)Math.round(1 / rate) + ", ma l'uno ce l'abbiamo perch facciamo -" + uncertainty + "%, che viene appunto " + ((int)((int)Math.round(1 / rate) * (100.0 - uncertainty) / 100.0)));
 							}*/
 							//System.err.println("\tQuindi consiglio di DIVIDERE sec/step almeno per " + (1.5 * rate / ((100.0 - uncertainty) / 100.0)) + ", ottenendo quindi non pi di " + (secPerStep / (1.5 * rate / (secStepFactor * (100.0 - uncertainty) / 100.0))));
 							double proposedSecStep = secPerStep / (1.5 * rate / (secStepFactor * (100.0 - uncertainty) / 100.0)); //Math.floor(secPerStep / (1.5 * rate / ((100.0 - uncertainty) / 100.0)));
 							if (proposedSecStep < maxSecStep) {
 								maxSecStep = proposedSecStep;
 							}
 						}
 						if (angoloBassoSxUB > VERY_LARGE_TIME_VALUE) {
 							//System.err.println("La reazione " + nodeAttributes.getAttribute(r1Id, Model.Properties.CANONICAL_NAME) + " --> " + nodeAttributes.getAttribute(r2Id, Model.Properties.CANONICAL_NAME) + " ha un numero troppo alto in angolo basso-sx!! (" + 1 + ", " + (nLevelsR2 - 1) + ")");
 							double rate = scenario.computeRate(1, nLevelsR2 - 1, activatingReaction);
 							//In questo caso si consiglia di dividere sec/step per un fattore < (VERY_LARGE_TIME_VALUE * rate / ((100.0 + uncertainty) / 100.0))
 							double proposedSecStep = secPerStep / (VERY_LARGE_TIME_VALUE * rate / (secStepFactor * (100.0 + uncertainty) / 100.0)); //Math.ceil(secPerStep / (VERY_LARGE_TIME_VALUE * rate / ((100.0 + uncertainty) / 100.0)));
 							if (proposedSecStep > minSecStep) {
 								minSecStep = proposedSecStep;
 							}
 						}
 					} else {
 						//System.err.println("Controllo la reazione " + nodeAttributes.getAttribute(r1Id, Model.Properties.CANONICAL_NAME) + " --| " + nodeAttributes.getAttribute(r2Id, Model.Properties.CANONICAL_NAME) + "...");
 						Double angoloBassoDx = scenario.computeFormula(nLevelsR1, nLevelsR2, activatingReaction),
 							angoloAltoSx = scenario.computeFormula(1, 1, activatingReaction);
 						int angoloBassoDxLB,
 							angoloAltoSxUB;
 						if (!Double.isInfinite(angoloBassoDx)) {
 							angoloBassoDxLB = Math.max(1, (int)Math.round(secStepFactor * angoloBassoDx * (100.0 - uncertainty) / 100.0));
 						} else {
 							angoloBassoDxLB = VariablesModel.INFINITE_TIME;
 						}
 						if (!Double.isInfinite(angoloAltoSx)) {
 							angoloAltoSxUB = Math.max(1, (int)Math.round(secStepFactor * angoloAltoSx * (100.0 + uncertainty) / 100.0));
 						} else {
 							angoloAltoSxUB = VariablesModel.INFINITE_TIME;
 						}
 						if (angoloBassoDxLB == 1) {
 							//System.err.println("La reazione " + nodeAttributes.getAttribute(r1Id, Model.Properties.CANONICAL_NAME) + " --| " + nodeAttributes.getAttribute(r2Id, Model.Properties.CANONICAL_NAME) + " ha un uno in angolo basso-dx!! (" + (nLevelsR1) + ", " + (nLevelsR2) + ")");
 							double rate = scenario.computeRate(nLevelsR1, nLevelsR2, activatingReaction);
 							/*if (rate > 1) {
 								System.err.println("\tIl rate (" + rate + ")  infatti > 1");
 							} else {
 								System.err.println("\tIl rate (" + rate + ") per NON  > 1! Il reciproco viene " + (int)Math.round(1 / rate) + ", ma l'uno ce l'abbiamo perch facciamo -" + uncertainty + "%, che viene appunto " + ((int)((int)Math.round(1 / rate) * (100.0 - uncertainty) / 100.0)));
 							}*/
 							//System.err.println("\tQuindi consiglio di DIVIDERE sec/step almeno per " + (1.5 * rate / ((100.0 - uncertainty) / 100.0)) + ", ottenendo quindi non pi di " + (secPerStep / (1.5 * rate / (secStepFactor * (100.0 - uncertainty) / 100.0))));
 							double proposedSecStep = secPerStep / (1.5 * rate / (secStepFactor * (100.0 - uncertainty) / 100.0)); //Math.floor(secPerStep / (1.5 * rate / ((100.0 - uncertainty) / 100.0)));
 							if (proposedSecStep < maxSecStep) {
 								maxSecStep = proposedSecStep;
 							}
 						}
 						if (angoloAltoSxUB > VERY_LARGE_TIME_VALUE) {
 							//System.err.println("La reazione " + nodeAttributes.getAttribute(r1Id, Model.Properties.CANONICAL_NAME) + " --| " + nodeAttributes.getAttribute(r2Id, Model.Properties.CANONICAL_NAME) + " ha un numero troppo alto in angolo alto-sx!! (1, 1)");
 							double rate = scenario.computeRate(1, 1, activatingReaction);
 							//In questo caso si consiglia di dividere sec/step per un fattore < (VERY_LARGE_TIME_VALUE * rate / ((100.0 + uncertainty) / 100.0))
 							double proposedSecStep = secPerStep / (VERY_LARGE_TIME_VALUE * rate / (secStepFactor * (100.0 + uncertainty) / 100.0)); //Math.ceil(secPerStep / (VERY_LARGE_TIME_VALUE * rate / ((100.0 + uncertainty) / 100.0)));
 							if (proposedSecStep > minSecStep) {
 								minSecStep = proposedSecStep;
 							}
 						}
 					}
 				}
 			}
 			if (!Double.isInfinite(minSecStep) || !Double.isInfinite(maxSecStep)) {
 				System.err.println("As far as I see from the computations, a valid interval for secs/point is [" + minSecStep + ", " + maxSecStep + "]");
 			}
 			if (!Double.isInfinite(maxSecStep) && secPerStep > maxSecStep) {
 				System.err.println("\tThe current setting is over the top: " + secPerStep + " > " + maxSecStep + ", so take " + maxSecStep);
 				secPerStep = maxSecStep;
 				networkAttributes.setAttribute(network.getIdentifier(), SECONDS_PER_POINT, secPerStep);
 			} else {
 				//System.err.println("\tNon vado sopra il massimo: " + secPerStep + " <= " + maxSecStep);
 			}
 			if (!Double.isInfinite(minSecStep) && secPerStep < minSecStep) { //Notice that this check is made last because it is the most important: if we set seconds/point to a value less than the computed minimum, the time values will be so large that UPPAAL will not be able to understand them, thus producing no result
 				System.err.println("\tThe current seetting is under the bottom: " + secPerStep + " < " + minSecStep + ", so take " + minSecStep);
 				secPerStep = minSecStep;
 				networkAttributes.setAttribute(network.getIdentifier(), SECONDS_PER_POINT, secPerStep);
 			} else {
 				//System.err.println("\tNon vado neanche sotto il minimo: " + secPerStep + " >= " + minSecStep);
 			}
 			
 			Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
 
 		}
 	}
 }
