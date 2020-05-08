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
 package com.mymed.controller.core.manager.pubsub;
 
 import static com.mymed.utils.GsonUtils.gson;
 import static com.mymed.utils.MiscUtils.extractApplication;
 import static com.mymed.utils.MiscUtils.extractNamespace;
 import static com.mymed.utils.MiscUtils.decode;
 import static com.mymed.utils.MiscUtils.encode;
 import static java.util.Arrays.asList;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.AbstractManager;
 import com.mymed.controller.core.manager.mailtemplates.MailTemplate;
 import com.mymed.controller.core.manager.mailtemplates.MailTemplateManager;
 import com.mymed.controller.core.manager.profile.ProfileManager;
 import com.mymed.controller.core.manager.storage.IStorageManager;
 import com.mymed.controller.core.manager.storage.StorageManager;
 import com.mymed.model.data.application.MDataBean;
 import com.mymed.model.data.user.MUserBean;
 import com.mymed.utils.MConverter;
 import com.mymed.utils.MiscUtils;
 import com.mymed.utils.mail.Mail;
 import com.mymed.utils.mail.MailMessage;
 import com.mymed.utils.mail.SubscribeMailSession;
 
 /**
  * The pub/sub mechanism manager.
  * 
  * @author lvanni
  */
 public class PubSubManager extends AbstractManager implements IPubSubManager {
 
 	protected static final String APPLICATION_CONTROLLER_ID = "applicationControllerID";
 	/**
 	 * The default publish prefix.
 	 */
 	protected static final String PUBLISHER_PREFIX = "PUBLISH_";
 
 	/**
 	 * The default subscribe prefix.
 	 */
 	protected static final String SUBSCRIBER_PREFIX = "SUBSCRIBE_";
 	protected static final String MEMBER_LIST_KEY = "memberList";
 	protected static final String DATA_ARG = "data";
 	protected static final String BEGIN_ARG = "begin";
 	protected static final String END_ARG = "end";
 
 	/**
 	 * The application controller super column.
 	 */
 	protected static final String SC_APPLICATION_CONTROLLER = COLUMNS.get("column.sc.application.controller");
 
 	/**
 	 * The application model super column.
 	 */
 	private static final String SC_APPLICATION_MODEL = COLUMNS.get("column.sc.application.model");
 
 	/**
 	 * The data list super column.
 	 */
 	protected static final String SC_DATA_LIST = COLUMNS.get("column.sc.data.list");
 
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
 	public PubSubManager() throws InternalBackEndException {
 		this(new StorageManager());
 	}
 
 	public PubSubManager(final IStorageManager storageManager) throws InternalBackEndException {
 		super(storageManager);
 		profileManager = new ProfileManager(storageManager);
 		mailTemplateManager = new MailTemplateManager(storageManager);
 	}
 
 	/**
 	 * Publish mechanism.
 	 * 
 	 * @see IPubSubManager#create(String, String, MUserBean)
 	 */
 	@Override public void create(
 			String application, 
 			final String predicate, 
 			final String subPredicate,
 			final MUserBean publisher, 
 			final List<MDataBean> dataList, 
 			final List<MDataBean> predicateList) throws InternalBackEndException, IOBackEndException 
 			{
 		// STORE THE PUBLISHER
 		final Map<String, byte[]> args = new HashMap<String, byte[]>();
 		args.put("publisherList", encode(PUBLISHER_PREFIX + application + subPredicate));
 		args.put("predicate", encode(subPredicate));
 		storageManager.insertSuperSlice(SC_APPLICATION_CONTROLLER, application + predicate, MEMBER_LIST_KEY, args);
 
 		// STORE VALUES RELATED TO THE PREDICATE
 		String data = "";
 		String begin = Long.toString(System.currentTimeMillis());
 		String end = "";
 
 		for (final MDataBean item : dataList) {
 			if (item.getKey().equals(DATA_ARG)) {
 				data = item.getValue();
 			} else if (item.getKey().equals(BEGIN_ARG)) {
 				begin = item.getValue();
 			} else if (item.getKey().equals(END_ARG)) {
 				end = item.getValue();
 			}
 		}
 		args.clear();
 		args.put("predicate", encode(subPredicate));
 		if (predicateList != null) {
 			args.put("predicateListJson", encode(gson.toJson(predicateList)));
 		}
 		args.put("begin", encode(begin));
 		args.put("end", encode(end));
 		args.put("publisherID", encode(publisher.getId()));
 
 		//---should be removed as they are not updated along with profile ...
 		args.put("publisherName", encode(publisher.getName()));
 		args.put("publisherProfilePicture",
 				encode(publisher.getProfilePicture() == null ? "" : publisher.getProfilePicture()));
 		args.put("publisherReputation",
 				encode(publisher.getReputation() == null ? "" : publisher.getReputation()));
 
 		args.put("data", encode(data));
 		storageManager.insertSuperSlice(SC_APPLICATION_CONTROLLER, application + predicate, subPredicate
 				+ publisher.getId(), args);
 
 		// STORE A NEW ENTRY IN THE ApplicationModel (use to retrieve all the
 		// predicate of a given application)
 		args.clear();
 		args.put(APPLICATION_CONTROLLER_ID, encode(application + predicate));
 		storageManager.insertSuperSlice(SC_APPLICATION_MODEL, application, predicate, args);
 
 		// STORE THE DATAs
 		args.clear();
 		for (final MDataBean item : dataList) {
 			args.put("key", encode(item.getKey()));
 			args.put("value", encode(item.getValue()));
 			args.put("ontologyID", encode(item.getOntologyID().getValue()));
 			storageManager.insertSuperSlice(
 					SC_DATA_LIST, application + subPredicate + publisher.getId(),
 					item.getKey(), 
 					args);
 			args.clear();
 		}
 
 		// Send emails
 		this.sendEmails( 
 				application,
 				publisher, 
 				dataList,
 				predicateList,
 				predicate);
 
 			}
 
 	/** 
 	 *  Send emails to the recipents, using the approriate template
 	 *  @param publisher User publishing this object
 	 *  @param applicationID ID of the application
 	 *  @param namespace Namespace, may be null. Equivalent to a "table name" 
 	 *  @param publication Object published (Data + Predicate) 
 	 *  @param recipients List of recipients 
 	 *  @param predicate The concatened predicate
 	 */
 	protected void sendEmails(  
 			String prefix,
 			MUserBean publisher,
 			List<MDataBean> dataList,
 			List<MDataBean> predicateList,
 			String predicate) 
 	{
 
 		// Prepare a map of key=>val to represent the publication for the mail 
 		HashMap<String, Object> publicationMap = new HashMap<String, Object>();
 		{
 			final List<MDataBean> ontologyList = new ArrayList<MDataBean>();
 
 			// Add predicate beans
 			if (predicateList != null) {
 				ontologyList.addAll(predicateList);      
 			}
 
 			// Add Data Beans 
 			ontologyList.addAll(dataList);
 
 			// Transform list of ontologies to map<String, String>
 			for (MDataBean dataBean : ontologyList) {
 				// Dont set empty values
 				if (MiscUtils.empty(dataBean.getValue())) continue;
 
 				// Unwrapp wrapped object in the "data" field into root publicationMap
 				if (dataBean.getKey().equals("data") && dataBean.getValue().startsWith("{")) {
 				    
 				    // Parse the embedded json object
 				    JSONObject obj = (JSONObject) JSONValue.parse(dataBean.getValue());
 				    
 				    // Copy each field into the publicationMap
 				    for (Object keyObj: obj.keySet()) {
 				        String key = (String) keyObj;
 				        Object val = obj.get(key);
 				        
 				        if (val instanceof String) publicationMap.put(key, val);
 
 				    } // End of loop on fields of unwrapped object
 				
 				} else { // Standard value (not wrapped 'data' object)
 				    publicationMap.put(dataBean.getKey(), dataBean.getValue());
 				}
 				
 			} // End of loop on ontologies 
 
 		} // End of "prepare a hashmap"
 
 		// Built list of recipients
 		final List<MUserBean> recipients = new ArrayList<MUserBean>();
 		{
 			final Map<byte[], byte[]> subscribers = storageManager.selectAll(CF_SUBSCRIBEES, prefix + predicate);
 			for (final Entry<byte[], byte[]> entry : subscribers.entrySet()) {
 				MUserBean recipient = null;
 				try {
 					recipient = profileManager.read(decode(entry.getKey()));
 				} catch (IOBackEndException e) {}
 				if (recipient != null) {
 					recipients.add(recipient);
 				}
 			}
 		}
 
 		// Split application ID
 		String applicationID = extractApplication(prefix);
 		String namespace = extractNamespace(prefix);
 
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
 		data.put("publisher", publisher);
 		data.put("publication", publicationMap);
 		data.put("predicate", predicate);
 
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
 
 	/**
 	 * The subscribe mechanism.
 	 * 
 	 * @see IPubSubManager#create(String, String, MUserBean)
 	 */
 	@Override public void create(
 			final String application, 
 			final String predicate, 
 			final MUserBean subscriber)
 					throws InternalBackEndException, IOBackEndException 
 					{
 		// STORE A NEW ENTRY IN THE UserList (SubscriberList)
 		storageManager.insertColumn(
 				CF_SUBSCRIBEES, 
 				application + predicate, 
 				subscriber.getId(), 
 				encode(String.valueOf(System.currentTimeMillis())));
 		storageManager.insertColumn(
 				CF_SUBSCRIBERS, 
 				application + subscriber.getId(), 
 				predicate, 
 				encode(String.valueOf(System.currentTimeMillis())));
 					}
 
 	/**
 	 * The find mechanism.
 	 * @see com.mymed.controller.core.manager.pubsub.IPubSubManager#read(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public List<Map<String, String>> read(final String application, final String predicate)
 			throws InternalBackEndException, IOBackEndException {
 
 		final List<Map<String, String>> resList = new ArrayList<Map<String, String>>();
 		final List<Map<byte[], byte[]>> subPredicateListMap = storageManager.selectList(SC_APPLICATION_CONTROLLER,
 				application + predicate);
 
 		for (final Map<byte[], byte[]> set : subPredicateListMap) {
 			if (set.size() > 3) { // do not return the memberList
 				final Map<String, String> resMap = new HashMap<String, String>();
 				for (final Entry<byte[], byte[]> entry : set.entrySet()) {
 					resMap.put(MConverter.byteArrayToString(entry.getKey()),
 							MConverter.byteArrayToString(entry.getValue()));
 				}
 
 				resList.add(resMap);
 			}
 		}
 
 		return resList;
 	}
 
 
 	/**
 	 * The find mechanism.
 	 * @see com.mymed.controller.core.manager.pubsub.IPubSubManager#read(String, String, String, int, Boolean)
 	 */
 	@Override
 	public List<Map<String, String>> read(
 			final String application, 
 			final String predicate,
 			final String start, 
 			final int count, 
 			final Boolean reversed)
 					throws InternalBackEndException, IOBackEndException, UnsupportedEncodingException 
 					{
 		final List<Map<String, String>> resList = new ArrayList<Map<String, String>>();
 		final List<Map<byte[], byte[]>> subPredicateListMap = storageManager.selectList(SC_APPLICATION_CONTROLLER,
 				application + predicate, start, count, reversed);
 
 		for (final Map<byte[], byte[]> set : subPredicateListMap) {
 			if (set.size() > 3) { // do not return the memberList
 				final Map<String, String> resMap = new HashMap<String, String>();
 				for (final Entry<byte[], byte[]> entry : set.entrySet()) {
 					resMap.put(MConverter.byteArrayToString(entry.getKey()),
 							MConverter.byteArrayToString(entry.getValue()));
 				}
 
 				resList.add(resMap);
 			}
 		}
 
 		return resList;
 					}
 
 
 
 	/**
 	 * The find mechanism: get more details.
 	 * @see com.mymed.controller.core.manager.pubsub.IPubSubManager#read(java.lang.String, java.lang.String,
 	 * java.lang.String)
 	 */
 	@Override
 	public List<Map<String, String>> read(
 			final String application, 
 			final String predicate, 
 			final String userID)
 					throws InternalBackEndException, IOBackEndException 
 					{
 
 		final List<Map<byte[], byte[]>> list = storageManager
 				.selectList(SC_DATA_LIST, application + predicate + userID);
 		final List<Map<String, String>> resList = new ArrayList<Map<String, String>>();
 		for (final Map<byte[], byte[]> set : list) {
 			final Map<String, String> resMap = new HashMap<String, String>();
 			for (final Entry<byte[], byte[]> entry : set.entrySet()) {
 				final String key = decode(entry.getKey());
 				final String value = decode(entry.getValue());
 				resMap.put(key, value);
 			}
 
 			resList.add(resMap);
 		}
 
 		return resList;
 					}
 
 	/**
 	 * The find mechanism.
 	 * @see com.mymed.controller.core.manager.pubsub.IPubSubManager#read(java.lang.String)
 	 */
 	@Override
 	public Map<String, String> read(final String appuserid) throws InternalBackEndException, IOBackEndException {
 
 		final Map<String, String> res = new HashMap<String, String>();
 		final Map<byte[], byte[]> predicates = storageManager.selectAll(CF_SUBSCRIBERS, appuserid);
 		for (final Entry<byte[], byte[]> entry : predicates.entrySet()) {
 			final String key = decode(entry.getKey());
 			final String val = decode(entry.getValue());
 			res.put(key, val);
 			LOGGER.info("__"+appuserid +" is subscribed to "+ key);
 		}
 
 		return res;
 	}
 
 	/**
 	 * @see IPubSubManager#delete(String * 3 + MUserBean)
 	 */
 	@Override
 	public void delete(
 			final String application, 
 			final String predicate, 
 			final String subPredicate,
 			final String publisherID) throws InternalBackEndException, IOBackEndException 
 			{
 
 		// Remove publisher member
 		storageManager.removeAll(
 				CF_SUBSCRIBEES, 
 				application + predicate);
 
 		// Remove the 1st level of data
 		storageManager.removeSuperColumn(
 				SC_APPLICATION_CONTROLLER, 
 				application + predicate,
 				subPredicate + publisherID);
 
 		// Remove the 2nd level of data
 		storageManager.removeAll(
 				SC_DATA_LIST, 
 				application + subPredicate + publisherID);
 
 		// Remove app model entry
 		// storageManager.removeSuperColumn(SC_APPLICATION_MODEL, application, predicate + publisher.getId());
 			}
 
 
 	/**
 	 * @see IPubSubManager#delete(String * 3)
 	 */
 	@Override
 	public void delete(
 			final String application, 
 			final String user, 
 			final String predicate) throws InternalBackEndException, IOBackEndException 
 			{
 		// Remove subscriber member from subsribers list
 		storageManager.removeColumn(CF_SUBSCRIBERS, application + user, predicate);
 		// Remove subscriber member from predicates subscribed list
 		storageManager.removeColumn(CF_SUBSCRIBEES, application + predicate, user);
 			}
 }
