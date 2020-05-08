 package org.zend.sdk.test.sdkcli.commands;
 
 import static org.mockito.Mockito.doReturn;
 import static org.mockito.Mockito.spy;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Random;
 
 import org.junit.After;
 import org.junit.Before;
 import org.zend.sdk.test.AbstractTest;
 import org.zend.sdklib.internal.target.UserBasedTargetLoader;
 import org.zend.sdklib.internal.target.ZendTarget;
 import org.zend.sdklib.manager.TargetsManager;
 import org.zend.sdklib.target.ITargetLoader;
 import org.zend.sdklib.target.IZendTarget;
 import org.zend.webapi.core.WebApiException;
 
 public class AbstractTargetCommandTest extends AbstractTest {
 
 	private ITargetLoader loader;
 	protected TargetsManager manager;
 	private File file;
 
 	@Before
 	public void startup() {
 		final String tempDir = System.getProperty("java.io.tmpdir");
 		file = new File(tempDir + File.separator + new Random().nextInt());
 		file.mkdir();
 		loader = new UserBasedTargetLoader(file);
 		manager = spy(new TargetsManager(loader));
 	}
 
 	@After
 	public void shutdown() {
 		file.deleteOnExit();
 	}
 
 	protected IZendTarget getTarget() throws WebApiException {
 		IZendTarget target = null;
 		try {
			target = spy(new ZendTarget("dev4", new URL("http://localhost"),
 					"mykey", "123456"));
 			doReturn(true).when(target).connect();
 		} catch (MalformedURLException e) {
 			// ignore
 		}
 		return target;
 	}
 
 }
