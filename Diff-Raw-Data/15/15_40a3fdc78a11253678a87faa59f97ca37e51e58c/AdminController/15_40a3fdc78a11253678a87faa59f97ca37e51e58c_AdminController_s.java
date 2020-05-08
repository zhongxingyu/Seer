 package com.youthministry.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.youthministry.domain.Group;
 import com.youthministry.domain.User;
 import com.youthministry.domain.Image;
 import com.youthministry.domain.TextEntry;
 import com.youthministry.domain.Event;
 import com.youthministry.domain.Role;
 import com.youthministry.domain.Page;
 import com.youthministry.domain.EventLocation;
 import com.youthministry.service.EventService;
 import com.youthministry.service.GroupService;
 import com.youthministry.service.PageContentService;
 import com.youthministry.service.PageService;
 import com.youthministry.service.RoleService;
 import com.youthministry.service.UserService;
 
 @Controller
 public class AdminController {
 
 	@Autowired
 	protected GroupService GroupService;
 	@Autowired
 	protected PageService PageService;
 	@Autowired
 	protected UserService UserService;
 	@Autowired
 	protected PageContentService PageContentService;
 	@Autowired
 	protected EventService EventService;
 	@Autowired
 	protected RoleService RoleService;
 	
 	protected Validator validator;
 	
 	@RequestMapping(value={"/admin"},method=RequestMethod.GET)
 	public String admin(Model map) {
 		map.addAttribute("users", UserService.getUsers());
 		map.addAttribute("groups", GroupService.getGroups());
 		map.addAttribute("contentItems", PageContentService.getAllPageContent());
 		map.addAttribute("events", EventService.getEvents());
 		map.addAttribute("roles", RoleService.getRoles());
 		map.addAttribute("pages", PageService.getPages());
 		return "admin";
 	}
 	@ModelAttribute(value="user")
 	public User getUser() {
 		return new User();
 	}
 	@ModelAttribute(value="group")
 	public Group getGroup() {
 		return new Group();
 	}
 	@ModelAttribute(value="image")
 	public Image getImage() {
 		return new Image();
 	}
	@ModelAttribute(value="textEntry")
	public TextEntry getTextEntry() {
		return new TextEntry();
 	}
 	@ModelAttribute(value="event")
 	public Event getEvent() {
 		return new Event();
 	}
 	@ModelAttribute(value="eventLocation")
 	public EventLocation getEventLocation() {
 		return new EventLocation();
 	}
 	@ModelAttribute(value="role")
 	public Role getRole() {
 		return new Role();
 	}
 	@ModelAttribute(value="page")
 	public Page getPage() {
 		return new Page();
 	}
 }
