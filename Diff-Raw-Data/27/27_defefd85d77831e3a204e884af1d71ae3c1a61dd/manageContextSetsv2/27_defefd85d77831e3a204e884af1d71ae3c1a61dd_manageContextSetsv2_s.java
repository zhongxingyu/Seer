 package moduls.frm.children;
 
 import genomeObjects.AnnotatedGenome;
 import genomeObjects.ContextSet;
 import genomeObjects.ContextSetDescription;
 import genomeObjects.GenomicElement;
 import genomeObjects.OrganismSet;
 import haloGUI.StartFrame;
 import inicial.Language;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.FileDialog;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.Map.Entry;
 
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.ButtonModel;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.SwingWorker;
 
 import moduls.frm.FrmPrincipalDesk;
 import moduls.frm.Panels.Jpan_btn_NEW;
 
 public class manageContextSetsv2 extends JDialog implements ActionListener, PropertyChangeListener{
 
 	//fields
 	//serial ID
 	private static final long serialVersionUID = 1L;
 
 	//parent information
 	private FrmPrincipalDesk fr;
 	private Jpan_btn_NEW jb;
 	
 	private String MostRecentlyAddedContextSet;
 	
 	//Loaded operon file + related booleans
 	private String strNoOperons = "Gene groupings could not be loaded or computed successfully.";
 	private String OperonStringToDisplay = strNoOperons;
 	private boolean OperonsbyComputation;
 	private boolean ReadyToAdd = false;
 	private boolean AcceptableName = true;
 	private ContextSetDescription ToAdd;
 	private boolean ComputedGrouping = false;
 	private boolean LoadedGrouping = false;
 	private String ComputedString;
 	private String LoadedString;
 	
 	//File loading related
 	private File[] GenomeGroupingFiles;
 	private File ReferenceDirectory;
 	private String GenomeWorkingSetFile_NoPath;
 	private boolean GenomicGroupingsAsSingleFile = false;
 	
 	//components
 	//panels
 	/*
 	 * 
 	 */
 	private JPanel jp;
 	private JScrollPane EnclosingPane;
 	
 	//dummy components (for spacing)
 	private JLabel d1, d2, d3, d4, d5;
 	
 	//general use
 	private String strLoad = "Load";
 	private JProgressBar progressBar;
 	
 	//radio button components + names + button group
 	private ButtonGroup CSType;
 	private LinkedHashMap<ButtonModel, LinkedList<Component>> RadioButtonComponents
 		= new LinkedHashMap<ButtonModel, LinkedList<Component>>();
 
 	//CSType (1) - Intergenic Distance
 	private LinkedList<Component> CSIntergenicDist_group;
 	private JRadioButton CSIntergenicDist;
 	private String strCSIntergenicDist = "Group genes based on intergenic distance";
 	private JTextField intergenicTolerance;
 	private JButton computeIntergenic; 
 	private String strcomputeIntergenic = "Compute";
 	private JCheckBox cbStrandOption;
 	private String strcbStrandOption = "Genes must be on same strand";
 	
 	//CSType (2) - CSRange
 	private LinkedList<Component> CSRange_group;
 	private JRadioButton CSRange;
 	private String strCSRange = "Group genes based on nucleotide range";
 	private JTextField ntBefore;
 	private JTextField ntBeforeLabel;
 	private String strntBeforeLabel = "nt Before:";
 	private JTextField ntAfter;
 	private JTextField ntAfterLabel;
 	private String strntAfterLabel = "nt After:";
 	
 	//CSType (3) - CSGenesAround
 	private LinkedList<Component> CSGenesAround_group;
 	private JRadioButton CSGenesAround;
 	private String strCSGenesAround = "Group genes based on number of nearby genes";
 	private JTextField GenesBefore;
 	private JTextField GenesBeforeLabel;
 	private String strGenesBeforeLabel = "Genes Before:";
 	private JTextField GenesAfter;
 	private JTextField GenesAfterLabel;
 	private String strGenesAfterLabel = "Genes After:";
 	private JCheckBox chkAttemptToStrandCorrect;
 	private String strAttempt = "Attempt to use relative before and after";
 	
 	//CSType (4) - CSGenesBetween
 	private LinkedList<Component> CSGenesBetween_group;
 	private JRadioButton CSGenesBetween;
 	private String strCSGenesBetween = "Group all genes between two queries together";
 	private JCheckBox cbLimitDistance;
 	private String strchkLimitDistance = "Max distance between query genes:";
 	private JTextField TxtDistanceLimit;
 	private JTextField LblDistanceLimit;
 	private String strLblDistanceLimit = "nt Span";
 	
 	//CSType (5) - CSMultipleQuery
 	private LinkedList<Component> CSMultipleQuery_group;
 	private JRadioButton CSMultipleQuery;
 	private String strCSMultipleQuery = "Group multiple independent queries together";
 	
 	//CSType (6)  - CSLoaded
 	private LinkedList<Component> CSLoaded_group;
 	private JRadioButton CSLoaded;
 	private String strCSLoaded = "Load gene groupings from file";
 	private JButton btnLoadCS;
 	
 	//CSType (7)  - Cassette
 	private LinkedList<Component> CSCassette_group;
 	private JRadioButton CSCassette;
 	private String strCSCassette = "Construct a cassette based on an existing context set";
 	private JComboBox<String> contextSetMenuforCassette;
 	private JTextField contextSetHeaderforCassette;
 	private JCheckBox cbOnlyNearby;
 	private JTextField TxtNearby, LblNearby;
 	private String strcbOnlyNearby = "Only add nearby features";
 	private String strTxtNearby = "1000";
 	private String strLblNearby = "nt Distance to closest original feature";
 	
 	//CSType (8) CS
 	private LinkedList<Component> CSCombination_group;
 	private JRadioButton CSCombination;
 	private String strCSCombination = "Create a new context set by combining existing context sets";
 	private JButton btnLaunchCombiner;
 	private String strLaunchCombiner = "Launch Context Set Combiner Tool";
 	
 	// MODIFIERS
 	
 	//Single organism amalgamation option
 	private JCheckBox cbAmalg, cbRetainElements;
 	private String strcbAmalg = "Single Organism Amalgamation";
 	private String strcbRetainElements = "Retain Elements Common to a Fraction of Genomic Groupings: ";
 	private String strInitialTxtFraction = "0.75";
 	private JTextField TxtFractionRetain;
 	
 	//Add panel
 	private JButton btnAddCS;
 	private JLabel Add;
 	private String strAddCS = "Add";
 	private JTextField CSName, CSNameLabel;
 	private String strCSNameLabel = "Enter Name: ";
 	private JTextField contextSetHeader;
 	private JTextField LoadedFileName;
 	
 	//Remove CS
 	private JButton btnRemoveCS;
 	private JLabel Remove;
 	private String strRemoveCS = "Remove";
 	private JComboBox<String> contextSetMenu;
 	
 	//okay - close panel
 	private JButton btnOK;
 	private String strbtnOK = "OK";
 	
 									//width, height
 	private Dimension D = new Dimension(800, 600);
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * 
 	 * 
 	 * 
 	 */
 	
 	//constructor
 	public manageContextSetsv2(FrmPrincipalDesk f, Jpan_btn_NEW jbn){
 		super();
 		
 		//imported information
 		this.fr = f;
 		this.jb = jbn;
 		this.ReferenceDirectory = fr.getFileChooserSource();
 		
 		//panel + framecomponents
 		this.getPanel();
 		this.getFrame();
 		
 		//Data motifications
 		this.DisableComponents();
 		btnAddCS.setEnabled(false);
 		
 		//final step - turn on visibility
 		this.setVisible(true);
 	}
 
 	//SwingWorker
 	class btnLoadCS extends SwingWorker<Void, Void>{
 		
 		//constructor
 		public btnLoadCS(String fileName){
 			if (fileName == null){
 				OperonsbyComputation = true;
 				ComputedGrouping = true;
 			} else {
 				OperonsbyComputation = false;
 				OperonStringToDisplay = fileName;
 				LoadedGrouping = true;
 			}
 		}
 		
 		//do in background method
 		@Override
 		protected Void doInBackground() throws Exception {
 
 			btnAddCS.setEnabled(false);
 			
 			if (OperonsbyComputation == true){
 				
 			int TotalOrganisms = fr.getOS().getSpeciesNames().size();	
 			progressBar.setVisible(true);
 			LoadedFileName.setVisible(false);
 			progressBar.setValue(0);
 			int OrganismsCompleted = 0;
 			int ComputeProgress = 0;
 			progressBar.setStringPainted(true);
 
 			for (Entry<String, AnnotatedGenome> entry: fr.getOS().getSpecies().entrySet()){
 				
 				entry.getValue().ComputeContextSet(CSName.getText(), Integer.parseInt(intergenicTolerance.getText()), cbStrandOption.isSelected());
 				OrganismsCompleted++;
 				ComputeProgress = (int) Math.round(100*((double)OrganismsCompleted/(double)TotalOrganisms));
 				this.setProgress(ComputeProgress);
 				
 			}
 			
 			//update the context set candidate for loading
 			ToAdd = new ContextSetDescription();
 			ToAdd.setName(CSName.getText());
 			ToAdd.setPreprocessed(true);
 			ToAdd.setType("IntergenicDist");
 			
 			//update progress bar
 			progressBar.setValue(100);
 			
 			//update operon file name, and make visible
 			if (cbStrandOption.isSelected()){
 				OperonStringToDisplay = "Gene groupings computed with an intergenic distance of " + intergenicTolerance.getText() + ", same strand only";
 			} else {
 				OperonStringToDisplay = "Gene groupings computed with an intergenic distance of " + intergenicTolerance.getText() + ", either strand";
 			}
 			
 			ComputedString = OperonStringToDisplay;
 			
 			LoadedFileName.setText(OperonStringToDisplay);
 			progressBar.setVisible(false);
 			LoadedFileName.setVisible(true);
 			
 			ReadyToAdd = true;
 			
 			} else {
 				
 				progressBar.setVisible(true);
 				LoadedFileName.setVisible(false);
 				progressBar.setValue(0);
 				int OrganismsCompleted = 0;
 				int OperonCounter = 0;
 				int operonLoadProgress = 0;
 				progressBar.setStringPainted(true);
 
 				try {
 					
 					//information regarding context set upload.
 					ToAdd = new ContextSetDescription();
 					ToAdd.setName(CSName.getText());
 					ToAdd.setPreprocessed(true);
 					ToAdd.setType("Loaded");
 					
 					//import buffered reader
 					BufferedReader br_count = new BufferedReader(new FileReader(OperonStringToDisplay));
 					int TotalLines = 0;
 					boolean FirstLine = true;
 					String CountLine = null;
 					//count lines
 					while ((CountLine = br_count.readLine()) != null){
 						TotalLines++;
 						if (FirstLine){
 							String[] L = CountLine.split("\t");
 							if (L.length == 5){
 								GenomicGroupingsAsSingleFile = true;
 							} else {
 								GenomicGroupingsAsSingleFile = false;
 							}
 							FirstLine = false;
 						}
 						
 					}
 					br_count.close();
 					
 					//single-file import
 					if (GenomicGroupingsAsSingleFile) {
 
 						//context set
 						ImportFromSingleFile(ToAdd.getName(),TotalLines);
 	
 					//from a set of files	
 					} else {
 						
 						// determine number of total organisms
 						int TotalOrganisms = 0;
 						for (File f : GenomeGroupingFiles) {
 							if (f.getName().contains(".txt")) {	//only take plain text files
 								TotalOrganisms++;
 							}
 						}
 						
 						int LineCounter = 0;
 						// retrieve all files
 						for (File f : GenomeGroupingFiles) {
 							if (f.getName().contains(".txt")) {
 								LineCounter++;
 								
 								String[] SpeciesName = f.getName().split(".txt");
 								String TheName = SpeciesName[0];
 								
 								//retrieve species
 								AnnotatedGenome AG = fr.getOS().getSpecies().get(TheName);
 								
 								//import from file
 								AG.ImportContextSet(CSName.getText(), f.getAbsolutePath());
 								
 								//break out of the loop.
 								if (!AG.isTryToComputeOperons()){
 									setProgress(0);
 									break;
 								}
 								
 								// update progress bar
 								operonLoadProgress= (int) Math.round(100*((double)LineCounter/(double)TotalOrganisms));
 								setProgress(operonLoadProgress);
 
 							}
 						}
 						
 						//check
 						int NewCSCounter = 0;
 						for (AnnotatedGenome AG : fr.getOS().getSpecies().values()){
 							boolean NewCS = true;
 							for (ContextSet CS : AG.getGroupings()){
 								if (CS.getName().equals(CSName.getText())){
 									NewCS = false;
 									break;
 								}
 							}
 
 							//create a new CS
 							if (NewCS){
 								NewCSCounter++;
 								ContextSet CS = new ContextSet();
 								CS.setName(CSName.getText());
 								CS.setType("Loaded");
 								CS.setPreProcessed(true);
 								CS.setContextMapping(new HashMap<Integer, LinkedList<GenomicElement>>());
 								AG.getGroupings().add(CS);
 							}
 						}
 							if (NewCSCounter == fr.getOS().getSpeciesNames().size()){
 								progressBar.setVisible(false);
 								progressBar.setValue(0);
 								LoadedFileName.setText("No genomic groupings were discovered.");
 								LoadedFileName.setVisible(true);
 								throw new IOException();
 							}
 
 					}
 					
 					progressBar.setVisible(false);
 					LoadedFileName.setVisible(true);
 					LoadedFileName.setText("File Loaded: " + OperonStringToDisplay);
 					LoadedString = OperonStringToDisplay;
 					
 					ReadyToAdd = true;
 				
 				} catch (IOException ex){
 					//ex.printStackTrace();
 					JOptionPane.showMessageDialog(null, "No genomic groupings could be mapped to the genomic working set.",
 							"No Groupings Discovered",JOptionPane.ERROR_MESSAGE);
 					
 				} catch(Exception ex) {
 					
 					//ex.printStackTrace();
 					
 					progressBar.setStringPainted(false);
 					progressBar.setVisible(false);
 					LoadedFileName.setText("Unable to load files.");
 					LoadedFileName.setVisible(true);
 					JOptionPane.showMessageDialog(null, "One or more of the files could not be loaded or was improperly formatted.",
 							"Improper File Format",JOptionPane.ERROR_MESSAGE);
 
 					LoadedFileName.setText(strNoOperons);
 					LoadedString = strNoOperons;
 
 				}
 			}
 			
 			return null;
 		}
 		
 		//Method to import
 		protected Void ImportFromSingleFile(String CSName, int TotalLines){
 			
 			int operonLoadProgress;
 			int LineCounter = 0;
 			
 			//stream in data from file
 			try {
 				
 				BufferedReader br = new BufferedReader(new FileReader(OperonStringToDisplay));
 				String Line = null;
 				while ((Line = br.readLine()) != null){
 					
 					//increment counter
 					LineCounter++;
 					
 					//split
 					String L[] = Line.split("\t");
 					
 					//recover species
 					AnnotatedGenome AG = fr.getOS().getSpecies().get(L[0]);
 					
 					//modify, when appropriate
 					if (AG != null){
 						
 						//try every line - skip lines that don't map anywhere
 						try {
 							
 							//parse other fields 
 							String ContigName = L[1];
 							int Start = Integer.parseInt(L[2]);
 							int Stop = Integer.parseInt(L[3]);
 							int Key = Integer.parseInt(L[4]);
 							
 							//adjust context set
 							AG.AdjustContextSet(CSName, ContigName, Start, Stop, Key);
 							
 							operonLoadProgress= (int) Math.round(100*((double)LineCounter/(double)TotalLines));
 							setProgress(operonLoadProgress);
 							
 						} catch (Exception ex) {
 							
 						}
 					}
 					
 				}
 				br.close();
 
 			} catch (Exception ex){
 				//System.out.println("dies in import");
				//ex.printStackTrace();
 			}
 			return null;
 		}
 		
 		//after all completed, load up this set
 		public void done(){
 			btnAddCS.setEnabled(true);
 		}
 		
 	}
 	
 	//add panel components
 	private void getPanel(){
 
 		//initial GridBagLayout parameters
 		jp = new JPanel();
 		jp.setLayout(new GridBagLayout());
 		final GridBagConstraints c = new GridBagConstraints();
 		int gridy = 0;
 		//initial GridBagLayout parameters
 		c.anchor = GridBagConstraints.FIRST_LINE_START;
 		c.weightx = 1;
 		
 		//hidden grid!
 		//dummy labels, to artificially normalize column widths
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		d1 = new JLabel(" ");
 		d1.setBackground(Color.LIGHT_GRAY);
 		d1.setOpaque(false);
 		jp.add(d1, c);
 		
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		d2 = new JLabel(" ");
 		d2.setBackground(Color.LIGHT_GRAY);
 		d2.setOpaque(false);
 		jp.add(d2, c);
 		
 		c.gridx = 2;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		d3 = new JLabel(" ");
 		d3.setBackground(Color.LIGHT_GRAY);
 		d3.setOpaque(false);
 		jp.add(d3, c);
 		
 		c.gridx = 3;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		d4 = new JLabel(" ");
 		d4.setBackground(Color.LIGHT_GRAY);
 		d4.setOpaque(false);
 		jp.add(d4, c);
 		
 		c.gridx = 4;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		d5 = new JLabel(" ");
 		d5.setBackground(Color.LIGHT_GRAY);
 		d5.setOpaque(false);
 		jp.add(d5, c);
 
 		//create radio buttons
 		//search type button group definition
 		CSIntergenicDist = new JRadioButton(strCSIntergenicDist);
 		CSRange = new JRadioButton(strCSRange);
 		CSGenesAround = new JRadioButton(strCSGenesAround);
 		CSGenesBetween = new JRadioButton(strCSGenesBetween);
 		CSMultipleQuery = new JRadioButton(strCSMultipleQuery);
 		CSLoaded = new JRadioButton(strCSLoaded);
 		CSCassette = new JRadioButton(strCSCassette);
 		CSCombination = new JRadioButton(strCSCombination);
 
 		//define button group
 		CSType = new ButtonGroup(); CSType.add(CSIntergenicDist); CSType.add(CSRange); CSType.add(CSGenesAround);
 		CSType.add(CSGenesBetween); CSType.add(CSMultipleQuery); CSType.add(CSLoaded); CSType.add(CSCassette);
 		CSType.add(CSCombination);
 		
 		//CSType.setSelected(CSIntergenicDist.getModel(), true);
 		/*
 		 * ADD COMPONENTS TO PANEL!!!
 		 */
 		
 		/*
 		 * ADD CS
 		 */
 		
 		//Add CS Heading
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.insets = new Insets(3,3,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 5;
 		Add = new JLabel(" ADD A CONTEXT SET");
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
 		c.insets = new Insets(3,3,3,0);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		CSNameLabel = new JTextField(strCSNameLabel);
 		CSNameLabel.setEditable(false);
 		jp.add(CSNameLabel,c);
 		
 		//the name itself text field
 		c.ipady = 7;
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridwidth = 4;
 		c.gridheight = 1;
 		c.insets = new Insets(3,2,3,3);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		CSName = new JTextField("");
 		CSName.setEditable(true);
 		CSName.addActionListener(this);
 		//CSName.setColumns(200);
 		jp.add(CSName, c);
 		gridy++;
 
 		//(1) CSINTERGENICDIST
 		
 		CSIntergenicDist_group = new LinkedList<Component>();
 		
 		//add radio button
 		c.gridx = 0;
 		c.ipady = 0;
 		c.insets = new Insets(5,1,1,1);
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 5;
 		jp.add(CSIntergenicDist, c);
 		CSIntergenicDist.addActionListener(this);
 		gridy++;
 		
 		//intergenic distance text field
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.ipady = 7;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(1,20,1,1);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		intergenicTolerance = new JTextField("20");
 		intergenicTolerance.setEditable(true);
 		intergenicTolerance.addActionListener(this);
 		jp.add(intergenicTolerance, c);
 		CSIntergenicDist_group.add(intergenicTolerance);
 		
 		//Compute gene groupings button
 		c.ipady = 0;
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.ipady = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(1,1,1,1);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		computeIntergenic = new JButton(strcomputeIntergenic);
 		computeIntergenic.addActionListener(this);
 		//jp.add(computeIntergenic, c);
 		CSIntergenicDist_group.add(computeIntergenic);
 		
 		//check box
 		c.ipady = 0;
 		c.gridy = gridy;
 		//c.gridx = 2
 		//c.gridwidth = 3;
 		c.gridx = 1;
 		c.gridwidth = 4;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		cbStrandOption = new JCheckBox(strcbStrandOption);
 		cbStrandOption.setSelected(true);
 		jp.add(cbStrandOption, c);
 		CSIntergenicDist_group.add(cbStrandOption);
 		gridy++;		
 		
 		//add this mapping to hash map.
 		RadioButtonComponents.put(CSIntergenicDist.getModel(), CSIntergenicDist_group);
 		
 		//(2) CSRANGE
 		
 		//grouping
 		CSRange_group = new LinkedList<Component>();
 		
 		//add radio button
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.insets = new Insets(10,1,1,1);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 5;
 		jp.add(CSRange, c);
 		CSRange.addActionListener(this);
 		gridy++;
 		
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.insets = new Insets(1,20,1,1);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		ntBeforeLabel = new JTextField(strntBeforeLabel);
 		ntBeforeLabel.setEditable(false);
 		CSRange_group.add(ntBeforeLabel);
 		jp.add(ntBeforeLabel, c);
 		
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.insets = new Insets(1,1,1,1);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		ntBefore = new JTextField("1000");
 		ntBefore.setEditable(true);
 		CSRange_group.add(ntBefore);
 		jp.add(ntBefore, c);
 		
 		c.gridx = 2;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		ntAfterLabel = new JTextField(strntAfterLabel);
 		ntAfterLabel.setEditable(false);
 		CSRange_group.add(ntAfterLabel);
 		jp.add(ntAfterLabel, c);
 		
 		c.gridx = 3;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		ntAfter = new JTextField("1000");
 		ntAfter.setEditable(true);
 		ntAfter.addActionListener(this);
 		CSRange_group.add(ntAfter);
 		jp.add(ntAfter, c);
 		gridy++;
 		
 		//add this mapping to hash map.
 		RadioButtonComponents.put(CSRange.getModel(), CSRange_group);
 		
 		//(3) CSGENESAROUND
 		
 		//grouping
 		CSGenesAround_group = new LinkedList<Component>();
 		
 		//add radio button
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(10,1,1,1);
 		c.gridwidth = 5;
 		jp.add(CSGenesAround, c);
 		CSGenesAround.addActionListener(this);
 		gridy++;
 		
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.insets = new Insets(1,20,1,1);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 2;
 		chkAttemptToStrandCorrect = new JCheckBox(strAttempt);
 		chkAttemptToStrandCorrect.setSelected(true);
 		CSGenesAround_group.add(chkAttemptToStrandCorrect);
 		jp.add(chkAttemptToStrandCorrect, c);
 		gridy++;
 		
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.insets = new Insets(1,20,1,1);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		GenesBeforeLabel = new JTextField(strGenesBeforeLabel);
 		GenesBeforeLabel.setEditable(false);
 		CSGenesAround_group.add(GenesBeforeLabel);
 		jp.add(GenesBeforeLabel, c);
 		
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.insets = new Insets(1,1,1,1);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		GenesBefore = new JTextField("2");
 		GenesBefore.setEditable(true);
 		CSGenesAround_group.add(GenesBefore);
 		jp.add(GenesBefore, c);
 		
 		c.gridx = 2;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		GenesAfterLabel = new JTextField(strGenesAfterLabel);
 		GenesAfterLabel.setEditable(false);
 		CSGenesAround_group.add(GenesAfterLabel);
 		jp.add(GenesAfterLabel, c);
 		
 		c.gridx = 3;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		GenesAfter = new JTextField("2");
 		GenesAfter.addActionListener(this);
 		GenesAfter.setEditable(true);
 		CSGenesAround_group.add(GenesAfter);
 		jp.add(GenesAfter, c);
 		gridy++;
 		
 		//add this mapping to hash map.
 		RadioButtonComponents.put(CSGenesAround.getModel(), CSGenesAround_group);
 		
 		//(4) CSGENESBETWEEN
 		
 		//grouping
 		CSGenesBetween_group = new LinkedList<Component>();
 		
 		//add radio button
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 5;
 		c.insets = new Insets(10,1,1,1);
 		jp.add(CSGenesBetween, c);
 		CSGenesBetween.addActionListener(this);
 		gridy++;
 		
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		c.insets = new Insets(1,20,1,1);
 		cbLimitDistance = new JCheckBox(strchkLimitDistance);
 		cbLimitDistance.setSelected(true);
 		cbLimitDistance.addActionListener(this);
 		jp.add(cbLimitDistance, c);
 		CSGenesBetween_group.add(cbLimitDistance);
 		
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		c.insets = new Insets(1,1,1,1);
 		TxtDistanceLimit = new JTextField("10000");
 		TxtDistanceLimit.setEditable(true);
 		TxtDistanceLimit.setHorizontalAlignment(JTextField.LEFT);
 		jp.add(TxtDistanceLimit, c);
 		CSGenesBetween_group.add(TxtDistanceLimit);
 		
 		c.gridx = 2;
 		c.gridy = gridy;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		c.insets = new Insets(1,1,1,1);
 		LblDistanceLimit = new JTextField(strLblDistanceLimit);
 		LblDistanceLimit.setEditable(false);
 		jp.add(LblDistanceLimit, c);
 		CSGenesBetween_group.add(LblDistanceLimit);
 		
 		gridy++;
 		
 		//private JTextField DistanceLimit;
 		
 		//add this mapping to hash map.
 		RadioButtonComponents.put(CSGenesBetween.getModel(), CSGenesBetween_group);
 		
 		//(5) CSMULTIPLEQUERY
 		
 		//grouping
 		CSMultipleQuery_group = new LinkedList<Component>();
 		
 		//add radio button
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 5;
 		c.insets = new Insets(10,1,1,1);
 		jp.add(CSMultipleQuery, c);
 		CSMultipleQuery.addActionListener(this);
 		gridy++;
 		
 		//add this mapping to hash map.
 		RadioButtonComponents.put(CSMultipleQuery.getModel(), CSMultipleQuery_group);
 		
 		//(6) CSLOADED
 		
 		//grouping
 		CSLoaded_group = new LinkedList<Component>();
 		
 		//add radio button
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 5;
 		c.insets = new Insets(10,1,1,1);
 		jp.add(CSLoaded, c);
 		CSLoaded.addActionListener(this);
 		gridy++;
 		
 		// load gene groupings from file
 		c.ipadx = 0;
 		c.insets = new Insets(1,20,1,1);
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridy = gridy;
 		btnLoadCS = new JButton(strLoad);
 		btnLoadCS.addActionListener(this);
 		jp.add(btnLoadCS, c);
 		CSLoaded_group.add(btnLoadCS);
 		gridy++;
 		
 		//add this mapping to hash map.
 		RadioButtonComponents.put(CSLoaded.getModel(), CSLoaded_group);
 		
 		//(7) CSCASSETTE
 		
 		//grouping
 		CSCassette_group = new LinkedList<Component>();
 		
 		//add radio button
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 5;
 		c.insets = new Insets(10,1,1,1);
 		jp.add(CSCassette, c);
 		CSCassette.addActionListener(this);
 		gridy++;
 		
 		// Context Set Text label
 		c.ipady = 5;
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(1,20,1,1);
 		//c.fill = GridBagConstraints.NONE;
 		contextSetHeaderforCassette = new JTextField();
 		contextSetHeaderforCassette.setText("Context Set:"); // context set currently loaded
 		contextSetHeaderforCassette.addActionListener(this);
 		contextSetHeaderforCassette.setEditable(false);
 		CSCassette_group.add(contextSetHeaderforCassette);
 		jp.add(contextSetHeaderforCassette, c);
 		
 		// drop down menu
 		c.ipady = 0;
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(1,1,1,1);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		String[] CurrentContextLists = this.convertContextSets(fr.getOS().getCSDs());
 		contextSetMenuforCassette = new JComboBox<String>(CurrentContextLists);
 		contextSetMenuforCassette.addActionListener(this);
 		contextSetMenuforCassette.setEnabled(true);
 		jp.add(contextSetMenuforCassette, c);
 		CSCassette_group.add(contextSetMenuforCassette);
 		
 		//row 2 - cassette options
 		gridy++;
 		
 		c.ipady = 0;
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(1,20,1,1);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		cbOnlyNearby = new JCheckBox(strcbOnlyNearby);
 		cbOnlyNearby.setSelected(true);
 		cbOnlyNearby.setEnabled(false);
 		cbOnlyNearby.addActionListener(this);
 		jp.add(cbOnlyNearby, c);
 		CSCassette_group.add(cbOnlyNearby);
 		
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(1,1,1,1);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		TxtNearby = new JTextField(strTxtNearby);
 		TxtNearby.setEditable(true);
 		jp.add(TxtNearby, c);
 		CSCassette_group.add(TxtNearby);
 		
 		c.gridx = 2;
 		c.gridy = gridy;
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.insets = new Insets(1,1,1,1);
 		LblNearby = new JTextField(strLblNearby);
 		LblNearby.setEditable(false);
 		jp.add(LblNearby, c);
 		CSCassette_group.add(LblNearby);
 	
 		
 		//add this mapping to hash map.
 		RadioButtonComponents.put(CSCassette.getModel(), CSCassette_group);
 
 		//(8) CSCOMBINATION
 		
 		//grouping
 		CSCombination_group = new LinkedList<Component>();
 		
 		//add radio button
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 5;
 		c.insets = new Insets(10,1,1,1);
 		//jp.add(CSCombination, c);
 		CSCombination.addActionListener(this);
 		gridy++;
 
 		//launch button tool
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.insets = new Insets(1,20,1,1);
 		btnLaunchCombiner = new JButton(strLaunchCombiner);
 		btnLaunchCombiner.addActionListener(this);
 		//jp.add(btnLaunchCombiner, c);
 		CSCombination_group.add(btnLaunchCombiner);
 		gridy++;
 
 		//add this mapping to hash map.
 		RadioButtonComponents.put(CSCombination.getModel(), CSCombination_group);
 
 		//FILTERS
 		
 		// Single organism amalgamation option
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 5;
 		c.insets = new Insets(10,1,1,1);
 		cbAmalg = new JCheckBox(strcbAmalg);
 		cbAmalg.setSelected(false);
 		jp.add(cbAmalg, c);
 		gridy++;
 		
 		//check box
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		c.insets = new Insets(10,1,1,1);
 		cbRetainElements = new JCheckBox(strcbRetainElements);
 		cbRetainElements.setSelected(false);
 		cbRetainElements.addActionListener(this);
 		jp.add(cbRetainElements, c);
 		
 		//fraction
 		c.gridx = 2;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		TxtFractionRetain = new JTextField(strInitialTxtFraction);
 		TxtFractionRetain.setEditable(true);
 		TxtFractionRetain.setEnabled(false);
 		jp.add(TxtFractionRetain, c);
 		gridy++;
 		
 		// ADD CONTEXT SET
 		
 		// progress bar
 		c.insets = new Insets(10,1,1,1);
 		c.ipady = 0;
 		c.gridy = gridy;
 		c.gridx = 0;
 		c.gridwidth = 4;
 		c.ipady = 7;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		progressBar = new JProgressBar(0, 100);
 		progressBar.setStringPainted(false);
 		progressBar.setBorderPainted(false);
 		progressBar.setValue(0);
 		progressBar.setForeground(Color.BLUE);
 		progressBar.setVisible(false);
 		jp.add(progressBar, c);
 		
 		// Operon File Name
 		c.insets = new Insets(10,1,1,1);
 		c.ipady = 5;
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 4;
 		LoadedFileName = new JTextField();
 		LoadedFileName.setText(""); // No file loaded
 		LoadedFileName.addActionListener(this);
 		LoadedFileName.setEditable(false);
 		jp.add(LoadedFileName, c);
 		
 		//add context set 
 		c.gridx = 4;
 		c.gridy = gridy;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.ipady = 0;
 		c.insets = new Insets(10,1,1,1);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		btnAddCS = new JButton(strAddCS);
 		btnAddCS.addActionListener(this);
 		btnAddCS.setEnabled(true);
 		jp.add(btnAddCS, c);
 		gridy++;
 		
 		/*
 		 * REMOVE CS
 		 */
 		
 		//Remove CS Heading
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 5;
 		c.insets = new Insets(3,3,3,3);
 		Remove = new JLabel(" REMOVE A CONTEXT SET");
 		Remove.setBackground(Color.GRAY);
 		Remove.setOpaque(true);
 		jp.add(Remove,c);
 		gridy++;
 		
 		// Context Set Text label
 		c.ipady = 5;
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		//c.fill = GridBagConstraints.NONE;
 		c.insets = new Insets(1,1,1,1);
 		contextSetHeader = new JTextField();
 		contextSetHeader.setText("Context Set:"); // context set currently loaded
 		contextSetHeader.addActionListener(this);
 		contextSetHeader.setEditable(false);
 		jp.add(contextSetHeader, c);
 		
 		// drop-down menu for Context Sets
 		c.ipady = 0;
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		//String[] CurrentContextLists = this.convertContextSets(fr.getOS().getCSDs());
 		contextSetMenu = new JComboBox<String>(CurrentContextLists);
 		contextSetMenu.addActionListener(this);
 		contextSetMenu.setEnabled(true);
 		jp.add(contextSetMenu, c);
 		
 		//remove button
 		c.gridx = 3;
 		c.gridy = gridy;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		btnRemoveCS = new JButton(strRemoveCS);
 		btnRemoveCS.addActionListener(this);
 		btnRemoveCS.setEnabled(true);
 		jp.add(btnRemoveCS, c);
 		gridy++;
 		gridy++;
 
 		//submit button
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(10,1,1,1);
 		btnOK = new JButton(strbtnOK);
 		btnOK.addActionListener(this);
 		jp.add(btnOK, c);
 		
 //		//add information to panel
 //		NorthPanel = new JPanel();
 //		NorthPanel.setLayout(new BorderLayout());
 //		NorthPanel.add(jp, BorderLayout.NORTH);
 //		JScrollPane PanelScroll = new JScrollPane(NorthPanel);
 //		this.getContentPane().add(PanelScroll);
 		
 		EnclosingPane = new JScrollPane(jp);
 		//EnclosingPane.setPreferredSize(D);
 		this.getContentPane().add(EnclosingPane, BorderLayout.CENTER);
 	}
 	
 	//add frame components
 	private void getFrame(){
 		
 		//frame-type settings
 		this.setTitle("Add or Remove Context Sets");
 		this.setSize(D);
 		this.setLocationRelativeTo(null);
 		
 		//modality settings
 		this.setModalityType(ModalityType.DOCUMENT_MODAL);
 		this.setModal(true);
 
 	}
 	
 	//disable all components
 	private void DisableComponents(){
 		for (LinkedList<Component> LLC : RadioButtonComponents.values()){
 			for (Component c : LLC){
 				c.setEnabled(false);
 			}
 		}
 	}
 	
 	//ACTIONS
 	@Override
 	public void actionPerformed(ActionEvent evt) {
 		
 		//check boxes: enable + disable appropriate components
 		EnableComponents(CSType.getSelection());
 		
 		//update appropriate message box
 		UpdateMessageBox();
 		
 		//enable adding a CS once a radio button is selected.
 		Enumeration E = CSType.getElements();
 		while (E.hasMoreElements()){
 			Object o = E.nextElement();
 			if (evt.getSource().equals(o)){
 				btnAddCS.setEnabled(true);
 				break;
 			}
 		}
 		
 		//CSType (1) - Intergenic Distance
 		if (evt.getSource().equals(computeIntergenic)){
 			
 			//check if name is acceptable
 			CheckName();
 			
 			if (AcceptableName == true){
 				//create a new swing worker
 				btnLoadCS lo = new btnLoadCS(null);
 				lo.addPropertyChangeListener(this);
 				lo.execute();
 			} 
 
 			
 		} 
 		
 		//CSType (6) CSLoaded
 		if (evt.getSource().equals(btnLoadCS)){
 			
 			//check if name is acceptable
 			CheckName();
 			
 			//only try to read in file if the name is acceptable.
 			if (AcceptableName == true){
 			
 				//String fileName = getMappingFile();
 				String fileName = getGenomicGroupings();
 				
 				if (fileName != null){
 				
 					if (!fileName.equals(OperonStringToDisplay)){
 				
 						LoadedFileName.setText(OperonStringToDisplay);
 						OperonStringToDisplay = fileName;
 						btnLoadCS lo = new btnLoadCS(fileName);
 						lo.addPropertyChangeListener(this);
 						lo.execute();
 				
 						}
 					}
 			
 				}
 		} 
 
 		//CSType (8) CSCombination
 		if (evt.getSource().equals(btnLaunchCombiner)){
 			
 			//check if name is acceptable
 			CheckName();
 			
 			//ensure name is acceptable.
 			if (AcceptableName == true){
 			
 				JOptionPane.showMessageDialog(null,"Functionality not implemented yet!",
 						"Not Yet Created",JOptionPane.INFORMATION_MESSAGE);
 			}
 			
 		} 
 		
 		//Between, limiting distance
 		if (evt.getSource().equals(cbLimitDistance)){
 			if (cbLimitDistance.isSelected()){
 				TxtDistanceLimit.setEnabled(true);
 				LblDistanceLimit.setEnabled(true);
 			} else {
 				TxtDistanceLimit.setEnabled(false);
 				LblDistanceLimit.setEnabled(false);
 			}
 		}
 		
 		//cassette, limiting distance
 		if (evt.getSource().equals(this.cbOnlyNearby)){
 			if (cbOnlyNearby.isSelected()){
 				this.LblNearby.setEnabled(true);
 				this.TxtNearby.setEnabled(true);
 			} else {
 				this.LblNearby.setEnabled(false);
 				this.TxtNearby.setEnabled(false);
 			}
 		}
 		
 		//ADD BUTTON
 		if (evt.getSource().equals(btnAddCS) || evt.getSource().equals(GenesAfter) || evt.getSource().equals(ntAfter)){
 			
 			//check if name is acceptable
 			CheckName();
 			
 			if (AcceptableName == true){
 			
 //				//rule out pre-processed cases, if necessary
 //				if (CSType.isSelected(CSIntergenicDist.getModel()) && ReadyToAdd == false) {
 //					JOptionPane.showMessageDialog(null, 
 //							"Select the compute button to compute genomic groupings before adding", "Gene Groupings not computed",
 //							JOptionPane.ERROR_MESSAGE);
 //				} else 
 					if (CSType.isSelected(CSLoaded.getModel()) && ReadyToAdd == false){
 					JOptionPane.showMessageDialog(null, 
 							"Select the load button to load genomic groupings from file before adding", "Gene Groupings not loaded",
 							JOptionPane.ERROR_MESSAGE);
 				
 				//CSDs to compute on the fly
 				} else {
 				
 					try {
 					
 					//initialize context set description
 					ToAdd = new ContextSetDescription();
 					
 					if (CSType.isSelected(CSIntergenicDist.getModel())){		//CSType (1) - CSIntergenicDist
 						ToAdd.setType("IntergenicDist-pre");
 						//ToAdd.setPreprocessed(true);
 						ToAdd.setPreprocessed(false);
 						ToAdd.setNeedSameStrand(cbStrandOption.isSelected());
 						ToAdd.setIntGenSpacing(Integer.parseInt(intergenicTolerance.getText()));
 					} else if (CSType.isSelected(CSLoaded.getModel())){ 		//CSType (6) - CSLoaded
 						ToAdd.setType("Loaded");
 						ToAdd.setPreprocessed(true);
 					} else if (CSType.isSelected(CSRange.getModel())){ 				//CSType (2) - CSRange
 						ToAdd.setType("Range");	ToAdd.setPreprocessed(false);
 						ToAdd.setNtRangeBefore(Integer.parseInt(ntBefore.getText()));
 						ToAdd.setNtRangeAfter(Integer.parseInt(ntAfter.getText()));
 						
 					} else if (CSType.isSelected(CSGenesAround.getModel())){    //CSType (3) - CSGenesAround
 						ToAdd.setType("GenesAround");	ToAdd.setPreprocessed(false);
 						ToAdd.setGenesBefore(Integer.parseInt(GenesBefore.getText()));
 						ToAdd.setGenesAfter(Integer.parseInt(GenesAfter.getText()));
 						ToAdd.setRelativeBeforeAfter(this.chkAttemptToStrandCorrect.isSelected());
 						
 					} else if (CSType.isSelected(CSGenesBetween.getModel())) {  //CSType (4) - CSGenesBetween
 						ToAdd.setType("GenesBetween");	ToAdd.setPreprocessed(false);
 						if (cbLimitDistance.isSelected()){
 							ToAdd.setGapLimit(true);
 							int Value = 10000;	//default: 10000
 							try {
 								Value = Integer.parseInt(TxtDistanceLimit.getText());
 							} catch (Exception ex){}
 							ToAdd.setGapLimitSize(Value);
 						} else {
 							ToAdd.setGapLimit(false);
 						}
 						
 					} else if (CSType.isSelected(CSMultipleQuery.getModel())){  //CSType (5) - CSMultipleQuery
 						ToAdd.setType("MultipleQuery");	ToAdd.setPreprocessed(false);
 						
 					} else if (CSType.isSelected(CSCassette.getModel())){	//CSType (7) - Cassette
 						ToAdd.setType("Cassette"); ToAdd.setPreprocessed(false);
 						
 						//cassette-related parameters
 						ToAdd.setCassette(true);
 						String CassetteOf = contextSetMenuforCassette.getSelectedItem().toString();
 						ToAdd.setCassetteOf(CassetteOf);
 						
 						//add a limit
 						if (cbOnlyNearby.isSelected()){
 							ToAdd.setNearbyOnly(true);
 							try {
 								String s = this.TxtNearby.getText();
 								Integer Dist = Integer.parseInt(s);
 								if (Dist <  0){
 									throw new Exception();
 								}
 								ToAdd.setNearbyLimit(Dist);
 							} catch (Exception ex){
 								JOptionPane.showMessageDialog(null, 
 										"Nearby Distance must be an integral value greater than 0.\n" +
 										"Could not add Nearby Distance to this Cassette Context Set.",
 										"Number Format Error",JOptionPane.ERROR_MESSAGE);
 								ToAdd.setNearbyOnly(false);
 							}
 						}
 
 					} 
 
 					//Add filters
 					ToAdd.setSingleOrganismAmalgamation(cbAmalg.isSelected());
 					if (this.cbRetainElements.isSelected()){
 						try {
 							String s = this.TxtFractionRetain.getText();
 							Double Value = Double.parseDouble(s);
 							if (Value < 0 || Value > 1){
 								throw new Exception();
 							}
 							ToAdd.setRetainFractionEnabled(true);
 							ToAdd.setRetainFraction(Value);
 						} catch (Exception ex){
 							ToAdd.setRetainFractionEnabled(false);
 							JOptionPane.showMessageDialog(null, 
 									"Retain Fraction must be a numerical value beween 0 and 1.\n" +
 									"Could not add Retain Fraction to this Context Set.",
 									"Number Format Error",JOptionPane.ERROR_MESSAGE);
 						}
 					} else{
 						ToAdd.setRetainFractionEnabled(false);
 					}
 
 					//add description to the OS
 					ToAdd.setName(CSName.getText());
 					fr.getOS().getCSDs().add(ToAdd);
 				
 					//insert item into the menu
 					MostRecentlyAddedContextSet = CSName.getText();
 					contextSetMenu.insertItemAt(CSName.getText(), 0);
 					contextSetMenuforCassette.insertItemAt(CSName.getText(), 0);
 					contextSetMenu.setSelectedItem(CSName.getText());
 					
 					//pre-processed sets are reset
 					ComputedGrouping = false;
 					LoadedGrouping = false;
 					LoadedFileName.setText("Context Set \"" + ToAdd.getName() + "\" Successfully Added!");
 					
 					} catch (Exception ex) {
 						JOptionPane.showMessageDialog(null, "Field values must be integers",
 								"Integer",JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			} 
 		}
 		
 		if (evt.getSource().equals(cbRetainElements)){
 			this.TxtFractionRetain.setEnabled(cbRetainElements.isSelected());
 		}
 		
 		//REMOVE BUTTON
 		if (evt.getSource().equals(btnRemoveCS)){
 			if (!contextSetMenu.getSelectedItem().equals("SingleGene")){
 				
 				try {
 					
 					//update available context sets
 					//OS level
 					for (int i = 0; i < fr.getOS().getCSDs().size(); i++){
 						if (fr.getOS().getCSDs().get(i).getName().equals(contextSetMenu.getSelectedItem())){
 							
 							//remove from all annotated genomes, if necessary
 							if (fr.getOS().getCSDs().get(i).isPreprocessed()){
 								for (AnnotatedGenome AG : fr.getOS().getSpecies().values()){
 									for (ContextSet CS : AG.getGroupings()){
 										if (CS.getName().equals(fr.getOS().getCSDs().get(i).getName())){
 											AG.getGroupings().remove(CS);
 										}
 									}
 								}
 							}
 							
 							//remove the organism set-wide context set description
 							fr.getOS().getCSDs().remove(i);
 						}
 					}
 					
 				} catch (Exception ex) {}
 
 				//remove from JComboBoxes
 				//add/remove menu
 				Object Item = contextSetMenu.getSelectedItem();
 				contextSetMenu.removeItem(contextSetMenu.getSelectedItem());
 				
 				//cassette menu
 				for (int i = 0; i < contextSetMenuforCassette.getItemCount(); i++){
 					if (contextSetMenuforCassette.getItemAt(i).equals(Item)){
 						contextSetMenuforCassette.removeItem(Item);
 						break;
 					}
 				}
 				
 				//remove from parent panel
 				for (int i = 0; i < fr.getPanBtn().getContextSetMenu().getItemCount(); i++){
 					if (fr.getPanBtn().getContextSetMenu().getItemAt(i).equals(Item)){
 						fr.getPanBtn().getContextSetMenu().removeItem(Item);
 						break;
 					}
 				}
 
 
 			} else {
 				
 				//remove the one and only context set
 				 JOptionPane.showMessageDialog(null, 
 						 "Unable to remove this context set.",
 						 "SingleGene Context Set",JOptionPane.ERROR_MESSAGE);
 				
 			}
 		} else if (evt.getSource().equals(btnOK)){
 			
 			//set the main frame menu to the existing menu.
 			//remove all items, then add all items back.
 			this.fr.getPanBtn().getContextSetMenu().removeAllItems();
 			for (int i = 0; i < fr.getOS().getCSDs().size(); i++){
 				this.fr.getPanBtn().getContextSetMenu().addItem(fr.getOS().getCSDs().get(i).getName());
 			}
 			this.fr.getPanBtn().getContextSetMenu().setSelectedItem(MostRecentlyAddedContextSet);
 			
 //			this.fr.getPanBtn().setContextSetMenu(contextSetMenu);
 //			this.fr.getPanBtn().revalidate();
 //			this.fr.getPanBtn().repaint();
 			
 			//close this window.
 			this.dispose();
 
 		}
 		
 		}
 
 	//enable all components within a single button group
 	public void EnableComponents(ButtonModel selectedGroup){
 
 		//enable appropriate  components
 		for (ButtonModel bm : RadioButtonComponents.keySet()){
 			LinkedList<Component> LL = RadioButtonComponents.get(bm);
 			if (bm.equals(selectedGroup)){
 				for (Component c : LL){
 					c.setEnabled(true);
 				}
 				
 				//case: between range limiter
 				if (LL.equals(CSGenesBetween_group) && !cbLimitDistance.isSelected()){
 					TxtDistanceLimit.setEnabled(false);
 					LblDistanceLimit.setEnabled(false);
 				}
 				
 				//case: cassette limiter
 				if (LL.equals(CSCassette_group) && !cbOnlyNearby.isSelected()){
 					LblNearby.setEnabled(false);
 					TxtNearby.setEnabled(false);
 				}
 				
 			} else {
 				for (Component c : LL){
 					c.setEnabled(false);
 				}
 			}
 		}
 		
 		//change message
 		if (CSName.getText().contentEquals("Between") || CSName.getText().contentEquals("MultipleQuery")){
 			CSName.setText("");
 		}
 	}
 
 	//enable message box
 	public void UpdateMessageBox(){
 
 		//update the message to the user based on what they're doing
 		if (CSType.isSelected(CSIntergenicDist.getModel())){
 //			if (ComputedGrouping == true){
 //				LoadedFileName.setText(ComputedString);
 //			} else {
 //				LoadedFileName.setText("No gene groupings currently computed.");
 //			}
 			//Initialize label sring
 			
 			//build string message
 			String strText = "Adjacent";
 			
 				//mention strandedness
 				if (cbStrandOption.isSelected()){
 					strText = strText + ", same-stranded";
 				} 
 				strText = strText + " genes with intergenic distance <= " + intergenicTolerance.getText() +" nt are grouped together.";
 			LoadedFileName.setText(strText);
 			
 		} else if (CSType.isSelected(CSLoaded.getModel())){
 			if (LoadedGrouping == true){
 				LoadedFileName.setText(LoadedString);
 			} else {
 				LoadedFileName.setText("No gene groupings are currently loaded.");
 			}
 		} else if (CSType.isSelected(CSRange.getModel())){
 			LoadedFileName.setText("All genes within a defined range of a single gene query are grouped together.");
 		} else if (CSType.isSelected(CSGenesBetween.getModel())){
 			LoadedFileName.setText("All genes between two independent queries are grouped together.");
 			if (CSName.getText().contentEquals("") || CSName.getText().contentEquals("MultipleQuery")
 					|| CSName.getText().contentEquals("Cas-")){
 				CSName.setText("Between");
 			}
 		} else if (CSType.isSelected(CSGenesAround.getModel())){
 			LoadedFileName.setText("A number of genes both before and after a single gene query are grouped together");
 		} else if (CSType.isSelected(CSMultipleQuery.getModel())) {
 			LoadedFileName.setText("Multiple gene query matches within a single organism are grouped together.");
 			if (CSName.getText().contentEquals("")  || CSName.getText().contentEquals("Between") 
 					|| CSName.getText().contentEquals("Cas-")){
 				CSName.setText("MultipleQuery");
 			}
 		} else if (CSType.isSelected(CSCombination.getModel())){
 			LoadedFileName.setText("Combine existing gene groupings to create a more complex gene grouping.");
 		} else if (CSType.isSelected(CSCassette.getModel())){
 			LoadedFileName.setText("Create a cassette associated with an existing Context Set.");
 			if (CSName.getText().contentEquals("") || CSName.getText().contentEquals("MultipleQuery") ||
 					CSName.getText().contentEquals("Between")){
 				String DefaultCassetteName = "Cas-";
 				CSName.setText(DefaultCassetteName);
 			}
 		}
 
 	}
 	
 	//check if name is acceptable
 	public void CheckName(){
 		
 		//set name as acceptable.
 		this.AcceptableName = true;
 		
 		//check for unique names
 		LinkedList<String> CurrentContextSets = new LinkedList<String>();
 		for (int i = 0 ; i< fr.getOS().getCSDs().size(); i++){
 			CurrentContextSets.add(fr.getOS().getCSDs().get(i).getName());
 		}
 		
 		//name is unacceptable if non-unique
 		for (int i = 0; i <CurrentContextSets.size(); i++){
 			if (CurrentContextSets.get(i).equals(CSName.getText())){
 				AcceptableName = false;
 			}
 		}
 		
 		//name is also unacceptable if field is empty
 		if (CSName.getText().equals("")){
 			AcceptableName = false;
 		}
 
 		//show error message if appropriate
 		if (AcceptableName == false){
 			JOptionPane.showMessageDialog(null, "Please give the context set a unique name.",
 					"Name Missing",JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	public String[] convertContextSets(LinkedList<ContextSetDescription> ListOfContextSets){
 		
 		//initialize output array
 		String[] ArrayOfContextSets = new String[ListOfContextSets.size()];
 		
 		//iterate through array
 		for (int i = 0; i < ListOfContextSets.size(); i++){
 			ArrayOfContextSets[i] = ListOfContextSets.get(i).getName();
 		}
 		
 		return ArrayOfContextSets;
 	}
 
 	//retrieve a data file
 	private String getMappingFile() {
 		
 		//use pre-existing 'FileDialog' GUI window to retrieve file
 		final FileDialog fd = new FileDialog(this, "English",
 				FileDialog.LOAD);
 
 		fd.setVisible(true);
 		String MappingFile = fd.getDirectory() + fd.getFile();
 		//String MappingFile =  fd.getFile();
 		if (fd.getFile() == null) {
 			MappingFile = null;
 		} 
 		return MappingFile; //file name
 	}
 
 	// retrieve either directory or data file of pre-computed genomic groupings
 	private String getGenomicGroupings(){
 
 		// initialize output
 		JFileChooser GetGenomicGroupings = new JFileChooser();
 		
 		GetGenomicGroupings.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 		GetGenomicGroupings
 				.setDialogTitle("Select directory or a single file of custom genomic groupings");
 
 		if (this.ReferenceDirectory != null) {
 			GetGenomicGroupings.setCurrentDirectory(ReferenceDirectory);
 		} else {
 			GetGenomicGroupings.setCurrentDirectory(new File("."));
 		}
 		GetGenomicGroupings.showOpenDialog(GetGenomicGroupings);
 
 		// retrieve a directory
 		// File[] AllFiles = GetGenomes.getSelectedFiles();
 		File DirectoryOrGWSFile = GetGenomicGroupings.getSelectedFile();
 		this.GenomeWorkingSetFile_NoPath = DirectoryOrGWSFile.getName();
 
 		// note current directory for next time
 		if (GetGenomicGroupings.getCurrentDirectory() != null) {
 			this.ReferenceDirectory = GetGenomicGroupings.getCurrentDirectory();
 		}
 
 		// check if file could be received
 		if (DirectoryOrGWSFile != null) {
 
 			// determine if file or directory loaded
 			if (DirectoryOrGWSFile.isDirectory()) {
 
 				// retrieving info as a directory.
 				this.GenomicGroupingsAsSingleFile = false;
 
 				// retrieve directory
 				this.GenomeGroupingFiles = DirectoryOrGWSFile.listFiles();
 
 			} else {
 
 				// all information stored in a single genome working set file.
 				this.GenomicGroupingsAsSingleFile = true;
 
 			}
 		}
 
 		// return the information.
 		return DirectoryOrGWSFile.getAbsolutePath();
 	}
 	
 	//update progress bar
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if (evt.getPropertyName() == "progress") {
 			int progress = (Integer) evt.getNewValue();
 			progressBar.setValue(progress);
 		}
 	}
 }
