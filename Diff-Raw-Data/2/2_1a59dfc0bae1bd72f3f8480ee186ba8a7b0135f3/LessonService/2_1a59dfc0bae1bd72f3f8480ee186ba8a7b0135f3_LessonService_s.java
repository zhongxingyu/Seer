 package ua.dp.primat.schedule.services;
 
 import java.util.Arrays;
 import java.util.List;
 import javax.annotation.Resource;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import ua.dp.primat.domain.Lecturer;
 import ua.dp.primat.domain.Room;
 import ua.dp.primat.domain.StudentGroup;
 import ua.dp.primat.domain.lesson.*;
 import ua.dp.primat.domain.workload.Discipline;
 import ua.dp.primat.repositories.DisciplineRepository;
 import ua.dp.primat.repositories.LecturerRepository;
 import ua.dp.primat.repositories.LessonDescriptionRepository;
 import ua.dp.primat.repositories.LessonRepository;
 import ua.dp.primat.repositories.RoomRepository;
 
 /**
  * Service for operations on objects Lesson, LessonDescription, Lecturer,
  * Room.
  * @author fdevelop
  */
 @Service
 @Transactional
 public class LessonService {
 
     public void saveLesson(Lesson lesson) {
         final Long id = lesson.getLessonDescription().getId();
         final LessonDescription descr = (id != null) ? lessonDescriptionRepository.find(id) : null;
         if (descr != null) {
             lesson.setLessonDescription(descr);
         } else {
             lessonDescriptionRepository.store(lesson.getLessonDescription());
         }
         lessonRepository.store(lesson);
     }
 
     public void deleteLesson(Long id) {
         Lesson lesson = lessonRepository.find(id);
         lessonRepository.remove(lesson);
     }
 
     public void saveLessonDescription(LessonDescription lessonDescription) {
         lessonDescriptionRepository.store(lessonDescription);
     }
 
     public void deleteLessonDescription(LessonDescription lessonDescription) {
         lessonDescriptionRepository.remove(lessonDescription);
     }
 
     public void saveRoom(Room room) {
         roomRepository.store(room);
     }
 
     public void deleteRoom(Room room) {
         roomRepository.delete(room);
     }
 
     public void saveLecturer(Lecturer lecturer) {
         lecturerRepository.store(lecturer);
     }
 
     public void deleteLecturer(Lecturer lecturer) {
         lecturerRepository.delete(lecturer);
     }
 
     public void saveDiscipline(Discipline discipline) {
         disciplineRepository.store(discipline);
     }
 
     public void deleteDiscipline(Discipline discipline) {
         disciplineRepository.delete(discipline);
     }
 
     public List<Lesson> getLessonsForGroupAndSemester(StudentGroup studentGroup,
             Long semester) {
         return lessonRepository.getLessonsByGroupAndSemester(studentGroup, semester);
     }
 
     /**
      * Query, that returns extacly LESSONCOUNT lessons for the specified
      * DayOfWeek and WeekType.
      * @param listLesson - data to search in
      * @param day
      * @param week
      * @return the list of Lesson, which are accepted by parameters
      */
     public List<Lesson> getLessonsPerDay(List<Lesson> listLesson, DayOfWeek day, WeekType week) {
         final Lesson[] dayLessons = new Lesson[LESSONCOUNT];
 
         //fill an array with empty items
         for (int i = 0; i < dayLessons.length; i++) {
            dayLessons[i] = new Lesson(Long.valueOf(i), WeekType.BOTH, day, null, null);
         }
 
         //if no data - leave the method
         if (listLesson == null) {
             return Arrays.asList(dayLessons);
         }
 
         for (Lesson l : listLesson) {
             if ((l.getDayOfWeek() == day)
                     && ((l.getWeekType() == week)
                     || (l.getWeekType() == WeekType.BOTH))) {
                 int lessonNumber = l.getLessonNumber().intValue();
                 if ((lessonNumber > 0) && (lessonNumber <= LESSONCOUNT)) {
                     dayLessons[lessonNumber-1] = l;
                 }
             }
         }
 
         return Arrays.asList(dayLessons);
     }
 
     @Resource
     private LessonRepository lessonRepository;
 
     @Resource
     private LessonDescriptionRepository lessonDescriptionRepository;
 
     @Resource
     private DisciplineRepository disciplineRepository;
 
     @Resource
     private RoomRepository roomRepository;
 
     @Resource
     private LecturerRepository lecturerRepository;
 
     public static final int LESSONCOUNT = 5;
 }
