 package org.extensiblecatalog.ncip.v2.aleph;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephException;
 import org.extensiblecatalog.ncip.v2.aleph.util.AlephRemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.item.AlephRequestItem;
 import org.extensiblecatalog.ncip.v2.service.CancelRequestItemInitiationData;
 import org.extensiblecatalog.ncip.v2.service.CancelRequestItemResponseData;
 import org.extensiblecatalog.ncip.v2.service.CancelRequestItemService;
 import org.extensiblecatalog.ncip.v2.service.InitiationHeader;
 import org.extensiblecatalog.ncip.v2.service.Problem;
 import org.extensiblecatalog.ncip.v2.service.ProblemType;
 import org.extensiblecatalog.ncip.v2.service.RemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.service.ResponseHeader;
 import org.extensiblecatalog.ncip.v2.service.ServiceContext;
 import org.extensiblecatalog.ncip.v2.service.ServiceError;
 import org.extensiblecatalog.ncip.v2.service.ServiceException;
 import org.extensiblecatalog.ncip.v2.service.Version1ItemElementType;
 import org.xml.sax.SAXException;
 
 public class AlephCancelRequestItemService implements CancelRequestItemService {
 
 	@Override
 	public CancelRequestItemResponseData performService(CancelRequestItemInitiationData initData, ServiceContext serviceContext, RemoteServiceManager serviceManager)
 			throws ServiceException {
 
		boolean itemIdIsEmpty = initData.getItemId().getItemIdentifierValue().isEmpty();
		boolean userIdIsEmpty = initData.getUserId().getUserIdentifierValue().isEmpty();
 
 		if (itemIdIsEmpty || userIdIsEmpty) {
 			if (itemIdIsEmpty)
 				throw new ServiceException(ServiceError.UNSUPPORTED_REQUEST,
 						"Item Id is undefined. Cannot cancel request for unknown item. Also Request Id is not supported by this service.");
 			else
 				throw new ServiceException(ServiceError.UNSUPPORTED_REQUEST, "User Id is undefined. Cannot lookup request with unknown user.");
 		}
 
 		AlephRemoteServiceManager alephRemoteServiceManager = (AlephRemoteServiceManager) serviceManager;
 
 		final CancelRequestItemResponseData responseData = new CancelRequestItemResponseData();
 
 		try {
 			AlephRequestItem requestItem = alephRemoteServiceManager.cancelRequestItem(initData);
 
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
 
 	private void updateResponseData(CancelRequestItemResponseData responseData, CancelRequestItemInitiationData initData, AlephRequestItem requestItem) {
 
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
 		responseData.setRequestId(requestItem.getRequestId());
 		responseData.setItemOptionalFields(requestItem.getItemOptionalFields());
 		responseData.setUserOptionalFields(requestItem.getUserOptionalFields());
 		responseData.setFiscalTransactionInformation(requestItem.getFiscalTransactionInfo());
 	}
 }
