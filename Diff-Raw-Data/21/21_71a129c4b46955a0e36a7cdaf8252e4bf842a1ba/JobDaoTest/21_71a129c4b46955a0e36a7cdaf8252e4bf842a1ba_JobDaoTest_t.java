 package org.imirsel.dao;
 
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.sql.DataSource;
 
 import org.imirsel.dao.JobDao;
 import org.imirsel.model.Job;
 import org.imirsel.plugins.JndiInitializeServlet;
import org.imirsel.nema.directoryservice.JndiHelper;
import org.imirsel.nema.directoryservice.PluginConstants;
 import org.jmock.Mockery;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 
 
 public class JobDaoTest {
 	
 	public static DataSource dataSource;
 	private Logger logger = Logger.getAnonymousLogger();
 	private Mockery context = new Mockery();
 	
 	@Before
 	public void initialize(){
 		ServletConfig servletConfig = context.mock(ServletConfig.class);
 		JndiInitializeServlet jndiServlet = new JndiInitializeServlet();
 		jndiServlet.setLogger(logger);
 		try {
 			jndiServlet.init(servletConfig);
 		} catch (ServletException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	
 		try {
 			dataSource= JndiHelper.getJobStatusDataSource();
 		} catch (Exception e) {
 			logger.severe("Error could not get dataSource for NEMA...\n");
 			System.exit(0);
 		}
 	}
 	
 	
 	@Test
 	public void testInsertJob(){
 		Job job = new Job();
 		job.setDescription("A Description");
 		job.setName("test1");
 		job.setEndTimestamp(new Date());
 		job.setStartTimestamp(new Date());
 		job.setExecutionInstanceId("http://test.org/token");
 		job.setHost("http://127.0.0.1");
 		job.setPort(1714);
 		job.setOwnerEmail("amitku@uiuc.edu");
 		job.setOwnerId(1l);
 		job.setStatusCode(Job.JobStatus.toJobStatus(Job.JobStatus.STARTED));
 		job.setSubmitTimestamp(new Date());
 		job.setToken("token");
 		job.setUpdateTimestamp(new Date());
 		JobDao jdao = new JobDao(dataSource);
 		boolean success=jdao.insertJob(job);
 		assert(success==Boolean.TRUE);
 	}
 	
 	
 	
 	
 	
 	
 	@After
 	public void dispose(){
 	
 	}
 
 }
