 package com.jclarity.had_one_dismissal.jmx;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 public class JettyConfigurationEditorTest {
 	
 	private final JettyConfigurationEditor object = new JettyConfigurationEditor();
 	
 	@Test
 	public void readsMin() {
 		assertEquals(200, object.readMinThreadPoolSize());
 	}
 
 	@Test
 	public void readsMax() {
 		assertEquals(200, object.readMaxThreadPoolSize());
 	}
 
 	@Test
 	public void writesMin() {
		object.writeMinThreadPoolSize(5);
		assertEquals(5, new JettyConfigurationEditor().readMinThreadPoolSize());
 	}
 
 	@Test
 	public void writesMax() {
		object.writeMaxThreadPoolSize(100);
		assertEquals(100, new JettyConfigurationEditor().readMaxThreadPoolSize());
 	}
 
 }
