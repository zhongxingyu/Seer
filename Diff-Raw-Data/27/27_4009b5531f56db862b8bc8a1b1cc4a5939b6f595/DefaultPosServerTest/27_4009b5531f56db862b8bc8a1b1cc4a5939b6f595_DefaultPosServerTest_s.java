 package com.chinarewards.qqgbpvn.main;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.apache.commons.configuration.BaseConfiguration;
 import org.apache.commons.configuration.Configuration;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.chinarewards.qqgbpvn.main.test.GuiceTest;
 import com.chinarewards.qqgbvpn.core.jpa.JpaPersistModuleBuilder;
 import com.chinarewards.qqgbvpn.main.PosServer;
 import com.chinarewards.qqgbvpn.main.ServerModule;
 import com.chinarewards.qqgbvpn.main.guice.AppModule;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceHandlerModule;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceMapping;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceMappingConfigBuilder;
 import com.chinarewards.qqgbvpn.main.protocol.guice.ServiceHandlerGuiceModule;
 import com.google.inject.Module;
 import com.google.inject.persist.jpa.JpaPersistModule;
 import com.google.inject.util.Modules;
 
 /**
  * 
  * 
  * @author Cyril
  * @since 0.1.0
  */
 public class DefaultPosServerTest extends GuiceTest {
 
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.chinarewards.qqgpvn.main.test.GuiceTest#getModules()
 	 */
 	@Override
 	protected Module[] getModules() {
 		
 		CommonTestConfigModule confModule = new CommonTestConfigModule();
 
 		ServiceMappingConfigBuilder mappingBuilder = new ServiceMappingConfigBuilder();
 		ServiceMapping mapping = mappingBuilder.buildMapping(confModule
 				.getConfiguration());
 
 		// build the Guice modules.
 		Module[] modules = new Module[] {
 				new CommonTestConfigModule(),
 				buildPersistModule(confModule.getConfiguration()),
 				new ServerModule(),
 				new AppModule(),
 				Modules.override(
 						new ServiceHandlerModule(confModule.getConfiguration()))
 						.with(new ServiceHandlerGuiceModule(mapping)) };
 
 		return modules;
 	}
 
 	protected Module buildTestConfigModule() {
 
 		Configuration conf = new BaseConfiguration();
 		// hard-coded config
 		conf.setProperty("server.port", 0);
 		// persistence
 		conf.setProperty("db.user", "sa");
 		conf.setProperty("db.password", "");
 		conf.setProperty("db.driver", "org.hsqldb.jdbcDriver");
 		conf.setProperty("db.url", "jdbc:hsqldb:.");
 		// additional Hibernate properties
 		conf.setProperty("db.ext.hibernate.dialect",
 				"org.hibernate.dialect.HSQLDialect");
 		conf.setProperty("db.ext.hibernate.show_sql", true);
 		// URL for QQ
 		conf.setProperty("qq.groupbuy.url.groupBuyingSearchGroupon",
 				"http://localhost:8086/qqapi");
 		conf.setProperty("qq.groupbuy.url.groupBuyingValidationUrl",
 				"http://localhost:8086/qqapi");
 		conf.setProperty("qq.groupbuy.url.groupBuyingUnbindPosUrl",
 				"http://localhost:8086/qqapi");
 
 		TestConfigModule confModule = new TestConfigModule(conf);
 		return confModule;
 	}
 
 	protected Module buildPersistModule(Configuration config) {
 
 		JpaPersistModule jpaModule = new JpaPersistModule("posnet");
 		// config it.
 
 		JpaPersistModuleBuilder b = new JpaPersistModuleBuilder();
 		b.configModule(jpaModule, config, "db");
 
 		return jpaModule;
 	}
 
 	@Test
 	public void testStart() throws Exception {
 
 		// force changing of configuration
 		Configuration conf = getInjector().getInstance(Configuration.class);
 		conf.setProperty("server.port", 0);
 
 		// get an new instance of PosServer
 		PosServer server = getInjector().getInstance(PosServer.class);
 		// make sure it is started, and port is correct
 		assertTrue(server.isStopped());
 		//
 		// start it!
 		server.start();
 		int runningPort = server.getLocalPort();
 		// stop it.
 		server.stop();
 		assertTrue(server.isStopped());
 
 		//
 		// Now we know which free port to use.
 		//
 		// XXX it is a bit risky since the port maybe in use by another
 		// process.
 		//
 
 		// get an new instance of PosServer
 		conf.setProperty("server.port", runningPort);
 
 		// make sure it is stopped
 		assertTrue(server.isStopped());
 
 		// start it!
 		server.start();
 
 		// make sure it is started, and port is correct
 		assertFalse(server.isStopped());
 		assertEquals(runningPort, server.getLocalPort());
 
 		// sleep for a while...
 		Thread.sleep(500); // 0.5 seconds
 
 		// stop it, and make sure it is stopped.
 		server.stop();
 		assertTrue(server.isStopped());
 
 		log.info("Server stopped");
 
 	}
 
 	public void testStart_RandomPort() throws Exception {
 
 		// force changing of configuration
 		Configuration conf = getInjector().getInstance(Configuration.class);
 		conf.setProperty("server.port", 0);
 
 		// get an new instance of PosServer
 		PosServer server = getInjector().getInstance(PosServer.class);
 
 		// make sure it is stopped
 		assertTrue(server.isStopped());
 
 		// start it!
 		server.start();
 
 		// make sure it is started, and port is correct
 		assertFalse(server.isStopped());
 		assertTrue(0 != server.getLocalPort());
 		assertTrue(server.getLocalPort() > 0);
 
 		// sleep for a while...
 		Thread.sleep(500); // 0.5 seconds
 
 		// stop it, and make sure it is stopped.
 		server.stop();
 		assertTrue(server.isStopped());
 
 		log.info("Server stopped");
 
 	}
 
 }
