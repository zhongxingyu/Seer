/**
 *  Copyright (c) 2008 - 2010 Obeo.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *      Obeo - initial API and implementation
  *
 */
 package org.eclipse.emf.eef.views.providers;
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.eef.toolkits.providers.ToolkitPropertiesEditionProvider;
 import org.eclipse.emf.eef.toolkits.providers.WidgetPropertiesEditionProvider;
 import org.eclipse.emf.eef.toolkits.util.ToolkitsAdapterFactory;
 
 
 /**
  * @author <a href="mailto:nathalie.lepine@obeo.fr">Nathalie Lepine</a>
  * 
  */
 public class ToolkitsEEFAdapterFactory extends ToolkitsAdapterFactory {
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.toolkits.util.ToolkitsAdapterFactory#createToolkitAdapter()
 	 * 
 	 */
 	public Adapter createToolkitAdapter() {
 		return new ToolkitPropertiesEditionProvider();
 	}
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.toolkits.util.ToolkitsAdapterFactory#createWidgetAdapter()
 	 * 
 	 */
 	public Adapter createWidgetAdapter() {
 		return new WidgetPropertiesEditionProvider();
 	}
 
 }
