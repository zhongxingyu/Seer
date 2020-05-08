 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * emueller
  * koegel
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.common.model.util;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.emf.emfstore.internal.common.model.impl.NotifiableIdEObjectCollectionImpl;
 
 /**
  * Notifies a about changes in its containment hierarchy.
  * 
  * @author koegel
  * @author emueller
  */
 public class EObjectChangeNotifier extends EContentAdapter {
 
 	private final NotifiableIdEObjectCollectionImpl collection;
 	private boolean isInitializing;
 	private final Stack<Notification> currentNotifications;
 	private final Stack<List<EObject>> removedModelElements;
 	private int reentrantCallToAddAdapterCounter;
 	private boolean notificationDisabled;
 
 	/**
 	 * Constructor. Attaches an Adapter to the given {@link Notifier} and forwards notifications to the given
 	 * NotifiableIdEObjectCollection, that reacts appropriately.
 	 * 
 	 * @param notifiableCollection
 	 *            a NotifiableIdEObjectCollection
 	 * @param notifier
 	 *            the {@link Notifier} to listen to
 	 */
 	public EObjectChangeNotifier(NotifiableIdEObjectCollectionImpl notifiableCollection, Notifier notifier) {
 		collection = notifiableCollection;
 		isInitializing = true;
 		currentNotifications = new Stack<Notification>();
 		removedModelElements = new Stack<List<EObject>>();
 		notifier.eAdapters().add(this);
 		isInitializing = false;
 		reentrantCallToAddAdapterCounter = 0;
 		notificationDisabled = false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.ecore.util.EContentAdapter#addAdapter(org.eclipse.emf.common.notify.Notifier)
 	 */
 	@Override
 	protected void addAdapter(Notifier notifier) {
 
 		try {
 			reentrantCallToAddAdapterCounter += 1;
 			if (!notifier.eAdapters().contains(this)) {
 				super.addAdapter(notifier);
 			}
 		} finally {
 			reentrantCallToAddAdapterCounter -= 1;
 		}
 		if (reentrantCallToAddAdapterCounter > 0 || currentNotifications.isEmpty()) {
 			// any other than the first call in re-entrant calls to addAdapter
 			// are going to call the project
 			return;
 		}
 
 		final Notification currentNotification = currentNotifications.peek();
 
 		if (currentNotification != null && !currentNotification.isTouch() && !isInitializing
 			&& notifier instanceof EObject && !ModelUtil.isIgnoredDatatype((EObject) notifier)) {
 			final EObject modelElement = (EObject) notifier;
 			if (!collection.contains(modelElement) && isInCollectionHierarchy(modelElement)) {
 				collection.modelElementAdded(collection, modelElement);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.ecore.util.EContentAdapter#removeAdapter(org.eclipse.emf.common.notify.Notifier)
 	 */
 	@Override
 	protected void removeAdapter(Notifier notifier) {
 
 		if (isInitializing || currentNotifications.isEmpty()) {
 			return;
 		}
 
 		final Notification currentNotification = currentNotifications.peek();
 
 		if (currentNotification != null && currentNotification.isTouch()) {
 			return;
 		}
 
 		if (currentNotification != null && currentNotification.getFeature() instanceof EReference) {
 			// do not remove adapter because of opposite
 			final EReference eReference = (EReference) currentNotification.getFeature();
 			if (eReference.isContainment() && eReference.getEOpposite() != null
 				&& !eReference.getEOpposite().isTransient()) {
 				return;
 			}
 		}
 
 		if (notifier instanceof EObject) {
 			final EObject modelElement = (EObject) notifier;
 			if (!isInCollectionHierarchy(modelElement)
 				&& (collection.contains(modelElement) || collection.getDeletedModelElementId(modelElement) != null)) {
 				removedModelElements.peek().add(modelElement);
 			}
 
 		}
 	}
 
 	/**
 	 * Checks whether the given {@link EObject} is within the collection.
 	 * 
 	 * @param modelElement
 	 *            the {@link EObject} whose containment should be checked
 	 * @return true, if the {@link EObject} is contained in the collection,
 	 *         false otherwise
 	 */
 	private boolean isInCollectionHierarchy(EObject modelElement) {
 		final EObject parent = modelElement.eContainer();
 		if (parent == null) {
 			return false;
 		}
 
 		if (parent == collection) {
 			return true;
 		}
 
 		if (collection.contains(parent)) {
 			return true;
 		}
 
 		return isInCollectionHierarchy(parent);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.ecore.util.EContentAdapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		if (notificationDisabled) {
 			return;
 		}
 
 		removedModelElements.push(new ArrayList<EObject>());
 		currentNotifications.push(notification);
 		final Object feature = notification.getFeature();
 		final Object notifier = notification.getNotifier();
 		final Object newValue = notification.getNewValue();
 
 		if (feature instanceof EReference) {
 			final EReference eReference = (EReference) feature;
 
 			// Do not create notifications for transient features
 			if (eReference.isTransient()) {
 				return;
 			}
 
 			if (eReference.isContainer()) {
 				handleContainer(notification, eReference);
 			}
 		}
 
 		super.notifyChanged(notification);
 
 		// detect if the notification is about a reference to an object outside of the collection => notify
 		// project
 		if (feature instanceof EReference && newValue != null && !notification.isTouch()) {
 			final EReference reference = (EReference) feature;
 
 			if (!reference.isContainment() && !reference.isContainer()) {
 				if (newValue instanceof EObject) {
 					handleSingleReference((EObject) newValue);
 				} else if (newValue instanceof List<?>) {
 					handleMultiReference((List<?>) newValue);
 				}
 			} else if (reference.isContainment()
 				&& Map.Entry.class.isAssignableFrom(reference.getEType().getInstanceClass())) {
 				handleMapEntry((Map.Entry<?, ?>) newValue, reference);
 			}
 		}
 
 		currentNotifications.pop();
 		notifyCollection(notification, notifier);
 		notifyCollectionAboutRemovedElements();
 	}
 
 	private void notifyCollectionAboutRemovedElements() {
 		for (final EObject removedElement : removedModelElements.pop()) {
 			collection.modelElementRemoved(collection, removedElement);
 		}
 	}
 
 	private void notifyCollection(Notification notification, final Object notifier) {
 		if (!notification.isTouch() && notifier instanceof EObject && hasId(notifier)) {
 			collection.notify(notification, collection, (EObject) notifier);
 		}
 	}
 
 	private boolean hasId(Object notifier) {
 		return collection.getModelElementId((EObject) notifier) != null
 			|| collection.getDeletedModelElementId((EObject) notifier) != null;
 	}
 
 	private void handleMapEntry(Map.Entry<?, ?> entry, EReference reference) {
 		for (final EReference ref : reference.getEReferenceType().getEReferences()) {
 			if (ref.getName().equals("key") && entry.getKey() instanceof EObject) { //$NON-NLS-1$
 				handleSingleReference((EObject) entry.getKey());
 			} else if (ref.getName().equals("value") && entry.getValue() instanceof EObject) { //$NON-NLS-1$
 				handleSingleReference((EObject) entry.getValue());
 			}
 		}
 	}
 
 	private void handleMultiReference(List<?> list) {
 		for (final Object obj : list) {
 
 			if (!(obj instanceof EObject)) {
 				continue;
 			}
 
 			final EObject eObject = (EObject) obj;
 
 			if (!collection.contains(eObject)) {
 				collection.addCutElement(eObject);
 			}
 		}
 	}
 
 	private void handleSingleReference(EObject newEObject) {
 		if (!collection.contains(newEObject)) {
 			if (ModelUtil.isSingleton(newEObject)) {
 				return;
 			}
 			collection.addCutElement(newEObject);
 		}
 	}
 
 	/**
 	 * @param notification
 	 */
 	private void handleContainer(Notification notification, EReference eReference) {
 		if (notification.getEventType() == Notification.SET) {
 			final Object newValue = notification.getNewValue();
 			final Object oldValue = notification.getOldValue();
 			if (newValue == null && oldValue != null) {
 				removeAdapter((Notifier) notification.getNotifier());
 			}
 		}
 	}
 
 	/**
 	 * @param notificationDisabled
 	 *            the notificationDisabled to set
 	 */
 	public void disableNotifications(boolean notificationDisabled) {
 		this.notificationDisabled = notificationDisabled;
 	}
 
 }
