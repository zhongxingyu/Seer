 package de.pellepelster.myadmin.demo.server.test;
 
 import org.junit.BeforeClass;
 import org.springframework.test.context.ContextConfiguration;
 
 import de.pellepelster.myadmin.server.test.base.BaseJndiContextTest;
 
@ContextConfiguration(locations = { "classpath:/DemoServerApplicationContext-gen.xml", "classpath:/DemoServerApplicationContextServices-gen.xml",
		"classpath:/DemoDB-gen.xml" })
 public abstract class BaseDemoTest extends BaseJndiContextTest
 {
 
 	@BeforeClass
 	public static void initJndi() throws Exception
 	{
 		initJndi("Demo");
 	}
 
 }
