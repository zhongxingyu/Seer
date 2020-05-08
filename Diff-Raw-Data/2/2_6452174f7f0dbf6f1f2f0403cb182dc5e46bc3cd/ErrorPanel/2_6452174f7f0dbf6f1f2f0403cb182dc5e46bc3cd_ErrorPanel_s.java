 /*
  * Copyright 2000-2003 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
  *
  * "This product includes software developed by Oracle, Inc. and the National Cancer Institute."
  *
  * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself, wherever such third-party acknowledgments normally appear.
  *
  * 3. The names "The National Cancer Institute", "NCI" and "Oracle" must not be used to endorse or promote products derived from this software.
  *
  * 4. This license does not authorize the incorporation of this software into any proprietary programs. This license does not authorize the recipient to use any trademarks owned by either NCI or Oracle, Inc.
  *
  * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, ORACLE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
  */
 package gov.nih.nci.ncicb.cadsr.loader.ui;
 
 import gov.nih.nci.ncicb.cadsr.loader.ui.util.TreeUtil;
 
 import gov.nih.nci.ncicb.cadsr.loader.util.UserPreferences;
 import java.io.File;
 import java.io.FileWriter;
 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import gov.nih.nci.ncicb.cadsr.loader.validator.*;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.*;
 
 import java.util.*;
 
 import javax.swing.tree.DefaultTreeCellRenderer;
 
 import org.apache.log4j.spi.LoggingEvent;
 import org.jdom.*;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 /**
  * The error viewer.
  *
  * @author Anwar Ahmad
  */
 public class ErrorPanel extends JPanel implements MouseListener {
 
   private JTree tree;
   private Set<UMLNode> displaySet = new HashSet<UMLNode>();
   private JPopupMenu popup;
   
   private JCheckBoxMenuItem conceptCb = new JCheckBoxMenuItem("Hide Concept Errors", false);
   private boolean hideConceptError = false;
   private UMLNode node;
   private JPanel cbPanel;
   
   private JLabel infoLabel = new JLabel(" ");
 
   public ErrorPanel(UMLNode rootNode) {
     node = rootNode;
     initCb();
     initUI(rootNode);
   }
   
     public void update(UMLNode rootNode) {
         node = rootNode;
         this.removeAll();
         initUI(rootNode);
         this.updateUI();
     }
     
     private void update() {
         update(node);
     }    
 
 
     private void initUI(UMLNode rootNode) {
         displaySet.clear();
         firstRun(rootNode);
 
         DefaultMutableTreeNode node = buildTree(rootNode);
 
         //create tree and make root not visible
         tree = new JTree(node);
         tree.setRootVisible(false);
         tree.setShowsRootHandles(true);
 
         //Traverse Tree expanding all nodes
         TreeUtil.expandAll(tree, node);
 
         tree.setCellRenderer(new UMLTreeCellRenderer());
         tree.addMouseListener(this);
 
         this.setLayout(new BorderLayout());
 
         JScrollPane scrollPane = new JScrollPane(tree);
         this.setPreferredSize(new Dimension(450, 110));
         this.add(scrollPane, BorderLayout.CENTER);
         this.add(infoLabel, BorderLayout.SOUTH);
 
         buildPopupMenu();
 
 //         add(cbPanel, BorderLayout.EAST);
     }
 
     private void initCb() {
 
       
       
     }
     
 
     public void mousePressed(MouseEvent e) {
         showPopup(e);
     }
 
     public void mouseExited(MouseEvent e) {
     }
 
     public void mouseClicked(MouseEvent e) {
     }
 
     public void mouseEntered(MouseEvent e) {
     }
 
     public void mouseReleased(MouseEvent e) {
         showPopup(e);
     }
 
     private void showPopup(MouseEvent e) {
         if (e.isPopupTrigger()) {
             popup.show(e.getComponent(), e.getX(), e.getY());
         }
     }
 
     private void buildPopupMenu() {
       popup = new JPopupMenu();
       JMenuItem menuItem = new JMenuItem("Export Errors");
       popup.add(menuItem);
       popup.addSeparator();
       popup.add(conceptCb);
 
       ActionListener cbAl = new ActionListener() {
           public void actionPerformed(ActionEvent evt) {
             AbstractButton cb = (AbstractButton)evt.getSource();
             boolean isSel = cb.isSelected();
             if (isSel) {
               hideConceptError = true;
             } else {
               hideConceptError = false;
             }
             update();
           }
         };
 
       conceptCb.addActionListener(cbAl);
       
       menuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           String saveDir = UserPreferences.getInstance().getRecentDir();
           JFileChooser chooser = new JFileChooser();
           javax.swing.filechooser.FileFilter filter = 
             new javax.swing.filechooser.FileFilter() {
               String fileExtension = "xml";
               
               public boolean accept(File f) {
                 if (f.isDirectory()) {
                   return true;
                 }                
                 return f.getName().endsWith("." + fileExtension);
               }
               public String getDescription() {
                 return fileExtension.toUpperCase() + " Files";
               }
             };
             
           chooser.setFileFilter(filter);
           int returnVal = chooser.showSaveDialog(null);
           if(returnVal == JFileChooser.APPROVE_OPTION) {
             String filePath = chooser.getSelectedFile().getAbsolutePath(); 
             //export here
             //writeXML();
             String fileExtension = "xml";
             if(!filePath.endsWith(fileExtension))
               filePath = filePath + "." + fileExtension;
           try 
           {
             FileWriter fw = new FileWriter(filePath);
           
             new XMLOutputter(Format.getPrettyFormat()).output(writeXML(filePath), fw);
             infoLabel.setText("File Exported");
           }
           catch (Exception e) 
           {
            infoLabel.setText("Save Failed!!");
             throw new RuntimeException("Error writing to " + filePath,  e);
           }            
         }
         }
       });
 }
     private void firstRun(UMLNode node) {
         Set<UMLNode> children = node.getChildren();
         Set<ValidationNode> valNodes = node.getValidationNodes();
         //     displaySet = new HashSet<UMLNode>();
 
         for (ValidationNode valNode: valNodes) {
             if (!(hideConceptError && valNode.getUserObject() instanceof ValidationConceptError)) {
                 navTree(valNode);
             }
         }
 
         for (UMLNode child: children) {
             firstRun(child);
         }
     }
 
     private void navTree(UMLNode node) {
         UMLNode pNode = node.getParent();
         if (pNode != null) {
             navTree(pNode);
         }
         displaySet.add(node);
     }
 
     private DefaultMutableTreeNode buildTree(UMLNode rootNode) {
         DefaultMutableTreeNode node = new DefaultMutableTreeNode(rootNode);
 
         return doNode(node);
     }
 
     private Element writeXML(String filePath) {
       Element rootElement = new Element("File");
       rootElement.setAttribute("name", filePath);
       doNode(rootElement, node);
       return rootElement;
     }
     
     private void doNode(Element element, UMLNode node) 
     {
       Set<UMLNode> children = node.getChildren();
       Set<ValidationNode> valNodes = node.getValidationNodes();
       
       for (ValidationNode valNode: valNodes) {
         String elementName = "ValidationError";
         if(valNode instanceof WarningNode)
           elementName = "ValidationWarning";
           
         Element validationElement = new Element(elementName);
         validationElement.addContent(valNode.getDisplay());
         element.addContent(validationElement);
       }
         
       for (UMLNode child: children) {
         //DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(child);
         Element childElement = new Element("child");
         if(child instanceof PackageNode) {
           childElement = new Element("Package");
           childElement.setAttribute("name", child.getDisplay());
         }
         if(child instanceof ClassNode) {
           childElement = new Element("Class");
           childElement.setAttribute("name", child.getDisplay());
         }
         if(child instanceof AttributeNode) {
           childElement = new Element("Attribute");
           childElement.setAttribute("name", child.getDisplay());
         }
         
         if (displaySet.contains(child)) {   
           element.addContent(childElement);
           //node.add(newNode);
         }
           doNode(childElement, child);
         }
 
     }
 
 
     private DefaultMutableTreeNode doNode(DefaultMutableTreeNode node) {
         UMLNode umlNode = (UMLNode)node.getUserObject();
 
         Set<UMLNode> children = umlNode.getChildren();
         Set<ValidationNode> valNodes = umlNode.getValidationNodes();
 
         for (ValidationNode valNode: valNodes) {
             if (!(hideConceptError && valNode.getUserObject() instanceof ValidationConceptError)) {
                 DefaultMutableTreeNode newNode = 
                     new DefaultMutableTreeNode(valNode);
 
                 node.add(newNode);
             }
 
         }
         for (UMLNode child: children) {
             DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(child);
 
             if (displaySet.contains(child))
                 node.add(newNode);
 
             doNode(newNode);
         }
 
         return node;
 
     }
 
 
 }
 
