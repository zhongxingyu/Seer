 // http://www.byteslounge.com/tutorials/java-ee-cdi-conversationscoped-example
 package org.uli.conversationscope;
 
 import java.io.Serializable;
 import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
 import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
 import javax.inject.Named;
 
 @Named
 @ConversationScoped
 public class CounterBean implements Serializable {
 
   private static final long serialVersionUID = 4771270804699990999L;
   
   private int counter;
   
   // Will only be called once
   // during bean initialization
   @PostConstruct
   public void init(){
     counter = 0;
   }
   
   public void increment(){
     counter++;
   }
   
   public int getCounter() {
     return counter;
   }
 
   public void setCounter(int counter) {
     this.counter = counter;
   }
 }
