 package fi.jawsy.jawwa.zk.gritter;
 
 import java.util.Map;
 
 import lombok.AccessLevel;
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 import lombok.RequiredArgsConstructor;
 import lombok.ToString;
 
 import org.zkoss.json.JSONAware;
 import org.zkoss.json.JSONObject;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.util.Clients;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Strings;
 import com.google.common.collect.Maps;
 
 /**
  * Non-intrusive notifications for <a href="http://www.zkoss.org">ZK</a> with <a
  * href="https://github.com/jboesch/Gritter">Gritter</a>.
  */
 public final class Gritter {
 
     /**
      * Default fade-out time for notifications.
      */
     public static final int DEFAULT_TIME = 6000;
 
     /**
      * Private constructor
      */
     private Gritter() {
     }
 
     /**
      * Represents a single Gritter notification.
      */
     @Data
     @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Notification implements JSONAware {
         /**
          * Notification title.
          */
         public final String title;
         /**
          * Notification text.
          */
         public final String text;
         /**
          * (Optional) Image source URL
          */
         public final String image;
         /**
          * Is the notification sticky?
          */
         public final boolean sticky;
         /**
          * Time before notification automatically fades out
          */
         public final int time;
         /**
          * (Optional) CSS class for the notification
          */
         public final String sclass;
 
         @Override
         public String toJSONString() {
             Map<String, Object> json = Maps.newLinkedHashMap();
 
             json.put("title", title);
             json.put("text", text);
 
             if (!Strings.isNullOrEmpty(image))
                 json.put("image", Executions.encodeURL(image));
             if (sticky)
                 json.put("sticky", sticky);
             if (time != DEFAULT_TIME)
                 json.put("time", time);
             if (!Strings.isNullOrEmpty(sclass))
                 json.put("class_name", sclass);
 
             return JSONObject.toJSONString(json);
         }
     }
 
     /**
      * Mutable builder class for notifications.
      */
     @EqualsAndHashCode
     @ToString
     public static class NotificationBuilder {
         private String title;
         private String text;
         private String image;
         private boolean sticky;
         private int time = DEFAULT_TIME;
         private String sclass;
 
         /**
          * Private constructor
          */
         private NotificationBuilder() {
         }
 
         /**
          * Sets the notification title
          * 
          * @param title
          *            non-null and non-empty string
          * @throws NullPointerException
          *             if title is null
          * @throws IllegalArgumentException
          *             if title is empty
          * @return this builder
          */
         public NotificationBuilder withTitle(String title) {
             Preconditions.checkNotNull(title, "title cannot be null");
             Preconditions.checkArgument(!Strings.isNullOrEmpty(title), "title cannot be empty");
             this.title = title;
             return this;
         }
 
         /**
          * Sets the notification text
          * 
          * @param text
          *            non-null and non-empty string
          * @throws NullPointerException
          *             if text is null
          * @throws IllegalArgumentException
          *             if text is empty
          * @return this builder
          */
         public NotificationBuilder withText(String text) {
             Preconditions.checkNotNull(text, "text cannot be null");
             Preconditions.checkArgument(!Strings.isNullOrEmpty(text), "text cannot be empty");
             this.text = text;
             return this;
         }
 
         /**
          * Sets the notification image.
          * 
          * @param image
          *            url as string
          * @return this builder
          */
         public NotificationBuilder withImage(String image) {
             this.image = image;
             return this;
         }
 
         /**
          * Sets whether the notification should auto-fade or stick until it is removed programmatically or by the user.
          * 
          * @param sticky
          *            sticky flag
          * @return this builder
          */
         public NotificationBuilder withSticky(boolean sticky) {
             this.sticky = sticky;
             return this;
         }
 
         /**
          * Sets the auto-fade timeout.
          * 
          * @param time
          *            positive integer
          * @throws IllegalArgumentException
          *             if time is negative
          * @return this builder
          */
         public NotificationBuilder withTime(int time) {
             Preconditions.checkArgument(time >= 0, "time must be positive");
             this.time = time;
             return this;
         }
 
         /**
          * Sets the CSS class.
          * 
          * @param sclass
          *            class string
          * @return this builder
          */
         public NotificationBuilder withSclass(String sclass) {
             this.sclass = sclass;
             return this;
         }
 
         /**
          * Builds the notification. Title and text must be set prior building.
          * 
          * @throws IllegalStateException
          *             if title or text are not set
          * @return built notification
          */
         public Notification build() {
             Preconditions.checkState(!Strings.isNullOrEmpty(title), "title has to be set");
             Preconditions.checkState(!Strings.isNullOrEmpty(text), "text has to be set");
             return new Notification(title, text, image, sticky, time, sclass);
         }
     }
 
     /**
      * Creates a notification builder.
      * 
      * @return new notification builder
      */
     public static NotificationBuilder notification() {
         return new NotificationBuilder();
     }
 
     /**
     * Shows a notification to the screen.
      * 
      * Must be called in a Servlet thread with an active ZK execution.
      * 
      * @param notification
      *            notification to be shown
      */
     public static void show(Notification notification) {
         Preconditions.checkNotNull(notification, "notification cannot be null");
         Clients.evalJavaScript("jq.gritter.add(" + notification.toJSONString() + ")");
     }
 
     /**
     * Removes all notifications from the screen.
      * 
      * Must be called in a Servlet thread with an active ZK execution.
      */
     public static void removeAll() {
         Clients.evalJavaScript("jq.gritter.removeAll()");
     }
 
 }
