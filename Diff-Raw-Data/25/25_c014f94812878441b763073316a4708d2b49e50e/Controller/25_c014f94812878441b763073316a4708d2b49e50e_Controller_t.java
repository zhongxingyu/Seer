 package com.simpoir.elixy;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.Arrays;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.zkoss.zk.ui.Desktop;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zul.Messagebox;
 import org.zkoss.zul.SimpleTreeModel;
 import org.zkoss.zul.SimpleTreeNode;
 import org.zkoss.zul.api.Treeitem;
 
 import com.simpoir.elixy.view.LocalTreeNode;
import com.simpoir.zk.vt.VirtTerm;
 
 public class Controller {
 
 	private static Log _log = LogFactory.getLog(Controller.class);
 	private static Controller _instance = null;
 	private static Properties _props = null;
 
 	public static Controller getInstance() {
 		if (null == _instance) {
 			_instance = new Controller();
 		}
 		return _instance;
 	}
 
 	public static Properties getProps() {
 		if (null == _props) {
 			_props = new Properties();
 			try {
 				_props.load(Controller.class.getResourceAsStream(
 						"build.properties"));
 			} catch (IOException e) {
 				_log.fatal("build.properties not found in build");
 			}
 
 			// load custom config
 			String cfgFile = _props.getProperty("CONFIG_FILE");
 			if (null != cfgFile) {
 				try {
 					_props.load(new FileInputStream(cfgFile));
 				} catch (IOException e) {
 					_log.fatal(MessageFormat.format(
 							"Configuration {0} could not be loaded.",
 							new Object[] {cfgFile}));
 				}
 			}
 		}
 		return _props;
 	}
 
 	public void start(Treeitem item) {
 		String host = item.getParentItemApi().getLabel();
 		String container = item.getLabel();
 		_log.info(MessageFormat.format(
 				"Starting container named {1} on {0}",
 				host, container));
 		new LocalContainerControl(host, container).start();
 	}
 
 	public void stop(Treeitem item) {
 		String host = item.getParentItemApi().getLabel();
 		String container = item.getLabel();
 		_log.info(MessageFormat.format(
 				"Stopping container named {1} on {0}",
 				host, container));
 		new LocalContainerControl(host, container).stop();
 	}
 
 	public LocalContainerControl.State getState(Treeitem item) {
 		String host = item.getParentItemApi().getLabel();
 		String container = item.getLabel();
 		_log.info(MessageFormat.format(
 				"Fetching status of container named {1} on {0}",
 				host, container));
 		return new LocalContainerControl(host, container).getState();
 	}
 
	public void attach(VirtTerm console, Treeitem item)
 			throws InterruptedException {
 		String host = item.getParentItemApi().getLabel();
 		String container = item.getLabel();
 		final Desktop desktop = Executions.getCurrent().getDesktop();
 		if (desktop.isServerPushEnabled()) {
 			Messagebox.show("Alreay attached");
 		} else {
 			desktop.enableServerPush(true);
 			new ThreadedConsole(desktop, console,
 					new LocalContainerControl(host, container).getConsole()).start();
 		}
 	}
 
 	public void detach() throws InterruptedException {
 		final Desktop desktop = Executions.getCurrent().getDesktop();
 		if (desktop.isServerPushEnabled()) {
 			desktop.enableServerPush(false);
 		} else {
 			Messagebox.show("Alreay detached");
 		}
 	}
 
 	public SimpleTreeModel getTreeModel() {
 		// TODO proper load the tree model
 		SimpleTreeNode[] nodes = new SimpleTreeNode[] { new LocalTreeNode() };
 		return new SimpleTreeModel(
 				new SimpleTreeNode("ROOT", Arrays.asList(nodes)));
 	}
 
 	public void doSomething() {
 	}
 
 }
