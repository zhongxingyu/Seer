 package edu.neu.webapp.graphiccodegen.controller;
 
 import java.io.File;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import edu.neu.webapp.graphiccodegen.dao.EventDao;
 import edu.neu.webapp.graphiccodegen.dao.RunAppDao;
 import edu.neu.webapp.graphiccodegen.model.AppFlowStart;
import edu.neu.webapp.graphiccodegen.entities.Event;
 import edu.neu.webapp.graphiccodegen.model.FlowNodeStart;
 
 
 @Controller
 public class PublishAppController {
 
 	@Autowired
 	private RunAppDao runAppDao;
 	
 	@Autowired
 	private EventDao eventDao;
 	
 	@RequestMapping(value = "/publishApp")
 	public void publishApp(ModelMap model, HttpServletRequest request) {
 		String appID = request.getParameter("appID");
 		//find app start flow.
 		AppFlowStart appFlow = runAppDao.getAppFlowStart(appID);
 		System.out.println("tomcat path ---"+System.getProperty("catalina.home"));
 		//APPFLOW.GETAPPLICATION().GETNAME()
 		File dir = new File(System.getProperty("catalina.home")+"/\\"+"webapps",appFlow.getApplication().getName());
 		if(!dir.exists()){
 			dir.mkdirs();
 		}
 		File flowDir = new File(dir.getAbsolutePath(), "flow");
 		if(!flowDir.exists()){
 			dir.mkdir();
 		}
 		
 		FlowNodeStart flowNode = runAppDao.getFlowNodeStart(appFlow.getFlow().getId());
 		String type = flowNode.getNode().getType();
 		//forward request to tej depending on the type
 		/*if(){
 			
 		}*/
 		
 		//get all the events from the node
 		//List<Event> events = eventDao.getEventBySourceNode(flowNode.getNode().getId()); 
 		
 		/*for(Event e:events){
 			type = e.getNodeTarget().getType();
 			// check if the node has parent. then create a new folder structure.
 			
 			// forward to tej pr [rof 
 		}*/
 	}
 	
 	public void testPath(){
 		System.out.println("tomcat path ---"+System.getProperty("catalina.home"));
 		
 		//publishApp(model, request);
 	}
 }
