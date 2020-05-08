 package cz.cvut.fel.bupro.controller;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import javax.validation.Valid;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
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
 import cz.cvut.fel.bupro.model.SemesterCode;
 import cz.cvut.fel.bupro.model.ProjectCourse;
 import cz.cvut.fel.bupro.model.Tag;
 import cz.cvut.fel.bupro.model.User;
 import cz.cvut.fel.bupro.security.SecurityService;
 import cz.cvut.fel.bupro.service.EmailService;
 import cz.cvut.fel.bupro.service.MembershipService;
 import cz.cvut.fel.bupro.service.ProjectService;
 import cz.cvut.fel.bupro.service.SemesterService;
 import cz.cvut.fel.bupro.service.CourseService;
 import cz.cvut.fel.bupro.service.TagService;
 import cz.cvut.fel.bupro.service.UserService;
 import cz.cvut.fel.kos.Translator;
 
 @Controller
 public class ProjectController {
 	private final Log log = LogFactory.getLog(getClass());
 
 	@Autowired
 	private ProjectService projectService;
 	@Autowired
 	private SemesterService semesterService;
 	@Autowired
 	private CourseService courseService;
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
 
 	private static Set<SemesterCode> semesterCodeSet(Collection<Project> c) {
 		Set<SemesterCode> set = new HashSet<SemesterCode>();
 		for (Project project : c) {
 			set.add(project.getStartSemester());
 			set.add(project.getEndSemester());
 		}
 		return set;
 	}
 
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
 
 	private String editPage(Model model, Locale locale, Project project, Collection<ProjectCourse> courses) {
 		model.addAttribute("project", project);
 		model.addAttribute("courseList", courses);
 		model.addAttribute("tags", tagService.getAllTags());
 		model.addAttribute("semesterList", semesterService.getAllSemesters());
 		model.addAttribute("translator", new Translator(locale));
 		return "project-edit";
 	}
 
 	@ModelAttribute("user")
 	@Transactional
 	public User getLoggedInUser() {
 		User user = securityService.getCurrentUser();
 		return user;
 	}
 
 	@RequestMapping({ "/", "/project/list" })
 	public String showProjectList(Model model, Locale locale, @PageableDefaults Pageable pageable, Filterable filterable) {
 		Page<Project> projects = projectService.getProjects(locale, pageable, filterable);
 		model.addAttribute("projects", projects);
 		model.addAttribute("semester", semesterService.getSemestersNames(semesterCodeSet(projects.getContent()), locale));
 		model.addAttribute("filter", filterable);
 		model.addAttribute("tags", tagService.getAllTags());
 		model.addAttribute("courses", courseService.getProjectCourses(locale));
 		return "project-list";
 	}
 
 	@RequestMapping({ "/project/view/{id}" })
 	@Transactional
 	public String showProjectDetail(Model model, Locale locale, @PathVariable Long id) {
 		Project project = projectService.getProject(id, locale);
 		return viewPage(model, project);
 	}
 
 	@RequestMapping({ "/project/edit/{id}" })
 	@Transactional
 	public String editProjectDetail(Model model, Locale locale, @PathVariable Long id) {
 		log.trace("ProjectManagementController.editProjectDetail()");
 		Project project = projectService.getProject(id);
 		Collection<ProjectCourse> courses = courseService.getProjectCourses();
 		return editPage(model, locale, project, courses);
 	}
 
 	@RequestMapping({ "/project/create" })
 	@Transactional
 	public String createProject(Model model, Locale locale) {
 		log.trace("ProjectManagementController.createProject()");
 		User user = securityService.getCurrentUser();
 		Project project = new Project();
 		Collection<ProjectCourse> courses = courseService.getProjectCourses(locale);
 		return editPage(model, locale, project, courses);
 	}
 
 	@RequestMapping({ "/project/save" })
 	@Transactional
 	public String saveProject(@Valid Project project, BindingResult bindingResult, Model model, Locale locale) {
 		User user = securityService.getCurrentUser();
 		if (bindingResult.hasErrors()) {
 			log(bindingResult.getAllErrors());
 			model.addAttribute("courseList", courseService.getProjectCourses(locale));
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
