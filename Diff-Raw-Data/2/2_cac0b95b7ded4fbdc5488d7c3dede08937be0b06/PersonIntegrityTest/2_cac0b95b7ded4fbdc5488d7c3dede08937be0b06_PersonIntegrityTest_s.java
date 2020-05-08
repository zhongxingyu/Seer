 package com.smalaca.basicentity;
 
 import static org.junit.Assert.*;
 
import java.io.Serializable;

 import org.hibernate.Session;
 import org.junit.Test;
 
 import com.smalaca.dbhandler.HibernateUtil;
 
 public class PersonIntegrityTest {
 
 	
 	@Test
 	public void creat() {
 		Session session = HibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
 
 		Person sebastian = new Person("Sebastian Malaca", "Cracow, Poland");
 		Person junior = new Person("Sebastian Junior", "Cracow, Poland");
 
 		Integer sebastianId = (Integer) session.save(sebastian);
 		Integer juniorId = (Integer) session.save(junior);
 
 		Person sebastianFromDb = (Person) session.get(Person.class, sebastianId);
 		Person juniorFromDb = (Person) session.get(Person.class, juniorId);
 
 		assertTrue(sebastian.equals(sebastianFromDb));
 		assertTrue(junior.equals(juniorFromDb));
 		assertFalse(sebastianFromDb.equals(juniorFromDb));
 		
 		session.getTransaction().rollback();
 		session.close();
 	}
 	
 	@Test
 	public void read() {
 
 	}
 	
 	@Test
 	public void update() {
 
 	}
 	
 	@Test
 	public void delete() {
 
 	}
 }
