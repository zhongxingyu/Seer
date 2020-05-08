 package ch.ethz.iks.slp.test;
 
 import java.util.Dictionary;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 import ch.ethz.iks.slp.ServiceLocationException;
 import ch.ethz.iks.slp.ServiceURL;
 
 public abstract class SLPTestCase extends TestCase {
 
 	protected static final String HOST_AND_PORT = System.getProperty("net.slp.tests.hostAndPort", "gantenbein:123");
 	protected ServiceURL service;
 	protected Dictionary properties;
 
 	public void tearDown() throws InterruptedException {
 		try {
 			if(service != null) {
 				TestActivator.advertiser.deregister(service);
 			}
 		} catch (ServiceLocationException e) {
 			Assert.fail(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 }
