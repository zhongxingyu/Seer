 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.util;
 
 import java.lang.ref.Reference;
 import java.lang.ref.ReferenceQueue;
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.internal.core.Activator;
 
 /**
  * A {@code WeakRef} tracks instances with a {@code WeakReference}. When the
  * instance gets garbage collected it executes a {@code Runnable} for
  * notification.<br>
  * However, when the notification gets called, the {@code referent} has already
  * been garbage collected and therefore {@code WeakRef.get()} will return
  * {@code null}.
  */
 public class WeakRef<T> {
 
 	private final Reference<T> ref;
 
 	private static final ReferenceQueue<? super Object> REF_QUEUE = new ReferenceQueue<Object>();
 	private static final Map<Reference<?>, Runnable> REMOVE_ACTIONS = new HashMap<Reference<?>, Runnable>();
 	static {
 		// Kick-off the remover
 		new Remover();
 	}
 
 	private static final Logger LOGGER = Log4r.getLogger(WeakRef.class);
 
 	/**
 	 * Create a {@code WeakRef} for the given {@code referent} with the given
 	 * {@code Runnable} for notification.
 	 * <p>
 	 * <b>Note:</b> Within the {@code Runnable} calling {@code WeakRef.get()}
 	 * will always return {@code null}!
 	 * 
 	 * @param referent
 	 *            the instance to track for garbage collection
 	 * @param runnable
 	 *            the {@code Runnable} for notification
 	 */
 	public WeakRef(final T referent, final Runnable runnable) {
 		ref = new WeakReference<T>(referent, REF_QUEUE);
 		synchronized (REMOVE_ACTIONS) {
 			REMOVE_ACTIONS.put(ref, runnable);
 		}
 	}
 
 	/**
 	 * Get the tracked instance.
 	 * 
 	 * @return the tracked instance or {@code null} if the instance has been
 	 *         garbage collected
 	 */
 	public T get() {
 		return ref.get();
 	}
 
 	/**
 	 * The {@code Remover} job waits for gc-ed instances and notifies the
 	 * <i>owner</i> of the instance with its {@code Runnable}.
 	 */
 	private static class Remover extends Job {
 
 		private static final int REMOVE_TIMEOUT = 1;
		private static final String WE_ARE_FAMILY = Activator.PLUGIN_ID;
 
 		public Remover() {
 			super("WeakRef remover"); //$NON-NLS-1$
 			setSystem(true);
 			schedule();
 		}
 
 		@Override
 		public boolean belongsTo(final Object family) {
 			return WE_ARE_FAMILY.equals(family);
 		}
 
 		@Override
 		protected IStatus run(final IProgressMonitor monitor) {
 			try {
 				while (!monitor.isCanceled()) {
 					final Reference<?> removed = REF_QUEUE.remove(REMOVE_TIMEOUT);
 					if (removed == null) {
 						continue;
 					}
 					Runnable runnable;
 					synchronized (REMOVE_ACTIONS) {
 						runnable = REMOVE_ACTIONS.remove(removed);
 					}
 					if (runnable != null) {
 						try {
 							runnable.run();
 						} catch (final Throwable t) {
 							LOGGER.log(LogService.LOG_ERROR, "Got exception executing remove notification.", t); //$NON-NLS-1$
 						}
 					}
 				}
 			} catch (final InterruptedException e) {
 				LOGGER.log(LogService.LOG_ERROR, "WeakRef remover has been interrupted.", e); //$NON-NLS-1$
 				Thread.currentThread().interrupt();
 				return Status.CANCEL_STATUS;
 			}
 			return Status.CANCEL_STATUS;
 		}
 	}
 
 }
