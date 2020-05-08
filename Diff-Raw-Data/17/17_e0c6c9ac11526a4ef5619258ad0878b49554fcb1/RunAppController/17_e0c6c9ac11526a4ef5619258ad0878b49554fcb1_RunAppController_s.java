 package edu.neu.webapp.graphiccodegen.controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.swing.text.FlowView.FlowStrategy;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import edu.neu.webapp.graphiccodegen.dao.ApplicationDao;
 import edu.neu.webapp.graphiccodegen.dao.EventDao;
 import edu.neu.webapp.graphiccodegen.dao.FlowDao;
 import edu.neu.webapp.graphiccodegen.dao.NodeDao;
 import edu.neu.webapp.graphiccodegen.dao.RunAppDao;
 import edu.neu.webapp.graphiccodegen.model.AppFlowStart;
import edu.neu.webapp.graphiccodegen.model.Application;
import edu.neu.webapp.graphiccodegen.model.Flow;
 import edu.neu.webapp.graphiccodegen.model.FlowNodeStart;
 
 public class RunAppController {
 
 	@Autowired
 	private FlowDao flowDao;
 	@Autowired
 	private ApplicationDao appDao;
 	@Autowired
 	private NodeDao nodeDao;
 
 	@Autowired
 	private EventDao eventDao;
 	
 	@Autowired
 	private RunAppDao runAppDao;
 
 	
 
 	@RequestMapping(value = "/runApp")
 	public String runApp(ModelMap model, HttpServletRequest request) {
 		String appID = request.getParameter("appID");
 		String returnPage = "";
 	
 
 		// main functionality
 		AppFlowStart app = runAppDao.getAppFlowStart(appID);
 		if(app != null){
 			FlowNodeStart flowNode = runAppDao.getFlowNodeStart(app.getFlow().getId());
 			flowNode.getNode();
 		}
 		return returnPage;
 	}
 
 	//added for testing
 	public void addValues(String appId,String flowID){
 		AppFlowStart appFlow = new AppFlowStart();
 		Application app = appDao.getApplication(appId);
 		Flow flow = flowDao.getFlow(flowID);
 		appFlow.setApplication(app);
 		appFlow.setFlow(flow);
 		runAppDao.persist(appFlow);
 	}
 }
