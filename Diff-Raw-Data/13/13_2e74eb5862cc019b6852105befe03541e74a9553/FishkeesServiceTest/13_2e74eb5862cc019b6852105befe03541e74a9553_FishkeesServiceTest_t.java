 package com.fishkees.backend.service;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
import org.eclipse.jetty.servlets.CrossOriginFilter;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.fishkees.backend.configuration.FishkeesConfiguration;
 import com.fishkees.backend.modules.lists.resources.FlashcardListResource;
 import com.yammer.dropwizard.config.Bootstrap;
 import com.yammer.dropwizard.config.Environment;
 
 @RunWith(MockitoJUnitRunner.class)
 public class FishkeesServiceTest {
 	private FishkeesService testObj = new FishkeesService();
 	
 	@Mock
 	private FishkeesConfiguration configuration;
 	
 	@Mock
 	private Environment environment;
 	
 	@Mock
 	private Bootstrap<FishkeesConfiguration> bootstrap;
 	
 	@Test
 	public void testRun() throws Exception{
 		// when
 		testObj.run(configuration, environment);
 		
 		// then
		verify(environment).addFilter(eq(CrossOriginFilter.class), anyString());
 		verify(environment).addResource(any(FlashcardListResource.class));
 	}
 	
 	@Test
 	public void testInitialization() throws Exception {
 		// given
 		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
 		
 		// when
 		testObj.initialize(bootstrap);
 		
 		// then
 		verify(bootstrap).setName(argument.capture());
 		assertEquals("fishkees", argument.getValue());
 	}
 
 }
