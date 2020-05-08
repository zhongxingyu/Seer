 package net.helloworld.dao;
 
 import net.helloworld.model.Student;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 /**
  * Date: 17/10/13
  * Time: 11:12 PM
  */
 @Repository
 @Transactional
 public class StudentDaoImpl implements StudentDao {
 
     @Autowired
     private SessionFactory sessionFactory;
 
     private Session getCurrentSession() {
         return sessionFactory.getCurrentSession();
     }
 
     @Override
     public void addStudent(Student student) {
         getCurrentSession().save(student);
     }
 
     @Override
     public void updateStudent(Student student) {
         Student studentToUpdate = getStudent(student.getId());
         studentToUpdate.setFirstName(student.getFirstName());
         studentToUpdate.setOtherNames(student.getOtherNames());
         studentToUpdate.setLastName(student.getLastName());
         getCurrentSession().update(studentToUpdate);
 
     }
 
     @Override
     public Student getStudent(int id) {
         Student student = (Student) getCurrentSession().get(Student.class, id);
         return student;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public List<Student> getAllStudents() {
         return sessionFactory.getCurrentSession().createQuery("from Student").list();
     }
 
     public void setSessionFactory(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 }
