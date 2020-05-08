 /**
  * Copyright (c) 2010, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  */
 package org.eclipse.mylyn.docs.intent.core.document.descriptionunit.impl;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.mylyn.docs.intent.core.document.descriptionunit.DescriptionBloc;
 import org.eclipse.mylyn.docs.intent.core.document.descriptionunit.DescriptionUnitPackage;
import org.eclipse.mylyn.docs.intent.markup.markup.Container;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>Description Bloc</b></em>'. <!--
  * end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
 *   <li>{@link org.eclipse.mylyn.docs.intent.core.document.descriptionunit.impl.DescriptionBlocImpl#getDescriptionBloc <em>Description Bloc</em>}</li>
  * </ul>
  * </p>
 *
 * @generated
  */
 public class DescriptionBlocImpl extends DescriptionUnitInstructionImpl implements DescriptionBloc {
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected DescriptionBlocImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return DescriptionUnitPackage.Literals.DESCRIPTION_BLOC;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
	 * @generated NOT
 	 */
 	public org.eclipse.mylyn.docs.intent.markup.markup.Container getDescriptionBloc() {
 		return (org.eclipse.mylyn.docs.intent.markup.markup.Container)eGet(
 				DescriptionUnitPackage.Literals.DESCRIPTION_BLOC__DESCRIPTION_BLOC, true);
 	}
 
 	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDescriptionBloc(Container newDescriptionBloc) {
		eSet(DescriptionUnitPackage.Literals.DESCRIPTION_BLOC__DESCRIPTION_BLOC, newDescriptionBloc);
	}

	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
	 * @generated NOT
 	 */
 	public void setDescriptionBloc(org.eclipse.mylyn.docs.intent.markup.markup.Container newDescriptionBloc) {
 		eSet(DescriptionUnitPackage.Literals.DESCRIPTION_BLOC__DESCRIPTION_BLOC, newDescriptionBloc);
 	}
 
 } // DescriptionBlocImpl
