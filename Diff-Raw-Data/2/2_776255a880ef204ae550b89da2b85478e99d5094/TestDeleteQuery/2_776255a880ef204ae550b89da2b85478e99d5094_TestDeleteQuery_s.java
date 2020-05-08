 package org.objectquery.jpa;
 
 import javax.persistence.EntityManager;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.objectquery.DeleteQuery;
 import org.objectquery.generic.GenericeDeleteQuery;
 import org.objectquery.jpa.domain.Other;
 import org.objectquery.jpa.domain.Person;
 
 public class TestDeleteQuery {
 
 	private EntityManager entityManager;
 
 	@Before
 	public void beforeTest() {
 		entityManager = PersistentTestHelper.getFactory().createEntityManager();
 		entityManager.getTransaction().begin();
 	}
 
 	@Test
 	public void testSimpleDelete() {
 		Other ot = new Other();
 		ot.setText("text");
		entityManager.persist(ot);
 		DeleteQuery<Other> dq = new GenericeDeleteQuery<Other>(Other.class);
 		int deleted = JPAObjectQuery.execute(dq, entityManager);
 		Assert.assertTrue(deleted != 0);
 
 	}
 
 	@Test
 	public void testSimpleDeleteGen() {
 		DeleteQuery<Person> dq = new GenericeDeleteQuery<Person>(Person.class);
 		JPQLQueryGenerator q = JPAObjectQuery.jpqlGenerator(dq);
 		Assert.assertEquals("delete org.objectquery.jpa.domain.Person ", q.getQuery());
 	}
 
 	@Test
 	public void testDeleteCondition() {
 		Person to_delete = new Person();
 		to_delete.setName("to-delete");
 		entityManager.persist(to_delete);
 
 		DeleteQuery<Person> dq = new GenericeDeleteQuery<Person>(Person.class);
 		dq.eq(dq.target().getName(), "to-delete");
 		int deleted = JPAObjectQuery.execute(dq, entityManager);
 		Assert.assertTrue(deleted != 0);
 	}
 
 	@Test
 	public void testDeleteConditionGen() {
 
 		DeleteQuery<Person> dq = new GenericeDeleteQuery<Person>(Person.class);
 		dq.eq(dq.target().getName(), "to-delete");
 		JPQLQueryGenerator q = JPAObjectQuery.jpqlGenerator(dq);
 		Assert.assertEquals("delete org.objectquery.jpa.domain.Person  where name  =  :name", q.getQuery());
 	}
 
 	@After
 	public void afterTest() {
 		if (entityManager != null) {
 			entityManager.getTransaction().commit();
 			entityManager.close();
 		}
 		entityManager = null;
 	}
 
 }
