 package org.jboss.tools.fuse.reddeer.server;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.reddeer.common.logging.Logger;
 import org.jboss.reddeer.eclipse.condition.ConsoleHasText;
 import org.jboss.reddeer.eclipse.exception.EclipseLayerException;
 import org.jboss.reddeer.eclipse.wst.server.ui.view.Server;
 import org.jboss.reddeer.eclipse.wst.server.ui.view.ServerLabel;
 import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersView;
 import org.jboss.reddeer.swt.api.Tree;
 import org.jboss.reddeer.swt.api.TreeItem;
 import org.jboss.reddeer.swt.condition.JobIsRunning;
 import org.jboss.reddeer.swt.condition.ShellWithTextIsAvailable;
 import org.jboss.reddeer.swt.exception.SWTLayerException;
 import org.jboss.reddeer.swt.impl.button.PushButton;
 import org.jboss.reddeer.swt.impl.menu.ContextMenu;
 import org.jboss.reddeer.swt.impl.shell.DefaultShell;
 import org.jboss.reddeer.swt.impl.shell.WorkbenchShell;
 import org.jboss.reddeer.swt.impl.text.LabeledText;
 import org.jboss.reddeer.swt.impl.tree.DefaultTree;
 import org.jboss.reddeer.swt.wait.AbstractWait;
 import org.jboss.reddeer.swt.wait.TimePeriod;
 import org.jboss.reddeer.swt.wait.WaitUntil;
 import org.jboss.reddeer.swt.wait.WaitWhile;
 import org.jboss.tools.fuse.reddeer.preference.ServerRuntimePreferencePage;
 import org.jboss.tools.fuse.reddeer.view.FuseShell;
 import org.jboss.tools.fuse.reddeer.wizard.ServerWizard;
 
 /**
  * Performs operation with a Fuse server
  * 
  * @author tsedmik
  */
 public class ServerManipulator {
 
 	private static final Logger log = Logger.getLogger(ServerManipulator.class);
 
 	public static void addServerRuntime(String type, String path) {
 
 		ServerRuntimePreferencePage serverRuntime = new ServerRuntimePreferencePage();
 		serverRuntime.open();
 		serverRuntime.addServerRuntime(type, path);
 		serverRuntime.ok();
 	}
 
 	public static void editServerRuntime(String name, String path) {
 
 		ServerRuntimePreferencePage serverRuntime = new ServerRuntimePreferencePage();
 		serverRuntime.open();
 		serverRuntime.editServerRuntime(name, path);
 		serverRuntime.ok();
 	}
 
 	public static void removeServerRuntime(String name) {
 
 		ServerRuntimePreferencePage serverRuntime = new ServerRuntimePreferencePage();
 		serverRuntime.open();
 		try {
 			serverRuntime.removeServerRuntime(name);
 		} catch (SWTLayerException ex) {
 			log.warn("Cannot remove '" + name + "' server runtime. It is not listed in Server Runtimes!");
 		}
 		serverRuntime.ok();
 	}
 
 	public static void deleteAllServerRuntimes() {
 
 		ServerRuntimePreferencePage serverRuntime = new ServerRuntimePreferencePage();
 		serverRuntime.open();
 		try {
 			for (String runtime : serverRuntime.getServerRuntimes()) {
 				serverRuntime.removeServerRuntime(runtime);
 			}
 		} catch (SWTLayerException ex) {
 		}
 		serverRuntime.ok();
 	}
 
 	public static List<String> getServerRuntimes() {
 
 		ServerRuntimePreferencePage serverRuntime = new ServerRuntimePreferencePage();
 		serverRuntime.open();
 		List<String> temp = serverRuntime.getServerRuntimes();
 		serverRuntime.cancel();
 		return temp;
 	}
 
 	public static void deleteAllServers() {
 
 		ServersView view = new ServersView();
 		view.open();
 		for (Server server : view.getServers()) {
 			server.delete(true);
 		}
 	}
 
 	public static void addServer(String type, String hostname, String name, String portNumber, String userName,
 			String password, String... projects) {
 
 		ServerWizard serverWizard = new ServerWizard();
 		serverWizard.setType(type);
 		serverWizard.setHostName(hostname);
 		serverWizard.setName(name);
 		serverWizard.setPortNumber(portNumber);
 		serverWizard.setUserName(userName);
 		serverWizard.setPassword(password);
 		serverWizard.setProjects(projects);
 		serverWizard.execute();
 	}
 
 	public static void removeServer(String name) {
 
 		try {
 			new ServersView().getServer(name).delete();
 		} catch (EclipseLayerException ex) {
 		}
 	}
 
 	public static void startServer(String name) {
 
 		doOperation("Start", name);
 	}
 
 	public static void debugServer(String name) {
 
 		doOperation("Debug", name);
 	}
 
 	public static void restartInDebug(String name) {
 
 		doOperation("Restart in Debug", name);
 	}
 
 	private static void doOperation(String operation, String name) {
 
 		log.info("Start server " + name);
 		new ServersView().open();
 		for (TreeItem item : new DefaultTree().getItems()) {
 			if (item.getText().startsWith(name)) {
 				item.select();
 				new ContextMenu(operation).select();
 
 				for (int i = 0; i < 10; i++) {
 					AbstractWait.sleep(TimePeriod.SHORT);
 					if (new ShellWithTextIsAvailable("Password Required").test()) {
 						new DefaultShell("Password Required");
 						new LabeledText("Password:").setText("admin");
 						new PushButton("OK").click();
 						AbstractWait.sleep(TimePeriod.SHORT);
 						new WorkbenchShell();
 					}
 				}
 
				new WaitWhile(new JobIsRunning(), TimePeriod.getCustom(300));
 				if (name.toLowerCase().contains("fuse")) {
 					new WaitUntil(new ConsoleHasText("100%"), TimePeriod.getCustom(300));
 				}
 				AbstractWait.sleep(TimePeriod.NORMAL);
 				log.info("Server " + name + " is started");
 				break;
 			}
 		}
 		new WorkbenchShell().setFocus();
 	}
 
 	public static void stopServer(String name) {
 
 		try {
 			new FuseShell().execute("log:clear");
 			ServersView serversView = new ServersView();
 			serversView.open();
 			Server server = serversView.getServer(name);
 			server.stop();
 			new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
 			hack_General_CloseServerTerminateWindow();
 		} catch (Exception ex) {
 		}
 	}
 
 	public static List<String> getServers() {
 
 		ServersView serversView = new ServersView();
 		serversView.open();
 		List<String> temp = new ArrayList<String>();
 		try {
 			Tree tree = new DefaultTree();
 			for (TreeItem item : tree.getItems()) {
 				temp.add(new ServerLabel(item).getName());
 			}
 		} catch (SWTLayerException e) {
 			return temp;
 		}
 
 		return temp;
 	}
 
 	/**
 	 * Checks if the Fuse server is started
 	 * 
 	 * @param name
 	 *            Name of the server
 	 * @return true - a Fuse server is started, false - otherwise
 	 */
 	public static boolean isServerStarted(String name) {
 
 		return new ServersView().getServer(name).getLabel().getState().isRunningState();
 	}
 
 	/**
 	 * Checks if the Fuse server with given name exists
 	 * 
 	 * @param name
 	 *            Name of the Server in Servers View
 	 * @return true - a Fuse server is present, false - otherwise
 	 */
 	public static boolean isServerPresent(String name) {
 
 		try {
 			new ServersView().getServer(name);
 		} catch (EclipseLayerException ex) {
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Removes server and removes server's runtime
 	 * 
 	 * @param name
 	 *            Name of the Server in Servers View
 	 */
 	public static void clean(String name) {
 
 		stopServer(name);
 		removeAllModules(name);
 		removeServer(name);
 		removeServerRuntime(name + " Runtime");
 	}
 
 	/**
 	 * Checks whether a defined server has added a given module (project)
 	 * 
 	 * @param server
 	 *            name of the server in the Servers view
 	 * @param module
 	 *            name of the module
 	 * @return true - the server has added a given module, false - otherwise
 	 */
 	public static boolean hasServerModule(String server, String module) {
 
 		try {
 			new ServersView().getServer(server).getModule(module);
 			AbstractWait.sleep(TimePeriod.SHORT);
 		} catch (EclipseLayerException ex) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Removes all assigned modules to a server
 	 * 
 	 * @param name
 	 *            name of the server in the Servers view
 	 */
 	public static void removeAllModules(String name) {
 
 		ServersView view = new ServersView();
 		view.open();
 		try {
 			view.getServer(name).addAndRemoveModules();
 		} catch (EclipseLayerException ex) {
 			return;
 		}
 
 		// Maybe there is nothing  to remove
 		try {
 			new WaitUntil(new ShellWithTextIsAvailable("Server"));
 			new PushButton("OK").click();
 			return;
 		} catch (Exception e) {
 		}
 
 		// There is something to remove - remove it
 		new DefaultShell("Add and Remove...");
 		FuseModifyModulesPage page = new FuseModifyModulesPage();
 		try {
 			page.removeAll();
 		} catch (Exception ex) {
 			log.debug("Nothing to remove.");
 		}
 		page.close();
 		new WaitWhile(new JobIsRunning(), TimePeriod.NORMAL);
 		new WorkbenchShell().setFocus();
 	}
 
 	/**
 	 * Adds a given project to the server
 	 * 
 	 * @param server
 	 *            name of the server in the Servers view
 	 * @param project
 	 *            name of the project in Project Explorer view
 	 */
 	public static void addModule(String server, String project) {
 
 		ServersView view = new ServersView();
 		view.open();
 		view.getServer(server).addAndRemoveModules();
 		new DefaultShell("Add and Remove...");
 		FuseModifyModulesPage page = new FuseModifyModulesPage();
 		try {
 			page.add(project);
 		} catch (Exception ex) {
 			log.error("Cannot add '" + project + "' project to the server.");
 		}
 		page.close();
 		new WaitWhile(new JobIsRunning(), TimePeriod.NORMAL);
 		AbstractWait.sleep(TimePeriod.NORMAL);
 	}
 
 	/**
 	 * Sets option 'If server is started, publish changes immediately'
 	 * 
 	 * @param name
 	 *            name of the server in the Servers view
 	 * @param value
 	 *            true - option is checked, false - option is not checked
 	 */
 	public static void setImmeadiatelyPublishing(String name, boolean value) {
 
 		new ServersView().getServer(name).addAndRemoveModules();
 		new DefaultShell("Add and Remove...");
 		FuseModifyModulesPage page = new FuseModifyModulesPage();
 		page.setImmeadiatelyPublishing(value);
 		page.close();
 		new WaitWhile(new JobIsRunning(), TimePeriod.NORMAL);
 		AbstractWait.sleep(TimePeriod.SHORT);
 	}
 
 	/**
 	 * Publishes the given server
 	 *
 	 * @param name
 	 *            name of the server in the Servers view
 	 */
 	public static void publish(String name) {
 
 		ServersView view = new ServersView();
 		view.open();
 		view.getServer(name).publish();
 	}
 
 	/**
 	 * If stopping a server takes a long time, <i>Terminate Server</i> window is
 	 * appeared. This method tries to close the window.
 	 */
 	private static void hack_General_CloseServerTerminateWindow() {
 
 		try {
 			new DefaultShell("Terminate Server").setFocus();
 			new PushButton("OK").click();
 		} catch (Exception e) {
 			log.info("Window 'Terminate Server' didn't appeared.");
 		}
 	}
 }
