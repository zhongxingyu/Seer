 package is2.service;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.springframework.stereotype.Service;
 
 import is2.domain.CategoriaNota;
 import is2.repository.CategoriaNotaDao;
 
 @Service
 public class CategoriaNotaService {
 
 	@Inject
 	CategoriaNotaDao categoriaNotaDao;
 	
	public CategoriaNota find(Long id){
		return categoriaNotaDao.find(id);
	}
	
 	public List<CategoriaNota> findAll(){
 		return categoriaNotaDao.findAll();
 	}
 	
 	public CategoriaNota persist(CategoriaNota categoria){
 		return categoriaNotaDao.persist(categoria);
 	}
 	
 	public CategoriaNota merge(CategoriaNota categoria){
 		return categoriaNotaDao.merge(categoria);
 	}
 }
