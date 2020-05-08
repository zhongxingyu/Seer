 package br.usp.ime.simulation.orchestration;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.runner.Request;
 import org.simgrid.msg.Msg;
 import org.simgrid.msg.MsgException;
 
 import br.usp.ime.simulation.datatypes.task.ResponseTask;
 import br.usp.ime.simulation.datatypes.task.WsRequest;
 import br.usp.ime.simulation.experiments.control.ControlVariables;
 import br.usp.ime.simulation.log.Log;
 import br.usp.ime.simulation.shared.ServiceInvoker;
 
 import commTime.FinalizeTask;
 
 public class Orchestrator extends ServiceInvoker {
 
 	private String deploymentInfo = "";
 	private String bpelFile = "";
 	private String myMailbox = "Orchestrator";
 	private HashMap<Integer, Orchestration> orchestrationInstances = new HashMap<Integer, Orchestration>();
 	private ArrayList<String> mailboxes = new ArrayList<String>();
 	private Log log = new Log();
 
 	public void main(final String[] args) throws MsgException {
 		if (args.length != 4) {
 			Msg.info("The orchestrator must receive 4 inputs: Request quantity, request per sec rate, orchestration descriptor and ammount of orchestrated services");
 			System.exit(1);
 		}
 		log.open();
 		final int ammountOfInstances = Integer.parseInt(args[0]);
 		if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
 			Msg.info("Starting " + ammountOfInstances + " Orchestrations...");
 		orchestrate(ammountOfInstances);
 	}
 
 	private void orchestrate(final int ammountOfInstances) throws MsgException {
 
 		for (int i = 0; i < ammountOfInstances; i++) {
 			Orchestration orquestration = new Orchestration(i);
 			orquestration.createServiceList(deploymentInfo);
 			orquestration.parseBpelFile(bpelFile);
 			for (Set<String> listEndpoint : orquestration
 					.getServiceMethodsMailboxEndpoints().values())
 				mailboxes.addAll(listEndpoint);
 			orchestrationInstances.put(i, orquestration);
 		}
 
 		if (ControlVariables.DEBUG || ControlVariables.PRINT_TIME_FOR_ORCH_REQ)
 			Msg.info("Beginning Orchestrations...");
 		
 		sendInitialTasks();
 
 		while (!orchestrationInstances.isEmpty()) {
 			ResponseTask response = (ResponseTask) getResponse(myMailbox);
			// startTime = response.requestServed.startTime;
			log.record(response.requestServed.startTime, Msg.getClock(),
					response.serviceMethod);
 			Orchestration orch = orchestrationInstances
 					.get(response.instanceId);
 			if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
 				Msg.info("Task " + response.serviceMethod
 						+ " completed for instance " + response.instanceId);
 
 			orch.notifyTaskConclusion(response.requestServed);
 
 			if (orch.getReadyTasks().isEmpty()) {
 				if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS || ControlVariables.PRINT_TIME_FOR_ORCH_REQ)
 					Msg.info("No more tasks for orchestration " + orch.getId());
 				orchestrationInstances.remove(orch.getId());
 			} else {
 				submitReadyTasks(orch);
 			}
 		}
 
 		finalizeOrchestration();
 	}
 
 	private void sendInitialTasks() throws MsgException {
 		for (Orchestration orch : orchestrationInstances.values()) {
 			submitReadyTasks(orch);
 		}
 		if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
 			Msg.info("Done sending tasks");
 	}
 
 	private void submitReadyTasks(Orchestration orch) throws MsgException {
 		List<WsRequest> readyTasks = orch.getReadyTasks();
 		if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
 			Msg.info(readyTasks.size() + " tasks are ready!");
 		for (WsRequest request : readyTasks) {
 			String chosenMailbox = orch.getWsMailbox(request);
 			if (ControlVariables.DEBUG
 					|| ControlVariables.PRINT_TASK_TRANSMISSION)
 				Msg.info(" Sending request for " + request.serviceMethod
 						+ " to " + chosenMailbox);
 			invokeWsMethod(request, myMailbox, chosenMailbox);
 		}
 	}
 
 	private void finalizeOrchestration() throws MsgException {
 		ArrayList<String> removedMailboxes = new ArrayList<String>();
 		if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
 			Msg.info("Telling everyone the orchestration is done...");
 		for (String mailbox : mailboxes) {
 			if (!removedMailboxes.contains(mailbox)) {
 				FinalizeTask task = new FinalizeTask();
 				if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS
 						|| ControlVariables.PRINT_TASK_TRANSMISSION)
 					Msg.info(" Sending termination signal to " + mailbox);
 				task.send(mailbox);
 				removedMailboxes.add(mailbox);
 			}
 		}
 
 		if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
 			Msg.info("Orchestration is done. Bye!");
 		log.close();
 	}
 
 	@Override
 	public void notifyCompletion(WsRequest request, ResponseTask response)
 			throws MsgException {
 		Orchestration orch = orchestrationInstances.get(response.instanceId);
 		orch.notifyTaskConclusion(request);
 		submitReadyTasks(orch);
 	}
 
 }
