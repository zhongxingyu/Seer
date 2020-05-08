 package de.aidger.view.tabs;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JPanel;
 
 import de.aidger.controller.ActionNotFoundException;
 import de.aidger.controller.ActionRegistry;
 import de.aidger.controller.actions.DetailViewerCancelAction;
 import de.aidger.controller.actions.DetailViewerEditAction;
 import de.aidger.model.AbstractModel;
 import de.aidger.model.models.Activity;
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Contract;
 import de.aidger.model.models.Course;
 import de.aidger.model.models.Employment;
 import de.aidger.model.models.FinancialCategory;
 import de.aidger.model.models.HourlyWage;
 import de.aidger.view.UI;
 import de.aidger.view.forms.AssistantViewerForm;
 import de.aidger.view.forms.ContractViewerForm;
 import de.aidger.view.forms.CourseViewerForm;
 import de.aidger.view.forms.EmploymentViewerForm;
 import de.aidger.view.forms.FinancialCategoryViewerForm;
 import de.aidger.view.forms.HourlyWageViewerForm;
 import de.aidger.view.models.GenericListModel;
 import de.aidger.view.models.ListModel;
 import de.aidger.view.models.UIAssistant;
 import de.aidger.view.models.UICourse;
 import de.aidger.view.tabs.ViewerTab.DataType;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IAssistant;
 import de.unistuttgart.iste.se.adohive.model.ICourse;
 
 /**
  * A tab which will be used to view the model.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class DetailViewerTab extends Tab {
 
     /**
      * The model for the detail viewer.
      */
     @SuppressWarnings("unchecked")
     protected AbstractModel model = null;
 
     /**
      * The type of the data.
      */
     private final DataType type;
 
     private class ListProperty {
         public String borderName = new String();
         public DataType type;
     }
 
     /**
      * The names of the list borders.
      */
     private final ListProperty[] listProperties = new ListProperty[2];
 
     /**
      * Constructs a data detail viewer tab.
      * 
      * @param type
      *            type of the data
      * @param model
      *            the data model
      */
     @SuppressWarnings("unchecked")
     public DetailViewerTab(DataType type, AbstractModel model) {
         this.type = type;
         this.model = model;
 
         init();
 
         // init the related lists
 
         listProperties[0] = new ListProperty();
         listProperties[1] = new ListProperty();
 
         switch (type) {
         case Course:
             initLists((Course) model);
             break;
         case Assistant:
             initLists((Assistant) model);
             break;
         case FinancialCategory:
             initLists((FinancialCategory) model);
             break;
         }
 
         if (list1.getModel().getSize() > 0) {
             panelList1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                 javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1),
                 listProperties[0].borderName));
 
             listModels.add((GenericListModel) list1.getModel());
 
             list1.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mousePressed(MouseEvent evt) {
                     if (evt.getClickCount() == 2) {
                         UI.getInstance().addNewTab(
                             new DetailViewerTab(listProperties[0].type,
                                 (AbstractModel) list1.getSelectedValue()));
                     }
                 }
             });
 
             lblNone1.setVisible(false);
         } else if (!listProperties[0].borderName.isEmpty()) {
             panelList1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                 javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1),
                 listProperties[0].borderName));
 
             scrollPane1.setVisible(false);
         } else {
             panelList1.setVisible(false);
         }
 
         if (list2.getModel().getSize() > 0) {
             panelList2.setBorder(javax.swing.BorderFactory.createTitledBorder(
                 javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1),
                 listProperties[1].borderName));
 
             listModels.add((GenericListModel) list2.getModel());
 
             list2.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mousePressed(MouseEvent evt) {
                     if (evt.getClickCount() == 2) {
                         UI.getInstance().addNewTab(
                             new DetailViewerTab(listProperties[1].type,
                                 (AbstractModel) list2.getSelectedValue()));
                     }
                 }
             });
 
             lblNone2.setVisible(false);
         } else if (!listProperties[1].borderName.isEmpty()) {
             panelList2.setBorder(javax.swing.BorderFactory.createTitledBorder(
                 javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1),
                 listProperties[1].borderName));
 
             scrollPane2.setVisible(false);
         } else {
             panelList2.setVisible(false);
         }
     }
 
     /**
      * Return a parseable string that lets the UI initalise the tab.
      * 
      * @return A parseable string
      */
     @Override
     public String toString() {
         String ret = getClass().getName() + "<" + DataType.class.getName()
                 + "@" + type + "<" + model.getClass().getName();
         if (model.getClass().equals(HourlyWage.class)) {
             HourlyWage h = (HourlyWage) model;
             ret += "@" + h.getQualification() + "@" + h.getMonth() + "@"
                     + h.getYear();
         } else {
             ret += "@" + model.getId();
         }
 
         return ret;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.view.tabs.Tab#getPredecessor()
      */
     @Override
     public Tab getPredecessor() {
         Tab p = super.getPredecessor();
 
         if (p == null) {
             p = new ViewerTab(getType());
         }
 
         return p;
     }
 
     /**
      * Initializes the components and the button actions.
      */
     private void init() {
         initComponents();
 
         try {
             btnEdit.setAction(ActionRegistry.getInstance().get(
                 DetailViewerEditAction.class.getName()));
 
             btnCancel.setAction(ActionRegistry.getInstance().get(
                 DetailViewerCancelAction.class.getName()));
         } catch (ActionNotFoundException e) {
             UI.displayError(e.getMessage());
         }
 
     }
 
     /**
      * Initializes the lists for a course.
      * 
      * @param course
      *            the course
      */
     private void initLists(Course course) {
         listProperties[0].borderName = _("Related assistants");
         listProperties[0].type = DataType.Assistant;
 
         listProperties[1].borderName = _("Related activities");
         listProperties[1].type = DataType.Activity;
 
         try {
             List<Employment> employments = (new Employment())
                 .getEmployments(course);
 
             ListModel listAssistantsModel = new ListModel(DataType.Assistant);
 
             Map<Integer, Double> totalHours = new HashMap<Integer, Double>();
 
             for (Employment employment : employments) {
                 if (totalHours.get(employment.getAssistantId()) == null) {
                     totalHours.put(employment.getAssistantId(), employment
                         .getHourCount());
                 } else {
                     totalHours.put(employment.getAssistantId(), totalHours
                         .get(employment.getAssistantId())
                             + employment.getHourCount());
                 }
             }
 
             Set<Integer> set = totalHours.keySet();
 
             for (Integer assistantId : set) {
                 IAssistant a = (new Assistant()).getById(assistantId);
 
                 UIAssistant assistant = new UIAssistant(a);
                 assistant.setTotalHours(new BigDecimal(totalHours
                     .get(assistantId)).setScale(2, BigDecimal.ROUND_HALF_EVEN)
                     .doubleValue());
 
                 listAssistantsModel.addElement(assistant);
             }
 
             List<Activity> activities = (new Activity()).getActivities(course);
 
             ListModel listActivitiesModel = new ListModel(DataType.Activity);
 
             for (Activity activity : activities) {
                 listActivitiesModel.addElement(activity);
             }
 
             list1.setModel(listAssistantsModel);
             list2.setModel(listActivitiesModel);
         } catch (AdoHiveException e) {
         }
     }
 
     /**
      * Initializes the lists for an assistant.
      * 
      * @param assistant
      *            the assisant
      */
     private void initLists(Assistant assistant) {
         listProperties[0].borderName = _("Related courses");
         listProperties[0].type = DataType.Course;
 
         listProperties[1].borderName = _("Related activities");
         listProperties[1].type = DataType.Activity;
 
         try {
             List<Employment> employments = (new Employment())
                 .getEmployments(assistant);
 
             ListModel listCoursesModel = new ListModel(DataType.Course);
 
             for (Employment employment : employments) {
                 ICourse c = (new Course()).getById(employment.getCourseId());
 
                 Course course = new UICourse(c);
 
                 if (!listCoursesModel.contains(course)) {
                     listCoursesModel.addElement(course);
                 }
             }
 
             List<Activity> activities = (new Activity())
                 .getActivities(assistant);
 
             ListModel listActivitiesModel = new ListModel(DataType.Activity);
 
             for (Activity activity : activities) {
                 listActivitiesModel.addElement(activity);
             }
 
             list1.setModel(listCoursesModel);
             list2.setModel(listActivitiesModel);
         } catch (AdoHiveException e) {
         }
     }
 
     /**
      * Initializes the lists for a financial category.
      * 
      * @param fc
      *            the financial category
      */
     private void initLists(FinancialCategory fc) {
         listProperties[0].borderName = _("Related courses");
         listProperties[0].type = DataType.Course;
 
         try {
             List<Course> courses = (new Course()).getCourses(fc);
 
             ListModel listCoursesModel = new ListModel(DataType.Course);
 
             for (Course c : courses) {
                 Course course = new UICourse(c);
 
                 if (!listCoursesModel.contains(course)) {
                     listCoursesModel.addElement(course);
                 }
             }
 
             list1.setModel(listCoursesModel);
         } catch (AdoHiveException e) {
         }
     }
 
     /**
      * Returns the data model.
      * 
      * @return the data model
      */
     @SuppressWarnings("unchecked")
     public AbstractModel getModel() {
         return model;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.view.tabs.Tab#getTabName()
      */
     @Override
     public String getTabName() {
         switch (type) {
         case Course:
             return _("View course");
         case Assistant:
             return _("View assistant");
         case FinancialCategory:
             return _("View financial category");
         case HourlyWage:
             return _("View hourly wage");
         case Employment:
             return _("View employment");
         case Contract:
             return _("View contract");
         default:
             return "";
         }
     }
 
     /**
      * Returns the type of the data that is added or edited.
      * 
      * @return the type of the data
      */
     public DataType getType() {
         return type;
     }
 
     /**
      * Returns the data viewer form.
      * 
      * @return the data viewer form.
      */
     public JPanel getViewerForm() {
         return viewerForm;
     }
 
     /**
      * Creates a new viewer form.
      * 
      * @return the created viewer form
      */
     public JPanel createViewerForm() {
         switch (type) {
         case Course:
             return new CourseViewerForm((Course) model);
         case Assistant:
             return new AssistantViewerForm((Assistant) model);
         case FinancialCategory:
             return new FinancialCategoryViewerForm((FinancialCategory) model);
         case HourlyWage:
             return new HourlyWageViewerForm((HourlyWage) model);
         case Employment:
             return new EmploymentViewerForm((Employment) model);
         case Contract:
             return new ContractViewerForm((Contract) model);
         default:
             return new JPanel();
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
 
         viewerForm = createViewerForm();
         buttons = new javax.swing.JPanel();
         btnEdit = new javax.swing.JButton();
         btnCancel = new javax.swing.JButton();
         filler = new javax.swing.JLabel();
         filler1 = new javax.swing.JLabel();
         panelLists = new javax.swing.JPanel();
         panelList1 = new javax.swing.JPanel();
         scrollPane1 = new javax.swing.JScrollPane();
         list1 = new javax.swing.JList();
         lblNone1 = new javax.swing.JLabel();
         filler2 = new javax.swing.JLabel();
         filler3 = new javax.swing.JLabel();
         panelList2 = new javax.swing.JPanel();
         scrollPane2 = new javax.swing.JScrollPane();
         list2 = new javax.swing.JList();
         lblNone2 = new javax.swing.JLabel();
         filler4 = new javax.swing.JLabel();
         filler5 = new javax.swing.JLabel();
 
         setLayout(new java.awt.GridBagLayout());
 
        viewerForm.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), getTabName()));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         add(viewerForm, gridBagConstraints);
 
         btnEdit.setText(_("Edit"));
         buttons.add(btnEdit);
 
         btnCancel.setText(_("Cancel"));
         buttons.add(btnCancel);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
         add(buttons, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.weighty = 1.0;
         add(filler, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridheight = 3;
         gridBagConstraints.weightx = 1.0;
         add(filler1, gridBagConstraints);
 
         panelLists.setLayout(new java.awt.GridBagLayout());
 
         panelList1.setLayout(new java.awt.GridBagLayout());
 
         scrollPane1.setMinimumSize(new java.awt.Dimension(350, 200));
         scrollPane1.setPreferredSize(new java.awt.Dimension(350, 200));
         scrollPane1.setViewportView(list1);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         panelList1.add(scrollPane1, gridBagConstraints);
 
         lblNone1.setText(_("None"));
         lblNone1.setPreferredSize(new java.awt.Dimension(350, 15));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         panelList1.add(lblNone1, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.weighty = 1.0;
         panelList1.add(filler2, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.weightx = 1.0;
         panelList1.add(filler3, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
         panelLists.add(panelList1, gridBagConstraints);
 
         panelList2.setLayout(new java.awt.GridBagLayout());
 
         scrollPane2.setMinimumSize(new java.awt.Dimension(350, 200));
         scrollPane2.setPreferredSize(new java.awt.Dimension(350, 200));
         scrollPane2.setViewportView(list2);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         panelList2.add(scrollPane2, gridBagConstraints);
 
         lblNone2.setText(_("None"));
         lblNone2.setPreferredSize(new java.awt.Dimension(350, 15));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         panelList2.add(lblNone2, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.weighty = 1.0;
         panelList2.add(filler4, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.weightx = 1.0;
         panelList2.add(filler5, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         panelLists.add(panelList2, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridheight = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 50, 0, 0);
         add(panelLists, gridBagConstraints);
     }// </editor-fold>//GEN-END:initComponents
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCancel;
     private javax.swing.JButton btnEdit;
     private javax.swing.JPanel buttons;
     private javax.swing.JLabel filler;
     private javax.swing.JLabel filler1;
     private javax.swing.JLabel filler2;
     private javax.swing.JLabel filler3;
     private javax.swing.JLabel filler4;
     private javax.swing.JLabel filler5;
     private javax.swing.JLabel lblNone1;
     private javax.swing.JLabel lblNone2;
     private javax.swing.JList list1;
     private javax.swing.JList list2;
     private javax.swing.JPanel panelList1;
     private javax.swing.JPanel panelList2;
     private javax.swing.JPanel panelLists;
     private javax.swing.JScrollPane scrollPane1;
     private javax.swing.JScrollPane scrollPane2;
     private javax.swing.JPanel viewerForm;
     // End of variables declaration//GEN-END:variables
 
 }
