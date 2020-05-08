 /*******************************************************************************
  * Copyright (c) 2004, 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.jsf.facesconfig.ui.util;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.emf.ManagedBeanType;
import org.eclipse.jst.jsf.facesconfig.util.FacesConfigArtifactEdit;
 
 /**
  * 
  * @author sfshi
  * 
  */
 public class ManagedBeanUtil {
 
 	/**
 	 * Determines if the new bean to be added is already a member of the
 	 * configuration set.
 	 * 
 	 * @param beanName -
 	 *            The name of the bean being added
 	 * @return int - 0 if bean doesn't exist, otherwise the choice from the
 	 *         Duplicate Bean dialog
 	 */
	public static boolean isBeanDuplicate(IProject project, String beanName) {
		// TODO we may need to consider the managed bean declared in other
		// faces config file in the project, not only the default faces config
		// file.
		FacesConfigArtifactEdit artifactEdit = FacesConfigArtifactEdit
				.getFacesConfigArtifactEditForRead(project);
		if (artifactEdit != null) {
			FacesConfigType facesConfig = artifactEdit.getFacesConfig();
			List beans = facesConfig.getManagedBean();
 
 			// Iterate through the bean list
 			for (Iterator i = beans.iterator(); i.hasNext();) {
 				Object o = i.next();
 				if (o instanceof ManagedBeanType) {
 					ManagedBeanType mbti = (ManagedBeanType) o;
 					if (mbti.getManagedBeanName() != null) {
 						String name = mbti.getManagedBeanName()
 								.getTextContent();
 						if (name != null && name.equals(beanName)) {
 							return true;
 						}
 					}
 				}
 			}
 
 		}
 		return false;
 	}
 
 	/**
 	 * get the default managed bean name in the current project according to
 	 * reference name
 	 * 
 	 * @param project -
 	 *            current project
 	 * @param refName -
 	 *            seed reference name
 	 * @return String - default managed bean name
 	 */
 	public static String getDefaultManagedBeanName(IProject project,
 			String refName) {
 		String defaultName = refName;
 
 		int newRefNameIndex = 1;
 		while (isBeanDuplicate(project, defaultName)) {
 			defaultName = refName + newRefNameIndex;
 			newRefNameIndex++;
 		}
 		return defaultName;
 	}
 }
