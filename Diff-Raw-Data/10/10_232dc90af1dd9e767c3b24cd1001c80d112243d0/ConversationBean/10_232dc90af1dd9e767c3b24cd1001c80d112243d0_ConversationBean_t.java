 // http://www.byteslounge.com/tutorials/java-ee-cdi-conversationscoped-example
 package org.uli.conversationscope;
 
 import java.io.Serializable;
 import javax.enterprise.context.Conversation;
 import javax.enterprise.context.ConversationScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 @Named
 @ConversationScoped
 public class ConversationBean implements Serializable {
 
   private static final long serialVersionUID = 4771270804699990999L;
   
   @Inject
   private Conversation conversation;
   
   public void initConversation(){
     if (!FacesContext.getCurrentInstance().isPostback() 
       && conversation.isTransient()) {
       
       conversation.begin();
     }
   }
   
   public String handleFirstStepSubmit(){
     return "step2?faces-redirect=true";
   }
   
   public String endConversation(){
     if(!conversation.isTransient()){
       conversation.end();
     }
     return "step1?faces-redirect=true";
   }
 
   public Conversation getConversation() {
     return conversation;
   }
   
 }
