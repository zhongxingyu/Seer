 /**
  * 
  */
 package com.welflex.service;
 
 import org.lacassandra.smooshyfaces.entity.User;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * @author UPAPIFR
  *
  */
 @RequestMapping("/user")
 public class UserServiceRestEndpoint {
 
	@RequestMapping( method = RequestMethod.POST, value = "/create")
 	@ResponseBody
 	public User create(@RequestBody User newUser) {
 		
 		User u = null;
 //		User u = userService.create(newUser);
 		return u;
 	}
 
	@RequestMapping(value = "/{userid}/like/{isbn}", method = RequestMethod.GET)
 	@ResponseBody
 	public String likeBook(@PathVariable String userid, @PathVariable String isbn)
 	{
 //		User u = userService.like(userid,isbn);
 		return "I like You";
 	}
 
 
	@RequestMapping(value = "/{userid}/dislike/{isbn}", method = RequestMethod.GET)
 	@ResponseBody
	public String disLikeBook(@PathVariable String userid, @PathVariable String isbn)
 	{
 //		User u = userService.create(newUser);
 		return "I hate You";
 	}
 
 }
