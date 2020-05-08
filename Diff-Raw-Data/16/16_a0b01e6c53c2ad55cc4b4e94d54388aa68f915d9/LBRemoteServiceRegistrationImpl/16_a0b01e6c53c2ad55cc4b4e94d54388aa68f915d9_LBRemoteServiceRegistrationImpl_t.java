 /*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
 package org.eclipse.ecf.provider.jms.container;
 
 import org.eclipse.ecf.provider.remoteservice.generic.RemoteCallImpl;
 import org.eclipse.ecf.provider.remoteservice.generic.RemoteServiceRegistrationImpl;
 
 public class LBRemoteServiceRegistrationImpl extends RemoteServiceRegistrationImpl {
 
 	private static final long serialVersionUID = -8066562963342697221L;
 
	private transient LBRegistrySharedObject lbRegistry;

	public LBRemoteServiceRegistrationImpl() {
		// nothing
	}
 
 	public LBRemoteServiceRegistrationImpl(LBRegistrySharedObject lbRegistry) {
 		this.lbRegistry = lbRegistry;
 	}
 
 	public Object callService(RemoteCallImpl call) throws Exception {
		if (lbRegistry == null)
			new NullPointerException("cannot call proxy directly"); //$NON-NLS-1$
 		return lbRegistry.callSynch(this, call);
 	}
 }
