 package com.jpizarro.th.server.game.view.rest;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.jpizarro.th.lib.game.entity.CreateGameTO;
 import com.jpizarro.th.lib.game.entity.GameTO;
 import com.jpizarro.th.lib.game.entity.list.GamesTO;
 import com.jpizarro.th.lib.game.entity.list.TeamsTO;
 import com.jpizarro.th.lib.game.entity.response.GenericGameResponseTO;
 import com.jpizarro.th.lib.game.entity.response.InGameUserInfoTO;
 import com.jpizarro.th.lib.message.entity.MessageTO;
 import com.jpizarro.th.lib.place.entity.PlaceTO;
 import com.jpizarro.th.lib.team.entity.TeamTO;
 import com.jpizarro.th.lib.user.entity.UserTO;
 import com.jpizarro.th.server.game.model.service.GameService;
 import com.jpizarro.th.server.generic.model.persistence.util.exceptions.DuplicateInstanceException;
 import com.jpizarro.th.server.generic.model.persistence.util.exceptions.InstanceNotFoundException;
 import com.jpizarro.th.server.generic.view.rest.GenericController;
 import com.jpizarro.th.server.user.view.rest.client.MessageRestClient;
 import com.jpizarro.th.server.user.view.rest.client.PlaceRestClient;
 import com.jpizarro.th.server.user.view.rest.client.UserRestClient;
 import com.jpizarro.th.server.user.view.rest.client.TeamRestClient;
 
 import es.sonxurxo.androidrunner.model.service.game.util.exception.TimeOutException;
 
 @Controller
 @RequestMapping("/test")
 public class TestController{
 	@Autowired
 	private GameService gameService;
 	private String XML_VIEW_NAME = "users";
 	
 	@Autowired
 	private UserRestClient userRestClient;
 	@Autowired
 	private TeamRestClient teamRestClient;
 	@Autowired
 	private PlaceRestClient placeRestClient;
 	@Autowired
 	private MessageRestClient messageRestClient;
 	
 	@RequestMapping(method=RequestMethod.GET, value="/users/{id}",
 			headers="Accept=application/xml")
 	@ResponseBody
 	public UserTO getEntityRest(@PathVariable Long id) {
 		return userRestClient.getEntity(id);
 	}
 	
 	@RequestMapping(method=RequestMethod.DELETE, value="/users/{id}",
 			headers="Accept=application/xml")
 	@ResponseBody
 	public Object removeEntityRest(@PathVariable Long id) {
 		return userRestClient.removeEntity(id);
 	}
 
 	@RequestMapping(method=RequestMethod.POST, value="/users",
 			headers="Accept=application/xml")
 	@ResponseBody
 	public Object addEntityRest() {
 		UserTO to = new UserTO();
 		to.setUsername("joteiro");
 		return userRestClient.addEntity(to);
 	}
 	@RequestMapping(method=RequestMethod.PUT, value="/users/{id}")
 	@ResponseBody
 	public UserTO updateEntityRest(@PathVariable Long id) {
 		UserTO body = new UserTO();
 		body.setUserId(id);
 		body.setName("juan");
 		return userRestClient.updateEntity(id, body);
 	}
 	
 	@RequestMapping(method=RequestMethod.GET, value="/teams/{id}",
 			headers="Accept=application/xml")
 	@ResponseBody
 	public TeamTO getEntityTeam(@PathVariable Long id) {
 		return teamRestClient.getEntity(id);
 	}
 	
 	@RequestMapping(method=RequestMethod.DELETE, value="/teams/{id}",
 			headers="Accept=application/xml")
 	@ResponseBody
 	public Object removeEntityTeam(@PathVariable Long id) {
 		return teamRestClient.removeEntity(id);
 	}
 
 	@RequestMapping(method=RequestMethod.POST, value="/teams",
 			headers="Accept=application/xml")
 	@ResponseBody
 	public Object addEntityTeam() {
 		TeamTO to = new TeamTO();
 		to.setName("joteiro");
 		return teamRestClient.addEntity(to);
 	}
 	@RequestMapping(method=RequestMethod.PUT, value="/teams/{id}")
 	@ResponseBody
 	public TeamTO updateEntityTeam(@PathVariable Long id) {
 		TeamTO body = new TeamTO();
 		body.setTeamId(id);
 		body.setDescription("juan desc");
 		return teamRestClient.updateEntity(id, body);
 	}
 	
 	@RequestMapping(method=RequestMethod.GET, value="/places/{id}",
 			headers="Accept=application/xml")
 	@ResponseBody
 	public PlaceTO getEntityPlace(@PathVariable Long id) {
 		return placeRestClient.getEntity(id);
 	}
 	
 	@RequestMapping(method=RequestMethod.DELETE, value="/places/{id}",
 			headers="Accept=application/xml")
 	@ResponseBody
 	public Object removeEntityPlace(@PathVariable Long id) {
 		return placeRestClient.removeEntity(id);
 	}
 
 	@RequestMapping(method=RequestMethod.POST, value="/places",
 			headers="Accept=application/xml")
 	@ResponseBody
 	public Object addEntityPlace() {
 		PlaceTO to = new PlaceTO();
 		to.setName("joteiro");
 		return placeRestClient.addEntity(to);
 	}
 	@RequestMapping(method=RequestMethod.PUT, value="/places/{id}")
 	@ResponseBody
 	public PlaceTO updateEntityPlace(@PathVariable Long id) {
 		PlaceTO body = new PlaceTO();
 		body.setPlaceId(id);
 		body.setDescription("juan desc");
 		return placeRestClient.updateEntity(id, body);
 	}
 	
 	@RequestMapping(method=RequestMethod.GET, value="/messages/{id}",
 			headers="Accept=application/xml")
 	@ResponseBody
 	public MessageTO getEntityMessage(@PathVariable Long id) {
 		return messageRestClient.getEntity(id);
 	}
 	
 	@RequestMapping(method=RequestMethod.DELETE, value="/messages/{id}",
 			headers="Accept=application/xml")
 	@ResponseBody
 	public Object removeEntityMessage(@PathVariable Long id) {
 		return messageRestClient.removeEntity(id);
 	}
 
 	@RequestMapping(method=RequestMethod.POST, value="/messages",
 			headers="Accept=application/xml")
 	@ResponseBody
 	public Object addEntityMessage() {
 		MessageTO to = new MessageTO();
 		to.setMessageBody("joteiro");
 		to.setSender(1);
 		return messageRestClient.addEntity(to);
 	}
 	@RequestMapping(method=RequestMethod.PUT, value="/messages/{id}")
 	@ResponseBody
 	public MessageTO updateEntityMessage(@PathVariable Long id) {
 		MessageTO body = new MessageTO();
 		body.setMessageId(id);
 		body.setMessageBody("juan desc");
 		return messageRestClient.updateEntity(id, body);
 	}
 	
 }
