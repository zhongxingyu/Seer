 package org.os.javaee.orm.multitenanacy.entity;
 
 import java.security.SecureRandom;
 import java.util.Random;
 
 import org.apache.log4j.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.os.javaee.orm.multitenancy.context.CompositeTenantContext;
 import org.os.javaee.orm.multitenancy.context.CompositeTenantInfo;
 import org.os.javaee.orm.multitenancy.context.ITenantContext;
 import org.os.javaee.orm.multitenancy.context.ITenantContextHolder;
 import org.os.javaee.orm.multitenancy.entity.CompositeMultiTenancyEnabledEntity;
 import org.os.javaee.orm.multitenancy.entity.MultiTenancyEnabledDAO;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * <p>Title: CompositeMultiTenancyEnabledEntityTest</p>
  * <p><b>Description:</b> CompositeMultiTenancyEnabledEntityTest</p>
  * <p>Copyright: Copyright (c) 2013</p>
  * <p>Company: Open Source Development.</p>
  * @author Murali Reddy
  * @version 1.0
  */
 public class CompositeMultiTenancyEnabledEntityTest {
 
 	@SuppressWarnings("unused")
 	private static Logger log = Logger.getLogger(CompositeMultiTenancyEnabledEntityTest.class);
 
 	static MultiTenancyEnabledDAO<CompositeMultiTenancyEnabledEntity> dao = null;
 	static ITenantContextHolder tenantContextHolder = null;
 	static Random random = null;
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@SuppressWarnings("unchecked")
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		@SuppressWarnings("resource")
 		ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-compositeconfig.xml");
 		dao = ctx.getBean("mtEnabledDAO", MultiTenancyEnabledDAO.class);
 		tenantContextHolder = ctx.getBean("tenantContextHolder", ITenantContextHolder.class);
 		random = SecureRandom.getInstance("SHA1PRNG");
 		random.setSeed(System.currentTimeMillis());
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	/**
 	 * Test method for {@link org.os.javaee.orm.multitenancy.entity.CompositeMultiTenancyEnabledEntity} creation.
 	 * 
 	 * TODO --> Needs to fix this test case.
 	 */
	@Test
 	public final void createEntity() {
 		CompositeTenantInfo tenantInfo = new CompositeTenantInfo();
 		ITenantContext<CompositeTenantInfo> context = new CompositeTenantContext();
 		tenantInfo.setTenantIdOne(random.nextInt());
 		tenantInfo.setTenantIdTwo(random.nextInt());
 		tenantInfo.setTenantIdThree(random.nextInt());
 		context.setTenantInfo(tenantInfo);
 		tenantContextHolder.setTenantContext(context); 
 		CompositeMultiTenancyEnabledEntity entity = new CompositeMultiTenancyEnabledEntity();
 		entity.setName("Murali Reddy");
 		dao.save(entity);
 	}
 
 }
