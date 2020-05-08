 package ch.cern.atlas.apvs.eventbus.client;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.List;
 
 import org.atmosphere.gwt.client.AtmosphereClient;
 import org.atmosphere.gwt.client.AtmosphereGWTSerializer;
 import org.atmosphere.gwt.client.AtmosphereListener;
 
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEvent;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 import com.google.gwt.core.client.GWT;
 
 public class AtmosphereEventBus extends RemoteEventBus {
 
 	private AtmosphereClient client;
 
 	public AtmosphereEventBus(AtmosphereGWTSerializer serializer) {
 		AtmosphereEventBusListener cometListener = new AtmosphereEventBusListener();
 		
 		client = new AtmosphereClient(GWT.getModuleBaseURL() + "eventBusComet",
 				serializer, cometListener);
 		client.start();
 		
 		getServerEvent();
 	}
 
 	private void getServerEvent() {
 		// FIXME do an async call, server implements suspend, and answers eventually when a remote even needs
 		// to be sent.
 	}
 
 	/**
 	 * broadcast event and (receive it locally to distribute, below)
 	 * 
 	 */
 	@Override
 	public void fireEvent(RemoteEvent<?> event) {
 		client.broadcast(event);
 	}
 	
 	/**
 	 * broadcast event and (receive it locally to distribute, below)
 	 * FIXME source is ignored
 	 * 
 	 */
 	@Override
 	public void fireEventFromSource(RemoteEvent<?> event, int uuid) {
 		client.broadcast(event);
 	}
 
 	public class AtmosphereEventBusListener implements AtmosphereListener {
 
 		@Override
 		public void onConnected(int heartbeat, int connectionID) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onBeforeDisconnected() {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onDisconnected() {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onError(Throwable exception, boolean connected) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onHeartbeat() {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onRefresh() {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void onAfterRefresh() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		/**
 		 * handle broadcasted events from other clients
 		 */
 		@Override
		public void onMessage(List<? extends Serializable> messages) {
 			for (Iterator<?> i = messages.iterator(); i.hasNext(); ) {
 				Object message = i.next();
 				if (message instanceof RemoteEvent<?>) {
 					RemoteEvent<?> event = (RemoteEvent<?>)message;
 					
 					// NOTE: also my own needs to be distributed locally
 					AtmosphereEventBus.super.fireEvent(event);
 				}
 			}
 		}
 	}
 }
