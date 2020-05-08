 package cz.cvut.fel.bupro.controller;
 
 import java.text.SimpleDateFormat;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import cz.cvut.fel.bupro.model.Project;
 import cz.cvut.fel.bupro.service.ProjectService;
 
 @Controller
 public class ProjectManagementController {
	private final Log log = LogFactory.getLog(getClass());
 
 	@Autowired
 	private ProjectService projectService;
 
 	@ModelAttribute("projectList")
 	public List<Project> getProjectList() {
 		return projectService.getAllProjects();
 	}
 
 	@RequestMapping({ "*", "/project/list" })
 	public String showProjectList() {
 		return "project-list";
 	}
 
 	@RequestMapping({ "/project/view/{id}" })
 	public String showProjectDetail(Model model, Locale locale, @PathVariable Long id) {
 		Project project = projectService.getProject(id);
 		model.addAttribute("project", project);
 		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", locale);
 		model.addAttribute("creationTime", format.format(project.getAuthorship().getCreationTime()));
 		return "project-view";
 	}
 
 	@RequestMapping({ "/project/edit/{id}" })
 	public String editProjectDetail(Model model, Locale locale, @PathVariable Long id) {
 		Project project = projectService.getProject(id);
 		model.addAttribute("project", project);
 		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", locale);
 		model.addAttribute("creationTime", format.format(project.getAuthorship().getCreationTime()));
 		return "project-edit";
 	}
 
 	@RequestMapping({ "/project/create" })
 	public String createProject(Model model, Locale locale) {
		log.trace("ProjectManagementController.createProject()");
 		Project project = new Project();
 		model.addAttribute("project", project);
 		return "project-edit";
 	}
 
 	@RequestMapping({ "/project/save" })
 	public String saveProject(@Validated Project project, BindingResult errors, Map<String, Object> model) {
 		project = projectService.save(project);
 		// User user = getCurrentUser();
 		// project.setAuthorship(new Authorship());
		log.info("Project saved " + project);
 		return "redirect:/project/view/" + project.getId();
 	}
 }
