 package ch.hszt.mdp.web;
 
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import ch.hszt.mdp.domain.Activity;
 import ch.hszt.mdp.service.ActivityService;
 
 @Controller
 @RequestMapping("/")
 public class StreamController {
 	
 	@Autowired
 	private ActivityService activityService;
 	
 	private DateTime dt;
 	
 	public StreamController() {
 		dt = new DateTime();
 	}
 	
	public StreamController(DateTime dt) {
 		this.dt = dt;
 	}
	
 	@RequestMapping(method = RequestMethod.GET)
 	public String list(Model model) {
 		model.addAttribute("today", dt.toDate());
 		
 		dt = dt.minusDays(1);
 		model.addAttribute("yesterday", dt.toDate());
 		
 		dt = dt.minusDays(1);
 		model.addAttribute("dayBeforeYesterday", dt.toDate());
 		
 		List<Activity> activities = activityService.getActivities();
 		
 		return "stream/list";
 	}
 }
