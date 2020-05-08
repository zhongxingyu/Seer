 package ru.spbau.bioinf.tagfinder.ui;
 
 import edu.ucsd.msalign.align.prsm.PrSM;
 import java.awt.Container;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Map;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingWorker;
 import ru.spbau.bioinf.tagfinder.Configuration;
 import ru.spbau.bioinf.tagfinder.EValueAdapter;
 import ru.spbau.bioinf.tagfinder.Protein;
 
 import java.util.List;
 import ru.spbau.bioinf.tagfinder.Scan;
 
 public class TagFinder extends JFrame {
 
     private Configuration conf;
     private List<Protein> proteins;
     private Map<Integer,Integer> msAlignResults;
     private Map<Integer,Scan> scans;
     private JTabbedPane tabs;
     private List<JPanel> tabsList = new ArrayList<JPanel>();
     private JLabel status =  new JLabel();
 
     public TagFinder(String[] args) throws Exception {
         super("TagFinder");
         conf = new Configuration(args);
         proteins = conf.getProteins();
         msAlignResults = conf.getMSAlignResults();
         updateStatus("Initializing E-value calculator...");
         final SwingWorker sw = new SwingWorker() {
             @Override
             protected Object doInBackground() throws Exception {
                 EValueAdapter.init(conf);
                 return null;
             }
 
             @Override
             protected void done() {
                 updateStatus("E-Value calculator initialized.");
             }
         };
         new Thread(new Runnable() {
             public void run() {
                 sw.execute();
             }
         }).start();
         System.out.println("sw started");
 
         tabs = new JTabbedPane();
         scans = conf.getScans();
         JPanel scanPanel = new ScanPanel(conf, scans, proteins, msAlignResults, this);
         addTab("Scan", scanPanel);
         JPanel proteinPanel = new ProteinPanel(proteins, this);
         addTab("Protein", proteinPanel);
         Container contentPane = this.getContentPane();
         GridBagLayout layout = new GridBagLayout();
         contentPane.setLayout(layout);
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.fill = GridBagConstraints.BOTH;
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.weightx = 1;
         gbc.weighty = 1;
         contentPane.add(tabs, gbc);
         JMenuBar menubar = new JMenuBar();
         JMenu scanMenu = new JMenu("Scan");
         scanMenu.setMnemonic(KeyEvent.VK_S);
         JMenuItem evalue = new JMenuItem("E-Value");
         evalue.setMnemonic(KeyEvent.VK_E);
         evalue.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_E, ActionEvent.CTRL_MASK));
 
 
         evalue.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 final JPanel panel = tabsList.get(tabs.getSelectedIndex());
                 if (panel instanceof ScanPanel) {
                     updateStatus("Computing E-value...");
                     new SwingWorker() {
 
                         private PrSM[][][] prsms;
                         private ScanPanel scanPanel;
 
                         @Override
                         protected Object doInBackground() throws Exception {
                             scanPanel = (ScanPanel) panel;
                             prsms = scanPanel.calculateEValue();
                             return null;
                         }
 
                         @Override
                         protected void done() {
                             double bestEvalue = -1;
                             String text = "";
                             if (prsms != null) {
                                 for (int i = 0; i < prsms.length; i++) {
                                     if (prsms[i] != null) {
                                         for (int j = 0; j < 4; j++) {
                                             if (prsms[i][j] != null) {
                                                 for (int k = 0; k < prsms[i][j].length; k++) {
                                                     if (prsms[i][j][k] != null) {
                                                         PrSM prsm = prsms[i][j][k];
                                                         double eValue = prsm.getEValue();
                                                         if (bestEvalue < 0 || bestEvalue > eValue) {
                                                            bestEvalue = eValue;
                                                             text = "Scan " + scanPanel.getScanId() + " protein " + scanPanel.getProteinId() + " shift " + i + " alignment type " + j + " score " + prsm.getUniqueScr() + " E-value " + eValue;
                                                         }
                                                     }
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                             updateStatus(text);
                         }
                     }.execute();
                 }
             }
         });
         JMenuItem reduce = new JMenuItem("Reduce");
         reduce.setMnemonic(KeyEvent.VK_R);
         reduce.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_R, ActionEvent.CTRL_MASK));
         reduce.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 JPanel panel = tabsList.get(tabs.getSelectedIndex());
                 if (panel instanceof ScanPanel) {
                     Scan reducedScan = ((ScanPanel) panel).createReducedScan();
                     if (reducedScan != null) {
                         addScanTab(reducedScan);
                     }
                 }
             }
         });
 
         scanMenu.add(evalue);
         scanMenu.add(reduce);
 
         menubar.add(scanMenu);
 
         setJMenuBar(menubar);
 
         gbc.fill = GridBagConstraints.NONE;
         gbc.gridy++;
         gbc.weightx = 0;
         gbc.weighty = 0;
         gbc.anchor = GridBagConstraints.LINE_START;
         contentPane.add(status, gbc);
     }
 
     private void addTab(String name, JPanel scanPanel) {
         tabs.addTab(name, scanPanel);
         tabsList.add(scanPanel);
     }
 
     public void updateStatus(final String text) {
         status.setText(text);
     }
 
     public void addScanTab(Scan scan) {
         ScanPanel scanPanel = new ScanPanel(conf, scans, proteins, msAlignResults, this);
         scanPanel.setScan(scan);
         addTab(scan.getName(), scanPanel);
     }
 
     public static void main(String[] args) throws Exception {
         TagFinder frame = new TagFinder(args);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(1000, 500);
         frame.setVisible(true);
     }
 
 }
