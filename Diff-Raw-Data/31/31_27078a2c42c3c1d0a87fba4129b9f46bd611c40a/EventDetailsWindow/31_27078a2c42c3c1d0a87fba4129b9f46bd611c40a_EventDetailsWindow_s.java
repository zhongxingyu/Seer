 package cz.admin24.myachievo.web2.calendar.detail;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.tuple.Pair;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.Validator;
 import com.vaadin.data.util.IndexedContainer;
 import com.vaadin.server.Page;
 import com.vaadin.shared.ui.combobox.FilteringMode;
 import com.vaadin.ui.AbstractSelect.NewItemHandler;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.DateField;
 import com.vaadin.ui.Form;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 import cz.admin24.myachievo.connector.http.dto.PhaseActivity;
 import cz.admin24.myachievo.connector.http.dto.Project;
 import cz.admin24.myachievo.connector.http.dto.ProjectPhase;
 import cz.admin24.myachievo.connector.http.dto.WorkReport;
 import cz.admin24.myachievo.web2.SpringUtils;
 import cz.admin24.myachievo.web2.calendar.CalendarUrl;
 import cz.admin24.myachievo.web2.service.AchievoConnectorWrapper;
 import cz.admin24.myachievo.web2.service.ProjectsCache;
 import cz.admin24.myachievo.web2.service.WorkReportCache;
 import cz.admin24.myachievo.web2.utils.TimesheetUtils;
 
 public abstract class EventDetailsWindow extends Window {
     private static final long             serialVersionUID    = 1L;
     private static final Object           PROPERTY_ID         = "ID";
     private final VerticalLayout          content             = new VerticalLayout();
     // private final EventDetailFieldGroup form = new EventDetailFieldGroup();
     @SuppressWarnings("deprecation")
     private final Form                    form                = new Form();
     private final ComboBox                projectCombo        = new LikeComboBox("Project");
     private final ComboBox                phaseCombo          = new LikeComboBox("Phase");
     private final ComboBox                activityCombo       = new LikeComboBox("Activity");
     private final IndexedContainer        remarkAutoContainer = new IndexedContainer();
     private final ComboBox                remarkAutoComplete  = new LikeComboBox("Remark", remarkAutoContainer);
     private final DateField               dayDateField        = new DateField("Date");
     private final ComboBox                hoursCombo          = new LikeComboBox("Hours");
     private final ComboBox                minutesCombo        = new LikeComboBox("Minutes");
     //
     private final HorizontalLayout        buttons             = new HorizontalLayout();
     private final Button                  saveBtn             = new Button("Save");
     private final Button                  saveAndRepeatBtn    = new Button("Save & Repeat");
     private final Button                  deleteBtn           = new Button("Delete");
 
     private final ProjectsCache           projectsCache       = SpringUtils.getBean(ProjectsCache.class);
     private final WorkReportCache         workReportCache     = SpringUtils.getBean(WorkReportCache.class);
     private final AchievoConnectorWrapper achievoConnector    = SpringUtils.getBean(AchievoConnectorWrapper.class);
     private final WorkReport              workReport;
 
 
     public EventDetailsWindow(WorkReport workReport) {
         super("Event detail");
         this.workReport = workReport;
         setContent(content);
         buildLayout();
         buildForm();
         configureForm();
         refreshForm(workReport);
         css();
 

     }
 
 
     public void commit() {
         form.commit();
         Date day = dayDateField.getValue();
         Integer hours = (Integer) hoursCombo.getValue();
         Integer minutes = (Integer) minutesCombo.getValue();
         String projectId = getSelectedId(projectCombo);
         String phaseId = getSelectedId(phaseCombo);
         String activityId = getSelectedId(activityCombo);
         String remark = (String) remarkAutoComplete.getValue();
         String workReportId = workReport.getId();
         if (workReportId == null) {
             achievoConnector.registerHours(day, hours, minutes, projectId, phaseId, activityId, remark);
         } else {
             achievoConnector.updateRegiteredHours(workReportId, day, hours, minutes, projectId, phaseId, activityId, remark);
         }
         onEventChanged();
     }
 
 
     protected abstract void onEventChanged();
 
 
     private void buildLayout() {
         content.addComponent(form);
         content.addComponent(buttons);
         buttons.addComponent(deleteBtn);
         buttons.addComponent(saveAndRepeatBtn);
         buttons.addComponent(saveBtn);
 
     }
 
 
     private void css() {
         center();
         content.setMargin(true);
         buttons.setSpacing(true);
 
         setWidth("500px");
         projectCombo.setWidth("100%");
         phaseCombo.setWidth("100%");
         activityCombo.setWidth("100%");
         remarkAutoComplete.setWidth("100%");
         dayDateField.setWidth("250px");
         hoursCombo.setWidth("50px");
         minutesCombo.setWidth("50px");
 
         buttons.setWidth("100%");
 
         buttons.setComponentAlignment(saveBtn, Alignment.MIDDLE_RIGHT);
         buttons.setComponentAlignment(saveAndRepeatBtn, Alignment.MIDDLE_RIGHT);
         buttons.setExpandRatio(deleteBtn, 1);
     }
 
 
     private void refreshForm(WorkReport r) {
         String project = r.getProject();
         if (project != null) {
             projectCombo.setValue(project);
         }
         String phase = r.getPhase();
         if (phase != null) {
             phaseCombo.setValue(phase);
         }
         String activity = r.getActivity();
         if (activity != null) {
             activityCombo.setValue(activity);
         }
         String remark = r.getRemark();
         if (remark != null) {
             remarkAutoComplete.addItem(remark);
             remarkAutoComplete.setValue(remark);
         }
         Date date = r.getDate();
         if (date != null) {
             dayDateField.setValue(date);
         }
         Integer hours = r.getHours();
         if (hours != null) {
             hoursCombo.setValue(hours);
         }
         Integer minutes = r.getMinutes();
         if (minutes != null) {
             minutesCombo.setValue(minutes);
         }
 
         deleteBtn.setEnabled(r.getId() != null);
     }
 
 
     private void configureForm() {
         // projectCombo.setInvalidAllowed(true);
         // projectCombo.setNewItemsAllowed(true);
 
         form.setValidationVisible(true);
         //
         projectCombo.setImmediate(true);
         phaseCombo.setImmediate(true);
         activityCombo.setImmediate(true);
         remarkAutoComplete.setImmediate(true);
         dayDateField.setImmediate(true);
         hoursCombo.setImmediate(true);
         minutesCombo.setImmediate(true);
         //
         projectCombo.setRequired(true);
         phaseCombo.setRequired(true);
         activityCombo.setRequired(true);
         remarkAutoComplete.setRequired(true);
         dayDateField.setRequired(true);
         hoursCombo.setRequired(true);
         minutesCombo.setRequired(true);
         //
         projectCombo.setNullSelectionAllowed(false);
         phaseCombo.setNullSelectionAllowed(false);
         activityCombo.setNullSelectionAllowed(false);
         remarkAutoComplete.setNullSelectionAllowed(false);
         // dayDateField.setNullSelectionAllowed(false);
         hoursCombo.setNullSelectionAllowed(false);
         minutesCombo.setNullSelectionAllowed(false);
 
         projectCombo.addContainerProperty(PROPERTY_ID, String.class, null);
         phaseCombo.addContainerProperty(PROPERTY_ID, String.class, null);
         activityCombo.addContainerProperty(PROPERTY_ID, String.class, null);
 
         projectCombo.addValueChangeListener(new ValueChangeListener() {
 
             @Override
             public void valueChange(ValueChangeEvent event) {
                 configurePhaseCombo();
             }
         });
         phaseCombo.addValueChangeListener(new ValueChangeListener() {
 
             @Override
             public void valueChange(ValueChangeEvent event) {
                 configureActivityCombo();
             }
 
         });
 
         projectCombo.removeAllItems();
         List<Project> projects = projectsCache.getProjects();
         for (Project p : projects) {
             Item item = projectCombo.addItem(p.getName());
             item.getItemProperty(PROPERTY_ID).setValue(p.getId());
         }
         if (!projects.isEmpty()) {
             projectCombo.setValue(projects.get(0).getName());
         }
 
         remarkAutoComplete.setNewItemsAllowed(true);
         remarkAutoComplete.removeAllItems();
         List<String> remarks = workReportCache.getRemarks();
         for (String remark : remarks) {
             remarkAutoComplete.addItem(remark);
         }
         if (!remarks.isEmpty()) {
             remarkAutoComplete.setValue(remarks.get(0));
         }
 
         remarkAutoComplete.setNewItemHandler(new NewItemHandler() {
 
             @Override
             public void addNewItem(String newItemCaption) {
                 if (StringUtils.isBlank(newItemCaption)) {
                     return;
                 }
                 remarkAutoContainer.addItemAt(0, newItemCaption);
                 remarkAutoComplete.setValue(newItemCaption);
             }
         });
         hoursCombo.setContainerDataSource(new HoursContainer());
         minutesCombo.setContainerDataSource(new MinutesContainer());
 
         Validator durationValidator = new Validator() {
 
             @Override
             public void validate(Object value) throws InvalidValueException {
                 if (getHours() + getMinutes() <= 0) {
                     throw new InvalidValueException("Cannot register work with duration 0 minutes");
                 }
             }
 
         };
         hoursCombo.addValidator(durationValidator);
         minutesCombo.addValidator(durationValidator);
 
         dayDateField.setDateFormat("EEEE, d. MMMM yyyy");
 
     }
 
 
     private void configurePhaseCombo() {
         phaseCombo.removeAllItems();
         String projectId = getSelectedId(projectCombo);
         if (projectId == null) {
             return;
         }
 
         List<ProjectPhase> phases = projectsCache.getPhases(projectId, (String) projectCombo.getValue());
         for (ProjectPhase p : phases) {
             Item item = phaseCombo.addItem(p.getName());
             item.getItemProperty(PROPERTY_ID).setValue(p.getId());
         }
 
         if (!phases.isEmpty()) {
             phaseCombo.setValue(phases.get(0).getName());
         }
     }
 
 
     private void configureActivityCombo() {
         activityCombo.removeAllItems();
         String projectId = getSelectedId(projectCombo);
         String phaseId = getSelectedId(phaseCombo);
         if (projectId == null || phaseId == null) {
             return;
         }
 
         List<PhaseActivity> activities = projectsCache.getActivities(projectId, phaseId, (String) projectCombo.getValue(), (String) phaseCombo.getValue());
         for (PhaseActivity a : activities) {
             Item item = activityCombo.addItem(a.getName());
             item.getItemProperty(PROPERTY_ID).setValue(a.getId());
         }
 
         if (!activities.isEmpty()) {
             activityCombo.setValue(activities.get(0).getName());
         }
     }
 
 
     private String getSelectedId(ComboBox cb) {
         Object value = cb.getValue();
         if (value == null) {
             return null;
         }
         return (String) cb.getItem(value).getItemProperty(PROPERTY_ID).getValue();
     }
 
 
     private void buildForm() {
 
         form.addField(projectCombo, projectCombo);
         form.addField(phaseCombo, phaseCombo);
         form.addField(activityCombo, activityCombo);
         form.addField(remarkAutoComplete, remarkAutoComplete);
         form.addField(dayDateField, dayDateField);
         form.addField(hoursCombo, hoursCombo);
         form.addField(minutesCombo, minutesCombo);
 
         saveBtn.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 if (form.isValid()) {
                     commit();
                     CalendarUrl url = new CalendarUrl(Page.getCurrent());
                     url.setDate(dayDateField.getValue());
                     UI.getCurrent().getNavigator().navigateTo(url.toFragment());
                     close();
                 }
             }
         });
         saveAndRepeatBtn.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 if (form.isValid()) {
                     commit();
                     Date day = dayDateField.getValue();
                     List<WorkReport> reportedHours = achievoConnector.getHours(day, day);
                     Pair<Integer, Integer> hoursMinutes = TimesheetUtils.countRemainingTime(reportedHours);
 
                     if (hoursMinutes.getKey() <= 0 && hoursMinutes.getValue() <= 0) {
                         // go to the next day
                         Calendar c = Calendar.getInstance();
                         c.setTime(dayDateField.getValue());
 
                         int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                         switch (dayOfWeek) {
                         case Calendar.FRIDAY:
                             c.add(Calendar.DAY_OF_YEAR, 3);
                             break;
                         case Calendar.SATURDAY:
                             c.add(Calendar.DAY_OF_YEAR, 2);
                             break;
                         default:
                             c.add(Calendar.DAY_OF_YEAR, 1);
                             break;
                         }
                         dayDateField.setValue(c.getTime());
                         Pair<Integer, Integer> newHoursMinutes = TimesheetUtils.countRemainingTime(hoursMinutes);
                         hoursCombo.setValue(newHoursMinutes.getKey());
                         minutesCombo.setValue(newHoursMinutes.getValue());
                         CalendarUrl url = new CalendarUrl(Page.getCurrent());
                         url.setDate(c.getTime());
                         UI.getCurrent().getNavigator().navigateTo(url.toFragment());
                     } else {
                         hoursCombo.setValue(hoursMinutes.getKey());
                         minutesCombo.setValue(hoursMinutes.getValue());
                     }
 
                 }
             }
         });
 
         deleteBtn.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 achievoConnector.deleteRegisteredHour(workReport.getId());
                 onEventChanged();
                 close();
             }
         });
     }
 
 
     private Integer getHours() {
         return (Integer) hoursCombo.getValue();
     }
 
 
     private Integer getMinutes() {
         return (Integer) minutesCombo.getValue();
     }
 }
