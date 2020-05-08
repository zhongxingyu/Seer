 package org.fuzzydb.spring.repository;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.net.MalformedURLException;
 
 import org.bson.types.ObjectId;
 import org.fuzzydb.client.Store;
 import org.fuzzydb.server.EmbeddedClientFactory;
 import org.fuzzydb.spring.entities.ObjectIdItem;
 import org.fuzzydb.spring.examples.ExampleCrudRepository;
 import org.fuzzydb.spring.examples.ExampleFuzzyRepository;
 import org.fuzzydb.spring.examples.ObjectIdIndexedFuzzyRepository;
 import org.fuzzydb.spring.repository.support.EnableFuzzyRepositories;
 import org.fuzzydb.spring.transaction.WhirlwindPlatformTransactionManager;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.data.repository.CrudRepository;
 import org.springframework.test.annotation.DirtiesContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.PlatformTransactionManager;
 
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration
 @DirtiesContext
 public class FuzzyRepositoriesJavaConfigTest {
 
     @Configuration
     @EnableFuzzyRepositories(basePackageClasses = ExampleCrudRepository.class)
     static class ContextConfiguration {
 
         @Bean
         public Store store() throws MalformedURLException {
             return EmbeddedClientFactory.getInstance().openStore("wwmdb:/testStore");
         }
 
         @Bean
         public PlatformTransactionManager transactionManager() throws MalformedURLException {
             return new WhirlwindPlatformTransactionManager(store());
         }
     }
 
 	@Autowired
 	private ExampleCrudRepository repo;
 
 	@Autowired
 	private ExampleFuzzyRepository fuzzyRepo;
 
 	@Autowired
 	private ObjectIdIndexedFuzzyRepository objectIdRepo;
 
 	@Test
 	public void repositoryShouldBeCreatedForInterface() {
 		assertTrue( repo instanceof CrudRepository);
 
 		repo.save(new PrimaryKeyedItem("email", "passhash"));
 
 		PrimaryKeyedItem item = repo.findOne("email");
 		assertThat(item.getPassHash(), is("passhash"));
 
 		// TODO: assert repo is configured correctly and that proxy behaviour is as expected
 	}
 
 	@Test
 	public void fuzzyRepositoryShouldBeCreatedForInterface() {
 		assertTrue( fuzzyRepo instanceof FuzzyRepository);
 
 		fuzzyRepo.save(new FuzzyItem("some description"));
 
 		FuzzyItem item = fuzzyRepo.findFirst();
 		assertThat(item.getDescription(), is("some description"));
 	}
 
 	@Test
 	public void shouldBeAbleToRetrieveByObjectId() {
 		assertTrue( objectIdRepo instanceof FuzzyRepository);
 
 		ObjectIdItem item = objectIdRepo.save( new ObjectIdItem("object id item"));
 		ObjectId id = item.getId();
 		assertNotNull(id);
 
 		ObjectIdItem retrieved = objectIdRepo.findOne(id);
 		assertNotNull(retrieved);
 	}
 }
