 
 import java.awt.BorderLayout;
 import java.awt.Component;
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
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JToggleButton;
 import javax.swing.border.Border;
 
 /**
  * Nikunj Koolar cs681-3 Fall 2002 - Spring 2003 Project VGL File:
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
  * @author Nikunj Koolar
 * @version 1.0 $Id: CageUI.java,v 1.2 2005-06-09 17:16:48 brian Exp $
  */
 public class CageUI extends JDialog implements WindowListener {
 	/**
 	 * The Id for the Cage. This value is always one more than the id of the
 	 * cage it holds. This is because the Cage id's begin from 0, but on screen
 	 * they have to be shown beginning from 1.
 	 */
 	private int m_Id;
 
 	/**
 	 * Parameter to the set the width of the dialog
 	 */
 	private int m_DialogWidth;
 
 	/**
 	 * Parameter to the set the height of the dialog
 	 */
 	private int m_DialogHeight;
 
 	/**
 	 * Parameter to the set the X-coordinate of the dialog
 	 */
 	private int m_DialogLocationX;
 
 	/**
 	 * Parameter to the set the Y-coordinate of the dialog
 	 */
 	private int m_DialogLocationY;
 
 	/**
 	 * This panel holds the entire list of Organism UIs for the Cage
 	 */
 	private JPanel m_OrganismsPanel;
 
 	/**
 	 * This panel holds the numeric count of each sex type thats currently
 	 * present in the cage
 	 */
 	private JPanel m_CountsPanel;
 
 	/**
 	 * This panel holds the Image for each phenotype associated with the
 	 * organisms in the cage
 	 */
 	private JPanel m_PicturesPanel;
 
 	/**
 	 * This panel holds the set of Organisms,their Counts and their pictures for
 	 * each phenotype.
 	 */
 	private JPanel m_IndividualPanel;
 
 	/**
 	 * The panel that contains all the subpanels.
 	 */
 	private JPanel m_SuperPanel;
 
 	/**
 	 * This panel holds the Individual Panel
 	 */
 	private JPanel m_DetailsPanel;
 
 	/**
 	 * This panel contains the info about the parents of the organisms of this
 	 * cage, or if the cage is the initial population cage then it holds the
 	 * information about the Genetic Model currently in use
 	 */
 	private JPanel m_ParentInfoPanel;
 
 	/**
 	 * This variable stores the count of the number of different phenotypes
 	 * associated with this cage
 	 */
 	private int m_Size;
 
 	/**
 	 * This variable keeps track of the Icon Image associated with the frame
 	 * holding this cage
 	 */
 	private Image m_Image;
 
 	/**
 	 * This variable holds the value of the Character that is currently being
 	 * studied
 	 */
 	private String m_Trait;
 
 	/**
 	 * This variable stores a reference to the hashmap of children associated
 	 * with this cage
 	 */
 	private HashMap m_Children;
 
 	/**
 	 * This variable stores a reference to the list of parents associated with
 	 * this cage
 	 */
 	private ArrayList m_Parents;
 
 	/**
 	 * Holds the array of labels associated with each of the phenotypes
 	 */
 	private JLabel[] m_Phenotypes;
 
 	/**
 	 * Holds the array of buttons associated with each of the phenotype images
 	 */
 	private JButton[] m_PhenotypeImages;
 
 	/**
 	 * Holds the array of images associated with each of the phenotypes
 	 */
 	private ImageIcon[] m_Images;
 
 	/**
 	 * Holds the array of smaller images associated with each of the phenotypes
 	 */
 	private ImageIcon[] m_ImagesSmall;
 
 	/**
 	 * An array of arraylists. Each of the arraylists is the set of organisms of
 	 * a particular phenotype
 	 */
 	private ArrayList[] m_ASet;
 
 	/**
 	 * An array of OLists. Each of the OLists is the object storing the set of
 	 * organims of a particular phenotype along with other details about the
 	 * set.
 	 */
 	private OList[] m_Set;
 
 	/**
 	 * A reference to the Cage object being displayed through this UI
 	 */
 	private Cage m_Cage;
 
 	/**
 	 * A reference to the selectionvial object that keeps track of the currently
 	 * selected male and female organisms for crossing.
 	 */
 	private SelectionVial m_Vial;
 
 	/**
 	 * This variable stores the details about the Genetic Model currently being
 	 * used.
 	 */
 	private String m_Details = null;
 
 	/**
 	 * This widget displays the informations stored in the m_Details variable.
 	 */
 	private JTextArea m_TextDetails;
 
 	/**
 	 * This is the button used to show/hide the Genetics information.
 	 */
 	private JToggleButton m_ShowHideDetails;
 
 	/**
 	 * This string holds the heading to be displayed on the toggle button when
 	 * the Genetics Details are being shown
 	 */
 	private String m_HideDetails = "hide model & genotypes";
 
 	/**
 	 * This string holds the heading to be displayed on the toggle button when
 	 * the Genetics Details are not being shown
 	 */
 	private String m_ShowDetails = "show model & genotypes";
 
 	/**
 	 * This icon is displayed in the toggle button when the Genetics Details are
 	 * not being shown
 	 */
 	private URL m_CloseURL = CageUI.class.getResource("images/close.gif");
 
 	private ImageIcon m_CloseIcon = new ImageIcon(m_CloseURL);
 
 	/**
 	 * This icon is displayed in the toggle button when the Genetics Details are
 	 * being shown.
 	 */
 	private URL m_DownURL = CageUI.class.getResource("images/down.gif");
 
 	private ImageIcon m_DownIcon = new ImageIcon(m_DownURL);
 
 	/**
 	 * This variable is used to decide the following: a. Whether to display the
 	 * Genetics Model details in cage 1 b. Whether to allow the individual
 	 * organisms to display allele information in balloon help active mode.
 	 */
 	private boolean m_IsBeginner = false;
 
 	/**
 	 * Array of Parent Organisms. This array was initially concieved to hold
 	 * simply the images of the parents (which explains its naming) but later
 	 * on, in order to support linking between parents and their corresponding
 	 * objects in the original cages where they were present, this array was
 	 * then used to store parent OrganismUI objects.
 	 */
 	private OrganismUI[] m_ParentImages;
 
 	/**
 	 * This array of arraylists stores the organismUIs for all the organisms of
 	 * all the phenotypes associated with this cage. For eg. If the cage
 	 * contains 1 phenotype then this variable will be [2][20] in size. If the
 	 * cage contains 2 phenotypes then this variable will be [4][20] in size.
 	 * i.e. there are two rows of maximum 40 organisms associated with each
 	 * phenotype. (This figure of 40 has been hard coded) on my side on the
 	 * basis of the information I got from the developer who coded the backend
 	 * where he/she decided to keep the maximum number of organisms collectively
 	 * possible for a cage to be 40. I probably should had asked for a getter
 	 * method to get this max. figure from the backend.
 	 */
 	private OrganismUI[][] m_OrganismImages;
 
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
 	public CageUI(Frame importFrame, boolean isbeginnersmode, Cage cage,
 			SelectionVial sv, String trait, String details) {
 		//initialize parent
 		super(importFrame, false);
 		addWindowListener(this);
 		m_IsBeginner = isbeginnersmode;
 		m_Cage = cage;
 		m_Vial = sv;
 		m_Trait = trait;
 		m_Image = importFrame.getIconImage();
 		m_Id = m_Cage.getId() + 1;
 		m_Children = m_Cage.getChildren();
 		m_Parents = m_Cage.getParents();
 		if (m_Id == 1)
 			if (details != null)
 				m_Details = details;
 		setTitle("Cage " + (new Integer(m_Id)).toString());
 		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
 		setupSubComponents();
 		setupDialogBox(importFrame, m_Size);
 		setResizable(false);
 		//setup the GUI of its internal components
 		components();
 		pack();
 		show();
 	}
 
 	/**
 	 * This method initializes the objects and widgets that store information
 	 * about the various phenotypes associated with the cage.
 	 */
 	private void setupSubComponents() {
 		Set phenotypes = m_Children.keySet();
 		Iterator it1 = phenotypes.iterator();
 		m_Size = phenotypes.size();
 		String[] phenotypeNames = new String[m_Size];
 		m_Phenotypes = new JLabel[m_Size];
 		m_ImagesSmall = new ImageIcon[m_Size];
 		m_Images = new ImageIcon[m_Size];
 		m_PhenotypeImages = new JButton[m_Size];
 		m_Set = new OList[m_Size];
 		m_ASet = new ArrayList[m_Size];
 		int i = 0;
 		String directory = "UIimages/";
 		String fileName;
 		String fileNameSmall;
 		while (it1.hasNext()) {
 			phenotypeNames[i] = new String((String) it1.next());
 			m_Set[i] = (OList) m_Children.get(phenotypeNames[i]);
 			m_ASet[i] = m_Set[i].getArrayList();
 			if (m_Trait.equals("Legs"))
 				m_Phenotypes[i] = new JLabel(phenotypeNames[i] + " " + m_Trait);
 			else
 				m_Phenotypes[i] = new JLabel(phenotypeNames[i]);
 			fileName = m_Trait + "_" + phenotypeNames[i];
 			fileNameSmall = fileName + "_" + "s.gif";
 			URL m_ImageSmallURL = CageUI.class.getResource(directory
 					+ fileNameSmall);
 			m_ImagesSmall[i] = new ImageIcon(m_ImageSmallURL);
 			URL m_ImageURL = CageUI.class.getResource(directory + fileName
 					+ ".gif");
 			m_Images[i] = new ImageIcon(m_ImageURL);
 			m_PhenotypeImages[i] = new JButton(m_ImagesSmall[i]);
 			m_PhenotypeImages[i].addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					JFrame frame = new JFrame();
 					frame.setIconImage(m_Image);
 					JDialog imageDlg = new JDialog(frame, "Phenotype Details",
 							true);
 					imageDlg.setSize(200, 220);
 					imageDlg.setLocation(512 - 100, 384 - 110);
 					imageDlg.setResizable(false);
 					JPanel details = new JPanel();
 					details.setLayout(new BorderLayout());
 					details.setBorder(BorderFactory.createEtchedBorder());
 					String path = ((JButton) evt.getSource()).getIcon()
 							.toString();
 					int endIndex = path.indexOf("_s.gif");
 					int splitter = path.indexOf("!");
 					int beginIndex = path.indexOf("_", splitter);
 					String phenotypeName = path.substring(beginIndex + 1,
 							endIndex);
 					String trait = path.substring(path.lastIndexOf("/") + 1,
 							beginIndex);
 					String newPath = path.substring(0, endIndex) + ".gif";
 					JLabel phenotype = new JLabel(trait + ": " + phenotypeName);
 					phenotype
 							.setHorizontalTextPosition(javax.swing.JLabel.CENTER);
 					phenotype.setHorizontalAlignment(javax.swing.JLabel.CENTER);
 					details.add(phenotype, BorderLayout.NORTH);
 					try {
 						URL newPathURL = new URL(newPath);
 						details.add(new JLabel(new ImageIcon(newPathURL)),
 								BorderLayout.CENTER);
 						imageDlg.getContentPane().add(details);
 					} catch (MalformedURLException mue) {
 					}
 					imageDlg.setVisible(true);
 				}
 			});
 			m_Phenotypes[i]
 					.setHorizontalTextPosition(javax.swing.JLabel.CENTER);
 			m_Phenotypes[i].setHorizontalAlignment(javax.swing.JLabel.CENTER);
 			m_PhenotypeImages[i].setPreferredSize(new Dimension(35, 35));
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
 		setContentPane(m_SuperPanel);
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
 		if (panelCount == 1)
 			m_DialogHeight = ht;
 		else if (panelCount == 2)
 			m_DialogHeight = ht + (int) (ht / 3);
 		else if (panelCount == 3)
 			m_DialogHeight = ht + (int) ((2 * ht) / 3);
 		m_DialogWidth = (int) (550);
 		m_DialogLocationX = (int) (dtWidth / 2) - m_DialogWidth / 2;
 		m_DialogLocationY = (int) (dtHeight / 2) - m_DialogHeight / 2;
 		setSize(m_DialogWidth, m_DialogHeight);
 		setLocation(new Point(m_DialogLocationX, m_DialogLocationY));
 	}
 
 	/**
 	 * This method sets up the panels for the Cage
 	 */
 	private void setupOrganismPanel() {
 		Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
 		BorderLayout bSelectionLayout = new BorderLayout();
 		m_SuperPanel = new JPanel();
 		m_SuperPanel.setLayout(bSelectionLayout);
 		m_SuperPanel.setBorder(BorderFactory.createEmptyBorder());
 		m_DetailsPanel = new JPanel();
 		m_DetailsPanel.setLayout(new BorderLayout());
 		m_IndividualPanel = new JPanel();
 		FlowLayout fl = new FlowLayout();
 		fl.setHgap(1);
 		fl.setVgap(1);
 		m_IndividualPanel.setLayout(fl);
 		m_OrganismsPanel = new JPanel();
 		m_OrganismsPanel.setLayout(new BorderLayout());
 		m_OrganismsPanel.setBorder(BorderFactory.createTitledBorder(
 				emptyBorder, "Individual Animals",
 				javax.swing.border.TitledBorder.CENTER,
 				javax.swing.border.TitledBorder.ABOVE_TOP));
 		m_CountsPanel = new JPanel();
 		m_CountsPanel.setLayout(new BorderLayout());
 		m_CountsPanel.setBorder(BorderFactory.createTitledBorder(emptyBorder,
 				"Count", javax.swing.border.TitledBorder.CENTER,
 				javax.swing.border.TitledBorder.ABOVE_TOP));
 		m_PicturesPanel = new JPanel();
 		m_PicturesPanel.setLayout(new BorderLayout());
 		m_PicturesPanel.setBorder(BorderFactory.createTitledBorder(emptyBorder,
 				m_Trait, javax.swing.border.TitledBorder.CENTER,
 				javax.swing.border.TitledBorder.ABOVE_TOP));
 
 		m_OrganismImages = new OrganismUI[2 * m_Size][20];
 		//For each phenotype, setup its own panels for organismUIs,count and
 		//pictures and add them to the right places in the organismpanel,
 		// countspanel
 		//and the picturespanel
 		for (int i = 0; i < m_Size; i++)
 			setupIndividualPanel(i);
 
 		m_IndividualPanel.add(m_OrganismsPanel);
 		m_IndividualPanel.add(m_CountsPanel);
 		m_IndividualPanel.add(m_PicturesPanel);
 		m_DetailsPanel.add(m_IndividualPanel, BorderLayout.NORTH);
 		m_SuperPanel.add(m_DetailsPanel, BorderLayout.NORTH);
 	}
 
 	/**
 	 * This method sets up the panels and the OrganismUIs for each phenotype
 	 * associated with this cage.
 	 * 
 	 * @param number
 	 *            index of the phenotype in the list of phenotypes for which the
 	 *            panels are being set up.
 	 */
 	private void setupIndividualPanel(int number) {
 		Border etched = BorderFactory.createEtchedBorder();
 		Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
 		JPanel organisms1Panel = new JPanel();
 		JPanel organisms2Panel = new JPanel();
 		Component filler = Box.createRigidArea(new Dimension(15, 15));
 		GridLayout gridlt = new GridLayout(1, 20);
 		gridlt.setHgap(1);
 		gridlt.setVgap(2);
 		organisms1Panel.setLayout(gridlt);
 		organisms2Panel.setLayout(gridlt);
 		OrganismUI[] organism1Images = m_OrganismImages[2 * number];
 		OrganismUI[] organism2Images = m_OrganismImages[2 * number + 1];
 		Iterator it = m_ASet[number].iterator();
 		int count = 0;
 		int i = 0;
 		int j = 0;
 		while (it.hasNext()) {
 			Organism o1 = (Organism) it.next();
 			count++;
 			if (count <= 20) {
 				organism1Images[i] = new OrganismUI(o1, false, m_IsBeginner,
 						m_Vial);
 				organisms1Panel.add(organism1Images[i]);
 				i++;
 			} else {
 				organism2Images[j] = new OrganismUI(o1, false, m_IsBeginner,
 						m_Vial);
 				organisms2Panel.add(organism2Images[j]);
 				j++;
 			}
 		}
 		if (i < 20) {
 			while (i < 20) {
 				organisms1Panel.add(Box.createRigidArea(new Dimension(15, 15)));
 				i++;
 			}
 		}
 		if (j < 20) {
 			while (j < 20) {
 				organisms2Panel.add(Box.createRigidArea(new Dimension(15, 15)));
 				j++;
 			}
 		}
 		JPanel organismPanel = new JPanel();
 		GridLayout gl = new GridLayout(2, 0);
 		gl.setVgap(4);
 		organismPanel.setLayout(gl);
 		organismPanel.setBorder(etched);
 		organismPanel.add(organisms1Panel);
 		organismPanel.add(organisms2Panel);
 
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
 		String mCount = (new Integer(m_Set[number].getMaleCount())).toString();
 		if (m_Set[number].getMaleCount() < 10)
 			mCount = "0" + mCount;
 		JLabel maleCountLabel = new JLabel(mCount);
 		String fCount = (new Integer(m_Set[number].getFemaleCount()))
 				.toString();
 		if (m_Set[number].getFemaleCount() < 10)
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
 		picturePanel.add(m_Phenotypes[number], BorderLayout.CENTER);
 		picturePanel.add(m_PhenotypeImages[number], BorderLayout.EAST);
 		JLabel trait = new JLabel(m_Trait);
 
 		if (number == 0) {
 			m_OrganismsPanel.add(organismPanel, BorderLayout.NORTH);
 			m_CountsPanel.add(countPanel, BorderLayout.NORTH);
 			m_PicturesPanel.add(picturePanel, BorderLayout.NORTH);
 		} else if (number == 1) {
 			m_OrganismsPanel.add(organismPanel, BorderLayout.CENTER);
 			m_CountsPanel.add(countPanel, BorderLayout.CENTER);
 			m_PicturesPanel.add(picturePanel, BorderLayout.CENTER);
 		} else if (number == 2) {
 			m_OrganismsPanel.add(organismPanel, BorderLayout.SOUTH);
 			m_CountsPanel.add(countPanel, BorderLayout.SOUTH);
 			m_PicturesPanel.add(picturePanel, BorderLayout.SOUTH);
 		}
 	}
 
 	/**
 	 * This method sets up the Panel that display the information about the
 	 * parents or if the Cage id is 1 and beginner's mode is true then it
 	 * displays the details about the underlying genetics model
 	 */
 	private void setupParentInfoPanel() {
 		m_ParentInfoPanel = new JPanel();
 		if (m_Id > 1) {
 			JLabel parentLabel = new JLabel("Parent");
 			m_ParentInfoPanel.add(parentLabel);
 			m_ParentImages = new OrganismUI[2];
 			Organism o1 = (Organism) m_Parents.get(0);
 			Organism o2 = (Organism) m_Parents.get(1);
 			int cageId = o1.getCageId() + 1;
 			String phenoName1 = o1.getPhenotype();
 			m_ParentImages[0] = new OrganismUI(o1, true, m_IsBeginner, m_Vial);
 			m_ParentInfoPanel.add(m_ParentImages[0]);
 			m_ParentInfoPanel.add(new JLabel("(" + cageId + ")"));
			m_ParentInfoPanel.add(new JLabel(phenoName1));
 			cageId = o2.getCageId() + 1;
 			String phenoName2 = o2.getPhenotype();
 			m_ParentImages[1] = new OrganismUI(o2, true, m_IsBeginner, m_Vial);
 			m_ParentInfoPanel.add(m_ParentImages[1]);
 			m_ParentInfoPanel.add(new JLabel("(" + cageId + ")"));
			m_ParentInfoPanel.add(new JLabel(phenoName2));
 		} else {
 			if (m_IsBeginner) {
 				if (m_Details != null) {
 					m_TextDetails = new JTextArea(m_Details);
 					m_TextDetails.setEditable(false);
 					m_TextDetails.setBackground(m_ParentInfoPanel
 							.getBackground());
 					m_TextDetails.setBorder(BorderFactory.createEtchedBorder());
 					m_ShowHideDetails = new JToggleButton();
 					m_TextDetails.setFont(m_ShowHideDetails.getFont());
 					m_ShowHideDetails.addItemListener(new ItemListener() {
 						public void itemStateChanged(ItemEvent e) {
 							JToggleButton jtb = (JToggleButton) e.getSource();
 							if (e.getStateChange() == ItemEvent.DESELECTED) {
 								showDetails();
 							} else if (e.getStateChange() == ItemEvent.SELECTED) {
 								hideDetails();
 							}
 						}
 					});
 					JPanel showHidePanel = new JPanel();
 					showHidePanel.setLayout(new BorderLayout());
 					showHidePanel.add(m_ShowHideDetails, BorderLayout.NORTH);
 					m_ParentInfoPanel.add(showHidePanel);
 					m_ShowHideDetails.setSelected(true);
 				}
 			}
 		}
 		m_SuperPanel.add(m_ParentInfoPanel, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * This method sets up the Panel that displays the genetics details when the
 	 * toggle button is pressed
 	 */
 	private void showDetails() {
 		m_ShowHideDetails.setText(m_HideDetails);
 		m_ShowHideDetails.setIcon(m_DownIcon);
 		JPanel panel = (JPanel) m_ShowHideDetails.getParent();
 		if (panel.getComponentCount() == 1) {
 			m_DialogHeight += 200;
 			int dialogWidth = this.getWidth();
 			setSize(dialogWidth, m_DialogHeight);
 			int hgt = panel.getHeight();
 			panel.add(m_TextDetails, BorderLayout.CENTER);
 			pack();
 			repaint();
 		}
 	}
 
 	/**
 	 * This method hides the panel thats has the genetics information
 	 */
 	private void hideDetails() {
 		m_ShowHideDetails.setText(m_ShowDetails);
 		m_ShowHideDetails.setIcon(m_CloseIcon);
 		JPanel panel = (JPanel) m_ShowHideDetails.getParent();
 		if (panel.getComponentCount() == 2) {
 			panel.remove(m_TextDetails);
 			int dialogWidth = this.getWidth();
 			int hgt = panel.getHeight();
 			m_DialogHeight -= 200;
 			setSize(dialogWidth, m_DialogHeight);
 			pack();
 			repaint();
 		}
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
 				+ "close Cage #" + m_Id + "?", "Closing Cage",
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
 		return m_Cage;
 	}
 
 	/**
 	 * Getter method to access the OrganismUIs for the parents for the cage of
 	 * this UI
 	 * 
 	 * @return the array containing the OrganismUIs of the parents
 	 */
 	public OrganismUI[] getParentUIs() {
 		if (m_ParentImages != null) {
 			if (m_ParentImages[0] != null && m_ParentImages[1] != null)
 				return m_ParentImages;
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
 		for (int i = 0; i < 2 * m_Size; i++) {
 			for (int j = 0; j < 20; j++) {
 				OrganismUI organismUI = ((OrganismUI) (m_OrganismImages[i][j]));
 				if (organismUI != null) {
 					if (organismUI.getOrganism().getId() == id)
 						return organismUI;
 				} else
 					break;
 			}
 		}
 		return null;
 	}
 
 	public void setBalloonHelp(boolean value) {
 		for (int i = 0; i < 2 * m_Size; i++) {
 			for (int j = 0; j < 20; j++) {
 				OrganismUI organismUI = ((OrganismUI) (m_OrganismImages[i][j]));
 				if (organismUI != null) {
 					organismUI.setBalloonHelp(value);
 				} else
 					break;
 			}
 		}
 		if (m_ParentImages != null) {
 			if (m_ParentImages[0] != null && m_ParentImages[1] != null) {
 				m_ParentImages[0].setBalloonHelp(value);
 				m_ParentImages[1].setBalloonHelp(value);
 			}
 		}
 	}
 }
