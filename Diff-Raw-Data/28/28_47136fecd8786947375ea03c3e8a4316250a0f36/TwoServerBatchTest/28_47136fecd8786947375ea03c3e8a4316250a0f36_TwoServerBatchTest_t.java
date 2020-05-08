 package org.opencredo.batch.modules.batch;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.Properties;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.Executor;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.opencredo.batch.modules.domain.Order;
 import org.opencredo.batch.modules.domain.OrderStatus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.batch.core.Job;
 import org.springframework.batch.core.JobExecution;
 import org.springframework.batch.core.JobParameters;
 import org.springframework.batch.core.JobParametersBuilder;
 import org.springframework.batch.core.launch.JobLauncher;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.core.task.SimpleAsyncTaskExecutor;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.TransactionCallback;
 import org.springframework.transaction.support.TransactionTemplate;
 
 /**
  * This simulates two servers both running the same job
  * 
  * @author Neale Upstone
  */
//@Ignore("Need to sort autowire issue. contexts[] are not behaving same way as test context")
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/batch-property-placeholders.xml", "classpath:/hibernate-config.xml", "classpath:/batch-infrastructure-single.xml"})
 public class TwoServerBatchTest {
 
	private static final int NUM_SERVERS = 2;
 	
 	private static final String[] configLocation = {"classpath:/batch-applicationContext.xml"};
 	
 	private static ClassPathXmlApplicationContext contexts[] = new ClassPathXmlApplicationContext[NUM_SERVERS];
 	private static Executor executor = new SimpleAsyncTaskExecutor("jobThread");
 	private static CountDownLatch latch = new CountDownLatch(contexts.length);
 	
 	private Logger logger = LoggerFactory.getLogger(getClass());
 
 	@PersistenceContext
 	private EntityManager entityManager;
 
	private int orderCount = 400;
 
 	TransactionTemplate dataTxTemplate;
 
 	
 //    @Autowired
 //    Job job;
 //
 //    @Autowired
 //    JobLauncher jobLauncher;
 
     
     @Autowired
     public void setTransactionManager(JpaTransactionManager txManager) {
         this.dataTxTemplate = new TransactionTemplate(txManager);
     }
 
 
     /** Create parameterized contexts: this must be done after */ 
     static public void createInstances(){
 //    	System.err.println(InetAddress.getLocalHost().getHostAddress());
 
     	for (int i = 0; i < contexts.length; i++) {
     		String[] locations = configLocation;
     		Properties props = new Properties();
     		props.setProperty("instanceKey", "context-" + i);
     		props.setProperty("initDbSchemas", "false");
     		props.setProperty("hibernate.hbm2ddl.auto", "none");
     		contexts[i] = createParameterisedContext(locations, props);
 		}
     }
 
     /**
      * Creates a {@link ClassPathXmlApplicationContext} for the supplied context files, configured 
      * with an additional {@link PropertyPlaceholderConfigurer} to substitute the supplied {@link Properties}
      * within the ${...} templates. 
      * 
      * @return a refreshed application context ready to use
      */
 	public static ClassPathXmlApplicationContext createParameterisedContext(String[] locations, Properties props) {
 		PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
 		configurer.setProperties(props);
 		configurer.setIgnoreUnresolvablePlaceholders(true);
 		configurer.setOrder(Integer.MAX_VALUE);
 		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(locations, false);
 		context.addBeanFactoryPostProcessor(configurer);
 		context.refresh();
 		return context;
 	}
     
     @Before
     public void createTestData() {
     	createInstances();
 
     	assertNotNull(this.entityManager);
     	
     	dataTxTemplate.execute(new TransactionCallback<Object>() {
             public Object doInTransaction(TransactionStatus status) {
                 for (int i = 0; i < orderCount; i++) {
                     Order order = new Order();
                     entityManager.persist(order);
                 }
                 entityManager.flush();
                 return null;
             }
         });
     }
 
 
 
     public void runParallelJobs() throws InterruptedException {
 
     	for (final BeanFactory context : contexts) {
 			executor.execute(new Runnable(){
 
 				public void run() {
 					JobLauncher jobLauncher = context.getBean(JobLauncher.class);
 					Job job = context.getBean(Job.class);
 					
 					JobParameters jobParameters = new JobParametersBuilder().addString("thread", Thread.currentThread().getName()).toJobParameters();
 					try {
 						JobExecution exec = jobLauncher.run(job, jobParameters );
 						while(exec.isRunning()){
 							Thread.sleep(100);
 						}
 					} catch (Exception e) {
 						throw new RuntimeException(e);
 					}
 					latch.countDown();
 				}
 			});
 		}
     	latch.await();
     }
     
     
     @Test
     public void jobShouldRunAndProcessCorrectNumberOfItems() throws Exception {
         assertEquals("Wrong count of orders at start", orderCount, (long) countOrdersWithStatus(OrderStatus.REQUESTED));
         runParallelJobs();
         assertEquals("Wrong count of orders at end", 0, (long) countOrdersWithStatus(OrderStatus.REQUESTED));
 
     }
 
     public Long countOrdersWithStatus(OrderStatus status) {
         return (Long) entityManager.createQuery("select count(o) from Order as o where o.status = :status").setParameter("status", status).getSingleResult();
     }
 
 
 }
