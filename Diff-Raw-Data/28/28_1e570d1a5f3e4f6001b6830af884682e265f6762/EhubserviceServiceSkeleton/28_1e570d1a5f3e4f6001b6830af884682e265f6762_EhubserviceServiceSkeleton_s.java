 /**
  * EhubserviceServiceSkeleton.java
  *
  * This file was auto-generated from WSDL
  * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:22:40 CEST)
  */
 package com.idega.xroad.service.wsdl;
 
 import is.idega.idegaweb.egov.application.data.Application;
 import is.idega.idegaweb.egov.message.data.UserMessage;
 
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.ejb.FinderException;
 import javax.mail.util.ByteArrayDataSource;
 
 import net.x_rd.ee.ehubservice.producer.AllowedMethodList;
 import net.x_rd.ee.ehubservice.producer.AllowedMethods;
 import net.x_rd.ee.ehubservice.producer.AllowedMethodsResponse;
 import net.x_rd.ee.ehubservice.producer.AllowedMethodsResponseE;
 import net.x_rd.ee.ehubservice.producer.CaseDetails;
 import net.x_rd.ee.ehubservice.producer.CaseList;
 import net.x_rd.ee.ehubservice.producer.CaseStep;
 import net.x_rd.ee.ehubservice.producer.GetCaseDetails;
 import net.x_rd.ee.ehubservice.producer.GetCaseDetailsE;
 import net.x_rd.ee.ehubservice.producer.GetCaseDetailsRequest;
 import net.x_rd.ee.ehubservice.producer.GetCaseDetailsResponse;
 import net.x_rd.ee.ehubservice.producer.GetCaseDetailsResponseE;
 import net.x_rd.ee.ehubservice.producer.GetCaseList;
 import net.x_rd.ee.ehubservice.producer.GetCaseListE;
 import net.x_rd.ee.ehubservice.producer.GetCaseListRequest;
 import net.x_rd.ee.ehubservice.producer.GetCaseListResponse;
 import net.x_rd.ee.ehubservice.producer.GetCaseListResponseE;
 import net.x_rd.ee.ehubservice.producer.GetDocument;
 import net.x_rd.ee.ehubservice.producer.GetDocumentE;
 import net.x_rd.ee.ehubservice.producer.GetDocumentRequest;
 import net.x_rd.ee.ehubservice.producer.GetDocumentResponse;
 import net.x_rd.ee.ehubservice.producer.GetDocumentResponseE;
 import net.x_rd.ee.ehubservice.producer.GetMessagesList;
 import net.x_rd.ee.ehubservice.producer.GetMessagesListE;
 import net.x_rd.ee.ehubservice.producer.GetMessagesListRequest;
 import net.x_rd.ee.ehubservice.producer.GetMessagesListResponse;
 import net.x_rd.ee.ehubservice.producer.GetMessagesListResponseE;
 import net.x_rd.ee.ehubservice.producer.GetNotifications;
 import net.x_rd.ee.ehubservice.producer.GetNotificationsE;
 import net.x_rd.ee.ehubservice.producer.GetNotificationsRequest;
 import net.x_rd.ee.ehubservice.producer.GetNotificationsResponse;
 import net.x_rd.ee.ehubservice.producer.GetNotificationsResponseE;
 import net.x_rd.ee.ehubservice.producer.GetPrefilledDocument;
 import net.x_rd.ee.ehubservice.producer.GetPrefilledDocumentE;
 import net.x_rd.ee.ehubservice.producer.GetPrefilledDocumentRequest;
 import net.x_rd.ee.ehubservice.producer.GetPrefilledDocumentResponse;
 import net.x_rd.ee.ehubservice.producer.GetPrefilledDocumentResponseE;
 import net.x_rd.ee.ehubservice.producer.GetServiceList;
 import net.x_rd.ee.ehubservice.producer.GetServiceListE;
 import net.x_rd.ee.ehubservice.producer.GetServiceListRequest;
 import net.x_rd.ee.ehubservice.producer.GetServiceListResponse;
 import net.x_rd.ee.ehubservice.producer.GetServiceListResponseE;
 import net.x_rd.ee.ehubservice.producer.GetXFormLabels;
 import net.x_rd.ee.ehubservice.producer.GetXFormLabelsE;
 import net.x_rd.ee.ehubservice.producer.GetXFormLabelsRequest;
 import net.x_rd.ee.ehubservice.producer.GetXFormLabelsResponse;
 import net.x_rd.ee.ehubservice.producer.GetXFormLabelsResponseE;
 import net.x_rd.ee.ehubservice.producer.LabelPair;
 import net.x_rd.ee.ehubservice.producer.LabelType;
 import net.x_rd.ee.ehubservice.producer.LangType;
 import net.x_rd.ee.ehubservice.producer.MarkCaseAsRead;
 import net.x_rd.ee.ehubservice.producer.MarkCaseAsReadE;
 import net.x_rd.ee.ehubservice.producer.MarkCaseAsReadRequest;
 import net.x_rd.ee.ehubservice.producer.MarkCaseAsReadResponse;
 import net.x_rd.ee.ehubservice.producer.MarkCaseAsReadResponseE;
 import net.x_rd.ee.ehubservice.producer.MarkNotificationAsRead;
 import net.x_rd.ee.ehubservice.producer.MarkNotificationAsReadE;
 import net.x_rd.ee.ehubservice.producer.MarkNotificationAsReadRequest;
 import net.x_rd.ee.ehubservice.producer.MarkNotificationAsReadResponse;
 import net.x_rd.ee.ehubservice.producer.MarkNotificationAsReadResponseE;
 import net.x_rd.ee.ehubservice.producer.MarkResult;
 import net.x_rd.ee.ehubservice.producer.Message;
 import net.x_rd.ee.ehubservice.producer.MessagesList;
 import net.x_rd.ee.ehubservice.producer.Notification;
 import net.x_rd.ee.ehubservice.producer.NotificationsList;
 import net.x_rd.ee.ehubservice.producer.PreffiledDocument;
 import net.x_rd.ee.ehubservice.producer.ServiceEntry;
 import net.x_rd.ee.ehubservice.producer.ServiceList;
 import net.x_rd.ee.ehubservice.producer.SubmitDocument;
 import net.x_rd.ee.ehubservice.producer.SubmitDocumentE;
 import net.x_rd.ee.ehubservice.producer.SubmitDocumentRequest;
 import net.x_rd.ee.ehubservice.producer.SubmitDocumentResponse;
 import net.x_rd.ee.ehubservice.producer.SubmitDocumentResponseE;
 import net.x_rd.ee.ehubservice.producer.SubmittedDocumentInfo;
 import net.x_rd.ee.ehubservice.producer.XFormLabel;
 
 import org.jbpm.taskmgmt.exe.TaskInstance;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 import org.w3c.dom.Document;
 
 import com.idega.block.form.data.XForm;
 import com.idega.block.process.data.Case;
 import com.idega.business.IBOLookup;
 import com.idega.business.IBOLookupException;
 import com.idega.core.accesscontrol.business.LoginBusinessBean;
 import com.idega.core.business.DefaultSpringBean;
 import com.idega.core.file.util.MimeTypeUtil;
 import com.idega.data.IDOLookup;
 import com.idega.idegaweb.IWMainApplication;
 import com.idega.idegaweb.IWMainApplicationSettings;
 import com.idega.notifier.business.NotificationService;
 import com.idega.notifier.data.NotificationEntity;
 import com.idega.notifier.data.NotificationReceiverEntity;
 import com.idega.notifier.data.dao.NotificationEntityDAO;
 import com.idega.presentation.IWContext;
 import com.idega.user.data.User;
 import com.idega.util.ArrayUtil;
 import com.idega.util.CoreConstants;
 import com.idega.util.CoreUtil;
 import com.idega.util.ListUtil;
 import com.idega.util.StringUtil;
 import com.idega.util.datastructures.map.MapUtil;
 import com.idega.util.expression.ELUtil;
 import com.idega.util.xml.XmlUtil;
 import com.idega.xroad.service.business.ApplicationService;
 import com.idega.xroad.service.business.BPMCasesService;
 import com.idega.xroad.service.business.CasesService;
 import com.idega.xroad.service.business.LegacyCasesService;
 import com.idega.xroad.service.business.XFormService;
 
 /**
  * EhubserviceServiceSkeleton java skeleton for the axisService
  */
 /**
  * <p>Implementation of EHub service for X-Road</p>
  * <p>You can report about problems to: 
  * <a href="mailto:martynas@idega.is">Martynas Stakė</a></p>
  *
  * @version 1.0.0 May 2, 2013
  * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
  */
 @Service
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class EhubserviceServiceSkeleton extends DefaultSpringBean implements
 		EhubserviceServiceSkeletonInterface {
 	
 	protected static final Logger LOGGER = Logger.getLogger(
 			EhubserviceServiceSkeleton.class.getName());
 	
 	@Autowired
 	private XFormService xFormService;
 	
 	@Autowired
 	private BPMCasesService bpmCasesService = null;
 	
 	@Autowired
 	private LegacyCasesService legacyCasesService = null;
 	
 	@Autowired
 	private ApplicationService applicationService = null;
 	
 	@Autowired
 	private NotificationService notificationService = null;
 
 	@Autowired
 	private NotificationEntityDAO notificationEntityDAO = null;
 	
 	private com.idega.user.business.UserBusiness userBussiness = null;
 	
 	/**
 	 * 
 	 * <p>Searches database for available {@link Case}s by given 
 	 * service provider id an {@link User#getId()}. 
 	 * If service provider id not given, exception will be thrown.</p>
 	 * @param caseListRequestE - request from client, which contains 
 	 * service provider id and {@link User#getId()}, not <code>null</code>;
 	 * @return response filled with data from {@link Collection} of 
 	 * {@link Case}s.
 	 * @throws NullPointerException when service provider id not given
 	 * or nothing found by given provider id.
 	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 	 */
 	public GetCaseListResponseE getCaseList(GetCaseListE caseListRequestE) {
 		if (caseListRequestE == null) {
 			throw new NullPointerException(GetCaseListE.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		
 		GetCaseList caseListRequest = caseListRequestE.getGetCaseList();
 		if (caseListRequest == null) {
 			throw new NullPointerException(GetCaseList.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 				
 		GetCaseListRequest request = caseListRequest.getRequest();
 		if (request == null) {
 			throw new NullPointerException(GetCaseListRequest.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		String serviceProviderID = request.getServiceProviderId();
 		if (StringUtil.isEmpty(serviceProviderID)) {
 			throw new NullPointerException("Service provider ID " + 
 					"is null. Please provide correct service provider id!");
 		}
 
 		String personalID = request.getCitizenId();
 		if (StringUtil.isEmpty(personalID)) {
 			throw new NullPointerException("Personal ID of " + User.class + 
 					"is null. Please provide correct personal id!");
 		}
 		
 		Collection<Case> activeCases = getCasesService(null).getCases(personalID);
 		if (ListUtil.isEmpty(activeCases)) {
 			throw new NullPointerException("No cases found for " + 
 					User.class.getName() + " by personalID: " + personalID);
 		}
 		
 		CaseList response = new CaseList();
 		for (Case activeCase : activeCases) {
 			if (StringUtil.isEmpty(activeCase.getCaseIdentifier())) {
 				continue;
 			}
 			
 			net.x_rd.ee.ehubservice.producer.Case caseType = new net.x_rd.ee.ehubservice.producer.Case();
 			caseType.setId(activeCase.getCaseIdentifier());
 			caseType.setLastOperationDate(getCasesService(activeCase)
 					.getLastOperationDate(activeCase, null));
 			caseType.setNameLabel(getLabelType(activeCase.getSubject(), null));
 			caseType.setNextOperationTime(null);
 			caseType.setOfficial(getCasesService(activeCase).getOfficialName(activeCase));
 			caseType.setOrgLabel(getLabelType(getOrganizationName(), null));
 			caseType.setServiceId(serviceProviderID);
 			caseType.setStatusLabel(
 					getLabelType(getCasesService(activeCase)
 							.getStatus(activeCase, null), null));
 			caseType.setTypeLabel(getLabelType(getApplicationService()
 					.getServiceDescription(activeCase), null));
 				
 			response.addCases(caseType);
 		}
 		
 		GetCaseListResponse caseListResponse = new GetCaseListResponse();
 		caseListResponse.setResponse(response);
 		caseListResponse.setRequest(request);
 				
 		GetCaseListResponseE caseListResponseE = new GetCaseListResponseE();
 		caseListResponseE.setGetCaseListResponse(caseListResponse);
 		return caseListResponseE;
 	}
 
 	/**
 	 * 
 	 * <p>Searches database for {@link Case} by given 
 	 * service provider id an {@link Case#getCaseIdentifier()}. 
 	 * If service provider id or case identifier not given, 
 	 * exception will be thrown.</p>
 	 * @param caseDetailsRequestE - request from client, which contains 
 	 * service provider id and {@link Case#getCaseIdentifier()}, 
 	 * not <code>null</code>;
 	 * @return response filled with data from {@link Case}.
 	 * @throws NullPointerException when service provider id not given
 	 * or nothing found by given case identifier.
 	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 	 */
 	public GetCaseDetailsResponseE getCaseDetails(
 			GetCaseDetailsE caseDetailsRequestE) {
 		if (caseDetailsRequestE == null) {
 			throw new NullPointerException(GetCaseDetailsE.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		GetCaseDetails caseDetails = caseDetailsRequestE.getGetCaseDetails();
 		if (caseDetails == null) {
 			throw new NullPointerException(GetCaseDetails.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		GetCaseDetailsRequest request = caseDetails.getRequest();
 		if (request == null) {
 			throw new NullPointerException(GetCaseDetailsRequest.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		String serviceProviderID = request.getServiceProviderId();
 		if (StringUtil.isEmpty(serviceProviderID)) {
 			throw new NullPointerException("Service provider ID " + 
 					"is null. Please provide correct service provider id!");
 		}
 		
 		String caseID = request.getCaseId();
 		if (StringUtil.isEmpty(caseID)) {
 			throw new NullPointerException("Case ID " + 
 					"is null. Please provide correct case id!");
 		}
 		
 		Case selectedCase = getCasesService(null).getCase(caseID);
 		if (selectedCase == null) {
 			throw new NullPointerException(Case.class.getName() + " by id: " +
 					caseID + " not found!");
 		}
 		
 		List<Long> ids = getCasesService(selectedCase)
 				.getStepIDs(selectedCase);
 		if (ListUtil.isEmpty(ids)) {
 			throw new NullPointerException(Case.class.getName() + " by id: " +
 					caseID + " does not have steps!");
 		}
 		
 		CaseDetails response = new CaseDetails();
 		for (long id : ids) {
 			CaseStep caseProcessingStep = new CaseStep();
 			
 			List<String> documentIds = getCasesService(selectedCase)
 					.getDocumentsIDs(selectedCase, id);
 			if (!ListUtil.isEmpty(documentIds)) {
 				caseProcessingStep.setDocumentId(
 						documentIds.toArray(new String[documentIds.size()]));
 			}
 
 			caseProcessingStep.setOfficial(getCasesService(selectedCase)
 					.getOfficialName(selectedCase));
 			caseProcessingStep.setOperationDate(getCasesService(selectedCase)
 					.getLastOperationDate(selectedCase, id));
 			caseProcessingStep.setOrgLabel(getLabelType(getOrganizationName(), null));
 			caseProcessingStep.setStatusLabel(getLabelType(
 					getCasesService(selectedCase).getStatus(selectedCase, id),
 					null));
 			caseProcessingStep.setStepId(String.valueOf(id));
 			caseProcessingStep.setStepName(getCasesService(selectedCase)
 					.getName(selectedCase, id));
 			caseProcessingStep.setStepUrl(
 					getCasesService(selectedCase).getStepURL(selectedCase, id));
 			caseProcessingStep.setSubmitted(
 					getCasesService(selectedCase).isSubmitted(selectedCase, id));
 			
 			response.addCaseProcessingStep(caseProcessingStep);
 		}
 				
 		GetCaseDetailsResponse caseDetailsResponse = new GetCaseDetailsResponse();
 		caseDetailsResponse.setResponse(response);
 		caseDetailsResponse.setRequest(request);
 		
 		GetCaseDetailsResponseE caseDetailsResponseE = new GetCaseDetailsResponseE();
 		caseDetailsResponseE.setGetCaseDetailsResponse(caseDetailsResponse);
 		
 		return caseDetailsResponseE;
 	}
 
 	/**
 	 * 
 	 * <p>Searches database for localized labels of {@link XForm} by given 
 	 * service provider id, language and {@link XForm#getFormId()}. 
 	 * If service provider id or case identifier not given, 
 	 * exception will be thrown.</p>
 	 * @param xFormLabelsE - request from client, which contains 
 	 * service provider id, language and {@link XForm#getFormId()}, 
 	 * not <code>null</code>;
 	 * @return response filled with labels from {@link XForm}.
 	 * @throws NullPointerException when service provider id not given
 	 * or nothing found by given {@link XForm} identifier.
 	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 	 */
 	public GetXFormLabelsResponseE getXFormLabels(GetXFormLabelsE xFormLabelsE) {
 		if (xFormLabelsE == null) {
 			throw new NullPointerException(GetXFormLabelsE.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		GetXFormLabels xformsLabels = xFormLabelsE.getGetXFormLabels();
 		if (xformsLabels == null) {
 			throw new NullPointerException(GetXFormLabels.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		GetXFormLabelsRequest request = xformsLabels.getRequest();
 		if (request == null) {
 			throw new NullPointerException(GetXFormLabelsRequest.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		String serviceProviderID = request.getServiceProviderId();
 		if (StringUtil.isEmpty(serviceProviderID)) {
 			throw new NullPointerException("Service provider ID " + 
 					"is null. Please provide correct service provider id!");
 		}
 		
 		String xformID = request.getXFormId();
 		if (StringUtil.isEmpty(xformID)) {
 			throw new NullPointerException("XForm ID " + 
 					"is null. Please provide correct xforms id!");
 		}
 
 		LangType language = request.getLang();
 		if (language == null) {
 			throw new NullPointerException("XForm language not provided. " + 
 					" Please provide labels language!");
 		}
 		
 		Map<String, String> labels = getXFormService()
 				.getXFormLabels(xformID, language.getLangType());
 		if (MapUtil.isEmpty(labels)) {
 			throw new NullPointerException("No labels for " + 
 					XForm.class.getName() + " by id: " + xformID);
 		}
 		
 		XFormLabel response = new XFormLabel();
 		for (String key : labels.keySet()) {
 			LabelPair labelPair = new LabelPair();
 			labelPair.setKey(key);
 			labelPair.setLabel(labels.get(key));
 			response.addLabelPairs(labelPair);
 		}	
 		
 		GetXFormLabelsResponse xFormLabelsResponse = new GetXFormLabelsResponse();
 		xFormLabelsResponse.setResponse(response);
 		xFormLabelsResponse.setRequest(request);
 		
 		GetXFormLabelsResponseE xFormsLabelsResponseE = new GetXFormLabelsResponseE();
 		xFormsLabelsResponseE.setGetXFormLabelsResponse(xFormLabelsResponse);
 		
 		return xFormsLabelsResponseE;
 	}
 
 	/**
 	 * 
 	 * @param notificationsRequestE is request from client, which contains 
 	 * personal id of {@link User}.
 	 * @return response filled with text about notifications
 	 * @throws NullPointerException when user id is not given or nothing found;
 	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 	 */
 	public GetNotificationsResponseE getNotifications(GetNotificationsE notificationsRequestE) {
 		if (notificationsRequestE == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					GetNotificationsE.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		GetNotifications notificationsRequest = notificationsRequestE.getGetNotifications();
 		if (notificationsRequest == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					GetNotifications.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		GetNotificationsRequest request = notificationsRequest.getRequest();
 		if (request == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					GetNotificationsRequest.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		String userId = request.getCitizenId();
 		if (StringUtil.isEmpty(userId)) {
 			throw new java.lang.NullPointerException(
 					"User id is not provided. Please provide correct request!");
 		}
 		
 		List<NotificationEntity> notificationEntities = getNotificationService().getNotificationEntities(userId);
 		if (notificationEntities == null) {
 			throw new java.lang.NullPointerException(
 					"No notifications found for user by id: " + userId);
 		}
 		
 		List<Notification> notifications = new ArrayList<Notification>(notificationEntities.size());
 		for (NotificationEntity notificationEntity: notificationEntities) {
 			Notification nt = new Notification();
 			nt.setNotificationId(String.valueOf(notificationEntity.getId()));
 			
 			User receiver = getUser(userId);
 			if (receiver != null) {
 				nt.setCitizenId(receiver.getPersonalID());
 			}
 			
 			nt.setOrgLabel(getLabelType(getOrganizationName(), null));
 			nt.setTextLabel(getLabelType(
 					notificationEntity.getMessage(), 
 					getCurrentLocale().getLanguage()));
 			
 			notifications.add(nt);
 		}
 		
 		NotificationsList response = new NotificationsList();		
 		response.setNotifications(notifications.toArray(
 				new Notification[notificationEntities.size()]));
 		
 		GetNotificationsResponse notificationsResponse = new GetNotificationsResponse();
 		notificationsResponse.setResponse(response);
 		notificationsResponse.setRequest(request);
 		
 		GetNotificationsResponseE notificationsResponseE = new GetNotificationsResponseE();
 		notificationsResponseE.setGetNotificationsResponse(notificationsResponse);
 
 		return notificationsResponseE;
 	}
 
 	/**
 	 * 
 	 * @param documentE - request with document id, 
 	 * which is {@link TaskInstance#getId()} and service provider id,
 	 * not <code>null</code>;
 	 * @return {@link Document} of XForm which is processed and 
 	 * filled with values;
 	 * @throws NullPointerException when request, document id, service provider 
 	 * id not found not found.
 	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 	 */
 	public GetDocumentResponseE getDocument(GetDocumentE documentE) {
 		if (documentE == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					GetDocumentE.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 				
 		GetDocument document = documentE.getGetDocument();
 		if (document == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					GetDocument.class.getName()  + 
 					" is null. Please provide correct request!");
 		}
 
 		GetDocumentRequest request = document.getRequest();
 		if (request == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					GetDocumentRequest.class.getName()  + 
 					" is null. Please provide correct request!");
 		}
 		
 		String serviceProviderId = request.getServiceProviderId();
 		if (StringUtil.isEmpty(serviceProviderId)) {
 			throw new java.lang.NullPointerException("No provider id is given, " +
 					"nothing to return.");
 		}
 		
 		String documentID = request.getDocumentId();
 		if (StringUtil.isEmpty(documentID)) {
 			throw new java.lang.NullPointerException("No document id is given, " +
 					"nothing to return.");
 		}
 
 		IWContext iwc = CoreUtil.getIWContext();
 		if (iwc == null) {
 			throw new java.lang.NullPointerException(
 					"Unable to get " + IWContext.class +
 					"no documents will be found. ");
 		}
 		
 		User administrator = null;
 		try {
 			administrator = iwc.getAccessController().getAdministratorUser();
 		} catch (Exception e1) {
 			throw new java.lang.NullPointerException(
 					"Unable to get administrator access, " +
 					"no documents will be found. ");
 		}
 		
 		try {
 			LoginBusinessBean.getDefaultLoginBusinessBean()
 				.logInByUUID(iwc.getRequest(), administrator.getUniqueId());
 		} catch (Exception e1) {
 			throw new java.lang.NullPointerException(
 					"Unable to login administrator user, " +
 					"no documents will be found. ");
 		}
 		
 		Document documentNode = getXFormService().getProcessedXFormDocument(
 				Long.valueOf(documentID));
 		if (documentNode == null) {
 			throw new java.lang.NullPointerException(
 					"No document by given ID: " + documentID + " found!");
 		}
 		
 		Document documentNodeTemplate = getXFormService().getXFormTemplate(
 				Long.valueOf(documentID));
 		if (documentNodeTemplate == null) {
 			throw new java.lang.NullPointerException(
 					"No document template by given ID: " + documentID + " found!");
 		}
 		
 		LoginBusinessBean.getDefaultLoginBusinessBean().logOutUser(iwc);
 		
 		byte[] documentBytes = XmlUtil.getBytes(documentNode);
 		if (documentBytes == null) {
 			throw new java.lang.NullPointerException(
 					"Failed to convert " + Document.class + 
 					" of XForm to byte array!");
 		}
 		
 		byte[] documentTemplateBytes = XmlUtil.getBytes(documentNodeTemplate);
 		if (documentTemplateBytes == null) {
 			throw new java.lang.NullPointerException(
 					"Failed to convert " + Document.class + 
 					" of XForm to byte array!");
 		}
 		
 		DataSource documentSource = new ByteArrayDataSource(
 				documentBytes, 
 				MimeTypeUtil.MIME_TYPE_XML);
 		
 		DataSource documentTemplateSource = new ByteArrayDataSource(
 				documentTemplateBytes, 
 				MimeTypeUtil.MIME_TYPE_XML);
 		
 		net.x_rd.ee.ehubservice.producer.Document response = new net.x_rd.ee.ehubservice.producer.Document();
 		response.setDocument(new DataHandler(documentSource));
 		response.setXFormsTemplate(new DataHandler(documentTemplateSource));
 		
 		GetDocumentResponse documentResponse = new GetDocumentResponse();
 		documentResponse.setResponse(response);
 		documentResponse.setRequest(request);
 		
 		GetDocumentResponseE documentResponseE = new GetDocumentResponseE();
 		documentResponseE.setGetDocumentResponse(documentResponse);
 
 		return documentResponseE;
 	}
 
 	/**
 	 * <p>Method lists all methods in {@link EhubserviceServiceSkeletonInterface}.</p>
 	 * @return response with method names available in 
 	 * {@link EhubserviceServiceSkeletonInterface};
 	 * @throws NullPointerException if correct request is not provided;
 	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 	 */
 	public AllowedMethodsResponseE allowedMethods(AllowedMethods allowedMethods) {
 		if (allowedMethods == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					AllowedMethods.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 
 		Method[] methods = EhubserviceServiceSkeletonInterface.class.getDeclaredMethods();
 		if (ArrayUtil.isEmpty(methods)) {
 			throw new NullPointerException("No methods found in this service!");
 		}
 
 		AllowedMethodList allowedMethod = new AllowedMethodList();
 		for (Method method : methods) {
 			allowedMethod.addItem(method.getName());
 		}
 
 		AllowedMethodsResponse response = new AllowedMethodsResponse();
 		response.setResponse(allowedMethod);
 
 		AllowedMethodsResponseE allowedMethodsResponse = new AllowedMethodsResponseE();
 		allowedMethodsResponse.setAllowedMethodsResponse(response);
 
 		return allowedMethodsResponse;
 	}
 	
 	/**
 	 * 
 	 * <p>Searches database for available {@link ServiceEntity} by given 
 	 * provider id. If provider id not givent, exception will be thrown.</p>
 	 * @param serviceListRequestE - request form service, which contains 
 	 * provider id, not <code>null</code>;
 	 * @return response filled with data from {@link Collection} of 
 	 * {@link ServiceEntity}s.
 	 * @throws NullPointerException when service provider id not given
 	 * ot nothing found by given provider id.
 	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 	 */
 	public GetServiceListResponseE getServiceList(GetServiceListE serviceListRequestE) {
 		if (serviceListRequestE == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					GetServiceListE.class.getName()  + " is null, probably request is " +
 							"not provided.");
 		}
 				
 		GetServiceList serviceListRequest = serviceListRequestE.getGetServiceList();
 		if (serviceListRequest == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					GetServiceList.class.getName()  + " is null, probably request " +
 							" does not have required service list.");
 		}
 
 		GetServiceListRequest request = serviceListRequest.getRequest();
 		if (request == null) {
 			throw new java.lang.NullPointerException(
 					GetServiceListRequest.class.getName()  + " is null.");
 		}
 		
 		String serviceProviderId = request.getServiceProviderId();
 		if (StringUtil.isEmpty(serviceProviderId)) {
 			throw new java.lang.NullPointerException("No provider id is given, " +
 					"nothing to return.");
 		}
 		
 		Collection<Application> services = getApplicationService().getServices();
 		if (ListUtil.isEmpty(services)) {
 			throw new java.lang.NullPointerException("No services found on this system!");
 		}
 		
 		ServiceList response = new ServiceList();
 		for (Application service : services) {
 			
 			/* Setting service id */
 			ServiceEntry serviceEntry = new ServiceEntry();
 			serviceEntry.setId(service.getPrimaryKey().toString());
 			
 			/* Setting label */
 			LabelType labelType = new LabelType();
 			LangType langType = new LangType();
 			langType.setLangType(CoreUtil.getCurrentLocale().getLanguage());
 			labelType.setLang(langType);
 			labelType.setText(service.getName());
 			serviceEntry.setNameLabel(labelType);
 			
 			/* Setting application icon */
 			URL icon = getApplicationService().getServiceLogoURL(service);
 			if (icon != null) {
				serviceEntry.setIcon(new DataHandler(icon));
 			}
			
 			response.addServiceEntries(serviceEntry);
 		}
 		
 		GetServiceListResponse serviceListResponse = new GetServiceListResponse();
 		serviceListResponse.setResponse(response);
 		serviceListResponse.setRequest(request);
 		
 		GetServiceListResponseE serviceListResponseE = new GetServiceListResponseE();
 		serviceListResponseE.setGetServiceListResponse(serviceListResponse);		
 
 		return serviceListResponseE;
 	}
 	
 	/**
 	 * 
 	 * <p>Searches data source for available messages by given 
 	 * service provider id and {@link User} personal id.</p>
 	 * @param messagesListRequestE - request form service, which contains 
 	 * provider id and {@link User} personal ID, not <code>null</code>;
 	 * @return response filled with data from {@link Collection} of 
 	 * {@link UserMessage}s.
 	 * @throws NullPointerException when service provider id, 
 	 * {@link User} personal id not given or nothing found by given credentials.
 	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 	 */
 	public GetMessagesListResponseE getMessagesList(GetMessagesListE messagesListRequestE) {
 		if (messagesListRequestE == null) {
 			throw new NullPointerException(GetMessagesListE.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		GetMessagesList messagesListRequest = messagesListRequestE.getGetMessagesList();
 		if (messagesListRequest == null) {
 			throw new NullPointerException(GetMessagesList.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		GetMessagesListRequest request = messagesListRequest.getRequest();
 		if (request == null) {
 			throw new NullPointerException(GetMessagesListRequest.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		String serviceProviderID = request.getServiceProviderId();
 		if (StringUtil.isEmpty(serviceProviderID)) {
 			throw new NullPointerException("Service provider ID " + 
 					"is null. Please provide correct service provider id!");
 		}
 		
 		String personalID = request.getCitizenId();
 		if (StringUtil.isEmpty(personalID)) {
 			throw new NullPointerException("Personal ID of " + User.class + 
 					"is null. Please provide correct personal id!");
 		}
 		
 		Collection<UserMessage> messages = getCasesService(null).getMessages(personalID);
 		if (ListUtil.isEmpty(messages)) {
 			throw new NullPointerException("No messages found for " + 
 					User.class.getName() + " by personal id: " + personalID);
 		}
 		
 		MessagesList response = new MessagesList();
 		for (UserMessage message : messages) {
 			Message messageType = new Message();
 			messageType.setBody(message.getBody());
 			messageType.setDate(message.getCreated());
 			messageType.setId(message.getId());
 			messageType.setOfficial(getCasesService(message).getOfficialName(message));
 			messageType.setOrgLabel(getLabelType(getOrganizationName(), null));
 			messageType.setSubject(message.getSubject());
 			messageType.setRead(message.isRead());
 
 			response.addMessages(messageType);
 		}
 		
 		GetMessagesListResponse messagesListResponse = new GetMessagesListResponse();
 		messagesListResponse.setResponse(response);
 		messagesListResponse.setRequest(request);
 		
 		GetMessagesListResponseE messagesListResponseE = new GetMessagesListResponseE();
 		messagesListResponseE.setGetMessagesListResponse(messagesListResponse);
 		
 		return messagesListResponseE;
 	}
 
 	/**
 	 * TODO
 	 */
 	public SubmitDocumentResponseE submitDocument(SubmitDocumentE submitDocumentE) {
 		if (submitDocumentE == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					SubmitDocumentE.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 		
 		SubmitDocument submitDocument = submitDocumentE.getSubmitDocument();
 		if (submitDocument == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					SubmitDocument.class.getName()  + 
 					" is null. Please provide correct request!");
 		}
 
 		SubmitDocumentRequest request = submitDocument.getRequest();
 		if (request == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					SubmitDocumentRequest.class.getName()  + 
 					" is null. Please provide correct request!");
 		}
 
 		String serviceProviderId = request.getServiceId();
 		if (StringUtil.isEmpty(serviceProviderId)) {
 			throw new java.lang.NullPointerException("No provider id is not " +
 					"given. Form won't be subbmitted!");
 		}
 
 		String citizenID = request.getCitizenId();
 		if (StringUtil.isEmpty(citizenID)) {
 			throw new java.lang.NullPointerException("No citizen id is given. " +
 					"Form won't be subbmitted!");
 		}
 
 		String stepID = request.getStepId();
 		if (StringUtil.isEmpty(stepID)) {
 			throw new java.lang.NullPointerException("No citizen id is given. " +
 					"Form won't be subbmitted!");
 		}
 		
 		DataHandler documentHandler = request.getDocument();
 		if (documentHandler == null) {
 			throw new java.lang.NullPointerException("Document not given. " +
 					"Form won't be subbmitted!");
 		}
 				
 		String languageID = request.getLanguageId();
 
 		// TODO
 		
 		LabelType submissionStatusLabel = null; //getLabelType(status, language);
 		LabelType caseNameLabel = null; //getLabelType(caseDescription, language);
 		
 		SubmittedDocumentInfo response = new SubmittedDocumentInfo();
 		response.setCaseNameLabel(caseNameLabel);
 		response.setSubmissionStatusLabel(submissionStatusLabel);
 		
 		SubmitDocumentResponse submitDocumentResponse = new SubmitDocumentResponse();
 		submitDocumentResponse.setResponse(response);
 		submitDocumentResponse.setRequest(request);
 		
 		SubmitDocumentResponseE submitDocumentResponseE = new SubmitDocumentResponseE();
 		submitDocumentResponseE.setSubmitDocumentResponse(submitDocumentResponse);
 		
 		return submitDocumentResponseE;
 	}
 
 	/**
 	 * 
 	 * <p>Retrieves {@link Document} of {@link XForm} by stepID filled with
 	 * data found by citizenID and in given language. If stepID not provided
 	 * then new process will be created and first form in process will 
 	 * be returned. If language not provided, then default language will be 
 	 * used.</p>
 	 * @param prefilledDocumentE request with citizenID, serviceID and, 
 	 * if required, language and stepID, not <code>null</code>
 	 * @return {@link Document} of {@link XForm} filled with data by given 
 	 * {@link User#getPersonalID()}.
 	 * @throws NullPointerException when service id or user personal ID is not 
 	 * provided.
 	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
 	 */
 	public GetPrefilledDocumentResponseE getPrefilledDocument(
 			GetPrefilledDocumentE prefilledDocumentE) {
 		if (prefilledDocumentE == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					GetPrefilledDocumentE.class.getName() + 
 					" is null. Please provide correct request!");
 		}
 				
 		GetPrefilledDocument prefilledDocument = prefilledDocumentE.getGetPrefilledDocument();
 		if (prefilledDocument == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					GetPrefilledDocument.class.getName()  + 
 					" is null. Please provide correct request!");
 		}
 
 		GetPrefilledDocumentRequest request = prefilledDocument.getRequest();
 		if (request == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					GetPrefilledDocumentRequest.class.getName()  + 
 					" is null. Please provide correct request!");
 		}
 		
 		String applicationID = request.getServiceId();
 		if (StringUtil.isEmpty(applicationID)) {
 			throw new java.lang.NullPointerException("No provider id is given, " +
 					"nothing to return.");
 		}
 		
 		String personalID = request.getCitizenId();
 		if (StringUtil.isEmpty(personalID)) {
 			throw new java.lang.NullPointerException("No user personal is given, " +
 					"nothing to return.");
 		}
 		
 		String language = null;
 		LangType languageType = request.getLanguageId();
 		if (languageType != null) {
 			language = languageType.getLangType();
 		}
 		
 		String taskID = request.getStepId();
 		
 		IWContext iwc = CoreUtil.getIWContext();
 		if (iwc == null) {
 			throw new java.lang.NullPointerException(
 					"Failed to get " + IWContext.class);
 		}
 		
 		try {
 			LoginBusinessBean.getDefaultLoginBusinessBean()
 					.logInByPersonalID(iwc, personalID);
 		} catch (Exception e) {
 			throw new java.lang.NullPointerException(
 					"Failed to log on user by personal ID: " + personalID);
 		}
 	
 		if (!iwc.isLoggedOn()) {
 			throw new java.lang.NullPointerException(
 					"You should be logged on to retrieve document!");
 		}
 		
 		Document documentNode = getXFormService().getPrefilledDocument(
 				personalID, applicationID, taskID, language);
 		if (documentNode == null) {
 			throw new java.lang.NullPointerException(
 					"No document created for user by personal ID: " + personalID);
 		}
 		
 		LoginBusinessBean.getDefaultLoginBusinessBean().logOutUser(iwc);
 		
 		byte[] documentBytes = XmlUtil.getBytes(documentNode);
 		if (documentBytes == null) {
 			throw new java.lang.NullPointerException(
 					"Failed to convert " + Document.class + 
 					" of XForm to byte array!");
 		}
 		
 		DataSource documentSource = new ByteArrayDataSource(
 				documentBytes, 
 				MimeTypeUtil.MIME_TYPE_XML);
 		
 		PreffiledDocument response = new PreffiledDocument();
 		response.setDocument(new DataHandler(documentSource));
 		response.setDocumentType(MimeTypeUtil.MIME_TYPE_XML);
 		
 		GetPrefilledDocumentResponse prefilledDocumentResponse = new GetPrefilledDocumentResponse();
 		prefilledDocumentResponse.setRequest(request);
 		prefilledDocumentResponse.setResponse(response);
 		
 		GetPrefilledDocumentResponseE prefilledDocumentResponseE = new GetPrefilledDocumentResponseE();
 		prefilledDocumentResponseE.setGetPrefilledDocumentResponse(prefilledDocumentResponse);
 		return prefilledDocumentResponseE;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.idega.xroad.service.wsdl.EhubserviceServiceSkeletonInterface#markNotificationAsRead(net.x_rd.ee.ehubservice.producer.MarkNotificationAsReadE)
 	 */
 	@Override
 	public MarkNotificationAsReadResponseE markNotificationAsRead(
 			MarkNotificationAsReadE markNotificationAsReadE) {
 		if (markNotificationAsReadE == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					MarkNotificationAsReadE.class.getName()  + 
 					" is null. Please provide correct request!");
 		}
 
 		MarkNotificationAsRead notificationInfo = markNotificationAsReadE.getMarkNotificationAsRead();
 		if (notificationInfo == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					MarkNotificationAsRead.class.getName()  + 
 					" is null. Please provide correct request!");
 		}
 
 		MarkNotificationAsReadRequest request = notificationInfo.getRequest();
 		if (request == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					MarkNotificationAsReadRequest.class.getName()  + 
 					" is null. Please provide correct request!");
 		}
 		
 		String notificationId = request.getNotificationId();
 		if (StringUtil.isEmpty(notificationId)) {
 			throw new java.lang.NullPointerException("Notification id not provided!");
 		}
 
 		
 		NotificationReceiverEntity notification = getNotificationEntityDAO().update(
 				Long.valueOf(notificationId), null, null, 
 				request.getMarkAsRead());
 		
 		MarkResult response = new MarkResult();
 		response.setSuccess(notification != null);
 		if (response.getSuccess()) {
 			response.setMessage("Notification by id: '" + notificationId + "' successfully updated!");
 		} else {
 			response.setMessage("Failed to update notification by id: '" + notificationId + "'!");
 		}
 		
 
 		MarkNotificationAsReadResponse markNotificationAsReadResponse = new MarkNotificationAsReadResponse();
 		markNotificationAsReadResponse.setRequest(request);
 		markNotificationAsReadResponse.setResponse(response);
 
 		MarkNotificationAsReadResponseE markNotificationAsReadResponseE = new MarkNotificationAsReadResponseE();
 		markNotificationAsReadResponseE.setMarkNotificationAsReadResponse(markNotificationAsReadResponse);
 		return markNotificationAsReadResponseE;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.idega.xroad.service.wsdl.EhubserviceServiceSkeletonInterface#markCaseAsRead(net.x_rd.ee.ehubservice.producer.MarkCaseAsReadE)
 	 */
 	@Override
 	public MarkCaseAsReadResponseE markCaseAsRead(MarkCaseAsReadE markCaseAsReadE) {
 		if (markCaseAsReadE == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					MarkCaseAsReadE.class.getName()  + 
 					" is null. Please provide correct request!");
 		}
 
 		MarkCaseAsRead markCaseAsRead = markCaseAsReadE.getMarkCaseAsRead();
 		if (markCaseAsRead == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					MarkCaseAsRead.class.getName()  + 
 					" is null. Please provide correct request!");
 		}
 
 		MarkCaseAsReadRequest request = markCaseAsRead.getRequest();
 		if (request == null) {
 			throw new java.lang.NullPointerException("Request of class " +
 					MarkCaseAsReadRequest.class.getName()  + 
 					" is null. Please provide correct request!");
 		}
 
 		String caseId = request.getCaseId();
 		if (StringUtil.isEmpty(caseId)) {
 			throw new java.lang.NullPointerException("Case id not provided!");
 		}
 
 		Case theCase = null;
 		try {
 			theCase = IDOLookup.findByPrimaryKey(Case.class, Integer.valueOf(caseId));
 		} catch (Exception e) {
 			throw new NullPointerException(Case.class.getName() + " not found by primary key: '" + caseId + "'");
 		}
 
 		theCase.setRead(request.getMarkAsRead());
 		theCase.store();
 
 		MarkResult response = new MarkResult();
 		response.setSuccess(theCase.isRead().booleanValue() == request.getMarkAsRead());
 		if (response.getSuccess()) {
 			response.setMessage(Case.class.getName() + " by primary key: '" + caseId + "' successfully updated!");
 		} else {
 			response.setMessage("Failed to update " + Case.class.getName() + " by primary key: '" + caseId + "'");
 		}
 		
 
 		MarkCaseAsReadResponse markCaseAsReadResponse = new MarkCaseAsReadResponse();
 		markCaseAsReadResponse.setRequest(request);
 		markCaseAsReadResponse.setResponse(response);
 
 		MarkCaseAsReadResponseE markCaseAsReadResponseE = new MarkCaseAsReadResponseE();
 		markCaseAsReadResponseE.setMarkCaseAsReadResponse(markCaseAsReadResponse);
 		return markCaseAsReadResponseE;
 	}
 
 	protected LabelType getLabelType(String label, String language) {
 		if (StringUtil.isEmpty(label)) {
 			return null;
 		}
 		
 		if (StringUtil.isEmpty(language)) {
 			language = CoreUtil.getCurrentLocale().getLanguage();
 		}
 		
 		LabelType labelType = new LabelType();
 		labelType.setText(label);
 		
 		LangType langType = new LangType();
 		langType.setLangType(language);
 		labelType.setLang(langType);
 		
 		return labelType;
 	}
 	
 	protected String getOrganizationName() {
 		IWMainApplicationSettings settings = IWMainApplication
 				.getDefaultIWMainApplication().getSettings();
 		if (settings == null) {
 			return null;
 		}
 		
 		return settings.getProperty(CoreConstants.ORGANIZATION_NAME, "Idega");
 	}
 	
 	protected CasesService getCasesService(Case theCase) {		
 		if (theCase == null || StringUtil.isEmpty(theCase.getCaseManagerType())) {
 			if (this.legacyCasesService == null) {
 				ELUtil.getInstance().autowire(this);
 			}
 			
 			return this.legacyCasesService;
 		} else {
 			if (this.bpmCasesService == null) {
 				ELUtil.getInstance().autowire(this);
 			}
 			
 			return this.bpmCasesService;
 		}
 	}
 	
 	protected XFormService getXFormService() {
 		if (this.xFormService == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 		
 		return this.xFormService;
 	}
 	
 	protected ApplicationService getApplicationService() {
 		if (this.applicationService == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 		
 		return this.applicationService;
 	}
 	
 	protected NotificationService getNotificationService() {
 		if (this.notificationService == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 		
 		return this.notificationService;
 	}
 
 	protected NotificationEntityDAO getNotificationEntityDAO() {
 		if (this.notificationEntityDAO == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 
 		return this.notificationEntityDAO;
 	}
 
 	protected com.idega.user.business.UserBusiness getUserBusiness() {
 		if (this.userBussiness != null) {
 			return this.userBussiness;
 		}
 		
 		try {
 			this.userBussiness = IBOLookup.getServiceInstance(
 					getApplication().getIWApplicationContext(), 
 					com.idega.user.business.UserBusiness.class);
 		} catch (IBOLookupException e) {
 			getLogger().log(Level.WARNING, "Unable to get " + 
 					com.idega.user.business.UserBusiness.class + 
 					" cause of: ", e);
 		}
 		
 		return this.userBussiness;
 	}
 	
 	protected User getUser(String userId) {
 		try {
 			return getUserBusiness().getUser(userId);
 		} catch (RemoteException e) {
 			getLogger().log(Level.WARNING, 
 					"Failed to connect data source: ", e);
 		} catch (FinderException e) {
 			getLogger().log(Level.WARNING, User.class + " by personal id: " + 
 					userId + " not found. Trying by primary key...");
 		}
 		
 		try {
 			return getUserBusiness().getUser(Integer.valueOf(userId));
 		} catch (NumberFormatException e) {
 			getLogger().log(Level.WARNING, 
 					"Failed to connect data source: ", e);
 		} catch (RemoteException e) {
 			getLogger().log(Level.WARNING, User.class + " by primary key: " + 
 					userId + " not found!");
 		}
 		
 		return null;
 	}
 }
