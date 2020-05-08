 package org.eclipse.dltk.internal.debug.ui;
 
 import java.util.HashMap;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.model.IValue;
 import org.eclipse.debug.ui.IValueDetailListener;
 import org.eclipse.dltk.core.SimpleDLTKExtensionManager;
 import org.eclipse.dltk.core.SimpleDLTKExtensionManager.ElementInfo;
 import org.eclipse.dltk.debug.core.eval.IScriptEvaluationCommand;
 import org.eclipse.dltk.debug.core.eval.IScriptEvaluationListener;
 import org.eclipse.dltk.debug.core.eval.IScriptEvaluationResult;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.debug.core.model.IScriptValue;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 
 public class ScriptDetailFormattersManager {
 	private static final String DEFAULT_FORMATTER_TYPE = "#DEFAULT#";
 	private static final String ATTR_SNIPPET = "snippet";
 	private static final String ATTR_TYPE = "type";
 	private static final String ATTR_NATURE = "nature";
 	private static final String SCRIPT_DETAIL_FORMATTER_EXTENSION = DLTKDebugUIPlugin.PLUGIN_ID
 			+ ".scriptDetailFormatter";
 
 	private static HashMap managerInstances = new HashMap();
 	private static final String CANNOT_EVALUATE = "Can't evaluate details.";
 	private HashMap formatters = new HashMap();
 	private DetailFormatter defaultFormatter = null;
 
 	/**
 	 * Return the default detail formatters manager.
 	 * 
 	 * @param natureId
 	 * 
 	 * @return default detail formatters manager.
 	 */
 	static public ScriptDetailFormattersManager getDefault(String natureId) {
 		ScriptDetailFormattersManager instance = (ScriptDetailFormattersManager) managerInstances
 				.get(natureId);
 		if (instance == null) {
 			instance = new ScriptDetailFormattersManager(natureId);
 			managerInstances.put(natureId, instance);
 		}
 		return instance;
 	}
 
 	public ScriptDetailFormattersManager(String natureId) {
 		populateDetailFormatters(natureId);
 	}
 
 	private void populateDetailFormatters(String natureId) {
 		SimpleDLTKExtensionManager manager = new SimpleDLTKExtensionManager(
 				SCRIPT_DETAIL_FORMATTER_EXTENSION);
 		ElementInfo[] infos = manager.getElementInfos();
 		for (int i = 0; i < infos.length; i++) {
 			IConfigurationElement config = infos[i].getConfig();
 			String nature = config.getAttribute(ATTR_NATURE);
 			if (natureId.equals(nature)) {
 				String code = config.getAttribute(ATTR_SNIPPET);
 				String type = config.getAttribute(ATTR_TYPE);
 				DetailFormatter formatter = new DetailFormatter(type, code,
 						true);
 				if (DEFAULT_FORMATTER_TYPE.equals(type)) {
 					setDefaultFormatter(formatter);
 				} else {
 					addFormatter(formatter);
 				}
 			}
 		}
 	}
 
 	private String getValueText(IValue value) {
 		try {
 			return value.getValueString();
 		} catch (DebugException e) {
 			return e.getMessage();
 		}
 	}
 
 	public void computeValueDetail(final IScriptValue value,
 			final IScriptThread thread, final IValueDetailListener listener) {
 		Runnable postEventDispatch = new Runnable() {
 			public void run() {
 				DetailFormatter formatter = getDetailFormatter(value);
 				if (thread == null || !thread.isSuspended()
 						|| formatter == null || !formatter.isEnabled()) {
 					listener.detailComputed(value, getValueText(value));
 				} else {
 					final IScriptEvaluationCommand command = value
 							.createEvaluationCommand(formatter.getSnippet(),
 									thread);
 					if (command != null) {
 						command.asyncEvaluate(new IScriptEvaluationListener() {
 							public void evaluationComplete(
 									IScriptEvaluationResult result) {
 								if (result == null)
 									return;
 
 								IScriptValue resultValue = result.getValue();
 								if (resultValue != null) {
 									listener.detailComputed(value,
 											getValueText(resultValue));
 								} else {
									listener.detailComputed(value,
											CANNOT_EVALUATE);
 								}
 							}
 						});
 					}
 				}
 			}
 		};
 		DebugPlugin.getDefault().asyncExec(postEventDispatch);
 	}
 
 	protected DetailFormatter getDetailFormatter(IScriptValue value) {
 		DetailFormatter formatter = (DetailFormatter) formatters.get(value
 				.getType().getName());
 		if (formatter == null)
 			formatter = getDefaultFormatter();
 		return formatter;
 	}
 
 	private DetailFormatter getDefaultFormatter() {
 		return defaultFormatter;
 	}
 
 	public void setDefaultFormatter(DetailFormatter formatter) {
 		defaultFormatter = formatter;
 	}
 
 	public void addFormatter(DetailFormatter formatter) {
 		formatters.put(formatter.getTypeName(), formatter);
 	}
 
 	public void removeFormatter(DetailFormatter formatter) {
 		formatters.remove(formatter.getTypeName());
 	}
 }
