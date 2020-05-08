 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.annotation.processor;
 
 import java.lang.reflect.Method;
 import java.util.EnumMap;
 
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.core.singleton.SingletonProvider;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.INavigationNode.State;
 import org.eclipse.riena.navigation.INavigationNodeController;
 import org.eclipse.riena.navigation.ISimpleNavigationNodeListener;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.annotation.OnNavigationNodeEvent;
 import org.eclipse.riena.navigation.annotation.OnNavigationNodeEvent.Event;
 import org.eclipse.riena.ui.filter.IUIFilter;
 
 /**
  * Annotation processor for {@code INavigationNodeController} annotations.
  * 
  * @since 3.0
  */
 public final class NavigationNodeControllerAnnotationProcessor {
 
 	private static final SingletonProvider<NavigationNodeControllerAnnotationProcessor> NNCAP = new SingletonProvider<NavigationNodeControllerAnnotationProcessor>(
 			NavigationNodeControllerAnnotationProcessor.class);
 
 	/**
 	 * Answer the singleton
 	 * <code>NavigationNodeControllerAnnotationProcessor</code>
 	 * 
 	 * @return the NavigationNodeControllerAnnotationProcessor singleton
 	 */
 	public static NavigationNodeControllerAnnotationProcessor getInstance() {
 		return NNCAP.getInstance();
 	}
 
 	private NavigationNodeControllerAnnotationProcessor() {
 		// singleton
 	}
 
 	/**
 	 * Process the {@link OnNavigationNodeEvent} annotations for the given
 	 * {@link INavigationNodeController}.
 	 * 
 	 * @param navigationNodeController
 	 *            the {@link INavigationNodeController} to process
 	 */
 	public void processAnnotations(final INavigationNodeController navigationNodeController) {
 		processAnnotations(navigationNodeController, navigationNodeController.getClass());
 	}
 
	private void processAnnotations(final INavigationNodeController navigationNodeController, final Class<?> clazz) {
		if (!INavigationNodeController.class.isAssignableFrom(clazz)) {
 			return;
 		}
		processAnnotations(navigationNodeController, clazz.getSuperclass());
 
		final EnumMap<OnNavigationNodeEvent.Event, Method> events = getEventMethodMap(navigationNodeController, clazz);
 		if (!events.isEmpty()) {
 			navigationNodeController.getNavigationNode().addSimpleListener(
 					new Listener(events, navigationNodeController));
 		}
 	}
 
 	private EnumMap<OnNavigationNodeEvent.Event, Method> getEventMethodMap(
			final INavigationNodeController navigationNodeController, final Class<?> clazz) {
 		final EnumMap<OnNavigationNodeEvent.Event, Method> events = new EnumMap<OnNavigationNodeEvent.Event, Method>(
 				OnNavigationNodeEvent.Event.class);
		for (final Method method : clazz.getDeclaredMethods()) {
 			final OnNavigationNodeEvent onNavigationNodeEvent = method.getAnnotation(OnNavigationNodeEvent.class);
 			if (onNavigationNodeEvent != null) {
 				events.put(onNavigationNodeEvent.event(), method);
 			}
 		}
 		return events;
 	}
 
 	private static final class Listener implements ISimpleNavigationNodeListener {
 
 		private final EnumMap<Event, Method> events;
 		private final INavigationNodeController navigationNodeController;
 
 		private Listener(final EnumMap<Event, Method> events, final INavigationNodeController navigationNodeController) {
 			this.events = events;
 			this.navigationNodeController = navigationNodeController;
 		}
 
 		private void call(final Event event, final Object... args) {
 			final Method method = events.get(event);
 			if (method != null) {
 				try {
 					if (method.getParameterTypes().length == 0) {
 						method.invoke(navigationNodeController);
 					} else {
 						method.invoke(navigationNodeController, args);
 					}
 				} catch (final Throwable e) {
 					throw new EventDispatchFaiure("Could not dispatch event '" + event + "' via method '" + method //$NON-NLS-1$ //$NON-NLS-2$
 							+ "' to '" + navigationNodeController + "'.", e); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 			}
 		}
 
 		public void labelChanged(final INavigationNode<?> source) {
 			call(Event.LABEL_CHANGED, new Object[] { source });
 		}
 
 		public void iconChanged(final INavigationNode<?> source) {
 			call(Event.ICON_CHANGED, new Object[] { source });
 		}
 
 		public void selectedChanged(final INavigationNode<?> source) {
 			call(Event.SELECTED_CHANGED, new Object[] { source });
 		}
 
 		public void childAdded(final INavigationNode<?> source, final INavigationNode<?> childAdded) {
 			call(Event.CHILD_ADDED, new Object[] { source, childAdded });
 		}
 
 		public void childRemoved(final INavigationNode<?> source, final INavigationNode<?> childRemoved) {
 			call(Event.CHILD_REMOVED, new Object[] { source, childRemoved });
 		}
 
 		public void presentationChanged(final INavigationNode<?> source) {
 			call(Event.PRESENTATION_CHANGED, new Object[] { source });
 		}
 
 		public void parentChanged(final INavigationNode<?> source) {
 			call(Event.PARENT_CHANGED, new Object[] { source });
 		}
 
 		public void expandedChanged(final INavigationNode<?> source) {
 			call(Event.EXPANDED_CHANGED, new Object[] { source });
 		}
 
 		public void markerChanged(final INavigationNode<?> source, final IMarker marker) {
 			call(Event.MARKER_CHANGED, new Object[] { source, marker });
 		}
 
 		public void activated(final INavigationNode<?> source) {
 			call(Event.ACTIVATED, new Object[] { source });
 		}
 
 		public void beforeActivated(final INavigationNode<?> source) {
 			call(Event.BEFORE_ACTIVATED, new Object[] { source });
 		}
 
 		public void afterActivated(final INavigationNode<?> source) {
 			call(Event.AFTER_ACTIVATED, new Object[] { source });
 		}
 
 		public void deactivated(final INavigationNode<?> source) {
 			call(Event.DEACTIVATED, new Object[] { source });
 		}
 
 		public void beforeDeactivated(final INavigationNode<?> source) {
 			call(Event.BEFORE_DECTIVATED, new Object[] { source });
 		}
 
 		public void afterDeactivated(final INavigationNode<?> source) {
 			call(Event.AFTER_DECTIVATED, new Object[] { source });
 		}
 
 		public void disposed(final INavigationNode<?> source) {
 			call(Event.DISPOSED, new Object[] { source });
 		}
 
 		public void beforeDisposed(final INavigationNode<?> source) {
 			call(Event.BEFORE_DISPOSED, new Object[] { source });
 		}
 
 		public void afterDisposed(final INavigationNode<?> source) {
 			call(Event.AFTER_DISPOSED, new Object[] { source });
 		}
 
 		public void stateChanged(final INavigationNode<?> source, final State oldState, final State newState) {
 			call(Event.STATE_CHANGED, new Object[] { source, oldState, newState });
 		}
 
 		public void block(final INavigationNode<?> source, final boolean block) {
 			call(Event.BLOCK, new Object[] { source, block });
 		}
 
 		public void filterAdded(final INavigationNode<?> source, final IUIFilter filter) {
 			call(Event.FILTER_ADDED, new Object[] { source, filter });
 		}
 
 		public void filterRemoved(final INavigationNode<?> source, final IUIFilter filter) {
 			call(Event.FILTER_REMOVED, new Object[] { source, filter });
 		}
 
 		public void prepared(final INavigationNode<?> source) {
 			call(Event.PREPARED, new Object[] { source });
 		}
 
 		public void nodeIdChange(final INavigationNode<?> source, final NavigationNodeId oldId,
 				final NavigationNodeId newId) {
 			call(Event.NODE_ID_CHANGED, new Object[] { source, oldId, newId });
 		}
 
 	}
 
 	public static class EventDispatchFaiure extends RuntimeException {
 
 		public EventDispatchFaiure(final String message, final Throwable cause) {
 			super(message, cause);
 		}
 
 		private static final long serialVersionUID = 1L;
 
 	}
 
 }
