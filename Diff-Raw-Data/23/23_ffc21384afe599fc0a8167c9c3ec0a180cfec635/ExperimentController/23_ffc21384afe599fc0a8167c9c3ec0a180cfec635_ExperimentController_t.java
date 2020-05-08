 package org.dhara.portal.web.controllers;
 
 import org.apache.airavata.registry.api.workflow.ExperimentData;
 import org.dhara.portal.web.airavataService.AiravataClientAPIService;
 import org.dhara.portal.web.helper.ExperimentHelper;
 import org.springframework.context.ApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.util.WebUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: nipuni
  * Date: 8/11/13
  * Time: 6:05 AM
  * To change this template use File | Settings | File Templates.
  */
 public class ExperimentController extends AbstractController {
 
     @Override
     protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
         ApplicationContext context= WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
         AiravataClientAPIService airavataClientAPIService= (AiravataClientAPIService) context.getBean("airavataAPIService");
 
         List<ExperimentData> experimentData = airavataClientAPIService.getExperimentData();
 
         List<ExperimentHelper> experimentHelpers=new ArrayList<ExperimentHelper>();
         for(int index = 0; index<experimentData.size();index++){
              ExperimentHelper helper = new ExperimentHelper();
              ExperimentData data = experimentData.get(index);
              helper.setName(data.getExperimentName());
              helper.setUpdatedTime(data.getStatusUpdateTime());
              helper.setAuthor(data.getUser());
              helper.setState(data.getState().toString());
              experimentHelpers.add(helper);
 
         }
         experimentHelpers =  reverseList(experimentHelpers);
 
        Map paramMap = WebUtils.getParametersStartingWith(httpServletRequest, "d-");
        if (paramMap.size() == 0) {
            WebUtils.setSessionAttribute(httpServletRequest, "userList", experimentHelpers);
        }

         ModelAndView model = new ModelAndView("experiments");
         model.addObject("message", experimentHelpers);
         return model;
     }
 
     public ArrayList<ExperimentHelper> reverseList(List<ExperimentHelper> list){
 
         if(list==null || list.isEmpty())
         {
             return null;
         }
 
         ArrayList<ExperimentHelper> riversedlist = new ArrayList<ExperimentHelper>(list);
 
         Collections.reverse(riversedlist);
         return riversedlist;
     }
 }
