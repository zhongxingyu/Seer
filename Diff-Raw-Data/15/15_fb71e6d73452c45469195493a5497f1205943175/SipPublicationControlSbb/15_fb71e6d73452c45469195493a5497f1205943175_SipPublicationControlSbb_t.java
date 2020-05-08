 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2011, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.mobicents.slee.sipevent.server.publication;
 
 import gov.nist.javax.sip.header.ims.PChargingFunctionAddressesHeader;
 import gov.nist.javax.sip.header.ims.PChargingVectorHeader;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.sip.RequestEvent;
 import javax.sip.ServerTransaction;
 import javax.sip.address.URI;
 import javax.sip.header.ContentTypeHeader;
 import javax.sip.header.EventHeader;
 import javax.sip.header.ExpiresHeader;
 import javax.sip.header.HeaderFactory;
 import javax.sip.header.SIPIfMatchHeader;
 import javax.sip.message.MessageFactory;
 import javax.sip.message.Request;
 import javax.sip.message.Response;
 import javax.slee.ActivityContextInterface;
 import javax.slee.CreateException;
 import javax.slee.InitialEventSelector;
 import javax.slee.RolledBackContext;
 import javax.slee.Sbb;
 import javax.slee.SbbContext;
 import javax.slee.SbbLocalObject;
 import javax.slee.serviceactivity.ServiceStartedEvent;
 
 import net.java.slee.resource.sip.SleeSipProvider;
 
 import org.apache.log4j.Logger;
 import org.mobicents.slee.ChildRelationExt;
 import org.mobicents.slee.sipevent.server.publication.jmx.PublicationControlManagement;
 import org.mobicents.slee.sipevent.server.publication.jmx.PublicationControlManagementMBean;
 
 /**
  * Sbb to control publication of sip events.
  * 
  * @author Eduardo Martins
  * 
  */
 public abstract class SipPublicationControlSbb implements Sbb, PublicationClientControlParent {
 
 	private static Logger logger = Logger.getLogger(SipPublicationControlSbb.class);	
 	
 	// JAIN-SIP provider & factories
 	private SleeSipProvider sipProvider;
 	private MessageFactory messageFactory;
 	private HeaderFactory headerFactory;
 
 	protected SbbContext sbbContext;
 	private Context jndiContext;
 	
 	/**
 	 * SbbObject's context setting
 	 */
 	public void setSbbContext(SbbContext sbbContext) {
 		this.sbbContext=sbbContext;
 		// retrieve factories, facilities & providers
 		try {
 			jndiContext = (Context) new InitialContext().lookup("java:comp/env");
 			sipProvider = (SleeSipProvider)jndiContext.lookup("slee/resources/jainsip/1.2/provider");      
 			headerFactory = sipProvider.getHeaderFactory();
 			messageFactory = sipProvider.getMessageFactory();
 		}
 		catch (Exception e) {
 			logger.error("Unable to retrieve factories, facilities & providers",e);			
 		}
 	}
 		
 	// --- INTERNAL CHILD SBB
 	
 	public abstract ChildRelationExt getPublicationControlChildRelation();
 	
 	private PublicationControlSbbLocalObject getPublicationControlChildSbb() {
 		ChildRelationExt childRelation = getPublicationControlChildRelation();
 		PublicationControlSbbLocalObject child = (PublicationControlSbbLocalObject) childRelation.get(ChildRelationExt.DEFAULT_CHILD_NAME);
 		if (child == null) {
 			try {
 				child = (PublicationControlSbbLocalObject) childRelation.create(ChildRelationExt.DEFAULT_CHILD_NAME);
 			} catch (Exception e) {
 				logger.error("Failed to create child sbb",e);				
 			}		
 		}
 		return child;
 	}
 	
 	// -- CONFIGURATION	
 
 	/**
 	 * Retrieves the current configuration for this component from an MBean
 	 * @return
 	 */
 	public static PublicationControlManagementMBean getConfiguration() {
 		return PublicationControlManagement.getInstance();
 	}
 	
 	/**
 	 * Initial event selector method for the PUBLISH event, defines the 
 	 * presentity as the custom name.
 	 *  
 	 * @param ies
 	 * @return
 	 */
 	public InitialEventSelector iesPublish(InitialEventSelector ies) {
 		final RequestEvent event = (RequestEvent) ies.getEvent();
 		ies.setCustomName(event.getRequest().getRequestURI().toString());
 		return ies;
 	}
 	
 	// ----------- EVENT HANDLERS
 	
 	/**
 	 * PUBLISH event processing
 	 * 
 	 * @param event
 	 * @param aci
 	 */
 	public void onPublish(RequestEvent event,
 			ActivityContextInterface aci) {
 
 		// detach from aci, we don't want ot handle the activity end event
 		SbbLocalObject sbbLocalObject = this.sbbContext.getSbbLocalObject();
 		aci.detach(sbbLocalObject);
 		
 		// get child sbb that handles all the publication logic
 		PublicationControlSbbLocalObject childSbb = getPublicationControlChildSbb();						
 		
 		if (logger.isDebugEnabled()) {
 			logger.debug("Processing PUBLISH request...");
 		}
 				
 		if (childSbb == null) { 
 			try {
 				// create response
 				Response response = messageFactory.createResponse(Response.SERVER_INTERNAL_ERROR,event.getRequest());
 				event.getServerTransaction().sendResponse(response);
 			}
 			catch (Exception f) {
 				logger.error("Can't send error response!",f);
 			}
 			return;
 		}
 				
 		/*
 		 * The presence of a body and the SIP-If-Match header field
 		 * determine the specific operation that the request is performing,
 		 * as described in Table 1.
 		 * +-----------+-------+---------------+---------------+
 		 * | Operation | Body? | SIP-If-Match? | Expires Value |
 		 * +-----------+-------+---------------+---------------+
 		 * | Initial   | yes   | no            | > 0           |
 		 * | Refresh   | no    | yes           | > 0           |
 		 * | Modify    | yes   | yes           | > 0           |
 		 * | Remove    | no    | yes           | 0             |
 		 * +-----------+-------+---------------+---------------+
 		 *            Table 1: Publication Operations
 		 *         
 		 *  If expires does not exist then the service must choose it's value          
 		 */
 		
 		// get event header
 		EventHeader eventHeader = (EventHeader) event.getRequest().getHeader(
 				EventHeader.NAME);
 		
 		if (eventHeader != null) {
 			// check event package
 			String eventPackage = eventHeader.getEventType();
 			if (acceptsEventPackage(eventPackage,childSbb)) {
 				URI entityURI = event.getRequest().getRequestURI();
				String entity = entityURI.toString();
				int i = entity.indexOf(';');
				if (i>0) {
					// remove all parameters (can't find a reference indicating any param that should be kept...)
					entity = entity.substring(0,i);
				}
 				// The ESC inspects the Request-URI to determine whether this request
 			    // is targeted to a resource for which the ESC is responsible for
 			    // maintaining event state.  If not, the ESC MUST return a 404 (Not
 			    // Found) response and skip the remaining steps.
 				if (childSbb.isResponsibleForResource(entityURI,eventPackage)) {	
 					
 					// process expires header
 					ExpiresHeader expiresHeader = event.getRequest().getExpires();
 					int expires;
 
 					// if expires does not exist then set it's value to default
 					// value
 					if (expiresHeader == null) {
 						expires = getConfiguration().getDefaultExpires();
 					} else {
 						expires = expiresHeader.getExpires();
 					}
 
 					// check expires value
 					if (expires > 0) {
 						// check if expires is not less than the allowed min expires
 						if (expires >= getConfiguration().getMinExpires()) {
 							// ensure expires is not bigger than max expires
 							if (expires > getConfiguration().getMaxExpires()) {
 								expires = getConfiguration().getMaxExpires();
 							}						
 							// new publication or publication refresh ?	
 							SIPIfMatchHeader sipIfMatchHeader = (SIPIfMatchHeader)event.getRequest().getHeader(SIPIfMatchHeader.NAME);
 							if (sipIfMatchHeader != null) {
 								// refresh or modification of publication
 								if (event.getRequest().getContentLength().getContentLength() == 0) {
 									// refreshing a publication
 									final Result result = childSbb.refreshPublication(entity, eventPackage, sipIfMatchHeader.getETag(), expires);
 									if (result.getStatusCode() < 300) {
 										try {
 											sendOkResponse(event, result.getETag(), result.getExpires());
 										}
 										catch (Exception e) {
 											logger.error("Error sending response to SIP client, removing related publication",e);
 											childSbb.removePublication(entity, eventPackage, result.getETag());
 										}
 									}
 									else {
 										sendErrorResponse(result.getStatusCode(), event.getRequest(), event.getServerTransaction(), eventPackage, childSbb);
 									}
 								}
 								else {
 									ContentTypeHeader contentTypeHeader = (ContentTypeHeader) event.getRequest().getHeader(ContentTypeHeader.NAME);
 									if (childSbb.acceptsContentType(eventPackage,contentTypeHeader)) {
 										// modification	
 										final Result result =  childSbb.modifyPublication(entity, eventPackage, sipIfMatchHeader.getETag(), new String(event.getRequest().getRawContent()), contentTypeHeader.getContentType(), contentTypeHeader.getContentSubType(), expires);
 										if (result.getStatusCode() < 300) {
 											try {
 												sendOkResponse(event, result.getETag(), result.getExpires());
 											} catch (Exception e) {
 												logger.error("Error sending response to SIP client, removing related publication",e);
 												childSbb.removePublication(entity, eventPackage, result.getETag());
 											}
 										}
 										else {
 											sendErrorResponse(result.getStatusCode(), event.getRequest(), event.getServerTransaction(), eventPackage, childSbb);
 										}
 									}
 									else {
 										// unsupported media type, send the ones supported
 										sendErrorResponse(Response.UNSUPPORTED_MEDIA_TYPE,event.getRequest(),event.getServerTransaction(),eventPackage,childSbb);
 									}
 								}
 							}
 							else {
 								// new publication
 								if (event.getRequest().getContentLength().getContentLength() != 0) {
 									ContentTypeHeader contentTypeHeader = (ContentTypeHeader) event.getRequest().getHeader(ContentTypeHeader.NAME);
 									if (childSbb.acceptsContentType(eventPackage,contentTypeHeader)) {
										final Result result = childSbb.newPublication(entity, eventPackage, new String(event.getRequest().getRawContent()), contentTypeHeader.getContentType(), contentTypeHeader.getContentSubType(), expires);
 										if (result.getStatusCode() < 300) {
 											try {
 												sendOkResponse(event, result.getETag(), result.getExpires());
 											} catch (Exception e) {
 												logger.error("Error sending response to SIP client, removing related publication",e);
 												childSbb.removePublication(entity, eventPackage, result.getETag());
 											}
 										}
 										else {
 											sendErrorResponse(result.getStatusCode(), event.getRequest(), event.getServerTransaction(), eventPackage, childSbb);
 										}
 									}
 									else {
 										// unsupported media type, send the one supported
 										sendErrorResponse(Response.UNSUPPORTED_MEDIA_TYPE,event.getRequest(),event.getServerTransaction(),eventPackage,childSbb);
 									}
 								}
 								else {
 									// send Bad Request since there is no content
 									sendErrorResponse(Response.BAD_REQUEST,event.getRequest(),event.getServerTransaction(),eventPackage,childSbb);
 								}
 							}						
 						} else {
 							// expires is > 0 but < min expires, respond (Interval
 							// Too Brief) with Min-Expires = MINEXPIRES
 							sendErrorResponse(Response.INTERVAL_TOO_BRIEF, event
 									.getRequest(), event.getServerTransaction(),eventPackage,childSbb);
 						}
 					}
 
 					else if (expires == 0) {
 						SIPIfMatchHeader sipIfMatchHeader = (SIPIfMatchHeader)event.getRequest().getHeader(SIPIfMatchHeader.NAME);
 						if (sipIfMatchHeader != null) {
 							// remove publication
 							int result = childSbb.removePublication(entity, eventPackage, sipIfMatchHeader.getETag());
 							if (result < 300) {
 								try {
 									sendOkResponse(event, null, -1);
 								} catch (Exception e) {
 									logger.error("Error sending response to SIP client",e);
 								}
 							}
 							else {
 								sendErrorResponse(result, event.getRequest(), event.getServerTransaction(), eventPackage, childSbb);
 							}
 						}
 						else {
 							// send Bad Request since removal requires etag
 							sendErrorResponse(Response.BAD_REQUEST,event.getRequest(),event.getServerTransaction(),eventPackage,childSbb);						
 						}							
 					} else {
 						// expires can't be negative
 						sendErrorResponse(Response.BAD_REQUEST, event.getRequest(),
 								event.getServerTransaction(),eventPackage,childSbb);
 					}
 				}
 				else {
 					// not responsible for this resource
 					sendErrorResponse(Response.NOT_FOUND, event.getRequest(), event.getServerTransaction(), eventPackage,childSbb);
 				}
 			} else {
 				// wrong event package, send bad event type error
 				sendErrorResponse(Response.BAD_EVENT, event.getRequest(), event
 						.getServerTransaction(),eventPackage,childSbb);
 			}
 		} else {
 			// subscribe does not have a event header
 			sendErrorResponse(Response.BAD_REQUEST, event.getRequest(), event
 					.getServerTransaction(),null,childSbb);
 		}
 
 	}
 
 	public void onOptions(RequestEvent requestEvent, ActivityContextInterface aci) {
 		
 		if (logger.isDebugEnabled()) {
 			logger.debug("Processing OPTIONS request");
 		}
 		aci.detach(this.sbbContext.getSbbLocalObject());
 		/*
 		 * A client may probe the ESC for the support of PUBLISH using the
 		 * OPTIONS request defined in SIP [4]. The ESC processes OPTIONS
 		 * requests as defined in Section 11.2 of RFC 3261 [4]. In the response
 		 * to an OPTIONS request, the ESC SHOULD include "PUBLISH" to the list
 		 * of allowed methods in the Allow header field. Also, it SHOULD list
 		 * the supported event packages in an Allow-Events header field.
 		 * 
 		 * The Allow header field may also be used to specifically announce
 		 * support for PUBLISH messages when registering. (See SIP Capabilities
 		 * [12] for details).
 		 */
         PublicationControlSbbLocalObject childSbb = getPublicationControlChildSbb();						
 		
 		if (childSbb == null) { 
 			try {
 				// create an error response:
 				Response response = messageFactory.createResponse(Response.SERVER_INTERNAL_ERROR,requestEvent.getRequest());
 				requestEvent.getServerTransaction().sendResponse(response);				
 			}
 			catch (Exception f) {
 				logger.error("Can't send error response!",f);
 			}
 			return;
 		}
 
 		try {
 			// create successful response:
 			Response response = messageFactory.createResponse(Response.OK,requestEvent.getRequest());
 			// add headers here:
 				String allowEventsHeader = "";
 				boolean first = true;
 				for (String acceptedEventPackage : childSbb.getEventPackages()) {
 					if (first) {
 						allowEventsHeader += acceptedEventPackage;
 						first = false;
 					}
 					else {
 						allowEventsHeader += ","+acceptedEventPackage;
 					}					
 				}
 				// Indicate the allowable event packages supported at the server:
 				response.addHeader(headerFactory.createAllowEventsHeader(allowEventsHeader));
 				
                 //In the response to an OPTIONS request, the ESC SHOULD include "PUBLISH" to the list
 				//of allowed methods in the Allow header field:
 				response.addHeader(headerFactory.createAllowHeader("PUBLISH, SUBSCRIBE, NOTIFY, OPTIONS"));
 				
 			requestEvent.getServerTransaction().sendResponse(response);			
 		}
 		catch (Exception e) {
 			logger.error("Can't send response!",e);
 		} 
 		
 	}
 	
 	// ----------- AUX METHODS
 	
 	private void sendOkResponse(RequestEvent event,String eTag,int expires) throws Exception {
 		// send 200 ok response	with expires and sipEtag				
 		Response response = messageFactory.createResponse(Response.OK, event.getRequest());
 		if (eTag != null) response.addHeader(headerFactory.createSIPETagHeader(eTag));
 		if (expires != -1) response.addHeader(headerFactory.createExpiresHeader(expires));
 				
 		/* aayush..started adding here: (Ref issue #567)
 		 * The PUBLISH request had a P-charging-vector header
 		 * that consisted of an ICID and an orig-ioi parameter.The PS
 		 * needs to preserve that header and needs to add a term-ioi
 		 * parameter pointing to its own domain name:*/
 		// 1. Get the header as received from the request:
 		final PChargingVectorHeader pcv = (PChargingVectorHeader) 
 		event.getRequest().getHeader(PChargingVectorHeader.NAME);
 		// 2. In case the request is received from a non-IMS VoIP network,P-charging-Vector wont be there,so check for pcv!=null
 		if (pcv!=null) {
 			pcv.setTerminatingIOI(getConfiguration().getPChargingVectorHeaderTerminatingIOI());
 			response.addHeader(pcv);
 		}
 		
 		// Also need to add the P-Charging-Function-Addresses header
 		// as received from the PUBLISH request in the 200 OK being sent:
 		final PChargingFunctionAddressesHeader pcfa = (PChargingFunctionAddressesHeader) 
 		event.getRequest().getHeader(PChargingFunctionAddressesHeader.NAME);
 		if (pcfa!=null) response.addHeader(pcfa);
 		
 		event.getServerTransaction().sendResponse(response);
 	}
 	
 	/*
 	 * Sends an error response with the specified status code, adding additional
 	 * headers if needed
 	 */
 	private void sendErrorResponse(int responseCode, Request request,
 			ServerTransaction serverTransaction, String eventPackage, PublicationControlSbbLocalObject childSbb) {
 		
 		try {
 			// create response
 			Response response = messageFactory.createResponse(responseCode,request);
 			// add headers if needed
 			if (responseCode == Response.BAD_EVENT && childSbb != null) {
 				String allowEventsHeader = "";
 				boolean first = true;
 				for (String acceptedEventPackage : childSbb.getEventPackages()) {
 					if (first) {
 						allowEventsHeader += acceptedEventPackage;
 						first = false;
 					}
 					else {
 						allowEventsHeader += ","+acceptedEventPackage;
 					}					
 				}
 				response
 						.addHeader(headerFactory.createAllowEventsHeader(allowEventsHeader));
 			}
 			else if (responseCode == Response.INTERVAL_TOO_BRIEF)
 				response.addHeader(headerFactory.createMinExpiresHeader(getConfiguration().getMinExpires()));
 			else if (responseCode == Response.UNSUPPORTED_MEDIA_TYPE && childSbb != null) {
 				response.addHeader(childSbb.getAcceptsHeader(eventPackage));				
 			}
 
 			serverTransaction.sendResponse(response);
 		}
 		catch (Exception e) {
 			logger.error("Can't send response!",e);
 		}
 	}	
 
 	/**
 	 * verifies if the specified event packaged is accepted
 	 */
 	private boolean acceptsEventPackage(String eventPackage,PublicationControlSbbLocalObject childSbb) {
 		if (eventPackage != null) {			
 			for(String acceptedEventPackage : childSbb.getEventPackages()) {
 				if (eventPackage.equals(acceptedEventPackage)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public void onServiceStartedEvent(ServiceStartedEvent event, ActivityContextInterface aci) {
 		logger.info("Mobicents SIP Event Publication Control service activated.");
 		// FIXME forcing load of classes of childs,  till deadlocks on slee class loaders nailed
 		getPublicationControlChildSbb();
 		aci.detach(sbbContext.getSbbLocalObject());
 	}
 	
 	// ----------- SBB OBJECT's LIFE CYCLE
 	
 	public void sbbActivate() {}
 	
 	public void sbbCreate() throws CreateException {}
 	
 	public void sbbExceptionThrown(Exception arg0, Object arg1,
 			ActivityContextInterface arg2) {}
 	
 	public void sbbLoad() {}
 	
 	public void sbbPassivate() {}
 	
 	public void sbbPostCreate() throws CreateException {}
 	
 	public void sbbRemove() {}
 	
 	public void sbbRolledBack(RolledBackContext arg0) {}
 	
 	public void sbbStore() {}
 	
 	public void unsetSbbContext() { this.sbbContext = null; }
 	
 }
