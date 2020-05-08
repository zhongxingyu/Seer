 package org.daum.library.replica.listener;
 
 import org.daum.library.replica.cache.StoreCommand;
 import org.daum.library.replica.msg.NotifyUpdate;
 
 import java.util.HashMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jed
  * Date: 20/06/12
  * Time: 11:55
  * To change this template use File | Settings | File Templates.
  */
 public class ChangeListener implements IChangeListener
 {
     private  static ChangeListener singleton = null;
 
     public static ChangeListener getInstance()
     {
         if(singleton == null){
             singleton = new ChangeListener();
         }
         return  singleton;
     }
 
 
     HashMap<String,EventListenerList> listHashMap = new HashMap<String,EventListenerList>();
 
 
     public void addEventListener (Class zclass,PropertyChangeListener listener) {
 
         String cache = zclass.getName();
 
         if(listHashMap.containsKey(cache))
         {
             EventListenerList eventListenerList =   listHashMap.get(cache);
             eventListenerList.add(PropertyChangeListener.class,listener);
         } else
         {
             EventListenerList eventListenerList = new EventListenerList();
             eventListenerList.add(PropertyChangeListener.class,listener);
             listHashMap.put(cache, eventListenerList);
         }
 
 
     }
 
     public void removeEventListener (Class zclass,PropertyChangeListener listener) {
         String cache = zclass.getName();
 
         if(listHashMap != null)
         {
 
             EventListenerList eventListenerList =   listHashMap.get(cache);
             eventListenerList.remove(PropertyChangeListener.class, listener);
 
             if(eventListenerList.getListenerCount() == 0)
             {
                 listHashMap.remove(cache);
             }
         }
     }
 
     public void receive(Object msg)
     {
         if(msg instanceof NotifyUpdate){
             NotifyUpdate e = (NotifyUpdate)msg;
             if(listHashMap.containsKey(e.getCache()))
             {
                 PropertyChangeEvent update = new PropertyChangeEvent();
                 update.setId(e.getId());
                 if(e.getCmd().equals(StoreCommand.ADD)){
                     update.setIsadded();
                 } else  if(e.getCmd().equals(StoreCommand.UPDATE)){
                     update.setIsupdated();
 
                 } else  if(e.getCmd().equals(StoreCommand.DELETE))
                 {
                   update.setIsdeleted();
                 }
 
 
                 Object[] listeners = listHashMap.get(e.getCache()).getListenerList();
                 if(listeners != null)
                 {
                     for (int i = 0; i < listeners.length;i += 2)
                     {
                         ((PropertyChangeListener) listeners[i + 1]).update(update);
                     }
 
                 }
 
             }
 
         }
     }
 
 }
