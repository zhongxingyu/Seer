 /*
  * Copyright 2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.nebulae2us.stardust;
 
 import java.sql.Timestamp;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 import org.nebulae2us.stardust.ddl.domain.DDLGenerator;
 import org.nebulae2us.stardust.internal.util.ObjectUtils;
 import org.nebulae2us.stardust.my.domain.EntityRepository;
 import org.nebulae2us.stardust.translate.domain.CommonTranslatorController;
 
 import org.nebulae2us.stardust.DaoManager;
 
 import static org.junit.Assert.*;
 
 /**
  * @author Trung Phan
  *
  */
 @RunWith(Parameterized.class)
 public class OneEntity_IT extends BaseIntegrationTest {
 
 	private EntityRepository entityRepository;
 	
 	private DaoManager daoManager;
 
 	private DDLGenerator ddlGenerator;
 
 	@Parameters
 	public static Collection<Object[]> data() {
 		return Arrays.asList(new Object[][]{
 				{"derby-in-memory"}, {"h2-in-memory"}, {"hsqldb-in-memory"}
 		});
 	}
 	
 	
 	public static class Person {
 		
 		@Id
 		@GeneratedValue(strategy=GenerationType.IDENTITY)
 		private Long id;
 		
 		private String firstName;
 		
 		private String lastName;
 		
 		private Date dateBorn;
 	}
 	
 	
 	public OneEntity_IT(String config) {
 		super(config);
 	}
 
 	
 	@Before
 	public void setup() {
 		this.entityRepository = new EntityRepository();
 		this.ddlGenerator = new DDLGenerator(dialect, entityRepository);
 		this.entityRepository.scanEntities(Person.class);
 		this.daoManager = new DaoManager(jdbcExecutor, entityRepository, new CommonTranslatorController(), dialect);
 		
 		List<String> ddls = ddlGenerator.generateCreateSchemaObjectsDDL();
 		for (String ddl : ddls) {
 			jdbcExecutor.execute(ddl);
 		}
 	}
 	
 	@After
 	public void tearDown() throws Exception {
 		List<String> ddls = ddlGenerator.generateDropSchemaObjectsDDL();
 		for (String ddl : ddls) {
 			jdbcExecutor.execute(ddl);
 		}
 	}
 	
 	private Long insertPerson(String firstName, String lastName, Date dateBorn) {
 		Person person = new Person();
 		person.firstName = firstName;
 		person.lastName = lastName;
 		person.dateBorn = dateBorn;
 		
 		daoManager.save(person);
 		
 		return person.id;
 	}
 	
 	@Test
 	public void should_return_empty_for_blank_table() {
 		
 		List<Person> people =
 		daoManager.newQuery(Person.class)
 			.list();
 		
 		assertEquals(0, people.size());
 		
 	}
 	
 	@Test
 	public void should_be_able_to_insert_one_record() {
 		
 		Person person = new Person();
 		person.firstName = "First";
 		person.lastName = "Last";
 		person.dateBorn = new Timestamp(100);
 		
 		daoManager.save(person);
 		
 		assertEquals(Long.valueOf(1), person.id);
 	} 
 	
 	@Test
 	public void should_be_able_to_use_customized_sql() {
 
 		should_be_able_to_insert_one_record();
 		
 		List<Person> people = daoManager.newQuery(Person.class)
 			.backedBySql("select first_name first_name, last_name from person")
 			.filterBy()
 				.predicate("firstName = ?", "First")
 			.endFilter()
 			.list();
 
 		assertEquals(1, people.size());
 		
 		Person person = people.get(0);
 		assertNull(person.dateBorn);
 		assertNull(person.id);
 		assertEquals("First", person.firstName);
 	}
 	
 	@Test
 	public void should_be_able_to_count() {
 		
 		should_be_able_to_insert_one_record();
 		
 		long count = daoManager.newQuery(Person.class)
 				.count();
 
 		assertEquals(1, count);
 		
 	}
 	
 	@Test
 	public void should_be_able_to_count_distinct() {
 		should_be_able_to_insert_one_record();
 		
 		long count = daoManager.newQuery(Person.class)
 				.distinct()
 				.count();
 		
 		assertEquals(1, count);
 	}
 	
 	@Test
 	public void should_be_able_to_list_top_5() {
 		
 		insertPerson("First 1", "Last 1", null);
 		insertPerson("First 2", "Last 2", null);
 		insertPerson("First 3", "Last 3", null);
 		insertPerson("First 4", "Last 4", null);
 		insertPerson("First 5", "Last 5", null);
 		insertPerson("First 6", "Last 6", null);
 		insertPerson("First 7", "Last 7", null);
 		insertPerson("First 8", "Last 8", null);
 		insertPerson("First 9", "Last 9", null);
 
 		List<Person> people = daoManager.newQuery(Person.class)
 				.maxResults(5)
 				.orderBy("firstName")
 				.list();
 		
 		assertEquals(5, people.size());
 		assertEquals(Arrays.asList("First 1", "First 2", "First 3", "First 4", "First 5"), ObjectUtils.extractValues(Person.class, people, "firstName"));
 		
 	}
 	
 	@Test
 	public void should_be_able_to_list_next_5() {
 		
 		insertPerson("First 1", "Last 1", null);
 		insertPerson("First 2", "Last 2", null);
 		insertPerson("First 3", "Last 3", null);
 		insertPerson("First 4", "Last 4", null);
 		insertPerson("First 5", "Last 5", null);
 		insertPerson("First 6", "Last 6", null);
 		insertPerson("First 7", "Last 7", null);
 		insertPerson("First 8", "Last 8", null);
 		insertPerson("First 9", "Last 9", null);
 
 		List<Person> people = daoManager.newQuery(Person.class)
 				.firstResult(3)
 				.maxResults(5)
 				.orderBy("firstName")
 				.list();
 		
 		assertEquals(5, people.size());
 		assertEquals(Arrays.asList("First 4", "First 5", "First 6", "First 7", "First 8"), ObjectUtils.extractValues(Person.class, people, "firstName"));
 	}
 	
 	
 	
 }
