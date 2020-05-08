 package is2.service;
 
 import java.util.List;
 
 import is2.domain.Carrera;
 import is2.repository.CarreraDao;
 
 import javax.inject.Inject;
 
 import org.springframework.stereotype.Service;
 
 @Service
 public class CarreraService {
 	@Inject
 	CarreraDao carreraDao;
 	
 	public List<Carrera> findAll()
 	{
 		return carreraDao.findAll();
 	}
 	
 	public Carrera find(Long id)
 	{
 		return carreraDao.find(id);
 	}
 	
 	public Carrera persist(Carrera carrera)
 	{
 		return carreraDao.persist(carrera);
 	}
 	
 	public Carrera merge(Carrera carrera)
 	{
 		return carreraDao.merge(carrera);
 	}
	
	public void removeById(Long id){
		carreraDao.removeById(id);
	}
 }
