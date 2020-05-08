 package org.meteornetwork.meteor.provider.access;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.meteornetwork.meteor.business.BestSourceAggregator;
 import org.meteornetwork.meteor.business.GrandTotalCalculator;
 import org.meteornetwork.meteor.common.util.message.Messages;
 import org.meteornetwork.meteor.common.util.message.MeteorMessage;
 import org.meteornetwork.meteor.common.xml.dataresponse.Award;
 import org.meteornetwork.meteor.common.xml.dataresponse.Contacts;
 import org.meteornetwork.meteor.common.xml.dataresponse.DataProviderAggregateTotal;
 import org.meteornetwork.meteor.common.xml.dataresponse.DataProviderData;
 import org.meteornetwork.meteor.common.xml.dataresponse.MeteorDataProviderAwardDetails;
 import org.meteornetwork.meteor.common.xml.dataresponse.MeteorDataProviderDetailInfo;
 import org.meteornetwork.meteor.common.xml.dataresponse.MeteorDataProviderInfo;
 import org.meteornetwork.meteor.common.xml.dataresponse.MeteorDataProviderMsg;
 import org.meteornetwork.meteor.common.xml.dataresponse.MeteorIndexProviderData;
 import org.meteornetwork.meteor.common.xml.dataresponse.MeteorRsMsg;
 import org.meteornetwork.meteor.common.xml.indexresponse.Message;
 import org.meteornetwork.meteor.common.xml.indexresponse.types.RsMsgLevelEnum;
 
 /**
  * Adds messages and other data to a single MeteorRsMsg object
  * 
  * @author jlazos
  */
 public class ResponseDataWrapper {
 
 	// special DataProviderType to indicate index response message container
 	private static final String IDX = "IDX";
 
 	// indicated DataProviderType is unknown - UNK data provider types are
 	// interpreted as loan locator messages
 	private static final String UNK = "UNK";
 
 	private String borrowerSsn;
 
 	private transient MeteorRsMsg responseData;
 	private transient MeteorDataProviderInfo indexMessageMdpi;
 	private transient BestSourceAggregator bestSourceAggregator;
 
 	private transient Integer nextAwardId = 0;
 
 	public ResponseDataWrapper() {
 		this.responseData = new MeteorRsMsg();
 	}
 
 	/**
 	 * Adds information of the index provider with loan locator data
 	 * 
 	 * @param ipId
 	 *            entity id of the index provider
 	 * @param ipName
 	 *            entity name of the index provider
 	 * @param ipUrl
 	 *            entity url of the index provider
 	 */
 	public void addLoanLocatorIndexProvider(String ipId, String ipName, String ipUrl) {
 		MeteorIndexProviderData indexProviderData = new MeteorIndexProviderData();
 		indexProviderData.setEntityID(ipId);
 		indexProviderData.setEntityName(ipName);
 		indexProviderData.setEntityURL(ipUrl);
 		indexProviderData.setContacts(new Contacts());
 		responseData.addMeteorIndexProviderData(indexProviderData);
 	}
 
 	/**
 	 * Add a data provider to loan locator data
 	 * 
 	 * @param dpId
 	 *            entity id of the data provider
 	 * @param dpName
 	 *            entity name of the data provider
 	 * @param dpUrl
 	 *            entity url of the data provider. this is the entity's web url,
 	 *            not the meteor web services url
 	 */
 	public void addLoanLocatorDataProvider(String dpId, String dpName, String dpUrl) {
 
 		responseData.addMeteorDataProviderInfo(addLoanLocatorInfo(dpId, dpName, dpUrl));
 	}
 
 	private MeteorDataProviderInfo addLoanLocatorInfo(String dpId, String dpName, String dpUrl) {
 		MeteorDataProviderInfo dpInfo = new MeteorDataProviderInfo();
 
 		dpInfo.setLoanLocatorActivationIndicator(true);
 
 		dpInfo.setMeteorDataProviderDetailInfo(new MeteorDataProviderDetailInfo());
 		dpInfo.getMeteorDataProviderDetailInfo().setDataProviderType(UNK);
 		dpInfo.getMeteorDataProviderDetailInfo().setDataProviderAggregateTotal(new DataProviderAggregateTotal());
 
 		DataProviderData dpContact = new DataProviderData();
 		dpInfo.getMeteorDataProviderDetailInfo().setDataProviderData(dpContact);
 
 		dpContact.setEntityID(dpId);
 		dpContact.setEntityName(dpName);
 		dpContact.setEntityURL(dpUrl);
 		dpContact.setContacts(new Contacts());
 
 		return dpInfo;
 	}
 
 	/**
 	 * Add response from data provider to this set of response data. If loan
 	 * locator activation indicator is set or there is an error message in the
 	 * response, the data provider is added to the loan locator
 	 * 
 	 * @param dataProviderInfo
 	 *            - information from the index provider about this data provider
 	 * @param dataProviderResponse
 	 */
 	public void addDataProviderInfo(MeteorRsMsg dataProviderResponse) {
 		for (MeteorDataProviderInfo info : dataProviderResponse.getMeteorDataProviderInfo()) {
 			responseData.addMeteorDataProviderInfo(info);
 
 			boolean loanLocatorActivationIndicator = info.getLoanLocatorActivationIndicator() == null ? false : info.getLoanLocatorActivationIndicator();
 			boolean hasErrorMessage = false;
 			if (info.getMeteorDataProviderMsgCount() > 0) {
 				for (MeteorDataProviderMsg message : info.getMeteorDataProviderMsg()) {
 					if (RsMsgLevelEnum.E.name().equals(message.getRsMsgLevel())) {
 						hasErrorMessage = true;
 						break;
 					}
 				}
 			}
 
 			if ((hasErrorMessage || loanLocatorActivationIndicator) && info.getMeteorDataProviderDetailInfo() != null && info.getMeteorDataProviderDetailInfo().getDataProviderData() != null) {
 				DataProviderData data = info.getMeteorDataProviderDetailInfo().getDataProviderData();
 				addLoanLocatorDataProvider(data.getEntityID(), data.getEntityName(), data.getEntityURL());
 			}
 
 			transformMessages(info);
 			setAPSUniqueAwardIds(info);
 		}
 	}
 
 	private void transformMessages(MeteorDataProviderInfo info) {
 		if (info.getMeteorDataProviderMsgCount() > 0) {
 			for (MeteorDataProviderMsg message : info.getMeteorDataProviderMsg()) {
 				message.setRsMsg(Messages.getMessage(message.getRsMsg()));
 			}
 		}
 	}
 
 	private void setAPSUniqueAwardIds(MeteorDataProviderInfo info) {
 		MeteorDataProviderAwardDetails awardDetails = info.getMeteorDataProviderAwardDetails();
 		if (awardDetails == null || awardDetails.getAwardCount() <= 0) {
 			return;
 		}
 
 		for (Award award : awardDetails.getAward()) {
 			award.setAPSUniqueAwardID(nextAwardId++);
 		}
 	}
 
 	/**
 	 * Add responses from data providers to this set of response data
 	 * 
 	 * @param dataProviderResponses
 	 */
 	public void addAllDataProviderInfo(Iterable<MeteorRsMsg> dataProviderResponses) {
 		if (dataProviderResponses != null) {
 			for (MeteorRsMsg response : dataProviderResponses) {
 				addDataProviderInfo(response);
 			}
 		}
 	}
 
 	/**
 	 * Adds error message from data provider and adds data provider to loan
 	 * locator
 	 * 
 	 * @param dataProviderInfo
 	 *            the entity id, name, and url of the data provider
 	 * @param message
 	 *            the data provider message
 	 * @param errorLevel
 	 *            the message's error level
 	 */
 	public void addDataProviderErrorMessage(DataProviderInfo dataProviderInfo, String message, String errorLevel) {
 		MeteorDataProviderInfo dpInfo = null;
 		if (dataProviderInfo.getIndexProviderInfo() == null) {
 			if (dataProviderInfo.getRegistryInfo() != null) {
 				dpInfo = new MeteorDataProviderInfo();
 
 				dpInfo.setLoanLocatorActivationIndicator(true);
 
 				dpInfo.setMeteorDataProviderDetailInfo(new MeteorDataProviderDetailInfo());
 				dpInfo.getMeteorDataProviderDetailInfo().setDataProviderType(UNK);
 				dpInfo.getMeteorDataProviderDetailInfo().setDataProviderAggregateTotal(new DataProviderAggregateTotal());
 
 				DataProviderData dpContact = new DataProviderData();
 				dpContact.setEntityID(dataProviderInfo.getRegistryInfo().getInstitutionIdentifier());
 				dpInfo.getMeteorDataProviderDetailInfo().setDataProviderData(dpContact);
 				dpContact.setContacts(new Contacts());
 			}
 		} else {
 			dpInfo = addLoanLocatorInfo(dataProviderInfo.getIndexProviderInfo().getEntityID(), dataProviderInfo.getIndexProviderInfo().getEntityName(), dataProviderInfo.getIndexProviderInfo().getEntityURL());
 		}
 
 		if (dpInfo != null) {
 			MeteorDataProviderMsg dpMsg = new MeteorDataProviderMsg();
 			dpMsg.setRsMsg(message);
 			dpMsg.setRsMsgLevel(errorLevel);
 
 			dpInfo.addMeteorDataProviderMsg(dpMsg);
 
 			responseData.addMeteorDataProviderInfo(dpInfo);
 		}
 	}
 
 	/**
 	 * Adds message from index provider to response
 	 * 
 	 * @param message
 	 *            the message to add to the response
 	 */
 	public void addIndexProviderMessage(Message message) {
 		addIndexProviderMessage(Messages.getMessage(message.getRsMsg()), message.getRsMsgLevel());
 	}
 
 	/**
 	 * Add message to index provider messages
 	 * 
 	 * @param msgLevel
 	 *            severity level of message
 	 * @param message
 	 *            message property reference
 	 * @param msgParameters
 	 *            parameters for message template referenced from property
 	 */
 	public void addIndexProviderMessage(RsMsgLevelEnum msgLevel, MeteorMessage message, Map<String, String> msgParameters) {
 		String messageContent = Messages.getMessage(message.getPropertyRef(), msgParameters);
 
 		addIndexProviderMessage(messageContent, msgLevel.name());
 	}
 
 	/**
 	 * Add message to index provider messages
 	 * 
 	 * @param message
 	 *            message
 	 * @param messageLevel
 	 *            message level
 	 */
 	public void addIndexProviderMessage(String message, String messageLevel) {
 		if (indexMessageMdpi == null) {
 			indexMessageMdpi = createMinimalMeteorDataProviderInfo(IDX);
 			responseData.addMeteorDataProviderInfo(indexMessageMdpi);
 		}
 
 		MeteorDataProviderMsg mdpMessage = new MeteorDataProviderMsg();
 		mdpMessage.setRsMsgLevel(messageLevel);
 		mdpMessage.setRsMsg(message);
 		indexMessageMdpi.addMeteorDataProviderMsg(mdpMessage);
 	}
 
 	public MeteorDataProviderInfo createMinimalMeteorDataProviderInfo(String dataProviderType) {
 		MeteorDataProviderInfo mdpi = new MeteorDataProviderInfo();
 		MeteorDataProviderDetailInfo mdpdi = new MeteorDataProviderDetailInfo();
 		mdpi.setMeteorDataProviderDetailInfo(mdpdi);
 
 		mdpdi.setDataProviderType(dataProviderType);
 
 		DataProviderData dpd = new DataProviderData();
 		mdpdi.setDataProviderData(dpd);
 
 		Contacts contacts = new Contacts();
 		dpd.setContacts(contacts);
 
 		mdpdi.setDataProviderAggregateTotal(new DataProviderAggregateTotal());
 		return mdpi;
 	}
 
 	/**
 	 * Returns response data without best source filtering logic applied to
 	 * awards
 	 * 
 	 * @return response data without best source logic applied to awards
 	 */
 	public MeteorRsMsg getUnfilteredResponseData() {
 		return responseData;
 	}
 
 	/**
 	 * Get response data with awards filtered by best source logic. Creates a
 	 * new BestSourceAggregator to filter the awards, which is accessible by
 	 * invoking getBestSourceAggregator() after calling this method. Also
 	 * calculates grand totals of best source awards
 	 * 
 	 * @return response data with awards filtered by best source logic
 	 */
 	public MeteorRsMsg getResponseDataBestSource() {
 		bestSourceAggregator = new BestSourceAggregator();
 
 		for (MeteorDataProviderInfo info : responseData.getMeteorDataProviderInfo()) {
 			if (info.getMeteorDataProviderAwardDetails() != null) {
 				for (Award award : info.getMeteorDataProviderAwardDetails().getAward()) {
 					bestSourceAggregator.add(award);
 				}
 			}
 		}
 
 		Set<Award> bestAwards = bestSourceAggregator.getBest();
 
 		MeteorRsMsg withBestSource = new MeteorRsMsg();
 		withBestSource.setMeteorIndexProviderData(responseData.getMeteorIndexProviderData());
 		withBestSource.setMeteorDataAggregates(responseData.getMeteorDataAggregates());
 
 		for (MeteorDataProviderInfo info : responseData.getMeteorDataProviderInfo()) {
 			MeteorDataProviderInfo newInfo = new MeteorDataProviderInfo();
 			newInfo.setMeteorDataProviderMsg(info.getMeteorDataProviderMsg());
 			newInfo.setMeteorDataProviderDetailInfo(info.getMeteorDataProviderDetailInfo());
 			withBestSource.addMeteorDataProviderInfo(newInfo);
 
 			if (info.getMeteorDataProviderAwardDetails() == null) {
 				continue;
 			}
 
 			newInfo.setMeteorDataProviderAwardDetails(new MeteorDataProviderAwardDetails());
 			for (Award award : info.getMeteorDataProviderAwardDetails().getAward()) {
 				if (bestAwards.contains(award)) {
 					newInfo.getMeteorDataProviderAwardDetails().addAward(award);
 				}
 			}
 		}
 
 		GrandTotalCalculator grandTotalCalc = new GrandTotalCalculator(withBestSource, getBorrowerSsn());
 		grandTotalCalc.calculate();
 		return withBestSource;
 	}
 
 	/**
 	 * Get the best source aggregator used after the last call to
 	 * getResponseDataBestSource()
 	 * 
 	 * @return the best source aggregator used after the last call to
 	 *         getResponseDataBestSource()
 	 */
 	public BestSourceAggregator getBestSourceAggregator() {
 		return bestSourceAggregator;
 	}
 
 	public String getBorrowerSsn() {
 		return borrowerSsn;
 	}
 
 	public void setBorrowerSsn(String borrowerSsn) {
 		this.borrowerSsn = borrowerSsn;
 	}
 }
