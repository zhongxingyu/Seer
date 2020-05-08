 package VGL;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JToggleButton;
 import javax.swing.border.Border;
 
 import GeneticModels.Cage;
 import GeneticModels.Organism;
 import GeneticModels.OrganismList;
 import GeneticModels.Phenotype;
 import PhenotypeImages.PhenotypeImageBank;
 
 /**
  * Nikunj Koolar cs681-3 Fall 2002 - Spring 2003 Project VGL File:
  * Brian White 2008
  * CustomizedFileFilter.java - Instances of this class provide for file filters
  * to show only those file that are supported by the application.
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
 public class CageUI extends JDialog 
 implements WindowListener, MouseListener, Comparable<CageUI> {
 
 	/**
 	 * the background color when the Cage is selected
 	 * for membership in the summary chart
 	 */
	private static Color selectedColor = new Color(255,0,0); 
 
 	/**
 	 * boolean to indicate membership in selected set for
 	 * summary chart
 	 */
 	private boolean isSelected;
 
 	/**
 	 * manager for membership in selected set for summary chart
 	 */
 	private SummaryChartManager summaryChartManager;
 
 	/**
 	 * The Id for the Cage. This value is always one more than the id of the
 	 * cage it holds. This is because the Cage id's begin from 0, but on screen
 	 * they have to be shown beginning from 1.
 	 */
 	private int id;
 
 	/**
 	 * number of traits in this problem
 	 */
 	private int numberOfTraits;
 
 	/**
 	 * maximum number of organisms in any row
 	 * it assumes 2 rows of orgs
 	 */
 	private int maxOrgsInOneRow;
 
 	/**
 	 * Parameter to the set the width of the dialog
 	 */
 	private int dialogWidth;
 
 	/**
 	 * Parameter to the set the height of the dialog
 	 */
 	private int dialogHeight ;
 
 	/**
 	 * Parameter to the set the X-coordinate of the dialog
 	 */
 	private int dialogLocationX;
 
 	/**
 	 * Parameter to the set the Y-coordinate of the dialog
 	 */
 	private int dialogLocationY;
 
 	/**
 	 * This panel holds the entire list of Organism UIs for the Cage
 	 */
 	private JPanel organismsPanel;
 
 	/**
 	 * This panel holds the numeric count of each sex type thats currently
 	 * present in the cage
 	 */
 	private JPanel countsPanel;
 
 	/**
 	 * this is an array of panels, one for each trait
 	 */
 	private JPanel[] traitPanels;
 
 	/**
 	 * This panel holds the Image for each phenotype associated with the
 	 * organisms in the cage
 	 */
 	private JPanel picturesPanel;
 
 	/**
 	 * This panel holds the set of Organisms,their Counts and their pictures for
 	 * each phenotype.
 	 */
 	private JPanel individualPanel;
 
 	/**
 	 * The panel that contains all the subpanels.
 	 */
 	private JPanel superPanel;
 
 	/**
 	 * This panel holds the Individual Panel
 	 */
 	private JPanel detailsPanel;
 
 	/**
 	 * This panel contains the info about the parents of the organisms of this
 	 * cage, or if the cage is the initial population cage then it holds the
 	 * information about the Genetic Model currently in use
 	 */
 	private JPanel parentInfoPanel;
 
 	/**
 	 * This variable stores the count of the number of different phenotypes
 	 * associated with this cage
 	 */
 	private int numPhenosPresent;
 
 	/**
 	 * This variable keeps track of the Icon Image associated with the frame
 	 * holding this cage
 	 */
 	private Image image;
 
 	/** 
 	 * keeps track of the default color of unselected cages
 	 */
 	private Color unselectedColor;
 
 	/**
 	 * This variable stores a reference to the hashmap of children associated
 	 * with this cage
 	 * sorted by phenotypeString
 	 */
 	private TreeMap<String, OrganismList> children;
 
 	/**
 	 * This stores a more easily accessible version of children
 	 * it is an array of OrganismLists, each holding all the kids with 
 	 * the same phenotype. It is indexed by a number for the phenotype
 	 */
 	private OrganismList[] childrenSortedByPhenotype;
 
 	/**
 	 * This variable stores a reference to the list of parents associated with
 	 * this cage
 	 */
 	private ArrayList<Organism> parents;
 
 	/**
 	 * Holds the array of labels associated with each of the phenotypes
 	 */
 	private JLabel[] phenotypeLabels;
 
 	/**
 	 * Holds the array of buttons associated with each of the phenotypes 
 	 * click these to see the images
 	 */
 	private JButton[] showPhenotypeButtons;
 
 	/**
 	 * holds array mapping real trait # to display order number
 	 * that way, the traits aren't displayed in chromosomal order
 	 */
 	private int[] scrambledTraitOrder;
 
 	/**
 	 * A reference to the Cage object being displayed through this UI
 	 */
 	private Cage cage;
 
 	/**
 	 * A reference to the selectionvial object that keeps track of the currently
 	 * selected male and female organisms for crossing.
 	 */
 	private SelectionVial vial;
 
 	/**
 	 * This variable stores the details about the Genetic Model currently being
 	 * used.
 	 */
 	private String details = null;
 
 	/**
 	 * This widget displays the informations stored in the details variable.
 	 */
 	private JEditorPane textDetails;
 
 	/**
 	 * this is the scrollpane for the details
 	 */
 	private JScrollPane detailsScrollPane;
 
 	/**
 	 * This is the button used to show/hide the Genetics information.
 	 */
 	private JToggleButton showHideDetails;
 
 	/**
 	 * This string holds the heading to be displayed on the toggle button when
 	 * the Genetics Details are being shown
 	 */
 	private String hideDetails = "Hide Genetic Model";
 
 	/**
 	 * This string holds the heading to be displayed on the toggle button when
 	 * the Genetics Details are not being shown
 	 */
 	private String showDetails = "Show Genetic Model";
 
 	/**
 	 * This icon is displayed in the toggle button when the Genetics Details are
 	 * not being shown
 	 */
 	private URL closeURL = CageUI.class.getResource("images/close.gif");
 
 	private ImageIcon closeIcon = new ImageIcon(closeURL);
 
 	/**
 	 * This icon is displayed in the toggle button when the Genetics Details are
 	 * being shown.
 	 */
 	private URL downURL = CageUI.class.getResource("images/down.gif");
 
 	private ImageIcon downIcon = new ImageIcon(downURL);
 
 	/**
 	 * This variable is used to decide the following: a. Whether to display the
 	 * Genetics Model details in cage 1 b. Whether to allow the individual
 	 * organisms to display allele information in balloon help active mode.
 	 */
 	private boolean isBeginner = false;
 
 	/**
 	 * Array of Parent Organisms. This array was initially concieved to hold
 	 * simply the images of the parents (which explains its naming) but later
 	 * on, in order to support linking between parents and their corresponding
 	 * objects in the original cages where they were present, this array was
 	 * then used to store parent OrganismUI objects.
 	 */
 	private OrganismUI[] parentOrganismUIs;
 
 	/**
 	 * This array of arrays stores the organismUIs for all the organisms of
 	 * all the phenotypes associated with this cage. 
 	 * it is stored in 2 rows where the length of the rows is
 	 * (maximum number of offspring for one pheno)/2 
 	 * For eg. If the pheno with the most offspring has 40 in it
 	 * and there is one pheno then this variable will be [2][20] in size. If the
 	 * cage contains 2 phenotypes then this variable will be [4][20] in size.
 	 * - there is probably some danger with really high numbers of offspring
 	 */
 	private OrganismUI[][] childrenOrganismUIs;
 
 	/**
 	 * The constructor
 	 * 
 	 * @param importFrame
 	 *            the reference frame for the dialog
 	 * @param isbeginnersmode
 	 *            true if user is allowed to view underlying genetics details,
 	 *            false otherwise
 	 * @param cage
 	 *            reference to the Cage object
 	 * @param sv
 	 *            reference to the SelectionVial object
 	 * @param details
 	 *            string containing information about the underlying genetics
 	 *            model
 	 */
 	public CageUI(Frame importFrame, 
 			boolean isbeginnersmode, 
 			Cage cage,
 			SelectionVial sv, 
 			String details, 
 			int numberOfTraits,
 			int[] scrambledTraitOrder) {
 		//initialize parent
 		super(importFrame, false);
 		addWindowListener(this);
 		addMouseListener(this);
 		this.isBeginner = isbeginnersmode;
 		this.cage = cage;
 		vial = sv;
 		image = importFrame.getIconImage();
 		id = cage.getId() + 1;
 		children = cage.getChildren();
 		parents = cage.getParents();
 		this.scrambledTraitOrder = scrambledTraitOrder;
 		if (id == 1)
 			if (details != null)
 				this.details = details;
 		this.numberOfTraits = numberOfTraits;
 		setTitle("Cage " + (new Integer(id)).toString());
 		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
 		setupSubComponents();
 		setupDialogBox(importFrame, numPhenosPresent);
 		setResizable(false);
 
 		unselectedColor = getBackground();
 		isSelected = false;
 		summaryChartManager = SummaryChartManager.getInstance();
 
 		maxOrgsInOneRow = (cage.getMaxOrgListSize()/2) + 1;  // assumes 2 rows of orgs
 		// add 1 in case rounding
 
 		//setup the GUI of its internal components
 		components();
 		pack();
 		setVisible(true);
 	}
 
 	/**
 	 * This method initializes the objects and widgets that store information
 	 * about the various phenotypes associated with the cage.
 	 */
 	private void setupSubComponents() {
 		// do this all by the number of phenotypes present in the chidren
 		Set<String> phenotypeStrings = children.keySet();
 		Iterator<String> it1 = phenotypeStrings.iterator();
 		numPhenosPresent = phenotypeStrings.size();
 
 		String[] phenotypeNames = new String[numPhenosPresent];
 		childrenSortedByPhenotype = new OrganismList[numPhenosPresent];
 		phenotypeLabels = new JLabel[numPhenosPresent];
 		showPhenotypeButtons = new ShowPhenotypeButton[numPhenosPresent];
 
 		int i = 0;
 		while (it1.hasNext()) {
 			phenotypeNames[i] = new String(it1.next());
 			childrenSortedByPhenotype[i] = children.get(phenotypeNames[i]);
 			showPhenotypeButtons[i] = 
 				new ShowPhenotypeButton(
 						childrenSortedByPhenotype[i].getPhenotypes(),
 						phenotypeNames[i]);
 			showPhenotypeButtons[i].addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					JFrame frame = new JFrame();
 					frame.setIconImage(null);
 					JDialog imageDlg = new JDialog(frame, "Phenotype Details",
 							true);
 					imageDlg.setSize(200, 220);
 					imageDlg.setLocation(512 - 100, 384 - 110);
 					imageDlg.setResizable(false);
 					JPanel details = new JPanel();
 					details.setLayout(new BorderLayout());
 					details.setBorder(BorderFactory.createEtchedBorder());
 					ShowPhenotypeButton button = (ShowPhenotypeButton)evt.getSource();
 
 					// fix the pheno string to more than one line
 					String phenoString = button.getPhenotypeString();
 					phenoString = phenoString.replaceAll("/", "<br>");
 					phenoString = "<html>" + phenoString + "</html>";
 					JLabel phenotypeLabel = 
 						new JLabel(phenoString);
 					phenotypeLabel.setHorizontalTextPosition(javax.swing.JLabel.CENTER);
 					phenotypeLabel.setHorizontalAlignment(javax.swing.JLabel.CENTER);
 					details.add(phenotypeLabel, BorderLayout.NORTH);
 					ImageIcon pic = 
 						PhenotypeImageBank.getInstance().getImageForPhenotype(
 								button.getPhenotypes(), button.getPhenotypeString());
 					details.add(new JLabel(pic), BorderLayout.CENTER);
 					imageDlg.getContentPane().add(details);
 					imageDlg.setVisible(true);
 				}
 			});
 			phenotypeLabels[i] = new JLabel(phenotypeNames[i]);
 			phenotypeLabels[i]
 			                .setHorizontalTextPosition(javax.swing.JLabel.CENTER);
 			phenotypeLabels[i].setHorizontalAlignment(javax.swing.JLabel.CENTER);
 			showPhenotypeButtons[i].setPreferredSize(new Dimension(38, 38));
 			showPhenotypeButtons[i].setFocusPainted(false);
 			showPhenotypeButtons[i].setToolTipText("Click to see image of this phenotype");
 			i++;
 		}
 	}
 
 	/**
 	 * This method sets up the GUI and other characteristics of the internals of
 	 * the Cage
 	 */
 	private void components() {
 		setupOrganismPanel();
 		setupParentInfoPanel();
 		setContentPane(superPanel);
 	}
 
 	/**
 	 * This method sets up the extents and position for the Cage
 	 */
 	private void setupDialogBox(Frame importFrame, int panelCount) {
 		int dtHeight = (int) (getGraphicsConfiguration().getDevice()
 				.getDefaultConfiguration().getBounds().getHeight());
 		int dtWidth = (int) (getGraphicsConfiguration().getDevice()
 				.getDefaultConfiguration().getBounds().getWidth());
 		int ht = (int) (768 / 5.6);
 		dialogHeight = ht + (int) ((panelCount - 1) * ht)/3;
 		dialogWidth = 550;
 		dialogLocationX = (int) (dtWidth / 2) - dialogWidth / 2;
 		dialogLocationY = (int) (dtHeight / 2) - dialogHeight / 2;
 		setSize(new Dimension(dialogWidth, dialogHeight));
 		setLocation(new Point(dialogLocationX, dialogLocationY));
 	}
 
 	/**
 	 * This method sets up the panels for the Cage
 	 */
 	private void setupOrganismPanel() {
 
 		Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
 		BorderLayout bSelectionLayout = new BorderLayout();
 		superPanel = new JPanel();
 		superPanel.setLayout(bSelectionLayout);
		superPanel.setBorder(BorderFactory.createLineBorder(unselectedColor, 2));
 		detailsPanel = new JPanel();
 		detailsPanel.setLayout(new BorderLayout());
 		individualPanel = new JPanel();
 		FlowLayout fl = new FlowLayout();
 		fl.setHgap(1);
 		fl.setVgap(1);
 		individualPanel.setLayout(fl);
 
 		JPanel organismsPanelWrapper = new JPanel();
 		organismsPanelWrapper.setLayout(
 				new BoxLayout(organismsPanelWrapper, BoxLayout.Y_AXIS));
 		organismsPanelWrapper.add(Box.createRigidArea(new Dimension(100,1)));
 		organismsPanel = new JPanel();
 		organismsPanel.setLayout(new GridLayout(numPhenosPresent, 1));
 		organismsPanel.setBorder(BorderFactory.createTitledBorder(
 				emptyBorder, "Organisms",
 				javax.swing.border.TitledBorder.CENTER,
 				javax.swing.border.TitledBorder.ABOVE_TOP));
 
 		countsPanel = new JPanel();
 		countsPanel.setLayout(new GridLayout(numPhenosPresent, 1));
 		countsPanel.setBorder(BorderFactory.createTitledBorder(emptyBorder,
 				"Count", javax.swing.border.TitledBorder.CENTER,
 				javax.swing.border.TitledBorder.ABOVE_TOP));
 
 		// headers for the different traits
 		traitPanels = new JPanel[numberOfTraits];
 		JPanel[] traitPanelWrappers = new JPanel[numberOfTraits];
 
 		// need to get the type of each trait
 		//  get one organism's pheno (it doesn't matter which one)
 		ArrayList<Phenotype> phenotypes = 
 			childrenSortedByPhenotype[0].get(0).getPhenotypes();
 		for (int i = 0; i < numberOfTraits; i++) {
 			traitPanels[i] = new JPanel();
 			traitPanels[i].setLayout(new GridLayout(numPhenosPresent, 1));
 			traitPanels[i].setBorder(BorderFactory.createTitledBorder(
 					emptyBorder,
 					phenotypes.get(scrambledTraitOrder[i]).getTrait().getBodyPart(), 
 					javax.swing.border.TitledBorder.CENTER,
 					javax.swing.border.TitledBorder.ABOVE_TOP));
 			traitPanelWrappers[i] = new JPanel();
 			traitPanelWrappers[i].setLayout(
 					new BoxLayout(traitPanelWrappers[i], BoxLayout.Y_AXIS));
 			traitPanelWrappers[i].add(Box.createHorizontalStrut(70));
 			traitPanelWrappers[i].add(traitPanels[i]);
 		}
 
 		picturesPanel = new JPanel();
 		picturesPanel.setLayout(new GridLayout(numPhenosPresent, 1));
 		picturesPanel.setBorder(BorderFactory.createTitledBorder(emptyBorder,
 				"Images", javax.swing.border.TitledBorder.CENTER,
 				javax.swing.border.TitledBorder.ABOVE_TOP));
 
 		childrenOrganismUIs = new OrganismUI[2 * numPhenosPresent][maxOrgsInOneRow];
 
 		//For each phenotype, setup its own panels for organismUIs,count and
 		//pictures and add them to the right places in the organismpanel,
 		// countspanel, phenotype panels
 		//and the picturespanel
 		for (int i = 0; i < numPhenosPresent; i++) {
 			IndividualPanelSet panelSet = setupIndividualPanel(i);
 			organismsPanel.add(panelSet.getOrganismPanel());
 			countsPanel.add(panelSet.getCountsPanel());
 			JPanel[] phenoPanels = panelSet.getPhenotypePanels();
 			for (int j = 0; j < numberOfTraits; j++) {
 				traitPanels[j].add(
 						phenoPanels[scrambledTraitOrder[j]]);
 			}
 			picturesPanel.add(showPhenotypeButtons[i]);
 		}
 
 		organismsPanelWrapper.add(organismsPanel);
 		individualPanel.add(organismsPanelWrapper);
 		individualPanel.add(countsPanel);
 		for (int i = 0; i < numberOfTraits; i++) {
 			individualPanel.add(traitPanelWrappers[i]);
 		}
 		individualPanel.add(picturesPanel);
 		detailsPanel.add(individualPanel, BorderLayout.NORTH);
 		superPanel.add(detailsPanel, BorderLayout.NORTH);
 	}
 
 	/**
 	 * This method returns a JPanel containing the OrganismUIs for each phenotype
 	 * associated with this cage.
 	 * 
 	 * @param number
 	 *            index of the phenotype in the list of phenotypes for which the
 	 *            panels are being set up.
 	 */
 	private IndividualPanelSet setupIndividualPanel(int number) {
 
 		Border etched = BorderFactory.createEtchedBorder();
 
 		JPanel topRowOfOrganismsPanel = new JPanel();
 		JPanel bottomRowOfOrganismsPanel = new JPanel();
 
 		GridLayout gridlt = new GridLayout(1, maxOrgsInOneRow);
 		gridlt.setHgap(1);
 		gridlt.setVgap(2);
 		topRowOfOrganismsPanel.setLayout(gridlt);
 		bottomRowOfOrganismsPanel.setLayout(gridlt);
 
 		OrganismUI[] topRowOfOrganismUIs = childrenOrganismUIs[2 * number];
 		OrganismUI[] bottomRowOFOrganismUIs = childrenOrganismUIs[2 * number + 1];
 		Iterator<Organism> it = childrenSortedByPhenotype[number].iterator();
 
 		//lay out two neat rows of OrganismUIs
 		int count = 0;
 		int i = 0;
 		int j = 0;
 		while (it.hasNext()) {
 			Organism o1 = (Organism) it.next();
 			count++;
 			if (count <= maxOrgsInOneRow) {
 				topRowOfOrganismUIs[i] = new OrganismUI(o1, false, isBeginner,
 						vial);
 				topRowOfOrganismsPanel.add(topRowOfOrganismUIs[i]);
 				i++;
 			} else {
 				bottomRowOFOrganismUIs[j] = new OrganismUI(o1, false, isBeginner,
 						vial);
 				bottomRowOfOrganismsPanel.add(bottomRowOFOrganismUIs[j]);
 				j++;
 			}
 		}
 		if (i < maxOrgsInOneRow) {
 			while (i < maxOrgsInOneRow) {
 				topRowOfOrganismsPanel.add(Box.createRigidArea(new Dimension(15, 15)));
 				i++;
 			}
 		}
 		if (j < maxOrgsInOneRow) {
 			while (j < maxOrgsInOneRow) {
 				bottomRowOfOrganismsPanel.add(Box.createRigidArea(new Dimension(15, 15)));
 				j++;
 			}
 		}
 		JPanel organismPanel = new JPanel();
 		GridLayout gl = new GridLayout(2, 0);
 		gl.setVgap(4);
 		organismPanel.setLayout(gl);
 		organismPanel.setBorder(etched);
 		organismPanel.add(topRowOfOrganismsPanel);
 		organismPanel.add(bottomRowOfOrganismsPanel);
 
 		JPanel countPanel = new JPanel();
 		GridLayout gl1 = new GridLayout(2, 0);
 		gl1.setVgap(0);
 		countPanel.setLayout(gl1);
 		countPanel.setBorder(etched);
 		URL maleLabelURL = CageUI.class.getResource("UIimages/maleblack.gif");
 		JLabel maleLabel = new JLabel(new ImageIcon(maleLabelURL));
 		URL femaleLabelURL = CageUI.class
 		.getResource("UIimages/femaleblack.gif");
 		JLabel femaleLabel = new JLabel(new ImageIcon(femaleLabelURL));
 		String mCount = (new Integer(childrenSortedByPhenotype[number].getNumberOfMales())).toString();
 		if (childrenSortedByPhenotype[number].getNumberOfMales() < 10)
 			mCount = "0" + mCount;
 		JLabel maleCountLabel = new JLabel(mCount);
 		String fCount = (new Integer(childrenSortedByPhenotype[number].getNumberOfFemales()))
 		.toString();
 		if (childrenSortedByPhenotype[number].getNumberOfFemales() < 10)
 			fCount = "0" + fCount;
 		JLabel femaleCountLabel = new JLabel(fCount);
 		maleCountLabel.setPreferredSize(new Dimension(25, 15));
 		femaleCountLabel.setPreferredSize(new Dimension(25, 15));
 		maleCountLabel.setHorizontalTextPosition(javax.swing.JLabel.RIGHT);
 		maleCountLabel.setHorizontalAlignment(javax.swing.JLabel.RIGHT);
 		femaleCountLabel.setHorizontalTextPosition(javax.swing.JLabel.RIGHT);
 		femaleCountLabel.setHorizontalAlignment(javax.swing.JLabel.RIGHT);
 		JPanel malePanel = new JPanel();
 		JPanel femalePanel = new JPanel();
 		FlowLayout fl1 = new FlowLayout();
 		fl1.setHgap(1);
 		fl1.setVgap(1);
 		malePanel.setLayout(fl1);
 		femalePanel.setLayout(fl1);
 		malePanel.add(maleCountLabel);
 		malePanel.add(maleLabel);
 		femalePanel.add(femaleCountLabel);
 		femalePanel.add(femaleLabel);
 		countPanel.add(malePanel);
 		countPanel.add(femalePanel);
 
 		JPanel picturePanel = new JPanel();
 		picturePanel.setLayout(new BorderLayout());
 		picturePanel.setPreferredSize(new Dimension(145, 38));
 		picturePanel.setBorder(etched);
 		picturePanel.add(phenotypeLabels[number], BorderLayout.CENTER);
 		picturePanel.add(showPhenotypeButtons[number], BorderLayout.EAST);
 
 		JPanel[] phenotypePanels = new JPanel[numberOfTraits];
 		ArrayList<Phenotype> phenoList = 
 			childrenSortedByPhenotype[number].get(0).getPhenotypes();
 		for (int k = 0; k < numberOfTraits; k++) {
 			phenotypePanels[k] = new JPanel();
 			phenotypePanels[k].setLayout(
 					new BoxLayout(phenotypePanels[k], BoxLayout.X_AXIS));
 			phenotypePanels[k].setBorder(etched);
 			phenotypePanels[k].add(
 					new JLabel(phenoList.get(k).getTrait().getTraitName()));
 			phenotypePanels[k].add(Box.createRigidArea(new Dimension(1, 34)));
 		}
 
 		return new IndividualPanelSet(organismPanel,
 				countPanel,
 				phenotypePanels,
 				picturePanel);
 	}
 
 	/**
 	 * This method sets up the Panel that display the information about the
 	 * parents or if the Cage id is 1 and beginner's mode is true then it
 	 * displays the details about the underlying genetics model
 	 */
 	private void setupParentInfoPanel() {
 		parentInfoPanel = new JPanel();
 		if (id > 1) {
 			JLabel parentLabel = new JLabel("Parent");
 			parentInfoPanel.add(parentLabel);
 			parentOrganismUIs = new OrganismUI[2];
 			Organism o1 = parents.get(0);
 			Organism o2 = parents.get(1);
 			int cageId = o1.getCageId() + 1;
 			String phenoName1 = o1.getPhenotypeString();
 			parentOrganismUIs[0] = new OrganismUI(o1, true, isBeginner, vial);
 			parentInfoPanel.add(parentOrganismUIs[0]);
 			parentInfoPanel.add(new JLabel("(" + cageId + ")" 
 					+ " " + phenoName1));
 			cageId = o2.getCageId() + 1;
 			String phenoName2 = o2.getPhenotypeString();
 			parentOrganismUIs[1] = new OrganismUI(o2, true, isBeginner, vial);
 			parentInfoPanel.add(parentOrganismUIs[1]);
 			parentInfoPanel.add(new JLabel("(" + cageId + ")"
 					+ " " + phenoName2));
 		} else {
 			if (isBeginner) {
 				if (details != null) {
 					textDetails = new JEditorPane();
 					textDetails.setContentType("text/html");
 					textDetails.setEditable(false);
 					textDetails.setBackground(parentInfoPanel
 							.getBackground());
 					textDetails.setBorder(BorderFactory.createEtchedBorder());
 					textDetails.setText(details);
 					showHideDetails = new JToggleButton();
 					textDetails.setFont(showHideDetails.getFont());
 					detailsScrollPane = new JScrollPane(textDetails);
 					showHideDetails.addItemListener(new ItemListener() {
 						public void itemStateChanged(ItemEvent e) {
 							if (e.getStateChange() == ItemEvent.DESELECTED) {
 								showDetails();
 							} else if (e.getStateChange() == ItemEvent.SELECTED) {
 								hideDetails();
 							}
 						}
 					});
 					JPanel showHidePanel = new JPanel();
 					showHidePanel.setLayout(new BorderLayout());
 					showHidePanel.add(showHideDetails, BorderLayout.NORTH);
 					parentInfoPanel.add(showHidePanel);
 					showHideDetails.setSelected(true);
 				}
 			}
 		}
 		superPanel.add(parentInfoPanel, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * This method sets up the Panel that displays the genetics details when the
 	 * toggle button is pressed
 	 */
 	private void showDetails() {
 		showHideDetails.setText(hideDetails);
 		showHideDetails.setIcon(downIcon);
 		JPanel panel = (JPanel) showHideDetails.getParent();
 		if (panel.getComponentCount() == 1) {
 			dialogHeight += 200;
 			int dialogWidth = this.getWidth();
 			setSize(dialogWidth, dialogHeight);
 			detailsScrollPane.setPreferredSize(new Dimension(320,200));
 			panel.add(detailsScrollPane, BorderLayout.CENTER);
 			pack();
 			repaint();
 		}
 	}
 
 	/**
 	 * This method hides the panel thats has the genetics information
 	 */
 	private void hideDetails() {
 		showHideDetails.setText(showDetails);
 		showHideDetails.setIcon(closeIcon);
 		JPanel panel = (JPanel) showHideDetails.getParent();
 		if (panel.getComponentCount() == 2) {
 			panel.remove(detailsScrollPane);
 			int dialogWidth = this.getWidth();
 			dialogHeight -= 200;
 			setSize(dialogWidth, dialogHeight);
 			pack();
 			repaint();
 		}
 	}
 
 	public void setIsSelected(boolean b) {
 		isSelected = b;
 		if (isSelected) {
			superPanel.setBorder(BorderFactory.createLineBorder(selectedColor, 2));
 		} else {
			superPanel.setBorder(BorderFactory.createLineBorder(unselectedColor, 2));
 		}
		superPanel.repaint();
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * Default implementation for the windowlistener class method
 	 * 
 	 * @param e
 	 *            windowevent
 	 */
 	public void windowActivated(WindowEvent e) {
 	}
 
 	/**
 	 * Default implementation for the windowlistener class method
 	 * 
 	 * @param e
 	 *            windowevent
 	 */
 	public void windowClosed(WindowEvent e) {
 	}
 
 	/**
 	 * Implementation for the windowlistener class method. This method handles
 	 * the window closing of the dialog.
 	 * 
 	 * @param e
 	 *            windowevent
 	 */
 	public void windowClosing(WindowEvent e) {
 		int ans = JOptionPane.showConfirmDialog(this,
 				"Are you sure you want to\n" 
 				+ "close Cage #" + id + "?", "Closing Cage",
 				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
 
 		if (ans == JOptionPane.YES_OPTION)
 			this.setVisible(false);
 	}
 
 	/**
 	 * Default implementation for the windowlistener class method
 	 * 
 	 * @param e
 	 *            windowevent
 	 */
 	public void windowDeactivated(WindowEvent e) {
 	}
 
 	/**
 	 * Default implementation for the windowlistener class method
 	 * 
 	 * @param e
 	 *            windowevent
 	 */
 	public void windowDeiconified(WindowEvent e) {
 	}
 
 	/**
 	 * Default implementation for the windowlistener class method
 	 * 
 	 * @param e
 	 *            windowevent
 	 */
 	public void windowIconified(WindowEvent e) {
 	}
 
 	/**
 	 * Default implementation for the windowlistener class method
 	 * 
 	 * @param e
 	 *            windowevent
 	 */
 	public void windowOpened(WindowEvent e) {
 	}
 
 	/**
 	 * Getter method to access the Cage object associated with this UI
 	 * 
 	 * @return the Cage object
 	 */
 	public Cage getCage() {
 		return cage;
 	}
 
 	/**
 	 * Getter method to access the OrganismUIs for the parents for the cage of
 	 * this UI
 	 * 
 	 * @return the array containing the OrganismUIs of the parents
 	 */
 	public OrganismUI[] getParentUIs() {
 		if (parentOrganismUIs != null) {
 			if (parentOrganismUIs[0] != null && parentOrganismUIs[1] != null)
 				return parentOrganismUIs;
 			else
 				return null;
 		} else
 			return null;
 	}
 
 	/**
 	 * This method returns the OrganismUI of the Organism with the sent id.
 	 * 
 	 * @param id
 	 *            The index of the organism
 	 * @return the OrganismUI of the organism
 	 */
 	public OrganismUI getOrganismUIFor(int id) {
 		for (int i = 0; i < 2 * numPhenosPresent; i++) {
 			for (int j = 0; j < maxOrgsInOneRow; j++) {
 				OrganismUI organismUI = ((OrganismUI) (childrenOrganismUIs[i][j]));
 				if (organismUI != null) {
 					if (organismUI.getOrganism().getId() == id)
 						return organismUI;
 				} else
 					break;
 			}
 		}
 		return null;
 	}
 
 	public void mouseClicked(MouseEvent e) {
 		if (e.getClickCount() == 2) {
 			if (isSelected) {
				superPanel.setBorder(BorderFactory.createLineBorder(unselectedColor, 2));
 				isSelected = false;
 				summaryChartManager.removeFromSelected(this);
 			} else {
				superPanel.setBorder(BorderFactory.createLineBorder(selectedColor, 2));
 				isSelected = true;
 				summaryChartManager.addToSelected(this);
 			}
			superPanel.repaint();
 		}
 	}
 
 	public void mouseEntered(MouseEvent e) {}
 
 	public void mouseExited(MouseEvent e) {}
 
 	public void mousePressed(MouseEvent e) {}
 
 	public void mouseReleased(MouseEvent e) {}
 
 	public int compareTo(CageUI o) {
 		return id - o.getId();
 	}
 
 }
