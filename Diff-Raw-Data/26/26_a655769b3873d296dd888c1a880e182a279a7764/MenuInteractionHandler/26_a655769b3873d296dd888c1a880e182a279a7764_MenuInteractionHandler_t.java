 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package student.gui;
 
 import java.util.Arrays;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import student.config.WorldFileParser;
 import student.config.Constants;
 import student.config.CritterFileParser;
 import student.grid.Critter;
 import student.world.World;
 import student.world.World.InvalidWorldAdditionException;
 
 /**
  *
  * @author Eileen Liu <el544@cornell.edu>
  */
 public class MenuInteractionHandler {
     //private WorldFrame view;
     private InteractionHandler masterController;
     public MenuInteractionHandler(final InteractionHandler _parent){
         masterController = _parent; 
         //view = masterController.getView();
         masterController.getView().importWorld.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 int returnVal = masterController.getView().fileSelector.showOpenDialog(masterController.getView());
                 if(returnVal == JFileChooser.APPROVE_OPTION) {
                     String filename = masterController.getView().fileSelector.getSelectedFile().getName();
                     World newImportedWorld = WorldFileParser.generateWorld(filename, Constants.MAX_ROW , Constants.MAX_COLUMN);
                     masterController.setModel(newImportedWorld);
                 }
             }
         });
         
         masterController.getView().importCritter.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                int returnVal = masterController.getView().fileSelector.showOpenDialog(masterController.getView());
                 /*String[] possibilities = {"North", "Northeast", "Southeast", "South", "Southwest", "Northwest"};
                 String s = (String)JOptionPane.showInputDialog(
                 masterController.getView(),"Which direction should the critter face?","Putting new critter in random location",JOptionPane.QUESTION_MESSAGE,
                 null,possibilities,"North");
                 int i = Arrays.asList(possibilities).indexOf(s);*/
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                     String filename = masterController.getView().fileSelector.getSelectedFile().getName();
                    Critter newImportedCritter = CritterFileParser.generateCritter(filename, masterController.getModel(), null, -1);
                    try {
                        masterController.getModel().addCritter(newImportedCritter);
                    } catch (InvalidWorldAdditionException ex) {
                        System.out.println("Failed to import: Critter import file is invalid");
                    }
                 }
                 /*Object[] options = {"Click on a location", "Anywhere", "Cancel"};
                 int n = JOptionPane.showOptionDialog(masterController.getView(), "How do you want to put the Critter?",
                         "Add Critter", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
                 
                 * if (n != 2) {
                     String filename = masterController.getView().fileSelector.getSelectedFile().getName();
                     Critter newImportedCritter = CritterFileParser.generateCritter(filename, Constants.MAX_ROW , Constants.MAX_COLUMN);
                     
                     if (n == 0) {
                         
                     } else {
                         
                     }
                 }*/
             }
         });
         masterController.getView().importSettings.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 masterController.getView().fileSelector.showOpenDialog(masterController.getView());
             }
         });
         masterController.getView().createCritter.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 Object[] options = {"OK", "Cancel"};
                 int n = JOptionPane.showOptionDialog(masterController.getView(), "Add a critter to a random location?",
                         "Create critter", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                System.out.println("N IS = "+n);
                 if (n == 0) {
                     Critter newCritter = CritterFileParser.generateCritter(masterController.getModel(),null, -1);
                 }
             }
         });
         masterController.getView().createPlant.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 Object[] options = {"OK", "Cancel"};
                 int n = JOptionPane.showOptionDialog(masterController.getView(), "Add a plant to a random location?",
                         "Create plant", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                 if (n == 0) {
                    masterController.getModel().add("plant");
                 }
             }
         });
     }
 }
