 package is2.service;
 import java.util.List;
 
 import is2.domain.CursoDictado;
 import is2.domain.Docente;
 import is2.repository.DocenteDao;
 import is2.repository.RoleDao;
 
 import javax.inject.Inject;
 
 import org.springframework.security.crypto.password.PasswordEncoder;
 import org.springframework.stereotype.Service;
 
 @Service
 public class DocenteService {
 	@Inject
 	DocenteDao docenteDao;
 	
 	@Inject
 	RoleDao roleDao;
 	
 	@Inject
 	PasswordEncoder encoder;
 	
 	public List<Docente> findAll()
 	{
 		return docenteDao.findAll();
 	}
 	
 	public Docente find(Long id)
 	{
 		return docenteDao.find(id);
 	}
 	
 	public Docente persist(Docente docente)
 	{
 		docente.setRole(roleDao.findByRole("ROLE_DOCENTE"));
 		return docenteDao.persist(docente);
 	}
 	
 	public Docente merge(Docente docente)
 	{
		docente.setRole(roleDao.findByRole("ROLE_DOCENTE"));
 		return docenteDao.merge(docente);
 	}
 	
 	public Docente findByUsername(String username)
 	{
 		return docenteDao.findByUsername(username);
 	}
 	
 	public List<CursoDictado> getCursosDictados(Long id)
 	{
 		return docenteDao.find(id).getCursosDictados();
 	}
 	public Docente encodePassword(Docente docente){
 		docente.setPassword(encoder.encode(docente.getPassword()));
 		return docente;
 	}
 	public Docente merge_sin_password(Docente docente){
 		Docente original = find(docente.getId());
 		docente.setPassword(original.getPassword());
 		return merge(docente);
 	}
	public void removeById(Long id){
		docenteDao.removeById(id);
	}
 }
