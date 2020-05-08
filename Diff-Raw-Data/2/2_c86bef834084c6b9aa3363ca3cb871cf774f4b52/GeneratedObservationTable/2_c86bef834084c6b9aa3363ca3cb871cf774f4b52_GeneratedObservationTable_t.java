 package com.jtbdevelopment.e_eye_o.ria.vaadin.components.filterabletables;
 
 import com.jtbdevelopment.e_eye_o.entities.*;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.components.editors.ObservationEditorDialogWindow;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.components.filterabletables.converters.LocalDateTimeDateConverter;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.components.filterabletables.converters.ObservationCategorySetStringConverter;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.components.filterabletables.converters.ShortenedCommentConverter;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.components.filterabletables.filters.ObservationCategoryFilter;
 import com.vaadin.data.Container;
 import com.vaadin.data.Property;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.data.util.filter.Between;
 import com.vaadin.data.util.filter.Or;
 import com.vaadin.data.util.filter.SimpleStringFilter;
 import com.vaadin.shared.ui.datefield.Resolution;
 import com.vaadin.ui.*;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 import org.joda.time.LocalTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  * Date: 6/5/13
  * Time: 10:22 PM
  */
 //  TODO - do we need a bulk archive/unarchive feature?
 public class GeneratedObservationTable extends GeneratedIdObjectTable<Observation> {
     private static final Logger logger = LoggerFactory.getLogger(ObservationWithoutSubjectTable.class);
     public static final String SIGNIFICANTONLY_DEFAULT = ".significantonly.default";
     public static final String MONTHSBACK_DEFAULT = ".monthsback.default";
     public static final boolean DEFAULT_SIGNIFICANT_ONLY = false;
     public static final int DEFAULT_FROM_MONTHS_BACK = 1;
     private Observable defaultObservationSubject;
     @Autowired
     private ObservationEditorDialogWindow observationEditorDialogWindow;
     @Autowired
     private ObservationCategorySetStringConverter observationCategorySetStringConverter;
     @Autowired
     private ShortenedCommentConverter shortenedCommentConverter;
     @Autowired
     private LocalDateTimeDateConverter localDateTimeDateConverter;
 
     private BeanItemContainer<Semester> semesters;
     private CheckBox significantOnly = new CheckBox("Significant Only");
     private DateField from = new DateField();
     private DateField to = new DateField();
     private ComboBox semesterList = new ComboBox();
     private Between dateRangeFilter;
 
     public GeneratedObservationTable() {
         super(Observation.class);
     }
 
     @Override
     public ObservationEditorDialogWindow showEntityEditor(final Observation entity) {
         if (entity.getObservationSubject() == null) {
             entity.setObservationSubject(defaultObservationSubject);
         }
         getUI().addWindow(observationEditorDialogWindow);
         observationEditorDialogWindow.setEntity(entity);
         return observationEditorDialogWindow;
     }
 
     @Override
     protected Container.Filter generateFilter(final String searchFor) {
         return new Or(
                 new SimpleStringFilter("comment", searchFor, true, false),
                 new ObservationCategoryFilter(searchFor)
         );
     }
 
     @Override
     protected boolean getDefaultSortAscending() {
         return false;
     }
 
     @Override
     protected void addColumnConverters() {
         super.addColumnConverters();
         entityTable.setConverter("categories", observationCategorySetStringConverter);
         entities.addAdditionalSortableProperty("categories");
         entityTable.setConverter("comment", shortenedCommentConverter);
         entityTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
             @Override
             public String generateDescription(final Component source, final Object itemId, final Object propertyId) {
                 if (itemId != null && propertyId != null && propertyId.equals("comment")) {
                     final Observation entity = entities.getItem(itemId).getBean();
                     return entity.getComment().replace("\n", "<br/>");
                 }
                 return null;
             }
         });
     }
 
     @Override
     protected void addGeneratedColumns() {
         super.addGeneratedColumns();
 
         //  TODO - do this better
         entityTable.setColumnExpandRatio("observationTimestamp", 0.15f);
         entityTable.setColumnExpandRatio("categories", 0.10f);
         entityTable.setColumnExpandRatio("modificationTimestamp", 0.15f);
         entityTable.setColumnExpandRatio("comment", 0.50f);
         entityTable.setColumnExpandRatio("archived", 0.05f);
         entityTable.setColumnExpandRatio("significant", 0.05f);
     }
 
     @Override
     protected void addCustomFilters(final HorizontalLayout filterSection) {
         super.addCustomFilters(filterSection);
         Label label = new Label("Semester");
         filterSection.addComponent(label);
         filterSection.addComponent(semesterList);
         semesterList.setItemCaptionPropertyId("description");
         semesterList.addStyleName("right-align");
 
 
         label = new Label("From");
         filterSection.addComponent(label);
         from.setResolution(Resolution.DAY);
         from.setConverter(localDateTimeDateConverter);
         filterSection.addComponent(from);
         from.addValueChangeListener(new Property.ValueChangeListener() {
             @Override
             public void valueChange(Property.ValueChangeEvent event) {
                 logger.trace(getSession().getAttribute(AppUser.class).getId() + ": from filter " + from.getValue());
                 updateDateRangeFilter();
             }
         });
         label = new Label("To");
         filterSection.addComponent(label);
         to.setResolution(Resolution.DAY);
         to.setConverter(localDateTimeDateConverter);
         to.addValueChangeListener(new Property.ValueChangeListener() {
             @Override
             public void valueChange(Property.ValueChangeEvent event) {
                 logger.trace(getSession().getAttribute(AppUser.class).getId() + ": to filter " + to.getValue());
                 updateDateRangeFilter();
             }
         });
         filterSection.addComponent(to);
 
         significantOnly.addValueChangeListener(new Property.ValueChangeListener() {
             @Override
             public void valueChange(Property.ValueChangeEvent event) {
                 logger.trace(getSession().getAttribute(AppUser.class).getId() + ": significant only filter " + significantOnly.getValue());
                 if (significantOnly.getValue()) {
                     entities.addContainerFilter("significant", "true", false, true);
                 } else {
                     entities.removeContainerFilters("significant");
                 }
                 refreshSizeAndSort();
             }
         });
         filterSection.addComponent(significantOnly);
 
         semesterList.addValueChangeListener(new Property.ValueChangeListener() {
             @Override
             public void valueChange(Property.ValueChangeEvent event) {
                 setDateFiltersFromSemester((Semester) semesterList.getValue());
             }
         });
     }
 
     private void setDateFiltersFromSemester(final Semester semester) {
         from.setConvertedValue(semester.getStart().toLocalDateTime(LocalTime.MIDNIGHT));
         to.setConvertedValue(semester.getEnd().toLocalDateTime(LocalTime.MIDNIGHT));
     }
 
     private void updateDateRangeFilter() {
         if (dateRangeFilter != null) {
             entities.removeContainerFilter(dateRangeFilter);
         }
         dateRangeFilter = new Between("observationTimestamp",
                 new LocalDateTime(from.getConvertedValue()),
                new LocalDate(to.getConvertedValue()).plusDays(1).toLocalDateTime(LocalTime.MIDNIGHT).minusSeconds(1));
         entities.addContainerFilter(dateRangeFilter);
         refreshSizeAndSort();
     }
 
     @Override
     public void setDisplayDriver(final IdObject tableDriver) {
         super.setDisplayDriver(tableDriver);
         if (tableDriver instanceof AppUserOwnedObject) {
             if (tableDriver instanceof ObservationCategory) {
                 entities.addAll(readWriteDAO.getAllObservationsForObservationCategory((ObservationCategory) tableDriver));
             } else if (tableDriver instanceof Observable) {
                 entities.addAll(readWriteDAO.getAllObservationsForEntity((Observable) tableDriver));
                 setDefaultObservationSubject((Observable) tableDriver);
             } else if (tableDriver instanceof Semester) {
                 Semester semester = (Semester) tableDriver;
                 entities.addAll(readWriteDAO.getAllObservationsForSemester(semester, 0, 0));
                 setDateFiltersFromSemester(semester);
                 if (semester.isArchived()) {
                     semesters.removeAllItems();
                     semesters.addAll(readWriteDAO.getEntitiesForUser(Semester.class, appUser, 0, 0));
                 } else {
                     semesters.removeAllItems();
                     semesters.addAll(readWriteDAO.getActiveEntitiesForUser(Semester.class, appUser, 0, 0));
                 }
                 semesterList.setValue(semester);
                 setDefaultObservationSubject(null);
             }
             refreshSizeAndSort();
         }
     }
 
     @Override
     protected void initializeFilters() {
         super.initializeFilters();
         AppUserSettings settings = getSession().getAttribute(AppUserSettings.class);
         significantOnly.setValue(settings.getSettingAsBoolean(baseConfigSetting + SIGNIFICANTONLY_DEFAULT, DEFAULT_SIGNIFICANT_ONLY));
         from.setConvertedValue(new LocalDateTime().minusMonths(settings.getSettingAsInt(baseConfigSetting + MONTHSBACK_DEFAULT, DEFAULT_FROM_MONTHS_BACK)));
         to.setConvertedValue(new LocalDateTime());
         semesters = new BeanItemContainer<>(Semester.class, readWriteDAO.getActiveEntitiesForUser(Semester.class, appUser, 0, 0));
         semesters.sort(new String[]{"description"}, new boolean[]{true});
         semesterList.setContainerDataSource(semesters);
     }
 
     public void setDefaultObservationSubject(final Observable defaultObservationSubject) {
         this.defaultObservationSubject = defaultObservationSubject;
     }
 }
