 package EntityManager;
 
 import Entity.Program;
 
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Sean
  * Date: 3/18/13
  * Time: 11:19 PM
  * To change this template use File | Settings | File Templates.
  */
 @Stateless
 @LocalBean
 public class ProgramManager {
 
     @PersistenceContext
     private EntityManager em;
 
     public List<Program> getPrograms() {
         TypedQuery<Program> ProgramQuery = em.createQuery("SELECT p FROM Program p", Program.class);
         return ProgramQuery.getResultList();
     }
 
//    public Integer getStartingSalary(){
//
//    }
 }
