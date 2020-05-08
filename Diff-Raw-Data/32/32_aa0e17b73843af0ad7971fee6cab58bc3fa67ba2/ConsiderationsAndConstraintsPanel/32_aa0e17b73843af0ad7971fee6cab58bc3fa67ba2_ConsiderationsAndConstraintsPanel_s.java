 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.honeybadgers.flltutorial.ui.main.content.stages;
 
 import com.honeybadgers.flltutorial.model.Option;
 import com.honeybadgers.flltutorial.ui.main.content.utilities.OptionPanel;
 import com.honeybadgers.flltutorial.ui.main.content.utilities.TextOptionPanel;
 
 /**
  *
  * @author chingaman
  */
 public class ConsiderationsAndConstraintsPanel extends MorphChartPanel{
     public ConsiderationsAndConstraintsPanel(Option rootOption)
     {
         super(rootOption);
        this.stageName = "Considerations and Constraints";
         this.initComponents();
     }
     @Override
     protected OptionPanel createOptionPanel(Option option)
     {
                     //System.out.println("Generated in consideration");
 
         return new TextOptionPanel(option);
     } 
 }
