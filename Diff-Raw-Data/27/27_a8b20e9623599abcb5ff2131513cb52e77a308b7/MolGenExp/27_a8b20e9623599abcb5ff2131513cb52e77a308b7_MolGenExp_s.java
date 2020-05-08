 package molGenExp;
 
 import genetics.GeneticsWorkshop;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import match.Blosum50;
 import match.DNAidentity;
 import match.NWSmart;
 import molBiol.Genex;
 import biochem.AminoAcid;
 import biochem.Protex;
 import biochem.StandardTable;
 
 
 public class MolGenExp extends JFrame {
 
 	//indices for tabbed panes
 	private final static int GENETICS = 0;
 	private final static int BIOCHEMISTRY = 1;
 	private final static int MOLECULAR_BIOLOGY = 2;
 
 	private final static String version = "1.0";
 
 	public final static String sampleDNA = 
 		new String("CAGCTATAACCGAGATTGATGTCTAG"
 				+ "TGCGATAAGCCCCAAAGATCGGCACATTTTGTGCGCTATA"
 				+ "CAAAGGTTAGTGGTCTGTCGGCAGTAGTAGGGGGCGT");
 
 	public final static String sampleProtein =
 		new String("MSNRHILLVVCRQ");
 
 
 	private JPanel mainPanel;
 
 	JMenuBar menuBar;
 
 	JMenu fileMenu;
 	JMenuItem quitMenuItem;
 
 	JMenu editMenu;
 	JMenuItem copyUpperToClipboardItem;
 	JMenuItem copyLowerToClipboardItem;	
 
 	JMenu compareMenu;
 	JMenuItem u_lMenuItem;
 	JMenuItem u_sMenuItem;
 	JMenuItem l_sMenuItem;
 	JMenuItem u_cbMenuItem;
 	JMenuItem l_cbMenuItem;
 
 
 	JMenu greenhouseMenu;
 	JMenuItem loadGreenhouseMenuItem;
 	JMenuItem saveGreenhouseMenuItem;
 	JMenuItem saveAsGreenhouseMenuItem;
 	JMenuItem deleteSelectedOrganismMenuItem;
 
 	private JPanel innerPanel;
 
 	private Greenhouse greenhouse;
 	private JButton addToGreenhouseButton;
 
 	JTabbedPane explorerPane;
 	
 	private GeneticsWorkshop gw;
 
 	//for genetics only; the two selected organisms
 	private OrganismAndLocation oal1;
 	private OrganismAndLocation oal2;
 
 	private Protex protex;
 	private Genex genex;
 
 	private File greenhouseDirectory;
 
 	private ColorModel colorModel;
 
 	public MolGenExp() {
 		super("Molecular Genetics Explorer " + version);
 		addWindowListener(new ApplicationCloser());
 		colorModel = new RYBColorModel();
 		setupUI();
 	}
 
 	public static void main(String[] args) {
 		MolGenExp mge = new MolGenExp();
 		mge.pack();
 		mge.setVisible(true);
 	}
 
 	class ApplicationCloser extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 			System.exit(0);
 		}
 	}
 
 	private void setupUI() {
 
 		mainPanel = new JPanel();
 		mainPanel.setLayout(new BorderLayout());
 
 		menuBar = new JMenuBar();
 		menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
 
 		fileMenu = new JMenu("File");
 		quitMenuItem = new JMenuItem("Quit");
 		fileMenu.add(quitMenuItem);
 		menuBar.add(fileMenu);
 
 		editMenu = new JMenu("Edit");
 		copyUpperToClipboardItem = 
 			new JMenuItem("Copy Upper Sequence to Clipboard");
 		editMenu.add(copyUpperToClipboardItem);
 		copyLowerToClipboardItem = 
 			new JMenuItem("Copy Lower Sequence to Clipboard");
 		editMenu.add(copyLowerToClipboardItem);
 		menuBar.add(editMenu);
 		editMenu.setEnabled(false);
 
 		compareMenu = new JMenu("Compare");
 		u_lMenuItem = new JMenuItem("Upper vs. Lower");
 		compareMenu.add(u_lMenuItem);
 		compareMenu.addSeparator();
 		u_sMenuItem = new JMenuItem("Upper vs. Sample");
 		compareMenu.add(u_sMenuItem);
 		l_sMenuItem = new JMenuItem("Lower vs. Sample");
 		compareMenu.add(l_sMenuItem);
 		compareMenu.addSeparator();
 		u_cbMenuItem = new JMenuItem("Upper vs. Clipboard");
 		compareMenu.add(u_cbMenuItem);
 		l_cbMenuItem = new JMenuItem("Lower vs. Clipboard");
 		compareMenu.add(l_cbMenuItem);
 		menuBar.add(compareMenu);
 		compareMenu.setEnabled(false);
 
 		greenhouseMenu = new JMenu("Greenhouse");
 		loadGreenhouseMenuItem = new JMenuItem("Load Greenhouse...");
 		greenhouseMenu.add(loadGreenhouseMenuItem);
 		saveGreenhouseMenuItem = new JMenuItem("Save Greenhouse");
 		greenhouseMenu.add(saveGreenhouseMenuItem);
 		saveAsGreenhouseMenuItem = new JMenuItem("Save Greenhouse As...");
 		greenhouseMenu.add(saveAsGreenhouseMenuItem);
 		greenhouseMenu.addSeparator();
 		deleteSelectedOrganismMenuItem = 
 			new JMenuItem("Delete Selected Organism");
 		greenhouseMenu.add(deleteSelectedOrganismMenuItem);
 		menuBar.add(greenhouseMenu);
 		mainPanel.add(menuBar, BorderLayout.NORTH);
 
 		innerPanel = new JPanel();
 		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));
 
 		explorerPane = new JTabbedPane();
 //		explorerPane.setSize(new Dimension(screenSize.width * 8/10,
 //		screenSize.height * 8/10));
 
 		gw = new GeneticsWorkshop(this);
 		explorerPane.addTab("Genetics", gw);
 		oal1 = null;
 		oal2 = null;
 
 
 		protex = new Protex(this);
 		explorerPane.addTab("Biochemistry", protex);
 
 		genex = new Genex(this);
 		explorerPane.addTab("Molecular Biology", genex);
 
 		innerPanel.add(explorerPane);
 
 		JPanel rightPanel = new JPanel();
 		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
 		rightPanel.add(Box.createRigidArea(new Dimension(100,1)));
		addToGreenhouseButton = new JButton("Add...");
 		addToGreenhouseButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		rightPanel.add(addToGreenhouseButton);
 		addToGreenhouseButton.setEnabled(false);
 
 		greenhouse = new Greenhouse(new DefaultListModel(), this);
 		greenhouse.setSelectionMode(
 				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		rightPanel.setBorder(BorderFactory.createTitledBorder("Greenhouse"));
 		JScrollPane greenhouseScrollPane = new JScrollPane(greenhouse);
 		rightPanel.add(greenhouseScrollPane);
 
 		innerPanel.add(rightPanel);
 
 		mainPanel.add(innerPanel, BorderLayout.CENTER);
 
 		getContentPane().add(mainPanel);
 
 		explorerPane.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				int currentPane = explorerPane.getSelectedIndex();
 				switch (currentPane) {
 				case GENETICS:			
 					clearSelectedOrganisms();
 					compareMenu.setEnabled(false);
 					editMenu.setEnabled(false);
 					addToGreenhouseButton.setEnabled(false);
 					setCustomSelectionSettings();
 					break;
 				case BIOCHEMISTRY:			
 					clearSelectedOrganisms();
 					compareMenu.setEnabled(true);
 					editMenu.setEnabled(true);
 					addToGreenhouseButton.setEnabled(false);
 					setDefaultSelectionSettings();
 					break;
 				case MOLECULAR_BIOLOGY:			
 					clearSelectedOrganisms();
 					compareMenu.setEnabled(true);
 					editMenu.setEnabled(true);
 					addToGreenhouseButton.setEnabled(true);
 					setDefaultSelectionSettings();
 					break;
 				}
 			}
 
 		});
 
 		//make a greenhouse directory if one doesn't exist
 		//  if one exists, load contents
 		greenhouseDirectory = new File("Greenhouse");
 		if(!greenhouseDirectory.exists() 
 				|| !greenhouseDirectory.isDirectory()) {
 			boolean success = greenhouseDirectory.mkdir();
 			if (!success) {
 				JOptionPane.showMessageDialog(
 						this, 
 						"Cannot create Greenhouse Folder",
 						"File System Error", 
 						JOptionPane.WARNING_MESSAGE);
 				System.exit(0);
 			}
 		} else {
 			loadGreenhouse(greenhouseDirectory);
 		}
 
 		quitMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}
 		});
 
 		copyUpperToClipboardItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String selectedPane = 
 					explorerPane.getSelectedComponent().getClass().toString();
 				if (selectedPane.equals("class molBiol.Genex")) {
 					Clipboard c = 
 						Toolkit.getDefaultToolkit().getSystemClipboard();
 					StringSelection s = 
 						new StringSelection(genex.getUpperGEW().getDNA());
 					c.setContents(s, null);
 					return;
 				}
 				if (selectedPane.equals("class biochem.Protex")) {
 					Clipboard c = 
 						Toolkit.getDefaultToolkit().getSystemClipboard();
 					StringSelection s = 
 						new StringSelection(
 								protex.getUpperFoldingWindow().getAaSeq());
 					c.setContents(s, null);
 					return;
 				}
 			}
 		});
 
 		copyLowerToClipboardItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String selectedPane = 
 					explorerPane.getSelectedComponent().getClass().toString();
 				if (selectedPane.equals("class molBiol.Genex")) {
 					Clipboard c = 
 						Toolkit.getDefaultToolkit().getSystemClipboard();
 					StringSelection s = 
 						new StringSelection(genex.getLowerGEW().getDNA());
 					c.setContents(s, null);
 					return;
 				}
 				if (selectedPane.equals("class biochem.Protex")) {
 					Clipboard c = 
 						Toolkit.getDefaultToolkit().getSystemClipboard();
 					StringSelection s = 
 						new StringSelection(
 								protex.getLowerFoldingWindow().getAaSeq());
 					c.setContents(s, null);
 					return;
 				}
 			}
 		});
 
 		// the compare menu items
 		u_lMenuItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				String selectedPane = 
 					explorerPane.getSelectedComponent().getClass().toString();
 				if (selectedPane.equals("class molBiol.Genex")) {
 					DNASequenceComparator dsc = getDNASequences();
 					if (dsc != null) {
 						dsc.compareSequences(SequenceComparator.UPPER, 
 								SequenceComparator.LOWER);
 					}
 					return;
 				}
 				if (selectedPane.equals("class biochem.Protex")) {
 					ProteinSequenceComparator psc = getProteinSequences();
 					if (psc != null) {
 						psc.compareSequences(SequenceComparator.UPPER, 
 								SequenceComparator.LOWER);
 					}
 					return;
 				}
 			}
 		});
 
 		u_sMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String selectedPane = 
 					explorerPane.getSelectedComponent().getClass().toString();
 				if (selectedPane.equals("class molBiol.Genex")) {
 					DNASequenceComparator dsc = getDNASequences();
 					if (dsc != null) {
 						dsc.compareSequences(SequenceComparator.UPPER, 
 								SequenceComparator.SAMPLE);
 					}
 					return;
 				}
 				if (selectedPane.equals("class biochem.Protex")) {
 					ProteinSequenceComparator psc = getProteinSequences();
 					if (psc != null) {
 						psc.compareSequences(SequenceComparator.UPPER, 
 								SequenceComparator.SAMPLE);
 					}
 					return;
 				}
 			}
 		});
 
 		l_sMenuItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				String selectedPane = 
 					explorerPane.getSelectedComponent().getClass().toString();
 				if (selectedPane.equals("class molBiol.Genex")) {
 					DNASequenceComparator dsc = getDNASequences();
 					if (dsc != null) {
 						dsc.compareSequences(SequenceComparator.LOWER, 
 								SequenceComparator.SAMPLE);
 					}
 					return;
 				}
 				if (selectedPane.equals("class biochem.Protex")) {
 					ProteinSequenceComparator psc = getProteinSequences();
 					if (psc != null) {
 						psc.compareSequences(SequenceComparator.LOWER, 
 								SequenceComparator.SAMPLE);
 					}
 					return;
 				}
 			}
 		});
 
 		u_cbMenuItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				String selectedPane = 
 					explorerPane.getSelectedComponent().getClass().toString();
 				if (selectedPane.equals("class molBiol.Genex")) {
 					DNASequenceComparator dsc = getDNASequences();
 					if (dsc != null) {
 						dsc.compareSequences(SequenceComparator.UPPER, 
 								SequenceComparator.CLIPBOARD);
 					}
 					return;
 				}
 				if (selectedPane.equals("class biochem.Protex")) {
 					ProteinSequenceComparator psc = getProteinSequences();
 					if (psc != null) {
 						psc.compareSequences(SequenceComparator.UPPER, 
 								SequenceComparator.CLIPBOARD);
 					}
 					return;
 				}
 			}
 		});
 
 		l_cbMenuItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				String selectedPane = 
 					explorerPane.getSelectedComponent().getClass().toString();
 				if (selectedPane.equals("class molBiol.Genex")) {
 					DNASequenceComparator dsc = getDNASequences();
 					if (dsc != null) {
 						dsc.compareSequences(SequenceComparator.LOWER, 
 								SequenceComparator.CLIPBOARD);
 					}
 					return;
 				}
 				if (selectedPane.equals("class biochem.Protex")) {
 					ProteinSequenceComparator psc = getProteinSequences();
 					if (psc != null) {
 						psc.compareSequences(SequenceComparator.LOWER, 
 								SequenceComparator.CLIPBOARD);
 					}
 					return;
 				}
 			}
 		});
 
 
 
 		loadGreenhouseMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				loadGreenhouseFromChosenFolder();
 			}
 		});
 
 		saveGreenhouseMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Object[] all = greenhouse.getAll();
 				if (all.length == 0) {
 					JOptionPane.showMessageDialog(
 							null, 
 							"No Organisms in Greenhouse to Save",
 							"Empty Greenhouse", 
 							JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 
 				//save it
 				if (greenhouseDirectory != null) {
 					saveToFolder(all);
 				} else {
 					saveToChosenFolder(all);
 				}
 			}
 		});
 
 		saveAsGreenhouseMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Object[] all = greenhouse.getAll();
 				if (all.length == 0) {
 					JOptionPane.showMessageDialog(
 							null, 
 							"No Organisms in Greenhouse to Save",
 							"Empty Greenhouse", 
 							JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				saveToChosenFolder(all)	;
 			}
 		});
 
 		deleteSelectedOrganismMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				greenhouse.deleteSelected();
 				clearSelectedOrganisms();
 			}
 		});
 
 		addToGreenhouseButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				saveSelectedOrganismToGreenhouse();
 			}
 		});
 
 	}
 
 	private DNASequenceComparator getDNASequences() {
 		String clipSeq = "";
 		Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
 		Transferable contents = c.getContents(null);
 		if ((contents != null) && 
 				(contents.isDataFlavorSupported(DataFlavor.stringFlavor))) {
 			try {
 				clipSeq = (String)contents.getTransferData(DataFlavor.stringFlavor);
 			} catch (Exception e) {
 				clipSeq = "";
 			}
 
 			// if not a DNA sequence
 			Pattern p = Pattern.compile("[^AGCT]+");
 			if (p.matcher(clipSeq).find()) {
 				clipSeq = "";
 			}
 		}
 		return new DNASequenceComparator(genex.getUpperGEW().getDNA(),
 				genex.getLowerGEW().getDNA(),
 				sampleDNA,
 				clipSeq);
 	}
 
 	private ProteinSequenceComparator getProteinSequences() {
 		String clipSeq = "";
 		Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
 		Transferable contents = c.getContents(null);
 		if ((contents != null) && 
 				(contents.isDataFlavorSupported(DataFlavor.stringFlavor))) {
 			try {
 				clipSeq = (String)contents.getTransferData(DataFlavor.stringFlavor);
 			} catch (Exception e) {
 				clipSeq = "";
 			}
 
 			//try to see if it's a 3-letter code sequence
 			if (clipSeq.indexOf(" ") != -1) {
 				clipSeq = convert3LetterTo1Letter(clipSeq);
 				if (clipSeq == null) {
 					clipSeq = "";
 				}
 			}
 			//maybe it's a single-letter aa seq
 			Pattern p = Pattern.compile("[^ARNDCQEGHILKMFPSTWYV]+");
 			if (p.matcher(clipSeq).find()) {
 				clipSeq ="";
 			} 
 		}
 		return new ProteinSequenceComparator(
 				convert3LetterTo1Letter(
 						protex.getUpperFoldingWindow().getAaSeq()),
 						convert3LetterTo1Letter(
 								protex.getLowerFoldingWindow().getAaSeq()),
 								sampleProtein,
 								clipSeq);
 
 	}
 
 	public String convert3LetterTo1Letter(String aaSeq) {
 		StandardTable table = new StandardTable();
 		StringBuffer abAASeq = new StringBuffer();
 		StringTokenizer st = new StringTokenizer(aaSeq);
 		while (st.hasMoreTokens()){
 			AminoAcid aa = table.get(st.nextToken());
 			//if it can't be parsed as an amino acid, abort
 			if (aa == null) {
 				return null;
 			}
 			abAASeq.append(aa.getAbName());
 		}
 		return abAASeq.toString();
 	}
 
 	public ColorModel getOverallColorModel() {
 		return colorModel;
 	}
 
 	public Genex getGenex() {
 		return genex;
 	}
 
 	public Protex getProtex() {
 		return protex;
 	}
 
 	public GeneticsWorkshop getGeneticsWorkshop() {
 		return gw;
 	}
 
 	public Greenhouse getGreenhouse() {
 		return greenhouse;
 	}
 
 	public void saveToGreenhouse(Organism o) {
 		greenhouse.add(o);
 	}
 
 	public void loadOrganismIntoActivePanel(Organism o) {
 		String selectedPane = 
 			explorerPane.getSelectedComponent().getClass().toString();
 
 		if (selectedPane.equals("class molBiol.Genex")) {
 			genex.loadOrganism(o);
 		}
 
 		if (selectedPane.equals("class biochem.Protex")) {
 			protex.loadOrganism(o);
 		}
 	}
 
 	public void loadGreenhouseFromChosenFolder() {
 		clearSelectedOrganisms();
 		JFileChooser inFolderChooser = new JFileChooser();
 		inFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		int returnVal = inFolderChooser.showOpenDialog(this);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			greenhouseDirectory = inFolderChooser.getSelectedFile();
 			loadGreenhouse(greenhouseDirectory);
 		}
 	}
 
 	public void saveToChosenFolder(Object[] all) {
 		JFileChooser outFolderChooser = new JFileChooser();
 		outFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		int returnVal = outFolderChooser.showSaveDialog(this);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			greenhouseDirectory = outFolderChooser.getSelectedFile();
 			saveToFolder(all);
 		}
 	}
 
 	public void saveToFolder(Object[] all) {
 		//first, clear out all the old organims
 		String[] oldOrganisms = greenhouseDirectory.list();
 		for (int i = 0; i < oldOrganisms.length; i++) {
 			String name = oldOrganisms[i];
 			if (name.indexOf(".organism") != -1) {
 				File f = new File(greenhouseDirectory, name);
 				f.delete();
 			}
 		}
 
 		for (int i = 0; i < all.length; i++) {
 			Organism o = (Organism)all[i];
 			String name = o.getName();
 			String fileName = greenhouseDirectory.toString() 
 			+ System.getProperty("file.separator") 
 			+ name
 			+ ".organism";
 			try {
 				FileOutputStream f =
 					new FileOutputStream(fileName);
 				ObjectOutput s = new ObjectOutputStream(f);
 				s.writeObject(o);
 				s.flush();
 				s.close();
 				f.close();
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void loadGreenhouse(File greenhouseDir) {
 		clearSelectedOrganisms();
 		greenhouse.clearList();
 		String[] files = greenhouseDir.list();
 		for (int i = 0; i < files.length; i++){
 			String fileString = files[i];
 			if (fileString.endsWith(".organism")) {
 				String organismName = 
 					fileString.replaceAll(".organism", "");
 				String orgFileName = greenhouseDirectory.toString() 
 				+ System.getProperty("file.separator") 
 				+ fileString;
 				try {
 					FileInputStream in = new FileInputStream(orgFileName);
 					ObjectInputStream s = new ObjectInputStream(in);
 					Organism o = (Organism) s.readObject();
 					o.setName(organismName);
 					greenhouse.add(o);
 					s.close();
 					in.close();
 				} 
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		greenhouse.revalidate();
 		greenhouse.repaint();
 	}
 
 	public void saveSelectedOrganismToGreenhouse() {
 		int currentPane = explorerPane.getSelectedIndex();
 		switch (currentPane) {
 		case GENETICS:
 			//should not happen - only works if one org selected
 			if ((oal1 == null) || (oal2 != null)) {
 				return;
 			}
 			saveOrganismToGreenhouse(oal1.getOrganism());
 			break;
 
 		case BIOCHEMISTRY:
 			// should not happen - button should be disabled
 			break;
 
 		case MOLECULAR_BIOLOGY:
 			//need to express and fold the proteins
 			genex.saveOrganismToGreenhouse();
 			break;
 		}
 
 	}
 
 	public void saveOrganismToGreenhouse(Organism o) {
 
 		String name = "";
 		String warning = "";
 		Pattern p = Pattern.compile("[^A-Za-z0-9\\_\\-]+");
 		while (name.equals("") || 
 				p.matcher(name).find() ||
 				greenhouse.nameExistsAlready(name)){
 			name = JOptionPane.showInputDialog(
 					this,
 					warning +
 					"Give a unique name for your new organism.\n"
 					+ "This can only include letters, numbers, -, and "
 					+ "_.",
 					"Name your organism.",
 					JOptionPane.PLAIN_MESSAGE);
 			if (name == null) {
 				return;
 			}
 
 			if(greenhouse.nameExistsAlready(name)) {
 				warning = "<html><font color=red>"
 					+ "The name you entered exists already,"
 					+ " please cancel or try again.</font>\n";
 			} else {
 				warning = "<html><font color=red>"
 					+ "The name you entered was not allowed," 
 					+ " please cancel or try again.</font>\n";
 			}
 		}
 		saveToGreenhouse(new Organism(name,o));
 		clearSelectedOrganisms();
 	}
 
 
 	//handlers for selections of creatures in Genetics mode
 	//  max of two at a time.
 	//these are called by the CustomListSelectionMode
 	public void deselectOrganism(OrganismAndLocation oal) {
 
 		// only do this in genetics
 		if (explorerPane.getSelectedIndex() != GENETICS) {
 			return;
 		}
 
 		//remove from list of selected organisms
 		//if #1 is being deleted, delete it and move #2 up
 		if ((oal.getOrganism()).equals(oal1.getOrganism())) {
 			oal1 = oal2;
 			oal2 = null;
 			updateGeneticsButtonStatus();
 			return;
 		}
 
 		//otherwise just delete #2
 		if ((oal.getOrganism()).equals(oal2.getOrganism())) {
 			oal2 = null;
 			updateGeneticsButtonStatus();
 			return;
 		}
 
 		//should not get to here
 		updateGeneticsButtonStatus();
 		return;
 	}
 
 	public void addSelectedOrganism(OrganismAndLocation oal) {
 
 		// only do this in genetics
 		if (explorerPane.getSelectedIndex() != GENETICS) {
 			return;
 		}
 
 		//if none selected so far, put it in #1
 		if ((oal1 == null) && (oal2 == null)) {
 			oal1 = oal;
 			updateGeneticsButtonStatus();
 			return;
 		}
 
 		// if only one selected so far, it should be in #1
 		// so move #1 to #2 and put this in #1
 		if ((oal1 != null) && (oal2 == null)) {
 			oal2 = oal1;
 			oal1 = oal;
 			updateGeneticsButtonStatus();
 			return;
 		}
 
 		//it must be that there are 2 selected orgs
 		// so you have to drop #2, move 1 to 2 and put the
 		// new one in 1.
 		if ((oal1 != null) && (oal2 != null)) {
 			//drop #2
 			oal2.getListLocation().removeSelectionIntervalDirectly(oal2);
 			//move 1 to 2
 			oal2 = oal1;
 			//put new one in 1
 			oal1 = oal;
 			updateGeneticsButtonStatus();
 		}
 
 		//should not get to here
 		return;
 	}
 
 	public void clearSelectedOrganisms() {
 		if (oal1 != null) {
 			oal1.getListLocation().removeSelectionIntervalDirectly(oal1);
 		}
 
 		if (oal2 != null) {
 			oal2.getListLocation().removeSelectionIntervalDirectly(oal2);
 		}
 
 		oal1 = null;
 		oal2 = null;
 		updateGeneticsButtonStatus();
 	}
 
 	public Organism getOrg1() {
 		return oal1.getOrganism();
 	}
 
 	public Organism getOrg2() {
 		return oal2.getOrganism();
 	}
 
 
 	//if no orgs selected - no buttons active;
 	// if only one - save to greenhouse, mutate, and self are active;
 	// if two - cross only
 	public void updateGeneticsButtonStatus() {
 		int numSelectedOrgs = 0;
 
 		if (oal1 != null) {
 			numSelectedOrgs++;
 		}
 
 		if (oal2 != null) {
 			numSelectedOrgs++;
 		}
 
 		switch (numSelectedOrgs) {
 		case 0:
 			addToGreenhouseButton.setEnabled(false);
 			gw.setCrossTwoButtonsEnabled(false);
 			gw.setSelfCrossButtonsEnabled(false);
 			gw.setMutateButtonsEnabled(false);
 			break;
 
 		case 1:
 			addToGreenhouseButton.setEnabled(true);
 			gw.setCrossTwoButtonsEnabled(false);
 			gw.setSelfCrossButtonsEnabled(true);
 			gw.setMutateButtonsEnabled(true);
 			break;
 
 		case 2:
 			addToGreenhouseButton.setEnabled(false);
 			gw.setCrossTwoButtonsEnabled(true);
 			gw.setSelfCrossButtonsEnabled(false);
 			gw.setMutateButtonsEnabled(false);
 			break;
 
 		}
 	}
 	
 	public void setDefaultSelectionSettings() {
 		greenhouse.setDefaultSelectionSettings();
 		gw.setDefaultSelectionSettings();
 	}
 	
 	public void setCustomSelectionSettings() {
 		greenhouse.setCustomSelectionSettings();
 		gw.setCustomSelectionSettings();
 	}
 }
