 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package models;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import play.db.jpa.Model;
 
 /**
  *
  * @author nile
  */
 @Entity
 public class Subscribe extends Model{
	public static final String TOPOIC_ALL_QESTION="all_question",TOPIC_ANSWER_FOR_ME = "answer_for_me";
     @ManyToOne
     public User user;
     public String topic;
     public static Subscribe unsubscribe(User user,String topic){
         Subscribe subscribe = Subscribe.find("user = ? and topic = ?", user,topic).first();
         if(subscribe !=null){
             subscribe.delete();            
         }
         return subscribe;
     }
     public static Subscribe subscribe(User user,String topic){
         Subscribe subscribe = Subscribe.find("user = ? and topic = ?", user,topic).first();
         if(subscribe == null){
             subscribe = new Subscribe();
             subscribe.user = user;
             subscribe.topic = topic;
             subscribe.save();
         }
         return subscribe;
     }
 }
