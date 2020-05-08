 package com.nelsonjrodrigues.twitter.web.rest;
 
 import java.util.List;
 import java.util.Objects;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.Assert;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import com.nelsonjrodrigues.twitter.data.model.User;
 import com.nelsonjrodrigues.twitter.repositories.UserRepository;
 import com.nelsonjrodrigues.twitter.services.UserService;
 
 @Controller
 @RequestMapping("/users")
 public class UsersApi {
 
 	@Autowired
 	private UserService userService;
 
 	@Autowired
 	private UserRepository userRepository;
 
 	@ResponseBody
 	@RequestMapping(method = RequestMethod.GET)
 	public List<User> findAll() {
 		return userRepository.findAll();
 	}
 
 	@ResponseBody
 	@ResponseStatus(HttpStatus.CREATED)
 	@RequestMapping(method = RequestMethod.POST)
 	public User create(@RequestBody User user) {
 		Objects.requireNonNull(user);
 
 		userRepository.save(user);
 
 		return user;
 	}
 
 	@ResponseBody
 	@RequestMapping(value = "/{username}", method = RequestMethod.GET)
 	public User retrieve(@PathVariable("username") String username) {
 		return userRepository.findByUsername(username);
 	}
 
 	@ResponseBody
 	@RequestMapping(value = "/{username}/followers", method = RequestMethod.GET)
 	public List<User> followers(@PathVariable("username") String username) {
 		Assert.hasText(username);
 
 		return userService.followers(username);
 	}
 
 	@ResponseStatus(HttpStatus.CREATED)
 	@RequestMapping(value = "/{followed}/followers", method = RequestMethod.POST)
 	public void beFollowed(@RequestBody User follower, @PathVariable("followed") String followed) {
 		Objects.requireNonNull(follower);
 		Assert.hasText(followed);
 
 		userService.follow(follower.getUsername(), followed);
 	}
 
 	@ResponseStatus(HttpStatus.OK)
 	@RequestMapping(value = "/{username}/followers/{follower}", method = RequestMethod.DELETE)
 	public void shakeOffFollower(@PathVariable("username") String username, @PathVariable("follower") String follower) {
 		Assert.hasText(follower);
 		Assert.hasText(username);
 
 		userService.unfollow(follower, username);
 	}
 
 	@ResponseBody
 	@RequestMapping(value = "/{username}/following", method = RequestMethod.GET)
 	public List<User> following(@PathVariable("username") String username) {
 		Assert.hasText(username);
 
 		return userService.following(username);
 	}
 
 	@ResponseStatus(HttpStatus.CREATED)
 	@RequestMapping(value = "/{username}/following", method = RequestMethod.POST)
	public void follow(@PathVariable("username") String follower, @RequestBody User followed) {
 		Assert.hasText(follower);
 		Objects.requireNonNull(followed);
 
 		userService.follow(follower, followed.getUsername());
 	}
 
 	@ResponseStatus(HttpStatus.OK)
 	@RequestMapping(value = "/{follower}/following/{username}", method = RequestMethod.DELETE)
 	public void unfollow(@PathVariable("follower") String follower, @PathVariable String username) {
 		Assert.hasText(follower);
 		Assert.hasText(username);
 
 		userService.unfollow(follower, username);
 	}
 
 }
