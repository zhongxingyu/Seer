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
 
 import java.util.ArrayList;
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
 import org.modelexecution.fumldebug.core.trace.tracemodel.ActivityNodeExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.CallActivityNodeExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.Input;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ObjectTokenInstance;
 import org.modelexecution.fumldebug.core.trace.tracemodel.Output;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ParameterInput;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ParameterOutput;
 import org.modelexecution.fumldebug.core.trace.tracemodel.TokenInstance;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ValueInstance;
 
 import fUML.Semantics.Classes.Kernel.Reference;
 import fUML.Semantics.Classes.Kernel.Value;
 import fUML.Syntax.Actions.BasicActions.CallBehaviorAction;
 import fUML.Syntax.Activities.IntermediateActivities.Activity;
 import fUML.Syntax.Activities.IntermediateActivities.ActivityNode;
 import fUML.Syntax.Activities.IntermediateActivities.ActivityParameterNode;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Activity Execution</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.modelexecution.fumldebug.core.trace.tracemodel.impl.ActivityExecutionImpl#getParameterInputs <em>Parameter Inputs</em>}</li>
  *   <li>{@link org.modelexecution.fumldebug.core.trace.tracemodel.impl.ActivityExecutionImpl#getParameterOutputs <em>Parameter Outputs</em>}</li>
  *   <li>{@link org.modelexecution.fumldebug.core.trace.tracemodel.impl.ActivityExecutionImpl#getNodeExecutions <em>Node Executions</em>}</li>
  *   <li>{@link org.modelexecution.fumldebug.core.trace.tracemodel.impl.ActivityExecutionImpl#getCaller <em>Caller</em>}</li>
  *   <li>{@link org.modelexecution.fumldebug.core.trace.tracemodel.impl.ActivityExecutionImpl#getActivity <em>Activity</em>}</li>
  *   <li>{@link org.modelexecution.fumldebug.core.trace.tracemodel.impl.ActivityExecutionImpl#getActivityExecutionID <em>Activity Execution ID</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ActivityExecutionImpl extends EObjectImpl implements ActivityExecution {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2012 Vienna University of Technology.\r\nAll rights reserved. This program and the accompanying materials are made \r\navailable under the terms of the Eclipse Public License v1.0 which accompanies \r\nthis distribution, and is available at http://www.eclipse.org/legal/epl-v10.html\r\n\r\nContributors:\r\nTanja Mayerhofer - initial API and implementation";
 
 	/**
 	 * The cached value of the '{@link #getParameterInputs() <em>Parameter Inputs</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getParameterInputs()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<ParameterInput> parameterInputs;
 
 	/**
 	 * The cached value of the '{@link #getParameterOutputs() <em>Parameter Outputs</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getParameterOutputs()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<ParameterOutput> parameterOutputs;
 
 	/**
 	 * The cached value of the '{@link #getNodeExecutions() <em>Node Executions</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getNodeExecutions()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<ActivityNodeExecution> nodeExecutions;
 
 	/**
 	 * The cached value of the '{@link #getCaller() <em>Caller</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCaller()
 	 * @generated
 	 * @ordered
 	 */
 	protected CallActivityNodeExecution caller;
 
 	/**
 	 * The default value of the '{@link #getActivity() <em>Activity</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getActivity()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Activity ACTIVITY_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getActivity() <em>Activity</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getActivity()
 	 * @generated
 	 * @ordered
 	 */
 	protected Activity activity = ACTIVITY_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getActivityExecutionID() <em>Activity Execution ID</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getActivityExecutionID()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int ACTIVITY_EXECUTION_ID_EDEFAULT = 0;
 
 	/**
 	 * The cached value of the '{@link #getActivityExecutionID() <em>Activity Execution ID</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getActivityExecutionID()
 	 * @generated
 	 * @ordered
 	 */
 	protected int activityExecutionID = ACTIVITY_EXECUTION_ID_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ActivityExecutionImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return TracemodelPackageImpl.Literals.ACTIVITY_EXECUTION;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<ParameterInput> getParameterInputs() {
 		if (parameterInputs == null) {
 			parameterInputs = new BasicInternalEList<ParameterInput>(ParameterInput.class);
 		}
 		return parameterInputs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<ParameterOutput> getParameterOutputs() {
 		if (parameterOutputs == null) {
 			parameterOutputs = new BasicInternalEList<ParameterOutput>(ParameterOutput.class);
 		}
 		return parameterOutputs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<ActivityNodeExecution> getNodeExecutions() {
 		if (nodeExecutions == null) {
 			nodeExecutions = new BasicInternalEList<ActivityNodeExecution>(ActivityNodeExecution.class);
 		}
 		return nodeExecutions;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public CallActivityNodeExecution getCaller() {
 		if (caller != null && caller.eIsProxy()) {
 			InternalEObject oldCaller = (InternalEObject)caller;
 			caller = (CallActivityNodeExecution)eResolveProxy(oldCaller);
 			if (caller != oldCaller) {
 			}
 		}
 		return caller;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public CallActivityNodeExecution basicGetCaller() {
 		return caller;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetCaller(CallActivityNodeExecution newCaller, NotificationChain msgs) {
 		CallActivityNodeExecution oldCaller = caller;
 		caller = newCaller;
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCaller(CallActivityNodeExecution newCaller) {
 		if (newCaller != caller) {
 			NotificationChain msgs = null;
 			if (caller != null)
 				msgs = ((InternalEObject)caller).eInverseRemove(this, TracemodelPackageImpl.CALL_ACTIVITY_NODE_EXECUTION__CALLEE, CallActivityNodeExecution.class, msgs);
 			if (newCaller != null)
 				msgs = ((InternalEObject)newCaller).eInverseAdd(this, TracemodelPackageImpl.CALL_ACTIVITY_NODE_EXECUTION__CALLEE, CallActivityNodeExecution.class, msgs);
 			msgs = basicSetCaller(newCaller, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Activity getActivity() {
 		return activity;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setActivity(Activity newActivity) {
 		activity = newActivity;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public int getActivityExecutionID() {
 		return activityExecutionID;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setActivityExecutionID(int newActivityExecutionID) {
 		activityExecutionID = newActivityExecutionID;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__NODE_EXECUTIONS:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getNodeExecutions()).basicAdd(otherEnd, msgs);
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__CALLER:
 				if (caller != null)
 					msgs = ((InternalEObject)caller).eInverseRemove(this, TracemodelPackageImpl.CALL_ACTIVITY_NODE_EXECUTION__CALLEE, CallActivityNodeExecution.class, msgs);
 				return basicSetCaller((CallActivityNodeExecution)otherEnd, msgs);
 		}
 		return eDynamicInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__PARAMETER_INPUTS:
 				return ((InternalEList<?>)getParameterInputs()).basicRemove(otherEnd, msgs);
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__PARAMETER_OUTPUTS:
 				return ((InternalEList<?>)getParameterOutputs()).basicRemove(otherEnd, msgs);
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__NODE_EXECUTIONS:
 				return ((InternalEList<?>)getNodeExecutions()).basicRemove(otherEnd, msgs);
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__CALLER:
 				return basicSetCaller(null, msgs);
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
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__PARAMETER_INPUTS:
 				return getParameterInputs();
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__PARAMETER_OUTPUTS:
 				return getParameterOutputs();
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__NODE_EXECUTIONS:
 				return getNodeExecutions();
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__CALLER:
 				if (resolve) return getCaller();
 				return basicGetCaller();
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__ACTIVITY:
 				return getActivity();
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__ACTIVITY_EXECUTION_ID:
 				return getActivityExecutionID();
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
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__PARAMETER_INPUTS:
 				getParameterInputs().clear();
 				getParameterInputs().addAll((Collection<? extends ParameterInput>)newValue);
 				return;
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__PARAMETER_OUTPUTS:
 				getParameterOutputs().clear();
 				getParameterOutputs().addAll((Collection<? extends ParameterOutput>)newValue);
 				return;
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__NODE_EXECUTIONS:
 				getNodeExecutions().clear();
 				getNodeExecutions().addAll((Collection<? extends ActivityNodeExecution>)newValue);
 				return;
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__CALLER:
 				setCaller((CallActivityNodeExecution)newValue);
 				return;
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__ACTIVITY:
 				setActivity((Activity)newValue);
 				return;
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__ACTIVITY_EXECUTION_ID:
 				setActivityExecutionID((Integer)newValue);
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
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__PARAMETER_INPUTS:
 				getParameterInputs().clear();
 				return;
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__PARAMETER_OUTPUTS:
 				getParameterOutputs().clear();
 				return;
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__NODE_EXECUTIONS:
 				getNodeExecutions().clear();
 				return;
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__CALLER:
 				setCaller((CallActivityNodeExecution)null);
 				return;
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__ACTIVITY:
 				setActivity(ACTIVITY_EDEFAULT);
 				return;
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__ACTIVITY_EXECUTION_ID:
 				setActivityExecutionID(ACTIVITY_EXECUTION_ID_EDEFAULT);
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
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__PARAMETER_INPUTS:
 				return parameterInputs != null && !parameterInputs.isEmpty();
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__PARAMETER_OUTPUTS:
 				return parameterOutputs != null && !parameterOutputs.isEmpty();
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__NODE_EXECUTIONS:
 				return nodeExecutions != null && !nodeExecutions.isEmpty();
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__CALLER:
 				return caller != null;
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__ACTIVITY:
 				return ACTIVITY_EDEFAULT == null ? activity != null : !ACTIVITY_EDEFAULT.equals(activity);
 			case TracemodelPackageImpl.ACTIVITY_EXECUTION__ACTIVITY_EXECUTION_ID:
 				return activityExecutionID != ACTIVITY_EXECUTION_ID_EDEFAULT;
 		}
 		return eDynamicIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (activity: ");
 		result.append(activity);
 		result.append(", activityExecutionID: ");
 		result.append(activityExecutionID);
 		result.append(')');
 		return result.toString();
 	}
 
 	public List<ActivityNodeExecution> getNodeExecutionsByNode(ActivityNode node) {
 		List<ActivityNodeExecution> nodeExecutionsForNode = new ArrayList<ActivityNodeExecution>();
 		for(ActivityNodeExecution nodeExecution : this.nodeExecutions) {
 			if(nodeExecution.getNode().equals(node)) {
 				nodeExecutionsForNode.add(nodeExecution);
 			}
 		}
 		return nodeExecutionsForNode;
 	}
 
 	public List<ActivityNodeExecution> getNodeExecutionsByNodeWithoutOutput(
 			ActivityNode node) {
 		List<ActivityNodeExecution> nodeExecutionsForNodeWihtoutOutput = new ArrayList<ActivityNodeExecution>();
 		List<ActivityNodeExecution> nodeExecutionsForNode = getNodeExecutionsByNode(node);
 		for(ActivityNodeExecution nodeExecution : nodeExecutionsForNode) {
 			if(nodeExecution.getOutputs().size() == 0) {
 				nodeExecutionsForNodeWihtoutOutput.add(nodeExecution);
 			}
 		}
 		return nodeExecutionsForNodeWihtoutOutput;
 	}
 
 	public void addParameterInput(ActivityParameterNode activityParameterNode,
 			List<Value> values) {
 		addParameterInput(activityParameterNode, values, false);
 	}
 
 	public void addUserParameterInput(
 			ActivityParameterNode activityParameterNode, List<Value> values) {
 		addParameterInput(activityParameterNode, values, true);		
 	}
 
 	private void addParameterInput(ActivityParameterNode activityParameterNode, List<Value> values, boolean userInput) {
 		ParameterInput parameterInput = new ParameterInputImpl();	
 		if(userInput) {
 			parameterInput = new UserParameterInputImpl();
 		}
 		parameterInput.setInputParameterNode(activityParameterNode);
 		this.getParameterInputs().add(parameterInput);
 
 		for(Value value : values) {
 			ObjectTokenInstance tokenInstance = new ObjectTokenInstanceImpl();
 			ValueInstance valueInstance = new ValueInstanceImpl();
 
 			if(value instanceof Reference) {
 				value = ((Reference)value).referent.copy();
 			}
 			valueInstance.setValue(value);
 			tokenInstance.setValue(valueInstance);
 			parameterInput.getParameterInputTokens().add(tokenInstance);
 		}
 	}
 	
 	public void addParameterOutput(ActivityParameterNode activityParameterNode,
 			List<Value> values) {
 		ParameterOutput parameterOutput = new ParameterOutputImpl();
 		parameterOutput.setOutputParameterNode(activityParameterNode);
 
 		for(Value value : values) {
 			ObjectTokenInstance objectTokenInstance = new ObjectTokenInstanceImpl();
 			ValueInstance valueInstance = new ValueInstanceImpl();
 			if(value instanceof Reference) {
 				valueInstance.setValue(((Reference)value).referent.copy());
 			} else {
 				valueInstance.setValue(value.copy());
 			}
 			objectTokenInstance.setValue(valueInstance);
 
 			parameterOutput.getParameterOutputTokens().add(objectTokenInstance);
 		}
 		this.getParameterOutputs().add(parameterOutput);
 	}
 
 	public ActivityNodeExecution addActivityNodeExecution(ActivityNode activityNode) {
 		ActivityNodeExecution activityNodeExecution = new ActivityNodeExecutionImpl();
 		if(activityNode instanceof CallBehaviorAction) {
 			activityNodeExecution = new CallActivityNodeExecutionImpl();
 			((CallActivityNodeExecution)activityNodeExecution).setCalledBehavior(((CallBehaviorAction)activityNode).behavior);
 		}
 		activityNodeExecution.setNode(activityNode);	
 		activityNodeExecution.setActivityExecution(this);
 		return activityNodeExecution;
 	}
 
 	public ActivityNodeExecution getNodeExecutionByTokenOutput(TokenInstance tokenInstance) {
 		ActivityNodeExecution activityNodeExecution = null;
 		
 		for(ActivityNodeExecution nodeExecution : this.getNodeExecutions()) {
 			List<Output> outputs = nodeExecution.getOutputs();
 			for(Output output : outputs) {
 				if(output.getTokens().contains(tokenInstance)) {
 					activityNodeExecution = nodeExecution;
 					break;
 				}
 			}
 			if(activityNodeExecution != null) {
 				break;
 			}
 		}
 				
 		return activityNodeExecution;
 	}
 
 	public List<ActivityNodeExecution> getNodeExecutionsByTokenInput(TokenInstance tokenInstance) {
 		List<ActivityNodeExecution> activityNodeExecution = new ArrayList<ActivityNodeExecution>();
 
 		for(ActivityNodeExecution nodeExecution : this.getNodeExecutions()) {
 			List<Input> inputs = nodeExecution.getInputs();
 			for(Input input : inputs) {
 				if(input.getTokens().contains(tokenInstance)) {
 					activityNodeExecution.add(nodeExecution);
 				}
 			}
 		}
 
 		return activityNodeExecution;
 	}			
 
 	public void setActivityNodeExecutionFinishedExecution(ActivityNodeExecution activityNodeExecution) {
 		int indexOfLastExecutedNode = this.nodeExecutions.indexOf(activityNodeExecution);
 		if(indexOfLastExecutedNode == -1) {
 			return;
 		}
 		boolean found = false;
 		while(!found && indexOfLastExecutedNode > 0) {
 			--indexOfLastExecutedNode;
 			ActivityNodeExecution nodeExecution = this.nodeExecutions.get(indexOfLastExecutedNode);
 			if(nodeExecution.getOutputs() != null && nodeExecution.getOutputs().size() > 0) {
 				found = true;
 			}			
 		}
 		if(!found) {
 			indexOfLastExecutedNode = -1;
 		}
 		nodeExecutions.remove(activityNodeExecution);
 		nodeExecutions.add(indexOfLastExecutedNode + 1, activityNodeExecution);
 		return;
 	}
 } //ActivityExecutionImpl
