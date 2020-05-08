 package org.meteornetwork.meteor.provider.access.ws;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 
 import javax.jws.WebService;
 import javax.xml.ws.Holder;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.meteornetwork.meteor.business.BestSourceAggregator;
 import org.meteornetwork.meteor.common.registry.RegistryManager;
 import org.meteornetwork.meteor.common.security.RequestInfo;
 import org.meteornetwork.meteor.common.ws.AccessProviderService;
 import org.meteornetwork.meteor.common.xml.dataresponse.Award;
 import org.meteornetwork.meteor.common.xml.dataresponse.MeteorRsMsg;
 import org.meteornetwork.meteor.provider.access.ResponseDataWrapper;
 import org.meteornetwork.meteor.provider.access.manager.AccessProviderManager;
 import org.meteornetwork.meteor.saml.SecurityTokenImpl;
 import org.meteornetwork.meteor.saml.TokenAttributes;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.SerializationUtils;
 
 @WebService(endpointInterface = "org.meteornetwork.meteor.common.ws.AccessProviderService", serviceName = "AccessProviderService")
 public class AccessProviderServiceImpl implements AccessProviderService {
 
 	private static final Log LOG = LogFactory.getLog(AccessProviderServiceImpl.class);
 
 	private AccessProviderManager accessProviderManager;
 
 	private Properties authenticationProperties;
 
 	private RequestInfo requestInfo;
 	private RegistryManager registryManager;
 
 	@Override
 	public String findDataForBorrower(String ssn, TokenAttributes meteorAttributes) {
 		LOG.debug("AP received request for best source data for ssn: " + ssn);
 
 		requestInfo.setMeteorInstitutionIdentifier(authenticationProperties.getProperty("authentication.identifier"));
 		requestInfo.setSecurityToken(new SecurityTokenImpl());
 		requestInfo.getSecurityToken().setMeteorAttributes(meteorAttributes);
 
 		ResponseDataWrapper response = accessProviderManager.queryMeteor(ssn);
 		try {
 			String marshalledResponse = marshalResponseData(response.getResponseDataBestSource());
 			if (marshalledResponse == null) {
 				return null;
 			}
 			LOG.debug("Returning data for ssn: " + ssn);
 			return accessProviderManager.maskSSNs(marshalledResponse);
 		} catch (Exception e) {
 			LOG.error("Could not mask SSNs", e);
 			return null;
 		}
 	}
 
 	@Override
 	public void findDataForBorrowerWithConsolidated(String ssn, TokenAttributes meteorAttributes, Holder<String> resultBestSource, Holder<String> resultAll, Holder<byte[]> duplicateAwardsMap) {
 		LOG.debug("AP received request for best source data for ssn: " + ssn);
 
 		requestInfo.setMeteorInstitutionIdentifier(authenticationProperties.getProperty("authentication.identifier"));
 		requestInfo.setSecurityToken(new SecurityTokenImpl());
 		requestInfo.getSecurityToken().setMeteorAttributes(meteorAttributes);
 
 		ResponseDataWrapper response = accessProviderManager.queryMeteor(ssn);
 		try {
 			String marshalledResponse = marshalResponseData(response.getUnfilteredResponseData());
 			resultAll.value = marshalledResponse == null ? null : accessProviderManager.maskSSNs(marshalledResponse);
 			marshalledResponse = marshalResponseData(response.getResponseDataBestSource());
 			resultBestSource.value = marshalledResponse == null ? null : accessProviderManager.maskSSNs(marshalledResponse);
 		} catch (Exception e) {
 			LOG.error("Could not mask SSNs", e);
 			return;
 		}
 
 		/*
 		 * map best source award ids to duplicate award ids
 		 */
 		HashMap<Integer, ArrayList<Integer>> duplicateAwardsMapObj = new HashMap<Integer, ArrayList<Integer>>();
 		BestSourceAggregator aggregator = response.getBestSourceAggregator();
 		for (Award award : aggregator.getBest()) {
 			ArrayList<Integer> duplicateAwardIds = new ArrayList<Integer>();
 			for (Award dupAward : aggregator.getDuplicates(award.getAPSUniqueAwardID())) {
 				duplicateAwardIds.add(dupAward.getAPSUniqueAwardID());
 			}
 
 			duplicateAwardsMapObj.put(award.getAPSUniqueAwardID(), duplicateAwardIds);
 		}
 
		duplicateAwardsMap.value = SerializationUtils.serialize(duplicateAwardsMapObj);
 		LOG.debug("Returning data for ssn: " + ssn);
 	}
 
 	private String marshalResponseData(MeteorRsMsg responseData) {
 		StringWriter marshalledResponse = new StringWriter();
 		try {
 			responseData.marshal(marshalledResponse);
 		} catch (Exception e) {
 			LOG.error("Could not marshal response data", e);
 			return null;
 		}
 
 		return marshalledResponse.toString();
 	}
 
 	public AccessProviderManager getAccessProviderManager() {
 		return accessProviderManager;
 	}
 
 	@Autowired
 	public void setAccessProviderManager(AccessProviderManager accessProviderManager) {
 		this.accessProviderManager = accessProviderManager;
 	}
 
 	public RequestInfo getRequestInfo() {
 		return requestInfo;
 	}
 
 	@Autowired
 	public void setRequestInfo(RequestInfo requestInfo) {
 		this.requestInfo = requestInfo;
 	}
 
 	public RegistryManager getRegistryManager() {
 		return registryManager;
 	}
 
 	@Autowired
 	public void setRegistryManager(RegistryManager registryManager) {
 		this.registryManager = registryManager;
 	}
 
 	public Properties getAuthenticationProperties() {
 		return authenticationProperties;
 	}
 
 	@Autowired
 	@Qualifier("AuthenticationProperties")
 	public void setAuthenticationProperties(Properties authenticationProperties) {
 		this.authenticationProperties = authenticationProperties;
 	}
 
 }
