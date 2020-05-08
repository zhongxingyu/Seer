 package de.aidger.controller.actions;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.event.ActionEvent;
 import java.math.BigDecimal;
 import java.sql.SQLIntegrityConstraintViolationException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 
 import de.aidger.model.AbstractModel;
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Course;
 import de.aidger.model.models.Employment;
 import de.aidger.model.models.FinancialCategory;
 import de.aidger.model.models.HourlyWage;
 import de.aidger.model.validators.PresenceValidator;
 import de.aidger.view.UI;
 import de.aidger.view.forms.AssistantEditorForm;
 import de.aidger.view.forms.CourseEditorForm;
 import de.aidger.view.forms.EmploymentEditorForm;
 import de.aidger.view.forms.FinancialCategoryEditorForm;
 import de.aidger.view.forms.HourlyWageEditorForm;
 import de.aidger.view.models.TableModel;
 import de.aidger.view.tabs.EditorTab;
 import de.aidger.view.tabs.Tab;
 import de.aidger.view.tabs.ViewerTab;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IAssistant;
 import de.unistuttgart.iste.se.adohive.model.ICourse;
 import de.unistuttgart.iste.se.adohive.model.IEmployment;
 import de.unistuttgart.iste.se.adohive.model.IFinancialCategory;
 import de.unistuttgart.iste.se.adohive.model.IHourlyWage;
 
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
      * Prepares the course model stored in the models list by setting the values
      * of the course editor form to this model. Returns the model from the
      * database before it is edited. If a database error occurs the model before
      * it was edited is returned.
      * 
      * @param models
      *            a list that contains the course model of the editor
      * @param form
      *            the course editor form
      * @return the model from database before it is edited
      */
     @SuppressWarnings("unchecked")
     private AbstractModel prepareModels(List<AbstractModel> models,
             CourseEditorForm form) {
         Course course = (Course) models.get(0);
 
         Course courseBeforeEdit = course.clone();
 
         course.setDescription(form.getDescription());
         course.setSemester(form.getSemester());
         course.setLecturer(form.getLecturer());
         course.setAdvisor(form.getAdvisor());
         course.setNumberOfGroups(form.getNumberOfGroups());
         course.setTargetAudience(form.getTargetAudience());
         course.setScope(form.getScope());
         course.setGroup(form.getGroup());
         course.setRemark(form.getRemark());
         course.setFinancialCategoryId(form.getFinancialCategoryId());
 
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
 
         try {
             ICourse c = course.getById(course.getId());
 
             return c == null ? courseBeforeEdit : new Course(c);
         } catch (AdoHiveException e) {
             return courseBeforeEdit;
         }
     }
 
     /**
      * Prepares the assistant model stored in the models list by setting the
      * values of the assistant editor form to this model. Returns the model from
      * the database before it is edited. If a database error occurs the model
      * before it was edited is returned.
      * 
      * @param models
      *            a list that contains the assistant model of the editor
      * @param form
      *            the assistant editor form
      * @return the model from database before it is edited
      */
     @SuppressWarnings("unchecked")
     private AbstractModel prepareModels(List<AbstractModel> models,
             AssistantEditorForm form) {
         Assistant assistant = (Assistant) models.get(0);
 
         Assistant assistantBeforeEdit = assistant.clone();
 
         assistant.setFirstName(form.getFirstName());
         assistant.setLastName(form.getLastName());
         assistant.setEmail(form.getEmail());
         assistant.setQualification(form.getQualification());
 
         try {
             IAssistant a = assistant.getById(assistant.getId());
 
             return a == null ? assistantBeforeEdit : new Assistant(a);
         } catch (AdoHiveException e) {
             return assistantBeforeEdit;
         }
     }
 
     /**
      * Prepares the financial category model stored in the models list by
      * setting the values of the financial category editor form to this model.
      * Returns the model from the database before it is edited. If a database
      * error occurs the model before it was edited is returned.
      * 
      * @param models
      *            a list that contains the financial category model of the
      *            editor
      * @param form
      *            the financial category editor form
      * @return the model from database before it is edited
      */
     @SuppressWarnings("unchecked")
     private AbstractModel prepareModels(List<AbstractModel> models,
             FinancialCategoryEditorForm form) {
         FinancialCategory fc = (FinancialCategory) models.get(0);
 
         FinancialCategory fcBeforeEdit = fc.clone();
 
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
 
         try {
            // fc.getByKeys expects only the ID as key
            IFinancialCategory f = fc.getByKeys(fc.getId());
 
             return f == null ? fcBeforeEdit : new FinancialCategory(f);
         } catch (AdoHiveException e) {
             return fcBeforeEdit;
         }
     }
 
     /**
      * Prepares the hourly wage models by setting the values of the hourly wage
      * editor form to the models. Returns the model from the database before it
      * is edited. If a database error occurs the model before it was edited is
      * returned.
      * 
      * @param models
      *            a list that contains the hourly wage model of the editor
      * @param form
      *            the hourly wage editor form
      * @return the model from database before it is edited
      */
     @SuppressWarnings("unchecked")
     private AbstractModel prepareModels(List<AbstractModel> models,
             HourlyWageEditorForm form) {
         HourlyWage hw = (HourlyWage) models.get(0);
 
         HourlyWage hwBeforeEdit = hw.clone();
 
         hw.setQualification(form.getQualification());
 
         try {
             hw.setWage(new BigDecimal(form.getWage()));
         } catch (NumberFormatException e) {
             hw.addError("wage", new PresenceValidator(hw, new String[] {})
                 .getMessage());
         }
 
         if (form.isEditMode()) {
             hw.setMonth(form.getMonth());
             hw.setYear(form.getYear());
         } else {
             Calendar start = Calendar.getInstance();
             start.setTime(form.getStartDate());
 
             Calendar finish = Calendar.getInstance();
             finish.setTime(form.getFinishDate());
 
             if (start.after(finish)) {
                 hw.addError("end date", _("must be after start date"));
             } else {
                 models.clear();
 
                 finish.add(Calendar.MONTH, 1);
 
                 while (finish.after(start)) {
                     HourlyWage clone = hw.clone();
                     models.add(clone);
 
                     clone.setMonth((byte) (start.get(Calendar.MONTH) + 1));
                     clone.setYear((short) start.get(Calendar.YEAR));
 
                     start.add(Calendar.MONTH, 1);
                 }
             }
         }
 
         try {
             IHourlyWage h = hwBeforeEdit.getByKeys(hwBeforeEdit
                 .getQualification(), hwBeforeEdit.getMonth(), hwBeforeEdit
                 .getYear());
 
             return h == null ? hwBeforeEdit : new HourlyWage(h);
         } catch (AdoHiveException e) {
             return hwBeforeEdit;
         }
     }
 
     /**
      * Prepares the employment models by setting the values of the employment
      * editor form to this model. Returns the model from the database before it
      * is edited. If a database error occurs the model before it was edited is
      * returned.
      * 
      * @param models
      *            a list that contains the employment model of the editor
      * @param form
      *            the employment editor form
      * @return the model from database before it is edited
      */
     @SuppressWarnings("unchecked")
     private AbstractModel prepareModels(List<AbstractModel> models,
             EmploymentEditorForm form) {
         Employment employment = (Employment) models.get(0);
 
         Employment employmentBeforeEdit = employment.clone();
 
         employment.setAssistantId(form.getAssistantId());
         employment.setCourseId(form.getCourseId());
         employment.setContractId(form.getContractId());
         employment.setCostUnit(form.getCostUnit());
         employment.setQualification(form.getQualification());
         employment.setRemark(form.getRemark());
 
         try {
             employment.setFunds(form.getFunds());
         } catch (NumberFormatException e) {
             employment.addError("funds", new PresenceValidator(employment,
                 new String[] {}).getMessage());
         }
 
         List<Date> dates = form.getDates();
         List<Double> hcs = new Vector<Double>();
 
         try {
             hcs = form.getHourCounts();
 
             models.clear();
 
             for (int i = 0; i < dates.size(); ++i) {
                 Employment clone = employment.clone();
                 models.add(clone);
 
                 Calendar cal = Calendar.getInstance();
                 cal.setTime(dates.get(i));
 
                 clone.setMonth((byte) (cal.get(Calendar.MONTH) + 1));
                 clone.setYear((short) cal.get(Calendar.YEAR));
                 clone.setHourCount(hcs.get(i));
             }
         } catch (NumberFormatException e) {
             employment.addError("hourCount", new PresenceValidator(employment,
                 new String[] {}).getMessage());
         }
 
         // TODO handling edit mode
 
         try {
             IEmployment e = employment.getById(employment.getId());
 
             return e == null ? employmentBeforeEdit : new Employment(e);
         } catch (AdoHiveException e) {
             return employmentBeforeEdit;
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
 
         ViewerTab next = (ViewerTab) tab.getPredecessorOf(ViewerTab.class);
 
         if (next == null) {
             next = new ViewerTab(tab.getType());
         }
 
         TableModel tableModel = next.getTableModel();
 
         // if something went wrong just the clone model is affected
         AbstractModel clone = (AbstractModel) tab.getModel().clone();
         clone.setNew(!tab.isEditMode());
 
         List<AbstractModel> models = new Vector<AbstractModel>();
         models.add(clone);
 
         AbstractModel modelBeforeEdit = tab.getModel();
 
         // preparation of the models (type specific)
         switch (tab.getType()) {
         case Course:
             modelBeforeEdit = prepareModels(models, (CourseEditorForm) tab
                 .getEditorForm());
             break;
         case Assistant:
             modelBeforeEdit = prepareModels(models, (AssistantEditorForm) tab
                 .getEditorForm());
             break;
         case FinancialCategory:
             modelBeforeEdit = prepareModels(models,
                 (FinancialCategoryEditorForm) tab.getEditorForm());
             break;
         case HourlyWage:
             modelBeforeEdit = prepareModels(models, (HourlyWageEditorForm) tab
                 .getEditorForm());
             break;
         case Employment:
             modelBeforeEdit = prepareModels(models, (EmploymentEditorForm) tab
                 .getEditorForm());
             break;
         }
 
         // save all prepared models
         for (AbstractModel model : models) {
 
             // table model needs the model before it was edited 
             tableModel.setModelBeforeEdit(modelBeforeEdit);
 
             // the model is observed by the table model
             model.addObserver(tableModel);
 
             /*
              * model should also be observed by the already existing table
              * models of the same type
              */
             for (Tab t : UI.getInstance().getTabs()) {
                 if (t instanceof ViewerTab
                         && ((ViewerTab) t).getType() == tab.getType()) {
                     TableModel tM = ((ViewerTab) t).getTableModel();
 
                     tM.setModelBeforeEdit(modelBeforeEdit);
 
                     model.addObserver(tM);
                 }
             }
 
             try {
 
                 // finally try to save the model to database
                 if (!model.save()) {
                     // validation has failed
                     tab.updateHints(model);
 
                     return;
                 }
             } catch (AdoHiveException e1) {
                 if (e1.getCause().getClass() == SQLIntegrityConstraintViolationException.class) {
                     UI
                         .displayError(_("Could not save the model because it already exists in the database."));
                 } else {
                     UI.displayError(_("Could not save the model to database."));
                 }
 
                 break;
             }
         }
 
         UI.getInstance().replaceCurrentTab(next);
     }
 }
