 package org.lightmare;
 
 import java.net.URL;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.lightmare.deploy.MetaCreator;
 import org.lightmare.ejb.EjbConnector;
 import org.lightmare.injections.InjectMainBean;
 import org.lightmare.injections.InjectMainBeanRemote;
 
 public class InjectTest {
 
     private InjectMainBeanRemote bean;
 
     @BeforeClass
     public static void init() {
 
 	try {
 	    MetaCreator.Builder builder = new MetaCreator.Builder();
 	    MetaCreator creator = builder.build();
 	    Package pkg = InjectMainBean.class.getPackage();
 	    String resource = pkg.getName();
 	    resource = resource.replace('.', '/');
 	    System.out.println(resource);
 
 	    URL url = InjectMainBeanRemote.class.getClassLoader().getResource(
 		    resource);
 	    URL[] urls = { url };
 	    creator.scanForBeans(urls);
	} catch (Exception ex) {
 	    ex.printStackTrace();
 	}
     }
 
     @Before
     public void start() {
 
 	EjbConnector connector = new EjbConnector();
 	try {
 	    bean = connector.connectToBean("InjectMainBean",
 		    InjectMainBeanRemote.class);
 	} catch (Exception ex) {
 	    ex.printStackTrace();
 	}
     }
 
     @Test
     public void testSimpleInjection() {
 
 	String injectText = bean.getFromInjection();
 	Assert.assertTrue("Could not inject bean", injectText != null
 		&& !injectText.isEmpty());
 
 	System.out.println(injectText);
     }
 }
