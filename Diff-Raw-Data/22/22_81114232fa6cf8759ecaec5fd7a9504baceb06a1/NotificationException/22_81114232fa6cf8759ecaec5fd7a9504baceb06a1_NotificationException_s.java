 package com.atlassian.plugin.event;
 
 import com.atlassian.plugin.PluginException;
 
 import java.util.List;
 import java.util.Collections;
 
 /**
  * This is used to wrap one or more exceptions thrown by Plugin Event Listeners on receiving an event.
  *
  * <p> {@link #getAllCauses()} will return a list with all the exceptions that were thrown by the listeners.
  * <p> {@link #getCause()} will return just the first Exception in the list.
  *
 * @since v2.3
  */
 public class NotificationException extends PluginException
 {
     private final List<Throwable> allCauses;
 
     public NotificationException(final Throwable cause)
     {
         super(cause);
         allCauses = Collections.singletonList(cause);
     }
 
     /**
      * Constructs a NotificationException with a List of the Exceptions that were thrown by the Listeners.
      * @param causes all Exceptions that were thrown by the Listeners.
      * @throws NullPointerException If a null List is passed.
      * @throws IndexOutOfBoundsException If an empty List is passed.
      */
     public NotificationException(final List<Throwable> causes)
     {
         //noinspection ThrowableResultOfMethodCallIgnored
         super(causes.get(0));
         this.allCauses = Collections.unmodifiableList(causes);
     }
 
     public List<Throwable> getAllCauses()
     {
         return allCauses;
     }
 }
