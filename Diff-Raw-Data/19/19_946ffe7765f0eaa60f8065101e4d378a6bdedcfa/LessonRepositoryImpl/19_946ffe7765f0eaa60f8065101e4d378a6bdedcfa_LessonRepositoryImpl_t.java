 package ua.dp.primat.repositories;
 
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import org.springframework.stereotype.Repository;
 import ua.dp.primat.domain.StudentGroup;
 import ua.dp.primat.domain.lesson.DayOfWeek;
 import ua.dp.primat.domain.lesson.Lesson;
 import ua.dp.primat.domain.lesson.WeekType;
 
 /**
  *
  * @author pesua
  */
 @Repository
 public class LessonRepositoryImpl implements LessonRepository {

     @PersistenceContext(unitName = "curriculum")
     private EntityManager em;
 
     public void store(Lesson lesson) {
         if (contains(lesson)) {
             em.merge(lesson);
        } else {
             em.persist(lesson);
         }
     }
 
     public void remove(Lesson lesson) {
        Lesson l = em.find(Lesson.class, lesson.getId());
        if (l != null) {
             em.remove(lesson);
         }
     }
 
     public Lesson find(Long id) {
         return em.find(Lesson.class, id);
     }
 
     public List<Lesson> getLessons(StudentGroup group) {
         Query query = em.createNamedQuery(Lesson.GET_LESSONS_BY_GROUP_QUERY);
         query.setParameter("group", group);
         return query.getResultList();
     }
 
     public List<Lesson> getLessonsByGroupAndSemester(StudentGroup group, Long semester) {
         Query query = em.createNamedQuery(Lesson.GET_LESSONS_BY_GROUP_AND_SEMESTER_QUERY);
         query.setParameter("group", group);
         query.setParameter("semester", semester);
         return query.getResultList();
     }
 
     public List<Lesson> getLessonsByGroupAndDay(StudentGroup group, Long semester, DayOfWeek dayOfWeek, WeekType weekType) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean contains(Lesson lesson) {
         if (lesson.getId() == null) {
             return false;
         }
        if (em.find(Lesson.class, lesson.getId()) == null) {
             return false;
         }
         return true;
     }
 }
