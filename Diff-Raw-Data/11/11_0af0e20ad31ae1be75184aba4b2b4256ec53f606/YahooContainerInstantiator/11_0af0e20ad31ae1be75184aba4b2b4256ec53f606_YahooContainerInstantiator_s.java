 /**
  * Copyright (c) 2002-2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	- Initial API and implementation
  *  	- Chris Aniszczyk <zx@us.ibm.com>
  *   	- Borna Safabakhsh <borna@us.ibm.com> 
  *   
  * $Id$
  */
 
 package org.eclipse.ecf.provider.yahoo.container;
 
 import org.eclipse.ecf.core.ContainerCreateException;
 import org.eclipse.ecf.core.ContainerTypeDescription;
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.core.identity.IDCreateException;
 import org.eclipse.ecf.core.provider.IContainerInstantiator;
 
 public class YahooContainerInstantiator implements IContainerInstantiator {
 	
 	public IContainer createInstance(ContainerTypeDescription description, Object[] args) throws ContainerCreateException {
 		ID guid;
 		try {
 			guid = IDFactory.getDefault().createGUID();
 		} catch (IDCreateException e) {
 			throw new ContainerCreateException("Exception creating ID",e);
 		}
 		return new YahooContainer(guid);
 	}
 	
 }
