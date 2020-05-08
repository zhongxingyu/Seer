 package com.eagle.ui;
 
 import javax.swing.*;
 import java.awt.*;
 import java.text.DecimalFormat;
 
 /**
  * Created with IntelliJ IDEA.
  * User: user
  * Date: 27/10/12
  * Time: 8:54 AM
  * To change this template use File | Settings | File Templates.
  */
 public class DistanceConverterPanel extends JPanel {
     private JLabel mDistLabel;
     private JLabel mUnitLabel;
     private JLabel mResLabel;
     private JTextField mTextField;
 
     public DistanceConverterPanel() {
         mDistLabel = new JLabel("Distance");
         mUnitLabel = new JLabel("km");
         mResLabel = new JLabel("mile");
         mTextField = new JNumberTextField(5,JNumberTextField.DECIMAL);
         mTextField.setColumns(10);
         // mTextField = new JTextField(10);
         init();
     }
     private void init(){
         setLayout(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
        setBorder(BorderFactory.createLineBorder(Color.CYAN));
 
 
         gbc.fill=GridBagConstraints.HORIZONTAL;
         gbc.gridx=0;
         gbc.gridy=0;
         add(mDistLabel,gbc);
         gbc.fill=GridBagConstraints.HORIZONTAL;
         gbc.gridx=1;
         gbc.gridy=0;
         add(mTextField,gbc);
 
         gbc.fill=GridBagConstraints.HORIZONTAL;
         gbc.gridx=2;
         gbc.gridy=0;
         gbc.anchor = GridBagConstraints.CENTER;
         add(mUnitLabel,gbc);
 
         gbc.fill=GridBagConstraints.HORIZONTAL;
         gbc.gridx=0;
         gbc.gridy=1;
         gbc.gridwidth = 3;
         add(mResLabel,gbc);
 
         setSize(300,300);
 
 
     }
     public void setResult(float dist, int unit){
         DecimalFormat df = new DecimalFormat("#.####");
         String s = df.format(dist);
         if(unit == DistanceConverterController.KMTOMILES){
             s+=" miles";
         }else{
             s+=" kms";
         }
         mResLabel.setText(s);
 
     }
 
     public JTextField getTextField(){
         return  mTextField;
     }
 }
