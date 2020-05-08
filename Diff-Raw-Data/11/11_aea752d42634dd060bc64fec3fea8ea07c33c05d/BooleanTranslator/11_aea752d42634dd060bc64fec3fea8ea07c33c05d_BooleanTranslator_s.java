 /*******************************************************************************
  * Copyright (c) 2001, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Apr 21, 2003
  *
  * To change the template for this generated file go to
  * Window>Preferences>Java>Code Generation>Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.model.translator.common;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.wst.common.internal.emf.resource.Translator;
 
 /**
  * @author administrator
  *
  * To change the template for this generated type comment go to
  * Window>Preferences>Java>Code Generation>Code and Comments
  */
 public class BooleanTranslator extends Translator {
 
 	
 	/**
 	 * @param domNameAndPath
 	 * @param aFeature
 	 */
 	public BooleanTranslator(String domNameAndPath, EStructuralFeature aFeature) {
 		super(domNameAndPath, aFeature, BOOLEAN_LOWERCASE);
 	}
 
 		
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.emf2xml.impl.Translator#convertStringToValue(java.lang.String, org.eclipse.emf.ecore.EObject)
 	 */
 	public Object convertStringToValue(String strValue, EObject owner) {
 		if (strValue == null)
 			return Boolean.FALSE;
		if (!Boolean.valueOf(strValue).booleanValue()) {
			if (strValue.toUpperCase().equals("1") || strValue.toUpperCase().equals("YES")) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
		return Boolean.FALSE;
 	}
 
 
 }
