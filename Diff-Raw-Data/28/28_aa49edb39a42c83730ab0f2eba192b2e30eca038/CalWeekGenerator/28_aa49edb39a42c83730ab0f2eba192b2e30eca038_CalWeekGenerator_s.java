 package at.ac.tuwien.sepm.ui.calender.cal;
 
 import at.ac.tuwien.sepm.service.ServiceException;
 import at.ac.tuwien.sepm.ui.UI;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.scheduling.annotation.Scheduled;
 
 import javax.swing.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Flo
  * Date: 01.06.13
  * Time: 15:30
  * To change this template use File | Settings | File Templates.
  */
 @UI
 @Scope("singleton")
 public class CalWeekGenerator extends JPanel implements CalendarInterface {
     private CalPanelWeek calPanelWeek;
     private static final Logger logger = Logger.getLogger(CalWeekGenerator.class);
 
 
     @Autowired
     public CalWeekGenerator(CalPanelWeek calPanelWeek) {
         this.calPanelWeek=calPanelWeek;
         this.setLayout(null);
         this.setOpaque(false);
         this.add(calPanelWeek);
     }
 
     @Override
     public void init() {
         calPanelWeek.init();
     }
 
     @Override
     @Scheduled(fixedDelay = 5000)
     public void refresh() {
         try {
             calPanelWeek.setDates();
         } catch (ServiceException e) {
             logger.error(e);
         }
     }
 
     @Override
     public void semester() {
         calPanelWeek.semester();
     }
 
     @Override
     public void next() throws ServiceException {
         calPanelWeek.next();
     }
 
     @Override
     public void last() throws ServiceException {
         calPanelWeek.last();
     }
 
     @Override
     public String getTimeIntervalInfo() {
         return calPanelWeek.getTimeIntervalInfo();
     }
 
     @Override
     public void goToDay(DateTime day) {
         calPanelWeek.goToDay(day);
     }
 }
