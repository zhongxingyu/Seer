 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.chalmers.fannysangles.friendzone.model.managers;
 
 import it.chalmers.fannysangles.friendzone.model.AbstractPost_;
 import it.chalmers.fannysangles.friendzone.model.Comment;
 import it.chalmers.fannysangles.friendzone.model.EventPost;
 import it.chalmers.fannysangles.friendzone.model.EventPost_;
 import it.chalmers.fannysangles.friendzone.model.FriendzoneUser;
 import it.chalmers.fannysangles.friendzone.utils.AbstractManager;
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.persistence.Query;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Expression;
 import javax.persistence.criteria.Root;
 
 /**
  *
  * @author marcusisaksson
  */
 @Stateless
 @LocalBean
 public class EventPostManager extends AbstractManager<EventPost,Long> {
     
     
     
     public EventPostManager(){
         super(EventPost.class);
     }
     
     public List<EventPost> search(String searchString){
        
        List<EventPost> found = new ArrayList();
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery cq = cb.createQuery();
         Root<EventPost> root = cq.from(EventPost.class);
         cq.select(root);
         
         //Search in title
         //TODO: Only returns equals string
        Expression<Boolean> expression = cb.like(root.get(AbstractPost_.title),searchString);
        cq.where(expression);
         
         Query q = em.createQuery(cq);
        found.addAll(q.getResultList());
        
        //Search in description
        //TODO: Only returns equals string
        expression = cb.like(root.get(EventPost_.description),searchString);
        cq.where(expression);
         
         cq.orderBy(cb.desc(root.get(EventPost_.dateCreated)));
         
        q = em.createQuery(cq);
        found.addAll(q.getResultList());
        
        return found;
     }
     
     /**
      * 
      * @param eventId - The id of the event from which comments you want.
      * @return - A list of all comments from the event.
      */
     public List<Comment> getAllCommentsFromEvent(Long eventId){
         
         return find(eventId).getComments();
         
     }
     
     public List<EventPost> searchByUser(FriendzoneUser user, EventPostManager emp) {
         List<EventPost> found = new ArrayList();
         
         for(EventPost e : emp.getRange(0, emp.getCount())) {
             if(e.getAllAttending().contains(user)) {
                 found.add(e);
             }
         }
         
         return found;
     }
     
     /**
      * Adds a comment to the EventPost with the given id.
      * @param eventPostId
      * @param commentContent 
      */
     public void addCommentToEventPost( Long eventPostId, Comment newComment){
         EventPost eventPost = find(eventPostId);
         eventPost.addComment(newComment);
         eventPost = this.update(eventPost);
     }
 }
