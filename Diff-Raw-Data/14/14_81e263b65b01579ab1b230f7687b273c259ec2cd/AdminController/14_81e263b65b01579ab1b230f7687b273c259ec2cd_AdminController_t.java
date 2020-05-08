 package com.acme.fitness.webmvc.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.acme.fitness.domain.exceptions.FitnessDaoException;
 import com.acme.fitness.domain.users.Role;
 import com.acme.fitness.domain.users.Roles;
 import com.acme.fitness.domain.users.User;
 import com.acme.fitness.users.GeneralUsersService;
 import com.acme.fitness.webmvc.dto.UserWithRoles;
 
 @Controller
 @RequestMapping("/admin")
 public class AdminController {
 	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
 
 	@Autowired
 	private GeneralUsersService gus;
 	
 	public AdminController(){
 		super();
 	}
 
 
	@RequestMapping(value = "")
 	public String roles(Model model, HttpServletRequest request) {
 		List<UserWithRoles> usersWithRoles=new ArrayList<UserWithRoles>();
 		for (User u : gus.getAllUsers()) {
 			List<String> roleNames = new ArrayList<String>();
 			for (Role r : gus.getRolesbyUser(u)) {
 				roleNames.add(r.getName());
 			}
 			UserWithRoles uwr=new UserWithRoles();
 			uwr.setUser(u);
 			uwr.setRoleNames(roleNames);
 			usersWithRoles.add(uwr);
 		}
 		request.setAttribute("UsersWithRoles", usersWithRoles);
 		return "jogosultsagok";
 	}
 	
 	@RequestMapping(value = "/jogosultsagok/torol")
 	public String deleteUser(Model model, HttpServletRequest request) {
 		User user = null;
 		try {
 			user = gus.getUserByUsername(request.getParameter("username-torol"));
 		} catch (FitnessDaoException e) {
 			e.printStackTrace();
 		}
		logger.info("Blocked user: "+user.toString());
 		user.setEnabled(!user.isEnabled());
 		gus.updateUser(user);
		return "redirect:/admin";
 	}
 
 	@RequestMapping(value = "/jogosultsagok/valtoztat")
 	public String roleModify(Model model, HttpServletRequest request) {
 		User user = null;
 		try {
 			user = gus.getUserByUsername(request.getParameter("username"));
 		} catch (FitnessDaoException e) {
 			e.printStackTrace();
 		}
 		loadRole(Roles.Client.toString(), user, request);
 		loadRole(Roles.Trainer.toString(), user, request);
 		loadRole(Roles.Recepcionist.toString(), user, request);
 		loadRole(Roles.SystemAdmin.toString(), user, request);
 		loadRole(Roles.ProductAdmin.toString(), user, request);
 		return roles(model, request);
 	}
 
 	private void loadRole(String roleName, User user, HttpServletRequest request) {
 		if (request.getParameter(roleName) != null) {
 			gus.addUserRole(roleName, user);
 			logger.info("add " + roleName + " role to " + user.getUsername());
 		} else {
 			gus.removeUserRole(roleName, user);
 			logger.info("remove " + roleName + " role from " + user.getUsername());
 		}
 	}
 
 }
