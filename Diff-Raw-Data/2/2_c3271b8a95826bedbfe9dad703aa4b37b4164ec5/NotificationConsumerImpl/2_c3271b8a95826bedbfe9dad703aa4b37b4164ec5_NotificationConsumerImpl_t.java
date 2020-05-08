 package org.petalslink.wsn.webservices.service;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.transform.TransformerException;
 
 import org.apache.commons.collections.buffer.CircularFifoBuffer;
 import org.event_processing.events.types.EsrAction;
 import org.event_processing.events.types.EsrShowFriendGeolocation;
 import org.event_processing.events.types.EsrSubscribeTo;
 import org.event_processing.events.types.EsrUnsubscribeFrom;
 import org.event_processing.events.types.UcTelcoCall;
 import org.event_processing.events.types.UcTelcoComposeMail;
 import org.event_processing.events.types.UcTelcoEsrRecom;
 import org.event_processing.events.types.UcTelcoGeoLocation;
 import org.event_processing.events.types.UcTelcoOpenFacebook;
 import org.event_processing.events.types.UcTelcoOpenTwitter;
 import org.ontoware.aifbcommons.collection.ClosableIterator;
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.Statement;
 import org.ontoware.rdf2go.model.Syntax;
 import org.ontoware.rdf2go.model.node.Node;
 import org.ontoware.rdf2go.model.node.URI;
 import org.ontoware.rdf2go.model.node.Variable;
 import org.ontoware.rdfreactor.schema.rdfs.Class;
 import org.w3c.dom.Document;
 
 import com.ebmwebsourcing.easycommons.xml.XMLHelper;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.NotificationMessageHolderType;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.TopicExpressionType;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
 import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;
 import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;
 import com.google.android.gcm.server.Constants;
 import com.google.android.gcm.server.Result;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.JsonPrimitive;
 import com.google.gson.JsonSyntaxException;
 import com.orange.play.gcmAPI.Registration;
 
 import eu.play_project.play_commons.constants.Stream;
 import eu.play_project.play_eventadapter.AbstractReceiver;
 import eu.play_project.play_eventadapter.NoRdfEventException;
 
 public class NotificationConsumerImpl implements INotificationConsumer {
 	private static INotificationConsumer INSTANCE;
 
 	private static final String greekPhones = "30[0-9]*";
 	private static final String frenchPhones = "33[0-9]*";
 
 	// FIXME Antonio: solve the / at the beginning
 	private static final String STREAM_ESR_RECOM = "S:" + Stream.ESRRecom.getQName().getLocalPart().split("#")[0].toUpperCase();
 	private static final String STREAM_TAXI_UC_ESR_RECOM = "S:" + Stream.TaxiUCESRRecom.getQName().getLocalPart().split("#")[0].toUpperCase();
 	private static final String STREAM_TAXI_UC_ESR_RECOM_DCEP = "S:" + Stream.TaxiUCESRRecomDcep.getQName().getLocalPart().split("#")[0].toUpperCase();
	private static final String STREAM_MCEP_CONTROL = "M:CONTROL";
 	private final AbstractReceiver receiver = new AbstractReceiver() {};
 	private final CircularFifoBuffer duplicatesCache =  new CircularFifoBuffer(32);
 
 	private final Registration gcmRegIds = Registration.getInstance();
 	
 	public static final synchronized INotificationConsumer getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new NotificationConsumerImpl();
 		}
 		return INSTANCE;
 	}
 
 	private NotificationConsumerImpl() {
 	}
 
 	@Override
 	public void notify(Notify notify) throws WsnbException {
 		// Get the DOM from the bean
 		Document document = Wsnb4ServUtils.getWsnbWriter().writeNotifyAsDOM(notify);
 		String documentStr = null;
 		try {
 			documentStr = XMLHelper.createStringFromDOMDocument(document);
 			System.out.println(String.format("\txmlDocument = %s\n", documentStr.replace("\n", "")));
 
 			List<NotificationMessageHolderType> messages = notify.getNotificationMessage();
 			for (NotificationMessageHolderType notificationMessageHolderType : messages) {
 				// Get the topic
 				TopicExpressionType targetTopic = notificationMessageHolderType.getTopic();
 				String targetTopicContent = targetTopic.getContent();
 				System.out.format("\ttargetTopic\n");
 				System.out.format("\t\tDialect = %s\n", targetTopic.getDialect());
 				System.out.format("\t\tnamespaces = %s\n", targetTopic.getTopicNamespaces().toString());
 				System.out.format("\t\tcontent = %s\n", targetTopicContent);
 
 				/*
 				 * RDF parsing
 				 */
 				System.out.print("\tRDF parsing... ");
 				Model rdf;
 				try {
 					rdf = receiver.parseRdf(documentStr);
 				} catch (NoRdfEventException e) {
 					System.out.format("\tNo RDF event content recognized: event not handled. (%s)\n", e.getMessage());
 					return;
 				}
 				System.out.println("\tok");
 				
 				/*
 				 * Do some checking for duplicates (memorizing a few recently seen
 				 * events)
 				 */
 				synchronized (duplicatesCache) {
 					String eventId = rdf.getContextURI().toString();
 					if (duplicatesCache.contains(eventId)) {
 						System.out.println("\tSuppressed Duplicate Event: " + eventId);
 						return;
 					}
 					else {
 						duplicatesCache.add(eventId);
 					}
 				}
 				
 				
 				// Depending on the stream, dispatch request
 				targetTopicContent = targetTopicContent.toUpperCase();
 
 				if (targetTopicContent.contains(STREAM_ESR_RECOM)
 						|| targetTopicContent.contains(STREAM_TAXI_UC_ESR_RECOM)
 						|| targetTopicContent.contains(STREAM_TAXI_UC_ESR_RECOM_DCEP)) {
 					/*
 					 * Deal with Recommendation events:
 					 */
 					handleRecommendation(targetTopicContent, rdf);
 				}
 				else if (targetTopicContent.contains(STREAM_MCEP_CONTROL)) {
 					/*
 					 * Deal with Control events:
 					 */
 					handleControlEvent(targetTopicContent, rdf);
 				}
 				else {
 					/*
 					 * Deal with arbitrary PLAY RDF Events:
 					 */
 					handleArbitraryEvent(targetTopicContent, rdf);
 				}
 			}
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected void handleRecommendation(String targetTopicContent, Model rdf) {
 		System.out.format("\ttype: recommendation\n");
 
 		try {
 			System.out.print("\tExtracting event...");
 			UcTelcoEsrRecom event = UcTelcoEsrRecom.getAllInstances_as(rdf).firstValue();
 			if (event == null)
 				throw new IllegalArgumentException("event == null");
 			System.out.format(" extracted = %s\n", event.toString());
 
 			String uctelco_callerPhoneNumber = event.getUcTelcoCallerPhoneNumber();
 			if (uctelco_callerPhoneNumber == null || uctelco_callerPhoneNumber.isEmpty())
 				throw new IllegalArgumentException("uctelco_callerPhoneNumber == null || uctelco_callerPhoneNumber.isEmpty()");
 			System.out.format("\t\tuctelco_callerPhoneNumber = %s\n", uctelco_callerPhoneNumber);
 
 			String uctelco_calleePhoneNumber = event.getUcTelcoCalleePhoneNumber();
 			if (uctelco_calleePhoneNumber == null || uctelco_calleePhoneNumber.isEmpty())
 				throw new IllegalArgumentException("uctelco_calleePhoneNumber == null || uctelco_calleePhoneNumber.isEmpty()");
 			System.out.format("\t\tuctelco_calleePhoneNumber = %s\n", uctelco_calleePhoneNumber);
 
 			// Skip particular phones
 			if (targetTopicContent.contains(STREAM_TAXI_UC_ESR_RECOM) &&
 					(uctelco_callerPhoneNumber.matches(greekPhones) ||
 							uctelco_calleePhoneNumber.matches(greekPhones))) {
 				System.out.println("\tRecom from Orange to greek phones => skip\n");
 				return;
 			}
 			if (targetTopicContent.contains(STREAM_TAXI_UC_ESR_RECOM_DCEP) &&
 					(uctelco_callerPhoneNumber.matches(greekPhones) ||
 							uctelco_calleePhoneNumber.matches(greekPhones))) {
 				System.out.println("\tRecom from DCEP to greek phones => skip\n");
 				return;
 			}
 			if (targetTopicContent.contains(STREAM_ESR_RECOM) &&
 					(uctelco_callerPhoneNumber.matches(frenchPhones) ||
 							uctelco_calleePhoneNumber.matches(frenchPhones))) {
 				System.out.println("\tRecom from ESR to french phones => skip\n");
 				return;
 			}
 
 			// Build JSON object
 			System.out.println("\tBuilding the JSON Object...");
 
 			JsonObject recomJson = new JsonObject();
 			Calendar endTime = event.getEndTime();
 			if (endTime != null)
 				recomJson.addProperty("endTime", endTime.getTimeInMillis());
 			ClosableIterator<String> messages = event.getAllMessage();
 			if (messages != null && messages.hasNext())
 				recomJson.addProperty("message", messages.next());
 			Calendar startTime = event.getStartTime();
 			if (startTime != null)
 				recomJson.addProperty("startTime", startTime.getTimeInMillis());
 
 			recomJson.addProperty("rdf_type", event.getRDFSClassURI().asJavaURI().toString());
 
 			Node esrRecommendation = event.getEsrRecommendation_asNode();
 			if (esrRecommendation != null)
 				recomJson.add("esr_recommendation", new JsonPrimitive(esrRecommendation.toString()));
 
 			ClosableIterator<EsrAction> uctelcoActions = event.getAllUcTelcoAction();
 			JsonArray uctelcoActionsJson = new JsonArray();
 			if (uctelcoActions != null) {
 				while (uctelcoActions.hasNext()) {
 					EsrAction esrAction = uctelcoActions.next();
 					System.out.format("\t\tadding action: %s\n", esrAction.toSPARQL());
 					JsonObject esrActionJson = new JsonObject();
 
 					ClosableIterator<Class> esrActionTypes = esrAction.getAllType();
 					Set<URI> esrActionTypesSet = new HashSet<URI>();
 					while (esrActionTypes.hasNext()) {
 						esrActionTypesSet.add(esrActionTypes.next().asURI());
 					}
 
 					if (esrAction.getAllType().hasNext()) {
 						esrActionJson.addProperty("rdf_type", esrAction.getAllType().next().asURI().asJavaURI().toString());
 					}
 					
 					if (esrActionTypesSet.contains(EsrSubscribeTo.RDFS_CLASS)) {
 						EsrSubscribeTo esrSubscribeTo = (EsrSubscribeTo) esrAction.castTo(EsrSubscribeTo.class);
 
 						org.event_processing.events.types.Stream toStream = esrSubscribeTo.getToStream();
 						if (toStream != null)
 							esrActionJson.addProperty("esr_toStream", toStream.toString());
 					} else if (esrActionTypesSet.contains(EsrUnsubscribeFrom.RDFS_CLASS)) {
 						EsrUnsubscribeFrom esrUnsubscribeFrom = (EsrUnsubscribeFrom) esrAction.castTo(EsrUnsubscribeFrom.class);
 
 						org.event_processing.events.types.Stream fromStream = esrUnsubscribeFrom.getFromStream();
 						if (fromStream != null)
 							esrActionJson.addProperty("esr_fromStream", fromStream.toString());
 					} else if (esrActionTypesSet.contains(UcTelcoOpenFacebook.RDFS_CLASS)) {
 						UcTelcoOpenFacebook ucTelcoOpenFacebook = (UcTelcoOpenFacebook) esrAction.castTo(UcTelcoOpenFacebook.class);
 
 						String user_id = ucTelcoOpenFacebook.getFacebookId();
 						if (user_id != null)
 							esrActionJson.addProperty("user_id", user_id);
 					} else if (esrActionTypesSet.contains(UcTelcoOpenTwitter.RDFS_CLASS)) {
 						UcTelcoOpenTwitter ucTelcoOpenTwitter = (UcTelcoOpenTwitter) esrAction.castTo(UcTelcoOpenTwitter.class);
 
 						String screenName = ucTelcoOpenTwitter.getTwitterScreenName();
 						if (screenName != null)
 							esrActionJson.addProperty("screenName", screenName);
 					} else if (esrActionTypesSet.contains(UcTelcoComposeMail.RDFS_CLASS)) {
 						UcTelcoComposeMail ucTelcoComposeMail = (UcTelcoComposeMail) esrAction.castTo(UcTelcoComposeMail.class);
 
 						String uctelco_mailAddress = ucTelcoComposeMail.getUcTelcoMailAddress().toString();
 						if (uctelco_mailAddress != null)
 							esrActionJson.addProperty("uctelco_mailAddress", uctelco_mailAddress);
 
 						String uctelco_mailSubject = ucTelcoComposeMail.getUcTelcoMailSubject();
 						if (uctelco_mailSubject != null)
 							esrActionJson.addProperty("uctelco_mailSubject", uctelco_mailSubject);
 
 						String uctelco_mailContent = ucTelcoComposeMail.getUcTelcoMailContent();
 						if (uctelco_mailContent != null)
 							esrActionJson.addProperty("uctelco_mailContent", uctelco_mailContent);
 
 					} else if (esrActionTypesSet.contains(EsrShowFriendGeolocation.RDFS_CLASS)) {
 						EsrShowFriendGeolocation esrShowFriendGeolocation = (EsrShowFriendGeolocation) (esrAction.castTo(EsrShowFriendGeolocation.class));
 
 						String uctelco_phoneNumber = esrShowFriendGeolocation.getUcTelcoPhoneNumber();
 						if (uctelco_phoneNumber != null)
 							esrActionJson.addProperty("uctelco_phoneNumber", uctelco_phoneNumber);
 
 						ClosableIterator<Node> geo_lat = esrShowFriendGeolocation.getAllGeoLatitude_asNode();
 						if (geo_lat.hasNext()) {
 							try {
 								esrActionJson.addProperty("geo_lat", Double.parseDouble(geo_lat.next().toString()));
 							} catch (NumberFormatException e) {
 								e.printStackTrace();
 							}
 						}
 
 						ClosableIterator<Node> geo_long = esrShowFriendGeolocation.getAllGeoLongitude_asNode();
 						if (geo_long.hasNext()) {
 							try {
 								esrActionJson.addProperty("geo_long", Double.parseDouble(geo_long.next().toString()));
 							} catch (NumberFormatException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 
 					uctelcoActionsJson.add(esrActionJson);
 					System.out.format("\t\taction added: %s\n", esrActionJson.toString());
 				}
 			}
 			recomJson.add("uctelco_action", uctelcoActionsJson);
 
 			if (uctelco_calleePhoneNumber != null)
 				recomJson.addProperty("uctelco_calleePhoneNumber", uctelco_calleePhoneNumber);
 
 			if (uctelco_callerPhoneNumber != null)
 				recomJson.addProperty("uctelco_callerPhoneNumber", uctelco_callerPhoneNumber);
 
 			Boolean uctelco_answerRequired = event.getUcTelcoAnswerRequired();
 			if (uctelco_answerRequired != null)
 				recomJson.addProperty("uctelco_answerRequired", uctelco_answerRequired);
 
 			Boolean uctelco_ackRequired = event.getUcTelcoAckRequired();
 			if (uctelco_ackRequired != null)
 				recomJson.addProperty("uctelco_ackRequired", uctelco_ackRequired);
 
 			System.out.println("\tJSON Object built");
 
 			// Recommendation
 			System.out.format("\tUcTelcoEsrRecom = %s\n", recomJson.toString());
 
 			notifyByPhoneNumber(uctelco_callerPhoneNumber, recomJson);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected void handleControlEvent(String targetTopicContent, Model rdf) {
 		System.out.format("\ttype: control event\n");
 
 		try {
 			StringWriter s = new StringWriter();
 
 			String phoneNumber = "";
 			ClosableIterator<Statement> results;
 			results = rdf.findStatements(Variable.ANY, UcTelcoCall.UCTELCOCALLERPHONENUMBER, Variable.ANY);
 			if (results.hasNext()) {
 				phoneNumber = results.next().getObject().toString();
 			}
 			else {
 				results.close();
 				results = rdf.findStatements(Variable.ANY, UcTelcoGeoLocation.UCTELCOPHONENUMBER, Variable.ANY);
 				if (results.hasNext()) {
 					phoneNumber = results.next().getObject().toString();
 				}
 			}
 
 			rdf.writeTo(s, Syntax.RdfJson);
 			// get subscribing regIds and use #notifyByTopic(...)
 			notifyByPhoneNumber(phoneNumber, new JsonParser().parse(s.toString()).getAsJsonObject());
 
 		} catch (JsonSyntaxException e) {
 			System.out.format("\tJsonSyntaxException while creating Json event: event not handled. (%s)\n", e.getMessage());
 		} catch (IOException e) {
 			System.out.format("\tIOException while creating Json even: event not handled. (%s)\n", e.getMessage());
 		}
 	}
 	
 	protected void handleArbitraryEvent(String targetTopicContent, Model rdf) {
 		System.out.format("\ttype: arbitrary event\n");
 
 		try {
 			StringWriter s = new StringWriter();
 
 			String topicUri = "";
 			ClosableIterator<Statement> results;
 			results = rdf.findStatements(Variable.ANY, UcTelcoCall.UCTELCOCALLERPHONENUMBER, Variable.ANY);
 			if (results.hasNext()) {
 				topicUri = results.next().getObject().toString();
 			}
 			else {
 				results.close();
 				results = rdf.findStatements(Variable.ANY, UcTelcoGeoLocation.UCTELCOPHONENUMBER, Variable.ANY);
 				if (results.hasNext()) {
 					topicUri = results.next().getObject().toString();
 				}
 			}
 
 			rdf.writeTo(s, Syntax.RdfJson);
 			notifyByTopic(topicUri, new JsonParser().parse(s.toString()).getAsJsonObject());
 
 		} catch (JsonSyntaxException e) {
 			System.out.format("\tJsonSyntaxException while creating Json event: event not handled. (%s)\n", e.getMessage());
 		} catch (IOException e) {
 			System.out.format("\tIOException while creating Json even: event not handled. (%s)\n", e.getMessage());
 		}
 	}
 
 	private void notifyByPhoneNumber(String uctelco_callerPhoneNumber,
 			JsonObject eventPayload) throws IOException {
 		String registrationId = gcmRegIds.getRegistrationIdForPhoneNumber(uctelco_callerPhoneNumber);
 		System.out.format("\t\t => related registrationID = %s\n", registrationId);
 		sendGcm(registrationId, eventPayload);
 	}
 	
 	private void notifyByTopic(String topicUri,
 			JsonObject eventPayload) {
 		// FIXME stuehmer: get subscribing regIds
 		
 	}
 	private void sendGcm(String registrationId,
 			JsonObject eventPayload) throws IOException {
 		System.out.println("\t\t => Message size [bytes]: " + eventPayload.toString().length());
 		if (registrationId != null) {
 			System.out.println("\t\t => Sending");
 			Result result = com.orange.play.gcmAPI.Send.sendMessage(registrationId, eventPayload.toString());
 			if (result.getMessageId() != null) {
 				System.out.println("\t\t => => GCM result: message sent successfully");
 				String canonicalRegId = result.getCanonicalRegistrationId();
 				if (canonicalRegId != null) {
 					// same device has more than one registration ID: update database
 					System.out.println("\t\t => => GCM result: same device has more than one registration ID: update database");
 					gcmRegIds.updateRegistrationId(registrationId, canonicalRegId);
 				}
 			} else {
 				String error = result.getErrorCodeName();
 				if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
 					// application has been removed from device - unregister database
 					System.out.println("\t\t => => GCM result: application has been removed from device - unregister database");
 					gcmRegIds.removeRegistrationId(registrationId);
 				}
 				else {
 					System.out.println("\t\t => => GCM result: " + error);
 				}
 			}
 		}
 	}
 }
