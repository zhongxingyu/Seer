 package objective.ng;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashSet;
 import java.util.Set;
 
import objective.ObjectiveUtilsPlugin;

 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.IVMInstall2;
 import org.eclipse.jdt.launching.IVMInstallChangedListener;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jdt.launching.PropertyChangeEvent;
 import org.eclipse.swt.widgets.Display;
 
 import editor.utils.EditorUtils;
 import editor.utils.ObjectiveEclipseUtilsException;
 
 public class GotoMethodServer {
 
 	public void start() {
 		new Thread() {
 			public void run() {
 				try {
 					ServerSocket serverSocket = makeServerSocket();
 					while (true) {
 						handleClientConnection(serverSocket.accept());
 					}
 				} catch (IOException e) {
 					throw new IllegalStateException(e);
 				}
 			}
 		}.start();
 	}
 	
 	protected ServerSocket makeServerSocket() {
 		int port = 47922;
 		int maxRetries = 10;
 		while (maxRetries > 0) {
 			try {
 				ServerSocket serverSocket = new ServerSocket(port);
 				setPortSystemProperty(port);
				ObjectiveUtilsPlugin.log("GotoMethodServer bound to port " + port);
 				return serverSocket;
 			} catch (IOException e) {
 				port++;
 			}
 		}
 		throw new ObjectiveEclipseUtilsException("Could not bind GotoMethodServer to any port after " + maxRetries + " attempts.");
 	}
 
 	private int setPortSystemProperty(final int port) {
 		Set<IVMInstall2> visited = new HashSet<IVMInstall2>();
 		IJavaProject[] allProjects = EditorUtils.getAllProjects();
 		for (IJavaProject iJavaProject : allProjects) {
 			try {
 				IVMInstall2 vmInstall = (IVMInstall2) JavaRuntime.getVMInstall(iJavaProject);
 				if (visited.contains(vmInstall))
 					continue;
 				addGotoServerPortToSystemProperties(port, vmInstall);
 				visited.add(vmInstall);
 			} catch (CoreException e) {
 				throw new ObjectiveEclipseUtilsException(e);
 			}
 		}
 		JavaRuntime.addVMInstallChangedListener(new IVMInstallChangedListenerImplementation(port));
 		
 		return port;
 	}
 
 	private void addGotoServerPortToSystemProperties(int port,
 			IVMInstall2 vmInstall) {
 		String vmArgs = vmInstall.getVMArgs();
 		if (vmArgs == null)
 			vmArgs = "";
 		
 		String gotoServerPropertyName = "GotoMethodServer.port";
 		vmArgs = vmArgs.replaceAll("[ ]*-D"+gotoServerPropertyName+"=\\d+","");
 		vmArgs = vmArgs+ " -D"+gotoServerPropertyName+"=" + port;
 		vmInstall.setVMArgs(vmArgs);
 	}
 
 	private void handleClientConnection(Socket client)
 			throws IOException {
 		try {
 			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
 			String message = in.readLine();
 			processMessage(message);
 		}
 		finally {
 			client.close();
 		}
 	}
 
 	protected void processMessage(final String message) {
 		if (message == null || message.length() == 0) {
 			return;
 		}
 		Display.getDefault().asyncExec(new Runnable() {
 		    @Override
 		    public void run() {
 		    	EditorUtils.goToClassMethod(message);
 		    }
 		});
 	}
 	
 	private final class IVMInstallChangedListenerImplementation implements
 		IVMInstallChangedListener {
 		private final int port;
 		private IVMInstallChangedListenerImplementation(int port) {
 			this.port = port;
 		}
 		@Override
 		public void vmAdded(IVMInstall addedVm) {
 			addGotoServerPortToSystemProperties(port, (IVMInstall2)addedVm);
 		}
 		
 		public void vmRemoved(IVMInstall arg0) { /**/ }
 		public void vmChanged(PropertyChangeEvent arg0) { /**/ }
 		public void defaultVMInstallChanged(IVMInstall arg0, IVMInstall arg1) { /**/}
 	}
 }
