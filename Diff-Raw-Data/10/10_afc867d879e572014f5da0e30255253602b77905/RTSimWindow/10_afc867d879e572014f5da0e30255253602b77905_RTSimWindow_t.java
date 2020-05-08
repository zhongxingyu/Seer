 /*
  * Copyright (c) 2003-2013, University of Luebeck, Institute of Computer Engineering
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the University of Luebeck, the Institute of Computer
  *       Engineering nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior
  *       written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE UNIVERSITY OF LUEBECK OR THE INSTITUTE OF COMPUTER
  * ENGINEERING BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
  * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 
 
 package de.uniluebeck.iti.rteasy.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Event;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.GraphicsEnvironment;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.Locale;
 
 import javax.swing.Action;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDesktopPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.text.DefaultHighlighter;
 import javax.swing.text.Document;
 import javax.swing.text.Element;
 import javax.swing.text.Keymap;
 import javax.swing.undo.UndoManager;
 
 import de.uniluebeck.iti.rteasy.PositionRange;
 import de.uniluebeck.iti.rteasy.RTSimGlobals;
 import de.uniluebeck.iti.rteasy.frontend.ASTRtProg;
 import de.uniluebeck.iti.rteasy.frontend.RTSim_Parser;
 import de.uniluebeck.iti.rteasy.kernel.Expression;
 import de.uniluebeck.iti.rteasy.kernel.RTProgram;
 import de.uniluebeck.iti.rteasy.kernel.RTSim_SemAna;
 import de.uniluebeck.iti.rteasy.kernel.RegisterArray;
 import de.uniluebeck.iti.rteasy.kernel.Statement;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 public abstract class RTSimWindow extends JFrame implements ActionListener {
 
 	private static String version;
 
 	private boolean filenameEndsWithRt(String filename) {
 		return filename.endsWith(".rt") || filename.endsWith(".RT")
 				|| filename.endsWith(".Rt") || filename.endsWith(".rT");
 	}
 
 	private String chompRtFilename(String filename) {
 		int len = filename.length();
 		if (len <= 6)
 			return filename;
 		String sub = filename.substring(0, len - 3);
 		if (filenameEndsWithRt(sub))
 			return sub;
 		else
 			return filename;
 	}
 
 	class RtFileFilter extends javax.swing.filechooser.FileFilter implements
 			java.io.FileFilter {
 		RtFileFilter() {
 		}
 
 		public boolean accept(File f) {
 			if (f.isDirectory())
 				return true;
 			String filename = f.getPath();
 			return filenameEndsWithRt(filename);
 		}
 
 		public String getDescription() {
 			return IUI.get("FILEDESC_RT");
 		}
 	}
 
 	class Runner implements Runnable {
 		private boolean runPermission = false;
 
 		Runner() {
 		}
 
 		public synchronized void setRunPermission(boolean p) {
 			runPermission = p;
 		}
 
 		public synchronized boolean checkRunPermission() {
 			return runPermission;
 		}
 
 		public void run() {
 			setRunPermission(true);
 			while (checkRunPermission() && RTSimWindow.this.step())
 				;
 			RTSimWindow.this.runStopButton.setToolTipText("Run");
 			RTSimWindow.this.runStopButton.setText("Run");
 			RTSimWindow.this.mi_srun.setText("Run");
 			return;
 		}
 	}
 
 	private int doubleUndo = -1;
 	private int countUndo = 0;
 	private UndoManager undo;
 	private MaintenanceWindow mwindow;
 	private boolean editorAreaModified = false;
 	private RTProgram rtprog;
 	private JDesktopPane desktop;
 	private SimObjectsFrame soframe;
 	private JInternalFrame logFrame;
 	private JInternalFrame editorFrame;
 	private JPanel editorPane;
 	private JTextArea editorArea;
 	private JTextArea logArea;
 	private JTextArea numberArea;
 	private File file = null;
 	private HelpBrowserFrame helpFrame;
 	private SettingsFrame settingsFrame = null;
 	private boolean alreadyCompiled = false;
 	private boolean hasEditorFrame = false;
 	private boolean hasLogFrame = false;
 	private boolean hasSimObjectsFrame = false;
 	private JLabel cursorPos;
 	private RtFileFilter fileFilter = new RtFileFilter();
 	private DocumentListener editorAreaListener;
 	private int stepMarkBegin = -1;
 	private int stepMarkEnd = -1;
 	private Object highlight = null;
 	private Object microStepHighlight = null;
 	private Object secMicroStepHighlight = null;
 	private boolean hasMicroStepMark = false;
 	private JButton simuButton, resetButton, stepButton, microStepButton,
 			runStopButton, breakButton, logClearButton;
 	private final static int M_NONE = 0;
 	private final static int M_EDIT = 1;
 	private final static int M_SIMU = 2;
 	private int mode = M_NONE;
 	private JMenu fileMenu, editMenu, designMenu, simuMenu, examMenu, helpMenu;
 	private JMenuItem mi_save, mi_saveAs, mi_undo, mi_ecopy, mi_ecut,
 			mi_epaste, mi_fnew, mi_epretty, mi_esettings, mi_fquit,
 			mi_dsignals, mi_dshowsig, mi_dexppipe, mi_dvhdlcu, mi_dvhdlou,
 			mi_dvhdltb, mi_dvhdlall, mi_sreset, mi_sstep, mi_smicro, mi_srun,
 			mi_slogres, mi_scommand, mi_sbreak, mi_smemres, mi_fopen,
 			mi_hindex, mi_habout;
 	private JScrollPane logScrollPane;
 	private BreakpointFrame breakFrame;
 	private boolean runPermission = false;
 	private Runner runner = new Runner();
 	private Thread runThread;
 	private final static String ITIICON_URL = "/help/itilogo.gif";
 	private final static String BUTTON_STEP_URL = "/help/symbol_step.gif";
 	private final static String BUTTON_MICROSTEP_URL = "/help/symbol_microstep.gif";
 	private final static String BUTTON_RUN_URL = "/help/symbol_run.gif";
 	private final static String BUTTON_STOP_URL = "/help/symbol_stop.gif";
 
 	// private final static String aboutMessage =
 	// "RTeasy Version "+version+"\n\n\u00A9 2003-2004 Institut f\u00FCr Technische Informatik (ITI), Universit\u00E4t zu L\u00FCbeck\n\nEntwickelt zu dem Skript von Prof. Dr.-Ing. Erik Maehle zur Vorlesung \"Technische Grundlagen der Informatik\"\n\nProjektleitung: Dipl.-Inf. Carsten Albrecht\nAusf\u00FChrung: cand. inf. Hagen Schendel";
 
 	/**
 	 * Alle Men&uuml;punkte, die nur im Simulationsmodus gehen, setzen.
 	 */
 	private void setSimuEnabled(boolean b) {
 		mi_sreset.setEnabled(b);
 		mi_sstep.setEnabled(b);
 		mi_smicro.setEnabled(b);
 		mi_srun.setEnabled(b);
 		mi_sbreak.setEnabled(b);
 		mi_smemres.setEnabled(b);
 	}
 
 	/**
 	 * Alle Men&uuml;punkte, die nur im Editiermodus gehen, setzen.
 	 */
 	protected void setEditMenuEnabled(boolean b) {
 		mi_ecopy.setEnabled(b);
 		mi_ecut.setEnabled(b);
 		mi_undo.setEnabled(b);
 		mi_epaste.setEnabled(b);
 		mi_epretty.setEnabled(b);
 		mi_dsignals.setEnabled(b);
 		mi_dshowsig.setEnabled(b);
 		mi_dexppipe.setEnabled(b);
 		mi_dvhdlcu.setEnabled(b);
 		mi_dvhdlou.setEnabled(b);
 		mi_dvhdltb.setEnabled(b);
 		mi_dvhdlall.setEnabled(b);
 	}
 
 	protected void modeEdit() {
 
 		mode = M_EDIT;
 		setEditMenuEnabled(true);
 		simuButton.setEnabled(true);
 		simuButton.setText(IUI.get("BUTTON_SIMULATE"));
 		resetButton.setEnabled(false);
 		stepButton.setEnabled(false);
 		microStepButton.setEnabled(false);
 		runStopButton.setText("Run");
 		mi_srun.setText("Run");
 		runStopButton.setEnabled(false);
 		breakButton.setEnabled(false);
 		setSimuEnabled(false);
 		if (hasSimObjectsFrame) {
 			soframe.setVisible(false);
 			desktop.remove(soframe);
 			soframe.dispose();
 			soframe = null;
 			hasSimObjectsFrame = false;
 		}
 		unsetStepMark();
 		unsetMicroStepMark();
 		editorArea.setEditable(true);
 		editorFrame.setVisible(true);
 		editorFrame.toFront();
 		breakFrame.revoke();
 		undo = new UndoManager();
 		if (hasEditorFrame) {
 			editorArea.getDocument().addUndoableEditListener(
 					new UndoableEditListener() {
 						public void undoableEditHappened(UndoableEditEvent e) {
 							undo.addEdit(e.getEdit());
 							countUndo++;
 						}
 					});
 			updateNumArea();
 		}
 	}
 
 	protected void modeSimulate() {
 		if (hasSimObjectsFrame) {
 			mode = M_SIMU;
 			setEditMenuEnabled(false);
 			simuButton.setEnabled(true);
 			simuButton.setText(IUI.get("BUTTON_EDIT"));
 			resetButton.setEnabled(true);
 			stepButton.setEnabled(true);
 			microStepButton.setEnabled(true);
 			runStopButton.setEnabled(true);
 			runStopButton.setText("Run");
 			mi_srun.setText("Run");
 			setSimuEnabled(true);
 			breakButton.setEnabled(true);
 			if (hasEditorFrame) {
 				editorArea.setEditable(false);
 			}
 		}
 	}
 
 	private void checkJavaVersion(Frame f) {
 		try {
 			String vs = System.getProperty("java.version", "1.0");
 			String xvs[] = vs.split("\\.");
 			Rectangle r = f.getMaximizedBounds();
 		} catch (NoSuchMethodError nsme) {
 			System.err.println("RTeasy needs Java 1.4 or higher!");
 			exit(1);
 		} catch (Throwable e) {
 			System.err.println(e.getMessage());
 			exit(2);
 		}
 	}
 
 	private JMenu getExamplesMenu() {
 		File files[];
 		BufferedReader r, er;
 		String fsuffix, desc;
 		URL fu, efu;
 		try {
 			JMenu exam_menu = new JMenu(IUI.get("MENU_EXAMPLES"));
 			fu = getClass().getResource("/example/index.ls");
 			r = new BufferedReader(new InputStreamReader(fu.openStream()));
 			fsuffix = r.readLine();
 			while (fsuffix != null) {
 				efu = getClass().getResource("/example/" + fsuffix);
 				er = new BufferedReader(new InputStreamReader(efu.openStream()));
 				desc = er.readLine();
 				er.close();
 				if (desc != null)
 					if (desc.startsWith("#"))
 						desc = desc.substring(1).trim();
 					else
 						desc = fsuffix;
 				else
 					desc = fsuffix;
 				exam_menu.add(makeMenuItem(desc, "x" + fsuffix));
 				fsuffix = r.readLine();
 			}
 			r.close();
 			return exam_menu;
 		} catch (Throwable t) {
 			maintenance(IUI.get("MSG_ON_EXAMPLE_LOADING") + ": "
 					+ t.getMessage());
 			return null;
 		}
 	}
 
 	RTSimWindow() {
 		super("RTeasy");
 		Locale.setDefault(IUI.getLocale());
                 checkJavaVersion(this);
                 
                 try {
                 Properties prop = new Properties();
                 prop.load(RTSimWindow.class.getResourceAsStream("/version.properties"));
                 version = prop.getProperty("rteasy.version");
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
                 
 		RTOptions.loadOptions();
 		IUI.init(RTOptions.locale);
 		RTSimGlobals.init();
 		setIconImage(new ImageIcon(getClass().getResource(ITIICON_URL))
 				.getImage());
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent we) {
 				if (confirmFileClose()) {
 					RTSimWindow.this.exit(0);
 				}
 			}
 		});
 		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		Dimension winDim = getToolkit().getScreenSize();
 		Dimension useDim = new Dimension(winDim);
 		boolean maxBoth = false;
 		// uebergrossen Desktop abfangen
 		// if(winDim.height <= 1024 && winDim.width <= 1280) {
 		// winDim=getToolkit().getScreenSize();
 		// if(getToolkit().isFrameStateSupported(MAXIMIZED_BOTH))
 		// setExtendedState(MAXIMIZED_BOTH);
 		// else setSize(winDim);
 		// setSize(winDim);
 		// }
 		// else {
 		// if(winDim.height > 1024) useDim.height = 980;
 		// if(winDim.width > 1280) useDim.width = 1280;
 		// setSize(useDim);
 		// }
 		setSize(getWindowSize());
 		desktop = new JDesktopPane();
 		desktop.setBackground(new Color(59, 89, 152));
 		mwindow = new MaintenanceWindow(desktop);
 		openLogFrame();
 		RTLog.setWindow(this); // register for logging
 		System.setErr(new LogErrorStream());
 		// RTOptions.dumpOptions();
 		// logLine("Max Window size: "+winDim.width+"x"+winDim.height);
 		// logLine("MAXIMIZED_BOTH: "+maxBoth);
 		fileMenu = new JMenu(IUI.get("MENU_FILE"));
 		mi_save = makeMenuItem(IUI.get("MENU_FILE_SAVE"), "fsave");
 		mi_save.setEnabled(false);
 		mi_save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
 		mi_saveAs = makeMenuItem(IUI.get("MENU_FILE_SAVEAS"), "fsaveas");
 		mi_saveAs.setEnabled(false);
 		mi_saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2,
 				Event.CTRL_MASK));
 		mi_fnew = makeMenuItem(IUI.get("MENU_FILE_NEW"), "fnew");
 		mi_fopen = makeMenuItem(IUI.get("MENU_FILE_OPEN"), "fopen");
 		mi_fopen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
 		mi_fquit = makeMenuItem(IUI.get("MENU_FILE_QUIT"), "fquit");
 		fileMenu.add(mi_fnew);
 		fileMenu.add(mi_fopen);
 		fileMenu.add(mi_save);
 		fileMenu.add(mi_saveAs);
 		fileMenu.add(mi_fquit);
 
 		editMenu = new JMenu(IUI.get("MENU_EDIT"));
 		mi_undo = makeMenuItem(IUI.get("MENU_EDIT_UNDO"), "eundo");
 		mi_undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
 				Event.CTRL_MASK));
 		mi_ecopy = makeMenuItem(IUI.get("MENU_EDIT_COPY"), "ecopy");
 		mi_ecopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
 				Event.CTRL_MASK));
 		mi_ecut = makeMenuItem(IUI.get("MENU_EDIT_CUT"), "ecut");
 		mi_ecut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
 				Event.CTRL_MASK));
 		mi_epaste = makeMenuItem(IUI.get("MENU_EDIT_PASTE"), "epaste");
 		mi_epaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
 				Event.CTRL_MASK));
 		mi_epretty = makeMenuItem(IUI.get("MENU_EDIT_PRETTYPRINT"), "epretty");
 		mi_esettings = makeMenuItem(IUI.get("MENU_EDIT_SETTINGS"), "esettings");
 		editMenu.add(mi_undo);
 		editMenu.add(mi_ecopy);
 		editMenu.add(mi_ecut);
 		editMenu.add(mi_epaste);
 		editMenu.add(mi_epretty);
 		editMenu.add(mi_esettings);
 
 		simuMenu = new JMenu(IUI.get("MENU_SIMULATOR"));
 		mi_sreset = makeMenuItem(IUI.get("MENU_SIMULATOR_RESET"), "sreset");
 		mi_sreset.setEnabled(false);
 		mi_sreset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
 				Event.CTRL_MASK));
 		mi_sstep = makeMenuItem(IUI.get("MENU_SIMULATOR_STEP"), "sstep");
 		mi_sstep.setEnabled(false);
 		mi_sstep.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
 				Event.CTRL_MASK));
 		mi_smicro = makeMenuItem(IUI.get("MENU_SIMULATOR_MICROSTEP"), "smicro");
 		mi_smicro.setEnabled(false);
 		mi_smicro.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
 				Event.CTRL_MASK));
 		mi_srun = makeMenuItem(IUI.get("MENU_SIMULATOR_RUN"), "srun");
 		mi_srun.setEnabled(false);
 		mi_sbreak = makeMenuItem(IUI.get("MENU_SIMULATOR_BREAKPOINTS"),
 				"sbreak");
 		mi_sbreak.setEnabled(false);
 		mi_smemres = makeMenuItem(IUI.get("MENU_SIMULATOR_MEMORY_RESET"),
 				"smemres");
 		mi_smemres.setEnabled(false);
 		mi_slogres = makeMenuItem(IUI.get("MENU_SIMULATOR_LOGCLEAR"), "slogres");
 		mi_scommand = makeMenuItem(IUI.get("MENU_SIMULATOR_COMMAND"),
 				"scommand");
 		// mi_scommand.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,0));
 		simuMenu.add(mi_sreset);
 		simuMenu.add(mi_sstep);
 		simuMenu.add(mi_smicro);
 		simuMenu.add(mi_srun);
 		simuMenu.add(mi_sbreak);
 		simuMenu.add(mi_smemres);
 		simuMenu.add(mi_slogres);
 		simuMenu.add(mi_scommand);
 
 		designMenu = new JMenu(IUI.get("MENU_DESIGN"));
 		mi_dsignals = makeMenuItem(IUI.get("MENU_DESIGN_SIGNALS_INSERT"),
 				"dsignals");
 		mi_dsignals.setEnabled(false);
 		mi_dshowsig = makeMenuItem(IUI.get("MENU_DESIGN_SIGNALS_SHOW"),
 				"dshowsig");
 		mi_dshowsig.setEnabled(false);
 		mi_dexppipe = makeMenuItem(IUI.get("MENU_DESIGN_REAL_STATES"),
 				"dexppipe");
 		mi_dexppipe.setEnabled(false);
 		mi_dvhdlall = makeMenuItem(IUI.get("MENU_DESIGN_VHDL_ALL"), "dvhdlall");
 		mi_dvhdlall.setEnabled(false);
 		mi_dvhdlcu = makeMenuItem(IUI.get("MENU_DESIGN_VHDL_CU"), "dvhdlcu");
 		mi_dvhdlcu.setEnabled(false);
 		mi_dvhdlou = makeMenuItem(IUI.get("MENU_DESIGN_VHDL_OU"), "dvhdlou");
 		mi_dvhdlou.setEnabled(false);
 		mi_dvhdltb = makeMenuItem(IUI.get("MENU_DESIGN_VHDL_TB"), "dvhdltb");
 		mi_dvhdltb.setEnabled(false);
 		// designMenu.add(mi_dsignals);
 		designMenu.add(mi_dshowsig);
 		designMenu.add(mi_dexppipe);
 		designMenu.add(mi_dvhdlall);
 
 		helpMenu = new JMenu(IUI.get("MENU_HELP"));
 		mi_hindex = makeMenuItem(IUI.get("MENU_HELP_INDEX"), "hindex");
 		mi_hindex.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
 		mi_habout = makeMenuItem(IUI.get("MENU_HELP_ABOUT"), "habout");
 		helpMenu.add(mi_hindex);
 		helpMenu.add(mi_habout);
 
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.add(fileMenu);
 		menuBar.add(editMenu);
 		menuBar.add(simuMenu);
 		menuBar.add(designMenu);
 		examMenu = getExamplesMenu();
 		if (examMenu != null)
 			menuBar.add(examMenu);
 		menuBar.add(helpMenu);
 		setJMenuBar(menuBar);
 
 		setEditMenuEnabled(false);
 
 		simuButton = new JButton(IUI.get("BUTTON_SIMULATE"));
 		simuButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				if (mode == M_EDIT)
 					simulate();
 				else if (mode == M_SIMU)
 					modeEdit();
 			}
 		});
 		simuButton.setEnabled(false);
 		resetButton = new JButton(IUI.get("BUTTON_RESET"));
 		resetButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				reset();
 			}
 		});
 		resetButton.setEnabled(false);
 		stepButton = new JButton("Step");
 		stepButton.setToolTipText("Step");
 		stepButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				boolean bk = step();
 			}
 		});
 		stepButton.setEnabled(false);
 		microStepButton = new JButton("MicroStep");
 		microStepButton.setToolTipText("MicroStep");
 		microStepButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				microStep();
 			}
 		});
 		microStepButton.setEnabled(false);
 		runStopButton = new JButton("Run");
 		runStopButton.setToolTipText("Run");
 		runStopButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				runStop();
 			}
 		});
 		runStopButton.setEnabled(false);
 		breakButton = new JButton("Breakpoints");
 		breakButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				breakpoints();
 			}
 		});
 		breakButton.setEnabled(false);
 		JPanel quickbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		quickbar.add(simuButton);
 		quickbar.add(resetButton);
 		quickbar.add(stepButton);
 		quickbar.add(microStepButton);
 		quickbar.add(runStopButton);
 		quickbar.add(breakButton);
 		getContentPane().add(quickbar, BorderLayout.NORTH);
 		getContentPane().add(desktop, BorderLayout.CENTER);
 
 		breakFrame = new BreakpointFrame(desktop);
 		/*
 		 * breakFrame.addInternalFrameListener(new InternalFrameAdapter() {
 		 * public void internalFrameClosing(InternalFrameEvent ife) {
 		 * breakButton.setEnabled(true); setSimuEnabled(true);
 		 * runStopButton.setEnabled(true); resetButton.setEnabled(true);
 		 * simuButton.setEnabled(true); stepButton.setEnabled(true);
 		 * microStepButton.setEnabled(true); } });
 		 */
 		// desktop.add(breakFrame);
 
 	}
 
 	/**
 	 * komplette Neubenennung aller Men&uuml;punkte, Buttons, Fenster
 	 */
 
 	public void updateCaptions() {
 		this.setLocale(IUI.getLocale());
 		fileMenu.setText(IUI.get("MENU_FILE"));
 		mi_fnew.setText(IUI.get("MENU_FILE_NEW"));
 		mi_fopen.setText(IUI.get("MENU_FILE_OPEN"));
 		mi_save.setText(IUI.get("MENU_FILE_SAVE"));
 		mi_saveAs.setText(IUI.get("MENU_FILE_SAVEAS"));
 		mi_fquit.setText(IUI.get("MENU_FILE_QUIT"));
 		editMenu.setText(IUI.get("MENU_EDIT"));
 		mi_undo.setText(IUI.get("MENU_EDIT_UNDO"));
 		mi_ecopy.setText(IUI.get("MENU_EDIT_COPY"));
 		mi_ecut.setText(IUI.get("MENU_EDIT_CUT"));
 		mi_epaste.setText(IUI.get("MENU_EDIT_PASTE"));
 		mi_epretty.setText(IUI.get("MENU_EDIT_PRETTYPRINT"));
 		mi_esettings.setText(IUI.get("MENU_EDIT_SETTINGS"));
 		designMenu.setText(IUI.get("MENU_DESIGN"));
 		mi_dsignals.setText(IUI.get("MENU_DESIGN_SIGNALS_INSERT"));
 		mi_dshowsig.setText(IUI.get("MENU_DESIGN_SIGNALS_SHOW"));
 		mi_dexppipe.setText(IUI.get("MENU_DESIGN_REAL_STATES"));
 		mi_dvhdlcu.setText(IUI.get("MENU_DESIGN_VHDL_CU"));
 		mi_dvhdlou.setText(IUI.get("MENU_DESIGN_VHDL_OU"));
 		mi_dvhdltb.setText(IUI.get("MENU_DESIGN_VHDL_TB"));
 		mi_dvhdlall.setText(IUI.get("MENU_DESIGN_VHDL_ALL"));
 		simuMenu.setText(IUI.get("MENU_SIMULATOR"));
 		mi_sreset.setText(IUI.get("MENU_SIMULATOR_RESET"));
 		mi_sstep.setText(IUI.get("MENU_SIMULATOR_STEP"));
 		mi_smicro.setText(IUI.get("MENU_SIMULATOR_MICROSTEP"));
 		mi_srun.setText(IUI.get("MENU_SIMULATOR_RUN"));
 		mi_slogres.setText(IUI.get("MENU_SIMULATOR_LOGCLEAR"));
 		mi_scommand.setText(IUI.get("MENU_SIMULATOR_COMMAND"));
 		mi_sbreak.setText(IUI.get("MENU_SIMULATOR_BREAKPOINTS"));
 		mi_smemres.setText(IUI.get("MENU_SIMULATOR_MEMORY_RESET"));
 		if (examMenu != null)
 			examMenu.setText(IUI.get("MENU_EXAMPLES"));
 		helpMenu.setText(IUI.get("MENU_HELP"));
 		mi_hindex.setText(IUI.get("MENU_HELP_INDEX"));
 		mi_habout.setText(IUI.get("MENU_HELP_ABOUT"));
 		if (mode == M_EDIT)
 			simuButton.setText(IUI.get("BUTTON_SIMULATE"));
 		else
 			simuButton.setText(IUI.get("BUTTON_EDIT"));
 		breakButton.setText(IUI.get("BUTTON_BREAKPOINTS"));
 		if (hasSimObjectsFrame)
 			soframe.updateCaptions();
 		if (hasLogFrame) {
 			logFrame.setTitle(IUI.get("TITLE_LOG"));
 			logClearButton.setText(IUI.get("BUTTON_CLEAR"));
 		}
 		if (editorFrame != null) {
 			cursorPos.setText(" " + IUI.get("LINE") + " 1, "
 					+ IUI.get("COLUMN") + " 1");
 		}
 		if (settingsFrame != null)
 			settingsFrame.updateCaptions();
 		if (breakFrame != null)
 			breakFrame.updateCaptions();
 		if (helpFrame != null)
 			helpFrame.updateCaptions();
 		if (isShowing())
 			show();
 	}
 
 	private JMenuItem makeMenuItem(String name, String cmd) {
 		JMenuItem m = new JMenuItem(name);
 		m.setActionCommand(cmd);
 		m.addActionListener(this);
 		return m;
 	}
 
 	/**
 	 * hier werden Kommandostrings der GUI in Methodenaufrufe umgewandelt
 	 * 
 	 * @param e
 	 *            ActionEvent der einen g&uuml;tigen, &uuml;ber
 	 *            e.getActionCommand() abrufbaren Kommandostring enth&auml;t
 	 */
 	public void actionPerformed(ActionEvent e) {
 		String command = e.getActionCommand();
 		boolean bk;
 		if (command.equals("epaste"))
 			editorArea.paste();
 		else if (command.equals("eundo"))
 			undoTyping();
 		else if (command.equals("ecopy"))
 			editorArea.copy();
 		else if (command.equals("ecut"))
 			editorArea.cut();
 		else if (command.equals("fquit")) {
 			if (confirmFileClose()) {
 				exit(0);
 			}
 		} else if (command.equals("fnew"))
 			newFile();
 		else if (command.equals("fopen"))
 			loadFile();
 		else if (command.equals("fsave"))
 			saveFile();
 		else if (command.equals("fsaveas"))
 			saveFileAs();
 		else if (command.equals("sreset"))
 			reset();
 		else if (command.equals("sstep"))
 			bk = step();
 		else if (command.equals("smicro"))
 			microStep();
 		else if (command.equals("srun"))
 			runStop();
 		else if (command.equals("sbreak"))
 			breakpoints();
 		else if (command.equals("smemres"))
 			memoryReset();
 		else if (command.equals("slogres"))
 			logArea.setText("");
 		else if (command.equals("scommand"))
 			enterCommand();
 		else if (command.equals("hindex"))
 			helpIndex();
 		else if (command.equals("epretty"))
 			prettyPrint();
 		else if (command.equals("habout"))
 			helpAbout();
 		else if (command.equals("dsignals"))
 			printSignals();
 		else if (command.equals("dshowsig"))
 			showSignals();
 		else if (command.equals("dexppipe"))
 			expandPipeOps();
 		else if (command.equals("dvhdlcu"))
 			showVHDL("cu");
 		else if (command.equals("dvhdlou"))
 			showVHDL("ou");
 		else if (command.equals("dvhdltb"))
 			showVHDL("tb");
 		else if (command.equals("dvhdlall")) {
 			expandPipeOps();
 			showVHDL("all");
 		} else if (command.equals("dumpsimtree"))
 			dumpSimTree();
 		else if (command.equals("esettings"))
 			showSettings();
 		else if (command.startsWith("x"))
 			loadExample(command.substring(1));
 	}
 
 	/**
 	 * Hier wird der Dialog zur Kommandoeingabe erzeugt. Der eingegebene String
 	 * wird ausgelesen.
 	 */
 	private void enterCommand() {
 		String input = JOptionPane.showInternalInputDialog(desktop,
 				IUI.get("DIALOG_COMMAND"), IUI.get("TITLE_COMMAND"),
 				JOptionPane.QUESTION_MESSAGE);
 		if (!checkCommand(input)) {
 			JOptionPane.showInternalMessageDialog(desktop,
 					"Kommando kann momentan nicht ausgeführt werden");
 		}
 	}
 
 	public boolean checkCommand(String command) {
 		boolean bk = false;
 		if (command.equals("epaste")) {
 			editorArea.paste();
 			return true;
 		} else if (command.equals("showstack")) {
 			mwindow.shows();
 			return true;
 		} else if (command.equals("ecopy")) {
 			editorArea.copy();
 			return true;
 		} else if (command.equals("ecut")) {
 			editorArea.cut();
 			return true;
 		} else if (command.equals("fquit")) {
 			if (confirmFileClose()) {
 				exit(0);
 			}
 			return true;
 		} else if (command.equals("fnew")) {
 			newFile();
 			return true;
 		} else if (command.equals("fopen")) {
 			loadFile();
 			return true;
 		} else if (command.equals("fsave")) {
 			saveFile();
 			return true;
 		} else if (command.equals("fsaveas")) {
 			saveFileAs();
 			return true;
 		} else if (command.equals("sreset")) {
 			if (resetButton.isEnabled()) {
 				reset();
 				return true;
 			} else
 				return false;
 		} else if (command.equals("sstep")) {
 			if (stepButton.isEnabled()) {
 				bk = step();
 				return true;
 			} else
 				return false;
 		} else if (command.equals("smicro")) {
 			if (microStepButton.isEnabled()) {
 				microStep();
 				return true;
 			} else
 				return false;
 		} else if (command.equals("srun")) {
 			if (runStopButton.isEnabled()) {
 				runStop();
 				return true;
 			} else
 				return false;
 		} else if (command.equals("sbreak")) {
 			if (breakButton.isEnabled()) {
 				breakpoints();
 				return true;
 			} else
 				return false;
 		} else if (command.equals("smemres")) {
 			if (mi_smemres.isEnabled()) {
 				memoryReset();
 				return true;
 			} else
 				return false;
 		} else if (command.equals("slogres")) {
 			logArea.setText("");
 			return true;
 		} else if (command.equals("hindex")) {
 			helpIndex();
 			return true;
 		} else if (command.equals("epretty")) {
 			if (mi_epretty.isEnabled()) {
 				prettyPrint();
 				return true;
 			} else
 				return false;
 		} else if (command.equals("habout")) {
 			helpAbout();
 			return true;
 		} else if (command.equals("dshowsig")) {
 			if (mi_dshowsig.isEnabled()) {
 				showSignals();
 				return true;
 			}
 			return false;
 		} else if (command.equals("dexppipe")) {
 			if (mi_dexppipe.isEnabled()) {
 				expandPipeOps();
 				return true;
 			} else
 				return false;
 		} else if (command.equals("esettings")) {
 			showSettings();
 			return true;
 		} else {
 			JOptionPane.showInternalMessageDialog(desktop,
 					"Ungültiges Kommando");
 			return true;
 		}
 	}
 
 	/**
 	 * Hier wird das Fenster zur Konfiguration von RTeasy erzeugt.
 	 */
 	private void showSettings() {
 		if (settingsFrame == null)
 			settingsFrame = new SettingsFrame(this);
 		if (!settingsFrame.isShowing()) {
 			settingsFrame.setLocation(150, 50);
 			desktop.add(settingsFrame);
 		}
 		settingsFrame.show();
 	}
 
 	/**
 	 * Hier wird das entsprechende Beispiel geladen.
 	 * 
 	 * @param examName
 	 *            : Der Name des zu ladenden Beispiels
 	 */
 	private void loadExample(String examName) {
 		if (editorAreaModified)
 			if (!confirmFileClose())
 				return;
 		try {
 			if (readFile(getClass().getResource("/example/" + examName),
 					examName))
 				modeEdit();
 		} catch (Throwable t) {
 			logLine(t.getLocalizedMessage());
 		}
 	}
 
 	/**
 	 * Hier wird die Hilfe erzeugt und angezeigt.
 	 */
 	private void helpIndex() {
 		helpFrame = new HelpBrowserFrame(this);
 		desktop.add(helpFrame);
 		helpFrame.setVisible(true);
 		helpFrame.openIndexURL();
 	}
 
 	/**
 	 * Hier wird das Fenster mit den Credits erzeugt.
 	 */
 	private void helpAbout() {
 		URL iconURL = getClass().getResource(ITIICON_URL);
 		String aboutMessage = IUI.get("CONTENT_ABOUT").replaceAll("%%VERSION",
 				version);
 		JOptionPane.showInternalMessageDialog(desktop, aboutMessage,
 				IUI.get("TITLE_ABOUT"), JOptionPane.PLAIN_MESSAGE,
 				new ImageIcon(iconURL));
 	}
 
 	/**
 	 * Hier wird der Men&uuml;punkt "expandPipeOps" durchgeführt.
 	 */
 	private void expandPipeOps() {
 		if (compile()) {
 			rtprog.deriveLowLevelModel();
 			editorArea.setText(rtprog.toString());
 			doubleUndo = countUndo;
 			updateNumArea();
 		}
 	}
 
 	/**
 	 * Hier wird der Men&uuml;punkt "prettyPrint" durchgeführt.
 	 */
 	private void prettyPrint() {
 		if (compile()) {
 			editorArea.setText(rtprog.toString());
 			doubleUndo = countUndo;
 			updateNumArea();
 		}
 	}
 
 	/**
 	 * Hier werden &Auml;nderungen r&uuml;ckg&auml;ngig gemacht.
 	 */
 	private void undoTyping() {
 		try {
 			undo.undo();
 			if (doubleUndo == countUndo) {
 				undo.undo();
 				countUndo--;
 			}
 			countUndo--;
 			updateNumArea();
 		} catch (Exception e) {
 			maintenance(e.getLocalizedMessage());
 		}
 	}
 
 	private void printSignals() {
 		boolean oldCalcSignals = RTOptions.calculateSignals;
 		RTOptions.calculateSignals = true;
 		if (compile()) {
 			// alte Signaleintraege loeschen, die mit ##C beginnen
 			editorArea.setText(editorArea.getText().replaceAll("##C.*$", "\n"));
 		}
 		RTOptions.calculateSignals = oldCalcSignals;
 	}
 
 	private void showVHDL(String kind) {
 		boolean oldCalcSignals = RTOptions.calculateSignals;
 		RTOptions.calculateSignals = true;
 		if (compile()) {
 			Document old_doc = editorArea.getDocument();
 			Document d = old_doc;
 			Hashtable regArHash = rtprog.getRegArrays();
 			String newdecl = "";
 			if (!regArHash.isEmpty()) {
 				for (Enumeration e = regArHash.elements(); e.hasMoreElements();) {
 					RegisterArray ra = (RegisterArray) e.nextElement();
 					newdecl = newdecl + ra.getSingleRegDecl();
 				}
 
 			}
 			StringWriter sw = new StringWriter();
 			PrintWriter pw = new PrintWriter(sw);
 			if (kind.equals("cu"))
 				rtprog.emitAllInOne(pw);
 			else if (kind.equals("ou"))
 				rtprog.emitAllInOne(pw);
 			else if (kind.equals("tb"))
 				rtprog.emitTestBenchFrame(pw);
 			else if (kind.equals("all"))
 				rtprog.emitAllInOne(pw);
 			else
 				pw.println("falscher Parameter fuer showVHDL()");
 			sw.flush();
 			VHDLFrame vhdlFrame = new VHDLFrame(this, sw.toString(), 500, 500,
 					rtprog.getComponentName());
 			desktop.add(vhdlFrame);
 			vhdlFrame.show();
 		}
 		RTOptions.calculateSignals = oldCalcSignals;
 	}
 
 	private void dumpSimTree() {
 		if ((mode == M_SIMU) || compile()) {
 			StringWriter sw = new StringWriter();
 			rtprog.dumpSimTree(new PrintWriter(sw), "");
 			InfoTextFrame f = new InfoTextFrame(this, sw.toString(),
 					"Dump Of Simulation Tree", 500, 500, IUI.get("CLOSE"));
 			desktop.add(f);
 			f.show();
 		}
 	}
 
 	private void showSignals() {
 		boolean oldCalcSignals = RTOptions.calculateSignals;
 		int max;
 		ArrayList el;
 		Expression e;
 		Statement s;
 		Iterator it;
 		RTOptions.calculateSignals = true;
 		if ((mode == M_SIMU) || compile()) {
 			String output = IUI.get("CONDITION_SIGNALS")
 					+ " (I) :\n----------------\n";
 			ArrayList inputSignals = rtprog.getSignalsData().getInputSignals();
 			max = inputSignals.size();
 			for (int i = 0; i < max; i++) {
 				el = (ArrayList) inputSignals.get(i);
 				it = el.listIterator();
 				if (it.hasNext()) {
 					e = (Expression) it.next();
 					output += "I" + Integer.toString(i) + "\t" + e.toString()
 					/* + "\tVHDL: " + e.toVHDL() */+ "\n";
 				}
 			}
 			output += "\n" + IUI.get("CONTROL_SIGNALS")
 					+ " (C) :\n----------------\n";
 			ArrayList controlSignals = rtprog.getSignalsData()
 					.getControlSignals();
 			max = controlSignals.size();
 			for (int i = 0; i < max; i++) {
 				el = (ArrayList) controlSignals.get(i);
 				it = el.listIterator();
 				if (it.hasNext()) {
 					s = (Statement) it.next();
 					output += "C" + Integer.toString(i) + "\t" + s.toString()
 							+ "\n";
 				}
 			}
 			InfoTextFrame signalsFrame = new InfoTextFrame(this, output,
 					IUI.get("TITLE_SIGNALS"), 300, 500, IUI.get("CLOSE"));
 			desktop.add(signalsFrame);
 			signalsFrame.show();
 		}
 		RTOptions.calculateSignals = oldCalcSignals;
 	}
 
 	private void memoryReset() {
 		if (mode == M_SIMU) {
 			rtprog.memoryReset();
 			soframe.simUpdate();
 		}
 	}
 
 	private void openEditorFrame() {
 		editorFrame = new JInternalFrame(IUI.get("UNNAMED"), true, false, true,
 				true);
 		editorFrame.setSize(640, 480);
 		editorFrame.setLocation(0, 0);
 		numberArea = new JTextArea();
 		numberArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
 		numberArea.setBackground(Color.lightGray);
 		numberArea.setEditable(false);
 		numberArea.setColumns(2);
 		editorArea = new JTextArea();
 		editorArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
 		editorAreaListener = new DocumentListener() {
 			public void insertUpdate(DocumentEvent e) {
 				RTSimWindow.this.editorAreaModified = true;
 				if (file != null)
 					RTSimWindow.this.mi_save.setEnabled(true);
 			}
 
 			public void removeUpdate(DocumentEvent e) {
 				RTSimWindow.this.editorAreaModified = true;
 				if (file != null)
 					RTSimWindow.this.mi_save.setEnabled(true);
 			}
 
 			public void changedUpdate(DocumentEvent e) {
 			}
 		};
 		editorArea.getDocument().addDocumentListener(editorAreaListener);
 		numberArea.setText("1");
 		editorArea.addKeyListener(new KeyListener() {
 			public void keyReleased(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 					updateNumArea();
 				} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
 					updateNumArea();
 				} else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
 					updateNumArea();
 				}
 			}
 
 			public void keyTyped(KeyEvent e) {
 			}
 
 			public void keyPressed(KeyEvent e) {
 			}
 		});
 		editorPane = new JPanel();
 		editorPane.setBackground(Color.WHITE);
 		// editorPane.setLayout(new FlowLayout(FlowLayout.LEADING,0,0));
 		editorPane.setLayout(new BoxLayout(editorPane, BoxLayout.LINE_AXIS));
 		editorArea.setMinimumSize(new Dimension(600, 400));
 		numberArea.setMinimumSize(new Dimension(10, 400));
 		numberArea.setMaximumSize(new Dimension(10, 1000));
 		// editorPane.add(numberArea, BorderLayout.WEST);
 		// editorPane.add(editorArea, BorderLayout.CENTER);
 		editorPane.add(numberArea);
 		editorPane.add(editorArea);
 		JScrollPane scrollPane = new JScrollPane(editorPane);
 		// tmp.add(new JScrollPane(editorPane));
 		// editorFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
 		// editorFrame.getContentPane().add(numberArea, BorderLayout.WEST);
 		cursorPos = new JLabel(" " + IUI.get("LINE") + " 1, "
 				+ IUI.get("COLUMN") + " 1");
 		editorFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
 		editorFrame.getContentPane().add(cursorPos, BorderLayout.SOUTH);
 		// tmp.add(scrollPane, BorderLayout.WEST);
 		// tmp.add(cursorPos,BorderLayout.SOUTH);
 		editorArea.addCaretListener(new CaretListener() {
 			public void caretUpdate(CaretEvent ce) {
 				Element root = editorArea.getDocument().getDefaultRootElement();
 				int dot = ce.getDot();
 				int line = root.getElementIndex(dot);
 				int col = dot - root.getElement(line).getStartOffset() + 1;
 				line++;
 				cursorPos.setText(" " + IUI.get("LINE") + " " + line + ", "
 						+ IUI.get("COLUMN") + " " + col);
 			}
 		});
 		JScrollBar bar = scrollPane.getVerticalScrollBar();
 		bar.setUnitIncrement(10);
 		/*
 		 * Action al[] = editorArea.getActions(); for(int
 		 * ai=0;ai<al.length;ai++) System.err.println(al[ai]); Keymap km =
 		 * editorArea.getKeymap(); System.err.println(km); //
 		 * Emacs-Tastenkombinationen // Bewegungen
 		 * keymapAddAlias(km,KeyEvent.VK_LEFT
 		 * ,0,KeyEvent.VK_B,InputEvent.CTRL_MASK);
 		 * keymapAddAlias(km,KeyEvent.VK_RIGHT
 		 * ,0,KeyEvent.VK_F,InputEvent.CTRL_MASK);
 		 * keymapAddAlias(km,KeyEvent.VK_UP
 		 * ,0,KeyEvent.VK_P,InputEvent.CTRL_MASK);
 		 * keymapAddAlias(km,KeyEvent.VK_DOWN
 		 * ,0,KeyEvent.VK_N,InputEvent.CTRL_MASK);
 		 * keymapAddAlias(km,KeyEvent.VK_PAGE_UP
 		 * ,0,KeyEvent.VK_V,InputEvent.META_MASK);
 		 * keymapAddAlias(km,KeyEvent.VK_PAGE_DOWN
 		 * ,0,KeyEvent.VK_V,InputEvent.CTRL_MASK);
 		 * keymapAddAlias(km,KeyEvent.VK_LEFT,InputEvent.CTRL_MASK,
 		 * KeyEvent.VK_B,InputEvent.META_MASK);
 		 * keymapAddAlias(km,KeyEvent.VK_RIGHT,InputEvent.CTRL_MASK,
 		 * KeyEvent.VK_F,InputEvent.META_MASK);
 		 * keymapAddAlias(km,KeyEvent.VK_HOME,0,KeyEvent.VK_A,
 		 * InputEvent.CTRL_MASK); keymapAddAlias(km,KeyEvent.VK_END,0,
 		 * KeyEvent.VK_E,InputEvent.CTRL_MASK);
 		 * keymapAddAlias(km,KeyEvent.VK_HOME,InputEvent.CTRL_MASK,
 		 * KeyEvent.VK_LESS,InputEvent.CTRL_MASK);
 		 * keymapAddAlias(km,KeyEvent.VK_END,InputEvent.CTRL_MASK,
 		 * KeyEvent.VK_GREATER,InputEvent.CTRL_MASK);
 		 */
 		breakFrame.setEditorArea(editorArea);
 		desktop.add(editorFrame);
 		hasEditorFrame = true;
 		//editorArea.setText(" ");
 		editorArea.setText("");
 		modeEdit();
 	}
 
 	private void keymapAddAlias(Keymap km, int origEvent, int origMask,
 			int aliasEvent, int aliasMask) {
 		KeyStroke orig = KeyStroke.getKeyStroke(origEvent, origMask);
 		System.err.println(orig);
 		KeyStroke alias = KeyStroke.getKeyStroke(aliasEvent, aliasMask);
 		System.err.println(alias);
 		Action a = km.getAction(alias);
 		System.err.println(a);
 		km.addActionForKeyStroke(orig, km.getAction(alias));
 	}
 
 	public JFileChooser getFileChooser() {
 		JFileChooser chooser;
 		File workDir = null;
 		try {
 			if (file != null)
 				workDir = file.getParentFile();
 			if (workDir == null)
 				workDir = new File(System.getProperty("user.dir"));
 			chooser = new JFileChooser(workDir);
 		} catch (Throwable t) {
 			chooser = new JFileChooser();
 		}
 		return chooser;
 	}
 
 	private boolean readFile(URL url, String name) {
 		try {
 			if (!hasEditorFrame)
 				openEditorFrame();
 			editorFrame.setVisible(true);
 			editorArea.read(new InputStreamReader(url.openStream()), null);
 			if (checkHeader(name)) {
 				editorArea.setText(editorArea.getText().substring(49));
 				editorFrame.setTitle(IUI.get("EXAMPLE") + ": " + name);
 				editorFrame.toFront();
 				editorArea.getDocument()
 						.addDocumentListener(editorAreaListener);
 				mi_save.setEnabled(false);
 				mi_saveAs.setEnabled(true);
 				editorAreaModified = false;
 				breakFrame.resetBreakpoints();
 			}
 			return true;
 		} catch (Throwable t) {
 			logLine(t.getLocalizedMessage());
 			return false;
 		}
 	}
 
 	private boolean checkHeader(String name) {
 		boolean b = false;
 		String s = editorArea.getText();
 		if (s.substring(0, 41).equals(
 				"% University of Luebeck - RTeasy Version ")) {
 			b = true;
 			if (!s.substring(41, 46).equals(version)) {
 				b = false;
 			}
 		}
 		if (!b) {
 			String msg = IUI.get("MSG_LOAD_WARNING")
 					.replace("%%FILENAME", name);
 			JOptionPane.showMessageDialog(editorFrame, msg);
 		}
 		return b;
 	}
 
 	private boolean readFile(File f) {
 		try {
 			file = f;
 			if (!hasEditorFrame)
 				openEditorFrame();
 			editorFrame.setVisible(true);
 			FileReader fr = new FileReader(file);
 			editorArea.read(fr, null);
 			fr.close();
 			if (checkHeader(f.toString())) {
 				editorArea.setText(editorArea.getText().substring(49));
 			}
 			editorFrame.setTitle(file.toString());
 			editorFrame.toFront();
 			editorArea.getDocument().addDocumentListener(editorAreaListener);
 			mi_save.setEnabled(false);
 			mi_saveAs.setEnabled(true);
 			editorAreaModified = false;
 			breakFrame.resetBreakpoints();
 			return true;
 		} catch (Throwable t) {
 			logLine(t.getLocalizedMessage());
 			return false;
 		}
 	}
 
 	public void loadFile() {
 		if (editorAreaModified)
 			if (!confirmFileClose())
 				return;
 		JFileChooser chooser = getFileChooser();
 		chooser.setFileFilter(fileFilter);
 		int result = chooser.showOpenDialog(this);
 		if (result == JFileChooser.CANCEL_OPTION)
 			return;
 		if (readFile(chooser.getSelectedFile())) {
 
 			modeEdit();
 			logArea.setText("");
 		}
 	}
 
 	public String replaceLineSeps(String s) {
 		String lineSep = System.getProperty("line.separator");
 		s.replaceAll("[^.]", lineSep);
 		return s;
 	}
 
 	public void saveFile() {
 		if (file == null) {
 			saveFileAs();
 			return;
 		}
 		try {
 			FileWriter fw = new FileWriter(file);
 			fw.write("% University of Luebeck - RTeasy Version " + version
 					+ "% \n");
 			fw.write(replaceLineSeps(editorArea.getText()));
 			fw.close();
 			String name = file.toString();
 			String msg = IUI.get("MSG_SAVE_SUCCESS")
 					.replace("%%FILENAME", name);
 			logLine(msg);
 			editorAreaModified = false;
 			mi_save.setEnabled(false);
 
 		} catch (Throwable t) {
 			logLine(t.getLocalizedMessage());
 		}
 	}
 
 	public void saveFileAs() {
 		JFileChooser chooser = getFileChooser();
 		chooser.setFileFilter(fileFilter);
 		int result = chooser.showSaveDialog(this);
 		if (result == JFileChooser.CANCEL_OPTION)
 			return;
 		try {
 			file = chooser.getSelectedFile();
 			file = new File(chompRtFilename(file.getCanonicalPath() + ".rt"));
 			FileWriter fw = new FileWriter(file);
 			fw.write("% University of Luebeck - RTeasy Version 0.3.3 % \n");
 			fw.write(replaceLineSeps(editorArea.getText()));
 			fw.close();
 			editorFrame.setTitle(file.toString());
 			String name = file.toString();
 			String msg = IUI.get("MSG_SAVE_SUCCESS")
 					.replace("%%FILENAME", name);
 			logLine(msg);
 			mi_save.setEnabled(false);
 			editorAreaModified = false;
 		} catch (Throwable t) {
 			logLine(t.getLocalizedMessage());
 		}
 	}
 
 	public void newFile() {
 		if (editorAreaModified)
 			if (!confirmFileClose())
 				return;
 		if (!hasEditorFrame)
 			openEditorFrame();
 		else
 			editorArea.setText("");
 		logArea.setText("");
 		editorFrame.setVisible(true);
 		editorFrame.toFront();
 		editorFrame.setTitle(IUI.get("UNNAMED"));
 		editorArea.getDocument().addDocumentListener(editorAreaListener);
 		mi_saveAs.setEnabled(true);
 		mi_save.setEnabled(false);
 		editorAreaModified = false;
 		file = null;
 		modeEdit();
 	}
 
 	public void openLogFrame() {
 		logFrame = new JInternalFrame(IUI.get("TITLE_LOG"), true, false, true,
 				true);
		logFrame.setSize(500, 100);
		logFrame.setLocation(0, 0);
 		logArea = new JTextArea();
 		logArea.setLineWrap(true);
 		logArea.setWrapStyleWord(true);
 		logArea.setEditable(false);
 		logScrollPane = new JScrollPane(logArea);
 		logFrame.getContentPane().add(logScrollPane, BorderLayout.CENTER);
 		JPanel panel = new JPanel();
 		logClearButton = new JButton(IUI.get("BUTTON_CLEAR"));
 		logClearButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				RTSimWindow.this.logArea.setText("");
 			}
 		});
 		panel.add(logClearButton);
 		logFrame.getContentPane().add(panel, BorderLayout.SOUTH);
 		desktop.add(logFrame);
 		logFrame.setVisible(true);
 		hasLogFrame = true;
 	}
 
 	public boolean compile() {
 		logFrame.setVisible(true);
 		logFrame.toFront();
 		try {
 			Document d = editorArea.getDocument();
 			RTSim_Parser parser = new RTSim_Parser(new StringReader(d.getText(
 					0, d.getLength())));
 			ASTRtProg rn = parser.parseRTProgram();
 			if (parser.hasSyntaxError()) {
 				logLine(parser.getSyntaxErrorMessage());
 				return false;
 			}
 			// entkommentieren, um ParseTree zu sehen
 			//rn.dump("");
 			RTSim_SemAna semAna = new RTSim_SemAna();
 			int errorCount = semAna.checkRTProgram(rn);
 			if (errorCount > 0) {
 				LinkedList errorMsg = semAna.getErrorMessages();
 				LinkedList errorPos = semAna.getErrorPositions();
 				ListIterator msgIt = errorMsg.listIterator(0);
 				ListIterator posIt = errorPos.listIterator(0);
 				while (msgIt.hasNext() && posIt.hasNext())
 					logLine(IUI.get("TITLE_ERROR") + ": "
 							+ ((PositionRange) posIt.next()).toString() + ": "
 							+ ((String) msgIt.next()));
 			}
 			if (semAna.hasWarnings()) {
 				LinkedList warnMsg = semAna.getWarningMessages();
 				LinkedList warnPos = semAna.getWarningPositions();
 				ListIterator msgIt = warnMsg.listIterator(0);
 				ListIterator posIt = warnPos.listIterator(0);
 				while (msgIt.hasNext() && posIt.hasNext())
 					logLine("Warnung: "
 							+ ((PositionRange) posIt.next()).toString() + ": "
 							+ ((String) msgIt.next()));
 			}
 			if (errorCount > 0)
 				return false;
 			rtprog = new RTProgram(rn, semAna);
 			if (hasSimObjectsFrame)
 				soframe.setNewData(semAna.getRegisterOrder(),
 						semAna.getBusOrder(), semAna.getMemoryOrder(),
 						semAna.getRegArrayOrder());
 			else
 				createSimObjectsFrame(semAna.getRegisterOrder(),
 						semAna.getBusOrder(), semAna.getMemoryOrder(),
 						semAna.getRegArrayOrder());
 			breakFrame.setProgram(rtprog);
 		} catch (Throwable e) {
 			String msg = e.getLocalizedMessage();
 			if (msg == null) {
 				maintenance(IUI.get("INTERNAL_ERROR_STACK_TRACE"));
 				try {
 					StackTraceElement[] stackTrace = e.getStackTrace();
 					for (int i = 0; i < stackTrace.length; i++)
 						maintenance(stackTrace[i].toString());
 				} catch (Throwable t) {
 					// e.g. JRE < 1.4 does not support stack traces
 					maintenance(IUI.get("INTERNAL_ERROR_NO_STACK_TRACE"));
 				}
 			} else
 				logLine(msg);
 			return false;
 		}
 		return true;
 	}
 
 	public void simulate() {
 		if (compile()) {
 			logLine(IUI.get("MSG_COMPILE_SUCCESS"));
 			soframe.setVisible(true);
 			modeSimulate();
 		}
 	}
 
 	private void createSimObjectsFrame(LinkedList registerOrder,
 			LinkedList busOrder, LinkedList memoryOrder, LinkedList regArrOrder) {
 		soframe = new SimObjectsFrame(desktop, registerOrder, busOrder,
 				memoryOrder, regArrOrder);
 		soframe.setLocation(680, 10);
 		desktop.add(soframe);
 		hasSimObjectsFrame = true;
 	}
 
 	public void logLine(String s) {
 		logArea.append("\n" + s);
 		logArea.setCaretPosition(logArea.getDocument().getLength());
 	}
 
 	public void maintenance(String s) {
 		mwindow.set(s);
 		mwindow.text.setCaretPosition(mwindow.text.getDocument().getLength());
 	}
 
 	public void runStop() {
 		if (runStopButton.getToolTipText().equals("Run")) {
 			runStopButton.setToolTipText("Stop");
 			runStopButton.setText("Stop");
 			mi_srun.setText("Stop");
 			runThread = new Thread(runner);
 			runThread.start();
 		} else {
 			runner.setRunPermission(false);
 		}
 	}
 
 	public void breakpoints() {
 		/*
 		 * simuButton.setEnabled(false); breakButton.setEnabled(false);
 		 * resetButton.setEnabled(false); runStopButton.setEnabled(false);
 		 * stepButton.setEnabled(false); microStepButton.setEnabled(false);
 		 * setSimuEnabled(false);
 		 */
 		breakFrame.invoke();
 	}
 
 	private void logTermination() {
 		logLine(IUI.get("MSG_SIMULATION_TERMINATED"));
 	}
 
 	public boolean step() {
 		if (rtprog.terminated()) {
 			logTermination();
 			return false;
 		}
 		boolean bk = true;
 		int smmod = 1;
 		unsetMicroStepMark();
 		if (breakFrame.getBreakpoints().contains(
 				new Integer(rtprog.getStatSeqIndex()))) {
 			smmod = 2;
 			bk = false;
 		}
 		PositionRange tpr = rtprog.getCurrentPositionRange();
 		if (!rtprog.step())
 			maintenance(rtprog.getErrorMessage());
 		if (rtprog.terminated()) {
 			logTermination();
 			bk = false;
 		}
 		setStepMark(tpr, smmod);
 		soframe.setVirtualPC(rtprog.getStatSeqIndex());
 		soframe.setCycleCount(rtprog.getCycleCount());
 		soframe.simUpdate();
 		return bk;
 	}
 
 	public void microStep() {
 		if (rtprog.terminated()) {
 			logTermination();
 			return;
 		}
 		PositionRange tpr = rtprog.getCurrentPositionRange();
 		unsetStepMark();
 		if (!rtprog.microStep())
 			maintenance(rtprog.getErrorMessage());
 		if (rtprog.terminated())
 			logTermination();
 		Statement st = rtprog.getCurrentStatement();
 		if (st != null)
 			tpr = st.getPositionRange();
 		setMicroStepMark(tpr, st.getStatementType(), st);
 		soframe.setVirtualPC(rtprog.getStatSeqIndex());
 		soframe.setCycleCount(rtprog.getCycleCount());
 		soframe.simUpdate();
 	}
 
 	public void appendCycleLog() {
 		logLine(rtprog.getCycleCount() + ": " + IUI.get("STATE") + "="
 				+ rtprog.getStatSeqIndex());
 	}
 
 	public void reset() {
 		unsetStepMark();
 		unsetMicroStepMark();
 		rtprog.reset();
 		soframe.setVirtualPC(rtprog.getStatSeqIndex());
 		soframe.setCycleCount(rtprog.getCycleCount());
 		soframe.simUpdate();
 	}
 
 	private void unsetMicroStepMark() {
 		if (!hasMicroStepMark)
 			return;
 		editorArea.getHighlighter().removeHighlight(microStepHighlight);
 		if (secMicroStepHighlight != null) {
 			editorArea.getHighlighter().removeHighlight(secMicroStepHighlight);
 		}
 		microStepHighlight = null;
 		hasMicroStepMark = false;
 	}
 
 	private void setMicroStepMark(PositionRange pr, int statementType,
 			Statement st) {
 		Element root = editorArea.getDocument().getDefaultRootElement();
 		int begin = root.getElement(pr.beginLine - 1).getStartOffset()
 				+ pr.beginColumn - 1;
 		int end = root.getElement(pr.endLine - 1).getStartOffset()
 				+ pr.endColumn;
 		int begincase = 0;
 		int endcase = 0;
 		Color hcolor = Color.YELLOW;
 		if (statementType == RTSimGlobals.IFBAILOUT
 				|| statementType == RTSimGlobals.SWITCHBAILOUT) {
 			if (st.getIfExpr()) {
 				hcolor = Color.GREEN;
 			} else {
 				hcolor = Color.RED;
 			}
 			if (statementType == RTSimGlobals.SWITCHBAILOUT) {
 				hcolor = Color.yellow;
 				PositionRange cpr = st.getCasePosition();
 				begincase = root.getElement(cpr.beginLine - 1).getStartOffset()
 						+ cpr.beginColumn - 1;
 				endcase = root.getElement(cpr.endLine - 1).getStartOffset()
 						+ cpr.endColumn;
 			}
 		}
 		try {
 			editorArea.scrollRectToVisible(editorArea.modelToView(begin));
 			if (hasMicroStepMark) {
 				editorArea.getHighlighter().removeHighlight(microStepHighlight);
 				if (secMicroStepHighlight != null) {
 					editorArea.getHighlighter().removeHighlight(
 							secMicroStepHighlight);
 				}
 			}
 			microStepHighlight = editorArea.getHighlighter().addHighlight(
 					begin, end,
 					new DefaultHighlighter.DefaultHighlightPainter(hcolor));
 			if (statementType == RTSimGlobals.SWITCHBAILOUT) {
 				secMicroStepHighlight = editorArea.getHighlighter()
 						.addHighlight(
 								begincase,
 								endcase,
 								new DefaultHighlighter.DefaultHighlightPainter(
 										hcolor));
 			}
 			hasMicroStepMark = true;
 		} catch (Throwable t) {
 			maintenance(t.getLocalizedMessage());
 		}
 		editorFrame.repaint();
 	}
 
 	private void unsetStepMark() {
 		if (highlight == null)
 			return;
 		editorArea.getHighlighter().removeHighlight(highlight);
 		highlight = null;
 	}
 
 	private void setStepMark(PositionRange pr, int mod) {
 		unsetStepMark();
 		Element root = editorArea.getDocument().getDefaultRootElement();
 		stepMarkBegin = root.getElement(pr.beginLine - 1).getStartOffset()
 				+ pr.beginColumn - 1;
 		stepMarkEnd = root.getElement(pr.endLine - 1).getStartOffset()
 				+ pr.endColumn;
 		Color col;
 		if (mod == 1)
 			col = Color.CYAN;
 		else if (mod == 2)
 			col = Color.RED;
 		else
 			return;
 		try {
 			editorArea.scrollRectToVisible(editorArea
 					.modelToView(stepMarkBegin));
 			highlight = editorArea.getHighlighter().addHighlight(stepMarkBegin,
 					stepMarkEnd,
 					new DefaultHighlighter.DefaultHighlightPainter(col));
 		} catch (Throwable t) {
 			maintenance(t.getLocalizedMessage());
 		}
 		editorFrame.repaint();
 	}
 
 	private boolean confirmFileClose() {
 		try {
 			String fname;
 			String dialogq = IUI.get("DIALOG_SAVE_CHANGES");
 			if (file == null)
 				fname = "\"" + IUI.get("UNKNOWN") + "\"";
 			else
 				fname = "\"" + file.toString() + "\"";
 			dialogq = dialogq.replace("%%FILENAME", fname);
 			int b = JOptionPane.showInternalConfirmDialog(editorFrame, dialogq,
 					IUI.get("TITLE_SAVE_CHANGES"),
 					JOptionPane.YES_NO_CANCEL_OPTION,
 					JOptionPane.QUESTION_MESSAGE);
 			switch (b) {
 			case JOptionPane.CLOSED_OPTION:
 			case JOptionPane.CANCEL_OPTION:
 				return false;
 			case JOptionPane.YES_OPTION:
 				if (file == null)
 					saveFileAs();
 				else
 					saveFile();
 			}
 		} catch (Exception e) {
 		}
 		return true;
 	}
 
 	/**
 	 * Hier wird die optimale Fenstergr&ouml;&szlig;e ermittelt.
 	 * 
 	 * @return Die optimale Position und Gr&ouml;&szlig;e des Hauptfensters
 	 */
 	public Dimension getWindowSize() {
 		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
 		Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
 				.getMaximumWindowBounds();
 		Insets screenInsets = new Insets((int) maxBounds.getY(),
 				(int) maxBounds.getX(), (int) (screenDim.getHeight()
 						- maxBounds.getY() - maxBounds.getHeight()),
 				(int) (screenDim.getWidth() - maxBounds.getWidth() - maxBounds
 						.getX()));
 		Dimension screenDimView = new Dimension(
 				(int) (screenDim.getWidth() - screenInsets.right - screenInsets.left),
 				(int) (screenDim.getHeight() - screenInsets.top - screenInsets.bottom));
 		return screenDimView;
 	}
 
 	/**
 	 * Die Methode passt die numerierte Seitenleiste des Editors beim Laden oder
 	 * ändern von neuen Dateien an.
 	 */
 	public void updateNumArea() {
 		final int docSize = editorArea.getLineCount();
 		numberArea.setText("");
 		for (int i = 1; i < docSize; i++) {
 			if (i < 10) {
 				numberArea.append(i + "\n");
 			} else {
 				numberArea.append(i + "\n");
 			}
 		}
 		numberArea.append("" + docSize);
 		numberArea.scrollRectToVisible(editorArea.getVisibleRect());
 	}
 
 	public abstract void exit(int exit_code);
 
 }
