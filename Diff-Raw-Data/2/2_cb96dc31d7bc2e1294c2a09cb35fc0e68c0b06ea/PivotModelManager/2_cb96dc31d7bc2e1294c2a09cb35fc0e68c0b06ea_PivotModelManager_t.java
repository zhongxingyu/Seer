 /**
  * <copyright>
  *
  * Copyright (c) 2010,2013 E.D.Willink and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   E.D.Willink - Initial API and implementation
  *   E.D.Willink (CEA LIST) - Bug 392981
  *
  * </copyright>
  */
 package org.eclipse.ocl.examples.pivot.evaluation;
 
 import org.apache.log4j.Logger;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.ocl.examples.domain.elements.DomainType;
 import org.eclipse.ocl.examples.domain.utilities.DomainUtil;
 import org.eclipse.ocl.examples.library.executor.LazyModelManager;
 import org.eclipse.ocl.examples.pivot.ParserException;
 import org.eclipse.ocl.examples.pivot.PivotPackage;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 
 public class PivotModelManager extends LazyModelManager
 {	
 	private static final Logger logger = Logger.getLogger(PivotModelManager.class);
 
 	protected final @NonNull MetaModelManager metaModelManager;
 	private boolean generatedErrorMessage = false;
 	
 	public PivotModelManager(@NonNull MetaModelManager metaModelManager, EObject context) {
 		super(context);
 		this.metaModelManager = metaModelManager;
 	}
 
 	@Override
 	protected boolean isInstance(@NonNull DomainType requiredType, @NonNull EObject eObject) {
 		EClass eClass = eObject.eClass();
 		EPackage ePackage = eClass.getEPackage();
 		Type objectType = null;
 		if (ePackage == PivotPackage.eINSTANCE) {
 			String name = DomainUtil.nonNullEMF(eClass.getName());
 			objectType = metaModelManager.getPivotType(name);
 		}
 		else {
 			try {
 				objectType = metaModelManager.getPivotOf(Type.class,  eClass);
 			} catch (ParserException e) {
 				if (!generatedErrorMessage) {
 					generatedErrorMessage = true;
 					logger.error("Failed to load an '" + eClass.getName() + "'", e);
 				}
 			}
 		}
	    return (objectType != null) && objectType.conformsTo(metaModelManager, requiredType);
 	}
 }
