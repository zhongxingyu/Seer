 package com.credera.trails.controller;
 
 import java.util.HashMap;
 import java.util.Map;
 
 //import org.slf4j.Logger;
 //import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import com.credera.trails.model.Comment;
 import com.credera.trails.model.Direction;
 import com.credera.trails.model.Rating;
 import com.credera.trails.model.Region;
 import com.credera.trails.model.Route;
 import com.credera.trails.model.User;
 import com.credera.trails.service.RegionService;
 import com.credera.trails.service.RouteService;
 import com.credera.trails.service.UserService;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class RouteController {
 	
 	@Autowired
 	private RegionService regionService;
 	
 	@Autowired
 	private RouteService routeService;
 	
 	@Autowired
 	private UserService userService;
 
 //	private static final Logger logger = LoggerFactory.getLogger(RouteController.class);
 	
 	@RequestMapping(value = "/{regionUrl}/{routeUrl}", method = RequestMethod.GET)
 	public ModelAndView getRoute(@PathVariable(value = "regionUrl") String regionUrl,@PathVariable(value = "routeUrl") String routeUrl) {
 		Map<String, Object> model = new HashMap<String, Object>();
 		
 		Region region = regionService.getRegionByUrlFriendlyName(regionUrl);
 		if (region == null) {
 			return RegionController.invalidRegion(regionUrl);
 		}
 		Route route = routeService.getRouteByUrlFriendlyName(routeUrl);
 		if (route == null) {
 			return invalidRoute(region, regionUrl);
 		}
 		
 		System.out.println("route ratings: " + route.getRatings().size());
 		for (Rating rating : route.getRatings()) {
 			System.out.println(rating.getId() + " | " + rating.getValue() + " | " + rating.getCreationDate());
 		}
 
 		model.put("region", region);
 		model.put("route", route);
 		
 		return new ModelAndView("route", model);
 	}
 
 	@RequestMapping(value = "/{regionUrl}/{routeUrl}/addcomment", method = RequestMethod.POST)
 	public ModelAndView addComment(@PathVariable(value = "regionUrl") String regionUrl,@PathVariable(value = "routeUrl") String routeUrl, @RequestParam("value") String value) {
 
 		Region region = regionService.getRegionByUrlFriendlyName(regionUrl);
 		if (region == null) {
 			return RegionController.invalidRegion(regionUrl);
 		}
 		Route route = routeService.getRouteByUrlFriendlyName(routeUrl);
 		if (route == null) {
 			return invalidRoute(region, regionUrl);
 		}
 
 		User user = null;
 		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
 			UserDetails principal = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 			user = userService.getUserByEmailAddress(principal.getUsername()); 
 		}
 		
 		Comment comment = new Comment();
 		comment.setValue(value);
		comment.setRoute(route);
 		comment.setUser(user);
		route.getComments().add(comment);
 		
 		routeService.saveRoute(route);
 
 		return new ModelAndView(new RedirectView(""));
 	}
 
 	@RequestMapping(value = "/{regionUrl}/addroute", method = RequestMethod.GET)
 	public ModelAndView addRoute(@PathVariable(value = "regionUrl") String regionUrl) {
 		Map<String, Object> model = new HashMap<String, Object>();
 		
 		Region region = regionService.getRegionByUrlFriendlyName(regionUrl);
 		if (region == null) {
 			return RegionController.invalidRegion(regionUrl);
 		}
 		Route route = new Route();
 
 		model.put("region", region);
 		model.put("route", route);
 		
 		return new ModelAndView("routeAdd", model);
 	}
 
 	@RequestMapping(value = "/{regionUrl}/addroute", method = RequestMethod.POST)
 	public ModelAndView postRoute(@PathVariable(value = "regionUrl") String regionUrl, @ModelAttribute("route") Route route) {
		System.out.println("number of directions: " + route.getDirectionList().size());
 		Region region = regionService.getRegionByUrlFriendlyName(regionUrl);
 		if (region == null) {
 			return RegionController.invalidRegion(regionUrl);
 		}
 
 		for (int pos = 0; pos < route.getDirectionList().size(); pos++) {
 			Direction direction = route.getDirectionList().get(pos);
			System.out.println(pos + " | " + direction.getStepNumber() + " | " + direction.getInstruction());
 			if (!direction.getInstruction().equals("")) {
 				route.addDirection(direction);
 			}
 		}
 
 		route.setRegion(region);
 		routeService.saveRoute(route);
 
 		return new ModelAndView(new RedirectView(route.getUrlFriendlyName()));
 	}
 
 	@RequestMapping(value = "/{regionUrl}/{routeUrl}/delete", method = RequestMethod.GET)
 	public ModelAndView deleteRoute(@PathVariable(value = "regionUrl") String regionUrl,@PathVariable(value = "routeUrl") String routeUrl) {
 		Region region = regionService.getRegionByUrlFriendlyName(regionUrl);
 		if (region == null) {
 			return RegionController.invalidRegion(regionUrl);
 		}
 		Route route = routeService.getRouteByUrlFriendlyName(routeUrl);
 		if (route == null) {
 			return invalidRoute(region, regionUrl);
 		}
 		
 		routeService.deleteRoute(route);
 		
 		return new ModelAndView(new RedirectView("../"));
 	}
 
 	@RequestMapping(value = "/{regionUrl}/{routeUrl}/rate", method = RequestMethod.GET)
 	public ModelAndView addRating(@PathVariable(value = "regionUrl") String regionUrl,@PathVariable(value = "routeUrl") String routeUrl, @RequestParam("rating") Integer value) {
 		Region region = regionService.getRegionByUrlFriendlyName(regionUrl);
 		if (region == null) {
 			return RegionController.invalidRegion(regionUrl);
 		}
 		Route route = routeService.getRouteByUrlFriendlyName(routeUrl);
 		if (route == null) {
 			return invalidRoute(region, regionUrl);
 		}
 		
 		User user = null;
 		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
 			UserDetails principal = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 			user = userService.getUserByEmailAddress(principal.getUsername()); 
 		}
 		
 		Rating rating = new Rating();
 		rating.setValue(value);
 		rating.setRoute(route);
 		rating.setUser(user);
 		route.getRatings().add(rating);
 		
 		routeService.saveRoute(route);
 		
 		return new ModelAndView(new RedirectView(""));
 	}
 	
 	static public ModelAndView invalidRoute(Region region, String routeUrl) {
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("message", "Route " + routeUrl + " not found in region " + region.getName());
 		return new ModelAndView(new RedirectView("/"+region.getUrlFriendlyName(), true), model);
 	}
 
 }
