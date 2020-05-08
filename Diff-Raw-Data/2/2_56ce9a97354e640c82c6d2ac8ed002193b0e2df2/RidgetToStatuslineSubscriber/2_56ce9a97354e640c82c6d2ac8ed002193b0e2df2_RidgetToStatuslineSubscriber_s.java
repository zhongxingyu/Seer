 /*******************************************************************************
  * Copyright (c) 2007, 2012 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets;
 
 import java.util.Collection;
 
 import org.eclipse.riena.ui.ridgets.marker.StatuslineMessageMarkerViewer;
 
 /**
  * @since 5.0
  */
 public class RidgetToStatuslineSubscriber {
 	/**
 	 * Key for the property which controls if ridget marker messages automatically are displayed in the status line.
 	 * <p>
 	 * The configuration for this property must be set to the extension point <tt>org.eclipse.riena.core.configuration</tt>
 	 * <p>
 	 * The default for this property (if not set) is <code>false</code>.
 	 */
 	public static final String SHOW_RIDGET_MESSAGES_IN_STATUSLINE_KEY = "riena.showRidgetMessagesInStatusline"; //$NON-NLS-1$
 
 	private StatuslineMessageMarkerViewer messageViewer;
 
 	/**
 	 * @param ridget
 	 */
 	public void addRidget(final IRidget ridget) {
 		setStatuslineViewerForRidget(ridget, messageViewer);
 	}
 
 	/**
 	 * @param ridget
 	 */
 	public void removeRidget(final IRidget ridget) {
 		removeStatuslineViewerForRidget(ridget, messageViewer);
 	}
 
 	/**
 	 * Set the {@link IStatuslineRidget} which should display the marker messages of the given ridgets. If a status line ridget is already set, it will be
 	 * "unsubscribed" from the marker messages. Only one status line ridget can be set up to display these messages. If <code>null</code> is passed, the marker
 	 * messages will not be displayed on any status line (that may be set before).
 	 */
 	public void setStatuslineToShowMarkerMessages(final IStatuslineRidget statusline, final Collection<? extends IRidget> ridgets) {
 		if (!isDifferentStatusline(statusline)) {
 			return;
 		}
 
 		final StatuslineMessageMarkerViewer newMessageViewer = createMessageViewer(statusline);
 		for (final IRidget r : ridgets) {
 			removeStatuslineViewerForRidget(r, messageViewer);
 			if (newMessageViewer != null) {
 				setStatuslineViewerForRidget(r, newMessageViewer);
 			}
 		}
 		messageViewer = newMessageViewer;
 	}
 
 	/**
 	 * visible for testing
 	 */
 	protected StatuslineMessageMarkerViewer createMessageViewer(final IStatuslineRidget statusline) {
 		return statusline != null ? new StatuslineMessageMarkerViewer(statusline) : null;
 	}
 
 	/**
 	 * visible for testing
 	 * 
 	 * @return checks if the given status line is different than the one we are holding
 	 */
	boolean isDifferentStatusline(final IStatuslineRidget statuslineToShowMarkerMessages) {
 		return messageViewer == null && statuslineToShowMarkerMessages != null || messageViewer != null
 				&& messageViewer.getStatusLine() != statuslineToShowMarkerMessages;
 	}
 
 	private void removeStatuslineViewerForRidget(final IRidget ridget, final StatuslineMessageMarkerViewer messageViewer) {
 		if (ridget == null || messageViewer == null) {
 			return;
 		}
 		if (ridget instanceof IRidgetContainer) {
 			((IRidgetContainer) ridget).setStatuslineToShowMarkerMessages(null);
 		} else if (ridget instanceof IBasicMarkableRidget) {
 			messageViewer.removeRidget((IBasicMarkableRidget) ridget);
 		}
 	}
 
 	private void setStatuslineViewerForRidget(final IRidget ridget, final StatuslineMessageMarkerViewer messageViewer) {
 		if (messageViewer == null || ridget == null) {
 			return;
 		}
 		if (ridget instanceof IRidgetContainer) {
 			((IRidgetContainer) ridget).setStatuslineToShowMarkerMessages(messageViewer.getStatusLine());
 		} else if (ridget instanceof IBasicMarkableRidget) {
 			messageViewer.addRidget((IBasicMarkableRidget) ridget);
 		}
 	}
 }
