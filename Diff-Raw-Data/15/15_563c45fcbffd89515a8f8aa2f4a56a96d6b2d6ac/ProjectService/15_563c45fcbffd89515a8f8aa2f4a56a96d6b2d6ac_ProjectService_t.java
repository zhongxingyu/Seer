 package cz.cvut.fel.bupro.service;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Join;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.jpa.domain.Specification;
 import org.springframework.data.jpa.domain.Specifications;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import cz.cvut.fel.bupro.TimeUtils;
 import cz.cvut.fel.bupro.dao.ProjectRepository;
 import cz.cvut.fel.bupro.filter.FilterSpecificationFactory;
 import cz.cvut.fel.bupro.filter.Filterable;
 import cz.cvut.fel.bupro.model.Membership;
 import cz.cvut.fel.bupro.model.MembershipState;
 import cz.cvut.fel.bupro.model.Project;
 import cz.cvut.fel.bupro.model.ProjectCourse;
 import cz.cvut.fel.bupro.model.SemesterCode;
 import cz.cvut.fel.bupro.model.Tag;
 import cz.cvut.fel.bupro.model.User;
 import cz.cvut.fel.kos.KosClient;
 import cz.cvut.fel.kos.Translator;
 import cz.cvut.fel.kos.jaxb.Course;
 import cz.cvut.fel.kos.jaxb.Semester;
 
 @Service
 public class ProjectService {
 
 	private final Log log = LogFactory.getLog(getClass());
 
 	@Autowired
 	private ProjectRepository projectRepository;
 	@Autowired
 	private KosClient kos;
 
 	private void localize(Project project, Translator translator) {
 		ProjectCourse projectCourse = project.getCourse();
 		Course course = kos.getCourse(projectCourse.getCode());
 		projectCourse.setName(translator.localizedString(course.getName()));
 		SemesterCode semesterCode = project.getStartSemester();
 		Semester semester = kos.getSemester(semesterCode.getCode());
 		semesterCode.setName(translator.localizedString(semester.getName()));
 		semesterCode = project.getEndSemester();
 		semester = kos.getSemester(semesterCode.getCode());
 		semesterCode.setName(translator.localizedString(semester.getName()));
 	}
 
	public Project localize(Project project, Locale locale) {
		localize(project, new Translator(locale));
		return project;
	}

 	@Transactional
 	public List<Project> getAllProjects() {
 		log.info("get all projects");
 		return projectRepository.findAll();
 	}
 
 	@Transactional
 	public Page<Project> getProjects(Pageable pageable) {
 		log.info("get project page " + pageable.getPageNumber() + " size " + pageable.getPageSize());
 		return projectRepository.findAll(pageable);
 	}
 
 	@Transactional
 	public Page<Project> getProjects(Pageable pageable, Filterable filterable) {
 		Specification<Project> spec = createSpecification(filterable);
 		if (spec == null) {
 			return projectRepository.findAll(pageable);
 		}
 		return projectRepository.findAll(spec, pageable);
 	}
 
 	@Transactional
 	public Page<Project> getProjects(Locale locale, Pageable pageable, Filterable filterable) {
 		Translator translator = new Translator(locale);
 		Page<Project> page = getProjects(pageable, filterable);
 		for (Project project : page) {
 			localize(project, translator);
 		}
 		return page;
 	}
 
 	@Transactional
 	public List<Project> getOwnedProjects(User user, Locale locale) {
 		List<Project> projects = projectRepository.findByOwner(user);
 		Translator translator = new Translator(locale);
 		for (Project project : projects) {
 			localize(project, translator);
 		}
 		return projects;
 	}
 
 	@Transactional
 	public List<Project> getMemberProjects(User user, Locale locale) {
 		List<Project> projects = new LinkedList<Project>();
 		for (Membership membership : user.getMemberships()) {
 			if (membership.getMembershipState() == MembershipState.APPROVED) {
 				projects.add(membership.getProject());
 			}
 		}
 		Translator translator = new Translator(locale);
 		for (Project project : projects) {
 			localize(project, translator);
 		}
 		return projects;
 	}
 
 	@Transactional
 	public Project getProject(Long id) {
 		log.info("get project " + id);
 		Project project = projectRepository.findOne(id);
 		project.getMemberships().size(); // force fetch
 		project.getComments().size(); // force fetch
 		project.getTags().size(); // force fetch
 		return project;
 	}
 
 	@Transactional
 	public Project getProject(Long id, Locale locale) {
 		Project project = getProject(id);
 		localize(project, new Translator(locale));
 		return project;
 	}
 
 	@Transactional
 	public Project save(Project project) {
 		log.info("save project " + ((project.getId() == null) ? "new" : project.getId()));
 		if (project.getCreationTime() == null) {
 			project.setCreationTime(TimeUtils.createCurrentTimestamp());
 		}
 		return projectRepository.save(project);
 	}
 
 	private static Specifications<Project> createSingleSpec(Specifications<Project> spec, Map<String, String> map, FilterKey key) {
 		Specification<Project> newSpec = createSingleSpec(map, key);
 		if (newSpec == null) {
 			return spec;
 		}
 		if (spec == null) {
 			return Specifications.where(newSpec);
 		}
 		return spec.and(newSpec);
 	}
 
 	private static Specification<Project> createSingleSpec(Map<String, String> map, FilterKey key) {
 		final String value = map.get(key.toString());
 		if (value == null || value.trim().isEmpty()) {
 			return null;
 		}
 		return key.create(value);
 	}
 
 	private static Specification<Project> createSpecification(Filterable filterable) {
 		return createSpecification(filterable.getFilter());
 	}
 
 	private static Specification<Project> createSpecification(Map<String, String> map) {
 		Specifications<Project> specifications = null;
 		for (FilterKey key : FilterKey.values()) {
 			specifications = createSingleSpec(specifications, map, key);
 		}
 		return specifications;
 	}
 
 	private static enum FilterKey implements FilterSpecificationFactory<Project> {
 		NAME {
 			public Specification<Project> create(final String value) {
 				return new Specification<Project>() {
 					public Predicate toPredicate(Root<Project> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
 						return cb.like(root.<String> get("name"), "%" + value + "%");
 					}
 				};
 			}
 		},
 		USER {
 			public Specification<Project> create(final String value) {
 				return new Specification<Project>() {
 					public Predicate toPredicate(Root<Project> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
 						Join<Project, User> join = root.<Project, User> join("owner");
 						User user = User.parseFullName(value);
 						if (user.getLastName() == null) {
 							user.setLastName(user.getFirstName());
 						}
 						List<Predicate> predicates = new LinkedList<Predicate>();
 						if (user.getFirstName() != null) {
 							predicates.add(cb.like(join.<String> get("firstName"), "%" + user.getFirstName() + "%"));
 						}
 						if (user.getLastName() != null) {
 							predicates.add(cb.like(join.<String> get("lastName"), "%" + user.getLastName() + "%"));
 						}
 						if (user.getTitlePre() != null) {
 							predicates.add(cb.like(join.<String> get("titlePre"), "%" + user.getTitlePre() + "%"));
 						}
 						if (user.getTitlePost() != null) {
 							predicates.add(cb.like(join.<String> get("titlePost"), "%" + user.getTitlePost() + "%"));
 						}
 						return cb.or(predicates.toArray(new Predicate[predicates.size()]));
 					}
 				};
 			}
 		},
 		TAG {
 			public Specification<Project> create(final String value) {
 				return new Specification<Project>() {
 					public Predicate toPredicate(Root<Project> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
 						Join<Project, Tag> join = root.<Project, Tag> join("tags");
 						return cb.like(join.<String> get("name"), value + "%");
 					}
 				};
 			}
 		},
 		COURSE {
 			public Specification<Project> create(final String value) {
 				return new Specification<Project>() {
 					public Predicate toPredicate(Root<Project> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
 						Join<Project, ProjectCourse> join = root.<Project, ProjectCourse> join("course");
 						return cb.like(join.<String> get("code"), value + "%");
 					}
 				};
 			}
 		},
 		// FIXME new filtering method
 		/*
 		 * SEMESTER { public Specification<Project> create(final String value) {
 		 * return new Specification<Project>() { public Predicate
 		 * toPredicate(Root<Project> root, CriteriaQuery<?> query,
 		 * CriteriaBuilder cb) { Join<Project, Semester> join = root.<Project,
 		 * Semester> join("startSemester"); return cb.like(join.<String>
 		 * get("name"), value + "%"); } }; } }
 		 */;
 		public String toString() {
 			return super.toString().toLowerCase();
 		}
 	}
 }
