 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003 - 2005  Lars Khne
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
 
 package net.sf.clirr.core;
 
 import java.util.Locale;
 import java.util.Iterator;
 import java.util.Collection;
 import java.util.ResourceBundle;
 
 /**
  * Class which is capable of translating a Message object into a localised
  * string.
  */
 public final class MessageTranslator
 {
     /**
      * The default base name of the resource bundle from which message
      * descriptions are read.
      */
     public static final String DFLT_RESOURCE_NAME = EventMessages.class.getName();
 
    private Locale locale;
     private String resourceName = DFLT_RESOURCE_NAME;
     private ResourceBundle messageText;
 
     /**
     * Translate using the system default locale.
      */
     public MessageTranslator()
     {
    	this(Locale.getDefault());
    }

    public MessageTranslator(Locale locale)
    {
    	this.locale = locale;
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
      * Define the base name of the properties file that message
      * translations are to be read from.
      */
     public void setResourceName(String resourceName)
     {
         this.resourceName = resourceName;
         this.messageText = null;
     }
 
     /**
      * Verify that the resource bundle for the currently set locale has
      * a translation string available for every message object in the provided
      * collection. This method is expected to be called from the unit tests,
      * so that if a developer adds a new message the unit tests will fail until
      * translations are also available for that new message.
      * <p>
      * @throws java.util.MissingResourceException if there is a registered
      * message for which no description is present in the current locale's
      * resources.
      */
     public void checkComplete(Collection messages)
     {
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
      * @throws java.util.MissingResourceException if there is no entry in the
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
             messageText = ResourceBundle.getBundle(resourceName, locale);
         }
 
         return messageText.getString("m" + msg.getId());
     }
 }
