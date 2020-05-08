 package com.chinarewards.qqgbvpn.main.jmx;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.apache.commons.configuration.Configuration;
 import org.junit.After;
 import org.junit.Before;
 
 import com.chinarewards.qqgbpvn.main.CommonTestConfigModule;
 import com.chinarewards.qqgbpvn.main.test.GuiceTest;
 import com.chinarewards.qqgbvpn.common.Tools;
 import com.chinarewards.qqgbvpn.core.jpa.JpaPersistModuleBuilder;
 import com.chinarewards.qqgbvpn.domain.Agent;
 import com.chinarewards.qqgbvpn.domain.Pos;
 import com.chinarewards.qqgbvpn.domain.PosAssignment;
 import com.chinarewards.qqgbvpn.domain.status.PosDeliveryStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosInitializationStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosOperationStatus;
 import com.chinarewards.qqgbvpn.main.ApplicationModule;
 import com.chinarewards.qqgbvpn.main.PosServer;
 import com.chinarewards.qqgbvpn.main.ServerModule;
 import com.chinarewards.qqgbvpn.main.guice.AppModule;
 import com.chinarewards.qqgbvpn.main.impl.DefaultPosServer;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceHandlerModule;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceMapping;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceMappingConfigBuilder;
 import com.chinarewards.qqgbvpn.main.protocol.guice.ServiceHandlerGuiceModule;
 import com.chinarewards.qqgbvpn.main.util.HMAC_MD5;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.persist.jpa.JpaPersistModule;
 import com.google.inject.util.Modules;
 
 /**
  * 
  * @author yanxin
  * 
  */
 public abstract class JmxBaseTest extends GuiceTest {
 
 	protected EntityManager em;
 	protected long runForSeconds;
 	protected PosServer posServer;
 	protected int port;
 
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 		port = startServer();
 		runForSeconds = 0;
 
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		super.tearDown();
 		if (em != null && em.getTransaction().isActive()) {
 			em.getTransaction().rollback();
 		}
 		stopServer();
 		resetDb();
 	}
 
 	private void resetDb() {
 		// clear database
 		em.getTransaction().begin();
 		em.createQuery("DELETE FROM Journal").executeUpdate();
 		em.getTransaction().commit();
 	}
 
 	private int startServer() throws Exception {
 
 		// force changing of configuration
 		Configuration conf = getInjector().getInstance(Configuration.class);
 		conf.setProperty("server.port", 0);
 
 		// get an new instance of PosServer
 		posServer = getInjector().getInstance(PosServer.class);
 		// make sure it is started, and port is correct
 		assertTrue(posServer.isStopped());
 		//
 		// start it!
 		posServer.start();
 		int runningPort = posServer.getLocalPort();
 		// stop it.
 		posServer.stop();
 		assertTrue(posServer.isStopped());
 
 		// XXX we insert data here
 		{
 			DefaultPosServer dserver = (DefaultPosServer) posServer;
 			Injector injector = dserver.getInjector();
 
 			em = injector.getInstance(EntityManager.class);
 			em.getTransaction().begin();
 			initDB(em);
 		}
 
 		//
 		// Now we know which free port to use.
 		//
 		// XXX it is a bit risky since the port maybe in use by another
 		// process.
 		//
 
 		// get an new instance of PosServer
 		conf.setProperty("server.port", runningPort);
 
 		// make sure it is stopped
 		assertTrue(posServer.isStopped());
 
 		// start it!
 		posServer.start();
 
 		// make sure it is started, and port is correct
 		assertFalse(posServer.isStopped());
 		assertEquals(runningPort, posServer.getLocalPort());
 		return posServer.getLocalPort();
 		// sleep for a while...
 	}
 
 	private void stopServer() throws Exception {
 		// stop it, and make sure it is stopped.
 		posServer.stop();
 		assertTrue(posServer.isStopped());
 		log.info("posServer stopped");
 	}
 
 	@Override
 	protected Module[] getModules() {
 
 		CommonTestConfigModule confModule = new CommonTestConfigModule();
 		ServiceMappingConfigBuilder mappingBuilder = new ServiceMappingConfigBuilder();
 		ServiceMapping mapping = mappingBuilder.buildMapping(confModule
 				.getConfiguration());
 
 		// build the Guice modules.
 		Module[] modules = new Module[] {
 				new ApplicationModule(),
 				new CommonTestConfigModule(),
 				buildPersistModule(confModule.getConfiguration()),
 				new ServerModule(),
 				new AppModule(),
 				Modules.override(
 						new ServiceHandlerModule(confModule.getConfiguration()))
 						.with(new ServiceHandlerGuiceModule(mapping)) };
 
 		return modules;
 	}
 
 	protected Module buildPersistModule(Configuration config) {
 
 		JpaPersistModule jpaModule = new JpaPersistModule("posnet");
 		// config it.
 
 		JpaPersistModuleBuilder b = new JpaPersistModuleBuilder();
 		b.configModule(jpaModule, config, "db");
 
 		return jpaModule;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void initDB(EntityManager em) {
 		// need override in child class.
 
 		String posId1 = "REWARDS-0003";
 		String posId2 = "REWARDS-0002";
 		String agentName = "agentName";
 
 		Pos pos = new Pos();
 		pos.setPosId(posId1);
 		pos.setDstatus(PosDeliveryStatus.DELIVERED);
 		pos.setSecret("000001");
 		pos.setIstatus(PosInitializationStatus.INITED);
 		pos.setOstatus(PosOperationStatus.ALLOWED);
 		em.persist(pos);
 		Pos pos2 = new Pos();
 		pos2.setPosId(posId2);
 		pos2.setDstatus(PosDeliveryStatus.DELIVERED);
 		pos2.setSecret("000001");
 		pos2.setIstatus(PosInitializationStatus.INITED);
 		pos2.setOstatus(PosOperationStatus.ALLOWED);
 		em.persist(pos2);
 		Agent agent = new Agent();
 		agent.setName(agentName);
 		em.persist(agent);
 		PosAssignment pa = new PosAssignment();
 		pa.setAgent(agent);
 		pa.setPos(pos);
 		em.persist(pa);
 		PosAssignment pa2 = new PosAssignment();
 		pa2.setAgent(agent);
 		pa2.setPos(pos2);
 		em.persist(pa2);
 
 		List<Pos> pp = em.createQuery(" from Pos").getResultList();
 		log.debug("pp={}", pp);
 		if (pp != null) {
 			for (Pos p : pp) {
 				log.debug("getPosId : {}", p.getPosId());
 			}
 		}
 	}
 
 	protected void oldPosInit(OutputStream os, InputStream is, byte[] challenge)
 			throws Exception {
 		byte[] msg = new byte[] {
 				// SEQ
 				0, 0, 0, 24,
 				// ACK
 				0, 0, 0, 0,
 				// flags
 				0, 0,
 				// checksum
 				0, 0,
 				// message length
 				0, 0, 0, 32,
 				// command ID
 				0, 0, 0, 5,
 				// POS ID
 				'R', 'E', 'W', 'A', 'R', 'D', 'S', '-', '0', '0', '0', '3' };
 
 		// calculate checksum
 		int checksum = Tools.checkSum(msg, msg.length);
 		Tools.putUnsignedShort(msg, checksum, 10);
 
 		// send both message at once
 		byte[] outBuf = new byte[msg.length];
 		System.arraycopy(msg, 0, outBuf, 0, msg.length);
 
 		// write response
 		log.info(" Init Send request to server");
 		os.write(outBuf);
 
 		// ----------
 
 		// session.write("Client First Message");
 		Thread.sleep(runForSeconds * 1000);
 		// read
 		log.info("Read response");
 		byte[] response = new byte[30];
 		int n = is.read(response);
 		System.out.println("Number of bytes init read: " + n);
 		for (int i = 0; i < n; i++) {
 			String s = Integer.toHexString((byte) response[i]);
 			if (s.length() < 2)
 				s = "0" + s;
 			if (s.length() > 2)
 				s = s.substring(s.length() - 2);
 			System.out.print(s + " ");
 			if ((i + 1) % 8 == 0)
 				System.out.println("");
 		}
 		System.arraycopy(response, 22, challenge, 0, 8);
 		assertEquals(0, response[20]);
 		assertEquals(0, response[21]);
 	}
 
 	protected void oldPosLogin(OutputStream os, InputStream is, byte[] challenge)
 			throws Exception {
 		byte[] msg = new byte[] {
 				// SEQ
 				0, 0, 0, 25,
 				// ACK
 				0, 0, 0, 0,
 				// flags
 				0, 0,
 				// checksum
 				0, 0,
 				// message length
 				0, 0, 0, 48,
 				// command ID
 				0, 0, 0, 7,
 				// POS ID
 				'R', 'E', 'W', 'A', 'R', 'D', 'S', '-', '0', '0', '0', '3',
 				// challengeResponse
 				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
 
 		byte[] content2 = HMAC_MD5.getSecretContent(challenge, "000001");
 		Tools.putBytes(msg, content2, 32);
 		int checksum = Tools.checkSum(msg, msg.length);
 		Tools.putUnsignedShort(msg, checksum, 10);
 		// System.out.println("--------------------");
 		// for (int i = 0; i < msg.length; i++) {
 		// String s = Integer.toHexString((byte) msg[i]);
 		// if (s.length() < 2)
 		// s = "0" + s;
 		// if (s.length() > 2)
 		// s = s.substring(s.length() - 2);
 		// System.out.print(s + " ");
 		// if ((i + 1) % 8 == 0)
 		// System.out.println("");
 		// }
 		// System.out.println("--------------------");
 		// send both message at once
 		byte[] outBuf = new byte[msg.length];
 		System.arraycopy(msg, 0, outBuf, 0, msg.length);
 
 		log.info(" Login Send request to server");
 		os.write(outBuf);
 		// ----------
 
 		Thread.sleep(runForSeconds * 1000);
 		// read
 		log.info("Read response");
 		byte[] response = new byte[30];
 		int n = is.read(response);
 		System.out.println("Number of bytes login read2: " + n);
 		for (int i = 0; i < n; i++) {
 			String s = Integer.toHexString((byte) response[i]);
 			if (s.length() < 2)
 				s = "0" + s;
 			if (s.length() > 2)
 				s = s.substring(s.length() - 2);
 			System.out.print(s + " ");
 			if ((i + 1) % 8 == 0)
 				System.out.println("");
 		}
 		assertEquals(0, response[20]);
 		assertEquals(0, response[21]);
 
 	}
 
 	private int getDefaultResponseHeaderLength() {
 		return 16;
 	}
 
 	protected Charset getDefaultCharset() {
 		return Charset.forName("GB2312");
 	}
 
 	protected EntityManager getEm() {
 		return em;
 	}
 
 	/**
 	 * Send byte array message ,return message body byte array.
 	 * 
 	 * @param os
 	 * @param is
 	 * @param sendMsg
 	 * @return
 	 * @throws Exception
 	 */
 	protected byte[] sendMessage(OutputStream os, InputStream is, byte[] sendMsg)
 			throws Exception {
 		os.write(sendMsg);
 		// ----------
 		Thread.sleep(runForSeconds * 500);
 		byte[] response = new byte[500];
 		int respLen = is.read(response);
 
 		log.debug("return byte size:{}", respLen);
 		printToHex(response, respLen);
 
 		// check
 		int respHeaderLen = getDefaultResponseHeaderLength();
 		int respBodyLen = respLen - respHeaderLen;
 		byte[] respBody = new byte[respBodyLen];
 		System.arraycopy(response, respHeaderLen, respBody, 0, respBodyLen);
 
 		return respBody;
 	}
 
 	protected void printToHex(byte[] response, int n) {
 		for (int i = 0; i < n; i++) {
 			String s = Integer.toHexString((byte) response[i]);
 			if (s.length() < 2)
 				s = "0" + s;
 			if (s.length() > 2)
 				s = s.substring(s.length() - 2);
 			System.out.print(s + " ");
 			if ((i + 1) % 8 == 0)
 				System.out.println("");
 		}
 	}
 }
