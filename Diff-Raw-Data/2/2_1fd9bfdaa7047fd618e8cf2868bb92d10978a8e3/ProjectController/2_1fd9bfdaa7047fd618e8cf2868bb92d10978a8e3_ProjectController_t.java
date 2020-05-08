 package controller;
 
 import models.Donation;
 import models.Project;
 import models.ProjectDAO;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.util.Calendar;
 
 @Controller
 public class ProjectController {
     @Autowired
     @Qualifier("projectDAO")
     private ProjectDAO dao;
 
     @RequestMapping(value="/project_detail.ftl", method = RequestMethod.GET)
     public String showProjectDetail(@ModelAttribute("model") ModelMap modelMap, @RequestParam int project_id){
         Project project = dao.fetch(project_id);
 
         modelMap.addAttribute("project", project);
         modelMap.addAttribute("anotherVar", project_id);
//        modelMap.addAttribute("donationPercentage", project.donationPercentage());
 
         return "project_detail";
     }
 
     public void setDao(ProjectDAO dao) {
         this.dao = dao;
     }
 }
