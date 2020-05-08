 /*
  *   Copyright (C) 2006  The Concord Consortium, Inc.,
  *   25 Love Lane, Concord, MA 01742
  *
  *   This program is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation; either version 2 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program; if not, write to the Free Software
  *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 package org.concord.mw2d.models;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.EventQueue;
 import java.awt.Shape;
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.BitSet;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Vector;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import javax.swing.Action;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.JProgressBar;
 import javax.swing.KeyStroke;
 import javax.swing.event.ChangeListener;
 import javax.swing.undo.UndoManager;
 
 import org.concord.modeler.ConnectionManager;
 import org.concord.modeler.DisasterHandler;
 import org.concord.modeler.Model;
 import org.concord.modeler.ModelerUtilities;
 import org.concord.modeler.Movie;
 import org.concord.modeler.PageBarGraph;
 import org.concord.modeler.PageXYGraph;
 import org.concord.modeler.ScriptCallback;
 import org.concord.modeler.SlideMovie;
 import org.concord.modeler.draw.FillMode;
 import org.concord.modeler.event.AbstractChange;
 import org.concord.modeler.event.ModelEvent;
 import org.concord.modeler.event.ModelListener;
 import org.concord.modeler.event.PageComponentEvent;
 import org.concord.modeler.event.PageComponentListener;
 import org.concord.modeler.event.ScriptListener;
 import org.concord.modeler.process.AbstractLoadable;
 import org.concord.modeler.process.ImageStreamGenerator;
 import org.concord.modeler.process.Job;
 import org.concord.modeler.process.Loadable;
 import org.concord.modeler.ui.ProcessMonitor;
 import org.concord.modeler.util.DataQueue;
 import org.concord.modeler.util.FileUtilities;
 import org.concord.modeler.util.FloatQueue;
 import org.concord.modeler.util.HomoQueueGroup;
 import org.concord.modeler.util.SwingWorker;
 import org.concord.mw2d.AtomisticView;
 import org.concord.mw2d.MDState;
 import org.concord.mw2d.MDView;
 import org.concord.mw2d.MesoModelProperties;
 import org.concord.mw2d.ModelProperties;
 import org.concord.mw2d.MolecularModelProperties;
 import org.concord.mw2d.event.ParameterChangeListener;
 import org.concord.mw2d.event.UpdateEvent;
 import org.concord.mw2d.event.UpdateListener;
 
 /**
  * For physical units used in the molecular dynamics simulations, we assume the following:
  * <ul>
  * <li> Avogadro's constant = 6 x 10<sup>23</sup> per mole.
  * <li> An electron volt (eV) = 1.6 x 10<sup>-19</sup> joules.
  * <li> Boltzmann's constant = 1.38 x 10<sup>-23</sup> joules/kelvin.
  * <li> Planck's constant (h-bar) = 6.583 x 10<sup>-16</sup> eV*s
  * <li> Unit of mass = 120 g/mol (about 10 times of that of a carbon atom).
  * <li> Unit of length (which maps to a pixel) = 0.1 angstrom = 10<sup>-11</sup> meter.
  * <li> Unit of time = femtosecond = 10<sup>-15</sup> second.
  * <li> Unit of temperature = kelvin.
  * </ul>
  * <p>
  * <b>Note to the API users</b>: The reason that the unit of mass is taken as 120 g/mol is because of an old mistake in
  * unit conversion. This mistake cannot be corrected because a lot of models have been made with it. The negative effect
  * of this mistake is not as severe as it appears to be, though, as everything in the molecular models is relative. If
  * you are aware of this problem and you are to simulate a realistic system such as the helium gas, the only thing
  * related to this mistake that you have to do is to set the mass of the Lennard-Jones particle that is supposed to
  * represent He to be 4/120.
  * </p>
  * <p>
  * A positive effect of this mistake is that, as we also use Lennard-Jones particles to represent big molecules, such as
  * amino acids and nucleotides, this can be regarded as a deliberate setting, instead of a mistake. :)
  * </p>
  * 
  * @author Charles Xie
  */
 
 public abstract class MDModel implements Model, ParameterChangeListener {
 
 	public final static short MAXWELL_SPEED_DISTRIBUTION = 1071;
 	public final static short MAXWELL_VELOCITY_DISTRIBUTION = 1072;
 
 	public final static String COMPUTE_MSD = "Mean Square Displacements";
 
 	/** the default width of a model */
 	public static final short DEFAULT_WIDTH = 525;
 
 	/** the default height of a model */
 	public static final short DEFAULT_HEIGHT = 250;
 
 	/*
 	 * converts energy gradient unit into force unit: 1.6E-19 [J] / ( E-11 [m] x 120E-3 / 6E23 [kg] ) / ( E-11 / ( E-15 ) ^
 	 * 2 ) [m/s^2]
 	 */
 	final static float GF_CONVERSION_CONSTANT = 0.008f;
 
 	/*
 	 * convert m*v*v into eV: ( 120E-3 / 6E23 ) [kg] x ( E-11 / E-15 )^2 [m^2/s^2] / 1.6E-19 [J] divided by 2 (save the
 	 * multiplier prefactor 0.5 for computing kinetic energy)
 	 */
 	final static float EV_CONVERTER = 100.0f / 1.6f;
 
 	/* converts temperature unit into velocity unit */
 	final static float VT_CONVERSION_CONSTANT = 0.00002882f;
 
 	/* converts electronic volt into temperature unit */
 	final static float UNIT_EV_OVER_KB = 16000.0f / 1.38f;
 
 	/* the minimum double number that is considered zero for judging changes. */
 	final static double ZERO = 0.0000000001;
 
 	final static Random RANDOM = new Random();
 
 	private final static String[] REMINDER_OPTIONS = { "Close", "Snapshot", "Continue" };
 
 	/*
 	 * In reality, Planck's constant = 6.626E-34 m^2kg/s. Here it is an adjustable parameter. The greater it is, the
 	 * more significant the quantum effect will be.
 	 */
 	static float PLANCK_CONSTANT = 0.2f;
 
 	PageComponentEvent modelChangeEvent;
 	int defaultTapeLength = DataQueue.DEFAULT_SIZE;
 
 	private Eval2D evalAction; // evaluator for action scripts
 	private Eval2D evalTask; // evaluator for task scripts
 	private Thread evalThread;
 	private ScriptCallback externalScriptCallback;
 	boolean initializationScriptToRun;
 	private final AtomicBoolean isLoading = new AtomicBoolean();
 
 	List<ModelListener> modelListenerList;
 	List<PageComponentListener> pageComponentListenerList;
 	List<UpdateListener> updateListenerList, updateListenerListCopy;
 	Vector<VectorField> fields;
 	ObstacleCollection obstacles;
 	RectangularBoundary boundary;
 	Universe universe;
 	SlideMovie movie;
 	ProcessMonitor monitor;
 	volatile float modelTime;
 	String initializationScript;
 
 	float range_xmin = 20, range_xmax = 20, range_ymin = 20, range_ymax = 20;
 	short id_xmin, id_xmax, id_ymin, id_ymax;
 
 	List<String> computeList;
 	Map<Object, Object> properties;
 	JProgressBar ioProgressBar;
 	ModelProperties modelProp;
 	Map<String, Action> actionMap;
 	Map<String, ChangeListener> changeMap;
 	Map<String, Action> switchMap;
 	Map<String, Action> choiceMap;
 	Map<String, Action> multiSwitchMap;
 	TimeSeriesRepository timeSeriesRepository;
 	ImageStreamGenerator imageStreamGenerator;
 	StateHolder stateHolder;
 
 	/* channels for outputing computed results */
 	double[] channels = new double[8];
 
 	/* time series for the channels */
 	FloatQueue[] channelTs = new FloatQueue[8];
 
 	/* current value of the potential energy */
 	double pot;
 
 	/* time series of potential energy */
 	FloatQueue pote;
 
 	/* current value of the kinetic energy */
 	double kin;
 
 	/* time series of kinetic energy */
 	FloatQueue kine;
 
 	/* current value of the total energy */
 	double tot;
 
 	/* time series of total energy */
 	FloatQueue tote;
 
 	/* Movie queue group for this model */
 	HomoQueueGroup movieQueueGroup;
 
 	/* Store the real model time in a queue for reconstructing time series later. */
 	volatile FloatQueue modelTimeQueue;
 
 	String reminderMessage;
 
 	private static byte jobIndex;
 	private volatile boolean stopAtNextRecordingStep;
 	private long systemTimeOfLastStepEnd;
 	private static short minimumJobCycleTime = 50;
 	private long systemTimeElapsed;
 	private double lastCheckedTot, lastCheckedKin;
 	private ActionReminder actionReminder;
 	private boolean resetTapeDialogEnabled = true;
 	private boolean recorderDeactivated;
 	private UndoManager undoManager;
 	private boolean reminderEnabled;
 	private boolean exclusiveSelection = true;
 	private InputJob inputJob;
 
 	/* the dynamic task pool installed in this model */
 	Job job;
 
 	/* heat bath (off by default) */
 	HeatBath heatBath;
 
 	/* the subtask of automatically popup a reminder at a given frequency. */
 	final Loadable reminder = new AbstractLoadable(5000) {
 		public void execute() {
 			if (job.getIndexOfStep() == 0) {
 				reminder.setCompleted(false);
 			}
 			else if (job.getIndexOfStep() >= reminder.getLifetime()) {
 				reminder.setCompleted(true);
 			}
 			if (modelTime > 0) {
 				stopImmediately();
 				if (movie != null)
 					movie.enableMovieActions(true);
 				if (reminderMessage != null && !reminderMessage.trim().equals("")) {
 					EventQueue.invokeLater(new Runnable() {
 						public void run() {
 							String s = MDView.getInternationalText("CloseButton");
 							if (s != null)
 								REMINDER_OPTIONS[0] = s;
 							s = MDView.getInternationalText("Snapshot");
 							if (s != null)
 								REMINDER_OPTIONS[1] = s;
 							s = MDView.getInternationalText("Continue");
 							if (s != null)
 								REMINDER_OPTIONS[2] = s;
 							s = MDView.getInternationalText("AutomaticReminder");
 							int i = JOptionPane.showOptionDialog(JOptionPane.getFrameForComponent(getView()),
 									reminderMessage, s != null ? s : "Automatical Reminder",
 									JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
 									REMINDER_OPTIONS, REMINDER_OPTIONS[0]);
 							if (i == JOptionPane.NO_OPTION) {
 								notifyPageComponentListeners(new PageComponentEvent(getView(),
 										PageComponentEvent.SNAPSHOT_TAKEN));
 							}
 							else if (i == JOptionPane.CANCEL_OPTION) {
 								play.actionPerformed(null);
 							}
 						}
 					});
 				}
 			}
 		}
 
 		public int getPriority() {
 			return Thread.NORM_PRIORITY - 1;
 		}
 
 		public String getName() {
 			return "Automatic reminder";
 		}
 
 		public String getDescription() {
 			return "This task automatically pauses the simulation and invokes a reminder\n at a given frquency.";
 		}
 	};
 
 	/* the subtask of updating the movie queues */
 	Loadable movieUpdater = new AbstractLoadable(100) {
 		public void execute() {
 			if (isEmpty()) {
 				stopImmediately();
 				stopAtNextRecordingStep = false;
 				return;
 			}
 			record();
 			movie.setCurrentFrameIndex(getTapePointer());
 			if (stopAtNextRecordingStep) {
 				stopImmediately();
 				stopAtNextRecordingStep = false;
 			}
 		}
 
 		public String getName() {
 			return "Recording the simulation";
 		}
 
 		public int getLifetime() {
 			return ETERNAL;
 		}
 
 		public int getPriority() {
 			return Thread.NORM_PRIORITY - 1;
 		}
 
 		public String getDescription() {
 			return "This task records the simulation.";
 		}
 	};
 
 	/* the subtask of safe-guarding the simulation */
 	private Loadable watchdog = new AbstractLoadable(200) {
 		public void execute() {
 			// check time overflow
 			if (modelTime > Float.MAX_VALUE - 10000.0f) {
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getView()),
 								"This model has been run for " + modelTime
 										+ " fs.\nTo avoid overflow, it must be reset to zero.", "Time overflow",
 								JOptionPane.WARNING_MESSAGE);
 					}
 				});
 				setModelTime(0.0f);
 			}
 			/*
 			 * check divergence. If tot energy increases 10.0 eV over a watchdog's period, divergence is considered to
 			 * have occurred.
 			 */
 			if (!((MDView) getView()).errorReminderSuppressed()) {
 				if (!heatBathActivated()) {
 					if (lastCheckedTot != 0 && tot - lastCheckedTot > 10.0) {
 						EventQueue.invokeLater(new Runnable() {
 							public void run() {
 								JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getView()),
 										"The total energy increases too fast. The\n"
 												+ "numerical simulation may have diverged.\n"
 												+ "Please check the model to see:\n\n"
 												+ "1. If the model is overheated.\n"
 												+ "2. If the time step is too big.", "Divergence warning",
 										JOptionPane.WARNING_MESSAGE);
 							}
 						});
 						stopImmediately();
 					}
 					lastCheckedTot = tot;
 				}
 				else {
 					if (lastCheckedKin != 0 && kin - lastCheckedKin > 100.0) {
 						EventQueue.invokeLater(new Runnable() {
 							public void run() {
 								JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getView()),
 										"The kinetic energy increases too fast. The\n"
 												+ "numerical simulation may have diverged.\n"
 												+ "Please check the model to see:\n\n"
 												+ "1. If the model is overheated.\n"
 												+ "2. If the time step is too big.", "Divergence warning",
 										JOptionPane.WARNING_MESSAGE);
 							}
 						});
 						stopImmediately();
 					}
 					lastCheckedKin = kin;
 				}
 			}
 		}
 
 		public int getPriority() {
 			return Thread.MIN_PRIORITY;
 		}
 
 		public String getName() {
 			return "Watch Dog";
 		}
 
 		public int getLifetime() {
 			return ETERNAL;
 		}
 
 		public String getDescription() {
 			return "This task monitors the simulation. It checks for a set of properties registered\nwith it. If anything goes wrong, it will give a report and pause the simulation.";
 		}
 	};
 
 	/* the subtask of painting the view at a given frequency */
 	private Loadable paintView = new AbstractLoadable(50) {
 		public void execute() {
 			if (isEmpty())
 				return;
 			systemTimeElapsed = System.currentTimeMillis() - systemTimeOfLastStepEnd;
 			if (systemTimeElapsed < minimumJobCycleTime) {
 				try {
 					Thread.sleep(minimumJobCycleTime - systemTimeElapsed);
 				}
 				catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				// System.out.println(systemTimeElapsed-minimumJobCycleTime);
 			}
 			systemTimeOfLastStepEnd = System.currentTimeMillis();
 			((MDView) getView()).showNextFrameOfImages();
 			if (getView() instanceof AtomisticView) {
 				AtomisticView view = (AtomisticView) getView();
 				if (view.getUseJmol() && !view.isVoronoiStyle()) {
 					view.refreshJmol(); // this method calls repaint()
 				}
 				else {
 					view.repaint();
 				}
 			}
 			else {
 				getView().repaint();
 			}
 			if (movie.getMovieSlider().isShowing()) {
 				movie.getMovieSlider().repaint();
 			}
 			notifyUpdateListeners(new UpdateEvent(MDModel.this, UpdateEvent.VIEW_UPDATED));
 		}
 
 		public int getPriority() {
 			return Thread.MIN_PRIORITY;
 		}
 
 		public String getName() {
 			return "Painting view";
 		}
 
 		public int getLifetime() {
 			return ETERNAL;
 		}
 
 		public String getDescription() {
 			return "This task updates the view to create continuous animation for the simulation.\nYou can decrease the interval parameter to smoothen the animation, or\nincrease to speed up the overall simulation.";
 		}
 	};
 
 	private ModelReader modelReader;
 	private ModelWriter modelWriter;
 
 	private Action play, stop, revert, reload, scriptAction, importModel, snapshot, snapshot2, heat, cool;
 	private Action accessProp, removeLastParticle;
 	private Action toggleAField, toggleGField, toggleEField, toggleBField, eFieldDirection, bFieldDirection;
 
 	private AbstractChange scriptChanger, temperatureChanger;
 	private AbstractChange aFieldChanger, gFieldChanger, eFieldChanger, bFieldChanger;
 
 	public MDModel() {
 
 		universe = new Universe();
 		fields = new Vector<VectorField>(3);
 		obstacles = new ObstacleCollection(this);
 		boundary = new RectangularBoundary(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, this);
 		boundary.setView(boundary);
 
 		play = new RunAction(this);
 		stop = new StopAction(this);
 		revert = new RevertAction(this);
 		reload = new ReloadAction(this);
 		scriptAction = new ScriptAction(this);
 		importModel = new ImportAction(this);
 		snapshot = new SnapshotAction(this, true);
 		snapshot2 = new SnapshotAction(this, false);
 		heat = new HeatAction(this, true);
 		cool = new HeatAction(this, false);
 		accessProp = new AccessPropAction(this);
 		removeLastParticle = new RemoveLastParticleAction(this);
 		toggleAField = new ToggleFieldAction(this, ToggleFieldAction.A_FIELD);
 		toggleGField = new ToggleFieldAction(this, ToggleFieldAction.G_FIELD);
 		toggleEField = new ToggleFieldAction(this, ToggleFieldAction.E_FIELD);
 		toggleBField = new ToggleFieldAction(this, ToggleFieldAction.B_FIELD);
 		eFieldDirection = new FieldDirectionAction(this, ToggleFieldAction.E_FIELD);
 		bFieldDirection = new FieldDirectionAction(this, ToggleFieldAction.B_FIELD);
 
 		scriptChanger = new ScriptChanger(this);
 		temperatureChanger = new TemperatureChanger(this);
 		aFieldChanger = new FieldChanger(this, ToggleFieldAction.A_FIELD);
 		gFieldChanger = new FieldChanger(this, ToggleFieldAction.G_FIELD);
 		eFieldChanger = new FieldChanger(this, ToggleFieldAction.E_FIELD);
 		bFieldChanger = new FieldChanger(this, ToggleFieldAction.B_FIELD);
 
 		actionReminder = new ActionReminder();
 
 		movieQueueGroup = new HomoQueueGroup("Movie");
 
 		computeList = new ArrayList<String>();
 		properties = new HashMap<Object, Object>();
 		undoManager = new UndoManager();
 		undoManager.setLimit(1);
 
 		movie = new EmbeddedMovie();
 		movie.setRunAction(play);
 		movie.setStopAction(stop);
 
 		modelReader = new ModelReader(ModelerUtilities.fileChooser, "Open", this);
 		modelWriter = new ModelWriter(ModelerUtilities.fileChooser, "Save As", this);
 
 		actionMap = Collections.synchronizedMap(new TreeMap<String, Action>());
 		actionMap.put((String) heat.getValue(Action.SHORT_DESCRIPTION), heat);
 		actionMap.put((String) cool.getValue(Action.SHORT_DESCRIPTION), cool);
 		actionMap.put((String) reload.getValue(Action.SHORT_DESCRIPTION), reload);
 		actionMap.put((String) removeLastParticle.getValue(Action.SHORT_DESCRIPTION), removeLastParticle);
 		actionMap.put((String) snapshot.getValue(Action.SHORT_DESCRIPTION), snapshot);
 		actionMap.put((String) snapshot2.getValue(Action.SHORT_DESCRIPTION), snapshot2);
 		actionMap.put((String) scriptAction.getValue(Action.SHORT_DESCRIPTION), scriptAction);
 		actionMap.put((String) movie.stepBackMovie.getValue(Action.SHORT_DESCRIPTION), movie.stepBackMovie);
 		actionMap.put((String) movie.stepForwardMovie.getValue(Action.SHORT_DESCRIPTION), movie.stepForwardMovie);
 
 		changeMap = Collections.synchronizedMap(new TreeMap<String, ChangeListener>());
 		changeMap.put(scriptChanger.toString(), scriptChanger);
 		changeMap.put(temperatureChanger.toString(), temperatureChanger);
 
 		switchMap = Collections.synchronizedMap(new TreeMap<String, Action>());
 		switchMap.put((String) scriptAction.getValue(Action.SHORT_DESCRIPTION), scriptAction);
 		switchMap.put(toggleGField.toString(), toggleGField);
 		switchMap.put(toggleEField.toString(), toggleEField);
 		switchMap.put(toggleBField.toString(), toggleBField);
 		switchMap.put(toggleAField.toString(), toggleAField);
 
 		choiceMap = Collections.synchronizedMap(new TreeMap<String, Action>());
 		choiceMap.put((String) scriptAction.getValue(Action.SHORT_DESCRIPTION), scriptAction);
 		choiceMap.put((String) importModel.getValue(Action.SHORT_DESCRIPTION), importModel);
 		if (getNonLocalField(ElectricField.class.getName()) != null)
 			choiceMap.put(eFieldDirection.toString(), eFieldDirection);
 		if (getNonLocalField(MagneticField.class.getName()) != null)
 			choiceMap.put(bFieldDirection.toString(), bFieldDirection);
 
 		multiSwitchMap = Collections.synchronizedMap(new TreeMap<String, Action>());
 		multiSwitchMap.put((String) scriptAction.getValue(Action.SHORT_DESCRIPTION), scriptAction);
 
 		Arrays.fill(channels, 0);
 
 	}
 
 	public void destroy() {
 
 		actionMap.clear();
 		changeMap.clear();
 		switchMap.clear();
 		choiceMap.clear();
 		multiSwitchMap.clear();
 		if (job != null) {
 			job.clear();
 			job = null;
 		}
 
 		if (timeSeriesRepository != null) {
 			timeSeriesRepository.clear();
 			timeSeriesRepository.dispose();
 		}
 
 		if (obstacles != null && !obstacles.isEmpty()) {
 			RectangularObstacle obs = null;
 			synchronized (obstacles) {
 				for (Iterator it = obstacles.iterator(); it.hasNext();) {
 					obs = (RectangularObstacle) it.next();
 					obs.initializeMovieQ(-1);
 					obs.setModel(null);
 					it.remove();
 				}
 			}
 		}
 
 		((MDView) getView()).destroy();
 		if (boundary.getQueue() != null)
 			boundary.getQueue().setLength(-1);
 		if (modelProp != null) {
 			modelProp.destroy();
 			modelProp = null;
 		}
 		if (stateHolder != null) {
 			stateHolder.destroy();
 			stateHolder = null;
 		}
 		clearScriptQueue();
 		haltScriptExecution();
 		if (evalAction != null) {
 			evalAction.removeAllScriptListeners();
 			if (evalThread != null)
 				evalThread.interrupt();
 			evalAction = null;
 		}
 		if (evalTask != null) {
 			evalTask.removeAllScriptListeners();
 			evalTask = null;
 		}
 		actionReminder = null;
 		movieUpdater = null;
 		watchdog = null;
 		paintView = null;
 		play = null;
 		stop = null;
 		heat = null;
 		cool = null;
 		reload = null;
 		revert = null;
 		snapshot = null;
 		snapshot2 = null;
 		importModel = null;
 		accessProp = null;
 		aFieldChanger = null;
 		gFieldChanger = null;
 		eFieldChanger = null;
 		bFieldChanger = null;
 		temperatureChanger = null;
 		scriptChanger = null;
 		removeLastParticle = null;
 		toggleAField = null;
 		toggleGField = null;
 		toggleBField = null;
 		toggleEField = null;
 		eFieldDirection = null;
 		bFieldDirection = null;
 
 	}
 
 	private void handleFailure(String msg) {
 		blockView(false);
 		if (monitor != null) {
 			monitor.setProgressMessage(msg);
 			monitor.resetProgressBar();
 		}
 	}
 
 	public void stopInput() {
 		if (inputJob != null)
 			inputJob.stopJob();
 	}
 
 	public void input(File file) {
 		if (file == null)
 			throw new IllegalArgumentException("null file");
 		stopInput();
 		inputJob = new InputJob();
 		inputJob.read(file);
 	}
 
 	public void input(URL url) {
 		if (url == null)
 			throw new IllegalArgumentException("null url");
 		stopInput();
 		inputJob = new InputJob();
 		if (url.toString().toLowerCase().startsWith("file:/")) {
 			inputJob.read(ModelerUtilities.convertURLToFile(url.toString()));
 		}
 		else {
 			inputJob.read(url);
 		}
 	}
 
 	public void input(URL baseURL, String relativeURL) {
 		if (baseURL == null)
 			throw new IllegalArgumentException("null base url");
 		if (relativeURL == null)
 			throw new IllegalArgumentException("null relative url");
 		URL url = null;
 		try {
 			url = new URL(baseURL, relativeURL);
 		}
 		catch (MalformedURLException e) {
 			e.printStackTrace();
 			return;
 		}
 		input(url);
 	}
 
 	public void output(File file) {
 		clearScriptQueue();
 		haltScriptExecution();
 		stopImmediately();
 		blockView(true);
 		if (file == null)
 			throw new IllegalArgumentException("null file");
 		if (monitor == null)
 			createProgressMonitor();
 		else monitor.resetProgressBar();
 		monitor.setProgressMessage("Opening " + file.getName() + "...");
 		OutputStream os = null;
 		try {
 			os = new BufferedOutputStream(new FileOutputStream(file));
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 			final String name = file.toString();
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getView()), "Error in writing to "
 							+ name, "Write Error", JOptionPane.ERROR_MESSAGE);
 				}
 			});
 			return;
 		}
 		XMLEncoder out = new XMLEncoder(os);
 		try {
 			synchronized (((MDView) getView()).getUpdateLock()) {
 				encode(out);
 			}
 			saveImages(file.getParentFile());
 			putProperties(file);
 			notifyModelListeners(new ModelEvent(this, ModelEvent.MODEL_OUTPUT));
 			blockView(false);
 			setProgress(0, "Done");
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			setProgress(0, "Error");
 			final String name = file.toString();
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getView()), "Encoding error: "
 							+ name, "Write Error", JOptionPane.ERROR_MESSAGE);
 				}
 			});
 		}
 		finally {
 			out.close();
 			try {
 				os.close();
 			}
 			catch (IOException iox) {
 			}
 		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				play.setEnabled(true);
 				stop.setEnabled(false);
 			}
 		});
 	}
 
 	// For use with scripting of applet
 	public void output(String fileName) {
 		File file = new File(fileName);
 		output(file);
 	}
 
 	abstract void encode(XMLEncoder out) throws Exception;
 
 	void decode(XMLDecoder in) throws Exception {
 	}
 
 	/** stores the result in the i-th channel */
 	public void setChannel(int i, double result) {
 		if (i < 0 || i >= channels.length)
 			throw new IllegalArgumentException("Channel " + i + " does not exist.");
 		channels[i] = result;
 	}
 
 	/** returns the result stored in the i-th channel */
 	public double getChannel(int i) {
 		if (i < 0 || i >= channels.length)
 			throw new IllegalArgumentException("Channel " + i + " does not exist.");
 		return channels[i];
 	}
 
 	/** notify the environment this model is embedded that its state has changed. */
 	public void notifyChange() {
 		if (modelChangeEvent == null)
 			modelChangeEvent = new PageComponentEvent(this, PageComponentEvent.COMPONENT_CHANGED);
 		notifyPageComponentListeners(modelChangeEvent);
 	}
 
 	public void setExclusiveSelection(boolean b) {
 		exclusiveSelection = b;
 	}
 
 	public boolean getExclusiveSelection() {
 		return exclusiveSelection;
 	}
 
 	// methods that support scripting
 
 	/** select a set of particles according to the instruction BitSet. */
 	public void setSelectionSet(BitSet set) {
 		if (set == null) {
 			for (int i = 0; i < getNumberOfParticles(); i++)
 				getParticle(i).setSelected(false);
 			getView().repaint();
 			return;
 		}
 		setExclusiveSelection(false);
 		for (int i = 0; i < getNumberOfParticles(); i++)
 			getParticle(i).setSelected(set.get(i));
 		getView().repaint();
 	}
 
 	/** return the selected set of particles in BitSet. */
 	public BitSet getSelectionSet() {
 		int n = getNumberOfParticles();
 		BitSet bs = new BitSet(n);
 		for (int i = 0; i < n; i++) {
 			if (getParticle(i).isSelected())
 				bs.set(i);
 		}
 		return bs;
 	}
 
 	/** return the lowest index of the selected particles. */
 	public int getIndexOfSelectedParticle() {
 		int n = getNumberOfParticles();
 		for (int i = 0; i < n; i++) {
 			if (getParticle(i).isSelected())
 				return i;
 		}
 		return -1;
 	}
 
 	public void markSelection(Color markColor) {
 		if (markColor != null)
 			((MDView) getView()).setMarkColor(markColor);
 		markSelection();
 	}
 
 	public void markSelection() {
 		int n = getNumberOfParticles();
 		for (int i = 0; i < n; i++) {
 			Particle p = getParticle(i);
 			p.setMarked(p.isSelected());
 		}
 	}
 
 	public boolean isActionScriptRunning() {
 		if (evalAction == null)
 			return false;
 		return !evalAction.isStopped();
 	}
 
 	/** stop the action script execution thread. */
 	public void haltScriptExecution() {
 		if (evalAction != null)
 			evalAction.halt();
 	}
 
 	private void clearScriptQueue() {
 		if (evalAction != null)
 			evalAction.clearScriptQueue();
 		if (evalTask != null)
 			evalTask.clearScriptQueue();
 	}
 
 	public void setInitializationScriptToRun(boolean b) {
 		initializationScriptToRun = b;
 	}
 
 	public void setExternalScriptCallback(ScriptCallback c) {
 		externalScriptCallback = c;
 	}
 
 	private void createTaskEvaluator() {
 		if (evalTask == null) {
 			evalTask = new Eval2D(this, true);
 			evalTask.setExternalScriptCallback(externalScriptCallback);
 		}
 		initEvalAction();
 		evalTask.setDefinition(evalAction.getDefinition()); // share the same definitions with the action scripts
 	}
 
 	private void runTaskScript(String script) {
 		createTaskEvaluator();
 		evalTask.appendScript(script);
 		try {
 			evalTask.evaluate2();
 		}
 		catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		getView().repaint();
 	}
 
 	public void runMouseScript(int eventType, int x, int y) {
 		initEvalAction();
 		String s = evalAction.getMouseScript(eventType);
 		if (s == null)
 			return;
 		evalAction.setNotifySaver(false);
 		evalAction.setMouseLocation(x, y);
 		runScript2(s);
 	}
 
 	public void clearMouseScripts() {
 		if (evalAction != null)
 			evalAction.clearMouseScripts();
 	}
 
 	public String runScript(String script) {
 		initEvalAction();
 		evalAction.setNotifySaver(!initializationScriptToRun);
 		return runScript2(script);
 	}
 
 	private void initEvalAction() {
 		if (evalAction == null) {
 			evalAction = new Eval2D(this, false);
 			evalAction.setExternalScriptCallback(externalScriptCallback);
 		}
 	}
 
 	private String runScript2(String script) {
 		evalAction.appendScript(script);
 		if (!evalAction.isStopped())
 			return null;
 		if (evalThread == null) {
 			evalThread = new Thread("Script Runner") {
 				public void run() {
 					try {
 						evalAction.evaluate();
 					}
 					catch (InterruptedException e) {
 						// System.out.println("script interrupted.");
 					}
 				}
 			};
 			evalThread.setPriority(Thread.MIN_PRIORITY);
 			evalThread.setUncaughtExceptionHandler(new DisasterHandler(DisasterHandler.SCRIPT_ERROR, new Runnable() {
 				public void run() {
 					evalThread = null;
 					evalAction.clearScriptQueue();
 					evalAction.halt();
 				}
 			}, null, getView()));
 			evalThread.start();
 		}
 		else {
 			// just in case there is a missed notification problem
 			try {
 				Thread.sleep(100);
 			}
 			catch (InterruptedException e) {
 			}
 			synchronized (evalAction) {
 				evalAction.notifyAll();
 			}
 		}
 		getView().repaint();
 		return null;
 	}
 
 	public void addScriptListener(ScriptListener listener) {
 		initEvalAction();
 		evalAction.addScriptListener(listener);
 		createTaskEvaluator();
 		evalTask.addScriptListener(listener);
 	}
 
 	public void removeScriptListener(ScriptListener listener) {
 		if (evalAction != null)
 			evalAction.removeScriptListener(listener);
 		if (evalTask != null)
 			evalTask.removeScriptListener(listener);
 	}
 
 	/**
 	 * set the minimum time of a molecular dynamics cycle (in milliseconds). If the calculation finishes earlier, then
 	 * the thread executing the MD task should sleep until this time is up.
 	 */
 	public void setJobCycleInMillis(short i) {
 		minimumJobCycleTime = i;
 	}
 
 	/** get the minimum time of a molecular dynamics cycle (in milliseconds) */
 	public int getJobCycleInMillis() {
 		return minimumJobCycleTime;
 	}
 
 	public abstract void setUpdateList(boolean b);
 
 	public abstract double getKin();
 
 	public abstract double getKinForParticles(List list);
 
 	public abstract float getTotalKineticEnergy();
 
 	public abstract float getTotalElectrostaticEnergy();
 
 	public abstract float getTotalElectricFieldEnergy();
 
 	public abstract float getTotalGravitationalFieldEnergy();
 
 	public abstract float getTotalRestraintEnergy();
 
 	/**
 	 * ask if this model has an embedded movie, or has loaded one from external source. A model is said to have an
 	 * embedded movie if all of its particles have arrays to store their zero and first order variables of motion and
 	 * all these arrays have the same length. These conditions are the basic requirements that constitute a file with
 	 * multiple playable frames, which we call a movie. If any of these conditions is violated, return false.
 	 */
 	public abstract boolean hasEmbeddedMovie();
 
 	/**
 	 * by default, a model comes with an embedded movie, but there are times that a movie is not needed, or not
 	 * applicable. In these circumstances, the movie mechanism can be deactivated.
 	 */
 	public abstract void activateEmbeddedMovie(boolean b);
 
 	/**
 	 * translate all the components of the model by the specified displacements.
 	 * 
 	 * @return true if this translation is permitted
 	 */
 	public abstract boolean translateWholeModel(double dx, double dy);
 
 	/**
 	 * rotate all the particles of the model by the specified degrees.
 	 * 
 	 * @return true if this rotation is permitted
 	 */
 	public boolean rotateWholeModel(double angleInDegrees) {
 		BitSet bs = getSelectionSet();
 		setExclusiveSelection(false);
 		int n = getNumberOfParticles();
 		for (int i = 0; i < n; i++)
 			getParticle(i).setSelected(true);
 		boolean b = rotateSelectedParticles(angleInDegrees);
 		setSelectionSet(bs);
 		return b;
 	}
 
 	/**
 	 * rotate all the selected particles by the specified degrees.
 	 * 
 	 * @return true if this rotation is permitted
 	 */
 	abstract boolean rotateSelectedParticles(double angleInDegrees);
 
 	/** return the bounds of objects */
 	public abstract float[] getBoundsOfObjects();
 
 	/** return the minimum x coordinate of all objects */
 	public float getMinX() {
 		return range_xmin;
 	}
 
 	/** return the maximum x coordinate of all objects */
 	public float getMaxX() {
 		return range_xmax;
 	}
 
 	/** return the minimum y coordinate of all objects */
 	public float getMinY() {
 		return range_ymin;
 	}
 
 	/** return the maximum y coordinate of all objects */
 	public float getMaxY() {
 		return range_ymax;
 	}
 
 	/** return the ID of the particle which has the minimum x coordinate */
 	public int getIDMinX() {
 		return id_xmin;
 	}
 
 	/** return the ID of the particle which has the maximum x coordinate */
 	public int getIDMaxX() {
 		return id_xmax;
 	}
 
 	/** return the ID of the particle which has the minimum y coordinate */
 	public int getIDMinY() {
 		return id_ymin;
 	}
 
 	/** return the ID of the particle which has the maximum y coordinate */
 	public int getIDMaxY() {
 		return id_ymax;
 	}
 
 	public abstract void assignTemperature(double temperature);
 
 	/** set the temperature of this model */
 	public abstract void setTemperature(double temperature);
 
 	/** @return the temperature of the entire model */
 	public abstract double getTemperature();
 
 	/** @return the temperature of the specified type of atoms inside the specified shape */
 	public abstract double getTemperature(byte type, Shape shape);
 
 	/** @return the total kinetic energy of the specified type of atoms inside the specified shape */
 	public abstract double getKineticEnergy(byte type, Shape shape);
 
 	/** return the number of particles contained in this model */
 	public abstract int getNumberOfParticles();
 
 	/** @return the number of particles of the specified type inside the specified shape */
 	public abstract int getParticleCount(byte type, Shape shape);
 
 	/** @return the number of particles inside a circle of the specified radius around the specified particle */
 	public int getParticleCountWithin(int index, float radius) {
 		int max = getNumberOfParticles();
 		if (index < 0 || index == max)
 			return -1;
 		int n = 0;
 		Particle a = getParticle(index);
 		double dx, dy;
 		for (int i = 0; i < max; i++) {
 			if (index == i)
 				continue;
 			dx = getParticle(i).rx - a.rx;
 			dy = getParticle(i).ry - a.ry;
 			if (dx * dx + dy * dy <= radius * radius)
 				n++;
 		}
 		return n;
 	}
 
 	/**
 	 * @return the average speed of the particles of the specified type inside the specified shape in the specified
 	 *         direction.
 	 */
 	public abstract double getAverageSpeed(String direction, byte type, Shape shape);
 
 	/** return the particle at the <i>i</i>-th position of the particle array */
 	public abstract Particle getParticle(int i);
 
 	abstract void showMovieFrame(int frame);
 
 	public void setPlanckConstant(float pc) {
 		PLANCK_CONSTANT = pc;
 	}
 
 	public float getPlanckConstant() {
 		return PLANCK_CONSTANT;
 	}
 
 	public void addCompute(String s) {
 		computeList.add(s);
 	}
 
 	public boolean isComputed(String s) {
 		if (computeList == null)
 			return false;
 		return computeList.contains(s);
 	}
 
 	public void removeCompute(String s) {
 		if (computeList == null)
 			return;
 		computeList.remove(s);
 	}
 
 	/** return true if this model contains no mobile object at all. */
 	public synchronized boolean isEmpty() {
 		return getNumberOfParticles() <= 0 && (obstacles == null || obstacles.isEmpty());
 	}
 
 	public void enableResetTapeDialog(boolean b) {
 		resetTapeDialogEnabled = b;
 	}
 
 	/* evolve this model one step forward */
 	abstract void advance(int time);
 
 	public void setTimeStepAndAdjustReminder(double d) {
 		if (reminderEnabled)
 			reminder.setInterval((int) (getTimeStep() * reminder.getInterval() / d));
 		setTimeStep(d);
 	}
 
 	/** set the time step for numerical integration */
 	public void setTimeStep(double d) {
 		if (d <= 0)
 			throw new IllegalArgumentException("time step cannot be negative or zero");
 	}
 
 	/** get the time step for numerical integration */
 	public abstract double getTimeStep();
 
 	/** change the temperature by percentage */
 	public abstract void changeTemperature(double percent);
 
 	public abstract void setNumberOfParticles(int i);
 
 	void record() {
 		if (modelTimeQueue.getPointer() > 0) {
 			if (modelListenerList != null && !modelListenerList.isEmpty()) {
 				ModelEvent me = new ModelEvent(this, ModelEvent.MODEL_CHANGED);
 				synchronized (modelListenerList) {
 					for (ModelListener ml : modelListenerList) {
 						ml.modelUpdate(me);
 					}
 				}
 			}
 		}
 		modelTimeQueue.update(modelTime);
 		getKin();
 		if (modelTime < ZERO)
 			pot = computeForce(0);
 		tot = kin + pot;
 		kine.update((float) kin);
 		pote.update((float) pot);
 		tote.update((float) tot);
 		for (int i = 0; i < channels.length; i++) {
 			channelTs[i].update((float) channels[i]);
 		}
 	}
 
 	abstract double computeForce(int step);
 
 	public UndoManager getUndoManager() {
 		return undoManager;
 	}
 
 	public void addUpdateListener(UpdateListener ul) {
 		if (updateListenerList == null) {
 			updateListenerList = Collections.synchronizedList(new ArrayList<UpdateListener>());
 		}
 		else {
 			if (updateListenerList.contains(ul))
 				return;
 		}
 		updateListenerList.add(ul);
 	}
 
 	public void removeUpdateListener(UpdateListener ul) {
 		if (updateListenerList != null)
 			updateListenerList.remove(ul);
 	}
 
 	void notifyUpdateListeners(UpdateEvent e) {
 		if (updateListenerList == null || updateListenerList.isEmpty())
 			return;
 		synchronized (updateListenerList) {
 			for (UpdateListener l : updateListenerList) {
 				switch (e.getType()) {
 				case UpdateEvent.VIEW_UPDATED:
 					l.viewUpdated(e);
 					break;
 				}
 			}
 		}
 	}
 
 	/** return a List copy of the ModelListeners */
 	public List<ModelListener> getModelListeners() {
 		return modelListenerList;
 	}
 
 	public void addModelListener(ModelListener ml) {
 		if (ml == null)
 			throw new IllegalArgumentException("null input");
 		if (modelListenerList == null) {
 			modelListenerList = Collections.synchronizedList(new ArrayList<ModelListener>());
 		}
 		else {
 			if (modelListenerList.contains(ml))
 				return;
 		}
 		modelListenerList.add(ml);
 	}
 
 	public void removeModelListener(ModelListener ml) {
 		if (ml == null)
 			throw new IllegalArgumentException("null input");
 		if (modelListenerList == null)
 			return;
 		modelListenerList.remove(ml);
 	}
 
 	public void notifyModelListeners(ModelEvent e) {
 		if (modelListenerList == null || modelListenerList.isEmpty())
 			return;
 		synchronized (modelListenerList) {
 			for (ModelListener ml : modelListenerList)
 				ml.modelUpdate(e);
 		}
 	}
 
 	public List<PageComponentListener> getPageComponentListeners() {
 		return pageComponentListenerList;
 	}
 
 	public void addPageComponentListener(PageComponentListener pcl) {
 		if (pcl == null)
 			throw new IllegalArgumentException("null input");
 		if (pageComponentListenerList == null) {
 			pageComponentListenerList = new ArrayList<PageComponentListener>();
 		}
 		else {
 			if (pageComponentListenerList.contains(pcl))
 				return;
 		}
 		pageComponentListenerList.add(pcl);
 	}
 
 	public void removePageComponentListener(PageComponentListener pcl) {
 		if (pcl == null)
 			throw new IllegalArgumentException("null input");
 		if (pageComponentListenerList == null)
 			return;
 		pageComponentListenerList.remove(pcl);
 	}
 
 	public void notifyPageComponentListeners(PageComponentEvent e) {
 		if (pageComponentListenerList == null || pageComponentListenerList.isEmpty())
 			return;
 		for (PageComponentListener l : pageComponentListenerList)
 			l.pageComponentChanged(e);
 	}
 
 	public boolean hasGraphs() {
 		if (modelListenerList == null || modelListenerList.isEmpty())
 			return false;
 		synchronized (modelListenerList) {
 			for (ModelListener ml : modelListenerList) {
 				if ((ml instanceof PageXYGraph) || (ml instanceof PageBarGraph))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	void createProgressMonitor() {
 		boolean b = ioProgressBar == null;
 		monitor = new ProcessMonitor(JOptionPane.getFrameForComponent(getView()), b);
 		if (b) {
 			monitor.setLocationRelativeTo(getView());
 		}
 		else {
 			monitor.setProgressBar(ioProgressBar);
 		}
 	}
 
 	public void setDefaultTapeLength(int i) {
 		defaultTapeLength = i;
 	}
 
 	public int getDefaultTapeLength() {
 		return defaultTapeLength;
 	}
 
 	public synchronized float getModelTime() {
 		return modelTime;
 	}
 
 	public synchronized void setModelTime(float f) {
 		modelTime = f;
 		if (f < ZERO) {
 			if (job != null)
 				job.setIndexOfStep(0);
 		}
 	}
 
 	public Map<String, Action> getActions() {
 		return actionMap;
 	}
 
 	public Map<String, ChangeListener> getChanges() {
 		return changeMap;
 	}
 
 	public Map<String, Action> getSwitches() {
 		return switchMap;
 	}
 
 	public Map<String, Action> getMultiSwitches() {
 		return multiSwitchMap;
 	}
 
 	public Map<String, Action> getChoices() {
 		Set set = choiceMap.keySet();
 		if (getNonLocalField(ElectricField.class.getName()) != null) {
 			if (!set.contains(eFieldDirection.toString())) {
 				choiceMap.put(eFieldDirection.toString(), eFieldDirection);
 			}
 		}
 		else {
 			if (set.contains(eFieldDirection.toString())) {
 				choiceMap.remove(eFieldDirection.toString());
 			}
 		}
 		if (getNonLocalField(MagneticField.class.getName()) != null) {
 			if (!set.contains(bFieldDirection.toString())) {
 				choiceMap.put(bFieldDirection.toString(), bFieldDirection);
 			}
 		}
 		else {
 			if (set.contains(bFieldDirection.toString())) {
 				choiceMap.remove(bFieldDirection.toString());
 			}
 		}
 		return choiceMap;
 	}
 
 	/** search for a queue with a specified name */
 	public DataQueue getQueue(String name) {
 		DataQueue q = null;
 		for (Iterator it = movieQueueGroup.iterator(); it.hasNext();) {
 			q = (DataQueue) it.next();
 			if (q.getName().equals(name))
 				return q;
 		}
 		return null;
 	}
 
 	public Object getProperty(Object key) {
 		return properties.get(key);
 	}
 
 	public Object removeProperty(Object key) {
 		return properties.remove(key);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void putProperty(Object key, Object value) {
 		if (key instanceof String) {
 			String s = ((String) key).toLowerCase();
 			if (s.equals("student") || s.equals("teacher")) {
 				if (properties.get(key) == null) {
 					List<Object> a = new ArrayList<Object>();
 					a.add(value);
 					properties.put(key, a);
 				}
 				else {
 					try {
 						List a = (List) properties.get(key);
 						a.add(value);
 					}
 					catch (ClassCastException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			else {
 				properties.put(key, value);
 			}
 		}
 		else {
 			properties.put(key, value);
 		}
 	}
 
 	public void setIOProgressBar(JProgressBar pb) {
 		ioProgressBar = pb;
 	}
 
 	public JProgressBar getIOProgressBar() {
 		return ioProgressBar;
 	}
 
 	void setProgress(final int value, final String message) {
 		if (ioProgressBar == null)
 			return;
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				ioProgressBar.setValue(value);
 				ioProgressBar.setString(message);
 			}
 		});
 	}
 
 	public void setupAutomaticReminder() {
 		AutomaticalReminderControlPanel a = new AutomaticalReminderControlPanel();
 		a.createDialog(getView(), this).setVisible(true);
 		if (reminderEnabled) {
 			int interval = (int) (a.getIntervalTime() / getTimeStep());
 			reminder.setInterval(interval);
 			reminder.setLifetime(a.isRepeatable() ? Loadable.ETERNAL : interval);
 			reminderMessage = a.getMessage();
 			boolean b = job.getIndexOfStep() > reminder.getLifetime();
 			reminder.setCompleted(b);
 			if (!b) {
 				if (!job.contains(reminder))
 					job.add(reminder);
 			}
 			else {
 				if (job.contains(reminder))
 					job.remove(reminder);
 			}
 		}
 	}
 
 	/** @see org.concord.mw2d.models.MDModel#modelTimeQueue */
 	public void setModelTimeQueue(FloatQueue q) {
 		modelTimeQueue = q;
 	}
 
 	/** @see org.concord.mw2d.models.MDModel#modelTimeQueue */
 	public FloatQueue getModelTimeQueue() {
 		return modelTimeQueue;
 	}
 
 	public void setMovieQueueGroup(HomoQueueGroup g) {
 		movieQueueGroup = g;
 	}
 
 	public HomoQueueGroup getMovieQueueGroup() {
 		return movieQueueGroup;
 	}
 
 	/**
 	 * this method is used to "get rid of" a tape when a user action that results in frame inconsistency has occurred,
 	 * and "insert a new tape".
 	 */
 	public int resetTape() {
 		if (recorderDeactivated)
 			return JOptionPane.YES_OPTION;
 		if (movie.length() <= 0)
 			return JOptionPane.YES_OPTION;
 		int i = JOptionPane.YES_OPTION;
 		if (resetTapeDialogEnabled) {
 			// boolean b=getJob().isStopped();
 			// if(!b) stop();
 			i = actionReminder.show(ActionReminder.RESET_TAPE);
 		}
 		if (i == JOptionPane.YES_OPTION || i == JOptionPane.CANCEL_OPTION)
 			insertNewTape();
 		return i;
 	}
 
 	/*
 	 * If the recorder tape is full and the user has saved the movie to the hard drive, this method should be called to
 	 * "insert a new tape" to the recorder, so as to record the next period of simulation.
 	 */
 	void insertNewTape() {
 		lastCheckedTot = 0;
 		movie.setCurrentFrameIndex(0);
 		movie.getMovieSlider().repaint();
 		if (hasEmbeddedMovie())
 			setTapePointer(0);
 	}
 
 	void initializeJob() {
 		if (job == null) {
 			String s = this instanceof MesoModel ? "2D Mesoparticle Simulator" : "2D Molecular Simulator";
 			job = new Job(s + " #" + (jobIndex++)) {
 
 				public void run() {
 					while (true) {
 						super.run();
 						while (!isStopped()) {
 							modelTime += (float) getTimeStep();
 							advance(indexOfStep++);
 							execute();
 						}
 						synchronized (this) {
 							try {
 								wait();
 							}
 							catch (InterruptedException e) {
 								// e.printStackTrace();
 								break;
 							}
 						}
 					}
 				}
 
 				public void runScript(String script) {
 					if (script != null)
 						MDModel.this.runTaskScript(script);
 				}
 
 				public void notifyChange() {
 					MDModel.this.notifyChange();
 				}
 
 			};
 		}
 		if (!recorderDeactivated && !job.contains(movieUpdater))
 			job.add(movieUpdater);
 		if (reminderEnabled && !job.contains(reminder))
 			job.add(reminder);
 		if (!job.contains(watchdog))
 			job.add(watchdog);
 		if (!job.contains(paintView))
 			job.add(paintView);
 	}
 
 	public Job getJob() {
 		return job;
 	}
 
 	void addCustomTasks(List<TaskAttributes> list) {
 		job.removeAllCustomTasks();
 		if (list == null || list.isEmpty())
 			return;
 		for (TaskAttributes a : list) {
 			Loadable l = new AbstractLoadable() {
 				public void execute() {
 					job.runScript(getScript());
 					if (job.getIndexOfStep() >= getLifetime()) {
 						setCompleted(true);
 					}
 				}
 			};
 			l.setName(a.getName());
 			l.setDescription(a.getDescription());
 			l.setScript(a.getScript());
 			l.setInterval(a.getInterval());
 			l.setLifetime(a.getLifetime());
 			l.setPriority(a.getPriority());
 			l.setSystemTask(false);
 			job.add(l);
 		}
 	}
 
 	public void enableReminder(boolean b) {
 		reminderEnabled = b;
 		if (job == null)
 			return;
 		if (b) {
 			if (!job.contains(reminder))
 				job.add(reminder);
 		}
 		else {
 			job.remove(reminder);
 		}
 	}
 
 	public boolean isReminderEnabled() {
 		return reminderEnabled;
 	}
 
 	/**
 	 * Evenly add to or substract from all particles of the system the specified amount of energy. If the passed
 	 * argument is positive, the system is heated; otherwise, the system is cooled.
 	 */
 	public abstract void transferHeat(double amount);
 
 	/**
 	 * Evenly add to or substract from all particles of the specified list the specified amount of energy. If the passed
 	 * argument is positive, the selected particles in the list is heated; otherwise, the selected particles is cooled.
 	 */
 	public abstract void transferHeatToParticles(List list, double amount);
 
 	public boolean getRecorderDisabled() {
 		return recorderDeactivated;
 	}
 
 	public void setRecorderDisabled(boolean b) {
 		recorderDeactivated = b;
 	}
 
 	public int getTapeLength() {
 		if (modelTimeQueue != null)
 			return modelTimeQueue.getLength();
 		return 0;
 	}
 
 	public int getTapePointer() {
 		return modelTimeQueue.getPointer();
 	}
 
 	abstract void setTapePointer(int n);
 
 	/**
 	 * in the case an action cannot be passed thru a ModelAction filter, call this method to ask for the recorder's
 	 * permission to reset before a change takes place.
 	 */
 	public boolean changeApprovedByRecorder() {
 		if (hasEmbeddedMovie()) {
 			if (resetTape() == JOptionPane.NO_OPTION) {
 				((MDView) getView()).clearEditor(false);
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void activateHeatBath(boolean b) {
 		if (b) {
 			if (heatBath == null)
 				heatBath = new HeatBath(this);
 			if (job == null)
 				initializeJob();
 			if (job.isStopped()) {
 				if (!job.contains(heatBath))
 					job.add(heatBath);
 			}
 			else {
 				if (!job.toBeAdded(heatBath) && !job.contains(heatBath))
 					job.add(heatBath);
 			}
 		}
 		else {
 			if (heatBath != null && job != null) {
 				job.remove(heatBath);
 				heatBath.setCompleted(true);
 				heatBath = null;
 			}
 		}
 		if (job != null && job.isStopped())
 			job.processPendingRequests();
 	}
 
 	public boolean heatBathActivated() {
 		if (job == null || heatBath == null)
 			return false;
 		if (job.isStopped())
 			return job.contains(heatBath);
 		if (job.toBeAdded(heatBath))
 			return true;
 		if (job.toBeRemoved(heatBath))
 			return false;
 		return job.contains(heatBath);
 	}
 
 	public HeatBath getHeatBath() {
 		return heatBath;
 	}
 
 	public Movie getMovie() {
 		return movie;
 	}
 
 	/*
 	 * should energy be minimized before the model is run? This method is usually called inside <code>run()</code> to
 	 * remove bad contacts.
 	 */
 	abstract boolean needMinimization();
 
 	/** revert the model's state to that before running */
 	public abstract boolean revert();
 
 	public void resetWithoutAsking() {
 		reset2(false);
 	}
 
 	public void reset() {
 		reset2(true);
 	}
 
 	private void reset2(boolean ask) {
 		clearScriptQueue();
 		haltScriptExecution();
 		stopImmediately();
 		if (job != null)
 			job.removeAllCustomTasks();
 		final String url = (String) getProperty("url");
 		if (url == null) {
 			if (hasEmbeddedMovie()) {
 				if (!recorderDeactivated)
 					insertNewTape();
 			}
 			setModelTime(0);
 			((MDView) getView()).removeAllObjects();
 			return;
 		}
 		if (ask && actionReminder.show(ActionReminder.RESET_TO_SAVED_STATE) == JOptionPane.NO_OPTION)
 			return;
 		new SwingWorker("Model Resetter", Thread.NORM_PRIORITY) {
 			public Object construct() {
 				File file = null;
 				if (FileUtilities.isRemote(url)) {
 					URL u = null;
 					try {
 						u = new URL(url);
 					}
 					catch (MalformedURLException mue) {
 						mue.printStackTrace();
 						return null;
 					}
 					file = ConnectionManager.sharedInstance().shouldUpdate(u);
 					if (file == null) {
 						try {
 							file = ConnectionManager.sharedInstance().cache(u);
 						}
 						catch (FileNotFoundException e) {
 							e.printStackTrace();
 						}
 					}
 					if (file == null) {
 						input(u);
 						return url;
 					}
 				}
 				else {
 					file = new File(url);
 				}
 				input(file);
 				return url;
 			}
 
 			public void finished() {
 				play.setEnabled(true);
 				stop.setEnabled(false);
 				setProgress(0, "Done");
 				notifyModelListeners(new ModelEvent(MDModel.this, ModelEvent.MODEL_RESET));
 				notifyPageComponentListeners(new PageComponentEvent(MDModel.this, PageComponentEvent.COMPONENT_RESET));
 			}
 		}.start();
 	}
 
 	/**
 	 * run this model, meanwhile disable the model editor. If you have not set up the job, a default job will be
 	 * assigned to this model. The default job paints the view, computes the energy, and updates the data cache.
 	 */
 	public void run() {
 		if (job != null)
 			job.processPendingRequests();
 		if (this instanceof AtomicModel) {
 			if (!((AtomicModel) this).isAtomFlowEnabled() && !((AtomicModel) this).isLightSourceEnabled()) {
 				if (isEmpty()) {
 					return;
 				}
 			}
 		}
 		else {
 			if (isEmpty())
 				return;
 		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				((MDView) getView()).enableEditor(false);
 			}
 		});
 		if (this instanceof MesoModel) {
 			run2();
 		}
 		else {
 			if (needMinimization()) {
 				new SwingWorker("Energy Minimizer") {
 					public Object construct() {
 						Minimizer m = new Minimizer((MolecularModel) (MDModel.this));
 						for (int i = 0; i <= 50; i++) {
 							pot = m.sd(1.0);
 							if (i % 10 == 0)
 								getView().repaint();
 						}
 						return new Double(pot);
 					}
 
 					public void finished() {
 						if (heatBath == null)
 							setTemperature(300);
 						run2();
 					}
 				}.start();
 			}
 			else {
 				run2();
 			}
 		}
 	}
 
 	private synchronized void run2() {
 		if (isLoading.get()) {
 			return;
 		}
 		if (job != null && !job.isStopped()) {
 			return;
 		}
 		if (job == null) {
 			initializeJob();
 		}
 		else {
 			if (!recorderDeactivated && !job.contains(movieUpdater))
 				job.add(movieUpdater);
 		}
 		job.start();
 		stopAtNextRecordingStep = false;
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				play.setEnabled(false);
 				stop.setEnabled(true);
 				revert.setEnabled(false);
 				notifyModelListeners(new ModelEvent(MDModel.this, ModelEvent.MODEL_RUN));
 				notifyPageComponentListeners(new PageComponentEvent(MDModel.this, PageComponentEvent.COMPONENT_RUN));
 				getView().requestFocusInWindow();
 			}
 		});
 	}
 
 	public boolean isRunning() {
 		if (job == null)
 			return false;
 		return !job.isStopped();
 	}
 
 	/**
 	 * stop running this model. NOTE: When the recording mode is invoked, calling this method does not stop the model
 	 * immediately; it will stop at the next recording step. If not in the recording mode, it will stop immediately.
 	 */
 	public void stop() {
 		if (getRecorderDisabled()) {
 			stopImmediately();
 		}
 		else {
 			if (movie.getCurrentFrameIndex() > 0) {
 				stopAtNextRecordingStep = true;
 			}
 			else {
 				stopImmediately();
 			}
 		}
 	}
 
 	/** stop the model immediately without retarding to the next recording step */
 	public void stopImmediately() {
 		if (job == null)
 			return;
 		if (job.isTurnedOff())
 			return;
 		job.turnOff();
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				getView().repaint();
 				if (getView() instanceof AtomisticView)
 					((AtomisticView) getView()).refreshJmol();
 				((MDView) getView()).enableEditor(true);
 				stop.setEnabled(false);
 				play.setEnabled(true);
 				revert.setEnabled(true);
 				if (movie != null) {
 					movie.pause();
 					movie.enableMovieActions(true);
 				}
 				notifyModelListeners(new ModelEvent(MDModel.this, ModelEvent.MODEL_STOP));
 			}
 		});
 	}
 
 	public boolean isTapeFull() {
 		if (movie == null)
 			throw new RuntimeException("There is no tape in the recorder");
 		return getTapePointer() == movie.getCapacity();
 	}
 
 	/** export the simulation as a SMIL movie */
 	public void exportSmilMovie() {
 		if (imageStreamGenerator == null) {
 			if (job == null)
 				initializeJob();
 			imageStreamGenerator = new ImageStreamGenerator(getView(), job);
 		}
 		imageStreamGenerator.chooseDirectory();
 	}
 
 	/** get the time series of potential energy per particle */
 	public FloatQueue getPotTS() {
 		return pote;
 	}
 
 	/** get the time series of kinetic energy per particle */
 	public FloatQueue getKinTS() {
 		return kine;
 	}
 
 	/** get the time series of total energy per particle */
 	public FloatQueue getTotTS() {
 		return tote;
 	}
 
 	/** return the potential energy per atom */
 	public double getPot() {
 		return pot;
 	}
 
 	/** return the total energy per atom */
 	public double getTot() {
 		tot = pot + kin;
 		return tot;
 	}
 
 	public void showTimeSeries() {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				if (timeSeriesRepository == null)
 					timeSeriesRepository = new TimeSeriesRepository(JOptionPane.getFrameForComponent(getView()));
 				timeSeriesRepository.clear();
 				timeSeriesRepository.addQueueGroup(movieQueueGroup);
 				timeSeriesRepository.pack();
 				timeSeriesRepository.setLocationRelativeTo(getView());
 				timeSeriesRepository.setVisible(true);
 			}
 		});
 	}
 
 	public void clearTimeSeries() {
 		movieQueueGroup.clear();
 		if (movieQueueGroup.getTable() != null)
 			movieQueueGroup.getTable().clear();
 		movieQueueGroup.add(kine);
 		movieQueueGroup.add(pote);
 		movieQueueGroup.add(tote);
 		for (FloatQueue q : channelTs)
 			movieQueueGroup.add(q);
 		if (timeSeriesRepository == null)
 			return;
 		timeSeriesRepository.clear();
 	}
 
 	/** this method can be used to empty a model. */
 	public void clear() {
 		// haltScriptExecution();
 		// if we halt scripts, then the scripts that may call this method will not be able to run
 		initializationScript = null;
 		tot = 0;
 		kin = 0;
 		pot = 0;
 		lastCheckedTot = 0;
 		lastCheckedKin = 0;
 		setModelTime(0);
 		enableReminder(false);
 		reminder.setCompleted(false);
 		stopAtNextRecordingStep = false;
 		setTimeStep(1.0);
 		removeAllFields();
 		clearTimeSeries();
 		boundary.setType(Boundary.DBC_ID);
 		range_xmin = range_ymin = 20;
 		range_xmax = range_ymax = 20;
 		id_xmin = id_xmax = id_ymin = id_ymax = -1;
 		((MDView) getView()).clear();
 		((MDView) getView()).enableEditor(true);
 		properties.clear();
 		activateHeatBath(false);
 		if (hasEmbeddedMovie())
 			insertNewTape();
 		if (movie != null) {
 			movie.enableAllMovieActions(false);
 			movie.getMovieSlider().repaint();
 		}
 		// This MUST be after the call to hasEmbeddedMovie() in order for the
 		// special case where there is only one obstacle to work.
 		if (obstacles != null && !obstacles.isEmpty()) {
 			for (Iterator it = obstacles.iterator(); it.hasNext();)
 				((RectangularObstacle) it.next()).destroy();
 			obstacles.clear();
 		}
 		setUniverse(new Universe());
 		if (computeList != null)
 			computeList.clear();
 		if (updateListenerList != null) {
 			// we must clear the listener to allow gc to clean, but this method is also called by reset(),
 			// so we save a copy of the listeners to be used after reset() is called
 			if (updateListenerListCopy == null)
 				updateListenerListCopy = new ArrayList<UpdateListener>();
 			else updateListenerListCopy.clear();
 			updateListenerListCopy.addAll(updateListenerList);
 			updateListenerList.clear();
 		}
 		// Do NOT remove the following listeners because we are reusing this model container!
 		// if (pageComponentListenerList != null) pageComponentListenerList.clear();
 		// if(modelListenerList!=null) modelListenerList.clear();
 		if (job != null)
 			job.removeAllCustomTasks();
 	}
 
 	public void setModelProperties() {
 		if (modelProp == null) {
 			if (this instanceof MesoModel) {
 				modelProp = new MesoModelProperties(JOptionPane.getFrameForComponent(getView()));
 			}
 			else {
 				modelProp = new MolecularModelProperties(JOptionPane.getFrameForComponent(getView()));
 			}
 		}
 	}
 
 	public ModelProperties getModelProperties() {
 		return modelProp;
 	}
 
 	public void setView(MDView v) {
 
 		if (v == null)
 			throw new IllegalArgumentException("view cannot be set null");
 
 		if (getView() == null) {
 			switchMap.putAll(v.getSwitches());
 			choiceMap.putAll(v.getChoices());
 		}
 		actionReminder.setParentComponent(v);
 
 		// actions that we don't want to be accessed by an author are put into the view's ActionMap
 
 		// actions with key bindings
 		v.getInputMap().put((KeyStroke) modelReader.getValue(Action.ACCELERATOR_KEY), "Open File");
 		v.getActionMap().put("Open File", modelReader);
 		v.getInputMap().put((KeyStroke) modelWriter.getValue(Action.ACCELERATOR_KEY), "Save File");
 		v.getActionMap().put("Save File", modelWriter);
 		v.getInputMap().put((KeyStroke) snapshot.getValue(Action.ACCELERATOR_KEY), "Snapshot");
 		v.getActionMap().put("Snapshot", snapshot);
 		v.getInputMap().put((KeyStroke) revert.getValue(Action.ACCELERATOR_KEY), "Revert");
 		v.getActionMap().put("Revert", revert);
 		v.getInputMap().put((KeyStroke) accessProp.getValue(Action.ACCELERATOR_KEY), "Properties");
 		v.getActionMap().put("Properties", accessProp);
 
 		// actions without key bindings
 		v.getActionMap().put("Play", play);
 		v.getActionMap().put("Stop", stop);
 		v.getActionMap().put("Reload", reload);
 		v.getActionMap().put("Model Reader", modelReader);
 		v.getActionMap().put("Model Writer", modelWriter);
 
 	}
 
 	public abstract JComponent getView();
 
 	public void setInitializationScript(String s) {
 		if (s != null && s.trim().equals(""))
 			s = null;
 		initializationScript = s;
 	}
 
 	public String getInitializationScript() {
 		return initializationScript;
 	}
 
 	public ActionReminder getActionReminder() {
 		return actionReminder;
 	}
 
 	public void setUniverse(Universe u) {
 		universe = u;
 	}
 
 	public Universe getUniverse() {
 		return universe;
 	}
 
 	public abstract void setFriction(float friction);
 
 	public void setBoundary(RectangularBoundary boundary) {
 		this.boundary = boundary;
 	}
 
 	public RectangularBoundary getBoundary() {
 		return boundary;
 	}
 
 	public ObstacleCollection getObstacles() {
 		return obstacles;
 	}
 
 	/* retrieve obstacles stored in a delegate collection */
 	void setObstacles(ArrayList delegates) {
 		// nullify all the previous obstacles to prevent memory leak
 		if (obstacles != null && !obstacles.isEmpty()) {
 			for (Iterator it = obstacles.iterator(); it.hasNext();) {
 				((RectangularObstacle) it.next()).destroy();
 			}
 			obstacles.clear();
 		}
 		if (delegates == null || delegates.isEmpty())
 			return;
 		RectangularObstacle.Delegate d;
 		for (Iterator it = delegates.iterator(); it.hasNext();) {
 			d = (RectangularObstacle.Delegate) it.next();
 			obstacles.add(new RectangularObstacle(d.getX(), d.getY(), d.getWidth() == 0 ? 1 : d.getWidth(), d
 					.getHeight() == 0 ? 1 : d.getHeight(), d.getVx(), d.getVy(), d.getExternalFx(), d.getExternalFy(),
 					d.getUserField(), d.getElasticity(), d.getFriction(), d.getDensity(), d.isWestProbe(), d
 							.isNorthProbe(), d.isEastProbe(), d.isSouthProbe(), d.getPermeability(), d.isBounced(), d
 							.isVisible(), d.isRoundCornered(), d.getFillMode()));
 		}
 	}
 
 	/**
 	 * Only one of a certain type of non-local field is allowed to exist at a time, superposition of non-local fields
 	 * are not implemented.
 	 */
 	public void addNonLocalField(VectorField field) {
 		if (field.isLocal())
 			return;
 		boolean exist = false;
 		if (!fields.isEmpty()) {
 			Class c = field.getClass();
 			synchronized (fields) {
 				for (VectorField f : fields) {
 					if (c.isInstance(f) && !f.isLocal()) {
 						exist = true;
 						break;
 					}
 				}
 			}
 		}
 		if (!exist) {
 			fields.add(field);
 			field.setBounds(boundary.getView().getBounds());
 			changeMap.put(getFieldChanger(field).toString(), getFieldChanger(field));
 			notifyModelListeners(new ModelEvent(this, field.getClass().getName(), null, "add field"));
 			notifyChange();
 		}
 	}
 
 	boolean removeNonLocalField(VectorField field) {
 		if (field.isLocal())
 			return false;
 		notifyModelListeners(new ModelEvent(this, field.getClass().getName(), null, "remove field"));
 		notifyPageComponentListeners(new PageComponentEvent(this, PageComponentEvent.COMPONENT_CHANGED));
 		changeMap.remove(getFieldChanger(field).toString());
 		return fields.remove(field);
 	}
 
 	/*
 	 * call this method to clear the vector to hold fields. It will also remove the change listener associated with the
 	 * fields.
 	 */
 	private void removeAllFields() {
 		synchronized (fields) {
 			for (VectorField f : fields) {
 				if (!f.isLocal()) {
 					changeMap.remove(getFieldChanger(f).toString());
 					notifyModelListeners(new ModelEvent(this, f.getClass().getName(), null, "remove field"));
 				}
 			}
 		}
 		fields.removeAllElements();
 		notifyPageComponentListeners(new PageComponentEvent(this, PageComponentEvent.COMPONENT_CHANGED));
 	}
 
 	void addAllNonLocalFields(Vector<VectorField> v) throws IllegalArgumentException {
 		synchronized (fields) {
 			for (VectorField f : v) {
 				if (!f.isLocal()) {
 					addNonLocalField(f);
 					changeMap.put(getFieldChanger(f).toString(), getFieldChanger(f));
 				}
 			}
 		}
 	}
 
 	public VectorField getNonLocalField(String s) {
 		synchronized (fields) {
 			for (VectorField f : fields) {
 				if (!f.isLocal() && f.getClass().getName().equals(s))
 					return f;
 			}
 		}
 		return null;
 	}
 
 	public boolean removeField(String s) {
 		synchronized (fields) {
 			for (VectorField f : fields) {
 				if (f.getClass().getName().equals(s)) {
 					if (!f.isLocal())
 						changeMap.remove(getFieldChanger(f).toString());
 					notifyModelListeners(new ModelEvent(this, s, null, "remove field"));
 					notifyPageComponentListeners(new PageComponentEvent(this, PageComponentEvent.COMPONENT_CHANGED));
 					return fields.remove(f);
 				}
 			}
 		}
 		return false;
 	}
 
 	void putProperties(File file) {
 		if (file != null) {
 			putProperty("url", file.getPath());
 			putProperty("codebase", file.getParent());
 			putProperty("filename", file.getName());
 			putProperty("date", new Date(file.lastModified()));
 			putProperty("size", new Long(file.length()));
 		}
 	}
 
 	void putProperties(URL u) {
 		if (u != null) {
 			putProperty("url", u.toString());
 			String path = u.getFile();
 			String fileName = path;
 			String codeBase = path;
 			int slash = path.lastIndexOf("/");
 			if (slash != -1) {
 				fileName = path.substring(slash + 1, path.length());
 				codeBase = path.substring(0, slash);
 			}
 			putProperty("codebase", codeBase);
 			putProperty("filename", fileName);
 			long[] t = ConnectionManager.getLastModifiedAndContentLength(u);
 			putProperty("date", new Date(t[0]));
 			putProperty("size", new Long(t[1]));
 		}
 	}
 
 	void saveImages(File parentFile) {
 		if (((MDView) getView()).getFillMode() instanceof FillMode.ImageFill) {
 			String url = ((FillMode.ImageFill) ((MDView) getView()).getFillMode()).getURL();
 			String s = url;
 			String base = FileUtilities.getCodeBase(url);
 			/*
 			 * if background image is set using the filechooser the URL field of the ImageFill will be set to be the
 			 * real HD address of the image. If background image transfers from another model because of downloading or
 			 * saving, the URL field of the ImageFill will be set to the file name of that image ONLY.
 			 */
 			if (base == null) {
 				base = FileUtilities.getCodeBase((String) getProperty("old url"));
 				if (base == null)
 					base = FileUtilities.getCodeBase((String) getProperty("url"));
 				s = base + url;
 				saveImage(s, parentFile);
 			}
 			else {
 				if (FileUtilities.isRemote(s)) {
 					saveImage(s, parentFile);
 				}
 				else {
 					copyFile(s, new File(parentFile, FileUtilities.getFileName(s)));
 				}
 			}
 		}
 		ImageComponent[] img = ((MDView) getView()).getImages();
 		if (img.length > 0) {
 			for (int i = 0; i < img.length; i++) {
 				String url = img[i].toString();
 				String s = url;
 				String base = FileUtilities.getCodeBase(url);
 				if (base == null) {
 					base = FileUtilities.getCodeBase((String) getProperty("old url"));
 					if (base == null)
 						base = FileUtilities.getCodeBase((String) getProperty("url"));
 					s = base + url;
 					saveImage(s, parentFile);
 				}
 				else {
 					copyFile(s, new File(parentFile, FileUtilities.getFileName(s)));
 				}
 			}
 		}
 	}
 
 	private void saveImage(final String name, final File parent) {
 		final int i = ModelerUtilities.copyResourceToDirectory(name, parent);
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				switch (i) {
 				case FileUtilities.SOURCE_NOT_FOUND:
 					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getView()), "Source " + name
 							+ " is not found.", "File not found", JOptionPane.ERROR_MESSAGE);
 					break;
 				case FileUtilities.FILE_ACCESS_ERROR:
 					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getView()), "Directory " + parent
 							+ " inaccessible.", "File access error", JOptionPane.ERROR_MESSAGE);
 					break;
 				case FileUtilities.WRITING_ERROR:
 					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getView()),
 							"Encountered error while writing to directory " + parent, "Writing error",
 							JOptionPane.ERROR_MESSAGE);
 					break;
 				}
 			}
 		});
 	}
 
 	void copyFile(final String s, final File d) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
				switch (FileUtilities.copy(new File(s), d)) {
 				case FileUtilities.SOURCE_NOT_FOUND:
 					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getView()), "Source " + s
 							+ " is not found.", "File not found", JOptionPane.ERROR_MESSAGE);
 					break;
 				case FileUtilities.FILE_ACCESS_ERROR:
 					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getView()), "Destination " + d
 							+ " cannot be created.", "File access error", JOptionPane.ERROR_MESSAGE);
 					break;
 				case FileUtilities.WRITING_ERROR:
 					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getView()),
 							"Encountered error while writing to " + d, "Writing error", JOptionPane.ERROR_MESSAGE);
 					break;
 				}
 			}
 		});
 	}
 
 	private ChangeListener getFieldChanger(VectorField f) {
 		if (f instanceof GravitationalField)
 			return gFieldChanger;
 		if (f instanceof ElectricField)
 			return eFieldChanger;
 		if (f instanceof MagneticField)
 			return bFieldChanger;
 		if (f instanceof AccelerationalField)
 			return aFieldChanger;
 		return null;
 	}
 
 	void setAField(double d) {
 		synchronized (fields) {
 			for (VectorField f : fields) {
 				if (f instanceof AccelerationalField) {
 					f.setIntensity(d);
 					break;
 				}
 			}
 		}
 	}
 
 	void setGField(double d) {
 		synchronized (fields) {
 			for (VectorField f : fields) {
 				if (f instanceof GravitationalField) {
 					f.setIntensity(d);
 					break;
 				}
 			}
 		}
 	}
 
 	void setEField(double d) {
 		synchronized (fields) {
 			for (VectorField f : fields) {
 				if (f instanceof ElectricField) {
 					f.setIntensity(d);
 					break;
 				}
 			}
 		}
 	}
 
 	void setBField(double d) {
 		synchronized (fields) {
 			for (VectorField f : fields) {
 				if (f instanceof MagneticField) {
 					f.setIntensity(d);
 					break;
 				}
 			}
 		}
 	}
 
 	void setT(double d) {
 		if (!heatBathActivated()) {
 			setTemperature(d);
 		}
 		else {
 			assignTemperature(d);
 			heatBath.setExpectedTemperature(d);
 		}
 	}
 
 	void blockView(final boolean b) {
 		((MDView) getView()).setRepaintBlocked(b);
 		getView().repaint();
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				getView().setCursor(Cursor.getPredefinedCursor(b ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
 				if (getIOProgressBar() != null)
 					return;
 				if (!b) {
 					monitor.hide();
 				}
 				else {
 					monitor.show(getView().getLocationOnScreen().x + (getView().getWidth() - monitor.getSize().width)
 							/ 2, getView().getLocationOnScreen().y + (getView().getHeight() - monitor.getSize().height)
 							/ 2);
 				}
 			}
 		});
 	}
 
 	void loadLayeredComponent(MDView.State vs) {
 		MDView view = (MDView) getView();
 		int cap = 0;
 		boolean noLayerSaved = false;
 		ImageComponent.Delegate[] icd = vs.getImages();
 		if (icd != null) {
 			String path = (String) getProperty("url");
 			if (path != null)
 				view.loadImageComponents(path, icd);
 			if (icd.length >= 2) { // backward compatible
 				if (icd[0].getLayerPosition() == icd[1].getLayerPosition())
 					noLayerSaved = true;
 			}
 			cap += icd.length;
 		}
 		TextBoxComponent.Delegate[] tbd = vs.getTextBoxes();
 		if (tbd != null) {
 			view.loadTextBoxComponents(tbd);
 			cap += tbd.length;
 		}
 		LineComponent.Delegate[] lcd = vs.getLines();
 		if (lcd != null) {
 			view.loadLineComponents(lcd);
 			cap += lcd.length;
 		}
 		RectangleComponent.Delegate[] rcd = vs.getRectangles();
 		if (rcd != null) {
 			view.loadRectangleComponents(rcd);
 			cap += rcd.length;
 		}
 		EllipseComponent.Delegate[] ecd = vs.getEllipses();
 		if (ecd != null) {
 			view.loadEllipseComponents(ecd);
 			cap += ecd.length;
 		}
 		// reshuffle the layered components according to the stored layer positions
 		if (cap > 0 && !noLayerSaved) {
 			Layered[] x = new Layered[cap];
 			if (icd != null) {
 				ImageComponent[] im = view.getImages();
 				for (int i = 0; i < im.length; i++)
 					x[icd[i].getLayerPosition()] = im[i];
 			}
 			if (tbd != null) {
 				TextBoxComponent[] tb = view.getTextBoxes();
 				for (int i = 0; i < tb.length; i++)
 					x[tbd[i].getLayerPosition()] = tb[i];
 			}
 			if (lcd != null) {
 				LineComponent[] lc = view.getLines();
 				for (int i = 0; i < lc.length; i++)
 					x[lcd[i].getLayerPosition()] = lc[i];
 			}
 			if (rcd != null) {
 				RectangleComponent[] rc = view.getRectangles();
 				for (int i = 0; i < rc.length; i++)
 					x[rcd[i].getLayerPosition()] = rc[i];
 			}
 			if (ecd != null) {
 				EllipseComponent[] ec = view.getEllipses();
 				for (int i = 0; i < ec.length; i++)
 					x[ecd[i].getLayerPosition()] = ec[i];
 			}
 			view.removeAllLayeredComponents();
 			view.addLayeredComponents(x);
 		}
 	}
 
 	public boolean isLoading() {
 		return isLoading.get();
 	}
 
 	void prepareToRead() {
 		isLoading.set(true);
 		stopImmediately();
 		blockView(true);
 		if (monitor == null)
 			createProgressMonitor();
 		else monitor.resetProgressBar();
 		((MDView) getView()).enableEditor(false);
 		((MDView) getView()).destroyAllLayeredComponents();
 		if (movie != null)
 			movie.enableAllMovieActions(false);
 		clear();
 	}
 
 	private void finishReading() {
 		notifyModelListeners(new ModelEvent(this, ModelEvent.MODEL_INPUT));
 		notifyPageComponentListeners(new PageComponentEvent(this, PageComponentEvent.COMPONENT_LOADED));
 		blockView(false);
 		play.setEnabled(true);
 		stop.setEnabled(false);
 		revert.setEnabled(false);
 		if (updateListenerListCopy != null && updateListenerList != null) {
 			updateListenerList.addAll(updateListenerListCopy);
 		}
 	}
 
 	class InputJob {
 
 		private File file;
 		private URL url;
 		private XMLDecoder in;
 		private InputStream is;
 		private FileInputStream fis;
 		private FileOutputStream fos;
 
 		InputJob() {
 			prepareToRead();
 		}
 
 		void read(File f) {
 			file = f;
 			readXML();
 			isLoading.set(false);
 			setProgress(ioProgressBar.getMaximum(), "100%");
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					finishReading();
 				}
 			});
 			setProgress(0, "Done");
 		}
 
 		void read(URL u) {
 			file = ConnectionManager.sharedInstance().shouldUpdate(u);
 			url = file == null ? u : null;
 			readXML();
 			isLoading.set(false);
 			setProgress(ioProgressBar.getMaximum(), "100%");
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					finishReading();
 				}
 			});
 			setProgress(0, "Done");
 		}
 
 		private void updateProperties() {
 			if (file != null) {
 				URL u = ConnectionManager.sharedInstance().getRemoteCopy(file);
 				if (u == null)
 					putProperties(file);
 				else putProperties(u);
 				return;
 			}
 			if (url != null)
 				putProperties(url);
 		}
 
 		private void readXML() {
 
 			updateProperties();
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					monitor.getProgressBar().setMaximum((int) (((Long) getProperty("size")).longValue() / 10240.0) + 1);
 					monitor.getProgressBar().setMinimum(0);
 				}
 			});
 
 			if (file != null) {
 				monitor.setProgressMessage("Opening " + file.getName() + ", wait...");
 				try {
 					fis = new FileInputStream(file);
 				}
 				catch (IOException e) {
 					handleFailure("Error in opening " + file);
 					e.printStackTrace();
 					EventQueue.invokeLater(new Runnable() {
 						public void run() {
 							JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getView()), file
 									+ " was not found or has a problem.", "File error", JOptionPane.ERROR_MESSAGE);
 						}
 					});
 					return;
 				}
 				if (fis != null)
 					in = new XMLDecoder(new BufferedInputStream(fis));
 			}
 			else if (url != null) {
 				monitor.setProgressMessage("Opening " + url + ", wait...");
 				URLConnection connect = ConnectionManager.getConnection(url);
 				if (connect == null) {
 					handleFailure("Error in connecting to " + url);
 					return;
 				}
 				try {
 					is = connect.getInputStream();
 				}
 				catch (IOException e) {
 					handleFailure("Error in getting input stream from " + url);
 					e.printStackTrace();
 					return;
 				}
 				// cache the model files if necessary
 				if (ConnectionManager.sharedInstance().isCachingAllowed()) {
 					String cachedFile = ConnectionManager.convertURLToFileName(url);
 					File cache = new File(ConnectionManager.sharedInstance().getCacheDirectory(), cachedFile);
 					cache.getParentFile().mkdirs();
 					try {
 						fos = new FileOutputStream(cache);
 					}
 					catch (FileNotFoundException fnfe) {
 						fnfe.printStackTrace();
 						handleFailure("Error in finding cached file for " + url);
 						return;
 					}
 					byte b[] = new byte[1024];
 					int amount;
 					float kbCount = 0.0f;
 					float constant = 1.0f / 1024.0f;
 					boolean shouldReturn = false;
 					try {
 						while ((amount = is.read(b)) != -1) {
 							fos.write(b, 0, amount);
 							kbCount += amount * constant;
 							if ((int) kbCount % 10 == 0)
 								monitor.setProgressMessage((int) kbCount + " KB read");
 						}
 						monitor.setProgressMessage((int) kbCount + " KB read");
 					}
 					catch (IOException ioe) {
 						handleFailure("Error in caching " + url);
 						ioe.printStackTrace();
 						shouldReturn = true;
 					}
 					finally {
 						if (is != null) {
 							try {
 								is.close();
 							}
 							catch (IOException e) {
 							}
 						}
 						if (fos != null) {
 							try {
 								fos.close();
 							}
 							catch (IOException e) {
 							}
 						}
 					}
 					cache.setLastModified(connect.getLastModified());
 					BufferedInputStream bis = null;
 					try {
 						bis = new BufferedInputStream(new FileInputStream(cache));
 					}
 					catch (FileNotFoundException e) {
 						e.printStackTrace();
 						handleFailure("Error in caching " + url);
 						shouldReturn = true;
 					}
 					if (shouldReturn)
 						return;
 					in = new XMLDecoder(bis);
 				}
 				else {
 					in = new XMLDecoder(new BufferedInputStream(is));
 				}
 			}
 
 			if (in != null) {
 				try {
 					decode(in);
 				}
 				catch (Exception e) {
 					EventQueue.invokeLater(new Runnable() {
 						public void run() {
 							stopJob();
 							// prepareToRead();
 							JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getView()),
 									"Error in XML-decoding", "Error", JOptionPane.ERROR_MESSAGE);
 						}
 					});
 					e.printStackTrace();
 				}
 				finally {
 					in.close();
 				}
 			}
 
 		}
 
 		void stopJob() {
 			if (is != null)
 				try {
 					is.close();
 				}
 				catch (IOException e) {
 				}
 			if (fos != null)
 				try {
 					fos.close();
 				}
 				catch (IOException e) {
 				}
 			if (fis != null)
 				try {
 					fis.close();
 				}
 				catch (IOException e) {
 				}
 			if (in != null)
 				in.close();
 			blockView(false);
 			setProgress(0, "Stopped");
 		}
 
 	}
 
 	public abstract static class State extends MDState {
 
 		private List<String> computeList;
 		private Universe universe;
 		private Vector<VectorField> fields;
 		private ArrayList<RectangularObstacle.Delegate> obstacles;
 		private RectangularBoundary.Delegate boundary;
 		private int numberOfParticles;
 		private double timeStep = 2.0;
 		private HeatBath bath;
 		private int frameInterval = 100;
 		private boolean repeatReminder;
 		private int reminderInterval = 5000;
 		private boolean reminderEnabled;
 		private String reminderMessage;
 		private String script;
 
 		public State() {
 			computeList = new ArrayList<String>();
 			obstacles = new ArrayList<RectangularObstacle.Delegate>();
 			fields = new Vector<VectorField>();
 		}
 
 		public void setScript(String s) {
 			script = s;
 		}
 
 		public String getScript() {
 			return script;
 		}
 
 		public void setReminderMessage(String s) {
 			reminderMessage = s;
 		}
 
 		public String getReminderMessage() {
 			return reminderMessage;
 		}
 
 		public void setReminderEnabled(boolean b) {
 			reminderEnabled = b;
 		}
 
 		public boolean getReminderEnabled() {
 			return reminderEnabled;
 		}
 
 		public void setRepeatReminder(boolean b) {
 			repeatReminder = b;
 		}
 
 		public boolean getRepeatReminder() {
 			return repeatReminder;
 		}
 
 		public void setReminderInterval(int i) {
 			reminderInterval = i;
 		}
 
 		public int getReminderInterval() {
 			return reminderInterval;
 		}
 
 		public void setComputeList(List<String> list) {
 			computeList.clear();
 			if (list != null)
 				computeList.addAll(list);
 		}
 
 		public List<String> getComputeList() {
 			return computeList;
 		}
 
 		public void setFrameInterval(int i) {
 			frameInterval = i;
 		}
 
 		public int getFrameInterval() {
 			return frameInterval;
 		}
 
 		public void setHeatBath(HeatBath hb) {
 			bath = hb;
 		}
 
 		public HeatBath getHeatBath() {
 			return bath;
 		}
 
 		public void setNumberOfParticles(int n) {
 			numberOfParticles = n;
 		}
 
 		public int getNumberOfParticles() {
 			return numberOfParticles;
 		}
 
 		public void setTimeStep(double d) {
 			timeStep = d;
 		}
 
 		public double getTimeStep() {
 			return timeStep;
 		}
 
 		public void setUniverse(Universe universe) {
 			this.universe = universe;
 		}
 
 		public Universe getUniverse() {
 			return universe;
 		}
 
 		public void setFields(Vector<VectorField> v) {
 			fields.clear();
 			for (VectorField vf : v) {
 				if (!vf.isLocal())
 					fields.add(vf);
 			}
 		}
 
 		public Vector<VectorField> getFields() {
 			return fields;
 		}
 
 		public void setObstacles(ArrayList obs) {
 			obstacles.clear();
 			RectangularObstacle o = null;
 			for (Iterator it = obs.iterator(); it.hasNext();) {
 				o = (RectangularObstacle) it.next();
 				obstacles.add(new RectangularObstacle.Delegate(o.x, o.y, o.width, o.height, o.getVx(), o.getVy(), o
 						.getHx(), o.getHy(), o.getUserField(), o.getElasticity(), o.getFriction(), o.getDensity(), o
 						.isWestProbe(), o.isNorthProbe(), o.isEastProbe(), o.isSouthProbe(), o.permeable,
 						o.isBounced(), o.isVisible(), o.getRoundCornerRadius() > 0, o.getFillMode()));
 			}
 		}
 
 		public ArrayList getObstacles() {
 			return obstacles;
 		}
 
 		public void setBoundary(RectangularBoundary.Delegate rb) {
 			if (rb == null)
 				throw new IllegalArgumentException("A model must have a boundary.");
 			boundary = rb;
 		}
 
 		public RectangularBoundary.Delegate getBoundary() {
 			return boundary;
 		}
 
 	}
 
 	class EmbeddedMovie extends SlideMovie {
 
 		public synchronized int length() {
 			if (modelTimeQueue == null || modelTimeQueue.isEmpty())
 				return 0;
 			if (modelTimeQueue.getPointer() == modelTimeQueue.getLength())
 				return modelTimeQueue.getLength();
 			return modelTimeQueue.getPointer();
 		}
 
 		/* show the <i>i</i>-th frame of the movie */
 		public void showFrame(int frame) {
 			super.showFrame(frame);
 			showMovieFrame(frame);
 			getView().repaint();
 		}
 
 	}
 
 }
