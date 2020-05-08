 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.collab.cdo.notification;
 
 import org.eclipse.emf.cdo.common.revision.CDORevisionKey;
 import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
 import org.eclipse.emf.cdo.session.CDOSessionInvalidationEvent;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.RepositoryChangeNotificationImpl;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotificationFactory;
 
 /**
  * Converts a notification into a standard RepositoryChangeNotification.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public final class CDORepositoryChangeNotificationFactory implements RepositoryChangeNotificationFactory {
 
 	/**
 	 * NotificationToChangeDescriptionConverter constructor.
 	 */
 	public CDORepositoryChangeNotificationFactory() {
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotificationFactory#createRepositoryChangeNotification(java.lang.Object)
 	 */
 	public RepositoryChangeNotification createRepositoryChangeNotification(Object specificChangeNotification) {
 
 		RepositoryChangeNotification genericChangeNotification = null;
 		// We dispatch the creation of the repositoryChangeNotification
 		if (specificChangeNotification instanceof Notification) {
 			genericChangeNotification = createFromEMFNotification((Notification)specificChangeNotification);
 		}
 		if (specificChangeNotification instanceof CDOSessionInvalidationEvent) {
 			genericChangeNotification = createFromCDOInvalidationEvent((CDOSessionInvalidationEvent)specificChangeNotification);
 		}
 		return genericChangeNotification;
 	}
 
 	/**
 	 * Converts the given EMF Notification into a RepositoryChangeNotification.
 	 * 
 	 * @param notification
 	 *            the notification to convert
 	 * @return the RepositoryChangeNotification corresponding to the given EMF Notification.
 	 */
 	public static RepositoryChangeNotification createFromEMFNotification(Notification notification) {
 		EObject target = (EObject)notification.getNotifier();
 
 		// int featureID = notification.getFeatureID(target.getClass());
 		// EStructuralFeature feature = target.eClass().getEStructuralFeature(featureID);
 		// notification.getNewValue();
 
 		RepositoryChangeNotification newNotification = new RepositoryChangeNotificationImpl();
 		newNotification.getImpactedElements().add(target);
 		return newNotification;
 	}
 
 	/**
 	 * Converts a given CDOSessionInvalidationEvent into an EMF changeDescription.
 	 * 
 	 * @param event
 	 *            a CDOSessionInvalidationEvent
 	 * @return the changeDescription corresponding to the given CDOSessionInvalidationEvent.
 	 */
 	public static RepositoryChangeNotification createFromCDOInvalidationEvent(
 			CDOSessionInvalidationEvent event) {
 		RepositoryChangeNotification newNotification = new RepositoryChangeNotificationImpl();
 
 		for (CDORevisionKey key : event.getChangedObjects()) {
 			CDORevisionDelta revisionDelta = (CDORevisionDelta)key;
 
 			for (CDOFeatureDelta delta : revisionDelta.getFeatureDeltas()) {
 				switch (delta.getType()) {
 					case SET:
 						if (event.getLocalTransaction() != null) {
							newNotification.getRightRoots().add(
 									event.getLocalTransaction().getObject(key.getID()));
 							break;
 						}
 					default:
 
 						break;
 				}
 			}
 
 		}
 		return newNotification;
 	}
 
 }
