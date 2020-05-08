 package com.inertia.solutions.threading.task.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.concurrent.Future;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.inertia.solutions.threading.bean.ThreadResultBean;
 import com.inertia.solutions.threading.random.SleepRandomizer;
 
 
 @RunWith(MockitoJUnitRunner.class)
 public class ThreadingTaskImplTest {
 
 	private static final Long SLEEP_TIME = new Long(1111);
 
 	@Mock
 	SleepRandomizer mockSleepRandomizer;
 	
 	@InjectMocks
 	ThreadingTaskImpl taskUnderTest;
 	
 	@Before
 	public void setup() {
 		Mockito.when(mockSleepRandomizer.randomize()).thenReturn(SLEEP_TIME);
 	}
 	
 	@Test
 	public void shouldReturnDataAfterLogging() throws Exception {
 		Future<ThreadResultBean> result = taskUnderTest.executeLogging(new Integer(1));
 		ThreadResultBean resultBean = result.get();
 		assertNotNull(resultBean);
 		assertNotNull(resultBean.getDateAfterExecution());
 		assertNotNull(resultBean.getDateBeforeExecution());
 		assertEquals(new Integer(1), resultBean.getTaskCountId());
 		assertEquals(SLEEP_TIME, resultBean.getSleepTime());
 	}
 
 }
