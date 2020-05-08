 package org.dhara.portal.web.controllers;
 
 import org.apache.airavata.registry.api.workflow.ExperimentData;
 import org.apache.airavata.registry.api.workflow.NodeExecutionData;
 import org.apache.log4j.Logger;
 import org.dhara.portal.web.airavataService.AiravataClientAPIService;
 import org.dhara.portal.web.helper.ExperimentHelper;
 import org.dhara.portal.web.helper.Nodehelper;
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
 
     static Logger log = Logger.getLogger(ExperimentController.class);
 


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
 
             //retrieve experiment data by id
             List<NodeExecutionData> nodeExecutionDataList = airavataClientAPIService.getWorkflowExperimentData(data.getExperimentId());
             List<Nodehelper> nodehelperList = new ArrayList<Nodehelper>();
 
             for(int i = 0; i< nodeExecutionDataList.size();i++){
                 Nodehelper nodehelper = new Nodehelper();
                 nodehelper.setType(nodeExecutionDataList.get(i).getType().name());
                 nodehelper.setInput(nodeExecutionDataList.get(i).getInput());
                 nodehelper.setOutput(nodeExecutionDataList.get(i).getOutput());
                 nodehelper.setWorkflowInstanceNodeId(nodeExecutionDataList.get(i).getWorkflowInstanceNode().getNodeId());
                 nodehelperList.add(nodehelper);
             }
 
             helper.setNodehelperList(nodehelperList);
             experimentHelpers.add(helper);
 
         }
         experimentHelpers =  reverseList(experimentHelpers);
 
         Map paramMap = WebUtils.getParametersStartingWith(httpServletRequest, "d-");
         if (paramMap.size() == 0) {
             WebUtils.setSessionAttribute(httpServletRequest, "experiments", experimentHelpers);
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
