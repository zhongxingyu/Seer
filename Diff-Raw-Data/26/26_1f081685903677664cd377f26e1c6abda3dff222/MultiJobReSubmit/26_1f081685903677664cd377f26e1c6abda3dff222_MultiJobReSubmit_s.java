 package org.vpac.grisu.frontend.examples;
 
 import java.util.Date;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.bushe.swing.event.EventBus;
 import org.bushe.swing.event.annotation.AnnotationProcessor;
 import org.bushe.swing.event.annotation.EventSubscriber;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.frontend.control.login.LoginParams;
 import org.vpac.grisu.frontend.control.login.ServiceInterfaceFactory;
 import org.vpac.grisu.frontend.model.StatusObject;
 import org.vpac.grisu.frontend.model.events.BatchJobEvent;
 import org.vpac.grisu.frontend.model.events.SystemOutStatusLogger;
 import org.vpac.grisu.frontend.model.job.JobObject;
 import org.vpac.grisu.frontend.model.job.JobsException;
 import org.vpac.grisu.frontend.model.job.BatchJobObject;
 import org.vpac.grisu.model.GrisuRegistry;
 import org.vpac.grisu.model.GrisuRegistryManager;
 import org.vpac.grisu.model.dto.DtoActionStatus;
 import org.vpac.grisu.model.dto.DtoBatchJob;
 
 import au.org.arcs.jcommons.constants.Constants;
 
 public class MultiJobReSubmit {
 
 	public MultiJobReSubmit() {
 		AnnotationProcessor.process(this);
 	}
 
 	public static void main(final String[] args) throws Exception {
 
 		ExecutorService executor = Executors.newFixedThreadPool(10);
 
 		String username = args[0];
 		char[] password = args[1].toCharArray();
 
 		LoginParams loginParams = new LoginParams(
 		// "http://localhost:8080/xfire-backend/services/grisu",
 				// "https://ngportal.vpac.org/grisu-ws/soap/EnunciateServiceInterfaceService",
 				// "https://ngportal.vpac.org/grisu-ws/services/grisu",
 				// "https://ngportal.vpac.org/grisu-ws/soap/GrisuService",
 				// "http://localhost:8080/enunciate-backend/soap/GrisuService",
 				"Local",
 				// "ARCS_DEV",
 				// "Dummy",
 				username, password);
 
 		final ServiceInterface si = ServiceInterfaceFactory
 				.createInterface(loginParams);
 
 		final GrisuRegistry registry = GrisuRegistryManager.getDefault(si);
 
 		final int numberOfJobs = 10;
 
 		Date start = new Date();
		final String multiJobName = "cat_restart500";
 
 		si.restartBatchJob(multiJobName, null, null);
 
 		BatchJobObject multiPartJob = new BatchJobObject(si, multiJobName, true);
 
 		while (!multiPartJob.isFinished(true)) {
 			System.out.println("Not finished yet...");
 			multiPartJob.getJobs().size();
 			System.out.println(multiPartJob.getDetails());
 			Thread.sleep(2000);
 		}
 
 		for (JobObject job : multiPartJob.getJobs()) {
 			System.out.println("-------------------------------");
 			System.out.println(job.getJobname() + ": "
 					+ job.getStatusString(false));
 			System.out.println(job.getStdOutContent());
 			System.out.println("-------------------------------");
 			System.out.println(job.getStdErrContent());
 			System.out.println("-------------------------------");
 			System.out.println();
 		}
 	}
 
 	@EventSubscriber(eventClass = BatchJobEvent.class)
 	public void onMultiPartJobEvent(BatchJobEvent event) {
 
 		System.out.println("Event: " + event.getMessage());
 
 	}
 
 }
