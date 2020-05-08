 package org.uncertweb.aquacrop.remote;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.uncertweb.aquacrop.AquaCropException;
 import org.uncertweb.aquacrop.data.Output;
 import org.uncertweb.aquacrop.data.Project;
 import org.uncertweb.aquacrop.test.TestData;
 import org.uncertweb.aquacrop.test.TestEnvironment;
 
 public class AquaCropServerTest {
 
 	@Rule
 	public ExpectedException exception = ExpectedException.none();
 
 	private AquaCropServer server;
 	private int port;
 	
 	private static final int STRESS_THREADS = 4;
 	private static final int STRESS_REQUESTS = 4;
 
 	@Before
 	public void before() {
 		port = TestEnvironment.getInstance().getPort();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				TestEnvironment env = TestEnvironment.getInstance();
 				server = new AquaCropServer(port, env.getBasePath(), env.getPrefixCommand(), env.getBasePathOverride());
 				try {
 					server.start();
 				}
 				catch (IOException e) {
 					fail("Couldn't start server: " + e.getMessage());
 				}
 			}
 		}).start();
 	}
 
 	@After
 	public void after() throws IOException {
 		server.stop();
 	}
 
 	@Test
 	public void serverHandlesRequest() throws AquaCropRemoteException, IOException, AquaCropException {
 		AquaCropClient client = new AquaCropClient("localhost", port);
 		Output output = client.send(TestData.getProject());
 		assertNotNull(output);
 	}
 
 	@Test
 	public void serverHandlesInvalidProject() throws AquaCropRemoteException, IOException, AquaCropException {
 		Project project = TestData.getProject();
 		project.getCropCharacteristics().setNumPlants(-1); // invalid
 		AquaCropClient client = new AquaCropClient("localhost", port);
 		exception.expect(AquaCropException.class);
 		client.send(project);
 	}
 
 	@Test
 	public void stress() throws InterruptedException, ExecutionException {
 		// create task list
 		Callable<List<Output>> task = new ProjectSender(STRESS_REQUESTS);
 		List<Callable<List<Output>>> tasks = Collections.nCopies(STRESS_THREADS, task);
 
 		ExecutorService executor = Executors.newFixedThreadPool(STRESS_THREADS);
 
 		// blocks
 		List<Future<List<Output>>> futures = executor.invokeAll(tasks);
 
 		// all should be successful - no nulls or exceptions
 		for (Future<List<Output>> future : futures) {
 			List<Output> outputs = future.get();
 			for (Output output : outputs) {
 				assertNotNull(output);
 			}
 		}
 	}
 
 	@Test
 	public void stressWithErrors() throws InterruptedException, ExecutionException {
 		// create task list
 		Callable<List<Output>> validTask = new ProjectSender(STRESS_REQUESTS);
 		Callable<List<Output>> invalidTask = new InvalidProjectSender(STRESS_REQUESTS);
		List<Callable<List<Output>>> tasks = Collections.nCopies(STRESS_THREADS / 2, validTask);
 		tasks.addAll(Collections.nCopies(STRESS_THREADS / 2, invalidTask));
 		Collections.shuffle(tasks);
 
 		ExecutorService executor = Executors.newFixedThreadPool(STRESS_THREADS);
 
 		// blocks
 		List<Future<List<Output>>> futures = executor.invokeAll(tasks);
 
 		// check they were successful
 		for (int i = 0; i < tasks.size(); i++) {
 			Callable<List<Output>> task = tasks.get(i);
 			Future<List<Output>> future = futures.get(i);
 
 			if (task instanceof ProjectSender) {
 				for (Output output : future.get()) {
 					assertNotNull(output);
 				}
 			}
 			else {
 				for (Output output : future.get()) {
 					assertNull(output);
 				}
 			}
 		}
 	}
 
 	private class ProjectSender implements Callable<List<Output>> {
 		private Project project;
 		private int requestCount;
 
 		public ProjectSender(int requestCount) {
 			this.project = TestData.getProject();
 			this.requestCount = requestCount;
 		}
 
 		@Override
 		public List<Output> call() throws AquaCropRemoteException, IOException, AquaCropException {
 			List<Output> outputs = new ArrayList<Output>(requestCount);
 			AquaCropClient client = new AquaCropClient("localhost", port);
 			for (int i = 0; i < requestCount; i++) {
 				project.setTitle("Project " + (i + 1));
 				outputs.add(client.send(project));
 			}
 			return outputs;
 		}
 	}
 
 	private class InvalidProjectSender implements Callable<List<Output>> {
 		private Project project;
 		private int requestCount;
 
 		public InvalidProjectSender(int requestCount) {
 			this.project = TestData.getProject();
 			this.project.getCropCharacteristics().setNumPlants(-1); // invalid
 			this.requestCount = requestCount;
 		}
 
 		@Override
 		public List<Output> call() throws AquaCropRemoteException, IOException {
 			List<Output> outputs = new ArrayList<Output>(requestCount);
 			AquaCropClient client = new AquaCropClient("localhost", port);
 			for (int i = 0; i < requestCount; i++) {
 				project.setTitle("Invalid project " + (i + 1));
 				try {
 					outputs.add(client.send(project));
 				}
 				catch (AquaCropException e) {
 					outputs.add(null);
 				}
 			}
 			return outputs;
 		}
 	}
 
 }
