 package no.niths.infrastructure;
 
 import java.util.List;
 
 import no.niths.common.QueryGenerator;
 import no.niths.domain.Student;
 import no.niths.infrastructure.interfaces.StudentRepository;
 
 import org.hibernate.Criteria;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class StudentRepositoryImpl extends GenericRepositoryImpl<Student>
 		implements StudentRepository {
 
 	
 	private QueryGenerator<Student> queryGen;
 	
 	public StudentRepositoryImpl() {
 		super(Student.class);
		queryGen = new QueryGenerator<>(Student.class);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Student> getStudentsWithNamedCourse(String name) {
 		String sql = "from " + Student.class.getSimpleName()
 				+ " s join fetch s.courses c where c.name=:name";
 		return getSession().getCurrentSession().createQuery(sql)
 				.setString("name", name)
 				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
 	}
 
 	public Student getStudentByEmail(String email) {
 		String sql = "from " + Student.class.getSimpleName()
 				+ " s where s.email=:email";
 		return (Student) getSession().getCurrentSession().createQuery(sql)
 				.setString("email", email).uniqueResult();
 	}
 
 	@Override
 	public Student getStudentBySessionToken(String sessionToken) {
 		String sql = "from " + Student.class.getSimpleName()
 				+ " s where s.sessionToken=:token";
 		return (Student) getSession().getCurrentSession()
 				.createQuery(sql)
 				.setString("token", sessionToken)
 				.uniqueResult();
 	}
 	
 	@Override
 	public void hibernateDelete(long id) {
 		Student s = new Student();
 		s.setId(id);
 
 		getSession().getCurrentSession().delete(s);
 	}
 
 	@Override
 	public List<Student> getStudentByColumn(String column, String criteria) {
 		
 		return queryGen.whereQuery(criteria, column, getSession().getCurrentSession());
 	}
 
 }
