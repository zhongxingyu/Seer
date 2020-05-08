 package org.huamuzhen.oa.server.controller;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 import org.huamuzhen.oa.biz.FeedbackManager;
 import org.huamuzhen.oa.biz.KeyValuePairManager;
 import org.huamuzhen.oa.domain.entity.User;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 @RequestMapping("/feedback")
 public class FeedbackController { 
 	
 	@Resource
 	private FeedbackManager feedbackManager;
 	
 	@Resource
 	private KeyValuePairManager config;
 	
 	@RequestMapping(value="add", method=RequestMethod.POST)
 	public String add(HttpServletRequest request){
 		String reportFormId = request.getParameter("reportFormId");
 		String content = request.getParameter("content");
 		String signature = request.getParameter("signature");
 		String owner = request.getParameter("owner");
 		String agreeStr = request.getParameter("agree");
 		Boolean agree = null;
 		if(agreeStr != null && !agreeStr.equals("")){
 			agree = Boolean.parseBoolean(agreeStr);
 		}
 		
 		String orgUnitId = request.getParameter("orgUnitId");
 		String currentReceiverId = request.getParameter("currentReceiverId");
		String leader2Id= request.getParameter("leader2Id");	
 		User currentUser = (User)request.getSession().getAttribute("currentUser");
 		feedbackManager.add(reportFormId,content,signature,orgUnitId,owner,currentUser, currentReceiverId, leader2Id, agree,Integer.parseInt(config.getParamsMap().get("deadlineDuration")));
 		
 		return "redirect:/reportForm";
 	}
 	
 
 }
