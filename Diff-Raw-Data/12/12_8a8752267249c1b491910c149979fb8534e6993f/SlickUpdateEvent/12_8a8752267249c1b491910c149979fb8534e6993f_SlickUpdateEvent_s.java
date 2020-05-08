 package org.tcrun.slickij.api.events;
 
 import org.codehaus.jackson.type.TypeReference;
 import org.tcrun.slickij.api.data.Copyable;
 
 /**
  * User: jcorbett
  * Date: 11/29/12
  * Time: 1:34 PM
  */
 public class SlickUpdateEvent<T> extends AbstractSlickEvent<UpdateWrapper<T>>
 {
     private TypeReference<UpdateWrapper<T>> type;
     private UpdateWrapper<T> body;
 
     public SlickUpdateEvent(UpdateWrapper<T> update)
     {
         super();
         if (update != null && update.getBefore() != null)
             setTopic("update." + update.getBefore().getClass().getSimpleName());
         else if(update != null && update.getAfter() != null)
             setTopic("update." + update.getAfter().getClass().getSimpleName());
         body = update;
     }
 
     public SlickUpdateEvent(T before, T after)
     {
         super();
        setTopic("update." + before.getClass().getSimpleName());
         body = new UpdateWrapper<T>();
         body.setBefore(before);
         body.setAfter(after);
     }
 
     public SlickUpdateEvent(RawMessage message, TypeReference<UpdateWrapper<T>> type) throws SlickEventException
     {
         super();
         this.type = type;
         loadFromRawMessage(message);
     }
 
     @Override
     protected TypeReference<UpdateWrapper<T>> getTypeReference()
     {
         return type;
     }
 
     @Override
     protected Class<? extends UpdateWrapper<T>> getBodyClass()
     {
         return null;
     }
 
     @Override
     protected UpdateWrapper<T> getBodyObject()
     {
         return body;
     }
 
     @Override
     protected void setBodyObject(UpdateWrapper<T> instance)
     {
         body = instance;
     }
 
     public T getBefore()
     {
         return body.getBefore();
     }
 
     public T getAfter()
     {
         return body.getAfter();
     }
 }
 
 class UpdateWrapper<T>
 {
     private T before;
     private T after;
 
     public UpdateWrapper()
     {
         before = null;
         after = null;
     }
 
     public UpdateWrapper(T before, T after)
     {
         this.before = before;
         this.after = after;
     }
 
     public T getBefore()
     {
         return before;
     }
 
     public void setBefore(T before)
     {
         this.before = before;
     }
 
     public T getAfter()
     {
         return after;
     }
 
     public void setAfter(T after)
     {
         this.after = after;
     }
 }
