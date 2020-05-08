 package de.reneruck.tcd.ipp.database.actions;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import de.reneruck.tcd.ipp.datamodel.Callback;
 import de.reneruck.tcd.ipp.datamodel.Datagram;
 import de.reneruck.tcd.ipp.datamodel.Statics;
 import de.reneruck.tcd.ipp.datamodel.transition.TemporalTransitionsStore;
 import de.reneruck.tcd.ipp.datamodel.transition.Transition;
 import de.reneruck.tcd.ipp.datamodel.transition.TransitionExchangeBean;
 import de.reneruck.tcd.ipp.datamodel.transition.TransitionState;
 import de.reneruck.tcd.ipp.fsm.Action;
 import de.reneruck.tcd.ipp.fsm.TransitionEvent;
 
 public class SendData implements Action, Callback {
 
 	private ObjectOutputStream out;
 	private DataSender sender;
 	private Map<Long, Transition> dataset = new HashMap<Long, Transition>();
 	private TemporalTransitionsStore transitionsStore;
 	private TransitionExchangeBean bean;
	private boolean rxModeAck = false;
 
 	public SendData(TransitionExchangeBean transitionExchangeBean, TemporalTransitionsStore transitionsStore ) {
 		this.bean = transitionExchangeBean;
 		this.transitionsStore = transitionsStore;
 	}
 
 	@Override
 	public void execute(TransitionEvent event) throws Exception {
 		if(this.out == null) {
 			this.out = this.bean.getOut();
 		}
		if(!this.rxModeAck) {
			send(Statics.RX_HELI_ACK); 
			this.rxModeAck = true;
		}
 		
 		if(this.sender == null) {
 			initializeDataSender();
 		}
 		
 		if(Statics.ACK.equals(event.getIdentifier())) {
 			Object parameter = event.getParameter(Statics.TRAMSITION_ID);
 			if(parameter != null && parameter instanceof Long) {
 				this.dataset.remove(parameter);
 			}
 		}
 	}
 
 	private void initializeDataSender() {
 		createDataset();
 		this.sender = new DataSender(this.out, this.dataset, this);
 		this.sender.start();
 	}
 
 	private void createDataset() {
 		Set<Transition> allTransitionsByState = this.transitionsStore.getAllTransitionsByState(TransitionState.PROCESSED);
 		for (Transition transition : allTransitionsByState) {
 			this.dataset.put(transition.getTransitionId(), transition);
 		}
 	}
 
 	@Override
 	public void handleCallback() {
 		this.sender = null;
 		try {
 			this.bean.getFsm().handleEvent(new TransitionEvent(Statics.FINISH_RX_HELI));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void send(String message) throws IOException {
		System.out.println("Sending> " + message);
 		this.out.writeObject(new Datagram(message));
 		this.out.flush();
 	}
 
 }
