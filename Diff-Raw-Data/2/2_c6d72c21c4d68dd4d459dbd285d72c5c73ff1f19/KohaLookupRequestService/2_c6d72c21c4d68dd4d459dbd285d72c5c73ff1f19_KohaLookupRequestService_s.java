 package org.extensiblecatalog.ncip.v2.koha;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.extensiblecatalog.ncip.v2.koha.util.KohaConstants;
 import org.extensiblecatalog.ncip.v2.koha.util.KohaException;
 import org.extensiblecatalog.ncip.v2.koha.util.KohaRemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.koha.util.KohaUtil;
 import org.extensiblecatalog.ncip.v2.service.AgencyId;
 import org.extensiblecatalog.ncip.v2.service.ItemId;
 import org.extensiblecatalog.ncip.v2.service.LookupRequestInitiationData;
 import org.extensiblecatalog.ncip.v2.service.LookupRequestResponseData;
 import org.extensiblecatalog.ncip.v2.service.LookupRequestService;
 import org.extensiblecatalog.ncip.v2.service.PickupLocation;
 import org.extensiblecatalog.ncip.v2.service.Problem;
 import org.extensiblecatalog.ncip.v2.service.ProblemType;
 import org.extensiblecatalog.ncip.v2.service.RemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.service.RequestElementType;
 import org.extensiblecatalog.ncip.v2.service.RequestId;
 import org.extensiblecatalog.ncip.v2.service.ResponseHeader;
 import org.extensiblecatalog.ncip.v2.service.ServiceContext;
 import org.extensiblecatalog.ncip.v2.service.ServiceException;
 import org.extensiblecatalog.ncip.v2.service.UserId;
 import org.extensiblecatalog.ncip.v2.service.Version1ItemIdentifierType;
 import org.extensiblecatalog.ncip.v2.service.Version1RequestElementType;
 import org.extensiblecatalog.ncip.v2.service.Version1RequestScopeType;
 import org.extensiblecatalog.ncip.v2.service.Version1RequestStatusType;
 import org.extensiblecatalog.ncip.v2.service.Version1UserIdentifierType;
 import org.json.simple.JSONObject;
 import org.xml.sax.SAXException;
 
 public class KohaLookupRequestService implements LookupRequestService {
 
 	@Override
 	public LookupRequestResponseData performService(LookupRequestInitiationData initData, ServiceContext serviceContext, RemoteServiceManager serviceManager)
 			throws ServiceException {
 
 		final LookupRequestResponseData responseData = new LookupRequestResponseData();
 
 		boolean requestIdIsEmpty = initData.getRequestId() == null || initData.getRequestId().getRequestIdentifierValue().isEmpty();
 		boolean itemIdIsEmpty = initData.getItemId() == null || initData.getItemId().getItemIdentifierValue().isEmpty();
 		boolean userIdIsEmpty = initData.getUserId() == null || initData.getUserId().getUserIdentifierValue().isEmpty();
 
 		if (requestIdIsEmpty && (itemIdIsEmpty || userIdIsEmpty)) {
 			String description = "Cannot lookup unknown request. Please specify RequestId or both ItemId & UserId";
 
 			if (requestIdIsEmpty) {
 				responseData.setProblems(Arrays.asList(new Problem(new ProblemType("Request Id is undefined."), null, null, description)));
 			} else {
 				List<Problem> problems = new ArrayList<Problem>();
 
 				if (itemIdIsEmpty) {
 
 					Problem p = new Problem(new ProblemType("Item id is undefined."), null, null, description);
 					problems.add(p);
 
 				}
 				if (userIdIsEmpty) {
 
 					Problem p = new Problem(new ProblemType("User Id is undefined."), null, null, description);
 					problems.add(p);
 				}
 				responseData.setProblems(problems);
 			}
 		} else {
 
 			KohaRemoteServiceManager kohaRemoteServiceManager = (KohaRemoteServiceManager) serviceManager;
 
 			boolean somethingSpecialDesired = initData.getInitiationHeader() != null && initData.getInitiationHeader().getApplicationProfileType() != null;
 
 			try {
 				if (!somethingSpecialDesired) {
 
 					JSONObject requestItem = kohaRemoteServiceManager.lookupRequest(initData, !requestIdIsEmpty);
 					updateRegularResponseData(responseData, initData, requestItem);
 
 				} else {
 					String desiredSpeciality = initData.getInitiationHeader().getApplicationProfileType().getValue();
 
 					if (desiredSpeciality != null && !desiredSpeciality.trim().isEmpty()) {
 						if (desiredSpeciality.equals(KohaConstants.APP_PROFILE_TYPE_CAN_BE_RENEWED)) {
 
 							JSONObject canBeRenewed = kohaRemoteServiceManager.canBeRenewed(initData);
 							updateCanBeRenewedResponseData(responseData, initData, canBeRenewed);
 
 						} else if (desiredSpeciality.equals(KohaConstants.APP_PROFILE_TYPE_CAN_BE_REQUESTED)) {
 
 							JSONObject canBeRequested = kohaRemoteServiceManager.canBeRequested(initData);
 							updateCanBeRequestedResponseData(responseData, initData, canBeRequested);
 
 						} else {
 							Problem p = new Problem(new ProblemType("Unrecognized value"), null, "InitiationHeader's ApplicationProfileType has an unknown value.");
 							responseData.setProblems(Arrays.asList(p));
 						}
 					} else {
 						Problem p = new Problem(new ProblemType("Empty value"), null, "InitiationHeader's ApplicationProfileType has an empty value.");
 						responseData.setProblems(Arrays.asList(p));
 					}
 				}
 			} catch (IOException ie) {
 				Problem p = new Problem(new ProblemType("Processing IOException error."), ie.getMessage(), "Are you connected to the Internet/Intranet?");
 				responseData.setProblems(Arrays.asList(p));
 			} catch (SAXException se) {
 				Problem p = new Problem(new ProblemType("Processing SAXException error."), null, se.getMessage());
 				responseData.setProblems(Arrays.asList(p));
 			} catch (KohaException ke) {
 				Problem p = new Problem(new ProblemType(ke.getShortMessage()), null, ke.getMessage());
 				responseData.setProblems(Arrays.asList(p));
 			} catch (ParserConfigurationException pce) {
 				Problem p = new Problem(new ProblemType("Processing ParserConfigurationException error."), null, pce.getMessage());
 				responseData.setProblems(Arrays.asList(p));
 			} catch (ServiceException e) {
 				throw e;
 			} catch (Exception e) {
 				Problem p = new Problem(new ProblemType("Unknown processing exception error."), null, e.getMessage());
 				responseData.setProblems(Arrays.asList(p));
 			}
 		}
 
 		return responseData;
 	}
 
 	private void updateRegularResponseData(LookupRequestResponseData responseData, LookupRequestInitiationData initData, JSONObject requestItem) throws ParseException {
 
 		ResponseHeader responseHeader = KohaUtil.reverseInitiationHeader(initData);
 
 		if (responseHeader != null)
 			responseData.setResponseHeader(responseHeader);
 
 		String branchCode = (String) requestItem.get("branchcode");
 
 		for (RequestElementType desiredService : initData.getRequestElementTypes()) {
 
 			if (desiredService.equals(Version1RequestElementType.DATE_OF_USER_REQUEST)) {
 				responseData.setDateOfUserRequest(KohaUtil.parseGregorianCalendarFromKohaLongDate((String) requestItem.get("timestamp")));
 			} else if (desiredService.equals(Version1RequestElementType.HOLD_QUEUE_POSITION)) {
 				BigDecimal holdQueuePosition = new BigDecimal((String) requestItem.get("priority"));
 				responseData.setHoldQueuePosition(holdQueuePosition);
 			} else if (desiredService.equals(Version1RequestElementType.PICKUP_LOCATION) && branchCode != null) {
 				responseData.setPickupLocation(new PickupLocation(branchCode));
 			} else if (desiredService.equals(Version1RequestElementType.REQUEST_SCOPE_TYPE)) {
 				if (requestItem.get("itemnumber") != null)
 					responseData.setRequestScopeType(Version1RequestScopeType.ITEM);
 				else
 					responseData.setRequestScopeType(Version1RequestScopeType.BIBLIOGRAPHIC_ITEM);
 			} else if (desiredService.equals(Version1RequestElementType.REQUEST_TYPE)) {
 				responseData.setRequestType(initData.getRequestType());
 			} else if (desiredService.equals(Version1RequestElementType.USER_ID)) {
 				UserId userId = new UserId();
 				userId.setUserIdentifierType(Version1UserIdentifierType.INSTITUTION_ID_NUMBER);
 				userId.setUserIdentifierValue((String) requestItem.get("borrowernumber"));
 				if (branchCode != null)
 					userId.setAgencyId(new AgencyId(branchCode));
 				responseData.setUserId(userId);
 			} else if (desiredService.equals(Version1RequestElementType.PICKUP_EXPIRY_DATE)) {
 				responseData.setPickupExpiryDate(KohaUtil.parseGregorianCalendarFromKohaDate((String) requestItem.get("expirationdate")));
 			} else if (desiredService.equals(Version1RequestElementType.EARLIEST_DATE_NEEDED)) {
 				responseData.setEarliestDateNeeded(KohaUtil.parseGregorianCalendarFromKohaDate((String) requestItem.get("reservedate")));
 			} else if (desiredService.equals(Version1RequestElementType.REQUEST_STATUS_TYPE)) {
 				if (requestItem.get("waitingdate") != null)
 					responseData.setRequestStatusType(Version1RequestStatusType.AVAILABLE_FOR_PICKUP);
 				else
 					responseData.setRequestStatusType(Version1RequestStatusType.IN_PROCESS);
 			} else if (desiredService.equals(Version1RequestElementType.DATE_AVAILABLE)) {
 				if (requestItem.get("waitingdate") != null)
 					responseData.setDateAvailable(KohaUtil.parseGregorianCalendarFromKohaDate((String) requestItem.get("waitingdate")));
 				else if (requestItem.get("onloanuntil") != null)
 					responseData.setDateAvailable(KohaUtil.parseGregorianCalendarFromKohaLongDate((String) requestItem.get("onloanuntil")));
 				else
 					responseData.setDateAvailable(KohaUtil.parseDateAvailableFromHoldingBranch(branchCode));
 			}
 		}
 
 		responseData.setRequestId(initData.getRequestId());
 
 		ItemId itemId = new ItemId();
 		itemId.setItemIdentifierValue((String) requestItem.get("itemnumber"));
 		itemId.setItemIdentifierType(Version1ItemIdentifierType.ACCESSION_NUMBER);
 
 		RequestId requestId = new RequestId();
 		requestId.setRequestIdentifierValue((String) requestItem.get("reserve_id"));
 
 		if (branchCode != null) {
 			itemId.setAgencyId(new AgencyId(branchCode));
 			requestId.setAgencyId(new AgencyId(branchCode));
 		}
 
 		responseData.setRequestId(requestId);
 		responseData.setItemId(itemId);
 
 	}
 
 	private void updateCanBeRenewedResponseData(LookupRequestResponseData responseData, LookupRequestInitiationData initData, JSONObject canBeRenewedResponse)
 			throws ParseException {
 		String allowed = (String) canBeRenewedResponse.get("allowed");
 
 		if (allowed.equals("y")) {
 			String maxDateDue = (String) canBeRenewedResponse.get("maxDateDue");
 			if (maxDateDue != null) {
 				responseData.setNeedBeforeDate(KohaUtil.parseGregorianCalendarFromKohaDateWithBackslashes(maxDateDue));
 			}
 			responseData.setItemId(initData.getItemId());
 		} else {
 			Problem p = new Problem(new ProblemType("Not allowed"), null, "Desired item/bib id cannot be renewed.");
 			responseData.setProblems(Arrays.asList(p));
 		}
 	}
 
 	private void updateCanBeRequestedResponseData(LookupRequestResponseData responseData, LookupRequestInitiationData initData, JSONObject canBeRequestedResponse) {
 		String allowed = (String) canBeRequestedResponse.get("allowed");
 
 		if (allowed.equals("y")) {
 			responseData.setItemId(initData.getItemId());
 		} else {
			Problem p = new Problem(new ProblemType("Not allowed"), null, "Desired item/bib id cannot be renewed.");
 			responseData.setProblems(Arrays.asList(p));
 		}
 	}
 }
