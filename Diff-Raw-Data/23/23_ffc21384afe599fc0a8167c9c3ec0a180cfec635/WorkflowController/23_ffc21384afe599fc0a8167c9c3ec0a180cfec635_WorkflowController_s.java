 package org.dhara.portal.web.controllers;
 
 import org.apache.airavata.workflow.model.wf.Workflow;
 import org.dhara.portal.web.airavataService.AiravataClientAPIService;
 import org.dhara.portal.web.helper.WorkflowHelper;
 import org.springframework.context.ApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: nipuni
  * Date: 8/11/13
  * Time: 1:42 PM
  * To change this template use File | Settings | File Templates.
  */
 public class WorkflowController extends AbstractController {
 
     @Override
     protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
         ApplicationContext context= WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
         AiravataClientAPIService airavataClientAPIService= (AiravataClientAPIService) context.getBean("airavataAPIService");
 
         List<Workflow> workflowList = airavataClientAPIService.getAllWorkflows();
 
         List<WorkflowHelper> workflowHelpers=new ArrayList<WorkflowHelper>();
         for(int index = 0; index<workflowList.size();index++){
             WorkflowHelper helper = new WorkflowHelper();
             Workflow workflow = workflowList.get(index);
             helper.setName(workflow.getName());
            //TODO get author and created data (need to implement methods)
             workflowHelpers.add(helper);
         }
 
         ModelAndView model = new ModelAndView("workflows");
         model.addObject("message", workflowHelpers);
         return model;
     }
 }
