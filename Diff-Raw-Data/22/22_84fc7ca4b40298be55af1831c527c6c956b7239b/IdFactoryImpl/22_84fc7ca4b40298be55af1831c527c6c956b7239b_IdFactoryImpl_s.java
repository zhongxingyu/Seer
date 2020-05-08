 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.rmf.pror.reqif10.presentation.id.impl;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;

 import org.eclipse.emf.ecore.impl.EFactoryImpl;

 import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.rmf.pror.reqif10.presentation.id.*;
 
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Factory</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class IdFactoryImpl extends EFactoryImpl implements IdFactory {
 	/**
 	 * Creates the default factory implementation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static IdFactory init() {
 		try {
 			IdFactory theIdFactory = (IdFactory)EPackage.Registry.INSTANCE.getEFactory("http://pror.org/presentation/id"); 
 			if (theIdFactory != null) {
 				return theIdFactory;
 			}
 		}
 		catch (Exception exception) {
 			EcorePlugin.INSTANCE.log(exception);
 		}
 		return new IdFactoryImpl();
 	}
 
 	/**
 	 * Creates an instance of the factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IdFactoryImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public EObject create(EClass eClass) {
 		switch (eClass.getClassifierID()) {
 			case IdPackage.ID_CONFIGURATION: return createIdConfiguration();
 			default:
 				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IdConfiguration createIdConfiguration() {
 		IdConfigurationImpl idConfiguration = new IdConfigurationImpl();
 		return idConfiguration;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IdPackage getIdPackage() {
 		return (IdPackage)getEPackage();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @deprecated
 	 * @generated
 	 */
 	@Deprecated
 	public static IdPackage getPackage() {
 		return IdPackage.eINSTANCE;
 	}
 
 } //IdFactoryImpl
