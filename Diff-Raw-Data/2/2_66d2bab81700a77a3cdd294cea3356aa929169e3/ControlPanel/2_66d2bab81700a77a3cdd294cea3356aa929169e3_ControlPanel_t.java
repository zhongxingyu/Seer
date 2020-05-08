 package com.tihiy.rclint.implement;
 
 import com.tihiy.rclint.control.Controller;
 import com.tihiy.rclint.mvcAbstract.AbstractViewPanel;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class ControlPanel extends AbstractViewPanel {
     private final Controller mc;
     private File defaultPath;
     private File sourceFile;
 
     public ControlPanel(Controller mc) {
         this.mc = mc;
         setBorder(BorderFactory.createLineBorder(Color.BLUE));
         setLayout(new GridBagLayout());
         initComponent();
     }
 
     private void initComponent(){
         butChooseSignal = new JButton("Choose Signal");
         butChooseFirstLayerSignal = new JButton("First Layer Signal");
         butChooseBaseSignal = new JButton("Base signal");
         butCalculate =  new JButton("Calculate");
         butDefault = new JButton("Default signal");
         mainSizeA = new JTextField();
         mainSizeB = new JTextField();
         mainXShift = new JTextField();
         mainYShift = new JTextField();
         mainRSphere = new JTextField();
         mainH = new JTextField();
         firstSizeA = new JTextField();
         firstSizeB = new JTextField();
 
         butChooseSignal.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
                     sourceFile = chooseFile();
                     mc.addSignal("sourceSignal", sourceFile);
                 } catch (IOException e1) {
                     e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         });
         butChooseFirstLayerSignal.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
                     mc.addSignal("targetSignal", chooseFile());
                 } catch (IOException e1) {
                     e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         });
         butChooseBaseSignal.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
                     mc.addSignal("baseSignal", chooseFile());
                 } catch (IOException e1) {
                    e1.printStackTrace();  // body of catch statement use File | Settings | File Templates.
                 }
             }
         });
         butCalculate.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 double[] main = {Double.valueOf(mainSizeA.getText()), Double.valueOf(mainSizeB.getText()),
                         Double.valueOf(mainXShift.getText()), Double.valueOf(mainYShift.getText()),
                         Double.valueOf(mainRSphere.getText()), Double.valueOf(mainH.getText())};
                 double[] first = {Double.valueOf(firstSizeA.getText()), Double.valueOf(firstSizeB.getText())};
                 File radiusFile = new File(defaultPath, "radius"+ getDate()+sourceFile.getName());
                 mc.calculate(main, first, radiusFile, null);
             }
         });
         butDefault.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 sourceFile = new File("C:\\Users\\Home\\Documents\\My Box Files\\Asp\\RoChange\\Rad 20130716\\P1.txt");
                 File roFile = new File("C:\\Users\\Home\\Documents\\My Box Files\\Asp\\RoChange\\Rad 20130716\\Ro.txt");
                 File baseFile = new File("C:\\Users\\Home\\Documents\\My Box Files\\Asp\\RoChange\\Rad 20130716\\PB1.txt");
                 try {
                     mc.addSignal("sourceSignal", sourceFile);
                     mc.addSignal("targetSignal", roFile);
                     mc.addSignal("baseSignal", baseFile);
                 } catch (IOException e1) {
                     e1.printStackTrace();
                 }
                 double[] main = {0.04,0.02,0,0.037,0.045,0.019};
                 double[] first = {0.06, 0.03};
                 defaultPath = new File("C:\\Users\\Home\\Documents\\My Box Files\\Asp\\RoChange\\Rad 20130716");
                 File radiusFile = new File(defaultPath, "radius"+getDate()+sourceFile.getName());
                 mc.calculate(main, first, radiusFile, getComment(roFile, baseFile, main, first));
             }
         });
 
         GridBagConstraints constraints = new GridBagConstraints(0,0, 1,1, 0,0,
                 GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0);
         add(butChooseSignal, constraints);
         constraints.gridy = 1;
         add(butChooseFirstLayerSignal, constraints);
         constraints.gridy = 2;
         add(butChooseBaseSignal, constraints);
         constraints.gridy = 3;
         add(butCalculate, constraints);
         constraints.gridy = 4;
         add(butDefault);
         constraints.gridy = 5;
         constraints.gridx = 0;
         constraints.insets = new Insets(0,5,0,5);
         add(new Label("Size A(main)"), constraints);
         constraints.gridx = 1;
         add(mainSizeA, constraints);
         constraints.gridy = 6;
         constraints.gridx = 0;
         add(new Label("Size B(main)"), constraints);
         constraints.gridx = 1;
         add(mainSizeB, constraints);
         constraints.gridy = 7;
         constraints.gridx = 0;
         add(new Label("Shift X(main)"), constraints);
         constraints.gridx = 1;
         add(mainXShift, constraints);
         constraints.gridy = 8;
         constraints.gridx = 0;
         add(new Label("Shift Y(main)"), constraints);
         constraints.gridx = 1;
         add(mainYShift, constraints);
         constraints.gridy = 9;
         constraints.gridx = 0;
         add(new Label("R sphere(main)"), constraints);
         constraints.gridx = 1;
         add(mainRSphere, constraints);
         constraints.gridy = 10;
         constraints.gridx = 0;
         add(new Label("H (main)"), constraints);
         constraints.gridx = 1;
         add(mainH, constraints);
         constraints.gridy = 11;
         constraints.gridx = 0;
         add(new Label("Size A(FL)"), constraints);
         constraints.gridx = 1;
         add(firstSizeA, constraints);
         constraints.gridy = 12;
         constraints.gridx = 0;
         add(new Label("Size B(FL)"), constraints);
         constraints.gridx = 1;
         add(firstSizeB, constraints);
     }
 
     @Override
     public void modelPropertyChange(PropertyChangeEvent evt) {
     }
     private JButton butChooseSignal;
     private JButton butChooseFirstLayerSignal;
     private JButton butChooseBaseSignal;
     private JButton butCalculate;
     private JButton butDefault;
     private JTextField mainSizeA;
     private JTextField mainSizeB;
     private JTextField mainXShift;
     private JTextField mainYShift;
     private JTextField mainRSphere;
     private JTextField mainH;
     private JTextField firstSizeA;
     private JTextField firstSizeB;
 
     private File chooseFile(){
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.changeToParentDirectory();
         fileChooser.setCurrentDirectory(new File("C:\\Users\\Home\\Documents\\My Box Files\\Asp\\RoChange\\Rad 20130716"));
 //        defaultPath = new File("C:\\Users\\Home\\Documents\\My Box Files\\Asp\\RoChange\\Rad 20130716");
         if(defaultPath!=null){
             fileChooser.setCurrentDirectory( defaultPath);
         }
         fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         fileChooser.showDialog(new JFrame(), "Choose signal!");
         if(defaultPath==null){
             defaultPath = fileChooser.getCurrentDirectory();
         }
         return fileChooser.getSelectedFile();
     }
 
     private static String getDate(){
         Date dNow = new Date( );
         SimpleDateFormat ft = new SimpleDateFormat ("_yyyy_MM_dd_hh_mm_ss");
         return ft.format(dNow);
     }
     private String getComment(File roFile, File baseFile, double[] main, double[] first){
         String comment = "Pulse="+sourceFile.getName()+" Ro="+roFile.getName()+" Base="+baseFile.getName()+" ";
         comment += "pulseES:";
         for(double d:main){
             comment += " "+d;
         }
         comment += " baseES" + first[0]+" "+first[1]+" ";
         return comment;
     }
 }
