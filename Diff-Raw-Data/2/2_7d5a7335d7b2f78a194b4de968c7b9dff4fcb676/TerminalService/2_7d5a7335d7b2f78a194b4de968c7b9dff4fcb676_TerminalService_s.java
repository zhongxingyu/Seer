 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.terminals.services;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.services.AbstractService;
 import org.eclipse.tcf.te.runtime.services.interfaces.ITerminalService;
 import org.eclipse.tcf.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
 import org.eclipse.tcf.te.runtime.utils.StatusHelper;
 import org.eclipse.tcf.te.ui.swt.DisplayUtil;
 import org.eclipse.tcf.te.ui.terminals.interfaces.IConnectorType;
 import org.eclipse.tcf.te.ui.terminals.interfaces.IUIConstants;
 import org.eclipse.tcf.te.ui.terminals.manager.ConsoleManager;
 import org.eclipse.tcf.te.ui.terminals.nls.Messages;
 import org.eclipse.tcf.te.ui.terminals.types.ConnectorManager;
 import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
 
 /**
  * Terminal service implementation.
  */
 @SuppressWarnings("restriction")
 public class TerminalService extends AbstractService implements ITerminalService {
 
 	/**
 	 * Common terminal service runnable implementation.
 	 */
 	protected static abstract class TerminalServiceRunnable {
 
 		/**
 		 * Invoked to execute the terminal service runnable.
 		 *
 		 * @param id The terminals view id or <code>null</code>.
 		 * @param secondaryId The terminals view secondary id or <code>null</code>.
 		 * @param title The terminal tab title. Must not be <code>null</code>.
 		 * @param connector The terminal connector. Must not be <code>null</code>.
 		 * @param data The custom terminal data node or <code>null</code>.
 		 * @param callback The target callback to invoke if the operation finished or <code>null</code>.
 		 */
 		public abstract void run(String id, String secondaryId, String title, ITerminalConnector connector, Object data, ICallback callback);
 
 		/**
 		 * Returns if or if not to execute the runnable asynchronously.
 		 * <p>
 		 * The method returns per default <code>true</code>. Overwrite to
 		 * modify the behavior.
 		 *
 		 * @return <code>True</code> to execute the runnable asynchronously, <code>false</code> otherwise.
 		 */
 		public boolean isExecuteAsync() { return true; }
 	}
 
 	/**
 	 * Executes the given runnable operation and invokes the given callback, if any,
 	 * after the operation finished.
 	 *
 	 * @param properties The terminal properties. Must not be <code>null</code>.
 	 * @param runnable The terminal service runnable. Must not be <code>null</code>.
 	 * @param callback The target callback to invoke if the operation has been finished or <code>null</code>.
 	 */
 	protected final void executeServiceOperation(final IPropertiesContainer properties, final TerminalServiceRunnable runnable, final ICallback callback) {
 		Assert.isNotNull(properties);
 		Assert.isNotNull(runnable);
 
 		// Extract the properties
 		String id = properties.getStringProperty(ITerminalsConnectorConstants.PROP_ID);
 		String secondaryId = properties.getStringProperty(ITerminalsConnectorConstants.PROP_SECONDARY_ID);
 		String title = properties.getStringProperty(ITerminalsConnectorConstants.PROP_TITLE);
 		Object data = properties.getProperty(ITerminalsConnectorConstants.PROP_DATA);
 
 		// Normalize the terminals console view id
 		id = normalizeId(id, data);
 		// Normalize the terminal console tab title
 		title = normalizeTitle(title, data);
 
 		// Create the terminal connector instance
 		final ITerminalConnector connector = createTerminalConnector(properties);
 		if (connector == null) {
 			// Properties contain invalid connector arguments
 			if (callback != null) {
 				callback.done(this, StatusHelper.getStatus(new IllegalArgumentException(Messages.TerminalService_error_cannotCreateConnector)));
 			}
 			return;
 		}
 
 		// Finalize the used variables
 		final String finId = id;
 		final String finSecondaryId = secondaryId;
 		final String finTitle = title;
 		final Object finData = data;
 
 		// Execute the operation
 		if (!runnable.isExecuteAsync()) {
 			runnable.run(finId, finSecondaryId, finTitle, connector, finData, callback);
 		}
 		else {
 			DisplayUtil.safeAsyncExec(new Runnable() {
 				@Override
                 public void run() {
 					runnable.run(finId, finSecondaryId, finTitle, connector, finData, callback);
 				}
 			});
 		}
 	}
 
 	/**
 	 * Normalize the terminals view id.
 	 *
 	 * @param id The terminals view id or <code>null</code>.
 	 * @param data The custom data object or <code>null</code>.
 	 *
 	 * @return The normalized terminals console view id.
 	 */
 	protected String normalizeId(String id, Object data) {
 		return id != null ? id : IUIConstants.ID;
 	}
 
 	/**
 	 * Normalize the terminal tab title.
 	 *
 	 * @param title The terminal tab title or <code>null</code>.
 	 * @param data The custom data object or <code>null</code>.
 	 *
 	 * @return The normalized terminal tab title.
 	 */
 	protected String normalizeTitle(String title, Object data) {
 		// If the title is explicitly specified, return as is
 		if (title != null) return title;
 
 		// Return the default console title in all other cases
 		return Messages.TerminalService_defaultTitle;
 	}
 
 	/**
 	 * Creates the terminal connector configured within the given properties.
 	 *
 	 * @param properties The terminal console properties. Must not be <code>null</code>.
 	 * @return The terminal connector or <code>null</code>.
 	 */
 	protected ITerminalConnector createTerminalConnector(IPropertiesContainer properties) {
 		Assert.isNotNull(properties);
 
 		// The terminal connector result object
 		ITerminalConnector connector = null;
 
 		// Get the connector type id from the properties
 		String connectorTypeId = properties.getStringProperty(ITerminalsConnectorConstants.PROP_CONNECTOR_TYPE_ID);
 		if (connectorTypeId != null) {
 			// Get the connector type
 			IConnectorType connectorType = ConnectorManager.getInstance().getConnectorType(connectorTypeId, false);
 			if (connectorType != null) {
 				// Create the connector
 				connector = connectorType.createTerminalConnector(properties);
 			}
 		}
 
 		return connector;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.services.interfaces.ITerminalService#openConsole(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
     public void openConsole(final IPropertiesContainer properties, final ICallback callback) {
 		Assert.isNotNull(properties);
 
 		executeServiceOperation(properties, new TerminalServiceRunnable() {
 			@Override
 			public void run(String id, String secondaryId, String title, ITerminalConnector connector, Object data, ICallback callback) {
 				// Determine if a new terminal tab shall be enforced
 				boolean forceNew = properties.getBooleanProperty(ITerminalsConnectorConstants.PROP_FORCE_NEW);
 				// Determine the terminal encoding
 				String encoding = properties.getStringProperty(ITerminalsConnectorConstants.PROP_ENCODING);
 				// Open the new console
 				CTabItem item = ConsoleManager.getInstance().openConsole(id, secondaryId, title, encoding, connector, data, true, forceNew);
 				// Associate the original terminal properties with the tab item.
 				// This makes it easier to persist the connection data within the memento handler
				if (item != null) item.setData("properties", properties); //$NON-NLS-1$
 				// Invoke the callback
 				if (callback != null) callback.done(this, Status.OK_STATUS);
 			}
 		}, callback);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.services.interfaces.ITerminalService#closeConsole(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
     public void closeConsole(final IPropertiesContainer properties, final ICallback callback) {
 		Assert.isNotNull(properties);
 
 		executeServiceOperation(properties, new TerminalServiceRunnable() {
 			@Override
 			public void run(String id, String secondaryId, String title, ITerminalConnector connector, Object data, ICallback callback) {
 				// Close the console
 				ConsoleManager.getInstance().closeConsole(id, title, connector, data);
 				// Invoke the callback
 				if (callback != null) callback.done(this, Status.OK_STATUS);
 			}
 		}, callback);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.services.interfaces.ITerminalService#terminateConsole(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void terminateConsole(IPropertiesContainer properties, ICallback callback) {
 		Assert.isNotNull(properties);
 
 		executeServiceOperation(properties, new TerminalServiceRunnable() {
 			@Override
 			public void run(String id, String secondaryId, String title, ITerminalConnector connector, Object data, ICallback callback) {
 				// Close the console
 				ConsoleManager.getInstance().terminateConsole(id, title, connector, data);
 				// Invoke the callback
 				if (callback != null) callback.done(this, Status.OK_STATUS);
 			}
 		}, callback);
 	}
 }
