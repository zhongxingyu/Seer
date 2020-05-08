 /**
  * Licensed to the Austrian Association for Software Tool Integration (AASTI)
  * under one or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information regarding copyright
  * ownership. The AASTI licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.connector.gcontacts.internal;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openengsb.connector.gcontacts.internal.misc.ContactConverter;
 import org.openengsb.core.api.AliveState;
 import org.openengsb.core.api.DomainMethodExecutionException;
 import org.openengsb.core.common.AbstractOpenEngSBService;
 import org.openengsb.domain.contact.ContactDomain;
 import org.openengsb.domain.contact.models.Contact;
 import org.openengsb.domain.contact.models.Location;
 
 import com.google.gdata.client.contacts.ContactQuery;
 import com.google.gdata.client.contacts.ContactsService;
 import com.google.gdata.data.contacts.ContactEntry;
 import com.google.gdata.data.contacts.ContactFeed;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 
 public class GcontactsServiceImpl extends AbstractOpenEngSBService implements ContactDomain {
 
     private static Log log = LogFactory.getLog(GcontactsServiceImpl.class);
 
     private AliveState state = AliveState.DISCONNECTED;
     private String googleUser;
     private String googlePassword;
 
     private ContactsService service;
 
     public GcontactsServiceImpl(String id) {
         super(id);
     }
 
     @Override
     public String createContact(Contact contact) {
         try {
             login();
 
             ContactEntry entry = ContactConverter.convertContactToContactEntry(contact);
             URL postUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
             entry = service.insert(postUrl, entry);
             log.info("Successfully created contact " + entry.getId());
             contact.setId(entry.getId());
             return entry.getId();
         } catch (MalformedURLException e) {
             throw new DomainMethodExecutionException("unknown type of URL", e);
         } catch (IOException e) {
             throw new DomainMethodExecutionException("unable to connect to the insert URL", e);
         } catch (ServiceException e) {
             throw new DomainMethodExecutionException("unable to insert a new contact", e);
         } finally {
             this.state = AliveState.DISCONNECTED;
         }
     }
 
     @Override
     public void updateContact(Contact contact) {
         login();
         ContactEntry entry = getContactEntry(contact.getId());
         ContactConverter.extendContactEntryWithContact(entry, contact);
         try {
             URL editUrl = new URL(entry.getEditLink().getHref());
             service.update(editUrl, entry);
         } catch (MalformedURLException e) {
             throw new DomainMethodExecutionException("unknown type of URL", e);
         } catch (IOException e) {
             throw new DomainMethodExecutionException("unable to connect to the google server", e);
         } catch (ServiceException e) {
             throw new DomainMethodExecutionException("unable to update the contact", e);
         } finally {
             this.state = AliveState.DISCONNECTED;
         }
     }
 
     @Override
     public void deleteContact(String id) {
         login();
         ContactEntry entry = getContactEntry(id);
         try {
             entry.delete();
         } catch (IOException e) {
             throw new DomainMethodExecutionException("unable to connect to the delete URL", e);
         } catch (ServiceException e) {
             throw new DomainMethodExecutionException("unable to delete the contact with the given id", e);
         } finally {
             this.state = AliveState.DISCONNECTED;
         }
     }
 
     @Override
     public Contact loadContact(String id) {
         login();
         ContactEntry entry = getContactEntry(id);
         return ContactConverter.convertContactEntryToContact(entry);
     }
 
     /**
      * it works for one argument only. That means if a name is set, homepage, location, ... will be ignored. location
      * works only partwise. If all values for location are set, it often don't work. Try it with less values then. e.g.
      * only address. date works unfortunately not at all. I assume there are problems with the internationalisation of
      * the format because only full text searches are working (at least until google gives us better oppurtunities)
      */
     @Override
     public ArrayList<Contact> retrieveContacts(String id, String name, String homepage, Location location, Date date,
             String comment) {
         ArrayList<Contact> contacts = new ArrayList<Contact>();
 
         String querytext = null;
 
         if (id != null) {
             contacts.add(loadContact(id));
             return contacts;
         } else if (name != null) {
             querytext = name;
         } else if (homepage != null) {
             querytext = homepage;
         } else if (location != null) {
             StringBuilder builder = new StringBuilder();
             if (location.getAddress() != null) {
                 builder.append(location.getAddress() + "\n");
             }
             if (location.getCity() != null) {
                 builder.append(location.getCity());
             }
             if (builder.length() != 0
                     && (location.getState() != null || location.getZip() != null || location.getCountry() != null)) {
                 builder.append(", ");
             }
             if (location.getState() != null) {
                 builder.append(location.getState() + " ");
             }
             if (location.getZip() != null) {
                 builder.append(location.getZip() + "\n");
             }
             if (location.getCountry() != null) {
                 builder.append(location.getCountry());
             }
             querytext = builder.toString();
         } else if (comment != null) {
             querytext = comment;
         } else {
             log.info("No parameters were set. Empty list will be returned");
             return contacts;
         }
 
         try {
             URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
             ContactQuery myQuery = new ContactQuery(feedUrl);
 
             myQuery.setFullTextQuery(querytext);
             ContactFeed resultFeed = service.query(myQuery, ContactFeed.class);
             for (ContactEntry entry : resultFeed.getEntries()) {
                 contacts.add(ContactConverter.convertContactEntryToContact(entry));
             }
             return contacts;
         } catch (MalformedURLException e) {
             throw new DomainMethodExecutionException("unknown type of URL", e);
         } catch (IOException e) {
             throw new DomainMethodExecutionException("unable to connect to the google server", e);
         } catch (ServiceException e) {
             throw new DomainMethodExecutionException("unable to retrieve contacts", e);
         }
     }
 
     /**
      * retrieves one contact by id
      */
     private ContactEntry getContactEntry(String id) {
         try {
 
             ContactEntry entry = (ContactEntry) service.getEntry(new URL(id), ContactEntry.class);
             return entry;
         } catch (MalformedURLException e) {
            throw new DomainMethodExecutionException("invalid id, id must be an url to the element on the calendar", e);
         } catch (IOException e) {
             throw new DomainMethodExecutionException("unable to connect to the google server", e);
         } catch (ServiceException e) {
             throw new DomainMethodExecutionException("unable to retrieve the appointment", e);
         }
     }
 
     @Override
     public AliveState getAliveState() {
         return this.state;
     }
 
     /**
      * make the login action to the google server
      */
     private void login() {
         try {
             service = new ContactsService("OPENENGSB");
             service.setUserCredentials(googleUser, googlePassword);
             this.state = AliveState.ONLINE;
         } catch (AuthenticationException e) {
             throw new DomainMethodExecutionException(
                 "unable to authenticate at google server, maybe wrong username and/or password?", e);
         }
     }
 
     public String getGooglePassword() {
         return googlePassword;
     }
 
     public void setGooglePassword(String googlePassword) {
         this.googlePassword = googlePassword;
     }
 
     public String getGoogleUser() {
         return googleUser;
     }
 
     public void setGoogleUser(String googleUser) {
         this.googleUser = googleUser;
     }
 }
