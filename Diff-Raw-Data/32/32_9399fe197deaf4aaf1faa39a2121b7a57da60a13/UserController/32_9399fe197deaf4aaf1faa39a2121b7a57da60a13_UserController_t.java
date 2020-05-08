 package org.huamuzhen.oa.server.controller;
 
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.huamuzhen.oa.biz.OrgUnitManager;
 import org.huamuzhen.oa.biz.UserManager;
 import org.huamuzhen.oa.domain.entity.OrgUnit;
 import org.huamuzhen.oa.domain.entity.User;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
import com.mysql.jdbc.StringUtils;

 @Controller
 @RequestMapping("/user")
 public class UserController {
 	
 	@Resource
 	private UserManager userManager;
 	
 	@Resource
 	private OrgUnitManager orgUnitManager;
 	
 	@RequestMapping(value = { "", "/" })
 	public ModelAndView list(HttpServletResponse response) throws IOException{
 		List<User> userList = userManager.findAllUser();
 		ModelAndView mav = new ModelAndView("user");
 		mav.addObject("userList", userList);
 		return mav;
 	}
 	
 	@RequestMapping(value="/add",method=RequestMethod.POST)
 	public String add(HttpServletRequest request){
 		String username = request.getParameter("username");
 		String password = request.getParameter("password");
 		String description = request.getParameter("description");
 		String orgUnitId = request.getParameter("orgUnitId");
		if(StringUtils.isNullOrEmpty(orgUnitId)){
			orgUnitId = null;
		}
 		String privilege = request.getParameter("privilege");
 		userManager.saveUser(null, username, password, description, orgUnitId, privilege);
 		return "redirect:/user";
 	}
 	
 	@RequestMapping(value="/edit",method=RequestMethod.POST)
 	public String edit(HttpServletRequest request){
 		String id = request.getParameter("id");
 		String username = request.getParameter("username");
 		String password = request.getParameter("password");
 		String description = request.getParameter("description");
 		String orgUnitId = request.getParameter("orgUnitId");
		if(StringUtils.isNullOrEmpty(orgUnitId)){
			orgUnitId = null;
		}
 		String privilege = request.getParameter("privilege");
 		userManager.saveUser(id, username, password, description, orgUnitId, privilege);
 		return "redirect:/user";
 	}
 	
 	@RequestMapping(value="/addUser")
 	public ModelAndView addUser(){
 		ModelAndView mav = new ModelAndView("addUser");
 		List<OrgUnit> orgUnitList = orgUnitManager.findAllOrgUnit();
 		mav.addObject("orgUnitList", orgUnitList);
 		return mav;
 	}
 	
 	@RequestMapping(value="/editUser/{id}")
 	public ModelAndView editUser(@PathVariable String id){
 		ModelAndView mav = new ModelAndView("editUser");
 		List<OrgUnit> orgUnitList = orgUnitManager.findAllOrgUnit();
 		mav.addObject("orgUnitList", orgUnitList);
 		User selectedUser = userManager.findOne(id);
 		mav.addObject("selectedUser", selectedUser);
 		return mav;
 	}
 	
 	@RequestMapping(value="/delete/{id}",method=RequestMethod.POST)
 	public String delete(@PathVariable String id){
 		userManager.deleteUser(id);
 		return "redirect:/user";
 	}
 	
 }
