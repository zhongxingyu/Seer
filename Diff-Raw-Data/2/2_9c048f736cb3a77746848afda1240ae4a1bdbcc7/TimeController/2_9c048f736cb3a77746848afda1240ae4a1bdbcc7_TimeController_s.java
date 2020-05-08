 package com.khs.example.controller;
 
 import java.util.Date;
 
 import javax.portlet.PortletConfig;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.portlet.bind.annotation.RenderMapping;
 import org.springframework.web.portlet.context.PortletConfigAware;
 
 @Controller(value = "time")
 @RequestMapping(value = "VIEW")
 @SessionAttributes({ "time" })
 public class TimeController implements PortletConfigAware {
 
 	// /** Utilities **/
 	// @ModelAttribute("time")
 	// protected TimeModel createTime(ModelMap modelMap) {
 	// return new TimeModel();
 	// }
 
 	/** default renderer **/
 	@RenderMapping
 	public String show(RenderResponse renderResponse, RenderRequest request, Model model) {
 
 		String userid = request.getRemoteUser();
 		// User liferayUser = UserLocalServiceUtil.getUserByEmailAddress(company.getCompanyId(), userid);
 		model.addAttribute("time", new Date());
 
 		return "time/time";
 	}
 
	@Override
 	public void setPortletConfig(PortletConfig portletConfig) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
