 package org.eclipse.dltk.debug.ui.preferences;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
 import org.eclipse.dltk.ui.environment.EnvironmentPathBlock;
 import org.eclipse.dltk.ui.preferences.PreferenceKey;
 import org.eclipse.dltk.ui.util.IStatusChangeListener;
 import org.eclipse.dltk.ui.util.SWTFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.browser.IWebBrowser;
 import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
 
 /**
  * Options block for external debugging engine that require the user to specify
  * their location on disk.
  */
 public abstract class ExternalDebuggingEngineOptionsBlock extends
 		DebuggingEngineConfigOptionsBlock {
 
 	EnvironmentPathBlock enginePaths;
 
 	public ExternalDebuggingEngineOptionsBlock(IStatusChangeListener context,
 			IProject project, PreferenceKey[] allKeys,
 			IWorkbenchPreferenceContainer container) {
 		super(context, project, allKeys, container);
 	}
 
 	/**
 	 * Add a link to an external site where the debugging engine can be
 	 * downloaded from
 	 * 
 	 * @param parent
 	 *            parent composite
 	 * @param text
 	 *            link text
 	 * @param url
 	 *            link url
 	 */
 	protected void addDownloadLink(Composite parent, String text,
 			final String url) {
 		Link link = new Link(parent, SWT.NONE);
 		link.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				openExternalUrl(url);
 			}
 		});
 
 		link.setText(text);
 	}
 
 	/**
 	 * Returns the debugging engine path preference key.
 	 */
 	protected abstract PreferenceKey getDebuggingEnginePathKey();
 
 	/**
 	 * Creates the engine path block.
 	 * 
 	 * <p>
 	 * Sub-classes are free to override if they wish to make additional
 	 * contributions to the parent composite to provide additional options for
 	 * their specific engine.
 	 * </p>
 	 * 
 	 * @param parent
 	 *            parent composite
 	 */
 	protected void createEngineBlock(final Composite parent) {
 		final Group group = SWTFactory.createGroup(parent,
 				ScriptDebugPreferencesMessages.ExternalEngineGroup, 3, 1,
				GridData.FILL_BOTH);
 
 		enginePaths = new EnvironmentPathBlock();
 		enginePaths.createControl(group);
 		enginePaths.setPaths(getEnvironmentPaths());
 	}
 
 	protected boolean processChanges(IWorkbenchPreferenceContainer container) {
 		setEnvironmentPaths(enginePaths.getPaths());
 		return super.processChanges(container);
 	}
 
 	private Map getEnvironmentPaths() {
 		String pathKeyValue = getString(getDebuggingEnginePathKey());
 		return EnvironmentPathUtils.decodePaths(pathKeyValue);
 	}
 
 	private void setEnvironmentPaths(Map env2path) {
 		String pathKeyValue = EnvironmentPathUtils.encodePaths(env2path); 
 		setString(getDebuggingEnginePathKey(), pathKeyValue);
 	}
 
 	protected void openExternalUrl(String url) {
 		try {
 			final IWebBrowser browser = PlatformUI.getWorkbench()
 					.getBrowserSupport().getExternalBrowser();
 			browser.openURL(new URL(url));
 		} catch (PartInitException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		} catch (MalformedURLException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
