 /**
  * Copyright (c) 2011 Gunnar Wagenknecht and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Gunnar Wagenknecht - initial API and implementation
  */
 package org.eclipse.gyrex.admin.ui.cloud.internal;
 
 import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;
 import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutFactory;
 import org.eclipse.gyrex.cloud.internal.zk.ZooKeeperGate;
import org.eclipse.gyrex.cloud.internal.zk.ZooKeeperGateListener;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.forms.IManagedForm;
 
 /**
  * Gyrex Cloud Configuration Page.
  */
 public class CloudConfigurationPage extends ConfigurationPage {
 
	final ZooKeeperGateListener connectionMonitor = new ZooKeeperGateListener() {
 		@Override
		public void gateDown(final ZooKeeperGate gate) {
			getManagedForm().setInput(null);
 		}
 
 		@Override
		public void gateRecovering(final ZooKeeperGate gate) {
			// TODO Auto-generated method stub

		}

		@Override
		public void gateUp(final ZooKeeperGate gate) {
			getManagedForm().setInput(CloudUiActivator.getInstance().getCloudManager());
 		}
 	};
 
 	boolean disposed;
 	private DataBindingContext bindingContext;
 
 	/**
 	 * Creates a new instance.
 	 */
 	public CloudConfigurationPage() {
 		setTitle("Cloud Configuration");
 		setTitleToolTip("Configure the cloud.");
 	}
 
 	@Override
 	protected void createFormContent(final IManagedForm managedForm) {
 		final Composite body = managedForm.getForm().getBody();
 		body.setLayout(FormLayoutFactory.createFormGridLayout(true, 2));
 
 		final Realm realm = SWTObservables.getRealm(Display.getCurrent());
 		bindingContext = new DataBindingContext(realm);
 
 		final NodeConnectionSection connectionSection = new NodeConnectionSection(this, body);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(connectionSection.getSection());
 		managedForm.addPart(connectionSection);
 
 		final OnlineNodesSection onlineNodesSection = new OnlineNodesSection(body, this);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(onlineNodesSection.getSection());
 		managedForm.addPart(onlineNodesSection);
 
 		final ApprovedNodesSection approvedNodesSection = new ApprovedNodesSection(body, this);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(approvedNodesSection.getSection());
 		managedForm.addPart(approvedNodesSection);
 
 		final PendingNodesSection pendingNodesSection = new PendingNodesSection(body, this);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(pendingNodesSection.getSection());
 		managedForm.addPart(pendingNodesSection);
 
 		ZooKeeperGate.addConnectionMonitor(connectionMonitor);
 	}
 
 	@Override
 	public void dispose() {
 		disposed = true;
 		bindingContext.dispose();
 		bindingContext = null;
 		super.dispose();
 		ZooKeeperGate.removeConnectionMonitor(connectionMonitor);
 	}
 
 	/**
 	 * Returns the bindingContext.
 	 * 
 	 * @return the bindingContext
 	 */
	@Override
 	public DataBindingContext getBindingContext() {
 		if (disposed) {
 			throw new IllegalStateException("disposed");
 		}
 		return bindingContext;
 	}
 
 	@Override
 	public IStatus performSave(final IProgressMonitor monitor) {
 		// call super
 		return super.performSave(monitor);
 	}
 }
