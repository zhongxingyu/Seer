 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.chalmers.fannysangles.friendzone.model.managers;
 
 import it.chalmers.fannysangles.friendzone.model.EventPost;
 import it.chalmers.fannysangles.friendzone.utils.AbstractManager;
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.persistence.Query;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Expression;
 import javax.persistence.criteria.ParameterExpression;
 import javax.persistence.criteria.Root;
 
 /**
  *
  * @author marcusisaksson
  */
 @Stateless
public class EventPostManager extends AbstractManager<EventPost,Long> {
     
     
     public EventPostManager(){
         super(EventPost.class);
     }
     
     public List<EventPost> search(String searchString){
         List<EventPost> found = new ArrayList<>();
         
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery cq = cb.createQuery();
         Root<EventPost> root = cq.from(EventPost.class);
         Expression<String> param = cb.parameter(String.class);
         
         cq.select(root).where(cb.substring(param, 0));
         
         
         Query q = em.createQuery(cq);
         found.addAll(q.getResultList());
         
         
         return found;
     }
     
     
 }
