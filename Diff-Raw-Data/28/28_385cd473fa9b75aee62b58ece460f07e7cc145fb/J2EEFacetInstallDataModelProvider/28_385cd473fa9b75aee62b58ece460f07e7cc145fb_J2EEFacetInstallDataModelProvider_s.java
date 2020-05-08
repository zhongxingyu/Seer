 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.project.facet;
 
 import java.util.Set;
 
 import org.eclipse.wst.common.componentcore.datamodel.FacetInstallDataModelProvider;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 
 public abstract class J2EEFacetInstallDataModelProvider extends FacetInstallDataModelProvider  implements IJ2EEFacetInstallDataModelProperties{
 
 	public Set getPropertyNames() {
 		Set names = super.getPropertyNames();
 		names.add(RUNTIME_TARGET_ID);
 		names.add(FACET_RUNTIME);
 		// added for jee projects that make deployment descriptors optional
 		names.add(GENERATE_DD);
 		return names;
 	}
 
 	protected final int getJ2EEVersion() {
 		return convertFacetVersionToJ2EEVersion((IProjectFacetVersion) getProperty(FACET_VERSION));
 	}
 
 	protected abstract int convertFacetVersionToJ2EEVersion(IProjectFacetVersion version);
	
 }
