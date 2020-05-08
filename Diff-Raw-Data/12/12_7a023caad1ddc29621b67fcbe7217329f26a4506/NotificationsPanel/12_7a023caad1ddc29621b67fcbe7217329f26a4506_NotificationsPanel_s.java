 package pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.components;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.Component;
 import org.apache.wicket.RestartResponseException;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 import org.purl.wf4ever.rosrs.client.notifications.Notification;
 
 import pl.psnc.dl.wf4ever.portal.listeners.IAjaxLinkListener;
 import pl.psnc.dl.wf4ever.portal.pages.notifications.NotificationsPage;
 import pl.psnc.dl.wf4ever.portal.ui.components.UniversalStyledAjaxButton;
 
 /**
  * An indicator of a number of notifications for this RO.
  * 
  * @author pejot
  * 
  */
 public class NotificationsPanel extends Panel implements IAjaxLinkListener {
 
     /** Logger. */
     @SuppressWarnings("unused")
     private static final Logger LOG = Logger.getLogger(NotificationsPanel.class);
 
     /** id. */
     private static final long serialVersionUID = 1L;
 
 
     /**
      * A status to notify the users that there are recent notifications.
      * 
      * @author piotrekhol
      * 
      */
     private enum RecentStatus {
         /** The latest notification has been generated today. */
         TODAY,
         /** The latest notification has been generated in the previous 7 days. */
         THIS_WEEK,
         /** No notifications in the previous 30 days. */
         DEFAULT
     }
 
 
     private RecentStatus recentStatus = RecentStatus.DEFAULT;
 
     private UniversalStyledAjaxButton button;
 
     private ResearchObject researchObject;
 
     private DateTime latest;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param researchObject
      *            research object for this indicator
      */
     public NotificationsPanel(String id, ResearchObject researchObject, IModel<List<Notification>> notificationsModel) {
         super(id, notificationsModel);
         this.researchObject = researchObject;
         Form<Void> form = new Form<Void>("form");
         add(form);
         button = new UniversalStyledAjaxButton("button", null) {
 
             /** id. */
             private static final long serialVersionUID = -7013354628009246474L;
         };
         button.add(new Label("text", new PropertyModel<>(notificationsModel, "size")));
         button.addLinkListener(this);
         button.add(new Behavior() {
 
             /** id. */
             private static final long serialVersionUID = -7063493249299862964L;
 
 
             @Override
             public void onComponentTag(Component component, ComponentTag tag) {
                 super.onComponentTag(component, tag);
                 switch (recentStatus) {
                     case TODAY:
                         tag.append("class", "btn-danger", " ");
                         break;
                     case THIS_WEEK:
                         tag.append("class", "btn-warning", " ");
                         break;
                     default:
                         break;
                 }
                 if (!component.isEnabled()) {
                     tag.append("class", "disabled", " ");
                 }
                 if (latest != null) {
                     tag.append("title", "Last message: " + DateTimeFormat.shortDateTime().print(latest), " ");
                 } else {
                     tag.append("title", "There are no messages", " ");
                 }
             }
         });
         form.add(button);
     }
 
 
     @Override
     protected void onConfigure() {
         @SuppressWarnings("unchecked")
         List<Notification> notifications = (List<Notification>) getDefaultModelObject();
         if (!notifications.isEmpty()) {
             latest = notifications.get(notifications.size() - 1).getPublished();
             DateTime now = DateTime.now();
             if (now.minusHours(24).isBefore(latest)) {
                 recentStatus = RecentStatus.TODAY;
             } else if (now.minusDays(7).isBefore(latest)) {
                 recentStatus = RecentStatus.THIS_WEEK;
             } else {
                 recentStatus = RecentStatus.DEFAULT;
             }
         }
         button.setEnabled(!notifications.isEmpty());
         super.onConfigure();
     }
 
 
     @Override
     public void onAjaxLinkClicked(Object source, AjaxRequestTarget target) {
         PageParameters params = new PageParameters();
         params.add("ro", researchObject.getUri());
         throw new RestartResponseException(NotificationsPage.class, params);
     }
 }
