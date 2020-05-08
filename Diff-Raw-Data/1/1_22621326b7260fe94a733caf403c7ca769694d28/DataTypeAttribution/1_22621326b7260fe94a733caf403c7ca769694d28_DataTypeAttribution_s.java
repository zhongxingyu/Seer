 /**
  * <copyright>
  *
  * Copyright (c) 2012 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  */
 package org.eclipse.ocl.examples.pivot.attributes;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.ocl.examples.pivot.DataType;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.scoping.AbstractAttribution;
 import org.eclipse.ocl.examples.pivot.scoping.EnvironmentView;
 import org.eclipse.ocl.examples.pivot.scoping.Attribution;
 import org.eclipse.ocl.examples.pivot.scoping.ScopeView;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 
 public class DataTypeAttribution extends AbstractAttribution
 {
 	public static final DataTypeAttribution INSTANCE = new DataTypeAttribution();
 
 	@Override
 	public ScopeView computeLookup(EObject target, EnvironmentView environmentView, ScopeView scopeView) {
 		DataType targetElement = (DataType) target;
 		Type behavioralType = targetElement.getBehavioralType();
 		Attribution attribution;
 		if (behavioralType != null) {
 			attribution = PivotUtil.getAttribution(behavioralType);
 		}
 		else {
 			attribution = ClassAttribution.INSTANCE;
 		}
 		return attribution.computeLookup(target, environmentView, scopeView);
 	}
 }
