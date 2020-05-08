 package org.eclipse.jst.server.servicemix;
 
 import java.io.IOException;
 
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.jst.server.generic.core.internal.GenericServerBehaviour;
 import org.eclipse.wst.server.core.IServer;
 
 /**
  * Eclipse JST Server Behaviour for Apache ServiceMix
  * 
  * Basically just extends the Generic to allow for the pinging of the server
  * during start-up to use the JMX access to determine whether the server has
  * started
  * 
  * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
  * 
  */
 public class ServiceMixServerBehaviour extends GenericServerBehaviour {
 	
 	private static final String ATTR_STOP = "stop-server";
 
 	private JMXPingThread jmxPingThread;
 
 	public void stop(boolean force) {
 		if (jmxPingThread != null)
 			jmxPingThread.stop();
 		super.stop(force);
 	}
 
 	protected void setupLaunch(ILaunch launch, String launchMode,
 			IProgressMonitor monitor) throws CoreException {
 		if ("true".equals(launch.getLaunchConfiguration().getAttribute(
 				ATTR_STOP, "false")))
 			return;
 
 		setServerState(IServer.STATE_STARTING);
 		setMode(launchMode);
 		jmxPingThread = new JMXPingThread(getServer(), this);
 	}
 
 	protected JMXConnector getJMXConnector() throws CoreException, IOException {
		String jndiPath = "jmxrmi";
 		JMXServiceURL url;
 		url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/"
 				+ jndiPath);
 		JMXConnector connector = JMXConnectorFactory.connect(url);
 		return connector;
 	}
 
 	public void setStarted() {
 		setServerState(IServer.STATE_STARTED);
 	}
 }
