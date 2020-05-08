 package no.sag.activitymanager.controller;
 
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import javax.servlet.http.HttpSession;
 
 import no.sag.activitymanager.entity.GroupEntity;
 import no.sag.activitymanager.entity.Role;
 import no.sag.activitymanager.entity.UserEntity;
 import no.sag.activitymanager.repo.UserRepository;
 import no.sag.activitymanager.security.Security;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class UserController {
 
 	private static final Logger log = Logger.getLogger(UserController.class);
 	
 	private UserRepository userRepository;
 
 
 	public UserController() {
 		log.info("GroupController instantiated");
 	}
 	
 	
 	@Security(role = Role.Admin)
 	@RequestMapping(value = "/groups", method = RequestMethod.GET)
 	public ModelAndView showGroups(HttpSession session, 
 			@RequestParam(value = "id", defaultValue = "0") Long groupId) {
 		
 		UserEntity user = (UserEntity)session.getAttribute("user");
 		log.info(String.format("Showing groups for user with id=%s", user.getId()));
 		
 		GroupEntity group = null;
 		if (groupId == 0) {
 			group = user.getFirstGroup();
 		} else {
 			group = user.getGroup(groupId);
 		}
 		
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("group", group);
 		return modelAndView;
 	}
 	
 	@Security(role = Role.Admin)
 	@RequestMapping(value = "/group/delete", method = RequestMethod.GET)
 	public ModelAndView deleteGroup(HttpSession session, 
 			@RequestParam("userid") Long userId, 
 			@RequestParam("groupid") Long groupId) {
 		
 		log.info(String.format("Deleting group with id=%s", groupId));
 		
 		UserEntity user = (UserEntity)session.getAttribute("user");
 		GroupEntity group = user.getGroup(groupId);
 		
 		user.removeGroup(group);
 		userRepository.delete(group);
 		
 		group = user.getFirstGroup();
 		
 		
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("group", group);
 		modelAndView.setViewName("/groups");
 		return modelAndView;
 	}
 	
 	@Security(role = Role.Admin)
 	@RequestMapping(value = "/group/form", method = RequestMethod.GET)
 	public ModelAndView showGroupForm() {
 		log.info("Displaying group form");
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.setViewName("/groupform");
 		return modelAndView;
 	}
 	
 	@Security(role = Role.Admin)
 	@RequestMapping(value = "/group/new", method = RequestMethod.POST)
 	public ModelAndView creatNewGroup(HttpSession session, @RequestParam("name") String name) {
 		UserEntity user = (UserEntity)session.getAttribute("user");
 		GroupEntity group = new GroupEntity();
 		group.setName(name);
 		group.setOwner(user.getId());
 		user.addGroup(group);
 		group.addMember(user);
 		userRepository.update(user);
 		
 		log.info("Created group with name=" + name);
 		
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("group", group);
 		modelAndView.setViewName("/groups");
 		return modelAndView;
 	}
 	
 	
 	@Security(role = Role.Admin)
 	@RequestMapping(value = "/group/member/delete", method = RequestMethod.GET)
 	public ModelAndView deleteMemberFromGroup(HttpSession session, 
 			@RequestParam("userid") Long userId, 
 			@RequestParam("groupid") Long groupId) {
 		
 		log.info(String.format("Deleting member with id=%s from group with id=%s", userId, groupId));
 		
 		UserEntity user = (UserEntity)session.getAttribute("user");
 		GroupEntity group = user.getGroup(groupId);
 		group.removeMember(userId);
 		userRepository.update(user);
 		
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("group", group);
 		modelAndView.setViewName("/groups");
 		return modelAndView;
 	}
 	
 	@Security(role = Role.Admin)
 	@RequestMapping(value = "/group/member/add", method = RequestMethod.GET)
 	public ModelAndView newMember(@RequestParam("groupid") Long groupId) {
 		log.info("/group/member/new");
 		ModelAndView mav = new ModelAndView();
 		mav.addObject("groupid", groupId);
 		mav.setViewName("/memberform");
 		return mav;
 	}
 	
 	@Security(role = Role.Admin)
 	@RequestMapping(value = "/group/member/add/find", method = RequestMethod.POST) 
 	public ModelAndView findMember(HttpSession session,
 			@RequestParam("groupid") Long groupId,
 			@RequestParam("email") String email)
 	{
 		log.info("/group/member/add/find");
 		ModelAndView mav = new ModelAndView();
 		UserEntity user = userRepository.find(email);
 		if (user != null) {
 			mav.addObject("member", user);
 		}
 		mav.addObject("email", email);
 		mav.addObject("groupid", groupId);
 		mav.setViewName("/memberform");
 		return mav;
 	}
 	
 	@Security(role = Role.Admin)
 	@RequestMapping(value = "/group/member/add/new", method = RequestMethod.POST) 
 	public ModelAndView newGroupMember(HttpSession session,
 			@RequestParam("groupid") Long groupId,
 			@RequestParam("email") String email,
 			@RequestParam("yes") String yes) {
 		
 		log.info("/group/member/add/new/");
 		
 		UserEntity groupOwner = (UserEntity)session.getAttribute("user");
 		
 		if (yes != null) {
 			return addGroupMember(groupOwner, groupId, email);
 		}
 		
 		ModelAndView mav = new ModelAndView();
 		GroupEntity group = groupOwner.getFirstGroup(); //TODO What if no groups???
 		mav.addObject("group", group);
 		mav.setViewName("/groups");
 		return mav;
 	}
 	
 	
 	private ModelAndView addGroupMember(UserEntity groupOwner, Long groupId, String email) {
 		UserEntity member = userRepository.find(email);
 		if (member == null) {
 			//TODO: Redirect to error page.
 		}
 		
 		GroupEntity group = groupOwner.getGroup(groupId);
 		group.addMember(member);
 		userRepository.update(group);
 		
 		ModelAndView mav = new ModelAndView();
 		mav.addObject("group", group);
 		mav.setViewName("/groups");
 		return mav;
 	}
 	
 	@Security(role = Role.Admin)
 	@RequestMapping(value = "/group/member/add/create", method = RequestMethod.POST) 
 	public ModelAndView createGroupMember(HttpSession session,
 			@RequestParam("groupid") Long groupId,
 			@RequestParam("email") String email,
 			@RequestParam("firstname") String firstName,
 			@RequestParam("lastname") String lastName,
 			@RequestParam("phone") String phone) {
 		
 		UserEntity groupOwner = (UserEntity)session.getAttribute("user");
 		GroupEntity group = groupOwner.getGroup(groupId);
 		
 		UserEntity member = new UserEntity();
 		member.setEmail(email);
 		member.setPassword(firstName.toLowerCase());//TODO: Generate password
 		member.setFirstName(firstName);
 		member.setLastName(lastName);
 		member.setPhone(phone);
 		member.setRole(Role.NormalUser);
 		
 		group.addMember(member);
 		userRepository.save(member);
 		
 		userRepository.update(groupOwner);
 		
 		ModelAndView mav = new ModelAndView();
 		mav.addObject("group", group);
 		mav.setViewName("/groups");
 		return mav;
 	}
 	
 	//@Security(role = Role.Admin)
 	@RequestMapping(value = "/group/activity/new", method = RequestMethod.GET) 
 	public ModelAndView createGroupMember(HttpSession session) {
 		
 		String f = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
 		System.out.println(new Date().toString());
 		System.out.println(f);
 		ModelAndView mav = new ModelAndView();
 		mav.addObject("now", f);
 		mav.setViewName("/activityform");
 		return mav;
 	}
 			
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public ModelAndView test() {
		log.info("/test");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/testpage");
		return mav;
	}
	
 	@Required
 	public void setUserRepository(UserRepository userRepository) {
 		this.userRepository = userRepository;
 	}
 	
 }
