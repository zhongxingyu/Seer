 
 package business;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 
 import controller.studentCtrl;
 
 
 import model.*;
 
 
 @Stateless
 public class DbPerson {
 
 	
     @PersistenceContext
     private EntityManager em;
 	
 	
     public DbPerson() {
 		// TODO Auto-generated constructor stub
 	}
     
     
     // ------------------------------------------ FM -----------------------------createStudent-------------
     // Adds a new Student to db, with all parameters
     
     public void createStudent(
     String gender, String firstname, String lastname, String adresse,
     String email, String phone, String mobil, String keyword){
     		
     	Student s = new Student( gender, firstname, lastname, adresse, email, phone, mobil, keyword);
     	em.persist(s);
     }
     
     
     // ------------------------------------------ FM -------------------------findStudentByNds---------------
     // find a student by nds
     public Student findStudentByNds(int nds){
         Query query = em.createQuery("select c from Student c where c.nds = :nds");
         query.setParameter("nds", nds);
         Student student = (Student)query.getSingleResult();
         return student;
     }
        
     // ------------------------------------------ FM ------------------------findStudentByName---------------
     // find a student by lastname 
     public Student findStudentByName(String name){
         Query query = em.createQuery("select c from Student c where c.name = :name");
         query.setParameter("name", name);
         Student student = (Student)query.getSingleResult();
         return student;
     }
         
     // ------------------------------------------ FM ---------------------getAllStudents----------------------
     // Returns a List of all Students in the db
     public List<Student> getAllStudents() {
         TypedQuery<Student> query = em.createQuery("select c from Student c", Student.class);
         return query.getResultList();
     }
     
     //--------------------------------------------JL----------------------deleteStudentByNds--------------------
     // deletes student from db using nds (prim key)
     public boolean deleteStudentByNds(int nds){
         Query query = em.createQuery("select c from Student c where c.nds = :ndsquery");
         query.setParameter("ndsquery", nds);
         Student student = (Student)query.getSingleResult();
         em.remove(student);
         return true;
     }
     
     // ------------------------------------------ FM ---------------------editStudent-------------------------
     // edits some parameter of given student
     
     public boolean editStudent(Student student){
    	
    	
    	if (student.equals(null)){
    		return false;
    	}else{
     	em.merge(student);
     	return true;
     	}
    }
     
     
 }
