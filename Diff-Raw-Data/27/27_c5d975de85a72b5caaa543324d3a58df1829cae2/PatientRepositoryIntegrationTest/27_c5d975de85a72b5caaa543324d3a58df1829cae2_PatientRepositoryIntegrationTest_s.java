 package org.medical.dao.mongo;
 
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 
 
 @ContextConfiguration
 public class PatientRepositoryIntegrationTest extends AbstractIntegrationTest {
 
 	@Autowired
	PatientRepository repository;
 
 	@Before
 	public void purgeRepository() {
 		repository.deleteAll();
 		super.setUp();
 	}
 
 	@Test
 	public void createPatient() throws Exception {
 		repository.save(sample1);
 		assertSingleSagarPatient(repository.findOne(sample1.getId()));
 	}
 
 	@Test
 	public void findsSagarByFirstnameLike() throws Exception {
 		repository.save(sample1);
 		repository.save(sample2);
 		assertSingleKunalPatient(repository.findByFirstnameLike("*na*").get(0));
 	}
 }
