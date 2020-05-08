 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.server.model.provider;
 
 import org.eclipse.emf.common.EMFPlugin;
 import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.emfstore.internal.common.model.edit.edit.provider.CommonEditPlugin;
 
 /**
  * This is the central singleton for the Server edit plugin.
  * <!-- begin-user-doc
  * --> <!-- end-user-doc -->
  * 
  * @generated
  */
 public final class ServerEditPlugin extends EMFPlugin {
 	/**
 	 * Keep track of the singleton. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	public static final ServerEditPlugin INSTANCE = new ServerEditPlugin();
 
 	/**
 	 * Keep track of the singleton. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	private static Implementation plugin;
 
 	/**
 	 * Create the instance.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ServerEditPlugin() {
 		super(new ResourceLocator[] { CommonEditPlugin.INSTANCE, });
 	}
 
 	/**
 	 * Returns the singleton instance of the Eclipse plugin.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @return the singleton instance.
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getPluginResourceLocator() {
 		return plugin;
 	}
 
 	/**
 	 * Returns the singleton instance of the Eclipse plugin.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @return the singleton instance.
 	 * @generated
 	 */
 	public static Implementation getPlugin() {
 		return plugin;
 	}
 
 	/**
 	 * The actual implementation of the Eclipse <b>Plugin</b>. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public static class Implementation extends EclipsePlugin {
 		/**
 		 * Creates an instance.
 		 * <!-- begin-user-doc --> <!-- end-user-doc -->
 		 * 
 		 * @generated
 		 */
 		public Implementation() {
 			super();
 
 			// Remember the static instance.
 			//
 			plugin = this;
 		}
 	}
 
 }
