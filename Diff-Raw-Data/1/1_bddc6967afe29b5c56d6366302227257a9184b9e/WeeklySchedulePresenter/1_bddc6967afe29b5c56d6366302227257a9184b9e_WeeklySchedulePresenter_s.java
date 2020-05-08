 package com.bulbview.recipeplanner.ui.presenter;
 
 import java.text.DateFormat;
 import java.util.Date;
 
 import org.springframework.beans.factory.ObjectFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.bulbview.recipeplanner.datamodel.Day;
 import com.bulbview.recipeplanner.datamodel.Schedule;
 import com.bulbview.recipeplanner.persistence.DaoException;
 import com.bulbview.recipeplanner.persistence.ScheduleObjectifyDao;
 import com.bulbview.recipeplanner.ui.manager.MainWindowUiManager;
 import com.bulbview.recipeplanner.ui.manager.ScheduleHistoryList;
 import com.bulbview.recipeplanner.ui.manager.WeeklySchedule;
 
 @Component
 public class WeeklySchedulePresenter extends Presenter implements SessionPresenter {
 
     private static final int        DAY_IN_MILLIS = 1 * 24 * 60 * 60 * 1000;
     private final DateFormat        dateFormatter;
 
     @Autowired
     private ObjectFactory<Day>      dayFactory;
     @Autowired
     private MainWindowUiManager     mainWindow;
     private Schedule                schedule;
     @Autowired
     private ScheduleObjectifyDao    scheduleDao;
     private ObjectFactory<Schedule> scheduleFactory;
     @Autowired
     private ScheduleHistoryList     scheduleHistoryList;
     private Date                    startDate;
     @Autowired
     private WeeklySchedule          weeklySchedule;
 
     public WeeklySchedulePresenter() {
         this.dateFormatter = DateFormat.getDateInstance();
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
         weeklySchedule.createTab("Miscellaneous Items");
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
         final String header = getFormattedDateString(incrementedDate);
         logger.debug("Creating daily tab: {}...", header);
         schedule.addDay(createDay(incrementedDate));
         weeklySchedule.createTab(header);
     }
 
     private void createNewSchedule() {
         this.schedule = scheduleFactory.getObject();
         logger.debug("...Schedule created {} ", schedule);
 
     }
 
     private String getFormattedDateString(final Date incrementedDate) {
         return dateFormatter.format(incrementedDate);
     }
 
     private Date incrementDate(final Date date) {
         return new Date(date.getTime() + DAY_IN_MILLIS);
     }
 
 }
