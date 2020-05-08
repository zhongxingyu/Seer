 /* SubmarineX3D v 2.0,
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
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.event.TreeSelectionEvent;
 import subwindows.explorer.Cdirectorymodel;
 import subwindows.explorer.Cfilesystem;
 import subwindows.explorer.Cfilesystemmodel;
 public class Cfileinterface extends JPanel {
     /* father's class */
     private Cmaininterface backTrack;
     private Cdirectorymodel directoryModel;
     /* end */
     /* SWING vars */
     private Cfilesystem fileTree;
     private JButton refreshButton;
     private JScrollPane container;
     /* end */
     public Cfileinterface (Cmaininterface backTrack) {
         this.backTrack = backTrack;
     }
 
     public void initializeComponent () {
         GridBagConstraints positioner = null;
         refreshButton = new JButton();
         refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/arts/refresh.png")));
         refreshButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 updateComponent();
             }
         });
         fileTree = new Cfilesystem(new Cfilesystemmodel());
         fileTree.getTree().addMouseListener(new Ctreelistener(backTrack, fileTree.getTree()));
         container = new JScrollPane(fileTree);
         container.setMaximumSize(new Dimension(200, 3500));
         container.setMinimumSize(new Dimension(200, 250));
         container.setPreferredSize(new Dimension(200, 250));
         /* blitting */
         this.setLayout(new GridBagLayout());
         this.setBorder(javax.swing.BorderFactory.createTitledBorder("Filesystem"));
         positioner = new GridBagConstraints();
         positioner.gridx = positioner.gridy = 0;
         positioner.weightx = positioner.weighty = 1.0;
         positioner.fill = GridBagConstraints.VERTICAL;
         this.add(container, positioner);
         positioner = new GridBagConstraints();
         positioner.gridx = 0;
         positioner.gridy = 1;
         positioner.weightx = 1.0;
         positioner.fill = GridBagConstraints.HORIZONTAL;
         this.add(refreshButton, positioner);
         /* end */
     }
     
     public void updateComponent () {
         GridBagConstraints positioner = null;
         this.remove(container);
         fileTree = new Cfilesystem(new Cfilesystemmodel());
         fileTree.getTree().addMouseListener(new Ctreelistener(backTrack, fileTree.getTree()));
         container = new JScrollPane(fileTree);
         container.setMaximumSize(new Dimension(200, 3500));
         container.setMinimumSize(new Dimension(200, 250));
         container.setPreferredSize(new Dimension(200, 250));
         positioner = new GridBagConstraints();
         positioner.gridx = positioner.gridy = 0;
         positioner.weightx = positioner.weighty = 1.0;
         positioner.fill = GridBagConstraints.VERTICAL;
         this.add(container, positioner);
         this.revalidate();
         this.repaint();
     }
 
     final static class Ctreelistener implements MouseListener {
         /* father's class */
         private Cmaininterface backTrack;
         private JTree backTree;
         /* end */
         public Ctreelistener (Cmaininterface backTrack, JTree backTree) {
             this.backTrack = backTrack;
             this.backTree = backTree;
         }
 
         public void valueChanged (TreeSelectionEvent event) {
             
         }
 
         public void mouseClicked(MouseEvent me) { }
 
         public void mousePressed(MouseEvent me) {
             String extension = null, fileName = null;
             if (me.getClickCount() == 2) {
                 File fileSystemEntity = (File)backTree.getSelectionPath().getLastPathComponent();
                if (fileSystemEntity.isFile()) {
                    fileName = fileSystemEntity.getName();
                    int index = fileName.lastIndexOf(".");
                    if ((index > 0) && (index < fileName.length())) {
                        extension = fileName.substring(index+1).toLowerCase();
                        if (extension.equals("x3d"))
                            backTrack.initializeTab(fileSystemEntity);
                     }
                 }
             }
         }
 
         public void mouseReleased(MouseEvent me) { }
         public void mouseEntered(MouseEvent me) { }
         public void mouseExited(MouseEvent me) { }
     }
 }
