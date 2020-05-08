 /*
  * Copyright 2012 INRIA
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mymed.controller.core.manager.pubsub.v2;
 
 import static com.mymed.utils.MatchMaking.extractApplication;
 import static com.mymed.utils.MatchMaking.extractNamespace;
 import static com.mymed.utils.MiscUtils.encode;
 import static java.util.Arrays.asList;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.AbstractManager;
 import com.mymed.controller.core.manager.mailtemplates.MailTemplate;
 import com.mymed.controller.core.manager.mailtemplates.MailTemplateManager;
 import com.mymed.controller.core.manager.profile.ProfileManager;
 import com.mymed.controller.core.manager.storage.IStorageManager;
 import com.mymed.controller.core.manager.storage.v2.StorageManager;
 import com.mymed.model.data.user.MUserBean;
 import com.mymed.utils.mail.Mail;
 import com.mymed.utils.mail.MailMessage;
 import com.mymed.utils.mail.SubscribeMailSession;
 
 
 /**
  * The pub/sub mechanism manager.
  * 
  */
 public class PubSubManager extends AbstractManager implements IPubSubManager {
 	
 	 /**
      * The application controller super column.
      */
     protected static final String SC_APPLICATION_CONTROLLER = COLUMNS.get("column.sc.application.controller");
 
     /**
      * The data table.
      */
     protected static final String CF_DATA = COLUMNS.get("column.cf.data");
 
     /**
      * The subscribees (users subscribed to a predicate) column family.
      */
     protected static final String CF_SUBSCRIBEES = COLUMNS.get("column.cf.subscribees");
     
 
     /**
      * The subscribers (predicates subscribed by a user) column family.
      */
     protected static final String CF_SUBSCRIBERS = COLUMNS.get("column.cf.subscribers");
     
     protected ProfileManager profileManager;
     protected MailTemplateManager mailTemplateManager;
     
     /**
      * Default constructor.
      * @throws InternalBackEndException 
      */
     public PubSubManager() 
             throws InternalBackEndException 
     {
         this(new StorageManager());
     }
 
     public PubSubManager(final IStorageManager storageManager) 
             throws InternalBackEndException 
     {
     	super(storageManager);
         profileManager = new ProfileManager(storageManager);
         mailTemplateManager = new MailTemplateManager(storageManager);
     }
 
     /**
      * Publish mechanism.
      * 
      * @see IPubSubManager#create(String, String, MUserBean)
      */
   
 	/* v2 create index */
 	public final void create(String application,
 			final String predicate,
 			final String id,
 			final Map<String, String> metadata)
 					throws InternalBackEndException, IOBackEndException {
 
 		storageManager.insertSuperSliceStr(
 				SC_APPLICATION_CONTROLLER,
 				application	+ predicate,
 				id,
 				metadata);
 
 	}
     
 	/* v2 create data */
 	public final void create(
 			String application,
 			final String subPredicate,
 			final Map<String, String> dataList)
 					throws InternalBackEndException, IOBackEndException {
 
 		/*
 		 * stores data
 		 * 
 		 * in CF Data
 		 */
 		storageManager.insertSliceStr(
 				CF_DATA,
 				application + subPredicate,
 				dataList);
 
 	}
     
     @Override
     public void create(
             final String application, 
             final String predicate, 
             final String subscriber,
             final String desc)
     throws InternalBackEndException, IOBackEndException 
     {
         // STORE A NEW ENTRY IN THE UserList (SubscriberList)
         storageManager.insertColumn(
                 CF_SUBSCRIBEES, 
                 application + predicate, 
                 subscriber, 
                 encode(desc));
         
         storageManager.insertColumn(
                 CF_SUBSCRIBERS, 
                 application + subscriber, 
                 (predicate.length() == 0) ? "_" : predicate, 
                 encode(desc));
         /* _ temp prefix to avoid empty predicates (for global namespace subscriptions) */
     }
     
     /** read details */
 	@Override
 	public Map<String, String> read(
 			final String application,
 			final String predicate)
 					throws InternalBackEndException, IOBackEndException {
 
 		
 		final Map<String, String> map = storageManager.selectAllStr(CF_DATA, application + predicate);
 		
 		return map;
 	}
 	
 	/** read detail 1-item*/
 	@Override
 	public String read(
 			final String application,
 			final String predicate,
 			final String name )
 					throws InternalBackEndException, IOBackEndException {
 
 		LOGGER.info("read {} {}", application, predicate +" name:" +name);
 		final Map<String, String> map = read(application, predicate);
 		return map.get(name);
 	}
 	
 	/** read results */
 	@Override
 	public final Map<String, Map<String, String>> read(
 			final String application, final List<String> predicate,
 			final String start, final String finish)
 			throws InternalBackEndException, IOBackEndException, UnsupportedEncodingException {
 		final Map<String, Map<String, String>> resMap = new TreeMap<String, Map<String, String>>();
 		resMap.putAll(storageManager.multiSelectList(SC_APPLICATION_CONTROLLER,
 				predicate, start, finish));
 		return resMap;
 	}
 	
 	/** reads Subscriptions for a user*/
 	@Override
 	public Map<String, String> read(String app_user)
 			throws InternalBackEndException, IOBackEndException {
 
         final Map<String, String> map = storageManager.selectAllStr(CF_SUBSCRIBERS, app_user);
         LOGGER.info("__"+app_user +" is subscribed to {}", map);
         return map;
 	}
 	
 	public String readSubEntry(String app_user, String key)
 			throws InternalBackEndException, IOBackEndException {
 
 		final Map<String, String> map = storageManager.selectAllStr(CF_SUBSCRIBERS, app_user);
         return map.get(key);
 	}
 	
     
     /* v2 deletes */
 	public final void delete(
 	        final String application, 
 	        final String subPredicate)
 			        throws InternalBackEndException, IOBackEndException 
 	{
 		storageManager.removeAll(CF_DATA, application + subPredicate);
 	}
     
 	@Override
     public final void delete(
             final String application, 
             final String predicate, 
             final String subPredicate,
             final String publisherID) 
     		        throws InternalBackEndException, IOBackEndException 
     {
 		storageManager.removeSuperColumn(SC_APPLICATION_CONTROLLER, application + predicate, subPredicate);
		delete(application, subPredicate);
     }
 	
 	@Override
     public final void delete(
             final String application, 
             final String predicate, 
             final String user)
                     throws InternalBackEndException, IOBackEndException 
     {
         // Remove subscriber member from subsribers list
         storageManager.removeColumn(CF_SUBSCRIBERS, application + user, (predicate.length() == 0) ? "_" : predicate);
         // Remove subscriber member from predicates subscribed list
         storageManager.removeColumn(CF_SUBSCRIBEES, application + predicate, user);
     }
 	
 	
 	
 
 	
 	public void sendEmailsToSubscribers(  
             String application,          
             String predicate,
             Map<String, String> details,
             MUserBean publisher) 
     {
         
         // Built list of recipients
         final List<MUserBean> recipients = new ArrayList<MUserBean>();
         {
             final Map<String, String> subscribers = storageManager.selectAllStr(CF_SUBSCRIBEES, application + predicate);
             for (final Entry<String, String> entry : subscribers.entrySet()) {
                 MUserBean recipient = null;
                 try {
                     recipient = profileManager.read(entry.getKey());
                 } catch (IOBackEndException e) {}
                 if (recipient != null) {
                     recipients.add(recipient);
                 }
             }
         }
        
         // Split application ID
         String applicationID = extractApplication(application);
         String namespace = extractNamespace(application);
         
         // Prepare HashMap of object for FreeMarker template
         HashMap<String, Object> data = new HashMap<String, Object>(); 
         
         // Build URL of the application
         String url = getServerProtocol() + getServerURI() + "/";
         //if (applicationID != null) {
             // can we rename the folder myEuroCINAdmin in myEuroCIN_ADMIN to skip below hack
             // url += "/application/" + (applicationID.equals("myEuroCIN_ADMIN") ? "myEuroCINAdmin" : applicationID);
         //}
 
         // Set data map
         data.put("base_url", url);
         data.put("application", applicationID);
         data.put("publication", details);
         data.put("publisher", publisher );
 
         // Loop on recipients
         for (MUserBean recipient : recipients) {
             
             // Update the current recipient in the data map
             data.put("recipient", recipient);
             
             // Get the prefered language of the user
             String language = recipient.getLang();
             
             // Get the mail template from the manager
             
             MailTemplate template = this.mailTemplateManager.getTemplate(
                     applicationID, 
                     namespace, 
                     language);
             
             // Render the template
             String subject = template.renderSubject(data);
             String body = template.renderBody(data);
             
             // Create the mail
             final MailMessage message = new MailMessage();
             message.setSubject(subject);
             message.setRecipients(asList(recipient.getEmail()));
             message.setText(body);
 
             // Send it
             final Mail mail = new Mail(
                     message, 
                     SubscribeMailSession.getInstance());
             mail.send();
             
             LOGGER.info(String.format("Mail sent to '%s' with title '%s' for predicate '%s'", 
                     recipient.getEmail(), 
                     subject,
                     predicate));
             
         } // End of loop on recipients 
     }
 	
 }
