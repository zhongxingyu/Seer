 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.wontonst.ghgrader.gui;
 
 import com.wontonst.ghgrader.model.Project;
 import com.wontonst.ghgrader.model.Student;
 import java.awt.CardLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.JPanel;
 
 /**
  * Displays the grades for a project.
  *
  * @author RoyZheng
  */
 public class ProjectGui extends JPanel {
 
     Project project;///<model representation
     List<StudentGui> students = new ArrayList<StudentGui>();
 
     public ProjectGui(Project p, List<StudentGui> gui) {
         this.project = p;
 
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.gridx = 0;
         gbc.gridy = 0;
 
         this.setLayout(new CardLayout());
 
         for (StudentGui s : gui) {
             this.add(s);
         }
     }
 }
