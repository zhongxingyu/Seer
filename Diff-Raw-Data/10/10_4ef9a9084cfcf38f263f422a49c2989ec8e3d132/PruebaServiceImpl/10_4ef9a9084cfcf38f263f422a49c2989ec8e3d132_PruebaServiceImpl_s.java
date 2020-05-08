 package com.tda.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.tda.model.Prueba;
 import com.tda.persistence.PruebaDAO;
 
 public class PruebaServiceImpl implements PruebaService {
 
 	PruebaDAO pruebaDao;
 
 	@Required
 	public void setPruebaDao(PruebaDAO pruebaDao) {
 		this.pruebaDao = pruebaDao;
 	}
 
 	@Transactional(readOnly = false)
 	public void save(Prueba prueba) {
 		pruebaDao.save(prueba);
 	}
 
 	@Transactional(readOnly = false)
 	public void delete(Prueba prueba) {
 		pruebaDao.delete(prueba);
 	}
 
 	@Transactional(readOnly = true)
 	public Prueba getById(int id) {
 		return pruebaDao.getById(id);
 	}
 
 	@Transactional(readOnly = true)
 	public List<Prueba> getAll() {
 		return pruebaDao.getAll();
 	}
 
 }
