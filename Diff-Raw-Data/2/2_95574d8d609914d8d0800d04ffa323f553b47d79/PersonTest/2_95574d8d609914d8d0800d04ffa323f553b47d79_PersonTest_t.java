 package com.kspichale.neo4j;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.junit.Assert.assertEquals;
 
 import java.util.Set;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.neo4j.graphdb.PropertyContainer;
 import org.neo4j.graphdb.index.Index;
 import org.neo4j.graphdb.index.IndexHits;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.neo4j.support.Neo4jTemplate;
 import org.springframework.data.neo4j.support.node.Neo4jHelper;
 import org.springframework.test.annotation.Rollback;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.BeforeTransaction;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.kspichale.neo4j.Person;
 import com.kspichale.neo4j.PersonService;
 
 @ContextConfiguration(locations = "classpath:/spring/applicationContext.xml")
 @RunWith(SpringJUnit4ClassRunner.class)
 @Transactional
 public class PersonTest {
 
 	@Autowired
 	private PersonService personService;
 
 	@Autowired
 	private Neo4jTemplate template;
 
 	@Rollback(false)
 	@BeforeTransaction
 	public void cleanUpGraph() {
 		Neo4jHelper.cleanDb(template);
 	}
 
 	@Test
 	public void createPersonsTest() {
 		assertEquals(0, personService.getNumberOfPersons());
 		Set<Person> persons = personService.createPersons();
 		assertEquals(3, personService.getNumberOfPersons());
 
 		Iterable<Person> foundPersons = personService.getAllPersons();
 		for (Person p : foundPersons) {
 			assertThat(p).isIn(persons);
 		}
 	}
 
 	@Test
 	public void findByName() {
 		String name = "John";
 		personService.createPersons();
 		Person p = personService.findByName(name);
 		assertThat(p).isNotNull();
 		assertThat(p.getName()).isEqualTo(name);
 	}
 
 	@Test
 	public void testCypherQuery() {
 		personService.createPersons();
 		Iterable<Person> friends = personService.getFriends();
 		assertThat(friends.iterator().hasNext()).isTrue();
 		for (Person friend : friends) {
 			System.out.println(friend);
 		}
 	}
 
 	@Test
	public void testFulltextIndex() {
 		personService.createPersons();
 
 		Index<PropertyContainer> index = template.getIndex("peoplesearch");
 		IndexHits<PropertyContainer> indexHits = index.query("name", "Jo*");
 		for (PropertyContainer c : indexHits) {
 			String name = (String) c.getProperty("name");
 			System.out.println(name);
 		}
 	}
 }
