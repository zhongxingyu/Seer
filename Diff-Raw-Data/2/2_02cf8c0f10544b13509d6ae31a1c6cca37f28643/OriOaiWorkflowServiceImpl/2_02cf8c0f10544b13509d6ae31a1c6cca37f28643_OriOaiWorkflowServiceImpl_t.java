 package org.orioai.esupecm.workflow.service;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.Service;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nuxeo.runtime.model.ComponentInstance;
 import org.nuxeo.runtime.model.DefaultComponent;
 import org.orioai.esupecm.OriOaiMetadataType;
 import org.orioai.esupecm.workflow.WsDescriptor;
 import org.orioai.ws.workflow.IOriWorkflowService;
 import org.orioai.ws.workflow.InstanceInfos;
 
 public class OriOaiWorkflowServiceImpl extends DefaultComponent implements OriOaiWorkflowService {
 
 	private static final Log log = LogFactory.getLog(OriOaiWorkflowServiceImpl.class);
 
 	protected WsDescriptor config;
 
 	protected HashMap<String, IOriWorkflowService> _oriWorkflowServices;
 
 
 
 	public void registerContribution(Object contribution,
 			String extensionPoint, ComponentInstance contributor) {
 		config = (WsDescriptor) contribution;
 	}
 
 
 	public void unregisterContribution(Object contribution,
 			String extensionPoint, ComponentInstance contributor) {
 		if (config==contribution)
 			config = null;
 	}
 
 
 	/**
 	 * Get the ori-oai-workflow service
 	 * @return
 	 */
 	private IOriWorkflowService getRemoteOriWorkflowService(String username) {
 
 		if (_oriWorkflowServices == null) {
 			_oriWorkflowServices = new HashMap<String, IOriWorkflowService>();
 		}
 
 		IOriWorkflowService oriWorkflowService = _oriWorkflowServices.get(username);
 
 		if( oriWorkflowService == null ) {
 
 			String wsUrl = config.getWsUrl();
 
 			log.info("getRemoteOriWorkflowService :: contacting Web Service from URL : " + wsUrl);
 
 			try {
                QName service_name = new QName("http://remote.services.workflow.orioai.org/", "OriWorkflowServiceService");
                 Service service = Service.create(new URL(wsUrl + "?wsdl"), service_name);
 				oriWorkflowService = service.getPort(IOriWorkflowService.class);
 				_oriWorkflowServices.put(username, oriWorkflowService);
 			}
 			catch (MalformedURLException e) {
 				throw new RuntimeException("pb retireving ori-oai-workflow Web Service", e);
 			}
 		}
 
 		return oriWorkflowService;
 	}
 
 
 
 
 
 
 
 	public List<OriOaiMetadataType> getMetadataTypes(String username) {
 
 
 		if (log.isDebugEnabled())
 			log.debug("getMetadataTypes :: going to get metadataTypes for "+username);
 
 		IOriWorkflowService oriWorkflowService = getRemoteOriWorkflowService(username);
 
 		Map<String,String> metadataTypes = oriWorkflowService.getMetadataTypes(username);
 		if (log.isDebugEnabled())
 			log.debug("getMetadataTypes :: get metadataTypes for "+username+" from Web Service ok : " + metadataTypes.toString());
 
 		List<OriOaiMetadataType> oriOaiMetadataTypes = new ArrayList<OriOaiMetadataType>();
 		Iterator<String> metadataTypeIds = metadataTypes.keySet().iterator();
 		while (metadataTypeIds.hasNext()) {
 			String metadataTypeId = metadataTypeIds.next();
 			String metadataTypeLabel = metadataTypes.get(metadataTypeId);
 
 			OriOaiMetadataType metadataType = new OriOaiMetadataType(metadataTypeId, metadataTypeLabel);
 
 			oriOaiMetadataTypes.add(metadataType);
 		}
 
 		return oriOaiMetadataTypes;
 	}
 
 
 
 
 	public Long newWorkflowInstance(String username, String metadataTypeId) {
 
 		IOriWorkflowService oriWorkflowService = getRemoteOriWorkflowService(username);
 
 		log.debug("newWorkflowInstance :: call newWorkflowInstance from Web Service ...");
 
 		Long id = oriWorkflowService.newWorkflowInstance(null, metadataTypeId, username);
 
 		return id;
 	}
 
 
 
 	/**
 	 * @deprecated
 	 * use Vector<String> getCurrentStates(Map<String, String> statesMap) instead
 	 */
 	public Map<String, String> getCurrentStates(String username, String idp, String language) {
 		IOriWorkflowService oriWorkflowService = getRemoteOriWorkflowService(username);
 
 		Map<String, String> currentStates = oriWorkflowService.getCurrentStates(idp, language);
 		if (log.isDebugEnabled())
 			log.debug("getCurrentStates :: currentStates=" + currentStates);
 
 		return currentStates;
 	}
 
 	public Vector<String> getCurrentStates(Map<String, String> statesMap) {
 		Vector<String> currentStates = new Vector<String>();
 		for (String key : statesMap.keySet())
 			currentStates.add(statesMap.get(key));
 
 		return currentStates;
 	}
 
 	/**
 	 * @deprecated
 	 * use List<String> getCurrentInformations(Map<String, String> currentInformations) instead
 	 */
 	public List<String> getCurrentInformations(String username, String idp, String language) {
 
 		IOriWorkflowService oriWorkflowService = getRemoteOriWorkflowService(username);
 
 		if (log.isDebugEnabled()) {
 			log.debug("getCurrentInformations :: idp=" + idp);
 			log.debug("getCurrentInformations :: language=" + language);
 		}
 
 		Map<String, String> currentInformations = oriWorkflowService.getErrors(idp, language);
 		if (log.isDebugEnabled())
 			log.debug("getCurrentInformations :: currentInformations=" + currentInformations);
 
 		List<String> informations = new ArrayList<String>();
 
 		for (Map.Entry<String, String> entry : currentInformations.entrySet()) {
 			if (entry.getKey() != null) {
 				informations.add(entry.getValue());
 			}
 		}
 
 		return informations;
 	}
 
 	public List<String> getCurrentInformations(Map<String, String> currentInformations) {
 		if (log.isDebugEnabled())
 			log.debug("getCurrentInformations :: currentInformations=" + currentInformations);
 
 		List<String> informations = new ArrayList<String>();
 
 		for (Map.Entry<String, String> entry : currentInformations.entrySet()) {
 			if (entry.getKey() != null) {
 				informations.add(entry.getValue());
 			}
 		}
 
 		return informations;
 	}
 
 
 
 	/**
 	 * Return metadata type for a given idp (remote access to workflow)
 	 * @param idp
 	 * @return metadata type or null if failed
 	 */
 	public OriOaiMetadataType getMetadataType(String username, String idp) {
 		IOriWorkflowService oriWorkflowService = getRemoteOriWorkflowService(username);
 		try {
 			String metadataTypeId = oriWorkflowService.getMetadataType(idp);
 
 			return new OriOaiMetadataType(metadataTypeId, oriWorkflowService.getMetadataTypes().get(metadataTypeId));
 		}
 		catch (Exception e) {
 			log.error("getMetadataType :: can't retrieve metadata type from idp "+ idp, e);
 			return null;
 		}
 
 	}
 
 
 	public String getMetadataSchemaNamespace(String username, String metadataTypeId) {
 		IOriWorkflowService oriWorkflowService = getRemoteOriWorkflowService(username);
 		try {
 			String metadataSchemaNamespace = oriWorkflowService.getMetadataSchemaNamespace(metadataTypeId);
 			//String metadataSchemaNamespace = "http://www.abes.fr/abes/documents/tef";
 			return metadataSchemaNamespace;
 		}
 		catch (Exception e) {
 			log.error("getMetadataSchemaNamespace :: can't retrieve metadata namespace from metadataTypeId "+ metadataTypeId, e);
 			return null;
 		}
 	}
 
 
 
 
 	public String getIdp(String username, Long id) {
 		IOriWorkflowService oriWorkflowService = getRemoteOriWorkflowService(username);
 		return oriWorkflowService.getIdp(id, username);
 	}
 
 
 
 	private String getReplacedMdEditorUrl(String mdEditorUrl) {
 		if (log.isDebugEnabled()) {
 			log.debug("getReplacedMdEditorUrl :: mdEditorUrl=" + mdEditorUrl);
 			log.debug("getReplacedMdEditorUrl :: config.getMdEditorFromUrl()=" + config.getMdEditorFromUrl());
 			log.debug("getReplacedMdEditorUrl :: config.getMdEditorToUrl()=" + config.getMdEditorToUrl());
 			log.debug("getReplacedMdEditorUrl :: config.isMdEditorTranslationSet()=" + config.isMdEditorTranslationSet());
 		}
 
 		String result = mdEditorUrl;
 
 		if (config.isMdEditorTranslationSet()) {
 			result = mdEditorUrl.replaceFirst(config.getMdEditorFromUrl(), config.getMdEditorToUrl());
 		}
 
 		return result;
 	}
 
 
 
 
 	/**
 	 * Returns md editor url for the first form available for user
 	 * @param idp
 	 * @param userId
 	 * @return
 	 */
 	public String getMdeditorUrl(String username, String idp) {
 		return getMdeditorUrlWS(username, idp);
 
 	}
 
 
 	/**
 	 *
 	 * @param idp
 	 * @param userId
 	 * @return
 	 */
 	public String getMdeditorUrlWS(String username, String idp) {
 		IOriWorkflowService oriWorkflowService = getRemoteOriWorkflowService(username);
 		Map<String,String> formsMap = oriWorkflowService.getMdEditorUrl(idp);
 
 		if (log.isDebugEnabled())
 			log.debug("getMdeditorUrlWS :: formsMap.get(0)=" + formsMap.get(0));
 
 		String result = getReplacedMdEditorUrl(formsMap.get(0));
 
 		if (log.isDebugEnabled())
 			log.debug("getMdeditorUrlWS :: result=" + result);
 
 		return result;
 	}
 
 	/**
 	 * Return a map for availables md editors
 	 * @param idp
 	 * @param userId
 	 * @return map formTitle:formUrl
 	 */
 	public Map<String,String> getMdeditorUrls(String username, String idp) {
 		IOriWorkflowService oriWorkflowService = getRemoteOriWorkflowService(username);
 		Map<String,String> formsMap = oriWorkflowService.getMdEditorUrl(idp);
 
 		if (log.isDebugEnabled())
 			log.debug("getMdeditorUrlWS :: before formsMap=" + formsMap);
 
 		for (String key : formsMap.keySet()) {
 			String value = getReplacedMdEditorUrl(formsMap.get(key));
 			if (log.isDebugEnabled())
 				log.debug("getMdeditorUrlWS :: put(" + key + ", "+value+")");
 			formsMap.put(key, value);
 		}
 
 		if (log.isDebugEnabled())
 			log.debug("getMdeditorUrlWS :: after formsMap=" + formsMap);
 
 		return formsMap;
 	}
 
 
 
 
 
 
 	/**
 	 * Returns availables actions for given user and idp
 	 * @param idp
 	 * @param userId
 	 * @return
 	 */
 	public Map<String,String> getAvailableActions(String username, String idp, String language) {
 
 		if (log.isDebugEnabled()) {
 			log.debug("getAvailableActions :: idp=" + idp);
 			log.debug("getAvailableActions :: language=" + language);
 		}
 
 		IOriWorkflowService oriWorkflowService = getRemoteOriWorkflowService(username);
 		Map<String,String> actionsMap = oriWorkflowService.getAvailableActions(idp, language);
 
 		if (log.isDebugEnabled())
 			log.debug("getAvailableActions :: actionsMap=" + actionsMap);
 
 		return actionsMap;
 	}
 
 
 	/**
 	 * Perform an available action
 	 * @param idp
 	 * @param actionId
 	 * @param observation
 	 * @return true if action was performed
 	 */
 	public boolean performAction(String username, String idp, int actionId, String observation) {
 		log.info("performAction :: idp=" + idp+", actionId="+actionId+", observation="+observation);
 		IOriWorkflowService oriWorkflowService = getRemoteOriWorkflowService(username);
 		return oriWorkflowService.performAction(idp, actionId, observation);
 
 	}
 
 
 	public String getXMLForms(String username, String idp) {
 		IOriWorkflowService oriWorkflowService = getRemoteOriWorkflowService(username);
 
 		String xml =  oriWorkflowService.getXMLForms(idp);
 		if (log.isDebugEnabled())
 			log.debug("getXMLForms :: xml=" + xml);
 
 		return xml;
 	}
 
 
 
 	public void saveXML(String username, String idp, String xmlContent) {
 		IOriWorkflowService oriWorkflowService = getRemoteOriWorkflowService(username);
 
 		oriWorkflowService.saveXML(idp, xmlContent);
 	}
 
 	public InstanceInfos getInstanceInfos(Long id, String userId, String language) {
 		IOriWorkflowService oriWorkflowService = getRemoteOriWorkflowService(userId);
 		return oriWorkflowService.getInstanceInfos(id, userId, language);
 	}
 }
