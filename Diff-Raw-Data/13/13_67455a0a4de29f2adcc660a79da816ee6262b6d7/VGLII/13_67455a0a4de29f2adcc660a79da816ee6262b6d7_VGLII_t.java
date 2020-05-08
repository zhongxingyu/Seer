 package VGL;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.CookieHandler;
 import java.net.CookieManager;
 import java.net.CookiePolicy;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.JToolBar;
 import javax.swing.SpringLayout;
 import javax.swing.border.SoftBevelBorder;
 import javax.swing.text.html.HTMLDocument;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import GeneticModels.Cage;
 import GeneticModels.CharacterSpecificationBank;
 import GeneticModels.GeneticModel;
 import GeneticModels.GeneticModelFactory;
 import GeneticModels.Organism;
 import Grader.AutoGrader;
 import Grader.CageScorer;
 import Grader.Grader;
 import ModelBuilder.ModelBuilderUI;
 import PhenotypeImages.PhenotypeImageBank;
 
 /**
  * Nikunj Koolar cs681-3 Fall 2002 - Spring 2003 Project VGL File:
  * Brian White Summer 2008
  * VGLII.java - the UI controller class. Its the heart of almost all UI
  * renditions and manipulations.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  * 
  * @author Nikunj Koolar & Brian White
  * @version 1.0 $Id$
  */
 public class VGLII extends JFrame {
 
 	/**
 	 * the version number
 	 */
 	public final static String version = "3.3.0 2012-02-15 21:22"; //$NON-NLS-1$
 
 	/*
 	 * param name for determining edXMode - see under main()
 	 */
 	private final static String ED_X_MODE_NAME = "-edXMode";
 
 	/**
 	 * the list of supported languages
 	 */
 	public final static LanguageSpecifierMenuItem[] supportedLanguageMenuItems = {
 		new LanguageSpecifierMenuItem("English", "en", "US"),
 		new LanguageSpecifierMenuItem("Espaol", "es", "ES"),
 		new LanguageSpecifierMenuItem("Franais", "fr", "FR"),
 		//		new LanguageSpecifierMenuItem("\uD55C\uAD6D\uC5B4", "ko", "KR")
 	};
 
 	// used for java web start (edX) runs
 	private String[] args;
 
 	private PrivateKey gradingKey;
 	private PublicKey saveForGradingKey;
 
 	// used for edX server login
 	private EmailAndPassword eMailAndPassword;
 
 	/**
 	 * the dimensions of the Phenotype image
 	 */
 	public final static int PHENO_IMAGE_WIDTH = 900;
 	public final static int PHENO_IMAGE_HEIGHT = 700;
 
 	private Random random;
 
 	/**
 	 * boolean set when anything savable changes
 	 * 	new cross
 	 *  change to ModelBuilder
 	 * cleared when user saves work
 	 */
 	private boolean changeSinceLastSave;
 
 	/**
 	 * the genetic model for the current problem
 	 */
 	private GeneticModel geneticModel;
 
 	/**
 	 * the model builder for the current problem
 	 */
 	private ModelBuilderUI modelBuilder;
 	private JDialog modelBuilderDialog;
 
 	/**
 	 * The common file chooser instance for the application
 	 */
 	private JFileChooser m_FChooser;
 
 	/**
 	 * The collection of Cage UIs associated with the current problem
 	 */
 	private ArrayList<CageUI> cageCollection;
 
 	/**
 	 * The id of the next cage that will be created
 	 */
 	private int nextCageId = 0;
 
 	/**
 	 * The singular instance that holds the current male-female selection for
 	 * crossing
 	 */
 	private SelectionVial selectionVial;
 
 	/**
 	 * This widget holds the buttons
 	 */
 	private JToolBar toolBar = null;
 
 	/**
 	 * The label for the status panel to display information
 	 */
 	private JLabel statusLabel = null;
 
 	/**
 	 * The Document renderer to allow printing
 	 */
 	private DocumentRenderer docRenderer;
 
 	/**
 	 * The filter type to display only Problem type files
 	 */
 	private static final String prbFilterString = new String("pr2"); //$NON-NLS-1$
 
 	/**
 	 * The filter type to display only Work files
 	 */
 	public static final String wrkFilterString = new String("wr2"); //$NON-NLS-1$
 
 	/**
 	 * The filter type to display Print files
 	 */
 	private static final String printFilterString = new String("html"); //$NON-NLS-1$
 
 	/**
 	 * Filter to display work saved for grading
 	 */
 	private static final String saveForGradingFilterString = new String("gr2");
 
 	/**
 	 * main menu bar
 	 */
 	private JMenuBar mnuBar = null;
 
 	/**
 	 * language selection menu
 	 */
 	private JMenu mnuLanguage;
 
 	/**
 	 * Menu item to open a new problem type
 	 */
 	private JMenuItem newProblemItem = null;
 
 	/**
 	 * Menu item to open a saved problem
 	 */
 	private JMenuItem openProblemItem = null;
 
 	/**
 	 * Menu item to save current work to a file
 	 */
 	private JMenuItem saveProblemItem = null;
 
 	/**
 	 * Menu item to save current work to a different file than the current one
 	 */
 	private JMenuItem saveProblemAsItem = null;
 
 	/**
 	 * Menu item to save work for grading
 	 * (encrypted with student.key)
 	 * student.key must be present
 	 * Only shown if saveForGradingEnabled = true
 	 */
 	private JMenuItem saveForGradingItem = null;
 
 	/**
 	 * only shown in edX version
 	 * if saveToServer = true
 	 */
 	private JMenuItem saveToServerItem = null;
 
 	/**
 	 * Menu item to close the current work
 	 */
 	private JMenuItem closeProblemItem = null;
 
 	/**
 	 * Menu item to close application
 	 */
 	private JMenuItem exitItem = null;
 
 	/**
 	 * Menu item to cross two organisms
 	 */
 	private JMenuItem crossTwoItem = null;
 
 	/**
 	 * menu item for super cross - 1000 offspring
 	 */
 	private JMenuItem superCrossItem = null;
 
 	/**
 	 * menu item for grading
 	 * only shown if graderEnabled = true
 	 */
 	private JMenuItem graderItem = null;
 
 	/**
 	 * Menu item to display "about VGL" box
 	 */
 	private JMenuItem aboutItem = null;
 
 	/**
 	 * Menu item to invoke cage manager dialog
 	 */
 	private JMenuItem cageManagerItem = null;
 
 	/**
 	 * Menu item to print current work to file
 	 */
 	private JMenuItem printToFileItem = null;
 
 	/**
 	 * Menu item to print current work
 	 */
 	private JMenuItem printItem = null;
 
 	/**
 	 * Menu item to set up the printing page
 	 */
 	private JMenuItem pageSetupItem = null;
 
 	/**
 	 * Menu item to invoke help
 	 */
 	private JMenuItem onlineHelpItem = null;
 
 	/**
 	 * Menu item to re-arrange cages
 	 */
 	private JMenuItem rearrangeCagesItem = null;
 
 	/**
 	 * menu item to show summary charts
 	 */
 	private JMenuItem summaryChartItem = null;
 
 	/**
 	 * menu item to clear selected cages
 	 */
 	private JMenuItem unselectAllItem = null;
 
 	/**
 	 * menu item to show the ModelBuilder
 	 */
 	private JMenuItem modelBuilderItem = null;
 
 	/**
 	 * Button to open a saved problem
 	 */
 	private JButton openButton = null;
 
 	/**
 	 * Button to open a new problem type
 	 */
 	private JButton newButton = null;
 
 	/**
 	 * Button to save to edX server
 	 */
 	private JButton saveToServerButton = null;
 
 	/**
 	 * Button to close the current work
 	 */
 	private JButton closeButton = null;
 
 	/**
 	 * Button to print the current work
 	 */
 	private JButton printButton = null;
 
 	/**
 	 * Button to exit application
 	 */
 	private JButton exitButton = null;
 
 	/**
 	 * Button to save current work
 	 */
 	private JButton saveButton = null;
 
 	/**
 	 * Button to save to a different file than the current file
 	 */
 	private JButton saveAsButton = null;
 
 	/**
 	 * Button to cross two organisms
 	 */
 	private JButton crossTwoButton = null;
 
 	/**
 	 * Button to display "about VGL" box
 	 */
 	private JButton aboutButton = null;
 
 	/**
 	 * Button to print current work to file
 	 */
 	private JButton printToFileButton = null;
 
 	/**
 	 * Button to invoke help
 	 */
 	private JButton onlineHelpButton = null;
 
 	/**
 	 * the current problem file 
 	 */
 	private File problemFile;
 
 	/**
 	 * The current file to which work is being saved to
 	 */
 	private File currentSavedFile = null;
 
 	/**
 	 * The default path for the problem file dialogs to open in
 	 */
 	private File defaultProblemDirectory = new File("."); //$NON-NLS-1$
 
 	/**
 	 * the default path for saving work and html files to
 	 * aka the desktop
 	 * - this requires some code, so runs in VGLII's constructor
 	 */
 	private File desktopDirectory = null;
 
 	/**
 	 * Stores the value of the next position on the screen where a cage should
 	 * be displayed
 	 */
 	private Point nextCageScreenPosition;
 
 	/**
 	 * The constructor
 	 * 
 	 */
 	public VGLII(String[] args) {
 		super(Messages.getInstance().getString("VGLII.Name") + version); //$NON-NLS-1$
 		addWindowListener(new ApplicationCloser());
 		this.args = args;
 		/*
 		 * find out if edX before constructing
 		 * 	so you can determine which menus to show
 		 *  - includes Save to Edx menu and button
 		 */
 		boolean saveToEdXServerEnabled = false;
 		if ((args.length == 1) && args[0].equals(ED_X_MODE_NAME)) {	
 			saveToEdXServerEnabled = true;					// mode 3
 		} else if (args.length > 1) {
 			saveToEdXServerEnabled = true;					// mode 4
 		}
 
 		random = new Random();
 
 		desktopDirectory = new File(System.getProperty("user.home")  //$NON-NLS-1$
 				+ System.getProperty("file.separator") //$NON-NLS-1$
 				+ "Desktop"); //$NON-NLS-1$
 		if (!desktopDirectory.exists()) {
 			desktopDirectory = defaultProblemDirectory;
 		}
 
 		/**
 		 * boolean for whether grading will work
 		 * requires all of these:
 		 * 	1) non-expired grader.key in same directory as VGL
 		 *  2) entry of correct password from grader.key
 		 *  3) instructor.key in same directory as VGL
 		 */
 		boolean graderEnabled = false;
 		if (!saveToEdXServerEnabled) {
 			gradingKey = KeyFileChecker.checkGradingKeys(this);
 			if (gradingKey != null) graderEnabled = true;
 		}
 
 		/**
 		 * whether Save for Grading is enabled
 		 * requires presence of student.key in 
 		 * same directory as VGL
 		 */
 		boolean saveForGradingEnabled = false;
 		if (!saveToEdXServerEnabled) {
 			saveForGradingKey = KeyFileChecker.checkSaveForGradingKey();
 			if (saveForGradingKey != null) saveForGradingEnabled = true;
 		}
 
 		setupUI(saveToEdXServerEnabled, graderEnabled, saveForGradingEnabled); 
 		changeSinceLastSave = false;
 	}
 
 
 	/**
 	 * main method; 4 modes
 	 * 1) Standard = start with blank screen and open from there (the way it's always been)
 	 * 		- launch with no params
 	 * 		- "Save to edX" - absent
 	 * 		- "new problem" - present
 	 * 
 	 * 2) Launch with work or problem file = opens with that problem started
 	 * 		- launch with 1 param = filename.pr2 or .wr2
 	 * 		- "save to edX" - absent
 	 * 		- "new problem" - grayed out
 	 * 
 	 * 3) Launch from edX with no file (so they can look at saved work)
 	 * 		- launch with 1 param = -edXMode
 	 * 		- it will get server params from saved .wr2 file
 	 * 		- "save to edX" - present
 	 * 		- "new problem" - absent (and disable key command)
 	 * 
 	 * 4) Launch from edX with lots of params to open with new problem file specified by params
 	 * 		- launch with > 1 param 
 	 * 		- "save to edX" - present
 	 * 		- "new problem" - absent (and disable key command)
 	 * 
 	 */
 	public static void main(String[] args) {
 
 		VGLII vgl2 = new VGLII(args);
 		vgl2.setVisible(true);
 
 		if ((args.length == 1) && (!args[0].equals(ED_X_MODE_NAME))) {
 			String fileName = args[0];
 			if (fileName.endsWith(".pr2")) { //$NON-NLS-1$
 				vgl2.newProblemFromFile(fileName);
 			} else if (fileName.endsWith(".wr2")) { //$NON-NLS-1$
 				vgl2.openProblem(fileName);
 			}
 		} 
 	}
 
 	class ApplicationCloser extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * Dispatches events resulting from pre-registered listeners.
 	 * 
 	 * @param evt
 	 *            the action to be taken
 	 *             
 	 */
 	private void eventHandler(ActionEvent evt) {
 		String cmd = evt.getActionCommand();
 		update(getGraphics());
 		if (cmd.equals("NewProblemFromFile")) //$NON-NLS-1$
 			newProblemFromFile(null);
 		else if (cmd.equals("NewProblemFromArgs"))
 			newProblemFromArgs();
 		else if (cmd.equals("OpenWork")) //$NON-NLS-1$
 			openProblem(null);
 		else if (cmd.equals("SaveWork")) //$NON-NLS-1$
 			saveProblem();
 		else if (cmd.equals("SaveAs")) //$NON-NLS-1$
 			saveAsProblem();
 		else if (cmd.equals("SaveForGrading"))
 			saveForGrading();
 		else if (cmd.equals("SaveToServer"))
 			saveToServer();
 		else if (cmd.equals("PrintToFile")) //$NON-NLS-1$
 			printToFile();
 		else if (cmd.equals("PageSetup")) //$NON-NLS-1$
 			pageSetup();
 		else if (cmd.equals("PrintWork")) //$NON-NLS-1$
 			print();
 		else if (cmd.equals("CloseWork")) //$NON-NLS-1$
 			closeProblem();
 		else if (cmd.equals("CrossTwo")) //$NON-NLS-1$
 			crossTwo(false);
 		else if (cmd.equals("SuperCross"))
 			crossTwo(true);
 		else if (cmd.equals("Grader"))
 			grade();
 		else if (cmd.equals("Exit")) //$NON-NLS-1$
 			exitApplication();
 		else if (cmd.equals("About")) //$NON-NLS-1$
 			aboutVGL();
 		else if (cmd.equals("CageManager")) //$NON-NLS-1$
 			cageManager();
 		else if (cmd.equals("OnlineHelp")) //$NON-NLS-1$
 			onlineHelp();
 		else if (cmd.equals("RearrangeCages")) //$NON-NLS-1$
 			reArrangeCages();
 		else if (cmd.equals("SummaryChart")) //$NON-NLS-1$
 			summaryChart();
 		else if (cmd.equals("UnselectAll")) //$NON-NLS-1$
 			unselectAll();
 		else if (cmd.equals("ModelBuilder"))
 			showModelBuilder();
 	}
 
 	/**
 	 * Create menu item
 	 * 
 	 * @param label
 	 *            the name for the menu item
 	 * @param actionCommand
 	 *            the command to execute when the menuitem is pressed
 	 * @param image
 	 *            the image to be used for the menu item
 	 * @return the newly created menu item
 	 */
 	private JMenuItem menuItem(String label, String actionCommand,
 			ImageIcon image) {
 		JMenuItem result = new JMenuItem(label);
 		result.setActionCommand(actionCommand);
 		result.setIcon(image);
 		result.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				eventHandler(evt);
 			}
 		});
 		return result;
 	}
 
 	/**
 	 * Create check box menu item.
 	 * 
 	 * @param label
 	 *            the label for the check box menu item
 	 * @param actionCommand
 	 *            the command to execute when the menuitem is pressed
 	 * @param image
 	 *            the image for the checkbox menu item
 	 * @return the newly created checkbox menu item
 	 */
 	private JCheckBoxMenuItem checkBoxMenuItem(String label,
 			String actionCommand, ImageIcon image) {
 		JCheckBoxMenuItem result = new JCheckBoxMenuItem(label);
 		result.setIcon(image);
 		result.setActionCommand(actionCommand);
 		result.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				eventHandler(evt);
 			}
 		});
 		return result;
 	}
 
 	/**
 	 * Create a button from an image (typically for a toolbar)
 	 * 
 	 * @param buttonImage
 	 *            an image icon to paint on the button
 	 * @param actionCommand
 	 *            the command to execute when this button is pressed
 	 * @param toolTipText
 	 *            if non-null, the tool tip for this button
 	 * @param keyEvent
 	 *            the ALT key to set as the shortcut for this button. (Set to
 	 *            KeyEvent.VK_UNDEFINED if none wanted.)
 	 * @return the newly created button
 	 */
 	private JButton JButtonImageItem(ImageIcon buttonImage,
 			String actionCommand, String toolTipText, int keyEvent) {
 		JButton result = new JButton(buttonImage);
 		result.setActionCommand(actionCommand);
 		result.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				eventHandler(evt);
 			}
 		});
 		if (toolTipText != null)
 			result.setToolTipText(toolTipText);
 		result.setMnemonic(keyEvent);
 		return result;
 	}
 
 	/**
 	 * Create and load menu bar.
 	 */
 	private void menuBar(
 			boolean saveToEdXServerEnabled, 
 			boolean graderEnabled, 
 			boolean saveForGradingEnabled) {
 
 		URL openImageURL = VGLII.class.getResource("images/open16.gif"); //$NON-NLS-1$
 		ImageIcon openImage = new ImageIcon(openImageURL);
 
 		URL newImageURL = VGLII.class.getResource("images/new16.gif"); //$NON-NLS-1$
 		ImageIcon newImage = new ImageIcon(newImageURL);
 
 		URL saveAsImageURL = VGLII.class
 				.getResource("images/saveas16.gif"); //$NON-NLS-1$
 		ImageIcon saveAsImage = new ImageIcon(saveAsImageURL);
 
 		URL saveImageURL = VGLII.class.getResource("images/save16.gif"); //$NON-NLS-1$
 		ImageIcon saveImage = new ImageIcon(saveImageURL);
 
 		URL saveToServerImageURL = VGLII.class
 				.getResource("images/edX16.gif");
 		ImageIcon saveToServerImage = new ImageIcon(saveToServerImageURL);
 
 		URL aboutImageURL = VGLII.class.getResource("images/about16.gif"); //$NON-NLS-1$
 		ImageIcon aboutImage = new ImageIcon(aboutImageURL);
 
 		URL printFileImageURL = 
 				VGLII.class.getResource("images/printtofile16.gif"); //$NON-NLS-1$
 		ImageIcon printFileImage = new ImageIcon(printFileImageURL);
 
 		URL printImageURL = VGLII.class.getResource("images/print16.gif"); //$NON-NLS-1$
 		ImageIcon printImage = new ImageIcon(printImageURL);
 
 		URL pageSetupImageURL = VGLII.class
 				.getResource("images/pagesetup16.gif"); //$NON-NLS-1$
 		ImageIcon pageSetupImage = new ImageIcon(pageSetupImageURL);
 
 		URL onlineHelpImageURL = VGLII.class
 				.getResource("images/onlinehelp16.gif"); //$NON-NLS-1$
 		ImageIcon onlineHelpImage = new ImageIcon(onlineHelpImageURL);
 
 		URL closeImageURL = VGLII.class
 				.getResource("images/closework16.gif"); //$NON-NLS-1$
 		ImageIcon closeImage = new ImageIcon(closeImageURL);
 
 		//  "File" options.
 		JMenu mnuFile = new JMenu(Messages.getInstance().getString("VGLII.File"));		 //$NON-NLS-1$
 
 		if (saveToEdXServerEnabled) {
 			newProblemItem = menuItem(Messages.getInstance().getString("VGLII.NewProblem"), "NewProblemFromArgs", newImage); 			
 		} else {
 			newProblemItem = menuItem(Messages.getInstance().getString("VGLII.NewProblem"), "NewProblemFromFile", newImage); 			
 		}
 		openProblemItem = menuItem(Messages.getInstance().getString("VGLII.OpenWork"), "OpenWork", openImage); //$NON-NLS-1$ //$NON-NLS-2$
 		saveProblemItem = menuItem(Messages.getInstance().getString("VGLII.SaveWork"), "SaveWork", saveImage); //$NON-NLS-1$ //$NON-NLS-2$
 		saveProblemAsItem = menuItem(Messages.getInstance().getString("VGLII.SaveWorkAs"), "SaveAs", saveAsImage); //$NON-NLS-1$ //$NON-NLS-2$
 
 		pageSetupItem = menuItem(Messages.getInstance().getString("VGLII.PageSetup"), "PageSetup", pageSetupImage); //$NON-NLS-1$ //$NON-NLS-2$
 		printItem = menuItem(Messages.getInstance().getString("VGLII.PrintWork"), "PrintWork", printImage); //$NON-NLS-1$ //$NON-NLS-2$
 		printToFileItem = menuItem(Messages.getInstance().getString("VGLII.PrintWorkToFile"), "PrintToFile", //$NON-NLS-1$ //$NON-NLS-2$
 				printFileImage);
 		closeProblemItem = menuItem(Messages.getInstance().getString("VGLII.CloseWork"), "CloseWork", closeImage); //$NON-NLS-1$ //$NON-NLS-2$
 		exitItem = menuItem(Messages.getInstance().getString("VGLII.Exit"), "Exit", null); //$NON-NLS-1$ //$NON-NLS-2$
 
 		mnuFile.add(newProblemItem);
 		mnuFile.add(openProblemItem);
 		mnuFile.addSeparator();
 		mnuFile.add(saveProblemItem);
 		mnuFile.add(saveProblemAsItem);
 		if (saveToEdXServerEnabled) {
 			saveToServerItem = menuItem("Save To edX Server...", "SaveToServer", saveToServerImage);
 			mnuFile.add(saveToServerItem);
 		}
 		if (saveForGradingEnabled) {
 			saveForGradingItem = 
 					menuItem(Messages.getInstance().getString("VGLII.SaveForGrading"), "SaveForGrading", null);
 			mnuFile.add(saveForGradingItem);
 		}
 		mnuFile.addSeparator();
 		mnuFile.add(pageSetupItem);
 		mnuFile.add(printItem);
 		mnuFile.add(printToFileItem);
 		mnuFile.addSeparator();
 		mnuFile.add(closeProblemItem);
 		mnuFile.addSeparator();
 		mnuFile.add(exitItem);
 
 		mnuBar.add(mnuFile);
 
 		//  "Utilities" options.
 		JMenu mnuUtilities = new JMenu(Messages.getInstance().getString("VGLII.Utilities")); //$NON-NLS-1$
 
 		crossTwoItem = menuItem(Messages.getInstance().getString("VGLII.CrossTwo"), 
 				"CrossTwo", 
 				null); //$NON-NLS-1$ //$NON-NLS-2$
 		mnuUtilities.add(crossTwoItem);
 
 		superCrossItem = menuItem(Messages.getInstance().getString("VGLII.SuperCross"), 
 				"SuperCross", 
 				null);
 		mnuUtilities.add(superCrossItem);
 
 		mnuUtilities.add(new JSeparator());
 
 		cageManagerItem = menuItem(Messages.getInstance().getString("VGLII.Cages"), 
 				"CageManager", 
 				null); //$NON-NLS-1$ //$NON-NLS-2$
 		mnuUtilities.add(cageManagerItem);
 
 		rearrangeCagesItem = menuItem(Messages.getInstance().getString("VGLII.RearrangeCages"), 
 				"RearrangeCages", //$NON-NLS-1$ //$NON-NLS-2$
 				null);
 		mnuUtilities.add(rearrangeCagesItem);
 
 		mnuUtilities.add(new JSeparator());
 
 		summaryChartItem = menuItem(Messages.getInstance().getString("VGLII.CreateSummaryChart"), 
 				"SummaryChart", //$NON-NLS-1$ //$NON-NLS-2$
 				null);
 		mnuUtilities.add(summaryChartItem);
 
 		unselectAllItem = menuItem(Messages.getInstance().getString("VGLII.UnselectAllCages"), 
 				"UnselectAll", //$NON-NLS-1$ //$NON-NLS-2$
 				null);
 		mnuUtilities.add(unselectAllItem);
 
 		mnuUtilities.add(new JSeparator());
 
 		modelBuilderItem = menuItem(Messages.getInstance().getString("VGLII.ShowModelBuilder"), 
 				"ModelBuilder", 
 				null);
 		mnuUtilities.add(modelBuilderItem);
 
 		if (graderEnabled) {
 			mnuUtilities.add(new JSeparator());
 			graderItem = menuItem("Grade Students' Work", "Grader", null);
 			mnuUtilities.add(graderItem);
 		}
 
 		mnuBar.add(mnuUtilities);
 
 		//  "Help" options.
 		JMenu mnuHelp = new JMenu(Messages.getInstance().getString("VGLII.Help")); //$NON-NLS-1$
 		onlineHelpItem = menuItem(Messages.getInstance().getString("VGLII.HelpPage"), "OnlineHelp", //$NON-NLS-1$ //$NON-NLS-2$
 				onlineHelpImage);
 		mnuHelp.add(onlineHelpItem);
 		mnuHelp.add(menuItem(Messages.getInstance().getString("VGLII.AboutVGL"), "About", //$NON-NLS-1$ //$NON-NLS-2$
 				aboutImage));
 		mnuBar.add(mnuHelp);
 
 		//language options
 		mnuLanguage = new JMenu(Messages.getInstance().getString("VGLII.Language"));
 		for (int i = 0; i < supportedLanguageMenuItems.length; i++) {
 			mnuLanguage.add(supportedLanguageMenuItems[i]);
 			supportedLanguageMenuItems[i].addActionListener(new LanguageMenuItemListener(saveToEdXServerEnabled, graderEnabled, saveForGradingEnabled));
 		}
 		mnuBar.add(Box.createHorizontalGlue());
 		mnuBar.add(mnuLanguage);
 
 		setJMenuBar(mnuBar);
 	}
 
 	private class LanguageMenuItemListener implements ActionListener {
 		private boolean saveToEdXServerEnabled; 
 		private boolean graderEnabled;
 		private boolean saveForGradingEnabled; 
 
 		public LanguageMenuItemListener(
 				boolean saveToEdXServerEnabled, 
 				boolean graderEnabled, 
 				boolean saveForGradingEnabled) {
 			this.saveToEdXServerEnabled = saveToEdXServerEnabled;
 			this.graderEnabled = graderEnabled;
 			this.saveForGradingEnabled = saveForGradingEnabled;
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			LanguageSpecifierMenuItem item = (LanguageSpecifierMenuItem)e.getSource();
 			Locale.setDefault(new Locale(item.getLanguage(), item.getCountry()));
 			Messages.getInstance().updateResourceBundle();
 			mnuBar.removeAll();
 			menuBar(saveToEdXServerEnabled, graderEnabled, saveForGradingEnabled);
 			toolBar.removeAll();
 			toolBar(saveToEdXServerEnabled, graderEnabled, saveForGradingEnabled);
 			cleanUp();
 		}
 	}
 
 	/**
 	 * Create and load status panel
 	 */
 	private void statusPanel() {
 		JPanel sPanel = new JPanel();
 		sPanel.setLayout(new BorderLayout());
 
 		statusLabel = new JLabel();
 		statusLabel.setForeground(Color.black);
 		statusLabel.setBorder(new SoftBevelBorder(1));
 		statusLabel.setText(" "); //$NON-NLS-1$
 		sPanel.add(statusLabel, BorderLayout.CENTER);
 		getContentPane().add(sPanel, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * Create and load toolbar
 	 */
 	private void toolBar(
 			boolean saveToEdXServerEnabled, 
 			boolean graderEnabled, 
 			boolean saveForGradingEnabled) {
 		URL openImageURL = VGLII.class.getResource("images/open.gif"); //$NON-NLS-1$
 		ImageIcon openImage = new ImageIcon(openImageURL);
 
 		URL newImageURL = VGLII.class.getResource("images/new.gif"); //$NON-NLS-1$
 		ImageIcon newImage = new ImageIcon(newImageURL);
 
 		URL saveAsImageURL = VGLII.class.getResource("images/saveas.gif"); //$NON-NLS-1$
 		ImageIcon saveAsImage = new ImageIcon(saveAsImageURL);
 
 		URL saveImageURL = VGLII.class.getResource("images/save.gif"); //$NON-NLS-1$
 		ImageIcon saveImage = new ImageIcon(saveImageURL);
 
 		URL saveToServerImageURL = VGLII.class.getResource("images/edX.gif");
 		ImageIcon saveToServerImage = new ImageIcon(saveToServerImageURL);
 
 		URL aboutImageURL = VGLII.class.getResource("images/about.gif"); //$NON-NLS-1$
 		ImageIcon aboutImage = new ImageIcon(aboutImageURL);
 
 		URL printImageURL = VGLII.class.getResource("images/print.gif"); //$NON-NLS-1$
 		ImageIcon printImage = new ImageIcon(printImageURL);
 
 		URL printFileImageURL = VGLII.class
 				.getResource("images/printtofile.gif"); //$NON-NLS-1$
 		ImageIcon printFileImage = new ImageIcon(printFileImageURL);
 
 		URL onlineHelpImageURL = VGLII.class
 				.getResource("images/onlinehelp.gif"); //$NON-NLS-1$
 		ImageIcon onlineHelpImage = new ImageIcon(onlineHelpImageURL);
 
 		URL closeImageURL = VGLII.class
 				.getResource("images/closework.gif"); //$NON-NLS-1$
 		ImageIcon closeImage = new ImageIcon(closeImageURL);
 
 		URL crossTwoImageURL = VGLII.class.getResource("images/cross.gif"); //$NON-NLS-1$
 		ImageIcon crossTwoImage = new ImageIcon(crossTwoImageURL);
 
 		URL exitImageURL = VGLII.class.getResource("images/exit.gif"); //$NON-NLS-1$
 		ImageIcon exitImage = new ImageIcon(exitImageURL);
 
 		if (saveToEdXServerEnabled) {
 			newButton = JButtonImageItem(newImage, "NewProblemFromArgs", 
 					Messages.getInstance().getString("VGLII.NewProblem"), KeyEvent.VK_N); 
 		} else {
 			newButton = JButtonImageItem(newImage, "NewProblemFromFile", 
 					Messages.getInstance().getString("VGLII.NewProblem"), KeyEvent.VK_N); 
 		}
 		if (saveToEdXServerEnabled) {
 			saveToServerButton = JButtonImageItem(saveToServerImage, "SaveToServer", "Save To EdX Server...", KeyEvent.VK_E);
 		}
 		openButton = JButtonImageItem(openImage, "OpenWork", Messages.getInstance().getString("VGLII.OpenWork"), //$NON-NLS-1$ //$NON-NLS-2$
 				KeyEvent.VK_O);
 		closeButton = JButtonImageItem(closeImage, "CloseWork", //$NON-NLS-1$
 				Messages.getInstance().getString("VGLII.CloseWork"), KeyEvent.VK_L); //$NON-NLS-1$
 		exitButton = JButtonImageItem(exitImage, "Exit", Messages.getInstance().getString("VGLII.Exit"), //$NON-NLS-1$ //$NON-NLS-2$
 				KeyEvent.VK_E);
 		saveButton = JButtonImageItem(saveImage, "SaveWork", Messages.getInstance().getString("VGLII.SaveWork"), //$NON-NLS-1$ //$NON-NLS-2$
 				KeyEvent.VK_S);
 		saveAsButton = JButtonImageItem(saveAsImage, "SaveAs", Messages.getInstance().getString("VGLII.SaveAs"), //$NON-NLS-1$ //$NON-NLS-2$
 				KeyEvent.VK_V);
 		crossTwoButton = JButtonImageItem(crossTwoImage, "CrossTwo", //$NON-NLS-1$
 				Messages.getInstance().getString("VGLII.CrossTwo"), KeyEvent.VK_C); //$NON-NLS-1$
 		aboutButton = JButtonImageItem(aboutImage, "About", //$NON-NLS-1$
 				Messages.getInstance().getString("VGLII.AboutVGL"), KeyEvent.VK_A); //$NON-NLS-1$
 
 		printButton = JButtonImageItem(printImage, "PrintWork", //$NON-NLS-1$
 				Messages.getInstance().getString("VGLII.PrintWork"), KeyEvent.VK_P); //$NON-NLS-1$
 
 		printToFileButton = JButtonImageItem(printFileImage, "PrintToFile", //$NON-NLS-1$
 				Messages.getInstance().getString("VGLII.PrintWorkToFile"), KeyEvent.VK_F); //$NON-NLS-1$
 		onlineHelpButton = JButtonImageItem(onlineHelpImage, "OnlineHelp", //$NON-NLS-1$
 				Messages.getInstance().getString("VGLII.HelpPage"), KeyEvent.VK_H); //$NON-NLS-1$
 
 		toolBar.add(newButton);
 		toolBar.add(openButton);
 		toolBar.add(closeButton);
 		toolBar.add(exitButton);
 		toolBar.add(saveButton);
 		toolBar.add(saveAsButton);
 		toolBar.add(printButton);
 		if (saveToServerButton != null)	toolBar.add(saveToServerButton);
 		toolBar.add(crossTwoButton);
 		toolBar.add(onlineHelpButton);
 		toolBar.add(aboutButton);
 	}
 
 	/**
 	 * Create and load all GUI components
 	 */
 	private void setupUI(
 			boolean saveToEdXServerEnabled, 
 			boolean graderEnabled, 
 			boolean saveForGradingEnabled) {
 		mnuBar = new JMenuBar();
 		menuBar(saveToEdXServerEnabled, graderEnabled, saveForGradingEnabled);
 		statusPanel();
 		toolBar = new JToolBar();
 		toolBar(saveToEdXServerEnabled, graderEnabled, saveForGradingEnabled);
 		JPanel panePanel = new JPanel();
 		panePanel.setLayout(new BorderLayout());
 		panePanel.add(toolBar, BorderLayout.NORTH);
 		getContentPane().add(panePanel, BorderLayout.CENTER);
 		cleanUp();
 		docRenderer = new DocumentRenderer(); //setup for printing
 
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		this.setSize(dim.width, (int)(dim.height * 0.9));
 	}
 
 	/**
 	 * Display about dialog
 	 */
 	private void aboutVGL() {
 		AboutVGLLabel.showAboutVGLLabel(this);
 	}
 
 	/**
 	 * Generic file chooser method
 	 * 
 	 * @param workingDir
 	 *            the directory to open into
 	 * @param dialogTitle
 	 *            the title for the file dialog
 	 * @param approveTip
 	 *            tool tip text for approve button of file dialog
 	 * @param useAllFilter
 	 *            true if "show all files" mode is needed, false otherwise
 	 * @param filefilter
 	 *            filter for the file types to be displayed in the dialog
 	 * @param filterTip
 	 *            description information about filefilter
 	 * @param dialogType
 	 *            an int value to decide the type of dialog.
 	 * @return
 	 */
 	private File selectFile(File workingDir, String dialogTitle,
 			String approveTip, boolean useAllFilter, String filefilter,
 			String filterTip, int dialogType) {
 		File result = null;
 		m_FChooser = null;
 		m_FChooser = new JFileChooser(workingDir);
 		m_FChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		javax.swing.filechooser.FileFilter ft = m_FChooser
 				.getAcceptAllFileFilter();
 		m_FChooser.removeChoosableFileFilter(ft);
 		if (dialogType != -1)
 			m_FChooser.setDialogType(dialogType);
 		if (dialogTitle != null)
 			m_FChooser.setDialogTitle(dialogTitle);
 		if (approveTip != null)
 			m_FChooser.setApproveButtonToolTipText(approveTip);
 
 		if (filefilter != null) {
 			CustomizedFileFilter filter = new CustomizedFileFilter(filefilter,
 					filterTip);
 			m_FChooser.addChoosableFileFilter(filter);
 		}
 		if (dialogType == JFileChooser.OPEN_DIALOG) {
 			if (m_FChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
 				result = m_FChooser.getSelectedFile();
 		} else if (dialogType == JFileChooser.SAVE_DIALOG) {
 			if (m_FChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
 				result = m_FChooser.getSelectedFile();
 		} else if (dialogType == -1) {
 			if (m_FChooser.showDialog(this, "Print") == JFileChooser.APPROVE_OPTION) //$NON-NLS-1$
 				result = m_FChooser.getSelectedFile();
 		}
 		update(getGraphics());
 
 		//need to kill the dialog so it won't re-appear on de-iconify
 		Window[] windows = this.getOwnedWindows();
 		for (int i = 0; i < windows.length; i++) {
 			if (windows[i].toString().contains(
 					Messages.getInstance().getString("VGLII.NewProbTypeSel"))) { //$NON-NLS-1$
 				windows[i].dispose();
 			}
 		}
 
 		// if it's a save or print dialog, need to be sure no 'poison' characters in filename
 		if ((dialogType != JFileChooser.OPEN_DIALOG) && (result != null)) {
 			String originalName = result.getName();
 			Pattern poisonChars = Pattern.compile("[/!@#$%^&(){}~*\\[\\]]+");
 			Matcher poisonFinder = poisonChars.matcher(originalName);
 			if (poisonFinder.find()) {
 				Matcher fixer = poisonChars.matcher(originalName);
 				String newName = fixer.replaceAll("_");
 				result = new File(result.getParent() + System.getProperty("file.separator") + newName);
 
 				int choice = JOptionPane.showConfirmDialog(
 						this,
 						"<html>" 
 								+ Messages.getInstance().getString("VGLII.BadFileName1")
 								+ "<br>" 
 								+ Messages.getInstance().getString("VGLII.BadFileName2")
 								+ "<br>" 
 								+ Messages.getInstance().getString("VGLII.BadFileName3")
 								+ ": " 
 								+ result.getName()
 								+ "</html>",
 								Messages.getInstance().getString("VGLII.Error"),
 								JOptionPane.YES_NO_OPTION);
 
 				if (choice == JOptionPane.NO_OPTION) {
 					result = null;
 				}
 			}
 		}
 
 		return result;
 	}
 
 
 	/**
 	 * Method to set up a new problem for the user
 	 * based on a file
 	 */
 	private void newProblemFromFile(String problemFileName) {
 		problemFile = null;
 
 		if (cageCollection == null) {
 			if (problemFileName == null) {
 				File problemsDirectory = new File(defaultProblemDirectory.toString()
 						+ System.getProperty("file.separator") + "Problems"); //$NON-NLS-1$ //$NON-NLS-2$
 				if (!problemsDirectory.exists()) {
 					problemsDirectory = defaultProblemDirectory;
 				}
 				problemFile = selectFile(problemsDirectory,
 						Messages.getInstance().getString("VGLII.NewProbTypeSel"), 
 						Messages.getInstance().getString("VGLII.SelProbType"), false, //$NON-NLS-1$ //$NON-NLS-2$
 						prbFilterString, Messages.getInstance().getString("VGLII.ProTypeFiles"), //$NON-NLS-1$
 						JFileChooser.OPEN_DIALOG);
 			} else {
 				problemFile = new File(problemFileName);
 			}
 
 			if (problemFile == null) return;
 			if (!problemFile.exists()) return;
 
 			//refresh possible characters and traits & image defaults
 			CharacterSpecificationBank.getInstance().refreshAll();
 			PhenotypeImageBank.getInstance().resetDefaults();
 			geneticModel = 
 					GeneticModelFactory.getInstance().createRandomModel(problemFile);
 			if (geneticModel == null) return;
 			geneticModel.setProblemFileName(problemFile.getName());			
 			startNewProblem();
 		}
 	}
 
 	/*
 	 * set up new problem based on command line params
 	 */
 	private void newProblemFromArgs() {
 		//refresh possible characters and traits & image defaults
 		CharacterSpecificationBank.getInstance().refreshAll();
 		PhenotypeImageBank.getInstance().resetDefaults();
 		geneticModel = 
 				GeneticModelFactory.getInstance().createRandomModel(args);
 		if (geneticModel == null) return;
 
 		int choice = CustomDialogs.getPracticeModeChoice(this);
 		switch (choice) {
 
 		case CustomDialogs.PMODE_PRACTICE:
 			geneticModel.setBeginnerMode(true);
 			break;
 
 		case CustomDialogs.PMODE_REGULAR:
 			geneticModel.setBeginnerMode(false);
 			break;
 
 		case CustomDialogs.PMODE_CANCEL:
 			return;
 
 		default: return;
 		}
 		
 		startNewProblem();
 	}
 
 	private void startNewProblem() {
 		if (geneticModel == null) return;
 
 		nextCageId = 0;
 		selectionVial = new SelectionVial(statusLabel);
 		cageCollection = new ArrayList<CageUI>();
 
 		Cage fieldPop = geneticModel.generateFieldPopulation();
 		createCageUI(fieldPop, false);
 		enableAll(true);
 		disableLanguageMenu();
 
 		if ((geneticModel.getProblemTypeSpecification().getEdXServerStrings() == null) ||
 				geneticModel.isBeginnerMode()) {
 			if (saveToServerItem != null) saveToServerItem.setEnabled(false);
 			if (saveToServerButton != null) saveToServerButton.setEnabled(false);
 		} else {
 			if (saveToServerItem != null) saveToServerItem.setEnabled(true);
 			if (saveToServerButton != null) saveToServerButton.setEnabled(true);
 		}
 
 		modelBuilderDialog = new JDialog(this);
 		modelBuilder = new ModelBuilderUI(modelBuilderDialog, this, geneticModel);
 		modelBuilderDialog.setTitle(Messages.getInstance().getString("VGLII.ModelBuilder"));
 		modelBuilderDialog.add(modelBuilder);
 		modelBuilderDialog.pack();
 		modelBuilderDialog.setLocation(300, 300);
 
 		changeSinceLastSave = true;
 	}
 
 	/**
 	 * Opens up an existing saved problem, sets up the model, and opens up all
 	 * the cages of that problem.
 	 */
 	public void openProblem(String workFileName) {	
 		File workFile = null;
 
 		selectionVial = new SelectionVial(statusLabel);
 		SavedWorkFileData result = null;
 
 		if (workFileName == null) {
 			workFile = selectFile(desktopDirectory, Messages.getInstance().getString("VGLII.OpenWork"), //$NON-NLS-1$
 					Messages.getInstance().getString("VGLII.SelWrkFile"), false, wrkFilterString, 
 					Messages.getInstance().getString("VGLII.WorkFiles"), //$NON-NLS-1$ //$NON-NLS-2$
 					JFileChooser.OPEN_DIALOG);
 		} else {	
 			workFile = new File(workFileName);
 		}
 
 		if (workFile == null) return;
 		if (!workFile.exists()) return;
 
 		try {
 			result = GeneticModelFactory.getInstance().readModelFromXORFile(workFile);
 			if (result == null) return;
 
 			PhenotypeImageBank.getInstance().resetDefaults();
 			geneticModel = result.getGeneticModel();
 			cageCollection = new ArrayList<CageUI>();
 			nextCageId = 0;
 			reopenCages(result.getCages());
 			enableAll(true);
 			disableLanguageMenu();
 			problemFile = new File(geneticModel.getProblemFileName());
 		} catch (Exception e) {
 			System.out.print(e.getMessage());
 		}
 
 		/**
 		 *  problems saved with older versions won't have the
 		 *    model specs saved in the problem file
 		 *    so you can't have the model builder
 		 *    - so disable it
 		 */
 		if (geneticModel.getProblemTypeSpecification() != null) {
 			modelBuilderDialog = new JDialog(this);
 			modelBuilder = new ModelBuilderUI(modelBuilderDialog, this, geneticModel);
 			modelBuilder.configureFromFile(result.getModelBuilderState());
 			modelBuilderDialog.setTitle(Messages.getInstance().getString("VGLII.ModelBuilder"));
 			modelBuilderDialog.add(modelBuilder);
 			modelBuilderDialog.pack();
 			modelBuilderDialog.setVisible(modelBuilder.getDesiredVisibility());
 			modelBuilderDialog.setLocation(modelBuilder.getDesiredXpos(), modelBuilder.getDesiredYpos());
 		} else {
 			modelBuilderItem.setEnabled(false);
 		}
 		currentSavedFile = workFile; // save now to file you just loaded
 		changeSinceLastSave = false;
 
 		if ((geneticModel.getProblemTypeSpecification().getEdXServerStrings() == null) ||
				geneticModel.isBeginnerMode()) {
			if (saveToServerItem != null) saveToServerItem.setEnabled(false);
			if (saveToServerButton != null) saveToServerButton.setEnabled(false);
		} else {
			if (saveToServerItem != null) saveToServerItem.setEnabled(true);
			if (saveToServerButton != null) saveToServerButton.setEnabled(true);
 		}
 	}
 
 	/**
 	 * Saves the current work done by the user to a file.
 	 */
 	private void saveProblem() {
 		if (cageCollection != null) {
 			if (currentSavedFile == null)
 				currentSavedFile = selectFile(desktopDirectory,
 						Messages.getInstance().getString("VGLII.SaveWork"), 
 						Messages.getInstance().getString("VGLII.EnterSaveFileName"), false, //$NON-NLS-1$ //$NON-NLS-2$
 						wrkFilterString, Messages.getInstance().getString("VGLII.WorkFiles"), //$NON-NLS-1$
 						JFileChooser.SAVE_DIALOG);
 			try {
 				Iterator<CageUI> it = cageCollection.iterator();
 				ArrayList<Cage> al = new ArrayList<Cage>();
 				while (it.hasNext()) {
 					CageUI cui = it.next();
 					Cage c = cui.getCage();
 					al.add(c);
 				}
 				if (currentSavedFile != null) {
 					if (!currentSavedFile.getPath().endsWith(wrkFilterString)) {
 						currentSavedFile = convertTo(currentSavedFile,
 								"." + wrkFilterString); //$NON-NLS-1$
 					}
 
 					Document doc = getXMLDoc(al); 
 					EncryptionTools.getInstance().saveXOREncrypted(doc, currentSavedFile);
 					changeSinceLastSave = false;
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * saves for grading using rsa key
 	 * 
 	 */
 	private void saveForGrading() {
 		if (cageCollection != null) {
 			File fileForGrading = selectFile(desktopDirectory,
 					Messages.getInstance().getString("VGLII.SaveForGrading"), 
 					Messages.getInstance().getString("VGLII.EnterSaveFileName"), false, //$NON-NLS-1$ //$NON-NLS-2$
 					saveForGradingFilterString, Messages.getInstance().getString("VGLII.GradingFiles"), //$NON-NLS-1$
 					JFileChooser.SAVE_DIALOG);
 			try {
 				Iterator<CageUI> it = cageCollection.iterator();
 				ArrayList<Cage> al = new ArrayList<Cage>();
 				while (it.hasNext()) {
 					CageUI cui = it.next();
 					Cage c = cui.getCage();
 					al.add(c);
 				}
 				if (fileForGrading != null) {
 					if (!fileForGrading.getPath().endsWith(saveForGradingFilterString)) {
 						fileForGrading = convertTo(fileForGrading,
 								"." + saveForGradingFilterString); //$NON-NLS-1$
 					}
 
 					Document doc = getGradingDoc(al); 
 					EncryptionTools.getInstance().saveRSAEncrypted(
 							doc, fileForGrading, saveForGradingKey);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	private Document getGradingDoc(ArrayList<Cage> cages) {
 		Element root = new Element("GraderInfo");
 
 		Element studentAnswer = new Element("StudentAnswer");
 		CageScorer cageScorer = new CageScorer(cages, modelBuilder);
 		studentAnswer.addContent(modelBuilder.getAsHtml(true)		
 				+ cageScorer.getCageScores());
 		root.addContent(studentAnswer);
 
 		Element correctAnswer = new Element("CorrectAnswer");
 		correctAnswer.addContent(geneticModel.getHTMLForGrader());
 		root.addContent(correctAnswer);
 
 		return new Document(root);
 	}
 
 	private Document getXMLDoc(ArrayList<Cage> cages) throws Exception {
 		// creating the whole tree
 		Element root = new Element("VglII"); //$NON-NLS-1$
 
 		Element pfn = new Element("ProbFileName");
 		if (problemFile == null) { 	// if it was started from command line args
 			pfn.addContent("Command Line Argument Problem");
 		} else {
 			pfn.addContent(problemFile.getName());
 		}
 		root.addContent(pfn);
 
 		root.addContent(geneticModel.save());
 		Element organisms = new Element("Organisms"); //$NON-NLS-1$
 		for (int i = 0; i < cages.size(); i++) {
 			Cage c = cages.get(i);
 			organisms.addContent(c.save());
 		}
 		root.addContent(organisms);
 
 		root.addContent(modelBuilder.save());
 
 		Document doc = new Document(root);
 		return doc;
 	}
 
 
 	/**
 	 * Same as saveProblem, with current file set to null, so as to enable
 	 * saving to new file
 	 */
 	private void saveAsProblem() {
 		currentSavedFile = null;
 		saveProblem();
 	}
 
 	/**
 	 * save to server for automated grading by edX
 	 * only available if built with saveToServerEnabled = true;
 	 */
 	private void saveToServer() {
 
 		if (cageCollection == null) {
 			JOptionPane.showMessageDialog(this, "Nothing to save to server.");
 			return;
 		} else {
 			System.out.println("edX strings:\n" + geneticModel.getProblemTypeSpecification().getEdXServerStrings().toString() + "\n********");
 
 			Iterator<CageUI> it = cageCollection.iterator();
 			ArrayList<Cage> cages = new ArrayList<Cage>();
 			while (it.hasNext()) {
 				CageUI cui = it.next();
 				Cage c = cui.getCage();
 				cages.add(c);
 			}
 			Document doc = null;
 			try {
 				doc = getXMLDoc(cages);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			if (doc != null) {
 
 				Element root = doc.getRootElement();
 				root.addContent(AutoGrader.grade(cageCollection, geneticModel, modelBuilder));
 				XMLOutputter outputter = 
 						new XMLOutputter(Format.getPrettyFormat());
 				String xmlString = outputter.outputString(doc);
 				EdXServerUtils.saveToEdXServer(this, geneticModel, eMailAndPassword, xmlString);
 			}
 		}
 	}
 
 
 	/**
 	 * Prints the current work done by the user to a .html file
 	 */
 	private void printToFile() {
 		if (cageCollection != null) {
 			File printFile = selectFile(desktopDirectory,
 					Messages.getInstance().getString("VGLII.PrintWorkToFile"), 
 					Messages.getInstance().getString("VGLII.EnterPrintFileName"), 
 					false, //$NON-NLS-1$ //$NON-NLS-2$
 					printFilterString, Messages.getInstance().getString("VGLII.PrintFiles"), -1); //$NON-NLS-1$
 			if (printFile != null) {
 				if (!printFile.getPath().endsWith(".html")) //$NON-NLS-1$
 					printFile = convertTo(printFile, ".html"); //$NON-NLS-1$
 				createHTMLFile(printFile);
 			}
 		}
 	}
 
 	private void createHTMLFile(File printFile) {
 		printFile.delete();
 		try {
 			printFile.createNewFile();
 			OutputStreamWriter op = 
 					new OutputStreamWriter(
 							new BufferedOutputStream(
 									new FileOutputStream(printFile)),"ISO8859_1");
 			op.write(GetWorkAsHTML.getWorkAsHTML(cageCollection, modelBuilder));
 			op.flush();
 			op.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Sets up the page for printing
 	 */
 	private void pageSetup() {
 		docRenderer.pageDialog();
 	}
 
 	/**
 	 * Prints the current work to the printer
 	 */
 	private void print() {
 		docRenderer = new DocumentRenderer();
 		docRenderer.setScaleWidthToFit(true);
 		JTextPane printTextPane = new JTextPane();
 		printTextPane.setContentType("text/html"); //$NON-NLS-1$
 		printTextPane.setText(GetWorkAsHTML.getWorkAsHTML(cageCollection, modelBuilder));
 		HTMLDocument htDoc = (HTMLDocument) printTextPane.getDocument();
 		docRenderer.print(htDoc);
 	}
 
 
 	/**
 	 * Closes the problem that the user has been working on so far and releases
 	 * all related objects
 	 */
 	private void closeProblem() {
 		if (!changeSinceLastSave) {
 			cleanUp();
 			return;
 		}
 		if (cageCollection != null) {
 			int ans1 = JOptionPane.showConfirmDialog(this,
 					Messages.getInstance().getString("VGLII.ClosingWarningLine1") //$NON-NLS-1$
 					+ "\n"
 					+ Messages.getInstance().getString("VGLII.ClosingWarningLine2"), //$NON-NLS-1$
 					Messages.getInstance().getString("VGLII.CloseWork"), JOptionPane.YES_NO_CANCEL_OPTION, //$NON-NLS-1$
 					JOptionPane.WARNING_MESSAGE);
 			if (ans1 == JOptionPane.YES_OPTION) {
 				saveProblem();
 				cleanUp();
 			}
 			if (ans1 == JOptionPane.NO_OPTION) 
 				cleanUp();
 			if (ans1 != JOptionPane.CANCEL_OPTION)
 				return;
 		}
 	}
 
 	/**
 	 * Exits the application after doing the necessary cleanup
 	 */
 	private void exitApplication() {
 		if (!changeSinceLastSave) {
 			cleanUp();
 			System.exit(0);
 		}
 		if (cageCollection != null) {
 
 			int ans = JOptionPane.showConfirmDialog(this,
 					Messages.getInstance().getString("VGLII.QuitWarningLine1") //$NON-NLS-1$
 					+ "\n"
 					+ Messages.getInstance().getString("VGLII.QuitWarningLine2"), //$NON-NLS-1$
 					Messages.getInstance().getString("VGLII.ExitVGL"), JOptionPane.YES_NO_CANCEL_OPTION, //$NON-NLS-1$
 					JOptionPane.WARNING_MESSAGE);
 			if (ans == JOptionPane.YES_OPTION) {
 				saveProblem();
 				cleanUp();
 				System.exit(0);
 			}
 			if (ans != JOptionPane.CANCEL_OPTION) {
 				cleanUp();
 				System.exit(0);
 			}
 		} else {
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * Method to release temporary objects and re-initialize objects and
 	 * variables before exiting the application or after closing a problem
 	 */
 	public void cleanUp() {
 		if (cageCollection != null) {
 			Iterator<CageUI> it = cageCollection.iterator();
 			while (it.hasNext()) {
 				CageUI c = it.next();
 				it.remove();
 				c.setVisible(false);
 			}
 		}
 		cageCollection = null;
 		geneticModel = null;
 		selectionVial = null;
 		currentSavedFile = null;
 		nextCageId = 1;
 		enableAll(false);
 		nextCageScreenPosition = new Point(this.getX() + 200,
 				this.getY() + 100);
 		statusLabel.setText(""); //$NON-NLS-1$
 		SummaryChartManager.getInstance().clearSelectedSet();
 		SummaryChartManager.getInstance().hideSummaryChart();
 		if (modelBuilder != null) {
 			modelBuilder.setVisible(false);
 		}
 		if (modelBuilderDialog != null) {
 			modelBuilderDialog.dispose();
 		}
 	}
 
 	/**
 	 * Method that actually sets up the cross between two organisms
 	 */
 	private void crossTwo(boolean isSuperCross) {
 		OrganismUI organismUI1 = selectionVial.getMaleParent();
 		OrganismUI organismUI2 = selectionVial.getFemaleParent();
 		if (organismUI1 != null && organismUI2 != null) {
 			Organism o1 = organismUI1.getOrganism();
 			Organism o2 = organismUI2.getOrganism();
 
 			int numOffspring = 0;
 			if (isSuperCross) {
 				Integer numSelected = (Integer)JOptionPane.showInputDialog(null, 
 						Messages.getInstance().getString("VGLII.SuperCrossMessage"),
 						Messages.getInstance().getString("VGLII.SuperCross"),
 						JOptionPane.PLAIN_MESSAGE,
 						null,
 						new Object[] {
 					new Integer(100),
 					new Integer(200),
 					new Integer(500),
 					new Integer(1000),
 					new Integer(2000)
 				},
 				new Integer(100));
 				if (numSelected == null) return;
 				numOffspring = numSelected.intValue();
 			} else {
 				numOffspring = random.nextInt(geneticModel.getMaxOffspring() - geneticModel.getMinOffspring())
 						+ geneticModel.getMinOffspring();
 			}
 
 			Cage c = geneticModel.crossTwo(nextCageId, 
 					o1, 
 					o2, 
 					numOffspring,
 					isSuperCross);
 
 			CageUI cageUI = createCageUI(c, isSuperCross);
 			OrganismUI[] parentUIs = cageUI.getParentUIs();
 			if (parentUIs[0].getOrganism().isMale() == o1.isMale()) {
 				organismUI1.getReferencesList().add(parentUIs[0]);
 				organismUI2.getReferencesList().add(parentUIs[1]);
 				parentUIs[0].setCentralOrganismUI(organismUI1);
 				parentUIs[1].setCentralOrganismUI(organismUI2);
 			} else {
 				organismUI1.getReferencesList().add(parentUIs[1]);
 				organismUI2.getReferencesList().add(parentUIs[0]);
 				parentUIs[1].setCentralOrganismUI(organismUI1);
 				parentUIs[0].setCentralOrganismUI(organismUI2);
 			}
 			modelBuilder.updateCageChoices(nextCageId);
 			changeSinceLastSave = true;
 		} else {
 			JOptionPane.showMessageDialog(this, Messages.getInstance().getString("VGLII.VGLII") //$NON-NLS-1$
 					+ "\n"
 					+ Messages.getInstance().getString("VGLII.CrossWarningLine1") //$NON-NLS-1$
 					+ "\n"
 					+ Messages.getInstance().getString("VGLII.CrossWarningLine2"), //$NON-NLS-1$
 					Messages.getInstance().getString("VGLII.CrossTwo"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * This method invokes .html help into a JEditor pane
 	 */
 	private void onlineHelp() {
 		ShowHelpInfo.showHelpInfo(this);
 	}
 
 	/**
 	 * sets up the cage manager dialog and displays it
 	 */
 	private void cageManager() {
 		CageManager dlg = new CageManager(
 				this, Messages.getInstance().getString("VGLII.Cages"), 
 				cageCollection); //$NON-NLS-1$
 		dlg.setVisible(true);
 		dlg = null;
 	}
 
 	/**
 	 * sets up and displays new summarychart
 	 */
 	private void summaryChart() {
 		SummaryChartManager.getInstance().showSummaryChart(this);
 	}
 
 	/**
 	 * clears selected cages for summary chart
 	 */
 	private void unselectAll() {
 		SummaryChartManager.getInstance().clearSelectedSet();
 	}
 
 	/**
 	 * This method acutally sets up the Cage's UI.
 	 * 
 	 * @param c
 	 *            The cage object whose UI is to be created
 	 * @return the newly created cageUI
 	 */
 	private CageUI createCageUI(Cage c, boolean isSuperCross) {
 		CageUI dlg = null;
 		String details = null;
 		details = geneticModel.toString();
 		dlg = new CageUI(this, 
 				geneticModel.isBeginnerMode(), 
 				isSuperCross,
 				c, 
 				selectionVial,
 				details, 
 				geneticModel.getNumberOfCharacters(),
 				geneticModel.getScrambledCharacterOrder());
 		nextCageId++;
 		if (dlg != null) {
 			cageCollection.add(dlg);
 			calculateCagePosition(dlg);
 			dlg.setVisible(true);
 		}
 		c.setCageUI(dlg);
 		return dlg;
 	}
 
 	/**
 	 * Method to toggle the enabled state of the various menu and button widgets
 	 * 
 	 * @param value
 	 */
 	private void enableAll(boolean value) {
 		if (newButton != null) newButton.setEnabled(!value);
 		openButton.setEnabled(!value);
 		if (newProblemItem != null) newProblemItem.setEnabled(!value);
 		openProblemItem.setEnabled(!value);
 		printItem.setEnabled(value);
 		printButton.setEnabled(value);
 		printToFileItem.setEnabled(value);
 		printToFileButton.setEnabled(value);
 		saveButton.setEnabled(value);
 		saveAsButton.setEnabled(value);
 		crossTwoButton.setEnabled(value);
 		superCrossItem.setEnabled(value);
 		cageManagerItem.setEnabled(value);
 		rearrangeCagesItem.setEnabled(value);
 		saveProblemItem.setEnabled(value);
 		saveProblemAsItem.setEnabled(value);
 		if (saveForGradingItem != null) saveForGradingItem.setEnabled(value);
 		if (saveToServerItem != null) saveToServerItem.setEnabled(value);
 		closeProblemItem.setEnabled(value);
 		closeButton.setEnabled(value);
 		crossTwoItem.setEnabled(value);
 		modelBuilderItem.setEnabled(value);
 	}
 
 	/**
 	 * This is a correction method to correct the file extensions if the user
 	 * did not enter them correctly
 	 * 
 	 * @param thisFile
 	 *            the file
 	 * @param suffix
 	 *            the extension to be given to the file
 	 * @return
 	 */
 	private File convertTo(File thisFile, String suffix) {
 		int endIndex = thisFile.getName().indexOf('.');
 		String name = null;
 		if (endIndex >= 0)
 			name = thisFile.getPath().substring(0, endIndex);
 		else
 			name = thisFile.getPath();
 		name = name + suffix;
 		thisFile.delete();
 		thisFile = new File(name);
 		return thisFile;
 	}
 
 	/**
 	 * This method iterates over the collection of cage objects and sets up the
 	 * UI for each of the cages. This method is invoked when an saved problem is
 	 * reopened for work
 	 * 
 	 * @param cages
 	 *            the list of cages
 	 * @throws Exception
 	 *             in case any or all of the cages are not correct
 	 */
 	private void reopenCages(ArrayList<Cage> cages) throws Exception {
 		Iterator<Cage> it = cages.iterator();
 		while (it.hasNext()) {
 			Cage c = it.next();
 			CageUI cageUI = createCageUI(c, c.isSuperCross());
 
 			/*
 			 *  see if the location and visibility have been saved
 			 *  if not, calculate them
 			 */
 			if (c.getXpos() == -1) {
 				calculateCagePosition(cageUI);
 			} else {
 				cageUI.setLocation(c.getXpos(), c.getYpos());
 			}
 			cageUI.setVisible(c.isVisible());
 
 			if (c.getId() > 0) {
 				OrganismUI[] parentUIs = cageUI.getParentUIs();
 				if (parentUIs == null)
 					System.out.println(Messages.getInstance().getString("VGLII.NoParentsWarning") //$NON-NLS-1$
 							+ " #:"
 							+ c.getId());
 				if (parentUIs[0] == null)
 					System.out.println(Messages.getInstance().getString("VGLII.NoParent0Warning") //$NON-NLS-1$
 							+ " #:"
 							+ c.getId());
 				if (parentUIs[1] == null)
 					System.out.println(Messages.getInstance().getString("VGLII.NoParent1Warning") //$NON-NLS-1$
 							+ " #:"
 							+ c.getId());
 				Organism o1 = parentUIs[0].getOrganism();
 				Organism o2 = parentUIs[1].getOrganism();
 				int o1_Id = o1.getId();
 				int o2_Id = o2.getId();
 				CageUI cage1 = (CageUI) cageCollection.get(o1.getCageId());
 				CageUI cage2 = (CageUI) cageCollection.get(o2.getCageId());
 				if (cage1 != null && cage2 != null) {
 					OrganismUI originalOUI1 = cage1.getOrganismUIFor(o1_Id);
 					OrganismUI originalOUI2 = cage2.getOrganismUIFor(o2_Id);
 					if (originalOUI1 != null && originalOUI2 != null) {
 						if (parentUIs[0].getOrganism().isMale() == originalOUI1
 								.getOrganism().isMale()) {
 							originalOUI1.getReferencesList().add(parentUIs[0]);
 							originalOUI2.getReferencesList().add(parentUIs[1]);
 							parentUIs[0].setCentralOrganismUI(originalOUI1);
 							parentUIs[1].setCentralOrganismUI(originalOUI2);
 						} else {
 							originalOUI1.getReferencesList().add(parentUIs[1]);
 							originalOUI2.getReferencesList().add(parentUIs[0]);
 							parentUIs[1].setCentralOrganismUI(originalOUI1);
 							parentUIs[0].setCentralOrganismUI(originalOUI2);
 						}
 					} else {
 						System.out
 						.println(Messages.getInstance().getString("VGLII.ForOrgs") //$NON-NLS-1$
 								+ "#:"
 								+ c.getId());
 						if (originalOUI1 == null)
 							System.out.println(Messages.getInstance().getString("VGLII.OrgFor") + ": " + o1.getId() //$NON-NLS-1$
 									+ " " + o1.getCageId() + " " + Messages.getInstance().getString("VGLII.NotFound") + " !"); //$NON-NLS-1$ //$NON-NLS-2$
 						if (originalOUI2 == null)
 							System.out.println(Messages.getInstance().getString("VGLII.OrgFor") + ": " + o2.getId() //$NON-NLS-1$
 									+ " " + o2.getCageId() + " " + Messages.getInstance().getString("VGLII.NotFound") + " !"); //$NON-NLS-1$ //$NON-NLS-2$
 					}
 				} else {
 					System.out.println(Messages.getInstance().getString("VGLII.ForParentsOfCage") + "#: " + c.getId()); //$NON-NLS-1$
 					if (cage1 == null)
 						System.out.println(Messages.getInstance().getString("VGLII.CageForOrg") + o1.getId() //$NON-NLS-1$
 								+ " " + o1.getCageId() + " " + Messages.getInstance().getString("VGLII.NotFound") + " !"); //$NON-NLS-1$ //$NON-NLS-2$
 					if (cage2 == null)
 						System.out.println(Messages.getInstance().getString("VGLII.CageForOrg") + o2.getId() //$NON-NLS-1$
 								+ " " + o2.getCageId() + " " + Messages.getInstance().getString("VGLII.NotFound") + " !"); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 			}
 		}
 	}
 
 	/**
 	 * Method to calculate the position of a cage on the screen
 	 * 
 	 * @param cageUI
 	 *            the cage whose position needs to be calculated
 	 */
 	private void calculateCagePosition(CageUI cageUI) {
 		Dimension cageSize = cageUI.getSize();
 		Dimension screenSize = this.getSize();
 		int positionX = (int) nextCageScreenPosition.getX();
 		int positionY = (int) nextCageScreenPosition.getY();
 		if ((positionX + cageSize.getWidth() > screenSize.getWidth())
 				|| (positionY + cageSize.getHeight() > screenSize.getHeight())) {
 			nextCageScreenPosition = new Point(this.getX() + 200,
 					this.getY() + 100);
 			positionX = (int) nextCageScreenPosition.getX();
 			positionY = (int) nextCageScreenPosition.getY();
 		}
 		nextCageScreenPosition = new Point(positionX + 30, positionY + 30);
 		cageUI.setLocation(positionX, positionY);
 	}
 
 	/**
 	 * This method rearranges the current list of cages in a proper fashion
 	 */
 	private void reArrangeCages() {
 		Dimension screenSize = this.getSize();
 		Iterator<CageUI> it = cageCollection.iterator();
 		nextCageScreenPosition = new Point(this.getX() + 200,
 				this.getY() + 100);
 		double positionX;
 		double positionY;
 		Dimension cageSize;
 		while (it.hasNext()) {
 			CageUI cageUI = it.next();
 			positionX = nextCageScreenPosition.getX();
 			positionY = nextCageScreenPosition.getY();
 			cageSize = cageUI.getSize();
 			if ((positionX + cageSize.getWidth() > screenSize.getWidth())
 					|| (positionY + cageSize.getHeight() > screenSize
 							.getHeight())) {
 				nextCageScreenPosition = new Point(this.getX() + 200, this
 						.getY() + 100);
 			} else
 				nextCageScreenPosition = new Point((int) positionX + 30,
 						(int) positionY + 30);
 			cageUI.setLocation((int) positionX, (int) positionY);
 			if (cageUI.isVisible())
 				cageUI.setVisible(true);
 		}
 		CageUI lastCageUI = (CageUI) cageCollection.get(cageCollection
 				.size() - 1);
 		nextCageScreenPosition = new Point(lastCageUI.getX(), lastCageUI
 				.getY());
 	}
 
 
 	/*
 	 * get list of current cages 
 	 *    needed to create evidenitary cage list for 
 	 *    ModelBuilder's panels
 	 */
 	public String[] getCageList() {
 		int numCages = 0;
 		if (cageCollection != null) {
 			numCages= cageCollection.size();
 		} 
 
 		String[] list = new String[numCages + 1];
 		list[0] = "?";
 		for (int i = 1; i < numCages + 1; i++) {
 			list[i] = Messages.getInstance().getString("VGLII.Cage") + " " + i;
 		}
 		return list;
 	}
 
 
 	/**
 	 * this disables language selection after a problem has been opened
 	 */
 	private void disableLanguageMenu() {
 		mnuLanguage.setEnabled(false);
 	}
 
 	private void showModelBuilder() {
 		modelBuilderDialog.setVisible(true);
 	}
 
 	/*
 	 * if enabled, grade students' work
 	 */
 	private void grade() {
 		JFileChooser fileChooser = new JFileChooser();
 		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		fileChooser.setDialogTitle("Choose the DIRECTORY where the work files are stored");
 		int val = fileChooser.showOpenDialog(this);
 		if (val == JFileChooser.APPROVE_OPTION) {
 			Grader grader = new Grader(fileChooser.getSelectedFile(), this);
 			grader.openDirectoryAndLoadFiles();
 		}
 	}
 
 	public PrivateKey getGradingKey() {
 		return gradingKey;
 	}
 
 	public ModelBuilderUI getModelBuilder() {
 		return modelBuilder;
 	}
 
 	public void setChangeSinceLastSave() {
 		changeSinceLastSave = true;
 	}
 
 }
 
