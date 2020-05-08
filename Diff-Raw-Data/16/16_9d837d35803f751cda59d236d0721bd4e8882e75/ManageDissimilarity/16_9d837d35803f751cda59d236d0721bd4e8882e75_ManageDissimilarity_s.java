 package moduls.frm.children;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Dialog.ModalityType;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import operonClustering.CustomDissimilarity;
 
 import moduls.frm.FrmPrincipalDesk;
 
 public class ManageDissimilarity extends JDialog implements ActionListener{
 
 	//Fields
 	private FrmPrincipalDesk f;
 	private JPanel jp;
 	private JScrollPane jsp;
 	private File ReferenceDirectory = null;
 	
 	//GUI fields
 	//Name/intro
 	private JLabel Add;
 	private JTextField DMNameLabel, DMName;
 	private String strDMNameLabel = "Enter Name: ";
 	private Insets TextInsets = new Insets(1,5,1,1);
 	
 	//Options
 	private JLabel LblFactor, LblWeight, LblImportance, LblAmalgamation;
 	private String strLblFactor = " FACTOR:";
 	private String strLblWeight = " WEIGHT";
 	private String strLblImportance = " IMPORTANCE";
 	private String strAmalgamation = " AMALGAMATION TYPE:";
 	private JRadioButton radLinear;
 	private JRadioButton radScaleHierarchy;
 	private String strradLinear = "Linear";
 	private String strradScaleHierarchy = "Scale Hierarchy";
 	private ButtonGroup AmalgamationType;
 	
 	//checkboxes
 	private JCheckBox chkCommonGenes;
 	private JCheckBox chkCommonMotifs;
 	private JCheckBox chkGeneOrder;
 	private JCheckBox chkGeneGaps;
 	private JCheckBox chkStrandedness;
 	private String strCommonGenes = "Presence / absence of common genes";
 	private String strCommonMotifs = "Presence / absence of common motifs";
 	private String strGeneOrder = "Changes in gene order";
 	private String strGeneGaps = "Changes in intragenic gap size";
 	private String strStrandedness = "Changes in strandedness";
 	private LinkedList<Component> grpCommonGenes = new LinkedList<Component>();
 	private LinkedList<Component> grpCommonMotifs = new LinkedList<Component>();
 	private LinkedList<Component> grpGeneOrder = new LinkedList<Component>();
 	private LinkedList<Component> grpGeneGaps = new LinkedList<Component>();
 	private LinkedList<Component> grpStrandedness = new LinkedList<Component>();
 	
 	//amalgamation technique - related
 	private LinkedList<Component> grpLinear = new LinkedList<Component>();
 	private LinkedList<Component> grpScaleHierarchy = new LinkedList<Component>();
 	private String TxtLinear = "Weight:"; 
 	private String TxtScale = "Importance:";
 	
 	//(1) COMMON GENES
 	private JTextField LblcgWeight, TxtcgWeight, LblcgScale, TxtcgScale;
 	private String strTxtcgWeight = "0.3";
 	private String strTxtcgScale = "1";
 	private JCheckBox chkTreatDuplicatesAsUnique;
 	private String strTreatDuplicatesAsUnique = "Treat duplicate genes as unique";
 	private JRadioButton radDice, radJaccard;
 	private String strDice = "Dice's Coefficient";
 	private String strJaccard = "Jaccard Index";
 	private ButtonGroup DiceOrJaccard;
 	
 	//(2) COMMON MOTIFS
 	private JTextField LblcmWeight, TxtcmWeight, LblcmScale, TxtcmScale;
 	private String strTxtcmWeight = "0.25";
 	private String strTxtcmScale = "2";
 	private JTextField LblSelectMotifs,LblComparisonScheme;
 	private String strSelectMotifs = " Select Motifs:";
 	private String strComparisonScheme = " Comparison Scheme:";
 	private JPanel AvailableMotifsPanel;
 	private CheckCombo AvailableMotifsBox;
 	private JRadioButton radDiceMotif, radJaccardMotif;
 	private ButtonGroup DiceOrJaccardMotif;
 	private JCheckBox chkTreatDuplicatesAsUniqueMotif;
 	private String strTreatDuplicatesAsUniqueMotif = "Treat duplicate motifs as unique";
 	
 	//(3) GENE ORDER
 	private JTextField LblgoWeight, TxtgoWeight, LblgoScale, TxtgoScale;
 	private String strTxtgoWeight = "0.2";
 	private String strTxtgoScale = "3";
 	private JCheckBox chkHeadPos, chkPairOrd;
 	private String strHeadPos = "Percent conserved gene position from head";
 	private String strPairOrd = "Percent conserved collinear gene pairs";
 	private JTextField LblwtHead, LblwtPair, TxtwtHead, TxtwtPair;
 	private String strLblwtOrd = " Relative Weight:";
 	private String strTxtwtHead = "0.5";
 	private String strTxtwtPair = "0.5";
 	
 	// (4) GENE GAPS
 	private JTextField LblggWeight, TxtggWeight, LblggScale, TxtggScale;
 	private String strTxtggWeight = "0.15";
 	private String strTxtggScale = "4";
 	private JRadioButton radThreshold, radInterpolation;
 	private ButtonGroup ThresholdOrInterpolation;
 	private String strThreshold = "Threshold";
 	private String strInterpolation = "Linear Interpolation";
 	private JTextField EnterPointsLabel;
 	private JTextArea EnterPointsTxt;
 	private String strEnterPointsLabel = "Enter points as:       gap_size dissimilarity;";
 	private JButton btnLoadFromFile;
 	private String strLoad = "Load points from file";
 
 	// (5) STRANDEDNESS
 	private JTextField LblssWeight, TxtssWeight, LblssScale, TxtssScale;
 	private String strTxtssWeight = "0.10";
 	private String strTxtssScale = "5";
 	private JCheckBox chkIndStrand, chkGrpStrand;
 	private String strIndStrand = "Change in strandedness of individual genes";
 	private String strGrpStrand = "Change in strandedness of entire group";
 	private JTextField LblwtInd, LblwtGrp, TxtwtInd, TxtwtGrp;
 	private String strLblwt = " Relative Weight:";
 	private String strTxtwtInd = "0.5";
 	private String strTxtwtGrp = "0.5";
 	private int StrColNum = 10;
 	
 	//Add button
 	private JButton btnAddDM, btnSelectAll, btnDeselectAll;
 	private String strAddDM = "Add Dissimilarity Measure";
 	private String strSelectAll = "Select All";
 	private String strDeselectAll = "Deselect all";
 	
 	//Remove button
 	private JLabel RemoveDM;
 	private JTextField LblDissimilarity;
 	private String LblstrRemoveDM = " REMOVE A DISSIMILARITY MEASURE";
 	private String strLblDissimilarity = "Dissimilarity Measure:";
 	private JComboBox<String> MenuDM;
 	private String[] CurrentDM;
 	private JButton btnRemoveDM;
 	private String strRemoveDM = "Remove";
 	
 	//Submit button
 	private JButton btnOK;
 	private String strbtnOK = "Submit";
 	
 	//constructor
 	public ManageDissimilarity(FrmPrincipalDesk f){
 		super();
 		this.f = f;
 		
		this.setSize(700,400);
 		this.setTitle("Manage Dissimilarity Measures");
 		this.setModalityType(ModalityType.DOCUMENT_MODAL);
 		this.setModal(true);
 		
 		//retrieve current list
 		String[] InitialDissimilarity = new String[f.getPan_Menu().getCbDissimilarity().getItemCount()];
 		for (int i = 0; i < f.getPan_Menu().getCbDissimilarity().getItemCount(); i++){
 			InitialDissimilarity[i] = (String)f.getPan_Menu().getCbDissimilarity().getItemAt(i);
 		}
 		this.CurrentDM = InitialDissimilarity;
 		
 		//build + initialize panel
 		this.getPanel();
 		DeactivateAllComponents();
 		
 		this.setLocationRelativeTo(null);
 		this.setVisible(true);
 	}
 	
 	//build panel
 	public void getPanel(){
 		
 		//initial GridBagLayout parameters
 		jp = new JPanel();
 		jp.setLayout(new GridBagLayout());
 		final GridBagConstraints c = new GridBagConstraints();
 		int gridy = 0;
 		
 		//initial GridBagLayout parameters
 		c.anchor = GridBagConstraints.FIRST_LINE_START;
 		c.weightx = 1;
 		
 		//Add Dissimilarity Measure Heading
 		c.ipady = 7;
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 6;
 		Add = new JLabel(" ADD A CUSTOM DISSIMILARITY METRIC");
 		Add.setBackground(Color.GRAY);
 		Add.setOpaque(true);
 		jp.add(Add,c);
 		gridy++;
 		
 		//Name Label
 		c.ipady = 7;
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = TextInsets;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		DMNameLabel = new JTextField(strDMNameLabel);
 		DMNameLabel.setEditable(false);
 		DMNameLabel.setBorder(null);
 		jp.add(DMNameLabel,c);
 		
 		//the name itself text field
 		c.ipady = 7;
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridwidth = 5;
 		c.gridheight = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		DMName = new JTextField("");
 		DMName.setEditable(true);
 		DMName.addActionListener(this);
 		jp.add(DMName, c);
 		gridy++;
 		
 		//AMALGAMATION
 		//Heading
 		c.ipady = 7;
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 6;
 		LblAmalgamation = new JLabel(strAmalgamation);
 		LblAmalgamation.setBackground(Color.GRAY);
 		LblAmalgamation.setOpaque(true);
 		jp.add(LblAmalgamation,c);
 		gridy++;
 		
 		//radio buttons - initialization
 		AmalgamationType = new ButtonGroup();
 		radLinear = new JRadioButton(strradLinear);
 		radScaleHierarchy = new JRadioButton(strradScaleHierarchy);
 		AmalgamationType.add(radLinear);
 		AmalgamationType.add(radScaleHierarchy);
 		radLinear.setSelected(true);
 		
 		//radio buttons
 		c.gridx = 2;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.insets = new Insets(5,1,1,1);
 		c.fill = GridBagConstraints.NONE;
 		radLinear.addActionListener(this);
 		jp.add(radLinear, c);
 		
 		c.gridx = 4;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.NONE;
 		radScaleHierarchy.addActionListener(this);
 		jp.add(radScaleHierarchy, c);
 		gridy++;
 
 		//Add Factor Heading
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 2;
 		LblFactor = new JLabel(strLblFactor);
 		LblFactor.setBackground(Color.GRAY);
 		LblFactor.setOpaque(true);
 		jp.add(LblFactor,c);
 		
 		//Add Weight Heading
 		c.gridx = 2;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 2;
 		LblWeight = new JLabel(strLblWeight);
 		LblWeight.setBackground(Color.GRAY);
 		LblWeight.setOpaque(true);
 		jp.add(LblWeight,c);
 		
 		//Add Importance Heading
 		c.gridx = 4;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.gridwidth = 2;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 2;
 		LblImportance = new JLabel(strLblImportance);
 		LblImportance.setBackground(Color.GRAY);
 		LblImportance.setOpaque(true);
 		gridy++;
 		jp.add(LblImportance,c);
 		gridy++;
 		
 		//checkboxes
 		chkCommonGenes = new JCheckBox(strCommonGenes);
 		chkCommonMotifs = new JCheckBox(strCommonMotifs);
 		chkGeneOrder = new JCheckBox(strGeneOrder);
 		chkGeneGaps = new JCheckBox(strGeneGaps);
 		chkStrandedness = new JCheckBox(strStrandedness);
 	
 		//Select/deselect buttons
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		btnSelectAll = new JButton(strSelectAll);
 		btnSelectAll.addActionListener(this);
 		jp.add(btnSelectAll, c);
 		
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		btnDeselectAll = new JButton(strDeselectAll);
 		btnDeselectAll.addActionListener(this);
 		jp.add(btnDeselectAll, c);
 		gridy++;
 		
 		//(1) COMMON GENES
 		//checkbox
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.insets = new Insets(5,1,1,1);
 		c.fill = GridBagConstraints.NONE;
 		chkCommonGenes.addActionListener(this);
 		jp.add(chkCommonGenes, c);
 		
 		//label - linear
 		c.gridx = 2;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(3,3,3,3);
 		LblcgWeight = new JTextField(TxtLinear);
 		LblcgWeight.setEditable(false);
 		grpCommonGenes.add(LblcgWeight);
 		grpLinear.add(LblcgWeight);
 		jp.add(LblcgWeight, c);
 
 		//value - linear
 		c.gridx = 3;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		TxtcgWeight = new JTextField(strTxtcgWeight);
 		TxtcgWeight.setEditable(true);
 		grpCommonGenes.add(TxtcgWeight);
 		grpLinear.add(TxtcgWeight);
 		jp.add(TxtcgWeight, c);
 		
 		//label - scale hierarchy
 		c.gridx = 4;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(3,3,3,3);
 		LblcgScale = new JTextField(TxtScale);
 		LblcgScale.setEditable(false);
 		grpCommonGenes.add(LblcgScale);
 		grpScaleHierarchy.add(LblcgScale);
 		jp.add(LblcgScale, c);
 		
 		//value - scale hierarchy
 		c.gridx = 5;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		TxtcgScale = new JTextField(strTxtcgScale);
 		TxtcgScale.setEditable(true);
 		grpCommonGenes.add(TxtcgScale);
 		grpScaleHierarchy.add(TxtcgScale);
 		jp.add(TxtcgScale, c);
 		gridy++;
 		
 		c.ipady = 0;
 		
 		//Dice/Jaccard radio buttons
 		radDice = new JRadioButton(strDice);
 		radJaccard = new JRadioButton(strJaccard);
 		DiceOrJaccard = new ButtonGroup();
 		DiceOrJaccard.add(radDice);
 		DiceOrJaccard.add(radJaccard);
 		
 		//Dice option
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.NONE;
 		c.insets = new Insets(1,20,1,1);
 		radDice.setSelected(true);
 		grpCommonGenes.add(radDice);
 		jp.add(radDice, c);
 		
 		//Jaccard Option
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.NONE;
 		c.insets = new Insets(1,1,1,1);
 		radJaccard.setSelected(false);
 		grpCommonGenes.add(radJaccard);
 		jp.add(radJaccard, c);
 		gridy++;
 		
 		//check box
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.fill = GridBagConstraints.NONE;
 		c.insets = new Insets(1,20,1,1);
 		chkTreatDuplicatesAsUnique = new JCheckBox(strTreatDuplicatesAsUnique);
 		chkTreatDuplicatesAsUnique.setSelected(true);
 		grpCommonGenes.add(chkTreatDuplicatesAsUnique);
 		jp.add(chkTreatDuplicatesAsUnique, c);
 		gridy++;
 		
 		//(2) COMMON MOTIFS
 		c.ipady = 7;
 		//checkbox
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.insets = new Insets(5,1,1,1);
 		c.fill = GridBagConstraints.NONE;
 		chkCommonMotifs.addActionListener(this);
 		jp.add(chkCommonMotifs, c);
 		
 		//label - linear
 		c.gridx = 2;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(3,3,3,3);
 		LblcmWeight = new JTextField(TxtLinear);
 		LblcmWeight.setEditable(false);
 		grpCommonMotifs.add(LblcmWeight);
 		grpLinear.add(LblcmWeight);
 		jp.add(LblcmWeight, c);
 
 		//value - linear
 		c.gridx = 3;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		TxtcmWeight = new JTextField(strTxtcmWeight);
 		TxtcmWeight.setEditable(true);
 		grpCommonMotifs.add(TxtcmWeight);
 		grpLinear.add(TxtcmWeight);
 		jp.add(TxtcmWeight, c);
 		
 		//label - scale hierarchy
 		c.gridx = 4;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(3,3,3,3);
 		LblcmScale = new JTextField(TxtScale);
 		LblcmScale.setEditable(false);
 		grpCommonMotifs.add(LblcmScale);
 		grpScaleHierarchy.add(LblcmScale);
 		jp.add(LblcmScale, c);
 		
 		//value - scale hierarchy
 		c.gridx = 5;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		TxtcmScale = new JTextField(strTxtcmScale);
 		TxtcmScale.setEditable(true);
 		grpCommonMotifs.add(TxtcmScale);
 		grpScaleHierarchy.add(TxtcmScale);
 		jp.add(TxtcmScale, c);
 		
 		gridy++;
 		c.ipady = 0;
 		
 		//Label: Select motifs
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 2;
 		c.gridwidth = 1;
 		c.insets = new Insets(1,20,1,1);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		LblSelectMotifs = new JTextField(strSelectMotifs);
 		LblSelectMotifs.setEditable(false);
 		LblSelectMotifs.setBorder(null);
 		grpCommonMotifs.add(LblSelectMotifs);
 		jp.add(LblSelectMotifs, c);
 
 		//Drop-down check box menu
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(1,1,1,1);
 		c.fill = GridBagConstraints.NONE;
 		AvailableMotifsBox = new CheckCombo(this.f.getPanMotifOptions().getLoadedMotifs());
 		AvailableMotifsPanel = AvailableMotifsBox.getContent();
 		grpCommonMotifs.add(AvailableMotifsPanel);
 		grpCommonMotifs.add(AvailableMotifsBox);
 		jp.add(AvailableMotifsPanel, c);
 		gridy++;
 		
 		//Label: comparison scheme
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.insets = new Insets(1,20,1,1);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		LblComparisonScheme = new JTextField(strComparisonScheme);
 		LblComparisonScheme.setEditable(false);
 		LblComparisonScheme.setBorder(null);
 		grpCommonMotifs.add(LblComparisonScheme);
 		jp.add(LblComparisonScheme, c);
 		gridy++;
 
 		//Dice/Jaccard radio buttons
 		radDiceMotif = new JRadioButton(strDice);
 		radJaccardMotif = new JRadioButton(strJaccard);
 		DiceOrJaccardMotif = new ButtonGroup();
 		DiceOrJaccardMotif.add(radDiceMotif);
 		DiceOrJaccardMotif.add(radJaccardMotif);
 		
 		//Dice option
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.NONE;
 		c.insets = new Insets(1,20,1,1);
 		radDiceMotif.setSelected(true);
 		grpCommonMotifs.add(radDiceMotif);
 		jp.add(radDiceMotif, c);
 		
 		//Jaccard Option
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.NONE;
 		c.insets = new Insets(1,1,1,1);
 		radJaccardMotif.setSelected(false);
 		grpCommonMotifs.add(radJaccardMotif);
 		jp.add(radJaccardMotif, c);
 		gridy++;
 		
 		//check box
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.fill = GridBagConstraints.NONE;
 		c.insets = new Insets(1,20,1,1);
 		chkTreatDuplicatesAsUniqueMotif = new JCheckBox(strTreatDuplicatesAsUniqueMotif);
 		chkTreatDuplicatesAsUniqueMotif.setSelected(false);
 		grpCommonMotifs.add(chkTreatDuplicatesAsUniqueMotif);
 		jp.add(chkTreatDuplicatesAsUniqueMotif, c);
 		gridy++;
 
 		//(3) GENE ORDER
 		c.ipady = 7;
 		
 		//checkbox
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.insets = new Insets(5,1,1,1);
 		c.fill = GridBagConstraints.NONE;
 		chkGeneOrder.addActionListener(this);
 		jp.add(chkGeneOrder, c);
 		
 		//label - linear
 		c.gridx = 2;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(3,3,3,3);
 		LblgoWeight = new JTextField(TxtLinear);
 		LblgoWeight.setEditable(false);
 		grpGeneOrder.add(LblgoWeight);
 		grpLinear.add(LblgoWeight);
 		jp.add(LblgoWeight, c);
 
 		//value - linear
 		c.gridx = 3;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		TxtgoWeight = new JTextField(strTxtgoWeight);
 		TxtgoWeight.setEditable(true);
 		grpGeneOrder.add(TxtgoWeight);
 		grpLinear.add(TxtgoWeight);
 		jp.add(TxtgoWeight, c);
 		
 		//label - scale hierarchy
 		c.gridx = 4;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(3,3,3,3);
 		LblgoScale = new JTextField(TxtScale);
 		LblgoScale.setEditable(false);
 		grpGeneOrder.add(LblgoScale);
 		grpScaleHierarchy.add(LblgoScale);
 		jp.add(LblgoScale, c);
 		
 		//value - scale hierarchy
 		c.gridx = 5;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		TxtgoScale = new JTextField(strTxtgoScale);
 		TxtgoScale.setEditable(true);
 		grpGeneOrder.add(TxtgoScale);
 		grpScaleHierarchy.add(TxtgoScale);
 		jp.add(TxtgoScale, c);
 		gridy++;
 		
 		c.ipady = 0;
 //		
 		//Position from Head Option
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.insets = new Insets(1,20,1,1);
 		chkHeadPos = new JCheckBox(strHeadPos);
 		chkHeadPos.setSelected(true);
 		grpGeneOrder.add(chkHeadPos);
 		jp.add(chkHeadPos, c);
 		gridy++;
 		
 		//relative weights
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(1,40,1,1);
 		LblwtHead = new JTextField(strLblwtOrd);
 		LblwtHead.setBorder(null);
 		LblwtHead.setEditable(false);
 		grpGeneOrder.add(LblwtHead);
 		jp.add(LblwtHead, c);
 		
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(1,1,1,1);
 		c.fill = GridBagConstraints.NONE;
 		TxtwtHead = new JTextField(strTxtwtHead);
 		TxtwtHead.setEditable(true);
 		TxtwtHead.setColumns(StrColNum);
 		grpGeneOrder.add(TxtwtHead);
 		jp.add(TxtwtHead, c);
 		gridy++;
 		
 		//Conserved collinear gene pair option
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.insets = new Insets(1,20,1,1);
 		chkPairOrd = new JCheckBox(strPairOrd);
 		chkPairOrd.setSelected(true);
 		grpGeneOrder.add(chkPairOrd);
 		jp.add(chkPairOrd, c);
 		gridy++;
 		
 		//relative weights
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(1,40,1,1);
 		LblwtPair = new JTextField(strLblwtOrd);
 		LblwtPair.setBorder(null);
 		LblwtPair.setEditable(false);
 		grpGeneOrder.add(LblwtPair);
 		jp.add(LblwtPair, c);
 		
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(1,1,1,1);
 		c.fill = GridBagConstraints.NONE;
 		TxtwtPair = new JTextField(strTxtwtPair);
 		TxtwtPair.setEditable(true);
 		TxtwtPair.setColumns(StrColNum);
 		grpGeneOrder.add(TxtwtPair);
 		jp.add(TxtwtPair, c);
 		gridy++;
 
 		//(4) GENE GAPS
 		c.ipady = 7;
 		//checkbox
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.insets = new Insets(5,1,1,1);
 		c.fill = GridBagConstraints.NONE;
 		chkGeneGaps.addActionListener(this);
 		jp.add(chkGeneGaps, c);
 		
 		//label - linear
 		c.gridx = 2;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(3,3,3,3);
 		LblggWeight = new JTextField(TxtLinear);
 		LblggWeight.setEditable(false);
 		grpGeneGaps.add(LblggWeight);
 		grpLinear.add(LblggWeight);
 		jp.add(LblggWeight, c);
 
 		//value - linear
 		c.gridx = 3;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		TxtggWeight = new JTextField(strTxtggWeight);
 		TxtggWeight.setEditable(true);
 		grpGeneGaps.add(TxtggWeight);
 		grpLinear.add(TxtggWeight);
 		jp.add(TxtggWeight, c);
 		
 		//label - scale hierarchy
 		c.gridx = 4;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(3,3,3,3);
 		LblggScale = new JTextField(TxtScale);
 		LblggScale.setEditable(false);
 		grpGeneGaps.add(LblggScale);
 		grpScaleHierarchy.add(LblggScale);
 		jp.add(LblggScale, c);
 		
 		//value - scale hierarchy
 		c.gridx = 5;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		TxtggScale = new JTextField(strTxtggScale);
 		TxtggScale.setEditable(true);
 		grpGeneGaps.add(TxtggScale);
 		grpScaleHierarchy.add(TxtggScale);
 		jp.add(TxtggScale, c);
 		
 		gridy++;
 		c.ipady = 0;
 	
 		//btn group
 		radThreshold = new JRadioButton(strThreshold);
 		radInterpolation = new JRadioButton(strInterpolation);
 		ThresholdOrInterpolation = new ButtonGroup();
 		ThresholdOrInterpolation.add(radThreshold);
 		ThresholdOrInterpolation.add(radInterpolation);
 		
 		//radio buttons
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.NONE;
 		c.insets = new Insets(1,20,1,1);
 		radThreshold.setSelected(true);
 		grpGeneGaps.add(radThreshold);
 		jp.add(radThreshold, c);
 		
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.NONE;
 		c.insets = new Insets(1,1,1,1);
 		grpGeneGaps.add(radInterpolation);
 		jp.add(radInterpolation, c);
 		gridy++;
 		
 		//Enter points label
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets  = new Insets(1,20,1,1);
 		EnterPointsLabel = new JTextField(strEnterPointsLabel);
 		EnterPointsLabel.setEditable(false);
 		EnterPointsLabel.setBorder(null);
 		grpGeneGaps.add(EnterPointsLabel);
 		jp.add(EnterPointsLabel, c);
 		gridy++;
 		
 		//Actual enter points form
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(1,20,1,1);
 		EnterPointsTxt = new JTextArea("");
 		EnterPointsTxt.setEditable(true);
 		JScrollPane ptsscroll = new JScrollPane(EnterPointsTxt);
 		ptsscroll.setPreferredSize(new Dimension(100, 50));
 		grpGeneGaps.add(ptsscroll);
 		grpGeneGaps.add(EnterPointsTxt);
 		jp.add(ptsscroll, c);
 		gridy++;
 		
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.NONE;
 		c.insets = new Insets(1,20,1,1);
 		btnLoadFromFile = new JButton(strLoad);
 		btnLoadFromFile.addActionListener(this);
 		grpGeneGaps.add(btnLoadFromFile);
 		jp.add(btnLoadFromFile, c);
 		gridy++;
 		
 		//(5) STRANDEDNESS
 		c.ipady = 7;
 		//checkbox
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.insets = new Insets(5,1,1,1);
 		c.fill = GridBagConstraints.NONE;
 		chkStrandedness.addActionListener(this);
 		jp.add(chkStrandedness, c);
 		
 		//label - linear
 		c.gridx = 2;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(3,3,3,3);
 		LblssWeight = new JTextField(TxtLinear);
 		LblssWeight.setEditable(false);
 		grpStrandedness.add(LblssWeight);
 		grpLinear.add(LblssWeight);
 		jp.add(LblssWeight, c);
 
 		//value - linear
 		c.gridx = 3;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		TxtssWeight = new JTextField(strTxtssWeight);
 		TxtssWeight.setEditable(true);
 		grpStrandedness.add(TxtssWeight);
 		grpLinear.add(TxtssWeight);
 		jp.add(TxtssWeight, c);
 		
 		//label - scale hierarchy
 		c.gridx = 4;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(3,3,3,3);
 		LblssScale = new JTextField(TxtScale);
 		LblssScale.setEditable(false);
 		grpStrandedness.add(LblssScale);
 		grpScaleHierarchy.add(LblssScale);
 		jp.add(LblssScale, c);
 		
 		//value - scale hierarchy
 		c.gridx = 5;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		TxtssScale = new JTextField(strTxtssScale);
 		TxtssScale.setEditable(true);
 		grpStrandedness.add(TxtssScale);
 		grpScaleHierarchy.add(TxtssScale);
 		jp.add(TxtssScale, c);
 		
 		gridy++;
 		c.ipady = 0;
 		
 		//individual element strandedness option
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.insets = new Insets(1,20,1,1);
 		chkIndStrand = new JCheckBox(strIndStrand);
 		chkIndStrand.setSelected(true);
 		grpStrandedness.add(chkIndStrand);
 		jp.add(chkIndStrand, c);
 		gridy++;
 		
 		//relative weights
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(1,40,1,1);
 		LblwtInd = new JTextField(strLblwt);
 		LblwtInd.setBorder(null);
 		LblwtInd.setEditable(false);
 		grpStrandedness.add(LblwtInd);
 		jp.add(LblwtInd, c);
 		
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(1,1,1,1);
 		c.fill = GridBagConstraints.NONE;
 		TxtwtInd = new JTextField(strTxtwtInd);
 		TxtwtInd.setEditable(true);
 		TxtwtInd.setColumns(StrColNum);
 		grpStrandedness.add(TxtwtInd);
 		jp.add(TxtwtInd, c);
 		gridy++;
 		
 		//whole group strandedness option
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.insets = new Insets(1,20,1,1);
 		chkGrpStrand = new JCheckBox(strGrpStrand);
 		grpStrandedness.add(chkGrpStrand);
 		jp.add(chkGrpStrand, c);
 		gridy++;
 		
 		//relative weights
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(1,40,1,1);
 		LblwtGrp = new JTextField(strLblwt);
 		LblwtGrp.setBorder(null);
 		LblwtGrp.setEditable(false);
 		grpStrandedness.add(LblwtGrp);
 		jp.add(LblwtGrp, c);
 		
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(1,1,1,1);
 		c.fill = GridBagConstraints.NONE;
 		TxtwtGrp = new JTextField(strTxtwtGrp);
 		TxtwtGrp.setEditable(true);
 		TxtwtGrp.setColumns(StrColNum);
 		grpStrandedness.add(TxtwtGrp);
 		jp.add(TxtwtGrp, c);
 		gridy++;
 		
 		c.ipady = 7;
 		
 
 		
 		//Add button
 		c.gridx = 4;
 		c.gridy = gridy;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		btnAddDM = new JButton(strAddDM);
 		btnAddDM.addActionListener(this);
 		jp.add(btnAddDM, c);
 		gridy++;
 		
 		//add content to pane
 		jsp = new JScrollPane(jp);
 		this.add(jsp);
 
 		/*
 		 * REMOVE DM
 		 */
 		c.ipady = 7;
 		
 		//Remove DM Heading
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 6;
 		c.insets = new Insets(3,3,3,3);
 		RemoveDM = new JLabel(LblstrRemoveDM);
 		RemoveDM.setBackground(Color.GRAY);
 		RemoveDM.setOpaque(true);
 		jp.add(RemoveDM,c);
 		gridy++;
 		
 		// Dissimilarity Measure label
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = TextInsets;
 		LblDissimilarity = new JTextField();
 		LblDissimilarity.setText(strLblDissimilarity); // context set currently loaded
 		LblDissimilarity.setEditable(false);
 		LblDissimilarity.setBorder(null);
 		jp.add(LblDissimilarity, c);
 		
 		// drop-down menu for Dissimilarity Measures
 		c.ipady = 0;
 		c.gridx = 2;
 		c.gridy = gridy;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		MenuDM = new JComboBox<String>(CurrentDM);
 		MenuDM.addActionListener(this);
 		MenuDM.setEnabled(true);
 		jp.add(MenuDM, c);
 		
 		//remove button
 		c.gridx = 4;
 		c.gridy = gridy;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		btnRemoveDM = new JButton(strRemoveDM);
 		btnRemoveDM.addActionListener(this);
 		btnRemoveDM.setEnabled(true);
 		jp.add(btnRemoveDM, c);
 		gridy++;
 		gridy++;
 
 		//submit button
 		c.gridx = 4;
 		c.gridy = gridy;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(20,1,1,1);
 		btnOK = new JButton(strbtnOK);
 		btnOK.addActionListener(this);
 		jp.add(btnOK, c);
 		
 	}
 
 	// ----- Component State Switching -------------------//
 	public void SwitchStateComponents(LinkedList<Component> list, boolean SwitchState){
 		//adjust component states
 		if (SwitchState){
 			for (Component C : list){
 				//Components associated with amalgamation options
 				if (AmalgamationType.isSelected(radLinear.getModel())){
 					if (grpLinear.contains(C)){
 						C.setEnabled(true);
 					}
 				} else if (AmalgamationType.isSelected(radScaleHierarchy.getModel())){
 					if (grpScaleHierarchy.contains(C)){
 						C.setEnabled(true);
 					}
 				}
 				
 				//all others
 				if (!grpLinear.contains(C) && !grpScaleHierarchy.contains(C)){
 					C.setEnabled(true);
 				}
 			}
 		} else {
 			for (Component C : list){
 				C.setEnabled(false);
 			}
 		}
 	}
 
 	public void ActivateIfEnabled(LinkedList<Component> list){
 		for (Component c : list){
 			if (grpCommonGenes.contains(c)){
 				if (chkCommonGenes.isSelected()){
 					c.setEnabled(true);
 				}
 			} else if (grpCommonMotifs.contains(c)){
 				if (chkCommonMotifs.isSelected()){
 					c.setEnabled(true);
 				}
 			} else if (grpGeneOrder.contains(c)){
 				if (chkGeneOrder.isSelected()){
 					c.setEnabled(true);
 				}
 			} else if (grpGeneGaps.contains(c)){
 				if (chkGeneGaps.isSelected()){
 					c.setEnabled(true);
 				}
 			} else if (grpStrandedness.contains(c)){
 				if (chkStrandedness.isSelected()){
 					c.setEnabled(true);
 				}
 			}
 		}
 	}
 	
 	public void DeactivateAllComponents(){
 		for (Component C : this.grpCommonGenes){
 			C.setEnabled(false);
 		}
 		for (Component C: this.grpCommonMotifs){
 			C.setEnabled(false);
 		}
 		for (Component C: this.grpGeneGaps){
 			C.setEnabled(false);
 		}
 		for (Component C: this.grpGeneOrder){
 			C.setEnabled(false);
 		}
 		for (Component C : this.grpStrandedness){
 			C.setEnabled(false);
 		}
 	}
 	
 	//Determine gap mapping
 	public GapPointMapping ComputeGapMapping(){
 		
 		//Initialize output
 		LinkedList<GapPoint> InitialPoints = new LinkedList<GapPoint>();
 		LinkedList<GapPoint> FinalPoints = new LinkedList<GapPoint>();
 		
 		//Retrieve from text field
 		String PointsAsString = EnterPointsTxt.getText();
 		String[] PointValues = PointsAsString.split("\\s+");
 		for (int i = 0; i < PointValues.length-1; i=i+2){
 			GapPoint p = new GapPoint();
 			p.GapValue = Integer.parseInt(PointValues[i].trim());
 			p.Dissimilarity = Double.parseDouble(PointValues[i+1].trim());
 			InitialPoints.add(p);
 		}
 		
 		//Sort in increasing order
 		for (int i = 0; i <InitialPoints.size()-1; i++){
 			for (int j = 0; j < InitialPoints.size()-1; j++){
 				if (InitialPoints.get(j).GapValue > InitialPoints.get(j+1).GapValue){
 					GapPoint Temp = InitialPoints.get(j);
 					InitialPoints.set(j+1, InitialPoints.get(j));
 					InitialPoints.set(j, Temp);
 				}
 			}
 		}
 		
 		//threshold
 		if (this.radThreshold.isSelected()){
 			int GapSize = -10;
 			double Dissimilarity = 0;
 			
 			//move through sorted list
 			for (GapPoint gp : InitialPoints){
 				
 				//make points until the value is reached
 				while (GapSize < gp.GapValue){
 					GapPoint gp2 = new GapPoint();
 					gp2.Dissimilarity = Dissimilarity;
 					gp2.GapValue = GapSize;
 					GapSize++;
 					FinalPoints.add(gp2);
 				}
 				
 				//adjust values
 				GapSize = gp.GapValue;
 				if (gp.Dissimilarity > 1){
 					Dissimilarity = 1;
 				} else {
 					Dissimilarity = gp.Dissimilarity;
 				}
 
 			}
 			
 			//add last point
 			GapPoint gp = new GapPoint();
 			gp.GapValue = GapSize;
 			gp.Dissimilarity = Dissimilarity;
 			FinalPoints.add(gp);
 			
 		//linear interpolation
 		} else {
 			double y1;			//y-values = Dissimilarity
 			double y2;
 			double x1;			//x-values = Gap Size
 			double x2;
 			double m;			//Slope
 			double b;			//y-intercept
 			
 			double GapValue = 0;
 			double Dissimilarity = 0;
 
 			for (int i = 0; i < InitialPoints.size()-1; i++){
 				if (i == 0 && InitialPoints.get(i).GapValue > 0){ //extra interpolation	
 					
 					//assign points
 					x1 = 0; 
 					y1 = 0;
 					x2 = (double) InitialPoints.get(i).GapValue;
 					y2 = InitialPoints.get(i).Dissimilarity;
 					
 					//add zero point
 					GapPoint ZeroPoint = new GapPoint();
 					ZeroPoint.GapValue = (int) x1;
 					ZeroPoint.Dissimilarity = y1;
 					FinalPoints.add(ZeroPoint);
 					
 					//compute slope
 					m = (y2 - y1) / (x2 - x1);
 					
 					//compute intercept
 					b = y2 - m*x2;
 					
 					GapValue = x1;
 					while (GapValue < x2){
 						GapValue++;
 						Dissimilarity = m * GapValue + b;
 						GapPoint p = new GapPoint();
 						if (Dissimilarity <= 1){
 							p.Dissimilarity = Dissimilarity;
 						} else {
 							p.Dissimilarity = 1;
 						}
 						p.GapValue = (int) GapValue;
 						FinalPoints.add(p);
 					}
 					
 				} else {
 					
 					//assign points
 					x1 = (double) InitialPoints.get(i).GapValue;
 					y1 = InitialPoints.get(i).Dissimilarity;
 					x2 = (double) InitialPoints.get(i+1).GapValue;
 					y2 = InitialPoints.get(i+1).Dissimilarity;
 					
 					//add zero point (if appropriate)
 					if (FinalPoints.size() == 0){
 						GapPoint FirstPoint = InitialPoints.get(i);
 						if (FirstPoint.Dissimilarity > 1){
 							FirstPoint.Dissimilarity = 1;
 						}
 						FinalPoints.add(FirstPoint);
 					}
 					
 					//compute slope
 					m = (y2 - y1) / (x2 - x1);
 					
 					//compute intercept
 					b = y2 - m*x2;
 					
 					GapValue = x1;
 					while (GapValue < x2){
 						GapValue++;
 						Dissimilarity = m * GapValue + b;
 						GapPoint p = new GapPoint();
 						if (Dissimilarity <= 1){
 							p.Dissimilarity = Dissimilarity;
 						} else {
 							p.Dissimilarity = 1;
 						}
 						p.GapValue = (int) GapValue;
 						FinalPoints.add(p);
 					}
 				}
 			}			
 		}
 				
 		//create final output structure
 		GapPointMapping GPM = new GapPointMapping();
 		GPM.MaxGapLimit = FinalPoints.getLast().GapValue;
 		GPM.MaxDissimilarity = FinalPoints.getLast().Dissimilarity;
 		GPM.MinGaplimit = FinalPoints.getFirst().GapValue;
 		for (GapPoint p : FinalPoints){
 			GPM.Mapping.put(p.GapValue, p.Dissimilarity);
 		}
 		
 		//debugging
 //		for (Integer key : GPM.Mapping.keySet()){
 //			System.out.println("Gap= " + key + " Value= " + GPM.Mapping.get(key));
 //		}
 		
 		return GPM;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent evt) {
 		
 		//activating/deactivating states by chkbox, btn, radio button
 		if (evt.getSource().equals(chkCommonGenes)){
 			if (chkCommonGenes.isSelected()){					//common genes
 				SwitchStateComponents(grpCommonGenes, true);
 			} else {
 				SwitchStateComponents(grpCommonGenes, false);
 			}
 		} else if (evt.getSource().equals(chkCommonMotifs)){	//common motifs
 			if (chkCommonMotifs.isSelected()){
 				SwitchStateComponents(grpCommonMotifs, true);
 			} else {
 				SwitchStateComponents(grpCommonMotifs, false);
 			}
 		} else if (evt.getSource().equals(chkGeneOrder)){		//gene order
 			if (chkGeneOrder.isSelected()){
 				SwitchStateComponents(grpGeneOrder, true);
 			} else{
 				SwitchStateComponents(grpGeneOrder, false);
 			}
 		} else if (evt.getSource().equals(chkGeneGaps)){		//gene gaps
 			if (chkGeneGaps.isSelected()){
 				SwitchStateComponents(grpGeneGaps, true);
 			} else {
 				SwitchStateComponents(grpGeneGaps, false);
 			}
 		} else if (evt.getSource().equals(chkStrandedness)){	//strandedness
 			if (chkStrandedness.isSelected()){
 				SwitchStateComponents(grpStrandedness, true);
 			} else{
 				SwitchStateComponents(grpStrandedness, false);
 			}
 		} else if (evt.getSource().equals(radLinear)){
 				ActivateIfEnabled(grpLinear);
 				SwitchStateComponents(grpScaleHierarchy, false);
 		} else if (evt.getSource().equals(radScaleHierarchy)){
 				ActivateIfEnabled(grpScaleHierarchy);
 				SwitchStateComponents(grpLinear, false);
 		} else if (evt.getSource().equals(btnSelectAll)){
 				//check boxes
 				this.chkCommonGenes.setSelected(true);
 				this.chkGeneGaps.setSelected(true);
 				this.chkCommonMotifs.setSelected(true);
 				this.chkGeneOrder.setSelected(true);
 				this.chkStrandedness.setSelected(true);
 				
 				//components
 				SwitchStateComponents(grpCommonGenes, true);
 				SwitchStateComponents(grpCommonMotifs, true);
 				SwitchStateComponents(grpGeneOrder, true);
 				SwitchStateComponents(grpGeneGaps, true);
 				SwitchStateComponents(grpStrandedness, true);
 		} else if (evt.getSource().equals(btnDeselectAll)){
 				//check boxes
 				this.chkCommonGenes.setSelected(false);
 				this.chkGeneGaps.setSelected(false);
 				this.chkCommonMotifs.setSelected(false);
 				this.chkGeneOrder.setSelected(false);
 				this.chkStrandedness.setSelected(false);
 				
 				//components
 				SwitchStateComponents(grpCommonGenes, false);
 				SwitchStateComponents(grpCommonMotifs, false);
 				SwitchStateComponents(grpGeneOrder, false);
 				SwitchStateComponents(grpGeneGaps, false);
 				SwitchStateComponents(grpStrandedness, false);
 		}
 		
 		//load button
 		if (evt.getSource().equals(btnLoadFromFile)){
 			this.LoadGapDissimilarityMapping();
 		}
 		
 		//add a dissimilarity measure
 		if (evt.getSource().equals(btnAddDM)){
 			
 			//check names
 			if (!(f.getOS().getCustomDissimilarities().contains(DMName.getText()) ||
 					DMName.getText().equals("Common Genes - Dice") ||
 					DMName.getText().equals("Common Genes - Jaccard") ||
 					DMName.getText().equals("Moving Distances") ||
 					DMName.getText().equals("Total Length"))){
 
 				try {
 					
 					//General
 					String Name = DMName.getText();
 					String AmalgamationType;
 					if (radLinear.isSelected()){
 						AmalgamationType = "Linear";
 					} else {
 						AmalgamationType = "ScaleHierarchy";
 					}
 					LinkedList<String> Factors = new LinkedList<String>();
 					
 					//Factor 1: Presence/absence of common genes
 					String CGCompareType;
 					boolean CGDuplicatesUnique;
 					double CGWeight;
 					int CGImportance;
 					
 					if (this.chkCommonGenes.isSelected()){
 						Factors.add("CG");
 						if (radDice.isSelected()){
 							CGCompareType = "Dice";
 						} else {
 							CGCompareType = "Jaccard";
 						}
 						CGDuplicatesUnique = this.chkTreatDuplicatesAsUnique.isSelected();
 						CGWeight = Double.parseDouble(this.TxtcgWeight.getText());
 						CGImportance = Integer.parseInt(this.TxtcgScale.getText());
 					} else {
 						CGCompareType = null;
 						CGDuplicatesUnique = false;
 						CGWeight = 0;
 						CGImportance = 1;
 					}
 
 					//Factor 2: Presence/absence of common motifs
 					LinkedList<String> CMMotifNames;
 					String CMCompareType;
 					boolean CMDuplicatesUnique;
 					double CMWeight;
 					int CMImportance;
 					
 					if (this.chkCommonMotifs.isSelected()){
 						Factors.add("CM");
 						CMMotifNames = AvailableMotifsBox.getSelectedMotifs();
 						if (radDiceMotif.isSelected()){
 							CMCompareType = "Dice";
 						} else {
 							CMCompareType = "Jaccard";
 						}
 						CMDuplicatesUnique = this.chkTreatDuplicatesAsUniqueMotif.isSelected();
 						CMWeight = Double.parseDouble(this.TxtcmWeight.getText());
 						CMImportance = Integer.parseInt(this.TxtcmScale.getText());
 					} else {
 						CMMotifNames = null;
 						CMCompareType = null;
 						CMDuplicatesUnique = false;
 						CMWeight = 0;
 						CMImportance = -1;
 					}
 					
 					//Factor 3: Gene order
 					boolean HeadPos;
 					boolean PairOrd;
 					double RelWeightHeadPos;
 					double RelWeightPairOrd;
 					double GOWeight;
 					int GOImportance;
 
 					if (this.chkGeneOrder.isSelected()){
 						Factors.add("GO");
 						if (this.chkHeadPos.isSelected()){
 							HeadPos = true;
 						} else {
 							HeadPos = false;
 						}
 						if (this.chkPairOrd.isSelected()){
 							PairOrd = true;
 						} else {
 							PairOrd = false;
 						}
 						RelWeightHeadPos = Double.parseDouble(this.TxtwtHead.getText());
 						RelWeightPairOrd = Double.parseDouble(this.TxtwtPair.getText());
 						GOWeight = Double.parseDouble(this.TxtgoWeight.getText());
 						GOImportance = Integer.parseInt(this.TxtgoScale.getText());
 					} else{
 						HeadPos = false;
 						PairOrd = false;
 						RelWeightHeadPos = 0;
 						RelWeightPairOrd = 0;
 						GOWeight = 0;
 						GOImportance = -1;
 					}
 					
 					//Factor 4: Intragenic Gap Sizes
 					GapPointMapping GapSizeDissMapping;
 					double GGWeight;
 					int GGImportance;
 
 					if (this.chkGeneGaps.isSelected()){
 						Factors.add("GG");
 						GapSizeDissMapping = this.ComputeGapMapping();
 						GGWeight = Double.parseDouble(this.TxtggWeight.getText());
 						GGImportance = Integer.parseInt(this.TxtggScale.getText());
 					} else {
 						GapSizeDissMapping = null;
 						GGWeight = 0;
 						GGImportance = -1;
 					}
 					
 					//Factor 5: Changes in strandedness
 					boolean IndividualGenes;
 					boolean WholeGroup;
 					double RelWeightIndGenes;
 					double RelWeightWholeGroup;
 					double SSWeight;
 					int SSImportance;
 					
 					if (this.chkStrandedness.isSelected()){
 						Factors.add("SS");
 						if (this.chkIndStrand.isSelected()){
 							IndividualGenes = true;
 						} else {
 							IndividualGenes = false;
 						}
 						if (this.chkGrpStrand.isSelected()){
 							WholeGroup = true;
 						} else {
 							WholeGroup = false;
 						}
 						RelWeightIndGenes = Double.parseDouble(this.TxtwtInd.getText());
 						RelWeightWholeGroup = Double.parseDouble(this.TxtwtGrp.getText());
 						SSWeight = Double.parseDouble(this.TxtssWeight.getText());
 						SSImportance = Integer.parseInt(this.TxtssScale.getText());
 					} else {
 						IndividualGenes = false;
 						WholeGroup = false;
 						RelWeightIndGenes = 0;
 						RelWeightWholeGroup = 0;
 						SSWeight = 0;
 						SSImportance = -1;
 					}
 					
 					//compute a new dissimilarity.
 					CustomDissimilarity CD = new CustomDissimilarity(
 							Name,				//General
 							AmalgamationType,
 							Factors,			
 							CGCompareType,		//Factor 1: Common Genes
 							CGDuplicatesUnique,
 							CGWeight,
 							CGImportance,		
 							CMMotifNames,		//Factor 2: Common Motifs
 							CMCompareType,
 							CMDuplicatesUnique,
 							CMWeight,
 							CMImportance,		
 							HeadPos,			//Factor 3: Gene Order
 							PairOrd,
 							RelWeightHeadPos,
 							RelWeightPairOrd,
 							GOWeight,
 							GOImportance,
 							GapSizeDissMapping,	//Factor 4: Gene Gaps
 							GGWeight,
 							GGImportance,
 							IndividualGenes,	//Factor 5: Strandedness
 							WholeGroup,
 							RelWeightIndGenes,
 							RelWeightWholeGroup,
 							SSWeight,
 							SSImportance
 							);
 					
 					//Add to the list
 					f.getOS().addCustomDissimilarity(CD);
 					
 					//insert item into the menu
 					MenuDM.insertItemAt(Name, 0);
 					
 					
 				} catch (Exception ex){
 					JOptionPane.showMessageDialog(null, "One or more fields incorrectly formatted.",
 							"Format Error",JOptionPane.ERROR_MESSAGE);
 				}
 
 			} else {
 				JOptionPane.showMessageDialog(null, "There is another dissimilarity measure of that name. Please choose a different name.", "Name Exists", JOptionPane.ERROR_MESSAGE);
 			}
 
 		}
 		
 		//Remove button
 		if (evt.getSource().equals(btnRemoveDM)){
 			if (!(MenuDM.getSelectedItem().equals("Common Genes - Dice") ||
 					MenuDM.getSelectedItem().equals("Common Genes - Jaccard") ||
 					MenuDM.getSelectedItem().equals("Moving Distances") ||
 					MenuDM.getSelectedItem().equals("Total Length"))){
 				
 				//remove this item, if possible
 				MenuDM.removeItem(MenuDM.getSelectedItem());
 				f.getOS().getCustomDissimilarities().remove(MenuDM.getSelectedItem());
 
 			} else {
 				JOptionPane.showMessageDialog(null, "Unable to remove this dissimilarity type.",
 						"Unable to Remove",JOptionPane.ERROR_MESSAGE);
 			}
 				
 		}
 		
 		//Submit button
 		if (evt.getSource().equals(btnOK)){
 			this.f.getPanMenu().getCbDissimilarity().removeAllItems();
 			for (int i = 0; i < f.getOS().getCustomDissimilarities().size(); i++){
 				this.f.getPanMenu().getCbDissimilarity().addItem(f.getOS().getCustomDissimilarities().get(i).getName());
 			}
 			this.f.getPanMenu().getCbDissimilarity().addItem("Common Genes - Dice");
 			this.f.getPanMenu().getCbDissimilarity().addItem("Common Genes - Jaccard");
 			this.f.getPanMenu().getCbDissimilarity().addItem("Moving Distances");
 			this.f.getPanMenu().getCbDissimilarity().addItem("Total Length");
 			
 			//close window
 			this.dispose();
 		}
 	}
 
 	//load gap dissimilarity mapping
 	private void LoadGapDissimilarityMapping() {
 		
 		//initialize output
 		JFileChooser GetGapMapping = new JFileChooser();
 
 		GetGapMapping.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		GetGapMapping.setDialogTitle("Select a gap size - dissimilarity mapping file.");
 
 		if (this.ReferenceDirectory != null){
 			GetGapMapping.setCurrentDirectory(ReferenceDirectory);
 		} else {
 			GetGapMapping.setCurrentDirectory(f.getFileChooserSource());
 		}
 		GetGapMapping.showOpenDialog(GetGapMapping);
 		
 		//retrieve directory containing fimo files
 		File MappingFile = GetGapMapping.getSelectedFile();
 
 		//check if file could be received
 		if (MappingFile != null){
 
 			try {
 				//Import file reader
 				BufferedReader br = new BufferedReader(new FileReader(MappingFile));
 				
 				String Line = null;
 				String AllText = "";
 				while ((Line = br.readLine()) != null){
 					AllText = AllText + Line;
 				}
 				
 				//set text to field
 				this.EnterPointsTxt.setText(AllText);
 				
 			} catch (Exception e) {
 				JOptionPane.showMessageDialog(null, "File not formatted correctly.",
 						"File Format Error.", JOptionPane.ERROR_MESSAGE);
 			}
 
 		}
 	}
 }
 	
