 package ru.rpn.publicrequestform.controller.check;
 
 import java.util.Calendar;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.portlet.bind.annotation.ActionMapping;
 import org.springframework.web.portlet.bind.annotation.RenderMapping;
 
 import ru.rpn.publicrequestform.model.RequestData;
 import ru.rpn.publicrequestform.service.RequestDataService;
 import ru.rpn.publicrequestform.service.RequestSubjectService;
 
 @Controller
 @RequestMapping(value = "VIEW")
 public class CheckController {
 
 	public static final Logger LOG = Logger.getLogger(CheckController.class);
 
 	@Autowired
 	private RequestDataService requestDataService;
 	
 	@Autowired
 	private RequestSubjectService requestSubjectService;
 	
 
 	@RenderMapping
 	public String view(RenderRequest request, RenderResponse response, Model model) {
 		Calendar calendar = Calendar.getInstance();
 		String year = Integer.toString(calendar.get(Calendar.YEAR)).substring(2);
 		model.addAttribute("year", year);
 		model.addAttribute("requestSubjects", requestSubjectService.getAll());
 		return "view";
 	}
 	
 
 	@ActionMapping("find")
	public void find(ActionRequest request, ActionResponse response, Model model, @RequestParam("id") String id, @RequestParam("requestSubjectIndex") String requestSubjectIndex) {
 		
 		response.setRenderParameter("view", "result");
 		response.setRenderParameter("id", id);
		response.setRenderParameter("requestSubjectIndex", requestSubjectIndex);
 
 	}
 	
 	@RenderMapping(params="view=result")
	public String result(RenderRequest request, RenderResponse response, Model model, @RequestParam("id") String id, @RequestParam("requestSubjectIndex") String requestSubjectIndex) {
 		RequestData requestData = null;
 		Long requestDataId = null;
 		try {
 			requestDataId = Long.parseLong(id);
 		} catch (NumberFormatException e) {
 			return "result";	
 		}
 		if (requestDataId != null) {
 			requestData = requestDataService.get(requestDataId);
 		}
		if (!requestData.getRequestSubject().getIndex().equalsIgnoreCase(requestSubjectIndex)) {
			requestData = null;
		}
 		model.addAttribute("requestData", requestData);
 		response.setContentType("UTF-8");
 		return "result";	
 	}
 	
 }
