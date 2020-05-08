 /**
  * Copyright (c) 2010 eXtensible Catalog Organization
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the MIT/X11 license. The text of the license can be
  * found at http://www.opensource.org/licenses/mit-license.php.
  */
 
 package org.extensiblecatalog.ncip.v2.aleph;
 
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephException;
 import org.extensiblecatalog.ncip.v2.aleph.util.AlephRemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.item.*;
 import org.extensiblecatalog.ncip.v2.service.*;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 public class AlephRenewItemService implements RenewItemService {
 
 	@Override
 	public RenewItemResponseData performService(RenewItemInitiationData initData, ServiceContext serviceContext, RemoteServiceManager serviceManager) throws ServiceException {
 
		boolean itemIdIsEmpty = initData.getItemId().getItemIdentifierValue().isEmpty();
		boolean userIdIsEmpty = initData.getUserId().getUserIdentifierValue().isEmpty();
 
 		if (itemIdIsEmpty || userIdIsEmpty) {
 			if (itemIdIsEmpty)
 				throw new ServiceException(ServiceError.UNSUPPORTED_REQUEST, "Item Id is undefined. Cannot renew unknown item.");
 			else
 				throw new ServiceException(ServiceError.UNSUPPORTED_REQUEST, "User Id is undefined. Cannot renew item loaned by unknown user.");
 		}
 
 		AlephRemoteServiceManager alephRemoteServiceManager = (AlephRemoteServiceManager) serviceManager;
 
 		final RenewItemResponseData responseData = new RenewItemResponseData();
 
 		try {
 			AlephRenewItem renewItem = alephRemoteServiceManager.renewItem(initData);
 
 			if (renewItem.getProblem() == null) {
 				updateResponseData(responseData, initData, renewItem);
 			} else
 				responseData.setProblems(Arrays.asList(renewItem.getProblem()));
 
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
 
 	private void updateResponseData(RenewItemResponseData responseData, RenewItemInitiationData initData, AlephRenewItem renewItem) {
 
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
 		responseData.setItemOptionalFields(renewItem.getItemOptionalFields());
 		responseData.setUserOptionalFields(renewItem.getUserOptionalFields());
 		responseData.setFiscalTransactionInformation(renewItem.getFiscalTransactionInfo());
 		responseData.setDateDue(renewItem.getDateDue());
 		responseData.setDateForReturn(renewItem.getDateForReturn());
 		responseData.setPending(renewItem.getPending());
 		responseData.setRenewalCount(renewItem.getRenewalCount());
 		responseData.setRequiredFeeAmount(renewItem.getRequiredFeeAmount());
 		responseData.setRequiredItemUseRestrictionTypes(renewItem.getRequiredItemUseRestrictionTypes());
 	}
 }
