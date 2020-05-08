 package controller;
 
 import java.util.HashMap;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 
 import model.Course;
 import model.Pair;
 import model.Student;
 import model.Teacher;
 import model.dao.DaoFactory;
 import model.dao.TeacherDao;
 import model.dao.TeachesDao;
 import model.dao.TutorsDao;
 
 public class TeacherController implements Observer {
 
     private static TeacherController controller;
     Observable observable;
     HashMap<Student,Integer> alert;
     private TeacherDao dao = (TeacherDao) DaoFactory.getTeacherDao();
     private TeachesDao teachesdao = (TeachesDao) DaoFactory.getTeachesDao();
     private TutorsDao tutorsdao = (TutorsDao) DaoFactory.getTutorsDao();
 
     /**
      * The only constructor, the private no-argument constructor, can only be
      * called from this class within the getInstance method. It should be called
      * exactly once, the first time getInstance is called.
      */
     private TeacherController() {
         if (controller == null) {
             controller = this;
             observable = StudentController.getInstance();
             observable.addObserver(this);
             alert = new HashMap<Student, Integer>();
         }
         else
             throw new IllegalArgumentException("Default constructor called more than once.");
     }
 
     public static TeacherController getInstance() {
         if (controller == null)
             controller = new TeacherController();
         return controller;
     }
 
     public boolean addCourse(Teacher t, Course course) {
         if (t != null && course != null) {
             return teachesdao.create(new Pair<Teacher, Course>(t, course));
         }
         return false;
     }
     
     public Set<Course> getCourse(Teacher t) {
         return teachesdao.getCourseByTeacher(t);
     }
 
     public Set<Teacher> getTeachers() {
         return dao.findAll();
     }
     
     public Set<Student> getStudentFromTutor(Teacher t) {
         return tutorsdao.findStudentByTeacher(t);
     }
     
     public boolean addStudentForTutor(Teacher t, Student s) {
         if (t == null || s == null) return false;
         return tutorsdao.create(new Pair<Teacher, Student>(t,s));
     }
 
     public boolean addTeacher(String name) {
        Teacher s = new Teacher((int) (Math.random() * 10000) + 20, name);
        return dao.create(s);
     }
 
     public boolean removeTeacher(Teacher t) {
         return dao.delete(t) && teachesdao.delete(t) && tutorsdao.delete(t);
     }
 
     @Override
     public void update(Observable obs, Object arg) {
         if (obs instanceof StudentController && arg instanceof Student) {
             if(alert.containsKey(arg)) {
                 alert.put((Student) arg, alert.get(arg)+1);
                 if(alert.get(arg) == 3) {
                 	StudentController.getInstance().setAlerted((Student)arg);               
                     alert.remove(arg);
                 }
             } else
                 alert.put((Student) arg, 1);
         }
     }
 
 }
