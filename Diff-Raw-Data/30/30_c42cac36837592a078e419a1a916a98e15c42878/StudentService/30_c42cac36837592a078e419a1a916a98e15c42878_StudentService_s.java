 package no.niths.services;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import no.niths.domain.Student;
 import no.niths.infrastructure.interfaces.StudentRepository;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Transactional
 public class StudentService {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(StudentService.class);
 
 	@Autowired
 	private StudentRepository repo;
 
 	public void createStudent(Student student) {
 		repo.create(student);
 	}
 
 	public Student getStudentById(long id) {

 		Student s = repo.getById(id);

 		if (s != null) {
			for (int i = 0; i < s.getCommittees().size(); i++) {
				s.getCommittees().get(i).setEvents(null);
 			}

			for (int i = 0; i < s.getCourses().size(); i++) {
				s.getCourses().get(i).setTopics(null);
 			}
 		}
 		return s;
 	}
 
 	public List<Student> getAllStudents() {
 		ArrayList<Student> temp = (ArrayList<Student>) repo.getAllStudents();
 		for (int i = 0; i < temp.size(); i++) {
 			temp.get(i).setCommittees(null);
 			temp.get(i).setCourses(null);
 		}
 		return temp;
 	}
 
 	public List<Student> getAllStudents(Student s) {
 		ArrayList<Student> temp = (ArrayList<Student>) repo.getAllStudents(s);
 		for (int i = 0; i < temp.size(); i++) {
 			// temp.get(i).getCommittees().size();
 			// temp.get(i).getCourses().size();
 			temp.get(i).setCommittees(null);
 			temp.get(i).setCourses(null);
 		}
 		return temp;
 	}
 
 	public void updateStudent(Student student) {
 		repo.update(student);
 	}
 
 	public boolean deleteStudent(long id) {
 		return repo.delete(id);
 	}
 }
