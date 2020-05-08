 /*
  * Experiment.java
  *
  * Created on July 11, 2006, 3:23 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package gov.nih.nimh.mass_sieve.gui;
 
 import antlr.RecognitionException;
 import antlr.TokenStreamException;
 import com.javadocking.DockingManager;
 import com.javadocking.component.DefaultSwComponentFactory;
 import com.javadocking.dock.LeafDock;
 import com.javadocking.dock.Position;
 import com.javadocking.dock.SplitDock;
 import com.javadocking.dock.TabDock;
 import com.javadocking.dockable.DefaultDockable;
 import com.javadocking.dockable.DockingMode;
 import com.javadocking.model.FloatDockModel;
 import com.javadocking.model.codec.DockModelPropertiesDecoder;
 import com.javadocking.model.codec.DockModelPropertiesEncoder;
 import gov.nih.nimh.mass_sieve.*;
 import gov.nih.nimh.mass_sieve.io.FileInformation;
 import gov.nih.nimh.mass_sieve.io.ParseFile;
 import gov.nih.nimh.mass_sieve.io.SetLexer;
 import gov.nih.nimh.mass_sieve.io.SetParser;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import javax.swing.BoxLayout;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTree;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeSelectionModel;
 import prefuse.Display;
 
 /**
  *
  * @author slotta
  */
 public class ExperimentPanel extends JPanel {
 
     private ArrayList<File> allFiles;
     private ArrayList<FileInformation> fileInfos;
     private PeptideCollection pepCollection,  pepCollectionOriginal;
     private FilterSettings filterSettings;
     private double omssaCutoffOrig,  mascotCutoffOrig,  xtandemCutoffOrig,  sequestCutoffOrig,  peptideProphetCutoffOrig;
     private DefaultTreeModel treeModelOverview;
     private FilterSettingsDialog prefDialog;
     private SummaryDialog summaryDialog;
     private JFileChooser jFileChooserLoad;
     //private JScrollPane jScrollPaneLeft;
     //private JSplitPane jSplitPaneMain;
     private JScrollPane graphPanel,  treePanel;
     private JPanel pepHitPanel,  pepPanel,  proPanel,  detailPanel;
     private FloatDockModel dockModel;
     private DefaultDockable pepHitDockable,  pepDockable,  proDockable,  graphDockable,  detailDockable,  treeDockable;
     private JTree jTreeMain;
     private MassSieveFrame msFrame;
     private final static String DOCK_NAME = "MassSieve";
     private final static String DOCK_FILE = "MassSieve.dck";
     private final static String ROOT_DOCK = "msRootDock";
     private SplitDock rootDock;
 
     //private String lowerFrameTitle, upperFrameTitle;
     private static class MyComponentFactory extends DefaultSwComponentFactory {
 
         public JSplitPane createJSplitPane() {
             JSplitPane splitPane = super.createJSplitPane();
             splitPane.setDividerSize(5);
             return splitPane;
         }
     }
 
     /** Creates a new instance of ExperimentPanel */
     public ExperimentPanel(MassSieveFrame frm, String name) {
         msFrame = frm;
         this.setName(name);
         initComponents();
         loadDockState();
         jFileChooserLoad.setMultiSelectionEnabled(true);
         allFiles = new ArrayList<File>();
         fileInfos = new ArrayList<FileInformation>();
         filterSettings = new FilterSettings();
         omssaCutoffOrig = filterSettings.getOmssaCutoff();
         mascotCutoffOrig = filterSettings.getMascotCutoff();
         xtandemCutoffOrig = filterSettings.getXtandemCutoff();
         sequestCutoffOrig = filterSettings.getSequestCutoff();
         peptideProphetCutoffOrig = filterSettings.getPeptideProphetCutoff();
         cleanDisplay();
     }
 
     private void cleanDisplay() {
         pepCollectionOriginal = new PeptideCollection();
         DefaultMutableTreeNode root = new DefaultMutableTreeNode("No data");
         DefaultTreeModel treeModel = new DefaultTreeModel(root);
         jTreeMain.setModel(treeModel);
         jTreeMain.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
         jTreeMain.setSelectionRow(0);
     }
 
     public void loadDockState() {
         // Try to decode the dock model from file.
         DockModelPropertiesDecoder dockModelDecoder = new DockModelPropertiesDecoder();
         if (dockModelDecoder.canDecodeSource(DOCK_FILE)) {
             try {
                 // Create the map with the dockables, that the decoder needs.
                 Map dockablesMap = new HashMap();
                 dockablesMap.put(pepHitDockable.getID(), pepHitDockable);
                 dockablesMap.put(pepDockable.getID(), pepDockable);
                 dockablesMap.put(proDockable.getID(), proDockable);
                 dockablesMap.put(graphDockable.getID(), graphDockable);
                 dockablesMap.put(detailDockable.getID(), detailDockable);
                 dockablesMap.put(treeDockable.getID(), treeDockable);
 
                 // Create the map with the owner windows, that the decoder needs.
                 Map ownersMap = new HashMap();
                 ownersMap.put(DOCK_NAME, msFrame);
 
                 // Create the map with the visualizers, that the decoder needs.
                 Map visualizersMap = new HashMap();
 
                 // Decode the file.
                 dockModel = (FloatDockModel) dockModelDecoder.decode(DOCK_FILE, dockablesMap, ownersMap, visualizersMap);
                 rootDock = (SplitDock) dockModel.getRootDock(ROOT_DOCK);
                 add(rootDock, BorderLayout.CENTER, 0);
                 this.validate();
             //jSplitPaneMain.setRightComponent(rootDock);
             } catch (FileNotFoundException fileNotFoundException) {
                 System.out.println("Could not find the file [" + DOCK_FILE + "] with the saved dock model.");
                 System.out.println("Continuing with the default dock model.");
             } catch (IOException ioException) {
                 System.out.println("Could not decode a dock model: [" + ioException + "].");
                 ioException.printStackTrace();
                 System.out.println("Continuing with the default dock model.");
             }
         }
         DockingManager.setDockModel(dockModel);
     }
 
     public void saveDockState() {
         DockModelPropertiesEncoder encoder = new DockModelPropertiesEncoder();
         if (encoder.canExport(dockModel, DOCK_FILE)) {
             try {
                 encoder.export(dockModel, DOCK_FILE);
             } catch (Exception e) {
                 System.out.println("Error while saving the dock model.");
                 e.printStackTrace();
             }
         } else {
             System.out.println("Could not save the dock model.");
         }
     }
 
     private void initComponents() {
         setLayout(new BorderLayout());
 
         jFileChooserLoad = new JFileChooser();
         //jSplitPaneMain = new JSplitPane();
         //jScrollPaneLeft = new JScrollPane();
         jTreeMain = new JTree();
         pepHitPanel = new JPanel(new BorderLayout());
         pepPanel = new JPanel(new BorderLayout());
         proPanel = new JPanel(new BorderLayout());
         detailPanel = new JPanel(new BorderLayout());
         graphPanel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         treePanel = new JScrollPane();
         pepHitDockable = new DefaultDockable("pepHits", pepHitPanel, "Peptide Hits", null, DockingMode.ALL - DockingMode.FLOAT);
         pepDockable = new DefaultDockable("pep", pepPanel, "Peptides", null, DockingMode.ALL - DockingMode.FLOAT);
         proDockable = new DefaultDockable("pro", proPanel, "Proteins", null, DockingMode.ALL - DockingMode.FLOAT);
         graphDockable = new DefaultDockable("graph", graphPanel, "Cluster Graph", null, DockingMode.ALL - DockingMode.FLOAT);
         detailDockable = new DefaultDockable("detail", detailPanel, "Details", null, DockingMode.ALL - DockingMode.FLOAT);
         treeDockable = new DefaultDockable("tree", treePanel, "Overview", null, DockingMode.ALL - DockingMode.FLOAT);
         //treeDockable = new DefaultDockable("tree", treePanel, "Overview", null, DockingMode.LEFT);
 
         jFileChooserLoad.setDialogTitle("Open Files");
         //jSplitPaneMain.setBorder(null);
         //jSplitPaneMain.setDividerLocation(175);
         //jSplitPaneMain.setDividerSize(5);
         //jSplitPaneMain.setMinimumSize(new java.awt.Dimension(0, 0));
         jTreeMain.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
 
             public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                 jTreeMainValueChanged(evt);
             }
         });
 
         treePanel.setViewportView(jTreeMain);
 
         resetDockModel();
     }
 
     public void resetDockModel() {
         // Create the dock model for the docks.
         dockModel = new FloatDockModel();
         //dockModel.addOwner(this.getName(), msFrame);
         dockModel.addOwner(DOCK_NAME, msFrame);
 
         // Give the dock model to the docking manager.
         DockingManager.setComponentFactory(new MyComponentFactory());
         DockingManager.setDockModel(dockModel);
 
         rootDock = new SplitDock();
         TabDock tabDockLeft = new TabDock();
         SplitDock splitDockRight = new SplitDock();
         TabDock tabDockTop = new TabDock();
         TabDock tabDockBottom = new TabDock();
 
         tabDockTop.addDockable(proDockable, new Position(0));
         tabDockTop.addDockable(pepDockable, new Position(1));
         tabDockTop.addDockable(pepHitDockable, new Position(2));
         tabDockBottom.addDockable(graphDockable, new Position(0));
         tabDockBottom.addDockable(detailDockable, new Position(1));
         tabDockLeft.addDockable(treeDockable, new Position(0));
         splitDockRight.addChildDock(tabDockTop, new Position(Position.TOP));
         splitDockRight.addChildDock(tabDockBottom, new Position(Position.BOTTOM));
         rootDock.addChildDock(tabDockLeft, new Position(Position.LEFT));
         rootDock.addChildDock(splitDockRight, new Position(Position.RIGHT));
         tabDockTop.setSelectedDockable(proDockable);
         tabDockBottom.setSelectedDockable(graphDockable);
         splitDockRight.setDividerLocation(400);
         rootDock.setDividerLocation(175);
 
         // Add the root docks to the dock model.
         dockModel.addRootDock(ROOT_DOCK, rootDock, msFrame);
 
         add(rootDock, BorderLayout.CENTER, 0);
     }
 
     public void showPreferences() {
         if (prefDialog == null) {
             prefDialog = new FilterSettingsDialog(this);
         }
         prefDialog.updateFilterDisplay();
         prefDialog.setVisible(true);
     }
 
     public void showSummary() {
         if (summaryDialog == null) {
             summaryDialog = new SummaryDialog(this);
         }
         summaryDialog.setFileInformation(fileInfos);
         summaryDialog.setVisible(true);
     }
 
     public void saveExperiment(ObjectOutputStream os) {
         System.out.print("Saving experiment " + this.getName() + "...");
         Experiment exp = new Experiment();
         exp.setName(this.getName());
         exp.setFileInfos(fileInfos);
         exp.setFilterSettings(filterSettings);
         exp.setPepCollection(pepCollection);
         exp.setPepCollectionOriginal(pepCollectionOriginal);
         try {
             os.writeObject(exp);
         } catch (IOException ex) {
             ex.printStackTrace();
         }
         System.out.println(" done");
     }
 
     public void reloadData(Experiment exp) {
         pepCollectionOriginal = exp.getPepCollectionOriginal();
         pepCollection = exp.getPepCollection();
         fileInfos = exp.getFileInfos();
         filterSettings = getFilterSettings();
         updateDisplay();
     }
 
     private PeptideCollection FilterBySearchProgram(PeptideCollection pc) {
         PeptideCollection result;
         StringReader setDescription = new StringReader(filterSettings.getFilterText());
         SetLexer lexer = new SetLexer(setDescription);
         SetParser parser = new SetParser(lexer);
         parser.setPeptideCollection(pc);
         try {
             result = parser.expr();
             result.updatePeptideHits();
             return result;
         } catch (TokenStreamException ex) {
             ex.printStackTrace();
         } catch (RecognitionException ex) {
             ex.printStackTrace();
         }
         return new PeptideCollection();
     }
 
     public void recomputeCutoff() {
         PeptideCollection pepFiltered;
         pepFiltered = pepCollectionOriginal.getCutoffCollection(filterSettings);
         pepFiltered.computeIndeterminates();
         if (!filterSettings.getUseIndeterminates()) {
             pepFiltered = pepFiltered.getNonIndeterminents();
         }
         pepFiltered = FilterBySearchProgram(pepFiltered);
         if (filterSettings.getFilterPeptides()) {
             pepFiltered = pepFiltered.getPeptidesByHits(filterSettings.getPepHitCutoffCount());
         }
         pepFiltered.createProteinList();
         if (filterSettings.getFilterProteins()) {
             pepFiltered = pepFiltered.filterByPeptidePerProtein(filterSettings.getPeptideCutoffCount());
         }
         if (filterSettings.getFilterCoverage()) {
             pepFiltered.updateClusters();
             pepFiltered = pepFiltered.filterByProteinCoverage(filterSettings.getCoverageCutoffAmount());
         }
         pepFiltered.updateClusters();
         pepCollection = pepFiltered;
 
         updateDisplay();
     }
 
     public void addFiles(final File files[]) {
         String exp_name = this.getName();
         for (File f : files) {
             HashSet<String> acceptedProteins = new HashSet<String>();
             allFiles.add(f);
             String filename = f.getName();
             ParseFile pf = new ParseFile(f, ExperimentPanel.this);
             for (PeptideHit p : pf.getPeptideHits()) {
                 p.setExperiment(exp_name);
                 p.setSourceFile(filename);
                 boolean usePepHit = false;
                 if (filterSettings.getUsePepProphet() && p.isPepXML()) {
                     if (p.getPepProphet() >= filterSettings.getPeptideProphetCutoff()) {
                         usePepHit = true;
                     }
                 } else {
                     switch (p.getSourceType()) {
                         case MASCOT:
                             if (p.getExpect() <= filterSettings.getMascotCutoff()) {
                                 usePepHit = true;
                             }
                             break;
                         case OMSSA:
                             if (p.getExpect() <= filterSettings.getOmssaCutoff()) {
                                 usePepHit = true;
                             }
                             break;
                         case XTANDEM:
                             if (p.getExpect() <= filterSettings.getXtandemCutoff()) {
                                 usePepHit = true;
                             }
                             break;
                         case SEQUEST:
                             if (p.getExpect() <= filterSettings.getSequestCutoff()) {
                                 usePepHit = true;
                             }
                             break;
                     }
                 }
                 if (usePepHit) {
                     pepCollectionOriginal.addPeptideHit(p);
                     acceptedProteins.addAll(p.getProteinNames());
                 }
             }
             HashMap<String, ProteinInfo> pDB = pf.getProteinDB();
             for (String pName : pDB.keySet()) {
                 if (acceptedProteins.contains(pName)) {
                     MassSieveFrame.addProtein(pDB.get(pName));
                 }
             }
             FileInformation fInfo = pf.getFileInformation();
             fInfo.setExperiment(exp_name);
             fileInfos.add(fInfo);
         }
         recomputeCutoff();
     }
 
     public void addPeptideHits(ArrayList<PeptideHit> pHits) {
         for (PeptideHit p : pHits) {
             pepCollectionOriginal.addPeptideHit(p);
         }
         recomputeCutoff();
     }
 
     public synchronized void reloadFiles() {
         if ((filterSettings.getOmssaCutoff() > omssaCutoffOrig) || (filterSettings.getMascotCutoff() > mascotCutoffOrig) ||
                 (filterSettings.getXtandemCutoff() > xtandemCutoffOrig) || (filterSettings.getSequestCutoff() > sequestCutoffOrig) ||
                 (filterSettings.getPeptideProphetCutoff() < peptideProphetCutoffOrig)) {
             cleanDisplay();
             if (filterSettings.getOmssaCutoff() > omssaCutoffOrig) {
                 omssaCutoffOrig = filterSettings.getOmssaCutoff();
             }
             if (filterSettings.getMascotCutoff() > mascotCutoffOrig) {
                 mascotCutoffOrig = filterSettings.getMascotCutoff();
             }
             if (filterSettings.getXtandemCutoff() > xtandemCutoffOrig) {
                 xtandemCutoffOrig = filterSettings.getXtandemCutoff();
             }
             if (filterSettings.getSequestCutoff() > sequestCutoffOrig) {
                 sequestCutoffOrig = filterSettings.getSequestCutoff();
             }
             if (filterSettings.getPeptideProphetCutoff() < peptideProphetCutoffOrig) {
                 peptideProphetCutoffOrig = filterSettings.getPeptideProphetCutoff();
             }
             System.err.println("Must reload files due to more permisive filter settings");
             new Thread(new Runnable() {
 
                 public void run() {
                     setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                     File f[] = new File[allFiles.size()];
                     allFiles.toArray(f);
                     allFiles.clear();
                     fileInfos.clear();
                     addFiles(f);
 
                     setCursor(null);
                 }
             }).start();
         } else {
             recomputeCutoff();
         }
     }
 
     private void jTreeMainValueChanged(javax.swing.event.TreeSelectionEvent evt) {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTreeMain.getLastSelectedPathComponent();
 
         if (node == null) {
             return;
         }
         Object nodeInfo = node.getUserObject();
         if (nodeInfo instanceof String) {
             msFrame.updateStatusMessage(nodeInfo.toString() + " selected");
             if (pepCollection == null) {
                 updatePepHitPanel(new JLabel("Please add search results to this experiment"));
                 updatePepPanel(new JLabel("Please add search results to this experiment"));
                 updateProPanel(new JLabel("Please add search results to this experiment"));
                 updateGraphPanel(new JLabel("Please add search results to this experiment"));
                 updateDetailPanel(new JLabel("Please add search results to this experiment"));
             } else {
                 updatePepHitPanel(pepCollection.getPeptideHitListPanel(this).createTable());
                 updatePepPanel(pepCollection.getPeptideListPanel(this).createTable());
                 updateProPanel(pepCollection.getParsimonyListPanel(this).createTable());
                 updateGraphPanel(new JLabel("No cluster, peptide, or protein selected"));
                 updateDetailPanel(new JLabel("No details for this item"));
             }
         }
         if (nodeInfo instanceof Peptide) {
             Peptide p = (Peptide) nodeInfo;
             PeptideCollection pc = pepCollection.getCluster(p.getCluster());
             updateGraphPanel(pc, p.getSequence());
             ArrayList<Protein> proteins = new ArrayList<Protein>();
             for (String pName : p.getProteins()) {
                 proteins.add(pepCollection.getMinProteins().get(pName));
             }
             ProteinListPanel lp = new ProteinListPanel(this);
             lp.addProteinList(proteins, pepCollection.getExperimentSet());
             updateProPanel(lp.createTable());
             showPeptide(p, true);
         }
         if (nodeInfo instanceof Protein) {
             Protein p = (Protein) nodeInfo;
             PeptideCollection pc = pepCollection.getCluster(p.getCluster());
             updateGraphPanel(pc, p.getName());
             showProtein(p, true);
         }
         if (nodeInfo instanceof PeptideCollection) {
             PeptideCollection pc = (PeptideCollection) nodeInfo;
             msFrame.updateStatusMessage(pc.toString() + " selected");
             updateGraphPanel(pc, null);
             Set<String> peps = pc.getPeptideNames();
             Set<String> pros = pc.getProteinNames();
             updatePepHitPanel(pepCollection.getPeptideHitListPanel(this, peps).createTable());
             updatePepPanel(pepCollection.getPeptideListPanel(this, peps).createTable());
             updateProPanel(pepCollection.getProteinListPanel(this, pros).createTable());
         }
         if (nodeInfo instanceof PeptideProteinNameSet) {
             PeptideProteinNameSet pps = (PeptideProteinNameSet) nodeInfo;
             msFrame.updateStatusMessage(pps.toString() + " selected");
             //updateGraphPanel(pc, null);
             updatePepHitPanel(pepCollection.getPeptideHitListPanel(this, pps.getPeptides()).createTable());
             updatePepPanel(pepCollection.getPeptideListPanel(this, pps.getPeptides()).createTable());
             updateProPanel(pepCollection.getProteinListPanel(this, pps.getProteins()).createTable());
         }
         if (nodeInfo instanceof ListPanel) {
             ListPanel lp = (ListPanel) nodeInfo;
             msFrame.updateStatusMessage(lp.toString() + " selected");
             if (nodeInfo instanceof PeptideHitListPanel) {
                 updatePepHitPanel(lp.createTable());
                 LeafDock dock = pepHitDockable.getDock();
                 if (dock instanceof TabDock) {
                     ((TabDock) dock).setSelectedDockable(pepHitDockable);
                 }
                 updatePepPanel(pepCollection.getPeptideListPanel(this).createTable());
                 updateProPanel(pepCollection.getProteinListPanel(this).createTable());
             }
             if (nodeInfo instanceof PeptideListPanel) {
                 updatePepPanel(lp.createTable());
                 LeafDock dock = pepDockable.getDock();
                 if (dock instanceof TabDock) {
                     ((TabDock) dock).setSelectedDockable(pepDockable);
                 }
                 updatePepHitPanel(pepCollection.getPeptideHitListPanel(this).createTable());
                 updateProPanel(pepCollection.getProteinListPanel(this).createTable());
             }
             if (nodeInfo instanceof ProteinListPanel) {
                 updateProPanel(lp.createTable());
                 LeafDock dock = proDockable.getDock();
                 if (dock instanceof TabDock) {
                     ((TabDock) dock).setSelectedDockable(proDockable);
                 }
                 updatePepHitPanel(pepCollection.getPeptideHitListPanel(this).createTable());
                 updatePepPanel(pepCollection.getPeptideListPanel(this).createTable());
             }
             updateGraphPanel(new JLabel("No cluster, peptide, or protein selected"));
             updateDetailPanel(new JLabel("No details for this item"));
         }
     }
 
     public void updatePepHitPanel(Component comp) {
         pepHitPanel.removeAll();
         pepHitPanel.add(BorderLayout.CENTER, comp);
         pepHitPanel.validate();
     }
 
     public void updatePepPanel(Component comp) {
         pepPanel.removeAll();
         pepPanel.add(BorderLayout.CENTER, comp);
         pepPanel.validate();
     }
 
     public void updateProPanel(Component comp) {
         proPanel.removeAll();
         proPanel.add(BorderLayout.CENTER, comp);
         proPanel.validate();
     }
 
     public void updateGraphPanel(PeptideCollection pc, String highlight) {
         final Display display = pc.getGraphDisplay(msFrame.getGraphLayout(), this, highlight);
         //graphPanel.getViewport().removeAll();
         graphPanel.setViewportView(display);
         graphPanel.validate();
         new Thread(new Runnable() {
 
             public void run() {
                 try {
                     Thread.sleep(250);
                 } catch (InterruptedException e) {
                 }
                 MouseEvent mEvt = new MouseEvent(display, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis() + 5000, MouseEvent.BUTTON2_MASK, 10, 10, 1, false, MouseEvent.BUTTON2);
                 Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(mEvt);
             }
         }).start();
     }
 
     public void updateGraphPanel(Component comp) {
         graphPanel.setViewportView(comp);
     }
 
     public void updateDetailPanel(Component comp) {
         detailPanel.removeAll();
         detailPanel.add(BorderLayout.CENTER, comp);
         detailPanel.validate();
         if (comp instanceof JScrollPane) {
             JScrollPane jsp = (JScrollPane) comp;
             jsp.revalidate();
         }
     }
 
     public int getDetailHeight() {
         return detailPanel.getHeight();
     }
 
     public int getDetailWidth() {
         return detailPanel.getWidth();
     }
 
     public void showPeptideHit(PeptideHit ph) {
         Peptide p = pepCollection.getMinPeptides().get(ph.getSequence());
 
         // Update peptide table
         PeptideListPanel peptideListPanel = new PeptideListPanel(this);
         ArrayList<Peptide> pepList = new ArrayList<Peptide>();
         pepList.add(p);
         peptideListPanel.addPeptideList(pepList, pepCollection.getExperimentSet());
         updatePepPanel(peptideListPanel.createTable());
 
         // Update protein table
         ProteinListPanel proteinListPanel = new ProteinListPanel(this);
         ArrayList<Protein> proList = new ArrayList<Protein>();
         for (String proName : p.getProteins()) {
             proList.add(pepCollection.getMinProteins().get(proName));
         }
         proteinListPanel.addProteinList(proList, pepCollection.getExperimentSet());
         updateProPanel(proteinListPanel.createTable());
 
         // Update cluster view
         //showCluster(p.getCluster());
         PeptideCollection pc = pepCollection.getCluster(p.getCluster());
         updateGraphPanel(pc, p.getSequence());
         msFrame.updateStatusMessage("Peptide Hit " + p.getSequence() + " (Scan:" + ph.getScanNum() + ", " + ph.getSourceType() + ", Query: " + ph.getQueryNum() + ") selected");
     }
 
     public void showPeptide(Peptide p, boolean updatePepTable) {
         if (updatePepTable) {
             // Update peptide table
             PeptideListPanel peptideListPanel = new PeptideListPanel(this);
             ArrayList<Peptide> pepList = new ArrayList<Peptide>();
             pepList.add(p);
             peptideListPanel.addPeptideList(pepList, pepCollection.getExperimentSet());
             updatePepPanel(peptideListPanel.createTable());
         }
 
         // Update protein table
         ProteinListPanel proteinListPanel = new ProteinListPanel(this);
         ArrayList<Protein> proList = new ArrayList<Protein>();
         for (String proName : p.getProteins()) {
             proList.add(pepCollection.getMinProteins().get(proName));
         }
         proteinListPanel.addProteinList(proList, pepCollection.getExperimentSet());
         updateProPanel(proteinListPanel.createTable());
 
         // update peptide hit table
         PeptideHitListPanel lp = new PeptideHitListPanel(this);
         lp.addProteinPeptideHitList(p.getPeptideHits());
         updatePepHitPanel(lp.createTable());
 
         updateDetailPanel(new JLabel("No Data Availiable"));
 
         msFrame.updateStatusMessage("Peptide " + p.getSequence() + " selected");
     }
 
     public void showProtein(Protein p, boolean updateProTable) {
         if (updateProTable) {
             // Update protein table
             ProteinListPanel proteinListPanel = new ProteinListPanel(this);
             ArrayList<Protein> proList = new ArrayList<Protein>();
             proList.add(p);
             proteinListPanel.addProteinList(proList, pepCollection.getExperimentSet());
             updateProPanel(proteinListPanel.createTable());
         }
 
         // Show protein detail
         JPanel jPanel = new JPanel();
         jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
         JPanel seqPanel;
         if (msFrame.getUseDigest()) {
             seqPanel = p.getSequenceDisplay(msFrame.getDigestName(), detailPanel.getWidth());
         } else {
             seqPanel = p.getSequenceDisplay(detailPanel.getWidth());
         }
         updateDetailPanel(seqPanel);
 
         // update peptide hit table
         PeptideHitListPanel lp = new PeptideHitListPanel(this);
         lp.addProteinPeptideHitList(p.getPeptideHitList());
         updatePepHitPanel(lp.createTable());
 
         // Update peptide table
         PeptideListPanel peptideListPanel = new PeptideListPanel(this);
         peptideListPanel.addPeptideList(p.getAllPeptides(), pepCollection.getExperimentSet());
         updatePepPanel(peptideListPanel.createTable());
 
         msFrame.updateStatusMessage("Protein " + p.getName() + " selected");
     }
 
     public void showCluster(int i) {
         PeptideCollection pc = pepCollection.getCluster(i);
         updateGraphPanel(pc, null);
     }
 
     private void updateDisplay() {
         treeModelOverview = pepCollection.getTree(this);
         jTreeMain.setModel(treeModelOverview);
         jTreeMain.setSelectionRow(0);
         System.err.println("PepCollectionOrig: " + pepCollectionOriginal.getPeptideHits().size());
         System.err.println("PepCollection: " + pepCollection.getPeptideHits().size());
     //System.gc();
     }
 
     public HashMap<String, Protein> getProteins() {
         return pepCollection.getMinProteins();
     }
 
     public PeptideCollection getPepCollection() {
         return pepCollection;
     }
 
     public Frame getParentFrame() {
         return (Frame) msFrame;
     }
 
     public MassSieveFrame getMassSieveFrame() {
         return msFrame;
     }
 
     public FilterSettings getFilterSettings() {
         return filterSettings;
     }
 
     public void setFilterSettings(FilterSettings filterSettings) {
         this.filterSettings = filterSettings;
     }
 
     public ArrayList<FileInformation> getFileInfos() {
         return fileInfos;
     }
 
     public void setFileInfos(ArrayList<FileInformation> fileInfos) {
         this.fileInfos = fileInfos;
     }
 }
