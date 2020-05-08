 package de.aidger.view.tabs;
 
 import static de.aidger.utils.Translation._;
 
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import javax.swing.JLabel;
 
 import de.aidger.model.AbstractModel;
 import de.aidger.model.Runtime;
 import de.aidger.model.models.Activity;
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Course;
 import de.aidger.model.models.Employment;
 import de.aidger.model.models.FinancialCategory;
 import de.aidger.utils.history.HistoryEvent;
 import de.aidger.utils.history.HistoryManager;
 import de.aidger.view.models.UIActivity;
 import de.aidger.view.models.UIModel;
 import de.aidger.view.utils.BulletList;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IActivity;
 import de.unistuttgart.iste.se.adohive.model.IAssistant;
 import de.unistuttgart.iste.se.adohive.model.IFinancialCategory;
 
 /**
  * A tab which greats the user when the application starts.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class WelcomeTab extends Tab {
 
     /**
      * The history list.
      */
     private final BulletList historyList = new BulletList();
 
     /**
      * The activities list.
      */
     private final BulletList activitiesList = new BulletList();
 
     /**
      * The statistics list.
      */
     private final BulletList statisticsList = new BulletList();
 
     /**
      * Initialises the WelcomeTab class.
      */
     @SuppressWarnings("unchecked")
     public WelcomeTab() {
         initComponents();
 
         int count = 10;
 
         if (Runtime.getInstance().isFirstStart()) {
             statisticsList.add(_("Currently no statistics are available."));
         } else {
             List<IActivity> activities = null;
             List<IAssistant> assistants = null;
             List<IFinancialCategory> financials = null;
             try {
                 activities = (new Activity()).getAll();
                 assistants = (new Assistant()).getAll();
                 financials = (new FinancialCategory()).getAll();
             } catch (AdoHiveException ex) {
             }
 
             List<HistoryEvent> events = HistoryManager.getInstance()
                 .getEvents();
 
            int min = activities.size() > count ? activities.size() - count : 0;
 
             for (int i = events.size() - 1; i >= min; --i) {
                 HistoryEvent evt = events.get(i);
 
                 try {
                     Class obj = Class.forName("de.aidger.model.models."
                             + evt.type);
                     AbstractModel a = (AbstractModel) obj.newInstance();
                     Object o;
 
                     o = a.getById(evt.id);
 
                     Class classUI = Class.forName("de.aidger.view.models.UI"
                             + evt.type);
 
                     Class classInterface = Class
                         .forName("de.unistuttgart.iste.se.adohive.model.I"
                                 + evt.type);
 
                     Object model = classUI.getConstructor(classInterface)
                         .newInstance(classInterface.cast(o));
 
                     UIModel modelUI = (UIModel) model;
 
                     String event = "";
 
                     switch (evt.status) {
                     case Added:
                         event = MessageFormat.format(
                             _("{0}: {1} {2} was added."), new Object[] {
                                     (new SimpleDateFormat("dd.MM.yy HH:mm"))
                                         .format(evt.date),
                                     modelUI.getDataType().getDisplayName(),
                                     modelUI.toString() });
                         break;
                     case Changed:
                         event = MessageFormat.format(
                             _("{0}: {1} {2} was edited."), new Object[] {
                                     (new SimpleDateFormat("dd.MM.yy HH:mm"))
                                         .format(evt.date),
                                     modelUI.getDataType().getDisplayName(),
                                     modelUI.toString() });
                         break;
                     case Removed:
                         event = MessageFormat.format(
                             _("{0}: {1} {2} was removed."), new Object[] {
                                     (new SimpleDateFormat("dd.MM.yy HH:mm"))
                                         .format(evt.date),
                                     modelUI.getDataType().getDisplayName(),
                                     modelUI.toString() });
                         break;
                     }
 
                     historyList.add(event);
                 } catch (Exception e) {
                 }
             }
 
             if (activities != null) {
                 min = activities.size() > count ? activities.size() - count : 0;
 
                 for (int i = activities.size() - 1; i >= min; --i) {
                     activitiesList.add((new UIActivity(activities.get(i))
                         .toString()));
                 }
             }
 
             Integer[] qualifications = new Integer[] { 0, 0, 0 };
             for (IAssistant a : assistants) {
                 if (a.getQualification().equals("u")) {
                     ++qualifications[0];
                 } else if (a.getQualification().equals("c")) {
                     ++qualifications[1];
                 } else {
                     ++qualifications[2];
                 }
             }
 
             int funds = 0;
             for (IFinancialCategory f : financials) {
                 for (int b : f.getBudgetCosts()) {
                     funds += b;
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
                     new Object[] { funds }));
             } catch (AdoHiveException ex) {
             }
         }
 
         lastChanges.add(new JLabel(historyList.getList()));
         lastActivities.add(new JLabel(activitiesList.getList()));
         lblStatistics.setText(statisticsList.getList());
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
         lastActivities = new javax.swing.JPanel();
         statistics = new javax.swing.JPanel();
         lblStatistics = new javax.swing.JLabel();
         diagram1 = new javax.swing.JLabel();
         diagram2 = new javax.swing.JLabel();
         diagram3 = new javax.swing.JLabel();
 
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
         gridBagConstraints.insets = new java.awt.Insets(0, 20, 20, 0);
         add(lblFirstStart, gridBagConstraints);
 
         boxes.setLayout(new java.awt.GridBagLayout());
 
         lastChanges.setBorder(javax.swing.BorderFactory.createTitledBorder(
             javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1),
             _("Last Changes")));
         lastChanges.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,
             0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
         boxes.add(lastChanges, gridBagConstraints);
 
         lastActivities.setBorder(javax.swing.BorderFactory.createTitledBorder(
             javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1),
             _("Last activities")));
         lastActivities.setLayout(new java.awt.FlowLayout(
             java.awt.FlowLayout.LEFT, 0, 0));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
         boxes.add(lastActivities, gridBagConstraints);
 
         statistics.setBorder(javax.swing.BorderFactory.createTitledBorder(
             javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1),
             _("Statistics & Diagrams")));
         statistics.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.weighty = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
         statistics.add(lblStatistics, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.weighty = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
         statistics.add(diagram1, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.weighty = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
         statistics.add(diagram2, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.weighty = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
         statistics.add(diagram3, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
         boxes.add(statistics, gridBagConstraints);
 
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
         return _("Welcome to aidGer");
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel boxes;
     private javax.swing.JLabel diagram1;
     private javax.swing.JLabel diagram2;
     private javax.swing.JLabel diagram3;
     private javax.swing.JPanel lastActivities;
     private javax.swing.JPanel lastChanges;
     private javax.swing.JLabel lblFirstStart;
     private javax.swing.JLabel lblStatistics;
     private javax.swing.JLabel lblTitle;
     private javax.swing.JPanel statistics;
     // End of variables declaration//GEN-END:variables
 
 }
