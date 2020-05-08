 package com.cloudcontrolled.api.client;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.cloudcontrolled.api.client.auth.Credentials;
 import com.cloudcontrolled.api.model.Worker;
 import com.cloudcontrolled.api.request.CreateWorkerRequest;
 import com.cloudcontrolled.api.request.DeleteWorkerRequest;
 import com.cloudcontrolled.api.request.ListWorkerRequest;
 import com.cloudcontrolled.api.response.CreateWorkerResponse;
 import com.cloudcontrolled.api.response.DeleteWorkerResponse;
 import com.cloudcontrolled.api.response.ListWorkerResponse;
 
//@Ignore
 public class WorkerIntegrationTest {
 
 	private String application = "zj";
 	private String deployment = "default";
 	private String worker = "worker.long";
 
 	private Credentials credentials;
 	private CloudControlClient client;
 
 	private String cctrl_email = System.getenv("CCTRL_EMAIL");
 	private String cctrl_password = System.getenv("CCTRL_PASSWORD");
 
 	@Before
 	public void setUp() {
 		credentials = new Credentials(cctrl_email, cctrl_password);
 		client = new CloudControlClient(credentials);
 	}
 
 	@Test
 	public void testCreate10() {
 		for (int i = 0; i < 10; i++) {
 			testCreateWorker();
 		}
 	}
 
 	@Test
 	public void testCreateWorker() {
 		CreateWorkerRequest request = new CreateWorkerRequest();
 		request.setApplicationName(application);
 		request.setDeploymentName(deployment);
 		request.setWorker(worker);
 
 		CreateWorkerResponse response = client.send(request);
 		System.out.println(response);
 	}
 
 	@Test
 	public void testListWorkers() {
 		ListWorkerRequest request = new ListWorkerRequest();
 		request.setApplicationName(application);
 		request.setDeploymentName(deployment);
 
 		ListWorkerResponse response = client.send(request);
 		System.out.println(response);
 
 		Worker[] workers = response.getWorkers();
 		if (workers != null) {
 			for (int i = 0; i < workers.length; i++) {
				System.out.println(i + "\t" + workers[i]);
 			}
 		}
 	}
 
 	@Test
 	public void testDeleteWorker() {
 		DeleteWorkerRequest request = new DeleteWorkerRequest();
 		request.setApplicationName(application);
 		request.setDeploymentName(deployment);
 		request.setWorkerId(worker);
 
 		DeleteWorkerResponse response = client.send(request);
 		System.out.println(response);
 	}
 
 	@Test
	public void testRemoveAllWorkers() {
 		ListWorkerRequest listRequest = new ListWorkerRequest();
 		listRequest.setApplicationName(application);
 		listRequest.setDeploymentName(deployment);
 		ListWorkerResponse listResponse = client.send(listRequest);
 		Worker[] workers = listResponse.getWorkers();
 		if (workers != null) {
 			for (Worker worker : workers) {
 				System.out.println(worker);
 
 				DeleteWorkerRequest request = new DeleteWorkerRequest();
 				request.setApplicationName(application);
 				request.setDeploymentName(deployment);
 				request.setWorkerId(worker.getWrk_id());
 
 				DeleteWorkerResponse response = client.send(request);
 				System.out.println(response);
 			}
 		}
 	}
 }
