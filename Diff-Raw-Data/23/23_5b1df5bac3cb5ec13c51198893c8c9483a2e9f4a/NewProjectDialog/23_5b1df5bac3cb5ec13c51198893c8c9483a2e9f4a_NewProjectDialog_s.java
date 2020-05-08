 /*
  * Nebula2D is a cross-platform, 2D game engine for PC, Mac, & Linux
  * Copyright (c) 2014 Jon Bonazza
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.nebula2d.editor.ui;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.Vector2;
 import com.nebula2d.editor.framework.Project;
 import com.nebula2d.editor.ui.controls.N2DLabel;
 import com.nebula2d.editor.ui.controls.N2DPanel;
 import com.nebula2d.editor.util.PlatformUtil;
 import com.nebula2d.scene.Scene;
 
 import javax.swing.*;
 import java.awt.*;
 import java.io.File;
 
 public class NewProjectDialog extends JDialog {
 
     private JTextField projNameTf;
     private JTextField parentDirTf;
     private JButton browseBtn;
     private JButton createBtn;
     private JButton cancelBtn;
 
     public NewProjectDialog() {
         setTitle("New Project");
 
         N2DLabel projNameLbl = new N2DLabel("Project Name:");
         N2DLabel parentDirLbl = new N2DLabel("Parent Directory:");
 
         projNameTf = new JTextField(20);
         parentDirTf = new JTextField(20);
 
         browseBtn = new JButton("...");
         createBtn = new JButton("Create");
         cancelBtn = new JButton("Cancel");
 
         N2DPanel buttonPanel = new N2DPanel(new FlowLayout(FlowLayout.LEFT));
         buttonPanel.add(createBtn);
         buttonPanel.add(cancelBtn);
 
 
         N2DPanel mainPanel = new N2DPanel();
         GroupLayout layout = new GroupLayout(mainPanel);
         mainPanel.setLayout(layout);
 
         layout.setAutoCreateGaps(true);
         layout.setAutoCreateContainerGaps(true);
 
         GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
 
         hGroup.addGroup(layout.createParallelGroup().addComponent(projNameLbl).addComponent(parentDirLbl));
         hGroup.addGroup(layout.createParallelGroup().addComponent(projNameTf).addComponent(parentDirTf));
         hGroup.addGroup(layout.createParallelGroup().addComponent(browseBtn));
         layout.setHorizontalGroup(hGroup);
 
         GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
 
         vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                 addComponent(projNameLbl).addComponent(projNameTf));
         vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).
                 addComponent(parentDirLbl).addComponent(parentDirTf).addComponent(browseBtn));
         layout.setVerticalGroup(vGroup);
 
         add(mainPanel, BorderLayout.NORTH);
         add(buttonPanel, BorderLayout.SOUTH);
 
         pack();
         setModal(true);
         bindButtons();
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         setLocationRelativeTo(null);
         setVisible(true);
     }
 
     private void bindButtons() {
         cancelBtn.addActionListener(e -> dispose());
 
         createBtn.addActionListener(e -> {
             if(!validateText()) {
                 JOptionPane.showMessageDialog(NewProjectDialog.this,
                         "You must provide a valid project name and parent directory.");
 
                 return;
             }
             String projName = projNameTf.getText().trim();
             String parentDir = parentDirTf.getText().trim();
             File projDir = new File(PlatformUtil.pathJoin(parentDir, projName));
             if (projDir.exists()) {
                 File existingFile = new File(PlatformUtil.pathJoin(projDir.getAbsolutePath(), projName+".n2d"));
                 if (existingFile.exists()) {
                     JOptionPane.showMessageDialog(NewProjectDialog.this,
                             String.format("Project %s already exists.", projName));
                     return;
                 }
             } else {
                 if (!projDir.mkdir()) {
                     JOptionPane.showMessageDialog(NewProjectDialog.this,
                             String.format("Failed to create directory %s.", projDir.getAbsolutePath()));
                     return;
                 }
             }
 
             Gdx.app.postRunnable(() -> {
                 Project project = new Project(projDir.getAbsolutePath(), projName);
                 project.addScene(new Scene("Untitled Scene 0", new Vector2(), true));
                 MainFrame.setProject(project);
                 dispose();
             });
         });
 
         browseBtn.addActionListener(e -> {
             JFileChooser fc = new JFileChooser();
             fc.setDialogTitle("Select a Directory");
             fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
             fc.setAcceptAllFileFilterUsed(false);
 
             if (fc.showOpenDialog(NewProjectDialog.this) == JFileChooser.APPROVE_OPTION) {
                 parentDirTf.setText(fc.getSelectedFile().getAbsolutePath());
             }
         });
     }
 
     private boolean validateText() {
         if (projNameTf.getText().trim().equals("") || parentDirTf.getText().trim().equals("")) {
             return false;
         }
 
         File parentDir = new File(parentDirTf.getText().trim());
 
         return parentDir.exists() && parentDir.isDirectory();
     }
 }
