 package com.webgearz.tb.controllers;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.Assert;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.webgearz.tb.client.model.JSONView;
 import com.webgearz.tb.domain.models.Template;
 import com.webgearz.tb.domain.models.User;
 import com.webgearz.tb.domain.models.UserDomain;
 import com.webgearz.tb.services.TemplateService;
 import com.webgearz.tb.services.UserDomainService;
 import com.webgearz.tb.services.UserService;
 import com.webgearz.tb.util.TemplateFactory;
 
 /**
  * @author admin
  *
  */
 @Controller
 @SessionAttributes("userid")
 public class TemplateController {
 	
 	private TemplateService templateService;
 	
 	private static final Log log = LogFactory.getLog(TemplateController.class);
 	
 	private UserService userService;
 	
 	private TemplateFactory templateFactory;
 	
 	private UserDomainService userDomainService;
 	@RequestMapping(value="/getTemplates",method=RequestMethod.GET)
 	public ModelAndView getAllTemplates(){
 		ModelAndView mav = new ModelAndView("templates");
 		mav.getModel().put("templates", templateService.getAll());
 		mav.getModel().put("userId", null);
 		return mav;
 		
 	}
 	
 
 	@RequestMapping(value="/cms/addDomain",method=RequestMethod.POST)
 	public ModelAndView addDomain(@ModelAttribute("userid")final String userid,@RequestParam("templateid")final String templateid,@RequestParam("domainName")final String domainName){
 		ModelAndView mav = new ModelAndView();
 		mav.setView(new JSONView());
 		User user = userService.findUser(userid);
 		Assert.notNull(user,"Could not find user");
 		
 		boolean result = userService.addDomains(user, new UserDomain(domainName,templateid));
 		
 		mav.getModel().put("result", result);
 		return mav;
 	}
 	/**
 	 * Should return the user specific template.. If user has not changed a particular the default text should appear
 	 * @param userId
 	 * @param domainName
 	 * @return
 	 */
 	@RequestMapping(value="/cms/getTemplate/{templateId}/{domainId}",method=RequestMethod.GET)
 	public ModelAndView getUserTemplate(@PathVariable("domainId")final String domainId,@PathVariable("templateId") final String templateId){
 		
 		Template template = templateService.findTemplate(templateId);
 		UserDomain userDomain = userDomainService.findUserDomainById(domainId);
 		Assert.notNull(template,"Could not find template in database!");
 		Assert.notNull(userDomain,"Could not find user domain");
 		ModelAndView mav = templateFactory.getTemplate(template.getTemplateName(), "index");
 		mav.getModel().put("domainId", userDomain.getId());
 		return mav;
 	}
 	@Autowired
 	public void setTemplateService(TemplateService templateService) {
 		this.templateService = templateService;
 	}
 	public TemplateService getTemplateService() {
 		return templateService;
 	}
 
 	@Autowired
 	public void setUserService(UserService userService) {
 		this.userService = userService;
 	}
 
 	public UserService getUserService() {
 		return userService;
 	}
 	
 
 
 
 	@Autowired
 	public void setTemplateFactory(TemplateFactory templateFactory) {
 		this.templateFactory = templateFactory;
 	}
 
 
 	public TemplateFactory getTemplateFactory() {
 		return templateFactory;
 	}
 
 
 	@Autowired
 	public void setUserDomainService(UserDomainService userDomainService) {
 		this.userDomainService = userDomainService;
 	}
 
 
 	public UserDomainService getUserDomainService() {
 		return userDomainService;
 	}
 
 }
 
