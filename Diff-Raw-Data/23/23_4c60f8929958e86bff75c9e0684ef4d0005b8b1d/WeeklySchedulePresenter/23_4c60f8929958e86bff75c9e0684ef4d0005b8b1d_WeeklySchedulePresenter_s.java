 package com.bulbview.recipeplanner.ui.presenter;
 
 import java.text.DateFormat;
 import java.util.Collection;
 import java.util.Date;
 
 import org.springframework.beans.factory.ObjectFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.bulbview.recipeplanner.datamodel.Day;
 import com.bulbview.recipeplanner.datamodel.Schedule;
 import com.bulbview.recipeplanner.persistence.DaoException;
 import com.bulbview.recipeplanner.persistence.ScheduleObjectifyDao;
 import com.bulbview.recipeplanner.ui.DailySchedule;
 import com.bulbview.recipeplanner.ui.manager.MainWindowUiManager;
 import com.bulbview.recipeplanner.ui.manager.ScheduleHistoryList;
 import com.bulbview.recipeplanner.ui.manager.WeeklySchedule;
 import com.google.appengine.repackaged.com.google.common.collect.Sets;
 
 @Component
 public class WeeklySchedulePresenter extends Presenter implements SessionPresenter {
 
     private static final int                DAY_IN_MILLIS = 1 * 24 * 60 * 60 * 1000;
    private final Collection<DailySchedule> dailySchedules;
 
     private final DateFormat                dateFormatter;
     @Autowired
     private ObjectFactory<Day>              dayFactory;
    private ObjectFactory<DailySchedule>    dailyScheduleListFactory;
     @Autowired
     private MainWindowUiManager             mainWindow;
     private Schedule                        schedule;
     @Autowired
     private ScheduleObjectifyDao            scheduleDao;
     private ObjectFactory<Schedule>         scheduleFactory;
     @Autowired
     private ScheduleHistoryList             scheduleHistoryList;
     private Date                            startDate;
     @Autowired
     private WeeklySchedule                  weeklySchedule;
 
     public WeeklySchedulePresenter() {
         this.dateFormatter = DateFormat.getDateInstance();
         this.dailySchedules = Sets.newHashSet();
     }
 
     public void clearSchedule() {
         for ( final DailySchedule dailySchedule : dailySchedules ) {
             dailySchedule.clear();
         }
     }
 
     public void createTab(final String header) {
         logger.debug("Creating tab with header {}", header);
         final DailySchedule dayScheduleList = createDaySchedule();
         weeklySchedule.addTab(header, dayScheduleList);
 
     }
 
     @Override
     public void init() {
         weeklySchedule.init();
         scheduleHistoryList.init();
        createNewSchedule();
         createAllTabs();
     }
 
     public void saveSchedule() {
         try {
             scheduleDao.save(schedule);
         } catch (final DaoException e) {
             throw new WeeklySchedulePresenterException("Error saving schedule", e);
         }
     }
 
     @Autowired
     public void setDailyScheduleListFactory(final ObjectFactory<DailySchedule> dayScheduleListFactory) {
         this.dailyScheduleListFactory = dayScheduleListFactory;
     }
 
     @Autowired
     public void setScheduleFactory(final ObjectFactory<Schedule> scheduleFactory) {
         this.scheduleFactory = scheduleFactory;
     }
 
     @Autowired
     public void setStartDate(final Date startDate) {
         this.startDate = startDate;
         logger.debug("start date: {}", startDate);
     }
 
     @Autowired
     public void setWeeklySchedule(final WeeklySchedule weeklySchedule) {
         this.weeklySchedule = weeklySchedule;
     }
 
     public void showHistory() {
         mainWindow.showScheduleHistoryWindow();
     }
 
     private void createAllTabs() {
         createDailyTabs();
         createTab("Miscellaneous Items");
     }
 
     private void createDailyTabs() {
        createDayAndAddToSchedule(startDate);
         Date incrementedDate = startDate;
         for ( int i = 0; i < 6; i++ ) {
             incrementedDate = incrementDate(incrementedDate);
            createDayAndAddToSchedule(incrementedDate);
         }
     }
 
     private Day createDay(final Date date) {
         final Day day = dayFactory.getObject();
         day.setDate(date);
         return day;
     }
 
     private void createDayAndAddToSchedule(final Date incrementedDate) {
         final String formatedDate = getFormattedDateString(incrementedDate);
         logger.debug("Creating daily tab: {}...", formatedDate);
         schedule.addDay(createDay(incrementedDate));
        createTab(formatedDate);
     }
 
     private DailySchedule createDaySchedule() {
         final DailySchedule dayScheduleList = dailyScheduleListFactory.getObject();
         dayScheduleList.init();
         dailySchedules.add(dayScheduleList);
         return dayScheduleList;
     }
 
     private void createNewSchedule() {
         this.schedule = scheduleFactory.getObject();
         schedule.setName(getFormattedDateString(startDate));
         logger.debug("...Schedule created {} ", schedule);
 
     }
 
     private String getFormattedDateString(final Date incrementedDate) {
         return dateFormatter.format(incrementedDate);
     }
 
     private Date incrementDate(final Date date) {
         return new Date(date.getTime() + DAY_IN_MILLIS);
     }
 
 }
