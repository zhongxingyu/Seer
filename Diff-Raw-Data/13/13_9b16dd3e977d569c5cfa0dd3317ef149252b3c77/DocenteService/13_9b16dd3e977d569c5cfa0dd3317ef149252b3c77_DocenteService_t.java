 package is2.service;
 import java.util.List;
 
import is2.domain.CursoDictado;
 import is2.domain.Docente;
 import is2.repository.DocenteDao;
 
 import javax.inject.Inject;
 
 import org.springframework.stereotype.Service;
 
 @Service
 public class DocenteService {
 	@Inject
 	DocenteDao docenteDao;
 	
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
 		return docenteDao.persist(docente);
 	}
 	
 	public Docente merge(Docente docente)
 	{
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
 }
