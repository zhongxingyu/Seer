 package de.ronnyfriedland.time.config;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class MessageTest {
 
	private static final String[] PROPERTIES = new String[] { "test1", "test2" };
 
	@Test
	public void testMessageProperties() {
		for (Messages message : Messages.values()) {
			Assert.assertFalse(message.getMessage(PROPERTIES).startsWith("!"));
			Assert.assertFalse(message.getMessage(PROPERTIES).endsWith("!"));
		}
 
	}
 }
