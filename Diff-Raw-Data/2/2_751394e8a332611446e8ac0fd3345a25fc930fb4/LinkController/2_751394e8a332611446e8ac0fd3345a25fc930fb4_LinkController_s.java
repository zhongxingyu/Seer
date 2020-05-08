 package org.linkstorage.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import org.linkstorage.model.Link;
 import org.linkstorage.repository.LinkBase;
 
 @Controller
 @RequestMapping("/integration/*")
 public class LinkController {
 	private LinkBase linksRepository;
 
 	private static final String XML_VIEW_NAME = "links";
 
 	@Autowired
 	public LinkController(LinkBase linksRepository) {
 		this.linksRepository = linksRepository;
 	}
 
 	@RequestMapping(method=RequestMethod.GET, value="/link/{id}")
 	public ModelAndView getLink(@PathVariable String id) {
 		Link link = linksRepository.getLink(Long.parseLong(id));
	    ModelAndView modelAndView = new ModelAndView("links");
 	    modelAndView.addObject("link", link);
 	    return modelAndView;
 	}
 }
