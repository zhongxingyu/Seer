 package cytoscape.util.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JRadioButton;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 
 import cytoscape.CytoscapeInit;
 
 /*
  * Final Step for the new file format.
  * This panel creates the manifest file and load the biodataserver.
  */
 public class BioDataServerPanel4 extends JPanel {
 
 	private JLabel anotherBlankSpace;
 
 	private JLabel blankSpace;
 
 	private ButtonGroup connectorGroup;
 
 	private JLabel jLabel1;
 
 	private JPanel jPanel1;
 
 	private JLabel progressDescription;
 
 	private JProgressBar progressSent;
 
 	private JLabel welcomeTitle;
 
 	private JLabel yetAnotherBlankSpace1;
 
 	private JPanel contentPanel;
 
 	private JLabel iconLabel;
 
 	private JSeparator separator;
 
 	private JLabel textLabel;
 
 	private JPanel titlePanel;
 
 	private JLabel jLabel2;
 
 	private JLabel jLabel3;
 
 	private JComboBox spList;
 
 	File start;
 
 	String species;
 
 	private JTextField taxonNameBox;
 
 	private JButton setButton;
 
 	private ButtonGroup selectionType;
 
 	private JRadioButton comboBoxButton;
 
 	private JRadioButton textBoxButton;
 
 	private JPanel jPanel2;
 
 	private JPanel jPanel3;
 
 	private JTextField currentSpBox;
 
 	private JTextField manifestFileName;
 
 	private JButton selectManifestFile;
 
 	private String manifestName;
 
 	private JFileChooser jc1;
 
 	static final String TAXON_FILE = "tax_report.txt";
 
 	private final String FS = System.getProperty("file.separator");
 
 	public BioDataServerPanel4() {
 
 		super();
 
 		species = null;
 		start = CytoscapeInit.getMRUD();
 		spList = new JComboBox();
 
 		contentPanel = getContentPanel();
 		ImageIcon icon = getImageIcon();
 
 		titlePanel = new javax.swing.JPanel();
 		textLabel = new javax.swing.JLabel();
 		iconLabel = new javax.swing.JLabel();
 		separator = new javax.swing.JSeparator();
 
 		setLayout(new java.awt.BorderLayout());
 
 		titlePanel.setLayout(new java.awt.BorderLayout());
 		titlePanel.setBackground(Color.gray);
 
 		textLabel.setBackground(Color.gray);
 		textLabel.setFont(new Font("Sans Serif", Font.BOLD, 14));
 		textLabel.setText("Load Manifest File");
 		textLabel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
 		textLabel.setOpaque(true);
 
 		iconLabel.setBackground(Color.gray);
 		if (icon != null)
 			iconLabel.setIcon(icon);
 
 		titlePanel.add(textLabel, BorderLayout.CENTER);
 		titlePanel.add(iconLabel, BorderLayout.EAST);
 		titlePanel.add(separator, BorderLayout.SOUTH);
 
 		add(titlePanel, BorderLayout.NORTH);
 		JPanel secondaryPanel = new JPanel();
 		secondaryPanel.add(contentPanel, BorderLayout.SOUTH);
 		add(secondaryPanel, BorderLayout.WEST);
 
 	}
 
 	public void createManifestFileChooser() {
 		jc1 = new JFileChooser(start);
 		jc1.setDialogTitle("Select Manifest File");
 	}
 
 	public String getManifestFileNameFromTextBox() {
 		manifestName = manifestFileName.getText();
 		return manifestName;
 	}
 
 	public void addSelectButtonActionListener(ActionListener l) {
 		selectManifestFile.addActionListener(l);
 	}
 
 	public File getManifestFile(boolean show) {
 		File targetFile = null;
 		if (show == true) {
 			int result = jc1.showOpenDialog(null);
 		}
 		targetFile = jc1.getSelectedFile();
 		return targetFile;
 	}
 
 	public void setManifestFileName(String fileName) {
 		manifestFileName.setText(fileName);
 		manifestName = fileName;
 	}
 
 	public String getManifestFileName() {
 		return manifestName;
 	}
 
 	private JPanel getContentPanel() {
 
 		JPanel contentPanel1 = new JPanel();
 		contentPanel1.setLayout(new GridLayout(3, 1));
 
 		// String filePath = start + "/testData/annotation/tax_report.txt";
 		String filePath = start + FS + TAXON_FILE;
 		// System.out.println( "Taxon file is: " + filePath );
 		File taxonFile = new File(filePath);
 
		manifestFileName = new JTextField(38);
 		selectManifestFile = new JButton("Select Manifest File");
 
 		jPanel1 = new JPanel();
 		jPanel1.setLayout(new FlowLayout());
 
 		blankSpace = new JLabel();
 		anotherBlankSpace = new JLabel();
 		yetAnotherBlankSpace1 = new JLabel();
 
 		jLabel1 = new JLabel();
 		jLabel2 = new JLabel();
 		jLabel3 = new JLabel();
 
 		jLabel1
 				.setText("Manifest file should include both annotation and ontology files.");
 
 		contentPanel1.add(jLabel1);
 		jPanel1.add(blankSpace);
 
 		// jPanel1.add(jLabel2);
 		jPanel1.add(anotherBlankSpace);
 		jPanel1.add(manifestFileName);
 		jPanel1.add(selectManifestFile);
 		// jPanel1.add(anotherBlankSpace);
 		contentPanel1.add(jPanel1);
 
 		jLabel3
 				.setText("Please press the Finish button to load BioDataServer.");
 		contentPanel1.add(jLabel3);
 
 		return contentPanel1;
 	}
 
 	public void setComboBoxState(boolean state) {
 		spList.setEnabled(state);
 	}
 
 	public void setTextBoxState(boolean state) {
 		taxonNameBox.setEnabled(state);
 	}
 
 	private ImageIcon getImageIcon() {
 		return null;
 	}
 
 }
