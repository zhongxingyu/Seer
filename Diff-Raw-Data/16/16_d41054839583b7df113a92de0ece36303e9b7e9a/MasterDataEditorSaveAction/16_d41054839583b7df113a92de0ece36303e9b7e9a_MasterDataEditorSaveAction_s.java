 package de.aidger.controller.actions.masterdata;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.event.ActionEvent;
 import java.math.BigDecimal;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import de.aidger.model.AbstractModel;
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Course;
 import de.aidger.model.models.FinancialCategory;
 import de.aidger.model.models.HourlyWage;
 import de.aidger.view.UI;
 import de.aidger.view.forms.AssistantEditorForm;
 import de.aidger.view.forms.CourseEditorForm;
 import de.aidger.view.forms.FinancialCategoryEditorForm;
 import de.aidger.view.forms.HourlyWageEditorForm;
 import de.aidger.view.models.MasterDataTableModel;
 import de.aidger.view.tabs.MasterDataEditorTab;
 import de.aidger.view.tabs.MasterDataViewerTab;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 
 /**
  * This action saves the master data and replaces the current tab with the
  * master data viewer tab.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class MasterDataEditorSaveAction extends AbstractAction {
 
     /**
      * Initializes the action.
      */
     public MasterDataEditorSaveAction() {
         putValue(Action.NAME, _("Save"));
     }
 
     /**
      * Sets the given course model to the values of the form.
      * 
      * @param course
      *            the course model
      * @param form
      *            the course editor form
      */
     private void setModel(Course course, CourseEditorForm form) {
         course.setDescription(form.getDescription());
         course.setSemester(form.getSemester());
         course.setLecturer(form.getLecturer());
         course.setAdvisor(form.getAdvisor());
         course.setNumberOfGroups(form.getNumberOfGroups());
         course.setTargetAudience(form.getTargetAudience());
         course.setUnqualifiedWorkingHours(form.getUnqualifiedWorkingHours());
         course.setScope(form.getScope());
         course.setPart(form.getPart());
         course.setGroup(form.getGroup());
         course.setRemark(form.getRemark());
     }
 
     /**
      * Sets the given assistant model to the values of the form.
      * 
      * @param assistant
      *            the assistant model
      * @param form
      *            the assistant editor form
      */
     private void setModel(Assistant assistant, AssistantEditorForm form) {
         assistant.setFirstName(form.getFirstName());
         assistant.setLastName(form.getLastName());
         assistant.setEmail(form.getEmail());
         assistant.setQualification(form.getQualification());
     }
 
     /**
      * Sets the given financial category model to the values of the form.
      * 
      * @param fc
      *            the financial category model
      * @param form
      *            the financial category editor form
      */
     private void setModel(FinancialCategory fc, FinancialCategoryEditorForm form) {
         fc.setName(form.getFCName());
         fc.setBudgetCosts(form.getBudgetCosts());
         fc.setFunds(form.getFunds());
         fc.setYear(form.getYear());
     }
 
     /**
      * Sets the given hourly wage model to the values of the form.
      * 
      * @param hw
      *            the hourly wage model
      * @param form
      *            the hourly wage editor form
      */
     private void setModel(HourlyWage hw, HourlyWageEditorForm form) {
         hw.setQualification(form.getQualification());
         hw.setMonth(form.getMonth());
         hw.setYear(form.getYear());
 
         String wage = form.getWage();
         if (!wage.isEmpty()) {
             hw.setWage(new BigDecimal(wage));
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     @SuppressWarnings("unchecked")
     @Override
     public void actionPerformed(ActionEvent e) {
         JButton btnSave = (JButton) e.getSource();
         JPanel buttons = (JPanel) btnSave.getParent();
         MasterDataEditorTab tab = (MasterDataEditorTab) buttons.getParent();
 
         AbstractModel model = tab.getModel();
 
         switch (tab.getType()) {
         case Course:
             setModel((Course) model, (CourseEditorForm) tab.getEditorForm());
             break;
         case Assistant:
             setModel((Assistant) model, (AssistantEditorForm) tab
                     .getEditorForm());
             break;
         case FinancialCategory:
             setModel((FinancialCategory) model,
                     (FinancialCategoryEditorForm) tab.getEditorForm());
             break;
         case HourlyWage:
             setModel((HourlyWage) model, (HourlyWageEditorForm) tab
                     .getEditorForm());
             break;
         }
 
         List<MasterDataTableModel> tableModels = MasterDataViewerTab.tableModels
                 .get(tab.getType());
 
         for (MasterDataTableModel tableModel : tableModels) {
             model.addObserver(tableModel);
         }
 
         try {
             if (!model.save()) {
                 tab.updateHints();
 
                 return;
             }
        } catch (AdoHiveException e1) {
             UI.displayError(_("Could not save the model to database."));
 
            System.out.println(e1.getMessage());
         }
 
         UI.getInstance().replaceCurrentTab(
                 new MasterDataViewerTab(tab.getType()));
     }
 }
