 package net.northfuse.resources.config;
 
 import net.northfuse.resources.ResourceHandler;
 import org.junit.Test;
import org.junit.Assert;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
 
import java.util.Map;

 /**
  * @author tylers2
  */
 public class ResourceDefinitionParserUnitTest {
 
 	@Test
 	public void checkNoDebugSet() {
 		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
 		context.setConfigLocation("classpath:/net/northfuse/resources/config/checkNoDebugSet-context.xml");
 		context.refresh();
 
 		ResourceHandler resourceHandler = context.getBean(ResourceHandler.class);
 		Assert.assertNotNull(resourceHandler);
 		Assert.assertFalse("By default, a resource is not in debug mode", resourceHandler.isDebug());
 	}
 
 	@Test
 	public void checkDefaultInResourcesElement() {
 		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
 		context.setConfigLocation("classpath:/net/northfuse/resources/config/defaultDebug-context.xml");
 		context.refresh();
 
 		ResourceHandler resourceHandler = context.getBean(ResourceHandler.class);
 		Assert.assertNotNull(resourceHandler);
 		Assert.assertTrue("The debug value for all scripts/sources in the resource block is true, unless overridden", resourceHandler.isDebug());
 		Assert.assertEquals("/test/test1.js", resourceHandler.getMapping());
 
 		SimpleUrlHandlerMapping mapping = context.getBean(SimpleUrlHandlerMapping.class);
 		Assert.assertNotNull(mapping);
 		Assert.assertSame(25, mapping.getOrder());
 	}
 
 	@Test
 	public void checkGlobalDebugELExpression() {
 		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
 		context.setConfigLocation("classpath:/net/northfuse/resources/config/globalDebug-elExpression-context.xml");
 		context.refresh();
 
 		ResourceHandler resourceHandler = context.getBean(ResourceHandler.class);
 		Assert.assertNotNull(resourceHandler);
 		Assert.assertTrue("The debug value for all scripts/sources in the resource block is " + IN_DEBUG_MODE + ", unless overridden", resourceHandler.isDebug());
 		Assert.assertEquals("/test/test1.js", resourceHandler.getMapping());
 
 		SimpleUrlHandlerMapping mapping = context.getBean(SimpleUrlHandlerMapping.class);
 		Assert.assertNotNull(mapping);
 		Assert.assertSame(25, mapping.getOrder());

		Map<String, ?> mappings = mapping.getUrlMap();
		Assert.assertNotNull(mappings);
		Assert.assertEquals(1, mappings.size());
		Assert.assertEquals("/test/test1.js", mappings.keySet().iterator().next());
 	}
 
 	public static boolean IN_DEBUG_MODE = true;
 }
