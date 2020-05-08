 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mgorning - Bug 343983 - Notification for Cancelled Reconnection Events
  *    mwenz - Bug 364035 - DefaultReconnectionFeature#reconnect should use getNewAnchor(context)
  *                         not context.getNewAnchor()
  *    Henrik Rentz-Reichert - mwenz - Bug 376544 - bug in re-connecting a connection with identical start and end anchor
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.features.impl;
 
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IReconnectionFeature;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.IReconnectionContext;
 import org.eclipse.graphiti.features.context.impl.ReconnectionContext;
 import org.eclipse.graphiti.internal.Messages;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 
 /**
  * The default implementation for the {@link IReconnectionFeature} interface. Is
  * used by default by the framework for any reconnection requests triggered in
  * the diagram. May be subclassed and adapted by clients.
  */
 public class DefaultReconnectionFeature extends AbstractFeature implements IReconnectionFeature {
 
 	/**
 	 * Creates a new {@link DefaultReconnectionFeature}.
 	 * 
 	 * @param fp
 	 *            the feature provider to use
 	 */
 	public DefaultReconnectionFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	/**
 	 * Called by the framework to check if this feature can perform the
 	 * currently triggered reconnection request. The default implementation
 	 * checks if
 	 * <ul>
 	 * <li>A connection and a new target anchor are passed in the given
 	 * {@link IReconnectionContext}</li>
 	 * <li>The connection passed in the context has a start and end anchor</li>
 	 * <li>The new anchor is not located directly in the diagram</li>
 	 * </ul>
 	 * Can be overridden by clients to add additional checks.
 	 * 
 	 * @param context
 	 *            the context object holding all the reconnection information
 	 * @return <code>true</code> if the feature is able to deal with the
 	 *         reconnection request, <code>false</code> otherwise or if the tool
 	 *         needs to prohibit the reconnection for the given situation.
 	 */
 	public boolean canReconnect(IReconnectionContext context) {
 		Connection connection = context.getConnection();
 		Anchor newAnchor = getNewAnchor(context);
 		boolean ret = (connection != null) && (newAnchor != null) && (connection.getStart() != null)
 				&& (connection.getEnd() != null) && !(newAnchor.getParent() instanceof Diagram);
 		return ret;
 	}
 
 	/**
 	 * Can be overridden by the user to influence the target anchor to use for
 	 * the reconnect.
 	 * 
 	 * @param context
 	 *            the context holding the reconnection information
 	 * 
 	 * @return the new anchor to use as target for the reconnect
 	 */
 	protected Anchor getNewAnchor(IReconnectionContext context) {
 		return context.getNewAnchor();
 	}
 
 	/**
 	 * Called by the framework to perform the currently triggered reconnection
 	 * request. The default implementation calls the
 	 * {@link #preReconnect(IReconnectionContext)} and
 	 * {@link #postReconnect(IReconnectionContext)} hooks. The reconnect is done
 	 * by either replacing the start anchor (in case it matches the old anchor
 	 * passed in the context) or the end anchor with the new anchor.
 	 * 
 	 * @param context
 	 *            the context object holding all the reconnection information
 	 * @nooverride This method is not intended to be re-implemented or extended
 	 *             by clients. Clients should override
 	 *             {@link #reconnect(IReconnectionContext)} instead.
 	 */
 	public final void reconnect(IReconnectionContext context) {
 		if (!getUserDecision()) {
 			return;
 		}
 
 		preReconnect(context);
 
 		Connection connection = context.getConnection();
 		Anchor newAnchor = context.getNewAnchor();
 
 		if (context.getReconnectType().equals(ReconnectionContext.RECONNECT_SOURCE)) {
			connection.setStart(newAnchor);
 		} else {
 			connection.setEnd(newAnchor);
 		}
 
 		postReconnect(context);
 	}
 
 	/**
 	 * Hook that is called by the {@link #reconnect(IReconnectionContext)}
 	 * method before the actual reconnect is done. Can be overridden by clients
 	 * to add additional functionality.
 	 * 
 	 * @param context
 	 *            the context object holding all the reconnection information
 	 */
 	public void preReconnect(IReconnectionContext context) {
 	}
 
 	/**
 	 * Hook that is called by the {@link #reconnect(IReconnectionContext)}
 	 * method after the actual reconnect is done. Can be overridden by clients
 	 * to add additional functionality.
 	 * 
 	 * @param context
 	 *            the context object holding all the reconnection information
 	 */
 	public void postReconnect(IReconnectionContext context) {
 	}
 
 	/**
 	 * Called by the framework to check if this feature can perform the
 	 * currently triggered reconnection request. Delegates to the
 	 * {@link #canReconnect(IReconnectionContext)} method in case the passed
 	 * context is a {@link IReconnectionContext}.
 	 * 
 	 * @param context
 	 *            the context object holding all the reconnection information
 	 * @return <code>true</code> if the feature is able to deal with the
 	 *         reconnection request, <code>false</code> otherwise or if the tool
 	 *         needs to prohibit the reconnection for the given situation.
 	 * @nooverride This method is not intended to be re-implemented or extended
 	 *             by clients. Clients should override
 	 *             {@link #canReconnect(IReconnectionContext)} instead.
 	 */
 	public boolean canExecute(IContext context) {
 		boolean ret = false;
 		if (context instanceof IReconnectionContext) {
 			ret = canReconnect((IReconnectionContext) context);
 		}
 		return ret;
 	}
 
 	/**
 	 * Called by the framework to execute this feature to perform the currently
 	 * triggered reconnection request. Delegates to the
 	 * {@link #reconnect(IReconnectionContext)} method in case the passed
 	 * context is a {@link IReconnectionContext}.
 	 * 
 	 * @param context
 	 *            the context object holding all the reconnection information
 	 * @nooverride This method is not intended to be re-implemented or extended
 	 *             by clients. Clients should override
 	 *             {@link #reconnect(IReconnectionContext)} instead.
 	 */
 	public void execute(IContext context) {
 		if (context instanceof IReconnectionContext) {
 			reconnect((IReconnectionContext) context);
 		}
 	}
 
 	/**
 	 * Returns the display name of the feature to use e.g. within a context
 	 * menu.
 	 * 
 	 * @return the display name of the feature
 	 */
 	@Override
 	public String getName() {
 		return NAME;
 	}
 
 	private static final String NAME = Messages.DefaultReconnectionFeature_0_xfld;
 
 	/**
 	 * Called by the framework in case a started reconnection operation has been
 	 * cancelled, e.g. by pressing ESC, selecting another tool from the palette
 	 * etc.
 	 * 
 	 * @param context
 	 *            the context object holding all the reconnection information
 	 * @since 0.9
 	 */
 	public void canceledReconnect(IReconnectionContext context) {
 	}
 }
