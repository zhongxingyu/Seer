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
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.KeyStroke;
 
 import com.alee.extended.statusbar.WebMemoryBar;
 import com.alee.extended.statusbar.WebStatusBar;
 import com.alee.extended.statusbar.WebStatusLabel;
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
 import fr.istic.synthlab.presentation.synthesizer.IPSynthesizer;
 import fr.istic.synthlab.presentation.synthesizer.PSynthesizer;
 
 public class SynthFrame extends JFrame implements ISynthFrame {
 
 	private static final long serialVersionUID = -7577358239451859975L;
 
 	// constantes pour les boutons de la toolbar
 	// private static final int BUTTON_DEFAULT = 0;
 	// private static final int BUTTON_MODULE = 2;
 
 	private static final int BUTTON_PLAY = 0;
 	private static final int BUTTON_PAUSE = 1;
 	// private static final int BUTTON_RECORD = 5;
 
 	private IPSynthesizer pres;
 
 	// Menu
 	private JMenuBar mainMenuBar;
 	private JMenu menuFile, menuAdd, menuHelp;
 	private JMenuItem menuItemNew, menuItemOpen, menuItemSave;
 	private JMenuItem menuItemQuit, menuItemDoc, menuItemAbout;
 	private JMenuItem menuItemAddModuleVCO, menuItemAddModuleOUT,
 			menuItemAddModuleVCFLP, menuItemAddModuleVCFHP, menuItemAddModuleEG,
 			menuItemAddModuleAudioScope, menuItemAddModuleREP,
 			menuItemAddModuleVCA, menuItemAddModuleMIX ;
 
 	// Toolbar
 	private WebToolBar toolBar = new WebToolBar();
 	private String[] iconFiles = { "res/play.png", "res/stop.png" };
 	private String buttonPlayPauseLabel = "Play/Stop";
 	private JButton buttonPlayPause = new JButton();
 
 	// Command
 	private ICommand newSynthCommand;
 	private ICommand saveSynthCommand;
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
 
 	private Color toolbarCurrentWireColor = Color.BLACK;
 	
	private boolean isPlaying = true;
 
 	public SynthFrame() {
 		this.initComponents();
 		this.configureView();
 		this.defineCallbacks();
 	}
 
 	/**
 	 * Initialize the frame's components
 	 */
 	private void initComponents() {
 		// instanciation de la barre de menu
 
 		mainMenuBar = new JMenuBar();
 
 		// -------------------------- File Menu --------------------------
 		menuFile = new JMenu("File");
 
 		// this.add(new UsageDisplay());
 		// instanciation des items
 		menuItemNew = new JMenuItem("New");
 		menuItemOpen = new JMenuItem("Open");
 		menuItemSave = new JMenuItem("Save");
 		menuItemQuit = new JMenuItem("Quit");
 
 		// ajout des items au menu File
 		menuFile.add(menuItemNew);
 		menuFile.add(menuItemOpen);
 		menuFile.add(menuItemSave);
 		menuFile.add(menuItemQuit);
 
 		// -------------------------- Add Menu --------------------------
 		menuAdd = new JMenu("Add module");
 
 		menuItemAddModuleAudioScope = new JMenuItem("SCOP   | AudioScope");
 		menuItemAddModuleVCFLP = new JMenuItem("VCF-LP | Voltage-Controlled Filter Low-Pass");
 		menuItemAddModuleVCFHP = new JMenuItem("VCF-HP | Voltage-Controlled Filter High-Pass");
 		menuItemAddModuleOUT = new JMenuItem("OUT    | Output on soundcard");
 		menuItemAddModuleVCO = new JMenuItem("VCO    | Voltage-Controlled Oscillator");
 		menuItemAddModuleVCA = new JMenuItem("VCA    | Voltage-Controlled Amplifier");
 		menuItemAddModuleREP = new JMenuItem("REP    | Replicator");
 		menuItemAddModuleMIX = new JMenuItem("MIX    | Mixer");
 		menuItemAddModuleEG  = new JMenuItem("EG     | Enveloppe generator");
 		
 		Font font = new Font(Font.MONOSPACED, Font.ROMAN_BASELINE, 11);
 		
 		menuItemAddModuleVCO.setFont(font);
 		menuItemAddModuleVCA.setFont(font);
 		menuItemAddModuleVCFLP.setFont(font);
 		menuItemAddModuleVCFHP.setFont(font);
 		menuItemAddModuleOUT.setFont(font);
 		menuItemAddModuleEG.setFont(font);
 		menuItemAddModuleMIX.setFont(font);
 		menuItemAddModuleREP.setFont(font);
 		menuItemAddModuleAudioScope.setFont(font);
 		menuAdd.add(menuItemAddModuleVCO);
 		menuAdd.add(menuItemAddModuleVCA);
 		menuAdd.add(menuItemAddModuleVCFLP);
 		menuAdd.add(menuItemAddModuleVCFHP);
 		menuAdd.add(menuItemAddModuleOUT);
 		menuAdd.add(menuItemAddModuleEG);
 		menuAdd.add(menuItemAddModuleMIX);
 		menuAdd.add(menuItemAddModuleREP);
 		menuAdd.add(menuItemAddModuleAudioScope);
 		
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
 		mainMenuBar.add(menuHelp);
 		this.setJMenuBar(mainMenuBar);
 
 		// instanciation des boutons de la toolbar
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
 		
 		final WebButton colorChooserButton = new WebButton ( "Next wire's color", ImageUtils.createColorIcon ( Color.GRAY ) );
         colorChooserButton.addActionListener ( new ActionListener ()
         {
             private WebColorChooserDialog colorChooser = null;
             private Color lastColor = Color.GRAY;
 
             public void actionPerformed ( ActionEvent e )
             {
                 if ( colorChooser == null )
                 {
                     colorChooser = new WebColorChooserDialog(toolBar);
                 }
                 colorChooser.setColor ( lastColor );
                 colorChooser.setVisible ( true );
 
                 if ( colorChooser.getResult () == StyleConstants.OK_OPTION )
                 {
                     Color color = colorChooser.getColor ();
                     toolbarCurrentWireColor = color;
                     toolbarCurrentWireColorCommand.execute();
                     lastColor = color;
 
                     colorChooserButton.setIcon ( ImageUtils.createColorIcon ( color ) );
                 }
             }
         } );
         
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
 		statusBar.add(new WebStatusLabel("Synthesizer ready"));
 
 		getContentPane().add(BorderLayout.SOUTH, statusBar);
 
 		// TODO remonter les evenements start et stop
 
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height
 				/ 2 - this.getSize().height / 2);
 		this.setResizable(true);
 
 		// ajout des raccourcis clavier au elements du menu
 		menuItemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
 				ActionEvent.CTRL_MASK));
 		menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
 				ActionEvent.CTRL_MASK));
 		menuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
 				ActionEvent.CTRL_MASK));
 		menuItemQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
 				ActionEvent.CTRL_MASK));
 		menuItemDoc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
 				ActionEvent.CTRL_MASK));
 		menuItemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
 				ActionEvent.CTRL_MASK));
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
 		menuItemQuit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (quitSynthCommand != null)
 					quitSynthCommand.execute();
 			}
 		});
 		menuItemAddModuleOUT.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleOUTCommand != null)
 					addModuleOUTCommand.execute();
 			}
 		});
 		menuItemAddModuleVCO.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleVCOCommand != null)
 					addModuleVCOCommand.execute();
 			}
 		});
 		menuItemAddModuleVCA.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleVCACommand != null)
 					addModuleVCACommand.execute();
 			}
 		});
 		menuItemAddModuleVCFLP.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleVCFLPCommand != null)
 					addModuleVCFLPCommand.execute();
 			}
 		});
 		menuItemAddModuleVCFHP.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleVCFHPCommand != null)
 					addModuleVCFHPCommand.execute();
 			}
 		});
 		menuItemAddModuleEG.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleEGCommand != null)
 					addModuleEGCommand.execute();
 			}
 		});
 		menuItemAddModuleAudioScope.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleAudioScopeCommand != null)
 					addModuleAudioScopeCommand.execute();
 			}
 		});
 		menuItemAddModuleREP.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleREPCommand != null)
 					addModuleREPCommand.execute();
 			}
 		});
 		menuItemAddModuleMIX.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (addModuleMIXCommand != null)
 					addModuleMIXCommand.execute();
 			}
 		});
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
 				if(isPlaying){
 					if (toolbarPauseCommand != null){
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
 					if (toolbarPlayCommand != null){
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
 	}
 
 	@Override
 	public void displaySynth(IPSynthesizer presentation) {
 		if (pres != null)
 			this.remove((PSynthesizer) pres);
 
 		pres = presentation;
 		this.add((PSynthesizer) pres);
 		this.setVisible(true);
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
 	public void setAddModuleAudioScopeCommand(
 			AddModuleAudioScopeCommand addModuleAudioScopeCommand) {
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
 		return toolbarCurrentWireColor ;
 	}
 
 }
