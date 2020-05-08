 package de.aidger.view.tabs;
 
 import static de.aidger.utils.Translation._;
 
 import java.util.ArrayList;
import java.util.List;
 
 import javax.swing.table.DefaultTableModel;
 
 import de.aidger.controller.ActionNotFoundException;
 import de.aidger.controller.ActionRegistry;
 import de.aidger.controller.actions.ActivityReportExportAction;
 import de.aidger.controller.actions.ActivityReportGenerateAction;
 import de.aidger.model.models.Assistant;
 import de.aidger.model.reports.ActivityEmployment;
 import de.aidger.utils.reports.ActivityReportHelper;
 import de.aidger.view.UI;
import de.aidger.view.models.UIAssistant;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IAssistant;
 
 /**
  * This class is used to display the activity report of one assistant.
  * 
  * @author aidGer Team
  */
 public class ActivityReportViewerTab extends Tab {
 
     /**
      * The table model of the content table.
      */
     private final DefaultTableModel contentTableModel = new DefaultTableModel(
         null, new String[] { _("Period"), _("Course"), _("Extent") }) {
 
         @Override
         public boolean isCellEditable(int rowIndex, int columnIndex) {
             return false;
         }
     };
 
     /**
      * The assistants in the combo box.
      */
    private ArrayList<UIAssistant> assistants;
 
     /**
      * Initializes a new AcitivityReportViewerTab.
      */
     public ActivityReportViewerTab() {
         initComponents();
         fillAssistants();
         try {
             generateButton.setAction(ActionRegistry.getInstance().get(
                 ActivityReportGenerateAction.class.getName()));
             exportButton.setAction(ActionRegistry.getInstance().get(
                 ActivityReportExportAction.class.getName()));
         } catch (ActionNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         exportButton.setVisible(false);
         jSeparator3.setVisible(false);
     }
 
    /**
     * Returns the name of the selected assistant.
     * 
     * @return
     */
     public String getAssistantName() {
         return assistantNameContentLabel.getText();
     }
 
     /**
      * Sets the export button and its separator to be visible.
      */
     private void visualizeExportButton() {
         exportButton.setVisible(false);
         exportButton.setVisible(true);
         jSeparator3.setVisible(false);
         jSeparator3.setVisible(true);
     }
 
     /**
      * Returns all the rows of the table.
      * 
      * @return The rows of the table.
      */
     public ArrayList<String[]> getRows() {
         ArrayList<String[]> returnVector = new ArrayList<String[]>();
         for (int i = 0; i < contentTableModel.getRowCount(); i++) {
             String[] returnString = new String[contentTableModel
                 .getColumnCount()];
             for (int j = 0; j < contentTableModel.getColumnCount(); j++) {
                 returnString[j] = contentTableModel.getValueAt(i, j).toString();
             }
             returnVector.add(returnString);
         }
         return returnVector;
     }
 
     /**
      * Removes all entries from the table
      */
     private void clearTable() {
         while (contentTableModel.getRowCount() > 0) {
             contentTableModel.removeRow(0);
         }
     }
 
     /**
      * Creates the activity report for a given assistant.
      * 
      * @param assistant
      *            The assistant
      */
     public void createReport(Assistant assistant) {
         clearTable();
         assistantNameContentLabel.setText(assistant.getFirstName() + " "
                 + assistant.getLastName());
         ActivityReportHelper reportHelper = new ActivityReportHelper();
         ArrayList<ActivityEmployment> activityEmployments = reportHelper
             .getEmployments(assistant);
         for (ActivityEmployment activityEmployment : activityEmployments) {
             addRow(reportHelper.getEmploymentArray(activityEmployment));
         }
         visualizeExportButton();
         assistantComboBox.setSelectedIndex(assistants.indexOf(assistant));
     }
 
     /**
      * Returns the assistant that is currently selected in the combo box.
      * 
      * @return The assistant.
      */
     public Assistant getSelectedAssistant() {
        return (Assistant) assistantComboBox.getSelectedItem();
     }
 
     /**
      * Adds all of the assistants into the combo box.
      */
     private void fillAssistants() {
         List<IAssistant> fillingAssistants;
        assistants = new ArrayList<UIAssistant>();
         try {
             fillingAssistants = new Assistant().getAll();
             for (IAssistant assistant : fillingAssistants) {
                assistants.add(new UIAssistant(assistant));
                assistantComboBox.addItem(new UIAssistant(assistant));
             }
         } catch (AdoHiveException e) {
             UI.displayError(e.toString());
         }
     }
 
     /**
      * Adds a new row to the table.
      * 
      * @param rowObject
      *            The row data
      */
     private void addRow(Object[] rowObject) {
         contentTableModel.addRow(rowObject);
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jToolBar1 = new javax.swing.JToolBar();
         jSeparator1 = new javax.swing.JToolBar.Separator();
         assistantLabel = new javax.swing.JLabel();
         assistantComboBox = new javax.swing.JComboBox();
         jSeparator2 = new javax.swing.JToolBar.Separator();
         generateButton = new javax.swing.JButton();
         jSeparator3 = new javax.swing.JToolBar.Separator();
         exportButton = new javax.swing.JButton();
         jSeparator4 = new javax.swing.JToolBar.Separator();
         contentPanel = new javax.swing.JPanel();
         assistantPanel = new javax.swing.JPanel();
         assistantNameLabel = new javax.swing.JLabel();
         assistantNameContentLabel = new javax.swing.JLabel();
         tablePanel = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTable1 = new javax.swing.JTable();
 
         setLayout(new java.awt.BorderLayout());
 
         jToolBar1.setFloatable(false);
         jToolBar1.setRollover(true);
         jToolBar1.add(jSeparator1);
 
         assistantLabel.setText(_("Assistant") + ":");
         jToolBar1.add(assistantLabel);
 
         jToolBar1.add(assistantComboBox);
         jToolBar1.add(jSeparator2);
 
         generateButton.setText(_("Generate"));
         generateButton.setFocusable(false);
         generateButton
             .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         generateButton
             .setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jToolBar1.add(generateButton);
         jToolBar1.add(jSeparator3);
 
         exportButton.setText(_("Export"));
         exportButton.setFocusable(false);
         exportButton
             .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         exportButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jToolBar1.add(exportButton);
         jToolBar1.add(jSeparator4);
 
         add(jToolBar1, java.awt.BorderLayout.PAGE_START);
 
         contentPanel.setLayout(new java.awt.BorderLayout());
 
         assistantPanel.setLayout(new java.awt.FlowLayout(
             java.awt.FlowLayout.LEFT));
 
         assistantNameLabel.setText(_("Assistant name") + ":");
         assistantPanel.add(assistantNameLabel);
         assistantPanel.add(assistantNameContentLabel);
 
         contentPanel.add(assistantPanel, java.awt.BorderLayout.PAGE_START);
 
         tablePanel.setLayout(new java.awt.GridLayout());
 
         jTable1.setModel(contentTableModel);
         jScrollPane1.setViewportView(jTable1);
 
         tablePanel.add(jScrollPane1);
 
         contentPanel.add(tablePanel, java.awt.BorderLayout.CENTER);
 
         add(contentPanel, java.awt.BorderLayout.CENTER);
     }// </editor-fold>//GEN-END:initComponents
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JComboBox assistantComboBox;
     private javax.swing.JLabel assistantLabel;
     private javax.swing.JLabel assistantNameContentLabel;
     private javax.swing.JLabel assistantNameLabel;
     private javax.swing.JPanel assistantPanel;
     private javax.swing.JPanel contentPanel;
     private javax.swing.JButton exportButton;
     private javax.swing.JButton generateButton;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JToolBar.Separator jSeparator1;
     private javax.swing.JToolBar.Separator jSeparator2;
     private javax.swing.JToolBar.Separator jSeparator3;
     private javax.swing.JToolBar.Separator jSeparator4;
     private javax.swing.JTable jTable1;
     private javax.swing.JToolBar jToolBar1;
     private javax.swing.JPanel tablePanel;
 
     // End of variables declaration//GEN-END:variables
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.view.tabs.Tab#getTabName()
      */
     @Override
     public String getTabName() {
         return _("Activity report");
     }
 
 }
