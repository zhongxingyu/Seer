 package biz.c24.io.fixml.sample.application;
 
 import biz.c24.io.api.data.ComplexDataObject;
 import biz.c24.io.fixml.sample.configuration.C24iOConfiguration;
 import biz.c24.io.fixml.sample.storage.MongoDbWriter;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mockito;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.springframework.integration.Message;
 import org.springframework.integration.channel.QueueChannel;
 import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.annotation.Resource;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.nullValue;
 import static org.hamcrest.core.IsNull.notNullValue;
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Mockito.*;
 
 /**
  * @author Enrico Musuruana
  * @since 03/04/2013
  */
 @ContextConfiguration(classes = {
         C24iOConfiguration.class,
         IntegrationTestConfiguration.class
 })
 @RunWith(SpringJUnit4ClassRunner.class)
 public class DbStoreFlowIntegrationTest {
 
     @Resource(name = "compass-store-channel-queue")
     private QueueChannel compassStoreChannel;
     @Resource(name = "file-reading-adapter")
     private SourcePollingChannelAdapter sourcePollingChannelAdapter;
     @Resource(name = "mongoDbFixMlCollectionWriter")
     private MongoDbWriter mongoDbWriter;
 
     public DbStoreFlowIntegrationTest() {
         System.setProperty("IO_HOME", "/Applications/C24");
     }
 
     @Before
     public void reset() {
         Mockito.reset(mongoDbWriter);
     }
 
     @Test
     public void testInputHandler() {
         
         when(mongoDbWriter.store((ComplexDataObject) anyObject())).thenAnswer(new Answer<Object>() {
             @Override
             public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                 return invocationOnMock.getArguments()[0];
             }
         });
         sourcePollingChannelAdapter.start();
         Message<?> message = compassStoreChannel.receive(5000);
        verify(mongoDbWriter, times(1)).store((ComplexDataObject) anyObject());
 
         assertThat(message, notNullValue());
         assertThat(message.getPayload(), notNullValue());
     }
 
     @Test
     public void testExceptionHandling() {
         
         when(mongoDbWriter.store((ComplexDataObject) anyObject())).thenThrow(IllegalArgumentException.class);
         sourcePollingChannelAdapter.start();
         Message<?> message = compassStoreChannel.receive(50);
        verify(mongoDbWriter, never()).store((ComplexDataObject) anyObject());
 
         assertThat(message, nullValue());
     }
 
    @Test
     public void typeHandlingStoreChannel() {
 
         when(mongoDbWriter.store((ComplexDataObject) anyObject())).thenAnswer(new Answer<Object>() {
             @Override
             public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                 return "Bad type";
             }
         });
         sourcePollingChannelAdapter.start();
         compassStoreChannel.receive(1000);
        verify(mongoDbWriter, never()).store((ComplexDataObject) anyObject());
     }
 }
