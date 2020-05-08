 package rtr.mvc;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import rtr.domain.Receptiveness;
 import rtr.repo.ReceptivenessInterface;
 
 
 @Controller
 @RequestMapping("/presenter")
 public class PresenterController {
 	
 	@Autowired
 	private ReceptivenessInterface receptiveness;
 	
 	@RequestMapping(method=RequestMethod.GET, value="/startPresentation")
 	public String start(HttpServletRequest request, Model model) {
 		return "presenter/statistics";
 	}
 	
 	@RequestMapping(method=RequestMethod.GET, value="/statistics")	 
 	public @ResponseBody Receptiveness refresh(HttpServletRequest request, Model model){
 		String courseId = (String) request.getSession().getAttribute("courseId");
 		return receptiveness.getReceptiveness(courseId);
 	}	
 }
