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
 package org.eclipse.mylyn.docs.intent.client.ui.repositoryconnection;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.elementList.ElementListAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.elementList.ElementListNotificator;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.TypeReference;
 
 /**
  * SubClass of ElementListAdapter used for handling notifications specially for the editor.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class EditorElementListAdapter extends ElementListAdapter {
 
 	/**
 	 * EditorElementListAdapter constructor.
 	 */
 	public EditorElementListAdapter() {
 		super();
 	}
 
 	/**
 	 * EditorElementListAdapter constructor.
 	 * 
 	 * @param notificator
 	 *            Notificator to prevent of any change occurred on the watched object
 	 */
 	public EditorElementListAdapter(ElementListNotificator notificator) {
 		super(notificator);
 	}
 
 	/**
 	 * Indicates if the given notifier contains relevant informations and should be handled by this adapter.
 	 * 
 	 * @param notifier
 	 *            the notifier to determine if it contains relevant informations
 	 * @return true if this adapter should handle this object, false otherwise
 	 */
 	private boolean isRelevantNotifier(Notifier notifier) {
 		return !(notifier instanceof TypeReference);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.elementList.ElementListAdapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		if (isRelevantNotifier((Notifier)notification.getNotifier())) {
			super.notifyChanged(notification);
 		}
 	}
 }
