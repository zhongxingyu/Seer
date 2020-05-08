 package com.tda.presentation.controller;
 
 import java.util.List;
 
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.tda.model.applicationuser.ApplicationUser;
 import com.tda.model.applicationuser.Authority;
 import com.tda.persistence.paginator.Order;
 import com.tda.persistence.paginator.Paginator;
 import com.tda.service.api.ApplicationUserService;
 import com.tda.service.api.AuthorityService;
 
 @Controller
 @RequestMapping(value = "/applicationUser")
 @SessionAttributes("applicationUser")
 public class ApplicationUserController {
 
 	private static final String USER_FORM_DELETE_ERROR = "user.form.deleteError";
 	private static final String USER_FORM_NOT_FOUND = "user.form.notFound";
 	private static final String USER_FORM_MESSAGE = "message";
 	private static final String USER_FORM_ADD_SUCCESSFUL = "user.form.addSuccessful";
 	private static final String REDIRECT_TO_USER_LIST = "redirect:/applicationUser";
 	private static final String USER_CREATE_FORM = "applicationUser/createForm";
 	private static final String USER_LIST = "applicationUser/list";
 
 	private Paginator paginator;
 	private ApplicationUserService applicationUserService;
 	private AuthorityService authorityService;
 
 	@Autowired
 	public void setApplicationUserService(
 			ApplicationUserService applicationUserService) {
 		this.applicationUserService = applicationUserService;
 	}
 
 	@Autowired
 	public void setAuthorityService(AuthorityService authorityService) {
 		this.authorityService = authorityService;
 	}
 
 	@Autowired
 	public void setPaginator(Paginator paginator) {
 		this.paginator = paginator;
 		paginator.setOrder(Order.asc);
 		paginator.setOrderField("id");
 	}
 
 	@ModelAttribute("allAuthorities")
 	public List<Authority> populateAuthorities() {
 		return authorityService.findAll();
 	}
 
 	@RequestMapping(value = "/add", method = RequestMethod.GET)
 	public String getCreateForm(Model model) {
 		model.addAttribute(new ApplicationUser());
 
 		return USER_CREATE_FORM;
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public ModelAndView create(Model model,
 			@Valid @ModelAttribute ApplicationUser applicationUser,
 			BindingResult result) {
 		ModelAndView modelAndView = new ModelAndView();
 
 		// TODO if we're editing and not adding a new item the message
 		// seems somewhat... misleading, CHANGE IT :D
 		if (result.hasErrors()) {
 			modelAndView.setViewName(USER_CREATE_FORM);
 		} else {
 			modelAndView.setViewName(REDIRECT_TO_USER_LIST);
 			modelAndView.addObject(USER_FORM_MESSAGE, USER_FORM_ADD_SUCCESSFUL);
 			applicationUserService.save(applicationUser);
 		}
 
 		return modelAndView;
 	}
 
 	@RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
 	public String getUpdateForm(@PathVariable Long id, Model model) {
 		ApplicationUser applicationUser = applicationUserService.findById(id);
 		model.addAttribute("applicationUser", applicationUser);
 
 		return USER_CREATE_FORM;
 	}
 
 	@RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
 	public ModelAndView deleteItem(@PathVariable Long id) {
 		ModelAndView modelAndView = new ModelAndView(REDIRECT_TO_USER_LIST);
 		ApplicationUser applicationUser = applicationUserService.findById(id);
 
 		if (applicationUser == null) {
 			modelAndView.addObject(USER_FORM_MESSAGE, USER_FORM_NOT_FOUND);
 		} else {
 
 			try {
 				applicationUserService.delete(applicationUser);
 			} catch (Exception e) {
 				modelAndView.addObject(USER_FORM_MESSAGE,
 						USER_FORM_DELETE_ERROR);
 			}
 		}
 		return modelAndView;
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView getList(
 			@RequestParam(value = "page", required = false) Integer pageNumber) {
 		ModelAndView modelAndView = new ModelAndView(USER_LIST);
 
 		if (pageNumber != null) {
 			paginator.setPageIndex(pageNumber);
 		}
 
 		modelAndView.addObject("applicationUserList",
 				applicationUserService.findAllPaged(paginator));
 		modelAndView.addObject("paginator", paginator);
 
 		return modelAndView;
 	}
 }
