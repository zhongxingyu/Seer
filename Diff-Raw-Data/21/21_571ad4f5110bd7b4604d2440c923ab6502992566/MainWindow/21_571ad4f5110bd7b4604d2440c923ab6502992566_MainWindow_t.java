 package pl.poznan.put.cs.bioserver.gui;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 import org.apache.commons.lang3.StringUtils;
 import org.biojava.bio.structure.Chain;
 import org.biojava.bio.structure.ResidueNumber;
 import org.biojava.bio.structure.Structure;
 import org.biojava.bio.structure.StructureException;
 import org.biojava.bio.structure.StructureImpl;
 import org.biojava.bio.structure.align.gui.jmol.JmolPanel;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
 import org.jfree.data.xy.DefaultXYDataset;
 import org.jmol.api.JmolViewer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pl.poznan.put.cs.bioserver.alignment.AlignmentOutput;
 import pl.poznan.put.cs.bioserver.alignment.OutputAlignSeq;
 import pl.poznan.put.cs.bioserver.alignment.SequenceAligner;
 import pl.poznan.put.cs.bioserver.alignment.StructureAligner;
 import pl.poznan.put.cs.bioserver.comparison.ComparisonListener;
 import pl.poznan.put.cs.bioserver.comparison.GlobalComparison;
 import pl.poznan.put.cs.bioserver.comparison.MCQ;
 import pl.poznan.put.cs.bioserver.comparison.RMSD;
 import pl.poznan.put.cs.bioserver.comparison.TorsionLocalComparison;
 import pl.poznan.put.cs.bioserver.helper.Helper;
 import pl.poznan.put.cs.bioserver.helper.PdbManager;
 import pl.poznan.put.cs.bioserver.torsion.AngleDifference;
 import pl.poznan.put.cs.bioserver.visualisation.MDS;
 import pl.poznan.put.cs.bioserver.visualisation.MDSPlot;
 
 import com.csvreader.CsvWriter;
 
 class MainWindow extends JFrame {
     private static final long serialVersionUID = 1L;
     private static final Logger LOGGER = LoggerFactory
             .getLogger(MainWindow.class);
 
     private static final char CSV_DELIMITER = ';';
 
     private static final String CARD_GLOBAL = "CARD_GLOBAL";
     private static final String CARD_LOCAL = "CARD_LOCAL";
     private static final String CARD_ALIGN_SEQ = "CARD_ALIGN_SEQ";
     private static final String CARD_ALIGN_STRUC = "CARD_ALIGN_STRUC";
 
     private static final String ABOUT = "MCQ4Structures is a tool for "
             + "structural similarity computation based on molecule tertiary "
             + "structure representation in torsional angle space.\nIt has been "
             + "designed to work primarily for RNA structures. Proteins are "
             + "also handled and their representation is restricted to the "
             + "backbone angles.\n\nMCQ4Structures is available as a free Java "
             + "WebStart application. The source code is licensed under BSD."
             + "\n\nCiting MCQ4Structures.\n"
             + "T. Zok, M. Popenda, M. Szachniuk. MCQ4Structures to compute "
             + "similarity of molecule structures. Central European Journal of "
             + "Operations Research, in press.\n\nAcknowledgements and funding.\n"
             + "This work has been partially supported by the European Regional "
             + "Development Fund within Innovative Economy Programme "
             + "(POIG.02.03.00-00-018/08 POWIEW)\nand grants from the National "
             + "Science Centre, Poland (2012/05/B/ST6/03026).";
 
     private static Component getCurrentCard(JPanel panel) {
         for (Component component : panel.getComponents()) {
             if (component.isVisible()) {
                 return component;
             }
         }
         return null;
     }
 
     private StructureSelectionDialog structureDialog;
     private ChainSelectionDialog chainDialog;
     private TorsionAnglesSelectionDialog torsionDialog;
 
     private String[] resultGlobalNames;
     private double[][] resultGlobalMatrix;
     private Map<String, List<AngleDifference>> resultLocal;
     private String resultAlignStruc;
     private String resultAlignSeq;
 
     public MainWindow() {
         super();
 
         structureDialog = new StructureSelectionDialog(this);
         chainDialog = new ChainSelectionDialog(this);
         torsionDialog = new TorsionAnglesSelectionDialog(this);
 
         /*
          * Create menu
          */
         JMenuItem itemOpen = new JMenuItem("Open structure(s)",
                 loadIcon("/toolbarButtonGraphics/general/Open16.gif"));
         final JMenuItem itemSave = new JMenuItem("Save results",
                 loadIcon("/toolbarButtonGraphics/general/Save16.gif"));
         itemSave.setEnabled(false);
        final JCheckBox checkBoxManager = new JCheckBox("PDB manager dialog",
                true);
         JMenuItem itemExit = new JMenuItem("Exit");
         JMenu menuFile = new JMenu("File");
         menuFile.setMnemonic(KeyEvent.VK_F);
         menuFile.add(itemOpen);
         menuFile.add(itemSave);
         menuFile.addSeparator();
         menuFile.add(checkBoxManager);
         menuFile.addSeparator();
         menuFile.add(itemExit);
 
         final JRadioButton radioMcq = new JRadioButton("MCQ", true);
         JRadioButton radioRmsd = new JRadioButton("RMSD", false);
         ButtonGroup group = new ButtonGroup();
         group.add(radioMcq);
         group.add(radioRmsd);
         final JMenu menuMeasure = new JMenu("Distance measure");
         menuMeasure.setEnabled(false);
         menuMeasure.add(radioMcq);
         menuMeasure.add(radioRmsd);
 
         final JMenuItem itemSelectStructures = new JMenuItem(
                 "Select structures");
         final JMenuItem itemComputeGlobal = new JMenuItem(
                 "Compute distance matrix");
         itemComputeGlobal.setEnabled(false);
         final JMenuItem itemVisualise = new JMenuItem("Visualise results");
         itemVisualise.setEnabled(false);
         final JMenuItem itemCluster = new JMenuItem("Cluster results");
         itemCluster.setEnabled(false);
         JMenu menuGlobal = new JMenu("Global comparison");
         menuGlobal.add(itemSelectStructures);
         menuGlobal.add(menuMeasure);
         menuGlobal.add(itemComputeGlobal);
         menuGlobal.add(itemVisualise);
         menuGlobal.add(itemVisualise);
         menuGlobal.add(itemCluster);
 
         final JMenuItem itemSelectChainsCompare = new JMenuItem("Select chains");
         final JMenuItem itemSelectTorsion = new JMenuItem(
                 "Select torsion angles");
         itemSelectTorsion.setEnabled(false);
         final JMenuItem itemComputeLocal = new JMenuItem("Compute distances");
         itemComputeLocal.setEnabled(false);
         JMenu menuLocal = new JMenu("Local comparison");
         menuLocal.add(itemSelectChainsCompare);
         menuLocal.add(itemSelectTorsion);
         menuLocal.add(itemComputeLocal);
 
         JMenu menuDistance = new JMenu("Distance computation");
         menuDistance.setMnemonic(KeyEvent.VK_D);
         menuDistance.add(menuGlobal);
         menuDistance.addSeparator();
         menuDistance.add(menuLocal);
 
         final JRadioButton radioAlignGlobal = new JRadioButton("Global", true);
         JRadioButton radioAlignLocal = new JRadioButton("Local", false);
         ButtonGroup groupAlign = new ButtonGroup();
         groupAlign.add(radioAlignGlobal);
         groupAlign.add(radioAlignLocal);
         final JMenu menuSelectAlignType = new JMenu("Select alignment type");
         menuSelectAlignType.setEnabled(false);
         menuSelectAlignType.add(radioAlignGlobal);
         menuSelectAlignType.add(radioAlignLocal);
 
         final JMenuItem itemSelectChainsAlignSeq = new JMenuItem(
                 "Select chains");
         final JMenuItem itemComputeAlignSeq = new JMenuItem("Compute alignment");
         itemComputeAlignSeq.setEnabled(false);
         JMenu menuAlignSeq = new JMenu("Sequence");
         menuAlignSeq.add(itemSelectChainsAlignSeq);
         menuAlignSeq.add(menuSelectAlignType);
         menuAlignSeq.add(itemComputeAlignSeq);
 
         final JMenuItem itemSelectChainsAlignStruc = new JMenuItem(
                 "Select chains");
         final JMenuItem itemComputeAlignStruc = new JMenuItem(
                 "Compute alignment");
         itemComputeAlignStruc.setEnabled(false);
         JMenu menuAlignStruc = new JMenu("3D structure");
         menuAlignStruc.add(itemSelectChainsAlignStruc);
         menuAlignStruc.add(itemComputeAlignStruc);
 
         JMenu menuAlignment = new JMenu("Alignment");
         menuAlignment.setMnemonic(KeyEvent.VK_A);
         menuAlignment.add(menuAlignSeq);
         menuAlignment.addSeparator();
         menuAlignment.add(menuAlignStruc);
 
         JMenuItem itemGuide = new JMenuItem("Quick guide");
         itemGuide.setMnemonic(KeyEvent.VK_Q);
         itemGuide.setMaximumSize(new Dimension(
                 itemGuide.getPreferredSize().width,
                 itemGuide.getMaximumSize().height));
 
         JMenuItem itemAbout = new JMenuItem("About");
         itemAbout.setMnemonic(KeyEvent.VK_O);
         itemAbout.setMaximumSize(new Dimension(
                 itemAbout.getPreferredSize().width,
                 itemAbout.getMaximumSize().height));
 
         JMenuBar menuBar = new JMenuBar();
         menuBar.add(menuFile);
         menuBar.add(menuDistance);
         menuBar.add(menuAlignment);
         menuBar.add(itemGuide);
         menuBar.add(itemAbout);
 
         setJMenuBar(menuBar);
 
         /*
          * Create card layout
          */
         final JTable tableMatrix = new JTable();
         final JProgressBar progressBar = new JProgressBar();
         progressBar.setStringPainted(true);
         JPanel panelProgressBar = new JPanel();
         panelProgressBar.setLayout(new BoxLayout(panelProgressBar,
                 BoxLayout.X_AXIS));
         panelProgressBar.add(new JLabel("Progress of computations:"));
         panelProgressBar.add(progressBar);
 
         final JPanel panelResultsGlobal = new JPanel(new BorderLayout());
         panelResultsGlobal.add(new JScrollPane(tableMatrix),
                 BorderLayout.CENTER);
         panelResultsGlobal.add(panelProgressBar, BorderLayout.SOUTH);
 
         final JPanel panelResultsLocal = new JPanel(new GridLayout(1, 1));
 
         final JTextArea textAreaAlignSeq = new JTextArea();
         textAreaAlignSeq.setEditable(false);
         textAreaAlignSeq.setFont(new Font("Monospaced", Font.PLAIN, 20));
         final JPanel panelResultsAlignSeq = new JPanel(new BorderLayout());
         panelResultsAlignSeq.add(new JScrollPane(textAreaAlignSeq),
                 BorderLayout.CENTER);
 
         JPanel panelAlignStrucInfo = new JPanel(new GridBagLayout());
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.gridx = 0;
         constraints.gridy = 0;
         constraints.weightx = 0.5;
         panelAlignStrucInfo.add(new JLabel("Whole structures Jmol view"),
                 constraints);
         constraints.gridx++;
         constraints.weightx = 0;
         final JLabel labelAlignStrucStatus = new JLabel("Ready");
         panelAlignStrucInfo.add(labelAlignStrucStatus, constraints);
         constraints.gridx++;
         constraints.weightx = 0.5;
         panelAlignStrucInfo.add(new JLabel("Aligned fragments Jmol view"),
                 constraints);
 
         final JmolPanel panelJmolLeft = new JmolPanel();
         final JmolPanel panelJmolRight = new JmolPanel();
         JPanel panelJmolBoth = new JPanel(new GridLayout(1, 2));
         panelJmolBoth.add(panelJmolLeft);
         panelJmolBoth.add(panelJmolRight);
         final JPanel panelResultsAlignStruc = new JPanel(new BorderLayout());
         panelResultsAlignStruc.add(panelAlignStrucInfo, BorderLayout.NORTH);
         panelResultsAlignStruc.add(panelJmolBoth, BorderLayout.CENTER);
 
         final CardLayout layoutCards = new CardLayout();
         final JPanel panelCards = new JPanel();
         panelCards.setLayout(layoutCards);
         panelCards.add(panelResultsGlobal, MainWindow.CARD_GLOBAL);
         panelCards.add(panelResultsLocal, MainWindow.CARD_LOCAL);
         panelCards.add(panelResultsAlignSeq, MainWindow.CARD_ALIGN_SEQ);
         panelCards.add(panelResultsAlignStruc, MainWindow.CARD_ALIGN_STRUC);
 
         setLayout(new BorderLayout());
         add(panelCards, BorderLayout.CENTER);
 
         /*
          * Set window properties
          */
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setTitle("MCQ4Structures: computing similarity of 3D RNA / protein structures");
 
         Toolkit toolkit = Toolkit.getDefaultToolkit();
         Dimension size = toolkit.getScreenSize();
         setSize(size.width * 3 / 4, size.height * 3 / 4);
         setLocation(size.width / 8, size.height / 8);
 
         /*
          * Set action listeners
          */
         itemOpen.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 File[] files = PdbFileChooser.getSelectedFiles(MainWindow.this);
                 for (File f : files) {
                     if (PdbManager.loadStructure(f) != null) {
                         PdbManagerDialog.MODEL.addElement(f);
                     }
                 }
             }
         });
 
         itemSave.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JFileChooser chooser = new JFileChooser();
                 int chosenOption = chooser.showSaveDialog(MainWindow.this);
                 if (chosenOption != JFileChooser.APPROVE_OPTION) {
                     return;
                 }
 
                 Component current = MainWindow.getCurrentCard(panelCards);
                 if (current.equals(panelResultsGlobal)) {
                     saveResultsGlobalComparison(chooser.getSelectedFile());
                 } else if (current.equals(panelResultsLocal)) {
                     saveResultsLocalComparison(chooser.getSelectedFile());
                 } else if (current.equals(panelResultsAlignSeq)) {
                     try (FileOutputStream stream = new FileOutputStream(chooser
                             .getSelectedFile())) {
                         stream.write(resultAlignSeq.getBytes("UTF-8"));
                     } catch (IOException e1) {
                         MainWindow.LOGGER.error(
                                 "Failed to save aligned sequences", e1);
                         JOptionPane.showMessageDialog(MainWindow.this,
                                 e1.getMessage(), "Error",
                                 JOptionPane.ERROR_MESSAGE);
                     }
                 } else if (current.equals(panelResultsAlignStruc)) {
                     try (FileOutputStream stream = new FileOutputStream(chooser
                             .getSelectedFile())) {
                         stream.write(resultAlignStruc.getBytes("UTF-8"));
                     } catch (IOException e1) {
                         MainWindow.LOGGER.error(
                                 "Failed to save PDB of aligned structures", e1);
                         JOptionPane.showMessageDialog(MainWindow.this,
                                 e1.getMessage(), "Error",
                                 JOptionPane.ERROR_MESSAGE);
                     }
                 }
             }
         });
 
         checkBoxManager.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 PdbManagerDialog.getInstance().setVisible(
                         checkBoxManager.isSelected());
             }
         });
 
         itemExit.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 dispatchEvent(new WindowEvent(MainWindow.this,
                         WindowEvent.WINDOW_CLOSING));
             }
         });
 
         itemSelectStructures.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 /*
                  * Add new structures to the "all" section of structures
                  * selection dialog
                  */
                 Enumeration<File> elements = PdbManagerDialog.MODEL.elements();
                 while (elements.hasMoreElements()) {
                     File path = elements.nextElement();
                     if (!structureDialog.modelAll.contains(path)
                             && !structureDialog.modelSelected.contains(path)) {
                         structureDialog.modelAll.addElement(path);
                     }
                 }
                 /*
                  * Remove from "all" section these structures, that were removed
                  * in PDB manager dialog
                  */
                 elements = structureDialog.modelAll.elements();
                 while (elements.hasMoreElements()) {
                     File path = elements.nextElement();
                     if (PdbManager.getStructure(path) == null) {
                         structureDialog.modelAll.removeElement(path);
                     }
                 }
                 /*
                  * Remove from "selected" section these structures, that were
                  * removed in PDB manager dialog
                  */
                 elements = structureDialog.modelSelected.elements();
                 while (elements.hasMoreElements()) {
                     File path = elements.nextElement();
                     if (PdbManager.getStructure(path) == null) {
                         structureDialog.modelSelected.removeElement(path);
                     }
                 }
                 /*
                  * Show dialog
                  */
                 structureDialog.setVisible(true);
                 if (structureDialog.selectedStructures != null
                         && structureDialog.selectedStructures.size() >= 2) {
                     menuMeasure.setEnabled(true);
                     itemComputeGlobal.setEnabled(true);
                 }
             }
         });
 
         itemComputeGlobal.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (structureDialog.selectedStructures == null
                         || structureDialog.selectedStructures.size() < 2) {
                     JOptionPane.showMessageDialog(MainWindow.this,
                             "You must open at least two structures",
                             "Information", JOptionPane.INFORMATION_MESSAGE);
                     return;
                 }
 
                 layoutCards.show(panelCards, MainWindow.CARD_GLOBAL);
 
                 final GlobalComparison comparison;
                 if (radioMcq.isSelected()) {
                     comparison = new MCQ();
                 } else { // radioRmsd.isSelected() == true
                     comparison = new RMSD();
                 }
 
                 Thread thread = new Thread(new Runnable() {
                     @Override
                     public void run() {
                         Structure[] structures = PdbManager
                                 .getSelectedStructures(structureDialog.selectedStructures);
 
                         resultGlobalNames = PdbManager
                                 .getSelectedStructuresNames(structureDialog.selectedStructures);
                         resultGlobalMatrix = comparison.compare(structures,
                                 new ComparisonListener() {
                                     @Override
                                     public void stateChanged(long all,
                                             long completed) {
                                         progressBar.setMaximum((int) all);
                                         progressBar.setValue((int) completed);
                                     }
                                 });
 
                         SwingUtilities.invokeLater(new Runnable() {
                             @Override
                             public void run() {
                                 MatrixTableModel model = new MatrixTableModel(
                                         resultGlobalNames, resultGlobalMatrix);
                                 tableMatrix.setModel(model);
                                 itemSave.setEnabled(true);
                                 itemSave.setText("Save results (CSV)");
                                 itemCluster.setEnabled(true);
                                 itemVisualise.setEnabled(true);
                             }
                         });
                     }
                 });
                 thread.start();
             }
         });
 
         itemVisualise.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 MatrixTableModel model = (MatrixTableModel) tableMatrix
                         .getModel();
                 String[] names = model.getNames();
                 double[][] values = model.getValues();
 
                 for (double[] value : values) {
                     for (double element : value) {
                         if (Double.isNaN(element)) {
                             JOptionPane.showMessageDialog(MainWindow.this,
                                     "Cannot visualize, because some "
                                             + "of the structures were "
                                             + "incomparable", "Error",
                                     JOptionPane.ERROR_MESSAGE);
                             return;
                         }
                     }
                 }
 
                 double[][] mds = MDS.multidimensionalScaling(values, 2);
                 if (mds == null) {
                     JOptionPane.showMessageDialog(null,
                             "Cannot visualise specified structures in 2D",
                             "Warning", JOptionPane.WARNING_MESSAGE);
                     return;
                 }
 
                 MDSPlot plot = new MDSPlot(mds, names);
                 plot.setVisible(true);
             }
         });
 
         itemCluster.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 MatrixTableModel model = (MatrixTableModel) tableMatrix
                         .getModel();
                 String[] names = model.getNames();
                 double[][] values = model.getValues();
 
                 for (double[] value : values) {
                     for (double element : value) {
                         if (Double.isNaN(element)) {
                             JOptionPane.showMessageDialog(MainWindow.this,
                                     "Cannot cluster, because some "
                                             + "of the structures were "
                                             + "incomparable", "Error",
                                     JOptionPane.ERROR_MESSAGE);
                             return;
                         }
                     }
                 }
 
                 ClusteringDialog dialogClustering = new ClusteringDialog(names,
                         values);
                 dialogClustering.setVisible(true);
             }
         });
 
         ActionListener actionListenerSelectChains = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 chainDialog.modelLeft.removeAllElements();
                 chainDialog.modelRight.removeAllElements();
                 Enumeration<File> elements = PdbManagerDialog.MODEL.elements();
                 while (elements.hasMoreElements()) {
                     File path = elements.nextElement();
                     chainDialog.modelLeft.addElement(path);
                     chainDialog.modelRight.addElement(path);
                 }
                 chainDialog.setVisible(true);
 
                 if (chainDialog.selectedStructures != null
                         && chainDialog.selectedChains != null) {
                     for (int i = 0; i < 2; i++) {
                         if (chainDialog.selectedChains[i].length == 0) {
                             JOptionPane
                                     .showMessageDialog(
                                             MainWindow.this,
                                             "No chains specified for structure: "
                                                     + chainDialog.selectedStructures[i],
                                             "Error", JOptionPane.ERROR_MESSAGE);
                             chainDialog.selectedStructures = null;
                             chainDialog.selectedChains = null;
                             return;
                         }
                     }
 
                     Object source = arg0.getSource();
                     if (source.equals(itemSelectChainsCompare)) {
                         itemSelectTorsion.setEnabled(true);
                     } else if (source.equals(itemSelectChainsAlignSeq)) {
                         if (chainDialog.selectedChains[0].length != 1
                                 || chainDialog.selectedChains[1].length != 1) {
                             JOptionPane.showMessageDialog(MainWindow.this,
                                     "For sequence alignment, you need "
                                             + "to select a single chain "
                                             + "from each structure",
                                     "Information",
                                     JOptionPane.INFORMATION_MESSAGE);
                             chainDialog.selectedStructures = null;
                             chainDialog.selectedChains = null;
                             return;
                         }
                         menuSelectAlignType.setEnabled(true);
                         itemComputeAlignSeq.setEnabled(true);
                     } else { // source.equals(itemSelectChainsAlignStruc)
                         itemComputeAlignStruc.setEnabled(true);
                     }
                 }
             }
         };
         itemSelectChainsCompare.addActionListener(actionListenerSelectChains);
         itemSelectChainsAlignSeq.addActionListener(actionListenerSelectChains);
         itemSelectChainsAlignStruc
                 .addActionListener(actionListenerSelectChains);
 
         itemSelectTorsion.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 torsionDialog.setVisible(true);
                 itemComputeLocal.setEnabled(true);
             }
         });
 
         itemComputeLocal.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 layoutCards.show(panelCards, MainWindow.CARD_LOCAL);
 
                 final Structure[] structures = new Structure[2];
                 for (int i = 0; i < 2; i++) {
                     structures[i] = new StructureImpl();
                     structures[i].setChains(Arrays
                             .asList(chainDialog.selectedChains[i]));
                 }
 
                 try {
                     resultLocal = TorsionLocalComparison.compare(structures[0],
                             structures[1], false);
                 } catch (StructureException e1) {
                     JOptionPane.showMessageDialog(MainWindow.this,
                             e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                     return;
                 }
 
                 Set<ResidueNumber> set = new HashSet<>();
                 for (String angle : torsionDialog.selectedNames) {
                     if (!resultLocal.containsKey(angle)) {
                         continue;
                     }
                     for (AngleDifference ad : resultLocal.get(angle)) {
                         set.add(ad.getResidue());
                     }
                 }
                 List<ResidueNumber> list = new ArrayList<>(set);
                 Collections.sort(list);
 
                 DefaultXYDataset dataset = new DefaultXYDataset();
                 for (String angle : torsionDialog.selectedNames) {
                     if (!resultLocal.containsKey(angle)) {
                         continue;
                     }
                     List<AngleDifference> diffs = resultLocal.get(angle);
                     Collections.sort(diffs);
 
                     double[] x = new double[diffs.size()];
                     double[] y = new double[diffs.size()];
                     for (int i = 0; i < diffs.size(); i++) {
                         AngleDifference ad = diffs.get(i);
                         x[i] = list.indexOf(ad.getResidue());
                         y[i] = ad.getDifference();
                     }
                     dataset.addSeries(angle, new double[][] { x, y });
                 }
 
                 NumberAxis xAxis = new TorsionAxis(resultLocal);
                 xAxis.setLabel("Residue");
                 NumberAxis yAxis = new NumberAxis();
                 yAxis.setAutoRange(false);
                 yAxis.setRange(0, Math.PI);
                 yAxis.setLabel("Distance [rad]");
                 XYPlot plot = new XYPlot(dataset, xAxis, yAxis,
                         new DefaultXYItemRenderer());
 
                 panelResultsLocal.removeAll();
                 panelResultsLocal.add(new ChartPanel(new JFreeChart(plot)));
                 panelResultsLocal.revalidate();
 
                 itemSave.setEnabled(true);
                 itemSave.setText("Save results (CSV)");
             }
         });
 
         itemComputeAlignSeq.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 layoutCards.show(panelCards, MainWindow.CARD_ALIGN_SEQ);
 
                 Chain chains[] = new Chain[] {
                         chainDialog.selectedChains[0][0],
                         chainDialog.selectedChains[1][0] };
 
                 boolean isRNA = Helper.isNucleicAcid(chains[0]);
                 if (isRNA != Helper.isNucleicAcid(chains[1])) {
                     String message = "Structures meant to be aligned "
                             + "represent different molecule types!";
                     MainWindow.LOGGER.error(message);
                     JOptionPane.showMessageDialog(null, message, "Error",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
 
                 OutputAlignSeq alignment = SequenceAligner.align(chains[0],
                         chains[1], radioAlignGlobal.isSelected());
                 resultAlignSeq = alignment.toString();
                 textAreaAlignSeq.setText(resultAlignSeq);
 
                 itemSave.setEnabled(true);
                 itemSave.setText("Save results (TXT)");
             }
         });
 
         itemComputeAlignStruc.addActionListener(new ActionListener() {
             private Thread thread;
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (thread != null && thread.isAlive()) {
                     JOptionPane.showMessageDialog(null,
                             "Alignment calculation underway!", "Information",
                             JOptionPane.INFORMATION_MESSAGE);
                     return;
                 }
 
                 layoutCards.show(panelCards, MainWindow.CARD_ALIGN_STRUC);
 
                 final Structure[] structures = new Structure[2];
                 for (int i = 0; i < 2; i++) {
                     structures[i] = new StructureImpl();
                     structures[i].setChains(Arrays
                             .asList(chainDialog.selectedChains[i]));
                 }
 
                 boolean isRNA = Helper.isNucleicAcid(structures[0]);
                 if (isRNA != Helper.isNucleicAcid(structures[1])) {
                     String message = "Structures meant to be aligned "
                             + "represent different molecule types!";
                     MainWindow.LOGGER.error(message);
                     JOptionPane.showMessageDialog(null, message, "Error",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
 
                 labelAlignStrucStatus.setText("Processing");
                 final Timer timer = new Timer(250, new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent arg0) {
                         String text = labelAlignStrucStatus.getText();
                         int count = StringUtils.countMatches(text, ".");
                         if (count < 5) {
                             labelAlignStrucStatus.setText(text + ".");
                         } else {
                             labelAlignStrucStatus.setText("Processing");
                         }
                     }
                 });
                 timer.start();
 
                 thread = new Thread(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             Helper.normalizeAtomNames(structures[0]);
                             Helper.normalizeAtomNames(structures[1]);
 
                             AlignmentOutput output = StructureAligner.align(
                                     structures[0], structures[1]);
                             final Structure[] aligned = output.getStructures();
 
                             SwingUtilities.invokeLater(new Runnable() {
                                 private static final String JMOL_SCRIPT = "frame 0.0; "
                                         + "cartoon only; "
                                         + "select model=1.1; color green; "
                                         + "select model=1.2; color red; ";
 
                                 @Override
                                 public void run() {
                                     StringBuilder builder = new StringBuilder();
                                     builder.append("MODEL        1                                                                  \n");
                                     builder.append(aligned[0].toPDB());
                                     builder.append("ENDMDL                                                                          \n");
                                     builder.append("MODEL        2                                                                  \n");
                                     builder.append(aligned[1].toPDB());
                                     builder.append("ENDMDL                                                                          \n");
                                     resultAlignStruc = builder.toString();
 
                                     JmolViewer viewer = panelJmolLeft
                                             .getViewer();
                                     viewer.openStringInline(builder.toString());
                                     panelJmolLeft.executeCmd(JMOL_SCRIPT);
 
                                     builder = new StringBuilder();
                                     builder.append("MODEL        1                                                                  \n");
                                     builder.append(aligned[2].toPDB());
                                     builder.append("ENDMDL                                                                          \n");
                                     builder.append("MODEL        2                                                                  \n");
                                     builder.append(aligned[3].toPDB());
                                     builder.append("ENDMDL                                                                          \n");
 
                                     viewer = panelJmolRight.getViewer();
                                     viewer.openStringInline(builder.toString());
                                     panelJmolRight.executeCmd(JMOL_SCRIPT);
 
                                     itemSave.setEnabled(true);
                                     itemSave.setText("Save results (PDB)");
                                 }
                             });
                         } catch (StructureException e1) {
                             MainWindow.LOGGER.error(
                                     "Failed to align structures", e1);
                             JOptionPane.showMessageDialog(getParent(),
                                     e1.getMessage(), "Error",
                                     JOptionPane.ERROR_MESSAGE);
                         } finally {
                             timer.stop();
                             labelAlignStrucStatus.setText("Ready");
                         }
                     }
                 });
                 thread.start();
             }
         });
 
         itemGuide.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 QuickGuideDialog dialog = new QuickGuideDialog(MainWindow.this);
                 dialog.setVisible(true);
             }
         });
 
         itemAbout.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JOptionPane.showMessageDialog(MainWindow.this,
                         MainWindow.ABOUT, "About",
                         JOptionPane.INFORMATION_MESSAGE);
             }
         });
     }
 
     private ImageIcon loadIcon(String name) {
         URL resource = getClass().getResource(name);
         if (resource == null) {
             MainWindow.LOGGER.error("Failed to load icon: " + name);
             return null;
         }
         return new ImageIcon(resource);
     }
 
     private void saveResultsGlobalComparison(File outputFile) {
         try (FileOutputStream stream = new FileOutputStream(outputFile)) {
             CsvWriter writer = new CsvWriter(stream, MainWindow.CSV_DELIMITER,
                     Charset.forName("UTF-8"));
             /*
              * Print header
              */
             int length = resultGlobalNames.length;
             writer.write("");
             for (int i = 0; i < length; i++) {
                 writer.write(resultGlobalNames[i]);
             }
             writer.endRecord();
             /*
              * Print each value in the matrix
              */
             for (int i = 0; i < length; i++) {
                 writer.write(resultGlobalNames[i]);
                 for (int j = 0; j < length; j++) {
                     writer.write(Double.toString(resultGlobalMatrix[i][j]));
                 }
                 writer.endRecord();
             }
             writer.close();
         } catch (IOException e) {
             MainWindow.LOGGER.error(
                     "Failed to save results from global comparison", e);
             JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }
 
     private void saveResultsLocalComparison(File outputFile) {
         /*
          * Reverse information: [angleName -> angleValue(residue)] into:
          * [residue -> angleValue(angleName)]
          */
         SortedMap<String, Map<String, Double>> map = new TreeMap<>();
         for (Entry<String, List<AngleDifference>> pair : resultLocal.entrySet()) {
             String angleName = pair.getKey();
             for (AngleDifference ad : pair.getValue()) {
                 ResidueNumber residue = ad.getResidue();
                 String residueName = String.format("%s:%03d",
                         residue.getChainId(), residue.getSeqNum());
                 if (!map.containsKey(residueName)) {
                     map.put(residueName, new LinkedHashMap<String, Double>());
                 }
                 Map<String, Double> angleValues = map.get(residueName);
                 angleValues.put(angleName, ad.getDifference());
             }
         }
 
         try (FileOutputStream stream = new FileOutputStream(outputFile)) {
             CsvWriter writer = new CsvWriter(stream, MainWindow.CSV_DELIMITER,
                     Charset.forName("UTF-8"));
             /*
              * Write header
              */
             writer.write("");
             Set<String> keySetReference = map.get(map.firstKey()).keySet();
             for (String angleName : keySetReference) {
                 writer.write(angleName);
             }
             writer.endRecord();
             /*
              * Write a record for each residue
              */
             for (String residueName : map.keySet()) {
                 writer.write(residueName);
                 Map<String, Double> mapAngles = map.get(residueName);
                 for (String angleName : keySetReference) {
                     if (mapAngles.containsKey(angleName)) {
                         String angleValue = Double.toString(mapAngles
                                 .get(angleName));
                         writer.write(angleValue);
                     } else {
                         writer.write("");
                     }
                 }
                 writer.endRecord();
             }
             writer.close();
         } catch (IOException e) {
             MainWindow.LOGGER.error(
                     "Failed to save results from local comparison", e);
             JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }
 }
