 /*******************************************************************************
  * Copyright (c) 2003, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.web.project.facet;
 
 import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 public class WebFacetUtils {
 
     public static final IProjectFacet WEB_FACET = ProjectFacetsManager.getProjectFacet(IJ2EEFacetConstants.DYNAMIC_WEB);
 	public static final IProjectFacetVersion WEB_22 = WEB_FACET.getVersion("2.2"); //$NON-NLS-1$
 	public static final IProjectFacetVersion WEB_23 = WEB_FACET.getVersion("2.3"); //$NON-NLS-1$
 	public static final IProjectFacetVersion WEB_24 = WEB_FACET.getVersion("2.4"); //$NON-NLS-1$
 	public static final IProjectFacetVersion WEB_25 = WEB_FACET.getVersion("2.5"); //$NON-NLS-1$
 	
	public static final IProjectFacet WEB_XDOCLET_FACET = ProjectFacetsManager.getProjectFacet(IJ2EEFacetConstants.DYNAMIC_WEB_XDOCLET); 
 	
 	public static IProjectFacet getProjectFacet(String id) {
 		try {
 			return ProjectFacetsManager.getProjectFacet(id);
 		} catch (IllegalArgumentException e) {
 			return null;
 		} 
 	}
 
 }
