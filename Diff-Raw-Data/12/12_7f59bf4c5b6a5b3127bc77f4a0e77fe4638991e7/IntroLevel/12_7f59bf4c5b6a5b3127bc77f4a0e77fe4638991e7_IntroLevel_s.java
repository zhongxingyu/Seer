 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package app.creator;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import javax.swing.JCheckBox;
import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 /**
  *
  * @author vaisagh
  */
 class IntroLevel extends CreatorLevel {
 
     JPanel descriptionArea;
     JTextField name;
     private JTextField xSize;
     private JTextField ySize;
     private JTextField scale;
     private JCheckBox latticeModel;
 
     public IntroLevel(ModelDetails model, JFrame frame, JLabel statusBar, JPanel buttonArea) {
         super(model, frame, statusBar, buttonArea);
         descriptionArea = new JPanel();




     }
 
     @Override
     public void setUpLevel() {
         previousButton.setEnabled(false);
         clearButton.setEnabled(false);
         nextButton.setEnabled(true);
 
         frame.add(descriptionArea, BorderLayout.CENTER);
 
         descriptionArea.setSize(frame.getSize());
        intializeDescriptionArea();
 
         descriptionArea.setEnabled(true);
         descriptionArea.setVisible(true);
 
         descriptionArea.repaint();
         frame.setTitle("Enter Properties");
         frame.repaint();
 
 
     }
 
     @Override
     public void clearUp() {
 
         model.setTitle(name.getText());
         model.setXSize(xSize.getText());
         model.setYSize(ySize.getText());
         model.setScale(scale.getText());
         model.setLatticeModel(this.latticeModel.isSelected());
         descriptionArea.setEnabled(false);
         descriptionArea.setVisible(false);
         frame.remove(descriptionArea);
     }
 
     @Override
     public void draw(Graphics g) {
         throw new UnsupportedOperationException("Not supported");
     }
 
     @Override
     public void clearAllPoints() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     private void intializeDescriptionArea() {
         descriptionArea.setLayout(new GridLayout(10, 4));
         descriptionArea.setBackground(Color.lightGray);
 
         name = new JTextField();
         name.setColumns(20);
         descriptionArea.add(new JLabel(""));
         descriptionArea.add(new JLabel("Name : "));
         descriptionArea.add(name);
         descriptionArea.add(new JLabel(""));
         name.setText("Default");
 
         for (int i = 0; i < 4; i++) {
             descriptionArea.add(new JLabel(""));
         }
 
         xSize = new JTextField();
         xSize.setColumns(20);
         descriptionArea.add(new JLabel(""));
         descriptionArea.add(new JLabel("X size(meters) :"));
         descriptionArea.add(xSize);
         descriptionArea.add(new JLabel(""));
         xSize.setText("3");
 
         for (int i = 0; i < 4; i++) {
             descriptionArea.add(new JLabel(""));
         }
 
         ySize = new JTextField();
         ySize.setColumns(20);
         descriptionArea.add(new JLabel(""));
         descriptionArea.add(new JLabel("Y size (meters) :"));
         descriptionArea.add(ySize);
         descriptionArea.add(new JLabel(""));
         ySize.setText("3");
 
         for (int i = 0; i < 4; i++) {
             descriptionArea.add(new JLabel(""));
         }
 
         scale = new JTextField();
         scale.setColumns(20);
         descriptionArea.add(new JLabel(""));
         descriptionArea.add(new JLabel("Scale (pix / m) :"));
         descriptionArea.add(scale);
         descriptionArea.add(new JLabel(""));
         scale.setText("100");
 
 
         for (int i = 0; i < 4; i++) {
             descriptionArea.add(new JLabel(""));
         }
 
 
         latticeModel = new JCheckBox();
         descriptionArea.add(new JLabel(""));
         descriptionArea.add(new JLabel("Lattice Model :"));
         descriptionArea.add(latticeModel);
         descriptionArea.add(new JLabel(""));
 
         for (int i = 0; i < 4; i++) {
             descriptionArea.add(new JLabel(""));
         }
 
 
     }
 
     public void reloadValuesFromModel() {
         name.setText(model.getTitle());
         xSize.setText(Integer.toString(model.getxSize()));
         ySize.setText(Integer.toString(model.getySize()));
         scale.setText(Integer.toString(model.getScale()));
         latticeModel.setSelected(model.getLatticeSpaceFlag());
     }
 }
