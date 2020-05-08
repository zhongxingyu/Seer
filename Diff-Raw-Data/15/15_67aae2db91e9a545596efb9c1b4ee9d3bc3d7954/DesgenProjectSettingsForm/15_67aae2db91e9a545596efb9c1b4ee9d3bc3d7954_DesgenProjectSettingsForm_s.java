 package org.techdesgen;
 
 import javax.swing.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: kcyxa
  * Date: 10/14/12
  * Time: 11:44 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DesgenProjectSettingsForm {
     private JPanel rootPanel;
    private JComboBox comboBox1;
 
     public JPanel getRootPanel() {
         return rootPanel;
     }
 
     public boolean isModified(DesgenProjectSettings settings) {
        return !settings.getJiraProject().equals(comboBox1.getSelectedItem());
     }
 
     public void getData(DesgenProjectSettings settings) {
        settings.setJiraProject(comboBox1.getSelectedItem().toString());
     }
 
     public void setData(DesgenProjectSettings settings) {
        comboBox1.setSelectedItem(settings.getJiraProject());
     }
 }
