 package org.dilzio.ripphttp.util;
 
 import static org.junit.Assert.*;
 
 import java.io.InputStream;
 
 import org.dilzio.riphttp.util.ApplicationParams;
 import org.dilzio.riphttp.util.ParamEnum;
 import org.junit.Test;
 
 public class ApplicationParamsFactoryTest {
 
 	private static final String TESTFILE1 = "./src/test/resources/org/dilzio/rippinhttp/util/testprops1.properties";
 
 	@Test
 	public void hydrateWithParams() {
 		ApplicationParamsFactory _underTest = ApplicationParamsFactory.getInstance();
 		String path = TESTFILE1;
 		boolean overlayEnvVars = false;
 		ApplicationParams params = _underTest.newParams(path, overlayEnvVars);
 		assertEquals(12345, params.getIntParam(ParamEnum.LISTEN_PORT));
 		assertEquals(44, params.getIntParam(ParamEnum.WORKER_COUNT));
 		assertEquals(7998, params.getIntParam(ParamEnum.RING_BUFFER_SIZE));
 	}
 	
 	@Test
 	public void hydrateWithParamsOveralayTrue() {
 		ApplicationParamsFactory _underTest = ApplicationParamsFactory.getInstance();
 		String path = TESTFILE1;
 		boolean overlayEnvVars = true;
 		ApplicationParams params = _underTest.newParams(path, overlayEnvVars);
 		assertEquals(12345, params.getIntParam(ParamEnum.LISTEN_PORT));
 		assertEquals(44, params.getIntParam(ParamEnum.WORKER_COUNT));
 		assertEquals(7998, params.getIntParam(ParamEnum.RING_BUFFER_SIZE));
 	}
 	
 	@Test
 	public void hydrateWithParamsEnvOverride() {
 	    System.setProperty("LISTEN_PORT", "6666");
 		ApplicationParamsFactory _underTest = ApplicationParamsFactory.getInstance();
 		String path = TESTFILE1;
 		boolean overlayEnvVars = true;
 		ApplicationParams params = _underTest.newParams(path, overlayEnvVars);
 		assertEquals(6666, params.getIntParam(ParamEnum.LISTEN_PORT));
 		assertEquals(44, params.getIntParam(ParamEnum.WORKER_COUNT));
 		assertEquals(7998, params.getIntParam(ParamEnum.RING_BUFFER_SIZE));
 	}
 }
