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
		try {			
			object.writeMinThreadPoolSize(5);
			assertEquals(5, new JettyConfigurationEditor().readMinThreadPoolSize());
		} finally {			
			object.writeMinThreadPoolSize(200);
		}
 	}
 
 	@Test
 	public void writesMax() {
		try {			
			object.writeMaxThreadPoolSize(100);
			assertEquals(100, new JettyConfigurationEditor().readMaxThreadPoolSize());
		} finally {			
			object.writeMaxThreadPoolSize(200);
		}
 	}
 
 }
