 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.edp.coderesolution;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.wazaabi.engine.edp.EDPSingletons;
 import org.eclipse.wazaabi.mm.edp.handlers.Deferred;
 import org.eclipse.wazaabi.mm.edp.handlers.EDPHandlersPackage;
 
 public abstract class AbstractDeferredAdapter extends AdapterImpl implements
 		DeferredAdapter {
 
 	private AbstractCodeDescriptor codeDescriptor = null;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wazaabi.engine.edp.coderesolution.DeferredAdapter2#
 	 * getCodeDescriptor()
 	 */
 	public AbstractCodeDescriptor getCodeDescriptor() {
 		return codeDescriptor;
 	}
 
 	/**
 	 * Attaches a new CodeDescriptor to this DeferredAdapter. It the uri is the
 	 * same than a previous attached CodeDescriptor, then the method simply
 	 * returns.
 	 * 
 	 * @param uri
 	 */
 	protected void attachCodeDescriptor(String uri) {
 		// in case of an existing previous CodeDescriptor, just check that it's
 		// uri has changed
 		if (getCodeDescriptor() != null
 				&& (getCodeDescriptor().getUri().equals(uri) || ("" //$NON-NLS-1$
 				.equals(getCodeDescriptor().getUri()) && (uri == null || "".equals(uri))))) //$NON-NLS-1$
 			return;
 
 		// since the CodeDescriptor is not the same than the previous one
 		releaseCodeDescriptor(getCodeDescriptor());
 
 		if (uri == null || "".equals(uri)) {//$NON-NLS-1$
 			codeDescriptor = null;
 			return;
 		}
 
		if (getCodeLocatorBaseUri() != null
				&& !getCodeLocatorBaseUri().isEmpty())
 			uri = EDPSingletons.getComposedCodeLocator().getFullPath(
 					getCodeLocatorBaseUri(), uri, getTarget());
 
 		codeDescriptor = EDPSingletons.getComposedCodeLocator()
 				.resolveCodeDescriptor(uri);
 		if (codeDescriptor != null) {
 			codeDescriptor.setUri(uri);
 			initCodeDescriptor(getCodeDescriptor());
 		}
 	}
 
 	/**
 	 * Initialize the CodeDescriptor. This method is a placeholder for
 	 * implementors who want to add specific behavior during construction.
 	 * 
 	 * @param codeDescriptor
 	 */
 	protected void initCodeDescriptor(AbstractCodeDescriptor codeDescriptor) {
 
 	}
 
 	/**
 	 * Releases the CodeDescriptor. If the code underneath has been instanciated
 	 * then the release method is called on the CodeDescriptor.
 	 * 
 	 * @param codeDescriptor
 	 */
 	protected void releaseCodeDescriptor(AbstractCodeDescriptor codeDescriptor) {
 		if (codeDescriptor != null && codeDescriptor.isInstanciated()) {
 			AbstractCodeDescriptor.MethodDescriptor releaseMethodDescriptor = getReleaseMethodDescriptor(codeDescriptor);
 			if (releaseMethodDescriptor != null)
 				codeDescriptor.invokeMethod(releaseMethodDescriptor, null);
 		}
 	}
 
 	/**
 	 * Returns the methodDescriptor used to release the CodeDescriptor.
 	 * Implementors could overwrite this method.
 	 * 
 	 * @param codeDescriptor
 	 * @return
 	 */
 	protected AbstractCodeDescriptor.MethodDescriptor getReleaseMethodDescriptor(
 			AbstractCodeDescriptor codeDescriptor) {
 		if (codeDescriptor != null)
 			return codeDescriptor.getMethodDescriptor(
 					"dispose", null, null, null); //$NON-NLS-1$
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.wazaabi.engine.edp.coderesolution.DeferredAdapter2#setTarget
 	 * (org.eclipse.emf.common.notify.Notifier)
 	 */
 	@Override
 	/**
 	 * if we re target an existing adapter, we need to unhook the previous target before.
 	 * @see Adapter#setTarget
 	 */
 	public void setTarget(Notifier newTarget) {
 		dispose();
 		super.setTarget(newTarget);
 		registerMethods();
 	}
 
 	protected void registerMethods() {
 		if (getTarget() != null) {
 			attachCodeDescriptor(((Deferred) getTarget()).getUri());
 			if (getCodeDescriptor() != null)
 				registerMethods(getCodeDescriptor());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wazaabi.engine.edp.coderesolution.DeferredAdapter2#
 	 * isAdapterForType(java.lang.Object)
 	 */
 	public boolean isAdapterForType(Object type) {
 		return type instanceof Deferred;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wazaabi.engine.edp.coderesolution.DeferredAdapter2#
 	 * notifyChanged(org.eclipse.emf.common.notify.Notification)
 	 */
 	public void notifyChanged(Notification notification) {
 		if (notification.getEventType() == Notification.SET) {
 			switch (notification.getFeatureID(Deferred.class)) {
 			case EDPHandlersPackage.DEFERRED__URI:
 				attachCodeDescriptor(notification.getNewStringValue());
 				break;
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.wazaabi.engine.edp.coderesolution.DeferredAdapter2#dispose ()
 	 */
 	public void dispose() {
 		if (getCodeDescriptor() != null) {
 			releaseCodeDescriptor(getCodeDescriptor());
 			codeDescriptor = null;
 		}
 	}
 
 	/**
 	 * Returns the container of this DeferredAdapter'model.
 	 * 
 	 * @return
 	 */
 	protected EObject getContainer() {
 		if (getTarget() instanceof Deferred)
 			return ((Deferred) getTarget()).eContainer();
 		return null;
 	}
 
 	/**
 	 * Registers all the methods required for this DeferredAdapter given a non
 	 * null <code>CodeDescriptor</code>. Implementors must override this method
 	 * in order to improve performances.
 	 * 
 	 * @param codeDescriptor
 	 *            The <code>CodeDescriptor</code>, canot be null.
 	 */
 	protected abstract void registerMethods(
 			AbstractCodeDescriptor codeDescriptor);
 }
