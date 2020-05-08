 package de.aidger.view.tabs;
 
 import static de.aidger.utils.Translation._;
 
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jfree.data.general.DefaultPieDataset;
 import org.jfree.data.time.Day;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 
 import de.aidger.model.Runtime;
 import de.aidger.model.budgets.CourseBudget;
 import de.aidger.model.models.Activity;
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Course;
 import de.aidger.model.models.Employment;
 import de.aidger.model.models.FinancialCategory;
 import de.aidger.utils.history.HistoryEvent;
 import de.aidger.utils.history.HistoryManager;
 import de.aidger.utils.reports.BalanceHelper;
 import de.aidger.view.forms.HourlyWageEditorForm.Qualification;
 import de.aidger.view.models.UIActivity;
 import de.aidger.view.models.UIModel;
 import de.aidger.view.tabs.ViewerTab.DataType;
 import de.aidger.view.utils.Charts;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IActivity;
 import de.unistuttgart.iste.se.adohive.model.IAssistant;
 import de.unistuttgart.iste.se.adohive.model.ICourse;
 import de.unistuttgart.iste.se.adohive.model.IEmployment;
 import de.unistuttgart.iste.se.adohive.model.IFinancialCategory;
 
 /**
  * A tab which greats the user when the application starts.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class WelcomeTab extends Tab {
 
     /**
      * Initialises the WelcomeTab class.
      */
     @SuppressWarnings("unchecked")
     public WelcomeTab() {
         initComponents();
 
         final int countLastChanges = 5, countLastActivities = 10;
 
         if (Runtime.getInstance().isFirstStart()) {
             statisticsList.add(_("Currently no statistics are available."));
             lblFirstStart
                 .setText(_("This is the first time aidGer is started."));
             Runtime.getInstance().setOption("last-start",
                 Long.toString(new java.util.Date().getTime()));
         } else {
             lblFirstStart.setText(MessageFormat
                 .format(_("The last start of aidGer was on {0}."),
                     new Object[] { (new SimpleDateFormat("dd.MM.yy HH:mm"))
                         .format(new java.sql.Date(Long
                             .valueOf(Runtime.getInstance()
                                 .getOption(
                                     "last-start",
                                     Long.toString((new java.util.Date())
                                         .getTime()))))) }));
             Runtime.getInstance().setOption("last-start",
                 Long.toString(new java.util.Date().getTime()));
 
             List<IActivity> activities = null;
             List<IAssistant> assistants = null;
             List<IFinancialCategory> financials = null;
             List<ICourse> courses = null;
             List<IEmployment> employments = null;
             try {
                 activities = (new Activity()).getAll();
                 assistants = (new Assistant()).getAll();
                 financials = (new FinancialCategory()).getAll();
                 courses = (new Course()).getAll();
                 employments = (new Employment()).getAll();
             } catch (AdoHiveException ex) {
             }
 
             List<HistoryEvent> events = HistoryManager.getInstance()
                 .getEvents();
 
             int min = events.size() > countLastChanges ? events.size()
                     - countLastChanges : 0;
 
             for (int i = events.size() - 1; i >= min; --i) {
                 HistoryEvent evt = events.get(i);
 
                 try {
                     UIModel modelUI = evt.getModel();
                     String event = "";
 
                     switch (evt.status) {
                     case Added:
                         if (modelUI == null) {
                             event = MessageFormat
                                 .format(_("{0}: {1} with Id {2} was added."),
                                     new Object[] {
                                             (new SimpleDateFormat(
                                                 "dd.MM.yy HH:mm"))
                                                 .format(evt.date),
                                             DataType.valueOf(evt.type)
                                                 .getDisplayName(), evt.id });
                         } else {
                             event = MessageFormat
                                 .format(_("{0}: {1} {2} was added."),
                                     new Object[] {
                                             (new SimpleDateFormat(
                                                 "dd.MM.yy HH:mm"))
                                                 .format(evt.date),
                                             modelUI.getDataType()
                                                 .getDisplayName(),
                                             modelUI.toString() });
                         }
                         break;
                     case Changed:
                         if (modelUI == null) {
                             event = MessageFormat
                                 .format(_("{0}: {1} with Id {2} was edited."),
                                     new Object[] {
                                             (new SimpleDateFormat(
                                                 "dd.MM.yy HH:mm"))
                                                 .format(evt.date),
                                             DataType.valueOf(evt.type)
                                                 .getDisplayName(), evt.id });
                         } else {
                             event = MessageFormat
                                 .format(_("{0}: {1} {2} was edited."),
                                     new Object[] {
                                             (new SimpleDateFormat(
                                                 "dd.MM.yy HH:mm"))
                                                 .format(evt.date),
                                             modelUI.getDataType()
                                                 .getDisplayName(),
                                             modelUI.toString() });
                         }
                         break;
                     case Removed:
                         event = MessageFormat
                             .format(
                                 _("{0}: {1} with Id {2} was removed."),
                                 new Object[] {
                                         (new SimpleDateFormat("dd.MM.yy HH:mm"))
                                             .format(evt.date),
                                         DataType.valueOf(evt.type)
                                             .getDisplayName(), evt.id });
                         break;
                     }
 
                     historyList.add(event, modelUI);
                 } catch (Exception e) {
                 }
             }
 
             if (activities != null) {
                 min = activities.size() > countLastActivities ? activities
                     .size()
                         - countLastActivities : 0;
 
                 for (int i = activities.size() - 1; i >= min; --i) {
                     UIActivity a = new UIActivity(activities.get(i));
 
                     activitiesList.add(a.toString(), a);
                 }
             }
 
             Integer[] qualifications = new Integer[] { 0, 0, 0 };
             for (IAssistant a : assistants) {
                 if (Qualification.valueOf(a.getQualification()) == Qualification.u) {
                     ++qualifications[0];
                 } else if (Qualification.valueOf(a.getQualification()) == Qualification.g) {
                     ++qualifications[1];
                 } else {
                     ++qualifications[2];
                 }
             }
 
             int maxFunds = 0;
             for (IFinancialCategory f : financials) {
                 for (int b : f.getBudgetCosts()) {
                     maxFunds += b;
                 }
             }
 
             double bookedBudget = 0.0, totalBudget = 0.0;
             for (ICourse c : courses) {
                 CourseBudget courseBudget = new CourseBudget(new Course(c));
 
                 bookedBudget += courseBudget.getBookedBudget();
                 totalBudget += courseBudget.getTotalBudget();
             }
 
             double bookedBudgetCosts = 0.0;
             Map<Date, Integer> employmentsCount = new HashMap<Date, Integer>();
 
             for (IEmployment e : employments) {
                 bookedBudgetCosts += BalanceHelper.calculateBudgetCost(e);
 
                 Calendar cal = Calendar.getInstance();
                 cal.clear();
 
                 cal.set(Calendar.MONTH, e.getMonth() - 1);
                 cal.set(Calendar.YEAR, e.getYear());
 
                 if (employmentsCount.get(cal.getTime()) == null) {
                     employmentsCount.put(cal.getTime(), 1);
                 } else {
                     employmentsCount.put(cal.getTime(), employmentsCount
                         .get(cal.getTime()) + 1);
                 }
             }
 
             try {
                 statisticsList.add(MessageFormat.format(
                     _("aidGer has created {0} activities."),
                     new Object[] { activities.size() }));
                 statisticsList.add(MessageFormat
                     .format(
                         _("{0} assistants are working in {1} employments."),
                         new Object[] { assistants.size(),
                                 (new Employment()).size() }));
                 statisticsList.add(MessageFormat.format(
                     _("These employments are part of {0} courses."),
                     new Object[] { (new Course()).size() }));
                 statisticsList
                     .add(MessageFormat
                         .format(
                             _("Of the assistants {0} are unchecked, {1} are checked and {2} are bachelors."),
                             (Object[]) qualifications));
                 statisticsList.add(MessageFormat.format(
                     _("They are using funds of {0} Euros."),
                     new Object[] { maxFunds }));
             } catch (AdoHiveException ex) {
             }
 
             // create diagrams
             DefaultPieDataset qualificationData = new DefaultPieDataset();
             qualificationData.setValue(_("Unchecked"), qualifications[0]);
             qualificationData.setValue(_("Checked"), qualifications[1]);
             qualificationData.setValue(_("Bachelor"), qualifications[2]);
 
             int widthPie = 260, heightPie = 220;
 
             diagram1.setIcon(Charts.createPieChart3D(
                 _("Qualifications of all assistants"), qualificationData,
                 widthPie, heightPie));
 
             DefaultPieDataset budgetCourseData = new DefaultPieDataset();
             budgetCourseData.setValue(_("Booked budgets"), bookedBudget);
             budgetCourseData.setValue(_("Remaining budgets"), totalBudget
                     - bookedBudget);
 
             diagram2.setIcon(Charts.createPieChart3D(
                 _("Budget of all courses"), budgetCourseData, widthPie,
                 heightPie));
 
             DefaultPieDataset budgetFundsData = new DefaultPieDataset();
             budgetFundsData.setValue(_("Used budget"), bookedBudgetCosts);
             budgetFundsData.setValue(_("Remaining budget"), maxFunds
                     - bookedBudgetCosts);
 
             diagram3.setIcon(Charts.createPieChart3D(
                 _("Budget costs of all funds"), budgetFundsData, widthPie,
                 heightPie));
 
             TimeSeries seriesEmployments = new TimeSeries(_("Employments"));
 
             Set<Date> set = employmentsCount.keySet();
 
             for (Date date : set) {
                 seriesEmployments
                     .add(new Day(date), employmentsCount.get(date));
             }
 
             TimeSeriesCollection employmentsCountData = new TimeSeriesCollection(
                 seriesEmployments);
             diagram4.setIcon(Charts.createXYAreaChart(
                 _("Count of employments"), employmentsCountData, 500, 270));
         }
 
         if (historyList.count() == 0) {
             historyList.add(_("None"));
         }
 
         if (activitiesList.count() == 0) {
             activitiesList.add(_("None"));
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         lblTitle = new javax.swing.JLabel();
         lblFirstStart = new javax.swing.JLabel();
         boxes = new javax.swing.JPanel();
         lastChanges = new javax.swing.JPanel();
         historyList = new de.aidger.view.utils.BulletList();
         lastActivities = new javax.swing.JPanel();
         activitiesList = new de.aidger.view.utils.BulletList();
         statistics = new javax.swing.JPanel();
         statisticsList = new de.aidger.view.utils.BulletList();
         diagrams = new javax.swing.JPanel();
         diagram1 = new javax.swing.JLabel();
         diagram2 = new javax.swing.JLabel();
         diagram3 = new javax.swing.JLabel();
         diagram4 = new javax.swing.JLabel();
 
         setLayout(new java.awt.GridBagLayout());
 
         lblTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 24));
         lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblTitle.setText(_("Welcome to aidGer"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(5, 11, 10, 11);
         add(lblTitle, gridBagConstraints);
 
         lblFirstStart.setFont(new java.awt.Font("DejaVu Sans", 0, 14));
         lblFirstStart.setText(_("The last start of aidGer was on {0}."));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 20, 20, 0);
         add(lblFirstStart, gridBagConstraints);
 
         boxes.setLayout(new java.awt.GridBagLayout());
 
        lastChanges.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1),
            _("Last Changes")));
         lastChanges.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
         lastChanges.add(historyList, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 0.5;
         boxes.add(lastChanges, gridBagConstraints);
 
        lastActivities.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1),
            _("Last activities")));
         lastActivities.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
         lastActivities.add(activitiesList, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridheight = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 0.5;
         boxes.add(lastActivities, gridBagConstraints);
 
        statistics.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1),
            _("Statistics & Diagrams")));
         statistics.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
         statistics.add(statisticsList, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 0.5;
         boxes.add(statistics, gridBagConstraints);
 
         diagrams.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 0.3;
         diagrams.add(diagram1, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 0.3;
         diagrams.add(diagram2, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 0.3;
         diagrams.add(diagram3, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(30, 0, 0, 0);
         diagrams.add(diagram4, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         boxes.add(diagrams, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
         add(boxes, gridBagConstraints);
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Get the name of the tab.
      * 
      * @return The name
      */
     @Override
     public String getTabName() {
        return _("Overview");
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private de.aidger.view.utils.BulletList activitiesList;
     private javax.swing.JPanel boxes;
     private javax.swing.JLabel diagram1;
     private javax.swing.JLabel diagram2;
     private javax.swing.JLabel diagram3;
     private javax.swing.JLabel diagram4;
     private javax.swing.JPanel diagrams;
     private de.aidger.view.utils.BulletList historyList;
     private javax.swing.JPanel lastActivities;
     private javax.swing.JPanel lastChanges;
     private javax.swing.JLabel lblFirstStart;
     private javax.swing.JLabel lblTitle;
     private javax.swing.JPanel statistics;
     private de.aidger.view.utils.BulletList statisticsList;
     // End of variables declaration//GEN-END:variables
 
 }
