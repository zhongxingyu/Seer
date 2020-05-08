 package de.aidger.controller.actions;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.event.ActionEvent;
 import java.util.Vector;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 
 import de.aidger.model.reports.BalanceReportSemesterCreator;
 import de.aidger.utils.reports.BalanceHelper;
 import de.aidger.view.UI;
 import de.aidger.view.reports.BalanceReportSemesterPanel;
 import de.aidger.view.tabs.BalanceViewerTab;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 
 /**
  * This action generates a report.
  * 
  * @author aidGer Team
  */
 public class ReportGenerateAction extends AbstractAction {
 
     /**
      * Initializes the action.
      */
     public ReportGenerateAction() {
         putValue(Action.NAME, _("Generate"));
         putValue(Action.SHORT_DESCRIPTION, _("Generate the report"));
 
         putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource(
             "/de/aidger/view/icons/report.png")));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     public void actionPerformed(ActionEvent e) {
         BalanceViewerTab tab = (BalanceViewerTab) UI.getInstance()
             .getCurrentTab();
 
         if (new BalanceHelper().getSemesters().size() > 0) {
             switch (tab.getType()) {
             case 1:
                 tab.clearPanel();
                 Vector semesters = new BalanceHelper().getSemesters();
                 for (int i = 0; i < semesters.size(); i++) {
                    BalanceReportSemesterCreator fullBalanceCreator = null;
                    try {
                        fullBalanceCreator = new BalanceReportSemesterCreator(
                            (String) semesters.get(i), tab.getBalanceFilter());
                    } catch (AdoHiveException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                     }
                    tab.addPanel(new BalanceReportSemesterPanel(
                        (String) semesters.get(i), fullBalanceCreator));
                 }
                 break;
             case 2:
                 tab.clearPanel();
 
                 Object year = tab.getYear();
                 String[] yearSemesters = new BalanceHelper()
                     .getYearSemesters((Integer) year);
                 for (int i = 0; i < yearSemesters.length; i++) {
                     if (new BalanceHelper().courseExists(yearSemesters[i], tab
                         .getBalanceFilter())) {
                         try {
                             BalanceReportSemesterCreator yearBalanceCreator = new BalanceReportSemesterCreator(
                                 yearSemesters[i], tab.getBalanceFilter());
                             tab.addPanel(new BalanceReportSemesterPanel(
                                 yearSemesters[i], yearBalanceCreator));
                         } catch (AdoHiveException e1) {
                             // TODO Auto-generated catch block
                             e1.printStackTrace();
                         }
                     }
                 }
                 break;
             case 3:
                 tab.clearPanel();
 
                 Object year2 = tab.getYear();
                 if (year2 != null) {
                     BalanceReportSemesterCreator fullBalanceCreator = null;
                     try {
                         fullBalanceCreator = new BalanceReportSemesterCreator(
                             (String) year2, tab.getBalanceFilter());
                     } catch (AdoHiveException e1) {
                         // TODO Auto-generated catch block
                         e1.printStackTrace();
                     }
                     tab.addPanel(new BalanceReportSemesterPanel((String) year2,
                         fullBalanceCreator));
                 }
             }
         }
     }
 
 }
