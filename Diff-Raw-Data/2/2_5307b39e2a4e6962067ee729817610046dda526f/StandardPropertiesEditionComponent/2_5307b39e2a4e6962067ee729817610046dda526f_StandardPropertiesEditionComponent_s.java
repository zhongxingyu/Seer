 /*******************************************************************************
  * Copyright (c) 2008, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.impl.components;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.change.util.ChangeRecorder;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener;
 import org.eclipse.emf.eef.runtime.api.notify.NotificationFilter;
 import org.eclipse.emf.eef.runtime.api.notify.PropertiesEditingSemanticListener;
 import org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.context.PropertiesEditingContext;
 import org.eclipse.emf.eef.runtime.context.impl.EObjectPropertiesEditionContext;
 import org.eclipse.emf.eef.runtime.impl.command.StandardEditingCommand;
 import org.eclipse.emf.eef.runtime.impl.notify.PropertiesValidationEditionEvent;
 import org.eclipse.emf.eef.runtime.impl.utils.StringTools;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikaël Barbero</a>
  */
 public abstract class StandardPropertiesEditionComponent implements IPropertiesEditionComponent {
 
 	private static final long DELAY = 500L;
 
 	public static final Object FIRE_PROPERTIES_CHANGED_JOB_FAMILY = new Object();
 
 	/**
 	 * List of IPropertiesEditionComponentListeners
 	 */
 	private List<IPropertiesEditionListener> listeners;
 
 	/**
 	 * the semantic listener dedicated to update view
 	 */
 	protected PropertiesEditingSemanticListener semanticAdapter;
 
 	/**
 	 * the editing domain where to perform live update
 	 */
 	protected EditingDomain liveEditingDomain;
 
 	/**
 	 * the job that will fire the property changed event
 	 */
 	protected FirePropertiesChangedJob firePropertiesChangedJob;
 
 	/**
 	 * Editing context
 	 */
 	protected PropertiesEditingContext editingContext;
 
 	/**
 	 * the editing mode
 	 */
 	protected String editing_mode;
 
 	/**
 	 * Is the component is current initializing
 	 */
 	protected boolean initializing = false;
 
 	/**
 	 * List of {@link IPropertiesEditionPart}'s key managed by the component.
 	 */
 	protected String[] parts;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#initPart(java.lang.Object,
 	 *      int, org.eclipse.emf.ecore.EObject)
 	 */
 	public void initPart(Object key, int kind, EObject element) {
 		this.initPart(key, kind, element, editingContext.getResourceSet());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#partsList()
 	 */
 	public String[] partsList() {
 		return parts;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#addListener(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener)
 	 */
 	public void addListener(IPropertiesEditionListener listener) {
 		if (listeners == null)
 			listeners = new ArrayList<IPropertiesEditionListener>();
 		listeners.add(listener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#removeListener(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener)
 	 */
 	public void removeListener(IPropertiesEditionListener listener) {
 		if (listeners != null)
 			listeners.remove(listener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#setLiveEditingDomain(org.eclipse.emf.edit.domain.EditingDomain)
 	 */
 	public void setLiveEditingDomain(EditingDomain editingDomain) {
 		this.liveEditingDomain = editingDomain;
 	}
 
 	/**
 	 * Initialize the semantic model listener for live editing mode
 	 * 
 	 * @return the semantic model listener
 	 */
 	protected PropertiesEditingSemanticListener initializeSemanticAdapter() {
 		PropertiesEditingSemanticListener listener = new PropertiesEditingSemanticListener(this,
 				getNotificationFilters()) {
 
 			@Override
 			public void runUpdateRunnable(Notification notification) {
 				if (!getPart().getFigure().isDisposed()) {
 					updatePart(notification);
 				} else {
 					dispose();
 				}
 			}
 		};
 		return listener;
 	}
 
 	/**
 	 * Returns the list of notification filters to use.
 	 * 
 	 * @return the list of notification filters to use
 	 */
 	protected abstract NotificationFilter[] getNotificationFilters();
 
 	/**
 	 * Update the part in response to a semantic event
 	 * 
 	 * @param msg
 	 *            the semantic event
 	 */
 	public abstract void updatePart(Notification msg);
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener#firePropertiesChanged(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
 	 */
 	private void propagateEvent(IPropertiesEditionEvent event) {
 		event.addHolder(this);
 		for (IPropertiesEditionListener listener : listeners) {
 			if (!event.hold(listener))
 				listener.firePropertiesChanged(event);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener#firePropertiesChanged(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
 	 */
 	public void firePropertiesChanged(final IPropertiesEditionEvent event) {
 		if (!isInitializing() && shouldProcess(event)) {
 			Diagnostic valueDiagnostic = validateValue(event);
 			if (valueDiagnostic.getSeverity() != Diagnostic.OK && valueDiagnostic instanceof BasicDiagnostic)
 				propagateEvent(new PropertiesValidationEditionEvent(event, valueDiagnostic));
 			else {
 				editingContext.initializeRecorder();
 				if (IPropertiesEditionComponent.BATCH_MODE.equals(editing_mode)) {
 					updateSemanticModel(event);
 				} else if (IPropertiesEditionComponent.LIVE_MODE.equals(editing_mode)) {
 					liveEditingDomain.getCommandStack().execute(
 							new StandardEditingCommand(new EObjectPropertiesEditionContext(editingContext,
 									this, editingContext.getEObject(), editingContext.getAdapterFactory())) {
 
 								public void execute() {
 									updateSemanticModel(event);
 									ChangeRecorder changeRecorder = editingContext.getChangeRecorder();
 									if (changeRecorder != null) {
 										description = changeRecorder.endRecording();
 										changeRecorder.dispose();
 									}
 								}
 
 							});
 				}
 				Diagnostic validate = validate();
 				propagateEvent(new PropertiesValidationEditionEvent(event, validate));
 			}
 			propagateEvent(event);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener#lazyFirePropertiesChanged(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
 	 */
 	public void delayedFirePropertiesChanged(IPropertiesEditionEvent event) {
 		if (IPropertiesEditionComponent.BATCH_MODE.equals(editing_mode)) {
 			firePropertiesChanged(event);
 		} else if (IPropertiesEditionComponent.LIVE_MODE.equals(editing_mode)) {
 			if (getFirePropertiesChangedJob().cancel()) {
 				getFirePropertiesChangedJob().setEvent(event);
 				getFirePropertiesChangedJob().schedule(DELAY);
 			} else {
 				try {
 					getFirePropertiesChangedJob().join();
 					getFirePropertiesChangedJob().setEvent(event);
 					getFirePropertiesChangedJob().schedule();
 				} catch (InterruptedException e) {
 					getFirePropertiesChangedJob().setEvent(null);
 				}
 			}
 		}
 	}
 
 	protected FirePropertiesChangedJob getFirePropertiesChangedJob() {
 		if (firePropertiesChangedJob == null) {
 			firePropertiesChangedJob = new FirePropertiesChangedJob("Fire properties changed...");
 		}
 		return firePropertiesChangedJob;
 	}
 
 	protected class FirePropertiesChangedJob extends Job {
 
 		private IPropertiesEditionEvent fEvent;
 
 		public FirePropertiesChangedJob(String name) {
 			super(name);
 		}
 
 		@Override
 		public boolean belongsTo(Object family) {
 			return family == FIRE_PROPERTIES_CHANGED_JOB_FAMILY;
 		}
 
 		@Override
 		public boolean shouldSchedule() {
 			return fEvent != null;
 		}
 
 		@Override
 		public boolean shouldRun() {
 			return fEvent != null;
 		}
 
 		@Override
 		protected void canceling() {
 			super.canceling();
 			fEvent = null;
 		}
 
 		public void setEvent(IPropertiesEditionEvent event) {
 			fEvent = event;
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			deactivate();
 			firePropertiesChanged(fEvent);
 			activate();
 			fEvent = null;
 			return Status.OK_STATUS;
 		}
 	}
 
 	/**
 	 * @param event
 	 *            event to process
 	 * @return <code>true</code> if the event should really launch a command.
 	 * @since 0.9
 	 */
 	protected boolean shouldProcess(IPropertiesEditionEvent event) {
		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#associatedFeature(Object)
 	 */
 	public EStructuralFeature associatedFeature(Object editorKey) {
 		return null;
 	}
 
 	/**
 	 * Update the model in response to a view event
 	 * 
 	 * @param event
 	 *            the view event
 	 */
 	public abstract void updateSemanticModel(IPropertiesEditionEvent event);
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#mustBeComposed(java.lang.Object,
 	 *      int)
 	 */
 	public boolean mustBeComposed(Object key, int kind) {
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#isRequired(java.lang.String,
 	 *      int)
 	 */
 	public boolean isRequired(Object key, int kind) {
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#getHelpContent(java.lang.String,
 	 *      int)
 	 */
 	public String getHelpContent(Object key, int kind) {
 		return StringTools.EMPTY_STRING;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#translatePart(java.lang.String)
 	 */
 	public Object translatePart(String key) {
 		return null;
 	}
 
 	/**
 	 * @return the initializing
 	 */
 	public boolean isInitializing() {
 		return initializing;
 	}
 
 	/**
 	 * @param initializing
 	 *            the initializing to set
 	 */
 	public void setInitializing(boolean initializing) {
 		this.initializing = initializing;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#setPropertiesEditionPart(java.lang.Object,
 	 *      int, org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart)
 	 */
 	public void setPropertiesEditionPart(Object key, int kind, IPropertiesEditionPart propertiesEditionPart) {
 		// Default case : nothing to do
 	}
 
 }
