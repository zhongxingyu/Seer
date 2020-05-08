 /*******************************************************************************
  * Copyright (c) 2007 Composent, Inc. and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.internal.provider.jgroups;
 

 import org.eclipse.ecf.core.ContainerCreateException;
 import org.eclipse.ecf.core.ContainerTypeDescription;
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.IDCreateException;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.provider.generic.GenericContainerInstantiator;
 import org.eclipse.ecf.provider.generic.SOContainerConfig;
 import org.eclipse.ecf.provider.jgroups.container.JGroupsClientContainer;
 import org.eclipse.ecf.provider.jgroups.identity.JGroupsID;
 import org.eclipse.ecf.provider.jgroups.identity.JGroupsNamespace;
 
 /**
  *
  */
 public class JGroupsClientContainerInstantiator extends
 		GenericContainerInstantiator {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ecf.core.provider.BaseContainerInstantiator#createInstance
 	 * (org.eclipse.ecf.core.ContainerTypeDescription, java.lang.Object[])
 	 */
 	public IContainer createInstance(ContainerTypeDescription description,
 			Object[] parameters) throws ContainerCreateException {
 		try {
 			ID newID = null;
 			if (parameters != null && parameters.length > 0) {
 				if (parameters[0] instanceof JGroupsID)
 					newID = (ID) parameters[0];
 				else if (parameters[0] instanceof String)
 					newID = IDFactory.getDefault().createID(
 							JGroupsNamespace.NAME, (String) parameters[0]);
 			} else
 				newID = IDFactory.getDefault()
 						.createID(
 								JGroupsNamespace.NAME,
 								JGroupsNamespace.SCHEME
 										+ JGroupsNamespace.SCHEME_SEPARATOR
 										+ "///"
 										+ IDFactory.getDefault().createGUID()
 												.getName());
 			if (newID == null)
 				throw new ContainerCreateException(
 						"invalid parameters for creating client instance");
 			return new JGroupsClientContainer(new SOContainerConfig(newID));
 		} catch (final IDCreateException e) {
 			throw new ContainerCreateException(
 					"Exception creating jgroups client container", e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.eclipse.ecf.core.provider.BaseContainerInstantiator#
 	 * getSupportedParameterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
 	 */
 	public Class[][] getSupportedParameterTypes(
 			ContainerTypeDescription description) {
 		return new Class[][] { { JGroupsID.class }, { String.class }, {} };
 	}
 }
