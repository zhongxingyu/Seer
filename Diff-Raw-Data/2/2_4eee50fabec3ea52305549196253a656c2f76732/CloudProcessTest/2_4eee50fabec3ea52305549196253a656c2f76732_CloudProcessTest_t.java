 package n3phele.process;
 /**
  * (C) Copyright 2010-2013. Nigel Cook. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * 
  * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
  * except in compliance with the License. 
  * 
  *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
  *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  *  specific language governing permissions and limitations under the License.
  */
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.SecurityContext;
 
 import n3phele.service.actions.CountDownAction;
 import n3phele.service.actions.JobAction;
 import n3phele.service.lifecycle.ProcessLifecycle;
 import n3phele.service.model.Action;
 import n3phele.service.model.ActionState;
 import n3phele.service.model.CloudProcess;
 import n3phele.service.model.Context;
 import n3phele.service.model.core.User;
 import n3phele.service.rest.impl.ActionResource;
 import n3phele.service.rest.impl.CloudProcessResource;
 import n3phele.service.rest.impl.UserResource;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
 import com.googlecode.objectify.Key;
 
 public class CloudProcessTest  {
 	
 	private final LocalServiceTestHelper helper =   new LocalServiceTestHelper(
 			new LocalDatastoreServiceTestConfig()
 				.setApplyAllHighRepJobPolicy(),
 			new LocalTaskQueueTestConfig()
 								.setDisableAutoTaskExecution(false)             
 								.setCallbackClass(LocalTaskQueueTestConfig.DeferredTaskCallback.class)) ;
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		helper.setUp();
 	}
 	
 	 @After     
 	 public void tearDown() {         helper.tearDown();     } 
 
 	
 	/** Creates and runs a simple test process verifying preservation of running task state
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void oneProcessTest() throws InterruptedException, ClassNotFoundException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 
 		Response result;
 
 		result = cpr.exec("CountDown", "CountDown", "now is the time");
 
 		assertEquals(201, result.getStatus());
 		URI processId = (URI) result.getMetadata().getFirst("Location");
 		Thread.sleep(3000);
 		CloudResourceTestWrapper.dao.clear();
 		CloudProcess process = CloudResourceTestWrapper.dao.load(processId);
 		assertEquals(ActionState.RUNABLE, process.getState());
 		CloudResourceTestWrapper.dao.clear();
 		result = cpr.refresh();
 		assertEquals(200,result.getStatus());
 		assertEquals("{\"RUNABLE\": 1}", result.getEntity());
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		CountDownAction action = (CountDownAction) ActionResource.dao.load(process.getAction());
 		assertEquals("Count value", 3, action.getCount());
 	}
 	
 	/** Creates and runs a simple test process verifying preservation of running task state
 	 * @throws IllegalAccessException 
 	 * @throws InstantiationException 
 	 * @throws URISyntaxException 
 	 */
 	@Test
 	public void cloudProcessCompletedAddTest() throws InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException, URISyntaxException
 	{
 		
 		Action task = new CountDownAction();
 		task.setUri(new URI("http://www.google.com.br"));
 		
 		// tom
 		CloudProcess tom   = new CloudProcess(UserResource.Root.getUri(), "tom", null, true, task);
 		tom.setCostPerHour((float)1.5);
 		tom.setAccount("conta");
 		tom.setComplete(Calendar.getInstance().getTime());
 		
 		CloudProcessResource.dao.add(tom);
 		
 		// jerry
 		Calendar calendar = Calendar.getInstance();
 		calendar.add(Calendar.MONTH, -2);
 		
 		CloudProcess jerry = new CloudProcess(UserResource.Root.getUri(), "jerry", null, true, task);
 		jerry.setCostPerHour((float)1.5);
 		jerry.setAccount("conta");
 		jerry.setComplete(calendar.getTime());
 		
 		CloudProcessResource.dao.add(jerry);
 		
 	}
 
 	/** Creates and runs a simple test process verifying preservation of running task state
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void oneProcessJobOkExitTest() throws InterruptedException, ClassNotFoundException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		CloudResourceTestWrapper.dao.clear();
 		Response result;
 		result = cpr.exec("Job", "Job", "CountDown foo");
 		
 		assertEquals(201, result.getStatus());
 		URI processId = (URI) result.getMetadata().getFirst("Location");
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		CloudProcess job = CloudResourceTestWrapper.dao.load(processId);
 		assertEquals(ActionState.RUNABLE, job.getState());
 		CloudResourceTestWrapper.dao.clear();
 		result = cpr.refresh();
 		assertEquals(200,result.getStatus());
 		assertEquals("{\"RUNABLE\": 1, \"RUNABLE_Wait\": 1}", result.getEntity());
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		JobAction jobAction = (JobAction) ActionResource.dao.load(job.getAction());
 		URI childProcess = jobAction.getChildProcess();
 		CloudProcess countDownProcess = CloudProcessResource.dao.load(childProcess);
 		
 		CountDownAction countDownAction = (CountDownAction) ActionResource.dao.load(countDownProcess.getAction());
 		
 		assertEquals("Count value", 3, countDownAction.getCount());
 		countDownAction.setCount(1);
 		ActionResource.dao.update(countDownAction);
 		CloudResourceTestWrapper.dao.clear();
 		result = cpr.refresh();
 		assertEquals("{\"RUNABLE\": 1, \"RUNABLE_Wait\": 1}", result.getEntity());
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		result = cpr.refresh();
 		assertEquals("{}", result.getEntity());
 		CloudResourceTestWrapper.dao.clear();
 		
 		
 		
 	}
 	
 	/** Demonstrates cancellation of a task
 	 * @throws InterruptedException
 	 * @throws ClassNotFoundException 
 	 */
 	@Test
 	public void oneProcessCancellationTest() throws InterruptedException, ClassNotFoundException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Response result = cpr.exec("CountDown", "CountDown", "doomed to die");
 		assertEquals(201, result.getStatus());
 		URI processId = (URI) result.getMetadata().getFirst("Location");
 		CloudProcess process = CloudResourceTestWrapper.dao.load(processId);
 		Thread.sleep(3000);
 		assertEquals(ActionState.RUNABLE, process.getState());
 
 		ProcessLifecycle.mgr().cancel(process);
 		CloudResourceTestWrapper.dao.clear();
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		process = CloudResourceTestWrapper.dao.load(processId);
 		assertEquals(ActionState.CANCELLED, process.getState());
 		assertTrue(process.isFinalized());
 		CountDownAction action = (CountDownAction) ActionResource.dao.load(process.getAction());
 		assertEquals("Count value", 1000, action.getCount());
 	}
 	
 	/** Demonstrates cancellation of a child job task
 	 * @throws InterruptedException
 	 * @throws ClassNotFoundException 
 	 */
 	@Test
 	public void oneProcessJobCancellationTest() throws InterruptedException, ClassNotFoundException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Response result;
 		result = cpr.exec("Job", "Job", "CountDown doomed2die2");
 		
 		assertEquals(201, result.getStatus());
 		URI processId = (URI) result.getMetadata().getFirst("Location");
 		
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		CloudProcess job = CloudResourceTestWrapper.dao.load(processId);
 		JobAction jobAction = (JobAction) ActionResource.dao.load(job.getAction());
 		CloudProcess process = CloudResourceTestWrapper.dao.load(jobAction.getChildProcess());
 		
 		assertEquals(ActionState.RUNABLE, process.getState());
 
 		ProcessLifecycle.mgr().cancel(process);
 		CloudResourceTestWrapper.dao.clear();
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		process = CloudResourceTestWrapper.dao.load(process.getUri());
 		assertEquals(ActionState.CANCELLED, process.getState());
 		assertTrue(process.isFinalized());
 		CountDownAction action = (CountDownAction) ActionResource.dao.load(process.getAction());
 		assertEquals("Count value", 1000, action.getCount());
 		job = CloudResourceTestWrapper.dao.load(job.getUri());
 		jobAction = (JobAction) ActionResource.dao.load(jobAction.getId());
 		assertEquals(ActionState.FAILED, job.getState());
 		assertEquals(ActionState.CANCELLED, jobAction.getChildEndState());
 	}
 	
 	
 	/** Demonstrates cancellation of the parent job task showing child cleanup
 	 * @throws InterruptedException
 	 * @throws ClassNotFoundException 
 	 */
 	@Test
 	public void oneProcessJobParentCancellationTest() throws InterruptedException, ClassNotFoundException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Response result;
 		result = cpr.exec("Job", "Job", "CountDown doomed2die2");
 		
 		assertEquals(201, result.getStatus());
 		URI processId = (URI) result.getMetadata().getFirst("Location");
 		
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		CloudProcess job = CloudResourceTestWrapper.dao.load(processId);
 		JobAction jobAction = (JobAction) ActionResource.dao.load(job.getAction());
 		CloudProcess process = CloudResourceTestWrapper.dao.load(jobAction.getChildProcess());
 		
 		assertEquals(ActionState.RUNABLE, process.getState());
 
 		ProcessLifecycle.mgr().cancel(job);
 		CloudResourceTestWrapper.dao.clear();
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		process = CloudResourceTestWrapper.dao.load(process.getUri());
 		assertEquals(ActionState.CANCELLED, process.getState());
 		assertTrue(process.isFinalized());
 		CountDownAction action = (CountDownAction) ActionResource.dao.load(process.getAction());
 		assertEquals("Count value", 2000, action.getCount());
 		job = CloudResourceTestWrapper.dao.load(job.getUri());
 		jobAction = (JobAction) ActionResource.dao.load(jobAction.getId());
 		assertEquals(ActionState.CANCELLED, job.getState());
 		assertEquals(ActionState.CANCELLED, jobAction.getChildEndState());
 	}
 	
 	/** Demonstrates exception throw in Init phase
 	 * @throws InterruptedException
 	 * @throws ClassNotFoundException 
 	 */
 	@Test
 	public void oneProcessInitExceptionTest() throws InterruptedException, ClassNotFoundException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Response result = cpr.exec("CountDown", "CountDown", "throwInit");
 		assertEquals(201, result.getStatus());
 		URI processId = (URI) result.getMetadata().getFirst("Location");
 		Thread.sleep(3000);
 		CloudResourceTestWrapper.dao.clear();
 		CloudProcess process = CloudResourceTestWrapper.dao.load(processId);
 
 		assertEquals(ActionState.FAILED, process.getState());
 		assertTrue(process.isFinalized());
 		CountDownAction action = (CountDownAction) ActionResource.dao.load(process.getAction());
 		assertEquals("Count value", 5, action.getCount());
 	}
 	
 	/** Demonstrates exception throw in Init phase of a child job
 	 * @throws InterruptedException
 	 * @throws ClassNotFoundException 
 	 */
 	@Test
 	public void oneProcessJobInitExceptionTest() throws InterruptedException, ClassNotFoundException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Response result;
 		result = cpr.exec("Job", "Job", "CountDown throwInit");
 		assertEquals(201, result.getStatus());
 		URI processId = (URI) result.getMetadata().getFirst("Location");
 		Thread.sleep(3000);
 		CloudResourceTestWrapper.dao.clear();
 		CloudProcess job = CloudResourceTestWrapper.dao.load(processId);
 		JobAction jobAction = (JobAction) ActionResource.dao.load(job.getAction());
 		CloudProcess process = CloudResourceTestWrapper.dao.load(jobAction.getChildProcess());
 
 		assertEquals(ActionState.FAILED, process.getState());
 		assertTrue(process.isFinalized());
 		CountDownAction action = (CountDownAction) ActionResource.dao.load(process.getAction());
 		assertEquals("Count value", 5, action.getCount());
 		CloudResourceTestWrapper.dao.clear();
 		result = cpr.refresh();
 		assertEquals(200,result.getStatus());
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		job = CloudResourceTestWrapper.dao.load(job.getUri());
 		jobAction = (JobAction) ActionResource.dao.load(jobAction.getId());
 		assertEquals(ActionState.FAILED, job.getState());
 		assertEquals(ActionState.FAILED, jobAction.getChildEndState());
 	}
 	
 	/** Demonstrates exception throw in Run phase
 	 * @throws InterruptedException
 	 * @throws ClassNotFoundException 
 	 */
 	@Test
 	public void oneProcessRunExceptionTest() throws InterruptedException, ClassNotFoundException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Response result = cpr.exec("CountDown", "CountDown", "throw5");
 		assertEquals(201, result.getStatus());
 		URI processId = (URI) result.getMetadata().getFirst("Location");
 		Thread.sleep(3000);
 		CloudResourceTestWrapper.dao.clear();
 		CloudProcess process = CloudResourceTestWrapper.dao.load(processId);
 		assertEquals(ActionState.FAILED, process.getState());
 	
 		assertTrue(process.isFinalized());
 		CountDownAction action = (CountDownAction) ActionResource.dao.load(process.getAction());
 		assertEquals("Count value", 4, action.getCount());
 	}
 	
 	/** Demonstrates exception throw in Run phase of a Child 
 	 * @throws InterruptedException
 	 * @throws ClassNotFoundException 
 	 */
 	@Test
 	public void oneProcessJobRunExceptionTest() throws InterruptedException, ClassNotFoundException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Response result = cpr.exec("Job", "Job", "CountDown throw5");
 		assertEquals(201, result.getStatus());
 		URI processId = (URI) result.getMetadata().getFirst("Location");
 		Thread.sleep(3000);
 		CloudResourceTestWrapper.dao.clear();
 		CloudProcess job = CloudResourceTestWrapper.dao.load(processId);
 		JobAction jobAction = (JobAction) ActionResource.dao.load(job.getAction());
 		CloudProcess process = CloudResourceTestWrapper.dao.load(jobAction.getChildProcess());;
 		assertEquals(ActionState.FAILED, process.getState());
 		assertTrue(process.isFinalized());
 		CountDownAction action = (CountDownAction) ActionResource.dao.load(process.getAction());
 		assertEquals("Count value", 4, action.getCount());
 		CloudResourceTestWrapper.dao.clear();
 		result = cpr.refresh();
 		assertEquals(200,result.getStatus());
 		assertEquals("{}", result.getEntity());
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		
 		
 		job = CloudResourceTestWrapper.dao.load(job.getUri());
 		jobAction = (JobAction) ActionResource.dao.load(jobAction.getId());
 		assertEquals(ActionState.FAILED, job.getState());
 		assertEquals(ActionState.FAILED, jobAction.getChildEndState());
 	}
 	
 	/** Demonstrates two processes and activation based on dependency completion
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void twoProcessWithPreestablishedDependency() throws InterruptedException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Context tom_env = new Context();
 		tom_env.putValue("arg", "tom rocks!");
 		
 		final Context jerry_env = new Context();
 		jerry_env.putValue("arg", "jelly rolls");
 	
 		CloudProcess tom = ProcessLifecycle.mgr().createProcess(UserResource.Root, "tom", tom_env, null, null, true, CountDownAction.class);
 		List<URI> jerryDependency = new ArrayList<URI>();
 		jerryDependency.add(tom.getUri());
 		CloudProcess jerry = ProcessLifecycle.mgr().createProcess(UserResource.Root, "jerry", jerry_env, jerryDependency, null, true, CountDownAction.class);
 		ProcessLifecycle.mgr().init(tom);
 		ProcessLifecycle.mgr().init(jerry);
 
 		Thread.sleep(3000);
 		CloudResourceTestWrapper.dao.clear();
 		
 		
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.RUNABLE, tom.getState());
 		assertEquals(ActionState.INIT, jerry.getState());
 		CloudResourceTestWrapper.dao.clear();
 		CountDownAction tomAction = (CountDownAction) ActionResource.dao.load(tom.getAction());
 		tomAction.setCount(1);
 		ActionResource.dao.update(tomAction);
 		
 		CloudResourceTestWrapper.dao.clear();
 		Response result = cpr.refresh();
 		assertEquals(200,result.getStatus());
 		assertEquals("{\"RUNABLE\": 1, \"INIT\": 1}", result.getEntity());
 		
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.COMPLETE, tom.getState());
 		assertEquals(ActionState.RUNABLE, jerry.getState());
 
 	}
 	
 	/** Demonstrates three processes and activation based on dependency completion of the other two
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void threeProcessWithPreestablishedDependency() throws InterruptedException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Context tom_env = new Context();
 		tom_env.putValue("arg", "tom rocks!");
 		
 		final Context jerry_env = new Context();
 		jerry_env.putValue("arg", "jelly rolls");
 		
 		final Context curley_env = new Context();
 		curley_env.putValue("arg", "curley tops");
 	
 		CloudProcess tom = ProcessLifecycle.mgr().createProcess(UserResource.Root, "tom", tom_env, null, null, true, CountDownAction.class);
 		CloudProcess curley = ProcessLifecycle.mgr().createProcess(UserResource.Root, "curley", curley_env, null, null, true, CountDownAction.class);
 		List<URI> jerryDependency = new ArrayList<URI>();
 		jerryDependency.add(tom.getUri());
 		jerryDependency.add(curley.getUri());
 		CloudProcess jerry = ProcessLifecycle.mgr().createProcess(UserResource.Root, "jerry", jerry_env, jerryDependency, null, true, CountDownAction.class);
 		ProcessLifecycle.mgr().init(tom);
 		ProcessLifecycle.mgr().init(curley);
 		ProcessLifecycle.mgr().init(jerry);
 
 		Thread.sleep(3000);
 		CloudResourceTestWrapper.dao.clear();
 		
 		
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		curley  = CloudResourceTestWrapper.dao.load(curley.getUri());
 		assertEquals(ActionState.RUNABLE, tom.getState());
 		assertEquals(ActionState.RUNABLE, curley.getState());
 		assertEquals(ActionState.INIT, jerry.getState());
 		CloudResourceTestWrapper.dao.clear();
 		CountDownAction tomAction = (CountDownAction) ActionResource.dao.load(tom.getAction());
 		tomAction.setCount(1);
 		ActionResource.dao.update(tomAction);
 		CountDownAction curleyAction = (CountDownAction) ActionResource.dao.load(curley.getAction());
 		curleyAction.setCount(1);
 		ActionResource.dao.update(curleyAction);
 		
 		CloudResourceTestWrapper.dao.clear();
 		Response result = cpr.refresh();
 		assertEquals(200,result.getStatus());
 		assertEquals("{\"RUNABLE\": 2, \"INIT\": 1}", result.getEntity());
 		
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		curley  = CloudResourceTestWrapper.dao.load(curley.getUri());
 		assertEquals(ActionState.COMPLETE, tom.getState());
 		assertEquals(ActionState.COMPLETE, curley.getState());
 		assertEquals(ActionState.RUNABLE, jerry.getState());
 
 	}
 	
 	/** Demonstrates two processes and activation based on dependency completion
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void twoProcessWithDependencyAddedAfterDependentCompleted() throws InterruptedException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Context tom_env = new Context();
 		tom_env.putValue("arg", "tom rocks!");
 		
 		final Context jerry_env = new Context();
 		jerry_env.putValue("arg", "jelly rolls");
 	
 		CloudProcess tom = ProcessLifecycle.mgr().createProcess(UserResource.Root, "tom", tom_env, null, null, true, CountDownAction.class);
 		ProcessLifecycle.mgr().init(tom);
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		assertEquals(ActionState.RUNABLE, tom.getState());
 		CloudResourceTestWrapper.dao.clear();
 		CountDownAction tomAction = (CountDownAction) ActionResource.dao.load(tom.getAction());
 		tomAction.setCount(1);
 		ActionResource.dao.update(tomAction);
 
 		
 		List<URI> jerryDependency = new ArrayList<URI>();
 		jerryDependency.add(tom.getUri());
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		
 		CloudProcess jerry = ProcessLifecycle.mgr().createProcess(UserResource.Root, "jerry", jerry_env, jerryDependency, null, true, CountDownAction.class);
 		ProcessLifecycle.mgr().init(jerry);
 		
 		CloudResourceTestWrapper.dao.clear();
 		
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.COMPLETE, tom.getState());
 		assertEquals(ActionState.RUNABLE, jerry.getState());
 
 	}
 	
 	/** Demonstrates two processes and clean up processing associated with task cancellation
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void twoProcessWithPreestablishedDependencyCancelTest() throws InterruptedException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Context tom_env = new Context();
 		tom_env.putValue("arg", "tom rocks!");
 		
 		final Context jerry_env = new Context();
 		jerry_env.putValue("arg", "jelly rolls");
 	
 		CloudProcess tom = ProcessLifecycle.mgr().createProcess(UserResource.Root, "tom", tom_env, null, null, true, CountDownAction.class);
 		List<URI> jerryDependency = new ArrayList<URI>();
 		jerryDependency.add(tom.getUri());
 		CloudProcess jerry = ProcessLifecycle.mgr().createProcess(UserResource.Root, "jerry", jerry_env, jerryDependency, null, true, CountDownAction.class);
 		ProcessLifecycle.mgr().init(tom);
 		ProcessLifecycle.mgr().init(jerry);
 
 		Thread.sleep(3000);
 		CloudResourceTestWrapper.dao.clear();
 
 		
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.RUNABLE, tom.getState());
 		assertEquals(ActionState.INIT, jerry.getState());
 		CloudResourceTestWrapper.dao.clear();
 		ProcessLifecycle.mgr().cancel(tom);
 
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		tom = CloudResourceTestWrapper.dao.load(tom.getUri());
 		assertEquals(ActionState.CANCELLED, tom.getState());
 		assertTrue(tom.isFinalized());
 		CloudResourceTestWrapper.dao.clear();
 
 		jerry = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.CANCELLED, jerry.getState());
 		assertTrue(jerry.isFinalized());
 
 	}
 	
 	/** Demonstrates two processes and activation based on dependency completion
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void twoProcessWithDependencyAddedAfterDependentCancelled() throws InterruptedException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Context tom_env = new Context();
 		tom_env.putValue("arg", "tom rocks!");
 		
 		final Context jerry_env = new Context();
 		jerry_env.putValue("arg", "jelly rolls");
 	
 		CloudProcess tom = ProcessLifecycle.mgr().createProcess(UserResource.Root, "tom", tom_env, null, null, true, CountDownAction.class);
 		ProcessLifecycle.mgr().init(tom);
 		Thread.sleep(3000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		assertEquals(ActionState.RUNABLE, tom.getState());
 		CloudResourceTestWrapper.dao.clear();
 		ProcessLifecycle.mgr().cancel(tom);
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		
 		List<URI> jerryDependency = new ArrayList<URI>();
 		jerryDependency.add(tom.getUri());
 		
 		CloudProcess jerry;
 		try {
 			jerry = ProcessLifecycle.mgr().createProcess(UserResource.Root, "jerry", jerry_env, jerryDependency, null, true, CountDownAction.class);
 			fail("Exception expected");
 		} catch (IllegalArgumentException e) {
 			CloudResourceTestWrapper.dao.clear();
 			tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 			assertEquals(ActionState.CANCELLED, tom.getState());
 		}
 
 	}
 	
 	/** Demonstrates two processes and clean up processing associated with task init failure
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void twoProcessWithPreestablishedDependencyInitFailTest() throws InterruptedException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		
 		Context tom_env = new Context();
 		tom_env.putValue("arg", "throwInit");
 		
 		final Context jerry_env = new Context();
 		jerry_env.putValue("arg", "jelly rolls");
 	
 		CloudProcess tom = ProcessLifecycle.mgr().createProcess(UserResource.Root, "tom", tom_env, null, null, true, CountDownAction.class);
 		List<URI> jerryDependency = new ArrayList<URI>();
 		jerryDependency.add(tom.getUri());
 		CloudProcess jerry = ProcessLifecycle.mgr().createProcess(UserResource.Root, "jerry", jerry_env, jerryDependency, null, true, CountDownAction.class);
 		ProcessLifecycle.mgr().init(tom);
 		ProcessLifecycle.mgr().init(jerry);
 
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 
 		
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.FAILED, tom.getState());
 		assertEquals(ActionState.CANCELLED, jerry.getState());
 		assertTrue(tom.isFinalized());
 		assertTrue(jerry.isFinalized());
 
 	}
 	
 	/** Demonstrates two processes and clean up processing associated with task runtime failure
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void twoProcessWithPreestablishedDependencyFailTest() throws InterruptedException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 
 		Context tom_env = new Context();
 		tom_env.putValue("arg", "throw5");
 		
 		final Context jerry_env = new Context();
 		jerry_env.putValue("arg", "jelly rolls");
 	
 		CloudProcess tom = ProcessLifecycle.mgr().createProcess(UserResource.Root, "tom", tom_env, null, null, true, CountDownAction.class);
 		List<URI> jerryDependency = new ArrayList<URI>();
 		jerryDependency.add(tom.getUri());
 		CloudProcess jerry = ProcessLifecycle.mgr().createProcess(UserResource.Root, "jerry", jerry_env, jerryDependency, null, true, CountDownAction.class);
 		ProcessLifecycle.mgr().init(tom);
 		ProcessLifecycle.mgr().init(jerry);
 
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 
 		
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.FAILED, tom.getState());
 		assertEquals(ActionState.CANCELLED, jerry.getState());
 		assertTrue(jerry.isFinalized());
 		assertTrue(tom.isFinalized());
 
 	}
 	
 	/** Demonstrates dump of first of two processes and activation based on dependency completion
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void twoProcessWithPreestablishedDependencyGetsDump() throws InterruptedException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Context tom_env = new Context();
 		tom_env.putValue("arg", "tom rocks!");
 		
 		final Context jerry_env = new Context();
 		jerry_env.putValue("arg", "jelly rolls");
 	
 		CloudProcess tom = ProcessLifecycle.mgr().createProcess(UserResource.Root, "tom", tom_env, null, null, true, CountDownAction.class);
 		List<URI> jerryDependency = new ArrayList<URI>();
 		jerryDependency.add(tom.getUri());
 		CloudProcess jerry = ProcessLifecycle.mgr().createProcess(UserResource.Root, "jerry", jerry_env, jerryDependency, null, true, CountDownAction.class);
 		ProcessLifecycle.mgr().init(tom);
 		ProcessLifecycle.mgr().init(jerry);
 
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		
 		
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.RUNABLE, tom.getState());
 		assertEquals(ActionState.INIT, jerry.getState());
 		CloudResourceTestWrapper.dao.clear();
 		ProcessLifecycle.mgr().dump(tom);
 		
 		CloudResourceTestWrapper.dao.clear();
 		
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.CANCELLED, tom.getState());
 		assertTrue(tom.isFinalized());
 		assertEquals(ActionState.CANCELLED, jerry.getState());
 		assertTrue(jerry.isFinalized());
 		
 		CountDownAction cda = (CountDownAction) ActionResource.dao.load(tom.getAction());
 		assertEquals("dump entry point activated", 2000, cda.getCount());
 		CountDownAction jcda = (CountDownAction) ActionResource.dao.load(jerry.getAction());
 		assertEquals("cancel entry point not activated", 99, jcda.getCount());
 
 	}
 	
 	/** Demonstrates a running process that gets blocked by an added dependency
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void runningProcessGetsDependencyAdded() throws InterruptedException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Context tom_env = new Context();
 		tom_env.putValue("arg", "tom rocks!");
 		
 		final Context jerry_env = new Context();
 		jerry_env.putValue("arg", "jelly rolls");
 	
 		CloudProcess tom = ProcessLifecycle.mgr().createProcess(UserResource.Root, "tom", tom_env, null, null, true, CountDownAction.class);
 		List<URI> jerryDependency = new ArrayList<URI>();
 		ProcessLifecycle.mgr().init(tom);
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		assertEquals(ActionState.RUNABLE, tom.getState());
 		//
 		CloudProcess jerry = ProcessLifecycle.mgr().createProcess(UserResource.Root, "jerry", jerry_env, null, null, true, CountDownAction.class);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		ProcessLifecycle.mgr().addDependentOn(tom, jerry);
 		ProcessLifecycle.mgr().init(jerry);
 
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		
 		
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.BLOCKED, tom.getState());
 		assertEquals(ActionState.RUNABLE, jerry.getState());
 	}
 	
 	/**  Demonstrates a running process that blocked by an added dependency resuming after the dependency completes
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void runningProcessGetsDependencyAddedAndResumesOnCompletion() throws InterruptedException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Context tom_env = new Context();
 		tom_env.putValue("arg", "tom rocks!");
 		
 		final Context jerry_env = new Context();
 		jerry_env.putValue("arg", "jelly rolls");
 	
 		CloudProcess tom = ProcessLifecycle.mgr().createProcess(UserResource.Root, "tom", tom_env, null, null, true, CountDownAction.class);
 		List<URI> jerryDependency = new ArrayList<URI>();
 		ProcessLifecycle.mgr().init(tom);
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		assertEquals(ActionState.RUNABLE, tom.getState());
 		//
 		CloudProcess jerry = ProcessLifecycle.mgr().createProcess(UserResource.Root, "jerry", jerry_env, null, null, true, CountDownAction.class);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		ProcessLifecycle.mgr().addDependentOn(tom, jerry);
 		ProcessLifecycle.mgr().init(jerry);
 
 		Thread.sleep(3000);
 		CloudResourceTestWrapper.dao.clear();
 		
 		
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.BLOCKED, tom.getState());
 		assertEquals(ActionState.RUNABLE, jerry.getState());
 		
 		CountDownAction jerryAction = (CountDownAction) ActionResource.dao.load(jerry.getAction());
 		jerryAction.setCount(1);
 		ActionResource.dao.update(jerryAction);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.RUNABLE, tom.getState());
 		assertEquals(ActionState.COMPLETE, jerry.getState());
 		
 	}
 	
 	
 	/** Demonstrates a running process that gets blocked by an added dependency and cancelled
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void runningProcessGetsDependencyAddedAndCancelled() throws InterruptedException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Context tom_env = new Context();
 		tom_env.putValue("arg", "tom rocks!");
 		
 		final Context jerry_env = new Context();
 		jerry_env.putValue("arg", "jelly rolls");
 	
 		CloudProcess tom = ProcessLifecycle.mgr().createProcess(UserResource.Root, "tom", tom_env, null, null, true, CountDownAction.class);
 		List<URI> jerryDependency = new ArrayList<URI>();
 		ProcessLifecycle.mgr().init(tom);
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		assertEquals(ActionState.RUNABLE, tom.getState());
 		//
 		CloudProcess jerry = ProcessLifecycle.mgr().createProcess(UserResource.Root, "jerry", jerry_env, null, null, true, CountDownAction.class);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		ProcessLifecycle.mgr().addDependentOn(tom, jerry);
 		ProcessLifecycle.mgr().init(jerry);
 
 		Thread.sleep(3000);
 		CloudResourceTestWrapper.dao.clear();
 		
 		
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.BLOCKED, tom.getState());
 		assertEquals(ActionState.RUNABLE, jerry.getState());
 		
 		CountDownAction jerryAction = (CountDownAction) ActionResource.dao.load(jerry.getAction());
 		jerryAction.setCount(1);
 		ActionResource.dao.update(jerryAction);
 		ProcessLifecycle.mgr().cancel(tom);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.CANCELLED, tom.getState());
 		assertEquals(ActionState.COMPLETE, jerry.getState());
 	}
 	
 	/** Demonstrates a running process that gets blocked by an added dependency and cancelled
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void runningProcessGetsDependencyAddedAndRunningProcessCancelled() throws InterruptedException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Context tom_env = new Context();
 		tom_env.putValue("arg", "tom rocks!");
 		
 		final Context jerry_env = new Context();
 		jerry_env.putValue("arg", "jelly rolls");
 	
 		CloudProcess tom = ProcessLifecycle.mgr().createProcess(UserResource.Root, "tom", tom_env, null, null, true, CountDownAction.class);
 		List<URI> jerryDependency = new ArrayList<URI>();
 		ProcessLifecycle.mgr().init(tom);
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		assertEquals(ActionState.RUNABLE, tom.getState());
 		//
 		CloudProcess jerry = ProcessLifecycle.mgr().createProcess(UserResource.Root, "jerry", jerry_env, null, null, true, CountDownAction.class);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		ProcessLifecycle.mgr().addDependentOn(tom, jerry);
 		ProcessLifecycle.mgr().init(jerry);
 
 		Thread.sleep(3000);
 		CloudResourceTestWrapper.dao.clear();
 		
 		
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.BLOCKED, tom.getState());
 		assertEquals(ActionState.RUNABLE, jerry.getState());
 		
 		CountDownAction jerryAction = (CountDownAction) ActionResource.dao.load(jerry.getAction());
 		jerryAction.setCount(1);
 		ActionResource.dao.update(jerryAction);
 		ProcessLifecycle.mgr().cancel(jerry);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.CANCELLED, tom.getState());
 		assertEquals(ActionState.CANCELLED, jerry.getState());
 		CountDownAction ta = (CountDownAction) ActionResource.dao.load(tom.getAction());
 		assertEquals("cancel has been called", 1000, ta.getCount());
 		CountDownAction ja = (CountDownAction) ActionResource.dao.load(jerry.getAction());
 		assertEquals("cancel has been called", 1000, ja.getCount());
 	}
 	
 	/** Demonstrates a running process that gets blocked by an added dependency and cancelled
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void runningProcessGetsDependencyAddedAndDumped() throws InterruptedException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Context tom_env = new Context();
 		tom_env.putValue("arg", "tom rocks!");
 		
 		final Context jerry_env = new Context();
 		jerry_env.putValue("arg", "jelly rolls");
 	
 		CloudProcess tom = ProcessLifecycle.mgr().createProcess(UserResource.Root, "tom", tom_env, null, null, true, CountDownAction.class);
 		List<URI> jerryDependency = new ArrayList<URI>();
 		ProcessLifecycle.mgr().init(tom);
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		assertEquals(ActionState.RUNABLE, tom.getState());
 		//
 		CloudProcess jerry = ProcessLifecycle.mgr().createProcess(UserResource.Root, "jerry", jerry_env, null, null, true, CountDownAction.class);
 		ProcessLifecycle.mgr().init(jerry);
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		ProcessLifecycle.mgr().addDependentOn(tom, jerry);
 		Thread.sleep(1000);
 
 
 		CloudResourceTestWrapper.dao.clear();
 		
 		
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.BLOCKED, tom.getState());
 		assertEquals(ActionState.RUNABLE, jerry.getState());
 		
 		CountDownAction jerryAction = (CountDownAction) ActionResource.dao.load(jerry.getAction());
 		jerryAction.setCount(1);
 		ActionResource.dao.update(jerryAction);
 		ProcessLifecycle.mgr().dump(tom);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.CANCELLED, tom.getState());
 		assertEquals(ActionState.COMPLETE, jerry.getState());
 		CountDownAction ta = (CountDownAction) ActionResource.dao.load(tom.getAction());
 		assertEquals("dump has been called", 2000, ta.getCount());
 	}
 	
 	/** Demonstrates a running process that gets blocked by an added dependency and cancelled
 	 * @throws InterruptedException
 	 */
 	@Test
 	public void runningProcessGetsDependencyAddedAndRunningProcessDumped() throws InterruptedException {
 		User root = UserResource.Root;
 		assertNotNull(root);
 		CloudResourceTestWrapper cpr = new CloudResourceTestWrapper(); cpr.addSecurityContext(null);
 		Context tom_env = new Context();
 		tom_env.putValue("arg", "tom rocks!");
 		
 		final Context jerry_env = new Context();
 		jerry_env.putValue("arg", "jelly rolls");
 	
 		CloudProcess tom = ProcessLifecycle.mgr().createProcess(UserResource.Root, "tom", tom_env, null, null, true, CountDownAction.class);
 		List<URI> jerryDependency = new ArrayList<URI>();
 		ProcessLifecycle.mgr().init(tom);
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		Thread.sleep(2000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		assertEquals(ActionState.RUNABLE, tom.getState());
 		//
 		CloudProcess jerry = ProcessLifecycle.mgr().createProcess(UserResource.Root, "jerry", jerry_env, null, null, true, CountDownAction.class);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
		Thread.sleep(2000);
		CloudResourceTestWrapper.dao.clear();
 		ProcessLifecycle.mgr().addDependentOn(tom, jerry);
 		ProcessLifecycle.mgr().init(jerry);
 
 		Thread.sleep(3000);
 		CloudResourceTestWrapper.dao.clear();
 		
 		
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.BLOCKED, tom.getState());
 		assertEquals(ActionState.RUNABLE, jerry.getState());
 		
 		CountDownAction jerryAction = (CountDownAction) ActionResource.dao.load(jerry.getAction());
 		jerryAction.setCount(1);
 		ActionResource.dao.update(jerryAction);
 		ProcessLifecycle.mgr().dump(jerry);
 		CloudResourceTestWrapper.dao.clear();
 		cpr.refresh();
 		Thread.sleep(1000);
 		CloudResourceTestWrapper.dao.clear();
 		tom  = CloudResourceTestWrapper.dao.load(tom.getUri());
 		jerry  = CloudResourceTestWrapper.dao.load(jerry.getUri());
 		assertEquals(ActionState.CANCELLED, tom.getState());
 		assertEquals(ActionState.CANCELLED, jerry.getState());
 		CountDownAction ta = (CountDownAction) ActionResource.dao.load(tom.getAction());
 		assertEquals("dump has been called", 2000, ta.getCount());
 		CountDownAction ja = (CountDownAction) ActionResource.dao.load(jerry.getAction());
 		assertEquals("dump has been called", 2000, ja.getCount());
 	}
 
 
 	public static class CloudResourceTestWrapper extends CloudProcessResource {
 		public void addSecurityContext(User user) {
 			final User u;
 			if(user == null) {
 				try {
 					User temp = com.googlecode.objectify.ObjectifyService.ofy().load().type(User.class).id(UserResource.Root.getId()).safeGet();
 				} catch (com.googlecode.objectify.NotFoundException e) {
 					User temp = UserResource.Root;
 					URI initial = temp.getUri();
 					temp.setId(null);
 					Key<User>key =  com.googlecode.objectify.ObjectifyService.ofy().save().entity(temp).now();
 					temp = com.googlecode.objectify.ObjectifyService.ofy().load().type(User.class).id(UserResource.Root.getId()).get();
 					UserResource.Root.setId(temp.getId());
 					UserResource.Root.setUri(temp.getUri());
 					System.out.println("============================>addSecurity notfoundexception initial="+initial.toString()+" final "+temp.toString());
 				}
 				u = UserResource.Root;
 				System.out.println("============================>Root is "+u.getUri());
 
 			} else {
 				u = user;
 			}
 			SecurityContext context = new SecurityContext() {
 
 				@Override
 				public String getAuthenticationScheme() {
 					return "Basic";
 				}
 
 				@Override
 				public Principal getUserPrincipal() {
 					return u;
 				}
 
 				@Override
 				public boolean isSecure() {
 					return true;
 				}
 
 				@Override
 				public boolean isUserInRole(String arg0) {
 					return true;
 				}};
 				
 				super.securityContext = context;
 		}
 	}
 }
