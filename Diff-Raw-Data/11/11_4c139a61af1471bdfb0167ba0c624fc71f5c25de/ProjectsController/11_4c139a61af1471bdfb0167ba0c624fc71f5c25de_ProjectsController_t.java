 package com.github.elvan.ticketline.controller;
 
 import com.github.elvan.ticketline.domain.Project;
 import com.github.elvan.ticketline.repository.ProjectRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Pageable;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import javax.validation.Valid;
 
 @Controller
 @RequestMapping("/projects")
 public class ProjectsController {
 
   @Autowired
   private ProjectRepository projectRepository;
 
   @RequestMapping(method = RequestMethod.GET)
   public String list(
       final Model model,
       final Pageable pageable) {
 
     model.addAttribute("title", "List of Projects");
     model.addAttribute("projects", projectRepository.findAll(pageable));
     return "projects/list";
   }
 
   @RequestMapping(value = "/add", method = RequestMethod.GET)
   public String add(final Model model) {
     model.addAttribute("title", "Add New Project");
     model.addAttribute("project", new Project());
     return "projects/add";
   }
 
   @RequestMapping(value = "/add", method = RequestMethod.POST)
   public String add(
       @Valid final Project project,
       final BindingResult result,
       final Model model,
       final RedirectAttributes redirectAttrs) {
 
     model.addAttribute("title", "Add New Project");
 
     if (result.hasErrors()) {
       model.addAttribute("message", "You must fill in all of the fields.");
       return "projects/add";
     }
     else {
       projectRepository.save(project);
       redirectAttrs.addFlashAttribute("message", "Project has been created.");
       return "redirect:/projects/show/" + project.getId();
     }
   }
 
   @RequestMapping(value = "/show/{id}", method = RequestMethod.GET)
   public String show(
       @PathVariable("id") final Project project,
       final Model model) {
 
     model.addAttribute("title", project.getName() + " - Projects");
     model.addAttribute("project", project);
     return "projects/show";
   }
 
   @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
   public String edit(
       @PathVariable("id") final Project project,
       final Model model) {
 
     model.addAttribute("title", project.getName() + " - Edit Project");
     model.addAttribute("project", project);
    model.addAttribute("project_id", project.getId());
     return "projects/edit";
   }
 
   @RequestMapping(value = "/edit/{id}", method = RequestMethod.POST)
   public String edit(
       @PathVariable("id") final Project existingProject,
       @Valid final Project submittedProject,
       final BindingResult result,
       final Model model,
       final RedirectAttributes redirectAttrs) {
 
    model.addAttribute("title", existingProject.getName() + " - Edit Project");
    model.addAttribute("project", submittedProject);
    model.addAttribute("project_id", existingProject.getId());
 
     existingProject.setName(submittedProject.getName());
     existingProject.setDescription(submittedProject.getDescription());
 
     if (result.hasErrors()) {
      model.addAttribute("message", "You must fill in all of the fields.");
      return "projects/edit";
     }
     else {
       projectRepository.save(existingProject);
       redirectAttrs.addFlashAttribute("message", "Project has been updated.");
       return "redirect:/projects/show/" + existingProject.getId();
     }
   }
 
   @RequestMapping(value = "/remove/{id}", method = RequestMethod.GET)
   public String remove(
       @PathVariable("id") final Project project,
       final Model model) {
 
     model.addAttribute("title", project.getName() + " - Remove Project");
     model.addAttribute("message", "Are you sure you want to destroy this project?");
     model.addAttribute("project", project);
     return "projects/remove";
   }
 
   @RequestMapping(value = "/remove/{id}", method = RequestMethod.POST)
   public String remove(
       @PathVariable("id") final Project existingProject,
       final RedirectAttributes redirectAttrs) {
 
     projectRepository.delete(existingProject.getId());
     redirectAttrs.addFlashAttribute("message", "Project has been deleted.");
     return "redirect:/projects";
   }
 
 }
