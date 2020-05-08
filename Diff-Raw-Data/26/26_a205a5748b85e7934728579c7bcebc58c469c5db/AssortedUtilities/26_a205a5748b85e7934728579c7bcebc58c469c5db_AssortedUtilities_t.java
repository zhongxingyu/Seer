 package com.eagle.main;
 
 import com.eagle.ui.CurrencyConverterController;
 import com.eagle.ui.DistanceConverterController;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: user
  * Date: 27/10/12
  * Time: 10:11 AM
  * To change this template use File | Settings | File Templates.
  */
 public class AssortedUtilities {
     public static void main(String args[]) throws Exception{
         DistanceConverterController dcc = new DistanceConverterController();
         CurrencyConverterController ccc = new CurrencyConverterController();
         JFrame mFrame = new JFrame("Assorted Utilities");
        mFrame.setSize(400,200);
        mFrame.setLocation(350,350);
         mFrame.setVisible(true);
 
         mFrame.setLayout(new GridBagLayout());
 
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.fill=GridBagConstraints.HORIZONTAL;
         gbc.gridx=0;
         gbc.gridy=0;
         mFrame.add(dcc.getPanel(), gbc);
 
 
         gbc.fill=GridBagConstraints.HORIZONTAL;
         gbc.gridx=1;
         gbc.gridy=0;
         mFrame.add(ccc.getPanel(), gbc);
 
       /*  gbc.fill=GridBagConstraints.HORIZONTAL;
         gbc.gridx=1;
         gbc.gridy=1;
         mFrame.add(dcc3.getPanel(),gbc);*/
 
         mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
     }
 
 
 }
