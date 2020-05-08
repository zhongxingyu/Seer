 package org.jboss.tools.fuse.reddeer.view;
 
 import java.util.List;
 
 import org.jboss.reddeer.common.logging.Logger;
 import org.jboss.reddeer.swt.api.TreeItem;
 import org.jboss.reddeer.swt.exception.SWTLayerException;
 import org.jboss.reddeer.swt.impl.menu.ContextMenu;
 import org.jboss.reddeer.swt.impl.tree.DefaultTree;
 import org.jboss.reddeer.swt.wait.AbstractWait;
 import org.jboss.reddeer.swt.wait.TimePeriod;
 import org.jboss.reddeer.workbench.impl.view.WorkbenchView;
 
 /**
  * Performs operations with the Fuse JMX Navigator View
  * 
  * @author tsedmik
  */
 public class JMXNavigator extends WorkbenchView {
 
 	public static final String TITLE = "JMX Navigator";
 	public static final String CONNECT_CONTEXT_MENU = "Connect...";
 
 	private static final Logger log = Logger.getLogger(JMXNavigator.class);
 
 	public JMXNavigator() {
 		super(TITLE);
 	}
 
 	/**
 	 * Tries to connect to a specified node under <i>Local Processes</i> in
 	 * <i>JMX Navigator</i> View.
 	 * 
 	 * @param name
 	 *            prefix name of the node
 	 * @return the desired node or null if the searched node is not present
 	 */
 	public TreeItem connectTo(String name) {
 
 		log.info("Connecting to '" + name + "'.");
 		open();
 		List<TreeItem> items = new DefaultTree().getItems();
 
 		for (TreeItem item : items) {
 			if (item.getText().equals("Local Processes")) {
 				item.expand();
 				items = item.getItems();
 				break;
 			}
 		}				 				
 			 				
 		for (TreeItem item : items) {
 			if (item.getText().contains(name)) {
 				item.select();
 				AbstractWait.sleep(TimePeriod.getCustom(2));
 				try {
 					new ContextMenu(CONNECT_CONTEXT_MENU).select();
 				} catch (SWTLayerException ex) {
 					log.info("Already connected to '" + name + "'.");
 				}
 				AbstractWait.sleep(TimePeriod.getCustom(2));
 				item.expand();
 				return item;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Tries to decide if a particular node described with the path is in the
 	 * tree in <i>JMX Navigator</i> View (below the node <i>Local
 	 * Processes</i>).
 	 * 
 	 * @param path
 	 *            Path to the desired node (names of nodes in the tree in <i>JMX
 	 *            Navigator</i> View without the <i>Local Processes</i> node.
 	 * @return The node if exists, <b>null</b> - otherwise
 	 */
 	public TreeItem getNode(String... path) {
 
 		open();
 		AbstractWait.sleep(TimePeriod.NORMAL);
 		List<TreeItem> items = new DefaultTree().getItems();
 
 		for (TreeItem item : items) {
 			if (item.getText().equals("Local Processes")) {
				item.collapse();
 				AbstractWait.sleep(TimePeriod.SHORT);
 				item.expand();
 				items = item.getItems();
 				break;
 			}
 		}
 		
 		// node 'Local Camel Context' could be sometimes named 'maven [pid]' and more than one maven process can be present
 		// connect to all Maven processes -> the right one will be renamed to 'Local Camel Context'
 		boolean camelContext = false;
 		for (TreeItem item : items) {
 			if (item.getText().contains("Local Camel Context")) {
 				camelContext = true;
 				break;
 			}
 		}
 		if (!camelContext) {
 			for (TreeItem item : items) {
 				if (item.getText().startsWith("maven [") || item.getText().startsWith("karaf")) {
 					item.select();
 					item.doubleClick();
 					expand(item);
 					AbstractWait.sleep(TimePeriod.SHORT);
 				}
 			}
 		}
 
 		for (int i = 0; i < path.length; i++) {
 			for (TreeItem item : items) {
 
 				if (i == 0 &&
 						(path[0].equals("Local Camel Context") && item.getText().startsWith("Local Camel Context") ||
 						 path[0].equals("karaf") && (item.getText().contains(path[i]) || item.getText().startsWith("JBoss Fuse")))) {
 
 					if (i == path.length - 1) {
 						return item;
 					}
 
 					item.select();
 					item.doubleClick();
 					expand(item);
 					AbstractWait.sleep(TimePeriod.SHORT);
 					item.select();
 					items = item.getItems();
 					log.info("Tree Item '" + item + "' has the following children:" + logTreeItems(items));
 					AbstractWait.sleep(TimePeriod.SHORT);
 					break;
 				}
 
 				if (item.getText().startsWith(path[i])) {
 
 					if (i == path.length - 1) {
 						return item;
 					}
 
 					item.select();
 					expand(item);
 					AbstractWait.sleep(TimePeriod.SHORT);
 					item.select();
 					items = item.getItems();
 					log.info("Tree Item '" + item + "' has the following children:" + logTreeItems(items));
 					AbstractWait.sleep(TimePeriod.SHORT);
 					break;
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private void expand(TreeItem item) {
 
 		for (int i = 0; i < 10; i++) {
 			log.info("Trying to expand '" + item.getText() + "' Tree Item");
 			item.expand();
 			AbstractWait.sleep(TimePeriod.SHORT);
 			if (item.isExpanded()) {
 				log.info("Tree Item '" + item.getText() + "' is expanded");
 				return;
 			}
 			log.error("Tree Item '" + item.getText() + "' is NOT expanded!");
 		}
 	}
 
 	private String logTreeItems(List<TreeItem> items) {
 
 		StringBuilder output = new StringBuilder();
 		output.append("\n");
 		for (TreeItem item : items) {
 			output.append(item.getText());
 			output.append("\n");
 		}
 		return output.toString();
 	}
 }
