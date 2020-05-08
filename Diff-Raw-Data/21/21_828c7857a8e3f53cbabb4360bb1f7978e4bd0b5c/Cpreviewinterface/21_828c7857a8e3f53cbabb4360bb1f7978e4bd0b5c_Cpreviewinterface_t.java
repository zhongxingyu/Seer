 /* SubmarineX3D (client edition) v 2.1,
  * Copyright (C) 2010-2011 Andrea Nardinocchi [nardinocchi@psychogames.net]
 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package main;
 import basics.Cbasewindow;
 import basics.Cdatafiles;
 import basics.Czebralist;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.io.BufferedWriter;
import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import javax.swing.*;
 import subwindows.Cerrorinterface;
 import tokenizer.Cblock;
 public class Cpreviewinterface extends Cbasewindow {
     /* father's class */
     private Cmaininterface backTrack;
     /* end */
     /* SWING vars */
     private Czebralist collectorList, drawList;
     private DefaultListModel collectorModel, drawModel;
     private JButton drawCheck, undrawCheck, previewSubmit, cancelSubmit;
     private JLabel collectorLabel, drawLabel, descriptionLabel;
     /* end */
     /* semaphores */
     final public Object listLocker = new Object();
     /* end */
     public Cpreviewinterface (Cmaininterface backTrack) {
         super(600, 400, "Preview window", true, false, true);
         this.backTrack = backTrack;
     }
 
     public void initializeComponent () {
         GridBagConstraints positioner = null;
         collectorModel = new DefaultListModel();
         drawModel = new DefaultListModel();
         collectorList = new Czebralist(collectorModel);
         drawList = new Czebralist(drawModel);
         drawCheck = new JButton();
         undrawCheck = new JButton();
         previewSubmit = new JButton();
         cancelSubmit = new JButton();
         collectorLabel = new JLabel();
         drawLabel = new JLabel();
         descriptionLabel = new JLabel();
         collectorLabel.setText("Objects");
         drawLabel.setText("to Render");
         drawCheck.setText("add");
         drawCheck.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 if (collectorList.getSelectedIndex() > -1) {
                     drawModel.addElement(collectorModel.getElementAt(collectorList.getSelectedIndex()));
                     collectorModel.remove(collectorList.getSelectedIndex());
                 }
             }
         });
         undrawCheck.setText("remove");
         undrawCheck.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 if (drawList.getSelectedIndex() > -1) {
                     collectorModel.addElement(drawModel.getElementAt(drawList.getSelectedIndex()));
                     drawModel.remove(drawList.getSelectedIndex());
                 }
             }
         });
         previewSubmit.setText("preview");
         previewSubmit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 previewTags();
             }
         });
         cancelSubmit.setText("cancel");
         cancelSubmit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 formWindowClosing();
             }
         });
         descriptionLabel.setFont(new java.awt.Font("Lucida Grande", 0, 10));
         descriptionLabel.setText("Only the segments of code between the tag shape in \"to Render\" panel will be blitted on the environment");
         JScrollPane collectorScroll = new JScrollPane(collectorList);
         collectorScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         collectorScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         collectorScroll.setMaximumSize(new Dimension(230, 3500));
         collectorScroll.setMinimumSize(new Dimension(230, 240));
         collectorScroll.setPreferredSize(new Dimension(230, 240));
         JScrollPane drawScroll = new JScrollPane(drawList);
         drawScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         drawScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         drawScroll.setMaximumSize(new Dimension(230, 3500));
         drawScroll.setMinimumSize(new Dimension(230, 240));
         drawScroll.setPreferredSize(new Dimension(230, 240));
         JPanel container = new JPanel(new GridBagLayout());
         container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         /* blitting */
         this.add(container);
         positioner = new GridBagConstraints();
         positioner.gridx = positioner.gridy = 0;
         positioner.anchor = GridBagConstraints.WEST;
         container.add(collectorLabel, positioner);
         positioner = new GridBagConstraints();
         positioner.gridx = 2;
         positioner.gridy = 0;
         positioner.anchor = GridBagConstraints.WEST;
         container.add(drawLabel, positioner);
         positioner = new GridBagConstraints();
         positioner.gridx = 0;
         positioner.gridy = 1;
         positioner.gridheight = 4;
         positioner.weightx = positioner.weighty = 1.0;
         positioner.anchor = GridBagConstraints.WEST;
         positioner.fill = GridBagConstraints.BOTH;
         container.add(collectorScroll, positioner);
         positioner = new GridBagConstraints();
         positioner.gridx = 2;
         positioner.gridy = 1;
         positioner.gridheight = 4;
         positioner.weightx = positioner.weighty = 1.0;
         positioner.anchor = GridBagConstraints.WEST;
         positioner.fill = GridBagConstraints.BOTH;
         container.add(drawScroll, positioner);
         positioner = new GridBagConstraints();
         positioner.gridx = 1;
         positioner.gridy = 2;
         positioner.weightx = 1.0;
         positioner.anchor = GridBagConstraints.CENTER;
         positioner.fill = GridBagConstraints.HORIZONTAL;
         container.add(drawCheck, positioner);
         positioner = new GridBagConstraints();
         positioner.gridx = 1;
         positioner.gridy = 3;
         positioner.weightx = 1.0;
         positioner.anchor = GridBagConstraints.CENTER;
         positioner.fill = GridBagConstraints.HORIZONTAL;
         container.add(undrawCheck, positioner);
         positioner = new GridBagConstraints();
         positioner.gridx = 2;
         positioner.gridy = 5;
         positioner.weightx = 1.0;
         positioner.anchor = GridBagConstraints.CENTER;
         positioner.fill = GridBagConstraints.HORIZONTAL;
         container.add(previewSubmit, positioner);
         positioner = new GridBagConstraints();
         positioner.gridx = 2;
         positioner.gridy = 6;
         positioner.weightx = 1.0;
         positioner.anchor = GridBagConstraints.CENTER;
         positioner.fill = GridBagConstraints.HORIZONTAL;
         container.add(cancelSubmit, positioner);
         positioner = new GridBagConstraints();
         positioner.gridx = 0;
         positioner.gridy = 7;
         positioner.gridwidth = 3;
         positioner.anchor = GridBagConstraints.WEST;
         container.add(descriptionLabel, positioner);
         this.pack();
     }
 
     public void loadTags (ArrayList<Cblock> blockList) {
         Cblock backupBlock = null;
         Ctabinterface singleSource = null;
         int initialPosition = -1, finalPosition = -1, backupPosition = 0;
         String backupString = new String(), singleString = new String();
         synchronized (listLocker) {
             collectorModel.clear();
             drawModel.clear();
             if (blockList != null) {
                 Iterator<Cblock> listIterator = blockList.iterator();
                 synchronized (backTrack.tabbedPaneLocker) {
                     singleSource = (Ctabinterface) backTrack.tabbedPane.getSelectedComponent();
                 }
                 while (listIterator.hasNext()) {
                     backupBlock = listIterator.next();
                     if (backupBlock.tagName.equalsIgnoreCase("shape")) {
                         initialPosition = backupBlock.beginPosition;
                         finalPosition = backupBlock.finalPosition;
                     } else if (backupBlock.tagName.equalsIgnoreCase("transform")) {
                         backupPosition = backupBlock.beginPosition;
                         backupString = "";
                         try {
                             do {
                                 singleString = singleSource.mainView.getDocument().getText(backupPosition, 1);
                                 backupString += singleString;
                                 backupPosition++;
                             } while ((!singleString.equalsIgnoreCase(">")) && (backupPosition < singleSource.mainView.getDocument().getLength()));
                         } catch (Exception exc) {
                             Cerrorinterface bck = new Cerrorinterface("DOCUMEERR", true);
                         }
                     } else if (backupBlock.defName != null) {
                         if ((initialPosition < 0) || (finalPosition < 0) || (finalPosition < backupBlock.finalPosition)) {
                             initialPosition = backupBlock.beginPosition;
                             finalPosition = backupBlock.finalPosition;
                         }
                         // Selecting only graphic nodes.
                         if ((backupBlock.tagName.equalsIgnoreCase("arc2d")) || (backupBlock.tagName.equalsIgnoreCase("arcclose2d")) ||
                                 (backupBlock.tagName.equalsIgnoreCase("circle2d")) || (backupBlock.tagName.equalsIgnoreCase("disk2d")) ||
                                     (backupBlock.tagName.equalsIgnoreCase("polyline2d")) || (backupBlock.tagName.equalsIgnoreCase("polypoint2d")) ||
                                         (backupBlock.tagName.equalsIgnoreCase("triangleset2d")) || (backupBlock.tagName.equalsIgnoreCase("box")) ||
                                 (backupBlock.tagName.equalsIgnoreCase("cone")) || (backupBlock.tagName.equalsIgnoreCase("cylinder")) ||
                                     (backupBlock.tagName.equalsIgnoreCase("sphere")) || (backupBlock.tagName.equalsIgnoreCase("elevationgrid")) ||
                                         (backupBlock.tagName.equalsIgnoreCase("text")) || (backupBlock.tagName.equalsIgnoreCase("trianglefanset")) ||
                                 (backupBlock.tagName.equalsIgnoreCase("triangleset")) || (backupBlock.tagName.equalsIgnoreCase("trianglestripset")) ||
                                     (backupBlock.tagName.equalsIgnoreCase("indexedfaceset"))) {
                             collectorModel.addElement(backupBlock.defName+" (tag "+backupBlock.tagName+")|"+initialPosition+"-"+finalPosition+"|"+backupString);
                         }
                         initialPosition = finalPosition = -1;
                     }
                 }
             }
         }
     }
 
     public void previewTags () {
         BufferedWriter outputFile;
         Ctabinterface singleSource = null;
        File link;
        String fileName, tempFileName, informations[], coords[], newStructure = "<?xml version='1.0' encoding='UTF-8'?>\n<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.1//EN\" \"http://www.web3d.org/specifications/x3d-3.1.dtd\">\n<X3D profile='Immersive' version='3.0'>\n<Head>\n</Head>\n<Scene>\n\t<NavigationInfo type='\"EXAMINE\",\"ANY\"'></NavigationInfo>\n";
         synchronized (listLocker) {
             synchronized (backTrack.tabbedPaneLocker) {
                 singleSource = (Ctabinterface) backTrack.tabbedPane.getSelectedComponent();
             }
             for (int index = 0; index < drawModel.getSize(); index++) {
                 informations = ((String)drawModel.getElementAt(index)).split("\\|");
                 coords = informations[1].split("-");
                 if (informations[2].length() > 0) { newStructure += informations[2]+"\n"; }
                 try {
                     newStructure += singleSource.mainView.getDocument().getText(Integer.valueOf(coords[0]), (Integer.valueOf(coords[1]) - Integer.valueOf(coords[0]))+1);
                 } catch (Exception exc) {
                     Cerrorinterface bck = new Cerrorinterface("DOCUMEERR", true);
                 }
                 if (informations[2].length() > 0) { newStructure += "</Transform>\n"; }
             }
         }
         newStructure += "\n</Scene>\n</X3D>\n";
         try {
            synchronized (backTrack.tabbedPaneLocker) {
                singleSource = (Ctabinterface) backTrack.tabbedPane.getSelectedComponent();
            }
            synchronized (singleSource.sourceNameLocker) {
                fileName = singleSource.sourceName;
            }
            link = new File(fileName);
            tempFileName = link.getAbsoluteFile().getParentFile().getAbsolutePath()+
                    File.separator+".tmp"+link.getName();
            System.out.println(tempFileName);
            outputFile = new BufferedWriter(new FileWriter(tempFileName));
             outputFile.write(newStructure);
             outputFile.newLine();
             outputFile.close();
            backTrack.previewFile(tempFileName);
         } catch (Exception ext) {
             Cerrorinterface bck = new Cerrorinterface("PREVIEERR", false);
         }
         this.formWindowClosing();
     }
 
     /* window's event catched */
     public void formWindowClosing() {
         this.setVisible(false);
         backTrack.setEnabled(true);
         backTrack.setFocusable(true);
     }
 }
