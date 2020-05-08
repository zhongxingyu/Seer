 package raisa.vis;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
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
 
 public class VisualizerFrame extends JFrame {
 	private static final long serialVersionUID = 1L;
 	private VisualizerPanel visualizer;
 	private File defaultDirectory = null;
 	private final WorldModel worldModel;
 	
 	public VisualizerFrame(WorldModel worldModel) {
 		super("Raisa Visualizer");
 		visualizer = new VisualizerPanel(worldModel);
 		MeasurementsPanel measurementsPanel = new MeasurementsPanel(worldModel);
 		this.worldModel = worldModel;
 		JMenuBar menuBar = new JMenuBar();
 		JMenu mainMenu = new JMenu("Main");
 		mainMenu.setMnemonic('m');
 		JMenuItem reset = new JMenuItem("Reset");
 		reset.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				reset();
 			}
 		});
 		reset.setMnemonic('r');
 		JMenuItem loadSimulation = new JMenuItem("Load simulation...");
 		loadSimulation.setMnemonic('s');
 		loadSimulation.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				loadSimulation(null);
 			}
 		});
 		JMenuItem loadData = new JMenuItem("Load data...");
 		loadData.setMnemonic('d');
 		loadData.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				loadData(null);
 			}
 		});
 		JMenuItem saveAs = new JMenuItem("Save as...");
 		saveAs.setMnemonic('a');
 		saveAs.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				save(null);
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
 		mainMenu.add(loadSimulation);
 		mainMenu.add(loadData);
 		mainMenu.add(saveAs);
 		mainMenu.addSeparator();
 		mainMenu.add(exit);
 		menuBar.add(mainMenu);
 
 		JMenu viewMenu = new JMenu("View");
 		mainMenu.setMnemonic('m');
 		JMenuItem zoomIn = new JMenuItem("Zoom in");
 		zoomIn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				visualizer.zoomIn();
 			}
 		});
 		zoomIn.setMnemonic('i');
 		JMenuItem zoomOut = new JMenuItem("Zoom out");
 		zoomOut.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				visualizer.zoomOut();
 			}
 		});
 		zoomOut.setMnemonic('o');
 		
 		viewMenu.add(zoomIn);
 		viewMenu.add(zoomOut);
 		menuBar.add(viewMenu);
 
 		FailoverCommunicator communicator = new FailoverCommunicator(new SerialCommunicator(worldModel), new ConsoleCommunicator());;
 		communicator.connect();
 
 		final BasicController controller = new BasicController(communicator);
 		
 		ControlPanel controlPanel = new ControlPanel(visualizer, controller, communicator);
 		
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
 		
 		visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('+'), ZOOM_IN_ACTION_KEY);
 		visualizer.getActionMap().put(ZOOM_IN_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				visualizer.zoomIn();
 			}
 		});
 		visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('-'), ZOOM_OUT_ACTION_KEY);
 		visualizer.getActionMap().put(ZOOM_OUT_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				visualizer.zoomOut();
 			}
 		});
		visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('c'), CLEAR_HISTORY_ACTION_KEY);
 		visualizer.getActionMap().put(CLEAR_HISTORY_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				visualizer.clear();
 			}
 		});
		visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('h'), LIMIT_HISTORY_ACTION_KEY);
 		visualizer.getActionMap().put(LIMIT_HISTORY_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				visualizer.removeOldSamples();
 			}
 		});
 		visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), LEFT_ACTION_KEY);
 		visualizer.getActionMap().put(LEFT_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				controller.sendLeft();
 			}
 		});
 		visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), RIGHT_ACTION_KEY);
 		visualizer.getActionMap().put(RIGHT_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				controller.sendRight();
 			}
 		});
 		visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), STOP_ACTION_KEY);
 		visualizer.getActionMap().put(STOP_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				controller.sendStop();
 			}
 		});
 
 		visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), FORWARD_ACTION_KEY);
 		visualizer.getActionMap().put(FORWARD_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				controller.sendForward();
 			}
 		});
 
 		visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), BACK_ACTION_KEY);
 		visualizer.getActionMap().put(BACK_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				controller.sendBack();
 			}
 		});
 
 		visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('l'), LIGHTS_ACTION_KEY);
 		visualizer.getActionMap().put(LIGHTS_ACTION_KEY, new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				controller.sendLights();
 			}
 		});
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(600, 400);
 		getContentPane().add(visualizer, BorderLayout.CENTER);
 		getContentPane().add(controlPanel, BorderLayout.WEST);
 		getContentPane().add(measurementsPanel, BorderLayout.EAST);
 		setJMenuBar(menuBar);
 		setVisible(true);
 		setLocationRelativeTo(null);
 		setExtendedState(JFrame.MAXIMIZED_BOTH);
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
 
 	public void loadSimulation(String fileName) {
 		if (fileName == null) {
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
 				internalLoad(fileName, true);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void loadData(String fileName) {
 		if (fileName == null) {
 			final JFileChooser chooser = new JFileChooser(defaultDirectory);
 			chooser.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					String fileName = chooser.getSelectedFile().getAbsolutePath();
 					try {
 						saveDefaultDirectory(fileName);
 						internalLoad(fileName, false);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			});
 			chooser.showOpenDialog(this);
 		} else {
 			try {
 				internalLoad(fileName, false);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void internalSave(String fileName) throws Exception {
 		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
 		for (Sample sample : worldModel.getSamples()) {
 			writer.write(sample.sampleString);
 			writer.newLine();
 		}
 		writer.close();
 	}
 
 	private void internalLoad(String fileName, boolean delayed) throws FileNotFoundException, IOException {
 		BufferedReader fr = new BufferedReader(new FileReader(fileName));
 		List<String> sampleStrings = new ArrayList<String>();
 		String line = fr.readLine();
 		while (line != null) {
 			if (!Sample.isValid(line)) {
 				System.out.println("Invalid sample! \"" + line + "\"");
 			} else {
 				sampleStrings.add(line);
 			}
 			line = fr.readLine();
 		}
 
 		spawnSimulationThread(sampleStrings, delayed);
 	}
 
 	public void reset() {
 		visualizer.reset();
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
 								Thread.sleep(100);
 							} catch (InterruptedException e) {
 							}
 						}
 					}
 				}
 			}).start();
 		}
 	}
 
 	public VisualizerPanel getVisualizer() {
 		return visualizer;
 	}
 	
 	private void saveDefaultDirectory(String filename) {
 		defaultDirectory = new File(filename).getParentFile();
 	}
 }
