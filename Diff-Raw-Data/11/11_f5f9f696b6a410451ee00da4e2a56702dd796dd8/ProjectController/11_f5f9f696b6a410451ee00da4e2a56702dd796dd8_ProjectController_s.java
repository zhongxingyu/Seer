 package cz.cvut.fel.bupro.controller;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
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
 import cz.cvut.fel.bupro.model.Email;
 import cz.cvut.fel.bupro.model.Membership;
 import cz.cvut.fel.bupro.model.MembershipState;
 import cz.cvut.fel.bupro.model.Project;
 import cz.cvut.fel.bupro.model.ProjectCourse;
 import cz.cvut.fel.bupro.model.Tag;
 import cz.cvut.fel.bupro.model.TagGroup;
 import cz.cvut.fel.bupro.model.User;
 import cz.cvut.fel.bupro.security.SecurityService;
 import cz.cvut.fel.bupro.service.CodeRepositoryService;
 import cz.cvut.fel.bupro.service.CourseService;
 import cz.cvut.fel.bupro.service.EmailService;
 import cz.cvut.fel.bupro.service.MembershipService;
 import cz.cvut.fel.bupro.service.ProjectService;
 import cz.cvut.fel.bupro.service.SemesterService;
 import cz.cvut.fel.bupro.service.TagService;
 import cz.cvut.fel.bupro.service.UserService;
 import cz.cvut.fel.kos.Translator;
 import cz.cvut.fel.reposapi.Issue;
 import cz.cvut.fel.reposapi.IssueState;
 import cz.cvut.fel.reposapi.ServiceProvider;
 
 @Controller
 public class ProjectController {
 	private static final int LOG_LIMIT = 5;
 
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
 	@Autowired
 	private CodeRepositoryService codeRepositoryService;
 
 	private static String getContextRoot(HttpServletRequest request) {
 		try {
 			URL url = new URL(request.getScheme(), request.getLocalName(), request.getLocalPort(), request.getContextPath());
 			return url.toExternalForm();
 		} catch (MalformedURLException e) {
 			return "#";
 		}
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
 		if (project.getTagGroup() == null) {
 			Set<Tag> set = project.getTags();
 			if (!set.isEmpty()) {
 				project.setTagGroup(set.iterator().next().getGroup());
 			}
 		}
 		model.addAttribute("courseList", courses);
 		model.addAttribute("tags", tagService.getTagNameMap());
 		model.addAttribute("semesterList", semesterService.getAllSemesters());
 		model.addAttribute("translator", new Translator(locale));
 		model.addAttribute("providerList", ServiceProvider.values());
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
 		model.addAttribute("project", project);
 		return editPage(model, locale, project, courses);
 	}
 
 	@RequestMapping({ "/project/create" })
 	@Transactional
 	public String createProject(Model model, Locale locale) {
 		log.trace("ProjectManagementController.createProject()");
 		// User user = securityService.getCurrentUser();
 		Project project = new Project();
 		Collection<ProjectCourse> courses = courseService.getProjectCourses(locale);
 		model.addAttribute("project", project);
 		return editPage(model, locale, project, courses);
 	}
 
 	@RequestMapping({ "/project/save" })
 	@Transactional
 	public String saveProject(@Valid Project project, BindingResult bindingResult, Model model, Locale locale) {
 		User user = securityService.getCurrentUser();
 		if (bindingResult.hasErrors()) {
 			log(bindingResult.getAllErrors());
 			Collection<ProjectCourse> courses = courseService.getProjectCourses(locale);
 			return editPage(model, locale, project, courses);
 		}
 		if (project.getOwner() == null) {
 			project.setOwner(user);
 		}
 		if (project.getOwner().equals(user)) {
 			TagGroup tagGroup = project.getTagGroup();
 			if (tagGroup != null && tagGroup.getId() == null) {
 				tagGroup = tagService.createNewTagGroup(tagGroup);
 			} else {
 				tagGroup = tagService.refresh(tagGroup);
 			}
			log.info(tagGroup);
			project.setTags(tagService.refresh(project.getTags()));
			for (Tag tag : project.getTags()) {
				tagGroup.add(tag);
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
 	public String joinProject(Model model, Locale locale, @PathVariable Long id, HttpServletRequest request) {
 		User user = securityService.getCurrentUser();
 		Project project = projectService.getProject(id);
 		if (project.getOwner().equals(user)) {
 			log.warn("Owner can't join project " + project);
 			return showProjectDetail(model, locale, id);
 		}
 		List<Membership> duplicate = membershipService.getMemberships(user, project.getCourse());
 		for (Membership membership : duplicate) {
 			log.info("Membership already found for course " + project.getCourse() + " in project " + membership.getProject().getName());
 		}
 		membershipService.deleteMembership(duplicate);
 		Membership membership = new Membership();
 		membership.setUser(user);
 		membership.setProject(project);
 		project.getMemberships().add(membership);
 		user.getMemberships().add(membership);
 		String baseLinkUrl = getContextRoot(request);
 		if (project.isAutoApprove()) {
 			membership.setMembershipState(MembershipState.APPROVED);
 			emailService.sendMembershipAutoapproved(baseLinkUrl, project, user);
 		} else {
 			emailService.sendMembershipRequest(baseLinkUrl, project, user);
 		}
 		return viewPage(model, project);
 	}
 
 	@RequestMapping({ "/project/membership/approve" })
 	@Transactional
 	public String approveMember(Model model, Locale locale, @RequestParam(value = "projectId", required = true) Long projectId,
 			@RequestParam(value = "userId", required = true) Long userId, HttpServletRequest request) {
 		Project project = projectService.getProject(projectId);
 		if (project.isCapacityFull()) {
 			log.error("Project " + project.getId() + " is at full capacity can not approve more members");
 			return viewPage(model, project);
 		}
 		boolean full = project.getCapacity() <= (project.getApprovedCount() + 1);
 		String baseLinkUrl = getContextRoot(request);
 		for (Membership membership : project.getMemberships()) {
 			if (membership.getUser().getId().equals(userId)) {
 				MembershipState state = MembershipState.APPROVED;
 				membership.setMembershipState(state);
 				emailService.sendMembershipState(baseLinkUrl, membership.getProject(), membership.getUser(), state);
 			} else if (full && membership.getMembershipState() == MembershipState.WAITING_APPROVAL) {
 				MembershipState state = MembershipState.DECLINED;
 				membership.setMembershipState(state);
 				emailService.sendMembershipState(baseLinkUrl, membership.getProject(), membership.getUser(), state);
 			}
 		}
 		return viewPage(model, project);
 	}
 
 	@RequestMapping({ "/project/membership/decline" })
 	@Transactional
 	public String declineMember(Model model, Locale locale, @RequestParam(value = "projectId", required = true) Long projectId,
 			@RequestParam(value = "userId", required = true) Long userId, HttpServletRequest request) {
 		MembershipState membershipState = MembershipState.DECLINED;
 		Membership membership = membershipService.getMembership(projectId, userId);
 		membership.setMembershipState(membershipState);
 		String baseLinkUrl = getContextRoot(request);
 		emailService.sendMembershipState(baseLinkUrl, membership.getProject(), membership.getUser(), membershipState);
 		return viewPage(model, membership.getProject());
 	}
 
 	@RequestMapping("/project/log/{id}")
 	@Transactional
 	public String projectLog(Model model, Locale locale, @PathVariable Long id) {
 		Project project = projectService.getProject(id, locale);
 		Collection<Issue> openedIssues = codeRepositoryService.getIssues(project, IssueState.OPEN);
 		Collection<Issue> closedIssues = codeRepositoryService.getIssues(project, IssueState.CLOSED);
 		String open = (openedIssues == null) ? "?" : String.valueOf(openedIssues.size());
 		String closed = (closedIssues == null) ? "?" : String.valueOf(closedIssues.size());
 		String total = (openedIssues == null || closedIssues == null) ? "?" : String.valueOf(openedIssues.size() + closedIssues.size());
 		model.addAttribute("project", project);
 		model.addAttribute("repository", codeRepositoryService.getRepository(project));
 		model.addAttribute("commits", codeRepositoryService.getCommits(project, LOG_LIMIT));
 		model.addAttribute("open", open);
 		model.addAttribute("closed", closed);
 		model.addAttribute("total", total);
 		model.addAttribute("issues", codeRepositoryService.getUpdatedIssues(project, LOG_LIMIT));
 		return "project-log";
 	}
 
 	@RequestMapping("/project/email/{id}")
 	@Transactional
 	public String composeEmail(Model model, Locale locale, @PathVariable Long id) {
 		User user = securityService.getCurrentUser();
 		Project project = projectService.getProject(id, locale);
 		if (!project.getOwner().equals(user)) {
 			log.warn("Only owner can send emails to all members" + project);
 			return showProjectDetail(model, locale, id);
 		}
 		Email email = new Email();
 		email.setId(project.getId());
 		email.addAllTo(project.getMembers());
 		email.setTitle(project.getName());
 		model.addAttribute("email", email);
 		return "project-email";
 	}
 
 	@RequestMapping("/project/email/send")
 	@Transactional
 	public String sendEmail(@Valid Email email, BindingResult bindingResult, Model model, Locale locale) {
 		if (bindingResult.hasErrors()) {
 			return "project-email";
 		}
 		emailService.sendEmail(email);
 		return showProjectDetail(model, locale, email.getId());
 	}
 
 }
