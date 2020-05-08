 package org.objectquery.hibernate;
 
 import junit.framework.Assert;
 
 import org.hibernate.Session;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.objectquery.DeleteQuery;
 import org.objectquery.generic.GenericeDeleteQuery;
 import org.objectquery.hibernate.domain.Other;
 import org.objectquery.hibernate.domain.Person;
 
 public class TestDeleteQuery {
 
 	private Session session;
 
 	@Before
 	public void beforeTest() {
 		session = PersistentTestHelper.getFactory().openSession();
 		session.getTransaction().begin();
 	}
 
 	@Test
 	public void testSimpleDelete() {
 		Other ot = new Other();
 		ot.setText("text");
		session.merge(ot);
 		DeleteQuery<Other> dq = new GenericeDeleteQuery<Other>(Other.class);
 		int deleted = HibernateObjectQuery.execute(dq, session);
 		Assert.assertTrue(deleted != 0);
 	}
 
 	@Test
 	public void testSimpleDeleteGen() {
 		DeleteQuery<Person> dq = new GenericeDeleteQuery<Person>(Person.class);
 		HQLQueryGenerator q = HibernateObjectQuery.hqlGenerator(dq);
 		Assert.assertEquals("delete org.objectquery.hibernate.domain.Person ", q.getQuery());
 	}
 
 	@Test
 	public void testDeleteCondition() {
 		Person to_delete = new Person();
 		to_delete.setName("to-delete");
 		session.persist(to_delete);
 
 		DeleteQuery<Person> dq = new GenericeDeleteQuery<Person>(Person.class);
 		dq.eq(dq.target().getName(), "to-delete");
 		int deleted = HibernateObjectQuery.execute(dq, session);
 		Assert.assertTrue(deleted != 0);
 	}
 
 	@Test
 	public void testDeleteConditionGen() {
 		DeleteQuery<Person> dq = new GenericeDeleteQuery<Person>(Person.class);
 		dq.eq(dq.target().getName(), "to-delete");
 		HQLQueryGenerator q = HibernateObjectQuery.hqlGenerator(dq);
 		Assert.assertEquals("delete org.objectquery.hibernate.domain.Person  where name  =  :name", q.getQuery());
 	}
 
 	@After
 	public void afterTest() {
 		if (session != null) {
 			session.getTransaction().commit();
 			session.close();
 		}
 		session = null;
 	}
 
 }
