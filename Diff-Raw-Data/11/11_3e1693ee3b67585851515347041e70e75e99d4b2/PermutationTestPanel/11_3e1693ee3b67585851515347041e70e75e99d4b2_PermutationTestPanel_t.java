 package edu.mit.wi.haploview.association;
 
 import edu.mit.wi.haploview.Constants;
 import edu.mit.wi.haploview.NumberTextField;
 import edu.mit.wi.haploview.BasicTableModel;
 
 import javax.swing.*;
 import javax.swing.table.TableModel;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.*;
 import java.util.Vector;
 import java.io.File;
 import java.io.IOException;
 
 import org.jfree.data.statistics.HistogramDataset;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 
 public class PermutationTestPanel extends JPanel implements Constants,ActionListener {
     private JLabel bestPermutationValueLabel;
     private JLabel blocksChangedLabel;
     private NumberTextField permCountField;
 
     private JProgressBar permProgressBar;
     private JButton doPermutationsButton;
     private JButton stopPermutationsButton;
 
     private PermutationTestSet testSet;
     private PermutationThread permThread;
     private ProgressBarUpdater progressUpdater;
     private Vector colNames;
     private JPanel resultsPanel;
     private JLabel bestObsValueLabel;
 
     private JLabel scoreBoardNumPassLabel;
     private JLabel scoreBoardNumTotalLabel;
     private JPanel scoreBoardPanel;
     private boolean finishedPerms;
     private JPanel bestObsPanel;
     private JPanel bestPermPanel;
     private ButtonGroup selectionGroup;
 
     public PermutationTestPanel(PermutationTestSet pts) {
         if(pts == null) {
             throw new NullPointerException();
         }
 
         testSet = pts;
 
         this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
 
         JPanel permCountPanel = new JPanel();
 
         JPanel selectionPanel = new JPanel();
         selectionGroup = new ButtonGroup();
         selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
         JRadioButton singleOnlyButton = new JRadioButton("Single Markers Only");
         singleOnlyButton.setActionCommand(String.valueOf(PermutationTestSet.SINGLE_ONLY));
         selectionPanel.add(singleOnlyButton);
         selectionGroup.add(singleOnlyButton);
         JRadioButton singlesPlusBlocksButton = new JRadioButton("Single Markers and Haplotypes in Blocks");
         singlesPlusBlocksButton.setActionCommand(String.valueOf(PermutationTestSet.SINGLE_PLUS_BLOCKS));
         selectionPanel.add(singlesPlusBlocksButton);
         selectionGroup.add(singlesPlusBlocksButton);
         singlesPlusBlocksButton.setSelected(true);
         if (testSet.isCustom()){
             JRadioButton customFileButton = new JRadioButton("Custom Tests from File");
             customFileButton.setActionCommand(String.valueOf(PermutationTestSet.CUSTOM));
             selectionPanel.add(customFileButton);
             selectionGroup.add(customFileButton);
             customFileButton.setSelected(true);
         }
         permCountPanel.add(selectionPanel);
 
         JLabel permCountTextLabel = new JLabel("Number of Permutations To Perform: ");
         permCountField = new NumberTextField("", 10, false);
         permCountPanel.add(permCountTextLabel);
         permCountPanel.add(permCountField);
 
         permCountPanel.setMaximumSize(permCountPanel.getPreferredSize());
         this.add(permCountPanel);
 
 
         JPanel buttonPanel = new JPanel();
         doPermutationsButton = new JButton("Do Permutations");
         doPermutationsButton.addActionListener(this);
         stopPermutationsButton = new JButton("Stop");
         stopPermutationsButton.addActionListener(this);
         stopPermutationsButton.setEnabled(false);
         buttonPanel.add(doPermutationsButton);
         buttonPanel.add(stopPermutationsButton);
         buttonPanel.setMaximumSize(buttonPanel.getPreferredSize());
         this.add(buttonPanel);
 
         bestObsPanel = new JPanel();
         JLabel bestObsTextLabel = new JLabel("Best Observed Chi-Square: ");
         bestObsValueLabel = new JLabel("");
         bestObsPanel.add(bestObsTextLabel);
         bestObsPanel.add(bestObsValueLabel);
        bestObsPanel.setMaximumSize(new Dimension(400,bestObsPanel.getPreferredSize().height));
         this.add(bestObsPanel);
 
         bestPermPanel = new JPanel();
         JLabel bestPermTextLabel = new JLabel("Best Permutation Chi-Square: ");
         bestPermutationValueLabel = new JLabel("");
         bestPermPanel.add(bestPermTextLabel);
         bestPermPanel.add(bestPermutationValueLabel);
        bestPermPanel.setMaximumSize(new Dimension(400,bestPermPanel.getPreferredSize().height));        
         this.add(bestPermPanel);
 
 
         scoreBoardPanel = new JPanel();
         scoreBoardNumPassLabel = new JLabel();
         scoreBoardNumTotalLabel = new JLabel();
         scoreBoardPanel.add(scoreBoardNumPassLabel);
         scoreBoardPanel.add(new JLabel("permutations out of"));
         scoreBoardPanel.add(scoreBoardNumTotalLabel);
         scoreBoardPanel.add(new JLabel("exceed highest observed chi square."));
         scoreBoardPanel.setMaximumSize(new Dimension(500,60));
         scoreBoardPanel.setVisible(false);
         add(scoreBoardPanel);
 
         colNames = new Vector();
         colNames.add("Name");
         colNames.add("Chi Square");
         colNames.add("Permutation p-value");
 
         blocksChangedLabel = new JLabel("The current blocks may have changed, so these values may not be accurate!");
         blocksChangedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
         blocksChangedLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
         blocksChangedLabel.setForeground(Color.RED);
 
     }
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if(command.equals("Do Permutations") ) {
             startPerms();
         }else if(command.equals("Stop")) {
             stopPerms();
         }
     }
 
     public void setTestSet(PermutationTestSet pts){
         testSet = pts;
     }
 
     public void startPerms() {
         if (permCountField.getText().equals("") || Integer.parseInt(permCountField.getText()) < 1){
             JOptionPane.showMessageDialog(this,
                     "Please specify a non-zero number of permutations.",
                     "Number of Permutations?",
                     JOptionPane.INFORMATION_MESSAGE);
             return;
         }
         scoreBoardPanel.setVisible(true);
         finishedPerms = false;
 
         if (resultsPanel != null){
             remove(resultsPanel);
             repaint();
         }
         this.remove(blocksChangedLabel);
 
         bestPermutationValueLabel.setText("");
 
         testSet.setPermutationCount(Integer.parseInt(permCountField.getText()));
 
         permThread = new PermutationThread(testSet);
 
         doPermutationsButton.setEnabled(false);
         stopPermutationsButton.setEnabled(true);
 
         permProgressBar = new JProgressBar(0,testSet.getPermutationCount());
         permProgressBar.setMaximumSize(new Dimension(200,30));
         permProgressBar.setStringPainted(true);
         this.add(permProgressBar);
 
         permThread.start();
 
         progressUpdater = new ProgressBarUpdater();
         progressUpdater.start();
     }
 
     public void stopPerms() {
         testSet.stopProcessing = true;
         progressUpdater.interrupt();
     }
 
     public void finishedPerms() {
         scoreBoardNumTotalLabel.setText(String.valueOf(testSet.getPermutationsPerformed()));
         scoreBoardNumPassLabel.setText(String.valueOf(testSet.getBestExceededCount()));
         doPermutationsButton.setEnabled(true);
         stopPermutationsButton.setEnabled(false);
         this.remove(permProgressBar);
         bestObsValueLabel.setText(testSet.getBestObsChiSq() + " (" + testSet.getBestObsName() + ")");
         bestPermutationValueLabel.setText(String.valueOf(testSet.getBestPermChiSquare()));
         makeTable();
         resultsPanel.revalidate();
         finishedPerms = true;
     }
 
     public void makeTable() {
         Vector tableData = testSet.getResults();
         TableModel tm = new BasicTableModel(colNames, tableData);
         JTable jt = new JTable(tm);
         JScrollPane tableScroller = new JScrollPane(jt);
         tableScroller.setMaximumSize(tableScroller.getPreferredSize());
         resultsPanel = new JPanel();
         resultsPanel.setLayout(new BoxLayout(resultsPanel,BoxLayout.Y_AXIS));
         resultsPanel.add(tableScroller);
 
         resultsPanel.add(Box.createRigidArea(new Dimension(0,5)));
 
         HistogramDataset resHist = new HistogramDataset();
         resHist.addSeries("Chi Squares", testSet.getPermBestChiSq(),100);
         JFreeChart jfc =  ChartFactory.createHistogram(null,
                 "Chi Square",
                 "Number Permutations",resHist,PlotOrientation.VERTICAL,false,false,false);
         jfc.setBorderVisible(true);
         XYPlot xyp = jfc.getXYPlot();
         xyp.getRenderer().setPaint(Color.blue);
         ChartPanel cp = new ChartPanel(jfc);
         cp.setMaximumSize(new Dimension(400, cp.getPreferredSize().height));
         resultsPanel.add(cp);
         add(resultsPanel);
     }
 
     public void setBlocksChanged() {
         if (resultsPanel != null){
             this.add(blocksChangedLabel);
         }
     }
 
     public void export(File outfile) throws IOException{
         //if a crazy user tries to export the data when the perms are running,
         //we have a problem. So this stops the perms and waits for it to finish up
         //before writing the output.
         if (testSet.getPermutationCount() != testSet.getPermutationsPerformed()){
             stopPerms();
 
             //don't let it run off forever!
             while (!finishedPerms){
                 try{
                     Thread.sleep(100);
                 }catch (InterruptedException e){
                 }
             }
         }
         testSet.writeResultsToFile(outfile);
     }
 
     private class PermutationThread extends Thread{
         PermutationTestSet testSet;
 
         public PermutationThread(PermutationTestSet pts) {
             testSet = pts;
         }
 
         public void run() {
             testSet.doPermutations(Integer.valueOf(selectionGroup.getSelection().getActionCommand()).intValue());
             finishedPerms();
         }
     }
 
     public PermutationTestSet getTestSet() {
         return testSet;
     }
 
     private class ProgressBarUpdater extends Thread{
         public void run() {
             try {
                 while(testSet.getPermutationCount() - testSet.getPermutationsPerformed() != 0) {
                     permProgressBar.setValue(testSet.getPermutationsPerformed());
                     scoreBoardNumTotalLabel.setText(String.valueOf(testSet.getPermutationsPerformed()));
                     scoreBoardNumPassLabel.setText(String.valueOf(testSet.getBestExceededCount()));
                     bestPermutationValueLabel.setText(String.valueOf(testSet.getBestPermChiSquare()));
                     sleep(200);
                 }
             } catch(InterruptedException ie) {}
         }
     }
 }
