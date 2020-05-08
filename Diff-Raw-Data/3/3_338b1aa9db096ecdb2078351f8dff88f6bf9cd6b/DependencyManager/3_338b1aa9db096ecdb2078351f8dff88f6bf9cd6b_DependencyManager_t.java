 /**
  * 
  */
 package org.geworkbench.engine.ccm;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.UIManager;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.engine.ccm.CCMTableModel;
 
 /**
  * 
  * The utility to ask for user's confirmation for load/unload dependent
  * components.
  * 
  * @author zji
  * 
  */
 public class DependencyManager {
 	private Log log = LogFactory.getLog(DependencyManager.class);
 
 	private CCMTableModel ccmTableModel = null;
 
 	// only one of these are used
 	private List<String> required = null;
 	private List<Integer> dependentPlugins = null;
 
 	private int selectedRow = -1;
 
 	static JTextPane textPane = new JTextPane();
 	static JScrollPane scrollPane = new JScrollPane();
 
 	static {
 		textPane.setEditable(false);
 		textPane.setFocusable(false);
 	}
 
 	static private enum ChangeFlag {
 		LOAD, UNLOAD
 	};
 
 	ChangeFlag changeFlag = null;
 
 	/**
 	 * The constructor for checking dependent plugins for unloading case.
 	 * 
 	 * @param ccmTableModel
 	 * @param dependentPlugins
 	 * @param selectedRow
 	 */
 	DependencyManager(final CCMTableModel ccmTableModel,
 			final List<Integer> dependentPlugins, final int selectedRow) {
 		changeFlag = ChangeFlag.UNLOAD;
 
 		this.ccmTableModel = ccmTableModel;
 		this.selectedRow = selectedRow;
 		this.dependentPlugins = dependentPlugins;
 
 		String unselectedPluginName = "Plugin is missing a Name descriptor";
 		if (selectedRow >= 0) {
 			unselectedPluginName = ccmTableModel.getPluginName(selectedRow);
 		}
 
 		String message = "The following is a list of plugins known to be\n";
 		message += "dependent on the plugin you have chosen to unload\n";
 		message += "(" + unselectedPluginName + "):\n\n";
 
 		for (int i = 0; i < dependentPlugins.size(); i++) {
 			Integer dependentRow = dependentPlugins.get(i);
 			int row = dependentRow.intValue();
 			String dependentName = ccmTableModel.getPluginName(row);
 
 			message += "* " + dependentName + "\n";
 		}
 		message += "\nIf you choose the \"Continue\" button below, \n";
 		message += "the listed plugins will be automatically be unselected\n";
 		message += "in your Component Configuration Manager window\n";
 		message += "and will be unloaded in the application along\n";
 		message += "with the plugin you unselected.\n";
 		textPane.setText(message);
 		scrollPane.setViewportView(textPane);
 	}
 
 	/**
 	 * The constructor for checking required plugins for loading case.
 	 * 
 	 * @param ccmTableModel
 	 * @param required
 	 * @param selectedRow
 	 * @param related
 	 */
 	DependencyManager(final CCMTableModel ccmTableModel,
 			final List<String> required, final int selectedRow,
 			final List<String> related) {
 		changeFlag = ChangeFlag.LOAD;
 
 		this.ccmTableModel = ccmTableModel;
 		this.required = required;
 		this.selectedRow = selectedRow;
 
 		String pluginName = "Plugin is missing a Name";
 		if (selectedRow >= 0) {
 			pluginName = (String) ccmTableModel.getPluginName(selectedRow);
 		}
 
 		ArrayList<String> requireAndRelated = new ArrayList<String>();
 		/* Format Required Plugins */
 		for (int i = 0; i < required.size(); i++) {
 			String requiredClazz = required.get(i);
 			int requiredRow = ccmTableModel.getModelRowByClazz(requiredClazz);
 
 			String requiredName = "missing Class description";
 			if (requiredRow >= 0) {
 				requiredName = (String) ccmTableModel.getPluginName(
 						requiredRow);
 			}
 
 			String req = "";
 			req += requiredName;
 			req += " (required)";
 			requireAndRelated.add(req);
 		}
 		/* Format Related Plugins */
 		for (int i = 0; i < related.size(); i++) {
 			String relatedClazz = related.get(i);
 			int relatedRow = ccmTableModel.getModelRowByClazz(relatedClazz);
 
 			String relatedName = "missing Class description";
 			if (relatedRow >= 0) {
 				relatedName = (String) ccmTableModel.getPluginName(
 						relatedRow);
 			}
 			requireAndRelated.add(relatedName);
 		}
 		String message = "The following is a list of plugins known to be\n";
 		message += "compatible with the plugin you have chosen to load\n";
 		message += "(" + pluginName + "):\n\n";
 
 		for (int i = 0; i < requireAndRelated.size(); i++) {
 			message += "* " + requireAndRelated.get(i) + "\n";
 		}
 		message += "\nIf you choose the \"Continue\" button below, those\n";
 		message += "plugins marked as \"required\" will be automatically\n";
 		message += "selected in your Component Configuration Manager\n";
 		message += "window and will be uploaded in the application along\n";
		message += "with the plugin you selected. If \"Cancel\" is pushed,\n" +
				"the plugin you chose will not be selected.";
 		textPane.setText(message);
 		scrollPane.setViewportView(textPane);
 	}
 
 	static private JOptionPane optionPane = new JOptionPane(scrollPane,
 			JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,
 			new String[] { "Continue", "Cancel" });
 
 	private void updateDependent() {
 		// if is unload
 		for (int i = 0; i < dependentPlugins.size(); i++) {
 			Integer dependentRow = dependentPlugins.get(i);
 			int row = dependentRow.intValue();
 			ccmTableModel.unselectWithoutValiation(row);
 		}
 	}
 
 	private void updateRequired() {
 		for (int i = 0; i < this.required.size(); i++) {
 			String requiredClazz = this.required.get(i);
 			int requiredRow = ccmTableModel.getModelRowByClazz(requiredClazz);
 			if (requiredRow < 0) {
 				log.error("Missing Class in Plugin");
 			}
 
 			boolean successful = ccmTableModel.selectWithoutValiation(requiredRow);
 
 			/*
 			 * If a license is not agreed to for a dependent component, then
 			 * roll back all the selections for all components
 			 */
 			if (!successful) {
 				rollBack();
 			}
 		}
 	}
 
 	void checkDependency() {
 		// in case other component does not like this
 		Object oldSetting = UIManager.get("Button.defaultButtonFollowsFocus");
 		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
 		JDialog dialog = optionPane.createDialog(null,
 				"Dependency Checking Dialog");
 
 		dialog.setVisible(true);
 		Object obj = optionPane.getValue();
 		UIManager.put("Button.defaultButtonFollowsFocus", oldSetting); // restore
 		if (obj == null) {
 			rollBack();
 			return;
 		}
 		String selectedValue = (String) optionPane.getValue();
 		if (selectedValue.equals("Continue")) {
 			switch (changeFlag) {
 			case LOAD:
 				updateRequired();
 				break;
 			case UNLOAD:
 				updateDependent();
 				break;
 			default:
 				log.error("invalid flag independency manager");
 			}
 			return;
 		} else { // if not "Continue"
 			rollBack();
 		}
 	}
 
 	private void rollBack() {
 		if (selectedRow >= 0) {
 			ccmTableModel.switchSelected(selectedRow);
 		} else {
 			log.error("Missing Class in Plugin");
 		}
 	}
 
 }
