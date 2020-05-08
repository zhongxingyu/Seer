 package com.tda.persistence;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.tda.model.Prueba;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring-persistence.xml"})
 public class PruebaDAOTest {
 	@Autowired
 	PruebaDAO pruebaDAO;
 	
 	@Test
 	public void testPruebaSave() {
 		Prueba prueba = new Prueba();
 		prueba.setId(1);
 		prueba.setNombre("polaco");
 		
 		pruebaDAO.save(prueba);
 		
 		Prueba aPrueba = pruebaDAO.getById(prueba.getId());
 		
 		assertEquals(prueba.getId(), aPrueba.getId());
 		assertEquals(prueba.getNombre(), aPrueba.getNombre());
 	}
 }
