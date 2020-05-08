 package raisa.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.KeyStroke;
 
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import raisa.comms.BasicController;
 import raisa.comms.Communicator;
 import raisa.comms.ConsoleCommunicator;
 import raisa.comms.ControlMessage;
 import raisa.comms.FailoverCommunicator;
 import raisa.comms.ReplayController;
 import raisa.comms.SampleParser;
 import raisa.comms.SerialCommunicator;
 import raisa.config.VisualizerConfig;
 import raisa.domain.WorldModel;
 import raisa.domain.particlefilter.ParticleFilter;
 import raisa.domain.robot.RobotStateAggregator;
 import raisa.domain.samples.Sample;
 import raisa.session.SessionWriter;
 import raisa.simulator.RobotSimulator;
 import raisa.ui.controls.ControlPanel;
 import raisa.ui.measurements.MeasurementsPanel;
 import raisa.ui.options.VisualizationOptionsDialog;
 import raisa.ui.tool.DrawTool;
 import raisa.ui.tool.MeasureTool;
 import raisa.ui.tool.Tool;
 import raisa.util.Vector2D;
 
 public class VisualizerFrame extends JFrame {
 	private static final Logger log = LoggerFactory.getLogger(VisualizerFrame.class);
 	private static final long serialVersionUID = 1L;
 	private int nparticles = 1000;
 	private VisualizerPanel visualizerPanel;
 	private final File currentDirectory = new File(".");
 	private File defaultDirectory = new File(currentDirectory, "data");
 	private final File sessionDirectory = new File(currentDirectory, "sessions");
 	private final WorldModel worldModel;
 	private Tool currentTool;
 	private DrawTool drawTool = new DrawTool(this);
 	private MeasureTool measureTool = new MeasureTool(this);
 	private List<UserEditUndoListener> userEditUndoListeners = new ArrayList<UserEditUndoListener>();
 	private final Communicator communicator;
 	private final BasicController controller;
 	private RobotStateAggregator robotStateAggregator;	
 	private ParticleFilter particleFilter;
 	private SessionWriter sessionWriter;
 	private RobotSimulator robotSimulator;
 	private FileBasedSimulation fileBasedSimulation;
 	private VisualizationOptionsDialog visualizationOptionsDialog;
 	
 	public VisualizerFrame(final WorldModel worldModel) {
 		addIcon();
 		this.worldModel = worldModel;
 		this.particleFilter = new ParticleFilter(worldModel, nparticles);
 		this.robotStateAggregator = new RobotStateAggregator(worldModel, particleFilter, worldModel.getLandmarkManager());
 		worldModel.addSampleListener(robotStateAggregator);
 		
 		robotSimulator = RobotSimulator.createRaisaInstance(new Vector2D(0, 0), 0, worldModel);
 		
 		visualizerPanel = new VisualizerPanel(this, worldModel, robotSimulator);
 		VisualizerConfig.getInstance().addVisualizerConfigListener(visualizerPanel);
 		visualizationOptionsDialog = new VisualizationOptionsDialog(this);
 		
 		MeasurementsPanel measurementsPanel = new MeasurementsPanel(worldModel);
 		JMenuBar menuBar = new JMenuBar();
 		JMenu mainMenu = createMainMenu(worldModel, menuBar);
 		
 		createViewMenu(menuBar, mainMenu);
 
 		sessionWriter = new SessionWriter(sessionDirectory, "data");
 
 		communicator = new FailoverCommunicator(new SerialCommunicator().addSensorListener(worldModel), new ConsoleCommunicator(), sessionWriter);		
 		communicator.connect();
 		
 		fileBasedSimulation = new FileBasedSimulation(worldModel);
 		
 		robotSimulator.addSensorListener(sessionWriter, worldModel);
 		controller = new BasicController(communicator, sessionWriter, robotSimulator);
 
 		setCurrentTool(drawTool);
 		communicator.addSensorListener(sessionWriter);
 		ControlPanel controlPanel = new ControlPanel(this, visualizerPanel, controller, communicator, sessionWriter, robotSimulator);
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			@Override
 			public void run() {
 				IOUtils.closeQuietly(sessionWriter);
 			}
 		});
 				
 		createKeyboardShortCuts();
 
 		getContentPane().add(visualizerPanel, BorderLayout.CENTER);
 		getContentPane().add(controlPanel, BorderLayout.WEST);
 		getContentPane().add(measurementsPanel, BorderLayout.EAST);
 		setJMenuBar(menuBar);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	private void createKeyboardShortCuts() {
 		int nextFreeActionKey = 0;
 		final int ZOOM_IN_ACTION_KEY = ++nextFreeActionKey;
 		final int ZOOM_OUT_ACTION_KEY = ++nextFreeActionKey;
 		final int CLEAR_HISTORY_ACTION_KEY = ++nextFreeActionKey;
 		final int LIMIT_HISTORY_ACTION_KEY = ++nextFreeActionKey;
 		final int STOP_ACTION_KEY = ++nextFreeActionKey;
 		final int LEFT_ACTION_KEY = ++nextFreeActionKey;
 		final int RIGHT_ACTION_KEY = ++nextFreeActionKey;
 		final int FORWARD_ACTION_KEY = ++nextFreeActionKey;
 		final int BACK_ACTION_KEY = ++nextFreeActionKey;
 		final int LIGHTS_ACTION_KEY = ++nextFreeActionKey;
 		final int UNDO_ACTION_KEY = ++nextFreeActionKey;
 		final int REDO_ACTION_KEY = ++nextFreeActionKey;
 		final int STEP_SIMULATION_ACTION_KEY = ++nextFreeActionKey;
 		final int RANDOMIZE_PARTICLES_ACTION_KEY = ++nextFreeActionKey;
 
 		visualizerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('+'), ZOOM_IN_ACTION_KEY);
 		visualizerPanel.getActionMap().put(ZOOM_IN_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				visualizerPanel.zoomIn();
 				updateTitle();
 			}
 		});
 		visualizerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('-'), ZOOM_OUT_ACTION_KEY);
 		visualizerPanel.getActionMap().put(ZOOM_OUT_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				visualizerPanel.zoomOut();
 				updateTitle();
 			}
 		});
 		visualizerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('c'), CLEAR_HISTORY_ACTION_KEY);
 		visualizerPanel.getActionMap().put(CLEAR_HISTORY_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				visualizerPanel.clear();
 			}
 		});
 		visualizerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('h'), LIMIT_HISTORY_ACTION_KEY);
 		visualizerPanel.getActionMap().put(LIMIT_HISTORY_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				visualizerPanel.removeOldSamples();
 			}
 		});
 		visualizerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
 				LEFT_ACTION_KEY);
 		visualizerPanel.getActionMap().put(LEFT_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				controller.sendLeft();
 			}
 		});
 		visualizerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
 				RIGHT_ACTION_KEY);
 		visualizerPanel.getActionMap().put(RIGHT_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				controller.sendRight();
 			}
 		});
 		visualizerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
 				STOP_ACTION_KEY);
 		visualizerPanel.getActionMap().put(STOP_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				controller.sendStop();
 			}
 		});
 
 		visualizerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
 				FORWARD_ACTION_KEY);
 		visualizerPanel.getActionMap().put(FORWARD_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				controller.sendForward();
 			}
 		});
 
 		visualizerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
 				BACK_ACTION_KEY);
 		visualizerPanel.getActionMap().put(BACK_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				controller.sendBack();
 			}
 		});
 
 		visualizerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('l'), LIGHTS_ACTION_KEY);
 		visualizerPanel.getActionMap().put(LIGHTS_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				controller.sendLights();
 			}
 		});
 
 		visualizerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), UNDO_ACTION_KEY);
 		visualizerPanel.getActionMap().put(UNDO_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				if (isUserEditUndoable()) {
 					popUserEditUndoLevel();
 					VisualizerFrame.this.repaint();
 				}
 			}
 		});
 
 		visualizerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), REDO_ACTION_KEY);
 		visualizerPanel.getActionMap().put(REDO_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				if (isUserEditRedoable()) {
 					redoUserEditUndoLevel();
 					VisualizerFrame.this.repaint();
 				}
 			}
 		});
 
 		visualizerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('p'),
 				STEP_SIMULATION_ACTION_KEY);
 		visualizerPanel.getActionMap().put(STEP_SIMULATION_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				fileBasedSimulation.setStepSimulation(false);
 			}
 		});
 
 		visualizerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('r'),
 				RANDOMIZE_PARTICLES_ACTION_KEY);
 		visualizerPanel.getActionMap().put(RANDOMIZE_PARTICLES_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				particleFilter.randomizeParticles(nparticles);
 				repaint();
 			}
 		});
 	}
 
 	private void createViewMenu(JMenuBar menuBar, JMenu mainMenu) {
 		JMenu viewMenu = new JMenu("View");
 		mainMenu.setMnemonic('m');
 		JMenuItem zoomIn = new JMenuItem("Zoom in");
 		zoomIn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				visualizerPanel.zoomIn();
 				updateTitle();
 			}
 		});
 		zoomIn.setMnemonic('i');
 		JMenuItem zoomOut = new JMenuItem("Zoom out");
 		zoomOut.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				visualizerPanel.zoomOut();
 				updateTitle();
 			}
 		});
 		zoomOut.setMnemonic('o');
 		
 		JMenuItem visualizationOptions = new JMenuItem("Options");
 		visualizationOptions.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				visualizationOptionsDialog.setVisible(true);
 			}
 		});
 
 		viewMenu.add(zoomIn);
 		viewMenu.add(zoomOut);
 		viewMenu.addSeparator();
 		viewMenu.add(visualizationOptions);
 		
 		menuBar.add(viewMenu);
 	}
 
 	private JMenu createMainMenu(final WorldModel worldModel, JMenuBar menuBar) {
 		JMenu mainMenu = new JMenu("Main");
 		mainMenu.setMnemonic('m');
 		JMenuItem reset = new JMenuItem("Reset");
 		reset.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				reset();
 				repaint();
 			}
 		});
 		reset.setMnemonic('r');
 		JMenuItem loadData = new JMenuItem("Load sample file...");
 		loadData.setMnemonic('d');
 		loadData.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				loadSamples(null);
 			}
 		});
 		JMenuItem loadReplay = new JMenuItem("Load control file...");
 		loadReplay.setMnemonic('p');
 		loadReplay.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				loadReplay(null);
 			}
 		});
 		JMenuItem saveAs = new JMenuItem("Save samples as...");
 		saveAs.setMnemonic('a');
 		saveAs.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				save(null);
 			}
 		});
 		JMenuItem loadMap = new JMenuItem("Load map...");
 		loadMap.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				loadMap(null);
 				notifyUserEditUndoAction();
 				VisualizerFrame.this.repaint();
 			}
 		});
 		JMenuItem resetMap = new JMenuItem("Reset map");
 		resetMap.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				pushUserEditUndoLevel();
 				worldModel.resetMap();
 				VisualizerFrame.this.repaint();
 			}
 		});
 		JMenuItem saveMapAs = new JMenuItem("Save map as...");
 		saveMapAs.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				saveMap(null);
 			}
 		});
 		JMenuItem exit = new JMenuItem("Exit");
 		exit.setMnemonic('x');
 		exit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				exit();
 			}
 		});
 		mainMenu.add(reset);
 		mainMenu.addSeparator();
 		mainMenu.add(loadData);
 		mainMenu.add(loadReplay);
 		mainMenu.add(saveAs);
 		mainMenu.addSeparator();
 		mainMenu.add(resetMap);
 		mainMenu.add(loadMap);
 		mainMenu.add(saveMapAs);
 		mainMenu.addSeparator();
 		mainMenu.add(exit);
 		menuBar.add(mainMenu);
 		return mainMenu;
 	}
 	
 	public void open() {
 		updateTitle();
 		setSize(600, 400);
 		setVisible(true);
 		setLocationRelativeTo(null);
 		setExtendedState(JFrame.MAXIMIZED_BOTH);		
 	}
 
 	public void loadMap(String fileName) {
 		if (fileName == null) {
 			final JFileChooser chooser = new JFileChooser(defaultDirectory);
 			chooser.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					String fileName = chooser.getSelectedFile().getAbsolutePath();
 					try {
 						saveDefaultDirectory(fileName);
 						worldModel.loadMap(fileName);
 						particleFilter.randomizeParticles(nparticles);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			});
 			chooser.showOpenDialog(this);
 		} else {
 			try {
 				worldModel.loadMap(fileName);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	protected void saveMap(String fileName) {
 		if (fileName == null) {
 			final JFileChooser chooser = new JFileChooser(defaultDirectory);
 			chooser.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					String fileName = chooser.getSelectedFile().getAbsolutePath();
 					try {
 						saveDefaultDirectory(fileName);
 						worldModel.saveMap(fileName);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			});
 			chooser.showSaveDialog(this);
 		} else {
 			try {
 				worldModel.saveMap(fileName);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void setCurrentTool(Tool tool) {
 		this.currentTool = tool;
 	}
 
 	public void save(String fileName) {
 		if (fileName == null) {
 			final JFileChooser chooser = new JFileChooser(defaultDirectory);
 			chooser.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					String fileName = chooser.getSelectedFile().getAbsolutePath();
 					try {
 						saveDefaultDirectory(fileName);
 						internalSave(fileName);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			});
 			chooser.showSaveDialog(this);
 		} else {
 			try {
 				internalSave(fileName);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void loadSamples(String filename) {
 		if (filename == null) {
 			final JFileChooser chooser = new JFileChooser(defaultDirectory);
 			chooser.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					String fileName = chooser.getSelectedFile().getAbsolutePath();
 					try {
 						saveDefaultDirectory(fileName);
						internalLoad(fileName, true);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			});
 			chooser.showOpenDialog(this);
 		} else {
 			try {
 				internalLoad(filename, false);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void loadReplay(String filename) {
 		if (filename == null) {
 			final JFileChooser chooser = new JFileChooser(defaultDirectory);
 			chooser.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					if (chooser.getSelectedFile() == null) {
 						log.debug("Canceled replay file selection");
 						return;
 					}
 					String fileName = chooser.getSelectedFile().getAbsolutePath();
 					try {
 						saveDefaultDirectory(fileName);
 						internalLoadReplay(fileName);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			});
 			chooser.showOpenDialog(this);
 		} else {
 			try {
 				internalLoadReplay(filename);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void internalLoadReplay(String fileName) throws FileNotFoundException, IOException {
 		log.debug("Loading replay file {}", fileName);
 		BufferedReader fr = new BufferedReader(new FileReader(fileName));
 		List<ControlMessage> controlMessages = new ArrayList<ControlMessage>();
 		String line = fr.readLine();
 		while (line != null) {
 			// TODO error handling
 			ControlMessage controlMessage = ControlMessage.fromJson(line);
 			if (controlMessage != null) {
 				controlMessages.add(controlMessage);
 			}
 			line = fr.readLine();
 		}
 		log.info("Replaying {} control messages", controlMessages.size());
 		ReplayController replayController = new ReplayController(controlMessages, communicator, robotSimulator);
 		controller.copyListenersTo(replayController);
 		replayController.start();
 	}
 
 	private void internalSave(String fileName) throws Exception {
 		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
 		for (Sample sample : worldModel.getSamples()) {
 			writer.write(sample.getSampleString());
 			writer.newLine();
 		}
 		writer.close();
 	}
 
 	private void internalLoad(String fileName, boolean delayed) throws FileNotFoundException, IOException {
 		BufferedReader fr = new BufferedReader(new FileReader(fileName));
 		List<String> sampleStrings = new ArrayList<String>();
 		String line = fr.readLine();
 		SampleParser parser = new SampleParser();
 		while (line != null) {
 			if (!parser.isValid(line)) {
 				log.warn("Invalid sample! \"{}\"", line);
 			} else {
 				sampleStrings.add(line);
 			}
 			line = fr.readLine();
 		}
 
 		spawnSimulationThread(sampleStrings, delayed);
 	}
 
 	
 	public void reset() {
 		visualizerPanel.reset();
 		robotSimulator.reset();
 		particleFilter.reset();
 		updateTitle();
 	}
 
 	public void exit() {
 		System.exit(0);
 	}
 
 	public void spawnSampleSimulationThread(final List<Sample> samples, final boolean delayed) {
 		if (!samples.isEmpty()) {
 			new Thread(new Runnable() {
 				private int nextSample = 0;
 
 				@Override
 				public void run() {
 					while (nextSample < samples.size()) {
 						worldModel.addSample(samples.get(nextSample));
 						++nextSample;
 						if (delayed) {
 							try {
 								Thread.sleep(50);
 							} catch (InterruptedException e) {
 							}
 						}
 					}
 				}
 			}).start();
 		}
 	}
 
 	public void spawnSimulationThread(final List<String> samples, final boolean delayed) {
 		fileBasedSimulation.setSamples(samples, delayed);
 		fileBasedSimulation.start();
 	}
 
 	public VisualizerPanel getVisualizer() {
 		return visualizerPanel;
 	}
 
 	private void saveDefaultDirectory(String filename) {
 		defaultDirectory = new File(filename).getParentFile();
 	}
 
 	private void updateTitle() {
 		setTitle("Raisa Visualizer - " + Math.round(visualizerPanel.getScale() * 100.0f) + "%");
 	}
 
 	public void selectedMeasureTool() {
 		setCurrentTool(measureTool);
 	}
 
 	public void selectedDrawTool() {
 		setCurrentTool(drawTool);
 	}
 
 	public Tool getCurrentTool() {
 		return currentTool;
 	}
 
 	public float getScale() {
 		return visualizerPanel.getScale();
 	}
 
 	public void panCameraBy(float dx, float dy) {
 		visualizerPanel.panCameraBy(dx, dy);
 	}
 
 	public void setGridPosition(Vector2D position, boolean isBlocked) {
 		worldModel.setGridPosition(position, isBlocked);
 	}
 
 	public void setUserPosition(Vector2D position, boolean isBlocked) {
 		worldModel.setUserPosition(position, isBlocked);
 	}
 
 	public Vector2D toWorld(Vector2D screenPosition) {
 		return visualizerPanel.toWorld(screenPosition);
 	}
 
 	public float toWorld(float screenDistance) {
 		return visualizerPanel.toWorld(screenDistance);
 	}
 
 	public void pushUserEditUndoLevel() {
 		worldModel.pushUserEditUndoLevel();
 		notifyUserEditUndoAction();
 	}
 
 	private void notifyUserEditUndoAction() {
 		for (UserEditUndoListener listener : userEditUndoListeners) {
 			listener.usedEditUndoAction();
 		}
 	}
 
 	public void addUserEditUndoListener(UserEditUndoListener listener) {
 		userEditUndoListeners.add(listener);
 	}
 
 	public void popUserEditUndoLevel() {
 		worldModel.popUserEditUndoLevel();
 		notifyUserEditUndoAction();
 	}
 
 	public void redoUserEditUndoLevel() {
 		worldModel.redoUserEditUndoLevel();
 		notifyUserEditUndoAction();
 	}
 
 	public boolean isUserEditUndoable() {
 		return worldModel.isUserEditUndoable();
 	}
 
 	public boolean isUserEditRedoable() {
 		return worldModel.isUserEditRedoable();
 	}
 
 	public int getUserUndoLevels() {
 		return worldModel.getUserUndoLevels();
 	}
 
 	public int getUserRedoLevels() {
 		return worldModel.getUserRedoLevels();
 	}
 
 	public ParticleFilter getParticleFilter() {
 		return particleFilter;
 	}
 	
 	public RobotSimulator getRobotSimulator() {
 		return robotSimulator;
 	}
 
 	private void addIcon() {
 		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/raisa-icon.png")));
 	}
 
 }
