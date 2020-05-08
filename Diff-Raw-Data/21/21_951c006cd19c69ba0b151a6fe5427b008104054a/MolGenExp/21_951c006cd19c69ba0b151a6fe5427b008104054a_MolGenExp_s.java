 package molGenExp;
 
 import evolution.EvolutionWorkArea;
 import genetics.GeneticsWorkbench;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.Timer;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import molBiol.MolBiolWorkbench;
 import molBiol.MolBiolWorkpanel;
 import preferences.GlobalDefaults;
 import preferences.MGEPreferences;
 import preferences.PreferencesDialog;
 import biochem.AminoAcid;
 import biochem.BiochemistryWorkbench;
 import biochem.BiochemistryWorkpanel;
 import biochem.FoldedProteinArchive;
 import biochem.FoldingException;
 import biochem.PaintedInACornerFoldingException;
 import biochem.StandardTable;
 
 
 public class MolGenExp extends JFrame {
 
 	//indices for tabbed panes
 	private final static int GENETICS = 0;
 	private final static int BIOCHEMISTRY = 1;
 	private final static int MOLECULAR_BIOLOGY = 2;
 	private final static int EVOLUTION = 3;
 
 	private JPanel mainPanel;
 
 	JMenuBar menuBar;
 
 	JMenu fileMenu;
 	JMenuItem prefsItem;
 	JMenuItem dumpWorldItem;
 	JMenuItem loadWorldItem;
 	JMenuItem savePicOfUpperPanelItem;
 	JMenuItem savePicOfLowerPanelItem;
 	JMenuItem quitMenuItem;
 
 	JMenu editMenu;
 	JMenuItem copyUpperSequenceToClipboardItem;
 	JMenuItem copyLowerSequenceToClipboardItem;	
 	JMenuItem copyUpperImageToClipboardItem;
 	JMenuItem copyLowerImageToClipboardItem;
 
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
 
 	private File greenhouseDirectory;
 	private GreenhouseLoader greenhouseLoader;
 	private Timer greenhouseLoaderTimer;
 	private Greenhouse greenhouse;
 	private JButton addToGreenhouseButton;
 
 	private JProgressBar progressBar;
 	private JLabel statusLabel;
 	private JLabel foldingStatsLabel;
 
 	JTabbedPane explorerPane;
 
 	private GeneticsWorkbench geneticsWorkbench;
 
 	private OrganismFactory organismFactory;
 
 	//for genetics only; the two selected organisms
 	private OrganismAndLocation oal1;
 	private OrganismAndLocation oal2;
 
 	private BiochemistryWorkbench biochemistryWorkbench;
 	private MolBiolWorkbench molBiolWorkbench;
 	private EvolutionWorkArea evolutionWorkArea;
 
 	private PreferencesDialog preferencesDialog;
 
 	public MolGenExp() {
 		super("Aipotu " + GlobalDefaults.version);
 		setupUI();
 		addWindowListener(new ApplicationCloser());
 	}
 
 	public static void main(String[] args) {
 		MolGenExp mge = new MolGenExp();
 		mge.pack();
 		mge.setVisible(true);
 	}
 
 	class ApplicationCloser extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 			FoldedProteinArchive.getInstance().saveArchiveToZipFile();
 			System.exit(0);
 		}
 	}
 
 	private void setupUI() {
 
 		mainPanel = new JPanel();
 		mainPanel.setLayout(new BorderLayout());
 
 		menuBar = new JMenuBar();
 		menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
 
 		fileMenu = new JMenu("File");
 		prefsItem = new JMenuItem("Preferences...");
 		dumpWorldItem = new JMenuItem("Save World to file...");
 		loadWorldItem = new JMenuItem("Load World from file...");
 		savePicOfUpperPanelItem = new JMenuItem("Save Image of Upper Panel...");
 		savePicOfLowerPanelItem = new JMenuItem("Save Image of Lower Panel...");		
 		quitMenuItem = new JMenuItem("Quit");
 		fileMenu.add(savePicOfUpperPanelItem);
 		fileMenu.add(savePicOfLowerPanelItem);
 		fileMenu.addSeparator();
 		fileMenu.add(prefsItem);
 		fileMenu.add(dumpWorldItem);
 		dumpWorldItem.setEnabled(false);
 		fileMenu.add(loadWorldItem);
 		loadWorldItem.setEnabled(false);
 		fileMenu.add(quitMenuItem);
 		menuBar.add(fileMenu);
 
 		editMenu = new JMenu("Edit");
 		copyUpperSequenceToClipboardItem = 
 			new JMenuItem("Copy Upper Sequence to Clipboard");
 		editMenu.add(copyUpperSequenceToClipboardItem);
 		copyLowerSequenceToClipboardItem = 
 			new JMenuItem("Copy Lower Sequence to Clipboard");
 		editMenu.add(copyLowerSequenceToClipboardItem);
 		editMenu.addSeparator();
 		copyUpperImageToClipboardItem = 
 			new JMenuItem("Copy Image of Upper Panel to Clipboard");
 		editMenu.add(copyUpperImageToClipboardItem);
 		copyLowerImageToClipboardItem = 
 			new JMenuItem("Copy Image of Lower Panel to Clipboard");
 		editMenu.add(copyLowerImageToClipboardItem);
 		menuBar.add(editMenu);
 		turnOffSequenceClipboardItems();
 
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
 
 		addToGreenhouseButton = new JButton("Add...");
 
 		geneticsWorkbench = new GeneticsWorkbench(this);
 		explorerPane.addTab("Genetics", geneticsWorkbench);
 		oal1 = null;
 		oal2 = null;
 
 		organismFactory = new OrganismFactory();
 
 		biochemistryWorkbench = new BiochemistryWorkbench(this);
 		explorerPane.addTab("Biochemistry", biochemistryWorkbench);
 
 		molBiolWorkbench = new MolBiolWorkbench(this);
 		explorerPane.addTab("Molecular Biology", molBiolWorkbench);
 
 		evolutionWorkArea = new EvolutionWorkArea(this);
 		explorerPane.addTab("Evolution", evolutionWorkArea);
 
 		innerPanel.add(explorerPane);
 
 		JPanel rightPanel = new JPanel();
 		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
 		rightPanel.setMaximumSize(new Dimension(120,1000));
 		rightPanel.add(Box.createRigidArea(new Dimension(100,1)));
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
 
 		JPanel statusPanel = new JPanel();
 		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
 		statusPanel.add(Box.createHorizontalStrut(10));
 		progressBar = new JProgressBar();
 		progressBar.setMaximumSize(new Dimension(200,20));
 		statusPanel.add(progressBar);
 		statusPanel.add(Box.createHorizontalStrut(20));
 		statusLabel = new JLabel("Welcome");
 		statusPanel.add(statusLabel);
 		statusPanel.add(Box.createHorizontalGlue());
 		foldingStatsLabel = new JLabel("No sequences folded yet");
 		statusPanel.add(foldingStatsLabel);
 		foldingStatsLabel.setToolTipText("<html>Certain protein sequences cannot be folded properly<br>"
 				+ "with the 2-dimensional algorithm Aipotu uses.<br>These sequences are "
				+ "automatically replaced with new sequences as needed.</html>");
 		statusPanel.add(Box.createHorizontalStrut(20));
 
 		mainPanel.add(statusPanel, BorderLayout.SOUTH);
 
 		setPreferredSize(new Dimension(1100,800));
 		getContentPane().add(mainPanel);
 
 		preferencesDialog = new PreferencesDialog(this);
 
 
 		explorerPane.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				int currentPane = explorerPane.getSelectedIndex();
 				switch (currentPane) {
 				case GENETICS:			
 					clearSelectedOrganisms();
 					compareMenu.setEnabled(false);
 					turnOffSequenceClipboardItems();
 					savePicOfLowerPanelItem.setEnabled(true);
 					savePicOfUpperPanelItem.setText("Save Image of Upper Panel...");
 					copyLowerImageToClipboardItem.setEnabled(true);
 					copyUpperImageToClipboardItem.setText(
 					"Copy Image of Upper Panel to Clipboard");
 					addToGreenhouseButton.setEnabled(false);
 					dumpWorldItem.setEnabled(false);
 					loadWorldItem.setEnabled(false);
 					setCustomSelectionSettings();
 					break;
 				case BIOCHEMISTRY:			
 					clearSelectedOrganisms();
 					compareMenu.setEnabled(true);
 					turnOnSequenceClipboardItems();
 					savePicOfLowerPanelItem.setEnabled(true);
 					savePicOfUpperPanelItem.setText("Save Image of Upper Panel...");
 					copyLowerImageToClipboardItem.setEnabled(true);
 					copyUpperImageToClipboardItem.setText(
 					"Copy Image of Upper Panel to Clipboard");
 					addToGreenhouseButton.setEnabled(false);
 					dumpWorldItem.setEnabled(false);
 					loadWorldItem.setEnabled(false);
 					setDefaultSelectionSettings();
 					break;
 				case MOLECULAR_BIOLOGY:			
 					clearSelectedOrganisms();
 					compareMenu.setEnabled(true);
 					turnOnSequenceClipboardItems();
 					savePicOfLowerPanelItem.setEnabled(true);
 					savePicOfUpperPanelItem.setText("Save Image of Upper Panel...");
 					copyLowerImageToClipboardItem.setEnabled(true);
 					copyUpperImageToClipboardItem.setText(
 					"Copy Image of Upper Panel to Clipboard");
 					addToGreenhouseButton.setEnabled(true);
 					dumpWorldItem.setEnabled(false);
 					loadWorldItem.setEnabled(false);
 					setDefaultSelectionSettings();
 					break;
 				case EVOLUTION:
 					clearSelectedOrganisms();
 					evolutionWorkArea.clearSelection();
 					compareMenu.setEnabled(false);
 					turnOffSequenceClipboardItems();
 					savePicOfLowerPanelItem.setEnabled(false);
 					savePicOfUpperPanelItem.setText("Save Image of Panel...");
 					copyUpperImageToClipboardItem.setText(
 					"Copy Image of Panel to Clipboard");
 					copyLowerImageToClipboardItem.setEnabled(false);
 					addToGreenhouseButton.setEnabled(true);
 					dumpWorldItem.setEnabled(true);
 					loadWorldItem.setEnabled(true);
 					setEvolutionSelectionSettings();
 					break;
 				}
 			}
 		});
 
 		savePicOfUpperPanelItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (explorerPane.getSelectedIndex() == EVOLUTION) {
 					saveSnapshotToFile(evolutionWorkArea.takeSnapshot());
 				} else {
 					saveSnapshotToFile(((Workbench)explorerPane
 							.getSelectedComponent())
 							.getUpperPanel().takeSnapshot());
 				}
 			}
 		});
 
 		savePicOfLowerPanelItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				saveSnapshotToFile((
 						((Workbench)explorerPane.getSelectedComponent())
 						.getLowerPanel().takeSnapshot()));
 			}			
 		});
 
 
 		//make a greenhouse directory if one doesn't exist
 		//  if one exists, load contents
 		greenhouseLoaderTimer = new Timer(100, new GrenhouseLoaderTimerListener());	//timer for greenhouse loading progress bar
 		greenhouseDirectory = new File(GlobalDefaults.greenhouseDirName);
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
 			try {
 				loadGreenhouse(greenhouseDirectory);
 			} catch (FoldingException e1) {
 				JOptionPane.showMessageDialog(null, 
 						GlobalDefaults.paintedInACornerNotice,
 						"Folding Error", JOptionPane.WARNING_MESSAGE);
 			}
 		}
 
 		quitMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				FoldedProteinArchive.getInstance().saveArchiveToZipFile();
 				System.exit(0);
 			}
 		});
 
 		prefsItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				preferencesDialog.setVisible(true);
 			}
 		});
 
 		dumpWorldItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				evolutionWorkArea.saveWorldToFile();
 			}
 		});
 
 		loadWorldItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				evolutionWorkArea.loadWorldFromFile();
 			}
 		});
 
 		copyUpperSequenceToClipboardItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String selectedPane = 
 					explorerPane.getSelectedComponent().getClass().toString();
 				if (selectedPane.equals("class molBiol.MolBiolWorkbench")) {
 					Clipboard c = 
 						Toolkit.getDefaultToolkit().getSystemClipboard();
 					StringSelection s = 
 						new StringSelection(((MolBiolWorkpanel)molBiolWorkbench.getUpperPanel()).getDNA());
 					c.setContents(s, null);
 					return;
 				}
 				if (selectedPane.equals("class biochem.BiochemistryWorkbench")) {
 					Clipboard c = 
 						Toolkit.getDefaultToolkit().getSystemClipboard();
 					StringSelection s = 
 						new StringSelection(
 								((BiochemistryWorkpanel)(biochemistryWorkbench.getUpperPanel())).getAaSeq());
 					c.setContents(s, null);
 					return;
 				}
 			}
 		});
 
 		copyLowerSequenceToClipboardItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String selectedPane = 
 					explorerPane.getSelectedComponent().getClass().toString();
 				if (selectedPane.equals("class molBiol.MolBiolWorkbench")) {
 					Clipboard c = 
 						Toolkit.getDefaultToolkit().getSystemClipboard();
 					StringSelection s = 
 						new StringSelection(((MolBiolWorkpanel)molBiolWorkbench.getLowerPanel()).getDNA());
 					c.setContents(s, null);
 					return;
 				}
 				if (selectedPane.equals("class biochem.BiochemistryWorkbench")) {
 					Clipboard c = 
 						Toolkit.getDefaultToolkit().getSystemClipboard();
 					StringSelection s = 
 						new StringSelection(
 								((BiochemistryWorkpanel)(biochemistryWorkbench
 										.getLowerPanel())).getAaSeq());
 					c.setContents(s, null);
 					return;
 				}
 			}
 		});
 
 		copyUpperImageToClipboardItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ImageForClipboard ifc = null;
 				if (explorerPane.getSelectedIndex() == EVOLUTION) {
 					ifc = new ImageForClipboard(evolutionWorkArea.takeSnapshot());
 				} else {
 					ifc = new ImageForClipboard(
 							((Workbench)explorerPane
 									.getSelectedComponent())
 									.getUpperPanel().takeSnapshot());
 				}
 				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ifc, null);
 			}
 		});
 
 		copyLowerImageToClipboardItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ImageForClipboard ifc = new ImageForClipboard((
 						((Workbench)explorerPane.getSelectedComponent())
 						.getLowerPanel().takeSnapshot()));
 				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ifc, null);
 			}			
 		});
 
 		// the compare menu items
 		u_lMenuItem.addActionListener(new SequenceComparatorMenuItemListener(
 				this, 
 				SequenceComparator.UPPER,
 				SequenceComparator.LOWER));
 
 		u_sMenuItem.addActionListener(new SequenceComparatorMenuItemListener(
 				this, 
 				SequenceComparator.UPPER,
 				SequenceComparator.SAMPLE));
 
 		l_sMenuItem.addActionListener(new SequenceComparatorMenuItemListener(
 				this, 
 				SequenceComparator.LOWER,
 				SequenceComparator.SAMPLE));
 
 		u_cbMenuItem.addActionListener(new SequenceComparatorMenuItemListener(
 				this, 
 				SequenceComparator.UPPER,
 				SequenceComparator.CLIPBOARD));
 
 		l_cbMenuItem.addActionListener(new SequenceComparatorMenuItemListener(
 				this, 
 				SequenceComparator.LOWER,
 				SequenceComparator.CLIPBOARD));
 
 		loadGreenhouseMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					loadGreenhouseFromChosenFolder();
 				} catch (FoldingException e1) {
 					JOptionPane.showMessageDialog(null, 
 							GlobalDefaults.paintedInACornerNotice,
 							"Folding Error", JOptionPane.WARNING_MESSAGE);
 				}
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
 
 				//save changes to disk
 				Object[] all = greenhouse.getAll();
 				if (all.length == 0) return;
 
 				if (greenhouseDirectory != null) {
 					saveToFolder(all);
 				} else {
 					saveToChosenFolder(all);
 				}
 			}
 		});
 
 		addToGreenhouseButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					saveSelectedOrganismToGreenhouse();
 					//save changes to disk
 					Object[] all = greenhouse.getAll();
 					if (all.length == 0) return;
 
 					if (greenhouseDirectory != null) {
 						saveToFolder(all);
 					} else {
 						saveToChosenFolder(all);
 					}
 				} catch (FoldingException e) {
 					JOptionPane.showMessageDialog(null, 
 							GlobalDefaults.paintedInACornerNotice,
 							"Folding Error", JOptionPane.WARNING_MESSAGE);
 				}
 			}
 		});
 
 	}
 
 	public DNASequenceComparator getDNASequences() {
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
 		return new DNASequenceComparator(((MolBiolWorkpanel)molBiolWorkbench.getUpperPanel()).getDNA(),
 				((MolBiolWorkpanel)molBiolWorkbench.getLowerPanel()).getDNA(),
 				GlobalDefaults.sampleDNA,
 				clipSeq);
 	}
 
 	public ProteinSequenceComparator getProteinSequences() {
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
 						((BiochemistryWorkpanel)
 								(biochemistryWorkbench.getUpperPanel())).getAaSeq()),
 								convert3LetterTo1Letter(
 										((BiochemistryWorkpanel)
 												(biochemistryWorkbench.getLowerPanel())).getAaSeq()),
 												GlobalDefaults.sampleProtein,
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
 
 	public MolBiolWorkbench getMolBiolWorkbench() {
 		return molBiolWorkbench;
 	}
 
 	public BiochemistryWorkbench getBiochemistryWorkbench() {
 		return biochemistryWorkbench;
 	}
 
 	public GeneticsWorkbench getGeneticsWorkbench() {
 		return geneticsWorkbench;
 	}
 
 	public Greenhouse getGreenhouse() {
 		return greenhouse;
 	}
 
 	public String getCurrentWorkingPanel() {
 		return explorerPane.getSelectedComponent().getClass().toString();
 	}
 
 	public void saveToGreenhouse(Organism o) {
 		greenhouse.add(o);
 	}
 
 	public String getGreenhouseDirectory() {
 		return greenhouseDirectory.toString();
 	}
 
 	public EvolutionWorkArea getEvolutionWorkArea() {
 		return evolutionWorkArea;
 	}
 
 	public void loadOrganismIntoActivePanel(Organism o) 
 	throws PaintedInACornerFoldingException {
 		String selectedPane = 
 			explorerPane.getSelectedComponent().getClass().toString();
 
 		if (selectedPane.equals("class molBiol.MolBiolWorkbench")) {
 			molBiolWorkbench.loadOrganism(o);
 		}
 
 		if (selectedPane.equals("class biochem.BiochemistryWorkbench")) {
 			biochemistryWorkbench.loadOrganism(o);
 		}
 	}
 
 	public void loadGreenhouseFromChosenFolder() throws FoldingException {
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
 			if (!greenhouseDirectory.exists()) {
 				greenhouseDirectory.mkdirs();
 			}
 			saveToFolder(all);
 		}
 	}
 
 	public void saveToFolder(Object[] all) {
 		//first, clear out all the old organims
 		String[] oldOrganisms = greenhouseDirectory.list();
 		if (oldOrganisms  != null) {
 			for (int i = 0; i < oldOrganisms.length; i++) {
 				String name = oldOrganisms[i];
 				if (name.indexOf(".organism") != -1) {
 					File f = new File(greenhouseDirectory, name);
 					f.delete();
 				}
 			}
 		}
 
 		for (int i = 0; i < all.length; i++) {
 			Organism o = (Organism)all[i];
 			String name = o.getName();
 			String fileName = greenhouseDirectory.toString() 
 			+ System.getProperty("file.separator") 
 			+ name
 			+ ".organism";
 			Writer output = null;
 			try {
 				output = new BufferedWriter(new FileWriter(fileName) );
 				output.write(o.getGene1().getExpressedGene().getDNA());
 				output.write("\n");
 				output.write(o.getGene2().getExpressedGene().getDNA());
 				output.write("\n");
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 			finally {
 				if (output != null)
 					try {
 						output.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 			}
 
 		}
 	}
 
 	public void loadGreenhouse(File greenhouseDir) throws FoldingException {
 		clearSelectedOrganisms();
 		greenhouse.clearList();
 
 		greenhouseLoader = new GreenhouseLoader(greenhouseDir, greenhouse);
 
 		Thread t = new Thread(greenhouseLoader);
 		t.start();
 		greenhouseLoaderTimer.start();
 		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
 		statusLabel.setText("MGX is Loading " 
 				+ greenhouseLoader.getLengthOfTask() 
 				+ " Organisms into the Greenhouse");
 		progressBar.setMinimum(0);
 		progressBar.setMaximum(greenhouseLoader.getLengthOfTask());
 		progressBar.setValue(0);
 	}
 
 	private class GrenhouseLoaderTimerListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			if (greenhouseLoader.done()) {
 				greenhouseLoaderTimer.stop();
 				MolGenExp.this.setCursor(
 						Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 				progressBar.setValue(0);
 				statusLabel.setText("Ready");
 			} else {
 				statusLabel.setText("Loading Organism number " 
 						+ (greenhouseLoader.getCurrent() + 1) 
 						+ " of "
 						+ greenhouseLoader.getLengthOfTask());
 				progressBar.setValue(greenhouseLoader.getCurrent() + 1);
 			}
 		}
 	}
 
 
 	public void saveSelectedOrganismToGreenhouse() throws FoldingException {
 		switch (explorerPane.getSelectedIndex()) {
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
 			molBiolWorkbench.saveOrganismToGreenhouse();
 			break;
 
 		case EVOLUTION:
 			//get from world
 			Organism o = evolutionWorkArea.getWorld().getSelectedOrganism();
 			if (o != null) {
 				saveOrganismToGreenhouse(o);
 				evolutionWorkArea.getWorld().clearSelectedOrganism();
 			}
 			break;
 		}
 
 	}
 
 	public void saveOrganismToGreenhouse(Organism o) throws FoldingException {
 
 		String name = "";
 		String warning = "";
 		Pattern p = Pattern.compile("[^ A-Za-z0-9\\_\\-]+");
 		while (name.equals("") || 
 				p.matcher(name).find() ||
 				greenhouse.nameExistsAlready(name)){
 			name = JOptionPane.showInputDialog(
 					this,
 					warning +
 					"Give a unique name for your new organism.\n"
 					+ "This can only include letters, numbers, -, <space> and "
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
 		saveToGreenhouse(organismFactory.createOrganism(name,o));
 		clearSelectedOrganisms();
 	}
 
 	public void saveSnapshotToFile(BufferedImage bi) {
 		// try to save to desktop, if possible
 		String savePixToPath = "";
 		File desktopFile = new File(System.getProperty("user.home") 
 				+  System.getProperty("file.separator") 
 				+ "Desktop");
 		if (desktopFile.canWrite()) {
 			savePixToPath = desktopFile.getAbsolutePath();
 		} 
 
 		JFileChooser fc = new JFileChooser(savePixToPath);
 		int retVal = fc.showSaveDialog(this);
 		if (retVal == JFileChooser.APPROVE_OPTION) {
 			String fileName = fc.getSelectedFile().getAbsolutePath();
 			if (!fileName.endsWith(".png")) {
 				fileName = fileName.concat(".png");
 			}
 
 			try {
 				ImageIO.write(bi, "png", new File(fileName));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
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
 			geneticsWorkbench.setCrossTwoButtonsEnabled(false);
 			geneticsWorkbench.setSelfCrossButtonsEnabled(false);
 			geneticsWorkbench.setMutateButtonsEnabled(false);
 			break;
 
 		case 1:
 			addToGreenhouseButton.setEnabled(true);
 			geneticsWorkbench.setCrossTwoButtonsEnabled(false);
 			geneticsWorkbench.setSelfCrossButtonsEnabled(true);
 			geneticsWorkbench.setMutateButtonsEnabled(true);
 			break;
 
 		case 2:
 			addToGreenhouseButton.setEnabled(false);
 			geneticsWorkbench.setCrossTwoButtonsEnabled(true);
 			geneticsWorkbench.setSelfCrossButtonsEnabled(false);
 			geneticsWorkbench.setMutateButtonsEnabled(false);
 			break;
 
 		}
 	}
 
 	public void setDefaultSelectionSettings() {
 		greenhouse.setDefaultSelectionSettings();
 		geneticsWorkbench.setDefaultSelectionSettings();
 	}
 
 	public void setCustomSelectionSettings() {
 		greenhouse.setCustomSelectionSettings();
 		geneticsWorkbench.setCustomSelectionSettings();
 	}
 
 	public void setEvolutionSelectionSettings() {
 		greenhouse.setEvolutionSelectionSettings();
 		geneticsWorkbench.setDefaultSelectionSettings();
 	}
 
 	public void setAddToGreenhouseButtonEnabled(boolean b) {
 		addToGreenhouseButton.setEnabled(b);
 	}
 
 	private void turnOnSequenceClipboardItems() {
 		copyUpperSequenceToClipboardItem.setEnabled(true);
 		copyLowerSequenceToClipboardItem.setEnabled(true);
 	}
 
 	private void turnOffSequenceClipboardItems() {
 		copyUpperSequenceToClipboardItem.setEnabled(false);
 		copyLowerSequenceToClipboardItem.setEnabled(false);
 	}
 
 	public void setStatusLabelText(String text) {
 		statusLabel.setText(text);
 	}
 
 	public JProgressBar getProgressBar() {
 		return progressBar;
 	}
 
 	public JLabel getFoldingStatsLabel() {
 		return foldingStatsLabel;
 	}
 
 	public void loadSelectedIntoWorld() {
 		Organism[] orgs = greenhouse.getSelectedOrganisms();
 		if (orgs != null) {
 			evolutionWorkArea.getWorld().initialize(orgs);
 			evolutionWorkArea.setGeneration(0);
 			evolutionWorkArea.repaint();
 			evolutionWorkArea.setReadyToRun();
 		}
 	}
 
 	public void setButtonStatusWhileEvolving() {
 		addToGreenhouseButton.setEnabled(false);
 		fileMenu.setEnabled(false);
 		greenhouseMenu.setEnabled(false);
 		explorerPane.setEnabled(false);
 	}
 
 	public void restoreButtonStatusWhenDoneEvolving() {
 		addToGreenhouseButton.setEnabled(true);
 		fileMenu.setEnabled(true);
 		greenhouseMenu.setEnabled(true);
 		explorerPane.setEnabled(true);		
 	}
 
 }
