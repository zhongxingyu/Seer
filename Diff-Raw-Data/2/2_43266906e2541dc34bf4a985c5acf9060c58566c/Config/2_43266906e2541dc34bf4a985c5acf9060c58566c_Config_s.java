 package x10.osgi;
 
 import java.io.IOException;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedServiceFactory;
 
 import x10.CM11ASerialController;
 import x10.CM17ASerialController;
 import x10.Controller;
 import x10.net.SocketController;
 
 public class Config implements ManagedServiceFactory {
 	// String (pid) -> ControllerWrap
 	Map<String, ControllerWrap> controllers = new HashMap<String, ControllerWrap>();
 
 	@SuppressWarnings("rawtypes")
 	public void updated(String pid, Dictionary props)
 			throws ConfigurationException {
 		System.out.println("updated " + pid + ", " + props);
 
 		try {
 			synchronized (controllers) {
 				ControllerWrap wrap = (ControllerWrap) controllers.get(pid);
 
 				if (wrap != null) {
 					wrap.stop();
 					controllers.remove(pid);
 				}
 
 				String port = (String) props.get("port");
 				String module = (String) props.get("module");
 
 				module = module.toLowerCase();
 
 				wrap = new ControllerWrap(pid, port, module);
 				controllers.put(pid, wrap);
 				wrap.register();
 			}
 		} catch (ConfigurationException e) {
 			e.printStackTrace();
 			throw e;
 		}
 	}
 
 	public void deleted(String pid) {
 		Activator.logger.info("delete pid=" + pid);
 		synchronized (controllers) {
 			ControllerWrap wrap = (ControllerWrap) controllers.get(pid);
 			if (wrap != null) {
 				wrap.stop();
 				controllers.remove(pid);
 			}
 		}
 	}
 
 	public void stop() {
 		synchronized (controllers) {
 			for (ControllerWrap wrap : controllers.values()) {
 				if (wrap != null)
 					wrap.stop();
 			}
 			controllers.clear();
 		}
 	}
 
 	public String getName() {
 		return "Creates instances of x10.Controller";
 	}
 }
 
 class ControllerWrap {
 
 	String pid;
 	String port;
 	String module;
 	Controller controller;
 
 	ServiceRegistration<?> reg;
 
 	ControllerWrap(String pid, String port, String module)
 			throws ConfigurationException {
 
 		if (port == null || "".equals(port)) {
 			throw new ConfigurationException("port", "port must be specified");
 		}
 
 		if (module == null || "".equals(module)) {
 			throw new ConfigurationException("module",
 					"module must be specified");
 		}
 
 		this.pid = pid;
 		this.port = port;
 		this.module = module;
 
 		try {
 			if ("cm11a".equals(module)) {
 				controller = new CM11ASerialController(port);
 			} else if ("firecracker".equals(module) || "cm17a".equals(module)) {
 				controller = new CM17ASerialController(port);
 			} else if ("socket".equals(module)) {
				String[] parts = module.split(":");
 				if (parts.length == 2) {
 					String host = parts[0];
 					int netPort = Integer.parseInt(parts[1]);
 					controller = new SocketController(host, netPort);
 				} else {
 					throw new ConfigurationException("module",
 							"Invalid port specification for SocketControler. Should be like localhost:9000");
 				}
 			} else {
 				throw new ConfigurationException("module",
 						"Unsupported module (valid modules are cm11a, cm17a and firecracker");
 			}
 		} catch (IOException e) {
 			throw new ConfigurationException("port", "IO failure on port="
 					+ port + ", " + e);
 		}
 		Activator.logger.info("created " + this);
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	void register() {
 		if (reg != null) {
 			return;
 		}
 		Hashtable props = new Hashtable();
 		props.put("port", port);
 		props.put("module", module);
 
 		reg = Activator.bc.registerService(
 				new String[] { Controller.class.getName(),
 						controller.getClass().getName() }, controller, props);
 
 		Activator.logger.info("registered " + this);
 	}
 
 	void unregister() {
 		if (reg != null) {
 			reg.unregister();
 			reg = null;
 			Activator.logger.info("unregistered " + this);
 		}
 	}
 
 	void stop() {
 		unregister();
 		if (controller instanceof CM11ASerialController) {
 			CM11ASerialController c = (CM11ASerialController) controller;
 			try {
 				c.shutdown(10 * 1000);
 			} catch (Exception e) {
 				Activator.logger.error("Clean shutdown of CM11A failed", e);
 				try {
 					c.shutdownNow();
 				} catch (Exception e2) {
 					Activator.logger.error("Forced shutdown of CM11A failed",
 							e2);
 				}
 			}
 		} else if (controller instanceof CM17ASerialController) {
 			CM17ASerialController c = (CM17ASerialController) controller;
 			try {
 				c.shutdown(10 * 1000);
 			} catch (Exception e) {
 				Activator.logger.error("Clean shutdown of CM17A failed", e);
 				try {
 					c.shutdownNow();
 				} catch (Exception e2) {
 					Activator.logger.error("Forced shutdown of CM17A failed",
 							e2);
 				}
 			}
 		}
 		controller = null;
 		Activator.logger.info("stopped " + this);
 	}
 
 	public String toString() {
 		return "ControllerWrap[" + "pid=" + pid + ", port=" + port
 				+ ", module=" + module + ", controller=" + controller + "]";
 	}
 }
