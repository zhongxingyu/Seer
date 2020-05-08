 package com.cyganov.simplecms.controllers;
 
 import com.cyganov.simplecms.domain.Section;
 import com.cyganov.simplecms.services.SectionService;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Tsyhanou Siarhei
  * Date: 24.07.13
  * Time: 11:06
  */
 @Controller
 public class SiteController {
 
 	@Autowired
 	private SectionService sectionService;
 
	@RequestMapping(value = "/login")
	public String login() {
		return "redirect:/site";
	}

 	@RequestMapping(value = "/site", method = RequestMethod.GET)
 	public ModelAndView loadSite(@RequestParam(value = "rootId", required = false) String rootId, @RequestParam(value = "sectionId", required = false) String sectionId) {
 		ModelAndView modelAndView = new ModelAndView("site");
 
 		List<Section> sectionList = sectionService.getSections();
 
 		Section section = null;
 		List<Section> sections = null;
 		if (StringUtils.isEmpty(rootId) && StringUtils.isEmpty(sectionId)){
 			//default values
 			section = sectionList.get(0);
 			sections = sectionService.getChildrenByParentId(section.getId());
 		} else {
 			section = sectionService.getSectionById(sectionId);
 			sections = sectionService.getChildrenByParentId(rootId);
 		}
 
 		modelAndView.addObject("sections", sections);
 		modelAndView.addObject("mainSection", section);
 		modelAndView.addObject("rootSections", sectionList);
 		return modelAndView;
 	}
 
 	@RequestMapping(value = "/mngt", method = RequestMethod.GET)
 	public ModelAndView loadSiteTree(@RequestParam(value = "rootId", required = false) String rootId, @RequestParam(value = "sectionId", required = false) String sectionId) {
 		ModelAndView modelAndView = new ModelAndView("mngt");
 		List<Section> sectionList = sectionService.getSections();
 
 		Section section = null;
 		if (StringUtils.isEmpty(sectionId)){
 			//default values
 			section = sectionList.get(0);
 		} else {
 			section = sectionService.getSectionById(sectionId);
 		}
 
 		modelAndView.addObject("section", section);
 		modelAndView.addObject("sections", sectionList);
 		return modelAndView;
 	}
 
 	@RequestMapping(value = "/mngt", method = RequestMethod.POST)
 	public String editSection(@ModelAttribute("section") Section section, @ModelAttribute("rootId") String rootId) {
 		sectionService.updateSection(section, rootId);
 		return "redirect:/mngt?sectionId="+section.getId();
 	}
 
 	@RequestMapping(value = "/mngt/add", method = RequestMethod.GET)
 	public ModelAndView addSection(@RequestParam(value = "parentId", required = false) String parentId) {
 
 		ModelAndView modelAndView = new ModelAndView("addSection");
 
 		modelAndView.addObject("parentId", parentId);
 		modelAndView.addObject("section", new Section());
 		return modelAndView;
 	}
 
 	@RequestMapping(value = "/mngt/add", method = RequestMethod.POST)
 	public String addSection(@ModelAttribute("section") Section section, @ModelAttribute("parentId") String parentId) {
 		sectionService.updateSection(section, parentId);
 		return "redirect:/mngt";
 	}
 
 	@RequestMapping(value = "/mngt/delete", method = RequestMethod.GET)
 	public String deleteSection(@RequestParam("id") String id) {
 		sectionService.deleteSectionById(id);
 		return "redirect:/mngt";
 	}
 
 }
