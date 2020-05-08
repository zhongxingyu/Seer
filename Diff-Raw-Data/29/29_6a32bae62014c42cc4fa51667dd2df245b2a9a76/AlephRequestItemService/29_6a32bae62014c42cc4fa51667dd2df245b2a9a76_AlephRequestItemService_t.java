 package org.extensiblecatalog.ncip.v2.aleph;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephConstants;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephException;
 import org.extensiblecatalog.ncip.v2.aleph.util.AlephRemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.item.AlephRequestItem;
 import org.extensiblecatalog.ncip.v2.service.*;
 import org.xml.sax.SAXException;
 
 public class AlephRequestItemService implements RequestItemService {
 
 	@Override
 	public RequestItemResponseData performService(RequestItemInitiationData initData, ServiceContext serviceContext, RemoteServiceManager serviceManager) throws ServiceException {
 
		boolean userIdIsEmpty = initData.getUserId() == null || initData.getUserId().getUserIdentifierValue().isEmpty();
 		
 		if (userIdIsEmpty) {
 			throw new ServiceException(ServiceError.UNSUPPORTED_REQUEST, "User Id is empty.");
 		}
 
 		AlephRemoteServiceManager alephRemoteServiceManager = (AlephRemoteServiceManager) serviceManager;
 
 		final RequestItemResponseData responseData = new RequestItemResponseData();
 
 		try {
 			AlephRequestItem requestItem = alephRemoteServiceManager.requestItem(initData);
 
 			if (requestItem.getProblem() == null) {
 				updateResponseData(responseData, initData, requestItem);
 			} else {
 				responseData.setProblems(Arrays.asList(requestItem.getProblem()));
 				responseData.setRequiredFeeAmount(requestItem.getRequiredFeeAmount());
 				responseData.setRequiredItemUseRestrictionTypes(requestItem.getItemUseRestrictionTypes());
 			}
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
 
 	private void updateResponseData(RequestItemResponseData responseData, RequestItemInitiationData initData, AlephRequestItem requestItem) {
 
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
 
 		if (initData.getItemIds().size() > 1) {
 			// If there was more than one requested item, then merge all item identifier's values into one because responseData does not support
 			// multiple ItemIds
 			StringBuilder joinedItemIds = new StringBuilder();
 			int itemIdsSize = initData.getItemIds().size();
 			for (int i = 0; i < itemIdsSize; i++) {
 				if (i == itemIdsSize - 1) {
 					joinedItemIds.append(initData.getItemId(i).getItemIdentifierValue());
 				} else {
 					joinedItemIds.append(initData.getItemId(i).getItemIdentifierValue());
 					joinedItemIds.append(AlephConstants.REQUEST_ID_DELIMITER);
 				}
 			}
 			ItemId itemId = new ItemId();
 			itemId.setItemIdentifierType(Version1ItemIdentifierType.ACCESSION_NUMBER);
 			itemId.setItemIdentifierValue(joinedItemIds.toString());
 			responseData.setItemId(itemId);
 		} else
 			responseData.setItemId(initData.getItemId(0));
 
 		responseData.setRequestScopeType(requestItem.getRequestScopeType());
 		responseData.setRequestType(requestItem.getRequestType());
 		responseData.setRequestId(requestItem.getRequestId());
 		responseData.setItemOptionalFields(requestItem.getItemOptionalFields());
 		responseData.setUserOptionalFields(requestItem.getUserOptionalFields());
 		responseData.setFiscalTransactionInformation(requestItem.getFiscalTransactionInfo());
 
 		// Not implemented services, most of them probably even not implementable in Aleph logic
 		responseData.setDateAvailable(requestItem.getDateAvailable());
 		responseData.setHoldPickupDate(requestItem.getHoldPickupDate());
 		responseData.setHoldQueueLength(requestItem.getHoldQueueLength());
 		responseData.setHoldQueuePosition(requestItem.getHoldQueuePosition());
 		responseData.setShippingInformation(requestItem.getShippingInformation());
 		responseData.setUserId(initData.getUserId());
 	}
 }
