 package ua.dp.primat.schedule.view.crosstab;
 import ua.dp.primat.domain.lesson.Lesson;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.panel.Panel;
 
 /**
  * Panel, that outputs one schedule cell for specified Lesson.
  * It outputs the subject name, lecturer etc.
  * @author fdevelop
  */
 public final class ScheduleCell extends Panel {
 
     public ScheduleCell(final String id, Lesson lesson) {
         super(id);
 
         final String cellSubject = ((lesson == null) ? "" : lesson.getLessonDescription().getDiscipline().getName());
        final String cellType = ((lesson == null) ? "" : "("+lesson.getLessonDescription().getLessonType().toString()+")");
         final String cellLecturer = ((lesson == null) ? "" : lesson.getLessonDescription().getLecturerNames());
         final String cellRoom = ((lesson == null) ? "" : lesson.getRoom().toString());
         
         add(new Label("cellSubject", cellSubject));
         add(new Label("cellType", cellType));
         add(new Label("cellLecturer", cellLecturer));
         add(new Label("cellRoom", cellRoom));
     }
 
     private static final long serialVersionUID = 1L;
 
 }
 
