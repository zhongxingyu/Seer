 package com.eagle.ui;
 
 import javax.swing.*;
 import javax.swing.plaf.basic.BasicBorders;
 import java.awt.*;
 import java.text.DecimalFormat;
 
 /**
  * Created with IntelliJ IDEA.
  * User: user
  * Date: 27/10/12
  * Time: 2:17 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CurrencyConverterPanel extends JPanel{
     private JLabel mRsLabel;
     private JLabel mUsdLabel;
     private JTextField mTextField;
     private JLabel mRsPerUsdLabel;
     private JTextField mRsPerUsdTextField;
 
     public CurrencyConverterPanel(){
         mRsLabel = new JLabel("Rs.");
         mUsdLabel = new JLabel("USD");
         mTextField = new JNumberTextField(10,JNumberTextField.DECIMAL);
         mTextField.setColumns(10);
 
         mRsPerUsdLabel = new JLabel("Rs per USD");
         mRsPerUsdTextField = new JNumberTextField(10,JNumberTextField.DECIMAL);
         mRsPerUsdTextField.setColumns(10);
         init();
     }
         private void init(){
             setLayout(new GridBagLayout());
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
             GridBagConstraints gbc = new GridBagConstraints();
 
             gbc.fill=GridBagConstraints.HORIZONTAL;
             gbc.gridx=0;
             gbc.gridy=0;
             add(mRsPerUsdLabel,gbc);
 
             gbc.fill=GridBagConstraints.HORIZONTAL;
             gbc.gridx=1;
             gbc.gridy=0;
             add(mRsPerUsdTextField,gbc);
 
             gbc.fill=GridBagConstraints.HORIZONTAL;
             gbc.gridx=0;
             gbc.gridy=1;
             add(mRsLabel,gbc);
 
             gbc.fill=GridBagConstraints.HORIZONTAL;
             gbc.gridx=1;
             gbc.gridy=1;
             add(mTextField,gbc);
 
             gbc.fill=GridBagConstraints.HORIZONTAL;
             gbc.gridx=0;
             gbc.gridy=2;
             add(mUsdLabel,gbc);
 
             setSize(200,200);
         }
     public void setResult(float curr, int unit){
         DecimalFormat df = new DecimalFormat("#.####");
         String s = df.format(curr);
         if(unit == CurrencyConverterController.RSTOUSD){
             s+=" USD";
         }else{
             s+=" Rs";
         }
         mUsdLabel.setText(s);
 
     }
 
     public JTextField getAmountTextField(){
         return  mTextField;
     }
     public JTextField getRateTextField(){
         return  mRsPerUsdTextField;
     }
 
 }
