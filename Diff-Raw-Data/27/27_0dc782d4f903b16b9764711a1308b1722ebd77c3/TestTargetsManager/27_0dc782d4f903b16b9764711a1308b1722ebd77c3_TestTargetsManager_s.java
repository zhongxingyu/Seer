 package org.zend.sdk.test.sdklib.manager;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.doReturn;
 import static org.mockito.Mockito.doThrow;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Random;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.zend.sdk.test.AbstractTest;
 import org.zend.sdklib.internal.target.UserBasedTargetLoader;
 import org.zend.sdklib.internal.target.ZendTarget;
 import org.zend.sdklib.manager.TargetsManager;
 import org.zend.sdklib.target.ITargetLoader;
 import org.zend.sdklib.target.IZendTarget;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.internal.core.connection.auth.signature.SignatureException;
 
 public class TestTargetsManager extends AbstractTest {
 
 	private ITargetLoader loader;
 	private File file;
 
 	@Before
 	public void startup() {
 		final String tempDir = System.getProperty("java.io.tmpdir");
 		file = new File(tempDir + File.separator + new Random().nextInt());
 		file.mkdir();
 		loader = new UserBasedTargetLoader(file);
 	}
 
 	@After
 	public void shutdown() {
 		loader = null;
 		file.deleteOnExit();
 	}
 
	@Test(expected = IllegalArgumentException.class)
 	public void testCreateManagerWithInvalidTarget() throws WebApiException,
 			MalformedURLException {
 		ITargetLoader loader = spy(new UserBasedTargetLoader(file));
 		when(loader.loadAll()).thenReturn(
 				new IZendTarget[] { new ZendTarget(null, new URL(
 						"http://localhost"), "mykey", "43543") });
		new TargetsManager(loader);
 	}
 
 	@Test
 	public void testCreateManagerWithValidTarget() throws WebApiException,
 			MalformedURLException {
 		ITargetLoader loader = spy(new UserBasedTargetLoader(file));
 		when(loader.loadAll()).thenReturn(
 				new IZendTarget[] { new ZendTarget("dev3", new URL(
 						"http://localhost"), "mykey", "43543") });
 		TargetsManager manager = new TargetsManager(loader);
 		assertTrue(manager.list().length == 1);
 	}
 
 	@Test
 	public void testAddTarget() throws WebApiException {
 		TargetsManager manager = new TargetsManager(loader);
 		IZendTarget target = getTarget();
 		manager.add(target);
 		assertTrue(manager.list().length == 1);
 	}
 
	@Test(expected = IllegalArgumentException.class)
 	public void testNullIdAddTarget() throws WebApiException,
 			MalformedURLException {
 		TargetsManager manager = new TargetsManager(loader);
 		IZendTarget target = spy(new ZendTarget(null, new URL(
 				"http://localhost:10081"), "mykey", "43543"));
 		doReturn(true).when(target).connect();
 		manager.add(target);
		assertTrue(manager.list().length == 1);
 	}
 
 	@Test
 	public void testAddDisconnectedTarget() throws WebApiException {
 		TargetsManager manager = new TargetsManager(loader);
 		IZendTarget target = getTarget();
 		when(target.connect()).thenReturn(false);
 		assertNull(manager.add(target));
 	}
 
	@Test(expected = IllegalArgumentException.class)
 	public void testAddDuplicatedTarget() throws WebApiException {
 		TargetsManager manager = new TargetsManager(loader);
 		IZendTarget target = getTarget();
 		manager.add(target);
 		manager.add(target);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testAddNullTarget() throws WebApiException {
 		TargetsManager manager = new TargetsManager(loader);
 		manager.add(null);
 	}
 
 	@Test
 	public void testRemoveTarget() throws WebApiException {
 		TargetsManager manager = new TargetsManager(loader);
 		IZendTarget target = getTarget();
 		manager.add(target);
 		assertTrue(manager.list().length == 1);
 		manager.remove(target);
 		assertTrue(manager.list().length == 0);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testRemoveNullTarget() throws WebApiException {
 		TargetsManager manager = new TargetsManager(loader);
 		manager.remove(null);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testRemoveNotAddedTarget() throws WebApiException {
 		TargetsManager manager = new TargetsManager(loader);
 		IZendTarget target = getTarget();
 		manager.remove(target);
 	}
 
 	@Test
 	public void testGetTargetById() throws WebApiException {
 		TargetsManager manager = new TargetsManager(loader);
 		IZendTarget target = getTarget();
 		manager.add(target);
 		assertTrue(manager.list().length == 1);
 		assertSame(target, manager.getTargetById(target.getId()));
 	}
 
 	@Test
 	public void testGetNullTargetById() throws WebApiException {
 		TargetsManager manager = new TargetsManager(loader);
 		assertNull(manager.getTargetById(null));
 	}
 
 	@Test
 	public void testGetNotAddedTargetById() throws WebApiException {
 		TargetsManager manager = new TargetsManager(loader);
 		assertNull(manager.getTargetById("0"));
 	}
 
 	@Test
 	public void testDetectLocalhost() throws WebApiException, IOException {
 		TargetsManager manager = new TargetsManager(loader);
 		IZendTarget target = getTarget();
 		manager.add(target);
 		assertTrue(manager.list().length == 1);
 		assertNotNull(manager.detectLocalhostTarget(target.getId(),
 				target.getKey()));
 	}
 
 	@Test
 	public void testDetectLocalhostNoId() throws WebApiException, IOException {
 		TargetsManager manager = new TargetsManager(loader);
 		IZendTarget target = getTarget();
 		manager.add(target);
 		assertTrue(manager.list().length == 1);
 		assertNotNull(manager.detectLocalhostTarget(target.getKey()));
 	}
 
 	@Test
 	public void testDetectLocalhostNoArgs() throws WebApiException, IOException {
 		TargetsManager manager = new TargetsManager(loader);
 		IZendTarget target = getTarget();
 		manager.add(target);
 		assertTrue(manager.list().length == 1);
 		assertNotNull(manager.detectLocalhostTarget());
 	}
 
 	@Test
 	public void testDetectLocalhostNoTarget() throws IOException {
 		TargetsManager manager = new TargetsManager(loader);
 		assertNotNull(manager.detectLocalhostTarget());
 	}
 
 	@Test
 	public void testDetectLocalhostNoTargetThrowsWebApiException()
 			throws IOException, WebApiException {
 		TargetsManager manager = spy(new TargetsManager(loader));
 		doThrow(new SignatureException("testError")).when(manager).add(
 				any(IZendTarget.class));
 		assertNull(manager.detectLocalhostTarget());
 	}
 
 	@Test
 	public void testCreateTarget() throws WebApiException {
 		TargetsManager manager = spy(new TargetsManager(loader));
 		doReturn(getTarget()).when(manager).add(any(IZendTarget.class));
 		assertNotNull(manager.createTarget("1", "http://localhost", "mykey",
 				"43543"));
 	}
 
 	@Test
 	public void testCreateTargetNoId() throws WebApiException {
 		TargetsManager manager = spy(new TargetsManager(loader));
 		doReturn(getTarget()).when(manager).add(any(IZendTarget.class));
 		assertNotNull(manager
 				.createTarget("http://localhost", "mykey", "43543"));
 	}
 
 	@Test
 	public void testCreateTargetFailAdd() throws WebApiException {
 		TargetsManager manager = spy(new TargetsManager(loader));
 		doReturn(null).when(manager).add(any(IZendTarget.class));
 		assertNull(manager.createTarget("1", "http://localhost", "mykey",
 				"43543"));
 	}
 
 	@Test
 	public void testCreateTargetInvalidUrl() throws WebApiException {
 		TargetsManager manager = spy(new TargetsManager(loader));
 		doReturn(null).when(manager).add(any(IZendTarget.class));
 		assertNull(manager.createTarget("1", "aaa11://localhost", "mykey",
 				"43543"));
 	}
 
 	@Test
 	public void testCreateTargetAddThrowsException() throws WebApiException {
 		TargetsManager manager = spy(new TargetsManager(loader));
 		Mockito.doThrow(new SignatureException("testError")).when(manager)
 				.add(any(IZendTarget.class));
 		assertNull(manager.createTarget("1", "http://localhost", "mykey",
 				"43543"));
 	}
 
 	private IZendTarget getTarget() throws WebApiException {
 		IZendTarget target = null;
 		try {
 			target = spy(new ZendTarget("dev4", new URL("http://localhost"),
 					"mykey", "43543"));
 			doReturn(true).when(target).connect();
 		} catch (MalformedURLException e) {
 			// ignore
 		}
 		return target;
 	}
 
 }
