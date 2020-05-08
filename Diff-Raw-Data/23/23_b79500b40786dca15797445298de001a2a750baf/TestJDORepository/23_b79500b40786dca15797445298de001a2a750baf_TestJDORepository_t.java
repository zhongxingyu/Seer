 /*
  * Copyright (c) 2008-2011 Ivan Khalopik.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.greatage.domain.jdo;
 
 import org.dbunit.JdbcDatabaseTester;
 import org.dbunit.PropertiesBasedJdbcDatabaseTester;
 import org.dbunit.dataset.xml.FlatXmlDataSet;
 import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
 import org.dbunit.operation.DatabaseOperation;
 import org.example.jdo.Company;
 import org.example.jdo.Department;
import org.greatage.domain.AllCriteria;
 import org.greatage.domain.Criteria;
 import org.greatage.domain.Entity;
 import org.greatage.domain.EntityRepository;
 import org.greatage.domain.Pagination;
 import org.testng.Assert;
 import org.testng.annotations.AfterTest;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import javax.jdo.JDOHelper;
 import javax.jdo.PersistenceManagerFactory;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 
 import static org.example.hibernate.Entities.company;
 
 /**
  * @author Ivan Khalopik
  * @since 1.0
  */
 public class TestJDORepository extends Assert {
 	private JDOExecutor executor;
 	private EntityRepository repository;
 	private JdbcDatabaseTester tester;
 
 	@BeforeTest
 	public void setup_repository() {
 		final PersistenceManagerFactory factory = JDOHelper.getPersistenceManagerFactory("jdo.properties");
 		executor = new JDOExecutorImpl(factory);
 		repository = new JDORepository(executor, new HashMap<Class, Class>());
 
 		repository.find(Company.class, Pagination.ALL);
 		repository.find(Department.class, Pagination.ALL);
 		executor.clear();
 	}
 
 	@BeforeTest(dependsOnMethods = "setup_repository")
 	public void setup_database() throws Exception {
 		final Properties properties = new Properties();
 		properties.load(getClass().getResourceAsStream("/dbunit.properties"));
 		tester = new JdbcDatabaseTester(
 				properties.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS),
 				properties.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL),
 				properties.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME),
 				properties.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD),
 				properties.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_SCHEMA)
 		);
 		tester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
 		tester.setTearDownOperation(DatabaseOperation.DELETE_ALL);
 
 		final InputStream inputStream = getClass().getResourceAsStream("/database.xml");
 		final FlatXmlDataSet dataSet = new FlatXmlDataSetBuilder().build(inputStream);
 		tester.setDataSet(dataSet);
 
 		tester.onSetup();
 	}
 
 	@AfterTest
 	public void cleanup_database() throws Exception {
 		tester.onTearDown();
 	}
 
 	@DataProvider
 	public Object[][] find_data() {
 		return new Object[][]{
				{Company.class, company.all(), 4},

 				{Company.class, company.id.eq(3l), 1},
 				{Company.class, company.id.eq(8l), 0},
 				{Company.class, company.name.eq("company2"), 1},
 				{Company.class, company.name.eq("c1"), 0},
 
 				{Company.class, company.id.ne(3l), 3},
 				{Company.class, company.id.ne(8l), 4},
 				// null is ignored
 				{Company.class, company.name.ne("company2"), 2},
 				{Company.class, company.name.ne("c1"), 3},
 
 				{Company.class, company.id.isNull(), 0},
 				{Company.class, company.id.notNull(), 4},
 				{Company.class, company.name.isNull(), 1},
 				{Company.class, company.name.notNull(), 3},
 
 				{Company.class, company.id.gt(3l), 1},
 				{Company.class, company.id.gt(8l), 0},
 				{Company.class, company.name.gt("company2"), 1},
 				{Company.class, company.name.gt("c1"), 3},
 
 				{Company.class, company.id.ge(3l), 2},
 				{Company.class, company.id.ge(8l), 0},
 				{Company.class, company.name.ge("company2"), 2},
 				{Company.class, company.name.ge("c1"), 3},
 
 				{Company.class, company.id.lt(3l), 2},
 				{Company.class, company.id.lt(8l), 4},
 				{Company.class, company.name.lt("company2"), 1},
 				{Company.class, company.name.lt("c1"), 0},
 
 				{Company.class, company.id.le(3l), 3},
 				{Company.class, company.id.le(8l), 4},
 				{Company.class, company.name.le("company2"), 2},
 				{Company.class, company.name.le("c1"), 0},
 
 				/*
 				{Company.class, company.id.like(3l), 1},
 				{Company.class, company.id.like(8l), 0},
 				{Company.class, company.name.like("company2"), 1},
 				{Company.class, company.name.like("c1"), 0},
 
 				{Company.class, company.name.like("%pany2"), 1},
 				{Company.class, company.name.like("%company2"), 1},
 				{Company.class, company.name.like("%pany"), 0},
 				{Company.class, company.name.like("company%"), 3},
 				{Company.class, company.name.like("company2%"), 1},
 				{Company.class, company.name.like("somecompany%"), 0},
 				{Company.class, company.name.like("%company%"), 3},
 				{Company.class, company.name.like("%pany2%"), 1},
 				{Company.class, company.name.like("%pany8%"), 0},
 				{Company.class, company.name.like("%pany8%"), 0},
 				{Company.class, company.name.like("%com%any%"), 3},
 				*/
 
 				{Company.class, company.id.in(1l, 3l), 2},
 				{Company.class, company.id.in(1l, 8l), 1},
 				{Company.class, company.id.in(7l, 8l), 0},
 				{Company.class, company.name.in("company2", "company3"), 2},
 				{Company.class, company.name.in("company8", "company3"), 1},
 				{Company.class, company.name.in("c1"), 0},
 
 				{Company.class, company.name.eq("company1").and(company.id.eq(1l)), 1},
 				{Company.class, company.name.eq("company1").and(company.id.eq(2l)), 0},
 				{Company.class, company.name.eq("company1").or(company.id.eq(1l)), 1},
 				{Company.class, company.name.eq("company1").or(company.id.eq(2l)), 2},
 				/*
 				{Company.class, company.name.like("company%").or(company.name.isNull()), 4},
 				*/
 
 				{Company.class, company.name.eq("company2").not(), 2},
 				{Company.class, company.name.eq("company2").not().not(), 1},
 				{Company.class, company.id.gt(3l).not(), 3},
 				{Company.class, company.id.gt(3l).not().not(), 1},
 				{Company.class, company.name.eq("company1").and(company.id.eq(1l)).not(), 2},
 				{Company.class, company.id.eq(1l).or(company.id.eq(2l)).not(), 2},
 
 				/*
 				{Company.class, company.name.eq("company1").and(company.id.eq(1l)).and(company.name.like("company%")), 1},
 				{Company.class, company.name.eq("company1").and(company.id.eq(2l)).and(company.name.isNull()), 0},
 				{Company.class, company.name.eq("company1").or(company.id.eq(1l)).or(company.name.like("company%")), 3},
 				{Company.class, company.name.eq("company1").or(company.id.eq(2l)).or(company.name.isNull()), 3},
 				{Company.class, company.name.eq("company1").and(company.id.eq(1l)).or(company.name.like("company%")), 3},
 				{Company.class, company.name.eq("company1").or(company.id.eq(4l)).and(company.name.isNull()), 1},
 
 				{Department.class, department.name.like("dep%").and(department.company.name.eq("company1")), 3},
 				{Department.class, department.name.eq("department4").and(department.company.name.eq("company1")), 0},
 				*/
 		};
 	}
 
 	@Test(dataProvider = "find_data")
 	public <PK extends Serializable, E extends Entity<PK>>
 	void test_find(final Class<E> entityClass, final Criteria<PK, E> criteria, final long expectedCount) {
 		final long actualCount = repository.count(entityClass, criteria);
 		assertEquals(actualCount, expectedCount);
 		final List<E> actual = repository.find(entityClass, criteria, Pagination.ALL);
 		assertNotNull(actual);
 		assertEquals(actual.size(), expectedCount);
 	}
 
 	private static long[] ids(final long... ids) {
 		return ids;
 	}
 }
