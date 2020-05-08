 package com.todo.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import com.todo.domain.Role;
 import com.todo.domain.Task;
 import com.todo.domain.User;
 import com.todo.dto.TaskListDTO;
 import com.todo.dto.UserListDTO;
 import com.todo.service.TaskService;
 import com.todo.service.UserService;
 
 @Controller
 @RequestMapping("/users")
@SessionAttributes({"user"})
 public class UserController {
 
	static String  name = new String();
 	@Autowired
 	private UserService service;
 	@Autowired
 	private TaskService taskService;
 
 	@RequestMapping
 	public String getUsersPage() {
 		return "users";
 	}
 
 	@RequestMapping(value = "/records")
 	public @ResponseBody
 	UserListDTO getUsers() {
 
 		UserListDTO userListDto = new UserListDTO();
 		userListDto.setUsers(service.readAll());
 		return userListDto;
 	}
 
 	@RequestMapping(value = "/get")
 	public @ResponseBody
 	User get(@RequestBody User user) {
 		return service.read(user);
 	}
 
 	@RequestMapping(value = "/create", method = RequestMethod.POST)
 	public @ResponseBody
 	User create(@RequestParam String username, @RequestParam String password,
 			@RequestParam String firstName, @RequestParam String lastName,
			@RequestParam Integer role, @RequestParam String userstatus,
			@RequestParam String mailId) {
 
 		Role newRole = new Role();
 		newRole.setRole(role);
 
 		User newUser = new User();
 		newUser.setUsername(username);
 		newUser.setPassword(password);
 		newUser.setFirstName(firstName);
 		newUser.setLastName(lastName);
 		newUser.setRole(newRole);
		newUser.setUserStatus(userstatus);
		newUser.setMailId(mailId);
 
 		return service.create(newUser);
 	}
 
 	@RequestMapping(value = "/update", method = RequestMethod.POST)
 	public @ResponseBody
 	User update(@RequestParam String username, @RequestParam String firstName,
 			@RequestParam String lastName, @RequestParam Integer role) {
 
 		Role existingRole = new Role();
 		existingRole.setRole(role);
 
 		User existingUser = new User();
 		existingUser.setUsername(username);
 		existingUser.setFirstName(firstName);
 		existingUser.setLastName(lastName);
 		existingUser.setRole(existingRole);
 
 		return service.update(existingUser);
 	}
 
 	@RequestMapping(value = "/delete", method = RequestMethod.POST)
 	public @ResponseBody
 	Boolean delete(@RequestParam String username) {
 
 		User existingUser = new User();
 		existingUser.setUsername(username);
 
 		return service.delete(existingUser);
 	}
 
 	@RequestMapping(value = "/login", method = RequestMethod.POST)
 	public @ResponseBody
	String login(@RequestParam String username, @RequestParam String password, ModelMap map) {
 		User user = service.read(username);
 		if (user != null) {
 			if (password.equals(user.getPassword())) {
 				map.addAttribute(user);
 				return user.getUsername();
 			} else {
 				return "login failed";
 			}
 		} else {
 			return "login failed";
 		}
 	}
	
 	@RequestMapping(value = "/task", method = RequestMethod.POST)
 	public @ResponseBody
 	TaskListDTO task(@ModelAttribute User user) {
 		TaskListDTO taskListDTO = new TaskListDTO();
 		taskListDTO.setTasks(taskService.readAll(user.getUsername()));
 		return taskListDTO;
 	}
	
 	@RequestMapping(value = "/changepwd", method = RequestMethod.POST)
 	public @ResponseBody
 	Boolean changepwd(@RequestParam String username,
 			@RequestParam String oldpass, @RequestParam String confirmpass) {
 
 		User existingUser = new User();
 		existingUser.setUsername(username);
 		existingUser.setPassword(confirmpass);
 
 		return service.updatePwd(existingUser);
 	}
	
 	@RequestMapping(value = "/addtask", method = RequestMethod.POST)
 	public @ResponseBody
 	String addEditTask(@RequestParam String taskid,
 			@RequestParam String taskname, @RequestParam String taskdesc,
 			@RequestParam String priority, @RequestParam String taskstatus,
 			@RequestParam String username, @RequestParam String createduser) {
 
 		Task task = new Task();
 		task.setTaskid(taskid);
 		task.setTaskname(taskname);
 		task.setTaskdesc(taskdesc);
 		task.setPriority(priority);
 		task.setTaskstatus(taskstatus);
 		task.setUsername(username);
 		task.setCreateduser(createduser);
 		Task newTask = taskService.addEditTask(task);
 		return newTask.getUsername();
 	}
 }
