 package com.bulbview.recipeplanner.ui.presenter;
 
 import java.text.DateFormat;
 import java.util.Collection;
 import java.util.Date;
 
 import org.springframework.beans.factory.ObjectFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.bulbview.recipeplanner.datamodel.schedule.DateSection;
 import com.bulbview.recipeplanner.datamodel.schedule.NameSection;
 import com.bulbview.recipeplanner.datamodel.schedule.Section;
 import com.bulbview.recipeplanner.persistence.DaoException;
 import com.bulbview.recipeplanner.persistence.ScheduleObjectifyDao;
 import com.bulbview.recipeplanner.ui.DailyScheduleView;
 import com.bulbview.recipeplanner.ui.manager.MainWindowUiManager;
 import com.bulbview.recipeplanner.ui.manager.ScheduleHistoryList;
 import com.bulbview.recipeplanner.ui.manager.WeeklyScheduleView;
 import com.google.appengine.repackaged.com.google.common.collect.Sets;
 
 @Component
 public class WeeklySchedulePresenter extends Presenter implements SessionPresenter {
 
     private static final int                    DAY_IN_MILLIS = 1 * 24 * 60 * 60 * 1000;
     private ObjectFactory<DailyScheduleView>    dailyScheduleViewFactory;
 
     private final Collection<DailyScheduleView> dailyScheduleViews;
     @Autowired
     private DateFormat                          dateFormatter;
     @Autowired
     private ObjectFactory<DateSection>          dateSectionFactory;
     @Autowired
     private MainWindowUiManager                 mainWindow;
     private ObjectFactory<NameSection>          nameSectionFactory;
     @Autowired
     private ScheduleObjectifyDao                scheduleDao;
     @Autowired
     private ScheduleHistoryList                 scheduleHistoryList;
     private WeeklyScheduleModel                 weeklyScheduleModel;
     @Autowired
     private WeeklyScheduleView                  weeklyScheduleView;
 
     public WeeklySchedulePresenter() {
         this.dailyScheduleViews = Sets.newHashSet();
     }
 
     public void clearSchedule() {
         for ( final DailyScheduleView dailyScheduleView : dailyScheduleViews ) {
             dailyScheduleView.clear();
         }
     }
 
     @Override
     public void init() {
         createNewSchedule();
         initViews();
        createMiscTab();
     }
 
     public void saveSchedule() {
         try {
             scheduleDao.save(weeklyScheduleModel.getSchedule());
         } catch (final DaoException e) {
             throw new WeeklySchedulePresenterException("Error saving schedule", e);
         }
     }
 
     @Autowired
     public void setDailyScheduleListFactory(final ObjectFactory<DailyScheduleView> dayScheduleListFactory) {
         this.dailyScheduleViewFactory = dayScheduleListFactory;
     }
 
     public void setDayFactory(final ObjectFactory<DateSection> dayFactory) {
         this.dateSectionFactory = dayFactory;
     }
 
     @Autowired
     public void setModel(final WeeklyScheduleModel model) {
         this.weeklyScheduleModel = model;
     }
 
     @Autowired
     public void setNameSectionFactory(final ObjectFactory<NameSection> nameSectionFactory) {
         this.nameSectionFactory = nameSectionFactory;
     }
 
     public void setStartDate(final Date startDate) {
         weeklyScheduleModel.createSchedule();
         weeklyScheduleModel.setStartDate(startDate);
         weeklyScheduleView.clearSectionsFromSchedule();
         createViewDailyTabs(startDate);
     }
 
     @Autowired
     public void setView(final WeeklyScheduleView weeklyScheduleView) {
         this.weeklyScheduleView = weeklyScheduleView;
     }
 
     public void showHistory() {
         mainWindow.showScheduleHistoryWindow();
     }
 
     private void addDailyTab(final Date incrementedDate) {
        final String formattedDate = formatDate(incrementedDate);
         final DateSection section = createSection(incrementedDate);
        addViewTabAndAddSectionToSchedule(formattedDate, section);
     }
 
    private void addViewTabAndAddSectionToSchedule(final String tabHeader,
                                                   final Section scheduleSection) {
        logger.debug("Creating tab with header {}", tabHeader);
         final DailyScheduleView dayScheduleList = createDayScheduleView();
         dayScheduleList.setSection(scheduleSection);
         weeklyScheduleView.addTab(dayScheduleList);
         weeklyScheduleModel.addSection(scheduleSection);
     }
 
     private DailyScheduleView createDayScheduleView() {
         final DailyScheduleView dayScheduleList = dailyScheduleViewFactory.getObject();
         dayScheduleList.init();
         dailyScheduleViews.add(dayScheduleList);
         return dayScheduleList;
     }
 
     private void createMiscTab() {
        addViewTabAndAddSectionToSchedule("Miscellaneous Items", createNameSection("Miscellaneous Items"));
     }
 
     private NameSection createNameSection(final String name) {
         final NameSection nameSection = nameSectionFactory.getObject();
         nameSection.setName(name);
         return nameSection;
     }
 
     private void createNewSchedule() {
         weeklyScheduleModel.createSchedule();
     }
 
     private DateSection createSection(final Date date) {
         final DateSection dateSection = dateSectionFactory.getObject();
         dateSection.setDate(date);
         return dateSection;
     }
 
     private void createViewDailyTabs(final Date startDate) {
         logger.info("Creating tabs with start date: {}", startDate);
         addDailyTab(startDate);
         Date incrementedDate = startDate;
         for ( int i = 0; i < 6; i++ ) {
             incrementedDate = incrementDate(incrementedDate);
             addDailyTab(incrementedDate);
         }
     }
 
     private String formatDate(final Date incrementedDate) {
         return dateFormatter.format(incrementedDate);
     }
 
     private Date incrementDate(final Date date) {
         return new Date(date.getTime() + DAY_IN_MILLIS);
     }
 
     private void initViews() {
         weeklyScheduleView.init();
         scheduleHistoryList.init();
     }
 
 }
