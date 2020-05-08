 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.chalmers.fannysangles.friendzone.model.managers;
 
 import it.chalmers.fannysangles.friendzone.model.AbstractUser_;
 import it.chalmers.fannysangles.friendzone.model.Comment;
 import it.chalmers.fannysangles.friendzone.model.EventPost;
 import it.chalmers.fannysangles.friendzone.model.FriendzoneUser;
 import it.chalmers.fannysangles.friendzone.model.Tag;
 import it.chalmers.fannysangles.friendzone.utils.AbstractManager;
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.persistence.Query;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Expression;
 import javax.persistence.criteria.Root;
 
 /**
  * Just a template for a manager at the moment...
  * @author Pontus
  */
 @Stateless
 @LocalBean
 public class UserManager extends AbstractManager<FriendzoneUser,Long> {
     
    @EJB
    CommentManager commentManager;
    
     public UserManager(){
         super(FriendzoneUser.class);
     }
     
     public FriendzoneUser getAnonymousUser(){
         if(this.getRange(0, 1).size() < 1){
             this.add(new FriendzoneUser("Friend", "friend", "friend@friendzone.net", "Friend Friendster", 0, "", new ArrayList<Tag>()));
         }
         return (FriendzoneUser) this.getRange(0, 1).get(0);
     }
     
     /**
      * Finds a user by username
      * @param userName - the username of the friendzoneuser
      * @return - the found user, can only return one since username is unique
      */
     public FriendzoneUser getUser(String userName){
         FriendzoneUser found;
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery cq = cb.createQuery();
         Root<FriendzoneUser> root = cq.from(FriendzoneUser.class);
         cq.select(root);
         Expression<Boolean> expression = cb.equal(root.get(AbstractUser_.username),userName);
         cq.where(expression);
         
         Query q = em.createQuery(cq);
         if(q.getResultList() != null){
             found = (FriendzoneUser) q.getResultList().get(0);
             return found;
         } else{
             return null;
         }
         
         
     }
       //BAD CODE
 //    @Override
 //    public void remove(Long userId){
 //        List<Comment> commentList = new ArrayList<>();
 //        commentList.addAll(commentManager.getRange(0, commentManager.getCount()));
 //        for(Comment comment : commentList){
 //            if(comment.getCreator().getId() == userId){
 //                commentManager.remove(comment.getId());
 //            }
 //        }
 //        super.remove(userId);
 //    }
    
 }
