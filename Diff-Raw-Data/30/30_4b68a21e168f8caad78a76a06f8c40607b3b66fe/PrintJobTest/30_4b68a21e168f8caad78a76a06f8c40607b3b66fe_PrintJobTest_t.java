 package pro.softcom.test.hibernate.ordercolumn;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.transaction.UserTransaction;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(Arquillian.class)
 public class PrintJobTest {
 
 	@Deployment
 	public static Archive<?> createTestArchive() {
 		return ShrinkWrap
 				.create(WebArchive.class, "test.war")
 				.addClasses(PrintQueue.class, PrintJob.class)
 				.addAsResource("META-INF/persistence.xml",
 						"META-INF/persistence.xml")
 				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
 	}
 
 	@PersistenceContext
 	EntityManager em;
 
 	@Inject
 	UserTransaction utx;
 
 	/**
	 * This test aimed at showing the number of update statements generated when using @OrderColumn.
	 * 
	 * In fact @OrderColumn is not supported by Hibernate when applied on the inverse side of a @OneToMany 
	 * relationship. So it is simply ignored here.
	 * 
	 * See https://hibernate.onjira.com/browse/HHH-5732
 	 */
 	@Test
 	public void testPersistPrintJobs() throws Exception {

 		PrintQueue queue = new PrintQueue();
 		queue.setName("test");

 		PrintJob job = new PrintJob();
 		job.setQueue(queue);
 		job.setContent("job1");
 		queue.getJobs().add(job);
 		em.persist(job);

 		job = new PrintJob();
 		job.setQueue(queue);
 		job.setContent("job2");
 		queue.getJobs().add(job);
 		em.persist(job);

 		em.persist(queue);

 		// => 2 update PrintJob set content=?, queue_name=? where id=?
 	}
 
 	@Before
 	public void preparePersistenceTest() throws Exception {
 		clearData();
 		startTransaction();
 	}
 
 	@After
 	public void commitTransaction() throws Exception {
		utx.commit();
 	}

 	private void clearData() throws Exception {
 		utx.begin();
 		em.joinTransaction();
 		System.out.println("Dumping old records...");
 		em.createQuery("delete from PrintJob").executeUpdate();
 		em.createQuery("delete from PrintQueue").executeUpdate();
 		utx.commit();
 	}
 
 	private void startTransaction() throws Exception {
 		utx.begin();
 		em.joinTransaction();
 	}
 
 }
