 package org.shirdrn.easyrun.config;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.shirdrn.easyrun.common.MySequentialTaskExecutor;
 import org.shirdrn.easyrun.common.MySimpleTaskExecutor;
 import org.shirdrn.easyrun.common.PrintTaskExecutor;
 import org.shirdrn.easyrun.config.Configuration;
 import org.shirdrn.easyrun.config.DefaultTaskExecutorChainBuilder;
 import org.shirdrn.easyrun.config.TaskExecutorChainBuilder;
 import org.shirdrn.easyrun.utils.FactoryUtils;
 
 public class TestDefaultTaskExecutorChainBuilder {
 
 	Configuration configuration;
 	TaskExecutorChainBuilder builder;
 	
 	@Before
 	public void intialize() {
 		configuration = FactoryUtils.getDefaultConfiguration();
 		builder = new DefaultTaskExecutorChainBuilder(configuration);
 	}
 	
 	@Test
 	public void fireChain() {
 		builder
		.chain(MySequentialTaskExecutor.class)
		.chain(MySimpleTaskExecutor.class)
		.chain(PrintTaskExecutor.class)
		.chain(PrintTaskExecutor.class)
 		.chain(PrintTaskExecutor.class)
 		.fireChain();
 	}
 	
 	@After
 	public void destroy() {
 		FactoryUtils.closeAll();
 	}
 	
 	
 }
