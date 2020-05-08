 package org.eclipse.dltk.core.internal.rse;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.utils.LazyExtensionManager;
 import org.eclipse.dltk.utils.LazyExtensionManager.Descriptor;
 import org.eclipse.rse.core.model.IHost;
 import org.eclipse.rse.core.subsystems.ISubSystem;
 import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
 
 /**
  * This class is designed to synchronize connections to RSE to display only one
  * connection dialog per time.
  * 
  * @author haiodo
  * @since 2.0
  * 
  */
 public class RSEConnectionQueryManager {
 	private static RSEConnectionQueryManager queryManager = null;
 
 	public static synchronized RSEConnectionQueryManager getInstance() {
 		if (queryManager == null) {
 			queryManager = new RSEConnectionQueryManager();
 		}
 		return queryManager;
 	}
 
 	private static class ConnectionRequest {
 		private IHost host;
 		private long id;
 		private boolean finished = false;
 
 		public ConnectionRequest(IHost host, long id) {
 			this.host = host;
 			this.id = id;
 		}
 
 		public synchronized boolean isFinished() {
 			return finished;
 		}
 
 		public synchronized void setFinished(boolean finished) {
 			this.finished = finished;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((host == null) ? 0 : host.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			ConnectionRequest other = (ConnectionRequest) obj;
 			if (host == null) {
 				if (other.host != null)
 					return false;
 			} else if (!host.equals(other.host))
 				return false;
 			return true;
 		}
 	}
 
 	public interface IConnector {
 		void register();
 
 		boolean isDirectProcessingRequired();
 
 		void runDisplayRunnables();
 	}
 
 	public void setConnectingStatus(boolean status) {
 		synchronized (requests) {
 			isConnecting = status;
 		}
 	}
 
 	public boolean isConnecting() {
 		synchronized (requests) {
 			return isConnecting;
 		}
 	};
 
 	public void markHostAsFinished(IHost host) {
 		synchronized (requests) {
 			List<ConnectionRequest> toRemove = new ArrayList<ConnectionRequest>();
 			for (ConnectionRequest request : requests) {
 				if (request.host.equals(host)) {
 					toRemove.add(request);
 					request.setFinished(true);
 				}
 			}
 			setConnectingStatus(false);
 			if (!toRemove.isEmpty()) {
 				requests.removeAll(toRemove);
 				requests.notifyAll();
 			}
 		}
 	}
 
 	/**
 	 * Get host and set state to isConnecting()
 	 * 
 	 * @return
 	 */
 	public IHost getNextHost(boolean wait) {
 		synchronized (requests) {
 			if (wait) {
 				while (isConnecting() || requests.isEmpty()) {
 					try {
 						requests.wait(100);
 					} catch (InterruptedException e) {
 						DLTKRSEPlugin.log(e);
 					}
 				}
 			}
 			if (!requests.isEmpty()) {
 				ConnectionRequest request = requests.get(0);
 				setConnectingStatus(true);
 				return request.host;
 			}
 			return null;
 		}
 	}
 
 	private LazyExtensionManager<IConnector> connectors = new LazyExtensionManager<IConnector>(
 			DLTKRSEPlugin.PLUGIN_ID + ".rseConnector");
 
 	private List<ConnectionRequest> requests = new ArrayList<ConnectionRequest>();
 	private boolean isConnecting = false;
 	private long currentId;
 
 	public RSEConnectionQueryManager() {
 		Descriptor<IConnector>[] descriptors = connectors.getDescriptors();
 		for (Descriptor<IConnector> descriptor : descriptors) {
 			IConnector connector = descriptor.get();
 			connector.register();
 		}
 	}
 
 	public boolean connectTo(IHost host) {
 		if (isConnected(host)) {
 			return true;
 		}
 		ConnectionRequest request = new ConnectionRequest(host, currentId++);
 		synchronized (requests) {
 			requests.add(request);
 			requests.notifyAll(); // Notify all connectors
 		}
 		// are in display thread.
 		Descriptor<IConnector>[] descriptors = connectors.getDescriptors();
 		for (Descriptor<IConnector> descriptor : descriptors) {
 			IConnector connector = descriptor.get();
 			if (connector.isDirectProcessingRequired()) {
 				connector.runDisplayRunnables();
 			}
 		}
 		synchronized (requests) {
			while (!request.isFinished() && Platform.isRunning()) {
 				try {
 					requests.wait(100);
 				} catch (InterruptedException e) {
 					DLTKRSEPlugin.log(e);
 				}
 			}
 		}
 
 		return isConnected(host);
 	}
 
 	private boolean isConnected(IHost host) {
 		ISubSystem[] subSystems = host.getSubSystems();
 		for (ISubSystem subsystem : subSystems) {
 			if (subsystem instanceof IRemoteFileSubSystem) {
 				return subsystem.isConnected();
 			}
 		}
 		return false;
 	}
 
 	public boolean hasHosts() {
 		synchronized (requests) {
 			return !requests.isEmpty();
 		}
 	}
 }
