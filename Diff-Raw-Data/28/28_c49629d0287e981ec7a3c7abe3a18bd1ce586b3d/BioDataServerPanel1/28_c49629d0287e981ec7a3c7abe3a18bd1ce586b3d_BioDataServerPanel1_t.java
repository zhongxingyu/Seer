 package cytoscape.util.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.Insets;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.JLabel;
 
 /*
  * Internal Class for the 1st panel
  */
 public class BioDataServerPanel1 extends JPanel {
 
 	private JLabel blankSpace;
 
 	private JLabel anotherBlankSpace;
 
 	private JLabel yetAnotherBlankSpace1;
 
 	private JLabel yetAnotherBlankSpace2;
 
 	private JLabel jLabel1;
 
 	private JLabel jLabel2;
 
 	private JLabel jLabel9;
 
 	private JLabel welcomeTitle;
 
 	private JPanel contentPanel;
 
 	private JLabel iconLabel;
 
 	private ImageIcon icon;
 
 	private javax.swing.JRadioButton oldFileRadioButton;
 
 	private javax.swing.JRadioButton newFileRadioButton;
 
 	private javax.swing.ButtonGroup connectorGroup;
 
 	private JLabel yetAnotherBlankSpace3;
 
 	private JLabel yetAnotherBlankSpace4;
 
 	private JLabel yetAnotherBlankSpace8;
 
 	private JLabel yetAnotherBlankSpace7;
 
 	private JLabel yetAnotherBlankSpace6;
 
 	private JLabel yetAnotherBlankSpace5;
 
 	private JLabel yetAnotherBlankSpace9;
 
 	private JLabel yetAnotherBlankSpace10;
 
 	private JLabel yetAnotherBlankSpace11;
 
 	private JLabel yetAnotherBlankSpace12;
 
 	private JLabel yetAnotherBlankSpace13;
 
 	private JLabel noteLabel;
 
 	private JLabel noteLabel1;
 
 	private JLabel noteLabel2;
 
 	private JLabel noteLabel3;
 
 	public BioDataServerPanel1() {
 
 		iconLabel = new JLabel();
 		contentPanel = getContentPanel();
 		
 		contentPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
 
 		icon = getImageIcon();
 
 		setLayout(new java.awt.BorderLayout());
 
 		if (icon != null)
 			iconLabel.setIcon(icon);
 
 		iconLabel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
 
 		add(iconLabel, BorderLayout.WEST);
 
 		JPanel secondaryPanel = new JPanel();
 		secondaryPanel.add(contentPanel, BorderLayout.NORTH);
 		add(secondaryPanel, BorderLayout.CENTER);
 		
 		this.setSize(10,10);
 		
 	}
 
 	private JPanel getContentPanel() {
 
 		JPanel contentPanel1 = new JPanel();
 		JPanel jPanel1 = new JPanel();
 
 		welcomeTitle = new JLabel();
 		blankSpace = new JLabel();
 		anotherBlankSpace = new JLabel();
 		yetAnotherBlankSpace1 = new JLabel();
 		yetAnotherBlankSpace2 = new JLabel();
 		yetAnotherBlankSpace3 = new JLabel();
 		yetAnotherBlankSpace4 = new JLabel();
 		yetAnotherBlankSpace5 = new JLabel();
 		yetAnotherBlankSpace6 = new JLabel();
 		yetAnotherBlankSpace7 = new JLabel();
 		yetAnotherBlankSpace8 = new JLabel();
 		yetAnotherBlankSpace9 = new JLabel();
 		yetAnotherBlankSpace10 = new JLabel();
 		yetAnotherBlankSpace11 = new JLabel();
 		yetAnotherBlankSpace12 = new JLabel();
 		yetAnotherBlankSpace13 = new JLabel();
 		
 		jLabel1 = new JLabel();
 		jLabel2 = new JLabel();
 		jLabel9 = new JLabel();
 		noteLabel1 =  new JLabel();
 		noteLabel2 =  new JLabel();
 		noteLabel3 =  new JLabel();
 
 		connectorGroup = new javax.swing.ButtonGroup();
 		oldFileRadioButton = new javax.swing.JRadioButton();
 		newFileRadioButton = new javax.swing.JRadioButton();
 
 		oldFileRadioButton.setActionCommand("annoAndOnto");
 		newFileRadioButton.setActionCommand("oboAndGa");
 		newFileRadioButton.setSelected(true);
 
 		contentPanel1.setLayout(new java.awt.BorderLayout());
 
 		welcomeTitle.setFont(new java.awt.Font("Sans Serif", Font.BOLD, 15));
		welcomeTitle.setText("Welcome to the Gene Ontology Wizard");
 		contentPanel1.add(welcomeTitle, java.awt.BorderLayout.NORTH);
 
 		jPanel1.setLayout(new java.awt.GridLayout(0, 1));
 
 		jPanel1.add(blankSpace);
 
 		jPanel1.add(anotherBlankSpace);
 		jLabel2.setText("Which file format do you want to load?");
 		jPanel1.add(jLabel2);
 		jPanel1.add(yetAnotherBlankSpace5);
 		
 		jPanel1.add(yetAnotherBlankSpace1);
 		jPanel1.add(yetAnotherBlankSpace2);
 		jPanel1.add(yetAnotherBlankSpace6);
 		
 		oldFileRadioButton.setText("Cytoscape BioDataServer (.anno and .onto)");
 		connectorGroup.add(oldFileRadioButton);
 		jPanel1.add(oldFileRadioButton);
 		
 		jPanel1.add(yetAnotherBlankSpace7);
 		
 		newFileRadioButton.setText("Gene Ontology (.obo and gene_association)");
 		connectorGroup.add(newFileRadioButton);
 		jPanel1.add(newFileRadioButton);
 		
 		noteLabel1.setText("Note: Please put all data files (both annotation and ontology)");
		noteLabel2.setText("      in the same directory.  Otherwise, the Gene Ontology Wizard" );
 		noteLabel3.setText("      cannot load the files." );
 
 		
 		jPanel1.add(yetAnotherBlankSpace8);
 		jPanel1.add(yetAnotherBlankSpace9);
 		jPanel1.add(yetAnotherBlankSpace10);
 		jPanel1.add(noteLabel1);
 		jPanel1.add(noteLabel2);
 		jPanel1.add(noteLabel3);
 		//jPanel1.add(yetAnotherBlankSpace11);
 		//jPanel1.add(yetAnotherBlankSpace12);
 		jPanel1.add(yetAnotherBlankSpace13);
 		
 		jLabel9.setText("Press the 'Next' button to continue....");
 		jPanel1.add(jLabel9);
 		//jPanel1.add(yetAnotherBlankSpace3);
 		//jPanel1.add(yetAnotherBlankSpace4);
 		
 		contentPanel1.add(jPanel1, java.awt.BorderLayout.CENTER);
 
 		return contentPanel1;
 
 	}
 
 	public String getFileFormatRadioButtonSelected() {
 		return connectorGroup.getSelection().getActionCommand();
 	}
 
 	private ImageIcon getImageIcon() {
 		// return new ImageIcon((URL)getResource("clouds.jpg"));
 		return null;
 	}
 
 	private Object getResource(String key) {
 
 		URL url = null;
 		String name = key;
 
 		if (name != null) {
 
 			try {
 				Class c = Class
 						.forName("cytoscape.util.swing.BioDataServerWizard");
 				url = c.getResource(name);
 			} catch (ClassNotFoundException cnfe) {
 				System.err.println("Unable to find Parent class");
 			}
 			return url;
 		} else
 			return null;
 	}
 }
