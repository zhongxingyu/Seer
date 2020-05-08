 package cz.cvut.fel.bupro.controller;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Locale;
 
 import javax.validation.Valid;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.web.PageableDefaults;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import cz.cvut.fel.bupro.filter.Filterable;
 import cz.cvut.fel.bupro.model.Membership;
 import cz.cvut.fel.bupro.model.MembershipState;
 import cz.cvut.fel.bupro.model.Project;
 import cz.cvut.fel.bupro.model.Subject;
 import cz.cvut.fel.bupro.model.Tag;
 import cz.cvut.fel.bupro.model.User;
 import cz.cvut.fel.bupro.security.SecurityService;
 import cz.cvut.fel.bupro.service.EmailService;
 import cz.cvut.fel.bupro.service.MembershipService;
 import cz.cvut.fel.bupro.service.ProjectService;
 import cz.cvut.fel.bupro.service.SemesterService;
 import cz.cvut.fel.bupro.service.SubjectService;
 import cz.cvut.fel.bupro.service.TagService;
 import cz.cvut.fel.bupro.service.UserService;
 
 @Controller
 public class ProjectController {
 	private final Log log = LogFactory.getLog(getClass());
 
 	@Autowired
 	private ProjectService projectService;
 	@Autowired
 	private SemesterService semesterService;
 	@Autowired
 	private SubjectService subjectService;
 	@Autowired
 	private TagService tagService;
 	@Autowired
 	private MembershipService membershipService;
 	@Autowired
 	private SecurityService securityService;
 	@Autowired
 	private EmailService emailService;
 	@Autowired
 	private UserService userService;
 
 	private void log(List<ObjectError> errors) {
 		log.info("Validation errors");
 		for (ObjectError error : errors) {
 			log.info(error.toString());
 		}
 	}
 
 	private String viewPage(Model model, Project project) {
 		project.getMemberships().size(); // force fetch
 		project.getComments().size(); // force fetch
 		project.getTags().size(); // force fetch
 		model.addAttribute("project", project);
 		return "project-view";
 	}
 
 	private String editPage(Model model, Project project, Collection<Subject> subjects) {
 		model.addAttribute("project", project);
 		model.addAttribute("subjectList", subjects);
 		model.addAttribute("tags", tagService.getAllTags());
 		model.addAttribute("semesterList", semesterService.getAllSemesters());
 		return "project-edit";
 	}
 
 	@ModelAttribute("user")
 	@Transactional
 	public User getLoggedInUser() {
 		User user = securityService.getCurrentUser();
 		user.getEnrolments().size(); // force fetch
 		return user;
 	}
 
	@RequestMapping({ "/project/list" })
 	public String showProjectList(Model model, Locale locale, @PageableDefaults Pageable pageable, Filterable filterable) {
 		model.addAttribute("projects", projectService.getProjects(pageable, filterable));
 		model.addAttribute("filter", filterable);
 		model.addAttribute("tags", tagService.getAllTags());
 		model.addAttribute("subjects", subjectService.getAllSubjects());
 		return "project-list";
 	}
 
 	@RequestMapping({ "/project/view/{id}" })
 	@Transactional
 	public String showProjectDetail(Model model, Locale locale, @PathVariable Long id) {
 		Project project = projectService.getProject(id);
 		return viewPage(model, project);
 	}
 
 	@RequestMapping({ "/project/edit/{id}" })
 	@Transactional
 	public String editProjectDetail(Model model, Locale locale, @PathVariable Long id) {
 		log.trace("ProjectManagementController.editProjectDetail()");
 		Project project = projectService.getProject(id);
 		Collection<Subject> subjects = project.getOwner().getTeachedSubjects();
 		return editPage(model, project, subjects);
 	}
 
 	@RequestMapping({ "/project/create" })
 	@Transactional
 	public String createProject(Model model, Locale locale) {
 		log.trace("ProjectManagementController.createProject()");
 		User user = securityService.getCurrentUser();
 		Project project = new Project();
 		Collection<Subject> subjects = user.getTeachedSubjects();
 		return editPage(model, project, subjects);
 	}
 
 	@RequestMapping({ "/project/save" })
 	@Transactional
 	public String saveProject(@Valid Project project, BindingResult bindingResult, Model model) {
 		User user = securityService.getCurrentUser();
 		if (bindingResult.hasErrors()) {
 			log(bindingResult.getAllErrors());
 			model.addAttribute("subjectList", user.getTeachedSubjects());
 			return "project-edit";
 		}
 		if (project.getOwner() == null) {
 			project.setOwner(user);
 		}
 		if (project.getOwner().equals(user)) {
 			project.setTags(tagService.refresh(project.getTags()));
 			for (Tag tag : project.getTags()) {
 				tag.getProjects().add(project);
 			}
 			project = projectService.save(project);
 			tagService.removeUnusedTags();
 			log.info("Project saved " + project);
 		} else {
 			log.error("Can't save " + project + " user " + user + " is not owner");
 		}
 		return "redirect:/project/view/" + project.getId();
 	}
 
 	@RequestMapping({ "/project/join/{id}" })
 	@Transactional
 	public String joinProject(Model model, Locale locale, @PathVariable Long id) {
 		User user = securityService.getCurrentUser();
 		Project project = projectService.getProject(id);
 		if (project.getOwner().equals(user)) {
 			log.warn("Owner can't join project " + project);
 			return showProjectDetail(model, locale, id);
 		}
 		Membership membership = new Membership();
 		membership.setUser(user);
 		membership.setProject(project);
 		project.getMemberships().add(membership);
 		user.getMemberships().add(membership);
 		emailService.sendMembershipRequest(locale, project, user);
 		return viewPage(model, project);
 	}
 
 	private String updateMembership(Model model, Locale locale, Long projectId, Long userId, MembershipState membershipState) {
 		Membership membership = membershipService.getMembership(projectId, userId);
 		membership.setMembershipState(membershipState);
 		emailService.sendMembershipState(locale, membership.getProject(), membership.getUser(), membershipState);
 		return viewPage(model, membership.getProject());
 	}
 
 	@RequestMapping({ "/project/membership/approve" })
 	@Transactional
 	public String approveMember(Model model, Locale locale, @RequestParam(value = "projectId", required = true) Long projectId,
 			@RequestParam(value = "userId", required = true) Long userId) {
 		return updateMembership(model, locale, projectId, userId, MembershipState.APPROVED);
 	}
 
 	@RequestMapping({ "/project/membership/decline" })
 	@Transactional
 	public String declineMember(Model model, Locale locale, @RequestParam(value = "projectId", required = true) Long projectId,
 			@RequestParam(value = "userId", required = true) Long userId) {
 		return updateMembership(model, locale, projectId, userId, MembershipState.DECLINED);
 	}
 
 }
