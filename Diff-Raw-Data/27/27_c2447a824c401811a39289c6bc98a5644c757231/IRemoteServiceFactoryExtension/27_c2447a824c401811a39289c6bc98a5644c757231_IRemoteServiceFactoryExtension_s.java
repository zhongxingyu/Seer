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
 package org.eclipse.riena.communication.core.factory;
 
 import org.eclipse.riena.core.injector.extension.ExtensionInterface;
 
 /**
  * {@code ExtensionInterface} for reading the available Remote Service Factories
 * <p>
 * <b>Note:</b> The "remoteservicefactory" (lower case!) is @deprecated.
  */
@ExtensionInterface(id = "remoteservicefactory,remoteServiceFactories")
 public interface IRemoteServiceFactoryExtension {
 
 	/**
 	 * Get the protocol property.
 	 * 
 	 * @return the protocol property
 	 */
 	String getProtocol();
 
 	/**
 	 * Create an instance of a {@code IRemoteServiceFactory}.
 	 * 
 	 * @return the RemoteServiceFactory instance for this protocol
 	 */
 	IRemoteServiceFactory createRemoteServiceFactory();
 }
