 /******************************************************************************* 
  * Copyright (c) 2011 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.jst.web.validation;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.CoreException;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.text.ITextSourceReference;
 import org.jboss.tools.common.validation.ValidationErrorManager;
 import org.jboss.tools.jst.web.WebModelPlugin;
 import org.jboss.tools.jst.web.model.project.ext.store.XMLValueInfo;
 
 /**
  * @author Viacheslav Kabanovich
  */
 public class Check {
 	static String ATTR_PATH = "path"; //$NON-NLS-1$
 	static String ATTR_ATTRIBUTE = "attribute"; //$NON-NLS-1$
 	protected String preference;
 	protected String attr;
	ValidationErrorManager manager;
 
 	public Check(ValidationErrorManager manager, String preference, String attr) {
 		this.manager = manager;
 		this.preference = preference;
 		this.attr = attr;			
 	}
 
 	public void check(XModelObject object) {			
 	}
 
 	protected void fireMessage(XModelObject object, String message) {
 		ITextSourceReference ref = getSourceReference(object, attr);
 		IMarker m = manager.addError(message, preference, ref, ref.getResource());
 		bindMarkerToPathAndAttribute(m, object, attr);
 	}
 
 	ITextSourceReference getSourceReference(XModelObject o, String attr) {
 		return new XMLValueInfo(o, attr);
 	}
 
 	public static void bindMarkerToPathAndAttribute(IMarker marker, XModelObject object, String attr) {
 		if(marker != null) {
 			try {
 				marker.setAttribute(ATTR_PATH, object.getPath());
 				marker.setAttribute(ATTR_ATTRIBUTE, attr);
 			} catch (CoreException e) {
 				WebModelPlugin.getDefault().logError(e);
 			}
 		}
 	}
 
 }
 
