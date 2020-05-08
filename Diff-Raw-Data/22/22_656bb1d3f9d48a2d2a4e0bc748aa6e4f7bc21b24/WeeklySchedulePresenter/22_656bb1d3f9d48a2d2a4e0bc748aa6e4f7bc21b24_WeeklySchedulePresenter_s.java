 package com.bulbview.recipeplanner.ui.presenter;
 
 import java.util.Collection;
 import java.util.Date;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.bulbview.recipeplanner.datamodel.schedule.Schedule;
 import com.bulbview.recipeplanner.datamodel.schedule.Section;
 import com.bulbview.recipeplanner.datamodel.schedule.SectionFactory;
 import com.bulbview.recipeplanner.service.ScheduleService;
import com.bulbview.recipeplanner.ui.manager.ShoppingList;
 import com.bulbview.recipeplanner.ui.manager.WeeklyScheduleView;
 import com.google.appengine.repackaged.com.google.common.collect.Sets;
 
 @Component
 public class WeeklySchedulePresenter extends Presenter<WeeklyScheduleView> implements SessionPresenter {
     
     private static final int                         DAY_IN_MILLIS = 1 * 24 * 60 * 60 * 1000;
     
     @Autowired
     private DailySchedulePresenterFactory            dailySchedulePresenterFactory;
     private final Collection<DailySchedulePresenter> dailySchedulePresenters;
     @Autowired
     private ScheduleHistoryPresenter                 scheduleHistoryPresenter;
     @Autowired
     private ScheduleService                          scheduleService;
     
     @Autowired
     private SectionFactory                           sectionFactory;
     @Autowired
    private ShoppingList                             shoppingList;
     
     @Autowired
     private WeeklyScheduleModel                      weeklyScheduleModel;
     
     public WeeklySchedulePresenter() {
         this.dailySchedulePresenters = Sets.newHashSet();
     }
     
     public void clearSchedule() {
         for (final DailySchedulePresenter presenter : dailySchedulePresenters) {
             presenter.clear();
         }
        shoppingList.clear();
     }
     
     public void display(final Schedule schedule) {
         getView().clearSectionsFromSchedule();
         weeklyScheduleModel.setSchedule(schedule);
         for (final Section section : schedule.getSections()) {
             createTab(section);
         }
     }
     
     @Override
     public void init() {
         createSchedule();
         getView().init();
     }
     
     public void saveSchedule() {
         final Schedule schedule = weeklyScheduleModel.getSchedule();
         scheduleService.save(schedule);
         updateScheduleHistoryView(schedule);
     }
     
     public void setDailySchedulePresenterFactory(final DailySchedulePresenterFactory dailySchedulePresenterFactory) {
         this.dailySchedulePresenterFactory = dailySchedulePresenterFactory;
     }
     
     public void setModel(final WeeklyScheduleModel model) {
         this.weeklyScheduleModel = model;
     }
     
     public void setScheduleHistoryPresenter(final ScheduleHistoryPresenter scheduleHistoryPresenter) {
         this.scheduleHistoryPresenter = scheduleHistoryPresenter;
     }
     
     public void setSectionFactory(final SectionFactory sectionFactory) {
         this.sectionFactory = sectionFactory;
     }
     
    public void setShoppingList(final ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
     }
     
     public void setStartDate(final Date startDate) {
         createSchedule();
         weeklyScheduleModel.setStartDate(startDate);
         getView().clearSectionsFromSchedule();
         createWeekTabs(startDate);
         createTab(sectionFactory.create("Miscellaneous Items"));
     }
     
     @Override
     @Autowired
     public void setView(final WeeklyScheduleView weeklyScheduleView) {
         super.setView(weeklyScheduleView);
     }
     
     private void addTab(final Section section) {
         final DailySchedulePresenter daySchedulePresenter = createDaySchedulePresenter(section);
         weeklyScheduleModel.addSection(section);
         getView().addTab(daySchedulePresenter.getView());
     }
     
     private DailySchedulePresenter createDaySchedulePresenter(final Section section) {
         final DailySchedulePresenter daySchedulePresenter = dailySchedulePresenterFactory.create();
         dailySchedulePresenters.add(daySchedulePresenter);
         daySchedulePresenter.setSection(section);
         return daySchedulePresenter;
     }
     
     private void createSchedule() {
         final Schedule schedule = scheduleService.createSchedule();
         weeklyScheduleModel.setSchedule(schedule);
     }
     
     private void createTab(final Section section) {
         final DailySchedulePresenter daySchedulePresenter = createDaySchedulePresenter(section);
         weeklyScheduleModel.addSection(section);
         getView().addTab(daySchedulePresenter.getView());
     }
     
     private void createWeekTabs(final Date startDate) {
         int i = 0;
         Date incrementDate = startDate;
         do {
             logger.info("Creating tab with date: {}", startDate);
             addTab(sectionFactory.create(incrementDate));
             incrementDate = incrementDate(incrementDate);
             i++;
         } while (i < 7);
     }
     
     private Date incrementDate(final Date date) {
         return new Date(date.getTime() + DAY_IN_MILLIS);
     }
     
     private void updateScheduleHistoryView(final Schedule schedule) {
         scheduleHistoryPresenter.addScheduleToView(schedule);
     }
     
 }
