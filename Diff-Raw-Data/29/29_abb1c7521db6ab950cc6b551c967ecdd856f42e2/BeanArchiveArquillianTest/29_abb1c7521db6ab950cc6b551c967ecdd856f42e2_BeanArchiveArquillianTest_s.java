 package de.lbe.sandbox.tomee;
 
 import javax.enterprise.inject.spi.BeanManager;
 import javax.inject.Inject;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.junit.Test;
 
 import com.zanox.lib.commons.arquillian.AbstractJUnit4ArquillianTest;
import com.zanox.lib.commons.lang.ClassLoaderUtils;
 import com.zanox.lib.commons.shrinkwrap.ShrinkWrapUtils;
 
 /**
  * @author lars.beuster
  */
 public class BeanArchiveArquillianTest extends AbstractJUnit4ArquillianTest {
 
 	@Inject
 	private BeanManager beanManager;
 
 	@Inject
 	private TestService testService;
 
 	/**
 	 * 
 	 */
 	@Deployment
 	public static JavaArchive deployment() {
 		JavaArchive archive = ShrinkWrapUtils.prepareCdiJar(TestService.class);
 		return archive;
 	}
 
 	/**
 	 * In contrast to JBoss we can use the complete test-classpath we we are inside the container. With JBoss we only have the module-classloader
 	 * without the test-classpath.
 	 * 
 	 * TomEE doesn't deploy Jars as BeanArchives (by default?).
 	 */
 	@Test
 	public void testTomEEDoesntRecognizeBeanArchives() throws Exception {
 		// System.out.println(ClassLoaderUtils.toString(getClass().getClassLoader()));
 		assertNull(this.beanManager);
 		assertNull(this.testService);
 	}
 
 	/**
 	 * 
 	 */
 	public static class TestService {
 		// no impl
 	}
 }
