 package org.imirsel.nema.webapp.controller;
 
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.imirsel.nema.Constants;
 import org.imirsel.nema.flowservice.FlowService;
 import org.imirsel.nema.model.Flow;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 
 /**
  * This controller exposes list of flows.
  * @author kumaramit01
  * @since 0.4.0
  *
  */
 public class FlowController extends MultiActionController{
 
 	private final static Logger log = Logger.getLogger(FlowController.class.getName());
 	private FlowService flowService = null;
 
 	public FlowService getFlowService() {
 		return flowService;
 	}
 
 
 	public void setFlowService(FlowService flowService) {
 		this.flowService = flowService;
 	}
 
 	/**Returns a view that displays Template Flows
 	 * 
 	 * @param req
 	 * @param res
 	 * @return flow/flowType.jsp
 	 */
 	public ModelAndView getTemplateFlows(HttpServletRequest req, HttpServletResponse res){
 		String type = req.getParameter("type");
 		if(type==null){
 			type="all";
 		}
 		
 		ModelAndView mav;
 		String uri = req.getRequestURI();
 		if (uri.substring(uri.length() - 4).equalsIgnoreCase("json")) {
 			mav = new ModelAndView("jsonView");
 		} else {
 			mav =new ModelAndView("flow/flowType");
 		}
 		Set<Flow> flowSet=this.flowService.getFlowTemplates();
 		if(!type.equalsIgnoreCase("all")){
 		ArrayList<Flow> list = new ArrayList<Flow>();
 		for(Flow flow:flowSet){
 			log.info("Name is: "+ flow.getName());
 			log.info("Flow Type is: " + flow.getTypeName());
			//System.out.println(flow.getType());
 			//if(flow.getTypeName().toUpperCase().indexOf(type.toUpperCase())!=-1){
 			//	System.out.println("adding: " + flow.getName() + "     "+flow.getType());
 				list.add(flow);
 		   //}
 			
 		}
 		log.info("done loading for flowlist");
 		mav.addObject(Constants.FLOW_LIST, list);
 		}else{
 		log.info("done loading for flowlist");
 		mav.addObject(Constants.FLOW_LIST, flowSet);
 		}
 		mav.addObject(Constants.FLOW_TYPE, type);
 		return mav;
 	} 
 	
 
 
 }
