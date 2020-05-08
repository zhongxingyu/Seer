 package com.sun.identity.admin.model;
 
 import java.io.Serializable;
 
 public class WizardBean implements Serializable {
     private WizardStepBean[] wizardStepBeans = null;
     private int steps;
 
     public void reset() {
         wizardStepBeans = new WizardStepBean[getSteps()];
         for (int i = 0; i < getSteps(); i++) {
             wizardStepBeans[i] = new WizardStepBean();
         }
 
        WizardStepBean first = getWizardStepBeans()[0];
        first.setEnabled(true);
        first.setExpanded(true);
     }
 
     public WizardStepBean[] getWizardStepBeans() {
         return wizardStepBeans;
     }
 

     public boolean isFinishRendered() {
        for (WizardStepBean wsb: wizardStepBeans) {
             if (!wsb.isEnabled()) {
                 return false;
             }
         }
         return true;
     }
 
     public void gotoStep(int gotoStep) {
         setAllExpanded(false);
 
         WizardStepBean next = getWizardStepBeans()[gotoStep];
         next.setEnabled(true);
         next.setExpanded(true);
     }
 
     public void setAllEnabled(boolean enabled) {
        for (WizardStepBean wsb: getWizardStepBeans()) {
             wsb.setEnabled(enabled);
         }
     }
 
     public void setAllExpanded(boolean expaned) {
        for (WizardStepBean wsb: getWizardStepBeans()) {
             wsb.setExpanded(expaned);
         }
     }
 
     public int getSteps() {
         return steps;
     }
 
     public void setSteps(int steps) {
         this.steps = steps;
         reset();
     }
 }
