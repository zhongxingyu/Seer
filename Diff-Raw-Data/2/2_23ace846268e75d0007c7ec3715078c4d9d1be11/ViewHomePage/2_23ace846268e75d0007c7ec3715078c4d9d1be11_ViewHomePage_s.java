 package ua.dp.primat.schedule.view;
 import ua.dp.primat.schedule.view.daybook.ViewDaybook;
 import ua.dp.primat.schedule.view.crosstab.ViewCrosstab;
 import ua.dp.primat.schedule.data.Lesson;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
 import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
 import org.apache.wicket.extensions.markup.html.tabs.ITab;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import ua.dp.primat.curriculum.data.Cathedra;
 import ua.dp.primat.schedule.data.DayOfWeek;
 import ua.dp.primat.curriculum.data.Discipline;
 import ua.dp.primat.schedule.data.Lecturer;
 import ua.dp.primat.schedule.data.LessonDescription;
 import ua.dp.primat.schedule.data.LessonType;
 import ua.dp.primat.schedule.data.Room;
 import ua.dp.primat.curriculum.data.StudentGroup;
 import ua.dp.primat.curriculum.data.StudentGroupRepository;
 import ua.dp.primat.curriculum.data.StudentGroupRepositoryImpl;
 import ua.dp.primat.schedule.data.LecturerType;
 import ua.dp.primat.schedule.data.LessonRepository;
 import ua.dp.primat.schedule.data.LessonRepositoryimpl;
 import ua.dp.primat.schedule.data.WeekType;
 import ua.dp.primat.utils.view.ChoosePanel;
 import ua.dp.primat.utils.view.RefreshablePanel;
 
 /**
  * View page for the Schedule portlet.
  * @author fdevelop
  */
 public final class ViewHomePage extends WebPage {
 
     private static final long serialVersionUID = 1L;
 
     private RefreshablePanel schedulePanel;
     private RefreshablePanel daybookPanel;
 
     private String tabScheduleText;
     private String tabDaybookText;
 
     private List<Lesson> queryResult;
 
     /**
      * Contructor for the home page, which adds tabs and choose panel
      */
     public ViewHomePage() {
         super();
         languageLoad();
 
         // create the tabbed panel
         List<ITab> tabs = new ArrayList<ITab>();
         tabs.add(new AbstractTab(new Model<String>(tabDaybookText))
         {
                 @Override
                 public Panel getPanel(String panelId)
                 {
                     daybookPanel = new ViewDaybook(panelId);
                     daybookPanel.refreshView(queryResult);
                     return daybookPanel;
                 }
         });
 
         tabs.add(new AbstractTab(new Model<String>(tabScheduleText))
         {
                 @Override
                 public Panel getPanel(String panelId)
                 {
                     schedulePanel = new ViewCrosstab(panelId);
                     schedulePanel.refreshView(queryResult);
                     return schedulePanel;
                 }
         });
         add(new AjaxTabbedPanel("tabs", tabs));
 
         //create chooser for group and semester
         final ChoosePanel choosePanel = new ChoosePanel("choosePanel") {
 
             @Override
             protected void executeAction(StudentGroup studentGroup, Long semester) {
                 queryResult = getLessons(studentGroup, semester);
                 if (schedulePanel != null) {
                     schedulePanel.refreshView(queryResult);
                 }
                 if (daybookPanel != null) {
                     daybookPanel.refreshView(queryResult);
                 }
             }
         };
         add(choosePanel);
     }
 
     /**
      * Load language resources from assigned to page .properties file.
      */
     private void languageLoad() {
         final ResourceBundle bundle = ResourceBundle.getBundle("ua.dp.primat.schedule.view.ViewHomePage");
         tabScheduleText = bundle.getString("tab.crosstab");
         tabDaybookText = bundle.getString("tab.daybook");
     }
 
     //TEMPORARY method for returning the list of lessons
     //TODO: remove it, when there will be an entity repository with this operation
     private List<Lesson> getLessons(StudentGroup studentGroup, Long semester) {
         final List<Lesson> list  = new ArrayList<Lesson>();
 
         final Cathedra cathedra = new Cathedra();
         cathedra.setName(".. .");
 
         final Room room46 = new Room(Long.valueOf(3), Long.valueOf(46));
         final Room room45 = new Room(Long.valueOf(3), Long.valueOf(45));
         final Room room31 = new Room(Long.valueOf(3), Long.valueOf(31));
         final Room room22 = new Room(Long.valueOf(2), Long.valueOf(25));
 
         final Discipline d1 = new Discipline("    ", cathedra);
         final Discipline d2 = new Discipline(" ", cathedra);
         final Discipline d3 = new Discipline("", cathedra);
         final Discipline d4 = new Discipline(" ..", cathedra);
         final Discipline d5 = new Discipline("Գ ", cathedra);
         final Discipline d6 = new Discipline(" ", cathedra);
 
         final Lecturer teacher1 = new Lecturer(" . .", cathedra, LecturerType.SENIORLECTURER);
         final Lecturer teacher2 = new Lecturer(" . .", cathedra, LecturerType.SENIORLECTURER);
         final Lecturer teacher3 = new Lecturer(" . .", cathedra, LecturerType.ASSIATANT);
         final Lecturer teacher4 = new Lecturer(" . .", cathedra, LecturerType.ASSIATANT);
         final Lecturer teacher5 = new Lecturer(" . .", cathedra, LecturerType.SENIORLECTURER);
         final Lecturer teacher6 = new Lecturer(" . .", cathedra, LecturerType.DOCENT);
         final Lecturer teacher7 = new Lecturer(" . .", cathedra, LecturerType.SENIORLECTURER);
         final Lecturer teacher8 = new Lecturer(" . .", cathedra, LecturerType.DOCENT);
         final Lecturer teacher9 = new Lecturer(" . .", cathedra, LecturerType.DOCENT);
 
         final LessonDescription ld1 = new LessonDescription(d1, studentGroup, Long.valueOf(4), LessonType.LECTURE, teacher1, null);
         final LessonDescription ld2 = new LessonDescription(d2, studentGroup, Long.valueOf(4), LessonType.LECTURE, teacher2, null);
         final LessonDescription ld3 = new LessonDescription(d2, studentGroup, Long.valueOf(4), LessonType.LABORATORY, teacher3, teacher4);
         final LessonDescription ld4 = new LessonDescription(d3, studentGroup, Long.valueOf(4), LessonType.LABORATORY, teacher5, teacher4);
         final LessonDescription ld5 = new LessonDescription(d4, studentGroup, Long.valueOf(4), LessonType.LABORATORY, teacher6, teacher3);
         final LessonDescription ld6 = new LessonDescription(d4, studentGroup, Long.valueOf(4), LessonType.LECTURE, teacher6, null);
         final LessonDescription ld7 = new LessonDescription(d5, studentGroup, Long.valueOf(4), LessonType.PRACTICE, teacher7, null);
         final LessonDescription ld8 = new LessonDescription(d6, studentGroup, Long.valueOf(4), LessonType.LECTURE, teacher8, null);
         final LessonDescription ld9 = new LessonDescription(d6, studentGroup, Long.valueOf(4), LessonType.PRACTICE, teacher8, null);
         final LessonDescription ld0 = new LessonDescription(d3, studentGroup, Long.valueOf(4), LessonType.LECTURE, teacher9, null);
 
        if ((semester == 4) && (studentGroup.getCode().equalsIgnoreCase(""))) {
             list.add(new Lesson(Long.valueOf(2), WeekType.BOTH, DayOfWeek.THURSDAY, room45, ld5));
             list.add(new Lesson(Long.valueOf(2), WeekType.BOTH, DayOfWeek.MONDAY, room22, ld7));
             list.add(new Lesson(Long.valueOf(2), WeekType.BOTH, DayOfWeek.FRIDAY, room22, ld7));
             list.add(new Lesson(Long.valueOf(2), WeekType.BOTH, DayOfWeek.TUESDAY, room46, ld8));
             list.add(new Lesson(Long.valueOf(3), WeekType.BOTH, DayOfWeek.MONDAY, room31, ld1));
             list.add(new Lesson(Long.valueOf(3), WeekType.BOTH, DayOfWeek.TUESDAY, room31, ld2));
             list.add(new Lesson(Long.valueOf(3), WeekType.BOTH, DayOfWeek.FRIDAY, room46, ld6));
             list.add(new Lesson(Long.valueOf(4), WeekType.NUMERATOR, DayOfWeek.THURSDAY, room45, ld3));
             list.add(new Lesson(Long.valueOf(4), WeekType.BOTH, DayOfWeek.WEDNESDAY, room45, ld4));
             list.add(new Lesson(Long.valueOf(4), WeekType.BOTH, DayOfWeek.MONDAY, room46, ld0));
             list.add(new Lesson(Long.valueOf(5), WeekType.DENOMINATOR, DayOfWeek.MONDAY, room46, ld9));
         }
 
         return list;
     }
 
 }
