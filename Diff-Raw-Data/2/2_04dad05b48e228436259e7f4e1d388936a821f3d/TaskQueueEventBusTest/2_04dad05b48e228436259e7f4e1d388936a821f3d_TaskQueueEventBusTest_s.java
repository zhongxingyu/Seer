 package com.clouway.asynctaskscheduler.gae;
 
 import com.clouway.asynctaskscheduler.common.ActionEvent;
 import com.clouway.asynctaskscheduler.common.CustomTaskQueueAsyncEvent;
 import com.clouway.asynctaskscheduler.common.DefaultActionEvent;
 import com.clouway.asynctaskscheduler.common.TaskQueueParamParser;
 import com.clouway.asynctaskscheduler.spi.AsyncEvent;
 import com.clouway.asynctaskscheduler.spi.AsyncEventBus;
 import com.clouway.asynctaskscheduler.util.FakeRequestScopeModule;
 import com.clouway.asynctaskscheduler.util.SimpleScope;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
 import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
 import com.google.gson.Gson;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.util.Modules;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.Date;
 import java.util.Map;
 
 import static junit.framework.Assert.assertEquals;
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author Mihail Lesikov (mlesikov@gmail.com)
  */
 public class TaskQueueEventBusTest {
 
   @Inject
   private AsyncEventBus eventBus;
 
   @Inject
   private Gson gson;
 
   private Injector injector;
 
 
   private LocalServiceTestHelper helper;
 
   private SimpleScope fakeRequestScope = new SimpleScope();
 
   @Before
   public void setUp() throws Exception {
 
     LocalTaskQueueTestConfig localTaskQueueTestConfig = new LocalTaskQueueTestConfig();
     localTaskQueueTestConfig.setQueueXmlPath("src/test/java/queue.xml");
     helper = new LocalServiceTestHelper(localTaskQueueTestConfig);
 
     helper.setUp();
     injector = Guice.createInjector(Modules.override(new BackgroundTasksModule()).with(new FakeRequestScopeModule(fakeRequestScope)));
     injector.injectMembers(this);
     fakeRequestScope.enter();
   }
 
   @After
   public void tearDown() {
     helper.tearDown();
     fakeRequestScope.exit();
   }
 
   @Test
   public void shouldAddTaskQueueToDefaultTaskQueueForExecutingHandlingTheFiredEvent() throws Exception {
     ActionEvent event = new ActionEvent("test");
     eventBus.fireEvent(event);
 
     QueueStateInfo defaultQueueStateInfo = getQueueStateInfo(QueueFactory.getDefaultQueue().getQueueName());
     assertEquals(1, defaultQueueStateInfo.getTaskInfo().size());
     assertEvent(defaultQueueStateInfo.getTaskInfo().get(0).getBody(), event);
   }
 
   @Test
   public void shouldAddTaskQueueToDefaultTaskQueueForExecutingHandlingTheFiredEventAfterDelay() throws Exception {
     ActionEvent event = new ActionEvent("test");
     Date start = new Date();
     eventBus.fireEvent(event, 1000l);
 
     QueueStateInfo defaultQueueStateInfo = getQueueStateInfo(QueueFactory.getDefaultQueue().getQueueName());
     assertEquals(1, defaultQueueStateInfo.getTaskInfo().size());
     assertEvent(defaultQueueStateInfo.getTaskInfo().get(0).getBody(), event);
     System.out.println(defaultQueueStateInfo.getTaskInfo().get(0).getEtaMillis() - start.getTime());
    assertTrue(defaultQueueStateInfo.getTaskInfo().get(0).getEtaMillis() - start.getTime() > 1000);
   }
 
   @Test
   public void shouldAddTaskInToDifferentTaskQueue() throws Exception {
 
     DefaultActionEvent event = new DefaultActionEvent("test");
     eventBus.fireEvent(event);
 
     QueueStateInfo defaultQueueStateInfo = getQueueStateInfo(QueueFactory.getDefaultQueue().getQueueName());
     assertEquals(0, defaultQueueStateInfo.getTaskInfo().size());
 
     QueueStateInfo customQueueStateInfo = getQueueStateInfo("customTaskQueue");
     assertEquals(1, customQueueStateInfo.getTaskInfo().size());
   }
 
   @Test
   public void shouldAddTaskInToDifferentTaskQueueWhenHandlerIsSetToDifferent() throws Exception {
 
     CustomTaskQueueAsyncEvent event = new CustomTaskQueueAsyncEvent("test");
     eventBus.fireEvent(event);
 
     QueueStateInfo defaultQueueStateInfo = getQueueStateInfo(QueueFactory.getDefaultQueue().getQueueName());
     assertEquals(0, defaultQueueStateInfo.getTaskInfo().size());
 
     QueueStateInfo customQueueStateInfo = getQueueStateInfo("customActionEventTaskQueue");
     assertEquals(1, customQueueStateInfo.getTaskInfo().size());
   }
 
   @Test
   public void shouldAddOnlyTaskQueueToDefaultTaskQueueForExecutingHandlingTheFiredEvent() throws Exception {
     ActionEvent event = new ActionEvent("test");
     eventBus.fireEvent(event);
 
     QueueStateInfo defaultQueueStateInfo = getQueueStateInfo(QueueFactory.getDefaultQueue().getQueueName());
     assertEquals(1, defaultQueueStateInfo.getTaskInfo().size());
     assertEvent(defaultQueueStateInfo.getTaskInfo().get(0).getBody(), event);
 
     eventBus.fireEvent(event);
 
     defaultQueueStateInfo = getQueueStateInfo(QueueFactory.getDefaultQueue().getQueueName());
     assertEquals(2, defaultQueueStateInfo.getTaskInfo().size());
     assertEvent(defaultQueueStateInfo.getTaskInfo().get(0).getBody(), event);
   }
 
 
   @Test
   public void shouldAddTaskQueueExecutingHandlingTheFiredEventThatContainsSpecialSymbols() throws Exception {
     ActionEvent event = new ActionEvent("test % test? *");
     eventBus.fireEvent(event);
 
     QueueStateInfo defaultQueueStateInfo = getQueueStateInfo(QueueFactory.getDefaultQueue().getQueueName());
     String taskQueueBody = defaultQueueStateInfo.getTaskInfo().get(0).getBody();
     assertEvent(taskQueueBody, event);
     String decodedEventValue = URLDecoder.decode(TaskQueueParamParser.parse(taskQueueBody).get(TaskQueueAsyncTaskScheduler.EVENT_AS_JSON),"UTF-8");
     assertThat(decodedEventValue, is(equalTo(gson.toJson(event))));
   }
 
   private void assertEvent(String taskQueueBody, AsyncEvent event) throws UnsupportedEncodingException {
     Map<String, String> params = TaskQueueParamParser.parse(taskQueueBody);
     assertEquals(params.get(TaskQueueAsyncTaskScheduler.EVENT), event.getClass().getName());
     assertEquals(params.get(TaskQueueAsyncTaskScheduler.EVENT_AS_JSON), encode(gson.toJson(event)));
   }
 
   private String encode(String value) throws UnsupportedEncodingException {
      return URLEncoder.encode(value,"UTF-8");
    }
 
   private QueueStateInfo getQueueStateInfo(String queueName) {
     LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
     return ltq.getQueueStateInfo().get(queueName);
   }
 
 }
