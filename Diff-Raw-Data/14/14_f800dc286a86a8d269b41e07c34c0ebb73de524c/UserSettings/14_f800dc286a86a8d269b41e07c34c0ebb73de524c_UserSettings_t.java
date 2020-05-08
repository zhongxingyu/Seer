 /*
  * Copyright (C) 2013 UniCoPA
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package unicopa.copa.base;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Represents the personal settings of a user.
  * 
  * @author Felix Wiemuth
  */
 public class UserSettings {
 
     private Set<String> gcmKeys = new HashSet<String>();
     private boolean emailNotification = false;
    private String language = "english";
     private Map<Integer, UserEventSettings> eventSettings = new HashMap<Integer, UserEventSettings>();
 
     /**
      * The constructor for UserSettings that is used by the Mapper
      */
     public UserSettings(Set<String> gcmKeys, boolean emailNotification,
 	    String language, Map<Integer, UserEventSettings> eventSettings) {
 	this.gcmKeys = gcmKeys;
 	this.emailNotification = emailNotification;
 	this.language = language;
 	this.eventSettings = eventSettings;
     }
 
     /**
      * The default constructor for class UserSettings
      */
     public UserSettings() {
     }
 
     /**
      * Enable notifications for a Google Cloud Messaging key. The key is added
      * to the set of GCM keys. As long as a key resides in the user settings
      * notifications will be sent over the Google Cloud Messaging for Android
      * (GCM) service to the corresponding Android device.
      * 
      * @param key
      *            the GCM key to add
      * @return true if the key was not already contained in the set
      */
     public boolean addGCMKey(String key) {
 	return gcmKeys.add(key);
     }
 
     /**
      * Disable notifications for a Google Cloud Messaging key. The key is
      * removed from the set of GCM keys.
      * 
      * @param key
      *            the GCM key to be removed
      * @return true if the key was contained in the set
      */
     public boolean removeGCMKey(String key) {
 	return gcmKeys.remove(key);
     }
 
     /**
      * Get the set of GCM keys. Note that changes on the returned set are backed
      * by this UserSettings object.
      * 
      * @return
      */
     public Set<String> getGCMKeys() {
 	return gcmKeys;
     }
 
     /**
      * Enable notification by email.
      */
     public void enableEmailNotification() {
 	emailNotification = true;
     }
 
     /**
      * Disable notification by email.
      */
     public void disableEmailNotification() {
 	emailNotification = false;
     }
 
     /**
      * Check if email notification is enabled. The default setting is false.
      * 
      * @return true if email notification is enabled
      */
     public boolean isEmailNotificationEnabled() {
 	return emailNotification;
     }
 
     /**
      * Get the user interface language that is currently set. The default
      * setting is "default".
      * 
      * @return
      */
     public String getLanguage() {
 	return language;
     }
 
     /**
      * Set the user interface language.
      * 
      * @param language
      */
     public void setLanguage(String language) {
 	this.language = language;
     }
 
     /**
      * Associate the specified settings with the specified event.
      * 
      * @param eventID
      *            the ID of the event with which the specified settings will be
      *            associated
      * @param settings
      *            the settings that will be associated with the specified event
      * @return the previous settings associated with the specified event or null
      *         if there was no such association
      */
     public UserEventSettings putEventSettings(int eventID,
 	    UserEventSettings settings) {
 	return eventSettings.put(eventID, settings);
     }
 
     /**
      * Remove the settings that are associated with the specified event if it is
      * present.
      * 
      * @param eventID
      *            the ID of the event to remove the settings from
      * @return the previous settings associated with the specified event or null
      *         if there was no such association
      */
     public UserEventSettings removeEventSettings(int eventID) {
 	return eventSettings.remove(eventID);
     }
 
     /**
      * Get the settings associated with the specified event.
      * 
      * @param eventID
      *            the ID of the event to get the settings for
      * @return the event settings associated with the specified event or null if
      *         there is no such association
      */
     public UserEventSettings getEventSettings(int eventID) {
 	return eventSettings.get(eventID);
     }
 
     /**
      * Add a subscription for an event. The ID is added to the eventSettings map
      * and a new UserEventSettings Object for that eventID is created
      * 
      * @param eventID
      *            the ID of the event to subscribe to
      * @return true if the event was not already contained in the set
      */
     public boolean addSubscription(int eventID) {
 	if (eventSettings.containsKey(eventID))
 	    return false;
 
 	eventSettings.put(eventID, new UserEventSettings());
 	return true;
     }
 
     /**
      * Remove a subscribtion for an event. The ID is removed from eventSettings
      * map. The UserEventSettings for that event are removed as well
      * 
      * @param eventID
      *            the event to unsubscribe
      * @return true if there has been a subscription to the specified event
      */
     public boolean removeSubscription(int eventID) {
	if (eventSettings.containsKey(eventID)) {
            eventSettings.remove(eventID);
            return true;
        } else {
            return false;
        }
     }
 
     /**
      * Check if the user has subscribed to an event.
      * 
      * @param eventID
      *            the ID of the event to check for
      * @return true if the user has subscribed to the sepcified event
      */
     public boolean hasSubscribed(int eventID) {
 	return eventSettings.keySet().contains(eventID);
     }
 
     /**
      * Returns the Set of the subscribed EventIDs
      * 
      * @return
      */
     public Set<Integer> getSubscriptions() {
 	return eventSettings.keySet();
     }
 }
