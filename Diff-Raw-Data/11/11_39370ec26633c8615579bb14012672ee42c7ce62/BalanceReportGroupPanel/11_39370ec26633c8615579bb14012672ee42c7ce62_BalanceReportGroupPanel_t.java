 package de.aidger.view.reports;
 
 import static de.aidger.utils.Translation._;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 
 import javax.swing.table.DefaultTableModel;
 
 import de.aidger.model.reports.BalanceCourse;
 import de.aidger.model.reports.BalanceReportGroupCreator;
 import de.aidger.model.reports.BalanceCourse.BudgetCost;
 
 /**
  * A JPanel which has the courses of a group in it.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class BalanceReportGroupPanel extends javax.swing.JPanel {
 
     /**
      * The name of the group.
      */
     private final String group;
 
     /**
      * The group creator, which adds courses to this group panel.
      */
     private BalanceReportGroupCreator groupCreator = null;
 
     /**
      * The table model of the content table.
      */
     private final DefaultTableModel groupTableModel = new DefaultTableModel(
         null, new String[] { _("Title"), _("Part"), _("Lecturer"),
                 _("Target Audience"), _("Planned AWS"), _("Basic needed AWS") }) {
 
         @Override
         public boolean isCellEditable(int rowIndex, int columnIndex) {
             return false;
         }
     };
 
     /**
      * Initializes a new BalanceReportGroupViewer and adds its courses.
      * 
      * @param group
      *            The name of the group.
      */
     public BalanceReportGroupPanel(String group,
             BalanceReportGroupCreator groupCreator) {
         this.groupCreator = groupCreator;
         this.group = group;
         initComponents();
         contentPanel.add(contentTable.getTableHeader(),
             java.awt.BorderLayout.NORTH);
         addCourses();
     }
 
     /**
      * Adds the courses of this group.
      */
     private void addCourses() {
         ArrayList<Object> sums = new ArrayList<Object>();
         /*
          * Create the row for the sums.
          */
         sums.add(_("Sum"));
         sums.add("");
         sums.add("");
         sums.add("");
         sums.add(0.0);
         sums.add(0.0);
         ArrayList<Integer> costUnits = new ArrayList<Integer>();
         ArrayList<BalanceCourse> balanceCourses = groupCreator
             .getBalanceCourses();
         for (Object balanceCourse : balanceCourses) {
             ArrayList<Object> rowObjectVector = new ArrayList<Object>();
             /*
              * Add the first 6 columns to the table, whose names are always the
              * same.
              */
             for (int i = 0; i < 6; i++) {
                 rowObjectVector.add(((BalanceCourse) balanceCourse)
                     .getCourseObject()[i]);
             }
             sums.set(4, (Double) sums.get(4) + (Double) rowObjectVector.get(4));
             sums.set(5, (Double) sums.get(5) + (Double) rowObjectVector.get(5));
             /*
              * If there are additional columns already, add empty strings to
              * them.
              */
             while (rowObjectVector.size() < costUnits.size() + 6) {
                 rowObjectVector.add("");
             }
             for (BudgetCost budgetCost : ((BalanceCourse) balanceCourse)
                 .getBudgetCosts()) {
                 String budgetCostName = budgetCost.getName();
                 int budgetCostId = budgetCost.getId();
                 /*
                  * Check by the id of the cost unit if that cost unit is already
                  * in the table. If not, add it.
                  */
                 if (!costUnits.contains(budgetCostId)) {
                     costUnits.add(budgetCostId);
                     rowObjectVector.add("");
                     addEmptyColumn(budgetCostName);
                 }
                 /*
                  * Set the budget cost of the cost unit to the one specified.
                  */
                 rowObjectVector.set(costUnits.indexOf(budgetCostId) + 6,
                     budgetCost.getValue());
             }
             for (int i = 6; i < rowObjectVector.size(); i++) {
                 if (sums.size() < i + 1) {
                     if (!rowObjectVector.get(i).equals("")) {
                         sums.add(rowObjectVector.get(i));
                     }
                 } else {
                     if (!rowObjectVector.get(i).equals("")) {
                         sums.set(i, (Double) sums.get(i)
                                 + (Double) rowObjectVector.get(i));
                     }
                 }
             }
             addCourse(rowObjectVector.toArray());
         }
         /*
          * Round the entries of sums.
          */
         for (int i = 4; i < sums.size(); i++) {
             sums.set(i, new BigDecimal((Double) sums.get(i)).setScale(2,
                 BigDecimal.ROUND_HALF_EVEN));
         }
         /*
          * Add the sums of the costs and hours after an empty row.
          */
         addCourse(null);
         addCourse(sums.toArray());
     }
 
     /**
      * Add an empty column to the table with the given name as title.
      * 
      * @param name
      *            The title of the column.
      */
     private void addEmptyColumn(String name) {
         groupTableModel.addColumn(_("Budget costs from") + " " + name);
     }
 
     /**
      * Adds a course to the table.
      * 
      * @param course
      *            The course to be added to the table.
      */
     public void addCourse(Object[] course) {
         groupTableModel.addRow(course);
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed"
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         groupNameLabel = new javax.swing.JLabel();
         contentPanel = new javax.swing.JPanel();
         contentTable = new javax.swing.JTable();
 
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 5, 0));
         setLayout(new java.awt.BorderLayout(0, 5));
 
         groupNameLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         groupNameLabel.setText(_("Group") + (" ") + group);
         add(groupNameLabel, java.awt.BorderLayout.PAGE_START);
 
         contentPanel.setLayout(new java.awt.BorderLayout());
 
         contentTable.setModel(groupTableModel);
         contentPanel.add(contentTable, java.awt.BorderLayout.CENTER);
 
         add(contentPanel, java.awt.BorderLayout.CENTER);
     }// </editor-fold>//GEN-END:initComponents
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel contentPanel;
     private javax.swing.JTable contentTable;
     private javax.swing.JLabel groupNameLabel;
     // End of variables declaration//GEN-END:variables
 
 }
