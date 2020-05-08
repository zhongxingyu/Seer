 /*
  * Copyright 2012 Markus Geiss.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.mgeiss.xls2ora.presentation.control;
 
 import com.github.mgeiss.xls2ora.presentation.view.ContentContainer;
 import com.github.mgeiss.xls2ora.presentation.view.WizardPanel;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 
 /**
  *
  * @author Markus Geiss
  * @version 1.0
  */
 public class WorkflowController implements ActionListener {
 
     public static final String ACTION_BACK = "actionBack";
     public static final String ACTION_NEXT = "actionNext";
     public static final String ACTION_DONE = "actionDone";
     public static final String ACTION_CANCEL = "actionCancel";
 
     private enum Direction {
 
         BACK,
         NEXT
     }
     private HashMap<String, Object> attributeMap;
     private ContentContainer contentContainer;
 
     public WorkflowController() {
         super();
         this.init();
     }
 
     public Object setAttribute(String name, Object value) {
         return this.attributeMap.put(name, value);
     }
 
     public Object getAttribute(String name) {
         return this.attributeMap.get(name);
     }
 
     public void setStartPanel(WizardPanel wizardPanel) {
         if (wizardPanel.getPredecessorPanel() == null) {
             this.contentContainer.setBackButtonEnabled(false);
         } else {
             this.contentContainer.setBackButtonEnabled(true);
         }
 
         if (wizardPanel.getSuccesorPanel() == null) {
             this.contentContainer.switchNext(true);
         } else {
             this.contentContainer.switchNext(false);
         }
 
         this.contentContainer.setWizardPanel(wizardPanel);
     }
 
     private void init() {
         this.attributeMap = new HashMap<>();
         this.contentContainer = new ContentContainer(this);
 
         this.contentContainer.setBackButtonEnabled(false);
         this.contentContainer.switchNext(false);
 
         JFrame frame = new JFrame("xls2ora - Excelimport");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setIconImage(new ImageIcon(ClassLoader.getSystemResource("icons/wizard.png")).getImage());
         frame.setContentPane(this.contentContainer);
        frame.setSize(new Dimension(520, 300));
         frame.setResizable(false);
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
     }
 
     private void showWizardPanel(Direction direction) {
         WizardPanel actualWizardPanel = this.contentContainer.getWizardPanel();
         WizardPanel newWizardPanel = null;
         switch (direction) {
             case BACK:
                 newWizardPanel = actualWizardPanel.getPredecessorPanel();
                 break;
             case NEXT:
                 try {
                     actualWizardPanel.proceed();
                 } catch (IllegalStateException isex) {
                     return;
                 }
                 newWizardPanel = actualWizardPanel.getSuccesorPanel();
                 break;
         }
 
         try {
             newWizardPanel.prepare();
         } catch (IllegalStateException isex) {
             return;
         }
 
         if (newWizardPanel.getPredecessorPanel() == null) {
             this.contentContainer.setBackButtonEnabled(false);
         } else {
             this.contentContainer.setBackButtonEnabled(true);
         }
 
         if (newWizardPanel.getSuccesorPanel() == null) {
             this.contentContainer.switchNext(true);
         } else {
             this.contentContainer.switchNext(false);
         }
 
         this.contentContainer.setWizardPanel(newWizardPanel);
     }
 
     @Override
     public void actionPerformed(ActionEvent evt) {
         switch (evt.getActionCommand()) {
             case WorkflowController.ACTION_CANCEL:
                 System.exit(0);
                 break;
             case WorkflowController.ACTION_BACK:
                 this.showWizardPanel(Direction.BACK);
                 break;
             case WorkflowController.ACTION_NEXT:
                 this.showWizardPanel(Direction.NEXT);
                 break;
             case WorkflowController.ACTION_DONE:
                 this.contentContainer.getWizardPanel().proceed();
                 break;
         }
     }
 }
