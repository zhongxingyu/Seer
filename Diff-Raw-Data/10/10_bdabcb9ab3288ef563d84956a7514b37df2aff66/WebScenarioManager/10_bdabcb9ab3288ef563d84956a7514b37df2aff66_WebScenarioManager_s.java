 package org.calculator.business.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.calculator.business.IWebRequestManager;
 import org.calculator.business.IWebScenarioManager;
 import org.calculator.dao.IGenericDao;
 import org.calculator.dao.IWebScenarioDao;
 import org.calculator.models.WebScenario;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * @see org.calculator.business.IWebScenarioManager
  * 
  * @author khalil.amdouni
  *
  */
 public class WebScenarioManager extends GenericManager<WebScenario> implements
 		IWebScenarioManager {
 
 	private IWebScenarioDao webScenarioDao;
 	private IWebRequestManager webRequestManager;
 	
 	@Override
 	public IGenericDao<WebScenario> getDao() {
 		return (IGenericDao<WebScenario>) webScenarioDao;
 	}
 	
 	@Override
 	public void addWebRequestToWebScenario(long scenarioId, long requestId) {
 		WebScenario webScenario = webScenarioDao.getById(scenarioId);
 		webScenario.setSequence((webScenario.getSequence() == null || ""
 				.equals(webScenario.getSequence())) ? Long.toString(requestId)
 				: webScenario.getSequence() + "-" + Long.toString(requestId));
 		webScenarioDao.save(webScenario);
 	}
 	
 	@Override
 	public List<WebScenario> getWebScenarios() {
 		return webScenarioDao.getWebScenarios();
 	}
 	
 	@Override
 	public void convertAndSaveXMLData(InputStream in)
 			throws ParserConfigurationException, SAXException, IOException {
 
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		Document document = db.parse(in);
 		document.getDocumentElement().normalize();
 		NodeList requestsNodeList = document.getElementsByTagName("request");
 		if (requestsNodeList != null && requestsNodeList.getLength() > 0) {
 			webRequestManager.convertAndSaveXMLRequests(requestsNodeList);
 		}
 		
 		NodeList scenariosNodeList = document.getElementsByTagName("scenario");
 		WebScenario webScenario = null;
 		for (int i = 0; i < scenariosNodeList.getLength(); i++) {
 			Node scenarioNode = scenariosNodeList.item(i);
 			if (scenarioNode.getNodeType() == Node.ELEMENT_NODE) {
 				Element scenarioElement = (Element)scenarioNode;
 				webScenario = new WebScenario();
 				webScenario.setName(scenarioElement.getAttribute("name"));
 				webScenario.setDescription(scenarioElement.getElementsByTagName("description").item(0).getTextContent());
 				webScenario.setSequence(webRequestManager.convertAndSaveXMLRequests(scenarioElement.getElementsByTagName("request")));
 			}
 		}
 	}
 
 	public IWebScenarioDao getWebScenarioDao() {
 		return webScenarioDao;
 	}
 
 	public void setWebScenarioDao(IWebScenarioDao webScenarioDao) {
 		this.webScenarioDao = webScenarioDao;
 	}
 
 	public IWebRequestManager getWebRequestManager() {
 		return webRequestManager;
 	}
 
 	public void setWebRequestManager(IWebRequestManager webRequestManager) {
 		this.webRequestManager = webRequestManager;
 	}
 
 }
