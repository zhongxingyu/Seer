 package ua.dp.primat.schedule.services;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.springframework.stereotype.Service;
 import ua.dp.primat.domain.lesson.Lesson;
 import ua.dp.primat.domain.lesson.WeekType;
 import ua.dp.primat.schedule.view.crosstab.LessonQueryItem;
 
 /**
  *
  * @author fdevelop
  */
 @Service
 public class CrossTabService {
 
     /**
      * Method, that puts lessons from database in a specific cells
      * into the schedule-table, where row is represented as
      * LessonQueryItem object.
      * @param listLesson - lessons to put
      * @return list of extacly LessonService.LESSONCOUNT*WeekType.TYPECOUNT items
      */
     public List<LessonQueryItem> getCrossTabItems(List<Lesson> listLesson) {
         final List<LessonQueryItem> list = new ArrayList<LessonQueryItem>();
 
         //creates the crosstab structure
         for (int i=1;i<=LessonService.LESSONCOUNT;i++) {
             // add extacly WeekType.TYPECOUNT items for each i
             list.add(new LessonQueryItem(i, WeekType.NUMERATOR));
             list.add(new LessonQueryItem(i, WeekType.DENOMINATOR));
         }
 
         //fill the crosstab with given data
         if (listLesson != null) {
             for (Lesson l : listLesson) {
                 final int oneLessonNumber = l.getLessonNumber().intValue()-1;
                 final int oneWeekType = l.getWeekType().ordinal() % WeekType.TYPECOUNT;
                 //calculate absolute index of the item
                 list.get(oneLessonNumber*WeekType.TYPECOUNT + oneWeekType).setLessonForDay(l.getDayOfWeek(), l);
             }
         }
         return list;
     }
 
 }
