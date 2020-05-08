 package org.calculator.business.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.calculator.business.IWebRequestManager;
 import org.calculator.dao.IGenericDao;
 import org.calculator.dao.IWebRequestDao;
 import org.calculator.enums.CalculatorHttpMethods;
 import org.calculator.models.WebParam;
 import org.calculator.models.WebRequest;
 import org.calculator.models.WebScenario;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * @see org.calculator.business.IWebRequestManager
  * 
  * @author khalil.amdouni
  * 
  */
 public class WebRequestManager extends GenericManager<WebRequest> implements
 		IWebRequestManager {
 
 	private IWebRequestDao webRequestDao;
 
 	@Override
 	public IGenericDao<WebRequest> getDao() {
 		return (IGenericDao<WebRequest>) webRequestDao;
 	}
 
 	@Override
 	public WebScenario populateWebScenario(WebScenario webScenario) {
 
 		if (webScenario.getSequence() == null || "".equals(webScenario.getSequence())) {
 			return webScenario;
 		}
 		String[] webRequestIds = webScenario.getSequence().split("-");
 		List<WebRequest> webRequests = new ArrayList<WebRequest>();
 		for (String webRequestId : webRequestIds) {
 			webRequests.add(webRequestDao.getById(Long.valueOf(webRequestId)));
 		}
 		webScenario.setWebRequests(webRequests);
 		return webScenario;
 	}
 	
 	@Override
 	public List<WebRequest> getAllWebRequests() {
 		return webRequestDao.getAllWebRequests();
 	}
 	
 	@Override
 	public String convertAndSaveXMLRequests(NodeList requestsNodeList) {
 		String[] requestIDs = new String[requestsNodeList.getLength()];
 		WebRequest webRequest = null;
 		List<WebParam> webParams = null;
 		for (int i = 0; i < requestsNodeList.getLength(); i++) {
 			webRequest = new WebRequest();
 			Node requestNode = requestsNodeList.item(i);
 			if (requestNode.getNodeType() == Node.ELEMENT_NODE) {
 				Element requestElement = (Element) requestNode;
 				webRequest.setName(requestElement.getAttribute("name"));
 				webRequest.setUrl(requestElement.getElementsByTagName("url")
 						.item(0).getTextContent());
 				webRequest.setDescription(requestElement
 						.getElementsByTagName("description").item(0)
 						.getTextContent());
 				webRequest.setMethod(CalculatorHttpMethods
 						.valueOf(requestElement.getAttribute("method")));
 				NodeList paramsNodeList = requestElement
 						.getElementsByTagName("param");
 				if (paramsNodeList != null && paramsNodeList.getLength() > 0) {
 					webParams = new ArrayList<WebParam>();
 					for (int j = 0; j < paramsNodeList.getLength(); j++) {
 						WebParam webParam = new WebParam();
 						Node paramNode = paramsNodeList.item(j);
 						if (paramNode.getNodeType() == Node.ELEMENT_NODE) {
 							Element paramElement = (Element) paramNode;
 							webParam.setName(paramElement.getAttribute("name"));
 							webParam.setValue(paramElement.getTextContent());
 							webParam.setWebRequest(webRequest);
							webParams.add(webParam);
 						}
 					}
 				}
 				webRequest.setWebParams(webParams);
 			}
 			webRequest = webRequestDao.save(webRequest);
 			requestIDs[i] = Long.toString(webRequest.getId());
 		}
 		return StringUtils.join(requestIDs, "-");
 	}
 
 	public IWebRequestDao getWebRequestDao() {
 		return webRequestDao;
 	}
 
 	public void setWebRequestDao(IWebRequestDao webRequestDao) {
 		this.webRequestDao = webRequestDao;
 	}
 
 }
