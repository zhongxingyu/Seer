 package mum.compro.onlineapp;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller
 public class PersonalInfoController {
 
 	@Resource
 	private PersonalInfoService personalInfoService;
 
 	@RequestMapping(value = "/application/personalinfo", method = RequestMethod.GET)
 	public String personalinfo(Model model, HttpSession session) {
 		User user = (User) session.getAttribute("user");
 		if (user != null) {
 			long id = user.getId();
 			PersonalInfo personalInfo = personalInfoService.getPersonalInfo(id);
 			if (personalInfo != null)
 				model.addAttribute("personalInfo",
						personalInfoService.getPersonalInfo(id));
 			model.addAttribute("countryList",
 					personalInfoService.getAllCountryList());
 			return "personalinfo";
 		} else {
 			return "login";
 		}
 	}
 
 	@RequestMapping(value = "/application/personalinfo", method = RequestMethod.POST)
 	public String registration(@RequestParam("month") String month,
 			@RequestParam("day") String day, @RequestParam("year") String year,
 			PersonalInfo personalinfo) {
 		personalInfoService.updatePersonalInfo(personalinfo, month, day, year);
 		return "redirect:/application";
 	}
 
 	@RequestMapping(value = "/application/personalinfo/{id}", method = RequestMethod.POST)
 	public String update(PersonalInfo personalInfo, @PathVariable long id,
 			@RequestParam("month") String month,
 			@RequestParam("day") String day, @RequestParam("year") String year) {
 		personalInfoService.updatePersonalInfo(personalInfo, month, day, year);
 		return "redirect:/application";
 	}
 }
