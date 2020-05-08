 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003 - 2004  Lars Khne
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //////////////////////////////////////////////////////////////////////////////
 
 package net.sf.clirr.event;
 
 import java.util.Locale;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.ResourceBundle;
 
 /**
  * Class which manages API Difference messages, including expanding message
  * codes into strings and descriptions.
  */
 public final class MessageManager
 {
     /**
      * The base name of the resource bundle from which message descriptions
      * are read.
      */
     public static final String RESOURCE_NAME = "event-messages";
 
     private static MessageManager instance;
     private ArrayList messages = new ArrayList();
     private Locale locale;
     private ResourceBundle messageText;
 
     /**
      * Utility class to sort messages by their numeric ids.
      */
     private static class MessageComparator implements Comparator
     {
         public int compare(Object o1, Object o2)
         {
             Message m1 = (Message) o1;
             Message m2 = (Message) o2;
             return m1.getId() - m2.getId();
         }
     }
 
     /**
      * This is a singleton class; to get an instance of this class, use
      * the getInstance method.
      */
     private MessageManager()
     {
     }
 
     /**
      * Return the singleton instance of this class.
      */
     public static MessageManager getInstance()
     {
         if (instance == null)
         {
             instance = new MessageManager();
         }
         return instance;
     }
 
     /**
      * Add a message to the list of known messages.
      */
     public void addMessage(Message msg)
     {
         messages.add(msg);
     }
 
     /**
      * Define the local language etc. Future calls to the getDesc method
      * will attempt to use a properties file which is appropriate to that
      * locale to look the message descriptions up in.
      * <p>
      * @param locale may be a valid Locale object, or null to indicate
      * that the default locale is to be used.
      */
     public void setLocale(Locale locale)
     {
         if (locale == null)
         {
             locale = Locale.getDefault();
         }
         this.locale = locale;
         this.messageText = null;
     }
 
     /**
      * Verify that the list of known messages contains no two objects
      * with the same numeric message id. This method is expected to be
      * called from the unit tests, so that if a developer adds a new
      * message and accidentally uses the message id of an existing
      * message object, then this will be reported as an error.
      * <p>
      * @throws IllegalArgumentException if any duplicate id is found.
      */
     public void checkUnique()
     {
         java.util.Collections.sort(messages, new MessageComparator());
         int lastId = -1;
         for (Iterator i = messages.iterator(); i.hasNext();)
         {
             // check for any duplicates
             Message m = (Message) i.next();
             int currId = m.getId();
             if (currId <= lastId)
             {
                 throw new IllegalArgumentException(
                     "Message id [" + currId + "] is not unique.");
             }
         }
     }
 
     /**
      * Verify that the resource bundle for the currently set locale has
      * a translation string available for every registered message object.
      * This method is expected to be called from the unit tests, so that
      * if a developer adds a new message the unit tests will fail until
      * translations are also available for that new message.
      * <p>
      * @throws java.util.MissingResourceException if there is a registered
      * message for which no description is present in the current locale's
      * resources.
      */
     public void checkComplete()
     {
         java.util.Collections.sort(messages, new MessageComparator());
         for (Iterator i = messages.iterator(); i.hasNext();)
         {
             Message m = (Message) i.next();
             getDesc(m);
         }
     }
 
     /**
      * Given a Message object (containing a unique message id), look up
      * that id in the appropriate resource bundle (properties file) for
      * the set locale and return the text string associated with that
      * message id.
      * <p>
      * Message ids in the properties file should be prefixed with an 'm',
      * eg "m1000", "m5003".
      * <p>
     * @throws MissingResourceException if there is no entry in the
      * message translation resource bundle for the specified message.
      */
     public String getDesc(Message msg)
     {
         // load resource bundle
         if (locale == null)
         {
             locale = Locale.getDefault();
         }
 
         if (messageText == null)
         {
             messageText = ResourceBundle.getBundle(RESOURCE_NAME, locale);
         }
 
         return messageText.getString("m" + msg.getId());
     }
 }
