 package by.itransition.fanfic.web.controller.visitPageController;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import by.itransition.fanfic.domain.Fanfic;
 import by.itransition.fanfic.domain.User;
 import by.itransition.fanfic.service.CategoryService;
 import by.itransition.fanfic.service.TagService;
 import by.itransition.fanfic.service.UserService;
 
 @Controller
 @RequestMapping("/createFanfic")
 public class CreateFanficController extends InputFanficController {
 
 	@Autowired
 	private UserService userService;
 	
 	@Autowired
 	private CategoryService categoryService;
 	
 	@Autowired
 	private TagService tagService;
 	
 	@RequestMapping(method = RequestMethod.GET)
	public String getFanficCreationForm(Model model){
 		settingModel(model);
 		model.addAttribute("newFanfic", new Fanfic());
 		return "createFanfic";
 	}
 	
 	@RequestMapping(method = RequestMethod.POST)
 	public String createFanfic(@ModelAttribute("newFanfic") Fanfic fanfic,
 			BindingResult bindingResult) {
 		User user = userService.getUserByName(
 				SecurityContextHolder.getContext().getAuthentication().getName());
 		correctFanfic(fanfic);
 		user.addFanfic(fanfic);
 		userService.save(user);
 		return "redirect:/user/" + user.getId();
 	}
 }
