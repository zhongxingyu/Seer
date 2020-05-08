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
 package org.eclipse.riena.internal.communication.core.proxyselector;
 
 import java.net.ProxySelector;
 
 import org.eclipse.riena.core.injector.extension.ExtensionInterface;
 import org.eclipse.riena.core.injector.extension.MapName;
 
 /**
  * Extension interface for defining {@code ProxySelector}s.
  */
@ExtensionInterface(id = "proxySelectors")
 public interface IProxySelectorExtension {
 
 	/**
 	 * A descriptive name for the proxy selector.
 	 * 
 	 * @return the name
 	 */
 	String getName();
 
 	/**
 	 * Proxy selectors will be ordered, i.e. the proxy selector with the lowest
 	 * order will be used first.
 	 * 
 	 * @return the order
 	 */
 	int getOrder();
 
 	/**
 	 * Create an instance of the proxy selector
 	 * 
 	 * @return the proxy selector
 	 */
 	@MapName("class")
 	ProxySelector createProxySelector();
 }
