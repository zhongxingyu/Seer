 package ua.dp.primat.schedule.view.crosstab;
 import java.util.List;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import ua.dp.primat.domain.lesson.DayOfWeek;
 import ua.dp.primat.domain.lesson.Lesson;
 import ua.dp.primat.domain.lesson.WeekType;
 import ua.dp.primat.schedule.services.CrossTabService;
 import ua.dp.primat.utils.view.RefreshablePanel;
 
 /**
  * View page for the Schedule portlet.
  * @author fdevelop
  */
 public final class ViewCrosstab extends RefreshablePanel {
 
     /**
      * Constructor for page, add ScheduleListView onto the panel.
      */
     public ViewCrosstab(String id) {
         super(id);
 
         for (int i=0;i<DAY_WICKET_KEYS.length;i++) {
             final Label labelDay = new Label(DAYNAME_WICKET + DAY_WICKET_KEYS[i], DayOfWeek.fromNumber(i).toString());
             add(labelDay);
         }
 
         lessonView = new ScheduleListView(ROW_MARKUP, lessons);
         add(lessonView);
     }
 
     /**
      * Method, which updates lessons and lessonView.
      * @param listLesson
      */
     @Override
     public void refreshView(List<Lesson> listLesson) {
         lessons = crossTabService.getCrossTabItems(listLesson);
         if (lessonView != null) {
             lessonView.setList(lessons);
         }
     }
 
     //list and view of all retrieved lessons
     private List<LessonQueryItem> lessons;
     private ListView<LessonQueryItem> lessonView;
 
     @SpringBean
     private CrossTabService crossTabService;
 
     private static final long serialVersionUID = 1L;
 
     //constants for wicket
     private static final String ROW_MARKUP = "row";
     private static final String NUM_COLUMN = "num";
     private static final String DAYNAME_WICKET = "caption_";
     private static final String[] DAY_WICKET_KEYS = {"monday", "tuesday", "wednesday", "thursday", "friday"};
 
     /**
      * ListView, that outputs generated LessonQueryItem list into the table.
      */
     private static class ScheduleListView extends ListView<LessonQueryItem> {
 
         public ScheduleListView(String string, List<? extends LessonQueryItem> list) {
             super(string, list);
         }
 
         @Override
         protected void populateItem(ListItem<LessonQueryItem> li) {
             final LessonQueryItem entry = li.getModelObject();
 
             //format the number of lesson (because of table structure, which has
             //  two cells per day/lesson (depends on lesson's weekType).
             final Label labelNum = new Label(NUM_COLUMN, Integer.toString(entry.getLessonNumber()));
             labelNum.setVisible(entry.getWeekType() != WeekType.DENOMINATOR);
             labelNum.add(rowspanAttrModifier);
             li.add(labelNum);
 
             //output the lesson's info in SchedulePanel for every day
            for (int i = 0; i<DAY_WICKET_KEYS.length; i++) {
                 final DayOfWeek dayOfWeek = DayOfWeek.values()[i];
                final Panel labelDay = new ScheduleCell(DAY_WICKET_KEYS[i],
                        entry.getLessonForDay(dayOfWeek));
                 
                 labelDay.setVisible(isCellVisible(li.getIndex(), dayOfWeek));
 
                 if ((entry.getLessonForDay(dayOfWeek) != null)
                         && (entry.getLessonForDay(dayOfWeek).getWeekType()
                         == WeekType.BOTH)) {
                     labelDay.add(rowspanAttrModifier);
                 }
 
                 li.add(labelDay);
             }
         }
 
         /**
          * Returns true, if the cell must be visible.
          * @param currItemIndex - index of the cell's row
          * @param day
          * @return
          */
         private boolean isCellVisible(int currItemIndex, DayOfWeek day) {
             if (currItemIndex > 0) {
                 LessonQueryItem previous = this.getList().get(currItemIndex-1);
                 if ((previous != null) && (previous.getLessonForDay(day) != null)) {
                     return previous.getLessonForDay(day).getWeekType() != WeekType.BOTH;
                 }
             }
             return true;
         }
 
         private final SimpleAttributeModifier rowspanAttrModifier = new SimpleAttributeModifier("rowspan", "2");
 
     }
 
 }
 
