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
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Random;
 import java.util.TreeMap;
 import java.util.zip.Deflater;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTextPane;
 import javax.swing.JToolBar;
 import javax.swing.border.SoftBevelBorder;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
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
 import GeneticModels.OrganismList;
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
 	public final static String version = "3.0.0"; //$NON-NLS-1$
 
 	/**
 	 * the list of supported languages
 	 */
 	public final static LanguageSpecifierMenuItem[] supportedLanguageMenuItems = {
 		new LanguageSpecifierMenuItem("English", "en", "US"),
 		new LanguageSpecifierMenuItem("Espaol", "es", "ES"),
 		new LanguageSpecifierMenuItem("Franais", "fr", "FR"),
 		//		new LanguageSpecifierMenuItem("Korean", "ko", "KR")
 	};
 
 	/**
 	 * boolean for whether grading will work
 	 */
 	private boolean graderEnabled;
 
 	/**
 	 * key for encrypting work files
 	 *   XORed with bytes of work file
 	 */
 	public final static byte[] KEY = 
 		(new String("The Virtual Genetics Lab is Awesome!")).getBytes();
 
 	/**
 	 * the dimensions of the Phenotype image
 	 */
 	public final static int PHENO_IMAGE_WIDTH = 900;
 	public final static int PHENO_IMAGE_HEIGHT = 700;
 
 	private Random random;
 
 	/**
 	 * the genetic model for the current problem
 	 */
 	private GeneticModel geneticModel;
 
 	/**
 	 * the model builder for the current problem
 	 */
 	private ModelBuilderUI modelBuilder;
 
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
 	 * keeps track if there have been changes (crosses only)
 	 * since last save
 	 *  that way, it won't bug you to save a second time
 	 */
 	private boolean changeSinceLastSave;
 
 	/**
 	 * The constructor
 	 * 
 	 */
 	public VGLII() {
 		super(Messages.getInstance().getString("VGLII.Name") + version); //$NON-NLS-1$
 		addWindowListener(new ApplicationCloser());
 
 		random = new Random();
 
 		graderEnabled = true;
 
 		desktopDirectory = new File(System.getProperty("user.home")  //$NON-NLS-1$
 				+ System.getProperty("file.separator") //$NON-NLS-1$
 				+ "Desktop"); //$NON-NLS-1$
 		if (!desktopDirectory.exists()) {
 			desktopDirectory = defaultProblemDirectory;
 		}
 		setupUI(); 
 		changeSinceLastSave = true;
 	}
 
 
 	/**
 	 * main method
 	 */
 	public static void main(String[] args) {
 		VGLII vgl2 = new VGLII();
 		vgl2.setVisible(true);
 		if (args.length > 0) {
 			String fileName = args[0];
 			if (fileName.endsWith(".pr2")) { //$NON-NLS-1$
 				vgl2.newProblem(fileName);
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
 	 */
 	private void eventHandler(ActionEvent evt) {
 		String cmd = evt.getActionCommand();
 		update(getGraphics());
 		if (cmd.equals("NewProblem")) //$NON-NLS-1$
 			newProblem(null);
 		else if (cmd.equals("OpenWork")) //$NON-NLS-1$
 			openProblem(null);
 		else if (cmd.equals("SaveWork")) //$NON-NLS-1$
 			saveProblem();
 		else if (cmd.equals("SaveAs")) //$NON-NLS-1$
 			saveAsProblem();
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
 	private void menuBar() {
 
 		URL openImageURL = VGLII.class.getResource("images/open16.gif"); //$NON-NLS-1$
 		ImageIcon openImage = new ImageIcon(openImageURL);
 
 		URL newImageURL = VGLII.class.getResource("images/new16.gif"); //$NON-NLS-1$
 		ImageIcon newImage = new ImageIcon(newImageURL);
 
 		URL saveAsImageURL = VGLII.class
 		.getResource("images/saveas16.gif"); //$NON-NLS-1$
 		ImageIcon saveAsImage = new ImageIcon(saveAsImageURL);
 
 		URL saveImageURL = VGLII.class.getResource("images/save16.gif"); //$NON-NLS-1$
 		ImageIcon saveImage = new ImageIcon(saveImageURL);
 
 		URL aboutImageURL = VGLII.class.getResource("images/about16.gif"); //$NON-NLS-1$
 		ImageIcon aboutImage = new ImageIcon(aboutImageURL);
 
 		URL printFileImageURL = 
 			VGLII.class.getResource("images/printtofile16.gif"); //$NON-NLS-1$
 		ImageIcon printFileImage = new ImageIcon(printFileImageURL);
 
 		URL balloonHelpImageURL = VGLII.class
 		.getResource("images/help16.gif"); //$NON-NLS-1$
 		ImageIcon balloonHelpImage = new ImageIcon(balloonHelpImageURL);
 
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
 		newProblemItem = menuItem(Messages.getInstance().getString("VGLII.NewProblem"), "NewProblem", newImage); //$NON-NLS-1$ //$NON-NLS-2$
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
 			supportedLanguageMenuItems[i].addActionListener(new LanguageMenuItemListener());
 		}
 		mnuBar.add(Box.createHorizontalGlue());
 		mnuBar.add(mnuLanguage);
 
 		setJMenuBar(mnuBar);
 	}
 
 	private class LanguageMenuItemListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			LanguageSpecifierMenuItem item = (LanguageSpecifierMenuItem)e.getSource();
 			Locale.setDefault(new Locale(item.getLanguage(), item.getCountry()));
 			Messages.getInstance().updateResourceBundle();
 			mnuBar.removeAll();
 			menuBar();
 			toolBar.removeAll();
 			toolBar();
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
 	private void toolBar() {
 		URL openImageURL = VGLII.class.getResource("images/open.gif"); //$NON-NLS-1$
 		ImageIcon openImage = new ImageIcon(openImageURL);
 
 		URL newImageURL = VGLII.class.getResource("images/new.gif"); //$NON-NLS-1$
 		ImageIcon newImage = new ImageIcon(newImageURL);
 
 		URL saveAsImageURL = VGLII.class.getResource("images/saveas.gif"); //$NON-NLS-1$
 		ImageIcon saveAsImage = new ImageIcon(saveAsImageURL);
 
 		URL saveImageURL = VGLII.class.getResource("images/save.gif"); //$NON-NLS-1$
 		ImageIcon saveImage = new ImageIcon(saveImageURL);
 
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
 
 		newButton = JButtonImageItem(newImage, "NewProblem", //$NON-NLS-1$
 				Messages.getInstance().getInstance().getString("VGLII.NewProblem"), KeyEvent.VK_N); //$NON-NLS-1$
 		openButton = JButtonImageItem(openImage, "OpenWork", Messages.getInstance().getInstance().getString("VGLII.OpenWork"), //$NON-NLS-1$ //$NON-NLS-2$
 				KeyEvent.VK_O);
 		closeButton = JButtonImageItem(closeImage, "CloseWork", //$NON-NLS-1$
 				Messages.getInstance().getInstance().getString("VGLII.CloseWork"), KeyEvent.VK_L); //$NON-NLS-1$
 		exitButton = JButtonImageItem(exitImage, "Exit", Messages.getInstance().getInstance().getString("VGLII.Exit"), //$NON-NLS-1$ //$NON-NLS-2$
 				KeyEvent.VK_E);
 		saveButton = JButtonImageItem(saveImage, "SaveWork", Messages.getInstance().getInstance().getString("VGLII.SaveWork"), //$NON-NLS-1$ //$NON-NLS-2$
 				KeyEvent.VK_S);
 		saveAsButton = JButtonImageItem(saveAsImage, "SaveAs", Messages.getInstance().getInstance().getString("VGLII.SaveAs"), //$NON-NLS-1$ //$NON-NLS-2$
 				KeyEvent.VK_V);
 		crossTwoButton = JButtonImageItem(crossTwoImage, "CrossTwo", //$NON-NLS-1$
 				Messages.getInstance().getInstance().getString("VGLII.CrossTwo"), KeyEvent.VK_C); //$NON-NLS-1$
 		aboutButton = JButtonImageItem(aboutImage, "About", //$NON-NLS-1$
 				Messages.getInstance().getInstance().getString("VGLII.AboutVGL"), KeyEvent.VK_A); //$NON-NLS-1$
 
 		printButton = JButtonImageItem(printImage, "PrintWork", //$NON-NLS-1$
 				Messages.getInstance().getInstance().getString("VGLII.PrintWork"), KeyEvent.VK_P); //$NON-NLS-1$
 
 		printToFileButton = JButtonImageItem(printFileImage, "PrintToFile", //$NON-NLS-1$
 				Messages.getInstance().getInstance().getString("VGLII.PrintWorkToFile"), KeyEvent.VK_F); //$NON-NLS-1$
 		onlineHelpButton = JButtonImageItem(onlineHelpImage, "OnlineHelp", //$NON-NLS-1$
 				Messages.getInstance().getInstance().getString("VGLII.HelpPage"), KeyEvent.VK_H); //$NON-NLS-1$
 
 		toolBar.add(newButton);
 		toolBar.add(openButton);
 		toolBar.add(closeButton);
 		toolBar.add(exitButton);
 		toolBar.add(saveButton);
 		toolBar.add(saveAsButton);
 		toolBar.add(printButton);
 		toolBar.add(crossTwoButton);
 		toolBar.add(onlineHelpButton);
 		toolBar.add(aboutButton);
 	}
 
 	/**
 	 * Create and load all GUI components
 	 */
 	private void setupUI() {
 		mnuBar = new JMenuBar();
 		menuBar();
 		statusPanel();
 		toolBar = new JToolBar();
 		toolBar();
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
 			if (windows[i].toString().matches("title=New Problem Type Selection")) { //$NON-NLS-1$
 				windows[i].dispose();
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Method to set up a new problem for the user
 	 */
 	private void newProblem(String problemFileName) {
 		problemFile = null;
 
 		if (cageCollection == null) {
 			if (problemFileName == null) {
 				File problemsDirectory = new File(defaultProblemDirectory.toString()
 						+ System.getProperty("file.separator") + "Problems"); //$NON-NLS-1$ //$NON-NLS-2$
 				if (!problemsDirectory.exists()) {
 					problemsDirectory = defaultProblemDirectory;
 				}
 				problemFile = selectFile(problemsDirectory,
 						Messages.getInstance().getInstance().getString("VGLII.NewProbTypeSel"), 
 						Messages.getInstance().getInstance().getString("VGLII.SelProbType"), false, //$NON-NLS-1$ //$NON-NLS-2$
 						prbFilterString, Messages.getInstance().getInstance().getString("VGLII.ProTypeFiles"), //$NON-NLS-1$
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
 
 			nextCageId = 0;
 			selectionVial = new SelectionVial(statusLabel);
 			cageCollection = new ArrayList<CageUI>();
 
 			Cage fieldPop = geneticModel.generateFieldPopulation();
 			createCageUI(fieldPop, false);
 			enableAll(true);
 			disableLanguageMenu();
 			modelBuilder = new ModelBuilderUI(this, geneticModel);
 		}
 	}
 
 	/**
 	 * Opens up an existing saved problem, sets up the model, and opens up all
 	 * the cages of that problem.
 	 */
 	public void openProblem(String workFileName) {	
 		File workFile = null;
 
 		selectionVial = new SelectionVial(statusLabel);
 		GeneticModelAndCageSet result = null;
 
 		if (workFileName == null) {
 			workFile = selectFile(desktopDirectory, Messages.getInstance().getInstance().getString("VGLII.OpenWork"), //$NON-NLS-1$
 					Messages.getInstance().getInstance().getString("VGLII.SelWrkFile"), false, wrkFilterString, 
 					Messages.getInstance().getInstance().getString("VGLII.WorkFiles"), //$NON-NLS-1$ //$NON-NLS-2$
 					JFileChooser.OPEN_DIALOG);
 		} else {	
 			workFile = new File(workFileName);
 		}
 
 		if (workFile == null) return;
 		if (!workFile.exists()) return;
 
 		try {
 			result = GeneticModelFactory.getInstance().readModelFromFile(workFile);
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
 			modelBuilder = new ModelBuilderUI(this, geneticModel);
 			modelBuilder.configureFromFile(result.getModelBuilderState());
 		} else {
 			modelBuilderItem.setEnabled(false);
 		}
 
 		changeSinceLastSave = true;
 	}
 
 	/**
 	 * Saves the current work done by the user to a file.
 	 */
 	private void saveProblem() {
 		if (cageCollection != null) {
 			if (currentSavedFile == null)
 				currentSavedFile = selectFile(desktopDirectory,
 						Messages.getInstance().getInstance().getString("VGLII.SaveWork"), 
 						Messages.getInstance().getInstance().getString("VGLII.EnterSaveFileName"), false, //$NON-NLS-1$ //$NON-NLS-2$
 						wrkFilterString, Messages.getInstance().getInstance().getString("VGLII.WorkFiles"), //$NON-NLS-1$
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
 
 					XMLOutputter outputter = 
 						new XMLOutputter(Format.getPrettyFormat());
 					String xmlString = outputter.outputString(doc);
 
 					//encrypt it with XOR and zip it to prevent cheating
 					byte[] xmlBytes = null;
 					try {
 						xmlBytes = xmlString.getBytes("UTF-8"); //$NON-NLS-1$
 					} catch (UnsupportedEncodingException e1) {
 						e1.printStackTrace();
 					}
 
 					for (int i = 0; i < xmlBytes.length; i++) {
 						xmlBytes[i] = (byte) (xmlBytes[i] ^ KEY[i % (KEY.length - 1)]);
 					}
 
 					ZipOutputStream zipWriter = null;
 					try {
 						zipWriter = 
 							new ZipOutputStream(new FileOutputStream(currentSavedFile));
 						zipWriter.setLevel(Deflater.DEFAULT_COMPRESSION);
 						zipWriter.putNextEntry(new ZipEntry("encrypted.txt")); //$NON-NLS-1$
 						zipWriter.write(xmlBytes, 0, xmlBytes.length);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					finally {
 						try {
 							if (zipWriter != null) {
 								zipWriter.close();
 							}
 						}
 						catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			} catch (Exception e) {
 			}
 		}
 		changeSinceLastSave = false;
 	}
 
 	private Document getXMLDoc(ArrayList<Cage> cages) throws Exception {
 		// creating the whole tree
 		Element root = new Element("VglII"); //$NON-NLS-1$
 		
 		Element pfn = new Element("ProbFileName");
 		pfn.addContent(problemFile.getName());
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
 	 * Prints the current work done by the user to a .html file
 	 */
 	private void printToFile() {
 		if (cageCollection != null) {
 			File printFile = selectFile(desktopDirectory,
 					Messages.getInstance().getInstance().getString("VGLII.PrintWorkToFile"), 
 					Messages.getInstance().getInstance().getString("VGLII.EnterPrintFileName"), 
 					false, //$NON-NLS-1$ //$NON-NLS-2$
 					printFilterString, Messages.getInstance().getInstance().getString("VGLII.PrintFiles"), -1); //$NON-NLS-1$
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
 		if (cageCollection != null) {
 			if (!changeSinceLastSave) {
 				cleanUp();
 			} else {
 				int ans1 = JOptionPane.showConfirmDialog(this,
 						Messages.getInstance().getInstance().getString("VGLII.ClosingWarningLine1") //$NON-NLS-1$
 						+ "\n"
 						+ Messages.getInstance().getInstance().getString("VGLII.ClosingWarningLine2"), //$NON-NLS-1$
 						Messages.getInstance().getInstance().getString("VGLII.CloseWork"), JOptionPane.YES_NO_CANCEL_OPTION, //$NON-NLS-1$
 						JOptionPane.WARNING_MESSAGE);
 				if (ans1 == JOptionPane.YES_OPTION)
 					saveProblem();
 				if (ans1 == JOptionPane.NO_OPTION) 
 					cleanUp();
 				if (ans1 != JOptionPane.CANCEL_OPTION)
 					return;
 			}
 		}
 	}
 
 	/**
 	 * Exits the application after doing the necessary cleanup
 	 */
 	private void exitApplication() {
 		if (cageCollection != null) {
 			if (!changeSinceLastSave) {
 				saveProblem();
 				cleanUp();
 				System.exit(0);
 			} else {
 				int ans = JOptionPane.showConfirmDialog(this,
 						Messages.getInstance().getInstance().getString("VGLII.QuitWarningLine1") //$NON-NLS-1$
 						+ "\n"
 						+ Messages.getInstance().getInstance().getString("VGLII.QuitWarningLine2"), //$NON-NLS-1$
 						Messages.getInstance().getInstance().getString("VGLII.ExitVGL"), JOptionPane.YES_NO_CANCEL_OPTION, //$NON-NLS-1$
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
 			changeSinceLastSave = true;
 			modelBuilder.updateCageChoices(nextCageId);
 		} else {
 			JOptionPane.showMessageDialog(this, Messages.getInstance().getInstance().getString("VGLII.VGLII") //$NON-NLS-1$
 					+ "\n"
 					+ Messages.getInstance().getInstance().getString("VGLII.CrossWarningLine1") //$NON-NLS-1$
 					+ "\n"
 					+ Messages.getInstance().getInstance().getString("VGLII.CrossWarningLine2"), //$NON-NLS-1$
 					Messages.getInstance().getInstance().getString("VGLII.CrossTwo"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
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
 		newButton.setEnabled(!value);
 		openButton.setEnabled(!value);
 		newProblemItem.setEnabled(!value);
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
 					System.out.println(Messages.getInstance().getInstance().getString("VGLII.NoParentsWarning") //$NON-NLS-1$
 							+ " #:"
 							+ c.getId());
 				if (parentUIs[0] == null)
 					System.out.println(Messages.getInstance().getInstance().getString("VGLII.NoParent0Warning") //$NON-NLS-1$
 							+ " #:"
 							+ c.getId());
 				if (parentUIs[1] == null)
 					System.out.println(Messages.getInstance().getInstance().getString("VGLII.NoParent1Warning") //$NON-NLS-1$
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
 						.println(Messages.getInstance().getInstance().getString("VGLII.ForOrgs") //$NON-NLS-1$
 								+ "#:"
 								+ c.getId());
 						if (originalOUI1 == null)
 							System.out.println(Messages.getInstance().getInstance().getString("VGLII.OrgFor") + ": " + o1.getId() //$NON-NLS-1$
 									+ " " + o1.getCageId() + " " + Messages.getInstance().getInstance().getString("VGLII.NotFound") + " !"); //$NON-NLS-1$ //$NON-NLS-2$
 						if (originalOUI2 == null)
 							System.out.println(Messages.getInstance().getInstance().getString("VGLII.OrgFor") + ": " + o2.getId() //$NON-NLS-1$
 									+ " " + o2.getCageId() + " " + Messages.getInstance().getInstance().getString("VGLII.NotFound") + " !"); //$NON-NLS-1$ //$NON-NLS-2$
 					}
 				} else {
 					System.out.println(Messages.getInstance().getInstance().getString("VGLII.ForParentsOfCage") + "#: " + c.getId()); //$NON-NLS-1$
 					if (cage1 == null)
 						System.out.println(Messages.getInstance().getInstance().getString("VGLII.CageForOrg") + o1.getId() //$NON-NLS-1$
 								+ " " + o1.getCageId() + " " + Messages.getInstance().getInstance().getString("VGLII.NotFound") + " !"); //$NON-NLS-1$ //$NON-NLS-2$
 					if (cage2 == null)
 						System.out.println(Messages.getInstance().getInstance().getString("VGLII.CageForOrg") + o2.getId() //$NON-NLS-1$
 								+ " " + o2.getCageId() + " " + Messages.getInstance().getInstance().getString("VGLII.NotFound") + " !"); //$NON-NLS-1$ //$NON-NLS-2$
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
 	 * Get number of cages for the model builder UI
 	 * - if this is an open, working problem, there will be 
 	 * 		CageUI's in the cageCollection
 	 * - if it's just being graded, there are no CageUI's
 	 * 		so return -1 as a flag
 	 */
 	public int getNumCages() {
 		if (cageCollection != null) {
 			return cageCollection.size();
 		} else {
 			return -1;
 		}
 	}
 
 	/**
 	 * this disables language selection after a problem has been opened
 	 */
 	private void disableLanguageMenu() {
 		mnuLanguage.setEnabled(false);
 	}
 
 	private void showModelBuilder() {
 		modelBuilder.setVisible(true);
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
 
 	/**
 	 * load a saved genetic model, cages, and model builder
 	 *  from the Grader
 	 *  but don't show any of the gui components
 	 *    unless showCagesEtc is true
 	 */
 	public void setupForGrading(GeneticModelAndCageSet result, boolean showCagesEtc) {
 		PhenotypeImageBank.getInstance().resetDefaults();
 		geneticModel = result.getGeneticModel();
 		if (showCagesEtc) {
 			cageCollection = new ArrayList<CageUI>();
 			nextCageId = 0;
 			try {
 				reopenCages(result.getCages());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		modelBuilder = new ModelBuilderUI(this, result, showCagesEtc);
 		modelBuilder.configureFromFile(result.getModelBuilderState());
 		modelBuilder.setVisible(showCagesEtc);
 	}
 
 	public ModelBuilderUI getModelBuilder() {
 		return modelBuilder;
 	}
 }
 
