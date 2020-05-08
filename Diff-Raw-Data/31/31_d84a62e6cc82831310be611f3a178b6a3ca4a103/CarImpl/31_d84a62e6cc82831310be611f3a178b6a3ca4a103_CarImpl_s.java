 package com.medullan.ref.webapp.component;
 
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.medullan.ref.webapp.dao.PersonDao;
 import com.medullan.ref.webapp.domain.Person;
 
 /**
  * 
  * @author rnorris
  * 
  */
 @Component
 public class CarImpl implements Car {
 
 	private PersonDao personDao;
 
 	public CarImpl() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.medullan.rampup.Car#startEngine()
 	 */
 	@Override
 	@Transactional
 	public void startEngine() {
 		Person p = new Person();
 		p.setFirstName("Ryan");
 		personDao.save(p);
 	}
 	
 	@Autowired
 	public void setPersonDao(PersonDao personDao) {
 		this.personDao = personDao;
 	}
 	
 	public PersonDao getPersonDao() {
 		return personDao;
 	}
 
 }
