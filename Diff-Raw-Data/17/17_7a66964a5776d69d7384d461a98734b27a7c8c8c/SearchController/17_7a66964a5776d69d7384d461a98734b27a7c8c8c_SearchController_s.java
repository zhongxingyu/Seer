 package org.netvogue.server.webmvc.controllers;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.netvogue.server.neo4japi.common.ProductLines;
 import org.netvogue.server.neo4japi.common.USER_TYPE;
 import org.netvogue.server.neo4japi.domain.User;
 import org.netvogue.server.neo4japi.service.UserService;
 import org.netvogue.server.webmvc.converters.ImageURLsConverter;
 import org.netvogue.server.webmvc.domain.ImageURLsResponse;
 import org.netvogue.server.webmvc.domain.SearchResponse;
 import org.netvogue.server.webmvc.domain.UsersResponse;
 import org.netvogue.server.webmvc.security.NetvogueUserDetailsService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.convert.ConversionService;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class SearchController {
 
 	@Autowired NetvogueUserDetailsService userDetailsService;
 	@Autowired UserService 			userService;
 	@Autowired ImageURLsConverter	imageURLsConverter;
 	@Autowired ConversionService	conversionService;
 
 	@RequestMapping(value="getallusers", method=RequestMethod.GET)
 	public @ResponseBody UsersResponse getAllUsers(@RequestParam("query") String query) {
 		System.out.println("Get all users: " + query);
 		
 		UsersResponse response = new UsersResponse();
 		User loggedinUser = userDetailsService.getUserFromSession();
 		response.setName(loggedinUser.getName());
 		response.setProfilepic(imageURLsConverter.convert(loggedinUser.getProfilePicLink(), loggedinUser.getUsername()));
 		
 		Set<SearchResponse> allusers = new LinkedHashSet<SearchResponse>();
 		
 			Iterable<User> users = userService.getAllUsers(query);
 			
 			if(null == users) {
 				System.out.println("No users found: ");
 				return response;
 			}
 			Iterator<User> first = users.iterator();
 			while ( first.hasNext() ){
 				User dbUser = first.next() ;
 				System.out.println("user name: " + dbUser.getUsername());
 				allusers.add(conversionService.convert(dbUser, SearchResponse.class));
 			}
 		response.setUsers(allusers);
 	
 		return response;
 	}
 	
 	@RequestMapping(value="basicsearch", method=RequestMethod.GET)
 	public @ResponseBody Set<SearchResponse> doBasicSearch(@RequestParam("query") String query) {
 		System.out.println("Get users: " + query);
 		Set<SearchResponse> response = new LinkedHashSet<SearchResponse>();
 		if(!query.isEmpty()) {
 			
 			Iterable<User> users = userService.doBasicSearch(query);
 			
 			if(null == users) {
 				System.out.println("No users found: " + query);
 				return response;
 			}
 			Iterator<User> first = users.iterator();
 			while ( first.hasNext() ){
 				User dbUser = first.next() ;
 				System.out.println("user name: " + dbUser.getUsername());
 				response.add(conversionService.convert(dbUser, SearchResponse.class));
 			}
 		}
 		return response;
 	}
 
 	@RequestMapping(value="advancedsearch", method=RequestMethod.GET)
 	public @ResponseBody Set<SearchResponse> doAdvancedSearch(
 							@RequestParam(value="pagenumber") int pagenumber,
 							@RequestParam(value="name", required=false) String name, 
 							@RequestParam(value="location", required=false) String location,
 							@RequestParam(value="categories", required=false) String categories, 
 							@RequestParam(value="usertype", required=false)	String searchtype,
 							@RequestParam(value="userscarried", required=false) String usersCarried,
 							@RequestParam(value="fromprice", required=false, defaultValue="0") long fromPrice,
 							@RequestParam(value="toprice", required=false, defaultValue="0") long toPrice){
 		
 		System.out.println("\nGet users: name:" + name + 
 							"\nlocation:" + location +
 							"\ncategories:" + categories.toString() +
 							"\nuser type:" + searchtype +
 							"\nusers carried:" + usersCarried.toString());
 		Set<SearchResponse> response = new LinkedHashSet<SearchResponse>();
 		
 		USER_TYPE user_type;
 		if(searchtype.equals("true")) {
 			user_type = USER_TYPE.BRAND;
 		} else {
 			user_type = USER_TYPE.BOUTIQUE;
 		}
 		
 		Set<String> productlines = new HashSet<String>();
 		List<String> categoriesafter =  Arrays.asList(categories.split(","));
 		for(String productline: categoriesafter) {
 			ProductLines productLine = ProductLines.getValueOf(productline);
 			if(null != productLine)
 				productlines.add(productLine.toString());
 			else
 				System.out.println("product line is null");
 		}
		
		Set<String> userscarried = new HashSet<String>(Arrays.asList(usersCarried.split(",")));
 		Iterable<User> users = userService.doAdvancedSearch(user_type, name, location, productlines, userscarried,
 				fromPrice, toPrice, pagenumber);
 		
 		if(null == users) {
 			System.out.println("No users found: ");
 			return response;
 		}
 		Iterator<User> first = users.iterator();
 		while ( first.hasNext() ){
 			User dbUser = first.next() ;
 			System.out.println("user name: " + dbUser.getUsername());
 			response.add(conversionService.convert(dbUser, SearchResponse.class));
 		}
 		
 		return response;
 	}
 }
