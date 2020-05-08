 package com.jtbdevelopment.e_eye_o.ria.vaadin.components.workareas;
 
 import com.google.common.eventbus.Subscribe;
 import com.jtbdevelopment.e_eye_o.DAO.ReadOnlyDAO;
 import com.jtbdevelopment.e_eye_o.entities.*;
 import com.jtbdevelopment.e_eye_o.entities.events.AppUserOwnedObjectChanged;
 import com.jtbdevelopment.e_eye_o.reports.ReportBuilder;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.server.ConnectorResource;
 import com.vaadin.server.DownloadStream;
 import com.vaadin.shared.ui.datefield.Resolution;
 import com.vaadin.ui.*;
 import com.vaadin.ui.themes.Runo;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.ConfigurableBeanFactory;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.PostConstruct;
 import java.io.ByteArrayInputStream;
 import java.util.Set;
 
 /**
  * Date: 5/7/13
  * Time: 11:26 AM
  */
 @Component
 @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
 //  TODO - everything
 public class ReportsWorkArea extends CustomComponent {
     public static final String BY_STUDENT_BY_CATEGORY = "By Student, By Category";
     public static final String BY_CATEGORY_BY_STUDENT = "By Category, By Student";
     public static final String SUMMARY_REPORT = "Summary Report";
     public static final String STUDENT_DISPLAY_PROPERTY = "summaryDescription";
     public static final String CATEGORY_DISPLAY_PROPERTY = "description";
     public static final String CLASS_DISPLAY_PROPERTY = "description";
     private DateField fromField;
     private DateField toField;
     private ListSelect reportTypeField;
 
     @Autowired
     private ReportBuilder reportBuilder;
 
     @Autowired
     private ReadOnlyDAO readOnlyDAO;
 
     private AppUser appUser;
     private ListSelect classListField;
     private ListSelect categoryListField;
     private ListSelect studentListField;
 
     @Override
     public void attach() {
         appUser = getUI().getSession().getAttribute(AppUser.class);
         refreshLists();
         super.attach();
     }
 
     private void refreshLists() {
         BeanItemContainer<ObservationCategory> categories = new BeanItemContainer<>(ObservationCategory.class);
         categories.addAll(readOnlyDAO.getActiveEntitiesForUser(ObservationCategory.class, appUser));
         categories.sort(new String[]{CATEGORY_DISPLAY_PROPERTY}, new boolean[]{true});
         categoryListField.setContainerDataSource(categories);
         categoryListField.setRows(categories.size());
 
         BeanItemContainer<ClassList> classes = new BeanItemContainer<>(ClassList.class);
         classes.addAll(readOnlyDAO.getActiveEntitiesForUser(ClassList.class, appUser));
         classes.sort(new String[]{CLASS_DISPLAY_PROPERTY}, new boolean[]{true});
         classListField.setContainerDataSource(classes);
         classListField.setRows(classes.size());
 
         BeanItemContainer<Student> students = new BeanItemContainer<>(Student.class);
         students.addAll(readOnlyDAO.getActiveEntitiesForUser(Student.class, appUser));
         students.sort(new String[]{STUDENT_DISPLAY_PROPERTY}, new boolean[]{true});
         studentListField.setContainerDataSource(students);
         studentListField.setRows(students.size());
     }
 
     @PostConstruct
     public void postConstruct() {
         setSizeFull();
 
         VerticalLayout mainLayout = new VerticalLayout();
         mainLayout.setImmediate(true);
         mainLayout.setSpacing(true);
         mainLayout.setMargin(true);
         setCompositionRoot(mainLayout);
 
         Label instruction = new Label("All ACTIVE classes, students and categories will be included by default unless you limit to specific ones.");
         instruction.setSizeUndefined();
         mainLayout.addComponent(instruction);
         mainLayout.setComponentAlignment(instruction, Alignment.MIDDLE_CENTER);
         HorizontalLayout selectionRow = new HorizontalLayout();
         selectionRow.setSpacing(true);
         reportTypeField = new ListSelect("Report Type:");
         reportTypeField.addItem(BY_STUDENT_BY_CATEGORY);
         reportTypeField.addItem(BY_CATEGORY_BY_STUDENT);
         reportTypeField.addItem(SUMMARY_REPORT);
         reportTypeField.setMultiSelect(false);
         reportTypeField.setValue(BY_STUDENT_BY_CATEGORY);
         reportTypeField.setNullSelectionAllowed(false);
        reportTypeField.setRows(3);
         selectionRow.addComponent(reportTypeField);
 
         VerticalLayout dates = new VerticalLayout();
         dates.setSpacing(true);
         final LocalDate now = new LocalDate();
         final LocalDate lastAugust;
         if (now.getMonthOfYear() < 8) {
             lastAugust = new LocalDate(now.getYear() - 1, 8, 1);
         } else {
             lastAugust = new LocalDate(now.getYear(), 8, 1);
         }
         fromField = new DateField("From:");
         fromField.setResolution(Resolution.DAY);
         fromField.setValue(lastAugust.toDate());
         dates.addComponent(fromField);
 
         toField = new DateField("To:");
         toField.setResolution(Resolution.DAY);
         toField.setValue(now.toDate());
         dates.addComponent(toField);
         selectionRow.addComponent(dates);
 
         classListField = new ListSelect("Only Include Classes:");
         classListField.setMultiSelect(true);
         classListField.setItemCaptionPropertyId(CLASS_DISPLAY_PROPERTY);
         selectionRow.addComponent(classListField);
 
         categoryListField = new ListSelect("Only Include Categories:");
         categoryListField.setMultiSelect(true);
         categoryListField.setItemCaptionPropertyId(CATEGORY_DISPLAY_PROPERTY);
         selectionRow.addComponent(categoryListField);
 
         studentListField = new ListSelect("Only Include Students:");
         studentListField.setMultiSelect(true);
         studentListField.setItemCaptionPropertyId(STUDENT_DISPLAY_PROPERTY);
         selectionRow.addComponent(studentListField);
 
 
         VerticalLayout buttons = new VerticalLayout();
         buttons.setSpacing(true);
         Button generate = new Button("Generate");
         buttons.addComponent(generate);
 
         Button reset = new Button("Reset");
         buttons.addComponent(reset);
         selectionRow.addComponent(buttons);
         selectionRow.setComponentAlignment(buttons, Alignment.MIDDLE_LEFT);
 
         generate.addClickListener(new Button.ClickListener() {
             @Override
             public void buttonClick(Button.ClickEvent event) {
                 final byte[] pdf;
                 switch ((String) reportTypeField.getValue()) {
                     case BY_CATEGORY_BY_STUDENT:
                         pdf = reportBuilder.generateObservationReportByCategoryAndStudent(appUser,
                                 (Set<ClassList>) classListField.getValue(),
                                 (Set<Student>) studentListField.getValue(),
                                 (Set<ObservationCategory>) categoryListField.getValue(),
                                 new LocalDate(fromField.getValue()),
                                 new LocalDate(toField.getValue()));
                         break;
                     case BY_STUDENT_BY_CATEGORY:
                         pdf = reportBuilder.generateObservationReportByStudentAndCategory(appUser,
                                 (Set<ClassList>) classListField.getValue(),
                                 (Set<Student>) studentListField.getValue(),
                                 (Set<ObservationCategory>) categoryListField.getValue(),
                                 new LocalDate(fromField.getValue()),
                                 new LocalDate(toField.getValue()));
                         break;
                     case SUMMARY_REPORT:
                         pdf = reportBuilder.generateObservationStudentSummaryReport(appUser,
                                 (Set<ClassList>) classListField.getValue(),
                                 (Set<Student>) studentListField.getValue(),
                                 (Set<ObservationCategory>) categoryListField.getValue(),
                                 new LocalDate(fromField.getValue()),
                                 new LocalDate(toField.getValue()));
                         break;
                     default:
                         throw new RuntimeException("Unknown Report Type");
                 }
                 BrowserFrame report = new BrowserFrame(null, new ConnectorResource() {
                     @Override
                     public String getMIMEType() {
                         return "application/pdf";
                     }
 
                     @Override
                     public DownloadStream getStream() {
                         return new DownloadStream(new ByteArrayInputStream(pdf), getMIMEType(), getFilename());
                     }
 
                     @Override
                     public String getFilename() {
                         return "Report" + new LocalDateTime().toString("yyyyMMddHHmmss") + ".pdf";
                     }
                 });
 
                 Window window = new Window();
                 VerticalLayout mainLayout = new VerticalLayout();
                 window.setContent(mainLayout);
                 window.setResizable(true);
                 window.setSizeFull();
                 window.center();
                 mainLayout.setSizeFull();
                 report.setSizeFull();
                 mainLayout.addComponent(report);
                 window.addStyleName(Runo.WINDOW_DIALOG);
                 window.setModal(true);
                 window.setCaption("Report Contents");
                 getUI().addWindow(window);
             }
         });
 
         reset.addClickListener(new Button.ClickListener() {
             @Override
             public void buttonClick(Button.ClickEvent event) {
                 getUI().setFocusedComponent(reportTypeField);
                 classListField.setValue(null);
                 studentListField.setValue(null);
                 categoryListField.setValue(null);
                 fromField.setValue(lastAugust.toDate());
                 toField.setValue(now.toDate());
                 reportTypeField.setValue(BY_STUDENT_BY_CATEGORY);
             }
         });
         mainLayout.addComponent(selectionRow);
         mainLayout.setComponentAlignment(selectionRow, Alignment.TOP_CENTER);
     }
 
     @Subscribe
     @SuppressWarnings({"unused", "unchecked"})
     public void handleIdObjectChange(final AppUserOwnedObjectChanged msg) {
         if (!getSession().getAttribute(AppUser.class).equals(msg.getEntity().getAppUser())) {
             return;
         }
         if (Observation.class.isAssignableFrom(msg.getEntityType())) {
             return;
         }
         refreshLists();
     }
 }
