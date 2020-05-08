 package edu.teco.dnd.eclipse.appView;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.UUID;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 import edu.teco.dnd.discover.ApplicationInformation;
 import edu.teco.dnd.discover.BlockInformation;
 import edu.teco.dnd.eclipse.DisplayUtil;
 import edu.teco.dnd.eclipse.TypecastingWidgetDataStore;
 import edu.teco.dnd.server.ApplicationManager;
 import edu.teco.dnd.server.ApplicationManagerListener;
 
 /**
  * This class is used to update the Tree used by {@link ApplicationView}.
  * 
  * @author Philipp Adolf
  */
 // TODO: Add module names
 class ApplicationTreeUpdater implements ApplicationManagerListener {
 	private static final Logger LOGGER = LogManager.getLogger(ApplicationTreeUpdater.class);
 
 	public static final TypecastingWidgetDataStore<ApplicationInformation> APPLICATION_INFORMATION_STORE =
 			new TypecastingWidgetDataStore<ApplicationInformation>(ApplicationInformation.class, "application");
 
 	private ApplicationManager applicationManager = null;
 	private volatile Tree applicationTree = null;
 
 	/**
 	 * Sets the ApplicationManager that should be used. This method is thread-safe and will update applicationTree if it
 	 * has been set.
 	 * 
 	 * @param applicationManager
 	 *            the new ApplicationManager to use
 	 * @see #setApplicationTree(Tree)
 	 */
 	synchronized void setApplicationManager(final ApplicationManager applicationManager) {
 		LOGGER.entry(applicationManager);
 		if (this.applicationManager != null) {
 			this.applicationManager.removeApplicationListener(this);
 		}
 
 		this.applicationManager = applicationManager;
 		if (this.applicationManager != null) {
 			this.applicationManager.addApplicationListener(this);
 
 			fillTree(this.applicationManager.getApps());
 		} else {
 			fillTree(Collections.<ApplicationInformation> emptyList());
 		}
 		LOGGER.exit();
 	}
 
 	/**
 	 * Sets the Tree object that should be filled with the Applications. If an ApplicationManager has been set, the Tree
 	 * is automatically filled. This method is thread-safe.
 	 * 
 	 * @param applicationTree
 	 *            the Tree object that should be filled with the Application data
 	 * @see #setApplicationManager(ApplicationManager)
 	 */
 	synchronized void setApplicationTree(final Tree applicationTree) {
 		this.applicationTree = applicationTree;
 		if (applicationManager == null) {
 			fillTree(Collections.<ApplicationInformation> emptyList());
 		} else {
 			fillTree(applicationManager.getApps());
 		}
 	}
 
 	@Override
 	public void applicationsResolved(final Collection<ApplicationInformation> apps) {
 		LOGGER.entry(apps);
 		fillTree(apps);
 		LOGGER.exit();
 	}
 
 	@Override
 	public void serverOnline() {
 	}
 
 	@Override
 	public void serverOffline() {
 		fillTree(Collections.<ApplicationInformation> emptyList());
 	}
 
 	/**
 	 * Fills the tree with the given ApplicationInformation. Old information is discarded first.
 	 * 
 	 * @param applications
 	 *            the Applications that should be displayed
 	 */
 	private synchronized void fillTree(final Collection<ApplicationInformation> applications) {
 		LOGGER.entry(applications);
 		assert applications != null;
 
		if (applicationTree == null) {
 			LOGGER.debug("applicationTree is null");
 		} else {
			DisplayUtil.getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
					// this is unsychronized access to applicationTree. However, the thread that submits this Runnable
					// holds the lock and this Runnable is executed synchronously, so applicationTree will not be
					// modified
					final Tree tree = applicationTree;
					tree.removeAll();
 
 					for (final ApplicationInformation application : applicationManager.getApps()) {
						addApplication(tree, application);
 					}
 
					tree.update();
 				}
 			});
 		}
 		LOGGER.exit();
 	}
 
 	/**
 	 * Adds a single Application to the tree.
 	 * 
 	 * @param applicationInformation
 	 *            the Application to add
 	 */
 	private void addApplication(final Tree tree, final ApplicationInformation applicationInformation) {
 		LOGGER.entry(applicationInformation);
 		assert tree != null;
 		assert applicationInformation != null;
 
 		LOGGER.debug("adding TableItem for {} to {}", applicationInformation, applicationTree);
 		final TreeItem applicationItem = new TreeItem(tree, SWT.NONE);
 		applicationItem.setText(0, applicationInformation.getName() + " (" + applicationInformation.getID() + ")");
 		APPLICATION_INFORMATION_STORE.store(applicationItem, applicationInformation);
 		for (final UUID moduleUUID : applicationInformation.getModules()) {
 			addModule(applicationItem, applicationInformation, moduleUUID);
 		}
 
 		LOGGER.exit();
 	}
 
 	/**
 	 * Adds a single Module to an Application TreeItem.
 	 * 
 	 * @param applicationItem
 	 *            the TreeItem of the Application this Module TreeItem should be added to
 	 * @param applicationInformation
 	 *            the ApplicationInformation for the Application
 	 * @param moduleUUID
 	 *            the UUID of the Module that should be added by this method
 	 */
 	private void addModule(final TreeItem applicationItem, final ApplicationInformation applicationInformation,
 			final UUID moduleUUID) {
 		LOGGER.trace("adding TableItem for module {} to {}", moduleUUID, applicationItem);
 		final TreeItem moduleItem = new TreeItem(applicationItem, SWT.NONE);
 		moduleItem.setText("" + moduleUUID);
 		for (final BlockInformation block : applicationInformation.getBlocks(moduleUUID)) {
 			addBlock(moduleItem, applicationInformation, block);
 		}
 	}
 
 	/**
 	 * Adds a single FunctionBlock to an Module TreeItem.
 	 * 
 	 * @param moduleItem
 	 *            the TreeItem of the Module this FunctionBlock TreeItem should be added to
 	 * @param applicationInformation
 	 *            the ApplicationInformation of the Application this block belongs to
 	 * @param blockID
 	 *            the ID of the FunctionBlock that should be added by this method
 	 */
 	private void addBlock(final TreeItem moduleItem, final ApplicationInformation applicationInformation,
 			final BlockInformation block) {
 		LOGGER.trace("adding TableItem for block {} to {}", block, moduleItem);
 		final TreeItem blockItem = new TreeItem(moduleItem, SWT.NONE);
 		blockItem.setText(block.getName() + " (" + block.getID() + ")");
 	}
 }
