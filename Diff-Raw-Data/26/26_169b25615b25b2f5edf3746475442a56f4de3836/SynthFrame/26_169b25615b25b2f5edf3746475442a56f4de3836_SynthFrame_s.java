 package fr.istic.synthlab;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.KeyStroke;
 
 import com.alee.extended.statusbar.WebMemoryBar;
 import com.alee.extended.statusbar.WebStatusBar;
 import com.alee.laf.StyleConstants;
 import com.alee.laf.button.WebButton;
 import com.alee.laf.colorchooser.WebColorChooserDialog;
 import com.alee.laf.toolbar.ToolbarStyle;
 import com.alee.laf.toolbar.WebToolBar;
 import com.alee.utils.ImageUtils;
 
 import fr.istic.synthlab.command.ICommand;
 import fr.istic.synthlab.command.menu.AddModuleAudioScopeCommand;
 import fr.istic.synthlab.command.menu.AddModuleMIXCommand;
 import fr.istic.synthlab.command.menu.AddModuleREPCommand;
 import fr.istic.synthlab.command.toolbar.ToolbarCurrentWireColorCommand;
 import fr.istic.synthlab.controller.synthesizer.ICSynthesizer;
 import fr.istic.synthlab.presentation.synthesizer.IPSynthesizer;
 import fr.istic.synthlab.presentation.synthesizer.PSynthesizer;
 
 public class SynthFrame extends JFrame implements ISynthFrame {
 
 	private static final long serialVersionUID = -7577358239451859975L;
 
 	private IPSynthesizer pres;
 
 	// Menu
 	private JMenuBar mainMenuBar;
 	private JMenu menuFile, menuAdd, menuHelp, menuWindow;
 	private JMenuItem menuItemNew, menuItemOpen, menuItemSave, menuItemSaveAs;
 	private JMenuItem menuItemQuit, menuItemDoc, menuItemAbout;
 	private JMenuItem menuItemAddModuleVCO, menuItemAddModuleOUT, menuItemAddModuleVCFLP, menuItemAddModuleVCFHP, menuItemAddModuleEG,
 			menuItemAddModuleAudioScope, menuItemAddModuleREP, menuItemAddModuleVCA, menuItemAddModuleMIX;
 	private List<JMenuItem> files;
 	private ButtonGroup filesGroup = new ButtonGroup();
 	
 	// Toolbar
 	private WebToolBar toolBar = new WebToolBar();
 	private String[] iconFiles = { "res/play.png", "res/stop.png" };
 	private String buttonPlayPauseLabel = "Play/Stop";
 	private JButton buttonPlayPause = new JButton();
 	private JButton buttonOUT, buttonVCO, buttonVCA, buttonVCFLP, buttonVCFHP;
 	private JButton buttonEG, buttonMIX, buttonREP, buttonSCOP;
 	private WebButton colorButtonBlack, colorButtonGray, colorButtonBlue, colorButtonCyan, colorButtonGreen, colorButtonMagenta, colorButtonOrange,
 			colorButtonPink, colorButtonRed, colorButtonWhite, colorButtonYellow, colorChooserButton;
 	private String[] tooltipTexts = { "Voltage-Controlled Oscillator", "Voltage-Controlled Amplifier", "Voltage-Controlled Filter Low-Pass",
 			"Voltage-Controlled Filter High-Pass", "Enveloppe generator", "Mixer", "Replicator", "Output on soundcard", "AudioScope" };
 
 	// Command
 	private ICommand newSynthCommand;
 	private ICommand saveSynthCommand;
 	private ICommand saveAsSynthCommand;
 	private ICommand openSynthCommand;
 	private ICommand quitSynthCommand;
 	private ICommand docSynthCommand;
 	private ICommand aboutSynthCommand;
 	private ICommand addModuleOUTCommand;
 	private ICommand addModuleVCOCommand;
 	private ICommand addModuleVCACommand;
 	private ICommand addModuleVCFLPCommand;
 	private ICommand addModuleVCFHPCommand;
 	private ICommand addModuleEGCommand;
 	private ICommand addModuleAudioScopeCommand;
 	private ICommand addModuleREPCommand;
 	private ICommand addModuleMIXCommand;
 	private ICommand toolbarPlayCommand;
 	private ICommand toolbarPauseCommand;
 	private ICommand toolbarCurrentWireColorCommand;
 
 	private Color toolbarCurrentWireColor;
 	private boolean isPlaying = true;
 	private ISynthApp app;
 
 	public SynthFrame(ISynthApp app) {
 		this.app = app;
 		this.initComponents();
 		this.configureView();
 		this.defineCallbacks();
 	}
 
 	/**
 	 * Initialize the frame's components
 	 */
 	private void initComponents() {
 		files = new ArrayList<JMenuItem>();
 		// instanciation de la barre de menu
 
 		mainMenuBar = new JMenuBar();
 
 		// -------------------------- File Menu --------------------------
 		menuFile = new JMenu("File");
 
 		// this.add(new UsageDisplay());
 		// instanciation des items
 		menuItemNew = new JMenuItem("New");
 		menuItemOpen = new JMenuItem("Open...");
 		menuItemSave = new JMenuItem("Save");
 		menuItemSaveAs = new JMenuItem("Save as...");
 		menuItemQuit = new JMenuItem("Quit");
 
 		// ajout des items au menu File
 		menuFile.add(menuItemNew);
 		menuFile.add(menuItemOpen);
 		menuFile.add(menuItemSave);
 		menuFile.add(menuItemSaveAs);
 		menuFile.add(menuItemQuit);
 
 		// -------------------------- Add Menu --------------------------
 		menuAdd = new JMenu("Add module");
 
 		menuItemAddModuleVCO = new JMenuItem("VCO    | " + tooltipTexts[0]);
 		menuItemAddModuleVCA = new JMenuItem("VCA    | " + tooltipTexts[1]);
 		menuItemAddModuleVCFLP = new JMenuItem("VCF-LP | " + tooltipTexts[2]);
 		menuItemAddModuleVCFHP = new JMenuItem("VCF-HP | " + tooltipTexts[3]);
 		menuItemAddModuleEG = new JMenuItem("EG     | " + tooltipTexts[4]);
 		menuItemAddModuleMIX = new JMenuItem("MIX    | " + tooltipTexts[5]);
 		menuItemAddModuleREP = new JMenuItem("REP    | " + tooltipTexts[6]);
 		menuItemAddModuleAudioScope = new JMenuItem("SCOP   | " + tooltipTexts[8]);
 		menuItemAddModuleOUT = new JMenuItem("OUT    | " + tooltipTexts[7]);
 
 		Font font = new Font(Font.MONOSPACED, Font.ROMAN_BASELINE, 11);
 		menuItemAddModuleVCO.setFont(font);
 		menuItemAddModuleVCA.setFont(font);
 		menuItemAddModuleVCFLP.setFont(font);
 		menuItemAddModuleVCFHP.setFont(font);
 		menuItemAddModuleEG.setFont(font);
 		menuItemAddModuleMIX.setFont(font);
 		menuItemAddModuleREP.setFont(font);
 		menuItemAddModuleOUT.setFont(font);
 		menuItemAddModuleAudioScope.setFont(font);
 
 		menuAdd.add(menuItemAddModuleVCO);
 		menuAdd.add(menuItemAddModuleVCA);
 		menuAdd.add(menuItemAddModuleVCFLP);
 		menuAdd.add(menuItemAddModuleVCFHP);
 		menuAdd.add(menuItemAddModuleEG);
 		menuAdd.add(menuItemAddModuleMIX);
 		menuAdd.add(menuItemAddModuleREP);
 		menuAdd.add(menuItemAddModuleOUT);
 		menuAdd.add(menuItemAddModuleAudioScope);
 		
 		// -------------------------- File Menu --------------------------
 		menuWindow = new JMenu("Window");
 
 		// -------------------------- Help Menu --------------------------
 		menuHelp = new JMenu("Help");
 
 		// instanciation des items
 		menuItemDoc = new JMenuItem("Documentation");
 		menuItemAbout = new JMenuItem("About");
 
 		// ajout des items au menu Help
 		menuHelp.add(menuItemDoc);
 		menuHelp.add(menuItemAbout);
 
 		// ajout des menus a la barre de menus
 		mainMenuBar.add(menuFile);
 		mainMenuBar.add(menuAdd);
 		mainMenuBar.add(menuWindow);
 		mainMenuBar.add(menuHelp);
 		this.setJMenuBar(mainMenuBar);
 
 		// instanciation des boutons de la toolbar
 		toolBar.addSpacing(7);
 		toolBar.setToolbarStyle(ToolbarStyle.attached);
 		Container frameContainer = getContentPane();
 		frameContainer.setLayout(new BorderLayout());
 
 		Image img;
 		try {
 			img = ImageIO.read(new File(iconFiles[1]));
 			img = img.getScaledInstance(25, 25, Image.SCALE_DEFAULT);
 			buttonPlayPause = new JButton(new ImageIcon(img));
 			buttonPlayPause.setToolTipText(buttonPlayPauseLabel);
 			toolBar.add(buttonPlayPause);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 		toolBar.addSpacing(3);
 		toolBar.addSeparator();
 		toolBar.addSpacing(7);
 
 		// -------------------------- Toolbar Modules --------------------------
 
 		buttonVCO = new JButton("VCO");
 		buttonVCA = new JButton("VCA");
 		buttonVCFLP = new JButton("VCF-LP");
 		buttonVCFHP = new JButton("VCF-HP");
 		buttonEG = new JButton("EG");
 		buttonMIX = new JButton("MIX");
 		buttonREP = new JButton("REP");
 		buttonOUT = new JButton("OUT");
 		buttonSCOP = new JButton("SCOP");
 
 		buttonVCO.setToolTipText(tooltipTexts[0]);
 		buttonVCA.setToolTipText(tooltipTexts[1]);
 		buttonVCFLP.setToolTipText(tooltipTexts[2]);
 		buttonVCFHP.setToolTipText(tooltipTexts[3]);
 		buttonEG.setToolTipText(tooltipTexts[4]);
 		buttonMIX.setToolTipText(tooltipTexts[5]);
 		buttonREP.setToolTipText(tooltipTexts[6]);
 		buttonOUT.setToolTipText(tooltipTexts[7]);
 		buttonSCOP.setToolTipText(tooltipTexts[8]);
 
 		toolBar.add(buttonVCO);
 		toolBar.add(buttonVCA);
 		toolBar.add(buttonVCFLP);
 		toolBar.add(buttonVCFHP);
 		toolBar.add(buttonEG);
 		toolBar.add(buttonMIX);
 		toolBar.add(buttonREP);
 		toolBar.add(buttonOUT);
 		toolBar.add(buttonSCOP);
 
 		toolBar.addSpacing(3);
 		toolBar.addSeparator();
 		toolBar.addSpacing(7);
 
 		// -------------------------- Toolbar Colors --------------------------
 		colorButtonBlack = new WebButton(ImageUtils.createColorIcon(Color.BLACK));
 		colorButtonGray = new WebButton(ImageUtils.createColorIcon(Color.GRAY));
 		colorButtonBlue = new WebButton(ImageUtils.createColorIcon(Color.BLUE));
 		colorButtonCyan = new WebButton(ImageUtils.createColorIcon(Color.CYAN));
 		colorButtonGreen = new WebButton(ImageUtils.createColorIcon(Color.GREEN));
 		colorButtonMagenta = new WebButton(ImageUtils.createColorIcon(Color.MAGENTA));
 		colorButtonOrange = new WebButton(ImageUtils.createColorIcon(Color.ORANGE));
 		colorButtonPink = new WebButton(ImageUtils.createColorIcon(Color.PINK));
 		colorButtonRed = new WebButton(ImageUtils.createColorIcon(Color.RED));
 		colorButtonWhite = new WebButton(ImageUtils.createColorIcon(Color.WHITE));
 		colorButtonYellow = new WebButton(ImageUtils.createColorIcon(Color.YELLOW));
 
 		colorChooserButton = new WebButton("Current color",
 				ImageUtils.createColorIcon(app.getSynthesizer().getCurrentWireColor()));
 
 		toolBar.add(colorButtonBlack);
 		toolBar.add(colorButtonGray);
 		toolBar.add(colorButtonWhite);
 
 		toolBar.add(colorButtonPink);
 		toolBar.add(colorButtonRed);
 		toolBar.add(colorButtonMagenta);
 		toolBar.add(colorButtonBlue);
 		toolBar.add(colorButtonCyan);
 		toolBar.add(colorButtonGreen);
 		toolBar.add(colorButtonYellow);
 		toolBar.add(colorButtonOrange);
 
 		toolBar.add(colorChooserButton);
 		frameContainer.add(BorderLayout.NORTH, toolBar);
 
 	}
 
 	/**
 	 * Configure the frame's components
 	 */
 	private void configureView() {
 		// parametrage de la JFrame
 		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setSize(1400, 880);
 		getContentPane().setBackground(Color.GRAY);
 
 		WebStatusBar statusBar = new WebStatusBar();
 		WebMemoryBar memoryBar = new WebMemoryBar();
 		memoryBar.setShowMaximumMemory(false);
 		statusBar.add(memoryBar);
 
 		getContentPane().add(BorderLayout.SOUTH, statusBar);
 
 		// TODO remonter les evenements start et stop
 
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
 		this.setResizable(true);
 
 		// ajout des raccourcis clavier aux elements du menu
 		menuItemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
 		menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
 		menuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
 		menuItemSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.SHIFT_MASK + ActionEvent.CTRL_MASK));
 		menuItemQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
 		menuItemDoc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
 		menuItemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
 	}
 
 	/**
 	 * Define some callbacks for interaction
 	 */
 	private void defineCallbacks() {
 		menuItemNew.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (newSynthCommand != null)
 					newSynthCommand.execute();
 			}
 		});
 
 		menuItemOpen.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (openSynthCommand != null)
 					openSynthCommand.execute();
 			}
 		});
 
 		menuItemSave.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (saveSynthCommand != null)
 					saveSynthCommand.execute();
 			}
 		});
 
 		menuItemSaveAs.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (saveAsSynthCommand != null)
 					saveAsSynthCommand.execute();
 			}
 		});
 
 		menuItemQuit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (quitSynthCommand != null)
 					quitSynthCommand.execute();
 			}
 		});
 
 		ActionListener listenerOUT = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleOUTCommand != null)
 					addModuleOUTCommand.execute();
 			}
 		};
 		menuItemAddModuleOUT.addActionListener(listenerOUT);
 		buttonOUT.addActionListener(listenerOUT);
 
 		ActionListener listenerVCO = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleVCOCommand != null)
 					addModuleVCOCommand.execute();
 			}
 		};
 		menuItemAddModuleVCO.addActionListener(listenerVCO);
 		buttonVCO.addActionListener(listenerVCO);
 
 		ActionListener listenerVCA = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleVCACommand != null)
 					addModuleVCACommand.execute();
 			}
 		};
 		menuItemAddModuleVCA.addActionListener(listenerVCA);
 		buttonVCA.addActionListener(listenerVCA);
 
 		ActionListener listenerVCFLP = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleVCFLPCommand != null)
 					addModuleVCFLPCommand.execute();
 			}
 		};
 		menuItemAddModuleVCFLP.addActionListener(listenerVCFLP);
 		buttonVCFLP.addActionListener(listenerVCFLP);
 
 		ActionListener listenerVCFHP = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleVCFHPCommand != null)
 					addModuleVCFHPCommand.execute();
 			}
 		};
 		menuItemAddModuleVCFHP.addActionListener(listenerVCFHP);
 		buttonVCFHP.addActionListener(listenerVCFHP);
 
 		ActionListener listenerEG = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleEGCommand != null)
 					addModuleEGCommand.execute();
 			}
 		};
 		menuItemAddModuleEG.addActionListener(listenerEG);
 		buttonEG.addActionListener(listenerEG);
 
 		ActionListener listenerSCOP = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleAudioScopeCommand != null)
 					addModuleAudioScopeCommand.execute();
 			}
 		};
 		menuItemAddModuleAudioScope.addActionListener(listenerSCOP);
 		buttonSCOP.addActionListener(listenerSCOP);
 
 		ActionListener listenerREP = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleREPCommand != null)
 					addModuleREPCommand.execute();
 			}
 		};
 		menuItemAddModuleREP.addActionListener(listenerREP);
 		buttonREP.addActionListener(listenerREP);
 
 		ActionListener listenerMIX = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleMIXCommand != null)
 					addModuleMIXCommand.execute();
 			}
 		};
 		menuItemAddModuleMIX.addActionListener(listenerMIX);
 		buttonMIX.addActionListener(listenerMIX);
 
 		menuItemDoc.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (docSynthCommand != null)
 					docSynthCommand.execute();
 			}
 		});
 		menuItemAbout.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (aboutSynthCommand != null)
 					aboutSynthCommand.execute();
 			}
 		});
 
 		buttonPlayPause.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
				if (isPlaying) {
 					if (toolbarPauseCommand != null) {
 						toolbarPauseCommand.execute();
 						Image img;
 						try {
 							img = ImageIO.read(new File(iconFiles[0]));
 							img = img.getScaledInstance(25, 25, Image.SCALE_DEFAULT);
 							buttonPlayPause.setIcon(new ImageIcon(img));
 						} catch (IOException e1) {
 							e1.printStackTrace();
 						}
						isPlaying = !isPlaying;
 					}
 				} else {
 					if (toolbarPlayCommand != null) {
 						toolbarPlayCommand.execute();
 						Image img;
 						try {
 							img = ImageIO.read(new File(iconFiles[1]));
 							img = img.getScaledInstance(25, 25, Image.SCALE_DEFAULT);
 							buttonPlayPause.setIcon(new ImageIcon(img));
 						} catch (IOException e1) {
 							e1.printStackTrace();
 						}
						isPlaying = !isPlaying;
 					}
 				}
 
 			}
 		});
 
 		// Toolbar colors ActionListeners
 		colorButtonBlack.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setCurrentWireColor(Color.BLACK);
 				toolbarCurrentWireColorCommand.execute();
 			}
 		});
 		colorButtonGray.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setCurrentWireColor(Color.GRAY);
 				toolbarCurrentWireColorCommand.execute();
 			}
 		});
 		colorButtonBlue.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setCurrentWireColor(Color.BLUE);
 				toolbarCurrentWireColorCommand.execute();
 			}
 		});
 		colorButtonCyan.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setCurrentWireColor(Color.CYAN);
 				toolbarCurrentWireColorCommand.execute();
 			}
 		});
 		colorButtonGreen.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setCurrentWireColor(Color.GREEN);
 				toolbarCurrentWireColorCommand.execute();
 			}
 		});
 		colorButtonMagenta.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setCurrentWireColor(Color.MAGENTA);
 				toolbarCurrentWireColorCommand.execute();
 			}
 		});
 		colorButtonOrange.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setCurrentWireColor(Color.ORANGE);
 				toolbarCurrentWireColorCommand.execute();
 			}
 		});
 		colorButtonPink.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setCurrentWireColor(Color.PINK);
 				toolbarCurrentWireColorCommand.execute();
 			}
 		});
 		colorButtonRed.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setCurrentWireColor(Color.RED);
 				toolbarCurrentWireColorCommand.execute();
 			}
 		});
 		colorButtonWhite.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setCurrentWireColor(Color.WHITE);
 				toolbarCurrentWireColorCommand.execute();
 			}
 		});
 		colorButtonYellow.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setCurrentWireColor(Color.YELLOW);
 				toolbarCurrentWireColorCommand.execute();
 			}
 		});
 
 		colorChooserButton.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				WebColorChooserDialog colorChooser = new WebColorChooserDialog(
 						toolBar);
 				colorChooser.setColor(app.getSynthesizer()
 						.getCurrentWireColor());
 				colorChooser.setVisible(true);
 
 				if (colorChooser.getResult() == StyleConstants.OK_OPTION) {
 					setCurrentWireColor(colorChooser.getColor());
 					toolbarCurrentWireColorCommand.execute();
 				}
 			}
 		});
 	}
 
 	@Override
 	public void displaySynth() {
 		if (pres != null)
 			this.remove((PSynthesizer) pres);
 
 		pres = app.getSynthesizer().getPresentation();
 		this.add((PSynthesizer) pres);
 		this.setVisible(true);
 		this.repaint();
 		this.validate();
 	}
 
 	@Override
 	public void quitSynth() {
 		this.setVisible(false);
 	}
 
 	/**
 	 * @param newSynthCommand
 	 *            the newSynthCommand to set
 	 */
 	public void setNewSynthCommand(ICommand newSynthCommand) {
 		this.newSynthCommand = newSynthCommand;
 	}
 
 	/**
 	 * @param saveSynthCommand
 	 *            the saveSynthCommand to set
 	 */
 	public void setSaveSynthCommand(ICommand saveSynthCommand) {
 		this.saveSynthCommand = saveSynthCommand;
 	}
 
 	/**
 	 * @param saveAsSynthCommand
 	 *            the saveAsSynthCommand to set
 	 */
 	public void setSaveAsSynthCommand(ICommand saveAsSynthCommand) {
 		this.saveAsSynthCommand = saveAsSynthCommand;
 	}
 
 	/**
 	 * @param openSynthCommand
 	 *            the openSynthCommand to set
 	 */
 	public void setOpenSynthCommand(ICommand openSynthCommand) {
 		this.openSynthCommand = openSynthCommand;
 	}
 
 	/**
 	 * @param quitSynthCommand
 	 *            the quitSynthCommand to set
 	 */
 	public void setQuitSynthCommand(ICommand quitSynthCommand) {
 		this.quitSynthCommand = quitSynthCommand;
 	}
 
 	/**
 	 * @param docSynthCommand
 	 *            the docSynthCommand to set
 	 */
 	public void setDocSynthCommand(ICommand docSynthCommand) {
 		this.docSynthCommand = docSynthCommand;
 	}
 
 	/**
 	 * @param aboutSynthCommand
 	 *            the aboutSynthCommand to set
 	 */
 	public void setAboutSynthCommand(ICommand aboutSynthCommand) {
 		this.aboutSynthCommand = aboutSynthCommand;
 	}
 
 	/**
 	 * @param toolbarModuleCommand
 	 *            the toolbarModuleCommand to set
 	 */
 	public void setToolbarPlayCommand(ICommand toolbarPlayCommand) {
 		this.toolbarPlayCommand = toolbarPlayCommand;
 	}
 
 	/**
 	 * @param toolbarModuleCommand
 	 *            the toolbarModuleCommand to set
 	 */
 	public void setToolbarPauseCommand(ICommand toolbarPauseCommand) {
 		this.toolbarPauseCommand = toolbarPauseCommand;
 	}
 
 	/**
 	 * @param addModuleVCOCommand
 	 */
 	public void setToolbarRecordCommand(ICommand toolbarRecordCommand) {
 	}
 
 	/**
 	 * @param addModuleVCOCommand
 	 */
 	public void setAddModuleVCOCommand(ICommand addModuleVCOCommand) {
 		this.addModuleVCOCommand = addModuleVCOCommand;
 	}
 
 	/**
 	 * @param addModuleVCACommand
 	 */
 	public void setAddModuleVCACommand(ICommand addModuleVCACommand) {
 		this.addModuleVCACommand = addModuleVCACommand;
 	}
 
 	/**
 	 * @param addModuleVCFLPCommand
 	 */
 	public void setAddModuleVCFLPCommand(ICommand addModuleVCFLPCommand) {
 		this.addModuleVCFLPCommand = addModuleVCFLPCommand;
 	}
 
 	/**
 	 * @param addModuleVCFHPCommand
 	 */
 	public void setAddModuleVCFHPCommand(ICommand addModuleVCFHPCommand) {
 		this.addModuleVCFHPCommand = addModuleVCFHPCommand;
 	}
 
 	/**
 	 * @param addModuleOUTCommand
 	 */
 	public void setAddModuleOUTCommand(ICommand addModuleOUTCommand) {
 		this.addModuleOUTCommand = addModuleOUTCommand;
 	}
 
 	/**
 	 * @param addModuleEGCommand
 	 */
 	public void setAddModuleEGCommand(ICommand addModuleEGCommand) {
 		this.addModuleEGCommand = addModuleEGCommand;
 	}
 
 	/**
 	 * @param addModuleAudioScopeCommand
 	 */
 	public void setAddModuleAudioScopeCommand(AddModuleAudioScopeCommand addModuleAudioScopeCommand) {
 		this.addModuleAudioScopeCommand = addModuleAudioScopeCommand;
 	}
 
 	/**
 	 * @param addModuleREPCommand
 	 */
 	public void setAddModuleREPCommand(AddModuleREPCommand addModuleREPCommand) {
 		this.addModuleREPCommand = addModuleREPCommand;
 	}
 
 	public void setAddModuleMIXCommand(AddModuleMIXCommand addModuleMIXCommand) {
 		this.addModuleMIXCommand = addModuleMIXCommand;
 	}
 
 	/**
 	 * @param setCurrentWireColor
 	 */
 	public void setCurrentWireColorCommand(ToolbarCurrentWireColorCommand currentWireColorCommand) {
 		this.toolbarCurrentWireColorCommand = currentWireColorCommand;
 	}
 
 	public Color getCurrentWireColor() {
 		return toolbarCurrentWireColor;
 	}
 
 	private void setCurrentWireColor(Color color) {
 		toolbarCurrentWireColor = color;
 		colorChooserButton.setIcon(ImageUtils.createColorIcon(toolbarCurrentWireColor));
 	}
 
 	@Override
 	public void addToMenu(final ICSynthesizer currentSynth) {
 		final JRadioButtonMenuItem item = new JRadioButtonMenuItem(currentSynth.getPath());
 		filesGroup.add(item);
 		
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				app.setSynthesizer(item.getText());
 				displaySynth();
 				setTitle("SynthlabG2 - "+currentSynth.getPath());
 			}
 		});
 		
 		files.add(item);
 		menuWindow.add(item);
 		item.setSelected(true);
 		this.setTitle("SynthlabG2 - "+currentSynth.getPath());
 	}
 
 	@Override
 	public void stopTheButton() {
 		Image img;
 		try {
 			img = ImageIO.read(new File(iconFiles[0]));
 			img = img.getScaledInstance(25, 25, Image.SCALE_DEFAULT);
 			buttonPlayPause.setIcon(new ImageIcon(img));
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
		isPlaying = false;
 	}
 
 	@Override
 	public void removeFromMenu(ICSynthesizer currentSynth) {
 		for(JMenuItem currItem : files){
 			if(currItem.getText().equals(currentSynth.getPath())){
 				menuWindow.remove(currItem);
 			}
 		}
 	}
 
 }
