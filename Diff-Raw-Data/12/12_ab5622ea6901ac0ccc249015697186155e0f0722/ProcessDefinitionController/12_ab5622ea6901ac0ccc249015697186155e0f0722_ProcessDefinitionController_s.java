 package gov.loc.repository.transfer.ui.controllers;
 
 import gov.loc.repository.transfer.ui.UIConstants;
 import gov.loc.repository.transfer.ui.dao.WorkflowDao;
 import gov.loc.repository.transfer.ui.model.ProcessDefinitionBean;
 import gov.loc.repository.transfer.ui.model.ProcessInstanceBean;
 import gov.loc.repository.transfer.ui.model.UserBean;
 import gov.loc.repository.transfer.ui.model.WorkflowBeanFactory;
 import gov.loc.repository.transfer.ui.springframework.ModelAndView;
 import gov.loc.repository.transfer.ui.utilities.PermissionsHelper;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class ProcessDefinitionController extends AbstractRestController {
 
 	public static final String PROCESSDEFINITIONID = "processDefinitionId";
 
 	@Override
 	public String getUrlParameterDescription() {
 		return "processdefinition/{processDefinitionId}\\.{format}";
 	}
 
 	@RequestMapping
 	@Override
 	public ModelAndView handleRequest(
 			HttpServletRequest request, 
 			HttpServletResponse response) throws Exception 
 	{
 		return this.handleRequestInternal(request, response);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void handlePost(
 	        HttpServletRequest request, 
 	        ModelAndView mav, 
 	        WorkflowBeanFactory factory, 
 	        WorkflowDao dao, PermissionsHelper permissionsHelper, Map<String, String> urlParameterMap) throws Exception 
 	{
 		
 		if (! urlParameterMap.containsKey(PROCESSDEFINITIONID)) {
 			mav.setError(HttpServletResponse.SC_BAD_REQUEST, "Process definition id not provided");
 			return;
 		}
 		String processDefinitionId = urlParameterMap.get(PROCESSDEFINITIONID);
 		ProcessDefinitionBean processDefinitionBean = dao.getProcessDefinitionBean(processDefinitionId);
 		if (processDefinitionBean == null)
 		{
 			mav.setError(HttpServletResponse.SC_NOT_FOUND);
 			return;
 		}
 
 		UserBean userBean = factory.createUserBean(request.getUserPrincipal().getName());
 		
 		if(! userBean.getProcessDefinitionBeanList().contains(processDefinitionBean)) {
 			mav.setError(HttpServletResponse.SC_UNAUTHORIZED, "User not authorized to create processinstance");
 			return;			
 		}
 	
		ProcessInstanceBean processInstanceBean = factory.createNewProcessInstanceBean(processDefinitionBean); 
		dao.save(processInstanceBean);
		request.getSession().setAttribute(UIConstants.SESSION_MESSAGE, "A new workflow was created.");
		
 		mav.setViewName("redirect:/processinstance/index.html");
 		
 	}
 	
 }
