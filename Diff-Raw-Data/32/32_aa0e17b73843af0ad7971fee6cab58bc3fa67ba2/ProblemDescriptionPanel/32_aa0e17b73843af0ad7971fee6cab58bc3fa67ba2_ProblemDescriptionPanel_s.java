 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.honeybadgers.flltutorial.ui.main.content.stages;
 
 import com.honeybadgers.flltutorial.model.Option;
 
 /**
  *
  * @author chingaman
  */
 public class ProblemDescriptionPanel extends StageSelectorPanel
 {
     public ProblemDescriptionPanel(Option rootOption)
     {
         super(rootOption);
        this.stageName = "Problem Description and constrains";
         this.initComponents();
     }
 }
