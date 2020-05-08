 package at.jku.busem.gui;
 
 import at.jku.busem.diff.core.DiffEngine;
 import at.jku.busem.diff.core.exceptions.ModelsNotLoadedException;
import at.jku.busem.diff.data.matching.Probability;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 public class Controller implements ActionListener {
     private JFrame view;
     private DiffEngine model;
 
     public Controller() {
         this.view = new View("Semantic Diff", this);
         this.model = new DiffEngine();
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         JFileChooser fileChooser = new JFileChooser();
         FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter(
                 "xml files (*.xml)", "xml");
         fileChooser.setFileFilter(xmlfilter);
         File file = null;
         if (e.getActionCommand().equalsIgnoreCase("model1Dialog")) {
             int returnVal = fileChooser.showOpenDialog(null);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 file = fileChooser.getSelectedFile();
                 ((View) this.view).setModel1Path(file.getPath());
             }
         } else if (e.getActionCommand().equalsIgnoreCase("model2Dialog")) {
             int returnVal = fileChooser.showOpenDialog(null);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 file = fileChooser.getSelectedFile();
                 ((View) this.view).setModel2Path(file.getPath());
             }
         } else if (e.getActionCommand().equalsIgnoreCase("closeItem")) {
             this.view.dispose();
         } else {
             //do diff
             String model2Path = ((View) this.view).getModel2Path();
             String model1Path = ((View) this.view).getModel1Path();
             System.out.println(model1Path);
 
             if (model1Path.equals("") || model2Path.equals("")) {
                 JOptionPane.showMessageDialog(new JFrame(), "Pfade sind nicht angegeben!", "Fehler",
                         JOptionPane.ERROR_MESSAGE);
             } else {
                 this.model.load(model1Path, model2Path);
                 try {
                    this.model.suppressResultsForMatchProbabilityAtOrBelow(Probability.HIGH);
                     this.model.start();
                     ((View) this.view).writeToTextArea(this.model.getWriteableResults());
                 } catch (ModelsNotLoadedException e1) {
                     // TODO Auto-generated catch block
                     e1.printStackTrace();
                 }
             }
         }
     }
 }
