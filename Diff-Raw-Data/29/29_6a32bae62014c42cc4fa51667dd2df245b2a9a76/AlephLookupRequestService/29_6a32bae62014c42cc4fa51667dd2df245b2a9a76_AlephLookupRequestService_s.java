 package org.extensiblecatalog.ncip.v2.aleph;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephException;
 import org.extensiblecatalog.ncip.v2.aleph.util.AlephRemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.item.AlephRequestItem;
 import org.extensiblecatalog.ncip.v2.service.*;
 import org.xml.sax.SAXException;
 
 public class AlephLookupRequestService implements LookupRequestService {
 
 	@Override
 	public LookupRequestResponseData performService(LookupRequestInitiationData initData, ServiceContext serviceContext, RemoteServiceManager serviceManager)
 			throws ServiceException {
 
		boolean itemIdIsEmpty = initData.getItemId().getItemIdentifierValue().isEmpty();
		boolean userIdIsEmpty = initData.getUserId().getUserIdentifierValue().isEmpty();
 
 		if (itemIdIsEmpty || userIdIsEmpty) {
 			if (itemIdIsEmpty)
 				throw new ServiceException(
 						ServiceError.UNSUPPORTED_REQUEST,
 						"Item Id is undefined. Cannot lookup request for an unknown item. Note that Request Id is also not supported by this service. It's because Aleph cannot search for an request without the knowledge of UserId.");
 			else
 				throw new ServiceException(ServiceError.UNSUPPORTED_REQUEST, "User Id is undefined. Cannot lookup request with unknown user.");
 		}
 
 		AlephRemoteServiceManager alephRemoteServiceManager = (AlephRemoteServiceManager) serviceManager;
 
 		final LookupRequestResponseData responseData = new LookupRequestResponseData();
 
 		try {
 			AlephRequestItem requestItem = alephRemoteServiceManager.lookupRequest(initData);
 
 			if (requestItem.getProblem() == null) {
 				updateResponseData(responseData, initData, requestItem);
 			} else
 				responseData.setProblems(Arrays.asList(requestItem.getProblem()));
 
 		} catch (IOException ie) {
 			Problem p = new Problem(new ProblemType("Processing IOException error."), null, ie.getMessage());
 			responseData.setProblems(Arrays.asList(p));
 		} catch (SAXException se) {
 			Problem p = new Problem(new ProblemType("Processing SAXException error."), null, se.getMessage());
 			responseData.setProblems(Arrays.asList(p));
 		} catch (AlephException ae) {
 			Problem p = new Problem(new ProblemType("Processing AlephException error."), null, ae.getMessage());
 			responseData.setProblems(Arrays.asList(p));
 		} catch (ParserConfigurationException pce) {
 			Problem p = new Problem(new ProblemType("Processing ParserConfigurationException error."), null, pce.getMessage());
 			responseData.setProblems(Arrays.asList(p));
 		} catch (Exception e) {
 			Problem p = new Problem(new ProblemType("Unknown processing exception error."), null, e.getMessage());
 			responseData.setProblems(Arrays.asList(p));
 		}
 		return responseData;
 	}
 
 	private void updateResponseData(LookupRequestResponseData responseData, LookupRequestInitiationData initData, AlephRequestItem requestItem) {
 
 		InitiationHeader initiationHeader = initData.getInitiationHeader();
 		if (initiationHeader != null) {
 			ResponseHeader responseHeader = new ResponseHeader();
 			if (initiationHeader.getFromAgencyId() != null && initiationHeader.getToAgencyId() != null) {
 				responseHeader.setFromAgencyId(initiationHeader.getFromAgencyId());
 				responseHeader.setToAgencyId(initiationHeader.getToAgencyId());
 			}
 			if (initiationHeader.getFromSystemId() != null && initiationHeader.getToSystemId() != null) {
 				responseHeader.setFromSystemId(initiationHeader.getFromSystemId());
 				responseHeader.setToSystemId(initiationHeader.getToSystemId());
 				if (initiationHeader.getFromAgencyAuthentication() != null && !initiationHeader.getFromAgencyAuthentication().isEmpty())
 					responseHeader.setFromSystemAuthentication(initiationHeader.getFromAgencyAuthentication());
 			}
 			responseData.setResponseHeader(responseHeader);
 		}
 
 		responseData.setUserId(initData.getUserId());
 		responseData.setItemId(initData.getItemId());
 		responseData.setRequestScopeType(requestItem.getRequestScopeType());
 		responseData.setRequestType(requestItem.getRequestType());
 		responseData.setRequestId(requestItem.getRequestId());
 		responseData.setItemOptionalFields(requestItem.getItemOptionalFields());
 		responseData.setUserOptionalFields(requestItem.getUserOptionalFields());
 
 		responseData.setDateAvailable(requestItem.getDateAvailable());
 		responseData.setHoldQueuePosition(requestItem.getHoldQueuePosition());
 		responseData.setShippingInformation(requestItem.getShippingInformation());
 		responseData.setAcknowledgedFeeAmount(requestItem.getAcknowledgedFeeAmout());
 		responseData.setDateOfUserRequest(requestItem.getDatePlaced());
 		responseData.setEarliestDateNeeded(requestItem.getEarliestDateNeeded());
 		responseData.setHoldQueuePosition(requestItem.getHoldQueuePosition());
 		responseData.setNeedBeforeDate(requestItem.getNeedBeforeDate());
 		responseData.setPaidFeeAmount(requestItem.getPaidFeeAmount());
 		responseData.setPickupDate(requestItem.getPickupDate());
 		responseData.setPickupExpiryDate(requestItem.getPickupExpiryDate());
 		responseData.setPickupLocation(requestItem.getPickupLocation());
 		responseData.setRequestStatusType(requestItem.getRequestStatusType());
 	}
 }
