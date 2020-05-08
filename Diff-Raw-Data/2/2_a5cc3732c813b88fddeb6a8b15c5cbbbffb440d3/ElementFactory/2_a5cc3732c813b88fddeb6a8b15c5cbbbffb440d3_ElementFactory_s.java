 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  */
 package org.modelexecution.fuml.convert.xmof.internal;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EParameter;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.ETypedElement;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.CallBehaviorAction;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.CallOperationAction;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.InputPin;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.OutputPin;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.SendSignalAction;
 import org.modelexecution.xmof.Syntax.Actions.CompleteActions.AcceptEventAction;
 import org.modelexecution.xmof.Syntax.Actions.CompleteActions.ReadExtentAction;
 import org.modelexecution.xmof.Syntax.Actions.CompleteActions.ReadIsClassifiedObjectAction;
 import org.modelexecution.xmof.Syntax.Actions.CompleteActions.ReclassifyObjectAction;
 import org.modelexecution.xmof.Syntax.Actions.CompleteActions.ReduceAction;
 import org.modelexecution.xmof.Syntax.Actions.CompleteActions.StartClassifierBehaviorAction;
 import org.modelexecution.xmof.Syntax.Actions.CompleteActions.StartObjectBehaviorAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.AddStructuralFeatureValueAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.ClearAssociationAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.ClearStructuralFeatureAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.CreateLinkAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.CreateObjectAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.DestroyLinkAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.DestroyObjectAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.LinkEndCreationData;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.LinkEndData;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.LinkEndDestructionData;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.ReadLinkAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.ReadSelfAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.ReadStructuralFeatureAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.RemoveStructuralFeatureValueAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.TestIdentityAction;
 import org.modelexecution.xmof.Syntax.Actions.IntermediateActions.ValueSpecificationAction;
 import org.modelexecution.xmof.Syntax.Activities.CompleteStructuredActivities.Clause;
 import org.modelexecution.xmof.Syntax.Activities.CompleteStructuredActivities.ConditionalNode;
 import org.modelexecution.xmof.Syntax.Activities.CompleteStructuredActivities.LoopNode;
 import org.modelexecution.xmof.Syntax.Activities.CompleteStructuredActivities.StructuredActivityNode;
 import org.modelexecution.xmof.Syntax.Activities.ExtraStructuredActivities.ExpansionNode;
 import org.modelexecution.xmof.Syntax.Activities.ExtraStructuredActivities.ExpansionRegion;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.Activity;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ActivityFinalNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ActivityParameterNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ControlFlow;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.DecisionNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ForkNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.InitialNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.JoinNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.MergeNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ObjectFlow;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.InstanceSpecification;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.InstanceValue;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.LiteralBoolean;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.LiteralInteger;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.LiteralNull;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.LiteralString;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.LiteralUnlimitedNatural;
 import org.modelexecution.xmof.Syntax.Classes.Kernel.Slot;
 import org.modelexecution.xmof.Syntax.CommonBehaviors.BasicBehaviors.FunctionBehavior;
 import org.modelexecution.xmof.Syntax.CommonBehaviors.BasicBehaviors.OpaqueBehavior;
 import org.modelexecution.xmof.Syntax.CommonBehaviors.Communications.Reception;
 import org.modelexecution.xmof.Syntax.CommonBehaviors.Communications.Signal;
 import org.modelexecution.xmof.Syntax.CommonBehaviors.Communications.SignalEvent;
 import org.modelexecution.xmof.Syntax.CommonBehaviors.Communications.Trigger;
 
 /**
  * Factory for {@link fUML.Syntax.Classes.Kernel.Element fUML elements}.
  * 
  * @author Philip Langer (langer@big.tuwien.ac.at)
  * 
  */
 public class ElementFactory {
 
 	public fUML.Syntax.Classes.Kernel.Element create(EObject element) {
 		String className = element.eClass().getName();
 		switch (className) {
 		case "EReference":
 			return create((EReference) element);
 		case "EAttribute":
 			return create((EAttribute) element);
 		case "EClass":
 			return create((EClass) element);
 		case "BehavioredEClass":
 			return create((EClass) element);
 		case "MainEClass":
 			return create((EClass) element);
 		case "EDataType":
 			return create((EDataType) element);
 		case "EEnum":
 			return create((EEnum) element);
 		case "EEnumLiteral":
 			return create((EEnumLiteral) element);
 		case "EOperation":
 			return create((EOperation) element);
 		case "BehavioredEOperation":
 			return create((EOperation) element);
 		case "EPackage":
 			return create((EPackage) element);
 		case "EParameter":
 			return create((EParameter) element);
 		case "DirectedParameter":
 			return create((EParameter) element);
 		
 		case "ETypedElement":
 			return create((ETypedElement) element);
 
 		case "InstanceSpecification":
 			return create((InstanceSpecification) element);
 		case "InstanceValue":
 			return create((InstanceValue) element);
 		case "LiteralBoolean":
 			return create((LiteralBoolean) element);
 		case "LiteralInteger":
 			return create((LiteralInteger) element);
 		case "LiteralNull":
 			return create((LiteralNull) element);
 		case "LiteralString":
 			return create((LiteralString) element);
 		case "LiteralUnlimitedNatural":
 			return create((LiteralUnlimitedNatural) element);
 		case "Slot":
 			return create((Slot) element);
 		case "FunctionBehavior":
 			return create((FunctionBehavior) element);
 		case "OpaqueBehavior":
 			return create((OpaqueBehavior) element);
 		case "Reception":
 			return create((Reception) element);
 		case "Signal":
 			return create((Signal) element);
 		case "SignalEvent":
 			return create((SignalEvent) element);
 		case "Trigger":
 			return create((Trigger) element);
 		case "CallBehaviorAction":
 			return create((CallBehaviorAction) element);
 		case "CallOperationAction":
 			return create((CallOperationAction) element);
 		case "InputPin":
 			return create((InputPin) element);
 		case "OutputPin":
 			return create((OutputPin) element);
 		case "SendSignalAction":
 			return create((SendSignalAction) element);
 		case "AddStructuralFeatureValueAction":
 			return create((AddStructuralFeatureValueAction) element);
 		case "ClearAssociationAction":
 			return create((ClearAssociationAction) element);
 		case "ClearStructuralFeatureAction":
 			return create((ClearStructuralFeatureAction) element);
 		case "CreateLinkAction":
 			return create((CreateLinkAction) element);
 		case "CreateObjectAction":
 			return create((CreateObjectAction) element);
 		case "DestroyLinkAction":
 			return create((DestroyLinkAction) element);
 		case "DestroyObjectAction":
 			return create((DestroyObjectAction) element);
 		case "LinkEndCreationData":
 			return create((LinkEndCreationData) element);
 		case "LinkEndData":
 			return create((LinkEndData) element);
 		case "LinkEndDestructionData":
 			return create((LinkEndDestructionData) element);
 		case "ReadLinkAction":
 			return create((ReadLinkAction) element);
 		case "ReadSelfAction":
 			return create((ReadSelfAction) element);
 		case "ReadStructuralFeatureAction":
 			return create((ReadStructuralFeatureAction) element);
 		case "TestIdentityAction":
 			return create((TestIdentityAction) element);
 		case "ValueSpecificationAction":
 			return create((ValueSpecificationAction) element);
 		case "AcceptEventAction":
 			return create((AcceptEventAction) element);
 		case "ReadExtentAction":
 			return create((ReadExtentAction) element);
 		case "ReadIsClassifiedObjectAction":
 			return create((ReadIsClassifiedObjectAction) element);
 		case "ReclassifyObjectAction":
 			return create((ReclassifyObjectAction) element);
 		case "ReduceAction":
 			return create((ReduceAction) element);
 		case "StartClassifierBehaviorAction":
 			return create((StartClassifierBehaviorAction) element);
 		case "StartObjectBehaviorAction":
 			return create((StartObjectBehaviorAction) element);
 		case "Activity":
 			return create((Activity) element);
 		case "ActivityFinalNode":
 			return create((ActivityFinalNode) element);
 		case "ActivityParameterNode":
 			return create((ActivityParameterNode) element);
 		case "ControlFlow":
 			return create((ControlFlow) element);
 		case "DecisionNode":
 			return create((DecisionNode) element);
 		case "ForkNode":
 			return create((ForkNode) element);
 		case "InitialNode":
 			return create((InitialNode) element);
 		case "JoinNode":
 			return create((JoinNode) element);
 		case "MergeNode":
 			return create((MergeNode) element);
 		case "ObjectFlow":
 			return create((ObjectFlow) element);
 		case "Clause":
 			return create((Clause) element);
 		case "ConditionalNode":
 			return create((ConditionalNode) element);
 		case "LoopNode":
 			return create((LoopNode) element);
 		case "StructuredActivityNode":
 			return create((StructuredActivityNode) element);
 		case "ExpansionNode":
 			return create((ExpansionNode) element);
 		case "ExpansionRegion":
 			return create((ExpansionRegion) element);
 		}
 		return null;
 	}
 
 	public fUML.Syntax.Classes.Kernel.Association create(EReference element) {
 		return new fUML.Syntax.Classes.Kernel.Association();
 	}
 
 	public fUML.Syntax.Classes.Kernel.Property create(EAttribute element) {
 		return new fUML.Syntax.Classes.Kernel.Property();
 	}
 
 	public fUML.Syntax.Classes.Kernel.Class_ create(EClass element) {
 		return new fUML.Syntax.Classes.Kernel.Class_();
 	}
 
 	public fUML.Syntax.Classes.Kernel.DataType create(EDataType element) {
 		return new fUML.Syntax.Classes.Kernel.DataType();
 	}
 
 	public fUML.Syntax.Classes.Kernel.Enumeration create(EEnum element) {
 		return new fUML.Syntax.Classes.Kernel.Enumeration();
 	}
 
 	public fUML.Syntax.Classes.Kernel.EnumerationLiteral create(
 			EEnumLiteral element) {
 		return new fUML.Syntax.Classes.Kernel.EnumerationLiteral();
 	}
 
 	public fUML.Syntax.Classes.Kernel.InstanceSpecification create(
 			InstanceSpecification element) {
 		return new fUML.Syntax.Classes.Kernel.InstanceSpecification();
 	}
 
 	public fUML.Syntax.Classes.Kernel.InstanceValue create(InstanceValue element) {
 		return new fUML.Syntax.Classes.Kernel.InstanceValue();
 	}
 
 	public fUML.Syntax.Classes.Kernel.LiteralBoolean create(
 			LiteralBoolean element) {
 		return new fUML.Syntax.Classes.Kernel.LiteralBoolean();
 	}
 
 	public fUML.Syntax.Classes.Kernel.LiteralInteger create(
 			LiteralInteger element) {
 		return new fUML.Syntax.Classes.Kernel.LiteralInteger();
 	}
 
 	public fUML.Syntax.Classes.Kernel.LiteralNull create(LiteralNull element) {
 		return new fUML.Syntax.Classes.Kernel.LiteralNull();
 	}
 
 	public fUML.Syntax.Classes.Kernel.LiteralString create(LiteralString element) {
 		return new fUML.Syntax.Classes.Kernel.LiteralString();
 	}
 
 	public fUML.Syntax.Classes.Kernel.LiteralUnlimitedNatural create(
 			LiteralUnlimitedNatural element) {
 		return new fUML.Syntax.Classes.Kernel.LiteralUnlimitedNatural();
 	}
 
 	public fUML.Syntax.Classes.Kernel.Operation create(EOperation element) {
 		return new fUML.Syntax.Classes.Kernel.Operation();
 	}
 
 	public fUML.Syntax.Classes.Kernel.Package create(EPackage element) {
 		return new fUML.Syntax.Classes.Kernel.Package();
 	}
 
 	public fUML.Syntax.Classes.Kernel.Parameter create(EParameter element) {
 		return new fUML.Syntax.Classes.Kernel.Parameter();
 	}
 
 	public fUML.Syntax.Classes.Kernel.Slot create(Slot element) {
 		return new fUML.Syntax.Classes.Kernel.Slot();
 	}
 
 	public fUML.Syntax.Classes.Kernel.TypedElement create(ETypedElement element) {
 		return new fUML.Syntax.Classes.Kernel.TypedElement();
 	}
 
 	public fUML.Syntax.CommonBehaviors.BasicBehaviors.FunctionBehavior create(
 			FunctionBehavior element) {
 		return new fUML.Syntax.CommonBehaviors.BasicBehaviors.FunctionBehavior();
 	}
 
 	public fUML.Syntax.CommonBehaviors.BasicBehaviors.OpaqueBehavior create(
 			OpaqueBehavior element) {
 		return new fUML.Syntax.CommonBehaviors.BasicBehaviors.OpaqueBehavior();
 	}
 
 	public fUML.Syntax.CommonBehaviors.Communications.Reception create(
 			Reception element) {
 		return new fUML.Syntax.CommonBehaviors.Communications.Reception();
 	}
 
 	public fUML.Syntax.CommonBehaviors.Communications.Signal create(
 			Signal element) {
 		return new fUML.Syntax.CommonBehaviors.Communications.Signal();
 	}
 
 	public fUML.Syntax.CommonBehaviors.Communications.SignalEvent create(
 			SignalEvent element) {
 		return new fUML.Syntax.CommonBehaviors.Communications.SignalEvent();
 	}
 
 	public fUML.Syntax.CommonBehaviors.Communications.Trigger create(
 			Trigger element) {
 		return new fUML.Syntax.CommonBehaviors.Communications.Trigger();
 	}
 
 	public fUML.Syntax.Actions.BasicActions.CallBehaviorAction create(
 			CallBehaviorAction element) {
 		return new fUML.Syntax.Actions.BasicActions.CallBehaviorAction();
 	}
 
 	public fUML.Syntax.Actions.BasicActions.CallOperationAction create(
 			CallOperationAction element) {
 		return new fUML.Syntax.Actions.BasicActions.CallOperationAction();
 	}
 
 	public fUML.Syntax.Actions.BasicActions.InputPin create(InputPin element) {
 		return new fUML.Syntax.Actions.BasicActions.InputPin();
 	}
 
 	public fUML.Syntax.Actions.BasicActions.OutputPin create(OutputPin element) {
 		return new fUML.Syntax.Actions.BasicActions.OutputPin();
 	}
 
 	public fUML.Syntax.Actions.BasicActions.SendSignalAction create(
 			SendSignalAction element) {
 		return new fUML.Syntax.Actions.BasicActions.SendSignalAction();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.AddStructuralFeatureValueAction create(
 			AddStructuralFeatureValueAction element) {
 		return new fUML.Syntax.Actions.IntermediateActions.AddStructuralFeatureValueAction();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.ClearAssociationAction create(
 			ClearAssociationAction element) {
 		return new fUML.Syntax.Actions.IntermediateActions.ClearAssociationAction();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.ClearStructuralFeatureAction create(
 			ClearStructuralFeatureAction element) {
 		return new fUML.Syntax.Actions.IntermediateActions.ClearStructuralFeatureAction();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.CreateLinkAction create(
 			CreateLinkAction element) {
 		return new fUML.Syntax.Actions.IntermediateActions.CreateLinkAction();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.CreateObjectAction create(
 			CreateObjectAction element) {
 		return new fUML.Syntax.Actions.IntermediateActions.CreateObjectAction();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.DestroyLinkAction create(
 			DestroyLinkAction element) {
 		return new fUML.Syntax.Actions.IntermediateActions.DestroyLinkAction();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.DestroyObjectAction create(
 			DestroyObjectAction element) {
 		return new fUML.Syntax.Actions.IntermediateActions.DestroyObjectAction();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.LinkEndCreationData create(
 			LinkEndCreationData element) {
 		return new fUML.Syntax.Actions.IntermediateActions.LinkEndCreationData();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.LinkEndData create(
 			LinkEndData element) {
 		return new fUML.Syntax.Actions.IntermediateActions.LinkEndData();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.LinkEndDestructionData create(
 			LinkEndDestructionData element) {
 		return new fUML.Syntax.Actions.IntermediateActions.LinkEndDestructionData();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.ReadLinkAction create(
 			ReadLinkAction element) {
 		return new fUML.Syntax.Actions.IntermediateActions.ReadLinkAction();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.ReadSelfAction create(
 			ReadSelfAction element) {
 		return new fUML.Syntax.Actions.IntermediateActions.ReadSelfAction();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.ReadStructuralFeatureAction create(
 			ReadStructuralFeatureAction element) {
 		return new fUML.Syntax.Actions.IntermediateActions.ReadStructuralFeatureAction();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.RemoveStructuralFeatureValueAction create(
 			RemoveStructuralFeatureValueAction element) {
 		return new fUML.Syntax.Actions.IntermediateActions.RemoveStructuralFeatureValueAction();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.TestIdentityAction create(
 			TestIdentityAction element) {
 		return new fUML.Syntax.Actions.IntermediateActions.TestIdentityAction();
 	}
 
 	public fUML.Syntax.Actions.IntermediateActions.ValueSpecificationAction create(
 			ValueSpecificationAction element) {
 		return new fUML.Syntax.Actions.IntermediateActions.ValueSpecificationAction();
 	}
 
 	public fUML.Syntax.Actions.CompleteActions.AcceptEventAction create(
 			AcceptEventAction element) {
 		return new fUML.Syntax.Actions.CompleteActions.AcceptEventAction();
 	}
 
 	public fUML.Syntax.Actions.CompleteActions.ReadExtentAction create(
 			ReadExtentAction element) {
 		return new fUML.Syntax.Actions.CompleteActions.ReadExtentAction();
 	}
 
 	public fUML.Syntax.Actions.CompleteActions.ReadIsClassifiedObjectAction create(
 			ReadIsClassifiedObjectAction element) {
 		return new fUML.Syntax.Actions.CompleteActions.ReadIsClassifiedObjectAction();
 	}
 
 	public fUML.Syntax.Actions.CompleteActions.ReclassifyObjectAction create(
 			ReclassifyObjectAction element) {
 		return new fUML.Syntax.Actions.CompleteActions.ReclassifyObjectAction();
 	}
 
 	public fUML.Syntax.Actions.CompleteActions.ReduceAction create(
 			ReduceAction element) {
 		return new fUML.Syntax.Actions.CompleteActions.ReduceAction();
 	}
 
 	public fUML.Syntax.Actions.CompleteActions.StartClassifierBehaviorAction create(
 			StartClassifierBehaviorAction element) {
 		return new fUML.Syntax.Actions.CompleteActions.StartClassifierBehaviorAction();
 	}
 
 	public fUML.Syntax.Actions.CompleteActions.StartObjectBehaviorAction create(
 			StartObjectBehaviorAction element) {
 		return new fUML.Syntax.Actions.CompleteActions.StartObjectBehaviorAction();
 	}
 
 	public fUML.Syntax.Activities.IntermediateActivities.Activity create(
 			Activity element) {
 		return new fUML.Syntax.Activities.IntermediateActivities.Activity();
 	}
 
 	public fUML.Syntax.Activities.IntermediateActivities.ActivityFinalNode create(
 			ActivityFinalNode element) {
 		return new fUML.Syntax.Activities.IntermediateActivities.ActivityFinalNode();
 	}
 
 	public fUML.Syntax.Activities.IntermediateActivities.ActivityParameterNode create(
 			ActivityParameterNode element) {
 		return new fUML.Syntax.Activities.IntermediateActivities.ActivityParameterNode();
 	}
 
 	public fUML.Syntax.Activities.IntermediateActivities.ControlFlow create(
 			ControlFlow element) {
 		return new fUML.Syntax.Activities.IntermediateActivities.ControlFlow();
 	}
 
 	public fUML.Syntax.Activities.IntermediateActivities.DecisionNode create(
 			DecisionNode element) {
 		return new fUML.Syntax.Activities.IntermediateActivities.DecisionNode();
 	}
 
 	public fUML.Syntax.Activities.IntermediateActivities.ForkNode create(
 			ForkNode element) {
 		return new fUML.Syntax.Activities.IntermediateActivities.ForkNode();
 	}
 
 	public fUML.Syntax.Activities.IntermediateActivities.InitialNode create(
 			InitialNode element) {
 		return new fUML.Syntax.Activities.IntermediateActivities.InitialNode();
 	}
 
 	public fUML.Syntax.Activities.IntermediateActivities.JoinNode create(
 			JoinNode element) {
 		return new fUML.Syntax.Activities.IntermediateActivities.JoinNode();
 	}
 
 	public fUML.Syntax.Activities.IntermediateActivities.MergeNode create(
 			MergeNode element) {
 		return new fUML.Syntax.Activities.IntermediateActivities.MergeNode();
 	}
 
 	public fUML.Syntax.Activities.IntermediateActivities.ObjectFlow create(
 			ObjectFlow element) {
 		return new fUML.Syntax.Activities.IntermediateActivities.ObjectFlow();
 	}
 
 	public fUML.Syntax.Activities.CompleteStructuredActivities.Clause create(
 			Clause element) {
 		return new fUML.Syntax.Activities.CompleteStructuredActivities.Clause();
 	}
 
 	public fUML.Syntax.Activities.CompleteStructuredActivities.ConditionalNode create(
 			ConditionalNode element) {
 		return new fUML.Syntax.Activities.CompleteStructuredActivities.ConditionalNode();
 	}
 
 	public fUML.Syntax.Activities.CompleteStructuredActivities.LoopNode create(
 			LoopNode element) {
 		return new fUML.Syntax.Activities.CompleteStructuredActivities.LoopNode();
 	}
 
 	public fUML.Syntax.Activities.CompleteStructuredActivities.StructuredActivityNode create(
 			StructuredActivityNode element) {
 		return new fUML.Syntax.Activities.CompleteStructuredActivities.StructuredActivityNode();
 	}
 
 	public fUML.Syntax.Activities.ExtraStructuredActivities.ExpansionNode create(
 			ExpansionNode element) {
 		return new fUML.Syntax.Activities.ExtraStructuredActivities.ExpansionNode();
 	}
 
 	public fUML.Syntax.Activities.ExtraStructuredActivities.ExpansionRegion create(
 			ExpansionRegion element) {
 		return new fUML.Syntax.Activities.ExtraStructuredActivities.ExpansionRegion();
 	}
 
 }
