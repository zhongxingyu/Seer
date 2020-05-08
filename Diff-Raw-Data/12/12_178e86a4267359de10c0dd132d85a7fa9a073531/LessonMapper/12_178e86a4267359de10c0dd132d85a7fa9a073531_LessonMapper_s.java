 package ro.btanase.chordlearning.data;
 
 import java.util.List;
 import java.util.Map;
 
 import ro.btanase.chordlearning.domain.Chord;
 import ro.btanase.chordlearning.domain.Lesson;
 
 public interface LessonMapper {
   public List<Lesson> selectAll();
   public Lesson selectById(int id);
  public int selectLastOrderIdx();
   public Lesson selectPreviousLesson(int orderIdx);
   public Lesson selectNextLesson(int orderIdx);
 
   public int lastInsertId();
   public void insert(Lesson lesson);
   /**
    * <b>parameters</b> map must contain the following keys:<br/>
    * <ul>
    *  <li><b>chordId</b></li>
    *  <li><b>lessonId</b></li>
    * </ul>
    * 
    * @param parameters
    */
   public void insertChordToLesson(Map<String, Object> parameters);
   
   public void update(Lesson lesson);
   public void delete(int id);
 
   public void deleteLessonChords(int lessonId);
   
   public List<Chord> selectChordsByLessonId(int lessonId);
 }
