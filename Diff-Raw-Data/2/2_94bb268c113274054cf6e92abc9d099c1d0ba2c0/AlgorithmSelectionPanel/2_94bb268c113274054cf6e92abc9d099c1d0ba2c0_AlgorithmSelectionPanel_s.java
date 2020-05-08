 package org.geworkbench.util;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Algorithm selection panel
  * <p>Title: Bioworks</p>
  * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
  * <p>Copyright: Copyright (c) 2003 -2004</p>
  * <p>Company: Columbia University</p>
  *
  */
 public class AlgorithmSelectionPanel extends JPanel {
     //algorithm names
     public static final String DISCOVER = "discovery";
     public static final String EXHAUSTIVE = "exhaustive";
     public static final String HIERARCHICAL = "hierarchical";
     ButtonGroup algorithmGroup = new ButtonGroup();
     JRadioButton discovery = new JRadioButton();
     JRadioButton hierarc = new JRadioButton();
     JRadioButton exhaustive = new JRadioButton();
     FlowLayout flowLayout1 = new FlowLayout();
 
     public AlgorithmSelectionPanel() throws Exception {
         try {
             jbInit();
         } catch (Exception ex) {
         }
     }
 
     public void jbInit() {
         initAlgorithmType();
         this.setBorder(null);
         setMaximumSize(new Dimension(270, 20));
         setMinimumSize(new Dimension(270, 20));
         setPreferredSize(new Dimension(270, 20));
         this.setLayout(flowLayout1);
         exhaustive.setBorder(null);
         hierarc.setBorder(null);
         discovery.setBorder(null);
         this.add(discovery, null);
         // remove the hierarchical clustering option for geWorkbencg 1.6 release 
         //this.add(hierarc, null);
         this.add(exhaustive, null);
     }
 
     /**
      * Initialize algorithm Radio button
      */
     private void initAlgorithmType() {
         discovery.setText("Norm.");
         discovery.setActionCommand(DISCOVER);
         hierarc.setText("Hierarch.");
         hierarc.setActionCommand(HIERARCHICAL);
        exhaustive.setText("Exhhaust.");
         exhaustive.setActionCommand(EXHAUSTIVE);
         discovery.setSelected(true);
 
         algorithmGroup.add(discovery);
         algorithmGroup.add(exhaustive);
         algorithmGroup.add(hierarc);
 
         add(discovery);
         // remove the hierarchical clustering option for geWorkbencg 1.6 release 
         //add(hierarc);
         add(exhaustive);
     }
 
     public String getSelectedAlgorithmName() {
         return algorithmGroup.getSelection().getActionCommand();
     }
 
     public void setSelectedAlgorithm(String algorithmDescription) {
         if (algorithmDescription.equalsIgnoreCase(DISCOVER)) {
             discovery.setSelected(true);
         } else if (algorithmDescription.equalsIgnoreCase(EXHAUSTIVE)) {
             exhaustive.setSelected(true);
         } else if (algorithmDescription.equalsIgnoreCase(HIERARCHICAL)) {
             hierarc.setSelected(true);
         }
     }
 }
