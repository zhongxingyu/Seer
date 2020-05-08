 package org.jboss.tools.runtime.reddeer;
 
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.ServerCore;
 import org.jboss.reddeer.eclipse.wst.server.ui.view.Server;
 import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersView;
 import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersViewEnums.ServerState;
 import org.jboss.reddeer.requirements.server.ServerReqState;
 
 /**
  * 
  * @author apodhrad
  * 
  */
 public abstract class ServerBase extends RuntimeBase {
 
 	public void setState(ServerReqState requiredState) {
 		ServersView serversView = new ServersView();
 		serversView.open();
 		Server server = serversView.getServer(name);
 
 		ServerState currentState = server.getLabel().getState();
 		switch (currentState) {
 		case STARTED:
 			if (requiredState == ServerReqState.STOPPED)
 				server.stop();
 			break;
 		case STOPPED:
			if (requiredState == ServerReqState.RUNNING) {
				try {
					server.start();
				} catch (Exception e) {
					try {
						server = new ServersView().getServer(name);
						server.stop();
					} catch (Exception ex) {

					}
					server = new ServersView().getServer(name);
					server.start();
				}
			}
 			break;
 		default:
 			new AssertionError("It was expected to have server in " + ServerState.STARTED + " or "
 					+ ServerState.STOPPED + "state." + " Not in state " + currentState + ".");
 		}
 	}
 
 	@Override
 	public boolean exists() {
 		IServer[] server = ServerCore.getServers();
 		for (int i = 0; i < server.length; i++) {
 			if (server[i].getId().equals(name)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
