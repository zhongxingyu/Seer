 package no.niths.services;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import no.niths.common.SecurityConstants;
 import no.niths.domain.Student;
 import no.niths.domain.security.Role;
 import no.niths.infrastructure.interfaces.GenericRepository;
 import no.niths.infrastructure.interfaces.RoleRepository;
 import no.niths.infrastructure.interfaces.StudentRepository;
 import no.niths.services.interfaces.StudentService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class StudentServiceImpl extends AbstractGenericService<Student> implements StudentService {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(StudentServiceImpl.class);
 
 	@Autowired
 	private StudentRepository repo;
 
 	@Autowired
 	private RoleRepository roleRepo;
 
 	public Long create(Student student) {
 		Role r = new Role(SecurityConstants.R_STUDENT);
 		List<Role> roles = roleRepo.getAll(r);
 		if (!roles.isEmpty() && roles.size() == 1) {
 			logger.debug("Role given to created student: "
 					+ roles.get(0).getRoleName());
 			student.setRoles(new ArrayList<Role>());
 			student.getRoles().add(roles.get(0));
 		}
 		return repo.create(student);
 	}
 
 	@Override
 	public Student getStudentByEmail(String email) {
 		Student s = new Student(email);
 		List<Student> all = getAll(s);
 		if (!all.isEmpty()) {
 			Student s2 = all.get(0);
 			s2.getRoles().size();
 			return s2;
 		}
 		return null;
 	}
 
 	@Override
 	public Student getStudentBySessionToken(String token) {
 		Student s = new Student();
 		s.setSessionToken(token);
 		List<Student> all = getAll(s);
 		if (!all.isEmpty()) {
 			Student s2 = all.get(0);
 			s2.getRoles().size();
 			return s2;
 		}
 		return null;
 	}
 	
 	/**
 	 * Finds and returns a student with a given id. Returns the student with
 	 * courses, committees, loaned games, loaned consoles and feeds
 	 * 
 	 * @param id
 	 *            ID of student to find
 	 * @return the student
 	 */
 	public Student getById(long id) {
 		Student s = repo.getById(id);
 		if (s != null) {
 			s.getCommittees().size();
 			s.getCourses().size();
             s.getLoans().size();
             s.getFeeds().size();
             
             if(s.getRepresentativeFor() != null){
             	s.getRepresentativeFor().getName();
             }
           
 		
             s.getLoans().size();
             s.getRoles().size();
 
 			int size = s.getFeeds().size();
 			for (int i = 0; i < size; i++) {
 				if (s.getFeeds().get(i).getLocation() != null){
 					s.getFeeds().get(i).getLocation().getPlace();
 				}
 			}
 		}		
 		return s;
 	}
 
 	@Override
 	public Student getStudentWithRoles(Long id) {
 		Student s = repo.getById(id);
 		if (s != null) {
 			s.getRoles().size();
 		}
 		return s;
 	}
 
 	public List<Student> getStudentsWithNamedCourse(String name) {
 		return repo.getStudentsWithNamedCourse(name);
 	}
 	
 	@Override
 	public List<Student> getStudentsAndRoles(Student s) {
 		List<Student> list = repo.getAll(s);
 		
 		for (int i = 0; i < list.size(); i++) {
 			list.get(i).getRoles().size();
 		}
 		return list;
 	}
 
 	@Override
 	public List<Student> getStudentByColumn(String column, String criteria) {
 		List<Student> list = repo.getStudentByColumn(column, criteria);
 		for (int i = 0; i < list.size(); i++) {
 			list.get(i).getRoles().size();
 		}
 
 		return list;
 	}
 
 	@Override
 	public GenericRepository<Student> getRepository() {
 		return repo;
 	}
 
 }
