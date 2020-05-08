 package cytoscape.util.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.ListModel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.JScrollPane;
 
 import cytoscape.CytoscapeInit;
 import cytoscape.util.BioDataServerUtil;
 
 public class BioDataServerPanel6 extends JPanel {
 
 	File start;
 	
 	
 	private final String FS = System.getProperty("file.separator");
 
 	private final String ANNOTATION_DIR = "/testData/annotation/";
 
 	// lookup table for the taxon number <-> name
 	public final String TAXON_FILE = "tax_report.txt";
 	
 	private String taxonFileLocation;
 	
 	private BioDataServerUtil bdsUtil;
 	
 	private boolean gaFileSelectedFlag = false;
 	
 
 	private JCheckBox overwriteCheckBox = null;
 
 	private JTextPane jTextPane1 = null;
 
 	private JPanel oboPanel = null;
 
 	private JTextField oboFileName = null;
 
 	private JPanel gaPanel = null;
 
 	private JPanel overwritePanel = null;
 
 	private JComboBox overwriteComboBox = null;
 
 	private JButton selectOboButton = null;
 
 	private JPanel jPanel1 = null;
 
 	private JButton addGAButton = null;
 
 	private JList gaFileList = null;
 
 	private JPanel jPanel2 = null;
 
 	private JPanel titlePanel = null;
 
 	private JLabel textLabel = null;
 
 	private JScrollPane jScrollPane = null;
 
 	private Vector gaListItems;
 
 	private JTextArea messageArea2 = null;
 
 	private JLabel jLabel = null;
 
 	private JCheckBox flipCheckBox = null;
 
 	public BioDataServerPanel6() {
 		super();
 		// TODO Auto-generated constructor stub
 		initialize();
 	}
 
 	public BioDataServerPanel6(boolean arg0) {
 		super(arg0);
 		// TODO Auto-generated constructor stub
 		initialize();
 	}
 
 	public BioDataServerPanel6(LayoutManager arg0) {
 		super(arg0);
 		// TODO Auto-generated constructor stub
 		initialize();
 	}
 
 	public BioDataServerPanel6(LayoutManager arg0, boolean arg1) {
 		super(arg0, arg1);
 		// TODO Auto-generated constructor stub
 		initialize();
 	}
 
 	/**
 	 * This method initializes this
 	 * 
 	 * @return void
 	 */
 	private void initialize() {
 
 		bdsUtil = new BioDataServerUtil();
 		gaListItems = new Vector();
 
 		this.setLayout(new BorderLayout());
 		this.setSize(517, 363);
 		this.add(getJPanel2(), java.awt.BorderLayout.CENTER);
 		this.add(getTitlePanel(), java.awt.BorderLayout.NORTH);
 	}
 
 	/**
 	 * This method initializes jCheckBox
 	 * 
 	 * @return javax.swing.JCheckBox
 	 */
 	private JCheckBox getOverwriteCheckBox() {
 		if (overwriteCheckBox == null) {
 			overwriteCheckBox = new JCheckBox();
 			overwriteCheckBox.setText("Overwrite default species name with:");
 			overwriteCheckBox.setActionCommand("overwrite");
 		}
 		return overwriteCheckBox;
 	}
 
 	/**
 	 * This method initializes jTextPane1
 	 * 
 	 * @return javax.swing.JTextPane
 	 */
 	private JTextPane getJTextPane1() {
 		if (jTextPane1 == null) {
 
 			String page = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">";
 			page = page
 					+ "<HTML><BODY>Full list of Gene Association files is available at:";
 			page = page
 					+ "<UL><LI><P><STRONG>The Gene Ontology Project: <A HREF=\"http://www.geneontology.org/GO.current.annotations.shtml\" TARGET=\"_blank\">";
 			page = page + "Current Annotations</A></STRONG></P></UL>";
 
 			page = page + "Taxonomy tabele is available from NCBI:";
 			page = page + "<UL><LI><P><STRONG><A HREF=\"http://www.ncbi.nlm.nih.gov/Taxonomy/TaxIdentifier/tax_identifier.cgi\" TARGET=\"_blank\">";
 			page = page + "Taxonomy name/id Status Report Page</A></STRONG></P></UL></BODY></HTML>";
 			
 			jTextPane1 = new JTextPane();
 			jTextPane1.setContentType("text/html");
 			jTextPane1.setBounds(new java.awt.Rectangle(7,207,504,110));
 			jTextPane1.setText(page);
 			jTextPane1.setBackground(java.awt.SystemColor.window);
 			jTextPane1.setEditable(false);
			
			
			jTextPane1.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        //hyperlinkActivated(e.getURL());
                        cytoscape.util.OpenBrowser.openURL(e.getURL().toString());
                        System.out.println("Opening Web Page: " + e.getURL().toString() );
                    }
                }
            });
			
 		}
 		return jTextPane1;
 	}
 
 	/**
 	 * This method initializes jPanel1
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJPanel1() {
 		if (oboPanel == null) {
 			BorderLayout borderLayout2 = new BorderLayout();
 			borderLayout2.setHgap(5);
 			borderLayout2.setVgap(5);
 			oboPanel = new JPanel();
 			oboPanel.setBounds(new java.awt.Rectangle(4,5,508,23));
 			oboPanel.setLayout(borderLayout2);
 			oboPanel.add(getOboFileName(), java.awt.BorderLayout.CENTER);
 			oboPanel.add(getJButton(), java.awt.BorderLayout.EAST);
 		}
 		return oboPanel;
 	}
 
 	/**
 	 * This method initializes jTextField1
 	 * 
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getOboFileName() {
 		if (oboFileName == null) {
 			oboFileName = new JTextField();
 			oboFileName.setActionCommand("oboFileName");
 		}
 		return oboFileName;
 	}
 
 	/**
 	 * This method initializes jPanel
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getGaPanel() {
 		if (gaPanel == null) {
 			jLabel = new JLabel();
 			jLabel.setText("The Gene Association files listed below will be loaded:");
 			jLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
 			BorderLayout borderLayout1 = new BorderLayout();
 			borderLayout1.setHgap(5);
 			borderLayout1.setVgap(5);
 			gaPanel = new JPanel();
 			gaPanel.setBounds(new java.awt.Rectangle(5,32,507,125));
 			gaPanel.setLayout(borderLayout1);
 			gaPanel.add(getJPanel13(), java.awt.BorderLayout.EAST);
 			gaPanel.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
 			gaPanel.add(jLabel, java.awt.BorderLayout.NORTH);
 		}
 		return gaPanel;
 	}
 
 	/**
 	 * This method initializes jPanel1
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJPanel12() {
 		if (overwritePanel == null) {
 			GridLayout gridLayout = new GridLayout();
 			gridLayout.setRows(1);
 			gridLayout.setColumns(2);
 			overwritePanel = new JPanel();
 			overwritePanel.setBounds(new java.awt.Rectangle(5,180,505,26));
 			overwritePanel.setLayout(gridLayout);
 			overwritePanel.add(getOverwriteCheckBox(), null);
 			overwritePanel.add(getOverwriteComboBox(), null);
 		}
 		return overwritePanel;
 	}
 
 	/**
 	 * This method initializes jComboBox
 	 * 
 	 * @return javax.swing.JComboBox
 	 */
 	private JComboBox getOverwriteComboBox() {
 		if (overwriteComboBox == null) {
 			overwriteComboBox = new JComboBox();
 			setTaxonomyTable();
 			overwriteComboBox.setEditable(false);
 			overwriteComboBox.setEnabled(false);
 		}
 		return overwriteComboBox;
 	}
 
 	/**
 	 * This method initializes jButton
 	 * 
 	 * @return javax.swing.JButton
 	 */
 	private JButton getJButton() {
 		if (selectOboButton == null) {
 			selectOboButton = new JButton();
 			selectOboButton.setActionCommand("selectObo");
 			selectOboButton.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
 			selectOboButton.setText("Select Obo File");
 		}
 		return selectOboButton;
 	}
 
 	/**
 	 * This method initializes jPanel1
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJPanel13() {
 		if (jPanel1 == null) {
 			jPanel1 = new JPanel();
 			jPanel1.setLayout(new BorderLayout());
 			jPanel1.add(getJButton2(), java.awt.BorderLayout.SOUTH);
 			jPanel1.add(getMessageArea2(), java.awt.BorderLayout.CENTER);
 		}
 		return jPanel1;
 	}
 
 	/**
 	 * This method initializes jButton
 	 * 
 	 * @return javax.swing.JButton
 	 */
 	private JButton getJButton2() {
 		if (addGAButton == null) {
 			addGAButton = new JButton();
 			addGAButton.setActionCommand("addGA");
 			addGAButton.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
 			addGAButton.setText("Add Gene Association File");
 		}
 		return addGAButton;
 	}
 
 	// Adding Action Listners
 
 	public void addButtonActionListener(ActionListener l) {
 		addGAButton.addActionListener(l);
 		selectOboButton.addActionListener(l);
 	}
 
 	public void addOboTextFieldActionListener(ActionListener l) {
 		oboFileName.addActionListener(l);
 	}
 
 	public void addGaListActionListener(ListSelectionListener l) {
 		gaFileList.addListSelectionListener(l);
 	}
 
 	public void addOboTextFieldDocumentListener(DocumentListener l) {
 		oboFileName.getDocument().addDocumentListener(l);
 	}
 
 	public void addOverwriteCheckBoxActionListener(ActionListener l) {
 		overwriteCheckBox.addActionListener(l);
 	}
 	
 	public void addFlipCheckBoxActionListener(ActionListener l) {
 		flipCheckBox.addActionListener(l);
 	}
 	
 	public void addOverwriteComboBoxActionListener(ActionListener l) {
 		overwriteComboBox.addActionListener(l);
 	}
 
 	/**
 	 * This method initializes jList
 	 * 
 	 * @return javax.swing.JList
 	 */
 	private JList getGaFileList() {
 		String taxonMessage = null;
 		
 		if (gaFileList == null) {
 			
 			gaListItems.add("No Gene Association file selected.");
 			gaFileList = new JList(gaListItems);
 
 			MouseListener mouseListener = new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					if (e.getClickCount() == 1) {
 						String selectedFileName = (String) gaFileList.getSelectedValue();
 						File selectedFile = new File(selectedFileName);
 						
 						if (selectedFile.canRead() == true) {
 							try {
 								final BufferedReader gaFileReader = new BufferedReader(
 										new FileReader(selectedFile));
 								
 								File taxonTarget = new File(taxonFileLocation);
 								try {
 									//messageTextArea.setText( selectedFile.getName() + 
 									//		" is an annotation file for " + bdsUtil.checkSpecies( gaFileReader, taxonTarget ));
 									
 									messageArea2.setText( selectedFile.getName() + 
 											" is an annotation file for " + bdsUtil.checkSpecies( gaFileReader, taxonTarget ));
 									
 									gaFileReader.close();
 									//System.out.print( "Target is : " + bdsUtil.checkSpecies( gaFileReader, taxonTarget ));
 								} catch (IOException e1) {
 									// TODO Auto-generated catch block
 									e1.printStackTrace();
 								}
 								
 							} catch (FileNotFoundException e1) {
 								// TODO Auto-generated catch block
 								e1.printStackTrace();
 							}
 						}
						//System.out.println("Clicked on Item " + selectedFileName );
 					}
 				}
 			};
 			gaFileList.addMouseListener(mouseListener);
 
 		}
 		return gaFileList;
 	}
 
 	public String getOboTextField() {
 		return oboFileName.getText();
 	}
 
 	/**
 	 * This method initializes jPanel2
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJPanel2() {
 		if (jPanel2 == null) {
 			jPanel2 = new JPanel();
 			jPanel2.setLayout(null);
 			jPanel2.add(getJTextPane1(), null);
 			jPanel2.add(getJPanel12(), null);
 			jPanel2.add(getGaPanel(), null);
 			jPanel2.add(getJPanel1(), null);
 			jPanel2.add(getFlipCheckBox(), null);
 		}
 		return jPanel2;
 	}
 
 	/**
 	 * This method initializes jPanel3
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getTitlePanel() {
 		if (titlePanel == null) {
 			titlePanel = new JPanel();
 			titlePanel.setLayout(new BorderLayout());
 			titlePanel.setBackground(java.awt.Color.gray);
 			titlePanel.add(getTextLabel(), java.awt.BorderLayout.CENTER);
 		}
 		return titlePanel;
 	}
 
 	/**
 	 * This method initializes jLabel
 	 * 
 	 * @return javax.swing.JLabel
 	 */
 	private JLabel getTextLabel() {
 		if (textLabel == null) {
 			textLabel = new JLabel();
 			textLabel.setBackground(Color.gray);
 			textLabel.setFont(new Font("Sans Serif", Font.BOLD, 15));
 			textLabel.setText("Select Ontology And Annotation File");
 			textLabel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
 			textLabel.setOpaque(true);
 		}
 		return textLabel;
 	}
 
 	public void setOboFileName(String fileName) {
 		oboFileName.setText(fileName);
 	}
 
 	public void addGaFile(String newGaFile) {
 		if(gaFileSelectedFlag == false ) {
 			gaListItems = new Vector();
 			gaFileSelectedFlag = true;
 		}
 		gaListItems.add(newGaFile);
 		gaFileList.setListData(gaListItems);
 		
 		gaFileList.setSelectedValue(newGaFile, true);
 		
 	}
 
 	/**
 	 * This method initializes jScrollPane
 	 * 
 	 * @return javax.swing.JScrollPane
 	 */
 	private JScrollPane getJScrollPane() {
 		if (jScrollPane == null) {
 			jScrollPane = new JScrollPane();
 			jScrollPane.setViewportView(getGaFileList());
 		}
 		return jScrollPane;
 	}
 	
 	
 	// ======================================================================
 	// ======================================================================
 	
 	public void setTaxonomyTable() {
 		String filePath = start + FS + TAXON_FILE;
 		File taxonFile = new File(filePath);
 		taxonFileLocation = taxonFile.getAbsolutePath();
 		// Find tax_report.
 		boolean taxonFound = false;
 		
 		// In case taxon file is not readable...
 		if (taxonFile.canRead() == false) {
 			try {
 				File file = new File(System.getProperty("user.dir"), TAXON_FILE);
 				if (!taxonFound) {
 					taxonFile = file;
 					System.out.println("Taxonomy table found at: " + taxonFile);
 					taxonFound = true;
 					taxonFileLocation = taxonFile.getAbsolutePath();
 				}
 
 			} catch (Exception e) {
 				taxonFound = false;
 			}
 			
 			try {
 				File file = new File(System.getProperty("CYTOSCAPE_HOME"),
 						TAXON_FILE);
 				if (!taxonFound) {
 					taxonFile = file;
 					System.out.println("Taxonomy table found at: " + taxonFile);
 					taxonFound = true;
 					taxonFileLocation = taxonFile.getAbsolutePath();
 				}
 
 			} catch (Exception e) {
 				taxonFound = false;
 			}
 
 			try {
 				File file = CytoscapeInit.getConfigFile(TAXON_FILE);
 				if (!taxonFound) {
 					taxonFile = file;
 					System.out.println("Taxonomy table found at: " + taxonFile);
 					taxonFound = true;
 					taxonFileLocation = taxonFile.getAbsolutePath();
 				}
 
 			} catch (Exception e) {
 				taxonFound = false;
 			}
 			
 			try {
 				String fileLocation = CytoscapeInit.getPropertiesLocation() + FS + TAXON_FILE;
 				File file = new File( fileLocation );
 				if (!taxonFound) {
 					taxonFile = file;
 					System.out.println("Taxonomy table found at: " + taxonFile);
 					taxonFound = true;
 					taxonFileLocation = taxonFile.getAbsolutePath();
 				}
 
 			} catch (Exception e) {
 				taxonFound = false;
 			}
 		}
 
 		System.out.println("Taxonomy file found at: " + taxonFile.getAbsolutePath());
 		BufferedReader spListReader = null;
 		try {
 			spListReader = new BufferedReader(new FileReader(taxonFile));
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		try {
 			setSpList(spListReader);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 	
 	protected void setSpList(final BufferedReader rd) throws IOException {
 		String curLine = null;
 		String name1 = null;
 		String name2 = null;
 
 		// remove the first line, which is a title
 		curLine = rd.readLine();
 
 		while ((curLine = rd.readLine()) != null) {
 			StringTokenizer st = new StringTokenizer(curLine, "|");
 			st.nextToken();
 			name1 = st.nextToken().trim();
 			name2 = st.nextToken().trim();
 			//if (name2.length() > 1) {
 			//	overwriteComboBox.addItem(name2);
 			//} else {
 				overwriteComboBox.addItem(name1);
 			//}
 		}
 	}
 	
 	public void setOverwriteState() {
 		if(overwriteCheckBox.isSelected()) {
 			overwriteComboBox.setEnabled(true);
 			// Set def sp. name here...
 			
 			
 		} else {
 			overwriteComboBox.setEnabled(false);
 		}
 	}
 
 	/**
 	 * This method initializes jTextArea	
 	 * 	
 	 * @return javax.swing.JTextArea	
 	 */
 	private JTextArea getMessageArea2() {
 		if (messageArea2 == null) {
 			messageArea2 = new JTextArea();
 			messageArea2.setLineWrap(true);
 			messageArea2.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
 			messageArea2.setText("Please click file name on the left to check species... ");
 			messageArea2.setWrapStyleWord(true);
 		}
 		return messageArea2;
 	}
 	
 	
 	public String getOverwiteComboBox() {
 		return (String) overwriteComboBox.getSelectedItem();
 	}
 
 	/**
 	 * This method initializes jCheckBox	
 	 * 	
 	 * @return javax.swing.JCheckBox	
 	 */
 	private JCheckBox getFlipCheckBox() {
 		if (flipCheckBox == null) {
 			flipCheckBox = new JCheckBox();
 			flipCheckBox.setText("Flip Cannonical Name and Common Name");
 			flipCheckBox.setBounds(new java.awt.Rectangle(5,160,508,20));
 			flipCheckBox.setActionCommand("flip");
 		}
 		return flipCheckBox;
 	}
 	
 	public boolean getFlipCheckBoxStatus() {
 		return flipCheckBox.isSelected();
 	}
 	
 	public String[] getGAFileList() {
 		ListModel model = gaFileList.getModel();
 		String[] gaList = new String[model.getSize()];
 		for (int i = 0; i<model.getSize(); i++ ) {
 			gaList[i] = (String) model.getElementAt( i );
 		}
 		return gaList;
 	}
 	
 
 	
 } // @jve:decl-index=0:visual-constraint="10,10"
