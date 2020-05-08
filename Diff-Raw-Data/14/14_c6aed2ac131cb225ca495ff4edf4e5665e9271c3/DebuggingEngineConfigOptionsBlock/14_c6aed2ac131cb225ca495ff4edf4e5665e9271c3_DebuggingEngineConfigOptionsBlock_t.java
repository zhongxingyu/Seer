 package org.eclipse.dltk.debug.ui.preferences;
 
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
 import org.eclipse.dltk.ui.environment.EnvironmentPathBlock;
 import org.eclipse.dltk.ui.preferences.AbstractOptionsBlock;
 import org.eclipse.dltk.ui.preferences.PreferenceKey;
 import org.eclipse.dltk.ui.util.IStatusChangeListener;
 import org.eclipse.dltk.ui.util.SWTFactory;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
 
 /**
  * Options block for debugging engine preferences.
  */
 public abstract class DebuggingEngineConfigOptionsBlock extends
 		AbstractOptionsBlock {
 
 	private EnvironmentPathBlock logFilePaths;
 //	private Text logFileName;
 //	private Text logFilePath;
 
 	public DebuggingEngineConfigOptionsBlock(IStatusChangeListener context,
 			IProject project, PreferenceKey[] allKeys,
 			IWorkbenchPreferenceContainer container) {
 		super(context, project, allKeys, container);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.preferences.AbstractOptionsBlock#createOptionsBlock(org.eclipse.swt.widgets.Composite)
 	 */
 	protected final Control createOptionsBlock(Composite parent) {
 		final Composite composite = SWTFactory.createComposite(parent, parent
 				.getFont(), 1, 1, GridData.FILL);
 		createEngineBlock(composite);
 		createLoggingBlock(composite);
 		createOtherBlock(composite);
 
 		return composite;
 	}
 
 	/**
 	 * Creates the options block for engine specific preferences/settings.
 	 * 
 	 * <p>
 	 * Subclasses may provide an empty implementation if their debugging engine
 	 * does not have any preferences/settings
 	 * </p>
 	 * 
 	 * @param parent
 	 *            composite
 	 */
 	protected abstract void createEngineBlock(Composite parent);
 
 	/**
 	 * Creates the options block that controls debugging engine logging.
 	 * 
 	 * <p>
 	 * Subclasses may override this method, providing an empty implementation if
 	 * their debugging engine does not support logging. This block will be
 	 * placed after the 'engine options' block.
 	 * </p>
 	 * 
 	 * @param parent
 	 *            composite
 	 */
 	protected void createLoggingBlock(final Composite parent) {
 		final Group group = SWTFactory.createGroup(parent,
 				ScriptDebugPreferencesMessages.LoggingGroupLabel, 3, 1,
				GridData.FILL_BOTH);
 		logFilePaths = new EnvironmentPathBlock();
 		logFilePaths.createControl(group);
 		Map paths = EnvironmentPathUtils
 				.decodePaths(getString(getLogFileNamePreferenceKey()));
 		logFilePaths.setPaths(paths);
 
 		// enableLogging = SWTFactory.createCheckButton(group,
 		// ScriptDebugPreferencesMessages.EnableLoggingLabel, null, false,
 		// 1);
 		// SWTFactory.createLabel(group,
 		// ScriptDebugPreferencesMessages.LogNameFormatLabel, 5, 2);
 		//
 		// SWTFactory.createLabel(group,
 		// ScriptDebugPreferencesMessages.LogNameLabel, 1);
 		// logFileName = SWTFactory.createText(group, SWT.BORDER, 2, "");
 		// //$NON-NLS-1$
 		//
 		// SWTFactory.createLabel(group,
 		// ScriptDebugPreferencesMessages.LogFolderLabel, 1);
 		// logFilePath = SWTFactory.createText(group, SWT.BORDER, 1, "");
 		// //$NON-NLS-1$
 		// logFilePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		//
 		// Button browseButton = SWTFactory.createPushButton(group,
 		// ScriptDebugPreferencesMessages.BrowseButton, null);
 		// browseButton.addSelectionListener(new SelectionAdapter() {
 		// public void widgetSelected(SelectionEvent e) {
 		// DirectoryDialog dialog = new DirectoryDialog(parent.getShell(),
 		// SWT.OPEN);
 		// String dir = dialog.open();
 		// if (dir != null) {
 		// logFilePath.setText(dir);
 		// }
 		// }
 		// });
 		//
 		// Control[] slaves = { logFileName, logFilePath, browseButton };
 		//
 		// bindControl(enableLogging, getEnableLoggingPreferenceKey(), slaves);
 		// bindControl(logFileName, getLogFileNamePreferenceKey(),
 		// FieldValidators.FILE_NAME_VALIDATOR);
 		// bindControl(logFilePath, getLogFilePathPreferenceKey(),
 		// FieldValidators.DIR_VALIDATOR);
 	}
 
 	protected boolean processChanges(IWorkbenchPreferenceContainer container) {
 		String loggingPaths = EnvironmentPathUtils.encodePaths(logFilePaths.getPaths());
 		setString(getLogFileNamePreferenceKey(), loggingPaths);
 		return super.processChanges(container);
 	}
 
 	/**
 	 * Creates an an options block for items that fall into the 'other' category -
 	 * ie: adding a link to download an external debugging engine.
 	 * 
 	 * <p>
 	 * This block will be placed after the 'logging options' block.
 	 * </p>
 	 * 
 	 * @param parent
 	 */
 	protected void createOtherBlock(Composite parent) {
 		// empty implementation
 	}
 
 	/**
 	 * Returns the enable logging preference key
 	 * 
 	 * <p>
 	 * Note: this preference controls logging for the actual debugging engine,
 	 * and not the DBGP protocol output.
 	 * </p>
 	 */
 	protected abstract PreferenceKey getEnableLoggingPreferenceKey();
 
 	/**
 	 * Returns the log file name preference key
 	 */
 	protected abstract PreferenceKey getLogFileNamePreferenceKey();
 
 	/**
 	 * Returns the log file path preference key
 	 */
 	protected abstract PreferenceKey getLogFilePathPreferenceKey();
 }
