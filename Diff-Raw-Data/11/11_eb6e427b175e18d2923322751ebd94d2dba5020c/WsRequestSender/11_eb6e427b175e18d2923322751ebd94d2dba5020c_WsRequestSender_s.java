 package br.usp.ime.simulation.shared;
 
 import org.simgrid.msg.Host;
 import org.simgrid.msg.HostFailureException;
 import org.simgrid.msg.Msg;
 import org.simgrid.msg.MsgException;
 import org.simgrid.msg.Task;
 import org.simgrid.msg.TimeoutException;
 import org.simgrid.msg.TransferFailureException;
 
 import br.usp.ime.simulation.datatypes.task.WsRequest;
 import br.usp.ime.simulation.experiments.control.ControlVariables;
 
 import commTime.FinalizeTask;
 
 public class WsRequestSender extends org.simgrid.msg.Process {
 
 	public WsRequest request;
 
 	public WsRequestSender(String[] args, Host host) {
 		super(host, "WsRequestSender", args);
 	}
 
 	@Override
 	public void main(String[] args) throws MsgException {
 		String destination = args[1];
 		Task request = null;
 		try {
 			request = WsRequest.fromString(args[0]);
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		if (request instanceof WsRequest) {
 			WsRequest wsrequest = (WsRequest) request;
 			WsRequest clonerequest = cloneWsRequest(wsrequest, destination);
 			if (ControlVariables.DEBUG
 					|| ControlVariables.PRINT_TASK_TRANSMISSION)
 				Msg.info("Created Task for " + clonerequest.serviceMethod
 						+ " with compute duration of "
 						+ clonerequest.getComputeDuration()
 						+ " and message size of "
 						+ clonerequest.inputMessageSize + " at " + destination);
 			clonerequest.send(destination);
 		} else {
 			if (request instanceof FinalizeTask)
 				if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
 					Msg.info(" Terminating service at " + destination);
 				else if (ControlVariables.DEBUG
 						|| ControlVariables.PRINT_ALERTS)
 					Msg.info(" Sending generic task to " + destination);
 			request.send(destination);
 		}
 	}
 
 	private WsRequest cloneWsRequest(WsRequest wsrequest, String destination) {
 		WsRequest clonerequest = new WsRequest(wsrequest.getId(),
 				wsrequest.serviceName, wsrequest.serviceMethod,
 				wsrequest.inputMessageSize, wsrequest.senderMailbox);
 		clonerequest.instanceId = wsrequest.instanceId;
 		clonerequest.senderMailbox = wsrequest.senderMailbox;
 		clonerequest.destination = destination;
 		return clonerequest;
 	}
 
 }
