 package springdata.jpa.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import springdata.jpa.dto.StudentDTO;
 import springdata.jpa.exception.RestResponseEntityException;
 import springdata.jpa.model.AbstractEntity;
 import springdata.jpa.model.ClassOfferingRegistration;
 import springdata.jpa.model.CurriculumRegistration;
 import springdata.jpa.model.Role;
 import springdata.jpa.model.Student;
 import springdata.jpa.repository.StudentRepository;
 
 import com.google.common.collect.Lists;
 
 @Service
 @Transactional(readOnly=true)
 public class StudentService {
 	
 	@Autowired
 	private StudentRepository studentRepository;
 	
 	public List<Student> getStudents() {
 		return Lists.newArrayList(studentRepository.findAll());
 	}
 
 	public Student getStudent(Long studentId) {
 		return studentRepository.findOne(studentId);
 	}
 
 	@Transactional(readOnly=false)
 	public Student createStudent(StudentDTO studentDTO) {
 		Student student = studentDTO.toStudent();
 		return studentRepository.save(student);
 	}
 	
 	@Transactional(readOnly=false)
 	public Student updateStudent(StudentDTO studentDTO) {
 		Student student = studentDTO.toStudent();
 		Student managedStudent = studentRepository.findOne(student.getId());
 		managedStudent.setFirstName(student.getFirstName());
		managedStudent.setLastName(student.getLastName());
 		//remove all first
 		if(managedStudent.getRoles() != null) {
 			List<Role> cloneOne = Lists.newArrayList(managedStudent.getRoles());
 			for(Role role : cloneOne) {
 				if(student.getRoles() == null || !student.getRoles().contains(role)) managedStudent.removeRole(role);
 			}
 		}
 		//update class offerings
 		if(student.getRoles() != null) {
 			for(Role role : student.getRoles()) {
 				if(managedStudent.getRoles() == null || !managedStudent.getRoles().contains(role)) {
 					managedStudent.addRole(role);
 				}
 				
 			}
 		}
		
		//update manager
		if(!student.getManager().equals(managedStudent.getManager())) {
			Student manager = studentRepository.findOne(student.getManager().getId());
			managedStudent.setManager(manager);
		}
 		return managedStudent;
 	}
 
 	public List<Student> getStudents(Long filterRole) {
 		return studentRepository.getStudents(filterRole);
 	}
 
 	public List<Student> searchStudents(String key) {
 		return studentRepository.findByFirstNameContainingOrLastNameContaining(key, key);
 	}
 
 	public List<AbstractEntity> getEnrolledContent(Long studentId) {
 		Student student = studentRepository.findOne(studentId);
 		if(student == null) throw new RestResponseEntityException("Student not found");
 		
 		List<AbstractEntity> results = new ArrayList<AbstractEntity>();
 		if(student.getEnrolledClassOfferings() != null) {
 			for(ClassOfferingRegistration reg : student.getEnrolledClassOfferings()) {
 				results.add(reg.getClassOffering());
 			}
 		}
 		
 		if(student.getEnrolledCurricula() != null) {
 			for(CurriculumRegistration reg : student.getEnrolledCurricula()) {
 				results.add(reg.getCurriculum());
 			}
 		}
 		return results;
 	}
 
 }
