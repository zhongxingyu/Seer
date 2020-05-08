 package com.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
 /**
  * @module Movie
  * @author sbicer
  *
  */
 @Controller
 @RequestMapping(value={"/movie","/movie2"})
 public class MovieController {
 	/**
 	 * @name GetMovie
 	 * @param name
 	 *            Name of movie
 	 * @param query
 	 *            Query String
 	 * @param model
 	 * @return
 	 */
 	@RequestMapping(value = "/{name}", method = RequestMethod.GET)
 	public String getMovie(@PathVariable String name,
 			@PathVariable String testParam1,
 			@PathVariable("test2") Boolean testParam2,
 			@PathVariable(value="test3") Integer testParam3,
 			@RequestParam(required = true, value="test4") String testParam4,
 			@RequestParam(required = true, defaultValue="test") String query, ModelMap model) {
 
 		model.addAttribute("movie", name);
 		return "list";
 
 	}
 	/**
 	 * Lists movies
 	 * @return Test value
 	 * @requestExample http://turkcellmuzik.com/hafifmuzik/mobile/artist/info/id/1234
 	 * @responseExample 
 {
   result: {
     id: 1737
     imagePath: http://turkcellmuzik.com/hafifmuzik-static/artistimages/[size]/Rm/Vy/RmVyZGkgw5Z6YmXEn2Vu.jpg
     name: "Artist ismi"
   }
 }
 	 */
 	@RequestMapping(value={"/list1", "/list2"})
 	public @ResponseBody String listMovies(){
 		return null;
 	}
 	/**
 	 * @name get
 	 * @param id
 	 * @return
 	 */
 	@RequestMapping(value="get")
 	public ModelAndView getModelAndView(@RequestParam Long id){
 		return null;
 	}
 	
 	public void dummy(){
 		
 	}
 
 }
