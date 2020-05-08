 /**
  * Copyright (c) 2010 eXtensible Catalog Organization
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the MIT/X11 license. The text of the license can be
  * found at http://www.opensource.org/licenses/mit-license.php. 
  */
 
 package org.extensiblecatalog.ncip.v2.aleph;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.extensiblecatalog.ncip.v2.service.AgencyId;
 import org.extensiblecatalog.ncip.v2.service.ItemOptionalFields;
 import org.extensiblecatalog.ncip.v2.service.ItemTransaction;
 import org.extensiblecatalog.ncip.v2.service.LookupItemInitiationData;
 import org.extensiblecatalog.ncip.v2.service.LookupItemResponseData;
 import org.extensiblecatalog.ncip.v2.service.LookupItemService;
 import org.extensiblecatalog.ncip.v2.service.Problem;
 import org.extensiblecatalog.ncip.v2.service.ProblemType;
 import org.extensiblecatalog.ncip.v2.service.RemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.service.ServiceContext;
 import org.extensiblecatalog.ncip.v2.service.ServiceError;
 import org.extensiblecatalog.ncip.v2.service.ServiceException;
 import org.extensiblecatalog.ncip.v2.service.UserIdentifierType;
 import org.extensiblecatalog.ncip.v2.service.CurrentBorrower;
 import org.extensiblecatalog.ncip.v2.service.CurrentRequester;
 import org.extensiblecatalog.ncip.v2.service.UserId;
 import org.extensiblecatalog.ncip.v2.service.Version1LookupItemProcessingError;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephException;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.AlephRemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.item.AlephItem;
 import org.extensiblecatalog.ncip.v2.aleph.restdlf.user.AlephUser;
 import org.extensiblecatalog.ncip.v2.aleph.util.AlephUtil;
 import org.extensiblecatalog.ncip.v2.service.RequestId;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.GregorianCalendar;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 /**
  * This class implements the Lookup Item service for the Aleph back-end connector. Basically this just calls the AlephRemoteServiceManager to get hard-coded data (e.g. title, call #, etc.).
  * <p/>
  * Note: If you're looking for a model of how to code your own ILS's NCIPService classes, do not use this class as an example. See the NCIP toolkit Connector developer's documentation for guidance.
  */
 public class AlephLookupItemService implements LookupItemService {
 
 	static Logger log = Logger.getLogger(AlephLookupItemService.class);
 
 	static int itemsFound;
 
 	/**
 	 * Construct a AlephRemoteServiceManager; this class is not configurable so there are no parameters.
 	 */
 	public AlephLookupItemService() {
 	}
 
 	/**
 	 * Handles a NCIP LookupItem service by returning data from Aleph.
 	 *
 	 * @param initData
 	 *            the LookupItemInitiationData
 	 * @param serviceManager
 	 *            provides access to remote services
 	 * @return LookupItemResponseData
 	 */
 	@Override
 	public LookupItemResponseData performService(LookupItemInitiationData initData, ServiceContext serviceContext, RemoteServiceManager serviceManager) throws ServiceException {
 		log.info("AlephLookupItemService.performService called");
 		LookupItemResponseData responseData = new LookupItemResponseData();
 		AlephRemoteServiceManager alephRemoteServiceManager = (AlephRemoteServiceManager) serviceManager;
 
 		// TODO: Which can be parsed from rest-dlf & which need x-services?
 		boolean getCurrentBorrower = initData.getCurrentBorrowerDesired();
 		boolean getCurrentRequesters = initData.getCurrentRequestersDesired();
 		boolean getItemUseRestrictionType = initData.getItemUseRestrictionTypeDesired();
 		boolean getPhysicalCondition = initData.getPhysicalConditionDesired();
 		boolean getSecurityMarker = initData.getSecurityMarkerDesired();
 		boolean getSensitizationFlag = initData.getSensitizationFlagDesired();
 		boolean getElectronicResource = initData.getElectronicResourceDesired();
 		boolean getLocation = initData.getLocationDesired();
 		// EOF TODO;
 
 		boolean getBibDescription = initData.getBibliographicDescriptionDesired();
 		boolean getCircStatus = initData.getCirculationStatusDesired();
 		boolean getHoldQueueLength = initData.getHoldQueueLengthDesired();
 		boolean getItemDescription = initData.getItemDescriptionDesired();
 
 		if (initData.getItemId() == null) {
 			throw new ServiceException(ServiceError.UNSUPPORTED_REQUEST, "Item id is undefined.");
 		}
 		AlephItem alephItem = null;
 		// Execute request if agency Id is blank or NRU
 		if (initData.getItemId().getAgencyId() != null && !initData.getItemId().getAgencyId().getValue().equalsIgnoreCase("")
 				&& alephRemoteServiceManager.getAlephAgency(initData.getItemId().getAgencyId().getValue()) == null) {
 			throw new ServiceException(ServiceError.UNSUPPORTED_REQUEST, "This request cannot be processed. Agency ID is invalid or not found.");
 		}
 
 		try {
 
 			alephItem = alephRemoteServiceManager.lookupItem(initData.getItemId().getItemIdentifierValue(), getBibDescription, getCircStatus, getHoldQueueLength,
 					getItemDescription);
 
 			// update NCIP response data with aleph item data
 			if (alephItem == null) {
 				Problem p = new Problem();
 				p.setProblemType(Version1LookupItemProcessingError.UNKNOWN_ITEM);
 				p.setProblemDetail("Item " + initData.getItemId().getItemIdentifierValue() + " you are searching for doesn't exists.");
 				List<Problem> problems = new ArrayList<Problem>();
 				problems.add(p);
 				responseData.setProblems(problems);
 			} else
 				updateResponseData(initData, responseData, alephItem);
 		} catch (IOException ie) {
 			Problem p = new Problem();
 			p.setProblemType(new ProblemType("Procesing IOException error"));
 			p.setProblemDetail(ie.getMessage());
 			List<Problem> problems = new ArrayList<Problem>();
 			problems.add(p);
 			responseData.setProblems(problems);
 		} catch (ParserConfigurationException pce) {
 			Problem p = new Problem();
 			p.setProblemType(new ProblemType("Procesing ParserConfigurationException error"));
 			p.setProblemDetail(pce.getMessage());
 			List<Problem> problems = new ArrayList<Problem>();
 			problems.add(p);
 			responseData.setProblems(problems);
 		} catch (SAXException se) {
 			Problem p = new Problem();
 			p.setProblemType(new ProblemType("Procesing SAXException error"));
 			p.setProblemDetail(se.getMessage());
 			List<Problem> problems = new ArrayList<Problem>();
 			problems.add(p);
 			responseData.setProblems(problems);
 		} catch (AlephException ae) {
 			Problem p = new Problem();
 			p.setProblemType(new ProblemType("Procesing AlephException error"));
 			p.setProblemDetail(ae.getMessage());
 			List<Problem> problems = new ArrayList<Problem>();
 			problems.add(p);
 			responseData.setProblems(problems);
 		} catch (Exception e) {
 			Problem p = new Problem();
 			p.setProblemType(new ProblemType("Unknown procesing exception error"));
 			p.setProblemDetail(e.getMessage());
 			List<Problem> problems = new ArrayList<Problem>();
 			problems.add(p);
 			responseData.setProblems(problems);
 		}
 
 		return responseData;
 	}
 
 	protected void updateResponseData(LookupItemInitiationData initData, LookupItemResponseData responseData, AlephItem alephItem) throws ServiceException {
 		if (responseData != null && alephItem != null) {
 			
 			ItemOptionalFields iof = AlephUtil.getItemOptionalFields(alephItem);
 
 			if (alephItem.getDateAvailablePickup() != null) {
 				GregorianCalendar gc = new GregorianCalendar();
 				gc.setTime(alephItem.getDateAvailablePickup());
 				responseData.setHoldPickupDate(gc);
 			}
 			responseData.setItemId(initData.getItemId());
 			
 			if (initData.getBibliographicDescriptionDesired()) {
				//FIXME: Merge duplicate code from AlephUtil, this & AlephLookupItemSetService
 				iof.setBibliographicDescription(AlephUtil.getBibliographicDescription(alephItem, initData.getItemId().getAgencyId()));
 			}
 
 			
 			ItemTransaction itemTransaction = null;
 			if (alephItem.getBorrowingUsers() != null && alephItem.getBorrowingUsers().size() > 0) {
 				if (itemTransaction == null)
 					itemTransaction = new ItemTransaction();
 				CurrentBorrower borrower = new CurrentBorrower();
 				UserId userId = new UserId();
 				AlephUser alephUser = alephItem.getBorrowingUsers().get(0);
 				userId.setUserIdentifierValue(alephUser.getAuthenticatedUsername());
 				userId.setUserIdentifierType(new UserIdentifierType("Username"));
 				userId.setAgencyId(new AgencyId(alephUser.getAgency().getAgencyId()));
 				borrower.setUserId(userId);
 				itemTransaction.setCurrentBorrower(borrower);
 			}
 
 			if (alephItem.getRequestingUsers() != null && alephItem.getRequestingUsers().size() > 0) {
 				if (itemTransaction == null)
 					itemTransaction = new ItemTransaction();
 				Iterator<AlephUser> i = alephItem.getRequestingUsers().iterator();
 				while (i.hasNext()) {
 					AlephUser alephUser = i.next();
 					CurrentRequester requester = new CurrentRequester();
 					UserId userId = new UserId();
 					userId.setUserIdentifierValue(alephUser.getAuthenticatedUsername());
 					userId.setUserIdentifierType(new UserIdentifierType("Username"));
 					userId.setAgencyId(new AgencyId(alephUser.getAgency().getAgencyId()));
 					requester.setUserId(userId);
 					List<CurrentRequester> requesters = itemTransaction.getCurrentRequesters();
 					if (requesters == null)
 						requesters = new ArrayList<CurrentRequester>();
 					requesters.add(requester);
 					itemTransaction.setCurrentRequesters(requesters);
 				}
 
 			}
 
 			if (alephItem.getHoldRequestId() != null) {
 				RequestId requestId = new RequestId();
 				requestId.setRequestIdentifierValue(alephItem.getHoldRequestId());
 				responseData.setRequestId(requestId);
 			}
 			responseData.setItemOptionalFields(iof);
 		}
 	}
 
 }
