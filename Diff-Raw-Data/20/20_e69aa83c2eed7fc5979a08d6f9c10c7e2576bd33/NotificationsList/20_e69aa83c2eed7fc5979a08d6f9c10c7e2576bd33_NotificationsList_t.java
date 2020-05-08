 package pl.psnc.dl.wf4ever.portal.pages.ro.notifications;
 
 import java.util.List;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxEventBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.PropertyListView;
 import org.apache.wicket.model.IModel;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.purl.wf4ever.rosrs.client.notifications.Notification;
 
 import pl.psnc.dl.wf4ever.portal.components.EventPanel;
 import pl.psnc.dl.wf4ever.portal.events.NotificationsLoadedEvent;
 import pl.psnc.dl.wf4ever.portal.events.ResourceSelectedEvent;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 
 /**
  * A list of notification headers.
  * 
  * @author piotrekhol
  * 
  */
 public class NotificationsList extends EventPanel {
 
     /** id. */
     private static final long serialVersionUID = -2527527943968289889L;
 
     /** formatter for showing only the hour. */
    private transient DateTimeFormatter hourFormatter;
 
     /** formatter for showing the full date. */
    private transient DateTimeFormatter dateFormatter;
 
     /** The list of headers. */
     private PropertyListView<Notification> list;
 
     /** The selected notification. */
     private IModel<Notification> selectedNotificationModel;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param notificationsModel
      *            list of notifications
      * @param selectedNotificationModel
      *            the model for setting the selected notification
      * @param eventBusModel
      *            event bus for posting the user clicks
      */
     public NotificationsList(String id, IModel<? extends List<Notification>> notificationsModel,
             IModel<Notification> selectedNotificationModel, IModel<EventBus> eventBusModel) {
         super(id, selectedNotificationModel, eventBusModel);
         this.selectedNotificationModel = selectedNotificationModel;
         list = new NotificationsPropertyListView("list", notificationsModel);
         list.setReuseItems(true);
         add(list);
         setOutputMarkupId(true);
     }
 
 
     /**
      * Return the formatted date time, the hour if today, the date if earlier.
      * 
      * @param dateTime
      *            the date time to format
      * @return the formatted date time
      */
     private String formatDateTime(DateTime dateTime) {
         if ((new LocalDate(dateTime)).equals(new LocalDate())) {
            if (hourFormatter == null) {
                hourFormatter = new DateTimeFormatterBuilder().appendHourOfDay(2).appendLiteral(":")
                        .appendMinuteOfHour(2).toFormatter();
            }
             return hourFormatter.print(dateTime);
         } else {
            if (dateFormatter == null) {
                dateFormatter = new DateTimeFormatterBuilder().appendDayOfMonth(1).appendLiteral("/")
                        .appendMonthOfYear(1).appendLiteral("/").appendYear(2, 2).toFormatter();
            }
             return dateFormatter.print(dateTime);
         }
     }
 
 
     /**
      * Refresh when the notifications are loaded.
      * 
      * @param event
      *            the trigger
      */
     @Subscribe
     public void onNotificationsLoaded(NotificationsLoadedEvent event) {
         event.getTarget().add(this);
     }
 
 
     /**
      * The notifications header list. This is an internal class because the parent panel contains additional HTML.
      * 
      * @author piotrekhol
      * 
      */
     private final class NotificationsPropertyListView extends PropertyListView<Notification> {
 
         /** id. */
         private static final long serialVersionUID = 3207517289844703505L;
 
 
         /**
          * Constructor.
          * 
          * @param id
          *            wicket id
          * @param notificationsModel
          *            notifications list
          */
        private NotificationsPropertyListView(String id, IModel<? extends List<Notification>> notificationsModel) {
             super(id, notificationsModel);
         }
 
 
         @Override
         protected void populateItem(final ListItem<Notification> item) {
             item.add(new Label("title"));
             item.add(new Label("published", formatDateTime(item.getModelObject().getPublished())));
             item.add(new Label("sourceName"));
             item.add(new AjaxEventBehavior("onclick") {
 
                 /** id. */
                 private static final long serialVersionUID = -7851716059681985652L;
 
 
                 @Override
                 protected void onEvent(AjaxRequestTarget target) {
                     selectedNotificationModel.setObject(item.getModelObject());
                     target.add(NotificationsList.this);
                     eventBusModel.getObject().post(new ResourceSelectedEvent(target));
                 }
             });
             item.add(new Behavior() {
 
                 /** id. */
                 private static final long serialVersionUID = -5842150372531742905L;
 
 
                 @SuppressWarnings("unchecked")
                 @Override
                 public void onComponentTag(Component component, ComponentTag tag) {
                     super.onComponentTag(component, tag);
                     if (((ListItem<Notification>) component).getModelObject().equals(
                         selectedNotificationModel.getObject())) {
                         //                        System.out.println("Selected item: " + selectedItem);
                         tag.append("class", "selected", " ");
                     }
                 }
             });
         }
     }
 
 }
