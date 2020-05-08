 package de.aidger.controller.actions;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.event.ActionEvent;
 import java.math.BigDecimal;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 
 import de.aidger.model.AbstractModel;
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Course;
 import de.aidger.model.models.FinancialCategory;
 import de.aidger.model.models.HourlyWage;
 import de.aidger.model.validators.PresenceValidator;
 import de.aidger.view.UI;
 import de.aidger.view.forms.AssistantEditorForm;
 import de.aidger.view.forms.CourseEditorForm;
 import de.aidger.view.forms.FinancialCategoryEditorForm;
 import de.aidger.view.forms.HourlyWageEditorForm;
 import de.aidger.view.models.TableModel;
 import de.aidger.view.tabs.EditorTab;
 import de.aidger.view.tabs.ViewerTab;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 
 /**
  * This action saves the model and replaces the current tab with the model
  * viewer tab.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class EditorSaveAction extends AbstractAction {
 
     /**
      * Initializes the action.
      */
     public EditorSaveAction() {
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
         course.setScope(form.getScope());
         course.setGroup(form.getGroup());
         course.setRemark(form.getRemark());
 
         try {
             course
                 .setUnqualifiedWorkingHours(form.getUnqualifiedWorkingHours());
         } catch (NumberFormatException e) {
             course.addError("unqualifiedWorkingHours", new PresenceValidator(
                 course, new String[] {}).getMessage());
         }
 
         try {
             course.setPart(form.getPart());
         } catch (StringIndexOutOfBoundsException e) {
             course.addError("part", new PresenceValidator(course,
                 new String[] {}).getMessage());
         }
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
 
         try {
             fc.setBudgetCosts(form.getBudgetCosts());
         } catch (NumberFormatException e) {
             fc.addError("budgetCosts", new PresenceValidator(fc,
                 new String[] {}).getMessage());
         }
 
         try {
             fc.setFunds(form.getFunds());
         } catch (NumberFormatException e) {
             fc.addError("funds", new PresenceValidator(fc, new String[] {})
                 .getMessage());
         }
 
         try {
             fc.setYear(form.getYear());
         } catch (NumberFormatException e) {
             fc.addError("year", new PresenceValidator(fc, new String[] {})
                 .getMessage());
         }
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
 
         try {
             hw.setWage(new BigDecimal(form.getWage()));
         } catch (NumberFormatException e) {
             hw.addError("wage", new PresenceValidator(hw, new String[] {})
                 .getMessage());
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
         EditorTab tab = (EditorTab) UI.getInstance().getCurrentTab();
 
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
 
        List<TableModel> tableModels = ViewerTab.tableModels.get(tab
            .getType());
 
         for (TableModel tableModel : tableModels) {
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
 
         UI.getInstance().replaceCurrentTab(new ViewerTab(tab.getType()));
     }
 }
