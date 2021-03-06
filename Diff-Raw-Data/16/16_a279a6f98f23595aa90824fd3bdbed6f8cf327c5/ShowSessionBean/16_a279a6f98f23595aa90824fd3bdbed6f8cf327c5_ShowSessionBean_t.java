 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ESMS.session;
 
 import ESMS.entity.ShowEntity;
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 /**
  *
  * @author Ser3na
  */
 @Stateless
 public class ShowSessionBean {
 
     @PersistenceContext(unitName = "IRMS-ejbPU")
     private EntityManager em;
     
     ShowEntity show;
     
     public ShowSessionBean(){}
     
     public ShowEntity addShow(ShowEntity show){
         em.persist(show);
         return show;
     }
     
     public boolean deleteShow(Long showId){
         show = em.find(ShowEntity.class, showId);
         if (show==null){
             System.out.println("deleteShow: show does not exist!");
             return false;
         }
         return true;
     }
     
     public boolean updateShow(ShowEntity show)
     {
         em.merge(show);
         System.out.println("ShowSessionBean: show " + show.getShowName() + " is successfully updated");
         return true;
     }
     
    public List<ShowEntity> getAllShows() throws NoResultException{
         Query q = em.createQuery("SELECT m FROM ShowEntity m");
        return q.getResultList();
     } 
 }
