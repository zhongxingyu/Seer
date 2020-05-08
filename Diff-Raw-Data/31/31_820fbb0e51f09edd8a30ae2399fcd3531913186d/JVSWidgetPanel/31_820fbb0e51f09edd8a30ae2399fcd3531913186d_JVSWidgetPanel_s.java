 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.javascool.gui;
 
 import javax.swing.JPanel;
 import org.javascool.tools.Proglet;
 
 /**
  *
  * @author Philippe Vienne
  */
 public class JVSWidgetPanel extends JVSTabs {
 
     private String progletTabId;
 
     public JVSWidgetPanel() {
         super();
     }
 
     public void setProglet(Proglet proglet) {
         if (proglet.getPanel() != null) {
             this.progletTabId = this.add("Proglet "+proglet.getName(), "", proglet.getPanel());
         }
         if (proglet.getHelpFileUrl()!=null){
             this.add("Aide de la proglet", "", proglet.getHelpFileUrl());
         }
         this.openWebTab("org/javascool/doc-files/memo.html","Mémo");
     }
 
     public JPanel getProgletPanel() {
         return this.getPanel(progletTabId);
     }
     
     public void focusOnProgletPanel(){
         if(progletTabId!=null){
             this.switchToTab(progletTabId);
         }
     }
     
     public void openWebTab(String url,String tabName){
         JVSHtmlDisplay memo = new org.javascool.gui.JVSHtmlDisplay();
         memo.load(url);
         this.add(tabName, "", memo);
         this.setTabComponentAt(this.indexOfTab(tabName),new TabPanel(this));
         this.setSelectedComponent(memo);
     }
 }
