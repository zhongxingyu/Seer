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
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ActionExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ActivityExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ActivityNodeExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.CallActionExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.ControlNodeExecution;
 import org.modelexecution.fumldebug.core.trace.tracemodel.Input;
 import org.modelexecution.fumldebug.core.trace.tracemodel.InputParameterSetting;
 import org.modelexecution.fumldebug.core.trace.tracemodel.InputValue;
 import org.modelexecution.fumldebug.core.trace.tracemodel.Output;
 import org.modelexecution.fumldebug.core.trace.tracemodel.OutputParameterSetting;
 import org.modelexecution.fumldebug.core.trace.tracemodel.OutputValue;
 import org.modelexecution.fumldebug.core.trace.tracemodel.TokenInstance;
 import org.modelexecution.fumldebug.core.trace.tracemodel.Trace;
 import org.modelexecution.fumldebug.core.trace.tracemodel.TracemodelPackage;
 
 import fUML.Syntax.Actions.BasicActions.Action;
 import fUML.Syntax.Actions.BasicActions.InputPin;
 import fUML.Syntax.Actions.BasicActions.OutputPin;
 import fUML.Syntax.Actions.BasicActions.Pin;
 import fUML.Syntax.Activities.CompleteStructuredActivities.StructuredActivityNode;
 import fUML.Syntax.Activities.IntermediateActivities.Activity;
 import fUML.Syntax.Activities.IntermediateActivities.ActivityEdge;
 import fUML.Syntax.Activities.IntermediateActivities.ActivityNode;
 import fUML.Syntax.Activities.IntermediateActivities.ActivityNodeList;
 import fUML.Syntax.Activities.IntermediateActivities.ActivityParameterNode;
 import fUML.Syntax.Classes.Kernel.Parameter;
 import fUML.Syntax.Classes.Kernel.ParameterDirectionKind;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Activity Execution</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.modelexecution.fumldebug.core.trace.tracemodel.impl.ActivityExecutionImpl#getActivityInputs <em>Activity Inputs</em>}</li>
  *   <li>{@link org.modelexecution.fumldebug.core.trace.tracemodel.impl.ActivityExecutionImpl#getNodeExecutions <em>Node Executions</em>}</li>
  *   <li>{@link org.modelexecution.fumldebug.core.trace.tracemodel.impl.ActivityExecutionImpl#getCaller <em>Caller</em>}</li>
  *   <li>{@link org.modelexecution.fumldebug.core.trace.tracemodel.impl.ActivityExecutionImpl#getActivity <em>Activity</em>}</li>
  *   <li>{@link org.modelexecution.fumldebug.core.trace.tracemodel.impl.ActivityExecutionImpl#getActivityExecutionID <em>Activity Execution ID</em>}</li>
  *   <li>{@link org.modelexecution.fumldebug.core.trace.tracemodel.impl.ActivityExecutionImpl#getTrace <em>Trace</em>}</li>
  *   <li>{@link org.modelexecution.fumldebug.core.trace.tracemodel.impl.ActivityExecutionImpl#getActivityOutputs <em>Activity Outputs</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ActivityExecutionImpl extends EObjectImpl implements ActivityExecution {
 	
 	private HashMap<ActivityNode, List<ActivityNode>> predecessorMap = null;
 	private HashMap<ActivityNode, List<ActivityNode>> successorMap = null;
 	private HashMap<Pin, Action> pinOwnerships = null;
 	private List<ActivityParameterNode> inputActivityParamenterNodes;	
 	private List<ActivityParameterNode> outputActivityParameterNodes;
 	private List<Parameter> inputParameters;
 	private List<Parameter> outputParameters;
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2012 Vienna University of Technology.\r\nAll rights reserved. This program and the accompanying materials are made \r\navailable under the terms of the Eclipse Public License v1.0 which accompanies \r\nthis distribution, and is available at http://www.eclipse.org/legal/epl-v10.html\r\n\r\nContributors:\r\nTanja Mayerhofer - initial API and implementation";
 
 	/**
 	 * The cached value of the '{@link #getActivityInputs() <em>Activity Inputs</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getActivityInputs()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<InputParameterSetting> activityInputs;
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
 	protected CallActionExecution caller;
 
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
 	 * The cached value of the '{@link #getActivityOutputs() <em>Activity Outputs</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getActivityOutputs()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<OutputParameterSetting> activityOutputs;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ActivityExecutionImpl() {
 		super();
 	}		
 
 	private void initializeActivityExecution() {
 		if(this.activity == null) {
 			return;
 		}
 		initializePinOwnership(this.activity.node);
 		initializeSuccessorMap(this.activity.node);
 		initializePredecessorMap(this.activity.node);
 		initializeActivityParameter();
 	}
 	
 	private void initializeActivityParameter() {
 		inputActivityParamenterNodes = new ArrayList<ActivityParameterNode>();	
 		outputActivityParameterNodes = new ArrayList<ActivityParameterNode>();
 		inputParameters = new ArrayList<Parameter>();
 		outputParameters = new ArrayList<Parameter>();
 		
 		for(ActivityNode node : this.activity.node) {
 			if(node instanceof ActivityParameterNode) {
 				ActivityParameterNode activityParameterNode = (ActivityParameterNode)node;
 				Parameter parameter = activityParameterNode.parameter;
 				
 				if(parameter.direction == ParameterDirectionKind.in || parameter.direction == ParameterDirectionKind.inout) {
 					inputActivityParamenterNodes.add(activityParameterNode);
 					if(!inputParameters.contains(parameter)) {
 						inputParameters.add(parameter);
 					}
 				}
 				if(parameter.direction == ParameterDirectionKind.out || parameter.direction == ParameterDirectionKind.inout || parameter.direction == ParameterDirectionKind.return_) {
 					outputActivityParameterNodes.add(activityParameterNode);
 					if(!outputParameters.contains(parameter)) {
 						outputParameters.add(parameter);
 					}
 				}
 			}
 		}	
 		
 		for(Parameter parameter : this.activity.ownedParameter) {
 			if(parameter.direction == ParameterDirectionKind.in || parameter.direction == ParameterDirectionKind.inout) {
 				if(!inputParameters.contains(parameter)) {
 					inputParameters.add(parameter);
 				}
 			}
 			if(parameter.direction == ParameterDirectionKind.out || parameter.direction == ParameterDirectionKind.inout || parameter.direction == ParameterDirectionKind.return_) {
 				if(!outputParameters.contains(parameter)) {
 					outputParameters.add(parameter);
 				}
 			}
 		}
 	}
 
 	private void initializeSuccessorMap(ActivityNodeList nodes) {
		successorMap = new HashMap<ActivityNode, List<ActivityNode>>();
		
 		for(ActivityNode node : nodes) {
 			List<ActivityNode> successors = successorMap.get(node);
 			if(successors == null) {
 				successors = new ArrayList<ActivityNode>();
 				successorMap.put(node, successors);
 			}
 			
 			List<ActivityEdge> outgoingEdges = getOutgoingEdges(node);			
 			for(ActivityEdge edge : outgoingEdges) {
 				if(edge.target instanceof InputPin) {
 					successors.add(pinOwnerships.get(edge.target));					
 				} else {
 					successors.add(edge.target);
 				}
 			}
 			
 			if(node instanceof StructuredActivityNode) {
 				initializeSuccessorMap(((StructuredActivityNode)node).node);
 			}
 		}		
 	}
 
 	private List<ActivityEdge> getOutgoingEdges(ActivityNode node) {
 		List<ActivityEdge> outgoingEdges = new ArrayList<ActivityEdge>();
 		outgoingEdges.addAll(node.outgoing);
 		if(node instanceof Action) {
 			Action action = (Action)node;
 			for(OutputPin pin : action.output) {
 				outgoingEdges.addAll(pin.outgoing);
 			}
 		}
 		return outgoingEdges;
 	}
 
 	private void initializePredecessorMap(ActivityNodeList nodes) {
		predecessorMap = new HashMap<ActivityNode, List<ActivityNode>>();
		
 		for(ActivityNode node : nodes) {
 			List<ActivityNode> predecessors = predecessorMap.get(node);
 			if(predecessors == null) {
 				predecessors = new ArrayList<ActivityNode>();
 				predecessorMap.put(node, predecessors);
 			}
 			
 			List<ActivityEdge> incomingEdges = getIncomingEdges(node);			
 			for(ActivityEdge edge : incomingEdges) {
 				if(edge.source instanceof OutputPin) {
 					predecessors.add(pinOwnerships.get(edge.source));					
 				} else {
 					predecessors.add(edge.source);
 				}
 			}
 			
 			if(node instanceof StructuredActivityNode) {
				initializeSuccessorMap(((StructuredActivityNode)node).node);
 			}
 		}		
 	}
 	
 	private List<ActivityEdge> getIncomingEdges(ActivityNode node) {
 		List<ActivityEdge> incomingEdges = new ArrayList<ActivityEdge>();
 		incomingEdges.addAll(node.incoming);
 		if(node instanceof Action) {
 			Action action = (Action)node;
 			for(InputPin pin : action.input) {
 				incomingEdges.addAll(pin.incoming);
 			}
 		}
 		return incomingEdges;
 	}
 	
	private void initializePinOwnership(List<ActivityNode> nodes) {
		pinOwnerships = new HashMap<Pin, Action>();
		
 		for(ActivityNode node : nodes) {			
 			if(node instanceof Action) {
 				Action action = (Action)node;
 				for(InputPin inputPin : action.input) {
 					pinOwnerships.put(inputPin, action);
 				}
 				for(OutputPin outputPin : action.output) {
 					pinOwnerships.put(outputPin, action);
 				}
 			} 
 			if(node instanceof StructuredActivityNode) {
 				initializePinOwnership(((StructuredActivityNode)node).node);
 			}
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	
 	protected EClass eStaticClass() {
 		return TracemodelPackage.Literals.ACTIVITY_EXECUTION;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<InputParameterSetting> getActivityInputs() {
 		if (activityInputs == null) {
 			activityInputs = new EObjectContainmentEList<InputParameterSetting>(InputParameterSetting.class, this, TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_INPUTS);
 		}
 		return activityInputs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<ActivityNodeExecution> getNodeExecutions() {
 		if (nodeExecutions == null) {
 			nodeExecutions = new EObjectContainmentWithInverseEList<ActivityNodeExecution>(ActivityNodeExecution.class, this, TracemodelPackage.ACTIVITY_EXECUTION__NODE_EXECUTIONS, TracemodelPackage.ACTIVITY_NODE_EXECUTION__ACTIVITY_EXECUTION);
 		}
 		return nodeExecutions;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public CallActionExecution getCaller() {
 		if (caller != null && caller.eIsProxy()) {
 			InternalEObject oldCaller = (InternalEObject)caller;
 			caller = (CallActionExecution)eResolveProxy(oldCaller);
 			if (caller != oldCaller) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TracemodelPackage.ACTIVITY_EXECUTION__CALLER, oldCaller, caller));
 			}
 		}
 		return caller;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public CallActionExecution basicGetCaller() {
 		return caller;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetCaller(CallActionExecution newCaller, NotificationChain msgs) {
 		CallActionExecution oldCaller = caller;
 		caller = newCaller;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TracemodelPackage.ACTIVITY_EXECUTION__CALLER, oldCaller, newCaller);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCaller(CallActionExecution newCaller) {
 		if (newCaller != caller) {
 			NotificationChain msgs = null;
 			if (caller != null)
 				msgs = ((InternalEObject)caller).eInverseRemove(this, TracemodelPackage.CALL_ACTION_EXECUTION__CALLEE, CallActionExecution.class, msgs);
 			if (newCaller != null)
 				msgs = ((InternalEObject)newCaller).eInverseAdd(this, TracemodelPackage.CALL_ACTION_EXECUTION__CALLEE, CallActionExecution.class, msgs);
 			msgs = basicSetCaller(newCaller, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, TracemodelPackage.ACTIVITY_EXECUTION__CALLER, newCaller, newCaller));
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
 	 * @generated not
 	 */
 	public void setActivity(Activity newActivity) {
 		Activity oldActivity = activity;
 		activity = newActivity;
 		if (eNotificationRequired()) {
 			eNotify(new ENotificationImpl(this, Notification.SET, TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY, oldActivity, activity));
 		}
 		initializeActivityExecution();
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
 		int oldActivityExecutionID = activityExecutionID;
 		activityExecutionID = newActivityExecutionID;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_EXECUTION_ID, oldActivityExecutionID, activityExecutionID));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Trace getTrace() {
 		if (eContainerFeatureID() != TracemodelPackage.ACTIVITY_EXECUTION__TRACE) return null;
 		return (Trace)eContainer();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetTrace(Trace newTrace, NotificationChain msgs) {
 		msgs = eBasicSetContainer((InternalEObject)newTrace, TracemodelPackage.ACTIVITY_EXECUTION__TRACE, msgs);
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTrace(Trace newTrace) {
 		if (newTrace != eInternalContainer() || (eContainerFeatureID() != TracemodelPackage.ACTIVITY_EXECUTION__TRACE && newTrace != null)) {
 			if (EcoreUtil.isAncestor(this, newTrace))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
 			NotificationChain msgs = null;
 			if (eInternalContainer() != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newTrace != null)
 				msgs = ((InternalEObject)newTrace).eInverseAdd(this, TracemodelPackage.TRACE__ACTIVITY_EXECUTIONS, Trace.class, msgs);
 			msgs = basicSetTrace(newTrace, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, TracemodelPackage.ACTIVITY_EXECUTION__TRACE, newTrace, newTrace));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<OutputParameterSetting> getActivityOutputs() {
 		if (activityOutputs == null) {
 			activityOutputs = new EObjectContainmentEList<OutputParameterSetting>(OutputParameterSetting.class, this, TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_OUTPUTS);
 		}
 		return activityOutputs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case TracemodelPackage.ACTIVITY_EXECUTION__NODE_EXECUTIONS:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getNodeExecutions()).basicAdd(otherEnd, msgs);
 			case TracemodelPackage.ACTIVITY_EXECUTION__CALLER:
 				if (caller != null)
 					msgs = ((InternalEObject)caller).eInverseRemove(this, TracemodelPackage.CALL_ACTION_EXECUTION__CALLEE, CallActionExecution.class, msgs);
 				return basicSetCaller((CallActionExecution)otherEnd, msgs);
 			case TracemodelPackage.ACTIVITY_EXECUTION__TRACE:
 				if (eInternalContainer() != null)
 					msgs = eBasicRemoveFromContainer(msgs);
 				return basicSetTrace((Trace)otherEnd, msgs);
 		}
 		return eDynamicInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_INPUTS:
 				return ((InternalEList<?>)getActivityInputs()).basicRemove(otherEnd, msgs);
 			case TracemodelPackage.ACTIVITY_EXECUTION__NODE_EXECUTIONS:
 				return ((InternalEList<?>)getNodeExecutions()).basicRemove(otherEnd, msgs);
 			case TracemodelPackage.ACTIVITY_EXECUTION__CALLER:
 				return basicSetCaller(null, msgs);
 			case TracemodelPackage.ACTIVITY_EXECUTION__TRACE:
 				return basicSetTrace(null, msgs);
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_OUTPUTS:
 				return ((InternalEList<?>)getActivityOutputs()).basicRemove(otherEnd, msgs);
 		}
 		return eDynamicInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	
 	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
 		switch (eContainerFeatureID()) {
 			case TracemodelPackage.ACTIVITY_EXECUTION__TRACE:
 				return eInternalContainer().eInverseRemove(this, TracemodelPackage.TRACE__ACTIVITY_EXECUTIONS, Trace.class, msgs);
 		}
 		return eDynamicBasicRemoveFromContainer(msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_INPUTS:
 				return getActivityInputs();
 			case TracemodelPackage.ACTIVITY_EXECUTION__NODE_EXECUTIONS:
 				return getNodeExecutions();
 			case TracemodelPackage.ACTIVITY_EXECUTION__CALLER:
 				if (resolve) return getCaller();
 				return basicGetCaller();
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY:
 				return getActivity();
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_EXECUTION_ID:
 				return getActivityExecutionID();
 			case TracemodelPackage.ACTIVITY_EXECUTION__TRACE:
 				return getTrace();
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_OUTPUTS:
 				return getActivityOutputs();
 		}
 		return eDynamicGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_INPUTS:
 				getActivityInputs().clear();
 				getActivityInputs().addAll((Collection<? extends InputParameterSetting>)newValue);
 				return;
 			case TracemodelPackage.ACTIVITY_EXECUTION__NODE_EXECUTIONS:
 				getNodeExecutions().clear();
 				getNodeExecutions().addAll((Collection<? extends ActivityNodeExecution>)newValue);
 				return;
 			case TracemodelPackage.ACTIVITY_EXECUTION__CALLER:
 				setCaller((CallActionExecution)newValue);
 				return;
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY:
 				setActivity((Activity)newValue);
 				return;
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_EXECUTION_ID:
 				setActivityExecutionID((Integer)newValue);
 				return;
 			case TracemodelPackage.ACTIVITY_EXECUTION__TRACE:
 				setTrace((Trace)newValue);
 				return;
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_OUTPUTS:
 				getActivityOutputs().clear();
 				getActivityOutputs().addAll((Collection<? extends OutputParameterSetting>)newValue);
 				return;
 		}
 		eDynamicSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_INPUTS:
 				getActivityInputs().clear();
 				return;
 			case TracemodelPackage.ACTIVITY_EXECUTION__NODE_EXECUTIONS:
 				getNodeExecutions().clear();
 				return;
 			case TracemodelPackage.ACTIVITY_EXECUTION__CALLER:
 				setCaller((CallActionExecution)null);
 				return;
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY:
 				setActivity(ACTIVITY_EDEFAULT);
 				return;
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_EXECUTION_ID:
 				setActivityExecutionID(ACTIVITY_EXECUTION_ID_EDEFAULT);
 				return;
 			case TracemodelPackage.ACTIVITY_EXECUTION__TRACE:
 				setTrace((Trace)null);
 				return;
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_OUTPUTS:
 				getActivityOutputs().clear();
 				return;
 		}
 		eDynamicUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_INPUTS:
 				return activityInputs != null && !activityInputs.isEmpty();
 			case TracemodelPackage.ACTIVITY_EXECUTION__NODE_EXECUTIONS:
 				return nodeExecutions != null && !nodeExecutions.isEmpty();
 			case TracemodelPackage.ACTIVITY_EXECUTION__CALLER:
 				return caller != null;
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY:
 				return ACTIVITY_EDEFAULT == null ? activity != null : !ACTIVITY_EDEFAULT.equals(activity);
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_EXECUTION_ID:
 				return activityExecutionID != ACTIVITY_EXECUTION_ID_EDEFAULT;
 			case TracemodelPackage.ACTIVITY_EXECUTION__TRACE:
 				return getTrace() != null;
 			case TracemodelPackage.ACTIVITY_EXECUTION__ACTIVITY_OUTPUTS:
 				return activityOutputs != null && !activityOutputs.isEmpty();
 		}
 		return eDynamicIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	
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
 
 	public List<ActivityNodeExecution> getNodeExecutionsWithTokenOutput(TokenInstance tokenInstance) {
 		Set<ActivityNodeExecution> nodeExecutions = new HashSet<ActivityNodeExecution>();	
 		
 		for(ActivityNodeExecution nodeExecution : this.getNodeExecutions()) {
 			if(nodeExecution instanceof ActionExecution) {
 				ActionExecution actionExecution = (ActionExecution)nodeExecution;
 				List<Output> outputs = actionExecution.getOutputs();
 				for(Output output : outputs) {
 					for(OutputValue outputValue : output.getOutputValues()) {
 						if(outputValue.getOutputObjectToken().equals(tokenInstance)) {
 							nodeExecutions.add(nodeExecution);
 							break;
 						}
 					}
 				}
 			} else if (nodeExecution instanceof ControlNodeExecution){
 				ControlNodeExecution controlExecution = (ControlNodeExecution)nodeExecution;
 				if(controlExecution.getRoutedTokens().contains(tokenInstance)) {
 					nodeExecutions.add(nodeExecution);
 				}
 			}
 		}			
 		List<ActivityNodeExecution> nodeExecutionsList = new ArrayList<ActivityNodeExecution>(nodeExecutions);	
 		return nodeExecutionsList;
 	}
 
 	public List<ActivityNodeExecution> getNodeExecutionsWithTokenInput(TokenInstance tokenInstance) {
 		Set<ActivityNodeExecution> nodeExecutions = new HashSet<ActivityNodeExecution>();
 
 		for(ActivityNodeExecution nodeExecution : this.getNodeExecutions()) {
 			if(nodeExecution instanceof ActionExecution) {
 				ActionExecution actionExecution = (ActionExecution)nodeExecution;
 				List<Input> inputs = actionExecution.getInputs();
 				for(Input input : inputs) {
 					for(InputValue inputValue : input.getInputValues()) {
 						if(inputValue.getInputObjectToken().equals(tokenInstance)) {
 							nodeExecutions.add(nodeExecution);
 						}
 					}
 				}
 			} else if(nodeExecution instanceof ControlNodeExecution) {
 				ControlNodeExecution controlExecution = (ControlNodeExecution)nodeExecution;
 				if(controlExecution.getRoutedTokens().contains(tokenInstance)) {
 					nodeExecutions.add(nodeExecution);
 				}
 			}
 		}
 		
 		List<ActivityNodeExecution> nodeExecutionsList = new ArrayList<ActivityNodeExecution>(nodeExecutions);
 		return nodeExecutionsList;
 	}				
 			
 	public ActivityNodeExecution getExecutionForEnabledNode(ActivityNode node) {
 		List<ActivityNodeExecution> nodeExecutionsForNode = getNodeExecutionsByNode(node);
 		for(ActivityNodeExecution nodeExecution : nodeExecutionsForNode) {
 			if(!nodeExecution.isExecuted()) {
 				return nodeExecution;
 			}
 		}
 		return null;
 	}
 
 	
 	public List<ActivityParameterNode> getInputActivityParamenterNodes() {
 		return this.inputActivityParamenterNodes;
 	}
 
 	
 	public List<ActivityParameterNode> getOutputActivityParameterNodes() {
 		return this.outputActivityParameterNodes;
 	}
 
 	
 	public List<Parameter> getInputParameters() {
 		return this.inputParameters;
 	}
 
 	
 	public List<Parameter> getOutputParameters() {
 		return this.outputParameters;
 	} 
 		
 	
 	public List<ActivityNode> getReachablePredecessorNodes(ActivityNode node) {
 		return predecessorMap.get(node);
 	}
 	
 	
 	public List<ActivityNode> getReachableSuccessorNodes(ActivityNode node) {
 		return successorMap.get(node);
 	}
 	
 } //ActivityExecutionImpl
