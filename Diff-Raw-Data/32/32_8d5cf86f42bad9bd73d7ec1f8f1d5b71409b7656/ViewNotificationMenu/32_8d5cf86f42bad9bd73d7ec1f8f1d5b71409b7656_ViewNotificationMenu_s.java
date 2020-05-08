 /**
  * 
  */
 package org.iplantc.de.client.notifications.views;
 
 import org.iplantc.core.uicommons.client.events.EventBus;
 
 import com.sencha.gxt.widget.core.client.event.ShowEvent;
 import com.sencha.gxt.widget.core.client.event.ShowEvent.ShowHandler;
 import com.sencha.gxt.widget.core.client.menu.Menu;
 
 /**
  * @author sriram
  * 
  */
 public class ViewNotificationMenu extends Menu {
 
     private final NotificationListView view;
 
     public ViewNotificationMenu(EventBus eventBus) {
         view = new NotificationListView(eventBus);
         add(view.asWidget());
         addShowHandler(new ShowHandler() {
 
             @Override
             public void onShow(ShowEvent event) {
                 view.highlightNewNotifications();
                 view.markAsSeen();
                 view.updateNotificationLink();
 
             }
         });
     }
 
     public void fetchUnseenNotifications() {
         view.fetchUnseenNotifications();
 
     }
 
     public void setUnseenCount(int new_count) {
         view.setUnseenCount(new_count);
     }
 
 }
