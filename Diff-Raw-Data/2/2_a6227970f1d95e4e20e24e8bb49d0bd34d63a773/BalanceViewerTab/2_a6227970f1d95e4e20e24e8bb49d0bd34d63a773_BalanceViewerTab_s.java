 package de.aidger.view.tabs;
 
 import static de.aidger.utils.Translation._;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 import de.aidger.controller.ActionNotFoundException;
 import de.aidger.controller.ActionRegistry;
 import de.aidger.controller.actions.ReportExportAction;
 import de.aidger.controller.actions.ReportGenerateAction;
 import de.aidger.model.models.Course;
 import de.aidger.model.reports.BalanceFilter;
 import de.aidger.utils.reports.BalanceHelper;
 import de.aidger.view.UI;
 import de.aidger.view.reports.BalanceFilterPanel;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.ICourse;
 
 /**
  * A tab for viewing balance reports.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class BalanceViewerTab extends ReportTab {
 
     /**
      * States which calculation method should be used. 0 - neutral, 1 -
      * pessimistic, 0 - historical
      */
     private final int calculationMethod = 0;
 
     /**
      * The balanceFilter of this balance.
      */
     private BalanceFilter balanceFilter = null;
 
     /**
      * The type of balance report this is. 1 is full, 2 is annual, 3 is
      * semester.
      */
     private int typeOfBalance = 0;
 
     /**
      * The name of this tab.
      */
     private String name;
 
     /**
      * Initializes a new BalanceViewerTab, which will have the balance
      * manipulation elements and the created Semesters added to it.
      */
     public BalanceViewerTab(Integer index) {
         initComponents();
         try {
             generateButton.setAction(ActionRegistry.getInstance().get(
                 ReportGenerateAction.class.getName()));
             exportButton.setAction(ActionRegistry.getInstance().get(
                 ReportExportAction.class.getName()));
         } catch (ActionNotFoundException ex) {
             UI.displayError(ex.getMessage());
         }
 
         typeOfBalance = index;
         balanceFilter = new BalanceFilter();
         switch (index) {
         case 1:
             /*
              * This is a full balance report.
              */
             name = _("Full Balance Report");
             break;
         case 2:
             /*
              * This is an annual balance report.
              */
             jToolBar1.add(yearComboBox, 0);
             jToolBar1.add(yearLabel, 0);
             jToolBar1.add(jSeparator4, 0);
             ArrayList<Integer> availableYears = new BalanceHelper().getYears();
             for (Object year : availableYears) {
                 yearComboBox.addItem(year);
             }
             name = _("Annual Balance Report");
             break;
         case 3:
             /*
              * This is a semester balance report.
              */
             jToolBar1.add(yearComboBox, 0);
             jToolBar1.add(yearLabel, 0);
             jToolBar1.add(jSeparator4, 0);
             yearLabel.setText(_("Semester" + ":"));
             ArrayList<String> semesters = new BalanceHelper().getSemesters();
             for (Object semester : semesters) {
                 yearComboBox.addItem(semester);
             }
             name = _("Semester Balance Report");
             break;
         }
         filterNameComboBox.addItem(_("Group"));
         filterNameComboBox.addItem(_("Lecturer"));
         filterNameComboBox.addItem(_("Target Audience"));
     }
 
     /**
      * Get the name of the tab and constructor options if necessary.
      * 
      * @return A string representation of the class
      */
     @Override
     public String toString() {
         return getClass().getName() + "<" + Integer.class.getName() + "@"
                 + Integer.toString(typeOfBalance);
     }
 
     /**
      * Whether the tab should be on a scroll pane.
      * 
      * @return
      */
     @Override
     public boolean isScrollable() {
         return false;
     }
 
     /**
      * Get the type of balance.
      * 
      * @return The type of balance
      */
     public int getType() {
         return typeOfBalance;
     }
 
     /**
      * Returns the balance filter.
      * 
      * @return The balance filter
      */
     public BalanceFilter getBalanceFilter() {
         return balanceFilter;
     }
 
     /**
      * Get the year currently selected-
      * 
      * @return The year
      */
     public Object getYear() {
         return yearComboBox.getSelectedIndex() > -1 ? yearComboBox
             .getSelectedItem() : null;
     }
 
     /**
      * Add a new panel
      * 
      * @param panel
      *            The panel to add
      */
     public void addPanel(JPanel panel) {
         contentPanel.add(panel);
         contentPanel.setVisible(false);
         contentPanel.setVisible(true);
     }
 
     /**
      * Clear the panel.
      */
     public void clearPanel() {
         contentPanel.removeAll();
         contentPanel.setVisible(false);
         contentPanel.setVisible(true);
     }
 
     /**
      * Removes the given panel from the filter panel.
      * 
      * @param panel
      *            The panel to remove
      */
     @Override
     public void removeFilterPanel(JPanel panel) {
         filterContentPanel.remove(panel);
         filterContentPanel.setVisible(false);
         filterContentPanel.setVisible(true);
     }
 
     /**
      * Get the calculation method to be used with this balance.
      * 
      * @return 0 if neutral, 1 if pessimistic, 2 if historical
      */
     public int getCalculationMethod() {
         return calculationMethod;
     }
 
     /**
      * Removes the specified filter from the filters list.
      * 
      * @param type
      *            The type of filter.
      * @param value
      *            The value of the filter.
      */
     @Override
     public void removeFilter(String name, String value) {
         if (name.equals(_("Group"))) {
             if (balanceFilter.getGroups().contains(value)) {
                 balanceFilter.removeGroup(value);
             }
         } else if (name.equals(_("Lecturer"))) {
             if (balanceFilter.getLecturers().contains(value)) {
                 balanceFilter.removeLecturer(value);
             }
         } else if (name.equals(_("Target audience"))) {
             if (balanceFilter.getTargetAudiences().contains(value)) {
                 balanceFilter.removeTargetAudience(value);
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.view.tabs.Tab#getTabName()
      */
     @Override
     public String getTabName() {
         return name;
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jSeparator4 = new javax.swing.JToolBar.Separator();
         yearLabel = new javax.swing.JLabel();
         yearComboBox = new javax.swing.JComboBox();
         jScrollPane1 = new javax.swing.JScrollPane();
         contentPanel = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         jToolBar1 = new javax.swing.JToolBar();
         jSeparator2 = new javax.swing.JToolBar.Separator();
         generateButton = new javax.swing.JButton();
         jSeparator1 = new javax.swing.JToolBar.Separator();
         exportButton = new javax.swing.JButton();
         jSeparator3 = new javax.swing.JToolBar.Separator();
         filtersPanel = new javax.swing.JPanel();
         filterContentPanel = new javax.swing.JPanel();
         filterCreationPanel = new javax.swing.JPanel();
         filtersLabel = new javax.swing.JLabel();
         filterNameComboBox = new javax.swing.JComboBox();
         filterComboBox = new javax.swing.JComboBox();
         addFilterButton = new javax.swing.JButton();
 
         yearLabel.setText(_("Year") + ":");
 
         setLayout(new java.awt.BorderLayout());
 
         contentPanel.setLayout(new javax.swing.BoxLayout(contentPanel,
             javax.swing.BoxLayout.Y_AXIS));
         jScrollPane1.setViewportView(contentPanel);
 
         add(jScrollPane1, java.awt.BorderLayout.CENTER);
 
         jPanel1.setLayout(new java.awt.BorderLayout());
 
         jToolBar1.setFloatable(false);
         jToolBar1.setRollover(true);
         jToolBar1.add(jSeparator2);
 
         generateButton.setText(_("Generate"));
         generateButton.setFocusable(false);
         generateButton
             .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         generateButton
             .setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jToolBar1.add(generateButton);
         jToolBar1.add(jSeparator1);
 
         exportButton.setText(_("Export"));
         exportButton.setFocusable(false);
         exportButton
             .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         exportButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jToolBar1.add(exportButton);
         jToolBar1.add(jSeparator3);
 
         jPanel1.add(jToolBar1, java.awt.BorderLayout.PAGE_START);
 
         filtersPanel.setLayout(new java.awt.BorderLayout());
 
         filterContentPanel.setLayout(new java.awt.GridLayout(0, 1));
         filtersPanel.add(filterContentPanel, java.awt.BorderLayout.PAGE_END);
 
         filterCreationPanel.setLayout(new java.awt.FlowLayout(
             java.awt.FlowLayout.LEFT));
 
         filtersLabel.setText(_("Filters") + ":");
         filterCreationPanel.add(filtersLabel);
 
         filterNameComboBox.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 filterNameComboBoxItemStateChanged(evt);
             }
         });
         filterCreationPanel.add(filterNameComboBox);
 
         filterCreationPanel.add(filterComboBox);
 
         addFilterButton.setIcon(new ImageIcon(getClass().getResource(
            "/de/aidger/view/icons/plus-small.png")));
         addFilterButton.setFocusable(false);
         addFilterButton
             .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         addFilterButton
             .setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         addFilterButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addFilterButtonActionPerformed(evt);
             }
         });
         filterCreationPanel.add(addFilterButton);
 
         filtersPanel.add(filterCreationPanel, java.awt.BorderLayout.PAGE_START);
 
         jPanel1.add(filtersPanel, java.awt.BorderLayout.CENTER);
 
         add(jPanel1, java.awt.BorderLayout.PAGE_START);
     }// </editor-fold>//GEN-END:initComponents
 
     private void addFilterButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_addFilterButtonActionPerformed
         switch (filterNameComboBox.getSelectedIndex()) {
         /*
          * If the filter doesn't exist, add it, repaint the combo box and set
          * the selected index of the name combo box to the type of filter added.
          */
         case 0:
             if (!balanceFilter.getGroups().contains(
                 filterComboBox.getSelectedItem().toString())
                     && filterComboBox.getItemCount() > 0) {
                 balanceFilter.addGroup((String) filterComboBox
                     .getSelectedItem());
                 filterContentPanel.add(new BalanceFilterPanel(
                     filterNameComboBox.getItemAt(0).toString(),
                     (String) filterComboBox.getSelectedItem(), this));
             }
             break;
         case 1:
             if (!balanceFilter.getLecturers().contains(
                 filterComboBox.getSelectedItem().toString())
                     && filterComboBox.getItemCount() > 0) {
                 balanceFilter.addLecturer((String) filterComboBox
                     .getSelectedItem());
                 filterContentPanel.add(new BalanceFilterPanel(
                     filterNameComboBox.getItemAt(1).toString(),
                     (String) filterComboBox.getSelectedItem(), this));
             }
             break;
         case 2:
             if (!balanceFilter.getTargetAudiences().contains(
                 filterComboBox.getSelectedItem().toString())
                     && filterComboBox.getItemCount() > 0) {
                 balanceFilter.addTargetAudience((String) filterComboBox
                     .getSelectedItem());
                 filterContentPanel.add(new BalanceFilterPanel(
                     filterNameComboBox.getItemAt(2).toString(),
                     (String) filterComboBox.getSelectedItem(), this));
             }
             break;
         }
         filterContentPanel.setVisible(false);
         filterContentPanel.setVisible(true);
     }// GEN-LAST:event_addFilterButtonActionPerformed
 
     private void filterNameComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {// GEN-FIRST:event_filterNameComboBoxItemStateChanged
         switch (filterNameComboBox.getSelectedIndex()) {
         /*
          * Clear the filter combo box and add all entries of this filter type.
          */
         case 0:
             filterComboBox.removeAllItems();
             List<ICourse> courses = null;
             try {
                 courses = (new Course()).getAll();
             } catch (AdoHiveException e) {
                 UI.displayError(e.toString());
             }
             ArrayList<String> courseGroups = new ArrayList<String>();
             for (ICourse course : courses) {
                 if (!courseGroups.contains(course.getGroup())) {
                     courseGroups.add(course.getGroup());
                     filterComboBox.addItem(course.getGroup());
                 }
             }
             break;
         case 1:
             filterComboBox.removeAllItems();
             courses = null;
             try {
                 courses = (new Course()).getAll();
             } catch (AdoHiveException e) {
                 UI.displayError(e.toString());
             }
             ArrayList<String> courseLecturers = new ArrayList<String>();
             for (ICourse course : courses) {
                 if (!courseLecturers.contains(course.getLecturer())) {
                     courseLecturers.add(course.getLecturer());
                     filterComboBox.addItem(course.getLecturer());
                 }
             }
             break;
         case 2:
             filterComboBox.removeAllItems();
             courses = null;
             try {
                 courses = (new Course()).getAll();
             } catch (AdoHiveException e) {
                 UI.displayError(e.toString());
             }
             ArrayList<String> courseAudiences = new ArrayList<String>();
             for (ICourse course : courses) {
                 if (!courseAudiences.contains(course.getTargetAudience())) {
                     courseAudiences.add(course.getTargetAudience());
                     filterComboBox.addItem(course.getTargetAudience());
                 }
             }
             break;
         }
     }// GEN-LAST:event_filterNameComboBoxItemStateChanged
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton addFilterButton;
     private javax.swing.JPanel contentPanel;
     private javax.swing.JButton exportButton;
     private javax.swing.JComboBox filterComboBox;
     private javax.swing.JPanel filterContentPanel;
     private javax.swing.JPanel filterCreationPanel;
     private javax.swing.JComboBox filterNameComboBox;
     private javax.swing.JLabel filtersLabel;
     private javax.swing.JPanel filtersPanel;
     private javax.swing.JButton generateButton;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JToolBar.Separator jSeparator1;
     private javax.swing.JToolBar.Separator jSeparator2;
     private javax.swing.JToolBar.Separator jSeparator3;
     private javax.swing.JToolBar.Separator jSeparator4;
     private javax.swing.JToolBar jToolBar1;
     private javax.swing.JComboBox yearComboBox;
     private javax.swing.JLabel yearLabel;
     // End of variables declaration//GEN-END:variables
 
 }
