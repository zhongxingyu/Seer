 package is2.service;
 
 import java.util.List;
 
 import is2.domain.Alumno;
 import is2.repository.AlumnoDao;
 import is2.repository.RoleDao;
 
 import javax.inject.Inject;
 
 import org.springframework.security.crypto.password.PasswordEncoder;
 import org.springframework.stereotype.Service;
 
 @Service
 public class AlumnoService {
 	
 	@Inject
 	PasswordEncoder encoder;
 
 	@Inject
 	AlumnoDao alumnoDao;
 	
 	@Inject
 	RoleDao roleDao;
 	
 	public Alumno find(Long id){
 		return alumnoDao.find(id);
 	}
 	
 	public List<Alumno>  findAll()	{
 		return alumnoDao.findAll();
 	}
 	
 	public Alumno findByUsername(String username)	{
 		return alumnoDao.findByUsername(username);
 	}
 	public Alumno persist(Alumno alumno)	{
 		alumno.setRole(roleDao.findByRole("ROLE_ALUMNO"));
 		return alumnoDao.persist(alumno);
 	}
 	
 	public Alumno merge(Alumno alumno)	{
 		return alumnoDao.merge(alumno);
 	}
 	public void removeById(Long id){
 		alumnoDao.removeById(id);
 	}
 	
 	public Alumno encodePassword(Alumno alumno){
 		alumno.setPassword(encoder.encode(alumno.getPassword()));
 		return alumno;
 	}
 	
 	public Alumno merge_sin_password(Alumno alumno){
 		Alumno original = find(alumno.getId());
 		alumno.setPassword(original.getPassword());
 		return merge(alumno);
 	}
 	
 }
