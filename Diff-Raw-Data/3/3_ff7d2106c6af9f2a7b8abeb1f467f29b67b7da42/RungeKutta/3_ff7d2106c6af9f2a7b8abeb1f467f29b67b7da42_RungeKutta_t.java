 package org.eclipse.stem.solvers.rk;
 
 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import org.eclipse.stem.core.solver.Solver;
 
 /**
  * <!-- begin-user-doc -->
  * A representation of the model object '<em><b>Runge Kutta</b></em>'.
  * <!-- end-user-doc -->
  *
  * <p>
  * The following features are supported:
  * <ul>
  *   <li>{@link org.eclipse.stem.solvers.rk.RungeKutta#getRelativeTolerance <em>Relative Tolerance</em>}</li>
  * </ul>
  * </p>
  *
  * @see org.eclipse.stem.solvers.rk.RkPackage#getRungeKutta()
  * @model
  * @generated
  */
 public interface RungeKutta extends Solver {
	
	public final static String URI_TYPE_SOLVER_SEGMENT = "RungeKuttaSolver";
	
 	/**
 	 * Returns the value of the '<em><b>Relative Tolerance</b></em>' attribute.
 	 * The default value is <code>"1E-9"</code>.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Relative Tolerance</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Relative Tolerance</em>' attribute.
 	 * @see #setRelativeTolerance(double)
 	 * @see org.eclipse.stem.solvers.rk.RkPackage#getRungeKutta_RelativeTolerance()
 	 * @model default="1E-9"
 	 * @generated
 	 */
 	double getRelativeTolerance();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.stem.solvers.rk.RungeKutta#getRelativeTolerance <em>Relative Tolerance</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Relative Tolerance</em>' attribute.
 	 * @see #getRelativeTolerance()
 	 * @generated
 	 */
 	void setRelativeTolerance(double value);
 
 } // RungeKutta
