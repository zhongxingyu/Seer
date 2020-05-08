 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.wontonst.ghgrader.gui;
 
 import com.wontonst.ghgformat.exceptions.IncompleteGHGFileException;
 import com.wontonst.ghgformat.exceptions.MalformedGHGFileException;
 import com.wontonst.ghgformat.file.GHGFile;
 import com.wontonst.ghgformat.parser.GHGLoader;
 import com.wontonst.ghgrader.model.Project;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author RoyZheng
  */
 public class GuiCore extends JFrame implements ActionListener, KeyListener {
 
    public static int FRAME_DIM_X = 800;
    public static int FRAME_DIM_Y = 600;
     protected static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
     protected JFileChooser file_chooser = new JFileChooser();
     protected Menu menu;
     protected Project project = null;
     protected ProjectBuilder project_builder = null;
 
     public GuiCore() {
         this.menu = new Menu(this);
         this.setJMenuBar(this.menu);
         this.addKeyListener(this);
         this.setSize(FRAME_DIM_X, FRAME_DIM_Y);
         this.setLocation(GuiCore.screenSize.width / 2 - FRAME_DIM_X / 2, GuiCore.screenSize.height / 2 - FRAME_DIM_Y / 2);
         this.setVisible(true);
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         System.out.println("Action performed");
         if (this.menu.objIsNew(e.getSource())) {
             this.actionNewProject();
             return;
         }
         if (this.menu.objIsQuit(e.getSource())) {
             return;
         }
         if (this.menu.objIsSave(e.getSource())) {
 
             return;
         }
         if (this.menu.objIsOpen(e.getSource())) {
             int returnv = this.file_chooser.showOpenDialog(this);
             if (returnv == JFileChooser.APPROVE_OPTION) {
                 File f = this.file_chooser.getSelectedFile();
                 //parse ghd file
             }
             return;
         }
     }
 
     public void projectBuildDone() {
         try {
             this.project = this.project_builder.build();
             this.project_builder.finish();
             this.project_builder = null;
             this.setVisible(true);
             this.add(this.project.toPanel());
 
         } catch (Exception ex) {
             //TODO: not sure what to do yet
         }
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_N && e.isControlDown()) {
             this.actionNewProject();
         }
     }
 
     private void actionNewProject() {
 
         int returnv = this.file_chooser.showOpenDialog(this);
         if (returnv == JFileChooser.APPROVE_OPTION) {
             File f = this.file_chooser.getSelectedFile();
             try {
                 GHGFile ghgf = GHGLoader.load(f);
                 this.setVisible(false);
                 this.project_builder = new ProjectBuilder(this, ghgf);
             } catch (FileNotFoundException ex) {//NOT TRUE
                 JOptionPane.showMessageDialog(this, "File could not be found!");
             } catch (MalformedGHGFileException ex) {
                 JOptionPane.showMessageDialog(this, "Malformed .ghg file: " + ex.getMessage());
             } catch (IncompleteGHGFileException ex) {
                 JOptionPane.showMessageDialog(this, "CRITICAL ERROR! " + ex.getMessage());
             }
         }
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
     }
 
     @Override
     public void keyTyped(KeyEvent e) {
     }
 }
