 /**
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Tanja Mayerhofer - initial API and implementation
  */
 package org.modelexecution.fumldebug.core.trace.tracemodel.impl;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.NotificationChain;
 
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 
 import org.eclipse.emf.ecore.util.BasicInternalEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 import org.modelexecution.fumldebug.core.trace.tracemodel.ActivityExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.Trace;
 
 import fUML.Syntax.Activities.IntermediateActivities.Activity;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Trace</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.modelexecution.fumldebug.core.trace.tracemodel.impl.TraceImpl#getActivityExecutions <em>Activity Executions</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class TraceImpl extends EObjectImpl implements Trace {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2012 Vienna University of Technology.\r\nAll rights reserved. This program and the accompanying materials are made \r\navailable under the terms of the Eclipse Public License v1.0 which accompanies \r\nthis distribution, and is available at http://www.eclipse.org/legal/epl-v10.html\r\n\r\nContributors:\r\nTanja Mayerhofer - initial API and implementation";
 
 	/**
 	 * The cached value of the '{@link #getActivityExecutions() <em>Activity Executions</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getActivityExecutions()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<ActivityExecution> activityExecutions;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public TraceImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return TracemodelPackageImpl.Literals.TRACE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<ActivityExecution> getActivityExecutions() {
 		if (activityExecutions == null) {
 			activityExecutions = new BasicInternalEList<ActivityExecution>(ActivityExecution.class);
 		}
 		return activityExecutions;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case TracemodelPackageImpl.TRACE__ACTIVITY_EXECUTIONS:
 				return ((InternalEList<?>)getActivityExecutions()).basicRemove(otherEnd, msgs);
 		}
 		return eDynamicInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case TracemodelPackageImpl.TRACE__ACTIVITY_EXECUTIONS:
 				return getActivityExecutions();
 		}
 		return eDynamicGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case TracemodelPackageImpl.TRACE__ACTIVITY_EXECUTIONS:
 				getActivityExecutions().clear();
 				getActivityExecutions().addAll((Collection<? extends ActivityExecution>)newValue);
 				return;
 		}
 		eDynamicSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case TracemodelPackageImpl.TRACE__ACTIVITY_EXECUTIONS:
 				getActivityExecutions().clear();
 				return;
 		}
 		eDynamicUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case TracemodelPackageImpl.TRACE__ACTIVITY_EXECUTIONS:
 				return activityExecutions != null && !activityExecutions.isEmpty();
 		}
 		return eDynamicIsSet(featureID);
 	}
 
 	public ActivityExecution getActivityExecutionByID(int activityExecutionID) {
 		List<ActivityExecution> activityExecutions = this.getActivityExecutions();		
 		ActivityExecution activityExecution = null;		
 		for(int i=0;i<activityExecutions.size();++i) {
 			if(activityExecutions.get(i).getActivityExecutionID() == activityExecutionID) {
 				activityExecution = activityExecutions.get(i);
 				break;
 			}
 		}
 		return activityExecution;
 	}
 
 	public ActivityExecution addActivityExecution(Activity activity, int activityExecutionID) {
 		ActivityExecution activityExecution = new ActivityExecutionImpl();
 		activityExecution.setActivity(activity);		
 		activityExecution.setActivityExecutionID(activityExecutionID);				
 		this.getActivityExecutions().add(activityExecution);
 		return activityExecution;
 	}
 
 } //TraceImpl
